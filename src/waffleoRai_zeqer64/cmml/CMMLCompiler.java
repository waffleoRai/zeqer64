package waffleoRai_zeqer64.cmml;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import waffleoRai_SeqSound.n64al.cmd.NUSALSeqCommandChunk;

public class CMMLCompiler {

	/*----- Constants -----*/
	
	/*----- Instance Variables -----*/
	
	private CMMLPreprocessor preprocessor;
	
	private List<CMMLModule> output;
	
	/*----- Init -----*/
	
	public CMMLCompiler(){
		initDefault();
	}
	
	private void initDefault(){
		preprocessor = new CMMLPreprocessor();
		output = new LinkedList<CMMLModule>();
	}
	
	/*----- Getters -----*/
	
	/*----- Setters -----*/
	
	/*----- Internal Methods -----*/
	
	/*----- Interface -----*/
	
	public CMMLModule compile(String cmml_path){
		//TODO
		//Give it the path so that you can load includes!
		return null;
	}
	
	public void outputModularMus(String dirpath){
		//TODO
	}
	
	public NUSALSeqCommandChunk complete(){
		//TODO
		//Does inter-module linking and returns the result.
		return null;
	}
	
}
