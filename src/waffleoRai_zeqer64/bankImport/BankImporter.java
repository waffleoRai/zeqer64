package waffleoRai_zeqer64.bankImport;

import java.util.Set;

import waffleoRai_zeqer64.filefmt.ZeqerWaveIO.SampleImportOptions;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;

public abstract class BankImporter {

	//Also holds the data file
	protected String path;
	protected BankImportInfo info;
	protected SampleImportOptions sampleOps;
	protected ZeqerCoreInterface core;
	
	protected Set<String> presetTags;
	protected Set<String> sampleTags;
	
	public BankImportInfo getInfo(){return info;}
	public SampleImportOptions getSampleOptions(){return sampleOps;}
	public String getPath(){return path;}
	public ZeqerCoreInterface getCoreInterface(){return core;}
	
	public void linkCoreInterface(ZeqerCoreInterface iface){core = iface;}
	
	
}
