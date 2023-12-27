package waffleoRai_zeqer64.GUI.dialogs;

import javax.swing.JDialog;
import javax.swing.JFrame;

import waffleoRai_zeqer64.ZeqerUtils;
import waffleoRai_zeqer64.GUI.dialogs.envedit.ZeqerEnvEditDialog;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;
import waffleoRai_zeqer64.presets.ZeqerInstPreset;
import java.awt.GridBagLayout;
import javax.swing.JPanel;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JTextField;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.GUITools;
import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.VoidCallbackMethod;
import waffleoRai_soundbank.nintendo.z64.Z64Envelope;
import waffleoRai_soundbank.nintendo.z64.Z64Instrument;

import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;
import java.awt.Font;

public class ZeqerInstEditDialog extends JDialog{
	
	private static final long serialVersionUID = 7053029408503859439L;
	
	public static final int DEFO_WIDTH = 700;
	public static final int DEFO_HEIGHT = 400;
	
	public static final int INSTLOAD_ERR_NONE = 0;
	public static final int INSTLOAD_INVALID_RELEASE = 1;
	public static final int INSTLOAD_INVALID_FINETUNE = 2;
	public static final int INSTLOAD_INVALID_SAMPLE = 3;
	public static final int INSTLOAD_NULL_INST = 4;
	
	/*----- Instance Variables -----*/
	
	private JFrame parent;
	private ZeqerCoreInterface core;
	
	private ComponentGroup globalEnable;
	private ComponentGroup cgEditable;
	
	private JTextField txtEnum;
	private JTextField txtName;
	private JLabel lblRelease;
	
	private ZeqerInstEditPanel[] pnlRegions;
	
	private boolean exitSelection = false; //True if save, false if cancel
	private boolean editable = true;
	//private ZeqerInstPreset myInst;
	private int tempRel = 0;
	private Z64Envelope tempEnv = null;
	private List<String> tempTags = null;
	
	/*----- Init -----*/
	
	/**
	 * @wbp.parser.constructor
	 */
	public ZeqerInstEditDialog(JFrame parent_frame, ZeqerCoreInterface core_iface){
		super(parent_frame, true);
		parent = parent_frame;
		core = core_iface;
		//myInst = null;
		initGUI();
		loadInstrument(null);
	}
	
	public ZeqerInstEditDialog(JFrame parent_frame, ZeqerCoreInterface core_iface, ZeqerInstPreset inst){
		super(parent_frame, true);
		parent = parent_frame;
		core = core_iface;
		//myInst = inst;
		initGUI();
		loadInstrument(inst);
	}
	
