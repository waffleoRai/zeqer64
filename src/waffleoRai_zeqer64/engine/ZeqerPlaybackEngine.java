package waffleoRai_zeqer64.engine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import waffleoRai_Sound.nintendo.N64ADPCMTable;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.MultiFileBuffer;
import waffleoRai_soundbank.nintendo.z64.Z64Bank;
import waffleoRai_soundbank.nintendo.z64.Z64Drum;
import waffleoRai_soundbank.nintendo.z64.Z64Envelope;
import waffleoRai_soundbank.nintendo.z64.Z64Instrument;
import waffleoRai_soundbank.nintendo.z64.Z64SoundEffect;
import waffleoRai_zeqer64.SoundTables.WaveArcInfoEntry;
import waffleoRai_zeqer64.filefmt.RecompFiles;
import waffleoRai_zeqer64.ZeqerCore;
import waffleoRai_zeqer64.ZeqerRom;

public class ZeqerPlaybackEngine {

	/*----- Constants -----*/
	
	public static final String MAGICNO = "aBld";
	public static final short VERSION = 1;
	
	public static final int DEFO_VCART_ALLOC = 0x800000;
	
	/*----- Font Transfer Commands -----*/
	
	/*
	 * For best arch/compiler compatibility (due to stupid padding, mostly)
	 * Soundfonts/banks are transferred as streams of ints instead of in their native
	 * serialized format...
	 */
	
	/*
	 * Font count
	 * STARTFONT
	 * Reference (highest bit set if reference, other bits are idx. 0 means not ref)
	 * Tbl Word 3 (Cache, Medium, Warcs)
	 * Tbl Word 4 (Inst counts)
	 * -- IF not reference
	 * STARTINST_TBL
	 * 	InstID...
	 * STARTWAVEHDR
	 * 	ID (For offset mapping)
	 * 	Offset
	 * 	Length
	 * 	Codec | Has Book? | Has Loop? (lowest 2 bytes)
	 * STARTADPCMBOOK
	 * 	Order
	 * 	PredCount
	 * 	Val|Val ... (A value in each halfword. Higher is first)
	 * STARTWAVELOOP
	 * 	Start
	 * 	End
	 * 	Count
	 * 	Val|Val (If appl.)
	 * STARTENV
	 * 	ID
	 * 	Word Count
	 *  Word...
	 * STARTINST
	 * 	ID
	 * 	Word 1 (Decay, key ranges)
	 * 	EnvID
	 * 	LoSmplID
	 * 	LoTune
	 * 	MidSmplID
	 * 	MidTune
	 * 	HiSmplID
	 * 	HiTune
	 * STARTDRUMTBL
	 * 	DrumID...
	 * STARTDRUM
	 * 	ID
	 * 	Word 1 (Decay, Pan etc.)
	 * 	Env
	 * 	SmplID
	 * 	Tune
	 * STARTSFX
	 * 	SmplID
	 * 	Tune
	 * --
	 * ENDFONT
	 */
	
	private static final int FNT_TXCMD_STARTFONT = 0x80000001;
	private static final int FNT_TXCMD_ENDFONT = 0x80FFFFFF;
	
	private static final int FNT_TXCMD_STARTWAVEHDR = 0x80000002;
	private static final int FNT_TXCMD_STARTADPCMBOOK = 0x80000003;
	private static final int FNT_TXCMD_STARTWAVELOOP = 0x80000004;
	private static final int FNT_TXCMD_STARTENV = 0x80000005;
	private static final int FNT_TXCMD_STARTINST = 0x80000006;
	private static final int FNT_TXCMD_STARTDRUM = 0x80000007;
	private static final int FNT_TXCMD_STARTSFX = 0x80000008;
	
	private static final int FNT_TXCMD_STARTINST_TBL = 0x800000FF;
	private static final int FNT_TXCMD_STARTDRUM_TBL = 0x800000DD;
	
	/*----- Instance Variables -----*/
	
	//-> audiobank
	private ArrayList<EngineBankInfo> banks;
	
	//-> audioseq
	private ArrayList<EngineSeqInfo> seqs;
	
