package waffleoRai_zeqer64.presets;

import java.io.IOException;
import java.io.Writer;

import waffleoRai_Utils.BinFieldSize;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileUtils;
import waffleoRai_zeqer64.ZeqerPreset;

public class ZeqerSFXPreset extends ZeqerPreset{
	
	private String name;
	private int group_idx;
	private int[] wave_ids;
	private float[] tuning;
	private String[] slot_enums;
	private String[] slot_names;
	
	private boolean hashmode = false;
	
	public ZeqerSFXPreset(int uid){
		super.uid = uid;
		name = String.format("sfxgroup_%08x", uid);
		wave_ids = new int[64];
		tuning = new float[64];
		slot_enums = new String[64];
		slot_names = new String[64];
	}

	public String getName() {return name;}
	public int getType() {return ZeqerPreset.PRESET_TYPE_SFX;}
	public int getGroupIndex(){return group_idx;}
	
	public int getWaveID(int idx){
		if(idx < 0 || idx >= 64) return 0;
		return wave_ids[idx];
	}
	
	public float getTuningValue(int idx){
		if(idx < 0 || idx >= 64) return 1.0f;
		return tuning[idx];
	}
	
	public int getMaxUsedSlotCount(){
		if(wave_ids == null) return 0;
		
		int c = 0;
		for(int i = 0; i < wave_ids.length; i++){
			if(wave_ids[i] != 0 && wave_ids[i] != -1) c = i;
		}
		return c;
	}

	public String getSlotEnumString(int slot){return slot_enums[slot];}
	public String getSlotNameString(int slot){return slot_names[slot];}
	
	public void setName(String s) {name = s;}
	public void setGroupIndex(int value){group_idx = value;}
	public void setSlotEnumString(int slot, String value){slot_enums[slot] = value;}
	public void setSlotNameString(int slot, String value){slot_names[slot] = value;}
	
	public void setWaveID(int idx, int value){
		if(idx < 0 || idx >= 64) return;
		wave_ids[idx] = value;
	}
	
	public void setTuningValue(int idx, float value){
		if(idx < 0 || idx >= 64) return;
		tuning[idx] = value;
	}
	
	public int hashToUID(){
		FileBuffer buff = new FileBuffer(4+(8*64), true);
		hashmode = true;
		serializeTo(buff);
		byte[] md5 = FileUtils.getMD5Sum(buff.getBytes(0, buff.getFileSize()));
		hashmode = false;
		
		int val = 0;
		for(int i = 0; i < 4; i++){
			val <<= 8;
			val |= Byte.toUnsignedInt(md5[i]);
		}
		return val;
	}
	
	protected int serializeMe(FileBuffer buffer) {
		long init_size = buffer.getFileSize();
		int flags = 0x80000000;
		flags |= ZeqerPreset.PRESET_TYPE_SFX;
		if(!hashmode) buffer.addToFile(uid);
		else buffer.addToFile(0);
		buffer.addToFile(flags); //Flags
		
		int scount = getMaxUsedSlotCount();
		buffer.addToFile((byte)scount);
		if(!hashmode) buffer.addToFile((byte)group_idx);
		else buffer.addToFile((byte)0);
		
		for(int i = 0; i < scount; i++){
			buffer.addToFile(wave_ids[i]);
			if(wave_ids[i] == 0 || wave_ids[i] == -1) buffer.addToFile(0);
			else{
				buffer.addToFile(Float.floatToRawIntBits(tuning[i]));
			}
		}
		
		//String table (V4+)
		int strtbl_size = 0;
		for(int i = 0; i < scount; i++){
			if(slot_enums[i] != null){
				strtbl_size += slot_enums[i].length();
				if((strtbl_size & 0x1) != 0) strtbl_size++;
			}
			else strtbl_size += 2;
			if(slot_names[i] != null){
				strtbl_size += slot_names[i].length();
				if((strtbl_size & 0x1) != 0) strtbl_size++;
			}
			else strtbl_size += 2;
		}
		buffer.addToFile(strtbl_size);
		
		for(int i = 0; i < scount; i++){
			if(slot_enums[i] != null){
				buffer.addVariableLengthString(slot_enums[i], BinFieldSize.WORD, 2);
			}
			else buffer.addToFile((short)0);
			if(slot_names[i] != null){
				buffer.addVariableLengthString(slot_names[i], BinFieldSize.WORD, 2);
			}
			else buffer.addToFile((short)0);
		}
		
		return (int)(buffer.getFileSize() - init_size);
	}

	public void exportTSVLine(Writer w) throws IOException{
		
		for(int i = 0; i < 64; i++){
			int wid = wave_ids[i];
			if(wid == 0 || wid == -1) continue;
			
			w.write(getName() + "\t");
			w.write(String.format("%08x\t", uid));
			w.write(group_idx + "\t");
			w.write(i + "\t");
			w.write(String.format("%08x\t", wave_ids[i]));
			w.write(tuning[i] + "\n");
		}
	}
	
}
