package waffleoRai_zeqer64;

import java.io.IOException;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileUtils;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_soundbank.nintendo.z64.UltraBankFile;
import waffleoRai_soundbank.nintendo.z64.Z64Bank;
import waffleoRai_zeqer64.filefmt.bank.BankTableEntry;
import waffleoRai_zeqer64.iface.SoundSampleSource;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;

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
	public boolean isBankDataLoaded(){return data != null;}
	
	public Z64Bank getBankData(){
		if(data == null){
			try{
				loadBankData();
			}
			catch(Exception ex){
				ex.printStackTrace();
			}	
		}
		return data;
	}
	
	/*----- Setters -----*/
	
	public void setDataSaveStem(String val){dataPath = val;}
	
	public boolean setBankData(Z64Bank bnk) throws IOException{
		if(!writePerm) return false;
		data = bnk;
		return (saveAll() != null);
	}
	
	public ZeqerBank createUserDuplicate(ZeqerCoreInterface core) {
		if(core == null) return null;
		
		//1. Copy bank data
		if(!isBankDataLoaded()) {
			try {this.loadBankData();} 
			catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}
		Z64Bank datcopy = data.copy(true);
		
		//2. Generate meta record from the core
		ZeqerBank copy = core.addUserBank(datcopy);
		
		//3. Update name and enum
		if(metaEntry != null) {
			BankTableEntry otherMeta = copy.getTableEntry();
			metaEntry.copyTo(otherMeta);
			String s = metaEntry.getName();
			if(s != null) {
				otherMeta.setName(s + " (" + String.format("%08x)", otherMeta.getUID()));
			}
			s = metaEntry.getEnumString();
			if(s != null) {
				otherMeta.setEnumString(String.format("%s_%08x", s, otherMeta.getUID()));
			}
		}
		
		unloadBankData();
		return copy;
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
