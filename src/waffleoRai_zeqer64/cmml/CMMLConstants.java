package waffleoRai_zeqer64.cmml;

public class CMMLConstants {
	
	public static final String KEYWORD_TRUE = "true";
	public static final String KEYWORD_FALSE = "false";
	
	public static final String KEYWORD_FOR = "for";
	public static final String KEYWORD_WHILE = "while";
	public static final String KEYWORD_DO = "do";
	public static final String KEYWORD_IF = "if";
	public static final String KEYWORD_ELSE = "else";
	public static final String KEYWORD_ELSEIF = "else if";
	public static final String KEYWORD_BREAK = "break";
	public static final String KEYWORD_CONTINUE = "continue";
	public static final String KEYWORD_GOTO = "goto";
	public static final String KEYWORD_SWITCH = "switch";
	public static final String KEYWORD_CASE = "case";
	public static final String KEYWORD_DEFAULT = "default";
	
	public static final String KEYWORD_INLINE = "inline";
	public static final String KEYWORD_CONST = "const";
	public static final String KEYWORD_RETURN = "return";
	
	public static final char[] RESERVED_CHARS = {'!', '~', '#', '$', '&', '(', ')',
			'|', '-', '+', '=', '*', '{', '}', '[', ']', ':', ';', '\'', '\"',
			'<', '>', '?', ',', '.', '/'};
	
	public static boolean isReservedCharacter(char c){
		for(char o : RESERVED_CHARS){
			if(c == o) return true;
		}
		return false;
	}

}
