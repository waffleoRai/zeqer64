package waffleoRai_zeqer64.GUI.seqDisplay;

import java.awt.image.BufferedImage;

import waffleoRai_zeqer64.GUI.ScaledImage;

class NoteNameIconFactory {
	
	public static final int PIX_BUFFER = 2;
	
	public static final int LETTER_A = 0;
	public static final int LETTER_B = 1;
	public static final int LETTER_C = 2;
	public static final int LETTER_D = 3;
	public static final int LETTER_E = 4;
	public static final int LETTER_F = 5;
	public static final int LETTER_G = 6;
	
	public static final int NOTE_C = 0;
	public static final int NOTE_CSHARP = 1;
	public static final int NOTE_D = 2;
	public static final int NOTE_EFLAT = 3;
	public static final int NOTE_E = 4;
	public static final int NOTE_F = 5;
	public static final int NOTE_FSHARP = 6;
	public static final int NOTE_G = 7;
	public static final int NOTE_AFLAT = 8;
	public static final int NOTE_A = 9;
	public static final int NOTE_BFLAT = 10;
	public static final int NOTE_B = 11;
	
	public static final char CHAR_SHARP = '♯';
	public static final char CHAR_FLAT = '♭';
	public static final char CHAR_NATURAL = '♮';
	
	private static final int[] NOTE_LETTER_IDXS = {LETTER_C, -1, LETTER_D, -1,
									               LETTER_E, LETTER_F, -1, LETTER_G,
									               -1, LETTER_A, -1, LETTER_B};
	
	private static final int[] SHARPS = {NOTE_FSHARP, NOTE_CSHARP, NOTE_AFLAT, NOTE_EFLAT, NOTE_BFLAT, NOTE_F};
	private static final int[] FLATS = {NOTE_BFLAT, NOTE_EFLAT, NOTE_AFLAT, NOTE_CSHARP, NOTE_FSHARP};
	
	private static NoteNameIconFactory static_factory;
	
	private boolean denote_octave = true; //Whether or not to include octave # at end
	
	private int[] letters;
	private int[] accidental; //-1 is flat, 0 is none, 1 is sharp, 2 is natural
	
	private int current_key;
	private boolean current_mode;
	
	private int[] majors; //Pregenerated tables
	private int[] minors; 
	
	private int letter_height;
	
	private int last_height;
	private FactoryIcon[] cache;
	
 	public NoteNameIconFactory(){
		letters = new int[12];
		accidental = new int[12];
		cache = new FactoryIcon[128];
		generateTables();
		setKey(NOTE_C, true);
		
		for(int i = 0; i < 7; i++){
			BufferedImage letter = IconFactory.getNoteLetterIcon(i);
			if(letter.getHeight() > letter_height){
				letter_height = letter.getHeight();
			}
		}
	}
	
	private void generateTables(){
		majors = new int[12];
		minors = new int[12];
		
		int maj_ctr = NOTE_C;
		int min_ctr = NOTE_A;
		
		majors[maj_ctr] = 0;
		minors[min_ctr] = 0;
		
		int maj = maj_ctr + 7;
		int min = min_ctr + 7;
		
		for(int i = 0; i < 7; i++){
			//Sharps
			if(maj > 12) maj -= 12;
			majors[maj] = i;
			maj+=7;
			
			if(min > 12) min -= 12;
			minors[min] = i;
			min+=7;
		}
		
		maj = maj_ctr - 7;
		min = min_ctr - 7;
		for(int i = -1; i > -6; i--){
			//Flats
			if(maj < 0) maj += 12;
			majors[maj] = i;
			maj -= 7;
			
			if(min < 0) min += 12;
			minors[min] = i;
			min-=7;
		}
		
	}
	
	public int getCurrentKey(){return current_key;}
	public boolean getCurrentMode(){return current_mode;}
	
	public void setDenoteOctave(boolean b){denote_octave = b; clearCache();}
	
	public void setKey(int key, boolean mode){
		//Mode - true is Major, false is minor
		//We'll use -5 to 6 for accidentals
		//For C major, use C#, Eb, F#, Ab, Bb
		//For A minor, use all sharps
		
		clearCache();
		
		key = key%12;
		current_key = key;
		current_mode = mode;
		
		int aval = 0;
		if(mode){
			//Major
			aval = majors[key];
		}
		else{
			aval = minors[key];
		}
		
		if(aval == 0){
			for(int i = 0; i < 12; i++){
				if(NOTE_LETTER_IDXS[i] >= 0){
					//White key
					letters[i] = NOTE_LETTER_IDXS[i];
					accidental[i] = 0;
				}
				else{
					//Black key.
					if(mode && (i == NOTE_EFLAT || i == NOTE_AFLAT || i == NOTE_BFLAT)){
						//Make it a flat
						letters[i] = NOTE_LETTER_IDXS[i+1];
						accidental[i] = -1;
					}
					else{
						//Make it a sharp
						letters[i] = NOTE_LETTER_IDXS[i-1];
						accidental[i] = 1;
					}
				}
			}
		}
		else if(aval > 0){
			//Sharps
			//Clear previous labels.
			for(int i = 0; i < 12; i++) {letters[i] = -1; accidental[i] = -2;}
			//Label known sharps
			for(int i = 0; i < aval; i++){
				int s = SHARPS[i];
				letters[s] = NOTE_LETTER_IDXS[s-1];
				accidental[s] = 1;
			}
			//Label the rest. White keys one semitone down from a previously labeled sharp add natural
			for(int i = 0; i < 12; i++){
				if(letters[i] >= 0) continue; //Already labeled
				if(NOTE_LETTER_IDXS[i] >= 0){
					letters[i] = NOTE_LETTER_IDXS[i];
					int up = i+1;
					if(up >= 12) up -= 12;
					if(accidental[up] == 1) accidental[i] = 2;
					else accidental[i] = 0;
				}
				else{
					letters[i] = NOTE_LETTER_IDXS[i-1];
					accidental[i] = 1;
				}
			}
		}
		else if(aval < 0){
			//Flats
			for(int i = 0; i < 12; i++) {letters[i] = -1; accidental[i] = -2;}
			aval = Math.abs(aval);
			for(int i = 0; i < aval; i++){
				int s = FLATS[i];
				letters[s] = NOTE_LETTER_IDXS[s+1];
				accidental[s] = -1;
			}
			for(int i = 0; i < 12; i++){
				if(letters[i] >= 0) continue; //Already labeled
				if(NOTE_LETTER_IDXS[i] >= 0){
					letters[i] = NOTE_LETTER_IDXS[i];
					int down = i-1;
					if(down < 0) down += 12;
					if(accidental[down] == -1) accidental[i] = 2;
					else accidental[i] = 0;
				}
				else{
					letters[i] = NOTE_LETTER_IDXS[i+1];
					accidental[i] = -1;
				}
			}
		}
		
	}
	