	//-> audiotable
	private ArrayList<EngineWaveArcInfo> warcs;
	
	//-> settings
	private int tv_type = ZeqerRom.TV_TYPE__NTSC;
	private boolean oot_mode = false;
	
	//-> state
	private boolean is_running = false;
	
	/*----- Native Method Declarations -----*/
	
	private native int loadAudiobank(int[] bankStream);
	private native int loadAudioseq(int[] seqtbl, byte[] seqfonttbl, byte[] data);
	private native int loadAudiotable(int[] tbltbl, byte[] data);
	
	private native int bootEngine_native(int tvmode, boolean ootmode, int alloc_vcart_data);//Probably alloc 8MB?
	private native int shutdownEngine_native();
	
	/*----- Initialization -----*/
	
	public ZeqerPlaybackEngine() {
		this(50, 150, 10);
	}
	
	public ZeqerPlaybackEngine(int bank_alloc, int seq_alloc, int warc_alloc) {
		bank_alloc = (bank_alloc>0)?bank_alloc:4;
		seq_alloc = (seq_alloc>0)?seq_alloc:4;
		warc_alloc = (warc_alloc>0)?warc_alloc:4;
		
		banks = new ArrayList<EngineBankInfo>(bank_alloc);
		seqs  = new ArrayList<EngineSeqInfo>(seq_alloc);
		warcs = new ArrayList<EngineWaveArcInfo>(warc_alloc);
	}
	
	/*----- Save/Load -----*/
	
	public static ZeqerPlaybackEngine loadBuild(String path){
		//TODO
		return null;
	}
	
	public void saveBuild(String path){
		//TODO
	}
	
	public static ZeqerPlaybackEngine fromRom(ZeqerRom rom){
		//TODO
		return null;
	}
	
	/*----- Getters -----*/
	
	public int getTVMode() {return tv_type;}
	public boolean getOoTMode() {return oot_mode;}
	
	public boolean isRunning() {return is_running;}
	
	public int bankCount() {return banks.size();}
	public int seqCount() {return seqs.size();}
	public int waveArchiveCount() {return warcs.size();}
	
	/*----- Setters -----*/
	
	public void setTVMode(int val) {if(is_running) return; tv_type = val;}
	public void setOoTMode(boolean b) {if(is_running) return; oot_mode = b;}
	
	public EngineBankInfo addBank(int bank_uid) {
		if(is_running) return null;
		EngineBankInfo info = new EngineBankInfo();
		banks.add(info);
		info.setBankUID(bank_uid);
		return info;
	}
	
	public EngineSeqInfo addSeq(int seq_uid) {
		if(is_running) return null;
		EngineSeqInfo info = new EngineSeqInfo();
		seqs.add(info);
		info.setSeqUID(seq_uid);
		return info;
	}
	
	public EngineWaveArcInfo addWaveArchive(int sample_alloc) {
		if(is_running) return null;
		EngineWaveArcInfo info = new EngineWaveArcInfo(sample_alloc);
		warcs.add(info);
		return info;
	}
	
	public EngineWaveArcInfo addWaveArchiveReference(int ref_arc_idx) {
		if(is_running) return null;
		if(ref_arc_idx < 0 || ref_arc_idx >= warcs.size()) return null;
		//Needs its own obj to hold alt cache/medium vals if required
		EngineWaveArcInfo info = new EngineWaveArcInfo(1);
		info.setRefIndex(ref_arc_idx);
		warcs.add(info);
		return info;
	}
	
	public boolean clear() {
		if(is_running) return false;
		banks.clear();
		seqs.clear();
		warcs.clear();
		return true;
	}
	
	/*----- Internal Data Transfer -----*/
	
	private Map<Integer, Z64WaveInfo> winfomap;
	private ArrayList<Map<Integer, Integer>> woffmap;
	
