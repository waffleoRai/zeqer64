package waffleoRai_zeqer64.extract;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.FileUtils;
import waffleoRai_soundbank.nintendo.z64.UltraBankFile;
import waffleoRai_soundbank.nintendo.z64.Z64Bank;
import waffleoRai_soundbank.nintendo.z64.Z64Bank.Z64ReadOptions;
import waffleoRai_soundbank.nintendo.z64.Z64Drum;
import waffleoRai_soundbank.nintendo.z64.Z64Instrument;
import waffleoRai_soundbank.nintendo.z64.Z64SoundEffect;
import waffleoRai_zeqer64.SoundTables.BankInfoEntry;
import waffleoRai_zeqer64.ZeqerCore;
import waffleoRai_zeqer64.ZeqerPreset;
import waffleoRai_zeqer64.ZeqerRom;
import waffleoRai_zeqer64.filefmt.NusRomInfo;
import waffleoRai_zeqer64.filefmt.ZeqerBankTable;
import waffleoRai_zeqer64.filefmt.ZeqerBankTable.BankTableEntry;
import waffleoRai_zeqer64.filefmt.ZeqerPresetTable;
import waffleoRai_zeqer64.presets.ZeqerInstPreset;
import waffleoRai_zeqer64.presets.ZeqerPercPreset;
import waffleoRai_zeqer64.presets.ZeqerSFXPreset;

public class BankExtractor {
	
	/*----- Constants -----*/
	
	private static final char SEP = File.separatorChar;
	
	/*----- Instance Variables -----*/
	
	private boolean write_perm; //Determines if sys tables are updated
	
	private String dir_base; //Expecting %PROGRAM_DIR%\bnk\zbnks
	private ZeqerRom z_rom;
	
	private ZeqerBankTable table;
	private ZeqerPresetTable presets;
	
	/*----- Init -----*/
	
	public BankExtractor(ZeqerRom rom) throws IOException{
		this(rom, ZeqerCore.getActiveCore().getProgramDirectory() + SEP + ZeqerCore.DIRNAME_BANK + SEP + ZeqerCore.DIRNAME_ZBANK);
	}
	
	public BankExtractor(ZeqerRom rom, String base_dir) throws IOException{
		this(rom, base_dir, null);
	}
	
	public BankExtractor(ZeqerRom rom, String base_dir, ZeqerBankTable tbl) throws IOException{
		if(rom == null) throw new IllegalArgumentException("ROM is required for extraction.");
		
		dir_base = base_dir;
		table = tbl;
		z_rom = rom;
		
		if(table == null){
			String path = getTablePath();
			if(FileBuffer.fileExists(path)){
				//Load
				try {
					table = ZeqerBankTable.readTable(FileBuffer.createBuffer(path, true));
				}
				catch (UnsupportedFileTypeException e) {
					e.printStackTrace();
					throw new IOException();
				}
			}
			else{
				//New
				table = ZeqerBankTable.createTable();
			}
		}
		
		String path = getPresetTablePath();
		if(FileBuffer.fileExists(path)){
			//Load
			try {
				presets = ZeqerPresetTable.readTable(FileBuffer.createBuffer(path, true));
			}
			catch (UnsupportedFileTypeException e) {
				e.printStackTrace();
				throw new IOException();
			}
		}
		else{
			//New
			presets = ZeqerPresetTable.createTable();
		}
	}
	
	/*----- Paths -----*/
	
	public String getTablePath(){
		return dir_base + SEP + ZeqerCore.FN_SYSBANK;
	}
	
	public String getPresetTablePath(){
		return dir_base + SEP + ZeqerCore.FN_SYSPRESET;
	}
	
	public String getIDTablePath(){
		return dir_base + SEP + "bnk_" + z_rom.getRomInfo().getZeqerID() + ".bin";
	}
	
	/*----- Setters -----*/
	
	public void setSysPermission(boolean b){write_perm = b;}
	
	/*----- Extraction -----*/

