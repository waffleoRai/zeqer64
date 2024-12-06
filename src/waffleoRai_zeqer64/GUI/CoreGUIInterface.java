package waffleoRai_zeqer64.GUI;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import waffleoRai_SeqSound.n64al.NUSALSeq;
import waffleoRai_Sound.nintendo.Z64Wave;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_soundbank.nintendo.z64.Z64Bank;
import waffleoRai_soundbank.nintendo.z64.Z64Envelope;
import waffleoRai_zeqer64.ErrorCode;
import waffleoRai_zeqer64.ZeqerBank;
import waffleoRai_zeqer64.ZeqerConstants;
import waffleoRai_zeqer64.ZeqerCore;
import waffleoRai_zeqer64.ZeqerPreset;
import waffleoRai_zeqer64.ZeqerRom;
import waffleoRai_zeqer64.ZeqerSeq;
import waffleoRai_zeqer64.ZeqerStringManager;
import waffleoRai_zeqer64.ZeqerWave;
import waffleoRai_zeqer64.GUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_zeqer64.ZeqerInstaller.ZeqerInstallListener;
import waffleoRai_zeqer64.filefmt.AbldFile;
import waffleoRai_zeqer64.filefmt.NusRomInfo;
import waffleoRai_zeqer64.filefmt.bank.BankTableEntry;
import waffleoRai_zeqer64.filefmt.bank.ZeqerBankTable;
import waffleoRai_zeqer64.filefmt.seq.SeqTableEntry;
import waffleoRai_zeqer64.filefmt.wave.ZeqerWaveIO;
import waffleoRai_zeqer64.filefmt.wave.ZeqerWaveTable;
import waffleoRai_zeqer64.filefmt.wave.ZeqerWaveIO.SampleImportOptions;
import waffleoRai_zeqer64.filefmt.wave.ZeqerWaveIO.SampleImportResult;
import waffleoRai_zeqer64.filefmt.wave.WaveTableEntry;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;
import waffleoRai_zeqer64.listeners.RomImportProgDiaListener;
import waffleoRai_zeqer64.presets.ZeqerDrumPreset;
import waffleoRai_zeqer64.presets.ZeqerInstPreset;
import waffleoRai_zeqer64.presets.ZeqerPercPreset;

public class CoreGUIInterface implements ZeqerCoreInterface{
	
	private static final char SEP = File.separatorChar;
	
	private static final String STRKEY_ADDROMRES_IOERR = "ADDROMRES_IOERROR";
	private static final String STRKEY_ADDROMRES_BADROM = "ADDROMRES_BADROM";
	private static final String STRKEY_ADDROMRES_SAMEIMG = "ADDROMRES_SAMEIMG";
	private static final String STRKEY_ADDROMRES_NULLROM = "ADDROMRES_NULLROM";
	private static final String STRKEY_ADDROMRES_NOINFO = "ADDROMRES_NOINFO";
	private static final String STRKEY_ADDROMRES_EXTFAIL = "ADDROMRES_EXTFAIL";

	/*----- Instance Variables -----*/
	
	private String root_dir;
	
	private ZeqerStringManager strManager;
	private ZeqerCore core;
	
	/*----- Boot/Shutdown -----*/
	
	public CoreGUIInterface() throws IOException{
		//This one checks the ini file and loads from there
		core = ZeqerCore.bootNewCore(false);
		root_dir = core.getProgramDirectory();
	}
	
	public CoreGUIInterface(String programDir){
		root_dir = programDir;
	}
	
	public boolean openStringManagerInstallMode(String lanCode) throws IOException{
		strManager = new ZeqerStringManager();
		return strManager.loadLanguage(lanCode);
	}
	
	public boolean openStringManager(String lanCode) throws IOException{
		if(lanCode == null){
			if(core != null) lanCode = core.getIniValue(ZeqerCore.IKEY_PREFLAN);
			if(lanCode == null) lanCode = "eng";
		}
		strManager = new ZeqerStringManager(root_dir + SEP + ZeqerCore.DIRNAME_STRING);
		return strManager.loadLanguage(lanCode);
	}
	
