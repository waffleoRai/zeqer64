package waffleoRai_zeqer64.GUI.seqDisplay;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

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
	
	public NoteNameIconFactory(){
		letters = new int[12];
		accidental = new int[12];
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
			
			if(min < 12) min += 12;
			minors[min] = i;
			min-=7;
		}
		
	}
	
	public int getCurrentKey(){return current_key;}
	public boolean getCurrentMode(){return current_mode;}
	
	public void setDenoteOctave(boolean b){denote_octave = b;}
	
	public void setKey(int key, boolean mode){
		//Mode - true is Major, false is minor
		//We'll use -5 to 6 for accidentals
		//For C major, use C#, Eb, F#, Ab, Bb
		//For A minor, use all sharps
		
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
		sb.append('A' + letteridx);
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
	
	public BufferedImage getNoteIcon(byte midi_val){
		int note_i = (int)midi_val;
		if(note_i < 0) return null;
		
		int semi = note_i%12;
		int octave = (note_i/12) - 1; 
		int alloc_h = letter_height;
		int alloc_w = 0;
		
		//Letter - buff - accidental (if appl, shrunk to 1/4) - buff - number
		//Pull necessary images
		BufferedImage i_letter, i_acc, i_number, i_neg;
		Image s_acc = null, s_number = null, s_neg = null;
		i_letter = IconFactory.getNoteLetterIcon(letters[semi]);
		i_number = i_acc = i_neg = null;
		switch(accidental[semi]){
		case -1: i_acc = IconFactory.getMappedIcon("flat"); break;
		case 1: i_acc = IconFactory.getMappedIcon("sharp"); break;
		case 2: i_acc = IconFactory.getMappedIcon("natural"); break;
		}
		if(denote_octave){
			if(octave < 0){
				i_number = IconFactory.getDigitIcon(1);
				i_neg = IconFactory.getMappedIcon("minus");
			}
			else{i_number = IconFactory.getDigitIcon(octave);} //Octaves go -1 to 9
		}
		
		int[] xpos = new int[4];
		int[] ypos = new int[4];
		//Scale component images
		alloc_w += i_letter.getWidth();
		if(i_acc != null){
			//Accidental in use
			alloc_w += PIX_BUFFER; //Buffer
			xpos[1] = alloc_w;
			
			//Move y down so acc is elevated a bit.
			int elev = i_letter.getHeight() >>> 3; //1/8th the height of the letter
			alloc_h += elev;
			ypos[0] = ypos[2] = ypos[3] = elev;
			
			//Determine scaling and scale
			int a_height = i_letter.getHeight() >>> 1; //Half
			double aratio = (double)i_acc.getWidth()/i_acc.getHeight();
			int a_width = (int)Math.round(aratio * (double)a_height);
			s_acc = i_acc.getScaledInstance(a_width, a_height, Image.SCALE_DEFAULT);
		}
		
		//Number
		if(denote_octave){
			alloc_w += PIX_BUFFER; //Buffer
			int elev = i_letter.getHeight() >>> 3;
			
			if(i_neg != null){
				xpos[2] = alloc_w;
				ypos[2] += elev*5; //Bump back up when know image size.
				
				double aratio = (double)i_neg.getWidth()/i_neg.getHeight();
				int a_height = i_letter.getHeight()/6;
				int a_width = (int)Math.round(aratio * (double)a_height);
				s_neg = i_neg.getScaledInstance(a_width, a_height, Image.SCALE_DEFAULT);
				alloc_w += a_width;
				
				ypos[2] -= a_height/2;
				alloc_w += PIX_BUFFER;
			}
			
			int q = i_letter.getHeight() >>> 2;
			xpos[3] = alloc_w;
			ypos[3] += q;
			
			double aratio = (double)i_number.getWidth()/i_number.getHeight();
			int a_height = q * 3;
			int a_width = (int)Math.round(aratio * (double)a_height);
			s_number = i_number.getScaledInstance(a_width, a_height, Image.SCALE_DEFAULT);
			alloc_w += a_width;
		}
		
		//Now paint to output.
		BufferedImage output = new BufferedImage(alloc_w, alloc_h, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = output.createGraphics();
		
		g.drawImage(i_letter, xpos[0], ypos[0], null);
		if(s_acc != null) g.drawImage(s_acc, xpos[1], ypos[1], null);
		if(s_neg != null) g.drawImage(s_neg, xpos[2], ypos[2], null);
		if(s_number != null) g.drawImage(s_number, xpos[3], ypos[3], null);
		
		return output;
	}

	private void dispose(){
		letters = null;
		accidental = null;
		majors = null;
		minors = null;
	}
	
	public static void setKeyStatic(int key, boolean mode){
		if(static_factory == null) static_factory = new NoteNameIconFactory();
		static_factory.setKey(key, mode);
	}
	
	public static String getNoteAsString(byte midi_val, boolean allow_unicode){
		if(static_factory == null) static_factory = new NoteNameIconFactory();
		return static_factory.noteToString(midi_val, allow_unicode);
	}
	
	public static BufferedImage getNoteAsImage(byte midi_val){
		if(static_factory == null) static_factory = new NoteNameIconFactory();
		return static_factory.getNoteIcon(midi_val);
	}
	
	public static void disposeStatic(){
		if(static_factory == null) return;
		static_factory.dispose();
		static_factory = null;
	}
	
}
