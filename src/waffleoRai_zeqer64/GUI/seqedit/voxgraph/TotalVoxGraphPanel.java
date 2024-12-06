package waffleoRai_zeqer64.GUI.seqedit.voxgraph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;

import waffleoRai_zeqer64.GUI.seqedit.voxgraph.GraphLine.GraphPoint;

//This is just the drawing area. No labels or anything like that.
public class TotalVoxGraphPanel extends JPanel{

	private static final long serialVersionUID = -1812124601944805431L;
	
	/*--- Instance Variables ---*/
	
	private List<GraphLine> plots;
	private List<GraphLine> vLines;
	private List<GraphLine> hLines;
	
	private double xmin = 0.0;
	private double xmax = 50.0;
	private double ymin = 0.0;
	private double ymax = 50.0;
	
	private double xZoom = 1.0; //Pixels per 1.0 x interval
	private double yZoom = 1.0; //Pixels per 1.0 y interval
	
	//--- Grid
	private Color gridColor = Color.gray;
	private float gridMajorWidth = 1.5f;
	private float gridMinorWidth = 1.0f;
	
	private double minVTick = 2;
	private double minHTick = 2;
	private int vMinPerMaj = 5;
	private int hMinPerMaj = 5;
	
	private int minVTickGap = 5;
	private int maxVTickGap = 20;
	private int minHTickGap = 5;
	private int maxHTickGap = 20;
	
	//--- Drawing scratch
	private int drawW = 0;
	private int drawH = 0;
	
	private int vGridMinorPix = 0; //In pixels
	private int hGridMinorPix = 0;
	private double vGridAdj = 1.0;
	private double hGridAdj = 1.0;
	
	/*--- Initialization ---*/
	
	public TotalVoxGraphPanel() {
		setBackground(Color.white);
		plots = new ArrayList<GraphLine>(4);
		vLines = new ArrayList<GraphLine>(4);
		hLines = new ArrayList<GraphLine>(4);
	}
	
	/*--- Getters ---*/
	
	public double getVerticalGridScaler() {return vGridAdj;}
	public double getHorizontalGridScaler() {return hGridAdj;}
	
	/*--- Setters ---*/
	
	public void setGridColor(Color c) {gridColor = c;}
	public void setGridMinorLineThickness(float val) {gridMinorWidth = val;}
	public void setGridMajorLineThickness(float val) {gridMajorWidth = val;}
	public void setXZoom(double val) {xZoom = val;}
	public void setYZoom(double val) {yZoom = val;}
	
	public void setXGridTicks(double minorVal, int minorPerMajor) {
		minVTick = minorVal;
		vMinPerMaj = minorPerMajor;
	}
	
	public void setYGridTicks(double minorVal, int minorPerMajor) {
		minHTick = minorVal;
		hMinPerMaj = minorPerMajor;
	}
	
	public void setXRange(double min, double max) {
		xmin = min; xmax = max;
	}
	
	public void setYRange(double min, double max) {
		ymin = min; ymax = max;
	}
	
	public void zoomToFit(int width, int height) {
		xZoom = (double)width / (xmax - xmin);
		yZoom = (double)height / (ymax - ymin);
	}
	
	public void addPlotLine(double[] x, double[] y, float thickness, Color color) {
		GraphLine line = new GraphLine();
		line.type = GraphLine.LINE_TYPE_PLOT;
		line.lineColor = color;
		line.thickness = thickness;
		
		line.points.ensureCapacity(x.length);
		for(int i = 0; i < x.length; i++) {
			GraphPoint pt = new GraphPoint();
			pt.x = x[i];
			if(i <= y.length) {
				pt.y = y[i];
			}
			else {pt.y = 0;}
			line.points.add(pt);
		}
		
		Collections.sort(line.points);
		plots.add(line);
	}
	
	public void addPlotLine(int[] x, int[] y, float thickness, Color color) {
		GraphLine line = new GraphLine();
		line.type = GraphLine.LINE_TYPE_PLOT;
		line.lineColor = color;
		line.thickness = thickness;
		
		line.points.ensureCapacity(x.length);
		for(int i = 0; i < x.length; i++) {
			GraphPoint pt = new GraphPoint();
			pt.x = (double)x[i];
			if(i <= y.length) {
				pt.y = (double)y[i];
			}
			else {pt.y = 0;}
			line.points.add(pt);
		}
		
		Collections.sort(line.points);
		plots.add(line);
	}
	
	public void addVerticalLine(double x, float thickness, Color color) {
		GraphLine line = new GraphLine();
		line.type = GraphLine.LINE_TYPE_VERT;
		line.lineColor = color;
		line.thickness = thickness;
		line.constVal = x;
		vLines.add(line);
	}
	
	public void addHorizontalLine(double y, float thickness, Color color) {
		GraphLine line = new GraphLine();
		line.type = GraphLine.LINE_TYPE_HORZ;
		line.lineColor = color;
		line.thickness = thickness;
		line.constVal = y;
		hLines.add(line);
	}
	
	public void clearAllPlots() {
		plots.clear();
		vLines.clear();
		hLines.clear();
	}
	
	/*--- Drawing ---*/
	
