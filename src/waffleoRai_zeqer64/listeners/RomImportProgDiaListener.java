package waffleoRai_zeqer64.listeners;

import waffleoRai_zeqer64.ZeqerRom;
import waffleoRai_zeqer64.GUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_zeqer64.extract.RomExtractionSummary;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;

public class RomImportProgDiaListener implements RomImportListener{
	
	private static final String STRKEY_TITLE = "ADDROMPROG_T";
	private static final String STRKEY_DEFO_A = "ADDROMPROG_DEFO_A";
	private static final String STRKEY_DEFO_B = "ADDROMPROG_DEFO_B";
	private static final String STRKEY_SUM_A = "ADDROMPROG_MDCALC_A";
	private static final String STRKEY_SUM_B = "ADDROMPROG_MDCALC_B";
	private static final String STRKEY_READINFO_A = "ADDROMPROG_READINFO_A";
	private static final String STRKEY_READINFO_B = "ADDROMPROG_READINFO_B";
	private static final String STRKEY_RECADD_A = "ADDROMPROG_RECADDED_A";
	private static final String STRKEY_RECADD_B = "ADDROMPROG_RECADDED_B";
	private static final String STRKEY_SNDEXT_A = "ADDROMPROG_STARTSOUNDEXT_A";
	private static final String STRKEY_SNDEXT_B = "ADDROMPROG_STARTSOUNDEXT_B";
	private static final String STRKEY_WAVEXT_B = "ADDROMPROG_WAVEXT_B";
	private static final String STRKEY_BNKEXT_B = "ADDROMPROG_BNKEXT_B";
	private static final String STRKEY_SEQEXT_B = "ADDROMPROG_SEQEXT_B";
	private static final String STRKEY_SEQBNKMAP_B = "ADDROMPROG_SEQBNKMAP_B";
	private static final String STRKEY_ABLDSAVE_B = "ADDROMPROG_SAVEABLD_B";
	private static final String STRKEY_EXT_DONE_A = "ADDROMPROG_EXTDONE_A";
	private static final String STRKEY_EXT_DONE_B = "ADDROMPROG_EXTDONE_B";
	
	private IndefProgressDialog dialog;
	private ZeqerCoreInterface core;
	
	public RomImportProgDiaListener(IndefProgressDialog target, ZeqerCoreInterface core_link){
		dialog = target;
		core = core_link;
		dialog.setTitle(getString(STRKEY_TITLE));
		dialog.setPrimaryString(getString(STRKEY_DEFO_A));
		dialog.setSecondaryString(getString(STRKEY_DEFO_B));
	}
	
	private String getString(String key){
		if(core == null) return key;
		return core.getString(key);
	}
	
	public void onStartMD5Calculation(){
		dialog.setPrimaryString(getString(STRKEY_SUM_A));
		dialog.setSecondaryString(getString(STRKEY_SUM_B));
	}
	
	public void onStartInfoImport(){
		dialog.setPrimaryString(getString(STRKEY_READINFO_A));
		dialog.setSecondaryString(getString(STRKEY_READINFO_B));
	}
	
	public void onRomRecordAdded(ZeqerRom rom){
		dialog.setPrimaryString(getString(STRKEY_RECADD_A));
		dialog.setSecondaryString(getString(STRKEY_RECADD_B));
	}
	
	public void onSoundExtractStart(RomExtractionSummary errinfo){
		dialog.setPrimaryString(getString(STRKEY_SNDEXT_A));
		dialog.setSecondaryString(getString(STRKEY_SNDEXT_B));
	}
	
	public void onWaveExtractStart(RomExtractionSummary errinfo){
		dialog.setSecondaryString(getString(STRKEY_WAVEXT_B));
	}
	
	public void onFontExtractStart(RomExtractionSummary errinfo){
		dialog.setSecondaryString(getString(STRKEY_BNKEXT_B));
	}
	
	public void onSeqExtractStart(RomExtractionSummary errinfo){
		dialog.setSecondaryString(getString(STRKEY_SEQEXT_B));
	}
	
	public void onSeqBankMapStart(RomExtractionSummary errinfo){
		dialog.setSecondaryString(getString(STRKEY_SEQBNKMAP_B));
	}
	
	public void onAbldGenStart(RomExtractionSummary errinfo){
		dialog.setSecondaryString(getString(STRKEY_ABLDSAVE_B));
	}
	
	public void onExtractionComplete(RomExtractionSummary errinfo){
		dialog.setPrimaryString(getString(STRKEY_EXT_DONE_A));
		dialog.setSecondaryString(getString(STRKEY_EXT_DONE_B));
	}

}
