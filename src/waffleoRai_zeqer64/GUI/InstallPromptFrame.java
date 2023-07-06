package waffleoRai_zeqer64.GUI;

import javax.swing.JFrame;

import java.awt.GridBagLayout;
import javax.swing.JButton;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import waffleoRai_GUITools.GUITools;
import waffleoRai_Utils.VoidCallbackMethod;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;

import java.awt.Dimension;
import java.awt.Font;

public class InstallPromptFrame extends JFrame{

	/*----- Constants -----*/
	
	private static final long serialVersionUID = -353098285426792574L;
	
	public static final int DEFO_WIDTH = 400;
	public static final int DEFO_HEIGHT = 170;
	
	private static final String STR_FORM_TITLE = "INSTALLPROMPT_TITLE";
	private static final String STR_MAIN_MESSAGE_A = "INSTALLPROMPT_MSG1";
	private static final String STR_MAIN_MESSAGE_B = "INSTALLPROMPT_MSG2";
	private static final String STR_BTN_INSTALL = "INSTALLPROMPT_BTN1";
	private static final String STR_BTN_EXIT = "INSTALLPROMPT_BTN2";

	/*----- Instance Variables -----*/
	
	private ZeqerCoreInterface core;
	private boolean exitSelection = false;
	
	private VoidCallbackMethod btnInstallListener;
	private VoidCallbackMethod btnExitListener;
	
	/*----- Init -----*/
	
	public InstallPromptFrame(ZeqerCoreInterface core_iface){
		core = core_iface;
		initGUI();
	}
	
	private void initGUI(){
		setResizable(false);
		setMinimumSize(new Dimension(DEFO_WIDTH, DEFO_HEIGHT));
		setPreferredSize(new Dimension(DEFO_WIDTH, DEFO_HEIGHT));
		
		setTitle(getString_internal(STR_FORM_TITLE));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JLabel lblMessage = new JLabel(getString_internal(STR_MAIN_MESSAGE_A));
		lblMessage.setFont(new Font("Tahoma", Font.PLAIN, 12));
		GridBagConstraints gbc_lblMessage = new GridBagConstraints();
		gbc_lblMessage.anchor = GridBagConstraints.SOUTH;
		gbc_lblMessage.insets = new Insets(0, 0, 5, 0);
		gbc_lblMessage.gridx = 0;
		gbc_lblMessage.gridy = 0;
		getContentPane().add(lblMessage, gbc_lblMessage);
		
		JLabel lblWouldYouLike = new JLabel(getString_internal(STR_MAIN_MESSAGE_B));
		lblWouldYouLike.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_lblWouldYouLike = new GridBagConstraints();
		gbc_lblWouldYouLike.anchor = GridBagConstraints.NORTH;
		gbc_lblWouldYouLike.insets = new Insets(0, 0, 5, 0);
		gbc_lblWouldYouLike.gridx = 0;
		gbc_lblWouldYouLike.gridy = 1;
		getContentPane().add(lblWouldYouLike, gbc_lblWouldYouLike);
		
		JPanel pnlButtons = new JPanel();
		GridBagConstraints gbc_pnlButtons = new GridBagConstraints();
		gbc_pnlButtons.fill = GridBagConstraints.BOTH;
		gbc_pnlButtons.gridx = 0;
		gbc_pnlButtons.gridy = 2;
		getContentPane().add(pnlButtons, gbc_pnlButtons);
		GridBagLayout gbl_pnlButtons = new GridBagLayout();
		gbl_pnlButtons.columnWidths = new int[]{0, 100, 100, 0, 0};
		gbl_pnlButtons.rowHeights = new int[]{35, 0};
		gbl_pnlButtons.columnWeights = new double[]{1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlButtons.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlButtons.setLayout(gbl_pnlButtons);
		
		JButton btnInstall = new JButton(getString_internal(STR_BTN_INSTALL));
		GridBagConstraints gbc_btnInstall = new GridBagConstraints();
		gbc_btnInstall.fill = GridBagConstraints.BOTH;
		gbc_btnInstall.insets = new Insets(0, 0, 5, 10);
		gbc_btnInstall.gridx = 1;
		gbc_btnInstall.gridy = 0;
		pnlButtons.add(btnInstall, gbc_btnInstall);
		btnInstall.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {btnInstallCallback();}});
		
		JButton btnExit = new JButton(getString_internal(STR_BTN_EXIT));
		GridBagConstraints gbc_btnExit = new GridBagConstraints();
		gbc_btnExit.fill = GridBagConstraints.BOTH;
		gbc_btnExit.insets = new Insets(0, 10, 5, 5);
		gbc_btnExit.gridx = 2;
		gbc_btnExit.gridy = 0;
		pnlButtons.add(btnExit, gbc_btnExit);
		btnExit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {btnExitCallback();}});
		
		setLocation(GUITools.getScreenCenteringCoordinates(this));
	}
	
	public void render(){
		this.pack();
		this.setVisible(true);
	}

	/*----- Getters -----*/
	
	public boolean getExitSelection(){return exitSelection;}
	
	private String getString_internal(String key){
		if(core == null) return key;
		return core.getString(key);
	}
	
	/*----- Setters -----*/
	
	public void setInstallButtonCallback(VoidCallbackMethod func){btnInstallListener = func;}
	public void setExitButtonCallback(VoidCallbackMethod func){btnExitListener = func;}
	
	/*----- Callbacks -----*/
	
	public void closeMe(){
		setVisible(false);
		dispose();
	}
	
	private void btnInstallCallback(){
		exitSelection = true;
		if(btnInstallListener != null){
			btnInstallListener.doMethod();
		}
		closeMe();
	}
	
	private void btnExitCallback(){
		exitSelection = false;
		if(btnExitListener != null){
			btnExitListener.doMethod();
		}
		closeMe();
	}
	
}
