package waffleoRai_zeqer64.filefmt;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import waffleoRai_DataContainers.TallyMap;
import waffleoRai_Sound.nintendo.Z64Wave;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_soundbank.nintendo.z64.Z64Bank;
import waffleoRai_soundbank.nintendo.z64.Z64Drum;
import waffleoRai_soundbank.nintendo.z64.Z64Envelope;
import waffleoRai_soundbank.nintendo.z64.Z64Instrument;
import waffleoRai_soundbank.nintendo.z64.Z64SoundEffect;
import waffleoRai_zeqer64.ZeqerCore;
import waffleoRai_zeqer64.SoundTables.BankInfoEntry;
import waffleoRai_zeqer64.SoundTables.SeqInfoEntry;
import waffleoRai_zeqer64.SoundTables.WaveArcInfoEntry;
import waffleoRai_zeqer64.engine.EngineBankInfo;
import waffleoRai_zeqer64.engine.EngineSeqInfo;
import waffleoRai_zeqer64.engine.EngineTables;
import waffleoRai_zeqer64.engine.EngineWaveArcInfo;
import waffleoRai_zeqer64.filefmt.bank.SoundfontXML;
import waffleoRai_zeqer64.filefmt.seq.SeqTableEntry;
import waffleoRai_zeqer64.filefmt.wave.ZRetAifcFile;
import waffleoRai_zeqer64.filefmt.bank.BankTableEntry;

public class RecompFiles {
	
	public static final String XML_HEAD = "<?xml version=\"1.0\" ?>";
	
	public static final String[] CACHE_STRINGS = {"Permanent", "Persistent", "Temporary", "Any", "AnyNoSyncLoad"};
	public static final String[] MEDIUM_STRINGS = {"RAM", "Unknown", "Cartridge", "Disk Drive"};

	public static final String[] FILENAME_BAD_CHARS = {"?", ":", "/", "\\", "*", "\"", ">", "<", "|", ";"};
	
	public static final int BINBUILD_FLAGS_DEFAULT = 0x0;
	public static final int BINBUILD_FLAG_LITTLE_ENDIAN = 0x1;
	public static final int BINBUILD_FLAG_64BIT = 0x2;
	public static final int BINBUILD_FLAG_DEBUGMODE = 0x8000;
	
	public static final String TBLNAME_WARC = "gSampleBankTable";
	public static final String TBLNAME_BANK = "gSoundFontTable";
	public static final String TBLNAME_SEQ = "gSequenceTable";
	public static final String TBLNAME_SBMAP = "gSequenceFontTable";
	
	public static String genEnumStrFromName(String name){
		int ccount = name.length();
		StringBuilder sb = new StringBuilder(ccount);
		for(int i = 0; i < ccount; i++){
			char ch = name.charAt(i);
			if(ch == ' ' || ch == '-' || ch == '_'){
				sb.append('_');
			}
			else if(Character.isAlphabetic(ch)){
				sb.append(Character.toUpperCase(ch));
			}
			else if(Character.isDigit(ch)){
				if(i == 0) continue;
				else sb.append(ch);
			}
		}
		
		return sb.toString();
	}
	
	public static String cleanWaveName(String name){
		//Makes sure it doesn't contain any characters that will offend the file system
		//	when output aifc is named.
		for(String bad : FILENAME_BAD_CHARS){
			name = name.replace(bad, "");
		}
		return name;
	}
	
	public static boolean writeBanksXML(String dirpath, List<EngineWaveArcInfo> warcs) throws IOException{
		if(warcs == null || warcs.isEmpty()) return false;
		String outpath = dirpath + File.separator + "Banks.xml";
		BufferedWriter bw = new BufferedWriter(new FileWriter(outpath));
		bw.write(XML_HEAD); bw.write("\n");
		bw.write("<SampleBanks>\n");
		int warc_count = warcs.size();
		String[] banknames = new String[warc_count];
		int i = 0;
		for(EngineWaveArcInfo info : warcs){
			banknames[i] = i + " - " + info.getName();
			i++;
		}
		
		i = 0;
		for(EngineWaveArcInfo info : warcs){
			bw.write("\t<SampleBank");
			if(info.isReference()){
				bw.write(" Reference=\"");
				bw.write(banknames[info.getRefIndex()]);
			}
			else{
				bw.write(" Name=\"");	
				bw.write(banknames[i]);
			}
			bw.write("\" CachePolicy=\"");
			bw.write(CACHE_STRINGS[info.getCachePolicy()]);
			bw.write("\" Medium=\"");
			bw.write(MEDIUM_STRINGS[info.getMedium()]);
			bw.write("\"/>\n");
			i++;
		}
		bw.write("</SampleBanks>\n");
		bw.close();
		return true;
	}
	
