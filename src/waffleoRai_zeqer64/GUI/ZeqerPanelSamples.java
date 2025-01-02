package waffleoRai_zeqer64.GUI;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import java.awt.GridBagLayout;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import waffleoRai_GUITools.WriterPanel;
import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.FileUtils;
import waffleoRai_Utils.VoidCallbackMethod;
import waffleoRai_zeqer64.ErrorCode;
import waffleoRai_zeqer64.GUI.dialogs.WaveImportDialog;
import waffleoRai_zeqer64.GUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_zeqer64.GUI.filters.FlagFilterPanel;
import waffleoRai_zeqer64.GUI.filters.TagFilterPanel;
import waffleoRai_zeqer64.GUI.filters.TextFilterPanel;
import waffleoRai_zeqer64.GUI.filters.ZeqerFilter;
import waffleoRai_zeqer64.filefmt.wave.ZeqerWaveIO;
import waffleoRai_zeqer64.filefmt.wave.ZeqerWaveTable;
import waffleoRai_zeqer64.filefmt.wave.ZeqerWaveIO.SampleImportOptions;
import waffleoRai_zeqer64.filefmt.wave.WaveTableEntry;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;

public class ZeqerPanelSamples extends JPanel{

	private static final long serialVersionUID = 3054393851326234350L;
	
	public static final String SAMPLEPNL_LAST_IMPORT_PATH = "ZSAMPPNL_LASTIMPORT";
	public static final String SAMPLEPNL_LAST_EXPORT_PATH = "ZSAMPPNL_LASTEXPORT";
	
	public static final int FLAGIDX_MUSIC = 0;
	public static final int FLAGIDX_SFX = 1;
	public static final int FLAGIDX_VOICE = 2;
	public static final int FLAGIDX_ENV = 3;
	public static final int FLAGIDX_ACTOR = 4;
	public static final int FLAGIDX_UNUSED = 5;
	
	public static final int FLAGIDX_INST = 0;
	public static final int FLAGIDX_DRUM = 1;
	public static final int FLAGIDX_FX = 2;
	public static final int FLAGIDX_OOTv0 = 3;
	public static final int FLAGIDX_OOT = 4;
	public static final int FLAGIDX_MM = 5;
	public static final int FLAGIDX_OOTv0_MAINWARC = 6;
	public static final int FLAGIDX_OOT_MAINWARC  = 7;
	public static final int FLAGIDX_MM_MAINWARC  = 8;
	public static final int FLAGIDX_USER = 9;

	/*----- Inner Classes -----*/
	
	private static class SampleNode implements Comparable<SampleNode>{
		public WaveTableEntry sample;
		
		public String toString(){
			if(sample == null) return super.toString();
			return sample.getName();
		}
		
		public int hashCode(){
			if(sample == null) return super.hashCode();
			return sample.hashCode();
		}

		public boolean equals(Object o){
			return o == this;
		}
		
		public int compareTo(SampleNode o) {
			//Sorts by name.
			if(o == null) return 1;
			WaveTableEntry tsmpl = this.sample;
			WaveTableEntry osmpl = o.sample;
			if(tsmpl == null){
				if(osmpl == null) return 0;
				return -1;
			}
			if(osmpl == null) return 1;
			
			if(this.sample.getName() == null){
				if(o.sample.getName() == null) return 0;
				return -1;
			}
			
			return sample.getName().compareTo(o.sample.getName());
		}
	}
	
	/*----- Instance Variables -----*/
	
	private ZeqerCoreInterface core;
	
	private JFrame parent;
	private boolean editable = true;
	
	private FilterListPanel<SampleNode> pnlFilt;
	private TagFilterPanel<SampleNode> pnlTags; //For refreshing/adding tags
	
	private List<SampleNode> source;
	
	private JList<SampleNode> list;
	private WriterPanel pnlInfo;
	
	private JButton btnImport;
	private JButton btnExport;
	private JButton btnPlay;
	
	/*----- Init -----*/
	
	/**
	 * @wbp.parser.constructor
	 */
	public ZeqerPanelSamples(JFrame parent_frame, ZeqerCoreInterface core_iface){
		parent = parent_frame;
		core = core_iface;
		initGUI(true);
	}
	
