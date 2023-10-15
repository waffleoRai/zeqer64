package waffleoRai_zeqer64.GUI.abldEdit;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.WRPanel;
import waffleoRai_Utils.VoidCallbackMethod;
import waffleoRai_zeqer64.GUI.CacheTypeCombobox;
import waffleoRai_zeqer64.GUI.MediumTypeCombobox;
import waffleoRai_zeqer64.GUI.ZeqerGUIUtils.CacheType;
import waffleoRai_zeqer64.GUI.ZeqerGUIUtils.MediumType;
import waffleoRai_zeqer64.filefmt.ZeqerBankTable.BankTableEntry;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;
import java.awt.GridBagLayout;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.awt.Font;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Dimension;

public class AbldBnkSlotPanel extends WRPanel{

	private static final long serialVersionUID = 8249214050736962469L;

	private static final int MIN_WIDTH = 300;
	private static final int MIN_HEIGHT_A = 35; //Name row only
	private static final int MIN_HEIGHT_B = 140; //Full, no edit buttons
	private static final int MIN_HEIGHT_C = 155; //With all rows
	
	public static final int EXPAND_LEVEL_A = 0;
	public static final int EXPAND_LEVEL_B = 1;
	public static final int EXPAND_LEVEL_C = 2;
	
	/*----- Instance Variables -----*/
	
	private ZeqerCoreInterface core;
	
	private int slotId = 0;
	private int bankUID = 0;
	
	private int expandMode = EXPAND_LEVEL_C;
	private boolean readOnly = false;
	
	private int warcCmbxLen = 0;
	private VoidCallbackMethod warcChangeCallback;
	private VoidCallbackMethod deleteButtonCallback;
	private VoidCallbackMethod sizeUpdateCallback;

	private ComponentGroup cgA;
	private ComponentGroup cgB;
	private ComponentGroup cgC;
	private ComponentGroup cgRODisable;
	
	private JLabel lblSlotId;
	private JLabel lblName;
	private JLabel lblSingleWarn;
	private JButton btnExpand;
	private CacheTypeCombobox cmbxCache;
	private MediumTypeCombobox cmbxMedium;
	private JComboBox<String> cmbxWarc;
	
	/*----- Init -----*/
	
	public AbldBnkSlotPanel(ZeqerCoreInterface coreIFace, boolean readOnly){
		core = coreIFace;
		this.readOnly = readOnly;
		
		cgA = globalEnable.newChild();
		cgB = globalEnable.newChild();
		cgC = globalEnable.newChild();
		cgRODisable = new ComponentGroup();
		
		initGUI();
		contract();
	}
	
