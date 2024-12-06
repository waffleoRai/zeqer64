package waffleoRai_zeqer64.GUI.seqedit;

import waffleoRai_GUITools.GUITools;
import waffleoRai_GUITools.WRFrame;
import waffleoRai_SeqSound.SeqVoiceCounter;
import waffleoRai_SeqSound.n64al.NUSALSeq;
import waffleoRai_zeqer64.ZeqerSeq;
import waffleoRai_zeqer64.GUI.dialogs.ZeqerSeqHubDialog;
import waffleoRai_zeqer64.GUI.seqedit.voxgraph.ChVoxChartPanel;
import waffleoRai_zeqer64.GUI.seqedit.voxgraph.TotalVoxChartPanel;

import java.awt.GridBagLayout;
import java.util.Arrays;

import javax.swing.JTabbedPane;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;

public class VoxUseViewDialog extends WRFrame{

	private static final long serialVersionUID = -3136491522660317247L;
	
	public static final int MIN_WIDTH = 400;
	public static final int MIN_HEIGHT = 300;
	
	/*--- Instance Variables ---*/
	
	private ZeqerSeqHubDialog parent;
	
	private ChVoxChartPanel pnlChannel;
	private TotalVoxChartPanel pnlTotal;
	
	/*--- Init ---*/
	
	public VoxUseViewDialog(ZeqerSeqHubDialog parentDialog) {
		parent = parentDialog;
		initGUI();
	}
	
	private void initGUI() {
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		
		setTitle("Voice Usage");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 0;
		getContentPane().add(tabbedPane, gbc_tabbedPane);
		
		pnlTotal = new TotalVoxChartPanel();
		tabbedPane.addTab("Total", null, pnlTotal, null);
		
		pnlChannel = new ChVoxChartPanel();
		tabbedPane.addTab("By Channel", null, pnlChannel, null);
	}
	
	/*--- Setters ---*/
	
	public void updateFromSeq(ZeqerSeq seq) {
		NUSALSeq seqDat = seq.getSequence();
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
		
		setLengthInTicks(len);
		
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
			loadChannelData(i, data);
		}
		
		pnlTotal.loadData(totals);
	}
	
	public void setLengthInTicks(int ticks) {
		pnlChannel.fillXPoints(0, ticks, 1);
	}
	
	public void loadChannelData(int ch, int[] data) {
		pnlChannel.setChannelData(ch, data);
	}
		
	/*--- Drawing ---*/
	
	public void showMe(Component c){
		if(c != null) setLocationRelativeTo(c);
		else{
			if(parent != null) setLocationRelativeTo(parent);
			else{
				setLocation(GUITools.getScreenCenteringCoordinates(this));
			}
		}
		
		pack();
		setVisible(true);
	}

}
