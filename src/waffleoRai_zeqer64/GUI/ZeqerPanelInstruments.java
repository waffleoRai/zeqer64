package waffleoRai_zeqer64.GUI;

import javax.swing.JFrame;
import javax.swing.JPanel;

import waffleoRai_zeqer64.ZeqerPreset;
import waffleoRai_zeqer64.GUI.dialogs.InstTypeMiniDialog;
import waffleoRai_zeqer64.GUI.dialogs.ZeqerDrumEditDialog;
import waffleoRai_zeqer64.GUI.dialogs.ZeqerInstEditDialog;
import waffleoRai_zeqer64.GUI.filters.FlagFilterPanel;
import waffleoRai_zeqer64.GUI.filters.TagFilterPanel;
import waffleoRai_zeqer64.GUI.filters.TextFilterPanel;
import waffleoRai_zeqer64.GUI.filters.ZeqerFilter;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;
import waffleoRai_zeqer64.presets.ZeqerInstPreset;
import waffleoRai_zeqer64.presets.ZeqerPercPreset;
import waffleoRai_zeqer64.presets.ZeqerSFXPreset;

import java.awt.GridBagLayout;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.WriterPanel;
import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.VoidCallbackMethod;
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
		gbl_pnlCtrl.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlCtrl.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlCtrl.setLayout(gbl_pnlCtrl);
		
		JButton btnNew = new JButton("New...");
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
				//btnNewCallback();
			}});
		
		JButton btnEdit = new JButton("Edit...");
		GridBagConstraints gbc_btnEdit = new GridBagConstraints();
		gbc_btnEdit.insets = new Insets(0, 0, 0, 5);
		gbc_btnEdit.gridx = 1;
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
				//btnEditCallback();
			}});
		
		JButton btnDelete = new JButton("Delete");
		GridBagConstraints gbc_btnDelete = new GridBagConstraints();
		gbc_btnDelete.gridx = 3;
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
				//btnDeleteCallback();
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
		}, "Standard Instrument", true);
		fpnl2.addSwitch(new ZeqerFilter<InstNode>(){
			public boolean itemPasses(InstNode item){
				if(item == null) return false;
				if(item.preset == null) return false;
				return (item.preset instanceof ZeqerPercPreset);
			}
		}, "Percussion", true);
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
		
		pnlTags = new TagFilterPanel<InstNode>(parent, core.getString(ZeqerGUIUtils.STRKEY_FILPNL_TAGS));
		pnlFilt.addPanel(pnlTags);
		
		//Might add flag panels too?
		
		pnlFilt.addRefilterCallback(new VoidCallbackMethod(){
			public void doMethod() {refilter();}
		});
	}
	
	/*----- Getters -----*/
	
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
	
	private void updateInfoPanel(InstNode selection){
		try{
			pnlInfo.clear();
			Writer writer = pnlInfo.getWriter();
			if(selection == null || selection.preset == null){
				writer.write("<No preset selected>\n");
			}
			else{
				writer.write(selection.preset.getName() + "\n");
				writer.write("UID: " + String.format("%08x\n", selection.preset.getUID()));
				writer.write("Type: ");
				if(selection.preset instanceof ZeqerInstPreset){
					writer.write("Standard Instrument\n");
					//Type specific...
					ZeqerInstPreset ipreset = (ZeqerInstPreset)selection.preset;
					writer.write("Enum Stem: " + ipreset.getEnumLabel() + "\n");
					
					Z64Instrument inst = ipreset.getInstrument();
					if(inst != null){
						int relraw = inst.getDecay();
						writer.write("Release Time: " + Z64Sound.releaseValueToMillis(relraw) + " milliseconds\n");
						writer.write("Regions -- \n");
						Z64WaveInfo smpl = inst.getSampleLow();
						if(smpl != null){
							writer.write("0 - " + inst.getLowRangeTop() + ":\t");
							writer.write(smpl.getName() + "\n");
							writer.write((inst.getLowRangeTop() + 1) + " - ");
						}
						else writer.write("0 - ");
						
						if(inst.getSampleHigh() != null){
							writer.write((inst.getHighRangeBottom() - 1) + ":\t");
						}
						else writer.write("127:\t");
						smpl = inst.getSampleMiddle();
						writer.write(smpl.getName() + "\n");
						
						smpl = inst.getSampleHigh();
						if(smpl != null){
							writer.write(inst.getHighRangeBottom() + " - 127:\t");
							writer.write(smpl.getName() + "\n");
						}
					}
				}
				else if(selection.preset instanceof ZeqerPercPreset){
					writer.write("Percussion Set\n");
					ZeqerPercPreset perc = (ZeqerPercPreset)selection.preset;
					writer.write("Regions: " + perc.getRegionCount() + "\n");
					writer.write("Max Slots Used: " + perc.getMaxUsedSlotCount() + "\n");
				}
				else if(selection.preset instanceof ZeqerSFXPreset){
					writer.write("SFX Set\n");
				}
				else{
					writer.write("Unknown\n");
				}
				
				writer.write("Tags: ");
				boolean first = true;
				List<String> tags = selection.preset.getAllTags();
				if(tags != null && !tags.isEmpty()){
					for(String tag : tags){
						if(!first) writer.write(";");
						writer.write(tag);
						first = false;	
					}
				}
				else writer.write("<None>");
				writer.write("\n");
			}
			writer.close();
		}
		catch(Exception ex){
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "ERROR: Could not render information for selected sample!",
						"Preset Info", JOptionPane.ERROR_MESSAGE);
		}
		pnlInfo.repaint();
	}
	
	public void setWait(){
		globalEnable.setEnabling(false);
		pnlFilt.disableAll();
		globalEnable.repaint();
		pnlFilt.repaint();
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	public void unsetWait(){
		globalEnable.setEnabling(true);
		pnlFilt.enableAll();
		lstSelectedEnable.setEnabling(!lstPresets.isSelectionEmpty());
		
		globalEnable.repaint();
		pnlFilt.repaint();
		setCursor(null);
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
	
	private void btnNewCallback(){
		if(!presetsEditable) return;
		
		//Need a dialog that asks if want to create
		//	a std inst or a perc preset.
		setWait();
		int ret = InstTypeMiniDialog.showDialog(parent);
		if(ret == InstTypeMiniDialog.SELECTION_INST){
			ZeqerInstEditDialog dialog = new ZeqerInstEditDialog(parent, core);
			dialog.showMe(this);
			//Disposes itself in closeMe()
			
			if(dialog.getExitSelection()){
				ZeqerInstPreset instp = dialog.getInstrument();
				if(core != null){
					core.addUserPreset(instp);
				}
				InstNode inode = new InstNode(instp);
				allPresets.add(inode);
				Collections.sort(allPresets);
				clearAllFilters();
				lstPresets.setSelectedValue(inode, true);
				updateInfoPanel(inode);
			}
		}
		else if(ret == InstTypeMiniDialog.SELECTION_PERC){
			ZeqerDrumEditDialog dialog = new ZeqerDrumEditDialog(parent, core);
			dialog.showMe(this);
			//Disposes itself in closeMe()
			
			ZeqerPercPreset percp = dialog.getDrum();
			if(percp != null){
				if(core != null){
					core.addUserPreset(percp);
				}
				InstNode inode = new InstNode(percp);
				allPresets.add(inode);
				Collections.sort(allPresets);
				clearAllFilters();
				lstPresets.setSelectedValue(inode, true);
				updateInfoPanel(inode);
			}
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
			if(sel.preset instanceof ZeqerInstPreset){
				ZeqerInstPreset instp = (ZeqerInstPreset)sel.preset;
				ZeqerInstEditDialog dialog = new ZeqerInstEditDialog(parent, core, instp);
				dialog.showMe(this);
				//Disposes itself in closeMe()
				
				//The save option in this dialog updates the inst preset by reference.
				//So shouldn't need to do anything else there.
				
				updateInfoPanel(sel);
			}
			else if(sel.preset instanceof ZeqerPercPreset){
				ZeqerPercPreset percp = (ZeqerPercPreset)sel.preset;
				ZeqerDrumEditDialog dialog = new ZeqerDrumEditDialog(parent, core);
				dialog.setDrum(percp);
				dialog.showMe(this);
				//Disposes itself in closeMe()
				
				//Like inst, the drum dialog writes back to ref on save.
				
				updateInfoPanel(sel);
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
