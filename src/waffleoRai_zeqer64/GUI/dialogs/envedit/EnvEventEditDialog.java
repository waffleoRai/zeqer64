package waffleoRai_zeqer64.GUI.dialogs.envedit;

import javax.swing.JDialog;
import java.awt.GridBagLayout;
import javax.swing.JPanel;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.border.EtchedBorder;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.GUITools;
import waffleoRai_Sound.nintendo.Z64Sound;

public class EnvEventEditDialog extends JDialog{
	
	private static final long serialVersionUID = -5441859097909491835L;
	
	public static final int DEFO_WIDTH = 470;
	public static final int DEFO_HEIGHT = 100;
	
	private static final int NUMPARSE_ERR_NONE = 0;
	private static final int NUMPARSE_ERR_EMPTY = 1;
	private static final int NUMPARSE_ERR_NOTNUMBER = 2;
	//private static final int NUMPARSE_ERR_OUTOFRANGE= 3;
	
	/*----- Inner Classes -----*/
	
	private static enum EnvCommand{
		
		DISABLE(0),
		HANG(-1),
		GOTO(-2),
		RESTART(-3),
		DELTA(1);
		
		private int cmdval;
		
		private EnvCommand(int n){cmdval = n;}
		public int getCommandValue(){return cmdval;}
		
	}
	
	/*----- Instance Variables -----*/
	
	private Frame parent;
	
	private short[] event = null;
	private int txtParseError = NUMPARSE_ERR_NONE;
	
	private JTextField txtTime;
	private JTextField txtLevel;
	private JComboBox<EnvCommand> cmbxCommand;
	private ComponentGroup globalEnable;
	
	/*----- Init -----*/
	
	public EnvEventEditDialog(Frame parent_frame){
		super(parent_frame, true);
		parent = parent_frame;
		globalEnable = new ComponentGroup();
		initGUI();
		populateCombobox();
	}
	
