package waffleoRai_zeqer64.test;

import java.io.File;
import java.util.Collection;
import java.util.List;

import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_soundbank.nintendo.z64.Z64Bank;
import waffleoRai_soundbank.nintendo.z64.Z64Drum;
import waffleoRai_soundbank.nintendo.z64.Z64Instrument;
import waffleoRai_soundbank.nintendo.z64.Z64SoundEffect;
import waffleoRai_zeqer64.ZeqerCore;
import waffleoRai_zeqer64.ZeqerRom;
import waffleoRai_zeqer64.engine.EngineBankInfo;
import waffleoRai_zeqer64.engine.EngineWaveArcInfo;
import waffleoRai_zeqer64.extract.RomExtractionSummary;
import waffleoRai_zeqer64.extract.SoundExtractor;
import waffleoRai_zeqer64.filefmt.AbldFile;
import waffleoRai_zeqer64.filefmt.ZeqerWaveTable;
import waffleoRai_zeqer64.filefmt.ZeqerWaveTable.WaveTableEntry;

public class Test_RegenSysdata {
	
	private static void flagVersion(ZeqerWaveTable wtbl, AbldFile abld, int v_flag, int ma_flag){
		List<EngineWaveArcInfo> warcs = abld.getWaveArcs();
		int j = 0;
		for(EngineWaveArcInfo warc : warcs){
			if(!warc.isReference()){
				List<Integer> samps = warc.getSamples();
				for(Integer samp_uid : samps){
					WaveTableEntry entry = wtbl.getEntryWithUID(samp_uid);
					if(entry != null){
						entry.setFlags(v_flag);
						if(j == 0){
							entry.setFlags(ma_flag);
						}
					}
				}
			}
			j++;
		}
	}
	
	private static void scanBank(ZeqerWaveTable wtbl, AbldFile abld){
		List<EngineBankInfo> banklist = abld.getBanks();
		for(EngineBankInfo bnk : banklist){
			int bank_uid = bnk.getBankUID();
			Z64Bank loaded_bnk = ZeqerCore.getActiveCore().loadBank(bank_uid);
			if(loaded_bnk != null){
				Z64WaveInfo winfo = null;
				Collection<Z64Instrument> insts = loaded_bnk.getAllInstruments();
				for(Z64Instrument inst : insts){
					winfo = inst.getSampleHigh();
					if(winfo != null){
						WaveTableEntry entry = wtbl.getEntryWithUID(winfo.getUID());
						if(entry != null){
							entry.clearFlags(ZeqerWaveTable.FLAG_ISUNUSED);
							entry.setFlags(ZeqerWaveTable.FLAG_ISIN_INST);
						}
					}
					
					winfo = inst.getSampleMiddle();
					if(winfo != null){
						WaveTableEntry entry = wtbl.getEntryWithUID(winfo.getUID());
						if(entry != null){
							entry.clearFlags(ZeqerWaveTable.FLAG_ISUNUSED);
							entry.setFlags(ZeqerWaveTable.FLAG_ISIN_INST);
						}
					}
					
					winfo = inst.getSampleLow();
					if(winfo != null){
						WaveTableEntry entry = wtbl.getEntryWithUID(winfo.getUID());
						if(entry != null){
							entry.clearFlags(ZeqerWaveTable.FLAG_ISUNUSED);
							entry.setFlags(ZeqerWaveTable.FLAG_ISIN_INST);
						}
					}
				}
				
				Collection<Z64Drum> drums = loaded_bnk.getAllDrums();
				for(Z64Drum drum : drums){
					winfo = drum.getSample();
					if(winfo != null){
						WaveTableEntry entry = wtbl.getEntryWithUID(winfo.getUID());
						if(entry != null){
							entry.clearFlags(ZeqerWaveTable.FLAG_ISUNUSED);
							entry.setFlags(ZeqerWaveTable.FLAG_ISIN_PERC);
						}
					}
				}
				
				Z64SoundEffect[] effects = loaded_bnk.getSFXSet();
				if(effects != null){
					for(int e = 0; e < effects.length; e++){
						if(effects[e] == null) continue;
						winfo = effects[e].getSample();
						if(winfo != null){
							WaveTableEntry entry = wtbl.getEntryWithUID(winfo.getUID());
							if(entry != null){
								entry.clearFlags(ZeqerWaveTable.FLAG_ISUNUSED);
								entry.setFlags(ZeqerWaveTable.FLAG_ISIN_SFX);
							}
						}
					}
				}
			}
		}
	}

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
			ZeqerCore core = ZeqerCore.getActiveCore();
			if(core == null){
				core = ZeqerCore.instantiateActiveCore(outpath, true);	
			}
			
