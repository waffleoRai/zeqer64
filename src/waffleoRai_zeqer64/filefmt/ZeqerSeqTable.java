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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.sound.midi.InvalidMidiDataException;

import waffleoRai_SeqSound.MIDI;
import waffleoRai_SeqSound.n64al.NUSALSeq;
import waffleoRai_Utils.BinFieldSize;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.SerializedString;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.FileUtils;
import waffleoRai_Utils.MultiFileBuffer;

public class ZeqerSeqTable {
	
	/*----- Constants -----*/
	
	public static final String MAGIC = "zeqrSEQt";
	public static final short CURRENT_VERSION = 1;
	
	public static final int FLAG_MULTIBANK = 0x00000001;
	public static final int FLAG_ALLOCTYPE = 0x00000006;
	
	/*----- Inner Classes -----*/
	
	public static class SeqTableEntry{
		
		private static final int BASE_SIZE = 4+3+1+16+8+8+2;
	
		private int uid;
		private int flags = 0;
		private byte unkbyte;
		private byte[] md5;
		
		private ZonedDateTime time_created;
		private ZonedDateTime time_modified;
		
		private int bank_uid;
		private List<Integer> banks; //Only used if multi bank
		
		private String name;
		
		private SeqTableEntry(){}
		
		public static SeqTableEntry read(FileBuffer in, long offset){
			SeqTableEntry entry = new SeqTableEntry();
			in.setCurrentPosition(offset);
			
			entry.uid = in.nextInt();
			entry.flags = in.nextShortish();
			entry.unkbyte = in.nextByte();
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
			
			return entry;
		}
		
		public boolean flagSet(int flag){
			return ((flags & flag) != 0);
		}
		
		public int getAllocType(){
			return (flags & FLAG_ALLOCTYPE) >>> 1;
		}
		
		public int getUID(){return uid;}
		public String getName(){return name;}
		
		public String getDataFileName(){
			return String.format("%08x.zuseq", uid);
		}
		
		public void setAllocType(byte type){
			int type_i = Byte.toUnsignedInt(type);
			type_i &= 3;
			type_i <<= 1;
			flags &= ~FLAG_ALLOCTYPE;
			flags |= type_i;
			
			time_modified = ZonedDateTime.now();
		}
		
		public void setUnkByte(byte b){
			unkbyte = b;
			time_modified = ZonedDateTime.now();
		}
		
		public void setName(String s){
			name = s;
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
			return size;
		}
		
		public int serializeTo(FileBuffer out){
			if(out == null) return 0;
			int initsize = (int)out.getFileSize();
			
			out.addToFile(uid);
			out.add24ToFile(flags);
			out.addToFile(unkbyte);
			
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
		
		long cpos = data.getCurrentPosition();
		ZeqerSeqTable tbl = new ZeqerSeqTable();
		for(int i = 0; i < rcount; i++){
			SeqTableEntry entry = SeqTableEntry.read(data, cpos);
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
			System.err.println("ZeqerSeqTable.importTSV || tsv header not formatted correctly!");
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
			System.err.println("ZeqerSeqTable.importTSV || UID field is required!");
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
			SeqTableEntry entry = entries.get(n);
			if(entry == null){
				System.err.println("ZeqerSeqTable.importTSV || Seq table entry with UID " + raw + " not found in table. Skipping this record...");
				continue;
			}
			
			//Update update-able fields.
			String key = "NAME";
			if(cidx_map.containsKey(key)) entry.setName(fields[cidx_map.get(key)]);
			key = "ALLOCTYPE";
			if(cidx_map.containsKey(key)){
				raw = fields[cidx_map.get(key)];
				n = Integer.parseUnsignedInt(raw);
				entry.setAllocType((byte)n);
			}
			key = "BANKID";
			if(cidx_map.containsKey(key)){
				raw = fields[cidx_map.get(key)];
				if(raw != null){
					String[] blist = raw.split(";");
					if(blist.length > 1){
						raw = blist[0].substring(2);
						n = Integer.parseUnsignedInt(raw, 16);
						entry.setSingleBank(n);
					}
					else{
						if(entry.banks != null) entry.banks.clear();
						for(String bid : blist){
							bid = bid.substring(2);
							n = Integer.parseUnsignedInt(bid, 16);
							entry.addBank(n);
						}
					}
				}
			}

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
		bw.write("#NAME\tUID\tMD5\tDATE_CREATED\tDATE_MOD\tALLOCTYPE\tUNK_BYTE\tMULTIBANK\tBANKID\n");
		int ecount = entries.size();
		List<Integer> uids = new ArrayList<Integer>(ecount+1);
		uids.addAll(entries.keySet());
		Collections.sort(uids); 
		
		for(Integer uid : uids){
			SeqTableEntry entry = entries.get(uid);
			bw.write(entry.getName() + "\t");
			bw.write(String.format("0x%08x\t", entry.getUID()));
			bw.write(FileUtils.bytes2str(entry.md5).toLowerCase() + "\t");
			bw.write(entry.time_created.format(DateTimeFormatter.ISO_ZONED_DATE_TIME) + "\t");
			bw.write(entry.time_modified.format(DateTimeFormatter.ISO_ZONED_DATE_TIME) + "\t");
			bw.write(entry.getAllocType() + "\t");
			bw.write(String.format("0x%02x\t", entry.unkbyte));
			
			if(entry.flagSet(FLAG_MULTIBANK)){
				bw.write("y\t");
				if(entry.banks != null){
					boolean first = true;
					for(Integer b : entry.banks){
						if(!first) bw.write(";");
						bw.write(String.format("0x%8x", b));
						first = false;
					}
					bw.write("\n");
				}
				else bw.write("<NULL>\n");
			}
			else{
				bw.write("n\t");
				bw.write(String.format("0x%8x\n", entry.bank_uid));
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
