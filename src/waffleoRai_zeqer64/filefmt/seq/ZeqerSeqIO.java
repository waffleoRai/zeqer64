package waffleoRai_zeqer64.filefmt.seq;

import java.util.Set;

import waffleoRai_SeqSound.n64al.NUSALSeq;
import waffleoRai_zeqer64.ErrorCode;
import waffleoRai_zeqer64.filefmt.seq.SeqTableEntry;

public class ZeqerSeqIO {
	//TODO eventually have settings that allow user to specify which MIDI events are
	//	translated into which NUS seq events
	
	public static final int WARNING_NONE = 0;
	
	public static final int WARNING_FLAG_CHVOX = 1 << 0; //Too many voices per channel
	public static final int WARNING_FLAG_TOTVOX = 1 << 1; //Too many voices at once in sequence
	public static final int WARNING_FLAG_SIZE = 1 << 2; //Sequence bin is too large
	public static final int WARNING_FLAG_LOSTEVENTS = 1 << 3; //Original seq contains commands not translatable
	public static final int WARNING_FLAG_TEMPO = 1 << 4; //Tempo is too fast (tick < update)
	
	
	public static final int ERROR_NONE = 0;
	public static final int ERROR_INVALID_PARAMS = 1;
	public static final int ERROR_MUS_PARSE = 2;
	public static final int ERROR_IMPORT_FAIL = 3;
	
	public static class SeqImportOptions{
		public Set<String> tags;
		public int seqType;
		public boolean limitVoxPerChannel = true;
		public boolean limitNoteSpeed = true;
		
	}
	
	public static class SeqImportResults{
		public SeqImportOptions options;
		public int warningFlags;
		public ErrorCode error;
		
		public SeqTableEntry meta;
		public NUSALSeq seq;
	}
	
}
