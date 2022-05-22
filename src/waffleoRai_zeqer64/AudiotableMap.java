package waffleoRai_zeqer64;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AudiotableMap<T> {

	private ArrayList<Map<Integer, T>> maps;
	
	public AudiotableMap(int arc_count){
		arc_count = Math.max(arc_count, 2);
		maps = new ArrayList<Map<Integer, T>>(arc_count);
		for(int i = 0; i < arc_count; i++){
			maps.add(new HashMap<Integer, T>());
		}
	}
	
	public Map<Integer, T> getMap(int arc_idx){
		return maps.get(arc_idx);
	}
	
	public void copyMappings(int src_idx, int trg_idx){
		Map<Integer, T> src = maps.get(src_idx);
		Map<Integer, T> trg = maps.get(trg_idx);
		trg.putAll(src);
	}
	
}
