package waffleoRai_zeqer64.filefmt;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

public abstract class RomInfoNode {
	
	protected String zeqer_id;
	protected String md5_str;
	protected String rom_name;
	
	private byte[] md5;
	
	public String getZeqerID(){return zeqer_id;}
	public String getMD5String(){return md5_str;}
	public String getROMName(){return rom_name;}
	public abstract boolean isGamecube();
	
	public List<RomInfoNode> getAllRomNodes(){
		List<RomInfoNode> list = new ArrayList<RomInfoNode>(1);
		list.add(this);
		return list;
	}
	
	protected abstract void readIn(Element xml_element);
	
	public byte[] getMD5Sum(){
		if(md5 != null) return md5;
		md5 = new byte[16];
		for(int i = 0; i < 16; i++){
			int si = i << 1;
			String substr = md5_str.substring(si, si+2);
			md5[i] = Byte.parseByte(substr, 16);
		}
		return md5;
	}
	
	public void printToStderr(int indent){
		StringBuilder sb = new StringBuilder(indent+1);
		for(int i = 0; i < indent; i++) sb.append("\t");
		String tabs = sb.toString();
		
		System.err.println(tabs + "----- RomInfoNode -----");
		System.err.println(tabs + "ZeqerID: " + zeqer_id);
		System.err.println(tabs + "ROM Name: " + rom_name);
		System.err.println(tabs + "MD5Sum: " + md5_str);
	}

}
