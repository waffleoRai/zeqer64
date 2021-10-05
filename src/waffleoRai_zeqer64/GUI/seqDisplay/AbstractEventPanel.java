package waffleoRai_zeqer64.GUI.seqDisplay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import waffleoRai_SeqSound.n64al.NUSALSeqCommand;
import waffleoRai_SeqSound.n64al.NUSALSeqCommandMap;

abstract class AbstractEventPanel extends JPanel{
	
	/*----- Constants -----*/

	private static final long serialVersionUID = 2880594559351668687L;
	
	public static final int LINE_THICKNESS_MINOR_TICK = 1;
	public static final int LINE_THICKNESS_MAJOR_TICK = 2;
	public static final int LINE_THICKNESS_MARKER = 2;
	
	public static final double DEFAULT_TPP = 4.0;
	public static final int DEFAULT_MINOR_IVAL = 24;
	public static final int DEFAULT_MAJOR_IVAL = DEFAULT_MINOR_IVAL << 3;
	
	/*----- Instance Variables -----*/
	
	private TrackColorScheme color_main;
	
	private int x_offset = 0; //Pixels left of tick 0
	private int major_tick_ival = DEFAULT_MAJOR_IVAL;
	private int minor_tick_ival = DEFAULT_MINOR_IVAL;
	private double ticks_per_pixel = DEFAULT_TPP;
	private int len_ticks = 0;
	
	private int exp_height = 0;
	private int exp_width = 0; 
	
	//private boolean recalc_xbuff = true;
	//private boolean repaint = true;
	
	private Map<Integer, Color> markers;
	
	/*----- Init -----*/
	
	public AbstractEventPanel(){
		this(TrackColorScheme.GREY);
	}
	
	public AbstractEventPanel(TrackColorScheme color){
		markers = new TreeMap<Integer, Color>();
		setColorScheme(color);
		this.setBorder(BorderFactory.createEtchedBorder());
	}
	
	/*----- Getters -----*/
	
	public TrackColorScheme getColorScheme(){return color_main;}
	public int getLeftBufferSize(){return x_offset;}
	public int getMajorTickInterval(){return major_tick_ival;}
	public int getMinorTickInterval(){return minor_tick_ival;}
	public double getTicksPerPixel(){return ticks_per_pixel;}
	public double getPixelsPerTick(){return 1.0/ticks_per_pixel;}
	public int getMinimumLengthInTicks(){return len_ticks;}
	protected int getExpectedWidth(){return this.exp_width;}
	public int getExpectedHeight(){return this.exp_height;}
	
	/*----- Setters -----*/
	
	protected abstract void updateNodeColor(ZeqerColorScheme clr);
	protected abstract void putEvent(int tick, NUSALSeqCommand cmd);
	protected abstract void onEventsUpdate();
	
	public void setExpectedHeight(int height){
		exp_height = Math.max(0, height);
		recalculateMinimumWidth();
	}
	
	public void setColorScheme(TrackColorScheme clr){
		if(clr == null) clr = TrackColorScheme.GREY;
		color_main = clr;
		updateNodeColor(color_main.getNodeColorScheme());
		setBackground(color_main.getBackgroundColor());
		this.setBorder(BorderFactory.createEtchedBorder());
	}
	
	public void setTickMarkIntervals(int ticks_per_minor, int minor_per_major){
		if(ticks_per_minor < 1) ticks_per_minor = 1;
		if(minor_per_major < 1) minor_per_major = 1;
		minor_tick_ival = ticks_per_minor;
		major_tick_ival = minor_tick_ival * minor_per_major;
	}

	public void setTicksPerPixel(double ratio){
		ticks_per_pixel = ratio;
		recalculateMinimumWidth();
	}
	
	public void setPixelsPerTick(double ratio){
		ticks_per_pixel = 1.0/ratio;
		recalculateMinimumWidth();
	}
	
	public void setLeftBuffer(int pixels){
		x_offset = pixels;
		recalculateMinimumWidth();
	}
	
	public void addGlobalMarker(int tick, Color color){
		markers.put(tick, color);
	}
	
	public void clearGlobalMarkers(){
		markers.clear();
	}

