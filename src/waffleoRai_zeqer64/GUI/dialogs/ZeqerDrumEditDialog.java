package waffleoRai_zeqer64.GUI.dialogs;

import javax.swing.JDialog;
import javax.swing.JFrame;

import java.awt.GridBagLayout;
import javax.swing.JPanel;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.border.BevelBorder;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.JLabel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_Sound.nintendo.Z64Sound.Z64Tuning;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_soundbank.nintendo.z64.Z64Drum;
import waffleoRai_soundbank.nintendo.z64.Z64Envelope;
import waffleoRai_zeqer64.ZeqerCoreInterface;
import waffleoRai_zeqer64.ZeqerUtils;
import waffleoRai_zeqer64.GUI.dialogs.envedit.ZeqerEnvEditDialog;
import waffleoRai_zeqer64.filefmt.ZeqerWaveTable.WaveTableEntry;
import waffleoRai_zeqer64.presets.ZeqerPercPreset;
import waffleoRai_zeqer64.presets.ZeqerPercRegion;

import javax.swing.JSpinner;
import javax.swing.JSlider;
import javax.swing.ListSelectionModel;

public class ZeqerDrumEditDialog extends JDialog{
	
	//TODO Oh crap, do we want a play/preview panel/interface of some kind?
	//TODO You forgot edit tags :)
	
	private static final long serialVersionUID = 6232066596448455202L;
	
	public static final int DEFO_WIDTH = 625;
	public static final int DEFO_HEIGHT = 425;
	
	//protected static final int RELRB_INDEX_MILLIS = 0;
	//protected static final int RELRB_INDEX_RAW = 1;
	//protected static final int RELRB_COUNT = 2;
	
	public static final int MIN_NOTE_DRUM = Z64Sound.STDRANGE_BOTTOM;
	public static final int MAX_NOTE_DRUM = MIN_NOTE_DRUM + Z64Sound.STDRANGE_SIZE - 1;
	public static final int MIDDLE_C = 60;
	
	private static final int ERR_REGUPDATE_NONE = 0;
	private static final int ERR_REGUPDATE_NOREG = 1;
	private static final int ERR_REGUPDATE_ISECT_NOTERANGE = 2;
	private static final int ERR_REGUPDATE_REL_INVALID = 3;
	private static final int ERR_REGUPDATE_FINETUNE_INVALID = 4;
	private static final int ERR_REGUPDATE_NULL_SAMPLE = 5;
	private static final int ERR_REGUPDATE_NULL_ENV = 6;
	
	/*----- Inner Classes -----*/
	
	private static class DrumRegion implements Comparable<DrumRegion>{
		
		public String name = null;
		public String enumName = null;

		public int noteMin = MIN_NOTE_DRUM;
		public int noteMax = MAX_NOTE_DRUM;
		public int unityKey = MIDDLE_C;
		public int fineTune = 0;
		
		public int release = 0;
		public Z64Envelope env = null;
		
		public int pan = 64;
		
		public Z64WaveInfo sample = null;
		
		public boolean equals(Object o){
			return (o == this);
		}
		
		public int compareTo(DrumRegion o) {
			if(o == null) return 1;
			
			//If preset is valid, it should NOT go beyond noteMin.
			//But just in case.
			if(this.noteMin != o.noteMin) return this.noteMin - o.noteMin;
			if(this.noteMax != o.noteMax) return this.noteMax - o.noteMax;
			if(this.unityKey != o.unityKey) return this.unityKey - o.unityKey;
			if(this.name != null) return this.name.compareTo(o.name);
			
			return 0;
		}
		
		public String toString(){
			StringBuilder sb = new StringBuilder(1024);
			sb.append("[" + noteMin + " - " + noteMax + "] ");
			if(name == null){
				sb.append("<Untitled>");
			}
			else{
				sb.append(name);
			}
			return sb.toString();
		}
		
		public boolean noteRangeIntersects(DrumRegion o){
			//We cannot have intersecting note ranges.
			if(o == null) return false;
			if(this.noteMin >= o.noteMax) return false;
			if(this.noteMax <= o.noteMin) return false;
			return true;
		}
		
		public void generateName(){
			name = "AnonRegion_" + noteMin;
			enumName = "ANONDRUM_" + noteMin;
		}
		
	}
	
	/*----- Instance Variables -----*/
	
	private JFrame parent;
	private ZeqerCoreInterface core;
	
	private ComponentGroup globalEnable;
	private ComponentGroup regionEnable;
	
	//For enabling control
	private JButton btnDelete;
	private JTextField txtName;
	private JTextField txtRegName;
	private JTextField txtRegEnum;
	private JTextField txtSample;
	
	private JList<DrumRegion> lstRegions;
	
	private JSlider sldPan;
	private JSlider sldFineTune;
	private JSlider sldRelease;
	
	private JLabel lblPanSetting;
	private JLabel lblFineAmt;
	private JLabel lblRel;
	
	private JSpinner spnNoteMin;
	private JSpinner spnNoteMax;
	private JSpinner spnUnityKey;
	
	private ZeqerPercPreset loadedDrum;
	private List<DrumRegion> regions; //So can be easily edited without affecting the loaded drum until save is hit
	private DrumRegion selectedRegion;
	
	//These are held here so only put in region instance when user clicks "apply"
	private Z64WaveInfo selectedSample;
	private Z64Envelope tempEnvelope;
	
	/*----- Init -----*/
	
	public ZeqerDrumEditDialog(JFrame parent_frame, ZeqerCoreInterface core_link){
		super(parent_frame, true);
		core = core_link;
		parent = parent_frame;
		regions = new ArrayList<DrumRegion>(64);
		initGUI();
		
		loadRegionInfoToGUI(null);
	}
	
