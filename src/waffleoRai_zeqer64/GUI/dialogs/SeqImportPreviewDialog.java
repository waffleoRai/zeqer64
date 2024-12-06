package waffleoRai_zeqer64.GUI.dialogs;

import waffleoRai_GUITools.WRDialog;
import waffleoRai_GUITools.WriterPanel;
import waffleoRai_SeqSound.SeqVoiceCounter;
import waffleoRai_SeqSound.n64al.NUSALSeq;
import waffleoRai_zeqer64.GUI.ZeqerGUIUtils;
import waffleoRai_zeqer64.GUI.seqedit.mmlview.MMLScriptPanel;
import waffleoRai_zeqer64.GUI.seqedit.voxgraph.ChVoxChartPanel;
import waffleoRai_zeqer64.GUI.seqedit.voxgraph.TotalVoxChartPanel;
import waffleoRai_zeqer64.filefmt.seq.ZeqerSeqIO;
import waffleoRai_zeqer64.filefmt.seq.ZeqerSeqIO.SeqImportOptions;
import waffleoRai_zeqer64.filefmt.seq.ZeqerSeqIO.SeqImportResults;

import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Writer;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.border.EtchedBorder;
import javax.swing.JLabel;

import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JTabbedPane;

public class SeqImportPreviewDialog extends WRDialog{

	private static final long serialVersionUID = -2704830528504239784L;
	
	private static final int MIN_WIDTH = 640;
	private static final int MIN_HEIGHT = 550;
	
	/*--- Instance Variables ---*/
	
	private JFrame parent;
	
	private boolean exitSelection = false;
	
	private MMLScriptPanel pnlScript;
	private ChVoxChartPanel pnlChVox;
	private TotalVoxChartPanel pnlTotalVox;
	
	private WriterPanel txpWarning;
	private WriterPanel txpInfo;
	
	/*--- Initialization ---*/
	
	public SeqImportPreviewDialog(JFrame parentFrame) {
		super(parentFrame, true);
		parent = parentFrame;
		//TODO
		initGUI();
	}
	
