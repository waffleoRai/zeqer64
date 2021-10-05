package waffleoRai_zeqer64.GUI.seqDisplay;

import javax.swing.JPanel;
import java.awt.GridBagLayout;

import javax.swing.JScrollPane;
import javax.swing.JViewport;

import waffleoRai_SeqSound.n64al.NUSALSeqCommandMap;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.awt.Color;

class TrackDisplayPanel extends JPanel{
	
	/*----- Constants -----*/

	private static final long serialVersionUID = 1251299650631570033L;
	
	public static final int MAX_ZOOM_LEVEL = DisplayTrack.TPP_ZOOM_VALS.length-1;
	public static final int MAX_VZOOM_LEVEL = DisplayTrack.HEIGHTS_EVENTPNL.length-1;
	
	public static final int LABEL_COL_WIDTH = 150;
	
	/*----- Instance Variables -----*/
	
	private JPanel pnlLabels;
	private JPanel pnlTracks;
	private TickMeterPanel pnlMeter;
	
	private GridBagLayout gbl_labels;
	private GridBagLayout gbl_tracks;
	
	private int zoom_lvl = 6;
	private int vzoom_lvl = 1;
	private int ticklen = 0;
	
	private ArrayList<DisplayTrack> tracks;
	
	/*----- Init -----*/
	
	public TrackDisplayPanel(int track_count){
		tracks = new ArrayList<DisplayTrack>(track_count);
		initGUI();
	}
	
	private void initGUI(){
		GridBagLayout gbl = new GridBagLayout();
		gbl.columnWidths = new int[]{0, 0};
		gbl.rowHeights = new int[]{0, 0};
		gbl.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gbl);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		add(scrollPane, gbc_scrollPane);
		
		pnlLabels = new JPanel();
		pnlLabels.setMinimumSize(new Dimension(LABEL_COL_WIDTH, LABEL_COL_WIDTH));
		pnlLabels.setPreferredSize(new Dimension(LABEL_COL_WIDTH, LABEL_COL_WIDTH));
		scrollPane.setRowHeaderView(pnlLabels);
		scrollPane.getRowHeader().setOpaque(false);
		gbl_labels = new GridBagLayout();
		gbl_labels.columnWidths = new int[]{0};
		gbl_labels.rowHeights = new int[]{0};
		gbl_labels.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_labels.rowWeights = new double[]{Double.MIN_VALUE};
		pnlLabels.setLayout(gbl_labels);
		pnlLabels.setOpaque(false);
		
		pnlTracks = new JPanel();
		pnlTracks.setBackground(Color.WHITE);
		scrollPane.setViewportView(pnlTracks);
		gbl_tracks = new GridBagLayout();
		gbl_tracks.columnWidths = new int[]{0};
		gbl_tracks.rowHeights = new int[]{0};
		gbl_tracks.columnWeights = new double[]{Double.MIN_VALUE};
		gbl_tracks.rowWeights = new double[]{Double.MIN_VALUE};
		pnlTracks.setLayout(gbl_tracks);
		
		pnlMeter = new TickMeterPanel();
		scrollPane.setColumnHeaderView(pnlMeter);
		pnlMeter.setXOffset(DisplayTrack.XOFFS_VZOOM[1]);
		
		//This is less efficient, but blitting disallows any drawing outside
		//	clip region which resulted in pixels (esp in numbers) not being drawn.
		//	Annoying.
		scrollPane.getColumnHeader().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		
		pnlMeter.addZoomListener(new ZoomListener(){
			public void onZoomIn() {
				zoomIn();
			}
			public void onZoomOut() {
				zoomOut();
			}
		});
		
