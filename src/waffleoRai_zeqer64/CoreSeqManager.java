package waffleoRai_zeqer64;

import java.io.File;
import java.io.IOException;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_zeqer64.filefmt.TSVTables;
import waffleoRai_zeqer64.filefmt.UltraSeqFile;
import waffleoRai_zeqer64.filefmt.ZeqerSeqTable;
import waffleoRai_zeqer64.filefmt.ZeqerSeqTable.SeqTableEntry;

class CoreSeqManager {
	
	private static final char SEP = File.separatorChar;
	
	/*----- Instance Variables -----*/
	
	private String root_dir; //Base seq dir (ie. zeqerbase/seq)
	private boolean sys_write_enabled = false;
	
	private ZeqerSeqTable seq_table_sys;
	private ZeqerSeqTable seq_table_user;
	
	/*----- Init -----*/
	
	public CoreSeqManager(String base_seq_dir, boolean sysMode) throws UnsupportedFileTypeException, IOException{
		//Loads tables too, or creates if not there.
		root_dir = base_seq_dir;
		sys_write_enabled = sysMode;
		
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
	
	/*----- Getters -----*/
	
	public String getSysTablePath(){
		return root_dir + SEP + ZeqerCore.DIRNAME_ZSEQ + SEP + ZeqerCore.FN_SYSSEQ;
	}
	
	public String getUserTablePath(){
		return root_dir + SEP + ZeqerCore.FN_USRSEQ;
	}
	
	public String getSeqDataFilePath(int uid){
		if(isSysSeq(uid)){
			return getSysTablePath() + SEP + String.format("%08x.buseq", uid);
		}
		else{
			return getUserTablePath() + SEP + String.format("%08x.buseq", uid);	
		}
	}
	
	public boolean isSysSeq(int uid){
		if(seq_table_sys != null){
			return seq_table_sys.getSequence(uid) != null;
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
			ZeqerSeq zseq = UltraSeqFile.readUSEQ(spath);
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
	
	/*----- Setters -----*/
	
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
	
	public boolean saveSeq(ZeqerSeq seq) throws IOException{
		if(seq == null) return false;
		if(seq.getTableEntry() == null) return false;
		int uid = seq.getTableEntry().getUID();
		if(!sys_write_enabled && isSysSeq(uid)) return false;
		String writepath = getSeqDataFilePath(uid);
		if(writepath == null) return false;
		UltraSeqFile.writeUSEQ(seq, writepath);
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
