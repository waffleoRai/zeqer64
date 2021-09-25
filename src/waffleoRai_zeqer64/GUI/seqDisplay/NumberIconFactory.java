package waffleoRai_zeqer64.GUI.seqDisplay;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

class NumberIconFactory {
	
	private static NumberIconFactory static_factory;
	
	private BufferedImage[] number_icons; //References images in IconFactory
	private int res_height;
	
	public NumberIconFactory(){
		number_icons = new BufferedImage[10];
		for(int i = 0; i < 10; i++){
			number_icons[i] = IconFactory.getDigitIcon(i);
			if(number_icons[i].getHeight() > res_height){
				res_height = number_icons[i].getHeight();
			}
		}
	}
	
	public BufferedImage generateNumberImage(int number){
		LinkedList<Integer> digits = new LinkedList<Integer>();
		//Only does positive ints right now.
		int value = Math.abs(number);
		while(value > 0){
			digits.push(value % 10);
			value %= 10;
		}
		
		int alloc_h = res_height;
		//int alloc_w = res_width * digits.size();
		int alloc_w = 0;
		for(Integer d : digits) alloc_w += number_icons[d].getWidth();
		BufferedImage out = new BufferedImage(alloc_w, alloc_h, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D canvas = out.createGraphics();
		int x = 0;
		for(Integer d : digits) {
			canvas.drawImage(number_icons[d], x, 0, null);
			x += number_icons[d].getWidth();
		}
		
		return out;
	}
	
	public static BufferedImage generate(int number){
		if(static_factory == null) static_factory = new NumberIconFactory();
		return static_factory.generateNumberImage(number);
	}
	
	public static void dispose(){
		if(static_factory != null){
			for(int i = 0; i < static_factory.number_icons.length; i++){
				static_factory.number_icons[i] = null;
			}
			static_factory = null;
		}
	}
	
}
