package waffleoRai_zeqer64.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_zeqer64.ZeqerCore;
import waffleoRai_zeqer64.engine.EngineWaveArcInfo;
import waffleoRai_zeqer64.filefmt.AbldFile;
import waffleoRai_zeqer64.filefmt.RecompFiles;

public class Test_BinBuild {
	
	public static final char SEP = File.separatorChar;

	public static void main(String[] args) {
		//args: zeqer_dir rom_id output_dir
		
		if(args.length < 3){
			System.err.println("Need 3 args: zeqer_dir rom_id output_dir");
			System.exit(1);
		}
		
		String zeqer_dir = args[0];
		String rom_id = args[1];
		String outdir = args[2];
		
		try{
			ZeqerCore.getActiveCore().setProgramDirectory(zeqer_dir);
			ZeqerCore.getActiveCore().loadCore();
			AbldFile abld = ZeqerCore.getActiveCore().loadSysBuild(rom_id);
			if(abld == null){
				System.err.println("Couldn't find sys build with ID: " + rom_id);
				System.exit(1);
			}
			
			if(!FileBuffer.directoryExists(outdir)){
				Files.createDirectories(Paths.get(outdir));
			}
			RecompFiles.exportBuildBinary(outdir, abld, RecompFiles.BINBUILD_FLAG_DEBUGMODE);
			
			//Debug area
			/*FileBuffer buff1 = FileBuffer.createBuffer(outdir + SEP + "d_audiotable" + SEP + "audiotable_00", true);
			FileBuffer buff2 = FileBuffer.createBuffer(outdir + SEP + "d_audiotable" + SEP + "audiotable_00_ref", true);
			long sz = Math.min(buff1.getFileSize(), buff2.getFileSize());
			for(long i = 0; i < sz; i++){
				if(buff1.getByte(i) != buff2.getByte(i)){
					System.err.println("Files diverge @ 0x" + Long.toHexString(i));
					break;
				}
			}*/
			
			//Print sample list.
			/*System.err.println("--- WArc 0 Samples ---");
			EngineWaveArcInfo ainfo = abld.getWaveArcs().get(0);
			List<Integer> slist = ainfo.getSamples();
			for(Integer sid : slist) System.err.println(String.format("%08x", sid));*/
			
		}
		catch(Exception ex){
			ex.printStackTrace();
			System.exit(1);
		}

	}

}
