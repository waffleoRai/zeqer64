package waffleoRai_zeqer64;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import waffleoRai_Utils.FileBuffer;

public class ZeqerInstaller {
	
	private static final String RES_JARPATH = "/waffleoRai_zeqer64/res";
	
	private static final char SEP = File.separatorChar;
	
	public static interface ZeqerInstallListener{
		public void onWriteIniStart();
		public void onTableReadStart();
		public void onFileExtractStart(String filepath);
		public void onFileExtractDone(String filepath);
	}

	private String install_dir;
	private ZeqerInstallListener listener;
	
	public ZeqerInstaller(String dirpath){
		install_dir = dirpath;
	}
	
	public void setListener(ZeqerInstallListener l){
		listener = l;
	}
	
	public boolean installToDir() throws IOException{
		if(!FileBuffer.directoryExists(install_dir)){
			Files.createDirectories(Paths.get(install_dir));
		}
		
		//Copy files from JAR
		return extractFiles();
	}
	
	private List<String[]> readFileTable(){
		if(listener != null) listener.onTableReadStart();
		
		String file_csv_path = RES_JARPATH + SEP + "installtbl.csv";
		try{
			InputStream jarstr = ZeqerCore.class.getResourceAsStream(file_csv_path);
			if(jarstr != null){
				BufferedReader br = new BufferedReader(new InputStreamReader(jarstr));
				List<String[]> list = new LinkedList<String[]>();
				String line = null;
				while((line = br.readLine()) != null){
					if(line.isEmpty()) continue;
					if(line.startsWith("#")) continue;
					list.add(line.split(","));
				}
				br.close();
				return list;
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		
		return null;
	}
	
	private boolean extractFiles() throws IOException{
		
		List<String[]> table = readFileTable();
		if(table == null) return false;
		
		for(String[] line : table){
			if(line.length < 2) continue;
			
			boolean isdir = false;
			if(line.length >= 3){
				isdir = line[2].equals("1");
			}
			
			String trg = line[1];
			String trg_path = install_dir + SEP + trg.replace('/', SEP);
			if(listener != null) listener.onFileExtractStart(trg);
			
			if(isdir){
				if(!FileBuffer.directoryExists(trg_path)){
					Files.createDirectories(Paths.get(trg_path));
				}
			}
			else{
				String src = line[0];
				String src_path = RES_JARPATH + '/' + src;
				
				try{
					InputStream jarstr = ZeqerCore.class.getResourceAsStream(src_path);
					if(jarstr != null){
						BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(trg_path));
						int b = -1;
						while((b = jarstr.read()) != -1){bos.write(b);}
						bos.close();
						jarstr.close();
					}
				}
				catch(Exception ex){
					ex.printStackTrace();
					return false;
				}
			}
			if(listener != null) listener.onFileExtractDone(trg);
			
		}
		
		return true;
	}
	
	
}
