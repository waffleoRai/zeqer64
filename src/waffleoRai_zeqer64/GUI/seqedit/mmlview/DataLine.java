package waffleoRai_zeqer64.GUI.seqedit.mmlview;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import waffleoRai_SeqSound.n64al.NUSALSeqCommands;
import waffleoRai_SeqSound.n64al.NUSALSeqDataType;
import waffleoRai_SeqSound.n64al.cmd.NUSALSeqDataCommand;

class DataLine extends CmdLine{
	
	private String dataTypeText;
	private boolean useBrackets = true;

	public DataLine(NUSALSeqDataCommand command, MMLScriptStyleRules rules, int pos, int indentAmt) {
		super(command, rules, pos, indentAmt);
		
		super.cmdText = "data";
		NUSALSeqDataType dattype = command.getDataType();
		if(dattype != null){
			//Also use this to determine whether to turn off brackets.
			dataTypeText = dattype.getMMLString();
			
			int ptype = dattype.getParamPrintType();
			if(ptype == NUSALSeqCommands.MML_DATAPARAM_TYPE__BUFFER){
				useBrackets = false;
			}
		}
		else dataTypeText = "?";
		
		//Super constructor should take care of args
		updatePositions();
	}
	
	private void updatePositions(){
		int pos = startPos;
		pos += indent;
		pos += cmdText.length() + 1; //Space
		pos += dataTypeText.length();
		if(useBrackets) pos++;
		if(args != null){
			for(int i = 0; i < args.length; i++){
				if(i > 0){
					pos += 2; //Space comma	
				}
				else pos++; //Space
				args[i].startPos = pos;
				pos += args[i].text.length();
				args[i].endPos = pos;
			}
		}
		if(useBrackets) pos++;
		
		endPos = pos;
		
	}
	
	public int writeToDocument(StyledDocument doc) {
		if(source == null) return 0;
		
		AttributeSet attr_lbl = null;
		AttributeSet attr_dat = null;
		if(styleRules != null){
			attr_lbl = styleRules.getLabelStyle();
			attr_dat = styleRules.getDataStyle();
		}
		
		try{
			int inspos = startPos;
			
			for(int i = 0; i < indent; i++){
				doc.insertString(inspos, "\t", attr_dat);
				inspos++;
			}
			
			doc.insertString(inspos, cmdText + " ", attr_dat);
			inspos += cmdText.length();
			
			doc.insertString(inspos, dataTypeText, attr_dat);
			inspos += dataTypeText.length();
			
			if(useBrackets){
				doc.insertString(inspos, " {", attr_dat);
				inspos += 2;
			}
			
			if(args != null){
				for(int i = 0; i < args.length; i++){
					if(args[i] == null) continue;
					
					if(i == 0){
						if(!useBrackets){
							doc.insertString(inspos, " ", attr_dat);
							inspos++;
						}
					}
					else{
						doc.insertString(inspos, ", ", attr_dat);
						inspos += 2;
					}
					
					if(args[i].ref != null){
						doc.insertString(inspos, args[i].text, attr_lbl);
						inspos += args[i].text.length();
					}
					else{
						doc.insertString(inspos, args[i].text, attr_dat);
						inspos += args[i].text.length();
					}
				}
			}
			
			if(useBrackets){
				doc.insertString(inspos, "}", attr_dat);
				inspos++;
			}
			
			if(newline){
				doc.insertString(inspos, "\n", attr_dat);
				inspos++;
			}
			
			return inspos - startPos;
		}
		catch (BadLocationException e) {
			e.printStackTrace();
			return 0;
		}
	}

}
