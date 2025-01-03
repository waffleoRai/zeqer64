package waffleoRai_zeqer64.filefmt;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import waffleoRai_zeqer64.ZeqerRom;

public class NusRomInfo extends RomInfoNode{
	
	private String game_id;
	private int tv_type = ZeqerRom.TV_TYPE__NTSC;
	private int gamever_enum = ZeqerRom.GAME_UNK;
	
	private long crc_distro = 0L;
	private long crc_uncomp = 0L;
	
	private long offset_dmadata;
	private long codeoffset_bnktbl;
	private long codeoffset_seqtbl;
	private long codeoffset_audtbl;
	
	private int dmadata_idx_code;
	private int dmadata_idx_audiobank;
	private int dmadata_idx_audioseq;
	private int dmadata_idx_audiotable;
	
	private long ramaddr_code;
	
	protected NusRomInfo(){}
	
	public boolean isGamecube(){return false;}
	public int getTVType(){return tv_type;}
	
	public boolean isZ5(){return game_id.contains("oot");}
	public boolean isZ6(){return game_id.equals("mm");}
	public int getGameVersionEnum(){return gamever_enum;}
	
	public long getMakeromCRC(){return crc_distro;}
	public long getMakeromCRC_Uncompressed(){return crc_uncomp;}
	
	public long getCodeRAMAddress(){return ramaddr_code;}
	public long getDMADataOffset(){return offset_dmadata;}
	public long getCodeOffset_bnktable(){return codeoffset_bnktbl;}
	public long getCodeOffset_seqtable(){return codeoffset_seqtbl;}
	public long getCodeOffset_audtable(){return codeoffset_audtbl;}
	
	public int getDMADataIndex_code(){return dmadata_idx_code;}
	public int getDMADataIndex_audiobank(){return dmadata_idx_audiobank;}
	public int getDMADataIndex_audioseq(){return dmadata_idx_audioseq;}
	public int getDMADataIndex_audiotable(){return dmadata_idx_audiotable;}
	
