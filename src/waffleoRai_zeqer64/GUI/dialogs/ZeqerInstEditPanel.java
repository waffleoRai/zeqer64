package waffleoRai_zeqer64.GUI.dialogs;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JSpinner;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.VoidCallbackMethod;
import waffleoRai_zeqer64.ZeqerCoreInterface;
import waffleoRai_zeqer64.filefmt.ZeqerWaveTable.WaveTableEntry;

import javax.swing.JButton;
import javax.swing.JSlider;

public class ZeqerInstEditPanel extends JPanel{
	
	//Allow for externally set spinner value change callback
	//	This can allow for automatically pushing the value up or down 
	//	for the high and low regions if the other region's boundary would cross it.
	// (ie. high is moved down, so low is moved down too if high would then be <=)
	
	private static final long serialVersionUID = -846022860700414964L;
	
	public static final int MIN_WIDTH = 300;
	public static final int MIN_HEIGHT_NOREG = 100;
	public static final int MIN_HEIGHT_REG = 145;
	
	public static final int REGION_TYPE_MID = 0;
	public static final int REGION_TYPE_LO = 1;
	public static final int REGION_TYPE_HI = 2;
	
	/*----- Instance Variables -----*/
	
	private JFrame parent;
	private ZeqerCoreInterface core;
	
	private ComponentGroup incl_enable;
	
	private JTextField txtSample;
	private JTextField txtFineTune;
	private JCheckBox cbInclude;
	private JSpinner spnUnityKey;
	private JSpinner spnLimitNote;
	private JSlider sldFineTune;
	private JLabel lblFineAmt;
	
	private VoidCallbackMethod noteRangeSpinnerCallback;
	
	private VoidCallbackMethod setSampleStartCallback;
	private VoidCallbackMethod setSampleDoneCallback;
	
	private int sampleUID = -1; //Selected sample
	
	/*----- Init -----*/
	
	/**
	 * @wbp.parser.constructor
	 */
	public ZeqerInstEditPanel(JFrame parent_frame, ZeqerCoreInterface core_iface){
		parent = parent_frame;
		core = core_iface;
		initGUI(REGION_TYPE_LO);
	}
	
	public ZeqerInstEditPanel(JFrame parent_frame, ZeqerCoreInterface core_iface, int region_type){
		parent = parent_frame;
		core = core_iface;
		initGUI(region_type);
	}
	