	private void initGUI(){
		globalEnable = new ComponentGroup();
		regionEnable = new ComponentGroup();
		
		setResizable(false);
		
		setMinimumSize(new Dimension(DEFO_WIDTH, DEFO_HEIGHT));
		setPreferredSize(new Dimension(DEFO_WIDTH, DEFO_HEIGHT));
		
		setTitle("Edit Drum Preset");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{250, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{3.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel pnlMeta = new JPanel();
		GridBagConstraints gbc_pnlMeta = new GridBagConstraints();
		gbc_pnlMeta.gridwidth = 2;
		gbc_pnlMeta.insets = new Insets(0, 0, 5, 0);
		gbc_pnlMeta.fill = GridBagConstraints.BOTH;
		gbc_pnlMeta.gridx = 0;
		gbc_pnlMeta.gridy = 0;
		getContentPane().add(pnlMeta, gbc_pnlMeta);
		GridBagLayout gridBagLayout_2 = new GridBagLayout();
		gridBagLayout_2.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gridBagLayout_2.rowHeights = new int[]{0, 0};
		gridBagLayout_2.columnWeights = new double[]{0.0, 1.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout_2.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlMeta.setLayout(gridBagLayout_2);
		
		JLabel lblName = new JLabel("Name:");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.insets = new Insets(5, 5, 5, 5);
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		pnlMeta.add(lblName, gbc_lblName);
		
		txtName = new JTextField();
		GridBagConstraints gbc_txtName = new GridBagConstraints();
		gbc_txtName.insets = new Insets(5, 0, 5, 5);
		gbc_txtName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtName.gridx = 1;
		gbc_txtName.gridy = 0;
		pnlMeta.add(txtName, gbc_txtName);
		txtName.setColumns(10);
		globalEnable.addComponent("txtName", txtName);
		
		JButton btnEditTags = new JButton("Edit Tags...");
		GridBagConstraints gbc_btnEditTags = new GridBagConstraints();
		gbc_btnEditTags.insets = new Insets(0, 0, 0, 5);
		gbc_btnEditTags.gridx = 4;
		gbc_btnEditTags.gridy = 0;
		pnlMeta.add(btnEditTags, gbc_btnEditTags);
		globalEnable.addComponent("btnEditTags", btnEditTags);
		btnEditTags.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {btnEditTagsCallback();}
		});
		
		JScrollPane spRegions = new JScrollPane();
		spRegions.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spRegions = new GridBagConstraints();
		gbc_spRegions.insets = new Insets(10, 10, 10, 10);
		gbc_spRegions.fill = GridBagConstraints.BOTH;
		gbc_spRegions.gridx = 0;
		gbc_spRegions.gridy = 1;
		getContentPane().add(spRegions, gbc_spRegions);
		globalEnable.addComponent("spRegions", spRegions);
		
		lstRegions = new JList<DrumRegion>();
		lstRegions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		spRegions.setViewportView(lstRegions);
		globalEnable.addComponent("lstRegions", lstRegions);
		
		JPanel pnlRegion = new JPanel();
		pnlRegion.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlRegion = new GridBagConstraints();
		gbc_pnlRegion.insets = new Insets(0, 0, 5, 5);
		gbc_pnlRegion.fill = GridBagConstraints.BOTH;
		gbc_pnlRegion.gridx = 1;
		gbc_pnlRegion.gridy = 1;
		getContentPane().add(pnlRegion, gbc_pnlRegion);
		GridBagLayout gridBagLayout_1 = new GridBagLayout();
		gridBagLayout_1.columnWidths = new int[]{0, 0};
		gridBagLayout_1.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout_1.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout_1.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlRegion.setLayout(gridBagLayout_1);
		
		JPanel pnlRegMeta = new JPanel();
		GridBagConstraints gbc_pnlRegMeta = new GridBagConstraints();
		gbc_pnlRegMeta.insets = new Insets(0, 0, 5, 0);
		gbc_pnlRegMeta.fill = GridBagConstraints.BOTH;
		gbc_pnlRegMeta.gridx = 0;
		gbc_pnlRegMeta.gridy = 0;
		pnlRegion.add(pnlRegMeta, gbc_pnlRegMeta);
		GridBagLayout gbl_pnlRegMeta = new GridBagLayout();
		gbl_pnlRegMeta.columnWidths = new int[]{0, 0, 0};
		gbl_pnlRegMeta.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_pnlRegMeta.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlRegMeta.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlRegMeta.setLayout(gbl_pnlRegMeta);
		
		JLabel lblRegionName = new JLabel("Region Name:");
		GridBagConstraints gbc_lblRegionName = new GridBagConstraints();
		gbc_lblRegionName.insets = new Insets(5, 5, 5, 5);
		gbc_lblRegionName.anchor = GridBagConstraints.EAST;
		gbc_lblRegionName.gridx = 0;
		gbc_lblRegionName.gridy = 0;
		pnlRegMeta.add(lblRegionName, gbc_lblRegionName);
		
		txtRegName = new JTextField();
		GridBagConstraints gbc_txtRegName = new GridBagConstraints();
		gbc_txtRegName.insets = new Insets(5, 0, 5, 5);
		gbc_txtRegName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtRegName.gridx = 1;
		gbc_txtRegName.gridy = 0;
		pnlRegMeta.add(txtRegName, gbc_txtRegName);
		txtRegName.setColumns(10);
		globalEnable.addComponent("txtRegName", txtRegName);
		regionEnable.addComponent("txtRegName", txtRegName);
		
		JLabel lblRegionEnum = new JLabel("Region Enum:");
		GridBagConstraints gbc_lblRegionEnum = new GridBagConstraints();
		gbc_lblRegionEnum.insets = new Insets(0, 5, 5, 5);
		gbc_lblRegionEnum.anchor = GridBagConstraints.EAST;
		gbc_lblRegionEnum.gridx = 0;
		gbc_lblRegionEnum.gridy = 1;
		pnlRegMeta.add(lblRegionEnum, gbc_lblRegionEnum);
		
		txtRegEnum = new JTextField();
		GridBagConstraints gbc_txtRegEnum = new GridBagConstraints();
		gbc_txtRegEnum.insets = new Insets(0, 0, 5, 5);
		gbc_txtRegEnum.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtRegEnum.gridx = 1;
		gbc_txtRegEnum.gridy = 1;
		pnlRegMeta.add(txtRegEnum, gbc_txtRegEnum);
		txtRegEnum.setColumns(10);
		globalEnable.addComponent("txtRegEnum", txtRegEnum);
		regionEnable.addComponent("txtRegEnum", txtRegEnum);
		
		JLabel lblSample = new JLabel("Sample:");
		GridBagConstraints gbc_lblSample = new GridBagConstraints();
		gbc_lblSample.insets = new Insets(0, 5, 5, 5);
		gbc_lblSample.anchor = GridBagConstraints.EAST;
		gbc_lblSample.gridx = 0;
		gbc_lblSample.gridy = 2;
		pnlRegMeta.add(lblSample, gbc_lblSample);
		
		txtSample = new JTextField();
		GridBagConstraints gbc_txtSample = new GridBagConstraints();
		gbc_txtSample.insets = new Insets(0, 0, 5, 5);
		gbc_txtSample.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtSample.gridx = 1;
		gbc_txtSample.gridy = 2;
		pnlRegMeta.add(txtSample, gbc_txtSample);
		txtSample.setColumns(10);
		globalEnable.addComponent("txtSample", txtSample);
		regionEnable.addComponent("txtSample", txtSample);
		
		JButton btnSample = new JButton("Set Sample...");
		GridBagConstraints gbc_btnSample = new GridBagConstraints();
		gbc_btnSample.gridx = 1;
		gbc_btnSample.gridy = 3;
		pnlRegMeta.add(btnSample, gbc_btnSample);
		globalEnable.addComponent("btnSample", btnSample);
		regionEnable.addComponent("btnSample", btnSample);
		btnSample.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {btnSampleCallback();}
		});
		
