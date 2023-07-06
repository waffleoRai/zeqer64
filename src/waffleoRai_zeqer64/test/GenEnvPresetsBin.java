package waffleoRai_zeqer64.test;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;

import waffleoRai_Utils.BinFieldSize;
import waffleoRai_Utils.FileBuffer;

public class GenEnvPresetsBin {

	public static void main(String[] args) {
		String inpath = args[0];
		String outpath = args[1];
		
		try{
			BufferedReader br = new BufferedReader(new FileReader(inpath));
			
			//First line
			String line = br.readLine();
			int name_col = -1;
			int script_col = -1;
			String[] fields = line.split("\t");
			for(int i = 0; i < fields.length; i++){
				if(fields[i].startsWith("#")){
					fields[i] = fields[i].substring(1);
				}
				if(name_col < 0 && fields[i].equals("NAME")){
					name_col = i;
				}
				if(script_col < 0 && fields[i].equals("SCRIPT")){
					script_col = i;
				}
			}
			
			//Check to make sure fields are there
			if(name_col < 0 || script_col < 0){
				System.err.println("Missing one or both fields!");
				br.close();
				System.exit(1);
			}
			
			//Process records
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outpath));
			
			while((line = br.readLine()) != null){
				fields = line.split("\t");
				String ename = fields[name_col];
				String escript = fields[script_col];
				
				escript = escript.replace("\"", "");
				ename = ename.replace("\"", "");
				
				int alloc = (ename.length() << 2) + 3;
				String[] sevents = escript.split(";");
				alloc += sevents.length << 2;
				
				FileBuffer buffer = new FileBuffer(alloc, true);
				buffer.addVariableLengthString("UTF8", ename, BinFieldSize.WORD, 2);
				
				for(int i = 0; i < sevents.length; i++){
					String[] spl = sevents[i].split(",");
					
					try{
						short num = (short)(Integer.parseInt(spl[0]));
						buffer.addToFile(num);
						num = (short)(Integer.parseInt(spl[1]));
						buffer.addToFile(num);
					}
					catch(NumberFormatException nex){
						if(spl[0].equals("ADSR_HANG")){
							buffer.addToFile(0xffff0000);
						}
					}
				}
				
				buffer.writeToStream(bos);
			}
			
			bos.close();
			br.close();
			
		}catch(Exception ex){
			ex.printStackTrace();
			System.exit(1);
		}
		
	}

}
