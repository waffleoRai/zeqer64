package waffleoRai_zeqer64.GUI;

import javax.swing.JFrame;
import javax.swing.JPanel;

import waffleoRai_zeqer64.ZeqerCoreInterface;
import waffleoRai_zeqer64.ZeqerPreset;
import waffleoRai_zeqer64.GUI.filters.FlagFilterPanel;
import waffleoRai_zeqer64.GUI.filters.TagFilterPanel;
import waffleoRai_zeqer64.GUI.filters.TextFilterPanel;
import waffleoRai_zeqer64.GUI.filters.ZeqerFilter;
import waffleoRai_zeqer64.filefmt.ZeqerWaveTable;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.border.BevelBorder;
import javax.swing.JButton;

public class ZeqerPanelInstruments extends JPanel{
	
	//TODO need a subpanel class for playing on the piano or playing using a simple seq
	//	Playback will probably have to involve sending a short command to native code for synthesis

	private static final long serialVersionUID = -6390624749239280331L;
	
	public static final String SAMPLEPNL_LAST_IMPORT_PATH = "ZINSTPNL_LASTIMPORT";
	public static final String SAMPLEPNL_LAST_EXPORT_PATH = "ZINSTPNL_LASTEXPORT";
	
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
	
	private static class InstNode implements Comparable<InstNode>{

		public ZeqerPreset preset;
		
		@Override
		public int compareTo(InstNode o) {
			// TODO Auto-generated method stub
			return 0;
		}
		
	}
	
	/*----- Instance Variables -----*/
	
	private ZeqerCoreInterface core;
	
	private JFrame parent;
	
	private FilterListPanel<InstNode> pnlFilt;
	private TagFilterPanel<InstNode> pnlTags; //For refreshing/adding tags
	
	/*----- Init -----*/
	
	public ZeqerPanelInstruments(JFrame parent_frame, ZeqerCoreInterface core_iface){
		parent = parent_frame;
		core = core_iface;
		initGUI();
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
		
		JList<InstNode> list = new JList<InstNode>();
		spList.setViewportView(list);
		
		JPanel pnlInfo = new JPanel();
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
		
		JButton btnEdit = new JButton("Edit...");
		GridBagConstraints gbc_btnEdit = new GridBagConstraints();
		gbc_btnEdit.insets = new Insets(0, 0, 0, 5);
		gbc_btnEdit.gridx = 1;
		gbc_btnEdit.gridy = 0;
		pnlCtrl.add(btnEdit, gbc_btnEdit);
		
		JButton btnDelete = new JButton("Delete");
		GridBagConstraints gbc_btnDelete = new GridBagConstraints();
		gbc_btnDelete.gridx = 3;
		gbc_btnDelete.gridy = 0;
		pnlCtrl.add(btnDelete, gbc_btnDelete);
		
		JPanel pnlPlay = new JPanel();
		GridBagConstraints gbc_pnlPlay = new GridBagConstraints();
		gbc_pnlPlay.gridwidth = 2;
		gbc_pnlPlay.fill = GridBagConstraints.BOTH;
		gbc_pnlPlay.gridx = 0;
		gbc_pnlPlay.gridy = 1;
		add(pnlPlay, gbc_pnlPlay);
		
		addFilterPanels();
	}
	
	private void addFilterPanels(){
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
		
		pnlTags = new TagFilterPanel<InstNode>(parent);
		pnlFilt.addPanel(pnlTags);
		
		//Might add flag panels too?
	}
	
	/*----- Getters -----*/
	
	/*----- Setters -----*/
	
	/*----- GUI Management -----*/
	
	private void updateList(){
		//TODO
	}
	
	/*----- Callbacks -----*/
	
}
