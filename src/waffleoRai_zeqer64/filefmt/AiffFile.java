package waffleoRai_zeqer64.filefmt;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import waffleoRai_Sound.nintendo.N64ADPCMTable;
import waffleoRai_Sound.nintendo.Z64ADPCM;
import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_Sound.nintendo.Z64Wave;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.FileUtils;
import waffleoRai_Utils.MultiFileBuffer;

//80-bit float methods adapted from Z64 sound build/disasm pipeline (audio_common.py)

/*
 * FORM [4]
 * Size [4]
 * AIFF/AIFC[4]
 * 
 * COMM [4]
 * Size [4]
 * Channel Count [2]
 * Frame Count [4]
 * Sample Size (Bit depth) [2]
 * Sample Rate [10] - 80-bit float (sample frames per second)
 * Compression Type [4] - Aifc only
 * Compression Name [Var] - Aifc only (pstring)
 * 	[1] String size
 * 	[Var] String
 * 	[0-1] Padding (Align to 2 bytes)
 * 
 * (Disasm pipeline adds this, but all fields are set to 0)
 * INST [4]
 * Size [4]
 * Base Note [1]
 * Detune [1]
 * Low Note [1]
 * High Note [1]
 * Low Vel [1]
 * High Vel [1]
 * Gain [2]
 * Sustain Loop [6]
 * Release Loop [6]
 * 
 * 	Loop:
 * 		Play Mode [2]
 * 		Begin Loop [2]
 * 		End Loop [2]
 * 
 * APPL [4]
 * Size [4]
 * Appl ID [4] "stoc"
 * Appl Name [Var] (pstring)
 * 	"VADPCMCODES"
 * Data [Var]
 * 	??? [2] (Set to 1)
 * 	Order [2]
 * 	Pred Count [2]
 * 	ADPCM Table... [2*P*O*8]
 * 
 * SSND [4]
 * Size [4]
 * Offset [4] (Usually 0)
 * Block Size [4] (Usually 0)
 * Data [Var]
 * 
 * (APPL - "VADPCMLOOPS")
 * 	Appl Name
 * 	Data
 * 		??? [2] - Set to 1
 * 		??? [2] - Set to 1
 * 		Loop Start [4]
 * 		Loop End [4]
 * 		Loop Count [4]
 * 		Loop State [32] (If applicable)
 * 	
 * 
 */

public class AiffFile{
	
	public static final String MAGIC_FORM = "FORM";
	public static final String MAGIC_AIFF = "AIFF";
	public static final String MAGIC_AIFC = "AIFC";
	public static final String MAGIC_COMM = "COMM";
	public static final String MAGIC_INST = "INST";
	public static final String MAGIC_APPL = "APPL";
	public static final String MAGIC_SSND = "SSND";
	
	public static final String CODECID_ADP9 = "ADP9";
	public static final String CODECID_ADP5 = "ADP5";
	public static final String CODECID_HPCM = "HPCM";
	public static final String CODECID_RVRB = "RVRB";
	public static final String CODECID_NONE = "NONE";
	
	public static final String CODECNAME_ADP9 = "Nintendo ADPCM 9-byte frame format";
	public static final String CODECNAME_ADP5 = "Nintendo ADPCM 5-byte frame format";
	public static final String CODECNAME_HPCM = "Half-frame PCM";
	public static final String CODECNAME_RVRB = "Nintendo Reverb format";
	public static final String CODECNAME_NONE = "not compressed";
	
	public static final String APPID_VADPCM = "stoc";
	public static final String APPLCH_NAME_CODETBL = "VADPCMCODES";
	public static final String APPLCH_NAME_LOOP = "VADPCMLOOPS";
	
	//COMM
	private int channelcount = 1;
	private int framecount = 0;
	private int bitdepth = 16;
	private double samplerate = 32000.0;
	private int codec = -1; //-1 means no comp, write AIFF
	
	//APPL VADPCMCODES
	private N64ADPCMTable adpcm_tbl;
	
