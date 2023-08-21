package waffleoRai_zeqer64;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.BinFieldSize;
import waffleoRai_Utils.BufferReference;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileUtils;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_soundbank.nintendo.z64.UltraBankFile;
import waffleoRai_soundbank.nintendo.z64.Z64Bank;
import waffleoRai_soundbank.nintendo.z64.Z64Bank.Z64ReadOptions;
import waffleoRai_soundbank.nintendo.z64.Z64Drum;
import waffleoRai_soundbank.nintendo.z64.Z64Envelope;
import waffleoRai_soundbank.nintendo.z64.Z64Instrument;
import waffleoRai_zeqer64.SoundTables.BankInfoEntry;
import waffleoRai_zeqer64.extract.RomExtractionSummary;
import waffleoRai_zeqer64.extract.WaveLocIDMap;
import waffleoRai_zeqer64.extract.RomExtractionSummary.ExtractionError;
import waffleoRai_zeqer64.filefmt.NusRomInfo;
import waffleoRai_zeqer64.filefmt.ZeqerBankTable;
import waffleoRai_zeqer64.filefmt.ZeqerPresetTable;
import waffleoRai_zeqer64.filefmt.ZeqerBankTable.BankTableEntry;
import waffleoRai_zeqer64.presets.ZeqerDrumPreset;
import waffleoRai_zeqer64.presets.ZeqerDummyPreset;
import waffleoRai_zeqer64.presets.ZeqerInstPreset;
import waffleoRai_zeqer64.presets.ZeqerPercPreset;
import waffleoRai_zeqer64.presets.ZeqerPercRegion;
import waffleoRai_zeqer64.presets.ZeqerSFXPreset;

class CoreBankManager {
	
	private static final char SEP = File.separatorChar;
	
	public static final String FN_ENVPRESETS = "envpresets.bin";
	
	/*----- Instance Variables -----*/
	
	private String root_dir; //Base bank dir (ie. zeqerbase/bnk)
	private boolean sys_write_enabled = false;
	
	private CoreWaveManager waveManager;
	
	private ZeqerBankTable bnk_table_sys;
	private ZeqerBankTable bnk_table_user;
	
	private ZeqerPresetTable preset_table_sys;
	private ZeqerPresetTable preset_table_user;
	
	private Map<String, Z64Envelope> envPresets;
	private boolean flag_save_systbl_usermode = false;
	
	/*----- Init -----*/
	
	public CoreBankManager(String base_bnk_dir, CoreWaveManager wavmgr, boolean sysMode) throws UnsupportedFileTypeException, IOException{
		//Loads tables too, or creates if not there.
		root_dir = base_bnk_dir;
		sys_write_enabled = sysMode;
		waveManager = wavmgr;
		envPresets = new HashMap<String, Z64Envelope>();
		
		loadTables();
	}
	
	private void loadTables() throws UnsupportedFileTypeException, IOException{
		String zdir = root_dir + SEP + ZeqerCore.DIRNAME_ZBANK;
		if(!FileBuffer.directoryExists(zdir)){
			Files.createDirectories(Paths.get(zdir));
		}
		
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
		
		//Load wave links for presets! Preset table loaded does not by itself!
		tblpath = getSysPresetTablePath();
		if(FileBuffer.fileExists(tblpath)){
			preset_table_sys = ZeqerPresetTable.readTable(FileBuffer.createBuffer(tblpath, true));
			linkPresetsToWaves(preset_table_sys);
		}
		else{
			preset_table_sys = ZeqerPresetTable.createTable();
		}
		
		tblpath = getUserPresetTablePath();
		if(FileBuffer.fileExists(tblpath)){
			preset_table_user = ZeqerPresetTable.readTable(FileBuffer.createBuffer(tblpath, true));
			linkPresetsToWaves(preset_table_user);
		}
		else{
			if(!sys_write_enabled){
				preset_table_user = ZeqerPresetTable.createTable();	
			}
		}
		
		loadEnvPresetTable();
	}
	
	public int genNewRandomBankGUID(){
		Random r = new Random();
		int uid = r.nextInt();
		while((uid == 0) || (uid == -1) || hasBankWithGUID(uid)){
			uid = r.nextInt();
		}
		return uid;
	}
	
	public int genNewRandomPresetGUID(){
		Random r = new Random();
		int uid = r.nextInt();
		while((uid == 0) || (uid == -1) || hasPresetWithGUID(uid)){
			uid = r.nextInt();
		}
		return uid;
	}
	
	private void linkPresetsToWaves(ZeqerPresetTable table){
		if(table == null) return;
		if(waveManager == null) return;
		List<ZeqerPreset> presets = table.getAll();
		for(ZeqerPreset preset : presets){
			if(preset instanceof ZeqerInstPreset){
				ZeqerInstPreset ipreset = (ZeqerInstPreset) preset;
				Z64Instrument idat = ipreset.getInstrument();
				
				int wid = ipreset.getWaveIDLo();
				Z64WaveInfo winfo = null;
				if((wid != 0) && (wid != -1)){
					winfo = waveManager.getWaveInfo(wid);
					idat.setSampleLow(winfo);
				}
				
				wid = ipreset.getWaveIDMid();
				winfo = null;
				if((wid != 0) && (wid != -1)){
					winfo = waveManager.getWaveInfo(wid);
					idat.setSampleMiddle(winfo);
				}
				
				wid = ipreset.getWaveIDHi();
				winfo = null;
				if((wid != 0) && (wid != -1)){
					winfo = waveManager.getWaveInfo(wid);
					idat.setSampleHigh(winfo);
				}
			}
			else if(preset instanceof ZeqerPercPreset){
				ZeqerPercPreset ppreset = (ZeqerPercPreset) preset;
				int rcount = ppreset.getRegionCount();
				for(int i = 0; i < rcount; i++){
					ZeqerPercRegion reg = ppreset.getRegion(i);
					if(reg != null){
						int min_note = reg.getMinSlot();
						int wid = ppreset.getSlotWaveID(min_note);
						if((wid != 0) && (wid != -1)){
							Z64Drum drum = reg.getDrumData();
							if(drum == null) continue;
							drum.setSample(waveManager.getWaveInfo(wid));
						}
					}
				}
			}
			else if(preset instanceof ZeqerDrumPreset){
				updateDrumPresetWaveLinks((ZeqerDrumPreset)preset);
			}
			else if(preset instanceof ZeqerSFXPreset){
				//Preset object doesn't have direct link.
			}
		}
	}
	
