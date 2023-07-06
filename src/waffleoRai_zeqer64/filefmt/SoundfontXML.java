package waffleoRai_zeqer64.filefmt;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import waffleoRai_soundbank.nintendo.z64.Z64Bank;
import waffleoRai_soundbank.nintendo.z64.Z64Drum;
import waffleoRai_soundbank.nintendo.z64.Z64Envelope;
import waffleoRai_soundbank.nintendo.z64.Z64Instrument;
import waffleoRai_soundbank.nintendo.z64.Z64SoundEffect;
import waffleoRai_zeqer64.ZeqerCore;

public class SoundfontXML {
	
	public static final int INST_SAMPLE_LO = -1;
	public static final int INST_SAMPLE_MID = 0;
	public static final int INST_SAMPLE_HI = 1;
	
	public static final int MIDC_OCT_RECOMP = 5;
	public static final String[] RECOMP_TONE_NAMES = {"C", "C♯", "D", "D♯",
													  "E", "F", "F♯", "G",
													  "G♯", "A", "A♯", "B"};
	
	private Map<String, Z64Envelope> env_map;
	private Z64Bank output;
	
	private int medium = Z64Sound.MEDIUM_CART;
	private int cache = Z64Sound.CACHE_TEMPORARY;
	
	private static class InstEntry{
		public Z64Instrument inst;
		public int index = -1;
	}
	
	private static class DrumEntry{
		public Z64Drum drum;
		public int range_min = -1;
		public int range_max = -1;
	}
	
	private static class SFXEntry{
		public Z64SoundEffect sfx;
		public int index = -1;
	}
	
	private SoundfontXML(){
		env_map = new HashMap<String, Z64Envelope>();
	}
	
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
	
