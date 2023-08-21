package waffleoRai_zeqer64.GUI.dialogs;

import javax.swing.JDialog;

import java.awt.GridBagLayout;
import javax.swing.JPanel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_soundbank.nintendo.z64.Z64Drum;
import waffleoRai_soundbank.nintendo.z64.Z64Envelope;
import waffleoRai_zeqer64.ZeqerUtils;
import waffleoRai_zeqer64.GUI.ZeqerGUIUtils;
import waffleoRai_zeqer64.GUI.dialogs.envedit.ZeqerEnvEditDialog;
import waffleoRai_zeqer64.GUI.smallElements.ReleaseEditPanel;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;
import waffleoRai_zeqer64.presets.ZeqerDrumPreset;

import javax.swing.JSpinner;
import javax.swing.JSlider;
import javax.swing.border.BevelBorder;

public class ZeqerDrumEditDialog extends JDialog{
	
	//TODO Oh crap, do we want a play/preview panel/interface of some kind?

	/*---- Constants: String Keys -----*/
	
	/*---- Constants -----*/
	
	private static final long serialVersionUID = -5160984883786336012L;
	
	public static final int DEFO_WIDTH = 440;
	public static final int DEFO_HEIGHT = 430;

	public static final int MIN_NOTE_DRUM = Z64Sound.STDRANGE_BOTTOM;
	public static final int MAX_NOTE_DRUM = MIN_NOTE_DRUM + Z64Sound.STDRANGE_SIZE - 1;
	public static final int MIDDLE_C = 60;
	
	/*----- Instance Variables -----*/
	
	private ComponentGroup globalEnable;
	
	private Frame parent;
	private ReleaseEditPanel pnlRel;
	
	private JTextField txtName;
	private JTextField txtEnum;
	private JTextField txtSample;
	private JSpinner spnUnityKey;
	private JSlider sldFineTune;
	private JLabel lblFineAmt;
	private JSlider sldPan;
	private JLabel lblPanAmt;
	
	private boolean exitSelection = false;
	private ZeqerCoreInterface core;
	private Z64WaveInfo loadedSample;
	private Z64Envelope loadedEnv;
	private Set<String> tags;
	
	/*----- Init -----*/
	
	public ZeqerDrumEditDialog(Frame parent_frame, ZeqerCoreInterface core_link){
		super(parent_frame, true);
		parent = parent_frame;
		core = core_link;
		globalEnable = new ComponentGroup();
		tags = new HashSet<String>();
		
		initGUI();
		loadDrumData(null);
	}
	
