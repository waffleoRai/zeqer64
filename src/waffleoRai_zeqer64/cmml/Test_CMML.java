package waffleoRai_zeqer64.cmml;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import waffleoRai_Utils.FileBuffer;

public class Test_CMML {
	
	private static void testPreprocessor(String input, String logpath, String[] include_dirs) throws IOException{
		String include_dir = input.substring(0, input.lastIndexOf(File.separatorChar));
		
		CMMLPreprocessor prepro = new CMMLPreprocessor();
		
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(input));
		prepro.preprocess(bis);
		bis.close();
		
		//Includes.
		/*String inclname;
		while((inclname = prepro.popUnprocessedInclude()) != null){
			System.err.println("Including " + inclname + "...");
			String inclpath = CMMLTextProcessing.findIncludePath(include_dir, inclname);
			if(include_dirs != null){
				int j = 0;
				while(!FileBuffer.fileExists(inclpath) && j < include_dirs.length){
					inclpath = CMMLTextProcessing.findIncludePath(include_dirs[j++], inclname);
				}	
			}
			
			bis = new BufferedInputStream(new FileInputStream(inclpath));
			prepro.preprocess(bis);
			bis.close();
		}*/
		
		//Print.
		BufferedWriter log = new BufferedWriter(new FileWriter(logpath));
		prepro.debugPrint(log);
		log.close();
		
	}

	public static void main(String[] args) {
		
		String input = args[0];
		String logpath = args[1];
		String incl_raw = args[2];
		
		String[] include_dirs = incl_raw.split(",");
		
		try{
			testPreprocessor(input, logpath, include_dirs);
		}
		catch(Exception ex){
			ex.printStackTrace();
			System.exit(1);
		}
		
	}

}
