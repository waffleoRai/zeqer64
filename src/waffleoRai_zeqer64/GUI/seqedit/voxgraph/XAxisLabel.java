package waffleoRai_zeqer64.GUI.seqedit.voxgraph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;

public class XAxisLabel extends JLabel{
	
	private static final long serialVersionUID = -41413748037659443L;
	
	private static final int LBL_TICK_SPACING = 3;
	
	/*--- Instance Variables ---*/
	
	private Color textColor = Color.black;
	private Color tickColor = Color.black;
	private float minorTickThickness = 1.0f;
	private float majorTickThickness = 2.0f;
	private int minorTickLength = 5;
	private int majorTickLength = 10;
	
	private Font textFont = new Font("Tahoma", Font.PLAIN, 11);
	
	private boolean autoZoom = false;
	private double zoom = 1.0;
	private List<AutoZoomCallback> autoZoomCallbacks;
	
	private double xMin = 0.0;
	private double xMax = 48.0;
	
	private double xTick = 12.0; //Minor tick in x value
	private int minorTickPerMajor = 4;
	
	private int minTickGap = 5; //In pixels
	private int maxTickGap = 20;
	
	//--- Labeling
	private boolean centerLabels = true;
	private boolean lblMusicMode = false; //If true, show in measures/beats instead of raw ticks
	private int beatsPerMeasure = 4;
	private int ticksPerBeat = 48;
	
	//--- Drawing scratch
	private int drawW = 0;
	private int drawH = 0;
		
	private int gridMinorPix = 0; //Tick distance in pixels
	private double gridAdj = 1.0;
	
	/*--- Initialization ---*/
	
	public XAxisLabel() {
		drawW = (int)Math.ceil((xMax - xMin) * zoom);
		drawH = majorTickLength + (LBL_TICK_SPACING << 1) + textFont.getSize();
		setMinimumSize(new Dimension(drawW, drawH));
		setPreferredSize(new Dimension(drawW, drawH));
		autoZoomCallbacks = new LinkedList<AutoZoomCallback>();
	}
	
	/*--- Getters ---*/
	
	public double getGridScale() {return gridAdj;}
	
	/*--- Setters ---*/
	
	public void setAutoZoom(boolean on) {autoZoom = on;}
	public void setZoom(double val) {zoom = val;}
	public void addAutoZoomXCallback(AutoZoomCallback func) {autoZoomCallbacks.add(func);}
	public void clearAutoZoomXCallbacks() {autoZoomCallbacks.clear();}
	
	public void setXRange(double min, double max) {
		xMin = min; xMax = max;
	}
	
	/*--- Drawing ---*/
	
	
	private String getMusicPosition(double tick) {
		double totalBeat = tick/(double)ticksPerBeat;
		int totalBeatI = (int)Math.floor(totalBeat);
		int measure = totalBeatI/beatsPerMeasure;
		int beatI = totalBeatI % beatsPerMeasure;
		double beat = totalBeat - (double)totalBeatI;
		beat += (double)beatI;
		
		return String.format("%d|%.3f", measure, beat);
	}
	
	private void updateScaling(Graphics g) {
		Rectangle clipBounds = g.getClipBounds();
		
		drawW = (int)Math.ceil((xMax - xMin) * zoom);
		drawH = majorTickLength + (LBL_TICK_SPACING << 1) + textFont.getSize();
		
		//Adjust to fill clip area if clip area is bigger
		if(clipBounds.width > drawW) {
			drawW = clipBounds.width;
			if(autoZoom) {
				zoom = (double)drawW / (xMax - xMin);
				for(AutoZoomCallback func : autoZoomCallbacks) func.onAutoZoom(zoom);
			}
		}
		if(clipBounds.height > drawH) {
			drawH = clipBounds.height;
		}
		
		setMinimumSize(new Dimension(drawW, drawH));
		setPreferredSize(new Dimension(drawW, drawH));
		
		//Update grid line positions
		gridMinorPix = (int)Math.floor(xTick * zoom);
		gridAdj = 1.0;
		if(gridMinorPix > minTickGap) {
			while(gridMinorPix > maxTickGap) {
				gridMinorPix >>= 1;
				gridAdj /= 2.0;
			}
		}
		else if(gridMinorPix < minTickGap) {
			while(gridMinorPix < minTickGap) {
				gridMinorPix <<= 1;
				gridAdj *= 2.0;
			}
		}
	}
	
	protected void paintComponent(Graphics g) {
		updateScaling(g);
		super.paintComponent(g);

		//Adjust graphics
		if(!(g instanceof Graphics2D)) return;
		Graphics2D g2 = (Graphics2D)g;
		
		//Minor ticks
		g2.setColor(tickColor);
		g2.setStroke(new BasicStroke(minorTickThickness));
		double deltaVal = xTick * gridAdj; //Change in x between minor gridlines
		double xStartVal = deltaVal; //Figure out x value that grid starts on.
		//First delta above xmin
		if(xStartVal > xMin) {
			//How many deltas can be subtracted until under xmin?
			while(xStartVal > xMin) xStartVal -= deltaVal;
		}
		while(xStartVal < xMin) xStartVal += deltaVal;
		int xStart = (int)Math.round((xStartVal - xMin) * zoom);
		for(int x = xStart; x < drawW; x+=gridMinorPix) {
			g2.drawLine(x, (drawH - minorTickLength), x, drawH);
		}
		
		//Major ticks
		g2.setStroke(new BasicStroke(majorTickThickness));
		int delta = gridMinorPix * minorTickPerMajor;
		for(int x = xStart; x < drawW; x+=delta) {
			g2.drawLine(x, (drawH - majorTickLength), x, drawH);
		}
		
		//Labels
		g2.setColor(textColor);
		g2.setFont(textFont);
		double xval = xStartVal;
		deltaVal = (xTick * minorTickPerMajor) * gridAdj;
		int txtYBase = drawH - majorTickLength - LBL_TICK_SPACING;
		for(int x = xStart; x < drawW; x+=delta) {
			String lblStr = String.format("%.1f", xval);
			if(lblMusicMode) {
				lblStr = getMusicPosition(xval);
			}
			
			if(centerLabels) {
				Rectangle2D slblBounds = textFont.getStringBounds(lblStr, g2.getFontRenderContext());
				int ww = (int)Math.ceil(slblBounds.getWidth());
				//int hh = (int)Math.ceil(slblBounds.getHeight());
				int xx = x - (ww/2);
				g2.drawString(lblStr, xx, txtYBase);
			}
			else g2.drawString(lblStr, x, txtYBase);

			xval += deltaVal;
		}
	}
	
	/*--- Callbacks ---*/

}
