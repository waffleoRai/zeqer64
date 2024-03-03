package waffleoRai_zeqer64.GUI.abldEdit;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.WRPanel;
import waffleoRai_Utils.VoidCallbackMethod;
import waffleoRai_zeqer64.GUI.CacheTypeCombobox;
import waffleoRai_zeqer64.GUI.MediumTypeCombobox;
import waffleoRai_zeqer64.GUI.ZeqerGUIUtils;
import waffleoRai_zeqer64.GUI.ZeqerGUIUtils.CacheType;
import waffleoRai_zeqer64.GUI.ZeqerGUIUtils.MediumType;
import waffleoRai_zeqer64.filefmt.bank.BankTableEntry;
import waffleoRai_zeqer64.filefmt.seq.SeqTableEntry;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.border.EtchedBorder;
import java.awt.Color;
import javax.swing.border.LineBorder;

public class AbldSeqSlotPanel extends WRPanel{

	private static final long serialVersionUID = 8719567478492307451L;
	
	private static final int MIN_WIDTH = 300;
	private static final int MIN_HEIGHT_A = 35; //Name row only
	private static final int MIN_HEIGHT_B = 50; //Name and type rows only
	private static final int MIN_HEIGHT_C = 140; //Full, no edit buttons
	private static final int MIN_HEIGHT_D = 155; //With all rows
	
	public static final int EXPAND_LEVEL_A = 0;
	public static final int EXPAND_LEVEL_B = 1;
	public static final int EXPAND_LEVEL_C = 2;
	public static final int EXPAND_LEVEL_D = 3;
	
	/*----- Instance Variables -----*/
	
	private ZeqerCoreInterface core;
	
	private int slotId = 0;
	private SeqTableEntry loadedSeq = null;
	private List<String> bankNames;
	private CacheType cacheOverride = null;
	private MediumType medOverride = null;
	
	private int expandMode = EXPAND_LEVEL_D;
	private boolean contractFull = true; //If full, contract to A. Otherwise, B.
	private boolean readOnly = false;
	private VoidCallbackMethod updateButtonCallback; //External listener. We do NOT want to modify the table entry!
	private VoidCallbackMethod deleteButtonCallback;
	private VoidCallbackMethod sizeUpdateCallback;
	
	private ComponentGroup cgA;
	private ComponentGroup cgB;
	private ComponentGroup cgC;
	private ComponentGroup cgD;
	private ComponentGroup cgRODisable;
	
	private JButton btnExpand;
	private JLabel lblSlotId;
	private JLabel lblName;
	private JLabel lblType;
	private CacheTypeCombobox cmbxCache;
	private MediumTypeCombobox cmbxMedium;
	private JLabel lblFontList;
	
	private JLabel lblSingleWarn;
	private JPanel pnlWarnings;
	
	/*----- Init -----*/
	
	public AbldSeqSlotPanel(ZeqerCoreInterface coreIFace, boolean readOnly){
		setBorder(new LineBorder(new Color(0, 0, 0), 1, true));
		core = coreIFace;
		this.readOnly = readOnly;
		
		bankNames = new ArrayList<String>(4);
		
		cgA = globalEnable.newChild();
		cgB = globalEnable.newChild();
		cgC = globalEnable.newChild();
		cgD = globalEnable.newChild();
		cgRODisable = new ComponentGroup();
		
		initGUI();
		
		if(this.readOnly) expandMode = EXPAND_LEVEL_C;
		updateGUIInfo();

	}
	
