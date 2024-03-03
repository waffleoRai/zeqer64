package waffleoRai_zeqer64.GUI.sampleImport;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.RadioButtonGroup;
import waffleoRai_GUITools.WRPanel;
import waffleoRai_zeqer64.GUI.MediumTypeCombobox;
import waffleoRai_zeqer64.filefmt.wave.ZeqerWaveIO;
import waffleoRai_zeqer64.filefmt.wave.ZeqerWaveIO.SampleImportOptions;

public class SampleOptionsPanel extends WRPanel{

	private static final long serialVersionUID = -4017800039246523276L;
	
	public static final int DEFO_WIDTH = 320;
	public static final int DEFO_HEIGHT = 550;
	
	private static final int RBIDX_ADP9 = 0;
	private static final int RBIDX_ADP5 = 1;
	private static final int RBCOUNT_CODECS = 2;
	
	private static final int RBIDX_2PRED = 0;
	private static final int RBIDX_4PRED = 1;
	private static final int RBIDX_8PRED = 2;
	private static final int RBCOUNT_NPRED = 3;
	
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
	
	private ComponentGroup cgLoopOps;
	
	private JTextField txtName;
	private MediumTypeCombobox cmbxMedium;
	private JCheckBox cbFlagMusic;
	private JCheckBox cbFlagSFX;
	private JCheckBox cbFlagActor;
	private JCheckBox cbFlagEnv;
	private JCheckBox cbFlagVox;
	
	private RadioButtonGroup rbgCodec;
	private RadioButtonGroup rbgPred;
	private RadioButtonGroup rbgSampleRate;
	private RadioButtonGroup rbgMultiChan;
	private JCheckBox cbDefoTbl;
	private JTextField txtChannel;
	
	private JCheckBox cbManualLoop;
	private JTextField txtLoopCt;
	private JTextField txtLoopSt;
	private JTextField txtLoopEd;
	
	/*----- Init -----*/
	
	public SampleOptionsPanel(){
		cgLoopOps = globalEnable.newChild();

		rbgCodec = new RadioButtonGroup(RBCOUNT_CODECS);
		rbgSampleRate = new RadioButtonGroup(RBCOUNT_SAMPLERATES);
		rbgMultiChan = new RadioButtonGroup(RBCOUNT_MULTICH);
		rbgPred = new RadioButtonGroup(RBCOUNT_NPRED);
		
		initGUI();
		reenable();
	}
	
	private void initPanel_Metadata_Flags(JPanel parent){
		JPanel pnlMetaFlags = new JPanel();
		pnlMetaFlags.setBorder(new LineBorder(new Color(0, 0, 0)));
		GridBagConstraints gbc_pnlMetaFlags = new GridBagConstraints();
		gbc_pnlMetaFlags.gridwidth = 2;
		gbc_pnlMetaFlags.insets = new Insets(0, 5, 5, 5);
		gbc_pnlMetaFlags.fill = GridBagConstraints.BOTH;
		gbc_pnlMetaFlags.gridx = 0;
		gbc_pnlMetaFlags.gridy = 3;
		parent.add(pnlMetaFlags, gbc_pnlMetaFlags);
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
	}
	
	private void initPanel_Metadata(){
		JPanel pnlMetadata = new JPanel();
		pnlMetadata.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlMetadata = new GridBagConstraints();
		gbc_pnlMetadata.weightx = 0.5;
		gbc_pnlMetadata.insets = new Insets(0, 0, 5, 0);
		gbc_pnlMetadata.fill = GridBagConstraints.BOTH;
		gbc_pnlMetadata.gridx = 0;
		gbc_pnlMetadata.gridy = 0;
		add(pnlMetadata, gbc_pnlMetadata);
		GridBagLayout gbl_pnlMetadata = new GridBagLayout();
		gbl_pnlMetadata.columnWidths = new int[]{0, 0, 0};
		gbl_pnlMetadata.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_pnlMetadata.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlMetadata.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
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
		
		initPanel_Metadata_Flags(pnlMetadata);
	}
	
