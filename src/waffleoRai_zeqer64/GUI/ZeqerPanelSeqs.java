package waffleoRai_zeqer64.GUI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.WRPanel;
import waffleoRai_GUITools.WriterPanel;
import waffleoRai_Utils.VoidCallbackMethod;
import waffleoRai_zeqer64.ZeqerSeq;
import waffleoRai_zeqer64.GUI.dialogs.ZeqerSeqHubDialog;
import waffleoRai_zeqer64.GUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_zeqer64.GUI.filters.FlagFilterPanel;
import waffleoRai_zeqer64.GUI.filters.TagFilterPanel;
import waffleoRai_zeqer64.GUI.filters.TextFilterPanel;
import waffleoRai_zeqer64.GUI.filters.ZeqerFilter;
import waffleoRai_zeqer64.filefmt.bank.BankTableEntry;
import waffleoRai_zeqer64.filefmt.seq.ZeqerSeqTable;
import waffleoRai_zeqer64.filefmt.seq.SeqTableEntry;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;
import java.awt.GridBagLayout;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.Writer;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.DefaultListModel;
import javax.swing.JButton;

public class ZeqerPanelSeqs extends WRPanel{

	private static final long serialVersionUID = -3624927390957947243L;
	
	/*----- Inner Classes -----*/
	
	private static class SeqNode implements Comparable<SeqNode>{
		public ZeqerSeq data;
		
		public boolean equals(Object o){
			return o == this;
		}
		
		public String toString(){
			if(data == null) return "<empty>";
			SeqTableEntry entry = data.getTableEntry();
			if(entry == null) return "<No Data>";
			
			String name = entry.getName();
			if(name != null) return name;
			
			return String.format("[Seq %08x]", entry.getUID());
		}

		
		public int compareTo(SeqNode o) {
			if(o == null) return 1;
			if(o == this) return 0;
			
			if(this.data == null){
				if(o.data == null) return 0;
				return -1;
			}
			
			if(o.data == null) return 1;
			
			SeqTableEntry tentry = this.data.getTableEntry();
			SeqTableEntry oentry = o.data.getTableEntry();
			
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
	private ComponentGroup selectEnable;
	private ComponentGroup cgEditable;
	
	private boolean editable = true;
	private List<SeqNode> source;
	
	private FilterListPanel<SeqNode> pnlFilt;
	private TagFilterPanel<SeqNode> pnlTags;
	private JList<SeqNode> lstSeqs;
	private WriterPanel pnlInfo;
	
	private ZeqerSeqHubDialog editDialog;
	private boolean editDialogEditable;
	private SeqNode editNode;
	
	/*----- Init -----*/
	
	public ZeqerPanelSeqs(JFrame parentFrame, ZeqerCoreInterface coreIFace, boolean readOnly){
		parent = parentFrame;
		core = coreIFace;
		editable = !readOnly;
		
		globalEnable = new ComponentGroup();
		selectEnable = globalEnable.newChild();
		cgEditable = globalEnable.newChild();
		
		initGUI();
		updateSeqPool();
	}
	
	private void initGUI(){
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		pnlFilt = new FilterListPanel<SeqNode>();
		GridBagConstraints gbc_pnlFilt = new GridBagConstraints();
		gbc_pnlFilt.insets = new Insets(0, 0, 0, 5);
		gbc_pnlFilt.fill = GridBagConstraints.BOTH;
		gbc_pnlFilt.gridx = 0;
		gbc_pnlFilt.gridy = 0;
		add(pnlFilt, gbc_pnlFilt);
		globalEnable.addComponent("pnlFilt", pnlFilt);
		
		JPanel pnlControl = new JPanel();
		GridBagConstraints gbc_pnlControl = new GridBagConstraints();
		gbc_pnlControl.fill = GridBagConstraints.BOTH;
		gbc_pnlControl.gridx = 1;
		gbc_pnlControl.gridy = 0;
		add(pnlControl, gbc_pnlControl);
		GridBagLayout gbl_pnlControl = new GridBagLayout();
		gbl_pnlControl.columnWidths = new int[]{0, 0};
		gbl_pnlControl.rowHeights = new int[]{0, 0, 0, 0};
		gbl_pnlControl.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlControl.rowWeights = new double[]{1.0, 1.0, 0.0, Double.MIN_VALUE};
		pnlControl.setLayout(gbl_pnlControl);
		
		JScrollPane spList = new JScrollPane();
		spList.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spList = new GridBagConstraints();
		gbc_spList.insets = new Insets(0, 0, 5, 0);
		gbc_spList.fill = GridBagConstraints.BOTH;
		gbc_spList.gridx = 0;
		gbc_spList.gridy = 0;
		pnlControl.add(spList, gbc_spList);
		globalEnable.addComponent("spList", spList);
		
		lstSeqs = new JList<SeqNode>();
		spList.setViewportView(lstSeqs);
		lstSeqs.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e) {
				listSelectionCallback();
			}});
		globalEnable.addComponent("lstSeqs", lstSeqs);
		
