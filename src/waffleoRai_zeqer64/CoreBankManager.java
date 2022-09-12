package waffleoRai_zeqer64;

import java.io.File;
import java.io.IOException;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_soundbank.nintendo.z64.Z64Bank;
import waffleoRai_soundbank.nintendo.z64.Z64Instrument;
import waffleoRai_zeqer64.filefmt.ZeqerBankTable;
import waffleoRai_zeqer64.filefmt.ZeqerPresetTable;
import waffleoRai_zeqer64.filefmt.ZeqerBankTable.BankTableEntry;
import waffleoRai_zeqer64.presets.ZeqerInstPreset;

class CoreBankManager {
	
	private static final char SEP = File.separatorChar;
	
	/*----- Instance Variables -----*/
	
	private String root_dir; //Base bank dir (ie. zeqerbase/bnk)
	private boolean sys_write_enabled = false;
	
	private CoreWaveManager waveManager;
	
	private ZeqerBankTable bnk_table_sys;
	private ZeqerBankTable bnk_table_user;
	
	private ZeqerPresetTable preset_table_sys;
	private ZeqerPresetTable preset_table_user;
	
	/*----- Init -----*/
	
	public CoreBankManager(String base_bnk_dir, CoreWaveManager wavmgr, boolean sysMode) throws UnsupportedFileTypeException, IOException{
		//Loads tables too, or creates if not there.
		root_dir = base_bnk_dir;
		sys_write_enabled = sysMode;
		waveManager = wavmgr;
		
		String tblpath = getSysTablePath();
		if(FileBuffer.fileExists(tblpath)){
			bnk_table_sys = ZeqerBankTable.readTable(FileBuffer.createBuffer(tblpath, true));
		}
		else{
			bnk_table_sys = ZeqerBankTable.createTable();
		}
	
		tblpath = getUserTablePath();
		if(FileBuffer.fileExists(tblpath)){
			bnk_table_user = ZeqerBankTable.readTable(FileBuffer.createBuffer(tblpath, true));
		}
		else{
			if(!sys_write_enabled){
				bnk_table_user = ZeqerBankTable.createTable();	
			}
		}
		
		tblpath = getSysPresetTablePath();
		if(FileBuffer.fileExists(tblpath)){
			preset_table_sys = ZeqerPresetTable.readTable(FileBuffer.createBuffer(tblpath, true));
		}
		else{
			preset_table_sys = ZeqerPresetTable.createTable();
		}
		
		tblpath = getUserPresetTablePath();
		if(FileBuffer.fileExists(tblpath)){
			preset_table_user = ZeqerPresetTable.readTable(FileBuffer.createBuffer(tblpath, true));
		}
		else{
			if(!sys_write_enabled){
				preset_table_user = ZeqerPresetTable.createTable();	
			}
		}
	}
	
	/*----- Getters -----*/
	
	public String getSysTablePath(){
		return root_dir + SEP + ZeqerCore.DIRNAME_ZBANK + SEP + ZeqerCore.FN_SYSBANK;
	}
	
	public String getUserTablePath(){
		return root_dir + SEP + ZeqerCore.FN_USRBANK;
	}
	
	public String getSysPresetTablePath(){
		return root_dir + SEP + ZeqerCore.DIRNAME_ZBANK + SEP + ZeqerCore.FN_SYSPRESET;
	}
	
	public String getUserPresetTablePath(){
		return root_dir + SEP + ZeqerCore.FN_USRPRESET;
	}
	
	public String getBankDataFilePath(int uid){
		if(isSysBank(uid)){
			return getSysTablePath() + SEP + String.format("%08x.bubnk", uid);
		}
		else{
			return getUserTablePath() + SEP + String.format("%08x.bubnk", uid);	
		}
	}
	
	public boolean isSysBank(int uid){
		if(bnk_table_sys != null){
			return bnk_table_sys.getBank(uid) != null;
		}
		return false;
	}
	
	public ZeqerBankTable getZeqerBankTable(){return bnk_table_sys;}
	public ZeqerPresetTable getZeqerPresetTable(){return preset_table_sys;}
	
	public ZeqerBankTable getUserBankTable(){
		if(bnk_table_user == null) bnk_table_user = ZeqerBankTable.createTable();
		return bnk_table_user;
	}
	
	public ZeqerPresetTable getUserPresetTable(){
		if(preset_table_user == null) preset_table_user = ZeqerPresetTable.createTable();
		return preset_table_user;
	}
	
	public BankTableEntry getBankInfo(int bank_uid){
		BankTableEntry entry = null;
		if(bnk_table_sys != null) entry = bnk_table_sys.getBank(bank_uid);
		if(entry == null && bnk_table_user != null) {
			entry = bnk_table_user.getBank(bank_uid);
		}
		return entry;
	}
	
