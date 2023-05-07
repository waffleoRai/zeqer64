package waffleoRai_zeqer64;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import waffleoRai_Sound.nintendo.Z64Wave;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileUtils;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_zeqer64.SoundTables.WaveArcInfoEntry;
import waffleoRai_zeqer64.extract.RomExtractionSummary;
import waffleoRai_zeqer64.extract.RomExtractionSummary.ExtractionError;
import waffleoRai_zeqer64.extract.WaveExtractor;
import waffleoRai_zeqer64.extract.WaveLocIDMap;
import waffleoRai_zeqer64.filefmt.NusRomInfo;
import waffleoRai_zeqer64.filefmt.UltraWavFile;
import waffleoRai_zeqer64.filefmt.VersionWaveTable;
import waffleoRai_zeqer64.filefmt.ZeqerWaveTable;
import waffleoRai_zeqer64.filefmt.ZeqerWaveTable.WaveTableEntry;

class CoreWaveManager {

	private static final char SEP = File.separatorChar;
	
	/*----- Instance Variables -----*/
	
	private String root_dir; //Base wav dir
	private boolean sys_write_enabled = false;
	
	private ZeqerWaveTable wav_table_sys;
	private ZeqerWaveTable wav_table_user;
	
	/*----- Init -----*/
	
	public CoreWaveManager(String base_wav_dir, boolean sysMode) throws UnsupportedFileTypeException, IOException{
		root_dir = base_wav_dir;
		sys_write_enabled = sysMode;
		
		if(sys_write_enabled){
			//Create directories, if not there.
			String zdir = root_dir + SEP + ZeqerCore.DIRNAME_ZWAVE;
			if(!FileBuffer.directoryExists(zdir)){
				//This also automatically creates the outer wavs dir too.
				Files.createDirectories(Paths.get(zdir));
			}
		}
		
		String tblpath = getSysTablePath();
		if(FileBuffer.fileExists(tblpath)){
			wav_table_sys = ZeqerWaveTable.readTable(FileBuffer.createBuffer(tblpath, true));
		}
		else{
			wav_table_sys = ZeqerWaveTable.createTable();
		}
	
		tblpath = getUserTablePath();
		if(FileBuffer.fileExists(tblpath)){
			wav_table_user = ZeqerWaveTable.readTable(FileBuffer.createBuffer(tblpath, true));
		}
		else{
			if(!sys_write_enabled){
				wav_table_user = ZeqerWaveTable.createTable();	
			}
		}
	}
	
	/*----- Getters -----*/
	
	public String getSysDirPath(){
		return root_dir + SEP + ZeqerCore.DIRNAME_ZWAVE;
	}
	
	public String getSysTablePath(){
		return root_dir + SEP + ZeqerCore.DIRNAME_ZWAVE + SEP + ZeqerCore.FN_SYSWAVE;
	}
	
	public String getUserTablePath(){
		return root_dir + SEP + ZeqerCore.FN_USRWAVE;
	}
	
	public String getWavDataFilePath(int uid){
		if(isWavSys(uid)){
			return getSysTablePath() + SEP + String.format("%08x.buwav", uid);
		}
		else{
			return getUserTablePath() + SEP + String.format("%08x.buwav", uid);	
		}
	}
	
	public boolean isWavSys(int uid){
		if(wav_table_sys != null){
			return wav_table_sys.getEntryWithUID(uid) != null;
		}
		return false;
	}
	
	public ZeqerWaveTable getZeqerWaveTable(){return wav_table_sys;}
	
	public ZeqerWaveTable getUserWaveTable(){
		if(wav_table_user == null) wav_table_user = ZeqerWaveTable.createTable();
		return wav_table_user;
	}
	
	public WaveTableEntry getWaveTableEntry(int uid){
		WaveTableEntry entry = null;
		if(wav_table_sys != null) entry = wav_table_sys.getEntryWithUID(uid);
		if(entry == null && wav_table_user != null) {
			entry = wav_table_user.getEntryWithUID(uid);
		}
		return entry;
	}

	public Z64WaveInfo getWaveByName(String wave_name){
		String wdir = null;
		WaveTableEntry entry = null;
		if(wav_table_sys != null){
			entry = wav_table_sys.getEntryWithName(wave_name);
			if(entry != null) wdir = root_dir + SEP + ZeqerCore.DIRNAME_ZWAVE;
		}
		if(entry == null){
			if(wav_table_user == null) return null;
			entry = wav_table_user.getEntryWithName(wave_name);
			if(entry != null) wdir = root_dir;
			else return null;
		}
		
		Z64WaveInfo winfo = entry.getWaveInfo();
		if(winfo == null) return null;
		
		if(winfo.getWaveSize() <= 0){
			//Not loaded.
			String wpath = wdir + File.separator + entry.getDataFileName();
			try{
				UltraWavFile uwav = UltraWavFile.createUWAV(wpath);
				uwav.readWaveInfo(winfo);
			}
			catch(Exception ex){
				System.err.println("ZeqerCore.getWaveByName || Failed to load " + wpath);
				ex.printStackTrace();
				return null;
			}
		}
		
		return winfo;
	}
	