		JScrollPane spInfo = new JScrollPane();
		GridBagConstraints gbc_spInfo = new GridBagConstraints();
		gbc_spInfo.insets = new Insets(0, 0, 5, 0);
		gbc_spInfo.fill = GridBagConstraints.BOTH;
		gbc_spInfo.gridx = 0;
		gbc_spInfo.gridy = 1;
		pnlControl.add(spInfo, gbc_spInfo);
		globalEnable.addComponent("spInfo", spInfo);
		
		pnlInfo = new WriterPanel();
		spInfo.setViewportView(pnlInfo);
		globalEnable.addComponent("pnlInfo", pnlInfo);
		
		JPanel pnlBtn = new JPanel();
		GridBagConstraints gbc_pnlBtn = new GridBagConstraints();
		gbc_pnlBtn.fill = GridBagConstraints.BOTH;
		gbc_pnlBtn.gridx = 0;
		gbc_pnlBtn.gridy = 2;
		pnlControl.add(pnlBtn, gbc_pnlBtn);
		GridBagLayout gbl_pnlBtn = new GridBagLayout();
		gbl_pnlBtn.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_pnlBtn.rowHeights = new int[]{0, 0};
		gbl_pnlBtn.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlBtn.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlBtn.setLayout(gbl_pnlBtn);
		
