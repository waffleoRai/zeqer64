package waffleoRai_zeqer64.filefmt.bank;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_Utils.BinFieldSize;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.SerializedString;
import waffleoRai_zeqer64.filefmt.RecompFiles;

public class BankTableEntry {
	
	/*----- Constants -----*/
	
	private static final int BASE_SIZE = 4+4+16+4+4+8+8+6;
	
	/*----- Instance Variables -----*/
	
	protected int uid;
	protected int flags = 0;
	protected byte[] md5;
	
	protected int[] icounts;
	protected byte medium = Z64Sound.MEDIUM_CART;
	protected byte cache = Z64Sound.CACHE_TEMPORARY;
	protected byte warc_idx;
	protected byte warc2_idx;
	
	protected ZonedDateTime time_created;
	protected ZonedDateTime time_modified;
	
	protected String name;
	protected String enm_str;
	protected Set<String> tags;
	
	/*----- Init -----*/
	
	protected BankTableEntry(){icounts = new int[3]; tags = new HashSet<String>();}
	
	/*----- Read -----*/
	
	public static BankTableEntry read(FileBuffer in, int version){
		BankTableEntry entry = new BankTableEntry();
		//in.setCurrentPosition(offset);
		
		entry.uid = in.nextInt();
		entry.flags = in.nextInt();
		entry.md5 = new byte[16];
		for(int i = 0; i < 16; i++) entry.md5[i] = in.nextByte();
		
		entry.icounts[0] = Byte.toUnsignedInt(in.nextByte());
		entry.icounts[1] = Byte.toUnsignedInt(in.nextByte());
		entry.icounts[2] = Short.toUnsignedInt(in.nextShort());
		
		//entry.unk_word = in.nextInt();
		entry.medium = in.nextByte();
		entry.cache = in.nextByte();
		entry.warc_idx = in.nextByte();
		entry.warc2_idx = in.nextByte();
		
		long rawtime = in.nextLong();
		entry.time_created = ZonedDateTime.ofInstant(Instant.ofEpochSecond(rawtime), ZoneId.systemDefault());
		rawtime = in.nextLong();
		entry.time_modified = ZonedDateTime.ofInstant(Instant.ofEpochSecond(rawtime), ZoneId.systemDefault());
		
		long cpos = in.getCurrentPosition();
		SerializedString ss = in.readVariableLengthString("UTF8", cpos, BinFieldSize.WORD, 2);
		entry.name = ss.getString();
		in.skipBytes(ss.getSizeOnDisk());
		
		if(version >= 2){
			cpos = in.getCurrentPosition();
			ss = in.readVariableLengthString("UTF8", cpos, BinFieldSize.WORD, 2);
			String tagstr = ss.getString();
			if(tagstr != null){
				String[] taglist = tagstr.split(";");
				for(String s : taglist) entry.tags.add(s);
			}
			in.skipBytes(ss.getSizeOnDisk());
			if(version >= 3){
				cpos = in.getCurrentPosition();
				ss = in.readVariableLengthString("UTF8", cpos, BinFieldSize.WORD, 2);
				entry.enm_str = ss.getString();
				in.skipBytes(ss.getSizeOnDisk());
			}
			else{
				entry.enm_str = RecompFiles.genEnumStrFromName(entry.name);
			}
		}
		
		return entry;
	}
	
	/*----- Getters -----*/
	
	public String getName(){return name;}
	public String getEnumString(){return enm_str;}
	public int getUID(){return uid;}
	public int getWarcIndex(){return Byte.toUnsignedInt(warc_idx);}
	public int getSecondaryWarcIndex(){return Byte.toUnsignedInt(warc2_idx);}
	public byte getMedium(){return medium;}
	public byte getCachePolicy(){return cache;}
	public int getInstrumentCount(){return icounts[0];}
	public int getPercussionCount(){return icounts[1];}
	public int getSFXCount(){return icounts[2];}
	public ZonedDateTime getDateModified(){return time_modified;}
	public boolean hasTag(String tag){return tags.contains(tag);}
	public int getFlags(){return flags;}
	
	public boolean flagsSet(int mask){
		return (flags & mask) != 0;
	}
	
	public Collection<String> getTags(){
		List<String> list = new ArrayList<String>(tags.size());
		list.addAll(tags);
		return list;
	}
	
	/*----- Setters -----*/
	
	public void setName(String s){
		name = s;
		time_modified = ZonedDateTime.now();
	}
	
	public void setEnumString(String s){enm_str = s;}
	
	public void setInstCounts(int i0, int i1, int i2){
		icounts[0] = i0;
		icounts[1] = i1;
		icounts[2] = i2;
		time_modified = ZonedDateTime.now();
	}
	
	public void setWarcIndex(int idx){
		this.warc_idx = (byte)idx;
		time_modified = ZonedDateTime.now();
	}
	
	public void setWarc2Index(int val){
		warc2_idx = (byte)val;
	}
	