		/*this.addKeyListener(new KeyListener(){

			@Override
			public void keyTyped(KeyEvent e) {}

			@Override
			public void keyPressed(KeyEvent e) {
				//ATM check for ctrl+] (in) or ctrl+[ (out) for zooming
				if(e.isControlDown()){
					if(e.getKeyChar() == ']') {zoomIn(); repaint();}
					else if (e.getKeyChar() == '[') {zoomOut(); repaint();}
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {}
			
		});*/
	}

	private void addComponentToRow(JPanel pnl, Component c, int row){
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = row;
		pnl.add(c, gbc);
	}
	
	private void regrid(){
		pnlLabels.removeAll();
		pnlTracks.removeAll();
		
		int rcount = tracks.size() + 1;
		int[] rheights = new int[rcount];
		double[] rweights = new double[rcount];
		rweights[rcount-1] = Double.MIN_VALUE;
		
		gbl_labels.rowHeights = rheights;
		gbl_labels.rowWeights = rweights;
		gbl_tracks.rowHeights = rheights;
		gbl_tracks.rowWeights = rweights;
		
		int i = 0;
		int h = 0;
		for(DisplayTrack track : tracks){
			addComponentToRow(pnlLabels, track.getLabelPanel(), i);
			addComponentToRow(pnlTracks, track.getTimelinePanel(), i);
			h += track.getTrackHeight();
			i++;
		}
		
		//System.err.println("height = " + h);
		pnlLabels.setMinimumSize(new Dimension(LABEL_COL_WIDTH, h));
		pnlLabels.setPreferredSize(new Dimension(LABEL_COL_WIDTH, h));
	}
	
	/*----- Getters -----*/
	
	public DisplayTrack getTrack(int idx){
		if(idx < 0) return null;
		if(idx >= tracks.size()) return null;
		return tracks.get(idx);
	}
	
	/*----- Setters -----*/
	
	public DisplayTrack addTrack(String name, TrackColorScheme color, NUSALSeqCommandMap commands){
		DisplayTrack track = new DisplayTrack(color, commands);
		track.setInactive(false);
		track.setTrackName(name);
		tracks.add(track);
		regrid();
		return track;
	}
	
	public void clearTracks(){
		for(DisplayTrack track : tracks) track.dispose();
		tracks.clear();
		regrid();
		//repaint();
	}
	
	public void setMinimumTicks(int ticks){
		ticklen = ticks;
		pnlMeter.setLengthInTicks(ticklen);
		for(DisplayTrack track : tracks)track.setLengthInTicks(ticks);
	}
	
	/*----- Control -----*/
	
	public void zoomIn(){
		if(zoom_lvl <= 0){zoom_lvl = 0; return;}
		//pnlMeter.zoomIn();
		for(DisplayTrack track : tracks) track.zoomIn();
		zoom_lvl--;
		repaint();
	}
	
	public void zoomOut(){
		if(zoom_lvl >= MAX_ZOOM_LEVEL){zoom_lvl = MAX_ZOOM_LEVEL; return;}
		//pnlMeter.zoomOut();
		for(DisplayTrack track : tracks) track.zoomOut();
		zoom_lvl++;
		repaint();
	}
	
	public void zoomInVertical(){
		if(vzoom_lvl >= MAX_VZOOM_LEVEL){vzoom_lvl = MAX_VZOOM_LEVEL; return;}
		for(DisplayTrack track : tracks) track.zoomInVertical();
		vzoom_lvl++;
		repaint();
	}
	
	public void zoomOutVertical(){
		if(vzoom_lvl <= 0){vzoom_lvl = 0; return;}
		for(DisplayTrack track : tracks) track.zoomOutVertical();
		vzoom_lvl--;
		repaint();
	}
	
	/*----- Paint -----*/
	
	/*public void paintComponent(Graphics g){
		int xoff = 0;
		//First determine x offset...
		for(DisplayTrack track : tracks){
			track.setTimelineRepaintMode(false);
		}
		super.paintComponent(g);
		
		for(DisplayTrack track : tracks){
			int toff = track.getXBuffer();
			if(toff > xoff) xoff = toff;
			track.setTimelineRepaintMode(true);
		}
		
		System.err.println("xoff = " + xoff);
		pnlMeter.setXOffset(xoff);
		for(DisplayTrack track : tracks){
			track.setXBuffer(xoff);
		}
		
		super.paintComponent(g);
	}*/
	
}
