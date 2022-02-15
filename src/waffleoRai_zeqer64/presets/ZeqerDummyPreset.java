package waffleoRai_zeqer64.presets;

import java.util.Random;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_zeqer64.ZeqerPreset;

public class ZeqerDummyPreset extends ZeqerPreset{
	
	private int uid;
	private int type;
	private String name;
	
	public ZeqerDummyPreset(){
		Random r = new Random();
		name = String.format("zpreset_%08x", Integer.toHexString(r.nextInt()));
	}
	
	public ZeqerDummyPreset(String n){name = n;}
	
	public ZeqerDummyPreset(int uid){
		this.uid = uid;
		name = String.format("zpreset_%08x", Integer.toHexString(uid));
	}

	public boolean hasData(){return false;}
	public String getName(){return name;}
	public int getType(){return type;}
	
	public void setName(String s){name = s;}
	public void setType(int val){type = val;}
	
	protected int serializeMe(FileBuffer buffer){
		long init_size = buffer.getFileSize();
		buffer.addToFile(uid);
		int flags = 0x00000000;
		flags |= (type & 0x3);
		buffer.addToFile(flags);
		//buffer.addVariableLengthString("UTF8", name, BinFieldSize.WORD, 2);
		return (int)(buffer.getFileSize() - init_size);
	}
	
}
