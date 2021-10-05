package waffleoRai_zeqer64.GUI.seqDisplay;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import waffleoRai_zeqer64.GUI.ScaledImage;

class IconFactory {
	
	private static final String PKG_PATH = "/waffleoRai_zeqer64/GUI/seqDisplay/iconres";
	
	private static IconFactory static_factory;
	
	private boolean is_disposed = false;
	
	private BufferedImage[] numbers;
	private BufferedImage[] note_letters;
	private Map<String, BufferedImage> img_map;
	
	private Map<Integer, ScaledSet> scaled_cache;
	
	public IconFactory(){
		numbers = new BufferedImage[10];
		note_letters = new BufferedImage[7];
		img_map = new HashMap<String, BufferedImage>();
		scaled_cache = new TreeMap<Integer, ScaledSet>();
		
		try{
			for(int i = 0; i < 10; i++){
				String ico_path = PKG_PATH + "/text_" + i + ".png";
				URL res = IconFactory.class.getResource(ico_path);
				if(res == null) System.err.println("IconFactory.<init> || ERROR: Couldn't find \"" + ico_path + "\"");
				else numbers[i] = ImageIO.read(res);
			}
			for(int i = 0; i < 7; i++){
				char c = (char)('A'+i);
				String ico_path = PKG_PATH + "/text_" + c + ".png";
				URL res = IconFactory.class.getResource(ico_path);
				if(res == null) System.err.println("IconFactory.<init> || ERROR: Couldn't find \"" + ico_path + "\"");
				else note_letters[i] = ImageIO.read(res);
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
		URL res = IconFactory.class.getResource(ico_path);
		if(res == null) System.err.println("IconFactory.<init> || ERROR: Couldn't find \"" + ico_path + "\"");
		else{
			BufferedImage img = ImageIO.read(IconFactory.class.getResource(ico_path));
			img_map.put(mapkey, img);	
		}
	}
	
	private static class ScaledSet{
		public ScaledImage[] numbers;
		public ScaledImage[] note_letters;
		public Map<String, ScaledImage> img_map;
		
		public ScaledSet(){
			numbers = new ScaledImage[10];
			note_letters = new ScaledImage[7];
			img_map = new HashMap<String, ScaledImage>();
		}
		
		public void dispose(){
			for(int i = 0; i < numbers.length; i++) numbers[i] = null;
			for(int i = 0; i < note_letters.length; i++) note_letters[i] = null;
			img_map.clear();
			numbers = null;
			note_letters = null;
			img_map = null;
		}
		
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
	
	public ScaledImage getScaledDigit(int number, int height){
		number %= 10;
		ScaledSet set = scaled_cache.get(height);
		if(set == null){
			set = new ScaledSet();
			scaled_cache.put(height, set);
		}
		ScaledImage img = set.numbers[number];
		if(img == null){
			BufferedImage src = numbers[number];
			if(src == null) return null;
			double ar = (double)src.getWidth()/(double)src.getHeight();
			int width = (int)Math.round(ar * (double)height);
			Image i = src.getScaledInstance(width, height, Image.SCALE_DEFAULT);
			img = new ScaledImage(i, width, height);
			set.numbers[number] = img;
		}
		return img;
	}
	
	public ScaledImage getScaledNoteLetter(int idx, int height){
		idx %= 7;
		ScaledSet set = scaled_cache.get(height);
		if(set == null){
			set = new ScaledSet();
			scaled_cache.put(height, set);
		}
		ScaledImage img = set.note_letters[idx];
		if(img == null){
			BufferedImage src = note_letters[idx];
			if(src == null) return null;
			double ar = (double)src.getWidth()/(double)src.getHeight();
			int width = (int)Math.round(ar * (double)height);
			Image i = src.getScaledInstance(width, height, Image.SCALE_DEFAULT);
			img = new ScaledImage(i, width, height);
			set.note_letters[idx] = img;
		}
		
		return img;
	}
	
	public ScaledImage getScaledIcon(String key, int height){
		ScaledSet set = scaled_cache.get(height);
		if(set == null){
			set = new ScaledSet();
			scaled_cache.put(height, set);
		}
		ScaledImage img = set.img_map.get(key);
		if(img == null){
			BufferedImage src = img_map.get(key);
			if(src == null) return null;
			double ar = (double)src.getWidth()/(double)src.getHeight();
			int width = (int)Math.round(ar * (double)height);
			Image i = src.getScaledInstance(width, height, Image.SCALE_DEFAULT);
			img = new ScaledImage(i, width, height);
			set.img_map.put(key, img);
		}
		
		return img;
	}
	
	public void clearCache(){
		for(ScaledSet set : scaled_cache.values()) set.dispose();
		scaled_cache.clear();
	}
	
	public void dispose(){
		clearCache();
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
	
	public static ScaledImage getScaledDigitIcon(int digit, int height){
		if(static_factory == null) static_factory = new IconFactory();
		return static_factory.getScaledDigit(digit, height);
	}
	
	public static ScaledImage getScaledNoteLetterIcon(int letter_idx, int height){
		if(static_factory == null) static_factory = new IconFactory();
		return static_factory.getScaledNoteLetter(letter_idx, height);
	}
	
	public static ScaledImage getScaledMappedIcon(String key, int height){
		if(static_factory == null) static_factory = new IconFactory();
		return static_factory.getScaledIcon(key, height);
	}
	
	public static void disposeStatic(){
		if(static_factory == null) return;
		static_factory.dispose();
		static_factory = null;
	}
	
}
