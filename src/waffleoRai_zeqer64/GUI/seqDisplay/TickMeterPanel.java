package waffleoRai_zeqer64.GUI.seqDisplay;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JLabel;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.awt.BasicStroke;
import java.awt.Color;
import javax.swing.border.EtchedBorder;

class TickMeterPanel extends JPanel{
	
	//TODO Add a zoom level variable that says how often major ticks should have number label

	/*----- Constants -----*/
	
	private static final long serialVersionUID = -7149681131575356729L;
	
	public static final int WIDTH_DEFO = 100;
	public static final int HEIGHT = 47;
	
	public static final int MARKER_SIZE = 15;
	public static final int MARKER_BORDER = 2;
	
	public static final int TICKHEIGHT_MAJOR = 10;
	public static final int TICKHEIGHT_MINOR = 5;
	public static final int TICKFONT_SIZE = 10;
	public static final Font TICKNO_FONT = new Font("Arial", Font.PLAIN, TICKFONT_SIZE);
	
	public static final int MAX_ZOOM_LEVEL = DisplayTrack.TPP_ZOOM_VALS.length-1;
	
	/*----- Instance Variables -----*/
	
	private Map<Integer, Marker> markers;
	private boolean m_dirty;
	
	private int zoom_lvl = 6;
	private List<ZoomListener> zlisteners;
	
	private double resolution_tpp = AbstractEventPanel.DEFAULT_TPP;
	private int minor_interval = AbstractEventPanel.DEFAULT_MINOR_IVAL;
	private int major_interval = AbstractEventPanel.DEFAULT_MAJOR_IVAL;
	private int major_per_num = DisplayTrack.MARK_IVALS_NUM[6];
	private int x_offset = 0; //Number of pixels left of zero tick
	private int len_ticks = 0;
	
	/*----- Init -----*/
	
	public TickMeterPanel(){
		markers = new TreeMap<Integer, Marker>();
		zlisteners = new LinkedList<ZoomListener>();
		initGUI();
	}
	
	private void initGUI(){
		this.setMinimumSize(new Dimension(1, HEIGHT));
		this.setPreferredSize(new Dimension(WIDTH_DEFO, HEIGHT));
		
		setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{25, 20, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JPanel pnlMarkers = new JPanel();
		pnlMarkers.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		pnlMarkers.setBackground(Color.LIGHT_GRAY);
		GridBagConstraints gbc_pnlMarkers = new GridBagConstraints();
		gbc_pnlMarkers.insets = new Insets(0, 0, 2, 0);
		gbc_pnlMarkers.fill = GridBagConstraints.BOTH;
		gbc_pnlMarkers.gridx = 0;
		gbc_pnlMarkers.gridy = 0;
		add(pnlMarkers, gbc_pnlMarkers);
		
		JLabel lblTicks = new JLabel(){

			private static final long serialVersionUID = 2872542104834487613L;
			
			public void paintComponent(Graphics g){
				super.paintComponent(g);
				drawTicker(g);
			}
			
		};
		
		GridBagConstraints gbc_lblTicks = new GridBagConstraints();
		gbc_lblTicks.gridx = 0;
		gbc_lblTicks.gridy = 1;
		gbc_lblTicks.fill = GridBagConstraints.BOTH;
		add(lblTicks, gbc_lblTicks);
		
		lblTicks.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1){
					zoomIn();
				}
				else{
					zoomOut();
				}
			}

			public void mousePressed(MouseEvent e) {}

