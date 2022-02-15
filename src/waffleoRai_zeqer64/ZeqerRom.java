package waffleoRai_zeqer64;

import java.io.IOException;

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
	
	public NusRomInfo getRomInfo(){return info;}
	
	/*----- Setters -----*/
	
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
