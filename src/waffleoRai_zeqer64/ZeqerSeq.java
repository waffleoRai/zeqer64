package waffleoRai_zeqer64;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import waffleoRai_SeqSound.n64al.NUSALSeq;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileUtils;
import waffleoRai_zeqer64.filefmt.seq.UltraSeqFile;
import waffleoRai_zeqer64.filefmt.seq.SeqTableEntry;

public class ZeqerSeq {
	
	//Includes both the N64 sequence and also annotations. To easily
	//	serialize/read from USEQ files
	
	/*----- Constants -----*/
	
	public static final int MMLCTXT_UNK = -1;
	public static final int MMLCTXT_SEQ = 0;
	public static final int MMLCTXT_CH_ANY = 1;
	public static final int MMLCTXT_LY_ANY = 2;
	public static final int MMLCTXT_LY_ANY_SHORTNOTES = 3;
	public static final int MMLCTXT_DATA_QTBL = 4;
	public static final int MMLCTXT_DATA_PTBL = 5;
	public static final int MMLCTXT_DATA_DBUFF = 6;
	public static final int MMLCTXT_DATA_IBUFF = 7;
	public static final int MMLCTXT_DATA_FILTER = 8;
	public static final int MMLCTXT_DATA_ENV = 9;
	public static final int MMLCTXT_DATA_CALLTBL = 10;
	public static final int MMLCTXT_EDITABLE_CMD = 11;
	public static final int MMLCTXT_EDITABLE_SEQ = 12;
	public static final int MMLCTXT_EDITABLE_CH = 13;
	public static final int MMLCTXT_EDITABLE_LYR = 14;
	public static final int MMLCTXT_EDITABLE_LYR_SHORT = 15;
	public static final int MMLCTXT_IBUFF_SEQ = 16;
	public static final int MMLCTXT_IBUFF_CH = 17;
	public static final int MMLCTXT_IBUFF_LYR = 18;
	public static final int MMLCTXT_IBUFF_LYR_SHORT = 19;
	
	public static final int IOALIAS_CH_SEQ = -1;
	public static final int IOALIAS_CH_ANY = -2;
	
	public static final int PARSE_ERR_NONE = 0;
	public static final int PARSE_ERR_CRASH = 1;
	public static final int PARSE_ERR_TIMEOUT = 2;
	public static final int PARSE_ERR_OTHER_IRQ = 3;
	
	public static final int SEQTYPE_UNKNOWN = 0;
	public static final int SEQTYPE_BGM = 1;
	public static final int SEQTYPE_BGM_PROG = 2;
	public static final int SEQTYPE_BGM_PIECE = 3;
	public static final int SEQTYPE_JINGLE = 4;
	public static final int SEQTYPE_OCARINA = 5;
	public static final int SEQTYPE_CUTSCENE = 6;
	public static final int SEQTYPE_AMBIENT = 7;
	public static final int SEQTYPE_SFX = 8;
	public static final int SEQTYPE_INTRMUS = 9;
	
	/*----- Inner Classes -----*/
	
	public static class Module{
		private int index = -1;
		
		private String name;
		private int start_pos;
		private int end_pos;
		
		private List<Label> labels;
		private List<IOAlias> aliases;
		
		public Module(String module_name){
			name = module_name;
			start_pos = -1;
			end_pos = -1;
			labels = new LinkedList<Label>();
			aliases = new LinkedList<IOAlias>();
		}
		
		public String getName(){return name;}
		public int getStartPosition(){return start_pos;}
		public int getEndPosition(){return end_pos;}
		public int getIndex(){return index;}
		
		public List<Label> getAllLabels(){
			ArrayList<Label> copy = new ArrayList<Label>(labels.size()+1);
			copy.addAll(labels);
			return copy;
		}
		
		public List<IOAlias> getAllIOAliases(){
			ArrayList<IOAlias> copy = new ArrayList<IOAlias>(aliases.size()+1);
			copy.addAll(aliases);
			return copy;
		}
		
		public void setName(String str){name = str;}
		public void setStartPosition(int val){start_pos = val;}
		public void setEndPosition(int val){end_pos = val;}
		public void setIndex(int val){index = val;}
		public void addLabel(Label lbl){labels.add(lbl);}
		public void clearLabels(){labels.clear();}
		public void addIOAlias(IOAlias alias){aliases.add(alias);}
		public void clearIOAliases(){aliases.clear();}
	}
	
	public static class Label{
		private int context = MMLCTXT_UNK;
		private Module module = null;
		private int data_size = -1; //Only used for data.
		private int lbl_pos = -1;
		private String label;
		
