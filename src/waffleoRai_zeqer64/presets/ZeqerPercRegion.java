package waffleoRai_zeqer64.presets;

import java.util.Random;

import waffleoRai_soundbank.nintendo.z64.Z64Drum;

public class ZeqerPercRegion {
	
	private byte minNote = 0;
	private byte maxNote = 127;
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
	
	public void setMinNote(byte val){minNote = val;}
	public void setMinNote(int val){minNote = (byte)val;}
	public void setMaxNote(byte val){maxNote = val;}
	public void setMaxNote(int val){maxNote = (byte)val;}
	public void setDrumData(Z64Drum drum){data = drum;}
	public void setNameStem(String val){nameStem = val;}
	public void setEnumStem(String val){enumStem = val;}

}
