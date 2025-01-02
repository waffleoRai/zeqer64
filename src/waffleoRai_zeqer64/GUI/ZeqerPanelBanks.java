package waffleoRai_zeqer64.GUI;

import javax.swing.JFrame;
import javax.swing.JPanel;

import waffleoRai_zeqer64.ErrorCode;
import waffleoRai_zeqer64.ZeqerBank;
import waffleoRai_zeqer64.GUI.dialogs.BankImportDialog;
import waffleoRai_zeqer64.GUI.dialogs.ZeqerBankEditDialog;
import waffleoRai_zeqer64.GUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_zeqer64.GUI.filters.FlagFilterPanel;
import waffleoRai_zeqer64.GUI.filters.TagFilterPanel;
import waffleoRai_zeqer64.GUI.filters.TextFilterPanel;
import waffleoRai_zeqer64.GUI.filters.ZeqerFilter;
import waffleoRai_zeqer64.bankImport.BankImporter;
import waffleoRai_zeqer64.filefmt.bank.ZeqerBankIO;
import waffleoRai_zeqer64.filefmt.bank.ZeqerBankTable;
import waffleoRai_zeqer64.filefmt.bank.BankTableEntry;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;
import java.awt.GridBagLayout;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.WriterPanel;
import waffleoRai_Utils.VoidCallbackMethod;
import waffleoRai_soundbank.nintendo.z64.Z64Bank;

import javax.swing.JList;
import javax.swing.JOptionPane;

public class ZeqerPanelBanks extends JPanel{

	private static final long serialVersionUID = 95293232098131821L;
	
	public static final String BNKPNL_LAST_IMPORT_PATH = "ZBNKPNL_LASTIMPORT";
	public static final String BNKPNL_LAST_EXPORT_PATH = "ZBNKPNL_LASTEXPORT";
	
	//private static final int EXPORT_OP_XML = 0;
	//private static final int EXPORT_OP_SF2 = 1;
	
	/*----- Inner Classes -----*/

	private static class BankNode implements Comparable<BankNode>{
		public ZeqerBank data;
		
		public boolean equals(Object o){
			return o == this;
		}
		
		public String toString(){
			if(data == null) return "<empty>";
			BankTableEntry entry = data.getTableEntry();
			if(entry == null) return "<No Metadata>";
			String name = entry.getName();
			if(name == null) return String.format("[Untitled Font %08x]", entry.getUID());
			return name;
		}