	protected void readIn(Element xml_element){
		super.zeqer_id = xml_element.getAttribute("id");
		game_id = "unk";
		String gver_str = null;
		
		NodeList list = xml_element.getChildNodes();
		int ccount = list.getLength();
		for(int i = 0; i < ccount; i++){
			Node n = list.item(i);
			if(n.getNodeType() == Node.ELEMENT_NODE){
				Element child = (Element)n;
				String key = child.getNodeName();
				String txt = child.getTextContent();
				//System.err.println("DEBUG: txt = " + txt);
				txt = txt.replace("\"", "");
				if(txt.startsWith("0x")) txt = txt.substring(2);
				if(txt.isEmpty()) continue;
				
				try{
					if(key.startsWith("offset_")){
						//ROM relative offset
						if(key.endsWith("dmadata")){
							offset_dmadata = Long.parseUnsignedLong(txt, 16);
						}
					}
					else if(key.startsWith("codeaddr_")){
						//Load address of code
						if(key.equals("codeaddr_ram")){
							ramaddr_code = Long.parseUnsignedLong(txt, 16);
						}
					}
					else if(key.startsWith("codeoffset_")){
						//Offset to data inside code file
						if(key.endsWith("banktbl")){
							codeoffset_bnktbl = Long.parseUnsignedLong(txt, 16);
						}
						else if(key.endsWith("seqtbl")){
							codeoffset_seqtbl = Long.parseUnsignedLong(txt, 16);
						}
						else if(key.endsWith("wavtbl")){
							codeoffset_audtbl = Long.parseUnsignedLong(txt, 16);
						}
					}
					else if(key.startsWith("dmaidx_")){
						//Index of file in dmadata table
						if(key.endsWith("_code")){
							dmadata_idx_code = Integer.parseInt(txt);
						}
						else if(key.endsWith("bnkarc")){
							dmadata_idx_audiobank = Integer.parseInt(txt);
						}
						else if(key.endsWith("seqarc")){
							dmadata_idx_audioseq = Integer.parseInt(txt);
						}
						else if(key.endsWith("wavarc")){
							dmadata_idx_audiotable = Integer.parseInt(txt);
						}
					}
					else if(key.startsWith("nusrom")){
						if(key.endsWith("md5")){
							super.md5_str = txt;
						}
						else if(key.endsWith("name")){
							super.rom_name = txt;
						}
						else if(key.endsWith("game")){
							game_id = txt;
						}
						else if(key.endsWith("gamever")){
							gver_str = txt;
						}
					}
					else if(key.startsWith("region_tv")){
						if(txt.equalsIgnoreCase("NTSC")) tv_type = ZeqerRom.TV_TYPE__NTSC;
						else if(txt.equalsIgnoreCase("MPAL")) tv_type = ZeqerRom.TV_TYPE__MPAL;
						else if(txt.equalsIgnoreCase("PAL")) tv_type = ZeqerRom.TV_TYPE__PAL;
					}
					else if(key.endsWith("headercrc")){
						if(key.equals("ucheadercrc")){
							try{crc_uncomp = Long.parseUnsignedLong(txt, 16);}
							catch(NumberFormatException ex){ex.printStackTrace();}
						}
						else if(key.equals("headercrc")){
							try{crc_distro = Long.parseUnsignedLong(txt, 16);}
							catch(NumberFormatException ex){ex.printStackTrace();}
						}
					}
				}
				catch(NumberFormatException ex){
					ex.printStackTrace();
				}
				
			}
		}
		
		//Try to resolve game version string
		if(gver_str != null){
			if(game_id.equals("oot")){
				if(gver_str.equals("1.0")) this.gamever_enum = ZeqerRom.GAME_OCARINA_V1_0;
				else if(gver_str.equals("1.1")) this.gamever_enum = ZeqerRom.GAME_OCARINA_V1_1;
				else if(gver_str.equals("1.2")) this.gamever_enum = ZeqerRom.GAME_OCARINA_V1_2;
				else if(gver_str.equals("0.9")) this.gamever_enum = ZeqerRom.GAME_OCARINA_V0_9;
				else if(gver_str.equals("1.1GC")) this.gamever_enum = ZeqerRom.GAME_OCARINA_GC_V1_1;
				else if(gver_str.equals("1.2GC")) this.gamever_enum = ZeqerRom.GAME_OCARINA_GC_V1_2;
				else if(gver_str.equals("GZ")) this.gamever_enum = ZeqerRom.GAME_OCARINA_GZ;
				else if(gver_str.equals("MOD")) this.gamever_enum = ZeqerRom.GAME_OCARINA_MOD;
			}
			else if(game_id.equals("ootmq")){
				if(gver_str.equals("1.1")) this.gamever_enum = ZeqerRom.GAME_OCARINA_MQ_V1_1;
				else if(gver_str.equals("1.2")) this.gamever_enum = ZeqerRom.GAME_OCARINA_MQ_V1_2;
				else if(gver_str.equals("1.1D")) this.gamever_enum = ZeqerRom.GAME_OCARINA_MQDBG_V1_1;
				else if(gver_str.equals("GZ")) this.gamever_enum = ZeqerRom.GAME_OCARINA_MQ_GZ;
				else if(gver_str.equals("MOD")) this.gamever_enum = ZeqerRom.GAME_OCARINA_MQ_MOD;
			}
			else if(game_id.equals("mm")){
				if(gver_str.equals("1.0J")) this.gamever_enum = ZeqerRom.GAME_MAJORA_V1_0_J;
				else if(gver_str.equals("1.0I")) this.gamever_enum = ZeqerRom.GAME_MAJORA_V1_0_I;
				else if(gver_str.equals("GC")) this.gamever_enum = ZeqerRom.GAME_MAJORA_GC;
				else if(gver_str.equals("KZ")) this.gamever_enum = ZeqerRom.GAME_MAJORA_KZ;
				else if(gver_str.equals("MOD")) this.gamever_enum = ZeqerRom.GAME_MAJORA_MOD;
			}
		}
		
	}

	public void printToStderr(int indent){
		super.printToStderr(indent);
		StringBuilder sb = new StringBuilder(indent+1);
		for(int i = 0; i < indent; i++) sb.append("\t");
		String tabs = sb.toString();
		
		System.err.println(tabs + "-> NUS ROM Info");
		System.err.println(tabs + "gameid: " + game_id);
		System.err.println(tabs + "dmadata Offset: 0x" + Long.toHexString(offset_dmadata));
		System.err.println(tabs + "dmadata Index (audiobank): " + dmadata_idx_audiobank);
		System.err.println(tabs + "dmadata Index (audioseq): " + dmadata_idx_audioseq);
		System.err.println(tabs + "dmadata Index (audiotable): " + dmadata_idx_audiotable);
		System.err.println(tabs + "dmadata Index (code): " + dmadata_idx_code);
		System.err.println(tabs + "Offset in code (bank table): 0x" + Long.toHexString(codeoffset_bnktbl));
		System.err.println(tabs + "Offset in code (seq table): 0x" + Long.toHexString(codeoffset_seqtbl));
		System.err.println(tabs + "Offset in code (aud table): 0x" + Long.toHexString(codeoffset_audtbl));
		System.err.println(tabs + "code RAM Address: 0x" + Long.toHexString(ramaddr_code));
	}
	
}
