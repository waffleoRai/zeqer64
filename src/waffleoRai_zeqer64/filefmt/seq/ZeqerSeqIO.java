package waffleoRai_zeqer64.filefmt.seq;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import javax.sound.midi.InvalidMidiDataException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import waffleoRai_SeqSound.MIDI;
import waffleoRai_SeqSound.MIDIInterpreter;
import waffleoRai_SeqSound.n64al.NUSALSeq;
import waffleoRai_SeqSound.n64al.NUSALSeqCommandBook;
import waffleoRai_SeqSound.n64al.cmd.SysCommandBook;
import waffleoRai_SeqSound.n64al.seqgen.NUSALSeqGenerator;
import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_zeqer64.ErrorCode;
import waffleoRai_zeqer64.ZeqerConstants;
import waffleoRai_zeqer64.ZeqerSeq;

public class ZeqerSeqIO {
	//TODO eventually have settings that allow user to specify which MIDI events are
	//	translated into which NUS seq events
	
	public static final int WARNING_NONE = 0;
	
	public static final int WARNING_FLAG_CHVOX = 1 << 0; //Too many voices per channel
	public static final int WARNING_FLAG_TOTVOX = 1 << 1; //Too many voices at once in sequence
	public static final int WARNING_FLAG_SIZE_TEMP = 1 << 2; //Sequence bin is too large for temp cache
	public static final int WARNING_FLAG_SIZE_PERSIST = 1 << 3; //Sequence bin is too large for persistant cache
	public static final int WARNING_FLAG_LOSTEVENTS = 1 << 4; //Original seq contains commands not translatable
	public static final int WARNING_FLAG_TEMPO = 1 << 5; //Tempo is too fast (tick < update)
	
	public static final int ERROR_NONE = 0;
	public static final int ERROR_INVALID_PARAMS = 1;
	public static final int ERROR_MUS_PARSE = 2;
	public static final int ERROR_IMPORT_FAIL = 3;
	public static final int ERROR_NULL_INPUT = 4;
	public static final int ERROR_NULL_OPTIONS = 5;
	public static final int ERROR_IMPORT_IO_FAIL = 6;
	public static final int ERROR_IMPORT_PARSE_FAIL = 7;
	public static final int ERROR_IMPORT_TYPE_NOT_RECOGNIZED = 8;
	
	public static class SeqImportOptions{
		public String name;
		public String eString;
		
		public Set<String> tags;
		public int seqType;
		public int cacheType;
		public boolean limitVoxPerChannel = true; //Not implemented yet
		public boolean limitNoteSpeed = true; //Not implemented yet
		public int maxTotalVox = 0; //If <= 0, then don't auto-limit.
		public boolean halveForTempoCap = false;
		public NUSALSeqCommandBook cmdBook = SysCommandBook.getDefaultBook();
		
		//User can specify before midi is read
		public boolean custom_timesig = false;
		public int timesig_beats = 4;
		public int timesig_div = 4;
		
		//Loop point
		public int loopMeasure = -1; //If negative, check if precalculated tick
		public double loopBeat = -1.0;
		public int loopTick = -1;
		
		public int bankUid = 0;
	}
	
	public static class SeqImportResults{
		public SeqImportOptions options;
		public int warningFlags;
		public ErrorCode error;

		public SeqTableEntry meta;
		public String filepath; //To derive default name
		public NUSALSeq seq;
		
		public byte[] voiceCountMap;
		
		public SeqImportResults() {
			error = new ErrorCode();
			error.value = ERROR_NONE;
		}
	}
	
	public static String getErrorCodeString(int code){
		switch(code){
		case ERROR_NONE: return "ERROR_NONE";
		case ERROR_INVALID_PARAMS: return "ERROR_INVALID_PARAMS";
		case ERROR_MUS_PARSE: return "ERROR_MUS_PARSE";
		case ERROR_IMPORT_FAIL: return "ERROR_IMPORT_FAIL";
		case ERROR_NULL_INPUT: return "ERROR_NULL_INPUT";
		case ERROR_NULL_OPTIONS: return "ERROR_NULL_OPTIONS";
		case ERROR_IMPORT_IO_FAIL: return "ERROR_IMPORT_IO_FAIL";
		case ERROR_IMPORT_PARSE_FAIL: return "ERROR_IMPORT_PARSE_FAIL";
		case ERROR_IMPORT_TYPE_NOT_RECOGNIZED: return "ERROR_IMPORT_TYPE_NOT_RECOGNIZED";
		}
		return null;
	}
	
	public static void updateImportMetadata(SeqImportResults res) {
		if(res == null) return;
		if(res.meta == null) return;
	
		res.meta.setCache((byte)res.options.cacheType);
		res.meta.setEnumString(res.options.eString);
		res.meta.setMedium((byte)Z64Sound.MEDIUM_CART);
		res.meta.setName(res.options.name);
		res.meta.setSeqType(res.options.seqType);
		res.meta.setSingleBank(res.options.bankUid);
		for(String tag : res.options.tags) {
			res.meta.addTag(tag);
		}
	}
	
