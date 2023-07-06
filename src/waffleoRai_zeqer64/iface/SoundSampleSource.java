package waffleoRai_zeqer64.iface;

import java.util.Map;

import waffleoRai_Sound.nintendo.Z64Wave;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.FileBuffer;

public interface SoundSampleSource {
	
	//Slimmed down public interface for the core wave manager
	
	public Z64WaveInfo getWaveInfo(int wave_uid);
	public Z64WaveInfo getWaveByName(String wave_name);
	public Map<Integer, Z64WaveInfo> getAllInfoMappedByUID();
	
	public Z64Wave loadWave(int wave_uid);
	public FileBuffer loadWaveData(int wave_uid);

}