	public static boolean outputSamples(String dirpath, EngineWaveArcInfo warc, int warc_idx) throws IOException{
		if(warc == null) return false;
		if(warc.isReference()) return true;
		String warcdir = dirpath + File.separator + warc_idx + " - " + warc.getName();
		if(!FileBuffer.directoryExists(warcdir)) Files.createDirectory(Paths.get(warcdir));
		
		List<Integer> samps = warc.getSamples();
		int i = 0;
		for(Integer suid : samps){
			String outstem = warcdir + File.separator + String.format("%03d", i) + " "; //Need wave info to get name
			Z64WaveInfo winfo = ZeqerCore.getActiveCore().getWaveInfo(suid);
			Z64Wave mywave = ZeqerCore.getActiveCore().loadWave(suid);
			if(winfo == null || mywave == null){
				System.err.println("ERROR: Could not find sample with UID " + String.format("%08x", suid));
				return false;
			}
			
			winfo.setName(cleanWaveName(winfo.getName()));
			String outpath = outstem + winfo.getName() + ".aifc";
			ZRetAifcFile.exportAIFC(mywave, outpath);
			
			i++;
		}
		
		return true;
	}
	
	public static boolean outputSoundfont(String dirpath, int idx, EngineBankInfo sf, String[] warc_names) throws IOException{
		//Load bank information
		if(sf == null) return false;
		int buid = sf.getBankUID();
		Z64Bank mybank = ZeqerCore.getActiveCore().loadBankData(buid);
		if(mybank == null) return false;
		BankTableEntry bankmeta = ZeqerCore.getActiveCore().getBankInfo(buid);
		if(bankmeta == null) return false;
		
		//Get envelopes.
		int i = 0;
		Set<String> enames = new HashSet<String>();
		Collection<Z64Envelope> envs = mybank.getAllEnvelopes();
		for(Z64Envelope env : envs){
			String ename = env.getName();
			if(ename == null || enames.contains(ename)){
				ename = String.format("ENV%03d", i++);
				env.setName(ename);
			}
			enames.add(ename);
		}
		
		//Open output files.
		bankmeta.setName(cleanWaveName(bankmeta.getName()));
		String bankfilename = String.format("%02d - %s", idx, bankmeta.getName());
		String xmlpath = dirpath + File.separator + bankfilename + ".xml";
		String hpath = String.format("sf%02d.h", idx);
		BufferedWriter xmlout = new BufferedWriter(new FileWriter(xmlpath));
		BufferedWriter hout = new BufferedWriter(new FileWriter(hpath));
		
		//Write XML head & basic font info.
		xmlout.write(XML_HEAD); xmlout.write("\n");
		xmlout.write("<Soundfont Medium=\"");
		xmlout.write(MEDIUM_STRINGS[sf.getMedium()]);
		xmlout.write("\" CachePolicy=\"");
		xmlout.write(CACHE_STRINGS[sf.getCachePolicy()]);
		xmlout.write("\">\n");
		
		//Header the h file
		hout.write("/*\n");
		hout.write("\tHeader auto-generated by Zeqer64\n");
		hout.write("\tSoundfont file constants\n");
		hout.write("\tID: "); hout.write(Integer.toString(idx)); hout.write("\n");
		hout.write("\tName: "); hout.write(bankfilename); hout.write("\n");
		hout.write("*/\n\n");
		
		//WArcs
		xmlout.write("\t<SampleBanks>\n");
		int warc_idx = sf.getWArc1();
		if(warc_idx >= 0){
			if(warc_names != null) SoundfontXML.writeSFBankElement(xmlout, 2, warc_names[warc_idx]);
			else SoundfontXML.writeSFBankElement(xmlout, 2, warc_idx);
		}
		xmlout.write("\t</SampleBanks>\n");
		
		//Instruments
		hout.write("/**** INSTRUMENTS ****/\n");
		xmlout.write("\t<Instruments>\n");
		Z64Instrument[] insts = mybank.getInstrumentPresets();
		if(insts != null){
			String h_prefix = String.format("F%d_I_", idx);
			for(i = 0; i < insts.length; i++){
				if(insts[i] == null) continue;
				String estr = mybank.getInstPresetEnumString(i);
				hout.write("#define ");
				hout.write(h_prefix); hout.write(estr);
				hout.write(" " + i); hout.write("\n");
				
				SoundfontXML.writeSFInstElement(xmlout, 2, i, insts[i], estr);
			}
		}
		xmlout.write("\t</Instruments>\n");
		hout.write("\n");
		
		//Drums
		if(mybank.getEffectivePercCount() > 0){
			hout.write("/**** DRUMS ****/\n");
			xmlout.write("\t<Drums>\n");
			Z64Drum[] pslots = mybank.getPercussionSet();
			if(pslots != null){
				String h_prefix = String.format("F%d_D_", idx);
				for(i = 0; i < pslots.length; i++){
					if(pslots[i] == null) continue;
					String estr = mybank.getDrumSlotEnumString(i);
					hout.write("#define ");
					hout.write(h_prefix); hout.write(estr);
					hout.write(" " + i); hout.write("\n");
					
					SoundfontXML.writeSFDrumElement(xmlout, 2, i, pslots[i], estr);
				}
			}
			xmlout.write("\t</Drums>\n");
			hout.write("\n");	
		}
		else xmlout.write("\t<Drums/>\n");
		
		//SFX
		if(mybank.getEffectiveSFXCount() > 0){
			hout.write("/**** EFFECTS ****/\n");
			xmlout.write("\t<SoundEffects>\n");
			Z64SoundEffect[] sfxslots = mybank.getSFXSet();
			if(sfxslots != null){
				String h_prefix = String.format("F%d_E_", idx);
				for(i = 0; i < sfxslots.length; i++){
					if(sfxslots[i] == null) continue;
					String estr = mybank.getSFXSlotEnumString(i);
					hout.write("#define ");
					hout.write(h_prefix); hout.write(estr);
					hout.write(" " + i); hout.write("\n");
					
					SoundfontXML.writeSFEffectElement(xmlout, 2, i, sfxslots[i], estr);
				}
			}
			xmlout.write("\t</SoundEffects>\n");
			hout.write("\n");
		}
		else xmlout.write("\t<SoundEffects/>\n");
		
		//Envelopes
		if(!envs.isEmpty()){
			xmlout.write("\t<Envelopes>\n");
			for(Z64Envelope env : envs){
				SoundfontXML.writeSFEnvelopeElement(xmlout, 2, env);
			}
			xmlout.write("\t</Envelopes>\n");
		}
		else xmlout.write("\t<Envelopes/>\n");
		
		//Close output files
		xmlout.close();
		hout.close();
		
		return true;
	}
	
