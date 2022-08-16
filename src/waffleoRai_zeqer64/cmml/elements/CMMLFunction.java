package waffleoRai_zeqer64.cmml.elements;

public class CMMLFunction implements CMMLElement{
	
	/*----- Constants -----*/
	
	/*----- Instance Variables -----*/
	
	private boolean is_inline = false;
	
	private String name;
	
	private String ret_type;
	private CMMLVarDecl[] args;
	
	/*----- Init -----*/
	
	public CMMLFunction(){}
	
	/*----- Getters -----*/
	
	public boolean isInline(){return is_inline;}
	
	public String getName(){return name;}
	public String getReturnType(){return ret_type;}
	
	public CMMLVarDecl getArg(int index){
		if(index < 0 || args == null) return null;
		if(index >= args.length) return null;
		return args[index];
	}
	
	/*----- Setters -----*/
	
	public void allocArgs(int count){args = new CMMLVarDecl[count];}
	
	public boolean setArg(int index, String type, String name){
		if(args == null) return false;
		if(index < 0 || index >= args.length) return false;
		CMMLVarDecl a = new CMMLVarDecl(type, name);
		args[index] = a;
		return true;
	}
	
	public void setName(String value){name = value;}
	public void setReturnType(String value){ret_type = value;}
	
	public void setFlagInline(boolean b){is_inline = b;}

}
