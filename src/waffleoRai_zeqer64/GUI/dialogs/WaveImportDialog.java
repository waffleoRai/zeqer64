package waffleoRai_zeqer64.GUI.dialogs;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.RadioButtonGroup;
import waffleoRai_GUITools.WRDialog;
import waffleoRai_zeqer64.GUI.MediumTypeCombobox;
import waffleoRai_zeqer64.filefmt.ZeqerWaveIO;
import waffleoRai_zeqer64.filefmt.ZeqerWaveIO.SampleImportOptions;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.border.BevelBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Font;
import javax.swing.JCheckBox;
import javax.swing.border.EtchedBorder;
import javax.swing.JRadioButton;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Dimension;

public class WaveImportDialog extends WRDialog{

	private static final long serialVersionUID = 6167906778954572163L;
	
	public static final int DEFO_WIDTH = 640;
	public static final int DEFO_HEIGHT = 480;
	
	public static final String OPKEY_LAST_SMPL_IMPORT = "LAST_SMPL_IMPORT_PATH";
	
	private static final int RBIDX_ADP9 = 0;
	private static final int RBIDX_ADP5 = 1;
	private static final int RBCOUNT_CODECS = 2;
	
	private static final int RBIDX_32K = 0;
	private static final int RBIDX_22K = 1;
	private static final int RBCOUNT_SAMPLERATES = 2;
	
	private static final int RBIDX_MULTICH_LEFT = 0;
	private static final int RBIDX_MULTICH_RIGHT = 1;
	private static final int RBIDX_MULTICH_AVG = 2;
	private static final int RBIDX_MULTICH_PICK = 3;
	private static final int RBCOUNT_MULTICH = 4;
	
	public static final int ERROR_NONE = 0;
	public static final int ERROR_NO_FILES = 1;
	public static final int ERROR_BAD_CUSTOM_CH = 2;
	public static final int ERROR_BAD_LOOP_TEXT = 3;
	
	/*----- Instance Variable -----*/
	
	private JFrame parent;
	private ZeqerCoreInterface core;
	private boolean exitSelection = false;
	
	private ComponentGroup cgLoopOps;
	private ComponentGroup cgItemSelected;
	private ComponentGroup cgListNotEmpty;
	
	private JTextField txtName;
	private MediumTypeCombobox cmbxMedium;
	private JCheckBox cbFlagMusic;
	private JCheckBox cbFlagSFX;
	private JCheckBox cbFlagActor;
	private JCheckBox cbFlagEnv;
	private JCheckBox cbFlagVox;
	
	private RadioButtonGroup rbgCodec;
	private RadioButtonGroup rbgSampleRate;
	private RadioButtonGroup rbgMultiChan;
	private JCheckBox cbDefoTbl;
	private JTextField txtChannel;
	
	private JCheckBox cbManualLoop;
	private JTextField txtLoopCt;
	private JTextField txtLoopSt;
	private JTextField txtLoopEd;

	private List<String> pathList;
	private Set<String> tags;
	private JList<String> lstFiles;
	
	/*----- Init -----*/
	
	public WaveImportDialog(JFrame parent, ZeqerCoreInterface core_iface){
		super(parent, true);
		this.parent = parent;
		core = core_iface;
		
		cgLoopOps = globalEnable.newChild();
		cgListNotEmpty = globalEnable.newChild();
		cgItemSelected = cgListNotEmpty.newChild();
		rbgCodec = new RadioButtonGroup(RBCOUNT_CODECS);
		rbgSampleRate = new RadioButtonGroup(RBCOUNT_SAMPLERATES);
		rbgMultiChan = new RadioButtonGroup(RBCOUNT_MULTICH);
		
		pathList = new LinkedList<String>();
		tags = new HashSet<String>();

		initGUI();
		reenable();
	}
	
