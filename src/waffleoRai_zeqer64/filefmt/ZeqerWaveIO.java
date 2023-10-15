package waffleoRai_zeqer64.filefmt;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import waffleoRai_Sound.AiffFile;
import waffleoRai_Sound.PCM16Sound;
import waffleoRai_Sound.WAV;
import waffleoRai_Sound.nintendo.N64ADPCMTable;
import waffleoRai_Sound.nintendo.Z64ADPCM;
import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_Sound.nintendo.Z64Wave;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.FileUtils;

public class ZeqerWaveIO {
	
	public static final int ERROR_CODE_UNKNOWN = -1;
	public static final int ERROR_CODE_NONE = 0;
	public static final int ERROR_CODE_IO = 1;
	public static final int ERROR_CODE_INPARSE = 2;
	public static final int ERROR_CODE_DOWNSAMPLE_FAIL = 3;
	public static final int ERROR_CODE_TBLGEN_FAIL = 4;
	public static final int ERROR_CODE_ENCODE_FAIL = 5;
	public static final int ERROR_CODE_AIFF_COMPRESSION_UNKNOWN = 6;
	public static final int ERROR_CODE_INPUT_FMT_UNKNOWN = 7;
	public static final int ERROR_CODE_TABLE_IMPORT_FAILED = 8;
	public static final int ERROR_CODE_MULTICHAN_NOT_SUPPORTED = 9;
	
	public static final int MULTICHAN_IMPORT_SUM = 0;
	public static final int MULTICHAN_IMPORT_LEFTONLY = 1;
	public static final int MULTICHAN_IMPORT_RIGHTONLY = 2;
	public static final int MULTICHAN_IMPORT_SPECIFY = 3;
	
	public static class SampleImportOptions{
		public boolean useDefaultADPCMTable = true;
		public boolean twoBit = false;
		public int maxSampleRate = 32000;
		public int minSampleRate = 1000;
		public int order = 2;
		public int multiChannelBehavior = MULTICHAN_IMPORT_LEFTONLY; //Or channel 0
		public int loopStart = -1; //Optional to manually enter
		public int loopEnd = -1; //Optional to manually enter
		public int loopCount = -1; //Optional to manually enter
		
		//Add some metadata options so CAN manually enter
		public String namestem = null;
		public int medium = Z64Sound.MEDIUM_CART;
		public boolean flagActor = false;
		public boolean flagMusic = false;
		public boolean flagSFX = false;
		public boolean flagEnv = false;
		public boolean flagVox = false;
		
		//These are updated by the file reader
		public int bitDepth = 16;
		public int srcSampleRate = 0;
		public int channel = 0;
	}
	
	public static class SampleImportResult{
		public Z64WaveInfo info;
		public FileBuffer data;
		public byte[] md5;
		public int error = ERROR_CODE_NONE;
	}
	
