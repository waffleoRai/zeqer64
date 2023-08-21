package waffleoRai_zeqer64.GUI.seqedit;

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.GUITools;
import waffleoRai_GUITools.WRFrame;
import waffleoRai_zeqer64.ZeqerBank;
import waffleoRai_zeqer64.GUI.dialogs.BankPickDialog;
import waffleoRai_zeqer64.GUI.dialogs.ZeqerSeqHubDialog;
import waffleoRai_zeqer64.filefmt.ZeqerBankTable.BankTableEntry;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;

import java.awt.Color;
import java.awt.Component;

public class SeqSoundfontEditDialog extends WRFrame{

	private static final long serialVersionUID = -4716550293228170639L;
	
	public static final int MIN_WIDTH  = 300;
	public static final int MIN_HEIGHT  = 450;
	
	private static final int SLOT_IDX_SF1 = 0;
	private static final int SLOT_IDX_SF2 = 1;
	private static final int SLOT_IDX_SF3 = 2;
	private static final int SLOT_IDX_SF4 = 3;
	
	/*--- Instance Variables ---*/
	
	private ZeqerSeqHubDialog parent;
	private ZeqerCoreInterface core;
	
	private boolean editable = true;
	private boolean changesApplied = false;
	
	private BankTableEntry[] banks;
	private BankTableEntry[] applBanks;
	
	private ComponentGroup globalEnable;
	private ComponentGroup editableEnable;
	private ComponentGroup cg2;
	private ComponentGroup cg3;
	private ComponentGroup cg4;
	
	private JTextField txtSF1;
	private JTextField txtSF2;
	private JTextField txtSF3;
	private JTextField txtSF4;
	
	private JCheckBox cbSF2;
	private JCheckBox cbSF3;
	private JCheckBox cbSF4;
	
	/*--- Initialization ---*/
	
	public SeqSoundfontEditDialog(ZeqerSeqHubDialog parentDialog, ZeqerCoreInterface core_iface, boolean readonly){
		parent = parentDialog;
		core = core_iface;
		editable = !readonly;
		
		globalEnable = new ComponentGroup();
		editableEnable = globalEnable.newChild();
		cg2 = globalEnable.newChild();
		cg3 = globalEnable.newChild();
		cg4 = globalEnable.newChild();
		
		banks = new BankTableEntry[4];
		applBanks = new BankTableEntry[4];
		
		initGUI();
		reenable();
	}
	
