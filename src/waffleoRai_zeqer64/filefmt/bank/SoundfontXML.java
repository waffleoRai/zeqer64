package waffleoRai_zeqer64.filefmt.bank;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_Sound.nintendo.Z64Sound.Z64Tuning;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_SoundSynth.SynthMath;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileUtils;
import waffleoRai_soundbank.nintendo.z64.Z64Bank;
import waffleoRai_soundbank.nintendo.z64.Z64Drum;
import waffleoRai_soundbank.nintendo.z64.Z64Envelope;
import waffleoRai_soundbank.nintendo.z64.Z64Instrument;
import waffleoRai_soundbank.nintendo.z64.Z64SoundEffect;
import waffleoRai_zeqer64.ZeqerBank;
import waffleoRai_zeqer64.bankImport.BankImportInfo;
import waffleoRai_zeqer64.bankImport.BankImportInfo.PresetInfo;
import waffleoRai_zeqer64.bankImport.BankImportInfo.SampleInfo;
import waffleoRai_zeqer64.bankImport.BankImportInfo.SubBank;
import waffleoRai_zeqer64.bankImport.BankImporter;
import waffleoRai_zeqer64.filefmt.XMLReader;
import waffleoRai_zeqer64.filefmt.wave.ZeqerWaveIO;
import waffleoRai_zeqer64.filefmt.wave.ZeqerWaveTable;
import waffleoRai_zeqer64.filefmt.wave.ZeqerWaveIO.SampleImportOptions;
import waffleoRai_zeqer64.filefmt.wave.WaveTableEntry;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;
import waffleoRai_zeqer64.presets.ZeqerDrumPreset;
import waffleoRai_zeqer64.presets.ZeqerInstPreset;
import waffleoRai_zeqer64.presets.ZeqerPercPreset;
import waffleoRai_zeqer64.presets.ZeqerPercRegion;

public class SoundfontXML extends BankImporter{
	
	/*----- Constants -----*/
	
	public static final int INST_SAMPLE_LO = -1;
	public static final int INST_SAMPLE_MID = 0;
	public static final int INST_SAMPLE_HI = 1;
	
	public static final int MIDC_OCT_RECOMP = 5;
	public static final String[] RECOMP_TONE_NAMES = {"C", "C♯", "D", "D♯",
													  "E", "F", "F♯", "G",
													  "G♯", "A", "A♯", "B"};
	
	/*----- Instance Variables -----*/
	
	private Map<String, Z64Envelope> env_map;
	private Z64Bank output;
	
	private int medium = Z64Sound.MEDIUM_CART;
	private int cache = Z64Sound.CACHE_TEMPORARY;
	
	private boolean importSamples = false;
	private InstEntry[] insts;
	private DrumEntry[] drums;
	private List<SFXEntry> sfx; //In order read, NOT be index
	
	/*----- Subclasses -----*/
	
	private static class InstEntry{
		public Z64Instrument inst;
		public String enumString;
		public String presetString;
		public String sampleStringMid;
		public String sampleStringLow;
		public String sampleStringHigh;
		public int index = -1;
	}
	
	private static class DrumEntry{
		public Z64Drum drum;
		//public String presetString;
		public String sampleString;
		public String enumString;
		public int range_min = -1;
		public int range_max = -1;
	}
	
	private static class SFXEntry{
		public Z64SoundEffect sfx;
		public String sampleString;
		public String enumString;
		public int index = -1;
	}
	
	/*----- Init -----*/
	
	private SoundfontXML(){
		env_map = new HashMap<String, Z64Envelope>();
	}
	
	/*----- Parsing -----*/
	
	public void updateMedium(String str){
		str = str.toLowerCase();
		if(str.equals("ram")){
			medium = Z64Sound.MEDIUM_RAM;
		}
		else if(str.equals("cartridge")){
			medium = Z64Sound.MEDIUM_CART;
		}
		else if(str.equals("disk drive")){
			medium = Z64Sound.MEDIUM_DISK_DRIVE;
		}
	}
	
	public void updateCache(String str){
		str = str.toLowerCase();
		if(str.equals("permanent")){
			cache = Z64Sound.CACHE_PERMANENT;
		}
		else if(str.equals("persistent")){
			cache = Z64Sound.CACHE_PERSISTENT;
		}
		else if(str.equals("temporary")){
			cache = Z64Sound.CACHE_TEMPORARY;
		}
		else if(str.equals("any")){
			cache = Z64Sound.CACHE_ANY;
		}
		else if(str.equals("anynosyncload")){
			cache = Z64Sound.CACHE_ANYNOSYNCLOAD;
		}
	}

	public static int readNote(String notestr){
		int midival = 60;
		try{midival = Integer.parseInt(notestr);}
		catch(NumberFormatException ex){
			//No biggie, probably just note name.
			int charidx = 0;
			char notename = notestr.charAt(charidx++);
			notename = Character.toUpperCase(notename);
			int semis = 0;
			switch(notename){
			case 'A': semis = 9; break;
			case 'B': semis = 11; break;
			case 'C': semis = 0; break;
			case 'D': semis = 2; break;
			case 'E': semis = 4; break;
			case 'F': semis = 5; break;
			case 'G': semis = 7; break;
			default: 
				System.err.println("Could not read note value: " + notestr);
				return -1;
			}
			
			char c = notestr.charAt(charidx);
			//Check for sharp or flat
			if(!Character.isDigit(c)){
				switch(c){
				case '#': 
				case '♯':
					semis++; break;
				case 'b': 
				case '♭':
					semis--; break;
				default: 
					System.err.println("Could not read note value: " + notestr);
					return -1;
				}
				charidx++;
			}
			
			//Read the rest as a number.
			int oct = 0;
			try{oct = Integer.parseInt(notestr.substring(charidx));}
			catch(NumberFormatException ne){
				//Okay, now we dunno what to do with it.
				System.err.println("Could not read note value: " + notestr);
				return -1;
			}
			midival = ((oct + 1) * 12) + semis;
		}
		return midival;
	}
	
	public static String znote2Str(int z64note, int midc_oct){
		return midnote2Str(z64note+0x15, midc_oct);
	}
	
	public static String midnote2Str(int midinote, int midc_oct){
		int octave = (midinote/12);
		octave += (midc_oct - 5);
		int tone = midinote % 12;
		return RECOMP_TONE_NAMES[tone] + Integer.toString(octave);
	}
	