	private void initGUI(){
		setTitle("Envelope Event");
		setResizable(false);
		
		setMinimumSize(new Dimension(DEFO_WIDTH, DEFO_HEIGHT));
		setPreferredSize(new Dimension(DEFO_WIDTH, DEFO_HEIGHT));
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel pnlParams = new JPanel();
		pnlParams.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlParams = new GridBagConstraints();
		gbc_pnlParams.insets = new Insets(0, 0, 5, 0);
		gbc_pnlParams.fill = GridBagConstraints.BOTH;
		gbc_pnlParams.gridx = 0;
		gbc_pnlParams.gridy = 0;
		getContentPane().add(pnlParams, gbc_pnlParams);
		GridBagLayout gbl_pnlParams = new GridBagLayout();
		gbl_pnlParams.columnWidths = new int[]{0, 100, 0, 0, 75, 0, 75, 0, 0};
		gbl_pnlParams.rowHeights = new int[]{0, 0};
		gbl_pnlParams.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlParams.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlParams.setLayout(gbl_pnlParams);
		
		JLabel lblCommand = new JLabel("Command:");
		GridBagConstraints gbc_lblCommand = new GridBagConstraints();
		gbc_lblCommand.insets = new Insets(5, 10, 5, 5);
		gbc_lblCommand.anchor = GridBagConstraints.EAST;
		gbc_lblCommand.gridx = 0;
		gbc_lblCommand.gridy = 0;
		pnlParams.add(lblCommand, gbc_lblCommand);
		
		cmbxCommand = new JComboBox<EnvCommand>();
		GridBagConstraints gbc_cmbxCommand = new GridBagConstraints();
		gbc_cmbxCommand.insets = new Insets(5, 5, 0, 5);
		gbc_cmbxCommand.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxCommand.gridx = 1;
		gbc_cmbxCommand.gridy = 0;
		pnlParams.add(cmbxCommand, gbc_cmbxCommand);
		globalEnable.addComponent("cmbxCommand", cmbxCommand);
		cmbxCommand.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				cmbxSelectCallback();
			}
		});
		
		JLabel lblTime = new JLabel("Time:");
		GridBagConstraints gbc_lblTime = new GridBagConstraints();
		gbc_lblTime.insets = new Insets(5, 5, 0, 5);
		gbc_lblTime.anchor = GridBagConstraints.EAST;
		gbc_lblTime.gridx = 3;
		gbc_lblTime.gridy = 0;
		pnlParams.add(lblTime, gbc_lblTime);
		
		txtTime = new JTextField();
		GridBagConstraints gbc_txtTime = new GridBagConstraints();
		gbc_txtTime.insets = new Insets(5, 5, 0, 5);
		gbc_txtTime.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtTime.gridx = 4;
		gbc_txtTime.gridy = 0;
		pnlParams.add(txtTime, gbc_txtTime);
		txtTime.setColumns(10);
		globalEnable.addComponent("txtTime", txtTime);
		
		JLabel lblLvl = new JLabel("Level:");
		GridBagConstraints gbc_lblLvl = new GridBagConstraints();
		gbc_lblLvl.insets = new Insets(5, 5, 0, 5);
		gbc_lblLvl.gridx = 5;
		gbc_lblLvl.gridy = 0;
		pnlParams.add(lblLvl, gbc_lblLvl);
		
		txtLevel = new JTextField();
		GridBagConstraints gbc_txtLevel = new GridBagConstraints();
		gbc_txtLevel.insets = new Insets(5, 5, 5, 10);
		gbc_txtLevel.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtLevel.gridx = 6;
		gbc_txtLevel.gridy = 0;
		pnlParams.add(txtLevel, gbc_txtLevel);
		txtLevel.setColumns(10);
		globalEnable.addComponent("txtLevel", txtLevel);
		
		JLabel label = new JLabel("");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.EAST;
		gbc_label.gridx = 7;
		gbc_label.gridy = 0;
		pnlParams.add(label, gbc_label);
		
		JPanel pnlButtons = new JPanel();
		GridBagConstraints gbc_pnlButtons = new GridBagConstraints();
		gbc_pnlButtons.fill = GridBagConstraints.BOTH;
		gbc_pnlButtons.gridx = 0;
		gbc_pnlButtons.gridy = 1;
		getContentPane().add(pnlButtons, gbc_pnlButtons);
		GridBagLayout gbl_pnlButtons = new GridBagLayout();
		gbl_pnlButtons.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlButtons.rowHeights = new int[]{0, 0};
		gbl_pnlButtons.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlButtons.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlButtons.setLayout(gbl_pnlButtons);
		
		JButton btnOkay = new JButton("Okay");
		GridBagConstraints gbc_btnOkay = new GridBagConstraints();
		gbc_btnOkay.insets = new Insets(5, 5, 5, 5);
		gbc_btnOkay.gridx = 1;
		gbc_btnOkay.gridy = 0;
		pnlButtons.add(btnOkay, gbc_btnOkay);
		globalEnable.addComponent("btnOkay", btnOkay);
		btnOkay.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnOkayCallback();
			}
		});
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(5, 5, 5, 5);
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 0;
		pnlButtons.add(btnCancel, gbc_btnCancel);
		globalEnable.addComponent("btnCancel", btnCancel);
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnCancelCallback();
			}
		});
	}

	private void populateCombobox(){
		DefaultComboBoxModel<EnvCommand> model = new DefaultComboBoxModel<EnvCommand>();
		EnvCommand[] allcmd = EnvCommand.values();
		for(EnvCommand cmd : allcmd) model.addElement(cmd);
		cmbxCommand.setModel(model);
		cmbxCommand.setSelectedIndex(0);
		cmbxCommand.repaint();
	}
	
	/*----- Getters -----*/
	
	public boolean selectionOkay(){return event != null;}
	
	public short[] getEvent(){return event;}
	
	/*----- Setters -----*/
	
	/*----- GUI Management -----*/
	
	private int parseTextboxValue(JTextField textbox){
		//Can either be hex or dec
		txtParseError = NUMPARSE_ERR_NONE;
		String txt = textbox.getText();
		if(txt.isEmpty()) {
			txtParseError = NUMPARSE_ERR_EMPTY;
			return -1;
		}
		
		try{
			if(txt.startsWith("0x")){
				return Integer.parseInt(txt.substring(2), 16);
			}
			else return Integer.parseInt(txt);
		}
		catch(NumberFormatException ex){
			txtParseError = NUMPARSE_ERR_NOTNUMBER;
			return -1;
		}
	}
	
	private EnvCommand getSelectedCommand(){
		int idx = cmbxCommand.getSelectedIndex();
		if(idx >= 0){
			return cmbxCommand.getItemAt(idx);
		}
		else return null;
	}
	
	private void disableGlobal(){
		globalEnable.setEnabling(false);
		globalEnable.repaint();
	}
	
	private void enableGlobal(){
		globalEnable.setEnabling(true);
		if(getSelectedCommand().getCommandValue() <= 0){
			txtTime.setEnabled(false);
			txtLevel.setEnabled(false);
		}
		globalEnable.repaint();
	}
	
	public void setWait(){
		disableGlobal();
		super.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	public void unsetWait(){
		enableGlobal();
		super.setCursor(null);
	}
	
	public void closeMe(){
		this.setVisible(false);
		this.dispose();
	}
	
	public void showMe(Component c){
		if(c != null) setLocationRelativeTo(c);
		else{
			if(parent != null) setLocationRelativeTo(parent);
			else{
				setLocation(GUITools.getScreenCenteringCoordinates(this));
			}
		}
		
		pack();
		setVisible(true);
	}
	
	/*----- Callbacks -----*/

	private void btnOkayCallback(){
		setWait();
		event = new short[2];
		
		EnvCommand cmd = getSelectedCommand();
		if(cmd == EnvCommand.DELTA){
			
			int time = parseTextboxValue(txtTime);
			String errmsg = null;
			switch(txtParseError){
			case NUMPARSE_ERR_EMPTY:
				errmsg = "Time field is required for DELTA command!";
				break;
			case NUMPARSE_ERR_NOTNUMBER:
				errmsg = "Time field must be valid integer between (1 - 32767)";
				break;
			}
			if(time < 1 || time > 0x7fff){
				errmsg = "Time field must be valid integer between (1 - 32767)";
			}
			
			if(errmsg != null){
				event = null;
				JOptionPane.showMessageDialog(this, errmsg, "Invalid Parameters", JOptionPane.ERROR_MESSAGE);
				unsetWait();
				return;
			}
			
			int val = parseTextboxValue(txtLevel);
			errmsg = null;
			switch(txtParseError){
			case NUMPARSE_ERR_EMPTY:
				errmsg = "Level field is required for DELTA command!";
				break;
			case NUMPARSE_ERR_NOTNUMBER:
				errmsg = "Level field must be valid integer between (0 - 32767)";
				break;
			}
			if(val < 0 || val > 0x7fff){
				errmsg = "Level field must be valid integer between (0 - 32767)";
			}
			
			if(errmsg != null){
				event = null;
				JOptionPane.showMessageDialog(this, errmsg, "Invalid Parameters", JOptionPane.ERROR_MESSAGE);
				unsetWait();
				return;
			}
			
			event[0] = (short)time;
			event[1] = (short)val;
		}
		else if(cmd == EnvCommand.GOTO){
			//Right now, this just behaves the same as the others.
			event[0] = Z64Sound.ENVCMD__ADSR_GOTO;
			event[1] = -1;
		}
		else{
			event[0] = (short)cmd.getCommandValue();
			event[1] = 0;
		}
		
		unsetWait();
		closeMe();
	}
	
	private void btnCancelCallback(){
		event = null;
		closeMe();
	}
	
	private void cmbxSelectCallback(){
		enableGlobal(); //This adjusts the enabling of the textboxes
	}
	
}
