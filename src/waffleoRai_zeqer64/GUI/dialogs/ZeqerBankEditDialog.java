package waffleoRai_zeqer64.GUI.dialogs;

import javax.swing.JDialog;
import javax.swing.JFrame;

import waffleoRai_soundbank.nintendo.z64.Z64Bank;
import waffleoRai_soundbank.nintendo.z64.Z64Drum;
import waffleoRai_soundbank.nintendo.z64.Z64Instrument;
import waffleoRai_soundbank.nintendo.z64.Z64SoundEffect;
import waffleoRai_zeqer64.ZeqerBank;
import waffleoRai_zeqer64.ZeqerUtils;
import waffleoRai_zeqer64.GUI.ZeqerGUIUtils;
import waffleoRai_zeqer64.filefmt.ZeqerBankTable.BankTableEntry;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;
import waffleoRai_zeqer64.presets.ZeqerInstPreset;
import waffleoRai_zeqer64.presets.ZeqerPercPreset;
import waffleoRai_zeqer64.presets.ZeqerPercRegion;

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JScrollPane;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.border.BevelBorder;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.GUITools;
import waffleoRai_Sound.nintendo.Z64Sound;

import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.border.EtchedBorder;

public class ZeqerBankEditDialog extends JDialog{

	private static final long serialVersionUID = -5902985074514066407L;
	
	private static final int[] MEDIUM_CMBX_IDXS = {0, -1, 1, 2};
	private static final int[] CACHE_CMBX_IDXS = {2, 1, 0, 3, 4};
	
	public static final int MIN_WIDTH = 640;
	public static final int MIN_HEIGHT = 350;
	
	/*----- Inner Classes -----*/
	
	private static abstract class ProgramNode{
		public int slot;
		public String enumLabel;
		
		public abstract String getName();
		public abstract boolean isEmpty();
		public abstract void clear();
		
		public String toString(){
			String name = getName();
			String lbl = String.format("%03d - ", slot);
			if(name != null) lbl += name;
			else lbl += "<empty>";
			return lbl;
		}
	}
	
	private static class InstProgNode extends ProgramNode{
		public Z64Instrument data;
		
		public String getName(){
			if(data == null) return null;
			String n = data.getName();
			if(n == null || n.isEmpty()) return "[untitled]";
			return n;
		}
		
		public boolean isEmpty(){
			return data == null;
		}
		
		public void clear(){
			data = null;
			enumLabel = String.format("INST_%03d", slot);
		}
	}
	
	private static class PercProgNode extends ProgramNode{
		
		public ZeqerPercRegion[] data;
		
		public String getName(){
			if(data == null) return "<Percussion Unused>";
			return "Percussion";
		}
		
		public boolean isEmpty(){
			return data == null;
		}
		
		public void clear(){
			data = null;
			enumLabel = String.format("PROGRAM_PERCUSSION", slot);
		}
	}
	
	private static class SfxProgNode extends ProgramNode{
		
		public Z64SoundEffect[] data;
		public String[] slotEnums;
		
		public String getName(){
			if(data == null) return "<SFX Unused>";
			return "Sound Effects";
		}
		
		public boolean isEmpty(){
			return data == null;
		}
		
		public void clear(){
			data = null;
			enumLabel = String.format("PROGRAM_SFX", slot);
			slotEnums = null;
		}
	}
	
	private static enum MediumType{
		RAM(Z64Sound.MEDIUM_RAM),
		UNKNOWN(Z64Sound.MEDIUM_UNK),
		CART(Z64Sound.MEDIUM_CART),
		DD(Z64Sound.MEDIUM_DISK_DRIVE);
		
		private int value;
		
		private MediumType(int val){value = val;}
		public int getValue(){return value;}
		
		public String toString(){
			return ZeqerGUIUtils.getMediumString(value);
		}
	}
	
	private static enum CacheType{
		PERMANENT(Z64Sound.CACHE_PERMANENT),
		PERSISTENT(Z64Sound.CACHE_PERSISTENT),
		TEMPORARY(Z64Sound.CACHE_TEMPORARY),
		ANY(Z64Sound.CACHE_ANY),
		ANY_NOSYNC(Z64Sound.CACHE_ANYNOSYNCLOAD);
		
