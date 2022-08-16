package waffleoRai_zeqer64.cmml;

import java.util.LinkedList;

public class CMMLTextProcessing {
	
	public static int findStandalone(String target, String query){
		//Looks for something as a keyword or operator. Substring must be flanked by whitespace or reserved characters.
		//Returns index of first instance found.
		if(target == null || query == null) return -1;
		
		int qlen = query.length();
		int tlen = target.length();
		int testidx = target.indexOf(query);
		char cl = '\0';
		char cr = '\0';
		boolean pass_left = false;
		boolean pass_right = false;
		
		while(testidx >= 0){
			pass_left = false; pass_right = false;
			
			if(testidx > 0){
				cl = target.charAt(testidx-1);
				if(Character.isWhitespace(cl)) pass_left = true;
				else if(CMMLConstants.isReservedCharacter(cl)) pass_left = true;
			}
			else pass_left = true;
			
			int eidx = testidx + qlen;
			if(eidx < tlen){
				cr = target.charAt(eidx);
				if(Character.isWhitespace(cr)) pass_right = true;
				else if(CMMLConstants.isReservedCharacter(cr)) pass_right = true;
			}
			else pass_right = true;
			
			if(pass_left && pass_right) return testidx;
			
			//Next testidx
			testidx = target.indexOf(query, testidx+1);
		}
		
		return -1;
	}
	
	public static String[][] extractStringLiterals(String input){
		//Output is two string arrays
		//The first is just one string - the input with the string literals replaced with '%s_idx'
		//The second is the substitute string literals.
		//Null return just means the input is unchanged.
		if(input == null || input.isEmpty()) return null;
		int ql_idx = input.indexOf('\"');
		if(ql_idx < 0) return null; //No quotation marks - common occurrence
		
		StringBuilder sb = new StringBuilder(input.length() + 16);
		LinkedList<String> slist = new LinkedList<String>();
		int qr_idx = -1;
		int eidx = 0;
		
		while(ql_idx >= 0){
			//Check if escaped.
			if(ql_idx > 0){
				if(input.charAt(ql_idx - 1) == '\\'){
					//Skip this one.
					ql_idx = input.indexOf('\"', ql_idx+1);
					continue;
				}
			}
			
			//Look for right bound.
			qr_idx = ql_idx;
			do{
				qr_idx = input.indexOf('\"', qr_idx+1);
				if(qr_idx < 0) break;
				if(input.charAt(qr_idx - 1) == '\\'){
					//Skip this one.
					qr_idx = input.indexOf('\"', qr_idx+1);
				}
			} while(qr_idx > 0 && qr_idx < input.length());
			if(qr_idx < 0) break;
			
			//Copy characters between last split and left bound...
			if(ql_idx > eidx){
				sb.append(input.substring(eidx, ql_idx));
			}
			
			//Copy characters between left and right bounds...
			if(qr_idx > ql_idx){
				slist.add(input.substring(ql_idx+1, qr_idx));
			}
			
			//Add placeholder...
			sb.append("%s_" + (slist.size()-1));
			
			//Scan for new left.
			eidx = qr_idx;
			ql_idx = input.indexOf('\"', qr_idx+1);
		}
		
		if(eidx < input.length()){
			sb.append(input.substring(eidx));
		}
		
		//Now prepare output.
		String[][] output = new String[2][];
		output[0] = new String[1];
		output[0][1] = sb.toString();
		
		int i = 0;
		output[1] = new String[slist.size()];
		while(!slist.isEmpty()){
			output[1][i++] = slist.pop();
		}
	
		return output;
	}
	
	public static StatementNode parsePhraseHierarchy(String input){
		StatementNode root = new StatementNode(null, input);
		return root;
	}

}