	public static boolean exportWAV(String path, Z64Wave sample){
		if(sample == null) return false;
		try{
			sample.writeToWav(path);
		}
		catch(Exception ex){
			ex.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static boolean exportAIFF(String path, Z64Wave sample){
		if(sample == null) return false;
		try{
			ZRetAifcFile.exportAIFF(sample, path);
		}
		catch(Exception ex){
			ex.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static boolean exportAIFC(String path, Z64Wave sample){
		if(sample == null) return false;
		try{
			ZRetAifcFile.exportAIFC(sample, path);
		}
		catch(Exception ex){
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	private static int[] sumPCM16Channels(List<int[]> channelData, int frames){
		//I guess it technically averages them. Oh well.
		int[] out = new int[frames];
		int chcount = channelData.size();
		for(int[] ch : channelData){
			for(int f = 0; f < frames; f++){
				if(f >= ch.length) break;
				out[f] += ch[f];
			}
		}
		
		for(int f = 0; f < frames; f++){
			out[f] /= chcount;
		}
		
		return out;
	}
	
	public static SampleImportResult importWAV(String path, SampleImportOptions options){
		SampleImportResult res = new SampleImportResult();
		
		try {
			WAV wave = new WAV(path);
			options.srcSampleRate = wave.getSampleRate();
			options.bitDepth = 16; //Because we scale to 16 when we pull the sample array
			
			if(options.loopStart < 0){
				//Not manually set.
				if(wave.loops()){
					options.loopCount = -1;
					options.loopStart = wave.getLoopFrame();
					options.loopEnd = wave.getLoopEndFrame();
				}
				else{
					options.loopCount = 0;
					options.loopStart = 0;
					options.loopEnd = wave.totalFrames();
				}
			}
			
			//Handle multichannel
			if(wave.totalChannels() > 1){
				System.err.println("ZeqerWaveIO.importWAV || --DEBUG-- Multiple channels detected: " + wave.totalChannels());
				switch(options.multiChannelBehavior){
				case MULTICHAN_IMPORT_SUM:
					options.channel = -1;
					break;
				case MULTICHAN_IMPORT_LEFTONLY:
					options.channel = 0;
					break;
				case MULTICHAN_IMPORT_RIGHTONLY:
					options.channel = 1;
					break;
				case MULTICHAN_IMPORT_SPECIFY:
					break;
				}
			}
			else options.channel = 0;
			
			int[] data = null;
			if(options.channel < 0){
				//Sum
				int chcount = wave.totalChannels();
				int fcount = wave.totalFrames();
				List<int[]> chlist = new ArrayList<int[]>(chcount);
				for(int i = 0; i < chcount; i++){
					chlist.add(wave.getSamples_16Signed(i));
				}
				data = sumPCM16Channels(chlist, fcount);
			}
			else{
				data = wave.getSamples_16Signed(options.channel);
			}
			importPCMAudio(data, options, res);
			
		} catch (IOException e) {
			e.printStackTrace();
			res.error = ERROR_CODE_IO;
			return res;
		} catch (UnsupportedFileTypeException e) {
			e.printStackTrace();
			res.error = ERROR_CODE_INPARSE;
			return res;
		}
		
		return res;
	}
	
	public static SampleImportResult importAIFF(String path, SampleImportOptions options){
		//Don't forget to try to find loop if loop points not provided
		//This assumes an uncompressed AIFF. If it doesn't recognize the compression, throws error.
		SampleImportResult res = new SampleImportResult();
		
		try {
			AiffFile aiff = AiffFile.readAiff(FileBuffer.createBuffer(path, true));
			if(aiff.getCompressionId() != 0){
				res.error = ERROR_CODE_AIFF_COMPRESSION_UNKNOWN;
				return res;
			}
			
			options.srcSampleRate = (int)Math.round(aiff.getSampleRate());
			options.bitDepth = 16; //Stored as floats, so we rescale to 16 before passing to encoder.
			
			if(options.loopStart < 0){
				//Not manually set.
				if(aiff.hasSustainLoop()){
					options.loopCount = -1;
					options.loopStart = aiff.getSustainLoopStart();
					options.loopEnd = aiff.getSustainLoopEnd();
				}
				else if(aiff.hasReleaseLoop()){
					options.loopCount = -1;
					options.loopStart = aiff.getReleaseLoopStart();
					options.loopEnd = aiff.getReleaseLoopEnd();
				}
				else{
					options.loopCount = 0;
					options.loopStart = 0;
					options.loopEnd = aiff.getFrameCount();
				}
			}
			
			//Handle multichannel
			if(aiff.getChannelCount() > 1){
				switch(options.multiChannelBehavior){
				case MULTICHAN_IMPORT_SUM:
					options.channel = -1;
					break;
				case MULTICHAN_IMPORT_LEFTONLY:
					options.channel = 0;
					break;
				case MULTICHAN_IMPORT_RIGHTONLY:
					options.channel = 1;
					break;
				case MULTICHAN_IMPORT_SPECIFY:
					break;
				}
			}
			else options.channel = 0;
			
			int[] data = null;
			if(options.channel < 0){
				//Sum
				int chcount = aiff.getChannelCount();
				int fcount = aiff.getFrameCount();
				List<int[]> chlist = new ArrayList<int[]>(chcount);
				for(int i = 0; i < chcount; i++){
					chlist.add(aiff.getSamples16(i));
				}
				data = sumPCM16Channels(chlist, fcount);
			}
			else{
				data = aiff.getSamples16(options.channel);
			}
			
			importPCMAudio(data, options, res);
		} 
		catch (UnsupportedFileTypeException e) {
			e.printStackTrace();
			res.error = ERROR_CODE_INPARSE;
		} 
		catch (IOException e) {
			e.printStackTrace();
			res.error = ERROR_CODE_IO;
		}
		
		return res;
	}
	
	public static SampleImportResult importAIFC(String path, SampleImportOptions options){
		//Only imports zeldaret aifc files. So shouldn't need to do compression or anything.
		SampleImportResult res = new SampleImportResult();
		
		try {
			Z64Wave zwave = ZRetAifcFile.readAIFC(path);
			byte[] dat = zwave.getRawData();
			res.md5 = FileUtils.getMD5Sum(dat);
			res.data = FileBuffer.wrap(dat);
			
			res.info = new Z64WaveInfo();
			int uid = 0;
			for(int i = 0; i < 4; i++){
				uid <<= 8;
				uid |= Byte.toUnsignedInt(res.md5[i]);
			}
			res.info.setUID(uid);
			
			res.info.setName(options.namestem);
			res.info.setMedium(options.medium);
			res.info.flagAsActor(options.flagActor);
			res.info.flagAsEnv(options.flagEnv);
			res.info.flagAsMusic(options.flagMusic);
			res.info.flagAsSFX(options.flagSFX);
			
			zwave.getProperties(res.info);
		} 
		catch (IOException e) {
			e.printStackTrace();
			res.error = ERROR_CODE_IO;
		} 
		catch (UnsupportedFileTypeException e) {
			e.printStackTrace();
			res.error = ERROR_CODE_INPARSE;
		}
		
		return res;
	}
	
	private static void importPCMAudio(int[] data, SampleImportOptions options, SampleImportResult res){
		//If multiple channels in file, they will be split into separate samples.
		//Also will need to downsize or upsize samples to 16 bits if not 16 bits.
		//Sample rate will also be reduced by an integer factor until it is at or below max sample rate (32k usually)
		//	So very rough resampling occurs
		
		//Find downsample factor
		System.err.println("ZeqerWaveIO.importPCMAudio || --DEBUG-- Input sample rate: " + options.srcSampleRate);
		float targetSR = options.srcSampleRate;
		int downSampleFactor = 1;
		while(targetSR > options.maxSampleRate){
			downSampleFactor++;
			targetSR = (float)options.srcSampleRate / (float)downSampleFactor;
		}
		if(targetSR < options.minSampleRate){
			res.error = ERROR_CODE_DOWNSAMPLE_FAIL;
			return; //Couldn't find a good integer factor.
		}
		System.err.println("ZeqerWaveIO.importPCMAudio || --DEBUG-- Target sample rate: " + targetSR);
		
		//Strip down data
		int frames = data.length / downSampleFactor;
		if((data.length % downSampleFactor) != 0) frames++;
		PCM16Sound edata = PCM16Sound.createSound(1, frames, targetSR);
		int k = 0;
		for(int i = 0; i < data.length; i++){
			if(k++ == 0){
				//Copy this sample to encoder input.
				int sample = data[i];
				if(options.bitDepth != 16){
					if(options.bitDepth > 16){
						//Shift right
						sample >>= options.bitDepth - 16;
					}
					else{
						//Shift left
						sample <<= 16 - options.bitDepth;
					}
				}
				edata.addSample(0, (short)sample);
			}
			if(k >= downSampleFactor) k = 0;
		}
		
		//Get ADPCM table
		N64ADPCMTable table = N64ADPCMTable.getDefaultTable();
		if(!options.useDefaultADPCMTable){
			//generate new table.
			table = Z64ADPCM.buildTable(edata.createSampleStream(false), frames, options.twoBit);
		}
		if(table == null){
			res.error = ERROR_CODE_TBLGEN_FAIL;
			return;
		}
		
		//Metadata
		//Don't forget to scale loop points if sample rate changes.
		Z64WaveInfo winfo = new Z64WaveInfo(table, options.twoBit);
		winfo.setFrameCount(frames);
		winfo.setLoopCount(options.loopCount);
		if(options.loopCount != 0){
			//Also need loop state
			int loopst = options.loopStart / downSampleFactor;
			winfo.setLoopStart(loopst);
			winfo.setLoopEnd(options.loopEnd / downSampleFactor);
			
			//Need 16 samples BEFORE loop (0 fill if not enough)
			short[] lstate = new short[16];
			for(int i = 15; i >= 0; i--){
				int ii = 16 - i;
				if(loopst - ii < 0) break;
				lstate[i] = (short)edata.getSample(0, loopst - ii);
			}
			winfo.setLoopState(lstate);
		}
		else{
			winfo.setLoopStart(0);
			winfo.setLoopEnd(frames);
		}
		winfo.setName(options.namestem);
		winfo.setTuning(targetSR / 32000f); //derived from sample rate
		winfo.setMedium(options.medium);
		winfo.flagAsActor(options.flagActor);
		winfo.flagAsEnv(options.flagEnv);
		winfo.flagAsMusic(options.flagMusic);
		winfo.flagAsSFX(options.flagSFX);
		res.info = winfo;
		
		//Encode
		Z64ADPCM enc = new Z64ADPCM(table);
		try {
			res.data = enc.encode(edata.createSampleStream(false), 0, frames);
		} catch (InterruptedException e) {
			e.printStackTrace();
			res.error = ERROR_CODE_ENCODE_FAIL;
			return;
		}
		if(res.data == null){
			res.error = ERROR_CODE_ENCODE_FAIL;
			return;
		}
		
		res.md5 = FileUtils.getMD5Sum(res.data.getBytes(0, res.data.getFileSize()));
		
		int uid = 0;
		for(int i = 0; i < 4; i++){
			uid <<= 8;
			uid |= Byte.toUnsignedInt(res.md5[i]);
		}
		winfo.setUID(uid);
		
		return;
	}
	
	public static void addImportableFileFilters(JFileChooser fc){
		fc.addChoosableFileFilter(new FileFilter(){
			public boolean accept(File f) {
				if(f.isDirectory()) return true;
				String path = f.getAbsolutePath().toString();
				return path.endsWith(".wav") || path.endsWith(".wave");
			}

			public String getDescription() {
				return "RIFF Audio Wave (.wav, .wave)";
			}});
		
		fc.addChoosableFileFilter(new FileFilter(){
			public boolean accept(File f) {
				if(f.isDirectory()) return true;
				String path = f.getAbsolutePath().toString();
				return path.endsWith(".aif") || path.endsWith(".aiff") || path.endsWith(".aifc");
			}

			public String getDescription() {
				return "Audio Interchange File Format (.aif, .aiff, .aifc)";
			}});
		
	}
	
}
