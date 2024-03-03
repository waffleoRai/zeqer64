package waffleoRai_zeqer64.GUI.seqedit;

import java.awt.GridBagLayout;
import javax.swing.JComboBox;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JTextField;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.GUITools;
import waffleoRai_GUITools.WRFrame;
import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_zeqer64.ZeqerSeq;
import waffleoRai_zeqer64.ZeqerUtils;
import waffleoRai_zeqer64.GUI.ZeqerGUIUtils.CacheType;
import waffleoRai_zeqer64.GUI.ZeqerGUIUtils.MediumType;
import waffleoRai_zeqer64.GUI.ZeqerGUIUtils.SeqType;
import waffleoRai_zeqer64.GUI.dialogs.ZeqerSeqHubDialog;
import waffleoRai_zeqer64.GUI.dialogs.ZeqerTagEditDialog;
import waffleoRai_zeqer64.filefmt.seq.SeqTableEntry;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;

import javax.swing.JPanel;

public class SeqMetaEditDialog extends WRFrame{

	private static final long serialVersionUID = -8289076955252492729L;
	
	private static final int[] MEDIUM_CMBX_IDXS = {0, -1, 1, 2};
	private static final int[] CACHE_CMBX_IDXS = {2, 1, 0, 3, 4};
	private static final int[] TYPE_CMBX_IDXS = {0, 1, 2, 3, 4,
												5, 6, 7, 8, 9};
	
	public static final int MIN_WIDTH = 320;
	public static final int MIN_HEIGHT = 250;
	
	/*--- Instance Variables ---*/
	
	private ZeqerSeqHubDialog parent;
	private ZeqerCoreInterface core;
	
	private ComponentGroup globalEnable;
	private ComponentGroup editableEnable;
	
	private boolean editable;
	private boolean changesApplied = false;
	private Set<String> tags;
	
	private String applEnum;
	private Set<String> applTags;
	private int applMed = Z64Sound.MEDIUM_CART;
	private int applCache = Z64Sound.CACHE_TEMPORARY;
	private int applType = ZeqerSeq.SEQTYPE_UNKNOWN;
	
	private JTextField txtName;
	private JTextField txtEnum;
	
	private JComboBox<SeqType> cmbxType;
	private JComboBox<MediumType> cmbxMedium;
	private JComboBox<CacheType> cmbxCache;
	
	/*--- Init ---*/

	public SeqMetaEditDialog(ZeqerSeqHubDialog parentDialog, ZeqerCoreInterface core_iface, boolean readonly){
		parent = parentDialog;
		core = core_iface;
		editable = !readonly;
		
		globalEnable = new ComponentGroup();
		editableEnable = globalEnable.newChild();
		tags = new HashSet<String>();
		applTags = new HashSet<String>();
		
		initGUI();
		reenable();
	}
	