		public int compareTo(BankNode o) {
			if(o == null) return 1;
			if(o == this) return 0;
			
			if(this.data == null){
				if(o.data == null) return 0;
				return -1;
			}
			
			if(o.data == null) return 1;
			
			BankTableEntry tentry = this.data.getTableEntry();
			BankTableEntry oentry = o.data.getTableEntry();
			
			if(tentry == null){
				if(oentry == null) return 0;
				return -1;
			}
			if(oentry == null) return 1;
			
			String tname = tentry.getName();
			String oname = oentry.getName();
			if(tname == null){
				if(oname == null){
					//Compare uids
					if(tentry.getUID() > oentry.getUID()) return 1;
					if(tentry.getUID() < oentry.getUID()) return -1;
					return 0;
				}
				else return -1;
			}
			if(oname == null) return 1;
			
			int ncompare = tname.compareTo(oname);
			if(ncompare != 0) return ncompare;
			
			if(tentry.getUID() > oentry.getUID()) return 1;
			if(tentry.getUID() < oentry.getUID()) return -1;
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
	private List<BankNode> source;
	
	private FilterListPanel<BankNode> pnlFilt;
	private TagFilterPanel<BankNode> pnlTags;
	private JList<BankNode> lstBanks;
	private WriterPanel pnlInfo;

	/*----- Init -----*/
	
	public ZeqerPanelBanks(JFrame parentFrame, ZeqerCoreInterface coreIFace, boolean readOnly){
		parent = parentFrame;
		core = coreIFace;
		editable = !readOnly;
		
		globalEnable = new ComponentGroup();
		selectEnable = globalEnable.newChild();
		cgEditable = globalEnable.newChild();
		
		initGUI();
		updateBankPool();
	}
	
	private void initGUI(){
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 60, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		pnlFilt = new FilterListPanel<BankNode>();
		GridBagConstraints gbc_pnlFilt = new GridBagConstraints();
		gbc_pnlFilt.insets = new Insets(0, 0, 5, 5);
		gbc_pnlFilt.fill = GridBagConstraints.BOTH;
		gbc_pnlFilt.gridx = 0;
		gbc_pnlFilt.gridy = 0;
		add(pnlFilt, gbc_pnlFilt);
		
		JPanel pnlRight = new JPanel();
		GridBagConstraints gbc_pnlRight = new GridBagConstraints();
		gbc_pnlRight.insets = new Insets(0, 0, 5, 0);
		gbc_pnlRight.fill = GridBagConstraints.BOTH;
		gbc_pnlRight.gridx = 1;
		gbc_pnlRight.gridy = 0;
		add(pnlRight, gbc_pnlRight);
		GridBagLayout gridBagLayout_1 = new GridBagLayout();
		gridBagLayout_1.columnWidths = new int[]{0, 0};
		gridBagLayout_1.rowHeights = new int[]{0, 60, 0, 0};
		gridBagLayout_1.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout_1.rowWeights = new double[]{1.0, 1.0, 0.0, Double.MIN_VALUE};
		pnlRight.setLayout(gridBagLayout_1);
		
		JScrollPane spList = new JScrollPane();
		spList.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spList = new GridBagConstraints();
		gbc_spList.insets = new Insets(5, 5, 5, 5);
		gbc_spList.fill = GridBagConstraints.BOTH;
		gbc_spList.gridx = 0;
		gbc_spList.gridy = 0;
		pnlRight.add(spList, gbc_spList);
		globalEnable.addComponent("spList", spList);
		
		lstBanks = new JList<BankNode>();
		spList.setViewportView(lstBanks);
		globalEnable.addComponent("lstBanks", lstBanks);
		lstBanks.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e) {
				listSelectionCallback();
			}});
		
		JScrollPane spInfo = new JScrollPane();
		GridBagConstraints gbc_spInfo = new GridBagConstraints();
		gbc_spInfo.insets = new Insets(5, 5, 5, 5);
		gbc_spInfo.fill = GridBagConstraints.BOTH;
		gbc_spInfo.gridx = 0;
		gbc_spInfo.gridy = 1;
		pnlRight.add(spInfo, gbc_spInfo);
		selectEnable.addComponent("spInfo", spInfo);
		
		pnlInfo = new WriterPanel();
		spInfo.setViewportView(pnlInfo);
		
		JPanel pnlBtn = new JPanel();
		GridBagConstraints gbc_pnlBtn = new GridBagConstraints();
		gbc_pnlBtn.fill = GridBagConstraints.BOTH;
		gbc_pnlBtn.gridx = 0;
		gbc_pnlBtn.gridy = 2;
		pnlRight.add(pnlBtn, gbc_pnlBtn);
		GridBagLayout gridBagLayout_2 = new GridBagLayout();
		gridBagLayout_2.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gridBagLayout_2.rowHeights = new int[]{0, 0, 0};
		gridBagLayout_2.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout_2.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		pnlBtn.setLayout(gridBagLayout_2);
		
