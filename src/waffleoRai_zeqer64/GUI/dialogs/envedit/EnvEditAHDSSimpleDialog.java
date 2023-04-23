package waffleoRai_zeqer64.GUI.dialogs.envedit;

import javax.swing.JDialog;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import javax.swing.JButton;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JRadioButton;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.GUITools;
import waffleoRai_GUITools.RadioButtonGroup;
import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_soundbank.nintendo.z64.Z64Envelope;

public class EnvEditAHDSSimpleDialog extends JDialog{
	//Can take AHDS parameters in ms to generate an applicable script
		//Curve types: Linear Env, Linear dB
	
	private static final long serialVersionUID = -4600961464127776010L;
	
	public static final int DEFO_WIDTH = 350;
	public static final int DEFO_HEIGHT = 466;
	
	private static final int RB_COUNT_CURVE = 2;
	private static final int RB_COUNT_DIR = 3;
	private static final int RB_COUNT_TIME = 2;
	
	private static final int RB_CURVE_IDX_LINENV = 0;
	private static final int RB_CURVE_IDX_LINDB = 1;
	
	private static final int RB_DIR_IDX_FLAT = 0;
	private static final int RB_DIR_IDX_UP = 1;
	private static final int RB_DIR_IDX_DOWN = 2;
	
	private static final int RB_TIMETYPE_IDX_TOZERO = 0;
	private static final int RB_TIMETYPE_IDX_TOSUS = 1;
	
	private static final int PARAM_ERR_OKAY = 0;
	private static final int PARAM_ERR_ATIME = 1;
	private static final int PARAM_ERR_HTIME = 2;
	private static final int PARAM_ERR_DTIME = 3;
	private static final int PARAM_ERR_SLVL = 4;
	private static final int PARAM_ERR_STIME = 5;

	/*----- Instance Variables -----*/
	
	private Frame parent;
	
	private JTextField txtATime;
	private JTextField txtHTime;
	private JTextField txtDTime;
	private JTextField txtSLevel;
	private JTextField txtSTime;
	
	private RadioButtonGroup rbgACurve;
	private RadioButtonGroup rbgDCurve;
	private RadioButtonGroup rbgDTime;
	private RadioButtonGroup rbgSDir;
	private RadioButtonGroup rbgSCurve;
	
	private ComponentGroup globalEnable;
	
	private Z64Envelope env;
	
	/*----- Init -----*/
	
	public EnvEditAHDSSimpleDialog(Frame parent_frame){
		super(parent_frame, true);
		parent = parent_frame;
		globalEnable = new ComponentGroup();
		rbgACurve = new RadioButtonGroup(RB_COUNT_CURVE);
		rbgDCurve = new RadioButtonGroup(RB_COUNT_CURVE);
		rbgSCurve = new RadioButtonGroup(RB_COUNT_CURVE);
		rbgSDir = new RadioButtonGroup(RB_COUNT_DIR);
		rbgDTime = new RadioButtonGroup(RB_COUNT_TIME);
		
		initGUI();
		
		onClickSDirRadio(RB_DIR_IDX_FLAT);
		onClickACurveRadio(RB_CURVE_IDX_LINENV);
		onClickDCurveRadio(RB_CURVE_IDX_LINENV);
		onClickSCurveRadio(RB_CURVE_IDX_LINENV);
		onClickDTimeRadio(RB_TIMETYPE_IDX_TOZERO);
	}
	
