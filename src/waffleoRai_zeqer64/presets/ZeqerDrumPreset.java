package waffleoRai_zeqer64.presets;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import waffleoRai_Sound.nintendo.Z64Sound.Z64Tuning;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.BinFieldSize;
import waffleoRai_Utils.BufferReference;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileUtils;
import waffleoRai_soundbank.nintendo.z64.Z64Drum;
import waffleoRai_soundbank.nintendo.z64.Z64Envelope;
import waffleoRai_zeqer64.ZeqerPreset;

public class ZeqerDrumPreset extends ZeqerPreset{
	
	private Z64Drum data;
	private int waveid = 0; //For linking
	
	public ZeqerDrumPreset(){
		data = new Z64Drum();
	}
	
	public ZeqerDrumPreset(int guid){
		uid = guid;
	}
	
	public ZeqerDrumPreset(Z64Drum drum){
		data = drum;
		if(data == null){
			data = new Z64Drum();
		}
	}
	
	public String getName(){return data.getName();}
	public int getType(){return ZeqerPreset.PRESET_TYPE_DRUM;}
	public Z64Drum getData(){return data;}
	public int getWaveID(){return waveid;}
	
	public Z64Envelope getEnvelope(){return data.getEnvelope();}
	
	public List<Z64Envelope> getEnvelopes(){
		Z64Envelope env = data.getEnvelope();
		if(env == null) return null;
		List<Z64Envelope> list = new ArrayList<Z64Envelope>(1);
		list.add(env);
		return list;
	}
	
	public void setName(String s){data.setName(s);}
	public void setWaveID(int val){waveid = val;}
	
	public void setEnvelope(Z64Envelope env){
		data.setEnvelope(env);
	}
	
	public void setData(Z64Drum drum){
		data = drum;
	}
	
	public void loadDrumData(Z64Drum drum){
		//This one creates a drum COPY.
		data = new Z64Drum();
		if(drum == null) return;
		
		data.setDecay(drum.getDecay());
		data.setPan(drum.getPan());
		
		Z64Envelope env = drum.getEnvelope();
		if(env != null){
			data.setEnvelope(env);
		}
		else data.setEnvelope(Z64Envelope.newDefaultEnvelope());
		
		Z64WaveInfo winfo = drum.getSample();
		if(winfo != null){
			data.setSample(winfo);
			waveid = winfo.getUID();
		}
		else{
			data.setSample(null);
			waveid = 0;
		}
		
		String n = drum.getName();
		if(n != null) data.setName(n);
		else{
			data.setName(String.format("DPRE_%08x", uid));
		}
		
		data.setRootKey(drum.getRootKey());
		data.setFineTune(drum.getFineTune());
	}
	
	public int hashToUID(){
		if(data == null) return 0;
		FileBuffer buff = new FileBuffer(64, true);
		buff.addToFile(data.getDecay());
		buff.addToFile(data.getPan());
		buff.addToFile(data.getTuning().root_key);
		buff.addToFile(data.getTuning().fine_tune);
		
		Z64WaveInfo winfo = data.getSample();
		if(winfo != null){
			buff.addToFile(winfo.getUID());
		}
		else{
			buff.addToFile(waveid);
		}
		
		Z64Envelope env = data.getEnvelope();
		if(env != null){
			List<short[]> events = env.getEvents();
			for(short[] e : events){
				buff.addToFile(e[0]);
				buff.addToFile(e[1]);
			}
		}
		
		byte[] raw = buff.getBytes(0, buff.getFileSize());
		byte[] md5 = FileUtils.getMD5Sum(raw);
		
		int hash = 0;
		for(int i = 0; i < 4; i++){
			hash <<= 8;
			hash |= Byte.toUnsignedInt(md5[i]);
		}
		
		return hash;
	}
	
