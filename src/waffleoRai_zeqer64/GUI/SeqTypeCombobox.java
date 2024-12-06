package waffleoRai_zeqer64.GUI;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import waffleoRai_zeqer64.ZeqerSeq;

public class SeqTypeCombobox extends JComboBox<String>{

	private static final long serialVersionUID = 1161631751995670478L;
	
	public static final int[] SEQ_CMBX_IDXS = {-1, 0, 6, 7, 1, 2, 3, 4, 5, -1};
	private static int[] REV_MAP = null;
	
	private static final String[] TYPE_STRINGS = {null, "Background Music (BGM)",
			"Jingle/Fanfare", "Ocarina Tune", "Cutscene Script", "Ambience Program",
			"SFX Program", "BGM Program", "BGM Program Element", null};
	
	public SeqTypeCombobox(){
		//Populate
		DefaultComboBoxModel<String> mdl = new DefaultComboBoxModel<String>();
		mdl.addElement(TYPE_STRINGS[ZeqerSeq.SEQTYPE_BGM]); //0
		mdl.addElement(TYPE_STRINGS[ZeqerSeq.SEQTYPE_JINGLE]); //1
		mdl.addElement(TYPE_STRINGS[ZeqerSeq.SEQTYPE_OCARINA]); //2
		mdl.addElement(TYPE_STRINGS[ZeqerSeq.SEQTYPE_CUTSCENE]); //3
		mdl.addElement(TYPE_STRINGS[ZeqerSeq.SEQTYPE_AMBIENT]); //4
		mdl.addElement(TYPE_STRINGS[ZeqerSeq.SEQTYPE_SFX]); //5
		mdl.addElement(TYPE_STRINGS[ZeqerSeq.SEQTYPE_BGM_PROG]); //6
		mdl.addElement(TYPE_STRINGS[ZeqerSeq.SEQTYPE_BGM_PIECE]); //7
		setModel(mdl);
		generateRevMap();
		
		setRawValue(ZeqerSeq.SEQTYPE_BGM);
	}
	
	public int getRawValue(){
		int selidx = getSelectedIndex();
		if(selidx < 0) return -1;
		return REV_MAP[selidx];
	}
	
	public void setRawValue(int seqType){
		if(seqType < 0 || seqType >= SEQ_CMBX_IDXS.length) {
			this.setSelectedIndex(-1);
			return;
		}
		setSelectedIndex(SEQ_CMBX_IDXS[seqType]);
	}
	
	private static void generateRevMap() {
		if(REV_MAP != null) return;
		int max = 0;
		for(int i = 0; i < SEQ_CMBX_IDXS.length; i++) {
			if(SEQ_CMBX_IDXS[i] > max) {
				max = SEQ_CMBX_IDXS[i];
			}
		}
		
		REV_MAP = new int[max+1];
		for(int i = 0; i < SEQ_CMBX_IDXS.length; i++) {
			if(SEQ_CMBX_IDXS[i] < 0) continue;
			REV_MAP[SEQ_CMBX_IDXS[i]] = i;
		}
	}

}
