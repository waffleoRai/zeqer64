package waffleoRai_zeqer64.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_soundbank.nintendo.z64.Z64Bank;
import waffleoRai_soundbank.nintendo.z64.Z64Drum;
import waffleoRai_soundbank.nintendo.z64.Z64Instrument;
import waffleoRai_zeqer64.ZeqerCore;
import waffleoRai_zeqer64.ZeqerPreset;
import waffleoRai_zeqer64.filefmt.ZeqerBankTable;
import waffleoRai_zeqer64.filefmt.ZeqerPresetTable;
import waffleoRai_zeqer64.filefmt.ZeqerSeqTable;
import waffleoRai_zeqer64.filefmt.ZeqerWaveTable;
import waffleoRai_zeqer64.filefmt.ZeqerBankTable.BankTableEntry;
import waffleoRai_zeqer64.presets.ZeqerInstPreset;
import waffleoRai_zeqer64.presets.ZeqerPercPreset;

public class ImportMetadata {
	
	private static void updateInstNames(ZeqerBankTable bnk_tbl, ZeqerPresetTable prs_tbl){
		Collection<BankTableEntry> bentries = bnk_tbl.getAllEntries();
		for(BankTableEntry e : bentries){
			Z64Bank zbank = ZeqerCore.getActiveCore().loadBank(e.getUID());
			if(zbank == null){
				System.err.println("ImportMetadata.updateInstNames || WARNING: Could not load data for bank " + String.format("%08x", e.getUID()));
			}
			else{
				Collection<Z64Instrument> ilist = zbank.getAllInstruments();
				for(Z64Instrument inst : ilist){
					ZeqerInstPreset preset = new ZeqerInstPreset(inst);
					int hash_uid = preset.hashToUID();
					preset.setUID(hash_uid);
					ZeqerPreset match = prs_tbl.getPreset(hash_uid);
					if(match != null){
						inst.setName(match.getName());
					}
				}
				
				//Perc
				if(e.getPercussionCount() > 0){
					Z64Drum[] drums = zbank.getPercussionSet();
					ZeqerPercPreset ppreset = new ZeqerPercPreset(-1);
					for(int j = 0; j < 64; j++){
						if(drums[j] != null) ppreset.setDrumToSlot(j, drums[j]);
					}
					int hash_uid = ppreset.hashToUID();
					ppreset.setUID(hash_uid);
					ZeqerPreset match = prs_tbl.getPreset(hash_uid);
					if(match != null){
						Collection<Z64Drum> udrums = zbank.getAllDrums();
						int j = 0;
						for(Z64Drum d : udrums){
							d.setName(match.getName() + " - " + (char)('A'+j));
						}
					}
				}
				//TODO Save bank files back
			}
		}
	}

