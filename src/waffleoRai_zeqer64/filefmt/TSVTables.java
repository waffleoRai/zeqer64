package waffleoRai_zeqer64.filefmt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

//Move some common pieces of code for reading TSVs here.

public class TSVTables {
	
	/*----- Instance Variables -----*/
	
	private Map<String, Integer> fieldMap;
	private String[] fieldKeys;
	
	private Map<String, String> lastRecord;
	
	private BufferedReader reader;
	
	/*----- Init -----*/
	
	public TSVTables(String filepath) throws IOException{
		fieldMap = new HashMap<String, Integer>();
		lastRecord = new HashMap<String, String>();
		reader = new BufferedReader(new FileReader(filepath));
		readHeader();
	}
	
	/*----- Getters -----*/
	
	public boolean hasField(String fieldKey){
		return fieldMap.containsKey(fieldKey);
	}
	
	public int getIndexOfField(String fieldKey){
		Integer idx = fieldMap.get(fieldKey);
		if(idx == null) return -1;
		return idx;
	}
	
	public String getValue(String fieldKey){
		return lastRecord.get(fieldKey);
	}
	
	public int getValueAsInt(String fieldKey) throws NumberFormatException{
		String rawval = lastRecord.get(fieldKey);
		if(rawval == null) return 0;
		
		if(rawval.startsWith("0x")){
			return Integer.parseUnsignedInt(rawval.substring(2), 16);
		}
		else{
			return Integer.parseInt(rawval);
		}
	}
	
	public int getValueAsHexInt(String fieldKey) throws NumberFormatException{
		String rawval = lastRecord.get(fieldKey);
		if(rawval == null) return 0;
		if(rawval.startsWith("0x")) rawval = rawval.substring(2);
		
		return Integer.parseUnsignedInt(rawval, 16);
	}
	
	public boolean getValueAsBool(String fieldKey){
		String rawval = lastRecord.get(fieldKey);
		if(rawval == null) return false;
		rawval = rawval.toLowerCase();
		
		if(rawval.equals("y")) return true;
		if(rawval.equals("yes")) return true;
		if(rawval.equals("t")) return true;
		if(rawval.equals("true")) return true;
		if(rawval.equals("1")) return true;
		
		return false;
	}
	
	public String[] getAndSplit(String fieldKey, String delimiter){
		String rawval = lastRecord.get(fieldKey);
		if(rawval == null) return null;
		return rawval.split(delimiter);
	}
	
	/*----- Setters -----*/
	
	/*----- Reading -----*/
	
	private void readHeader() throws IOException{
		String hline = reader.readLine();
		if(hline == null) return;
		if(hline.startsWith("#")){
			hline = hline.substring(1);
		}
		fieldKeys = hline.split("\t");
		if(fieldKeys == null) return;
		for(int i = 0; i < fieldKeys.length; i++){
			fieldMap.put(fieldKeys[i], i);
		}
	}
	
	public boolean nextRecord() throws IOException{
		String rline = reader.readLine();
		if(rline == null) return false;
		
		lastRecord.clear();
		String[] fields = rline.split("\t");
		for(int i = 0; i < fields.length; i++){
			if(i >= fieldKeys.length) break;
			lastRecord.put(fieldKeys[i], fields[i]);
		}
		
		return true;
	}
	
	public void closeReader() throws IOException{
		reader.close();
	}

}