	public long readIn(BufferReference src, Z64Envelope[] envs, int version){
		if(src == null) return 0;
		if(version < 5) return 0;
		
		long stpos = src.getBufferPosition();
		if(data == null){data = new Z64Drum();}
		data.setDecay(src.nextByte());
		data.setPan(src.nextByte());
		
		int envidx = (int)src.nextShort();
		if(envidx >= 0 && envs != null && (envidx < envs.length)){
			data.setEnvelope(envs[envidx]);
		}
		waveid = src.nextInt();
		
		Z64Tuning tuning = new Z64Tuning();
		tuning.root_key = src.nextByte();
		tuning.fine_tune = src.nextByte();
		data.setTuning(tuning);
		
		if(version < 6){
			enumLabel = src.nextVariableLengthString(BinFieldSize.WORD, 2);
		}

		return src.getBufferPosition() - stpos;
	}
	
	public int estimateWriteBufferSize(){
		int sz = super.estimateWriteBufferSize();
		sz += 12;
		
		if(enumLabel != null) {sz += enumLabel.length() + 2;}
		
		return sz;
	}
	
	protected int serializeMe(FileBuffer buffer){
		long init_size = buffer.getFileSize();
		int flags = 0x80000000;
		flags |= ZeqerPreset.PRESET_TYPE_DRUM;
		
		buffer.addToFile(uid);
		buffer.addToFile(flags);
		
		if(this.data != null){
			buffer.addToFile(data.getDecay());
			buffer.addToFile(data.getPan());
			
			Z64Envelope env = data.getEnvelope();
			if(env != null) buffer.addToFile((short)env.getID());
			else buffer.addToFile((short)-1);
			
			Z64WaveInfo winfo = data.getSample();
			if(winfo != null){
				waveid = winfo.getUID();
			}
			buffer.addToFile(waveid);
			
			Z64Tuning tune = data.getTuning();
			if(tune != null){
				buffer.addToFile(tune.root_key);
				buffer.addToFile(tune.fine_tune);
			}
			else{
				buffer.addToFile((byte)60);
				buffer.addToFile((byte)0);
			}
		}
		else{
			buffer.addToFile((short)0);
			buffer.addToFile((short)-1);
			buffer.addToFile(0);
			buffer.addToFile((byte)60);
			buffer.addToFile((byte)0);
		}
		
		/*if(enumLabel != null){
			buffer.addVariableLengthString(enumLabel, BinFieldSize.WORD, 2);
		}
		else buffer.addToFile((short)0);*/
		
		return (int)(buffer.getFileSize() - init_size);
	}
	
	public void exportTSVLine(Writer w) throws IOException{
		//#NAME	UID	ENUM	ENVELOPE_IDX	DECAY	WAVE UNITY_KEY FINE_TUNE TAGS
		if(w == null) return;
		if(data == null) return;
		
		w.write(getName() + '\t');
		w.write(String.format("%08x\t", uid));
		if(enumLabel == null){
			enumLabel = String.format("DPRE_%08x", uid);
		}
		w.write(enumLabel + "\t");
		
		Z64Envelope env = data.getEnvelope();
		if(env != null){
			w.write(env.getID() + "\t");
		}
		else w.write("-1\t");
		
		w.write(data.getDecay() + "\t");
		
		Z64WaveInfo winfo = data.getSample();
		if(winfo != null){
			w.write(String.format("%08x\t", winfo.getUID()));
		}
		else{
			w.write(String.format("%08x\t", waveid));
		}
		
		Z64Tuning tune = data.getTuning();
		if(tune == null){
			tune = new Z64Tuning();
			data.setTuning(tune);
		}
		
		w.write(tune.root_key + "\t");
		w.write(tune.fine_tune + "\t");
		
		if(super.tags == null || super.tags.isEmpty()){
			w.write("<None>\n");
		}
		else{
			boolean first = true;
			for(String tag : super.tags){
				if(!first)w.write(';');
				else{
					w.write(tag);
					first = false;
				}
			}
			w.write('\n');
		}
	}

}
