package waffleoRai_zeqer64.GUI.dialogs;

import javax.swing.JDialog;
import javax.swing.JFrame;

import waffleoRai_zeqer64.GUI.dialogs.envedit.ZeqerEnvEditDialog;
import waffleoRai_zeqer64.presets.ZeqerInstPreset;
import java.awt.GridBagLayout;
import javax.swing.JPanel;

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
import waffleoRai_GUITools.RadioButtonGroup;
import waffleoRai_Utils.VoidCallbackMethod;
import waffleoRai_soundbank.nintendo.z64.Z64Envelope;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.border.EtchedBorder;

public class ZeqerInstEditDialog extends JDialog{

	private static final long serialVersionUID = 7053029408503859439L;
	
	public static final int DEFO_WIDTH = 700;
	public static final int DEFO_HEIGHT = 450;
	
	protected static final int RELRB_INDEX_MILLIS = 0;
	protected static final int RELRB_INDEX_RAW = 1;
	protected static final int RELRB_COUNT = 2;
	
	private static final int INSTLOAD_ERR_NONE = 0;
	private static final int INSTLOAD_INVALID_RELEASE = 1;
	private static final int INSTLOAD_INVALID_FINETUNE = 2;
	private static final int INSTLOAD_INVALID_SAMPLE = 3;
	
	/*----- Instance Variables -----*/
	
	private JFrame parent;
	
	private ComponentGroup globalEnable;
	private RadioButtonGroup rbgRelUnits;
	
	private JTextField txtEnum;
	private JTextField txtName;
	private JTextField txtRelease;
	
	private ZeqerInstEditPanel[] pnlRegions;
	
	private boolean exitSelection = false; //True if save, false if cancel
	private ZeqerInstPreset myInst;
	
	/*----- Init -----*/
	
	/**
	 * @wbp.parser.constructor
	 */
	public ZeqerInstEditDialog(JFrame parent_frame){
		super(parent_frame, true);
		parent = parent_frame;
		myInst = null;
		initGUI();
		loadInstrument(null);
	}
	
	public ZeqerInstEditDialog(JFrame parent_frame, ZeqerInstPreset inst){
		super(parent_frame, true);
		parent = parent_frame;
		myInst = null;
		initGUI();
		loadInstrument(myInst);
	}
	