	/*----- Install -----*/
	
	protected boolean updateVersion(String temp_update_dir) throws IOException, UnsupportedFileTypeException{
		//All user files get moved back automatically.
		//The exception is zpresets, which gets merged.
		//The new version of zbanks is retained (not overwritten by old)
		
		Path src_dir_path = Paths.get(temp_update_dir);
		if(!Files.isDirectory(src_dir_path)) return false;
		
		DirectoryStream<Path> dirstr = Files.newDirectoryStream(src_dir_path);
		for(Path child : dirstr){
			if(Files.isDirectory(child)){
				String fn = child.getFileName().toString();
				if(!fn.equals(ZeqerCore.DIRNAME_ZBANK)){
					String target = root_dir + SEP + fn;
					FileUtils.moveDirectory(child.toAbsolutePath().toString(), target, true);
				}
			}
			else {
				String target = root_dir + SEP + child.getFileName().toString();
				Files.move(child, Paths.get(target), StandardCopyOption.REPLACE_EXISTING);
			}
		}
		dirstr.close();
		
		//zbnks
		src_dir_path = Paths.get(temp_update_dir + SEP + ZeqerCore.DIRNAME_ZBANK);
		if(Files.isDirectory(src_dir_path)){
			dirstr = Files.newDirectoryStream(src_dir_path);
			for(Path child : dirstr){
				String fn = child.getFileName().toString();
				if(Files.isDirectory(child)){
					//Does not expect internal directories... Copy back if found?
					String target = root_dir + SEP + ZeqerCore.DIRNAME_ZBANK + SEP + fn;
					FileUtils.moveDirectory(child.toAbsolutePath().toString(), target, true);
				}
				else{
					if(!fn.equals(ZeqerCore.FN_SYSBANK)){
						if(fn.equals(ZeqerCore.FN_SYSPRESET)){
							String target = root_dir + SEP + ZeqerCore.DIRNAME_ZBANK + SEP + fn;
							if(FileBuffer.fileExists(target)){
								ZeqerPresetTable oldPresets = ZeqerPresetTable.readTable(FileBuffer.createBuffer(child.toAbsolutePath().toString(), true));
								ZeqerPresetTable newPresets = ZeqerPresetTable.readTable(FileBuffer.createBuffer(target, true));
								
								//Look for dataless presets in new and replace with data in old, if present
								List<ZeqerPreset> allnew = newPresets.getAll();
								if(allnew != null){
									for(ZeqerPreset npre : allnew){
										if(!npre.hasData()){
											//Probably won't have data, but worth checking.
											int uid = npre.getUID();
											ZeqerPreset opre = oldPresets.getPreset(uid);
											if(opre != null){
												if(npre instanceof ZeqerDummyPreset){
													ZeqerDummyPreset dummy = (ZeqerDummyPreset)npre;
													if(opre instanceof ZeqerInstPreset){
														ZeqerInstPreset ipre = (ZeqerInstPreset)opre;
														ZeqerInstPreset new_ipre = dummy.loadInstData(ipre.getInstrument());
														newPresets.addPreset(new_ipre);
														updateInstPresetWaveLinks(new_ipre);
													}
													else if(opre instanceof ZeqerDrumPreset){
														ZeqerDrumPreset dpre = (ZeqerDrumPreset)opre;
														ZeqerDrumPreset new_dpre = dummy.loadDrumData(dpre.getData());
														newPresets.addPreset(new_dpre);
														updateDrumPresetWaveLinks(new_dpre);
													}
													else if(opre instanceof ZeqerPercPreset){
														ZeqerPercPreset ppre = (ZeqerPercPreset)opre;
														ZeqerPercPreset new_ppre = dummy.loadDrumsetData(ppre);
														newPresets.addPreset(new_ppre);
														updateDrumsetPresetWaveLinks(new_ppre);
													}
													else{
														opre.setName(npre.getName());
														opre.setEnumLabel(npre.getEnumLabel());
														opre.clearTags();
														List<String> tags = npre.getAllTags();
														for(String tag: tags){
															opre.addTag(tag);
														}
														newPresets.addPreset(opre);
													}
												}
												else{
													opre.setName(npre.getName());
													opre.setEnumLabel(npre.getEnumLabel());
													opre.clearTags();
													List<String> tags = npre.getAllTags();
													for(String tag: tags){
														opre.addTag(tag);
													}
													newPresets.addPreset(opre);
												}
											}
										}
									}
								}
								
								//Re-save the new preset table.
								newPresets.writeTo(target);
							}
							else{
								Files.move(child, Paths.get(target), StandardCopyOption.REPLACE_EXISTING);
							}
						}
						else{
							String target = root_dir + SEP + ZeqerCore.DIRNAME_ZBANK + SEP + fn;
							Files.move(child, Paths.get(target), StandardCopyOption.REPLACE_EXISTING);
						}
					}
				}
			}
			dirstr.close();
		}

		return true;
	}
	
