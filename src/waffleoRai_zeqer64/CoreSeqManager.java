package waffleoRai_zeqer64;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import waffleoRai_SeqSound.SeqVoiceCounter;
import waffleoRai_SeqSound.n64al.NUSALSeq;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileUtils;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_zeqer64.SoundTables.SeqInfoEntry;
import waffleoRai_zeqer64.extract.RomExtractionSummary;
import waffleoRai_zeqer64.extract.RomExtractionSummary.ExtractionError;
import waffleoRai_zeqer64.filefmt.NusRomInfo;
import waffleoRai_zeqer64.filefmt.TSVTables;
import waffleoRai_zeqer64.filefmt.UltraFile;
import waffleoRai_zeqer64.filefmt.UltraSeqFile;
import waffleoRai_zeqer64.filefmt.ZeqerSeqTable;
import waffleoRai_zeqer64.filefmt.ZeqerSeqTable.SeqTableEntry;

class CoreSeqManager {
	
	private static final char SEP = File.separatorChar;
	
	public static final int PARSE_TIMEOUT = 10000; //10s
	
	/*----- Instance Variables -----*/
	
	private String root_dir; //Base seq dir (ie. zeqerbase/seq)
	private boolean sys_write_enabled = false;
	
	private boolean parse_seq_on_load = true;
	
	private ZeqerSeqTable seq_table_sys;
	private ZeqerSeqTable seq_table_user;
	
	/*----- Init -----*/
	
	public CoreSeqManager(String base_seq_dir, boolean sysMode) throws UnsupportedFileTypeException, IOException{
		//Loads tables too, or creates if not there.
		root_dir = base_seq_dir;
		sys_write_enabled = sysMode;
		parse_seq_on_load = !sysMode;
		
		loadTables();
	}
	
	private void loadTables() throws IOException, UnsupportedFileTypeException{
		String zdir = root_dir + SEP + ZeqerCore.DIRNAME_ZSEQ;
		if(!FileBuffer.directoryExists(zdir)){
			Files.createDirectories(Paths.get(zdir));
		}
		
		String tblpath = getSysTablePath();
		if(FileBuffer.fileExists(tblpath)){
			seq_table_sys = ZeqerSeqTable.readTable(FileBuffer.createBuffer(tblpath, true));
		}
		else{
			seq_table_sys = ZeqerSeqTable.createTable();
		}
	
		tblpath = getUserTablePath();
		if(FileBuffer.fileExists(tblpath)){
			seq_table_user = ZeqerSeqTable.readTable(FileBuffer.createBuffer(tblpath, true));
		}
		else{
			if(!sys_write_enabled){
				seq_table_user = ZeqerSeqTable.createTable();	
			}
		}
	}
	
	public int genNewRandomGUID(){
		//TODO Also add this as a safety for other managers
		Random r = new Random();
		int uid = r.nextInt();
		while((uid == 0) || (uid == -1) || hasSeqWithUID(uid)){
			uid = r.nextInt();
		}
		return uid;
	}
	
