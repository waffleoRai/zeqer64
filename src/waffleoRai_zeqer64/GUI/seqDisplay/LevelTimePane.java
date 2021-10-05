package waffleoRai_zeqer64.GUI.seqDisplay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

class LevelTimePane extends JPanel{

	/*----- Constants -----*/
	
	private static final long serialVersionUID = 5406829324186175950L;
	
	public static final int VALTYPE_8SIGNED = 0;
	public static final int VALTYPE_8UNSIGNED = 1; //But only uses 0-127, like volume
	public static final int VALTYPE_8PAN = 2;
	public static final int VALTYPE_8UNSIGNED_FULLRANGE = 3;
	
	private static final int MID_PAN = 0x40;
	
	private static final int POLYGON_BORDER_THICKNESS = 2;
	
	private static final int SHADELVL_OUTLINE = 50;
	//private static final int SHADELVL_POLYFILL = 120;
	private static final int SHADELVL_NODEFILL = 120;
	
	private static final int POINTLBL_INNER_RADIUS = 2;
	private static final int POINTLBL_OUTER_RADIUS = 4;
	
	private static final int POINTLBL_OUTERDIM = POINTLBL_OUTER_RADIUS << 1;
	private static final int POINTLBL_INNEROFF = POINTLBL_OUTER_RADIUS - POINTLBL_INNER_RADIUS;
	private static final int POINTLBL_INNERDIM = POINTLBL_INNER_RADIUS << 1;
	
	/*----- Instance Variables -----*/
	
	private int val_type;
	private LevelPanelType lvl_type;
	private Map<Integer, Integer> points;
	private List<ValuePoint> prerendered;
	
	private ZeqerColorScheme colorScheme;
	private double ticks_per_pixel = 4.0;
	private int x_offset = 0; //Number of pixels left of zero tick
	private int ex_height = DisplayTrack.HEIGHTS_LEVELPNL[1]; //Expected height
	private int max_width = 0;
	
	private Color clr_outline = Color.black;
	private Color clr_fill = Color.gray;
	private Color clr_node = Color.gray;
	
	/*----- Init -----*/
	
	public LevelTimePane(){
		this(ZeqerColorSchemes.GREY, LevelPanelType.VOLUME);
	}
	
	public LevelTimePane(ZeqerColorScheme color, LevelPanelType lvltype){
		colorScheme = color;
		lvl_type = lvltype;
		switch(lvltype){
		case PAN:
			val_type = VALTYPE_8PAN;
			break;
		case PITCH_BEND:
			val_type = VALTYPE_8SIGNED;
			break;
		case TEMPO:
			val_type = VALTYPE_8UNSIGNED;
			break;
		case VOLUME:
			val_type = VALTYPE_8UNSIGNED;
			break;
		default:
			val_type = VALTYPE_8SIGNED;
			break;
		}
		points = new TreeMap<Integer, Integer>();
		updateColor();
		prerendered = new LinkedList<ValuePoint>();
		this.setLayout(null);
	}
	
	/*----- Inner Classes -----*/
	
	private class ValuePoint extends JLabel implements Comparable<ValuePoint>{
		
		private static final long serialVersionUID = 5937845479949465801L;
		
		private int x; //Centerpoint of value label
		private int y; //Centerpoint of value label
		private int y_rel; //Pixels from midline (if applicable)
		
		public ValuePoint(String tooltip){
			super.setText("");
			super.setIcon(null);
			
			//Size
			super.setMinimumSize(new Dimension(POINTLBL_OUTERDIM, POINTLBL_OUTERDIM));
			super.setPreferredSize(new Dimension(POINTLBL_OUTERDIM, POINTLBL_OUTERDIM));
			
			super.setToolTipText(tooltip);
		}
		
		public void updateBounds(){
			this.setBounds(x - POINTLBL_OUTER_RADIUS, 
					y - POINTLBL_OUTER_RADIUS, 
					POINTLBL_OUTERDIM, 
					POINTLBL_OUTERDIM);
		}
		
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			
			Color gcolor = g.getColor();
			g.setColor(clr_outline);
			g.fillOval(0, 0, POINTLBL_OUTERDIM, POINTLBL_OUTERDIM);
			g.setColor(clr_node);
			g.fillOval(POINTLBL_INNEROFF, POINTLBL_INNEROFF, POINTLBL_INNERDIM, POINTLBL_INNERDIM);
			
			g.setColor(gcolor);
		}

		public boolean equals(Object o){
			if(o == null) return false;
			if(o == this) return true;
			if(!(o instanceof ValuePoint)) return false;
			
			ValuePoint other = (ValuePoint)o;
			if(this.x != other.x) return false;
			if(this.y != other.y) return false;
			if(this.y_rel != other.y_rel) return false;
			
			return true;
		}
		