	public Z64Bank loadBank(int bank_uid){
		BankTableEntry entry = null;
		String basedir = null;
		if(bnk_table_sys != null) entry = bnk_table_sys.getBank(bank_uid);
		if(entry != null){
			basedir = root_dir + SEP + ZeqerCore.DIRNAME_ZBANK;
		}
		
		if(entry == null && bnk_table_user != null) {
			entry = bnk_table_user.getBank(bank_uid);
			basedir = root_dir;
		}
		if(entry == null) return null;
		
		String bnkpath = basedir + SEP + entry.getDataFileName();
		//System.err.println("ZeqerCore.loadBank || Path: " + bnkpath);
		if(!FileBuffer.fileExists(bnkpath)) return null;
		try{
			Z64Bank zbank = Z64Bank.readUBNK(FileBuffer.createBuffer(bnkpath, true));
			String wsdpath = basedir + SEP + entry.getWSDFileName();
			if(FileBuffer.fileExists(wsdpath)){
				zbank.readUWSD(FileBuffer.createBuffer(wsdpath, true));
			}
			return zbank;
		}
		catch(Exception ex){
			System.err.println("ZeqerCore.loadBank || Failed to load bank " + String.format("%08x", bank_uid));
			ex.printStackTrace();
			return null;
		}
	}
	
	public Z64Instrument getPresetInstrumentByName(String preset_name){
		//Look thru preset tables for ZeqerPreset
		if(preset_table_sys == null) return null; //Should not be null...
		ZeqerPreset preset = preset_table_sys.getPresetByName(preset_name);
		if(preset == null){
			//Try user table.
			if(preset_table_user == null) return null;
			preset = preset_table_user.getPresetByName(preset_name);
			if(preset == null) return null;
		}
		
		//Cast to instrument preset (or return null if not one)
		if(!(preset instanceof ZeqerInstPreset)) return null;
		ZeqerInstPreset ipreset = (ZeqerInstPreset)preset;
		
		//Link up wave samples if not already linked!
		Z64Instrument inst = ipreset.getInstrument();
		int wuid = ipreset.getWaveIDMid();
		if(inst.getSampleMiddle() == null){
			if(wuid != 0 && wuid != -1){
				if(waveManager != null){
					inst.setSampleMiddle(waveManager.getWaveInfo(wuid));	
				}
			}
		}
		
		wuid = ipreset.getWaveIDLo();
		if(inst.getSampleLow() == null){
			if(wuid != 0 && wuid != -1){
				if(waveManager != null){
					inst.setSampleLow(waveManager.getWaveInfo(wuid));
				}
			}
		}
		
		wuid = ipreset.getWaveIDHi();
		if(inst.getSampleHigh() == null){
			if(wuid != 0 && wuid != -1){
				if(waveManager != null){
					inst.setSampleHigh(waveManager.getWaveInfo(wuid));
				}
			}
		}
		inst.setID(ipreset.getUID());
		
		return inst;
	}
	
	/*----- I/O -----*/
	
	public int importBankMetaTSV(String tsv_path) throws IOException{
		int records = 0;
		if(bnk_table_user != null) records += bnk_table_user.importTSV(tsv_path);
		if(sys_write_enabled && bnk_table_sys != null) records += bnk_table_sys.importTSV(tsv_path);
		return records;
	}
	
	public int importPresetMetaTSV(String tsv_path) throws IOException{
		int records = 0;
		if(preset_table_user != null) records += preset_table_user.importTSV(tsv_path);
		if(sys_write_enabled && preset_table_sys != null) records += preset_table_sys.importTSV(tsv_path);
		return records;
	}
	
	public void saveSysTables() throws IOException{
		if(sys_write_enabled && bnk_table_sys != null){
			bnk_table_sys.writeTo(getSysTablePath());
		}
		if(sys_write_enabled && preset_table_sys != null){
			preset_table_sys.writeTo(getSysPresetTablePath());
		}
	}
	
	public void saveUserTables() throws IOException{
		if(bnk_table_user != null){
			bnk_table_user.writeTo(getUserTablePath());
		}
		if(preset_table_user != null){
			preset_table_user.writeTo(getUserPresetTablePath());
		}
	}
	
	public void saveTables() throws IOException{
		if(sys_write_enabled && bnk_table_sys != null){
			bnk_table_sys.writeTo(getSysTablePath());
		}
		if(bnk_table_user != null){
			bnk_table_user.writeTo(getUserTablePath());
		}
		if(sys_write_enabled && preset_table_sys != null){
			preset_table_sys.writeTo(getSysPresetTablePath());
		}
		if(preset_table_user != null){
			preset_table_user.writeTo(getUserPresetTablePath());
		}
	}
	
}
