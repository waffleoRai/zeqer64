package waffleoRai_zeqer64.GUI.dialogs;

import javax.swing.JDialog;
import javax.swing.JFrame;
import java.awt.GridBagLayout;
import javax.swing.JPanel;

import waffleoRai_GUITools.GUITools;
import waffleoRai_zeqer64.ZeqerBank;
import waffleoRai_zeqer64.GUI.ZeqerPanelBanks;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

public class BankPickDialog extends JDialog{

	private static final long serialVersionUID = 6273615659816081842L;
	
	private JFrame parent;
	private ZeqerCoreInterface core;
	
	private boolean exitSelection = false;
	
	private ZeqerPanelBanks pnlBanks;
	
	public BankPickDialog(JFrame frame, ZeqerCoreInterface core_iface){
		super(frame, true);
		parent = frame;
		core = core_iface;
		
		initGUI();
	}
	
	private void initGUI(){
		setTitle("Pick Bank");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		pnlBanks = new ZeqerPanelBanks(parent, core, true);
		GridBagConstraints gbc_pnlBanks = new GridBagConstraints();
		gbc_pnlBanks.insets = new Insets(5, 5, 5, 5);
		gbc_pnlBanks.fill = GridBagConstraints.BOTH;
		gbc_pnlBanks.gridx = 0;
		gbc_pnlBanks.gridy = 0;
		getContentPane().add(pnlBanks, gbc_pnlBanks);
		
		JPanel pnlBtn = new JPanel();
		GridBagConstraints gbc_pnlBtn = new GridBagConstraints();
		gbc_pnlBtn.insets = new Insets(0, 5, 5, 5);
		gbc_pnlBtn.fill = GridBagConstraints.BOTH;
		gbc_pnlBtn.gridx = 0;
		gbc_pnlBtn.gridy = 1;
		getContentPane().add(pnlBtn, gbc_pnlBtn);
		GridBagLayout gbl_pnlBtn = new GridBagLayout();
		gbl_pnlBtn.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlBtn.rowHeights = new int[]{0, 0};
		gbl_pnlBtn.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlBtn.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlBtn.setLayout(gbl_pnlBtn);
		
		JButton btnOkay = new JButton("Okay");
		GridBagConstraints gbc_btnOkay = new GridBagConstraints();
		gbc_btnOkay.insets = new Insets(0, 0, 0, 5);
		gbc_btnOkay.gridx = 1;
		gbc_btnOkay.gridy = 0;
		pnlBtn.add(btnOkay, gbc_btnOkay);
		btnOkay.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnOkayCallback();
			}});
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 0;
		pnlBtn.add(btnCancel, gbc_btnCancel);
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnCloseCallback();
			}});
	}
	
	public ZeqerBank getSelectedBank(){
		return pnlBanks.getSelectedBank();
	}
	
	public boolean getExitSelection(){return exitSelection;}
	
	public void showMe(Component c){
		if(c != null) setLocationRelativeTo(c);
		else{
			if(parent != null) setLocationRelativeTo(parent);
			else{
				setLocation(GUITools.getScreenCenteringCoordinates(this));
			}
		}
		
		pack();
		setVisible(true);
	}
	
	public void closeMe(){
		setVisible(false);
		dispose();
	}

	private void btnOkayCallback(){
		exitSelection = true;
		closeMe();
	}
	
	private void btnCloseCallback(){
		exitSelection = false;
		closeMe();
	}
	
}
