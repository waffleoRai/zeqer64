package waffleoRai_zeqer64.GUI.seqedit.mmlview;

import javax.swing.text.StyledDocument;

public abstract class IMMLLine {
	
	protected int binAddr = -1;
	
	protected int startPos = -1;
	protected int endPos = -1;
	
	protected boolean newline = true;

	public abstract int writeToDocument(StyledDocument doc); //Returns number of characters written
	public int getBinAddress(){return binAddr;}
	
	public int getStartPosition(){return startPos;}
	public int getEndPosition(){return endPos;}
	
	public void setStartPosition(int val){startPos = val;}
	public void setEndPosition(int val){endPos = val;}
	
	public void setIncludeNewline(boolean b){newline = b;}
	
	public int positionIsInLine(int pos){
		//-1 means before, 0 means on, 1 means after
		if(pos < startPos) return -1;
		if(pos >= endPos) return 1;
		return 0;
	}
	
	public abstract IMMLLine positionInHyperlink(int pos); //If position is inside a hyperlink, return the line it is linked to
	
}
