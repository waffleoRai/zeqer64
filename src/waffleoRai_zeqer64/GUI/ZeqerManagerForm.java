package waffleoRai_zeqer64.GUI;

import javax.swing.JFrame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTabbedPane;

import waffleoRai_GUITools.GUITools;
import waffleoRai_Utils.VoidCallbackMethod;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class ZeqerManagerForm extends JFrame{

	private static final long serialVersionUID = -2977885456814658055L;
	
	public static final int MIN_WIDTH = 600;
	public static final int MIN_HEIGHT = 400;
	
	private static final String STRKEY_MAIN_TITLE = "MAINFORM_T";
	private static final String STRKEY_MENU_FILE = "MAINFORM_MENU_FILE";
	private static final String STRKEY_MITEM_F_SAVE = "MAINFORM_MNUI_F_SAVE";
	private static final String STRKEY_MITEM_F_UNINST = "MAINFORM_MNUI_F_UNINSTALL";
	private static final String STRKEY_MITEM_F_EXIT = "MAINFORM_MNUI_F_EXIT";
	
	private static final String STRKEY_TAB_ROMS = "MAINFORM_TAB_ROMS";
	private static final String STRKEY_TAB_INST = "MAINFORM_TAB_INST";
	private static final String STRKEY_TAB_WAV = "MAINFORM_TAB_SMPL";

	/*----- Instance Variables -----*/
	
	private ZeqerCoreInterface core;
	
	private ZeqerPanelRoms pnlRoms;
	private ZeqerPanelInstruments pnlInst;
	private ZeqerPanelSamples pnlSmpl;
	
	/*----- Init -----*/
	
	public ZeqerManagerForm(ZeqerCoreInterface core_link){
		core = core_link;
		initGUI();
	}
	
	private void initGUI(){
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		
		setTitle(getString(STRKEY_MAIN_TITLE));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.LEFT);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 0;
		getContentPane().add(tabbedPane, gbc_tabbedPane);
		
		pnlRoms = new ZeqerPanelRoms(core, this);
		tabbedPane.addTab(getString(STRKEY_TAB_ROMS), null, pnlRoms, null);
		pnlRoms.setImportCallback(new VoidCallbackMethod(){
			public void doMethod() {onRomImport();}
		});
		
		JPanel pnlAbld = new JPanel();
		tabbedPane.addTab("Audio Builds", null, pnlAbld, null);
		
		JPanel pnlSeq = new JPanel();
		tabbedPane.addTab("Sequences", null, pnlSeq, null);
		
		JPanel pnlFont = new JPanel();
		tabbedPane.addTab("Soundfonts", null, pnlFont, null);
		
		pnlInst = new ZeqerPanelInstruments(this, core, true);
		tabbedPane.addTab(getString(STRKEY_TAB_INST), null, pnlInst, null);
		
		pnlSmpl = new ZeqerPanelSamples(this, core, true);
		tabbedPane.addTab(getString(STRKEY_TAB_WAV), null, pnlSmpl, null);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu(getString(STRKEY_MENU_FILE));
		menuBar.add(mnFile);
		
		JMenuItem mntmSaveWorkspace = new JMenuItem(getString(STRKEY_MITEM_F_SAVE));
		mnFile.add(mntmSaveWorkspace);
		mntmSaveWorkspace.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				mntmFileSaveCallback();
			}});
		
		JMenuItem mntmUninstallZeqer = new JMenuItem(getString(STRKEY_MITEM_F_UNINST));
		mnFile.add(mntmUninstallZeqer);
		mntmUninstallZeqer.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				mntmFileUninstallCallback();
			}});
		
		JMenuItem mntmExit = new JMenuItem(getString(STRKEY_MITEM_F_EXIT));
		mnFile.add(mntmExit);
		mntmExit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				mntmFileExitCallback();
			}});
	}
	
	public void render(){
		pack();
		setLocation(GUITools.getScreenCenteringCoordinates(this));
		setVisible(true);
	}
	
	/*----- Getters -----*/
	
	protected String getString(String key){
		if(core == null) return key;
		return core.getString(key);
	}
	
	/*----- Setters -----*/
	
	/*----- Draw -----*/
	
	/*----- Callbacks -----*/
	
	public void onRomImport(){
		pnlSmpl.refreshSamplePool();
		pnlInst.refreshPresetPool();
	}
	
	private void mntmFileSaveCallback(){
		//TODO
	}
	
	private void mntmFileUninstallCallback(){
		//TODO
	}
	
	private void mntmFileExitCallback(){
		//TODO
	}
	
}