	private Z64WaveInfo resolveSampleLink(String str){
		if(str == null) return null;
		//Check if path...
		final String SEP = File.separator;
		
		if(str.contains(SEP) || str.contains("/")){
			//Try file
			//See if absolute.
			String path = str;
			if(!FileBuffer.fileExists(path) && str.contains("/")){
				//Relative path?
				String workingDir = super.path;
				int lastslash = workingDir.lastIndexOf(SEP);
				if(lastslash >= 0){
					workingDir = workingDir.substring(0, lastslash);
				}
				path = FileUtils.unixRelPath2Local(workingDir, path);
			}
			if(FileBuffer.fileExists(path)){
				//Load sample file (may need ZeqerWaveIO?)
				//Shoot in that case, we need sample import options...
				if(importSamples && sampleOps != null){
					if(core == null) return null;
					WaveTableEntry meta = core.importSample(path, sampleOps, sampleImportError);
					if(meta == null) return null;
					if(sampleTags != null){
						for(String tag : sampleTags) meta.addTag(tag);
					}
					return meta.getWaveInfo();
				}
				else return null;
			}
		}
		
		//See if already imported.
		Z64WaveInfo winfo = core.getSampleInfoByName(str);
		if(winfo == null){
			//Try isolating for a file name
			String fname = str;
			int lastslash = fname.lastIndexOf(SEP);
			if(lastslash >= 0){
				fname = fname.substring(lastslash+1);
			}
			int lastdot = fname.lastIndexOf('.');
			if(lastdot >= 0){
				fname = fname.substring(0, lastdot);
			}
			winfo = core.getSampleInfoByName(fname);
		}
		
		return winfo;
	}
	
	private int resolveInstSamples(InstEntry e){
		int result = 0;
		if(e == null) return result;
		if(e.inst == null) return result;
		
		//Flags last three bits of bytes 0 and 1. (Hi Mid Lo)
		//Byte 0 is whether import is okay
		//Byte 1 is whether region has sample string
		
		if(e.sampleStringLow != null) result |= (1 << 8);
		if(e.sampleStringMid != null) result |= (1 << 9);
		if(e.sampleStringHigh != null) result |= (1 << 10);
		
		Z64WaveInfo sample = resolveSampleLink(e.sampleStringMid);
		if(sample == null) return result;
		e.inst.setSampleMiddle(sample);
		result |= (1 << 1);
		
		if(e.sampleStringLow != null){
			sample = resolveSampleLink(e.sampleStringMid);
			if(sample != null){
				e.inst.setSampleLow(sample);
				result |= (1 << 0);
			}
		}
		else result |= (1 << 0);
		
		if(e.sampleStringHigh != null){
			sample = resolveSampleLink(e.sampleStringHigh);
			if(sample != null){
				e.inst.setSampleHigh(sample);
				result |= (1 << 2);
			}
		}
		else result |= (1 << 2);
		
		return result;
	}
	
	private boolean resolveDrumSample(DrumEntry e){
		if(e == null) return false;
		if(e.drum == null) return false;
		if(e.drum.getSample() != null) return true;
		
		Z64WaveInfo sample = resolveSampleLink(e.sampleString);
		if(sample == null) return false;
		e.drum.setSample(sample);
		
		return true;
	}
	
	private boolean resolveSFXSample(SFXEntry e){
		if(e == null) return false;
		if(e.sfx == null) return false;
		if(e.sfx.getSample() != null) return true;
		
		Z64WaveInfo sample = resolveSampleLink(e.sampleString);
		if(sample == null) return false;
		e.sfx.setSample(sample);
		
		return true;
	}
	
	public static Z64Envelope readEnvelope(Element xml_element){
		Z64Envelope env = new Z64Envelope();
		env.setIDRandom();
		String ename = xml_element.getAttribute("Name");
		env.setName(ename);
		
		NodeList scripts = xml_element.getElementsByTagName("Script");
		if(scripts.getLength() < 1) return null;
		
		Node script = scripts.item(0);
		if(script.getNodeType() == Node.ELEMENT_NODE){
			Element e_script = (Element)script;
			NodeList points = e_script.getElementsByTagName("Point");
			int ccount = points.getLength();
			for(int i = 0; i < ccount; i++){
				Node n = points.item(i);
				if(n.getNodeType() == Node.ELEMENT_NODE){
					Element point = (Element)n;
					String str1 = point.getAttribute("Delay");
					String str2 = point.getAttribute("Command");
					int p1 = Z64Sound.ENVCMD__ADSR_HANG;
					int p2 = 0;
					
					//See if special command
					try{p1 = Integer.parseInt(str1);}
					catch(NumberFormatException ex){
						if(str1.equalsIgnoreCase("ADSR_DISABLE")){
							p1 = Z64Sound.ENVCMD__ADSR_DISABLE;
						}
						else if(str1.equalsIgnoreCase("ADSR_HANG")){
							p1 = Z64Sound.ENVCMD__ADSR_HANG;
						}
						else if(str1.equalsIgnoreCase("ADSR_GOTO")){
							p1 = Z64Sound.ENVCMD__ADSR_GOTO;
						}
						else if(str1.equalsIgnoreCase("ADSR_RESTART")){
							p1 = Z64Sound.ENVCMD__ADSR_RESTART;
						}
						else {ex.printStackTrace(); return null;}
					}
					
					if(p1 > 0){
						try{p2 = Integer.parseInt(str2);}
						catch(NumberFormatException ex){
							ex.printStackTrace(); return null;
						}	
					}
					
					env.addEvent((short)p1, (short)p2);
				}
			}
		}
		
		return env;
	}
	
	public static SFXEntry readSoundEffect(Element xml_element){
		if(xml_element == null) return null;
		SFXEntry entry = new SFXEntry();
		entry.sfx = new Z64SoundEffect();
		entry.sfx.setIDRandom();
		entry.sfx.setName(xml_element.getAttribute("Name"));
		entry.enumString = xml_element.getAttribute("Enum");
		
		//Looks for Index, Sample or SampleName, and Pitch
		String attrstr = xml_element.getAttribute("Index");
		if(attrstr != null && !attrstr.isEmpty()){
			try{entry.index = Integer.parseInt(attrstr);}
			catch(NumberFormatException ex){
				System.err.println("Number Format Error: Index value \"" + attrstr + "\" for SoundEffect \"" + entry.sfx.getName() + "\" is invalid!");
			}	
		}
		
		//Find sample
		attrstr = xml_element.getAttribute("Sample");
		if(attrstr == null || attrstr.isEmpty()){
			attrstr = xml_element.getAttribute("SampleName");
		}
		if(attrstr == null || attrstr.isEmpty()){
			System.err.println("SoundEffect \"" + entry.sfx.getName() + "\" must have a sample!");
			return null;
		}
		entry.sampleString = attrstr;
		
		//Set pitch.
		if(xml_element.hasAttribute("Pitch")){
			attrstr = xml_element.getAttribute("Pitch");
			try{entry.sfx.setTuning(Float.parseFloat(attrstr));}
			catch(NumberFormatException ex){
				System.err.println("Number Format Error: Tuning value \"" + attrstr + "\" for SoundEffect \"" + entry.sfx.getName() + "\" is invalid!");
			}
		}
		else{
			//Pull from wave info
			entry.sfx.setTuning(1.0f);
			//entry.sfx.setTuning(wave.getTuning());
		}
		
		return entry;
	}
	
	public static float calculateDrumTune(String tuneto){
		//1. Determine midi note from string... 
		//	May be note name like "C4" or number like "60"
		//	Sharps and flats may be written with #,b, or unicode characters
		int midival = readNote(tuneto);
		if(midival < 0) return 1.0f;
		
		//2. This is the note that should play sample at 32kHz
		//	Figure out ratio to C4.
		int cents = (midival - 60) * 100;
		return (float)SynthMath.cents2FreqRatio(cents);
	}
	
