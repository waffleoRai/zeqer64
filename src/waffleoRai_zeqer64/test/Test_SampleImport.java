package waffleoRai_zeqer64.test;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import waffleoRai_Sound.nintendo.Z64Wave;
import waffleoRai_zeqer64.filefmt.wave.UltraWavFile;
import waffleoRai_zeqer64.filefmt.wave.ZeqerWaveIO;
import waffleoRai_zeqer64.filefmt.wave.ZeqerWaveIO.SampleImportOptions;
import waffleoRai_zeqer64.filefmt.wave.ZeqerWaveIO.SampleImportResult;

public class Test_SampleImport {

	public static void main(String[] args) {
		String indir = args[0];
		
		int maxsr = 32000;
		boolean twobit = false;
		boolean defotbl = false;
		int pred_scaler = 2;
		int stereoStrat = ZeqerWaveIO.MULTICHAN_IMPORT_SUM;
		
		try{
			SampleImportOptions ops = new SampleImportOptions();
			ops.maxSampleRate = maxsr;
			ops.twoBit = twobit;
			ops.useDefaultADPCMTable = defotbl;
			ops.multiChannelBehavior = stereoStrat;
			ops.npredScale = pred_scaler;
			
			DirectoryStream<Path> dirstr = Files.newDirectoryStream(Paths.get(indir));
			for(Path p : dirstr){
				if(Files.isDirectory(p)) continue;
				
				String inpath = p.toAbsolutePath().toString();
				if(!inpath.endsWith(".wav")) continue;
				if(inpath.contains("_cap")) continue;
				System.err.println("Test_SampleImport.main || Processing " + inpath + " ...");
				
				String suffix = "_cap" + maxsr;
				if(twobit) suffix += "_ADP5";
				else suffix += "_ADP9";
				suffix += "_npred" + (1 << pred_scaler);
				if(defotbl) suffix += "_tbldefo";
				else suffix += "_tblcustom";
				switch(stereoStrat){
				case ZeqerWaveIO.MULTICHAN_IMPORT_LEFTONLY:
					suffix += "_stereoL"; break;
				case ZeqerWaveIO.MULTICHAN_IMPORT_RIGHTONLY:
					suffix += "_stereoR"; break;
				case ZeqerWaveIO.MULTICHAN_IMPORT_SUM:
					suffix += "_stereoAvg"; break;
				}
				
				//Output paths
				String prefix = inpath;
				int lastdot = prefix.lastIndexOf('.');
				if(lastdot >= 0) prefix = prefix.substring(0, lastdot);
				
				String outpathCompr = prefix + suffix + ".buwav";
				String outpathUncmp = prefix + suffix + ".wav";
				
				//Try to compress...
				System.err.println("Test_SampleImport.main || Starting compression attempt...");
				SampleImportResult res = ZeqerWaveIO.importWAV(inpath, ops);
				if(res == null){
					System.err.println("Test_SampleImport.main || ERROR! (Unknown - return value is null)");
					continue;
				}
				switch(res.error){
				case ZeqerWaveIO.ERROR_CODE_NONE: 
					System.err.println("Test_SampleImport.main || No error caught!");
					break;
				case ZeqerWaveIO.ERROR_CODE_IO:
					System.err.println("Test_SampleImport.main || ERROR! I/O error.");
					break;
				case ZeqerWaveIO.ERROR_CODE_INPARSE: 
					System.err.println("Test_SampleImport.main || ERROR! Input parsing error.");
					break;
				case ZeqerWaveIO.ERROR_CODE_DOWNSAMPLE_FAIL: 
					System.err.println("Test_SampleImport.main || ERROR! Downsample failed.");
					break;
				case ZeqerWaveIO.ERROR_CODE_TBLGEN_FAIL: 
					System.err.println("Test_SampleImport.main || ERROR! ADPCM table generation failed.");
					break;
				case ZeqerWaveIO.ERROR_CODE_ENCODE_FAIL: 
					System.err.println("Test_SampleImport.main || ERROR! ADPCM encoding failed.");
					break;
				case ZeqerWaveIO.ERROR_CODE_AIFF_COMPRESSION_UNKNOWN: 
					System.err.println("Test_SampleImport.main || ERROR! AIFF compression code not recognized.");
					break;
				case ZeqerWaveIO.ERROR_CODE_INPUT_FMT_UNKNOWN: 
					System.err.println("Test_SampleImport.main || ERROR! Unknown input format.");
					break;
				case ZeqerWaveIO.ERROR_CODE_TABLE_IMPORT_FAILED: 
					System.err.println("Test_SampleImport.main || ERROR! Zeqer core table import error.");
					break;
				case ZeqerWaveIO.ERROR_CODE_MULTICHAN_NOT_SUPPORTED: 
					System.err.println("Test_SampleImport.main || ERROR! Multichannel support error.");
					break;
				case ZeqerWaveIO.ERROR_CODE_UNKNOWN: 
					System.err.println("Test_SampleImport.main || ERROR! (Unknown)");
					break;
				}
				
				//Save compressed version...
				System.err.println("Test_SampleImport.main || Saving compressed file...");
				UltraWavFile.writeUWAV(outpathCompr, res.info, res.data);
				
				//Redecompress and save...
				System.err.println("Test_SampleImport.main || Re-decompressing...");
				Z64Wave wave64 = Z64Wave.readZ64Wave(res.data, res.info);
				wave64.writeToWav(outpathUncmp);
			}
			dirstr.close();
		}
		catch(Exception ex){
			ex.printStackTrace();
			System.exit(1);
		}

	}

}
