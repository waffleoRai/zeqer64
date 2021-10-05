package waffleoRai_zeqer64.GUI.seqDisplay;

import java.awt.Color;

public class ZeqerColorScheme {
	
	public static final float MIN_BRIGHTNESS = 0.10f;
	public static final float MAX_BRIGHTNESS = 0.98f;
	
	private float hue;
	private float saturation;
	private String name;
	
	private Color light_bkg;
	
	private Color border_light;
	private Color border_dark;
	private Color[] shades_128;
	
	public ZeqerColorScheme(float hue, float saturation, String schemeName){
		this.hue = hue;
		this.saturation = saturation;
		name = schemeName;
	}
	
	public float getHue(){return hue;}
	public float getSaturation(){return saturation;}
	
	public String toString(){
		return name;
	}
	
	public Color getShade(int level){
		if(shades_128 == null){
			shades_128 = new Color[128];
		}
		if(level < 0 || level > 127) return null;
		if(shades_128[level] == null){
			double brightness = MAX_BRIGHTNESS - MIN_BRIGHTNESS;
			brightness *= ((double)level/128.0);
			brightness += MIN_BRIGHTNESS;
			shades_128[level] = Color.getHSBColor(hue, saturation, (float)brightness);
		}
		return shades_128[level];
	}
	
	public Color getLightBorder(){
		if(border_light == null){
			border_light = Color.getHSBColor(hue, (float)(saturation/2.0), MAX_BRIGHTNESS);
		}
		return border_light;
	}
	
	public Color getLightBackground(){
		if(light_bkg == null){
			light_bkg = Color.getHSBColor(hue, (float)(saturation/4.0), 0.97f);
		}
		return light_bkg;
	}
	
	public Color getDarkBorder(){
		if(border_dark == null){
			border_dark = Color.getHSBColor(hue, (float)(saturation/2.0), MIN_BRIGHTNESS);
		}
		return border_dark;
	}
	
	public void clearMemory(){
		border_light = null;
		border_dark = null;
		if(shades_128 != null){
			for(int i = 0; i < shades_128.length; i++){
				shades_128[i] = null;
			}
		}
		shades_128 = null;
	}

}
