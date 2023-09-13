package waffleoRai_zeqer64.GUI.abldEdit;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.WRPanel;
import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_zeqer64.ZeqerUtils;
import waffleoRai_zeqer64.GUI.CacheTypeCombobox;
import waffleoRai_zeqer64.GUI.MediumTypeCombobox;
import waffleoRai_zeqer64.engine.EngineWaveArcInfo;
import waffleoRai_zeqer64.filefmt.AbldFile;
import waffleoRai_zeqer64.filefmt.ZeqerWaveTable.WaveTableEntry;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;

import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.border.BevelBorder;
import java.awt.Font;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;

public class WarcSlotEditPanel extends WRPanel{

	private static final long serialVersionUID = 5750603475356365458L;
	
	/*----- Inner Classes -----*/
	
	private class ArcListNode{
		public int index;
		public EngineWaveArcInfo warc;
		
		public String toString(){
			String s = index + " - ";
			if(warc != null){
				String n = warc.getName();
				if(n != null) s += n;
				else s += "[Untitled]";
			}
			else s += "<empty>";
			return s;
		}
	}
	
	private class SampleListNode{
		public WaveTableEntry waveMeta;
		public int index;
		
		public String toString(){
			String s = String.format("%04d - ", index);
			if(waveMeta != null){
				String name = waveMeta.getName();
				if(name != null) s += name;
				else s += "[Untitled]";
			}
			else s += "<empty>";
			return s;
		}
	}
	
	/*----- Instance Variables -----*/
	
	private ZeqerCoreInterface core;
	private boolean readOnly;

	//Hold copies.
	private ArrayList<ArcListNode> allArcs;
	private ArrayList<SampleListNode> arcSamps;
	
	private ComponentGroup cgEditable; //Enabled if not readOnly
	private ComponentGroup cgArcInfo; //Enabled on arc select
	private ComponentGroup cgNotRef; //Enabled if selected arc is not a reference
	private ComponentGroup cgSampSel; //Enabled on sample select
	
	private JList<ArcListNode> lstArcs;
	private JList<SampleListNode> lstSamps;
	
	private JTextField txtArcName;
	private JTextField txtEnum;
	private MediumTypeCombobox cmbxMedium;
	private CacheTypeCombobox cmbxCache;
	
	private volatile boolean cmbxRefAutoMode = false;
	private JCheckBox cbRefArc;
	private JComboBox<ArcListNode> cmbxRefArc;

	/*----- Init -----*/
	
	public WarcSlotEditPanel(ZeqerCoreInterface coreIFace, boolean readOnly){
		core = coreIFace;
		this.readOnly = readOnly;
		cgEditable = globalEnable.newChild();
		cgArcInfo = cgEditable.newChild();
		cgNotRef = cgArcInfo.newChild();
		cgSampSel = cgNotRef.newChild();
		
		initGUI();
	}
	
