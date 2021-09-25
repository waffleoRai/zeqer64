package waffleoRai_zeqer64.GUI.seqDisplay;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JPanel;

import waffleoRai_SeqSound.n64al.NUSALSeqCommand;

import java.awt.GridBagLayout;
import java.awt.Rectangle;

import java.awt.GridBagConstraints;

class TrackEventPanel extends JPanel{

	//min/preferred size should be recalculated whenever:
	//	- Ticks per pixel (zoom level) is set or
	//	- A new command is added that changes the track length
	
	/*----- Constants -----*/
	
	private static final long serialVersionUID = 7333078946589654233L;

	/*----- Instance Variables -----*/
	
	private Map<Integer, List<CommandDisplayNode>> nodes;
	
	private TrackColorScheme color;
	private ZeqerColorScheme node_color;
	private Color track_bkg_color;
	private Color tick_color;
	
	private GridBagLayout layout;
	
	private int max_per_tick = 3; //Max nodes visible on a single tick.
	private int major_tick_ival = 24 * 8;
	private int minor_tick_ival = 24;
	private double ticks_per_pixel = 4.0;
	private int leftbuff_pix; //# pixels to draw left of zero tick (so can leave room for event buttons at 0)
	private int len_ticks; //Track length in ticks. Determines minimum width of component.
	
	private Set<Integer> markers; //Lines to draw extra thick (like for entry and loop points)
	private Color marker_color;
	
	/*----- Initialization -----*/
	
	public TrackEventPanel(){
		markers = new TreeSet<Integer>();
		nodes = new TreeMap<Integer, List<CommandDisplayNode>>();
		
		initGUI();
	}
	
	private void initGUI(){
		marker_color = Color.BLACK;
		setColorScheme(TrackColorScheme.GREY);
		
		layout = new GridBagLayout();
		layout.columnWidths = new int[]{0, 0, 0, 0};
		layout.rowHeights = new int[]{0, 0, 0, 0};
		layout.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		layout.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(layout);
	}
	
	/*----- Inner Classes -----*/
	
	private static class NodeDrawGroup{
		
		private int xoff;
		private LinkedList<CommandDisplayNode> nodes;
		
		public NodeDrawGroup(){
			xoff = 0;
			nodes = new LinkedList<CommandDisplayNode>();
		}
		
	}
	
	/*----- Getters -----*/
	
	/*----- Setters -----*/
	
	public void setColorScheme(TrackColorScheme clr){
		if(clr == null) clr = TrackColorScheme.GREY;
		color = clr;
		node_color = color.getNodeColorScheme();
		track_bkg_color = color.getBackgroundColor();
		tick_color = color.getTickMarkColor();
		
		//Update node colors...
		for(List<CommandDisplayNode> tgroup : nodes.values()){
			if(tgroup == null) continue; //Don't think that should be possible, but...
			for(CommandDisplayNode node : tgroup) node.setColorScheme(node_color);
		}
	}
	
