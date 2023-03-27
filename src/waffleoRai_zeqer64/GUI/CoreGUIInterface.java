package waffleoRai_zeqer64.GUI;

import java.util.Collection;

import waffleoRai_zeqer64.ZeqerCoreInterface;
import waffleoRai_zeqer64.ZeqerRom;
import waffleoRai_zeqer64.GUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_zeqer64.filefmt.ZeqerWaveTable.WaveTableEntry;

public class CoreGUIInterface implements ZeqerCoreInterface{

	/*----- Boot/Shutdown -----*/
	
	/*----- Settings Keys -----*/
	
	@Override
	public String getSetting(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String setSetting(String key, String value) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/*----- ROM Record Management -----*/

	@Override
	public Collection<ZeqerRom> getImportedRoms() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean removeImportedRoms(Collection<ZeqerRom> romlist) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String importRom(String path, IndefProgressDialog listener) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String importRom(String path, String xmlpath, IndefProgressDialog listener) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/*----- Sample Management -----*/

	@Override
	public boolean playSample(WaveTableEntry wave) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public WaveTableEntry importSample(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean exportSample(WaveTableEntry wave, String path) {
		// TODO Auto-generated method stub
		return false;
	}

}
