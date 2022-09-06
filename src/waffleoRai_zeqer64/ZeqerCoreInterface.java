package waffleoRai_zeqer64;

import java.util.Collection;

import waffleoRai_zeqer64.GUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_zeqer64.filefmt.ZeqerWaveTable.WaveTableEntry;

public interface ZeqerCoreInterface {
	
	/*----- Settings Keys -----*/
	public String getSetting(String key);
	public String setSetting(String key, String value);
	
	/*----- ROM Record Management -----*/
	public Collection<ZeqerRom> getImportedRoms();
	public boolean removeImportedRoms(Collection<ZeqerRom> romlist);
	public String importRom(String path, IndefProgressDialog listener); //Also does detection. Or attempts. Returns error message
	public String importRom(String path, String xmlpath, IndefProgressDialog listener); //When detection fails. Returns error message.
	
	/*----- Sample Management -----*/
	public boolean playSample(WaveTableEntry wave);
	public WaveTableEntry importSample(String path);
	public boolean exportSample(WaveTableEntry wave, String path);
	
}
