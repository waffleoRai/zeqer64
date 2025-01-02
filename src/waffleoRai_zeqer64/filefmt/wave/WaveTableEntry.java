package waffleoRai_zeqer64.filefmt.wave;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import waffleoRai_Sound.nintendo.N64ADPCMTable;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.BinFieldSize;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.SerializedString;

public class WaveTableEntry {
	
	private static final int BASE_SIZE = 4+2+1+1+16; //V2+
	
	protected byte[] md5;
	protected byte unity_key = 60;
	protected byte fine_tune = 0;
	private int flags = 0;
	protected Set<String> tags;
	
	protected Z64WaveInfo wave_info;
	
	private int read_amt = 0; //For parsing.
	
	protected WaveTableEntry(){
		wave_info = new Z64WaveInfo();
		tags = new HashSet<String>();
	}
	
	public static WaveTableEntry read(FileBuffer in, long offset, int version){
		//System.err.println("DEBUG: Starting entry read at 0x" + Long.toHexString(offset));
		WaveTableEntry entry = new WaveTableEntry();
		in.setCurrentPosition(offset);
		long stpos = offset;
		
		entry.wave_info.setUID(in.nextInt());
		if(version < 2){
			int flags = in.nextInt();
			entry.md5 = new byte[16];
			for(int i = 0; i < 16; i++) entry.md5[i] = in.nextByte();
			int samplerate = in.nextInt();
			entry.unity_key = in.nextByte();
			entry.fine_tune = in.nextByte();
			entry.wave_info.setLoopCount(in.nextByte());
			in.nextByte(); //Reserved
			entry.wave_info.setLoopStart(in.nextInt());
			entry.wave_info.setLoopEnd(in.nextInt());
			entry.wave_info.setTuning((float)samplerate/32000.0f);
			
			//If ADPCM
			if((flags | ZeqerWaveTable.FLAG_ADPCM) != 0){
				int pred_order = in.nextInt();
				int pred_count = in.nextInt();
				int n = (pred_count * pred_order) << 3;
				short[] pred_table = new short[n];
				for(int i = 0; i < n; i++) pred_table[i] = in.nextShort();
				entry.wave_info.setADPCMBook(N64ADPCMTable.fromRaw(pred_order, pred_count, pred_table));
			}
		}
		else{
			entry.flags = Short.toUnsignedInt(in.nextShort());
			entry.unity_key = in.nextByte();
			entry.fine_tune = in.nextByte();
			entry.md5 = new byte[16];
			for(int i = 0; i < 16; i++) entry.md5[i] = in.nextByte();
			entry.wave_info.setWaveSize(-1); //Unloaded marker
			
			//Update flags in wave info
			entry.wave_info.flagAsActor(entry.isFlagSet(ZeqerWaveTable.FLAG_ISACTOR));
			entry.wave_info.flagAsEnv(entry.isFlagSet(ZeqerWaveTable.FLAG_ISENV));
			entry.wave_info.flagAsSFX(entry.isFlagSet(ZeqerWaveTable.FLAG_ISSFX));
			entry.wave_info.flagAsMusic(entry.isFlagSet(ZeqerWaveTable.FLAG_ISMUSIC));
		}
		
		long pos = in.getCurrentPosition();
		SerializedString ss = in.readVariableLengthString("UTF8", pos, BinFieldSize.WORD, 2);
		entry.wave_info.setName(ss.getString());
		in.skipBytes(ss.getSizeOnDisk());
		
		if(version >= 3){
			pos = in.getCurrentPosition();
			ss = in.readVariableLengthString("UTF8", pos, BinFieldSize.WORD, 2);
			String rawtags = ss.getString();
			if(rawtags != null && !rawtags.isEmpty()){
				entry.tags = new HashSet<String>();
				String[] spl = rawtags.split(";");
				for(String s : spl){
					entry.tags.add(s);
				}
			}
			in.skipBytes(ss.getSizeOnDisk());
		}
		entry.read_amt = (int)(in.getCurrentPosition() - stpos);
		
		return entry;
	}
	
