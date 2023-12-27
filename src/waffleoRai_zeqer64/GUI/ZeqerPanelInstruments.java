package waffleoRai_zeqer64.GUI;

import javax.swing.JFrame;
import javax.swing.JPanel;

import waffleoRai_zeqer64.ZeqerPreset;
import waffleoRai_zeqer64.GUI.dialogs.InstTypeMiniDialog;
import waffleoRai_zeqer64.GUI.dialogs.ZeqerDrumEditDialog;
import waffleoRai_zeqer64.GUI.dialogs.ZeqerDrumsetEditDialog;
import waffleoRai_zeqer64.GUI.dialogs.ZeqerInstEditDialog;
import waffleoRai_zeqer64.GUI.filters.FlagFilterPanel;
import waffleoRai_zeqer64.GUI.filters.TagFilterPanel;
import waffleoRai_zeqer64.GUI.filters.TextFilterPanel;
import waffleoRai_zeqer64.GUI.filters.ZeqerFilter;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;
import waffleoRai_zeqer64.presets.ZeqerDrumPreset;
import waffleoRai_zeqer64.presets.ZeqerInstPreset;
import waffleoRai_zeqer64.presets.ZeqerPercPreset;
import waffleoRai_zeqer64.presets.ZeqerSFXPreset;

import java.awt.GridBagLayout;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.WriterPanel;
import waffleoRai_SeqSound.MIDI;
import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_Sound.nintendo.Z64Sound.Z64Tuning;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.VoidCallbackMethod;
import waffleoRai_soundbank.nintendo.z64.Z64Drum;
import waffleoRai_soundbank.nintendo.z64.Z64Instrument;

import javax.swing.DefaultListModel;
import javax.swing.JButton;

public class ZeqerPanelInstruments extends JPanel{
	
	//TODO need a subpanel class for playing on the piano or playing using a simple seq
	//	Playback will probably have to involve sending a short command to native code for synthesis
	
	private static final long serialVersionUID = -6390624749239280331L;
	
	public static final String SAMPLEPNL_LAST_IMPORT_PATH = "ZINSTPNL_LASTIMPORT";
	public static final String SAMPLEPNL_LAST_EXPORT_PATH = "ZINSTPNL_LASTEXPORT";
	
	public static final int FLAGIDX_INST = 0;
	public static final int FLAGIDX_PERC = 1;
	public static final int FLAGIDX_SFX = 2;
	
	private static final String STRKEY_BTN_NEW = "PNLINST_BTN_NEW";
	private static final String STRKEY_BTN_EDIT = "PNLINST_BTN_EDIT";
	private static final String STRKEY_BTN_DUP = "PNLINST_BTN_DUP";
	private static final String STRKEY_BTN_DELETE = "PNLINST_BTN_DELETE";
	
	private static final String STRKEY_FLTTYPE_INST = "PNLINST_FLAG_INST";
	private static final String STRKEY_FLTTYPE_DRUM = "PNLINST_FLAG_DRUM";
	private static final String STRKEY_FLTTYPE_PERC = "PNLINST_FLAG_PERC";
	
	private static final String STRKEY_INFO_ERROR_T = "PNLINST_INFOPNL_ERROR_T";
	private static final String STRKEY_INFO_ERROR_M = "PNLINST_INFOPNL_ERROR_M";
	private static final String STRKEY_INFO_NOPRS = "PNLINST_INFOPNL_NOPRS";
	
	private static final String STRKEY_IFIELD_TYPE = "PNLINST_INFOFIELD_TYPE";
	private static final String STRKEY_IFIELD_ENUM = "PNLINST_INFOFIELD_ENUM";
	private static final String STRKEY_IFIELD_RTIME = "PNLINST_INFOFIELD_RELTIME";
	private static final String STRKEY_IFIELD_RUNIT = "PNLINST_INFOFIELD_RELUNIT";
	private static final String STRKEY_IFIELD_REG = "PNLINST_INFOFIELD_REGIONS";
	private static final String STRKEY_IFIELD_SLOTSUSED = "PNLINST_INFOFIELD_PSLOTSUSED";
	private static final String STRKEY_IFIELD_PAN = "PNLINST_INFOFIELD_PAN";
	private static final String STRKEY_IFIELD_SMPL = "PNLINST_INFOFIELD_SAMPLE";
	private static final String STRKEY_IFIELD_NOSMPL = "PNLINST_INFOFIELD_SAMPLE_BAD";
	private static final String STRKEY_IFIELD_ROOTTUNE = "PNLINST_INFOFIELD_TUNE_ROOT";
	private static final String STRKEY_IFIELD_FINETUNE = "PNLINST_INFOFIELD_TUNE_FINE";
	private static final String STRKEY_IFIELD_FINEUNIT = "PNLINST_INFOFIELD_TUNE_FINE_UNITS";
	private static final String STRKEY_IFIELD_TAGS = "PNLINST_INFOFIELD_TAGS";
	private static final String STRKEY_IFIELD_NOTAGS = "PNLINST_INFOFIELD_TAGS_NONE";
	
