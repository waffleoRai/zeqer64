package waffleoRai_zeqer64.GUI.dialogs;

import javax.swing.JDialog;
import javax.swing.JFrame;

import waffleoRai_zeqer64.ZeqerUtils;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;
import java.awt.GridBagLayout;
import javax.swing.JPanel;

import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_SoundSynth.SynthMath;
import waffleoRai_soundbank.nintendo.z64.Z64Bank;
import waffleoRai_soundbank.nintendo.z64.Z64SoundEffect;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.border.BevelBorder;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.GUITools;
import javax.swing.ListSelectionModel;

public class ZeqerBankSFXEditDialog extends JDialog{
	
	private static final long serialVersionUID = -5916625453341705669L;
	
	public static final int MIN_WIDTH = 585;
	public static final int MIN_HEIGHT = 360;
	
	public static final int COARSE_TUNE_MAX = 24;
	public static final int MAX_GROUPS = 128;
	
	/*----- Inner Classes -----*/
	
	public static class SFXNode{
		public Z64WaveInfo sample;
		public int tune; //Total coarse and fine tune in cents
		public int slot; //For rendering string
		public String enumLabel = null;
		
		public String toString(){
			if(sample == null){
				return slot + " - <empty>";
			}
			else{
				String name = sample.getName();
				if(name == null) name = String.format("[Sample %08x]", sample.getUID());
				return slot + " - " + name;
			}
		}
	}
	
	/*----- Instance Variables -----*/
	
	private ZeqerCoreInterface core;
	
	private boolean editable = true;
	private boolean exitSelection = false;
	private ArrayList<SFXNode[]> groups;
	private Z64WaveInfo editWave = null; //Sample in edit region
	
	private JFrame parent;
	private ComponentGroup globalEnable;
	private ComponentGroup editableEnable;
	private ComponentGroup slotEditEnable;
	
	private JComboBox<Integer> cmbxGroups;
	private JList<SFXNode> lstSFX;
	
	private JTextField txtSampleName;
	private JSlider sldCoarse;
	private JSlider sldFine;
	private JLabel lblCoarse;
	private JLabel lblFine;
	private JTextField txtEnum;
	
	/*----- Init -----*/
	
	public ZeqerBankSFXEditDialog(JFrame parentFrame, ZeqerCoreInterface coreIface){
		super(parentFrame, true);
		parent = parentFrame;
		core = coreIface;
		groups = new ArrayList<SFXNode[]>(4);
		globalEnable = new ComponentGroup();
		editableEnable = new ComponentGroup();
		slotEditEnable = new ComponentGroup();
		
		initGUI();
	}
	
