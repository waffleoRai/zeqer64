package waffleoRai_zeqer64.GUI.seqDisplay;

import java.awt.Graphics;

class NoteNameIconDrawer implements IconDrawer{

	private int last_height;
	private FactoryIcon icon;
	
	private byte midi_note;
	
	public NoteNameIconDrawer(byte mid_note){
		midi_note = mid_note;
		last_height = -1;
	}
	
	@Override
	public void updateSize(int height) {
		if(height != last_height){
			icon = NoteNameIconFactory.getNoteAsImage(midi_note, height);
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