	public ZeqerPanelSamples(JFrame parent_frame, ZeqerCoreInterface core_iface, boolean editable_mode){
		parent = parent_frame;
		core = core_iface;
		initGUI(editable_mode);
	}
	
	private void initGUI(boolean editable_mode){
		editable = editable_mode;
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		pnlFilt = new FilterListPanel<SampleNode>();
		GridBagConstraints gbc_pnlFilt = new GridBagConstraints();
		gbc_pnlFilt.insets = new Insets(0, 0, 0, 5);
		gbc_pnlFilt.fill = GridBagConstraints.BOTH;
		gbc_pnlFilt.gridx = 0;
		gbc_pnlFilt.gridy = 0;
		add(pnlFilt, gbc_pnlFilt);
		
		JPanel pnlList = new JPanel();
		GridBagConstraints gbc_pnlList = new GridBagConstraints();
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
		gbc_spList.insets = new Insets(0, 0, 5, 0);
		gbc_spList.fill = GridBagConstraints.BOTH;
		gbc_spList.gridx = 0;
		gbc_spList.gridy = 0;
		pnlList.add(spList, gbc_spList);
		
		list = new JList<SampleNode>();
		spList.setViewportView(list);
		list.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e) {
				onChangeListSelection();
			}
		});
		list.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() != MouseEvent.BUTTON1){
					onListItemRightClick(e.getX(), e.getY());
				}
			}});
		
		pnlInfo = new WriterPanel();
		GridBagConstraints gbc_pnlInfo = new GridBagConstraints();
		gbc_pnlInfo.insets = new Insets(0, 0, 5, 0);
		gbc_pnlInfo.fill = GridBagConstraints.BOTH;
		gbc_pnlInfo.gridx = 0;
		gbc_pnlInfo.gridy = 1;
		pnlList.add(pnlInfo, gbc_pnlInfo);
		
		JPanel pnlCtrl = new JPanel();
		GridBagConstraints gbc_pnlCtrl = new GridBagConstraints();
		gbc_pnlCtrl.fill = GridBagConstraints.BOTH;
		gbc_pnlCtrl.gridx = 0;
		gbc_pnlCtrl.gridy = 2;
		pnlList.add(pnlCtrl, gbc_pnlCtrl);
		GridBagLayout gbl_pnlCtrl = new GridBagLayout();
		gbl_pnlCtrl.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_pnlCtrl.rowHeights = new int[]{0, 0};
		gbl_pnlCtrl.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlCtrl.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlCtrl.setLayout(gbl_pnlCtrl);
		
		if(editable_mode){
			btnImport = new JButton("Import...");
			GridBagConstraints gbc_btnImport = new GridBagConstraints();
			gbc_btnImport.insets = new Insets(5, 5, 5, 5);
			gbc_btnImport.gridx = 0;
			gbc_btnImport.gridy = 0;
			pnlCtrl.add(btnImport, gbc_btnImport);
			btnImport.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					onButton_import();
				}
			});
			
			btnExport = new JButton("Export...");
			GridBagConstraints gbc_btnExport = new GridBagConstraints();
			gbc_btnExport.insets = new Insets(5, 5, 5, 5);
			gbc_btnExport.gridx = 1;
			gbc_btnExport.gridy = 0;
			pnlCtrl.add(btnExport, gbc_btnExport);
			btnExport.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					onButton_export();
				}
			});
			btnExport.setEnabled(false);
		}
		
		btnPlay = new JButton("Play");
		GridBagConstraints gbc_btnPlay = new GridBagConstraints();
		gbc_btnPlay.insets = new Insets(5, 5, 5, 5);
		gbc_btnPlay.gridx = 4;
		gbc_btnPlay.gridy = 0;
		pnlCtrl.add(btnPlay, gbc_btnPlay);
		btnPlay.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				//onButton_play();
				dummyCallback();
			}
		});
		btnPlay.setEnabled(false);
		
		addFilterPanels();
		drawToInfoPanel(null);
		
		if(core != null){
			addSamples(core.getAllRegisteredSamples());
		}
	}
	
	private void addFilterPanels(){
		//Text search
		TextFilterPanel<SampleNode> fpnl1 = new TextFilterPanel<SampleNode>(0);
		fpnl1.setSearchFilter(new ZeqerFilter<SampleNode>(){
			public boolean itemPasses(SampleNode item, String txt, long flags){
				if(item == null) return false;
				if(txt == null || txt.isEmpty()) return true;
				String name = item.sample.getName();
				if(name == null) return false;
				return name.toLowerCase().contains(txt.toLowerCase());
			}
		});
		pnlFilt.addPanel(fpnl1);
		
		//Tags (Maybe? I don't have tags on them right now, but they may be helpful.)
		pnlTags = new TagFilterPanel<SampleNode>(parent, core.getString(ZeqerGUIUtils.STRKEY_FILPNL_TAGS));
		
		pnlFilt.addPanel(pnlTags);
		
		//Flags 1
		FlagFilterPanel<SampleNode> fpnl2 = new FlagFilterPanel<SampleNode>(core.getString(ZeqerGUIUtils.STRKEY_FILPNL_FLAGS));
		fpnl2.addSwitch(new ZeqerFilter<SampleNode>(){
			public boolean itemPasses(SampleNode item){
				if(item == null) return false;
				return item.sample.isFlagSet(ZeqerWaveTable.FLAG_ISMUSIC);
			}
		}, "Music", true);
		fpnl2.addSwitch(new ZeqerFilter<SampleNode>(){
			public boolean itemPasses(SampleNode item){
				if(item == null) return false;
				return item.sample.isFlagSet(ZeqerWaveTable.FLAG_ISSFX);
			}
		}, "SFX", true);
		fpnl2.addSwitch(new ZeqerFilter<SampleNode>(){
			public boolean itemPasses(SampleNode item){
				if(item == null) return false;
				return item.sample.isFlagSet(ZeqerWaveTable.FLAG_ISVOX);
			}
		}, "Voice", true);
		fpnl2.addSwitch(new ZeqerFilter<SampleNode>(){
			public boolean itemPasses(SampleNode item){
				if(item == null) return false;
				return item.sample.isFlagSet(ZeqerWaveTable.FLAG_ISENV);
			}
		}, "Environment", true);
		fpnl2.addSwitch(new ZeqerFilter<SampleNode>(){
			public boolean itemPasses(SampleNode item){
				if(item == null) return false;
				return item.sample.isFlagSet(ZeqerWaveTable.FLAG_ISACTOR);
			}
		}, "Actor", true);
		fpnl2.addSwitch(new ZeqerFilter<SampleNode>(){
			public boolean itemPasses(SampleNode item){
				if(item == null) return false;
				return item.sample.isFlagSet(ZeqerWaveTable.FLAG_ISUNUSED);
			}
		}, "Unused", true);
		pnlFilt.addPanel(fpnl2);
		
		//Flags 2
		FlagFilterPanel<SampleNode> fpnl3 = new FlagFilterPanel<SampleNode>(core.getString(ZeqerGUIUtils.STRKEY_FILPNL_ADDOP));
		fpnl3.addSwitch(new ZeqerFilter<SampleNode>(){
			public boolean itemPasses(SampleNode item){
				if(item == null) return false;
				return item.sample.isFlagSet(ZeqerWaveTable.FLAG_ISIN_INST);
			}
		}, "Used in Inst", true);
		fpnl3.addSwitch(new ZeqerFilter<SampleNode>(){
			public boolean itemPasses(SampleNode item){
				if(item == null) return false;
				return item.sample.isFlagSet(ZeqerWaveTable.FLAG_ISIN_PERC);
			}
		}, "Used in Drum", true);
		fpnl3.addSwitch(new ZeqerFilter<SampleNode>(){
			public boolean itemPasses(SampleNode item){
				if(item == null) return false;
				return item.sample.isFlagSet(ZeqerWaveTable.FLAG_ISIN_SFX);
			}
		}, "Used in FX", true);
		fpnl3.addSwitch(new ZeqerFilter<SampleNode>(){
			public boolean itemPasses(SampleNode item){
				if(item == null) return false;
				return item.sample.isFlagSet(ZeqerWaveTable.FLAG_ISIN_OOTv0);
			}
		}, "In Ocarina 1.0", true);
		fpnl3.addSwitch(new ZeqerFilter<SampleNode>(){
			public boolean itemPasses(SampleNode item){
				if(item == null) return false;
				return item.sample.isFlagSet(ZeqerWaveTable.FLAG_ISIN_OOT);
			}
		}, "In Ocarina", true);
		fpnl3.addSwitch(new ZeqerFilter<SampleNode>(){
			public boolean itemPasses(SampleNode item){
				if(item == null) return false;
				return item.sample.isFlagSet(ZeqerWaveTable.FLAG_ISIN_MM);
			}
		}, "In Majora", true);
		fpnl3.addSwitch(new ZeqerFilter<SampleNode>(){
			public boolean itemPasses(SampleNode item){
				if(item == null) return false;
				return item.sample.isFlagSet(ZeqerWaveTable.FLAG_ISIN_OOTv0_MAINARC);
			}
		}, "OoT 1.0 Main Sample Bank", true);
		fpnl3.addSwitch(new ZeqerFilter<SampleNode>(){
			public boolean itemPasses(SampleNode item){
				if(item == null) return false;
				return item.sample.isFlagSet(ZeqerWaveTable.FLAG_ISIN_OOT_MAINARC);
			}
		}, "OoT Main Sample Bank", true);
		fpnl3.addSwitch(new ZeqerFilter<SampleNode>(){
			public boolean itemPasses(SampleNode item){
				if(item == null) return false;
				return item.sample.isFlagSet(ZeqerWaveTable.FLAG_ISIN_MM_MAINARC);
			}
		}, "MM Main Sample Bank", true);
		fpnl3.addSwitch(new ZeqerFilter<SampleNode>(){
			public boolean itemPasses(SampleNode item){
				if(item == null) return false;
				return item.sample.isFlagSet(ZeqerWaveTable.FLAG_ISCUSTOM);
			}
		}, "User Imported", true);
		pnlFilt.addPanel(fpnl3);
		
		//Add refresh listener...
		pnlFilt.addRefilterCallback(new VoidCallbackMethod(){
			public void doMethod() {refilter();}
		});
	}
	
	/*----- Getters -----*/
	
	public WaveTableEntry getSelectedSample(){
		SampleNode selnode = list.getSelectedValue();
		if(selnode == null) return null;
		return selnode.sample;
	}
	
	/*----- Setters -----*/
	
	public void addSamples(Collection<WaveTableEntry> samples){
		if(samples == null){
			source = null;
		}
		else{
			source = new ArrayList<SampleNode>(samples.size()+1);
			for(WaveTableEntry e : samples){
				SampleNode node = new SampleNode();
				node.sample = e;
				source.add(node);
			}
			Collections.sort(source);
		}
		
		updateTagPool();
		updateList();
	}
	
	/*----- Draw -----*/
	
	public void disableAll(){
		pnlInfo.setEnabled(false);
		pnlFilt.disableAll();
		list.setEnabled(false);
		if(btnImport != null) btnImport.setEnabled(false); 
		if(btnExport != null) btnExport.setEnabled(false);
		btnPlay.setEnabled(false);
	}
	
	public void reenable(){
		pnlInfo.setEnabled(true);
		pnlFilt.enableAll();
		list.setEnabled(true);
		if(btnImport != null) btnImport.setEnabled(true); 
		if(btnExport != null) btnExport.setEnabled(!list.isSelectionEmpty());
		btnPlay.setEnabled(!list.isSelectionEmpty());
	}
	
	public void setWait(){
		disableAll();
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	public void unsetWait(){
		reenable();
		setCursor(null);
	}
	
	private void updateTagPool(){
		Set<String> tagpool = new HashSet<String>();
		if(source != null){
			for(SampleNode node : source){
				Set<String> tags = node.sample.getTagSet();
				if(tags != null){
					tagpool.addAll(tags);
				}
			}
		}
		pnlTags.addTagPool(tagpool, new ZeqerFilter<SampleNode>(){
			public boolean itemPasses(SampleNode item, String txt){
				if(item == null || item.sample == null) return false;
				Set<String> tags = item.sample.getTagSet();
				if(tags == null) return false;
				return tags.contains(txt);
			}
		});
		updateList();
	}
	
	private void updateList(){
		DefaultListModel<SampleNode> mdl = new DefaultListModel<SampleNode>();
		if(source != null){
			for(SampleNode smpl : source){
				if(pnlFilt.itemPassesFilters(smpl)){
					mdl.addElement(smpl);
				}
			}
		}
		list.setModel(mdl);
		list.repaint();
		onChangeListSelection();
	}
	
	private void drawToInfoPanel(SampleNode node){
		try{
			pnlInfo.clear();
			Writer writer = pnlInfo.getWriter();
			if(node == null || node.sample == null){
				writer.write("<No sample selected>\n");
			}
			else{
				WaveTableEntry sample = node.sample;
				Z64WaveInfo info = sample.getWaveInfo();
				writer.write(sample.getName() + "\n");
				writer.write("UID: " + String.format("%08x", info.getUID()) + "\n");
				writer.write("Codec: " + Z64Sound.getCodecString(info.getCodec(), false) + "\n");
				writer.write("Medium: " + Z64Sound.getMediumTypeString(info.getMedium()) + "\n");
				writer.write("Wave Size: 0x" + Integer.toHexString(info.getWaveSize()) + "\n");
				writer.write("Frame Count: " + info.getFrameCount() + "\n");
				writer.write("Loop: " + info.getLoopStart() + "\n");
				writer.write("Loop Count: " + info.getLoopCount() + "\n");
				writer.write("MD5: " + FileUtils.bytes2str(sample.getMD5()) + "\n");
				
				writer.write("Tags: ");
				boolean first = true;
				Set<String> tags = sample.getTagSet();
				if(tags != null && !tags.isEmpty()){
					for(String tag : tags){
						if(!first) writer.write(";");
						writer.write(tag);
						first = false;	
					}
				}
				else writer.write("<None>");
				writer.write("\n");
				
				writer.write("Flags: ");
				first = true;
				int mask = 1;
				for(int i = 0; i < ZeqerWaveTable.FLAG_NAMES.length; i++){
					if(sample.isFlagSet(mask)){
						if(!first)writer.write(";");
						writer.write(ZeqerWaveTable.FLAG_NAMES[i]);
						first = false;
					}
					mask <<= 1;
				}
			}
			writer.close();
		}
		catch(Exception ex){
			ex.printStackTrace();
			showError("ERROR: Could not render information for selected sample!");
		}
		pnlInfo.repaint();
	}
	
	/*----- Right Click Menu -----*/
	
	private class RCMenu extends JPopupMenu{
		
		private static final long serialVersionUID = -4805616678195512144L;

		private SampleNode target;
		
		public RCMenu(SampleNode node){
			target = node;
			if(target == null) return;
			if(target.sample == null) return;
			boolean tUser = core.isEditableSample(target.sample.getUID());
			
			JMenuItem opEdit = new JMenuItem("Edit Loop Points...");
			add(opEdit);
			if(editable && tUser){
				opEdit.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						onMenuItemEditLoop(target);
					}});
			}
			else{
				opEdit.setEnabled(false);
			}
			
			JMenuItem opDelete = new JMenuItem("Delete...");
			add(opDelete);
			if(editable && tUser){
				opDelete.addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						onMenuItemDelete();
					}});
			}
			else{
				opDelete.setEnabled(false);
			}
		}
	}
	
	/*----- Actions -----*/
	
	private void dummyCallback(){
		showInfo("Sorry, this component doesn't work yet!");
	}
	
	public void refreshSamplePool(){
		setWait();
		if(core != null){
			addSamples(core.getAllRegisteredSamples());
		}
		unsetWait();
	}
	
	public void refilter(){
		updateList();
	}
	
	private void onChangeListSelection(){
		SampleNode selnode = list.getSelectedValue();
		if(btnExport != null) btnExport.setEnabled(!list.isSelectionEmpty());
		btnPlay.setEnabled(!list.isSelectionEmpty());
		drawToInfoPanel(selnode);
	}
	
	private void onButton_play(){
		setWait();
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>(){
			protected Void doInBackground() throws Exception{
				try{
					if(core != null){
						core.playSample(getSelectedSample());
					}
				}
				catch(Exception ex){
					ex.printStackTrace();
					showError("Sample could not be played!");
				}
				return null;
			}
			
			public void done(){
				unsetWait();
			}
		};
		task.execute();
	}
	
	private void onButton_export(){
		
		//If only one selected, allow user to name file. Otherwise, only pick target dir.
		if(core == null) return;
		if(list.isSelectionEmpty()) return;
		List<SampleNode> sel = list.getSelectedValuesList();
		if(sel == null) return; //Safety check only.
		
		boolean multi = sel.size() > 1;
		String nameStem = null;
		JFileChooser fc = new JFileChooser(core.getSetting(SAMPLEPNL_LAST_EXPORT_PATH));
		if(multi){
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		else{
			String ext = core.getSampleExportFormatExtention();
			String desc = core.getSampleExportFormatDescription();
			
			if(ext != null && desc != null){
				fc.addChoosableFileFilter(new FileFilter(){
					public boolean accept(File f) {
						if(f.isDirectory()) return true;
						String name = f.getAbsolutePath();
						if(name == null) return false;
						return name.endsWith("." + ext);
					}

					public String getDescription() {
						return desc + " (." + ext + ")";
					}});	
			}
			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		}
		
		int op = fc.showSaveDialog(this);
		if(op != JFileChooser.APPROVE_OPTION) return;
		nameStem = fc.getSelectedFile().getAbsolutePath();
		core.setSetting(SAMPLEPNL_LAST_EXPORT_PATH, nameStem);
		
		IndefProgressDialog dialog = new IndefProgressDialog(parent, "Please Wait");
		dialog.setPrimaryString("Export Sample");
		dialog.setSecondaryString("Exporting to " + nameStem);
	
		final String FNAME = nameStem;
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>(){
			protected Void doInBackground() throws Exception{
				try{
					if(multi){
						List<SampleNode> sel = list.getSelectedValuesList();
						if(sel != null){
							for(SampleNode samp : sel){
								core.exportSample(samp.sample, FNAME + File.separator + samp.sample.getName());		
							}
						}
					}
					else{
						SampleNode samp = list.getSelectedValue();
						core.exportSample(samp.sample, FNAME);
					}
				}
				catch(Exception ex){
					ex.printStackTrace();
					showError("Export failed. See stderr for details.");
				}
				return null;
			}
			
			public void done(){
				dialog.closeMe();
			}
		};
		task.execute();
		dialog.render();
	}
	
	private void onButton_import(){
		if(core == null) return;
		
		WaveImportDialog waveimport = new WaveImportDialog(parent, core);
		waveimport.showMe(this);
		
		if(!waveimport.getExitSelection()) return;
		SampleImportOptions ops = new SampleImportOptions();
		waveimport.getSettings(ops);
		
		if(ops.loopCount < -1) ops.loopCount = -1;
		final Set<String> tags = waveimport.getTags();
		final List<String> paths = waveimport.getFilePaths();
		
		IndefProgressDialog dialog = new IndefProgressDialog(parent, "Please Wait");
		dialog.setPrimaryString("Import Samples");
		dialog.setSecondaryString("Initializing import");
		
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>(){
			protected Void doInBackground() throws Exception{
				try{
					if(core != null){
						ErrorCode err = new ErrorCode();
						for(String path : paths){
							if(path == null || path.isEmpty()) continue;
							dialog.setSecondaryString("Importing " + path);
							
							boolean noname = (ops.namestem == null) || (ops.namestem.isEmpty());
							if(noname){
								//Name from file
								ops.namestem = path;
								int i = ops.namestem.lastIndexOf(File.separatorChar);
								if(i >= 0) ops.namestem = ops.namestem.substring(i+1);
								i = ops.namestem.lastIndexOf('.');
								if(i >= 0) ops.namestem = ops.namestem.substring(0, i);
							}
							
							WaveTableEntry imprt = core.importSample(path, ops, err);
							if(imprt == null || err.value != ZeqerWaveIO.ERROR_CODE_NONE){
								String errmsg = "Import of \"" + path + "\" failed! Reason: ";
								switch(err.value){
								case ZeqerWaveIO.ERROR_CODE_UNKNOWN:
									errmsg += " Unknown";
									break;
								case ZeqerWaveIO.ERROR_CODE_IO:
									errmsg += " I/O Error";
									break;
								case ZeqerWaveIO.ERROR_CODE_INPARSE:
									errmsg += " Input file parsing error";
									break;
								case ZeqerWaveIO.ERROR_CODE_DOWNSAMPLE_FAIL:
									errmsg += " Input could not be downsampled";
									break;
								case ZeqerWaveIO.ERROR_CODE_TBLGEN_FAIL:
									errmsg += " ADPCM table generation failed";
									break;
								case ZeqerWaveIO.ERROR_CODE_ENCODE_FAIL:
									errmsg += " ADPCM encoding failed";
									break;
								case ZeqerWaveIO.ERROR_CODE_AIFF_COMPRESSION_UNKNOWN:
									errmsg += " AIFF/AIFC compression codec not recognized";
									break;
								case ZeqerWaveIO.ERROR_CODE_INPUT_FMT_UNKNOWN:
									errmsg += " Input format unknown";
									break;
								case ZeqerWaveIO.ERROR_CODE_TABLE_IMPORT_FAILED:
									errmsg += " Import to Zeqer user table failed";
									break;
								case ZeqerWaveIO.ERROR_CODE_MULTICHAN_NOT_SUPPORTED:
									errmsg += " Multi-channel support error";
									break;
								}
								
								showError(errmsg);
								break;
							}
							
							//Add tags
							for(String tag : tags){
								imprt.addTag(tag);
							}
							
							//Add node
							SampleNode node = new SampleNode();
							node.sample = imprt;
							source.add(node);
							
							if(noname) ops.namestem = null;
						}

						Collections.sort(source);
						updateTagPool();
						updateList();
					}
				}
				catch(Exception ex){
					ex.printStackTrace();
					showError("Import failed. See stderr for details.");
				}
				return null;
			}
			
			public void done(){
				dialog.closeMe();
			}
		};
		task.execute();
		dialog.render();
	}
	
	private void onListItemRightClick(int x, int y) {
		if(!editable) return;
		if(list.isSelectionEmpty()) return;
		RCMenu menu = new RCMenu(list.getSelectedValue());
		menu.show(list, x, y);
	}
	
	private void onMenuItemDelete() {
		if(list.isSelectionEmpty()) return;
		if(core == null) return;
		
		//Asks to delete ALL customs that are selected, so menu target doesn't matter
		int ret = JOptionPane.showConfirmDialog(this, 
				"Are you sure you want to delete selected sample(s)?", 
				"Delete Samples", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		
		if(ret != JOptionPane.YES_OPTION) return;
		
		int reselIdx = list.getSelectedIndex();
		List<SampleNode> sellist = list.getSelectedValuesList();
		int selCount = sellist.size();
		
		int count = 0;
		for(SampleNode sel : sellist) {
			if(sel.sample == null) continue;
			int id = sel.sample.getUID();
			if(!core.isEditableSample(id)) continue;
			if(core.deleteSample(id)) count++;
		}
		
		if(count == selCount) {
			JOptionPane.showMessageDialog(this, count + " sample(s) successfully removed!", 
					"Delete Samples", JOptionPane.INFORMATION_MESSAGE);
		}
		else {
			if(count == 0) {
				JOptionPane.showMessageDialog(this, "Sample deletion failed! See stderr for details.", 
						"Delete Samples", JOptionPane.ERROR_MESSAGE);
			}
			else {
				int diff = selCount - count;
				JOptionPane.showMessageDialog(this, diff + " sample(s) could not be removed.", 
						"Delete Samples", JOptionPane.WARNING_MESSAGE);
			}
		}
	
		
		refreshSamplePool();
		
		reselIdx--;
		if(reselIdx < 0) reselIdx = 0;
		list.setSelectedIndex(reselIdx);
	}
	
	private void onMenuItemEditLoop(SampleNode node) {
		//TODO
		if(core == null) return;
		if(node == null) return;
		if(node.sample == null) return;
		
		int id = node.sample.getUID();
		if(!core.isEditableSample(id)) {
			showError("Selected sample cannot be edited!");
			return;
		}
		
		//TODO
		//Need a new dialog.....
		dummyCallback();
	}
	
	/*----- Text Boxes -----*/
	
	public void showWarning(String text){
		JOptionPane.showMessageDialog(this, text, "Warning", JOptionPane.WARNING_MESSAGE);
	}
	
	public void showError(String text){
		JOptionPane.showMessageDialog(this, text, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	public void showInfo(String text){
		JOptionPane.showMessageDialog(this, text, "Notice", JOptionPane.INFORMATION_MESSAGE);
	}
	
}
