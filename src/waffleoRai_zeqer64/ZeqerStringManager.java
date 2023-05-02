package waffleoRai_zeqer64;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import waffleoRai_Files.FileBufferInputStream;
import waffleoRai_Utils.FileBuffer;

public class ZeqerStringManager {
	
	public static final String LAN_CODE_ENGLISH = "eng";
	
	private String loadedLan;
	private Map<String, String> strmap;
	
	private String root_dir; //Usually ${zeqerdir}/str
	
	public ZeqerStringManager(){
		strmap = new HashMap<String, String>();
		root_dir = null;
	}
	
	public ZeqerStringManager(String dir){
		strmap = new HashMap<String, String>();
		root_dir = dir;
	}
	
	public String getLoadedLanguageCode(){
		return loadedLan;
	}
	
	public String getString(String key){
		String s = strmap.get(key);
		if(s != null){
			return performPlaceholderSubstitutions(s);
		}
		return null;
	}
	
	public String performPlaceholderSubstitutions(String input){
		//%U -> Username (from system)
		if(input == null) return null;
		
		String output = input;
		if(output.contains("%U")){
			String username = System.getProperty("user.name");
			output = output.replace("%U", username);
		}
		
		return output;
	}
	
	public boolean loadLanguage(String lankey) throws IOException{
		if(lankey == null){
			clear();
			return true;
		}
		
		if(lankey.equals(loadedLan)) return true;
		
		//Install mode, read from resources
		FileBuffer dec = null;
		if(root_dir == null){
			String respath = ZeqerCore.RES_JARPATH + "/" + ZeqerCore.DIRNAME_STRING + "/guistr_" + lankey + ".txt.jdfl";
			try{
				InputStream jarstr = ZeqerCore.class.getResourceAsStream(respath);
				if(jarstr != null){
					dec = ZeqerUtils.inflate(jarstr);
					jarstr.close();
				}
			}
			catch(Exception ex){
				ex.printStackTrace();
				return false;
			}
		}
		else{
			//Find file
			String fpath = root_dir + File.separator + "guistr_" + lankey + ".txt.jdfl";
			if(!FileBuffer.fileExists(fpath)) return false;
			
			FileBuffer buffer = FileBuffer.createBuffer(fpath, true);
			dec = ZeqerUtils.inflate(buffer);
			buffer = null;
		}
		
		
		if(dec == null) return false;
		strmap.clear();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileBufferInputStream(dec)));
		String line = null;
		while((line = br.readLine()) != null){
			int eqidx = line.indexOf('=');
			if(eqidx < 0) continue;
			String key = line.substring(0, eqidx).trim();
			String value = line.substring(eqidx+1).trim();
			if(value.startsWith("\"")) value = value.substring(1);
			if(value.endsWith("\"")) value = value.substring(0, value.length()-1);
			//Perform reserved character replacements before value
			value.replace("\\n", "\n");
			strmap.put(key, value);
		}
		br.close();
		
		loadedLan = lankey;
		dec = null;
		return true;
	}
	
	public void clear(){
		loadedLan = null;
		strmap.clear();
	}

}
