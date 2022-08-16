package waffleoRai_zeqer64.cmml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import waffleoRai_SeqSound.n64al.NUSALSeqDataType;
import waffleoRai_SeqSound.n64al.cmd.NUSALSeqReader;

public class CMMLPreprocessor {

	/*----- Constants -----*/
	
	public static final String MACRONAME_MMLCONTEXT = "mmlcontext";
	public static final String MACRONAME_IOALIAS = "ioalias";
	public static final String MACRONAME_MMLDATTYPE = "mmldattype";
	
	public static final int FLAG_SEQIO_ALIAS = 0x80000000;
	public static final int FLAG_CHIO_ALIAS  = 0x40000000;
	public static final int FLAG_OCHIO_ALIAS = 0x20000000;
	
	private static final int READMODE_NONE = -1;
	private static final int READMODE_MACRO = 1;
	private static final int READMODE_CODE = 2;
	private static final int READMODE_ONELINECMT = 3;
	private static final int READMODE_MULTILINECMT = 4;
	
	public static final int FILEMODE_MODULE_CODE = 0;
	public static final int FILEMODE_MODULE_HEADER = 1;
	public static final int FILEMODE_INCLUDED_HEADER = 2;
	
	/*----- Instance Variables -----*/
	
	private int file_mode = FILEMODE_MODULE_CODE;
	
	private LinkedList<String> include_queue;
	private Map<String, CMMLDefine> defines;
	private Map<String, Integer> io_alias; //If ochio, second byte is the channel index
	
	private Stack<ParserState> state_stack;
	private StringBuilder textbuffer;
	private int readmode = READMODE_NONE;
	private int line_number = 1;
	private boolean macromode_nlescape = false;
	private boolean ignore_code = false;
	
	private CMMLStatementGroup current_block;
	private boolean codemode_textpost = false;
	private int current_context = NUSALSeqReader.PARSEMODE_UNDEFINED;
	private NUSALSeqDataType current_dtype;

	private Stack<Boolean> ifstack; //For preprocessor ifs (eg. #ifndef)
	private List<CMMLStatementGroup> output;
	private List<CMMLStatementGroup> included;
	
	/*----- Inner Classes -----*/
	
	private static class ParserState{
		public int readmode = READMODE_NONE;
		public boolean nlescape = false;
		public StringBuilder buffer;
	}
	
	/*----- Init -----*/
	
	public CMMLPreprocessor(){
		initDefault();
	}
	
	private void initDefault(){
		include_queue = new LinkedList<String>();
		defines = new HashMap<String, CMMLDefine>();
		io_alias = new HashMap<String, Integer>();
		state_stack = new Stack<ParserState>();
		ifstack = new Stack<Boolean>();
		output = new LinkedList<CMMLStatementGroup>();
		included = new LinkedList<CMMLStatementGroup>();
	}
	
	/*----- Getters -----*/
	
	public CMMLDefine getDefine(String key){
		return defines.get(key);
	}
	
	public List<CMMLDefine> getAllDefines(){
		List<CMMLDefine> copy = new LinkedList<CMMLDefine>();
		copy.addAll(defines.values());
		return copy;
	}
	
	public String popUnprocessedInclude(){
		if(include_queue.isEmpty()) return null;
		return include_queue.pop();
	}
	
	/*----- Setters -----*/
	
	public void clearIOAliases(){
		io_alias.clear();
	}
	
	/*----- Internal -----*/
	
	private void clearTextBuffer(){
		int len = textbuffer.length();
		textbuffer.delete(0, len);
	}
	
