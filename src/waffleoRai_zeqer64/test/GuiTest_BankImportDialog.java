package waffleoRai_zeqer64.test;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import waffleoRai_zeqer64.ErrorCode;
import waffleoRai_zeqer64.GUI.dialogs.BankImportDialog;
import waffleoRai_zeqer64.bankImport.BankImporter;
import waffleoRai_zeqer64.filefmt.bank.ZeqerBankIO;

public class GuiTest_BankImportDialog {

	public static void main(String[] args) {
		String inpath = args[0];
		
		try {
			SwingUtilities.invokeLater(new Runnable(){
	            public void run(){
	            	ErrorCode err = new ErrorCode();
	            	System.err.println("Loading " + inpath);
	            	BankImporter imp = ZeqerBankIO.initializeImport(inpath, err);
					if(imp == null || err.value != ZeqerBankIO.ERR_NONE){
						System.err.println("Error caught! " + ZeqerBankIO.getErrorCodeString(err.value));
						System.err.println("Now exiting...");
					}
					
					JFrame dummyParent = new JFrame();
					dummyParent.addWindowListener(new WindowAdapter(){
			    		public void windowClosing(WindowEvent e){
			    			dummyParent.dispose();
			    			System.exit(0);
						}});
					
					BankImportDialog dialog = new BankImportDialog(dummyParent);
					dialog.loadInfoToGUI(imp.getInfo());
					
					dummyParent.setVisible(true);
					dialog.showMe(dummyParent);
					
					
	            }
	        });
		}
		catch(Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}

	}

}