		JButton btnNew = new JButton("New...");
		GridBagConstraints gbc_btnNew = new GridBagConstraints();
		gbc_btnNew.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNew.insets = new Insets(0, 0, 5, 5);
		gbc_btnNew.gridx = 0;
		gbc_btnNew.gridy = 0;
		pnlBtn.add(btnNew, gbc_btnNew);
		cgEditable.addComponent("btnNew", btnNew);
		btnNew.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnNewCallback();
			}});
		
		JButton btnDuplicate = new JButton("Duplicate...");
		GridBagConstraints gbc_btnDuplicate = new GridBagConstraints();
		gbc_btnDuplicate.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnDuplicate.insets = new Insets(0, 0, 5, 5);
		gbc_btnDuplicate.gridx = 1;
		gbc_btnDuplicate.gridy = 0;
		pnlBtn.add(btnDuplicate, gbc_btnDuplicate);
		selectEnable.addComponent("btnDuplicate", btnDuplicate);
		cgEditable.addComponent("btnDuplicate", btnDuplicate);
		btnDuplicate.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnDupCallback();
			}});
		
		JButton btnEdit = new JButton("Edit...");
		GridBagConstraints gbc_btnEdit = new GridBagConstraints();
		gbc_btnEdit.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnEdit.insets = new Insets(0, 0, 5, 5);
		gbc_btnEdit.gridx = 2;
		gbc_btnEdit.gridy = 0;
		pnlBtn.add(btnEdit, gbc_btnEdit);
		selectEnable.addComponent("btnEdit", btnEdit);
		cgEditable.addComponent("btnEdit", btnEdit);
		btnEdit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnEditCallback();
			}});
		
		JButton btnDelete = new JButton("Delete");
		GridBagConstraints gbc_btnDelete = new GridBagConstraints();
		gbc_btnDelete.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnDelete.insets = new Insets(0, 0, 5, 0);
		gbc_btnDelete.gridx = 4;
		gbc_btnDelete.gridy = 0;
		pnlBtn.add(btnDelete, gbc_btnDelete);
		selectEnable.addComponent("btnDelete", btnDelete);
		cgEditable.addComponent("btnDelete", btnDelete);
		btnDelete.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnDeleteCallback();
			}});
		
		JButton btnImport = new JButton("Import...");
		GridBagConstraints gbc_btnImport = new GridBagConstraints();
		gbc_btnImport.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnImport.insets = new Insets(0, 0, 0, 5);
		gbc_btnImport.gridx = 0;
		gbc_btnImport.gridy = 1;
		pnlBtn.add(btnImport, gbc_btnImport);
		cgEditable.addComponent("btnImport", btnImport);
		btnImport.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnImportCallback();
			}});
		
		JButton btnExport = new JButton("Export...");
		GridBagConstraints gbc_btnExport = new GridBagConstraints();
		gbc_btnExport.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnExport.insets = new Insets(0, 0, 0, 5);
		gbc_btnExport.gridx = 1;
		gbc_btnExport.gridy = 1;
		pnlBtn.add(btnExport, gbc_btnExport);
		selectEnable.addComponent("btnExport", btnExport);
		cgEditable.addComponent("btnExport", btnExport);
		btnExport.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnExportCallback();
			}});
		
		JPanel pnlPlay = new JPanel();
		GridBagConstraints gbc_pnlPlay = new GridBagConstraints();
		gbc_pnlPlay.gridwidth = 2;
		gbc_pnlPlay.fill = GridBagConstraints.BOTH;
		gbc_pnlPlay.gridx = 0;
		gbc_pnlPlay.gridy = 1;
		add(pnlPlay, gbc_pnlPlay);
		
		addFilterPanels();
		updateEnabling();
	}
	
	private void addFilterPanels(){
		//Text search
		TextFilterPanel<BankNode> fpnl1 = new TextFilterPanel<BankNode>(0);
		fpnl1.setSearchFilter(new ZeqerFilter<BankNode>(){
			public boolean itemPasses(BankNode item, String txt, long flags){
				if(item == null) return false;
				if(txt == null || txt.isEmpty()) return true;
				String name = item.data.getTableEntry().getName();
				if(name == null) return false;
				return name.toLowerCase().contains(txt.toLowerCase());
			}
		});
		pnlFilt.addPanel(fpnl1);
		
		//Tags
		pnlTags = new TagFilterPanel<BankNode>(parent, core.getString(ZeqerGUIUtils.STRKEY_FILPNL_TAGS));
		pnlFilt.addPanel(pnlTags);
		
		//Flags (OoT, MM, User, SFX Bank)
		FlagFilterPanel<BankNode> fpnl2 = new FlagFilterPanel<BankNode>(core.getString(ZeqerGUIUtils.STRKEY_FILPNL_FLAGS));
		fpnl2.addSwitch(new ZeqerFilter<BankNode>(){
			public boolean itemPasses(BankNode item){
				if(item == null) return false;
				if(item.data == null) return false;
				BankTableEntry entry = item.data.getTableEntry();
				if(entry == null) return false;
				return entry.flagsSet(ZeqerBankTable.FLAG_Z5);
			}
		}, "Ocarina of Time", true);
		fpnl2.addSwitch(new ZeqerFilter<BankNode>(){
			public boolean itemPasses(BankNode item){
				if(item == null) return false;
				if(item.data == null) return false;
				BankTableEntry entry = item.data.getTableEntry();
				if(entry == null) return false;
				return entry.flagsSet(ZeqerBankTable.FLAG_Z6);
			}
		}, "Majora's Mask", true);
		fpnl2.addSwitch(new ZeqerFilter<BankNode>(){
			public boolean itemPasses(BankNode item){
				if(item == null) return false;
				if(item.data == null) return false;
				BankTableEntry entry = item.data.getTableEntry();
				if(entry == null) return false;
				return entry.flagsSet(ZeqerBankTable.FLAG_CUSTOM);
			}
		}, "Custom", true);
		fpnl2.addSwitch(new ZeqerFilter<BankNode>(){
			public boolean itemPasses(BankNode item){
				if(item == null) return false;
				if(item.data == null) return false;
				BankTableEntry entry = item.data.getTableEntry();
				if(entry == null) return false;
				return entry.getSFXCount() > 0;
			}
		}, "SFX Bank", true);
		pnlFilt.addPanel(fpnl2);
		
		pnlFilt.addRefilterCallback(new VoidCallbackMethod(){
			public void doMethod() {refilterCallback();}
		});
	}
	
	/*----- Getters -----*/
	
	public ZeqerBank getSelectedBank(){
		BankNode sel = lstBanks.getSelectedValue();
		if(sel == null) return null;
		return sel.data;
	}
	
	/*----- Setters -----*/
	
	public void selectBankByUID(int uid) {
		if(source == null || lstBanks == null) return;
		lstBanks.setSelectedIndex(-1);
		if(uid == 0) return;
		if(uid == -1) return;
		
		int i = 0;
		for(BankNode node : source) {
			if(node.data != null) {
				BankTableEntry meta = node.data.getTableEntry();
				if(meta != null) {
					if(meta.getUID() == uid) {
						lstBanks.setSelectedIndex(i);
						return;
					}
				}
			}
			i++;
		}
	}
	
	/*----- Core Pull -----*/
	
	public void updateBankPool(){
		if(source != null) source.clear();
		if(core == null) return;
		
		List<ZeqerBank> banks = core.getAllValidBanks();
		DefaultListModel<BankNode> model = new DefaultListModel<BankNode>();
		if(banks != null){
			int alloc = banks.size()+1;
			source = new ArrayList<BankNode>(alloc);
			for(ZeqerBank bnk : banks){
				BankNode bnode = new BankNode();
				bnode.data = bnk;
				source.add(bnode);
			}
		}
		
		Collections.sort(source);
		for(BankNode n : source){
			model.addElement(n);
		}
		
		lstBanks.setModel(model);
		updateTagPool();
		globalEnable.repaint();
	}
	
	private void updateTagPool(){
		Set<String> tagpool = new HashSet<String>();
		if(source != null){
			for(BankNode node : source){
				if(node.data == null) continue;
				BankTableEntry entry = node.data.getTableEntry();
				if(entry == null) continue;
				Collection<String> tags = entry.getTags();
				if(tags != null){
					tagpool.addAll(tags);
				}
			}
		}
		pnlTags.addTagPool(tagpool, new ZeqerFilter<BankNode>(){
			public boolean itemPasses(BankNode item, String txt){
				if(item == null || item.data == null) return false;
				BankTableEntry entry = item.data.getTableEntry();
				if(entry == null) return false;
				Collection<String> tags = entry.getTags();
				if(tags == null) return false;
				return tags.contains(txt);
			}
		});
	}
	
	/*----- Drawing -----*/
	
	private void updateEnabling(){
		globalEnable.setEnabling(true);
		pnlFilt.enableAll();
		if(lstBanks.isSelectionEmpty()) {
			selectEnable.setEnabling(false);
		}
		if(!editable){
			cgEditable.setEnabling(false);
		}
		globalEnable.repaint();
	}
	
	public void disableAll(){
		globalEnable.setEnabling(false);
		pnlFilt.disableAll();
		globalEnable.repaint();
	}
	
	public void reenable(){updateEnabling();}
	
	public void setWait(){
		globalEnable.setEnabling(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	public void unsetWait(){
		updateEnabling();
		setCursor(null);
	}
	
	private void printInfoToPanel(BankNode node){
		pnlInfo.clear();
		try{
			Writer writer = pnlInfo.getWriter();
			if(node != null){
				if(node.data != null){
					BankTableEntry entry = node.data.getTableEntry();
					if(entry != null){
						String s = entry.getName();
						if(s != null) writer.write(s + "\n");
						else writer.write("[Untitled]\n");
						
						writer.write(String.format("UID: %08x\n", entry.getUID()));
						writer.write("Medium: " + ZeqerGUIUtils.getMediumString(entry.getMedium()) + "\n");
						writer.write("Cache Policy: " + ZeqerGUIUtils.getCacheString(entry.getCachePolicy()) + "\n");
						writer.write("Instrument Slots: " + entry.getInstrumentCount() + "\n");
						writer.write("Percussion Slots: " + entry.getPercussionCount() + "\n");
						writer.write("SFX Slots: " + entry.getSFXCount() + "\n");
						writer.write("Last Modified: " + entry.getDateModified().format(DateTimeFormatter.RFC_1123_DATE_TIME) + "\n");
						
						writer.write("Tags: ");
						Collection<String> tags = entry.getTags();
						boolean first = true;
						for(String tag : tags){
							if(!first) writer.write(";");
							else first = false;
							writer.write(tag);
						}
						
						writer.close();
					}
					else{
						writer.write("<Metadata not found>");
					}
				}
				else{
					writer.write("<Data not found>");
				}
			}
			else{
				writer.write("<No bank selected>");
			}
		}
		catch(IOException ex){
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "ERROR: Could not render bank info. See stderr for details.", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		
		
		pnlInfo.repaint();
	}
	
	/*----- Internal -----*/
	
	private void finishImport(BankImporter importer){
		if(importer == null) return;
		if(core == null) return;
		setWait();
		importer.linkCoreInterface(core);
		
		IndefProgressDialog dialog = new IndefProgressDialog(parent, "Please Wait");
		dialog.setPrimaryString("Import in Progress");
		dialog.setSecondaryString("Importing requested items from " + importer.getPath());
		
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>(){
			protected Void doInBackground() throws Exception{
				ErrorCode err = new ErrorCode();
				try{
					ZeqerBankIO.finishImport(importer, err);
					if(err.value != ZeqerBankIO.ERR_NONE){
						JOptionPane.showMessageDialog(parent, 
								"Error caught during import: " + ZeqerBankIO.getErrorCodeString(err.value), 
								"Import Failed", JOptionPane.ERROR_MESSAGE);
					}
					else{
						JOptionPane.showMessageDialog(parent, 
								"Import complete. No error caught.", 
								"Import Succeeded", JOptionPane.INFORMATION_MESSAGE);
					}
				}
				catch(Exception ex){
					ex.printStackTrace();
					JOptionPane.showMessageDialog(parent, 
							"Error occurred during import. See stderr for details.", 
							"Import Failed", JOptionPane.ERROR_MESSAGE);
				}
				
				return null;
			}
			
			public void done(){
				dialog.closeMe();
				updateBankPool();
				unsetWait();
			}
		};
		
		dialog.render();
		task.execute();
	}
	
	private void showImportOptions(BankImporter importer){
		if(importer == null) return;
		
		setWait();
		BankImportDialog dialog = new BankImportDialog(parent);
		dialog.loadInfoToGUI(importer.getInfo());
		dialog.showMe(this);
		
		if(dialog.getExitSelection()){
			dialog.getInfoFromGUI(importer.getInfo());
			dialog.getSampleImportSettings(importer.getSampleOptions());
			finishImport(importer);
		}
		unsetWait();
	}
	
	private void startImport(String path){
		setWait();
		IndefProgressDialog dialog = new IndefProgressDialog(parent, "Please Wait");
		dialog.setPrimaryString("Reading");
		dialog.setSecondaryString("Processing contents of " + path);
	
		SwingWorker<BankImporter, Void> task = new SwingWorker<BankImporter, Void>(){
			protected BankImporter doInBackground() throws Exception{
				ErrorCode err = new ErrorCode();
				try{
					BankImporter imp = ZeqerBankIO.initializeImport(path, err);
					if(imp == null || err.value != ZeqerBankIO.ERR_NONE){
						JOptionPane.showMessageDialog(parent, 
								"Error caught during read: " + ZeqerBankIO.getErrorCodeString(err.value), 
								"Import Failed", JOptionPane.ERROR_MESSAGE);
						return null;
					}
					return imp;
				}
				catch(Exception ex){
					ex.printStackTrace();
					JOptionPane.showMessageDialog(parent, 
							"Input file could not be read. See stderr for details.", 
							"Import Failed", JOptionPane.ERROR_MESSAGE);
				}
				
				return null;
			}
			
			public void done(){
				dialog.closeMe();
				unsetWait();
			}
		};
		
		task.addPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				if(task.isDone()){
					try {
						BankImporter result = task.get();
						if(result == null) return;
						showImportOptions(result);
					} 
					catch (InterruptedException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(parent, 
								"Internal error: Unexpected interrupt retrieving import info.", 
								"Import Failed", JOptionPane.ERROR_MESSAGE);
					} 
					catch (ExecutionException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(parent, 
								"Internal error: Async execution failed when retrieving import info.", 
								"Import Failed", JOptionPane.ERROR_MESSAGE);
					}
				}
			}});
		
		dialog.render();
		task.execute();
	}
	
	/*----- Callbacks -----*/
	
	private void refilterCallback(){
		setWait();
		DefaultListModel<BankNode> model = new DefaultListModel<BankNode>();
		
		if(source != null){
			for(BankNode node : source){
				if(node.data == null) continue;
				if(pnlFilt.itemPassesFilters(node)){
					model.addElement(node);
				}
			}	
		}
		
		lstBanks.setModel(model);
		lstBanks.setSelectedIndex(-1);
		unsetWait();
	}
	
	private void listSelectionCallback(){
		printInfoToPanel(lstBanks.getSelectedValue());
		updateEnabling();
	}
	
	private void btnNewCallback(){
		if(core == null){
			JOptionPane.showMessageDialog(this, 
					"NULL core link!", "New Bank", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		setWait();
		ZeqerBank nbank = core.newUserBank(0);
		if(nbank != null){
			ZeqerBankEditDialog dialog = new ZeqerBankEditDialog(parent, core);
			dialog.setEditable(true);
			dialog.loadBankToForm(nbank);
			dialog.showMe(this);
			
			if(dialog.getExitSelection()) {
				if(dialog.loadIntoBankFromForm(nbank)){
					try{
						nbank.saveAll();
					}catch(Exception ex){
						ex.printStackTrace();
						JOptionPane.showMessageDialog(this, 
								"There was an error saving the bank changes!", 
								"Edit Bank", JOptionPane.ERROR_MESSAGE);
					}
					nbank.unloadBankData();
				}
				else{
					JOptionPane.showMessageDialog(this, 
							"There was an error saving the bank changes!", 
							"Edit Bank", JOptionPane.ERROR_MESSAGE);
				}
			}
			
			updateBankPool();
			selectBankByUID(nbank.getTableEntry().getUID());
		}
		else {
			JOptionPane.showMessageDialog(this, 
					"Bank spawn failed! See stderr for details!", "New Bank", JOptionPane.ERROR_MESSAGE);
		}
		
		unsetWait();
	}
	
	private void btnEditCallback(){
		BankNode selected = lstBanks.getSelectedValue();
		if(selected == null){
			JOptionPane.showMessageDialog(this, 
					"No bank selected!", "Edit Bank", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		if(selected.data == null){
			JOptionPane.showMessageDialog(this, 
					"Selected bank has no data!", "Edit Bank", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		BankTableEntry meta = selected.data.getTableEntry();
		if(meta == null){
			JOptionPane.showMessageDialog(this, 
					"Selected bank is not in table!", "Edit Bank", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		setWait();
		boolean canEdit = editable;
		if(editable){
			if(core != null) canEdit = core.isEditableBank(meta.getUID());
		}
		
		if(!canEdit){
			int ret = JOptionPane.showConfirmDialog(this, 
					"Edits on selected bank will not be saved. Continue?", 
					"Edit Bank", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(ret != JOptionPane.YES_OPTION){
				unsetWait();
				return;
			}
		}
		
		ZeqerBankEditDialog dialog = new ZeqerBankEditDialog(parent, core);
		dialog.setEditable(canEdit);
		dialog.loadBankToForm(selected.data);
		dialog.showMe(this);
		
		if(canEdit && dialog.getExitSelection()){
			if(dialog.loadIntoBankFromForm(selected.data)){
				try{
					selected.data.saveAll();
				}catch(Exception ex){
					ex.printStackTrace();
					JOptionPane.showMessageDialog(this, 
							"There was an error saving the bank changes!", 
							"Edit Bank", JOptionPane.ERROR_MESSAGE);
				}
				selected.data.unloadBankData();
			}
			else{
				JOptionPane.showMessageDialog(this, 
						"There was an error saving the bank changes!", 
						"Edit Bank", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		unsetWait();
	}
	
	private void btnDupCallback(){
		BankNode selected = lstBanks.getSelectedValue();
		if(selected == null){
			JOptionPane.showMessageDialog(this, 
					"Please select a bank to duplicate!", "Duplicate Bank", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		if(selected.data == null){
			JOptionPane.showMessageDialog(this, 
					"Selected bank has no data!", "Duplicate Bank", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		BankTableEntry meta = selected.data.getTableEntry();
		if(meta == null){
			JOptionPane.showMessageDialog(this, 
					"Selected bank is not in table!", "Duplicate Bank", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		setWait();
		ZeqerBank dup = selected.data.createUserDuplicate(core);
		if(dup != null) {
			ZeqerBankEditDialog dialog = new ZeqerBankEditDialog(parent, core);
			dialog.setEditable(true);
			dialog.loadBankToForm(dup);
			dialog.showMe(this);
			
			if(dialog.getExitSelection()) {
				if(dialog.loadIntoBankFromForm(dup)){
					try{
						dup.saveAll();
					}catch(Exception ex){
						ex.printStackTrace();
						JOptionPane.showMessageDialog(this, 
								"There was an error saving the bank changes!", 
								"Edit Bank", JOptionPane.ERROR_MESSAGE);
					}
					dup.unloadBankData();
				}
				else{
					JOptionPane.showMessageDialog(this, 
							"There was an error saving the bank changes!", 
							"Edit Bank", JOptionPane.ERROR_MESSAGE);
				}
			}
			
			//After close, update bank list and select the new duplicate
			updateBankPool();
			selectBankByUID(dup.getTableEntry().getUID());
		}
		else {
			JOptionPane.showMessageDialog(this, 
					"Duplication failed! See stderr for details.", "Duplicate Bank", JOptionPane.ERROR_MESSAGE);
		}
		
		unsetWait();
	}
	
	private void btnDeleteCallback(){
		if(lstBanks.isSelectionEmpty()){
			JOptionPane.showMessageDialog(this, 
					"No bank selected!", "Delete Bank", JOptionPane.WARNING_MESSAGE);
			return;
		}
		if(core == null){
			JOptionPane.showMessageDialog(this, 
					"Core link is null!", "Delete Bank", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		List<BankNode> selList = lstBanks.getSelectedValuesList();
		int sysCount = 0;
		int selCount = selList.size();
		boolean[] isSys = new boolean[selCount];
		int i = 0;
		for(BankNode n : selList) {
			if(n.data != null) {
				BankTableEntry meta = n.data.getTableEntry();
				if(meta != null) {
					int uid = meta.getUID();
					isSys[i] = !core.isEditableBank(uid);
					if(isSys[i]) sysCount++;
				}
			}
			i++;
		}
		
		if(sysCount >= selCount) {
			JOptionPane.showMessageDialog(this, 
					"No editable banks selected!", "Delete Bank", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		int ret = 0;
		int usrCount = selCount - sysCount;
		String msg = "<null message>";
		if(sysCount > 0) {
			msg = "Remove " + usrCount + " user banks from workspace? "
					+ "(Remaining " + sysCount + " are ROM imports and cannot be edited.)";
		}
		else {
			if(selCount > 1) {
				msg = "Remove " + usrCount + " user banks from workspace?";
			}
			else {
				BankNode n = lstBanks.getSelectedValue();
				if(n.data != null) {
					BankTableEntry meta = n.data.getTableEntry();
					if(meta != null) {
						String bname = meta.getName();
						if(bname != null && !bname.isEmpty()) {
							msg = "Remove \"" + bname + "\" from workspace?";
						}
						else msg = String.format("Remove bank with ID %08x from workspace?", meta.getUID());
					}
					else msg = "Remove selected bank from workspace?";
				}
				else msg = "Remove selected bank from workspace?";
			}
		}
		
		ret = JOptionPane.showConfirmDialog(
				this, msg, "Delete Bank", 
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if(ret != JOptionPane.YES_OPTION) return;
		
		setWait();
		i = 0;
		int okayCount = 0;
		for(BankNode n : selList) {
			if(!isSys[i] && n.data != null) {
				BankTableEntry meta = n.data.getTableEntry();
				if(meta != null) {
					if(core.deleteUserBank(meta.getUID())) okayCount++;
				}
			}
			i++;
		}
		int notOkayCount = usrCount - okayCount;
		if(notOkayCount > 0) {
			JOptionPane.showMessageDialog(this, 
					notOkayCount + " banks could not be removed! See stderr for details.", 
					"Delete Bank", JOptionPane.WARNING_MESSAGE);
		}
		else {
			JOptionPane.showMessageDialog(this, 
					okayCount + " banks successfully removed!", 
					"Delete Bank", JOptionPane.INFORMATION_MESSAGE);
		}
		
		updateBankPool();
		unsetWait();
	}
	
	private void btnExportCallback(){
		//Only exports to decomp XML for now.
		//But if add more, put in an ExportTypeDialog
		if(lstBanks.isSelectionEmpty()) {
			JOptionPane.showMessageDialog(parent, 
					"Please select a bank to export.", 
					"No Selection", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		String lastpath = null;
		if(core != null){
			lastpath = core.getSetting(BNKPNL_LAST_EXPORT_PATH);
		}
		JFileChooser fc = new JFileChooser(lastpath);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int res = fc.showSaveDialog(this);
		if(res != JFileChooser.APPROVE_OPTION) return;
		
		String exportDir = fc.getSelectedFile().getAbsolutePath();
		if(core != null){
			core.setSetting(BNKPNL_LAST_EXPORT_PATH, exportDir);
		}
		
		IndefProgressDialog dialog = new IndefProgressDialog(parent, "Please Wait");
		dialog.setPrimaryString("Export in Progress");
		dialog.setSecondaryString("Exporting requested items to " + exportDir);
		
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>(){
			protected Void doInBackground() throws Exception{
				try{
					List<BankNode> sel = lstBanks.getSelectedValuesList();
					for(BankNode node : sel) {
						ZeqerBank bnk = node.data;
						if(bnk == null) continue;
						String bnkname = bnk.getTableEntry().getName();
						String expath = exportDir + File.separator + bnkname + ".xml";
						dialog.setSecondaryString("Exporting " + expath);
						ZeqerBankIO.exportDecompXML(expath, bnk);
					}
				}
				catch(Exception ex){
					ex.printStackTrace();
					JOptionPane.showMessageDialog(parent, 
							"Error occurred during export. See stderr for details.", 
							"Export Failed", JOptionPane.ERROR_MESSAGE);
				}
				
				return null;
			}
			
			public void done(){
				dialog.closeMe();
				unsetWait();
			}
		};
		
		dialog.render();
		task.execute();
	}
	
	private void btnImportCallback(){
		//Imports from decomp XML, SF2, DLS. Might add other game formats like SBNK
		
		//Ask user for import file.
		String lastpath = null;
		if(core != null){
			lastpath = core.getSetting(BNKPNL_LAST_IMPORT_PATH);
		}
		JFileChooser fc = new JFileChooser(lastpath);
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		ZeqerBankIO.addImportableFileFilters(fc);
		int res = fc.showOpenDialog(this);
		if(res != JFileChooser.APPROVE_OPTION) return;
		
		String importPath = fc.getSelectedFile().getAbsolutePath();
		if(core != null){
			core.setSetting(BNKPNL_LAST_IMPORT_PATH, importPath);
		}
		
		startImport(importPath);
	}

	
}
