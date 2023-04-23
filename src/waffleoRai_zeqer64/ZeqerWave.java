package waffleoRai_zeqer64;

import java.io.IOException;

import waffleoRai_Sound.nintendo.Z64Wave;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileUtils;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_zeqer64.filefmt.UltraWavFile;
import waffleoRai_zeqer64.filefmt.ZeqerWaveTable.WaveTableEntry;

public class ZeqerWave {
	
	/*----- Instance Variables -----*/
	
	private String dataPath; //buwav path for loading and unloading
	private boolean writePerm = false;
	
	private WaveTableEntry metaEntry;
	private Z64Wave soundData = null;
	
	/*----- Init -----*/
	
	public ZeqerWave(WaveTableEntry tableEntry, boolean readonly){
		metaEntry = tableEntry;
		writePerm = !readonly;
	}
	
	/*----- Getters -----*/
	
	public WaveTableEntry getTableEntry(){return metaEntry;}
	public Z64Wave getSoundData(){return soundData;}
	public boolean isSoundDataLoaded(){return soundData != null;}
	
	/*----- Setters -----*/
	
	public void setDataPath(String val){dataPath = val;}
	
	public WaveTableEntry loadSoundData(FileBuffer data) throws IOException{
		if(!writePerm) return null;
		soundData = Z64Wave.readZ64Wave(data, metaEntry.getWaveInfo());
		return saveAll();
	}
	
	/*----- Management -----*/
	
	public Z64Wave loadSoundData() throws IOException, UnsupportedFileTypeException{
		if(dataPath == null) return null;
		if(!FileBuffer.fileExists(dataPath)) return null;
		
		UltraWavFile uwav = UltraWavFile.createUWAV(dataPath);
		uwav.readWaveInfo(metaEntry.getWaveInfo());
		soundData = Z64Wave.readZ64Wave(uwav.loadSoundData(), metaEntry.getWaveInfo());
		
		return soundData;
	}
	
	public void unloadSoundData(){
		soundData = null;
	}
	
	public WaveTableEntry saveAll() throws IOException{
		if(!writePerm) return metaEntry;
		
		//This also updates the entry w/ new MD5
		if(soundData != null){
			byte[] sdat = soundData.getRawData();
			byte[] md5 = FileUtils.getMD5Sum(sdat);
			metaEntry.setMD5(md5);
			
			if(dataPath != null){
				UltraWavFile.writeUWAV(dataPath, metaEntry.getWaveInfo(), FileBuffer.wrap(sdat));
			}
		}
		
		return metaEntry;
	}

}