	private boolean processMacroStatement(String statement){
		statement = substituteDefines(statement);
		int cidx = statement.indexOf(' ');
		String val;
		if(statement.startsWith("#define")){
			CMMLDefine def = new CMMLDefine(statement);
			defines.put(def.getKey(), def);
		}
		else if(statement.startsWith("#ioalias")){
			if(file_mode == FILEMODE_INCLUDED_HEADER) return true;
			if(cidx >= 0){
				val = statement.substring(cidx+1);
				cidx = val.indexOf(' ');
				String k = val.substring(0, cidx);
				String v = val.substring(cidx+1);
				int io = -1;
				
				//Determine what v is.
				cidx = v.indexOf('_');
				if(v.startsWith("seqio")){
					try{
						io = Integer.parseInt(v.substring(cidx+1));
					}
					catch(NumberFormatException ex){
						System.err.println("Preprocessor Error || Line " + line_number + ": Invalid seqio register.");
						ex.printStackTrace();
						return false;
					}
					if(io >= 8 || io < 0){
						System.err.println("Preprocessor Error || Line " + line_number + ": Invalid seqio register.");
						return false;
					}
					//Instead of overwriting with new ones, will use the first version of alias it comes across
					//seqio_alias.put(k, io);
					Integer check = io_alias.get(k);
					if(check != null){
						System.err.println("Preprocessor Warning || Line " + line_number + ": Duplicate IO alias \"" + k + " \"");
					}
					else io_alias.put(k, io | FLAG_SEQIO_ALIAS);
				}
				else if(v.startsWith("chio")){
					try{
						io = Integer.parseInt(v.substring(cidx+1));
					}
					catch(NumberFormatException ex){
						System.err.println("Preprocessor Error || Line " + line_number + ": Invalid chio register.");
						ex.printStackTrace();
						return false;
					}
					if(io >= 8 || io < 0){
						System.err.println("Preprocessor Error || Line " + line_number + ": Invalid chio register.");
						return false;
					}
					//chio_alias.put(k, io);
					Integer check = io_alias.get(k);
					if(check != null){
						System.err.println("Preprocessor Warning || Line " + line_number + ": Duplicate IO alias \"" + k + " \"");
					}
					else io_alias.put(k, io | FLAG_CHIO_ALIAS);
				}
				else if(v.startsWith("ochio")){
					int ch = -1;
					try{
						String[] split = v.split("_");
						ch = Integer.parseInt(split[1]);
						io = Integer.parseInt(split[2]);
					}
					catch(Exception ex){
						System.err.println("Preprocessor Error || Line " + line_number + ": Invalid ochio register.");
						ex.printStackTrace();
						return false;
					}
					if(ch >= 16 || ch < 0){
						System.err.println("Preprocessor Error || Line " + line_number + ": Invalid ochio channel index.");
						return false;
					}
					if(io >= 8 || io < 0){
						System.err.println("Preprocessor Error || Line " + line_number + ": Invalid ochio register index.");
						return false;
					}
					//ochio_alias.put(k, ch << 8 | io);
					Integer check = io_alias.get(k);
					if(check != null){
						System.err.println("Preprocessor Warning || Line " + line_number + ": Duplicate IO alias \"" + k + " \"");
					}
					else io_alias.put(k, io | FLAG_OCHIO_ALIAS | ch << 8);
				}
			}
		}
		else if(statement.startsWith("#" + MACRONAME_MMLCONTEXT)){
			if(cidx >= 0){
				val = statement.substring(cidx+1).trim();
				if(val.equals("seq") || val.equals("sequence")){
					current_context = NUSALSeqReader.PARSEMODE_SEQ;
				}
				else if(val.equals("channel")){
					current_context = NUSALSeqReader.PARSEMODE_CHANNEL;
				}
				else if(val.equals("layer")){
					current_context = NUSALSeqReader.PARSEMODE_LAYER;
				}
				else if(val.equals("auto")){
					current_context = NUSALSeqReader.PARSEMODE_UNDEFINED;
				}
				else{
					System.err.println("Preprocessor Error || Line " + line_number + ": Invalid mmlcontext value: " + val);
					return false;
				}
			}
		}
		else if(statement.startsWith("#" + MACRONAME_MMLDATTYPE)){
			if(cidx >= 0){
				val = statement.substring(cidx+1).trim();
				current_dtype = NUSALSeqDataType.readMML(val);
				if(current_dtype == null){
					System.err.println("Preprocessor Warning || Line " + line_number + ": Data type value not recognized: " + val);
				}
			}
		}
		else if(statement.startsWith("#include")){
			if(cidx >= 0){
				val = statement.substring(cidx+1);
				val.replace("\"", "");
				include_queue.add(val);
			}
		}
		else if(statement.startsWith("#sysdefine")){
			//Ignore
		}
		else if(statement.startsWith("#ifdef")){
			if(cidx >= 0){
				val = statement.substring(cidx+1).trim();
				if(defines.containsKey(val)){
					ifstack.push(true);
				}
				else{
					ifstack.push(false);
					ignore_code = true;
				}
			}
		}
		else if(statement.startsWith("#ifndef")){
			if(cidx >= 0){
				val = statement.substring(cidx+1).trim();
				if(defines.containsKey(val)){
					ifstack.push(false);
					ignore_code = true;
				}
				else{
					ifstack.push(true);
				}
			}
		}
		else if(statement.startsWith("#endif")){
			if(!ifstack.isEmpty()) ifstack.pop();
			boolean has_false = false;
			for(Boolean b : ifstack){
				if(!b){
					has_false = true;
					break;
				}
			}
			ignore_code = has_false;
		}
		else{
			System.err.println("Preprocessor Error || Line " + line_number + ": Unrecognized macro");
			return false;
		}
		return true;
	}
	
