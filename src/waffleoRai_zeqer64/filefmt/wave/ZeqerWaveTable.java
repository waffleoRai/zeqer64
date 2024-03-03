package waffleoRai_zeqer64.filefmt.wave;

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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.FileUtils;
import waffleoRai_Utils.MultiFileBuffer;
import waffleoRai_zeqer64.extract.WaveLocIDMap;

public class ZeqerWaveTable {
	
	//TODO Flags must by synced between UWAV and table...

	/*----- Constants -----*/
	
	public static final String MAGIC = "zeqrWAVt";
	public static final short CURRENT_VERSION = 3;
	
	public static final int FLAG_ISMUSIC = 0x00000001;
	public static final int FLAG_ISACTOR = 0x00000002;
	public static final int FLAG_ISENV = 0x00000004;
	public static final int FLAG_ISSFX = 0x00000008;
	public static final int FLAG_ISVOX = 0x00000010;
	public static final int FLAG_ISUNUSED = 0x00000020;
	public static final int FLAG_ISIN_INST = 0x00000040;
	public static final int FLAG_ISIN_PERC = 0x00000080;
	public static final int FLAG_ISIN_SFX = 0x00000100;
	public static final int FLAG_ISIN_OOTv0 = 0x00000200;
	public static final int FLAG_ISIN_OOT = 0x00000400;
	public static final int FLAG_ISIN_MM = 0x00000800;
	public static final int FLAG_ISIN_OOTv0_MAINARC = 0x00001000;
	public static final int FLAG_ISIN_OOT_MAINARC = 0x00002000;
	public static final int FLAG_ISIN_MM_MAINARC = 0x00004000;
	public static final int FLAG_ISCUSTOM = 0x00008000;
	
	//public static final int FLAG_2BIT = 0x40000000; //DEPRECATED
	public static final int FLAG_ADPCM = 0x80000000;
	
	public static final String[] FLAG_NAMES = {"Music", "Actor", "Environment", "SFX", "Voice", "Unused", 
			"In Instrument", "In Drum", "In SFX", "Ocarina 1.0", "Ocarina", "Majora", "Ocarina 1.0 Main Bank",
			"Ocarina Main Bank", "Majora Main Bank", "User Custom"};
	
	/*----- Instance Variables -----*/
	
	private Map<Integer, WaveTableEntry> entries;
	private Map<String, WaveTableEntry> md5_map;
	
	private Map<String, WaveTableEntry> name_map;
	
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
	public WaveTableEntry getEntryWithUID(int uid){return entries.get(uid);}
	
	public WaveTableEntry getEntryWithName(String name){
		if(name_map == null){
			name_map = new HashMap<String, WaveTableEntry>();
			for(WaveTableEntry entry : entries.values()){
				name_map.put(entry.getName().toUpperCase(), entry);
			}
		}
		return name_map.get(name.toUpperCase());
	}
	
	public List<Integer> getAllEntryIDs(){
		if(entries == null || entries.isEmpty()){
			return new LinkedList<Integer>();
		}
		List<Integer> list = new ArrayList<Integer>(entries.size());
		list.addAll(entries.keySet());
		Collections.sort(list);
		return list;
	}
	
	public List<WaveTableEntry> getAllEntries(){
		if(entries == null || entries.isEmpty()){
			return new LinkedList<WaveTableEntry>();
		}
		List<WaveTableEntry> list = new ArrayList<WaveTableEntry>(entries.size());
		list.addAll(entries.values());
		return list;
	}
	
	/*----- Setters -----*/	

	public WaveTableEntry addEntryFromInfoBlock(Z64WaveInfo block, byte[] md5sum){
		if(block == null || md5sum == null) return null;
		WaveTableEntry entry = new WaveTableEntry();
		entry.wave_info = block;
		entry.md5 = new byte[16];
		int uid = 0;
		for(int i = 0; i < 16; i++){
			if(i >= md5sum.length) break;
			entry.md5[i] = md5sum[i];
			if(i < 4){
				uid <<= 8;
				uid |= Byte.toUnsignedInt(entry.md5[i]);
			}
		}
		entry.wave_info.setUID(uid);
		entry.setFlags(FLAG_ISUNUSED);
		
		entries.put(uid, entry);
		md5_map.put(FileUtils.bytes2str(md5sum), entry);
		
		return entry;
	}
	
