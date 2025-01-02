package waffleoRai_zeqer64.GUI.seqedit.mmlview;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import waffleoRai_SeqSound.n64al.NUSALSeqCmdType;
import waffleoRai_SeqSound.n64al.NUSALSeqCommand;
import waffleoRai_SeqSound.n64al.cmd.NUSALSeqCommandDef;

class CmdLine extends IMMLLine{
	
	public static class CmdLineArg{
		public String text;
		public IMMLLine ref;
		public boolean unresolvedRef = false;
		
		public int startPos;
		public int endPos;
	}
	
	protected MMLScriptStyleRules styleRules;
	protected NUSALSeqCommand source;
	
	protected String cmdText;
	protected CmdLineArg[] args;
	protected int indent; // # of tabs to put in
	
	public CmdLine(NUSALSeqCommand command, MMLScriptStyleRules rules, int pos, int indentAmt){
		//Save initial params
		styleRules = rules;
		source = command;
		indent = indentAmt;
		if(source == null) return;
		
		//Extract address
		super.binAddr = source.getAddress();
		
		//Extract command text
		NUSALSeqCommandDef def = command.getCommandDef();
		if(def != null) {
			cmdText = def.getMnemonicZeqer();
		}
		else {
			NUSALSeqCmdType type = command.getFunctionalType();
			if(type != null){
				cmdText = type.toString();
			}
			else cmdText = "[Unknown]";
		}

		//Extract args
		String[][] astr = command.getParamStrings();
		if(astr != null){
			int acount = astr.length;
			args = new CmdLineArg[acount];
			
			for(int i = 0; i < acount; i++){
				args[i] = new CmdLineArg();
				args[i].text = astr[i][0];
				if(astr[i][1] != null) args[i].unresolvedRef = true;
			}
		}
		
		//Calculate positions...
		startPos = pos;
		updatePositions();
	}
	
	private void updatePositions(){
		//Overall end and arg positions
		int pos = startPos;
		pos += indent;
		pos += cmdText.length();
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
		
		endPos = pos;
	}
	
	public void setIndent(int val){
		indent = val;
		updatePositions();
	}
	
	public int writeToDocument(StyledDocument doc) {
		if(source == null) return 0;
		
		AttributeSet attr_lbl = null;
		AttributeSet attr_std = null;
		AttributeSet attr_cmd = null;
		if(styleRules != null){
			attr_lbl = styleRules.getLabelStyle();
			attr_std = styleRules.getBaseStyle();
			attr_cmd = styleRules.getStyleForCommand(source.getFunctionalType());
		}
		
		try{
			int inspos = startPos;
			
			for(int i = 0; i < indent; i++){
				doc.insertString(inspos, "\t", attr_std);
				inspos++;
			}
			
			doc.insertString(inspos, cmdText, attr_cmd);
			inspos += cmdText.length();
			
			if(args != null){
				for(int i = 0; i < args.length; i++){
					if(args[i] == null) continue;
					
					if(i == 0){
						doc.insertString(inspos, " ", attr_std);
						inspos++;
					}
					else{
						doc.insertString(inspos, ", ", attr_std);
						inspos += 2;
					}
					
					if(args[i].ref != null){
						doc.insertString(inspos, args[i].text, attr_lbl);
						inspos += args[i].text.length();
					}
					else{
						doc.insertString(inspos, args[i].text, attr_std);
						inspos += args[i].text.length();
					}
				}
			}
			
			if(newline){
				doc.insertString(inspos, "\n", attr_std);
				inspos++;
			}
			
			return inspos - startPos;
		}
		catch (BadLocationException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public IMMLLine positionInHyperlink(int pos) {
		if(args == null) return null;
		for(int i = 0; i < args.length; i++){
			if(args[i].ref != null){
				if(pos >= args[i].startPos){
					if(pos < args[i].endPos) return args[i].ref;
				}
			}
		}
		return null;
	}

	public List<CmdLineArg> getArgs(){
		if(args == null || args.length == 0) return new LinkedList<CmdLineArg>();
		List<CmdLineArg> list = new ArrayList<CmdLineArg>(args.length);
		for(int i = 0; i < args.length; i++) list.add(args[i]);
		return list;
	}
	
}