	public DrumEntry readDrum(Element xml_element){
		if(xml_element == null) return null;
		DrumEntry entry = new DrumEntry();
		String mytag = xml_element.getTagName();
		
		//Common attr
		entry.drum = new Z64Drum();
		//entry.drum.setIDRandom();
		entry.drum.setName(xml_element.getAttribute("Name"));
		entry.enumString = xml_element.getAttribute("Enum");
		
		String attrstr = xml_element.getAttribute("Decay");
		if(attrstr != null && !attrstr.isEmpty()){
			try{entry.drum.setDecay((byte)Integer.parseInt(attrstr));}
			catch(NumberFormatException ex){
				System.err.println("Number Format Error: Decay value \"" + attrstr + "\" for Drum \"" + entry.drum.getName() + "\" is invalid!");
			}	
		}
		
		attrstr = xml_element.getAttribute("Pan");
		if(attrstr != null && !attrstr.isEmpty()){
			try{entry.drum.setPan((byte)Integer.parseInt(attrstr));}
			catch(NumberFormatException ex){
				System.err.println("Number Format Error: Pan value \"" + attrstr + "\" for Drum \"" + entry.drum.getName() + "\" is invalid!");
			}	
		}
		
		attrstr = xml_element.getAttribute("Sample");
		if(attrstr == null || attrstr.isEmpty()){
			attrstr = xml_element.getAttribute("SampleName");
		}
		if(attrstr == null || attrstr.isEmpty()){
			System.err.println("Drum \"" + entry.drum.getName() + "\" must have a sample!");
			sampleImportError.value = ZeqerBankIO.ERR_XML_ATTR_MISSING;
			return null;
		}
		
		//Find sample
		/*if(attrstr.endsWith(".aifc")) attrstr = attrstr.substring(0, attrstr.length() - 5);
		Z64WaveInfo wave = ZeqerCore.getActiveCore().getWaveByName(attrstr);
		if(wave == null){
			System.err.println("Could not find sample by name \"" + attrstr + "\"" + " for Drum \"" + entry.drum.getName() + "\"!");
			return null;
		}
		entry.drum.setSample(wave);*/
		entry.sampleString = attrstr;
		
		attrstr = xml_element.getAttribute("Envelope");
		if(attrstr != null && !attrstr.isEmpty()){
			Z64Envelope env = env_map.get(attrstr);
			if(env != null){
				entry.drum.setEnvelope(env);
			}
			else{
				System.err.println("Could not find envelope by name \"" + attrstr + "\"" + " for Drum \"" + entry.drum.getName() + "\"!");
				sampleImportError.value = ZeqerBankIO.ERR_XML_UNRESOLVED_LINK;
				return null;
			}
		}
		
		//Type specific
		if(mytag.equals("Drum")){
			//Standard build output
			//Look for Index and Pitch
			attrstr = xml_element.getAttribute("Index");
			if(attrstr != null && !attrstr.isEmpty()){
				try{
					int val = Integer.parseInt(attrstr);
					entry.range_min = val;
					entry.range_max = val;
				}
				catch(NumberFormatException ex){
					System.err.println("Number Format Error: Index value \"" + attrstr + "\" for Drum \"" + entry.drum.getName() + "\" is invalid!");
				}	
			}
			
			if(xml_element.hasAttribute("Pitch")){
				attrstr = xml_element.getAttribute("Pitch");
				try{
					entry.drum.setTuning(Z64Drum.localToCommonTuning(entry.range_min, Float.parseFloat(attrstr)));
				}
				catch(NumberFormatException ex){
					System.err.println("Number Format Error: Tuning value \"" + attrstr + "\" for Drum \"" + entry.drum.getName() + "\" is invalid!");
				}
			}
			else{
				//Pull from wave info
				//entry.drum.setTuning(wave.getTuning());
				//entry.drum.setTuning(Z64Drum.localToCommonTuning(entry.range_min, wave.getTuning()));
				entry.drum.setRootKey(Z64Sound.MIDDLE_C_S8);
				entry.drum.setFineTune((byte)0);
			}
		}
		else if(mytag.equals("DrumRange")){
			//Zeqer input
			//Look for DrumMin, DrumMax, and TuneTo
			attrstr = xml_element.getAttribute("DrumMin");
			if(attrstr != null && !attrstr.isEmpty()){
				try{
					entry.range_min = Integer.parseInt(attrstr);
				}
				catch(NumberFormatException ex){
					System.err.println("Number Format Error: Min Index value \"" + attrstr + "\" for Drum \"" + entry.drum.getName() + "\" is invalid!");
				}	
			}
			else entry.range_min = 0;
			
			attrstr = xml_element.getAttribute("DrumMax");
			if(attrstr != null && !attrstr.isEmpty()){
				try{
					entry.range_max = Integer.parseInt(attrstr);
				}
				catch(NumberFormatException ex){
					System.err.println("Number Format Error: Max Index value \"" + attrstr + "\" for Drum \"" + entry.drum.getName() + "\" is invalid!");
				}	
			}
			else entry.range_min = 63;
			
			attrstr = xml_element.getAttribute("TuneTo");
			if(attrstr != null && !attrstr.isEmpty()){
				Z64Tuning drumtune = new Z64Tuning();
				drumtune.root_key = (byte)readNote(attrstr);
				entry.drum.setTuning(drumtune);
				//entry.drum.setTuning(calculateDrumTune(attrstr));
			}
			else{
				entry.drum.setRootKey(Z64Sound.MIDDLE_C_S8);
				entry.drum.setFineTune((byte)0);
				//entry.drum.setTuning(Z64Drum.localToCommonTuning(entry.range_min, wave.getTuning()));
			}
		}
		
		return entry;
	}
	