	private void initGUI() {
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		
		setTitle("Preview Seq");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel pnlInfo = new JPanel();
		GridBagConstraints gbc_pnlInfo = new GridBagConstraints();
		gbc_pnlInfo.insets = new Insets(0, 0, 5, 5);
		gbc_pnlInfo.fill = GridBagConstraints.BOTH;
		gbc_pnlInfo.gridx = 0;
		gbc_pnlInfo.gridy = 0;
		getContentPane().add(pnlInfo, gbc_pnlInfo);
		GridBagLayout gbl_pnlInfo = new GridBagLayout();
		gbl_pnlInfo.columnWidths = new int[]{0, 0};
		gbl_pnlInfo.rowHeights = new int[]{0, 0, 0};
		gbl_pnlInfo.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlInfo.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		pnlInfo.setLayout(gbl_pnlInfo);
		
		txpInfo = new WriterPanel();
		txpInfo.setFont(new Font("Courier New", Font.PLAIN, 11));
		GridBagConstraints gbc_spInfo = new GridBagConstraints();
		gbc_spInfo.insets = new Insets(5, 5, 5, 5);
		gbc_spInfo.fill = GridBagConstraints.BOTH;
		gbc_spInfo.gridx = 0;
		gbc_spInfo.gridy = 0;
		pnlInfo.add(txpInfo, gbc_spInfo);
		globalEnable.addComponent("txpInfo", txpInfo);
		
		JPanel pnlWarn = new JPanel();
		pnlWarn.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagConstraints gbc_pnlWarn = new GridBagConstraints();
		gbc_pnlWarn.insets = new Insets(5, 5, 5, 5);
		gbc_pnlWarn.fill = GridBagConstraints.BOTH;
		gbc_pnlWarn.gridx = 0;
		gbc_pnlWarn.gridy = 1;
		pnlInfo.add(pnlWarn, gbc_pnlWarn);
		GridBagLayout gbl_pnlWarn = new GridBagLayout();
		gbl_pnlWarn.columnWidths = new int[]{0, 0};
		gbl_pnlWarn.rowHeights = new int[]{0, 0, 0};
		gbl_pnlWarn.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlWarn.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		pnlWarn.setLayout(gbl_pnlWarn);
		
		JLabel lblWarnings = new JLabel("Warnings");
		lblWarnings.setFont(new Font("Tahoma", Font.BOLD, 13));
		GridBagConstraints gbc_lblWarnings = new GridBagConstraints();
		gbc_lblWarnings.anchor = GridBagConstraints.WEST;
		gbc_lblWarnings.insets = new Insets(5, 5, 5, 0);
		gbc_lblWarnings.gridx = 0;
		gbc_lblWarnings.gridy = 0;
		pnlWarn.add(lblWarnings, gbc_lblWarnings);
		
		txpWarning = new WriterPanel();
		txpWarning.setFont(new Font("Courier New", Font.PLAIN, 11));
		GridBagConstraints gbc_spWarning = new GridBagConstraints();
		gbc_spWarning.fill = GridBagConstraints.BOTH;
		gbc_spWarning.gridx = 0;
		gbc_spWarning.gridy = 1;
		pnlWarn.add(txpWarning, gbc_spWarning);
		globalEnable.addComponent("txpWarning", txpWarning);
		
		pnlScript = new MMLScriptPanel();
		GridBagConstraints gbc_spMML = new GridBagConstraints();
		gbc_spMML.insets = new Insets(0, 0, 5, 0);
		gbc_spMML.fill = GridBagConstraints.BOTH;
		gbc_spMML.gridx = 1;
		gbc_spMML.gridy = 0;
		getContentPane().add(pnlScript, gbc_spMML);
		globalEnable.addComponent("pnlScript", pnlScript);
		
		JTabbedPane tpGraph = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tpGraph = new GridBagConstraints();
		gbc_tpGraph.gridwidth = 2;
		gbc_tpGraph.insets = new Insets(0, 5, 5, 5);
		gbc_tpGraph.fill = GridBagConstraints.BOTH;
		gbc_tpGraph.gridx = 0;
		gbc_tpGraph.gridy = 1;
		getContentPane().add(tpGraph, gbc_tpGraph);
		globalEnable.addComponent("tpGraph", tpGraph);
		
		pnlTotalVox = new TotalVoxChartPanel();
		tpGraph.addTab("Total Activity", null, pnlTotalVox, null);
		
		pnlChVox = new ChVoxChartPanel();
		tpGraph.addTab("Channel Activity", null, pnlChVox, null);
		
		JPanel pnlExit = new JPanel();
		GridBagConstraints gbc_pnlExit = new GridBagConstraints();
		gbc_pnlExit.gridwidth = 2;
		gbc_pnlExit.fill = GridBagConstraints.BOTH;
		gbc_pnlExit.gridx = 0;
		gbc_pnlExit.gridy = 2;
		getContentPane().add(pnlExit, gbc_pnlExit);
		GridBagLayout gbl_pnlExit = new GridBagLayout();
		gbl_pnlExit.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlExit.rowHeights = new int[]{0, 0};
		gbl_pnlExit.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlExit.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlExit.setLayout(gbl_pnlExit);
		
		JButton btnFinish = new JButton("Finish Import");
		GridBagConstraints gbc_btnFinish = new GridBagConstraints();
		gbc_btnFinish.insets = new Insets(5, 5, 5, 5);
		gbc_btnFinish.gridx = 1;
		gbc_btnFinish.gridy = 0;
		pnlExit.add(btnFinish, gbc_btnFinish);
		btnFinish.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnOkayCallback();
			}});
		globalEnable.addComponent("btnFinish", btnFinish);
		
		JButton btnCancel = new JButton("Cancel Import");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(5, 5, 5, 5);
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 0;
		pnlExit.add(btnCancel, gbc_btnCancel);
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnCancelCallback();
			}});
		globalEnable.addComponent("btnCancel", btnCancel);
		
		setNullResults();
	}
	
	/*--- Getters ---*/
	
	public boolean getExitSelection() {return exitSelection;}
	
	/*--- Setters ---*/
	
	private void setNullResults() {
		txpInfo.clear();
		txpWarning.clear();
		pnlScript.loadScript(null);
		//TODO Add clears for the charts.
	}
	
	public void loadInSeqImportResults(SeqImportResults res) {
		if(res == null) {
			setNullResults();
			return;
		}
		
		setWait();
		writeToInfoPanel(res);
		updateWarningsPanel(res.warningFlags);
		pnlScript.loadScript(res.seq);
		updateVoiceGraphs(res.seq);
		unsetWait();
	}
	
	/*--- Drawing ---*/
	
	private void updateVoiceGraphs(NUSALSeq seqDat) {
		if(seqDat == null) return; //TODO Clear
		SeqVoiceCounter vc = new SeqVoiceCounter();
		seqDat.playTo(vc, false);
		byte[][] chDat = vc.getVoiceUsageAllChannels();
		
		int len = 0;
		for(int i = 0; i < chDat.length; i++) {
			if(chDat[i] != null) {
				if(chDat[i].length > len) {
					len = chDat[i].length;
				}
			}
		}
		
		pnlChVox.fillXPoints(0, len, 1);
		
		int[] totals = new int[len];
		int[] data = new int[len];
		for(int i = 0; i < 16; i++) {
			Arrays.fill(data, 0);
			if((i < chDat.length) && (chDat[i] != null)) {
				for(int j = 0; j < chDat[i].length; j++) {
					data[j] = Byte.toUnsignedInt(chDat[i][j]);
					totals[j] += data[j];
				}
			}
			pnlChVox.setChannelData(i, data);
		}
		
		pnlTotalVox.loadData(totals);
	}
	
	private void updateWarningsPanel(int warningFlags) {
		txpWarning.clear();
		try{
			Writer writer = txpWarning.getWriter();
			
			if(warningFlags != 0) {
				if((warningFlags & ZeqerSeqIO.WARNING_FLAG_CHVOX) != 0) writer.write("[!] Channel voice cap exceeded [!]\n");
				if((warningFlags & ZeqerSeqIO.WARNING_FLAG_TOTVOX) != 0) writer.write("[!] Total voice cap exceeded [!]\n");
				if((warningFlags & ZeqerSeqIO.WARNING_FLAG_SIZE_TEMP) != 0) writer.write("[!] Generated binary too large for standard temp cache [!]\n");
				if((warningFlags & ZeqerSeqIO.WARNING_FLAG_SIZE_PERSIST) != 0) writer.write("[!] Generated binary too large for standard persist cache [!]\n");
				if((warningFlags & ZeqerSeqIO.WARNING_FLAG_LOSTEVENTS) != 0) writer.write("[!] Events from source could not be converted [!]\n");
				if((warningFlags & ZeqerSeqIO.WARNING_FLAG_TEMPO) != 0) writer.write("[!] Input tempo exceeded cap (225) [!]\n");
			}
			else {
				writer.write("[O] No warnings found!\n");
			}
			
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void writeToInfoPanel(SeqImportResults res) {
		SeqImportOptions ops = res.options;
		try{
			Writer writer = txpInfo.getWriter();
			writer.write("Source File: " + res.filepath + "\n");
			
			if(ops != null) {
				writer.write("Name: " + ops.name + "\n");
				writer.write("Enum Id: " + ops.eString + "\n");
				writer.write("Seq Type: " + ZeqerGUIUtils.getSeqTypeString(ops.seqType) + "\n");
				writer.write("Cache Policy: " + ZeqerGUIUtils.getCacheString(ops.cacheType) + "\n");
				if(ops.maxTotalVox > 0) {
					writer.write("Auto Voice Cap: " + ops.maxTotalVox + "\n");
				}
				if(ops.custom_timesig) {
					writer.write("Custome Time Signature: " + ops.timesig_beats + " | " + ops.timesig_div + "\n");
				}
				if(ops.bankUid != 0) {
					writer.write(String.format("Requested Soundfont ID: 0x%08x\n", ops.bankUid));
				}
				
				writer.write("Tags: ");
				if(!ops.tags.isEmpty()) {
					boolean first = true;
					for(String tag : ops.tags) {
						if(!first) writer.write(", ");
						else first = false;
						writer.write(tag);
					}
					writer.write("\n");
				}
				else writer.write("<none>\n");
			}
			
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/*--- Callbacks ---*/
	
	private void btnOkayCallback() {
		exitSelection = true;
		closeMe();
	}
	
	private void btnCancelCallback() {
		exitSelection = false;
		closeMe();
	}

}
