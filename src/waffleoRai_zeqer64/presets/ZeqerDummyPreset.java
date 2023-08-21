package waffleoRai_zeqer64.presets;

import java.util.Random;

import waffleoRai_Utils.BufferReference;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_soundbank.nintendo.z64.Z64Drum;
import waffleoRai_soundbank.nintendo.z64.Z64Envelope;
import waffleoRai_soundbank.nintendo.z64.Z64Instrument;
import waffleoRai_zeqer64.ZeqerPreset;

public class ZeqerDummyPreset extends ZeqerPreset{
	
	private int type;
	private String name;
	
	public ZeqerDummyPreset(){
		Random r = new Random();
		name = String.format("zpreset_%08x", r.nextInt());
	}
	
	public ZeqerDummyPreset(String n){name = n;}
	
	public ZeqerDummyPreset(int uid){
		this.uid = uid;
		name = String.format("zpreset_%08x", uid);
	}

	public boolean hasData(){return false;}
	public String getName(){return name;}
	public int getType(){return type;}
	
	public void setName(String s){name = s;}
	public void setType(int val){type = val;}
	
	public long readIn(BufferReference src, Z64Envelope[] envs, int version){
		return 0L;
	}
	
	public ZeqerInstPreset loadInstData(Z64Instrument inst){
		if(inst == null) return null;
		
		ZeqerInstPreset ipre = new ZeqerInstPreset();
		ipre.loadData(inst);
		ipre.setUID(uid);
		ipre.setEnumLabel(enumLabel);
		ipre.setName(name);
		
		if(this.tags != null && !this.tags.isEmpty()){
			for(String tag : this.tags){
				ipre.addTag(tag);
			}
		}
		
		return ipre;
	}
	
	public ZeqerDrumPreset loadDrumData(Z64Drum drum){
		if(drum == null) return null;
		
		ZeqerDrumPreset dpre = new ZeqerDrumPreset();
		dpre.loadDrumData(drum);
		dpre.setUID(uid);
		dpre.setEnumLabel(enumLabel);
		dpre.setName(name);
		
		if(this.tags != null && !this.tags.isEmpty()){
			for(String tag : this.tags){
				dpre.addTag(tag);
			}
		}
		
		if(enumLabel == null){
			dpre.setEnumLabel(String.format("DPRE_%08x", uid));
		}
		
		if(name == null){
			dpre.setName(String.format("DPRE_%08x", uid));
		}
		
		return dpre;
	}
	
	public ZeqerPercPreset loadDrumsetData(ZeqerPercPreset source){
		if(source == null) return null;
		int regcount = source.getRegionCount();
		
		ZeqerPercPreset ppreset = new ZeqerPercPreset(uid);
		ppreset.clearAndReallocRegions(regcount);
		
		for(int i = 0; i < regcount; i++) {
			ppreset.setRegion(i, source.getRegion(i));
		}
		
		ppreset.setUID(uid);
		ppreset.setEnumLabel(enumLabel);
		ppreset.setName(name);
		
		if(this.tags != null && !this.tags.isEmpty()){
			for(String tag : this.tags){
				ppreset.addTag(tag);
			}
		}
		
		if(enumLabel == null){
			ppreset.setEnumLabel(String.format("PPRE_%08x", uid));
		}
		
		if(name == null){
			ppreset.setName(String.format("PPRE_%08x", uid));
		}
		
		return ppreset;
	}
	
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