	private boolean transferAudiotable() {
		//Includes the code table and the file
		int warc_count = warcs.size();
		if(warc_count < 1) return false;
		woffmap = new ArrayList<Map<Integer, Integer>>(warc_count);
		winfomap = new TreeMap<Integer, Z64WaveInfo>();
		WaveArcInfoEntry[] wtbl = new WaveArcInfoEntry[warc_count];
		Map<Integer, FileBuffer> loaded_wdat = new TreeMap<Integer, FileBuffer>();
		
		//Count max segments...
		int maxseg = 0;
		for(EngineWaveArcInfo warinfo : warcs){
			if(!warinfo.isReference()){
				maxseg += (warinfo.countSamples() << 1);
			}
		}
		
		MultiFileBuffer buff = new MultiFileBuffer(maxseg+1);
		int atoff = 0;
		int warc_off = 0, pad_off = 0, pad_amt = 0;
		for(int i = 0; i < warc_count; i++){
			EngineWaveArcInfo warinfo = warcs.get(i);
			Map<Integer, Integer> soffmap;
			wtbl[i] = new WaveArcInfoEntry();
			wtbl[i].setCachePolicy(warinfo.getCachePolicy());
			wtbl[i].setMedium(warinfo.getMedium());
			if(warinfo.isReference()){
				int ridx = warinfo.getRefIndex();
				wtbl[i].setSize(0);
				wtbl[i].setOffset(ridx);
				if(ridx >= woffmap.size()){
					soffmap = new TreeMap<Integer, Integer>();
				}
				else{
					soffmap = woffmap.get(ridx);	
				}
				woffmap.add(soffmap);	
				continue;
			}
			
			//Standard.
			warc_off = 0;
			soffmap = new TreeMap<Integer, Integer>();
			woffmap.add(soffmap);
			wtbl[i].setOffset(atoff);
			List<Integer> sampleids = warinfo.getSamples();
			for(Integer id : sampleids){
				if(soffmap.containsKey(id)) continue; //No duplicates.
				Z64WaveInfo info = winfomap.get(id);
				if(info == null){
					//Load
					info = ZeqerCore.getActiveCore().getWaveInfo(id);
					if(info == null){
						System.err.println("No sample with ID 0x" + String.format("%08x", id) + " found.");
						return false;
					}
					winfomap.put(id, info);
				}
				//Map warc offset.
				soffmap.put(id, warc_off);
				//See if data are already loaded.
				FileBuffer wbuff = loaded_wdat.get(id);
				//Load if not
				if(wbuff == null){
					wbuff = ZeqerCore.getActiveCore().loadWaveData(id);
					if(wbuff == null){
						System.err.println("No sample data with ID 0x" + String.format("%08x", id) + " found.");
						return false;
					}
					loaded_wdat.put(id, wbuff);
				}
				
				//Add to buffer
				buff.addToFile(wbuff);
				warc_off += (int)wbuff.getFileSize();
				
				//Add needed padding to buffer
				pad_off = warc_off + 0xF;
				pad_off &= ~0xF;
				pad_amt = pad_off - warc_off;
				if(pad_amt > 0){
					FileBuffer padbuff = new FileBuffer(pad_amt);
					for(int j = 0; j < pad_amt; j++) padbuff.addToFile(FileBuffer.ZERO_BYTE);
					buff.addToFile(padbuff);
					warc_off = pad_off;
				}
			}
			
			wtbl[i].setSize(warc_off);
			atoff += warc_off;
		}
		
		//Render table and audiotable and send to native func
		int[] tbltbl = new int[1 + (warc_count*3)];
		int ctr = 0;
		tbltbl[ctr++] = warc_count;
		for(int i = 0; i < warc_count; i++){
			tbltbl[ctr++] = wtbl[i].getOffset();
			tbltbl[ctr++] = wtbl[i].getSize();
			tbltbl[ctr++] = ((wtbl[i].getMedium() & 0xffff) << 16) | (wtbl[i].getCachePolicy() & 0xffff);
		}
		
		if(loadAudiotable(tbltbl, buff.getBytes()) != 0) return false;
		
		//Clean up?
		try{buff.dispose();}
		catch(Exception ex){ex.printStackTrace();}
		
		return true;
	}
	
