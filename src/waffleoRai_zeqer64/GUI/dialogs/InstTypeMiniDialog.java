package waffleoRai_zeqer64.GUI.dialogs;

import javax.swing.JDialog;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;

public class InstTypeMiniDialog extends JDialog {

	private static final long serialVersionUID = 1541321101116617262L;
	
	public static final int STD_WIDTH = 420;
	public static final int STD_HEIGHT = 190;
	
	public static final int SELECTION_INST = 0;
	public static final int SELECTION_PERC = 1;
	public static final int SELECTION_DRUM = 2;
	public static final int SELECTION_NONE = -1;
	
	private int selection = SELECTION_NONE;
	
	public InstTypeMiniDialog(Frame parent){
		super(parent, true);
		setResizable(false);
		setMinimumSize(new Dimension(STD_WIDTH, STD_HEIGHT));
		setPreferredSize(new Dimension(STD_WIDTH, STD_HEIGHT));
		
		setTitle("Instrument Type");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 40, 40, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JLabel lblWhatTypeOf = new JLabel("What type of instrument would you like to create?");
		lblWhatTypeOf.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_lblWhatTypeOf = new GridBagConstraints();
		gbc_lblWhatTypeOf.gridwidth = 3;
		gbc_lblWhatTypeOf.insets = new Insets(10, 10, 10, 10);
		gbc_lblWhatTypeOf.gridx = 0;
		gbc_lblWhatTypeOf.gridy = 0;
		getContentPane().add(lblWhatTypeOf, gbc_lblWhatTypeOf);
		
		JButton btnInst = new JButton("Standard Instrument");
		btnInst.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_btnInst = new GridBagConstraints();
		gbc_btnInst.fill = GridBagConstraints.BOTH;
		gbc_btnInst.insets = new Insets(0, 5, 5, 5);
		gbc_btnInst.gridx = 0;
		gbc_btnInst.gridy = 1;
		getContentPane().add(btnInst, gbc_btnInst);
		btnInst.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				selection = SELECTION_INST;
				closeMe();
			}});
		
		JButton btnPerc = new JButton("Percussion Set");
		btnPerc.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_btnPerc = new GridBagConstraints();
		gbc_btnPerc.fill = GridBagConstraints.BOTH;
		gbc_btnPerc.insets = new Insets(0, 0, 5, 5);
		gbc_btnPerc.gridx = 1;
		gbc_btnPerc.gridy = 1;
		getContentPane().add(btnPerc, gbc_btnPerc);
		btnPerc.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				selection = SELECTION_PERC;
				closeMe();
			}});
		
		JButton btnSingleDrum = new JButton("Single Drum");
		btnSingleDrum.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_btnSingleDrum = new GridBagConstraints();
		gbc_btnSingleDrum.anchor = GridBagConstraints.WEST;
		gbc_btnSingleDrum.fill = GridBagConstraints.BOTH;
		gbc_btnSingleDrum.insets = new Insets(0, 0, 5, 5);
		gbc_btnSingleDrum.gridx = 2;
		gbc_btnSingleDrum.gridy = 1;
		getContentPane().add(btnSingleDrum, gbc_btnSingleDrum);
		btnSingleDrum.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				selection = SELECTION_DRUM;
				closeMe();
			}});
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(0, 0, 5, 5);
		gbc_btnCancel.fill = GridBagConstraints.BOTH;
		gbc_btnCancel.gridx = 1;
		gbc_btnCancel.gridy = 2;
		getContentPane().add(btnCancel, gbc_btnCancel);
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				selection = SELECTION_NONE;
				closeMe();
			}});
	}
	
	public int getSelection(){return this.selection;}
	
	public void closeMe(){
		this.setVisible(false);
		this.dispose();
	}
	
	public static int showDialog(Frame parent){
		InstTypeMiniDialog dialog = new InstTypeMiniDialog(parent);
		dialog.setVisible(true);
		return dialog.getSelection();
	}

}