	public void setNodeRowCount(int count){
		max_per_tick = count;
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
	
	public void setGlobalMarkerColor(Color c){
		if(c == null) return;
		marker_color = c;
	}
	
	public void addGlobalMarkerAt(int tick){
		markers.add(tick);
	}
	
	public void addEvent(int tick, NUSALSeqCommand cmd){
		if(cmd == null) return;
		if(tick < 0) return;
		
		List<CommandDisplayNode> tgroup = nodes.get(tick);
		if(tgroup == null){
			tgroup = new LinkedList<CommandDisplayNode>();
			nodes.put(tick, tgroup);
		}
		
		//Wrap in display node...
		CommandDisplayNode dn = new CommandDisplayNode(cmd, tick);
		dn.setColorScheme(node_color);
		tgroup.add(dn);
		
		//See if need to adjust end.
		int end = tick + cmd.getSizeInTicks();
		if(end > len_ticks) {len_ticks = end; recalculateMinimumWidth();}
	}
	
	/*----- Util -----*/
	
	private void recalculateMinimumWidth(){
		final int buffer = 10; //Arbitrary buffer for marker. Fine if too high - will just extend track to the right.
		final int min_height = 10;
		int w = buffer + ticksToPixels(len_ticks);
		setMinimumSize(new Dimension(w, min_height));
		//setPreferredSize(new Dimension(w, min_height));
	}
	
	private int eventMarkerSize(Graphics g){
		return g.getClipBounds().height / max_per_tick;
	}
	
	private int pixelsToTicks(int pix){
		int ticks = (int)Math.round((double)pix * ticks_per_pixel);
		return ticks;
	}
	
	private int ticksToPixels(int ticks){
		int pix = (int)Math.round((double)ticks/ticks_per_pixel);
		return pix;
	}
	
	/*----- Drawing -----*/
	
	private LinkedList<NodeDrawGroup> clusterNodes(int node_sz){
		LinkedList<NodeDrawGroup> out = new LinkedList<NodeDrawGroup>();
		LinkedList<Integer> tickcoords = new LinkedList<Integer>();
		tickcoords.addAll(nodes.keySet());
		Collections.sort(tickcoords);
		
		while(!tickcoords.isEmpty()){
			int grouploc = tickcoords.pop();
			NodeDrawGroup group = new NodeDrawGroup();
			group.xoff = leftbuff_pix + ticksToPixels(grouploc);
			
			int added = 0;
			//Add events from this tick.
			List<CommandDisplayNode> egroup = nodes.get(grouploc);
			MultiCmdDisplayNode multi = null;
			for(CommandDisplayNode n : egroup){
				if(added < max_per_tick-1){
					group.nodes.add(n);
				}
				else{
					if(multi == null){
						multi = new MultiCmdDisplayNode(grouploc);
						group.nodes.add(multi);
					}
				}
				added++;
			}
			
			//Determine how many ticks will be clustered into this group
			int end_tick = pixelsToTicks(node_sz) + grouploc;
			
			//Pull ticks and add events from that tick until outside cluster range
			while(!tickcoords.isEmpty() && tickcoords.peek() < end_tick){
				int mytick = tickcoords.pop();
				egroup = nodes.get(mytick);
				for(CommandDisplayNode n : egroup){
					if(added < max_per_tick-1){
						group.nodes.add(n);
					}
					else{
						if(multi == null){
							multi = new MultiCmdDisplayNode(mytick);
							group.nodes.add(multi);
						}
					}
					added++;
				}
			}
			
			//Add group to output list
			out.add(group);
		}
		
		return out;
	}

	private void positionEventMarker(int gb_x, int gb_y, CommandDisplayNode node){
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = gb_x;
		gbc.gridy = gb_y;
		add(node, gbc);
	}
	
	private void drawEventMarkers(Graphics g){
		if(nodes.isEmpty()) return;
		
		//Cluster the nodes
		int msz = eventMarkerSize(g);
		LinkedList<NodeDrawGroup> drawgroups = clusterNodes(msz);
		int drawgroup_count = drawgroups.size();
		
		//Calculate gridbag columns and rows needed
		int rows = max_per_tick;
		int cols = drawgroup_count << 1;
		
		//Determine if need left buffer
		boolean buffer_left = !nodes.containsKey(0);
		if(buffer_left) cols++;
		
		//Do row weights
		int[] iarr = new int[rows+1];
		double[] darr = new double[rows+1];
		for(int i = 0; i < max_per_tick; i++){
			iarr[i] = msz;
			darr[i] = 0.0;
		}
		iarr[iarr.length-1] = 0;
		darr[darr.length-1] = Double.MIN_VALUE;
		layout.rowHeights = iarr;
		layout.rowWeights = darr;
		
		//Do column weights
		LinkedList<NodeDrawGroup> templist = new LinkedList<NodeDrawGroup>();
		iarr = new int[cols+1];
		darr = new double[cols+1];
		int l = 0;
		if(buffer_left){
			iarr[0] = drawgroups.peek().xoff;
			darr[0] = 0.0;
			l++;
		}
		while(!drawgroups.isEmpty()){
			//Node column
			iarr[l] = msz; darr[l] = 0.0;
			l++;
			
			//Buffer column
			NodeDrawGroup dg = drawgroups.pop();
			templist.add(dg);
			if(!drawgroups.isEmpty()){
				int x0 = dg.xoff;
				int x1 = drawgroups.peek().xoff;
				
				iarr[l] = x1 - x0 - msz; darr[l] = 0.0;
				l++;
			}
			else{
				//Last
				iarr[l] = 0; darr[l] = 1.0;
				l++;
			}
		}
		
		//Adjust last buffer column to be filler
		iarr[iarr.length-1] = 0;
		darr[darr.length-1] = Double.MIN_VALUE;
		layout.columnWidths = iarr;
		layout.columnWeights = darr;

		//Add nodes
		l = 0; int r = 0;
		if(buffer_left) l++;
		while(!templist.isEmpty()){
			NodeDrawGroup dg = drawgroups.pop();
			
			for(CommandDisplayNode n : dg.nodes){
				if(r >= max_per_tick) break; //Shouldn't occur, but ignores remaining if it does
				positionEventMarker(l, r, n);
				r++;
			}
			
			l+=2;
		}
		
	}
	
	private void drawTickMarks(Graphics g){
		//Calculate left buffer (not hard)
		Rectangle bounds = g.getClipBounds(); //Not sure if this is what to use here
		leftbuff_pix = bounds.height/2+1;
		
		//Calculate # of ticks that can be drawen in this window
		int w = bounds.width - leftbuff_pix;
		int total_ticks = (int)Math.round((double)w/ticks_per_pixel);
		
		//Draw background
		Color clr = g.getColor();
		g.setColor(track_bkg_color);
		g.fillRect(0, 0, bounds.width, bounds.height);
		
		//Draw tick lines
		g.setColor(tick_color);
		
		//Major ticks
		int x = leftbuff_pix;
		int t = 0;
		int tickpix = (int)Math.round((double)major_tick_ival/ticks_per_pixel);
		while(t < total_ticks){
			g.fillRect(x, 0, 2, bounds.height);
			t += major_tick_ival;
			x += tickpix;
		}
		
		//Minor ticks
		x = leftbuff_pix;
		t = 0;
		tickpix = (int)Math.round((double)minor_tick_ival/ticks_per_pixel);
		while(t < total_ticks){
			g.drawLine(x, 0, x, bounds.height);
			t += minor_tick_ival;
			x += tickpix;
		}
		g.setColor(clr);
	}
	
	private void drawMarkers(Graphics g){
		Color clr = g.getColor();
		g.setColor(marker_color);
		for(Integer m : markers){
			int xpos = ticksToPixels(m);
			xpos += leftbuff_pix;
			g.drawRect(xpos, 0, 2, g.getClipBounds().height);
		}
		g.setColor(clr);
	}
	
	public void paintComponent(Graphics g){
		super.removeAll();
		
		drawTickMarks(g);
		drawEventMarkers(g);
		drawMarkers(g);
		
		//super.paintComponent(g);
	}
	
}