	private void initGUI(int region_type){
		
		incl_enable = new ComponentGroup();
		
		boolean inclReg = (region_type != REGION_TYPE_MID);
		int height = !inclReg ? MIN_HEIGHT_NOREG:MIN_HEIGHT_REG;
		setMinimumSize(new Dimension(MIN_WIDTH, height));
		setPreferredSize(new Dimension(284, 145));
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JPanel pnlHiLo = new JPanel();
		GridBagConstraints gbc_pnlHiLo = new GridBagConstraints();
		gbc_pnlHiLo.insets = new Insets(0, 0, 5, 0);
		gbc_pnlHiLo.fill = GridBagConstraints.BOTH;
		gbc_pnlHiLo.gridx = 0;
		gbc_pnlHiLo.gridy = 0;
		add(pnlHiLo, gbc_pnlHiLo);
		GridBagLayout gbl_pnlHiLo = new GridBagLayout();
		gbl_pnlHiLo.columnWidths = new int[]{0, 0, 0, 60, 0};
		gbl_pnlHiLo.rowHeights = new int[]{0, 0};
		gbl_pnlHiLo.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlHiLo.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlHiLo.setLayout(gbl_pnlHiLo);
		
		cbInclude = new JCheckBox("Include");
		GridBagConstraints gbc_chckbxInclude = new GridBagConstraints();
		gbc_chckbxInclude.insets = new Insets(5, 5, 5, 5);
		gbc_chckbxInclude.gridx = 0;
		gbc_chckbxInclude.gridy = 0;
		pnlHiLo.add(cbInclude, gbc_chckbxInclude);
		if(inclReg){
			cbInclude.setSelected(true);
		}
		else{
			cbInclude.setSelected(false);
			cbInclude.setEnabled(false);
			cbInclude.setVisible(false);
		}
		cbInclude.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				cbIncludeCallback();
			}});
		
		JLabel lblRangeLimit = new JLabel("Range Limit:");
		GridBagConstraints gbc_lblRangeLimit = new GridBagConstraints();
		gbc_lblRangeLimit.insets = new Insets(0, 0, 0, 5);
		gbc_lblRangeLimit.gridx = 2;
		gbc_lblRangeLimit.gridy = 0;
		pnlHiLo.add(lblRangeLimit, gbc_lblRangeLimit);
		
		spnLimitNote = new JSpinner();
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.insets = new Insets(0, 0, 0, 5);
		gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner.gridx = 3;
		gbc_spinner.gridy = 0;
		pnlHiLo.add(spnLimitNote, gbc_spinner);
		//incl_enable.addComponent("spnLimitNote", spnLimitNote);
		initKeySpinnerModel(spnLimitNote);
		spnLimitNote.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				if(noteRangeSpinnerCallback != null) noteRangeSpinnerCallback.doMethod();
			}});
		switch(region_type){
		case REGION_TYPE_MID:
			lblRangeLimit.setVisible(false);
			spnLimitNote.setEnabled(false);
			spnLimitNote.setVisible(false);
			break;
		case REGION_TYPE_LO:
			lblRangeLimit.setText("Max Note:");
			break;
		case REGION_TYPE_HI:
			lblRangeLimit.setText("Min Note:");
			break;
		}
		
		JPanel pnlTune = new JPanel();
		GridBagConstraints gbc_pnlTune = new GridBagConstraints();
		gbc_pnlTune.insets = new Insets(0, 0, 5, 0);
		gbc_pnlTune.fill = GridBagConstraints.BOTH;
		gbc_pnlTune.gridx = 0;
		gbc_pnlTune.gridy = 1;
		add(pnlTune, gbc_pnlTune);
		GridBagLayout gbl_pnlTune = new GridBagLayout();
		gbl_pnlTune.columnWidths = new int[]{0, 55, 100, 0, 0};
		gbl_pnlTune.rowHeights = new int[]{0, 0, 0};
		gbl_pnlTune.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlTune.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		pnlTune.setLayout(gbl_pnlTune);
		
		JLabel lblUnityKey = new JLabel("Unity Key:");
		GridBagConstraints gbc_lblUnityKey = new GridBagConstraints();
		gbc_lblUnityKey.insets = new Insets(0, 5, 5, 5);
		gbc_lblUnityKey.gridx = 0;
		gbc_lblUnityKey.gridy = 0;
		pnlTune.add(lblUnityKey, gbc_lblUnityKey);
		
		spnUnityKey = new JSpinner();
		GridBagConstraints gbc_spinner2 = new GridBagConstraints();
		gbc_spinner2.fill = GridBagConstraints.BOTH;
		gbc_spinner2.insets = new Insets(0, 0, 5, 5);
		gbc_spinner2.gridx = 1;
		gbc_spinner2.gridy = 0;
		pnlTune.add(spnUnityKey, gbc_spinner2);
		incl_enable.addComponent("spnUnityKey", spnUnityKey);
		initKeySpinnerModel(spnUnityKey);
		
		JLabel lblFineTune = new JLabel("Fine Tune:");
		GridBagConstraints gbc_lblFineTune = new GridBagConstraints();
		gbc_lblFineTune.insets = new Insets(0, 5, 0, 5);
		gbc_lblFineTune.gridx = 0;
		gbc_lblFineTune.gridy = 1;
		pnlTune.add(lblFineTune, gbc_lblFineTune);
		
		sldFineTune = new JSlider();
		GridBagConstraints gbc_slider = new GridBagConstraints();
		gbc_slider.gridwidth = 2;
		gbc_slider.fill = GridBagConstraints.HORIZONTAL;
		gbc_slider.insets = new Insets(0, 0, 0, 5);
		gbc_slider.gridx = 1;
		gbc_slider.gridy = 1;
		pnlTune.add(sldFineTune, gbc_slider);
		incl_enable.addComponent("sldFineTune", sldFineTune);
		sldFineTune.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				updateFineTuneLabel(sldFineTune.getValue());
			}});
		
		lblFineAmt = new JLabel("0 cents");
		GridBagConstraints gbc_lblFineAmt = new GridBagConstraints();
		gbc_lblFineAmt.anchor = GridBagConstraints.WEST;
		gbc_lblFineAmt.insets = new Insets(0, 0, 0, 5);
		gbc_lblFineAmt.gridx = 3;
		gbc_lblFineAmt.gridy = 1;
		pnlTune.add(lblFineAmt, gbc_lblFineAmt);
		
		JPanel pnlSample = new JPanel();
		GridBagConstraints gbc_pnlSample = new GridBagConstraints();
		gbc_pnlSample.fill = GridBagConstraints.BOTH;
		gbc_pnlSample.gridx = 0;
		gbc_pnlSample.gridy = 2;
		add(pnlSample, gbc_pnlSample);
		GridBagLayout gbl_pnlSample = new GridBagLayout();
		gbl_pnlSample.columnWidths = new int[]{0, 0, 0};
		gbl_pnlSample.rowHeights = new int[]{0, 0, 0};
		gbl_pnlSample.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlSample.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		pnlSample.setLayout(gbl_pnlSample);
		
		JLabel lblSample = new JLabel("Sample:");
		GridBagConstraints gbc_lblSample = new GridBagConstraints();
		gbc_lblSample.anchor = GridBagConstraints.EAST;
		gbc_lblSample.insets = new Insets(0, 5, 5, 5);
		gbc_lblSample.gridx = 0;
		gbc_lblSample.gridy = 0;
		pnlSample.add(lblSample, gbc_lblSample);
		
		txtSample = new JTextField();
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField.gridx = 1;
		gbc_textField.gridy = 0;
		pnlSample.add(txtSample, gbc_textField);
		txtSample.setColumns(10);
		incl_enable.addComponent("txtSample", txtSample);
		
		JButton btnSample = new JButton("Set Sample...");
		GridBagConstraints gbc_btnSample = new GridBagConstraints();
		gbc_btnSample.insets = new Insets(0, 0, 5, 0);
		gbc_btnSample.gridx = 1;
		gbc_btnSample.gridy = 1;
		pnlSample.add(btnSample, gbc_btnSample);
		incl_enable.addComponent("btnSample", btnSample);
		btnSample.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnSetSampleCallback();
			}});
		
		initFineTuneSlider();
		setEnabled();
	}
	
	private void initKeySpinnerModel(JSpinner spn){
		spn.setModel(new SpinnerNumberModel(60, 0, 127, 1));
	}
	
	private void initFineTuneSlider(){
		sldFineTune.setMinimum(-100);
		sldFineTune.setMaximum(100);
		sldFineTune.setValue(0);
		sldFineTune.setSnapToTicks(true);
		sldFineTune.setMinorTickSpacing(1);
		sldFineTune.setMajorTickSpacing(5);
	}
	
	/*----- Getters -----*/
	
	public boolean regionIncluded(){return cbInclude.isSelected();}
	public int getSelectedSampleUID(){return sampleUID;}
	public int getLimitNote(){return (Integer)spnLimitNote.getValue();}
	public int getUnityKey(){return (Integer)spnUnityKey.getValue();}
	
	public int getFineTune(){
		return sldFineTune.getValue();
	}
	
	/*----- Setters -----*/
	
	public void setRegionIncluded(boolean val){
		cbInclude.setSelected(val);
		setEnabled();
	}
	
	public void clearSample(){
		txtSample.setText("");
		sampleUID = -1;
		txtSample.repaint();
	}
	
	public void setSample(WaveTableEntry smpl){
		if(smpl != null){
			txtSample.setText(smpl.getName());
			sampleUID = smpl.getUID();
		}
		else{
			txtSample.setText("");
			sampleUID = -1;
		}
		txtSample.repaint();
	}
	
	public void setSample(Z64WaveInfo smpl){
		if(smpl != null){
			txtSample.setText(smpl.getName());
			sampleUID = smpl.getUID();
		}
		else{
			txtSample.setText("");
			sampleUID = -1;
		}
		txtSample.repaint();
	}
	
	public void setLimitNote(byte value){
		if(value < 0) return;
		spnLimitNote.setValue(new Integer(value));
		spnLimitNote.repaint();
	}
	
	public void setUnityKey(byte value){
		if(value < 0) return;
		spnUnityKey.setValue(new Integer(value));
		spnUnityKey.repaint();
	}
	
	public void setFineTune(int value){
		//Clamp value between -100 and 100
		if(value < -100) value = -100;
		if(value > 100) value = 100;
		txtFineTune.setText(Integer.toString(value));
		txtFineTune.repaint();
	}
	
	public void setNoteLimitSpinnerCallback(VoidCallbackMethod func){
		noteRangeSpinnerCallback = func;
	}
	
	public void setSampleSetStartCallback(VoidCallbackMethod func){
		setSampleStartCallback = func;
	}
	
	public void setSampleSetDoneCallback(VoidCallbackMethod func){
		setSampleDoneCallback = func;
	}
	
	/*----- GUI Management -----*/
	
	private void updateFineTuneLabel(int value){
		lblFineAmt.setText(value + " cents");
		lblFineAmt.repaint();
	}
	
	public void setDisabled(){
		incl_enable.setEnabling(false);
		incl_enable.repaint();
		
		if(cbInclude.isVisible()){
			spnLimitNote.setEnabled(false);
			cbInclude.setEnabled(false);
			spnLimitNote.repaint();
			cbInclude.repaint();	
		}
	}
	
	public void setEnabled(){
		//All but cb is enabled based on cb value
		incl_enable.setEnabling(cbInclude.isSelected());
		incl_enable.repaint();
		
		if(cbInclude.isVisible()){
			spnLimitNote.setEnabled(true);
			cbInclude.setEnabled(true);
			spnLimitNote.repaint();
			cbInclude.repaint();	
		}
	}
	
	/*----- Callbacks -----*/
	
	private void cbIncludeCallback(){
		//Enables/disables other components
		setEnabled();
	}
	
	private void btnSetSampleCallback(){
		if(setSampleStartCallback != null) setSampleStartCallback.doMethod();
		setDisabled();
		SamplePickDialog dialog = new SamplePickDialog(parent, core);
		dialog.setVisible(true);
		
		if(dialog.getExitSelection()){
			WaveTableEntry sel = dialog.getSelectedSample();
			setSample(sel);
		}

		setEnabled();
		if(setSampleDoneCallback != null) setSampleDoneCallback.doMethod();
	}
	
}
