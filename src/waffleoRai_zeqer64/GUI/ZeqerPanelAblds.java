package waffleoRai_zeqer64.GUI;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Writer;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.WriterPanel;
import waffleoRai_Utils.VoidCallbackMethod;
import waffleoRai_zeqer64.ZeqerRom;
import waffleoRai_zeqer64.GUI.abldEdit.AbldEditDialog;
import waffleoRai_zeqer64.GUI.filters.FlagFilterPanel;
import waffleoRai_zeqer64.GUI.filters.TextFilterPanel;
import waffleoRai_zeqer64.GUI.filters.ZeqerFilter;
import waffleoRai_zeqer64.filefmt.AbldFile;
import waffleoRai_zeqer64.filefmt.NusRomInfo;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;

public class ZeqerPanelAblds extends JPanel{

	private static final long serialVersionUID = 727068912955057214L;
	
	/*----- Inner Classes -----*/
	
	private static class AbldNode implements Comparable<AbldNode>{
		public AbldFile data;
		
		public boolean equals(Object o){
			return o == this;
		}
		
		public String toString(){
			if(data == null) return "<empty>";
			String name = data.getBuildName();
			if(name == null) return "[Untitled]";
			return name;
		}

		public int compareTo(AbldNode o) {
			if(o == null) return 1;
			if(o == this) return 0;
			
			if(this.data == null){
				if(o.data == null) return 0;
				return -1;
			}
			
			if(o.data == null) return 1;
			
			String tname = this.data.getBuildName();
			String oname = o.data.getBuildName();
			if(tname == null){
				if(oname == null){
					//Compare timestamps
					ZonedDateTime ttime = this.data.getTimeCreated();
					ZonedDateTime otime = o.data.getTimeCreated();
					
					return ttime.compareTo(otime);
				}
				else return -1;
			}
			if(oname == null) return 1;
			
			int ncompare = tname.compareTo(oname);
			if(ncompare != 0) return ncompare;
			
			return 0;
		}
	}
	
	/*----- Instance Variables -----*/
	
	private ZeqerCoreInterface core;
	
	private JFrame parent;
	private ComponentGroup globalEnable;
	private ComponentGroup selectEnable;
	private ComponentGroup cgEditable;
	
	private boolean editable = true;
	private List<AbldNode> source;
	
	private FilterListPanel<AbldNode> pnlFilt;
	
	private JList<AbldNode> lstAbld;
	private WriterPanel pnlInfo;
	
	/*----- Init -----*/
	
	public ZeqerPanelAblds(JFrame parentFrame, ZeqerCoreInterface coreIFace, boolean readOnly){
		parent = parentFrame;
		core = coreIFace;
		editable = !readOnly;
		
		globalEnable = new ComponentGroup();
		selectEnable = globalEnable.newChild();
		cgEditable = globalEnable.newChild();
		
		initGUI();
		updatePool();
	}
	
