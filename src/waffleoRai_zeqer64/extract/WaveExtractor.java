package waffleoRai_zeqer64.extract;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import waffleoRai_Sound.nintendo.N64ADPCMTable;
import waffleoRai_Sound.nintendo.Z64Wave;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileUtils;
import waffleoRai_soundbank.nintendo.z64.Z64Bank;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_zeqer64.ZeqerCore;
import waffleoRai_zeqer64.ZeqerRom;
import waffleoRai_zeqer64.SoundTables.BankInfoEntry;
import waffleoRai_zeqer64.SoundTables.WaveArcInfoEntry;
import waffleoRai_zeqer64.filefmt.NusRomInfo;
import waffleoRai_zeqer64.filefmt.RomInfoNode;
import waffleoRai_zeqer64.filefmt.UltraWavFile;
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
		this(ZeqerCore.getActiveCore().getProgramDirectory() + File.separator + ZeqerCore.DIRNAME_WAVE, null, rom);
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
	
	public static Z64WaveInfo[][] buildWaveTable(ZeqerRom rom) throws IOException{
		if(rom == null) return null;
		
		//Load Tables.
		WaveArcInfoEntry[] warc_tbl = rom.loadWaveArcEntries();
		BankInfoEntry[] bnk_tbl = rom.loadBankEntries();
		int war_count = warc_tbl.length;
		int bnk_count = bnk_tbl.length;
		
		ArrayList<Map<Integer, Z64WaveInfo>> found = new ArrayList<Map<Integer,Z64WaveInfo>>(war_count);
		for(int i = 0; i < war_count; i++){found.add(new TreeMap<Integer, Z64WaveInfo>());}
		boolean[] warc_valid = new boolean[war_count];
		Arrays.fill(warc_valid, true);
		
		FileBuffer f_audiobank = rom.loadAudiobank();
		for(int i = 0; i < bnk_count; i++){
			//System.err.println("DEBUG Reading bank " + i + " --");
			//Majora's last bank (40) appears to be garbage
			//Linked to warc 0 in the table, though it should be warc 2
			//Thus, reading it yields nonsense wave offsets, which buggers up the indexing
			if(rom.getRomInfo().isZ6() && i == 40) continue;
			
			BankInfoEntry e = bnk_tbl[i];
			if(e == null) continue;
			FileBuffer bnkdat = f_audiobank.createReadOnlyCopy(e.getOffset(), e.getOffset() + e.getSize());
			Z64Bank bnk = Z64Bank.readBank(bnkdat, e.getInstrumentCount(), e.getPercussionCount(), e.getSFXCount());
			bnkdat.dispose();
			
			int w_idx = e.getPrimaryWaveArcIndex();
			//Check if that warc is valid. If not, look for nearest.
			WaveArcInfoEntry we = warc_tbl[w_idx];
			while(we.getSize() <= 0){
				if(w_idx <= 0) break;
				warc_valid[w_idx] = false;
				w_idx--;
				we = warc_tbl[w_idx];
			}
			
			//System.err.println("DEBUG Using WArc " + w_idx + " --");
			Map<Integer, Z64WaveInfo> wmap = found.get(w_idx);
			List<Z64WaveInfo> winfo_list = bnk.getAllWaveInfoBlocks();
			for(Z64WaveInfo winfo : winfo_list){
				int key = winfo.getWaveOffset();
				if(!wmap.containsKey(key)){
					wmap.put(key, winfo);
					//Flags
					if(i == 0){
						if(winfo.sfxUseFlag()) winfo.flagAsActor(true);
						else winfo.flagAsSFX(true);
					}
					if(i == 1) winfo.flagAsActor(true);
					if(i == 2) winfo.flagAsEnv(true);
					else winfo.flagAsMusic(true);
				}
			}
		}
		
		//Fill in gaps & copy to output
		Z64WaveInfo[][] wav_tbl = new Z64WaveInfo[war_count][];
		for(int i = 0; i < war_count; i++){
			if(!warc_valid[i]) continue;
			Map<Integer, Z64WaveInfo> wmap = found.get(i);
			if(wmap.isEmpty()) continue;
			
			List<Integer> keylist = new ArrayList<Integer>(wmap.size()+16);
			keylist.addAll(wmap.keySet());
			Collections.sort(keylist);
			
			int added = 0;
			int lastend = 0;
			for(Integer k : keylist){
				Z64WaveInfo winfo = wmap.get(k);
				int diff = winfo.getWaveOffset() - lastend;
				if(diff > 16){
					//Gap.
					added++;
					Z64WaveInfo gap = new Z64WaveInfo();
					gap.setADPCMBook(N64ADPCMTable.getDefaultTable());
					int noff = (lastend%16 == 0)?lastend:(lastend & ~0xf) + 0x10;
					gap.setWaveOffset(noff);
					gap.setWaveSize(winfo.getWaveOffset() - noff);
					gap.flagUsed(false);
					wmap.put(noff, gap);
				}
				lastend = winfo.getWaveOffset() + winfo.getWaveSize();
			}
			
			if(added > 0){
				//Rebuild key list.
				keylist.clear();
				keylist.addAll(wmap.keySet());
				Collections.sort(keylist);
			}
			
			//Now add to table.
			int wav_count = keylist.size();
			wav_tbl[i] = new Z64WaveInfo[wav_count];
			Z64WaveInfo[] wavs = wav_tbl[i];
			int j = 0;
			for(Integer k : keylist){
				wavs[j++] = wmap.get(k);
			}
		}
		
		return wav_tbl;
	}

	public boolean extractWaves() throws IOException{
		if(z_rom == null) return false;
		
		//We will need audiobank, audiotable, and code
		//FileBuffer code = z_rom.loadCode(); code.setEndian(true);
		//FileBuffer audiobank = z_rom.loadAudiobank(); audiobank.setEndian(true);
		FileBuffer audiotable = z_rom.loadAudiotable(); audiotable.setEndian(true);
		NusRomInfo rominfo = z_rom.getRomInfo();
		if(rominfo == null) return false;
		
		/*Special banks in OoT are:
		 * 	0 - General SFX (inst/perc) + Actor Sounds (SFX)
		 * 	1 - Actor Sounds
		 * 	2 - Environment Sounds
		 * 
		 * Think it's the same in MM
		*/
		
		//Read warc and bank tables from code file
		WaveArcInfoEntry[] warc_tbl = z_rom.loadWaveArcEntries();
		int war_count = warc_tbl.length;
		
		//Read wave info blocks off the ROM.
		Z64WaveInfo[][] waves = buildWaveTable(z_rom);
		
		//Now go through the waves. Check against existing, add to table and copy
		//	to dir if not there.
		//Remember to register to output offset -> zeqerUID table
		if(!tbl_w){
			System.err.println("WaveExtractor.extractWaves || WARNING: Table write disabled! Unrecognized sounds will be ignored!");
		}
		//Map<Integer, Integer> uidmap = new TreeMap<Integer, Integer>();
		for(int i = 0; i < war_count; i++){
			Z64WaveInfo[] wav_list = waves[i];
			if(wav_list == null) continue;
			
			int wav_count = wav_list.length;
			if(wav_count < 1) continue;
			for(int j = 0; j < wav_count; j++){
				Z64WaveInfo winfo = wav_list[j];
				if(!winfo.usedFlag()) continue;
				
				//Hash
				long offset = (long)warc_tbl[i].getOffset() + (long)winfo.getWaveOffset();
				FileBuffer wavedat = audiotable.createReadOnlyCopy(offset, offset + winfo.getWaveSize());
				byte[] md5 = FileUtils.getMD5Sum(wavedat.getBytes());
				
				//See if this wave already exists in database
				String md5str = FileUtils.bytes2str(md5).toLowerCase();
				WaveTableEntry entry = wave_table.getEntryWithSum(md5str);
				
				if(entry == null){
					//Need new entry. Also don't forget to copy actual wave file.
					if(tbl_w){
						entry = wave_table.addEntryFromInfoBlock(winfo, md5);
						String wpath = dir_base + File.separator + entry.getDataFileName();
						UltraWavFile.writeUWAV(wpath, winfo, wavedat);
						//wavedat.writeFile(wpath);
						//uidmap.put((int)offset, entry.getUID());
					}
				}
				else{
					//Just map to uidmap then.
					System.err.println("Wave match found! 0x" + Integer.toHexString((int)offset) + " to " + entry.getName());
					winfo.setUID(entry.getUID());
					String wpath = dir_base + File.separator + entry.getDataFileName();
					if(!FileBuffer.fileExists(wpath)){
						//wavedat.writeFile(wpath);
						UltraWavFile.writeUWAV(wpath, winfo, wavedat);
					}
					//uidmap.put((int)offset, entry.getUID());
				}
				
				//Toss data ref
				wavedat.dispose();
			}
			
		}
			
		//Write wave table & offset/UID table to folder
		if(tbl_w){
			String wtpath = getWaveTablePath();
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(wtpath));
			wave_table.writeTo(bos);
			bos.close();	
		}
		
		//Version UID map
		if(waves != null){
			String idtbl_path = dir_base + File.separator + "wav_" + rominfo.getZeqerID() + ".bin";
			int count = 0;
			for(int i = 0; i < war_count; i++){
				Z64WaveInfo[] wav_list = waves[i];
				if(wav_list != null){
					count += wav_list.length;
				}
			}
			FileBuffer idtbl = new FileBuffer((count+war_count+4) << 3, true);

			idtbl.addToFile((short)war_count);
			for(int i = 0; i < war_count; i++){
				Z64WaveInfo[] wav_list = waves[i];
				if(wav_list != null){
					idtbl.addToFile((short)0);
					idtbl.addToFile((short)wav_list.length);
					for(int j = 0; j < wav_list.length; j++){
						Z64WaveInfo winfo = wav_list[j];
						idtbl.addToFile(winfo.getUID());
						idtbl.addToFile(winfo.getWaveOffset());
					}
				}
				else{
					idtbl.addToFile((short)0x1);
					idtbl.addToFile((short)(i-1));
				}
			}
			
			idtbl.writeFile(idtbl_path);
		}
		
		return true;
	}
	
	/*----- Debug -----*/
	
	public static void main(String[] args){
		try{
			String inpath = args[0];
			String outpath = args[1];
			
			System.err.println("inpath = " + inpath);
			System.err.println("outpath = " + outpath);
			
			RomInfoNode rin = ZeqerCore.getActiveCore().detectROM(inpath);
			if(rin == null){
				System.err.println("ROM was not recognized: " + inpath);
				System.exit(1);
			}
			if(!(rin instanceof NusRomInfo)){
				//I'll probably fix this eventually, but I'm lazy
				System.err.println("ROM recognized, but not N64 ROM: " + inpath);
				System.err.println("Please use direct N64 ROMs for this tool.");
				System.exit(1);
			}
			System.err.println("N64 ROM Detected: " + rin.getROMName());
			NusRomInfo rinfo = (NusRomInfo)rin;
			ZeqerRom rom = new ZeqerRom(inpath, rinfo);
			
			Z64WaveInfo[][] wave_tbl = buildWaveTable(rom);
			
			WaveArcInfoEntry[] warc_tbl = rom.loadWaveArcEntries();
			FileBuffer audiotable = rom.loadAudiotable();
			
			BufferedWriter bw = new BufferedWriter(new FileWriter(outpath + File.separator + "samples.csv"));
			bw.write("#BANK,SAMPLE,CODEC,OFFSET,LENGTH\n");
			for(int i = 0; i < warc_tbl.length; i++){
				if(wave_tbl[i] == null) continue;
				String wdir = outpath + File.separator + "warc_" + String.format("%02d", i);
				if(!FileBuffer.directoryExists(wdir)){
					Files.createDirectories(Paths.get(wdir));
				}
				Z64WaveInfo[] waves = wave_tbl[i];
				int base_off = warc_tbl[i].getOffset();
				for(int j = 0; j < waves.length; j++){
					String wpath = wdir + File.separator + "wave_" + String.format("%02d_%03d.wav", i, j);
					int woff = base_off + waves[j].getWaveOffset();
					int wlen = waves[j].getWaveSize();
					FileBuffer wdat = audiotable.createReadOnlyCopy(woff, woff+wlen);
					try{
					Z64Wave zwave = Z64Wave.readZ64Wave(wdat, waves[j]);
					zwave.writeToWav(wpath);}
					catch(Exception e){
						System.err.println("Failed to convert sample " + i + "-" + j);
						System.err.println("Skipping wav conversion...");
					}
					wdat.dispose();
					bw.write(i + "," + j + ",");
					bw.write(waves[j].getCodec() + ",");
					bw.write("0x" + Integer.toHexString(waves[j].getWaveOffset()) + ",");
					bw.write("0x" + Integer.toHexString(waves[j].getWaveSize()) + "\n");
				}
			}
			bw.close();
		}
		catch(Exception ex){
			ex.printStackTrace();
			System.exit(1);
		}
	}
	
}