	public static boolean exportBuild(String dirpath, AbldFile build) throws IOException{
		//TODO
		if(build == null || dirpath == null) return false;
		if(!FileBuffer.directoryExists(dirpath)){
			Files.createDirectories(Paths.get(dirpath));
		}
		
		String dir_at = dirpath + File.separator + "audiotable";
		String dir_ab = dirpath + File.separator + "audiobank";
		String dir_as = dirpath + File.separator + "audioseq";
		String dir_incl = dirpath + File.separator + "include";
		
		//audiotable
		if(!FileBuffer.directoryExists(dir_at)) Files.createDirectory(Paths.get(dir_at));
		List<EngineWaveArcInfo> warclist = build.getWaveArcs();
		if(!RecompFiles.writeBanksXML(dir_at, warclist)) return false;

		int i = 0;
		String[] warcnames = new String[warclist.size()];
		for(EngineWaveArcInfo ainfo : warclist){
			warcnames[i] = String.format("%d - %s", i, ainfo.getName());
			i++;
		}
		
		//audiobank
		if(!FileBuffer.directoryExists(dir_ab)) Files.createDirectory(Paths.get(dir_ab));
		if(!FileBuffer.directoryExists(dir_incl)) Files.createDirectory(Paths.get(dir_incl));
		List<EngineBankInfo> banklist = build.getBanks();
		int bcount = banklist.size();
		String incl_bnk_path = dir_incl + File.separator + "soundfonts.h";
		i = 0;
		for(EngineBankInfo binfo : banklist){
			if(!RecompFiles.outputSoundfont(dir_ab, i, binfo, warcnames)) return false;
			i++;
		}
		
		//Include
		BufferedWriter bw = new BufferedWriter(new FileWriter(incl_bnk_path));
		bw.write("#ifndef SOUNDFONTS_H\n");
		bw.write("#define SOUNDFONTS_H\n\n");
		for(i = 0; i < bcount; i++){
			bw.write("#include \"audiobank/sf");
			bw.write(String.format("%02d.h\"\n", i));
		}
		bw.write("\n");
		i = 0;
		for(EngineBankInfo binfo : banklist){
			BankTableEntry bankmeta = ZeqerCore.getActiveCore().getBankInfo(binfo.getBankUID());
			if(bankmeta == null){
				System.err.println("WARNING: Could not find metadata for bank with UID 0x" + String.format("%08x", binfo.getBankUID()));
				i++; continue;
			}
			bw.write("#define ");
			bw.write(bankmeta.getEnumString());
			bw.write(" " + i + "\n");
			i++;
		}
		bw.write("\n");
		bw.write("#endif\n");
		bw.close();
		
		//audioseq
		//TODO - export to .mus files, then make a version of bgm.h
		//TODO - sometime in future, something better for sfx? Modded mus format? Updated sfx.h?
		if(!FileBuffer.directoryExists(dir_as)) Files.createDirectory(Paths.get(dir_as));
		
		return true;
	}
	
