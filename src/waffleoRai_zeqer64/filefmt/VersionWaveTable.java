package waffleoRai_zeqer64.filefmt;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import waffleoRai_Utils.FileBuffer;

public class VersionWaveTable {
	
	private static class SampleEntry{
		public int id = 0;
		public int offset = -1; //From bank start
	}
	
	private static class SampleBankEntry{
		public int refId = -1;
		public SampleEntry[] samples = null;
	}
	
	private SampleBankEntry[] sampleBanks;
	
	public VersionWaveTable(int warcCount){
		sampleBanks = new SampleBankEntry[warcCount];
		for(int i = 0; i < sampleBanks.length; i++){
			sampleBanks[i] = new SampleBankEntry();
		}
	}
	
	public static VersionWaveTable readIn(String path) throws IOException{
		if(path == null) return null;
		FileBuffer data = FileBuffer.createBuffer(path, true);
		data.setCurrentPosition(0L);
		
		int warcCount = Short.toUnsignedInt(data.nextShort());
		VersionWaveTable tbl = new VersionWaveTable(warcCount);
		for(int i = 0; i < warcCount; i++){
			int flags = Short.toUnsignedInt(data.nextShort());
			boolean isref = (flags & 0x1) != 0;
			if(!isref){
				int scount = Short.toUnsignedInt(data.nextShort());
				tbl.sampleBanks[i].samples = new SampleEntry[scount];
				tbl.sampleBanks[i].refId = -1;
				for(int j = 0; j < scount; j++){
					SampleEntry samp = new SampleEntry();
					samp.id = data.nextInt();
					samp.offset = data.nextInt();
					tbl.sampleBanks[i].samples[j] = samp;
				}
			}
			else{
				tbl.sampleBanks[i].samples = null;
				tbl.sampleBanks[i].refId = Short.toUnsignedInt(data.nextShort());
			}
		}
		
		return tbl;
	}
	
	public void writeTo(String path) throws IOException{
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path));
		int warcCount = sampleBanks.length;
		bos.write((warcCount >>> 8) & 0xff);
		bos.write(warcCount & 0xff);
		for(int i = 0; i < warcCount; i++){
			if(sampleBanks[i].refId < 0){
				//Not a ref
				if(sampleBanks[i].samples == null){
					bos.write(new byte[]{0,0,0,0});
				}
				else{
					int scount = sampleBanks[i].samples.length;
					FileBuffer buff = new FileBuffer((scount << 2) + 4, true);
					buff.addToFile((short)0);
					buff.addToFile((short)scount);
					for(int j = 0; j < scount; j++){
						buff.addToFile(sampleBanks[i].samples[j].id);
						buff.addToFile(sampleBanks[i].samples[j].offset);
					}
					buff.writeToStream(bos);
				}
			}
			else{
				//Ref
				FileBuffer buff = new FileBuffer(4, true);
				buff.addToFile((short)0x1);
				buff.addToFile((short)sampleBanks[i].refId);
				buff.writeToStream(bos);
			}
		}
		bos.close();
	}
	
	public int getWArcCount(){return sampleBanks.length;}
	
	public boolean isWArcReference(int index){
		if(index < 0) return false;
		if(index >= sampleBanks.length) return false;
		return(sampleBanks[index].refId >= 0);
	}
	
	public int getWArcReference(int index){
		if(index < 0) return -1;
		if(index >= sampleBanks.length) return -1;
		return sampleBanks[index].refId;
	}
	
	public int getWArcSampleCount(int index){
		if(index < 0) return -1;
		if(index >= sampleBanks.length) return -1;
		if(sampleBanks[index].samples == null) return 0;
		return sampleBanks[index].samples.length;
	}
	
	public int[] getSampleInfo(int warc, int idx){
		if(warc < 0 || warc >= sampleBanks.length) return null;
		if(idx < 0) return null;
		
		SampleBankEntry wb = sampleBanks[warc];
		while(wb.refId >= 0){
			wb = sampleBanks[wb.refId];
		}
		
		if(wb.samples == null) return null;
		if(idx >= wb.samples.length) return null;
		
		return new int[]{wb.samples[idx].id, wb.samples[idx].offset};
	}
	
	public void allocWarc(int index, int samples){
		if(index < 0 || index >= sampleBanks.length) return;
		sampleBanks[index].refId = -1;
		sampleBanks[index].samples = new SampleEntry[samples];
		for(int i = 0; i < samples; i++){
			sampleBanks[index].samples[i] = new SampleEntry();
		}
	}
	
	public void setWarcAsReference(int index, int refId){
		if(index < 0 || index >= sampleBanks.length) return;
		sampleBanks[index].refId = refId;
		sampleBanks[index].samples = null;
	}
	
	public void setSampleInfo(int warc, int idx, int waveid, int offset){
		if(warc < 0 || warc >= sampleBanks.length) return;
		if(idx < 0) return;
		
		SampleBankEntry wb = sampleBanks[warc];
		while(wb.refId >= 0){
			wb = sampleBanks[wb.refId];
		}
		
		if(wb.samples == null) return;
		if(idx >= wb.samples.length) return;
		
		wb.samples[idx].id = waveid;
		wb.samples[idx].offset = offset;
	}

}
