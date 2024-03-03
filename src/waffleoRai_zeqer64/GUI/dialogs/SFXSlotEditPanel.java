package waffleoRai_zeqer64.GUI.dialogs;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JLabel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.VoidCallbackMethod;
import waffleoRai_zeqer64.filefmt.wave.WaveTableEntry;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;

import javax.swing.JSlider;

public class SFXSlotEditPanel extends JPanel{

	private static final long serialVersionUID = -467936528951106929L;
	
	public static final int DEFO_WIDTH = 365;
	public static final int DEFO_HEIGHT = 170;
	
	/*----- Instance Variables -----*/
	
	private JFrame parent;
	private ZeqerCoreInterface core;
	
	private ComponentGroup globalEnable;
	
	private JTextField txtName;
	private JTextField txtEnum;
	private JTextField txtSample;
	
	private JSlider sldCoarse;
	private JSlider sldFine;
	private JLabel lblCoarseAmt;
	private JLabel lblFineAmt;
	
	private Z64WaveInfo setSample = null;
	private VoidCallbackMethod setSampleStartCallback = null;
	private VoidCallbackMethod setSampleDoneCallback = null;
	
	/*----- Init -----*/
	
	public SFXSlotEditPanel(JFrame parent_frame, ZeqerCoreInterface core_iface){
		parent = parent_frame;
		core = core_iface;
		globalEnable = new ComponentGroup();
		initGUI();
	}
	
	private void initGUI(){
		setMinimumSize(new Dimension(DEFO_WIDTH, DEFO_HEIGHT));
		setPreferredSize(new Dimension(DEFO_WIDTH, DEFO_HEIGHT));
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 100, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JLabel lblName = new JLabel("Name:");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(5, 5, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		add(lblName, gbc_lblName);
		
		txtName = new JTextField();
		GridBagConstraints gbc_txtName = new GridBagConstraints();
		gbc_txtName.gridwidth = 2;
		gbc_txtName.insets = new Insets(5, 0, 5, 5);
		gbc_txtName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtName.gridx = 1;
		gbc_txtName.gridy = 0;
		add(txtName, gbc_txtName);
		txtName.setColumns(10);
		globalEnable.addComponent("txtName", txtName);
		
		JLabel lblSlot = new JLabel("Enum:");
		GridBagConstraints gbc_lblSlot = new GridBagConstraints();
		gbc_lblSlot.anchor = GridBagConstraints.EAST;
		gbc_lblSlot.insets = new Insets(0, 5, 5, 5);
		gbc_lblSlot.gridx = 0;
		gbc_lblSlot.gridy = 1;
		add(lblSlot, gbc_lblSlot);
		
		txtEnum = new JTextField();
		GridBagConstraints gbc_txtEnum = new GridBagConstraints();
		gbc_txtEnum.gridwidth = 2;
		gbc_txtEnum.insets = new Insets(0, 0, 5, 5);
		gbc_txtEnum.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtEnum.gridx = 1;
		gbc_txtEnum.gridy = 1;
		add(txtEnum, gbc_txtEnum);
		txtEnum.setColumns(10);
		globalEnable.addComponent("txtEnum", txtEnum);
		
		JLabel lblSample = new JLabel("Sample:");
		GridBagConstraints gbc_lblSample = new GridBagConstraints();
		gbc_lblSample.anchor = GridBagConstraints.EAST;
		gbc_lblSample.insets = new Insets(0, 5, 5, 5);
		gbc_lblSample.gridx = 0;
		gbc_lblSample.gridy = 2;
		add(lblSample, gbc_lblSample);
		
		txtSample = new JTextField();
		GridBagConstraints gbc_txtSample = new GridBagConstraints();
		gbc_txtSample.gridwidth = 2;
		gbc_txtSample.insets = new Insets(0, 0, 5, 5);
		gbc_txtSample.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtSample.gridx = 1;
		gbc_txtSample.gridy = 2;
		add(txtSample, gbc_txtSample);
		txtSample.setColumns(10);
		globalEnable.addComponent("txtSample", txtSample);
		
		JButton btnSetSample = new JButton("Set Sample...");
		GridBagConstraints gbc_btnSetSample = new GridBagConstraints();
		gbc_btnSetSample.insets = new Insets(0, 0, 5, 5);
		gbc_btnSetSample.gridx = 1;
		gbc_btnSetSample.gridy = 3;
		add(btnSetSample, gbc_btnSetSample);
		globalEnable.addComponent("btnSetSample", btnSetSample);
		btnSetSample.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {btnSetSampleCallback();}});
		
		JLabel lblCoarseTune = new JLabel("Coarse Tune:");
		GridBagConstraints gbc_lblCoarseTune = new GridBagConstraints();
		gbc_lblCoarseTune.anchor = GridBagConstraints.EAST;
		gbc_lblCoarseTune.insets = new Insets(0, 5, 5, 5);
		gbc_lblCoarseTune.gridx = 0;
		gbc_lblCoarseTune.gridy = 4;
		add(lblCoarseTune, gbc_lblCoarseTune);
		
