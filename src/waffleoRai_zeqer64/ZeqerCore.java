package waffleoRai_zeqer64;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import waffleoRai_Sound.nintendo.Z64Wave;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.FileUtils;
import waffleoRai_soundbank.nintendo.z64.Z64Bank;
import waffleoRai_soundbank.nintendo.z64.Z64Instrument;
import waffleoRai_zeqer64.engine.ZeqerPlaybackEngine;
import waffleoRai_zeqer64.extract.RomExtractionSummary;
import waffleoRai_zeqer64.extract.WaveLocIDMap;
import waffleoRai_zeqer64.filefmt.AbldFile;
import waffleoRai_zeqer64.filefmt.NusRomInfo;
import waffleoRai_zeqer64.filefmt.RomInfoNode;
import waffleoRai_zeqer64.filefmt.VersionWaveTable;
import waffleoRai_zeqer64.filefmt.ZeqerBankTable;
import waffleoRai_zeqer64.filefmt.ZeqerBankTable.BankTableEntry;
import waffleoRai_zeqer64.filefmt.ZeqerPresetTable;
import waffleoRai_zeqer64.filefmt.ZeqerRomInfo;
import waffleoRai_zeqer64.filefmt.ZeqerSeqTable;
import waffleoRai_zeqer64.filefmt.ZeqerSeqTable.SeqTableEntry;
import waffleoRai_zeqer64.filefmt.ZeqerWaveTable;

public class ZeqerCore {
	
	//Does the same stuff that I usually put in XYZProgramFiles
	
	/*----- Misc Constants -----*/
	
	public static final String ENCODING = "UTF8";
	
	public static final String DIRNAME_ROMINFO = "rominfo";
	public static final String DIRNAME_WAVE = "wav";
	public static final String DIRNAME_SEQ = "seq";
	public static final String DIRNAME_BANK = "bnk";
	public static final String DIRNAME_ABLD = "abld";
	public static final String DIRNAME_ENGINE = "engine";
	
	public static final String DIRNAME_ZSEQ = "zseqs";
	public static final String DIRNAME_ZWAVE = "zwavs";
	public static final String DIRNAME_ZBANK = "zbnks";
	public static final String DIRNAME_ZBLD = "zbld";
	
	public static final String FN_SYSBANK = "zbanks.bin";
	public static final String FN_SYSWAVE = "zwavs.bin";
	public static final String FN_SYSPRESET = "zpresets.bin";
	public static final String FN_SYSSEQ = "zseqs.bin";
	//public static final String FN_SYSSEQ_OOT = "ootseqs.bin";
	//public static final String FN_SYSSEQ_MM = "mmseqs.bin";
	
	public static final String FN_USRBANK = "mybanks.bin";
	public static final String FN_USRWAVE = "mywaves.bin";
	public static final String FN_USRSEQ = "myseqs.bin";
	public static final String FN_USRPRESET = "mypresets.bin";
	
	public static final String IKEY_VERSION = "VERSION";
	public static final String CURRENT_VERSION = "1.0.0";
	
	private static final String RES_JARPATH = "/waffleoRai_zeqer64/res";
	
	private static final char SEP = File.separatorChar;
	
	/*----- Init -----*/
	
	private ZeqerCore(boolean adminMode){
		this.admin_write = adminMode;
	}
	
	/*----- Core Management -----*/
	
	private static ZeqerCore active_core;
	
	public static ZeqerCore getActiveCore(){return active_core;}
	
	public static ZeqerCore instantiateActiveCore(String base_dir, boolean admin_mode){
		active_core = new ZeqerCore(admin_mode);
		active_core.root_dir = base_dir;
		try{
			if(!active_core.loadCore()) active_core = null;
		}
		catch(IOException ex){
			ex.printStackTrace();
			active_core = null;
		}
		return active_core;
	}
	
	public static ZeqerCore bootNewCore(boolean admin_mode) throws IOException{
		active_core = new ZeqerCore(admin_mode);
		if(!active_core.loadCore()){
			active_core = null;
			return null;
		}
		else return active_core;
	}
	
