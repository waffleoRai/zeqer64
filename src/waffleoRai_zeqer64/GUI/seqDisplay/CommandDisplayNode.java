package waffleoRai_zeqer64.GUI.seqDisplay;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import waffleoRai_SeqSound.n64al.NUSALSeqCommand;

class CommandDisplayNode extends JButton{
	
	//TODO Make constructor derive icon and color level from command!

	/*----- Constants -----*/
	
	private static final long serialVersionUID = -7098041759358257063L;
	
	public static final int SHAPE_UNKNOWN = 0;
	public static final int SHAPE_DIAMOND = 1;
	public static final int SHAPE_CIRCLE = 2;
	public static final int SHAPE_EXT_RECTANGLE = 3;
	
	public static final int DEFO_DIM_MINI = 11;
	public static final int DEFO_DIM = 29;
	public static final int DEFO_BORDER = 3;
	public static final int ICON_MIN_DIM = 9;
	public static final int RR_ARC_MINI = 1;
	public static final int RR_ARC = 3;
	
	/*----- Instance Variable -----*/
	
	private NUSALSeqCommand command;
	private int tick_coord;
	
	private boolean isSelected;
	private boolean iconNoDraw;
	private boolean drawMini;
	
	private ZeqerColorScheme colorScheme = ZeqerColorSchemes.GREY;
	private ZeqerColorScheme activeColorScheme = ZeqerColorSchemes.GREY;
	private int borderThickness = DEFO_BORDER; //Default
	private int shade = 64;
	private int shapeType = SHAPE_DIAMOND; //Diamond, circle, rectangle (for notes/rests)
	private int alpha_shape = 255;
	//private int alpha_ico = 255;
	
	private IconDrawer icon;
	//private BufferedImage icon; //Optional.
	//private BufferedImage icon_alpha; //Icon with alpha applied
	
	//private Image scaled_ico;
	//private int scale_dim = -1;
	//private double aspect_ratio;
	
	//private List<ActionListener> alisteners;
	
	/*----- Initialization -----*/
	