	public int getUID(){return wave_info.getUID();}
	public String getName(){return wave_info.getName();}
	
	public int getReadBytes(){return read_amt;}
	
	public int getSampleRate(){
		return Math.round(32000f * wave_info.getTuning());
	}
	
	public byte[] getMD5(){return md5;}
	
	private void updateFlags(){
		clearFlags(ZeqerWaveTable.FLAG_ISACTOR | ZeqerWaveTable.FLAG_ISENV | ZeqerWaveTable.FLAG_ISSFX | ZeqerWaveTable.FLAG_ISMUSIC);
		if(wave_info.actorFlag()) flags |= ZeqerWaveTable.FLAG_ISACTOR;
		if(wave_info.envFlag()) flags |= ZeqerWaveTable.FLAG_ISENV;
		if(wave_info.sfxFlag()) flags |= ZeqerWaveTable.FLAG_ISSFX;
		if(wave_info.musicFlag()) flags |= ZeqerWaveTable.FLAG_ISMUSIC;
	}
	
	public boolean isFlagSet(int flagMask){
		return (flags & flagMask) != 0; 
	}
	
	public void addTag(String tag){tags.add(tag);}
	
	public Set<String> getTagSet(){
		return tags;
	}
	
	public void setName(String s){wave_info.setName(s);}
	
	public void setMD5(byte[] new_md5){
		if(new_md5 == null || new_md5.length != 16) return;
		md5 = new_md5;
	}
	
	public void setSampleRate(int sampleRate){
		float sr = (float)sampleRate;
		wave_info.setTuning(sr/32000f);
	}
	
	public void setFlags(int flagMask){
		flags |= flagMask;
	}
	
	public void clearFlags(int flagMask){
		flags &= ~flagMask;
	}
	
	public Z64WaveInfo getWaveInfo(){
		return wave_info;
	}
	
	public String getDataFileName(){
		//return String.format("%08x.vadpcm", UID);
		return String.format("%08x.buwav", wave_info.getUID());
	}
	
	public int getSerializedSize(){
		int size = BASE_SIZE;
		
		//Add name size
		size += 2;
		String wname = wave_info.getName();
		if(wname != null){
			int nlen = wname.length();
			size += nlen;
			if(size % 2 != 0) size++;	
		}
		
		//Tags size
		size += 2;
		int tlen = 0;
		if(tags != null){
			int tag_count = tags.size() - 1;
			if(tag_count < 0) tag_count = 0;
			for(String tag:tags){
				tlen += tag.length();
			}
			tlen += tag_count;
		}
		size += tlen;
		if(size % 2 != 0) size++;
		
		return size;
	}
	
	public int serializeTo(FileBuffer out){
		long initsize = out.getFileSize();
		
		out.addToFile(wave_info.getUID());
		//out.addToFile((short)0);
		updateFlags();
		out.addToFile((short)flags);
		out.addToFile(unity_key);
		out.addToFile(fine_tune);
		
		if(md5 != null){
			for(int i = 0; i < 16; i++) out.addToFile(md5[i]);
		}
		else{
			for(int i = 0; i < 4; i++) out.addToFile(0);
		}
		out.addVariableLengthString("UTF8", wave_info.getName(), BinFieldSize.WORD, 2);
		
		if(tags != null && !tags.isEmpty()){
			String fulltags = "";
			boolean first = true;
			for(String tag : tags){
				if(!first) fulltags += ";";
				fulltags += tag;
				first = false;
			}
			out.addVariableLengthString("UTF8", fulltags, BinFieldSize.WORD, 2);
		}
		else out.addToFile((short)0);
		
		return (int)(out.getFileSize() - initsize);
	}
	
	public int serializeTo(OutputStream out) throws IOException{
		FileBuffer buffer = new FileBuffer(getSerializedSize()+4, true);
		int count = serializeTo(buffer);
		buffer.writeToStream(out);
		return count;
	}
	

}