	private boolean addEventCore(int tick, NUSALSeqCommand cmd){
		if(cmd == null) return false;
		if(tick < 0) tick = 0;
		putEvent(tick, cmd);
		int end_tick = tick + cmd.getSizeInTicks();
		if(end_tick > len_ticks) len_ticks = end_tick;
		return true;
	}
	
	public boolean addEvent(int tick, NUSALSeqCommand cmd){
		if(!addEventCore(tick, cmd)) return false;
		onEventsUpdate();
		recalculateMinimumWidth();
		return true;
	}
	
	public int addEvents(NUSALSeqCommandMap cmds){
		if(cmds == null) return 0;
		int c = 0;
		List<Integer> times = cmds.getTimeCoordinates();
		for(Integer t : times){
			List<NUSALSeqCommand> cgroup = cmds.getCommandsAt(t);
			if(cgroup == null) continue;
			for(NUSALSeqCommand cmd : cgroup){
				if(addEventCore(t, cmd)) c++;
			}
		}
		
		onEventsUpdate();
		recalculateMinimumWidth();
		return c;
	}
	
	public abstract void clearEvents();
	
	//protected void setRecalcXBuffer(boolean b){recalc_xbuff = b;}
	//protected void setRepaint(boolean b){repaint = b;}
	
	/*----- Utils -----*/
	
	private void recalculateMinimumWidth(){
		//final int buffer = 10; //Arbitrary buffer for marker. Fine if too high - will just extend track to the right.
		int w = (x_offset*2) + ticksToPixels(len_ticks);
		setMinimumSize(new Dimension(w, exp_height));
		setPreferredSize(new Dimension(w, exp_height));
		exp_width = w;
		onResizeTrigger();
	}
	
	public int pixelsToTicks(int pix){
		int ticks = (int)Math.round((double)pix * ticks_per_pixel);
		return ticks;
	}
	
	public int ticksToPixels(int ticks){
		int pix = (int)Math.round((double)ticks/ticks_per_pixel);
		return pix;
	}
	
	/*----- Paint -----*/
	
	protected abstract void onResizeTrigger();
	//protected abstract void recalculateMinimumLeftBuffer(Graphics2D g);
	protected abstract void onRepaint(Graphics2D g);
	//protected abstract void drawEventMarkers(Graphics2D g);
	
	private void drawTickMarks(Graphics2D g){
		Color gclr = g.getColor();
		Stroke gstr = g.getStroke();
		
		Rectangle bounds = g.getClipBounds();
		int end = Math.max(bounds.x + bounds.width, exp_width);
		g.setColor(color_main.getTickMarkColor());
		//Draw minor ticks
		int x_int_min = ticksToPixels(minor_tick_ival);
		int x = x_offset;
		g.setStroke(new BasicStroke(LINE_THICKNESS_MINOR_TICK));
		while(x < end){
			g.drawLine(x, 0, x, exp_height);
			x += x_int_min;
		}
		
		//Draw major ticks
		x = x_offset;
		int x_int_maj = ticksToPixels(major_tick_ival);
		g.setStroke(new BasicStroke(LINE_THICKNESS_MAJOR_TICK));
		while(x < end){
			g.drawLine(x, 0, x, exp_height);
			x += x_int_maj;
		}

		g.setColor(gclr);
		g.setStroke(gstr);
	}
	
	private void drawMarkers(Graphics2D g){
		if(markers.isEmpty()) return;
		Color gclr = g.getColor();
		Stroke gstr = g.getStroke();
		
		Rectangle bounds = g.getClipBounds();
		g.setStroke(new BasicStroke(LINE_THICKNESS_MARKER));
		for(Entry<Integer, Color> entry : markers.entrySet()){
			int x = x_offset + ticksToPixels(entry.getKey());
			g.setColor(entry.getValue());
			g.drawLine(x, 0, x, bounds.height);
		}
		
		g.setColor(gclr);
		g.setStroke(gstr);
	}
	
	public void paintComponent(Graphics g){
		Graphics2D g2 = (Graphics2D)g;
		
		//if(recalc_xbuff) recalculateMinimumLeftBuffer(g2);
		//if(repaint){}
		
		onRepaint(g2);
		
		super.paintComponent(g);
		
		drawTickMarks(g2);
		//drawEventMarkers(g2);
		drawMarkers(g2);	
		
		//Repaint components?
		//Component[] comps = this.getComponents();
		//for(Component c : comps) c.repaint();
	}

}
