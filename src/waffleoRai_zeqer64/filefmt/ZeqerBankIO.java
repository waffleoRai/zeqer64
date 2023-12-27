package waffleoRai_zeqer64.filefmt;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import waffleoRai_soundbank.nintendo.z64.Z64Bank;
import waffleoRai_zeqer64.ZeqerBank;
import waffleoRai_zeqer64.bankImport.BankImportInfo;
import waffleoRai_zeqer64.bankImport.BankImporter;
import waffleoRai_zeqer64.filefmt.ZeqerBankTable.BankTableEntry;
import waffleoRai_zeqer64.filefmt.ZeqerWaveIO.SampleImportOptions;

public class ZeqerBankIO {
	
	public static final int ERR_NONE = 0;
	public static final int ERR_NULL_INPUT_DATA = 1;
	public static final int ERR_NULL_IMPORT_PARAMS = 2;
	public static final int ERR_NULL_CORE_LINK = 3;
	public static final int ERR_SAMPLE_IMPORT_FAILED = 4;
	public static final int ERR_BANK_IMPORT_FAILED = 5;
	public static final int ERR_PRESET_IMPORT_FAILED = 6;
	
	public static class BankImportOptions{
		public String importPath;
		
		public BankImportInfo bnkImport;
		public SampleImportOptions smplImport;
		//TODO read input file first, then let user specify WHICH
		//	samples and presets they want to import instead?
	}
	
	public static class BankImportResult{
		public int errCode;
		public BankTableEntry metaEntry;
		public Z64Bank data;
	}
	
	public static boolean exportDecompXML(String path, ZeqerBank data){
		//TODO
		return false;
	}
	
	public static boolean exportSF2(String path, ZeqerBank data){
		//TODO
		return false;
	}
	
	public static BankImporter initializeImport(String filePath){
		//TODO
		return null;
	}
	
	public static void finishImport(BankImporter importer){
		//TODO
	}
	
	public static BankImportResult importDLS(BankImportOptions ops){
		//TODO
		return null;
	}
	
	public static BankImportResult importSF2(BankImportOptions ops){
		//TODO
		return null;
	}
	
	public static BankImportResult importDecompXML(BankImportOptions ops){
		//TODO
		return null;
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
		
		fc.addChoosableFileFilter(new FileFilter(){
			public boolean accept(File f) {
				if(f.isDirectory()) return true;
				String path = f.getAbsolutePath().toString().toLowerCase();
				return path.endsWith(".sf2");
			}

			public String getDescription() {
				return "SoundFont 2 (.sf2)";
			}});
		
	}

}
