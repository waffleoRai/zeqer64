package waffleoRai_zeqer64.GUI.dialogs;

import javax.swing.JFrame;

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.border.EtchedBorder;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.GUITools;
import waffleoRai_GUITools.WRFrame;
import waffleoRai_SeqSound.n64al.NUSALSeq;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_zeqer64.ZeqerSeq;
import waffleoRai_zeqer64.ZeqerSeq.Label;
import waffleoRai_zeqer64.ZeqerSeq.Module;
import waffleoRai_zeqer64.GUI.seqedit.SeqBinPanel;
import waffleoRai_zeqer64.GUI.seqedit.SeqMetaEditDialog;
import waffleoRai_zeqer64.GUI.seqedit.SeqSoundfontEditDialog;
import waffleoRai_zeqer64.GUI.seqedit.mmlview.MMLScriptPanel;
import waffleoRai_zeqer64.filefmt.bank.BankTableEntry;
import waffleoRai_zeqer64.filefmt.seq.SeqTableEntry;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.border.BevelBorder;

public class ZeqerSeqHubDialog extends WRFrame{

	private static final long serialVersionUID = -150662495937951531L;
	
	public static final int MIN_WIDTH = 720;
	public static final int MIN_HEIGHT = 480;

	/*--- Instance Variables ---*/
	
	private JFrame parent;
	private ZeqerCoreInterface core;
	
	private boolean editable;
	private boolean exitSelection = false;
	
	private ComponentGroup globalEnable;
	private ComponentGroup editableEnable;
	
	private JLabel lblSeqTitle;
	private JComboBox<String> cmbxGoto;
	
	private SeqBinPanel pnlBin;
	private MMLScriptPanel pnlMML;
	
	private String seqName; //Stored here instead of just in component.
	private Map<String, Integer> labelMap;
	private NUSALSeq parsedSeq;
	private FileBuffer seqData;
	
	private SeqMetaEditDialog childMeta;
	private SeqSoundfontEditDialog childSoundfont;
	
	/*--- Initialization ---*/
	
	public ZeqerSeqHubDialog(JFrame parentFrame, ZeqerCoreInterface coreLink, boolean readOnly){
		super();
		parent = parentFrame;
		core = coreLink;
		editable = !readOnly;
		
		globalEnable = new ComponentGroup();
		editableEnable = globalEnable.newChild();
		
		labelMap = new HashMap<String, Integer>();
		
		initGUI();
		refreshLocalGUI();
	}
	
