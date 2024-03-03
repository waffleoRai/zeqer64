package waffleoRai_zeqer64.extract;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import waffleoRai_SeqSound.SeqVoiceCounter;
import waffleoRai_SeqSound.n64al.NUSALSeq;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.FileUtils;
import waffleoRai_zeqer64.SoundTables.SeqInfoEntry;
import waffleoRai_zeqer64.ZeqerCore;
import waffleoRai_zeqer64.ZeqerErrorCode;
import waffleoRai_zeqer64.ZeqerRom;
import waffleoRai_zeqer64.ZeqerSeq;
import waffleoRai_zeqer64.filefmt.NusRomInfo;
import waffleoRai_zeqer64.filefmt.seq.UltraSeqFile;
import waffleoRai_zeqer64.filefmt.seq.ZeqerSeqTable;
import waffleoRai_zeqer64.filefmt.seq.SeqTableEntry;

public class SeqExtractor {
	
	/*----- Constants -----*/
	
	public static final int MODE_USER = 0;
	public static final int MODE_SYS_Z5 = 1;
	public static final int MODE_SYS_Z6 = 2;
	public static final int MODE_SYS = 3;
	
	private static final char SEP = File.separatorChar;
	
	public static final int PARSE_TIMEOUT = 10000; //10s
	
	/*----- Instance Variables -----*/
	
	private int mode = MODE_USER; //Determines if sys tables are updated (and which one)
	
	private String dir_base; //Expecting %PROGRAM_DIR%\seq\zseqs
	private ZeqerRom z_rom;
	
	private ZeqerSeqTable seq_tbl;
	
	/*----- Init -----*/
	
	public SeqExtractor(ZeqerRom rom) throws IOException{
		this(rom, ZeqerCore.getActiveCore().getProgramDirectory() + SEP + ZeqerCore.DIRNAME_SEQ + SEP + ZeqerCore.DIRNAME_ZSEQ);
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
		//if(z_rom.getRomInfo().isZ5()) return dir_base + SEP + ZeqerCore.FN_SYSSEQ_OOT;
		//return dir_base + SEP + ZeqerCore.FN_SYSSEQ_MM;
		return dir_base + SEP + ZeqerCore.FN_SYSSEQ;
	}
	
	public String getIDTablePath(){
		return dir_base + SEP + "seq_" + z_rom.getRomInfo().getZeqerID() + ".bin";
	}
	
	/*----- Setters -----*/
	
	public void setUserMode(){mode = MODE_USER;}
	public void setSysMode(){mode = MODE_SYS;}
	//public void setSysZ5Mode(){mode = MODE_SYS_Z5;}
	//public void setSysZ6Mode(){mode = MODE_SYS_Z6;}
	
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
		//FileBuffer code = z_rom.loadCode(); code.setEndian(true);
		NusRomInfo rominfo = z_rom.getRomInfo();
		if(rominfo == null) return false;
		String zid = rominfo.getZeqerID();
		FileBuffer audioseq = z_rom.loadAudioseq(); audioseq.setEndian(true);
		SeqInfoEntry[] cseq_tbl = z_rom.loadSeqEntries();
		Set<Integer> ref_idxs = new TreeSet<Integer>();
		