		JPanel pnlRegData = new JPanel();
		GridBagConstraints gbc_pnlRegData = new GridBagConstraints();
		gbc_pnlRegData.insets = new Insets(0, 0, 5, 0);
		gbc_pnlRegData.fill = GridBagConstraints.BOTH;
		gbc_pnlRegData.gridx = 0;
		gbc_pnlRegData.gridy = 1;
		pnlRegion.add(pnlRegData, gbc_pnlRegData);
		GridBagLayout gbl_pnlRegData = new GridBagLayout();
		gbl_pnlRegData.columnWidths = new int[]{0, 50, 75, 0};
		gbl_pnlRegData.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_pnlRegData.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlRegData.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlRegData.setLayout(gbl_pnlRegData);
		
		JLabel lblRelease = new JLabel("Release:");
		GridBagConstraints gbc_lblRelease = new GridBagConstraints();
		gbc_lblRelease.anchor = GridBagConstraints.EAST;
		gbc_lblRelease.insets = new Insets(5, 5, 5, 5);
		gbc_lblRelease.gridx = 0;
		gbc_lblRelease.gridy = 0;
		pnlRegData.add(lblRelease, gbc_lblRelease);
		
		sldRelease = new JSlider();
		GridBagConstraints gbc_sldRelease = new GridBagConstraints();
		gbc_sldRelease.fill = GridBagConstraints.BOTH;
		gbc_sldRelease.insets = new Insets(0, 0, 5, 5);
		gbc_sldRelease.gridx = 1;
		gbc_sldRelease.gridy = 0;
		pnlRegData.add(sldRelease, gbc_sldRelease);
		globalEnable.addComponent("sldRelease", sldRelease);
		regionEnable.addComponent("sldRelease", sldRelease);
		
		lblRel = new JLabel("[0] 0 ms");
		GridBagConstraints gbc_lblRel = new GridBagConstraints();
		gbc_lblRel.anchor = GridBagConstraints.WEST;
		gbc_lblRel.insets = new Insets(0, 0, 5, 0);
		gbc_lblRel.gridx = 2;
		gbc_lblRel.gridy = 0;
		pnlRegData.add(lblRel, gbc_lblRel);