	public CommandDisplayNode(NUSALSeqCommand cmd, int tick){
		command = cmd;
		tick_coord = tick;
		if(command != null){
			super.setToolTipText(command.toString() 
					+ " (@0x" + Integer.toHexString(command.getAddress())
					+ ", tick " + tick + ")");
		}
		//alisteners = new LinkedList<ActionListener>();
		
		/*this.addMouseListener(new MouseAdapter(){
			public void mousePressed(MouseEvent e){
				onClick();
			}
		});*/
		
		setOpaque(false);
		
		this.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onClick();
			}
		});
		super.setText("");
		super.setIcon(null);
		super.setBorderPainted(false);
		setMini(false); //Sets the minimum/preferred sizes
	}
	
	/*----- Getters -----*/
	
	public NUSALSeqCommand getCommand(){return command;}
	public int getTick(){return tick_coord;}
	public boolean isSelected(){return isSelected;}
	public boolean iconDrawSet(){return !iconNoDraw;}
	public boolean drawModeMini(){return drawMini;}
	public ZeqerColorScheme getColorScheme(){return colorScheme;}
	public int getBorderThickness(){return borderThickness;}
	public int getColorLevel(){return shade;}
	public int getShapeType(){return shapeType;}
	//public Image getIconImage(){return icon;}
	public int getMainAlpha(){return alpha_shape;}
	//public int getIconAlpha(){return alpha_ico;}
	
	/*----- Setters -----*/
	
	public void setSelected(boolean b){
		if(!isEnabled()) return;
		isSelected = b;
		repaint();
	}
	
	public void setEnabled(boolean b){
		super.setEnabled(b);
		if(b){
			activeColorScheme = colorScheme;
		}
		else{
			isSelected = false;
			activeColorScheme = ZeqerColorSchemes.GREY;
		}
		repaint();
	}
	
	public void setIconVisible(boolean b){
		iconNoDraw = !b;
	}
	
	public void setMini(boolean b){
		drawMini = b;
		
		if(b){
			setMinimumSize(new Dimension(DEFO_DIM_MINI, DEFO_DIM_MINI));
			setPreferredSize(new Dimension(DEFO_DIM_MINI, DEFO_DIM_MINI));
		}
		else{
			setMinimumSize(new Dimension(DEFO_DIM, DEFO_DIM));
			setPreferredSize(new Dimension(DEFO_DIM, DEFO_DIM));
		}
		
		repaint();
	}
	
	public void setColorScheme(ZeqerColorScheme color){
		if(color == null) color = ZeqerColorSchemes.GREY;
		colorScheme = color;
		if(this.isEnabled()) activeColorScheme = colorScheme;
	}
	
	public void setBorderThickness(int pixels){
		if(pixels < 0) pixels = 0;
		borderThickness = pixels;
	}
	
	public void setColorLevel(int level){
		if(level < 0) level = 0;
		if(level > 127) level = 127;
		shade = level;
	}
	
	public void setShapeType(int shape){
		if(shape < SHAPE_DIAMOND) return;
		if(shape > SHAPE_EXT_RECTANGLE) return;
		shapeType = shape;
	}
	
	/*public void setIconImage(BufferedImage img){
		icon = img;
		scaled_ico = null;
		scale_dim = -1;
		if(icon != null){
			aspect_ratio = (double)icon.getWidth()/(double)icon.getHeight();
		}
		else aspect_ratio = 0.0;
		alphaFilterIcon();
	}*/
	
	public void setIconDrawer(IconDrawer drawer){
		icon = drawer;
	}
	
	public void setMainAlpha(int value){
		if(value > 255) value = 255;
		if(value < 0) value = 0;
		alpha_shape = value;
	}
	
	/*public void setIconAlpha(int value){
		if(value > 255) value = 255;
		if(value < 0) value = 0;
		alpha_ico = value;
		alphaFilterIcon();
	}*/
	
	public void setText(String text){}
	public void setIcon(Icon icon){}
	
	/*----- Mouse Interface -----*/
	
	public void onClick(){
		//System.err.println("Node clicked: " + this.getToolTipText());
		if(!this.isEnabled()) return;
		setSelected(!isSelected);
		
		//System.err.println("Bounds Width: " + this.getBounds().width);
		//System.err.println("Bounds Height: " + this.getBounds().height);
	}
	
	/*----- GUI -----*/
	
	/*private void alphaFilterIcon(){
		scaled_ico = null;
		scale_dim = -1;
		if(icon == null) {icon_alpha = null; return;} 
		
		int w = icon.getWidth();
		int h = icon.getHeight();
		
		icon_alpha = new BufferedImage(icon.getWidth(), icon.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		for(int x = 0; x < w; x++){
			for(int y = 0; y < h; y++){
				int pix = icon.getRGB(x, y);
				if((pix & 0xFF000000) != 0){
					pix &= 0x00FFFFFF;
					pix |= (alpha_ico << 24);
				}
				icon_alpha.setRGB(x, y, pix);
			}
		}
	}*/
	
	private Rectangle drawShape(Graphics2D g){
		//Return is box where shape is drawn relative to Graphics g
		//	x,y,w,h
		
		//Find center of draw-able area
		//Rectangle cb = g.getClipBounds();
		Rectangle mybounds = this.getBounds();
		int width = mybounds.width;
		int height = mybounds.height;
		int cx = (int)Math.round(width/2);
		int cy = height/2;
		int amt = (height/2)-2;
		int side = amt << 1;
		
		Color borderColor = activeColorScheme.getDarkBorder();
		if(isSelected) borderColor = activeColorScheme.getLightBorder();
		Color fillColor = activeColorScheme.getShade(shade);
		if(alpha_shape < 255){
			Color c = borderColor;
			borderColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha_shape);
			 c = fillColor;
			fillColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha_shape);
		}
		
		g.setStroke(new BasicStroke(borderThickness));
		if(shapeType == SHAPE_DIAMOND){
			int[] xarr = null;
			int[] yarr = null;
			
			if(drawMini){
				//Size is fixed
				int coff = DEFO_DIM_MINI >>> 1;
				xarr = new int[]{cx, cx+coff, cx, cx-coff};
				yarr = new int[]{cy-coff, cy, cy+coff, cy};
				g.setColor(borderColor);
				g.fillPolygon(xarr, yarr, 4);
				return new Rectangle(cx-coff, cy-coff, DEFO_DIM_MINI, DEFO_DIM_MINI);
			}
			else{
				//Size is determined by drawing space
				//Dim needs to be odd. So if height is even, then subtract one row
				if(side % 2 != 1) {side--; amt--;}
				xarr = new int[]{cx, cx+amt, cx, cx-amt};
				yarr = new int[]{cy-amt, cy, cy+amt, cy};
				g.setColor(fillColor);
				g.fillPolygon(xarr, yarr, 4);
				g.setColor(borderColor);
				g.drawPolygon(xarr, yarr, 4);
				return new Rectangle(cx-amt, cy-amt, side, side);
			}
			
		}
		else if(shapeType == SHAPE_CIRCLE){
			if(drawMini){
				int coff = DEFO_DIM_MINI >>> 1;
				Rectangle icobox = new Rectangle(cx - coff, cy - coff, DEFO_DIM_MINI, DEFO_DIM_MINI);
				g.setColor(borderColor);
				g.fillOval(icobox.x, icobox.y, icobox.width, icobox.height);
				return icobox;
			}
			else{
				Rectangle icobox = new Rectangle(cx - amt, 0, side, side);
				g.setColor(fillColor);
				g.fillOval(icobox.x, icobox.y, icobox.width, icobox.height);
				
				int movein = borderThickness/2;
				g.setColor(borderColor);
				g.drawOval(icobox.x+movein, icobox.y+movein, 
						icobox.width - (movein<<1), icobox.height - (movein<<1));
				return icobox;
			}
		}
		else if(shapeType == SHAPE_EXT_RECTANGLE){
			//Fill whole width
			if(drawMini){
				int coff = DEFO_DIM_MINI >>> 1;
				Rectangle icobox = new Rectangle(0, cy-coff, width, cy+coff);
				g.setColor(borderColor);
				g.fillRoundRect(icobox.x, icobox.y, icobox.width, icobox.height, RR_ARC_MINI, RR_ARC_MINI);
				return icobox;
			}
			else{
				Rectangle icobox = new Rectangle(0,0,width,height);
				g.setColor(fillColor);
				g.fillRoundRect(icobox.x, icobox.y, icobox.width, icobox.height, RR_ARC, RR_ARC);
				
				int movein = borderThickness/2;
				int moveback = (movein << 1) + 1;
				g.setColor(borderColor);
				g.drawRoundRect(icobox.x + movein, icobox.y + movein, 
						icobox.width - moveback, icobox.height - moveback, RR_ARC, RR_ARC);
				return icobox;
			}
		}

		return null;
	}
	
	private void drawIcon(Graphics2D g, Rectangle box){
		//if(icon_alpha == null) return;
		if(icon == null) return;
		int side = box.height;
		if(side < ICON_MIN_DIM) return;

		if(shapeType == SHAPE_DIAMOND || shapeType == SHAPE_CIRCLE){
			int offset = side + (borderThickness << 1);
			offset >>>= 2;
			int iside = side - (offset << 1);
			if(iside < ICON_MIN_DIM) return;
			icon.updateSize(iside);
			
			int hw = icon.getRequiredWidth() >>> 1;
			int xc = box.x + (box.width/2);
			
			//See if need to re-render scaled icon
			/*if(scale_dim != iside){
				int w = (int)Math.round((double)iside * aspect_ratio);
				scale_dim = iside;
				scaled_ico = icon_alpha.getScaledInstance(w, iside, Image.SCALE_DEFAULT);
			}*/
			
			//Draw Icon
			//g.drawImage(scaled_ico, box.x + offset, box.y+offset, null);
			icon.drawIcon(g, xc - hw, box.y+offset);
		}
		else if(shapeType == SHAPE_EXT_RECTANGLE){
			int t2 = borderThickness << 1;
			int iside = side - t2;
			if(iside < ICON_MIN_DIM) return;
			icon.updateSize(iside);
			
			//Determine available width
			final int leftbuff = 2;
			int x = borderThickness + leftbuff;
			int w = box.width - t2 - leftbuff;
			if(w < ICON_MIN_DIM) return;
			if(w < icon.getRequiredWidth()) return;
			
			/*if(scale_dim != iside){
				w = (int)Math.round((double)iside * aspect_ratio);
				if(w < ICON_MIN_DIM) return;
				scale_dim = iside;
				scaled_ico = icon_alpha.getScaledInstance(w, iside, Image.SCALE_DEFAULT);
			}*/
			
			//g.drawImage(scaled_ico, box.x + x, box.y+borderThickness, null);
			icon.drawIcon(g, box.x + x, box.y+borderThickness);
		}
	}
	
	public void paintComponent(Graphics g){
		//super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D)g;
		Color gclr = g2.getColor();
		Stroke gstr = g2.getStroke();
		
		Rectangle box = drawShape(g2);
		if(!iconNoDraw && !drawMini) drawIcon(g2, box);
		
		g2.setColor(gclr);
		g2.setStroke(gstr);
	}
	
}
