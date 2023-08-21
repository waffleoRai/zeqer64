package waffleoRai_zeqer64.presets;

import java.util.Random;

import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_soundbank.nintendo.z64.Z64Drum;

public class ZeqerPercRegion {
	
	public static final int PERCREG_MIN_NOTE = Z64Sound.STDRANGE_BOTTOM;
	public static final int PERCREG_MAX_NOTE = Z64Sound.STDRANGE_BOTTOM + Z64Sound.STDRANGE_SIZE - 1;
	
	private byte minNote = PERCREG_MIN_NOTE;
	private byte maxNote = PERCREG_MAX_NOTE;
	private Z64Drum data = null;
	
	private String nameStem = null;
	private String enumStem = null;
	
	public void setRandomStringStems(){
		Random r = new Random();
		int ri = r.nextInt();
		nameStem = String.format("PercReg_%08x", ri);
		enumStem = String.format("PERCREG_%08x", ri);
	}
	
	public byte getMinNote(){return minNote;}
	public byte getMaxNote(){return maxNote;}
	public Z64Drum getDrumData(){return data;}
	public String getNameStem(){return nameStem;}
	public String getEnumStem(){return enumStem;}
	
	public void setMinNote(byte val){
		if(val < PERCREG_MIN_NOTE) val = PERCREG_MIN_NOTE;
		if(val > PERCREG_MAX_NOTE) val = PERCREG_MAX_NOTE;
		minNote = val;
	}
	
	public void setMinNote(int val){
		if(val < PERCREG_MIN_NOTE) val = PERCREG_MIN_NOTE;
		if(val > PERCREG_MAX_NOTE) val = PERCREG_MAX_NOTE;
		minNote = (byte)val;
	}
	
	public void setMaxNote(byte val){
		if(val < PERCREG_MIN_NOTE) val = PERCREG_MIN_NOTE;
		if(val > PERCREG_MAX_NOTE) val = PERCREG_MAX_NOTE;
		maxNote = val;
	}
	
	public void setMaxNote(int val){
		if(val < PERCREG_MIN_NOTE) val = PERCREG_MIN_NOTE;
		if(val > PERCREG_MAX_NOTE) val = PERCREG_MAX_NOTE;
		maxNote = (byte)val;
	}
	
	public void setDrumData(Z64Drum drum){data = drum;}
	public void setNameStem(String val){nameStem = val;}
	public void setEnumStem(String val){enumStem = val;}
	
	public int getMinSlot(){return (int)minNote - PERCREG_MIN_NOTE;}
	public int getMaxSlot(){return (int)maxNote - PERCREG_MIN_NOTE;}
	
	public void setMinSlot(int val){
		if(val < 0) return;
		if(val >= 64) return;
		minNote = (byte)(val + PERCREG_MIN_NOTE);
	}
	
	public void setMaxSlot(int val){
		if(val < 0) return;
		if(val >= 64) return;
		maxNote = (byte)(val + PERCREG_MIN_NOTE);
	}

}
