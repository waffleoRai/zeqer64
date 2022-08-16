package waffleoRai_zeqer64.cmml;

import java.io.OutputStream;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import waffleoRai_SeqSound.n64al.NUSALSeqCommand;
import waffleoRai_SeqSound.n64al.cmd.NUSALSeqCommandChunk;

public class CMMLModule {
	
	/*----- Instance Variables -----*/
	
	private String module_name;
	private int mml_context;
	
	private Map<String, Integer> seqio_alias;
	private Map<String, Integer> chio_alias;
	private Map<String, Integer> ochio_alias; //Values use second byte for channel
	
	private NUSALSeqCommandChunk bin;
	private List<UnresolvedLinkCommand> import_sym;
	private Map<String, NUSALSeqCommand> export_sym;
	
	/*----- Init -----*/
	
	
	
	/*----- Interface -----*/
	
	public void outputBin(OutputStream output){
		//TODO
		//Will NOT work if there are unresolved links...
	}
	
	public void outputMus(Writer output){
		//TODO
	}
	
}
