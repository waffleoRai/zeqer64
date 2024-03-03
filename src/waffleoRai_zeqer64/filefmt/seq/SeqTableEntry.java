package waffleoRai_zeqer64.filefmt.seq;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import waffleoRai_Utils.BinFieldSize;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.SerializedString;
import waffleoRai_zeqer64.filefmt.RecompFiles;
import waffleoRai_zeqer64.filefmt.seq.SeqTableEntry;

public class SeqTableEntry {
	
	private static final int BASE_SIZE = 4+4+16+8+8+6;
	
	/*----- Instance Variables -----*/
	
	protected int uid;
	protected int flags = 0;
	protected byte medium;
	protected byte cache;
	protected byte[] md5;
	
	protected ZonedDateTime time_created;
	protected ZonedDateTime time_modified;
	
	protected int bank_uid = 0;
	protected List<Integer> banks; //Only used if multi bank
	
	protected String name;
	protected String enm_str;
	protected Set<String> tags;
	
	/*----- Init -----*/
	
	protected SeqTableEntry(){tags = new HashSet<String>();}
	
	public static SeqTableEntry read(FileBuffer in, int version){
		SeqTableEntry entry = new SeqTableEntry();
		//in.setCurrentPosition(offset);
		
		entry.uid = in.nextInt();
		entry.flags = Short.toUnsignedInt(in.nextShort());
		entry.medium = in.nextByte();
		entry.cache = in.nextByte();
		entry.md5 = new byte[16];
		for(int i = 0; i < 16; i++) entry.md5[i] = in.nextByte();
		
		long rawtime = in.nextLong();
		entry.time_created = ZonedDateTime.ofInstant(Instant.ofEpochSecond(rawtime), ZoneId.systemDefault());
		rawtime = in.nextLong();
		entry.time_modified = ZonedDateTime.ofInstant(Instant.ofEpochSecond(rawtime), ZoneId.systemDefault());
		
		if((entry.flags & ZeqerSeqTable.FLAG_MULTIBANK) != 0){
			entry.bank_uid = 0;
			int bank_count = Short.toUnsignedInt(in.nextShort());
			entry.banks = new ArrayList<Integer>(bank_count+1);
			for(int i = 0; i < bank_count; i++){
				entry.banks.add(in.nextInt());
			}
		}
		else{
			entry.bank_uid = in.nextInt();
		}
		
		long cpos = in.getCurrentPosition();
		SerializedString ss = in.readVariableLengthString("UTF8", cpos, BinFieldSize.WORD, 2);
		entry.name = ss.getString();
		in.skipBytes(ss.getSizeOnDisk());
		
		if(version >= 2){
			cpos = in.getCurrentPosition();
			ss = in.readVariableLengthString("UTF8", cpos, BinFieldSize.WORD, 2);
			String rawtags = ss.getString();
			if(rawtags != null && !rawtags.isEmpty()){
				String[] taglist = rawtags.split(";");
				for(String tag:taglist) entry.tags.add(tag);
			}
			in.skipBytes(ss.getSizeOnDisk());	
			
			if(version >= 3){
				cpos = in.getCurrentPosition();
				ss = in.readVariableLengthString("UTF8", cpos, BinFieldSize.WORD, 2);
				entry.enm_str = ss.getString();
				in.skipBytes(ss.getSizeOnDisk());
			}
			else{
				//Generate Enum String
				entry.enm_str = RecompFiles.genEnumStrFromName(entry.name);
			}
		}
		
		return entry;
	}
	
	/*----- Getters -----*/
	
	public boolean flagSet(int flag){
		return ((flags & flag) != 0);
	}
	
	public int getUID(){return uid;}
	public String getName(){return name;}
	public String getEnumString(){return enm_str;}
	public byte getMedium(){return medium;}
	public byte getCache(){return cache;}
	public boolean hasTag(String tag){return tags.contains(tag);}
	public ZonedDateTime getTimeCreated(){return this.time_created;}
	public ZonedDateTime getTimeModified(){return this.time_modified;}
	
	public Collection<String> getTags(){
		List<String> list = new ArrayList<String>(tags.size());
		list.addAll(tags);
		return list;
	}
	
	public String getDataFileName(){
		return String.format("%08x.buseq", uid);
	}
		
