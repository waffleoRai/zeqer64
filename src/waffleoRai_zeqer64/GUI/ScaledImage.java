package waffleoRai_zeqer64.GUI;

import java.awt.Image;

public class ScaledImage {

	private Image img;
	private int width;
	private int height;
	
	public ScaledImage(Image image, int width, int height){
		img = image;
		this.width = width;
		this.height = height;
	}
	
	public Image getImage(){return img;}
	public int getWidth(){return width;}
	public int getHeight(){return height;}
	
}
