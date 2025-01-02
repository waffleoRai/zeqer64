package waffleoRai_zeqer64.filefmt.bank;

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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileUtils;
import waffleoRai_Utils.MultiFileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;

public class ZeqerBankTable {

	/*----- Constants -----*/
	
	public static final String MAGIC = "zeqrBNKt";
	public static final short CURRENT_VERSION = 4;
	
	private static final char SEP = File.separatorChar;
	
	public static final int FLAG_CUSTOM = 0x1;
	public static final int FLAG_Z5 = 0x2;
	public static final int FLAG_Z6 = 0x4;
	
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
	
	public boolean hasBank(int UID) {
		return entries.containsKey(UID);
	}
	
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
	
	public BankTableEntry newEntry(int uid){
		if(uid == 0 || uid == -1) return null;

		BankTableEntry entry = new BankTableEntry();
		entry.uid = uid;
		entry.md5 = new byte[16];
		
		entries.put(uid, entry);
		
		entry.time_created = ZonedDateTime.now();
		entry.time_modified = ZonedDateTime.now();
		
		entry.name = "bnk_" + Integer.toHexString(uid);
		
		return entry;
	}
	
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
	
	public BankTableEntry removeEntry(int uid){
		BankTableEntry e = entries.remove(uid);
		if(e != null && md5_map != null){
			String md5str = FileUtils.bytes2str(e.md5);
			md5_map.remove(md5str);
		}
		return e;
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