		private int value;
		
		private CacheType(int val){value = val;}
		public int getValue(){return value;}
		
		public String toString(){
			return ZeqerGUIUtils.getCacheString(value);
		}
	}
	
	/*----- Instance Variables -----*/
	
	private ZeqerCoreInterface core;
	
	private boolean editable = true;
	private boolean exitSelection = false;
	
	private ComponentGroup globalEnable;
	private ComponentGroup editableEnable;
	private ComponentGroup listEnable;
	
	private InstProgNode[] instNodes;
	private SfxProgNode sfxNode; //Program 126
	private PercProgNode percNode; //Program 127
	private Set<String> tags;
	
	private JFrame parent;
	private JList<ProgramNode> lstProgs;
	private JTextField txtName;
	private JTextField txtEnum;
	private JComboBox<MediumType> cmbxMedium;
	private JComboBox<CacheType> cmbxCache;
	
	/*----- Init -----*/
	
	public ZeqerBankEditDialog(JFrame parentFrame, ZeqerCoreInterface coreIFace){
		super(parentFrame, true);
		parent = parentFrame;
		core = coreIFace;

		globalEnable = new ComponentGroup();
		editableEnable = globalEnable.newChild();
		listEnable = globalEnable.newChild();
		
		tags = new HashSet<String>();
		
		initGUI();
		loadBankToForm(null);
	}
	
	private void initGUI(){
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		
		setTitle("Edit Soundfont");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel pnlMeta = new JPanel();
		pnlMeta.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlMeta = new GridBagConstraints();
		gbc_pnlMeta.insets = new Insets(5, 5, 5, 5);
		gbc_pnlMeta.fill = GridBagConstraints.BOTH;
		gbc_pnlMeta.gridx = 0;
		gbc_pnlMeta.gridy = 0;
		getContentPane().add(pnlMeta, gbc_pnlMeta);
		GridBagLayout gbl_pnlMeta = new GridBagLayout();
		gbl_pnlMeta.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_pnlMeta.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_pnlMeta.columnWeights = new double[]{0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_pnlMeta.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlMeta.setLayout(gbl_pnlMeta);
		
		JLabel lblName = new JLabel("Name:");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(5, 5, 5, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		pnlMeta.add(lblName, gbc_lblName);
		
		txtName = new JTextField();
		GridBagConstraints gbc_txtName = new GridBagConstraints();
		gbc_txtName.gridwidth = 3;
		gbc_txtName.insets = new Insets(5, 0, 5, 0);
		gbc_txtName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtName.gridx = 1;
		gbc_txtName.gridy = 0;
		pnlMeta.add(txtName, gbc_txtName);
		txtName.setColumns(10);
		editableEnable.addComponent("txtName", txtName);
		
		JLabel lblEnum = new JLabel("Enum:");
		GridBagConstraints gbc_lblEnum = new GridBagConstraints();
		gbc_lblEnum.anchor = GridBagConstraints.EAST;
		gbc_lblEnum.insets = new Insets(0, 5, 5, 5);
		gbc_lblEnum.gridx = 0;
		gbc_lblEnum.gridy = 1;
		pnlMeta.add(lblEnum, gbc_lblEnum);
		
		txtEnum = new JTextField();
		GridBagConstraints gbc_txtEnum = new GridBagConstraints();
		gbc_txtEnum.gridwidth = 3;
		gbc_txtEnum.insets = new Insets(0, 0, 5, 0);
		gbc_txtEnum.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtEnum.gridx = 1;
		gbc_txtEnum.gridy = 1;
		pnlMeta.add(txtEnum, gbc_txtEnum);
		txtEnum.setColumns(10);
		editableEnable.addComponent("txtEnum", txtEnum);
		
		JLabel lblDefaultMedium = new JLabel("Default Medium:");
		GridBagConstraints gbc_lblDefaultMedium = new GridBagConstraints();
		gbc_lblDefaultMedium.anchor = GridBagConstraints.EAST;
		gbc_lblDefaultMedium.insets = new Insets(0, 0, 5, 5);
		gbc_lblDefaultMedium.gridx = 1;
		gbc_lblDefaultMedium.gridy = 2;
		pnlMeta.add(lblDefaultMedium, gbc_lblDefaultMedium);
		
		cmbxMedium = new JComboBox<MediumType>();
		GridBagConstraints gbc_cmbxMedium = new GridBagConstraints();
		gbc_cmbxMedium.gridwidth = 2;
		gbc_cmbxMedium.insets = new Insets(0, 0, 5, 0);
		gbc_cmbxMedium.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxMedium.gridx = 2;
		gbc_cmbxMedium.gridy = 2;
		pnlMeta.add(cmbxMedium, gbc_cmbxMedium);
		editableEnable.addComponent("cmbxMedium", cmbxMedium);
		
		JLabel lblDefaultCachePolicy = new JLabel("Default Cache Policy:");
		GridBagConstraints gbc_lblDefaultCachePolicy = new GridBagConstraints();
		gbc_lblDefaultCachePolicy.anchor = GridBagConstraints.EAST;
		gbc_lblDefaultCachePolicy.insets = new Insets(0, 0, 5, 5);
		gbc_lblDefaultCachePolicy.gridx = 1;
		gbc_lblDefaultCachePolicy.gridy = 3;
		pnlMeta.add(lblDefaultCachePolicy, gbc_lblDefaultCachePolicy);
		
		cmbxCache = new JComboBox<CacheType>();
		GridBagConstraints gbc_cmbxCache = new GridBagConstraints();
		gbc_cmbxCache.gridwidth = 2;
		gbc_cmbxCache.insets = new Insets(0, 0, 5, 0);
		gbc_cmbxCache.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxCache.gridx = 2;
		gbc_cmbxCache.gridy = 3;
		pnlMeta.add(cmbxCache, gbc_cmbxCache);
		editableEnable.addComponent("cmbxCache", cmbxCache);
		
		JButton btnEditTags = new JButton("Edit Tags...");
		GridBagConstraints gbc_btnEditTags = new GridBagConstraints();
		gbc_btnEditTags.gridx = 3;
		gbc_btnEditTags.gridy = 4;
		pnlMeta.add(btnEditTags, gbc_btnEditTags);
		editableEnable.addComponent("btnEditTags", btnEditTags);
		btnEditTags.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnTagsCallback();
			}});
		
