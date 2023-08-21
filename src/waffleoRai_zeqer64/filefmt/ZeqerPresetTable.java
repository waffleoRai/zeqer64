package waffleoRai_zeqer64.filefmt;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import waffleoRai_Utils.BinFieldSize;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.MultiFileBuffer;
import waffleoRai_Utils.SerializedString;
import waffleoRai_soundbank.nintendo.z64.Z64Drum;
import waffleoRai_soundbank.nintendo.z64.Z64Envelope;
import waffleoRai_soundbank.nintendo.z64.Z64Instrument;
import waffleoRai_zeqer64.ZeqerCore;
import waffleoRai_zeqer64.ZeqerPreset;
import waffleoRai_zeqer64.presets.ZeqerDrumPreset;
import waffleoRai_zeqer64.presets.ZeqerDummyPreset;
import waffleoRai_zeqer64.presets.ZeqerInstPreset;
import waffleoRai_zeqer64.presets.ZeqerPercPreset;
import waffleoRai_zeqer64.presets.ZeqerSFXPreset;

public class ZeqerPresetTable {
	
	public static final String TBL_MAGIC = "zeqrINSt";
	public static final int TBL_CURRENT_VERSION = 6;
	
	private static final int HEADER_SIZE = 16+8;
	
	/*----- Instance Variables -----*/
	
	private Map<Integer, ZeqerPreset> presets;
	private Map<String, ZeqerPreset> name_map;
	
	/*----- Init -----*/
	
	public ZeqerPresetTable(){
		presets = new TreeMap<Integer, ZeqerPreset>();
	}
	
	/*----- Parsing -----*/
	
	public static ZeqerPresetTable readTable(FileBuffer data) throws UnsupportedFileTypeException{
		data.setEndian(true);
		ZeqerPresetTable tbl = new ZeqerPresetTable();
		
		//Header
		long mpos = data.findString(0, 0x10, TBL_MAGIC);
		if(mpos != 0) throw new UnsupportedFileTypeException("ZeqerPresetTable.readTable || Invalid table file: magic number not found!");
		data.setCurrentPosition(8L);
		data.nextShort(); //Reserved flags
		int version = data.nextShort(); //Version
		
		int ecount = data.nextInt();
		int rcount = data.nextInt();
		data.nextInt(); //Record Block Offset
		
		//Read Envelopes
		Z64Envelope[] envs = null;
		if(ecount > 0){
			envs = new Z64Envelope[ecount];
			for(int i = 0; i < ecount; i++){
				int esz = Short.toUnsignedInt(data.nextShort());
				envs[i] = new Z64Envelope();
				int event_ct = esz >>> 2;
				for(int j = 0; j < event_ct; j++){
					short cmd = data.nextShort();
					short val = data.nextShort();
					envs[i].addEvent(cmd, val);
				}
				envs[i].setID(i);
			}
		}
		
		//Read Presets
		for(int i = 0; i < rcount; i++){
			int uid = data.nextInt();
			int flags = data.nextInt();
			ZeqerPreset preset = null;
			int type = flags & 0x3;
			if((flags & 0x80000000) != 0){
				//Has data
				switch(type){
				case ZeqerPreset.PRESET_TYPE_INST:
					preset = new ZeqerInstPreset(uid);
					break;
				case ZeqerPreset.PRESET_TYPE_PERC:
					preset = new ZeqerPercPreset(uid);
					break;
				case ZeqerPreset.PRESET_TYPE_SFX:
					preset = new ZeqerSFXPreset(uid);
					break;
				case ZeqerPreset.PRESET_TYPE_DRUM:
					preset = new ZeqerDrumPreset(uid);
					break;
				default:
					throw new UnsupportedFileTypeException("ZeqerPresetTable.readTable || Unknown record type found: " + type);
				}
				
				long amtread = preset.readIn(data.getReferenceAt(data.getCurrentPosition()), envs, version);
				data.skipBytes(amtread);
			}
			else{
				//Dummy
				ZeqerDummyPreset dpreset = new ZeqerDummyPreset(uid);
				dpreset.setType(type);			
				preset = dpreset;
			}
			//Read name
			mpos = data.getCurrentPosition();
			SerializedString ss = data.readVariableLengthString(ZeqerCore.ENCODING, mpos, BinFieldSize.WORD, 2);
			preset.setName(ss.getString());
			data.skipBytes(ss.getSizeOnDisk());
			
			//Read enum label
			if(version >= 6){
				mpos = data.getCurrentPosition();
				ss = data.readVariableLengthString(ZeqerCore.ENCODING, mpos, BinFieldSize.WORD, 2);
				preset.setEnumLabel(ss.getString());
				data.skipBytes(ss.getSizeOnDisk());
			}
			
			//Read tags
			if(version >= 2){
				mpos = data.getCurrentPosition();
				ss = data.readVariableLengthString(ZeqerCore.ENCODING, mpos, BinFieldSize.WORD, 2);
				String tagstr = ss.getString();
				data.skipBytes(ss.getSizeOnDisk());
				
				if(tagstr != null){
					String[] tags = tagstr.split(";");
					for(String tag : tags) preset.addTag(tag);
				}	
			}
			
			//Map
			tbl.presets.put(uid, preset);
		}
		
		return tbl;
	}
	