	public static boolean shutdownActiveCore() throws IOException{
		if(active_core == null) return false;
		active_core.shutdownCore();
		active_core = null;
		return true;
	}
	
	/*----- Font -----*/
	
	public static final String IKEY_UNIFONT_NAME = "UNICODE_FONT";
	private static final String[] TRYFONTS = {"Arial Unicode MS", "MS PGothic", "MS Gothic", 
			"AppleGothic", "Takao PGothic",
			"Hiragino Maru Gothic Pro", "Hiragino Kaku Gothic Pro"};
	
	private String my_unifont;
	
	public Font getUnicodeFont(int style, int size){
		if(my_unifont != null) return new Font(my_unifont, style, size);
		
		//Try the key...
		String fontkey = getIniValue(IKEY_UNIFONT_NAME);
		
		if(fontkey != null) my_unifont = fontkey;
		else{
			//See what's on this system
			String[] flist = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
			for(String name : TRYFONTS){
				if(my_unifont != null) break;
				for(String f : flist){
					if(f.equalsIgnoreCase(name)){
						my_unifont = name;
						System.err.println("Unicode font detected: " + my_unifont);
						break;
					}
				}
			}
			setIniValue(IKEY_UNIFONT_NAME, my_unifont);
		}
		
		return new Font(my_unifont, style, size);
	}

	/*----- Initialize -----*/
	
	public boolean loadCore() throws IOException{
		String pdir = getProgramDirectory();
		if(pdir == null) return false; //Not installed or other issue
		
		//Load settings
		loadSettingsFile(pdir + File.separator + SETTINGS_FILE_NAME);
		
		//Create directories.
		makeDirectories();
		
		//Load ROM Info
		if(!loadRomInfoMap()) return false;
		
		try{loadSoundTables();}
		catch(UnsupportedFileTypeException ex){
			ex.printStackTrace();
			throw new IOException();
		}
		
		return true;
	}
	
	/*----- Root Directory/OS Detect -----*/
	
	public static final String INI_FILE_NAME = "zeqer64.ini";
	
	private String ini_path;
	private String root_dir;
	
	public String getIniPath(){
		if(ini_path != null) return ini_path;
		
		String osname = System.getProperty("os.name");
		osname = osname.toLowerCase();
		String username = System.getProperty("user.name");
		
		if(osname.startsWith("win")){
			//Assumed windows
			String dir = "C:\\Users\\" + username;
			dir += "\\AppData\\Local\\waffleorai\\zeqer64";
			dir += "\\" + INI_FILE_NAME;
			ini_path = dir;
			return dir;
		}
		else{
			//Assumed Unix like
			String dir = System.getProperty("user.home");
			char sep = File.separatorChar;
			dir += "appdata" + sep + "local" + sep + "waffleorai" + sep + "zeqer64";
			dir += sep + INI_FILE_NAME;
			ini_path = dir;
			return dir;
		}
	}
	