	private void initGUI(){
		setMinimumSize(new Dimension(DEFO_WIDTH, DEFO_HEIGHT));
		setPreferredSize(new Dimension(DEFO_WIDTH, DEFO_HEIGHT));
		setResizable(false);
		
		setTitle("Create Basic Envelope");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel pnlAttack = new JPanel();
		pnlAttack.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlAttack = new GridBagConstraints();
		gbc_pnlAttack.insets = new Insets(0, 0, 5, 0);
		gbc_pnlAttack.fill = GridBagConstraints.BOTH;
		gbc_pnlAttack.gridx = 0;
		gbc_pnlAttack.gridy = 0;
		getContentPane().add(pnlAttack, gbc_pnlAttack);
		GridBagLayout gbl_pnlAttack = new GridBagLayout();
		gbl_pnlAttack.columnWidths = new int[]{0, 0};
		gbl_pnlAttack.rowHeights = new int[]{0, 0, 0, 0};
		gbl_pnlAttack.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlAttack.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlAttack.setLayout(gbl_pnlAttack);
		
		JLabel lblAttack = new JLabel("Attack");
		lblAttack.setFont(new Font("Tahoma", Font.BOLD, 13));
		GridBagConstraints gbc_lblAttack = new GridBagConstraints();
		gbc_lblAttack.insets = new Insets(5, 5, 5, 5);
		gbc_lblAttack.gridx = 0;
		gbc_lblAttack.gridy = 0;
		pnlAttack.add(lblAttack, gbc_lblAttack);
		
		JPanel pnlATime = new JPanel();
		GridBagConstraints gbc_pnlATime = new GridBagConstraints();
		gbc_pnlATime.insets = new Insets(0, 0, 5, 0);
		gbc_pnlATime.fill = GridBagConstraints.BOTH;
		gbc_pnlATime.gridx = 0;
		gbc_pnlATime.gridy = 1;
		pnlAttack.add(pnlATime, gbc_pnlATime);
		GridBagLayout gbl_pnlATime = new GridBagLayout();
		gbl_pnlATime.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlATime.rowHeights = new int[]{0, 0};
		gbl_pnlATime.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlATime.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlATime.setLayout(gbl_pnlATime);
		
		JLabel lblTime = new JLabel("Time:");
		GridBagConstraints gbc_lblTime = new GridBagConstraints();
		gbc_lblTime.anchor = GridBagConstraints.EAST;
		gbc_lblTime.insets = new Insets(5, 10, 5, 5);
		gbc_lblTime.gridx = 0;
		gbc_lblTime.gridy = 0;
		pnlATime.add(lblTime, gbc_lblTime);
		
		txtATime = new JTextField();
		GridBagConstraints gbc_txtATime = new GridBagConstraints();
		gbc_txtATime.insets = new Insets(5, 5, 5, 5);
		gbc_txtATime.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtATime.gridx = 1;
		gbc_txtATime.gridy = 0;
		pnlATime.add(txtATime, gbc_txtATime);
		txtATime.setColumns(10);
		globalEnable.addComponent("txtATime", txtATime);
		
		JLabel lblMilliseconds = new JLabel("milliseconds");
		lblMilliseconds.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblMilliseconds = new GridBagConstraints();
		gbc_lblMilliseconds.insets = new Insets(5, 5, 5, 5);
		gbc_lblMilliseconds.anchor = GridBagConstraints.WEST;
		gbc_lblMilliseconds.gridx = 2;
		gbc_lblMilliseconds.gridy = 0;
		pnlATime.add(lblMilliseconds, gbc_lblMilliseconds);
		
		JPanel pnlACurve = new JPanel();
		GridBagConstraints gbc_pnlACurve = new GridBagConstraints();
		gbc_pnlACurve.fill = GridBagConstraints.BOTH;
		gbc_pnlACurve.gridx = 0;
		gbc_pnlACurve.gridy = 2;
		pnlAttack.add(pnlACurve, gbc_pnlACurve);
		GridBagLayout gbl_pnlACurve = new GridBagLayout();
		gbl_pnlACurve.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_pnlACurve.rowHeights = new int[]{0, 0};
		gbl_pnlACurve.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlACurve.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlACurve.setLayout(gbl_pnlACurve);
		
		JLabel lblCurveType = new JLabel("Curve Type:");
		GridBagConstraints gbc_lblCurveType = new GridBagConstraints();
		gbc_lblCurveType.insets = new Insets(5, 10, 5, 5);
		gbc_lblCurveType.gridx = 0;
		gbc_lblCurveType.gridy = 0;
		pnlACurve.add(lblCurveType, gbc_lblCurveType);
		
		JRadioButton rbACurveLinEnv = new JRadioButton("Linear Envelope");
		GridBagConstraints gbc_rbACurveLinEnv = new GridBagConstraints();
		gbc_rbACurveLinEnv.insets = new Insets(0, 0, 0, 5);
		gbc_rbACurveLinEnv.gridx = 1;
		gbc_rbACurveLinEnv.gridy = 0;
		pnlACurve.add(rbACurveLinEnv, gbc_rbACurveLinEnv);
		globalEnable.addComponent("rbACurveLinEnv", rbACurveLinEnv);
		rbgACurve.addButton(rbACurveLinEnv, RB_CURVE_IDX_LINENV);
		rbACurveLinEnv.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {onClickACurveRadio(RB_CURVE_IDX_LINENV);}
		});
		
		JRadioButton rbACurveLinDb = new JRadioButton("Linear dB");
		GridBagConstraints gbc_rbACurveLinDb = new GridBagConstraints();
		gbc_rbACurveLinDb.insets = new Insets(0, 0, 0, 5);
		gbc_rbACurveLinDb.gridx = 2;
		gbc_rbACurveLinDb.gridy = 0;
		pnlACurve.add(rbACurveLinDb, gbc_rbACurveLinDb);
		globalEnable.addComponent("rbACurveLinDb", rbACurveLinDb);
		rbgACurve.addButton(rbACurveLinDb, RB_CURVE_IDX_LINDB);
		rbACurveLinDb.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {onClickACurveRadio(RB_CURVE_IDX_LINDB);}
		});
		
		JPanel pnlHold = new JPanel();
		pnlHold.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlHold = new GridBagConstraints();
		gbc_pnlHold.insets = new Insets(0, 0, 5, 0);
		gbc_pnlHold.fill = GridBagConstraints.BOTH;
		gbc_pnlHold.gridx = 0;
		gbc_pnlHold.gridy = 1;
		getContentPane().add(pnlHold, gbc_pnlHold);
		GridBagLayout gbl_pnlHold = new GridBagLayout();
		gbl_pnlHold.columnWidths = new int[]{0, 0};
		gbl_pnlHold.rowHeights = new int[]{0, 0, 0};
		gbl_pnlHold.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlHold.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		pnlHold.setLayout(gbl_pnlHold);
		
		JLabel lblHold = new JLabel("Hold");
		lblHold.setFont(new Font("Tahoma", Font.BOLD, 13));
		GridBagConstraints gbc_lblHold = new GridBagConstraints();
		gbc_lblHold.insets = new Insets(5, 5, 5, 0);
		gbc_lblHold.gridx = 0;
		gbc_lblHold.gridy = 0;
		pnlHold.add(lblHold, gbc_lblHold);
		
		JPanel pnlHTime = new JPanel();
		GridBagConstraints gbc_pnlHTime = new GridBagConstraints();
		gbc_pnlHTime.fill = GridBagConstraints.BOTH;
		gbc_pnlHTime.gridx = 0;
		gbc_pnlHTime.gridy = 1;
		pnlHold.add(pnlHTime, gbc_pnlHTime);
		GridBagLayout gbl_pnlHTime = new GridBagLayout();
		gbl_pnlHTime.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlHTime.rowHeights = new int[]{0, 0};
		gbl_pnlHTime.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlHTime.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlHTime.setLayout(gbl_pnlHTime);
		
		JLabel label = new JLabel("Time:");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.EAST;
		gbc_label.insets = new Insets(5, 10, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = 0;
		pnlHTime.add(label, gbc_label);
		
		txtHTime = new JTextField();
		txtHTime.setColumns(10);
		GridBagConstraints gbc_txtHTime = new GridBagConstraints();
		gbc_txtHTime.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtHTime.insets = new Insets(5, 5, 5, 5);
		gbc_txtHTime.gridx = 1;
		gbc_txtHTime.gridy = 0;
		pnlHTime.add(txtHTime, gbc_txtHTime);
		globalEnable.addComponent("txtHTime", txtHTime);
		
		JLabel label_1 = new JLabel("milliseconds");
		label_1.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_label_1 = new GridBagConstraints();
		gbc_label_1.insets = new Insets(5, 5, 5, 5);
		gbc_label_1.anchor = GridBagConstraints.WEST;
		gbc_label_1.gridx = 2;
		gbc_label_1.gridy = 0;
		pnlHTime.add(label_1, gbc_label_1);
		
		JPanel pnlDecay = new JPanel();
		pnlDecay.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlDecay = new GridBagConstraints();
		gbc_pnlDecay.insets = new Insets(0, 0, 5, 0);
		gbc_pnlDecay.fill = GridBagConstraints.BOTH;
		gbc_pnlDecay.gridx = 0;
		gbc_pnlDecay.gridy = 2;
		getContentPane().add(pnlDecay, gbc_pnlDecay);
		GridBagLayout gbl_pnlDecay = new GridBagLayout();
		gbl_pnlDecay.columnWidths = new int[]{0, 0};
		gbl_pnlDecay.rowHeights = new int[]{0, 0, 0, 0};
		gbl_pnlDecay.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlDecay.rowWeights = new double[]{0.0, 1.0, 1.0, Double.MIN_VALUE};
		pnlDecay.setLayout(gbl_pnlDecay);
		
		JLabel lblDecay = new JLabel("Decay");
		lblDecay.setFont(new Font("Tahoma", Font.BOLD, 13));
		GridBagConstraints gbc_lblDecay = new GridBagConstraints();
		gbc_lblDecay.insets = new Insets(5, 5, 5, 5);
		gbc_lblDecay.gridx = 0;
		gbc_lblDecay.gridy = 0;
		pnlDecay.add(lblDecay, gbc_lblDecay);
		
		JPanel pnlDTime = new JPanel();
		GridBagConstraints gbc_pnlDTime = new GridBagConstraints();
		gbc_pnlDTime.insets = new Insets(0, 0, 5, 0);
		gbc_pnlDTime.fill = GridBagConstraints.BOTH;
		gbc_pnlDTime.gridx = 0;
		gbc_pnlDTime.gridy = 1;
		pnlDecay.add(pnlDTime, gbc_pnlDTime);
		GridBagLayout gbl_pnlDTime = new GridBagLayout();
		gbl_pnlDTime.columnWidths = new int[]{0, 75, 0, 0, 0, 0, 0};
		gbl_pnlDTime.rowHeights = new int[]{0, 0};
		gbl_pnlDTime.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlDTime.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlDTime.setLayout(gbl_pnlDTime);
		
		JLabel lblTime_1 = new JLabel("Time:");
		GridBagConstraints gbc_lblTime_1 = new GridBagConstraints();
		gbc_lblTime_1.anchor = GridBagConstraints.EAST;
		gbc_lblTime_1.insets = new Insets(5, 10, 5, 5);
		gbc_lblTime_1.gridx = 0;
		gbc_lblTime_1.gridy = 0;
		pnlDTime.add(lblTime_1, gbc_lblTime_1);
		
		txtDTime = new JTextField();
		GridBagConstraints gbc_txtDTime = new GridBagConstraints();
		gbc_txtDTime.insets = new Insets(5, 5, 5, 0);
		gbc_txtDTime.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtDTime.gridx = 1;
		gbc_txtDTime.gridy = 0;
		pnlDTime.add(txtDTime, gbc_txtDTime);
		txtDTime.setColumns(10);
		globalEnable.addComponent("txtDTime", txtDTime);
		
		JLabel lblMs = new JLabel("ms");
		GridBagConstraints gbc_lblMs = new GridBagConstraints();
		gbc_lblMs.insets = new Insets(5, 5, 5, 0);
		gbc_lblMs.gridx = 2;
		gbc_lblMs.gridy = 0;
		pnlDTime.add(lblMs, gbc_lblMs);
		
		JRadioButton rbDT1 = new JRadioButton("To Silence");
		GridBagConstraints gbc_rbDT1 = new GridBagConstraints();
		gbc_rbDT1.insets = new Insets(5, 0, 5, 0);
		gbc_rbDT1.gridx = 4;
		gbc_rbDT1.gridy = 0;
		pnlDTime.add(rbDT1, gbc_rbDT1);
		globalEnable.addComponent("rbDT1", rbDT1);
		rbgDTime.addButton(rbDT1, RB_TIMETYPE_IDX_TOZERO);
		rbDT1.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {onClickDTimeRadio(RB_TIMETYPE_IDX_TOZERO);}
		});
		
		JRadioButton rbDT2 = new JRadioButton("To Sustain");
		GridBagConstraints gbc_rbDT2 = new GridBagConstraints();
		gbc_rbDT2.insets = new Insets(5, 5, 5, 5);
		gbc_rbDT2.gridx = 5;
		gbc_rbDT2.gridy = 0;
		pnlDTime.add(rbDT2, gbc_rbDT2);
		globalEnable.addComponent("rbDT2", rbDT2);
		rbgDTime.addButton(rbDT2, RB_TIMETYPE_IDX_TOSUS);
		rbDT2.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {onClickDTimeRadio(RB_TIMETYPE_IDX_TOSUS);}
		});
		
		JPanel pnlDCurve = new JPanel();
		GridBagConstraints gbc_pnlDCurve = new GridBagConstraints();
		gbc_pnlDCurve.fill = GridBagConstraints.BOTH;
		gbc_pnlDCurve.gridx = 0;
		gbc_pnlDCurve.gridy = 2;
		pnlDecay.add(pnlDCurve, gbc_pnlDCurve);
		GridBagLayout gbl_pnlDCurve = new GridBagLayout();
		gbl_pnlDCurve.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_pnlDCurve.rowHeights = new int[]{0, 0};
		gbl_pnlDCurve.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlDCurve.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlDCurve.setLayout(gbl_pnlDCurve);
		
		JLabel lblCurveType_1 = new JLabel("Curve Type:");
		GridBagConstraints gbc_lblCurveType_1 = new GridBagConstraints();
		gbc_lblCurveType_1.insets = new Insets(5, 10, 5, 5);
		gbc_lblCurveType_1.gridx = 0;
		gbc_lblCurveType_1.gridy = 0;
		pnlDCurve.add(lblCurveType_1, gbc_lblCurveType_1);
		
		JRadioButton rbDCurveLinEnv = new JRadioButton("Linear Envelope");
		GridBagConstraints gbc_rbDCurveLinEnv = new GridBagConstraints();
		gbc_rbDCurveLinEnv.insets = new Insets(0, 0, 0, 5);
		gbc_rbDCurveLinEnv.gridx = 1;
		gbc_rbDCurveLinEnv.gridy = 0;
		pnlDCurve.add(rbDCurveLinEnv, gbc_rbDCurveLinEnv);
		globalEnable.addComponent("rbDCurveLinEnv", rbDCurveLinEnv);
		rbgDCurve.addButton(rbDCurveLinEnv, RB_CURVE_IDX_LINENV);
		rbDCurveLinEnv.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {onClickDCurveRadio(RB_CURVE_IDX_LINENV);}
		});
		
		JRadioButton rbDCurveLinDb = new JRadioButton("Linear dB");
		GridBagConstraints gbc_rbDCurveLinDb = new GridBagConstraints();
		gbc_rbDCurveLinDb.insets = new Insets(0, 0, 0, 5);
		gbc_rbDCurveLinDb.gridx = 2;
		gbc_rbDCurveLinDb.gridy = 0;
		pnlDCurve.add(rbDCurveLinDb, gbc_rbDCurveLinDb);
		globalEnable.addComponent("rbDCurveLinDb", rbDCurveLinDb);
		rbgDCurve.addButton(rbDCurveLinDb, RB_CURVE_IDX_LINDB);
		rbDCurveLinDb.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {onClickDCurveRadio(RB_CURVE_IDX_LINDB);}
		});
		
		JPanel pnlSustain = new JPanel();
		pnlSustain.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlSustain = new GridBagConstraints();
		gbc_pnlSustain.insets = new Insets(0, 0, 5, 0);
		gbc_pnlSustain.fill = GridBagConstraints.BOTH;
		gbc_pnlSustain.gridx = 0;
		gbc_pnlSustain.gridy = 3;
		getContentPane().add(pnlSustain, gbc_pnlSustain);
		GridBagLayout gbl_pnlSustain = new GridBagLayout();
		gbl_pnlSustain.columnWidths = new int[]{0, 0};
		gbl_pnlSustain.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_pnlSustain.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlSustain.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlSustain.setLayout(gbl_pnlSustain);
		
		JLabel lblNewLabel = new JLabel("Sustain");
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 13));
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(5, 5, 5, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		pnlSustain.add(lblNewLabel, gbc_lblNewLabel);
		
		JPanel pnlSLevel = new JPanel();
		GridBagConstraints gbc_pnlSLevel = new GridBagConstraints();
		gbc_pnlSLevel.insets = new Insets(0, 0, 5, 0);
		gbc_pnlSLevel.fill = GridBagConstraints.BOTH;
		gbc_pnlSLevel.gridx = 0;
		gbc_pnlSLevel.gridy = 1;
		pnlSustain.add(pnlSLevel, gbc_pnlSLevel);
		GridBagLayout gbl_pnlSLevel = new GridBagLayout();
		gbl_pnlSLevel.columnWidths = new int[]{0, 75, 0, 0};
		gbl_pnlSLevel.rowHeights = new int[]{0, 0};
		gbl_pnlSLevel.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlSLevel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlSLevel.setLayout(gbl_pnlSLevel);
		
		JLabel lblLevel = new JLabel("Level: ");
		GridBagConstraints gbc_lblLevel = new GridBagConstraints();
		gbc_lblLevel.insets = new Insets(5, 10, 0, 5);
		gbc_lblLevel.anchor = GridBagConstraints.EAST;
		gbc_lblLevel.gridx = 0;
		gbc_lblLevel.gridy = 0;
		pnlSLevel.add(lblLevel, gbc_lblLevel);
		
		txtSLevel = new JTextField();
		GridBagConstraints gbc_txtSLevel = new GridBagConstraints();
		gbc_txtSLevel.insets = new Insets(0, 0, 0, 5);
		gbc_txtSLevel.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtSLevel.gridx = 1;
		gbc_txtSLevel.gridy = 0;
		globalEnable.addComponent("txtSLevel", txtSLevel);
		pnlSLevel.add(txtSLevel, gbc_txtSLevel);
		txtSLevel.setColumns(10);
		
		JPanel pnlSDir = new JPanel();
		GridBagConstraints gbc_pnlSDir = new GridBagConstraints();
		gbc_pnlSDir.insets = new Insets(0, 0, 5, 0);
		gbc_pnlSDir.fill = GridBagConstraints.BOTH;
		gbc_pnlSDir.gridx = 0;
		gbc_pnlSDir.gridy = 2;
		pnlSustain.add(pnlSDir, gbc_pnlSDir);
		GridBagLayout gbl_pnlSDir = new GridBagLayout();
		gbl_pnlSDir.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_pnlSDir.rowHeights = new int[]{0, 0};
		gbl_pnlSDir.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlSDir.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlSDir.setLayout(gbl_pnlSDir);
		
		JLabel lblDirection = new JLabel("Direction:");
		GridBagConstraints gbc_lblDirection = new GridBagConstraints();
		gbc_lblDirection.insets = new Insets(5, 10, 5, 5);
		gbc_lblDirection.gridx = 0;
		gbc_lblDirection.gridy = 0;
		pnlSDir.add(lblDirection, gbc_lblDirection);
		
		JRadioButton rbSDirFlat = new JRadioButton("Flat");
		GridBagConstraints gbc_rbSDirFlat = new GridBagConstraints();
		gbc_rbSDirFlat.insets = new Insets(0, 0, 0, 5);
		gbc_rbSDirFlat.gridx = 1;
		gbc_rbSDirFlat.gridy = 0;
		pnlSDir.add(rbSDirFlat, gbc_rbSDirFlat);
		globalEnable.addComponent("rbSDirFlat", rbSDirFlat);
		rbgSDir.addButton(rbSDirFlat, RB_DIR_IDX_FLAT);
		rbSDirFlat.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {onClickSDirRadio(RB_DIR_IDX_FLAT);}
		});
		
		JRadioButton rbSDirUp = new JRadioButton("Increase");
		GridBagConstraints gbc_rbSDirUp = new GridBagConstraints();
		gbc_rbSDirUp.insets = new Insets(0, 0, 0, 5);
		gbc_rbSDirUp.gridx = 2;
		gbc_rbSDirUp.gridy = 0;
		pnlSDir.add(rbSDirUp, gbc_rbSDirUp);
		globalEnable.addComponent("rbSDirUp", rbSDirUp);
		rbgSDir.addButton(rbSDirUp, RB_DIR_IDX_UP);
		rbSDirUp.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {onClickSDirRadio(RB_DIR_IDX_UP);}
		});
		
		JRadioButton rbSDirDown = new JRadioButton("Decrease");
		GridBagConstraints gbc_rbSDirDown = new GridBagConstraints();
		gbc_rbSDirDown.insets = new Insets(0, 0, 0, 5);
		gbc_rbSDirDown.gridx = 3;
		gbc_rbSDirDown.gridy = 0;
		pnlSDir.add(rbSDirDown, gbc_rbSDirDown);
		globalEnable.addComponent("rbSDirDown", rbSDirDown);
		rbgSDir.addButton(rbSDirDown, RB_DIR_IDX_DOWN);
		rbSDirDown.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {onClickSDirRadio(RB_DIR_IDX_DOWN);}
		});
		
		JPanel pnlSTime = new JPanel();
		GridBagConstraints gbc_pnlSTime = new GridBagConstraints();
		gbc_pnlSTime.insets = new Insets(0, 0, 5, 0);
		gbc_pnlSTime.fill = GridBagConstraints.BOTH;
		gbc_pnlSTime.gridx = 0;
		gbc_pnlSTime.gridy = 3;
		pnlSustain.add(pnlSTime, gbc_pnlSTime);
		GridBagLayout gbl_pnlSTime = new GridBagLayout();
		gbl_pnlSTime.columnWidths = new int[]{0, 75, 0, 0};
		gbl_pnlSTime.rowHeights = new int[]{0, 0};
		gbl_pnlSTime.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlSTime.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlSTime.setLayout(gbl_pnlSTime);
		
		JLabel lblTime_2 = new JLabel("Time:");
		GridBagConstraints gbc_lblTime_2 = new GridBagConstraints();
		gbc_lblTime_2.insets = new Insets(5, 10, 5, 5);
		gbc_lblTime_2.anchor = GridBagConstraints.EAST;
		gbc_lblTime_2.gridx = 0;
		gbc_lblTime_2.gridy = 0;
		pnlSTime.add(lblTime_2, gbc_lblTime_2);
		
		txtSTime = new JTextField();
		GridBagConstraints gbc_txtSTime = new GridBagConstraints();
		gbc_txtSTime.insets = new Insets(0, 0, 0, 5);
		gbc_txtSTime.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtSTime.gridx = 1;
		gbc_txtSTime.gridy = 0;
		pnlSTime.add(txtSTime, gbc_txtSTime);
		txtSTime.setColumns(10);
		globalEnable.addComponent("txtSTime", txtSTime);
		
		JLabel lblMilliseconds_1 = new JLabel("milliseconds");
		GridBagConstraints gbc_lblMilliseconds_1 = new GridBagConstraints();
		gbc_lblMilliseconds_1.insets = new Insets(0, 5, 0, 0);
		gbc_lblMilliseconds_1.anchor = GridBagConstraints.WEST;
		gbc_lblMilliseconds_1.gridx = 2;
		gbc_lblMilliseconds_1.gridy = 0;
		pnlSTime.add(lblMilliseconds_1, gbc_lblMilliseconds_1);
		
		JPanel pnlSCurve = new JPanel();
		GridBagConstraints gbc_pnlSCurve = new GridBagConstraints();
		gbc_pnlSCurve.fill = GridBagConstraints.BOTH;
		gbc_pnlSCurve.gridx = 0;
		gbc_pnlSCurve.gridy = 4;
		pnlSustain.add(pnlSCurve, gbc_pnlSCurve);
		GridBagLayout gbl_pnlSCurve = new GridBagLayout();
		gbl_pnlSCurve.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlSCurve.rowHeights = new int[]{0, 0};
		gbl_pnlSCurve.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlSCurve.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlSCurve.setLayout(gbl_pnlSCurve);
		
		JLabel lblCurveType_2 = new JLabel("Curve Type:");
		GridBagConstraints gbc_lblCurveType_2 = new GridBagConstraints();
		gbc_lblCurveType_2.insets = new Insets(5, 10, 5, 5);
		gbc_lblCurveType_2.gridx = 0;
		gbc_lblCurveType_2.gridy = 0;
		pnlSCurve.add(lblCurveType_2, gbc_lblCurveType_2);
		
		JRadioButton rbSCurveLinEnv = new JRadioButton("Linear Envelope");
		GridBagConstraints gbc_rbSCurveLinEnv = new GridBagConstraints();
		gbc_rbSCurveLinEnv.insets = new Insets(0, 0, 0, 5);
		gbc_rbSCurveLinEnv.gridx = 1;
		gbc_rbSCurveLinEnv.gridy = 0;
		pnlSCurve.add(rbSCurveLinEnv, gbc_rbSCurveLinEnv);
		globalEnable.addComponent("rbSCurveLinEnv", rbSCurveLinEnv);
		rbgSCurve.addButton(rbSCurveLinEnv, RB_CURVE_IDX_LINENV);
		rbSCurveLinEnv.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {onClickSCurveRadio(RB_CURVE_IDX_LINENV);}
		});
		
		JRadioButton rbSCurveLinDb = new JRadioButton("Linear dB");
		GridBagConstraints gbc_rbSCurveLinDb = new GridBagConstraints();
		gbc_rbSCurveLinDb.gridx = 2;
		gbc_rbSCurveLinDb.gridy = 0;
		pnlSCurve.add(rbSCurveLinDb, gbc_rbSCurveLinDb);
		globalEnable.addComponent("rbSCurveLinDb", rbSCurveLinDb);
		rbgSCurve.addButton(rbSCurveLinDb, RB_CURVE_IDX_LINDB);
		rbSCurveLinDb.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {onClickSCurveRadio(RB_CURVE_IDX_LINDB);}
		});
		
		JPanel pnlButtons = new JPanel();
		GridBagConstraints gbc_pnlButtons = new GridBagConstraints();
		gbc_pnlButtons.fill = GridBagConstraints.BOTH;
		gbc_pnlButtons.gridx = 0;
		gbc_pnlButtons.gridy = 5;
		getContentPane().add(pnlButtons, gbc_pnlButtons);
		GridBagLayout gbl_pnlButtons = new GridBagLayout();
		gbl_pnlButtons.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlButtons.rowHeights = new int[]{0, 0};
		gbl_pnlButtons.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlButtons.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlButtons.setLayout(gbl_pnlButtons);
		
		JButton btnOkay = new JButton("Okay");
		GridBagConstraints gbc_btnOkay = new GridBagConstraints();
		gbc_btnOkay.insets = new Insets(5, 5, 5, 5);
		gbc_btnOkay.gridx = 1;
		gbc_btnOkay.gridy = 0;
		pnlButtons.add(btnOkay, gbc_btnOkay);
		btnOkay.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {onOkayButton();}
		});
		globalEnable.addComponent("btnOkay", btnOkay);
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(5, 5, 5, 5);
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 0;
		pnlButtons.add(btnCancel, gbc_btnCancel);
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {onCancelButton();}
		});
		globalEnable.addComponent("btnCancel", btnCancel);
	}
	
	/*----- Envelope Generation -----*/
	
	private double decibelsToEnvRatio(int db){
		double val = (double)db/20.0;
		return Math.pow(10.0, val);
	}
	
	private int readTextBoxValue(JTextField txt){
		if(txt == null) return -1;
		try{return Integer.parseInt(txt.getText());}
		catch(NumberFormatException ex){return -1;}
	}
	
	private int checkParameterValidity(){
		//Returns an error code
		int i = 0;
		try{
			i = Integer.parseInt(txtATime.getText());
			if(i < 5) return PARAM_ERR_ATIME;
			if(i > Z64Sound.ENV_MAX_DELTA_MS) return PARAM_ERR_ATIME;
		}
		catch(NumberFormatException ex){return PARAM_ERR_ATIME;}
		
		try{
			i = Integer.parseInt(txtHTime.getText());
			if(i < 0) return PARAM_ERR_HTIME;
			if(i > Z64Sound.ENV_MAX_DELTA_MS) return PARAM_ERR_HTIME;
		}
		catch(NumberFormatException ex){return PARAM_ERR_HTIME;}
		
		try{
			i = Integer.parseInt(txtDTime.getText());
			if(i < 0) return PARAM_ERR_DTIME;
			if(i > Z64Sound.ENV_MAX_DELTA_MS) return PARAM_ERR_DTIME;
		}
		catch(NumberFormatException ex){return PARAM_ERR_DTIME;}
		
		try{
			double d = Double.parseDouble(txtSLevel.getText());
			if(d < 0.0) return PARAM_ERR_SLVL;
			if(i > 1.0) return PARAM_ERR_SLVL;
		}
		catch(NumberFormatException ex){return PARAM_ERR_SLVL;}
		
		if(this.rbgSDir.getSelectedIndex() != RB_DIR_IDX_FLAT){
			try{
				i = Integer.parseInt(txtSTime.getText());
				if(i < 0) return PARAM_ERR_STIME;
				if(i > Z64Sound.ENV_MAX_DELTA_MS) return PARAM_ERR_STIME;
			}
			catch(NumberFormatException ex){return PARAM_ERR_STIME;}	
		}
		
		return PARAM_ERR_OKAY;
	}
	
	private int addLinearChangeToEnv(Z64Envelope env, int start_lvl, int targ_lvl, int delta, int stop_lvl){
		//Returns actual delta
		if(stop_lvl != start_lvl){
			//Add only part of the line
			if(targ_lvl >= start_lvl){
				if(stop_lvl > targ_lvl || stop_lvl <= start_lvl){
					//Don't need to trim.
					env.addEvent((short)delta, (short)targ_lvl);
					return delta;
				}
				else{
					int fulldiff = targ_lvl - start_lvl;
					int usediff = stop_lvl - start_lvl;
					double usedamt = (double)usediff / (double)fulldiff;
					int scaled_delta = (int)Math.round(usedamt * (double)delta);
					if(scaled_delta < 1) scaled_delta = 1;
					env.addEvent((short)scaled_delta, (short)stop_lvl);
					return scaled_delta;
				}
			}
			else{
				if(stop_lvl < targ_lvl || stop_lvl >= start_lvl){
					//Don't need to trim.
					env.addEvent((short)delta, (short)targ_lvl);
					return delta;
				}
				else{
					int fulldiff = start_lvl - targ_lvl;
					int usediff = start_lvl - stop_lvl;
					double usedamt = (double)usediff / (double)fulldiff;
					int scaled_delta = (int)Math.round(usedamt * (double)delta);
					if(scaled_delta < 1) scaled_delta = 1;
					env.addEvent((short)scaled_delta, (short)stop_lvl);
					return scaled_delta;
				}
			}
		}
		else{
			env.addEvent((short)delta, (short)targ_lvl);
			return delta;
		}
	}
	
	private void addLinearDBChangeToEnv(Z64Envelope env, int start_lvl, int targ_lvl, int delta, int stop_lvl){
		//Delta is time in refresh units
		double r = 0.0;
		int lvl_delta = 0;
		int act_delta = 0;
		int lvl = 0;
		int last_lvl = 0;
		double lvldiff = (double)(targ_lvl - start_lvl);
		if(delta >= 2){
			if(delta >= 3){
				if(delta >= 4){
					//4 lines
					double u_per_db = (double)delta/50.0;
					int delta_l = (int)Math.round(u_per_db * 15.0);
					r = decibelsToEnvRatio(-35);
					lvl_delta = (int)Math.round(r * lvldiff);
					lvl = start_lvl + lvl_delta;
					act_delta = addLinearChangeToEnv(env, start_lvl, lvl, delta_l, stop_lvl);
					if(act_delta < delta_l) return;

					r = decibelsToEnvRatio(-20);
					lvl_delta = (int)Math.round(r * lvldiff);
					last_lvl = lvl;
					lvl = start_lvl + lvl_delta;
					act_delta = addLinearChangeToEnv(env, last_lvl, lvl, delta_l, stop_lvl);
					if(act_delta < delta_l) return;
					
					delta_l = (int)Math.round(u_per_db * 10.0);
					r = decibelsToEnvRatio(-10);
					lvl_delta = (int)Math.round(r * lvldiff);
					last_lvl = lvl;
					lvl = start_lvl + lvl_delta;
					act_delta = addLinearChangeToEnv(env, last_lvl, lvl, delta_l, stop_lvl);
					if(act_delta < delta_l) return;
					
					act_delta = addLinearChangeToEnv(env, lvl, targ_lvl, delta_l, stop_lvl);
				}
				else{
					//3 lines
					r = decibelsToEnvRatio(-35);
					lvl_delta = (int)Math.round(r * lvldiff);
					lvl = start_lvl + lvl_delta;
					if(lvldiff >= 0.0){
						if(stop_lvl >= lvl){
							env.addEvent((short)1, (short)stop_lvl);
							return;
						}
						env.addEvent((short)1, (short)lvl);
					}
					else{
						if(stop_lvl <= lvl){
							env.addEvent((short)1, (short)stop_lvl);
							return;
						}
						env.addEvent((short)1, (short)lvl);
					}
					
					r = decibelsToEnvRatio(-10);
					lvl_delta = (int)Math.round(r * lvldiff);
					lvl = start_lvl + lvl_delta;
					if(lvldiff >= 0.0){
						if(stop_lvl >= lvl){
							env.addEvent((short)1, (short)stop_lvl);
							return;
						}
						env.addEvent((short)1, (short)lvl);
					}
					else{
						if(stop_lvl <= lvl){
							env.addEvent((short)1, (short)stop_lvl);
							return;
						}
						env.addEvent((short)1, (short)lvl);
					}
					
					env.addEvent((short)1, (short)stop_lvl);
				}
			}
			else{
				//2 lines
				r = decibelsToEnvRatio(-20);
				lvl_delta = (int)Math.round(r * lvldiff);
				lvl = start_lvl + lvl_delta;
				if(lvldiff >= 0.0){
					if(stop_lvl >= lvl){
						env.addEvent((short)1, (short)stop_lvl);
						return;
					}
					env.addEvent((short)1, (short)lvl);
				}
				else{
					if(stop_lvl <= lvl){
						env.addEvent((short)1, (short)stop_lvl);
						return;
					}
					env.addEvent((short)1, (short)lvl);
				}
				
				env.addEvent((short)1, (short)stop_lvl);
			}
		}
		else{
			//Can only fit in one.
			if(stop_lvl != targ_lvl){
				env.addEvent((short)1, (short)stop_lvl);
			}
			else{
				env.addEvent((short)1, (short)targ_lvl);
			}
		}
	}
	
	private void generateEnvelope(){
		Z64Envelope nenv = new Z64Envelope();
		
		//I'll split into four commands for each linear dB choice.
		//...I don't recommend linear dB lol
		
		//Attack
		int time = readTextBoxValue(txtATime);
		int t_units = Z64Sound.envelopeMillisToDelta(time);
		switch(rbgACurve.getSelectedIndex()){
		case RB_CURVE_IDX_LINENV:
			nenv.addEvent((short)t_units, (short)32700);
			break;
		case RB_CURVE_IDX_LINDB:
			addLinearDBChangeToEnv(nenv, 0, 32700, t_units, 32700);
			break;
		}
		
		//Hold
		time = readTextBoxValue(txtHTime);
		if(time > 0){
			t_units = Z64Sound.envelopeMillisToDelta(time);
			nenv.addEvent((short)t_units, (short)32700);
		}
		
		//Decay
		double slvl = Double.parseDouble(txtSLevel.getText());
		int slvlabs = (int)Math.round(slvl * 32700.0);
		if(slvl < 1.0){
			int targlvl = slvlabs;
			
			int rbidx = rbgDTime.getSelectedIndex();
			time = readTextBoxValue(txtDTime);
			t_units = Z64Sound.envelopeMillisToDelta(time);
			if(rbidx == RB_TIMETYPE_IDX_TOZERO){
				rbidx = rbgDCurve.getSelectedIndex();
				if(rbidx == RB_CURVE_IDX_LINENV){
					//Rescale the time
					double rescale = 1.0 - slvl;
					t_units = (int)Math.round(rescale * (double)t_units);
					nenv.addEvent((short)t_units, (short)targlvl);
				}
				else if(rbidx == RB_CURVE_IDX_LINDB){
					//Why do I do this to myself.
					addLinearDBChangeToEnv(nenv, 32700, 0, t_units, targlvl);
				}
			}
			else if(rbidx == RB_TIMETYPE_IDX_TOSUS){
				rbidx = rbgDCurve.getSelectedIndex();
				if(rbidx == RB_CURVE_IDX_LINENV){
					nenv.addEvent((short)t_units, (short)targlvl);
				}
				else if(rbidx == RB_CURVE_IDX_LINDB){
					addLinearDBChangeToEnv(nenv, 32700, targlvl, t_units, targlvl);
				}
			}
		}

		//Sustain
		int rbidx = rbgSDir.getSelectedIndex();
		switch(rbidx){
		case RB_DIR_IDX_FLAT:
			nenv.addEvent((short)32700, (short)slvlabs);
			break;
		case RB_DIR_IDX_UP:
			time = readTextBoxValue(txtSTime);
			t_units = Z64Sound.envelopeMillisToDelta(time);
			rbidx = rbgSCurve.getSelectedIndex();
			if(rbidx == RB_CURVE_IDX_LINENV){
				nenv.addEvent((short)t_units, (short)32700);
			}
			else if(rbidx == RB_CURVE_IDX_LINDB){
				addLinearDBChangeToEnv(nenv, slvlabs, 32700, t_units, 32700);
			}
			break;
		case RB_DIR_IDX_DOWN:
			time = readTextBoxValue(txtSTime);
			t_units = Z64Sound.envelopeMillisToDelta(time);
			rbidx = rbgSCurve.getSelectedIndex();
			if(rbidx == RB_CURVE_IDX_LINENV){
				nenv.addEvent((short)t_units, (short)0);
			}
			else if(rbidx == RB_CURVE_IDX_LINDB){
				addLinearDBChangeToEnv(nenv, slvlabs, 0, t_units, 0);
			}
			break;
		}
		
		//Add an ADSR_HANG command at the end
		nenv.addEvent((short)Z64Sound.ENVCMD__ADSR_HANG, (short)0);
	}
	
	public Z64Envelope getEnvelope(){return env;}
	
	/*----- Component Management -----*/
	
	public void setWait(){
		globalEnable.setEnabling(false);
		globalEnable.repaint();
		super.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	public void unsetWait(){
		globalEnable.setEnabling(true);
		globalEnable.repaint();
		super.setCursor(null);
	}
	
	public void closeMe(){
		this.setVisible(false);
		this.dispose();
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
	
	/*----- Callbacks -----*/
	
	private void onOkayButton(){
		//Needs to check for textbox parameter validity too
		//SLevel should be from 0.0 - 1.0
		setWait();
		int pverr = checkParameterValidity();
		if(pverr != PARAM_ERR_OKAY){
			String errmsg = "";
			switch(pverr){
			case PARAM_ERR_ATIME:
				errmsg = "Attack time is invalid! (Must be integer between 0 and 10000)";
				break;
			case PARAM_ERR_HTIME:
				errmsg = "Hold time is invalid! (Must be integer between 0 and 10000)";
				break;
			case PARAM_ERR_DTIME:
				errmsg = "Decay time is invalid! (Must be integer between 0 and 10000)";
				break;
			case PARAM_ERR_STIME:
				errmsg = "Sustain time is invalid! (Must be integer between 0 and 10000)";
				break;
			case PARAM_ERR_SLVL:
				errmsg = "Sustain level is invalid! (Must be between 0.0 and 1.0)";
				break;
			default: errmsg = "Internal Error!"; break;
			}
			JOptionPane.showMessageDialog(this, errmsg, "Parameter Invalid", JOptionPane.ERROR_MESSAGE);
			unsetWait();
		}
		else{
			generateEnvelope();
			unsetWait();
			closeMe();
		}
	}
	
	private void onCancelButton(){
		env = null;
		closeMe();
	}
	
	private void onClickSDirRadio(int id){
		rbgSDir.select(id);
		rbgSDir.repaintAll();
		if(id == RB_DIR_IDX_FLAT){
			txtSTime.setEnabled(false);
			rbgSDir.setEnabledAll(false);
		}
		else{
			txtSTime.setEnabled(true);
			rbgSDir.setEnabledAll(true);
		}
		txtSTime.repaint();
		rbgSDir.repaintAll();
	}
	
	private void onClickSCurveRadio(int id){
		rbgSCurve.select(id);
		rbgSCurve.repaintAll();
	}
	
	private void onClickACurveRadio(int id){
		rbgACurve.select(id);
		rbgACurve.repaintAll();
	}
	
	private void onClickDCurveRadio(int id){
		rbgDCurve.select(id);
		rbgDCurve.repaintAll();
	}
	
	private void onClickDTimeRadio(int id){
		rbgDTime.select(id);
		rbgDTime.repaintAll();
	}
	
}