	public static void readInstSampleElement(Element xml_element, InstEntry inst, int which_sample){
		String attrstr = xml_element.getAttribute("Sample");
		if(attrstr == null || attrstr.isEmpty()){
			attrstr = xml_element.getAttribute("SampleName");
		}
		if(attrstr == null || attrstr.isEmpty()){
			//No sample used here
			return;
		}
		
		/*if(attrstr.endsWith(".aifc")) attrstr = attrstr.substring(0, attrstr.length() - 5);
		Z64WaveInfo wave = ZeqerCore.getActiveCore().getWaveByName(attrstr);
		if(wave == null) return null;*/
		
		//Set. Add note range
		int midival = -1;
		switch(which_sample){
		case INST_SAMPLE_MID:
			//inst.setSampleMiddle(wave);
			inst.sampleStringMid = attrstr;
			break;
		case INST_SAMPLE_LO:
			//inst.setSampleLow(wave);
			inst.sampleStringLow = attrstr;
			attrstr = xml_element.getAttribute("MaxNote");
			midival = readNote(attrstr);
			midival -= 21;
			if(midival < 0 || midival > 63) midival = 0;
			inst.inst.setLowRangeTop((byte)midival);
			break;
		case INST_SAMPLE_HI:
			//inst.setSampleHigh(wave);
			inst.sampleStringHigh = attrstr;
			attrstr = xml_element.getAttribute("MinNote");
			midival = readNote(attrstr); midival -= 21;
			if(midival <= 0 || midival > 63) midival = 127;
			inst.inst.setHighRangeBottom((byte)midival);
			break;
		}
		
		//Tuning
		if(xml_element.hasAttribute("Pitch")){
			attrstr = xml_element.getAttribute("Pitch");
			try{
				float tuning = Float.parseFloat(attrstr);
				switch(which_sample){
				case INST_SAMPLE_MID:
					inst.inst.setTuningMiddle(tuning);
					break;
				case INST_SAMPLE_LO:
					inst.inst.setTuningLow(tuning);
					break;
				case INST_SAMPLE_HI:
					inst.inst.setTuningHigh(tuning);
					break;
				}
			}
			catch(NumberFormatException ex){
				System.err.println("Number Format Error: Tuning value \"" + attrstr + "\" for Instrument \"" + inst.inst.getName() + "\" is invalid!");
			}
		}
		else{
			//Try finding root note/finetune
			float tuning = 1.0f;
			String rootstr = xml_element.getAttribute("RootNote");
			String ftstr = xml_element.getAttribute("FineTune");
			int cents = 0; boolean flag = false;
			if(rootstr != null && !rootstr.isEmpty()){
				midival = readNote(rootstr);
				cents = (midival - 60) * 100;
				flag = true;
			}
			if(ftstr != null && !ftstr.isEmpty()){
				try{cents += Integer.parseInt(ftstr); flag = true;}
				catch(NumberFormatException ex){
					System.err.println("FineTune value: \"" + ftstr + "\" is not valid integer!");
				}
			}
			if(flag){
				tuning = (float)SynthMath.cents2FreqRatio(cents);
			}
			
			switch(which_sample){
			case INST_SAMPLE_MID:
				inst.inst.setTuningMiddle(tuning);
				break;
			case INST_SAMPLE_LO:
				inst.inst.setTuningLow(tuning);
				break;
			case INST_SAMPLE_HI:
				inst.inst.setTuningHigh(tuning);
				break;
			}
		}
		
		//return wave;
	}
	
	public InstEntry readInstrument(Element xml_element){
		InstEntry entry = new InstEntry();
		
		//Get index
		String attrstr = xml_element.getAttribute("Index");
		if(attrstr != null && !attrstr.isEmpty()){
			try{entry.index = Integer.parseInt(attrstr);}
			catch(NumberFormatException ex){
				System.err.println("Number Format Error: Index value \"" + attrstr + "\" for Instrument is invalid!");
			}	
		}
		
		//See if it's a preset or not.
		if(xml_element.hasAttribute("PresetName")){
			String presetname = xml_element.getAttribute("PresetName");
			/*entry.inst = ZeqerCore.getActiveCore().getPresetInstrumentByName(presetname);
			if(entry.inst == null){
				System.err.println("Could not find preset \"" + presetname + "\"!");
				return null;
			}*/
			entry.presetString = presetname;
		}
		else{
			entry.inst = new Z64Instrument();	
			entry.inst.setIDRandom();
			entry.enumString = xml_element.getAttribute("Enum");
			
			//Name, Decay, Envelope
			entry.inst.setName(xml_element.getAttribute("Name"));
			attrstr = xml_element.getAttribute("Decay");
			if(attrstr != null && !attrstr.isEmpty()){
				try{entry.inst.setDecay((byte)Integer.parseInt(attrstr));}
				catch(NumberFormatException ex){
					System.err.println("Number Format Error: Decay value \"" + attrstr + "\" for Instrument \"" + entry.inst.getName() + "\" is invalid!");
				}	
			}
			attrstr = xml_element.getAttribute("Envelope");
			if(attrstr != null && !attrstr.isEmpty()){
				Z64Envelope env = env_map.get(attrstr);
				if(env != null){
					entry.inst.setEnvelope(env);
				}
				else{
					System.err.println("Could not find envelope by name \"" + attrstr + "\"" + " for Instrument \"" + entry.inst.getName() + "\"!");
					sampleImportError.value = ZeqerBankIO.ERR_XML_UNRESOLVED_LINK;
					return null;
				}
			}
			
			//Figure out samples.
			NodeList children = xml_element.getChildNodes();
			int ccount = children.getLength();
			for(int i = 0; i < ccount; i++){
				Node n = children.item(i);
				if(n.getNodeType() == Node.ELEMENT_NODE){
					Element child = (Element)n;
					if(child.getTagName().equals("LowKey")){
						readInstSampleElement(child, entry, INST_SAMPLE_LO);
					}
					else if(child.getTagName().equals("MediumKey")){
						readInstSampleElement(child, entry, INST_SAMPLE_MID);
					}
					else if(child.getTagName().equals("HighKey")){
						readInstSampleElement(child, entry, INST_SAMPLE_HI);
					}
				}
			}
		}		
	
		return entry;
	}
	
	/*----- Read -----*/
	
	private void readMainElement(Document xml_doc){
		NodeList nl = xml_doc.getElementsByTagName("Soundfont");
		if(nl == null || nl.getLength() < 0){
			bankImportError.value = ZeqerBankIO.ERR_XML_ELEMENT_MISSING;
			return;
		}
		
		Node sf_node = nl.item(0);
		NamedNodeMap attrmap = sf_node.getAttributes();
		Node sf_attr = attrmap.getNamedItem("Medium");
		updateMedium(sf_attr.getNodeValue());
		sf_attr = attrmap.getNamedItem("CachePolicy");
		updateCache(sf_attr.getNodeValue());
	}
	
	private void readEnvelopes(Document xml_doc){
		NodeList nl = xml_doc.getElementsByTagName("Envelope");
		int ccount = nl.getLength();
		for(int i = 0; i < ccount; i++){
			Node n = nl.item(i);
			if(n.getNodeType() == Node.ELEMENT_NODE){
				Element child = (Element)n;
				Z64Envelope env = readEnvelope(child);
				env_map.put(env.getName(), env);
			}
		}
	}
	
	private void readSFXElements(Document xml_doc){
		NodeList nl = xml_doc.getElementsByTagName("SoundEffect");
		int ccount = nl.getLength();
		if(ccount < 1) return;
		sfx = new ArrayList<SFXEntry>(ccount); //In order read, NOT be index
		
		for(int i = 0; i < ccount; i++){
			Node n = nl.item(i);
			if(n.getNodeType() == Node.ELEMENT_NODE){
				Element child = (Element)n;
				SFXEntry sfxe = readSoundEffect(child);
				if(sfxe != null){
					sfx.add(sfxe);
				}
			}
		}
	}
	
