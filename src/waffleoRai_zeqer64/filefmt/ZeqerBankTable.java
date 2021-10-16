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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	public static final short CURRENT_VERSION = 1;
	
	/*----- Inner Classes -----*/
	
	public static class BankTableEntry{
		
		private static final int BASE_SIZE = 4+4+16+4+4+8+8+2;
	
		private int uid;
		private int flags = 0;
		private byte[] md5;
		
		private int[] icounts;
		private int unk_word;
		
		private ZonedDateTime time_created;
		private ZonedDateTime time_modified;
		
		private String name;
		
		private BankTableEntry(){icounts = new int[3];}
		
		public static BankTableEntry read(FileBuffer in, long offset){
			BankTableEntry entry = new BankTableEntry();
			in.setCurrentPosition(offset);
			
			entry.uid = in.nextInt();
			entry.flags = in.nextInt();
			entry.md5 = new byte[16];
			for(int i = 0; i < 16; i++) entry.md5[i] = in.nextByte();
			
			entry.icounts[0] = Byte.toUnsignedInt(in.nextByte());
			entry.icounts[1] = Byte.toUnsignedInt(in.nextByte());
			entry.icounts[2] = Short.toUnsignedInt(in.nextShort());
			
			entry.unk_word = in.nextInt();
			
			long rawtime = in.nextLong();
			entry.time_created = ZonedDateTime.ofInstant(Instant.ofEpochSecond(rawtime), ZoneId.systemDefault());
			rawtime = in.nextLong();
			entry.time_modified = ZonedDateTime.ofInstant(Instant.ofEpochSecond(rawtime), ZoneId.systemDefault());
			
			long cpos = in.getCurrentPosition();
			SerializedString ss = in.readVariableLengthString("UTF8", cpos, BinFieldSize.WORD, 2);
			entry.name = ss.getString();
			
			return entry;
		}
		
		public String getName(){return name;}
		public int getUID(){return uid;}
		
		public void setName(String s){
			name = s;
			time_modified = ZonedDateTime.now();
		}
		
		public void setInstCounts(int i0, int i1, int i2){
			icounts[0] = i0;
			icounts[1] = i1;
			icounts[2] = i2;
			time_modified = ZonedDateTime.now();
		}
		
		public void setUnknownWord(int val){
			unk_word = val;
			time_modified = ZonedDateTime.now();
		}
		
		public String getDataFileName(){
			return String.format("%08x.zubnk", uid);
		}
		
		public int getSerializedSize(){
			int size = BASE_SIZE;
			if(name != null){
				size += name.length();
				if(size % 2 != 0) size++;
			}
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
			out.addToFile(unk_word);
			
			if(time_created == null) time_created = ZonedDateTime.now();
			out.addToFile(time_created.toEpochSecond());
			
			if(time_modified == null) time_modified = ZonedDateTime.now();
			out.addToFile(time_modified.toEpochSecond());
			
			out.addVariableLengthString("UTF8", name, BinFieldSize.WORD, 2);
			
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
	
	public BankTableEntry getSequence(int UID){
		return entries.get(UID);
	}
	
	public BankTableEntry matchSequenceMD5(String md5str){
		return md5_map.get(md5str);
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
		
		long cpos = data.getCurrentPosition();
		ZeqerBankTable tbl = new ZeqerBankTable();
		for(int i = 0; i < rcount; i++){
			BankTableEntry entry = BankTableEntry.read(data, cpos);
			cpos += entry.getSerializedSize();
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
			cidx_map.put(fields[i].toUpperCase(), i);
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
			raw = fields[cidx_uid].substring(2); //Chop 0x prefix
			n = Integer.parseUnsignedInt(raw, 16); //Let the exception be thrown
			BankTableEntry entry = entries.get(n);
			if(entry == null){
				System.err.println("ZeqerBankTable.importTSV || Bank table entry with UID " + raw + " not found in table. Skipping this record...");
				continue;
			}
			
			//Update update-able fields.
			String key = "NAME";
			if(cidx_map.containsKey(key)) entry.setName(fields[cidx_map.get(key)]);

			update_count++;
		}
		
		br.close();
		
		return update_count;
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
		bw.write("#NAME\tUID\tMD5\tDATE_CREATED\tDATE_MOD\tINST_COUNT\tUNK_WORD\n");
		int ecount = entries.size();
		List<Integer> uids = new ArrayList<Integer>(ecount+1);
		uids.addAll(entries.keySet());
		Collections.sort(uids); 
		
		for(Integer uid : uids){
			BankTableEntry entry = entries.get(uid);
			bw.write(entry.getName() + "\t");
			bw.write(String.format("0x%08x\t", entry.getUID()));
			bw.write(FileUtils.bytes2str(entry.md5).toLowerCase() + "\t");
			bw.write(entry.time_created.format(DateTimeFormatter.ISO_ZONED_DATE_TIME) + "\t");
			bw.write(entry.time_modified.format(DateTimeFormatter.ISO_ZONED_DATE_TIME) + "\t");
			
			bw.write(entry.icounts[0] + ";");
			bw.write(entry.icounts[1] + ";");
			bw.write(entry.icounts[2] + "\t");
			
			bw.write(String.format("0x%08x\n", entry.unk_word));
		}
		
		bw.close();
		
	}
	
}