	public Z64WaveInfo getWaveInfo(int wave_uid){
		String wdir = null;
		WaveTableEntry entry = null;
		if(wav_table_sys != null){
			entry = wav_table_sys.getEntryWithUID(wave_uid);
			if(entry != null) wdir = root_dir + SEP + ZeqerCore.DIRNAME_ZWAVE;
		}
		if(entry == null){
			if(wav_table_user == null) return null;
			entry = wav_table_user.getEntryWithUID(wave_uid);
			if(entry != null) wdir = root_dir;
			else return null;
		}
		
		Z64WaveInfo winfo = entry.getWaveInfo();
		if(winfo == null) return null;
		
		if(winfo.getWaveSize() <= 0){
			//Not loaded.
			String wpath = wdir + File.separator + entry.getDataFileName();
			try{
				UltraWavFile uwav = UltraWavFile.createUWAV(wpath);
				uwav.readWaveInfo(winfo);
			}
			catch(Exception ex){
				System.err.println("ZeqerCore.getWaveInfo || Failed to load " + wpath);
				ex.printStackTrace();
				return null;
			}
		}
		
		return winfo;
	}
	
	public Z64Wave loadWave(int wave_uid){
		String wdir = null;
		WaveTableEntry entry = null;
		if(wav_table_sys != null){
			entry = wav_table_sys.getEntryWithUID(wave_uid);
			if(entry != null) wdir = root_dir + SEP + ZeqerCore.DIRNAME_ZWAVE;
		}
		if(entry == null){
			if(wav_table_user == null) return null;
			entry = wav_table_user.getEntryWithUID(wave_uid);
			if(entry != null) wdir = root_dir;
			else return null;
		}
		
		Z64WaveInfo winfo = entry.getWaveInfo();
		if(winfo == null) return null;
		
		String wpath = wdir + File.separator + entry.getDataFileName();
		FileBuffer sounddat = null;
		try{
			UltraWavFile uwav = UltraWavFile.createUWAV(wpath);
			if(winfo.getWaveSize() <= 0){
				//Info has not been loaded
				uwav.readWaveInfo(winfo);
			}
			sounddat = uwav.loadSoundData();
		}
		catch(Exception ex){
			System.err.println("ZeqerCore.getWaveInfo || Failed to load " + wpath);
			ex.printStackTrace();
			return null;
		}
		if(sounddat == null) return null;
		
		return Z64Wave.readZ64Wave(sounddat, winfo);
	}
	
	public FileBuffer loadWaveData(int wave_uid){
		String wdir = null;
		WaveTableEntry entry = null;
		if(wav_table_sys != null){
			entry = wav_table_sys.getEntryWithUID(wave_uid);
			if(entry != null) wdir = root_dir + SEP + ZeqerCore.DIRNAME_ZWAVE;
		}
		if(entry == null){
			if(wav_table_user == null) return null;
			entry = wav_table_user.getEntryWithUID(wave_uid);
			if(entry != null) wdir = root_dir;
			else return null;
		}
		
		Z64WaveInfo winfo = entry.getWaveInfo();
		if(winfo == null) return null;
		
		String wpath = wdir + File.separator + entry.getDataFileName();
		FileBuffer sounddat = null;
		try{
			UltraWavFile uwav = UltraWavFile.createUWAV(wpath);
			if(winfo.getWaveSize() <= 0){
				//Info has not been loaded
				uwav.readWaveInfo(winfo);
			}
			sounddat = uwav.loadSoundData();
		}
		catch(Exception ex){
			System.err.println("ZeqerCore.getWaveInfo || Failed to load " + wpath);
			ex.printStackTrace();
			return null;
		}
		return sounddat;
	}
	
	public VersionWaveTable loadWaveVersionTable(String rom_id) throws IOException{
		if(wav_table_sys == null) return null;
		String wavdir = root_dir + SEP + ZeqerCore.DIRNAME_ZWAVE;
		return ZeqerWaveTable.loadVersionTable(wavdir, rom_id);
	}
	
	public WaveLocIDMap loadVersionWaveOffsetIDMap(String rom_id) throws IOException{
		if(wav_table_sys == null) return null;
		String wavdir = root_dir + SEP + ZeqerCore.DIRNAME_ZWAVE;
		return ZeqerWaveTable.loadVersionWaveOffsetIDMap(wavdir, rom_id);
	}
	