		public int hashCode(){
			return x ^ (y << 16) ^ (y_rel << 24);
		}
		
		public int compareTo(ValuePoint o) {
			if(o == null) return 1;
			
			if(this.x != o.x) return this.x - o.x;
			if(this.y != o.y) return this.y - o.y;
			
			return 0;
		}
		
	}
	
	/*----- Getters -----*/
	
	public int getValueType(){return val_type;}
	public LevelPanelType getLevelValueType(){return lvl_type;}
	
	/*----- Setters -----*/
	
	public void setValueType(int valType){val_type = valType;}
	public void clearPoints(){points.clear();}
	
	public void setColorScheme(ZeqerColorScheme color){
		colorScheme = color;
		updateColor();
		//Don't need to update point comp values - they are non-static and refer to 
		//	this panel's color scheme to decide how to paint
	}
	
	public void addPoint(int tick, int value){
		points.put(tick, value);
		prerendered.add(pointToComponent(tick, value));
		Collections.sort(prerendered);
	}
	
	public void setTicksPerPixel(double val){ticks_per_pixel = val; updatePointPositions();}
	public void setPixelsPerTick(double val){ticks_per_pixel = 1.0/val; updatePointPositions();}
	public void setLeftBufferSize(int pixels){x_offset = pixels; updatePointPositions();}
	public void setExpectedHeight(int pixels){ex_height = pixels; updatePointPositions();}
	
	/*----- Utils -----*/
	
	private int ticksToPixels(int ticks){
		int pix = (int)Math.round((double)ticks/ticks_per_pixel);
		return pix;
	}
	
	/*----- Paint -----*/
	
	private ValuePoint pointToComponent(int tick, int value){
		ValuePoint vp = new ValuePoint(getTooltip(tick, value));
		vp.x = x_offset;
		vp.x += ticksToPixels(tick);
		
		double d = 0.0;
		int halfheight = ex_height >>> 1;
		switch(val_type){
		case VALTYPE_8SIGNED:
			if(value < 0) d = (double)value/128.0;
			else d = (double)value/127.0;
			vp.y_rel = (int)Math.round(d * (double)(halfheight-2) * -1);
			vp.y = halfheight + vp.y_rel;
			break;
		case VALTYPE_8UNSIGNED:
			d = (double)value/127.0;
			vp.y_rel = (int)Math.round(d * (double)ex_height * -1);
			vp.y = ex_height + vp.y_rel;
			break;
		case VALTYPE_8PAN:
			if(value < MID_PAN){
				value = MID_PAN - value;
				d = (double)value/(double)MID_PAN;
				vp.y_rel = (int)Math.round(d * (double)(halfheight-2)) * -1;
			}
			else if(value > MID_PAN){
				value -= MID_PAN;
				d = (double)value/(double)(MID_PAN-1);
				vp.y_rel = (int)Math.round(d * (double)(halfheight-2));
			}
			vp.y = halfheight + vp.y_rel;
			break;
		case VALTYPE_8UNSIGNED_FULLRANGE:
			d = (double)value/255.0;
			vp.y_rel = (int)Math.round(d * (double)ex_height * -1);
			vp.y = ex_height + vp.y_rel;
			break;
		}
		
		return vp;
	}
	
	private void updateColor(){
		setBackground(colorScheme.getLightBackground());
		setForeground(colorScheme.getDarkBorder());
		this.setBorder(BorderFactory.createEtchedBorder());
		
		clr_outline = colorScheme.getShade(SHADELVL_OUTLINE);
		//clr_fill = colorScheme.getShade(SHADELVL_POLYFILL);
		clr_fill = colorScheme.getLightBorder();
		clr_node = colorScheme.getShade(SHADELVL_NODEFILL);
	}
	
	private void updatePointPositions(){
		removeAll();
		prerendered.clear();
		List<Integer> tlist = new LinkedList<Integer>();
		tlist.addAll(points.keySet());
		
		for(Integer t : tlist){
			int val = points.get(t);
			ValuePoint vp = pointToComponent(t, val);
			prerendered.add(vp);
			vp.updateBounds();
			add(vp);
		}
		
		Collections.sort(prerendered);
		max_width = 0;
	}
	
	private String getTooltip(int key, int val){
		StringBuilder sb = new StringBuilder(256);
		sb.append("Tick ");
		sb.append(key);
		sb.append(": ");
		
		switch(val_type){
		case VALTYPE_8SIGNED:  
			int vtrim = val & 0xff;
			sb.append(String.format("0x%02x", vtrim));
			sb.append(" (" + val + ")");
			break;
		case VALTYPE_8PAN: 
			sb.append(String.format("0x%02x", val));
			if(val < MID_PAN){
				//Left
				val = MID_PAN - val;
				double amt = (double)val/(double)MID_PAN;
				amt *= 100.0;
				int round = (int)Math.round(amt);
				//sb.append(String.format("(%.2f", amt));
				sb.append(String.format("(%d", round));
				sb.append("% L)");
			}
			else if(val > MID_PAN){
				//Right
				double amt = (double)(val - MID_PAN)/(double)(0x7f - MID_PAN);
				amt *= 100.0;
				int round = (int)Math.round(amt);
				//sb.append(String.format("(%.2f", amt));
				sb.append(String.format("(%d", round));
				sb.append("% R)");
			}
			else sb.append("(Center)");
			break;
		case VALTYPE_8UNSIGNED: 
			sb.append(String.format("0x%02x", val));
			sb.append(" (" + val + ")");
			break;
		case VALTYPE_8UNSIGNED_FULLRANGE:
			sb.append(String.format("0x%02x", val));
			sb.append(" (" + val + ")");
			break;
		}
		
		return sb.toString();
	}
		
	private void drawPolygon(Graphics2D g, List<Integer> xlist, List<Integer> ylist){
		if(xlist.isEmpty()) return;
		int pcount = xlist.size()+1;
		int[] xarr = new int[pcount];
		int[] yarr = new int[pcount];
		
		int i = 0;
		for(Integer x : xlist) xarr[i++] = x;
		i = 0;
		for(Integer y : ylist) yarr[i++] = y;
		xarr[pcount-1] = xarr[pcount-2];
		yarr[pcount-1] = yarr[0];
		
		g.setColor(clr_fill);
		g.fillPolygon(xarr, yarr, pcount);
		
		//g.setColor(clr_outline);
		//g.drawPolygon(xarr, yarr, pcount);
	}
	
	private void paintComponent_signedType(Graphics2D g){
		//Determine colors
		Color clr_midline = colorScheme.getDarkBorder();
		
		Stroke defostroke = g.getStroke();
		
		//Draw midline
		Rectangle bounds = g.getClipBounds();
		int xend = bounds.x + bounds.width;
		if(xend > max_width) max_width = xend;
		else xend = max_width;
				
		int ymid = ex_height/2;
		Color gcolor = g.getColor();
		g.setColor(clr_midline);
		g.drawLine(0, ymid, xend, ymid);
		
		//Generate and add labels (absolute layout)
		g.setStroke(new BasicStroke(POLYGON_BORDER_THICKNESS));
		//int x = 0, y = 0;
		
		LinkedList<ValuePoint> pointlist = new LinkedList<ValuePoint>();
		pointlist.addAll(prerendered);
		
		//Draw connecting lines/polygon
		int side = 0; //Starts at midline
		LinkedList<Integer> xlist = new LinkedList<Integer>();
		LinkedList<Integer> ylist = new LinkedList<Integer>();
		xlist.add(0); ylist.add(ymid);
		while(!pointlist.isEmpty()){
			ValuePoint vp = pointlist.pop();

			//Check against last sign. If crossing zero, draw previous polygon.
			if(vp.y_rel == 0){
				//If it was zero before, no polygon to draw. Clear previous points and add this one.
				//If it was nonzero, draw polygon.
				if(side == 0){
					xlist.clear(); ylist.clear();
					xlist.add(vp.x); ylist.add(vp.y);
				}
				else{
					xlist.add(vp.x); ylist.add(vp.y);
					drawPolygon(g, xlist, ylist);
					
					xlist.clear(); ylist.clear();
					xlist.add(vp.x); ylist.add(vp.y);
					side = 0;
				}
			}
			else if(vp.y_rel < 0){
				if(side <= 0){
					//Add this point to polygon
					xlist.add(vp.x); ylist.add(vp.y);
					side = -1;
				}
				else{
					if(xlist.isEmpty()){
						//This is an error. But put anyway.
						xlist.add(vp.x); ylist.add(vp.y);
						side = -1;
					}
					else{
						//Need to draw polygon.
						//Calculate midline intercept
						int lastx = xlist.getLast();
						int lasty = ylist.getLast();
						
						double slope = (double)(vp.y - lasty);
						slope /= (double)(vp.x - lastx);
						int yoff = ymid-lasty;
						int xoff = (int)Math.round(slope/(double)yoff);
						
						int ctrx = lastx + xoff;
						xlist.add(ctrx); ylist.add(ymid);
						drawPolygon(g, xlist, ylist);
						
						xlist.clear(); ylist.clear();
						xlist.add(ctrx); ylist.add(ymid);
						xlist.add(vp.x); ylist.add(vp.y);
						side = -1;
					}
				}
			}
			else{
				//Greater than (minus to plus)
				if(side >= 0){
					xlist.add(vp.x); ylist.add(vp.y);
					side = 1;
				}
				else{
					int lastx = xlist.getLast();
					int lasty = ylist.getLast();
					
					double slope = (double)(lasty - vp.y);
					slope /= (double)(vp.x - lastx);
					int yoff = lasty-ymid;
					int xoff = (int)Math.round(slope/(double)yoff);
					
					int ctrx = lastx + xoff;
					xlist.add(ctrx); ylist.add(ymid);
					drawPolygon(g, xlist, ylist);
					
					xlist.clear(); ylist.clear();
					xlist.add(ctrx); ylist.add(ymid);
					xlist.add(vp.x); ylist.add(vp.y);
					side = 1;
				}
			}
		}
		
		//If there are leftover points, put into polygon
		if(!xlist.isEmpty()){
			int lasty = ylist.getLast();
			xlist.add(xend);
			ylist.add(lasty);
			drawPolygon(g, xlist, ylist);
			xlist.clear(); ylist.clear();
		}
		
		//Draw lines
		g.setColor(clr_outline);
		ValuePoint last = null;
		for(ValuePoint vp : prerendered){
			if(last != null){
				g.drawLine(last.x, last.y, vp.x, vp.y);
			}
			else{
				g.drawLine(0, ymid, vp.x, vp.y);
			}
			last = vp;
		}
		if(last != null)g.drawLine(last.x, last.y, xend, last.y);
		
		//Reset color
		g.setColor(gcolor);
		g.setStroke(defostroke);
	}
	
	private void paintComponent_unsignedType(Graphics2D g){
		Stroke defostroke = g.getStroke();
		Color gcolor = g.getColor();
		
		Rectangle bounds = g.getClipBounds();
		int xend = bounds.x + bounds.width;
		if(xend > max_width) max_width = xend;
		else xend = max_width;
		
		//Generate and add labels (absolute layout)
		g.setStroke(new BasicStroke(POLYGON_BORDER_THICKNESS));
		LinkedList<ValuePoint> pointlist = new LinkedList<ValuePoint>();
		pointlist.addAll(prerendered);
		
		//Draw connecting lines/polygon
		LinkedList<Integer> xlist = new LinkedList<Integer>();
		LinkedList<Integer> ylist = new LinkedList<Integer>();
		xlist.add(0); ylist.add(ex_height);
		while(!pointlist.isEmpty()){
			ValuePoint vp = pointlist.pop();
			
			if(vp.y == ex_height){
				//Draw current polygon.
				if(!xlist.isEmpty()){
					//Check if last is also a zero point. If so, clear.
					if(ylist.getLast() == ex_height){
						xlist.clear(); ylist.clear();
						xlist.add(vp.x); ylist.add(vp.y);
					}
					else{
						//Draw
						xlist.add(vp.x); ylist.add(vp.y);
						drawPolygon(g, xlist, ylist);
						xlist.clear(); ylist.clear();
						xlist.add(vp.x); ylist.add(vp.y);
					}
				}
				else{
					xlist.add(vp.x); ylist.add(vp.y);
				}
			}
			else{
				//Just add.
				xlist.add(vp.x); ylist.add(vp.y);
			}
			
		}
		
		//If there are leftover points, put into polygon
		if(!xlist.isEmpty()){
			int lasty = ylist.getLast();
			xlist.add(xend);
			ylist.add(lasty);
			drawPolygon(g, xlist, ylist);
			xlist.clear(); ylist.clear();
		}
		
		//Draw lines
		g.setColor(clr_outline);
		ValuePoint last = null;
		for(ValuePoint vp : prerendered){
			if(last != null){
				g.drawLine(last.x, last.y, vp.x, vp.y);
			}
			else{
				g.drawLine(0, ex_height, vp.x, vp.y);
			}
			last = vp;
		}
		if(last != null)g.drawLine(last.x, last.y, xend, last.y);
		
		//Reset Graphics state
		g.setColor(gcolor);
		g.setStroke(defostroke);
	}
	
	public void paintComponent(Graphics g){
		//super.setBackground(colorScheme.getLightBorder());
		
		//removeAll();
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		switch(val_type){
		case VALTYPE_8SIGNED: 
		case VALTYPE_8PAN:
			paintComponent_signedType(g2); break;
		case VALTYPE_8UNSIGNED: 
		case VALTYPE_8UNSIGNED_FULLRANGE:
			paintComponent_unsignedType(g2); break;
		}
		//super.paintComponent(g);
	}

}
