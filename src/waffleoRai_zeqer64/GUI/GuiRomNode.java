package waffleoRai_zeqer64.GUI;

import waffleoRai_Containers.nintendo.nus.N64ROMImage;
import waffleoRai_zeqer64.ZeqerRom;
import waffleoRai_zeqer64.filefmt.NusRomInfo;

public class GuiRomNode implements Comparable<GuiRomNode>{
	
	private ZeqerRom rom;
	
	public GuiRomNode(ZeqerRom zrom){
		rom = zrom;
	}
	
	public ZeqerRom getRom(){
		return rom;
	}
	
	public boolean equals(Object o){
		if(o == null) return false;
		if(!(o instanceof GuiRomNode)) return false;
		GuiRomNode other = (GuiRomNode)o;
		return this.rom == other.rom;
	}
	
	public int hashCode(){
		return rom.hashCode();
	}
	
	public int compareTo(GuiRomNode o) {
		if(o == null || o.rom == null) return 1;
		if(this.rom == null){
			if(o.rom == null) return 0;
			return -1;
		}
		if(o.rom == null) return 1;
		NusRomInfo tinfo = this.rom.getRomInfo();
		NusRomInfo oinfo = o.rom.getRomInfo();
		if(tinfo == null){
			if(oinfo == null) return 0;
			return -1;
		}
		if(oinfo == null) return 1;
		
		//First sort by version enum.
		//Except version unknown should go at bottom
		int tver = tinfo.getGameVersionEnum();
		int over = oinfo.getGameVersionEnum();
		if(tver != over){
			if(tver == ZeqerRom.GAME_UNK) return 1;
			if(over == ZeqerRom.GAME_UNK) return -1;
			return tver - over;
		}
		
		//Sort by gamecode
		N64ROMImage thead = this.rom.getRomHead();
		N64ROMImage ohead = o.rom.getRomHead();
		if(thead == null){
			if(ohead == null) return 0;
			return -1;
		}
		if(ohead == null) return 1;
		int comp = 0;
		if(thead.getGamecode() != null) comp = thead.getGamecode().compareTo(ohead.getGamecode());
		else{
			if(ohead.getGamecode() != null) comp = -1;
		}
		if(comp != 0) return comp;
		
		//Then sort by name
		comp = 0;
		if(thead.getName() != null) comp = thead.getName().compareTo(ohead.getName());
		else{
			if(ohead.getName() != null) comp = -1;
		}

		return comp;
	}
	
	public String toString(){
		if(rom == null) return super.toString();
		String romname = rom.getRomInfo().getROMName();
		String romcode = rom.getRomHead().getGamecode();
		StringBuilder sb = new StringBuilder(romname.length() + romcode.length() + 8);
		sb.append('[');
		sb.append(romcode);
		sb.append("] ");
		sb.append(romname);
		return sb.toString();
	}

}
