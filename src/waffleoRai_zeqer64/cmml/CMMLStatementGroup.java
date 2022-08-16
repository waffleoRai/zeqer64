package waffleoRai_zeqer64.cmml;

import java.util.LinkedList;
import java.util.List;

import waffleoRai_SeqSound.n64al.NUSALSeqDataType;
import waffleoRai_SeqSound.n64al.cmd.NUSALSeqReader;

public class CMMLStatementGroup {
	
	/*----- Instance Variables -----*/
	
	private String pre_text;
	private String post_text;
	
	private int mml_context = NUSALSeqReader.PARSEMODE_UNDEFINED;
	private NUSALSeqDataType mml_dtype;
	
	private CMMLStatementGroup parent;
	private List<CMMLStatementGroup> children;
	
	/*----- Init -----*/
	
	protected CMMLStatementGroup(){}
	
	public CMMLStatementGroup(CMMLStatementGroup parent_group){
		parent = parent_group;
		children = new LinkedList<CMMLStatementGroup>();
	}
	
	/*----- Getters -----*/
	
	public String getText(){return post_text;}
	public String getPreText(){return pre_text;}
	public String getPostText(){return post_text;}
	
	public boolean isGroup(){return true;}
	public CMMLStatementGroup getParent(){return parent;}
	
	public int getMMLContext(){return mml_context;}
	public NUSALSeqDataType getMMLDataType(){return mml_dtype;}
	
	public int childCount(){
		if(children == null) return 0;
		return children.size();
	}
	
	public boolean expectsPost(){
		//rn only detects do/while and typedef...
		if(pre_text == null) return false;
		if(pre_text.equals("do")) return true;
		if(pre_text.startsWith("typedef")) return true;
		return false;
	}
	
	/*----- Setters -----*/
	
	protected void setParent(CMMLStatementGroup parent_group){
		//parent = parent_group;
		if(parent != null) parent.removeChild(this);
		if(parent_group == null) {
			parent = null;
			return;
		}
		parent = parent_group;
		parent.addChild(this);
	}
	
	public void addChild(CMMLStatementGroup child){
		if(children == null) return;
		children.add(child);
		child.parent = this;
	}
	
	public void removeChild(CMMLStatementGroup child){
		if(children == null) return;
		children.remove(child);
		if(child.parent == this) child.parent = null;
	}
	
	public void setText(String text){post_text = text;}
	public void setPreText(String text){pre_text = text;}
	public void setPostText(String text){post_text = text;}
	
	public void setMMLContext(int val){this.mml_context = val;}
	public void setMMLDataType(NUSALSeqDataType dtype){this.mml_dtype = dtype;}
	
	/*----- Internal -----*/
	
	protected List<CMMLStatementGroup> evaluate(){
		//TODO
		//Preparse to determine what kind of logic this statement has, and for non-groups,
		//	how many statements it needs to be split up into.
		return null;
	}
	
	/*----- Compare -----*/
	
	public boolean equals(Object o){
		return this == o;
	}

}
