package waffleoRai_zeqer64.GUI;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JOptionPane;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import waffleoRai_GUITools.WriterPanel;
import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.FileUtils;
import waffleoRai_Utils.VoidCallbackMethod;
import waffleoRai_zeqer64.ZeqerCoreInterface;
import waffleoRai_zeqer64.GUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_zeqer64.GUI.filters.FlagFilterPanel;
import waffleoRai_zeqer64.GUI.filters.TagFilterPanel;
import waffleoRai_zeqer64.GUI.filters.TextFilterPanel;
import waffleoRai_zeqer64.GUI.filters.ZeqerFilter;
import waffleoRai_zeqer64.filefmt.ZeqerWaveTable;
import waffleoRai_zeqer64.filefmt.ZeqerWaveTable.WaveTableEntry;

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
	
	private Frame parent;
	
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
	public ZeqerPanelSamples(Frame parent_frame, ZeqerCoreInterface core_iface){
		parent = parent_frame;
		core = core_iface;
		initGUI(true);
	}
	
	public ZeqerPanelSamples(Frame parent_frame, ZeqerCoreInterface core_iface, boolean editable_mode){
		parent = parent_frame;
		core = core_iface;
		initGUI(editable_mode);
	}
	
	private void initGUI(boolean editable_mode){
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
				onButton_play();
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
		pnlTags = new TagFilterPanel<SampleNode>(parent, "Tags");
		
		pnlFilt.addPanel(pnlTags);
		
		//Flags 1
		FlagFilterPanel<SampleNode> fpnl2 = new FlagFilterPanel<SampleNode>("Flags");
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
		FlagFilterPanel<SampleNode> fpnl3 = new FlagFilterPanel<SampleNode>("Additional Options");
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
	
	public void setWait(){
		pnlInfo.setEnabled(false);
		pnlFilt.disableAll();
		list.setEnabled(false);
		btnImport.setEnabled(false); 
		btnExport.setEnabled(false);
		btnPlay.setEnabled(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	public void unsetWait(){
		pnlInfo.setEnabled(true);
		pnlFilt.enableAll();
		list.setEnabled(true);
		btnImport.setEnabled(true); 
		btnExport.setEnabled(!list.isSelectionEmpty());
		btnPlay.setEnabled(!list.isSelectionEmpty());
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
	
	/*----- Actions -----*/
	
	public void refilter(){
		updateList();
	}
	
	private void onChangeListSelection(){
		SampleNode selnode = list.getSelectedValue();
		btnExport.setEnabled(!list.isSelectionEmpty());
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
		JFileChooser fc = new JFileChooser(core.getSetting(SAMPLEPNL_LAST_EXPORT_PATH));
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int op = fc.showSaveDialog(this);
		if(op != JFileChooser.APPROVE_OPTION) return;
		String exdir = fc.getSelectedFile().getAbsolutePath();
		core.setSetting(SAMPLEPNL_LAST_EXPORT_PATH, exdir);
		
		IndefProgressDialog dialog = new IndefProgressDialog(parent, "Please Wait");
		dialog.setPrimaryString("Export Sample");
		dialog.setSecondaryString("Exporting to " + exdir);
	
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>(){
			protected Void doInBackground() throws Exception{
				try{
					if(core != null){
						List<SampleNode> sel = list.getSelectedValuesList();
						if(sel != null){
							for(SampleNode samp : sel){
								core.exportSample(samp.sample, exdir + File.separator + samp.sample.getName() + ".wav");		
							}
						}
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
		dialog.render();
		task.execute();
	}
	
	private void onButton_import(){
		if(core == null) return;
		JFileChooser fc = new JFileChooser(core.getSetting(SAMPLEPNL_LAST_IMPORT_PATH));
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		int op = fc.showOpenDialog(this);
		if(op != JFileChooser.APPROVE_OPTION) return;
		String inpath = fc.getSelectedFile().getAbsolutePath();
		core.setSetting(SAMPLEPNL_LAST_IMPORT_PATH, inpath);
		
		IndefProgressDialog dialog = new IndefProgressDialog(parent, "Please Wait");
		dialog.setPrimaryString("Import Sample");
		dialog.setSecondaryString("Importing " + inpath);
	
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>(){
			protected Void doInBackground() throws Exception{
				try{
					if(core != null){
						WaveTableEntry imprt = core.importSample(inpath);
						if(imprt == null){
							showError("File could not be imported!");
							return null;
						}
						SampleNode node = new SampleNode();
						node.sample = imprt;
						source.add(node);
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
		dialog.render();
		task.execute();
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
