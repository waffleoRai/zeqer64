package waffleoRai_zeqer64.extract;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.FileUtils;
import waffleoRai_zeqer64.ZeqerCore;
import waffleoRai_zeqer64.ZeqerRom;
import waffleoRai_zeqer64.filefmt.NusRomInfo;
import waffleoRai_zeqer64.filefmt.ZeqerSeqTable;
import waffleoRai_zeqer64.filefmt.ZeqerSeqTable.SeqTableEntry;

public class SeqExtractor {
	
	/*----- Constants -----*/
	
	public static final int MODE_USER = 0;
	public static final int MODE_SYS_Z5 = 1;
	public static final int MODE_SYS_Z6 = 2;
	
	private static final char SEP = File.separatorChar;
	
	/*----- Instance Variables -----*/
	
	private int mode = MODE_USER; //Determines if sys tables are updated (and which one)
	
	private String dir_base; //Expecting %PROGRAM_DIR%\seq\zseqs
	private ZeqerRom z_rom;
	
	private ZeqerSeqTable seq_tbl;
	
	/*----- Init -----*/
	
	public SeqExtractor(ZeqerRom rom) throws IOException{
		this(rom, ZeqerCore.getProgramDirectory() + SEP + ZeqerCore.DIRNAME_SEQ + SEP + ZeqerCore.DIRNAME_ZSEQ);
	}
	
	public SeqExtractor(ZeqerRom rom, String base_dir) throws IOException{
		this(rom, base_dir, null);
	}
	
	public SeqExtractor(ZeqerRom rom, String base_dir, ZeqerSeqTable table) throws IOException{
		if(rom == null) throw new IllegalArgumentException("ROM is required for extraction.");
		
		dir_base = base_dir;
		seq_tbl = table;
		z_rom = rom;
		
		if(seq_tbl == null){
			String path = getTablePath();
			if(FileBuffer.fileExists(path)){
				//Load
				try {
					seq_tbl = ZeqerSeqTable.readTable(FileBuffer.createBuffer(path, true));
				}
				catch (UnsupportedFileTypeException e) {
					e.printStackTrace();
					throw new IOException();
				}
			}
			else{
				//New
				seq_tbl = ZeqerSeqTable.createTable();
			}
		}
	}
	
	/*----- Paths -----*/
	
	public String getTablePath(){
		if(z_rom.getRomInfo().isZ5()) return dir_base + SEP + ZeqerCore.FN_SYSSEQ_OOT;
		return dir_base + SEP + ZeqerCore.FN_SYSSEQ_MM;
	}
	
	public String getIDTablePath(){
		return dir_base + SEP + "seq_" + z_rom.getRomInfo().getZeqerID() + ".bin";
	}
	
	/*----- Setters -----*/
	
	public void setUserMode(){mode = MODE_USER;}
	public void setSysZ5Mode(){mode = MODE_SYS_Z5;}
	public void setSysZ6Mode(){mode = MODE_SYS_Z6;}
	
	/*----- Extraction -----*/
	