	private void initGUI(){
		setResizable(false);
		setTitle("Sequence Metadata");
		
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JLabel lblName = new JLabel("Name:");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(5, 5, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		getContentPane().add(lblName, gbc_lblName);
		
		txtName = new JTextField();
		GridBagConstraints gbc_txtName = new GridBagConstraints();
		gbc_txtName.gridwidth = 2;
		gbc_txtName.insets = new Insets(5, 0, 5, 0);
		gbc_txtName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtName.gridx = 1;
		gbc_txtName.gridy = 0;
		getContentPane().add(txtName, gbc_txtName);
		txtName.setColumns(10);
		globalEnable.addComponent("txtName", txtName);
		
		JLabel lblEnum = new JLabel("Enum:");
		GridBagConstraints gbc_lblEnum = new GridBagConstraints();
		gbc_lblEnum.anchor = GridBagConstraints.EAST;
		gbc_lblEnum.insets = new Insets(0, 5, 5, 5);
		gbc_lblEnum.gridx = 0;
		gbc_lblEnum.gridy = 1;
		getContentPane().add(lblEnum, gbc_lblEnum);
		
		txtEnum = new JTextField();
		GridBagConstraints gbc_txtEnum = new GridBagConstraints();
		gbc_txtEnum.gridwidth = 2;
		gbc_txtEnum.insets = new Insets(0, 0, 5, 0);
		gbc_txtEnum.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtEnum.gridx = 1;
		gbc_txtEnum.gridy = 1;
		getContentPane().add(txtEnum, gbc_txtEnum);
		txtEnum.setColumns(10);
		globalEnable.addComponent("txtEnum", txtEnum);
		
		JButton btnTags = new JButton("Edit Tags...");
		GridBagConstraints gbc_btnTags = new GridBagConstraints();
		gbc_btnTags.insets = new Insets(0, 0, 5, 0);
		gbc_btnTags.gridx = 2;
		gbc_btnTags.gridy = 2;
		getContentPane().add(btnTags, gbc_btnTags);
		btnTags.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnTagsCallback();
			}
		});
		globalEnable.addComponent("btnTags", btnTags);
		
		JLabel lblType = new JLabel("Type:");
		GridBagConstraints gbc_lblType = new GridBagConstraints();
		gbc_lblType.anchor = GridBagConstraints.EAST;
		gbc_lblType.insets = new Insets(0, 5, 5, 5);
		gbc_lblType.gridx = 0;
		gbc_lblType.gridy = 3;
		getContentPane().add(lblType, gbc_lblType);
		
		cmbxType = new JComboBox<SeqType>();
		GridBagConstraints gbc_cmbxType = new GridBagConstraints();
		gbc_cmbxType.gridwidth = 2;
		gbc_cmbxType.insets = new Insets(0, 0, 5, 0);
		gbc_cmbxType.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxType.gridx = 1;
		gbc_cmbxType.gridy = 3;
		getContentPane().add(cmbxType, gbc_cmbxType);
		editableEnable.addComponent("cmbxType", cmbxType);
		
		JLabel lblDefaultMedium = new JLabel("Default Medium:");
		GridBagConstraints gbc_lblDefaultMedium = new GridBagConstraints();
		gbc_lblDefaultMedium.anchor = GridBagConstraints.EAST;
		gbc_lblDefaultMedium.gridwidth = 2;
		gbc_lblDefaultMedium.insets = new Insets(0, 5, 5, 5);
		gbc_lblDefaultMedium.gridx = 0;
		gbc_lblDefaultMedium.gridy = 4;
		getContentPane().add(lblDefaultMedium, gbc_lblDefaultMedium);
		
		cmbxMedium = new JComboBox<MediumType>();
		GridBagConstraints gbc_cmbxMedium = new GridBagConstraints();
		gbc_cmbxMedium.insets = new Insets(0, 0, 5, 0);
		gbc_cmbxMedium.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxMedium.gridx = 2;
		gbc_cmbxMedium.gridy = 4;
		getContentPane().add(cmbxMedium, gbc_cmbxMedium);
		editableEnable.addComponent("cmbxMedium", cmbxMedium);
		
		JLabel lblDefaultCachePolicy = new JLabel("Default Cache Policy:");
		GridBagConstraints gbc_lblDefaultCachePolicy = new GridBagConstraints();
		gbc_lblDefaultCachePolicy.anchor = GridBagConstraints.EAST;
		gbc_lblDefaultCachePolicy.gridwidth = 2;
		gbc_lblDefaultCachePolicy.insets = new Insets(0, 5, 5, 5);
		gbc_lblDefaultCachePolicy.gridx = 0;
		gbc_lblDefaultCachePolicy.gridy = 5;
		getContentPane().add(lblDefaultCachePolicy, gbc_lblDefaultCachePolicy);
		
		cmbxCache = new JComboBox<CacheType>();
		GridBagConstraints gbc_cmbxCache = new GridBagConstraints();
		gbc_cmbxCache.insets = new Insets(0, 0, 5, 0);
		gbc_cmbxCache.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxCache.gridx = 2;
		gbc_cmbxCache.gridy = 5;
		getContentPane().add(cmbxCache, gbc_cmbxCache);
		editableEnable.addComponent("cmbxCache", cmbxCache);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 3;
		gbc_panel.insets = new Insets(0, 5, 5, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 6;
		getContentPane().add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JButton btnSave = new JButton("Apply");
		GridBagConstraints gbc_btnSave = new GridBagConstraints();
		gbc_btnSave.insets = new Insets(0, 0, 0, 5);
		gbc_btnSave.gridx = 1;
		gbc_btnSave.gridy = 0;
		panel.add(btnSave, gbc_btnSave);
		btnSave.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnApplyCallback();
			}
		});
		editableEnable.addComponent("btnSave", btnSave);
		
		JButton btnClose = new JButton("Close");
		GridBagConstraints gbc_btnClose = new GridBagConstraints();
		gbc_btnClose.gridx = 2;
		gbc_btnClose.gridy = 0;
		panel.add(btnClose, gbc_btnClose);
		btnClose.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnCloseCallback();
			}
		});
		globalEnable.addComponent("btnClose", btnClose);
		
		initComboboxes();
	}
	
	private void initComboboxes(){
		DefaultComboBoxModel<MediumType> mdl1 = new DefaultComboBoxModel<MediumType>();
		mdl1.addElement(MediumType.RAM);
		mdl1.addElement(MediumType.CART);
		mdl1.addElement(MediumType.DD);
		cmbxMedium.setModel(mdl1);
		cmbxMedium.setSelectedIndex(MEDIUM_CMBX_IDXS[Z64Sound.MEDIUM_CART]);
		
		DefaultComboBoxModel<CacheType> mdl2 = new DefaultComboBoxModel<CacheType>();
		mdl2.addElement(CacheType.TEMPORARY);
		mdl2.addElement(CacheType.PERSISTENT);
		mdl2.addElement(CacheType.PERMANENT);
		mdl2.addElement(CacheType.ANY);
		mdl2.addElement(CacheType.ANY_NOSYNC);
		cmbxCache.setModel(mdl2);
		cmbxCache.setSelectedIndex(CACHE_CMBX_IDXS[Z64Sound.CACHE_TEMPORARY]);
		
		DefaultComboBoxModel<SeqType> mdl3 = new DefaultComboBoxModel<SeqType>();
		mdl3.addElement(SeqType.NONE);
		mdl3.addElement(SeqType.BGM);
		mdl3.addElement(SeqType.BGM_PROG);
		mdl3.addElement(SeqType.BGM_PIECE);
		mdl3.addElement(SeqType.JINGLE);
		mdl3.addElement(SeqType.OCARINA);
		mdl3.addElement(SeqType.CUTSCENE);
		mdl3.addElement(SeqType.AMBIENT);
		mdl3.addElement(SeqType.SFX);
		mdl3.addElement(SeqType.INTRMUS);
		cmbxType.setModel(mdl3);
		cmbxType.setSelectedIndex(TYPE_CMBX_IDXS[ZeqerSeq.SEQTYPE_UNKNOWN]);
	}
	
	/*--- Getters ---*/
	
	public String getNameField(){return txtName.getText();}
	
	public boolean getFields(SeqTableEntry seqmeta){
		if(seqmeta == null) return false;
		if(!editable) return false;
		if(!changesApplied) return false;
		
		seqmeta.setEnumString(applEnum);
		seqmeta.setMedium((byte)applMed);
		seqmeta.setCache((byte)applCache);
		seqmeta.setSeqType(applType);
		
		seqmeta.clearTags();
		for(String tag : applTags){
			seqmeta.addTag(tag);
		}
		
		return true;
	}
	
	/*--- Setters ---*/
	
	public boolean setFields(SeqTableEntry seqmeta){
		tags.clear();
		if(seqmeta != null){
			int uid = seqmeta.getUID();
			String s = seqmeta.getName();
			
			if(s != null) txtName.setText(s);
			else{
				s = String.format("Sequence %08x", uid);
				txtName.setText(s);
			}
			
			s = seqmeta.getEnumString();
			if(s != null) txtEnum.setText(s);
			else txtEnum.setText(String.format("SEQ_%08x", uid));
			
			cmbxMedium.setSelectedIndex(MEDIUM_CMBX_IDXS[seqmeta.getMedium()]);
			cmbxCache.setSelectedIndex(CACHE_CMBX_IDXS[seqmeta.getCache()]);
			cmbxType.setSelectedIndex(TYPE_CMBX_IDXS[seqmeta.getSeqType()]);
			
			Collection<String> mytags = seqmeta.getTags();
			if(mytags != null) tags.addAll(mytags);
		}
		else{
			txtName.setText("<empty>");
			txtEnum.setText("<empty>");
			cmbxMedium.setSelectedIndex(MEDIUM_CMBX_IDXS[Z64Sound.MEDIUM_CART]);
			cmbxCache.setSelectedIndex(CACHE_CMBX_IDXS[Z64Sound.CACHE_TEMPORARY]);
			cmbxType.setSelectedIndex(TYPE_CMBX_IDXS[ZeqerSeq.SEQTYPE_UNKNOWN]);
		}
		
		reenable();
		return true;
	}
	
	/*--- Draw ---*/
	
	public void disableAll(){
		globalEnable.setEnabling(false);
		globalEnable.repaint();
	}
	
	public void reenable(){
		globalEnable.setEnabling(true);
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
	
	/*--- Callbacks ---*/
	
	private void btnTagsCallback(){
		ZeqerTagEditDialog dialog = new ZeqerTagEditDialog(parent.getParentFrame());
		dialog.loadTags(tags);
		
		dialog.showMe(this);
		
		if(editable && dialog.getExitSelection()){
			tags.clear();
			tags.addAll(dialog.getTags());
		}
	}
	
	private void btnApplyCallback(){
		if(!editable) return;
		
		//Send name to parent form
		String s = txtName.getText();
		if(s == null){
			s = "Untitled sequence";
			txtName.setText(s);
		}
		parent.setSeqNameLocal(s);
		
		//Fix enum string
		s = txtEnum.getText();
		if(s == null){
			s = "SEQ_UNKNOWN";
			txtEnum.setText(s);
		}
		s = ZeqerUtils.fixHeaderEnumID(s, true);
		txtEnum.setText(s);
		
		//Save state
		applEnum = txtEnum.getText();
		applTags.clear();
		applTags.addAll(tags);
		
		int idx = cmbxMedium.getSelectedIndex();
		MediumType med = cmbxMedium.getItemAt(idx);
		if(med != null){
			applMed = med.getValue();
		}
		
		idx = cmbxCache.getSelectedIndex();
		CacheType ct = cmbxCache.getItemAt(idx);
		if(ct != null){
			applCache = ct.getValue();
		}
		
		idx = cmbxType.getSelectedIndex();
		SeqType st = cmbxType.getItemAt(idx);
		if(st != null){
			applType = st.getValue();
		}
		
		changesApplied = true;
		reenable();
	}
	
	private void btnCloseCallback(){
		this.setVisible(false);
	}
	
}
