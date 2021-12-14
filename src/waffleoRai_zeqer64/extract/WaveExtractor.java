package waffleoRai_zeqer64.extract;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileUtils;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_soundbank.nintendo.Z64Bank;
import waffleoRai_soundbank.nintendo.Z64Bank.WaveInfoBlock;
import waffleoRai_zeqer64.ZeqerCore;
import waffleoRai_zeqer64.ZeqerRom;
import waffleoRai_zeqer64.filefmt.NusRomInfo;
import waffleoRai_zeqer64.filefmt.ZeqerWaveTable;
import waffleoRai_zeqer64.filefmt.ZeqerWaveTable.WaveTableEntry;

public class WaveExtractor {
	
	/*----- Instance Variables -----*/
	
	private boolean tbl_w = false; //Write to wave_table file? Or just use for ref?
	
	private String dir_base; //wav dir
	private ZeqerWaveTable wave_table;
	
	private ZeqerRom z_rom;
	
	/*----- Init -----*/
	
	public WaveExtractor(ZeqerRom rom) throws IOException{
		this(ZeqerCore.getProgramDirectory() + File.separator + ZeqerCore.DIRNAME_WAVE, null, rom);
	}
	
	public WaveExtractor(String dir, ZeqerRom rom) throws IOException{
		this(dir, null, rom);
	}
	
	public WaveExtractor(String dir, ZeqerWaveTable table, ZeqerRom rom) throws IOException{
		dir_base = dir;
		if(!Files.isDirectory(Paths.get(dir_base))){
			Files.createDirectories(Paths.get(dir_base));
		}
		
		z_rom = rom;
		wave_table = table;
		
		if(wave_table == null){try {
			loadWaveTable();
		} catch (UnsupportedFileTypeException e) {
			e.printStackTrace();
			throw new IOException();
		}}
	}
	
	private void loadWaveTable() throws UnsupportedFileTypeException, IOException{
		String tblpath = getWaveTablePath();
		
		if(FileBuffer.fileExists(tblpath)){
			//Read that one
			wave_table = ZeqerWaveTable.readTable(FileBuffer.createBuffer(tblpath, true));
		}
		else{
			//New table
			wave_table = ZeqerWaveTable.createTable();
		}
	}
	
	/*----- Paths -----*/

	public String getWaveTablePath(){return dir_base + File.separator + ZeqerCore.FN_SYSWAVE;}
	
	public String getIDTablePath(){
		return dir_base + File.separator + "wav_" + z_rom.getRomInfo().getZeqerID() + ".bin";
	}
	
	/*----- Setters -----*/
	
	public void setWaveTableWriteEnabled(boolean b){tbl_w = b;}
	
	/*----- Extraction -----*/
	
