package waffleoRai_zeqer64.GUI.dialogs;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.WRDialog;
import waffleoRai_zeqer64.GUI.SeqTypeCombobox;
import waffleoRai_zeqer64.filefmt.seq.ZeqerSeqIO;
import waffleoRai_zeqer64.filefmt.seq.ZeqerSeqIO.SeqImportOptions;

public class SeqMultiImportDialog extends WRDialog{
	
	/*----- Constants -----*/
	
	private static final long serialVersionUID = -520793174057249945L;
	
	public static final int WIDTH = 450;
	public static final int HEIGHT = 270;
	
	/*----- Instance Variables -----*/
	
	private ComponentGroup filesEnabled; //Enabled when there is at least one file in list
	
	private JList<String> lstFiles;
	private SeqTypeCombobox cmbxCategory;
	private JCheckBox cbTimeMin;
	private JCheckBox cbVoxLimit;
	
	private Set<String> tags;
	
	private boolean exitSelection = false;
	private String lastImportPath = null;

	/*----- Init -----*/
	
	public SeqMultiImportDialog(JFrame parent) {
		super(parent, true);
		filesEnabled = globalEnable.newChild();
		initGUI();
		tags = new HashSet<String>();
	}
	
	private void initGUI() {
		setMinimumSize(new Dimension(WIDTH, HEIGHT));
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		
		setTitle("Import Sequences");
		setResizable(false);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{200, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel pnlOptions = new JPanel();
		GridBagConstraints gbc_pnlOptions = new GridBagConstraints();
		gbc_pnlOptions.insets = new Insets(5, 5, 5, 5);
		gbc_pnlOptions.fill = GridBagConstraints.BOTH;
		gbc_pnlOptions.gridx = 0;
		gbc_pnlOptions.gridy = 0;
		getContentPane().add(pnlOptions, gbc_pnlOptions);
		GridBagLayout gbl_pnlOptions = new GridBagLayout();
		gbl_pnlOptions.columnWidths = new int[]{0, 0, 0};
		gbl_pnlOptions.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gbl_pnlOptions.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlOptions.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		pnlOptions.setLayout(gbl_pnlOptions);
		
		JLabel lclCategory = new JLabel("Category:");
		GridBagConstraints gbc_lclCategory = new GridBagConstraints();
		gbc_lclCategory.insets = new Insets(0, 0, 5, 5);
		gbc_lclCategory.anchor = GridBagConstraints.EAST;
		gbc_lclCategory.gridx = 0;
		gbc_lclCategory.gridy = 0;
		pnlOptions.add(lclCategory, gbc_lclCategory);
		
		cmbxCategory = new SeqTypeCombobox();
		GridBagConstraints gbc_cmbxCategory = new GridBagConstraints();
		gbc_cmbxCategory.insets = new Insets(0, 0, 5, 0);
		gbc_cmbxCategory.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxCategory.gridx = 1;
		gbc_cmbxCategory.gridy = 0;
		pnlOptions.add(cmbxCategory, gbc_cmbxCategory);
		globalEnable.addComponent("cmbxCategory", cmbxCategory);
		
		cbVoxLimit = new JCheckBox("Limit to 4 voices per channel");
		GridBagConstraints gbc_cbVoxLimit = new GridBagConstraints();
		gbc_cbVoxLimit.gridwidth = 2;
		gbc_cbVoxLimit.insets = new Insets(0, 0, 5, 5);
		gbc_cbVoxLimit.gridx = 0;
		gbc_cbVoxLimit.gridy = 1;
		cbVoxLimit.setSelected(true);
		pnlOptions.add(cbVoxLimit, gbc_cbVoxLimit);
		globalEnable.addComponent("cbVoxLimit", cbVoxLimit);
		
		cbTimeMin = new JCheckBox("Limit note speed");
		GridBagConstraints gbc_cbTimeMin = new GridBagConstraints();
		gbc_cbTimeMin.gridwidth = 2;
		gbc_cbTimeMin.insets = new Insets(0, 0, 5, 5);
		gbc_cbTimeMin.gridx = 0;
		gbc_cbTimeMin.gridy = 2;
		cbTimeMin.setSelected(true);
		pnlOptions.add(cbTimeMin, gbc_cbTimeMin);
		globalEnable.addComponent("cbTimeMin", cbTimeMin);
		
		JButton btnTags = new JButton("Tags...");
		GridBagConstraints gbc_btnTags = new GridBagConstraints();
		gbc_btnTags.insets = new Insets(0, 0, 0, 5);
		gbc_btnTags.gridx = 0;
		gbc_btnTags.gridy = 4;
		pnlOptions.add(btnTags, gbc_btnTags);
		globalEnable.addComponent("btnTags", btnTags);
		btnTags.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnTagsCallback();
			}
		});
		
		JPanel pnlFiles = new JPanel();
		GridBagConstraints gbc_pnlFiles = new GridBagConstraints();
		gbc_pnlFiles.insets = new Insets(5, 0, 5, 5);
		gbc_pnlFiles.fill = GridBagConstraints.BOTH;
		gbc_pnlFiles.gridx = 1;
		gbc_pnlFiles.gridy = 0;
		getContentPane().add(pnlFiles, gbc_pnlFiles);
		GridBagLayout gbl_pnlFiles = new GridBagLayout();
		gbl_pnlFiles.columnWidths = new int[]{0, 0};
		gbl_pnlFiles.rowHeights = new int[]{0, 0, 0};
		gbl_pnlFiles.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlFiles.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		pnlFiles.setLayout(gbl_pnlFiles);
		
		JScrollPane spFiles = new JScrollPane();
		spFiles.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spFiles = new GridBagConstraints();
		gbc_spFiles.insets = new Insets(0, 0, 5, 0);
		gbc_spFiles.fill = GridBagConstraints.BOTH;
		gbc_spFiles.gridx = 0;
		gbc_spFiles.gridy = 0;
		pnlFiles.add(spFiles, gbc_spFiles);
		filesEnabled.addComponent("spFiles", spFiles);
		
		lstFiles = new JList<String>();
		spFiles.setViewportView(lstFiles);
		filesEnabled.addComponent("lstFiles", lstFiles);
		
		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		pnlFiles.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JButton btnClear = new JButton("Clear");
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.insets = new Insets(0, 5, 5, 5);
		gbc_btnNewButton_1.gridx = 0;
		gbc_btnNewButton_1.gridy = 0;
		panel.add(btnClear, gbc_btnNewButton_1);
		filesEnabled.addComponent("btnClear", btnClear);
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnClearCallback();
			}
		});
		
		JButton btnAddFiles = new JButton("Add Files...");
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewButton.gridx = 2;
		gbc_btnNewButton.gridy = 0;
		panel.add(btnAddFiles, gbc_btnNewButton);
		globalEnable.addComponent("btnAddFiles", btnAddFiles);
		btnAddFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnAddFilesCallback();
			}
		});
		
		JPanel pnlButtons = new JPanel();
		pnlButtons.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
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
		
		JButton btnImport = new JButton("Import");
		GridBagConstraints gbc_btnImport = new GridBagConstraints();
		gbc_btnImport.insets = new Insets(5, 0, 5, 5);
		gbc_btnImport.gridx = 1;
		gbc_btnImport.gridy = 0;
		pnlButtons.add(btnImport, gbc_btnImport);
		filesEnabled.addComponent("btnImport", btnImport);
		btnImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnImportCallback();
			}
		});
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(5, 0, 5, 5);
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 0;
		pnlButtons.add(btnCancel, gbc_btnCancel);
		globalEnable.addComponent("btnCancel", btnCancel);
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnCancelCallback();
			}
		});
	}
	
	/*----- Getters -----*/
	
	public boolean getExitSelection() {return exitSelection;}
	
	public boolean getOptions(SeqImportOptions op) {
		if(op == null) return false;
		op.limitNoteSpeed = cbTimeMin.isSelected();
		op.limitVoxPerChannel = cbVoxLimit.isSelected();
		op.tags = tags;
		op.seqType = cmbxCategory.getRawValue();
		return true;
	}
	
	public List<String> getFilePaths(){
		ListModel<String> mdl = lstFiles.getModel();
		int count = mdl.getSize();
		if(count < 1) return new ArrayList<String>(1);
		ArrayList<String> list = new ArrayList<String>(count);
		for(int i = 0; i < count; i++) {
			list.add(mdl.getElementAt(i));
		}
		
		return list;
	}
	
	public String getLastImportPath(){return lastImportPath;}
	
	/*----- Setters -----*/
	
	public void setLastImportPath(String val) {lastImportPath = val;}
	
	/*----- GUI -----*/
	
	public void reenable() {
		super.reenable();
		if(lstFiles.getModel().getSize() < 1) {
			filesEnabled.setEnabling(false);
			filesEnabled.repaint();
		}
	}
	
	/*----- Callbacks -----*/
	
	public void btnImportCallback() {
		exitSelection = true;
		closeMe();
	}
	
	public void btnCancelCallback() {
		exitSelection = false;
		closeMe();
	}
	
	public void btnTagsCallback() {
		setWait();
		ZeqerTagEditDialog dialog = new ZeqerTagEditDialog(parent);
		dialog.loadTags(tags);
		dialog.showMe(this);
		
		if(dialog.getExitSelection()) {
			tags.clear();
			tags.addAll(dialog.getTags());
		}
		unsetWait();
	}
	
	public void btnClearCallback() {
		setWait();
		
		int ret = JOptionPane.showConfirmDialog(this, "Are you sure you want to clear the file list?", 
				"Clear Files", JOptionPane.YES_NO_OPTION);
		
		if(ret == JOptionPane.YES_OPTION) {
			lstFiles.setModel(new DefaultListModel<String>());
		}
		unsetWait();
	}
	
	public void btnAddFilesCallback() {
		JFileChooser fc = new JFileChooser(lastImportPath);
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setMultiSelectionEnabled(true);
		ZeqerSeqIO.addImportableFileFilters(fc);
		
		int ret = fc.showOpenDialog(this);
		
		if(ret != JFileChooser.APPROVE_OPTION) return;
		
		setWait();
		//Extract file list
		File[] farr = fc.getSelectedFiles();
		if(farr != null) {
			List<String> existingFiles = this.getFilePaths();
			DefaultListModel<String> mdl = new DefaultListModel<String>();
			for(String s : existingFiles) mdl.addElement(s);
			for(int i = 0; i < farr.length; i++) {
				mdl.addElement(farr[i].getAbsolutePath());
			}
		}
		
		unsetWait();
	}

}
