package waffleoRai_zeqer64.GUI.dialogs;

import javax.swing.JFrame;

import waffleoRai_GUITools.WRDialog;
import java.awt.GridBagLayout;
import javax.swing.JPanel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import javax.swing.JComboBox;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;

public class ExportTypeDialog extends WRDialog{

	private static final long serialVersionUID = 5882740516611578015L;
	
	public static final int WIDTH = 200;
	public static final int HEIGHT = 140;
	
	private boolean selectionOkay = false;
	
	private JButton btnOkay;
	private JComboBox<String> cmbxOptions;
	private List<String> options;
	
	public ExportTypeDialog(JFrame parent) {
		super(parent, true);
		options = new LinkedList<String>();
		initGUI();
	}
	
	private void initGUI() {
		setResizable(false);
		setTitle("Select Export Format");
		super.setMinimumSize(new Dimension(WIDTH, HEIGHT));
		super.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		cmbxOptions = new JComboBox<String>();
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(5, 10, 5, 10);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 0;
		gbc_comboBox.gridy = 0;
		getContentPane().add(cmbxOptions, gbc_comboBox);
		cmbxOptions.setEnabled(false);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		getContentPane().add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{197, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		btnOkay = new JButton("Okay");
		GridBagConstraints gbc_btnOkay = new GridBagConstraints();
		gbc_btnOkay.insets = new Insets(0, 0, 5, 5);
		gbc_btnOkay.gridx = 1;
		gbc_btnOkay.gridy = 0;
		panel.add(btnOkay, gbc_btnOkay);
		btnOkay.setEnabled(false);
		btnOkay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnOkayCallback();
			}
		});
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(0, 0, 5, 5);
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 0;
		panel.add(btnCancel, gbc_btnCancel);
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnCancelCallback();
			}
		});
	}
	
	public boolean getSelectionOkay() {return selectionOkay;}
	
	public int getSelection() {
		return cmbxOptions.getSelectedIndex();
	}
	
	public int addOption(String op) {
		options.add(op);
		
		DefaultComboBoxModel<String> mdl = new DefaultComboBoxModel<String>();
		for(String s : options) mdl.addElement(s);
		cmbxOptions.setModel(mdl);
		
		btnOkay.setEnabled(true);
		btnOkay.repaint();
		cmbxOptions.setEnabled(true);
		cmbxOptions.repaint();
		return options.size()-1;
	}
	
	private void btnOkayCallback() {
		selectionOkay = true;
		closeMe();
	}
	
	private void btnCancelCallback() {
		selectionOkay = false;
		closeMe();
	}

}
