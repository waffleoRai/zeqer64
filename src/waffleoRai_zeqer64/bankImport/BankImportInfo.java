package waffleoRai_zeqer64.bankImport;

import waffleoRai_Sound.nintendo.Z64Sound;

public class BankImportInfo {

	/*----- Constants -----*/
	
	public static final int MIN_SAMPLE_RATE_NOWARN = 10000;
	
	public static final int SAMPLE_WARNING_LOWSR = 1 << 0; //Sample rate is very low. May not compress well.
	
	public static final int PRESET_WARNING_REGCOUNT = 1 << 0; //More than 3 regions. Will need to collapse.
	public static final int PRESET_WARNING_INCOMP_MODS = 1 << 1; //Contains incompatible modulation specs.
	
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
		
		public boolean emptySlot = true;
		public int warningFlags = 0;
		
		public void copyTo(PresetInfo other){
			if(other == null) return;
			other.index = this.index;
			other.name = this.name;
			other.importToFont = this.importToFont;
			other.savePreset = this.savePreset;
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
		public int percInst = -1; //Preset index
		public boolean importPercToFont = false;
		public boolean saveDrumset = false;
		public boolean saveDrums = false;
		
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
			other.percInst = this.percInst;
			other.importPercToFont = this.importPercToFont;
			other.saveDrumset = this.saveDrumset;
			other.saveDrums = this.saveDrums;
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
			return String.format("%03d - %s", bankIndex, name);
		}
		
	}
	
	/*----- Instance Variables -----*/
	
	private SubBank[] banks;
	private SampleInfo[] samples;
	
	/*----- Init -----*/
	
	public BankImportInfo(int bankAlloc, int sampleAlloc){
		allocBanks(bankAlloc);
		allocSamples(sampleAlloc);
	}
	
	public void allocBanks(int alloc){
		if(alloc < 1){
			banks = null;
			return;
		}
		banks = new SubBank[alloc];
		for(int i = 0; i < alloc; i++){
			newBank(i);
		}
	}
	
	private void newBank(int index){
		banks[index] = new SubBank();
		banks[index].bankIndex = index;
		banks[index].name = String.format("Bank %05d", index);
		banks[index].enumLabel = String.format("SFBANK_%05d", index);
		banks[index].allocInstruments(128);
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
			other.banks = new SubBank[this.banks.length];
			for(int i = 0; i < banks.length; i++){
				if(banks[i] != null){
					other.banks[i] = this.banks[i].copy();
				}
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
		return banks.length;
	}
	
	public SubBank getBank(int index){
		if(banks == null) return null;
		if(index < 0 || index >= banks.length) return null;
		return banks[index];
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
	
	/*----- Setters -----*/
	
	
	
}
