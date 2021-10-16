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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import waffleoRai_Sound.nintendo.Z64Wave;
import waffleoRai_Utils.BinFieldSize;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.FileUtils;
import waffleoRai_Utils.MultiFileBuffer;
import waffleoRai_Utils.SerializedString;
import waffleoRai_soundbank.nintendo.Z64Bank.WaveInfoBlock;

public class ZeqerWaveTable {

	/*----- Constants -----*/
	
	public static final String MAGIC = "zeqrWAVt";
	public static final short CURRENT_VERSION = 1;
	
	public static final int FLAG_ISMUSIC = 0x00000001;
	public static final int FLAG_ISACTOR = 0x00000002;
	public static final int FLAG_ISENV = 0x00000004;
	public static final int FLAG_ISSFX = 0x00000008;
	
	public static final int FLAG_2BIT = 0x40000000;
	public static final int FLAG_ADPCM = 0x80000000;
	
	/*----- Inner Classes -----*/
	
	public static class WaveTableEntry{
		
		private static final int BASE_SIZE = 4+4+16+16+2;
		
		private int UID;
		private int flags = 0;
		private byte[] md5;
		private int samplerate = 22050;
		private byte unity_key = 60;
		private byte fine_tune = 0;
		private byte loop_count = 0;
		//byte reserved
		private int loop_start = -1;
		private int loop_end = -1;
		
		private int pred_order = -1;
		private int pred_count = -1;
		private short[] pred_table;
		
		private String name;
		
		private WaveTableEntry(){}
		
		public static WaveTableEntry read(FileBuffer in, long offset){
			//System.err.println("DEBUG: Starting entry read at 0x" + Long.toHexString(offset));
			WaveTableEntry entry = new WaveTableEntry();
			in.setCurrentPosition(offset);
			
			entry.UID = in.nextInt();
			entry.flags = in.nextInt();
			entry.md5 = new byte[16];
			for(int i = 0; i < 16; i++) entry.md5[i] = in.nextByte();
			entry.samplerate = in.nextInt();
			entry.unity_key = in.nextByte();
			entry.fine_tune = in.nextByte();
			entry.loop_count = in.nextByte();
			in.nextByte(); //Reserved
			entry.loop_start = in.nextInt();
			entry.loop_end = in.nextInt();
			
			//If ADPCM
			if((entry.flags | FLAG_ADPCM) != 0){
				entry.pred_order = in.nextInt();
				entry.pred_count = in.nextInt();
				int n = (entry.pred_count * entry.pred_order) << 3;
				entry.pred_table = new short[n];
				for(int i = 0; i < n; i++) entry.pred_table[i] = in.nextShort();
			}
			
			long pos = in.getCurrentPosition();
			SerializedString ss = in.readVariableLengthString("UTF8", pos, BinFieldSize.WORD, 2);
			entry.name = ss.getString();
			
			return entry;
		}
		
		public int getUID(){return UID;}
		public String getName(){return name;}
		
		public boolean isFlagSet(int flagMask){
			return (flags & flagMask) != 0; 
		}
		
		public void setName(String s){name = s;}
		public void setFlags(int flagMask){flags |= flagMask;}
		
		public String getDataFileName(){
			return String.format("%08x.adpcm", UID);
		}
		
		public int getSerializedSize(){
			int size = BASE_SIZE;
			
			//Add ADPCM data
			if(isFlagSet(FLAG_ADPCM)){
				size += 8;
				int n = (pred_order * pred_count) << 3;
				size += n << 1;
			}
			
			//Add name size
			int nlen = name.length();
			size += nlen;
			if(nlen % 2 != 0) size++;
			
			return size;
		}
		
		public int serializeTo(FileBuffer out){
			long initsize = out.getFileSize();
			
			out.addToFile(UID);
			out.addToFile(flags);
			if(md5 == null){for(int i = 0; i < 4; i++) out.addToFile(0);}
			else{
				for(int i = 0; i < 16; i++){
					if(md5.length <= i) out.addToFile(FileBuffer.ZERO_BYTE);
					else{
						out.addToFile(md5[i]);
					}
				}
			}
			
			out.addToFile(samplerate);
			out.addToFile(unity_key);
			out.addToFile(fine_tune);
			out.addToFile(loop_count);
			out.addToFile(FileBuffer.ZERO_BYTE);
			out.addToFile(loop_start);
			out.addToFile(loop_end);
			
			if(isFlagSet(FLAG_ADPCM)){
				out.addToFile(pred_order);
				out.addToFile(pred_count);
				int n = (pred_order * pred_count) << 3;
				if(pred_table == null){
					for(int i = 0; i < n; i++){
						out.addToFile((short)0);
					}
				}
				else{
					for(int i = 0; i < n; i++){
						if(pred_table.length <= i) out.addToFile((short)0);
						else{
							out.addToFile(pred_table[i]);
						}
					}	
				}
			}
			
			out.addVariableLengthString("UTF8", name, BinFieldSize.WORD, 2);
			
			return (int)(out.getFileSize() - initsize);
		}
		
