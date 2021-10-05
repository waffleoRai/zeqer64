package waffleoRai_zeqer64.GUI.seqDisplay;

import java.awt.Graphics;

class NumberIconDrawer implements IconDrawer{
	
	private int last_height;
	private FactoryIcon icon;
	
	private int value;
	
	public NumberIconDrawer(int number){
		value = number;
		last_height = -1;
	}
	
	@Override
	public void updateSize(int height) {
		if(height != last_height){
			icon = NumberIconFactory.generate(value, height);
			last_height = height;
		}
	}

	public void drawIcon(Graphics g, int x, int y) {
		if(icon == null) return;
		icon.drawTo(g, x, y);
	}
	
	public int getRequiredWidth(){
		return icon.getWidth();
	}

}
