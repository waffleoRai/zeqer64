package waffleoRai_zeqer64.GUI.dialogs;

import javax.swing.JFrame;

import java.awt.GridBagLayout;
import javax.swing.JPanel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import javax.swing.JTabbedPane;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JComboBox;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.WRDialog;
import waffleoRai_zeqer64.GUI.CacheTypeCombobox;
import waffleoRai_zeqer64.GUI.CheckboxTable;
import waffleoRai_zeqer64.GUI.MediumTypeCombobox;
import waffleoRai_zeqer64.GUI.sampleImport.SampleOptionsPanel;
import waffleoRai_zeqer64.bankImport.BankImportInfo;
import waffleoRai_zeqer64.bankImport.BankImportInfo.PresetInfo;
import waffleoRai_zeqer64.bankImport.BankImportInfo.SampleInfo;
import waffleoRai_zeqer64.bankImport.BankImportInfo.SubBank;
import waffleoRai_zeqer64.filefmt.wave.ZeqerWaveIO.SampleImportOptions;

import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import java.awt.Font;

public class BankImportDialog extends WRDialog{
	
	/*----- Constants -----*/
	
	private static final long serialVersionUID = 4901070415958862918L;
	
	public static final int MIN_WIDTH = 750;
	public static final int MIN_HEIGHT = 620;
	
	private static final int INSTTBL_COL_INDEX = 0;
	private static final int INSTTBL_COL_NAME = 1;
	private static final int INSTTBL_COL_WARNINGS = 2; //String of flag field in hex until graphics done.
	private static final int INSTTBL_COL_TOFONT = 3;
	private static final int INSTTBL_COL_TOPRESET = 4;
	private static final int INSTTBL_COL_SAVEDRUMS = 5;
	
	private static final int INSTTBL_COL_COUNT = 6;
	
	private static final int EXTRA_INSTTBL_COL_INDEX = 0;
	private static final int EXTRA_INSTTBL_COL_NAME = 1;
	private static final int EXTRA_INSTTBL_COL_WARNINGS = 2; //String of flag field in hex until graphics done.
	private static final int EXTRA_INSTTBL_COL_TOFONT = 3;
	private static final int EXTRA_INSTTBL_COL_NEWIDX = 4;
	private static final int EXTRA_INSTTBL_COL_TOPRESET = 5;
	private static final int EXTRA_INSTTBL_COL_SAVEDRUMS = 6;
	
	private static final int EXTRA_INSTTBL_COL_COUNT = 7;
	
	private static final int SMPLTBL_COL_NAME = 0;
	private static final int SMPLTBL_COL_WARNINGS = 1; //String of flag field in hex until graphics done.
	private static final int SMPLTBL_COL_IMPORT = 2;
	
	private static final int SMPLTBL_COL_COUNT = 3;
	
	/*----- Instance Variables -----*/
	
	private boolean exitSelection = false;
	
	private JTabbedPane tabbedPane;
	private ComponentGroup cgSubLoaded;
	private ComponentGroup cgUse126;
	private ComponentGroup cgUse127;
	
	private JCheckBox cbImportFont;
	private JTextField txtEnum;
	private JTextField txtName;
	private CacheTypeCombobox cmbxCache;
	private MediumTypeCombobox cmbxMedium;
	private JList<SubBank> lstBanks;
	
	private CheckboxTable cbtInst;
	private CheckboxTable cbtExtInst;
	private CheckboxTable cbtSamples;
	
	private SampleOptionsPanel pnlSampleOps;
	
	//Perc & SFX panels
	private JCheckBox cbUsePerc;
	private JCheckBox cbSave127;
	private JCheckBox cbSaveDrums127;
	private JComboBox<SubBank> cmbxBank127;
	private JComboBox<PresetInfo> cmbxPreset127;
	private JCheckBox cbFilter127;
	private JCheckBox cbUseSfx;
	private JCheckBox cbSaveSfx;
	private JComboBox<SubBank> cmbxBank126;
	private JCheckBox cbFilter126;
	private JComboBox<PresetInfo> cmbxPreset126;
	
	//Tags to apply to all incoming presets and samples
	private Set<String> ptags;
	private Set<String> stags;
	
	private SubBank selectedBank;
	private BankImportInfo bankInfo;
	
	/*----- Init -----*/

	public BankImportDialog(JFrame parent){
		super(parent, true);
		cgSubLoaded = globalEnable.newChild();
		cgUse126 = cgSubLoaded.newChild();
		cgUse127 = cgSubLoaded.newChild();
		
		initGUI();
	}
	