		JScrollPane spList = new JScrollPane();
		spList.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spList = new GridBagConstraints();
		gbc_spList.insets = new Insets(5, 5, 5, 0);
		gbc_spList.fill = GridBagConstraints.BOTH;
		gbc_spList.gridx = 1;
		gbc_spList.gridy = 0;
		getContentPane().add(spList, gbc_spList);
		listEnable.addComponent("spList", spList);
		
		lstProgs = new JList<ProgramNode>();
		spList.setViewportView(lstProgs);
		listEnable.addComponent("lstProgs", lstProgs);
		lstProgs.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() != MouseEvent.BUTTON1){
					rightClickSlotCallback(e.getX(), e.getY());
				}
			}});
		
		JPanel pnlButtons = new JPanel();
		GridBagConstraints gbc_pnlButtons = new GridBagConstraints();
		gbc_pnlButtons.gridwidth = 2;
		gbc_pnlButtons.fill = GridBagConstraints.BOTH;
		gbc_pnlButtons.gridx = 0;
		gbc_pnlButtons.gridy = 1;
		getContentPane().add(pnlButtons, gbc_pnlButtons);
		GridBagLayout gbl_pnlButtons = new GridBagLayout();
		gbl_pnlButtons.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_pnlButtons.rowHeights = new int[]{0, 0};
		gbl_pnlButtons.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlButtons.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlButtons.setLayout(gbl_pnlButtons);
		
		JButton btnPreview = new JButton("Preview...");
		GridBagConstraints gbc_btnPreview = new GridBagConstraints();
		gbc_btnPreview.insets = new Insets(5, 5, 5, 5);
		gbc_btnPreview.gridx = 0;
		gbc_btnPreview.gridy = 0;
		pnlButtons.add(btnPreview, gbc_btnPreview);
		globalEnable.addComponent("btnPreview", btnPreview);
		btnPreview.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnPreviewCallback();
			}});
		
		JButton btnSave = new JButton("Save");
		GridBagConstraints gbc_btnSave = new GridBagConstraints();
		gbc_btnSave.insets = new Insets(5, 0, 5, 5);
		gbc_btnSave.gridx = 2;
		gbc_btnSave.gridy = 0;
		pnlButtons.add(btnSave, gbc_btnSave);
		editableEnable.addComponent("btnSave", btnSave);
		btnSave.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnSaveCallback();
			}});
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(5, 0, 5, 5);
		gbc_btnCancel.gridx = 3;
		gbc_btnCancel.gridy = 0;
		pnlButtons.add(btnCancel, gbc_btnCancel);
		globalEnable.addComponent("btnCancel", btnCancel);
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnCancelCallback();
			}});
	
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
	}
	
	/*----- Getters -----*/
	
	public boolean getExitSelection(){return exitSelection;}
	
	public boolean loadIntoBankFromForm(ZeqerBank target){
		if(target == null) return false;
		if(!editable) return false;
		
		BankTableEntry meta = target.getTableEntry();
		if(meta != null){
			String s = txtName.getText();
			if(s != null && !s.isEmpty()){
				meta.setName(s);
			}
			
			s = txtEnum.getText();
			if(s != null && !s.isEmpty()){
				s = ZeqerUtils.fixHeaderEnumID(s, true);
				meta.setEnumString(s);
			}
			
			int selidx = cmbxMedium.getSelectedIndex();
			if(selidx >= 0){
				MediumType med = cmbxMedium.getItemAt(selidx);
				meta.setMedium(med.getValue());
			}
			
			selidx = cmbxCache.getSelectedIndex();
			if(selidx >= 0){
				CacheType cc = cmbxCache.getItemAt(selidx);
				meta.setCachePolicy(cc.getValue());
			}
			
			meta.clearTags();
			for(String tag : tags) meta.addTag(tag);
		}
		else return false;
		
		Z64Bank data = target.getBankData();
		if(data != null){
			data.clearContents();
			
			//Metadata
			int selidx = cmbxMedium.getSelectedIndex();
			if(selidx >= 0){
				MediumType med = cmbxMedium.getItemAt(selidx);
				data.setMedium(med.getValue());
			}
			
			selidx = cmbxCache.getSelectedIndex();
			if(selidx >= 0){
				CacheType cc = cmbxCache.getItemAt(selidx);
				data.setCachePolicy(cc.getValue());
			}
			
			for(int i = 0; i < 126; i++){
				if(instNodes[i].isEmpty()) continue;
				data.setInstrument(i, instNodes[i].data);
				data.setInstPresetEnumString(i, instNodes[i].enumLabel);
			}
			
			if(!percNode.isEmpty()){
				for(int i = 0; i < percNode.data.length; i++){
					if(percNode.data[i] == null) continue;
					Z64Drum drum = percNode.data[i].getDrumData();
					if(drum == null) continue;
					int min = percNode.data[i].getMinSlot();
					int max = percNode.data[i].getMaxSlot();
					data.setDrum(drum, min, max);
					
					for(int j = min; j <= max; j++){
						data.setDrumPresetEnumString(j, String.format("%s_%02d", percNode.data[i].getEnumStem(), j));
					}
				}
			}
			
			if(!sfxNode.isEmpty()){
				for(int i = 0; i < sfxNode.data.length; i++){
					if(sfxNode.data[i] == null) continue;
					data.setSFX(i, sfxNode.data[i]);
					data.setSFXPresetEnumString(i, sfxNode.enumLabel);
				}
			}
		}

		return true;
	}
	
	/*----- Setters -----*/
	
	public void setEditable(boolean b){
		editable = b;
		refreshGUI();
	}
	
	public void loadBankToForm(ZeqerBank source){
		//Instantiate slot nodes, if not already. Otherwise, clear them.
		tags.clear();
		if(instNodes == null){
			instNodes = new InstProgNode[126];
			for(int i = 0; i < 126; i++){
				instNodes[i] = new InstProgNode();
				instNodes[i].slot = i;
				instNodes[i].enumLabel = String.format("INST_%03d", i);
			}
		}
		else{
			for(int i = 0; i < 126; i++){
				instNodes[i].clear();
			}
		}
		
		if(sfxNode == null){
			sfxNode = new SfxProgNode();
			sfxNode.slot = 126;
			sfxNode.enumLabel = "PROGRAM_SFX";
		}
		else sfxNode.clear();
		
		if(percNode == null){
			percNode = new PercProgNode();
			percNode.slot = 127;
			percNode.enumLabel = "PROGRAM_PERCUSSION";
		}
		else percNode.clear();
		
		//Load in bank data, if source provided...
		if(source != null){
			BankTableEntry meta = source.getTableEntry();
			if(meta != null){
				String s = meta.getName();
				if(s != null) txtName.setText(s);
				else txtName.setText(String.format("Soundfont %08x", meta.getUID()));
				
				s = meta.getEnumString();
				if(s != null) txtEnum.setText(s);
				else txtEnum.setText(String.format("BNK_%08x", meta.getUID()));

				cmbxMedium.setSelectedIndex(MEDIUM_CMBX_IDXS[meta.getMedium()]);
				cmbxCache.setSelectedIndex(CACHE_CMBX_IDXS[meta.getCachePolicy()]);
				tags.addAll(meta.getTags());
			}
			else{
				txtName.setText("Untitled soundfont");
				txtEnum.setText("UNTITLED");
				cmbxMedium.setSelectedIndex(MEDIUM_CMBX_IDXS[Z64Sound.MEDIUM_CART]);
				cmbxCache.setSelectedIndex(CACHE_CMBX_IDXS[Z64Sound.CACHE_TEMPORARY]);
			}
			
			Z64Bank bankdata = source.getBankData();
			if(bankdata != null){
				int icount = bankdata.getEffectiveInstCount();
				for(int i = 0; i < icount; i++){
					Z64Instrument inst = bankdata.getInstrumentInSlot(i);
					if(inst != null){
						instNodes[i].data = inst.copy();
					}
					String e = bankdata.getInstPresetEnumString(i);
					if(e != null && !e.isEmpty()){
						instNodes[i].enumLabel = e;
					}
				}
				
				int pcount = bankdata.getEffectivePercCount();
				if(pcount > 0){
					ZeqerPercPreset pp = new ZeqerPercPreset(0); //Dummy used as shortcut for region consolidation
					for(int i = 0; i < pcount; i++){
						Z64Drum slotdrum = bankdata.getDrumInSlot(i);
						if(slotdrum != null) pp.setDrumToSlot(i, slotdrum);
					}
					pp.consolidateRegions();
					
					int rcount = pp.getRegionCount();
					percNode.data = new ZeqerPercRegion[rcount];
					for(int r = 0; r < rcount; r++){
						percNode.data[r] = pp.getRegion(r);
						
						Z64Drum drum = percNode.data[r].getDrumData();
						if(drum != null){
							percNode.data[r].setNameStem(drum.getName());
							percNode.data[r].setDrumData(drum.copy());
						}
						
						String estr = bankdata.getDrumSlotEnumString(percNode.data[r].getMinSlot());
						if(estr != null){
							int lastunderscore = estr.lastIndexOf('_');
							if(lastunderscore > 0){
								percNode.data[r].setEnumStem(estr.substring(0,lastunderscore));
							}
							else percNode.data[r].setEnumStem(estr);
						}
						else{
							percNode.data[r].setEnumStem("PERC_REG_" + r);
						}
					}
				}
				
				int xcount = bankdata.getEffectiveSFXCount();
				if(xcount > 0){
					sfxNode.data = new Z64SoundEffect[xcount];
					sfxNode.slotEnums = new String[xcount];
					for(int i = 0; i < xcount; i++){
						sfxNode.data[i] = bankdata.getSFXInSlot(i);
						sfxNode.slotEnums[i] = bankdata.getSFXSlotEnumString(i);
						
						if(sfxNode.data[i] != null){
							sfxNode.data[i] = sfxNode.data[i].copy();
						}
						
						if(sfxNode.slotEnums[i] == null || sfxNode.slotEnums[i].isEmpty()){
							if(sfxNode.data[i] != null){
								int g = i >>> 6;
								int s = i & 0x3F;
								sfxNode.slotEnums[i] = String.format("SFX_G%02d_%02d", g, s);
							}
						}
					}
				}	
			}
		}
		else{
			txtName.setText("<No font loaded>");
			txtEnum.setText("<No font loaded>");
			cmbxMedium.setSelectedIndex(MEDIUM_CMBX_IDXS[Z64Sound.MEDIUM_CART]);
			cmbxCache.setSelectedIndex(CACHE_CMBX_IDXS[Z64Sound.CACHE_TEMPORARY]);
		}
		
		refreshGUI();
	}
	
	/*----- Drawing -----*/
	
	private void refreshGUI(){
		updateProgramList();
		globalEnable.setEnabling(true);
		editableEnable.setEnabling(editable);
		globalEnable.repaint();
	}
	
	private void updateProgramList(){
		int selidx = lstProgs.getSelectedIndex();
		
		DefaultListModel<ProgramNode> model = new DefaultListModel<ProgramNode>();
		for(int i = 0; i < 126; i++){
			model.addElement(instNodes[i]);
		}
		model.addElement(sfxNode);
		model.addElement(percNode);
		
		lstProgs.setModel(model);
		lstProgs.setSelectedIndex(selidx);
		listEnable.repaint();
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
		this.setVisible(false);
		this.dispose();
	}
	
	/*----- Right Click Menu -----*/
	
	private class RCMenu extends JPopupMenu{
		private static final long serialVersionUID = 1066640952575668977L;
		
		private ProgramNode target;
		
		public RCMenu(ProgramNode node){
			target = node;
			
			JMenuItem opSet = new JMenuItem("Set Preset...");
			add(opSet);
			if(editable){
				opSet.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						menuSetPresetCallback(target);
					}});
			}
			else{
				opSet.setEnabled(false);
			}
			
			JMenuItem opEdit = new JMenuItem("Edit...");
			add(opEdit);
			if(!editable){
				opEdit.setText("View...");
				opEdit.setEnabled(!node.isEmpty());
			}
			opEdit.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					menuEditProgramCallback(target);
				}});
			
			
			JMenuItem opClear = new JMenuItem("Clear Program");
			add(opClear);
			if(editable){
				opClear.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						menuClearProgramCallback(target);
					}});
			}
			else{
				opClear.setEnabled(false);
			}
			
			JMenuItem opSavePreset = new JMenuItem("Save to Preset...");
			add(opSavePreset);
			opSavePreset.setEnabled(!node.isEmpty());
			opSavePreset.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					menuSaveToPresetCallback(target);
				}});
		}
		
	}
	
	/*----- Callbacks -----*/
	
	private void dummyCallback(){
		JOptionPane.showMessageDialog(this, "Sorry, this component doesn't work yet!", 
				"Notice", JOptionPane.INFORMATION_MESSAGE);
	}
	
	private void rightClickSlotCallback(int x, int y){
		if(lstProgs.isSelectionEmpty()) return;
		RCMenu menu = new RCMenu(lstProgs.getSelectedValue());
		menu.show(lstProgs, x, y);
	}
	
	private void menuSetPresetCallback(ProgramNode node){
		//TODO
		//Only enabled/present if font is editable
		if(!editable) return;
		if(node == null) return;
		dummyCallback();
	}
	
	private void menuEditProgramCallback(ProgramNode node){
		//Present whether or not font is editable, but option should say "view" instead of "edit"
		if(node == null) return;
		if(!editable && node.isEmpty()) return;
		
		if(node.slot < 126){
			if(!(node instanceof InstProgNode)) return;
			InstProgNode inode = (InstProgNode)node;
			
			ZeqerInstPreset ipre = new ZeqerInstPreset(0);
			ipre.loadData(inode.data);
			ipre.setName(inode.data.getName());
			ipre.setEnumLabel(inode.enumLabel);
			
			ZeqerInstEditDialog dialog = new ZeqerInstEditDialog(parent, core, ipre);
			dialog.setEditable(editable);
			dialog.showMe(this);
			
			if(editable & dialog.getExitSelection()){
				dialog.loadDataToInstrument(ipre);
				inode.data = ipre.getInstrument();
				inode.enumLabel = ipre.getEnumLabel();
				updateProgramList();
			}
		}
		else if(node.slot == 126){
			if(!(node instanceof SfxProgNode)) return;
			SfxProgNode sfxnode = (SfxProgNode)node;
			
			ZeqerBankSFXEditDialog dialog = new ZeqerBankSFXEditDialog(parent, core);
			dialog.setEditable(editable);
			dialog.loadSFXSetToForm(sfxnode.data, sfxnode.slotEnums);
			
			dialog.showMe(this);
			if(editable & dialog.getExitSelection()){
				int sfxcount = dialog.getSFXCount();
				sfxnode.clear();
				if(sfxcount > 0){
					sfxnode.data = new Z64SoundEffect[sfxcount];
					sfxnode.slotEnums = new String[sfxcount];
					
					for(int i = 0; i < sfxcount; i++){
						sfxnode.data[i] = dialog.getSFX(i);
						sfxnode.slotEnums[i] = dialog.getEnumLabel(i);
					}
				}
				
				updateProgramList();
			}
		}
		else if(node.slot == 127){
			if(!(node instanceof PercProgNode)) return;
			ZeqerDrumsetEditDialog dialog = new ZeqerDrumsetEditDialog(parent, core);
			dialog.setEditable(editable);
			PercProgNode pnode = (PercProgNode)node;
			int rcount = pnode.data.length;
			
			ZeqerPercPreset pre = new ZeqerPercPreset(0);
			pre.clearAndReallocRegions(rcount);
			for(int i = 0; i < rcount; i++) pre.setRegion(i, pnode.data[i]);
			pre.setEnumLabel(pnode.enumLabel);
			
			dialog.setDrum(pre);
			dialog.showMe(this);
			
			if(editable & dialog.getExitSelection()){
				dialog.loadDataIntoDrum(pre);
				pnode.clear();
				
				rcount = pre.getRegionCount();
				pnode.data = new ZeqerPercRegion[rcount];
				for(int r = 0; r < rcount; r++){
					pnode.data[r] = pre.getRegion(r);
				}
				pnode.enumLabel = pre.getEnumLabel();
				
				updateProgramList();
			}
		}
		else return;
	}
	
	private void menuClearProgramCallback(ProgramNode node){
		//Only enabled/present if font is editable
		//Have a confirm dialog
		
		if(node == null) return;
		if(node.isEmpty()) return;
		if(!editable) return;
		
		int res = JOptionPane.showConfirmDialog(this, 
				"Are you sure you want to set program " + node.slot + " to empty?", 
				"Clear Program Slot", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		
		if(res != JOptionPane.YES_OPTION) return;
		
		node.clear();
		updateProgramList();
	}
	
	private void menuSaveToPresetCallback(ProgramNode node){
		//TODO
		if(node == null) return;
		dummyCallback();
	}
	
	private void btnTagsCallback(){
		//TODO
		dummyCallback();
	}
	
	private void btnPreviewCallback(){
		//TODO
		//This opens up a (non-modal) standalone piano dialog
		//	Keep up until closed by user and linked to this dialog,
		//	that way user can change program in the main dialog and play it in piano dialog.
		dummyCallback();
	}
	
	private void btnSaveCallback(){
		if(!editable) return;
		exitSelection = true;
		closeMe();
	}
	
	private void btnCancelCallback(){
		exitSelection = false;
		closeMe();
	}

}