		public Label(String name){
			label = name;
		}
		
		public int getContext(){return context;}
		public Module getModule(){return module;}
		public int getDataSize(){return data_size;}
		public int getPosition(){return lbl_pos;}
		public String getName(){return label;}
		
		public void setContext(int val){context = val;}
		public void setModule(Module val){module = val;}
		public void setDataSize(int val){data_size = val;}
		public void setPosition(int val){lbl_pos = val;}
		public void setName(String val){label = val;}
	}
	
	public static class IOAlias{
		private String alias;
		private int channel; //-1 is seq, -2 is any channel
		private int io_slot;
		private Module module = null;
		
		public IOAlias(String name, int ch, int slot){
			alias = name;
			channel = ch;
			io_slot = slot;
		}
		
		public String getName(){return alias;}
		public int getChannel(){return channel;}
		public int getSlot(){return io_slot;}
		public Module getModule(){return module;}
		
		public void setName(String val){alias = val;}
		public void setChannel(int val){channel = val;}
		public void setSlot(int val){io_slot = val;}
		public void setModule(Module val){module = val;}
	}
	
	/*----- Instance Variables -----*/
	
	private SeqTableEntry table_entry = null; //Direct link to metadata.
	
	private boolean oot_compat = false;
	private boolean layers_over_four = false;
	private boolean sfx_seq = false;
	private int max_voice_load = -1;
	
	private ArrayList<Module> modules;
	private List<Label> common_labels;
	private List<IOAlias> common_aliases;
	
	private String useq_path = null;
	private NUSALSeq sequence = null;
	private FileBuffer raw_data = null; //Only used if seq cannot be parsed!
	
	/*----- Initialization -----*/
	
	protected ZeqerSeq(){
		common_labels = new LinkedList<Label>();
		common_aliases = new LinkedList<IOAlias>();
	}
	
	public ZeqerSeq(SeqTableEntry tableEntry){
		this();
		table_entry = tableEntry;
	}
	
	/*----- Getters -----*/
	
	public SeqTableEntry getTableEntry(){return table_entry;}
	public boolean isOoTCompatible(){return oot_compat;}
	public boolean canUseMoreThanFourLayers(){return layers_over_four;}
	public boolean isSFXSeq(){return sfx_seq;}
	public int getMaxVoiceLoad(){return max_voice_load;}
	
	public List<Label> getCommonLabels(){
		List<Label> copy = new LinkedList<Label>();
		copy.addAll(common_labels);
		return copy;
	}
	
	public List<IOAlias> getCommonAliases(){
		List<IOAlias> copy = new LinkedList<IOAlias>();
		copy.addAll(common_aliases);
		return copy;
	}
	
	public boolean isParsable(){return sequence != null;}
	public NUSALSeq getSequence(){return sequence;}
	public FileBuffer getRawData(){return raw_data;}
	
	public boolean isMetaHusk(){
		return(sequence == null) && (raw_data == null);
	}
	
	public boolean dataLoaded(){
		//Anything in the useq, including the modules and that.
		return sequence != null || raw_data != null;
	}
	
 	public Module getModule(int index){
		if(index < 0 || modules == null) return null;
		if(index >= modules.size()) return null;
		return modules.get(index);
	}
 	
 	public Module[] getModules(){
 		if(modules == null) return null;
 		int modcount = modules.size();
 		Module[] modlist = new Module[modcount];
 		int i = 0;
 		for(Module m : modules) modlist[i++] = m;
 		return modlist;
 	}
	
 	public Module getModuleByName(String name){
 		if(modules == null) return null;
 		for(Module m : modules){
 			if(m.getName().equalsIgnoreCase(name)) return m;
 		}
 		return null;
 	}
 	
	/*----- Setters -----*/
	
	public void setOoTCompatible(boolean b){oot_compat = b;}
	public void setMoreThanFourLayers(boolean b){layers_over_four = b;}
	public void setSFXSeqFlag(boolean b){sfx_seq = b;}
	public void setMaxVoiceLoad(int val){max_voice_load = val;}
	public void setSequence(NUSALSeq seq){sequence = seq;}
	public void setRawData(FileBuffer val){raw_data = val;}
	public void setSourcePath(String val){useq_path = val;}
	
