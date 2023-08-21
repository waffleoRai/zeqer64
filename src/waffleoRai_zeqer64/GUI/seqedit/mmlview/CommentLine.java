package waffleoRai_zeqer64.GUI.seqedit.mmlview;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

class CommentLine extends IMMLLine{
	
	private MMLScriptStyleRules styleRules;
	private String data;
	
	public CommentLine(String s, MMLScriptStyleRules rules, int position){
		data = s;
		styleRules = rules;
		
		startPos = position;
		endPos = position + data.length();
	}

	public int writeToDocument(StyledDocument doc) {
		AttributeSet attr = null;
		if(styleRules != null) attr = styleRules.getCommentStyle();
		
		try{
			if(newline){
				doc.insertString(startPos, data + "\n", attr);
				return data.length() + 1;
			}
			else{
				doc.insertString(startPos, data, attr);
				return data.length();
			}
		}
		catch (BadLocationException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public IMMLLine positionInHyperlink(int pos) {
		return null;
	}

}
