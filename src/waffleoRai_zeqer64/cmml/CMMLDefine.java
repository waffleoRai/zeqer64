package waffleoRai_zeqer64.cmml;

import java.util.LinkedList;

public class CMMLDefine {
	
	/*----- Instance Variables -----*/
	
	private String key;
	private String[] args;
	private Segment[] value;
	
	/*----- Inner Class -----*/
	
	private static class Segment{
		public String text = null;
		public int arg_idx = -1;
		
		public Segment(String txt){text = txt;}
		public Segment(String txt, int argidx){text = txt; arg_idx = argidx;}
	}
	
	/*----- Init -----*/
	
	public CMMLDefine(String define_text){
		processFromText(define_text);
	}
	
	private void processFromText(String define_text){
		if(define_text.startsWith("#define")){
			//Cut off "#define"
			define_text = define_text.substring(7);
		}
		define_text = define_text.trim();
		
		//Split off key/args
		int cidx = define_text.indexOf(' ');
		int plidx = define_text.indexOf('(');
		int pridx = define_text.indexOf(')');
		
		if(cidx < 0){
			//Assumed key only.
			key = define_text;
			return;
		}
		
		if((plidx >= 0) && (plidx < cidx)){
			//Has args.
			String rawvalue = define_text.substring(pridx+1).trim();
			key = define_text.substring(0,plidx).trim();
			String argsstr = define_text.substring(plidx+1, pridx);
			String[] arglist = argsstr.split(",");
			if(arglist != null && arglist.length > 0){
				args = new String[arglist.length];
				for(int i = 0; i < arglist.length; i++){
					args[i] = arglist[i].trim();
				}
			}
			
			LinkedList<Segment> nowlist = new LinkedList<Segment>();
			LinkedList<Segment> lastlist = new LinkedList<Segment>();
			lastlist.add(new Segment(rawvalue));
			
			for(int i = 0; i < args.length; i++){
				for(Segment s : lastlist){
					if(s.arg_idx < 0){
						int aidx = s.text.indexOf(args[i]);
						if(aidx < 0){
							nowlist.add(s);
							continue;
						}
						
						String rem = s.text;
						while(aidx >= 0){
							int eidx = aidx + args[i].length();
							int sidx = 0;
							char lc = ' ';
							char rc = ' ';
							
							if(aidx > 0) lc = rem.charAt(aidx-1);
							if(eidx < (rem.length() - 1)) rc = rem.charAt(eidx);
							
							if(flankingCharOK(lc) && flankingCharOK(rc)){
								if(aidx > 0){
									//Left part
									nowlist.add(new Segment(rem.substring(0, aidx)));
								}
								nowlist.add(new Segment(rem.substring(aidx, eidx), i));
								rem = rem.substring(eidx);
							}
							else{
								//So next search doesn't just find this instance again.
								sidx = eidx;
							}
						
							aidx = rem.indexOf(args[i], sidx);
						}
						
						//Add last bit.
						if(rem != null && !rem.isEmpty()){
							nowlist.add(new Segment(rem));
						}
					}
					else nowlist.add(s);
				}
				
				lastlist.clear();
				lastlist.addAll(nowlist);
				nowlist.clear();
			}
			
			//Copy to value
			if(!lastlist.isEmpty()){
				value = new Segment[lastlist.size()];
				int j = 0;
				for(Segment seg : lastlist){
					value[j++] = seg;
				}
				lastlist.clear();
			}
			else{
				value = new Segment[1];
				value[0] = new Segment(rawvalue);
			}
		}
		else{
			//No args?
			key = define_text.substring(0, cidx).trim();
			value = new Segment[1];
			value[0] = new Segment(define_text.substring(cidx+1).trim());
		}
	}

	/*----- Getters -----*/
	
	public String getKey(){return key;}
	
	public String getValue(){
		if(value == null) return null;
		String outval = "";
		for(int i = 0; i < value.length; i++){
			Segment seg = value[i];
			if(seg.arg_idx >= 0){
				outval += args[value[i].arg_idx];
			}
			else{
				outval += value[i].text;
			}
		}
		return outval;
	}
	
	public int getArgCount(){
		if(args == null) return 0;
		return args.length;
	}
	
	public String getArg(int idx){
		if(args == null) return null;
		if(idx < 0) return null;
		if(idx >= args.length) return null;
		return args[idx];
	}
	
	/*----- Setters -----*/
	
	/*----- Action -----*/
	
	private static boolean flankingCharOK(char c){
		if(Character.isWhitespace(c)) return true;
		if(CMMLConstants.isReservedCharacter(c)) return true;
		return false;
	}
	
	public String performSubstitution(String input){
		if(input == null) return null;
		if(input.isEmpty()) return input;
		String output = input;
		
		//Search for key instances.
		int kidx = output.indexOf(key);
		int eidx;
		while(kidx >= 0){
			//Find key end.
			if(args == null){
				eidx = kidx + key.length();
			}
			else{
				eidx = output.indexOf(')', kidx) + 1;
			}
			
			//Check flanking characters.
			//Must be whitespace or reserved.
			char lc = ' ';
			char rc = ' ';
			if(kidx > 0) lc = output.charAt(kidx-1);
			if(eidx < (output.length()-1)) rc = output.charAt(eidx);
			
			if(flankingCharOK(lc) && flankingCharOK(rc)){
				//Perform substitution
				String temp = "";
				int search_idx = -1;
				if(args == null || args.length < 1){
					temp = output.substring(0, kidx);
					temp += value;
					search_idx = temp.length();
					temp += output.substring(eidx);
				}
				else{
					//Get args sub strings.
					String[] myargs = new String[args.length];
					int pl = output.indexOf('(', kidx);
					int pr = eidx - 1;
					String[] split = output.substring(pl+1, pr).split(",");
					for(int j = 0; j < split.length; j++){
						myargs[j] = split[j].trim();
					}
					
					temp = output.substring(0, kidx);
					for(int j = 0; j < value.length; j++){
						Segment seg = value[j];
						if(seg.arg_idx < 0){
							temp += seg.text;
						}
						else{
							temp += myargs[seg.arg_idx];
						}
					}
					search_idx = temp.length();
					temp += output.substring(eidx);
				}
				output = temp;
				
				kidx = output.indexOf(key, search_idx);
			}
			else{
				//Find next instance after this one.
				kidx = output.indexOf(key, eidx);	
			}
		}
		
		return output;
	}
	
}