	//APPL VADPCMLOOPS
	private int loop_st = 0;
	private int loop_ed = 0;
	private int loop_ct = 0;
	private short[] loop_state;
	
	//SSND
	private FileBuffer sounddat;
	
	public AiffFile(FileBuffer data) throws UnsupportedFileTypeException, IOException{
		readFrom(data);
	}
	
	public AiffFile(Z64Wave zwave){
		if(zwave == null) return;
		
		//Get data first
		byte[] rawdat = zwave.getRawData();
		sounddat = FileBuffer.wrap(rawdat);
		
		//Get codec
		codec = zwave.getCodecEnum();
		
		//Calculate frame count and sample rate
		framecount = frameCountFromDataSize((int)sounddat.getFileSize());
		samplerate = 32000.0 * (double)zwave.getTuningValue();
		
		//Get ADPCM table (if applicable)
		adpcm_tbl = zwave.getADPCMTable();
		
		//Then loop info.
		loop_st = zwave.getLoopFrame();
		loop_ed = zwave.getLoopEndFrame();
		loop_ct = zwave.getLoopCount();
		if(loop_ct != 0){
			loop_state = Arrays.copyOf(zwave.getLoopState(), 16);
		}
	}
	
	public static double readFloat80(FileBuffer data){
		int exp = data.nextShort();
		long mantissa = data.nextLong();
		double sign = 1.0;
		if((exp & 0x8000) != 0) sign = -1.0;
		exp &= 0x7fff;
		if(exp == 0 && mantissa == 0) return (0.0 * sign);
		if(exp == 0 || exp == 0x7fff){
			System.err.println("AiffFile.readFloat80 || Value is infinity or denormal. Returning NaN.");
			return Double.NaN;
		}
		
		double mantissa_f = (double)mantissa / (double)(1L << 63);
		return sign * mantissa_f * Math.pow(2.0, (double)(exp - 0x3fff));
	}
	
	public static boolean writeFloat80(double value, FileBuffer data){
		long dbl_bits = Double.doubleToRawLongBits(value);
		int sign = 0;
		if((dbl_bits & (1L << 63)) != 0) sign = 1;
		if(value == 0.0){
			if(sign != 0){
				data.addToFile((byte)0x80);
				for(int i = 0; i < 9; i++) data.addToFile(FileBuffer.ZERO_BYTE);
				return true;
			}
			else{
				for(int i = 0; i < 10; i++) data.addToFile(FileBuffer.ZERO_BYTE);
				return true;
			}
		}
		dbl_bits &= ~(1L << 63);
		long exp = dbl_bits >>> 52;
		if(exp == 0 || exp == 0x7ff){
			System.err.println("AiffFile.writeFloat80 || Value is infinity or denormal. Returning without writing...");
			return false;
		}
		exp -= 1023;
		long mbits = dbl_bits & ((1L << 52) - 1);
		exp += 0x3fff;
		exp |= sign << 15;
		mbits = (1L << 63) | (mbits << (63-52));
		return true;
	}
	
	public int frameCountFromDataSize(int datasize){
		int blocksize = 1;
		int sperblock = 1;
		switch(codec){
		case Z64Sound.CODEC_ADPCM: blocksize = 9; sperblock = 16; break;
		case Z64Sound.CODEC_SMALL_ADPCM: blocksize = 5; sperblock = 16; break;
		case Z64Sound.CODEC_S8: blocksize = 1; break;
		}
		return (datasize * sperblock) / blocksize;
	}
	