	private void initGUI(){
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		
		setTitle("Edit SFX Set");
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 2.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel pnlList = new JPanel();
		GridBagConstraints gbc_pnlList = new GridBagConstraints();
		gbc_pnlList.insets = new Insets(0, 0, 5, 5);
		gbc_pnlList.fill = GridBagConstraints.BOTH;
		gbc_pnlList.gridx = 0;
		gbc_pnlList.gridy = 0;
		getContentPane().add(pnlList, gbc_pnlList);
		GridBagLayout gbl_pnlList = new GridBagLayout();
		gbl_pnlList.columnWidths = new int[]{0, 0};
		gbl_pnlList.rowHeights = new int[]{0, 0, 0};
		gbl_pnlList.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlList.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		pnlList.setLayout(gbl_pnlList);
		
		JPanel pnlGroupPick = new JPanel();
		GridBagConstraints gbc_pnlGroupPick = new GridBagConstraints();
		gbc_pnlGroupPick.insets = new Insets(0, 0, 5, 0);
		gbc_pnlGroupPick.fill = GridBagConstraints.BOTH;
		gbc_pnlGroupPick.gridx = 0;
		gbc_pnlGroupPick.gridy = 0;
		pnlList.add(pnlGroupPick, gbc_pnlGroupPick);
		GridBagLayout gbl_pnlGroupPick = new GridBagLayout();
		gbl_pnlGroupPick.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_pnlGroupPick.rowHeights = new int[]{0, 0};
		gbl_pnlGroupPick.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlGroupPick.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlGroupPick.setLayout(gbl_pnlGroupPick);
		
		JLabel lblGroup = new JLabel("Group:");
		GridBagConstraints gbc_lblGroup = new GridBagConstraints();
		gbc_lblGroup.insets = new Insets(5, 5, 5, 5);
		gbc_lblGroup.anchor = GridBagConstraints.EAST;
		gbc_lblGroup.gridx = 0;
		gbc_lblGroup.gridy = 0;
		pnlGroupPick.add(lblGroup, gbc_lblGroup);
		
		cmbxGroups = new JComboBox<Integer>();
		GridBagConstraints gbc_cmbxGroups = new GridBagConstraints();
		gbc_cmbxGroups.insets = new Insets(5, 0, 5, 5);
		gbc_cmbxGroups.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxGroups.gridx = 1;
		gbc_cmbxGroups.gridy = 0;
		pnlGroupPick.add(cmbxGroups, gbc_cmbxGroups);
		globalEnable.addComponent("cmbxGroups", cmbxGroups);
		cmbxGroups.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onCmbxSelection();
			}});
		
		JButton btnAddGroup = new JButton("+");
		GridBagConstraints gbc_btnAddGroup = new GridBagConstraints();
		gbc_btnAddGroup.insets = new Insets(0, 0, 0, 5);
		gbc_btnAddGroup.gridx = 2;
		gbc_btnAddGroup.gridy = 0;
		pnlGroupPick.add(btnAddGroup, gbc_btnAddGroup);
		globalEnable.addComponent("btnAddGroup", btnAddGroup);
		editableEnable.addComponent("btnAddGroup", btnAddGroup);
		btnAddGroup.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnAddGroupCallback();
			}});
		
		JButton btnDelGroup = new JButton("-");
		GridBagConstraints gbc_btnDelGroup = new GridBagConstraints();
		gbc_btnDelGroup.gridx = 3;
		gbc_btnDelGroup.gridy = 0;
		pnlGroupPick.add(btnDelGroup, gbc_btnDelGroup);
		globalEnable.addComponent("btnDelGroup", btnDelGroup);
		editableEnable.addComponent("btnDelGroup", btnDelGroup);
		btnDelGroup.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnDelGroupCallback();
			}});
		
		JScrollPane spList = new JScrollPane();
		GridBagConstraints gbc_spList = new GridBagConstraints();
		gbc_spList.insets = new Insets(0, 5, 0, 0);
		gbc_spList.fill = GridBagConstraints.BOTH;
		gbc_spList.gridx = 0;
		gbc_spList.gridy = 1;
		pnlList.add(spList, gbc_spList);
		globalEnable.addComponent("spList", spList);
		
		lstSFX = new JList<SFXNode>();
		lstSFX.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lstSFX.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		spList.setViewportView(lstSFX);
		globalEnable.addComponent("lstSFX", lstSFX);
		lstSFX.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e) {
				onListItemSelect();
			}});
		
		JPanel pnlEdit = new JPanel();
		pnlEdit.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlEdit = new GridBagConstraints();
		gbc_pnlEdit.insets = new Insets(0, 0, 5, 0);
		gbc_pnlEdit.fill = GridBagConstraints.BOTH;
		gbc_pnlEdit.gridx = 1;
		gbc_pnlEdit.gridy = 0;
		getContentPane().add(pnlEdit, gbc_pnlEdit);
		GridBagLayout gbl_pnlEdit = new GridBagLayout();
		gbl_pnlEdit.columnWidths = new int[]{0, 0};
		gbl_pnlEdit.rowHeights = new int[]{0, 0, 0};
		gbl_pnlEdit.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlEdit.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		pnlEdit.setLayout(gbl_pnlEdit);
		
		JPanel pnlEditArea = new JPanel();
		GridBagConstraints gbc_pnlEditArea = new GridBagConstraints();
		gbc_pnlEditArea.insets = new Insets(0, 0, 5, 0);
		gbc_pnlEditArea.fill = GridBagConstraints.BOTH;
		gbc_pnlEditArea.gridx = 0;
		gbc_pnlEditArea.gridy = 0;
		pnlEdit.add(pnlEditArea, gbc_pnlEditArea);
		GridBagLayout gbl_pnlEditArea = new GridBagLayout();
		gbl_pnlEditArea.columnWidths = new int[]{0, 75, 0, 0, 0, 0};
		gbl_pnlEditArea.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_pnlEditArea.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlEditArea.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		pnlEditArea.setLayout(gbl_pnlEditArea);
		
		JLabel lblSample = new JLabel("Sample:");
		GridBagConstraints gbc_lblSample = new GridBagConstraints();
		gbc_lblSample.anchor = GridBagConstraints.EAST;
		gbc_lblSample.insets = new Insets(5, 5, 5, 5);
		gbc_lblSample.gridx = 0;
		gbc_lblSample.gridy = 0;
		pnlEditArea.add(lblSample, gbc_lblSample);
		
		txtSampleName = new JTextField();
		GridBagConstraints gbc_txtSampleName = new GridBagConstraints();
		gbc_txtSampleName.gridwidth = 2;
		gbc_txtSampleName.insets = new Insets(5, 0, 5, 5);
		gbc_txtSampleName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtSampleName.gridx = 1;
		gbc_txtSampleName.gridy = 0;
		pnlEditArea.add(txtSampleName, gbc_txtSampleName);
		txtSampleName.setColumns(10);
		globalEnable.addComponent("txtSampleName", txtSampleName);
		editableEnable.addComponent("txtSampleName", txtSampleName);
		slotEditEnable.addComponent("txtSampleName", txtSampleName);
		
		JButton btnSample = new JButton("Set Sample...");
		GridBagConstraints gbc_btnSample = new GridBagConstraints();
		gbc_btnSample.gridwidth = 2;
		gbc_btnSample.insets = new Insets(5, 0, 5, 5);
		gbc_btnSample.gridx = 1;
		gbc_btnSample.gridy = 1;
		pnlEditArea.add(btnSample, gbc_btnSample);
		globalEnable.addComponent("btnSample", btnSample);
		editableEnable.addComponent("btnSample", btnSample);
		slotEditEnable.addComponent("btnSample", btnSample);
		btnSample.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnSetSampleCallback();
			}});
		
		JLabel lblCoarseTune = new JLabel("Coarse Tune:");
		GridBagConstraints gbc_lblCoarseTune = new GridBagConstraints();
		gbc_lblCoarseTune.anchor = GridBagConstraints.EAST;
		gbc_lblCoarseTune.insets = new Insets(0, 5, 5, 5);
		gbc_lblCoarseTune.gridx = 0;
		gbc_lblCoarseTune.gridy = 2;
		pnlEditArea.add(lblCoarseTune, gbc_lblCoarseTune);
		
		sldCoarse = new JSlider();
		GridBagConstraints gbc_sldCoarse = new GridBagConstraints();
		gbc_sldCoarse.fill = GridBagConstraints.HORIZONTAL;
		gbc_sldCoarse.insets = new Insets(0, 0, 5, 5);
		gbc_sldCoarse.gridx = 1;
		gbc_sldCoarse.gridy = 2;
		pnlEditArea.add(sldCoarse, gbc_sldCoarse);
		globalEnable.addComponent("sldCoarse", sldCoarse);
		editableEnable.addComponent("sldCoarse", sldCoarse);
		slotEditEnable.addComponent("sldCoarse", sldCoarse);
		
		sldCoarse.setMinimum(COARSE_TUNE_MAX * -1);
		sldCoarse.setMaximum(COARSE_TUNE_MAX);
		sldCoarse.setValue(0);
		sldCoarse.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				onCoarseTuneUpdate();
			}});
		
		lblCoarse = new JLabel("0 semitone(s)");
		GridBagConstraints gbc_lblCoarse = new GridBagConstraints();
		gbc_lblCoarse.anchor = GridBagConstraints.WEST;
		gbc_lblCoarse.insets = new Insets(0, 0, 5, 5);
		gbc_lblCoarse.gridx = 2;
		gbc_lblCoarse.gridy = 2;
		pnlEditArea.add(lblCoarse, gbc_lblCoarse);
		
		JButton btnCL = new JButton("<");
		GridBagConstraints gbc_btnCL = new GridBagConstraints();
		gbc_btnCL.insets = new Insets(0, 0, 5, 5);
		gbc_btnCL.gridx = 3;
		gbc_btnCL.gridy = 2;
		pnlEditArea.add(btnCL, gbc_btnCL);
		globalEnable.addComponent("btnCL", btnCL);
		editableEnable.addComponent("btnCL", btnCL);
		slotEditEnable.addComponent("btnCL", btnCL);
		btnCL.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnCLCallback();
			}});
		
		JButton btnCR = new JButton(">");
		GridBagConstraints gbc_btnCR = new GridBagConstraints();
		gbc_btnCR.insets = new Insets(0, 0, 5, 0);
		gbc_btnCR.gridx = 4;
		gbc_btnCR.gridy = 2;
		pnlEditArea.add(btnCR, gbc_btnCR);
		globalEnable.addComponent("btnCR", btnCR);
		editableEnable.addComponent("btnCR", btnCR);
		slotEditEnable.addComponent("btnCR", btnCR);
		btnCR.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnCRCallback();
			}});
		
		JLabel lblFineTune = new JLabel("Fine Tune:");
		GridBagConstraints gbc_lblFineTune = new GridBagConstraints();
		gbc_lblFineTune.anchor = GridBagConstraints.EAST;
		gbc_lblFineTune.insets = new Insets(0, 5, 5, 5);
		gbc_lblFineTune.gridx = 0;
		gbc_lblFineTune.gridy = 3;
		pnlEditArea.add(lblFineTune, gbc_lblFineTune);
		
		sldFine = new JSlider();
		GridBagConstraints gbc_sldFine = new GridBagConstraints();
		gbc_sldFine.fill = GridBagConstraints.HORIZONTAL;
		gbc_sldFine.insets = new Insets(0, 0, 5, 5);
		gbc_sldFine.gridx = 1;
		gbc_sldFine.gridy = 3;
		pnlEditArea.add(sldFine, gbc_sldFine);
		globalEnable.addComponent("sldFine", sldFine);
		editableEnable.addComponent("sldFine", sldFine);
		slotEditEnable.addComponent("sldFine", sldFine);
		
		sldFine.setMinimum(-100);
		sldFine.setMaximum(100);
		sldFine.setValue(0);
		sldFine.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				onFineTuneUpdate();
			}});
		
		lblFine = new JLabel("0 cent(s)");
		GridBagConstraints gbc_lblFine = new GridBagConstraints();
		gbc_lblFine.anchor = GridBagConstraints.WEST;
		gbc_lblFine.insets = new Insets(0, 0, 5, 5);
		gbc_lblFine.gridx = 2;
		gbc_lblFine.gridy = 3;
		pnlEditArea.add(lblFine, gbc_lblFine);
		
		JButton btnFL = new JButton("<");
		GridBagConstraints gbc_btnFL = new GridBagConstraints();
		gbc_btnFL.insets = new Insets(0, 0, 5, 5);
		gbc_btnFL.gridx = 3;
		gbc_btnFL.gridy = 3;
		pnlEditArea.add(btnFL, gbc_btnFL);
		globalEnable.addComponent("btnFL", btnFL);
		editableEnable.addComponent("btnFL", btnFL);
		slotEditEnable.addComponent("btnFL", btnFL);
		btnFL.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnFLCallback();
			}});
		
		JButton btnFR = new JButton(">");
		GridBagConstraints gbc_btnFR = new GridBagConstraints();
		gbc_btnFR.insets = new Insets(0, 0, 5, 0);
		gbc_btnFR.gridx = 4;
		gbc_btnFR.gridy = 3;
		pnlEditArea.add(btnFR, gbc_btnFR);
		globalEnable.addComponent("btnFR", btnFR);
		editableEnable.addComponent("btnFR", btnFR);
		slotEditEnable.addComponent("btnFR", btnFR);
		btnFR.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnFRCallback();
			}});
		
		JLabel lblEnum = new JLabel("Enum:");
		GridBagConstraints gbc_lblEnum = new GridBagConstraints();
		gbc_lblEnum.insets = new Insets(0, 0, 5, 5);
		gbc_lblEnum.anchor = GridBagConstraints.EAST;
		gbc_lblEnum.gridx = 0;
		gbc_lblEnum.gridy = 4;
		pnlEditArea.add(lblEnum, gbc_lblEnum);
		
		txtEnum = new JTextField();
		GridBagConstraints gbc_txtEnum = new GridBagConstraints();
		gbc_txtEnum.gridwidth = 2;
		gbc_txtEnum.insets = new Insets(0, 0, 5, 5);
		gbc_txtEnum.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtEnum.gridx = 1;
		gbc_txtEnum.gridy = 4;
		pnlEditArea.add(txtEnum, gbc_txtEnum);
		txtEnum.setColumns(10);
		globalEnable.addComponent("txtEnum", txtEnum);
		editableEnable.addComponent("txtEnum", txtEnum);
		slotEditEnable.addComponent("txtEnum", txtEnum);
		
		JPanel pnlEditBtn = new JPanel();
		GridBagConstraints gbc_pnlEditBtn = new GridBagConstraints();
		gbc_pnlEditBtn.fill = GridBagConstraints.BOTH;
		gbc_pnlEditBtn.gridx = 0;
		gbc_pnlEditBtn.gridy = 1;
		pnlEdit.add(pnlEditBtn, gbc_pnlEditBtn);
		GridBagLayout gbl_pnlEditBtn = new GridBagLayout();
		gbl_pnlEditBtn.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_pnlEditBtn.rowHeights = new int[]{0, 0};
		gbl_pnlEditBtn.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlEditBtn.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlEditBtn.setLayout(gbl_pnlEditBtn);
		
		JButton btnClear = new JButton("Clear Slot");
		GridBagConstraints gbc_btnClear = new GridBagConstraints();
		gbc_btnClear.insets = new Insets(0, 5, 0, 5);
		gbc_btnClear.gridx = 0;
		gbc_btnClear.gridy = 0;
		pnlEditBtn.add(btnClear, gbc_btnClear);
		globalEnable.addComponent("btnClear", btnClear);
		editableEnable.addComponent("btnClear", btnClear);
		slotEditEnable.addComponent("btnClear", btnClear);
		btnClear.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnClearCallback();
			}});
		
		JButton btnPlay = new JButton("Play");
		GridBagConstraints gbc_btnPlay = new GridBagConstraints();
		gbc_btnPlay.insets = new Insets(0, 0, 0, 5);
		gbc_btnPlay.gridx = 2;
		gbc_btnPlay.gridy = 0;
		pnlEditBtn.add(btnPlay, gbc_btnPlay);
		globalEnable.addComponent("btnPlay", btnPlay);
		slotEditEnable.addComponent("btnPlay", btnPlay);
		btnPlay.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnPlayCallback();
			}});
		
		JButton btnApply = new JButton("Apply");
		GridBagConstraints gbc_btnApply = new GridBagConstraints();
		gbc_btnApply.gridx = 3;
		gbc_btnApply.gridy = 0;
		pnlEditBtn.add(btnApply, gbc_btnApply);
		globalEnable.addComponent("btnApply", btnApply);
		editableEnable.addComponent("btnApply", btnApply);
		slotEditEnable.addComponent("btnApply", btnApply);
		btnApply.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnApplyCallback();
			}});
		
		JPanel pnlPlay = new JPanel();
		GridBagConstraints gbc_pnlPlay = new GridBagConstraints();
		gbc_pnlPlay.gridwidth = 2;
		gbc_pnlPlay.insets = new Insets(0, 0, 5, 0);
		gbc_pnlPlay.fill = GridBagConstraints.BOTH;
		gbc_pnlPlay.gridx = 0;
		gbc_pnlPlay.gridy = 1;
		getContentPane().add(pnlPlay, gbc_pnlPlay);
		GridBagLayout gbl_pnlPlay = new GridBagLayout();
		gbl_pnlPlay.columnWidths = new int[]{0};
		gbl_pnlPlay.rowHeights = new int[]{0};
		gbl_pnlPlay.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_pnlPlay.rowWeights = new double[]{Double.MIN_VALUE};
		pnlPlay.setLayout(gbl_pnlPlay);
		
		JPanel pnlBtn = new JPanel();
		GridBagConstraints gbc_pnlBtn = new GridBagConstraints();
		gbc_pnlBtn.gridwidth = 2;
		gbc_pnlBtn.fill = GridBagConstraints.BOTH;
		gbc_pnlBtn.gridx = 0;
		gbc_pnlBtn.gridy = 2;
		getContentPane().add(pnlBtn, gbc_pnlBtn);
		GridBagLayout gbl_pnlBtn = new GridBagLayout();
		gbl_pnlBtn.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlBtn.rowHeights = new int[]{0, 0};
		gbl_pnlBtn.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlBtn.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlBtn.setLayout(gbl_pnlBtn);
		
		JButton btnOkay = new JButton("Okay");
		GridBagConstraints gbc_btnOkay = new GridBagConstraints();
		gbc_btnOkay.insets = new Insets(0, 0, 5, 5);
		gbc_btnOkay.gridx = 1;
		gbc_btnOkay.gridy = 0;
		pnlBtn.add(btnOkay, gbc_btnOkay);
		globalEnable.addComponent("btnOkay", btnOkay);
		btnOkay.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnOkayCallback();
			}});
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(0, 0, 5, 5);
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 0;
		pnlBtn.add(btnCancel, gbc_btnCancel);
		globalEnable.addComponent("btnCancel", btnCancel);
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnCancelCallback();
			}});
	}
	
	/*----- Getters -----*/
	
	public boolean getExitSelection(){return exitSelection;}
	
	public int getSFXCount(){
		if(groups == null) return 0;
		
		int ct = 0;
		int gcount = groups.size();
		for(int g = gcount - 1; g >=0; g--){
			if(ct > 0) break;
			SFXNode[] group = groups.get(g);
			if(group == null) continue;
			
			for(int j = group.length - 1; j >= 0; j--){
				if(group[j] != null){
					if(group[j].sample != null){
						ct = (g << 6) + j + 1;
						break;
					}
				}
			}
		}
		
		return ct;
	}
	
	public Z64SoundEffect getSFX(int index){
		int g = index >>> 6;
		int i = index & 0x3f;
		return getSFX(g,i);
	}
	
	public Z64SoundEffect getSFX(int group, int index){
		if(groups == null) return null;
		if(group < 0) return null;
		if(index < 0 || index >= 64) return null;
		if(group >= groups.size()) return null;
		
		SFXNode[] gdata = groups.get(group);
		if(gdata[index] == null) return null;
		if(gdata[index].sample == null) return null;
		
		Z64SoundEffect sfx = new Z64SoundEffect();
		sfx.setSample(gdata[index].sample);
		
		double tune = SynthMath.cents2FreqRatio(gdata[index].tune);
		tune = 1.0/tune;
		sfx.setTuning((float)tune);
		
		return sfx;
	}
	
	public String getEnumLabel(int index){
		int g = index >>> 6;
		int i = index & 0x3f;
		return getEnumLabel(g,i);
	}
	
	public String getEnumLabel(int group, int index){
		if(groups == null) return null;
		if(group < 0) return null;
		if(index < 0 || index >= 64) return null;
		if(group >= groups.size()) return null;
		
		SFXNode[] gdata = groups.get(group);
		if(gdata[index] == null) return null;

		return gdata[index].enumLabel;
	}
	
	public boolean updateBankFromForm(Z64Bank bank){
		if(bank == null) return false;
		
		//Clear existing
		int sfxcount = bank.getEffectiveSFXCount();
		for(int i = 0; i < sfxcount; i++){
			bank.setSFX(i, null);
		}
		
		//Add data from form
		int gcount = groups.size();
		int i = 0;
		for(int g = 0; g < gcount; g++){
			SFXNode[] group = groups.get(g);
			for(int j = 0; j < 64; j++){
				if((group[j] != null) && (group[j].sample != null)){
					Z64SoundEffect sfx = new Z64SoundEffect();
					sfx.setSample(group[j].sample);
					double tune = SynthMath.cents2FreqRatio(group[j].tune);
					tune = 1.0/tune;
					sfx.setTuning((float)tune);
					bank.setSFX(i, sfx);
	
					if(group[j].enumLabel != null){
						bank.setSFXPresetEnumString(i, group[j].enumLabel);
					}
				}
				i++;
			}
		}
		
		return true;
	}
	
	/*----- Setters -----*/
	
	public void setEditable(boolean b){
		editable = b;
		refreshGUI();
	}
	
	public void loadSFXSetToForm(Z64SoundEffect[] sfxset, String[] enumset){
		editWave = null;
		groups.clear();
		if(sfxset != null){
			int sfxct = -1;
			for(int j = sfxset.length-1; j >= 0; j--){
				if(sfxset[j] != null){
					sfxct = j + 1;
					break;
				}
			}
			
			int gcount = (sfxct + 0x3F) >>> 6;
			
			int i = 0;
			for(int g = 0; g < gcount; g++){
				SFXNode[] group = new SFXNode[64];
				groups.add(group);
				
				for(int j = 0; j < 64; j++){
					if(i < sfxset.length && sfxset[i] != null){
						group[j] = new SFXNode();
						group[j].sample = sfxset[i].getSample();
						group[j].slot = j;
						double tune = sfxset[i].getTuning();
						group[j].tune = SynthMath.freqRatio2Cents(1.0/tune);
					}
					else{
						group[j] = new SFXNode();
						group[j].slot = j;
					}
					i++;
				}
			}
		}
		
		refreshGUI();
	}
	
	public void loadBankToForm(Z64Bank bank){
		if(bank != null){
			Z64SoundEffect[] sfxset = bank.getSFXSet();
			if(sfxset != null && sfxset.length > 0){
				String[] enumset = new String[sfxset.length];
				for(int i = 0; i < enumset.length; i++){
					enumset[i] = bank.getSFXSlotEnumString(i);
				}
				loadSFXSetToForm(sfxset, enumset);	
			}
		}
		else{
			editWave = null;
			groups.clear();
			refreshGUI();
		}
	}
	
	/*----- Drawing -----*/
	
	private void refreshGUI(){
		updateCombobox();
		updateList(0);
		
		editableEnable.setEnabling(editable);
		slotEditEnable.setEnabling(!lstSFX.isSelectionEmpty());
		globalEnable.repaint();
	}
	
	private void updateCombobox(){
		DefaultComboBoxModel<Integer> mdl = new DefaultComboBoxModel<Integer>();
		
		int gcount = groups.size();
		if(gcount > 0){
			for(int i = 0; i < gcount; i++) mdl.addElement(i);
		}
		else{
			mdl.addElement(0);
		}
		
		cmbxGroups.setModel(mdl);
		cmbxGroups.setSelectedIndex(0);
		cmbxGroups.repaint();
	}
	
	private void updateList(int group){
		if((group < 0) || (group >= groups.size())){
			lstSFX.setModel(new DefaultListModel<SFXNode>());
			lstSFX.setSelectedIndex(-1);
			updateSlotEditGUI(null);
			lstSFX.repaint();
			return;
		}
		
		SFXNode[] group_members = groups.get(group);
		DefaultListModel<SFXNode> model = new DefaultListModel<SFXNode>();
		for(int i = 0; i < group_members.length; i++){
			model.addElement(group_members[i]);
		}
		
		lstSFX.setModel(model);
		lstSFX.setSelectedIndex(-1);
		updateSlotEditGUI(null);
		lstSFX.repaint();
	}
	
	private void updateSampleNameTextbox(){
		if(editWave != null){
			String name = editWave.getName();
			if(name == null) name = "<Untitled>";
			txtSampleName.setText(String.format("[%08x] %s", editWave.getUID(), name));
		}
		else{
			txtSampleName.setText("<null>");
		}
		txtSampleName.repaint();
	}
	
	private void updateSlotEditGUI(SFXNode node){
		if(node != null){
			editWave = node.sample;
			sldCoarse.setValue(node.tune/ 100);
			sldFine.setValue(node.tune % 100);
			slotEditEnable.setEnabling(true);
			if(node.enumLabel == null || node.enumLabel.isEmpty()){
				node.enumLabel = String.format("SFX_G%03d_%02d", cmbxGroups.getSelectedIndex(), node.slot);
			}
			txtEnum.setText(node.enumLabel);
		}
		else{
			editWave = null;
			sldCoarse.setValue(0);
			sldFine.setValue(0);
			slotEditEnable.setEnabling(false);
			txtEnum.setText("<No selection>");
		}
		
		updateSampleNameTextbox();
		slotEditEnable.repaint();
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
	
	public void closeMe(){
		this.setVisible(false);
		this.dispose();
	}
	
	/*----- Callbacks -----*/
	
	private void dummyCallback(){
		JOptionPane.showMessageDialog(this, "Sorry, this component doesn't work yet!", 
				"Notice", JOptionPane.INFORMATION_MESSAGE);
	}
	
	private void btnAddGroupCallback(){
		if(!editable) return; //Button does nothing, just in case.
		if(groups.size() >= MAX_GROUPS){
			//This is arbitrary
			JOptionPane.showMessageDialog(this, "You have reached the maximum number of groups!", 
					"Add Group", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		int res = JOptionPane.showConfirmDialog(this, "Do you want to add a new SFX group?", 
				"Add Group", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		
		if(res != JOptionPane.YES_OPTION) return;
		
		SFXNode[] group = new SFXNode[64];
		for(int i = 0; i < 64; i++){
			group[i] = new SFXNode();
			group[i].slot = i;
		}
		groups.add(group);
		
		updateCombobox();
	}
	
	private void btnDelGroupCallback(){
		if(!editable) return; //Button does nothing, just in case.
		if(groups.isEmpty()){
			//This is arbitrary
			JOptionPane.showMessageDialog(this, "There are no groups to delete!", 
					"Delete Group", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		int res = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the end SFX group?"
				+ " You will not be able to undo this.", 
				"Delete Group", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		
		if(res != JOptionPane.YES_OPTION) return;
		
		groups.remove(groups.size() - 1);
		updateCombobox();
	}
	
	private void btnOkayCallback(){
		exitSelection = true;
		closeMe();
	}
	
	private void btnCancelCallback(){
		exitSelection = false;
		editWave = null;
		groups.clear();
		closeMe();
	}
	
	private void btnClearCallback(){
		if(!editable) return;
		if(lstSFX.isSelectionEmpty()){
			JOptionPane.showMessageDialog(this, "Please select a slot to clear.", 
					"Clear Slot", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		if(groups.isEmpty()) return;
		
		int g = cmbxGroups.getSelectedIndex();
		int slot = lstSFX.getSelectedIndex();
		
		int res = JOptionPane.showConfirmDialog(this, "Clear slot " + slot + " of group " + g + "?", 
				"Clear Slot", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		
		if(res != JOptionPane.YES_OPTION) return;
		
		SFXNode[] group = groups.get(g);
		group[slot].sample = null;
		group[slot].tune = 0;
		group[slot].enumLabel = null;
		
		globalEnable.repaint();
	}
	
	private void btnPlayCallback(){
		//TODO
		dummyCallback();
	}
	
	private void btnApplyCallback(){
		if(!editable) return;
		if(lstSFX.isSelectionEmpty()){
			JOptionPane.showMessageDialog(this, "No slot is selected.", 
					"Update Slot", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		if(groups.isEmpty()) return;
		
		int g = cmbxGroups.getSelectedIndex();
		int slot = lstSFX.getSelectedIndex();
		
		int res = JOptionPane.showConfirmDialog(this, "Update slot " + slot + " of group " + g + "?", 
				"Update Slot", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		
		if(res != JOptionPane.YES_OPTION) return;
		
		SFXNode[] group = groups.get(g);
		group[slot].sample = editWave;
		group[slot].tune = (sldCoarse.getValue() * 100) + sldFine.getValue();
		
		String e = txtEnum.getText();
		if(e != null) group[slot].enumLabel = ZeqerUtils.fixHeaderEnumID(e, true);
		else{
			group[slot].enumLabel = String.format("SFX_G%03d_%02d", g, slot);
		}
		txtEnum.setText(group[slot].enumLabel);
		
		globalEnable.repaint();
	}
	
	private void btnSetSampleCallback(){
		//TODO
		dummyCallback();
	}
	
	private void btnCLCallback(){
		if(!editable) return;
		int current = sldCoarse.getValue();
		if(current <= (-COARSE_TUNE_MAX)) return;
		sldCoarse.setValue(current - 1);
		sldCoarse.repaint();
	}
	
	private void btnCRCallback(){
		if(!editable) return;
		int current = sldCoarse.getValue();
		if(current >= COARSE_TUNE_MAX) return;
		sldCoarse.setValue(current + 1);
		sldCoarse.repaint();
	}
	
	private void btnFLCallback(){
		if(!editable) return;
		int current = sldFine.getValue();
		if(current <= -100) return;
		sldFine.setValue(current - 1);
		sldFine.repaint();
	}
	
	private void btnFRCallback(){
		if(!editable) return;
		int current = sldFine.getValue();
		if(current >= 100) return;
		sldFine.setValue(current + 1);
		sldFine.repaint();
	}
	
	private void onCmbxSelection(){
		if(groups.isEmpty()) return;
		int g = cmbxGroups.getSelectedIndex();
		updateList(g);
	}
	
	private void onCoarseTuneUpdate(){
		int val = sldCoarse.getValue();
		if(val < 0){
			lblCoarse.setText(val + " semitone(s)");
		}
		else if(val > 0){
			lblCoarse.setText("+" + val +" semitone(s)");
		}
		else lblCoarse.setText("0 semitone(s)");
		lblCoarse.repaint();
	}

	private void onFineTuneUpdate(){
		int val = sldFine.getValue();
		if(val < 0){
			lblFine.setText(val + " cent(s)");
		}
		else if(val > 0){
			lblFine.setText("+" + val +" cent(s)");
		}
		else lblFine.setText("0 cent(s)");
		lblFine.repaint();
	}
	
	private void onListItemSelect(){
		SFXNode sel = lstSFX.getSelectedValue();
		updateSlotEditGUI(sel);
	}
	
}
