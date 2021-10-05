package waffleoRai_zeqer64.GUI.seqDisplay;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import waffleoRai_zeqer64.GUI.ScaledImage;

class TrackLabelPanel extends JPanel{

	private static final long serialVersionUID = -4729053967333166157L;
	
	public static final int MAX_ZOOM_LVL = DisplayTrack.HEIGHTS_EVENTPNL.length-1;
	public static final Font FONT_TRACKNAME = new Font("Arial", Font.BOLD, 18);
	
	private GridBagLayout layout;
	
	private TrackColorScheme color_primary;
	private TrackColorScheme color_secondary;
	
	private int zoom_lvl = 1;
	
	private int vox_count;
	private ArrayList<LevelPanelType> lvl_types;
	
	private TrackLabel trackNameLabel;
	private List<Subpanel> subpanels; //For color updates
	
	private Color alpha_bkg;
	private Color alpha_tlbl;
	private Color alpha_llbl;
	private Color alpha_vlbl;
	private Image tlbl_bkg;
	private Image vlbl_bkg;

	public TrackLabelPanel(){
		subpanels = new LinkedList<Subpanel>();
		lvl_types = new ArrayList<LevelPanelType>(4);
		color_primary = color_secondary = TrackColorScheme.GREY;
		initGUI();
	}
	
	private void initGUI(){
		updateCompColors();
		
		layout = new GridBagLayout();
		layout.columnWidths = new int[]{0, DisplayTrack.HEIGHTS_LEVELPNL[zoom_lvl], DisplayTrack.HEIGHTS_LEVELPNL[zoom_lvl], 0};
		layout.rowHeights = new int[]{0};
		//layout.columnWeights = new double[]{3.0, 1.0, Double.MIN_VALUE};
		layout.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		layout.rowWeights = new double[]{Double.MIN_VALUE};
		setLayout(layout);
		
		trackNameLabel = new TrackLabel();
		addComponentToGrid(trackNameLabel, 0, 0);
		subpanels.add(trackNameLabel);
		regrid();
	}
	
	private void addComponentToGrid(Component c, int row, int type){
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridy = row;
		if(type == 1){ //Level
			gbc.gridx = 2;
			gbc.gridwidth = 1;
		}
		else if(type == 2){ //Vox
			gbc.gridx = 1;
			gbc.gridwidth = 2;
		}
		else{ //Event
			gbc.gridx = 0;
			gbc.gridwidth = 3;
		}
		add(c, gbc);
	}
	
	private void forceComponentSize(Component c, int height){
		c.setMinimumSize(new Dimension(30, height));
		c.setPreferredSize(new Dimension(30, height));
	}
	
	private void regrid(){
		super.removeAll();
		subpanels.clear();
		tlbl_bkg = null;
		vlbl_bkg = null;
		
		int lcount = lvl_types.size();
		int rcount = 1 + lcount + vox_count + 1;
		
		int[] heights = new int[rcount]; //Just so alloc # matches. All zero
		double[] weights = new double[rcount];
		
		weights[rcount-1] = Double.MIN_VALUE;
		
		int r = 0;
		int h = 0;
		//weights[r++] = DisplayTrack.ROW_WEIGHT_EVENTS;
		heights[r++] = DisplayTrack.HEIGHTS_EVENTPNL[zoom_lvl];
		h += DisplayTrack.HEIGHTS_EVENTPNL[zoom_lvl];
		for(int i = 0; i < lcount; i++){
			//weights[r++] = DisplayTrack.ROW_WEIGHT_LEVEL;
			heights[r++] = DisplayTrack.HEIGHTS_LEVELPNL[zoom_lvl];
			h += DisplayTrack.HEIGHTS_LEVELPNL[zoom_lvl];
		}
		for(int i = 0; i < vox_count; i++){
			//weights[r++] = DisplayTrack.ROW_WEIGHT_VOX;
			heights[r++] = DisplayTrack.HEIGHTS_VOXPNL[zoom_lvl];
			h += DisplayTrack.HEIGHTS_VOXPNL[zoom_lvl];
		}
		
		layout.rowHeights = heights;
		layout.rowWeights = weights;
		
		r = 0;
		addComponentToGrid(trackNameLabel, r++, 0);
		forceComponentSize(trackNameLabel, DisplayTrack.HEIGHTS_EVENTPNL[zoom_lvl]);
		for(int i = 0; i < lcount; i++){
			LevelLabel lbl = new LevelLabel();
			LevelPanelType type = lvl_types.get(i);
			BufferedImage img = LevelPanelType.getIconFor(type);
			if(img != null){
				//lbl.setIcon(new ImageIcon(img));
				lbl.icon = img;
			}
			else{
				lbl.setText(type.name().substring(0,1));
			}
			lbl.setToolTipText(type.name());
			addComponentToGrid(lbl, r++, 1);
			forceComponentSize(lbl, DisplayTrack.HEIGHTS_LEVELPNL[zoom_lvl]);
			subpanels.add(lbl);
		}
		for(int i = 0; i < vox_count; i++){
			VoiceLabel lbl = new VoiceLabel();
			lbl.setText("Voice " + i);
			addComponentToGrid(lbl, r++, 2);
			forceComponentSize(lbl, DisplayTrack.HEIGHTS_VOXPNL[zoom_lvl]);
			subpanels.add(lbl);
		}
		
		this.setMinimumSize(new Dimension(5, h));
		this.setPreferredSize(new Dimension(5, h));
		repaint();
	}
	
