package waffleoRai_zeqer64.listeners;

import waffleoRai_zeqer64.ZeqerRom;
import waffleoRai_zeqer64.extract.RomExtractionSummary;

public interface RomImportListener {
	
	public void onStartMD5Calculation();
	public void onStartInfoImport();
	public void onRomRecordAdded(ZeqerRom rom);
	public void onSoundExtractStart(RomExtractionSummary errinfo);
	public void onWaveExtractStart(RomExtractionSummary errinfo);
	public void onFontExtractStart(RomExtractionSummary errinfo);
	public void onSeqExtractStart(RomExtractionSummary errinfo);
	public void onSeqBankMapStart(RomExtractionSummary errinfo);
	public void onAbldGenStart(RomExtractionSummary errinfo);
	public void onExtractionComplete(RomExtractionSummary errinfo);

}
