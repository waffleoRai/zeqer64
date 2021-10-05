package waffleoRai_zeqer64.GUI.seqDisplay;

import java.awt.Color;

enum TrackColorScheme {

	RED(ZeqerColorSchemes.RED, Color.decode("0xdc2020")),
	ORANGE(ZeqerColorSchemes.ORANGE, Color.decode("0xffa200")),
	YELLOW(ZeqerColorSchemes.YELLOW, Color.decode("0xf9ff49")),
	GREEN(ZeqerColorSchemes.GREEN, Color.decode("0x38d622")),
	BLUE(ZeqerColorSchemes.BLUE, Color.decode("0x2a3cf1")),
	LIGHT_BLUE(ZeqerColorSchemes.LIGHT_BLUE, Color.decode("0x2af9fb")),
	INDIGO(ZeqerColorSchemes.INDIGO, Color.decode("0x8c2af1")),
	PURPLE(ZeqerColorSchemes.PURPLE, Color.decode("0xdc2af1")),
	AQUA(ZeqerColorSchemes.AQUA, Color.decode("0x12eca7")),
	PINK(ZeqerColorSchemes.PINK, Color.decode("0xff5ca1")),
	GREY(ZeqerColorSchemes.GREY, Color.decode("0x6a6a6a"));
	
	private ZeqerColorScheme scheme;
	private Color base_color; //Derives other colors from this
	private Color bkg_color;
	private Color tick_color;
	
	private TrackColorScheme(ZeqerColorScheme color_sc, Color base){
		scheme = color_sc;
		base_color = base;
		
		//Calcluate other colors
		float[] hsb = new float[3];
		Color.RGBtoHSB(base.getRed(), base.getGreen(), base.getBlue(), hsb);
		bkg_color = Color.getHSBColor(hsb[0], hsb[1]*0.2f, 1.0f);
		tick_color = Color.getHSBColor(hsb[0], hsb[1], 0.5f);
	}
	
	public ZeqerColorScheme getNodeColorScheme(){
		return scheme;
	}
	
	public Color getBaseColor(){
		return base_color;
	}
	
	public Color getBackgroundColor(){
		return bkg_color;
	}
	
	public Color getTickMarkColor(){
		return tick_color;
	}
	
	
}
