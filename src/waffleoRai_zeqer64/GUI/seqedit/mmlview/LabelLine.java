package waffleoRai_zeqer64.GUI.seqedit.mmlview;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

class LabelLine extends IMMLLine{
	
	private MMLScriptStyleRules styleRules;
	private String label; //Only the label itself is colored. Colon is not.

	public LabelLine(String s, MMLScriptStyleRules rules, int position){
		label = s;
		styleRules = rules;
		
		startPos = position;
		endPos = position + label.length() + 1;
	}

	public int writeToDocument(StyledDocument doc) {
		AttributeSet attr_lbl = null;
		AttributeSet attr_std = null;
		if(styleRules != null){
			attr_lbl = styleRules.getLabelStyle();
			attr_std = styleRules.getBaseStyle();
		}
		
		try{
			int inspos = startPos;
			doc.insertString(inspos, label, attr_lbl);
			
			inspos += label.length();
			doc.insertString(inspos, ":", attr_std);
			inspos++;
			
			if(newline){
				doc.insertString(inspos, "\n", attr_std);
				return label.length() + 2;
			}
			else{
				return label.length() + 1;
			}
		}
		catch (BadLocationException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public IMMLLine positionInHyperlink(int pos) {return null;}

}