	public String getProgramDirectory(){
		if(root_dir != null) return root_dir;
		String initpath = getIniPath(); 
		if(initpath == null) return null;
		if(!FileBuffer.fileExists(initpath)) return null;
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(initpath));
			root_dir = br.readLine();
			br.close();
		}
		catch(IOException ex){
			ex.printStackTrace();
			return null;
		}
		
		return root_dir;
	}
	
	public void setProgramDirectory(String path){
		//To force load from outside installation setup
		root_dir = path;
	}
	
	/*----- Paths -----*/
	
	public String getBuildDirectoryPath(){
		return getProgramDirectory() + SEP + DIRNAME_ABLD;
	}
	
	public String getWaveDirectoryPath(){
		return getProgramDirectory() + SEP + DIRNAME_WAVE;
	}
	
	public String getSysWaveDirectoryPath(){
		return getWaveDirectoryPath() + SEP + DIRNAME_ZWAVE;
	}
	
	/*----- Settings -----*/
	
	public static final String SETTINGS_FILE_NAME = "settings.ini";
	
	private Map<String, String> init_values;
	
	public String getIniValue(String key){
		if(init_values == null) return null;
		return init_values.get(key);
	}
	
	public void setIniValue(String key, String value){
		if(init_values == null) return;
		init_values.put(key, value);
	}
	
	public void loadSettingsFile(String filepath) throws IOException{
		init_values = new HashMap<String, String>();
		if(!FileBuffer.fileExists(filepath)) return;
		BufferedReader br = new BufferedReader(new FileReader(filepath));
		String line = null;
		while((line = br.readLine()) != null){
			if(line.isEmpty()) continue;
			String[] fields = line.split("=");
			if(fields.length < 2) continue;
			init_values.put(fields[0], fields[1]);
		}
		br.close();
	}
	
	public void saveSettingsFile(String filepath) throws IOException{
		if(init_values == null) return;
		BufferedWriter bw = new BufferedWriter(new FileWriter(filepath));
		for(Entry<String, String> entry : init_values.entrySet()){
			bw.write(entry.getKey());
			bw.write("=");
			bw.write(entry.getValue());
			bw.write("\n");
		}
		bw.close();
	}
	
	/*----- Install/Uninstall -----*/
	
	private static boolean extractFromJAR(String jarpath, String targetpath) throws IOException{
		InputStream jarstr = ZeqerCore.class.getResourceAsStream(jarpath);
		if(jarstr == null){
			System.err.println("ZeqerCore.extractFromJAR || WARNING: Res " + jarpath + " could not be extracted!");
			return false;
		}
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(targetpath));
		int b = -1;
		while((b = jarstr.read()) != -1) bos.write(b);
		bos.close();
		return false;
	}
	
	public boolean installTo(String dirpath) throws IOException{
		if(dirpath == null) return false;
		if(dirpath.isEmpty()) return false;
		
		//Program launch info
		String inipath = getIniPath();
		if(inipath == null) return false;
		BufferedWriter bw = new BufferedWriter(new FileWriter(inipath));
		bw.write(dirpath + "\n");
		bw.close();
		
		if(!FileBuffer.directoryExists(dirpath)){
			Files.createDirectories(Paths.get(dirpath));
		}
		
		//settings
		init_values = new HashMap<String, String>();
		init_values.put(IKEY_VERSION, CURRENT_VERSION);
		saveSettingsFile(dirpath + File.separator + SETTINGS_FILE_NAME);
		
		//rominfo
		String subdir = dirpath + File.separator + DIRNAME_ROMINFO;
		if(!FileBuffer.directoryExists(subdir)){
			Files.createDirectory(Paths.get(subdir));
		}
		//Copy from JAR
		boolean good = true;
		for(String vname : RES_ROMINFO_VERS){
			String respath = RES_JARPATH + "/" + vname + ".xml";
			String targetpath = subdir + File.separator + vname + ".xml";
			good = good && extractFromJAR(respath, targetpath);
		}
		
		//seq
		subdir = dirpath + File.separator + DIRNAME_SEQ;
		String zsubdir = subdir + File.separatorChar + DIRNAME_ZSEQ;
		if(!FileBuffer.directoryExists(subdir)){
			Files.createDirectories(Paths.get(zsubdir));
		}
		/*String respath = RES_JARPATH + "/" + FN_SYSSEQ_OOT;
		String targetpath = zsubdir + File.separator + FN_SYSSEQ_OOT;
		good = good && extractFromJAR(respath, targetpath);
		respath = RES_JARPATH + "/" + FN_SYSSEQ_MM;
		targetpath = zsubdir + File.separator + FN_SYSSEQ_MM;
		good = good && extractFromJAR(respath, targetpath);*/
		String respath = RES_JARPATH + "/" + FN_SYSSEQ;
		String targetpath = zsubdir + File.separator + FN_SYSSEQ;
		good = good && extractFromJAR(respath, targetpath);
		
		//bnk
		subdir = dirpath + File.separator + DIRNAME_BANK;
		zsubdir = subdir + File.separatorChar + DIRNAME_ZBANK;
		if(!FileBuffer.directoryExists(subdir)){
			Files.createDirectories(Paths.get(zsubdir));
		}
		respath = RES_JARPATH + "/" + FN_SYSBANK;
		targetpath = zsubdir + File.separator + FN_SYSBANK;
		good = good && extractFromJAR(respath, targetpath);
		
		//wav
		subdir = dirpath + File.separator + DIRNAME_WAVE;
		zsubdir = subdir + File.separatorChar + DIRNAME_ZWAVE;
		if(!FileBuffer.directoryExists(subdir)){
			Files.createDirectory(Paths.get(subdir));
		}
		respath = RES_JARPATH + "/" + FN_SYSWAVE;
		targetpath = zsubdir + File.separator + FN_SYSWAVE;
		good = good && extractFromJAR(respath, targetpath);
		
		//abld
		subdir = dirpath + File.separator + DIRNAME_ABLD;
		if(!FileBuffer.directoryExists(subdir)){
			Files.createDirectory(Paths.get(subdir));
		}
		
		//engine
		subdir = dirpath + File.separator + DIRNAME_ENGINE;
		if(!FileBuffer.directoryExists(subdir)){
			Files.createDirectory(Paths.get(subdir));
		}
		//TODO
		
		return good;
	}
	
	public boolean uninstall() throws IOException{
		String pdir = getProgramDirectory();
		if(pdir == null) return false;
		
		FileUtils.deleteRecursive(pdir);
		Files.deleteIfExists(Paths.get(getIniPath()));
		
		return true;
	}
	
	public boolean importUserDataFrom(String dirpath) throws UnsupportedFileTypeException, IOException{
		//Copies user seqs and banks back into program dir
		//	in case of re-install or import from someone else
		String pdir = getProgramDirectory();
		if(pdir == null) return false;
		
		//Remember to MERGE metatables...
		String srcpath = dirpath + File.separatorChar + FN_USRBANK;
		String tdir = pdir + File.separatorChar + DIRNAME_BANK;
		if(FileBuffer.fileExists(srcpath)){
			ZeqerBankTable importtbl = ZeqerBankTable.readTable(FileBuffer.createBuffer(srcpath, true));
			//Merge with existing one (overwriting)
			ZeqerBankTable usrtbl = getUserBankTable();
			usrtbl.importRecordsFrom(importtbl);
		}
		//Copy bank files.
		DirectoryStream<Path> dstr = Files.newDirectoryStream(Paths.get(srcpath));
		for(Path p : dstr){
			if(!Files.isDirectory(p)){
				if(p.toAbsolutePath().toString().endsWith(".zubnk"));
				String tpath = tdir + File.separatorChar + p.getFileName().toString();
				if(FileBuffer.fileExists(tpath)){
					System.err.println("ZeqerCore.importUserDataFrom || WARNING: Import overwriting " + tpath);
					Files.delete(Paths.get(tpath));
				}
				Files.copy(p, Paths.get(tpath));
			}
		}
		dstr.close();
		
		//Repeat with seqs
		srcpath = dirpath + File.separatorChar + FN_USRSEQ;
		tdir = pdir + File.separatorChar + DIRNAME_SEQ;
		if(FileBuffer.fileExists(srcpath)){
			ZeqerSeqTable importtbl = ZeqerSeqTable.readTable(FileBuffer.createBuffer(srcpath, true));
			//Merge with existing one (overwriting)
			ZeqerSeqTable usrtbl = getUserSeqTable();
			usrtbl.importRecordsFrom(importtbl);
		}
		dstr = Files.newDirectoryStream(Paths.get(srcpath));
		for(Path p : dstr){
			if(!Files.isDirectory(p)){
				if(p.toAbsolutePath().toString().endsWith(".zuseq"));
				String tpath = tdir + File.separatorChar + p.getFileName().toString();
				if(FileBuffer.fileExists(tpath)){
					System.err.println("ZeqerCore.importUserDataFrom || WARNING: Import overwriting " + tpath);
					Files.delete(Paths.get(tpath));
				}
				Files.copy(p, Paths.get(tpath));
			}
		}
		dstr.close();
		
		//Save tables
		saveSoundTables();
		
		return true;
	}
	
	public boolean copyUserDataTo(String dirpath) throws IOException{
		//Basically copies all the user seqs and banks (and meta tables)
		//	to another place on disk in case uninstall is wanted.
		
		String pdir = getProgramDirectory();
		if(pdir == null) return false;
		
		String path = dirpath + File.separatorChar + "zeqeruserdata";
		if(!FileBuffer.directoryExists(path)) Files.createDirectories(Paths.get(path));
		
		String srcdir = pdir + File.separatorChar + DIRNAME_SEQ;
		String metapath = srcdir + File.separatorChar + FN_USRSEQ;
		if(FileBuffer.fileExists(metapath)){
			String tpath = path + File.separatorChar + FN_USRSEQ;
			if(FileBuffer.fileExists(tpath)){
				System.err.println("ZeqerCore.copyUserDataTo || WARNING: " + tpath + " already exists. Overwriting...");
				Files.delete(Paths.get(tpath));
			}
			Files.copy(Paths.get(metapath), Paths.get(tpath));
		}
		
		//Look for file names with .zuseq extension
		DirectoryStream<Path> dstr = Files.newDirectoryStream(Paths.get(srcdir));
		for(Path p : dstr){
			if(!Files.isDirectory(p)){
				String pstr = p.toAbsolutePath().toString();
				if(pstr.endsWith(".zuseq"));
				
				String tpath = path + File.separatorChar + p.getFileName().toString();
				if(FileBuffer.fileExists(tpath)){
					System.err.println("ZeqerCore.copyUserDataTo || WARNING: " + tpath + " already exists. Overwriting...");
					Files.delete(Paths.get(tpath));
				}
				
				Files.copy(p, Paths.get(tpath));
			}
		}
		dstr.close();
		
		//Repeat with banks
		srcdir = pdir + File.separatorChar + DIRNAME_BANK;
		metapath = srcdir + File.separatorChar + FN_USRBANK;
		if(FileBuffer.fileExists(metapath)){
			String tpath = path + File.separatorChar + FN_USRBANK;
			if(FileBuffer.fileExists(tpath)){
				System.err.println("ZeqerCore.copyUserDataTo || WARNING: " + tpath + " already exists. Overwriting...");
				Files.delete(Paths.get(tpath));
			}
			Files.copy(Paths.get(metapath), Paths.get(tpath));
		}
		
		//Look for file names with .zuseq extension
		dstr = Files.newDirectoryStream(Paths.get(srcdir));
		for(Path p : dstr){
			if(!Files.isDirectory(p)){
				String pstr = p.toAbsolutePath().toString();
				if(pstr.endsWith(".zubnk"));
				
				String tpath = path + File.separatorChar + p.getFileName().toString();
				if(FileBuffer.fileExists(tpath)){
					System.err.println("ZeqerCore.copyUserDataTo || WARNING: " + tpath + " already exists. Overwriting...");
					Files.delete(Paths.get(tpath));
				}
				
				Files.copy(p, Paths.get(tpath));
			}
		}
		dstr.close();
		
		return true;
	}
	
	public boolean makeDirectories() throws IOException{
		//Right now just need to make abld since the managers handle theirs
		String zbld_dir = getBuildDirectoryPath() + SEP + ZeqerCore.DIRNAME_ZBLD;
		if(!FileBuffer.directoryExists(zbld_dir)){
			Files.createDirectories(Paths.get(zbld_dir));
		}
		return true;
	}
	
	/*----- ShutDown -----*/
	
	public void shutdownCore() throws IOException{
		//Save settings and all that.
		String pdir = getProgramDirectory();
		if(pdir == null) return;
		
		saveSettingsFile(pdir + File.separator + SETTINGS_FILE_NAME);
		saveSoundTables();
		
		init_values.clear();
		romdetector = null;
		root_dir = null;
		
		wavManager = null;
		bnkManager = null;
		seqManager = null;
	}
	
	/*----- RomInfo -----*/
	
	private static final String[] RES_ROMINFO_VERS = {"czle_1-0", "nzse", "pzle","nzlp_mq_dbg"};
	
	private RomDetector romdetector; //Mapped by md5sum string
	private ZeqerRom last_rom;
	
 	private boolean loadRomInfoMap_fs() throws IOException{
		String rootdir = getProgramDirectory();
		if(rootdir == null || rootdir.isEmpty()) return false;
		
		String dir = rootdir + File.separator + DIRNAME_ROMINFO;
		if(!FileBuffer.directoryExists(dir)) return false;
		
		DirectoryStream<Path> dstr = Files.newDirectoryStream(Paths.get(dir));
		for(Path p : dstr){
			String pname = p.toAbsolutePath().toString();
			if(pname.endsWith(".xml")){
				//System.err.println("ZeqerCore: Loading ROM xml -- " + pname);
				ZeqerRomInfo zri = ZeqerRomInfo.readXML(pname);
				if(zri != null){
					List<RomInfoNode> nlist = zri.getAllRomNodes();
					for(RomInfoNode rin : nlist){
						romdetector.mapRomInfo(rin);
					}
				}
			}
		}
		dstr.close();
		
		return true;
	}
	
	private boolean loadRomInfoMap_jar() throws IOException{
		
		//TODO This dependency on the hardcoded name list needs to be fixed
		for(String f : RES_ROMINFO_VERS){
			String respath = RES_JARPATH + "/" + f + ".xml";
			//System.err.println("ZeqerCore: Loading ROM xml from JAR -- " + respath);
			String tempath = FileBuffer.generateTemporaryPath("zeqercore_loadrominfomap_jar");
			try{
				InputStream jarstr = ZeqerCore.class.getResourceAsStream(respath);
				if(jarstr == null) continue;
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tempath));
				int b = -1;
				while((b = jarstr.read()) != -1) bos.write(b);
				bos.close();
				
				ZeqerRomInfo zri = ZeqerRomInfo.readXML(tempath);
				if(zri != null){
					List<RomInfoNode> nlist = zri.getAllRomNodes();
					for(RomInfoNode rin : nlist){
						romdetector.mapRomInfo(rin);
					}
					//zri.printToStdErr();
				}
				
				Files.deleteIfExists(Paths.get(tempath));				
			}
			catch(Exception ex){
				System.err.println("Couldn't read rominfo xml (in JAR) for: " + f);
				ex.printStackTrace();
				continue;
			}
			
		}
		
		return true;
	}
	
	private boolean loadRomInfoMap(){
		romdetector = new RomDetector();
		try{
			if(!loadRomInfoMap_fs()) return loadRomInfoMap_jar();
			return true;
		}
		catch(Exception ex){
			ex.printStackTrace();
			return false;
		}
	}
	
	public RomInfoNode detectROM(String path) throws IOException{
		if(romdetector == null) loadRomInfoMap();
		return romdetector.detectRomVersion(path);
	}
	
	public ZeqerRom loadNUSROM(String path) throws IOException{
		RomInfoNode info = detectROM(path);
		if(info == null) return null;
		if(info.isGamecube()) return null;
		if(!(info instanceof NusRomInfo)) return null;
		NusRomInfo rominfo = (NusRomInfo)info;
		
		ZeqerRom rom = new ZeqerRom(path, rominfo);
		last_rom = rom;
		return rom;
	}
	
	public ZeqerPlaybackEngine loadRomBuild(String zeqer_id){
		//TODO
		return null;
	}
	
	public ZeqerRom lastRomUsed(){return last_rom;}
	
	/*----- Tables -----*/
	
	private CoreWaveManager wavManager;
	private CoreBankManager bnkManager;
	private CoreSeqManager seqManager;
	
	public void loadSoundTables() throws IOException, UnsupportedFileTypeException{
		String pdir = getProgramDirectory();
		if(pdir == null) return;
		
		String loadpath = pdir + SEP + DIRNAME_WAVE;
		wavManager = new CoreWaveManager(loadpath, admin_write);
		
		loadpath = pdir + SEP + DIRNAME_BANK;
		bnkManager = new CoreBankManager(loadpath, wavManager, admin_write);
		
		loadpath = pdir + SEP + DIRNAME_SEQ;
		seqManager = new CoreSeqManager(loadpath, admin_write);
	}
	
	public void saveZeqerTables() throws IOException{
		if(wavManager != null) wavManager.saveSysTable();
		if(bnkManager != null) bnkManager.saveSysTables();
		if(seqManager != null) seqManager.saveSysTable();
	}
	
	public void saveSoundTables() throws IOException{
		if(wavManager != null) wavManager.saveUserTable();
		if(bnkManager != null) bnkManager.saveUserTables();
		if(seqManager != null) seqManager.saveUserTable();
	}

	public ZeqerWaveTable getZeqerWaveTable(){return wavManager != null ? wavManager.getZeqerWaveTable():null;}
	public ZeqerSeqTable getZeqerSeqTable(){return seqManager != null ? seqManager.getZeqerSeqTable():null;}
	public ZeqerBankTable getZeqerBankTable(){return bnkManager != null ? bnkManager.getZeqerBankTable():null;}
	public ZeqerPresetTable getZeqerPresetTable(){return bnkManager != null ? bnkManager.getZeqerPresetTable():null;}
	public ZeqerWaveTable getUserWaveTable(){return wavManager != null ? wavManager.getUserWaveTable():null;}
	public ZeqerSeqTable getUserSeqTable(){return seqManager != null ? seqManager.getUserSeqTable():null;}
	public ZeqerBankTable getUserBankTable(){return bnkManager != null ? bnkManager.getUserBankTable():null;}
	public ZeqerPresetTable getUserPresetTable(){return bnkManager != null ? bnkManager.getUserPresetTable():null;}
	
	/*----- Data Loading -----*/
	
	public AbldFile loadSysBuild(String rom_id) throws UnsupportedFileTypeException, IOException{
		String dirpath = getProgramDirectory();
		if(dirpath == null) return null;
		dirpath += SEP + DIRNAME_ABLD + SEP + DIRNAME_ZBLD;
		String filepath = dirpath + SEP + rom_id + ".abld";
		AbldFile abld = AbldFile.readABLD(FileBuffer.createBuffer(filepath, true));
		return abld;
	}
	
	public Z64WaveInfo getWaveByName(String wave_name){
		if(wavManager == null) return null;
		return wavManager.getWaveByName(wave_name);
	}
	
	public Z64WaveInfo getWaveInfo(int wave_uid){
		if(wavManager == null) return null;
		return wavManager.getWaveInfo(wave_uid);
	}
	
	public Z64Wave loadWave(int wave_uid){
		if(wavManager == null) return null;
		return wavManager.loadWave(wave_uid);
	}
	
	public FileBuffer loadWaveData(int wave_uid){
		if(wavManager == null) return null;
		return wavManager.loadWaveData(wave_uid);
	}
	
	public BankTableEntry getBankInfo(int bank_uid){
		if(bnkManager == null) return null;
		return bnkManager.getBankInfo(bank_uid);
	}
	
	public Z64Bank loadBank(int bank_uid){
		if(bnkManager == null) return null;
		return bnkManager.loadBank(bank_uid);
	}
	
	public Z64Instrument getPresetInstrumentByName(String preset_name){
		if(bnkManager == null) return null;
		return bnkManager.getPresetInstrumentByName(preset_name);
	}
	
	public SeqTableEntry getSeqInfo(int seq_uid){
		if(seqManager == null) return null;
		return seqManager.getSeqInfo(seq_uid);
	}
	
	public ZeqerSeq loadSeq(int seq_uid){
		if(seqManager == null) return null;
		return seqManager.loadSeq(seq_uid);
	}
	
	public FileBuffer loadSeqData(int seq_uid){
		if(seqManager == null) return null;
		return seqManager.loadSeqData(seq_uid);
	}
	
	public VersionWaveTable loadWaveVersionTable(String rom_id) throws IOException{
		if(wavManager == null) return null;
		return wavManager.loadWaveVersionTable(rom_id);
	}
	
	public WaveLocIDMap loadVersionWaveOffsetIDMap(String rom_id) throws IOException{
		if(wavManager == null) return null;
		return wavManager.loadVersionWaveOffsetIDMap(rom_id);
	}
	
	/*----- Data Saving -----*/
	
	private boolean admin_write = false;
	
	public RomExtractionSummary extractSoundDataFromRom(ZeqerRom z_rom) throws IOException{
		//----- Check ROM
		RomExtractionSummary errinfo = new RomExtractionSummary();
		if(z_rom == null){
			errinfo.genError = RomExtractionSummary.GENERR_INVALID_ROM;
			return errinfo;
		}
		NusRomInfo rominfo = z_rom.getRomInfo();
		if(rominfo == null){
			errinfo.genError = RomExtractionSummary.GENERR_INVALID_ROM;
			return errinfo;
		}
		String zid = rominfo.getZeqerID();
		errinfo.romid = zid;
		
		//----- Waves
		if(!wavManager.importROMWaves(z_rom, errinfo)){
			errinfo.genError = RomExtractionSummary.GENERR_WAV_DUMP_FAILED;
			return errinfo;
		}
		
		//----- Banks
		//Load wave loc map
		WaveLocIDMap wavelocs = wavManager.loadVersionWaveOffsetIDMap(zid);
		if(!bnkManager.importROMBanks(z_rom, wavelocs, errinfo)){
			errinfo.genError = RomExtractionSummary.GENERR_BNK_DUMP_FAILED;
			return errinfo;
		}
		
		//----- Seqs
		if(!seqManager.importROMSeqs(z_rom, errinfo)){
			errinfo.genError = RomExtractionSummary.GENERR_SEQ_DUMP_FAILED;
			return errinfo;
		}
		
		//Seq/Bank mapping
		int[] bankUids = bnkManager.loadVersionTable(zid);
		if(!seqManager.mapSeqBanks(z_rom, bankUids)){
			errinfo.genError = RomExtractionSummary.GENERR_SEQBNKMAP_FAILED;
			return errinfo;
		}
		
		//----- ABLDs
		int[] seqUids = seqManager.loadVersionTable(zid);
		VersionWaveTable vwt = wavManager.loadWaveVersionTable(zid);
		
		String abld_dir = getBuildDirectoryPath() + SEP + ZeqerCore.DIRNAME_ZBLD;
		AbldFile abld = AbldFile.fromROM(z_rom, seqUids, bankUids, vwt);
		if(!abld.serializeTo(abld_dir + SEP + zid + ".abld")){
			errinfo.genError = RomExtractionSummary.GENERR_ABLD_SAVE_FAILED;
			return errinfo;
		}
		
		return errinfo;
	}
	
	public boolean saveSeq(ZeqerSeq seq) throws IOException{
		if(seqManager == null) return false;
		seqManager.saveSeq(seq);
		return true;
	}
	
	/*----- Metadata -----*/
	
	public int importWaveMetaTSV(String tsv_path) throws IOException{
		if(wavManager == null) return 0;
		return wavManager.importWaveMetaTSV(tsv_path);
	}
	
	public int importSeqMetaTSV(String tsv_path) throws IOException{
		if(seqManager == null) return 0;
		return seqManager.importSeqMetaTSV(tsv_path);
	}
	
	public int importSeqLabelTSV(String tsv_path) throws IOException{
		if(seqManager == null) return 0;
		return seqManager.importSeqLabelTSV(tsv_path);
	}
	
	public int importSeqIOAliasTSV(String tsv_path) throws IOException{
		if(seqManager == null) return 0;
		return seqManager.importSeqIOAliasTSV(tsv_path);
	}
	
}
