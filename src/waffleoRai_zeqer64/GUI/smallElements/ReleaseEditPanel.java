package waffleoRai_zeqer64.GUI.smallElements;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JSeparator;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.RadioButtonGroup;
import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;

import javax.swing.JRadioButton;
import javax.swing.JSlider;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.FlowLayout;

public class ReleaseEditPanel extends JPanel{

	private static final long serialVersionUID = -5444205536303250474L;
	
	/*--- Constants: String Keys ---*/
	
	private static final String STRKEY_TITLE = "PNLREL_TITLE";
	private static final String STRKEY_LBL_SCALE = "PNLREL_SCALE_LBL";
	private static final String STRKEY_RB_SHORT = "PNLREL_SCALE_SHORT";
	private static final String STRKEY_RB_LONG = "PNLREL_SCALE_LONG";
	private static final String STRKEY_RB_VLONG = "PNLREL_SCALE_VLONG";
	private static final String STRKEY_RB_NONE = "PNLREL_SCALE_NONE";
	private static final String STRKEY_LBL_TIME = "PNLREL_TIME_LBL";
	
	private static final String STRKEY_AMT_UNITS = "PNLREL_AMT_UNITS";
	private static final String STRKEY_AMT_NONE = "PNLREL_AMT_NONE";
	
	private static final String STRKEY_NTSC = "TVSTD_NTSC";
	private static final String STRKEY_PAL = "TVSTD_PAL";
	
	/*--- Constants ---*/
	
	public static final int MIN_WIDTH = 440;
	public static final int MIN_HEIGHT = 90;
	
	/*--- Constants: Indices ---*/
	
	public static final int RB_SCALE_IDX_SHORT = 0;
	public static final int RB_SCALE_IDX_LONG = 1;
	public static final int RB_SCALE_IDX_VLONG = 2;
	public static final int RB_SCALE_IDX_NONE = 3;
	
	/*--- Instance Variables ---*/
	
	private boolean use_ntsc = true;
	private int lastval_short = 251;
	private int lastval_long = 64;
	private int lastval_vlong = 5;
	
	private ComponentGroup globalEnable;
	
	private RadioButtonGroup rbgScale;
	private JSlider sldVal;
	private JLabel lblAmt;
	private JLabel lblNtsc;
	private JLabel lblPal;
	
	private ZeqerCoreInterface core;
	
	/*--- Init ---*/
	
	public ReleaseEditPanel(ZeqerCoreInterface corelink){
		core = corelink;
		rbgScale = new RadioButtonGroup(4);
		globalEnable = new ComponentGroup();
		
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		setPreferredSize(new Dimension(441, 90));
		
		initGUI();
	}
	
