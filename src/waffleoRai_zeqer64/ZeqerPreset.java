package waffleoRai_zeqer64;

import java.util.HashSet;
import java.util.List;

import waffleoRai_Utils.BinFieldSize;
import waffleoRai_Utils.BufferReference;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_soundbank.nintendo.z64.Z64Envelope;
import waffleoRai_zeqer64.iface.ALabeledElement;

public abstract class ZeqerPreset extends ALabeledElement {
	
	public static final int PRESET_TYPE_INST = 0;
	public static final int PRESET_TYPE_PERC = 1;
	public static final int PRESET_TYPE_SFX = 2;
	public static final int PRESET_TYPE_DRUM = 3;
	
	protected ZeqerPreset(){
		tags = new HashSet<String>();
	}

	public abstract int getType();
	
	public int hashToUID(){return 0;} //Defaults to empty dummy...
	
	public List<Z64Envelope> getEnvelopes(){return null;}
	
	public boolean hasData(){return true;}
	
	public abstract long readIn(BufferReference src, Z64Envelope[] envs, int version);
	
	public int estimateWriteBufferSize(){
		int size = 8; //UID, flags
		String name = getName();
		if(name == null) size += 2;
		else size += 3 + name.length();

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
		buffer.addVariableLengthString(ZeqerCore.ENCODING, name, BinFieldSize.WORD, 2);	
		sz += name.length() + 2;
		if((sz % 2) != 0) sz++;
		
		//Enum Label
		String elbl = getEnumLabel();
		if(elbl != null){
			buffer.addVariableLengthString(ZeqerCore.ENCODING, elbl, BinFieldSize.WORD, 2);	
			sz += elbl.length() + 2;
			if((sz % 2) != 0) sz++;
		}
		else{
			buffer.addToFile((short)0);
			sz += 2;
		}
		
		//Tags.
		StringBuilder sb = new StringBuilder(1024);
		boolean first = true;
		for(String tag : tags){
			if(!first)sb.append(';');
			sb.append(tag);
			first = false;
		}
		String tagstr = sb.toString();
		buffer.addVariableLengthString(ZeqerCore.ENCODING, tagstr, BinFieldSize.WORD, 2);	
		sz += tagstr.length() + 2;
		if((sz % 2) != 0) sz++;
		
		return sz;
	}
	
}
