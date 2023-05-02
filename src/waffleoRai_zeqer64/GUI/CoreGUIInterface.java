package waffleoRai_zeqer64.GUI;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_soundbank.nintendo.z64.Z64Envelope;
import waffleoRai_zeqer64.ZeqerCore;
import waffleoRai_zeqer64.ZeqerCoreInterface;
import waffleoRai_zeqer64.ZeqerPreset;
import waffleoRai_zeqer64.ZeqerRom;
import waffleoRai_zeqer64.ZeqerStringManager;
import waffleoRai_zeqer64.GUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_zeqer64.filefmt.NusRomInfo;
import waffleoRai_zeqer64.filefmt.ZeqerWaveTable.WaveTableEntry;
import waffleoRai_zeqer64.listeners.RomImportProgDiaListener;

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
