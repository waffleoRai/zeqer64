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

public class BackgroundFactory {
	
	private static final String PKG_PATH = "/waffleoRai_zeqer64/GUI/seqDisplay/res";
	
	public static final String GRADIENT_DOWN_1 = "grdown1";
	public static final String GRADIENT_UP_1 = "grup1";
	public static final String GRADIENT_OUT_1 = "grout1";
	public static final String GRADIENT_OUT_2 = "grout2";
	public static final String GRADIENT_RADIALOUT_1 = "grradout1";

	private static BackgroundFactory static_factory;
	
	private boolean is_disposed = false;
	private Map<String, ImageSet> img_map;
	
	private static class ImageSet{
		
		//Includes pre-scaling.
		public BufferedImage base_img;
		public Map<Integer, ScaledImage> img_map;
		
		public ImageSet(BufferedImage image){
			base_img = image;
			img_map = new TreeMap<Integer, ScaledImage>();
		}
		
		public void clearCache(){
			img_map.clear();
		}
		
	}

	public BackgroundFactory(){
		img_map = new HashMap<String, ImageSet>();
		loadImages();
	}
	
	private void loadImages(){
		try{
			loadImage(GRADIENT_DOWN_1, "gradient_down_256");
			loadImage(GRADIENT_UP_1, "gradient_up_256");
			loadImage(GRADIENT_OUT_1, "gradient_out1_256");
			loadImage(GRADIENT_OUT_2, "gradient_out2_256");
			loadImage(GRADIENT_RADIALOUT_1, "gradient_radialout1_256");
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
	}
	
	private void loadImage(String key, String filename) throws IOException{
		String ico_path = PKG_PATH + "/" + filename + ".png";
		URL res = IconFactory.class.getResource(ico_path);
		if(res == null) System.err.println("IconFactory.<init> || ERROR: Couldn't find \"" + ico_path + "\"");
		else{
			BufferedImage img = ImageIO.read(BackgroundFactory.class.getResource(ico_path));
			img_map.put(key, new ImageSet(img));	
		}
	}
	
	public ScaledImage getImage(String key, int height){
		if(is_disposed) return null;
		
		ImageSet set = img_map.get(key);
		if(set == null) return null;
		
		if(set.base_img.getHeight() != height){
			//Check cache
			ScaledImage img = set.img_map.get(height);
			if(img != null) return img;
			
			//If not there, scale new one
			double aspect_ratio = (double)set.base_img.getWidth() / (double)set.base_img.getHeight();
			int width = (int)Math.round(aspect_ratio * (double)height);
			img = new ScaledImage(set.base_img.getScaledInstance(width, height, Image.SCALE_DEFAULT), width, height);
			set.img_map.put(height, img);
			return img;
		}
		else{
			//Just return the base image
			ScaledImage img = new ScaledImage(set.base_img, set.base_img.getWidth(), set.base_img.getHeight());
			return img;
		}
	}
	
	public void clearCache(){
		if(is_disposed) return;
		for(ImageSet set : img_map.values()){
			set.clearCache();
		}
	}
	
	public void dispose(){
		if(is_disposed) return;
		for(ImageSet set : img_map.values()){
			set.clearCache();
		}
		img_map.clear();
		img_map = null;
		is_disposed = true;
	}
	
	public static ScaledImage getScaledImage(String key, int height){
		if(static_factory == null) static_factory = new BackgroundFactory();
		return static_factory.getImage(key, height);
	}
	
	public static void disposeStatic(){
		if(static_factory != null){
			static_factory.dispose();
			static_factory = null;
		}
	}
	
}
