package waffleoRai_zeqer64.GUI.seqDisplay;

public class ZeqerColorSchemes {

	public static final ZeqerColorScheme RED = new ZeqerColorScheme(0, 0.8f, "Red");
	public static final ZeqerColorScheme ORANGE = new ZeqerColorScheme(0.085f, 0.8f, "Orange"); //Hue = 31
	public static final ZeqerColorScheme YELLOW = new ZeqerColorScheme(0.164f, 0.8f, "Yellow");
	public static final ZeqerColorScheme GREEN = new ZeqerColorScheme(0.329f, 0.8f, "Green");
	public static final ZeqerColorScheme AQUA = new ZeqerColorScheme(0.444f, 0.8f, "Aqua");
	public static final ZeqerColorScheme LIGHT_BLUE = new ZeqerColorScheme(0.553f, 0.8f, "Light Blue");
	public static final ZeqerColorScheme BLUE = new ZeqerColorScheme(0.658f, 0.7f, "Blue");
	public static final ZeqerColorScheme INDIGO = new ZeqerColorScheme(0.759f, 0.8f, "Indigo");
	public static final ZeqerColorScheme PURPLE = new ZeqerColorScheme(0.805f, 0.7f, "Purple");
	public static final ZeqerColorScheme PINK = new ZeqerColorScheme(0.904f, 0.8f, "Pink");
	public static final ZeqerColorScheme GREY = new ZeqerColorScheme(0, 0, "Grey");
	
	public static void clearMemory(){
		RED.clearMemory();
		ORANGE.clearMemory();
		YELLOW.clearMemory();
		GREEN.clearMemory();
		AQUA.clearMemory();
		LIGHT_BLUE.clearMemory();
		BLUE.clearMemory();
		INDIGO.clearMemory();
		PURPLE.clearMemory();
		PINK.clearMemory();
		GREY.clearMemory();
	}
	
}
