package waffleoRai_zeqer64.filefmt;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import waffleoRai_Utils.BinFieldSize;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileUtils;
import waffleoRai_Utils.MultiFileBuffer;
import waffleoRai_Utils.SerializedString;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class ZeqerBankTable {

	/*----- Constants -----*/
	
	public static final String MAGIC = "zeqrBNKt";
	public static final short CURRENT_VERSION = 3;
	
	private static final char SEP = File.separatorChar;
	
	/*----- Inner Classes -----*/
	
	public static class BankTableEntry{
		
		private static final int BASE_SIZE = 4+4+16+4+4+8+8+6;
	
		private int uid;
		private int flags = 0;
		private byte[] md5;
		
		private int[] icounts;
		private byte medium;
		private byte cache;
		private byte warc_idx;
		private byte warc2_idx;
		
		private ZonedDateTime time_created;
		private ZonedDateTime time_modified;
		
		private String name;
		private String enm_str;
		private Set<String> tags;
		
		private BankTableEntry(){icounts = new int[3]; tags = new HashSet<String>();}
		
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
		
		public String getName(){return name;}
		public String getEnumString(){return enm_str;}
		public int getUID(){return uid;}
		public int getWarcIndex(){return Byte.toUnsignedInt(warc_idx);}
		public int getSecondaryWarcIndex(){return Byte.toUnsignedInt(warc2_idx);}
		public byte getMedium(){return medium;}
		public byte getCachePolicy(){return cache;}
		public int getPercussionCount(){return (int)icounts[1];}
		public boolean hasTag(String tag){return tags.contains(tag);}
		
		public Collection<String> getTags(){
			List<String> list = new ArrayList<String>(tags.size());
			list.addAll(tags);
			return list;
		}
		
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
		
		public void addTag(String tag){tags.add(tag);}
		public void clearTags(){tags.clear();}
		
		public String getDataPathStem(){
			return String.format("%08x", uid);
		}
		
		public String getDataFileName(){
			return getDataPathStem() + ".bubnk";
		}
		
		public String getWSDFileName(){
			return getDataPathStem() + ".buwsd";
		}
		
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
		
	}
	
	/*----- Instance Variables -----*/
	
	private Map<Integer, BankTableEntry> entries;
	private Map<String, BankTableEntry> md5_map;
	
	/*----- Initialization -----*/
	
	private ZeqerBankTable(){
		entries = new TreeMap<Integer, BankTableEntry>();
		md5_map = new HashMap<String, BankTableEntry>();
	}
	
	public static ZeqerBankTable createTable(){
		ZeqerBankTable tbl = new ZeqerBankTable();
		return tbl;
	}
	
	/*----- Getters -----*/
	
	public BankTableEntry getBank(int UID){
		return entries.get(UID);
	}
	
	public BankTableEntry matchBankMD5(String md5str){
		return md5_map.get(md5str);
	}
	
	public Collection<BankTableEntry> getAllEntries(){
		List<BankTableEntry> list = new ArrayList<BankTableEntry>(entries.size()+1);
		list.addAll(entries.values());
		return list;
	}
	
	/*----- Setters -----*/
	
	public BankTableEntry newEntry(byte[] md5){
		if(md5 == null) return null;
		
		int id = 0;
		for(int i = 0; i < 4; i++){
			id <<= 8;
			id |= Byte.toUnsignedInt(md5[i]);
		}
		
		BankTableEntry entry = new BankTableEntry();
		entry.uid = id;
		entry.md5 = md5;
		
		entries.put(id, entry);
		String md5str = FileUtils.bytes2str(md5).toLowerCase();
		md5_map.put(md5str, entry);
		
		entry.time_created = ZonedDateTime.now();
		entry.time_modified = ZonedDateTime.now();
		
		entry.name = "bnk_" + Integer.toHexString(id);
		
		return entry;
	}
	
	public void importRecordsFrom(ZeqerBankTable other){
		if(other == null) return;
		
		for(BankTableEntry e : other.entries.values()){
			entries.put(e.uid, e);
		}
		
		md5_map.clear();
		for(BankTableEntry e : entries.values()){
			String md5str = FileUtils.bytes2str(e.md5).toLowerCase();
			md5_map.put(md5str, e);
		}
	}
	
	/*----- Reading -----*/
	
	public static ZeqerBankTable readTable(FileBuffer data) throws UnsupportedFileTypeException{
		return readTable(data, 0L);
	}
	
	public static ZeqerBankTable readTable(FileBuffer data, long offset) throws UnsupportedFileTypeException{
		if(data == null) return null;
		
		//Check magic
		String mcheck = data.getASCII_string(offset, 8);
		if(!mcheck.equals(MAGIC)){
			throw new FileBuffer.UnsupportedFileTypeException("ZeqerBankTable magic number not found!");
		}
		data.setCurrentPosition(offset + 10);
		int version = Short.toUnsignedInt(data.nextShort());
		
		if(version < 1 || version > CURRENT_VERSION){
			throw new FileBuffer.UnsupportedFileTypeException("Version number not recognized!");
		}
		int rcount = data.nextInt();
		
		ZeqerBankTable tbl = new ZeqerBankTable();
		for(int i = 0; i < rcount; i++){
			BankTableEntry entry = BankTableEntry.read(data, version);
			//cpos += entry.getSerializedSize();
			tbl.entries.put(entry.uid, entry);
			String md5str = FileUtils.bytes2str(entry.md5).toLowerCase();
			tbl.md5_map.put(md5str, entry);
		}
		
		return tbl;
	}
	
	public int importTSV(String tsv_path) throws IOException{
		if(tsv_path == null) return 0;
		if(!FileBuffer.fileExists(tsv_path)) return 0;
		
		//This only updates specific fields.
		Map<String, Integer> cidx_map = new HashMap<String, Integer>();
		
		int update_count = 0;
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(tsv_path), StandardCharsets.UTF_8));
		//Process header to determine column indices
		String line = br.readLine();
		if(!line.startsWith("#")){
			System.err.println("ZeqerBankTable.importTSV || tsv header not formatted correctly!");
			br.close();
			return 0;
		}
		line = line.substring(1);
		String[] fields = line.split("\t");
		for(int i = 0; i < fields.length; i++){
			if(fields[i].startsWith("#")) fields[i] = fields[i].substring(1);
			String k = fields[i].toUpperCase();
			cidx_map.put(k, i);
			System.err.println("ZeqerBankTable.importTSV || Field Key Found: " + k);
		}
		
		//Make sure mandatory fields are present
		if(!cidx_map.containsKey("UID")){
			System.err.println("ZeqerBankTable.importTSV || UID field is required!");
			br.close();
			return 0;
		}
		
		//Process records
		String raw = null;
		int n = 0;
		int cidx_uid = cidx_map.get("UID");
		while((line = br.readLine()) != null){
			fields = line.split("\t");
			
			//Look for matching record
			raw = fields[cidx_uid];
			if(raw.startsWith("0x")) raw = raw.substring(2); //Chop 0x prefix
			n = Integer.parseUnsignedInt(raw, 16); //Let the exception be thrown
			BankTableEntry entry = entries.get(n);
			if(entry == null){
				System.err.println("ZeqerBankTable.importTSV || Bank table entry with UID " + raw + " not found in table. Skipping this record...");
				continue;
			}
			
			//Update update-able fields.
			String key = "NAME";
			if(cidx_map.containsKey(key)) entry.setName(fields[cidx_map.get(key)]);
			
			key = "TAGS";
			if(cidx_map.containsKey(key)){
				raw = fields[cidx_map.get(key)];
				if(raw != null && !raw.isEmpty()){
					if(!raw.startsWith("<NONE>")){
						String[] taglist = raw.split(";");
						for(String tag:taglist) entry.addTag(tag);	
					}
				}
			}
			
			key = "ENUM";
			if(cidx_map.containsKey(key)) entry.setEnumString(fields[cidx_map.get(key)]);

			update_count++;
		}
		
		br.close();
		
		return update_count;
	}
	
	public static int[] loadVersionTable(String dir_base, String zeqer_id) throws IOException{
		String path = dir_base + SEP + "bnk_" + zeqer_id + ".bin";
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
			BankTableEntry e = entries.get(uid);
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
	
	public void exportTo(String dirpath) throws IOException{
		String tsvpath = dirpath + File.separator + "_zubnk_tbl.tsv";
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tsvpath), StandardCharsets.UTF_8));
		bw.write("#NAME\tUID\tMD5\tENUM\tDATE_CREATED\tDATE_MOD\tINST_COUNT\tWARC_IDX\tTAGS\n");
		int ecount = entries.size();
		List<Integer> uids = new ArrayList<Integer>(ecount+1);
		uids.addAll(entries.keySet());
		Collections.sort(uids); 
		
		for(Integer uid : uids){
			BankTableEntry entry = entries.get(uid);
			bw.write(entry.getName() + "\t");
			bw.write(String.format("0x%08x\t", entry.getUID()));
			bw.write(FileUtils.bytes2str(entry.md5).toLowerCase() + "\t");
			bw.write(entry.getEnumString() + "\t");
			bw.write(entry.time_created.format(DateTimeFormatter.ISO_ZONED_DATE_TIME) + "\t");
			bw.write(entry.time_modified.format(DateTimeFormatter.ISO_ZONED_DATE_TIME) + "\t");
			
			bw.write(entry.icounts[0] + ";");
			bw.write(entry.icounts[1] + ";");
			bw.write(entry.icounts[2] + "\t");
			
			bw.write(entry.warc_idx + "\t");
			//bw.write(String.format("%02x %02x %02x\n", entry.unk_bytes[0], entry.unk_bytes[1], entry.unk_bytes[2]));
			
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
		
	}
	
}