	protected boolean hardReset(){
		seq_table_sys = null;
		seq_table_user = null;
		
		try{loadTables();}
		catch(Exception ex){
			ex.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/*----- Install -----*/
	
	protected boolean updateVersion(String temp_update_dir) throws IOException{
		//For waves we pretty much just copy everything back except zwavs.bin
		
		Path src_dir_path = Paths.get(temp_update_dir);
		if(!Files.isDirectory(src_dir_path)) return false;
		
		DirectoryStream<Path> dirstr = Files.newDirectoryStream(src_dir_path);
		for(Path child : dirstr){
			//User overwrites all of these.
			if(Files.isDirectory(child)){
				String fn = child.getFileName().toString();
				if(!fn.equals(ZeqerCore.DIRNAME_ZSEQ)){
					String target = root_dir + SEP + fn;
					FileUtils.moveDirectory(child.toAbsolutePath().toString(), target, true);
				}
			}
			else {
				String target = root_dir + SEP + child.getFileName().toString();
				Files.move(child, Paths.get(target), StandardCopyOption.REPLACE_EXISTING);
			}
		}
		dirstr.close();
		
		src_dir_path = Paths.get(temp_update_dir + SEP + ZeqerCore.DIRNAME_ZSEQ);
		if(Files.isDirectory(src_dir_path)){
			dirstr = Files.newDirectoryStream(src_dir_path);
			for(Path child : dirstr){
				//User overwrites all of these.
				if(Files.isDirectory(child)){
					String target = root_dir + SEP + ZeqerCore.DIRNAME_ZSEQ + SEP + child.getFileName().toString();
					FileUtils.moveDirectory(child.toAbsolutePath().toString(), target, true);
				}
				else {
					String fn = child.getFileName().toString();
					String target = root_dir + SEP + ZeqerCore.DIRNAME_ZSEQ + SEP + fn;
					if(fn.endsWith(".buseq")){
						if(FileBuffer.fileExists(target)){
							//Load seq data from old and splice into new.
							UltraFile oldseq = UltraFile.openUltraFile(child.toAbsolutePath().toString());
							if(oldseq.hasChunk("DATA")){
								UltraFile newseq = UltraFile.openUltraFile(target);
								newseq.addOrReplaceChunk("DATA", oldseq.getChunkData("DATA"), 1);
								
								//Have to unset a flag in the META chunk too...
								FileBuffer meta = newseq.getChunkData("META");
								if(meta != null){
									int b = Byte.toUnsignedInt(meta.getByte(4L));
									b &= 0x7F;
									meta.replaceByte((byte)b, 4L);
								}
								
								newseq.writeTo(target);
								newseq.close();
							}
							oldseq.close();
						}
						else{
							//Just copy
							Files.move(child, Paths.get(target), StandardCopyOption.REPLACE_EXISTING);
						}
					}
					else{
						if(!fn.equals(ZeqerCore.FN_SYSSEQ)){
							Files.move(child, Paths.get(target), StandardCopyOption.REPLACE_EXISTING);
						}
					}
				}
			}
			dirstr.close();
		}
		
		return true;
	}
	
	/*----- Getters -----*/
	
	public String getSysTablePath(){
		return root_dir + SEP + ZeqerCore.DIRNAME_ZSEQ + SEP + ZeqerCore.FN_SYSSEQ;
	}
	
	public String getUserTablePath(){
		return root_dir + SEP + ZeqerCore.FN_USRSEQ;
	}
	
	public String getSeqDataFilePath(int uid){
		if(isSysSeq(uid)){
			return root_dir + SEP + ZeqerCore.DIRNAME_ZSEQ + SEP + String.format("%08x.buseq", uid);
		}
		else{
			return root_dir + SEP + String.format("%08x.buseq", uid);	
		}
	}
	
	public boolean isSysSeq(int uid){
		if(seq_table_sys != null){
			return seq_table_sys.getSequence(uid) != null;
		}
		return false;
	}
	
	public boolean hasSeqWithUID(int uid){
		if(seq_table_sys != null){
			if(seq_table_sys.getSequence(uid) != null) return true;
		}
		if(seq_table_user != null){
			if(seq_table_user.getSequence(uid) != null) return true;
		}
		return false;
	}
	
	public ZeqerSeqTable getZeqerSeqTable(){return seq_table_sys;}
	
	public ZeqerSeqTable getUserSeqTable(){
		if(seq_table_user == null) seq_table_user = ZeqerSeqTable.createTable();
		return seq_table_user;
	}
	
	public SeqTableEntry getSeqInfo(int seq_uid){
		SeqTableEntry entry = null;
		if(seq_table_sys != null) entry = seq_table_sys.getSequence(seq_uid);
		if(entry == null && seq_table_user != null) {
			entry = seq_table_user.getSequence(seq_uid);
		}
		return entry;
	}
	
	public ZeqerSeq loadSeq(int seq_uid){
		SeqTableEntry entry = null;
		String basedir = null;
		if(seq_table_sys != null) entry = seq_table_sys.getSequence(seq_uid);
		if(entry != null){
			basedir = root_dir + SEP + ZeqerCore.DIRNAME_ZSEQ;
		}
		
		if(entry == null && seq_table_sys != null) {
			entry = seq_table_user.getSequence(seq_uid);
			basedir = root_dir;
		}
		if(entry == null) return null;
		
		String spath = basedir + SEP + entry.getDataFileName();
		if(!FileBuffer.fileExists(spath)) return null;
		try{
			UltraSeqFile useq =  UltraSeqFile.openUSEQ(spath);
			ZeqerSeq zseq = UltraSeqFile.readUSEQ(useq, entry, parse_seq_on_load);
			useq.dispose();
			return zseq;
		}
		catch(Exception ex){
			System.err.println("ZeqerCore.loadSeqData || Failed to load seq " + String.format("%08x", seq_uid));
			ex.printStackTrace();
			return null;
		}
	}
	
	public FileBuffer loadSeqData(int seq_uid){
		SeqTableEntry entry = null;
		String basedir = null;
		if(seq_table_sys != null) entry = seq_table_sys.getSequence(seq_uid);
		if(entry != null){
			basedir = root_dir + SEP + ZeqerCore.DIRNAME_ZSEQ;
		}
		
		if(entry == null && seq_table_sys != null) {
			entry = seq_table_user.getSequence(seq_uid);
			basedir = root_dir;
		}
		if(entry == null) return null;
		
		String spath = basedir + SEP + entry.getDataFileName();
		if(!FileBuffer.fileExists(spath)) return null;
		try{
			UltraSeqFile useq = UltraSeqFile.openUSEQ(spath);
			return useq.loadChunk("DATA");
		}
		catch(Exception ex){
			System.err.println("ZeqerCore.loadSeqData || Failed to load seq " + String.format("%08x", seq_uid));
			ex.printStackTrace();
			return null;
		}
	}
	
	public List<ZeqerSeq> getAllValidSeqs() throws IOException, UnsupportedFileTypeException{
		//To be valid, file must be present AND have a DATA block!
		List<ZeqerSeq> list = new LinkedList<ZeqerSeq>();
		if(seq_table_sys != null){
			String zdir = root_dir + SEP + ZeqerCore.DIRNAME_ZSEQ;
			List<SeqTableEntry> entries = seq_table_sys.getAllEntries();
			for(SeqTableEntry entry : entries){
				String useqPath = zdir + SEP + entry.getDataFileName();
				if(!FileBuffer.fileExists(useqPath)) continue;
				
				//Check for DATA chunk...
				UltraSeqFile file = UltraSeqFile.openUSEQ(useqPath);
				if(file.hasDATAChunk()){
					ZeqerSeq zseq = new ZeqerSeq(entry);
					zseq.setSourcePath(useqPath);
					list.add(zseq);
				}
				file.clearStoredFileData();
			}
		}
		
		if(seq_table_user != null){
			List<SeqTableEntry> entries = seq_table_user.getAllEntries();
			for(SeqTableEntry entry : entries){
				String useqPath = root_dir + SEP + entry.getDataFileName();
				if(!FileBuffer.fileExists(useqPath)) continue;
				
				//Check for DATA chunk...
				UltraSeqFile file = UltraSeqFile.openUSEQ(useqPath);
				if(file.hasDATAChunk()){
					ZeqerSeq zseq = new ZeqerSeq(entry);
					zseq.setSourcePath(useqPath);
					list.add(zseq);
				}
				file.clearStoredFileData();
			}
		}
		return list;
	}
	
	public int[] loadVersionTable(String zeqer_id) throws IOException{
		String idtblpath = root_dir + SEP + ZeqerCore.DIRNAME_ZSEQ + SEP + "seq_" + zeqer_id + ".bin";
		if(!FileBuffer.fileExists(idtblpath)) return null;
		FileBuffer buff = FileBuffer.createBuffer(idtblpath, true);
		buff.setCurrentPosition(0L);
		int scount = (int)(buff.getFileSize() >>> 2);
		int[] uids = new int[scount];
		for(int i = 0; i < scount; i++){
			uids[i] = buff.nextInt();
		}
		return uids;
	}
	
	/*----- Setters -----*/
	
	public void setParseSeqOnLoad(boolean b){
		this.parse_seq_on_load = b;
	}
	
	public SeqTableEntry newUserSeq(NUSALSeq data) throws IOException{
		int id = genNewRandomGUID();
		SeqTableEntry e = seq_table_user.newEntry(id);
		if(e == null) return null;
		
		ZeqerSeq zseq = new ZeqerSeq(e);
		zseq.setSequence(data);
		zseq.updateTableEntry();
		String writepath = root_dir + SEP + String.format("%08x.buseq", id);
		UltraSeqFile.writeUSEQ(zseq, writepath);
		saveUserTable();
		return e;
	}
	
	public boolean deleteSeq(int uid){
		if(seq_table_user != null){
			if(seq_table_user.deleteEntry(uid) != null) return true;
		}
		if(!sys_write_enabled) return false;
		return seq_table_sys.deleteEntry(uid) != null;
	}
	
	/*----- ROM Extract -----*/
	
	private static void addZeqerSeqData(FileBuffer myseq, ZeqerSeq zseq){
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
			System.err.print("CoreSeqManager.addZeqerSeqData | Seq " + String.format("%08x", zseq.getTableEntry().getUID()) + " | Seq parse failed. Likely reason: ");
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
	}
	
	public boolean importROMSeqs(ZeqerRom z_rom, RomExtractionSummary errorInfo, boolean verbose) throws IOException{
		if(z_rom == null) return false;
		NusRomInfo rominfo = z_rom.getRomInfo();
		if(rominfo == null) return false;
		
		if(errorInfo != null){
			errorInfo.seqsAddedAsCustom = 0;
			errorInfo.seqsFound = 0;
			errorInfo.seqsNew = 0;
			errorInfo.seqsOkay = 0;
		}
		
		final String ZSEQ_DIR = root_dir + SEP + ZeqerCore.DIRNAME_ZSEQ;
		boolean good = true;
		String zid = rominfo.getZeqerID();
		String version_tbl_path = ZSEQ_DIR + SEP + "seq_" + zid + ".bin";
		FileBuffer audioseq = z_rom.loadAudioseq(); audioseq.setEndian(true);
		SeqInfoEntry[] cseq_tbl = z_rom.loadSeqEntries();
		Set<Integer> ref_idxs = new TreeSet<Integer>();
		
		int scount = cseq_tbl.length;
		int[] uid_tbl = new int[scount];
		for(int i = 0; i < scount; i++){
			long stoff = cseq_tbl[i].getOffset();
			long size = cseq_tbl[i].getSize();
			if(size <= 0L){
				ref_idxs.add(i);
				continue;
			}
			if(errorInfo != null) errorInfo.seqsFound++; //Don't want to count refs
			
			//Nab seq file
			FileBuffer myseq = audioseq.createReadOnlyCopy(stoff, stoff+size);
			byte[] md5 = FileUtils.getMD5Sum(myseq.getBytes());
			String md5str = FileUtils.bytes2str(md5).toLowerCase();
			
			SeqTableEntry entry = seq_table_sys.matchSequenceMD5(md5str);
			if(entry == null){
				//Create if write enabled.
				if(sys_write_enabled){
					entry = seq_table_sys.newEntry(md5);
					entry.setEnumString("AUDIOSEQ_" + zid.toUpperCase() + String.format("_%03d", i));
					entry.setMedium((byte)cseq_tbl[i].getMedium());
					entry.setCache((byte)cseq_tbl[i].getCachePolicy());
					
					//Copy data
					String datapath = ZSEQ_DIR + SEP + entry.getDataFileName();
					if(!FileBuffer.fileExists(datapath)){
						ZeqerSeq zseq = new ZeqerSeq(entry);
						if(z_rom.getRomInfo().isZ5()) zseq.setOoTCompatible(true);
						
						//Add data.
						addZeqerSeqData(myseq, zseq);
						UltraSeqFile.writeUSEQ(zseq, datapath, myseq);
					}
					
					//Mark uid
					uid_tbl[i] = entry.getUID();
					if(errorInfo != null){
						errorInfo.seqsNew++;
						errorInfo.seqsOkay++;
					}
				}
				else{
					//Try to add as user.
					if(seq_table_user != null){
						entry = seq_table_user.newEntry(md5);
						entry.setEnumString("AUDIOSEQ_" + zid.toUpperCase() + String.format("_%03d", i));
						entry.setMedium((byte)cseq_tbl[i].getMedium());
						entry.setCache((byte)cseq_tbl[i].getCachePolicy());
						
						//Copy data
						String datapath = root_dir + SEP + entry.getDataFileName();
						if(!FileBuffer.fileExists(datapath)){
							ZeqerSeq zseq = new ZeqerSeq(entry);
							if(z_rom.getRomInfo().isZ5()) zseq.setOoTCompatible(true);
							
							//Add data.
							addZeqerSeqData(myseq, zseq);
							UltraSeqFile.writeUSEQ(zseq, datapath, myseq);
						}
						
						//Mark uid
						uid_tbl[i] = entry.getUID();
						if(errorInfo != null) errorInfo.seqsAddedAsCustom++;
					}
					else{
						//User mode disabled. Note error.
						uid_tbl[i] = 0;
						if(errorInfo != null){
							if(errorInfo.seqErrors == null){
								errorInfo.seqErrors = new LinkedList<ExtractionError>();
							}
							ExtractionError err = new ExtractionError();
							err.index0 = i;
							err.itemType = RomExtractionSummary.ITEM_TYPE_SEQ;
							err.reason = RomExtractionSummary.ERROR_NOT_IN_TABLE;
							err.itemUID = ZeqerUtils.md52UID(md5);
							errorInfo.seqErrors.add(err);
						}
						good = false;
					}
				}
			}
			else{
				//Existing match. Check if there's a data file.
				String datapath = ZSEQ_DIR + SEP + entry.getDataFileName();
				if(!FileBuffer.fileExists(datapath)){
					ZeqerSeq zseq = new ZeqerSeq(entry);
					if(z_rom.getRomInfo().isZ5()) zseq.setOoTCompatible(true);
					
					//Add data.
					addZeqerSeqData(myseq, zseq);
					UltraSeqFile.writeUSEQ(zseq, datapath, myseq);
					if(errorInfo != null) errorInfo.seqsOkay++;
				}
				else{
					//It may be a husk. Inject data.
					try {
						UltraSeqFile useq = UltraSeqFile.openUSEQ(datapath);
						ZeqerSeq zseq = UltraSeqFile.readUSEQ(useq, entry, false);
						useq.dispose();
						
						zseq.setRawData(myseq);
						UltraSeqFile.writeUSEQ(zseq, datapath, myseq);
						if(errorInfo != null) errorInfo.seqsOkay++;
					}
					catch (UnsupportedFileTypeException e) {
						e.printStackTrace();
						if(errorInfo != null){
							if(errorInfo.seqErrors == null){
								errorInfo.seqErrors = new LinkedList<ExtractionError>();
							}
							ExtractionError err = new ExtractionError();
							err.index0 = i;
							err.itemType = RomExtractionSummary.ITEM_TYPE_SEQ;
							err.reason = RomExtractionSummary.ERROR_WRITE_FAILED;
							err.itemUID = ZeqerUtils.md52UID(md5);
							errorInfo.seqErrors.add(err);
						}
						good = false;
					}
				}
				
				uid_tbl[i] = entry.getUID();
			}
			myseq.dispose();
			
			if(entry != null){
				//Version flags
				if(rominfo.isZ5()) entry.setFlags(ZeqerSeqTable.FLAG_Z5);
				if(rominfo.isZ6()) entry.setFlags(ZeqerSeqTable.FLAG_Z6);
			}
		}
		
		//Resolve references
		for(Integer ridx : ref_idxs){
			int tidx = cseq_tbl[ridx].getOffset();
			uid_tbl[ridx] = uid_tbl[tidx];
		}
		
		//Save tables
		FileBuffer outbuff = new FileBuffer(scount << 2, true);
		for(int i = 0; i < scount; i++){
			outbuff.addToFile(uid_tbl[i]);
		}
		outbuff.writeFile(version_tbl_path);
		
		saveTables();
		return good;
	}

	public boolean mapSeqBanks(ZeqerRom z_rom, int[] bank_uids, boolean verbose) throws IOException{
		//Okay, so the seq/bank map table is weird.
		//Appears to be positioned after bank table, right before seq table.
		
		//Will need to load rom version uid table too, so know which uids are 
		//	connected to which seqs
		
		if(!sys_write_enabled) return false;
		if(z_rom == null) return false;
		if(bank_uids == null) return false;
		
		NusRomInfo rominfo = z_rom.getRomInfo();
		if(rominfo == null) return false;
		
		final String ZSEQ_DIR = root_dir + SEP + ZeqerCore.DIRNAME_ZSEQ;
		boolean good = true;
		
		String idtblpath = ZSEQ_DIR + SEP + "seq_" + rominfo.getZeqerID() + ".bin";
		if(!FileBuffer.fileExists(idtblpath)) return false;
		FileBuffer inbuff = FileBuffer.createBuffer(idtblpath, true);
		int scount = (int)(inbuff.getFileSize() >>> 2);
		if(scount < 1) return false;
		int[] seqids = new int[scount];
		inbuff.setCurrentPosition(0L);
		for(int i = 0; i < scount; i++) seqids[i] = inbuff.nextInt();
		
		FileBuffer code = z_rom.loadCode(); code.setEndian(true);
		
		int bcount = bank_uids.length;
		long tst = rominfo.getCodeOffset_bnktable() + ((bcount+1) << 4);
		for(int i = 0; i < scount; i++){
			//Pull seq from table. If not found, warn and continue.
			ZeqerSeq zs = loadSeq(seqids[i]);
			if(zs == null){
				if(verbose) System.err.println("WARNING: Sequence at index " + i + " could not be found in zeqer table!");
				continue;
			}
			SeqTableEntry entry = zs.getTableEntry();
			
			//Get bnk/seq table record location
			long roff = Short.toUnsignedLong(code.shortFromFile(tst + (i<<1)));
			roff += tst;
			
			//Read record/save to entry
			int sbcount = Byte.toUnsignedInt(code.getByte(roff));
			if(sbcount > 1){
				for(int j = 0; j < sbcount; j++){
					int bidx = Byte.toUnsignedInt(code.getByte(roff+1+j));
					if(bidx < 0 || bidx >= bank_uids.length){
						if(verbose) System.err.println("WARNING: Bank index " + bidx + " out of range. Skipping seq " + i + "...");
						continue;
					}
					entry.addBank(bank_uids[bidx]);
				}	
			}
			else{
				int bidx = Byte.toUnsignedInt(code.getByte(roff+1));
				if(bidx < 0 || bidx >= bank_uids.length){
					if(verbose) System.err.println("WARNING: Bank index " + bidx + " out of range. Skipping seq " + i + "...");
					continue;
				}
				entry.setSingleBank(bank_uids[bidx]);
			}
			
			//Save zs
			UltraSeqFile.writeUSEQ(zs, ZSEQ_DIR + SEP + entry.getDataFileName());
		}
		saveSysTable();
		return good;
	}
	
	/*----- I/O -----*/
	
	public int importSeqMetaTSV(String tsv_path) throws IOException{
		int records = 0;
		if(seq_table_user != null) records += seq_table_user.importTSV(tsv_path);
		if(sys_write_enabled && seq_table_sys != null) records += seq_table_sys.importTSV(tsv_path);
		return records;
	}
	
	public int importSeqLabelTSV(String tsv_path) throws IOException{
		int import_count = 0;
		TSVTables tsv = new TSVTables(tsv_path);
		
		//Check for required fields...
		if(!tsv.hasField("SEQUID")){tsv.closeReader(); return 0;}
		if(!tsv.hasField("LABEL")){tsv.closeReader(); return 0;}
		if(!tsv.hasField("POSITION")){tsv.closeReader(); return 0;}
		
		//Loop through records...
		ZeqerSeq openSeq = null;
		int openSeqUID = 0;
		try{
			while(tsv.nextRecord()){
				int seqid = tsv.getValueAsHexInt("SEQUID");
				if(!sys_write_enabled && isSysSeq(seqid)){
					System.err.println("Sys Edit Mode is disabled. Could not update sequence " + String.format("%08x", seqid));
					continue;
				}
				
				if(seqid != openSeqUID){
					if(openSeq != null){
						//Save.
						saveSeq(openSeq);
					}
					//Open next seq.
					openSeqUID = seqid;
					openSeq = loadSeq(openSeqUID);
				}
				
				//Process this label
				ZeqerSeq.Label lbl = new ZeqerSeq.Label(tsv.getValue("LABEL"));
				lbl.setPosition(tsv.getValueAsInt("POSITION"));
				String stype = tsv.getValue("SEQTYPE");
				if(stype != null){
					if(stype.equals("SFX")) openSeq.setSFXSeqFlag(true);
				}
				
				//Parse module (if applicable), and add to seq
				String modname = tsv.getValue("MODULE");
				if(modname != null && !modname.isEmpty()){
					ZeqerSeq.Module mod = openSeq.getModuleByName(modname);
					if(mod == null){
						mod = new ZeqerSeq.Module(modname);
						openSeq.addModule(mod);
					}
					lbl.setModule(mod);
					mod.addLabel(lbl);
				}
				else{
					openSeq.addCommonLabel(lbl);
				}
				
				//Parse context...
				String ctxt = tsv.getValue("CTXT");
				if(ctxt != null){
					if(ctxt.contains(":")){
						String[] split = ctxt.split(":");
						if(split[0].equals("ecmd")){
							if(split[1].equals("seq")){
								lbl.setContext(ZeqerSeq.MMLCTXT_EDITABLE_SEQ);
							}
							else if(split[1].equals("chx")){
								lbl.setContext(ZeqerSeq.MMLCTXT_EDITABLE_CH);
							}
							else if(split[1].equals("lyrx")){
								lbl.setContext(ZeqerSeq.MMLCTXT_EDITABLE_LYR);
							}
							else if(split[1].equals("lyshx")){
								lbl.setContext(ZeqerSeq.MMLCTXT_EDITABLE_LYR_SHORT);
							}
							else lbl.setContext(ZeqerSeq.MMLCTXT_EDITABLE_CMD);
						}
						else if(split[0].equals("data")){
							if(split[1].equals("fbuff")){
								lbl.setContext(ZeqerSeq.MMLCTXT_DATA_FILTER);
							}
							else if(split[1].equals("env")){
								lbl.setContext(ZeqerSeq.MMLCTXT_DATA_ENV);
							}
							else if(split[1].equals("ptbl")){
								lbl.setContext(ZeqerSeq.MMLCTXT_DATA_PTBL);
							}
							else if(split[1].equals("qtbl")){
								lbl.setContext(ZeqerSeq.MMLCTXT_DATA_QTBL);
							}
							else if(split[1].equals("calltbl")){
								lbl.setContext(ZeqerSeq.MMLCTXT_DATA_CALLTBL);
							}
							else if(split[1].equals("buff")){
								lbl.setContext(ZeqerSeq.MMLCTXT_DATA_DBUFF);
							}
						}
						else if(split[0].equals("ibuff")){
							if(split[1].equals("seq")){
								lbl.setContext(ZeqerSeq.MMLCTXT_IBUFF_SEQ);
							}
							else if(split[1].equals("chx")){
								lbl.setContext(ZeqerSeq.MMLCTXT_IBUFF_CH);
							}
							else if(split[1].equals("lyrx")){
								lbl.setContext(ZeqerSeq.MMLCTXT_IBUFF_LYR);
							}
							else if(split[1].equals("lyshx")){
								lbl.setContext(ZeqerSeq.MMLCTXT_IBUFF_LYR_SHORT);
							}
							else lbl.setContext(ZeqerSeq.MMLCTXT_DATA_IBUFF);
						}
					}
					else{
						if(ctxt.equals("seq")){
							lbl.setContext(ZeqerSeq.MMLCTXT_SEQ);
						}
						else if(ctxt.equals("chx") || ctxt.startsWith("ch")){
							lbl.setContext(ZeqerSeq.MMLCTXT_CH_ANY);
						}
						else if(ctxt.equals("lyrx") || ctxt.startsWith("lyr")){
							lbl.setContext(ZeqerSeq.MMLCTXT_LY_ANY);
						}
						else if(ctxt.equals("lyshx") || ctxt.startsWith("lysh")){
							lbl.setContext(ZeqerSeq.MMLCTXT_LY_ANY_SHORTNOTES);
						}
						else if(ctxt.equals("ecmd")){
							lbl.setContext(ZeqerSeq.MMLCTXT_EDITABLE_CMD);
						}
						else if(ctxt.equals("ibuff")){
							lbl.setContext(ZeqerSeq.MMLCTXT_DATA_IBUFF);
						}
					}
				}
				
				import_count++;
			}
		} catch(NumberFormatException ex){
			System.err.println("CoreSeqManager.importSeqLabelTSV || Number parsing error caught!");
			ex.printStackTrace();
		}
		
		if(openSeq != null){
			saveSeq(openSeq);
		}
		tsv.closeReader();
		
		return import_count;
	}
	
	public int importSeqIOAliasTSV(String tsv_path) throws IOException{
		int import_count = 0;
		TSVTables tsv = new TSVTables(tsv_path);
		
		//Check for required fields...
		if(!tsv.hasField("SEQUID")){tsv.closeReader(); return 0;}
		if(!tsv.hasField("ALIAS")){tsv.closeReader(); return 0;}
		if(!tsv.hasField("CHANNEL")){tsv.closeReader(); return 0;}
		if(!tsv.hasField("IOSLOT")){tsv.closeReader(); return 0;}
		
		//Loop through records...
		ZeqerSeq openSeq = null;
		int openSeqUID = 0;
		try{
			while(tsv.nextRecord()){
				int seqid = tsv.getValueAsHexInt("SEQUID");
				if(!sys_write_enabled && isSysSeq(seqid)){
					System.err.println("Sys Edit Mode is disabled. Could not update sequence " + String.format("%08x", seqid));
					continue;
				}
				
				if(seqid != openSeqUID){
					if(openSeq != null){
						//Save.
						saveSeq(openSeq);
					}
					//Open next seq.
					openSeqUID = seqid;
					openSeq = loadSeq(openSeqUID);
				}
				
				//Process alias record...
				int ch = tsv.getValueAsInt("CHANNEL");
				int slot = tsv.getValueAsInt("IOSLOT");
				String modname = tsv.getValue("MODULE");
				
				ZeqerSeq.IOAlias alias = new ZeqerSeq.IOAlias(tsv.getValue("ALIAS"), ch, slot);
				
				if(modname != null && !modname.isEmpty()){
					ZeqerSeq.Module mod = openSeq.getModuleByName(modname);
					if(mod == null){
						mod = new ZeqerSeq.Module(modname);
						openSeq.addModule(mod);
					}
					alias.setModule(mod);
					mod.addIOAlias(alias);
				}
				else{
					openSeq.addCommonAlias(alias);
				}
				
				import_count++;
			}
		} catch(NumberFormatException ex){
			System.err.println("CoreSeqManager.importSeqIOAliasTSV || Number parsing error caught!");
			ex.printStackTrace();
		}
		
		if(openSeq != null){
			saveSeq(openSeq);
		}
		tsv.closeReader();
		
		return import_count;
	}
	
	public void saveSysHusks(boolean outputInstallPaths) throws IOException{
		if(!sys_write_enabled || (seq_table_sys == null)) return;
		String zseq_dir = root_dir + SEP + ZeqerCore.DIRNAME_ZSEQ;
		String husk_dir = zseq_dir + SEP + "scrubbed";
		if(!FileBuffer.directoryExists(husk_dir)){
			Files.createDirectories(Paths.get(husk_dir));
		}
		
		List<SeqTableEntry> entries = seq_table_sys.getAllEntries();
		for(SeqTableEntry entry : entries){
			String spath = zseq_dir + SEP + entry.getDataFileName();
			if(!FileBuffer.fileExists(spath)) continue;
			try{
				UltraSeqFile useq =  UltraSeqFile.openUSEQ(spath);
				ZeqerSeq zseq = UltraSeqFile.readUSEQ(useq, entry, false);
				useq.dispose();
				
				String fn = entry.getDataFileName();
				zseq.clearData();
				String cpypath = husk_dir + SEP + fn;
				UltraSeqFile.writeUSEQ(zseq, cpypath, null);
				
				if(outputInstallPaths){
					System.out.println("seqmeta/" + fn + ",seq/" + ZeqerCore.DIRNAME_ZSEQ + "/" + fn + ",0");
				}
			}
			catch(Exception ex){
				System.err.println("ZeqerCore.saveSysHusks || Failed to load seq " + String.format("%08x", entry.getUID()));
				ex.printStackTrace();
			}
		}
	}
	
	public boolean saveSeq(ZeqerSeq seq) throws IOException{
		if(seq == null) return false;
		if(seq.getTableEntry() == null) return false;
		
		seq.updateTableEntry();
		int uid = seq.getTableEntry().getUID();
		if(!sys_write_enabled && isSysSeq(uid)) return false;
		String writepath = getSeqDataFilePath(uid);
		if(writepath == null) return false;
		UltraSeqFile.writeUSEQ(seq, writepath);
		saveTables();
		return true;
	}
	
	public void saveSysTable() throws IOException{
		if(sys_write_enabled && seq_table_sys != null){
			seq_table_sys.writeTo(getSysTablePath());
		}
	}
	
	public void saveUserTable() throws IOException{
		if(seq_table_user != null){
			seq_table_user.writeTo(getUserTablePath());
		}
	}
	
	public void saveTables() throws IOException{
		if(sys_write_enabled && seq_table_sys != null){
			seq_table_sys.writeTo(getSysTablePath());
		}
		if(seq_table_user != null){
			seq_table_user.writeTo(getUserTablePath());
		}
	}

}
