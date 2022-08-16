package waffleoRai_zeqer64.cmml.elements;

public class CMMLSymbol implements CMMLElement{
	
	/*----- Constants -----*/
	
	public static final int TYPE_UNK = -1;
	public static final int TYPE_LOCALVAR = 0;
	public static final int TYPE_IOVAR = 1;
	public static final int TYPE_ARGVAR = 2;
	public static final int TYPE_FUNCCALL = 3;
	
	/*----- Instance Variables -----*/
	
	private String symbol;
	private int type = TYPE_UNK;
	
	/*----- Init -----*/
	
	public CMMLSymbol(){}
	
	/*----- Getters -----*/
	
	public String getSymbolName(){return symbol;}
	public int getType(){return type;}
	
	/*----- Setters -----*/

	public void setSymbolName(String name){symbol = name;}
	public void setType(int symbol_type){type = symbol_type;}
	
}
