package waffleoRai_zeqer64.presets;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.BinFieldSize;
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
	private String enm_str;
	
	private boolean hashmode = false;
	
	public ZeqerInstPreset(){inst = new Z64Instrument();}
	
	public ZeqerInstPreset(Z64Instrument i){
		inst = i;
		if(inst == null) inst = new Z64Instrument();
	}
	
	public String getName(){return inst.getName();}
	public String getEnumStringBase(){return enm_str;}
	public int getType(){return ZeqerPreset.PRESET_TYPE_INST;}
	public Z64Instrument getInstrument(){return inst;}
	public int getWaveIDLo(){return waveid_lo;}
	public int getWaveIDMid(){return waveid_mid;}
	public int getWaveIDHi(){return waveid_hi;}
	
	public Z64Envelope getEnvelope(){return inst.getEnvelope();}
	
	public List<Z64Envelope> getEnvelopes(){
		Z64Envelope env = inst.getEnvelope();
		if(env == null) return null;
		List<Z64Envelope> list = new ArrayList<Z64Envelope>(1);
		list.add(env);
		return list;
	}
	
	public void setName(String s){inst.setName(s);}
	public void setEnumStringBase(String s){enm_str = s;}
	public void setWaveIDLo(int val){waveid_lo = val;}
	public void setWaveIDMid(int val){waveid_mid = val;}
	public void setWaveIDHi(int val){waveid_hi = val;}
	
	public void setEnvelope(Z64Envelope env){
		inst.setEnvelope(env);
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
		
		if(enm_str == null){
			enm_str = String.format("IPRE_%08x", uid);
		}
		buffer.addVariableLengthString("UTF8", enm_str, BinFieldSize.WORD, 2);
		
		return (int)(buffer.getFileSize() - init_size);
	}

	public void exportTSVLine(Writer w) throws IOException{
		w.write(getName() + "\t");
		w.write(String.format("%08x\t", uid));
		
		if(enm_str == null){
			enm_str = String.format("IPRE_%08x", uid);
		}
		w.write(enm_str + "\t");
		
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