	private void initGUI(){
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JPanel pnlWarcs = new JPanel();
		pnlWarcs.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlWarcs = new GridBagConstraints();
		gbc_pnlWarcs.insets = new Insets(0, 0, 0, 5);
		gbc_pnlWarcs.fill = GridBagConstraints.BOTH;
		gbc_pnlWarcs.gridx = 0;
		gbc_pnlWarcs.gridy = 0;
		add(pnlWarcs, gbc_pnlWarcs);
		GridBagLayout gbl_pnlWarcs = new GridBagLayout();
		gbl_pnlWarcs.columnWidths = new int[]{0, 0};
		gbl_pnlWarcs.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_pnlWarcs.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlWarcs.rowWeights = new double[]{0.0, 2.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		pnlWarcs.setLayout(gbl_pnlWarcs);
		
		JLabel lblArchives = new JLabel("Archives");
		lblArchives.setFont(new Font("Tahoma", Font.BOLD, 13));
		GridBagConstraints gbc_lblArchives = new GridBagConstraints();
		gbc_lblArchives.insets = new Insets(0, 0, 5, 0);
		gbc_lblArchives.gridx = 0;
		gbc_lblArchives.gridy = 0;
		pnlWarcs.add(lblArchives, gbc_lblArchives);
		
		JScrollPane spArcs = new JScrollPane();
		spArcs.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spArcs = new GridBagConstraints();
		gbc_spArcs.insets = new Insets(0, 5, 5, 5);
		gbc_spArcs.fill = GridBagConstraints.BOTH;
		gbc_spArcs.gridx = 0;
		gbc_spArcs.gridy = 1;
		pnlWarcs.add(spArcs, gbc_spArcs);
		globalEnable.addComponent("spArcs", spArcs);
		
		lstArcs = new JList<ArcListNode>();
		lstArcs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		spArcs.setViewportView(lstArcs);
		globalEnable.addComponent("lstArcs", lstArcs);
		lstArcs.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e) {
				lstArcSelectCallback();
			}});
		
		JPanel pnlArcBtn = new JPanel();
		GridBagConstraints gbc_pnlArcBtn = new GridBagConstraints();
		gbc_pnlArcBtn.insets = new Insets(0, 5, 5, 5);
		gbc_pnlArcBtn.fill = GridBagConstraints.BOTH;
		gbc_pnlArcBtn.gridx = 0;
		gbc_pnlArcBtn.gridy = 2;
		pnlWarcs.add(pnlArcBtn, gbc_pnlArcBtn);
		GridBagLayout gbl_pnlArcBtn = new GridBagLayout();
		gbl_pnlArcBtn.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_pnlArcBtn.rowHeights = new int[]{0, 0};
		gbl_pnlArcBtn.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlArcBtn.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlArcBtn.setLayout(gbl_pnlArcBtn);
		
		JButton btnUpA = new JButton("^");
		GridBagConstraints gbc_btnUpA = new GridBagConstraints();
		gbc_btnUpA.insets = new Insets(0, 0, 0, 5);
		gbc_btnUpA.gridx = 0;
		gbc_btnUpA.gridy = 0;
		pnlArcBtn.add(btnUpA, gbc_btnUpA);
		cgArcInfo.addComponent("btnUpA", btnUpA);
		btnUpA.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnArcUpCallback();
			}});
		
		JButton btnDownA = new JButton("v");
		GridBagConstraints gbc_btnDownA = new GridBagConstraints();
		gbc_btnDownA.insets = new Insets(0, 0, 0, 5);
		gbc_btnDownA.gridx = 1;
		gbc_btnDownA.gridy = 0;
		pnlArcBtn.add(btnDownA, gbc_btnDownA);
		cgArcInfo.addComponent("btnDownA", btnDownA);
		btnDownA.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnArcDownCallback();
			}});
		
		JButton btnAddA = new JButton("+");
		GridBagConstraints gbc_btnAddA = new GridBagConstraints();
		gbc_btnAddA.insets = new Insets(0, 0, 0, 5);
		gbc_btnAddA.gridx = 3;
		gbc_btnAddA.gridy = 0;
		pnlArcBtn.add(btnAddA, gbc_btnAddA);
		cgEditable.addComponent("btnAddA", btnAddA);
		btnAddA.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnArcPlusCallback();
			}});
		
		JButton btnDeleteA = new JButton("-");
		GridBagConstraints gbc_btnDeleteA = new GridBagConstraints();
		gbc_btnDeleteA.gridx = 4;
		gbc_btnDeleteA.gridy = 0;
		pnlArcBtn.add(btnDeleteA, gbc_btnDeleteA);
		cgArcInfo.addComponent("btnDeleteA", btnDeleteA);
		btnDeleteA.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnArcMinusCallback();
			}});
		
		JPanel pnlArcEdit = new JPanel();
		GridBagConstraints gbc_pnlArcEdit = new GridBagConstraints();
		gbc_pnlArcEdit.insets = new Insets(0, 5, 5, 5);
		gbc_pnlArcEdit.fill = GridBagConstraints.BOTH;
		gbc_pnlArcEdit.gridx = 0;
		gbc_pnlArcEdit.gridy = 3;
		pnlWarcs.add(pnlArcEdit, gbc_pnlArcEdit);
		GridBagLayout gbl_pnlArcEdit = new GridBagLayout();
		gbl_pnlArcEdit.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlArcEdit.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_pnlArcEdit.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlArcEdit.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlArcEdit.setLayout(gbl_pnlArcEdit);
		
		JLabel lblName = new JLabel("Name:");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		pnlArcEdit.add(lblName, gbc_lblName);
		
		txtArcName = new JTextField();
		GridBagConstraints gbc_txtArcName = new GridBagConstraints();
		gbc_txtArcName.gridwidth = 2;
		gbc_txtArcName.insets = new Insets(0, 0, 5, 0);
		gbc_txtArcName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtArcName.gridx = 1;
		gbc_txtArcName.gridy = 0;
		pnlArcEdit.add(txtArcName, gbc_txtArcName);
		txtArcName.setColumns(10);
		cgArcInfo.addComponent("txtArcName", txtArcName);
		
		JLabel lblEnum = new JLabel("Enum:");
		GridBagConstraints gbc_lblEnum = new GridBagConstraints();
		gbc_lblEnum.anchor = GridBagConstraints.EAST;
		gbc_lblEnum.insets = new Insets(0, 0, 5, 5);
		gbc_lblEnum.gridx = 0;
		gbc_lblEnum.gridy = 1;
		pnlArcEdit.add(lblEnum, gbc_lblEnum);
		
		txtEnum = new JTextField();
		GridBagConstraints gbc_txtEnum = new GridBagConstraints();
		gbc_txtEnum.gridwidth = 2;
		gbc_txtEnum.insets = new Insets(0, 0, 5, 0);
		gbc_txtEnum.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtEnum.gridx = 1;
		gbc_txtEnum.gridy = 1;
		pnlArcEdit.add(txtEnum, gbc_txtEnum);
		txtEnum.setColumns(10);
		cgArcInfo.addComponent("txtEnum", txtEnum);
		
		JLabel lblMedium = new JLabel("Medium:");
		GridBagConstraints gbc_lblMedium = new GridBagConstraints();
		gbc_lblMedium.anchor = GridBagConstraints.EAST;
		gbc_lblMedium.insets = new Insets(0, 0, 5, 5);
		gbc_lblMedium.gridx = 0;
		gbc_lblMedium.gridy = 2;
		pnlArcEdit.add(lblMedium, gbc_lblMedium);
		
		cmbxMedium = new MediumTypeCombobox();
		GridBagConstraints gbc_cmbxMedium = new GridBagConstraints();
		gbc_cmbxMedium.gridwidth = 2;
		gbc_cmbxMedium.insets = new Insets(0, 0, 5, 0);
		gbc_cmbxMedium.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxMedium.gridx = 1;
		gbc_cmbxMedium.gridy = 2;
		pnlArcEdit.add(cmbxMedium, gbc_cmbxMedium);
		cgArcInfo.addComponent("cmbxMedium", cmbxMedium);
		
		JLabel lblCachePolicy = new JLabel("Cache Policy:");
		GridBagConstraints gbc_lblCachePolicy = new GridBagConstraints();
		gbc_lblCachePolicy.anchor = GridBagConstraints.EAST;
		gbc_lblCachePolicy.insets = new Insets(0, 0, 5, 5);
		gbc_lblCachePolicy.gridx = 0;
		gbc_lblCachePolicy.gridy = 3;
		pnlArcEdit.add(lblCachePolicy, gbc_lblCachePolicy);
		
		cmbxCache = new CacheTypeCombobox();
		GridBagConstraints gbc_cmbxCache = new GridBagConstraints();
		gbc_cmbxCache.gridwidth = 2;
		gbc_cmbxCache.insets = new Insets(0, 0, 5, 0);
		gbc_cmbxCache.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxCache.gridx = 1;
		gbc_cmbxCache.gridy = 3;
		pnlArcEdit.add(cmbxCache, gbc_cmbxCache);
		cgArcInfo.addComponent("cmbxCache", cmbxCache);
		
		JButton btnApply = new JButton("Apply");
		GridBagConstraints gbc_btnApply = new GridBagConstraints();
		gbc_btnApply.gridx = 2;
		gbc_btnApply.gridy = 4;
		pnlArcEdit.add(btnApply, gbc_btnApply);
		btnApply.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnApplyCallback();
			}});
		cgArcInfo.addComponent("btnApply", btnApply);
		
		JPanel pnlSamps = new JPanel();
		pnlSamps.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlSamps = new GridBagConstraints();
		gbc_pnlSamps.fill = GridBagConstraints.BOTH;
		gbc_pnlSamps.gridx = 1;
		gbc_pnlSamps.gridy = 0;
		add(pnlSamps, gbc_pnlSamps);
		GridBagLayout gbl_pnlSamps = new GridBagLayout();
		gbl_pnlSamps.columnWidths = new int[]{0, 0};
		gbl_pnlSamps.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_pnlSamps.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlSamps.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		pnlSamps.setLayout(gbl_pnlSamps);
		
		JLabel lblSamples = new JLabel("Samples");
		lblSamples.setFont(new Font("Tahoma", Font.BOLD, 13));
		GridBagConstraints gbc_lblSamples = new GridBagConstraints();
		gbc_lblSamples.insets = new Insets(0, 0, 5, 0);
		gbc_lblSamples.gridx = 0;
		gbc_lblSamples.gridy = 0;
		pnlSamps.add(lblSamples, gbc_lblSamples);
		
		JPanel pnlRef = new JPanel();
		GridBagConstraints gbc_pnlRef = new GridBagConstraints();
		gbc_pnlRef.insets = new Insets(0, 5, 5, 5);
		gbc_pnlRef.fill = GridBagConstraints.BOTH;
		gbc_pnlRef.gridx = 0;
		gbc_pnlRef.gridy = 1;
		pnlSamps.add(pnlRef, gbc_pnlRef);
		GridBagLayout gbl_pnlRef = new GridBagLayout();
		gbl_pnlRef.columnWidths = new int[]{0, 0, 0};
		gbl_pnlRef.rowHeights = new int[]{0, 0};
		gbl_pnlRef.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlRef.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlRef.setLayout(gbl_pnlRef);
		
		cbRefArc = new JCheckBox("Reference:");
		GridBagConstraints gbc_cbRefArc = new GridBagConstraints();
		gbc_cbRefArc.insets = new Insets(0, 0, 0, 5);
		gbc_cbRefArc.gridx = 0;
		gbc_cbRefArc.gridy = 0;
		pnlRef.add(cbRefArc, gbc_cbRefArc);
		cgArcInfo.addComponent("cbRefArc", cbRefArc);
		cbRefArc.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				cbRefCallback();
			}});
		
		cmbxRefArc = new JComboBox<ArcListNode>();
		GridBagConstraints gbc_cmbxRefArc = new GridBagConstraints();
		gbc_cmbxRefArc.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxRefArc.gridx = 1;
		gbc_cmbxRefArc.gridy = 0;
		pnlRef.add(cmbxRefArc, gbc_cmbxRefArc);
		cgArcInfo.addComponent("cmbxRefArc", cmbxRefArc);
		cmbxRefArc.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				cmbxRefCallback(!cmbxRefAutoMode);
			}});
		
		JScrollPane spSamps = new JScrollPane();
		spSamps.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spSamps = new GridBagConstraints();
		gbc_spSamps.insets = new Insets(0, 5, 5, 5);
		gbc_spSamps.fill = GridBagConstraints.BOTH;
		gbc_spSamps.gridx = 0;
		gbc_spSamps.gridy = 2;
		pnlSamps.add(spSamps, gbc_spSamps);
		cgArcInfo.addComponent("spSamps", spSamps);
		
		lstSamps = new JList<SampleListNode>();
		spSamps.setViewportView(lstSamps);
		cgArcInfo.addComponent("lstSamps", lstSamps);
		
		JPanel pnlSampBtn = new JPanel();
		GridBagConstraints gbc_pnlSampBtn = new GridBagConstraints();
		gbc_pnlSampBtn.insets = new Insets(0, 5, 5, 5);
		gbc_pnlSampBtn.fill = GridBagConstraints.BOTH;
		gbc_pnlSampBtn.gridx = 0;
		gbc_pnlSampBtn.gridy = 3;
		pnlSamps.add(pnlSampBtn, gbc_pnlSampBtn);
		GridBagLayout gbl_pnlSampBtn = new GridBagLayout();
		gbl_pnlSampBtn.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_pnlSampBtn.rowHeights = new int[]{0, 0};
		gbl_pnlSampBtn.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlSampBtn.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlSampBtn.setLayout(gbl_pnlSampBtn);
		
		JButton btnUpS = new JButton("^");
		GridBagConstraints gbc_btnUpS = new GridBagConstraints();
		gbc_btnUpS.insets = new Insets(0, 0, 0, 5);
		gbc_btnUpS.gridx = 0;
		gbc_btnUpS.gridy = 0;
		pnlSampBtn.add(btnUpS, gbc_btnUpS);
		cgSampSel.addComponent("btnUpS", btnUpS);
		btnUpS.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnSampUpCallback();
			}});
		
		JButton btnDownS = new JButton("v");
		GridBagConstraints gbc_btnDownS = new GridBagConstraints();
		gbc_btnDownS.insets = new Insets(0, 0, 0, 5);
		gbc_btnDownS.gridx = 1;
		gbc_btnDownS.gridy = 0;
		pnlSampBtn.add(btnDownS, gbc_btnDownS);
		cgSampSel.addComponent("btnDownS", btnDownS);
		btnDownS.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnSampDownCallback();
			}});
		
		JButton btnAddS = new JButton("+");
		GridBagConstraints gbc_btnAddS = new GridBagConstraints();
		gbc_btnAddS.insets = new Insets(0, 0, 0, 5);
		gbc_btnAddS.gridx = 3;
		gbc_btnAddS.gridy = 0;
		pnlSampBtn.add(btnAddS, gbc_btnAddS);
		cgNotRef.addComponent("btnAddS", btnAddS);
		btnAddS.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnSampPlusCallback();
			}});
		
		JButton btnDeleteS = new JButton("-");
		GridBagConstraints gbc_btnDeleteS = new GridBagConstraints();
		gbc_btnDeleteS.gridx = 4;
		gbc_btnDeleteS.gridy = 0;
		pnlSampBtn.add(btnDeleteS, gbc_btnDeleteS);
		cgSampSel.addComponent("btnDeleteS", btnDeleteS);
		btnDeleteS.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnSampMinusCallback();
			}});
	}
	
	/*----- Getters -----*/
	
	public List<EngineWaveArcInfo> getContents(){
		if(allArcs == null) return null;
		int wcount = allArcs.size();
		List<EngineWaveArcInfo> out = new ArrayList<EngineWaveArcInfo>(wcount);
		for(ArcListNode node : allArcs){
			out.add(node.warc);
		}
		return out;
	}
	
	/*----- Setters -----*/
	
	public void loadAbld(AbldFile abld){
		//Copy arc structures...
		if(allArcs != null) allArcs.clear();
		if(abld != null){
			List<EngineWaveArcInfo> warcs = abld.getWaveArcs();
			if(allArcs == null){
				allArcs = new ArrayList<ArcListNode>(warcs.size()+4);
			}
			else allArcs.ensureCapacity(warcs.size()+4);
			
			int i = 0;
			for(EngineWaveArcInfo warc : warcs){
				ArcListNode node = new ArcListNode();
				node.index = i++;
				node.warc = warc.copy();
				allArcs.add(node);
			}
		}
		
		//Update GUI
		updateArcGUILists();
		loadArcInfoToGUI(null);
	}
	
	/*----- Draw -----*/
	
	private void updateArcGUILists(){
		//Update GUI to match backing list
		lstArcs.setSelectedIndex(-1);
		DefaultListModel<ArcListNode> lmdl = new DefaultListModel<ArcListNode>();
		if(allArcs != null){
			for(ArcListNode node : allArcs){
				lmdl.addElement(node);
			}
		}
		lstArcs.setModel(lmdl);
		
		//Also update the ref combobox
		DefaultComboBoxModel<ArcListNode> cmdl = new DefaultComboBoxModel<ArcListNode>();
		if(allArcs != null){
			for(ArcListNode node : allArcs){
				cmdl.addElement(node);
			}
		}
		cmbxRefArc.setModel(cmdl);
		if(allArcs != null && !allArcs.isEmpty()){
			setRefComboboxSelectionIndex(findFirstReferencableArc(-1));
		}
	}
	
	private void updateSampleGUIList(){
		//Update GUI to match backing list
		lstSamps.setSelectedIndex(-1);
		DefaultListModel<SampleListNode> mdl = new DefaultListModel<SampleListNode>();
		if(arcSamps != null){
			for(SampleListNode node : arcSamps){
				mdl.addElement(node);
			}
		}
		lstSamps.setModel(mdl);
	}
	
	private void loadArcInfoToGUI(ArcListNode node){
		if(node != null){
			if(node.warc == null){
				//Generate one?
				node.warc = new EngineWaveArcInfo(16);
			}
			
			String s = node.warc.getName();
			if(s != null) txtArcName.setText(s);
			else txtArcName.setText("Untitled Archive " + String.format("%02d", node.index));
			
			s = node.warc.getEnumString();
			if(s != null) txtEnum.setText(s);
			else txtEnum.setText("WAVE_ARC_" + String.format("%02d", node.index));
			
			int refidx = node.warc.getRefIndex();
			if(refidx >= 0){
				//Ref
				cbRefArc.setSelected(true);
				setRefComboboxSelectionIndex(refidx);
				//Get samps from referenced warc
				ArcListNode ref = allArcs.get(refidx);
				if(ref != null && ref.warc != null){
					loadSampleListFrom(ref);
				}
			}
			else{
				cbRefArc.setSelected(false);
				setRefComboboxSelectionIndex(findFirstReferencableArc(-1));
				loadSampleListFrom(node);
			}
		}
		else{
			txtArcName.setText("");
			txtEnum.setText("");
			cbRefArc.setSelected(false);
			loadSampleListFrom(null);
		}
		
		reenable();
	}
		
	public void reenable(){
		//Override to make sure correct subgroups enabled.
		globalEnable.setEnabling(true);
		if(!readOnly) cgEditable.setEnabling(false);
		else{
			//Check list selection.
			if(lstArcs.isSelectionEmpty()){
				cgArcInfo.setEnabling(false);
			}
			else{
				boolean refenabled = findFirstReferencableArc(lstArcs.getSelectedIndex()) >= 0;
				boolean isref = cbRefArc.isSelected() && refenabled;
				
				//Disable cbArcRef if not refable
				cgNotRef.setEnabling(!isref);
				cmbxRefArc.setEnabled(isref);
				if(!refenabled){
					cbRefArc.setSelected(false);
					cbRefArc.setEnabled(false);
				}
				
				if(lstSamps.isSelectionEmpty()){
					cgSampSel.setEnabling(false);
				}
			}
		}
		globalEnable.repaint();
	}
	
	/*----- Internal -----*/
	
	private void swapArcsInList(int srcidx, int trgidx){
		ArcListNode src = allArcs.get(srcidx);
		ArcListNode trg = allArcs.get(trgidx);
		src.index = trgidx;
		trg.index = srcidx;
		allArcs.set(trgidx, src);
		allArcs.set(srcidx, trg);
		
		for(ArcListNode node : allArcs){
			if(node == null) continue;
			if(node.warc == null) continue;
			int ref = node.warc.getRefIndex();
			if(ref == trgidx) node.warc.setRefIndex(srcidx);
			else if(ref == srcidx) node.warc.setRefIndex(trgidx);
		}
		
		updateArcGUILists();
		lstArcs.setSelectedIndex(trgidx);
		loadArcInfoToGUI(src);
	}
	
	private int findFirstReferencableArc(int arcIndex){
		//Will only do if there is another non-ref arc available to reference.
		if(allArcs != null){
			for(ArcListNode node : allArcs){
				if(node.index != arcIndex){
					if(node.warc != null){
						if(!node.warc.isReference()) return node.index;
					}
				}
			}
		}
		return -1;
	}
	
	private void setRefComboboxSelectionIndex(int value){
		cmbxRefAutoMode = true;
		cmbxRefArc.setSelectedIndex(value);
		cmbxRefAutoMode = false;
	}
	
	private void loadSampleListFrom(ArcListNode node){
		if(arcSamps != null) arcSamps.clear();
		if(node == null || node.warc == null){
			return;
		}
	
		List<Integer> samps = node.warc.getSamples();
		
		//Load sample list
		if(samps != null){
			if(arcSamps == null) arcSamps = new ArrayList<SampleListNode>(samps.size()+1);
			else arcSamps.ensureCapacity(samps.size() + 1);
			
			int i = 0;
			for(Integer sampId : samps){
				SampleListNode snode = new SampleListNode();
				snode.index = i++;
				if(core != null){
					if((sampId != 0) && (sampId != -1)){
						snode.waveMeta = core.getWaveTableEntry(sampId);
					}
				}
				arcSamps.add(snode);
			}
		}
		
		updateSampleGUIList();
	}
	
	private void updateArcInfoFromGUI(ArcListNode node){
		if(node == null || node.warc == null) return;
		
		//Basic metadata
		String s = txtArcName.getText();
		if(s == null || s.isEmpty()){
			s = "Wave Archive " + node.index;
			txtArcName.setText(s);
			txtArcName.repaint();
		}
		node.warc.setName(s);
		
		s = txtEnum.getText();
		if(s == null || s.isEmpty()){
			s = "WAVE_ARC_" + String.format("%02d", node.index);
		}
		s = ZeqerUtils.fixHeaderEnumID(s, true);
		txtEnum.setText(s);
		txtEnum.repaint();
		node.warc.setEnumString(s);
		
		node.warc.setCachePolicy(cmbxCache.getRawValue());
		node.warc.setMedium(cmbxMedium.getRawValue());
		
		//Reference, if reference.
		if(cbRefArc.isSelected()){
			//TODO Check to make sure it isn't trying to reference itself or another reference.
			node.warc.setRefIndex(cmbxRefArc.getSelectedIndex());
		}
		else{
			node.warc.setRefIndex(-1);
			//Samples (if not reference)
			
			node.warc.clearSamples();
			if(arcSamps != null && !arcSamps.isEmpty()){
				node.warc.updateCapacity(arcSamps.size());
				for(SampleListNode samp : arcSamps){
					if(samp.waveMeta != null){
						node.warc.addSample(samp.waveMeta.getUID());
					}
					else node.warc.addSample(0);
				}
			}
		}
	}
	
	/*----- Callbacks -----*/
	
	private void lstArcSelectCallback(){
		setWait();
		loadArcInfoToGUI(lstArcs.getSelectedValue());
		unsetWait();
	}
	
	private void btnArcUpCallback(){
		if(readOnly) return;
		if(lstArcs.isSelectionEmpty()) return;
		
		int selidx = lstArcs.getSelectedIndex();
		if(selidx <= 0) return;
		
		setWait();
		int newidx = selidx - 1;
		swapArcsInList(selidx, newidx);
		unsetWait();
	}
	
	private void btnArcDownCallback(){
		if(readOnly) return;
		if(lstArcs.isSelectionEmpty()) return;
		
		int selidx = lstArcs.getSelectedIndex();
		int maxidx = allArcs.size() - 1;
		if(selidx >= maxidx) return;
		
		setWait();
		int newidx = selidx + 1;
		swapArcsInList(selidx, newidx);
		unsetWait();
	}
	
	private void btnArcPlusCallback(){
		if(readOnly) return;
		if(allArcs == null){
			allArcs = new ArrayList<ArcListNode>(8);
		}
		
		//Adds new arc to end of list
		int idx = allArcs.size() - 1;
		ArcListNode node = new ArcListNode();
		node.index = idx;
		node.warc = new EngineWaveArcInfo(16);
		node.warc.setName("Wave Archive " + idx);
		node.warc.setEnumString(String.format("WAVE_ARC_%02d", idx));
		node.warc.setMedium(Z64Sound.MEDIUM_RAM);
		node.warc.setMedium(Z64Sound.CACHE_ANYNOSYNCLOAD);

		updateArcGUILists();
		lstArcs.setSelectedIndex(idx);
		loadArcInfoToGUI(node);
	}
	
	private void btnArcMinusCallback(){
		if(readOnly) return;
		if(lstArcs.isSelectionEmpty()) return;
		
		//Deletes selected  (don't forget to adjust references)
		int selidx = lstArcs.getSelectedIndex();
		int ret = JOptionPane.showConfirmDialog(this, 
				"Remove the selected wave archive from build?", 
				"Remove Wave Archive", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		
		if(ret != JOptionPane.YES_OPTION) return;
		setWait();
		
		allArcs.remove(selidx);
		int j = 0;
		for(ArcListNode node : allArcs){
			node.index = j++;
			if(node.warc != null){
				int ref = node.warc.getRefIndex();
				if(ref == selidx){
					node.warc.setRefIndex(-1);
				}
			}
		}
		
		updateArcGUILists();
		loadArcInfoToGUI(null);
		unsetWait();
	}
	
	private void btnSampUpCallback(){
		if(readOnly) return;
		
		//Moves all selected up one.
		if(lstSamps.isSelectionEmpty()) return;
		int[] selidx = lstSamps.getSelectedIndices();
		if(selidx == null) return;
		
		setWait();
		int minidx = 0;
		int[] trgidx = new int[selidx.length];
		for(int i = 0; i < selidx.length; i++){
			trgidx[i] = selidx[i] - 1;
			if(trgidx[i] < minidx){
				trgidx[i] = minidx;
				minidx++;
			}
		}
		
		for(int i = 0; i < trgidx.length; i++){
			SampleListNode sel = arcSamps.get(selidx[i]);
			SampleListNode target = arcSamps.get(trgidx[i]);
			arcSamps.set(trgidx[i], sel);
			arcSamps.set(selidx[i], target);
			sel.index = trgidx[i];
			target.index = selidx[i];
		}
		
		updateSampleGUIList();
		//Don't forget to make sure that the same ones selected before are still selected after list refresh...
		lstSamps.setSelectedIndices(trgidx);
		unsetWait();
	}
	
	private void btnSampDownCallback(){
		if(readOnly) return;
		
		//Moves all selected down one.
		if(lstSamps.isSelectionEmpty()) return;
		int[] selidx = lstSamps.getSelectedIndices();
		if(selidx == null) return;
		
		setWait();
		int maxidx = arcSamps.size() - 1;
		int[] trgidx = new int[selidx.length];
		for(int i = trgidx.length; i >= 0; i--){
			trgidx[i] = selidx[i] + 1;
			if(trgidx[i] > maxidx){
				trgidx[i] = maxidx;
				maxidx--;
			}
		}
		
		for(int i = 0; i < trgidx.length; i++){
			SampleListNode sel = arcSamps.get(selidx[i]);
			SampleListNode target = arcSamps.get(trgidx[i]);
			arcSamps.set(trgidx[i], sel);
			arcSamps.set(selidx[i], target);
			sel.index = trgidx[i];
			target.index = selidx[i];
		}
		
		updateSampleGUIList();
		//Don't forget to make sure that the same ones selected before are still selected after list refresh...
		lstSamps.setSelectedIndices(trgidx);
		unsetWait();
	}
	
	private void btnSampPlusCallback(){
		//TODO
		if(readOnly) return;
		//Will insert new sample(s) after last selected entry, or at end of no selections.
		//Brings up select sample dialog to create new entry...
		dummyCallback();
	}
	
	private void btnSampMinusCallback(){
		//Can do multiple selection. This deletes all after asking user.
		if(readOnly) return;
		if(lstSamps.isSelectionEmpty()) return;
		
		int[] selidx = lstSamps.getSelectedIndices();
		if(selidx == null) return;
		
		int ret = JOptionPane.showConfirmDialog(this, 
				"Remove the selected " + selidx.length + " sample(s) from wave archive?", 
				"Remove Samples", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		
		if(ret != JOptionPane.YES_OPTION) return;
		
		setWait();
		List<SampleListNode> temp = new ArrayList<SampleListNode>(arcSamps.size());
		temp.addAll(arcSamps);
		arcSamps.clear();
		
		int i = 0;
		int j = 0;
		for(SampleListNode n : temp){
			boolean okay = true;
			for(int k = 0; k < selidx.length; k++){
				if(selidx[k] == i){
					okay = false;
					break;
				}
			}
			
			if(okay){
				arcSamps.add(n);
				n.index = j++;
			}
			i++;
		}
		
		updateSampleGUIList();
		unsetWait();
	}
	
	private void btnApplyCallback(){
		if(readOnly) return;
		if(lstArcs.isSelectionEmpty()) return;
		
		setWait();
		updateArcInfoFromGUI(lstArcs.getSelectedValue());
		unsetWait();
	}
	
	private void cbRefCallback(){
		if(readOnly) return;
		
		//Swap between stored sample list and referenced
		ArcListNode arc = lstArcs.getSelectedValue();
		if(arc != null){
			if(cbRefArc.isSelected()){
				int defotrg = findFirstReferencableArc(arc.index);
				if(defotrg < 0){
					cbRefArc.setSelected(false);
					cbRefArc.setEnabled(false);
					showWarning("No other archives to reference!");
				}
				else{
					//Okay, can switch to reference. Look for first referencable arc.
					setRefComboboxSelectionIndex(defotrg);
					ArcListNode trg = allArcs.get(defotrg);
					loadSampleListFrom(trg);
				}
			}
			else{
				//Reference off
				loadSampleListFrom(arc);
			}
		}
		else{
			loadArcInfoToGUI(null);
		}
		reenable();
	}
	
	private void cmbxRefCallback(boolean manual){
		if(!manual) return;
		if(readOnly) return;
		
		if(cbRefArc.isSelected() && !lstArcs.isSelectionEmpty()){
			//Update the sample list...
			int sel = cmbxRefArc.getSelectedIndex();
			if(sel >= 0){
				ArcListNode arc = lstArcs.getSelectedValue();
				if(arc != null){
					//Check if valid.
					boolean valid = arc.index != sel;
					
					ArcListNode trg = allArcs.get(sel);
					valid = valid && trg.warc.isReference();
					
					if(valid) loadSampleListFrom(trg);
					else{
						showWarning("Reference target must be a non-reference archive.");
						int nidx = findFirstReferencableArc(arc.index);
						if(nidx < 0){
							setRefComboboxSelectionIndex(-1);
							cbRefArc.setSelected(false);
							cbRefArc.setEnabled(false);
							loadSampleListFrom(arc);
						}
						else{
							setRefComboboxSelectionIndex(nidx);
							trg = allArcs.get(nidx);
							loadSampleListFrom(trg);
						}
					}
				}
			}
			else{
				loadSampleListFrom(null);
			}
		}
		reenable();
	}
	
}
