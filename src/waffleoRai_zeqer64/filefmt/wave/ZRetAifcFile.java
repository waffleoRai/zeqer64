package waffleoRai_zeqer64.filefmt.wave;

import waffleoRai_Sound.nintendo.N64ADPCMTable;
import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_Sound.nintendo.Z64Wave;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_SoundSynth.AudioSampleStream;
import waffleoRai_Utils.BufferReference;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.MultiFileBuffer;

import java.io.IOException;
import java.util.List;

import waffleoRai_Files.AIFFReader;
import waffleoRai_Files.AIFFReader.AIFFChunk;
import waffleoRai_Sound.AiffFile;

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

public class ZRetAifcFile extends AiffFile{
	
	public static final String CODECID_ADP9 = "ADP9";
	public static final String CODECID_ADP5 = "ADP5";
	public static final String CODECID_HPCM = "HPCM";
	public static final String CODECID_RVRB = "RVRB";
	public static final String CODECID_NONE = "NONE";
	
	public static final int CODECID_ADP9_NUM = 0x41445035;
	public static final int CODECID_ADP5_NUM = 0x41445039;
	public static final int CODECID_HPCM_NUM = 0x4850434d;
	public static final int CODECID_RVRB_NUM = 0x52565242;
	public static final int CODECID_NONE_NUM = 0x4e4f4e45;
	
	public static final String CODECNAME_ADP9 = "Nintendo ADPCM 9-byte frame format";
	public static final String CODECNAME_ADP5 = "Nintendo ADPCM 5-byte frame format";
	public static final String CODECNAME_HPCM = "Half-frame PCM";
	public static final String CODECNAME_RVRB = "Nintendo Reverb format";
	public static final String CODECNAME_NONE = "not compressed";
	
	public static final String APPID_VADPCM = "stoc";
	public static final String APPLCH_NAME_CODETBL = "VADPCMCODES";
	public static final String APPLCH_NAME_LOOP = "VADPCMLOOPS";
	
	/*----- Instance Variables -----*/
	
	//COMM
	private int codec = -1;
	
	//APPL VADPCMCODES
	private N64ADPCMTable adpcm_tbl;
	
	//APPL VADPCMLOOPS
	private int loop_st = 0;
	private int loop_ed = 0;
	private int loop_ct = 0;
	private short[] loop_state;
	
	//SSND
	private FileBuffer sounddat;
	
	/*----- Init -----*/
	
	protected ZRetAifcFile(){
		super.channelCount = 1;
		super.bitDepth = 16;
	}
	
	/*----- Parsing -----*/
	
