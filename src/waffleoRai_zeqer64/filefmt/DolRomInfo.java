package waffleoRai_zeqer64.filefmt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DolRomInfo extends RomInfoNode{
	
	public static final String OOT_KEY = "oot";
	public static final String OOTMQ_KEY = "ootmq";
	public static final String MM_KEY = "mm";
	
	private static class NusNode{
		public String gameid;
		public String file_path;
		public long offset;
		public String zeqer_id;
		
		public NusRomInfo nus_info;
	}
	
	private Map<String, NusNode> children; //Mapped to gameid
	private Map<String, NusNode> idmap; //mapped by zeqer id
	 
	public DolRomInfo(){
		children = new HashMap<String, NusNode>();
		idmap = new HashMap<String, NusNode>();
	}
	
	public boolean isGamecube(){return true;}
	
	public boolean linkNusNode(NusRomInfo node){
		if(node == null) return false;
		NusNode nn = idmap.get(node.zeqer_id);
		if(nn == null) return false;
		nn.nus_info = node;
		return true;
	}
	
	public List<RomInfoNode> getAllRomNodes(){
		List<RomInfoNode> list = new ArrayList<RomInfoNode>(children.size()+1);
		list.add(this);
		for(NusNode nn : children.values()){
			if(nn.nus_info != null) list.add(nn.nus_info);
		}
		return list;
	}
	
	protected void readIn(Element xml_element){
		super.zeqer_id = xml_element.getAttribute("id");
		NodeList list = xml_element.getChildNodes();
		int ccount = list.getLength();
		for(int i = 0; i < ccount; i++){
			Node n = list.item(i);
			if(n.getNodeType() == Node.ELEMENT_NODE){
				Element child = (Element)n;
				String key = child.getNodeName();
				String txt = child.getTextContent();
				txt = txt.replaceAll("\"", "");
				
				if(key.equals("dolrommd5")){
					super.md5_str = txt;
				}
				else if(key.equals("dolromname")){
					super.rom_name = txt;
				}
				else if(key.startsWith("dolootmq")){
					NusNode nn = children.get(OOTMQ_KEY);
					if(nn == null){
						nn = new NusNode();
						children.put(OOTMQ_KEY, nn);
						nn.gameid = OOTMQ_KEY;
					}
					
					if(key.equals("dolootmqpath")){
						nn.file_path = txt;
					}
					else if(key.equals("dolootmqoffset")){
						try{
							if(txt.startsWith("0x")) txt = txt.substring(2);
							nn.offset = Long.parseUnsignedLong(txt, 16);
						}
						catch(NumberFormatException ex){
							ex.printStackTrace();
						}
					}
					else if(key.equals("dolootmqromid")){
						nn.zeqer_id = txt;
						idmap.put(nn.zeqer_id, nn);
					}
				}
				else if(key.startsWith("doloot")){
					NusNode oot = children.get(OOT_KEY);
					if(oot == null){
						oot = new NusNode();
						children.put(OOT_KEY, oot);
						oot.gameid = OOT_KEY;
					}
					
					if(key.equals("dolootpath")){
						oot.file_path = txt;
					}
					else if(key.equals("dolootoffset")){
						try{
							if(txt.startsWith("0x")) txt = txt.substring(2);
							oot.offset = Long.parseUnsignedLong(txt, 16);
						}
						catch(NumberFormatException ex){
							ex.printStackTrace();
						}
					}
					else if(key.equals("dolootromid")){
						oot.zeqer_id = txt;
						idmap.put(oot.zeqer_id, oot);
					}
				}
				else if(key.startsWith("dolmm")){
					NusNode nn = children.get(MM_KEY);
					if(nn == null){
						nn = new NusNode();
						children.put(MM_KEY, nn);
						nn.gameid = MM_KEY;
					}
					
					if(key.equals("dolmmpath")){
						nn.file_path = txt;
					}
					else if(key.equals("dolmmoffset")){
						try{
							if(txt.startsWith("0x")) txt = txt.substring(2);
							nn.offset = Long.parseUnsignedLong(txt, 16);
						}
						catch(NumberFormatException ex){
							ex.printStackTrace();
						}
					}
					else if(key.equals("dolmmromid")){
						nn.zeqer_id = txt;
						idmap.put(nn.zeqer_id, nn);
					}
				}
				
			}
		}
		
	}
	
	public void printToStderr(int indent){
		super.printToStderr(indent);
		StringBuilder sb = new StringBuilder(indent+1);
		for(int i = 0; i < indent; i++) sb.append("\t");
		String tabs = sb.toString();
		
		System.err.println(tabs + "-> DOL ROM Info");
		List<String> keys = new LinkedList<String>();
		keys.addAll(idmap.keySet());
		Collections.sort(keys);
		
		int i = 0;
		for(String key : keys){
			NusNode nn = idmap.get(key);
			System.err.println(tabs + "ROM " + i);
			System.err.println(tabs + "\tGC ROM Path: " + nn.file_path);
			System.err.println(tabs + "\tOffset: 0x" + Long.toHexString(nn.offset));
			if(nn.nus_info != null){
				nn.nus_info.printToStderr(indent+1);
			}
			
			i++;
		}
		
	}

}