	public boolean mapSeqBanks(int[] bank_uids) throws IOException{
		//Okay, so the seq/bank map table is weird.
		//Appears to be positioned after bank table, right before seq table.
		
		//Will need to load rom version uid table too, so know which uids are 
		//	connected to which seqs
		
		if(mode == MODE_USER) return false;
		if(z_rom == null) return false;
		if(bank_uids == null) return false;
		boolean good = true;
		
		String idtblpath = getIDTablePath();
		if(!FileBuffer.fileExists(idtblpath)) return false;
		FileBuffer inbuff = FileBuffer.createBuffer(idtblpath, true);
		int scount = (int)(inbuff.getFileSize() >>> 2);
		if(scount < 1) return false;
		int[] seqids = new int[scount];
		inbuff.setCurrentPosition(0L);
		for(int i = 0; i < scount; i++) seqids[i] = inbuff.nextInt();
		
		NusRomInfo rominfo = z_rom.getRomInfo();
		if(rominfo == null) return false;
		FileBuffer code = z_rom.loadCode(); code.setEndian(true);
		
		int bcount = bank_uids.length;
		long tst = rominfo.getCodeOffset_bnktable() + ((bcount+1) << 4);
		for(int i = 0; i < scount; i++){
			//Pull seq from table. If not found, warn and continue.
			SeqTableEntry entry = seq_tbl.getSequence(seqids[i]);
			if(entry == null){
				System.err.println("WARNING: Sequence at index " + i + " could not be found in zeqer table!");
				continue;
			}
			
			//Get bnk/seq table record location
			long roff = Short.toUnsignedLong(code.shortFromFile(tst + (i<<1)));
			roff += tst;
			
			//Read record/save to entry
			int sbcount = Byte.toUnsignedInt(code.getByte(roff));
			if(sbcount > 1){
				for(int j = 0; j < sbcount; j++){
					int bidx = Byte.toUnsignedInt(code.getByte(roff+1+j));
					if(bidx < 0 || bidx >= bank_uids.length){
						System.err.println("WARNING: Bank index " + bidx + " out of range. Skipping seq " + i + "...");
						continue;
					}
					entry.addBank(bank_uids[bidx]);
				}	
			}
			else{
				int bidx = Byte.toUnsignedInt(code.getByte(roff+1));
				if(bidx < 0 || bidx >= bank_uids.length){
					System.err.println("WARNING: Bank index " + bidx + " out of range. Skipping seq " + i + "...");
					continue;
				}
				entry.setSingleBank(bank_uids[bidx]);
			}
		}
		seq_tbl.writeTo(getTablePath());
		
		return good;
	}
	
	public boolean extractSeqs() throws IOException{
		if(z_rom == null) return false;
		boolean good = true;
		
		//Get needed files from ROM
		FileBuffer code = z_rom.loadCode(); code.setEndian(true);
		FileBuffer audioseq = z_rom.loadAudioseq(); audioseq.setEndian(true);
		NusRomInfo rominfo = z_rom.getRomInfo();
		if(rominfo == null) return false;
		
		//Iterate thru seq table
		code.setCurrentPosition(rominfo.getCodeOffset_seqtable());
		int scount = Short.toUnsignedInt(code.nextShort());
		int[] uid_tbl = new int[scount];
		code.skipBytes(14);
		for(int i = 0; i < scount; i++){
			long stoff = Integer.toUnsignedLong(code.nextInt());
			long size = Integer.toUnsignedLong(code.nextInt());
			byte unk = code.nextByte();
			byte atype = code.nextByte();
			code.skipBytes(6L);
			if(size <= 0L){
				System.err.println("SeqExtractor.extractSeqs || Seq " + i + " appears to be empty. Skipping...");
				continue;
			}
			
			//Nab seq file
			FileBuffer myseq = audioseq.createReadOnlyCopy(stoff, stoff+size);
			byte[] md5 = FileUtils.getMD5Sum(myseq.getBytes());
			String md5str = FileUtils.bytes2str(md5).toLowerCase();
			
			//See if this sequence is already registered.
			SeqTableEntry entry = seq_tbl.matchSequenceMD5(md5str);
			if(entry == null){
				//Create if write enabled.
				if(mode != MODE_USER){
					entry = seq_tbl.newEntry(md5);
					entry.setAllocType(atype);
					entry.setUnkByte(unk);
					
					//Copy data
					String datapath = dir_base + SEP + entry.getDataFileName();
					if(!FileBuffer.fileExists(datapath)){
						myseq.writeFile(datapath);
					}
					
					//Mark uid
					uid_tbl[i] = entry.getUID();
				}
			}
			else{
				String datapath = dir_base + SEP + entry.getDataFileName();
				if(!FileBuffer.fileExists(datapath)){
					myseq.writeFile(datapath);
				}
				uid_tbl[i] = entry.getUID();
			}
			
			myseq.dispose();
		}
		
		//Save tables
		String idtblpath = getIDTablePath();
		FileBuffer outbuff = new FileBuffer(scount << 2, true);
		for(int i = 0; i < scount; i++){
			outbuff.addToFile(uid_tbl[i]);
		}
		outbuff.writeFile(idtblpath);
		
		if(mode != MODE_USER){
			String tpath = getTablePath();
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tpath));
			seq_tbl.writeTo(tpath);
			bos.close();
		}
		
		return good;
	}

}