	private void initGUI(){
		pnlRegions = new ZeqerInstEditPanel[3];
		globalEnable = new ComponentGroup();
		rbgRelUnits = new RadioButtonGroup(RELRB_COUNT);
		
		setMinimumSize(new Dimension(DEFO_WIDTH, DEFO_HEIGHT));
		setPreferredSize(new Dimension(DEFO_WIDTH, DEFO_HEIGHT));
		
		setTitle("Edit Instrument");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 100, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel pnlMeta = new JPanel();
		GridBagConstraints gbc_pnlMeta = new GridBagConstraints();
		gbc_pnlMeta.gridwidth = 3;
		gbc_pnlMeta.insets = new Insets(5, 5, 5, 5);
		gbc_pnlMeta.fill = GridBagConstraints.BOTH;
		gbc_pnlMeta.gridx = 0;
		gbc_pnlMeta.gridy = 0;
		getContentPane().add(pnlMeta, gbc_pnlMeta);
		GridBagLayout gbl_pnlMeta = new GridBagLayout();
		gbl_pnlMeta.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_pnlMeta.rowHeights = new int[]{0, 0};
		gbl_pnlMeta.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlMeta.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlMeta.setLayout(gbl_pnlMeta);
		
		JLabel lblName = new JLabel("Name:");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.insets = new Insets(0, 0, 0, 5);
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		pnlMeta.add(lblName, gbc_lblName);
		
		txtName = new JTextField();
		GridBagConstraints gbc_txtName = new GridBagConstraints();
		gbc_txtName.insets = new Insets(0, 0, 0, 5);
		gbc_txtName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtName.gridx = 1;
		gbc_txtName.gridy = 0;
		pnlMeta.add(txtName, gbc_txtName);
		txtName.setColumns(10);
		globalEnable.addComponent("txtName", txtName);
		
		JLabel lblEnumId = new JLabel("Enum ID:");
		GridBagConstraints gbc_lblEnumId = new GridBagConstraints();
		gbc_lblEnumId.insets = new Insets(0, 0, 0, 5);
		gbc_lblEnumId.anchor = GridBagConstraints.EAST;
		gbc_lblEnumId.gridx = 3;
		gbc_lblEnumId.gridy = 0;
		pnlMeta.add(lblEnumId, gbc_lblEnumId);
		
		txtEnum = new JTextField();
		GridBagConstraints gbc_txtEnum = new GridBagConstraints();
		gbc_txtEnum.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtEnum.gridx = 4;
		gbc_txtEnum.gridy = 0;
		pnlMeta.add(txtEnum, gbc_txtEnum);
		txtEnum.setColumns(10);
		globalEnable.addComponent("txtEnum", txtEnum);
		
		JPanel pnlEnv = new JPanel();
		GridBagConstraints gbc_pnlEnv = new GridBagConstraints();
		gbc_pnlEnv.gridwidth = 3;
		gbc_pnlEnv.insets = new Insets(0, 0, 5, 5);
		gbc_pnlEnv.fill = GridBagConstraints.BOTH;
		gbc_pnlEnv.gridx = 0;
		gbc_pnlEnv.gridy = 1;
		getContentPane().add(pnlEnv, gbc_pnlEnv);
		GridBagLayout gbl_pnlEnv = new GridBagLayout();
		gbl_pnlEnv.columnWidths = new int[]{0, 75, 0, 0, 0, 0, 0};
		gbl_pnlEnv.rowHeights = new int[]{0, 0};
		gbl_pnlEnv.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlEnv.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlEnv.setLayout(gbl_pnlEnv);
		
		JLabel lblRelease = new JLabel("Release:");
		GridBagConstraints gbc_lblRelease = new GridBagConstraints();
		gbc_lblRelease.anchor = GridBagConstraints.EAST;
		gbc_lblRelease.insets = new Insets(5, 5, 5, 5);
		gbc_lblRelease.gridx = 0;
		gbc_lblRelease.gridy = 0;
		pnlEnv.add(lblRelease, gbc_lblRelease);
		
		txtRelease = new JTextField();
		GridBagConstraints gbc_txtRelease = new GridBagConstraints();
		gbc_txtRelease.insets = new Insets(5, 5, 5, 5);
		gbc_txtRelease.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtRelease.gridx = 1;
		gbc_txtRelease.gridy = 0;
		pnlEnv.add(txtRelease, gbc_txtRelease);
		txtRelease.setColumns(10);
		globalEnable.addComponent("txtRelease", txtRelease);
		
		JRadioButton rbMillis = new JRadioButton("milliseconds");
		GridBagConstraints gbc_rbMillis = new GridBagConstraints();
		gbc_rbMillis.insets = new Insets(0, 0, 0, 5);
		gbc_rbMillis.gridx = 2;
		gbc_rbMillis.gridy = 0;
		pnlEnv.add(rbMillis, gbc_rbMillis);
		rbMillis.setSelected(true);
		rbgRelUnits.addButton(rbMillis, RELRB_INDEX_MILLIS);
		globalEnable.addComponent("rbMillis", rbMillis);
		rbMillis.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				releaseRBSelectCallback(RELRB_INDEX_MILLIS);
			}
		});
		
		JRadioButton rbRawRel = new JRadioButton("Raw Value");
		GridBagConstraints gbc_rbRawRel = new GridBagConstraints();
		gbc_rbRawRel.insets = new Insets(0, 0, 0, 5);
		gbc_rbRawRel.gridx = 3;
		gbc_rbRawRel.gridy = 0;
		pnlEnv.add(rbRawRel, gbc_rbRawRel);
		rbRawRel.setSelected(false);
		rbgRelUnits.addButton(rbRawRel, RELRB_INDEX_RAW);
		globalEnable.addComponent("rbRawRel", rbRawRel);
		rbRawRel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				releaseRBSelectCallback(RELRB_INDEX_RAW);
			}
		});
		
		JButton btnEditEnv = new JButton("Edit Envelope...");
		GridBagConstraints gbc_btnEditEnv = new GridBagConstraints();
		gbc_btnEditEnv.insets = new Insets(5, 5, 5, 5);
		gbc_btnEditEnv.gridx = 5;
		gbc_btnEditEnv.gridy = 0;
		pnlEnv.add(btnEditEnv, gbc_btnEditEnv);
		globalEnable.addComponent("btnEditEnv", btnEditEnv);
		btnEditEnv.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnEditEnvCallback();
			}
		});
		
		ZeqerInstEditPanel pnlLo = new ZeqerInstEditPanel(ZeqerInstEditPanel.REGION_TYPE_LO);
		pnlLo.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlLo = new GridBagConstraints();
		gbc_pnlLo.insets = new Insets(0, 0, 5, 5);
		gbc_pnlLo.fill = GridBagConstraints.BOTH;
		gbc_pnlLo.gridx = 0;
		gbc_pnlLo.gridy = 2;
		getContentPane().add(pnlLo, gbc_pnlLo);
		pnlRegions[0] = pnlLo;
		
		ZeqerInstEditPanel pnlMid = new ZeqerInstEditPanel(ZeqerInstEditPanel.REGION_TYPE_MID);
		pnlMid.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlMid = new GridBagConstraints();
		gbc_pnlMid.insets = new Insets(0, 0, 5, 5);
		gbc_pnlMid.fill = GridBagConstraints.BOTH;
		gbc_pnlMid.gridx = 1;
		gbc_pnlMid.gridy = 2;
		getContentPane().add(pnlMid, gbc_pnlMid);
		pnlRegions[1] = pnlMid;
		
		ZeqerInstEditPanel pnlHi = new ZeqerInstEditPanel(ZeqerInstEditPanel.REGION_TYPE_HI);
		pnlHi.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlHi = new GridBagConstraints();
		gbc_pnlHi.insets = new Insets(0, 0, 5, 0);
		gbc_pnlHi.fill = GridBagConstraints.BOTH;
		gbc_pnlHi.gridx = 2;
		gbc_pnlHi.gridy = 2;
		getContentPane().add(pnlHi, gbc_pnlHi);
		pnlRegions[2] = pnlHi;
		
		JPanel pnlPlay = new JPanel();
		GridBagConstraints gbc_pnlPlay = new GridBagConstraints();
		gbc_pnlPlay.gridwidth = 3;
		gbc_pnlPlay.insets = new Insets(0, 0, 5, 5);
		gbc_pnlPlay.fill = GridBagConstraints.BOTH;
		gbc_pnlPlay.gridx = 0;
		gbc_pnlPlay.gridy = 3;
		getContentPane().add(pnlPlay, gbc_pnlPlay);
		
		JPanel pnlButtons = new JPanel();
		GridBagConstraints gbc_pnlButtons = new GridBagConstraints();
		gbc_pnlButtons.insets = new Insets(0, 0, 0, 5);
		gbc_pnlButtons.gridwidth = 3;
		gbc_pnlButtons.fill = GridBagConstraints.BOTH;
		gbc_pnlButtons.gridx = 0;
		gbc_pnlButtons.gridy = 4;
		getContentPane().add(pnlButtons, gbc_pnlButtons);
		GridBagLayout gbl_pnlButtons = new GridBagLayout();
		gbl_pnlButtons.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_pnlButtons.rowHeights = new int[]{0, 0};
		gbl_pnlButtons.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlButtons.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlButtons.setLayout(gbl_pnlButtons);
		
		JButton btnEditTags = new JButton("Edit Tags...");
		GridBagConstraints gbc_btnEditTags = new GridBagConstraints();
		gbc_btnEditTags.insets = new Insets(5, 5, 5, 5);
		gbc_btnEditTags.gridx = 0;
		gbc_btnEditTags.gridy = 0;
		pnlButtons.add(btnEditTags, gbc_btnEditTags);
		globalEnable.addComponent("btnEditTags", btnEditTags);
		btnEditTags.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnEditTagsCallback();
			}
		});
		
		JButton btnSave = new JButton("Save");
		GridBagConstraints gbc_btnSave = new GridBagConstraints();
		gbc_btnSave.insets = new Insets(5, 5, 0, 5);
		gbc_btnSave.gridx = 2;
		gbc_btnSave.gridy = 0;
		pnlButtons.add(btnSave, gbc_btnSave);
		globalEnable.addComponent("btnSave", btnSave);
		btnSave.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnSaveCallback();
			}
		});
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(5, 5, 0, 0);
		gbc_btnCancel.gridx = 3;
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
	
	/*----- Setters -----*/
	
	public void loadInstrument(ZeqerInstPreset ipreset){
		//TODO
	}
	
	/*----- GUI Management -----*/
	
	private int updateInstrument(){
		//TODO
		//Returns an error code
		
		
		
		return INSTLOAD_ERR_NONE;
	}
	
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
		globalEnable.repaint();
		
		for(int i = 0; i < 3; i++){
			this.pnlRegions[i].setEnabled();
		}
		
		super.setCursor(null);
	}
	
	public void closeMe(){
		this.setVisible(false);
		this.dispose();
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
		
		int errcode = updateInstrument();
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
		}
		
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
		if(myInst != null) dialog.loadTags(myInst.getAllTags());
		else myInst = new ZeqerInstPreset();
		
		dialog.setVisible(true);
		if(dialog.getExitSelection()){
			List<String> newtags = dialog.getTags();
			myInst.clearTags();
			for(String s : newtags) myInst.addTag(s);
		}
		
		unsetWait();
	}
	
	private void btnEditEnvCallback(){
		setWait();
		ZeqerEnvEditDialog dialog = new ZeqerEnvEditDialog(parent);
		if(myInst != null) dialog.loadEnvelope(myInst.getEnvelope());
		else myInst = new ZeqerInstPreset();
		
		dialog.setVisible(true);
		Z64Envelope result = dialog.getOutputEnvelope();
		if(result != null){
			myInst.setEnvelope(result);
		}
		
		unsetWait();
	}
	
	private void releaseRBSelectCallback(int idx){
		rbgRelUnits.select(idx);
	}

}
