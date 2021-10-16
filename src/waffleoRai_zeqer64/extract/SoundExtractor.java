package waffleoRai_zeqer64.extract;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_zeqer64.ZeqerCore;
import waffleoRai_zeqer64.ZeqerRom;
import waffleoRai_zeqer64.filefmt.ZeqerBankTable;
import waffleoRai_zeqer64.filefmt.ZeqerSeqTable;
import waffleoRai_zeqer64.filefmt.ZeqerWaveTable;

public class SoundExtractor {
	
	/*----- Constants -----*/
	
	private static final char SEP = File.separatorChar;
	
	/*----- Instance Variables -----*/
	
	private boolean write_perm = false;
	private String dir_base;
	private ZeqerRom z_rom;
	
	/*----- Init -----*/
	
	public SoundExtractor(ZeqerRom rom) throws IOException{
		this(rom, ZeqerCore.getProgramDirectory());
	}
	
	public SoundExtractor(ZeqerRom rom, String base_dir) throws IOException{
		if(rom == null) throw new IllegalArgumentException("ROM is required for extraction.");
		dir_base = base_dir;
		z_rom = rom;
	}
	
	/*----- Setters -----*/
	
	protected void setSysMode(boolean b){write_perm = b;}
	
	/*----- Dump -----*/
	
	public boolean dumpSoundData(boolean verbose) throws IOException{
		String wavdir = dir_base + SEP + ZeqerCore.DIRNAME_WAVE;
		String bnkdir = dir_base + SEP + ZeqerCore.DIRNAME_BANK + SEP + ZeqerCore.DIRNAME_ZBANK;
		String seqdir = dir_base + SEP + ZeqerCore.DIRNAME_SEQ + SEP + ZeqerCore.DIRNAME_ZSEQ;
		
		if(verbose) System.err.println("Creating output directories...");
		if(!FileBuffer.directoryExists(wavdir)) Files.createDirectories(Paths.get(wavdir));
		if(!FileBuffer.directoryExists(bnkdir)) Files.createDirectories(Paths.get(bnkdir));
		if(!FileBuffer.directoryExists(seqdir)) Files.createDirectories(Paths.get(seqdir));
		
		if(verbose) System.err.println("Initializing extractors...");
		boolean good = true;
		WaveExtractor wavex = new WaveExtractor(wavdir, z_rom);
		BankExtractor bnkex = new BankExtractor(z_rom, bnkdir);
		SeqExtractor seqex = new SeqExtractor(z_rom, seqdir);
		
		//Sound waves
		if(verbose) System.err.println("Extracting audio waves...");
		if(write_perm) wavex.setWaveTableWriteEnabled(true);
		if(!wavex.extractWaves()) return false;
		
		//Banks
		if(verbose) System.err.println("Mapping wave offsets to UIDs...");
		if(write_perm) bnkex.setSysPermission(true);
		//Reload wav id map for banks
		Map<Integer, Integer> wavmap = new TreeMap<Integer, Integer>();
		String tblpath = wavex.getIDTablePath();
		System.err.println("tblpath = " + tblpath);
		if(!FileBuffer.fileExists(tblpath)) return false;
		FileBuffer buffer = FileBuffer.createBuffer(tblpath, true);
		int ecount = (int)buffer.getFileSize() >>> 3;
		buffer.setCurrentPosition(0L);
		for(int i = 0; i < ecount; i++){
			int id = buffer.nextInt();
			int pos = buffer.nextInt();
			wavmap.put(pos, id);
		}
		
		if(verbose) System.err.println("Extracting soundbanks...");
		if(!bnkex.extractBanks(wavmap)) return false;
		
		//Seqs
		if(verbose) System.err.println("Extracting seqs...");
		if(write_perm) {
			if(z_rom.getRomInfo().isZ5()) seqex.setSysZ5Mode();
			else seqex.setSysZ6Mode();
		}
		if(!seqex.extractSeqs()) return false;
		
		if(verbose) System.err.println("Mapping soundbanks to seqs...");
		tblpath = bnkex.getIDTablePath();
		if(!FileBuffer.fileExists(tblpath)) return false;
		buffer = FileBuffer.createBuffer(tblpath, true);
		ecount = (int)buffer.getFileSize() >>> 2;
		if(ecount == 0) return false;
		int[] buids = new int[ecount];
		buffer.setCurrentPosition(0L);
		for(int i = 0; i < ecount; i++){
			int id = buffer.nextInt();
			buids[i] = id;
		}
		if(!seqex.mapSeqBanks(buids)) return false;
		
		return good;
	}
	