	private boolean checkMacroEntry(char this_c){
		if(this_c == '#'){
			pushParseState();
			readmode = READMODE_MACRO;
			macromode_nlescape = false;
			textbuffer.append('#');	
			return true;
		}
		return false;
	}
	
	private boolean checkCommentEntry(char last_c, char this_c){
		switch(this_c){
		case '/':
			pushParseState();
			if(last_c == '/'){
				readmode = READMODE_ONELINECMT;
			}
			return true;
		case '*':
			pushParseState();
			if(last_c == '/'){
				readmode = READMODE_MULTILINECMT;
			}
			return true;
		default: return false;
		}
	}
	
	private void pushParseState(){
		ParserState state = new ParserState();
		state.buffer = textbuffer;
		state.nlescape = macromode_nlescape;
		state.readmode = readmode;
		state_stack.push(state);
		macromode_nlescape = false;
		textbuffer = new StringBuilder(1024);
	}
	
	private void popParseState(){
		if(state_stack.isEmpty()) return;
		ParserState state = state_stack.pop();
		clearTextBuffer();
		textbuffer = state.buffer;
		macromode_nlescape = state.nlescape;
		readmode = state.readmode;
	}
	
	private void clearParseStateStack(){
		while(!state_stack.isEmpty()) popParseState();
	}
	
	private void saveStatement(CMMLStatementGroup statement){
		if(file_mode == FILEMODE_MODULE_CODE){
			output.add(statement);
		}
		else{
			included.add(statement);
		}
	}
	
	/*----- Interface -----*/
	
	public int parseIOAlias(String alias){
		Integer check = io_alias.get(alias);
		if(check == null) return -1;
		return check;
	}
	
	public String substituteDefines(String input){
		String output = input;
		for(CMMLDefine def : defines.values()){
			output = def.performSubstitution(output);
		}
		return output.trim();
	}
	