	private void initPanel_Compression_Multichan(JPanel parent){
		JPanel pnlMultichan = new JPanel();
		pnlMultichan.setBorder(new LineBorder(new Color(0, 0, 0)));
		GridBagConstraints gbc_pnlMultichan = new GridBagConstraints();
		gbc_pnlMultichan.insets = new Insets(0, 5, 5, 5);
		gbc_pnlMultichan.gridwidth = 4;
		gbc_pnlMultichan.fill = GridBagConstraints.BOTH;
		gbc_pnlMultichan.gridx = 0;
		gbc_pnlMultichan.gridy = 5;
		parent.add(pnlMultichan, gbc_pnlMultichan);
		GridBagLayout gbl_pnlMultichan = new GridBagLayout();
		gbl_pnlMultichan.columnWidths = new int[]{125, 69, 75, 0};
		gbl_pnlMultichan.rowHeights = new int[]{23, 23, 0};
		gbl_pnlMultichan.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlMultichan.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		pnlMultichan.setLayout(gbl_pnlMultichan);
		
		JLabel lblMultichannelCollapse = new JLabel("Multichannel Collapse:");
		lblMultichannelCollapse.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblMultichannelCollapse = new GridBagConstraints();
		gbc_lblMultichannelCollapse.anchor = GridBagConstraints.WEST;
		gbc_lblMultichannelCollapse.insets = new Insets(5, 5, 5, 5);
		gbc_lblMultichannelCollapse.gridx = 0;
		gbc_lblMultichannelCollapse.gridy = 0;
		pnlMultichan.add(lblMultichannelCollapse, gbc_lblMultichannelCollapse);
		
		JRadioButton rbMCLeft = new JRadioButton("Left Only");
		GridBagConstraints gbc_rbMCLeft = new GridBagConstraints();
		gbc_rbMCLeft.anchor = GridBagConstraints.NORTHWEST;
		gbc_rbMCLeft.insets = new Insets(5, 0, 5, 5);
		gbc_rbMCLeft.gridx = 1;
		gbc_rbMCLeft.gridy = 0;
		pnlMultichan.add(rbMCLeft, gbc_rbMCLeft);
		globalEnable.addComponent("rbMCLeft", rbMCLeft);
		rbgMultiChan.addButton(rbMCLeft, RBIDX_MULTICH_LEFT);
		rbMCLeft.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				rbgMultiChan.select(RBIDX_MULTICH_LEFT);
				reenable();
			}});
		
		JRadioButton rbMCRight = new JRadioButton("Right Only");
		GridBagConstraints gbc_rbMCRight = new GridBagConstraints();
		gbc_rbMCRight.anchor = GridBagConstraints.NORTHWEST;
		gbc_rbMCRight.insets = new Insets(5, 0, 5, 5);
		gbc_rbMCRight.gridx = 2;
		gbc_rbMCRight.gridy = 0;
		pnlMultichan.add(rbMCRight, gbc_rbMCRight);
		globalEnable.addComponent("rbMCRight", rbMCRight);
		rbgMultiChan.addButton(rbMCRight, RBIDX_MULTICH_RIGHT);
		rbMCRight.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				rbgMultiChan.select(RBIDX_MULTICH_RIGHT);
				reenable();
			}});
		
		JRadioButton rbMCAvg = new JRadioButton("Average Channels");
		GridBagConstraints gbc_rbMCAvg = new GridBagConstraints();
		gbc_rbMCAvg.anchor = GridBagConstraints.WEST;
		gbc_rbMCAvg.insets = new Insets(0, 5, 5, 5);
		gbc_rbMCAvg.gridx = 0;
		gbc_rbMCAvg.gridy = 1;
		pnlMultichan.add(rbMCAvg, gbc_rbMCAvg);
		globalEnable.addComponent("rbMCAvg", rbMCAvg);
		rbgMultiChan.addButton(rbMCAvg, RBIDX_MULTICH_AVG);
		rbMCAvg.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				rbgMultiChan.select(RBIDX_MULTICH_AVG);
				reenable();
			}});
		
		JPanel pnlUseChan = new JPanel();
		GridBagConstraints gbc_pnlUseChan = new GridBagConstraints();
		gbc_pnlUseChan.insets = new Insets(0, 0, 5, 5);
		gbc_pnlUseChan.gridwidth = 2;
		gbc_pnlUseChan.anchor = GridBagConstraints.NORTH;
		gbc_pnlUseChan.gridx = 1;
		gbc_pnlUseChan.gridy = 1;
		pnlMultichan.add(pnlUseChan, gbc_pnlUseChan);
		GridBagLayout gbl_pnlUseChan = new GridBagLayout();
		gbl_pnlUseChan.columnWidths = new int[]{0, 50, 0};
		gbl_pnlUseChan.rowHeights = new int[]{0, 0};
		gbl_pnlUseChan.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
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
		gbc_txtChannel.insets = new Insets(5, 0, 5, 5);
		gbc_txtChannel.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtChannel.gridx = 1;
		gbc_txtChannel.gridy = 0;
		pnlUseChan.add(txtChannel, gbc_txtChannel);
		txtChannel.setColumns(10);
		globalEnable.addComponent("txtChannel", txtChannel);
	}
	
	private void initPanel_Compression(){
		JPanel pnlCompression = new JPanel();
		pnlCompression.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlCompression = new GridBagConstraints();
		gbc_pnlCompression.weightx = 0.5;
		gbc_pnlCompression.insets = new Insets(0, 0, 5, 0);
		gbc_pnlCompression.fill = GridBagConstraints.BOTH;
		gbc_pnlCompression.gridx = 0;
		gbc_pnlCompression.gridy = 1;
		add(pnlCompression, gbc_pnlCompression);
		GridBagLayout gbl_pnlCompression = new GridBagLayout();
		gbl_pnlCompression.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_pnlCompression.rowHeights = new int[]{0, 0, 0, 0, 0, 50, 0};
		gbl_pnlCompression.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlCompression.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
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
		gbc_rbADP9.gridwidth = 2;
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
		gbc_rbADP5.insets = new Insets(0, 0, 5, 0);
		gbc_rbADP5.gridx = 3;
		gbc_rbADP5.gridy = 1;
		pnlCompression.add(rbADP5, gbc_rbADP5);
		globalEnable.addComponent("rbADP5", rbADP5);
		rbgCodec.addButton(rbADP5, RBIDX_ADP5);
		rbADP5.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				rbgCodec.select(RBIDX_ADP5);
			}});
		
		JLabel lblPredictors = new JLabel("Predictors:");
		GridBagConstraints gbc_lblPredictors = new GridBagConstraints();
		gbc_lblPredictors.anchor = GridBagConstraints.EAST;
		gbc_lblPredictors.insets = new Insets(0, 0, 5, 5);
		gbc_lblPredictors.gridx = 0;
		gbc_lblPredictors.gridy = 2;
		pnlCompression.add(lblPredictors, gbc_lblPredictors);
		
		JRadioButton rbPred2 = new JRadioButton("2");
		GridBagConstraints gbc_rbPred2 = new GridBagConstraints();
		gbc_rbPred2.anchor = GridBagConstraints.WEST;
		gbc_rbPred2.insets = new Insets(0, 0, 5, 5);
		gbc_rbPred2.gridx = 1;
		gbc_rbPred2.gridy = 2;
		pnlCompression.add(rbPred2, gbc_rbPred2);
		rbgPred.addButton(rbPred2, RBIDX_2PRED);
		globalEnable.addComponent("rbPred2", rbPred2);
		rbPred2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				rbgPred.select(RBIDX_2PRED);
			}});
		
		JRadioButton rbPred4 = new JRadioButton("4");
		GridBagConstraints gbc_rbPred4 = new GridBagConstraints();
		gbc_rbPred4.anchor = GridBagConstraints.WEST;
		gbc_rbPred4.insets = new Insets(0, 0, 5, 5);
		gbc_rbPred4.gridx = 2;
		gbc_rbPred4.gridy = 2;
		pnlCompression.add(rbPred4, gbc_rbPred4);
		rbgPred.addButton(rbPred4, RBIDX_4PRED);
		globalEnable.addComponent("rbPred4", rbPred4);
		rbPred4.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				rbgPred.select(RBIDX_4PRED);
			}});
		
		JRadioButton rbPred8 = new JRadioButton("8");
		GridBagConstraints gbc_rbPred8 = new GridBagConstraints();
		gbc_rbPred8.anchor = GridBagConstraints.WEST;
		gbc_rbPred8.insets = new Insets(0, 0, 5, 0);
		gbc_rbPred8.gridx = 3;
		gbc_rbPred8.gridy = 2;
		pnlCompression.add(rbPred8, gbc_rbPred8);
		rbgPred.addButton(rbPred8, RBIDX_8PRED);
		globalEnable.addComponent("rbPred8", rbPred8);
		rbPred8.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				rbgPred.select(RBIDX_8PRED);
			}});
		
		cbDefoTbl = new JCheckBox("Use Default ADPCM Table");
		GridBagConstraints gbc_cbDefoTbl = new GridBagConstraints();
		gbc_cbDefoTbl.anchor = GridBagConstraints.WEST;
		gbc_cbDefoTbl.gridwidth = 3;
		gbc_cbDefoTbl.insets = new Insets(0, 0, 5, 0);
		gbc_cbDefoTbl.gridx = 1;
		gbc_cbDefoTbl.gridy = 3;
		pnlCompression.add(cbDefoTbl, gbc_cbDefoTbl);
		globalEnable.addComponent("cbDefoTbl", cbDefoTbl);
		cbDefoTbl.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(cbDefoTbl.isSelected()){
					rbgCodec.select(RBIDX_ADP9);
					rbgPred.select(RBIDX_4PRED);
				}
				reenable();
			}});
		
		JLabel lblSampleRateCap = new JLabel("Sample Rate Cap:");
		GridBagConstraints gbc_lblSampleRateCap = new GridBagConstraints();
		gbc_lblSampleRateCap.anchor = GridBagConstraints.EAST;
		gbc_lblSampleRateCap.insets = new Insets(0, 5, 5, 5);
		gbc_lblSampleRateCap.gridx = 0;
		gbc_lblSampleRateCap.gridy = 4;
		pnlCompression.add(lblSampleRateCap, gbc_lblSampleRateCap);
		
		JRadioButton rb32k = new JRadioButton("32000 Hz");
		GridBagConstraints gbc_rb32k = new GridBagConstraints();
		gbc_rb32k.gridwidth = 2;
		gbc_rb32k.anchor = GridBagConstraints.WEST;
		gbc_rb32k.insets = new Insets(0, 0, 5, 5);
		gbc_rb32k.gridx = 1;
		gbc_rb32k.gridy = 4;
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
		gbc_rb22k.insets = new Insets(0, 0, 5, 0);
		gbc_rb22k.gridx = 3;
		gbc_rb22k.gridy = 4;
		pnlCompression.add(rb22k, gbc_rb22k);
		globalEnable.addComponent("rb22k", rb22k);
		rbgSampleRate.addButton(rb22k, RBIDX_22K);
		rb22k.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				rbgSampleRate.select(RBIDX_22K);
			}});
		
		initPanel_Compression_Multichan(pnlCompression);
	}

	private void initPanel_Misc_Loop(JPanel parent){
		JPanel pnlLoop = new JPanel();
		GridBagConstraints gbc_pnlLoop = new GridBagConstraints();
		gbc_pnlLoop.insets = new Insets(0, 5, 5, 5);
		gbc_pnlLoop.fill = GridBagConstraints.BOTH;
		gbc_pnlLoop.gridx = 0;
		gbc_pnlLoop.gridy = 2;
		parent.add(pnlLoop, gbc_pnlLoop);
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
	}
	
	private void initPanel_Misc(){
		JPanel pnlMisc = new JPanel();
		pnlMisc.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlMisc = new GridBagConstraints();
		gbc_pnlMisc.weightx = 0.5;
		gbc_pnlMisc.fill = GridBagConstraints.BOTH;
		gbc_pnlMisc.gridx = 0;
		gbc_pnlMisc.gridy = 2;
		add(pnlMisc, gbc_pnlMisc);
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
		
		initPanel_Misc_Loop(pnlMisc);
	}
	
	private void initGUI(){
		setMinimumSize(new Dimension(DEFO_WIDTH, DEFO_HEIGHT));
		setPreferredSize(new Dimension(DEFO_WIDTH, DEFO_HEIGHT));
		
		GridBagLayout gbl_pnlOptions = new GridBagLayout();
		gbl_pnlOptions.columnWidths = new int[]{0, 0};
		gbl_pnlOptions.rowHeights = new int[]{0, 0, 0, 0};
		gbl_pnlOptions.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_pnlOptions.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gbl_pnlOptions);
		
		initPanel_Metadata();
		initPanel_Compression();
		initPanel_Misc();
		
		rbgSampleRate.select(RBIDX_32K);
		rbgCodec.select(RBIDX_ADP9);
		rbgMultiChan.select(RBIDX_MULTICH_AVG);
		rbgPred.select(RBIDX_4PRED);
	}

	/*----- Getters -----*/
	
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
		
		switch(rbgPred.getSelectedIndex()){
		case RBIDX_2PRED: ops.npredScale = 1; break;
		case RBIDX_4PRED: ops.npredScale = 2; break;
		case RBIDX_8PRED: ops.npredScale = 3; break;
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
	
	public int checkForError(){
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
	
	/*----- Draw -----*/
	
	public void reenable(){
		globalEnable.setEnabling(true);
		cgLoopOps.setEnabling(cbManualLoop.isSelected());
		rbgCodec.setEnabledAll(!cbDefoTbl.isSelected());
		rbgPred.setEnabledAll(!cbDefoTbl.isSelected());
		repaint();
	}
	
}
