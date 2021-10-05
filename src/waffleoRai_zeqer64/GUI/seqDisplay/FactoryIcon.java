package waffleoRai_zeqer64.GUI.seqDisplay;

import java.awt.Graphics;
import java.util.LinkedList;
import java.util.List;

import waffleoRai_zeqer64.GUI.ScaledImage;

class FactoryIcon {
	
	private List<ImagePiece> pieces;
	private int height;
	private int width;
	
	public FactoryIcon(){
		pieces = new LinkedList<ImagePiece>();
	}
	
	private static class ImagePiece{
		public ScaledImage img;
		public int x;
		public int y;
	}
	
	public void addPiece(ScaledImage image, int x, int y){
		ImagePiece piece = new ImagePiece();
		piece.img = image;
		piece.x = x;
		piece.y = y;
		pieces.add(piece);
		
		int xend = x + image.getWidth();
		int yend = y + image.getHeight();
		if(xend > width) width = xend;
		if(yend > height) height = yend;
	}
	
	public int getWidth(){return width;}
	public int getHeight(){return height;}
	
	public void clearPieces(){
		pieces.clear();
	}
	
	public void drawTo(Graphics g, int x, int y){
		for(ImagePiece p : pieces){
			g.drawImage(p.img.getImage(), x+p.x, y+p.y, null);
		}
	}

}
