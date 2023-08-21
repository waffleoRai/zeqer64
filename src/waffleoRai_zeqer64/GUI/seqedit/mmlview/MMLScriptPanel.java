package waffleoRai_zeqer64.GUI.seqedit.mmlview;

import javax.swing.JPanel;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.StyledDocument;

import waffleoRai_SeqSound.n64al.NUSALSeq;
import waffleoRai_SeqSound.n64al.NUSALSeqCommand;
import waffleoRai_SeqSound.n64al.cmd.NUSALSeqCommandChunk;
import waffleoRai_SeqSound.n64al.cmd.NUSALSeqDataCommand;
import waffleoRai_zeqer64.GUI.seqedit.mmlview.CmdLine.CmdLineArg;

import java.awt.GridBagLayout;
import javax.swing.JScrollPane;

import java.awt.Color;
import java.awt.GridBagConstraints;
import javax.swing.JTextPane;
import javax.swing.border.BevelBorder;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MMLScriptPanel extends JPanel{

	private static final long serialVersionUID = -3444244058038684444L;
	
	private static final Color SELECT_COLOR_FOCUS = new Color(198, 225, 245);
	private static final Color SELECT_COLOR_NO_FOCUS = new Color(216, 227, 235);
	
	/*----- Inner Classes -----*/
	
	private static class RefInfo{
		//public String label;
		public IMMLLine reference;
		public List<CmdLineArg> referees;
		
		public RefInfo(){
			referees = new LinkedList<CmdLineArg>();
		}
	}
	
	/*----- Instance Variables -----*/
	
	private JTextPane textPane;
	private JScrollPane scrollPane;
	
	private StyledDocument doc;
	private MMLScriptStyleRules scriptStyler;
	
	private ArrayList<IMMLLine> lines;
	private Map<String, RefInfo> linkingMap;
	
	private List<ScriptJumpListener> jumpListeners;
	
	/*----- Init -----*/
	
	public MMLScriptPanel(){
		jumpListeners = new LinkedList<ScriptJumpListener>();
		initGUI();
		resetDocument();
	}
	
	private void initGUI(){
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(5, 5, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		add(scrollPane, gbc_scrollPane);
		
		textPane = new JTextPane();
		textPane.setEditable(false);
		
		SelectCaret caret = new SelectCaret();
		textPane.setCaret(caret);
		caret.setSelectionVisible(true);
		scrollPane.setViewportView(textPane);
	}
	
	/*----- Getters -----*/
	
	public IMMLLine getCurrentLine(){
		//Gets the line the caret is inside, if valid.
		if(lines.isEmpty()) return null;
		
		int docpos = textPane.getCaretPosition();
		if(docpos < 0) return null;
		
		int left = 0;
		int right = lines.size();
		int range = right - left;
		int search_idx = range / 2;
		
		while(range >= 1){
			IMMLLine line = lines.get(search_idx);
			int rel = line.positionIsInLine(docpos);
			
			if(rel == 0) return line;
			if(rel < 0){
				//Left
				right = search_idx;
			}
			else{
				//Right
				left = search_idx + 1;
			}
			
			range = right - left;
			search_idx = left + (range/2);
			if(search_idx >= right) search_idx = right - 1;
		}

		return null;
	}
	
	/*----- Setters -----*/
	
	public void resetDocument(){
		doc = new DefaultStyledDocument();
		textPane.setDocument(doc);
		scriptStyler = new MMLScriptStyleRules(doc, 11);
		
		textPane.repaint();
		scrollPane.repaint();
	}
	
	public boolean loadScript(NUSALSeq seq){
		resetDocument();
		try{
			if(seq != null){
				List<NUSALSeqCommand> cmdlist = seq.getAllCommands().getOrderedCommands();
				if(cmdlist != null){
					lines = new ArrayList<IMMLLine>(cmdlist.size() << 1);
					
					//Comment lines.
					addLine(new CommentLine(
							"; Nintendo 64 Music Macro Language", scriptStyler, doc.getLength()));
					
					addLine(new CommentLine(
							"; Generated by Zeqer64", scriptStyler, doc.getLength()));
					addLine(new CommentLine(
							"; Based upon documentation from SEQ64 [https://github.com/sauraen/seq64]", 
							scriptStyler, doc.getLength()));
					addLine(new EmptyLine(scriptStyler));
					
					linkingMap = new HashMap<String, RefInfo>();
					for(NUSALSeqCommand cmd : cmdlist) loadCommand(cmd);
					
					resolveReferences();
				}
				else{
					doc.insertString(0, "<No data>", scriptStyler.getBaseStyle());
				}
			}
			else{
				doc.insertString(0, "<No script loaded>", scriptStyler.getBaseStyle());
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
			return false;
		}
		
		textPane.setCaretPosition(0);
		
		textPane.repaint();
		scrollPane.repaint();
		return true;
	}
	
	public void clearJumpListeners(){
		jumpListeners.clear();
	}
	
	public void addJumpListener(ScriptJumpListener l){
		if(l != null){
			jumpListeners.add(l);
		}
	}
	
	public boolean jumpTo(int addr, boolean triggerCallbacks){
		if(addr < 0) return false;
		IMMLLine target = null;
		for(IMMLLine line : lines){
			int lineaddr = line.getBinAddress();
			if(lineaddr < 0) continue; //Ignore
			if(lineaddr > addr) break;
			target = line;
			
			if(lineaddr == addr) break;
		}
		if(target == null) return false;
		int pos = target.startPos;
		if(pos < 0) return false;
		
		textPane.setCaretPosition(doc.getLength());
		textPane.repaint();
		scrollPane.repaint();
		
		textPane.select(pos, target.endPos);
		//textPane.setCaretPosition(pos);
		if(triggerCallbacks) fireJumpCallbacks(addr);
		
		textPane.repaint();
		scrollPane.repaint();
		return true;
	}
	
	/*----- Internal -----*/
	
	private void addLine(IMMLLine line){
		lines.add(line); 
		line.writeToDocument(doc);
	}
	
	private void resolveReferences(){
		if(linkingMap == null) return;
		for(RefInfo ref : linkingMap.values()){
			if(ref.reference != null){
				for(CmdLineArg carg : ref.referees){
					carg.ref = ref.reference;
					carg.unresolvedRef = false;
				}
			}
		}
		
		linkingMap.clear();
		linkingMap = null;
	}
	
	private void loadCommand(NUSALSeqCommand cmd){
		//So can be recursive in case of chunks.
		if(cmd == null) return;
		if(cmd instanceof NUSALSeqCommandChunk){
			NUSALSeqCommandChunk ccmd = (NUSALSeqCommandChunk)cmd;
			List<NUSALSeqCommand> children = ccmd.getCommands();
			for(NUSALSeqCommand child : children) loadCommand(child);
		}
		else{
			String mylbl = cmd.getLabel();
			
			if(mylbl != null){
				//Label Line.
				IMMLLine lline = new LabelLine(mylbl, scriptStyler, doc.getLength());
				lline.binAddr = cmd.getAddress();
				addLine(lline);
				RefInfo ref = linkingMap.get(mylbl);
				if(ref == null){
					ref = new RefInfo();
					linkingMap.put(mylbl, ref);
					//ref.label = mylbl;
				}
				ref.reference = lline;
			}
			
			CmdLine myline = null;
			if(cmd instanceof NUSALSeqDataCommand){
				NUSALSeqDataCommand dcmd = (NUSALSeqDataCommand)cmd;
				myline = new DataLine(dcmd, scriptStyler, doc.getLength(), 1);
				addLine(myline);
			}
			else{
				myline = new CmdLine(cmd, scriptStyler, doc.getLength(), 1);
				addLine(myline);
			}
			
			//Go thru args to look for ref flags
			if(myline != null){
				List<CmdLineArg> cargs = myline.getArgs();
				for(CmdLineArg carg : cargs){
					if(carg.unresolvedRef){
						String lbl = carg.text;
						RefInfo ref = linkingMap.get(lbl);
						if(ref == null){
							ref = new RefInfo();
							linkingMap.put(lbl, ref);
							//ref.label = lbl;
						}
						ref.referees.add(carg);
					}
				}
			}
		}
	}
	
	/*----- Draw -----*/
	
	private static class SelectCaret extends DefaultCaret{
		//https://stackoverflow.com/questions/18237317/how-to-retain-selected-text-in-jtextfield-when-focus-lost
		private static final long serialVersionUID = -8574459446660429726L;
		
		private static final HighlightPainter PAINTER_FOCUS = new DefaultHighlighter.DefaultHighlightPainter(SELECT_COLOR_FOCUS);
		private static final HighlightPainter PAINTER_NO_FOCUS = new DefaultHighlighter.DefaultHighlightPainter(SELECT_COLOR_NO_FOCUS);
		
		private boolean isFocused = false;
		
		protected HighlightPainter getSelectionPainter() {
	        return isFocused ? PAINTER_FOCUS : PAINTER_NO_FOCUS;
	    }

	    public void setSelectionVisible(boolean hasFocus) {
	        if (hasFocus != isFocused) {
	            isFocused = hasFocus;
	            super.setSelectionVisible(false);
	            super.setSelectionVisible(true);
	        }
	    }
		
		
	}
	
	public void disable(){
		textPane.setEnabled(false);
		scrollPane.setEnabled(false);
		textPane.repaint();
		scrollPane.repaint();
	}
	
	public void enable(){
		textPane.setEnabled(true);
		scrollPane.setEnabled(true);
		textPane.repaint();
		scrollPane.repaint();
	}
	
	/*----- Callbacks -----*/
	
	private void fireJumpCallbacks(int addr){
		for(ScriptJumpListener l : jumpListeners){
			l.onAddressJump(addr);
		}
	}
	
	private void leftClickCallback(){
		//TODO
		//Checks caret position to determine multi-clicks.
		//This is for address jump listeners (ie. like hex view)
	}
	
	private void rightClickCallback(){
		//TODO
		//Used to jump to a label
	}

}