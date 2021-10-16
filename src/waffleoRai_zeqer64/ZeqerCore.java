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

import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.FileUtils;
import waffleoRai_zeqer64.filefmt.NusRomInfo;
import waffleoRai_zeqer64.filefmt.RomInfoNode;
import waffleoRai_zeqer64.filefmt.ZeqerBankTable;
import waffleoRai_zeqer64.filefmt.ZeqerRomInfo;
import waffleoRai_zeqer64.filefmt.ZeqerSeqTable;
import waffleoRai_zeqer64.filefmt.ZeqerWaveTable;

public class ZeqerCore {
	
	//Does the same stuff that I usually put in XYZProgramFiles
	
	/*----- Misc Constants -----*/
	
	public static final String ENCODING = "UTF8";
	
	public static final String DIRNAME_ROMINFO = "rominfo";
	public static final String DIRNAME_WAVE = "wav";
	public static final String DIRNAME_SEQ = "seq";
	public static final String DIRNAME_BANK = "bnk";
	public static final String DIRNAME_ENGINE = "engine";
	
	public static final String DIRNAME_ZSEQ = "zseqs";
	public static final String DIRNAME_ZBANK = "zbnks";
	
	public static final String FN_SYSBANK = "zbanks.bin";
	public static final String FN_SYSWAVE = "zwavs.bin";
	public static final String FN_SYSSEQ_OOT = "ootseqs.bin";
	public static final String FN_SYSSEQ_MM = "mmseqs.bin";
	
	public static final String FN_USRBANK = "mybanks.bin";
	public static final String FN_USRSEQ = "myseqs.bin";
	
	public static final String IKEY_VERSION = "VERSION";
	public static final String CURRENT_VERSION = "1.0.0";
	
	private static final String RES_JARPATH = "/waffleoRai_zeqer64/res";
	
	/*----- Font -----*/
	
	public static final String IKEY_UNIFONT_NAME = "UNICODE_FONT";
	private static final String[] TRYFONTS = {"Arial Unicode MS", "MS PGothic", "MS Gothic", 
			"AppleGothic", "Takao PGothic",
			"Hiragino Maru Gothic Pro", "Hiragino Kaku Gothic Pro"};
	
	private static String my_unifont;
	