	private void initGUI(){
		setTitle("View Sequence");
		
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel pnlModBtns = new JPanel();
		pnlModBtns.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlModBtns = new GridBagConstraints();
		gbc_pnlModBtns.gridwidth = 2;
		gbc_pnlModBtns.insets = new Insets(0, 0, 5, 0);
		gbc_pnlModBtns.fill = GridBagConstraints.BOTH;
		gbc_pnlModBtns.gridx = 0;
		gbc_pnlModBtns.gridy = 0;
		getContentPane().add(pnlModBtns, gbc_pnlModBtns);
		GridBagLayout gbl_pnlModBtns = new GridBagLayout();
		gbl_pnlModBtns.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_pnlModBtns.rowHeights = new int[]{0, 0};
		gbl_pnlModBtns.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlModBtns.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlModBtns.setLayout(gbl_pnlModBtns);
		
		JButton btnMeta = new JButton("Metadata");
		GridBagConstraints gbc_btnMeta = new GridBagConstraints();
		gbc_btnMeta.insets = new Insets(0, 0, 0, 5);
		gbc_btnMeta.gridx = 0;
		gbc_btnMeta.gridy = 0;
		pnlModBtns.add(btnMeta, gbc_btnMeta);
		globalEnable.addComponent("btnMeta", btnMeta);
		btnMeta.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnMetaCallback();
			}});
		
		JButton btnSF = new JButton("Soundfonts");
		GridBagConstraints gbc_btnSF = new GridBagConstraints();
		gbc_btnSF.insets = new Insets(0, 0, 0, 5);
		gbc_btnSF.gridx = 1;
		gbc_btnSF.gridy = 0;
		pnlModBtns.add(btnSF, gbc_btnSF);
		globalEnable.addComponent("btnSF", btnSF);
		btnSF.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnSoundfontsCallback();
			}});
		
		JButton btnLabels = new JButton("Labels");
		GridBagConstraints gbc_btnLabels = new GridBagConstraints();
		gbc_btnLabels.insets = new Insets(0, 0, 0, 5);
		gbc_btnLabels.gridx = 2;
		gbc_btnLabels.gridy = 0;
		pnlModBtns.add(btnLabels, gbc_btnLabels);
		globalEnable.addComponent("btnLabels", btnLabels);
		btnLabels.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnLabelsCallback();
			}});
		
		JButton btnAnalyze = new JButton("Analyze");
		GridBagConstraints gbc_btnAnalyze = new GridBagConstraints();
		gbc_btnAnalyze.insets = new Insets(0, 0, 0, 5);
		gbc_btnAnalyze.gridx = 3;
		gbc_btnAnalyze.gridy = 0;
		pnlModBtns.add(btnAnalyze, gbc_btnAnalyze);
		globalEnable.addComponent("btnAnalyze", btnAnalyze);
		btnAnalyze.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnAnalyzeCallback();
			}});
		
		JButton btnTimeline = new JButton("Timeline");
		GridBagConstraints gbc_btnTimeline = new GridBagConstraints();
		gbc_btnTimeline.insets = new Insets(0, 0, 0, 5);
		gbc_btnTimeline.gridx = 4;
		gbc_btnTimeline.gridy = 0;
		pnlModBtns.add(btnTimeline, gbc_btnTimeline);
		globalEnable.addComponent("btnTimeline", btnTimeline);
		btnTimeline.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnTimelineCallback();
			}});
		
		JButton btnPlayer = new JButton("Player");
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 0, 5);
		gbc_btnNewButton.gridx = 5;
		gbc_btnNewButton.gridy = 0;
		pnlModBtns.add(btnPlayer, gbc_btnNewButton);
		globalEnable.addComponent("btnPlayer", btnPlayer);
		btnPlayer.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnPlayerCallback();
			}});
		
		JPanel pnlInfo = new JPanel();
		pnlInfo.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		GridBagConstraints gbc_pnlInfo = new GridBagConstraints();
		gbc_pnlInfo.gridwidth = 2;
		gbc_pnlInfo.insets = new Insets(0, 0, 5, 5);
		gbc_pnlInfo.fill = GridBagConstraints.BOTH;
		gbc_pnlInfo.gridx = 0;
		gbc_pnlInfo.gridy = 1;
		getContentPane().add(pnlInfo, gbc_pnlInfo);
		GridBagLayout gbl_pnlInfo = new GridBagLayout();
		gbl_pnlInfo.columnWidths = new int[]{0, 0, 0, 50, 0};
		gbl_pnlInfo.rowHeights = new int[]{0, 0};
		gbl_pnlInfo.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlInfo.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlInfo.setLayout(gbl_pnlInfo);
		
		lblSeqTitle = new JLabel("[empty]");
		lblSeqTitle.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblSeqTitle = new GridBagConstraints();
		gbc_lblSeqTitle.insets = new Insets(0, 5, 0, 5);
		gbc_lblSeqTitle.gridx = 0;
		gbc_lblSeqTitle.gridy = 0;
		pnlInfo.add(lblSeqTitle, gbc_lblSeqTitle);
		
		JLabel lblGoto = new JLabel("Goto:");
		GridBagConstraints gbc_lblGoto = new GridBagConstraints();
		gbc_lblGoto.insets = new Insets(0, 0, 0, 5);
		gbc_lblGoto.anchor = GridBagConstraints.EAST;
		gbc_lblGoto.gridx = 2;
		gbc_lblGoto.gridy = 0;
		pnlInfo.add(lblGoto, gbc_lblGoto);
		
		cmbxGoto = new JComboBox<String>();
		GridBagConstraints gbc_cmbxGoto = new GridBagConstraints();
		gbc_cmbxGoto.insets = new Insets(0, 0, 0, 5);
		gbc_cmbxGoto.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxGoto.gridx = 3;
		gbc_cmbxGoto.gridy = 0;
		pnlInfo.add(cmbxGoto, gbc_cmbxGoto);
		globalEnable.addComponent("cmbxGoto", cmbxGoto);
		cmbxGoto.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				cmbxGotoCallback();
			}});
		
		pnlMML = new MMLScriptPanel();
		pnlMML.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlMML = new GridBagConstraints();
		gbc_pnlMML.insets = new Insets(0, 5, 5, 5);
		gbc_pnlMML.fill = GridBagConstraints.BOTH;
		gbc_pnlMML.gridx = 0;
		gbc_pnlMML.gridy = 2;
		getContentPane().add(pnlMML, gbc_pnlMML);
		
		pnlBin = new SeqBinPanel();
		pnlBin.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlBin = new GridBagConstraints();
		gbc_pnlBin.insets = new Insets(0, 0, 5, 5);
		gbc_pnlBin.fill = GridBagConstraints.BOTH;
		gbc_pnlBin.gridx = 1;
		gbc_pnlBin.gridy = 2;
		getContentPane().add(pnlBin, gbc_pnlBin);
		
		JPanel pnlBtn = new JPanel();
		GridBagConstraints gbc_pnlBtn = new GridBagConstraints();
		gbc_pnlBtn.gridwidth = 2;
		gbc_pnlBtn.fill = GridBagConstraints.BOTH;
		gbc_pnlBtn.gridx = 0;
		gbc_pnlBtn.gridy = 3;
		getContentPane().add(pnlBtn, gbc_pnlBtn);
		GridBagLayout gbl_pnlBtn = new GridBagLayout();
		gbl_pnlBtn.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_pnlBtn.rowHeights = new int[]{0, 0};
		gbl_pnlBtn.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlBtn.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlBtn.setLayout(gbl_pnlBtn);
		
		JButton btnImport = new JButton("Import Data...");
		GridBagConstraints gbc_btnImport = new GridBagConstraints();
		gbc_btnImport.insets = new Insets(0, 5, 5, 5);
		gbc_btnImport.gridx = 0;
		gbc_btnImport.gridy = 0;
		pnlBtn.add(btnImport, gbc_btnImport);
		editableEnable.addComponent("btnImport", btnImport);
		btnImport.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnImportCallback();
			}});
		
		JButton btnExport = new JButton("Export...");
		GridBagConstraints gbc_btnExport = new GridBagConstraints();
		gbc_btnExport.insets = new Insets(0, 0, 5, 5);
		gbc_btnExport.gridx = 1;
		gbc_btnExport.gridy = 0;
		pnlBtn.add(btnExport, gbc_btnExport);
		globalEnable.addComponent("btnExport", btnExport);
		btnExport.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnExportCallback();
			}});
		
		JButton btnSave = new JButton("Save");
		GridBagConstraints gbc_btnSave = new GridBagConstraints();
		gbc_btnSave.insets = new Insets(0, 0, 5, 5);
		gbc_btnSave.gridx = 3;
		gbc_btnSave.gridy = 0;
		pnlBtn.add(btnSave, gbc_btnSave);
		editableEnable.addComponent("btnSave", btnSave);
		btnSave.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnSaveCallback();
			}});
		
		JButton btnClose = new JButton("Close");
		GridBagConstraints gbc_btnClose = new GridBagConstraints();
		gbc_btnClose.insets = new Insets(0, 0, 5, 5);
		gbc_btnClose.gridx = 4;
		gbc_btnClose.gridy = 0;
		pnlBtn.add(btnClose, gbc_btnClose);
		globalEnable.addComponent("btnClose", btnClose);
		btnClose.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnCloseCallback();
			}});
		
		initChildDialogs();
	}
	
	private void initChildDialogs(){
		//TODO
		childMeta = new SeqMetaEditDialog(this, core, !editable);
		childSoundfont = new SeqSoundfontEditDialog(this, core, !editable);
	}
	
	/*--- Getters ---*/
	
	public boolean getExitSelection(){return exitSelection;}
	
	public JFrame getParentFrame(){
		return parent;
	}
	
	public void loadIntoSeq(ZeqerSeq seq){
		//TODO
		if(seq == null) return;
		if(!editable) return;
		
		SeqTableEntry meta = seq.getTableEntry();
		if(meta == null) return;
		
		childMeta.getFields(meta);
		meta.setName(seqName);
		
		meta.clearBanks();
		BankTableEntry[] banks = childSoundfont.getBanks();
		if(banks != null && banks.length > 0){
			int count = -1;
			for(int i = 3; i >= 0; i--){
				if(banks[i] != null){
					count = i+1;
					break;
				}
			}
			if(count > 0){
				if(count == 1){
					meta.setSingleBank(banks[0].getUID());
				}
				else{
					for(int i = 0; i < 4; i++){
						if(banks[i] != null){
							meta.addBank(banks[i].getUID());
						}
					}
				}
			}
		}
		
	}
	
	/*--- Setters ---*/
	
	private void loadLabelMap(ZeqerSeq seq){
		//Pull from seq metadata
		if(seq == null) return;
		List<Label> labels = seq.getCommonLabels();
		if(labels != null){
			for(Label lbl : labels){
				labelMap.put(lbl.getName(), lbl.getPosition());
			}
		}
		
		Module[] mods = seq.getModules();
		if(mods != null){
			for(int i = 0; i < mods.length; i++){
				if(mods[i] != null){
					labels = mods[i].getAllLabels();
					if(labels != null){
						for(Label lbl : labels){
							labelMap.put(lbl.getName(), lbl.getPosition());
						}
					}
				}
			}
		}
	}
	
	public void setSeqNameLocal(String value){
		//Does NOT update it in metadata child dialog.
		seqName = value;
		lblSeqTitle.setText(seqName);
		lblSeqTitle.repaint();
	}
	
	public void loadFromSeq(ZeqerSeq seq){
		//TODO
		labelMap.clear();
		parsedSeq = null;
		seqData = null;
		
		if(seq != null){
			SeqTableEntry meta = seq.getTableEntry();
			
			//Child forms
			childMeta.setFields(meta);
			childSoundfont.clear();
			if(core != null){
				int bcount = meta.getBankCount();
				if(bcount > 1){
					List<Integer> ilist = meta.getLinkedBankUIDs();
					int i = 0;
					for(Integer uid : ilist){
						childSoundfont.setBank(uid, i);
					}
				}
				else{
					int buid = meta.getPrimaryBankUID();
					if(buid != 0){
						childSoundfont.setBank(buid, 0);
					}
				}
			}
			
			setSeqNameLocal(childMeta.getNameField());
			
			//Local form (Sequence rendering, label mapping)
			parsedSeq = seq.getSequence();
			seqData = seq.getRawData();
			
			loadLabelMap(seq);
		}
		else{
			setSeqNameLocal("<null>");
			childMeta.setFields(null);
			childSoundfont.clear();
		}
		
		refreshCombobox();
		refreshLocalGUI();
		refreshChildDialogs();
	}
	
	/*--- Drawing ---*/
	
	private void refreshChildDialogs(){
		//TODO
		childMeta.unsetWait();
		childSoundfont.unsetWait();
	}
	
	public void refreshLocalGUI(){
		//Does not include open children.
		lblSeqTitle.setText(seqName);
		pnlMML.loadScript(parsedSeq);
		if(parsedSeq != null){
			pnlBin.loadSeq(parsedSeq);
		}
		else pnlBin.loadRawSeq(seqData);
		
		pnlBin.repaint();
		pnlMML.repaint();
		reenable();
	}
	
	private void refreshCombobox(){
		int selidx = cmbxGoto.getSelectedIndex();
		
		int count = 1;
		DefaultComboBoxModel<String> mdl = new DefaultComboBoxModel<String>();
		mdl.addElement("_start");
		
		List<String> lbls = new LinkedList<String>();
		lbls.addAll(labelMap.keySet());
		Collections.sort(lbls);
		
		for(String lbl : lbls){
			if(lbl.equals("_start")) continue;
			mdl.addElement(lbl);
			count++;
		}
		
		cmbxGoto.setModel(mdl);
		if(selidx >= count){
			selidx = lbls.isEmpty()?-1:0;
		}
		if(!lbls.isEmpty() && selidx < 0) selidx = 0;
		cmbxGoto.setSelectedIndex(selidx);
	}
	
	public void disableAll(){
		globalEnable.setEnabling(false);
		pnlMML.disable();
		pnlBin.disable();
		//Does NOT disable children.
		globalEnable.repaint();
	}
	
	public void reenable(){
		globalEnable.setEnabling(true);
		pnlMML.enable();
		pnlBin.enable();
		if(!editable) editableEnable.setEnabling(false);
		globalEnable.repaint();
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
	
	public void closeMe(){
		//TODO
		//Close and dispose children too!!
		childMeta.setVisible(false);
		childMeta.dispose();
		
		childSoundfont.setVisible(false);
		childSoundfont.dispose();
		
		this.setVisible(false);
		this.dispose();
	}
	
	/*--- Callbacks ---*/
	
	private void btnSaveCallback(){
		if(!editable) return;
		exitSelection = true;
		closeMe();
	}
	
	private void btnCloseCallback(){
		exitSelection = false;
		closeMe();
	}
	
	private void btnImportCallback(){
		//TODO
		dummyCallback();
	}
	
	private void btnExportCallback(){
		//TODO
		dummyCallback();
	}
	
	private void btnMetaCallback(){
		if(childMeta.isVisible()){
			childMeta.requestFocus();
		}
		else{
			childMeta.showMe(this);
		}
	}
	
	private void btnSoundfontsCallback(){
		if(childSoundfont.isVisible()){
			childSoundfont.requestFocus();
		}
		else{
			childSoundfont.showMe(this);
		}
	}
	
	private void btnLabelsCallback(){
		//TODO
		dummyCallback();
	}
	
	private void btnAnalyzeCallback(){
		//TODO
		dummyCallback();
	}
	
	private void btnTimelineCallback(){
		//TODO
		dummyCallback();
	}
	
	private void btnPlayerCallback(){
		//TODO
		dummyCallback();
	}
	
	private void cmbxGotoCallback(){
		//dummyCallback();
		int idx = cmbxGoto.getSelectedIndex();
		if(idx < 0) return;
		
		String lbl = cmbxGoto.getItemAt(idx);
		Integer addr = labelMap.get(lbl);
		if(addr == null) return;
		
		pnlBin.jumpTo(addr, false);
		pnlMML.jumpTo(addr, false);
	}
	
}
