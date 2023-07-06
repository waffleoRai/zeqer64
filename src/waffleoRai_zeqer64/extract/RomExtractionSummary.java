package waffleoRai_zeqer64.extract;

import java.util.List;

public class RomExtractionSummary {
	
	/*----- Constants -----*/
	
	public static final int ITEM_TYPE_UNK = -1;
	public static final int ITEM_TYPE_WAV = 1;
	public static final int ITEM_TYPE_PRS = 2;
	public static final int ITEM_TYPE_BNK = 3;
	public static final int ITEM_TYPE_SEQ = 4;
	
	public static final int ERROR_NONE = 0;
	public static final int ERROR_UNKNOWN = -1;
	public static final int ERROR_PARSE_FAILED = 1;
	public static final int ERROR_DATA_NOT_FOUND = 2;
	public static final int ERROR_WRITE_FAILED = 3;
	public static final int ERROR_NOT_IN_TABLE = 4; //From user mode, cannot match from rom to sys table
	
	public static final int GENERR_NONE = 0;
	public static final int GENERR_INVALID_ROM = 1;
	public static final int GENERR_WAV_DUMP_FAILED = 2;
	public static final int GENERR_BNK_DUMP_FAILED = 3;
	public static final int GENERR_SEQ_DUMP_FAILED = 4;
	public static final int GENERR_SEQBNKMAP_FAILED = 5;
	public static final int GENERR_ABLD_SAVE_FAILED = 6;
	
	/*----- Inner Structs -----*/
	
	public static class ExtractionError{
		public int itemUID = 0;
		public int itemType = ITEM_TYPE_UNK;
		public int index0 = -1; //Index of seq, font, or samplebank
		public int index1 = -1; //Index of sample within samplebank
		public int reason = ERROR_UNKNOWN;
	}
	
	/*----- Instance Variables -----*/
	
	public String romid = null;
	public int genError = GENERR_NONE;
	
	//Wav
	public int sampleBanksFound = -1;
	public int samplesFound = -1;
	public int samplesOkay = -1;
	public int samplesNew = -1;
	public int samplesAddedAsCustom = -1;
	public List<ExtractionError> sampleErrors;
	
	//Bnk
	public int soundfontsFound = -1;
	public int soundfontsOkay = -1;
	public int soundfontsNew = -1;
	public int soundfontsAddedAsCustom = -1;
	public List<ExtractionError> soundfontErrors;
	
	//Prs
	public int ipresetsNew = -1;
	public int ipresetsAddedAsCustom = -1;
	public int ppresetsNew = -1;
	public int ppresetsAddedAsCustom = -1;
	public int dpresetsNew = -1;
	public int dpresetsAddedAsCustom = -1;
	
	//Seq
	public int seqsFound = -1;
	public int seqsOkay = -1;
	public int seqsNew = -1;
	public int seqsAddedAsCustom = -1;
	public List<ExtractionError> seqErrors;
	
	
	/*----- Init -----*/
	

}
