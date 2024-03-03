package waffleoRai_zeqer64.extract;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import waffleoRai_zeqer64.filefmt.wave.VersionWaveTable;
import waffleoRai_zeqer64.filefmt.wave.ZeqerWaveTable;

public class WaveLocIDMap {
	
	private ArrayList<LocIDMap> sampleBanks;
	
	private static class LocIDMap{
		public int refId = -1;
		public Map<Integer, Integer> map;
		public LocIDMap(int ref){refId = ref;}
		public LocIDMap(){map = new HashMap<Integer, Integer>();}
	}
	
	public WaveLocIDMap(int warcCount){
		if(warcCount < 4) warcCount = 4;
		sampleBanks = new ArrayList<LocIDMap>(warcCount);
	}
	
	public int getWArcCount(){
		if(sampleBanks == null) return 0;
		return sampleBanks.size();
	}
	
	public static WaveLocIDMap readVersionMap(String path) throws IOException{
		VersionWaveTable tbl = ZeqerWaveTable.loadVersionTable(path);
		int warc_count = tbl.getWArcCount();
		WaveLocIDMap output = new WaveLocIDMap(warc_count);
		
		for(int i = 0; i < warc_count; i++){
			int refId = tbl.getWArcReference(i);
			if(refId < 0){
				LocIDMap map = new LocIDMap();
				output.sampleBanks.add(map);
				
				int scount = tbl.getWArcSampleCount(i);
				for(int j = 0; j < scount; j++){
					int[] sinfo = tbl.getSampleInfo(i, j);
					map.map.put(sinfo[1], sinfo[0]);
				}
			}
			else{
				LocIDMap map = new LocIDMap(refId);
				output.sampleBanks.add(map);
				map.refId = refId;
			}
		}
		
		return output;
	}
	
	public Map<Integer, Integer> getWArcMap(int index){
		if(sampleBanks == null) return null;
		if(index < 0) return null;
		if(index >= sampleBanks.size()) return null;
		
		LocIDMap bmap = sampleBanks.get(index);
		while(bmap.refId >= 0){
			bmap = sampleBanks.get(bmap.refId);
		}
		
		return bmap.map;
	}

}