	private void initGUI(){
		setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		lblSlotId = new JLabel("000");
		lblSlotId.setFont(new Font("Tahoma", Font.BOLD, 14));
		GridBagConstraints gbc_lblSlotId = new GridBagConstraints();
		gbc_lblSlotId.insets = new Insets(5, 5, 5, 5);
		gbc_lblSlotId.gridx = 0;
		gbc_lblSlotId.gridy = 0;
		add(lblSlotId, gbc_lblSlotId);
		cgA.addComponent("lblSlotId", lblSlotId);
		
		lblName = new JLabel("Name");
		lblName.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.WEST;
		gbc_lblName.gridwidth = 3;
		gbc_lblName.insets = new Insets(5, 0, 5, 5);
		gbc_lblName.gridx = 1;
		gbc_lblName.gridy = 0;
		add(lblName, gbc_lblName);
		cgA.addComponent("lblName", lblName);
		
		lblSingleWarn = new JLabel("W!");
		GridBagConstraints gbc_lblSingleWarn = new GridBagConstraints();
		gbc_lblSingleWarn.insets = new Insets(0, 0, 5, 5);
		gbc_lblSingleWarn.gridx = 4;
		gbc_lblSingleWarn.gridy = 0;
		add(lblSingleWarn, gbc_lblSingleWarn);
		cgA.addComponent("lblSingleWarn", lblSingleWarn);
		
		btnExpand = new JButton("-");
		GridBagConstraints gbc_btnExpand = new GridBagConstraints();
		gbc_btnExpand.insets = new Insets(5, 0, 5, 5);
		gbc_btnExpand.gridx = 5;
		gbc_btnExpand.gridy = 0;
		add(btnExpand, gbc_btnExpand);
		cgA.addComponent("btnExpand", btnExpand);
		btnExpand.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnExpandCallback();
			}});
		
		JLabel lblCache = new JLabel("Cache:");
		GridBagConstraints gbc_lblCache = new GridBagConstraints();
		gbc_lblCache.anchor = GridBagConstraints.EAST;
		gbc_lblCache.insets = new Insets(0, 0, 5, 5);
		gbc_lblCache.gridx = 1;
		gbc_lblCache.gridy = 1;
		add(lblCache, gbc_lblCache);
		cgB.addComponent("lblCache", lblCache);
		
		cmbxCache = new CacheTypeCombobox();
		GridBagConstraints gbc_cmbxCache = new GridBagConstraints();
		gbc_cmbxCache.gridwidth = 2;
		gbc_cmbxCache.insets = new Insets(0, 0, 5, 5);
		gbc_cmbxCache.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxCache.gridx = 2;
		gbc_cmbxCache.gridy = 1;
		add(cmbxCache, gbc_cmbxCache);
		cgB.addComponent("cmbxCache", cmbxCache);
		cgRODisable.addComponent("cmbxCache", cmbxCache);
		
		JLabel lblMedium = new JLabel("Medium:");
		GridBagConstraints gbc_lblMedium = new GridBagConstraints();
		gbc_lblMedium.anchor = GridBagConstraints.EAST;
		gbc_lblMedium.insets = new Insets(0, 0, 5, 5);
		gbc_lblMedium.gridx = 1;
		gbc_lblMedium.gridy = 2;
		add(lblMedium, gbc_lblMedium);
		cgB.addComponent("lblMedium", lblMedium);
		
		cmbxMedium = new MediumTypeCombobox();
		GridBagConstraints gbc_cmbxMedium = new GridBagConstraints();
		gbc_cmbxMedium.gridwidth = 2;
		gbc_cmbxMedium.insets = new Insets(0, 0, 5, 5);
		gbc_cmbxMedium.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxMedium.gridx = 2;
		gbc_cmbxMedium.gridy = 2;
		add(cmbxMedium, gbc_cmbxMedium);
		cgB.addComponent("cmbxMedium", cmbxMedium);
		cgRODisable.addComponent("cmbxMedium", cmbxMedium);
		
		JPanel pnlWarnings = new JPanel();
		GridBagConstraints gbc_pnlWarnings = new GridBagConstraints();
		gbc_pnlWarnings.gridheight = 2;
		gbc_pnlWarnings.insets = new Insets(0, 0, 5, 5);
		gbc_pnlWarnings.fill = GridBagConstraints.BOTH;
		gbc_pnlWarnings.gridx = 5;
		gbc_pnlWarnings.gridy = 2;
		add(pnlWarnings, gbc_pnlWarnings);
		cgB.addComponent("pnlWarnings", pnlWarnings);
		
		JLabel lblSampleSource = new JLabel("Sample Source:");
		GridBagConstraints gbc_lblSampleSource = new GridBagConstraints();
		gbc_lblSampleSource.anchor = GridBagConstraints.EAST;
		gbc_lblSampleSource.insets = new Insets(0, 0, 5, 5);
		gbc_lblSampleSource.gridx = 1;
		gbc_lblSampleSource.gridy = 3;
		add(lblSampleSource, gbc_lblSampleSource);
		cgB.addComponent("lblSampleSource", lblSampleSource);
		
		cmbxWarc = new JComboBox<String>();
		GridBagConstraints gbc_cmbxWarc = new GridBagConstraints();
		gbc_cmbxWarc.gridwidth = 2;
		gbc_cmbxWarc.insets = new Insets(0, 0, 5, 5);
		gbc_cmbxWarc.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxWarc.gridx = 2;
		gbc_cmbxWarc.gridy = 3;
		add(cmbxWarc, gbc_cmbxWarc);
		cgB.addComponent("cmbxWarc", cmbxWarc);
		cgRODisable.addComponent("cmbxWarc", cmbxWarc);
		cmbxWarc.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(warcChangeCallback != null){
					warcChangeCallback.doMethod();
				}
			}
		});
		
		JPanel pnlEditBtn = new JPanel();
		pnlEditBtn.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlEditBtn = new GridBagConstraints();
		gbc_pnlEditBtn.gridwidth = 4;
		gbc_pnlEditBtn.insets = new Insets(0, 0, 5, 5);
		gbc_pnlEditBtn.fill = GridBagConstraints.BOTH;
		gbc_pnlEditBtn.gridx = 1;
		gbc_pnlEditBtn.gridy = 4;
		add(pnlEditBtn, gbc_pnlEditBtn);
		GridBagLayout gbl_pnlEditBtn = new GridBagLayout();
		gbl_pnlEditBtn.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlEditBtn.rowHeights = new int[]{0, 0};
		gbl_pnlEditBtn.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlEditBtn.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlEditBtn.setLayout(gbl_pnlEditBtn);
		cgC.addComponent("pnlEditBtn", pnlEditBtn);
		
		JButton btnS = new JButton("S");
		GridBagConstraints gbc_btnS = new GridBagConstraints();
		gbc_btnS.insets = new Insets(5, 5, 5, 5);
		gbc_btnS.gridx = 0;
		gbc_btnS.gridy = 0;
		pnlEditBtn.add(btnS, gbc_btnS);
		cgC.addComponent("btnS", btnS);
		cgRODisable.addComponent("btnS", btnS);
		btnS.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnSetCallback();
			}});
		
		JButton btnD = new JButton("D");
		GridBagConstraints gbc_btnD = new GridBagConstraints();
		gbc_btnD.insets = new Insets(5, 0, 5, 5);
		gbc_btnD.gridx = 2;
		gbc_btnD.gridy = 0;
		pnlEditBtn.add(btnD, gbc_btnD);
		cgC.addComponent("btnD", btnD);
		cgRODisable.addComponent("btnD", btnD);
		btnD.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(deleteButtonCallback != null){
					deleteButtonCallback.doMethod();
				}
			}});
	}
	
	/*----- Getters -----*/
	
	public CacheType getCacheOverride(){return cmbxCache.getItemAt(cmbxCache.getSelectedIndex());}
	public MediumType getMediumOverride(){return cmbxMedium.getItemAt(cmbxMedium.getSelectedIndex());}
	public int getBankUID(){return bankUID;}
	public int getWaveArcIndex(){return cmbxWarc.getSelectedIndex();}
	
	/*----- Setters -----*/
	
	public void setDeleteButtonCallback(VoidCallbackMethod func){deleteButtonCallback = func;}
	public void setSizeUpdateCallback(VoidCallbackMethod func){sizeUpdateCallback = func;}
	
	public void setSlotId(int value){
		slotId = value;
		lblSlotId.setText(String.format("%03d", slotId));
		lblSlotId.repaint();
	}
	
	public void loadWaveArcNames(List<String> names){
		int sel = cmbxWarc.getSelectedIndex();
		DefaultComboBoxModel<String> mdl = new DefaultComboBoxModel<String>();
		if(names != null){
			for(String n : names) mdl.addElement(n);
			warcCmbxLen = names.size();
		}
		else warcCmbxLen = 0;
		cmbxWarc.setModel(mdl);
		if(sel < warcCmbxLen) cmbxWarc.setSelectedIndex(sel);
		else cmbxWarc.setSelectedIndex(0);
		
		cmbxWarc.repaint();
	}
	
	public void setBank(BankTableEntry meta){
		if(meta != null){
			bankUID = meta.getUID();
			String n = meta.getName();
			if(n == null){
				n = String.format("BNK_%08x", meta.getUID());
				meta.setName(n);
			}
			lblName.setText(n);
			cmbxCache.setRawValue(meta.getCachePolicy());
			cmbxMedium.setRawValue(meta.getMedium());
			setWaveArcIndex(meta.getWarcIndex());
		}
		else{
			bankUID = 0;
			lblName.setText("<empty>");
			setWaveArcIndex(0);
		}
		
		reenable();
	}
	
	public void setCacheOverride(int val){
		cmbxCache.setRawValue(val);
		cmbxCache.repaint();
	}
	
	public void setMediumOverride(int val){
		cmbxMedium.setRawValue(val);
		cmbxMedium.repaint();
	}
	
	public int setWaveArcIndex(int val){
		if(val < 0) return -1;
		if(val >= warcCmbxLen) return -1;
		cmbxWarc.setSelectedIndex(val);
		cmbxWarc.repaint();
		return cmbxWarc.getSelectedIndex();
	}
	
	/*----- Misc. Internal -----*/
	
	/*----- Draw -----*/
	
	public void reenable(){
		cgA.setEnabling(true);
		
		if(expandMode >= EXPAND_LEVEL_B){
			cgB.setEnabling(true);
		}
		else cgB.setEnabling(false);
		
		if(expandMode >= EXPAND_LEVEL_C){
			cgC.setEnabling(true);
		}
		else cgC.setEnabling(false);
		
		cgRODisable.setEnabling(!readOnly && (bankUID != 0));
		if(!readOnly){
			cmbxWarc.setEnabled(warcCmbxLen > 0);
		}
		
		globalEnable.repaint();
	}
	
	private void setStateA(){
		cgB.setEnabling(false); 
		cgB.setVisible(false);
		cgC.setEnabling(false); 
		cgC.setVisible(false);
		
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT_A));
		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT_A));
		
		cgRODisable.setEnabling(!readOnly && (bankUID != 0));
		globalEnable.repaint();
		expandMode = EXPAND_LEVEL_A;
	}
	
	private void setStateB(){
		cgB.setEnabling(true); 
		cgB.setVisible(true);
		cgC.setEnabling(false); 
		cgC.setVisible(false);
		
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT_B));
		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT_B));
		
		cgRODisable.setEnabling(!readOnly && (bankUID != 0));
		globalEnable.repaint();
		expandMode = EXPAND_LEVEL_B;
	}
	
	private void setStateC(){
		cgB.setEnabling(true); 
		cgB.setVisible(true);
		cgC.setEnabling(true); 
		cgC.setVisible(true);
		
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT_C));
		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT_C));
		
		cgRODisable.setEnabling(!readOnly && (bankUID != 0));
		globalEnable.repaint();
		expandMode = EXPAND_LEVEL_C;
	}
	
	public void expand(){
		if(readOnly) setStateB();
		else setStateC();
		btnExpand.setText("-");
		btnExpand.repaint();
		
		if(sizeUpdateCallback != null) sizeUpdateCallback.doMethod();
	}
	
	public void contract(){
		setStateA();
		btnExpand.setText("+");
		btnExpand.repaint();
		
		if(sizeUpdateCallback != null) sizeUpdateCallback.doMethod();
	}
	
	/*----- Callbacks -----*/
	
	private void btnExpandCallback(){
		if(expandMode == EXPAND_LEVEL_A) expand();
		else contract();
	}
	
	private void btnSetCallback(){
		//TODO
		dummyCallback();
	}
	
}
