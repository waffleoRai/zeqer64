package waffleoRai_zeqer64.extract;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_zeqer64.ZeqerCore;
import waffleoRai_zeqer64.ZeqerRom;
import waffleoRai_zeqer64.filefmt.AbldFile;
import waffleoRai_zeqer64.filefmt.ZeqerBankTable;
import waffleoRai_zeqer64.filefmt.ZeqerPresetTable;
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
		this(rom, ZeqerCore.getActiveCore().getProgramDirectory());
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
		String wavdir = dir_base + SEP + ZeqerCore.DIRNAME_WAVE + SEP + ZeqerCore.DIRNAME_ZWAVE;
		String bnkdir = dir_base + SEP + ZeqerCore.DIRNAME_BANK + SEP + ZeqerCore.DIRNAME_ZBANK;
		String seqdir = dir_base + SEP + ZeqerCore.DIRNAME_SEQ + SEP + ZeqerCore.DIRNAME_ZSEQ;
		String blddir = dir_base + SEP + ZeqerCore.DIRNAME_ABLD + SEP + ZeqerCore.DIRNAME_ZBLD;
		String zid = z_rom.getRomInfo().getZeqerID();
		
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
		
		//Banks & Presets
		if(verbose) System.err.println("Mapping wave offsets to UIDs...");
		if(write_perm) bnkex.setSysPermission(true);
		//Reload wav id map for banks
		List<Map<Integer, Integer>> wavmap = ZeqerWaveTable.loadVersionWaveOffsetIDMap(wavdir, zid);
		
		if(verbose) System.err.println("Extracting soundbanks...");
		if(!bnkex.extractBanks(wavmap)) return false;
		
		//Seqs
		if(verbose) System.err.println("Extracting seqs...");
		if(write_perm) {
			seqex.setSysMode();
			//if(z_rom.getRomInfo().isZ5()) seqex.setSysZ5Mode();
			//else seqex.setSysZ6Mode();
		}
		if(!seqex.extractSeqs()) return false;
		
		if(verbose) System.err.println("Mapping soundbanks to seqs...");
		int[] buids = ZeqerBankTable.loadVersionTable(bnkdir, zid);
		if(buids == null) return false;
		if(!seqex.mapSeqBanks(buids)) return false;
		
		//generate abld
		//Load id tables.
		if(!FileBuffer.directoryExists(blddir)) Files.createDirectories(Paths.get(blddir));
		int[][][] wuids = ZeqerWaveTable.loadVersionTable(wavdir, zid);
		int[] suids = ZeqerSeqTable.loadVersionTable(seqdir, zid);
		AbldFile abld = AbldFile.fromROM(z_rom, suids, buids, wuids);
		good = good && abld.serializeTo(blddir + SEP + zid + ".abld");
		
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
			ZeqerCore core = ZeqerCore.getActiveCore();
			if(core == null){
				core = ZeqerCore.instantiateActiveCore(outdir, true);
			}
			
			//Try to detect ROM
			ZeqerRom rom = core.loadNUSROM(inpath);
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
			String srcdir = outdir + SEP + ZeqerCore.DIRNAME_WAVE + SEP + ZeqerCore.DIRNAME_ZWAVE;
			String srcpath = srcdir + SEP + ZeqerCore.FN_SYSWAVE;
			if(!FileBuffer.directoryExists(tblpath)) Files.createDirectories(Paths.get(tblpath));
			ZeqerWaveTable wtbl = ZeqerWaveTable.readTable(FileBuffer.createBuffer(srcpath, true));
			wtbl.exportTo(tblpath, null);

			tblpath = tbldir + SEP + "bnk";
			srcdir = outdir + SEP + ZeqerCore.DIRNAME_BANK + SEP + ZeqerCore.DIRNAME_ZBANK;
			srcpath = srcdir + SEP + ZeqerCore.FN_SYSBANK;
			if(!FileBuffer.directoryExists(tblpath)) Files.createDirectories(Paths.get(tblpath));
			ZeqerBankTable btbl = ZeqerBankTable.readTable(FileBuffer.createBuffer(srcpath, true));
			btbl.exportTo(tblpath);
			
			srcpath = srcdir + SEP + ZeqerCore.FN_SYSPRESET;
			if(!FileBuffer.directoryExists(tblpath)) Files.createDirectories(Paths.get(tblpath));
			ZeqerPresetTable ptbl = ZeqerPresetTable.readTable(FileBuffer.createBuffer(srcpath, true));
			ptbl.exportTo(tblpath);
			
			tblpath = tbldir + SEP + "seq";
			srcpath = srcdir + SEP + ZeqerCore.FN_SYSSEQ;
			if(!FileBuffer.directoryExists(tblpath)) Files.createDirectories(Paths.get(tblpath));
			if(FileBuffer.fileExists(srcpath)){
				ZeqerSeqTable stbl = ZeqerSeqTable.readTable(FileBuffer.createBuffer(srcpath, true));
				stbl.exportTo(tblpath, srcdir);
			}
			
			srcdir = outdir + SEP + ZeqerCore.DIRNAME_BANK + SEP + ZeqerCore.DIRNAME_ZBANK;
			int[] verids = ZeqerBankTable.loadVersionTable(srcdir, rom.getRomInfo().getZeqerID());
			System.out.println("Banks Found: " + verids.length);
			for(int id : verids) System.out.println(String.format("%08x", id));
			
			srcdir = outdir + SEP + ZeqerCore.DIRNAME_SEQ + SEP + ZeqerCore.DIRNAME_ZSEQ;
			verids = ZeqerSeqTable.loadVersionTable(srcdir, rom.getRomInfo().getZeqerID());
			System.out.println("Seqs Found: " + verids.length);
			for(int id : verids) System.out.println(String.format("%08x", id));
			
		}
		catch(Exception ex){
			ex.printStackTrace();
			System.exit(1);
		}
		
		
	}

}
