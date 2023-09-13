package waffleoRai_zeqer64.GUI;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_zeqer64.GUI.ZeqerGUIUtils.CacheType;

public class CacheTypeCombobox extends JComboBox<CacheType>{
	
	private static final long serialVersionUID = 5972555799851830140L;
	
	public static final int[] CACHE_CMBX_IDXS = {2, 1, 0, 3, 4};
	
	public CacheTypeCombobox(){
		//Populate
		DefaultComboBoxModel<CacheType> mdl = new DefaultComboBoxModel<CacheType>();
		mdl.addElement(CacheType.TEMPORARY);
		mdl.addElement(CacheType.PERSISTENT);
		mdl.addElement(CacheType.PERMANENT);
		mdl.addElement(CacheType.ANY);
		mdl.addElement(CacheType.ANY_NOSYNC);
		setModel(mdl);
		setRawValue(Z64Sound.CACHE_TEMPORARY);
	}
	
	public int getRawValue(){
		int selidx = getSelectedIndex();
		if(selidx < 0) return -1;
		CacheType sel = getItemAt(selidx);
		if(sel == null) return -1;
		return sel.getValue();
	}
	
	public void setRawValue(int cacheType){
		if((cacheType >= 0) && (cacheType < CACHE_CMBX_IDXS.length)){
			this.setSelectedIndex(CACHE_CMBX_IDXS[cacheType]);
		}
		else this.setSelectedIndex(-1);
	}

}
