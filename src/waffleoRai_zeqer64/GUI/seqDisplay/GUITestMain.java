package waffleoRai_zeqer64.GUI.seqDisplay;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import waffleoRai_GUITools.GUITools;

public class GUITestMain {

	private static void testMethod(JFrame frame){
		//TODO
		SeqDisplayForm form = null;
		if(frame instanceof SeqDisplayForm){
			form = (SeqDisplayForm)frame;
		}
		else System.exit(3);
		
		String testseq_path = "C:\\Users\\Blythe\\Documents\\Desktop\\out\\n64test\\seq_095.buseq";
		
	}
	
	private static JFrame gimmeFrame(){
		SeqDisplayForm f = new SeqDisplayForm();
		f.setLocation(GUITools.getScreenCenteringCoordinates(f));
		return f;
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() 
        {
            public void run() 
            {
            	JFrame frame = gimmeFrame();
            	if(frame == null) System.exit(2);
            	frame.addWindowListener(new WindowAdapter(){
            		public void windowClosing(WindowEvent e) {
            			System.exit(0);
					}
            	});
            	frame.pack();
        		frame.setVisible(true);
            	testMethod(frame);
            }
        });
	}

}