		//Iterate thru seq table
		int scount = cseq_tbl.length;
		int[] uid_tbl = new int[scount];
		for(int i = 0; i < scount; i++){
			long stoff = cseq_tbl[i].getOffset();
			long size = cseq_tbl[i].getSize();
			if(size <= 0L){
				//System.err.println("SeqExtractor.extractSeqs || Seq " + i + " appears to be empty. Skipping...");
				ref_idxs.add(i);
				continue;
			}
			
			//Nab seq file
			FileBuffer myseq = audioseq.createReadOnlyCopy(stoff, stoff+size);
			byte[] md5 = FileUtils.getMD5Sum(myseq.getBytes());
			String md5str = FileUtils.bytes2str(md5).toLowerCase();
			
			System.err.println(String.format("Processing seq %d of %d: Size 0x%08x", i, scount, myseq.getFileSize()));
			
			//See if this sequence is already registered.
			SeqTableEntry entry = seq_tbl.matchSequenceMD5(md5str);
			if(entry == null){
				//Create if write enabled.
				if(mode != MODE_USER){
					entry = seq_tbl.newEntry(md5);
					entry.setEnumString("AUDIOSEQ_" + zid.toUpperCase() + String.format("_%03d", i));
					entry.setMedium((byte)cseq_tbl[i].getMedium());
					entry.setCache((byte)cseq_tbl[i].getCachePolicy());
					
					//Copy data
					String datapath = dir_base + SEP + entry.getDataFileName();
					if(!FileBuffer.fileExists(datapath)){
						ZeqerSeq zseq = new ZeqerSeq(entry);
						if(z_rom.getRomInfo().isZ5()) zseq.setOoTCompatible(true);
						
						//Add data.
						ZeqerErrorCode err = new ZeqerErrorCode();
						NUSALSeq nseq = ZeqerSeq.parseSeqWithTimeout(myseq, PARSE_TIMEOUT, err);
						if(nseq != null){
							zseq.setSequence(nseq);
							SeqVoiceCounter vctr = new SeqVoiceCounter();
							nseq.playTo(vctr, false);
							zseq.setMaxVoiceLoad(vctr.getMaxTotalVoiceCount());
							int lyrch = nseq.getMaxLayersPerChannel();
							zseq.setMoreThanFourLayers(lyrch > 4);
						}
						else{
							zseq.setRawData(myseq);
							System.err.print("SeqExtractor.extractSeqs | Seq " + String.format("%08x", entry.getUID()) + " | Seq parse failed. Likely reason: ");
							switch(err.getValue()){
							case ZeqerSeq.PARSE_ERR_CRASH:
								System.err.print("Parser could not read data");
								break;
							case ZeqerSeq.PARSE_ERR_TIMEOUT:
								System.err.print("Time out (parser probably hung on infinite while)");
								break;
							case ZeqerSeq.PARSE_ERR_OTHER_IRQ:
								System.err.print("Misc. interrupt request");
								break;
							case ZeqerSeq.PARSE_ERR_NONE:
							default:
								System.err.print("Unknown");
								break;
							}
							System.err.println();
						}
						UltraSeqFile.writeUSEQ(zseq, datapath, myseq);
						//myseq.writeFile(datapath);
					}
					
					//Mark uid
					uid_tbl[i] = entry.getUID();
				}
			}
			else{
				String datapath = dir_base + SEP + entry.getDataFileName();
				if(!FileBuffer.fileExists(datapath)){
					ZeqerSeq zseq = new ZeqerSeq(entry);
					if(z_rom.getRomInfo().isZ5()) zseq.setOoTCompatible(true);
					
					//Add data.
					ZeqerErrorCode err = new ZeqerErrorCode();
					NUSALSeq nseq = ZeqerSeq.parseSeqWithTimeout(myseq, PARSE_TIMEOUT, err);
					if(nseq != null){
						zseq.setSequence(nseq);
						SeqVoiceCounter vctr = new SeqVoiceCounter();
						nseq.playTo(vctr, false);
						zseq.setMaxVoiceLoad(vctr.getMaxTotalVoiceCount());
						int lyrch = nseq.getMaxLayersPerChannel();
						zseq.setMoreThanFourLayers(lyrch > 4);
					}
					else{
						zseq.setRawData(myseq);
						System.err.print("SeqExtractor.extractSeqs | Seq " + String.format("%08x", entry.getUID()) + " | Seq parse failed. Likely reason: ");
						switch(err.getValue()){
						case ZeqerSeq.PARSE_ERR_CRASH:
							System.err.print("Parser could not read data");
							break;
						case ZeqerSeq.PARSE_ERR_TIMEOUT:
							System.err.print("Time out (parser probably hung on infinite while)");
							break;
						case ZeqerSeq.PARSE_ERR_OTHER_IRQ:
							System.err.print("Misc. interrupt request");
							break;
						case ZeqerSeq.PARSE_ERR_NONE:
						default:
							System.err.print("Unknown");
							break;
						}
						System.err.println();
					}
					UltraSeqFile.writeUSEQ(zseq, datapath, myseq);
				}
				uid_tbl[i] = entry.getUID();
			}
			
			myseq.dispose();
		}
		
		//Resolve references
		for(Integer ridx : ref_idxs){
			int tidx = cseq_tbl[ridx].getOffset();
			uid_tbl[ridx] = uid_tbl[tidx];
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
