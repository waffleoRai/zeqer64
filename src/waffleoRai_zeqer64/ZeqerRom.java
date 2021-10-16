package waffleoRai_zeqer64;

import java.io.IOException;

import waffleoRai_Containers.nintendo.nus.N64ROMImage;
import waffleoRai_Containers.nintendo.nus.N64ZFileTable;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_zeqer64.filefmt.NusRomInfo;

public class ZeqerRom {

	/*----- Instance Variables -----*/
	
	private String rom_path;
	private N64ROMImage head;
	
	private NusRomInfo info;
	private N64ZFileTable dmadata;
	
	/*----- Init -----*/
	
	public ZeqerRom(String path, NusRomInfo info) throws IOException{
		if(info == null) throw new IllegalArgumentException("ROM Info argument cannot be null");
		
		rom_path = path;
		this.info = info;
		
		head = N64ROMImage.readROMHeader(path);
		FileBuffer romdat = N64ROMImage.loadROMasZ64(rom_path);
		dmadata = N64ZFileTable.readTable(romdat, this.info.getDMADataOffset());
	}
	
	/*----- Getters -----*/
	
	public NusRomInfo getRomInfo(){return info;}
	
	/*----- Setters -----*/
	
	/*----- File Load -----*/
	
	public FileBuffer loadFile(int dmadata_idx) throws IOException{
		FileNode node = dmadata.getFileAsNode(rom_path, head.getOrdering(), dmadata_idx);
		if(node == null) return null;
		return node.loadDecompressedData();
	}
	
	public FileBuffer loadCode() throws IOException{
		return loadFile(info.getDMADataIndex_code());
	}
	
	public FileBuffer loadAudiobank() throws IOException{
		return loadFile(info.getDMADataIndex_audiobank());
	}
	
	public FileBuffer loadAudioseq() throws IOException{
		return loadFile(info.getDMADataIndex_audioseq());
	}
	
	public FileBuffer loadAudiotable() throws IOException{
		return loadFile(info.getDMADataIndex_audiotable());
	}
	
}
