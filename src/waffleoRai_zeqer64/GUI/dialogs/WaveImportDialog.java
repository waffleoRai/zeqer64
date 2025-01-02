package waffleoRai_zeqer64.GUI.dialogs;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.WRDialog;
import waffleoRai_zeqer64.GUI.sampleImport.SampleOptionsPanel;
import waffleoRai_zeqer64.filefmt.wave.ZeqerWaveIO;
import waffleoRai_zeqer64.filefmt.wave.ZeqerWaveIO.SampleImportOptions;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.awt.Dimension;

public class WaveImportDialog extends WRDialog{

	private static final long serialVersionUID = 6167906778954572163L;
	
	public static final int DEFO_WIDTH = 640;
	public static final int DEFO_HEIGHT = 600;
	
	public static final String OPKEY_LAST_SMPL_IMPORT = "LAST_SMPL_IMPORT_PATH";
	
	public static final int ERROR_NONE = SampleOptionsPanel.ERROR_NONE;
	public static final int ERROR_NO_FILES = SampleOptionsPanel.ERROR_NO_FILES;
	public static final int ERROR_BAD_CUSTOM_CH = SampleOptionsPanel.ERROR_BAD_CUSTOM_CH;
	public static final int ERROR_BAD_LOOP_TEXT = SampleOptionsPanel.ERROR_BAD_LOOP_TEXT;
	
	/*----- Instance Variable -----*/
	
	private ZeqerCoreInterface core;
	private boolean exitSelection = false;
	
	private SampleOptionsPanel pnlOps;
	
	private ComponentGroup cgItemSelected;
	private ComponentGroup cgListNotEmpty;

	private List<String> pathList;
	private Set<String> tags;
	private JList<String> lstFiles;
	
	/*----- Init -----*/
	
	public WaveImportDialog(JFrame parent, ZeqerCoreInterface core_iface){
		super(parent, true);
		core = core_iface;
		
		cgListNotEmpty = globalEnable.newChild();
		cgItemSelected = cgListNotEmpty.newChild();
		
		pathList = new LinkedList<String>();
		tags = new HashSet<String>();

		initGUI();
		reenable();
	}
	
