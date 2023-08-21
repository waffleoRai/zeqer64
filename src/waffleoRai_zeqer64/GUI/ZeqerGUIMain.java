package waffleoRai_zeqer64.GUI;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import waffleoRai_Utils.VoidCallbackMethod;
import waffleoRai_zeqer64.ZeqerCore;
import waffleoRai_zeqer64.ZeqerInstaller.ZeqerInstallListener;
import waffleoRai_zeqer64.ZeqerStringManager;
import waffleoRai_zeqer64.GUI.dialogs.progress.IndefProgressDialog;

public class ZeqerGUIMain {
	
	public static final String DEFAULT_LANGUAGE = "eng";
	
	private static final String STRKEY_IPROG_TITLE = "INSTALLPROG_T";
	private static final String STRKEY_IPROG_DEFO_A = "INSTALLPROG_DEFO_A";
	private static final String STRKEY_IPROG_DEFO_B = "INSTALLPROG_DEFO_B";
	private static final String STRKEY_IPROG_INI_A = "INSTALLPROG_INISTART_A";
	private static final String STRKEY_IPROG_INI_B = "INSTALLPROG_INISTART_B";
	private static final String STRKEY_IPROG_FTREAD_A = "INSTALLPROG_FTREAD_A";
	private static final String STRKEY_IPROG_FTREAD_B = "INSTALLPROG_FTREAD_B";
	private static final String STRKEY_IPROG_FEXT_A = "INSTALLPROG_FEXTSTART_A";
	private static final String STRKEY_IPROG_FEXT_B = "INSTALLPROG_FEXTSTART_B";
	
	private static final String STRKEY_IDIA_NCDIR_T = "INSTALLDIALOG_NOCREATEDIR_T";
	private static final String STRKEY_IDIA_NCDIR_M = "INSTALLDIALOG_NOCREATEDIR_M";
	private static final String STRKEY_IDIA_CONFFC_T = "INSTALLDIALOG_CONFIRMFC_T";
	private static final String STRKEY_IDIA_CONFFC_M = "INSTALLDIALOG_CONFIRMFC_M";
	private static final String STRKEY_IDIA_FAIL1_T = "INSTALLDIALOG_FAIL_A_T";
	private static final String STRKEY_IDIA_FAIL1_M = "INSTALLDIALOG_FAIL_A_M";
	private static final String STRKEY_IDIA_FAIL2_T = "INSTALLDIALOG_FAIL_B_T";
	private static final String STRKEY_IDIA_FAIL2_M = "INSTALLDIALOG_FAIL_B_M";
	private static final String STRKEY_IDIA_OKAY_T = "INSTALLDIALOG_OKAY_T";
	private static final String STRKEY_IDIA_OKAY_M = "INSTALLDIALOG_OKAY_M";
	
	private static class InstallListener implements ZeqerInstallListener{

		private IndefProgressDialog dialog;
		private ZeqerStringManager strMng;
		
		private String iprog_fext_b = null;
		
		public InstallListener(IndefProgressDialog d, ZeqerStringManager strm){
			dialog = d;
			strMng = strm;
			iprog_fext_b = strMng.getString(STRKEY_IPROG_FEXT_B);
		}
		
		public void onWriteIniStart() {
			dialog.setPrimaryString(strMng.getString(STRKEY_IPROG_INI_A));
			dialog.setSecondaryString(strMng.getString(STRKEY_IPROG_INI_B));
		}

		public void onTableReadStart() {
			dialog.setPrimaryString(strMng.getString(STRKEY_IPROG_FTREAD_A));
			dialog.setSecondaryString(strMng.getString(STRKEY_IPROG_FTREAD_B));
		}

		public void onFileExtractStart(String filepath) {
			dialog.setPrimaryString(strMng.getString(STRKEY_IPROG_FEXT_A));
			dialog.setSecondaryString(iprog_fext_b.replace("%s", filepath));
		}

		public void onFileExtractDone(String filepath) {}
		
		public void onUpdateFileProcessStart(){}
		public void init(){}
		public void close(){}
	}
	
