package waffleoRai_zeqer64.filefmt;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import waffleoRai_Sound.nintendo.N64ADPCMTable;
import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.BufferReference;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class UltraWavFile {
	
	/*----- Constants -----*/
	
	public static final String MAGIC = "UWAV";
	public static final byte MAJOR_VERSION = 1;
	public static final byte MINOR_VERSION = 1; //TODO Remove type flags field (moved back to table)
	
	private static final int META_SIZE = 20;
	private static final int HEADER_SIZE = 16;
	
	/*----- Instance Variables -----*/
	
	//Offset, size pairs
	
	private String path;
	private int data_len = -1;
	private Map<String, int[]> chunks;
	
	/*----- Initialization -----*/
	
	private UltraWavFile(){
		chunks = new HashMap<String, int[]>();
	}
	
	/*----- Getters -----*/
	
	public String getFilePath(){return path;}
	
	public void readWaveInfo(Z64WaveInfo info) throws IOException{
		if(info == null) return;
		FileBuffer buff = FileBuffer.createBuffer(path, true);

		int[] loc = chunks.get("META");
		if(loc != null){
			readMETA(buff.getReferenceAt(loc[0]), info);
			data_len = info.getWaveSize();
		}
		
		loc = chunks.get("ADPC");
		if(loc != null){
			readADPC(buff.getReferenceAt(loc[0]), info);
		}
		
		loc = chunks.get("LOOP");
		if(loc != null){
			readLOOP(buff.getReferenceAt(loc[0]), info);
		}
	}
	
	public FileBuffer loadSoundData() throws IOException{
		int[] data_loc = chunks.get("DATA");
		if(data_loc == null) return null;
		if(data_len < 0) data_len = data_loc[1];
		
		return FileBuffer.createBuffer(path, data_loc[0], data_loc[0] + data_len, true);
	}
	
	/*----- Setters -----*/
	
	/*----- Reading -----*/
	
	private static void readMETA(BufferReference chunk_data, Z64WaveInfo info){
		if(chunk_data == null || info == null) return;
		info.setUID(chunk_data.nextInt());
		info.setMedium(chunk_data.nextByte());
		info.setCodec(chunk_data.nextByte());
		chunk_data.nextByte(); //Reserved
		int flags = Byte.toUnsignedInt(chunk_data.nextByte());
		info.flagAsSFX((flags & 0x01) != 0);
		info.flagAsActor((flags & 0x02) != 0);
		info.flagAsEnv((flags & 0x04) != 0);
		info.flagAsMusic((flags & 0x08) != 0);
		int sr = chunk_data.nextInt();
		info.setTuning((float)sr/32000f);
		info.setWaveSize(chunk_data.nextInt());
		//Don't need frame size since it's auto set from wave size
		chunk_data.nextInt();
	}
	
	private static void readADPC(BufferReference chunk_data, Z64WaveInfo info){
		if(chunk_data == null || info == null) return;
		info.setADPCMBook(N64ADPCMTable.readTable(chunk_data));
	}
	
	private static void readLOOP(BufferReference chunk_data, Z64WaveInfo info){
		if(chunk_data == null || info == null) return;
		info.setLoopStart(chunk_data.nextInt());
		info.setLoopEnd(chunk_data.nextInt());
		info.setLoopCount(chunk_data.nextInt());
		chunk_data.add(4L); //Empty 0 field
		if(info.getLoopCount() != 0){
			//Loop state
			short[] state = new short[16];
			for(int i = 0; i < 16; i++) state[i] = chunk_data.nextShort();
			info.setLoopState(state);
		}
	}
	
	public static UltraWavFile createUWAV(String filepath) throws IOException, UnsupportedFileTypeException{
		UltraWavFile uwav = new UltraWavFile();
		uwav.path = filepath;
		
		FileBuffer buff = FileBuffer.createBuffer(filepath, true);
		long pos = buff.findString(0L, 0x10L, MAGIC);
		if(pos != 0L) throw new UnsupportedFileTypeException("UltraWavFile.createUWAV || Magic number not recognized.");
		buff.setCurrentPosition(4L);
		
		int bom = Short.toUnsignedInt(buff.nextShort());
		if(bom == 0xFFFE) buff.setEndian(false);
		
		//Skip version for now
		buff.nextByte(); //Major version
		buff.nextByte(); //Minor version
		buff.nextInt(); //Total file size
		buff.nextShort(); //Header size
		int ccount = Short.toUnsignedInt(buff.nextShort());
		
		String cmag = null;
		int csz = 0;
		for(int i = 0; i < ccount; i++){
			pos = buff.getCurrentPosition();
			cmag = buff.getASCII_string(pos, 4);
			buff.skipBytes(4L);
			csz = buff.nextInt();
			uwav.chunks.put(cmag, new int[]{(int)buff.getCurrentPosition(), csz});
			buff.skipBytes(csz);
		}
		
		return uwav;
	}
	
	/*----- Writing -----*/
	
	private static FileBuffer serialize_META(Z64WaveInfo info){
		if(info == null) return null;
		FileBuffer buff = new FileBuffer(META_SIZE+8, true);
		
		buff.printASCIIToFile("META");
		buff.addToFile(META_SIZE);
		buff.addToFile(info.getUID());
		buff.addToFile((byte)info.getMedium());
		buff.addToFile((byte)info.getCodec());
		buff.addToFile(FileBuffer.ZERO_BYTE);
		
		int flags = 0;
		if(info.sfxFlag()) flags |= 0x01;
		if(info.actorFlag()) flags |= 0x02;
		if(info.envFlag()) flags |= 0x04;
		if(info.musicFlag()) flags |= 0x08;
		buff.addToFile((byte)flags);
		
		float tune = info.getTuning();
		buff.addToFile(Math.round(tune*32000f));
		buff.addToFile(info.getWaveSize());
		buff.addToFile(info.getFrameCount());
		
		return buff;
	}
	
	private static FileBuffer serialize_ADPC(Z64WaveInfo info){
		if(info == null) return null;
		int codec = info.getCodec();
		
		if(!(codec == Z64Sound.CODEC_ADPCM || codec == Z64Sound.CODEC_SMALL_ADPCM)) return null;
		
		N64ADPCMTable tbl = info.getADPCMBook();
		if(tbl == null) return null;
		
		int o = tbl.getOrder();
		int c = tbl.getPredictorCount();
		int ecount = (o*c) << 3;
		int datsize = 8 + (ecount << 1);
		FileBuffer buff = new FileBuffer(8+datsize, true);
		
		buff.printASCIIToFile("ADPC");
		buff.addToFile(datsize);
		buff.addToFile(o);
		buff.addToFile(c);
		for(int i = 0; i < c; i++){
			for(int j = 0; j < o; j++){
				for(int k = 0; k < 8; k++){
					buff.addToFile((short)tbl.getCoefficient(i, j, k));
				}
			}
		}
		
		return buff;
	}
	
	private static FileBuffer serialize_LOOP(Z64WaveInfo info){
		if(info == null) return null;
		FileBuffer buff = new FileBuffer(64, true);
		
		boolean has_state = info.getLoopCount() != 0;
		
		buff.printASCIIToFile("LOOP");
		if(has_state) buff.addToFile(48);
		else buff.addToFile(16);
		
		buff.addToFile(info.getLoopStart());
		buff.addToFile(info.getLoopEnd());
		buff.addToFile(info.getLoopCount());
		buff.addToFile(0);
		
		if(has_state){
			short[] state = info.getLoopState();
			if(state == null) state = new short[16];
			for(int i = 0; i < 16; i++) buff.addToFile(state[i]);
		}
		
		return buff;
	}
	
	public static void writeUWAV(String filepath, Z64WaveInfo info, FileBuffer sound_data) throws IOException{
		int ccount = 0;
		int total_size = HEADER_SIZE;
		FileBuffer meta = serialize_META(info);
		FileBuffer adpc = serialize_ADPC(info);
		FileBuffer loop = serialize_LOOP(info);
		
		FileBuffer header = new FileBuffer(HEADER_SIZE, true);
		header.printASCIIToFile(MAGIC);
		header.addToFile((short)0xFEFF);
		header.addToFile(MAJOR_VERSION);
		header.addToFile(MINOR_VERSION);

		if(meta != null){
			ccount++;
			total_size += (int)meta.getFileSize();
		}
		if(adpc != null){
			ccount++;
			total_size += (int)adpc.getFileSize();
		}
		if(loop != null){
			ccount++;
			total_size += (int)loop.getFileSize();
		}
		
		//Figure out data size.
		int dat_size = info.getWaveSize();
		int dat_pad = 0;
		if(sound_data != null){
			int datonly = dat_size;
			ccount++;
			dat_size += 0x3; dat_size &= ~0x3;
			total_size += dat_size + 8;
			dat_pad = dat_size - datonly;
			
			header.addToFile(total_size);
			header.addToFile((short)HEADER_SIZE);
			header.addToFile((short)ccount);
		}
		else dat_size = 0;
		
		//Write
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filepath));
		header.writeToStream(bos);
		if(meta != null) meta.writeToStream(bos);
		if(adpc != null) adpc.writeToStream(bos);
		if(loop != null) loop.writeToStream(bos);
		if(sound_data != null){
			FileBuffer buff = new FileBuffer(8, true);
			buff.printASCIIToFile("DATA");
			buff.addToFile(dat_size);
			buff.writeToStream(bos);
			sound_data.writeToStream(bos, 0L, dat_size - dat_pad);
			for(int i = 0; i < dat_pad; i++) bos.write(0);
		}
		bos.close();
	}

}