	private void initGUI(){
		setMinimumSize(new Dimension(DEFO_WIDTH, DEFO_HEIGHT));
		setPreferredSize(new Dimension(DEFO_WIDTH, DEFO_HEIGHT));
		
		setTitle("Import Sound Sample");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 200, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.9, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		pnlOps = new SampleOptionsPanel();
		GridBagConstraints gbc_pnlOptions = new GridBagConstraints();
		gbc_pnlOptions.insets = new Insets(5, 5, 5, 5);
		gbc_pnlOptions.fill = GridBagConstraints.BOTH;
		gbc_pnlOptions.gridx = 0;
		gbc_pnlOptions.gridy = 0;
		getContentPane().add(pnlOps, gbc_pnlOptions);
		
		JPanel pnlFiles = new JPanel();
		pnlFiles.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlFiles = new GridBagConstraints();
		gbc_pnlFiles.weightx = 2.0;
		gbc_pnlFiles.insets = new Insets(5, 0, 5, 5);
		gbc_pnlFiles.fill = GridBagConstraints.BOTH;
		gbc_pnlFiles.gridx = 1;
		gbc_pnlFiles.gridy = 0;
		getContentPane().add(pnlFiles, gbc_pnlFiles);
		GridBagLayout gbl_pnlFiles = new GridBagLayout();
		gbl_pnlFiles.columnWidths = new int[]{200, 0};
		gbl_pnlFiles.rowHeights = new int[]{0, 0, 0};
		gbl_pnlFiles.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlFiles.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		pnlFiles.setLayout(gbl_pnlFiles);
		
		JScrollPane spFiles = new JScrollPane();
		spFiles.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spFiles = new GridBagConstraints();
		gbc_spFiles.insets = new Insets(5, 5, 5, 5);
		gbc_spFiles.fill = GridBagConstraints.BOTH;
		gbc_spFiles.gridx = 0;
		gbc_spFiles.gridy = 0;
		pnlFiles.add(spFiles, gbc_spFiles);
		
		lstFiles = new JList<String>();
		spFiles.setViewportView(lstFiles);
		lstFiles.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				listSelectCallback();
			}
		});
		
		globalEnable.addComponent("lstFiles", lstFiles);
		globalEnable.addComponent("spFiles", spFiles);
		
		JPanel pnlBtnFiles = new JPanel();
		GridBagConstraints gbc_pnlBtnFiles = new GridBagConstraints();
		gbc_pnlBtnFiles.insets = new Insets(0, 5, 5, 5);
		gbc_pnlBtnFiles.fill = GridBagConstraints.BOTH;
		gbc_pnlBtnFiles.gridx = 0;
		gbc_pnlBtnFiles.gridy = 1;
		pnlFiles.add(pnlBtnFiles, gbc_pnlBtnFiles);
		GridBagLayout gbl_pnlBtnFiles = new GridBagLayout();
		gbl_pnlBtnFiles.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlBtnFiles.rowHeights = new int[]{0, 0};
		gbl_pnlBtnFiles.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlBtnFiles.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlBtnFiles.setLayout(gbl_pnlBtnFiles);
		
		JButton btnDelete = new JButton("Delete");
		GridBagConstraints gbc_btnDelete = new GridBagConstraints();
		gbc_btnDelete.insets = new Insets(0, 0, 0, 5);
		gbc_btnDelete.gridx = 0;
		gbc_btnDelete.gridy = 0;
		pnlBtnFiles.add(btnDelete, gbc_btnDelete);
		cgItemSelected.addComponent("btnDelete", btnDelete);
		btnDelete.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnDeleteCallback();
			}});
		
		JButton btnAdd = new JButton("Add...");
		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.gridx = 2;
		gbc_btnAdd.gridy = 0;
		pnlBtnFiles.add(btnAdd, gbc_btnAdd);
		globalEnable.addComponent("btnAdd", btnAdd);
		btnAdd.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnAddCallback();
			}});
		
		JPanel pnlBtn = new JPanel();
		GridBagConstraints gbc_pnlBtn = new GridBagConstraints();
		gbc_pnlBtn.gridwidth = 2;
		gbc_pnlBtn.insets = new Insets(0, 5, 5, 5);
		gbc_pnlBtn.fill = GridBagConstraints.BOTH;
		gbc_pnlBtn.gridx = 0;
		gbc_pnlBtn.gridy = 1;
		getContentPane().add(pnlBtn, gbc_pnlBtn);
		GridBagLayout gbl_pnlBtn = new GridBagLayout();
		gbl_pnlBtn.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlBtn.rowHeights = new int[]{0, 0};
		gbl_pnlBtn.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlBtn.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlBtn.setLayout(gbl_pnlBtn);
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(0, 0, 0, 5);
		gbc_btnCancel.gridx = 1;
		gbc_btnCancel.gridy = 0;
		pnlBtn.add(btnCancel, gbc_btnCancel);
		globalEnable.addComponent("btnCancel", btnCancel);
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnCancelCallback();
			}});
		
		JButton btnOkay = new JButton("Okay");
		GridBagConstraints gbc_btnOkay = new GridBagConstraints();
		gbc_btnOkay.gridx = 2;
		gbc_btnOkay.gridy = 0;
		pnlBtn.add(btnOkay, gbc_btnOkay);
		cgListNotEmpty.addComponent("btnOkay", btnOkay);
		btnOkay.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnOkayCallback();
			}});
	
	}
	
	/*----- Getters -----*/
	
	public JFrame getParentFrame(){return parent;}
	public boolean getExitSelection(){return this.exitSelection;}
	
	public int getSettings(SampleImportOptions ops){
		return pnlOps.getSettings(ops);
	}
	
	public List<String> getFilePaths(){
		int count = pathList.size();
		if(count < 1) return new LinkedList<String>();
		ArrayList<String> lst = new ArrayList<String>(count);
		lst.addAll(pathList);
		return lst;
	}
	
	public Set<String> getTags(){
		Set<String> copy = new HashSet<String>();
		copy.addAll(tags);
		return copy;
	}
	
	public int checkForError(){
		if(pathList.isEmpty()) return ERROR_NO_FILES;
		return pnlOps.checkForError();
	}
	
	/*----- Setters -----*/
	
	/*----- Draw -----*/
	
	private void refreshFileList(){
		Collections.sort(pathList);
		
		DefaultListModel<String> model = new DefaultListModel<String>();
		for(String p : pathList){
			model.addElement(p);
		}
		
		lstFiles.setModel(model);
		//reenable();
	}
	
	public void reenable(){
		globalEnable.setEnabling(true);
		if(pathList.isEmpty()){
			cgListNotEmpty.setEnabling(false);
			pnlOps.disableAll();
		}
		else{
			boolean sel = !lstFiles.isSelectionEmpty();
			cgItemSelected.setEnabling(sel);
			if(sel) pnlOps.reenable();
			else pnlOps.disableAll();
		}
		pnlOps.repaint();
		repaint();
	}
	
	/*----- Callbacks -----*/
	
	private void listSelectCallback() {
		reenable();
	}
	
	private void btnOkayCallback(){
		int err = checkForError();

		if(err != ERROR_NONE){
			String errmsg = null;
			
			switch(err){
			case ERROR_NO_FILES:
				errmsg = "Please select at least one file to import.";
				break;
			case ERROR_BAD_CUSTOM_CH:
				errmsg = "Please enter a channel value that is an integer greater than or equal to 0.";
				break;
			case ERROR_BAD_LOOP_TEXT:
				errmsg = "Please enter valid loop values (integers, loop points must be positive or 0).";
				break;
			}
			
			JOptionPane.showMessageDialog(this, errmsg, "Invalid Options", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		exitSelection = true;
		closeMe();
	}
	
	private void btnCancelCallback(){
		exitSelection = false;
		closeMe();
	}
	
	private void btnAddCallback(){
		String last = null;
		if(core != null) last = core.getSetting(OPKEY_LAST_SMPL_IMPORT);
		
		JFileChooser fc = new JFileChooser(last);
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setMultiSelectionEnabled(true);
		ZeqerWaveIO.addImportableFileFilters(fc);
		
		int sel = fc.showOpenDialog(this);
		if(sel != JFileChooser.APPROVE_OPTION) return;
		
		File[] files = fc.getSelectedFiles();
		if(files == null) return;
		
		setWait();
		for(File f : files){
			pathList.add(f.getAbsolutePath().toString());
		}
		refreshFileList();
		unsetWait();
	}
	
	private void btnDeleteCallback(){
		if(lstFiles.isSelectionEmpty()) return;
		
		int ret = JOptionPane.showConfirmDialog(this, "Remove the selected file(s) for import?", 
				"Remove Import Files", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		
		if(ret != JOptionPane.YES_OPTION) return;
		
		setWait();
		List<String> sel = lstFiles.getSelectedValuesList();
		pathList.removeAll(sel);
		
		refreshFileList();
		unsetWait();
	}
	
}