		JButton btnImport = new JButton("Import...");
		GridBagConstraints gbc_btnImport = new GridBagConstraints();
		gbc_btnImport.insets = new Insets(0, 5, 5, 5);
		gbc_btnImport.gridx = 0;
		gbc_btnImport.gridy = 0;
		pnlBtn.add(btnImport, gbc_btnImport);
		btnImport.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnImportCallback();
			}});
		cgEditable.addComponent("btnImport", btnImport);
		
		JButton btnExport = new JButton("Export...");
		GridBagConstraints gbc_btnExport = new GridBagConstraints();
		gbc_btnExport.insets = new Insets(0, 0, 5, 5);
		gbc_btnExport.gridx = 1;
		gbc_btnExport.gridy = 0;
		pnlBtn.add(btnExport, gbc_btnExport);
		btnExport.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnExportCallback();
			}});
		selectEnable.addComponent("btnExport", btnExport);
		
		JButton btnEdit = new JButton("Edit...");
		GridBagConstraints gbc_btnEdit = new GridBagConstraints();
		gbc_btnEdit.insets = new Insets(0, 0, 5, 5);
		gbc_btnEdit.gridx = 2;
		gbc_btnEdit.gridy = 0;
		pnlBtn.add(btnEdit, gbc_btnEdit);
		btnEdit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnEditCallback();
			}});
		cgEditable.addComponent("btnEdit", btnEdit);
		selectEnable.addComponent("btnEdit", btnEdit);
		
		JButton btnDelete = new JButton("Delete");
		GridBagConstraints gbc_btnDelete = new GridBagConstraints();
		gbc_btnDelete.insets = new Insets(0, 0, 5, 5);
		gbc_btnDelete.gridx = 4;
		gbc_btnDelete.gridy = 0;
		pnlBtn.add(btnDelete, gbc_btnDelete);
		btnDelete.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnDeleteCallback();
			}});
		cgEditable.addComponent("btnDelete", btnDelete);
		selectEnable.addComponent("btnDelete", btnDelete);
		
		addFilterPanels();
	}
	
	private void addFilterPanels(){
		//Text search
		TextFilterPanel<SeqNode> fpnl1 = new TextFilterPanel<SeqNode>(0);
		fpnl1.setSearchFilter(new ZeqerFilter<SeqNode>(){
			public boolean itemPasses(SeqNode item, String txt, long flags){
				if(item == null) return false;
				if(txt == null || txt.isEmpty()) return true;
				String name = item.data.getTableEntry().getName();
				if(name == null) return false;
				return name.toLowerCase().contains(txt.toLowerCase());
			}
		});
		pnlFilt.addPanel(fpnl1);
		
		//Tags
		pnlTags = new TagFilterPanel<SeqNode>(parent, core.getString(ZeqerGUIUtils.STRKEY_FILPNL_TAGS));
		pnlFilt.addPanel(pnlTags);
		
		//Flags
		FlagFilterPanel<SeqNode> fpnl2 = new FlagFilterPanel<SeqNode>(core.getString(ZeqerGUIUtils.STRKEY_FILPNL_FLAGS));
		fpnl2.addSwitch(new ZeqerFilter<SeqNode>(){
			public boolean itemPasses(SeqNode item){
				if(item == null) return false;
				if(item.data == null) return false;
				SeqTableEntry entry = item.data.getTableEntry();
				if(entry == null) return false;
				return entry.flagSet(ZeqerSeqTable.FLAG_Z5);
			}
		}, "Ocarina of Time", true);
		fpnl2.addSwitch(new ZeqerFilter<SeqNode>(){
			public boolean itemPasses(SeqNode item){
				if(item == null) return false;
				if(item.data == null) return false;
				SeqTableEntry entry = item.data.getTableEntry();
				if(entry == null) return false;
				return entry.flagSet(ZeqerSeqTable.FLAG_Z6);
			}
		}, "Majora's Mask", true);
		fpnl2.addSwitch(new ZeqerFilter<SeqNode>(){
			public boolean itemPasses(SeqNode item){
				if(item == null) return false;
				if(item.data == null) return false;
				SeqTableEntry entry = item.data.getTableEntry();
				if(entry == null) return false;
				return entry.flagSet(ZeqerSeqTable.FLAG_CUSTOM);
			}
		}, "Custom", true);
		
		fpnl2.addSwitch(new ZeqerFilter<SeqNode>(){
			public boolean itemPasses(SeqNode item){
				if(item == null) return false;
				if(item.data == null) return false;
				SeqTableEntry entry = item.data.getTableEntry();
				if(entry == null) return false;
				
				int seqtype = entry.getSeqType();
				if(seqtype == ZeqerSeq.SEQTYPE_BGM) return true;
				if(seqtype == ZeqerSeq.SEQTYPE_BGM_PIECE) return true;
				if(seqtype == ZeqerSeq.SEQTYPE_JINGLE) return true;
				if(seqtype == ZeqerSeq.SEQTYPE_OCARINA) return true;
				if(seqtype == ZeqerSeq.SEQTYPE_INTRMUS) return true;
				if(seqtype == ZeqerSeq.SEQTYPE_BGM_PROG) return true;
				
				return false;
			}
		}, "Music", true);
		
		fpnl2.addSwitch(new ZeqerFilter<SeqNode>(){
			public boolean itemPasses(SeqNode item){
				if(item == null) return false;
				if(item.data == null) return false;
				SeqTableEntry entry = item.data.getTableEntry();
				if(entry == null) return false;
				
				int seqtype = entry.getSeqType();
				if(seqtype == ZeqerSeq.SEQTYPE_BGM) return true;
				if(seqtype == ZeqerSeq.SEQTYPE_BGM_PIECE) return true;
				if(seqtype == ZeqerSeq.SEQTYPE_BGM_PROG) return true;
				
				return false;
			}
		}, "BGM", true);
		
		fpnl2.addSwitch(new ZeqerFilter<SeqNode>(){
			public boolean itemPasses(SeqNode item){
				if(item == null) return false;
				if(item.data == null) return false;
				SeqTableEntry entry = item.data.getTableEntry();
				if(entry == null) return false;
				
				int seqtype = entry.getSeqType();
				if(seqtype == ZeqerSeq.SEQTYPE_AMBIENT) return true;
				if(seqtype == ZeqerSeq.SEQTYPE_CUTSCENE) return true;
				if(seqtype == ZeqerSeq.SEQTYPE_SFX) return true;
				
				return false;
			}
		}, "SFX Program", true);
		
		fpnl2.addSwitch(new ZeqerFilter<SeqNode>(){
			public boolean itemPasses(SeqNode item){
				if(item == null) return false;
				if(item.data == null) return false;
				SeqTableEntry entry = item.data.getTableEntry();
				if(entry == null) return false;
				
				int seqtype = entry.getSeqType();
				if(seqtype == ZeqerSeq.SEQTYPE_AMBIENT) return true;
				if(seqtype == ZeqerSeq.SEQTYPE_CUTSCENE) return true;
				if(seqtype == ZeqerSeq.SEQTYPE_SFX) return true;
				if(seqtype == ZeqerSeq.SEQTYPE_BGM_PROG) return true;
				
				return false;
			}
		}, "Program", true);
		pnlFilt.addPanel(fpnl2);
		
		pnlFilt.addRefilterCallback(new VoidCallbackMethod(){
			public void doMethod() {refilterCallback();}
		});
	}
	
	/*----- Getters -----*/
	
	public ZeqerSeq getSelectedSeq(){
		SeqNode node = lstSeqs.getSelectedValue();
		if(node == null) return null;
		return node.data;
	}
	
	/*----- Setters -----*/
	
	/*----- Core Pull -----*/
	
	public void updateSeqPool(){
		if(source != null) source.clear();
		if(core == null) return;
		
		List<ZeqerSeq> seqlist = core.getAllValidSeqs();
		DefaultListModel<SeqNode> mdl = new DefaultListModel<SeqNode>();
		if(seqlist != null){
			int alloc = seqlist.size() + 1;
			source = new ArrayList<SeqNode>(alloc);
			for(ZeqerSeq seq : seqlist){
				SeqNode node = new SeqNode();
				node.data = seq;
				source.add(node);
			}
		}
		
		
		Collections.sort(source);
		for(SeqNode node : source){
			mdl.addElement(node);
		}

		updateTagPool();
		lstSeqs.setModel(mdl);
		globalEnable.repaint();
	}
	
	private void updateTagPool(){
		Set<String> tagpool = new HashSet<String>();
		if(source != null){
			for(SeqNode node : source){
				if(node.data == null) continue;
				SeqTableEntry entry = node.data.getTableEntry();
				if(entry == null) continue;
				Collection<String> tags = entry.getTags();
				if(tags != null){
					tagpool.addAll(tags);
				}
			}
		}
		pnlTags.addTagPool(tagpool, new ZeqerFilter<SeqNode>(){
			public boolean itemPasses(SeqNode item, String txt){
				if(item == null || item.data == null) return false;
				SeqTableEntry entry = item.data.getTableEntry();
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
		if(lstSeqs.isSelectionEmpty()) {
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
	}
	
	public void setWait(boolean freezeParent){
		if(freezeParent && parent != null && (parent instanceof ZeqerManagerForm)){
			//This will call this panel's disableAll()
			ZeqerManagerForm zparent = (ZeqerManagerForm)parent;
			zparent.setWait();
		}
		else{
			disableAll();
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			repaint();
		}
	}
	
	public void unsetWait(boolean unfreezeParent){
		if(unfreezeParent && parent != null && (parent instanceof ZeqerManagerForm)){
			ZeqerManagerForm zparent = (ZeqerManagerForm)parent;
			zparent.unsetWait();
		}
		else{
			updateEnabling();
			setCursor(null);
			repaint();
		}
	}
	
	private void printInfoToPanel(SeqNode node){
		pnlInfo.clear();
		try{
			Writer writer = pnlInfo.getWriter();
			if(node != null){
				if(node.data != null){
					SeqTableEntry entry = node.data.getTableEntry();
					if(entry != null){
						String s = entry.getName();
						if(s != null) writer.write(s + "\n");
						else writer.write(String.format("[Untitled Seq %08x]\n", entry.getUID()));
						
						writer.write(String.format("UID: %08x\n", entry.getUID()));
						writer.write("Sequence Type: " + ZeqerGUIUtils.getSeqTypeString(entry.getSeqType()) + "\n");
						writer.write("Default Medium: " + ZeqerGUIUtils.getMediumString(entry.getMedium()) + "\n");
						writer.write("Default Cache Policy: " + ZeqerGUIUtils.getCacheString(entry.getCache()) + "\n");
						
						int bcount = entry.getBankCount();
						if(bcount == 1){
							int bankuid = entry.getPrimaryBankUID();
							writer.write("Soundfont: ");
							if(core != null){
								BankTableEntry bnkinfo = core.getBankInfo(bankuid);
								if(bnkinfo != null){
									String n = bnkinfo.getName();
									if(n != null) writer.write(n);
									else writer.write(String.format("[%08x]", bankuid));
								}
								else writer.write(String.format("[%08x]", bankuid));
							}
							else{
								writer.write(String.format("[%08x]", bankuid));
							}
							writer.write("\n");
						}
						else{
							List<Integer> banks = entry.getLinkedBankUIDs();
							writer.write("Soundfonts:\n");
							for(Integer buid : banks){
								writer.write("\t");
								if(core != null){
									BankTableEntry bnkinfo = core.getBankInfo(buid);
									if(bnkinfo != null){
										String n = bnkinfo.getName();
										if(n != null) writer.write(n);
										else writer.write(String.format("[%08x]", buid));
									}
									else writer.write(String.format("[%08x]", buid));
								}
								else{
									writer.write(String.format("[%08x]", buid));
								}
								writer.write("\n");
							}
						}
						
						//Timestamp
						ZonedDateTime modstamp = entry.getTimeModified();
						if(modstamp != null){
							writer.write("Last Modified: ");
							writer.write(modstamp.format(DateTimeFormatter.RFC_1123_DATE_TIME));
							writer.write("\n");
						}
						
						//Tags
						writer.write("Tags: ");
						Collection<String> tags = entry.getTags();
						if(tags != null && !tags.isEmpty()){
							boolean first = true;
							for(String tag : tags){
								if(!first) writer.write(";");
								else first = false;
								writer.write(tag);
							}
						}
						else{
							writer.write("<None>");
						}
						writer.write("\n");
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
				writer.write("<No seq selected>");
			}
		}
		catch(IOException ex){
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "ERROR: Could not render sequence info. See stderr for details.", 
					"Error", JOptionPane.ERROR_MESSAGE);
		}
		
		pnlInfo.repaint();
	}
	
	/*----- Callbacks -----*/
	
	private void closeEditChildCallback(){
		//System.err.println("ZeqerPanelSeqs.closeEditChildCallback || Called");
		if(editDialog == null) return;
		if(editDialogEditable && editDialog.getExitSelection()){
			editDialog.loadIntoSeq(editNode.data);
			if(!editNode.data.save()){
				JOptionPane.showMessageDialog(this, "Sequence could not be saved! (See stderr for details)", 
						"Save Failed", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		//Unload seq
		editNode.data.unloadData();
		
		editDialog = null;
		editNode = null;
		editDialogEditable = false;
		
		unsetWait(true);
	}
	
	private void refilterCallback(){
		setWait(false);
		DefaultListModel<SeqNode> model = new DefaultListModel<SeqNode>();
		
		for(SeqNode node : source){
			if(node.data == null) continue;
			if(pnlFilt.itemPassesFilters(node)){
				model.addElement(node);
			}
		}
		
		lstSeqs.setModel(model);
		lstSeqs.setSelectedIndex(-1);
		unsetWait(false);
	}
	
	private void listSelectionCallback(){
		printInfoToPanel(lstSeqs.getSelectedValue());
		updateEnabling();
	}
	
	private void btnEditCallback(){
		if(this.lstSeqs.isSelectionEmpty()) return;
		
		editNode = lstSeqs.getSelectedValue();
		if(editNode.data == null){
			JOptionPane.showMessageDialog(this, 
					"Selected sequence has no data!", "Edit Sequence", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		SeqTableEntry entry = editNode.data.getTableEntry();
		if(entry == null){
			JOptionPane.showMessageDialog(this, 
					"Selected sequence is not in table!", "Edit Sequence", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		//Determine if editable
		setWait(true);
		editDialogEditable = editable;
		if(editable){
			editDialogEditable = entry.flagSet(ZeqerSeqTable.FLAG_CUSTOM);
		}
		
		if(!editDialogEditable){
			int ret = JOptionPane.showConfirmDialog(this, 
					"Edits on selected sequence will not be saved. Continue?", 
					"Edit Sequence", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(ret != JOptionPane.YES_OPTION){
				unsetWait(true);
				return;
			}
		}
		
		IndefProgressDialog pdialog = new IndefProgressDialog(parent, "Loading Seq Data");
		pdialog.setPrimaryString("Please wait");
		pdialog.setSecondaryString("Loading sequence data");
		
		ZeqerPanelSeqs me = this;
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>(){

			protected Void doInBackground() throws Exception{
				try{
					editDialog = new ZeqerSeqHubDialog(parent, core, !editDialogEditable);
					editDialog.addWindowListener(new WindowAdapter(){

						public void windowClosing(WindowEvent e) {
							closeEditChildCallback();
						}

						public void windowClosed(WindowEvent e) {
							if(editDialog != null){
								closeEditChildCallback();
							}
						}
						
					});
					
					editNode.data.loadData(true);
					editDialog.loadFromSeq(editNode.data);
				}
				catch(Exception x){
					x.printStackTrace();
					JOptionPane.showMessageDialog(parent, 
							"Unknown Error: Seq load failed! See stderr for details.", 
							"Error", JOptionPane.ERROR_MESSAGE);
				}
				
				return null;
			}
			
			public void done(){
				pdialog.closeMe();
				editDialog.showMe(me);
			}
		};
		
		task.execute();
		pdialog.render();
	}
	
	private void btnDeleteCallback(){
		//TODO
		dummyCallback();
	}
	
	private void btnExportCallback(){
		//TODO
		//Can export to MIDI, MML script, or raw binary
		dummyCallback();
	}
	
	private void btnImportCallback(){
		//TODO
		//Can import from MIDI, MML script, or raw binary
		//Could probably add crazy sequence types like SSEQ etc. too.
		dummyCallback();
	}

}
