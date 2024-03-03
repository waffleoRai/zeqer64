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
import java.util.Set;

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

public class BankImportDialog extends WRDialog{
	
	/*----- Constants -----*/
	
	private static final long serialVersionUID = 4901070415958862918L;
	
	public static final int MIN_WIDTH = 640;
	public static final int MIN_HEIGHT = 480;
	
	private static final int INSTTBL_COL_INDEX = 0;
	private static final int INSTTBL_COL_NAME = 1;
	private static final int INSTTBL_COL_WARNINGS = 2; //String of flag field in hex until graphics done.
	private static final int INSTTBL_COL_TOFONT = 3;
	private static final int INSTTBL_COL_TOPRESET = 4;
	
	private static final int SMPLTBL_COL_NAME = 0;
	private static final int SMPLTBL_COL_WARNINGS = 1; //String of flag field in hex until graphics done.
	private static final int SMPLTBL_COL_IMPORT = 2;
	
	/*----- Instance Variables -----*/
	
	private boolean exitSelection = false;
	
	private JTabbedPane tabbedPane;
	private ComponentGroup cgSubLoaded;
	
	private JCheckBox cbImportFont;
	private JTextField txtEnum;
	private JTextField txtName;
	private CacheTypeCombobox cmbxCache;
	private MediumTypeCombobox cmbxMedium;
	private JList<SubBank> lstBanks;
	
	private CheckboxTable cbtInst;
	private CheckboxTable cbtSamples;
	
	private JComboBox<PresetInfo> cmbxPerc;
	private JCheckBox cbImportPerc;
	private JCheckBox cbSaveDrumset;
	private JCheckBox cbSaveDrum;
	
	private SampleOptionsPanel pnlSampleOps;
	
	//Tags to apply to all incoming presets and samples
	private Set<String> ptags;
	private Set<String> stags;
	
	private SubBank selectedBank;
	private BankImportInfo bankInfo;
	
	/*----- Init -----*/

