package waffleoRai_zeqer64.filefmt.bank;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import waffleoRai_zeqer64.ErrorCode;
import waffleoRai_zeqer64.ZeqerBank;
import waffleoRai_zeqer64.bankImport.BankImporter;
import waffleoRai_zeqer64.bankImport.DLSImportHandler;

public class ZeqerBankIO {
	
	public static final int ERR_NONE = 0;
	public static final int ERR_NULL_INPUT_DATA = 1;
	public static final int ERR_NULL_IMPORT_PARAMS = 2;
	public static final int ERR_NULL_CORE_LINK = 3;
	public static final int ERR_SAMPLE_IMPORT_FAILED = 4;
	public static final int ERR_BANK_IMPORT_FAILED = 5;
	public static final int ERR_PRESET_IMPORT_FAILED = 6;
	public static final int ERR_INPUT_PATH_NULL = 7;
	public static final int ERR_FILE_TYPE_UNKNOWN = 8;
	public static final int ERR_FILE_READ_FAILED = 9;
	public static final int ERR_XML_ELEMENT_MISSING = 10;
	public static final int ERR_XML_NUMFORMAT = 11;
	public static final int ERR_XML_UNRESOLVED_LINK = 12;
	public static final int ERR_XML_ATTR_MISSING = 13;
	public static final int ERR_INVALID_PRESET_LINK = 14;
	
	public static String getErrorCodeString(int code){
		switch(code){
		case ERR_NONE: return "ERR_NONE";
		case ERR_NULL_INPUT_DATA: return "ERR_NULL_INPUT_DATA";
		case ERR_NULL_IMPORT_PARAMS: return "ERR_NULL_IMPORT_PARAMS";
		case ERR_NULL_CORE_LINK: return "ERR_NULL_CORE_LINK";
		case ERR_SAMPLE_IMPORT_FAILED: return "ERR_SAMPLE_IMPORT_FAILED";
		case ERR_BANK_IMPORT_FAILED: return "ERR_BANK_IMPORT_FAILED";
		case ERR_PRESET_IMPORT_FAILED: return "ERR_PRESET_IMPORT_FAILED";
		case ERR_INPUT_PATH_NULL: return "ERR_INPUT_PATH_NULL";
		case ERR_FILE_TYPE_UNKNOWN: return "ERR_FILE_TYPE_UNKNOWN";
		case ERR_FILE_READ_FAILED: return "ERR_FILE_READ_FAILED";
		}
		return null;
	}
	
	public static boolean exportDecompXML(String path, ZeqerBank data){
		try {
			boolean okay = true;
			BufferedWriter out = new BufferedWriter(new FileWriter(path));
			okay = SoundfontXML.writeSFXML(out, data.getBankData());
			out.close();
			return okay;
		}
		catch(IOException ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	public static boolean exportSF2(String path, ZeqerBank data){
		//TODO
		return false;
	}
	
	public static BankImporter initializeImport(String filePath, ErrorCode err){
		//TODO
		if(err != null) err.value = ERR_NONE;
		if(filePath == null){
			if(err != null) err.value = ERR_INPUT_PATH_NULL;
			return null;
		}
		
		//Determine file type from extension
		String pathlower = filePath.toLowerCase();
		if(pathlower.endsWith(".dls")){
			DLSImportHandler imp = new DLSImportHandler(filePath);
			if(!imp.init()){
				if(err != null) err.value = ERR_FILE_READ_FAILED;
				return null;
			}
			return imp;
		}
		else if(pathlower.endsWith(".sf2")){
			//TODO
		}
		else if(pathlower.endsWith(".xml")){
			SoundfontXML imp = SoundfontXML.readSFXMLContents(filePath);
			if(imp == null) {
				if(err != null) err.value = ERR_FILE_READ_FAILED;
				return null;
			}
			else {
				if(err != null) err.value = imp.getBankImportError().value;
			}
		}
		else{
			if(err != null) err.value = ERR_FILE_TYPE_UNKNOWN;
			return null;
		}
		
		return null;
	}
	
	public static void finishImport(BankImporter importer, ErrorCode err){
		if(err != null) err.value = ERR_NONE;
		if(importer == null){
			if(err != null) err.value = ERR_NULL_INPUT_DATA;
			return;
		}
		importer.importToCore(); //This function will set the error code.
	}
	
	public static void addImportableFileFilters(JFileChooser fc){
		fc.addChoosableFileFilter(new FileFilter(){
			public boolean accept(File f) {
				if(f.isDirectory()) return true;
				String path = f.getAbsolutePath().toString().toLowerCase();
				return path.endsWith(".xml");
			}

			public String getDescription() {
				return "Decomp XML SoundFont specification (.xml)";
			}});
		
		fc.addChoosableFileFilter(new FileFilter(){
			public boolean accept(File f) {
				if(f.isDirectory()) return true;
				String path = f.getAbsolutePath().toString().toLowerCase();
				return path.endsWith(".dls");
			}

			public String getDescription() {
				return "Downloadable Sounds (.dls)";
			}});
		
		/*fc.addChoosableFileFilter(new FileFilter(){
			public boolean accept(File f) {
				if(f.isDirectory()) return true;
				String path = f.getAbsolutePath().toString().toLowerCase();
				return path.endsWith(".sf2");
			}

			public String getDescription() {
				return "SoundFont 2 (.sf2)";
			}});*/
	}

}
