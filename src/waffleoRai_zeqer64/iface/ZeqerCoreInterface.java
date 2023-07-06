package waffleoRai_zeqer64.iface;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_soundbank.nintendo.z64.Z64Envelope;
import waffleoRai_zeqer64.ZeqerPreset;
import waffleoRai_zeqer64.ZeqerRom;
import waffleoRai_zeqer64.GUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_zeqer64.filefmt.ZeqerWaveTable.WaveTableEntry;

public interface ZeqerCoreInterface {
	
	/*----- Settings Keys -----*/
	public String getSetting(String key);
	public String setSetting(String key, String value);
	
	/*----- String Management -----*/
	
	public boolean loadLanguage(String lanCode);
	public String getString(String key);
	
	/*----- ROM Record Management -----*/
	public Collection<ZeqerRom> getImportedRoms();
	public boolean removeImportedRoms(Collection<ZeqerRom> romlist);
	public String importRom(String path, IndefProgressDialog listener); //Also does detection. Or attempts. Returns error message
	public String importRom(String path, String xmlpath, IndefProgressDialog listener); //When detection fails. Returns error message.
	
	/*----- Sample Management -----*/
	public boolean playSample(WaveTableEntry wave);
	public WaveTableEntry importSample(String path);
	public boolean exportSample(WaveTableEntry wave, String path);
	public Z64WaveInfo getDefaultPercussionSample();
	public List<WaveTableEntry> getAllRegisteredSamples();
	
	/*----- Envelope Management -----*/
	
	public Map<String, Z64Envelope> getAllEnvelopePresets();
	public boolean addEnvelopePreset(String name, Z64Envelope env);
	
	/*----- Preset Management -----*/
	
	public List<ZeqerPreset> getAllInstPresets();
	public boolean addUserPreset(ZeqerPreset preset);
	public boolean deletePreset(int uid);
	
}
