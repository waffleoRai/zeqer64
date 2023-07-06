package waffleoRai_zeqer64.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import waffleoRai_SeqSound.n64al.NUSALSeq;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.MultiFileBuffer;
import waffleoRai_soundbank.nintendo.z64.Z64Bank;
import waffleoRai_zeqer64.ZeqerCore;
import waffleoRai_zeqer64.ZeqerRom;
import waffleoRai_zeqer64.filefmt.NusRomInfo;
import waffleoRai_zeqer64.filefmt.RomInfoNode;
import waffleoRai_zeqer64.filefmt.SoundfontXML;
import waffleoRai_zeqer64.filefmt.VersionWaveTable;

public class Test_BruteforceInsertSeq {
	//This just adds seq, font, and any samples to end of ROM

	public static void printUsage(){
		System.err.println("Argument Options:");
		System.err.println("\t-mml\t\tMML Sequence Script");
		System.err.println("\t-binseq\t\tBinary Sequence File");
		System.err.println("\t-fontxml\tSoundfont XML (Zeqer Format)");
		System.err.println("\t-fontidx\tIndex of soundfont in ROM to use. If also using -fontxml, this is the font index to replace.");
		System.err.println("\t-replaceseq\tIndex of sequence in target ROM to replace");
		System.err.println("\t-targetrom\tFile path of ROM to add seq to");
		System.err.println("\t-output\t\tOutput ROM path");
		System.err.println("\t-dbgout\t\tOutput directory for intermediate bins for debug");
		System.err.println("\t-zeqdir\t\tRoot of Zeqer data directory");
	}
	
	public static void exit(String message){
		System.err.println(message);
		printUsage();
		System.exit(1);
	}
	