			public void mouseReleased(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			
		});
	}
	
	private void adjustMinSize(){
		int w = Math.max(WIDTH_DEFO, ticksToPixels(len_ticks) + x_offset);
		this.setMinimumSize(new Dimension(w, HEIGHT));
		this.setPreferredSize(new Dimension(w, HEIGHT));
	}
	
	/*----- Inner Classes -----*/
	
	protected class Marker{
		public String label;
		public Color color1;
		public Color color2;
		public int tickpos;
		
		public Marker(String txt, Color baseColor, int tick){
			label = txt;
			color1 = baseColor;
			color2 = baseColor.darker();
			tickpos = tick;
		}
		
		public JLabel createComp(){
			JLabel lbl = new JLabel(){

				private static final long serialVersionUID = -6636970649564929046L;

				public void paintComponent(Graphics g){
					super.paintComponent(g);
					
					Graphics2D g2 = (Graphics2D)g;
					Stroke gstr = g2.getStroke();
					Color gclr = g2.getColor();
					Rectangle bounds = g.getClipBounds();
					
					g2.setStroke(new BasicStroke(MARKER_BORDER));
					g2.setColor(color1);
					g2.fillRect(0, 0, bounds.width, bounds.height);
					g2.setColor(color2);
					g2.drawRect(0, 0, bounds.width, bounds.height);
					
					g2.setStroke(gstr);
					g2.setColor(gclr);
				}
			};
			lbl.setOpaque(false);
			lbl.setToolTipText(label + " (@ tick " + tickpos + ")");
			return lbl;
		}
	}
	
	protected class MarkerPanel extends JPanel{

		private static final long serialVersionUID = 7851033529352203839L;
		
		private Map<Integer, JLabel> comps; //Shortcut to component list
		
		public MarkerPanel(){
			comps = new TreeMap<Integer, JLabel>();
			this.setLayout(null);
		}
		
		public void paintComponent(Graphics g){
			if(m_dirty){
				//Delete all components and regenerate
				this.removeAll();
				comps.clear();
				for(Marker m : markers.values()){
					JLabel l = m.createComp();
					l.setBounds(0, 0, MARKER_SIZE, MARKER_SIZE);
					comps.put(m.tickpos, l);
					this.add(l);
				}
				m_dirty = false;
			}
			
			//Adjust component positions
			for(Integer t : comps.keySet()){
				JLabel l = comps.get(t);
				int x = x_offset;
				x += ticksToPixels(t);
				x -= MARKER_SIZE/2;
				l.setBounds(x, 0, MARKER_SIZE, MARKER_SIZE);
			}
			super.paintComponent(g);
		}
		
	}

	/*----- Getters -----*/
	
	/*----- Setters -----*/
	
	public void setResolution(double tpp){
		resolution_tpp = tpp;
	}
	
	public void setTickIntervals(int major, int minor){
		major_interval = major;
		minor_interval = minor;
	}
	
	public void setXOffset(int pixels){
		x_offset = pixels;
		adjustMinSize();
	}
	
	public void addMarker(int tick, String marker, Color color){
		Marker m = new Marker(marker, color, tick);
		markers.put(tick, m);
		m_dirty = true;
	}
	
	public void clearMarkers(){
		markers.clear();
		m_dirty = true;
	}
	
	public void setLengthInTicks(int ticks){
		if(ticks < 0) ticks = 0;
		len_ticks = ticks;
		adjustMinSize();
	}
	
	public void addZoomListener(ZoomListener l){
		zlisteners.add(l);
	}
	
	public void clearZoomListeners(){zlisteners.clear();}
	
	public boolean zoomIn(){
		if(zoom_lvl <= 0){zoom_lvl = 0; return false;}
		zoom_lvl--;
		resolution_tpp = DisplayTrack.TPP_ZOOM_VALS[zoom_lvl];
		minor_interval = DisplayTrack.MARK_IVALS_MINOR[zoom_lvl];
		major_interval = DisplayTrack.MARK_IVALS_MAJOR[zoom_lvl];
		major_per_num = DisplayTrack.MARK_IVALS_NUM[zoom_lvl];
		adjustMinSize();
		for(ZoomListener zl : zlisteners) zl.onZoomIn();
		//repaint();
		return true;
	}
	
	public boolean zoomOut(){
		if(zoom_lvl >= MAX_ZOOM_LEVEL){zoom_lvl = MAX_ZOOM_LEVEL; return false;}
		zoom_lvl++;
		resolution_tpp = DisplayTrack.TPP_ZOOM_VALS[zoom_lvl];
		minor_interval = DisplayTrack.MARK_IVALS_MINOR[zoom_lvl];
		major_interval = DisplayTrack.MARK_IVALS_MAJOR[zoom_lvl];
		major_per_num = DisplayTrack.MARK_IVALS_NUM[zoom_lvl];
		adjustMinSize();
		for(ZoomListener zl : zlisteners) zl.onZoomOut();
		//repaint();
		return true;
	}
	
	/*----- Paint -----*/
	
	private void drawTicker(Graphics g){
		Color gclr = g.getColor();
		Font gfnt = g.getFont();
		Rectangle bounds = g.getClipBounds();
		
		//System.err.println("lblTicks bounds: " + bounds.x + ", " + bounds.y + "," + bounds.width + "," + bounds.height);
		int x_start = bounds.x;
		int x_end = x_start + bounds.width;
		
		int x = x_offset;
		g.setColor(Color.BLACK);
		int pix_ival = ticksToPixels(minor_interval);
		int y = bounds.height - TICKHEIGHT_MINOR;
		while(x < x_start) x += pix_ival;
		while(x < x_end){
			//Draw minor ticks
			g.drawLine(x, y, x, bounds.height);
			x += pix_ival;
		}
		
		x = x_offset;
		int t = 0;
		pix_ival = ticksToPixels(major_interval);
		y = bounds.height - TICKHEIGHT_MAJOR;
		g.setFont(TICKNO_FONT);
		int mcount = 0;
		final int halffont = TICKFONT_SIZE/2;
		while(x < x_start) {x += pix_ival; t += major_interval; mcount++;}
		while(x < x_end){
			//Draw major ticks
			g.drawLine(x, y, x, bounds.height);
			
			//Number
			if((mcount++ % major_per_num) == 0){
				String istr = Integer.toString(t);
				int toff = halffont + ((istr.length()/2) * halffont);
				g.drawString(istr, x-toff, halffont+2);	
			}
			
			x += pix_ival;
			t += major_interval;
		}
		
		g.setColor(gclr);
		g.setFont(gfnt);
	}
	
	public int ticksToPixels(int ticks){
		int pix = (int)Math.round((double)ticks/resolution_tpp);
		return pix;
	}
	
}
