package waffleoRai_zeqer64.filefmt.seq;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_zeqer64.filefmt.TSVTables;
import waffleoRai_Utils.FileUtils;
import waffleoRai_Utils.MultiFileBuffer;

public class ZeqerSeqTable {
	
	/*----- Constants -----*/
	
	public static final String MAGIC = "zeqrSEQt";
	public static final short CURRENT_VERSION = 4;
	
	public static final int FLAG_MULTIBANK = 0x00000001;
	public static final int FLAG_PROJECT = 0x00000002;
	public static final int FLAG_CUSTOM = 0x00000100;
	public static final int FLAG_Z5 = 0x00000200;
	public static final int FLAG_Z6 = 0x00000400;
	public static final int FLAG_MASK_TYPE = 0x0000f000;
	public static final int FLAG_TYPE_SHAMT = 12;
	
	private static final char SEP = File.separatorChar;
	
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
	
	public List<SeqTableEntry> getAllEntries(){
		List<SeqTableEntry> list = new ArrayList<SeqTableEntry>(entries.size()+1);
		list.addAll(entries.values());
		return list;
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
			md5_map.remove(FileUtils.bytes2str(e.md5));
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
				
				value = tsv.getValue("SEQTYPE");
				if(value != null) {
					int type = 0;
					try{type = Integer.parseInt(value);}
					catch(NumberFormatException ex){}
					entry.clearFlags(FLAG_MASK_TYPE);
					entry.setFlags((type & 0xf) << FLAG_TYPE_SHAMT);
				}
				
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
