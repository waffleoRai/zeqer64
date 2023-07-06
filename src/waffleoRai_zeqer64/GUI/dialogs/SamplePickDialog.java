package waffleoRai_zeqer64.GUI.dialogs;

import javax.swing.JDialog;

import java.awt.GridBagLayout;
import javax.swing.JPanel;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.border.EtchedBorder;

import waffleoRai_GUITools.GUITools;
import waffleoRai_zeqer64.GUI.ZeqerPanelSamples;
import waffleoRai_zeqer64.filefmt.ZeqerWaveTable.WaveTableEntry;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;

public class SamplePickDialog extends JDialog{

	private static final long serialVersionUID = -7904299455161598831L;
	
	private Frame parent;
	private ZeqerCoreInterface core;
	
	private ZeqerPanelSamples pnlSamples;
	
	private WaveTableEntry selection = null;
	private boolean exitOkay = false;
	
	public SamplePickDialog(Frame parent_frame, ZeqerCoreInterface core_iface){
		super(parent_frame, true);
		parent = parent_frame;
		core = core_iface;
		initGUI();
	}
	
	private void initGUI(){
		setTitle("Sample Selection");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		pnlSamples = new ZeqerPanelSamples(parent, core, false);
		pnlSamples.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlSamples = new GridBagConstraints();
		gbc_pnlSamples.insets = new Insets(0, 0, 5, 0);
		gbc_pnlSamples.fill = GridBagConstraints.BOTH;
		gbc_pnlSamples.gridx = 0;
		gbc_pnlSamples.gridy = 0;
		getContentPane().add(pnlSamples, gbc_pnlSamples);
		
		JPanel pnlButtons = new JPanel();
		GridBagConstraints gbc_pnlButtons = new GridBagConstraints();
		gbc_pnlButtons.insets = new Insets(5, 5, 5, 5);
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
		gbc_btnOkay.insets = new Insets(0, 0, 0, 5);
		gbc_btnOkay.gridx = 1;
		gbc_btnOkay.gridy = 0;
		pnlButtons.add(btnOkay, gbc_btnOkay);
		btnOkay.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnOkayCallback();
			}
		});
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 0;
		pnlButtons.add(btnCancel, gbc_btnCancel);
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnCancelCallback();
			}
		});
	}
	
	public boolean getExitSelection(){return exitOkay;}
	public WaveTableEntry getSelectedSample(){return selection;}
	
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
	
	private void btnOkayCallback(){
		exitOkay = true;
		selection = pnlSamples.getSelectedSample();
		setVisible(false);
		dispose();
	}
	
	private void btnCancelCallback(){
		exitOkay = false;
		selection = null;
		setVisible(false);
		dispose();
	}

}