	public Z64Envelope readEnvelope(Element xml_element){
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
	
	public SFXEntry readSoundEffect(Element xml_element){
		if(xml_element == null) return null;
		SFXEntry entry = new SFXEntry();
		entry.sfx = new Z64SoundEffect();
		entry.sfx.setIDRandom();
		entry.sfx.setName(xml_element.getAttribute("Name"));
		
		//Looks for Index, Sample or SampleName, and Pitch
		String attrstr = xml_element.getAttribute("Index");
		if(attrstr != null && !attrstr.isEmpty()){
			try{entry.index = Integer.parseInt(attrstr);}
			catch(NumberFormatException ex){
				System.err.println("Number Format Error: Index value \"" + attrstr + "\" for SoundEffect \"" + entry.sfx.getName() + "\" is invalid!");
			}	
		}
		
		attrstr = xml_element.getAttribute("Sample");
		if(attrstr == null || attrstr.isEmpty()){
			attrstr = xml_element.getAttribute("SampleName");
		}
		if(attrstr == null || attrstr.isEmpty()){
			System.err.println("SoundEffect \"" + entry.sfx.getName() + "\" must have a sample!");
			return null;
		}
		
		//Find sample
		if(attrstr.endsWith(".aifc")) attrstr = attrstr.substring(0, attrstr.length() - 5);
		Z64WaveInfo wave = ZeqerCore.getActiveCore().getWaveByName(attrstr);
		if(wave == null){
			System.err.println("Could not find sample by name \"" + attrstr + "\"" + " for SoundEffect \"" + entry.sfx.getName() + "\"!");
			return null;
		}
		entry.sfx.setSample(wave);
		
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
			entry.sfx.setTuning(wave.getTuning());
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
			return null;
		}
		
		//Find sample
		if(attrstr.endsWith(".aifc")) attrstr = attrstr.substring(0, attrstr.length() - 5);
		Z64WaveInfo wave = ZeqerCore.getActiveCore().getWaveByName(attrstr);
		if(wave == null){
			System.err.println("Could not find sample by name \"" + attrstr + "\"" + " for Drum \"" + entry.drum.getName() + "\"!");
			return null;
		}
		entry.drum.setSample(wave);
		
		attrstr = xml_element.getAttribute("Envelope");
		if(attrstr != null && !attrstr.isEmpty()){
			Z64Envelope env = env_map.get(attrstr);
			if(env != null){
				entry.drum.setEnvelope(env);
			}
			else{
				System.err.println("Could not find envelope by name \"" + attrstr + "\"" + " for Drum \"" + entry.drum.getName() + "\"!");
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
				entry.drum.setTuning(Z64Drum.localToCommonTuning(entry.range_min, wave.getTuning()));
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
			else entry.drum.setTuning(Z64Drum.localToCommonTuning(entry.range_min, wave.getTuning()));
		}
		
		return entry;
	}
	
	public static Z64WaveInfo readInstSampleElement(Element xml_element, Z64Instrument inst, int which_sample){
		String attrstr = xml_element.getAttribute("Sample");
		if(attrstr == null || attrstr.isEmpty()){
			attrstr = xml_element.getAttribute("SampleName");
		}
		if(attrstr == null || attrstr.isEmpty()){
			//No sample used here
			return null;
		}
		
		if(attrstr.endsWith(".aifc")) attrstr = attrstr.substring(0, attrstr.length() - 5);
		Z64WaveInfo wave = ZeqerCore.getActiveCore().getWaveByName(attrstr);
		if(wave == null) return null;
		
		//Set. Add note range
		int midival = -1;
		switch(which_sample){
		case INST_SAMPLE_MID:
			inst.setSampleMiddle(wave);
			break;
		case INST_SAMPLE_LO:
			inst.setSampleLow(wave);
			attrstr = xml_element.getAttribute("MaxNote");
			midival = readNote(attrstr);
			midival -= 21;
			if(midival < 0 || midival > 63) midival = 0;
			inst.setLowRangeTop((byte)midival);
			break;
		case INST_SAMPLE_HI:
			inst.setSampleHigh(wave);
			attrstr = xml_element.getAttribute("MinNote");
			midival = readNote(attrstr); midival -= 21;
			if(midival <= 0 || midival > 63) midival = 127;
			inst.setHighRangeBottom((byte)midival);
			break;
		}
		
		//Tuning
		if(xml_element.hasAttribute("Pitch")){
			attrstr = xml_element.getAttribute("Pitch");
			try{
				float tuning = Float.parseFloat(attrstr);
				switch(which_sample){
				case INST_SAMPLE_MID:
					inst.setTuningMiddle(tuning);
					break;
				case INST_SAMPLE_LO:
					inst.setTuningLow(tuning);
					break;
				case INST_SAMPLE_HI:
					inst.setTuningHigh(tuning);
					break;
				}
			}
			catch(NumberFormatException ex){
				System.err.println("Number Format Error: Tuning value \"" + attrstr + "\" for Instrument \"" + inst.getName() + "\" is invalid!");
			}
		}
		else{
			//Try finding root note/finetune
			float tuning = wave.getTuning();
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
				inst.setTuningMiddle(tuning);
				break;
			case INST_SAMPLE_LO:
				inst.setTuningLow(tuning);
				break;
			case INST_SAMPLE_HI:
				inst.setTuningHigh(tuning);
				break;
			}
		}
		
		return wave;
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
			entry.inst = ZeqerCore.getActiveCore().getPresetInstrumentByName(presetname);
			if(entry.inst == null){
				System.err.println("Could not find preset \"" + presetname + "\"!");
				return null;
			}
		}
		else{
			entry.inst = new Z64Instrument();	
			entry.inst.setIDRandom();
			
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
						readInstSampleElement(child, entry.inst, INST_SAMPLE_LO);
					}
					else if(child.getTagName().equals("MediumKey")){
						readInstSampleElement(child, entry.inst, INST_SAMPLE_MID);
					}
					else if(child.getTagName().equals("HighKey")){
						readInstSampleElement(child, entry.inst, INST_SAMPLE_HI);
					}
				}
			}
		}		
	
		return entry;
	}
	
	public static Z64Bank readSFXML(String path) throws ParserConfigurationException, SAXException, IOException{
		Document xml_doc = XMLReader.readXMLStatic(path);
		SoundfontXML sfxml = new SoundfontXML();
		sfxml.output = new Z64Bank();
		//sfxml.output.setSamplesOrderedByUID(true);
		
		//Top soundfont node
		NodeList nl = xml_doc.getElementsByTagName("Soundfont");
		if(nl == null || nl.getLength() < 0) return null;
		Node sf_node = nl.item(0);
		NamedNodeMap attrmap = sf_node.getAttributes();
		Node sf_attr = attrmap.getNamedItem("Medium");
		sfxml.updateMedium(sf_attr.getNodeValue());
		sf_attr = attrmap.getNamedItem("CachePolicy");
		sfxml.updateCache(sf_attr.getNodeValue());
		sfxml.output.setCachePolicy(sfxml.cache);
		sfxml.output.setMedium(sfxml.medium);
		
		//Envelopes
		nl = xml_doc.getElementsByTagName("Envelope");
		int ccount = nl.getLength();
		for(int i = 0; i < ccount; i++){
			Node n = nl.item(i);
			if(n.getNodeType() == Node.ELEMENT_NODE){
				Element child = (Element)n;
				Z64Envelope env = sfxml.readEnvelope(child);
				sfxml.env_map.put(env.getName(), env);
			}
		}
		
		//Sound effects
		nl = xml_doc.getElementsByTagName("SoundEffect");
		ccount = nl.getLength();
		int c = 0;
		for(int i = 0; i < ccount; i++){
			Node n = nl.item(i);
			if(n.getNodeType() == Node.ELEMENT_NODE){
				Element child = (Element)n;
				SFXEntry sfxe = sfxml.readSoundEffect(child);
				if(sfxe != null){
					if(sfxe.index < 0) sfxe.index = c++;
					//sfxml.output.setSoundEffect(sfxe.sfx, sfxe.index);
					sfxml.output.setSFX(sfxe.index, sfxe.sfx);
				}
			}
		}
		
		//Drums
		nl = xml_doc.getElementsByTagName("Drum");
		ccount = nl.getLength();
		c = 0;
		for(int i = 0; i < ccount; i++){
			Node n = nl.item(i);
			if(n.getNodeType() == Node.ELEMENT_NODE){
				Element child = (Element)n;
				DrumEntry drume = sfxml.readDrum(child);
				if(drume != null){
					if(drume.range_min < 0) drume.range_min = c++;
					sfxml.output.setDrum(drume.drum, drume.range_min, drume.range_max);
				}
			}
		}
		
		nl = xml_doc.getElementsByTagName("DrumRange");
		ccount = nl.getLength();
		c = 0;
		for(int i = 0; i < ccount; i++){
			Node n = nl.item(i);
			if(n.getNodeType() == Node.ELEMENT_NODE){
				Element child = (Element)n;
				DrumEntry drume = sfxml.readDrum(child);
				if(drume != null){
					if(drume.range_min < 0) drume.range_min = c++;
					sfxml.output.setDrum(drume.drum, drume.range_min, drume.range_max);
				}
			}
		}
		
		//Instruments
		nl = xml_doc.getElementsByTagName("Instrument");
		ccount = nl.getLength();
		c = 0;
		for(int i = 0; i < ccount; i++){
			Node n = nl.item(i);
			if(n.getNodeType() == Node.ELEMENT_NODE){
				Element child = (Element)n;
				InstEntry inste = sfxml.readInstrument(child);
				if(inste != null){
					if(inste.index < 0) inste.index = c++;
					inste.inst.setID(inste.index);
					//sfxml.output.setInstrument(inste.inst, inste.index);
					sfxml.output.setInstrument(inste.index, inste.inst);
				}
			}
		}
		
		return sfxml.output;
	}
	
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
	
}
