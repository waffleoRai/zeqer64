package waffleoRai_zeqer64.GUI;

import javax.swing.JFrame;
import java.awt.GridBagLayout;
import javax.swing.JTabbedPane;
import java.awt.GridBagConstraints;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

public class ZeqerManagerForm extends JFrame{

	private static final long serialVersionUID = -2977885456814658055L;

	public ZeqerManagerForm(){
		initGUI();
	}
	
	private void initGUI(){
		setTitle("Zeqer64");
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
		
		JPanel pnlRoms = new JPanel();
		tabbedPane.addTab("ROMs", null, pnlRoms, null);
		
		JPanel pnlAbld = new JPanel();
		tabbedPane.addTab("Audio Builds", null, pnlAbld, null);
		
		JPanel pnlSeq = new JPanel();
		tabbedPane.addTab("Sequences", null, pnlSeq, null);
		
		JPanel pnlFont = new JPanel();
		tabbedPane.addTab("Soundfonts", null, pnlFont, null);
		
		JPanel pnlInst = new JPanel();
		tabbedPane.addTab("Instruments", null, pnlInst, null);
		
		JPanel pnlSmpl = new JPanel();
		tabbedPane.addTab("Samples", null, pnlSmpl, null);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
	}
	
}