	public BankImportDialog(JFrame parent){
		super(parent, true);
		cgSubLoaded = globalEnable.newChild();
		
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
		btnImport.addActionListener(new ActionListener(){
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
		gbl_pnlInst.rowHeights = new int[]{0, 0, 0};
		gbl_pnlInst.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlInst.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		pnlInst.setLayout(gbl_pnlInst);
		
		cbtInst = new CheckboxTable(new int[]
				{CheckboxTable.COLTYPE_STRING, CheckboxTable.COLTYPE_STRING,
					CheckboxTable.COLTYPE_STRING,
				CheckboxTable.COLTYPE_CHECKBOX, CheckboxTable.COLTYPE_CHECKBOX});
		cbtInst.setColumnName(INSTTBL_COL_INDEX, "Slot");
		cbtInst.setColumnName(INSTTBL_COL_NAME, "Name");
		cbtInst.setColumnName(INSTTBL_COL_WARNINGS, "Warnings");
		cbtInst.setColumnName(INSTTBL_COL_TOFONT, "Include in Font");
		cbtInst.setColumnName(INSTTBL_COL_TOPRESET, "Save Preset");
		
		GridBagConstraints gbc_pnlStdInst = new GridBagConstraints();
		gbc_pnlStdInst.insets = new Insets(5, 5, 5, 5);
		gbc_pnlStdInst.fill = GridBagConstraints.BOTH;
		gbc_pnlStdInst.gridx = 0;
		gbc_pnlStdInst.gridy = 0;
		pnlInst.add(cbtInst, gbc_pnlStdInst);
		
		JPanel pnlPerc = new JPanel();
		pnlPerc.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlPerc = new GridBagConstraints();
		gbc_pnlPerc.insets = new Insets(0, 5, 0, 5);
		gbc_pnlPerc.fill = GridBagConstraints.BOTH;
		gbc_pnlPerc.gridx = 0;
		gbc_pnlPerc.gridy = 1;
		pnlInst.add(pnlPerc, gbc_pnlPerc);
		GridBagLayout gbl_pnlPerc = new GridBagLayout();
		gbl_pnlPerc.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_pnlPerc.rowHeights = new int[]{0, 0};
		gbl_pnlPerc.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlPerc.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlPerc.setLayout(gbl_pnlPerc);
		
		JLabel lblPercussion = new JLabel("Percussion:");
		GridBagConstraints gbc_lblPercussion = new GridBagConstraints();
		gbc_lblPercussion.anchor = GridBagConstraints.EAST;
		gbc_lblPercussion.insets = new Insets(0, 5, 0, 5);
		gbc_lblPercussion.gridx = 0;
		gbc_lblPercussion.gridy = 0;
		pnlPerc.add(lblPercussion, gbc_lblPercussion);
		
		cmbxPerc = new JComboBox<PresetInfo>();
		GridBagConstraints gbc_cmbxPerc = new GridBagConstraints();
		gbc_cmbxPerc.insets = new Insets(0, 0, 0, 5);
		gbc_cmbxPerc.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxPerc.gridx = 1;
		gbc_cmbxPerc.gridy = 0;
		pnlPerc.add(cmbxPerc, gbc_cmbxPerc);
		cgSubLoaded.addComponent("cmbxPerc", cmbxPerc);
		
		cbImportPerc = new JCheckBox("Import to Font");
		GridBagConstraints gbc_cbImportPerc = new GridBagConstraints();
		gbc_cbImportPerc.insets = new Insets(0, 0, 0, 5);
		gbc_cbImportPerc.gridx = 2;
		gbc_cbImportPerc.gridy = 0;
		pnlPerc.add(cbImportPerc, gbc_cbImportPerc);
		cgSubLoaded.addComponent("cbImportPerc", cbImportPerc);
		
		cbSaveDrumset = new JCheckBox("Save Drumset");
		GridBagConstraints gbc_cbSaveDrumset = new GridBagConstraints();
		gbc_cbSaveDrumset.insets = new Insets(0, 0, 0, 5);
		gbc_cbSaveDrumset.gridx = 3;
		gbc_cbSaveDrumset.gridy = 0;
		pnlPerc.add(cbSaveDrumset, gbc_cbSaveDrumset);
		cgSubLoaded.addComponent("cbSaveDrumset", cbSaveDrumset);
		
		cbSaveDrum = new JCheckBox("Save Drums");
		GridBagConstraints gbc_cbSaveDrum = new GridBagConstraints();
		gbc_cbSaveDrum.insets = new Insets(0, 0, 0, 5);
		gbc_cbSaveDrum.gridx = 4;
		gbc_cbSaveDrum.gridy = 0;
		pnlPerc.add(cbSaveDrum, gbc_cbSaveDrum);
		cgSubLoaded.addComponent("cbSaveDrum", cbSaveDrum);
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
	
	private void selectSubbank(int index){
		saveGUIToSubbank(selectedBank);
		
		if(index < 0 || index >= bankInfo.getBankCount()) return;
		selectedBank = bankInfo.getBank(index);
		loadSubbankToGUI(selectedBank);
	}
		
	private void loadSubbankToFontsInfoPanel(SubBank bnk){
		if(bnk != null){
			cbImportFont.setSelected(bnk.importAsFont);
			txtEnum.setText(bnk.name);
			txtName.setText(bnk.enumLabel);
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
			cbImportPerc.setSelected(bnk.importPercToFont);
			cbSaveDrumset.setSelected(bnk.saveDrumset);
			cbSaveDrum.setSelected(bnk.saveDrums);
			
			if(bnk.instruments != null){
				cbtInst.allocateRows(bnk.instruments.length);
				DefaultComboBoxModel<PresetInfo> mdl = new DefaultComboBoxModel<PresetInfo>();
				
				for(int i = 0; i < bnk.instruments.length; i++){
					PresetInfo preset = bnk.instruments[i];
					if(!preset.emptySlot){
						mdl.addElement(preset);
						cbtInst.setTextCellContents(i, INSTTBL_COL_INDEX, String.format("%03d", preset.index));
						cbtInst.setTextCellContents(i, INSTTBL_COL_NAME, preset.name);
						cbtInst.setTextCellContents(i, INSTTBL_COL_WARNINGS, String.format("%08x", preset.warningFlags));
						cbtInst.setCheckboxCellContents(i, INSTTBL_COL_TOFONT, preset.importToFont);
						cbtInst.setCheckboxCellContents(i, INSTTBL_COL_TOPRESET, preset.savePreset);
						cbtInst.setRowEnabled(i, true);
					}
					else{
						cbtInst.setTextCellContents(i, INSTTBL_COL_INDEX, String.format("%03d", preset.index));
						cbtInst.setTextCellContents(i, INSTTBL_COL_NAME, "<Empty>");
						cbtInst.setTextCellContents(i, INSTTBL_COL_WARNINGS, "N/A");
						cbtInst.setCheckboxCellContents(i, INSTTBL_COL_TOFONT, false);
						cbtInst.setCheckboxCellContents(i, INSTTBL_COL_TOPRESET, false);
						cbtInst.setRowEnabled(i, false);
						
						//Dummy to keep indices the same
						preset = new PresetInfo();
						preset.index = i;
						preset.name = "<Empty>";
						mdl.addElement(preset);
					}
				}
				cmbxPerc.setModel(mdl);
			}
			else{
				cbtInst.allocateRows(0);
				cmbxPerc.setModel(new DefaultComboBoxModel<PresetInfo>());
			}
		}
		else{
			cbtInst.allocateRows(0);
			cmbxPerc.setModel(new DefaultComboBoxModel<PresetInfo>());
			cbImportPerc.setSelected(false);
			cbSaveDrumset.setSelected(false);
			cbSaveDrum.setSelected(false);
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
		bnk.importPercToFont = cbImportPerc.isSelected();
		bnk.saveDrumset = cbSaveDrumset.isSelected();
		bnk.saveDrums = cbSaveDrum.isSelected();
		
		if(bnk.importPercToFont){
			bnk.percInst = cmbxPerc.getSelectedIndex();
		}
		else bnk.percInst = -1;
		
		int icount = cbtInst.getAllocatedRowCount();
		bnk.allocInstruments(icount);
		for(int i = 0; i < icount; i++){
			bnk.instruments[i].index = i;
			boolean notEmpty = cbtInst.rowEnabled(i);
			if(notEmpty){
				bnk.instruments[i].emptySlot = false;
				bnk.instruments[i].name = cbtInst.getTextCellContents(i, INSTTBL_COL_NAME);
				bnk.instruments[i].importToFont = cbtInst.getCheckboxCellContents(i, INSTTBL_COL_TOFONT);
				bnk.instruments[i].savePreset = cbtInst.getCheckboxCellContents(i, INSTTBL_COL_TOPRESET);
			}
			else{
				bnk.instruments[i].emptySlot = true;
				bnk.instruments[i].importToFont = false;
				bnk.instruments[i].savePreset = false;
			}
		}
	}
	
	private void saveGUIToSubbank(SubBank bnk){
		saveFontInfoPanelToSubbank(bnk);
		saveInstPanelToSubbank(bnk);
	}
	
	/*----- GUI Management -----*/
	
	private void clearBankList(){
		selectedBank = null;
		lstBanks.setModel(new DefaultListModel<SubBank>());
	}
	
	private void updateBankList(){
		if(bankInfo == null){
			clearBankList();
			return;
		}
		
		int count = bankInfo.getBankCount();
		if(count < 1){
			clearBankList();
			return;
		}
		
		int selidx = lstBanks.getSelectedIndex();
		DefaultListModel<SubBank> mdl = new DefaultListModel<SubBank>();
		for(int i = 0; i < count; i++){
			mdl.addElement(bankInfo.getBank(i));
		}
		
		lstBanks.setModel(mdl);
		if(selidx > 0 && selidx < count){
			selectSubbank(selidx);
		}
		else{
			selectSubbank(0);
		}
	}
	
	public void reenable(){
		super.reenable();
		cgSubLoaded.setEnabling(selectedBank != null);
		cbtInst.updateEnabling();
		cbtSamples.updateEnabling();
		
		if(bankInfo != null){
			pnlSampleOps.reenable();
		}
		else pnlSampleOps.disableAll();
	}
	
	public void disableAll(){
		super.disableAll();
		cbtInst.disableAll();
		cbtSamples.disableAll();
	}
	
	public void repaint(){
		super.repaint();
		cbtInst.repaintAll();
		cbtSamples.repaintAll();
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
		int selidx = lstBanks.getSelectedIndex();
		if(selidx >= 0){
			selectSubbank(selidx);
		}
		unsetWait();
	}
	
}