	public static Font getUnicodeFont(int style, int size){
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
	
	public static boolean loadCore() throws IOException{
		String pdir = getProgramDirectory();
		if(pdir == null) return false; //Not installed or other issue
		
		//Load settings
		loadSettingsFile(pdir + File.separator + SETTINGS_FILE_NAME);
		
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
	
	private static String ini_path;
	private static String root_dir;
	
	public static String getIniPath(){
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
	
	public static String getProgramDirectory(){
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
	
	/*----- Settings -----*/
	
	public static final String SETTINGS_FILE_NAME = "settings.ini";
	
	private static Map<String, String> init_values;
	
	public static String getIniValue(String key){
		if(init_values == null) return null;
		return init_values.get(key);
	}
	
	public static void setIniValue(String key, String value){
		if(init_values == null) return;
		init_values.put(key, value);
	}
	
	public static void loadSettingsFile(String filepath) throws IOException{
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
	
	public static void saveSettingsFile(String filepath) throws IOException{
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
	
	public static boolean installTo(String dirpath) throws IOException{
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
		String respath = RES_JARPATH + "/" + FN_SYSSEQ_OOT;
		String targetpath = zsubdir + File.separator + FN_SYSSEQ_OOT;
		good = good && extractFromJAR(respath, targetpath);
		respath = RES_JARPATH + "/" + FN_SYSSEQ_MM;
		targetpath = zsubdir + File.separator + FN_SYSSEQ_MM;
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
		if(!FileBuffer.directoryExists(subdir)){
			Files.createDirectory(Paths.get(subdir));
		}
		respath = RES_JARPATH + "/" + FN_SYSWAVE;
		targetpath = subdir + File.separator + FN_SYSWAVE;
		good = good && extractFromJAR(respath, targetpath);
		
		//engine
		subdir = dirpath + File.separator + DIRNAME_ENGINE;
		if(!FileBuffer.directoryExists(subdir)){
			Files.createDirectory(Paths.get(subdir));
		}
		//TODO
		
		return good;
	}
	
	public static boolean uninstall() throws IOException{
		String pdir = getProgramDirectory();
		if(pdir == null) return false;
		
		FileUtils.deleteRecursive(pdir);
		Files.deleteIfExists(Paths.get(getIniPath()));
		
		return true;
	}
	
	public static boolean importUserDataFrom(String dirpath) throws UnsupportedFileTypeException, IOException{
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
	
	public static boolean copyUserDataTo(String dirpath) throws IOException{
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
	
	/*----- ShutDown -----*/
	
	public static void shutdownCore() throws IOException{
		//Save settings and all that.
		String pdir = getProgramDirectory();
		if(pdir == null) return;
		
		saveSettingsFile(pdir + File.separator + SETTINGS_FILE_NAME);
		saveSoundTables();
		
		init_values.clear();
		romdetector = null;
		root_dir = null;
		
		wav_table_sys = null;
		seq_table_sys_oot = null;
		seq_table_sys_mm = null;
		bnk_table_sys = null;
		seq_table_user = null;
		bnk_table_user = null;
	}
	
	/*----- RomInfo -----*/
	
	private static final String[] RES_ROMINFO_VERS = {"czle_1-0", "nzse", "pzle"};
	
	private static RomDetector romdetector; //Mapped by md5sum string
	
	private static boolean loadRomInfoMap_fs() throws IOException{
		String rootdir = getProgramDirectory();
		if(rootdir == null || rootdir.isEmpty()) return false;
		
		String dir = rootdir + File.separator + DIRNAME_ROMINFO;
		
		DirectoryStream<Path> dstr = Files.newDirectoryStream(Paths.get(dir));
		for(Path p : dstr){
			String pname = p.toAbsolutePath().toString();
			if(pname.endsWith(".xml")){
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
	
	private static boolean loadRomInfoMap_jar() throws IOException{
		
		for(String f : RES_ROMINFO_VERS){
			String respath = RES_JARPATH + "/" + f + ".xml";
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
	
	private static boolean loadRomInfoMap(){
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
	
	public static RomInfoNode detectROM(String path) throws IOException{
		if(romdetector == null) loadRomInfoMap();
		return romdetector.detectRomVersion(path);
	}
	
	public static ZeqerRom loadNUSROM(String path) throws IOException{
		RomInfoNode info = detectROM(path);
		if(info == null) return null;
		if(info.isGamecube()) return null;
		if(!(info instanceof NusRomInfo)) return null;
		NusRomInfo rominfo = (NusRomInfo)info;
		
		ZeqerRom rom = new ZeqerRom(path, rominfo);
		return rom;
	}
	
	/*----- Tables -----*/
	
	private static ZeqerWaveTable wav_table_sys;
	private static ZeqerSeqTable seq_table_sys_oot;
	private static ZeqerSeqTable seq_table_sys_mm;
	private static ZeqerBankTable bnk_table_sys;
	
	private static ZeqerSeqTable seq_table_user;
	private static ZeqerBankTable bnk_table_user;
	
	public static void loadSoundTables() throws IOException, UnsupportedFileTypeException{
		String pdir = getProgramDirectory();
		if(pdir == null) return;
		
		char sep = File.separatorChar;
		String loadpath = pdir + sep + DIRNAME_WAVE + sep + FN_SYSWAVE;
		FileBuffer buffer = null;
		if(FileBuffer.fileExists(loadpath)) {
			buffer = FileBuffer.createBuffer(loadpath, true);
			wav_table_sys = ZeqerWaveTable.readTable(buffer);
		}
		
		loadpath = pdir + sep + DIRNAME_SEQ + sep + DIRNAME_ZSEQ + sep + FN_SYSSEQ_OOT;
		buffer = null;
		if(FileBuffer.fileExists(loadpath)) {
			buffer = FileBuffer.createBuffer(loadpath, true);
			seq_table_sys_oot = ZeqerSeqTable.readTable(buffer);
		}
		
		loadpath = pdir + sep + DIRNAME_SEQ + sep + DIRNAME_ZSEQ + sep + FN_SYSSEQ_MM;
		buffer = null;
		if(FileBuffer.fileExists(loadpath)) {
			buffer = FileBuffer.createBuffer(loadpath, true);
			seq_table_sys_mm = ZeqerSeqTable.readTable(buffer);
		}
		
		loadpath = pdir + sep + DIRNAME_BANK + sep + DIRNAME_ZBANK + sep + FN_SYSBANK;
		buffer = null;
		if(FileBuffer.fileExists(loadpath)) {
			buffer = FileBuffer.createBuffer(loadpath, true);
			bnk_table_sys = ZeqerBankTable.readTable(buffer);
		}
		
		loadpath = pdir + sep + DIRNAME_SEQ + sep + FN_USRSEQ;
		buffer = null;
		if(FileBuffer.fileExists(loadpath)) {
			buffer = FileBuffer.createBuffer(loadpath, true);
			seq_table_user = ZeqerSeqTable.readTable(buffer);
		}
		
		loadpath = pdir + sep + DIRNAME_BANK + sep + FN_USRBANK;
		buffer = null;
		if(FileBuffer.fileExists(loadpath)) {
			buffer = FileBuffer.createBuffer(loadpath, true);
			bnk_table_user = ZeqerBankTable.readTable(buffer);
		}
	}
	
	public static void saveSoundTables() throws IOException{
		String pdir = getProgramDirectory();
		if(pdir == null) return;
		
		char sep = File.separatorChar;
		String savepath = pdir + sep + DIRNAME_SEQ + sep + FN_USRSEQ;
		if(seq_table_user != null) seq_table_user.writeTo(savepath);
		
		savepath = pdir + sep + DIRNAME_BANK + sep + FN_USRBANK;
		if(bnk_table_user != null) bnk_table_user.writeTo(savepath);
	}

	public static ZeqerWaveTable getZeqerWaveTable(){return wav_table_sys;}
	public static ZeqerSeqTable getZeqerZ5SeqTable(){return seq_table_sys_oot;}
	public static ZeqerSeqTable getZeqerZ6SeqTable(){return seq_table_sys_mm;}
	public static ZeqerBankTable getZeqerBankTable(){return bnk_table_sys;}
	
	public static ZeqerBankTable getUserBankTable(){
		if(bnk_table_user == null) bnk_table_user = ZeqerBankTable.createTable();
		return bnk_table_user;
	}
	
	public static ZeqerSeqTable getUserSeqTable(){
		if(seq_table_user == null) seq_table_user = ZeqerSeqTable.createTable();
		return seq_table_user;
	}
	
}
