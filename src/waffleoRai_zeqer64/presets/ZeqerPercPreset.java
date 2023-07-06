package waffleoRai_zeqer64.presets;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_Sound.nintendo.Z64Sound.Z64Tuning;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.BinFieldSize;
import waffleoRai_Utils.BufferReference;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileUtils;
import waffleoRai_soundbank.nintendo.z64.Z64Drum;
import waffleoRai_soundbank.nintendo.z64.Z64Envelope;
import waffleoRai_zeqer64.ZeqerPreset;
import waffleoRai_zeqer64.filefmt.ZeqerPresetTable;

public class ZeqerPercPreset extends ZeqerPreset{

	private Z64Drum[] slots;
	private ZeqerPercRegion[] regions; //Z64Drum instances stored here and linked in slots
	private int[] wave_ids; //By slots
	
	private boolean hashmode = false;
	private boolean regions_dirty = true;
	
	public ZeqerPercPreset(int uid){
		super.uid = uid;
		name = String.format("perc_%08x", uid);
		slots = new Z64Drum[64];
		wave_ids = new int[64];
	}
	
	public int getType() {return ZeqerPreset.PRESET_TYPE_PERC;}
	
	public long readIn(BufferReference src, Z64Envelope[] envs, int version){
		if(src == null) return 0;
		if(version < 1) return 0;
		if(version > ZeqerPresetTable.TBL_CURRENT_VERSION) return 0;
		
		long stpos = src.getBufferPosition();
		int entrycount = Byte.toUnsignedInt(src.nextByte());
		src.add(3);
		
		for(int i = 0; i < 64; i++) {
			slots[i] = null;
			wave_ids[i] = 0;
		}
		
		int val = 0;
		if(version >= 4){
			regions = new ZeqerPercRegion[entrycount];
			for(int i = 0; i < entrycount; i++){
				int minnote = -1;
				int maxnote = -1;
				Z64Drum drum = new Z64Drum();
				drum.setDecay(src.nextByte());
				drum.setPan(src.nextByte());
				val = src.nextShort();
				if(val >= 0){
					drum.setEnvelope(envs[val]);
				}
				val = src.nextInt();
				Z64Tuning tune = new Z64Tuning();
				tune.root_key = src.nextByte();
				tune.fine_tune = src.nextByte();
				minnote = src.nextByte();
				maxnote = src.nextByte();
				
				//Wrap in region.
				ZeqerPercRegion reg = new ZeqerPercRegion();
				regions[i] = reg;
				reg.setDrumData(drum);
				reg.setMinNote(minnote);
				reg.setMaxNote(maxnote);
				
				//Link in drum and wave id slots.
				int minslot = minnote - Z64Sound.STDRANGE_BOTTOM;
				int maxslot = maxnote - Z64Sound.STDRANGE_BOTTOM;
				for(int j = minslot; j <= maxslot; j++){
					slots[j] = drum;
					wave_ids[j] = val;
				}
			}
			
			//String table
			src.add(4); //Skip the table length
			for(int i = 0; i < entrycount; i++){
				regions[i].setEnumStem(src.nextVariableLengthString(BinFieldSize.WORD, 2));
				regions[i].setNameStem(src.nextVariableLengthString(BinFieldSize.WORD, 2));
			}
		}
		else {
			regions_dirty = true;
			for(int i = 0; i < entrycount; i++){
				Z64Drum drum = new Z64Drum();
				drum.setDecay(src.nextByte());
				drum.setPan(src.nextByte());
				val = src.nextShort();
				if(val >= 0){
					drum.setEnvelope(envs[val]);
				}
				val = src.nextInt();
				if(val != 0 && val != -1){
					wave_ids[i] = val;
					float tune = Float.intBitsToFloat(src.nextInt());
					drum.setTuning(Z64Drum.localToCommonTuning(i, tune));
				}
				else{
					drum.setSample(null);
					wave_ids[i] = 0;
					src.add(4);
				}
				slots[i] = drum;
			}
			consolidateRegions();
		}
		
		return src.getBufferPosition() - stpos;
	}
	
	public Z64Drum getDrumInSlot(int idx){
		if(idx < 0 || idx >= 64) return null;
		return slots[idx];
	}
	
	public int getSlotWaveID(int idx){
		if(wave_ids == null) return 0;
		if(idx < 0) return 0;
		if(idx >= wave_ids.length) return 0;
		return wave_ids[idx];
	}
	
	public int getMaxUsedSlotCount(){
		if(slots == null) return 0;
		
		int c = 0;
		for(int i = 0; i < slots.length; i++){
			if(slots[i] != null) c = i;
		}
		return c;
	}
	
	public ZeqerPercRegion getRegion(int index){
		if(regions == null) return null;
		if(index < 0) return null;
		if(index >= regions.length) return null;
		return regions[index];
	}
	
	public int getRegionCount(){
		if(regions == null) return 0;
		return regions.length;
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
		regions_dirty = true;
	}
	
	public void clearAndReallocRegions(int regcount){
		//Clears EVERYTHING
		for(int i = 0; i < 64; i++){
			slots[i] = null;
			wave_ids[i] = 0;
		}
		regions = new ZeqerPercRegion[regcount];
		regions_dirty = true;
	}
	
