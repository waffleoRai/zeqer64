package waffleoRai_zeqer64.GUI.seqedit.voxgraph;

import java.awt.BasicStroke;
import java.awt.Color;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYDataset;

public class TotalVoxChartPanel extends ChartPanel {
	
	/*--- Constants ---*/

	private static final long serialVersionUID = 4409172050724137859L;
	
	private static final String SERIES_KEY_PLOT = "VoicePlot";
	private static final String SERIES_KEY_WHI = "WarnHi";
	private static final String SERIES_KEY_WLO = "WarnLo";
	
	private static final int SERIES_IDX_PLOT = 0;
	private static final int SERIES_IDX_WHI = 2;
	private static final int SERIES_IDX_WLO = 1;
	
	/*--- Instance Variables ---*/
	
	//--- Data
	private int[] yData;
	private int voxWarnLow = 17;
	private int voxWarnHigh = 24;
	
	private DefaultXYDataset dataset;
	private XYPlot xyPlot;
	private JFreeChart chart;
	
	//--- Aesthetics
	private Color colorPlot = Color.BLUE;
	private Color colorWarnHi = Color.RED;
	private Color colorWarnLo = Color.ORANGE;
	private Color colorBkg = Color.WHITE;
	private Color colorGridline = Color.BLACK;
	
	private float lineWidthPlot = 2.5f;
	private float lineWidthWarnHi = 1.5f;
	private float lineWidthWarnLo = 1.5f;
	
	private String xLabel = "Time (Ticks)";
	private String yLabel = "Total Voices";
	
	/*--- Init ---*/
	
	public TotalVoxChartPanel() {
		super(null);
	}
	
	private void updateChartAppearance() {
		//TODO XY axis labels? How to update those?
		xyPlot.setBackgroundPaint(colorBkg);
		xyPlot.getRenderer().setSeriesPaint(SERIES_IDX_PLOT, colorPlot);
		xyPlot.getRenderer().setSeriesStroke(SERIES_IDX_PLOT, new BasicStroke(lineWidthPlot));
		xyPlot.setRangeGridlinesVisible(true);
		xyPlot.setRangeGridlinePaint(colorGridline); 
		xyPlot.setDomainGridlinesVisible(true);
		xyPlot.setDomainGridlinePaint(colorGridline);
		
		if(voxWarnLow > 0) {
			xyPlot.getRenderer().setSeriesPaint(SERIES_IDX_WLO, colorWarnLo);
			xyPlot.getRenderer().setSeriesStroke(SERIES_IDX_WLO, new BasicStroke(lineWidthWarnLo));
			if(voxWarnHigh > 0) {
				xyPlot.getRenderer().setSeriesPaint(SERIES_IDX_WHI, colorWarnHi);
				xyPlot.getRenderer().setSeriesStroke(SERIES_IDX_WHI, new BasicStroke(lineWidthWarnHi));
			}
		}
	}
	
	private void updateChart() {
		if(dataset != null) {
			dataset.removeSeries(SERIES_KEY_PLOT);
			dataset.removeSeries(SERIES_KEY_WHI);
			dataset.removeSeries(SERIES_KEY_WLO);
		}
		else dataset = new DefaultXYDataset();
		
		if(yData != null) {
			double[][] serdat = new double[2][yData.length];
			for(int i = 0; i < yData.length; i++) {
				serdat[0][i] = (double)i;
				serdat[1][i] = (double)yData[i];
			}
			dataset.addSeries(SERIES_KEY_PLOT, serdat);
			
			if(voxWarnLow > 0) {
				serdat = new double[2][2];
				serdat[0][0] = 0.0; //x0
				serdat[0][1] = (double)(yData.length-1); //x1
				serdat[1][0] = (double)voxWarnLow; //y0
				serdat[1][1] = (double)voxWarnLow; //y1
				dataset.addSeries(SERIES_KEY_WLO, serdat);
				
				if(voxWarnHigh > 0) {
					serdat = new double[2][2];
					serdat[0][0] = 0.0; //x0
					serdat[0][1] = (double)(yData.length-1); //x1
					serdat[1][0] = (double)voxWarnHigh; //y0
					serdat[1][1] = (double)voxWarnHigh; //y1
					dataset.addSeries(SERIES_KEY_WHI, serdat);
				}
			}
		}
		
		if(chart == null) {
			chart = ChartFactory.createXYLineChart("", xLabel, yLabel, dataset);
			chart.removeLegend();
			xyPlot = chart.getXYPlot();
			setChart(chart);
		}
		
		//Update aesthetics
		updateChartAppearance();
		repaint();
	}
	
	/*--- Getters ---*/
	
	/*--- Setters ---*/
	
	public void setBackgroundColor(Color c) {colorBkg = c; updateChartAppearance();}
	public void setPlotColor(Color c) {colorPlot = c; updateChartAppearance();}
	public void setHLine1Color(Color c) {colorWarnLo = c; updateChartAppearance();}
	public void setHLine2Color(Color c) {colorWarnHi = c; updateChartAppearance();}
	public void setGridLineColor(Color c) {colorGridline = c; updateChartAppearance();}
	
	public void setPlotLineWidth(float val) {lineWidthPlot = val; updateChartAppearance();}
	public void setHLine1Width(float val) {lineWidthWarnLo = val; updateChartAppearance();}
	public void setHLine2Width(float val) {lineWidthWarnHi = val; updateChartAppearance();}
	
	public void loadData(int[] y) {
		yData = y;
		updateChart();
	}

}