	public void readFrom(FileBuffer data) throws UnsupportedFileTypeException, IOException{
		if(data == null) throw new UnsupportedFileTypeException("Provided buffer is null!");
		
		//Try header.
		data.setEndian(true);
		long mpos = data.findString(0L, 4L, MAGIC_FORM);
		if(mpos != 0L) throw new UnsupportedFileTypeException("AIFF/AIFC magic number not found (FORM)");
		String mstr = data.getASCII_string(8L, 4);
		if(mstr == null) throw new UnsupportedFileTypeException("AIFF/AIFC magic number not found!");
		boolean isaifc = false;
		if(mstr.equals(MAGIC_AIFC)) isaifc = true;
		else if(!mstr.equals(MAGIC_AIFF)) throw new UnsupportedFileTypeException("AIFF/AIFC magic number not found!");
		
		//Go thru blocks.
		data.setCurrentPosition(0xCL);
		while(data.hasRemaining()){
			mstr = data.getASCII_string(data.getCurrentPosition(), 4); data.skipBytes(4L);
			if(mstr.equals(MAGIC_COMM)){
				data.skipBytes(4L); //Skip size
				channelcount = data.nextShort();
				framecount = data.nextInt();
				bitdepth = data.nextShort();
				samplerate = readFloat80(data);
				if(isaifc){
					String cid = data.getASCII_string(data.getCurrentPosition(), 4); data.skipBytes(4L);
					//Skip codec name
					int ssz = data.nextByte();
					data.skipBytes(ssz);
					if((data.getCurrentPosition() % 2) != 0) data.skipBytes(1L);
					if(cid.equals(CODECID_ADP5)) codec = Z64Sound.CODEC_SMALL_ADPCM;
					else if(cid.equals(CODECID_ADP9)) codec = Z64Sound.CODEC_ADPCM;
					else if(cid.equals(CODECID_HPCM)) codec = Z64Sound.CODEC_S8;
					else if(cid.equals(CODECID_RVRB)) codec = Z64Sound.CODEC_REVERB;
					else if(cid.equals(CODECID_NONE)) codec = Z64Sound.CODEC_S16;
				}
			}
			else if(mstr.equals(MAGIC_SSND)){
				int csz = data.nextInt();
				int dsz = getSoundDataSizeValue();
				long stpos = data.getCurrentPosition() + 8L;
				sounddat = data.createCopy(stpos, stpos+dsz);
				data.skipBytes(csz);
			}
			else if(mstr.equals(MAGIC_APPL)){
				int csz = data.nextInt();
				long jumptarg = csz + data.getCurrentPosition();
				data.skipBytes(4); //Skip 4 byte code.
				int astr_len = data.nextByte();
				String astr = data.getASCII_string(data.getCurrentPosition(), astr_len);
				data.skipBytes(astr_len);
				if((astr_len % 2) != 0) data.skipBytes(1L);
				if(astr.equals(APPLCH_NAME_CODETBL)){
					data.nextShort(); //Dunno what this is.
					int order = data.nextShort();
					int pcount = data.nextShort();
					adpcm_tbl = new N64ADPCMTable(order, pcount);
					for(int p = 0; p < pcount; p++){
						for(int o = 0; o < order; o++){
							for(int i = 0; i < 8; i++){
								adpcm_tbl.setCoefficient(p, o, i, data.nextShort());
							}
						}
					}
				}
				else if(astr.equals(APPLCH_NAME_LOOP)){
					data.nextInt(); //Not sure what this is.
					loop_st = data.nextInt();
					loop_ed = data.nextInt();
					loop_ct = data.nextInt();
					if(loop_ct != 0){
						loop_state = new short[16];
						for(int i = 0; i < 16; i++) loop_state[i] = data.nextShort();
					}
				}
				data.setCurrentPosition(jumptarg);
			}
		}
	}
	
