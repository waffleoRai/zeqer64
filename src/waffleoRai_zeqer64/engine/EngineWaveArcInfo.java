package waffleoRai_zeqer64.engine;

import java.util.ArrayList;
import java.util.List;

import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_Utils.BinFieldSize;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_zeqer64.filefmt.RecompFiles;

public class EngineWaveArcInfo {
	
	private int ref_idx = -1;
	
	private ArrayList<Integer> samples;
	private String name;
	private String enm_str;
	private int cachePolicy;
	private int medium = Z64Sound.MEDIUM_CART;
	
	/*----- Initialization -----*/
	
	public EngineWaveArcInfo(int salloc) {
		if(salloc <= 0) salloc = 16;
		samples = new ArrayList<Integer>(salloc);
	}
	
	/*----- Getters -----*/
	
	public int getCachePolicy() {return cachePolicy;}
	public int getMedium() {return medium;}
	public int countSamples() {return samples.size();}
	public String getName(){return name;}
	public String getEnumString(){return enm_str;}
	
	public boolean isReference() {return ref_idx >= 0;}
	public int getRefIndex() {return ref_idx;}
	
	public List<Integer> getSamples(){
		ArrayList<Integer> list = new ArrayList<Integer>(samples.size()+1);
		list.addAll(samples);
		return list;
	}
	
	/*----- Setters -----*/
	
	public void setCachePolicy(int val) {cachePolicy = val;}
	public void setMedium(int val) {medium = val;}
	public void setRefIndex(int val) {ref_idx = val;}
	public void setName(String val){name = val;}
	public void setEnumString(String s){enm_str = s;}
	
	public void addSample(int sampleID) {
		if(samples.contains(sampleID)) return;
		samples.add(sampleID);
	}
	
	public void clearSamples() {samples.clear();}
	
	public void updateCapacity(int capacity){
		samples.ensureCapacity(capacity);
	}

	/*----- Serialize -----*/
	
	public FileBuffer serializeMe(){
			
		boolean isref = isReference();
		int alloc_size = 6;
		if(!isref) alloc_size += samples.size() << 2;
		if(name == null){
			name = "uwar_" + ref_idx;
		}
		alloc_size += name.length() + 3;
		if(enm_str == null){
			enm_str = RecompFiles.genEnumStrFromName(name);
		}
		alloc_size += enm_str.length() + 3;
		
		FileBuffer buff = new FileBuffer(alloc_size, true);
		if(isref){
			int ref = 0x8000;
			ref |= ref_idx & 0x7fff;
			buff.addToFile((short)ref);
		}
		else buff.addToFile((short)samples.size());
		buff.addVariableLengthString("UTF8", name, BinFieldSize.WORD, 2);
		buff.addVariableLengthString("UTF8", enm_str, BinFieldSize.WORD, 2);
		buff.addToFile((byte)medium);
		buff.addToFile((byte)cachePolicy);
		if(!isref){
			for(Integer wid : samples) buff.addToFile(wid);
		}
		
		return buff;
	}

}