	public List<WaveTableEntry> getAllValidTableEntries(){
		//Only return entries where there is a matching uwav file
		List<WaveTableEntry> list = new LinkedList<WaveTableEntry>();
		if(wav_table_user != null){
			List<WaveTableEntry> alle = wav_table_user.getAllEntries();
			for(WaveTableEntry e : alle){
				String datpath = root_dir + SEP + e.getDataFileName();
				if(FileBuffer.fileExists(datpath)){
					//Load remaining metadata, if needed
					if(e.getWaveInfo().getWaveSize() < 1){
						try{
							UltraWavFile uwav = UltraWavFile.createUWAV(datpath);
							uwav.readWaveInfo(e.getWaveInfo());
						}
						catch(IOException | UnsupportedFileTypeException ex){
							ex.printStackTrace();
							continue;
						}
					}
					list.add(e);
				}
			}
		}
		if(wav_table_sys != null){
			List<WaveTableEntry> alle = wav_table_sys.getAllEntries();
			for(WaveTableEntry e : alle){
				String datpath = root_dir + SEP + ZeqerCore.DIRNAME_ZWAVE + SEP + e.getDataFileName();
				if(FileBuffer.fileExists(datpath)){
					if(e.getWaveInfo().getWaveSize() < 1){
						try{
							UltraWavFile uwav = UltraWavFile.createUWAV(datpath);
							uwav.readWaveInfo(e.getWaveInfo());
						}
						catch(IOException | UnsupportedFileTypeException ex){
							ex.printStackTrace();
							continue;
						}
					}
					list.add(e);
				}
			}
		}
		return list;
	}
	
	/*----- Setters -----*/
	
	public boolean importROMWaves(ZeqerRom z_rom, RomExtractionSummary errorInfo) throws IOException{
		if(z_rom == null) return false;
	
		NusRomInfo rominfo = z_rom.getRomInfo();
		if(rominfo == null) return false;
		
		if(errorInfo != null){
			errorInfo.sampleBanksFound = 0;
			errorInfo.samplesAddedAsCustom = 0;
			errorInfo.samplesFound = 0;
			errorInfo.samplesOkay = 0;
			errorInfo.samplesNew = 0;
		}
		
		FileBuffer audiotable = z_rom.loadAudiotable(); audiotable.setEndian(true);
		
		//Read warc and bank tables from code file
		WaveArcInfoEntry[] warc_tbl = z_rom.loadWaveArcEntries();
		int war_count = warc_tbl.length;
		
		//Read wave info blocks off the ROM.
		//This requires the soundfont data, so I'm leaving that mess in WaveExtractor
		Z64WaveInfo[][] waves = WaveExtractor.buildWaveTable(z_rom);
		VersionWaveTable vwt = new VersionWaveTable(war_count);
		
		for(int i = 0; i < war_count; i++){
			Z64WaveInfo[] wav_list = waves[i];
			if(wav_list == null){
				//ref?
				if(warc_tbl[i].getSize() <= 0){
					vwt.setWarcAsReference(i, warc_tbl[i].getOffset());
				}
				continue;
			}
			if(errorInfo != null) errorInfo.sampleBanksFound++;
			
			int wav_count = wav_list.length;
			if(wav_count < 1) continue;
			vwt.allocWarc(i, wav_count);
			for(int j = 0; j < wav_count; j++){
				Z64WaveInfo winfo = wav_list[j];
				if(!winfo.usedFlag()) continue;
				if(errorInfo != null) errorInfo.samplesFound++;
				
				//Hash
				long offset = (long)warc_tbl[i].getOffset() + (long)winfo.getWaveOffset();
				FileBuffer wavedat = audiotable.createReadOnlyCopy(offset, offset + winfo.getWaveSize());
				byte[] md5 = FileUtils.getMD5Sum(wavedat.getBytes());
				
				//See if this wave already exists in database
				String md5str = FileUtils.bytes2str(md5).toLowerCase();
				WaveTableEntry entry = wav_table_sys.getEntryWithSum(md5str);
				
				if(entry == null){
					//Need new entry. Also don't forget to copy actual wave file.
					if(sys_write_enabled){
						entry = wav_table_sys.addEntryFromInfoBlock(winfo, md5);
						String wpath = getSysDirPath() + SEP + entry.getDataFileName();
						UltraWavFile.writeUWAV(wpath, winfo, wavedat);
						if(errorInfo != null){
							errorInfo.samplesOkay++;
							errorInfo.samplesNew++;
						}
						vwt.setSampleInfo(i, j, entry.getUID(), winfo.getWaveOffset());
					}
					else{
						//Add as user wave.
						if(wav_table_user != null){
							entry = wav_table_user.addEntryFromInfoBlock(winfo, md5);
							String wpath = root_dir + SEP + entry.getDataFileName();
							UltraWavFile.writeUWAV(wpath, winfo, wavedat);
							if(errorInfo != null) errorInfo.samplesAddedAsCustom++;
							vwt.setSampleInfo(i, j, entry.getUID(), winfo.getWaveOffset());
						}
						else{
							if(errorInfo != null){
								if(errorInfo.sampleErrors == null){
									errorInfo.sampleErrors = new LinkedList<ExtractionError>();
									ExtractionError err = new ExtractionError();
									err.index0 = i;
									err.index1 = j;
									err.itemType = RomExtractionSummary.ITEM_TYPE_WAV;
									err.reason = RomExtractionSummary.ERROR_NOT_IN_TABLE;
									err.itemUID = ZeqerUtils.md52UID(md5);
									errorInfo.sampleErrors.add(err);
								}
							}
						}
					}
				}
				else{
					//Just map to uidmap then.
					//System.err.println("Wave match found! 0x" + Integer.toHexString((int)offset) + " to " + entry.getName());
					winfo.setUID(entry.getUID());
					String wpath = getSysDirPath() + SEP + entry.getDataFileName();
					if(!FileBuffer.fileExists(wpath)){
						UltraWavFile.writeUWAV(wpath, winfo, wavedat);
					}
					if(errorInfo != null) errorInfo.samplesOkay++;
					vwt.setSampleInfo(i, j, entry.getUID(), winfo.getWaveOffset());
				}
				
				//Toss data ref
				wavedat.dispose();
			}
		}
		
		vwt.writeTo(root_dir + SEP + ZeqerCore.DIRNAME_ZWAVE + SEP + "wav_" + rominfo.getZeqerID() + ".bin");
		saveTables();
		return true;
	}
	