	private void initGUI(){
		setResizable(false);
		setMinimumSize(new Dimension(DEFO_WIDTH, DEFO_HEIGHT));
		setPreferredSize(new Dimension(DEFO_WIDTH, DEFO_HEIGHT));
		setLocationRelativeTo(parent);
		
		setTitle("Edit Drum Preset");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, 1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel pnlLabels = new JPanel();
		pnlLabels.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlLabels = new GridBagConstraints();
		gbc_pnlLabels.insets = new Insets(5, 10, 5, 10);
		gbc_pnlLabels.fill = GridBagConstraints.BOTH;
		gbc_pnlLabels.gridx = 0;
		gbc_pnlLabels.gridy = 0;
		getContentPane().add(pnlLabels, gbc_pnlLabels);
		GridBagLayout gbl_pnlLabels = new GridBagLayout();
		gbl_pnlLabels.columnWidths = new int[]{70, 0, 0, 0};
		gbl_pnlLabels.rowHeights = new int[]{0, 0, 0, 0};
		gbl_pnlLabels.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlLabels.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlLabels.setLayout(gbl_pnlLabels);
		
		JLabel lblName = new JLabel("Name:");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.insets = new Insets(0, 0, 5, 5);
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		pnlLabels.add(lblName, gbc_lblName);
		
		txtName = new JTextField();
		GridBagConstraints gbc_txtName = new GridBagConstraints();
		gbc_txtName.gridwidth = 2;
		gbc_txtName.insets = new Insets(0, 0, 5, 5);
		gbc_txtName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtName.gridx = 1;
		gbc_txtName.gridy = 0;
		pnlLabels.add(txtName, gbc_txtName);
		txtName.setColumns(10);
		globalEnable.addComponent("txtName", txtName);
		
		JLabel lblEnumStem = new JLabel("Enum Stem:");
		GridBagConstraints gbc_lblEnumStem = new GridBagConstraints();
		gbc_lblEnumStem.insets = new Insets(0, 5, 5, 5);
		gbc_lblEnumStem.gridx = 0;
		gbc_lblEnumStem.gridy = 1;
		pnlLabels.add(lblEnumStem, gbc_lblEnumStem);
		
		txtEnum = new JTextField();
		GridBagConstraints gbc_txtEnum = new GridBagConstraints();
		gbc_txtEnum.gridwidth = 2;
		gbc_txtEnum.insets = new Insets(0, 0, 5, 5);
		gbc_txtEnum.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtEnum.gridx = 1;
		gbc_txtEnum.gridy = 1;
		pnlLabels.add(txtEnum, gbc_txtEnum);
		txtEnum.setColumns(10);
		globalEnable.addComponent("txtEnum", txtEnum);
		
		JButton btnTags = new JButton("Edit Tags...");
		GridBagConstraints gbc_btnTags = new GridBagConstraints();
		gbc_btnTags.insets = new Insets(0, 0, 0, 5);
		gbc_btnTags.gridx = 2;
		gbc_btnTags.gridy = 2;
		pnlLabels.add(btnTags, gbc_btnTags);
		globalEnable.addComponent("btnTags", btnTags);
		btnTags.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnTagsCallback();
			}});
		
		JPanel pnlSample = new JPanel();
		pnlSample.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlSample = new GridBagConstraints();
		gbc_pnlSample.insets = new Insets(0, 10, 5, 10);
		gbc_pnlSample.fill = GridBagConstraints.BOTH;
		gbc_pnlSample.gridx = 0;
		gbc_pnlSample.gridy = 1;
		getContentPane().add(pnlSample, gbc_pnlSample);
		GridBagLayout gbl_pnlSample = new GridBagLayout();
		gbl_pnlSample.columnWidths = new int[]{70, 0, 0, 0, 0};
		gbl_pnlSample.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_pnlSample.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlSample.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		pnlSample.setLayout(gbl_pnlSample);
		
		JLabel lblSample = new JLabel("Sample:");
		GridBagConstraints gbc_lblSample = new GridBagConstraints();
		gbc_lblSample.anchor = GridBagConstraints.EAST;
		gbc_lblSample.insets = new Insets(0, 5, 5, 5);
		gbc_lblSample.gridx = 0;
		gbc_lblSample.gridy = 0;
		pnlSample.add(lblSample, gbc_lblSample);
		
		txtSample = new JTextField();
		GridBagConstraints gbc_txtSample = new GridBagConstraints();
		gbc_txtSample.gridwidth = 2;
		gbc_txtSample.insets = new Insets(0, 0, 5, 5);
		gbc_txtSample.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtSample.gridx = 1;
		gbc_txtSample.gridy = 0;
		pnlSample.add(txtSample, gbc_txtSample);
		txtSample.setColumns(10);
		globalEnable.addComponent("txtSample", txtSample);
		
		JButton btnSample = new JButton("Set Sample...");
		GridBagConstraints gbc_btnSample = new GridBagConstraints();
		gbc_btnSample.insets = new Insets(0, 0, 5, 5);
		gbc_btnSample.gridx = 1;
		gbc_btnSample.gridy = 1;
		pnlSample.add(btnSample, gbc_btnSample);
		globalEnable.addComponent("btnSample", btnSample);
		btnSample.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnSampleCallback();
			}});
		
		JLabel lblUnityKey = new JLabel("Unity Key:");
		GridBagConstraints gbc_lblUnityKey = new GridBagConstraints();
		gbc_lblUnityKey.insets = new Insets(0, 5, 5, 5);
		gbc_lblUnityKey.gridx = 0;
		gbc_lblUnityKey.gridy = 2;
		pnlSample.add(lblUnityKey, gbc_lblUnityKey);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 1;
		gbc_panel.gridy = 2;
		pnlSample.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{60, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		spnUnityKey = new JSpinner();
		GridBagConstraints gbc_spnUnityKey = new GridBagConstraints();
		gbc_spnUnityKey.fill = GridBagConstraints.HORIZONTAL;
		gbc_spnUnityKey.insets = new Insets(0, 0, 0, 5);
		gbc_spnUnityKey.gridx = 0;
		gbc_spnUnityKey.gridy = 0;
		panel.add(spnUnityKey, gbc_spnUnityKey);
		globalEnable.addComponent("spnUnityKey", spnUnityKey);
		spnUnityKey.setModel(new SpinnerNumberModel(MIDDLE_C, 0, 127, 1));
		
		JLabel lblFineTune = new JLabel("Fine Tune:");
		GridBagConstraints gbc_lblFineTune = new GridBagConstraints();
		gbc_lblFineTune.insets = new Insets(0, 5, 0, 5);
		gbc_lblFineTune.gridx = 0;
		gbc_lblFineTune.gridy = 3;
		pnlSample.add(lblFineTune, gbc_lblFineTune);
		
		sldFineTune = new JSlider();
		GridBagConstraints gbc_sldFineTune = new GridBagConstraints();
		gbc_sldFineTune.fill = GridBagConstraints.HORIZONTAL;
		gbc_sldFineTune.insets = new Insets(0, 0, 0, 5);
		gbc_sldFineTune.gridx = 1;
		gbc_sldFineTune.gridy = 3;
		pnlSample.add(sldFineTune, gbc_sldFineTune);
		globalEnable.addComponent("sldFineTune", sldFineTune);
		sldFineTune.setMinimum(-100);
		sldFineTune.setMaximum(100);
		sldFineTune.setValue(0);
		
		lblFineAmt = new JLabel("0 cents");
		GridBagConstraints gbc_lblFineAmt = new GridBagConstraints();
		gbc_lblFineAmt.insets = new Insets(0, 0, 0, 5);
		gbc_lblFineAmt.gridx = 2;
		gbc_lblFineAmt.gridy = 3;
		pnlSample.add(lblFineAmt, gbc_lblFineAmt);
		
		JPanel pnlEnv = new JPanel();
		pnlEnv.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlEnv = new GridBagConstraints();
		gbc_pnlEnv.insets = new Insets(0, 10, 5, 10);
		gbc_pnlEnv.fill = GridBagConstraints.BOTH;
		gbc_pnlEnv.gridx = 0;
		gbc_pnlEnv.gridy = 2;
		getContentPane().add(pnlEnv, gbc_pnlEnv);
		GridBagLayout gbl_pnlEnv = new GridBagLayout();
		gbl_pnlEnv.columnWidths = new int[]{70, 0, 0, 0, 0};
		gbl_pnlEnv.rowHeights = new int[]{0, 0, 0, 0};
		gbl_pnlEnv.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlEnv.rowWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlEnv.setLayout(gbl_pnlEnv);
		
		pnlRel = new ReleaseEditPanel(core);
		pnlRel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		GridBagConstraints gbc_pnlRel = new GridBagConstraints();
		gbc_pnlRel.gridwidth = 4;
		gbc_pnlRel.insets = new Insets(0, 0, 5, 5);
		gbc_pnlRel.fill = GridBagConstraints.BOTH;
		gbc_pnlRel.gridx = 0;
		gbc_pnlRel.gridy = 0;
		pnlEnv.add(pnlRel, gbc_pnlRel);
		
		
		JButton btnEnv = new JButton("Edit Envelope...");
		GridBagConstraints gbc_btnEnv = new GridBagConstraints();
		gbc_btnEnv.insets = new Insets(0, 0, 5, 5);
		gbc_btnEnv.gridx = 1;
		gbc_btnEnv.gridy = 1;
		pnlEnv.add(btnEnv, gbc_btnEnv);
		globalEnable.addComponent("btnEnv", btnEnv);
		btnEnv.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnEnvelopeCallback();
			}});
		
		JLabel lblPan = new JLabel("Pan:");
		GridBagConstraints gbc_lblPan = new GridBagConstraints();
		gbc_lblPan.anchor = GridBagConstraints.EAST;
		gbc_lblPan.insets = new Insets(0, 5, 5, 5);
		gbc_lblPan.gridx = 0;
		gbc_lblPan.gridy = 2;
		pnlEnv.add(lblPan, gbc_lblPan);
		
		sldPan = new JSlider();
		GridBagConstraints gbc_sldPan = new GridBagConstraints();
		gbc_sldPan.fill = GridBagConstraints.HORIZONTAL;
		gbc_sldPan.insets = new Insets(0, 0, 5, 5);
		gbc_sldPan.gridx = 1;
		gbc_sldPan.gridy = 2;
		pnlEnv.add(sldPan, gbc_sldPan);
		globalEnable.addComponent("sldPan", sldPan);
		sldPan.setMinimum(0);
		sldPan.setMaximum(127);
		sldPan.setValue(64);
		
		lblPanAmt = new JLabel("Center");
		GridBagConstraints gbc_lblPanAmt = new GridBagConstraints();
		gbc_lblPanAmt.insets = new Insets(0, 0, 5, 5);
		gbc_lblPanAmt.gridx = 2;
		gbc_lblPanAmt.gridy = 2;
		pnlEnv.add(lblPanAmt, gbc_lblPanAmt);
		
		JPanel pnlControl = new JPanel();
		GridBagConstraints gbc_pnlControl = new GridBagConstraints();
		gbc_pnlControl.insets = new Insets(0, 10, 5, 10);
		gbc_pnlControl.fill = GridBagConstraints.BOTH;
		gbc_pnlControl.gridx = 0;
		gbc_pnlControl.gridy = 3;
		getContentPane().add(pnlControl, gbc_pnlControl);
		GridBagLayout gbl_pnlControl = new GridBagLayout();
		gbl_pnlControl.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_pnlControl.rowHeights = new int[]{0, 0};
		gbl_pnlControl.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlControl.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlControl.setLayout(gbl_pnlControl);
		
		JButton btnPreview = new JButton("Preview...");
		GridBagConstraints gbc_btnPreview = new GridBagConstraints();
		gbc_btnPreview.insets = new Insets(0, 0, 0, 5);
		gbc_btnPreview.gridx = 0;
		gbc_btnPreview.gridy = 0;
		pnlControl.add(btnPreview, gbc_btnPreview);
		globalEnable.addComponent("btnPreview", btnPreview);
		btnPreview.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnPreviewCallback();
			}});
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 0;
		pnlControl.add(btnCancel, gbc_btnCancel);
		globalEnable.addComponent("btnCancel", btnCancel);
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnCancelCallback();
			}});
		
		JButton btnOkay = new JButton("Okay");
		GridBagConstraints gbc_btnOkay = new GridBagConstraints();
		gbc_btnOkay.gridx = 3;
		gbc_btnOkay.gridy = 0;
		pnlControl.add(btnOkay, gbc_btnOkay);
		globalEnable.addComponent("btnOkay", btnOkay);
		btnOkay.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnOkayCallback();
			}});
		
		
		sldPan.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				sldPanCallback();
			}});
		
		sldFineTune.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				sldFineTuneCallback();
			}});
	}
	
	/*----- Getters -----*/
	
	public boolean loadDataInto(ZeqerDrumPreset dpreset){
		if(dpreset == null) return false;
		Z64Drum drum = dpreset.getData();
		if(drum == null) return false;
		
		String s = txtName.getText();
		if(s != null) dpreset.setName(s);
		else{
			//Gen name.
			s = String.format("Drum %08x", dpreset.getUID());
			txtName.setText(s);
			dpreset.setName(s);
		}
		
		s = txtEnum.getText();
		s = ZeqerUtils.fixHeaderEnumID(s, true);
		if(s == null || s.isEmpty()){
			
		}
		txtEnum.setText(s);
		dpreset.setEnumLabel(s);
		
		dpreset.clearTags();
		for(String tag : tags) dpreset.addTag(tag);
		
		drum.setSample(loadedSample);
		if(loadedSample != null){
			dpreset.setWaveID(loadedSample.getUID());
		}
		else dpreset.setWaveID(0);
		
		drum.setRootKey((Byte)spnUnityKey.getValue());
		drum.setFineTune((byte)sldFineTune.getValue());
		drum.setDecay((byte)pnlRel.getCurrentReleaseValue());
		if(loadedEnv == null){
			loadedEnv = Z64Envelope.newDefaultEnvelope();
		}
		drum.setEnvelope(loadedEnv);
		drum.setPan((byte)sldPan.getValue());
		
		globalEnable.repaint();
		return true;
	}
	
	public boolean getExitSelection(){return exitSelection;}
	
	/*----- Setters -----*/
	
	public void loadDrumData(ZeqerDrumPreset dpreset){
		if(dpreset != null){
			int uid = dpreset.getUID();
			String s = dpreset.getName();
			if(s == null){
				s = String.format("Drum %08x", uid);
				dpreset.setName(s);
			}
			txtName.setText(s);
			
			s = dpreset.getEnumLabel();
			if(s == null){
				s = String.format("DPRE_%08x", uid);
				dpreset.setEnumLabel(s);
			}
			txtEnum.setText(s);
			
			tags.clear();
			tags.addAll(dpreset.getAllTags());
			
			Z64Drum drum = dpreset.getData();
			loadedSample = drum.getSample();
			loadedEnv = drum.getEnvelope().copy();
			
			spnUnityKey.setValue(drum.getRootKey());
			sldFineTune.setValue(drum.getFineTune());
			sldPan.setValue(drum.getPan());
			pnlRel.setCurrentReleaseVal(Byte.toUnsignedInt(drum.getDecay()));
			//System.err.println("ZeqerDrumEditDialg.loadDrumData || -DEBUG- || Release = " + Byte.toUnsignedInt(drum.getDecay()));
		}
		else{
			//Set to default parameters.
			txtName.setText("Untitled Drum");
			txtEnum.setText("DPRE_ANON");
			tags.clear();
			
			loadedSample = null;
			
			spnUnityKey.setValue(MIDDLE_C);
			sldFineTune.setValue(0);
			sldPan.setValue(64);
			
			loadedEnv = null;
			pnlRel.setCurrentReleaseVal(251);
		}
		
		updateSampleTextbox();
		updatePanAmtLabel();
		updateFineTuneAmtLabel();
		
		pnlRel.repaint();
		globalEnable.repaint();
	}
	
	/*----- Drawing -----*/
	
	private void updatePanAmtLabel(){
		int panval = sldPan.getValue();
		lblPanAmt.setText(ZeqerGUIUtils.pan2String((byte)panval, core));
		lblPanAmt.repaint();
	}
	
	private void updateFineTuneAmtLabel(){
		int tuneamt = sldFineTune.getValue();
		lblFineAmt.setText(tuneamt + " cent(s)");
		lblFineAmt.repaint();
	}
	
	private void updateSampleTextbox(){
		if(loadedSample != null){
			String name = loadedSample.getName();
			if(name != null){
				txtSample.setText(name);
			}
			else txtSample.setText("<unnamed>");
		}
		else{
			txtSample.setText("<null>");
		}
		txtSample.repaint();
	}
	
	public void showMe(Component c){
		setLocationRelativeTo(c);
		pack();
		setVisible(true);
	}
	
	public void closeMe(){
		this.setVisible(false);
		this.dispose();
	}
	
	/*----- Callbacks -----*/
	
	private void dummyCallback(){
		JOptionPane.showMessageDialog(this, "Sorry, this component doesn't work yet!", 
				"Notice", JOptionPane.INFORMATION_MESSAGE);
	}
	
	private void btnOkayCallback(){
		exitSelection = true;
		closeMe();
	}
	
	private void btnCancelCallback(){
		exitSelection = false;
		closeMe();
	}
	
	private void btnPreviewCallback(){
		//TODO
		dummyCallback();
	}
	
	private void btnEnvelopeCallback(){
		ZeqerEnvEditDialog dialog = new ZeqerEnvEditDialog(parent, core);
		dialog.loadEnvelope(loadedEnv);
		dialog.showMe(this);
		
		Z64Envelope env = dialog.getOutputEnvelope();
		if(env != null) loadedEnv = env;
	}
	
	private void btnSampleCallback(){
		//TODO
		dummyCallback();
	}
	
	private void btnTagsCallback(){
		ZeqerTagEditDialog dialog = new ZeqerTagEditDialog(parent);
		dialog.loadTags(tags);
		dialog.showMe(this);
		
		if(dialog.getExitSelection()){
			tags.clear();
			tags.addAll(dialog.getTags());
		}
	}
	
	private void sldFineTuneCallback(){
		updateFineTuneAmtLabel();
	}
	
	private void sldPanCallback(){
		updatePanAmtLabel();
	}

}