	private void updateCompColors(){
		ZeqerColorScheme nodescheme = color_primary.getNodeColorScheme();
		setBackground(nodescheme.getShade(100));
		setForeground(nodescheme.getShade(30));
		setBorder(BorderFactory.createEtchedBorder(
				nodescheme.getLightBorder(), nodescheme.getDarkBorder()));
		
		//Set gradient background overlay color
		Color clr = nodescheme.getShade(100);
		alpha_bkg = new Color(clr.getRed(), clr.getGreen(), clr.getBlue(), 100);
		alpha_tlbl = new Color(clr.getRed(), clr.getGreen(), clr.getBlue(), 150);
		alpha_llbl = new Color(clr.getRed(), clr.getGreen(), clr.getBlue(), 120);
		
		clr = color_secondary.getNodeColorScheme().getShade(100);
		alpha_vlbl = new Color(clr.getRed(), clr.getGreen(), clr.getBlue(), 120);
	}
	
	protected interface Subpanel{
		public void updateColors();
	}
	
	protected class TrackLabel extends JLabel implements Subpanel{

		private static final long serialVersionUID = -1267609555287865171L;
		
		public TrackLabel(){
			/*int dim = DisplayTrack.DEFO_HEIGHT_EVENTPNL;
			this.setMinimumSize(new Dimension(0, dim));
			this.setPreferredSize(new Dimension(0, dim));*/
			//this.setOpaque(true);
			this.setHorizontalAlignment(SwingConstants.CENTER);
			setFont(FONT_TRACKNAME);
			updateColors();
		}
		
		public void updateColors(){
			ZeqerColorScheme nodescheme = color_primary.getNodeColorScheme();
			setBackground(nodescheme.getShade(100));
			setForeground(nodescheme.getShade(30));
			setBorder(BorderFactory.createEtchedBorder(
					nodescheme.getLightBorder(), nodescheme.getDarkBorder()));
		}
		
		public void paintComponent(Graphics g){
			//super.paintComponent(g);
			//Graphics2D g2 = (Graphics2D)g;
			
			Rectangle bounds = this.getBounds();
			if(tlbl_bkg == null){
				ScaledImage gradient_raw = BackgroundFactory.getScaledImage(BackgroundFactory.GRADIENT_OUT_1, 256);
				tlbl_bkg = gradient_raw.getImage().getScaledInstance(bounds.width, 
						DisplayTrack.HEIGHTS_EVENTPNL[zoom_lvl], Image.SCALE_DEFAULT);
			}
			
			if(tlbl_bkg == null){
				super.paintComponent(g);
				return;
			}
			
			g.drawImage(tlbl_bkg, 0, 0, null);
			
			Color gclr = g.getColor();
			g.setColor(alpha_tlbl);
			g.fillRect(0, 0, bounds.width, bounds.height);
			
			g.setColor(gclr);
			
			super.paintComponent(g);
		}
		
	}
	
	protected class LevelLabel extends JLabel implements Subpanel{
		
		private static final long serialVersionUID = 3905009875095181894L;

		private BufferedImage icon;
		
		private int last_dim;
		private Image icon_scaled;
		
		public LevelLabel(){
			//int dim = DisplayTrack.DEFO_HEIGHT_LEVELPNL;
			//this.setMinimumSize(new Dimension(dim, dim));
			//this.setPreferredSize(new Dimension(dim, dim));
			setHorizontalAlignment(SwingConstants.CENTER);
			updateColors();
		}
		
		public void updateColors(){
			ZeqerColorScheme nodescheme = color_primary.getNodeColorScheme();
			setBackground(nodescheme.getShade(100));
			setForeground(nodescheme.getShade(30));
			setBorder(BorderFactory.createEtchedBorder(
					nodescheme.getLightBorder(), nodescheme.getDarkBorder()));
		}
		