	private void initGUI(){
		setTitle("Import Soundfont");
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.insets = new Insets(0, 0, 5, 0);
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 0;
		getContentPane().add(tabbedPane, gbc_tabbedPane);
		
		initFontTab();
		initInstTab();
		initSampleTab();
		
		JPanel pnlButtons = new JPanel();
		GridBagConstraints gbc_pnlButtons = new GridBagConstraints();
		gbc_pnlButtons.fill = GridBagConstraints.BOTH;
		gbc_pnlButtons.gridx = 0;
		gbc_pnlButtons.gridy = 1;
		getContentPane().add(pnlButtons, gbc_pnlButtons);
		GridBagLayout gbl_pnlButtons = new GridBagLayout();
		gbl_pnlButtons.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_pnlButtons.rowHeights = new int[]{0, 0};
		gbl_pnlButtons.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlButtons.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlButtons.setLayout(gbl_pnlButtons);
		
		JButton btnTagPresets = new JButton("Preset Tags...");
		GridBagConstraints gbc_btnTagPresets = new GridBagConstraints();
		gbc_btnTagPresets.insets = new Insets(0, 5, 5, 5);
		gbc_btnTagPresets.gridx = 0;
		gbc_btnTagPresets.gridy = 0;
		pnlButtons.add(btnTagPresets, gbc_btnTagPresets);
		globalEnable.addComponent("btnTagPresets", btnTagPresets);
		btnTagPresets.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnPTagsCallback();
			}
		});
		
		JButton btnTagSamples = new JButton("Sample Tags...");
		GridBagConstraints gbc_btnTagSamples = new GridBagConstraints();
		gbc_btnTagSamples.insets = new Insets(0, 0, 5, 5);
		gbc_btnTagSamples.gridx = 1;
		gbc_btnTagSamples.gridy = 0;
		pnlButtons.add(btnTagSamples, gbc_btnTagSamples);
		globalEnable.addComponent("btnTagSamples", btnTagSamples);
		btnTagSamples.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnSTagsCallback();
			}
		});
		
		JButton btnImport = new JButton("Import");
		GridBagConstraints gbc_btnImport = new GridBagConstraints();
		gbc_btnImport.insets = new Insets(0, 0, 0, 5);
		gbc_btnImport.gridx = 3;
		gbc_btnImport.gridy = 0;
		pnlButtons.add(btnImport, gbc_btnImport);
		globalEnable.addComponent("btnImport", btnImport);
		btnImport.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnOkayCallback();
			}
		});
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.gridx = 4;
		gbc_btnCancel.gridy = 0;
		pnlButtons.add(btnCancel, gbc_btnCancel);
		globalEnable.addComponent("btnCancel", btnCancel);
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnCancelCallback();
			}
		});
	}
	
	private void initFontTab(){
		JPanel pnlFont = new JPanel();
		tabbedPane.addTab("Font", null, pnlFont, null);
		GridBagLayout gbl_pnlFont = new GridBagLayout();
		gbl_pnlFont.columnWidths = new int[]{0, 200, 0};
		gbl_pnlFont.rowHeights = new int[]{0, 0};
		gbl_pnlFont.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_pnlFont.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		pnlFont.setLayout(gbl_pnlFont);
		
		JScrollPane spSubBanks = new JScrollPane();
		spSubBanks.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spSubBanks = new GridBagConstraints();
		gbc_spSubBanks.insets = new Insets(0, 0, 0, 5);
		gbc_spSubBanks.fill = GridBagConstraints.BOTH;
		gbc_spSubBanks.gridx = 0;
		gbc_spSubBanks.gridy = 0;
		pnlFont.add(spSubBanks, gbc_spSubBanks);
		globalEnable.addComponent("spSubBanks", spSubBanks);
		
		lstBanks = new JList<SubBank>();
		spSubBanks.setViewportView(lstBanks);
		globalEnable.addComponent("lstBanks", lstBanks);
		lstBanks.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e) {
				lstFontSelectCallback();
			}});

		JPanel pnlMeta = new JPanel();
		pnlMeta.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlMeta = new GridBagConstraints();
		gbc_pnlMeta.fill = GridBagConstraints.BOTH;
		gbc_pnlMeta.gridx = 1;
		gbc_pnlMeta.gridy = 0;
		pnlFont.add(pnlMeta, gbc_pnlMeta);
		GridBagLayout gbl_pnlMeta = new GridBagLayout();
		gbl_pnlMeta.columnWidths = new int[]{0, 0};
		gbl_pnlMeta.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_pnlMeta.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlMeta.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlMeta.setLayout(gbl_pnlMeta);
		
		cbImportFont = new JCheckBox("Import as Soundfont");
		GridBagConstraints gbc_cbImportFont = new GridBagConstraints();
		gbc_cbImportFont.insets = new Insets(5, 5, 5, 5);
		gbc_cbImportFont.gridx = 0;
		gbc_cbImportFont.gridy = 0;
		pnlMeta.add(cbImportFont, gbc_cbImportFont);
		cgSubLoaded.addComponent("cbImportFont", cbImportFont);
		
		JLabel lblName = new JLabel("Name:");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.WEST;
		gbc_lblName.insets = new Insets(5, 5, 5, 0);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 1;
		pnlMeta.add(lblName, gbc_lblName);
		
		txtName = new JTextField();
		GridBagConstraints gbc_txtName = new GridBagConstraints();
		gbc_txtName.insets = new Insets(0, 5, 5, 0);
		gbc_txtName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtName.gridx = 0;
		gbc_txtName.gridy = 2;
		pnlMeta.add(txtName, gbc_txtName);
		txtName.setColumns(10);
		cgSubLoaded.addComponent("txtName", txtName);
		
		JLabel lblEnumLabel = new JLabel("Enum Label:");
		GridBagConstraints gbc_lblEnumLabel = new GridBagConstraints();
		gbc_lblEnumLabel.anchor = GridBagConstraints.WEST;
		gbc_lblEnumLabel.insets = new Insets(0, 5, 5, 0);
		gbc_lblEnumLabel.gridx = 0;
		gbc_lblEnumLabel.gridy = 3;
		pnlMeta.add(lblEnumLabel, gbc_lblEnumLabel);
		
		txtEnum = new JTextField();
		GridBagConstraints gbc_txtEnum = new GridBagConstraints();
		gbc_txtEnum.anchor = GridBagConstraints.NORTH;
		gbc_txtEnum.insets = new Insets(0, 5, 5, 0);
		gbc_txtEnum.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtEnum.gridx = 0;
		gbc_txtEnum.gridy = 4;
		pnlMeta.add(txtEnum, gbc_txtEnum);
		txtEnum.setColumns(10);
		cgSubLoaded.addComponent("txtEnum", txtEnum);
		
		JLabel lblDefaultCachePolicy = new JLabel("Default Cache Policy:");
		GridBagConstraints gbc_lblDefaultCachePolicy = new GridBagConstraints();
		gbc_lblDefaultCachePolicy.anchor = GridBagConstraints.WEST;
		gbc_lblDefaultCachePolicy.insets = new Insets(0, 5, 5, 0);
		gbc_lblDefaultCachePolicy.gridx = 0;
		gbc_lblDefaultCachePolicy.gridy = 5;
		pnlMeta.add(lblDefaultCachePolicy, gbc_lblDefaultCachePolicy);
		
		cmbxCache = new CacheTypeCombobox();
		GridBagConstraints gbc_cmbxCache = new GridBagConstraints();
		gbc_cmbxCache.insets = new Insets(0, 5, 5, 0);
		gbc_cmbxCache.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxCache.gridx = 0;
		gbc_cmbxCache.gridy = 6;
		pnlMeta.add(cmbxCache, gbc_cmbxCache);
		cgSubLoaded.addComponent("cmbxCache", cmbxCache);
		
		JLabel lblDefaultMedium = new JLabel("Default Medium:");
		GridBagConstraints gbc_lblDefaultMedium = new GridBagConstraints();
		gbc_lblDefaultMedium.anchor = GridBagConstraints.WEST;
		gbc_lblDefaultMedium.insets = new Insets(0, 5, 5, 0);
		gbc_lblDefaultMedium.gridx = 0;
		gbc_lblDefaultMedium.gridy = 7;
		pnlMeta.add(lblDefaultMedium, gbc_lblDefaultMedium);
		
		cmbxMedium = new MediumTypeCombobox();
		GridBagConstraints gbc_cmbxMedium = new GridBagConstraints();
		gbc_cmbxMedium.insets = new Insets(0, 5, 0, 0);
		gbc_cmbxMedium.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxMedium.gridx = 0;
		gbc_cmbxMedium.gridy = 8;
		pnlMeta.add(cmbxMedium, gbc_cmbxMedium);
		cgSubLoaded.addComponent("cmbxMedium", cmbxMedium);
	}
	
	private void initInstTab(){
		JPanel pnlInst = new JPanel();
		tabbedPane.addTab("Instruments", null, pnlInst, null);
		GridBagLayout gbl_pnlInst = new GridBagLayout();
		gbl_pnlInst.columnWidths = new int[]{0, 0};
		gbl_pnlInst.rowHeights = new int[] {0, 100, 50, 100, 0};
		gbl_pnlInst.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlInst.rowWeights = new double[]{1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlInst.setLayout(gbl_pnlInst);
		
		cbtInst = new CheckboxTable(new int[]
				{CheckboxTable.COLTYPE_STRING, CheckboxTable.COLTYPE_STRING, CheckboxTable.COLTYPE_STRING,
				CheckboxTable.COLTYPE_CHECKBOX, CheckboxTable.COLTYPE_CHECKBOX, CheckboxTable.COLTYPE_CHECKBOX});
		cbtInst.setColumnName(INSTTBL_COL_INDEX, "Slot");
		cbtInst.setColumnName(INSTTBL_COL_NAME, "Name");
		cbtInst.setColumnName(INSTTBL_COL_WARNINGS, "Warnings");
		cbtInst.setColumnName(INSTTBL_COL_TOFONT, "Include in Font");
		cbtInst.setColumnName(INSTTBL_COL_TOPRESET, "Save Preset");
		cbtInst.setColumnName(INSTTBL_COL_SAVEDRUMS, "Save Drums");
		
		GridBagConstraints gbc_pnlStdInst = new GridBagConstraints();
		gbc_pnlStdInst.insets = new Insets(5, 5, 5, 0);
		gbc_pnlStdInst.fill = GridBagConstraints.BOTH;
		gbc_pnlStdInst.gridx = 0;
		gbc_pnlStdInst.gridy = 0;
		pnlInst.add(cbtInst, gbc_pnlStdInst);
		
		cbtExtInst = new CheckboxTable(new int[]
				{CheckboxTable.COLTYPE_STRING, CheckboxTable.COLTYPE_STRING, CheckboxTable.COLTYPE_STRING,
				CheckboxTable.COLTYPE_CHECKBOX, CheckboxTable.COLTYPE_STRING, 
				CheckboxTable.COLTYPE_CHECKBOX, CheckboxTable.COLTYPE_CHECKBOX});
		GridBagConstraints gbc_cbtExtInst = new GridBagConstraints();
		gbc_cbtExtInst.insets = new Insets(0, 5, 5, 0);
		gbc_cbtExtInst.fill = GridBagConstraints.BOTH;
		gbc_cbtExtInst.gridx = 0;
		gbc_cbtExtInst.gridy = 1;
		pnlInst.add(cbtExtInst, gbc_cbtExtInst);
		cbtExtInst.setColumnName(EXTRA_INSTTBL_COL_INDEX, "Source Slot");
		cbtExtInst.setColumnName(EXTRA_INSTTBL_COL_NAME, "Name");
		cbtExtInst.setColumnName(EXTRA_INSTTBL_COL_WARNINGS, "Warnings");
		cbtExtInst.setColumnName(EXTRA_INSTTBL_COL_TOFONT, "Include in Font");
		cbtExtInst.setColumnName(EXTRA_INSTTBL_COL_NEWIDX, "Target Slot");
		cbtExtInst.setColumnName(EXTRA_INSTTBL_COL_TOPRESET, "Save Preset");
		cbtExtInst.setColumnName(EXTRA_INSTTBL_COL_SAVEDRUMS, "Save Drums");
		
		JPanel pnlPerc = new JPanel();
		pnlPerc.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlPerc = new GridBagConstraints();
		gbc_pnlPerc.insets = new Insets(0, 5, 5, 0);
		gbc_pnlPerc.fill = GridBagConstraints.BOTH;
		gbc_pnlPerc.gridx = 0;
		gbc_pnlPerc.gridy = 3;
		pnlInst.add(pnlPerc, gbc_pnlPerc);
		GridBagLayout gbl_pnlPerc = new GridBagLayout();
		gbl_pnlPerc.columnWidths = new int[]{0, 0, 10, 0, 0, 0, 0, 0};
		gbl_pnlPerc.rowHeights = new int[]{0, 0, 0, 0};
		gbl_pnlPerc.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
		gbl_pnlPerc.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlPerc.setLayout(gbl_pnlPerc);
		
		JLabel lbl127 = new JLabel("127 - Percussion");
		lbl127.setFont(new Font("Tahoma", Font.BOLD, 13));
		GridBagConstraints gbc_lbl127 = new GridBagConstraints();
		gbc_lbl127.anchor = GridBagConstraints.WEST;
		gbc_lbl127.gridwidth = 2;
		gbc_lbl127.insets = new Insets(5, 5, 5, 5);
		gbc_lbl127.gridx = 0;
		gbc_lbl127.gridy = 0;
		pnlPerc.add(lbl127, gbc_lbl127);
		cgSubLoaded.addComponent("lbl127", lbl127);
		
		cbUsePerc = new JCheckBox("Import to Font");
		GridBagConstraints gbc_cbUsePerc = new GridBagConstraints();
		gbc_cbUsePerc.anchor = GridBagConstraints.WEST;
		gbc_cbUsePerc.insets = new Insets(0, 0, 5, 5);
		gbc_cbUsePerc.gridx = 4;
		gbc_cbUsePerc.gridy = 0;
		pnlPerc.add(cbUsePerc, gbc_cbUsePerc);
		cgSubLoaded.addComponent("cbUsePerc", cbUsePerc);
		cbUsePerc.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				cbUse127Callback();
			}
		});
		
		cbSave127 = new JCheckBox("Save Preset");
		GridBagConstraints gbc_cbSave127 = new GridBagConstraints();
		gbc_cbSave127.anchor = GridBagConstraints.WEST;
		gbc_cbSave127.insets = new Insets(0, 0, 5, 5);
		gbc_cbSave127.gridx = 5;
		gbc_cbSave127.gridy = 0;
		pnlPerc.add(cbSave127, gbc_cbSave127);
		cgUse127.addComponent("cbSave127", cbSave127);
		
		cbSaveDrums127 = new JCheckBox("Save Drums");
		GridBagConstraints gbc_cbSaveDrums127 = new GridBagConstraints();
		gbc_cbSaveDrums127.anchor = GridBagConstraints.WEST;
		gbc_cbSaveDrums127.insets = new Insets(0, 0, 5, 0);
		gbc_cbSaveDrums127.gridx = 6;
		gbc_cbSaveDrums127.gridy = 0;
		pnlPerc.add(cbSaveDrums127, gbc_cbSaveDrums127);
		cgUse127.addComponent("cbSaveDrums127", cbSaveDrums127);
		
		JLabel lblBank127 = new JLabel("Bank:");
		GridBagConstraints gbc_lblBank127 = new GridBagConstraints();
		gbc_lblBank127.anchor = GridBagConstraints.EAST;
		gbc_lblBank127.insets = new Insets(0, 5, 5, 5);
		gbc_lblBank127.gridx = 0;
		gbc_lblBank127.gridy = 1;
		pnlPerc.add(lblBank127, gbc_lblBank127);
		cgUse127.addComponent("lblBank127", lblBank127);
		
		cmbxBank127 = new JComboBox<SubBank>();
		GridBagConstraints gbc_cmbxBank127 = new GridBagConstraints();
		gbc_cmbxBank127.insets = new Insets(0, 0, 5, 5);
		gbc_cmbxBank127.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxBank127.gridx = 1;
		gbc_cmbxBank127.gridy = 1;
		pnlPerc.add(cmbxBank127, gbc_cmbxBank127);
		cgUse127.addComponent("cmbxBank127", cmbxBank127);
		cmbxBank127.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				cmbxBank127Callback();
			}
		});
		
		JLabel lblPreset127 = new JLabel("Preset:");
		GridBagConstraints gbc_lblPreset127 = new GridBagConstraints();
		gbc_lblPreset127.anchor = GridBagConstraints.EAST;
		gbc_lblPreset127.insets = new Insets(0, 0, 5, 5);
		gbc_lblPreset127.gridx = 3;
		gbc_lblPreset127.gridy = 1;
		pnlPerc.add(lblPreset127, gbc_lblPreset127);
		cgUse127.addComponent("lblPreset127", lblPreset127);
		
		cmbxPreset127 = new JComboBox<PresetInfo>();
		GridBagConstraints gbc_cmbxPreset127 = new GridBagConstraints();
		gbc_cmbxPreset127.gridwidth = 3;
		gbc_cmbxPreset127.insets = new Insets(0, 0, 5, 5);
		gbc_cmbxPreset127.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxPreset127.gridx = 4;
		gbc_cmbxPreset127.gridy = 1;
		pnlPerc.add(cmbxPreset127, gbc_cmbxPreset127);
		cgUse127.addComponent("cmbxPreset127", cmbxPreset127);
		
		cbFilter127 = new JCheckBox("Exclude regular instruments");
		cbFilter127.setSelected(true);
		GridBagConstraints gbc_cbFilter127 = new GridBagConstraints();
		gbc_cbFilter127.anchor = GridBagConstraints.WEST;
		gbc_cbFilter127.gridx = 4;
		gbc_cbFilter127.gridy = 2;
		pnlPerc.add(cbFilter127, gbc_cbFilter127);
		cgUse127.addComponent("cbFilter127", cbFilter127);
		cbFilter127.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				cbFilter127Callback();
			}
		});
		
		JPanel pnlSfx = new JPanel();
		pnlSfx.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlSfx = new GridBagConstraints();
		gbc_pnlSfx.insets = new Insets(0, 5, 5, 0);
		gbc_pnlSfx.fill = GridBagConstraints.BOTH;
		gbc_pnlSfx.gridx = 0;
		gbc_pnlSfx.gridy = 2;
		pnlInst.add(pnlSfx, gbc_pnlSfx);
		GridBagLayout gbl_pnlSfx = new GridBagLayout();
		gbl_pnlSfx.columnWidths = new int[] {0, 0, 10, 0, 0, 0, 0};
		gbl_pnlSfx.rowHeights = new int[]{0, 0, 0, 0};
		gbl_pnlSfx.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
		gbl_pnlSfx.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlSfx.setLayout(gbl_pnlSfx);
		
		JLabel lbl126 = new JLabel("126 - Sound Effects Program");
		lbl126.setFont(new Font("Tahoma", Font.BOLD, 13));
		GridBagConstraints gbc_lbl126 = new GridBagConstraints();
		gbc_lbl126.anchor = GridBagConstraints.WEST;
		gbc_lbl126.gridwidth = 2;
		gbc_lbl126.insets = new Insets(5, 5, 5, 5);
		gbc_lbl126.gridx = 0;
		gbc_lbl126.gridy = 0;
		pnlSfx.add(lbl126, gbc_lbl126);
		cgSubLoaded.addComponent("lbl126", lbl126);
		
		cbUseSfx = new JCheckBox("Import to Font");
		GridBagConstraints gbc_cbUseSfx = new GridBagConstraints();
		gbc_cbUseSfx.anchor = GridBagConstraints.WEST;
		gbc_cbUseSfx.insets = new Insets(5, 0, 5, 5);
		gbc_cbUseSfx.gridx = 4;
		gbc_cbUseSfx.gridy = 0;
		pnlSfx.add(cbUseSfx, gbc_cbUseSfx);
		cgSubLoaded.addComponent("cbUseSfx", cbUseSfx);
		cbUseSfx.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				cbUse126Callback();
			}
		});
		
		cbSaveSfx = new JCheckBox("Save SFX Preset");
		GridBagConstraints gbc_cbSaveSfx = new GridBagConstraints();
		gbc_cbSaveSfx.anchor = GridBagConstraints.WEST;
		gbc_cbSaveSfx.insets = new Insets(5, 0, 5, 5);
		gbc_cbSaveSfx.gridx = 5;
		gbc_cbSaveSfx.gridy = 0;
		pnlSfx.add(cbSaveSfx, gbc_cbSaveSfx);
		cgUse126.addComponent("cbSaveSfx", cbSaveSfx);
		
		JLabel lblBank126 = new JLabel("Bank:");
		GridBagConstraints gbc_lblBank126 = new GridBagConstraints();
		gbc_lblBank126.insets = new Insets(0, 5, 5, 5);
		gbc_lblBank126.anchor = GridBagConstraints.EAST;
		gbc_lblBank126.gridx = 0;
		gbc_lblBank126.gridy = 1;
		pnlSfx.add(lblBank126, gbc_lblBank126);
		cgUse126.addComponent("lblBank126", lblBank126);
		
		cmbxBank126 = new JComboBox<SubBank>();
		GridBagConstraints gbc_cmbxBank126 = new GridBagConstraints();
		gbc_cmbxBank126.insets = new Insets(0, 0, 5, 5);
		gbc_cmbxBank126.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxBank126.gridx = 1;
		gbc_cmbxBank126.gridy = 1;
		pnlSfx.add(cmbxBank126, gbc_cmbxBank126);
		cgUse126.addComponent("cmbxBank126", cmbxBank126);
		cmbxBank126.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				cmbxBank126Callback();
			}
		});
		
		JLabel lblPreset126 = new JLabel("Preset:");
		GridBagConstraints gbc_lblPreset126 = new GridBagConstraints();
		gbc_lblPreset126.insets = new Insets(0, 0, 5, 5);
		gbc_lblPreset126.anchor = GridBagConstraints.EAST;
		gbc_lblPreset126.gridx = 3;
		gbc_lblPreset126.gridy = 1;
		pnlSfx.add(lblPreset126, gbc_lblPreset126);
		cgUse126.addComponent("lblPreset126", lblPreset126);
		
		cbFilter126 = new JCheckBox("Exclude regular instruments");
		cbFilter126.setSelected(true);
		GridBagConstraints gbc_cbFilter126 = new GridBagConstraints();
		gbc_cbFilter126.anchor = GridBagConstraints.WEST;
		gbc_cbFilter126.gridwidth = 2;
		gbc_cbFilter126.insets = new Insets(0, 0, 5, 5);
		gbc_cbFilter126.gridx = 4;
		gbc_cbFilter126.gridy = 2;
		pnlSfx.add(cbFilter126, gbc_cbFilter126);
		cgUse126.addComponent("cbFilter126", cbFilter126);
		cbFilter126.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				cbFilter126Callback();
			}
		});
		
		cmbxPreset126 = new JComboBox<PresetInfo>();
		GridBagConstraints gbc_cmbxPreset126 = new GridBagConstraints();
		gbc_cmbxPreset126.gridwidth = 2;
		gbc_cmbxPreset126.insets = new Insets(0, 0, 5, 5);
		gbc_cmbxPreset126.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxPreset126.gridx = 4;
		gbc_cmbxPreset126.gridy = 1;
		pnlSfx.add(cmbxPreset126, gbc_cmbxPreset126);
		cgUse126.addComponent("cmbxPreset126", cmbxPreset126);
	}
	
	private void initSampleTab(){
		JPanel pnlSmpl = new JPanel();
		tabbedPane.addTab("Samples", null, pnlSmpl, null);
		GridBagLayout gbl_pnlSmpl = new GridBagLayout();
		gbl_pnlSmpl.columnWidths = new int[]{0, 0, 0};
		gbl_pnlSmpl.rowHeights = new int[]{0, 0};
		gbl_pnlSmpl.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_pnlSmpl.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		pnlSmpl.setLayout(gbl_pnlSmpl);
		
		cbtSamples = new CheckboxTable(new int[]
				{CheckboxTable.COLTYPE_STRING, CheckboxTable.COLTYPE_STRING, 
						CheckboxTable.COLTYPE_CHECKBOX});
		cbtSamples.setColumnName(SMPLTBL_COL_NAME, "Sample");
		cbtSamples.setColumnName(SMPLTBL_COL_WARNINGS, "Warnings");
		cbtSamples.setColumnName(SMPLTBL_COL_IMPORT, "Include");
		/*cbtSamples.setColumnCheckboxCallback(SMPLTBL_COL_IMPORT, new CheckCallback(){ 
			public void onAction(int row, JCheckBox cb) {
					if(bankInfo != null){
						if(row < 0) return;
						int scount = bankInfo.getSampleCount();
						if(row >= scount) return;
						SampleInfo sinfo = bankInfo.getSample(row);
						if(sinfo == null) return;
						sinfo.importSample = cb.isSelected();
					}
			}});*/
		
		GridBagConstraints gbc_pnlSmplList = new GridBagConstraints();
		gbc_pnlSmplList.insets = new Insets(0, 0, 0, 5);
		gbc_pnlSmplList.fill = GridBagConstraints.BOTH;
		gbc_pnlSmplList.gridx = 0;
		gbc_pnlSmplList.gridy = 0;
		pnlSmpl.add(cbtSamples, gbc_pnlSmplList);
		
		pnlSampleOps = new SampleOptionsPanel();
		GridBagConstraints gbc_pnlSmplOp = new GridBagConstraints();
		gbc_pnlSmplOp.fill = GridBagConstraints.BOTH;
		gbc_pnlSmplOp.gridx = 1;
		gbc_pnlSmplOp.gridy = 0;
		pnlSmpl.add(pnlSampleOps, gbc_pnlSmplOp);
	}
	
	/*----- Getters -----*/
	
	public boolean getExitSelection(){return exitSelection;}
	
	public boolean getInfoFromGUI(BankImportInfo info){
		if(bankInfo == null || info == null) return false;
		saveGUIToSubbank(selectedBank);
		updateSampleImportFlags();
		bankInfo.copyTo(info);
		return true;
	}
	
	public Set<String> getPresetTags(){return ptags;}
	public Set<String> getSampleTags(){return stags;}
	
	public int getSampleImportSettings(SampleImportOptions ops){
		return pnlSampleOps.getSettings(ops);
	}
	
	/*----- Setters -----*/
	
	public void loadInfoToGUI(BankImportInfo info){
		bankInfo = info.copy();
		selectedBank = null;
		updateBankList();
		populateBankCombobox(cmbxBank126);
		populateBankCombobox(cmbxBank127);
		loadSamplesToGUI();
	}
	
	/*----- Internal -----*/
	
	private void updateSampleImportFlags(){
		if(bankInfo != null){
			int scount = bankInfo.getSampleCount();
			for(int i = 0; i < scount; i++){
				SampleInfo sinfo = bankInfo.getSample(i);
				sinfo.importSample = cbtSamples.getCheckboxCellContents(i, SMPLTBL_COL_IMPORT);
			}
		}
	}
	
	private void loadSamplesToGUI(){
		if(bankInfo != null){
			int scount = bankInfo.getSampleCount();
			cbtSamples.allocateRows(scount);
			for(int i = 0; i < scount; i++){
				SampleInfo sinfo = bankInfo.getSample(i);
				if(sinfo != null){
					cbtSamples.setTextCellContents(i, SMPLTBL_COL_NAME, sinfo.name);
					cbtSamples.setTextCellContents(i, SMPLTBL_COL_WARNINGS, String.format("%08x", sinfo.warningFlags));	
					cbtSamples.setCheckboxCellContents(i, SMPLTBL_COL_IMPORT, sinfo.importSample);
				}
			}
		}
		else{
			cbtSamples.allocateRows(0);
		}
		
		reenable();
	}
	
	private void selectSubbank(int id){
		saveGUIToSubbank(selectedBank);
		
		//if(index < 0 || index >= bankInfo.getBankCount()) return;
		if(id >= 0) {
			selectedBank = bankInfo.getBank(id);
		}
		else {
			//Just pull first from list
			selectedBank = lstBanks.getModel().getElementAt(0);
		}
		loadSubbankToGUI(selectedBank);
	}
		
	private void loadSubbankToFontsInfoPanel(SubBank bnk){
		if(bnk != null){
			cbImportFont.setSelected(bnk.importAsFont);
			txtName.setText(bnk.name);
			txtEnum.setText(bnk.enumLabel);
			cmbxCache.setRawValue(bnk.cache);
			cmbxMedium.setRawValue(bnk.medium);
		}
		else{
			cbImportFont.setSelected(false);
			txtEnum.setText("<No selection>");
			txtName.setText("<No selection>");
		}
	}

	private void loadSubbankToInstPanel(SubBank bnk){
		if(bnk != null){
			//cbImportPerc.setSelected(bnk.importPercToFont);
			//cbSaveDrumset.setSelected(bnk.saveDrumset);
			//cbSaveDrum.setSelected(bnk.saveDrums);
			
			if(bnk.instruments != null){
				int mainInstAlloc = bnk.instruments.length;
				if(mainInstAlloc > 126) mainInstAlloc = 126;
				cbtInst.allocateRows(mainInstAlloc);
				//DefaultComboBoxModel<PresetInfo> mdl = new DefaultComboBoxModel<PresetInfo>();
				
				for(int i = 0; i < mainInstAlloc; i++){
					PresetInfo preset = bnk.instruments[i];
					if(!preset.emptySlot){
						//mdl.addElement(preset);
						cbtInst.setTextCellContents(i, INSTTBL_COL_INDEX, String.format("%03d", preset.index));
						cbtInst.setTextCellContents(i, INSTTBL_COL_NAME, preset.name);
						cbtInst.setTextCellContents(i, INSTTBL_COL_WARNINGS, String.format("%08x", preset.warningFlags));
						cbtInst.setCheckboxCellContents(i, INSTTBL_COL_TOFONT, preset.importToFont);
						cbtInst.setCheckboxCellContents(i, INSTTBL_COL_TOPRESET, preset.savePreset);
						cbtInst.setCheckboxCellContents(i, INSTTBL_COL_SAVEDRUMS, preset.saveDrums);
						cbtInst.setRowEnabled(i, true);
						cbtInst.setCellEnabled(i, INSTTBL_COL_SAVEDRUMS, preset.percInSrc);
					}
					else{
						cbtInst.setTextCellContents(i, INSTTBL_COL_INDEX, String.format("%03d", preset.index));
						cbtInst.setTextCellContents(i, INSTTBL_COL_NAME, "<Empty>");
						cbtInst.setTextCellContents(i, INSTTBL_COL_WARNINGS, "N/A");
						cbtInst.setCheckboxCellContents(i, INSTTBL_COL_TOFONT, false);
						cbtInst.setCheckboxCellContents(i, INSTTBL_COL_TOPRESET, false);
						cbtInst.setCheckboxCellContents(i, INSTTBL_COL_SAVEDRUMS, false);
						cbtInst.setRowEnabled(i, false);
						
						//Dummy to keep indices the same
						/*preset = new PresetInfo();
						preset.index = i;
						preset.name = "<Empty>";
						mdl.addElement(preset);*/
					}
				}
				//cmbxPerc.setModel(mdl);
				
				if(bnk.instruments.length > 126) {
					cbtExtInst.allocateRows(2);
					for(int i = 0; i < 2; i++){
						int pidx = i + 126;
						PresetInfo preset = null;
						if(pidx < bnk.instruments.length) preset = bnk.instruments[pidx];
						if((preset != null) && (!preset.emptySlot)) {
							cbtExtInst.setTextCellContents(i, EXTRA_INSTTBL_COL_INDEX, String.format("%03d", preset.index));
							cbtExtInst.setTextCellContents(i, EXTRA_INSTTBL_COL_NAME, preset.name);
							cbtExtInst.setTextCellContents(i, EXTRA_INSTTBL_COL_WARNINGS, String.format("%08x", preset.warningFlags));
							cbtExtInst.setCheckboxCellContents(i, EXTRA_INSTTBL_COL_TOFONT, preset.importToFont);
							cbtExtInst.setCheckboxCellContents(i, EXTRA_INSTTBL_COL_TOPRESET, preset.savePreset);
							cbtExtInst.setCheckboxCellContents(i, EXTRA_INSTTBL_COL_SAVEDRUMS, preset.saveDrums);
							if(preset.movedToIndex >= 0) {
								cbtExtInst.setTextCellContents(i, EXTRA_INSTTBL_COL_NEWIDX, String.format("%03d", preset.movedToIndex));
							}
							else {
								cbtExtInst.setTextCellContents(i, EXTRA_INSTTBL_COL_NEWIDX, "N/A");
							}
							cbtExtInst.setRowEnabled(i, true);
							cbtExtInst.setCellEnabled(i, EXTRA_INSTTBL_COL_SAVEDRUMS, preset.percInSrc);
						}
						else {
							cbtExtInst.setTextCellContents(i, EXTRA_INSTTBL_COL_INDEX, String.format("%03d", preset.index));
							cbtExtInst.setTextCellContents(i, EXTRA_INSTTBL_COL_NAME, "<Empty>");
							cbtExtInst.setTextCellContents(i, EXTRA_INSTTBL_COL_WARNINGS, "N/A");
							cbtExtInst.setTextCellContents(i, EXTRA_INSTTBL_COL_NEWIDX, "N/A");
							cbtExtInst.setCheckboxCellContents(i, EXTRA_INSTTBL_COL_TOFONT, false);
							cbtExtInst.setCheckboxCellContents(i, EXTRA_INSTTBL_COL_TOPRESET, false);
							cbtExtInst.setCheckboxCellContents(i, EXTRA_INSTTBL_COL_SAVEDRUMS, false);
							cbtExtInst.setRowEnabled(i, false);
						}
					}
				}
				else {
					cbtExtInst.allocateRows(0);
				}
				
				
				cbUsePerc.setSelected(bnk.importPercToFont);
				cbUseSfx.setSelected(bnk.importSfxToFont);
				cbSave127.setSelected(bnk.saveDrumset);
				cbSaveDrums127.setSelected(bnk.saveDrums);
				cbSaveSfx.setSelected(bnk.saveSfx);
				updateCmbxSelectionTo(cmbxBank127, cmbxPreset127, bnk.percBank, bnk.percInst, cbFilter127.isSelected());
				updateCmbxSelectionTo(cmbxBank126, cmbxPreset126, bnk.sfxBank, bnk.sfxInst, cbFilter126.isSelected());
			}
			else{
				cbtInst.allocateRows(0);
				cbtExtInst.allocateRows(0);
				//cmbxPerc.setModel(new DefaultComboBoxModel<PresetInfo>());
			}
		}
		else{
			cbtInst.allocateRows(0);
			cbtExtInst.allocateRows(0);
			//cmbxPerc.setModel(new DefaultComboBoxModel<PresetInfo>());
			//cbImportPerc.setSelected(false);
			//cbSaveDrumset.setSelected(false);
			//cbSaveDrum.setSelected(false);
			cbUsePerc.setSelected(false);
			cbUseSfx.setSelected(false);
			cbSave127.setSelected(false);
			cbSaveDrums127.setSelected(false);
			cbSaveSfx.setSelected(false);
		}
		
	}
	
	private void loadSubbankToGUI(SubBank bnk){
		loadSubbankToFontsInfoPanel(bnk);
		loadSubbankToInstPanel(bnk);
		reenable();
	}
	
	private void saveFontInfoPanelToSubbank(SubBank bnk){
		if(bnk == null) return;
		bnk.importAsFont = cbImportFont.isSelected();
		bnk.name = txtName.getText();
		bnk.enumLabel = txtEnum.getText();
		bnk.cache = cmbxCache.getRawValue();
		bnk.medium = cmbxMedium.getRawValue();
	}
	
	private void saveInstPanelToSubbank(SubBank bnk){
		if(bnk == null) return;
		
		//Main inst table
		int icount = cbtInst.getAllocatedRowCount();
		int eicount = cbtExtInst.getAllocatedRowCount();
		if (eicount > 0) icount = 126;
		bnk.allocInstruments(icount + eicount);
		for(int i = 0; i < icount; i++){
			bnk.instruments[i].index = i;
			boolean notEmpty = cbtInst.rowEnabled(i);
			if(notEmpty){
				bnk.instruments[i].emptySlot = false;
				bnk.instruments[i].name = cbtInst.getTextCellContents(i, INSTTBL_COL_NAME);
				bnk.instruments[i].warningFlags = Integer.parseUnsignedInt(cbtInst.getTextCellContents(i, INSTTBL_COL_WARNINGS), 16);
				bnk.instruments[i].importToFont = cbtInst.getCheckboxCellContents(i, INSTTBL_COL_TOFONT);
				bnk.instruments[i].savePreset = cbtInst.getCheckboxCellContents(i, INSTTBL_COL_TOPRESET);
				bnk.instruments[i].percInSrc = cbtInst.cellEnabled(i, INSTTBL_COL_SAVEDRUMS);
				bnk.instruments[i].saveDrums = cbtInst.getCheckboxCellContents(i, INSTTBL_COL_SAVEDRUMS);
			}
			else{
				bnk.instruments[i].emptySlot = true;
				bnk.instruments[i].importToFont = false;
				bnk.instruments[i].savePreset = false;
			}
		}
		
		//Extra slots table
		for(int i = 0; i < eicount; i++) {
			int ii = 126 + i;
			bnk.instruments[ii].index = ii;
			boolean notEmpty = cbtExtInst.rowEnabled(i);
			if(notEmpty){
				bnk.instruments[ii].emptySlot = false;
				bnk.instruments[ii].name = cbtExtInst.getTextCellContents(i, EXTRA_INSTTBL_COL_NAME);
				bnk.instruments[ii].warningFlags = Integer.parseUnsignedInt(cbtExtInst.getTextCellContents(i, EXTRA_INSTTBL_COL_WARNINGS), 16);
				bnk.instruments[ii].importToFont = cbtExtInst.getCheckboxCellContents(i, EXTRA_INSTTBL_COL_TOFONT);
				bnk.instruments[ii].savePreset = cbtExtInst.getCheckboxCellContents(i, EXTRA_INSTTBL_COL_TOPRESET);
				bnk.instruments[ii].percInSrc = cbtExtInst.cellEnabled(i, EXTRA_INSTTBL_COL_SAVEDRUMS);
				bnk.instruments[ii].saveDrums = cbtExtInst.getCheckboxCellContents(i, EXTRA_INSTTBL_COL_SAVEDRUMS);
				
				String ccont = cbtExtInst.getTextCellContents(i, EXTRA_INSTTBL_COL_NEWIDX);
				if((ccont == null) || ccont.equals("N/A")) {
					bnk.instruments[ii].movedToIndex = -1;
				}
				else {
					bnk.instruments[ii].movedToIndex = Integer.parseInt(ccont);	
				}
			}
			else{
				bnk.instruments[ii].emptySlot = true;
				bnk.instruments[ii].importToFont = false;
				bnk.instruments[ii].savePreset = false;
				bnk.instruments[ii].saveDrums = false;
				bnk.instruments[ii].movedToIndex = -1;
			}
		}
		
		//Slot 126
		bnk.importSfxToFont = cbUseSfx.isSelected();
		bnk.saveSfx = cbSaveSfx.isSelected();
		bnk.sfxBank = getSelectedBank126Id();
		bnk.sfxInst = getSelectedPreset126Index();
		
		//Slot 127
		bnk.importPercToFont = cbUsePerc.isSelected();
		bnk.saveDrumset = cbSave127.isSelected();
		bnk.saveDrums = cbSaveDrums127.isSelected();
		bnk.percBank = getSelectedBank127Id();
		bnk.percInst = getSelectedPreset127Index();
	}
	
	private void saveGUIToSubbank(SubBank bnk){
		saveFontInfoPanelToSubbank(bnk);
		saveInstPanelToSubbank(bnk);
	}
	
	/*----- GUI Management -----*/
	
	private void populateBankCombobox(JComboBox<SubBank> cmbxBank) {
		DefaultComboBoxModel<SubBank> mdl = new DefaultComboBoxModel<SubBank>();
		if(bankInfo != null) {
			List<Integer> bnkIds = bankInfo.getAllBankIds();
			for(Integer id : bnkIds) {
				mdl.addElement(bankInfo.getBank(id));
			}
		}
		cmbxBank.setModel(mdl);
	}
	
	private static void populatePresetCombobox(JComboBox<PresetInfo> cmbxPreset, SubBank bank, boolean percFilter) {
		DefaultComboBoxModel<PresetInfo> mdl = new DefaultComboBoxModel<PresetInfo>();
		if(bank != null) {
			int icount = bank.instruments.length;
			for(int i = 0; i < icount; i++) {
				if(bank.instruments[i] == null) continue;
				if(bank.instruments[i].emptySlot) continue;
				if(percFilter && !bank.instruments[i].percInSrc) continue;
				mdl.addElement(bank.instruments[i]);
			}
		}
		cmbxPreset.setModel(mdl);
		cmbxPreset.setSelectedIndex(-1);
	}
	
	private void updateCmbxSelectionTo(JComboBox<SubBank> cmbxBank, JComboBox<PresetInfo> cmbxPreset, int bankId, int presetId, boolean percFilter) {
		SubBank selbnk = null;
		int selidx = -1;
		ComboBoxModel<SubBank> bmdl = cmbxBank.getModel();
		int count = bmdl.getSize();
		for(int i = 0; i < count; i++) {
			SubBank bnk = bmdl.getElementAt(i);
			if(bnk.bankIndex == bankId) {
				selbnk = bnk;
				selidx = i;
				break;
			}
		}
		
		cmbxBank.setSelectedIndex(selidx);
		populatePresetCombobox(cmbxPreset, selbnk, percFilter);
		if(selbnk == null) return;
		selidx = -1;
		ComboBoxModel<PresetInfo> pmdl = cmbxPreset.getModel();
		count = pmdl.getSize();
		for(int i = 0; i < count; i++) {
			PresetInfo preset = pmdl.getElementAt(i);
			if(preset.index == presetId) {
				selidx = i;
				break;
			}
		}
		
		cmbxPreset.setSelectedIndex(selidx);
	}
	
	private static SubBank getBankComboBoxSelection(JComboBox<SubBank> cmbxBank) {
		ComboBoxModel<SubBank> bmdl = cmbxBank.getModel();
		return bmdl.getElementAt(cmbxBank.getSelectedIndex());
	}
	
	private static PresetInfo getPresetComboBoxSelection(JComboBox<PresetInfo> cmbxPreset) {
		ComboBoxModel<PresetInfo> mdl = cmbxPreset.getModel();
		return mdl.getElementAt(cmbxPreset.getSelectedIndex());
	}
	
	private int getSelectedBank126Id() {
		SubBank bnk = getBankComboBoxSelection(cmbxBank126);
		if(bnk == null) return -1;
		return bnk.bankIndex;
	}
	
	private int getSelectedPreset126Index() {
		PresetInfo preset = getPresetComboBoxSelection(cmbxPreset126);
		if(preset == null) return -1;
		return preset.index;
	}
	
	private int getSelectedBank127Id() {
		SubBank bnk = getBankComboBoxSelection(cmbxBank127);
		if(bnk == null) return -1;
		return bnk.bankIndex;
	}
	
	private int getSelectedPreset127Index() {
		PresetInfo preset = getPresetComboBoxSelection(cmbxPreset127);
		if(preset == null) return -1;
		return preset.index;
	}
	
 	private void clearBankList(){
		selectedBank = null;
		lstBanks.setModel(new DefaultListModel<SubBank>());
	}
	
	private void updateBankList(){
		if(bankInfo == null){
			clearBankList();
			return;
		}
		
		List<Integer> idlist = bankInfo.getAllBankIds();
		if(idlist.isEmpty()){
			clearBankList();
			return;
		}
		
		//int selidx = lstBanks.getSelectedIndex();
		SubBank selobj = lstBanks.getSelectedValue();
		DefaultListModel<SubBank> mdl = new DefaultListModel<SubBank>();
		for(Integer id : idlist) {
			mdl.addElement(bankInfo.getBank(id));
		}

		lstBanks.setModel(mdl);
		if(selobj != null){
			selectSubbank(selobj.bankIndex);
		}
		else{
			selectSubbank(-1);
		}
	}
	
	public void reenable(){
		super.reenable();
		cgSubLoaded.setEnabling(selectedBank != null);
		cbtInst.updateEnabling();
		cbtExtInst.updateEnabling();
		cbtSamples.updateEnabling();
		
		if(bankInfo != null){
			pnlSampleOps.reenable();
			cgUse126.setEnabling(cbUseSfx.isSelected());
			cgUse127.setEnabling(cbUsePerc.isSelected());
		}
		else pnlSampleOps.disableAll();
		//repaint();
	}
	
	public void disableAll(){
		super.disableAll();
		cbtInst.disableAll();
		cbtExtInst.disableAll();
		cbtSamples.disableAll();
	}
	
	public void repaint(){
		super.repaint();
		cbtInst.repaintAll();
		cbtExtInst.repaintAll();
		cbtSamples.repaintAll();
	}
	
	/*----- Callbacks -----*/
	
	private void cmbxBank126Callback() {
		setWait();
		SubBank sel = getBankComboBoxSelection(cmbxBank126);
		populatePresetCombobox(cmbxPreset126, sel, cbFilter126.isSelected());
		unsetWait();
	}
	
	private void cmbxBank127Callback() {
		setWait();
		SubBank sel = getBankComboBoxSelection(cmbxBank127);
		populatePresetCombobox(cmbxPreset127, sel, cbFilter127.isSelected());
		unsetWait();
	}
	
	private void cbUse126Callback() {
		reenable();
	}
	
	private void cbUse127Callback() {
		reenable();
	}
	
	private void cbFilter126Callback() {
		setWait();
		SubBank sel = getBankComboBoxSelection(cmbxBank126);
		populatePresetCombobox(cmbxPreset126, sel, cbFilter126.isSelected());
		unsetWait();
	}
	
	private void cbFilter127Callback() {
		setWait();
		SubBank sel = getBankComboBoxSelection(cmbxBank127);
		populatePresetCombobox(cmbxPreset127, sel, cbFilter127.isSelected());
		unsetWait();
	}
	
	private void btnOkayCallback(){
		exitSelection = true;
		closeMe();
	}
	
	private void btnCancelCallback(){
		exitSelection = false;
		closeMe();
	}
	
	private void btnPTagsCallback(){
		ZeqerTagEditDialog dialog = new ZeqerTagEditDialog(parent);
		if(ptags != null) dialog.loadTags(ptags);
		dialog.showMe(this);
		
		if(dialog.getExitSelection()){
			if(ptags == null) ptags = new HashSet<String>();
			else ptags.clear();
			ptags.addAll(dialog.getTags());
		}
	}
	
	private void btnSTagsCallback(){
		ZeqerTagEditDialog dialog = new ZeqerTagEditDialog(parent);
		if(stags != null) dialog.loadTags(stags);
		dialog.showMe(this);
		
		if(dialog.getExitSelection()){
			if(stags == null) stags = new HashSet<String>();
			else stags.clear();
			stags.addAll(dialog.getTags());
		}
	}
	
	private void lstFontSelectCallback(){
		setWait();
		SubBank selobj = lstBanks.getSelectedValue();
		if(selobj != null){
			selectSubbank(selobj.bankIndex);
		}
		else {
			selectSubbank(-1);
		}
		unsetWait();
	}
	
}