	private void initGUI(){
		pnlRegions = new ZeqerInstEditPanel[3];
		globalEnable = new ComponentGroup();
		cgEditable = globalEnable.newChild();
		
		setMinimumSize(new Dimension(DEFO_WIDTH, DEFO_HEIGHT));
		setPreferredSize(new Dimension(DEFO_WIDTH, DEFO_HEIGHT));
		setTitle("Edit Instrument");
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel pnlMeta = new JPanel();
		GridBagConstraints gbc_pnlMeta = new GridBagConstraints();
		gbc_pnlMeta.gridwidth = 3;
		gbc_pnlMeta.insets = new Insets(0, 0, 5, 0);
		gbc_pnlMeta.fill = GridBagConstraints.BOTH;
		gbc_pnlMeta.gridx = 0;
		gbc_pnlMeta.gridy = 0;
		getContentPane().add(pnlMeta, gbc_pnlMeta);
		GridBagLayout gbl_pnlMeta = new GridBagLayout();
		gbl_pnlMeta.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_pnlMeta.rowHeights = new int[]{0, 0};
		gbl_pnlMeta.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlMeta.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlMeta.setLayout(gbl_pnlMeta);
		
		JLabel lblName = new JLabel("Name:");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.insets = new Insets(5, 5, 0, 5);
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		pnlMeta.add(lblName, gbc_lblName);
		
		txtName = new JTextField();
		GridBagConstraints gbc_txtA = new GridBagConstraints();
		gbc_txtA.insets = new Insets(5, 5, 0, 5);
		gbc_txtA.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtA.gridx = 1;
		gbc_txtA.gridy = 0;
		pnlMeta.add(txtName, gbc_txtA);
		txtName.setColumns(10);
		globalEnable.addComponent("txtName", txtName);
		
		JLabel lblEnum = new JLabel("Enum:");
		GridBagConstraints gbc_lblEnum = new GridBagConstraints();
		gbc_lblEnum.insets = new Insets(5, 0, 0, 5);
		gbc_lblEnum.anchor = GridBagConstraints.EAST;
		gbc_lblEnum.gridx = 3;
		gbc_lblEnum.gridy = 0;
		pnlMeta.add(lblEnum, gbc_lblEnum);
		
		txtEnum = new JTextField();
		GridBagConstraints gbc_txtB = new GridBagConstraints();
		gbc_txtB.insets = new Insets(5, 5, 0, 5);
		gbc_txtB.anchor = GridBagConstraints.NORTH;
		gbc_txtB.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtB.gridx = 4;
		gbc_txtB.gridy = 0;
		pnlMeta.add(txtEnum, gbc_txtB);
		txtEnum.setColumns(10);
		globalEnable.addComponent("txtEnum", txtEnum);
		
		JButton btnEditTags = new JButton("Edit Tags...");
		GridBagConstraints gbc_btnEditTags = new GridBagConstraints();
		gbc_btnEditTags.insets = new Insets(5, 5, 0, 5);
		gbc_btnEditTags.gridx = 5;
		gbc_btnEditTags.gridy = 0;
		pnlMeta.add(btnEditTags, gbc_btnEditTags);
		globalEnable.addComponent("btnEditTags", btnEditTags);
		btnEditTags.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnEditTagsCallback();
			}
		});
		
		JPanel pnlEnv = new JPanel();
		GridBagConstraints gbc_pnlEnv = new GridBagConstraints();
		gbc_pnlEnv.gridwidth = 3;
		gbc_pnlEnv.insets = new Insets(0, 0, 5, 0);
		gbc_pnlEnv.fill = GridBagConstraints.BOTH;
		gbc_pnlEnv.gridx = 0;
		gbc_pnlEnv.gridy = 1;
		getContentPane().add(pnlEnv, gbc_pnlEnv);
		GridBagLayout gbl_pnlEnv = new GridBagLayout();
		gbl_pnlEnv.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_pnlEnv.rowHeights = new int[]{0, 0};
		gbl_pnlEnv.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlEnv.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		pnlEnv.setLayout(gbl_pnlEnv);
		
		lblRelease = new JLabel("Release: [0] 0 ms NTSC / 0 ms PAL");
		GridBagConstraints gbc_lblRelease = new GridBagConstraints();
		gbc_lblRelease.anchor = GridBagConstraints.WEST;
		gbc_lblRelease.insets = new Insets(0, 5, 0, 20);
		gbc_lblRelease.gridx = 0;
		gbc_lblRelease.gridy = 0;
		pnlEnv.add(lblRelease, gbc_lblRelease);
		
		JButton btnEditRelease = new JButton("Edit Release...");
		GridBagConstraints gbc_btnEditRelease = new GridBagConstraints();
		gbc_btnEditRelease.insets = new Insets(0, 0, 0, 5);
		gbc_btnEditRelease.gridx = 1;
		gbc_btnEditRelease.gridy = 0;
		pnlEnv.add(btnEditRelease, gbc_btnEditRelease);
		cgEditable.addComponent("btnEditRelease", btnEditRelease);
		btnEditRelease.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnEditRelCallback();
			}
		});
		
		JButton btnEditEnvelope = new JButton("Edit Envelope...");
		GridBagConstraints gbc_btnEditEnvelope = new GridBagConstraints();
		gbc_btnEditEnvelope.insets = new Insets(0, 0, 0, 10);
		gbc_btnEditEnvelope.gridx = 3;
		gbc_btnEditEnvelope.gridy = 0;
		pnlEnv.add(btnEditEnvelope, gbc_btnEditEnvelope);
		globalEnable.addComponent("btnEditEnvelope", btnEditEnvelope);
		btnEditEnvelope.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnEditEnvCallback();
			}
		});
		
		JPanel pnlRLo = new JPanel();
		pnlRLo.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlRLo = new GridBagConstraints();
		gbc_pnlRLo.insets = new Insets(0, 0, 5, 5);
		gbc_pnlRLo.fill = GridBagConstraints.BOTH;
		gbc_pnlRLo.gridx = 0;
		gbc_pnlRLo.gridy = 2;
		getContentPane().add(pnlRLo, gbc_pnlRLo);
		GridBagLayout gbl_pnlRLo = new GridBagLayout();
		gbl_pnlRLo.columnWidths = new int[]{0, 0};
		gbl_pnlRLo.rowHeights = new int[]{0, 0, 0};
		gbl_pnlRLo.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlRLo.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		pnlRLo.setLayout(gbl_pnlRLo);
		
		JLabel lblLowRegion = new JLabel("Low Region");
		lblLowRegion.setFont(new Font("Tahoma", Font.BOLD, 13));
		GridBagConstraints gbc_lblLowRegion = new GridBagConstraints();
		gbc_lblLowRegion.insets = new Insets(0, 0, 5, 0);
		gbc_lblLowRegion.gridx = 0;
		gbc_lblLowRegion.gridy = 0;
		pnlRLo.add(lblLowRegion, gbc_lblLowRegion);
		
		ZeqerInstEditPanel pnlReg0 = new ZeqerInstEditPanel(parent, core, ZeqerInstEditPanel.REGION_TYPE_LO);
		GridBagConstraints gbc_pnlReg0 = new GridBagConstraints();
		gbc_pnlReg0.fill = GridBagConstraints.BOTH;
		gbc_pnlReg0.gridx = 0;
		gbc_pnlReg0.gridy = 1;
		pnlRLo.add(pnlReg0, gbc_pnlReg0);
		pnlRegions[0] = pnlReg0;
		
		JPanel pnlRMid = new JPanel();
		pnlRMid.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlRMid = new GridBagConstraints();
		gbc_pnlRMid.insets = new Insets(0, 0, 5, 5);
		gbc_pnlRMid.fill = GridBagConstraints.BOTH;
		gbc_pnlRMid.gridx = 1;
		gbc_pnlRMid.gridy = 2;
		getContentPane().add(pnlRMid, gbc_pnlRMid);
		GridBagLayout gbl_pnlRMid = new GridBagLayout();
		gbl_pnlRMid.columnWidths = new int[]{0, 0};
		gbl_pnlRMid.rowHeights = new int[]{0, 0, 0};
		gbl_pnlRMid.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlRMid.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		pnlRMid.setLayout(gbl_pnlRMid);
		
		JLabel lblMainRegion = new JLabel("Main Region");
		lblMainRegion.setFont(new Font("Tahoma", Font.BOLD, 13));
		GridBagConstraints gbc_lblMainRegion = new GridBagConstraints();
		gbc_lblMainRegion.insets = new Insets(0, 0, 5, 0);
		gbc_lblMainRegion.gridx = 0;
		gbc_lblMainRegion.gridy = 0;
		pnlRMid.add(lblMainRegion, gbc_lblMainRegion);
		
		ZeqerInstEditPanel pnlReg1 = new ZeqerInstEditPanel(parent, core, ZeqerInstEditPanel.REGION_TYPE_MID);
		GridBagConstraints gbc_pnlReg1 = new GridBagConstraints();
		gbc_pnlReg1.fill = GridBagConstraints.BOTH;
		gbc_pnlReg1.gridx = 0;
		gbc_pnlReg1.gridy = 1;
		pnlRMid.add(pnlReg1, gbc_pnlReg1);
		pnlRegions[1] = pnlReg1;
		
		JPanel pnlRHi = new JPanel();
		pnlRHi.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlRHi = new GridBagConstraints();
		gbc_pnlRHi.insets = new Insets(0, 0, 5, 0);
		gbc_pnlRHi.fill = GridBagConstraints.BOTH;
		gbc_pnlRHi.gridx = 2;
		gbc_pnlRHi.gridy = 2;
		getContentPane().add(pnlRHi, gbc_pnlRHi);
		GridBagLayout gbl_pnlRHi = new GridBagLayout();
		gbl_pnlRHi.columnWidths = new int[]{0, 0};
		gbl_pnlRHi.rowHeights = new int[]{0, 0, 0};
		gbl_pnlRHi.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlRHi.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		pnlRHi.setLayout(gbl_pnlRHi);
		
		JLabel lblHighRegion = new JLabel("High Region");
		lblHighRegion.setFont(new Font("Tahoma", Font.BOLD, 13));
		GridBagConstraints gbc_lblHighRegion = new GridBagConstraints();
		gbc_lblHighRegion.insets = new Insets(0, 0, 5, 0);
		gbc_lblHighRegion.gridx = 0;
		gbc_lblHighRegion.gridy = 0;
		pnlRHi.add(lblHighRegion, gbc_lblHighRegion);
		
		ZeqerInstEditPanel pnlReg2 = new ZeqerInstEditPanel(parent, core, ZeqerInstEditPanel.REGION_TYPE_HI);
		GridBagConstraints gbc_pnlReg2 = new GridBagConstraints();
		gbc_pnlReg2.fill = GridBagConstraints.BOTH;
		gbc_pnlReg2.gridx = 0;
		gbc_pnlReg2.gridy = 1;
		pnlRHi.add(pnlReg2, gbc_pnlReg2);
		pnlRegions[2] = pnlReg2;
		
		JPanel pnlPlay = new JPanel();
		GridBagConstraints gbc_pnlPlay = new GridBagConstraints();
		gbc_pnlPlay.gridwidth = 3;
		gbc_pnlPlay.insets = new Insets(0, 0, 5, 0);
		gbc_pnlPlay.fill = GridBagConstraints.BOTH;
		gbc_pnlPlay.gridx = 0;
		gbc_pnlPlay.gridy = 3;
		getContentPane().add(pnlPlay, gbc_pnlPlay);
		
		JPanel pnlButtons = new JPanel();
		GridBagConstraints gbc_pnlButtons = new GridBagConstraints();
		gbc_pnlButtons.gridwidth = 3;
		gbc_pnlButtons.fill = GridBagConstraints.BOTH;
		gbc_pnlButtons.gridx = 0;
		gbc_pnlButtons.gridy = 4;
		getContentPane().add(pnlButtons, gbc_pnlButtons);
		GridBagLayout gbl_pnlButtons = new GridBagLayout();
		gbl_pnlButtons.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlButtons.rowHeights = new int[]{0, 0};
		gbl_pnlButtons.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlButtons.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlButtons.setLayout(gbl_pnlButtons);
		
		JButton btnOkay = new JButton("Okay");
		GridBagConstraints gbc_btnOkay = new GridBagConstraints();
		gbc_btnOkay.insets = new Insets(0, 0, 5, 5);
		gbc_btnOkay.gridx = 1;
		gbc_btnOkay.gridy = 0;
		pnlButtons.add(btnOkay, gbc_btnOkay);
		cgEditable.addComponent("btnOkay", btnOkay);
		btnOkay.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnSaveCallback();
			}
		});
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(0, 0, 5, 5);
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 0;
		pnlButtons.add(btnCancel, gbc_btnCancel);
		globalEnable.addComponent("btnCancel", btnCancel);
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnCancelCallback();
			}
		});
		
		setRegionPanelCallbacks();
	}
	
	private void setRegionPanelCallbacks(){
		for(int i = 0; i < 3; i++){
			pnlRegions[i].setSampleSetStartCallback(new SetSampleStartCallback());
			pnlRegions[i].setSampleSetDoneCallback(new SetSampleDoneCallback());
		}
		pnlRegions[0].setNoteLimitSpinnerCallback(new LoLimitSetCallback());
		pnlRegions[2].setNoteLimitSpinnerCallback(new HiLimitSetCallback());
	}
	
	/*----- Getters -----*/
	
	public boolean getExitSelection(){return exitSelection;}
		
	public int loadDataToInstrument(ZeqerInstPreset ipreset){
		if(ipreset == null) return INSTLOAD_NULL_INST;
		
		Z64Instrument instdat = ipreset.getInstrument();
		if(instdat == null) return INSTLOAD_NULL_INST;
		
		instdat.setDecay((byte)tempRel);
		
		//Regions
		for(int i = 0; i < 3; i++){
			if(!pnlRegions[i].regionIncluded()){
				//Clear region in instrument.
				if(i == 0){
					ipreset.setWaveIDLo(0);
					instdat.setSampleLow(null);
					instdat.setTuningLow(1.0f);
				}
				else if(i == 2){
					ipreset.setWaveIDHi(0);
					instdat.setSampleHigh(null);
					instdat.setTuningHigh(1.0f);
				}
			}
			else{
				//Calculate tuning
				byte rootNote = (byte)pnlRegions[i].getUnityKey();
				byte fineTune = (byte)pnlRegions[i].getFineTune();
				float tune = Z64Sound.calculateTuning(Z64Sound.MIDDLE_C_S8, rootNote, fineTune);
				
				//Check wave ID
				Z64WaveInfo sample = pnlRegions[i].getSelectedSample();
				if(sample == null){
					//Invalid
					return INSTLOAD_INVALID_SAMPLE;
				}
				
				switch(i){
				case 0:
					ipreset.getInstrument().setSampleLow(sample);
					ipreset.setWaveIDLo(sample.getUID());
					instdat.setTuningLow(tune);
					instdat.setLowRangeTop((byte)pnlRegions[i].getLimitNote());
					break;
				case 1:
					ipreset.getInstrument().setSampleMiddle(sample);
					ipreset.setWaveIDMid(sample.getUID());
					instdat.setTuningMiddle(tune);
					break;
				case 2:
					ipreset.getInstrument().setSampleHigh(sample);
					ipreset.setWaveIDHi(sample.getUID());
					instdat.setTuningHigh(tune);
					instdat.setHighRangeBottom((byte)pnlRegions[i].getLimitNote());
					break;
				}
			}
		}
		
		String txt = txtName.getText();
		if(txt == null || txt.isEmpty()){
			txt = "Untitled Instrument";
			txtName.setText(txt);
			txtName.repaint();
		}
		ipreset.setName(txt);
		
		txt = txtEnum.getText();
		if(txt == null || txt.isEmpty()){
			txt = "UNKINST_PRESET_" + String.format("%08x", ipreset.hashToUID());
		}
		//Also make sure it is valid (All caps, no C reserved chars)
		txt = ZeqerUtils.fixHeaderEnumID(txt, true);
		txtEnum.setText(txt);
		txtEnum.repaint();
		ipreset.setEnumLabel(txt);
		
		ipreset.setEnvelope(tempEnv);
		
		//Tags
		ipreset.clearTags();
		if(tempTags != null){
			for(String tag : tempTags){
				ipreset.addTag(tag);
			}
		}
		
		return INSTLOAD_ERR_NONE;
	}
	
	/*----- Setters -----*/
	
	public void setEditable(boolean b){
		editable = b;
		for(int i = 0; i < 3; i++){
			this.pnlRegions[i].setEditable(b);
		}
		unsetWait();
	}
	
	private void clearInstrument(){
		setWait();
		txtName.setText("<No instrument>");
		txtEnum.setText("<No instrument>");
		tempRel = 0;
		
		for(int i = 0; i < 3; i++){
			if (i != 1){
				pnlRegions[i].setLimitNote((byte)0);
				pnlRegions[i].clearSample();
			}
			pnlRegions[i].setUnityKey(Z64Sound.MIDDLE_C_S8);
			pnlRegions[i].setFineTune(0);
			pnlRegions[i].setRegionIncluded(false);
		}
		
		unsetWait();
	}
	
	public void loadInstrument(ZeqerInstPreset ipreset){
		if(ipreset == null){
			clearInstrument();
			return;
		}
		//myInst = ipreset;
		
		setWait();
		txtName.setText(ipreset.getName());
		txtEnum.setText(ipreset.getEnumLabel());
		
		tempRel = Byte.toUnsignedInt(ipreset.getInstrument().getDecay());
		
		tempEnv = ipreset.getEnvelope().copy();
		tempTags = ipreset.getAllTags();
		
		//Low region
		int sid = ipreset.getWaveIDLo();
		if(sid == 0 || sid == -1){
			//Blank.
			pnlRegions[0].setLimitNote((byte)0);
			pnlRegions[0].clearSample();
			pnlRegions[0].setUnityKey(Z64Sound.MIDDLE_C_S8);
			pnlRegions[0].setFineTune(0);
			pnlRegions[0].setRegionIncluded(false);
		}
		else{
			pnlRegions[0].setRegionIncluded(true);
			//TODO Uh oh, we need a sample link... Assume it is in instr then?
			pnlRegions[0].setSample(ipreset.getInstrument().getSampleLow());
			pnlRegions[0].setLimitNote(ipreset.getInstrument().getLowRangeTop());
			
			//Calculate tuning
			float tune = ipreset.getInstrument().getTuningLow();
			Z64Sound.Z64Tuning miditune = Z64Sound.calculateTuning(Z64Sound.MIDDLE_C_S8, tune);
			pnlRegions[0].setUnityKey(miditune.root_key);
			pnlRegions[0].setFineTune(miditune.fine_tune);
		}
		
		//Middle region
		sid = ipreset.getWaveIDMid();
		pnlRegions[1].setRegionIncluded(true);
		if(sid == 0 || sid == -1){
			//Blank. This is... not great.
			pnlRegions[1].clearSample();
			pnlRegions[1].setUnityKey(Z64Sound.MIDDLE_C_S8);
			pnlRegions[1].setFineTune(0);
		}
		else{
			pnlRegions[1].setSample(ipreset.getInstrument().getSampleMiddle());
			
			//Calculate tuning
			float tune = ipreset.getInstrument().getTuningMiddle();
			Z64Sound.Z64Tuning miditune = Z64Sound.calculateTuning(Z64Sound.MIDDLE_C_S8, tune);
			pnlRegions[1].setUnityKey(miditune.root_key);
			pnlRegions[1].setFineTune(miditune.fine_tune);
		}
		
		//High region
		sid = ipreset.getWaveIDHi();
		if(sid == 0 || sid == -1){
			//Blank.
			pnlRegions[2].setLimitNote((byte)0);
			pnlRegions[2].clearSample();
			pnlRegions[2].setUnityKey(Z64Sound.MIDDLE_C_S8);
			pnlRegions[2].setFineTune(0);
			pnlRegions[2].setRegionIncluded(false);
		}
		else{
			pnlRegions[2].setRegionIncluded(true);
			pnlRegions[2].setSample(ipreset.getInstrument().getSampleHigh());
			pnlRegions[2].setLimitNote(ipreset.getInstrument().getHighRangeBottom());
			
			//Calculate tuning
			float tune = ipreset.getInstrument().getTuningHigh();
			Z64Sound.Z64Tuning miditune = Z64Sound.calculateTuning(Z64Sound.MIDDLE_C_S8, tune);
			pnlRegions[2].setUnityKey(miditune.root_key);
			pnlRegions[2].setFineTune(miditune.fine_tune);
		}
		
		unsetWait();
	}
	
	/*----- GUI Management -----*/

	public void setWait(){
		globalEnable.setEnabling(false);
		globalEnable.repaint();
		
		for(int i = 0; i < 3; i++){
			this.pnlRegions[i].setDisabled();
		}
		
		super.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	public void unsetWait(){
		globalEnable.setEnabling(true);
		
		updateReleaseAmtLabel(tempRel);
		
		for(int i = 0; i < 3; i++){
			this.pnlRegions[i].setEnabled();
		}
		
		cgEditable.setEnabling(editable);
		globalEnable.repaint();
		
		super.setCursor(null);
	}
	
	private void updateReleaseAmtLabel(int val){
		String relamtstr = "Release: [" + val + "] ";
		
		if(val > 0){
			if(val <= 255){
				int millis = Z64Sound.releaseValueToMillis_NTSC(val);
				relamtstr += millis + " ms NTSC / ";
				millis = Z64Sound.releaseValueToMillis_PAL(val);
				relamtstr += millis + " ms PAL";
			}
			else relamtstr += "Invalid";
		}
		else{
			if(val == 0) relamtstr += "N/A";
			else relamtstr += "Invalid";
		}
		
		lblRelease.setText(relamtstr);
		lblRelease.repaint();
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
	
	private class SetSampleStartCallback implements VoidCallbackMethod{
		public void doMethod() {setWait();}
	}
	
	private class SetSampleDoneCallback implements VoidCallbackMethod{
		public void doMethod() {unsetWait();}
	}
	
	private class LoLimitSetCallback implements VoidCallbackMethod{
		public void doMethod() {
			int noteLo = pnlRegions[0].getLimitNote();
			int noteHi = pnlRegions[2].getLimitNote();
			if(noteLo >= noteHi){
				if(noteLo >= 125){
					//Push down low note
					noteLo = Math.min(noteHi - 1, 125);
					pnlRegions[0].setLimitNote((byte)noteLo);
				}
				else{
					//Push up high note
					pnlRegions[2].setLimitNote((byte)(noteLo+1));
				}
			}
		}
	}
	
	private class HiLimitSetCallback implements VoidCallbackMethod{
		public void doMethod() {
			int noteLo = pnlRegions[0].getLimitNote();
			int noteHi = pnlRegions[2].getLimitNote();
			if(noteHi <= noteLo){
				if(noteHi <= 2){
					//Push up hi
					noteHi = Math.max(noteLo + 1, 2);
					pnlRegions[2].setLimitNote((byte)noteHi);
				}
				else{
					//Push down lo
					pnlRegions[0].setLimitNote((byte)(noteHi-1));
				}
			}
		}
	}
	
	private void btnSaveCallback(){
		setWait();
		
		/*int errcode = updateInstrument();
		if(errcode != INSTLOAD_ERR_NONE){
			String errmsg = "Internal error! Could not save instrument!";
			switch(errcode){
			case INSTLOAD_INVALID_RELEASE:
				errmsg = "Release value is invalid! Must be a positive integer.";
				break;
			case INSTLOAD_INVALID_FINETUNE:
				errmsg = "One or more fine tune values are invalid.\n"
						+ "Must be an integer between -100 and 100.";
				break;
			case INSTLOAD_INVALID_SAMPLE:
				errmsg = "One or more included regions does not have a valid sample.";
				break;
			}
			
			JOptionPane.showMessageDialog(this, errmsg, "Failed Export", JOptionPane.ERROR_MESSAGE);
			unsetWait();
			return;
		}*/
		
		exitSelection = true;
		unsetWait();
		closeMe();
	}
	
	private void btnCancelCallback(){
		setWait();
		exitSelection = false;
		unsetWait();
		closeMe();
	}
	
	private void btnEditTagsCallback(){
		setWait();
		ZeqerTagEditDialog dialog = new ZeqerTagEditDialog(parent);
		if(tempTags != null) dialog.loadTags(tempTags);

		dialog.showMe(this);
		if(dialog.getExitSelection()){
			tempTags = dialog.getTags();
		}
		
		unsetWait();
	}
	
	private void btnEditEnvCallback(){
		setWait();
		ZeqerEnvEditDialog dialog = new ZeqerEnvEditDialog(parent, core);
		if(tempEnv != null) dialog.loadEnvelope(tempEnv);
		else tempEnv = Z64Envelope.newDefaultEnvelope();
		
		dialog.showMe(this);
		Z64Envelope result = dialog.getOutputEnvelope();
		if(result != null){
			tempEnv = result;
		}
		
		unsetWait();
	}

	private void btnEditRelCallback(){
		setWait();
		
		StandaloneReleaseEditDialog dialog = new StandaloneReleaseEditDialog(parent, core);
		dialog.setReleaseValue(tempRel);
		
		dialog.showMe(this);
		
		if(dialog.getExitSelection()){
			tempRel = dialog.getReleaseValue();
		}
		unsetWait();
	}
	
}
