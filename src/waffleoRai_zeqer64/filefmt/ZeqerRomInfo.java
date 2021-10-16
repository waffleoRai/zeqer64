package waffleoRai_zeqer64.filefmt;

import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ZeqerRomInfo {
	
	public static final String ROOT_NODE_KEY = "ZeqerRomInfo";
	public static final String DOLROM_KEY = "Dolrom";
	public static final String NUSROM_KEY = "Nusrom";

	private RomInfoNode head_node;
	
	private ZeqerRomInfo(){}
	
	public List<RomInfoNode> getAllRomNodes(){
		if(head_node == null) return new LinkedList<RomInfoNode>();
		return head_node.getAllRomNodes();
	}
	
	public static ZeqerRomInfo readXML(String xmlpath){
		ZeqerRomInfo zri = new ZeqerRomInfo();
		try{
			Document xml = XMLReader.readXMLStatic(xmlpath);
			
			String rootname = xml.getDocumentElement().getNodeName();
			if(!rootname.equals(ROOT_NODE_KEY)){
				System.err.println("Root Node must be: " + ROOT_NODE_KEY);
				return null;
			}
			
			//Look for dol nodes. If present, may be multiple nus nodes
			NodeList nl = xml.getElementsByTagName(DOLROM_KEY);
			if(nl != null && (nl.getLength() > 0)){
				Node node = nl.item(0);
				DolRomInfo dolnode = new DolRomInfo();
				zri.head_node = dolnode;
				if(node.getNodeType() == Node.ELEMENT_NODE){
					Element e = (Element)node;
					dolnode.readIn(e);
				}
				
				//Get NUS roms and map to id
				nl = xml.getElementsByTagName(NUSROM_KEY);
				if(nl != null){
					int count = nl.getLength();	
					for(int i = 0; i < count; i++){
						node = nl.item(i);
						if(node.getNodeType() == Node.ELEMENT_NODE){
							Element e = (Element)node;
							NusRomInfo nusnode = new NusRomInfo();
							nusnode.readIn(e);
							dolnode.linkNusNode(nusnode);
						}
					}
				}
			}
			else{
				//If no dol nodes, there should be ONE nus node
				nl = xml.getElementsByTagName(NUSROM_KEY);
				if(nl != null && (nl.getLength() > 0)){
					Node node = nl.item(0);
					NusRomInfo nnode = new NusRomInfo();
					zri.head_node = nnode;
					if(node.getNodeType() == Node.ELEMENT_NODE){
						Element e = (Element)node;
						nnode.readIn(e);
					}
				}
			}

		}
		catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
		return zri;
	}
	
	public void printToStdErr(){
		if(head_node != null) head_node.printToStderr(0);
		System.err.println();
	}
	
}
