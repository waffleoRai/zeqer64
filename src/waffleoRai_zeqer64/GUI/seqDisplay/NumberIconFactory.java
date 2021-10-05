package waffleoRai_zeqer64.GUI.seqDisplay;

import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import waffleoRai_zeqer64.GUI.ScaledImage;

class NumberIconFactory {
	
	private static NumberIconFactory static_factory;
	
	private int last_height;
	private Map<Integer, FactoryIcon> cache;
	
	public NumberIconFactory(){
		/*number_icons = new BufferedImage[10];
		for(int i = 0; i < 10; i++){
			number_icons[i] = IconFactory.getDigitIcon(i);
			if(number_icons[i].getHeight() > res_height){
				res_height = number_icons[i].getHeight();
			}
		}*/
		cache = new TreeMap<Integer, FactoryIcon>();
		last_height = -1;
	}
	
	public FactoryIcon generateNumberImage(int number, int height){
		if(height != last_height){
			clearCache();
			last_height = height;
		}
		else{
			//Check to see if this number has been rendered before.
			FactoryIcon ico = cache.get(number);
			if(ico != null) return ico;
		}
		
		LinkedList<Integer> digits = new LinkedList<Integer>();
		//Only does positive ints right now.
		int value = Math.abs(number);
		while(value > 0){
			digits.push(value % 10);
			value /= 10;
		}
		if(digits.isEmpty()) digits.push(0);
		
		FactoryIcon icon = new FactoryIcon();
		int x = 0;
		while(!digits.isEmpty()){
			int digit = digits.pop();
			ScaledImage digimg = IconFactory.getScaledDigitIcon(digit, height);
			icon.addPiece(digimg, x, 0);
			x += digimg.getWidth() + 2;
		}
		
		cache.put(number, icon);
		return icon;
	}
	
	public void clearCache(){
		if(cache == null) return;
		cache.clear();
	}
	
	public static FactoryIcon generate(int number, int height){
		if(static_factory == null) static_factory = new NumberIconFactory();
		return static_factory.generateNumberImage(number, height);
	}
	
	public static void dispose(){
		if(static_factory != null){
			static_factory.clearCache();
			static_factory = null;
		}
	}
	
}
