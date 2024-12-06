package waffleoRai_zeqer64.GUI.seqedit.mmlview;

import java.awt.Color;

import javax.swing.text.AttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import waffleoRai_SeqSound.n64al.NUSALSeqCmdType;

public class MMLScriptStyleRules {
	
	public static final Color GREY_GREEN = new Color(55, 89, 55);
	public static final Color DARK_PURPLE = new Color(94, 35, 92);
	public static final Color DARK_BLUE = new Color(53, 41, 186);
	public static final Color DARK_RED = new Color(176, 23, 23);
	public static final Color DIRT_BROWN = new Color(84, 62, 36);
	public static final Color GREEN_1 = new Color(24, 186, 24);
	public static final Color DARK_CYAN = new Color(19, 171, 102);
	public static final Color ORANGE_1 = new Color(222, 148, 20);
	public static final Color ORANGE_2 = new Color(163, 143, 41);
	public static final Color GREY_1 = new Color(79, 79, 79);
	public static final Color GREY_2 = new Color(128, 128, 128);
	
	private Style baseStyle;
	
	private Style commentStyle;
	private Style labelStyle;
	private Style commandStyle; //Command common
	private Style dataStyle;
	
	private Style flowCmdStyle; //Call, jump, end etc.
	private Style mathCmdStyle; //sub, subio etc.
	private Style regCmdStyle; //ldi, stio etc.
	private Style openChLyCmdStyle;
	private Style testCmdStyle;
	private Style selfModCmdStyle; //sts, loadseq etc.
	private Style artCmdStyle; //Non note/wait articulation. Pan, tempo, vol, etc.
	private Style unkCmdStyle;
	
	public MMLScriptStyleRules(StyledDocument source, int fontSize){
		baseStyle = source.addStyle("Base", null);
		StyleConstants.setFontSize(baseStyle, fontSize);
		StyleConstants.setForeground(baseStyle, Color.black);
		StyleConstants.setFontFamily(baseStyle, "Courier New");
		StyleConstants.setAlignment(baseStyle, StyleConstants.ALIGN_LEFT);
		StyleConstants.setBold(baseStyle, false);
		StyleConstants.setItalic(baseStyle, false);
		
		//(Default) Modifications for each...
		commentStyle = source.addStyle("Comment", baseStyle);
		StyleConstants.setForeground(commentStyle, GREY_GREEN);
	
		labelStyle = source.addStyle("Label", baseStyle);
		StyleConstants.setForeground(labelStyle, DARK_PURPLE);
		StyleConstants.setItalic(labelStyle, true);
		
		commandStyle = source.addStyle("CommandCommon", baseStyle);
		StyleConstants.setBold(commandStyle, true);
		
		dataStyle = source.addStyle("Data", baseStyle);
		StyleConstants.setForeground(dataStyle, GREY_2);
		
		artCmdStyle = source.addStyle("ArticulationCommand", commandStyle);
		StyleConstants.setForeground(artCmdStyle, DARK_BLUE);
		
		selfModCmdStyle = source.addStyle("SelfModCommand", commandStyle);
		StyleConstants.setForeground(selfModCmdStyle, DARK_RED);
		
		testCmdStyle = source.addStyle("TestCommand", commandStyle);
		StyleConstants.setForeground(testCmdStyle, DIRT_BROWN);
		
		flowCmdStyle = source.addStyle("FlowCommand", commandStyle);
		StyleConstants.setForeground(flowCmdStyle, GREEN_1);
		
		openChLyCmdStyle = source.addStyle("OpenTrack", commandStyle);
		StyleConstants.setForeground(openChLyCmdStyle, DARK_CYAN);
		
		regCmdStyle = source.addStyle("RegCommand", commandStyle);
		StyleConstants.setForeground(regCmdStyle, ORANGE_1);
		
		mathCmdStyle = source.addStyle("MathCommand", commandStyle);
		StyleConstants.setForeground(mathCmdStyle, ORANGE_2);
		
		unkCmdStyle = source.addStyle("UnkCommand", commandStyle);
		StyleConstants.setForeground(unkCmdStyle, GREY_1);
	}
	
	public AttributeSet getBaseStyle(){
		return baseStyle;
	}

	public AttributeSet getCommentStyle(){
		return commentStyle;
	}
	
	public AttributeSet getLabelStyle(){
		return labelStyle;
	}
	
	public AttributeSet getDataStyle(){
		return dataStyle;
	}
	