	private void readDrumElements(Document xml_doc){
		//Drums
		NodeList nl = xml_doc.getElementsByTagName("Drum");
		int ccount = nl.getLength();
		if(ccount > 0){
			drums = new DrumEntry[64];
		}
		
		int c = 0;
		for(int i = 0; i < ccount; i++){
			Node n = nl.item(i);
			if(n.getNodeType() == Node.ELEMENT_NODE){
				Element child = (Element)n;
				DrumEntry drume = readDrum(child);
				if(drume != null){
					if(drume.range_min < 0) drume.range_min = c++;
					if(drume.range_max < 0) drume.range_max = drume.range_min;
					for(int j = drume.range_min; j <= drume.range_max; j++){
						drums[j] = drume;
					}
				}
			}
		}
		
		nl = xml_doc.getElementsByTagName("DrumRange");
		ccount = nl.getLength();
		if((ccount > 0) && (drums == null)){
			drums = new DrumEntry[64];
		}
		
		c = 0;
		for(int i = 0; i < ccount; i++){
			Node n = nl.item(i);
			if(n.getNodeType() == Node.ELEMENT_NODE){
				Element child = (Element)n;
				DrumEntry drume = readDrum(child);
				if(drume != null){
					if(drume.range_min < 0) drume.range_min = c;
					if(drume.range_max < 0) drume.range_max = drume.range_min;
					for(int j = drume.range_min; j <= drume.range_max; j++){
						drums[j] = drume;
					}
					c = drume.range_max + 1;
				}
			}
		}
	}
	
	private void readInstElements(Document xml_doc){
		insts = new InstEntry[126];
		
		NodeList nl = xml_doc.getElementsByTagName("Instrument");
		int ccount = nl.getLength();
		int c = 0;
		for(int i = 0; i < ccount; i++){
			Node n = nl.item(i);
			if(n.getNodeType() == Node.ELEMENT_NODE){
				Element child = (Element)n;
				InstEntry inste = readInstrument(child);
				if(inste != null){
					if(inste.index < 0) inste.index = c;
					inste.inst.setID(inste.index);
					c = inste.index + 1;
					insts[inste.index] = inste;
				}
			}
		}
	}
	
	public void genImportInfo(){
		sampleOps = new SampleImportOptions();
		
		//Sample name pool
		Set<String> pool = new HashSet<String>();
		if(insts != null){
			for(int i = 0; i < insts.length; i++){
				if(insts[i] != null){
					if(insts[i].sampleStringMid != null) pool.add(insts[i].sampleStringMid);
					if(insts[i].sampleStringLow != null) pool.add(insts[i].sampleStringLow);
					if(insts[i].sampleStringHigh != null) pool.add(insts[i].sampleStringHigh);
				}
			}
		}
		
		if(drums != null){
			for(int i = 0; i < drums.length; i++){
				if(drums[i] != null){
					if(drums[i].sampleString != null) pool.add(drums[i].sampleString);
				}
			}
		}
		
		if(sfx != null){
			for(SFXEntry e : sfx){
				if(e.sampleString != null) pool.add(e.sampleString);
			}
		}
		
		int salloc = pool.size();
		super.info = new BankImportInfo(1, salloc);
		
		int i = 0;
		for(String sname : pool){
			SampleInfo sinfo = info.getSample(i);
			sinfo.id = i++;
			sinfo.name = sname;
		}
		
		SubBank bnk = info.getBank(0);
		bnk.allocInstruments(128);
		bnk.cache = this.cache;
		bnk.medium = this.medium;
		
		for(i = 0; i < 126; i++){
			PresetInfo pinfo = bnk.instruments[i];
			if(insts[i] != null){
				pinfo.emptySlot = false;
				pinfo.importToFont = true;
				pinfo.savePreset = false;
				pinfo.index = i;
				if(insts[i].inst != null){
					pinfo.name = insts[i].inst.getName();
				}
				else pinfo.name = insts[i].presetString;
			}
			else{
				pinfo.emptySlot = true;
				pinfo.importToFont = false;
				pinfo.savePreset = false;
			}
		}
		
		if(drums != null){
			PresetInfo pinfo = bnk.instruments[127];
			pinfo.emptySlot = false;
			pinfo.importToFont = true;
			pinfo.savePreset = false;
			pinfo.name = "Percussion";
			
			bnk.percInst = 127;
			bnk.saveDrums = false;
			bnk.saveDrumset = false;
		}
		else{
			bnk.percInst = -1;
			bnk.saveDrums = false;
			bnk.saveDrumset = false;
		}
		
		if(sfx != null && !sfx.isEmpty()){
			PresetInfo pinfo = bnk.instruments[126];
			pinfo.emptySlot = false;
			pinfo.importToFont = true;
			pinfo.savePreset = false;
			pinfo.name = "SFX";
		}
	}
	
	public static SoundfontXML readSFXMLContents(String path){
		SoundfontXML sfxml = new SoundfontXML();
		try {
			Document xml_doc = XMLReader.readXMLStatic(path);
			
			//Top soundfont node
			sfxml.readMainElement(xml_doc);
			if(sfxml.bankImportError.value != ZeqerBankIO.ERR_NONE) return sfxml;
			
			sfxml.readEnvelopes(xml_doc);
			sfxml.readSFXElements(xml_doc);
			sfxml.readDrumElements(xml_doc);
			sfxml.readInstElements(xml_doc);
		} 
		catch (ParserConfigurationException e) {
			sfxml.bankImportError.value = ZeqerBankIO.ERR_FILE_READ_FAILED;
			e.printStackTrace();
		} 
		catch (SAXException e) {
			sfxml.bankImportError.value = ZeqerBankIO.ERR_FILE_READ_FAILED;
			e.printStackTrace();
		} 
		catch (IOException e) {
			sfxml.bankImportError.value = ZeqerBankIO.ERR_FILE_READ_FAILED;
			e.printStackTrace();
		}
		
		return sfxml;
	}
	