	private void initGUI(){
		setResizable(false);
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		
		setTitle("Manage Soundfonts");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel pnlSF1 = new JPanel();
		pnlSF1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlSF1 = new GridBagConstraints();
		gbc_pnlSF1.insets = new Insets(5, 5, 5, 5);
		gbc_pnlSF1.fill = GridBagConstraints.BOTH;
		gbc_pnlSF1.gridx = 0;
		gbc_pnlSF1.gridy = 0;
		getContentPane().add(pnlSF1, gbc_pnlSF1);
		GridBagLayout gbl_pnlSF1 = new GridBagLayout();
		gbl_pnlSF1.columnWidths = new int[]{0, 0};
		gbl_pnlSF1.rowHeights = new int[]{24, 0, 0, 0};
		gbl_pnlSF1.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlSF1.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlSF1.setLayout(gbl_pnlSF1);
		
		JLabel lblSoundfontA = new JLabel("Soundfont A");
		lblSoundfontA.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblSoundfontA = new GridBagConstraints();
		gbc_lblSoundfontA.insets = new Insets(5, 0, 5, 0);
		gbc_lblSoundfontA.gridx = 0;
		gbc_lblSoundfontA.gridy = 0;
		pnlSF1.add(lblSoundfontA, gbc_lblSoundfontA);
		
		txtSF1 = new JTextField();
		txtSF1.setBackground(new Color(255, 255, 255));
		txtSF1.setEditable(false);
		GridBagConstraints gbc_txtSF1 = new GridBagConstraints();
		gbc_txtSF1.insets = new Insets(0, 5, 5, 5);
		gbc_txtSF1.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtSF1.gridx = 0;
		gbc_txtSF1.gridy = 1;
		pnlSF1.add(txtSF1, gbc_txtSF1);
		txtSF1.setColumns(10);
		editableEnable.addComponent("txtSF1", txtSF1);
		
		JButton btnSet1 = new JButton("Set...");
		GridBagConstraints gbc_btnSet1 = new GridBagConstraints();
		gbc_btnSet1.insets = new Insets(0, 5, 5, 0);
		gbc_btnSet1.anchor = GridBagConstraints.WEST;
		gbc_btnSet1.gridx = 0;
		gbc_btnSet1.gridy = 2;
		pnlSF1.add(btnSet1, gbc_btnSet1);
		editableEnable.addComponent("btnSet1", btnSet1);
		btnSet1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnSetCallback(SLOT_IDX_SF1);
			}});
		
		JPanel pnlSF2 = new JPanel();
		pnlSF2.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlSF2 = new GridBagConstraints();
		gbc_pnlSF2.insets = new Insets(0, 5, 5, 5);
		gbc_pnlSF2.fill = GridBagConstraints.BOTH;
		gbc_pnlSF2.gridx = 0;
		gbc_pnlSF2.gridy = 1;
		getContentPane().add(pnlSF2, gbc_pnlSF2);
		GridBagLayout gbl_pnlSF2 = new GridBagLayout();
		gbl_pnlSF2.columnWidths = new int[]{0, 75, 0};
		gbl_pnlSF2.rowHeights = new int[]{0, 0, 0, 0};
		gbl_pnlSF2.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlSF2.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlSF2.setLayout(gbl_pnlSF2);
		
		JLabel lblSoundfont = new JLabel("Soundfont B");
		lblSoundfont.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblSoundfont = new GridBagConstraints();
		gbc_lblSoundfont.insets = new Insets(5, 0, 5, 5);
		gbc_lblSoundfont.gridx = 0;
		gbc_lblSoundfont.gridy = 0;
		pnlSF2.add(lblSoundfont, gbc_lblSoundfont);
		
		cbSF2 = new JCheckBox("Include");
		GridBagConstraints gbc_cbSF2 = new GridBagConstraints();
		gbc_cbSF2.anchor = GridBagConstraints.WEST;
		gbc_cbSF2.insets = new Insets(5, 0, 5, 0);
		gbc_cbSF2.gridx = 1;
		gbc_cbSF2.gridy = 0;
		pnlSF2.add(cbSF2, gbc_cbSF2);
		editableEnable.addComponent("cbSF2", cbSF2);
		cbSF2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				cbClickCallback();
			}});
		
		txtSF2 = new JTextField();
		txtSF2.setBackground(new Color(255, 255, 255));
		txtSF2.setEditable(false);
		GridBagConstraints gbc_txtSF2 = new GridBagConstraints();
		gbc_txtSF2.gridwidth = 2;
		gbc_txtSF2.insets = new Insets(0, 5, 5, 5);
		gbc_txtSF2.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtSF2.gridx = 0;
		gbc_txtSF2.gridy = 1;
		pnlSF2.add(txtSF2, gbc_txtSF2);
		txtSF2.setColumns(10);
		editableEnable.addComponent("txtSF2", txtSF2);
		cg2.addComponent("txtSF2", txtSF2);
		
		JButton btnSet2 = new JButton("Set...");
		GridBagConstraints gbc_btnSet2 = new GridBagConstraints();
		gbc_btnSet2.insets = new Insets(0, 5, 5, 5);
		gbc_btnSet2.anchor = GridBagConstraints.WEST;
		gbc_btnSet2.gridx = 0;
		gbc_btnSet2.gridy = 2;
		pnlSF2.add(btnSet2, gbc_btnSet2);
		editableEnable.addComponent("btnSet2", btnSet2);
		cg2.addComponent("btnSet2", btnSet2);
		btnSet2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnSetCallback(SLOT_IDX_SF2);
			}});
		
		JPanel pnlSF3 = new JPanel();
		pnlSF3.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlSF3 = new GridBagConstraints();
		gbc_pnlSF3.insets = new Insets(0, 5, 5, 5);
		gbc_pnlSF3.fill = GridBagConstraints.BOTH;
		gbc_pnlSF3.gridx = 0;
		gbc_pnlSF3.gridy = 2;
		getContentPane().add(pnlSF3, gbc_pnlSF3);
		GridBagLayout gbl_pnlSF3 = new GridBagLayout();
		gbl_pnlSF3.columnWidths = new int[]{0, 75, 0};
		gbl_pnlSF3.rowHeights = new int[]{0, 0, 0, 0};
		gbl_pnlSF3.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlSF3.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlSF3.setLayout(gbl_pnlSF3);
		
		JLabel lblSoundfontC = new JLabel("Soundfont C");
		lblSoundfontC.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblSoundfontC = new GridBagConstraints();
		gbc_lblSoundfontC.insets = new Insets(5, 0, 5, 5);
		gbc_lblSoundfontC.gridx = 0;
		gbc_lblSoundfontC.gridy = 0;
		pnlSF3.add(lblSoundfontC, gbc_lblSoundfontC);
		
		cbSF3 = new JCheckBox("Include");
		GridBagConstraints gbc_cbSF3 = new GridBagConstraints();
		gbc_cbSF3.anchor = GridBagConstraints.WEST;
		gbc_cbSF3.insets = new Insets(5, 0, 5, 0);
		gbc_cbSF3.gridx = 1;
		gbc_cbSF3.gridy = 0;
		pnlSF3.add(cbSF3, gbc_cbSF3);
		editableEnable.addComponent("cbSF3", cbSF3);
		cbSF3.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				cbClickCallback();
			}});
		
		txtSF3 = new JTextField();
		txtSF3.setBackground(new Color(255, 255, 255));
		txtSF3.setEditable(false);
		txtSF3.setColumns(10);
		GridBagConstraints gbc_txtSF3 = new GridBagConstraints();
		gbc_txtSF3.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtSF3.gridwidth = 2;
		gbc_txtSF3.insets = new Insets(0, 5, 5, 5);
		gbc_txtSF3.gridx = 0;
		gbc_txtSF3.gridy = 1;
		pnlSF3.add(txtSF3, gbc_txtSF3);
		editableEnable.addComponent("txtSF3", txtSF3);
		cg3.addComponent("txtSF3", txtSF3);
		
		JButton btnSet3 = new JButton("Set...");
		GridBagConstraints gbc_btnSet3 = new GridBagConstraints();
		gbc_btnSet3.anchor = GridBagConstraints.WEST;
		gbc_btnSet3.insets = new Insets(0, 5, 5, 5);
		gbc_btnSet3.gridx = 0;
		gbc_btnSet3.gridy = 2;
		pnlSF3.add(btnSet3, gbc_btnSet3);
		editableEnable.addComponent("btnSet3", btnSet3);
		cg3.addComponent("btnSet3", btnSet3);
		btnSet3.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnSetCallback(SLOT_IDX_SF3);
			}});
		
		JPanel pnlSF4 = new JPanel();
		pnlSF4.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlSF4 = new GridBagConstraints();
		gbc_pnlSF4.insets = new Insets(0, 5, 5, 5);
		gbc_pnlSF4.fill = GridBagConstraints.BOTH;
		gbc_pnlSF4.gridx = 0;
		gbc_pnlSF4.gridy = 3;
		getContentPane().add(pnlSF4, gbc_pnlSF4);
		GridBagLayout gbl_pnlSF4 = new GridBagLayout();
		gbl_pnlSF4.columnWidths = new int[]{0, 75, 0};
		gbl_pnlSF4.rowHeights = new int[]{0, 0, 0, 0};
		gbl_pnlSF4.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlSF4.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlSF4.setLayout(gbl_pnlSF4);
		
		JLabel lblSoundfontD = new JLabel("Soundfont D");
		lblSoundfontD.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblSoundfontD = new GridBagConstraints();
		gbc_lblSoundfontD.insets = new Insets(5, 0, 5, 5);
		gbc_lblSoundfontD.gridx = 0;
		gbc_lblSoundfontD.gridy = 0;
		pnlSF4.add(lblSoundfontD, gbc_lblSoundfontD);
		
		cbSF4 = new JCheckBox("Include");
		GridBagConstraints gbc_cbSF4 = new GridBagConstraints();
		gbc_cbSF4.anchor = GridBagConstraints.WEST;
		gbc_cbSF4.insets = new Insets(5, 0, 5, 0);
		gbc_cbSF4.gridx = 1;
		gbc_cbSF4.gridy = 0;
		pnlSF4.add(cbSF4, gbc_cbSF4);
		editableEnable.addComponent("cbSF4", cbSF4);
		cbSF4.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				cbClickCallback();
			}});
		
		txtSF4 = new JTextField();
		txtSF4.setBackground(new Color(255, 255, 255));
		txtSF4.setEditable(false);
		txtSF4.setColumns(10);
		GridBagConstraints gbc_txtSF4 = new GridBagConstraints();
		gbc_txtSF4.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtSF4.gridwidth = 2;
		gbc_txtSF4.insets = new Insets(0, 5, 5, 5);
		gbc_txtSF4.gridx = 0;
		gbc_txtSF4.gridy = 1;
		pnlSF4.add(txtSF4, gbc_txtSF4);
		editableEnable.addComponent("txtSF4", txtSF4);
		cg4.addComponent("txtSF4", txtSF4);
		
		JButton btnSet4 = new JButton("Set...");
		GridBagConstraints gbc_btnSet4 = new GridBagConstraints();
		gbc_btnSet4.anchor = GridBagConstraints.WEST;
		gbc_btnSet4.insets = new Insets(0, 5, 5, 5);
		gbc_btnSet4.gridx = 0;
		gbc_btnSet4.gridy = 2;
		pnlSF4.add(btnSet4, gbc_btnSet4);
		editableEnable.addComponent("btnSet4", btnSet4);
		cg4.addComponent("btnSet4", btnSet4);
		btnSet4.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnSetCallback(SLOT_IDX_SF4);
			}});
		
		JPanel pnlBtn = new JPanel();
		GridBagConstraints gbc_pnlBtn = new GridBagConstraints();
		gbc_pnlBtn.insets = new Insets(0, 5, 5, 0);
		gbc_pnlBtn.fill = GridBagConstraints.BOTH;
		gbc_pnlBtn.gridx = 0;
		gbc_pnlBtn.gridy = 4;
		getContentPane().add(pnlBtn, gbc_pnlBtn);
		GridBagLayout gbl_pnlBtn = new GridBagLayout();
		gbl_pnlBtn.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlBtn.rowHeights = new int[]{0, 0};
		gbl_pnlBtn.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlBtn.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlBtn.setLayout(gbl_pnlBtn);
		
		JButton btnApply = new JButton("Apply");
		GridBagConstraints gbc_btnApply = new GridBagConstraints();
		gbc_btnApply.insets = new Insets(0, 0, 0, 5);
		gbc_btnApply.gridx = 1;
		gbc_btnApply.gridy = 0;
		pnlBtn.add(btnApply, gbc_btnApply);
		editableEnable.addComponent("btnApply", btnApply);
		btnApply.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnApplyCallback();
			}});
		
		JButton btnCancel = new JButton("Close");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 0;
		pnlBtn.add(btnCancel, gbc_btnCancel);
		globalEnable.addComponent("btnCancel", btnCancel);
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnCloseCallback();
			}});
	}
	
	/*--- Getters ---*/
	
	public BankTableEntry[] getBanks(){
		if(!changesApplied) return null;
		BankTableEntry[] res = new BankTableEntry[4];
		for(int i = 0; i < 4; i++) res[i] = applBanks[i];
		
		return res;
	}
	
	/*--- Setters ---*/
	
	public boolean setBank(int uid, int idx){
		if(idx < 0) return false;
		if(idx >= 4) return false;
		if(uid == 0 || uid == -1) return false;
		if(core == null) return false;
		
		BankTableEntry entry = core.getBankInfo(uid);
		if(entry == null) return false;
		
		banks[idx] = entry;
		
		JTextField txt = null;
		switch(idx){
		case SLOT_IDX_SF1:
			txt = txtSF1; break;
		case SLOT_IDX_SF2:
			txt = txtSF2; break;
		case SLOT_IDX_SF3:
			txt = txtSF3; break;
		case SLOT_IDX_SF4:
			txt = txtSF4; break;
		}
		updateBankText(banks[idx], txt);
		
		if(banks[idx] != null){
			switch(idx){
			case SLOT_IDX_SF2:
				cbSF2.setSelected(true); break;
			case SLOT_IDX_SF3:
				cbSF3.setSelected(true); break;
			case SLOT_IDX_SF4:
				cbSF4.setSelected(true); break;
			}
		}
		
		reenable();
		return true;
	}

	public void clear(){
		for(int i = 0; i < 4; i++){
			banks[i] = null;
			applBanks[i] = null;
			cbSF2.setSelected(false);
			cbSF3.setSelected(false);
			cbSF4.setSelected(false);
		}
		
		reenable();
	}
	
	/*--- Drawing ---*/
	
	private void updateBankText(BankTableEntry entry, JTextField textField){
		if(entry == null){
			textField.setText("<null>");
		}
		else{
			String name = entry.getName();
			if(name == null){
				name = String.format("[Untitled Bank %08x]", entry.getUID());
			}
			else{
				textField.setText(name);
			}
		}
		textField.repaint();
	}
	
	public void disableAll(){
		globalEnable.setEnabling(false);
		globalEnable.repaint();
	}
	
	public void reenable(){
		globalEnable.setEnabling(true);
		if(!editable) editableEnable.setEnabling(false);
		
		//SF Boxes...
		if(editable){
			cg4.setEnabling(cbSF4.isSelected());
			cg3.setEnabling(cbSF3.isSelected());
			cg2.setEnabling(cbSF2.isSelected());
			cbSF4.setEnabled(cbSF3.isSelected() && cbSF2.isSelected());
			cbSF3.setEnabled(cbSF2.isSelected());
		}
		
		globalEnable.repaint();
	}
	
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
	
	/*--- Callbacks ---*/
	
	private void cbClickCallback(){
		reenable();
	}
	
	private void btnSetCallback(int idx){
		if(!editable) return;
		
		setWait();
		BankPickDialog dialog = new BankPickDialog(parent.getParentFrame(), core);
		dialog.showMe(this);
		
		if(dialog.getExitSelection()){
			ZeqerBank bank = dialog.getSelectedBank();
			if(bank != null){
				banks[idx] = bank.getTableEntry();
				
				//Update text box
				JTextField txt = null;
				switch(idx){
				case SLOT_IDX_SF1:
					txt = txtSF1; break;
				case SLOT_IDX_SF2:
					txt = txtSF2; break;
				case SLOT_IDX_SF3:
					txt = txtSF3; break;
				case SLOT_IDX_SF4:
					txt = txtSF4; break;
				}
				updateBankText(banks[idx], txt);
				
				changesApplied = false;
			}
		}
		unsetWait();
	}
	
	private void btnApplyCallback(){
		if(!editable) return;
		
		applBanks[SLOT_IDX_SF1] = banks[SLOT_IDX_SF1];
		
		if(cbSF2.isSelected()) applBanks[SLOT_IDX_SF2] = banks[SLOT_IDX_SF2];
		else applBanks[SLOT_IDX_SF2] = null;
		
		if(cbSF3.isSelected()) applBanks[SLOT_IDX_SF3] = banks[SLOT_IDX_SF3];
		else applBanks[SLOT_IDX_SF3] = null;
		
		if(cbSF4.isSelected()) applBanks[SLOT_IDX_SF4] = banks[SLOT_IDX_SF4];
		else applBanks[SLOT_IDX_SF4] = null;
		
		changesApplied = true;
	}
	
	private void btnCloseCallback(){
		this.setVisible(false);
	}
	
}
