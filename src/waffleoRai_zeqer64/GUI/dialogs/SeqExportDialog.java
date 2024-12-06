package waffleoRai_zeqer64.GUI.dialogs;

import javax.swing.JFrame;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.WRDialog;
import java.awt.GridBagLayout;
import javax.swing.JPanel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.border.EtchedBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;

public class SeqExportDialog extends WRDialog{

	private static final long serialVersionUID = -484311827832149562L;
	
	private static final int MIN_WIDTH = 350;
	private static final int MIN_HEIGHT = 200;
	
	public static final int FMT_IDX_MID = 0;
	public static final int FMT_IDX_MUS = 1;
	public static final int FMT_IDX_BIN = 2;
	
	public static final int SYNTAX_IDX_ZEQER = 0;
	public static final int SYNTAX_IDX_SEQ64 = 1;
	public static final int SYNTAX_IDX_ZRET = 2;
	
	/*--- Instance Variables ---*/
	
	//private JFrame parent;
	private ComponentGroup cgFormatMML;
	
	private boolean exitSelection = false;
	private String lastPath = null;
	
	private JTextField txtPath;
	private JComboBox<String> cmbxFormat;
	private JComboBox<String> cmbxSyntax;
	
	/*--- Init ---*/
	
	public SeqExportDialog(JFrame parentFrame) {
		super(parentFrame, true);
		//parent = parentFrame;
		cgFormatMML = globalEnable.newChild();
		initGUI();
	}

