package waffleoRai_zeqer64.GUI.dialogs;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JSpinner;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_Utils.VoidCallbackMethod;
import waffleoRai_zeqer64.filefmt.ZeqerWaveTable.WaveTableEntry;

import javax.swing.JButton;

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
	
	private ComponentGroup incl_enable;
	
	private JTextField txtSample;
	private JTextField txtFineTune;
	private JCheckBox cbInclude;
	private JSpinner spnUnityKey;
	private JSpinner spnLimitNote;
	
	private VoidCallbackMethod noteRangeSpinnerCallback;
	
	private VoidCallbackMethod setSampleStartCallback;
	private VoidCallbackMethod setSampleDoneCallback;
	
	private int sampleUID = -1; //Selected sample
	
	/*----- Init -----*/
	
	/**
	 * @wbp.parser.constructor
	 */
	public ZeqerInstEditPanel(){
		initGUI(REGION_TYPE_LO);
	}
	
	public ZeqerInstEditPanel(int region_type){
		initGUI(region_type);
	}
	
	private void initGUI(int region_type){
		
		incl_enable = new ComponentGroup();
		
		boolean inclReg = (region_type != REGION_TYPE_MID);
		int height = !inclReg ? MIN_HEIGHT_NOREG:MIN_HEIGHT_REG;
		setMinimumSize(new Dimension(MIN_WIDTH, height));
		setPreferredSize(new Dimension(MIN_WIDTH, height));
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JPanel pnlReg = new JPanel();
		GridBagConstraints gbc_pnlReg = new GridBagConstraints();
		gbc_pnlReg.insets = new Insets(0, 0, 5, 0);
		gbc_pnlReg.fill = GridBagConstraints.BOTH;
		gbc_pnlReg.gridx = 0;
		gbc_pnlReg.gridy = 0;
		if(inclReg) add(pnlReg, gbc_pnlReg);
		else{
			//Put in a label, I guess
			JLabel lblMid = new JLabel("Regions");
			lblMid.setFont(new Font("Tahoma", Font.BOLD, 13));
			GridBagConstraints gbc_lblMid = new GridBagConstraints();
			gbc_lblMid.insets = new Insets(5, 5, 5, 5);
			gbc_lblMid.gridx = 0;
			gbc_lblMid.gridy = 0;
			add(lblMid, gbc_lblMid);
		}
		
		GridBagLayout gbl_pnlReg = new GridBagLayout();
		gbl_pnlReg.columnWidths = new int[]{0, 0, 0, 50, 0};
		gbl_pnlReg.rowHeights = new int[]{0, 0};
		gbl_pnlReg.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlReg.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlReg.setLayout(gbl_pnlReg);
		
		cbInclude = new JCheckBox("Include | ");
		GridBagConstraints gbc_cbInclude = new GridBagConstraints();
		gbc_cbInclude.insets = new Insets(5, 5, 5, 5);
		gbc_cbInclude.gridx = 0;
		gbc_cbInclude.gridy = 0;
		pnlReg.add(cbInclude, gbc_cbInclude);
		cbInclude.setSelected(!inclReg);
		cbInclude.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				cbIncludeCallback();
			}
		});
		//cbInclude.setFont(new Font("Tahoma", Font.ITALIC, 11));
		
		String notelabel = "INIT BUG";
		switch(region_type){
		case REGION_TYPE_LO:
			notelabel = "Max Note (MIDI):";
			break;
		case REGION_TYPE_HI:
			notelabel = "Min Note (MIDI):";
			break;
		default:
			notelabel = "Note Limit:";
			break;
		}
		JLabel lblMaxNotemidi = new JLabel(notelabel);
		GridBagConstraints gbc_lblMaxNotemidi = new GridBagConstraints();
		gbc_lblMaxNotemidi.insets = new Insets(0, 0, 0, 5);
		gbc_lblMaxNotemidi.gridx = 2;
		gbc_lblMaxNotemidi.gridy = 0;
		pnlReg.add(lblMaxNotemidi, gbc_lblMaxNotemidi);
		
		spnLimitNote = new JSpinner();
		incl_enable.addComponent("spnLimitNote", spnLimitNote);
		GridBagConstraints gbc_spnLimitNote = new GridBagConstraints();
		gbc_spnLimitNote.insets = new Insets(5, 0, 5, 5);
		gbc_spnLimitNote.fill = GridBagConstraints.BOTH;
		gbc_spnLimitNote.gridx = 3;
		gbc_spnLimitNote.gridy = 0;
		pnlReg.add(spnLimitNote, gbc_spnLimitNote);
		initKeySpinnerModel(spnLimitNote);
		switch(region_type){
		case REGION_TYPE_LO:
			spnLimitNote.setValue(0);
			break;
		case REGION_TYPE_HI:
			spnLimitNote.setValue(127);
			break;
		}
		spnLimitNote.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				if(noteRangeSpinnerCallback != null){
					noteRangeSpinnerCallback.doMethod();
				}
			}
		});
		
		//int gridy = inclReg?1:0;
		int gridy = 1;
		JPanel pnlSample = new JPanel();
		GridBagConstraints gbc_pnlSample = new GridBagConstraints();
		gbc_pnlSample.insets = new Insets(0, 0, 5, 0);
		gbc_pnlSample.fill = GridBagConstraints.BOTH;
		gbc_pnlSample.gridx = 0;
		gbc_pnlSample.gridy = gridy++;
		add(pnlSample, gbc_pnlSample);
		GridBagLayout gbl_pnlSample = new GridBagLayout();
		gbl_pnlSample.columnWidths = new int[]{0, 0, 0};
		gbl_pnlSample.rowHeights = new int[]{0, 0, 0};
		gbl_pnlSample.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlSample.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		pnlSample.setLayout(gbl_pnlSample);
		
		JLabel lblSample = new JLabel("Sample:");
		GridBagConstraints gbc_lblSample = new GridBagConstraints();
		gbc_lblSample.insets = new Insets(0, 5, 5, 5);
		gbc_lblSample.anchor = GridBagConstraints.EAST;
		gbc_lblSample.gridx = 0;
		gbc_lblSample.gridy = 0;
		pnlSample.add(lblSample, gbc_lblSample);
		
		txtSample = new JTextField();
		incl_enable.addComponent("txtSample", txtSample);
		GridBagConstraints gbc_txtSample = new GridBagConstraints();
		gbc_txtSample.insets = new Insets(0, 0, 5, 5);
		gbc_txtSample.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtSample.gridx = 1;
		gbc_txtSample.gridy = 0;
		pnlSample.add(txtSample, gbc_txtSample);
		txtSample.setColumns(10);
		
		JButton btnSample = new JButton("Set Sample...");
		incl_enable.addComponent("btnSample", btnSample);
		GridBagConstraints gbc_btnSample = new GridBagConstraints();
		gbc_btnSample.gridx = 1;
		gbc_btnSample.gridy = 1;
		pnlSample.add(btnSample, gbc_btnSample);
		btnSample.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnSetSampleCallback();
			}
		});
		
		JPanel pnlTune = new JPanel();
		GridBagConstraints gbc_pnlTune = new GridBagConstraints();
		gbc_pnlTune.fill = GridBagConstraints.BOTH;
		gbc_pnlTune.gridx = 0;
		gbc_pnlTune.gridy = gridy;
		add(pnlTune, gbc_pnlTune);
		GridBagLayout gbl_pnlTune = new GridBagLayout();
		gbl_pnlTune.columnWidths = new int[]{0, 50, 0, 0};
		gbl_pnlTune.rowHeights = new int[]{0, 0, 0};
		gbl_pnlTune.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlTune.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		pnlTune.setLayout(gbl_pnlTune);
		
		JLabel lblUnityKey = new JLabel("Unity Key (MIDI):");
		GridBagConstraints gbc_lblUnityKey = new GridBagConstraints();
		gbc_lblUnityKey.insets = new Insets(5, 0, 5, 5);
		gbc_lblUnityKey.gridx = 0;
		gbc_lblUnityKey.gridy = 0;
		pnlTune.add(lblUnityKey, gbc_lblUnityKey);
		
		spnUnityKey = new JSpinner();
		incl_enable.addComponent("spnUnityKey", spnUnityKey);
		GridBagConstraints gbc_spnUnityKey = new GridBagConstraints();
		gbc_spnUnityKey.anchor = GridBagConstraints.WEST;
		gbc_spnUnityKey.fill = GridBagConstraints.BOTH;
		gbc_spnUnityKey.insets = new Insets(0, 5, 5, 5);
		gbc_spnUnityKey.gridx = 1;
		gbc_spnUnityKey.gridy = 0;
		pnlTune.add(spnUnityKey, gbc_spnUnityKey);
		initKeySpinnerModel(spnUnityKey);
		
		JLabel lblFineTunecents = new JLabel("Fine Tune (Cents):");
		GridBagConstraints gbc_lblFineTunecents = new GridBagConstraints();
		gbc_lblFineTunecents.insets = new Insets(0, 5, 5, 5);
		gbc_lblFineTunecents.anchor = GridBagConstraints.EAST;
		gbc_lblFineTunecents.gridx = 0;
		gbc_lblFineTunecents.gridy = 1;
		pnlTune.add(lblFineTunecents, gbc_lblFineTunecents);
		
		txtFineTune = new JTextField();
		incl_enable.addComponent("txtFineTune", txtFineTune);
		GridBagConstraints gbc_txtFineTune = new GridBagConstraints();
		gbc_txtFineTune.anchor = GridBagConstraints.WEST;
		gbc_txtFineTune.insets = new Insets(0, 5, 5, 5);
		gbc_txtFineTune.gridx = 1;
		gbc_txtFineTune.gridy = 1;
		pnlTune.add(txtFineTune, gbc_txtFineTune);
		txtFineTune.setColumns(10);
		txtFineTune.setText("0");
		
		setEnabled();
	}
	
	private void initKeySpinnerModel(JSpinner spn){
		spn.setModel(new SpinnerNumberModel(60, 0, 127, 1));
	}
	
	/*----- Getters -----*/
	
	public boolean regionIncluded(){return cbInclude.isSelected();}
	public int getSelectedSampleUID(){return sampleUID;}
	public int getLimitNote(){return (Integer)spnLimitNote.getValue();}
	public int getUnityKey(){return (Integer)spnUnityKey.getValue();}
	
	public int getFineTune(){
		String ntext = txtFineTune.getText();
		if(ntext == null || ntext.isEmpty()){
			setFineTune(0);
			return 0;
		}
		
		try{
			int val = Integer.parseInt(txtFineTune.getText());
			if(val < -100){
				setFineTune(-100);
				return -100;
			}
			if(val > 100){
				setFineTune(100);
				return 100;
			}
			return val;
		}
		catch(NumberFormatException e){
			setFineTune(0);
			return 0;
		}
	}
	
	/*----- Setters -----*/
	
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
	
	public void setDisabled(){
		incl_enable.setEnabling(false);
		cbInclude.setEnabled(false);
		
		incl_enable.repaint();
		cbInclude.repaint();
	}
	
	public void setEnabled(){
		//All but cb is enabled based on cb value
		incl_enable.setEnabling(cbInclude.isSelected());
		cbInclude.setEnabled(true);
		
		incl_enable.repaint();
		cbInclude.repaint();
	}
	
	/*----- Callbacks -----*/
	
	private void cbIncludeCallback(){
		//Enables/disables other components
		setEnabled();
	}
	
	private void btnSetSampleCallback(){
		//TODO
		if(setSampleStartCallback != null) setSampleStartCallback.doMethod();
		
		if(setSampleDoneCallback != null) setSampleDoneCallback.doMethod();
	}
	
}
