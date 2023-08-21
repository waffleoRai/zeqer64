package waffleoRai_zeqer64.GUI.dialogs;

import javax.swing.JDialog;

import waffleoRai_zeqer64.GUI.smallElements.ReleaseEditPanel;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;

import javax.swing.JPanel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

public class StandaloneReleaseEditDialog extends JDialog{

	/*--- Constants ---*/
	
	private static final long serialVersionUID = -3912769828610348273L;
	
	public static final int MIN_WIDTH = ReleaseEditPanel.MIN_WIDTH + 10;
	public static final int MIN_HEIGHT = ReleaseEditPanel.MIN_HEIGHT + 55;
	
	/*----- Instance Variables -----*/
	
	private ReleaseEditPanel pnlRel = null;
	
	private ZeqerCoreInterface core;
	private boolean exitSelection = false;
	
	/*----- Init -----*/
	
	public StandaloneReleaseEditDialog(Frame parent_frame, ZeqerCoreInterface core_link){
		super(parent_frame, true);
		core = core_link;
		initGUI();
	}
	
	private void initGUI(){
		setTitle("Edit Release");
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		setResizable(false);
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		pnlRel = new ReleaseEditPanel(core);
		GridBagConstraints gbc_pnlRel = new GridBagConstraints();
		gbc_pnlRel.fill = GridBagConstraints.BOTH;
		gbc_pnlRel.gridx = 0;
		gbc_pnlRel.gridy = 0;
		getContentPane().add(pnlRel, gbc_pnlRel);
		
		JPanel pnlButtons = new JPanel();
		GridBagConstraints gbc_pnlButtons = new GridBagConstraints();
		gbc_pnlButtons.fill = GridBagConstraints.BOTH;
		gbc_pnlButtons.gridx = 0;
		gbc_pnlButtons.gridy = 1;
		getContentPane().add(pnlButtons, gbc_pnlButtons);
		GridBagLayout gbl_pnlButtons = new GridBagLayout();
		gbl_pnlButtons.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlButtons.rowHeights = new int[]{0, 0};
		gbl_pnlButtons.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlButtons.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlButtons.setLayout(gbl_pnlButtons);
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.anchor = GridBagConstraints.NORTH;
		gbc_btnCancel.insets = new Insets(5, 0, 5, 5);
		gbc_btnCancel.gridx = 1;
		gbc_btnCancel.gridy = 0;
		pnlButtons.add(btnCancel, gbc_btnCancel);
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnCancelCallback();
			}});
		
		JButton btnOkay = new JButton("Okay");
		GridBagConstraints gbc_btnOkay = new GridBagConstraints();
		gbc_btnOkay.insets = new Insets(5, 0, 5, 10);
		gbc_btnOkay.gridx = 2;
		gbc_btnOkay.gridy = 0;
		pnlButtons.add(btnOkay, gbc_btnOkay);
		btnOkay.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnOkayCallback();
			}});
	}
	
	/*----- Getters -----*/
	
	public boolean getExitSelection(){return exitSelection;}
	
	public int getReleaseValue(){
		return pnlRel.getCurrentReleaseValue();
	}
	
	/*----- Setters -----*/
	
	public void setReleaseValue(int value){
		pnlRel.setCurrentReleaseVal(value);
		pnlRel.repaint();
	}
	
	/*----- Drawing -----*/
	
	public void showMe(Component c){
		setLocationRelativeTo(c);
		pack();
		setVisible(true);
	}
	
	public void closeMe(){
		setVisible(false);
		dispose();
	}
	
	/*----- Callbacks -----*/
	
	private void btnOkayCallback(){
		exitSelection = true;
		closeMe();
	}
	
	private void btnCancelCallback(){
		exitSelection = false;
		closeMe();
	}

}