		sldCoarse = new JSlider();
		sldCoarse.setValue(0);
		sldCoarse.setMaximum(60);
		sldCoarse.setMinimum(-60);
		GridBagConstraints gbc_sldCoarse = new GridBagConstraints();
		gbc_sldCoarse.insets = new Insets(0, 0, 5, 5);
		gbc_sldCoarse.gridx = 1;
		gbc_sldCoarse.gridy = 4;
		add(sldCoarse, gbc_sldCoarse);
		globalEnable.addComponent("sldCoarse", sldCoarse);
		sldCoarse.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				updateCoarseTuneLabel(sldCoarse.getValue());
			}});
		
		lblCoarseAmt = new JLabel("0 semitones");
		GridBagConstraints gbc_lblCoarseAmt = new GridBagConstraints();
		gbc_lblCoarseAmt.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblCoarseAmt.insets = new Insets(0, 0, 5, 5);
		gbc_lblCoarseAmt.gridx = 2;
		gbc_lblCoarseAmt.gridy = 4;
		add(lblCoarseAmt, gbc_lblCoarseAmt);
		
		JLabel lblFineTune = new JLabel("Fine Tune:");
		GridBagConstraints gbc_lblFineTune = new GridBagConstraints();
		gbc_lblFineTune.anchor = GridBagConstraints.EAST;
		gbc_lblFineTune.insets = new Insets(0, 5, 5, 5);
		gbc_lblFineTune.gridx = 0;
		gbc_lblFineTune.gridy = 5;
		add(lblFineTune, gbc_lblFineTune);
		
		sldFine = new JSlider();
		sldFine.setValue(0);
		sldFine.setMinorTickSpacing(1);
		sldFine.setMajorTickSpacing(5);
		sldFine.setMinimum(-100);
		GridBagConstraints gbc_sldFine = new GridBagConstraints();
		gbc_sldFine.insets = new Insets(0, 0, 5, 5);
		gbc_sldFine.gridx = 1;
		gbc_sldFine.gridy = 5;
		add(sldFine, gbc_sldFine);
		globalEnable.addComponent("sldFine", sldFine);
		sldFine.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				updateFineTuneLabel(sldFine.getValue());
			}});
		
		lblFineAmt = new JLabel("0 cents");
		GridBagConstraints gbc_lblFineAmt = new GridBagConstraints();
		gbc_lblFineAmt.insets = new Insets(0, 0, 5, 5);
		gbc_lblFineAmt.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblFineAmt.gridx = 2;
		gbc_lblFineAmt.gridy = 5;
		add(lblFineAmt, gbc_lblFineAmt);
	}
	
	/*----- Getters -----*/
	
	public String getNameString(){return txtName.getText();}
	public String getEnumString(){return txtEnum.getText();}
	public Z64WaveInfo getSetSample(){return setSample;}
	public int getCoarseTune(){return sldCoarse.getValue();}
	public int getFineTune(){return sldCoarse.getValue();}
	
	/*----- Setters -----*/
	
	public void setNameString(String value){txtName.setText(value);}
	public void setEnumString(String value){txtEnum.setText(value);}
	
	public void setCoarseTune(int value){
		value = Math.max(-60, value);
		value = Math.min(60, value);
		sldCoarse.setValue(value);
		updateCoarseTuneLabel(value);
	}
	
	public void setFineTune(int value){
		value = Math.max(-100, value);
		value = Math.min(100, value);
		sldFine.setValue(value);
		updateFineTuneLabel(value);
	}
	
	public void setSample(Z64WaveInfo sample){
		setSample = sample;
		if(setSample != null){
			txtSample.setText(String.format("[%08x] %s", setSample.getUID(), setSample.getName()));
		}
		else{
			txtSample.setText("<No Sample>");
		}
		txtSample.repaint();
	}
	
	public void setSampleSelectStartCallback(VoidCallbackMethod func){setSampleStartCallback = func;}
	public void setSampleSelectDoneCallback(VoidCallbackMethod func){setSampleDoneCallback = func;}
	
	/*----- GUI Management -----*/
	
	public void disableAll(){
		globalEnable.setEnabling(false);
	}
	
	public void enableAll(){
		globalEnable.setEnabling(true);
	}
	
	private void updateCoarseTuneLabel(int value){
		lblCoarseAmt.setText(value + " semitones");
		lblCoarseAmt.repaint();
	}
	
	private void updateFineTuneLabel(int value){
		lblFineAmt.setText(value + " cents");
		lblFineAmt.repaint();
	}
	
	/*----- Callbacks -----*/
	
	private void btnSetSampleCallback(){
		if(setSampleStartCallback != null) setSampleStartCallback.doMethod();
		disableAll();
		SamplePickDialog dialog = new SamplePickDialog(parent, core);
		dialog.setVisible(true);
		
		if(dialog.getExitSelection()){
			WaveTableEntry sel = dialog.getSelectedSample();
			if(sel != null){
				setSample(sel.getWaveInfo());
			}
			else setSample(null);
		}
		enableAll();
		if(setSampleDoneCallback != null) setSampleDoneCallback.doMethod();
	}
	
}
