package waffleoRai_zeqer64.engine;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_zeqer64.SoundTables;

public class EngineBankInfo {
	
	/*----- Instance Variables -----*/
	
	private int uid = 0; //UID of bank to include
	//If this same ID is further up in list, engine loader will automatically turn into ref
	
	//Yes, these are included in the Z64Bank, but need to be specific to this engine load
	private int warc1 = 0;
	private int warc2 = -1;
	private int cachePolicy = SoundTables.CACHE__TEMPORARY;
	private int medium = SoundTables.MEDIUM_CART;
	private int subslot = -1;
	
	/*----- Initialization -----*/
	
	/*----- Getters -----*/
	
	public int getBankUID() {return uid;}
	public int getWArc1() {return warc1;}
	public int getWArc2() {return warc2;}
	public int getCachePolicy() {return cachePolicy;}
	public int getMedium() {return medium;}
	public int getSubSlot(){return subslot;}
	
	/*----- Setters -----*/
	
	public void setBankUID(int val) {uid = val;}
	public void setWArc1(int val) {warc1 = val;}
	public void setWArc2(int val) {warc2 = val;}
	public void setCachePolicy(int val) {cachePolicy = val;}
	public void setMedium(int val) {medium = val;}
	public void setSubSlot(int val){subslot = val;}

	/*----- Serialize -----*/
	
	public FileBuffer serializeMe(){
		FileBuffer buff = new FileBuffer(12, true);
		if(subslot >= 0) buff.addToFile(subslot);
		buff.addToFile(uid);
		buff.addToFile((byte)medium);
		buff.addToFile((byte)cachePolicy);
		buff.addToFile((byte)warc1);
		buff.addToFile((byte)warc2);
		return buff;
	}
	
}