	public static Z64Bank readSFXML(String path, ZeqerCoreInterface coreIFace) throws ParserConfigurationException, SAXException, IOException{
		//This method doesn't do any importing - neither samples nor bank
		Document xml_doc = XMLReader.readXMLStatic(path);
		SoundfontXML sfxml = new SoundfontXML();
		sfxml.output = new Z64Bank();
		sfxml.importSamples = false;
		sfxml.core = coreIFace;
		//sfxml.output.setSamplesOrderedByUID(true);
		
		//Top soundfont node
		sfxml.readMainElement(xml_doc);
		sfxml.output.setCachePolicy(sfxml.cache);
		sfxml.output.setMedium(sfxml.medium);
		
		//Envelopes
		sfxml.readEnvelopes(xml_doc);
		
		//Sound effects
		sfxml.readSFXElements(xml_doc);
		if(sfxml.sfx != null){
			int sfxcount = sfxml.sfx.size();
			if(sfxcount > 0){
				sfxml.output.ensureSFXSlotCapacity(sfxcount);
				for(SFXEntry entry : sfxml.sfx){
					Z64WaveInfo sample = sfxml.resolveSampleLink(entry.sampleString);
					if(sample != null){
						entry.sfx.setSample(sample);
						sfxml.output.setSFX(entry.index, entry.sfx);
						sfxml.output.setSFXPresetEnumString(entry.index, entry.enumString);
					}
					else{
						System.err.println("SoundfontXML.readSFXML || SFX sample \"" + entry.sampleString + "\" could not be resolved!");
					}
				}
			}
		}
		
		//Drums
		sfxml.readDrumElements(xml_doc);
		if(sfxml.drums != null){
			for(int i = 0; i < sfxml.drums.length; i++){
				if(sfxml.drums[i] != null){
					DrumEntry entry = sfxml.drums[i];
					if(entry.drum.getSample() == null){
						Z64WaveInfo sample = sfxml.resolveSampleLink(entry.sampleString);
						if(sample != null){
							entry.drum.setSample(sample);
							sfxml.output.setDrum(entry.drum, i, i);
							if(entry.range_min == entry.range_max){
								sfxml.output.setDrumPresetEnumString(i, entry.enumString);
							}
							else{
								sfxml.output.setDrumPresetEnumString(i, entry.enumString + "_" + String.format("%02d", i));
							}
						}
						else{
							System.err.println("SoundfontXML.readSFXML || Drum sample \"" + entry.sampleString + "\" could not be resolved!");
						}
					}
				}
			}
		}
		
		//Instruments
		sfxml.readInstElements(xml_doc);
		for(int i = 0; i < 126; i++){
			InstEntry entry = sfxml.insts[i];
			if(entry == null) continue;
			
			//Check if preset requested...
			if(entry.presetString != null){
				ZeqerInstPreset ipre = sfxml.core.getInstrumentPresetByName(entry.presetString);
				if(ipre != null){
					sfxml.output.setInstrument(entry.index, ipre.getInstrument());
					if(entry.enumString != null){
						sfxml.output.setInstPresetEnumString(entry.index, entry.enumString);
					}
					else{
						sfxml.output.setInstPresetEnumString(entry.index, ipre.getEnumLabel());
					}
				}
				else{
					System.err.println("SoundfontXML.readSFXML || Instrument preset \"" + entry.presetString + "\" could not be resolved!");
				}
			}
			else{
				Z64WaveInfo sample = null;
				sample = sfxml.resolveSampleLink(entry.sampleStringMid);
				if(sample == null){
					entry.inst.setSampleMiddle(sample);
				}
				else{
					System.err.println("SoundfontXML.readSFXML || Instrument main sample \"" + entry.sampleStringMid + "\" could not be resolved!");
				}
				
				if(entry.sampleStringLow != null){
					sample = sfxml.resolveSampleLink(entry.sampleStringLow);
					if(sample == null){
						entry.inst.setSampleLow(sample);
					}
					else{
						System.err.println("SoundfontXML.readSFXML || Instrument low sample \"" + entry.sampleStringLow + "\" could not be resolved!");
					}
				}
				
				if(entry.sampleStringHigh != null){
					sample = sfxml.resolveSampleLink(entry.sampleStringHigh);
					if(sample == null){
						entry.inst.setSampleHigh(sample);
					}
					else{
						System.err.println("SoundfontXML.readSFXML || Instrument low sample \"" + entry.sampleStringHigh + "\" could not be resolved!");
					}
				}

				//Add to bank
				sfxml.output.setInstrument(entry.index, entry.inst);
				sfxml.output.setInstPresetEnumString(entry.index, entry.enumString);
			}
		}
		
		return sfxml.output;
	}
	
	public boolean importToCore(){
		if(core == null){
			bankImportError.value = ZeqerBankIO.ERR_NULL_CORE_LINK;
			return false;
		}
		if(info == null){
			bankImportError.value = ZeqerBankIO.ERR_NULL_IMPORT_PARAMS;
			return false;
		}
		
		Random rand = new Random();
		importSamples = (sampleOps != null);
		int sfxcount = 0;
		if(sfx != null) sfxcount = sfx.size();
		
		Set<String> addedsmpl = new HashSet<String>();
		
		output = new Z64Bank(sfxcount);
		output.setCachePolicy(cache);
		output.setMedium(medium);
		SubBank bnk = info.getBank(0);
		
		//Sound effects
		if(sfxcount > 0){
			PresetInfo pinfo = bnk.instruments[126];
			if(pinfo != null){
				if(pinfo.importToFont && bnk.importAsFont){
					for(SFXEntry entry : sfx){
						if(resolveSFXSample(entry)){
							addedsmpl.add(entry.sampleString);
							output.setSFX(entry.index, entry.sfx);
							output.setSFXPresetEnumString(entry.index, entry.enumString);
						}
						else{
							bankImportError.value = ZeqerBankIO.ERR_SAMPLE_IMPORT_FAILED;
							return false;
						}
					}
				}
				//Save preset is ignored for SFX set for now.
			}
		}
		
		//Percussion
		if(drums != null){
			if(bnk.importAsFont && bnk.importPercToFont){
				for(int i = 0; i < drums.length; i++){
					if(drums[i] != null){
						DrumEntry entry = drums[i];
						if(resolveDrumSample(entry)){
							addedsmpl.add(entry.sampleString);
							output.setDrum(entry.drum, i, i);
							if(entry.range_min == entry.range_max){
								output.setDrumPresetEnumString(i, entry.enumString);
							}
							else{
								output.setDrumPresetEnumString(i, entry.enumString + "_" + String.format("%02d", i));
							}
						}
						else{
							bankImportError.value = ZeqerBankIO.ERR_SAMPLE_IMPORT_FAILED;
							return false;
						}
					}
				}
			}
			
			ZeqerPercPreset ppre = new ZeqerPercPreset(rand.nextInt());
			for(int i = 0; i < drums.length; i++){
				if(drums[i] != null){
					DrumEntry entry = drums[i];
					if(resolveDrumSample(entry)){
						addedsmpl.add(entry.sampleString);
						ppre.setDrumToSlot(i, entry.drum);
					}
					else{
						bankImportError.value = ZeqerBankIO.ERR_SAMPLE_IMPORT_FAILED;
						return false;
					}
				}
			}
			ppre.consolidateRegions();
			if(presetTags != null){
				for(String tag : presetTags) ppre.addTag(tag);
			}
			
			if(bnk.saveDrumset){
				if(!core.addUserPreset(ppre)){
					bankImportError.value = ZeqerBankIO.ERR_PRESET_IMPORT_FAILED;
					return false;
				}
			}
			
			if(bnk.saveDrums){
				int rcount = ppre.getRegionCount();
				for(int r = 0; r < rcount; r++){
					ZeqerPercRegion reg = ppre.getRegion(r);
					if(reg != null){
						Z64Drum drum = reg.getDrumData();
						if(drum != null){
							ZeqerDrumPreset dpre = new ZeqerDrumPreset(drum);
							dpre.setUID(dpre.hashToUID());
							if(presetTags != null){
								for(String tag : presetTags) dpre.addTag(tag);
							}
							if(!core.addUserPreset(dpre)){
								bankImportError.value = ZeqerBankIO.ERR_PRESET_IMPORT_FAILED;
								return false;
							}
						}
					}
				}
			}
		}
		
		//Instruments
		for(int i = 0; i < 126; i++){
			PresetInfo pinfo = bnk.instruments[i];
			if(pinfo.emptySlot) continue;
			if(insts[i] == null) continue;
			
			InstEntry entry = insts[i];
			if((pinfo.importToFont && bnk.importAsFont) || pinfo.savePreset){
				if(entry.presetString == null) {
					int wres = resolveInstSamples(entry) & 0x7;
					if(wres != 0x7){
						bankImportError.value = ZeqerBankIO.ERR_SAMPLE_IMPORT_FAILED;
						return false;
					}
					
					//Add to added sample pool
					addedsmpl.add(entry.sampleStringMid);
					if(entry.sampleStringLow != null) addedsmpl.add(entry.sampleStringLow);
					if(entry.sampleStringHigh != null) addedsmpl.add(entry.sampleStringHigh);	
				}
			}
			
			if(pinfo.importToFont && bnk.importAsFont){
				if(entry.presetString != null) {
					ZeqerInstPreset pmatch = core.getInstrumentPresetByName(entry.presetString);
					if(pmatch == null) {
						bankImportError.value = ZeqerBankIO.ERR_INVALID_PRESET_LINK;
						return false;
					}
					output.setInstrument(entry.index, pmatch.getInstrument());
				}
				else {
					//Import font instrument
					output.setInstrument(entry.index, entry.inst);
				}
				output.setInstPresetEnumString(entry.index, entry.enumString);
			}
			
			if(pinfo.savePreset){
				//Import preset
				if(entry.presetString == null) {
					//No point if it IS a preset link.
					ZeqerInstPreset ipre = new ZeqerInstPreset(entry.inst);
					ipre.setUID(ipre.hashToUID());
					if(presetTags != null){
						for(String tag : presetTags) ipre.addTag(tag);
					}
					if(!core.addUserPreset(ipre)){
						bankImportError.value = ZeqerBankIO.ERR_PRESET_IMPORT_FAILED;
						return false;
					}
				}
			}
		}
		
		//Additional samples
		int scount = info.getSampleCount();
		for(int s = 0; s < scount; s++) {
			SampleInfo sinfo = info.getSample(s);
			if(!sinfo.importSample) continue;
			if(addedsmpl.contains(sinfo.name)) continue;
			Z64WaveInfo winfo = resolveSampleLink(sinfo.name);
			if(winfo == null) {
				bankImportError.value = ZeqerBankIO.ERR_SAMPLE_IMPORT_FAILED;
				return false;
			}
			
		}
		
		//Import font itself, if requested
		if(bnk.importAsFont) {
			ZeqerBank ires = core.addUserBank(output);
			if(ires == null) {
				bankImportError.value = ZeqerBankIO.ERR_BANK_IMPORT_FAILED;
				return false;
			}
		}
		
		return true;
	}
	