	private static final String STRKEY_IFIELD_PTYPE_I = "PNLINST_INFOFIELD_PTYPE_I";
	private static final String STRKEY_IFIELD_PTYPE_D = "PNLINST_INFOFIELD_PTYPE_D";
	private static final String STRKEY_IFIELD_PTYPE_P = "PNLINST_INFOFIELD_PTYPE_P";
	private static final String STRKEY_IFIELD_PTYPE_X = "PNLINST_INFOFIELD_PTYPE_X";
	private static final String STRKEY_IFIELD_PTYPE_U = "PNLINST_INFOFIELD_PTYPE_U";
	
	/*----- Inner Classes -----*/
	
	private static class InstNode implements Comparable<InstNode>{

		public ZeqerPreset preset;
		
		public InstNode(ZeqerPreset p){preset = p;}
		
		public boolean equals(Object o){
			return this == o;
		}
		
		public int compareTo(InstNode o) {
			if(o == null) return 1;
			if(this.preset == null){
				if(o.preset == null) return 0;
			}
			if(o.preset == null) return 1;
			
			String name = preset.getName();
			if(name == null){
				if(o.preset.getName() == null) return 0;
				return -1;
			}
			
			return name.compareTo(o.preset.getName());
		}
		
		public String toString(){
			if(preset == null) return "<null>";
			return preset.getName();
		}
		
	}
	
	/*----- Instance Variables -----*/
	
	private ZeqerCoreInterface core;
	private boolean presetsEditable = false;
	
	private JFrame parent;
	private ComponentGroup globalEnable;
	private ComponentGroup lstSelectedEnable;
	
	private List<InstNode> allPresets; //Source list
	
	private FilterListPanel<InstNode> pnlFilt;
	private TagFilterPanel<InstNode> pnlTags; //For refreshing/adding tags
	private WriterPanel pnlInfo;
	private JList<InstNode> lstPresets;
	
	/*----- Init -----*/
	
	public ZeqerPanelInstruments(JFrame parent_frame, ZeqerCoreInterface core_iface, boolean editable_mode){
		parent = parent_frame;
		core = core_iface;
		presetsEditable = editable_mode;
		globalEnable = new ComponentGroup();
		lstSelectedEnable = new ComponentGroup();
		initGUI();
		loadPresetsFromCore();
		clearAllFilters();
	}
	