	/*----- Sys Dump -----*/
	
	public static void main(String[] args){
		if(args == null || args.length < 2){
			System.err.println("Insufficient args! Expect: input_rom_path, output_dir");
			System.exit(1);
		}
		
		String inpath = args[0];
		String outdir = args[1];
		
		System.err.println("Input ROM: " + inpath);
		System.err.println("Output Directory: " + outdir);
		
		try{
			//Try to detect ROM
			ZeqerRom rom = ZeqerCore.loadNUSROM(inpath);
			if(rom == null){
				System.err.println("ROM was not recognized. Exiting...");
				System.exit(1);
			}
			System.err.println("ROM Detected: " + rom.getRomInfo().getZeqerID());
			
			SoundExtractor sndex = new SoundExtractor(rom, outdir);
			sndex.setSysMode(true);
			if(!sndex.dumpSoundData(true)){
				System.err.println("Extraction failed! See stderr for details...");
				System.exit(1);
			}
			
			//Also dump to tsv
			System.err.println("Extraction succeeded! Translating to tsv...");
			String tbldir = outdir + SEP + "dbgtbl";
			
			String tblpath = tbldir + SEP + "wav";
			String srcdir = outdir + SEP + ZeqerCore.DIRNAME_WAVE;
			String srcpath = srcdir + SEP + ZeqerCore.FN_SYSWAVE;
			if(!FileBuffer.directoryExists(tblpath)) Files.createDirectories(Paths.get(tblpath));
			ZeqerWaveTable wtbl = ZeqerWaveTable.readTable(FileBuffer.createBuffer(srcpath, true));
			wtbl.exportTo(tblpath, srcdir);
			
			tblpath = tbldir + SEP + "bnk";
			srcdir = outdir + SEP + ZeqerCore.DIRNAME_BANK + SEP + ZeqerCore.DIRNAME_ZBANK;
			srcpath = srcdir + SEP + ZeqerCore.FN_SYSBANK;
			if(!FileBuffer.directoryExists(tblpath)) Files.createDirectories(Paths.get(tblpath));
			ZeqerBankTable btbl = ZeqerBankTable.readTable(FileBuffer.createBuffer(srcpath, true));
			btbl.exportTo(tblpath);
			
			tblpath = tbldir + SEP + "seq" + SEP + "z5";
			srcdir = outdir + SEP + ZeqerCore.DIRNAME_SEQ + SEP + ZeqerCore.DIRNAME_ZSEQ;
			srcpath = srcdir + SEP + ZeqerCore.FN_SYSSEQ_OOT;
			if(!FileBuffer.directoryExists(tblpath)) Files.createDirectories(Paths.get(tblpath));
			if(FileBuffer.fileExists(srcpath)){
				ZeqerSeqTable stbl = ZeqerSeqTable.readTable(FileBuffer.createBuffer(srcpath, true));
				stbl.exportTo(tblpath, srcdir);
			}
			
			tblpath = tbldir + SEP + "seq" + SEP + "z6";
			srcpath = srcdir + SEP + ZeqerCore.FN_SYSSEQ_MM;
			if(!FileBuffer.directoryExists(tblpath)) Files.createDirectories(Paths.get(tblpath));
			if(FileBuffer.fileExists(srcpath)){
				ZeqerSeqTable stbl = ZeqerSeqTable.readTable(FileBuffer.createBuffer(srcpath, true));
				stbl.exportTo(tblpath, srcdir);
			}
			
		}
		catch(Exception ex){
			ex.printStackTrace();
			System.exit(1);
		}
		
		
	}

}