	private void updateScaling(Graphics g) {
		Rectangle clipBounds = g.getClipBounds();
		
		//Calculate minimum based on zoom/range settings
		drawW = (int)Math.ceil((xmax - xmin) * xZoom);
		drawH = (int)Math.ceil((ymax - ymin) * yZoom);
		
		//Adjust to fill clip area if clip area is bigger
		if(clipBounds.width > drawW) {
			drawW = clipBounds.width;
			xZoom = (double)drawW / (xmax - xmin);
		}
		if(clipBounds.height > drawH) {
			drawH = clipBounds.height;
			yZoom = (double)drawH / (ymax - ymin);
		}
		
		setMinimumSize(new Dimension(drawW, drawH));
		setPreferredSize(new Dimension(drawW, drawH));
		
		//Update grid line positions
		vGridMinorPix = (int)Math.floor(minVTick * xZoom);
		vGridAdj = 1.0;
		if(vGridMinorPix > maxVTickGap) {
			while(vGridMinorPix > maxVTickGap) {
				vGridMinorPix >>>= 1;
				vGridAdj /= 2.0;
			}
		}
		else if(vGridMinorPix < minVTickGap) {
			while(vGridMinorPix < minVTickGap) {
				vGridMinorPix <<= 1;
				vGridAdj *= 2.0;
			}
		}
		
		hGridMinorPix = (int)Math.floor(minHTick * yZoom);
		hGridAdj = 1.0;
		if(hGridMinorPix > maxHTickGap) {
			while(hGridMinorPix > maxHTickGap) {
				hGridMinorPix >>>= 1;
				hGridAdj /= 2.0;
			}
		}
		else if(hGridMinorPix < minHTickGap) {
			while(hGridMinorPix < minHTickGap) {
				hGridMinorPix <<= 1;
				hGridAdj *= 2.0;
			}
		}
	}
	
	protected void paintComponent(Graphics g) {
		updateScaling(g);
		super.paintComponent(g);
		
		//Adjust graphics
		if(!(g instanceof Graphics2D)) return;
		Graphics2D g2 = (Graphics2D)g;
		
		//Minor gridlines
		g2.setColor(gridColor);
		g2.setStroke(new BasicStroke(gridMinorWidth));
		double deltaVal = minVTick * vGridAdj; //Change in x between minor gridlines
		double xStartVal = deltaVal; //Figure out x value that grid starts on.
		//First delta above xmin
		if(xStartVal > xmin) {
			//How many deltas can be subtracted until under xmin?
			while(xStartVal > xmin) xStartVal -= deltaVal;
		}
		while(xStartVal < xmin) xStartVal += deltaVal;
		int xStart = (int)Math.round((xStartVal - xmin) * xZoom);
		for(int x = xStart; x < drawW; x+=vGridMinorPix) {
			g2.drawLine(x, 0, x, drawH);
		}
		
		deltaVal = minHTick * hGridAdj;
		double yStartVal = deltaVal;
		if(yStartVal > ymin) {
			while(yStartVal > ymin) yStartVal -= deltaVal;
		}
		while(yStartVal < ymin) yStartVal += deltaVal;
		int yStart = (int)Math.round((yStartVal - ymin) * yZoom);
		for(int y = yStart; y < drawH; y+=hGridMinorPix) {
			int yy = drawH - y;
			g2.drawLine(0, yy, drawW, yy);
		}
		
		//Major gridlines
		int delta = vGridMinorPix * vMinPerMaj;
		g2.setStroke(new BasicStroke(gridMajorWidth));
		for(int x = xStart; x < drawW; x+=delta) {
			g2.drawLine(x, 0, x, drawH);
		}
		delta = hGridMinorPix * hMinPerMaj;
		for(int y = yStart; y < drawH; y+=delta) {
			int yy = drawH - y;
			g2.drawLine(0, yy, drawW, yy);
		}
		
		//Plots
		for(GraphLine line : plots) {
			if(line.type != GraphLine.LINE_TYPE_PLOT) continue;
			g2.setColor(line.lineColor);
			g2.setStroke(new BasicStroke(line.thickness));
			int lastX = Integer.MIN_VALUE;
			int lastY = Integer.MIN_VALUE;
			for(GraphPoint pt : line.points) {
				//TODO Update this so it collects those with same draw x and puts down average instead of drawing a bunch of weird vertical lines?
				int xx = (int)Math.round(xZoom * (pt.x - xmin));
				int yy = (int)Math.round(yZoom * (pt.y - ymin));
				yy = drawH - yy; //Flip y
				if(lastX != Integer.MIN_VALUE) {
					g2.drawLine(lastX, xx, lastY, yy);
				}
				lastX = xx;
				lastY = yy;
			}
		}
		
		//Horizontal lines
		for(GraphLine line : hLines) {
			if(line.type != GraphLine.LINE_TYPE_HORZ) continue;
			g2.setColor(line.lineColor);
			g2.setStroke(new BasicStroke(line.thickness));
			int yy = (int)Math.round(yZoom * (line.constVal - ymin));
			g2.drawLine(0, yy, drawW, yy);
		}
		
		//Vertical lines
		for(GraphLine line : vLines) {
			if(line.type != GraphLine.LINE_TYPE_VERT) continue;
			g2.setColor(line.lineColor);
			g2.setStroke(new BasicStroke(line.thickness));
			int xx = (int)Math.round(xZoom * (line.constVal - xmin));
			g2.drawLine(xx, 0, xx, drawH);
		}
	}
	
	/*--- Callbacks ---*/

}
