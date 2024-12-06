package waffleoRai_zeqer64.GUI.dialogs;

import javax.swing.JFrame;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.WRDialog;
import waffleoRai_zeqer64.ZeqerBank;
import waffleoRai_zeqer64.GUI.CacheTypeCombobox;
import waffleoRai_zeqer64.GUI.SeqTypeCombobox;
import waffleoRai_zeqer64.filefmt.seq.ZeqerSeqIO;
import waffleoRai_zeqer64.filefmt.seq.ZeqerSeqIO.SeqImportOptions;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;

import java.awt.GridBagLayout;
import javax.swing.JPanel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.border.EtchedBorder;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JSeparator;

public class SeqImportDialog extends WRDialog{

	/*----- Constants -----*/
	
	private static final long serialVersionUID = -520793174057249944L;
	
	public static final int WIDTH = 475;
	public static final int HEIGHT = 350;
	
	/*----- Instance Variables -----*/
	
	private ZeqerCoreInterface core = null;
	
	private ComponentGroup fileEnabled; //Enabled when there is a file selected
	private ComponentGroup customLoopGroup;
	private ComponentGroup timeSigGroup;
	
	private SeqTypeCombobox cmbxCategory;
	private CacheTypeCombobox cmbxCache;
	
	private Set<String> tags;
	
	private boolean exitSelection = false;
	private String lastImportPath = null;
	
	private JLabel lblFontName;
	private int fontUid = 0;
	
	private JCheckBox cbMaxVox;
	private JCheckBox cbTempoCap;
	private JCheckBox cbTimesig;
	private JCheckBox cbLoop;
	
	private JTextField txtFilePath;
	private JTextField txtLoopM;
	private JTextField txtLoopBt;
	private JTextField txtMaxVox;
	private JTextField txtName;
	private JTextField txtEnum;
	private JTextField txtTimeSigDiv;
	private JTextField txtTimeSigBeats;

	/*----- Init -----*/
	
	public SeqImportDialog(JFrame parent) {
		super(parent, true);
		constructMe();
	}
	
	public SeqImportDialog(JFrame parent, ZeqerCoreInterface coreIFace) {
		super(parent, true);
		core = coreIFace;
		constructMe();
	}
	
	private void constructMe() {
		fileEnabled = globalEnable.newChild();
		customLoopGroup = globalEnable.newChild();
		timeSigGroup = globalEnable.newChild();
		initGUI();
		tags = new HashSet<String>();
	}
	
