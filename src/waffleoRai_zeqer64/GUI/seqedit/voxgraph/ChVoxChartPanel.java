package waffleoRai_zeqer64.GUI.seqedit.voxgraph;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;

import javax.swing.JScrollPane;

import java.awt.GridBagConstraints;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;

public class ChVoxChartPanel extends JPanel{

	private static final long serialVersionUID = 2186182163545306930L;
	
	/*--- Instance Variables ---*/
	
	private int ySize = 25;
	//private double xZoom = 1.0;
	
	private XAxisLabel lblTimeline;
	private JPanel pnlLabels;
	private JPanel pnlData;
	
	private JLabel[] chLabels;
	private OneDimHeatMap[] dataPanels;
	
	/*--- Initialization ---*/
	
	public ChVoxChartPanel() {
		chLabels = new JLabel[16];
		dataPanels = new OneDimHeatMap[16];
		initGUI();
	}
	
	private void initGUI() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JScrollPane spData = new JScrollPane();
		spData.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spData = new GridBagConstraints();
		gbc_spData.fill = GridBagConstraints.BOTH;
		gbc_spData.gridx = 0;
		gbc_spData.gridy = 0;
		add(spData, gbc_spData);
		
		pnlLabels = new JPanel();
		spData.setRowHeaderView(pnlLabels);
		GridBagLayout gbl_pnlLabels = new GridBagLayout();
		gbl_pnlLabels.columnWidths = new int[]{100, 0};
		gbl_pnlLabels.rowHeights = new int[]{0};
		gbl_pnlLabels.columnWeights = new double[]{Double.MIN_VALUE, Double.MIN_VALUE};
		gbl_pnlLabels.rowWeights = new double[]{Double.MIN_VALUE};
		pnlLabels.setLayout(gbl_pnlLabels);
		
		lblTimeline = new XAxisLabel();
		spData.setColumnHeaderView(lblTimeline);
		
		pnlData = new JPanel();
		spData.setViewportView(pnlData);
		GridBagLayout gbl_pnlData = new GridBagLayout();
		gbl_pnlData.columnWidths = new int[]{0};
		gbl_pnlData.rowHeights = new int[]{0};
		gbl_pnlData.columnWeights = new double[]{1.0};
		gbl_pnlData.rowWeights = new double[]{Double.MIN_VALUE};
		pnlData.setLayout(gbl_pnlData);
		
		populateRows(gbl_pnlLabels, gbl_pnlData);
	}
	
	private void populateRows(GridBagLayout gbl_pnlLabels, GridBagLayout gbl_pnlData) {
		GridBagConstraints gbc = null;
		
		gbl_pnlLabels.rowHeights = new int[16];
		for(int i = 0; i < 16; i++) gbl_pnlLabels.rowHeights[i] = ySize+2;
		gbl_pnlLabels.rowWeights = new double[16];
		Arrays.fill(gbl_pnlLabels.rowWeights, Double.MIN_VALUE);
		
		for(int i = 0; i < 16; i++) {
			chLabels[i] = new JLabel(String.format("Channel %02d", i));
			chLabels[i].setHorizontalAlignment(JLabel.CENTER);
			chLabels[i].setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
			gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridx = 0;
			gbc.gridy = i;
			if(i > 0) {
				gbc.insets = new Insets(2,0,0,0);
			}
			pnlLabels.add(chLabels[i], gbc);
		}
		
		gbl_pnlData.rowHeights = new int[16];
		for(int i = 0; i < 16; i++) gbl_pnlData.rowHeights[i] = ySize;
		gbl_pnlData.rowWeights = new double[16];
		Arrays.fill(gbl_pnlData.rowWeights, Double.MIN_VALUE);
		
		for(int i = 0; i < 16; i++) {
			dataPanels[i] = new OneDimHeatMap();
			dataPanels[i].setMinYSize(ySize);
			dataPanels[i].setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			gbc = new GridBagConstraints();
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridy = i;
			if(i > 0) {
				gbc.insets = new Insets(2,0,0,0);
			}
			pnlData.add(dataPanels[i], gbc);
		}
	}
	
	/*--- Getters ---*/
	
	/*--- Setters ---*/
	
	public void fillXPoints(int min, int max, int incr) {
		int est = max - min;
		est /= incr;
		est++;
		double[] x = new double[est];
		int j = 0;
		for(int i = min; i <= max; i+=incr) {
			x[j++] = (double)i;
		}
		
		double xMin = x[0];
		double xMax = x[x.length-1];
		
		lblTimeline.setXRange(xMin, xMax);
		for(int i = 0; i < 16; i++) {
			dataPanels[i].setXRange(xMin, xMax);
			dataPanels[i].setXValues(x);
		}
	}
	
	public void setXPoints(double[] x) {
		if(x == null) return;
		
		double xMin = Double.NaN;
		double xMax = Double.NaN;
		
		for(int i = 0; i < x.length; i++) {
			if(xMin == Double.NaN || x[i] < xMin) xMin = x[i];
			if(xMax == Double.NaN || x[i] > xMax) xMax = x[i];
		}
		
		lblTimeline.setXRange(xMin, xMax);
		for(int i = 0; i < 16; i++) {
			dataPanels[i].setXRange(xMin, xMax);
			dataPanels[i].setXValues(x);
		}
	}
	
	public void setChannelData(int ch, int[] y) {
		if(ch < 0 || ch >= 16) return;
		dataPanels[ch].setLevelValues(y);
	}
	
	/*--- Drawing ---*/
	
	/*--- Callbacks ---*/

}
