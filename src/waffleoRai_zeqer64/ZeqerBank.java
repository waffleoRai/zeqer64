package waffleoRai_zeqer64;

import java.io.IOException;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileUtils;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_soundbank.nintendo.z64.Z64Bank;
import waffleoRai_zeqer64.filefmt.ZeqerBankTable.BankTableEntry;

public class ZeqerBank {
	
	/*----- Instance Variables -----*/
	
	private String dataPath; //bubnk/buwsd stem for loading and unloading
	private boolean writePerm = false;
	
	private BankTableEntry metaEntry;
	private Z64Bank data;
	
	/*----- Init -----*/
	
	public ZeqerBank(BankTableEntry entry, boolean readonly){
		metaEntry = entry;
		writePerm = !readonly;
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
		data = Z64Bank.readUBNK(buff);
		
		String wsdpath = dataPath + ".buwsd";
		if(FileBuffer.fileExists(wsdpath)){
			buff = FileBuffer.createBuffer(wsdpath, true);
			data.readUWSD(buff);
		}
		
		return data;
	}
	
	public void unloadBankData(){
		data = null;
	}
	
	public BankTableEntry saveAll() throws IOException{
		if(!writePerm) return null;
		if(data == null) return metaEntry;
		
		//Reserialize to get checksum
		FileBuffer ser = data.serializeMe();
		byte[] sdata = ser.getBytes(0, ser.getFileSize());
		byte[] md5 = FileUtils.getMD5Sum(sdata);
		ser = null; sdata = null;
		
		//Save bank data
		data.writeUFormat(dataPath);
		
		//Sync metadata between objects
		metaEntry.setCachePolicy(data.getCachePolicy());
		metaEntry.setMedium(data.getMedium());
		metaEntry.setInstCounts(data.getInstCount(), data.getPercCount(), data.getSFXCount());
		metaEntry.setWarcIndex(data.getPrimaryWaveArchiveIndex());
		metaEntry.setWarc2Index(data.getSecondaryWaveArchiveIndex());
		metaEntry.setMD5(md5); //This updates the modified timestamp
		
		return metaEntry;
	}

}
