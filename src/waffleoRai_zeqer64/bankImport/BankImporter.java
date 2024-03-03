package waffleoRai_zeqer64.bankImport;

import java.util.Set;

import waffleoRai_zeqer64.ErrorCode;
import waffleoRai_zeqer64.filefmt.bank.ZeqerBankIO;
import waffleoRai_zeqer64.filefmt.wave.ZeqerWaveIO;
import waffleoRai_zeqer64.filefmt.wave.ZeqerWaveIO.SampleImportOptions;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;

public abstract class BankImporter {

	//Also holds the data file
	protected String path;
	protected BankImportInfo info;
	protected SampleImportOptions sampleOps;
	protected ZeqerCoreInterface core;
	
	protected ErrorCode sampleImportError;
	protected ErrorCode bankImportError;
	
	protected Set<String> presetTags;
	protected Set<String> sampleTags;
	
	public BankImporter(){
		sampleImportError = new ErrorCode();
		bankImportError = new ErrorCode();
		sampleImportError.value = ZeqerWaveIO.ERROR_CODE_NONE;
		bankImportError.value = ZeqerBankIO.ERR_NONE;
	}
	
	public BankImportInfo getInfo(){return info;}
	public SampleImportOptions getSampleOptions(){return sampleOps;}
	public String getPath(){return path;}
	public ZeqerCoreInterface getCoreInterface(){return core;}
	public ErrorCode getSampleImportError(){return sampleImportError;}
	public ErrorCode getBankImportError(){return bankImportError;}
	
	public void linkCoreInterface(ZeqerCoreInterface iface){core = iface;}
	
	public abstract boolean importToCore();
	
}
