package waffleoRai_zeqer64;

import java.io.IOException;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileUtils;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_soundbank.nintendo.z64.UltraBankFile;
import waffleoRai_soundbank.nintendo.z64.Z64Bank;
import waffleoRai_zeqer64.filefmt.ZeqerBankTable.BankTableEntry;
import waffleoRai_zeqer64.iface.SoundSampleSource;

public class ZeqerBank {
	
	/*----- Instance Variables -----*/
	
	private String dataPath; //bubnk/buwsd stem for loading and unloading
	private boolean writePerm = false;
	
	private SoundSampleSource sampleSrc;
	
	private BankTableEntry metaEntry;
	private Z64Bank data;
	
	/*----- Init -----*/
	
	public ZeqerBank(BankTableEntry entry, SoundSampleSource sampleSource, boolean readonly){
		metaEntry = entry;
		writePerm = !readonly;
		sampleSrc = sampleSource;
	}
	
	/*----- Getters -----*/
	
	public BankTableEntry getTableEntry(){return metaEntry;}
	public Z64Bank getBankData(){return data;}
	public boolean isBankDataLoaded(){return data != null;}
	
	/*----- Setters -----*/
	
	public void setDataSaveStem(String val){dataPath = val;}
	
	public boolean setBankData(Z64Bank bnk) throws IOException{
		if(!writePerm) return false;
		data = bnk;
		return (saveAll() != null);
	}
	
	/*----- Management -----*/
	
	public Z64Bank loadBankData() throws UnsupportedFileTypeException, IOException{
		if(dataPath == null) return null;
		String bnkpath = dataPath + ".bubnk";
		if(!FileBuffer.fileExists(bnkpath)) return null;
		
		FileBuffer buff = FileBuffer.createBuffer(bnkpath, true);
		UltraBankFile ubnk = UltraBankFile.open(buff);
		data = ubnk.read();
		
		String wsdpath = dataPath + ".buwsd";
		if(FileBuffer.fileExists(wsdpath)){
			buff = FileBuffer.createBuffer(wsdpath, true);
			UltraBankFile uwsd = UltraBankFile.open(buff);
			uwsd.readTo(data);
			uwsd.close();
		}
		
		//Link samples
		if(sampleSrc != null){
			ubnk.linkWaves(data, sampleSrc.getAllInfoMappedByUID());
		}

		ubnk.close();
		return data;
	}
	
	public void unloadBankData(){
		data = null;
	}
	
	public BankTableEntry saveAll() throws IOException{
		if(!writePerm) return null;
		if(data == null) return metaEntry;
		
		//Reserialize to get checksum
		FileBuffer ser = data.serializeMe(Z64Bank.SEROP_REF_WAV_UIDS);
		byte[] sdata = ser.getBytes(0, ser.getFileSize());
		byte[] md5 = FileUtils.getMD5Sum(sdata);
		ser = null; sdata = null;
		
		//Save bank data
		UltraBankFile.writeUBNK(data, dataPath + ".bubnk", UltraBankFile.OP_LINK_WAVES_UID);
		int sfxCount = data.getEffectiveSFXCount();
		if(sfxCount > 0){
			UltraBankFile.writeUWSD(data, dataPath + ".buwsd", UltraBankFile.OP_LINK_WAVES_UID);
		}
		
		//Sync metadata between objects
		metaEntry.setCachePolicy(data.getCachePolicy());
		metaEntry.setMedium(data.getMedium());
		metaEntry.setInstCounts(data.getEffectiveInstCount(), data.getEffectivePercCount(), sfxCount);
		metaEntry.setWarcIndex(data.getPrimaryWaveArcIndex());
		metaEntry.setWarc2Index(data.getSecondaryWaveArcIndex());
		metaEntry.setMD5(md5); //This updates the modified timestamp
		
		return metaEntry;
	}

}