		public void paintComponent(Graphics g){
			super.paintComponent(g);
			
			Rectangle bounds = this.getBounds();
			int dim = bounds.height;
			
			ScaledImage gradient = BackgroundFactory.getScaledImage(BackgroundFactory.GRADIENT_RADIALOUT_1, dim);
			if(gradient == null){
				super.paintComponent(g);
				return;
			}
			g.drawImage(gradient.getImage(), 0, 0, null);
			
			Color gclr = g.getColor();
			g.setColor(alpha_llbl);
			g.fillRect(0, 0, dim, dim);
			g.setColor(gclr);
			
			if(icon == null){
				super.paintComponent(g);
				return;
			}
			
			if(dim != last_dim){
				icon_scaled = icon.getScaledInstance(dim, dim, Image.SCALE_DEFAULT);
				last_dim = dim;
			}
			int xc = bounds.width/2;
			int x = xc - (dim/2);
			if(x < 0) x = 0;
			g.drawImage(icon_scaled, x, 0, null);
			
			super.paintComponent(g);
		}
	}
	
	protected class VoiceLabel extends JLabel implements Subpanel{

		private static final long serialVersionUID = 3625656911628232119L;

		public VoiceLabel(){
			//int dim = DisplayTrack.DEFO_HEIGHT_VOXPNL;
			//this.setMinimumSize(new Dimension(0, dim));
			//this.setPreferredSize(new Dimension(0, dim));
			setHorizontalAlignment(SwingConstants.CENTER);
			updateColors();
			//this.setOpaque(true);
		}
		
		public void updateColors(){
			ZeqerColorScheme nodescheme = color_secondary.getNodeColorScheme();
			setBackground(nodescheme.getShade(100));
			setForeground(nodescheme.getShade(30));
			setBorder(BorderFactory.createEtchedBorder(
					nodescheme.getLightBorder(), nodescheme.getDarkBorder()));
		}
		
		public void paintComponent(Graphics g){
			//TODO
			Rectangle bounds = this.getBounds();
			if(vlbl_bkg == null){
				ScaledImage gradient_raw = BackgroundFactory.getScaledImage(BackgroundFactory.GRADIENT_OUT_1, 256);
				vlbl_bkg = gradient_raw.getImage().getScaledInstance(bounds.width, 
						DisplayTrack.HEIGHTS_VOXPNL[zoom_lvl], Image.SCALE_DEFAULT);
			}
			
			if(vlbl_bkg == null){
				super.paintComponent(g);
				return;
			}
			
			g.drawImage(vlbl_bkg, 0, 0, null);
			
			Color gclr = g.getColor();
			g.setColor(alpha_vlbl);
			g.fillRect(0, 0, bounds.width, bounds.height);
			
			g.setColor(gclr);
			
			super.paintComponent(g);
		}
	}
	
	public void setPrimaryColorScheme(TrackColorScheme color){
		if(color == null) color = TrackColorScheme.GREY;
		color_primary = color;
		
		updateCompColors();
		trackNameLabel.updateColors();
		for(Subpanel pnl : subpanels) pnl.updateColors();
	}
	
	public void setSecondaryColorScheme(TrackColorScheme color){
		if(color == null) color = TrackColorScheme.GREY;
		color_secondary = color;
		
		updateCompColors();
		for(Subpanel pnl : subpanels) pnl.updateColors();
	}
	
	public void setGrey(){
		setPrimaryColorScheme(TrackColorScheme.GREY);
		setSecondaryColorScheme(TrackColorScheme.GREY);
	}
	
	public void setVoiceCount(int n){
		if(n < 0) n = 0;
		if(n > 4) n = 4;
		vox_count = n;
		regrid();
	}
	
	public void addLevelPaneType(LevelPanelType ltype){
		lvl_types.add(ltype);
		regrid();
	}
	
	public void clearLevelPaneLabels(){
		lvl_types.clear();
		regrid();
	}
	
	public String getTrackName(){return this.trackNameLabel.getText();}
	
	public void setTrackName(String s){
		trackNameLabel.setText(s);
	}
	
	public void zoomInVertical(){
		if(zoom_lvl >= MAX_ZOOM_LVL){zoom_lvl = MAX_ZOOM_LVL; return;}
		zoom_lvl++;
		regrid();
		repaint();
	}
	
	public void zoomOutVertical(){
		if(zoom_lvl <= 0){zoom_lvl = 0; return;}
		zoom_lvl--;
		regrid();
		repaint();
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		
		Rectangle bounds = this.getBounds();
		int dim = bounds.width;
		if(bounds.height > dim) dim = bounds.height;
		
		ScaledImage gradient = BackgroundFactory.getScaledImage(BackgroundFactory.GRADIENT_RADIALOUT_1, dim);
		if(gradient == null) return;
		g.drawImage(gradient.getImage(), 0, 0, null);
		
		Color gclr = g.getColor();
		g.setColor(alpha_bkg);
		g.fillRect(0, 0, dim, dim);
		g.setColor(gclr);
	}
	
}
