package waffleoRai_zeqer64.engine;

import java.util.ArrayList;

import waffleoRai_soundbank.nintendo.z64.Z64Bank;

public class ZeqerPlaybackEngine {

	/*----- Constants -----*/
	
	//TODO: Update these to ensure they match actual engine enums
	public static final int TV_TYPE__NTSC = 0;
	public static final int TV_TYPE__PAL  = 1;
	public static final int TV_TYPE__MPAL = 2;
	
	/*----- Font Transfer Commands -----*/
	
	/*
	 * For best arch/compiler compatibility (due to stupid padding, mostly)
	 * Soundfonts/banks are transferred as streams of ints instead of in their native
	 * serialized format...
	 */
	
	/*
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
	 * 	Book ID (-1 if none)
	 * 	Loop ID (-1 if none)
	 * STARTADPCMBOOK
	 * 	ID
	 * 	Order
	 * 	PredCount
	 * 	Val|Val ... (A value in each halfword. Higher is first)
	 * STARTWAVELOOP
	 * 	ID
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
	 * STARTDRUM
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
	
	/*----- Instance Variables -----*/
	
	//-> audiobank
	private ArrayList<EngineBankInfo> banks;
	
	//-> audioseq
	private ArrayList<EngineSeqInfo> seqs;
	
	//-> audiotable
	private ArrayList<EngineWaveArcInfo> warcs;
	
	//-> settings
	private int tv_type = TV_TYPE__NTSC;
	private boolean oot_mode = false;
	
	//-> state
	private boolean is_running = false;
	
	/*----- Native Method Declarations -----*/
	
	private native int loadAudiobank(int[][] bankStream);
	private native int loadAudioseq(int[] seqtbl, byte[] seqfonttbl, byte[] data);
	private native int loadAudiotable(int[] tbltbl, byte[] data);
	
	private native int bootEngine_native(int tvmode, boolean ootmode);
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
	
	private boolean transferAudiotable() {
		//TODO
		//Includes the code table and the file
		return false;
	}
	
	private boolean transferAudiobank() {
		//TODO
		return false;
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
