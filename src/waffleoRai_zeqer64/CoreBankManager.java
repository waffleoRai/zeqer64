package waffleoRai_zeqer64;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileUtils;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_soundbank.nintendo.z64.Z64Bank;
import waffleoRai_soundbank.nintendo.z64.Z64Drum;
import waffleoRai_soundbank.nintendo.z64.Z64Instrument;
import waffleoRai_zeqer64.SoundTables.BankInfoEntry;
import waffleoRai_zeqer64.extract.RomExtractionSummary;
import waffleoRai_zeqer64.extract.WaveLocIDMap;
import waffleoRai_zeqer64.extract.RomExtractionSummary.ExtractionError;
import waffleoRai_zeqer64.filefmt.NusRomInfo;
import waffleoRai_zeqer64.filefmt.ZeqerBankTable;
import waffleoRai_zeqer64.filefmt.ZeqerPresetTable;
import waffleoRai_zeqer64.filefmt.ZeqerBankTable.BankTableEntry;
import waffleoRai_zeqer64.presets.ZeqerInstPreset;
import waffleoRai_zeqer64.presets.ZeqerPercPreset;

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
	
	public ZeqerBank loadZeqerBank(int bank_uid){
		BankTableEntry entry = null;
		if(bnk_table_sys != null) entry = bnk_table_sys.getBank(bank_uid);
		if(entry != null){
			ZeqerBank bnk = new ZeqerBank(entry, !sys_write_enabled);
			bnk.setDataSaveStem(root_dir + SEP + ZeqerCore.DIRNAME_ZBANK + SEP + entry.getDataPathStem());
			return bnk;
		}
		else{
			if(bnk_table_user != null) entry = bnk_table_user.getBank(bank_uid);
			if(entry != null){
				ZeqerBank bnk = new ZeqerBank(entry, true);
				bnk.setDataSaveStem(root_dir + SEP + entry.getDataPathStem());
				return bnk;
			}
		}
		return null;
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
		
		ZeqerBank bank = new ZeqerBank(entry, true);
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
	
	/*----- ROM Extract -----*/
	
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
		}
	}
	
	public boolean importROMBanks(ZeqerRom z_rom, WaveLocIDMap wavelocs, RomExtractionSummary errorInfo) throws IOException{
		//May be possible to drop wavelocs as input param, loading using z_rom id instead.
		//Would need to assume we go up a dir to the wavs dir from this root though.
		
		if(wavelocs == null) return false;
		if(z_rom == null) return false;
		
		NusRomInfo rominfo = z_rom.getRomInfo();
		if(rominfo == null) return false;
		
		initErrorSummary(errorInfo);
		
		boolean good = true;
		Set<Integer> preset_ids = new TreeSet<Integer>();
		Set<Integer> ref_idxs = new TreeSet<Integer>();
		
		FileBuffer audiobank = z_rom.loadAudiobank(); audiobank.setEndian(true);
		BankInfoEntry[] bank_tbl = z_rom.loadBankEntries();
		int bcount = bank_tbl.length;
		int[] bank_ids = new int[bcount];
		
		final String ZBANK_DIR = root_dir + SEP + ZeqerCore.DIRNAME_ZBANK;
		for(int i = 0; i < bcount; i++){
			int stpos = bank_tbl[i].getOffset();
			int filelen = bank_tbl[i].getSize();
			int icount = bank_tbl[i].getInstrumentCount();
			int pcount = bank_tbl[i].getPercussionCount();
			int xcount = bank_tbl[i].getSFXCount();
			int warc = bank_tbl[i].getPrimaryWaveArcIndex();
			
			if(filelen <= 0){
				ref_idxs.add(i);
				continue;
			}
			if(errorInfo != null) errorInfo.soundfontsFound++; //Don't want to count refs
			
			//Parse bank binary
			FileBuffer bdat = audiobank.createReadOnlyCopy(stpos, stpos+filelen);
			Z64Bank mybank = Z64Bank.readBank(bdat, icount, pcount, xcount);
			bdat.dispose();
			
			//Substitute UIDs for wave offsets
			Map<Integer, Integer> omap = wavelocs.getWArcMap(warc);
			List<Z64WaveInfo> wavelist = mybank.getAllWaveInfoBlocks();
			for(Z64WaveInfo winfo : wavelist){
				int woff = winfo.getWaveOffset();
				Integer mapped = omap.get(woff);
				if(mapped != null){
					winfo.setUID(mapped);
				}
			}
			
			//Re-serialize & Hash
			mybank.setSamplesOrderedByUID(true);
			FileBuffer serbank = new FileBuffer(filelen+1024, true);
			mybank.serializeTo(serbank);
			byte[] md5 = FileUtils.getMD5Sum(serbank.getBytes(0, serbank.getFileSize()));

			//Check for matches
			String md5str = FileUtils.bytes2str(md5);
			BankTableEntry bentry = bnk_table_sys.matchBankMD5(md5str);
			
			if(bentry != null){
				//If match, no need to continue on this one
					//Map UID to index and continue
				bank_ids[i] = bentry.getUID();
				String data_path = ZBANK_DIR + SEP + bentry.getDataFileName();
				if(!FileBuffer.fileExists(data_path)){
					mybank.setUID(bentry.getUID());
					mybank.setMedium(bentry.getMedium());
					mybank.setCachePolicy(bentry.getCachePolicy());
					mybank.setPrimaryWaveArchiveIndex(bentry.getWarcIndex());
					mybank.setSecondaryWaveArchiveIndex(bentry.getSecondaryWarcIndex());
					data_path = ZBANK_DIR + SEP + bentry.getDataPathStem();
					mybank.writeUFormat(data_path);
				}
				
				if(errorInfo != null) errorInfo.soundfontsOkay++;
			}
			else{
				if(sys_write_enabled){
					//If no match, need new entry
					//Create new entry
					bentry = bnk_table_sys.newEntry(md5);
					bentry.setEnumString("AUDIOBANK_" + z_rom.getRomInfo().getZeqerID().toUpperCase() + String.format("_%03d", i));
					bentry.setInstCounts(icount, pcount, xcount);
					bentry.setWarcIndex(warc);
					bentry.setWarc2Index(bank_tbl[i].getSecondaryWaveArcIndex());
					bentry.setMedium(bank_tbl[i].getMedium());
					bentry.setCachePolicy(bank_tbl[i].getCachePolicy());
					
					//Map UID to index
					bank_ids[i] = bentry.getUID();
					
					//Write bank file
					String data_path = ZBANK_DIR + SEP + bentry.getDataFileName();
					if(!FileBuffer.fileExists(data_path)){
						//serbank.writeFile(data_path);
						mybank.setUID(bentry.getUID());
						mybank.setMedium(bentry.getMedium());
						mybank.setCachePolicy(bentry.getCachePolicy());
						mybank.setPrimaryWaveArchiveIndex(bentry.getWarcIndex());
						mybank.setSecondaryWaveArchiveIndex(bentry.getSecondaryWarcIndex());
						data_path = ZBANK_DIR + SEP + bentry.getDataPathStem();
						mybank.writeUFormat(data_path);
					}
					
					if(errorInfo != null){
						errorInfo.soundfontsOkay++;
						errorInfo.soundfontsNew++;
					}
				}
				else{
					//try to add as user font
					System.err.println("CoreBankManager.importROMBanks || WARNING: Bank at index " + i + " count not be matched.");
					if(bnk_table_user != null){
						bentry = bnk_table_user.matchBankMD5(md5str);
						if(bentry != null){
							bank_ids[i] = bentry.getUID();
							String data_path = root_dir + SEP + bentry.getDataFileName();
							if(!FileBuffer.fileExists(data_path)){
								mybank.setUID(bentry.getUID());
								mybank.setMedium(bentry.getMedium());
								mybank.setCachePolicy(bentry.getCachePolicy());
								mybank.setPrimaryWaveArchiveIndex(bentry.getWarcIndex());
								mybank.setSecondaryWaveArchiveIndex(bentry.getSecondaryWarcIndex());
								data_path = root_dir + SEP + bentry.getDataPathStem();
								mybank.writeUFormat(data_path);
							}
						}
						else{
							bentry = bnk_table_user.newEntry(md5);
							bentry.setEnumString("AUDIOBANK_" + z_rom.getRomInfo().getZeqerID().toUpperCase() + String.format("_%03d", i));
							bentry.setInstCounts(icount, pcount, xcount);
							bentry.setWarcIndex(warc);
							bentry.setWarc2Index(bank_tbl[i].getSecondaryWaveArcIndex());
							bentry.setMedium(bank_tbl[i].getMedium());
							bentry.setCachePolicy(bank_tbl[i].getCachePolicy());
							
							//Map UID to index
							bank_ids[i] = bentry.getUID();
							
							//Write bank file
							String data_path = root_dir + SEP + bentry.getDataFileName();
							if(!FileBuffer.fileExists(data_path)){
								mybank.setUID(bentry.getUID());
								mybank.setMedium(bentry.getMedium());
								mybank.setCachePolicy(bentry.getCachePolicy());
								mybank.setPrimaryWaveArchiveIndex(bentry.getWarcIndex());
								mybank.setSecondaryWaveArchiveIndex(bentry.getSecondaryWarcIndex());
								data_path = root_dir + SEP + bentry.getDataPathStem();
								mybank.writeUFormat(data_path);
							}
							
							if(errorInfo != null){
								errorInfo.soundfontsAddedAsCustom++;
							}
						}
					}
					else{
						good = false;
						if(errorInfo != null){
							if(errorInfo.soundfontErrors == null){
								errorInfo.soundfontErrors = new LinkedList<ExtractionError>();
							}
							ExtractionError err = new ExtractionError();
							err.index0 = i;
							err.itemType = RomExtractionSummary.ITEM_TYPE_BNK;
							err.reason = RomExtractionSummary.ERROR_NOT_IN_TABLE;
							err.itemUID = ZeqerUtils.md52UID(md5);
							errorInfo.soundfontErrors.add(err);
						}
					}
				}
			}
			
			//Extract presets...
			//Instruments...
			Collection<Z64Instrument> ilist = mybank.getAllInstruments();
			for(Z64Instrument inst : ilist){
				ZeqerInstPreset preset = new ZeqerInstPreset(inst);
				int hash_uid = preset.hashToUID();
				preset.setUID(hash_uid);
				ZeqerPreset match = preset_table_sys.getPreset(hash_uid);
				if(match == null){
					if(sys_write_enabled){
						//Add to table.
						preset_table_sys.addPreset(preset);
						preset_ids.add(hash_uid);
						if(errorInfo != null) errorInfo.ipresetsNew++;
					}
					else{
						System.err.println("CoreBankManager.importROMBanks || WARNING: Could not match preset 0x" + Integer.toHexString(hash_uid));
						if(preset_table_user != null){
							preset_table_user.addPreset(preset);
							preset_ids.add(hash_uid);
							if(errorInfo != null) errorInfo.ipresetsAddedAsCustom++;
						}
						else good = false;
					}
				}
				else{
					//If it's a dummy, swap it out for one with data.
					preset_ids.add(hash_uid);
					if(!match.hasData()){
						if(sys_write_enabled) {
							preset_table_sys.addPreset(preset);
						}
					}
				}
			}
			
			//Percussion (as a set)
			if(pcount > 0){
				Z64Drum[] drums = mybank.getPercussionSet();
				ZeqerPercPreset ppreset = new ZeqerPercPreset(-1);
				ppreset.setName("perc_" + rominfo.getZeqerID() + String.format("_%02x", i));
				for(int j = 0; j < 64; j++){
					if(drums[j] != null) ppreset.setDrumToSlot(j, drums[j]);
				}
				
				int hash_uid = ppreset.hashToUID();
				ppreset.setUID(hash_uid);
				ZeqerPreset match = preset_table_sys.getPreset(hash_uid);
				if(match == null){
					if(sys_write_enabled){
						//Add to table.
						preset_table_sys.addPreset(ppreset);
						preset_ids.add(hash_uid);
						if(errorInfo != null) errorInfo.ppresetsNew++;
					}
					else{
						System.err.println("CoreBankManager.importROMBanks || WARNING: Could not match preset 0x" + Integer.toHexString(hash_uid));
						if(preset_table_user != null){
							preset_table_user.addPreset(ppreset);
							preset_ids.add(hash_uid);
							if(errorInfo != null) errorInfo.ppresetsAddedAsCustom++;
						}
						else good = false;
					}
				}
				else{
					//If it's a dummy, swap it out for one with data.
					preset_ids.add(hash_uid);
					if(!match.hasData()){
						if(sys_write_enabled) {
							preset_table_sys.addPreset(ppreset);
						}
					}
				}
			}
		}
		
		//Resolve references
		for(Integer ridx : ref_idxs){
			int tidx = bank_tbl[ridx].getOffset();
			bank_ids[ridx] = bank_ids[tidx];
		}

		//Write
		String idtblpath = ZBANK_DIR + SEP + "bnk_" + z_rom.getRomInfo().getZeqerID() + ".bin";
		FileBuffer outbuff = new FileBuffer(bcount << 2, true);
		for(int i = 0; i < bcount; i++){
			outbuff.addToFile(bank_ids[i]);
		}
		outbuff.writeFile(idtblpath);
		
		saveTables();
		return good;
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