	/*----- Write -----*/
	
	public static String tabstr(int tabs){
		if(tabs <= 0) return "";
		StringBuilder sb = new StringBuilder(tabs);
		for(int i = 0; i < tabs; i++) sb.append('\t');
		return sb.toString();
	}
	
	public static boolean writeSFBankElement(Writer out, int tabs, String bank_name) throws IOException{
		String tabstr = tabstr(tabs);
		out.write(tabstr);
		out.write("<Bank ");
		out.write("Name=\"");
		out.write(bank_name);
		out.write("\"/>\n");
		return true;
	}
	
	public static boolean writeSFBankElement(Writer out, int tabs, int bank_idx) throws IOException{
		String tabstr = tabstr(tabs);
		out.write(tabstr);
		out.write("<Bank ");
		out.write("Index=\"");
		out.write(Integer.toString(bank_idx));
		out.write("\"/>\n");
		return true;
	}

	public static boolean writeSFInstElement(Writer out, int tabs, int prefix_idx, Z64Instrument inst, String enumstr) throws IOException{
		if(out == null) return false;
		String tabstr = tabstr(tabs);
		out.write(tabstr);
		if(inst == null){
			out.write("<Instrument/>\n");
			return true;
		}
		
		out.write("<Instrument Name=\"");
		out.write(inst.getName());
		out.write("\" Index=\"");
		out.write(Integer.toString(prefix_idx));
		out.write("\" Enum=\"");
		out.write(enumstr);
		out.write("\" Decay=\"");
		out.write(Integer.toString(Byte.toUnsignedInt(inst.getDecay())));
		out.write("\" Envelope=\"");
		Z64Envelope env = inst.getEnvelope();
		if(env != null) out.write(env.getName());
		out.write("\">\n");
		
		//Low
		out.write(tabstr + "\t");
		Z64WaveInfo winfo = inst.getSampleLow();
		if(winfo != null){
			out.write("<LowKey Sample=\"");
			out.write(winfo.getName());
			out.write(".aifc\" MaxNote=\"");
			out.write(znote2Str(inst.getLowRangeTop(), MIDC_OCT_RECOMP));
			float tune = inst.getTuningLow();
			if(tune != winfo.getTuning()){
				out.write("\" Pitch=\"");
				out.write(Float.toString(tune));
			}
			out.write("\"/>\n");
		}
		else out.write("<LowKey/>\n");
		
		//Mid
		out.write(tabstr + "\t");
		winfo = inst.getSampleMiddle();
		if(winfo != null){
			out.write("<MediumKey Sample=\"");
			out.write(winfo.getName());
			out.write(".aifc");
			float tune = inst.getTuningMiddle();
			if(tune != winfo.getTuning()){
				out.write("\" Pitch=\"");
				out.write(Float.toString(tune));
			}
			out.write("\"/>\n");
		}
		else out.write("<MediumKey/>\n"); //This should not happen, but it is here.
				
		//High
		out.write(tabstr + "\t");
		winfo = inst.getSampleHigh();
		if(winfo != null){
			out.write("<HighKey Sample=\"");
			out.write(winfo.getName());
			out.write(".aifc\" MinNote=\"");
			out.write(znote2Str(inst.getHighRangeBottom(), MIDC_OCT_RECOMP));
			float tune = inst.getTuningHigh();
			if(tune != winfo.getTuning()){
				out.write("\" Pitch=\"");
				out.write(Float.toString(tune));
			}
			out.write("\"/>\n");
		}
		else out.write("<HighKey/>\n");
		
		out.write(tabstr);
		out.write("</Instrument>\n");
		return true;
	}
	