		public int serializeTo(OutputStream out) throws IOException{
			FileBuffer buffer = new FileBuffer(getSerializedSize()+4, true);
			int count = serializeTo(buffer);
			buffer.writeToStream(out);
			return count;
		}
		
	}

	/*----- Instance Variables -----*/
	
	private Map<Integer, WaveTableEntry> entries;
	private Map<String, WaveTableEntry> md5_map;
	
	/*----- Initialization -----*/
	
	private ZeqerWaveTable(){
		entries = new TreeMap<Integer, WaveTableEntry>();
		md5_map = new TreeMap<String, WaveTableEntry>();
	}
	
	public static ZeqerWaveTable createTable(){
		ZeqerWaveTable tbl = new ZeqerWaveTable();
		return tbl;
	}
	
	/*----- Getters -----*/
	
	public WaveTableEntry getEntryWithSum(String md5str){return md5_map.get(md5str);}
	
	/*----- Setters -----*/
	
	public WaveTableEntry addEntryFromBankBlock(WaveInfoBlock block, byte[] md5sum){
		if(block == null || md5sum == null) return null;
		WaveTableEntry entry = new WaveTableEntry();
		int uid = 0;
		for(int i = 0; i < 4; i++){
			uid <<= 8;
			uid |= Byte.toUnsignedInt(md5sum[i]);
		}
		
		entry.UID = uid;
		entry.md5 = md5sum;
		entry.flags |= FLAG_ADPCM;
		entry.loop_start = block.getLoopStart();
		entry.loop_end = block.getLoopEnd();
		entry.loop_count = (byte)block.getLoopCount();
		entry.pred_count = block.getPredCount();
		entry.pred_order = block.getPredOrder();
		entry.pred_table = block.getPredictorTable();
		if(block.isTwoBit()) entry.flags |= FLAG_2BIT;
		entry.name = "adpcmsound_" + Integer.toHexString(uid);
		
		entries.put(entry.UID, entry);
		md5_map.put(FileUtils.bytes2str(entry.md5).toLowerCase(), entry);
		
		return entry;
	}
	
	/*----- Reading -----*/
	
	public static ZeqerWaveTable readTable(FileBuffer data) throws UnsupportedFileTypeException{
		return readTable(data, 0L);
	}
	