	public boolean writeTo(FileBuffer buff){
		if(buff == null) return false;
		if(sounddat == null){
			System.err.println("AiffFile.writeTo || No sound data!");
			return false;
		}
		
		boolean is_aifc = (codec >= 0);
		FileBuffer hdr = new FileBuffer(12, true);
		hdr.printASCIIToFile(MAGIC_FORM); 
		hdr.addToFile(0); //Hold off until calculated.
		if(!is_aifc) hdr.printASCIIToFile(MAGIC_AIFF);
		else hdr.printASCIIToFile(MAGIC_AIFC);
		buff.addToFile(hdr);
		
		//COMM
		String codec_id = null;
		String codec_desc = null;
		if(is_aifc){
			switch(codec){
			case Z64Sound.CODEC_ADPCM:
				codec_id = CODECID_ADP9;
				codec_desc = CODECNAME_ADP9;
				break;
			case Z64Sound.CODEC_SMALL_ADPCM:
				codec_id = CODECID_ADP5;
				codec_desc = CODECNAME_ADP5;
				break;
			case Z64Sound.CODEC_S8:
				codec_id = CODECID_HPCM;
				codec_desc = CODECNAME_HPCM;
				break;
			case Z64Sound.CODEC_REVERB:
				codec_id = CODECID_RVRB;
				codec_desc = CODECNAME_RVRB;
				break;
			case Z64Sound.CODEC_S16:
				codec_id = CODECID_NONE;
				codec_desc = CODECNAME_NONE;
				break;
			default:
				codec_desc = "";
				codec_id = "_UNK";
				break;
			}
		}
		int comm_size = is_aifc?22:18;
		if(is_aifc){
			comm_size += 1 + codec_desc.length();
			if((comm_size % 2) != 0) comm_size++;
		}
		FileBuffer comm = new FileBuffer(comm_size+8, true);
		comm.printASCIIToFile(MAGIC_COMM);
		comm.addToFile(comm_size);
		comm.addToFile((short)channelcount);
		comm.addToFile(framecount);
		comm.addToFile((short)bitdepth);
		if(!AiffFile.writeFloat80(samplerate, comm)){
			for(int i = 0; i < 10; i++) comm.addToFile(FileBuffer.ZERO_BYTE);
		}
		if(is_aifc){
			int desclen = codec_desc.length();
			comm.printASCIIToFile(codec_id);
			comm.addToFile((byte)desclen);
			comm.printASCIIToFile(codec_desc);
			if(((desclen+1) % 2) != 0) comm.addToFile(FileBuffer.ZERO_BYTE);
		}
		buff.addToFile(comm);
		
		//INST
		final int INST_SIZE = 20;
		FileBuffer inst = new FileBuffer(INST_SIZE+8, true);
		inst.printASCIIToFile(MAGIC_INST);
		inst.addToFile(INST_SIZE);
		for(int i = 0; i < INST_SIZE; i++) inst.addToFile(FileBuffer.ZERO_BYTE);
		
		//ADPCM TABLE
		if(adpcm_tbl != null){
			int order = adpcm_tbl.getOrder();
			int pcount = adpcm_tbl.getPredictorCount();
			int ecount = order*pcount*8;
			int size = (ecount << 1) + 6;
			int namelen = APPLCH_NAME_CODETBL.length();
			boolean pad = ((namelen+1) % 2) != 0;
			size += 4;
			size += namelen + 1;
			if(pad) size++;
			FileBuffer codc = new FileBuffer(size+8, true);
			codc.printASCIIToFile(MAGIC_APPL);
			codc.addToFile(size);
			codc.printASCIIToFile(APPID_VADPCM);
			codc.addToFile((byte)namelen);
			codc.printASCIIToFile(APPLCH_NAME_CODETBL);
			if(pad) codc.addToFile(FileBuffer.ZERO_BYTE);
			codc.addToFile((short)1);
			codc.addToFile((short)order);
			codc.addToFile((short)pcount);
			for(int p = 0; p < pcount; p++){
				for(int o = 0; o < order; o++){
					for(int i = 0; i < 8; i++){
						codc.addToFile((short)adpcm_tbl.getCoefficient(p, o, i));
					}
				}
			}
			
			buff.addToFile(codc);
		}
		
		//SSND
		int datlen = getSoundDataSizeValue();
		FileBuffer ssnd = new FileBuffer(datlen + 32, true);
		ssnd.printASCIIToFile(MAGIC_SSND);
		ssnd.addToFile(0); //Placeholder
		ssnd.addToFile(0); ssnd.addToFile(0); //Unused fields.
		ssnd.addToFile(sounddat.createReadOnlyCopy(0, datlen));
		while(ssnd.getFileSize() % 2 != 0) ssnd.addToFile(FileBuffer.ZERO_BYTE);
		ssnd.replaceInt((int)(ssnd.getFileSize()-8L), 4L);
		buff.addToFile(ssnd);
		
		//LOOP
		int chnamelen = APPLCH_NAME_LOOP.length();
		FileBuffer loop = new FileBuffer(12 + 2 + chnamelen + 48,true);
		boolean pad = ((chnamelen+1) % 2) != 0;
		loop.printASCIIToFile(MAGIC_APPL);
		loop.addToFile(0);
		loop.printASCIIToFile(APPID_VADPCM);
		loop.addToFile((byte)chnamelen);
		loop.printASCIIToFile(APPLCH_NAME_LOOP);
		if(pad) loop.addToFile(FileBuffer.ZERO_BYTE);
		loop.addToFile(0x00010001); //No idea what this is.
		loop.addToFile(loop_st);
		loop.addToFile(loop_ed);
		loop.addToFile(loop_ct);
		if(loop_ct != 0){
			if(loop_state != null){
				for(int i = 0; i < 16; i++) loop.addToFile(loop_state[i]);
			}
			else{
				for(int i = 0; i < 8; i++) loop.addToFile(0);
			}
		}
		loop.replaceInt((int)(loop.getFileSize()-8L), 4L);
		buff.addToFile(loop);
		return true;
	}
	
