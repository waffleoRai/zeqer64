package waffleoRai_zeqer64.filefmt;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import waffleoRai_Utils.BinFieldSize;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.SerializedString;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.FileUtils;
import waffleoRai_Utils.MultiFileBuffer;

public class ZeqerSeqTable {
	
	/*----- Constants -----*/
	
	public static final String MAGIC = "zeqrSEQt";
	public static final short CURRENT_VERSION = 3;
	
	public static final int FLAG_MULTIBANK = 0x00000001;
	
	private static final char SEP = File.separatorChar;
	
	/*----- Inner Classes -----*/
	
	public static class SeqTableEntry{
		
		/*----- Constants -----*/
		
		private static final int BASE_SIZE = 4+4+16+8+8+6;
	
		/*----- Instance Variables -----*/
		
		private int uid;
		private int flags = 0;
		private byte medium;
		private byte cache;
		private byte[] md5;
		
		private ZonedDateTime time_created;
		private ZonedDateTime time_modified;
		
		private int bank_uid;
		private List<Integer> banks; //Only used if multi bank
		
		private String name;
		private String enm_str;
		private Set<String> tags;
		
		/*----- Init -----*/
		
		private SeqTableEntry(){tags = new HashSet<String>();}
		
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
			
			if((entry.flags & FLAG_MULTIBANK) != 0){
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
		
		public Collection<String> getTags(){
			List<String> list = new ArrayList<String>(tags.size());
			list.addAll(tags);
			return list;
		}
		
		public String getDataFileName(){
			return String.format("%08x.buseq", uid);
		}
			
		public int getBankCount(){
			if(banks == null) return 0;
			return banks.size();
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
		
		public void setSingleBank(int bank_uid){
			flags &= ~FLAG_MULTIBANK;
			if(banks != null){
				banks.clear();
				banks = null;
			}
			this.bank_uid = bank_uid;
			time_modified = ZonedDateTime.now();
		}
		
		public void addBank(int bank_uid){
			flags |= FLAG_MULTIBANK;
			if(banks == null) banks = new LinkedList<Integer>();
			banks.add(bank_uid);
			time_modified = ZonedDateTime.now();
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
		
		/*----- Serialization -----*/
		
		public int getSerializedSize(){
			int size = BASE_SIZE;
			if(flagSet(FLAG_MULTIBANK)){
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
			
			if(flagSet(FLAG_MULTIBANK)){
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
	
	/*----- Instance Variables -----*/
	
	private Map<Integer, SeqTableEntry> entries;
	private Map<String, SeqTableEntry> md5_map;
	
	/*----- Initialization -----*/
	
	private ZeqerSeqTable(){
		entries = new TreeMap<Integer, SeqTableEntry>();
		md5_map = new HashMap<String, SeqTableEntry>();
	}
	
	public static ZeqerSeqTable createTable(){
		ZeqerSeqTable tbl = new ZeqerSeqTable();
		return tbl;
	}
	
	/*----- Getters -----*/
	
	public SeqTableEntry getSequence(int UID){
		return entries.get(UID);
	}
	
	public SeqTableEntry matchSequenceMD5(String md5str){
		return md5_map.get(md5str);
	}
	
	/*----- Setters -----*/
	
	public SeqTableEntry newEntry(int uid){
		if(uid == 0) return null;
		if(uid == -1) return null;
		
		SeqTableEntry entry = new SeqTableEntry();
		entry.uid = uid;
		entry.md5 = new byte[16];
		
		entries.put(uid, entry);
		
		entry.time_created = ZonedDateTime.now();
		entry.time_modified = ZonedDateTime.now();
		
		entry.name = "seq_" + Integer.toHexString(uid);
		
		return entry;
	}
	
	public SeqTableEntry newEntry(byte[] md5){
		if(md5 == null) return null;
		
		int id = 0;
		for(int i = 0; i < 4; i++){
			id <<= 8;
			id |= Byte.toUnsignedInt(md5[i]);
		}
		
		SeqTableEntry entry = new SeqTableEntry();
		entry.uid = id;
		entry.md5 = md5;
		
		entries.put(id, entry);
		String md5str = FileUtils.bytes2str(md5).toLowerCase();
		md5_map.put(md5str, entry);
		
		entry.time_created = ZonedDateTime.now();
		entry.time_modified = ZonedDateTime.now();
		
		entry.name = "seq_" + Integer.toHexString(id);
		
		return entry;
	}
	
	public void importRecordsFrom(ZeqerSeqTable other){
		if(other == null) return;
		
		for(SeqTableEntry e : other.entries.values()){
			entries.put(e.uid, e);
		}
		
		md5_map.clear();
		for(SeqTableEntry e : entries.values()){
			String md5str = FileUtils.bytes2str(e.md5).toLowerCase();
			md5_map.put(md5str, e);
		}
	}
	
	public SeqTableEntry deleteEntry(int uid){
		SeqTableEntry e = entries.remove(uid);
		if(e == null) return null;
		if(md5_map != null){
			entries.remove(FileUtils.bytes2str(e.md5));
		}
		return e;
	}
	
	/*----- Reading -----*/
	
	public static ZeqerSeqTable readTable(FileBuffer data) throws UnsupportedFileTypeException{
		return readTable(data, 0L);
	}
	
	public static ZeqerSeqTable readTable(FileBuffer data, long offset) throws UnsupportedFileTypeException{
		if(data == null) return null;
		
		//Check magic
		String mcheck = data.getASCII_string(offset, 8);
		if(!mcheck.equals(MAGIC)){
			throw new FileBuffer.UnsupportedFileTypeException("ZeqerSeqTable magic number not found!");
		}
		data.setCurrentPosition(offset + 10);
		int version = Short.toUnsignedInt(data.nextShort());
		
		if(version < 1 || version > CURRENT_VERSION){
			throw new FileBuffer.UnsupportedFileTypeException("Version number not recognized!");
		}
		int rcount = data.nextInt();
		
		//long cpos = data.getCurrentPosition();
		ZeqerSeqTable tbl = new ZeqerSeqTable();
		for(int i = 0; i < rcount; i++){
			SeqTableEntry entry = SeqTableEntry.read(data,version);
			//cpos += entry.getSerializedSize(); //You can't use this - will have versioning issues.
			tbl.entries.put(entry.uid, entry);
			String md5str = FileUtils.bytes2str(entry.md5).toLowerCase();
			tbl.md5_map.put(md5str, entry);
		}
		
		return tbl;
	}
	
	public int importTSV(String tsv_path) throws IOException{
		if(tsv_path == null) return 0;
		if(!FileBuffer.fileExists(tsv_path)) return 0;
		
		int import_count = 0;
		TSVTables tsv = new TSVTables(tsv_path);
		
		//Check for required fields...
		if(!tsv.hasField("UID")){tsv.closeReader(); return 0;}
		
		//Loop through records...
		try{
			while(tsv.nextRecord()){
				int seqid = tsv.getValueAsHexInt("UID");
				SeqTableEntry entry = getSequence(seqid);
				if(entry == null){
					System.err.println("ZeqerSeqTable.importTSV || Sequence " + String.format("%08x could not be found. Skipping...", seqid));
					continue;
				}
				
				String value = tsv.getValue("NAME");
				if(value != null) entry.setName(value);
				
				value = tsv.getValue("ENUM");
				if(value != null) entry.setEnumString(value);
				
				value = tsv.getValue("TAGS");
				if(value != null && !value.isEmpty()){
					String[] split = value.split(";");
					if(split == null) continue;
					if(entry.tags == null){
						entry.tags = new HashSet<String>();
					}
					for(String s : split){
						if(!s.isEmpty()) entry.tags.add(s);
					}
				}
				
			}
		}catch(NumberFormatException ex){
			System.err.println("ZeqerSeqTable.importTSV || Number parsing error caught!");
			ex.printStackTrace();
		}
		
		tsv.closeReader();

		return import_count;
	}

	public static int[] loadVersionTable(String dir_base, String zeqer_id) throws IOException{
		String path = dir_base + SEP + "seq_" + zeqer_id + ".bin";
		if(!FileBuffer.fileExists(path)) return null;
		FileBuffer buffer = FileBuffer.createBuffer(path, true);
		long sz = buffer.getFileSize();
		int bcount = (int)sz >>> 2;
		buffer.setCurrentPosition(0L);
		
		int[] ids = new int[bcount];
		for(int i = 0; i < bcount; i++){
			ids[i] = buffer.nextInt();
		}
		
		return ids;
	}
	
	/*----- Writing -----*/
	
	public int writeTo(FileBuffer out){
		//VERSION: 1
		int sz = 0;
		FileBuffer header = new FileBuffer(16, true);
		header.printASCIIToFile(MAGIC);
		header.addToFile((short)0);
		header.addToFile(CURRENT_VERSION);
		
		int ecount = entries.size();
		header.addToFile(ecount);
		out.addToFile(header);
		sz += (int)header.getFileSize();
		
		List<Integer> uids = new ArrayList<Integer>(ecount+1);
		uids.addAll(entries.keySet());
		Collections.sort(uids); 
		
		for(Integer uid : uids){
			SeqTableEntry e = entries.get(uid);
			int esize = e.getSerializedSize();
			FileBuffer ebuff = new FileBuffer(esize+8, true);
			e.serializeTo(ebuff);
			out.addToFile(ebuff);
			sz += (int)ebuff.getFileSize();
		}
		
		return sz;
	}

	public int writeTo(OutputStream out) throws IOException{
		int ecount = entries.size();
		MultiFileBuffer buff = new MultiFileBuffer(1+ecount);
		int size = writeTo(buff);
		buff.writeToStream(out);
		return size;
	}
	
	public int writeTo(String outpath) throws IOException{
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outpath));
		int written = writeTo(bos);
		bos.close();
		return written;
	}
	
	public void exportTo(String dirpath, String datdir) throws IOException{
		//Writes table to tsv and converts all seqs to MIDI
		//This is for easy(?) manual inspection using other programs
		
		String tsvpath = dirpath + File.separator + "_zuseq_tbl.tsv";
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tsvpath), StandardCharsets.UTF_8));
		bw.write("#NAME\tUID\tMD5\tENUM\tDATE_CREATED\tDATE_MOD\tMEDIUM\tCACHE\tMULTIBANK\tBANKID\tTAGS\n");
		int ecount = entries.size();
		List<Integer> uids = new ArrayList<Integer>(ecount+1);
		uids.addAll(entries.keySet());
		Collections.sort(uids); 
		
		for(Integer uid : uids){
			SeqTableEntry entry = entries.get(uid);
			bw.write(entry.getName() + "\t");
			bw.write(String.format("0x%08x\t", entry.getUID()));
			bw.write(FileUtils.bytes2str(entry.md5).toLowerCase() + "\t");
			bw.write(entry.getEnumString() + "\t");
			bw.write(entry.time_created.format(DateTimeFormatter.ISO_ZONED_DATE_TIME) + "\t");
			bw.write(entry.time_modified.format(DateTimeFormatter.ISO_ZONED_DATE_TIME) + "\t");
			bw.write(entry.getMedium() + "\t");
			bw.write(entry.getCache() + "\t");
			
			if(entry.flagSet(FLAG_MULTIBANK)){
				bw.write("y\t");
				if(entry.banks != null){
					boolean first = true;
					for(Integer b : entry.banks){
						if(!first) bw.write(";");
						bw.write(String.format("0x%8x", b));
						first = false;
					}
					bw.write("\t");
				}
				else bw.write("<NULL>\t");
			}
			else{
				bw.write("n\t");
				bw.write(String.format("0x%8x\t", entry.bank_uid));
			}

			if(entry.tags.isEmpty()){
				bw.write("<NONE>\n");
			}
			else{
				boolean first = true;
				for(String tag:entry.tags){
					if(!first)bw.write(";");
					bw.write(tag);
					first = false;
				}
				bw.write("\n");
			}
			
		}
		
		bw.close();
		
		/*if(datdir != null){
			//Write to mid
			for(Integer uid : uids){
				System.err.println("Converting seq 0x" + Integer.toHexString(uid) + " to midi");
				SeqTableEntry entry = entries.get(uid);
				String srcpath = datdir + File.separator + entry.getDataFileName();
				String tpath = dirpath + File.separator + entry.getName() + ".mid";
				
				FileBuffer dat = FileBuffer.createBuffer(srcpath, true);
				//If the format is not 0x20, skip
				int fmt = Byte.toUnsignedInt(dat.getByte(1L));
				if(fmt != 0x20){
					System.err.println("ZeqerSeqTable.exportTo || Sequence in non-standard format, skipping conversion");
					continue;
				}
				
				NUSALSeq seq = new NUSALSeq(dat);
				try {
					MIDI m = seq.toMidi();
					m.writeMIDI(tpath);
				} catch (InvalidMidiDataException e) {
					e.printStackTrace();
				}
			}
		}*/
	}
	
}
