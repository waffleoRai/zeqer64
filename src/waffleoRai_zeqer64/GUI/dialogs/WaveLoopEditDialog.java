package waffleoRai_zeqer64.GUI.dialogs;

import javax.swing.JFrame;

import waffleoRai_GUITools.RadioButtonGroup;
import waffleoRai_GUITools.WRDialog;
import java.awt.GridBagLayout;
import javax.swing.JPanel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.border.EtchedBorder;

public class WaveLoopEditDialog extends WRDialog{

	private static final long serialVersionUID = 8782594470797675016L;
	
	private static final int MIN_WIDTH = 280;
	private static final int MIN_HEIGHT = 280;
	
	public static final int LOOP_COUNT_ONESHOT = 0;
	public static final int LOOP_COUNT_INFINITE = 1;
	public static final int LOOP_COUNT_FINITE = 2;
	
	/*----- Instance Variables -----*/
	
	private boolean exitSelection = false;
	
	private RadioButtonGroup rbgLoopCount;
	
	private JTextField txtStart;
	private JTextField txtEnd;
	private JTextField txtLoopCount;
	
	/*----- Init -----*/
	
	public WaveLoopEditDialog(JFrame parentFrame) {
		super(parentFrame, true);
		rbgLoopCount = new RadioButtonGroup(3);
		initGUI();
		rbgLoopCount.select(LOOP_COUNT_ONESHOT);
	}
	
	private void initGUI() {
		setTitle("Edit Wave Loop");
		setResizable(false);
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 0;
		getContentPane().add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_panel_1.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_panel_1.columnWeights = new double[]{1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{1.0, 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);
		
		JLabel lblNewLabel = new JLabel("Start Sample:");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.gridx = 1;
		gbc_lblNewLabel.gridy = 1;
		panel_1.add(lblNewLabel, gbc_lblNewLabel);
		
		txtStart = new JTextField();
		GridBagConstraints gbc_txtStart = new GridBagConstraints();
		gbc_txtStart.insets = new Insets(0, 0, 5, 5);
		gbc_txtStart.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtStart.gridx = 2;
		gbc_txtStart.gridy = 1;
		panel_1.add(txtStart, gbc_txtStart);
		txtStart.setColumns(10);
		globalEnable.addComponent("txtStart", txtStart);
		
		JLabel lblNewLabel_1 = new JLabel("End Sample:");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.gridx = 1;
		gbc_lblNewLabel_1.gridy = 2;
		panel_1.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		txtEnd = new JTextField();
		GridBagConstraints gbc_txtEnd = new GridBagConstraints();
		gbc_txtEnd.insets = new Insets(0, 0, 5, 5);
		gbc_txtEnd.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtEnd.gridx = 2;
		gbc_txtEnd.gridy = 2;
		panel_1.add(txtEnd, gbc_txtEnd);
		txtEnd.setColumns(10);
		globalEnable.addComponent("txtEnd", txtEnd);
		
		JLabel lblLoopCount = new JLabel("Loop Count:");
		GridBagConstraints gbc_lblLoopCount = new GridBagConstraints();
		gbc_lblLoopCount.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblLoopCount.insets = new Insets(5, 0, 5, 5);
		gbc_lblLoopCount.gridx = 1;
		gbc_lblLoopCount.gridy = 3;
		panel_1.add(lblLoopCount, gbc_lblLoopCount);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.insets = new Insets(0, 0, 5, 5);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 2;
		gbc_panel_2.gridy = 3;
		panel_1.add(panel_2, gbc_panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[]{0, 0, 0};
		gbl_panel_2.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_panel_2.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel_2.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		panel_2.setLayout(gbl_panel_2);
		
		JRadioButton rbOneShot = new JRadioButton("One Shot");
		GridBagConstraints gbc_rbOneShot = new GridBagConstraints();
		gbc_rbOneShot.anchor = GridBagConstraints.WEST;
		gbc_rbOneShot.insets = new Insets(5, 5, 5, 5);
		gbc_rbOneShot.gridx = 0;
		gbc_rbOneShot.gridy = 0;
		panel_2.add(rbOneShot, gbc_rbOneShot);
		globalEnable.addComponent("rbOneShot", rbOneShot);
		rbgLoopCount.addButton(rbOneShot, LOOP_COUNT_ONESHOT);
		rbOneShot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rbSelectCallback(LOOP_COUNT_ONESHOT);
			}
		});
		