	public void initGUI() {
		setResizable(false);
		
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		
		setTitle("Export Sequence");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel pnlOps = new JPanel();
		GridBagConstraints gbc_pnlOps = new GridBagConstraints();
		gbc_pnlOps.insets = new Insets(10, 5, 5, 5);
		gbc_pnlOps.fill = GridBagConstraints.BOTH;
		gbc_pnlOps.gridx = 0;
		gbc_pnlOps.gridy = 0;
		getContentPane().add(pnlOps, gbc_pnlOps);
		GridBagLayout gbl_pnlOps = new GridBagLayout();
		gbl_pnlOps.columnWidths = new int[]{0, 0, 0};
		gbl_pnlOps.rowHeights = new int[]{0, 0, 0};
		gbl_pnlOps.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlOps.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		pnlOps.setLayout(gbl_pnlOps);
		
		JLabel lblFormat = new JLabel("Format:");
		GridBagConstraints gbc_lblFormat = new GridBagConstraints();
		gbc_lblFormat.insets = new Insets(0, 0, 5, 5);
		gbc_lblFormat.anchor = GridBagConstraints.EAST;
		gbc_lblFormat.gridx = 0;
		gbc_lblFormat.gridy = 0;
		pnlOps.add(lblFormat, gbc_lblFormat);
		
		cmbxFormat = new JComboBox<String>();
		GridBagConstraints gbc_cmbxFormat = new GridBagConstraints();
		gbc_cmbxFormat.insets = new Insets(0, 0, 5, 0);
		gbc_cmbxFormat.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxFormat.gridx = 1;
		gbc_cmbxFormat.gridy = 0;
		pnlOps.add(cmbxFormat, gbc_cmbxFormat);
		globalEnable.addComponent("cmbxFormat", cmbxFormat);
		cmbxFormat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cmbxFormatCallback();
			}});
		
		JLabel lblSyntax = new JLabel("Syntax:");
		GridBagConstraints gbc_lblSyntax = new GridBagConstraints();
		gbc_lblSyntax.insets = new Insets(0, 0, 0, 5);
		gbc_lblSyntax.anchor = GridBagConstraints.EAST;
		gbc_lblSyntax.gridx = 0;
		gbc_lblSyntax.gridy = 1;
		pnlOps.add(lblSyntax, gbc_lblSyntax);
		
		cmbxSyntax = new JComboBox<String>();
		GridBagConstraints gbc_cmbxSyntax = new GridBagConstraints();
		gbc_cmbxSyntax.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxSyntax.gridx = 1;
		gbc_cmbxSyntax.gridy = 1;
		pnlOps.add(cmbxSyntax, gbc_cmbxSyntax);
		cgFormatMML.addComponent("cmbxSyntax", cmbxSyntax);
		
		JPanel pnlPath = new JPanel();
		GridBagConstraints gbc_pnlPath = new GridBagConstraints();
		gbc_pnlPath.insets = new Insets(0, 0, 5, 0);
		gbc_pnlPath.fill = GridBagConstraints.BOTH;
		gbc_pnlPath.gridx = 0;
		gbc_pnlPath.gridy = 1;
		getContentPane().add(pnlPath, gbc_pnlPath);
		GridBagLayout gbl_pnlPath = new GridBagLayout();
		gbl_pnlPath.columnWidths = new int[]{0, 0, 175, 0, 0, 0};
		gbl_pnlPath.rowHeights = new int[]{0, 0};
		gbl_pnlPath.columnWeights = new double[]{1.0, 0.0, 1.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlPath.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlPath.setLayout(gbl_pnlPath);
		
		JLabel lblPath = new JLabel("Path:");
		GridBagConstraints gbc_lblPath = new GridBagConstraints();
		gbc_lblPath.anchor = GridBagConstraints.EAST;
		gbc_lblPath.insets = new Insets(5, 5, 5, 5);
		gbc_lblPath.gridx = 1;
		gbc_lblPath.gridy = 0;
		pnlPath.add(lblPath, gbc_lblPath);
		
		txtPath = new JTextField();
		GridBagConstraints gbc_txtPath = new GridBagConstraints();
		gbc_txtPath.insets = new Insets(5, 5, 5, 5);
		gbc_txtPath.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtPath.gridx = 2;
		gbc_txtPath.gridy = 0;
		pnlPath.add(txtPath, gbc_txtPath);
		txtPath.setColumns(10);
		globalEnable.addComponent("txtPath", txtPath);
		
		JButton btnBrowse = new JButton("Browse...");
		GridBagConstraints gbc_btnBrowse = new GridBagConstraints();
		gbc_btnBrowse.insets = new Insets(5, 5, 5, 5);
		gbc_btnBrowse.gridx = 3;
		gbc_btnBrowse.gridy = 0;
		pnlPath.add(btnBrowse, gbc_btnBrowse);
		globalEnable.addComponent("btnBrowse", btnBrowse);
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnBrowseCallback();
			}});
		
		JPanel pnlButtons = new JPanel();
		pnlButtons.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlButtons = new GridBagConstraints();
		gbc_pnlButtons.insets = new Insets(0, 5, 5, 5);
		gbc_pnlButtons.fill = GridBagConstraints.BOTH;
		gbc_pnlButtons.gridx = 0;
		gbc_pnlButtons.gridy = 2;
		getContentPane().add(pnlButtons, gbc_pnlButtons);
		GridBagLayout gbl_pnlButtons = new GridBagLayout();
		gbl_pnlButtons.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlButtons.rowHeights = new int[]{0, 0};
		gbl_pnlButtons.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlButtons.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlButtons.setLayout(gbl_pnlButtons);
		
		JButton btnOkay = new JButton("Okay");
		GridBagConstraints gbc_btnOkay = new GridBagConstraints();
		gbc_btnOkay.insets = new Insets(5, 0, 5, 5);
		gbc_btnOkay.gridx = 1;
		gbc_btnOkay.gridy = 0;
		pnlButtons.add(btnOkay, gbc_btnOkay);
		globalEnable.addComponent("btnOkay", btnOkay);
		btnOkay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnOkayCallback();
			}});
		
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
			}});
		
		populateComboboxes();
	}
	
	private void populateComboboxes() {
		DefaultComboBoxModel<String> mdl = new DefaultComboBoxModel<String>();
		mdl.addElement("MIDI (.mid)");
		mdl.addElement("MML Script (.mus)");
		mdl.addElement("Raw Binary (.bin)");
		cmbxFormat.setModel(mdl);
		cmbxFormat.setSelectedIndex(0);
		
		mdl = new DefaultComboBoxModel<String>();
		mdl.addElement("Zeqer");
		mdl.addElement("Seq64");
		mdl.addElement("ZeldaRet");
		cmbxSyntax.setModel(mdl);
		cmbxSyntax.setSelectedIndex(0);
		cmbxSyntax.setEnabled(false);
	}
	
	/*--- Getters ---*/
	
	public boolean getExitSelection() {return exitSelection;}
	//public String getLastPath() {return lastPath;}
	public int getFormatSelection() {return cmbxFormat.getSelectedIndex();}
	public int getSyntaxSelection() {return cmbxSyntax.getSelectedIndex();}
	
	public String getTargetPath() {
		String path = txtPath.getText();
		if(path == null || path.isEmpty()) return null;
		
		/*if(path.contains(".")) {
			path = path.substring(0, path.lastIndexOf('.'));
			switch(cmbxFormat.getSelectedIndex()) {
			case FMT_IDX_MID:
				path += ".mid"; break;
			case FMT_IDX_MUS:
				path += ".mus"; break;
			case FMT_IDX_BIN:
				path += ".bin"; break;
			}
		}*/
		
		return path;
	}
	
	/*--- Setters ---*/
	
	public void setLastPath(String val) {lastPath = val;}
	
	/*--- Draw ---*/
	
	public void reenable() {
		super.reenable();
		cgFormatMML.setEnabling(cmbxFormat.getSelectedIndex() == FMT_IDX_MUS);
		repaint();
	}
	
	/*--- Callbacks ---*/
	
	private void btnOkayCallback() {
		String path = txtPath.getText();
		if(path == null || path.isEmpty()) {
			showError("Please specify output path!");
			return;
		}
		
		exitSelection = true;
		closeMe();
	}
	
	private void btnCancelCallback() {
		exitSelection = false;
		closeMe();
	}
	
	private void btnBrowseCallback() {
		JFileChooser fc = new JFileChooser(lastPath);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		setWait();
		int ret = fc.showSaveDialog(this);
		
		if(ret == JFileChooser.APPROVE_OPTION) {
			lastPath = fc.getSelectedFile().getAbsolutePath();
			/*if(lastPath.contains(".")) {
				lastPath = lastPath.substring(0, lastPath.lastIndexOf('.'));
			}*/
			txtPath.setText(lastPath);
		}
		unsetWait();
		
	}
	
	private void cmbxFormatCallback() {
		reenable();
	}
	
}
