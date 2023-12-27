package waffleoRai_zeqer64.presets;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.BinFieldSize;
import waffleoRai_Utils.BufferReference;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileUtils;
import waffleoRai_soundbank.nintendo.z64.Z64Envelope;
import waffleoRai_soundbank.nintendo.z64.Z64Instrument;
import waffleoRai_zeqer64.ZeqerPreset;

public class ZeqerInstPreset extends ZeqerPreset{
	
	private Z64Instrument inst;
	private int waveid_lo;
	private int waveid_mid;
	private int waveid_hi;
	
	private boolean hashmode = false;
	
	public ZeqerInstPreset(){
		inst = new Z64Instrument();
		inst.setIDRandom();
	}
	
	public ZeqerInstPreset(int guid){
		inst = new Z64Instrument();
		super.uid = guid;
	}
	
	public ZeqerInstPreset(Z64Instrument i){
		inst = i;
		if(inst == null){
			inst = new Z64Instrument();
			inst.setIDRandom();
		}
	}
	
	public String getName(){return inst.getName();}
	public int getType(){return ZeqerPreset.PRESET_TYPE_INST;}
	public Z64Instrument getInstrument(){return inst;}
	public int getWaveIDLo(){return waveid_lo;}
	public int getWaveIDMid(){return waveid_mid;}
	public int getWaveIDHi(){return waveid_hi;}
	
	public Z64Envelope getEnvelope(){return inst.getEnvelope();}
	
	public long readIn(BufferReference src, Z64Envelope[] envs, int version){
		if(src == null) return 0;
		if(version < 1) return 0;
		
		if(inst == null) inst = new Z64Instrument();
		
		long stpos = src.getBufferPosition();
		src.add(1L); //Reserved
		inst.setLowRangeTop(src.nextByte());
		inst.setHighRangeBottom(src.nextByte());
		inst.setDecay(src.nextByte());
		src.add(2L); //Reserved
		
		int envidx = (int)src.nextShort();
		if(envidx >= 0 && envs != null && envidx < envs.length){
			inst.setEnvelope(envs[envidx]);
		}
		else inst.setEnvelope(null);
		
		waveid_lo = src.nextInt();
		inst.setSampleLow(null);
		inst.setTuningLow(Float.intBitsToFloat(src.nextInt()));
		
		waveid_mid = src.nextInt();
		inst.setSampleMiddle(null);
		inst.setTuningMiddle(Float.intBitsToFloat(src.nextInt()));
		
		waveid_hi = src.nextInt();
		inst.setSampleHigh(null);
		inst.setTuningHigh(Float.intBitsToFloat(src.nextInt()));
		
		if(version >= 3 && version < 6){
			enumLabel = src.nextVariableLengthString(BinFieldSize.WORD, 2);
		}
		
		return src.getBufferPosition() - stpos;
	}
	
	public List<Z64Envelope> getEnvelopes(){
		Z64Envelope env = inst.getEnvelope();
		if(env == null) return null;
		List<Z64Envelope> list = new ArrayList<Z64Envelope>(1);
		list.add(env);
		return list;
	}
	
	public ZeqerInstPreset copy(){
		ZeqerInstPreset mycopy = new ZeqerInstPreset();
		super.copyTo(mycopy);
		
		//Modify identifiers a little
		mycopy.enumLabel = mycopy.enumLabel + "_COPY";
		mycopy.uid++;
		
		mycopy.waveid_hi = this.waveid_hi;
		mycopy.waveid_mid = this.waveid_mid;
		mycopy.waveid_lo = this.waveid_lo;
		
		//Uses instrument name
		mycopy.inst = this.inst.copy();
		mycopy.inst.setName(this.getName() + " (Copy)");
		mycopy.name = mycopy.inst.getName();
		
		return mycopy;
	}
	
	public void setName(String s){inst.setName(s);}
	public void setWaveIDLo(int val){waveid_lo = val;}
	public void setWaveIDMid(int val){waveid_mid = val;}
	public void setWaveIDHi(int val){waveid_hi = val;}
	
	public void setEnvelope(Z64Envelope env){
		inst.setEnvelope(env);
	}
	
	public void loadData(Z64Instrument src){
		inst.setDecay(src.getDecay());
		inst.setEnvelope(src.getEnvelope());
		inst.setHighRangeBottom(src.getHighRangeBottom());
		inst.setLowRangeTop(src.getLowRangeTop());
		inst.setTuningHigh(src.getTuningHigh());
		inst.setTuningMiddle(src.getTuningMiddle());
		inst.setTuningLow(src.getTuningLow());
		
		Z64WaveInfo winfo = src.getSampleMiddle();
		inst.setSampleMiddle(winfo);
		if(winfo != null) waveid_mid = winfo.getUID();
		
		winfo = src.getSampleLow();
		inst.setSampleLow(winfo);
		if(winfo != null) waveid_lo = winfo.getUID();
		
		winfo = src.getSampleHigh();
		inst.setSampleHigh(winfo);
		if(winfo != null) waveid_hi = winfo.getUID();
	}
	