	public static ZeqerWaveTable readTable(FileBuffer data, long offset) throws UnsupportedFileTypeException{
		if(data == null) return null;
		data.setEndian(true);
		
		ZeqerWaveTable table = new ZeqerWaveTable();
		String mcheck = data.getASCII_string(offset, 8);
		if(!mcheck.equals(MAGIC)){
			throw new FileBuffer.UnsupportedFileTypeException("ZeqerWaveTable magic number not found!");
		}
		data.setCurrentPosition(offset + 10);
		int version = Short.toUnsignedInt(data.nextShort());
		
		if(version < 1 || version > CURRENT_VERSION){
			throw new FileBuffer.UnsupportedFileTypeException("Version number not recognized!");
		}
		
		int rcount = data.nextInt();
		long cpos = data.getCurrentPosition();
		for(int i = 0; i < rcount; i++){
			WaveTableEntry entry = WaveTableEntry.read(data, cpos);
			table.entries.put(entry.getUID(), entry);
			table.md5_map.put(FileUtils.bytes2str(entry.md5).toLowerCase(), entry);
			cpos += entry.getSerializedSize();
		}
		
		return table;
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
			System.err.println("ZeqerWaveTable.importTSV || tsv header not formatted correctly!");
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
			System.err.println("ZeqerWaveTable.importTSV || UID field is required!");
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
			WaveTableEntry entry = entries.get(n);
			if(entry == null){
				System.err.println("ZeqerWaveTable.importTSV || Wave table entry with UID " + raw + " not found in table. Skipping this record...");
				continue;
			}
			
			//Update update-able fields.
			String key = "NAME";
			if(cidx_map.containsKey(key)) entry.setName(fields[cidx_map.get(key)]);
			key = "SAMPLERATE";
			if(cidx_map.containsKey(key)){
				raw = fields[cidx_map.get(key)];
				n = Integer.parseUnsignedInt(raw);
				entry.samplerate = n;
			}
			key = "UNITYKEY";
			if(cidx_map.containsKey(key)){
				raw = fields[cidx_map.get(key)];
				n = Integer.parseUnsignedInt(raw);
				entry.unity_key = (byte)n;
			}
			key = "FINETUNE";
			if(cidx_map.containsKey(key)){
				raw = fields[cidx_map.get(key)];
				n = Integer.parseUnsignedInt(raw);
				entry.fine_tune = (byte)n;
			}
			key = "LOOPSTART";
			if(cidx_map.containsKey(key)){
				raw = fields[cidx_map.get(key)];
				n = Integer.parseUnsignedInt(raw);
				entry.loop_start = n;
			}
			key = "LOOPEND";
			if(cidx_map.containsKey(key)){
				raw = fields[cidx_map.get(key)];
				n = Integer.parseUnsignedInt(raw);
				entry.loop_end = n;
			}
			key = "LOOPCOUNT";
			if(cidx_map.containsKey(key)){
				raw = fields[cidx_map.get(key)];
				n = Integer.parseUnsignedInt(raw);
				entry.loop_count = (byte)n;
			}
			key = "IS_MUSIC";
			if(cidx_map.containsKey(key)){
				raw = fields[cidx_map.get(key)].toLowerCase();
				if(raw.equals("y")) entry.setFlags(FLAG_ISMUSIC);
			}
			key = "IS_ACTOR";
			if(cidx_map.containsKey(key)){
				raw = fields[cidx_map.get(key)].toLowerCase();
				if(raw.equals("y")) entry.setFlags(FLAG_ISACTOR);
			}
			key = "IS_ENV";
			if(cidx_map.containsKey(key)){
				raw = fields[cidx_map.get(key)].toLowerCase();
				if(raw.equals("y")) entry.setFlags(FLAG_ISENV);
			}
			key = "IS_SFX";
			if(cidx_map.containsKey(key)){
				raw = fields[cidx_map.get(key)].toLowerCase();
				if(raw.equals("y")) entry.setFlags(FLAG_ISSFX);
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
		
		//Sort UIDs so get consistent ordering
		//This isn't format mandatory, it's just nice :3
		List<Integer> uids = new ArrayList<Integer>(ecount+1);
		uids.addAll(entries.keySet());
		Collections.sort(uids); 
		
		for(Integer uid : uids){
			WaveTableEntry e = entries.get(uid);
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
		//Writes table to tsv and converts all sounds to .WAV
		//This is for easy(?) manual inspection using other programs
		
		String tsvpath = dirpath + File.separator + "_zuwav_tbl.tsv";
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tsvpath), StandardCharsets.UTF_8));
		bw.write("#NAME\tUID\tMD5\tSAMPLERATE\tUNITYKEY\tFINETUNE\tLOOPSTART\tLOOPEND\tLOOPCOUNT\t" +
		 "PREDORDER\tPREDCOUNT\tIS_ADPCM\tIS_TWOBIT\tIS_MUSIC\tIS_ACTOR\tIS_ENV\tIS_SFX\n");
		int ecount = entries.size();
		List<Integer> uids = new ArrayList<Integer>(ecount+1);
		uids.addAll(entries.keySet());
		Collections.sort(uids); 
		
		for(Integer uid : uids){
			WaveTableEntry entry = entries.get(uid);
			bw.write(entry.getName() + "\t");
			bw.write(String.format("0x%08x\t", entry.getUID()));
			bw.write(FileUtils.bytes2str(entry.md5).toLowerCase() + "\t");
			bw.write(entry.samplerate + "\t");
			bw.write(entry.unity_key + "\t");
			bw.write(entry.fine_tune + "\t");
			bw.write(entry.loop_start + "\t");
			bw.write(entry.loop_end + "\t");
			bw.write(entry.loop_count + "\t");
			bw.write(entry.pred_order + "\t");
			bw.write(entry.pred_count + "\t");
			
			if(entry.isFlagSet(FLAG_ADPCM)) bw.write("y\t");
			else bw.write("n\t");
			if(entry.isFlagSet(FLAG_2BIT)) bw.write("y\t");
			else bw.write("n\t");
			if(entry.isFlagSet(FLAG_ISMUSIC)) bw.write("y\t");
			else bw.write("n\t");
			if(entry.isFlagSet(FLAG_ISACTOR)) bw.write("y\t");
			else bw.write("n\t");
			if(entry.isFlagSet(FLAG_ISENV)) bw.write("y\t");
			else bw.write("n\t");
			if(entry.isFlagSet(FLAG_ISSFX)) bw.write("y\n");
			else bw.write("n\n");
		}
		
		bw.close();
		
		if(datdir != null){
			//Write waves
			for(Integer uid : uids){
				WaveTableEntry entry = entries.get(uid);
				String srcpath = datdir + File.separator + entry.getDataFileName();
				String tpath = dirpath + File.separator + entry.getName() + ".wav";
				
				FileBuffer wavdat = FileBuffer.createBuffer(srcpath, true);
				Z64Wave wave = Z64Wave.readZ64Wave(wavdat, entry.pred_table, entry.loop_start, entry.loop_end, entry.isFlagSet(FLAG_2BIT));
				wave.writeToWav(tpath);
			}
		}
	}
	
}