	private int bankContentsToStream(Z64Bank bnk, int[] maparr, LinkedList<int[]> output){
		int wordcount = 0;
		int randid = 0x1234;
		
		//Get presets, and do inst table
		int[] wordblock = new int[1+maparr[1]];
		wordblock[0] = FNT_TXCMD_STARTINST_TBL;
		int pos = 1;
		
		Z64Instrument[] presets = bnk.getInstrumentPresets();
		int[] inst_ids = new int[126];
		for(int i = 0; i < maparr[1]; i++){
			if(presets[i] != null){
				inst_ids[i] = randid++;
				wordblock[pos++] = inst_ids[i];
			}
			else{
				wordblock[pos++] = 0;
			}
		}
		output.add(wordblock);
		wordcount += wordblock.length;
		
		//Get all sample headers and do sample blocks
		/*Collection<Integer> waves_ids = bnk.getAllWaveUIDs();
		Map<Integer, Integer> offmap = woffmap.get(bnk.getPrimaryWaveArchiveIndex());
		
		//Eh fuggit, I don't feel like merging ADPCM tables
		for(Integer id : waves_ids){
			wordblock = new int[5];
			wordblock[0] = FNT_TXCMD_STARTWAVEHDR;
			pos = 1;
			
			Z64WaveInfo winfo = winfomap.get(id);
			N64ADPCMTable book = winfo.getADPCMBook();
			
			//Main info
			wordblock[pos++] = id;
			wordblock[pos++] = offmap.get(id);
			wordblock[pos++] = winfo.getWaveSize();
			wordblock[pos++] = ((winfo.getCodec() & 0xFF) << 16) | (0x0101);
			output.add(wordblock);
			wordcount += wordblock.length;
			
			//ADPCM Book
			int order = book.getOrder();
			int pcount = book.getPredictorCount();
			int entries = order * pcount * 8;
			wordblock = new int[3+(entries/2)];
			wordblock[0] = FNT_TXCMD_STARTADPCMBOOK;
			pos = 1;
			wordblock[pos++] = order;
			wordblock[pos++] = pcount;
			for(int p = 0; p < pcount; p++){
				for(int o = 0; o < order; o++){
					for(int i = 0; i < 8; i+=2){
						wordblock[pos++] = ((book.getCoefficient(p, o, i) & 0xffff) << 16) | 
								(book.getCoefficient(p, o, i+1) & 0xffff);
					}
				}
			}
			output.add(wordblock);
			wordcount += wordblock.length;
			
			//Loop
			int lc = winfo.getLoopCount();
			if(lc != 0) wordblock = new int[4+8];
			else wordblock = new int[4];
			wordblock[0] = FNT_TXCMD_STARTWAVELOOP;
			pos = 1;
			wordblock[pos++] = winfo.getLoopStart();
			wordblock[pos++] = winfo.getLoopEnd();
			wordblock[pos++] = lc;
			if(lc != 0){
				short[] loop_state = winfo.getLoopState();
				for(int i = 0; i < 16; i+=2){
					int hi = (int)loop_state[i] << 16;
					int lo = (int)loop_state[i+1] & 0xffff;
					wordblock[pos++] = hi | lo;
				}
			}
			output.add(wordblock);
			wordcount += wordblock.length;
		}
		
		//Get all envelopes and do envelope blocks
		Collection<Z64Envelope> envs = bnk.getAllEnvelopes();
		for(Z64Envelope e : envs){
			e.setID(randid++);
			int ecount = e.eventCount();
			
			wordblock = new int[3+ecount];
			wordblock[0] = FNT_TXCMD_STARTENV;
			pos = 1;
			wordblock[pos++] = e.getID();
			wordblock[pos++] = ecount;
			List<short[]> events = e.getEvents();
			for(short[] event : events){
				int hi = (int)event[0] << 16;
				int lo = (int)event[1] & 0xffff;
				wordblock[pos++] = hi | lo;
			}
			output.add(wordblock);
			wordcount += wordblock.length;
		}
		
		//Do instrument blocks
		for(int i = 0; i < maparr[1]; i++){
			if(presets[i] != null){
				wordblock = new int[10];
				wordblock[0] = FNT_TXCMD_STARTINST;
				pos = 1;
				
				wordblock[pos++] = inst_ids[i];
				wordblock[pos++] = (((int)presets[i].getLowRangeTop() & 0xFF) << 24) 
						| (((int)presets[i].getHighRangeBottom() & 0xFF) << 16) 
						| ((int)presets[i].getDecay() & 0xff);
				
				Z64Envelope env = presets[i].getEnvelope();
				if(env == null) wordblock[pos++] = 0;
				else wordblock[pos++] = env.getID();
				
				Z64WaveInfo winfo = presets[i].getSampleLow();
				if(winfo != null){
					wordblock[pos++] = winfo.getUID();
					wordblock[pos++] = Float.floatToRawIntBits(presets[i].getTuningLow());
				}
				else{
					wordblock[pos++] = 0;
					wordblock[pos++] = 0;
				}
				
				winfo = presets[i].getSampleMiddle();
				wordblock[pos++] = winfo.getUID();
				wordblock[pos++] = Float.floatToRawIntBits(presets[i].getTuningMiddle());
				
				winfo = presets[i].getSampleHigh();
				if(winfo != null){
					wordblock[pos++] = winfo.getUID();
					wordblock[pos++] = Float.floatToRawIntBits(presets[i].getTuningHigh());
				}
				else{
					wordblock[pos++] = 0;
					wordblock[pos++] = 0;
				}
				
				output.add(wordblock);
				wordcount += wordblock.length;
			}
		}
		
		//Perc blocks
		int drumcount = maparr[2];
		Z64Drum[] drums = bnk.getPercussionSet();
		if(drums != null){
			
			int[] drumids = new int[drums.length];
			wordblock = new int[drumcount+1];
			wordblock[0] = FNT_TXCMD_STARTDRUM_TBL;
			pos = 1;
			for(int i = 0; i < drumcount; i++){
				if(drums[i] != null){
					drumids[i] = randid++;
					wordblock[pos++] = drumids[i];
				}
				else{
					wordblock[pos++] = 0;
				}
			}
			output.add(wordblock);
			wordcount += wordblock.length;
			
			for(int i = 0; i < drumcount; i++){
				if(drums[i] != null){
					wordblock = new int[5];
					wordblock[0] = FNT_TXCMD_STARTDRUM;
					pos = 1;
					
					wordblock[pos++] = drumids[i];
					wordblock[pos++] = (((int)drums[i].getDecay() & 0xff) << 24) | 
							(((int)drums[i].getPan() & 0xff) << 16);
					
					Z64WaveInfo winfo = drums[i].getSample();
					wordblock[pos++] = winfo.getUID();
					wordblock[pos++] = Float.floatToRawIntBits(Z64Drum.commonToLocalTuning(i, drums[i].getTuning())); //
					
					Z64Envelope env = drums[i].getEnvelope();
					if(env == null) wordblock[pos++] = 0;
					else wordblock[pos++] = env.getID();
					
					output.add(wordblock);
					wordcount += wordblock.length;	
				}
			}
		}
		
		//SFX
		int sfxcount = maparr[3];
		Z64SoundEffect[] sfxset = bnk.getSFXSet();
		if(sfxset != null){
			for(int i = 0; i < sfxcount; i++){
				wordblock = new int[3];
				wordblock[0] = FNT_TXCMD_STARTSFX;
				pos = 1;
				
				if(sfxset[i] != null){
					wordblock[pos++] = sfxset[i].getSample().getUID();
					wordblock[pos++] = Float.floatToRawIntBits(sfxset[i].getTuning());
				}
				
				output.add(wordblock);
				wordcount += wordblock.length;	
			}
		}*/
		
		return wordcount;
	}
	
