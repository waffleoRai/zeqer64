package waffleoRai_zeqer64.GUI;


import javax.swing.JFrame;
import javax.swing.JPanel;

import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_zeqer64.ZeqerSeq;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;

public class ZeqerGUIUtils {
	
	public static final String STRKEY_FILPNL_FLAGS = "PNLFLTER_TITLE_FLAGS";
	public static final String STRKEY_FILPNL_TAGS = "PNLFLTER_TITLE_TAGS";
	public static final String STRKEY_FILPNL_ADDOP = "PNLFLTER_TITLE_ADDOP";
	
	public static final String STRKEY_PAN_L = "PANSTR_LEFT";
	public static final String STRKEY_PAN_R = "PANSTR_RIGHT";
	public static final String STRKEY_PAN_C = "PANSTR_CENTER";
	
	public static enum MediumType{
		RAM(Z64Sound.MEDIUM_RAM),
		UNKNOWN(Z64Sound.MEDIUM_UNK),
		CART(Z64Sound.MEDIUM_CART),
		DD(Z64Sound.MEDIUM_DISK_DRIVE);
		
		private int value;
		
		private MediumType(int val){value = val;}
		public int getValue(){return value;}
		
		public String toString(){
			return ZeqerGUIUtils.getMediumString(value);
		}
	}
	
	public static enum CacheType{
		PERMANENT(Z64Sound.CACHE_PERMANENT),
		PERSISTENT(Z64Sound.CACHE_PERSISTENT),
		TEMPORARY(Z64Sound.CACHE_TEMPORARY),
		ANY(Z64Sound.CACHE_ANY),
		ANY_NOSYNC(Z64Sound.CACHE_ANYNOSYNCLOAD);
		
		private int value;
		
		private CacheType(int val){value = val;}
		public int getValue(){return value;}
		
		public String toString(){
			return ZeqerGUIUtils.getCacheString(value);
		}
	}
	
	public static enum SeqType{
		NONE(ZeqerSeq.SEQTYPE_UNKNOWN),
		BGM(ZeqerSeq.SEQTYPE_BGM),
		BGM_PROG(ZeqerSeq.SEQTYPE_BGM_PROG),
		BGM_PIECE(ZeqerSeq.SEQTYPE_BGM_PIECE),
		JINGLE(ZeqerSeq.SEQTYPE_JINGLE),
		OCARINA(ZeqerSeq.SEQTYPE_OCARINA),
		CUTSCENE(ZeqerSeq.SEQTYPE_CUTSCENE),
		AMBIENT(ZeqerSeq.SEQTYPE_AMBIENT),
		SFX(ZeqerSeq.SEQTYPE_SFX),
		INTRMUS(ZeqerSeq.SEQTYPE_INTRMUS);
		
		private int value;
		
		private SeqType(int val){value = val;}
		public int getValue(){return value;}
		
		public String toString(){
			return ZeqerGUIUtils.getSeqTypeString(value);
		}
	}
	
	public static String pan2String(byte raw, ZeqerCoreInterface core){
		if(raw == 0x40){
			if(core == null) return "Center";
			return core.getString(STRKEY_PAN_C);
		}
		
		double perc = 0.0;
		String lrstr = "";
		if(raw < 0x40){
			lrstr = "Left";
			if(core != null) lrstr = core.getString(STRKEY_PAN_L);
			int amt = 0x40 - (int)raw;
			perc = (double)amt / 64.0;
			perc *= 100.0;
		}
		else{
			lrstr = "Right";
			if(core != null) lrstr = core.getString(STRKEY_PAN_R);
			int amt = (int)raw - 0x40;
			perc = (double)amt / 63.0;
			perc *= 100.0;
		}
		
		return String.format("%.1f%% %s", perc, lrstr);
	}
	
	public static String getMediumString(int eVal){
		switch(eVal){
		case Z64Sound.MEDIUM_CART: return "Cartridge";
		case Z64Sound.MEDIUM_DISK_DRIVE: return "64DD";
		case Z64Sound.MEDIUM_RAM: return "RAM";
		}
		return "Unknown";
	}
	
	public static String getCacheString(int eVal){
		switch(eVal){
		case Z64Sound.CACHE_ANY: return "Any";
		case Z64Sound.CACHE_ANYNOSYNCLOAD: return "Any (No Sync Load)";
		case Z64Sound.CACHE_PERMANENT: return "Permanent";
		case Z64Sound.CACHE_PERSISTENT: return "Persistent";
		case Z64Sound.CACHE_TEMPORARY: return "Temporary";
		}
		return "Unknown";
	}
	
	public static String getSeqTypeString(int eVal){
		switch(eVal){
		case ZeqerSeq.SEQTYPE_AMBIENT: return "Ambient SFX Program";
		case ZeqerSeq.SEQTYPE_BGM: return "BGM (Standard)";
		case ZeqerSeq.SEQTYPE_BGM_PIECE: return "BGM Piece";
		case ZeqerSeq.SEQTYPE_BGM_PROG: return "BGM Program";
		case ZeqerSeq.SEQTYPE_CUTSCENE: return "Cutscene SFX Program";
		case ZeqerSeq.SEQTYPE_INTRMUS: return "In-Game Music";
		case ZeqerSeq.SEQTYPE_JINGLE: return "Jingle/Fanfare";
		case ZeqerSeq.SEQTYPE_OCARINA: return "Ocarina Song";
		case ZeqerSeq.SEQTYPE_SFX: return "General SFX Program";
		case ZeqerSeq.SEQTYPE_UNKNOWN: return "(Unmarked)";
		}
		return null;
	}
	
	public static JFrame genPanelStandalone(JFrame parent, JPanel pnl){
		JFrame newFrame = new JFrame();
		newFrame.add(pnl);
		newFrame.pack();
		
		return newFrame;
	}

}