	public int hashToUID(){
		FileBuffer buff = new FileBuffer(64, true);
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
	
	public int estimateWriteBufferSize(){
		int sz = super.estimateWriteBufferSize();
		sz += 32;
		
		sz += 2; //Enum label size field
		if(enumLabel != null) {sz += enumLabel.length() + 2;}
		
		return sz;
	}
	
	protected int serializeMe(FileBuffer buffer){
		long init_size = buffer.getFileSize();
		int flags = 0x80000000;
		flags |= ZeqerPreset.PRESET_TYPE_INST;
		
		if(!hashmode) buffer.addToFile(uid);
		else buffer.addToFile(0);
		
		buffer.addToFile(flags); //Flags
		buffer.addToFile(FileBuffer.ZERO_BYTE); //Reserved
		buffer.addToFile(inst.getLowRangeTop());
		buffer.addToFile(inst.getHighRangeBottom());
		buffer.addToFile(inst.getDecay());
		buffer.addToFile((short)0); //Reserved
		
		Z64Envelope env = inst.getEnvelope();
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
		
		Z64WaveInfo winfo = inst.getSampleLow();
		if(winfo != null){
			buffer.addToFile(winfo.getUID());
			buffer.addToFile(Float.floatToRawIntBits(inst.getTuningLow()));
		}
		else{
			buffer.addToFile(waveid_lo);
			if(waveid_lo == 0) buffer.addToFile(0);
			else buffer.addToFile(Float.floatToRawIntBits(inst.getTuningLow()));
		}
		
		winfo = inst.getSampleMiddle();
		if(winfo != null){
			buffer.addToFile(winfo.getUID());
			buffer.addToFile(Float.floatToRawIntBits(inst.getTuningMiddle()));
		}
		else{
			buffer.addToFile(waveid_mid);
			if(waveid_mid == 0) buffer.addToFile(0);
			else buffer.addToFile(Float.floatToRawIntBits(inst.getTuningMiddle()));
		}
		
		winfo = inst.getSampleHigh();
		if(winfo != null){
			buffer.addToFile(winfo.getUID());
			buffer.addToFile(Float.floatToRawIntBits(inst.getTuningHigh()));
		}
		else{
			buffer.addToFile(waveid_hi);
			if(waveid_hi == 0) buffer.addToFile(0);
			else buffer.addToFile(Float.floatToRawIntBits(inst.getTuningHigh()));
		}
		
		if(enumLabel == null){
			enumLabel = String.format("IPRE_%08x", uid);
		}
		//buffer.addVariableLengthString("UTF8", enumLabel, BinFieldSize.WORD, 2);
		
		return (int)(buffer.getFileSize() - init_size);
	}

	public void exportTSVLine(Writer w) throws IOException{
		w.write(getName() + "\t");
		w.write(String.format("%08x\t", uid));
		
		if(enumLabel == null){
			enumLabel = String.format("IPRE_%08x", uid);
		}
		w.write(enumLabel + "\t");
		
		Z64Envelope env = inst.getEnvelope();
		if(env == null) w.write("-1\t");
		else w.write(env.getID() + "\t");
		w.write(inst.getDecay() + "\t");
		w.write(inst.getLowRangeTop() + "\t");
		
		Z64WaveInfo winfo = inst.getSampleLow();
		if(winfo == null){
			if(waveid_lo == 0 || waveid_lo == -1) w.write("N/A\tN/A\t");
			else{
				w.write(String.format("%08x\t", waveid_lo));
				w.write(inst.getTuningLow() + "\t");
			}
		}
		else{ 
			w.write(String.format("%08x\t", winfo.getUID()));
			w.write(inst.getTuningLow() + "\t");
		}
		
		winfo = inst.getSampleMiddle();
		if(winfo == null){
			if(waveid_mid == 0 || waveid_mid == -1) w.write("N/A\tN/A\t");
			else{
				w.write(String.format("%08x\t", waveid_mid));
				w.write(inst.getTuningMiddle() + "\t");
			}
		}
		else{ 
			w.write(String.format("%08x\t", waveid_mid));
			w.write(inst.getTuningMiddle() + "\t");
		}
		
		w.write(inst.getHighRangeBottom() + "\t");
		winfo = inst.getSampleHigh();
		if(winfo == null){
			if(waveid_hi == 0 || waveid_hi == -1) w.write("N/A\tN/A\t");
			else{
				w.write(String.format("%08x\t", waveid_hi));
				w.write(inst.getTuningHigh() + "\t");
			}
		}
		else{ 
			w.write(String.format("%08x\t", winfo.getUID()));
			w.write(inst.getTuningHigh() + "\t");
		}
		
		if(!tags.isEmpty()){
			boolean first = true;
			for(String tag : tags){
				if(!first)w.write(";");
				w.write(tag);
				first = false;
			}
			w.write("\n");
		}
		else w.write("<None>\n");
		
	}
	
}
