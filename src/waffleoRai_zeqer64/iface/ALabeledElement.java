package waffleoRai_zeqer64.iface;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import waffleoRai_zeqer64.ZeqerUtils;

public abstract class ALabeledElement implements INameable, ITaggable, IEnumNameable{
	
	protected int uid;
	
	protected String name;
	protected String enumLabel;
	
	protected Set<String> tags;
	
	protected ALabeledElement(){
		uid = 0;
		name = null;
		enumLabel = null;
		tags = new HashSet<String>();
	}
	
	public void generateRandomSpecs(){
		uid = 0;
		Random rand = new Random();
		while((uid == 0) || (uid == -1)){
			uid = rand.nextInt();
		}
		name = String.format("Item %08x", uid);
		enumLabel = String.format("ITEM_%08x", uid);
	}
	
	public void generateRandomSpecs(String strPrefix){
		uid = 0;
		Random rand = new Random();
		while((uid == 0) || (uid == -1)){
			uid = rand.nextInt();
		}
		name = String.format("%s %08x", strPrefix, uid);
		
		strPrefix = ZeqerUtils.fixHeaderEnumID(strPrefix, true);
		enumLabel = String.format("%s_%08x", strPrefix, uid);
	}
	
	public int getUID(){return uid;}
	public String getName(){return name;}
	public String getEnumLabel(){return enumLabel;}
	
	public boolean hasTag(String tag){
		if(tags == null || tag == null) return false;
		return tags.contains(tag);
	}
	
	public List<String> getAllTags(){
		if(tags == null) return null;
		List<String> copy = new ArrayList<String>(tags.size() + 1);
		copy.addAll(tags);
		return copy;
	}
	
	public void setUID(int value){uid = value;}
	public void setName(String s){name = s;}
	public void setEnumLabel(String s){enumLabel = s;}
	
	public void addTag(String tag){
		if(tags == null || tag == null) return;
		tags.add(tag);
	}
	
	public void clearTags(){
		if(tags == null) return;
		tags.clear();
	}

}
