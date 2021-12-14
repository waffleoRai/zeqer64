package waffleoRai_zeqer64.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileUtils;
import waffleoRai_zeqer64.ZeqerCore;
import waffleoRai_zeqer64.ZeqerRom;
import waffleoRai_zeqer64.extract.WaveExtractor;
import waffleoRai_zeqer64.filefmt.NusRomInfo;
import waffleoRai_zeqer64.filefmt.RomInfoNode;

public class ExtractSound {
	
	private static String[] version_pri;
	
	private static class WaveTableEntry implements Comparable<WaveTableEntry>{
		
		public String md5str;
		public int len;
		public Map<String, int[]> versions; //WArc idx, index, offset, used
		
		public WaveTableEntry(String hash){
			md5str = hash;
			len = 0;
			versions = new HashMap<String, int[]>();
		}
		
		public int hashCode(){
			return md5str.hashCode();
		}
		
		public boolean equals(Object o){
			return this == o;
		}
		
		public int compareTo(WaveTableEntry o) {
			if(o == null) return 1;
			
			//1. Versions
			if(version_pri != null){
				for(String v : version_pri){
					int[] myvals = this.versions.get(v);
					int[] ovals = o.versions.get(v);
					
					if(myvals == null && ovals == null) continue;
					if(myvals == null) return 1;
					if(ovals == null) return -1;
					
					int count = myvals.length;
					for(int i = 0; i < count; i++){
						if(myvals[i] == ovals[i]) continue;
						return myvals[i] - ovals[i];
					}
				}
			}
			
			//2. MD5
			return this.md5str.compareTo(o.md5str);
		}
	}
	
	private static class SeqTableEntry implements Comparable<SeqTableEntry>{
		public String md5str;
		public int len;
		public Map<String, int[]> versions; //index, offset
		
		public SeqTableEntry(String hash){
			md5str = hash;
			len = 0;
			versions = new HashMap<String, int[]>();
		}

		public int hashCode(){
			return md5str.hashCode();
		}
		
		public boolean equals(Object o){
			return this == o;
		}
		
		public int compareTo(SeqTableEntry o) {
			if(o == null) return 1;
			
			//1. Versions
			if(version_pri != null){
				for(String v : version_pri){
					int[] myvals = this.versions.get(v);
					int[] ovals = o.versions.get(v);
					
					if(myvals == null && ovals == null) continue;
					if(myvals == null) return 1;
					if(ovals == null) return -1;
					
					int count = myvals.length;
					for(int i = 0; i < count; i++){
						if(myvals[i] == ovals[i]) continue;
						return myvals[i] - ovals[i];
					}
				}
			}
			
			//2. MD5
			return this.md5str.compareTo(o.md5str);
		}
	}
	
	public static void printUsage(){
		System.out.println("Usage:");
		System.out.println("\tjava ExtractSound [outdir] [inpath0] [inpath1]...");
	}
	
	public static Map<String, WaveTableEntry> tryLoadWaveTable(String path) throws IOException{
		//Return empty map if file does not exist
		//csv
		//HASH,SIZE,versions...
		//Per version: WARC:IDX:OFFSET:USED
		
		//Create map
		Map<String, WaveTableEntry> map = new HashMap<String, WaveTableEntry>();
		if(FileBuffer.fileExists(path)){
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line = br.readLine();
			//Header
			String[] fields = line.split(",");
			if(fields.length < 2){
				br.close();
				return map;
			}
			int vcount = fields.length - 2;
			String[] versions = new String[vcount];
			for(int i = 0; i < vcount; i++) versions[i] = fields[i+2];
			if(version_pri != null){
				//See which ones are there...
				List<String> newlist = new LinkedList<String>();
				for(int i = 0; i < version_pri.length; i++) newlist.add(version_pri[i]);
				for(int i = 0; i < vcount; i++){
					if(!newlist.contains(versions[i])){
						newlist.add(versions[i]);
					}
				}
				int c = newlist.size();
				version_pri = new String[c];
				int i = 0;
				for(String v : newlist){
					version_pri[i++] = v;
				}
			}
			else{
				version_pri = Arrays.copyOf(versions, vcount);
			}
			
			while((line = br.readLine()) != null){
				if(line.isEmpty()) continue;
				if(line.startsWith("#")) continue;
				fields = line.split(",");
				if(fields.length < vcount+2) continue;
				WaveTableEntry entry = new WaveTableEntry(fields[0]);
				entry.len = Integer.parseInt(fields[1].substring(2), 16);
				for(int i = 0; i < vcount; i++){
					String[] subfields = fields[i+2].split(":");
					if(subfields.length < 4) continue;
					
					int warc = Integer.parseInt(subfields[0]);
					int idx = Integer.parseInt(subfields[1]);
					int offset = Integer.parseInt(subfields[2].substring(2), 16);
					int used = Integer.parseInt(subfields[3]);
					entry.versions.put(versions[i], new int[]{warc,idx,offset,used});
				}
				
				map.put(entry.md5str, entry);
			}
			br.close();
		}
		
		return map;
	}
	
