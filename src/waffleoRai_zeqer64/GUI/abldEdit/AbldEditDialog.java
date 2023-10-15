package waffleoRai_zeqer64.GUI.abldEdit;

import javax.swing.JFrame;

import java.awt.GridBagLayout;
import javax.swing.JTabbedPane;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.RadioButtonGroup;
import waffleoRai_GUITools.WRDialog;
import waffleoRai_zeqer64.ZeqerRom;
import waffleoRai_zeqer64.filefmt.AbldFile;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import javax.swing.JPanel;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.ZonedDateTime;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class AbldEditDialog extends WRDialog{
	//Put the set/clear ref here as it's common across warc/bnk/seq

	private static final long serialVersionUID = -5950488722882431036L;
	
	private static final int RBIDX_NTSC = 0;
	private static final int RBIDX_PAL = 1;
	private static final int RBIDX_MPAL = 2;
	//private static final int RBIDX_NONE = -1;
	
	private static final int WARC_TAB_ME = 0;
	private static final int WARC_TAB_REF = 1;
	
	public static final int MIN_WIDTH = 540;
	public static final int MIN_HEIGHT = 500;
	
	/*----- Instance Variables -----*/
	
	private ZeqerCoreInterface core;
	
	private boolean readOnly = false;
	private boolean exitSelection = false;
	private boolean injectionMode = false;
	private boolean refLoaded = false;
	private ComponentGroup cgEditable;
	private ComponentGroup cgRefSettable;
	
	private JTabbedPane tpWarc; //Link to control ref tab enabled.
	private AbldSeqEditPanel pnlSeq;
	private AbldBnkEditPanel pnlBank;
	private WarcSlotEditPanel pnlWarcMe;
	private WarcSlotEditPanel pnlWarcRef;
	
	private JTextField txtName;
	private RadioButtonGroup rbgTVType;
	private JLabel lblRefName;
	
	/*----- Init -----*/
	
	public AbldEditDialog(JFrame parentFrame, ZeqerCoreInterface coreIFace, boolean readOnly){
		super(parentFrame, true);
		core = coreIFace;
		this.readOnly = readOnly;
		cgEditable = globalEnable.newChild();
		cgRefSettable = cgEditable.newChild();
		rbgTVType = new RadioButtonGroup(3);
		
		initGUI();
	}

	private void initGUI(){
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		
		setTitle("Edit Audio Build");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel pnlMeta = new JPanel();
		GridBagConstraints gbc_pnlMeta = new GridBagConstraints();
		gbc_pnlMeta.insets = new Insets(5, 5, 5, 5);
		gbc_pnlMeta.fill = GridBagConstraints.BOTH;
		gbc_pnlMeta.gridx = 0;
		gbc_pnlMeta.gridy = 0;
		getContentPane().add(pnlMeta, gbc_pnlMeta);
		GridBagLayout gbl_pnlMeta = new GridBagLayout();
		gbl_pnlMeta.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
		gbl_pnlMeta.rowHeights = new int[]{0, 0};
		gbl_pnlMeta.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlMeta.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlMeta.setLayout(gbl_pnlMeta);
		
		JLabel lblName = new JLabel("Name:");
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.EAST;
		gbc_lblName.insets = new Insets(0, 0, 0, 5);
		gbc_lblName.gridx = 0;
		gbc_lblName.gridy = 0;
		pnlMeta.add(lblName, gbc_lblName);
		
		txtName = new JTextField();
		GridBagConstraints gbc_txtName = new GridBagConstraints();
		gbc_txtName.insets = new Insets(0, 0, 0, 5);
		gbc_txtName.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtName.gridx = 1;
		gbc_txtName.gridy = 0;
		pnlMeta.add(txtName, gbc_txtName);
		txtName.setColumns(10);
		cgEditable.addComponent("txtName", txtName);
		
		JRadioButton rbNtsc = new JRadioButton("NTSC");
		GridBagConstraints gbc_rbNtsc = new GridBagConstraints();
		gbc_rbNtsc.insets = new Insets(0, 0, 0, 5);
		gbc_rbNtsc.gridx = 3;
		gbc_rbNtsc.gridy = 0;
		pnlMeta.add(rbNtsc, gbc_rbNtsc);
		cgEditable.addComponent("rbNtsc", rbNtsc);
		rbgTVType.addButton(rbNtsc, RBIDX_NTSC);
		rbNtsc.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				rbTVSelectCallback(RBIDX_NTSC);
			}});
		
		JRadioButton rbPal = new JRadioButton("PAL");
		GridBagConstraints gbc_rbPal = new GridBagConstraints();
		gbc_rbPal.insets = new Insets(0, 0, 0, 5);
		gbc_rbPal.gridx = 4;
		gbc_rbPal.gridy = 0;
		pnlMeta.add(rbPal, gbc_rbPal);
		cgEditable.addComponent("rbPal", rbPal);
		rbgTVType.addButton(rbPal, RBIDX_PAL);
		rbPal.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				rbTVSelectCallback(RBIDX_PAL);
			}});
		
		JRadioButton rbMPal = new JRadioButton("MPAL");
		GridBagConstraints gbc_rbMPal = new GridBagConstraints();
		gbc_rbMPal.gridx = 5;
		gbc_rbMPal.gridy = 0;
		pnlMeta.add(rbMPal, gbc_rbMPal);
		cgEditable.addComponent("rbMPal", rbMPal);
		rbgTVType.addButton(rbMPal, RBIDX_MPAL);
		rbMPal.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				rbTVSelectCallback(RBIDX_MPAL);
			}});
		
		rbgTVType.select(0);
		
		JPanel pnlSetRef = new JPanel();
		GridBagConstraints gbc_pnlSetRef = new GridBagConstraints();
		gbc_pnlSetRef.insets = new Insets(0, 5, 5, 5);
		gbc_pnlSetRef.fill = GridBagConstraints.BOTH;
		gbc_pnlSetRef.gridx = 0;
		gbc_pnlSetRef.gridy = 1;
		getContentPane().add(pnlSetRef, gbc_pnlSetRef);
		GridBagLayout gbl_pnlSetRef = new GridBagLayout();
		gbl_pnlSetRef.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_pnlSetRef.rowHeights = new int[]{0, 0};
		gbl_pnlSetRef.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlSetRef.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlSetRef.setLayout(gbl_pnlSetRef);
		
		JLabel lblRefBuild = new JLabel("Reference Build:");
		GridBagConstraints gbc_lblRefBuild = new GridBagConstraints();
		gbc_lblRefBuild.insets = new Insets(0, 0, 0, 5);
		gbc_lblRefBuild.gridx = 0;
		gbc_lblRefBuild.gridy = 0;
		pnlSetRef.add(lblRefBuild, gbc_lblRefBuild);
		
		lblRefName = new JLabel("<none>");
		GridBagConstraints gbc_lblRefName = new GridBagConstraints();
		gbc_lblRefName.insets = new Insets(0, 0, 0, 5);
		gbc_lblRefName.gridx = 1;
		gbc_lblRefName.gridy = 0;
		pnlSetRef.add(lblRefName, gbc_lblRefName);
		
		JButton btnSetReference = new JButton("Set Reference");
		GridBagConstraints gbc_btnSetReference = new GridBagConstraints();
		gbc_btnSetReference.insets = new Insets(0, 0, 0, 5);
		gbc_btnSetReference.gridx = 3;
		gbc_btnSetReference.gridy = 0;
		pnlSetRef.add(btnSetReference, gbc_btnSetReference);
		cgRefSettable.addComponent("btnSetReference", btnSetReference);
		btnSetReference.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnSetRefCallback();
			}});
		
		JButton btnClearReference = new JButton("Clear Reference");
		GridBagConstraints gbc_btnClearReference = new GridBagConstraints();
		gbc_btnClearReference.gridx = 4;
		gbc_btnClearReference.gridy = 0;
		pnlSetRef.add(btnClearReference, gbc_btnClearReference);
		btnClearReference.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnClearRefCallback();
			}});
		cgRefSettable.addComponent("btnClearReference", btnClearReference);
		
		JTabbedPane tpMain = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tpMain = new GridBagConstraints();
		gbc_tpMain.insets = new Insets(0, 5, 5, 5);
		gbc_tpMain.fill = GridBagConstraints.BOTH;
		gbc_tpMain.gridx = 0;
		gbc_tpMain.gridy = 2;
		getContentPane().add(tpMain, gbc_tpMain);
		globalEnable.addComponent("tpMain", tpMain);
		
		pnlSeq = new AbldSeqEditPanel(parent, core, readOnly);
		tpMain.addTab("Sequences", null, pnlSeq, null);
		tpMain.setEnabledAt(0, true);
		
		pnlBank = new AbldBnkEditPanel(parent, core, readOnly);
		tpMain.addTab("Soundfonts", null, pnlBank, null);
		tpMain.setEnabledAt(1, true);
		
		JPanel pnlWarc = new JPanel();
		tpMain.addTab("Samples", null, pnlWarc, null);
		tpMain.setEnabledAt(2, true);
		GridBagLayout gbl_pnlWarc = new GridBagLayout();
		gbl_pnlWarc.columnWidths = new int[]{0, 0};
		gbl_pnlWarc.rowHeights = new int[]{0, 0};
		gbl_pnlWarc.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlWarc.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		pnlWarc.setLayout(gbl_pnlWarc);
		
		tpWarc = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tpWarc = new GridBagConstraints();
		gbc_tpWarc.fill = GridBagConstraints.BOTH;
		gbc_tpWarc.gridx = 0;
		gbc_tpWarc.gridy = 0;
		pnlWarc.add(tpWarc, gbc_tpWarc);
		globalEnable.addComponent("tpWarc", tpWarc);
		
		pnlWarcMe = new WarcSlotEditPanel(core, readOnly);
		tpWarc.addTab("Build", null, pnlWarcMe, null);
		tpWarc.setEnabledAt(WARC_TAB_ME, true);
		
		pnlWarcRef = new WarcSlotEditPanel(core, readOnly);
		tpWarc.addTab("Reference", null, pnlWarcRef, null);
		tpWarc.setEnabledAt(WARC_TAB_REF, true);
		
		JPanel pnlButtons = new JPanel();
		GridBagConstraints gbc_pnlButtons = new GridBagConstraints();
		gbc_pnlButtons.insets = new Insets(0, 5, 5, 5);
		gbc_pnlButtons.fill = GridBagConstraints.BOTH;
		gbc_pnlButtons.gridx = 0;
		gbc_pnlButtons.gridy = 3;
		getContentPane().add(pnlButtons, gbc_pnlButtons);
		GridBagLayout gbl_pnlButtons = new GridBagLayout();
		gbl_pnlButtons.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_pnlButtons.rowHeights = new int[]{0, 0};
		gbl_pnlButtons.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlButtons.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlButtons.setLayout(gbl_pnlButtons);
		
		JButton btnMeta = new JButton("Edit Metadata");
		btnMeta.setVisible(false);
		btnMeta.setEnabled(false);
		GridBagConstraints gbc_btnMeta = new GridBagConstraints();
		gbc_btnMeta.insets = new Insets(0, 0, 0, 5);
		gbc_btnMeta.gridx = 0;
		gbc_btnMeta.gridy = 0;
		pnlButtons.add(btnMeta, gbc_btnMeta);
		cgEditable.addComponent("btnMeta", btnMeta);
		
		JButton btnOkay = new JButton("Okay");
		GridBagConstraints gbc_btnOkay = new GridBagConstraints();
		gbc_btnOkay.insets = new Insets(0, 0, 0, 5);
		gbc_btnOkay.gridx = 2;
		gbc_btnOkay.gridy = 0;
		pnlButtons.add(btnOkay, gbc_btnOkay);
		btnOkay.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnOkayCallback();
			}});
		cgEditable.addComponent("btnOkay", btnOkay);
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.gridx = 3;
		gbc_btnCancel.gridy = 0;
		pnlButtons.add(btnCancel, gbc_btnCancel);
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnCancelCallback();
			}});
		globalEnable.addComponent("btnCancel", btnCancel);
		reenable();
	}
	
	/*----- Getters -----*/
	
	public boolean getExitSelection(){return exitSelection;}
	
	public void loadFormInfoToAbld(AbldFile abld){
		//TODO
		if(abld == null) return;
		if(readOnly) return;
		
		//Metadata
		
		//Warcs
		
		//Fonts
		
		//Seqs
		
		
		abld.timestamp();
	}
	
	/*----- Setters -----*/
	
	public void loadAbldToForm(AbldFile abld){
		injectionMode = false;
		if(abld != null){
			//Metadata
			String n = abld.getBuildName();
			if(n == null){
				ZonedDateTime datecreated = abld.getTimeCreated();
				n = "ABLD_" + datecreated.toEpochSecond();
				abld.setBuildName(n);
			}
			txtName.setText(n);
			
			rbgTVType.setEnabledAll(true);
			switch(abld.getTVType()){
			case ZeqerRom.TV_TYPE__NTSC:
				rbgTVType.select(RBIDX_NTSC);
				break;
			case ZeqerRom.TV_TYPE__PAL:
				rbgTVType.select(RBIDX_PAL);
				break;
			case ZeqerRom.TV_TYPE__MPAL:
				rbgTVType.select(RBIDX_MPAL);
				break;
			}
			
			//Data
			pnlWarcMe.loadAbld(abld);
			pnlSeq.loadBuild(abld);
			pnlBank.loadBuild(abld);
			
			if(abld.isInjectionBuild()){
				//load ref
				
				if(core != null){
					AbldFile ref = core.getSysAbldFile(abld.getBaseRomID());
					loadRef(ref);
					injectionMode = true;
				}
				else loadRef(null);
			}
			
		}
		else{
			txtName.setText("<No build loaded>");
			
			pnlWarcMe.loadAbld(null);
			loadRef(null);
			pnlSeq.loadBuild(null);
			pnlBank.loadBuild(null);
		}
		
		reenable();
	}
	
	/*----- Drawing -----*/
	
	public void repaint(){
		super.repaint();
		lblRefName.repaint();
		globalEnable.repaint();
		pnlWarcMe.repaint();
		pnlWarcRef.repaint();
		pnlSeq.repaint();
		pnlBank.repaint();
	}
	
	public void reenable(){
		globalEnable.setEnabling(true);
		cgEditable.setEnabling(!readOnly);
		if(!readOnly) cgRefSettable.setEnabling(!injectionMode);
		if(!refLoaded){
			tpWarc.setEnabledAt(WARC_TAB_REF, false);
		}
		pnlSeq.reenable();
		pnlBank.reenable();
		repaint();
	}
	
	/*----- Internal -----*/
	
	private void loadRef(AbldFile build){
		refLoaded = false;
		if(build != null){
			lblRefName.setText(build.getBuildName());
			pnlWarcRef.loadAbld(build);
			pnlSeq.loadRef(build);
			pnlBank.loadRef(build);
			refLoaded = true;
		}
		else{
			lblRefName.setText("<none>");
			pnlWarcRef.loadAbld(null);
			pnlSeq.loadRef(null);
			pnlBank.loadRef(null);
		}
		
		repaint();
	}
	
	/*----- Callbacks -----*/
	
	private void btnOkayCallback(){
		exitSelection = true;
		super.closeMe();
	}
	
	private void btnCancelCallback(){
		exitSelection = false;
		super.closeMe();
	}
	
	private void btnSetRefCallback(){
		//TODO
		dummyCallback();
	}
	
	private void btnClearRefCallback(){
		//TODO
		dummyCallback();
	}
	
	private void rbTVSelectCallback(int index){
		rbgTVType.select(index);
		cgEditable.repaint();
	}
	
}