	public int getBankCount(){
		if(banks == null && bank_uid == 0) return 0;
		if(bank_uid != 0) return 1;
		return banks.size();
	}
	
	public int getSeqType(){
		return (flags >>> ZeqerSeqTable.FLAG_TYPE_SHAMT) & 0xf;
	}
	
	public int getPrimaryBankUID(){
		return bank_uid;
	}
	
	public List<Integer> getLinkedBankUIDs(){
		if(banks == null) return new LinkedList<Integer>();
		ArrayList<Integer> copy = new ArrayList<Integer>(banks.size() + 1);
		copy.addAll(banks);
		return copy;
	}
	
	/*----- Setters -----*/
	
	public void setName(String s){
		name = s;
		time_modified = ZonedDateTime.now();
	}
	
	public void setEnumString(String s){
		enm_str = s;
	}
	
	public void setMedium(byte val){
		medium = val;
		time_modified = ZonedDateTime.now();
	}
	
	public void setCache(byte val){
		cache = val;
		time_modified = ZonedDateTime.now();
	}
	
	public void setFlags(int mask){
		flags |= mask;
	}
	
	public void clearFlags(int mask){
		flags &= ~mask;
	}
	
	public void setSingleBank(int bank_uid){
		flags &= ~ZeqerSeqTable.FLAG_MULTIBANK;
		if(banks != null){
			banks.clear();
			banks = null;
		}
		this.bank_uid = bank_uid;
		time_modified = ZonedDateTime.now();
	}
	
	public void addBank(int bank_uid){
		flags |= ZeqerSeqTable.FLAG_MULTIBANK;
		if(banks == null) banks = new LinkedList<Integer>();
		banks.add(bank_uid);
		time_modified = ZonedDateTime.now();
	}
	
	public void clearBanks(){
		banks.clear();
		bank_uid = 0;
	}
	
	public void addTag(String tag){
		tags.add(tag);
	}
	
	public void clearTags(){
		tags.clear();
	}
	
	public void updateMD5(byte[] newmd5){
		if(newmd5 == null) return;
		if(newmd5.length != 16) return;
		md5 = newmd5;
		time_modified = ZonedDateTime.now();
	}
	
	public void setSeqType(int val){
		val &= 0xf;
		val <<= ZeqerSeqTable.FLAG_TYPE_SHAMT;
		
		flags &= ~(0xf << ZeqerSeqTable.FLAG_TYPE_SHAMT);
		flags |= val;
	}
	
	/*----- Serialization -----*/
	
	public int getSerializedSize(){
		int size = BASE_SIZE;
		if(flagSet(ZeqerSeqTable.FLAG_MULTIBANK)){
			size += 2;
			if(banks != null) size += banks.size() << 2;
		}
		else size += 4;
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
		size += (tags.size()-1); //Semicolons
		if(size % 2 != 0) size++;
		return size;
	}
	
	public int serializeTo(FileBuffer out){
		if(out == null) return 0;
		int initsize = (int)out.getFileSize();
		
		out.addToFile(uid);
		out.addToFile((short)flags);
		out.addToFile(medium);
		out.addToFile(cache);
		
		if(md5 == null){
			for(int i = 0; i < 16; i++) out.addToFile(FileBuffer.ZERO_BYTE);
		}
		else{
			for(int i = 0; i < 16; i++){
				if(i >= md5.length) out.addToFile(FileBuffer.ZERO_BYTE);
				else out.addToFile(md5[i]);
			}
		}
		
		if(time_created == null) time_created = ZonedDateTime.now();
		out.addToFile(time_created.toEpochSecond());
		
		if(time_modified == null) time_modified = ZonedDateTime.now();
		out.addToFile(time_modified.toEpochSecond());
		
		if(flagSet(ZeqerSeqTable.FLAG_MULTIBANK)){
			int bcount = 0;
			if(banks != null) bcount = banks.size();
			out.addToFile((short)bcount);
			if(banks != null){
				for(Integer b : banks) out.addToFile(b);
			}
		}
		else{
			out.addToFile(bank_uid);
		}
		
		out.addVariableLengthString("UTF8", name, BinFieldSize.WORD, 2);
		
		//Tags
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
	
}