	public static boolean exportMidi(NUSALSeq seq, String path) {
		if(seq == null) return false;
		MIDI mid;
		try {
			mid = seq.toMidi();
			mid.writeMIDI(path);
			return true;
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean exportMus(NUSALSeq seq, int syntaxType, String path) throws IOException {
		if(seq == null) return false;
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(path));
		seq.exportMMLScript(bw, true, syntaxType);
		bw.close();
		
		return true;
	}
	
	public static boolean exportRaw(NUSALSeq seq, String path) {
		if(seq == null) return false;
		
		FileBuffer dat = seq.getSerializedData();
		try {
			dat.writeFile(path);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public static SeqImportResults importSequence(String filepath, SeqImportOptions op) {
		SeqImportResults res = new SeqImportResults();
		if(filepath == null) {
			res.error.value = ERROR_NULL_INPUT;
			return res;
		}
		
		String fplower = filepath.toLowerCase();
		if(fplower.endsWith(".mid") || fplower.endsWith(".midi")) {
			return importMidi(filepath, op);
		}
		else if(fplower.endsWith(".mus") || fplower.endsWith(".mml")) {
			return importMus(filepath, op);
		}
		
		res.error.value = ERROR_IMPORT_TYPE_NOT_RECOGNIZED;
		return res;
	}
	
	public static SeqImportResults importMidi(String filepath, SeqImportOptions op) {
		SeqImportResults res = new SeqImportResults();
		if(filepath == null) {
			res.error.value = ERROR_NULL_INPUT;
			return res;
		}
		if(op == null) {
			res.error.value = ERROR_NULL_OPTIONS;
			return res;
		}
		
		res.options = op;
		res.filepath = filepath;
		
		try {
			//Load MIDI
			FileBuffer buffer = FileBuffer.createBuffer(filepath, true);
			MIDI mid = new MIDI(buffer);
			MIDIInterpreter midReader = new MIDIInterpreter(mid.getSequence());
		
			//Create generator and set options
			NUSALSeqGenerator gen = new NUSALSeqGenerator(op.cmdBook);
			if(op.maxTotalVox <= 0) {
				gen.setMaxTotalVox(17);
				gen.setAutoCapVox(false);
			}
			else {
				gen.setMaxTotalVox(op.maxTotalVox);
				gen.setAutoCapVox(true);
			}
			gen.setTempoCapHalve(op.halveForTempoCap);
			//TODO Add per-channel voice limit lift ability
		
			//Run generation
			//Set timebase and loop
			int inRes = mid.getTPQN();
			gen.setTimebase(inRes);
			if(op.loopMeasure >= 0) {
				int loopTick = (int)Math.round(NUSALSeq.music2TickCoord(op.loopMeasure, op.loopBeat, op.timesig_beats, op.timesig_div, inRes));
				gen.setLoop(loopTick, -1);
			}
			else if(op.loopTick >= 0){
				gen.setLoop(op.loopTick, -1);
			}
			midReader.readMIDITo(gen);

			//Note
			gen.complete();
			NUSALSeq nseq = gen.getOutput();
			if(op.custom_timesig) {
				nseq.setBeatsPerMeasure(op.timesig_beats);
				nseq.setBeatDiv(op.timesig_div);
			}
			res.seq = nseq;
			res.voiceCountMap = gen.getVoicesAtTickTable();
			
			//Check new bin size
			int binSize = nseq.getMinSizeInBytes();
			if(binSize > 0x7f0) res.warningFlags |= WARNING_FLAG_SIZE_PERSIST;
			if(binSize > 0x3800) res.warningFlags |= WARNING_FLAG_SIZE_TEMP;
			
			//Check other errors and warnings
			if(gen.getChannelVoxOverFlag()) res.warningFlags |= WARNING_FLAG_CHVOX;
			if(gen.getVoxOverFlag()) res.warningFlags |= WARNING_FLAG_TOTVOX;
			if(gen.getLostControllerEventFlag()) res.warningFlags |= WARNING_FLAG_LOSTEVENTS;
			if(gen.getTempoCapFlag()) res.warningFlags |= WARNING_FLAG_TEMPO;
		}
		catch(IOException ex) {
			ex.printStackTrace();
			res.error.value = ERROR_IMPORT_IO_FAIL;
		} 
		catch (UnsupportedFileTypeException ex) {
			ex.printStackTrace();
			res.error.value = ERROR_IMPORT_PARSE_FAIL;
		}
		
		return res;
	}
	
	public static SeqImportResults importMus(String filepath, SeqImportOptions op) {
		SeqImportResults res = new SeqImportResults();
		if(filepath == null) {
			res.error.value = ERROR_NULL_INPUT;
			return res;
		}
		if(op == null) {
			res.error.value = ERROR_NULL_OPTIONS;
			return res;
		}
		
		res.options = op;
		res.filepath = filepath;
		
		try {
			//TODO
			BufferedReader br = new BufferedReader(new FileReader(filepath));
			res.seq = NUSALSeq.readMMLScript(null);
			//Maybe add warning flags and such to the MML parsing
			br.close();
		}
		catch(IOException ex) {
			ex.printStackTrace();
			res.error.value = ERROR_IMPORT_IO_FAIL;
		} 
		catch (UnsupportedFileTypeException ex) {
			ex.printStackTrace();
			res.error.value = ERROR_MUS_PARSE;
		}
		
		return res;
	}
	
	public static void addImportableFileFilters(JFileChooser fc){
		fc.addChoosableFileFilter(new FileFilter(){
			public boolean accept(File f) {
				if(f.isDirectory()) return true;
				String path = f.getAbsolutePath().toString().toLowerCase();
				return path.endsWith(".mid") || path.endsWith(".midi");
			}

			public String getDescription() {
				return "MIDI Sequence (.mid, .midi)";
			}});
		
		fc.addChoosableFileFilter(new FileFilter(){
			public boolean accept(File f) {
				if(f.isDirectory()) return true;
				String path = f.getAbsolutePath().toString().toLowerCase();
				return path.endsWith(".mus") || path.endsWith(".mml");
			}

			public String getDescription() {
				return "N64 Music Macro Language script (.mus, .mml)";
			}});
	}
	
}
