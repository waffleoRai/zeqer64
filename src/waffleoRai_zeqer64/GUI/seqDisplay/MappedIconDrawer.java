package waffleoRai_zeqer64.GUI.seqDisplay;

import java.awt.Graphics;

import waffleoRai_zeqer64.GUI.ScaledImage;

class MappedIconDrawer implements IconDrawer{
	
	private int last_height;
	private ScaledImage icon;
	
	private String iconkey;
	
	public MappedIconDrawer(String key){
		iconkey = key;
		last_height = -1;
	}
	
	@Override
	public void updateSize(int height) {
		if(height != last_height){
			icon = IconFactory.getScaledMappedIcon(iconkey, height);
			last_height = height;
		}
	}

	public void drawIcon(Graphics g, int x, int y) {
		if(icon == null) return;
		g.drawImage(icon.getImage(), x, y, null);
	}
	
	public int getRequiredWidth(){
		return icon.getWidth();
	}

}