	public boolean writeTo(OutputStream stream) throws IOException{
		FileBuffer buff = new MultiFileBuffer(8);
		if(!writeTo(buff))return false;
		buff.writeToStream(stream);
		return true;
	}
	
	public boolean writeTo(String output_path) throws IOException{
		FileBuffer buff = new MultiFileBuffer(8);
		if(!writeTo(buff))return false;
		buff.writeFile(output_path);
		return true;
	}
	
	public int getSoundDataSizeValue(){
		if(codec == Z64Sound.CODEC_ADPCM || codec == Z64Sound.CODEC_SMALL_ADPCM){
			int blocksize = 9;
			if(codec == Z64Sound.CODEC_SMALL_ADPCM) blocksize = 5;
			int len = (framecount * blocksize) / 16;
			if(len % 2 != 0)len++;
			return len;
		}
		else if(codec == Z64Sound.CODEC_S8){
			return framecount*channelcount;
		}
		else{
			return framecount*channelcount*2;
		}
	}

	public static Z64Wave readAIFC(String path) throws IOException, UnsupportedFileTypeException{
		FileBuffer dat = FileBuffer.createBuffer(path, true);
		AiffFile aiff = new AiffFile(dat);
		
		//Now wrap in a Z64Wave.
		Z64WaveInfo winfo = new Z64WaveInfo(aiff.adpcm_tbl, aiff.codec == Z64Sound.CODEC_SMALL_ADPCM);
		winfo.setTuning((float)(aiff.samplerate/32000.0));
		winfo.setLoopStart(aiff.loop_st);
		winfo.setLoopEnd(aiff.loop_ed);
		winfo.setLoopCount(aiff.loop_ct);
		winfo.setLoopState(aiff.loop_state);
		winfo.setWaveSize((int)(aiff.sounddat.getFileSize()));
		
		//Gen name and UID
		byte[] md5 = FileUtils.getMD5Sum(aiff.sounddat.getBytes());
		int uid = 0;
		for(int i = 0; i < 4; i++){
			uid <<= 8;
			uid |= Byte.toUnsignedInt(md5[i]);
		}
		String name = String.format("aifc_%08x", uid);
		winfo.setName(name);
		winfo.setUID(uid);
		
		return Z64Wave.readZ64Wave(aiff.sounddat, winfo);
	}
	
	public static Z64Wave readAIFF(String path, int codec) throws IOException, UnsupportedFileTypeException{
		return readAIFF(path, codec, -1, -1, 0);
	}
	