	public void setMedium(int val){
		medium = (byte)val;
	}
	
	public void setCachePolicy(int val){
		cache = (byte)val;
	}
	
	public void setFlags(int mask){
		flags |= mask;
	}
	
	public void clearFlags(int mask){
		flags &= ~mask;
	}
	
	public void addTag(String tag){tags.add(tag);}
	public void clearTags(){tags.clear();}
	
	public void updateModifiedTimestamp(){
		time_modified = ZonedDateTime.now();
	}
	
	/*----- File Paths -----*/
	
	public String getDataPathStem(){
		return String.format("%08x", uid);
	}
	
	public String getDataFileName(){
		return getDataPathStem() + ".bubnk";
	}
	
	public String getWSDFileName(){
		return getDataPathStem() + ".buwsd";
	}
	
	/*----- Write -----*/
	
	public int getSerializedSize(){
		int size = BASE_SIZE;
		if(name != null){
			size += name.length();
			if(size % 2 != 0) size++;
		}
		if(enm_str != null){
			size += enm_str.length();
			if(size % 2 != 0) size++;
		}
		//Tags
		for(String tag:tags){
			size += tag.length();
		}
		size += tags.size()-1;
		if(size % 2 != 0) size++;
		
		return size;
	}
		
	public void setMD5(byte[] new_md5){
		if(new_md5 == null) return;
		if(new_md5.length != 16) return;
		md5 = new_md5;
		time_modified = ZonedDateTime.now();
	}
	
	public int serializeTo(FileBuffer out){
		if(out == null) return 0;
		int initsize = (int)out.getFileSize();
		
		out.addToFile(uid);
		out.addToFile(flags);
		
		if(md5 == null){
			for(int i = 0; i < 16; i++) out.addToFile(FileBuffer.ZERO_BYTE);
		}
		else{
			for(int i = 0; i < 16; i++){
				if(i >= md5.length) out.addToFile(FileBuffer.ZERO_BYTE);
				else out.addToFile(md5[i]);
			}
		}
		
		out.addToFile((byte)icounts[0]);
		out.addToFile((byte)icounts[1]);
		out.addToFile((short)icounts[2]);
		//out.addToFile(unk_word);
		out.addToFile(medium);
		out.addToFile(cache);
		out.addToFile(warc_idx);
		out.addToFile(warc2_idx);
		
		if(time_created == null) time_created = ZonedDateTime.now();
		out.addToFile(time_created.toEpochSecond());
		
		if(time_modified == null) time_modified = ZonedDateTime.now();
		out.addToFile(time_modified.toEpochSecond());
		
		out.addVariableLengthString("UTF8", name, BinFieldSize.WORD, 2);
		
		StringBuilder sb = new StringBuilder(1024);
		boolean first = true;
		for(String tag:tags){
			if(!first)sb.append(";");
			sb.append(tag);
			first = false;
		}
		out.addVariableLengthString("UTF8", sb.toString(), BinFieldSize.WORD, 2);
		out.addVariableLengthString("UTF8", enm_str, BinFieldSize.WORD, 2);
		
		return (int)out.getFileSize() - initsize;
	}
	
	public int serializeTo(OutputStream out) throws IOException{
		if(out == null) return 0;
		FileBuffer buffer = new FileBuffer(getSerializedSize()+4, true);
		int sz = serializeTo(buffer);
		buffer.writeToStream(out);
		return sz;
	}
	
	/*----- Misc. -----*/
	
	public void copyTo(BankTableEntry copy) {
		if(copy == null) return;
		
		copy.flags = this.flags;
		if(md5 != null) {
			copy.md5 = Arrays.copyOf(this.md5, 16);
		}
		copy.icounts = Arrays.copyOf(this.icounts, 3);
		copy.medium = this.medium;
		copy.cache = this.cache;
		copy.warc_idx = this.warc_idx;
		copy.warc2_idx = this.warc2_idx;
		copy.name = this.name;
		copy.enm_str = this.enm_str;
		copy.tags.clear();
		copy.tags.addAll(this.tags);
		
		copy.time_modified = ZonedDateTime.now();
	}
	
	public BankTableEntry copy() {
		BankTableEntry copy = new BankTableEntry();
		copy.uid = this.uid;
		copy.flags = this.flags;
		if(md5 != null) {
			copy.md5 = Arrays.copyOf(this.md5, 16);
		}
		copy.icounts = Arrays.copyOf(this.icounts, 3);
		copy.medium = this.medium;
		copy.cache = this.cache;
		copy.warc_idx = this.warc_idx;
		copy.warc2_idx = this.warc2_idx;
		copy.name = this.name;
		copy.enm_str = this.enm_str;
		copy.tags.addAll(this.tags);
		
		copy.time_created = ZonedDateTime.now();
		copy.time_modified = ZonedDateTime.now();
		
		return copy;
	}

}