	private boolean transferAudiobank() {
		if(winfomap == null || woffmap == null) return false;
		int bnk_count = banks.size();
		Map<Integer, int[]> bnk_ref_map = new TreeMap<Integer, int[]>();
		LinkedList<int[]> wordlist = new LinkedList<int[]>();
		int[] wordblock = null;
		int wordcount = 0;
		
		wordlist.add(new int[]{bnk_count});
		wordcount++;
		
		int word = 0;
		for(int i = 0; i < bnk_count; i++){
			wordblock = new int[4];
			wordlist.add(wordblock); wordcount += 4;
			
			EngineBankInfo binfo = banks.get(i);
			wordblock[0] = FNT_TXCMD_STARTFONT;
			
			int bnkid = binfo.getBankUID();
			int refid = -1;
			if(bnk_ref_map.containsKey(bnkid)){
				refid = bnk_ref_map.get(bnkid)[0];
				wordblock[1] = 0x80000000 | refid;
			}
			else{
				wordblock[1] = 0;
			}
			
			word = (binfo.getMedium() & 0xff) << 24;
			word |= (binfo.getCachePolicy() & 0xff) << 16;
			word |= (binfo.getWArc1() & 0xff) << 8;
			word |= (binfo.getWArc2() & 0xff);
			wordblock[2] = word;
			
			if(refid < 0){
				//Have to actually serialize font
				Z64Bank mybank = ZeqerCore.getActiveCore().loadBank(bnkid);
				if(mybank == null){
					System.err.println("ZeqerPlaybackEngine.transferAudiobank || ERROR: Bank with ID 0x" + 
							String.format("%08x", bnkid) + " could not be found!");
					return false;
				}
				
				//Get instrument counts and map to bnk_ref_map
				int[] maparr = new int[]{i, 
						mybank.getEffectiveInstCount(), 
						mybank.getEffectivePercCount(), 
						mybank.getEffectiveSFXCount()};
				bnk_ref_map.put(bnkid, maparr);
				word = (maparr[1] & 0xff) << 24;
				word |= (maparr[2] & 0xff) << 16;
				word |= (maparr[3] & 0xffff);
				wordblock[3] = word;
				
				mybank.setPrimaryWaveArcIndex(binfo.getWArc1());
				wordcount += bankContentsToStream(mybank, maparr, wordlist);
			}
			else{
				//Get instrument counts from back reference...
				int[] backref = bnk_ref_map.get(bnkid);
				word = (backref[1] & 0xff) << 24;
				word |= (backref[2] & 0xff) << 16;
				word |= (backref[3] & 0xffff);
				wordblock[3] = word;
			}
			
			wordlist.add(new int[]{FNT_TXCMD_ENDFONT});
			wordcount++;
		}
		
		//Render wordlist to int array, then pass to native
		int[] bnkstr = new int[wordcount];
		int pos = 0;
		for(int[] block : wordlist){
			for(int i = 0; i < block.length; i++){
				bnkstr[pos++] = block[i];
			}
		}
		
		wordlist.clear();
		loadAudiobank(bnkstr);
		
		return true;
	}
	