	public static Z64Wave readAIFF(String path, int codec, int loopst, int looped, int loopct) throws IOException, UnsupportedFileTypeException{
		if(path == null) return null;
		FileBuffer aiff_dat = FileBuffer.createBuffer(path, true);
		AiffFile aiff = new AiffFile(aiff_dat);
		if(aiff.bitdepth != 16){
			throw new UnsupportedFileTypeException("Only supported bit depth is 16-bit signed!");
		}
		
		Z64WaveInfo winfo = new Z64WaveInfo();
		winfo.setTuning((float)(aiff.samplerate/32000.0));
		winfo.setLoopStart(loopst);
		winfo.setLoopEnd(looped);
		winfo.setLoopCount(loopct);
		winfo.setCodec(codec);
		
		FileBuffer sound = null;
		N64ADPCMTable atbl = null;
		Z64ADPCM encoder = null;
		AiffPCM aiffsnd = new AiffPCM(aiff);
		
		try{
			switch(codec){
			case Z64Sound.CODEC_ADPCM:
				atbl = Z64ADPCM.buildTable(aiffsnd.createSampleStream(false), 0, aiff.framecount, false);
				encoder = new Z64ADPCM(atbl);
				encoder.setFourBit();
				sound = encoder.encode(aiffsnd.createSampleStream(false), 0, aiff.framecount);
				break;
			case Z64Sound.CODEC_SMALL_ADPCM:
				atbl = N64ADPCMTable.getDefaultTable();
				encoder = new Z64ADPCM(atbl);
				encoder.setTwoBit();
				sound = encoder.encode(aiffsnd.createSampleStream(false), 0, aiff.framecount);
				break;
			case Z64Sound.CODEC_S8:
				sound = new FileBuffer(aiff.framecount, true);
				aiff.sounddat.setCurrentPosition(0L);
				for(int i = 0; i < aiff.framecount; i++){
					sound.addToFile((byte)((int)aiff.sounddat.nextShort() >> 8));
				}
				break;
			case Z64Sound.CODEC_S16:
				sound = aiff.sounddat;
				break;
			default:
				throw new UnsupportedFileTypeException("Codec " + codec + " not supported!");
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
			throw new UnsupportedFileTypeException("Unexpected exception caught! See stack trace.");
		}
		
		//Finish adding winfo
		if(atbl != null) winfo.setADPCMBook(atbl);
		byte[] md5 = FileUtils.getMD5Sum(sound.getBytes());
		int uid = 0;
		for(int i = 0; i < 4; i++){
			uid <<= 8;
			uid |= Byte.toUnsignedInt(md5[i]);
		}
		String name = String.format("aiff_%08x", uid);
		winfo.setName(name);
		winfo.setUID(uid);
		winfo.setWaveSize((int)(sound.getFileSize()));
		
		return Z64Wave.readZ64Wave(sound, winfo);
	}
	
	public static void exportAIFC(Z64Wave wave, String path) throws IOException{
		if(wave == null || path == null) return;
		AiffFile aiff = new AiffFile(wave);
		aiff.writeTo(path);
	}
	
	public static void exportAIFF(Z64Wave wave, String path) throws IOException{
		if(wave == null || path == null) return;
		AiffFile aiff = new AiffFile(wave);
		aiff.codec = -1;
		int[] samps_16 = wave.getSamples_16Signed(0);
		aiff.sounddat = new FileBuffer(samps_16.length*2);
		for(int i = 0; i < samps_16.length; i++){
			aiff.sounddat.addToFile((short)samps_16[i]);
		}
		aiff.writeTo(path);
	}
	
	//Getters
	public int getChannelCount(){return channelcount;}
	public int getBitDepth(){return bitdepth;}
	public int getFrameCount(){return framecount;}
	public double getSampleRate(){return samplerate;}
	public int getLoopStart(){return loop_st;}
	public int getLoopEnd(){return loop_ed;}
	public FileBuffer getSoundData(){return sounddat;}
	
}
