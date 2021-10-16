package waffleoRai_zeqer64;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import waffleoRai_Containers.nintendo.nus.N64ROMImage;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileUtils;
import waffleoRai_zeqer64.filefmt.RomInfoNode;

public class RomDetector {

	/*----- Constants -----*/
	
	/*----- Instance Variables -----*/
	
	private Map<String, RomInfoNode> md5_map;
	
	/*----- Init -----*/
	
	public RomDetector(){
		md5_map = new HashMap<String, RomInfoNode>();
	}
	
	/*----- Process -----*/
	
	public RomInfoNode detectRomVersion(String rompath) throws IOException{
		if(rompath == null) return null;
		FileBuffer romdat = N64ROMImage.loadROMasZ64(rompath);
		
		byte[] md5 = FileUtils.getMD5Sum(romdat.getBytes());
		String md5str = FileUtils.bytes2str(md5).toLowerCase();
		//System.err.println("RomDetector.detectRomVersion || DEBUG -- MD5: " + md5str);
		
		return matchRomByMD5(md5str);
	}
	
	/*----- Getters -----*/
	
	public RomInfoNode matchRomByMD5(String md5str){
		return md5_map.get(md5str);
	}
	
	/*----- Setters -----*/
	
	public void mapRomInfo(RomInfoNode info){
		md5_map.put(info.getMD5String(), info);
	}
	
	public void clearMap(){
		md5_map.clear();
	}
	
}