	public static void main(String[] args) {
		/*
		 * Command Line Args:
		 * [zeqer_dir] [wav_info.tsv] [seq_info.tsv] [inst_info.tsv] [inst_info.tsv] [font_info.tsv]
		 */
		
		if(args.length < 5){
			System.err.println("Insufficient args. Expected:");
			System.err.println("[zeqer_dir] [wav_info.tsv] [seq_info.tsv] [inst_info.tsv] [font_info.tsv]");
			System.exit(1);
		}
		
		String zdir = args[0];
		String wav_tsv_path = args[1];
		String seq_tsv_path = args[2];
		String ins_tsv_path = args[3];
		String bnk_tsv_path = args[4];
		
		try{
			ZeqerCore.getActiveCore().setProgramDirectory(zdir);
			ZeqerCore.getActiveCore().loadSoundTables();
			
			//Trim meta wav UIDs (I just exported MD5s because lazy)
			BufferedReader br = new BufferedReader(new FileReader(wav_tsv_path));
			//Check if UIDs or MD5s...
			String header = br.readLine();
			String line = br.readLine();
			String[] fields = line.split("\t");
			int cols = header.split("\t").length;
			if(fields[0].length() > 8){
				BufferedWriter bw = new BufferedWriter(new FileWriter(wav_tsv_path + ".tmp"));
				bw.write(header + "\n");
				while(line != null){
					fields = line.split("\t");
					if(fields == null || fields.length < 1) break;  
					bw.write(fields[0].substring(0,8));
					for(int i = 1; i < fields.length; i++){
						bw.write("\t");
						bw.write(fields[i]);
					}
					if(fields.length < cols){
						for(int i = fields.length; i < cols; i++){
							bw.write("\t");
						}
					}
					bw.write("\n");
					line = br.readLine();
				}
				bw.close();
			}
			br.close();
			
			if(FileBuffer.fileExists(wav_tsv_path + ".tmp")){
				Files.move(Paths.get(wav_tsv_path), Paths.get(wav_tsv_path + "_old.tsv"));
				Files.move(Paths.get(wav_tsv_path + ".tmp"), Paths.get(wav_tsv_path));
			}
			
			//Update wav table
			ZeqerWaveTable tbl_wav = ZeqerCore.getActiveCore().getZeqerWaveTable();
			tbl_wav.importTSV(wav_tsv_path);
			
			//Update seq tables
			/*ZeqerSeqTable tbl_seq5 = ZeqerCore.getZeqerZ5SeqTable();
			tbl_seq5.importTSV(seq_tsv_path);
			ZeqerSeqTable tbl_seq6 = ZeqerCore.getZeqerZ6SeqTable();
			tbl_seq6.importTSV(seq_tsv_path);*/
			ZeqerSeqTable tbl_seq = ZeqerCore.getActiveCore().getZeqerSeqTable();
			tbl_seq.importTSV(seq_tsv_path);
			
			//Update preset table
			ZeqerPresetTable tbl_prs = ZeqerCore.getActiveCore().getZeqerPresetTable();
			tbl_prs.importTSV(ins_tsv_path);
			
			//Update bank table (don't forget inst names)
			ZeqerBankTable tbl_bnk = ZeqerCore.getActiveCore().getZeqerBankTable();
			tbl_bnk.importTSV(bnk_tsv_path);
			updateInstNames(tbl_bnk, tbl_prs);
			
			//Close, then reload tables
			ZeqerCore.getActiveCore().saveZeqerTables();
			ZeqerCore.getActiveCore().loadSoundTables();
			
			//Print tables to tsv
			tbl_wav = ZeqerCore.getActiveCore().getZeqerWaveTable();
			tbl_wav.exportTo(zdir, zdir + File.separator + "wav");
			/*tbl_seq5 = ZeqerCore.getZeqerZ5SeqTable();
			tbl_seq5.exportTo(zdir, zdir + File.separator + "seq" + File.separator + "zseqs");
			Files.move(Paths.get(zdir + File.separator + "_zuseq_tbl.tsv"), Paths.get(zdir + File.separator + "_zuseq5_tbl.tsv"));
			tbl_seq6 = ZeqerCore.getZeqerZ6SeqTable();
			tbl_seq6.exportTo(zdir, zdir + File.separator + "seq" + File.separator + "zseqs");
			Files.move(Paths.get(zdir + File.separator + "_zuseq_tbl.tsv"), Paths.get(zdir + File.separator + "_zuseq6_tbl.tsv"));*/
			tbl_seq = ZeqerCore.getActiveCore().getZeqerSeqTable();
			tbl_seq.exportTo(zdir, zdir + File.separator + "seq");
			tbl_prs = ZeqerCore.getActiveCore().getZeqerPresetTable();
			tbl_prs.exportTo(zdir);
			tbl_bnk = ZeqerCore.getActiveCore().getZeqerBankTable();
			tbl_bnk.exportTo(zdir);
		}
		catch(Exception ex){
			ex.printStackTrace();
			System.exit(1);
		}

	}

}
