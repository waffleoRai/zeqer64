package waffleoRai_zeqer64.GUI.seqDisplay;

import java.awt.Graphics;

interface IconDrawer {

	public void updateSize(int height);
	public void drawIcon(Graphics g, int x, int y);
	public int getRequiredWidth();
	
}
