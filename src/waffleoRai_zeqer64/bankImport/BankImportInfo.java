package waffleoRai_zeqer64.bankImport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import waffleoRai_Sound.nintendo.Z64Sound;

public class BankImportInfo {

	/*----- Constants -----*/
	
	public static final int MIN_SAMPLE_RATE_NOWARN = 10000;
	
	public static final int SAMPLE_WARNING_LOWSR = 1 << 0; //Sample rate is very low. May not compress well.
	
	public static final int PRESET_WARNING_REGCOUNT = 1 << 0; //More than 3 regions (inst). Will need to collapse.
	public static final int PRESET_WARNING_INCOMP_MODS = 1 << 1; //Contains incompatible modulation specs.
	public static final int PRESET_WARNING_REGCOUNT_PERC = 1 << 2; //Perc preset contains drum assignments outside 64 note range
	public static final int PRESET_WARNING_BAD_SLOT = 1 << 3; //Instrument preset sits in slot 126 or 127. Will be moved or cut.
	public static final int PRESET_WARNING_PERC_AS_INST = 1 << 4; //Preset marked as percussion in source is in instrument slot for import
	
	/*----- Inner Classes -----*/
	
	public static class SampleInfo{
		public int id;
		public String name;
		
		public boolean importSample = true;
		public int warningFlags = 0;
		
		public SampleInfo copy(){
			SampleInfo mycopy = new SampleInfo();
			this.copyTo(mycopy);
			return mycopy;
		}
		
		public void copyTo(SampleInfo other){
			if(other == null) return;
			other.id = this.id;
			other.name = this.name;
			other.importSample = this.importSample;
			other.warningFlags = this.warningFlags;
		}
		
		public String toString(){
			return String.format("%x - %s", id, name);
		}
	}
	
	public static class PresetInfo{
		
		public int index;
		public String name;
		
		public boolean importToFont = true;
		public boolean savePreset = false;
		public boolean percInSrc = false; //Is marked percussion in source file
		public boolean saveDrums = false;
		
		public boolean emptySlot = true;
		public int warningFlags = 0;
		public int movedToIndex = -1; //If in slot 126 or 127
		
		public List<String> incompModNames = new LinkedList<String>();
		
		public void copyTo(PresetInfo other){
			if(other == null) return;
			other.index = this.index;
			other.name = this.name;
			other.emptySlot = this.emptySlot;
			other.warningFlags = this.warningFlags;
			other.importToFont = this.importToFont;
			other.savePreset = this.savePreset;
			other.percInSrc = this.percInSrc;
			other.saveDrums = this.saveDrums;
			other.movedToIndex = this.movedToIndex;
		}
		
		public PresetInfo copy(){
			PresetInfo mycopy = new PresetInfo();
			this.copyTo(mycopy);
			return mycopy;
		}
		
		public String toString(){
			return String.format("%03d - %s", index, name);
		}
	}
	
	public static class SubBank{
		//For example, SF2 files can have multiple banks.
		
		//Metadata
		public String name;
		public String enumLabel;
		public int cache = Z64Sound.CACHE_TEMPORARY;
		public int medium = Z64Sound.MEDIUM_CART;
		
		//Options
		public boolean importAsFont = true;
		
		//Contents
		public int bankIndex;
		public PresetInfo[] instruments;
		
		//Perc
		public int percBank = -1; //If from other bank
		public int percInst = -1; //Preset index
		public boolean importPercToFont = false;
		public boolean saveDrumset = false;
		public boolean saveDrums = false;
		
		//SFX
		public int sfxBank = -1; //If from other bank
		public int sfxInst = -1; //Preset index
		public boolean importSfxToFont = false;
		public boolean saveSfx = false;
		
		public void allocInstruments(int count){
			if(count < 1){
				instruments = null;
				return;
			}
			instruments = new PresetInfo[count];
			for(int i = 0; i < count; i++){
				newInstrument(i);
			}
		}
		
		private void newInstrument(int index){
			PresetInfo inst = new PresetInfo();
			inst.index = index;
			inst.name = String.format("Preset %03d", index);
			instruments[index] = inst;
		}
		
		public void clearInstrumentSlot(int index){
			if(instruments == null) return;
			if(index < 0 || index >= instruments.length) return;
			instruments[index] = null;
		}
		
