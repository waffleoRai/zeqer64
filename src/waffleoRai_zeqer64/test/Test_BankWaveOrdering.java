package waffleoRai_zeqer64.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_soundbank.nintendo.z64.Z64Bank;
import waffleoRai_zeqer64.SoundTables.BankInfoEntry;
import waffleoRai_zeqer64.ZeqerCore;
import waffleoRai_zeqer64.ZeqerRom;
import waffleoRai_zeqer64.filefmt.NusRomInfo;
import waffleoRai_zeqer64.filefmt.RomInfoNode;

public class Test_BankWaveOrdering {
	
	public static void main(String[] args) {
		//Args
		//ROMpath BankIdx
		String rompath = args[0];
		String outdir = args[1];

		try{
			RomInfoNode rin = ZeqerCore.detectROM(rompath);
			if(rin == null){
				System.err.println("ROM was not recognized: " + rompath);
				System.exit(1);
			}
			if(!(rin instanceof NusRomInfo)){
				//I'll probably fix this eventually, but I'm lazy
				System.err.println("ROM recognized, but not N64 ROM: " + rompath);
				System.err.println("Please use direct N64 ROMs for this tool.");
				System.exit(1);
			}
			System.err.println("N64 ROM Detected: " + rin.getROMName());
			NusRomInfo rinfo = (NusRomInfo)rin;
			ZeqerRom rom = new ZeqerRom(rompath, rinfo);
			
			BankInfoEntry[] bank_info = rom.loadBankEntries();
			FileBuffer f_audiobank = rom.loadAudiobank();
			
			BufferedWriter stderr = new BufferedWriter(new OutputStreamWriter(System.err));
			for(int i = 0; i < bank_info.length; i++){
				String bname = "audiobank_" + String.format("%03d", i);
				int icount = bank_info[i].getInstrumentCount();
				int pcount = bank_info[i].getPercussionCount();
				int xcount = bank_info[i].getSFXCount();
				int offset = bank_info[i].getOffset();
				int len = bank_info[i].getSize();
				
				stderr.write("----- " + bname + " -----\n");
				FileBuffer bfile = f_audiobank.createReadOnlyCopy(offset, offset + len);
				bfile.writeFile(outdir + File.separator + bname + ".bin");
				Z64Bank zbnk = Z64Bank.readBank(bfile, icount, pcount, xcount);
				zbnk.debug_printWaveBlocks(stderr);
				bfile.dispose();
				
				stderr.write("\n");
			}
			stderr.flush();
		}
		catch(Exception ex){
			ex.printStackTrace();
			System.exit(1);
		}
		
	}

}