	private void initGUI(){
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		pnlFilt = new FilterListPanel<AbldNode>();
		GridBagConstraints gbc_pnlFilt = new GridBagConstraints();
		gbc_pnlFilt.gridheight = 3;
		gbc_pnlFilt.insets = new Insets(0, 0, 5, 5);
		gbc_pnlFilt.fill = GridBagConstraints.BOTH;
		gbc_pnlFilt.gridx = 0;
		gbc_pnlFilt.gridy = 0;
		add(pnlFilt, gbc_pnlFilt);
		globalEnable.addComponent("pnlFilt", pnlFilt);
		
		JScrollPane spList = new JScrollPane();
		spList.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spList = new GridBagConstraints();
		gbc_spList.insets = new Insets(0, 0, 5, 0);
		gbc_spList.fill = GridBagConstraints.BOTH;
		gbc_spList.gridx = 1;
		gbc_spList.gridy = 0;
		add(spList, gbc_spList);
		globalEnable.addComponent("spList", spList);
		
		lstAbld = new JList<AbldNode>();
		spList.setViewportView(lstAbld);
		globalEnable.addComponent("lstAbld", lstAbld);
		lstAbld.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e) {
				listSelectionCallback();
			}});
		
		JScrollPane spInfo = new JScrollPane();
		GridBagConstraints gbc_spInfo = new GridBagConstraints();
		gbc_spInfo.insets = new Insets(0, 0, 5, 0);
		gbc_spInfo.fill = GridBagConstraints.BOTH;
		gbc_spInfo.gridx = 1;
		gbc_spInfo.gridy = 1;
		add(spInfo, gbc_spInfo);
		selectEnable.addComponent("spInfo", spInfo);
		
		pnlInfo = new WriterPanel();
		spInfo.setViewportView(pnlInfo);
		selectEnable.addComponent("pnlInfo", pnlInfo);
		
		JPanel pnlBtns = new JPanel();
		pnlBtns.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlBtns = new GridBagConstraints();
		gbc_pnlBtns.fill = GridBagConstraints.BOTH;
		gbc_pnlBtns.gridx = 1;
		gbc_pnlBtns.gridy = 2;
		add(pnlBtns, gbc_pnlBtns);
		GridBagLayout gbl_pnlBtns = new GridBagLayout();
		gbl_pnlBtns.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_pnlBtns.rowHeights = new int[]{0, 0, 0};
		gbl_pnlBtns.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlBtns.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		pnlBtns.setLayout(gbl_pnlBtns);
		
		JButton btnNew = new JButton("New...");
		GridBagConstraints gbc_btnNew = new GridBagConstraints();
		gbc_btnNew.insets = new Insets(5, 5, 5, 5);
		gbc_btnNew.gridx = 0;
		gbc_btnNew.gridy = 0;
		pnlBtns.add(btnNew, gbc_btnNew);
		cgEditable.addComponent("btnNew", btnNew);
		btnNew.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnNewCallback();
			}});
		
		JButton btnDuplicate = new JButton("Duplicate...");
		GridBagConstraints gbc_btnDuplicate = new GridBagConstraints();
		gbc_btnDuplicate.insets = new Insets(5, 0, 5, 5);
		gbc_btnDuplicate.gridx = 1;
		gbc_btnDuplicate.gridy = 0;
		pnlBtns.add(btnDuplicate, gbc_btnDuplicate);
		cgEditable.addComponent("btnDuplicate", btnDuplicate);
		selectEnable.addComponent("btnDuplicate", btnDuplicate);
		btnDuplicate.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnDupCallback();
			}});
		
		JButton btnEdit = new JButton("Edit...");
		if(!editable) btnEdit.setText("View...");
		GridBagConstraints gbc_btnEdit = new GridBagConstraints();
		gbc_btnEdit.insets = new Insets(5, 0, 5, 5);
		gbc_btnEdit.gridx = 2;
		gbc_btnEdit.gridy = 0;
		pnlBtns.add(btnEdit, gbc_btnEdit);
		selectEnable.addComponent("btnEdit", btnEdit);
		btnEdit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnEditCallback();
			}});
		
		JButton btnDelete = new JButton("Delete");
		GridBagConstraints gbc_btnDelete = new GridBagConstraints();
		gbc_btnDelete.insets = new Insets(5, 0, 5, 5);
		gbc_btnDelete.gridx = 4;
		gbc_btnDelete.gridy = 0;
		pnlBtns.add(btnDelete, gbc_btnDelete);
		cgEditable.addComponent("btnDelete", btnDelete);
		selectEnable.addComponent("btnDelete", btnDelete);
		btnDelete.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnDeleteCallback();
			}});
		
		JButton btnExport = new JButton("Export...");
		GridBagConstraints gbc_btnExport = new GridBagConstraints();
		gbc_btnExport.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnExport.gridwidth = 2;
		gbc_btnExport.insets = new Insets(0, 5, 5, 5);
		gbc_btnExport.gridx = 0;
		gbc_btnExport.gridy = 1;
		pnlBtns.add(btnExport, gbc_btnExport);
		selectEnable.addComponent("btnExport", btnExport);
		btnExport.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnExportCallback();
			}});
		
		addFilterPanels();
	}
	
	private void addFilterPanels(){
		//Text search
		TextFilterPanel<AbldNode> fpnl1 = new TextFilterPanel<AbldNode>(0);
		fpnl1.setSearchFilter(new ZeqerFilter<AbldNode>(){
			public boolean itemPasses(AbldNode item, String txt, long flags){
				if(item == null) return false;
				if(txt == null || txt.isEmpty()) return true;
				if(item.data == null) return false;
				String name = item.data.getBuildName();
				if(name == null) return false;
				return name.toLowerCase().contains(txt.toLowerCase());
			}
		});
		pnlFilt.addPanel(fpnl1);
		
		//Flags
		FlagFilterPanel<AbldNode> fpnl2 = new FlagFilterPanel<AbldNode>(core.getString(ZeqerGUIUtils.STRKEY_FILPNL_FLAGS));
		fpnl2.addSwitch(new ZeqerFilter<AbldNode>(){
			public boolean itemPasses(AbldNode item){
				if(item == null) return false;
				if(item.data == null) return false;
				return item.data.isZ5Compatible();
			}
		}, "Ocarina of Time Compatible", true);
		fpnl2.addSwitch(new ZeqerFilter<AbldNode>(){
			public boolean itemPasses(AbldNode item){
				if(item == null) return false;
				if(item.data == null) return false;
				return item.data.isInjectionBuild();
			}
		}, "Injection Build", true);
		fpnl2.addSwitch(new ZeqerFilter<AbldNode>(){
			public boolean itemPasses(AbldNode item){
				if(item == null) return false;
				if(item.data == null) return false;
				return !item.data.isSysBuild();
			}
		}, "Custom", true);
		
		pnlFilt.addRefilterCallback(new VoidCallbackMethod(){
			public void doMethod() {refilterCallback();}
		});
		pnlFilt.addPanel(fpnl2);
	}
	
	/*----- Getters -----*/
	
	public AbldFile getSelectedAbld(){
		if(lstAbld.isSelectionEmpty()) return null;
		AbldNode node = lstAbld.getSelectedValue();
		if(node == null) return null;
		return node.data;
	}
	
	/*----- Setters -----*/
	
	/*----- Core Pull -----*/
	
	public void updatePool(){
		if(source != null) source.clear();
		DefaultListModel<AbldNode> model = new DefaultListModel<AbldNode>();
		
		if(core != null){
			List<AbldFile> ablds = core.getAllAblds();
			if(ablds != null){
				if(source == null){
					source = new ArrayList<AbldNode>(ablds.size()+1);
				}	
				for(AbldFile a : ablds){
					AbldNode node = new AbldNode();
					node.data = a;
					source.add(node);
				}	
			}
		}
		
		Collections.sort(source);
		for(AbldNode node : source){
			model.addElement(node);
		}
		
		lstAbld.setModel(model);
		updateEnabling();
	}
	
	/*----- Drawing -----*/
	
	private void updateEnabling(){
		globalEnable.setEnabling(true);
		pnlFilt.enableAll();
		if(lstAbld.isSelectionEmpty()) {
			selectEnable.setEnabling(false);
		}
		if(!editable){
			cgEditable.setEnabling(false);
		}
		globalEnable.repaint();
	}
	
	public void reenable(){
		updateEnabling();
	}
	
	public void disableAll(){
		globalEnable.setEnabling(false);
		pnlFilt.disableAll();
		globalEnable.repaint();
		pnlFilt.repaint();
	}
	
	public void setWait(){
		globalEnable.setEnabling(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	public void unsetWait(){
		updateEnabling();
		setCursor(null);
	}
	
	private void printInfoToPanel(AbldNode node){
		pnlInfo.clear();
		try{
			Writer writer = pnlInfo.getWriter();
			if(node != null){
				if(node.data != null){
					String s = node.data.getBuildName();
					if(s != null) writer.write(s + "\n");
					else writer.write("[Untitled]\n");
					
					if(node.data.isInjectionBuild()){
						writer.write("Injectable\n");
						s = node.data.getBaseRomID();
						if(core != null){
							ZeqerRom rom = core.getRom(s);
							if(rom != null){
								NusRomInfo info = rom.getRomInfo();
								if(info != null){
									s = info.getROMName();
								}	
							}
						}
						
						writer.write("Base: " + s + "\n");
					}
					else{
						writer.write("Full Build\n");
					}
					
					writer.write("OoT Compatible: " + node.data.isZ5Compatible() + "\n");
					writer.write("Default TV Type: ");
					switch(node.data.getTVType()){
					case ZeqerRom.TV_TYPE__NTSC:
						writer.write("NTSC"); break;
					case ZeqerRom.TV_TYPE__PAL:
						writer.write("PAL"); break;
					case ZeqerRom.TV_TYPE__MPAL:
						writer.write("MPAL"); break;
					default:
						writer.write("(Unknown)"); break;
					}
					writer.write("\n");
					
					writer.write("Seq Count: " + node.data.getSeqCount() + "\n");
					writer.write("Soundfont Count: " + node.data.getBankCount() + "\n");
					writer.write("Sample Count: " + node.data.getTotalSampleCount() + "\n");
					writer.write("Created: " + node.data.getTimeCreated().format(DateTimeFormatter.RFC_1123_DATE_TIME) + "\n");
					writer.write("Last Modified: " + node.data.getTimeModified().format(DateTimeFormatter.RFC_1123_DATE_TIME) + "\n");
				}
				else{
					writer.write("<Data not found>");
				}
			}
			else{
				writer.write("<No build selected>");
			}
		}
		catch(IOException ex){
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "ERROR: Could not render bank info. See stderr for details.", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		pnlInfo.repaint();
	}
	
	/*----- Callbacks -----*/
	
	private void dummyCallback(){
		JOptionPane.showMessageDialog(this, "Sorry, this component doesn't work yet!", 
				"Notice", JOptionPane.INFORMATION_MESSAGE);
	}
	
	private void refilterCallback(){
		setWait();
		DefaultListModel<AbldNode> model = new DefaultListModel<AbldNode>();
		
		if(source != null){
			for(AbldNode node : source){
				if(node.data == null) continue;
				if(pnlFilt.itemPassesFilters(node)){
					model.addElement(node);
				}
			}	
		}
		
		lstAbld.setModel(model);
		lstAbld.setSelectedIndex(-1);
		unsetWait();
	}
	
	private void listSelectionCallback(){
		printInfoToPanel(lstAbld.getSelectedValue());
		updateEnabling();
	}
	
	private void btnNewCallback(){
		//TODO
		dummyCallback();
	}
	
	private void btnEditCallback(){
		if(lstAbld.isSelectionEmpty()) return;
		AbldNode sel = lstAbld.getSelectedValue();
		if(sel == null) return;
		if(sel.data == null) return;
		
		boolean canedit = editable && !sel.data.isSysBuild();
		AbldEditDialog dialog = new AbldEditDialog(parent, core, !canedit);
		dialog.loadAbldToForm(sel.data);
		dialog.showMe(this);
		
		if(canedit && dialog.getExitSelection()){
			dialog.loadFormInfoToAbld(sel.data);
			lstAbld.repaint();
		}
	}
	
	private void btnDupCallback(){
		//TODO
		dummyCallback();
	}
	
	private void btnDeleteCallback(){
		//TODO
		dummyCallback();
	}
	
	private void btnExportCallback(){
		//TODO
		dummyCallback();
	}

}
