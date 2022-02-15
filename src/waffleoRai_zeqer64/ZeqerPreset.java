package waffleoRai_zeqer64;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import waffleoRai_Utils.BinFieldSize;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_soundbank.nintendo.z64.Z64Envelope;

public abstract class ZeqerPreset {
	
	public static final int PRESET_TYPE_INST = 0;
	public static final int PRESET_TYPE_PERC = 1;
	public static final int PRESET_TYPE_SFX = 2;
	
	protected int uid;
	protected Set<String> tags;
	
	protected ZeqerPreset(){
		tags = new HashSet<String>();
	}

	public abstract String getName();
	public abstract int getType();
	public int getUID(){return uid;}
	
	public int hashToUID(){return 0;} //Defaults to empty dummy...
	
	public List<Z64Envelope> getEnvelopes(){return null;}
	
	public abstract void setName(String s);
	
	public void setUID(int val){uid = val;}
	public boolean hasData(){return true;}
	
	public boolean hasTag(String tag){
		return tags.contains(tag);
	}
	
	public void addTag(String tag){
		tags.add(tag);
	}
	
	public void clearTags(){
		tags.clear();
	}
	
	public List<String> getAllTags(){
		List<String> list = new ArrayList<String>(tags.size()+1);
		list.addAll(tags);
		return list;
	}
	
	public int estimateWriteBufferSize(){
		int size = 8; //UID, flags
		String name = getName();
		if(name == null) size += 2;
		else size += 3 + name.length();
		if(hasData()){
			switch(getType()){
			case ZeqerPreset.PRESET_TYPE_INST:
				size += 32;
				break;
			case ZeqerPreset.PRESET_TYPE_PERC:
				size += 4 + (12*64);
				break;
			case ZeqerPreset.PRESET_TYPE_SFX:
				size += 4 + (8*64);
				break;
			}	
		}
		
		for(String tag : tags){
			size += tag.length() + 4;
		}
		
		return size;
	}
	
	protected abstract int serializeMe(FileBuffer buffer);
	
	public int serializeTo(FileBuffer buffer){
		int sz = serializeMe(buffer);
		
		String name = getName();
		if(name == null){
			name = String.format("preset_%08x", uid);
			setName(name);
		}
		buffer.addVariableLengthString("UTF8", name, BinFieldSize.WORD, 2);	
		sz += name.length() + 2;
		if((sz % 2) != 0) sz++;
		
		//Tags.
		StringBuilder sb = new StringBuilder(1024);
		boolean first = true;
		for(String tag : tags){
			if(!first)sb.append(';');
			sb.append(tag);
			first = false;
		}
		String tagstr = sb.toString();
		buffer.addVariableLengthString("UTF8", tagstr, BinFieldSize.WORD, 2);	
		sz += tagstr.length() + 2;
		if((sz % 2) != 0) sz++;
		
		return sz;
	}
	
}
