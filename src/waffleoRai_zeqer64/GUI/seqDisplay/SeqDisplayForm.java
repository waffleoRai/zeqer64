package waffleoRai_zeqer64.GUI.seqDisplay;

import java.awt.Dimension;

import javax.swing.JFrame;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import java.awt.GridBagConstraints;
import javax.swing.JPanel;
import java.awt.Insets;
import javax.swing.JScrollPane;

public class SeqDisplayForm extends JFrame{

	/*----- Constants -----*/
	
	private static final long serialVersionUID = -5924941739239218627L;
	
	public static final int MIN_WIDTH = 640;
	public static final int MIN_HEIGHT = 480;

	/*----- Instance Variables -----*/
	
	private JPanel pnlTracks;
	
	/*----- Initialization -----*/
	
	public SeqDisplayForm(){
		//TODO
		init();
		
	}
	
	private void init(){
		//TODO
		setTitle("(test)");
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{4.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 3.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel pnlInfo = new JPanel();
		GridBagConstraints gbc_pnlInfo = new GridBagConstraints();
		gbc_pnlInfo.insets = new Insets(0, 0, 5, 5);
		gbc_pnlInfo.fill = GridBagConstraints.BOTH;
		gbc_pnlInfo.gridx = 0;
		gbc_pnlInfo.gridy = 0;
		getContentPane().add(pnlInfo, gbc_pnlInfo);
		
		JScrollPane spCmdTable = new JScrollPane();
		GridBagConstraints gbc_spCmdTable = new GridBagConstraints();
		gbc_spCmdTable.gridheight = 2;
		gbc_spCmdTable.fill = GridBagConstraints.BOTH;
		gbc_spCmdTable.gridx = 1;
		gbc_spCmdTable.gridy = 0;
		getContentPane().add(spCmdTable, gbc_spCmdTable);
		
		JScrollPane spTracks = new JScrollPane();
		GridBagConstraints gbc_spTracks = new GridBagConstraints();
		gbc_spTracks.insets = new Insets(0, 0, 0, 5);
		gbc_spTracks.fill = GridBagConstraints.BOTH;
		gbc_spTracks.gridx = 0;
		gbc_spTracks.gridy = 1;
		getContentPane().add(spTracks, gbc_spTracks);
		
		pnlTracks = new JPanel();
		spTracks.setViewportView(pnlTracks);
		GridBagLayout gbl_pnlTracks = new GridBagLayout();
		gbl_pnlTracks.columnWidths = new int[]{0};
		gbl_pnlTracks.rowHeights = new int[]{0};
		gbl_pnlTracks.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_pnlTracks.rowWeights = new double[]{Double.MIN_VALUE};
		pnlTracks.setLayout(gbl_pnlTracks);
	}
	
	public void render(){
		this.pack();
		this.setVisible(true);
	}
	
	/*----- Getters -----*/
	
	/*----- Setters -----*/
	
	/*----- Drawing -----*/
	
}
