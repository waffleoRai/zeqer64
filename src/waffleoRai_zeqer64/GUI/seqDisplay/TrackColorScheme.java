package waffleoRai_zeqer64.GUI.seqDisplay;

import java.awt.Color;

enum TrackColorScheme {

	RED(ZeqerColorSchemes.RED, Color.decode("0x000000")),
	ORANGE(ZeqerColorSchemes.ORANGE, Color.decode("0x000000")),
	YELLOW(ZeqerColorSchemes.YELLOW, Color.decode("0x000000")),
	GREEN(ZeqerColorSchemes.GREEN, Color.decode("0x000000")),
	BLUE(ZeqerColorSchemes.BLUE, Color.decode("0x000000")),
	LIGHT_BLUE(ZeqerColorSchemes.LIGHT_BLUE, Color.decode("0x000000")),
	INDIGO(ZeqerColorSchemes.INDIGO, Color.decode("0x000000")),
	PURPLE(ZeqerColorSchemes.PURPLE, Color.decode("0x000000")),
	AQUA(ZeqerColorSchemes.AQUA, Color.decode("0x000000")),
	PINK(ZeqerColorSchemes.PINK, Color.decode("0x000000")),
	GREY(ZeqerColorSchemes.GREY, Color.decode("0x000000"));
	
	private ZeqerColorScheme scheme;
	private Color base_color; //Derives other colors from this
	
	private TrackColorScheme(ZeqerColorScheme color_sc, Color base){
		scheme = color_sc;
		base_color = base;
	}
	
	public ZeqerColorScheme getNodeColorScheme(){
		return scheme;
	}
	
	public Color getBaseColor(){
		return base_color;
	}
	
	public Color getBackgroundColor(){
		//TODO
		return null;
	}
	
	public Color getTickMarkColor(){
		//TODO
		return null;
	}
	
	
}