	private static void installBtnCallback(JFrame parent, CoreGUIInterface core){
		
		String inifiledir = ZeqerCore.getDefaultAppDataPath();
		try{
			Files.createDirectories(Paths.get(inifiledir));
		}
		catch(Exception ex){
			ex.printStackTrace();
			JOptionPane.showMessageDialog(parent, core.getString(STRKEY_IDIA_NCDIR_M), 
					core.getString(STRKEY_IDIA_NCDIR_T), JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		String installdir = null;
		while(installdir == null){
			JFileChooser fc = new JFileChooser(inifiledir);
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			int select = fc.showSaveDialog(parent);
			
			if(select != JFileChooser.APPROVE_OPTION) return;
			installdir = fc.getSelectedFile().getAbsolutePath();
			
			select = JOptionPane.showConfirmDialog(parent, 
					core.getString(STRKEY_IDIA_CONFFC_M).replace("%s", installdir), 
					core.getString(STRKEY_IDIA_CONFFC_T), 
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			
			switch(select){
			case JOptionPane.YES_OPTION: break;
			case JOptionPane.NO_OPTION: installdir = null; break;
			case JOptionPane.CANCEL_OPTION: return;
			}
		}
		
		final String install_dir_safe = installdir;
		
		IndefProgressDialog dialog = new IndefProgressDialog(parent, core.getString(STRKEY_IPROG_TITLE));
		InstallListener listener = new InstallListener(dialog, core.getStringManager());
		dialog.setPrimaryString(core.getString(STRKEY_IPROG_DEFO_A));
		dialog.setSecondaryString(core.getString(STRKEY_IPROG_DEFO_B));
		
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>(){

			protected Void doInBackground() throws Exception{
				try{
					ZeqerCore zcore = core.createCoreForPath(install_dir_safe);
					if(!zcore.installTo(install_dir_safe, listener)){
						JOptionPane.showMessageDialog(parent, 
								core.getString(STRKEY_IDIA_FAIL1_M), 
								core.getString(STRKEY_IDIA_FAIL1_T), JOptionPane.ERROR_MESSAGE);
					}
					else{
						JOptionPane.showMessageDialog(parent, 
								core.getString(STRKEY_IDIA_OKAY_M), 
								core.getString(STRKEY_IDIA_OKAY_T), 
								JOptionPane.INFORMATION_MESSAGE);
					}
					zcore.setIniValue(ZeqerCore.IKEY_PREFLAN, core.getLoadedLanguageCode());
					
					core.rebootFromInstalledPath();
				}
				catch(Exception x){
					x.printStackTrace();
					JOptionPane.showMessageDialog(parent, 
							core.getString(STRKEY_IDIA_FAIL2_M), 
							core.getString(STRKEY_IDIA_FAIL2_T), JOptionPane.ERROR_MESSAGE);
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
	
	public static void launchInstaller(String lanCode){
		//Boot manager
		CoreGUIInterface core = new CoreGUIInterface(null);
		try{
			core.openStringManagerInstallMode(lanCode);
		} catch(IOException ex){
			System.err.println("ZeqerGUIMain.launchInstaller || I/O Error loading GUI strings.");
			ex.printStackTrace();
			System.exit(1);
		}
		
		InstallPromptFrame iform = new InstallPromptFrame(core);
		iform.addWindowListener(new WindowAdapter(){
    		public void windowClosing(WindowEvent e){
    			//If closed by user...
    			iform.dispose();
    			//Installation successful? If so, go to launchProgram.
    			//If not, just exit.
    			if(core.isOkayForMainProgram()){
    				launchProgram(lanCode, core);
    			}
    			else System.exit(1);
			}});
		
		//Set Callbacks
		iform.setInstallButtonCallback(new VoidCallbackMethod(){
			public void doMethod() {
				installBtnCallback(iform, core);
			}});
		
		iform.render();
	}
	
	public static void launchProgram(String lanCode, CoreGUIInterface core){
		//TODO
		//If core is null, boot new one.
		//The ability to pass it is for if this is called via launchInstaller
		if(core == null){
			try {
				core = new CoreGUIInterface();
				core.openStringManager(lanCode);
			} 
			catch (IOException ex) {
				System.err.println("ERROR: Core could not boot!");
				ex.printStackTrace();
				System.exit(1);
			}
		}
		
		final CoreGUIInterface core_safe = core;
		ZeqerManagerForm mainform = new ZeqerManagerForm(core_safe);
		mainform.addWindowListener(new WindowAdapter(){
    		public void windowClosing(WindowEvent e){
    			try{
    				core_safe.shutdown();
    			}
    			catch(Exception ex){
    				System.err.println("WARNING: Core did not shut down properly!");
    				ex.printStackTrace();
    				System.exit(1);
    			}
    			mainform.dispose();
    			System.exit(0);
			}});
		
		mainform.render();
	}
	
	public static Map<String, String> parseCommandLineArgs(String[] args){
		Map<String, String> map = new HashMap<String, String>();
		if(args == null) return map;
		
		String key = null;
		String val = null;
		for(int i = 0; i < args.length; i++){
			if(args[i].startsWith("--")){
				//Key
				if(key != null) map.put(key, "true");
				key = args[i];
			}
			else{
				//Value
				if(key == null){
					System.err.println("ZeqerGUIMain.parseCommandLineArgs || Value \"" + args[i] + "\" has no key. Ignoring....");
				}
				else{
					val = args[i];
					map.put(key, val);
					
					key = null;
					val = null;
				}
			}
		}
		return map;
	}

	public static void main(String[] args) {
		//Can take optional command line arguments
		
		Map<String, String> cmdargs = parseCommandLineArgs(args);
		String lanCode = cmdargs.get("language");
		
		//Check for installation
		boolean installed = ZeqerCore.userHasInstallation();
		
		//Let's get the new thread going so we don't have to deal with passing
		//	variables around.
		SwingUtilities.invokeLater(new Runnable(){
            public void run(){
            	if(installed) {
            		launchProgram(lanCode, null);
            	}
            	else {
            		if(lanCode == null){
            			launchInstaller(DEFAULT_LANGUAGE);
            		}
            		else launchInstaller(lanCode);
            	}
            }
        });
		
	}

}
