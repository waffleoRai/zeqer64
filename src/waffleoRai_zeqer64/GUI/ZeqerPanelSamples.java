package waffleoRai_zeqer64.GUI;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JList;

public class ZeqerPanelSamples extends JPanel{

	private static final long serialVersionUID = 3054393851326234350L;

	public ZeqerPanelSamples(){
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JPanel pnlFiltSearch = new JPanel();
		GridBagConstraints gbc_pnlFiltSearch = new GridBagConstraints();
		gbc_pnlFiltSearch.insets = new Insets(0, 0, 5, 5);
		gbc_pnlFiltSearch.fill = GridBagConstraints.BOTH;
		gbc_pnlFiltSearch.gridx = 0;
		gbc_pnlFiltSearch.gridy = 0;
		add(pnlFiltSearch, gbc_pnlFiltSearch);
		
		JPanel pnlFiltTags = new JPanel();
		GridBagConstraints gbc_pnlFiltTags = new GridBagConstraints();
		gbc_pnlFiltTags.insets = new Insets(0, 0, 5, 5);
		gbc_pnlFiltTags.fill = GridBagConstraints.BOTH;
		gbc_pnlFiltTags.gridx = 0;
		gbc_pnlFiltTags.gridy = 1;
		add(pnlFiltTags, gbc_pnlFiltTags);
		
		JPanel pnlFiltFlags1 = new JPanel();
		GridBagConstraints gbc_pnlFiltFlags1 = new GridBagConstraints();
		gbc_pnlFiltFlags1.insets = new Insets(0, 0, 5, 5);
		gbc_pnlFiltFlags1.fill = GridBagConstraints.BOTH;
		gbc_pnlFiltFlags1.gridx = 0;
		gbc_pnlFiltFlags1.gridy = 2;
		add(pnlFiltFlags1, gbc_pnlFiltFlags1);
		
		JPanel pnlFiltFlags2 = new JPanel();
		GridBagConstraints gbc_pnlFiltFlags2 = new GridBagConstraints();
		gbc_pnlFiltFlags2.insets = new Insets(0, 0, 0, 5);
		gbc_pnlFiltFlags2.fill = GridBagConstraints.BOTH;
		gbc_pnlFiltFlags2.gridx = 0;
		gbc_pnlFiltFlags2.gridy = 3;
		add(pnlFiltFlags2, gbc_pnlFiltFlags2);
		
		JPanel pnlList = new JPanel();
		GridBagConstraints gbc_pnlList = new GridBagConstraints();
		gbc_pnlList.gridheight = 4;
		gbc_pnlList.fill = GridBagConstraints.BOTH;
		gbc_pnlList.gridx = 1;
		gbc_pnlList.gridy = 0;
		add(pnlList, gbc_pnlList);
		GridBagLayout gbl_pnlList = new GridBagLayout();
		gbl_pnlList.columnWidths = new int[]{0, 0};
		gbl_pnlList.rowHeights = new int[]{0, 0, 0, 0};
		gbl_pnlList.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlList.rowWeights = new double[]{1.0, 1.0, 1.0, Double.MIN_VALUE};
		pnlList.setLayout(gbl_pnlList);
		
		JScrollPane spList = new JScrollPane();
		GridBagConstraints gbc_spList = new GridBagConstraints();
		gbc_spList.insets = new Insets(0, 0, 5, 0);
		gbc_spList.fill = GridBagConstraints.BOTH;
		gbc_spList.gridx = 0;
		gbc_spList.gridy = 0;
		pnlList.add(spList, gbc_spList);
		
		JList list = new JList();
		spList.setViewportView(list);
		
		JScrollPane spInfo = new JScrollPane();
		GridBagConstraints gbc_spInfo = new GridBagConstraints();
		gbc_spInfo.insets = new Insets(0, 0, 5, 0);
		gbc_spInfo.fill = GridBagConstraints.BOTH;
		gbc_spInfo.gridx = 0;
		gbc_spInfo.gridy = 1;
		pnlList.add(spInfo, gbc_spInfo);
		
		JTextPane txtInfo = new JTextPane();
		spInfo.setViewportView(txtInfo);
		
		JPanel pnlCtrl = new JPanel();
		GridBagConstraints gbc_pnlCtrl = new GridBagConstraints();
		gbc_pnlCtrl.fill = GridBagConstraints.BOTH;
		gbc_pnlCtrl.gridx = 0;
		gbc_pnlCtrl.gridy = 2;
		pnlList.add(pnlCtrl, gbc_pnlCtrl);
		GridBagLayout gbl_pnlCtrl = new GridBagLayout();
		gbl_pnlCtrl.columnWidths = new int[]{0};
		gbl_pnlCtrl.rowHeights = new int[]{0};
		gbl_pnlCtrl.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_pnlCtrl.rowWeights = new double[]{Double.MIN_VALUE};
		pnlCtrl.setLayout(gbl_pnlCtrl);
		//TODO
	}
	
	private void initGUI(){
		//TODO
	}
	
}
