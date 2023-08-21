package waffleoRai_zeqer64.engine;

import java.util.ArrayList;
import java.util.List;

import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_Utils.FileBuffer;

public class EngineSeqInfo {
	
	private int uid = 0; //UID of seq to include
	//If this same ID is further up in list, engine loader will automatically turn into ref

	private ArrayList<Integer> banks;
	private int cachePolicy;
	private int medium = Z64Sound.MEDIUM_CART;
	private int subslot = -1;
	
	/*----- Initialization -----*/
	
	public EngineSeqInfo() {
		banks = new ArrayList<Integer>(4);
	}
	
	/*----- Getters -----*/
	
	public int getSeqUID() {return uid;}
	public int getCachePolicy() {return cachePolicy;}
	public int getMedium() {return medium;}
	public List<Integer> getBankList(){return banks;}
	public int getSubSlot(){return subslot;}
	
	/*----- Setters -----*/
	
	public void setSeqUID(int val) {uid = val;}
	public void setCachePolicy(int val) {cachePolicy = val;}
	public void setMedium(int val) {medium = val;}
	public void setSubSlot(int val){subslot = val;}
	
	public void setBanks(int[] vals){
		banks.clear();
		if(vals == null) return;
		for(int i = 0; i < vals.length; i++) banks.add(vals[i]);
	}
	
	/*----- Serialize -----*/
	
	public FileBuffer serializeMe(){
		int bcount = banks.size();
		FileBuffer buff = new FileBuffer(12+(bcount<<2), true);
		if(subslot >= 0) buff.addToFile(subslot);
		buff.addToFile(uid);
		buff.addToFile((byte)medium);
		buff.addToFile((byte)cachePolicy);
		buff.addToFile((short)bcount);
		for(int buid : banks){
			buff.addToFile(buid);
		}
		return buff;
	}

}