	private void initGUI(){
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		pnlFilt = new FilterListPanel<InstNode>();
		GridBagConstraints gbc_pnlFilt = new GridBagConstraints();
		gbc_pnlFilt.insets = new Insets(0, 0, 5, 5);
		gbc_pnlFilt.fill = GridBagConstraints.BOTH;
		gbc_pnlFilt.gridx = 0;
		gbc_pnlFilt.gridy = 0;
		add(pnlFilt, gbc_pnlFilt);
		
		JPanel pnlList = new JPanel();
		GridBagConstraints gbc_pnlList = new GridBagConstraints();
		gbc_pnlList.insets = new Insets(0, 0, 5, 0);
		gbc_pnlList.fill = GridBagConstraints.BOTH;
		gbc_pnlList.gridx = 1;
		gbc_pnlList.gridy = 0;
		add(pnlList, gbc_pnlList);
		GridBagLayout gbl_pnlList = new GridBagLayout();
		gbl_pnlList.columnWidths = new int[]{0, 0};
		gbl_pnlList.rowHeights = new int[]{0, 0, 0, 0};
		gbl_pnlList.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlList.rowWeights = new double[]{1.0, 1.0, 0.0, Double.MIN_VALUE};
		pnlList.setLayout(gbl_pnlList);
		
		JScrollPane spList = new JScrollPane();
		spList.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spList = new GridBagConstraints();
		gbc_spList.insets = new Insets(5, 5, 5, 5);
		gbc_spList.fill = GridBagConstraints.BOTH;
		gbc_spList.gridx = 0;
		gbc_spList.gridy = 0;
		pnlList.add(spList, gbc_spList);
		globalEnable.addComponent("spList", spList);
		
		lstPresets = new JList<InstNode>();
		spList.setViewportView(lstPresets);
		globalEnable.addComponent("lstPresets", lstPresets);
		lstPresets.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e) {
				lstPresetsSelectCallback();
			}
		});
		
		pnlInfo = new WriterPanel();
		GridBagConstraints gbc_pnlInfo = new GridBagConstraints();
		gbc_pnlInfo.insets = new Insets(0, 0, 5, 0);
		gbc_pnlInfo.fill = GridBagConstraints.BOTH;
		gbc_pnlInfo.gridx = 0;
		gbc_pnlInfo.gridy = 1;
		pnlList.add(pnlInfo, gbc_pnlInfo);
		globalEnable.addComponent("pnlInfo", pnlInfo);
		
		JPanel pnlCtrl = new JPanel();
		GridBagConstraints gbc_pnlCtrl = new GridBagConstraints();
		gbc_pnlCtrl.fill = GridBagConstraints.BOTH;
		gbc_pnlCtrl.gridx = 0;
		gbc_pnlCtrl.gridy = 2;
		pnlList.add(pnlCtrl, gbc_pnlCtrl);
		GridBagLayout gbl_pnlCtrl = new GridBagLayout();
		gbl_pnlCtrl.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_pnlCtrl.rowHeights = new int[]{0, 0};
		gbl_pnlCtrl.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlCtrl.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlCtrl.setLayout(gbl_pnlCtrl);
		
		JButton btnNew = new JButton(getString(STRKEY_BTN_NEW));
		GridBagConstraints gbc_btnNew = new GridBagConstraints();
		gbc_btnNew.insets = new Insets(0, 0, 0, 5);
		gbc_btnNew.gridx = 0;
		gbc_btnNew.gridy = 0;
		pnlCtrl.add(btnNew, gbc_btnNew);
		if(presetsEditable){
			globalEnable.addComponent("btnNew", btnNew);
		}
		else{
			btnNew.setEnabled(false);
			btnNew.setVisible(false);
		}
		btnNew.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnNewCallback();
			}});
		
		JButton btnDup = new JButton(getString(STRKEY_BTN_DUP));
		GridBagConstraints gbc_btnDup = new GridBagConstraints();
		gbc_btnDup.insets = new Insets(0, 0, 0, 5);
		gbc_btnDup.gridx = 1;
		gbc_btnDup.gridy = 0;
		pnlCtrl.add(btnDup, gbc_btnDup);
		if(presetsEditable){
			globalEnable.addComponent("btnDup", btnDup);
			lstSelectedEnable.addComponent("btnDup", btnDup);
		}
		else{
			btnDup.setEnabled(false);
			btnDup.setVisible(false);
		}
		btnDup.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnDuplicateCallback();
			}});
		
		JButton btnEdit = new JButton(getString(STRKEY_BTN_EDIT));
		GridBagConstraints gbc_btnEdit = new GridBagConstraints();
		gbc_btnEdit.insets = new Insets(0, 0, 0, 5);
		gbc_btnEdit.gridx = 2;
		gbc_btnEdit.gridy = 0;
		pnlCtrl.add(btnEdit, gbc_btnEdit);
		if(presetsEditable){
			globalEnable.addComponent("btnEdit", btnEdit);
			lstSelectedEnable.addComponent("btnEdit", btnEdit);
		}
		else{
			btnEdit.setEnabled(false);
			btnEdit.setVisible(false);
		}
		btnEdit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnEditCallback();
				//dummyCallback();
			}});
		
		JButton btnDelete = new JButton(getString(STRKEY_BTN_DELETE));
		GridBagConstraints gbc_btnDelete = new GridBagConstraints();
		gbc_btnDelete.gridx = 4;
		gbc_btnDelete.gridy = 0;
		pnlCtrl.add(btnDelete, gbc_btnDelete);
		if(presetsEditable){
			globalEnable.addComponent("btnDelete", btnDelete);
			lstSelectedEnable.addComponent("btnDelete", btnDelete);
		}
		else{
			btnDelete.setEnabled(false);
			btnDelete.setVisible(false);
		}
		btnDelete.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnDeleteCallback();
				//dummyCallback();
			}});
		
		//TODO
		JPanel pnlPlay = new JPanel();
		pnlPlay.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_pnlPlay = new GridBagConstraints();
		gbc_pnlPlay.gridwidth = 2;
		gbc_pnlPlay.fill = GridBagConstraints.BOTH;
		gbc_pnlPlay.gridx = 0;
		gbc_pnlPlay.gridy = 1;
		add(pnlPlay, gbc_pnlPlay);
		
		addFilterPanels();
		
		lstSelectedEnable.setEnabling(!lstPresets.isSelectionEmpty());
	}
	
	private void addFilterPanels(){
		
		//Preset type
		FlagFilterPanel<InstNode> fpnl2 = new FlagFilterPanel<InstNode>(core.getString(ZeqerGUIUtils.STRKEY_FILPNL_FLAGS));
		fpnl2.addSwitch(new ZeqerFilter<InstNode>(){
			public boolean itemPasses(InstNode item){
				if(item == null) return false;
				if(item.preset == null) return false;
				return (item.preset instanceof ZeqerInstPreset);
			}
		}, getString(STRKEY_FLTTYPE_INST), true);
		fpnl2.addSwitch(new ZeqerFilter<InstNode>(){
			public boolean itemPasses(InstNode item){
				if(item == null) return false;
				if(item.preset == null) return false;
				return (item.preset instanceof ZeqerDrumPreset);
			}
		}, getString(STRKEY_FLTTYPE_DRUM), true);
		fpnl2.addSwitch(new ZeqerFilter<InstNode>(){
			public boolean itemPasses(InstNode item){
				if(item == null) return false;
				if(item.preset == null) return false;
				return (item.preset instanceof ZeqerPercPreset);
			}
		}, getString(STRKEY_FLTTYPE_PERC), true);
		pnlFilt.addPanel(fpnl2);
		
		//Text search
		TextFilterPanel<InstNode> fpnl1 = new TextFilterPanel<InstNode>(0);
		fpnl1.setSearchFilter(new ZeqerFilter<InstNode>(){
			public boolean itemPasses(InstNode item, String txt, long flags){
				if(item == null) return false;
				if(txt == null || txt.isEmpty()) return true;
				String name = item.preset.getName();
				if(name == null) return false;
				return name.toLowerCase().contains(txt.toLowerCase());
			}
		});
		pnlFilt.addPanel(fpnl1);
		
		//Tag panel
		pnlTags = new TagFilterPanel<InstNode>(parent, core.getString(ZeqerGUIUtils.STRKEY_FILPNL_TAGS));
		pnlFilt.addPanel(pnlTags);
		
		//Might add flag panels too?
		
		pnlFilt.addRefilterCallback(new VoidCallbackMethod(){
			public void doMethod() {refilter();}
		});
	}
	
	/*----- Getters -----*/
	
	protected String getString(String key){
		if(core == null) return key;
		return core.getString(key);
	}
	
	public ZeqerPreset getSelectedPreset(){
		if(lstPresets.isSelectionEmpty()) return null;
		InstNode node = lstPresets.getSelectedValue();
		if(node == null) return null;
		return node.preset;
	}
	
	/*----- Setters -----*/
	
	/*----- Misc. -----*/
	
	private void loadPresetsFromCore(){
		if(core != null){
			List<ZeqerPreset> corePresets = core.getAllInstPresets();
			allPresets = new ArrayList<InstNode>(corePresets.size()+1);
			for(ZeqerPreset p : corePresets){
				allPresets.add(new InstNode(p));
			}
			Collections.sort(allPresets);
		}
		
		updateTagPool();
	}
	
	private void updateTagPool(){
		Set<String> tagpool = new HashSet<String>();
		if(allPresets != null){
			for(InstNode node : allPresets){
				List<String> tags = node.preset.getAllTags();
				if(tags != null){
					tagpool.addAll(tags);
				}
			}
		}
		pnlTags.addTagPool(tagpool, new ZeqerFilter<InstNode>(){
			public boolean itemPasses(InstNode item, String txt){
				if(item == null || item.preset == null) return false;
				List<String> tags = item.preset.getAllTags();
				if(tags == null) return false;
				return tags.contains(txt);
			}
		});
		pnlTags.repaint();
		refilter();
	}
	
	/*----- GUI Management -----*/
	
	private void clearAllFilters(){
		pnlFilt.clearFilterSelections();
		refilter();
	}
	
	public void refreshPresetPool(){
		setWait();
		loadPresetsFromCore();
		unsetWait();
	}
	
	public void refilter(){
		DefaultListModel<InstNode> mdl = new DefaultListModel<InstNode>();
		if(allPresets != null){
			for(InstNode node : allPresets){
				if(pnlFilt.itemPassesFilters(node)){
					mdl.addElement(node);
				}
			}
		}
		lstPresets.setModel(mdl);
		lstPresets.setSelectedIndex(-1);
		lstPresets.repaint();
		updateInfoPanel(null);
	}
	
	private void writeInstTuningInfo(float raw_tune, Writer writer) throws IOException{
		Z64Tuning cmn_tune = Z64Sound.calculateTuning((byte)60, raw_tune);
		
		int rootkey = cmn_tune.root_key;
		int octave = (rootkey / 12) - 1;
		String notename = MIDI.NOTES[rootkey % 12];
		
		writer.write(notename + octave + " (" + rootkey + ") ");
		
		if(cmn_tune.fine_tune >= 0){
			writer.write("+" + cmn_tune.fine_tune);
		}
		else writer.write(Byte.toString(cmn_tune.fine_tune));
		
		writer.write(" " + getString(STRKEY_IFIELD_FINEUNIT));
		
	}
	
	private void writeInstInfo(InstNode selection, Writer writer) throws IOException{
		writer.write(getString(STRKEY_IFIELD_PTYPE_I) + "\n");
		//Type specific...
		ZeqerInstPreset ipreset = (ZeqerInstPreset)selection.preset;
		writer.write(getString(STRKEY_IFIELD_ENUM) + " " + ipreset.getEnumLabel() + "\n");
		
		Z64Instrument inst = ipreset.getInstrument();
		if(inst != null){
			int relraw = Byte.toUnsignedInt(inst.getDecay());
			writer.write(getString(STRKEY_IFIELD_RTIME) + " " + 
					relraw + " (" +
					Z64Sound.releaseValueToMillis(relraw) + " " + getString(STRKEY_IFIELD_RUNIT) + ")\n");
			
			writer.write(getString(STRKEY_IFIELD_REG) + " -- \n");
			String sname = null;
			Z64WaveInfo smpl = inst.getSampleLow();
			if(smpl != null){
				writer.write("0 - " + inst.getLowRangeTop() + ":\t");
				sname = smpl.getName();
				if(sname != null) writer.write(sname);
				else writer.write(String.format("(%08x)", smpl.getUID()));
				
				float raw_tune = inst.getTuningLow();
				writer.write(" [");
				writeInstTuningInfo(raw_tune, writer);
				
				writer.write("]\n");
				//Start middle
				writer.write((inst.getLowRangeTop() + 1) + " - ");
			}
			else writer.write("0 - ");
			
			if(inst.getSampleHigh() != null){
				writer.write((inst.getHighRangeBottom() - 1) + ":\t");
			}
			else writer.write("127:\t");
			
			
			smpl = inst.getSampleMiddle();
			sname = smpl.getName();
			if(sname != null) writer.write(sname);
			else writer.write(String.format("(%08x)", smpl.getUID()));
			
			float raw_tune = inst.getTuningMiddle();
			writer.write(" [");
			writeInstTuningInfo(raw_tune, writer);
			writer.write("]\n");
			
			smpl = inst.getSampleHigh();
			if(smpl != null){
				writer.write(inst.getHighRangeBottom() + " - 127:\t");
				sname = smpl.getName();
				if(sname != null) writer.write(sname);
				else writer.write(String.format("(%08x)", smpl.getUID()));
				
				raw_tune = inst.getTuningHigh();
				writer.write(" [");
				writeInstTuningInfo(raw_tune, writer);
				writer.write("]\n");
			}
		}
	}
	
	private void writeDrumInfo(InstNode selection, Writer writer)  throws IOException{
		writer.write(getString(STRKEY_IFIELD_PTYPE_D) + "\n");
		ZeqerDrumPreset dpreset = (ZeqerDrumPreset)selection.preset;
		writer.write(getString(STRKEY_IFIELD_ENUM) + " " + dpreset.getEnumLabel() + "\n");
		
		Z64Drum drum = dpreset.getData();
		if(drum != null){
			int relraw = Byte.toUnsignedInt(drum.getDecay());
			writer.write(getString(STRKEY_IFIELD_RTIME) + " " + 
					relraw + " (" +
					Z64Sound.releaseValueToMillis(relraw) + " " + getString(STRKEY_IFIELD_RUNIT) + ")\n");
			writer.write(getString(STRKEY_IFIELD_PAN) + " " + 
					ZeqerGUIUtils.pan2String(drum.getPan(), core) + String.format(" (0x%02x)\n", drum.getPan()));
			
			Z64WaveInfo smpl = drum.getSample();
			writer.write(getString(STRKEY_IFIELD_SMPL) + " ");
			if(smpl != null){
				String name = smpl.getName();
				if(name != null) writer.write(name);
				else writer.write(String.format("(%08x)", smpl.getUID()));
			}
			else writer.write("<" + getString(STRKEY_IFIELD_NOSMPL) + ">");
			writer.write("\n");
			
			int ukey = drum.getRootKey();
			int octave = (ukey/12) - 1;
			String note = MIDI.NOTES[ukey%12];
			writer.write(getString(STRKEY_IFIELD_ROOTTUNE) + " " + note + octave + " (" + ukey + ")\n");
			writer.write(getString(STRKEY_IFIELD_FINETUNE) + " " + drum.getFineTune() + " " + getString(STRKEY_IFIELD_FINEUNIT) + "\n");
		}
	}
	
	private void writePercSetInfo(InstNode selection, Writer writer) throws IOException{
		writer.write(getString(STRKEY_IFIELD_PTYPE_P) + "\n");
		ZeqerPercPreset perc = (ZeqerPercPreset)selection.preset;
		writer.write(getString(STRKEY_IFIELD_REG) + " " + perc.getRegionCount() + "\n");
		writer.write(getString(STRKEY_IFIELD_SLOTSUSED) + " " + perc.getMaxUsedSlotCount() + "\n");
	}
	
	private void updateInfoPanel(InstNode selection){
		try{
			pnlInfo.clear();
			Writer writer = pnlInfo.getWriter();
			if(selection == null || selection.preset == null){
				writer.write("<" + getString(STRKEY_INFO_NOPRS) + ">\n");
			}
			else{
				writer.write(selection.preset.getName() + "\n");
				writer.write("UID: " + String.format("%08x\n", selection.preset.getUID()));
				writer.write(getString(STRKEY_IFIELD_TYPE) + " ");
				if(selection.preset instanceof ZeqerInstPreset){
					writeInstInfo(selection, writer);
				}
				else if(selection.preset instanceof ZeqerPercPreset){
					writePercSetInfo(selection, writer);
				}
				else if(selection.preset instanceof ZeqerDrumPreset){
					writeDrumInfo(selection, writer);
				}
				else if(selection.preset instanceof ZeqerSFXPreset){
					writer.write(getString(STRKEY_IFIELD_PTYPE_X) + "\n");
				}
				else{
					writer.write(getString(STRKEY_IFIELD_PTYPE_U) + "\n");
				}
				
				//Tags
				writer.write(getString(STRKEY_IFIELD_TAGS) + " ");
				boolean first = true;
				List<String> tags = selection.preset.getAllTags();
				if(tags != null && !tags.isEmpty()){
					for(String tag : tags){
						if(!first) writer.write(";");
						writer.write(tag);
						first = false;	
					}
				}
				else writer.write("<" + getString(STRKEY_IFIELD_NOTAGS) + ">");
				writer.write("\n");
			}
			writer.close();
		}
		catch(Exception ex){
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, getString(STRKEY_INFO_ERROR_M),
						getString(STRKEY_INFO_ERROR_T), JOptionPane.ERROR_MESSAGE);
		}
		pnlInfo.repaint();
	}
	
	public void disableAll(){
		globalEnable.setEnabling(false);
		pnlFilt.disableAll();
		globalEnable.repaint();
		pnlFilt.repaint();
	}
	
	public void reenable(){
		globalEnable.setEnabling(true);
		pnlFilt.enableAll();
		lstSelectedEnable.setEnabling(!lstPresets.isSelectionEmpty());
		
		globalEnable.repaint();
		pnlFilt.repaint();
	}
	
	public void setWait(){
		disableAll();
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	public void unsetWait(){
		reenable();
		setCursor(null);
	}
	
	/*----- Callbacks Internal -----*/
	
	private InstNode newInstAction(ZeqerInstPreset template){
		ZeqerInstPreset instp = null;
		if(template != null){
			//Duplicate
			instp = template.copy();
		}
		else{
			//New
			instp = new ZeqerInstPreset();
			instp.generateRandomSpecs();
			instp.setName(String.format("Instrument %08x", instp.getUID()));
		}
		
		ZeqerInstEditDialog dialog = new ZeqerInstEditDialog(parent, core);
		dialog.loadInstrument(instp);
		dialog.showMe(this);
		//Disposes itself in closeMe()
		
		InstNode inode = null;
		if(dialog.getExitSelection()){
			int result = dialog.loadDataToInstrument(instp);
			
			if(result == ZeqerInstEditDialog.INSTLOAD_ERR_NONE){
				if(core != null){
					core.addUserPreset(instp);
				}
				inode = new InstNode(instp);
				
			}
			else{
				String errmsg = "Internal Error (Unknown)";
				switch(result){
				case ZeqerInstEditDialog.INSTLOAD_NULL_INST:
					errmsg = "Cannot load to null instrument.";
					break;
				case ZeqerInstEditDialog.INSTLOAD_INVALID_SAMPLE:
					errmsg = "One or more sound samples are invalid.";
					break;
				case ZeqerInstEditDialog.INSTLOAD_INVALID_FINETUNE:
					errmsg = "One or more fine tune values are invalid "
							+ "(Must be integer between -100 and 100).";
					break;
				case ZeqerInstEditDialog.INSTLOAD_INVALID_RELEASE:
					errmsg = "Release time is invalid.";
					break;
				}
				
				JOptionPane.showMessageDialog(this, 
						"ERROR: " + errmsg, 
						"Preset Creation Failed", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		return inode;
	}
	
	private InstNode newDrumAction(ZeqerDrumPreset template){
		ZeqerDrumPreset drump = null;
		if(template != null){
			drump = template.copy();
		}
		else{
			drump = new ZeqerDrumPreset();
		}
		
		ZeqerDrumEditDialog dialog = new ZeqerDrumEditDialog(parent, core);
		dialog.loadDrumData(drump);
		dialog.showMe(this);
		
		InstNode inode = null;
		if(dialog.getExitSelection()){
			dialog.loadDataInto(drump);
			if(core != null){
				core.addUserPreset(drump);
			}
			inode = new InstNode(drump);
		}
		
		return inode;
	}
	
	private InstNode newDrumsetAction(ZeqerPercPreset template){
		ZeqerPercPreset percp = null;
		if(template != null){
			percp = template.copy();
		}
		else{
			Random r = new Random();
			percp = new ZeqerPercPreset(r.nextInt());
		}
		
		ZeqerDrumsetEditDialog dialog = new ZeqerDrumsetEditDialog(parent, core);
		dialog.setDrum(percp);
		dialog.showMe(this);
		//Disposes itself in closeMe()

		InstNode inode = null;
		if(dialog.getExitSelection()){
			dialog.loadDataIntoDrum(percp);
			if(core != null){
				core.addUserPreset(percp);
			}
			inode = new InstNode(percp);
		}
		
		return inode;
	}
	
	/*----- Callbacks -----*/
	
	private void lstPresetsSelectCallback(){
		setWait();
		if(!lstPresets.isSelectionEmpty()){
			updateInfoPanel(lstPresets.getSelectedValue());
		}
		else updateInfoPanel(null);
		unsetWait();
	}
	
	private void btnDuplicateCallback(){
		if(!presetsEditable) return;
		
		ZeqerPreset sel = getSelectedPreset();
		if(sel == null){
			JOptionPane.showMessageDialog(this, 
					"Please select a preset to duplicate.", 
					"Duplicate Preset", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		setWait();
		
		InstNode inode = null;
		switch(sel.getType()){
		case ZeqerPreset.PRESET_TYPE_INST:
			if(sel instanceof ZeqerInstPreset){
				inode = newInstAction((ZeqerInstPreset)sel);
			}
			else{
				JOptionPane.showMessageDialog(this, 
						"Internal Error: Selection is marked as instrument preset,"
						+ " but is not instrument.", 
						"Corrupted Preset", JOptionPane.ERROR_MESSAGE);
			}
			break;
		case ZeqerPreset.PRESET_TYPE_DRUM:
			if(sel instanceof ZeqerDrumPreset){
				inode = newDrumAction((ZeqerDrumPreset)sel);
			}
			else{
				JOptionPane.showMessageDialog(this, 
						"Internal Error: Selection is marked as drum preset,"
						+ " but is not drum.", 
						"Corrupted Preset", JOptionPane.ERROR_MESSAGE);
			}
			break;
		case ZeqerPreset.PRESET_TYPE_PERC:
			if(sel instanceof ZeqerPercPreset){
				inode = newDrumsetAction((ZeqerPercPreset)sel);
			}
			else{
				JOptionPane.showMessageDialog(this, 
						"Internal Error: Selection is marked as drumset preset,"
						+ " but is not drumset.", 
						"Corrupted Preset", JOptionPane.ERROR_MESSAGE);
			}
			break;
		default:
			JOptionPane.showMessageDialog(this, 
					"Preset type not recognized. Cannot duplicate.", 
					"Duplicate Preset", JOptionPane.ERROR_MESSAGE);
			unsetWait();
			return;
		}
		
		//Add to GUI
		if(inode != null){
			allPresets.add(inode);
			Collections.sort(allPresets);
			clearAllFilters();
			lstPresets.setSelectedValue(inode, true);
			updateInfoPanel(inode);
			
			JOptionPane.showMessageDialog(this, 
					"User Preset Added: " + inode.toString(), 
					"Preset Creation Success", JOptionPane.INFORMATION_MESSAGE);
		}
		
		unsetWait();
	}
	
	private void btnNewCallback(){
		if(!presetsEditable) return;
		
		//Need a dialog that asks if want to create
		//	a std inst or a perc preset.
		setWait();
		InstNode inode = null;
		int ret = InstTypeMiniDialog.showDialog(parent);
		if(ret == InstTypeMiniDialog.SELECTION_INST){
			inode = newInstAction(null);
		}
		else if(ret == InstTypeMiniDialog.SELECTION_PERC){
			inode = newDrumsetAction(null);
		}
		else if(ret == InstTypeMiniDialog.SELECTION_DRUM){
			inode = newDrumAction(null);
		}
		else{
			unsetWait();
			return;
		}
		
		if(inode != null){
			allPresets.add(inode);
			Collections.sort(allPresets);
			clearAllFilters();
			lstPresets.setSelectedValue(inode, true);
			updateInfoPanel(inode);
			
			JOptionPane.showMessageDialog(this, 
					"User Preset Added: " + inode.toString(), 
					"Preset Creation Success", JOptionPane.INFORMATION_MESSAGE);
		}
		
		unsetWait();
	}
	
	private void btnEditCallback(){
		if(!presetsEditable) return;
		if(lstPresets.isSelectionEmpty()){
			JOptionPane.showMessageDialog(this, "No presets are selected!", 
					"Edit Preset", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		setWait();
		InstNode sel = lstPresets.getSelectedValue();
		if(sel != null && sel.preset != null){
			
			boolean editable = true;
			if(core != null){
				editable = core.isEditablePreset(sel.preset.getUID());
				if(!editable){
					int ret = JOptionPane.showConfirmDialog(this, 
							"Edits on selected preset will not be saved. Continue?", 
							"Edit Preset", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
					if(ret != JOptionPane.YES_OPTION){
						unsetWait();
						return;
					}
				}
			}
			
			if(sel.preset instanceof ZeqerInstPreset){
				ZeqerInstPreset instp = (ZeqerInstPreset)sel.preset;
				ZeqerInstEditDialog dialog = new ZeqerInstEditDialog(parent, core, instp);
				//dialog.setEditable(editable);
				dialog.showMe(this);
				//Disposes itself in closeMe()
				
				if(editable && dialog.getExitSelection()){
					dialog.loadDataToInstrument(instp);
					updateInfoPanel(sel);
				}
			}
			else if(sel.preset instanceof ZeqerPercPreset){
				ZeqerPercPreset percp = (ZeqerPercPreset)sel.preset;
				ZeqerDrumsetEditDialog dialog = new ZeqerDrumsetEditDialog(parent, core);
				dialog.setDrum(percp);
				dialog.showMe(this);
				//Disposes itself in closeMe()
				
				if(editable && dialog.getExitSelection()){
					dialog.loadDataIntoDrum(percp);
					updateInfoPanel(sel);
				}
			}
			else if(sel.preset instanceof ZeqerDrumPreset){
				ZeqerDrumPreset drump = (ZeqerDrumPreset)sel.preset;
				ZeqerDrumEditDialog dialog = new ZeqerDrumEditDialog(parent, core);
				dialog.loadDrumData(drump);
				dialog.showMe(this);
				
				if(editable && dialog.getExitSelection()){
					dialog.loadDataInto(drump);
					updateInfoPanel(sel);
				}
			}
		}
		
		unsetWait();
	}
	
	private void btnDeleteCallback(){
		if(!presetsEditable) return;
		if(lstPresets.isSelectionEmpty()){
			JOptionPane.showMessageDialog(this, "No presets are selected!", 
					"Delete Presets", JOptionPane.WARNING_MESSAGE);
			return;
		}
		setWait();
		//Can delete multiple at a time. Otherwise, while the list allows
		//	multiple selection, it ignores all but one item.
		
		//Have a message dialog for confirmation
		//"Delete n presets?"
		//Core should refuse to delete system presets.
		List<InstNode> allsel = lstPresets.getSelectedValuesList();
		int dcount = allsel.size();
		int ret = JOptionPane.showConfirmDialog(this, "Are you sure you want to detele " + dcount + " presets?", 
				"Delete Presets", 
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		
		if(ret != JOptionPane.YES_OPTION){
			unsetWait();
			return;
		}
		
		int successCount = 0;
		boolean[] successVec = new boolean[dcount];
		int i = 0;
		for(InstNode node : allsel){
			if(node.preset == null){i++; continue;}
			if(core.deletePreset(node.preset.getUID())){
				successVec[i++] = true;
				successCount++;
			}
		}
		
		allPresets.clear();
		loadPresetsFromCore();
		refilter();
		
		if(successCount < dcount){
			//Show message
			int nondel = dcount - successCount;
			JOptionPane.showMessageDialog(this, nondel + " of " + dcount + " presets"
					+ " could not be deleted.\n"
					+ "This is likely due to lack of permissions.", 
					"Delete Presets", JOptionPane.WARNING_MESSAGE);
		}
		
		unsetWait();
	}
	
}