	public WaveTableEntry removeEntryWithUID(int uid){
		//If failed, returns null.
		WaveTableEntry entry = entries.remove(uid);
		if(entry == null) return null;
		if(name_map != null) name_map.remove(entry.getName());
		if(md5_map != null){
			md5_map.remove(FileUtils.bytes2str(entry.md5));
		}
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
			WaveTableEntry entry = WaveTableEntry.read(data, cpos, version);
			table.entries.put(entry.getUID(), entry);
			table.md5_map.put(FileUtils.bytes2str(entry.md5).toLowerCase(), entry);
			cpos += entry.getReadBytes();
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
			if(fields[i].startsWith("#")) fields[i] = fields[i].substring(1);
			String k = fields[i].toUpperCase();
			cidx_map.put(k, i);
			System.err.println("ZeqerWaveTable.importTSV || Field Key Found: " + k);
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
			raw = fields[cidx_uid];
			if(raw.startsWith("0x")) raw = raw.substring(2); //Chop 0x prefix
			if(raw.length() > 8) raw = raw.substring(0, 8);
			n = Integer.parseUnsignedInt(raw, 16); //Let the exception be thrown
			WaveTableEntry entry = entries.get(n);
			if(entry == null){
				System.err.println("ZeqerWaveTable.importTSV || Wave table entry with UID " + raw + " not found in table. Skipping this record...");
				continue;
			}
			
			//Update update-able fields.
			String key = "NAME";
			if(cidx_map.containsKey(key)) entry.setName(fields[cidx_map.get(key)]);
			/*key = "SAMPLERATE";
			if(cidx_map.containsKey(key)){
				raw = fields[cidx_map.get(key)];
				n = Integer.parseUnsignedInt(raw);
				entry.setSampleRate(n);
			}*/
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
			/*key = "LOOPSTART";
			if(cidx_map.containsKey(key)){
				raw = fields[cidx_map.get(key)];
				n = Integer.parseUnsignedInt(raw);
				entry.wave_info.setLoopStart(n);
			}
			key = "LOOPEND";
			if(cidx_map.containsKey(key)){
				raw = fields[cidx_map.get(key)];
				n = Integer.parseUnsignedInt(raw);
				entry.wave_info.setLoopEnd(n);
			}
			key = "LOOPCOUNT";
			if(cidx_map.containsKey(key)){
				raw = fields[cidx_map.get(key)];
				n = Integer.parseUnsignedInt(raw);
				entry.wave_info.setLoopCount(n);
			}*/
			key = "IS_MUSIC";
			if(cidx_map.containsKey(key)){
				raw = fields[cidx_map.get(key)].toLowerCase();
				if(raw.equals("y")) entry.wave_info.flagAsMusic(true);
				else entry.wave_info.flagAsMusic(false);
			}
			key = "IS_ACTOR";
			if(cidx_map.containsKey(key)){
				raw = fields[cidx_map.get(key)].toLowerCase();
				if(raw.equals("y")) entry.wave_info.flagAsActor(true);
				else entry.wave_info.flagAsActor(false);
			}
			key = "IS_ENV";
			if(cidx_map.containsKey(key)){
				raw = fields[cidx_map.get(key)].toLowerCase();
				if(raw.equals("y")) entry.wave_info.flagAsEnv(true);
				else entry.wave_info.flagAsEnv(false);
			}
			key = "IS_SFX";
			if(cidx_map.containsKey(key)){
				raw = fields[cidx_map.get(key)].toLowerCase();
				if(raw.equals("y")) entry.wave_info.flagAsSFX(true);
				else entry.wave_info.flagAsSFX(false);
			}
			key = "IS_VOX";
			if(cidx_map.containsKey(key)){
				raw = fields[cidx_map.get(key)].toLowerCase();
				if(raw.equals("y")) entry.setFlags(FLAG_ISVOX);
				else entry.clearFlags(FLAG_ISVOX);
			}
			key = "TAGS";
			if(cidx_map.containsKey(key)){
				int fidx = cidx_map.get(key);
				//Tag field may be blank and get trimmed.
				if(fidx < fields.length){
					raw = fields[fidx];
					if(raw != null && !raw.isEmpty()){
						String[] tagsplit = raw.split(";");
						if(entry.tags == null){
							entry.tags = new HashSet<String>();
						}
						for(String tag:tagsplit) entry.tags.add(tag);
					}
				}
			}
		
			update_count++;
		}
		
		br.close();
		