		public void copyTo(SubBank other){
			if(other == null) return;
			other.name = this.name;
			other.enumLabel = this.enumLabel;
			other.cache = this.cache;
			other.medium = this.medium;
			other.importAsFont = this.importAsFont;
			other.bankIndex = this.bankIndex;
			other.percBank = this.percBank;
			other.percInst = this.percInst;
			other.importPercToFont = this.importPercToFont;
			other.saveDrumset = this.saveDrumset;
			other.saveDrums = this.saveDrums;
			other.sfxBank = this.sfxBank;
			other.sfxInst = this.sfxInst;
			other.importSfxToFont = this.importSfxToFont;
			other.saveSfx = this.saveSfx;
			if(this.instruments != null){
				other.instruments = new PresetInfo[this.instruments.length];
				for(int i = 0; i < this.instruments.length; i++){
					if(this.instruments[i] != null){
						other.instruments[i] = this.instruments[i].copy();
					}
				}
			}
			else{
				other.instruments = null;
			}
		}
		
		public SubBank copy(){
			SubBank mycopy = new SubBank();
			this.copyTo(mycopy);
			return mycopy;
		}
		
		public String toString(){
			return String.format("0x%04x - %s", bankIndex, name);
		}
		
	}
	
	/*----- Instance Variables -----*/
	
	//private SubBank[] banks;
	private Map<Integer, SubBank> banks; //IDs are not necessarily index
	private SampleInfo[] samples;
	
	/*----- Init -----*/
	
	public BankImportInfo(int sampleAlloc){
		allocBanks(0); //Just instantiates map
		allocSamples(sampleAlloc);
	}
	
	public BankImportInfo(int bankAlloc, int sampleAlloc){
		allocBanks(bankAlloc);
		allocSamples(sampleAlloc);
	}
	
	public void allocBanks(int alloc){
		banks = new HashMap<Integer, SubBank>();
		if(alloc < 1){
			return;
		}
		//banks = new SubBank[alloc];
		for(int i = 0; i < alloc; i++){
			newBank(i);
		}
	}
	
	public SubBank newBank(int index){
		SubBank bank = new SubBank();
		bank = new SubBank();
		bank.bankIndex = index;
		//bank.name = String.format("Bank %05d", index);
		//bank.enumLabel = String.format("SFBANK_%05d", index);
		bank.name = String.format("Bank %04x", index);
		bank.enumLabel = String.format("SFBANK_%04x", index);
		bank.allocInstruments(128);
		banks.put(index, bank);
		return bank;
	}
	
	public void allocSamples(int alloc){
		if(alloc < 1){
			samples = null;
			return;
		}
		samples = new SampleInfo[alloc];
		for(int i = 0; i < alloc; i++){
			newSample(i);
		}
	}
	
	private void newSample(int index){
		SampleInfo smpl = new SampleInfo();
		smpl.id = index;
		smpl.name = String.format("Sample %d", index);
		samples[index] = smpl;
	}
	
	/*----- Getters -----*/
	
	public void copyTo(BankImportInfo other){
		if(banks != null){
			/*other.banks = new SubBank[this.banks.length];
			for(int i = 0; i < banks.length; i++){
				if(banks[i] != null){
					other.banks[i] = this.banks[i].copy();
				}
			}*/
			other.allocBanks(0);
			for(SubBank bank : this.banks.values()) {
				SubBank bankCopy = bank.copy();
				other.banks.put(bankCopy.bankIndex, bankCopy);
			}
		}
		else other.banks = null;
		
		if(samples != null){
			other.samples = new SampleInfo[this.samples.length];
			for(int i = 0; i < samples.length; i++){
				if(samples[i] != null){
					other.samples[i] = this.samples[i].copy();
				}
			}
		}
		else other.samples = null;
	}
	
	public BankImportInfo copy(){
		BankImportInfo mycopy = new BankImportInfo(0,0);
		this.copyTo(mycopy);
		return mycopy;
	}
	
	public int getBankCount(){
		if(banks == null) return 0;
		//return banks.length;
		return banks.size();
	}
	
	public SubBank getBank(int id){
		if(banks == null) return null;
		return banks.get(id);
	}
	
	public int getSampleCount(){
		if(samples == null) return 0;
		return samples.length;
	}
	
	public SampleInfo getSample(int index){
		if(samples == null) return null;
		if(index < 0 || index >= samples.length) return null;
		return samples[index];
	}
	
	public List<Integer> getAllBankIds(){
		if(banks == null) return new LinkedList<Integer>();
		List<Integer> list = new ArrayList<Integer>(banks.size()+1);
		list.addAll(banks.keySet());
		Collections.sort(list);
		return list;
	}
	
	/*----- Setters -----*/
	
	
	
}