	public boolean loadData(boolean attemptSeqParse){
		//Everything not in the table entry, including modules etc.
		if(useq_path == null || useq_path.isEmpty()) return false;
		if(!FileBuffer.fileExists(useq_path)) return false;
		
		try{
			UltraSeqFile myfile = UltraSeqFile.openUSEQ(useq_path);
			UltraSeqFile.readUSEQInto(myfile, this, attemptSeqParse);
		}
		catch(Exception ex){
			ex.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public void unloadData(){
		//Everything not in the table entry, including modules etc.
		if(modules != null) modules.clear();
		if(common_labels != null) common_labels.clear();
		if(common_aliases != null) common_aliases.clear();
		sequence = null;
		raw_data = null;
	}
	
	public void clearData(){
		sequence = null;
		raw_data = null;
	}
	
	public void allocModuleList(int size){modules = new ArrayList<Module>(size+1);}
	
	public void addModule(Module mod){
		if(modules == null){
			modules = new ArrayList<Module>();
		}
		mod.setIndex(modules.size()); 
		modules.add(mod);
	}
	
	public void addCommonLabel(Label lbl){
		common_labels.add(lbl);
	}
	
	public void addCommonAlias(IOAlias ioalias){
		common_aliases.add(ioalias);
	}
	
	/*----- Reading -----*/
	
	public static NUSALSeq parseSeqWithTimeout(FileBuffer rawdata, int timeout_millis, ZeqerErrorCode error){
		TimeoutParser runner = new TimeoutParser(rawdata);
		Thread th = new Thread(runner);
		int wait_count = 0;
		final int SLEEP_TIME = 50;
		
		if(error != null) error.setValue(PARSE_ERR_NONE);
		
		th.setDaemon(true);
		th.start();
		while((th.isAlive()) && (wait_count < timeout_millis)){
			try {Thread.sleep(SLEEP_TIME);} 
			catch (InterruptedException e) {
				if(th.isAlive()){
					synchronized(th){th.interrupt();}
					if(error != null){
						error.setValue(PARSE_ERR_OTHER_IRQ);
					}
					return null;
				}
				break;
			}
			wait_count += SLEEP_TIME;
		}
		
		if(th.isAlive()){
			//Time out and kill
			synchronized(th){th.interrupt();}
			if(error != null){
				error.setValue(PARSE_ERR_TIMEOUT);
			}
			return null;
		}
		
		//Wait for it to finish.
		while(th.isAlive()){
			try {Thread.sleep(10);} 
			catch (InterruptedException e) {
				error.setValue(PARSE_ERR_OTHER_IRQ);
				return null;
			}
		}
		
		NUSALSeq seq = runner.getResult();
		Exception ex = runner.getError();
		if(seq != null && ex == null) return seq;
		
		if(error != null){
			error.setValue(PARSE_ERR_CRASH);
		}
		return null;
	}
	
	/*----- Writing -----*/
	
	public boolean updateTableEntry(){
		//Updates md5 and timestamp
		if(table_entry == null) return false;
		if(raw_data != null){
			byte[] dat = raw_data.getBytes(0, raw_data.getFileSize());
			byte[] md5 = FileUtils.getMD5Sum(dat);
			dat = null;
			table_entry.updateMD5(md5);
			return true;
		}
		else{
			//Try to serialize it.
			if(sequence != null){
				FileBuffer buff = sequence.getSerializedData();
				byte[] dat = buff.getBytes(0, buff.getFileSize());
				byte[] md5 = FileUtils.getMD5Sum(dat);
				dat = null;
				table_entry.updateMD5(md5);
				return true;
			}
		}
		return false;
	}
	
	public boolean save(){
		if(!updateTableEntry()) return false;
		if(sequence == null && raw_data == null) return false;
		
		if(useq_path == null) return false;
		try {
			UltraSeqFile.writeUSEQ(this, useq_path);
		} 
		catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	/*----- Misc. -----*/
	
	private static class TimeoutParser implements Runnable{

		private FileBuffer rawdat = null;
		private volatile NUSALSeq result = null;
		private volatile Exception error = null;
		
		public TimeoutParser(FileBuffer input){
			rawdat = input;
		}
		
		public void run() {
			try{
				synchronized(this){result = NUSALSeq.readNUSALSeq(rawdat);}
			}
			catch(InterruptedException ex){
				System.err.println("ZeqerSeq.TimeoutParser.run || Parser timed out and was interrupted.");
				synchronized(this){
					result = null;
					error = null;
				}
			}
			catch(Exception ex){
				synchronized(this){
					error = ex;
					result = null;
				}
			}
		}
		
		public synchronized NUSALSeq getResult(){return result;}
		public synchronized Exception getError(){return error;}
		
	}
	

}