	public static Map<String, SeqTableEntry> tryLoadSeqTable(String path) throws NumberFormatException, IOException{
		//Return empty list if file does not exist
		//HASH,SIZE,versions...
		//Per version: IDX:OFFSET
		Map<String, SeqTableEntry> map = new HashMap<String, SeqTableEntry>();
		if(FileBuffer.fileExists(path)){
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line = br.readLine();
			//Header
			String[] fields = line.split(",");
			if(fields.length < 2){
				br.close();
				return map;
			}
			int vcount = fields.length - 2;
			String[] versions = new String[vcount];
			for(int i = 0; i < vcount; i++) versions[i] = fields[i+2];
			
			while((line = br.readLine()) != null){
				if(line.isEmpty()) continue;
				if(line.startsWith("#")) continue;
				fields = line.split(",");
				if(fields.length < vcount+2) continue;
				SeqTableEntry entry = new SeqTableEntry(fields[0]);
				entry.len = Integer.parseInt(fields[1].substring(2), 16);
				for(int i = 0; i < vcount; i++){
					String[] subfields = fields[i+2].split(":");
					if(subfields.length < 2) continue;
					
					int idx = Integer.parseInt(subfields[0]);
					int offset = Integer.parseInt(subfields[1].substring(2), 16);
					entry.versions.put(versions[i], new int[]{idx,offset});
				}
				
				map.put(entry.md5str, entry);
			}
			br.close();
		}
		
		return map;
	}
	
	public static void writeWaveTable(Map<String, WaveTableEntry> tbl, String path) throws IOException{
		if(tbl == null) return;
		
		//Need to sort entries...
		List<WaveTableEntry> list = new ArrayList<WaveTableEntry>(tbl.size()+1);
		list.addAll(tbl.values());
		Collections.sort(list);

		//Open & write header
		BufferedWriter bw = new BufferedWriter(new FileWriter(path));
		bw.write("#MD5,SIZE");
		if(version_pri != null){
			for(String v : version_pri){
				bw.write("," + v);
			}
		}
		bw.write("\n");
		
		//Write entries
		for(WaveTableEntry e : list){
			bw.write(e.md5str); bw.write(",");
			bw.write("0x"); bw.write(Integer.toHexString(e.len));
			
			if(version_pri != null){
				for(String v : version_pri){
					int[] vdat = e.versions.get(v);
					if(vdat == null){
						bw.write(",N/A");
					}
					else{
						bw.write(","); bw.write(Integer.toString(vdat[0]));
						bw.write(":"); bw.write(Integer.toString(vdat[1]));
						bw.write(":0x"); bw.write(Integer.toHexString(vdat[2]));
						bw.write(":"); bw.write(Integer.toString(vdat[3]));
					}
				}
			}
			
			bw.write("\n");
		}
		
		//Close
		bw.close();
	}
	
	public static void writeSeqTable(Map<String, SeqTableEntry> tbl, String path) throws IOException{
		if(tbl == null) return;
		
		//Need to sort entries...
		List<SeqTableEntry> list = new ArrayList<SeqTableEntry>(tbl.size()+1);
		list.addAll(tbl.values());
		Collections.sort(list);

		//Open & write header
		BufferedWriter bw = new BufferedWriter(new FileWriter(path));
		bw.write("#MD5,SIZE");
		if(version_pri != null){
			for(String v : version_pri){
				bw.write("," + v);
			}
		}
		bw.write("\n");
		
		//Write entries
		for(SeqTableEntry e : list){
			bw.write(e.md5str); bw.write(",");
			bw.write("0x"); bw.write(Integer.toHexString(e.len));
			
			if(version_pri != null){
				for(String v : version_pri){
					int[] vdat = e.versions.get(v);
					if(vdat == null){
						bw.write(",N/A");
					}
					else{
						bw.write(","); bw.write(Integer.toString(vdat[0]));
						bw.write(":0x"); bw.write(Integer.toHexString(vdat[1]));
					}
				}
			}
			
			bw.write("\n");
		}
		
		//Close
		bw.close();
	}

