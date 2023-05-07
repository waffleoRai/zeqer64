package waffleoRai_zeqer64.GUI.sampleImport;

import java.awt.Frame;

import javax.swing.JDialog;

import waffleoRai_zeqer64.ZeqerCoreInterface;
import java.awt.GridBagLayout;
import javax.swing.JScrollPane;
import java.awt.GridBagConstraints;
import javax.swing.JPanel;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import waffleoRai_GUITools.ComponentGroup;

public class ImportSampleDialog extends JDialog{

	private static final long serialVersionUID = 1677509157623328685L;
	
	public static final int MIN_WIDTH = 0;
	public static final int MIN_HEIGHT = 0;

	/*----- Instance Variables -----*/
	
	private ComponentGroup globalEnable;
	
	private JList<ImportableSample> lstSamples;
	private JButton btnRemove;
	
	private boolean exitSelection = false;
	private Map<Integer, ImportableSample> allSamples;
	
	/*----- Init -----*/
	
	public ImportSampleDialog(Frame parent, ZeqerCoreInterface core_link){
		super(parent, true);
		allSamples = new HashMap<Integer, ImportableSample>();
		initGUI();
	}
	
	private void initGUI(){
		globalEnable = new ComponentGroup();
		
		setTitle("Sample Import");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 3.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel pnlSamples = new JPanel();
		pnlSamples.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlSamples = new GridBagConstraints();
		gbc_pnlSamples.insets = new Insets(0, 0, 5, 5);
		gbc_pnlSamples.fill = GridBagConstraints.BOTH;
		gbc_pnlSamples.gridx = 0;
		gbc_pnlSamples.gridy = 0;
		getContentPane().add(pnlSamples, gbc_pnlSamples);
		GridBagLayout gbl_pnlSamples = new GridBagLayout();
		gbl_pnlSamples.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlSamples.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_pnlSamples.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlSamples.rowWeights = new double[]{1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		pnlSamples.setLayout(gbl_pnlSamples);
		
		JScrollPane spList = new JScrollPane();
		spList.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spList = new GridBagConstraints();
		gbc_spList.gridwidth = 3;
		gbc_spList.insets = new Insets(5, 5, 5, 5);
		gbc_spList.fill = GridBagConstraints.BOTH;
		gbc_spList.gridx = 0;
		gbc_spList.gridy = 0;
		pnlSamples.add(spList, gbc_spList);
		globalEnable.addComponent("spList", spList);
		
		lstSamples = new JList<ImportableSample>();
		spList.setViewportView(lstSamples);
		globalEnable.addComponent("lstSamples", lstSamples);
		lstSamples.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e) {
				lstSelectCallback();
			}
		});
		
		JButton btnAddDir = new JButton("Scan directory...");
		GridBagConstraints gbc_btnAddDir = new GridBagConstraints();
		gbc_btnAddDir.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnAddDir.insets = new Insets(0, 5, 5, 5);
		gbc_btnAddDir.gridx = 0;
		gbc_btnAddDir.gridy = 1;
		pnlSamples.add(btnAddDir, gbc_btnAddDir);
		globalEnable.addComponent("btnAddDir", btnAddDir);
		btnAddDir.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnAddDirCallback();
			}});
		
		JButton btnAddWav = new JButton("Add sound file(s)...");
		GridBagConstraints gbc_btnAddWav = new GridBagConstraints();
		gbc_btnAddWav.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnAddWav.insets = new Insets(0, 5, 5, 5);
		gbc_btnAddWav.gridx = 0;
		gbc_btnAddWav.gridy = 2;
		pnlSamples.add(btnAddWav, gbc_btnAddWav);
		globalEnable.addComponent("btnAddWav", btnAddWav);
		btnAddWav.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnAddWavCallback();
			}});
		
		JButton btnAddSar = new JButton("Add from sound data archive...");
		GridBagConstraints gbc_btnAddSar = new GridBagConstraints();
		gbc_btnAddSar.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnAddSar.insets = new Insets(0, 5, 5, 5);
		gbc_btnAddSar.gridx = 0;
		gbc_btnAddSar.gridy = 3;
		pnlSamples.add(btnAddSar, gbc_btnAddSar);
		globalEnable.addComponent("btnAddSar", btnAddSar);
		btnAddSar.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnAddSarCallback();
			}});
		
		JButton btnAddWar = new JButton("Add from sample archive...");
		GridBagConstraints gbc_btnAddWar = new GridBagConstraints();
		gbc_btnAddWar.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnAddWar.insets = new Insets(0, 5, 5, 5);
		gbc_btnAddWar.gridx = 0;
		gbc_btnAddWar.gridy = 4;
		pnlSamples.add(btnAddWar, gbc_btnAddWar);
		globalEnable.addComponent("btnAddWar", btnAddWar);
		btnAddWar.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnAddWarCallback();
			}});
		
		btnRemove = new JButton("Remove Selected");
		GridBagConstraints gbc_btnRemove = new GridBagConstraints();
		gbc_btnRemove.insets = new Insets(0, 0, 5, 5);
		gbc_btnRemove.gridx = 2;
		gbc_btnRemove.gridy = 4;
		pnlSamples.add(btnRemove, gbc_btnRemove);
		globalEnable.addComponent("btnRemove", btnRemove);
		btnRemove.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnRemoveCallback();
			}});
		
		JPanel pnlInfo = new JPanel();
		pnlInfo.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlInfo = new GridBagConstraints();
		gbc_pnlInfo.insets = new Insets(0, 0, 5, 0);
		gbc_pnlInfo.fill = GridBagConstraints.BOTH;
		gbc_pnlInfo.gridx = 1;
		gbc_pnlInfo.gridy = 0;
		getContentPane().add(pnlInfo, gbc_pnlInfo);
		
		JPanel pnlButtons = new JPanel();
		GridBagConstraints gbc_pnlButtons = new GridBagConstraints();
		gbc_pnlButtons.gridwidth = 2;
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
		gbc_btnOkay.insets = new Insets(0, 0, 5, 5);
		gbc_btnOkay.gridx = 1;
		gbc_btnOkay.gridy = 0;
		pnlButtons.add(btnOkay, gbc_btnOkay);
		globalEnable.addComponent("btnOkay", btnOkay);
		btnOkay.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnOkayCallback();
			}});
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(0, 0, 5, 5);
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 0;
		pnlButtons.add(btnCancel, gbc_btnCancel);
		globalEnable.addComponent("btnCancel", btnCancel);
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnCancelCallback();
			}});
	}
	
	/*----- Getters -----*/
	
	public boolean getExitSelection(){return exitSelection;}
	
	public List<ImportableSample> getSamples(){
		List<ImportableSample> list = new ArrayList<ImportableSample>(allSamples.size()+1);
		list.addAll(allSamples.values());
		return list;
	}
	
	/*----- Setters -----*/
	
	/*----- Draw -----*/
	
	public void disableAll(){
		globalEnable.setEnabling(false);
		globalEnable.repaint();
	}
	
	public void enableAll(){
		globalEnable.setEnabling(true);
		btnRemove.setEnabled(!lstSamples.isSelectionEmpty());
		globalEnable.repaint();
	}
	
	/*----- Callbacks -----*/
	
	private void btnOkayCallback(){
		exitSelection = true;
		setVisible(false);
		dispose();
	}
	
	private void btnCancelCallback(){
		exitSelection = false;
		allSamples.clear();
		setVisible(false);
		dispose();
	}
	
	private void btnRemoveCallback(){
		//TODO
	}
	
	private void btnAddDirCallback(){
		//TODO
	}
	
	private void btnAddWavCallback(){
		//TODO
	}
	
	private void btnAddWarCallback(){
		//TODO
	}
	
	private void btnAddSarCallback(){
		//TODO
	}
	
	private void lstSelectCallback(){
		//TODO
	}
	
}
