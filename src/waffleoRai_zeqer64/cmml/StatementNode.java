package waffleoRai_zeqer64.cmml;

import java.util.LinkedList;

//For parsing nested parentheses

public class StatementNode {
	
	private StatementNode parent;

	private boolean insert_first = false; //If first child insertion is before first content.
	private String[] content;
	private StatementNode[] children;
	
	public StatementNode(StatementNode parent_node, String text){
		int pcount = 0;
		int pidx = -1;
		int eidx = -1;
		int pos = -1;
		int tlen = text.length();
		
		parent = parent_node;
		if(text == null || text.isEmpty()) return;
		
		pidx = text.indexOf('(');
		if(pidx < 0){
			//Nothing further down.
			content = new String[1];
			content[0] = text;
			return;
		}
		
		LinkedList<String> contentlist = new LinkedList<String>();
		LinkedList<String> childlist = new LinkedList<String>();
		
		if(pidx == 0) insert_first = true;
		pos = pidx+1; pcount = 1;
		while(pos >= 0 && pos < tlen){
			char c = text.charAt(pos);
			if(c == ')'){
			 if(--pcount < 1){
				 //split into content and child.
				 if(eidx < 0) eidx = 0;
				 if(pidx > 0){
					 contentlist.add(text.substring(eidx+1, pidx));
				 }
				 eidx = pos;
				 childlist.add(text.substring(pidx+1, eidx));
			 }
			}
			else if(c == '('){
				if(pcount++ ==0){
					pidx = pos;
				}
			}
			pos++;
		}
		
		int i = 0;
		content = new String[contentlist.size()];
		while(!contentlist.isEmpty()){
			content[i++] = contentlist.pop();
		}
		
		if(!childlist.isEmpty()){
			children = new StatementNode[childlist.size()];
			i = 0;
			while(!childlist.isEmpty()){
				StatementNode cnode = new StatementNode(this, childlist.pop());
				children[i++] = cnode;
			}
		}
	}

	public StatementNode getParent(){return parent;}
	
	public int childCount(){
		if(children == null) return 0;
		return children.length;
	}
	
	public StatementNode getChild(int idx){
		if(children == null) return null;
		if(idx < 0 || idx >= children.length) return null;
		return children[idx];
	}
	
	public int contentEntryCount(){
		if(content == null) return 0;
		return content.length;
	}
	
	public String getContentEntry(int idx){
		if(content == null) return null;
		if(idx < 0 || idx >= content.length) return null;
		return content[idx];
	}
	
	public boolean startsWithChild(){
		return insert_first;
	}
	
	public boolean startsWithContent(){
		return !insert_first;
	}
	
	public void trimFromBeginning(int count){
		boolean cmode = insert_first; //check children
		int i1 = 0;
		int i2 = 0;
		for(int i = 0; i < count; i++){
			if(cmode) i2++;
			else i1++;
			cmode = !cmode;
		}
		
		if(i1 >= content.length){
			content = new String[1];
			content[0] = "";
		}
		else{
			int newsize = content.length - i1;
			String[] old = content;
			content = new String[newsize];
			for(int i = 0; i < newsize; i++){
				content[i] = old[i+i1];
			}
		}
		
		if(i2 >= children.length){
			children = null;
		}
		else{
			int newsize = children.length - i2;
			StatementNode[] old = children;
			children = new StatementNode[newsize];
			for(int i = 0; i < newsize; i++){
				children[i] = old[i+i2];
			}
		}
		
		if(count % 2 != 0) insert_first = !insert_first;
	}
	
}
