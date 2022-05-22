package waffleoRai_zeqer64.test;

import java.io.File;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_zeqer64.extract.SoundExtractor;

public class Test_RegenSysdata {

	public static void main(String[] args) {

		//Args: outpath	rom1 (rom2) (rom3)...
		if(args.length < 2){
			System.err.println("args: outpath rom1 (rom2) (rom3)...");
			System.exit(1);
		}
		
		String outpath = args[0];
		int inrom_count = args.length-1;
		String[] inpaths = new String[inrom_count];
		for(int i = 0; i < inrom_count; i++) inpaths[i] = args[i+1];
		
		try{
			//Extract
			for(int i = 0; i < inrom_count; i++){
				SoundExtractor.main(new String[]{inpaths[i], outpath});
			}
			
			//Metadata (assumes table is already there)
			String pmeta_wav = outpath + File.separator + "meta_wav.tsv";
			String pmeta_prs = outpath + File.separator + "meta_prs.tsv";
			String pmeta_bnk = outpath + File.separator + "meta_bnk.tsv";
			String pmeta_seq = outpath + File.separator + "meta_seq.tsv";
			
			if(FileBuffer.fileExists(pmeta_wav) && FileBuffer.fileExists(pmeta_seq) && FileBuffer.fileExists(pmeta_bnk)){
				ImportMetadata.main(new String[]{outpath, pmeta_wav, pmeta_seq, pmeta_prs, pmeta_bnk});
			}
			
		}
		catch(Exception ex){
			ex.printStackTrace();
			System.exit(1);
		}
		
	}

}