	public static ZeqerPresetTable createTable(){
		return new ZeqerPresetTable();
	}
	
	public int importTSV(String tsv_path) throws IOException{
		//ONLY updates names and tags!
		if(tsv_path == null) return 0;
		if(!FileBuffer.fileExists(tsv_path)) return 0;
		
		//This only updates specific fields.
		Map<String, Integer> cidx_map = new HashMap<String, Integer>();
		
		int update_count = 0;
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(tsv_path), StandardCharsets.UTF_8));
		String line = br.readLine();
		if(!line.startsWith("#")){
			System.err.println("ZeqerPresetTable.importTSV || tsv header not formatted correctly!");
			br.close();
			return 0;
		}
		line = line.substring(1);
		String[] fields = line.split("\t");
		for(int i = 0; i < fields.length; i++){
			if(fields[i].startsWith("#")) fields[i] = fields[i].substring(1);
			String k = fields[i].toUpperCase();
			cidx_map.put(k, i);
			System.err.println("ZeqerPresetTable.importTSV || Field Key Found: " + k);
		}
		
		//Make sure mandatory fields are present
		if(!cidx_map.containsKey("UID")){
			System.err.println("ZeqerPresetTable.importTSV || UID field is required!");
			br.close();
			return 0;
		}
		
		String raw = null;
		int n = 0;
		int cidx_uid = cidx_map.get("UID");
		while((line = br.readLine()) != null){
			fields = line.split("\t");
			
			raw = fields[cidx_uid];
			if(raw.startsWith("0x")) raw = raw.substring(2); //Chop 0x prefix
			n = Integer.parseUnsignedInt(raw, 16); //Let the exception be thrown
			ZeqerPreset entry = presets.get(n);
			if(entry == null){
				System.err.println("ZeqerPresetTable.importTSV || Preset entry with UID " + raw + " not found in table. Skipping this record...");
				continue;
			}
			
			String key = "NAME";
			if(cidx_map.containsKey(key)) entry.setName(fields[cidx_map.get(key)]);
			
			key = "TAGS";
			if(cidx_map.containsKey(key)){
				raw = fields[cidx_map.get(key)];
				if(raw != null && !raw.isEmpty()){
					if(!raw.startsWith("<NONE>")){
						String[] taglist = raw.split(";");
						for(String tag:taglist) entry.addTag(tag);	
					}
				}
			}
			
			key = "ENUM";
			if(cidx_map.containsKey(key)){
				entry.setEnumLabel(fields[cidx_map.get(key)]);
			}

			update_count++;
		}
		
		br.close();
		return update_count;
	}
	
	/*----- Serialization -----*/
	
	public List<Z64Envelope> mergeEnvelopes(){
		//Again, it's pretty inefficient but eh.
		
		//Merges within this list...
		List<Z64Envelope> envlist = new LinkedList<Z64Envelope>();
		boolean inlist = false;
		for(ZeqerPreset preset : presets.values()){
			List<Z64Envelope> plist = preset.getEnvelopes();
			if(plist != null){
				for(Z64Envelope env : plist){
					inlist = false;
					for(Z64Envelope other : envlist){
						if(env == other){
							inlist = true;
							break;
						}
						else{
							if(other.envEquals(env)){
								inlist = true;
								break;
							}
						}
					}
					if(!inlist) envlist.add(env);
				}
			}
		}
		
		//Then goes back thru presets to merge list items back into presets
		for(ZeqerPreset preset : presets.values()){
			if(preset instanceof ZeqerInstPreset){
				ZeqerInstPreset ipreset = (ZeqerInstPreset)preset;
				Z64Envelope env = ipreset.getInstrument().getEnvelope();
				if(env == null) continue;
				
				for(Z64Envelope other : envlist){
					if(env == other) break; //Already in list.
					else{
						if(other.envEquals(env)){
							//Swap out.
							ipreset.getInstrument().setEnvelope(other);
							break;
						}
					}
				}
			}
			else if(preset instanceof ZeqerPercPreset){
				ZeqerPercPreset ppreset = (ZeqerPercPreset)preset;
				for(int i = 0; i < 64; i++){
					Z64Drum drum = ppreset.getDrumInSlot(i);
					if(drum == null) continue;
					Z64Envelope env = drum.getEnvelope();
					if(env == null) continue;
					for(Z64Envelope other : envlist){
						if(env == other) break; //Already in list.
						else{
							if(other.envEquals(env)){
								//Swap out.
								drum.setEnvelope(other);
								break;
							}
						}
					}
				}
			}
		}
		
		int i = 0;
		for(Z64Envelope env : envlist) env.setID(i++);
		return envlist;
	}

	public void writeTo(String path) throws IOException{
		//Gather envelopes
		List<Z64Envelope> envlist = mergeEnvelopes();
		FileBuffer envbuff = new MultiFileBuffer(envlist.size()+1);
		for(Z64Envelope env : envlist){
			int eventcount = env.eventCount();
			int esize = eventcount << 2;
			FileBuffer ebuff = new FileBuffer(esize + 4, true);
			ebuff.addToFile((short)esize);
			List<short[]> events = env.getEvents();
			for(short[] event : events){
				ebuff.addToFile(event[0]);
				ebuff.addToFile(event[1]);
			}
			envbuff.addToFile(ebuff);
		}
		int rcount = presets.size();
		
		//Header
		FileBuffer header = new FileBuffer(HEADER_SIZE, true);
		header.printASCIIToFile(TBL_MAGIC);
		header.addToFile((short)0); //Reserved flags
		header.addToFile((short)TBL_CURRENT_VERSION);
		header.addToFile(envlist.size());
		header.addToFile(rcount);
		header.addToFile((int)envbuff.getFileSize() + HEADER_SIZE);
		
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path));
		header.writeToStream(bos);
		envbuff.writeToStream(bos);
		FileBuffer buff = null;
		for(ZeqerPreset preset : presets.values()){
			buff = new FileBuffer(preset.estimateWriteBufferSize(), true);
			preset.serializeTo(buff);
			buff.writeToStream(bos);
		}
		bos.close();
	}
	
	public void writeScrubbed(String path) throws IOException{
		int rcount = presets.size();
		FileBuffer header = new FileBuffer(HEADER_SIZE, true);
		header.printASCIIToFile(TBL_MAGIC);
		header.addToFile((short)0); //Reserved flags
		header.addToFile((short)TBL_CURRENT_VERSION);
		header.addToFile(0);
		header.addToFile(rcount);
		header.addToFile(HEADER_SIZE);
		
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path));
		header.writeToStream(bos);
		FileBuffer buff = null;
		for(ZeqerPreset preset : presets.values()){
			String tagstr = "";
			List<String> tags = preset.getAllTags();
			boolean first = true;
			if(tags != null){
				for(String t : tags){
					if(!first) tagstr += ";";
					tagstr += t;
					first = false;
				}
			}
			
			String pname = preset.getName();
			int nlen = 0;
			if(pname != null) nlen = pname.length();
			String enlbl = preset.getEnumLabel();
			if(enlbl != null) nlen += enlbl.length();
			
			buff = new FileBuffer(32 + nlen + tagstr.length(), true);
			buff.addToFile(preset.getUID());
			int flags = 0x0;
			switch(preset.getType()){
			case ZeqerPreset.PRESET_TYPE_PERC:
				flags |= 0x1;
				break;
			case ZeqerPreset.PRESET_TYPE_SFX:
				flags |= 0x2;
				break;
			case ZeqerPreset.PRESET_TYPE_DRUM:
				flags |= 0x3;
				break;
			}
			buff.addToFile(flags);
			
			buff.addVariableLengthString(ZeqerCore.ENCODING, pname, BinFieldSize.WORD, 2);
			if (enlbl != null) buff.addVariableLengthString(ZeqerCore.ENCODING, enlbl, BinFieldSize.WORD, 2);
			else buff.addToFile((short)0);
			buff.addVariableLengthString(ZeqerCore.ENCODING, tagstr, BinFieldSize.WORD, 2);
			
			buff.writeToStream(bos);
		}
		bos.close();
	}
	
	/*----- Getters -----*/
	
	public boolean hasPreset(int uid){
		return presets.containsKey(uid);
	}
	
	public ZeqerPreset getPreset(int uid){
		return presets.get(uid);
	}
	
	public ZeqerPreset getPresetByName(String name){
		if(name_map == null){
			name_map = new HashMap<String, ZeqerPreset>();
			for(ZeqerPreset preset : presets.values()){
				name_map.put(preset.getName().toUpperCase(), preset);
			}
		}
		return name_map.get(name.toUpperCase());
	}
	
	public ZeqerPreset searchForEquivalentPreset(Z64Instrument inst){
		if(inst == null) return null;
		for(ZeqerPreset other : presets.values()){
			if(!(other instanceof ZeqerInstPreset)) continue;
			ZeqerInstPreset ipreset = (ZeqerInstPreset)other;
			Z64Instrument iother = ipreset.getInstrument();
			if(iother == null) continue;
			if(iother.instEquals(inst)) return other;
		}
		return null;
	}
	
	public List<ZeqerPreset> getAll(){
		List<ZeqerPreset> list = new ArrayList<ZeqerPreset>(presets.size()+1);
		list.addAll(presets.values());
		return list;
	}
	
	/*----- Setters -----*/
	
	public void addPreset(ZeqerPreset preset){
		if(preset == null) return;
		presets.put(preset.getUID(), preset);
	}
	
	public ZeqerPreset removePreset(int uid){
		ZeqerPreset prs = presets.remove(uid);
		if(prs != null){
			if(name_map != null){
				name_map.remove(prs.getName());
			}
		}
		return prs;
	}
	
	/*----- Misc -----*/
	
	public void exportTo(String dirpath) throws IOException{
		String tsvpath = dirpath + File.separator + "_zupst_inst_tbl.tsv";
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tsvpath), StandardCharsets.UTF_8));
		bw.write("#NAME\tUID\tENUM\tENVELOPE_IDX\tDECAY\tKEY_LO\tWAVE_LO\tTUNE_LO\t"
				+ "WAVE_MID\tTUNE_MID\tKEY_HI\tWAVE_HI\tTUNE_HI\tTAGS\n");
		for(ZeqerPreset preset : presets.values()){
			if(preset instanceof ZeqerInstPreset){
				ZeqerInstPreset ipreset = (ZeqerInstPreset)preset;
				ipreset.exportTSVLine(bw);
			}
		}
		bw.close();
		
		tsvpath = dirpath + File.separator + "_zupst_perc_tbl.tsv";
		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tsvpath), StandardCharsets.UTF_8));
		bw.write("#NAME\tUID\tSLOT\tENVELOPE_IDX\tDECAY\tPAN\tWAVE\tTUNE_COMMON\tTUNE_LOCAL\n");
		for(ZeqerPreset preset : presets.values()){
			if(preset instanceof ZeqerPercPreset){
				ZeqerPercPreset ppreset = (ZeqerPercPreset)preset;
				ppreset.exportTSVLine(bw);
			}
		}
		bw.close();
		
		tsvpath = dirpath + File.separator + "_zupst_sfx_tbl.tsv";
		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tsvpath), StandardCharsets.UTF_8));
		bw.write("#NAME\tUID\tGROUP\tSLOT\tWAVE\tTUNE\n");
		for(ZeqerPreset preset : presets.values()){
			if(preset instanceof ZeqerSFXPreset){
				ZeqerSFXPreset spreset = (ZeqerSFXPreset)preset;
				spreset.exportTSVLine(bw);
			}
		}
		bw.close();
		
		tsvpath = dirpath + File.separator + "_zupst_drum_tbl.tsv";
		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tsvpath), StandardCharsets.UTF_8));
		bw.write("#NAME\tUID\tENUM\tENVELOPE_IDX\tDECAY\tWAVE\tUNITY_KEY\tFINE_TUNE\tTAGS\n");
		for(ZeqerPreset preset : presets.values()){
			if(preset instanceof ZeqerDrumPreset){
				ZeqerDrumPreset dpreset = (ZeqerDrumPreset)preset;
				dpreset.exportTSVLine(bw);
			}
		}
		bw.close();
		
	}
	
}
