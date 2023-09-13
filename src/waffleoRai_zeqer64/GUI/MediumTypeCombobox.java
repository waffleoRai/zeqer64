package waffleoRai_zeqer64.GUI;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_zeqer64.GUI.ZeqerGUIUtils.MediumType;

public class MediumTypeCombobox extends JComboBox<MediumType>{

	private static final long serialVersionUID = -3754695058946348606L;

	public static final int[] MEDIUM_CMBX_IDXS = {0, -1, 1, 2};
	
	public MediumTypeCombobox(){
		//Populate
		DefaultComboBoxModel<MediumType> mdl = new DefaultComboBoxModel<MediumType>();
		mdl.addElement(MediumType.RAM);
		mdl.addElement(MediumType.CART);
		mdl.addElement(MediumType.DD);
		setModel(mdl);
		setRawValue(Z64Sound.MEDIUM_CART);
	}
	
	public int getRawValue(){
		int selidx = getSelectedIndex();
		if(selidx < 0) return -1;
		MediumType sel = getItemAt(selidx);
		if(sel == null) return -1;
		return sel.getValue();
	}
	
	public void setRawValue(int medType){
		if((medType >= 0) && (medType < MEDIUM_CMBX_IDXS.length)){
			this.setSelectedIndex(MEDIUM_CMBX_IDXS[medType]);
		}
		else this.setSelectedIndex(-1);
	}
	
}