		JButton btnEnv = new JButton("Edit Envelope...");
		GridBagConstraints gbc_btnEnv = new GridBagConstraints();
		gbc_btnEnv.insets = new Insets(0, 0, 5, 5);
		gbc_btnEnv.gridx = 1;
		gbc_btnEnv.gridy = 1;
		pnlRegData.add(btnEnv, gbc_btnEnv);
		globalEnable.addComponent("btnEnv", btnEnv);
		regionEnable.addComponent("btnEnv", btnEnv);
		btnEnv.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {btnEnvCallback();}
		});
		
		JLabel lblPan = new JLabel("Pan:");
		GridBagConstraints gbc_lblPan = new GridBagConstraints();
		gbc_lblPan.anchor = GridBagConstraints.EAST;
		gbc_lblPan.insets = new Insets(0, 0, 5, 5);
		gbc_lblPan.gridx = 0;
		gbc_lblPan.gridy = 2;
		pnlRegData.add(lblPan, gbc_lblPan);
		
		sldPan = new JSlider();
		GridBagConstraints gbc_sldPan = new GridBagConstraints();
		gbc_sldPan.fill = GridBagConstraints.HORIZONTAL;
		gbc_sldPan.insets = new Insets(0, 0, 5, 5);
		gbc_sldPan.gridx = 1;
		gbc_sldPan.gridy = 2;
		pnlRegData.add(sldPan, gbc_sldPan);
		globalEnable.addComponent("sldPan", sldPan);
		regionEnable.addComponent("sldPan", sldPan);
		
		lblPanSetting = new JLabel("Center");
		GridBagConstraints gbc_lblPanSetting = new GridBagConstraints();
		gbc_lblPanSetting.anchor = GridBagConstraints.WEST;
		gbc_lblPanSetting.insets = new Insets(0, 0, 5, 0);
		gbc_lblPanSetting.gridx = 2;
		gbc_lblPanSetting.gridy = 2;
		pnlRegData.add(lblPanSetting, gbc_lblPanSetting);
		
		initTuningPanel(pnlRegData);
		
		JLabel lblFineTunecents = new JLabel("Fine Tune:");
		GridBagConstraints gbc_lblFineTunecents = new GridBagConstraints();
		gbc_lblFineTunecents.insets = new Insets(0, 5, 0, 5);
		gbc_lblFineTunecents.anchor = GridBagConstraints.EAST;
		gbc_lblFineTunecents.gridx = 0;
		gbc_lblFineTunecents.gridy = 4;
		pnlRegData.add(lblFineTunecents, gbc_lblFineTunecents);
		
		sldFineTune = new JSlider();
		GridBagConstraints gbc_sldFineTune = new GridBagConstraints();
		gbc_sldFineTune.fill = GridBagConstraints.BOTH;
		gbc_sldFineTune.insets = new Insets(0, 0, 0, 5);
		gbc_sldFineTune.gridx = 1;
		gbc_sldFineTune.gridy = 4;
		pnlRegData.add(sldFineTune, gbc_sldFineTune);
		globalEnable.addComponent("sldFineTune", sldFineTune);
		regionEnable.addComponent("sldFineTune", sldFineTune);
		
		lblFineAmt = new JLabel("0 cents");
		GridBagConstraints gbc_lblFineAmt = new GridBagConstraints();
		gbc_lblFineAmt.anchor = GridBagConstraints.WEST;
		gbc_lblFineAmt.gridx = 2;
		gbc_lblFineAmt.gridy = 4;
		pnlRegData.add(lblFineAmt, gbc_lblFineAmt);
		
		JPanel pnlRegBtns = new JPanel();
		GridBagConstraints gbc_pnlRegBtns = new GridBagConstraints();
		gbc_pnlRegBtns.fill = GridBagConstraints.BOTH;
		gbc_pnlRegBtns.gridx = 0;
		gbc_pnlRegBtns.gridy = 2;
		pnlRegion.add(pnlRegBtns, gbc_pnlRegBtns);
		GridBagLayout gridBagLayout_4 = new GridBagLayout();
		gridBagLayout_4.columnWidths = new int[]{0, 0, 0};
		gridBagLayout_4.rowHeights = new int[]{0, 0};
		gridBagLayout_4.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout_4.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlRegBtns.setLayout(gridBagLayout_4);
		
		JButton btnApply = new JButton("Apply");
		GridBagConstraints gbc_btnApply = new GridBagConstraints();
		gbc_btnApply.insets = new Insets(5, 5, 5, 5);
		gbc_btnApply.gridx = 1;
		gbc_btnApply.gridy = 0;
		pnlRegBtns.add(btnApply, gbc_btnApply);
		globalEnable.addComponent("btnApply", btnApply);
		regionEnable.addComponent("btnApply", btnApply);
		btnApply.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {btnApplyCallback();}
		});
		
		JPanel pnlButtons = new JPanel();
		GridBagConstraints gbc_pnlButtons = new GridBagConstraints();
		gbc_pnlButtons.gridwidth = 2;
		gbc_pnlButtons.fill = GridBagConstraints.BOTH;
		gbc_pnlButtons.gridx = 0;
		gbc_pnlButtons.gridy = 2;
		getContentPane().add(pnlButtons, gbc_pnlButtons);
		GridBagLayout gridBagLayout_3 = new GridBagLayout();
		gridBagLayout_3.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gridBagLayout_3.rowHeights = new int[]{0, 0};
		gridBagLayout_3.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout_3.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlButtons.setLayout(gridBagLayout_3);
		
		JButton btnInsert = new JButton("Insert Region");
		GridBagConstraints gbc_btnInsert = new GridBagConstraints();
		gbc_btnInsert.insets = new Insets(5, 5, 5, 5);
		gbc_btnInsert.gridx = 0;
		gbc_btnInsert.gridy = 0;
		pnlButtons.add(btnInsert, gbc_btnInsert);
		globalEnable.addComponent("btnInsert", btnInsert);
		btnInsert.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {btnInsertCallback();}
		});
		
		btnDelete = new JButton("Delete Region");
		GridBagConstraints gbc_btnDelete = new GridBagConstraints();
		gbc_btnDelete.insets = new Insets(5, 0, 5, 5);
		gbc_btnDelete.gridx = 1;
		gbc_btnDelete.gridy = 0;
		pnlButtons.add(btnDelete, gbc_btnDelete);
		globalEnable.addComponent("btnDelete", btnDelete);
		btnDelete.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {btnDeleteCallback();}
		});
		
		JButton btnSave = new JButton("Save");
		GridBagConstraints gbc_btnSave = new GridBagConstraints();
		gbc_btnSave.insets = new Insets(5, 5, 0, 5);
		gbc_btnSave.gridx = 3;
		gbc_btnSave.gridy = 0;
		pnlButtons.add(btnSave, gbc_btnSave);
		globalEnable.addComponent("btnSave", btnSave);
		btnSave.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {btnSaveCallback();}
		});
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(5, 5, 0, 0);
		gbc_btnCancel.gridx = 4;
		gbc_btnCancel.gridy = 0;
		pnlButtons.add(btnCancel, gbc_btnCancel);
		globalEnable.addComponent("btnCancel", btnCancel);
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {btnCancelCallback();}
		});
		
		initSpinners();
		initSliders();
		linkMiscCallbacks();
	}
	
	private void initTuningPanel(JPanel pnlRegData){
		
		JPanel pnlTuning = new JPanel();
		GridBagConstraints gbc_pnlTuning = new GridBagConstraints();
		gbc_pnlTuning.anchor = GridBagConstraints.NORTH;
		gbc_pnlTuning.gridwidth = 3;
		gbc_pnlTuning.insets = new Insets(0, 0, 5, 0);
		gbc_pnlTuning.fill = GridBagConstraints.HORIZONTAL;
		gbc_pnlTuning.gridx = 0;
		gbc_pnlTuning.gridy = 3;
		GridBagLayout gbl_pnltuning = new GridBagLayout();
		gbl_pnltuning.columnWidths = new int[]{0, 50, 0, 0, 50, 0};
		gbl_pnltuning.rowHeights = new int[]{0, 0, 0};
		gbl_pnltuning.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnltuning.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		pnlTuning.setLayout(gbl_pnltuning);
		pnlRegData.add(pnlTuning, gbc_pnlTuning);
		
		JLabel lblRangeMin = new JLabel("Range Min:");
		GridBagConstraints gbc_lblRangeMin = new GridBagConstraints();
		gbc_lblRangeMin.anchor = GridBagConstraints.EAST;
		gbc_lblRangeMin.insets = new Insets(0, 10, 5, 5);
		gbc_lblRangeMin.gridx = 0;
		gbc_lblRangeMin.gridy = 0;
		//pnlRegData.add(lblRangeMin, gbc_lblRangeMin);
		pnlTuning.add(lblRangeMin, gbc_lblRangeMin);
		
		spnNoteMin = new JSpinner();
		GridBagConstraints gbc_spnNoteMin = new GridBagConstraints();
		gbc_spnNoteMin.fill = GridBagConstraints.HORIZONTAL;
		gbc_spnNoteMin.insets = new Insets(0, 0, 5, 5);
		gbc_spnNoteMin.gridx = 1;
		gbc_spnNoteMin.gridy = 0;
		//pnlRegData.add(spnNoteMin, gbc_spnNoteMin);
		pnlTuning.add(spnNoteMin, gbc_spnNoteMin);
		globalEnable.addComponent("spnNoteMin", spnNoteMin);
		regionEnable.addComponent("spnNoteMin", spnNoteMin);
		
		JLabel lblRangeMax = new JLabel("Range Max:");
		GridBagConstraints gbc_lblRangeMax = new GridBagConstraints();
		gbc_lblRangeMax.anchor = GridBagConstraints.EAST;
		gbc_lblRangeMax.insets = new Insets(0, 0, 5, 5);
		gbc_lblRangeMax.gridx = 3;
		gbc_lblRangeMax.gridy = 0;
		//pnlRegData.add(lblRangeMax, gbc_lblRangeMax);
		pnlTuning.add(lblRangeMax, gbc_lblRangeMax);
		
		spnNoteMax = new JSpinner();
		GridBagConstraints gbc_spnNoteMax = new GridBagConstraints();
		gbc_spnNoteMax.fill = GridBagConstraints.HORIZONTAL;
		gbc_spnNoteMax.insets = new Insets(0, 0, 5, 0);
		gbc_spnNoteMax.gridx = 4;
		gbc_spnNoteMax.gridy = 0;
		//pnlRegData.add(spnNoteMax, gbc_spnNoteMax);
		pnlTuning.add(spnNoteMax, gbc_spnNoteMax);
		globalEnable.addComponent("spnNoteMax", spnNoteMax);
		regionEnable.addComponent("spnNoteMax", spnNoteMax);
		
		JLabel lblUnityKey = new JLabel("Unity Key:");
		GridBagConstraints gbc_lblUnityKey = new GridBagConstraints();
		gbc_lblUnityKey.anchor = GridBagConstraints.EAST;
		gbc_lblUnityKey.insets = new Insets(0, 10, 5, 5);
		gbc_lblUnityKey.gridx = 0;
		gbc_lblUnityKey.gridy = 1;
		//pnlRegData.add(lblUnityKey, gbc_lblUnityKey);
		pnlTuning.add(lblUnityKey, gbc_lblUnityKey);
		
		spnUnityKey = new JSpinner();
		GridBagConstraints gbc_spnUnityKey = new GridBagConstraints();
		gbc_spnUnityKey.fill = GridBagConstraints.HORIZONTAL;
		gbc_spnUnityKey.insets = new Insets(0, 0, 5, 5);
		gbc_spnUnityKey.gridx = 1;
		gbc_spnUnityKey.gridy = 1;
		//pnlRegData.add(spnUnityKey, gbc_spnUnityKey);
		pnlTuning.add(spnUnityKey, gbc_spnUnityKey);
		globalEnable.addComponent("spnUnityKey", spnUnityKey);
		regionEnable.addComponent("spnUnityKey", spnUnityKey);
	}

	private void initSpinners(){
		spnUnityKey.setModel(new SpinnerNumberModel(MIDDLE_C, 0, 127, 1));
		spnNoteMin.setModel(new SpinnerNumberModel(MIN_NOTE_DRUM, MIN_NOTE_DRUM, MAX_NOTE_DRUM, 1));
		spnNoteMax.setModel(new SpinnerNumberModel(MAX_NOTE_DRUM, MIN_NOTE_DRUM, MAX_NOTE_DRUM, 1));
	}
	
	private void initSliders(){
		sldPan.setMinimum(0);
		sldPan.setMaximum(127);
		sldPan.setValue(64);
		
		sldRelease.setMinimum(0);
		sldRelease.setMinimum(255);
		sldRelease.setValue(0);
		
		sldFineTune.setMinimum(-100);
		sldFineTune.setMaximum(100);
		sldFineTune.setValue(0);
	}
	
	private void linkMiscCallbacks(){
		//For neatness
		//Adds the callbacks for all components stored as instance variables
		lstRegions.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e) {
				listItemSelectCallback();
			}
		});
		
		sldPan.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				sldPanMoveCallback();
			}
		});
		
		sldRelease.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				sldRelMoveCallback();
			}
		});
		
		sldFineTune.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				sldFineTuneMoveCallback();
			}
		});
	}
	
	/*----- Getters -----*/
	
	public ZeqerPercPreset getDrum(){
		return loadedDrum;
	}
	
	/*----- Setters -----*/
	
	public void setDrum(ZeqerPercPreset drum){
		//Merge regions as we scan since it is indeed possible to have multiple
		//	instances of Z64Drum for essentially the same region spec
		
		loadedDrum = drum;
		selectedRegion = null;
		regions.clear();
		selectedSample = null;
		tempEnvelope = null;
		
		int rcount = drum.getRegionCount();
		for(int i = 0; i < rcount; i++){
			ZeqerPercRegion oreg = drum.getRegion(i);
			if(oreg != null){
				DrumRegion treg = new DrumRegion();
				regions.add(treg);
				treg.enumName = oreg.getEnumStem();
				treg.name = oreg.getNameStem();
				treg.noteMin = oreg.getMinNote();
				treg.noteMax = oreg.getMaxNote();
				
				Z64Drum drumdata = oreg.getDrumData();
				if(drumdata != null){
					treg.env = drumdata.getEnvelope();
					treg.pan = drumdata.getPan();
					treg.release = drumdata.getDecay();
					treg.sample = drumdata.getSample();
					Z64Tuning tune = drumdata.getTuning();
					treg.unityKey = tune.root_key;
					treg.fineTune = tune.fine_tune;
				}
			}
		}

		updateRegionList();
	}
	
	/*----- GUI Management -----*/
	
	private void setSelectedSample(Z64WaveInfo sample){
		selectedSample = sample;
		if(selectedSample != null){
			txtSample.setText(String.format("[%08x] %s", selectedSample.getUID(), selectedSample.getName()));
		}
		else{
			txtSample.setText("<No Sample>");
		}
		txtSample.repaint();
	}
	
	private void updateRelAmtLabel(int value){
		//Get millis.
		int millis = Z64Sound.releaseValueToMillis(value);
		lblRel.setText("[" + value + "] " + millis + " ms");
		lblRel.repaint();
	}
	
	private void updateFineTuneLabel(int value){
		lblFineAmt.setText(value + " cents");
		lblFineAmt.repaint();
	}
	
	private void updatePanLabel(int value){
		if(value == 64){
			lblPanSetting.setText("Center");
		}
		else if(value < 64){
			//Left
			int amt = 64 - value;
			double perc = ((double)amt / 64.0) * 100.0;
			lblPanSetting.setText(String.format("%.1f\\% Left", perc));
		}
		else{
			//Right
			int amt = value - 64;
			double perc = ((double)amt / 64.0) * 100.0;
			lblPanSetting.setText(String.format("%.1f\\% Right", perc));
		}
		
		lblPanSetting.repaint(); //Since it's not in the enable groups
	}
	
	private void updatePanGUIInfo(int value){
		if(value < 0) value = 0;
		if(value > 127) value = 127;
		sldPan.setValue(value);
		sldPan.repaint();
		updatePanLabel(value);
	}

	private void loadRegionInfoToGUI(DrumRegion reg){
		//setWait();
		selectedRegion = reg;
		if(reg == null){
			txtRegName.setText("<No Selection>");
			txtRegEnum.setText("<No Selection>");
			//txtRelease.setText("0");
			sldRelease.setValue(0);
			updateRelAmtLabel(0);
			updatePanGUIInfo(64);
			
			spnNoteMin.setValue(MIN_NOTE_DRUM);
			spnNoteMax.setValue(MAX_NOTE_DRUM);
			spnUnityKey.setValue(MIDDLE_C);
			//txtFineTune.setText("0");
			sldFineTune.setValue(0);
			updateFineTuneLabel(0);
			setSelectedSample(null);
			tempEnvelope = null;
		}
		else{
			if(reg.name == null) reg.generateName();
			
			txtRegName.setText(reg.name);
			txtRegEnum.setText(reg.enumName);
			
			setSelectedSample(reg.sample);
			
			//txtRelease.setText(Integer.toString(reg.release));
			//rbgRelUnits.select(reg.relUnits);
			sldRelease.setValue(reg.release);
			updateRelAmtLabel(reg.release);
			updatePanGUIInfo(reg.pan);
			
			spnNoteMin.setValue(reg.noteMin);
			spnNoteMax.setValue(reg.noteMax);
			spnUnityKey.setValue(reg.unityKey);
			//txtFineTune.setText(Integer.toString(reg.fineTune));
			sldFineTune.setValue(reg.fineTune);
			updateFineTuneLabel(reg.fineTune);
			tempEnvelope = reg.env;
		}
		//unsetWait();
	}
	
	private void updateRegionList(){
		//Preserve previous selection through update. So mark what reg is selected before replacing model.
		Collections.sort(regions);
		DrumRegion sel = lstRegions.getSelectedValue();
		DefaultListModel<DrumRegion> model = new DefaultListModel<DrumRegion>();
		int selidx = -1;
		int i = 0;
		for(DrumRegion reg : regions){
			if((sel != null) && (sel == reg)) selidx = i;
			model.addElement(reg);
			i++;
		}
		lstRegions.setModel(model);
		lstRegions.setSelectedIndex(selidx);
		lstRegions.repaint();
	}
	
	private void reenable(){
		//Some elements are dependent upon whether something is selected in list
		globalEnable.setEnabling(true);
		if(selectedRegion == null){
			//Disable everything in region edit area
			regionEnable.setEnabling(false);
		}
		
		if(lstRegions.isSelectionEmpty()){
			//Disable delete button
			btnDelete.setEnabled(false);
		}
		globalEnable.repaint();
	}
	
	public void setWait(){
		globalEnable.setEnabling(false);
		globalEnable.repaint();
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	public void unsetWait(){
		reenable();
		setCursor(null);
	}
	
	public void closeMe(){
		this.setVisible(false);
		this.dispose();
	}
	
	/*----- Updates -----*/
	
	private int applyGUIInfoToRegion(){
		//Returns error codes.
		if(selectedRegion == null) return ERR_REGUPDATE_NOREG;
		
		//Run checks first, before changing anything...
		if(selectedSample == null) return ERR_REGUPDATE_NULL_SAMPLE;
		if(tempEnvelope == null) return ERR_REGUPDATE_NULL_ENV;
	
		//Look for note range intersections...
		int notemin_old = selectedRegion.noteMin;
		int notemax_old = selectedRegion.noteMax;
		selectedRegion.noteMin = (Integer)spnNoteMin.getValue();
		selectedRegion.noteMax = (Integer)spnNoteMax.getValue();
		for(DrumRegion other : this.regions){
			if(selectedRegion == other) continue;
			if(selectedRegion.noteRangeIntersects(other)){
				selectedRegion.noteMin = notemin_old;
				selectedRegion.noteMax = notemax_old;
				return ERR_REGUPDATE_ISECT_NOTERANGE;
			}
		}
		
		//Now update the region instance...
		selectedRegion.name = txtName.getText();
		if(selectedRegion.name == null) selectedRegion.generateName();
		selectedRegion.enumName = ZeqerUtils.fixHeaderEnumID(txtRegEnum.getText(), true);
		txtRegEnum.setText(selectedRegion.enumName);
		
		selectedRegion.sample = selectedSample;
		selectedRegion.env = tempEnvelope;
		
		selectedRegion.unityKey = (Integer)spnUnityKey.getValue();
		selectedRegion.fineTune = sldFineTune.getValue();
		
		selectedRegion.release = sldRelease.getValue();
		selectedRegion.pan = sldPan.getValue();
		
		return ERR_REGUPDATE_NONE;
	}
	
	private void loadRegionsBackToPreset(){
		if(loadedDrum == null) return;
		int rcount = regions.size();
		loadedDrum.clearAndReallocRegions(rcount);
		
		Collections.sort(regions);
		int i = 0;
		for(DrumRegion r : regions){
			ZeqerPercRegion treg = new ZeqerPercRegion();
			treg.setEnumStem(r.enumName);
			treg.setNameStem(r.name);
			treg.setMinNote(r.noteMin);
			treg.setMaxNote(r.noteMax);
			
			Z64Drum drumdat = new Z64Drum();
			drumdat.setDecay((byte)r.release);
			drumdat.setPan((byte)r.pan);
			drumdat.setEnvelope(r.env);
			drumdat.setSample(r.sample);
			
			Z64Tuning tune = new Z64Tuning();
			tune.root_key = (byte)r.unityKey;
			tune.fine_tune = (byte)r.fineTune;
			drumdat.setTuning(tune);
			treg.setDrumData(drumdat);
			
			loadedDrum.setRegion(i++, treg);
		}
		
	}
	
	/*----- Callbacks -----*/
	
	private void btnInsertCallback(){
		//Find last selected index
		//I guess it'll determine note range depending on position...
		setWait();
		
		//Can't insert if full note range is already covered ig
		int rcount = regions.size();
		int selidx = lstRegions.getMaxSelectionIndex();
		int insidx = selidx + 1;
		if(selidx < 0){
			//Insert at end
			insidx = rcount;
			selidx = insidx - 1;
		}
		
		//Check if there is a gap in note range in which to insert new region
		int insmin_note = MIN_NOTE_DRUM;
		int insmax_note = MAX_NOTE_DRUM;
		DrumRegion before = null, after = null;
		if(selidx >= 0) before = regions.get(selidx);
		if(insidx >= 0 && insidx < rcount) after = regions.get(insidx);
		if(before != null) insmin_note = before.noteMax+1;
		if(after != null) insmax_note = after.noteMin-1;
		
		if((insmin_note > insmax_note) || (insmin_note < MIN_NOTE_DRUM) || (insmax_note < MAX_NOTE_DRUM)){
			//Show message and return without insertion
			JOptionPane.showMessageDialog(this, "Cannot insert new region between "
					+ "existing regions covering adjacent notes,\n"
					+ "or outside the percussion range.", 
					"Insertion Failed", JOptionPane.WARNING_MESSAGE);
			unsetWait();
			return;
		}
		
		DrumRegion newreg = new DrumRegion();
		newreg.noteMin = insmin_note;
		newreg.noteMax = insmax_note;
		newreg.release = 10;
		newreg.env = Z64Envelope.newDefaultEnvelope();
		newreg.generateName();
		newreg.sample = core.getDefaultPercussionSample();
		
		regions.add(newreg);
		updateRegionList();
		lstRegions.setSelectedValue(newreg, true);
		loadRegionInfoToGUI(newreg);
		unsetWait();
	}
	
	private void btnDeleteCallback(){
		if(lstRegions.isSelectionEmpty()) return;
		
		setWait();
		int[] selidxs = lstRegions.getSelectedIndices();
		List<DrumRegion> templist = new LinkedList<DrumRegion>();
		int i = 0;
		for(DrumRegion r : templist){
			boolean incl = true;
			for(int j = 0; j < selidxs.length; j++){
				if(selidxs[j] == i){
					incl = false;
					break;
				}
			}
			if(incl) templist.add(r);
			i++;
		}
		regions.clear();
		regions.addAll(templist);
		templist.clear();
		
		updateRegionList();
		lstRegions.setSelectedIndex(-1);
		loadRegionInfoToGUI(null);
		unsetWait();
	}
	
	private void btnSaveCallback(){
		//Does NOT save unapplied info in region editor.
		setWait();
		loadRegionsBackToPreset();
		unsetWait();
		closeMe();
	}
	
	private void btnCancelCallback(){
		loadedDrum = null;
		loadRegionInfoToGUI(null);
		regions.clear();
		closeMe();
	}
	
	private void btnApplyCallback(){
		if(selectedRegion == null){
			JOptionPane.showMessageDialog(this, 
					"Region changes cannot be applied to null region!", 
					"Region Changes", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		setWait();
		int applres = applyGUIInfoToRegion();
		if(applres != ERR_REGUPDATE_NONE){
			String errmsg = "ERROR: An internal error occurred!";
			switch(applres){
			case ERR_REGUPDATE_NOREG:
				errmsg = "ERROR: No region is currently loaded.";
				break;
			case ERR_REGUPDATE_ISECT_NOTERANGE:
				errmsg = "ERROR: Note range cannot intersect that of another region!";
				break;
			case ERR_REGUPDATE_REL_INVALID:
				errmsg = "ERROR: Release value is invalid!\n"
						+ "Must be an integer between (0-255) if raw,\n"
						+ "or (0-10000) if in milliseconds.";
				break;
			case ERR_REGUPDATE_FINETUNE_INVALID:
				errmsg = "ERROR: Fine tune value is invalid!\n"
						+ "Must be an integer between -100 and 100.";
				break;
			case ERR_REGUPDATE_NULL_SAMPLE:
				errmsg = "ERROR: Sound sample is required!";
				break;
			case ERR_REGUPDATE_NULL_ENV:
				errmsg = "ERROR: Envelope is required!";
				break;
			}
			
			JOptionPane.showMessageDialog(this, errmsg, 
					"Region Update Failed", JOptionPane.ERROR_MESSAGE);
			unsetWait();
			return;
		}
		
		//Update list? 
		
		unsetWait();
	}
	
	private void btnEnvCallback(){
		setWait();
		ZeqerEnvEditDialog dialog = new ZeqerEnvEditDialog(parent, core);
		
		//Show
		dialog.pack();
		dialog.setVisible(true);
		
		//Check result and save env, if needed
		Z64Envelope env = dialog.getOutputEnvelope();
		if(env != null){
			this.tempEnvelope = env;
		}
		
		unsetWait();
	}
	
	private void btnSampleCallback(){
		setWait();
		SamplePickDialog dialog = new SamplePickDialog(parent, core);
		dialog.setVisible(true);
		
		if(dialog.getExitSelection()){
			WaveTableEntry sel = dialog.getSelectedSample();
			if(sel != null){
				setSelectedSample(sel.getWaveInfo());
			}
			else setSelectedSample(null);
		}

		unsetWait();
	}
	
	private void btnEditTagsCallback(){
		if(loadedDrum == null) return;
		setWait();
		ZeqerTagEditDialog dialog = new ZeqerTagEditDialog(parent);
		dialog.loadTags(loadedDrum.getAllTags());
		
		dialog.setVisible(true);
		if(dialog.getExitSelection()){
			List<String> newtags = dialog.getTags();
			loadedDrum.clearTags();
			for(String s : newtags) loadedDrum.addTag(s);
		}
		
		unsetWait();
	}
	
	private void sldPanMoveCallback(){
		int value = sldPan.getValue();
		updatePanLabel(value);
	}
	
	private void sldRelMoveCallback(){
		int value = sldRelease.getValue();
		updateRelAmtLabel(value);
	}
	
	private void sldFineTuneMoveCallback(){
		int value = sldFineTune.getValue();
		updateFineTuneLabel(value);
	}
	
	private void listItemSelectCallback(){
		setWait();
		if(lstRegions.isSelectionEmpty()){
			loadRegionInfoToGUI(null);
		}
		else{
			loadRegionInfoToGUI(lstRegions.getSelectedValue());
		}
		unsetWait();
	}
	
}
