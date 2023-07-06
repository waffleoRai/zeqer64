package waffleoRai_zeqer64.GUI;


import javax.swing.JFrame;
import javax.swing.JPanel;

public class ZeqerGUIUtils {
	
	public static final String STRKEY_FILPNL_FLAGS = "PNLFLTER_TITLE_FLAGS";
	public static final String STRKEY_FILPNL_TAGS = "PNLFLTER_TITLE_TAGS";
	public static final String STRKEY_FILPNL_ADDOP = "PNLFLTER_TITLE_ADDOP";
	
	public static JFrame genPanelStandalone(JFrame parent, JPanel pnl){
		JFrame newFrame = new JFrame();
		newFrame.add(pnl);
		newFrame.pack();
		
		return newFrame;
	}

}
