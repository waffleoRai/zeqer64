package waffleoRai_zeqer64.test;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_zeqer64.ZeqerUtils;

public class Test_CompressGuiStr {

	public static void main(String[] args) {
		String path = args[0];
		
		try{
			String outpath = path + ".jdfl";
			
			//Compress
			FileBuffer input = FileBuffer.createBuffer(path);
			FileBuffer comp = ZeqerUtils.deflate(input);
			comp.writeFile(outpath);
			
			//Decompress and test
			FileBuffer dec = ZeqerUtils.inflate(comp);
			dec.writeFile(outpath + ".txt");
			
		}
		catch(Exception ex){
			ex.printStackTrace();
			System.exit(0);
		}
		
	}

}
