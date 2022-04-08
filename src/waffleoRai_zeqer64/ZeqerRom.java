package waffleoRai_zeqer64;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import waffleoRai_Containers.nintendo.nus.N64ROMImage;
import waffleoRai_Containers.nintendo.nus.N64ZFileTable;
import waffleoRai_Files.tree.FileNode;
import waffleoRai_Utils.BufferReference;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_zeqer64.SoundTables.BankInfoEntry;
import waffleoRai_zeqer64.SoundTables.BankMapEntry;
import waffleoRai_zeqer64.SoundTables.SeqInfoEntry;
import waffleoRai_zeqer64.SoundTables.WaveArcInfoEntry;
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
		
		//System.err.println("DMAData Offset: 0x" + Long.toHexString(this.info.getDMADataOffset()));
	}
	
	/*----- Getters -----*/
	
	public String getRomPath(){return rom_path;}
	public NusRomInfo getRomInfo(){return info;}
	
	public int getVirtualRomSize(){
		if(dmadata == null) return 0;
		return (int)dmadata.getVirtualRomEnd();
	}
	
	public int getFileVirtualOffset(int dmadata_idx){
		return (int)dmadata.getEntry(dmadata_idx).getVirtualStart();
	}
	
	public int[][] getVirtualAddressTable(){
		if(dmadata == null) return null;
		int fcount = dmadata.getEntryCount();
		int[][] table = new int[fcount][2];
		
		for(int i = 0; i < fcount; i++){
			table[i][0] = (int)dmadata.getEntry(i).getVirtualStart();
			table[i][1] = (int)dmadata.getEntry(i).getVirtualEnd();
		}
		
		return table;
	}
	
	public int[] getFileImageOrder(){
		if(dmadata == null) return null;
		int fcount = dmadata.getEntryCount();
		List<SortNode> nodes = new ArrayList<SortNode>(fcount);
		for(int i = 0; i < fcount; i++){
			nodes.add(new SortNode(dmadata.getEntry(i).getVirtualStart(), i));
		}
		Collections.sort(nodes);
		int[] out = new int[fcount];
		int i = 0;
		for(SortNode n : nodes){
			out[i++] = n.f_idx;
		}
		
		return out;
	}
	
	/*----- Setters -----*/
	
	/*----- Inner Classes -----*/
	
	private static class SortNode implements Comparable<SortNode>{
		public long vaddr;
		public int f_idx;
		
		public SortNode(long addr, int idx){
			vaddr = addr; f_idx = idx;
		}
		
		public int hashCode(){
			return (int)vaddr ^ f_idx;
		}

		public boolean equals(Object o){
			if(o == this) return true;
			if(o == null) return false;
			if(!(o instanceof SortNode)) return false;
			SortNode onode = (SortNode)o;
			if(onode.vaddr != this.vaddr) return false;
			if(onode.f_idx != this.f_idx) return false;
			return true;
		}
		
		public int compareTo(SortNode o) {
			if(o == null) return -1;
			if(this.vaddr > o.vaddr) return 1;
			else if(this.vaddr < o.vaddr) return -1;
			
			if(this.f_idx > o.f_idx) return 1;
			else if(this.f_idx < o.f_idx) return -1;
			
			return 0;
		}
		
	}
	
	/*----- File Load -----*/
	
	public BankInfoEntry[] loadBankEntries() throws IOException{
		FileBuffer f_code = loadCode();
		BufferReference ptr = f_code.getReferenceAt(info.getCodeOffset_bnktable());
		int count = Short.toUnsignedInt(ptr.nextShort());
		ptr.add(14);
		
		BankInfoEntry[] tbl = new BankInfoEntry[count];
		for(int b = 0; b < count; b++){
			tbl[b] = BankInfoEntry.readEntry(ptr);
		}
		
		return tbl;
	}
	
	public SeqInfoEntry[] loadSeqEntries() throws IOException{
		FileBuffer f_code = loadCode();
		BufferReference ptr = f_code.getReferenceAt(info.getCodeOffset_seqtable());
		int count = Short.toUnsignedInt(ptr.nextShort());
		ptr.add(14);
		
		SeqInfoEntry[] tbl = new SeqInfoEntry[count];
		for(int i = 0; i < count; i++){
			tbl[i] = SeqInfoEntry.readEntry(ptr);
		}
		
		return tbl;
	}
	
	public WaveArcInfoEntry[] loadWaveArcEntries() throws IOException{
		FileBuffer f_code = loadCode();
		BufferReference ptr = f_code.getReferenceAt(info.getCodeOffset_audtable());
		int count = Short.toUnsignedInt(ptr.nextShort());
		ptr.add(14);
		
		WaveArcInfoEntry[] tbl = new WaveArcInfoEntry[count];
		for(int i = 0; i < count; i++){
			tbl[i] = WaveArcInfoEntry.readEntry(ptr);
		}
		
		return tbl;
	}
	
	public BankMapEntry[] loadSeqBankMap() throws IOException{
		FileBuffer f_code = loadCode();
		BufferReference ptr = f_code.getReferenceAt(info.getCodeOffset_bnktable());
		int bcount = Short.toUnsignedInt(ptr.getShort());
		ptr.add((bcount+1) << 4);
		
		int roff = Short.toUnsignedInt(ptr.getShort());
		int scount = roff >>> 1;
		BankMapEntry[] tbl = new BankMapEntry[scount];
		ptr.add(roff);
		for(int i = 0; i < scount; i++){
			int mcount = Byte.toUnsignedInt(ptr.nextByte());
			tbl[i] = new BankMapEntry(mcount);
			for(int j = 0; j < mcount; j++){
				tbl[i].setBank(j, Byte.toUnsignedInt(ptr.nextByte()));
			}
		}
		return tbl;
	}
	
	public int getDmadataIndex(){
		long dmadata_offset = info.getDMADataOffset();
		int fcount = dmadata.getEntryCount();
		for(int i = 0; i < fcount; i++){
			if(dmadata.getEntry(i).getROMAddress() == dmadata_offset) return i;
		}
		return -1;
	}
	
	public FileBuffer loadFile(int dmadata_idx) throws IOException{
		FileNode node = dmadata.getFileAsNode(rom_path, head.getOrdering(), dmadata_idx);
		if(node == null) return null;
		return node.loadDecompressedData();
	}
	
	public FileBuffer loadDmadata() throws IOException{
		int idx = getDmadataIndex();
		if(idx < 0) return null;
		return loadFile(idx);
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