	public boolean bootSoundCore() throws IOException{
		if(core != null) return false;
		core = ZeqerCore.instantiateActiveCore(root_dir, false);
		return (core != null);
	}
	
	public boolean rebootFromInstalledPath() throws IOException{
		String lanCode = null;
		if(strManager != null){
			lanCode = strManager.getLoadedLanguageCode();
			strManager.clear();
		}
		if(core != null){
			core.shutdownCore();
			core = null;
		}
		strManager = null;
		
		core = ZeqerCore.bootNewCore(false);
		if(core == null) return false;
		if(lanCode == null){
			lanCode = core.getIniValue(ZeqerCore.IKEY_PREFLAN);
			if(lanCode == null) lanCode = "eng";
		}
		root_dir = core.getProgramDirectory();
		
		return openStringManager(lanCode);
	}
	
	public boolean rebootFrom(String programDir) throws IOException{
		String lanCode = "eng";
		if(strManager != null){
			lanCode = strManager.getLoadedLanguageCode();
			strManager.clear();
		}
		if(core != null){
			core.shutdownCore();
			core = null;
		}
		strManager = null;
		
		root_dir = programDir;
		if(!openStringManager(lanCode)) return false;
		
		return bootSoundCore();
	}
	
	public boolean shutdown(){
		if(strManager != null) strManager.clear();
		strManager = null;
		
		if(core != null){
			try {
				core.shutdownCore();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		core = null;
		return true;
	}
	
	/*----- Updates -----*/
	public boolean updateAvailable(){
		if(core == null) return false;
		return core.needsUpdate();
	}
	
	public boolean performUpdate(ZeqerInstallListener l){
		if(core == null) return false;
		try{return core.updateToThisVersion(l);}
		catch(IOException ex){
			ex.printStackTrace();
			return false;
		}
	}
	
	/*----- Getters -----*/
	
	public ZeqerCore getCore(){return core;}
	public ZeqerStringManager getStringManager(){return strManager;}
	
	public boolean isOkayForMainProgram(){
		if(strManager == null) return false;
		if(root_dir == null) return false;
		if(core == null) return false;
		return (core.getProgramDirectory() != null);
	}
	
	/*----- Setters -----*/
	
	public ZeqerCore createCoreForPath(String path){
		root_dir = path;
		core = ZeqerCore.instantiateActiveCore(root_dir, false);
		return core;
	}
	
	/*----- Settings Keys -----*/
	
	public String getSetting(String key) {
		if(core == null) return null;
		return core.getIniValue(key);
	}

	public String setSetting(String key, String value) {
		if(core == null) return null;
		core.setIniValue(key, value);
		return core.getIniValue(key);
	}
	
	/*----- String Management -----*/
	
	public String getLoadedLanguageCode(){
		if(strManager == null) return null;
		return strManager.getLoadedLanguageCode();
	}
	
	public boolean loadLanguage(String lanCode){
		if(strManager == null) return false;
		try{
			boolean okay = strManager.loadLanguage(lanCode);
			if(okay && core != null){
				core.setIniValue(ZeqerCore.IKEY_PREFLAN, lanCode);
			}
			return okay;
		}
		catch(IOException ex){
			ex.printStackTrace();
			return false;
		}
	}
	
	public String getString(String key){
		if(strManager == null) return null;
		return strManager.getString(key);
	}
	
	/*----- ROM Record Management -----*/

	public Collection<ZeqerRom> getImportedRoms() {
		if(core == null) return null;
		return core.getAllUserRoms();
	}

	public boolean removeImportedRoms(Collection<ZeqerRom> romlist) {
		if(core == null) return false;
		
		boolean good = true;
		for(ZeqerRom rom : romlist){
			NusRomInfo info = rom.getRomInfo();
			if(info == null){
				good = false;
				continue;
			}
			good = good && core.removeUserRom(info.getMD5String());
		}
		
		return good;
	}

	public String importRom(String path, IndefProgressDialog listener) {
		return importRom(path, null, listener);
	}

	public String importRom(String path, String xmlpath, IndefProgressDialog listener) {
		RomImportProgDiaListener dlistener = null;
		if(listener != null){
			dlistener = new RomImportProgDiaListener(listener, this);
		}
		
		try {
			int res = core.addUserRom(path, xmlpath, true, dlistener);
			switch(res){
			case ZeqerCore.ADDROM_ERROR_NONE:
				return null;
			case ZeqerCore.ADDROM_ERROR_BADROM:
				return getString(STRKEY_ADDROMRES_BADROM);
			case ZeqerCore.ADDROM_ERROR_IMAGE_ALREADY_IMPORTED:
				return getString(STRKEY_ADDROMRES_SAMEIMG);
			case ZeqerCore.ADDROM_ERROR_NULL_ROM:
				return getString(STRKEY_ADDROMRES_NULLROM);
			case ZeqerCore.ADDROM_ERROR_INFO_NOT_FOUND:
				return getString(STRKEY_ADDROMRES_NOINFO);
			case ZeqerCore.ADDROM_ERROR_EXTRACT_FAIL:
				return getString(STRKEY_ADDROMRES_EXTFAIL);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return getString(STRKEY_ADDROMRES_IOERR);
		}
		
		return null;
	}
	
	public ZeqerRom getRom(String romid){
		if(core == null) return null;
		return core.getUserRom(romid); //The map uses BOTH md5 string and zeqerid as keys.
	}
	
	/*----- Sample Management -----*/

	@Override
	public boolean playSample(WaveTableEntry wave) {
		// TODO Auto-generated method stub
		return false;
	}

	public WaveTableEntry importSample(String path, SampleImportOptions options, ErrorCode error) {
		if(core == null) return null;
		if(path == null) return null;
		if(options == null) options = new SampleImportOptions();
		
		if(error != null) error.value = ZeqerWaveIO.ERROR_CODE_NONE;
		
		//Try to detect type from extension
		SampleImportResult res = null;
		if(path.endsWith(".wav") || path.endsWith(".wave")){
			res = ZeqerWaveIO.importWAV(path, options);
		}
		else if(path.endsWith(".aif") || path.endsWith(".aiff")){
			res = ZeqerWaveIO.importAIFF(path, options);
			if(res.error == ZeqerWaveIO.ERROR_CODE_AIFF_COMPRESSION_UNKNOWN){
				//Try AIFC
				res = ZeqerWaveIO.importAIFC(path, options);
			}
		}
		else if(path.endsWith(".aifc")){
			res = ZeqerWaveIO.importAIFC(path, options);
		}
		else{
			if(error != null) error.value = ZeqerWaveIO.ERROR_CODE_INPUT_FMT_UNKNOWN;
			return null;
		}
		
		if(res == null){
			if(error != null) error.value = ZeqerWaveIO.ERROR_CODE_UNKNOWN;
			return null;
		}
		
		if(error != null) error.value = res.error;
		if(res.error != ZeqerWaveIO.ERROR_CODE_NONE) return null;
		
		//Add to core.
		WaveTableEntry e = core.addUserWaveSample(res.info, res.data);
		if(e == null){
			if(error != null) error.value = ZeqerWaveIO.ERROR_CODE_TABLE_IMPORT_FAILED;
			return null;
		}
		
		//Additional flags for the entry.
		if(options.flagActor) e.setFlags(ZeqerWaveTable.FLAG_ISACTOR);
		if(options.flagEnv) e.setFlags(ZeqerWaveTable.FLAG_ISENV);
		if(options.flagMusic) e.setFlags(ZeqerWaveTable.FLAG_ISMUSIC);
		if(options.flagSFX) e.setFlags(ZeqerWaveTable.FLAG_ISSFX);
		if(options.flagVox) e.setFlags(ZeqerWaveTable.FLAG_ISVOX);
		e.setFlags(ZeqerWaveTable.FLAG_ISCUSTOM);
		e.addTag("Custom");
		
		return e;
	}

	public WaveTableEntry addUserWaveSample(Z64WaveInfo info, FileBuffer data, ErrorCode error){
		if(core == null) return null;
		
		WaveTableEntry e = core.addUserWaveSample(info, data);
		if(e == null){
			if(error != null) error.value = ZeqerWaveIO.ERROR_CODE_TABLE_IMPORT_FAILED;
			return null;
		}
		
		e.setFlags(ZeqerWaveTable.FLAG_ISCUSTOM);
		e.addTag("Custom");
		
		return e;
	}
	
	public boolean exportSample(WaveTableEntry wave, String pathstem) {
		if(wave == null || pathstem == null) return false;
		if(core == null) return false;
		
		//Fetch wave data.
		Z64Wave data = core.loadWave(wave.getUID());
		if(data == null) return false;
		
		switch(core.getSampleExportFormat()){
		case ZeqerConstants.AUDIOFILE_FMT_WAV:
			return ZeqerWaveIO.exportWAV(pathstem + ".wav", data);
		case ZeqerConstants.AUDIOFILE_FMT_AIFF:
			return ZeqerWaveIO.exportAIFF(pathstem + ".aiff", data);
		case ZeqerConstants.AUDIOFILE_FMT_AIFC:
			return ZeqerWaveIO.exportAIFC(pathstem + ".aifc", data);
		}
		
		return false;
	}
	
	public Z64WaveInfo getDefaultPercussionSample(){
		//TODO
		return null;
	}
	
	public List<WaveTableEntry> getAllRegisteredSamples(){
		if(core == null) return null;
		return core.getAllValidWaveTableEntries();
	}
	
	public WaveTableEntry getWaveTableEntry(int uid){
		if(core == null) return null;
		return core.getWaveTableEntry(uid);
	}
	
	public Z64WaveInfo getSampleInfo(int uid){
		if(core == null) return null;
		return core.getWaveInfo(uid);
	}
	
	public Z64WaveInfo getSampleInfoByName(String name){
		if(core == null) return null;
		return core.getWaveByName(name);
	}
	
	public ZeqerWave getSample(int uid){
		if(core == null) return null;
		return core.getWave(uid);
	}
	
	public ZeqerWave getSampleByName(String name){
		if(core == null) return null;
		Z64WaveInfo winfo = core.getWaveByName(name);
		if(winfo == null) return null;
		return core.getWave(winfo.getUID());
	}
	
	public int getSampleExportFormat(){
		if(core == null) return ZeqerConstants.AUDIOFILE_FMT_WAV;
		return core.getSampleExportFormat();
	}
	
	public String getSampleExportFormatExtention(){
		if(core == null) return null;
		switch(core.getSampleExportFormat()){
		case ZeqerConstants.AUDIOFILE_FMT_WAV: return "wav";
		case ZeqerConstants.AUDIOFILE_FMT_AIFF: return "aiff";
		case ZeqerConstants.AUDIOFILE_FMT_AIFC: return "aifc";
		}
		return null;
	}
	
	public String getSampleExportFormatDescription(){
		if(core == null) return null;
		switch(core.getSampleExportFormat()){
		case ZeqerConstants.AUDIOFILE_FMT_WAV: return "RIFF Wave Audio File";
		case ZeqerConstants.AUDIOFILE_FMT_AIFF: return "Audio Interchange File Format";
		case ZeqerConstants.AUDIOFILE_FMT_AIFC: return "Compressed Audio Interchange File";
		}
		return null;
	}
	
	/*----- Envelope Management -----*/
	
	public Map<String, Z64Envelope> getAllEnvelopePresets(){
		if(core == null) return null;
		return core.getSavedEnvPresets();
	}
	
	public boolean addEnvelopePreset(String name, Z64Envelope env){
		if(core == null) return false;
		return core.addEnvPreset(name, env);
	}
	
	/*----- Preset Management -----*/
	
	public ZeqerPreset getPreset(int uid){
		if(core == null) return null;
		return core.getPreset(uid);
	}
	
	public ZeqerInstPreset getInstrumentPreset(int uid){
		if(core == null) return null;
		return core.getInstrumentPreset(uid);
	}
	
	public ZeqerPercPreset getPercussionPreset(int uid){
		if(core == null) return null;
		return core.getPercussionPreset(uid);
	}
	
	public ZeqerDrumPreset getDrumPreset(int uid){
		if(core == null) return null;
		return core.getDrumPreset(uid);
	}
	
	public ZeqerPreset getPresetByName(String name){
		if(core == null) return null;
		return core.getPresetByName(name);
	}
	
	public ZeqerInstPreset getInstrumentPresetByName(String name){
		if(core == null) return null;
		return core.getInstrumentPresetByName(name);
	}
	
	public ZeqerPercPreset getPercussionPresetByName(String name){
		if(core == null) return null;
		return core.getPercussionPresetByName(name);
	}
	
	public ZeqerDrumPreset getDrumPresetByName(String name){
		if(core == null) return null;
		return core.getDrumPresetByName(name);
	}
	
	public boolean isEditablePreset(int uid){
		if(core == null) return false;
		return !core.isSystemPreset(uid);
	}
	
	public List<ZeqerPreset> getAllInstPresets(){
		if(core == null) return null;
		return core.getAllValidPresets();
	}
	
	public boolean addUserPreset(ZeqerPreset preset){
		if(core == null) return false;
		return core.addUserPreset(preset);
	}
	
	public boolean deletePreset(int uid){
		if(core == null) return false;
		return core.removeUserPreset(uid);
	}

	/*----- Bank Management -----*/
	
	public List<ZeqerBank> getAllValidBanks(){
		if(core == null) return new LinkedList<ZeqerBank>();
		return core.getAllValidBanks();
	}
	
	public boolean isEditableBank(int uid){
		if(core == null) return true;
		return !core.isSystemBank(uid);
	}
	
	public BankTableEntry getBankInfo(int uid){
		if(core == null) return null;
		return core.getBankInfo(uid);
	}
	
	public ZeqerBank getBank(int uid){
		if(core == null) return null;
		return core.loadZeqerBank(uid);
	}
	
	public ZeqerBank addUserBank(Z64Bank bankdata){
		if(core == null) return null;
		ZeqerBank bnk = core.addUserBank(bankdata);
		if(bnk != null){
			BankTableEntry meta = bnk.getTableEntry();
			if(meta != null) meta.setFlags(ZeqerBankTable.FLAG_CUSTOM);
		}
		return bnk;
	}
	
	public boolean deleteUserBank(int uid) {
		if(core == null) return false;
		return core.removeUserBank(uid);
	}
	
	/*----- Seq Management -----*/
	
	public SeqTableEntry getSeqInfo(int uid){
		if(core == null) return null;
		return core.getSeqInfo(uid);
	}
	
	public List<ZeqerSeq> getAllValidSeqs(){
		if(core == null) return new LinkedList<ZeqerSeq>();
		return core.getAllValidSeqs();
	}
	
	public ZeqerSeq addUserSeq(NUSALSeq seq) {
		if(core == null) return null;
		return core.addUserSeq(seq);
	}
	
	public boolean deleteSeq(int uid) {
		if(core == null) return false;
		return core.deleteSeq(uid);
	}
	
	/*----- Abld Management -----*/
	
	public AbldFile getSysAbldFile(String romId){
		if(core == null) return null;
		try {
			return core.loadSysBuild(romId);
		}
		catch (UnsupportedFileTypeException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public List<AbldFile> getAllAblds(){
		if(core == null) return new LinkedList<AbldFile>();
		try {
			return core.loadAllAblds();
		} catch (UnsupportedFileTypeException e) {
			e.printStackTrace();
			return new LinkedList<AbldFile>();
		} catch (IOException e) {
			e.printStackTrace();
			return new LinkedList<AbldFile>();
		}
	}
	
}