	public static boolean exportBuildBinary(String dirpath, AbldFile build, int flags) throws IOException{
		if(!FileBuffer.directoryExists(dirpath)) Files.createDirectories(Paths.get(dirpath));
		boolean endian = (flags & BINBUILD_FLAG_LITTLE_ENDIAN) == 0;
		boolean wideptr = (flags & BINBUILD_FLAG_64BIT) != 0;
		boolean dbgmode = (flags & BINBUILD_FLAG_DEBUGMODE) != 0;
		TallyMap refctr = new TallyMap();
		
		//audiotable
		List<EngineWaveArcInfo> warclist = build.getWaveArcs();
		if(warclist == null || warclist.isEmpty()) return false;

		BufferedWriter hout = new BufferedWriter(new FileWriter(dirpath + File.separator + "audiotable.h"));
		hout.write("#ifndef AUDIOTABLE_H\n");
		hout.write("#define AUDIOTABLE_H\n\n");
		int i = 0;
		for(EngineWaveArcInfo ainfo : warclist){
			hout.write("#define "); hout.write(ainfo.getEnumString());
			hout.write(" " + i + "\n");
			i++;
		}
		hout.write("\n#endif //AUDIOTABLE_H\n");
		hout.close();
		
		String at_path = dirpath + File.separator + "audiotable";
		BufferedOutputStream dataout = new BufferedOutputStream(new FileOutputStream(at_path));
		EngineTables.AudiotableReturn atret = EngineTables.buildAudiotable(warclist, dataout, endian);
		dataout.close();
		if(atret == null){
			System.err.println("ERROR: Audiotable build failed. See stderr for details.");
			return false;
		}
		atret.code_table.writeFile(dirpath + File.separator + TBLNAME_WARC);
		
		if(dbgmode){
			String dbgdir = dirpath + File.separator + "d_audiotable";
			if(!FileBuffer.fileExists(dbgdir)) Files.createDirectories(Paths.get(dbgdir));
			WaveArcInfoEntry[] ctbl = atret.ctbl;
			for(int j = 0; j < ctbl.length; j++){
				if(ctbl[j].getSize() <= 0) continue;
				String subpath = dbgdir + File.separator + String.format("audiotable_%02d", j);
				long st = ctbl[j].getOffset();
				long ed = st + ctbl[j].getSize();
				FileBuffer buff = FileBuffer.createBuffer(at_path, st, ed, true);
				buff.writeFile(subpath);
			}
		}
		
		//audiobank
		List<EngineBankInfo> banklist = build.getBanks();
		hout = new BufferedWriter(new FileWriter(dirpath + File.separator + "audiobank.h"));
		hout.write("#ifndef AUDIOBANK_H\n");
		hout.write("#define AUDIOBANK_H\n\n");
		i = 0;
		for(EngineBankInfo binfo : banklist){
			int buid = binfo.getBankUID();
			String rsuffix = "";
			int rcount = refctr.getCount(buid);
			if(rcount > 0){
				rsuffix = String.format("_R%02d", rcount);
			}
			BankTableEntry bentry = ZeqerCore.getActiveCore().getBankInfo(buid);
			if(bentry == null){
				hout.close(); dataout.close();
				System.err.println("ERROR: Could not find bank table entry with ID 0x" + String.format("%08x", buid));
				return false;
			}
			
			hout.write("#define ");
			hout.write(bentry.getEnumString() + rsuffix);
			hout.write(" " + i + "\n");
			refctr.increment(buid);
			i++;
		}
		hout.write("\n#endif //AUDIOBANK_H\n");
		hout.close();
		
		String ab_path = dirpath + File.separator + "audiobank";
		dataout = new BufferedOutputStream(new FileOutputStream(ab_path));
		EngineTables.AudiobankReturn abret = EngineTables.buildAudiobank(banklist, atret.id_offset_map, dataout, endian, wideptr);
		dataout.close();
		if(abret == null){
			System.err.println("ERROR: Audiobank build failed. See stderr for details.");
			return false;
		}
		abret.code_table.writeFile(dirpath + File.separator + TBLNAME_BANK);
		
		if(dbgmode){
			String dbgdir = dirpath + File.separator + "d_audiobank";
			if(!FileBuffer.fileExists(dbgdir)) Files.createDirectories(Paths.get(dbgdir));
			BankInfoEntry[] ctbl = abret.ctbl;
			for(int j = 0; j < ctbl.length; j++){
				if(ctbl[j].getSize() <= 0) continue;
				String subpath = dbgdir + File.separator + String.format("audiobank_%03d", j);
				long st = ctbl[j].getOffset();
				long ed = st + ctbl[j].getSize();
				FileBuffer buff = FileBuffer.createBuffer(ab_path, st, ed, true);
				buff.writeFile(subpath);
			}
		}
		
		//audioseq
		List<EngineSeqInfo> seqlist = build.getSeqs();
		refctr = new TallyMap();

		hout = new BufferedWriter(new FileWriter(dirpath + File.separator + "audioseq.h"));
		hout.write("#ifndef AUDIOSEQ_H\n");
		hout.write("#define AUDIOSEQ_H\n\n");
		i = 0;
		for(EngineSeqInfo sinfo : seqlist){
			int suid = sinfo.getSeqUID();
			String rsuffix = "";
			int rcount = refctr.getCount(suid);
			if(rcount > 0){
				rsuffix = String.format("_R%02d", rcount);
			}
			SeqTableEntry sentry = ZeqerCore.getActiveCore().getSeqInfo(suid);
			if(sentry == null){
				hout.close(); dataout.close();
				System.err.println("ERROR: Could not find seq table entry with ID 0x" + String.format("%08x", suid));
				return false;
			}
			hout.write("#define ");
			hout.write(sentry.getEnumString() + rsuffix);
			hout.write(" " + i + "\n");
			refctr.increment(suid);
			i++;
		}
		hout.write("\n#endif //AUDIOSEQ_H\n");
		hout.close();
		
		String as_path = dirpath + File.separator + "audioseq";
		dataout = new BufferedOutputStream(new FileOutputStream(as_path));
		EngineTables.AudioseqReturn asret = EngineTables.buildAudioseq(seqlist, abret.id_idx_map, dataout, endian);
		dataout.close();
		if(asret == null){
			System.err.println("ERROR: Audioseq build failed. See stderr for details.");
			return false;
		}
		asret.code_table.writeFile(dirpath + File.separator + TBLNAME_SEQ);
		asret.code_sbmap.writeFile(dirpath + File.separator + TBLNAME_SBMAP);
		
		if(dbgmode){
			String dbgdir = dirpath + File.separator + "d_audioseq";
			if(!FileBuffer.fileExists(dbgdir)) Files.createDirectories(Paths.get(dbgdir));
			SeqInfoEntry[] ctbl = asret.ctbl;
			for(int j = 0; j < ctbl.length; j++){
				if(ctbl[j].getSize() <= 0) continue;
				String subpath = dbgdir + File.separator + String.format("audioseq_%03d", j);
				long st = ctbl[j].getOffset();
				long ed = st + ctbl[j].getSize();
				FileBuffer buff = FileBuffer.createBuffer(as_path, st, ed, true);
				buff.writeFile(subpath);
			}
		}
		
		return true;
	}
	
}