	public boolean extractBanks(List<Map<Integer, Integer>> wavloc_map) throws IOException{
		if(wavloc_map == null) return false;
		if(z_rom == null) return false;
		boolean good = true;
		
		Set<Integer> preset_ids = new TreeSet<Integer>();
		Set<Integer> ref_idxs = new TreeSet<Integer>();
		
		NusRomInfo rominfo = z_rom.getRomInfo();
		if(rominfo == null) return false;
		FileBuffer audiobank = z_rom.loadAudiobank(); audiobank.setEndian(true);
		BankInfoEntry[] bank_tbl = z_rom.loadBankEntries();
		int bcount = bank_tbl.length;
		int[] bank_ids = new int[bcount];
		for(int i = 0; i < bcount; i++){
			//System.err.print("DEBUG Reading bank " + i + " --");
			//Read the bank
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
			
			FileBuffer bdat = audiobank.createReadOnlyCopy(stpos, stpos+filelen);
			//Z64Bank mybank = Z64Bank.readBank(bdat, icount, pcount, xcount);
			Z64ReadOptions op = Z64Bank.genOptionsForKnownCounts(icount, pcount, xcount);
			Z64Bank mybank = Z64Bank.readRaw(bdat, op);
			bdat.dispose();
			
			//Substitute wave offsets
			Map<Integer, Integer> omap = wavloc_map.get(warc);
			if(omap.isEmpty()){
				//Very specific to index 1 but I'll make it better later
				omap = wavloc_map.get(warc-1);
			}
			List<Z64WaveInfo> wavelist = mybank.getAllWaveBlocks();
			for(Z64WaveInfo winfo : wavelist){
				int woff = winfo.getWaveOffset();
				//System.err.println("woff = 0x" + Integer.toHexString(woff));
				Integer mapped = omap.get(woff);
				if(mapped != null){
					winfo.setUID(mapped);
					//System.err.println("matched to UID 0x" + Integer.toHexString(mapped));
				}
			}
			
			//Re-serialize & Hash
			/*mybank.setSamplesOrderedByUID(true);
			FileBuffer serbank = new FileBuffer(filelen+1024, true);
			mybank.serializeTo(serbank);
			byte[] md5 = FileUtils.getMD5Sum(serbank.getBytes(0, serbank.getFileSize()));*/
			FileBuffer serbank = mybank.serializeMe(Z64Bank.SEROP_REF_WAV_UIDS);
			byte[] md5 = FileUtils.getMD5Sum(serbank.getBytes(0, serbank.getFileSize()));

			//Check for matches
			String md5str = FileUtils.bytes2str(md5);
			BankTableEntry bentry = table.matchBankMD5(md5str);

			if(bentry != null){
				//If match, no need to continue on this one
					//Map UID to index and continue
				bank_ids[i] = bentry.getUID();
				String data_path = dir_base + File.separator + bentry.getDataFileName();
				if(!FileBuffer.fileExists(data_path)){
					//serbank.writeFile(data_path);
					mybank.setUID(bentry.getUID());
					mybank.setMedium(bentry.getMedium());
					mybank.setCachePolicy(bentry.getCachePolicy());
					mybank.setPrimaryWaveArcIndex(bentry.getWarcIndex());
					mybank.setSecondaryWaveArcIndex(bentry.getSecondaryWarcIndex());
					data_path = dir_base + File.separator + bentry.getDataPathStem();
					//mybank.writeUFormat(data_path);
					UltraBankFile.writeUBNK(mybank, data_path, UltraBankFile.OP_LINK_WAVES_UID);
					if(mybank.getEffectiveSFXCount() > 0){
						UltraBankFile.writeUWSD(mybank, data_path, UltraBankFile.OP_LINK_WAVES_UID);
					}
				}
			}
			else{
				if(write_perm){
					//If no match, need new entry
					//Create new entry
					bentry = table.newEntry(md5);
					bentry.setEnumString("AUDIOBANK_" + z_rom.getRomInfo().getZeqerID().toUpperCase() + String.format("_%03d", i));
					bentry.setInstCounts(icount, pcount, xcount);
					bentry.setWarcIndex(warc);
					bentry.setWarc2Index(bank_tbl[i].getSecondaryWaveArcIndex());
					bentry.setMedium(bank_tbl[i].getMedium());
					bentry.setCachePolicy(bank_tbl[i].getCachePolicy());
					
					//Map UID to index
					bank_ids[i] = bentry.getUID();
					
					//Write bank file
					String data_path = dir_base + File.separator + bentry.getDataFileName();
					if(!FileBuffer.fileExists(data_path)){
						//serbank.writeFile(data_path);
						mybank.setUID(bentry.getUID());
						mybank.setMedium(bentry.getMedium());
						mybank.setCachePolicy(bentry.getCachePolicy());
						mybank.setPrimaryWaveArcIndex(bentry.getWarcIndex());
						mybank.setSecondaryWaveArcIndex(bentry.getSecondaryWarcIndex());
						data_path = dir_base + File.separator + bentry.getDataPathStem();
						//mybank.writeUFormat(data_path);
						UltraBankFile.writeUBNK(mybank, data_path, UltraBankFile.OP_LINK_WAVES_UID);
						if(mybank.getEffectiveSFXCount() > 0){
							UltraBankFile.writeUWSD(mybank, data_path, UltraBankFile.OP_LINK_WAVES_UID);
						}
					}
				}
				else{
					good = false;
					System.err.println("BankExtractor.extractBanks || WARNING: Bank at index " + i + " count not be matched.");
				}
			}
			
			//Extract presets...
			//Instruments...
			Collection<Z64Instrument> ilist = mybank.getAllUniqueInstruments();
			//System.err.println("DEBUG: ilist size: " + ilist.size());
			for(Z64Instrument inst : ilist){
				ZeqerInstPreset preset = new ZeqerInstPreset(inst);
				int hash_uid = preset.hashToUID();
				preset.setUID(hash_uid);
				ZeqerPreset match = presets.getPreset(hash_uid);
				if(match == null){
					if(write_perm){
						//Add to table.
						presets.addPreset(preset);
						System.err.println("BankExtractor.extractBanks || New Inst Preset: 0x" + 
								Integer.toHexString(hash_uid) + " from bank " + i + ", inst " + inst.getName());
						preset_ids.add(hash_uid);
					}
					else{
						good = false;
						System.err.println("BankExtractor.extractBanks || WARNING: Could not match preset 0x" + Integer.toHexString(hash_uid));
					}
				}
				else{
					//If it's a dummy, swap it out for one with data.
					preset_ids.add(hash_uid);
					if(!match.hasData()){
						presets.addPreset(preset);
						System.err.println("BankExtractor.extractBanks || Data loaded for Inst Preset: 0x" + 
								Integer.toHexString(hash_uid) + " from bank " + i + ", inst " + inst.getName());
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
				ZeqerPreset match = presets.getPreset(hash_uid);
				if(match == null){
					if(write_perm){
						//Add to table.
						presets.addPreset(ppreset);
						System.err.println("BankExtractor.extractBanks || New Perc Preset: 0x" + 
								Integer.toHexString(hash_uid) + " from bank " + i);
						preset_ids.add(hash_uid);
					}
					else{
						good = false;
						System.err.println("BankExtractor.extractBanks || WARNING: Could not match preset 0x" + Integer.toHexString(hash_uid));
					}
				}
				else{
					//If it's a dummy, swap it out for one with data.
					preset_ids.add(hash_uid);
					if(!match.hasData()){
						presets.addPreset(ppreset);
						System.err.println("BankExtractor.extractBanks || Data loaded for Perc Preset: 0x" + 
								Integer.toHexString(hash_uid) + " from bank " + i);
					}
				}
			}
			
			//SFX (as sets of 64)
			if(xcount > 0){
				Z64SoundEffect[] sfx = mybank.getSFXSet();
				int j = 0;
				int groups = (sfx.length/64) + 1;
				for(int g = 0; g < groups; g++){
					ZeqerSFXPreset spreset = new ZeqerSFXPreset(0);
					spreset.setName("sfx_" + rominfo.getZeqerID() + String.format("_%02x_%02d", i, g));
					
					for(int k = 0; k < 64; k++){
						if(j < sfx.length && sfx[j] != null){
							spreset.setWaveID(k, sfx[j].getSample().getUID());
							spreset.setTuningValue(k, sfx[j].getTuning());
						}
						j++;
					}
					
					int hash_uid = spreset.hashToUID();
					spreset.setUID(hash_uid);
					ZeqerPreset match = presets.getPreset(hash_uid);
					if(match == null){
						if(write_perm){
							//Add to table.
							presets.addPreset(spreset);
							System.err.println("BankExtractor.extractBanks || New SFX Preset: 0x" + 
									Integer.toHexString(hash_uid) + " from bank " + i + ", group " + g);
							preset_ids.add(hash_uid);
						}
						else{
							good = false;
							System.err.println("BankExtractor.extractBanks || WARNING: Could not match preset 0x" + Integer.toHexString(hash_uid));
						}
					}
					else{
						//If it's a dummy, swap it out for one with data.
						preset_ids.add(hash_uid);
						if(!match.hasData()){
							presets.addPreset(spreset);
							System.err.println("BankExtractor.extractBanks || Data loaded for SFX Preset: 0x" + 
									Integer.toHexString(hash_uid) + " from bank " + i + ", group " + g);
						}
					}
				}
			}
		}
		
		//DEBUG
		//System.err.println("Presets Found in ROM:");
		//for(Integer id : preset_ids) System.err.println(String.format("%08x", id));
		
		//Resolve references
		for(Integer ridx : ref_idxs){
			int tidx = bank_tbl[ridx].getOffset();
			bank_ids[ridx] = bank_ids[tidx];
		}

		//Write
		String idtblpath = getIDTablePath();
		FileBuffer outbuff = new FileBuffer(bcount << 2, true);
		for(int i = 0; i < bcount; i++){
			outbuff.addToFile(bank_ids[i]);
		}
		outbuff.writeFile(idtblpath);
		
		if(write_perm){
			table.writeTo(getTablePath());
		}
		//Always written in order to update dummies
		presets.writeTo(getPresetTablePath());
		
		return good;
	}
	
}
