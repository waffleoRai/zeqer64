package waffleoRai_zeqer64.cmml.elements;

public class CMMLVarDecl {
	
	/*----- Constants -----*/
	
	/*----- Instance Variables -----*/
	
	private String type;
	private String name;
	
	/*----- Init -----*/
	
	public CMMLVarDecl(){}
	
	public CMMLVarDecl(String var_type, String var_name){
		type = var_type;
		name = var_name;
	}
	
	/*----- Getters -----*/
	
	public String getType(){return type;}
	public String getName(){return name;}
	
	/*----- Setters -----*/
	
	public void setType(String value){type = value;}
	public void setName(String value){name = value;}

}