	protected void readSSND(BufferReference data){
		if(codec == -1){
			//Uncompressed. Read frames.
			super.readSSND(data);
			return;
		}
		
		//Compressed. Just read data.
		//Skip first 8 bytes since fields are usually just 0
		long pos = data.getBufferPosition() + 8L;
		FileBuffer buff = data.getBuffer();
		int size = getSoundDataSizeValue();
		try {
			sounddat = buff.createCopy(pos, pos + size);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void readAPPL(BufferReference data){
		data.add(4); //Skip short code.
		
		//Get long ID
		int strlen = data.nextByte();
		String longid = data.nextASCIIString(strlen);
		if((strlen % 2) == 0) data.add(1); //Since len field is one byte, an even length needs padding to 2.
		
		if(longid.equals(APPLCH_NAME_CODETBL)){
			data.add(2); //Not sure what this is.
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
		else if(longid.equals(APPLCH_NAME_LOOP)){
			data.add(4); //Not sure what this is.
			loop_st = data.nextInt();
			loop_ed = data.nextInt();
			loop_ct = data.nextInt();
			if(loop_ct != 0){
				loop_state = new short[16];
				for(int i = 0; i < 16; i++) loop_state[i] = data.nextShort();
			}
		}
	}
	
	public static Z64Wave readAIFC(String path) throws IOException, UnsupportedFileTypeException{
		if(path == null) return null;
		FileBuffer buffer = FileBuffer.createBuffer(path, true);
		return readAIFC(buffer.getReferenceAt(0L));
	}
	
	public static Z64Wave readAIFC(FileBuffer buffer) throws IOException, UnsupportedFileTypeException{
		if(buffer == null) return null;
		return readAIFC(buffer.getReferenceAt(0L));
	}
	
	public static Z64Wave readAIFC(BufferReference inputData) throws IOException, UnsupportedFileTypeException{
		if(inputData == null) return null;
		ZRetAifcFile aiff = new ZRetAifcFile();
		aiff.reader = AIFFReader.readFile(inputData, true);
		if((!aiff.reader.getAIFFFileType().equals(MAGIC_AIFF)) && (!aiff.reader.getAIFFFileType().equals(MAGIC_AIFC))){
			throw new FileBuffer.UnsupportedFileTypeException("ZRetAifcFile.readAIFC || File form type not recognized!");
		}
		
		//Look for COMM (throw if not found)
		AIFFChunk chunk = aiff.reader.getFirstTopLevelChunk(MAGIC_COMM);
		if(chunk == null) throw new FileBuffer.UnsupportedFileTypeException("ZRetAifcFile.readAIFC || COMM chunk not found!");
		aiff.readCOMM(chunk.open());
		chunk.clearCache();
		
		//Determine codec.
		switch(aiff.compressionId){
		case CODECID_ADP9_NUM:
			aiff.codec = Z64Sound.CODEC_ADPCM;
			break;
		case CODECID_ADP5_NUM:
			aiff.codec = Z64Sound.CODEC_SMALL_ADPCM;
			break;
		case CODECID_HPCM_NUM:
			aiff.codec = Z64Sound.CODEC_S8;
			break;
		case CODECID_RVRB_NUM:
			aiff.codec = Z64Sound.CODEC_REVERB;
			break;
		case CODECID_NONE_NUM:
		default:
			aiff.codec = -1;
			break;
		}
		
		//Look for SSND (throw if not found)
		chunk = aiff.reader.getFirstTopLevelChunk(MAGIC_SSND);
		if(chunk == null) throw new FileBuffer.UnsupportedFileTypeException("ZRetAifcFile.readAIFC || SSND chunk not found!");
		aiff.readSSND(chunk.open());
		chunk.clearCache();
		
		//Look for INST
		chunk = aiff.reader.getFirstTopLevelChunk(MAGIC_INST);
		if(chunk != null){
			aiff.readINST(chunk.open());
			chunk.clearCache();
		}
		
		//Loop & ADPCM table
		List<AIFFChunk> applchunks = aiff.reader.getTopLevelChunks(AiffFile.MAGIC_APPL);
		for(AIFFChunk applChunk : applchunks){
			aiff.readAPPL(applChunk.open());
			applChunk.clearCache();
		}
		
		aiff.reader.clearDataCache();
		
		//Extract Z64Wave
		Z64WaveInfo info = new Z64WaveInfo();
		info.setCodec(aiff.codec);
		info.setADPCMBook(aiff.adpcm_tbl);
		info.setLoopStart(aiff.loop_st);
		info.setLoopEnd(aiff.loop_ed);
		info.setLoopCount(aiff.loop_ct);
		info.setLoopState(aiff.loop_state);
		info.setTuning((float)(aiff.sampleRate / 32000.0));
		
		return Z64Wave.readZ64Wave(aiff.sounddat, info);
	}
	
	/*----- Getters -----*/
	
	public int getCodec(){return codec;}
	public N64ADPCMTable getADPCMTable(){return adpcm_tbl;}
	public int getLoopStart(){return loop_st;}
	public int getLoopEnd(){return loop_ed;}
	public int getLoopCount(){return loop_ct;}
	public FileBuffer getSoundData(){return sounddat;}
	
	public int getSoundDataSizeValue(){
		if(codec == Z64Sound.CODEC_ADPCM || codec == Z64Sound.CODEC_SMALL_ADPCM){
			int blocksize = 9;
			if(codec == Z64Sound.CODEC_SMALL_ADPCM) blocksize = 5;
			int len = (frameCount * blocksize) / 16;
			if(len % 2 != 0)len++;
			return len;
		}
		else if(codec == Z64Sound.CODEC_S8){
			return frameCount * channelCount;
		}
		else{
			return frameCount * channelCount * 2;
		}
	}

	/*----- Setters -----*/
	
	/*----- Serialization -----*/
	
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
	
	public FileBuffer serializeSSND(){
		if(codec == -1) return super.serializeSSND();
		
		FileBuffer ssnd = null;
		if(sounddat == null){
			ssnd = new FileBuffer(16, true);
			ssnd.printASCIIToFile(AiffFile.MAGIC_SSND);
			ssnd.addToFile(8);
			ssnd.addToFile(0L);
			return ssnd;
		}
		
		int fsize = (int)sounddat.getFileSize();
		int datSize = (fsize + 0xF) & ~0xF;
		ssnd = new FileBuffer(16 + datSize, true);
		ssnd = new FileBuffer(16, true);
		ssnd.printASCIIToFile(AiffFile.MAGIC_SSND);
		ssnd.addToFile(datSize + 8);
		ssnd.addToFile(0L);
		
		//Copy data
		sounddat.setCurrentPosition(0L);
		for(int i = 0; i < fsize; i++) ssnd.addToFile(sounddat.nextByte());
		int pad = datSize - fsize;
		for(int i = 0; i < pad; i++) ssnd.addToFile(FileBuffer.ZERO_BYTE);
		
		return ssnd;
	}
	
	protected FileBuffer serializeLOOPS(){
		int strlen = APPLCH_NAME_CODETBL.length();
		int alloc = 48;
		alloc += 12;
		alloc += strlen + 2;
		
		FileBuffer buff = new FileBuffer(alloc, true);
		buff.printASCIIToFile(AiffFile.MAGIC_APPL);
		buff.addToFile(0); //Placeholder size
		buff.printASCIIToFile(APPID_VADPCM);
		buff.addToFile((byte)strlen);
		buff.printASCIIToFile(APPLCH_NAME_LOOP);
		if((strlen % 2) == 0) buff.addToFile((byte)0);
		
		buff.addToFile(0x00010001); //Not sure what this is
		buff.addToFile(loop_st);
		buff.addToFile(loop_ed);
		buff.addToFile(loop_ct);
		if(loop_state != null){
			for(int i = 0; i < 16; i++){
				buff.addToFile(loop_state[i]);
			}
		}
		
		buff.replaceInt((int)(buff.getFileSize() - 8L), 4L);
		
		return buff;
	}
	
	protected FileBuffer serializeCODES(){
		if(adpcm_tbl == null) return null;
		
		short[] rawtbl = adpcm_tbl.getAsRaw();
		
		int strlen = APPLCH_NAME_CODETBL.length();
		int alloc = rawtbl.length << 1;
		alloc += 12;
		alloc += strlen + 2;
		alloc += 6;
		
		FileBuffer buff = new FileBuffer(alloc, true);
		buff.printASCIIToFile(AiffFile.MAGIC_APPL);
		buff.addToFile(0); //Placeholder size
		buff.printASCIIToFile(APPID_VADPCM);
		buff.addToFile((byte)strlen);
		buff.printASCIIToFile(APPLCH_NAME_CODETBL);
		if((strlen % 2) == 0) buff.addToFile((byte)0);
		
		buff.addToFile((short)1); //Not sure what this is
		buff.addToFile((short)adpcm_tbl.getOrder());
		buff.addToFile((short)adpcm_tbl.getPredictorCount());
		for(int i = 0; i < rawtbl.length; i++){
			buff.addToFile(rawtbl[i]);
		}
		
		buff.replaceInt((int)(buff.getFileSize() - 8L), 4L);
		
		return buff;
	}
	
	public FileBuffer serializeAiff(){
		MultiFileBuffer out = new MultiFileBuffer(6);
		
		//Header, COMM, INST, CODES, SSND, LOOPS
		int size = 0;
		FileBuffer comm = serializeCOMM();
		FileBuffer inst = serializeINST();
		FileBuffer ssnd = serializeSSND();
		FileBuffer codes = null;
		FileBuffer loops = null;
		
		if(codec != -1){
			codes = serializeCODES();
			size += (int)codes.getFileSize();
		}
		if(loop_ct != 0){
			loops = serializeLOOPS();
			size += (int)loops.getFileSize();
		}
		size += (int)comm.getFileSize();
		size += (int)inst.getFileSize();
		size += (int)ssnd.getFileSize();
		
		
		FileBuffer header = new FileBuffer(12, true);
		header.printASCIIToFile(AiffFile.MAGIC_FORM);
		header.addToFile(size + 4);
		if(codec != -1) header.printASCIIToFile(AiffFile.MAGIC_AIFC);
		else header.printASCIIToFile(AiffFile.MAGIC_AIFF);
		
		out.addToFile(header);
		out.addToFile(comm);
		out.addToFile(inst);
		if(codes != null) out.addToFile(codes);
		out.addToFile(ssnd);
		if(loops != null) out.addToFile(loops);
		
		return out;
	}
	
	public static void exportAIFC(Z64Wave wave, String path) throws IOException{
		if(wave == null) return;
		
		//Exports as is.
		ZRetAifcFile aifc = new ZRetAifcFile();
		aifc.adpcm_tbl = wave.getADPCMTable();
		aifc.codec = wave.getCodecEnum();
		aifc.sounddat = FileBuffer.wrap(wave.getRawData());
		aifc.loop_st = wave.getLoopFrame();
		aifc.loop_ed = wave.getLoopEndFrame();
		aifc.loop_ct = wave.getLoopCount();
		aifc.loop_state = wave.getLoopState();
		aifc.frameCount = aifc.frameCountFromDataSize((int)aifc.sounddat.getFileSize());
		aifc.sampleRate = 32000.0 * (double)wave.getTuningValue();
		
		switch(aifc.codec){
		case Z64Sound.CODEC_ADPCM:
			aifc.compressionId = CODECID_ADP9_NUM;
			aifc.compressionName = CODECNAME_ADP9;
			break;
		case Z64Sound.CODEC_SMALL_ADPCM:
			aifc.compressionId = CODECID_ADP5_NUM;
			aifc.compressionName = CODECNAME_ADP5;
			break;
		case Z64Sound.CODEC_REVERB:
			aifc.compressionId = CODECID_RVRB_NUM;
			aifc.compressionName = CODECNAME_RVRB;
			break;
		case Z64Sound.CODEC_S8:
			aifc.compressionId = CODECID_HPCM_NUM;
			aifc.compressionName = CODECNAME_HPCM;
			break;
		}
		
		FileBuffer ser = aifc.serializeAiff();
		ser.writeFile(path);
	}
	
	public static void exportAIFF(Z64Wave wave, String path) throws IOException{
		//This one decompresses to export.
		if(wave == null) return;
		
		ZRetAifcFile aifc = new ZRetAifcFile();
		aifc.frameCount = wave.totalFrames();
		aifc.sampleRate = 32000.0 * (double)wave.getTuningValue();
		
		//Note loop using the INST block sustain loop instead?
		if(wave.loops()){
			aifc.loops_start = (short)wave.getLoopFrame();
			aifc.loops_end = (short)wave.getLoopEndFrame();
			aifc.loops_playMode = AiffFile.LOOPMODE_FWD;
		}
		
		//Pull data.
		aifc.allocateFrames(aifc.frameCount);
		AudioSampleStream str = wave.createSampleStream(false);
		try{
			for(int i = 0; i < aifc.frameCount; i++){
				int[] samps = str.nextSample();
				aifc.addFrame(samps);
			}
		}
		catch(InterruptedException ex){ex.printStackTrace();}
		
		FileBuffer ser = aifc.serializeAiff();
		ser.writeFile(path);
	}
	
	
}