		JRadioButton rbInfinite = new JRadioButton("Infinite");
		GridBagConstraints gbc_rbInfinite = new GridBagConstraints();
		gbc_rbInfinite.anchor = GridBagConstraints.WEST;
		gbc_rbInfinite.insets = new Insets(0, 5, 5, 5);
		gbc_rbInfinite.gridx = 0;
		gbc_rbInfinite.gridy = 1;
		panel_2.add(rbInfinite, gbc_rbInfinite);
		globalEnable.addComponent("rbInfinite", rbInfinite);
		rbgLoopCount.addButton(rbInfinite, LOOP_COUNT_INFINITE);
		rbInfinite.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rbSelectCallback(LOOP_COUNT_INFINITE);
			}
		});
		
		JRadioButton rbFinite = new JRadioButton("Finite");
		GridBagConstraints gbc_rbFinite = new GridBagConstraints();
		gbc_rbFinite.anchor = GridBagConstraints.WEST;
		gbc_rbFinite.insets = new Insets(0, 5, 5, 5);
		gbc_rbFinite.gridx = 0;
		gbc_rbFinite.gridy = 2;
		panel_2.add(rbFinite, gbc_rbFinite);
		globalEnable.addComponent("rbFinite", rbFinite);
		rbgLoopCount.addButton(rbFinite, LOOP_COUNT_FINITE);
		rbFinite.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rbSelectCallback(LOOP_COUNT_FINITE);
			}
		});
		
		txtLoopCount = new JTextField();
		GridBagConstraints gbc_txtLoopCount = new GridBagConstraints();
		gbc_txtLoopCount.anchor = GridBagConstraints.WEST;
		gbc_txtLoopCount.insets = new Insets(0, 5, 5, 5);
		gbc_txtLoopCount.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtLoopCount.gridx = 0;
		gbc_txtLoopCount.gridy = 3;
		panel_2.add(txtLoopCount, gbc_txtLoopCount);
		txtLoopCount.setColumns(10);
		globalEnable.addComponent("txtLoopCount", txtLoopCount);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		getContentPane().add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JButton btnOkay = new JButton("Okay");
		GridBagConstraints gbc_btnOkay = new GridBagConstraints();
		gbc_btnOkay.insets = new Insets(5, 5, 5, 5);
		gbc_btnOkay.gridx = 1;
		gbc_btnOkay.gridy = 0;
		panel.add(btnOkay, gbc_btnOkay);
		globalEnable.addComponent("btnOkay", btnOkay);
		btnOkay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnOkayCallback();
			}
		});
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(5, 5, 5, 5);
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 0;
		panel.add(btnCancel, gbc_btnCancel);
		globalEnable.addComponent("btnCancel", btnCancel);
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnCancelCallback();
			}
		});
		
	}
	
	/*----- Getters -----*/
	
	public boolean getExitSelection() {return exitSelection;}
	
	public int getLoopStart() {
		String str = txtStart.getText();
		str = str.trim();
		if(str == null) return -1;
		if(str.isEmpty()) return -1;
		try {
			return Integer.parseInt(str);
		}
		catch(NumberFormatException ex) {return -1;}
	}
	
	public int getLoopEnd() {
		String str = txtEnd.getText();
		str = str.trim();
		if(str == null) return -1;
		if(str.isEmpty()) return -1;
		try {
			return Integer.parseInt(str);
		}
		catch(NumberFormatException ex) {return -1;}
	}
	
	public int getLoopCount() {
		String str = txtLoopCount.getText();
		str = str.trim();
		if(str == null) return -1;
		if(str.isEmpty()) return -1;
		try {
			return Integer.parseInt(str);
		}
		catch(NumberFormatException ex) {return -1;}
	}
	
	public int getLoopType() {return rbgLoopCount.getSelectedIndex();}
	
	/*----- Setters -----*/
	
	public void setLoopStart(int val) {
		txtStart.setText(Integer.toString(val));
	}
	
	public void setLoopEnd(int val) {
		txtEnd.setText(Integer.toString(val));
	}
	
	public void setLoopCount(int val) {
		txtLoopCount.setText(Integer.toString(val));
	}
	
	public void setLoopType(int val) {
		rbgLoopCount.select(val);
		reenable();
	}
	
	/*----- GUI -----*/
	
	public void reenable() {
		globalEnable.setEnabling(true);
		txtLoopCount.setEnabled(rbgLoopCount.getSelectedIndex() == LOOP_COUNT_FINITE);
		globalEnable.repaint();
	}
	
	/*----- Callbacks -----*/
	
	private void btnOkayCallback() {
		exitSelection = true;
		closeMe();
	}
	
	private void btnCancelCallback() {
		exitSelection = false;
		closeMe();
	}
	
	private void rbSelectCallback(int index) {
		rbgLoopCount.select(index);
		reenable();
	}

}
