package waffleoRai_zeqer64.GUI;


import javax.swing.JFrame;
import javax.swing.JPanel;

public class ZeqerGUIUtils {
	
	public static JFrame genPanelStandalone(JFrame parent, JPanel pnl){
		JFrame newFrame = new JFrame();
		newFrame.add(pnl);
		newFrame.pack();
		
		return newFrame;
	}

}
