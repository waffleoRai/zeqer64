package waffleoRai_zeqer64.GUI.seqDisplay;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

class IconFactory {
	
	private static final String PKG_PATH = "waffleoRai_zeqer64/GUI/seqDisplay/iconres";
	
	private static IconFactory static_factory;
	
	private boolean is_disposed = false;
	
	private BufferedImage[] numbers;
	private BufferedImage[] note_letters;
	private Map<String, BufferedImage> img_map;
	
	public IconFactory(){
		numbers = new BufferedImage[10];
		note_letters = new BufferedImage[7];
		img_map = new HashMap<String, BufferedImage>();
		
		try{
			for(int i = 0; i < 10; i++){
				String ico_path = PKG_PATH + "/text_" + i + ".png";
				numbers[i] = ImageIO.read(IconFactory.class.getResource(ico_path));
			}
			for(int i = 0; i < 7; i++){
				String ico_path = PKG_PATH + "/text_" + ('A'+i) + ".png";
				note_letters[i] = ImageIO.read(IconFactory.class.getResource(ico_path));
			}
			
			loadImageFile("text_flat", "flat");
			loadImageFile("text_sharp", "sharp");
			loadImageFile("text_natural", "natural");
			loadImageFile("text_plus", "plus");
			loadImageFile("text_minus", "minus");
			loadImageFile("pan_03", "pan");
			loadImageFile("pri_01", "priority");
			loadImageFile("rvb_01", "reverb");
			
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
		
	}
	
	private void loadImageFile(String file_name, String mapkey) throws IOException{
		String ico_path = PKG_PATH + "/" + file_name + ".png";
		BufferedImage img = ImageIO.read(IconFactory.class.getResource(ico_path));
		img_map.put(mapkey, img);
	}
	
	public BufferedImage getDigit(int number){
		if(is_disposed) return null;
		if(number < 0) number = 0;
		number %= 10;
		return numbers[number];
	}
	
	public BufferedImage getNoteLetter(int idx){
		if(is_disposed) return null;
		if(idx < 0) idx = 0;
		idx %= 7;
		return note_letters[idx];
	}
	
	public BufferedImage getIcon(String key){
		if(is_disposed) return null;
		return img_map.get(key);
	}
	
	public void dispose(){
		for(int i = 0; i < numbers.length; i++) numbers[i] = null;
		for(int i = 0; i < note_letters.length; i++) note_letters[i] = null;
		img_map.clear();
		
		numbers = null;
		note_letters = null;
		img_map = null;
		
		is_disposed = true;
	}
	
	public static BufferedImage getDigitIcon(int digit){
		if(static_factory == null) static_factory = new IconFactory();
		return static_factory.getDigit(digit);
	}
	
	public static BufferedImage getNoteLetterIcon(int letter_idx){
		if(static_factory == null) static_factory = new IconFactory();
		return static_factory.getNoteLetter(letter_idx);
	}
	
	public static BufferedImage getMappedIcon(String key){
		if(static_factory == null) static_factory = new IconFactory();
		return static_factory.getIcon(key);
	}
	
	public static void disposeStatic(){
		if(static_factory == null) return;
		static_factory.dispose();
		static_factory = null;
	}
	
}