	private void initGUI(){
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 100, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 30, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblRelease = new JLabel(getString(STRKEY_TITLE));
		lblRelease.setFont(new Font("Tahoma", Font.BOLD, 13));
		GridBagConstraints gbc_lblRelease = new GridBagConstraints();
		gbc_lblRelease.anchor = GridBagConstraints.SOUTH;
		gbc_lblRelease.insets = new Insets(5, 10, 0, 0);
		gbc_lblRelease.gridx = 0;
		gbc_lblRelease.gridy = 0;
		add(lblRelease, gbc_lblRelease);
		
		JPanel pnlTvMode = new JPanel();
		GridBagConstraints gbc_pnlTvMode = new GridBagConstraints();
		gbc_pnlTvMode.anchor = GridBagConstraints.SOUTH;
		gbc_pnlTvMode.insets = new Insets(5, 0, 0, 10);
		gbc_pnlTvMode.fill = GridBagConstraints.HORIZONTAL;
		gbc_pnlTvMode.gridx = 4;
		gbc_pnlTvMode.gridy = 0;
		add(pnlTvMode, gbc_pnlTvMode);
		GridBagLayout gbl_pnlTvMode = new GridBagLayout();
		gbl_pnlTvMode.columnWidths = new int[] {0, 0, 2, 0, 0};
		gbl_pnlTvMode.rowHeights = new int[] {0, 0, 0, 0};
		gbl_pnlTvMode.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlTvMode.rowWeights = new double[]{1.0, 0.0, 1.0, Double.MIN_VALUE};
		pnlTvMode.setLayout(gbl_pnlTvMode);
		
		lblNtsc = new JLabel(getString(STRKEY_NTSC));
		GridBagConstraints gbc_lblNtsc = new GridBagConstraints();
		gbc_lblNtsc.anchor = GridBagConstraints.EAST;
		gbc_lblNtsc.insets = new Insets(5, 5, 5, 5);
		gbc_lblNtsc.gridx = 1;
		gbc_lblNtsc.gridy = 1;
		pnlTvMode.add(lblNtsc, gbc_lblNtsc);
		lblNtsc.setFont(new Font("Tahoma", Font.BOLD, 11));
		globalEnable.addComponent("lblNtsc", lblNtsc);
		lblNtsc.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				use_ntsc = true;
				updateTvModeCallback();
			}
		});
		
		JSeparator separator = new JSeparator(){
			private static final long serialVersionUID = -1380361507203748059L;
			protected void paintComponent(Graphics g){
				//TODO Try to detect orientation of the two labels?
				super.paintComponent(g);
			}
		};
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.fill = GridBagConstraints.VERTICAL;
		gbc_separator.insets = new Insets(5, 0, 5, 5);
		gbc_separator.gridx = 2;
		gbc_separator.gridy = 1;
		pnlTvMode.add(separator, gbc_separator);
		separator.setOrientation(SwingConstants.VERTICAL);
		
		
		lblPal = new JLabel(getString(STRKEY_PAL));
		lblPal.setFont(new Font("Tahoma", Font.PLAIN, 11));
		GridBagConstraints gbc_lblPal = new GridBagConstraints();
		gbc_lblPal.insets = new Insets(5, 0, 5, 5);
		gbc_lblPal.anchor = GridBagConstraints.WEST;
		gbc_lblPal.gridx = 3;
		gbc_lblPal.gridy = 1;
		pnlTvMode.add(lblPal, gbc_lblPal);
		globalEnable.addComponent("lblPal", lblPal);
		lblPal.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				use_ntsc = false;
				updateTvModeCallback();
			}
		});
		
		JLabel lblScale = new JLabel(getString(STRKEY_LBL_SCALE));
		GridBagConstraints gbc_lblScale = new GridBagConstraints();
		gbc_lblScale.anchor = GridBagConstraints.EAST;
		gbc_lblScale.insets = new Insets(0, 10, 0, 0);
		gbc_lblScale.gridx = 0;
		gbc_lblScale.gridy = 1;
		add(lblScale, gbc_lblScale);
		
		JPanel pnlScale = new JPanel();
		GridBagConstraints gbc_pnlScale = new GridBagConstraints();
		gbc_pnlScale.gridwidth = 4;
		gbc_pnlScale.fill = GridBagConstraints.BOTH;
		gbc_pnlScale.gridx = 1;
		gbc_pnlScale.gridy = 1;
		add(pnlScale, gbc_pnlScale);
		FlowLayout fl_pnlScale = new FlowLayout(FlowLayout.LEFT, 5, 5);
		fl_pnlScale.setAlignOnBaseline(true);
		pnlScale.setLayout(fl_pnlScale);
		
		JRadioButton rbShort = new JRadioButton(getString(STRKEY_RB_SHORT));
		pnlScale.add(rbShort);
		rbgScale.addButton(rbShort, RB_SCALE_IDX_SHORT);
		globalEnable.addComponent("rbShort", rbShort);
		
		JRadioButton rbLong = new JRadioButton(getString(STRKEY_RB_LONG));
		pnlScale.add(rbLong);
		rbgScale.addButton(rbLong, RB_SCALE_IDX_LONG);
		globalEnable.addComponent("rbLong", rbLong);
		
		JRadioButton rbVLong = new JRadioButton(getString(STRKEY_RB_VLONG));
		pnlScale.add(rbVLong);
		rbgScale.addButton(rbVLong, RB_SCALE_IDX_VLONG);
		globalEnable.addComponent("rbVLong", rbVLong);
		
		JRadioButton rbNone = new JRadioButton(getString(STRKEY_RB_NONE));
		pnlScale.add(rbNone);
		rbgScale.addButton(rbNone, RB_SCALE_IDX_NONE);
		globalEnable.addComponent("rbNone", rbNone);
		rbNone.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				rbSelectCallback(RB_SCALE_IDX_NONE);
			}});
		rbVLong.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				rbSelectCallback(RB_SCALE_IDX_VLONG);
			}});
		rbLong.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				rbSelectCallback(RB_SCALE_IDX_LONG);
			}});
		rbShort.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				rbSelectCallback(RB_SCALE_IDX_SHORT);
			}});
		
		JLabel lblTime = new JLabel(getString(STRKEY_LBL_TIME));
		GridBagConstraints gbc_lblTime = new GridBagConstraints();
		gbc_lblTime.anchor = GridBagConstraints.EAST;
		gbc_lblTime.insets = new Insets(0, 10, 5, 0);
		gbc_lblTime.gridx = 0;
		gbc_lblTime.gridy = 2;
		add(lblTime, gbc_lblTime);
		
		sldVal = new JSlider();
		sldVal.setMajorTickSpacing(1);
		sldVal.setMinorTickSpacing(1);
		sldVal.setInverted(true);
		GridBagConstraints gbc_sldVal = new GridBagConstraints();
		gbc_sldVal.insets = new Insets(0, 0, 5, 0);
		gbc_sldVal.fill = GridBagConstraints.HORIZONTAL;
		gbc_sldVal.gridx = 1;
		gbc_sldVal.gridy = 2;
		add(sldVal, gbc_sldVal);
		globalEnable.addComponent("sldVal", sldVal);
		sldVal.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				sliderAdjustCallback();
			}});
		
		lblAmt = new JLabel("[0] N/A");
		GridBagConstraints gbc_lblAmt = new GridBagConstraints();
		gbc_lblAmt.insets = new Insets(0, 5, 5, 0);
		gbc_lblAmt.gridx = 2;
		gbc_lblAmt.gridy = 2;
		add(lblAmt, gbc_lblAmt);
		
		rbgScale.select(RB_SCALE_IDX_SHORT);
		updateScaleToShort();
	}
	
	/*--- Getters ---*/
	
	private String getString(String key){
		if(core == null) return key;
		return core.getString(key);
	}

	public int getCurrentReleaseValue(){
		if(rbgScale.getSelectedIndex() == RB_SCALE_IDX_NONE) return 0;
		return sldVal.getValue();
	}
	
	/*--- Setters ---*/
	
	public boolean setCurrentReleaseVal(int val){
		if(val < 0 || val > 255) return false;
		if(val > 15){
			if(val > 127){
				lastval_short = val;
				updateScaleToShort();
				rbgScale.select(RB_SCALE_IDX_SHORT);
			}
			else{
				lastval_long = val;
				updateScaleToLong();
				rbgScale.select(RB_SCALE_IDX_LONG);
			}
		}
		else{
			if(val > 0){
				lastval_vlong = val;
				updateScaleToVLong();
				rbgScale.select(RB_SCALE_IDX_VLONG);
			}
			else{
				updateScaleToNone();
				rbgScale.select(RB_SCALE_IDX_NONE);
			}
		}
		
		globalEnable.repaint();
		
		return true;
	}
	
	public void setNTSC(){
		use_ntsc = true;
		updateTvModeCallback();
	}
	
	public void setPAL(){
		use_ntsc = false;
		updateTvModeCallback();
	}
	
	/*--- Drawing ---*/
	
	private void updateAmtLabel(int val){
		if(val != 0){
			int millis = 0;
			if(use_ntsc){
				millis = Z64Sound.releaseValueToMillis_NTSC(val);
			}
			else{
				millis = Z64Sound.releaseValueToMillis_PAL(val);
			}
			
			lblAmt.setText("[" + val + "] " + millis + " " + getString(STRKEY_AMT_UNITS));
		}
		else{
			lblAmt.setText("[0] " + getString(STRKEY_AMT_NONE));
		}
		lblAmt.repaint();
	}
	
	private void updateScaleToShort(){
		sldVal.setMinimum(128);
		sldVal.setMaximum(255);
		
		sldVal.setValue(lastval_short);
		
		sldVal.setEnabled(true);
		sldVal.repaint();
		
		updateAmtLabel(lastval_short);
	}
	
	private void updateScaleToLong(){
		sldVal.setMinimum(16);
		sldVal.setMaximum(127);
		
		sldVal.setValue(lastval_long);
		
		sldVal.setEnabled(true);
		sldVal.repaint();
		
		updateAmtLabel(lastval_long);
	}
	
	private void updateScaleToVLong(){
		sldVal.setMinimum(1);
		sldVal.setMaximum(15);
		
		sldVal.setValue(lastval_vlong);
		
		sldVal.setEnabled(true);
		sldVal.repaint();
		
		updateAmtLabel(lastval_vlong);
	}
	
	private void updateScaleToNone(){
		sldVal.setEnabled(false);
		sldVal.repaint();
		
		updateAmtLabel(0);
	}
	
	public void enableAll(){
		globalEnable.setEnabling(true);
		if(rbgScale.getSelectedIndex() == RB_SCALE_IDX_NONE){
			sldVal.setEnabled(false);
		}
		globalEnable.repaint();
	}
	
	public void disableAll(){
		globalEnable.setEnabling(false);
		globalEnable.repaint();
	}
	
	/*--- Callbacks ---*/
	
	private void rbSelectCallback(int index){
		int last_select = rbgScale.getSelectedIndex();
		switch(last_select){
		case RB_SCALE_IDX_SHORT:
			lastval_short = sldVal.getValue();
			break;
		case RB_SCALE_IDX_LONG:
			lastval_long = sldVal.getValue();
			break;
		case RB_SCALE_IDX_VLONG:
			lastval_vlong = sldVal.getValue();
			break;
		}
		
		rbgScale.select(index);
		rbgScale.repaintAll();
		
		switch(index){
		case RB_SCALE_IDX_SHORT:
			updateScaleToShort();
			break;
		case RB_SCALE_IDX_LONG:
			updateScaleToLong();
			break;
		case RB_SCALE_IDX_VLONG:
			updateScaleToVLong();
			break;
		default:
			updateScaleToNone();
			break;
		}
	}
	
	private void updateTvModeCallback(){
		if(use_ntsc){
			lblNtsc.setFont(new Font("Tahoma", Font.BOLD, 11));
			lblPal.setFont(new Font("Tahoma", Font.PLAIN, 11));
		}
		else{
			lblNtsc.setFont(new Font("Tahoma", Font.PLAIN, 11));
			lblPal.setFont(new Font("Tahoma", Font.BOLD, 11));
		}
		lblNtsc.repaint();
		lblPal.repaint();
		updateAmtLabel(getCurrentReleaseValue());
	}
	
	private void sliderAdjustCallback(){
		updateAmtLabel(sldVal.getValue());
	}

}