	public static int[][][] buildWavePosTable(ZeqerRom rom) throws IOException{
		if(rom == null) return null;
		
		FileBuffer code = rom.loadCode();
		
		//Read code table for audiotable
		int[][] warc_tbl, bnk_tbl;
		code.setCurrentPosition(rom.getRomInfo().getCodeOffset_audtable());
		int warc_count = Short.toUnsignedInt(code.nextShort());
		code.skipBytes(14);
		warc_tbl = new int[warc_count][2];
		for(int i = 0; i < warc_count; i++){
			warc_tbl[i][0] = code.nextInt();
			warc_tbl[i][1] = code.nextInt();
			code.skipBytes(8);
		}
		
		//Ew. But it does the job.
		ArrayList<Map<Integer, Integer>> found = new ArrayList<Map<Integer,Integer>>(warc_count);
		for(int i = 0; i < warc_count; i++){found.add(new TreeMap<Integer, Integer>());}
		//Maps offset to length.
		
		//Read code table for audiobank
		code.setCurrentPosition(rom.getRomInfo().getCodeOffset_bnktable());
		int bcount = Short.toUnsignedInt(code.nextShort());
		code.skipBytes(14);
		bnk_tbl = new int[bcount][6];
		for(int i = 0; i < bcount; i++){
			bnk_tbl[i][0] = code.nextInt();
			bnk_tbl[i][1] = code.nextInt();
			code.skipBytes(2);
			bnk_tbl[i][2] = Byte.toUnsignedInt(code.nextByte());
			code.nextByte();
			bnk_tbl[i][3] = Byte.toUnsignedInt(code.nextByte());
			bnk_tbl[i][4] = Byte.toUnsignedInt(code.nextByte());
			bnk_tbl[i][5] = Short.toUnsignedInt(code.nextShort());
			
			//if(bnk_tbl[i][2] == 1) bnk_tbl[i][2] = 0; //Dunno why this is true, but it is. I think 1 refers to the musical samples maybe?
		}
		
		//Scan banks for wave references
		FileBuffer audiobank = rom.loadAudiobank();
		for(int i = 0; i < bcount; i++){
			FileBuffer mybank = audiobank.createReadOnlyCopy(bnk_tbl[i][0], bnk_tbl[i][0] + bnk_tbl[i][1]);
			Map<Integer, Integer> warcmap = found.get(bnk_tbl[i][2]);
			
			Z64Bank zbnk = Z64Bank.readBank(mybank, bnk_tbl[i][3], bnk_tbl[i][4], bnk_tbl[i][5]);
			List<WaveInfoBlock> winfos = zbnk.getAllWaveInfoBlocks();
			for(WaveInfoBlock winfo : winfos){
				if(!warcmap.containsKey(winfo.getOffset())){
					//System.err.println("New wave: 0x" + Integer.toHexString(winfo.getOffset()) + ", 0x" + Integer.toHexString(winfo.getLength()));
					warcmap.put(winfo.getOffset(), winfo.getLength());
				}
			}
			mybank.dispose();
		}
		
		int[][][] wave_table = new int[warc_count][][];
		for(int i = 0; i < warc_count; i++){
			//Don't forget to search for gaps too! (Gaps must be longer than 16 bytes to count as gaps)
			Map<Integer, Integer> warcmap = found.get(i);
			
			if(warcmap.isEmpty()){
				//Just put the whole thing as one "sample"
				int[][] mytbl = new int[1][3];
				wave_table[i] = mytbl;
				mytbl[0][0] = 0;
				mytbl[0][1] = warc_tbl[i][1];
				mytbl[0][2] = 0;
				continue;
			}
			
			List<Integer> offsets = new ArrayList<Integer>(warcmap.size()+1);
			offsets.addAll(warcmap.keySet());
			Collections.sort(offsets);
			int count = offsets.size();
			for(int j = 0; j < count; j++){
				int myoff = offsets.get(j);
				int nextoff = warc_tbl[i][1];
				if(j < count-1) nextoff = offsets.get(j+1);
				int mylen = warcmap.get(myoff);
				int myend = myoff + mylen;
				if(myend < nextoff){
					//Gap.
					if(!(i == 0 && j == count-1)){
						int diff = nextoff - myend;
						if(diff > 16){
							warcmap.put(myend, (nextoff - myend) | 0x80000000); //Hi bit marks as unused	
						}	
					}
				}
			}

			//Now allocate and copy to table.
			count = warcmap.size();
			int[][] mytbl = new int[count][3];
			wave_table[i] = mytbl;
			
			offsets = new ArrayList<Integer>(count+1);
			offsets.addAll(warcmap.keySet());
			Collections.sort(offsets);
			for(int j = 0; j < count; j++){
				int myoff = offsets.get(j);
				int mylen = warcmap.get(myoff);
				boolean used = (mylen & 0x80000000) == 0;
				mylen &= 0x7fffffff;
				mytbl[j][0] = myoff;
				mytbl[j][1] = mylen;
				mytbl[j][2] = used?1:0;
				System.err.println("wave_table[" + i + "][" + j + "] = " 
						+ "0x" + Integer.toHexString(mytbl[j][0])
						+ ":0x" + Integer.toHexString(mytbl[j][1])
						+ ":" + Integer.toHexString(mytbl[j][2]));
			}
			//System.err.println("\t\tWARC " + i + " Wave Count: " + count);
		}
		
		//[warc_idx][wav_idx][field]
		//Fields:
		//	0 - Offset (relative to warc)
		//	1 - Length
		//	2 - Is used? (0 or 1)
		
		return wave_table;
	}
	