	public String noteToString(byte midi_val, boolean allow_unicode){
		int note_i = (int)midi_val;
		if(note_i < 0) return null;
		
		int semi = note_i%12;
		int octave = (note_i/12)-1;
		StringBuilder sb = new StringBuilder(4);
		int letteridx = letters[semi];
		if(letteridx < 0) return null;
		sb.append((char)('A' + letteridx));
		switch(accidental[semi]){
		case -1:
			if(allow_unicode) sb.append(CHAR_FLAT);
			else sb.append('b');
			break;
		case 1:
			if(allow_unicode) sb.append(CHAR_SHARP);
			else sb.append('#');
			break;
		case 2:
			if(allow_unicode) sb.append(CHAR_NATURAL);
			else sb.append('*');
			break;
		}
		
		if(denote_octave){
			sb.append(octave);
		}
		
		return sb.toString();
	}
	
	public FactoryIcon getNoteIcon(byte midi_val, int height){
		if(height != last_height){clearCache(); last_height = height;}
		
		int note_i = (int)midi_val;
		if(note_i < 0) return null;
		if(cache[note_i] != null) return cache[note_i];
		
		int semi = note_i%12;
		int octave = (note_i/12) - 1; 
		int x = 0;
		FactoryIcon output = new FactoryIcon();
		
		//Letter
		float height_f = (float)height;
		int h_4 = Math.round((float)height_f/4.0f);
		int h_6 = h_4 * 3;
		ScaledImage img_letter = IconFactory.getScaledNoteLetterIcon(letters[semi], height-1);
		output.addPiece(img_letter, x, 1);
		x += img_letter.getWidth() + 2;
		
		//Accidental
		//int h_2 = height >>> 1;
		int acc = accidental[semi];
		ScaledImage img_acc = null;
		switch(acc){
		case -1:
			img_acc = IconFactory.getScaledMappedIcon("flat", h_6);
			break;
		case 1:
			img_acc = IconFactory.getScaledMappedIcon("sharp", h_6);
			break;
		case 2:
			img_acc = IconFactory.getScaledMappedIcon("natural", h_6);
			break;
		}
		if(img_acc != null){
			output.addPiece(img_acc, x, 0);
			x += img_acc.getWidth() + 2;
		}
		
		if(denote_octave){
			//Negative sign
			if(octave < 0){
				ScaledImage img_minus = IconFactory.getScaledMappedIcon("minus", h_6);
				output.addPiece(img_minus, x, height - h_6);
				x += img_minus.getWidth() + 2;
			}
			
			//Octave number	
			if(octave > 9){
				//Two digits
				ScaledImage img_num = IconFactory.getScaledDigitIcon(1, h_6);
				output.addPiece(img_num, x, height - h_6);
				x += img_num.getWidth() + 2;
				img_num = IconFactory.getScaledNoteLetterIcon(octave, h_6);
				output.addPiece(img_num, x, height - h_6);
			}
			else{
				ScaledImage img_num = IconFactory.getScaledDigitIcon(octave, h_6);
				output.addPiece(img_num, x, height - h_6);
			}
			
		}
		
		cache[note_i] = output;
		return output;
	}
	
	public void clearCache(){
		for(int i = 0; i < 128; i++) cache[i] = null;
	}

	private void dispose(){
		clearCache();
		cache = null;
		letters = null;
		accidental = null;
		majors = null;
		minors = null;
	}
	
	public static void setDenoteOctaveStatic(boolean b){
		if(static_factory == null) static_factory = new NoteNameIconFactory();
		static_factory.setDenoteOctave(b);
	}
	
	public static void setKeyStatic(int key, boolean mode){
		if(static_factory == null) static_factory = new NoteNameIconFactory();
		static_factory.setKey(key, mode);
	}
	
	public static String getNoteAsString(byte midi_val, boolean allow_unicode){
		if(static_factory == null) static_factory = new NoteNameIconFactory();
		return static_factory.noteToString(midi_val, allow_unicode);
	}
	
	public static FactoryIcon getNoteAsImage(byte midi_val, int height){
		if(static_factory == null) static_factory = new NoteNameIconFactory();
		return static_factory.getNoteIcon(midi_val, height);
	}
	
	public static void disposeStatic(){
		if(static_factory == null) return;
		static_factory.dispose();
		static_factory = null;
	}
	
}
