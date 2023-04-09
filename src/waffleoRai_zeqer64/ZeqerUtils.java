package waffleoRai_zeqer64;

public class ZeqerUtils {

	public static String fixHeaderEnumID(String input, boolean macro){
		//If a macro, will be set to all uppercase
		if(input == null) return input;
		if(macro) input = input.toUpperCase();
		input = input.replace(' ', '_');
		
		int len = input.length();
		StringBuilder sb = new StringBuilder(len);
		for(int i = 0; i < len; i++){
			char c = input.charAt(i);
			if(Character.isAlphabetic(c)) sb.append(c);
			else if(Character.isDigit(c) && i != 0) sb.append(c);
			else if(c == '_') sb.append(c);
		}
		
		return sb.toString();
	}
	
}