	public boolean extractWaves() throws IOException{
		if(z_rom == null) return false;
		
		//We will need audiobank, audiotable, and code
		FileBuffer code = z_rom.loadCode(); code.setEndian(true);
		FileBuffer audiobank = z_rom.loadAudiobank(); audiobank.setEndian(true);
		FileBuffer audiotable = z_rom.loadAudiotable(); audiotable.setEndian(true);
		NusRomInfo rominfo = z_rom.getRomInfo();
		if(rominfo == null) return false;
		
		/*Special banks in OoT are:
		 * 	0 - Ocarina
		 * 	1 - Actor Sounds
		 * 	2 - Environment Sounds
		 * 
		 * Think it's the same in MM
		*/
		Map<Integer, WaveInfoBlock> waves_actor = new TreeMap<Integer, WaveInfoBlock>();
		Map<Integer, WaveInfoBlock> waves_env = new TreeMap<Integer, WaveInfoBlock>();
		Map<Integer, WaveInfoBlock> waves_music = new TreeMap<Integer, WaveInfoBlock>();
		
		//Read warc table from code file
		code.setCurrentPosition(rominfo.getCodeOffset_audtable());
		int warccount = Short.toUnsignedInt(code.nextShort());
		code.skipBytes(14);
		long[][] warcpos = new long[warccount][2];
		for(int i = 0; i < warccount; i++){
			warcpos[i][0] = Integer.toUnsignedLong(code.nextInt());
			warcpos[i][1] = Integer.toUnsignedLong(code.nextInt());
			code.skipBytes(8L);
		}
		
		//Read audio bank table from code file
		code.setCurrentPosition(rominfo.getCodeOffset_bnktable());
		int bcount = Short.toUnsignedInt(code.nextShort());
		code.skipBytes(14);
		for(int i = 0; i < bcount; i++){
			long stoff = Integer.toUnsignedLong(code.nextInt());
			long size = Integer.toUnsignedLong(code.nextInt());
			
			//Not sure what the next two bytes are
			code.skipBytes(2);
			int bwarc = Byte.toUnsignedInt(code.nextByte());
			code.nextByte(); //Unknown, usually 0xff
			
			int s0count = Byte.toUnsignedInt(code.nextByte());
			int s1count = Byte.toUnsignedInt(code.nextByte());
			int s2count = Short.toUnsignedInt(code.nextShort());
			
			FileBuffer bnkdat = audiobank.createReadOnlyCopy(stoff, stoff+size);
			Z64Bank zbnk = Z64Bank.readBank(bnkdat, s0count, s1count, s2count);
			
			//Get waves
			int wmask = bwarc << 24;
			List<WaveInfoBlock> wavelist = zbnk.getAllWaveInfoBlocks();
			for(WaveInfoBlock wb : wavelist){
				int modoff = wb.getOffset() | wmask;
				if(i == 1){
					waves_actor.put(modoff, wb);
				}
				else if(i == 2){
					if(waves_actor.containsKey(modoff)) continue;
					waves_env.put(modoff, wb);
				}
				else{
					if(waves_actor.containsKey(modoff)) continue;
					if(waves_env.containsKey(modoff)) continue;
					waves_music.put(modoff, wb);
				}
			}
			
			bnkdat.dispose();
		}
		
		//Now go through the waves. Check against existing, add to table and copy
		//	to dir if not there.
		//Remember to register to output offset -> zeqerUID table
		Map<Integer, Integer> uidmap = new TreeMap<Integer, Integer>();
		List<Integer> ilist = new LinkedList<Integer>();
		ilist.addAll(waves_actor.keySet());
		for(Integer k : ilist){
			WaveInfoBlock wb = waves_actor.get(k);
			int warc_idx = k >>> 24;
			long offset = wb.getOffset() + warcpos[warc_idx][0];
			
			FileBuffer wavedat = audiotable.createReadOnlyCopy(offset, offset + wb.getLength());
			
			//Hash.
			byte[] md5 = FileUtils.getMD5Sum(wavedat.getBytes());
			
			//See if this wave already exists in database
			String md5str = FileUtils.bytes2str(md5).toLowerCase();
			WaveTableEntry entry = wave_table.getEntryWithSum(md5str);
			if(!tbl_w){
				System.err.println("WaveExtractor.extractWaves || WARNING: Table write disabled! Unrecognized sounds will be ignored!");
			}
			
			if(entry == null){
				//Need new entry. Also don't forget to copy actual wave file.
				if(tbl_w){
					entry = wave_table.addEntryFromBankBlock(wb, md5);
					entry.setFlags(ZeqerWaveTable.FLAG_ISACTOR);
					int uid = entry.getUID();
					entry.setName("actor_snd_" + Integer.toHexString(uid));
					String wpath = dir_base + File.separator + String.format("%08x", uid) + ".adpcm";
					wavedat.writeFile(wpath);
					uidmap.put(k, entry.getUID());	
				}
			}
			else{
				//Just map to uidmap then.
				System.err.println("Wave match found! 0x" + Integer.toHexString(k) + " to " + entry.getName());
				String wpath = dir_base + File.separator + String.format("%08x", entry.getUID()) + ".adpcm";
				if(!FileBuffer.fileExists(wpath)){
					wavedat.writeFile(wpath);
				}
				uidmap.put(k, entry.getUID());
			}
			
			wavedat.dispose();
		}
		//Repeat with other maps
		ilist.clear();
		ilist.addAll(waves_env.keySet());
		for(Integer k : ilist){
			WaveInfoBlock wb = waves_env.get(k);
			int warc_idx = k >>> 24;
			long offset = wb.getOffset() + warcpos[warc_idx][0];
			
			FileBuffer wavedat = audiotable.createReadOnlyCopy(offset, offset + wb.getLength());
			byte[] md5 = FileUtils.getMD5Sum(wavedat.getBytes());
			
			//See if this wave already exists in database
			String md5str = FileUtils.bytes2str(md5).toLowerCase();
			WaveTableEntry entry = wave_table.getEntryWithSum(md5str);
			
			if(entry == null){
				//Need new entry.
				if(tbl_w){
					entry = wave_table.addEntryFromBankBlock(wb, md5);
					entry.setFlags(ZeqerWaveTable.FLAG_ISENV);
					int uid = entry.getUID();
					entry.setName("env_snd_" + Integer.toHexString(uid));
					String wpath = dir_base + File.separator + String.format("%08x", uid) + ".adpcm";
					wavedat.writeFile(wpath);
					uidmap.put(k, entry.getUID());
				}
			}
			else{
				//Just map to uidmap then.
				System.err.println("Wave match found! 0x" + Integer.toHexString(wb.getOffset()) + " to " + entry.getName());
				String wpath = dir_base + File.separator + String.format("%08x", entry.getUID()) + ".adpcm";
				if(!FileBuffer.fileExists(wpath)){
					wavedat.writeFile(wpath);
				}
				uidmap.put(k, entry.getUID());
			}
			wavedat.dispose();
		}
		
		ilist.clear();
		ilist.addAll(waves_music.keySet());
		for(Integer k : ilist){
			WaveInfoBlock wb = waves_music.get(k);
			int warc_idx = k >>> 24;
			long offset = wb.getOffset() + warcpos[warc_idx][0];
			
			FileBuffer wavedat = audiotable.createReadOnlyCopy(offset, offset + wb.getLength());
			byte[] md5 = FileUtils.getMD5Sum(wavedat.getBytes());
			
			//See if this wave already exists in database
			String md5str = FileUtils.bytes2str(md5).toLowerCase();
			WaveTableEntry entry = wave_table.getEntryWithSum(md5str);
			
			if(entry == null){
				//Need new entry.
				if(tbl_w){
					entry = wave_table.addEntryFromBankBlock(wb, md5);
					entry.setFlags(ZeqerWaveTable.FLAG_ISMUSIC);
					int uid = entry.getUID();
					entry.setName("music_snd_" + Integer.toHexString(uid));
					String wpath = dir_base + File.separator + String.format("%08x", uid) + ".adpcm";
					wavedat.writeFile(wpath);
					uidmap.put(k, entry.getUID());
				}
			}
			else{
				System.err.println("Wave match found! 0x" + Integer.toHexString(wb.getOffset()) + " to " + entry.getName());
				String wpath = dir_base + File.separator + String.format("%08x", entry.getUID()) + ".adpcm";
				if(!FileBuffer.fileExists(wpath)){
					wavedat.writeFile(wpath);
				}
				uidmap.put(k, entry.getUID());
			}
			wavedat.dispose();
		}
		
		//Write wave table & offset/UID table to folder
		if(tbl_w){
			String wtpath = getWaveTablePath();
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(wtpath));
			wave_table.writeTo(bos);
			bos.close();	
		}
		
		if(!uidmap.isEmpty()){
			String idtbl_path = dir_base + File.separator + "wav_" + rominfo.getZeqerID() + ".bin";
			int count = uidmap.size();
			FileBuffer idtbl = new FileBuffer((count+1) << 3, true);
			ilist.clear();
			ilist.addAll(uidmap.keySet());
			Collections.sort(ilist);
			for(int off : ilist){
				int uid = uidmap.get(off);
				idtbl.addToFile(uid);
				idtbl.addToFile(off);
			}
			idtbl.writeFile(idtbl_path);
		}
		
		return true;
	}
	
}
