package waffleoRai_zeqer64.GUI.seqedit.mmlview;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

class EmptyLine extends IMMLLine {
	
	private MMLScriptStyleRules style;
	
	public EmptyLine(MMLScriptStyleRules styler){
		super.newline = true;
		style = styler;
	}

	public int writeToDocument(StyledDocument doc) {
		try {
			doc.insertString(doc.getLength(), "\n", style.getBaseStyle());
			return 1;
		} 
		catch (BadLocationException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public IMMLLine positionInHyperlink(int pos) {return null;}

}