		return update_count;
	}
	
	public static VersionWaveTable loadVersionTable(String dirpath, String verID) throws IOException{
		String path = dirpath + File.separator + "wav_" + verID + ".bin";
		return loadVersionTable(path);
	}
	
	public static VersionWaveTable loadVersionTable(String filepath) throws IOException{
		VersionWaveTable vwt = VersionWaveTable.readIn(filepath);
		return vwt;
	}
	
	public static WaveLocIDMap loadVersionWaveOffsetIDMap(String dirpath, String verID) throws IOException{
		String path = dirpath + File.separator + "wav_" + verID + ".bin";
		return WaveLocIDMap.readVersionMap(path);
	}
	
	/*----- Writing -----*/
	
	public int writeTo(FileBuffer out){
		//VERSION: 2
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
		 "PREDORDER\tPREDCOUNT\tCODEC\tIS_MUSIC\tIS_ACTOR\tIS_ENV\tIS_SFX\tTAGS\n");
		int ecount = entries.size();
		List<Integer> uids = new ArrayList<Integer>(ecount+1);
		uids.addAll(entries.keySet());
		Collections.sort(uids); 
		
		for(Integer uid : uids){
			WaveTableEntry entry = entries.get(uid);
			String srcpath = datdir + File.separator + entry.getDataFileName();
			if(FileBuffer.fileExists(srcpath)){
				try{
					UltraWavFile uwav = UltraWavFile.createUWAV(srcpath);
					uwav.readWaveInfo(entry.wave_info);
				}
				catch(Exception ex){
					ex.printStackTrace();
				}
			}
			
			bw.write(entry.getName() + "\t");
			bw.write(String.format("0x%08x\t", entry.getUID()));
			bw.write(FileUtils.bytes2str(entry.md5).toLowerCase() + "\t");
			bw.write(entry.getSampleRate() + "\t");
			bw.write(entry.unity_key + "\t");
			bw.write(entry.fine_tune + "\t");
			bw.write(entry.wave_info.getLoopStart() + "\t");
			bw.write(entry.wave_info.getLoopEnd() + "\t");
			bw.write(entry.wave_info.getLoopCount() + "\t");
			
			if(entry.wave_info.getADPCMBook() != null){
				bw.write(entry.wave_info.getADPCMBook().getOrder() + "\t");
				bw.write(entry.wave_info.getADPCMBook().getPredictorCount() + "\t");
			}
			else{
				bw.write("N/A\tN/A\t");
			}
			
			switch(entry.wave_info.getCodec()){
			case Z64Sound.CODEC_ADPCM:
				bw.write("ADP9"); break;
			case Z64Sound.CODEC_REVERB:
				bw.write("RVRB"); break;
			case Z64Sound.CODEC_S16:
				bw.write("_PCM"); break;
			case Z64Sound.CODEC_S16_INMEMORY:
				bw.write("MPCM"); break;
			case Z64Sound.CODEC_S8:
				bw.write("HPCM"); break;
			case Z64Sound.CODEC_SMALL_ADPCM:
				bw.write("ADP5"); break;
			default:
				bw.write("UNK_" + entry.wave_info.getCodec()); break;
			}
			bw.write("\t");
			if(entry.wave_info.musicFlag()) bw.write("y\t");
			else bw.write("n\t");
			if(entry.wave_info.actorFlag()) bw.write("y\t");
			else bw.write("n\t");
			if(entry.wave_info.envFlag()) bw.write("y\t");
			else bw.write("n\t");
			if(entry.wave_info.sfxFlag()) bw.write("y\t");
			else bw.write("n\t");
			
			if(entry.tags != null && !entry.tags.isEmpty()){
				boolean first = true;
				for(String tag : entry.tags){
					if(!first) bw.write(";");
					bw.write(tag);
					first = false;
				}
				bw.write("\n");
			}
			else bw.write("\n");
		}
		
		bw.close();
		
		/*if(datdir != null){
			//Write waves
			for(Integer uid : uids){
				WaveTableEntry entry = entries.get(uid);
				String srcpath = datdir + File.separator + entry.getName() + ".buwav";
				String tpath = dirpath + File.separator + entry.getName() + ".wav";
				
				FileBuffer wavdat = FileBuffer.createBuffer(srcpath, true);
				Z64Wave wave = Z64Wave.readZ64Wave(wavdat, entry.getWaveInfo());
				wave.writeToWav(tpath);
			}
		}*/
	}
	
}