	public static boolean writeSFDrumElement(Writer out, int tabs, int slot_idx, Z64Drum drum, String enumstr) throws IOException{
		if(out == null) return false;
		String tabstr = tabstr(tabs);
		out.write(tabstr);
		if(drum == null){
			out.write("<Drum/>\n");
			return true;
		}
		
		out.write("<Drum Name=\"");
		out.write(drum.getName());
		out.write("\" Index=\"");
		out.write(Integer.toString(slot_idx));
		out.write("\" Enum=\"");
		out.write(enumstr);
		out.write("\" Decay=\"");
		out.write(Integer.toString(Byte.toUnsignedInt(drum.getDecay())));
		out.write("\" Pan=\"");
		out.write(Integer.toString(drum.getPan()));
		
		Z64WaveInfo winfo = drum.getSample();
		out.write("\" Sample=\"");
		out.write(winfo.getName());
		out.write(".aifc\"");
		
		Z64Envelope env = drum.getEnvelope();
		if(env != null){
			out.write(" Envelope=\"");
			out.write(env.getName());
			out.write("\"");
		}
		
		float ltune = Z64Drum.commonToLocalTuning(slot_idx, drum.getTuning());
		if(ltune != winfo.getTuning()){
			out.write(" Pitch=\"");
			out.write(Float.toString(ltune));
			out.write("\"");
		}
		out.write("/>\n");
		
		return true;
	}
	
	public static boolean writeSFEffectElement(Writer out, int tabs, int slot_idx, Z64SoundEffect sfx, String enumstr) throws IOException{
		if(out == null) return false;
		String tabstr = tabstr(tabs);
		out.write(tabstr);
		if(sfx == null){
			out.write("<SoundEffect/>\n");
			return true;
		}
		
		out.write("<SoundEffect Name=\"");
		out.write(sfx.getName());
		out.write("\" Index=\"");
		out.write(Integer.toString(slot_idx));
		out.write("\" Enum=\"");
		out.write(enumstr);
		
		Z64WaveInfo winfo = sfx.getSample();
		out.write("\" Sample=\"");
		out.write(winfo.getName());
		out.write(".aifc\"");
		
		float tune = sfx.getTuning();
		if(tune != winfo.getTuning()){
			out.write(" Pitch=\"");
			out.write(Float.toString(tune));
			out.write("\"");
		}
		out.write("/>\n");
		
		return true;
	}
	
	public static boolean writeSFEnvelopeElement(Writer out, int tabs, Z64Envelope env) throws IOException{
		if(out == null) return false;
		String tabstr = tabstr(tabs);
		out.write(tabstr);
		if(env == null){
			out.write("<Envelope/>\n");
			return true;
		}
		
		out.write("<Envelope Name=\"");
		out.write(env.getName());
		out.write("\">\n");
		
		List<short[]> script = env.getEvents();
		out.write(tabstr + "\t<Script>\n");
		for(short[] event : script){
			out.write(tabstr); 
			out.write("\t\t<Point Delay=\"");
			switch(event[0]){
			case Z64Sound.ENVCMD__ADSR_DISABLE:
				out.write("ADSR_DISABLE"); 
				break;
			case Z64Sound.ENVCMD__ADSR_HANG:
				out.write("ADSR_HANG"); 
				break;
			case Z64Sound.ENVCMD__ADSR_GOTO:
				out.write("ADSR_GOTO"); 
				break;
			case Z64Sound.ENVCMD__ADSR_RESTART:
				out.write("ADSR_RESTART"); 
				break;
			default:
				out.write(Short.toString(event[0]));
				break;
			}
			out.write("\" Command=\"");
			out.write(Short.toString(event[1]));
			out.write("\"/>\n");
		}
		out.write(tabstr + "\t</Script>\n");
		
		out.write(tabstr);
		out.write("</Envelope>\n");
		return true;
	}
	
	public static boolean writeSFSampleElement(Writer out, int tabs, Z64WaveInfo wave, int bankidx) throws IOException{
		if(out == null) return false;
		
		String tabstr = tabstr(tabs);
		out.write(tabstr);
		if(wave == null) {
			out.write("<Sample/>\n");
			return true;
		}
		
		out.write("<Sample ");
		out.write("Name=\"" + String.format("Sample%08x", wave.getUID()) + "\" ");
		out.write("File=\"" + wave.getName() + ".aifc\" ");
		out.write("BankIndex=\"" + bankidx + "\"");
		out.write("/>\n");
		
		return true;
	}
	
	public static boolean writeSFXML(Writer out, Z64Bank data) throws IOException {
		if(out == null) return false;
		out.write("<?xml version=\"1.0\" ?>\n");
		
		//Soundfont element
		out.write("<Soundfont Medium=\"");
		out.write(Z64Sound.getMediumTypeString(data.getMedium()));
		out.write("\" CachePolicy=\"");
		out.write(Z64Sound.getCacheTypeString(data.getCachePolicy()));
		out.write("\">\n");
		
		//WArc links
		out.write("\t<SampleBanks>\n");
		int val = data.getPrimaryWaveArcIndex();
		if(val >= 0) writeSFBankElement(out, 2, val);
		val = data.getSecondaryWaveArcIndex();
		if(val >= 0) writeSFBankElement(out, 2, val);
		out.write("\t</SampleBanks>\n");
		
		//Organize envelopes
		out.write("\t<Envelopes>\n");
		List<Z64Envelope> envlist = data.getAllEnvelopes();
		int i = 0;
		for(Z64Envelope env : envlist) {
			env.setName(String.format("Env_%04d", i));
			env.setID(i++);
		}
		
		//Instruments
		out.write("\t<Instruments>\n");
		int icount = data.getEffectiveInstCount();
		for(i = 0; i < icount; i++) {
			Z64Instrument inst = data.getInstrumentInSlot(i);
			if(inst != null) {
				writeSFInstElement(out, 2, i, inst, data.getInstPresetEnumString(i));
			}
			else {
				out.write("\t\t<Instrument/>\n");
			}
		}
		out.write("\t</Instruments>\n");
		
		//Percussion
		int pcount = data.getEffectivePercCount();
		if(pcount > 0) {
			out.write("\t<Drums>\n");
			for(i = 0; i < pcount; i++) {
				Z64Drum drum = data.getDrumInSlot(i);
				if(drum != null) {
					writeSFDrumElement(out, 2, i, drum, data.getDrumSlotEnumString(i));
				}
				else {
					out.write("\t\t<Drum/>\n");
				}
			}
			out.write("\t</Drums>\n");
		}
		
		//SFX
		int xcount = data.getEffectiveSFXCount();
		if(xcount > 0) {
			out.write("\t<SoundEffects>\n");
			for(i = 0; i < xcount; i++) {
				Z64SoundEffect sfx = data.getSFXInSlot(i);
				if(sfx != null) {
					writeSFEffectElement(out, 2, i, sfx, data.getSFXSlotEnumString(i));
				}
				else {
					out.write("\t\t<SoundEffect/>\n");
				}
			}
			out.write("\t</SoundEffects>\n");
		}
		
		//Envelopes
		out.write("\t<Envelopes>\n");
		for(Z64Envelope env : envlist) {
			writeSFEnvelopeElement(out, 2, env);
		}
		out.write("\t</Envelopes>\n");
		
		//Samples
		out.write("\t<Samples>\n");
		List<Z64WaveInfo> samples = data.getAllWaveBlocks();
		for(Z64WaveInfo wave : samples) {
			writeSFSampleElement(out, 2, wave, 0);
		}
		out.write("\t</Samples>\n");
		
		out.write("</Soundfont>");
		return true;
	}
	
}