	protected boolean hardReset(){
		bnk_table_sys = null;
		bnk_table_user = null;
		preset_table_sys = null;
		preset_table_user = null;
		envPresets.clear();
		flag_save_systbl_usermode = false;
		
		try{loadTables();}
		catch(Exception ex){
			ex.printStackTrace();
			return false;
		}
		
		return true;
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
	
	public boolean isSysPreset(int uid){
		if(preset_table_sys == null) return false;
		return preset_table_sys.hasPreset(uid);
	}
	
	public boolean isSysBank(int uid){
		if(bnk_table_sys != null){
			return bnk_table_sys.getBank(uid) != null;
		}
		return false;
	}
	
	public boolean hasPresetWithGUID(int uid){
		if(preset_table_user != null){
			if(preset_table_user.getPreset(uid) != null) return true;
		}
		if(preset_table_sys != null){
			if(preset_table_sys.getPreset(uid) != null) return true;
		}
		return false;
	}
	
	public boolean hasBankWithGUID(int uid){
		if(bnk_table_user != null){
			if(bnk_table_user.getBank(uid) != null) return true;
		}
		if(bnk_table_sys != null){
			if(bnk_table_sys.getBank(uid) != null) return true;
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
		//This method DOES reference the waveManager
		//	to link wave info as well!
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
			Z64Bank zbank = new Z64Bank();
			UltraBankFile ubnk = UltraBankFile.open(FileBuffer.createBuffer(bnkpath, true));
			ubnk.readTo(zbank);
			
			String wsdpath = basedir + SEP + entry.getWSDFileName();
			if(FileBuffer.fileExists(wsdpath)){
				UltraBankFile uwsd = UltraBankFile.open(FileBuffer.createBuffer(wsdpath, true));
				uwsd.readTo(zbank);
				uwsd.close();
			}
			
			Map<Integer, Z64WaveInfo> wavemap = waveManager.getAllInfoMappedByUID();
			if(wavemap != null){
				//uwsd and ubnk are independent.
				//I don't know why I made this function non-static, but
				//	I'm too lazy to fix it.
				ubnk.linkWaves(zbank, wavemap);
			}
			
			ubnk.close();
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
	
	public List<ZeqerPreset> getAllPresets(){
		List<ZeqerPreset> list = new LinkedList<ZeqerPreset>();
		if(preset_table_sys != null){
			list.addAll(preset_table_sys.getAll());
		}
		if(preset_table_user != null){
			list.addAll(preset_table_user.getAll());
		}
		return list;
	}
	
	public List<ZeqerPreset> getAllValidPresets(){
		List<ZeqerPreset> list = new LinkedList<ZeqerPreset>();
		if(preset_table_sys != null){
			List<ZeqerPreset> all = preset_table_sys.getAll();
			for(ZeqerPreset preset : all){
				if(preset.hasData()) list.add(preset);
			}
		}
		if(preset_table_user != null){
			List<ZeqerPreset> all = preset_table_user.getAll();
			for(ZeqerPreset preset : all){
				if(preset.hasData()) list.add(preset);
			}
		}
		return list;
	}
	
	public ZeqerBank loadZeqerBank(int bank_uid){
		BankTableEntry entry = null;
		if(bnk_table_sys != null) entry = bnk_table_sys.getBank(bank_uid);
		if(entry != null){
			ZeqerBank bnk = new ZeqerBank(entry, waveManager, !sys_write_enabled);
			bnk.setDataSaveStem(root_dir + SEP + ZeqerCore.DIRNAME_ZBANK + SEP + entry.getDataPathStem());
			return bnk;
		}
		else{
			if(bnk_table_user != null) entry = bnk_table_user.getBank(bank_uid);
			if(entry != null){
				ZeqerBank bnk = new ZeqerBank(entry, waveManager, false);
				bnk.setDataSaveStem(root_dir + SEP + entry.getDataPathStem());
				return bnk;
			}
		}
		return null;
	}
	
	public List<ZeqerBank> getAllValidBanks(){
		List<ZeqerBank> list = new LinkedList<ZeqerBank>();
		if(bnk_table_sys != null){
			String zdir = root_dir + SEP + ZeqerCore.DIRNAME_ZBANK;
			Collection<BankTableEntry> contents = bnk_table_sys.getAllEntries();
			for(BankTableEntry entry : contents){
				//Check for bubnk or buwsd file
				boolean dataokay = false;
				String fpath = zdir + SEP + entry.getDataFileName();
				if(FileBuffer.fileExists(fpath)) dataokay = true;
				if(!dataokay){
					fpath = zdir + SEP + entry.getWSDFileName();
					if(FileBuffer.fileExists(fpath)) dataokay = true;
				}
				
				if(dataokay){
					ZeqerBank bnk = new ZeqerBank(entry, waveManager, !sys_write_enabled);
					bnk.setDataSaveStem(zdir + SEP + entry.getDataPathStem());
					list.add(bnk);
				}
			}
		}
		if(bnk_table_user != null){
			//Probably don't need to check for file presence...?
			Collection<BankTableEntry> contents = bnk_table_user.getAllEntries();
			for(BankTableEntry entry : contents){
				ZeqerBank bnk = new ZeqerBank(entry, waveManager, false);
				bnk.setDataSaveStem(root_dir + SEP + entry.getDataPathStem());
				list.add(bnk);
			}
		}
		return list;
	}
	
	public int[] loadVersionTable(String zeqer_id) throws IOException{
		String idtblpath = root_dir + SEP + ZeqerCore.DIRNAME_ZBANK + SEP + "bnk_" + zeqer_id + ".bin";
		if(!FileBuffer.fileExists(idtblpath)) return null;
		FileBuffer buff = FileBuffer.createBuffer(idtblpath, true);
		buff.setCurrentPosition(0L);
		int bcount = (int)(buff.getFileSize() >>> 2);
		int[] uids = new int[bcount];
		for(int i = 0; i < bcount; i++){
			uids[i] = buff.nextInt();
		}
		return uids;
	}
	
	/*----- Setters -----*/
	
	public ZeqerBank newUserBank(int sfx_alloc) throws IOException{
		if(bnk_table_user == null) return null;
		
		int uid = genNewRandomBankGUID();
		
		BankTableEntry entry = bnk_table_user.newEntry(uid);
		Z64Bank bnk = new Z64Bank(sfx_alloc);
		
		ZeqerBank bank = new ZeqerBank(entry, waveManager, true);
		bank.setDataSaveStem(root_dir + SEP + entry.getDataPathStem());
		bank.setBankData(bnk);
		
		return bank;
	}
	
	public boolean deleteBank(int uid) throws IOException{
		if(uid == 0 || uid == -1) return false;
		if(bnk_table_user != null){
			BankTableEntry e = bnk_table_user.removeEntry(uid);
			if(e != null){
				String path = root_dir + SEP + e.getDataPathStem() + ".bubnk";
				Files.deleteIfExists(Paths.get(path));
				path = root_dir + SEP + e.getDataPathStem() + ".buwsd";
				Files.deleteIfExists(Paths.get(path));
				return true;
			}
		}
		if(!sys_write_enabled) return false;
		BankTableEntry e = bnk_table_sys.removeEntry(uid);
		if(e != null){
			String path = root_dir + SEP + ZeqerCore.DIRNAME_ZBANK + SEP + e.getDataPathStem() + ".bubnk";
			Files.deleteIfExists(Paths.get(path));
			path = root_dir + SEP + ZeqerCore.DIRNAME_ZBANK + SEP + e.getDataPathStem() + ".buwsd";
			Files.deleteIfExists(Paths.get(path));
			return true;
		}
		return false;
	}
	
	public int addUserPreset(ZeqerPreset preset){
		if(preset == null) return 0;
		if(preset_table_user == null) return 0;
		
		int uid = preset.getUID();
		if(uid == 0 || uid == -1){
			Random r = new Random();
			uid = r.nextInt();
			preset.setUID(uid);
		}
		preset_table_user.addPreset(preset);
		
		return uid;
	}
	
	public boolean deletePreset(int uid){
		if(preset_table_user != null){
			ZeqerPreset prs = preset_table_user.removePreset(uid);
			if(prs != null) return true;
		}
		if(!sys_write_enabled) return false;
		return preset_table_sys.removePreset(uid) != null;
	}
	
	/*----- Envelope Presets -----*/
	
	public Map<String, Z64Envelope> getAllEnvelopePresets(){
		Map<String, Z64Envelope> copy = new HashMap<String, Z64Envelope>();
		for(Entry<String, Z64Envelope> entry : envPresets.entrySet()){
			copy.put(entry.getKey(), entry.getValue());
		}
		return copy;
	}
	
	public void addEnvelopePreset(String name, Z64Envelope env){
		if(name == null) return;
		if(env == null) return;
		envPresets.put(name, env);
	}
	
	public boolean removeEnvelopePreset(String name){
		return (envPresets.remove(name) != null);
	}
	
	public void loadEnvPresetTable() throws IOException{
		String path = root_dir + SEP + FN_ENVPRESETS;
		envPresets.clear();
		if(FileBuffer.fileExists(path)){
			FileBuffer data = FileBuffer.createBuffer(path, true);
			BufferReference rdr = data.getReferenceAt(0L);
			long fileSize = data.getFileSize();
			while(rdr.getBufferPosition() < fileSize){
				String ename = rdr.nextVariableLengthString("UTF8", BinFieldSize.WORD, 2);
				Z64Envelope env = new Z64Envelope();
				while(true){
					short cmd = rdr.nextShort();
					short val = rdr.nextShort();
					if(!env.addEvent(cmd, val)) break;
					if(env.hasTerminal()) break;
				}
				envPresets.put(ename, env);
			}
		}
	}
	
	public void saveEnvPresetTable() throws IOException{
		String path = root_dir + SEP + FN_ENVPRESETS;
		if(envPresets != null && !envPresets.isEmpty()){
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path));
			for(Entry<String, Z64Envelope> entry : envPresets.entrySet()){
				int buffsize = 0;
				String name = entry.getKey();
				buffsize += name.length() << 2;
				buffsize += 3;
				List<short[]> events = entry.getValue().getEvents();
				buffsize += events.size() << 2;
				
				
				FileBuffer buffer = new FileBuffer(buffsize, true);
				buffer.addVariableLengthString("UTF8", name, BinFieldSize.WORD, 2);
				for(short[] event : events){
					buffer.addToFile(event[0]);
					buffer.addToFile(event[1]);
				}
				buffer.writeToStream(bos);
			}
			bos.close();
		}
		else{
			Files.deleteIfExists(Paths.get(path));
		}
	}
	
	/*----- ROM Extract -----*/
	
	private static class RomImportContext{
		String zbnk_dir;
		
		int bankIndex;
		BankInfoEntry bankInfo;
		FileBuffer audiobankFull;
		WaveLocIDMap wavelocs;
		ZeqerRom z_rom;
		
		BankTableEntry bentry;
		Z64Bank mybank;
		byte[] md5;
		
		boolean good;
		Set<Integer> preset_ids;
		Set<Integer> ref_idxs;
		int[] bank_ids;
		
		Map<String, String> name_enum_map;
		
		boolean verbose;
		
		RomExtractionSummary errorInfo;
		
		public RomImportContext(){
			good = true;
			preset_ids = new TreeSet<Integer>();
			ref_idxs = new TreeSet<Integer>();
			verbose = false;
			bankIndex = 0;
			name_enum_map = new HashMap<String, String>();
		}
		
		public void clearForNextBank(){
			bankInfo = null;
			bentry = null;
			mybank = null;
			bankIndex++;
			md5 = null;
			name_enum_map.clear();
		}
		
	}
	
	private static void initErrorSummary(RomExtractionSummary errorInfo){
		if(errorInfo != null){
			errorInfo.soundfontsAddedAsCustom = 0;
			errorInfo.soundfontsFound = 0;
			errorInfo.soundfontsNew = 0;
			errorInfo.soundfontsOkay = 0;
			errorInfo.ipresetsAddedAsCustom = 0;
			errorInfo.ipresetsNew = 0;
			errorInfo.ppresetsAddedAsCustom = 0;
			errorInfo.ppresetsNew = 0;
			errorInfo.dpresetsAddedAsCustom = 0;
			errorInfo.dpresetsNew = 0;
		}
	}
	
	private static void autogenInstLabels(Z64Bank bank){
		if(bank == null) return;
		
		int i = 0;
		List<Z64Instrument> insts = bank.getAllUniqueInstruments();
		if(insts != null){
			for(Z64Instrument inst : insts){
				inst.setName(String.format("Instrument %03d", i++));
			}
		}
		
		for(i = 0; i < 126; i++){
			Z64Instrument inst = bank.getInstrumentInSlot(i);
			if(inst != null){
				bank.setInstPresetEnumString(i, String.format("INST_%03d", i));
			}
		}
		
		i = 0;
		List<Z64Drum> drums = bank.getAllUniqueDrums();
		if(drums != null){
			for(Z64Drum drum : drums){
				drum.setName(String.format("Drum %03d", i++));
			}	
		}
		
		for(i = 0; i < 64; i++){
			Z64Drum drum = bank.getDrumInSlot(i);
			if(drum != null){
				bank.setDrumPresetEnumString(i, String.format("PERC_%02d", i));
			}
		}
		
		int sfxCount = bank.getEffectiveSFXCount();
		for(i = 0; i < sfxCount; i++){
			bank.setSFXPresetEnumString(i, String.format("SFX_%03d", i));
		}
	}
	
	private void updateBankEntryMetadata(RomImportContext ctx){
		int icount = ctx.bankInfo.getInstrumentCount();
		int pcount = ctx.bankInfo.getPercussionCount();
		int xcount = ctx.bankInfo.getSFXCount();
		
		ctx.bentry.setEnumString("AUDIOBANK_" + ctx.z_rom.getRomInfo().getZeqerID().toUpperCase() + 
				String.format("_%03d", ctx.bankIndex));
		ctx.bentry.setInstCounts(icount, pcount, xcount);
		ctx.bentry.setWarcIndex(ctx.bankInfo.getPrimaryWaveArcIndex());
		ctx.bentry.setWarc2Index(ctx.bankInfo.getSecondaryWaveArcIndex());
		ctx.bentry.setMedium(ctx.bankInfo.getMedium());
		ctx.bentry.setCachePolicy(ctx.bankInfo.getCachePolicy());
	}
	
	private String updateBankMetadata(RomImportContext ctx, boolean sysmode){
		ctx.mybank.setUID(ctx.bentry.getUID());
		ctx.mybank.setMedium(ctx.bentry.getMedium());
		ctx.mybank.setCachePolicy(ctx.bentry.getCachePolicy());
		ctx.mybank.setPrimaryWaveArcIndex(ctx.bentry.getWarcIndex());
		ctx.mybank.setSecondaryWaveArcIndex(ctx.bentry.getSecondaryWarcIndex());
		
		String data_path = null;
		if(sysmode){
			data_path = ctx.zbnk_dir + SEP + ctx.bentry.getDataPathStem();
		}
		else{
			data_path = root_dir + SEP + ctx.bentry.getDataPathStem();
		}
		
		return data_path;
	}
	
	private String importBankToTable(RomImportContext ctx){
		String data_path = null;
		if(ctx.bentry != null){
			//If match, no need to continue on this one
				//Map UID to index and continue
			ctx.bank_ids[ctx.bankIndex] = ctx.bentry.getUID();
			
			//Extract or update data file if not present
			data_path = ctx.zbnk_dir + SEP + ctx.bentry.getDataFileName();
			if(!FileBuffer.fileExists(data_path)){
				data_path = updateBankMetadata(ctx, true);
			}
			else data_path = null;
			
			if(ctx.errorInfo != null) ctx.errorInfo.soundfontsOkay++;
		}
		else{
			if(sys_write_enabled){
				//If no match, need new entry
				//Create new entry
				ctx.bentry = bnk_table_sys.newEntry(ctx.md5);
				updateBankEntryMetadata(ctx);
				
				//Map UID to index
				ctx.bank_ids[ctx.bankIndex] = ctx.bentry.getUID();
				
				//Update bank info
				data_path = ctx.zbnk_dir + SEP + ctx.bentry.getDataFileName();
				if(!FileBuffer.fileExists(data_path)){
					data_path = updateBankMetadata(ctx, true);
				}
				else data_path = null;
				
				if(ctx.errorInfo != null){
					ctx.errorInfo.soundfontsOkay++;
					ctx.errorInfo.soundfontsNew++;
				}
			}
			else{
				//try to add as user font
				if(ctx.verbose) System.err.println("CoreBankManager.importROMBanks || WARNING: Bank at index " + ctx.bankIndex + " count not be matched.");
				if(bnk_table_user != null){
					String md5str = FileUtils.bytes2str(ctx.md5);
					ctx.bentry = bnk_table_user.matchBankMD5(md5str);
					if(ctx.bentry != null){
						ctx.bank_ids[ctx.bankIndex] = ctx.bentry.getUID();
						data_path = root_dir + SEP + ctx.bentry.getDataFileName();
						if(!FileBuffer.fileExists(data_path)){
							data_path = updateBankMetadata(ctx, false);
						}
						else data_path = null;
					}
					else{
						ctx.bentry = bnk_table_user.newEntry(ctx.md5);
						updateBankEntryMetadata(ctx);
						
						//Map UID to index
						ctx.bank_ids[ctx.bankIndex] = ctx.bentry.getUID();
						
						//Write bank file
						data_path = root_dir + SEP + ctx.bentry.getDataFileName();
						if(!FileBuffer.fileExists(data_path)){
							data_path = updateBankMetadata(ctx, false);
						}
						else data_path = null;
						
						if(ctx.errorInfo != null){
							ctx.errorInfo.soundfontsAddedAsCustom++;
						}
					}
				}
				else{
					ctx.good = false;
					if(ctx.errorInfo != null){
						if(ctx.errorInfo.soundfontErrors == null){
							ctx.errorInfo.soundfontErrors = new LinkedList<ExtractionError>();
						}
						ExtractionError err = new ExtractionError();
						err.index0 = ctx.bankIndex;
						err.itemType = RomExtractionSummary.ITEM_TYPE_BNK;
						err.reason = RomExtractionSummary.ERROR_NOT_IN_TABLE;
						err.itemUID = ZeqerUtils.md52UID(ctx.md5);
						ctx.errorInfo.soundfontErrors.add(err);
					}
				}
			}
		}
		
		return data_path; //Returns the ultrabank file stem
	}
	
	private void updateDrumsetPresetWaveLinks(ZeqerPercPreset preset){
		if(waveManager == null || preset == null) return;
		
		int rcount = preset.getRegionCount();
		for(int i = 0; i < rcount; i++){
			ZeqerPercRegion reg = preset.getRegion(i);
			if(reg != null){
				Z64Drum drum = reg.getDrumData();
				if(drum == null) continue;
				
				Z64WaveInfo winfo = null;
				
				int wid = preset.getSlotWaveID(reg.getMinSlot());
				if((wid == 0) || (wid == -1)){
					winfo = drum.getSample();
					if(winfo != null){
						wid = winfo.getUID();
						int minnote = reg.getMinSlot();
						int maxnote = reg.getMaxSlot();
						for(int j = minnote; j <= maxnote; j++){
							preset.setWaveID(j, wid);
						}
					}
				}
				
				if((wid != 0) && (wid != -1)){
					winfo = waveManager.getWaveInfo(wid);
					if(winfo != null) drum.setSample(winfo);
				}
			}
		}
		
	}
	
	private void updateDrumPresetWaveLinks(ZeqerDrumPreset preset){
		if(waveManager == null || preset == null) return;
		
		Z64Drum drum = preset.getData();
		if(drum == null) return;
		Z64WaveInfo winfo = null;
		
		int wid = preset.getWaveID();
		if((wid == 0) || (wid == -1)){
			winfo = drum.getSample();
			if(winfo != null){
				wid = winfo.getUID();
				preset.setWaveID(wid);
			}
		}
		
		if((wid != 0) && (wid != -1)){
			winfo = waveManager.getWaveInfo(wid);
			if(winfo != null) drum.setSample(winfo);
		}
	}
	
	private void updateInstPresetWaveLinks(ZeqerInstPreset preset){
		if(waveManager == null || preset == null) return;
		
		Z64Instrument inst = preset.getInstrument();
		if(inst == null) return;
		Z64WaveInfo winfo = null;
		
		//Mid sample
		int wid = preset.getWaveIDMid();
		if((wid == 0) || (wid == -1)){
			winfo = inst.getSampleMiddle();
			if(winfo != null){
				wid = winfo.getUID();
				preset.setWaveIDMid(wid);
			}
		}
		if((wid != 0) && (wid != -1)){
			winfo = waveManager.getWaveInfo(wid);
			if(winfo != null) inst.setSampleMiddle(winfo);
		}
		
		//Lo
		wid = preset.getWaveIDLo();
		if((wid == 0) || (wid == -1)){
			winfo = inst.getSampleLow();
			if(winfo != null){
				wid = winfo.getUID();
				preset.setWaveIDLo(wid);
			}
		}
		if((wid != 0) && (wid != -1)){
			winfo = waveManager.getWaveInfo(wid);
			if(winfo != null) inst.setSampleLow(winfo);
		}
		
		//Hi
		wid = preset.getWaveIDHi();
		if((wid == 0) || (wid == -1)){
			winfo = inst.getSampleHigh();
			if(winfo != null){
				wid = winfo.getUID();
				preset.setWaveIDHi(wid);
			}
		}
		if((wid != 0) && (wid != -1)){
			winfo = waveManager.getWaveInfo(wid);
			if(winfo != null) inst.setSampleHigh(winfo);
		}
		
	}
	
	private void importUpdateInstPreset(RomImportContext ctx, Z64Instrument inst){
		ZeqerInstPreset preset = new ZeqerInstPreset(inst);
		int hash_uid = preset.hashToUID();
		preset.setUID(hash_uid);
		ZeqerPreset match = preset_table_sys.getPreset(hash_uid);
		if(match == null){
			if(sys_write_enabled){
				//Add to table.
				preset_table_sys.addPreset(preset);
				ctx.preset_ids.add(hash_uid);
				if(ctx.errorInfo != null) ctx.errorInfo.ipresetsNew++;
			}
			else{
				if(ctx.verbose) System.err.println("CoreBankManager.importROMBanks || WARNING: Could not match preset 0x" + Integer.toHexString(hash_uid));
				if(preset_table_user != null){
					preset_table_user.addPreset(preset);
					ctx.preset_ids.add(hash_uid);
					if(ctx.errorInfo != null) ctx.errorInfo.ipresetsAddedAsCustom++;
				}
				else ctx.good = false;
			}
			updateInstPresetWaveLinks(preset);
		}
		else{
			//If it's a dummy, swap it out for one with data.
			ctx.preset_ids.add(hash_uid);
			if(!match.hasData()){
				//Load data
				if(sys_write_enabled) {
					//Also this just overwrites all of it.
					//We want to keep the metadata...
					preset_table_sys.addPreset(preset);
				}
				else{
					//But still need to swap in the data if in user mode
					if(match instanceof ZeqerInstPreset){
						//Need to make sure linked to samples? 
						//	Otherwise load function can't find them.
						//System.err.println("DEBUG -- CoreBankManager.importUpdatePresets || Update sys preset.");
						ZeqerInstPreset imatch = (ZeqerInstPreset)match;
						imatch.loadData(inst);
						flag_save_systbl_usermode = true;
						
						updateInstPresetWaveLinks(imatch);
					}
					else if(match instanceof ZeqerDummyPreset){
						ZeqerInstPreset imatch = ((ZeqerDummyPreset) match).loadInstData(inst);
						preset_table_sys.addPreset(imatch);
						flag_save_systbl_usermode = true;
						match = imatch;
						
						updateInstPresetWaveLinks(imatch);
					}
				}
			}
			//Set inst name to match
			inst.setName(match.getName());
			if(match instanceof ZeqerInstPreset){
				ZeqerInstPreset ipreset = (ZeqerInstPreset)match;
				ctx.name_enum_map.put(ipreset.getName(), ipreset.getEnumLabel());
			}
		}
	}
	
	private Z64Drum importUpdateDrumPreset(RomImportContext ctx, Z64Drum drum){
		ZeqerDrumPreset dpreset = new ZeqerDrumPreset(drum);
		int dhash = dpreset.hashToUID();
		dpreset.setUID(dhash);
		
		ZeqerPreset dmatch = preset_table_sys.getPreset(dhash);
		if(dmatch == null){
			//New preset
			dpreset.setName(String.format("drum %08x", dhash));
			dpreset.setEnumLabel(String.format("DPRE_%08x", dhash));
			if(sys_write_enabled){
				preset_table_sys.addPreset(dpreset);
				ctx.preset_ids.add(dhash);
				if(ctx.errorInfo != null) ctx.errorInfo.dpresetsNew++;
			}
			else{
				//Try to add to user table
				if(ctx.verbose) System.err.println("CoreBankManager.importROMBanks || WARNING: Could not match drum preset 0x" + Integer.toHexString(dhash));
				if(preset_table_user != null){
					preset_table_user.addPreset(dpreset);
					ctx.preset_ids.add(dhash);
					if(ctx.errorInfo != null) ctx.errorInfo.dpresetsAddedAsCustom++;
				}
				else ctx.good = false;
			}
			updateDrumPresetWaveLinks(dpreset);
			return dpreset.getData();
		}
		else{
			//Load data if empty.
			ctx.preset_ids.add(dhash);
			if(!dmatch.hasData()){
				if(sys_write_enabled) {
					preset_table_sys.addPreset(dpreset);
					updateDrumPresetWaveLinks(dpreset);
				}
				else{
					//But still need to swap in the data if in user mode
					if(dmatch instanceof ZeqerDrumPreset){
						//Need to make sure linked to samples? 
						//	Otherwise load function can't find them.
						//System.err.println("DEBUG -- CoreBankManager.importUpdatePresets || Update sys preset.");
						ZeqerDrumPreset ddmatch = (ZeqerDrumPreset)dmatch;
						ddmatch.loadDrumData(drum);
						flag_save_systbl_usermode = true;
						updateDrumPresetWaveLinks(ddmatch);
					}
					else if(dmatch instanceof ZeqerDummyPreset){
						ZeqerDrumPreset ddmatch = ((ZeqerDummyPreset) dmatch).loadDrumData(drum);
						preset_table_sys.addPreset(ddmatch);
						flag_save_systbl_usermode = true;
						dmatch = ddmatch;
						
						updateDrumPresetWaveLinks(ddmatch);
					}
				}
			}
			//Set name to match
			drum.setName(dmatch.getName());
			if(dmatch instanceof ZeqerDrumPreset){
				ZeqerDrumPreset ddpreset = (ZeqerDrumPreset)dmatch;
				ctx.name_enum_map.put(ddpreset.getName(), ddpreset.getEnumLabel());
				return ddpreset.getData();
			}
		}
		
		return null;
	}
	
	private void importUpdateDrumset(RomImportContext ctx){
		Z64Drum[] drums = ctx.mybank.getPercussionSet();
		ZeqerPercPreset ppreset = new ZeqerPercPreset(-1);
		ppreset.setName("perc_" + ctx.z_rom.getRomInfo().getZeqerID() 
				+ String.format("_%02x", ctx.bankIndex));
		for(int j = 0; j < 64; j++){
			if(drums[j] != null) ppreset.setDrumToSlot(j, drums[j]);
		}
		
		int hash_uid = ppreset.hashToUID(); //Calls consolidateRegions()
		ppreset.setUID(hash_uid);
		ZeqerPreset match = preset_table_sys.getPreset(hash_uid);
		if(match == null){
			if(sys_write_enabled){
				//Add to table.
				preset_table_sys.addPreset(ppreset);
				ctx.preset_ids.add(hash_uid);
				if(ctx.errorInfo != null) ctx.errorInfo.ppresetsNew++;
			}
			else{
				if(ctx.verbose) System.err.println("CoreBankManager.importROMBanks || WARNING: Could not match preset 0x" + Integer.toHexString(hash_uid));
				if(preset_table_user != null){
					preset_table_user.addPreset(ppreset);
					ctx.preset_ids.add(hash_uid);
					if(ctx.errorInfo != null) ctx.errorInfo.ppresetsAddedAsCustom++;
				}
				else ctx.good = false;
			}
		}
		else{
			//If it's a dummy, swap it out for one with data.
			ctx.preset_ids.add(hash_uid);
			if(!match.hasData()){
				if(sys_write_enabled) {
					preset_table_sys.addPreset(ppreset);
				}
				else{
					int rcount = ppreset.getRegionCount();
					if(match instanceof ZeqerPercPreset){
						ZeqerPercPreset pmatch = (ZeqerPercPreset)match;
						pmatch.clearAndReallocRegions(rcount);
						for(int i = 0; i < rcount; i++){
							pmatch.setRegion(i, ppreset.getRegion(i));
						}
						ppreset = pmatch;
						flag_save_systbl_usermode = true;
					}
					else if(match instanceof ZeqerDummyPreset){
						ppreset = ((ZeqerDummyPreset) match).loadDrumsetData(ppreset);
						preset_table_sys.addPreset(ppreset);
						flag_save_systbl_usermode = true;
					}
				}
			}
		}
		
		//Shouldn't need wavelinking for set since should happen when processing indiv drums...
		
		//Individual Drums
		int rcount = ppreset.getRegionCount();
		for(int r = 0; r < rcount; r++){
			ZeqerPercRegion reg = ppreset.getRegion(r);
			if(reg != null){
				Z64Drum drum = reg.getDrumData();
				if(drum != null){
					importUpdateDrumPreset(ctx, drum);
				}
				//Link back to drumset
				reg.setDrumData(drum);
			}
		}
		
		//Drum enums
		for(int n = 0; n < 64; n++){
			Z64Drum drum = ctx.mybank.getDrumInSlot(n);
			if(drum != null){
				String dname = drum.getName();
				if(dname != null){
					String denum = ctx.name_enum_map.get(dname);
					if(denum != null){
						ctx.mybank.setDrumPresetEnumString(n, String.format("%s_%02d", denum, n));
					}
				}
			}
		}
	}
	
	private void importUpdatePresets(RomImportContext ctx){
		//Instruments...
		Collection<Z64Instrument> ilist = ctx.mybank.getAllUniqueInstruments();
		for(Z64Instrument inst : ilist){
			importUpdateInstPreset(ctx, inst);
		}
		
		//Inst Enums
		for(int n = 0; n < 126; n++){
			Z64Instrument inst = ctx.mybank.getInstrumentInSlot(n);
			if(inst != null){
				String iname = inst.getName();
				if(iname != null){
					String ienum = ctx.name_enum_map.get(iname);
					if(ienum != null){
						ctx.mybank.setInstPresetEnumString(n, String.format("%s_%03d", ienum, n));
					}
				}
			}
		}
		
		//Percussion (as a set)
		if(ctx.bankInfo.getPercussionCount() > 0){
			importUpdateDrumset(ctx);
		}
	}
	
	private void importROMBank(RomImportContext ctx) throws IOException{
		int stpos = ctx.bankInfo.getOffset();
		int filelen = ctx.bankInfo.getSize();
		int icount = ctx.bankInfo.getInstrumentCount();
		int pcount = ctx.bankInfo.getPercussionCount();
		int xcount = ctx.bankInfo.getSFXCount();
		int warc = ctx.bankInfo.getPrimaryWaveArcIndex();
		
		if(filelen <= 0){
			ctx.ref_idxs.add(ctx.bankIndex);
			return;
		}
		if(ctx.errorInfo != null) ctx.errorInfo.soundfontsFound++; //Don't want to count refs
		
		//Parse bank binary
		FileBuffer bdat = ctx.audiobankFull.createReadOnlyCopy(stpos, stpos+filelen);
		Z64ReadOptions op = Z64Bank.genOptionsForKnownCounts(icount, pcount, xcount);
		ctx.mybank = Z64Bank.readRaw(bdat, op);
		try {bdat.dispose();} 
		catch (IOException e) {
			e.printStackTrace();
		}
		autogenInstLabels(ctx.mybank);
		
		//Substitute UIDs for wave offsets
		Map<Integer, Integer> omap = ctx.wavelocs.getWArcMap(warc);
		List<Z64WaveInfo> wavelist = ctx.mybank.getAllWaveBlocks();
		for(Z64WaveInfo winfo : wavelist){
			int woff = winfo.getWaveOffset();
			Integer mapped = omap.get(woff);
			if(mapped != null){
				winfo.setUID(mapped);
			}
		}
		
		//Re-serialize & Hash
		FileBuffer serbank = ctx.mybank.serializeMe(Z64Bank.SEROP_ORDERING_UID | Z64Bank.SEROP_REF_WAV_UIDS);
		ctx.md5 = FileUtils.getMD5Sum(serbank.getBytes(0, serbank.getFileSize()));

		//Check for matches
		String md5str = FileUtils.bytes2str(ctx.md5);
		ctx.bentry = bnk_table_sys.matchBankMD5(md5str);
		String data_path = importBankToTable(ctx);
		
		//Version flags
		if(ctx.bentry != null){
			NusRomInfo rominfo = ctx.z_rom.getRomInfo();
			if(rominfo != null){
				if(rominfo.isZ5()) ctx.bentry.setFlags(ZeqerBankTable.FLAG_Z5);
				if(rominfo.isZ6()) ctx.bentry.setFlags(ZeqerBankTable.FLAG_Z6);
			}
		}
		
		//Extract presets in sys mode, *match* them in user mode
		importUpdatePresets(ctx);
		
		//Write ultrabank files, if needed
		if(data_path != null){
			UltraBankFile.writeUBNK(ctx.mybank, data_path + ".bubnk", UltraBankFile.OP_LINK_WAVES_UID);
			if(ctx.mybank.getEffectiveSFXCount() > 0){
				UltraBankFile.writeUWSD(ctx.mybank, data_path + ".buwsd", UltraBankFile.OP_LINK_WAVES_UID);
			}
		}
	}
	
	public boolean importROMBanks(ZeqerRom z_rom, WaveLocIDMap wavelocs, RomExtractionSummary errorInfo, boolean verbose) throws IOException{
		//May be possible to drop wavelocs as input param, loading using z_rom id instead.
		//Would need to assume we go up a dir to the wavs dir from this root though.
		
		if(wavelocs == null) return false;
		if(z_rom == null) return false;
		
		NusRomInfo rominfo = z_rom.getRomInfo();
		if(rominfo == null) return false;
		
		initErrorSummary(errorInfo);
		
		RomImportContext ctx = new RomImportContext();
		ctx.z_rom = z_rom;
		ctx.wavelocs = wavelocs;
		ctx.verbose = verbose;
		
		ctx.audiobankFull = z_rom.loadAudiobank(); 
		ctx.audiobankFull.setEndian(true);
		BankInfoEntry[] bank_tbl = z_rom.loadBankEntries();
		int bcount = bank_tbl.length;
		ctx.bank_ids = new int[bcount];
		
		final String ZBANK_DIR = root_dir + SEP + ZeqerCore.DIRNAME_ZBANK;
		ctx.zbnk_dir = ZBANK_DIR;
		for(int i = 0; i < bcount; i++){
			ctx.bankInfo = bank_tbl[i];
			importROMBank(ctx);
			ctx.clearForNextBank();
		}
		
		//Resolve references
		for(Integer ridx : ctx.ref_idxs){
			int tidx = bank_tbl[ridx].getOffset();
			ctx.bank_ids[ridx] = ctx.bank_ids[tidx];
		}

		//Write
		String idtblpath = ZBANK_DIR + SEP + "bnk_" + rominfo.getZeqerID() + ".bin";
		FileBuffer outbuff = new FileBuffer(bcount << 2, true);
		for(int i = 0; i < bcount; i++){
			outbuff.addToFile(ctx.bank_ids[i]);
		}
		outbuff.writeFile(idtblpath);
		
		saveTables();
		return ctx.good;
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
	
	public void saveSysPresetsScrubbed() throws IOException{
		if(sys_write_enabled && preset_table_sys != null){
			String sysdir = root_dir + SEP + ZeqerCore.DIRNAME_ZBANK;
			preset_table_sys.writeScrubbed(sysdir + SEP + "zpresets_scrubbed.bin");
		}
	}
	
	public void saveSysTables() throws IOException{
		if(sys_write_enabled && bnk_table_sys != null){
			bnk_table_sys.writeTo(getSysTablePath());
		}
		if(sys_write_enabled && preset_table_sys != null){
			preset_table_sys.writeTo(getSysPresetTablePath());
		}
		saveEnvPresetTable();
	}
	
	public void saveUserTables() throws IOException{
		if(bnk_table_user != null){
			bnk_table_user.writeTo(getUserTablePath());
		}
		if(preset_table_user != null){
			preset_table_user.writeTo(getUserPresetTablePath());
		}
		if(flag_save_systbl_usermode && (preset_table_sys != null)){
			preset_table_sys.writeTo(getSysPresetTablePath());
			flag_save_systbl_usermode = false;
		}
		saveEnvPresetTable();
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
		saveEnvPresetTable();
	}
	
}