	private void initGUI() {
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		
		setTitle("Import Sequence");
		setResizable(false);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{200, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel pnlOptions = new JPanel();
		GridBagConstraints gbc_pnlOptions = new GridBagConstraints();
		gbc_pnlOptions.insets = new Insets(5, 5, 5, 5);
		gbc_pnlOptions.fill = GridBagConstraints.BOTH;
		gbc_pnlOptions.gridx = 0;
		gbc_pnlOptions.gridy = 0;
		getContentPane().add(pnlOptions, gbc_pnlOptions);
		GridBagLayout gbl_pnlOptions = new GridBagLayout();
		gbl_pnlOptions.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlOptions.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_pnlOptions.columnWeights = new double[]{1.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlOptions.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		pnlOptions.setLayout(gbl_pnlOptions);
		
		JLabel lblNewLabel_3 = new JLabel("Name:");
		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_3.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_3.gridx = 0;
		gbc_lblNewLabel_3.gridy = 0;
		pnlOptions.add(lblNewLabel_3, gbc_lblNewLabel_3);
		
		txtName = new JTextField();
		GridBagConstraints gbc_txtName = new GridBagConstraints();
		gbc_txtName.gridwidth = 2;
		gbc_txtName.insets = new Insets(0, 0, 5, 5);
		gbc_txtName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtName.gridx = 1;
		gbc_txtName.gridy = 0;
		pnlOptions.add(txtName, gbc_txtName);
		txtName.setColumns(10);
		globalEnable.addComponent("txtName", txtName);
		
		cmbxCategory = new SeqTypeCombobox();
		GridBagConstraints gbc_cmbxCategory = new GridBagConstraints();
		gbc_cmbxCategory.gridwidth = 2;
		gbc_cmbxCategory.insets = new Insets(0, 0, 5, 5);
		gbc_cmbxCategory.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxCategory.gridx = 1;
		gbc_cmbxCategory.gridy = 1;
		pnlOptions.add(cmbxCategory, gbc_cmbxCategory);
		globalEnable.addComponent("cmbxCategory", cmbxCategory);
		
		JLabel lclCategory = new JLabel("Category:");
		GridBagConstraints gbc_lclCategory = new GridBagConstraints();
		gbc_lclCategory.insets = new Insets(0, 0, 5, 5);
		gbc_lclCategory.anchor = GridBagConstraints.EAST;
		gbc_lclCategory.gridx = 0;
		gbc_lclCategory.gridy = 1;
		pnlOptions.add(lclCategory, gbc_lclCategory);
		
		JLabel lblNewLabel_4 = new JLabel("Enum:");
		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_4.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_4.gridx = 0;
		gbc_lblNewLabel_4.gridy = 2;
		pnlOptions.add(lblNewLabel_4, gbc_lblNewLabel_4);
		
		txtEnum = new JTextField();
		GridBagConstraints gbc_txtEnum = new GridBagConstraints();
		gbc_txtEnum.gridwidth = 2;
		gbc_txtEnum.insets = new Insets(0, 0, 5, 5);
		gbc_txtEnum.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtEnum.gridx = 1;
		gbc_txtEnum.gridy = 2;
		pnlOptions.add(txtEnum, gbc_txtEnum);
		txtEnum.setColumns(10);
		globalEnable.addComponent("txtEnum", txtEnum);
		
		JLabel lblNewLabel_5 = new JLabel("Cache:");
		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
		gbc_lblNewLabel_5.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_5.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_5.gridx = 0;
		gbc_lblNewLabel_5.gridy = 3;
		pnlOptions.add(lblNewLabel_5, gbc_lblNewLabel_5);
		
		cmbxCache = new CacheTypeCombobox();
		GridBagConstraints gbc_cmbxCache = new GridBagConstraints();
		gbc_cmbxCache.gridwidth = 2;
		gbc_cmbxCache.insets = new Insets(0, 0, 5, 5);
		gbc_cmbxCache.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxCache.gridx = 1;
		gbc_cmbxCache.gridy = 3;
		pnlOptions.add(cmbxCache, gbc_cmbxCache);
		globalEnable.addComponent("cmbxCache", cmbxCache);
		
		JLabel lblNewLabel_6 = new JLabel("Soundfont:");
		lblNewLabel_6.setHorizontalAlignment(SwingConstants.TRAILING);
		GridBagConstraints gbc_lblNewLabel_6 = new GridBagConstraints();
		gbc_lblNewLabel_6.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_6.fill = GridBagConstraints.BOTH;
		gbc_lblNewLabel_6.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel_6.gridx = 0;
		gbc_lblNewLabel_6.gridy = 4;
		pnlOptions.add(lblNewLabel_6, gbc_lblNewLabel_6);
		
		lblFontName = new JLabel("<none>");
		GridBagConstraints gbc_lblFontName = new GridBagConstraints();
		gbc_lblFontName.gridwidth = 2;
		gbc_lblFontName.insets = new Insets(0, 0, 5, 5);
		gbc_lblFontName.gridx = 1;
		gbc_lblFontName.gridy = 4;
		pnlOptions.add(lblFontName, gbc_lblFontName);
		
		JButton btnFont = new JButton("Set Soundfont...");
		GridBagConstraints gbc_btnFont = new GridBagConstraints();
		gbc_btnFont.insets = new Insets(0, 0, 5, 5);
		gbc_btnFont.gridx = 2;
		gbc_btnFont.gridy = 5;
		pnlOptions.add(btnFont, gbc_btnFont);
		globalEnable.addComponent("btnFont", btnFont);
		btnFont.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnSetSoundfontCallback();
			}
		});
		
