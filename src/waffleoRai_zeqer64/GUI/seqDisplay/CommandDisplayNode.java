package waffleoRai_zeqer64.GUI.seqDisplay;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import waffleoRai_SeqSound.n64al.NUSALSeqCommand;

class CommandDisplayNode extends JButton{

	/*----- Constants -----*/
	
	private static final long serialVersionUID = -7098041759358257063L;
	
	public static final int SHAPE_UNKNOWN = 0;
	public static final int SHAPE_DIAMOND = 1;
	public static final int SHAPE_CIRCLE = 2;
	public static final int SHAPE_EXT_RECTANGLE = 3;

	/*----- Instance Variable -----*/
	
	private NUSALSeqCommand command;
	private int tick_coord;
	
	private boolean isSelected;
	private boolean iconNoDraw;
	
	private ZeqerColorScheme colorScheme = ZeqerColorSchemes.GREY;
	private ZeqerColorScheme activeColorScheme = ZeqerColorSchemes.GREY;
	private int borderThickness = 2; //Default
	private int shade = 64;
	private int shapeType = SHAPE_CIRCLE; //Diamond, circle, rectangle (for notes/rests)
	
	private Image icon; //Optional.
	
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
		
		this.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onClick();
			}
		});
		super.setText("");
		super.setIcon(null);
		super.setBorderPainted(false);
	}
	
	/*----- Getters -----*/
	
	public NUSALSeqCommand getCommand(){return command;}
	public int getTick(){return tick_coord;}
	public boolean isSelected(){return isSelected;}
	public boolean iconDrawSet(){return !iconNoDraw;}
	public ZeqerColorScheme getColorScheme(){return colorScheme;}
	public int getBorderThickness(){return borderThickness;}
	public int getColorLevel(){return shade;}
	public int getShapeType(){return shapeType;}
	public Image getIconImage(){return icon;}
	
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
	
	public void setColorScheme(ZeqerColorScheme color){
		if(color == null) color = ZeqerColorSchemes.GREY;
		colorScheme = color;
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
	
	public void setIconImage(Image img){
		icon = img;
	}
	
	public void setText(String text){}
	public void setIcon(Icon icon){}
	
	/*----- Mouse Interface -----*/
	
	public void onClick(){
		if(!this.isEnabled()) return;
		this.setSelected(!isSelected);
	}
	
	/*----- GUI -----*/
	
	private Rectangle drawShape(Graphics g){
		//Return is box where shape is drawn relative to Graphics g
		//	x,y,w,h
		
		//Find center of draw-able area
		Rectangle cb = g.getClipBounds();
		int cx = (int)Math.round(cb.getWidth()/2);
		int height = cb.height;
		int amt = height/2;
		int side = amt << 1;
		
		Color clr = g.getColor();
		Color borderColor = activeColorScheme.getDarkBorder();
		if(isSelected) borderColor = activeColorScheme.getLightBorder();
		Color fillColor = activeColorScheme.getShade(shade);
		
		if(shapeType == SHAPE_DIAMOND){
			int cy = height/2;
			int[] xarr = new int[4];
			int[] yarr = new int[4];
			xarr[0] = xarr[2] = cx;
			xarr[1] = cx + amt;
			xarr[3] = cx - amt;
			yarr[0] = 0; yarr[2] = height;
			yarr[1] = yarr[3] = cy;
			g.setColor(borderColor);
			g.fillPolygon(xarr, yarr, 4);
			xarr[1] -= borderThickness;
			xarr[3] -= borderThickness;
			yarr[0] -= borderThickness;
			yarr[2] -= borderThickness;
			g.setColor(fillColor);
			g.fillPolygon(xarr, yarr, 4);
			g.setColor(clr);
			
			return new Rectangle(cx - amt,0,side,side);
		}
		else if(shapeType == SHAPE_CIRCLE){
			g.setColor(borderColor);
			g.fillOval(cx-amt, 0, side, side);
			int sideshrink = borderThickness << 1;
			g.setColor(fillColor);
			g.fillOval(cx-amt+borderThickness, borderThickness, side-sideshrink, side-sideshrink);
			g.setColor(clr);
			
			return new Rectangle(cx - amt,0,side,side);
		}
		else if(shapeType == SHAPE_EXT_RECTANGLE){
			//Fill whole area
			g.setColor(borderColor);
			g.fillRect(0, 0, cb.width, cb.height);
			int sideshrink = borderThickness << 1;
			g.setColor(fillColor);
			g.fillRect(borderThickness, borderThickness, cb.width-sideshrink, cb.height-sideshrink);
			g.setColor(clr);
			
			return new Rectangle(cx - amt,0,side,side);
		}

		return null;
	}
	
	private void drawIcon(Graphics g, Rectangle box){

		if(icon == null) return;
		int side = box.height;

		if(shapeType == SHAPE_DIAMOND){
			//Recalculate box inside diamond.
			int halfside = side/2;
			if(halfside < 8) return;
			halfside -= borderThickness;
			int qside = halfside/2;
			int cx = (box.width / 2) + box.x;
			
			int x0 = cx - qside;
			int y0 = (box.height/2) - qside;
			Image scaled_icon = icon.getScaledInstance(halfside, halfside, Image.SCALE_DEFAULT);
			g.drawImage(scaled_icon, x0, y0, null);
		}
		else if(shapeType == SHAPE_CIRCLE){
			int buff = 1;
			side -= (borderThickness + buff) << 1;
			if(side < 8) return;
			int y0 = borderThickness + buff;
			int x0 = box.x + borderThickness + buff;
			Image scaled_icon = icon.getScaledInstance(side, side, Image.SCALE_DEFAULT);
			g.drawImage(scaled_icon, x0, y0, null);
		}
		else if(shapeType == SHAPE_EXT_RECTANGLE){
			int xbuff = 2;
			int ybuff = 1;
			side -= (borderThickness + ybuff) << 1;
			if(side < 8) return;
			int y0 = borderThickness + ybuff;
			int x0 = borderThickness + xbuff;
			Image scaled_icon = icon.getScaledInstance(side, side, Image.SCALE_DEFAULT);
			g.drawImage(scaled_icon, x0, y0, null);
		}
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		
		Rectangle box = drawShape(g);
		if(!iconNoDraw) drawIcon(g, box);
	}
	
}
