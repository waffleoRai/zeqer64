package waffleoRai_zeqer64;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import waffleoRai_Sound.nintendo.Z64Wave;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_zeqer64.filefmt.UltraWavFile;
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
	
	public String getSysTablePath(){
		return root_dir + SEP + ZeqerCore.DIRNAME_ZWAVE + SEP + ZeqerCore.FN_SYSWAVE;
	}
	
	public String getUserTablePath(){
		return root_dir + SEP + ZeqerCore.FN_USRWAVE;
	}
	
	public String getWavDataFilePath(int uid){
		if(isWavSeq(uid)){
			return getSysTablePath() + SEP + String.format("%08x.buwav", uid);
		}
		else{
			return getUserTablePath() + SEP + String.format("%08x.buwav", uid);	
		}
	}
	
	public boolean isWavSeq(int uid){
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
	
	public int[][][] loadWaveVersionTable(String rom_id) throws IOException{
		if(wav_table_sys == null) return null;
		String wavdir = root_dir + SEP + ZeqerCore.DIRNAME_ZWAVE;
		return ZeqerWaveTable.loadVersionTable(wavdir, rom_id);
	}
	
	public List<Map<Integer, Integer>> loadVersionWaveOffsetIDMap(String rom_id) throws IOException{
		if(wav_table_sys == null) return null;
		String wavdir = root_dir + SEP + ZeqerCore.DIRNAME_ZWAVE;
		return ZeqerWaveTable.loadVersionWaveOffsetIDMap(wavdir, rom_id);
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