		JButton btnTags = new JButton("Tags...");
		GridBagConstraints gbc_btnTags = new GridBagConstraints();
		gbc_btnTags.insets = new Insets(0, 0, 5, 5);
		gbc_btnTags.gridx = 0;
		gbc_btnTags.gridy = 7;
		pnlOptions.add(btnTags, gbc_btnTags);
		globalEnable.addComponent("btnTags", btnTags);
		btnTags.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnTagsCallback();
			}
		});
		
		JPanel pnlConvertOps = new JPanel();
		GridBagConstraints gbc_pnlConvertOps = new GridBagConstraints();
		gbc_pnlConvertOps.insets = new Insets(5, 0, 5, 0);
		gbc_pnlConvertOps.fill = GridBagConstraints.BOTH;
		gbc_pnlConvertOps.gridx = 1;
		gbc_pnlConvertOps.gridy = 0;
		getContentPane().add(pnlConvertOps, gbc_pnlConvertOps);
		GridBagLayout gbl_pnlConvertOps = new GridBagLayout();
		gbl_pnlConvertOps.columnWidths = new int[]{0, 0, 0};
		gbl_pnlConvertOps.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_pnlConvertOps.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlConvertOps.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlConvertOps.setLayout(gbl_pnlConvertOps);
		
		cbMaxVox = new JCheckBox("Autocap total voices:");
		GridBagConstraints gbc_cbMaxVox = new GridBagConstraints();
		gbc_cbMaxVox.anchor = GridBagConstraints.WEST;
		gbc_cbMaxVox.insets = new Insets(5, 5, 5, 5);
		gbc_cbMaxVox.gridx = 0;
		gbc_cbMaxVox.gridy = 0;
		pnlConvertOps.add(cbMaxVox, gbc_cbMaxVox);
		globalEnable.addComponent("cbMaxVox", cbMaxVox);
		cbMaxVox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cbMaxVoxCallback();
			}
		});
		
		txtMaxVox = new JTextField();
		txtMaxVox.setText("17");
		GridBagConstraints gbc_txtMaxVox = new GridBagConstraints();
		gbc_txtMaxVox.insets = new Insets(5, 0, 5, 5);
		gbc_txtMaxVox.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtMaxVox.gridx = 1;
		gbc_txtMaxVox.gridy = 0;
		pnlConvertOps.add(txtMaxVox, gbc_txtMaxVox);
		txtMaxVox.setColumns(10);
		globalEnable.addComponent("txtMaxVox", txtMaxVox);
		
		cbTempoCap = new JCheckBox("Halve tempo if exceeds cap");
		GridBagConstraints gbc_cbTempoCap = new GridBagConstraints();
		gbc_cbTempoCap.anchor = GridBagConstraints.WEST;
		gbc_cbTempoCap.gridwidth = 2;
		gbc_cbTempoCap.insets = new Insets(0, 5, 5, 0);
		gbc_cbTempoCap.gridx = 0;
		gbc_cbTempoCap.gridy = 1;
		pnlConvertOps.add(cbTempoCap, gbc_cbTempoCap);
		globalEnable.addComponent("cbTempoCap", cbTempoCap);
		cbTempoCap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cbTempoHalveCallback();
			}
		});
		
		cbTimesig = new JCheckBox("Set time signature:");
		GridBagConstraints gbc_cbTimesig = new GridBagConstraints();
		gbc_cbTimesig.anchor = GridBagConstraints.WEST;
		gbc_cbTimesig.insets = new Insets(0, 5, 5, 5);
		gbc_cbTimesig.gridx = 0;
		gbc_cbTimesig.gridy = 2;
		pnlConvertOps.add(cbTimesig, gbc_cbTimesig);
		globalEnable.addComponent("cbTimesig", cbTimesig);
		cbTimesig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cbTimeSigCallback();
			}
		});
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 2;
		gbc_panel.insets = new Insets(0, 5, 5, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 3;
		pnlConvertOps.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 30, 0, 30, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		txtTimeSigBeats = new JTextField();
		txtTimeSigBeats.setText("4");
		GridBagConstraints gbc_txtTimeSigBeats = new GridBagConstraints();
		gbc_txtTimeSigBeats.insets = new Insets(0, 0, 0, 5);
		gbc_txtTimeSigBeats.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtTimeSigBeats.gridx = 1;
		gbc_txtTimeSigBeats.gridy = 0;
		panel.add(txtTimeSigBeats, gbc_txtTimeSigBeats);
		txtTimeSigBeats.setColumns(10);
		timeSigGroup.addComponent("txtTimeSigBeats", txtTimeSigBeats);
		
		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.fill = GridBagConstraints.VERTICAL;
		gbc_separator.insets = new Insets(0, 0, 0, 5);
		gbc_separator.gridx = 2;
		gbc_separator.gridy = 0;
		panel.add(separator, gbc_separator);
		
		txtTimeSigDiv = new JTextField();
		txtTimeSigDiv.setText("4");
		GridBagConstraints gbc_txtTimeSigDiv = new GridBagConstraints();
		gbc_txtTimeSigDiv.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtTimeSigDiv.gridx = 3;
		gbc_txtTimeSigDiv.gridy = 0;
		panel.add(txtTimeSigDiv, gbc_txtTimeSigDiv);
		txtTimeSigDiv.setColumns(10);
		timeSigGroup.addComponent("txtTimeSigDiv", txtTimeSigDiv);
		
		cbLoop = new JCheckBox("Set loop point:");
		GridBagConstraints gbc_cbLoop = new GridBagConstraints();
		gbc_cbLoop.gridwidth = 2;
		gbc_cbLoop.anchor = GridBagConstraints.WEST;
		gbc_cbLoop.insets = new Insets(0, 5, 5, 0);
		gbc_cbLoop.gridx = 0;
		gbc_cbLoop.gridy = 4;
		pnlConvertOps.add(cbLoop, gbc_cbLoop);
		globalEnable.addComponent("cbLoop", cbLoop);
		cbLoop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cbLoopCallback();
			}
		});
		
		JLabel lblNewLabel_1 = new JLabel("Measure:");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 5, 5, 5);
		gbc_lblNewLabel_1.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_1.gridx = 0;
		gbc_lblNewLabel_1.gridy = 5;
		pnlConvertOps.add(lblNewLabel_1, gbc_lblNewLabel_1);
		
		txtLoopM = new JTextField();
		txtLoopM.setText("0");
		GridBagConstraints gbc_txtLoopM = new GridBagConstraints();
		gbc_txtLoopM.insets = new Insets(0, 0, 5, 5);
		gbc_txtLoopM.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtLoopM.gridx = 1;
		gbc_txtLoopM.gridy = 5;
		pnlConvertOps.add(txtLoopM, gbc_txtLoopM);
		txtLoopM.setColumns(10);
		customLoopGroup.addComponent("txtLoopM", txtLoopM);
		
		JLabel lblNewLabel_2 = new JLabel("Beat:");
		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
		gbc_lblNewLabel_2.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel_2.gridx = 0;
		gbc_lblNewLabel_2.gridy = 6;
		pnlConvertOps.add(lblNewLabel_2, gbc_lblNewLabel_2);
		
		txtLoopBt = new JTextField();
		txtLoopBt.setText("0");
		GridBagConstraints gbc_txtLoopBt = new GridBagConstraints();
		gbc_txtLoopBt.insets = new Insets(0, 0, 5, 5);
		gbc_txtLoopBt.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtLoopBt.gridx = 1;
		gbc_txtLoopBt.gridy = 6;
		pnlConvertOps.add(txtLoopBt, gbc_txtLoopBt);
		txtLoopBt.setColumns(10);
		customLoopGroup.addComponent("txtLoopBt", txtLoopBt);
		
		JPanel pnlFilepath = new JPanel();
		pnlFilepath.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlFilepath = new GridBagConstraints();
		gbc_pnlFilepath.gridwidth = 2;
		gbc_pnlFilepath.insets = new Insets(0, 5, 5, 5);
		gbc_pnlFilepath.fill = GridBagConstraints.BOTH;
		gbc_pnlFilepath.gridx = 0;
		gbc_pnlFilepath.gridy = 1;
		getContentPane().add(pnlFilepath, gbc_pnlFilepath);
		GridBagLayout gbl_pnlFilepath = new GridBagLayout();
		gbl_pnlFilepath.columnWidths = new int[]{0, 60, 0};
		gbl_pnlFilepath.rowHeights = new int[]{0, 0, 0};
		gbl_pnlFilepath.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlFilepath.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		pnlFilepath.setLayout(gbl_pnlFilepath);
		
		JLabel lblNewLabel = new JLabel("Import File:");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblNewLabel.insets = new Insets(5, 5, 0, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		pnlFilepath.add(lblNewLabel, gbc_lblNewLabel);
		
		txtFilePath = new JTextField();
		GridBagConstraints gbc_txtFilePath = new GridBagConstraints();
		gbc_txtFilePath.insets = new Insets(5, 5, 0, 5);
		gbc_txtFilePath.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtFilePath.gridx = 0;
		gbc_txtFilePath.gridy = 1;
		pnlFilepath.add(txtFilePath, gbc_txtFilePath);
		txtFilePath.setColumns(10);
		globalEnable.addComponent("txtFilePath", txtFilePath);
		
		JButton btnBrowse = new JButton("Browse...");
		GridBagConstraints gbc_btnBrowse = new GridBagConstraints();
		gbc_btnBrowse.insets = new Insets(5, 5, 5, 5);
		gbc_btnBrowse.fill = GridBagConstraints.BOTH;
		gbc_btnBrowse.gridx = 1;
		gbc_btnBrowse.gridy = 1;
		pnlFilepath.add(btnBrowse, gbc_btnBrowse);
		globalEnable.addComponent("btnBrowse", btnBrowse);
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnBrowseCallback();
			}
		});
		
		JPanel pnlButtons = new JPanel();
		pnlButtons.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlButtons = new GridBagConstraints();
		gbc_pnlButtons.gridwidth = 2;
		gbc_pnlButtons.fill = GridBagConstraints.BOTH;
		gbc_pnlButtons.gridx = 0;
		gbc_pnlButtons.gridy = 2;
		getContentPane().add(pnlButtons, gbc_pnlButtons);
		GridBagLayout gbl_pnlButtons = new GridBagLayout();
		gbl_pnlButtons.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlButtons.rowHeights = new int[]{0, 0};
		gbl_pnlButtons.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlButtons.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlButtons.setLayout(gbl_pnlButtons);
		
		JButton btnImport = new JButton("Import");
		GridBagConstraints gbc_btnImport = new GridBagConstraints();
		gbc_btnImport.insets = new Insets(5, 0, 5, 5);
		gbc_btnImport.gridx = 1;
		gbc_btnImport.gridy = 0;
		pnlButtons.add(btnImport, gbc_btnImport);
		fileEnabled.addComponent("btnImport", btnImport);
		btnImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnImportCallback();
			}
		});
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(5, 0, 5, 5);
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 0;
		pnlButtons.add(btnCancel, gbc_btnCancel);
		globalEnable.addComponent("btnCancel", btnCancel);
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnCancelCallback();
			}
		});
	}
	
	/*----- Getters -----*/
	
	public boolean getExitSelection() {return exitSelection;}
	
	public boolean getOptions(SeqImportOptions op) {
		if(op == null) return false;
		//op.limitNoteSpeed = cbTimeMin.isSelected();
		//op.limitVoxPerChannel = cbVoxLimit.isSelected();
		op.tags = tags;
		op.seqType = cmbxCategory.getRawValue();
		op.name = txtName.getText();
		op.eString = txtEnum.getText();
		op.cacheType = cmbxCache.getRawValue();
		op.bankUid = fontUid;
		
		try {
			if(cbMaxVox.isSelected()) {
				op.maxTotalVox = Integer.parseInt(txtMaxVox.getText());
			}
			else op.maxTotalVox = 0;
			
			op.halveForTempoCap = cbTempoCap.isSelected();
			
			if(cbTimesig.isSelected()) {
				op.custom_timesig = true;
				op.timesig_beats = Integer.parseInt(txtTimeSigBeats.getText());
				op.timesig_div = Integer.parseInt(txtTimeSigDiv.getText());
			}
			else op.custom_timesig = false;
			
			if(cbLoop.isSelected()) {
				op.loopMeasure = Integer.parseInt(txtLoopM.getText());
				op.loopBeat = Double.parseDouble(txtLoopBt.getText());
				op.loopTick = 0;
			}
			else {
				op.loopMeasure = -1;
				op.loopBeat = -1.0;
				op.loopTick = -1;
			}
			
		}
		catch(NumberFormatException ex) {
			ex.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public String getPath() {return txtFilePath.getText();}
	
	public String getLastImportPath(){return lastImportPath;}
	
	/*----- Setters -----*/
	
	public void setLastImportPath(String val) {lastImportPath = val;}
	
	/*----- GUI -----*/
	
	public boolean checkAllFieldsValid() {
		String txt = txtFilePath.getText();
		if(txt == null || txt.isEmpty()) {
			showError("File path is required for import!");
			return false;
		}
		
		if(cbMaxVox.isSelected()) {
			txt = txtMaxVox.getText();
			if(txt == null || txt.isEmpty()) {
				showError("Please enter a positive integer for voice cap.");
				return false;
			}
			try {
				int n = Integer.parseInt(txt);
				if(n < 0) {
					showError("Please enter a positive integer for voice cap.");
					return false;
				}
			}
			catch(NumberFormatException ex) {
				showError("Please enter a positive integer for voice cap.");
				return false;
			}
		}
		
		if(cbTimesig.isSelected()) {
			txt = txtTimeSigBeats.getText();
			if(txt == null || txt.isEmpty()) {
				showError("Please enter a positive integer for time signature beats per measure.");
				return false;
			}
			try {
				int n = Integer.parseInt(txt);
				if(n < 1) {
					showError("Please enter a positive integer for time signature beats per measure.");
					return false;
				}
			}
			catch(NumberFormatException ex) {
				showError("Please enter a positive integer for time signature beats per measure.");
				return false;
			}
			
			txt = txtTimeSigDiv.getText();
			if(txt == null || txt.isEmpty()) {
				showError("Please enter a positive integer for time signature beat value.");
				return false;
			}
			try {
				int n = Integer.parseInt(txt);
				if(n < 1) {
					showError("Please enter a positive integer for time signature beat value.");
					return false;
				}
			}
			catch(NumberFormatException ex) {
				showError("Please enter a positive integer for time signature beat value.");
				return false;
			}
		}
		
		if(cbLoop.isSelected()) {
			txt = txtLoopM.getText();
			if(txt == null || txt.isEmpty()) {
				showError("Please enter a positive integer for custom loop measure.");
				return false;
			}
			try {
				int n = Integer.parseInt(txt);
				if(n < 0) {
					showError("Please enter a positive integer for custom loop measure.");
					return false;
				}
			}
			catch(NumberFormatException ex) {
				showError("Please enter a positive integer for custom loop measure.");
				return false;
			}
			
			txt = txtLoopBt.getText();
			if(txt == null || txt.isEmpty()) {
				showError("Please enter a positive rational number for custom loop beat.");
				return false;
			}
			try {
				double n = Double.parseDouble(txt);
				if(n < 0.0) {
					showError("Please enter a positive rational number for custom loop beat.");
					return false;
				}
			}
			catch(NumberFormatException ex) {
				showError("Please enter a positive rational number for custom loop beat.");
				return false;
			}
		}
		
		return true;
	}
	
	public void reenable() {
		super.reenable();
		fileEnabled.setEnabling(!txtFilePath.getText().isEmpty());
		customLoopGroup.setEnabling(cbLoop.isSelected());
		timeSigGroup.setEnabling(cbTimesig.isSelected());
		txtMaxVox.setEnabled(cbMaxVox.isSelected());
		globalEnable.repaint();
	}
	
	/*----- Callbacks -----*/
	
	public void cbMaxVoxCallback() {reenable();}
	public void cbTempoHalveCallback() {reenable();}
	public void cbTimeSigCallback() {reenable();}
	public void cbLoopCallback() {reenable();}
	
	public void btnSetSoundfontCallback() {
		//Needs a core link.
		
		if(core == null) {
			showError("Need Zeqer Core link to retrieve Soundfont options!");
			return;
		}
		
		setWait();
		BankPickDialog dialog = new BankPickDialog(parent, core);
		dialog.showMe(this);
		
		if(dialog.getExitSelection()) {
			ZeqerBank bnk = dialog.getSelectedBank();
			if(bnk != null) {
				fontUid = bnk.getTableEntry().getUID();
				lblFontName.setText(String.format("[%08x] %s", fontUid, bnk.getTableEntry().getName()));
			}
			else {
				fontUid = 0;
				lblFontName.setText("<none>");
			}
		}
		
		unsetWait();
	}
	
	public void btnBrowseCallback() {
		JFileChooser fc = new JFileChooser(lastImportPath);
		ZeqerSeqIO.addImportableFileFilters(fc);
		int res = fc.showOpenDialog(this);
		
		if(res != JFileChooser.APPROVE_OPTION) return;
		lastImportPath = fc.getSelectedFile().getAbsolutePath();
		txtFilePath.setText(lastImportPath);
		reenable();
	}
	
	public void btnImportCallback() {
		if(!checkAllFieldsValid()) return;
		exitSelection = true;
		closeMe();
	}
	
	public void btnCancelCallback() {
		exitSelection = false;
		closeMe();
	}
	
	public void btnTagsCallback() {
		setWait();
		ZeqerTagEditDialog dialog = new ZeqerTagEditDialog(parent);
		dialog.loadTags(tags);
		dialog.showMe(this);
		
		if(dialog.getExitSelection()) {
			tags.clear();
			tags.addAll(dialog.getTags());
		}
		unsetWait();
	}
	
}
