package waffleoRai_zeqer64.GUI.seqedit.voxgraph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.border.Border;

public class OneDimHeatMap extends JLabel{

	private static final long serialVersionUID = -8534311898406517792L;
	
	/*--- Instance Variables ---*/
	
	private double xMin = 0.0;
	private double xMax = 50.0;
	
	private boolean autoXZoom = false;
	private double xZoom = 1.0;
	private boolean autoY = false;
	private int minYSizePix = 20;
	private List<AutoZoomCallback> autoZoomXCallbacks;
	
	//8 levels
	private Color[] palette = null;
	
	private double[] x = null; //Corresponding x values
	private int[] data = null; //Levels
	
	//--- Drawing scratch
	private int drawW = 0;
	private int drawH = 0;
	
	/*--- Initialization ---*/
	
	public OneDimHeatMap() {
		//Default palette
		palette = new Color[9];
		palette[0] = new Color(0xffffff);
		palette[1] = new Color(0xffb5b5);
		palette[2] = new Color(0xff6a6a);
		palette[3] = new Color(0xff4040);
		palette[4] = new Color(0xff0000);
		palette[5] = new Color(0xbf0000);
		palette[6] = new Color(0x840000);
		palette[7] = new Color(0x440000);
		palette[8] = new Color(0x000000);
		setBackground(palette[0]);
		autoZoomXCallbacks = new LinkedList<AutoZoomCallback>();
	}
	
	/*--- Getters ---*/
	
	/*--- Setters ---*/
	
	public void setXValues(double[] xVal) {x = xVal;}
	public void setLevelValues(int[] lVal) {data = Arrays.copyOf(lVal, lVal.length);}
	public void setAutoXZoom(boolean on) {autoXZoom = on;}
	public void addAutoZoomXCallback(AutoZoomCallback func) {autoZoomXCallbacks.add(func);}
	public void clearAutoZoomXCallbacks() {autoZoomXCallbacks.clear();}
	
	public void setXRange(double min, double max) {xMin = min; xMax = max;}
	public void setXZoom(double val) {xZoom = val;}
	public void setMinYSize(int val) {minYSizePix = val;}
	
	/*--- Drawing ---*/
	
	private void updateScaling(Graphics g) {
		Rectangle clipBounds = g.getClipBounds();
		
		//Calculate minimum based on zoom/range settings
		drawW = (int)Math.ceil((xMax - xMin) * xZoom);
		drawH = minYSizePix;
		
		//Adjust to fill clip area if clip area is bigger
		if(clipBounds.width > drawW) {
			drawW = clipBounds.width;
			if(autoXZoom) {
				xZoom = (double)drawW / (xMax - xMin);
				for(AutoZoomCallback func : autoZoomXCallbacks) func.onAutoZoom(xZoom);
			}
		}
		
		if(autoY && (clipBounds.height > drawH)) {
			drawH = clipBounds.height;
		}
		
		setMinimumSize(new Dimension(drawW, drawH));
		setPreferredSize(new Dimension(drawW, drawH));
		
		if(!autoY && (clipBounds.height > drawH)) {
			drawH = clipBounds.height;
		}
		
		Border b = this.getBorder();
		if(b != null) {
			Insets n = b.getBorderInsets(this);
			drawH -= n.bottom;
		}
	}
	
	protected void paintComponent(Graphics g) {
		updateScaling(g);
		super.paintComponent(g);
		
		if(x == null) return;
		
		//Adjust graphics
		if(!(g instanceof Graphics2D)) return;
		Graphics2D g2 = (Graphics2D)g;
		
		if(xZoom >= 1.0) {
			for(int i = 0; i < x.length; i++) {
				double xx = (double)x[i];
				if(xx < xMin) continue;
				if(xx > xMax) continue;
				double xSt = (xx - xMin) * xZoom;
				double xEd = xSt + xZoom;
				int x0 = (int)Math.round(xSt);
				int x1 = (int)Math.round(xEd);
				
				if(i < data.length) {
					int dval = data[i];
					if(dval < 0) dval = 0;
					if(dval >= palette.length) dval = palette.length - 1;
					g2.setColor(palette[dval]);
					g2.fillRect(x0, 0, (x1-x0), drawH);
				}
				else break;
			}
		}
		else {
			//Collect and average
			int[] sum = new int[drawW];
			int[] count = new int[drawW];
			for(int i = 0; i < x.length; i++) {
				double xx = (double)x[i];
				if(xx < xMin) continue;
				if(xx > xMax) continue;
				double xSt = (xx - xMin) * xZoom;
				int x0 = (int)Math.round(xSt);
				
				if(i < data.length) {
					sum[x0] += data[i];
					count[x0]++;
				}
				else break;
			}
			for(int i = 0; i < drawW; i++) {
				double avgRaw = (double)sum[i] / (double)count[i];
				int avgRound = (int)Math.round(avgRaw);
				if(avgRound < 0) avgRound = 0;
				if(avgRound >= palette.length) avgRound = palette.length-1;
				g2.setColor(palette[avgRound]);
				g2.fillRect(i, 0, 1, drawH);
			}
		}
	}
	
	/*--- Callbacks ---*/

}
