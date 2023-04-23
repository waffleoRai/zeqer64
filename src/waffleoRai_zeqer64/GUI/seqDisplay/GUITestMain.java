package waffleoRai_zeqer64.GUI.seqDisplay;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import waffleoRai_GUITools.GUITools;
import waffleoRai_SeqSound.n64al.NUSALSeq;
import waffleoRai_Utils.FileBuffer;

public class GUITestMain {

	private static void testMethod(JFrame frame){
		//TODO
		SeqDisplayForm form = null;
		if(frame instanceof SeqDisplayForm){
			form = (SeqDisplayForm)frame;
		}
		else System.exit(3);
		
		String testseq_path = "C:\\Users\\Blythe\\Documents\\Desktop\\out\\n64test\\seq_095.buseq";
		try {
			//NUSALSeq seq = new NUSALSeq(FileBuffer.createBuffer(testseq_path, true));
			NUSALSeq seq = NUSALSeq.readNUSALSeq(FileBuffer.createBuffer(testseq_path, true));
			form.loadSequence(seq);
		} 
		catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(1);
		}
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