	private void initGUI(){
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 32, 40, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
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
		gbc_lblName.insets = new Insets(5, 5, 5, 5);
		gbc_lblName.gridx = 1;
		gbc_lblName.gridy = 0;
		add(lblName, gbc_lblName);
		cgA.addComponent("lblName", lblName);
		
		lblSingleWarn = new JLabel("W!");
		GridBagConstraints gbc_lblSingleWarn = new GridBagConstraints();
		gbc_lblSingleWarn.insets = new Insets(5, 0, 5, 5);
		gbc_lblSingleWarn.gridx = 4;
		gbc_lblSingleWarn.gridy = 0;
		add(lblSingleWarn, gbc_lblSingleWarn);
		cgA.addComponent("lblSingleWarn", lblSingleWarn);
		
		btnExpand = new JButton("-");
		btnExpand.setToolTipText("Contract");
		GridBagConstraints gbc_btnExpand = new GridBagConstraints();
		gbc_btnExpand.insets = new Insets(5, 0, 5, 0);
		gbc_btnExpand.gridx = 5;
		gbc_btnExpand.gridy = 0;
		add(btnExpand, gbc_btnExpand);
		cgA.addComponent("btnExpand", btnExpand);
		btnExpand.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnExpandCallback();
			}});
		
		lblType = new JLabel("Seq Type");
		GridBagConstraints gbc_lblType = new GridBagConstraints();
		gbc_lblType.anchor = GridBagConstraints.WEST;
		gbc_lblType.gridwidth = 4;
		gbc_lblType.insets = new Insets(0, 5, 5, 5);
		gbc_lblType.gridx = 1;
		gbc_lblType.gridy = 1;
		add(lblType, gbc_lblType);
		cgB.addComponent("lblType", lblType);
		
		JLabel lblCache = new JLabel("Cache:");
		GridBagConstraints gbc_lblCache = new GridBagConstraints();
		gbc_lblCache.anchor = GridBagConstraints.EAST;
		gbc_lblCache.insets = new Insets(0, 5, 5, 5);
		gbc_lblCache.gridx = 1;
		gbc_lblCache.gridy = 2;
		add(lblCache, gbc_lblCache);
		cgC.addComponent("lblCache", lblCache);
		
		cmbxCache = new CacheTypeCombobox();
		GridBagConstraints gbc_cmbxCache = new GridBagConstraints();
		gbc_cmbxCache.gridwidth = 3;
		gbc_cmbxCache.insets = new Insets(0, 0, 5, 5);
		gbc_cmbxCache.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxCache.gridx = 2;
		gbc_cmbxCache.gridy = 2;
		add(cmbxCache, gbc_cmbxCache);
		cgC.addComponent("cmbxCache", cmbxCache);
		cgRODisable.addComponent("cmbxCache", cmbxCache);
		cmbxCache.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				cmbxCacheCallback();
			}});
		
		pnlWarnings = new JPanel();
		GridBagConstraints gbc_pnlWarnings = new GridBagConstraints();
		gbc_pnlWarnings.gridheight = 3;
		gbc_pnlWarnings.insets = new Insets(0, 5, 5, 5);
		gbc_pnlWarnings.fill = GridBagConstraints.BOTH;
		gbc_pnlWarnings.gridx = 5;
		gbc_pnlWarnings.gridy = 2;
		add(pnlWarnings, gbc_pnlWarnings);
		pnlWarnings.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		cgC.addComponent("pnlWarnings", pnlWarnings);
		
		JLabel lblMedium = new JLabel("Medium:");
		GridBagConstraints gbc_lblMedium = new GridBagConstraints();
		gbc_lblMedium.anchor = GridBagConstraints.EAST;
		gbc_lblMedium.insets = new Insets(0, 5, 5, 5);
		gbc_lblMedium.gridx = 1;
		gbc_lblMedium.gridy = 3;
		add(lblMedium, gbc_lblMedium);
		cgC.addComponent("lblMedium", lblMedium);
		
		cmbxMedium = new MediumTypeCombobox();
		GridBagConstraints gbc_cmbxMedium = new GridBagConstraints();
		gbc_cmbxMedium.gridwidth = 3;
		gbc_cmbxMedium.insets = new Insets(0, 0, 5, 5);
		gbc_cmbxMedium.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxMedium.gridx = 2;
		gbc_cmbxMedium.gridy = 3;
		add(cmbxMedium, gbc_cmbxMedium);
		cgC.addComponent("cmbxMedium", cmbxMedium);
		cgRODisable.addComponent("cmbxMedium", cmbxMedium);
		cmbxMedium.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				cmbxMedCallback();
			}});
		
		JLabel lblFonts = new JLabel("Fonts:");
		GridBagConstraints gbc_lblFonts = new GridBagConstraints();
		gbc_lblFonts.anchor = GridBagConstraints.EAST;
		gbc_lblFonts.insets = new Insets(0, 5, 5, 5);
		gbc_lblFonts.gridx = 1;
		gbc_lblFonts.gridy = 4;
		add(lblFonts, gbc_lblFonts);
		cgC.addComponent("lblFonts", lblFonts);
		
		lblFontList = new JLabel("My Favorite Font");
		GridBagConstraints gbc_lblFontList = new GridBagConstraints();
		gbc_lblFontList.gridwidth = 3;
		gbc_lblFontList.anchor = GridBagConstraints.WEST;
		gbc_lblFontList.insets = new Insets(0, 0, 5, 5);
		gbc_lblFontList.gridx = 2;
		gbc_lblFontList.gridy = 4;
		add(lblFontList, gbc_lblFontList);
		cgC.addComponent("lblFontList", lblFontList);
		
		JPanel pnlEditBtn = new JPanel();
		pnlEditBtn.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlEditBtn = new GridBagConstraints();
		gbc_pnlEditBtn.gridwidth = 4;
		gbc_pnlEditBtn.insets = new Insets(0, 0, 5, 5);
		gbc_pnlEditBtn.fill = GridBagConstraints.BOTH;
		gbc_pnlEditBtn.gridx = 1;
		gbc_pnlEditBtn.gridy = 5;
		add(pnlEditBtn, gbc_pnlEditBtn);
		GridBagLayout gbl_pnlEditBtn = new GridBagLayout();
		gbl_pnlEditBtn.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_pnlEditBtn.rowHeights = new int[]{0, 0};
		gbl_pnlEditBtn.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlEditBtn.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlEditBtn.setLayout(gbl_pnlEditBtn);
		cgD.addComponent("pnlEditBtn", pnlEditBtn); //To hide it.
		
		JButton btnSet = new JButton("S");
		btnSet.setToolTipText("Set sequence");
		GridBagConstraints gbc_btnSet = new GridBagConstraints();
		gbc_btnSet.insets = new Insets(5, 5, 5, 5);
		gbc_btnSet.gridx = 0;
		gbc_btnSet.gridy = 0;
		pnlEditBtn.add(btnSet, gbc_btnSet);
		cgD.addComponent("btnSet", btnSet);
		cgRODisable.addComponent("btnSet", btnSet);
		btnSet.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnSetCallback();
			}});
		
		JButton btnUpdate = new JButton("U");
		btnUpdate.setToolTipText("Apply update");
		GridBagConstraints gbc_btnUpdate = new GridBagConstraints();
		gbc_btnUpdate.insets = new Insets(5, 0, 5, 5);
		gbc_btnUpdate.gridx = 1;
		gbc_btnUpdate.gridy = 0;
		pnlEditBtn.add(btnUpdate, gbc_btnUpdate);
		cgD.addComponent("btnUpdate", btnUpdate);
		cgRODisable.addComponent("btnUpdate", btnUpdate);
		btnUpdate.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(updateButtonCallback != null){
					updateButtonCallback.doMethod();
				}
			}});
		
		JButton btnDelete = new JButton("D");
		btnDelete.setToolTipText("Delete slot");
		GridBagConstraints gbc_btnDelete = new GridBagConstraints();
		gbc_btnDelete.insets = new Insets(5, 0, 5, 5);
		gbc_btnDelete.gridx = 3;
		gbc_btnDelete.gridy = 0;
		pnlEditBtn.add(btnDelete, gbc_btnDelete);
		cgD.addComponent("btnDelete", btnDelete);
		cgRODisable.addComponent("btnDelete", btnDelete);
		btnDelete.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(deleteButtonCallback != null){
					deleteButtonCallback.doMethod();
				}
			}});

	}
	
	/*----- Getters -----*/
	
	public CacheType getCacheOverride(){return cacheOverride;}
	public MediumType getMediumOverride(){return medOverride;}
	
	/*----- Setters -----*/

	public void setUpdateButtonCallback(VoidCallbackMethod func){updateButtonCallback = func;}
	public void setDeleteButtonCallback(VoidCallbackMethod func){deleteButtonCallback = func;}
	public void setSizeUpdateCallback(VoidCallbackMethod func){sizeUpdateCallback = func;}
	
	public void setCacheOverride(int val){
		cacheOverride = cmbxCache.getItemAt(CacheTypeCombobox.CACHE_CMBX_IDXS[val]);
		updateGUIInfo();
	}
	
	public void setMediumOverride(int val){
		medOverride = cmbxMedium.getItemAt(MediumTypeCombobox.MEDIUM_CMBX_IDXS[val]);
		updateGUIInfo();
	}
	
	public void setContractFull(boolean b){
		contractFull = b;
		contract();
	}
	
	public void setSlotId(int value){
		slotId = value;
		lblSlotId.setText(String.format("%03d", slotId));
		globalEnable.repaint();
	}
	
	public void setSeq(SeqTableEntry seqmeta){
		bankNames.clear();
		loadedSeq = seqmeta;
		cacheOverride = null;
		medOverride = null;
		updateGUIInfo();
	}
	
	/*----- Misc. Internal -----*/
	
	private void updateBankNames(){
		bankNames.clear();
		if(loadedSeq != null){
			int bcount = loadedSeq.getBankCount();
			if(bcount == 1){
				int buid = loadedSeq.getPrimaryBankUID();
				String bname = String.format("(Bank %08x)", buid);
				if(core != null){
					BankTableEntry bmeta = core.getBankInfo(buid);
					if(bmeta != null){
						String s = bmeta.getName();
						if(s != null && !s.isEmpty()) bname = s;
					}
				}
				bankNames.add(bname);
			}
			else if(bcount > 1){
				List<Integer> buids = loadedSeq.getLinkedBankUIDs();
				for(Integer buid : buids){
					String bname = String.format("(Bank %08x)", buid);
					if(core != null){
						BankTableEntry bmeta = core.getBankInfo(buid);
						if(bmeta != null){
							String s = bmeta.getName();
							if(s != null && !s.isEmpty()) bname = s;
						}
					}
					bankNames.add(bname);
				}
			}
		}
	}
	
	/*----- Draw -----*/
	
	public void updateGUIInfo(){
		//TODO Update this function when warnings are added.
		
		//Sync GUI to loadedSeq
		lblSlotId.setText(String.format("%03d", slotId));
		if(loadedSeq != null){
			String name = loadedSeq.getName();
			if(name == null){
				name = String.format("[Seq %08x]", loadedSeq.getUID());
			}
			lblName.setText(name);
			lblType.setText(ZeqerGUIUtils.getSeqTypeString(loadedSeq.getSeqType()));
			
			if(cacheOverride != null) cmbxCache.setSelectedItem(cacheOverride);
			else cmbxCache.setRawValue(loadedSeq.getCache());
			
			if(medOverride != null) cmbxMedium.setSelectedItem(medOverride);
			else cmbxMedium.setRawValue(loadedSeq.getMedium());
			
			//Fonts
			if(bankNames.isEmpty()) updateBankNames();
			
			if(!bankNames.isEmpty()){
				String ss = "";
				boolean first = true;
				for(String s : bankNames){
					if(!first) ss += ", ";
					else first = false;
					ss += s;
				}
				lblFontList.setText(ss);
			}
			else lblFontList.setText("None");
			
		}
		else{
			lblName.setText("<null>");
			lblType.setText("No sequence");
			lblFontList.setText("None");
		}
		
		reenable();
	}
	
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
		
		if(expandMode >= EXPAND_LEVEL_D){
			cgD.setEnabling(true);
		}
		else cgD.setEnabling(false);
		
		cgRODisable.setEnabling(!readOnly && (loadedSeq != null));
		
		globalEnable.repaint();
	}
	
	private void setStateA(){
		cgB.setEnabling(false); 
		cgB.setVisible(false);
		cgC.setEnabling(false); 
		cgC.setVisible(false);
		cgD.setEnabling(false); 
		cgD.setVisible(false);
		
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT_A));
		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT_A));
		
		cgRODisable.setEnabling(!readOnly && (loadedSeq != null));
		globalEnable.repaint();
		expandMode = EXPAND_LEVEL_A;
	}
	
	private void setStateB(){
		cgB.setEnabling(true); 
		cgB.setVisible(true);
		cgC.setEnabling(false); 
		cgC.setVisible(false);
		cgD.setEnabling(false); 
		cgD.setVisible(false);
		
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT_B));
		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT_B));
		
		cgRODisable.setEnabling(!readOnly && (loadedSeq != null));
		globalEnable.repaint();
		expandMode = EXPAND_LEVEL_B;
	}
	
	private void setStateC(){
		cgB.setEnabling(true); 
		cgB.setVisible(true);
		cgC.setEnabling(true); 
		cgC.setVisible(true);
		cgD.setEnabling(false); 
		cgD.setVisible(false);
		
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT_C));
		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT_C));
		
		cgRODisable.setEnabling(!readOnly && (loadedSeq != null));
		globalEnable.repaint();
		expandMode = EXPAND_LEVEL_C;
	}
	
	private void setStateD(){
		cgB.setEnabling(true); 
		cgB.setVisible(true);
		cgC.setEnabling(true); 
		cgC.setVisible(true);
		cgD.setEnabling(true); 
		cgD.setVisible(true);
		
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT_D));
		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT_D));
		
		cgRODisable.setEnabling(!readOnly && (loadedSeq != null));
		globalEnable.repaint();
		expandMode = EXPAND_LEVEL_D;
	}
	
	public void expand(){
		if(readOnly) setStateC();
		else setStateD();
		btnExpand.setText("-");
		btnExpand.repaint();
		
		if(sizeUpdateCallback != null) sizeUpdateCallback.doMethod();
	}
	
	public void contract(){
		if(contractFull) setStateA();
		else setStateB();
		btnExpand.setText("+");
		btnExpand.repaint();
		
		if(sizeUpdateCallback != null) sizeUpdateCallback.doMethod();
	}
	
	/*----- Callbacks -----*/
	
	private void cmbxMedCallback(){
		int selidx = cmbxMedium.getSelectedIndex();
		if(selidx >= 0){
			medOverride = cmbxMedium.getItemAt(selidx);
		}
	}
	
	private void cmbxCacheCallback(){
		int selidx = cmbxCache.getSelectedIndex();
		if(selidx >= 0){
			cacheOverride = cmbxCache.getItemAt(selidx);
		}
	}
	
	private void btnSetCallback(){
		//TODO
		dummyCallback();
	}
	
	private void btnExpandCallback(){
		switch(expandMode){
		case EXPAND_LEVEL_A:
		case EXPAND_LEVEL_B:
			expand();
			break;
		case EXPAND_LEVEL_C:
		case EXPAND_LEVEL_D:
			contract();
			break;
		}
	}
	
}