	private boolean transferAudioseq() {
		//TODO
		return false;
	}
	
	/*----- General Control -----*/
	
	public boolean bootEngine() {
		//TODO
		return false;
	}
	
	public boolean shutdownEngine() {
		//TODO
		return false;
	}
	
	public boolean playBGM(int seq_idx) {
		//TODO
		return false;
	}
	
	public boolean stopBGM() {
		//TODO
		return false;
	}
	
	public boolean pressControllerButton(int button) {
		//TODO
		return false;
	}
	
	public boolean setAnalogStickValue(float x, float y) {
		//TODO
		return false;
	}
	
	/*----- Direct SeqPlayer Control -----*/
	
	//For messing with IO variables, playing SFX etc.
	//Muting channels, maybe sending overriding seq commands?
	//TODO Need to study how the SFX seq programs work.
	
	public boolean setIOValue_seqPlayer(int seqplayer_idx, int slot, byte value) {
		//TODO
		return false;
	}
	
	public boolean setIOValue_seqChannel(int seqplayer_idx, int channel, int slot, byte value) {
		//TODO
		return false;
	}
	
	/*----- Audio Capture -----*/
	
	//Capture types: AI out, SeqPlayer out, channel out
	//Should GUI callbacks be implemented this way too?
	
	/*----- Monitoring Callbacks -----*/
	
	
}
