package waffleoRai_zeqer64.GUI;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_soundbank.nintendo.z64.Z64Envelope;
import waffleoRai_zeqer64.ZeqerCoreInterface;
import waffleoRai_zeqer64.ZeqerPreset;
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
	
	public Z64WaveInfo getDefaultPercussionSample(){
		//TODO
		return null;
	}
	
	/*----- Envelope Management -----*/
	
	public Map<String, Z64Envelope> getAllEnvelopePresets(){
		//TODO
		return null;
	}
	
	public boolean addEnvelopePreset(String name, Z64Envelope env){
		//TODO
		return false;
	}
	
	/*----- Preset Management -----*/
	
	public List<ZeqerPreset> getAllInstPresets(){
		//TODO
		return null;
	}
	
	public boolean addUserPreset(ZeqerPreset preset){
		//TODO
		return false;
	}
	
	public boolean deletePreset(int uid){
		//TODO
		return false;
	}

}