	public int addUserWave(Z64WaveInfo metadata, FileBuffer sounddata) throws IOException{
		//Returns UID
		if(wav_table_user == null) return -1;
		if(metadata == null) return -1;
		if(sounddata == null) return -1;
		
		byte[] soundbytes = sounddata.getBytes(0, sounddata.getFileSize());
		byte[] md5 = FileUtils.getMD5Sum(soundbytes);
		soundbytes = null;
		
		//Is this one already in sys or user tables?
		String md5str = FileUtils.bytes2str(md5).toLowerCase();
		WaveTableEntry entry = wav_table_sys.getEntryWithSum(md5str);
		if(entry != null) return entry.getUID();
		entry = wav_table_user.getEntryWithSum(md5str);
		if(entry != null) return entry.getUID();
		
		//Add if not.
		entry = wav_table_user.addEntryFromInfoBlock(metadata, md5);
		String wpath = root_dir + SEP + entry.getDataFileName();
		UltraWavFile.writeUWAV(wpath, metadata, sounddata);
		
		saveUserTable();
		return entry.getUID();
	}
	
	public boolean deleteWave(int uid) throws IOException{
		//If sys write not enabled, will refuse to delete if sys
		if(wav_table_user != null){
			WaveTableEntry entry = wav_table_user.removeEntryWithUID(uid);
			String path = root_dir + SEP + entry.getDataFileName();
			if(FileBuffer.fileExists(path)){
				Files.delete(Paths.get(path));
			}
			if(entry != null) return true;
		}
		if(!sys_write_enabled) return false;
		WaveTableEntry entry = wav_table_sys.removeEntryWithUID(uid);
		String path = root_dir + SEP + ZeqerCore.DIRNAME_ZWAVE + SEP + entry.getDataFileName();
		if(FileBuffer.fileExists(path)){
			Files.delete(Paths.get(path));
		}
		
		return (entry != null);
	}
	
	/*----- I/O -----*/
	
	public int importWaveMetaTSV(String tsv_path) throws IOException{
		int records = 0;
		if(wav_table_user != null) records += wav_table_user.importTSV(tsv_path);
		if(sys_write_enabled && wav_table_sys != null) records += wav_table_sys.importTSV(tsv_path);
		return records;
	}
	
	public void saveSysTable() throws IOException{
		if(sys_write_enabled && wav_table_sys != null){
			wav_table_sys.writeTo(getSysTablePath());
		}
	}
	
	public void saveUserTable() throws IOException{
		if(wav_table_user != null){
			wav_table_user.writeTo(getUserTablePath());
		}
	}
	
	public void saveTables() throws IOException{
		if(sys_write_enabled && wav_table_sys != null){
			wav_table_sys.writeTo(getSysTablePath());
		}
		if(wav_table_user != null){
			wav_table_user.writeTo(getUserTablePath());
		}
	}
	
}