	public static void extractToDir(String[] args) throws IOException{
		//Args should just be an output dir followed by a list of ROM paths
		
		//Read args
		if(args.length < 2){
			System.err.println("Insufficient args!");
			printUsage();
			System.exit(1);
		}
		String outdir = args[0];
		int romcount = args.length - 1;
		String[] inpaths = new String[romcount];
		ZeqerRom[] roms = new ZeqerRom[romcount];
		version_pri = new String[romcount];
		for(int i = 0; i < romcount; i++)inpaths[i] = args[i+1]; //This isn't strictly necessary, mostly for tidying
		
		//Load ROMs
		for(int i = 0; i < romcount; i++){
			RomInfoNode rin = ZeqerCore.detectROM(inpaths[i]);
			if(rin == null){
				System.err.println("ROM was not recognized: " + inpaths[i]);
				printUsage();
				System.exit(1);
			}
			if(!(rin instanceof NusRomInfo)){
				//I'll probably fix this eventually, but I'm lazy
				System.err.println("ROM recognized, but not N64 ROM: " + inpaths[i]);
				System.err.println("Please use direct N64 ROMs for this tool.");
				printUsage();
				System.exit(1);
			}
			System.err.println("N64 ROM Detected: " + rin.getROMName());
			NusRomInfo rinfo = (NusRomInfo)rin;
			roms[i] = new ZeqerRom(inpaths[i], rinfo);
			version_pri[i] = rinfo.getZeqerID();
		}
		
		//Make common directories
		//String wdir_path = outdir + File.separator + "wave";
		//String sdir_path = outdir + File.separator + "seq";
		//String bdir_path = outdir + File.separator + "bank";
		
		//if(!FileBuffer.directoryExists(wdir_path)) Files.createDirectories(Paths.get(wdir_path));
		//if(!FileBuffer.directoryExists(sdir_path)) Files.createDirectories(Paths.get(sdir_path));
		//if(!FileBuffer.directoryExists(bdir_path)) Files.createDirectories(Paths.get(bdir_path));
		
		//Load wave & seq tables if already in outdir
		String wtbl_path = outdir + File.separator + "comp_waves.csv";
		String stbl_path = outdir + File.separator + "comp_seqs.csv";
		
		Map<String, WaveTableEntry> wtbl = tryLoadWaveTable(wtbl_path);
		Map<String, SeqTableEntry> stbl = tryLoadSeqTable(stbl_path);
		
		for(ZeqerRom rom : roms){
			System.err.println("Starting ROM: " + rom.getRomInfo().getZeqerID());
			
			//Make rom dump directory...
			String rdir_path = outdir + File.separator + rom.getRomInfo().getZeqerID();
			if(!FileBuffer.directoryExists(rdir_path)) Files.createDirectories(Paths.get(rdir_path));
			
			//Get tables from code.
			FileBuffer data = rom.loadCode();
			int[] bank_warcs;
			
			//Banks
			long boff = rom.getRomInfo().getCodeOffset_bnktable();
			long soff = rom.getRomInfo().getCodeOffset_seqtable();
			data.setCurrentPosition(boff);
			int bcount = Short.toUnsignedInt(data.nextShort());
			System.err.println("\tBank count: " + bcount);
			
			long bsize = (bcount+1) * 16;
			long mapoff = boff + bsize;
			data.writeFile(rdir_path + File.separator + "codetbl_audiobank.bin", boff, mapoff);
			data.writeFile(rdir_path + File.separator + "codetbl_sbmap.bin", mapoff, soff);
			
			String subdir = rdir_path + File.separator + "audiobank";
			if(!FileBuffer.directoryExists(subdir)) Files.createDirectories(Paths.get(subdir));
			FileBuffer adata = rom.loadAudiobank();
			bank_warcs = new int[bcount];
			
			data.skipBytes(14);
			for(int i = 0; i < bcount; i++){
				int st = data.nextInt();
				int len = data.nextInt();
				adata.writeFile(subdir + File.separator + "audiobank_" + String.format("%03d", i) + ".bin", st, st+len);
				data.skipBytes(2);
				bank_warcs[i] = Byte.toUnsignedInt(data.nextByte());
				data.skipBytes(5);
			}
			
			//Seqs
			data.setCurrentPosition(soff);
			int scount = Short.toUnsignedInt(data.nextShort());
			System.err.println("\tSeq count: " + scount);
			long ssize = (scount+1) * 16;
			data.writeFile(rdir_path + File.separator + "codetbl_audioseq.bin", soff, soff+ssize);
			
			subdir = rdir_path + File.separator + "audioseq";
			if(!FileBuffer.directoryExists(subdir)) Files.createDirectories(Paths.get(subdir));
			adata = rom.loadAudioseq();
			
			data.skipBytes(14);
			for(int i = 0; i < scount; i++){
				int st = data.nextInt();
				int len = data.nextInt();
				if(len < 1) continue;
				FileBuffer seqdat = adata.createReadOnlyCopy(st, st+len);
				seqdat.writeFile(subdir + File.separator + "audioseq_" + String.format("%04d", i) + ".bin");
				data.skipBytes(8);
				
				byte[] md5 = FileUtils.getMD5Sum(seqdat.getBytes());
				String md5str = FileUtils.bytes2str(md5).toLowerCase();
				SeqTableEntry e = stbl.get(md5str);
				if(e == null){
					e = new SeqTableEntry(md5str);
					e.len = len;
					stbl.put(md5str, e);
				}
				e.versions.put(rom.getRomInfo().getZeqerID(), new int[]{i, st});
			}
			
			//Waves (and warcs)
			//WArc table
			long wtoff = rom.getRomInfo().getCodeOffset_audtable();
			data.setCurrentPosition(wtoff);
			int warccount = Short.toUnsignedInt(data.nextShort());
			System.err.println("\tWave Archive count: " + warccount);
			
			int[][] warctbl = new int[warccount][2];
			data.skipBytes(14);
			for(int i = 0; i < warccount; i++){
				warctbl[i][0] = data.nextInt();
				warctbl[i][1] = data.nextInt();
				data.skipBytes(8);
			}
			long wtsize = (warccount+1) * 16;
			data.writeFile(rdir_path + File.separator + "codetbl_audiotable.bin", wtoff, wtoff+wtsize);
			
			//Build wave table
			System.err.println("\tBuilding wave table...");
			int[][][] wav_tbl = WaveExtractor.buildWavePosTable(rom);
			
			//Extract waves (raw), write to comp table
			subdir = rdir_path + File.separator + "audiotable";
			if(!FileBuffer.directoryExists(subdir)) Files.createDirectories(Paths.get(subdir));
			adata = rom.loadAudiotable();
			for(int i = 0; i < warccount; i++){
				if(i != 1 && warctbl[i][1] < 1) continue; //Empty warc
				String warcdir = subdir + File.separator + String.format("%02d", i);
				if(!FileBuffer.directoryExists(warcdir)) Files.createDirectories(Paths.get(warcdir));
				
				int[][] mytbl = wav_tbl[i];
				if(mytbl == null) continue;
				int count = mytbl.length;
				System.err.println("\t\tReceived -- WARC " + i + " Wave Count: " + count);
				int baseoff = warctbl[i][0];
				if(i == 1) baseoff = warctbl[0][0];
				for(int j = 0; j < count; j++){
					//System.err.println("");
					int wavoff = baseoff + mytbl[j][0];
					if(mytbl[j][1] < 1) continue;
					FileBuffer wavbuff = adata.createReadOnlyCopy(wavoff, wavoff + mytbl[j][1]);
					wavbuff.writeFile(warcdir + File.separator + "audiotable_" + String.format("%02d_%04d.vadpcm", i,j));
					byte[] md5 = FileUtils.getMD5Sum(wavbuff.getBytes());
					String md5str = FileUtils.bytes2str(md5).toLowerCase();
					
					WaveTableEntry e = wtbl.get(md5str);
					if(e == null){
						e = new WaveTableEntry(md5str);
						e.len = mytbl[j][1];
						wtbl.put(md5str, e);
					}
					//See if version is already present and print warning if so.
					e.versions.put(rom.getRomInfo().getZeqerID(), new int[]{i, j, mytbl[j][0], mytbl[j][2]});
					
					wavbuff.dispose();
				}
			}
		}
		
		//Output comp tables
		writeWaveTable(wtbl, wtbl_path);
		writeSeqTable(stbl, stbl_path);
	}
	
	public static void main(String[] args) {

		try{
			extractToDir(args);
		}
		catch(Exception ex){
			ex.printStackTrace();
			System.exit(1);
		}
		
	}

}
