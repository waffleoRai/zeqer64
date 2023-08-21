package waffleoRai_zeqer64.GUI;

import javax.swing.JFrame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.GUITools;
import waffleoRai_Utils.VoidCallbackMethod;
import waffleoRai_zeqer64.ZeqerCore;
import waffleoRai_zeqer64.ZeqerInstaller.ZeqerInstallListener;
import waffleoRai_zeqer64.GUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

public class ZeqerManagerForm extends JFrame{

	private static final long serialVersionUID = -2977885456814658055L;
	
	public static final int MIN_WIDTH = 640;
	public static final int MIN_HEIGHT = 480;
	
	public static final int DEFO_WIDTH = 750;
	public static final int DEFO_HEIGHT = 520;
	
	private static final String STRKEY_MAIN_TITLE = "MAINFORM_T";
	private static final String STRKEY_MENU_FILE = "MAINFORM_MENU_FILE";
	private static final String STRKEY_MENU_SYS = "MAINFORM_MENU_SYS";
	private static final String STRKEY_MITEM_F_SAVE = "MAINFORM_MNUI_F_SAVE";
	private static final String STRKEY_MITEM_F_UNINST = "MAINFORM_MNUI_F_UNINSTALL";
	private static final String STRKEY_MITEM_F_EXIT = "MAINFORM_MNUI_F_EXIT";
	private static final String STRKEY_MITEM_S_UPD1 = "MAINFORM_MNUI_S_UPDATE_DEFO";
	private static final String STRKEY_MITEM_S_UPD2 = "MAINFORM_MNUI_S_UPDATE_AVAIL";
	
	private static final String STRKEY_TAB_ROMS = "MAINFORM_TAB_ROMS";
	private static final String STRKEY_TAB_INST = "MAINFORM_TAB_INST";
	private static final String STRKEY_TAB_WAV = "MAINFORM_TAB_SMPL";
	
	private static final String STRKEY_IPROG_INI_A = "INSTALLPROG_INISTART_A";
	private static final String STRKEY_IPROG_INI_B = "INSTALLPROG_INISTART_B";
	private static final String STRKEY_IPROG_FTREAD_A = "INSTALLPROG_FTREAD_A";
	private static final String STRKEY_IPROG_FTREAD_B = "INSTALLPROG_FTREAD_B";
	private static final String STRKEY_IPROG_FEXT_A = "INSTALLPROG_FEXTSTART_A";
	private static final String STRKEY_IPROG_FEXT_B = "INSTALLPROG_FEXTSTART_B";
	
	private static final String STRKEY_UPDCONF_T = "MAINFORM_DIA_UPD_T";
	private static final String STRKEY_UPDCONF_DEFO = "MAINFORM_DIA_UPDCONF_DEFO";
	private static final String STRKEY_UPDCONF_AVAIL = "MAINFORM_DIA_UPDCONF_AVAIL";

	private static final String STRKEY_UPDPROG_T = "MAINFORM_DIA_UPDPROG_T";
	private static final String STRKEY_UPDPROG_A = "MAINFORM_DIA_UPDPROG_COMMON_A";
	private static final String STRKEY_UPDPROG_EXDONE = "MAINFORM_DIA_UPDPROG_EXDONE_B";
	private static final String STRKEY_UPDPROG_PROC = "MAINFORM_DIA_UPDPROG_PROC_B";
	private static final String STRKEY_UPDPROG_INIT = "MAINFORM_DIA_UPDPROG_INIT_B";
	private static final String STRKEY_UPDPROG_OKAY_M = "MAINFORM_DIA_UPDPROG_OKAY_M";
	private static final String STRKEY_UPDPROG_OKAY_T = "MAINFORM_DIA_UPDPROG_OKAY_T";
	private static final String STRKEY_UPDPROG_FAIL_M = "MAINFORM_DIA_UPDPROG_FAIL_M";
	private static final String STRKEY_UPDPROG_FAIL_T = "MAINFORM_DIA_UPDPROG_FAIL_T";
	
	/*----- Instance Variables -----*/
	
	private ZeqerCoreInterface core;
	
	private boolean updateAvailable = false;
	private JMenuItem mntmUpdate = null;
	
	private ComponentGroup globalEnable;
	
	private ZeqerPanelRoms pnlRoms;
	private ZeqerPanelAblds pnlAbld;
	private ZeqerPanelSeqs pnlSeq;
	private ZeqerPanelBanks pnlFont;
	private ZeqerPanelInstruments pnlInst;
	private ZeqerPanelSamples pnlSmpl;
	
