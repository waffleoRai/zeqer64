package waffleoRai_zeqer64.presets;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileUtils;
import waffleoRai_soundbank.nintendo.z64.Z64Drum;
import waffleoRai_soundbank.nintendo.z64.Z64Envelope;
import waffleoRai_zeqer64.ZeqerPreset;

public class ZeqerPercPreset extends ZeqerPreset{

	private String name;
	private Z64Drum[] slots;
	private int[] wave_ids;
	
	private boolean hashmode = false;
	
	public ZeqerPercPreset(int uid){
		super.uid = uid;
		name = String.format("perc_%08x", uid);
		slots = new Z64Drum[64];
		wave_ids = new int[64];
	}
	
	public String getName() {return name;}
	public int getType() {return ZeqerPreset.PRESET_TYPE_PERC;}
	
	public Z64Drum getDrumInSlot(int idx){
		if(idx < 0 || idx >= 64) return null;
		return slots[idx];
	}
	
	public int getMaxUsedSlotCount(){
		if(slots == null) return 0;
		
		int c = 0;
		for(int i = 0; i < slots.length; i++){
			if(slots[i] != null) c = i;
		}
		return c;
	}
	
	public List<Z64Envelope> getEnvelopes(){
		//Not a good approach but I'll fix it later...
		List<Z64Envelope> list = new LinkedList<Z64Envelope>();
		for(int i = 0; i < 64; i++){
			Z64Drum drum = slots[i];
			if(drum == null) continue;
			Z64Envelope env = drum.getEnvelope();
			if(env == null) continue;
			
			//See if already in...
			boolean inlist = false;
			for(Z64Envelope other : list){
				if(other == env){
					//Literally the same
					inlist = true;
					break;
				}
				else{
					//Check equivalent
					if(other.envEquals(env)){
						inlist = true;
						//And we'll also merge them for good measure.
						drum.setEnvelope(other);
						break;
					}
				}
			}
			if(!inlist) list.add(env);
		}
		return list;
	}

	public void setName(String s) {name = s;}
	
	public void setWaveID(int idx, int value){
		if(idx < 0 || idx >= 64) return;
		wave_ids[idx] = value;
	}
	
	public void setDrumToSlot(int idx, Z64Drum drum){
		if(idx < 0 || idx >= 64) return;
		//Check slot before it and merge if identical.
		if(idx > 0 && drum != null && slots[idx-1] != null){
			Z64Drum prev = slots[idx-1];
			if(drum != prev){
				if(drum.drumEquals(prev) && (wave_ids[idx] == wave_ids[idx-1])){
					drum = prev;
				}
			}
		}
		slots[idx] = drum;
	}
	
	public int hashToUID(){
		FileBuffer buff = new FileBuffer(4+(32*64), true);
		hashmode = true;
		serializeMe(buff);
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
		flags |= ZeqerPreset.PRESET_TYPE_PERC;
		
		if(!hashmode) buffer.addToFile(uid);
		else buffer.addToFile(0);
		buffer.addToFile(flags); //Flags
		
		int scount = getMaxUsedSlotCount();
		buffer.addToFile((byte)scount);
		buffer.add24ToFile(0);
		
		for(int i = 0; i < scount; i++){
			Z64Drum drum = slots[i];
			if(drum != null){
				buffer.addToFile(drum.getDecay());
				buffer.addToFile(drum.getPan());
				Z64Envelope env = drum.getEnvelope();
				
				if(!hashmode){
					if(env == null) buffer.addToFile((short)-1);
					else buffer.addToFile((short)env.getID());
				}
				else{
					//Copy envelope.
					if(env != null){
						List<short[]> events = env.getEvents();
						for(short[] event : events){
							buffer.addToFile(event[0]);
							buffer.addToFile(event[1]);
						}
					}
				}
				
				Z64WaveInfo winfo = drum.getSample();
				if(winfo == null) buffer.addToFile(wave_ids[i]);
				else buffer.addToFile(winfo.getUID());
				
				float tune = Z64Drum.commonToLocalTuning(i, drum.getTuning());
				buffer.addToFile(Float.floatToRawIntBits(tune));
			}
			else{
				//Empty Slot
				buffer.addToFile((short)0);
				buffer.addToFile((short)-1);
				buffer.addToFile(0);
				buffer.addToFile(0);
			}
		}
		
		return (int)(buffer.getFileSize() - init_size);
	}
	
	public void exportTSVLine(Writer w) throws IOException{
		
		for(int i = 0; i < 64; i++){
			Z64Drum drum = slots[i];
			if(drum == null) continue;
			
			w.write(getName() + "\t");
			w.write(String.format("%08x\t", uid));
			w.write(i + "\t");
			
			Z64Envelope env = drum.getEnvelope();
			if(env == null) w.write("-1\t");
			else w.write(env.getID() + "\t");
			
			w.write(drum.getDecay() + "\t");
			w.write(drum.getPan() + "\t");
			
			Z64WaveInfo winfo = drum.getSample();
			if(winfo == null){
				if(wave_ids[i] == 0 || wave_ids[i] == -1) w.write("N/A\tN/A\n");
				else{
					w.write(String.format("%08x\t", wave_ids[i]));
					w.write(drum.getTuning() + "\t");
					w.write(Z64Drum.commonToLocalTuning(i, drum.getTuning()) + "\n");
				}
			}
			else{
				w.write(String.format("%08x\t", winfo.getUID()));
				w.write(drum.getTuning() + "\t");
				w.write(Z64Drum.commonToLocalTuning(i, drum.getTuning()) + "\n");
			}
		}
	}

}