			boolean oot_10 = false;
			boolean oot = false;
			boolean mm = false;
			AbldFile abld = null;
			
			//Extract
			for(int i = 0; i < inrom_count; i++){
				//SoundExtractor.main(new String[]{inpaths[i], outpath});
				ZeqerRom z_rom = core.loadNUSROM(inpaths[i]);
				System.err.println("Now processing ROM: " + z_rom.getRomInfo().getZeqerID());
				RomExtractionSummary errinfo = core.extractSoundDataFromRom(z_rom);
				//TODO outpath?
				
				//Figure out ID of ROM just added. If version hasn't been
				//	scanned yet, do wave flagging.
				if(z_rom != null){
					ZeqerWaveTable wtbl = core.getZeqerWaveTable();
					abld = core.loadSysBuild(z_rom.getRomInfo().getZeqerID());
					int ver = z_rom.getRomInfo().getGameVersionEnum();
					switch(ver){
					case ZeqerRom.GAME_OCARINA_V1_0:
					case ZeqerRom.GAME_OCARINA_V0_9:
						if(!oot_10){
							flagVersion(wtbl, abld, ZeqerWaveTable.FLAG_ISIN_OOTv0, ZeqerWaveTable.FLAG_ISIN_OOTv0_MAINARC);
							oot_10 = true;
						}
						break;
					case ZeqerRom.GAME_OCARINA_V1_1:
					case ZeqerRom.GAME_OCARINA_V1_2:
					case ZeqerRom.GAME_OCARINA_MQ_V1_1:
					case ZeqerRom.GAME_OCARINA_MQ_V1_2:
					case ZeqerRom.GAME_OCARINA_MQDBG_V1_1:
					case ZeqerRom.GAME_OCARINA_GC_V1_1:
					case ZeqerRom.GAME_OCARINA_GC_V1_2:
						if(!oot){
							flagVersion(wtbl, abld, ZeqerWaveTable.FLAG_ISIN_OOT, ZeqerWaveTable.FLAG_ISIN_OOT_MAINARC);
							oot = true;
						}
						break;
					case ZeqerRom.GAME_MAJORA_V1_0_J:
					case ZeqerRom.GAME_MAJORA_V1_0_I:
					case ZeqerRom.GAME_MAJORA_GC:
						if(!mm){
							flagVersion(wtbl, abld, ZeqerWaveTable.FLAG_ISIN_MM, ZeqerWaveTable.FLAG_ISIN_MM_MAINARC);
							mm = true;
						}
						break;
					}
					
					//Scan banks to adjust inst usage flags...
					scanBank(wtbl, abld);
					ZeqerCore.getActiveCore().saveZeqerTables();
				}
			}
			
			//Metadata (assumes table is already there)
			String pmeta_wav = outpath + File.separator + "meta_wav.tsv";
			String pmeta_prs = outpath + File.separator + "meta_prs.tsv";
			String pmeta_bnk = outpath + File.separator + "meta_bnk.tsv";
			String pmeta_seq = outpath + File.separator + "meta_seq.tsv";
			String pmeta_slb = outpath + File.separator + "meta_seqlbl.tsv";
			String pmeta_sal = outpath + File.separator + "meta_seqioa.tsv";
			
			if(FileBuffer.fileExists(pmeta_wav) && FileBuffer.fileExists(pmeta_seq) && FileBuffer.fileExists(pmeta_bnk)){
				ImportMetadata.main(new String[]{outpath, pmeta_wav, pmeta_seq, pmeta_prs, pmeta_bnk, pmeta_slb, pmeta_sal});
			}
			
		}
		catch(Exception ex){
			ex.printStackTrace();
			System.exit(1);
		}
		
	}

}
