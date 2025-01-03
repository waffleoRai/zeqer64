package waffleoRai_zeqer64.iface;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import waffleoRai_SeqSound.n64al.NUSALSeq;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_soundbank.nintendo.z64.Z64Bank;
import waffleoRai_soundbank.nintendo.z64.Z64Envelope;
import waffleoRai_zeqer64.ZeqerPreset;
import waffleoRai_zeqer64.ZeqerRom;
import waffleoRai_zeqer64.ZeqerSeq;
import waffleoRai_zeqer64.ZeqerWave;
import waffleoRai_zeqer64.GUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_zeqer64.ErrorCode;
import waffleoRai_zeqer64.ZeqerBank;
import waffleoRai_zeqer64.ZeqerInstaller.ZeqerInstallListener;
import waffleoRai_zeqer64.filefmt.AbldFile;
import waffleoRai_zeqer64.filefmt.bank.BankTableEntry;
import waffleoRai_zeqer64.filefmt.seq.SeqTableEntry;
import waffleoRai_zeqer64.filefmt.wave.ZeqerWaveIO.SampleImportOptions;
import waffleoRai_zeqer64.filefmt.wave.WaveTableEntry;
import waffleoRai_zeqer64.presets.ZeqerDrumPreset;
import waffleoRai_zeqer64.presets.ZeqerInstPreset;
import waffleoRai_zeqer64.presets.ZeqerPercPreset;

public interface ZeqerCoreInterface {
	
	/*----- Updates -----*/
	public boolean updateAvailable();
	public boolean performUpdate(ZeqerInstallListener l);
	
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
	public ZeqerRom getRom(String romid);
	
	/*----- Sample Management -----*/
	public boolean playSample(WaveTableEntry wave);
	public boolean exportSample(WaveTableEntry wave, String pathstem);
	public Z64WaveInfo getDefaultPercussionSample();
	public List<WaveTableEntry> getAllRegisteredSamples();
	
	public WaveTableEntry getWaveTableEntry(int uid);
	public Z64WaveInfo getSampleInfo(int uid);
	public Z64WaveInfo getSampleInfoByName(String name);
	public ZeqerWave getSample(int uid);
	public ZeqerWave getSampleByName(String name);
	public boolean isEditableSample(int uid);
	
	public int getSampleExportFormat();
	public String getSampleExportFormatExtention();
	public String getSampleExportFormatDescription();
	
	public WaveTableEntry addUserWaveSample(Z64WaveInfo info, FileBuffer data, ErrorCode error);
	public WaveTableEntry importSample(String path, SampleImportOptions options, ErrorCode error);
	public boolean deleteSample(int uid);
	
	/*----- Envelope Management -----*/
	
	public Map<String, Z64Envelope> getAllEnvelopePresets();
	public boolean addEnvelopePreset(String name, Z64Envelope env);
	
	/*----- Preset Management -----*/
	
	public ZeqerPreset getPreset(int uid);
	public ZeqerInstPreset getInstrumentPreset(int uid);
	public ZeqerPercPreset getPercussionPreset(int uid);
	public ZeqerDrumPreset getDrumPreset(int uid);
	public ZeqerPreset getPresetByName(String name);
	public ZeqerInstPreset getInstrumentPresetByName(String name);
	public ZeqerPercPreset getPercussionPresetByName(String name);
	public ZeqerDrumPreset getDrumPresetByName(String name);
	
	public List<ZeqerPreset> getAllInstPresets();
	public boolean isEditablePreset(int uid);
	
	public boolean addUserPreset(ZeqerPreset preset);
	public boolean deletePreset(int uid);
	
	/*----- Bank Management -----*/
	
	public List<ZeqerBank> getAllValidBanks();
	public boolean isEditableBank(int uid);
	public BankTableEntry getBankInfo(int uid);
	public ZeqerBank getBank(int uid);
	
	public ZeqerBank newUserBank(int sfx_alloc);
	public ZeqerBank addUserBank(Z64Bank bankdata);
	public boolean deleteUserBank(int uid);
	
	/*----- Seq Management -----*/
	
	public SeqTableEntry getSeqInfo(int uid);
	public List<ZeqerSeq> getAllValidSeqs();
	
	public ZeqerSeq addUserSeq(NUSALSeq seq);
	public boolean deleteSeq(int uid);
	
	/*----- Abld Management -----*/
	
	public AbldFile getSysAbldFile(String romId);
	public List<AbldFile> getAllAblds();
	
}