	public AttributeSet getStyleForCommand(NUSALSeqCmdType cmd){
		//TODO
		if(cmd == null) return commandStyle;
		
		switch(cmd){
		//Value manipulation
		case ADD_IMM_P:
		case ADD_RAND_IMM_P:
		case AND_IMM_S:
		case AND_IMM_C:
		case RANDP:
		case RAND_C:
		case RAND_S:
		case SUBTRACT_IMM_S:
		case SUBTRACT_IMM_C:
		case SUBTRACT_IO_S:
		case SUBTRACT_IO_C:
			return mathCmdStyle;
			
		//Logic Flow
		case BRANCH_ALWAYS:
		case BRANCH_ALWAYS_REL:
		case BRANCH_IF_EQZ:
		case BRANCH_IF_EQZ_REL:
		case BRANCH_IF_GTEZ:
		case BRANCH_IF_LTZ:
		case BRANCH_IF_LTZ_REL:
		case BREAK:
		case CALL:
		case CALL_DYNTABLE:
		case CALL_TABLE:
		case CH_HALT:
		case END_READ:
		case LOOP_END:
		case LOOP_START:
		case YIELD:
			return flowCmdStyle;
			
		//Open track
		case CHANNEL_OFFSET:
		case CHANNEL_OFFSET_C:
		case CHANNEL_OFFSET_REL:
		case VOICE_OFFSET:
		case VOICE_OFFSET_REL:
		case VOICE_OFFSET_TABLE:
			return openChLyCmdStyle;
			
		//Music
		case CH_DELTA_TIME:
		case PLAY_NOTE_NTV:
		case PLAY_NOTE_NTVG:
		case PLAY_NOTE_NVG:
		case REST:
		case WAIT:
			return commandStyle;
			
		//Data based articulation
		case CH_ENVELOPE:
		case CH_FILTER_GAIN:
		case CH_REVERB_IDX:
		case CLEAR_CH_FILTER:
		case COPY_CH_FILTER:
		case L_ENVELOPE:
		case SET_CH_FILTER:
			return artCmdStyle;
			
		//General articulation
		case CH_EXP:
		case CH_FREQSCALE:
		case CH_LOAD_PARAMS:
		case CH_PAN:
		case CH_PANMIX:
		case CH_PITCHBEND:
		case CH_PITCHBEND_ALT:
		case CH_RELEASE:
		case CH_REVERB:
		case CH_SAMPLE_VARIATION:
		case CH_SET_PARAMS:
		case CH_STEREO_EFF:
		case CH_SUSTAIN:
		case CH_TRANSPOSE:
		case CH_VIBRATO_DELAY:
		case CH_VIBRATO_DEPTH:
		case CH_VIBRATO_DEPTHENV:
		case CH_VIBRATO_FREQ:
		case CH_VIBRATO_FREQENV:
		case CH_VOLUME:
		case CHORUS:
		case GATERAND:
		case LEGATO_OFF:
		case LEGATO_ON:
		case L_PAN:
		case L_PITCHBEND_ALT:
		case L_RELEASE:
		case L_REVERB_PHASE:
		case L_SET_PROGRAM:
		case L_TRANSPOSE:
		case MASTER_EXP:
		case MASTER_FADE:
		case MASTER_VOLUME:
		case MUTE_S:
		case PORTAMENTO_OFF:
		case PORTAMENTO_ON:
		case SEQ_TRANSPOSE:
		case SEQ_TRANSPOSE_REL:
		case SET_BANK:
		case SET_BANK_AND_PROGRAM:
		case SET_PROGRAM:
		case SET_TEMPO:
		case SET_TEMPO_VAR:
		case VELRAND:
			return artCmdStyle;

		//Meta / Playback Control
		case CH_PRIORITY:
		case CH_NOTEALLOC_POLICY:
		case CH_RESET:
		case DISABLE_CHANNELS:
		case DRUMPAN_OFF:
		case ENABLE_CHANNELS:
		case L_SHORTGATE:
		case L_SHORTNOTE:
		case L_SHORTTIME:
		case L_SHORTVEL:
		case MUTE_BEHAVIOR_C:
		case MUTE_BEHAVIOR_S:
		case MUTE_SCALE_S:
		case NOTEALLOC_POLICY_S:
		case RESERVE_NOTES:
		case SHORTNOTE_OFF:
		case SHORTNOTE_ON:
		case STOP_CHANNEL:
		case STOP_CHANNEL_C:
		case STOP_VOICE:
		case TEST_CHANNEL:
		case TEST_VOICE:
		case UNRESERVE_NOTES:
			return testCmdStyle;
			
		//Unknown
		case C_UNK_C0:
		case S_RUNSEQ:
		case S_SCRIPTCTR:
		case S_STOP:
			return unkCmdStyle;
		
		//Data
		case DATA_ONLY:
			return dataStyle;
			
		//Self modifying
		case LOAD_BANK:
		case LOAD_FROM_SELF:
		case LOAD_SAMPLE:
		case LOAD_SAMPLE_P:
		case LOAD_SEQ:
		case STORE_TO_SELF_S:
		case STORE_TO_SELF_C:
		case STORE_TO_SELF_P:
			return selfModCmdStyle;
			
		//Load/store general
		case LOAD_CHIO:
		case LOAD_IMM_S:
		case LOAD_IMM_C:
		case LOAD_IMM_P:
		case LOAD_IO_C:
		case LOAD_IO_S:
		case LOAD_P_TABLE:
		case STORE_CHIO:
		case STORE_IO_S:
		case STORE_IO_C:
			return regCmdStyle;
			
		case DYNTABLE_LOAD:
		case DYNTABLE_READ:
		case DYNTABLE_WRITE:
		case LOAD_SHORTTBL_GATE:
		case LOAD_SHORTTBL_VEL:
		case MULTI_EVENT_CHUNK:
		case PRINT:
		case PSEUDO_MIDILIKE_NOTE:
		case SET_DYNTABLE:
		case SHIFT_DYNTABLE:
		case SHORTTBL_GATE:
		case SHORTTBL_VEL:
		case UNRESOLVED_LINK:
		default:
			return commandStyle;
		
		}
	}
	
}
