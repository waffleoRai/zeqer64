package waffleoRai_zeqer64;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import waffleoRai_Files.FileBufferInputStream;
import waffleoRai_Files.FileBufferOutputStream;
import waffleoRai_Utils.FileBuffer;

public class ZeqerUtils {
	
	public static int md52UID(byte[] md5){
		if(md5 == null) return 0;
		int id = 0;
		for(int i = 0; i < 4; i++){
			id <<= 8;
			if(i >= md5.length){
				int b = Byte.toUnsignedInt(md5[i]);
				id |= b;
			}
		}
		return id;
	}

	public static String fixHeaderEnumID(String input, boolean macro){
		//If a macro, will be set to all uppercase
		if(input == null) return input;
		if(macro) input = input.toUpperCase();
		input = input.replace(' ', '_');
		
		int len = input.length();
		StringBuilder sb = new StringBuilder(len);
		for(int i = 0; i < len; i++){
			char c = input.charAt(i);
			if(Character.isAlphabetic(c)) sb.append(c);
			else if(Character.isDigit(c) && i != 0) sb.append(c);
			else if(c == '_') sb.append(c);
		}
		
		return sb.toString();
	}
	
	public static FileBuffer inflate(InputStream input){
		if(input == null) return null;
		try{
			int decsize = 0;
			for(int i = 0; i < 4; i++){
				decsize <<= 8;
				decsize |= input.read();
			}
			
			FileBuffer dec = new FileBuffer(decsize, true);
			
			InflaterInputStream iis = new InflaterInputStream(input);
			for(int i = 0; i < decsize; i++){
				dec.addToFile((byte)iis.read());
			}
			iis.close();
			return dec;
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
		return null;
	}
	
	public static FileBuffer inflate(FileBuffer input){
		if(input == null) return null;
		input.setCurrentPosition(0L);
		int decsize = input.nextInt();
		
		FileBuffer dec = new FileBuffer(decsize, true);
		try{
			InflaterInputStream iis = new InflaterInputStream(new FileBufferInputStream(input));
			input.setCurrentPosition(4L);
			for(int i = 0; i < decsize; i++){
				dec.addToFile((byte)iis.read());
			}
			iis.close();
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
		return dec;
	}
	
	public static FileBuffer deflate(FileBuffer input){
		if(input == null) return null;
		int size = (int)input.getFileSize();
		FileBuffer comp = new FileBuffer(size + 4, true);
		comp.addToFile(size);
		
		try{
			DeflaterOutputStream dos = new DeflaterOutputStream(new FileBufferOutputStream(comp));
			dos.write(input.getBytes(), 0, (int)input.getFileSize());
			dos.close();
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
		
		return comp;
	}
	
}
