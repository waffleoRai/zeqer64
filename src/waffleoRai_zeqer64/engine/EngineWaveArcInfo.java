package waffleoRai_zeqer64.engine;

import java.util.ArrayList;
import java.util.List;

import waffleoRai_Sound.nintendo.Z64Sound;

public class EngineWaveArcInfo {
	
	private int ref_idx = -1;
	
	private ArrayList<Integer> samples;
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
	
	public void addSample(int sampleID) {
		if(samples.contains(sampleID)) return;
		samples.add(sampleID);
	}
	
	public void clearSamples() {samples.clear();}


}
