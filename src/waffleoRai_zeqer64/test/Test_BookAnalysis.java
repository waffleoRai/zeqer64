package waffleoRai_zeqer64.test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import waffleoRai_Sound.nintendo.N64ADPCMTable;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_soundbank.nintendo.z64.Z64Bank;
import waffleoRai_zeqer64.ZeqerCore;
import waffleoRai_zeqer64.ZeqerRom;
import waffleoRai_zeqer64.filefmt.NusRomInfo;
import waffleoRai_zeqer64.filefmt.RomInfoNode;

public class Test_BookAnalysis {
	
	private static class BankInfo{
		public long offset;
		public long size;
		public int warc_1;
		public int warc_2;
		public int inst_count;
		public int perc_count;
		public int sfx_count;
	}
	
	public static void printUsage(){
		System.out.println("Usage:");
		System.out.println("\tjava Test_BookAnalysis [inpath] [outdir]");
	}

	private static void analyzeSamples(String inpath, String outdir) throws IOException{
		
		//Load ROM
		RomInfoNode rin = ZeqerCore.getActiveCore().detectROM(inpath);
		if(rin == null){
			System.err.println("ROM was not recognized: " + inpath);
			printUsage();
			System.exit(1);
		}
		if(!(rin instanceof NusRomInfo)){
			//I'll probably fix this eventually, but I'm lazy
			System.err.println("ROM recognized, but not N64 ROM: " + inpath);
			System.err.println("Please use direct N64 ROMs for this tool.");
			printUsage();
			System.exit(1);
		}
		System.err.println("N64 ROM Detected: " + rin.getROMName());
		NusRomInfo rinfo = (NusRomInfo)rin;
		ZeqerRom rom = new ZeqerRom(inpath, rinfo);
		
		//Read code tables...
		BankInfo[] bank_tbl;
		FileBuffer f_code = rom.loadCode();
		f_code.setCurrentPosition(rinfo.getCodeOffset_bnktable());
		int bcount = Short.toUnsignedInt(f_code.nextShort());
		f_code.skipBytes(14L);
		bank_tbl = new BankInfo[bcount];
		for(int i = 0; i < bcount; i++){
			BankInfo binfo = new BankInfo();
			bank_tbl[i] = binfo;
			binfo.offset = Integer.toUnsignedLong(f_code.nextInt());
			binfo.size = Integer.toUnsignedLong(f_code.nextInt());
			f_code.skipBytes(2L); //Don't need cache and medium
			binfo.warc_1 = (int)f_code.nextByte();
			binfo.warc_2 = (int)f_code.nextByte();
			binfo.inst_count = Byte.toUnsignedInt(f_code.nextByte());
			binfo.perc_count = Byte.toUnsignedInt(f_code.nextByte());
			binfo.sfx_count = Short.toUnsignedInt(f_code.nextShort());
		}
		int warc_count = (int)f_code.shortFromFile(rinfo.getCodeOffset_audtable());
		
		//Scan banks for samples
		List<Map<Integer, Z64WaveInfo>> waveinfos = new ArrayList<Map<Integer, Z64WaveInfo>>(warc_count);
		for(int i = 0; i < warc_count; i++) waveinfos.add(new HashMap<Integer, Z64WaveInfo>());
		
		FileBuffer f_audiobank = rom.loadAudiobank();
		for(int i = 0; i < bcount; i++){
			BankInfo binfo = bank_tbl[i];
			FileBuffer bnkdat = f_audiobank.createReadOnlyCopy(binfo.offset, binfo.offset + binfo.size);
			Z64Bank bnk = Z64Bank.readBank(bnkdat, binfo.inst_count, binfo.perc_count, binfo.sfx_count);
			bnkdat.dispose();
			
			int widx = binfo.warc_1;
			if(widx == 1) widx = 0;
			Map<Integer, Z64WaveInfo> warcmap = waveinfos.get(widx);
			List<Z64WaveInfo> bnkwaves = bnk.getAllWaveInfoBlocks();
			for(Z64WaveInfo wib : bnkwaves){
				int off = wib.getWaveOffset();
				if(!warcmap.containsKey(off)){
					warcmap.put(off, wib);
				}
			}
		}
		
		//Write to table...
		String csv_path = outdir + File.separator + rinfo.getZeqerID() + "__sampleinfotbl.csv";
		BufferedWriter bw = new BufferedWriter(new FileWriter(csv_path));
		//Header
		bw.write("#BANK_IDX,SAMPLE_IDX,CODEC,MEDIUM,OFFSET,LENGTH,LOOP_ST,LOOP_ED,LOOP_CT,P_ORDER,P_COUNT,ADPCM_BOOK...\n");
		for(int i = 0; i < warc_count; i++){
			Map<Integer, Z64WaveInfo> warcmap = waveinfos.get(i);
			if(warcmap.isEmpty()) continue;
			List<Integer> keys = new ArrayList<Integer>(warcmap.size());
			keys.addAll(warcmap.keySet());
			Collections.sort(keys);
			
			int j = 0;
			for(Integer k : keys){
				Z64WaveInfo winfo = warcmap.get(k);
				N64ADPCMTable adpcm_book = winfo.getADPCMBook();
				bw.write(Integer.toString(i) + ",");
				bw.write(Integer.toString(j++) + ",");
				bw.write(winfo.getCodec() + ",");
				bw.write(winfo.getMedium() + ",");
				bw.write("0x" + Integer.toHexString(winfo.getWaveOffset()) + ",");
				bw.write("0x" + Integer.toHexString(winfo.getWaveSize()) + ",");
				bw.write(winfo.getLoopStart() + ",");
				bw.write(winfo.getLoopEnd() + ",");
				bw.write(winfo.getLoopCount() + ",");
				bw.write(adpcm_book.getOrder() + ",");
				bw.write(Integer.toString(adpcm_book.getPredictorCount()));
				short[] ptbl = adpcm_book.getAsRaw();
				for(int n = 0; n < ptbl.length; n++){
					bw.write(",");
					bw.write(Short.toString(ptbl[n]));
				}
				bw.write("\n");
			}
		}
		bw.close();
	}
	
	public static void main(String[] args) {
		if(args.length < 2){
			System.err.println("Insufficient args! Expected: rom_path output_dir");
			System.exit(1);
		}
		
		String inpath = args[0];
		String outdir = args[1];
		
		try{
			analyzeSamples(inpath, outdir);
		}
		catch(Exception ex){
			ex.printStackTrace();
			System.exit(1);
		}
	}

}