	public boolean preprocess(InputStream input) throws IOException{
		textbuffer = new StringBuilder(1024);
		current_context = NUSALSeqReader.PARSEMODE_UNDEFINED;
		
		int c = -1;
		char c_this = '\0';
		char c_last = '\0';
		while((c = input.read()) != -1){
			c_this = (char)c;
			switch(readmode){
			case READMODE_NONE:
				//Ignore whitespace. Look for characters that signal beginning of statement
				//	or macro or something.
				if(!Character.isWhitespace(c_this)){
					if(!checkMacroEntry(c_this) && !checkCommentEntry(c_last, c_this)){
						if(Character.isAlphabetic(c_this)){
							//Assumed code
							readmode = READMODE_CODE;
						}
						else{
							System.err.println("Preprocessor Syntax Error | Unexpected character found in line " + line_number);
							return false;
						}
					}
				}
				break;
			case READMODE_MACRO:
				switch(c_this){
				case '\\':
					macromode_nlescape = true;
					break;
				case '\n':
					if(macromode_nlescape){
						textbuffer.append(c_this);
					}
					else{
						if(!processMacroStatement(textbuffer.toString())) return false;
						popParseState();
					}
					break;
				default:
					if(!checkCommentEntry(c_last, c_this)){
						if(!Character.isWhitespace(c_this)){
							macromode_nlescape = false;
						}
						textbuffer.append(c_this);
					}
					break;
				}
				break;
			case READMODE_CODE:
				if(!ignore_code){
					switch(c_this){
					case '{':
						//Start block
						CMMLStatementGroup myblock = new CMMLStatementGroup(current_block);
						current_block = myblock;
						//Put text through defines.
						myblock.setPreText(substituteDefines(textbuffer.toString()));
						clearTextBuffer();
						myblock.setMMLContext(current_context);
						myblock.setMMLDataType(current_dtype);
						codemode_textpost = false;
						break;
					case '}':
						//End block, or start end block
						clearTextBuffer();
						if(current_block == null || !current_block.isGroup()){
							System.err.println("Preprocessor Syntax Error | Line " + line_number + ": End of block with no start?");
							return false;
						}
						codemode_textpost = current_block.expectsPost();
						if(!codemode_textpost){
							//Finish with block.
							CMMLStatementGroup parent = current_block.getParent();
							if(parent == null){
								saveStatement(current_block);
								current_block = null;
							}
							else{
								current_block = parent;
							}
						}
						break;
					case ';':
						//End statement
						if(codemode_textpost){
							//Assumed to be tail for previous block.
							if(current_block == null || !current_block.isGroup()){
								//Regular statement (erroneous)
								System.err.println("Preprocessor Syntax Warning | Line " + line_number + ": Statement expected to be part of nonexistent block?");
								CMMLStatement statement = new CMMLStatement(null);
								statement.setText(substituteDefines(textbuffer.toString()));
								clearTextBuffer();
								statement.setMMLContext(current_context);
								statement.setMMLDataType(current_dtype);
								saveStatement(statement);
								current_block = null;
							}
							else{
								current_block.setPostText(substituteDefines(textbuffer.toString()));
								CMMLStatementGroup parent = current_block.getParent();
								if(parent == null){
									saveStatement(current_block);
									current_block = null;
								}
								else{
									current_block = parent;
								}
							}
							codemode_textpost = false;
						}
						else{
							//Assumed regular statement.
							CMMLStatement statement = new CMMLStatement(current_block);
							statement.setText(substituteDefines(textbuffer.toString()));
							clearTextBuffer();
							statement.setMMLContext(current_context);
							statement.setMMLDataType(current_dtype);
							if(current_block == null) saveStatement(statement);
						}
						break;
					default:
						if(!checkMacroEntry(c_this) && !checkCommentEntry(c_last, c_this)){
							textbuffer.append(c_this);
						}
						break;
					}	
				}
				break;
			case READMODE_ONELINECMT:
				if(c_this == '\n'){
					popParseState();
				}
				//else do nothing
				break;
			case READMODE_MULTILINECMT:
				if(c_this == '/' && c_last == '*'){
					popParseState();
				}
				//else do nothing
				break;
			}
			if(c_this == '\n') line_number++;
			c_last = c_this;
		}
		
		if(current_block != null) saveStatement(current_block);
		current_block = null;
		
		clearParseStateStack();
		clearTextBuffer();
		textbuffer = null;
		
		if(file_mode == FILEMODE_MODULE_CODE) file_mode = FILEMODE_MODULE_HEADER;
		else if(file_mode == FILEMODE_MODULE_HEADER) file_mode = FILEMODE_INCLUDED_HEADER;
		
		return true;
	}
	
}