	private void initGUI(){
		setMinimumSize(new Dimension(DEFO_WIDTH, DEFO_HEIGHT));
		setPreferredSize(new Dimension(DEFO_WIDTH, DEFO_HEIGHT));
		
		setTitle("Import Sound Sample");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 300, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel pnlOptions = new JPanel();
		GridBagConstraints gbc_pnlOptions = new GridBagConstraints();
		gbc_pnlOptions.insets = new Insets(5, 5, 5, 5);
		gbc_pnlOptions.fill = GridBagConstraints.BOTH;
		gbc_pnlOptions.gridx = 0;
		gbc_pnlOptions.gridy = 0;
		getContentPane().add(pnlOptions, gbc_pnlOptions);
		GridBagLayout gbl_pnlOptions = new GridBagLayout();
		gbl_pnlOptions.columnWidths = new int[]{0, 0};
		gbl_pnlOptions.rowHeights = new int[]{0, 0, 0, 0};
		gbl_pnlOptions.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_pnlOptions.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		pnlOptions.setLayout(gbl_pnlOptions);
		
		JPanel pnlMetadata = new JPanel();
		pnlMetadata.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlMetadata = new GridBagConstraints();
		gbc_pnlMetadata.insets = new Insets(0, 0, 5, 0);
		gbc_pnlMetadata.fill = GridBagConstraints.BOTH;
		gbc_pnlMetadata.gridx = 0;
		gbc_pnlMetadata.gridy = 0;
		pnlOptions.add(pnlMetadata, gbc_pnlMetadata);
		GridBagLayout gbl_pnlMetadata = new GridBagLayout();
		gbl_pnlMetadata.columnWidths = new int[]{0, 0, 0};
		gbl_pnlMetadata.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_pnlMetadata.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlMetadata.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlMetadata.setLayout(gbl_pnlMetadata);
		
		JLabel lblMetadata = new JLabel("Metadata");
		lblMetadata.setFont(new Font("Tahoma", Font.BOLD, 13));
		GridBagConstraints gbc_lblMetadata = new GridBagConstraints();
		gbc_lblMetadata.anchor = GridBagConstraints.WEST;
		gbc_lblMetadata.insets = new Insets(5, 5, 5, 5);
		gbc_lblMetadata.gridx = 0;
		gbc_lblMetadata.gridy = 0;
		pnlMetadata.add(lblMetadata, gbc_lblMetadata);
		
		JLabel lblNamenameStem = new JLabel("Name/Name Stem:");
		GridBagConstraints gbc_lblNamenameStem = new GridBagConstraints();
		gbc_lblNamenameStem.anchor = GridBagConstraints.EAST;
		gbc_lblNamenameStem.insets = new Insets(0, 5, 5, 5);
		gbc_lblNamenameStem.gridx = 0;
		gbc_lblNamenameStem.gridy = 1;
		pnlMetadata.add(lblNamenameStem, gbc_lblNamenameStem);
		
		txtName = new JTextField();
		GridBagConstraints gbc_txtName = new GridBagConstraints();
		gbc_txtName.insets = new Insets(0, 0, 5, 5);
		gbc_txtName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtName.gridx = 1;
		gbc_txtName.gridy = 1;
		pnlMetadata.add(txtName, gbc_txtName);
		txtName.setColumns(10);
		globalEnable.addComponent("txtName", txtName);
		
		JLabel lblMedium = new JLabel("Medium:");
		GridBagConstraints gbc_lblMedium = new GridBagConstraints();
		gbc_lblMedium.anchor = GridBagConstraints.EAST;
		gbc_lblMedium.insets = new Insets(0, 5, 5, 5);
		gbc_lblMedium.gridx = 0;
		gbc_lblMedium.gridy = 2;
		pnlMetadata.add(lblMedium, gbc_lblMedium);
		
		cmbxMedium = new MediumTypeCombobox();
		GridBagConstraints gbc_cmbxMedium = new GridBagConstraints();
		gbc_cmbxMedium.insets = new Insets(0, 0, 5, 5);
		gbc_cmbxMedium.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxMedium.gridx = 1;
		gbc_cmbxMedium.gridy = 2;
		pnlMetadata.add(cmbxMedium, gbc_cmbxMedium);
		globalEnable.addComponent("cmbxMedium", cmbxMedium);
		
		JPanel pnlMetaFlags = new JPanel();
		pnlMetaFlags.setBorder(new LineBorder(new Color(0, 0, 0)));
		GridBagConstraints gbc_pnlMetaFlags = new GridBagConstraints();
		gbc_pnlMetaFlags.gridwidth = 2;
		gbc_pnlMetaFlags.insets = new Insets(0, 5, 5, 5);
		gbc_pnlMetaFlags.fill = GridBagConstraints.BOTH;
		gbc_pnlMetaFlags.gridx = 0;
		gbc_pnlMetaFlags.gridy = 3;
		pnlMetadata.add(pnlMetaFlags, gbc_pnlMetaFlags);
		GridBagLayout gbl_pnlMetaFlags = new GridBagLayout();
		gbl_pnlMetaFlags.columnWidths = new int[]{0, 0};
		gbl_pnlMetaFlags.rowHeights = new int[]{0, 0, 0};
		gbl_pnlMetaFlags.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlMetaFlags.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		pnlMetaFlags.setLayout(gbl_pnlMetaFlags);
		
		JLabel lblFlags = new JLabel("Flags:");
		lblFlags.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblFlags = new GridBagConstraints();
		gbc_lblFlags.insets = new Insets(5, 5, 5, 5);
		gbc_lblFlags.gridx = 0;
		gbc_lblFlags.gridy = 0;
		pnlMetaFlags.add(lblFlags, gbc_lblFlags);
		
		JPanel pnlFlagBoxes = new JPanel();
		GridBagConstraints gbc_pnlFlagBoxes = new GridBagConstraints();
		gbc_pnlFlagBoxes.fill = GridBagConstraints.BOTH;
		gbc_pnlFlagBoxes.gridx = 0;
		gbc_pnlFlagBoxes.gridy = 1;
		pnlMetaFlags.add(pnlFlagBoxes, gbc_pnlFlagBoxes);
		
		cbFlagMusic = new JCheckBox("Music");
		pnlFlagBoxes.add(cbFlagMusic);
		globalEnable.addComponent("cbFlagMusic", cbFlagMusic);
		
		cbFlagSFX = new JCheckBox("SFX");
		pnlFlagBoxes.add(cbFlagSFX);
		globalEnable.addComponent("cbFlagSFX", cbFlagSFX);
		
		cbFlagActor = new JCheckBox("Actor");
		pnlFlagBoxes.add(cbFlagActor);
		globalEnable.addComponent("cbFlagActor", cbFlagActor);
		
		cbFlagEnv = new JCheckBox("Environment");
		pnlFlagBoxes.add(cbFlagEnv);
		globalEnable.addComponent("cbFlagEnv", cbFlagEnv);
		
		cbFlagVox = new JCheckBox("Voice");
		pnlFlagBoxes.add(cbFlagVox);
		globalEnable.addComponent("cbFlagVox", cbFlagVox);
		
		JButton btnTags = new JButton("Edit Tags...");
		GridBagConstraints gbc_btnTags = new GridBagConstraints();
		gbc_btnTags.insets = new Insets(0, 0, 5, 5);
		gbc_btnTags.anchor = GridBagConstraints.EAST;
		gbc_btnTags.gridx = 1;
		gbc_btnTags.gridy = 4;
		pnlMetadata.add(btnTags, gbc_btnTags);
		globalEnable.addComponent("btnTags", btnTags);
		btnTags.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnTagsCallback();
			}});
		
		JPanel pnlCompression = new JPanel();
		pnlCompression.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlCompression = new GridBagConstraints();
		gbc_pnlCompression.insets = new Insets(0, 0, 5, 0);
		gbc_pnlCompression.fill = GridBagConstraints.BOTH;
		gbc_pnlCompression.gridx = 0;
		gbc_pnlCompression.gridy = 1;
		pnlOptions.add(pnlCompression, gbc_pnlCompression);
		GridBagLayout gbl_pnlCompression = new GridBagLayout();
		gbl_pnlCompression.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_pnlCompression.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_pnlCompression.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlCompression.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		pnlCompression.setLayout(gbl_pnlCompression);
		
		JLabel lblCompressionOptions = new JLabel("Compression Options");
		lblCompressionOptions.setFont(new Font("Tahoma", Font.BOLD, 13));
		GridBagConstraints gbc_lblCompressionOptions = new GridBagConstraints();
		gbc_lblCompressionOptions.gridwidth = 2;
		gbc_lblCompressionOptions.anchor = GridBagConstraints.WEST;
		gbc_lblCompressionOptions.insets = new Insets(5, 5, 5, 5);
		gbc_lblCompressionOptions.gridx = 0;
		gbc_lblCompressionOptions.gridy = 0;
		pnlCompression.add(lblCompressionOptions, gbc_lblCompressionOptions);
		
		JLabel lblCodec = new JLabel("Codec:");
		GridBagConstraints gbc_lblCodec = new GridBagConstraints();
		gbc_lblCodec.anchor = GridBagConstraints.EAST;
		gbc_lblCodec.insets = new Insets(0, 5, 5, 5);
		gbc_lblCodec.gridx = 0;
		gbc_lblCodec.gridy = 1;
		pnlCompression.add(lblCodec, gbc_lblCodec);
		
		JRadioButton rbADP9 = new JRadioButton("ADP9 (4-bit)");
		rbADP9.setSelected(true);
		GridBagConstraints gbc_rbADP9 = new GridBagConstraints();
		gbc_rbADP9.anchor = GridBagConstraints.WEST;
		gbc_rbADP9.insets = new Insets(0, 0, 5, 5);
		gbc_rbADP9.gridx = 1;
		gbc_rbADP9.gridy = 1;
		pnlCompression.add(rbADP9, gbc_rbADP9);
		globalEnable.addComponent("rbADP9", rbADP9);
		rbgCodec.addButton(rbADP9, RBIDX_ADP9);
		rbADP9.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				rbgCodec.select(RBIDX_ADP9);
			}});
		
		JRadioButton rbADP5 = new JRadioButton("ADP5 (2-bit)");
		rbADP5.setSelected(false);
		GridBagConstraints gbc_rbADP5 = new GridBagConstraints();
		gbc_rbADP5.anchor = GridBagConstraints.WEST;
		gbc_rbADP5.insets = new Insets(0, 0, 5, 5);
		gbc_rbADP5.gridx = 2;
		gbc_rbADP5.gridy = 1;
		pnlCompression.add(rbADP5, gbc_rbADP5);
		globalEnable.addComponent("rbADP5", rbADP5);
		rbgCodec.addButton(rbADP5, RBIDX_ADP5);
		rbADP5.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				rbgCodec.select(RBIDX_ADP5);
			}});
		
		cbDefoTbl = new JCheckBox("Use Default ADPCM Table");
		GridBagConstraints gbc_cbDefoTbl = new GridBagConstraints();
		gbc_cbDefoTbl.anchor = GridBagConstraints.WEST;
		gbc_cbDefoTbl.gridwidth = 2;
		gbc_cbDefoTbl.insets = new Insets(0, 0, 5, 5);
		gbc_cbDefoTbl.gridx = 1;
		gbc_cbDefoTbl.gridy = 2;
		pnlCompression.add(cbDefoTbl, gbc_cbDefoTbl);
		globalEnable.addComponent("cbDefoTbl", cbDefoTbl);
		cbDefoTbl.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				rbgCodec.select(RBIDX_ADP9);
				reenable();
			}});
		
		JLabel lblSampleRateCap = new JLabel("Sample Rate Cap:");
		GridBagConstraints gbc_lblSampleRateCap = new GridBagConstraints();
		gbc_lblSampleRateCap.anchor = GridBagConstraints.EAST;
		gbc_lblSampleRateCap.insets = new Insets(0, 5, 5, 5);
		gbc_lblSampleRateCap.gridx = 0;
		gbc_lblSampleRateCap.gridy = 3;
		pnlCompression.add(lblSampleRateCap, gbc_lblSampleRateCap);
		
		JRadioButton rb32k = new JRadioButton("32000 Hz");
		GridBagConstraints gbc_rb32k = new GridBagConstraints();
		gbc_rb32k.anchor = GridBagConstraints.WEST;
		gbc_rb32k.insets = new Insets(0, 0, 5, 5);
		gbc_rb32k.gridx = 1;
		gbc_rb32k.gridy = 3;
		pnlCompression.add(rb32k, gbc_rb32k);
		globalEnable.addComponent("rb32k", rb32k);
		rbgSampleRate.addButton(rb32k, RBIDX_32K);
		rb32k.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				rbgSampleRate.select(RBIDX_32K);
			}});
		
		JRadioButton rb22k = new JRadioButton("22050 Hz");
		GridBagConstraints gbc_rb22k = new GridBagConstraints();
		gbc_rb22k.anchor = GridBagConstraints.WEST;
		gbc_rb22k.insets = new Insets(0, 0, 5, 5);
		gbc_rb22k.gridx = 2;
		gbc_rb22k.gridy = 3;
		pnlCompression.add(rb22k, gbc_rb22k);
		globalEnable.addComponent("rb22k", rb22k);
		rbgSampleRate.addButton(rb22k, RBIDX_22K);
		rb22k.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				rbgSampleRate.select(RBIDX_22K);
			}});
		
		
		JPanel pnlMultichan = new JPanel();
		pnlMultichan.setBorder(new LineBorder(new Color(0, 0, 0)));
		GridBagConstraints gbc_pnlMultichan = new GridBagConstraints();
		gbc_pnlMultichan.insets = new Insets(0, 5, 5, 5);
		gbc_pnlMultichan.gridwidth = 4;
		gbc_pnlMultichan.fill = GridBagConstraints.BOTH;
		gbc_pnlMultichan.gridx = 0;
		gbc_pnlMultichan.gridy = 4;
		pnlCompression.add(pnlMultichan, gbc_pnlMultichan);
		
		JLabel lblMultichannelCollapse = new JLabel("Multichannel Collapse:");
		lblMultichannelCollapse.setFont(new Font("Tahoma", Font.BOLD, 11));
		pnlMultichan.add(lblMultichannelCollapse);
		
		JRadioButton rbMCLeft = new JRadioButton("Left Only");
		pnlMultichan.add(rbMCLeft);
		globalEnable.addComponent("rbMCLeft", rbMCLeft);
		rbgMultiChan.addButton(rbMCLeft, RBIDX_MULTICH_LEFT);
		rbMCLeft.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				rbgMultiChan.select(RBIDX_MULTICH_LEFT);
				reenable();
			}});
		
		JRadioButton rbMCRight = new JRadioButton("Right Only");
		pnlMultichan.add(rbMCRight);
		globalEnable.addComponent("rbMCRight", rbMCRight);
		rbgMultiChan.addButton(rbMCRight, RBIDX_MULTICH_RIGHT);
		rbMCRight.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				rbgMultiChan.select(RBIDX_MULTICH_RIGHT);
				reenable();
			}});
		
		JRadioButton rbMCAvg = new JRadioButton("Average Channels");
		pnlMultichan.add(rbMCAvg);
		globalEnable.addComponent("rbMCAvg", rbMCAvg);
		rbgMultiChan.addButton(rbMCAvg, RBIDX_MULTICH_AVG);
		rbMCAvg.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				rbgMultiChan.select(RBIDX_MULTICH_AVG);
				reenable();
			}});
		
		JPanel pnlUseChan = new JPanel();
		pnlMultichan.add(pnlUseChan);
		GridBagLayout gbl_pnlUseChan = new GridBagLayout();
		gbl_pnlUseChan.columnWidths = new int[]{0, 0, 0};
		gbl_pnlUseChan.rowHeights = new int[]{0, 0};
		gbl_pnlUseChan.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlUseChan.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlUseChan.setLayout(gbl_pnlUseChan);
		
		JRadioButton rbMCCh = new JRadioButton("Use Channel:");
		GridBagConstraints gbc_rbMCCh = new GridBagConstraints();
		gbc_rbMCCh.insets = new Insets(0, 0, 0, 5);
		gbc_rbMCCh.gridx = 0;
		gbc_rbMCCh.gridy = 0;
		pnlUseChan.add(rbMCCh, gbc_rbMCCh);
		globalEnable.addComponent("rbMCCh", rbMCCh);
		rbgMultiChan.addButton(rbMCCh, RBIDX_MULTICH_PICK);
		rbMCCh.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				rbgMultiChan.select(RBIDX_MULTICH_PICK);
				reenable();
			}});
		
		txtChannel = new JTextField();
		txtChannel.setText("0");
		GridBagConstraints gbc_txtChannel = new GridBagConstraints();
		gbc_txtChannel.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtChannel.gridx = 1;
		gbc_txtChannel.gridy = 0;
		pnlUseChan.add(txtChannel, gbc_txtChannel);
		txtChannel.setColumns(10);
		globalEnable.addComponent("txtChannel", txtChannel);
		
		JPanel pnlMisc = new JPanel();
		pnlMisc.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlMisc = new GridBagConstraints();
		gbc_pnlMisc.fill = GridBagConstraints.BOTH;
		gbc_pnlMisc.gridx = 0;
		gbc_pnlMisc.gridy = 2;
		pnlOptions.add(pnlMisc, gbc_pnlMisc);
		GridBagLayout gbl_pnlMisc = new GridBagLayout();
		gbl_pnlMisc.columnWidths = new int[]{0, 0};
		gbl_pnlMisc.rowHeights = new int[]{0, 0, 0, 0};
		gbl_pnlMisc.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlMisc.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		pnlMisc.setLayout(gbl_pnlMisc);
		
		JLabel lblMisc = new JLabel("Miscellaneous");
		lblMisc.setFont(new Font("Tahoma", Font.BOLD, 13));
		GridBagConstraints gbc_lblMisc = new GridBagConstraints();
		gbc_lblMisc.anchor = GridBagConstraints.WEST;
		gbc_lblMisc.insets = new Insets(5, 5, 5, 0);
		gbc_lblMisc.gridx = 0;
		gbc_lblMisc.gridy = 0;
		pnlMisc.add(lblMisc, gbc_lblMisc);
		
		cbManualLoop = new JCheckBox("Manually specify loop");
		GridBagConstraints gbc_cbManualLoop = new GridBagConstraints();
		gbc_cbManualLoop.anchor = GridBagConstraints.WEST;
		gbc_cbManualLoop.insets = new Insets(0, 5, 5, 0);
		gbc_cbManualLoop.gridx = 0;
		gbc_cbManualLoop.gridy = 1;
		pnlMisc.add(cbManualLoop, gbc_cbManualLoop);
		globalEnable.addComponent("cbManualLoop", cbManualLoop);
		cbManualLoop.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				reenable();
			}});
		
		JPanel pnlLoop = new JPanel();
		GridBagConstraints gbc_pnlLoop = new GridBagConstraints();
		gbc_pnlLoop.insets = new Insets(0, 5, 5, 5);
		gbc_pnlLoop.fill = GridBagConstraints.BOTH;
		gbc_pnlLoop.gridx = 0;
		gbc_pnlLoop.gridy = 2;
		pnlMisc.add(pnlLoop, gbc_pnlLoop);
		GridBagLayout gbl_pnlLoop = new GridBagLayout();
		gbl_pnlLoop.columnWidths = new int[]{0, 50, 0, 0};
		gbl_pnlLoop.rowHeights = new int[]{0, 0, 0, 0};
		gbl_pnlLoop.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlLoop.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlLoop.setLayout(gbl_pnlLoop);
		
		JLabel lblNewLabel = new JLabel("Loop Start:");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		pnlLoop.add(lblNewLabel, gbc_lblNewLabel);
		
		txtLoopSt = new JTextField();
		txtLoopSt.setText("0");
		GridBagConstraints gbc_txtLoopSt = new GridBagConstraints();
		gbc_txtLoopSt.anchor = GridBagConstraints.WEST;
		gbc_txtLoopSt.insets = new Insets(0, 0, 5, 5);
		gbc_txtLoopSt.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtLoopSt.gridx = 1;
		gbc_txtLoopSt.gridy = 0;
		pnlLoop.add(txtLoopSt, gbc_txtLoopSt);
		txtLoopSt.setColumns(10);
		cgLoopOps.addComponent("txtLoopSt", txtLoopSt);
		
		JLabel lblLoopEnd = new JLabel("Loop End:");
		GridBagConstraints gbc_lblLoopEnd = new GridBagConstraints();
		gbc_lblLoopEnd.anchor = GridBagConstraints.EAST;
		gbc_lblLoopEnd.insets = new Insets(0, 0, 5, 5);
		gbc_lblLoopEnd.gridx = 0;
		gbc_lblLoopEnd.gridy = 1;
		pnlLoop.add(lblLoopEnd, gbc_lblLoopEnd);
		
		txtLoopEd = new JTextField();
		txtLoopEd.setText("1");
		GridBagConstraints gbc_txtLoopEd = new GridBagConstraints();
		gbc_txtLoopEd.anchor = GridBagConstraints.WEST;
		gbc_txtLoopEd.insets = new Insets(0, 0, 5, 5);
		gbc_txtLoopEd.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtLoopEd.gridx = 1;
		gbc_txtLoopEd.gridy = 1;
		pnlLoop.add(txtLoopEd, gbc_txtLoopEd);
		txtLoopEd.setColumns(10);
		cgLoopOps.addComponent("txtLoopEd", txtLoopEd);
		
		JLabel lblLoopCount = new JLabel("Loop Count:");
		GridBagConstraints gbc_lblLoopCount = new GridBagConstraints();
		gbc_lblLoopCount.anchor = GridBagConstraints.EAST;
		gbc_lblLoopCount.insets = new Insets(0, 0, 0, 5);
		gbc_lblLoopCount.gridx = 0;
		gbc_lblLoopCount.gridy = 2;
		pnlLoop.add(lblLoopCount, gbc_lblLoopCount);
		
		txtLoopCt = new JTextField();
		txtLoopCt.setText("0");
		GridBagConstraints gbc_txtLoopCt = new GridBagConstraints();
		gbc_txtLoopCt.insets = new Insets(0, 0, 0, 5);
		gbc_txtLoopCt.anchor = GridBagConstraints.WEST;
		gbc_txtLoopCt.gridx = 1;
		gbc_txtLoopCt.gridy = 2;
		pnlLoop.add(txtLoopCt, gbc_txtLoopCt);
		txtLoopCt.setColumns(10);
		cgLoopOps.addComponent("txtLoopCt", txtLoopCt);
		
		JPanel pnlFiles = new JPanel();
		pnlFiles.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlFiles = new GridBagConstraints();
		gbc_pnlFiles.weightx = 2.0;
		gbc_pnlFiles.insets = new Insets(5, 0, 5, 5);
		gbc_pnlFiles.fill = GridBagConstraints.BOTH;
		gbc_pnlFiles.gridx = 1;
		gbc_pnlFiles.gridy = 0;
		getContentPane().add(pnlFiles, gbc_pnlFiles);
		GridBagLayout gbl_pnlFiles = new GridBagLayout();
		gbl_pnlFiles.columnWidths = new int[]{200, 0};
		gbl_pnlFiles.rowHeights = new int[]{0, 0, 0};
		gbl_pnlFiles.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlFiles.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		pnlFiles.setLayout(gbl_pnlFiles);
		
		JScrollPane spFiles = new JScrollPane();
		spFiles.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spFiles = new GridBagConstraints();
		gbc_spFiles.insets = new Insets(5, 5, 5, 5);
		gbc_spFiles.fill = GridBagConstraints.BOTH;
		gbc_spFiles.gridx = 0;
		gbc_spFiles.gridy = 0;
		pnlFiles.add(spFiles, gbc_spFiles);
		
		lstFiles = new JList<String>();
		spFiles.setViewportView(lstFiles);
		
		globalEnable.addComponent("lstFiles", lstFiles);
		globalEnable.addComponent("spFiles", spFiles);
		
		JPanel pnlBtnFiles = new JPanel();
		GridBagConstraints gbc_pnlBtnFiles = new GridBagConstraints();
		gbc_pnlBtnFiles.insets = new Insets(0, 5, 5, 5);
		gbc_pnlBtnFiles.fill = GridBagConstraints.BOTH;
		gbc_pnlBtnFiles.gridx = 0;
		gbc_pnlBtnFiles.gridy = 1;
		pnlFiles.add(pnlBtnFiles, gbc_pnlBtnFiles);
		GridBagLayout gbl_pnlBtnFiles = new GridBagLayout();
		gbl_pnlBtnFiles.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlBtnFiles.rowHeights = new int[]{0, 0};
		gbl_pnlBtnFiles.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlBtnFiles.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlBtnFiles.setLayout(gbl_pnlBtnFiles);
		
		JButton btnDelete = new JButton("Delete");
		GridBagConstraints gbc_btnDelete = new GridBagConstraints();
		gbc_btnDelete.insets = new Insets(0, 0, 0, 5);
		gbc_btnDelete.gridx = 0;
		gbc_btnDelete.gridy = 0;
		pnlBtnFiles.add(btnDelete, gbc_btnDelete);
		cgItemSelected.addComponent("btnDelete", btnDelete);
		btnDelete.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnDeleteCallback();
			}});
		
		JButton btnAdd = new JButton("Add...");
		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.gridx = 2;
		gbc_btnAdd.gridy = 0;
		pnlBtnFiles.add(btnAdd, gbc_btnAdd);
		globalEnable.addComponent("btnAdd", btnAdd);
		btnAdd.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnAddCallback();
			}});
		
		JPanel pnlBtn = new JPanel();
		GridBagConstraints gbc_pnlBtn = new GridBagConstraints();
		gbc_pnlBtn.gridwidth = 2;
		gbc_pnlBtn.insets = new Insets(0, 5, 5, 5);
		gbc_pnlBtn.fill = GridBagConstraints.BOTH;
		gbc_pnlBtn.gridx = 0;
		gbc_pnlBtn.gridy = 1;
		getContentPane().add(pnlBtn, gbc_pnlBtn);
		GridBagLayout gbl_pnlBtn = new GridBagLayout();
		gbl_pnlBtn.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlBtn.rowHeights = new int[]{0, 0};
		gbl_pnlBtn.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlBtn.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlBtn.setLayout(gbl_pnlBtn);
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
		gbc_btnCancel.gridx = 1;
		gbc_btnCancel.gridy = 0;
		pnlBtn.add(btnCancel, gbc_btnCancel);
		globalEnable.addComponent("btnCancel", btnCancel);
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnCancelCallback();
			}});
		
		JButton btnOkay = new JButton("Okay");
		GridBagConstraints gbc_btnOkay = new GridBagConstraints();
		gbc_btnOkay.gridx = 2;
		gbc_btnOkay.gridy = 0;
		pnlBtn.add(btnOkay, gbc_btnOkay);
		cgListNotEmpty.addComponent("btnOkay", btnOkay);
		btnOkay.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnOkayCallback();
			}});
	
		rbgSampleRate.select(RBIDX_32K);
		rbgCodec.select(RBIDX_ADP9);
		rbgMultiChan.select(RBIDX_MULTICH_AVG);
	}
	
	/*----- Getters -----*/
	
	public boolean getExitSelection(){return this.exitSelection;}
	
	public int getSettings(SampleImportOptions ops){
		if(ops == null) return ERROR_NONE;
		
		ops.namestem = txtName.getText();
		ops.medium = cmbxMedium.getRawValue();
		ops.flagActor = cbFlagActor.isSelected();
		ops.flagEnv = cbFlagEnv.isSelected();
		ops.flagMusic = cbFlagMusic.isSelected();
		ops.flagSFX = cbFlagSFX.isSelected();
		ops.flagVox = cbFlagVox.isSelected();
		
		switch(rbgCodec.getSelectedIndex()){
		case RBIDX_ADP9: ops.twoBit = false; break;
		case RBIDX_ADP5: ops.twoBit = true; break;
		}
		
		ops.useDefaultADPCMTable = cbDefoTbl.isSelected();
		switch(rbgSampleRate.getSelectedIndex()){
		case RBIDX_32K: ops.maxSampleRate = 32000; break;
		case RBIDX_22K:	ops.maxSampleRate = 22050; break;
		}
		
		switch(rbgMultiChan.getSelectedIndex()){
		case RBIDX_MULTICH_LEFT: ops.multiChannelBehavior = ZeqerWaveIO.MULTICHAN_IMPORT_LEFTONLY; break;
		case RBIDX_MULTICH_RIGHT: ops.multiChannelBehavior = ZeqerWaveIO.MULTICHAN_IMPORT_RIGHTONLY; break;
		case RBIDX_MULTICH_AVG: ops.multiChannelBehavior = ZeqerWaveIO.MULTICHAN_IMPORT_SUM; break;
		case RBIDX_MULTICH_PICK: 
			ops.multiChannelBehavior = ZeqerWaveIO.MULTICHAN_IMPORT_SPECIFY; 
			try{
				ops.channel = Integer.parseInt(txtChannel.getText());
				if(ops.channel < 0){
					return ERROR_BAD_CUSTOM_CH;
				}
			}
			catch(NumberFormatException ex){
				ex.printStackTrace();
				return ERROR_BAD_CUSTOM_CH;
			}
			break;
		}
		
		if(cbManualLoop.isSelected()){
			try{
				ops.loopCount = Integer.parseInt(txtLoopCt.getText());
				ops.loopStart = Integer.parseInt(txtLoopSt.getText());
				ops.loopEnd = Integer.parseInt(txtLoopEd.getText());
				//Also return error if out of range?
			}
			catch(NumberFormatException ex){
				ex.printStackTrace();
				ops.loopCount = -1;
				ops.loopStart = -1;
				ops.loopEnd = -1;
				return ERROR_BAD_LOOP_TEXT;
			}
		}
		else{
			ops.loopCount = -1;
			ops.loopStart = -1;
			ops.loopEnd = -1;
		}
		return ERROR_NONE;
	}
	
	public List<String> getFilePaths(){
		int count = pathList.size();
		if(count < 1) return new LinkedList<String>();
		ArrayList<String> lst = new ArrayList<String>(count);
		lst.addAll(pathList);
		return lst;
	}
	
	public Set<String> getTags(){
		Set<String> copy = new HashSet<String>();
		copy.addAll(tags);
		return copy;
	}
	
	public int checkForError(){
		if(pathList.isEmpty()) return ERROR_NO_FILES;
		if(rbgMultiChan.getSelectedIndex() == RBIDX_MULTICH_PICK){
			try{
				int n = Integer.parseInt(txtChannel.getText());
				if(n < 0) return ERROR_BAD_CUSTOM_CH;
			}
			catch(NumberFormatException ex){return ERROR_BAD_CUSTOM_CH;}
		}
		if(cbManualLoop.isSelected()){
			try{
				int st = Integer.parseInt(txtLoopSt.getText());
				if(st < 0) return ERROR_BAD_LOOP_TEXT;
				int ed = Integer.parseInt(txtLoopEd.getText());
				if(ed <= st) return ERROR_BAD_LOOP_TEXT;
				Integer.parseInt(txtLoopCt.getText());
				
			}
			catch(NumberFormatException ex){return ERROR_BAD_LOOP_TEXT;}
		}
		return ERROR_NONE;
	}
	
	/*----- Setters -----*/
	
	/*----- Draw -----*/
	
	private void refreshFileList(){
		Collections.sort(pathList);
		
		DefaultListModel<String> model = new DefaultListModel<String>();
		for(String p : pathList){
			model.addElement(p);
		}
		
		lstFiles.setModel(model);
		//reenable();
	}
	
	public void reenable(){
		globalEnable.setEnabling(true);
		cgLoopOps.setEnabling(cbManualLoop.isSelected());
		rbgCodec.setEnabledAll(!cbDefoTbl.isSelected());
		if(pathList.isEmpty()){
			cgListNotEmpty.setEnabling(false);
		}
		else{
			cgItemSelected.setEnabling(!lstFiles.isSelectionEmpty());
		}
		repaint();
	}
	
	/*----- Callbacks -----*/
	
	private void btnOkayCallback(){
		int err = checkForError();

		if(err != ERROR_NONE){
			String errmsg = null;
			
			switch(err){
			case ERROR_NO_FILES:
				errmsg = "Please select at least one file to import.";
				break;
			case ERROR_BAD_CUSTOM_CH:
				errmsg = "Please enter a channel value that is an integer greater than or equal to 0.";
				break;
			case ERROR_BAD_LOOP_TEXT:
				errmsg = "Please enter valid loop values (integers, loop points must be positive or 0).";
				break;
			}
			
			JOptionPane.showMessageDialog(this, errmsg, "Invalid Options", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		exitSelection = true;
		closeMe();
	}
	
	private void btnCancelCallback(){
		exitSelection = false;
		closeMe();
	}
	
	private void btnTagsCallback(){
		ZeqerTagEditDialog dialog = new ZeqerTagEditDialog(parent);
		dialog.loadTags(tags);
		
		setWait();
		dialog.showMe(this);
		
		if(dialog.getExitSelection()){
			tags.clear();
			tags.addAll(dialog.getTags());
		}
		unsetWait();
	}
	
	private void btnAddCallback(){
		String last = null;
		if(core != null) last = core.getSetting(OPKEY_LAST_SMPL_IMPORT);
		
		JFileChooser fc = new JFileChooser(last);
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setMultiSelectionEnabled(true);
		ZeqerWaveIO.addImportableFileFilters(fc);
		
		int sel = fc.showOpenDialog(this);
		if(sel != JFileChooser.APPROVE_OPTION) return;
		
		File[] files = fc.getSelectedFiles();
		if(files == null) return;
		
		setWait();
		for(File f : files){
			pathList.add(f.getAbsolutePath().toString());
		}
		refreshFileList();
		unsetWait();
	}
	
	private void btnDeleteCallback(){
		if(lstFiles.isSelectionEmpty()) return;
		
		int ret = JOptionPane.showConfirmDialog(this, "Remove the selected file(s) for import?", 
				"Remove Import Files", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		
		if(ret != JOptionPane.YES_OPTION) return;
		
		setWait();
		List<String> sel = lstFiles.getSelectedValuesList();
		pathList.removeAll(sel);
		
		refreshFileList();
		unsetWait();
	}
	
}
