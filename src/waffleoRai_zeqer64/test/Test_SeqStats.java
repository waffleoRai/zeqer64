package waffleoRai_zeqer64.test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import waffleoRai_SeqSound.SeqVoiceCounter;
import waffleoRai_SeqSound.n64al.NUSALSeq;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_zeqer64.SoundTables.SeqInfoEntry;
import waffleoRai_zeqer64.ZeqerCore;
import waffleoRai_zeqer64.ZeqerRom;
import waffleoRai_zeqer64.filefmt.NusRomInfo;
import waffleoRai_zeqer64.filefmt.RomInfoNode;

public class Test_SeqStats {
	
	public static void doRom(String inpath, String outstem) throws IOException{
		//TODO
		RomInfoNode rin = ZeqerCore.detectROM(inpath);
		if(rin == null){
			System.err.println("ROM was not recognized: " + inpath);
			System.exit(1);
		}
		if(!(rin instanceof NusRomInfo)){
			//I'll probably fix this eventually, but I'm lazy
			System.err.println("ROM recognized, but not N64 ROM: " + inpath);
			System.err.println("Please use direct N64 ROMs for this tool.");
			System.exit(1);
		}
		System.err.println("N64 ROM Detected: " + rin.getROMName());
		NusRomInfo rinfo = (NusRomInfo)rin;
		
		ZeqerRom rom = new ZeqerRom(inpath, rinfo);
		String outpath = outstem + "_" + rinfo.getZeqerID() + "_seqstats.csv";
		FileBuffer audioseq = rom.loadAudioseq();
		SeqInfoEntry[] seqtbl = rom.loadSeqEntries();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(outpath));
		bw.write("SEQ_IDX,SIZE,SIZE_DEC,MAXVOX\n");
		for(int i = 0; i < seqtbl.length; i++){
			bw.write(i + ",");
			int size = seqtbl[i].getSize();
			bw.write("0x" + Integer.toHexString(size) + ",");
			bw.write(Integer.toString(size) + ",");
			if(size < 1){
				bw.write("0\n");
				continue;
			}
			
			//Try to load the seq...
			int stpos = seqtbl[i].getOffset();
			int edpos = stpos + size;
			FileBuffer seqdat = audioseq.createReadOnlyCopy(stpos, edpos);
			try{
				NUSALSeq seq = NUSALSeq.readNUSALSeq(seqdat);
				SeqVoiceCounter vc = new SeqVoiceCounter();
				seq.playTo(vc, false);
				bw.write(vc.getMaxTotalVoiceCount() + "\n");
			}
			catch(Exception x){
				System.err.println("Couldn't read seq " + i + " - skipping voice count...");
				bw.write("-1\n");
			}
		}
		bw.close();
	}
	
	public static void main(String[] args) {
	
		//args: output input0 input1...
	
		if(args.length < 2){
			System.err.println("Need at least two arguments! Usage: outstem input_rom0 input_rom1...");
			System.exit(1);
		}
		
		try{
			String outstem = args[0];
			List<String> inpaths = new ArrayList<String>(args.length);
			for(int i = 1; i < args.length; i++) inpaths.add(args[i]);
			for(String inpath : inpaths){
				doRom(inpath, outstem);
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
			System.exit(1);
		}
		
	}

}
