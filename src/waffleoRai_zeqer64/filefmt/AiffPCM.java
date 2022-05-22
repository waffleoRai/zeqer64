package waffleoRai_zeqer64.filefmt;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import waffleoRai_Sound.BitDepth;
import waffleoRai_Sound.RandomAccessSound;
import waffleoRai_Sound.Sound;
import waffleoRai_SoundSynth.AudioSampleStream;
import waffleoRai_SoundSynth.soundformats.PCMSampleStream;
import waffleoRai_Utils.FileBuffer;

class AiffPCM implements RandomAccessSound{

	private AiffFile source;
	
	public AiffPCM(AiffFile src){
		source = src;
	}

	public AudioFormat getFormat() {
		int ch = source.getChannelCount();
		int bd = source.getBitDepth();
		AudioFormat aifformat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, (float)source.getSampleRate(), 
				bd, 
				ch, (bd/8) * ch,
				(float)source.getSampleRate(), true);
		return aifformat;
	}

	@Override
	public AudioInputStream getStream() {
		// TODO Auto-generated method stub
		return null;
	}

	public AudioSampleStream createSampleStream() {
		return createSampleStream(false);
	}

	public AudioSampleStream createSampleStream(boolean loop) {
		return new PCMSampleStream(this, loop);
	}

	public void setActiveTrack(int tidx) {}
	public int countTracks() {return 1;}
	public int totalFrames() {return source.getFrameCount();}
	public int totalChannels() {return source.getChannelCount();}

	@Override
	public Sound getSingleChannel(int channel) {
		// TODO Auto-generated method stub
		return null;
	}

	public int[] getRawSamples(int channel) {
		int fcount = 0; //for realz
		FileBuffer sdat = source.getSoundData();
		int bd = source.getBitDepth();
		int cc = source.getChannelCount();
		fcount = (int)sdat.getFileSize() / cc;
		fcount /= (bd >>> 3);
		int[] output = new int[fcount];
		int i = channel;
		
		switch(bd){
		case 8:
			for(int f = 0; f < fcount; f++){
				output[f] = (int)sdat.intFromFile(i);
				i+=cc;
			}
			break;
		case 16:
			for(int f = 0; f < fcount; f++){
				output[f] = (int)sdat.shortFromFile(i);
				i+=cc << 1;
			}
			break;
		case 24:
			for(int f = 0; f < fcount; f++){
				output[f] = (sdat.shortishFromFile(i) << 8) >> 8;
				i+=cc*3;
			}
			break;
		}
		return output;
	}

	public int[] getSamples_16Signed(int channel) {
		int[] raw = getRawSamples(channel);
		if(raw == null) return null;
		switch(source.getBitDepth()){
		case 8:
			for(int i = 0; i < raw.length; i++){
				raw[i] >>= 8;
			}
			return raw;
		case 16:
			return raw;
		case 24:
			for(int i = 0; i < raw.length; i++){
				raw[i] <<= 8;
			}
			return raw;
		}
		return null;
	}

	public int[] getSamples_24Signed(int channel) {
		int[] raw = getRawSamples(channel);
		if(raw == null) return null;
		switch(source.getBitDepth()){
		case 8:
			for(int i = 0; i < raw.length; i++){
				raw[i] >>= 16;
			}
			return raw;
		case 16:
			for(int i = 0; i < raw.length; i++){
				raw[i] >>= 8;
			}
			return raw;
		case 24:
			return raw;
		}
		return null;
	}

	public BitDepth getBitDepth() {
		switch(source.getBitDepth()){
		case 8: return BitDepth.EIGHT_BIT_UNSIGNED;
		case 16: return BitDepth.SIXTEEN_BIT_SIGNED;
		case 24: return BitDepth.TWENTYFOUR_BIT_SIGNED;
		default: return null;
		}
	}

	public int getSampleRate() {return (int)Math.round(source.getSampleRate());}
	public boolean loops() {return source.getLoopStart() >= 0;}
	public int getLoopFrame() {return source.getLoopStart();}
	public int getLoopEndFrame() {return source.getLoopEnd();}
	public int getUnityNote() {return 60;}
	public int getFineTune() {return 0;}

	public int getSample(int channel, int frame) {
		int bpf = source.getBitDepth()/8;
		int bcpf = bpf*source.getChannelCount();
		int off =(bcpf*frame)+(channel*bpf);
		FileBuffer dat = source.getSoundData();
		
		int val = 0;
		for(int i = 0; i < bpf; i++){
			val <<= 8;
			val |= Byte.toUnsignedInt(dat.getByte(off+i));
		}
		
		//Sign extend
		int exbits = (4 - bpf) << 3;
		if(exbits > 0){
			val <<= exbits;
			val >>= exbits;
		}
		
		return val;
	}

	public byte[] frame2Bytes(int frame) {
		int bpf = source.getBitDepth()/8;
		int bcpf = bpf*source.getChannelCount();
		int off = bcpf*frame;
		FileBuffer dat = source.getSoundData();
		
		byte[] output = new byte[bcpf];
		for(int i = 0; i < bcpf; i++){
			output[i] = dat.getByte(off+i);
		}
		
		return output;
	}
	
}