	public int setRegion(int index, ZeqerPercRegion region){
		if(regions == null) return -1;
		if(index < 0 || index >= regions.length) return -1;
		regions[index] = region;
		
		if(region != null){
			int minnote = region.getMinNote();
			int maxnote = region.getMaxNote();
			for(int i = minnote; i <= maxnote; i++){
				slots[i] = region.getDrumData();
				if(slots[i] != null){
					Z64WaveInfo winfo = slots[i].getSample();
					if(winfo != null){
						wave_ids[i] = winfo.getUID();
					}
					else wave_ids[i] = 0;
				}
				else wave_ids[i] = 0;
			}
		}
		
		return index;
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

	public void consolidateRegions(){
		//If only slots are recorded, then combine these into regions
		//	for more streamlined serialization and user editing.
		if(!regions_dirty) return;
		if(slots == null) return;
		
		List<ZeqerPercRegion> templist = new LinkedList<ZeqerPercRegion>();
		ZeqerPercRegion lastreg = null;
		regions = null;
		char nchar = 'A';
		int lastwave = -1;
		for(int i = 0; i < slots.length; i++){
			if(slots[i] != null){
				if(lastreg != null){
					//See if eq. If not, new region.
					if((lastwave == wave_ids[i]) && slots[i].drumEquals(lastreg.getDrumData())){
						lastreg.setMaxNote(i + Z64Sound.STDRANGE_BOTTOM);
						continue;
					}
				}
				//new reg
				lastreg = new ZeqerPercRegion();
				templist.add(lastreg);
				lastreg.setMinNote(i + Z64Sound.STDRANGE_BOTTOM);
				lastreg.setMaxNote(i + Z64Sound.STDRANGE_BOTTOM);
				lastreg.setDrumData(slots[i]);
				lastreg.setRandomStringStems();
				lastwave = wave_ids[i];
				
				if(nchar <= 'Z'){
					lastreg.setEnumStem("PERCREG_" + nchar);
					lastreg.setNameStem("Percussion Instrument " + nchar);
					nchar++;
				}
				else{
					lastreg.setEnumStem("PERCREG_" + i);
					lastreg.setNameStem("Percussion Instrument " + i);
				}
			}
			else{
				lastreg = null;
				lastwave = -1;
			}
		}
		
		int rcount = templist.size();
		regions = new ZeqerPercRegion[rcount];
		int i = 0;
		for(ZeqerPercRegion r : templist) regions[i++] = r;
		templist.clear();
		
		regions_dirty = false;
	}
	
	public int estimateWriteBufferSize(){
		int sz = super.estimateWriteBufferSize();
		sz += 4;
		sz += 12 << 6;
		
		//String table
		consolidateRegions();
		sz += 4;
		
		if(regions != null){
			int rcount = regions.length;
			for(int i = 0; i < rcount; i++){
				if(regions[i] != null){
					String s = regions[i].getEnumStem();
					if(s != null) sz += s.length() + 4;
					else sz += 2;
					
					s = regions[i].getNameStem();
					if(s != null) sz += s.length() + 4;
					else sz += 2;
				}
				else sz += 4;
			}
		}
	
		return sz;
	}
	
	protected int serializeMe(FileBuffer buffer) {
		long init_size = buffer.getFileSize();
		int flags = 0x80000000;
		flags |= ZeqerPreset.PRESET_TYPE_PERC;
		
		if(!hashmode) buffer.addToFile(uid);
		else buffer.addToFile(0);
		buffer.addToFile(flags); //Flags
		
		//Version 4+
		consolidateRegions();
		int rcount = regions.length;
		buffer.addToFile((byte)rcount);
		buffer.add24ToFile(0);
		for(int i = 0; i < rcount; i++){
			ZeqerPercRegion reg = regions[i];
			if(reg != null){
				Z64Drum drum = reg.getDrumData();
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
					
					Z64Tuning tuning = drum.getTuning();
					buffer.addToFile(tuning.root_key);
					buffer.addToFile(tuning.fine_tune);
					buffer.addToFile(reg.getMinNote());
					buffer.addToFile(reg.getMaxNote());
				}
				else{
					//Empty slot
					buffer.addToFile((short)0);
					buffer.addToFile((short)-1);
					buffer.addToFile(-1);
					buffer.addToFile((byte)60);
					buffer.addToFile((byte)0);
					
					buffer.addToFile(reg.getMinNote());
					buffer.addToFile(reg.getMaxNote());
				}
			}
			else{
				//Empty slot
				buffer.addToFile((short)0);
				buffer.addToFile((short)-1);
				buffer.addToFile(-1);
				buffer.addToFile((byte)60);
				buffer.addToFile((byte)0);
				
				buffer.addToFile((byte)0);
				buffer.addToFile((byte)127);
			}
		}
		
		//String table (V4+)
		//First, need to calculate size.....
		int stsize = 0;
		for(int i = 0; i < rcount; i++){
			ZeqerPercRegion reg = regions[i];
			stsize += 4;
			if(reg != null){
				String s = reg.getEnumStem();
				if(s != null){
					stsize += s.length();
					if((stsize & 0x1) != 0) stsize++;
				}
				s = reg.getNameStem();
				if(s != null){
					stsize += s.length();
					if((stsize & 0x1) != 0) stsize++;
				}
			}
		}
		buffer.addToFile(stsize);
		
		//THEN write the actual strings...
		for(int i = 0; i < rcount; i++){
			ZeqerPercRegion reg = regions[i];
			if(reg != null){
				buffer.addVariableLengthString(reg.getEnumStem(), BinFieldSize.WORD, 2);
				buffer.addVariableLengthString(reg.getNameStem(), BinFieldSize.WORD, 2);
			}
			else{
				//Empty
				buffer.addToFile(0);
			}
		}
		
		//VERSIONS 3-
		/*
		
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
		*/
		
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