	/*----- Init -----*/
	
	public ZeqerManagerForm(ZeqerCoreInterface core_link){
		core = core_link;
		globalEnable = new ComponentGroup();
		initGUI();
		checkForUpdate();
	}
	
	private void initGUI(){
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		setPreferredSize(new Dimension(DEFO_WIDTH, DEFO_HEIGHT));
		
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
		globalEnable.addComponent("tabbedPane", tabbedPane);
		
		pnlRoms = new ZeqerPanelRoms(core, this);
		tabbedPane.addTab(getString(STRKEY_TAB_ROMS), null, pnlRoms, null);
		pnlRoms.setImportCallback(new VoidCallbackMethod(){
			public void doMethod() {onRomImport();}
		});
		
		pnlAbld = new ZeqerPanelAblds(this, core, false);
		tabbedPane.addTab("Audio Builds", null, pnlAbld, null);
		
		pnlSeq = new ZeqerPanelSeqs(this, core, false);
		tabbedPane.addTab("Sequences", null, pnlSeq, null);
		
		pnlFont = new ZeqerPanelBanks(this, core, false);
		tabbedPane.addTab("Soundfonts", null, pnlFont, null);
		
		pnlInst = new ZeqerPanelInstruments(this, core, true);
		tabbedPane.addTab(getString(STRKEY_TAB_INST), null, pnlInst, null);
		
		pnlSmpl = new ZeqerPanelSamples(this, core, true);
		tabbedPane.addTab(getString(STRKEY_TAB_WAV), null, pnlSmpl, null);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		globalEnable.addComponent("menuBar", menuBar);
		
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
		
		JMenu mnSys = new JMenu(getString(STRKEY_MENU_SYS));
		menuBar.add(mnSys);
		
		mntmUpdate = new JMenuItem(getString(STRKEY_MITEM_S_UPD1));
		mntmUpdate.setFont(new Font("Segoe UI", Font.BOLD, 12));
		mnSys.add(mntmUpdate);
		mntmUpdate.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				mntmSysUpdateCallback();
			}});
	}
	
	public void render(){
		pack();
		setLocation(GUITools.getScreenCenteringCoordinates(this));
		setVisible(true);
	}
	
	private void checkForUpdate(){
		if(core != null){
			updateAvailable = core.updateAvailable();
		}
		updateUpdateMenuItemAppearance();
	}
	
	/*----- Getters -----*/
	
	protected String getString(String key){
		if(core == null) return key;
		return core.getString(key);
	}
	
	/*----- Setters -----*/
	
	/*----- Draw -----*/
	
	public void disableAll(){
		//System.err.println("ZeqerManagerForm.disableAll || Called!");
		globalEnable.setEnabling(false);
		
		//Child panels...
		if(pnlRoms != null) pnlRoms.disableAll();
		if(pnlAbld != null) pnlAbld.disableAll();
		if(pnlSeq != null) pnlSeq.disableAll();
		if(pnlFont != null) pnlFont.disableAll();
		if(pnlInst != null) pnlInst.disableAll();
		if(pnlSmpl != null) pnlSmpl.disableAll();
		
		globalEnable.repaint();
	}
	
	public void reenable(){
		//System.err.println("ZeqerManagerForm.reenable || Called!");
		globalEnable.setEnabling(true);
		
		//Child panels...
		if(pnlRoms != null) pnlRoms.reenable();
		if(pnlAbld != null) pnlAbld.reenable();
		if(pnlSeq != null) pnlSeq.reenable();
		if(pnlFont != null) pnlFont.reenable();
		if(pnlInst != null) pnlInst.reenable();
		if(pnlSmpl != null) pnlSmpl.reenable();
		
		globalEnable.repaint();
	}
	
	public void setWait(){
		//System.err.println("ZeqerManagerForm.setWait || Called!");
		disableAll();
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	public void unsetWait(){
		//System.err.println("ZeqerManagerForm.unsetWait || Called!");
		reenable();
		setCursor(null);
	}
	
	private void updateUpdateMenuItemAppearance(){
		if(updateAvailable){
			mntmUpdate.setFont(new Font("Segoe UI", Font.BOLD, 12));
			mntmUpdate.setForeground(Color.BLUE);
			
			String txt = getString(STRKEY_MITEM_S_UPD2);
			txt = txt.replace("%s", ZeqerCore.CURRENT_VERSION);
			mntmUpdate.setText(txt);
		}
		else{
			mntmUpdate.setForeground(Color.BLACK);
			mntmUpdate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
			mntmUpdate.setText(getString(STRKEY_MITEM_S_UPD1));
		}
		
		mntmUpdate.repaint();
	}
	
	/*----- Callbacks -----*/
	
	public void onRomImport(){
		pnlSmpl.refreshSamplePool();
		pnlInst.refreshPresetPool();
		pnlFont.updateBankPool();
		pnlSeq.updateSeqPool();
		pnlAbld.updatePool();
	}
	
	private void dummyCallback(){
		JOptionPane.showMessageDialog(this, "Sorry, this component doesn't work yet!", 
				"Notice", JOptionPane.INFORMATION_MESSAGE);
	}
	
	private void mntmFileSaveCallback(){
		//TODO
		dummyCallback();
	}
	
	private void mntmFileUninstallCallback(){
		//TODO
		dummyCallback();
	}
	
	private void mntmFileExitCallback(){
		//TODO
		dummyCallback();
	}
	
	private void mntmSysUpdateCallback(){
		//Confirm
		//Can update if no version diff to reset sys tables
		String msg = null;
		if(updateAvailable){
			String myver = core.getSetting(ZeqerCore.IKEY_VERSION);
			if(myver == null) myver = "0.0.0";
			
			msg = getString(STRKEY_UPDCONF_AVAIL);
			msg = msg.replace("%s1", myver);
			msg = msg.replace("%s2", ZeqerCore.CURRENT_VERSION);
			
		}
		else{
			msg = getString(STRKEY_UPDCONF_DEFO);
		}
		
		int res = JOptionPane.showConfirmDialog(this, msg, getString(STRKEY_UPDCONF_T), 
				JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		
		if(res != JOptionPane.YES_OPTION) return;
		
		IndefProgressDialog dialog = new IndefProgressDialog(this, getString(STRKEY_UPDPROG_T));
		ZeqerInstallListener listener = new ZeqerInstallListener(){

			public void onWriteIniStart() {
				dialog.setPrimaryString(getString(STRKEY_IPROG_INI_A));
				dialog.setSecondaryString(getString(STRKEY_IPROG_INI_B));
			}

			public void onTableReadStart() {
				dialog.setPrimaryString(getString(STRKEY_IPROG_FTREAD_A));
				dialog.setSecondaryString(getString(STRKEY_IPROG_FTREAD_B));
			}

			public void onFileExtractStart(String filepath) {
				dialog.setPrimaryString(getString(STRKEY_IPROG_FEXT_A));
				
				String iprog_fext_b = getString(STRKEY_IPROG_FEXT_B);
				dialog.setSecondaryString(iprog_fext_b.replace("%s", filepath));
			}

			public void onFileExtractDone(String filepath) {
				dialog.setPrimaryString(getString(STRKEY_UPDPROG_A));
				dialog.setSecondaryString(getString(STRKEY_UPDPROG_EXDONE));
			}

			public void onUpdateFileProcessStart() {
				dialog.setPrimaryString(getString(STRKEY_UPDPROG_A));
				dialog.setSecondaryString(getString(STRKEY_UPDPROG_PROC));
			}
			
			public void init(){
				dialog.setPrimaryString(getString(STRKEY_UPDPROG_A));
				dialog.setSecondaryString(getString(STRKEY_UPDPROG_INIT));
			}
			
			public void close(){
				dialog.closeMe();
			}
			
		};
		
		listener.init();
		JFrame me = this;
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>(){
			protected Void doInBackground() throws Exception{
				try{
					if(core.performUpdate(listener)){
						JOptionPane.showMessageDialog(me, getString(STRKEY_UPDPROG_OKAY_M), 
								getString(STRKEY_UPDPROG_OKAY_T), JOptionPane.INFORMATION_MESSAGE);
					}
					else{
						JOptionPane.showMessageDialog(me, getString(STRKEY_UPDPROG_FAIL_M), 
								getString(STRKEY_UPDPROG_FAIL_T), JOptionPane.ERROR_MESSAGE);
					}
				}
				catch(Exception ex){
					ex.printStackTrace();
					JOptionPane.showMessageDialog(me, getString(STRKEY_UPDPROG_FAIL_M), 
							getString(STRKEY_UPDPROG_FAIL_T), JOptionPane.ERROR_MESSAGE);
					
				}
				return null;
			}
			
			public void done(){
				listener.close();
				pnlInst.refreshPresetPool();
				pnlSmpl.refreshSamplePool();
			}
		};
		
		task.execute();
		dialog.render();
	}
	
}