	public static NUSALSeq readSeq(Map<String, String> argmap) throws IOException, UnsupportedFileTypeException{
		final String KEY_BIN = "binseq";
		final String KEY_MML = "mml";
		
		//Bin file takes priority
		if(argmap.containsKey(KEY_BIN)){
			String val = argmap.get(KEY_BIN);
			System.err.println("readSeq - Using provided binary file: " + val);
			NUSALSeq seq;
			try {
				seq = NUSALSeq.readNUSALSeq(FileBuffer.createBuffer(val, true));
				return seq;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		else if(argmap.containsKey(KEY_MML)){
			String val = argmap.get(KEY_MML);
			System.err.println("readSeq - Using provided MML script: " + val);
			BufferedReader br = new BufferedReader(new FileReader(val));
			NUSALSeq seq = NUSALSeq.readMMLScript(br);
			br.close();
			return seq;
		}
		
		System.err.println("No seq file provided!");
		return null;
	}
	
	public static ZeqerRom loadRom(String path) throws IOException{
		RomInfoNode rin = ZeqerCore.getActiveCore().detectROM(path);
		if(rin == null){
			System.err.println("ROM was not recognized: " + path);
			System.exit(1);
		}
		if(!(rin instanceof NusRomInfo)){
			//I'll probably fix this eventually, but I'm lazy
			System.err.println("ROM recognized, but not N64 ROM: " + path);
			System.err.println("Please use direct N64 ROMs for this tool.");
			System.exit(1);
		}
		System.err.println("N64 ROM Detected: " + rin.getROMName());
		NusRomInfo rinfo = (NusRomInfo)rin;
		
		ZeqerRom rom = new ZeqerRom(path, rinfo);
		return rom;
	}
	
	public static Map<String, String> parseArgs(String[] args){
		Map<String, String> map = new HashMap<String, String>();
		if(args == null) return map;
		
		String lastkey = null;
		for(int i = 0; i < args.length; i++){
			if(args[i].startsWith("-")){
				lastkey = args[i].substring(1);
				continue;
			}
			if(lastkey == null){
				System.err.println("Argument value found without key: " + args[i]);
				return map;
			}
			map.put(lastkey, args[i]);
		}
		
		return map;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Map<String, String> argmap = parseArgs(args);
		if(argmap == null || argmap.isEmpty()){
			exit("Insufficient args!");
		}
		
		//Get other important args.
		String rompath = argmap.get("targetrom");
		String outpath = argmap.get("output");
		String zdir = argmap.get("zeqdir");
		String replidxstr = argmap.get("replaceseq");
		String debugdir = argmap.get("dbgout");
		if(rompath == null || outpath == null || zdir == null){
			exit("Missing required arguments!");
		}
		if(debugdir != null) System.err.println("dbgout = " + debugdir);
		
		int replidx = -1;
		try{replidx = Integer.parseInt(replidxstr);}
		catch(NumberFormatException ex){
			System.err.println("Seq replacement index must be valid integer.");
			ex.printStackTrace(); 
			System.exit(1);
		}
		
		int freplidx = -1;
		replidxstr = argmap.get("fontidx");
		if(replidxstr != null){
			try{freplidx = Integer.parseInt(replidxstr);}
			catch(NumberFormatException ex){
				System.err.println("Font replacement index must be valid integer.");
				ex.printStackTrace(); 
				System.exit(1);
			}	
		}
		
		try{
			ZeqerCore.getActiveCore().setProgramDirectory(zdir);
			ZeqerCore.getActiveCore().loadSoundTables();
			
			//1. Read in ROM
			ZeqerRom rom = loadRom(rompath);
			int rom_end = rom.getVirtualRomSize();
			int ins_pos = rom_end;
			int[][] vaddr_tbl = rom.getVirtualAddressTable();
			System.err.println("ROM FS End: 0x" + Integer.toHexString(rom_end));
			
			//Check validity of font/seq replacement indices
			int seq_count = rom.loadSeqEntries().length;
			int bnk_count = rom.loadBankEntries().length;
			if(replidx < 0 || replidx >= seq_count){
				exit("Seq index is not valid for this ROM!");
			}
			if(freplidx >= bnk_count){ //-1 to signal not provided, use bank already assigned to seq.
				exit("Font index is not valid for this ROM!");
			}
			
			//2. Read in font, if provided.
			Z64Bank custom_font = null;
			String fontxml_path = argmap.get("fontxml");
			if(fontxml_path != null){
				System.err.println("Reading custom font XML...");
				custom_font = SoundfontXML.readSFXML(fontxml_path);
			}
			//System.exit(2); //DEBUG
			
			//3. Read in seq
			NUSALSeq seq = readSeq(argmap);
			if(seq == null){
				exit("Sequence was not found or could not be read!");
			}
			
			//4. Determine which samples are in the ROM and which need to be added.
			//	Then update offsets in custom font
			List<Z64WaveInfo> add_samples = new LinkedList<Z64WaveInfo>();
			int font_bin_alloc = 0;
			if(custom_font != null){
				VersionWaveTable vwav_tbl = ZeqerCore.getActiveCore().loadWaveVersionTable(rom.getRomInfo().getZeqerID());
				Map<Integer, Integer> wid_map_b1 = new HashMap<Integer, Integer>(); //Map offsets in bank 1 to UIDs
				/*int b1_scount = vwav_tbl[0].length;
				for(int i = 0; i < b1_scount; i++){
					wid_map_b1.put(vwav_tbl[0][i][0], vwav_tbl[0][i][1]);
				}*/
				int audiotable_off = rom.getFileVirtualOffset(rom.getRomInfo().getDMADataIndex_audiotable());
				List<Z64WaveInfo> fnt_samples = custom_font.getAllWaveBlocks();
				font_bin_alloc = fnt_samples.size() * (16+48+144);
				
				for(Z64WaveInfo winfo : fnt_samples){
					int wuid = winfo.getUID();
					if(wid_map_b1.containsKey(wuid)){
						winfo.setWaveOffset(wid_map_b1.get(wuid));
					}
					else{
						int wsz = winfo.getWaveSize();
						wsz = (wsz + 0xF) & ~0xF;
						add_samples.add(winfo);
						winfo.setWaveOffset(ins_pos - audiotable_off);
						ins_pos += wsz;
					}
				}
				//custom_font.setSamplesOrderedByUID(false);
				font_bin_alloc += 16;
				font_bin_alloc += custom_font.getEffectiveInstCount() * (4+48); //Inst offset, env, inst block
				font_bin_alloc += custom_font.getEffectivePercCount() * (4+32);
				font_bin_alloc += custom_font.getEffectiveSFXCount() * (8);
			}
			
			//5. Serialize the font and seq, determine offsets and sizes
			FileBuffer font_bin = null, seq_bin = null;
			int off_seq = 0, sz_seq = 0, off_fnt = 0, sz_fnt = 0;
			int audioseq_off = vaddr_tbl[rom.getRomInfo().getDMADataIndex_audioseq()][0];
			int audiobnk_off = vaddr_tbl[rom.getRomInfo().getDMADataIndex_audiobank()][0];
			seq_bin = seq.getSerializedData();
			sz_seq = (int)seq_bin.getFileSize();
			off_seq = ins_pos - audioseq_off;
			ins_pos += sz_seq;
			if(debugdir != null){
				seq_bin.writeFile(debugdir + File.separator + "zeqer_seqins_seq.bin");
			}
			
			if(custom_font != null){
				font_bin = new FileBuffer(font_bin_alloc, true);
				//custom_font.serializeTo(font_bin);
				custom_font.serializeTo(font_bin, Z64Bank.SEROP_DEFAULT);
				off_fnt = ins_pos - audiobnk_off;
				sz_fnt = ((int)font_bin.getFileSize() + 0xf) & ~0xf;
				ins_pos += sz_fnt;
				if(debugdir != null){
					font_bin.writeFile(debugdir + File.separator + "zeqer_seqins_bnk.bin");
					BufferedWriter dbgw = new BufferedWriter(new FileWriter(debugdir + File.separator + "zeqer_seqins_bnk.txt"));
					custom_font.printMeTo(dbgw);
					dbgw.close();
				}
			}

			//6. Load source ROM code file (and dmadata) into FileBuffer for modification
			int dmadata_idx = rom.getDmadataIndex();
			int code_idx = rom.getRomInfo().getDMADataIndex_code();
			FileBuffer dmadata_dat = rom.loadFile(dmadata_idx);
			FileBuffer code_dat = rom.loadCode();
			
			//7. Update code tables: seq table -- and if custom font, font table and seq-font map
			long tbloff = rom.getRomInfo().getCodeOffset_seqtable();
			tbloff += 0x10 + (replidx << 4); //Set offset to replacement slot
			code_dat.replaceInt(off_seq, tbloff);
			code_dat.replaceInt(sz_seq, tbloff+4);
			System.err.println("Updated seq record @ code 0x" + Long.toHexString(tbloff));
			
			if(freplidx >= 0){
				tbloff = rom.getRomInfo().getCodeOffset_bnktable();
				tbloff += (1+bnk_count) << 4;
				long soff = (long)code_dat.shortFromFile(tbloff + (replidx << 1));
				tbloff += soff;
				code_dat.replaceByte((byte)freplidx, tbloff+1);
				
				if(custom_font != null){
					tbloff = rom.getRomInfo().getCodeOffset_bnktable();
					tbloff += 0x10 + (freplidx << 4);
					code_dat.replaceInt(off_fnt, tbloff);
					code_dat.replaceInt(sz_fnt, tbloff+4);
					code_dat.replaceByte((byte)custom_font.getMedium(), tbloff+8);
					code_dat.replaceByte((byte)custom_font.getCachePolicy(), tbloff+9);
					code_dat.replaceByte((byte)1, tbloff+10);
					code_dat.replaceByte((byte)-1, tbloff+11);
					code_dat.replaceByte((byte)custom_font.getEffectiveInstCount(), tbloff+12);
					code_dat.replaceByte((byte)custom_font.getEffectivePercCount(), tbloff+13);
					code_dat.replaceShort((short)custom_font.getEffectiveSFXCount(), tbloff+14);
					System.err.println("Updated font record @ code 0x" + Long.toHexString(tbloff));
				}
			}
			
			//8. Update dmadata to decompress everything
			tbloff = 0;
			for(int i = 0; i < vaddr_tbl.length; i++){
				//System.err.println("DEBUG: Updating record for file " + i + " | tbloff = 0x" + Long.toHexString(tbloff));
				dmadata_dat.replaceInt(vaddr_tbl[i][0], tbloff+8);
				dmadata_dat.replaceInt(0, tbloff+12);
				tbloff+=16;
			}
			if(debugdir != null){
				dmadata_dat.writeFile(debugdir + File.separator + "zeqer_seqins_dmadata.bin");
				code_dat.writeFile(debugdir + File.separator + "zeqer_seqins_code.bin");
			}
			
			//9. Copy files, replacing dmadata and code with new versions
			MultiFileBuffer outbuff = new MultiFileBuffer((vaddr_tbl.length + 2 + add_samples.size()) << 1);
			int[] file_order = rom.getFileImageOrder();
			long cpos = 0L; //For padding.
			for(int i = 0; i < file_order.length; i++){
				//System.err.println("cpos = 0x" + Long.toHexString(cpos) + ", vaddr_tbl[" + i + "] = 0x" + Integer.toHexString(vaddr_tbl[i][0]));
				int f_idx = file_order[i];
				if(cpos < vaddr_tbl[f_idx][0]){
					int pad = (int)(vaddr_tbl[f_idx][0] - cpos);
					FileBuffer padding = new FileBuffer(pad, true);
					for(int j = 0; j < pad; j++)padding.addToFile(FileBuffer.ZERO_BYTE);
					outbuff.addToFile(padding);
					cpos += pad;
				}
				
				FileBuffer file = null;
				if(f_idx == dmadata_idx){
					file = dmadata_dat;
				}
				else if(f_idx == code_idx){
					file = code_dat;
				}
				else{
					file = rom.loadFile(f_idx);
				}
				outbuff.addToFile(file);
				cpos += file.getFileSize();
			}
			
			//10. Insert extra samples, seq, then font
			if(cpos < rom_end){
				int pad = (int)(rom_end - cpos);
				FileBuffer padding = new FileBuffer(pad, true);
				for(int j = 0; j < pad; j++)padding.addToFile(FileBuffer.ZERO_BYTE);
				outbuff.addToFile(padding);
				cpos += pad;
			}
			for(Z64WaveInfo winfo : add_samples){
				FileBuffer wavedat = ZeqerCore.getActiveCore().loadWaveData(winfo.getUID());
				outbuff.addToFile(wavedat);
				int wsz = (int)wavedat.getFileSize();
				if(wsz % 16 != 0){
					int pad = 16 - (wsz%16);
					FileBuffer padding = new FileBuffer(pad, true);
					for(int j = 0; j < pad; j++)padding.addToFile(FileBuffer.ZERO_BYTE);
					outbuff.addToFile(padding);
					cpos += pad;
				}
				cpos += wsz;
			}
			outbuff.addToFile(seq_bin);
			cpos += sz_seq;
			if(custom_font != null){
				outbuff.addToFile(font_bin);
				int pad = sz_fnt - (int)font_bin.getFileSize();
				if(pad != 0){
					FileBuffer padding = new FileBuffer(pad, true);
					for(int j = 0; j < pad; j++)padding.addToFile(FileBuffer.ZERO_BYTE);
					outbuff.addToFile(padding);
				}
				cpos += sz_fnt;
			}
			
			//11. Pad (0xff) the new ROM to an even number of MB, then write out.
			//	(Might have 4kb (0x1000) blocks, since it's 0x00 up to multiple, then 0xff after?)
			long c_align = (cpos + 0xFFF) & ~0xFFFL;
			if(c_align > cpos){
				int pad = (int)(c_align - cpos);
				FileBuffer padding = new FileBuffer(pad, true);
				for(int j = 0; j < pad; j++)padding.addToFile(FileBuffer.ZERO_BYTE);
				outbuff.addToFile(padding);
				cpos = c_align;
			}
			c_align = (cpos + 0x1FFFFF) & ~0x1FFFFFL;
			if(c_align > cpos){
				int pad = (int)(c_align - cpos);
				FileBuffer padding = new FileBuffer(pad, true);
				for(int j = 0; j < pad; j++)padding.addToFile((byte)-1);
				outbuff.addToFile(padding);
			}
			
			outbuff.writeFile(outpath);
			
		}
		catch(Exception ex){
			ex.printStackTrace();
			System.exit(1);
		}
		
	}

}
