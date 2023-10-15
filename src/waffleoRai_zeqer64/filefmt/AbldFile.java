package waffleoRai_zeqer64.filefmt;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import waffleoRai_Utils.BinFieldSize;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.MultiFileBuffer;
import waffleoRai_Utils.SerializedString;
import waffleoRai_zeqer64.SoundTables.*;
import waffleoRai_zeqer64.ZeqerRom;
import waffleoRai_zeqer64.engine.EngineBankInfo;
import waffleoRai_zeqer64.engine.EngineSeqInfo;
import waffleoRai_zeqer64.engine.EngineWaveArcInfo;

public class AbldFile {

	public static final String MAGIC = "aBld";
	public static final String EXT = "abld";
	
	public static final short VERSION = 2;
	
	public static final String[][] WARC_NAMES_Z5 = {{"Main Effects Bank", "SAMPLEBANK_MAINFX"}, {"Main Music Bank", "SAMPLEBANK_MAIN_MUSIC"},
			{"Deku Tree", "SAMPLEBANK_DEKU_TREE"}, {"Jabu-Jabu", "SAMPLEBANK_JABUJABU"},
			{"Forest Temple (Unused)", "SAMPLEBANK_FOREST_TEMPLE"}, {"Goron City","SAMPLEBANK_GORON"}, {"Spirit Temple","SAMPLEBANK_SPIRIT_TEMPLE"}};
	public static final String[][] WARC_NAMES_Z6 = {{"Main Effects Bank", "SAMPLEBANK_MAINFX"}, 
			{"Main Music Bank", "SAMPLEBANK_MAIN_MUSIC"}, {"Goron Music", "SAMPLEBANK_GORON"}};
	public static final int[] Z5_WARC4_SAMPLES = {0xd4820de5, 0xaebb9249, 0x72d2d140, 0x0948d264, 0x43f029df};
	
	/*----- Instance Variables -----*/
	
	//-> audiobank
	private ArrayList<EngineBankInfo> banks;
	
	//-> audioseq
	private ArrayList<EngineSeqInfo> seqs;
	
	//-> audiotable
	private ArrayList<EngineWaveArcInfo> warcs;
	
	//-> settings
	private int tv_type = ZeqerRom.TV_TYPE__NTSC;
	private boolean oot_mode = false;
	private boolean sys_build = false; //Flagged on load, not stored in file
	private String name;
	private String baserom;
	
	private ZonedDateTime date_created;
	private ZonedDateTime date_modified;
	
	/*----- Init -----*/
	
	private AbldFile(){}
	
	public static AbldFile newABLD(){
		return newABLD("abld_" + getDateTimeString());
	}
	
	public static AbldFile newABLD(String buildname){
		AbldFile abld = new AbldFile();
		abld.date_created = ZonedDateTime.now();
		//abld.name = "abld_" + getDateTimeString();
		abld.name = buildname;
		abld.banks = new ArrayList<EngineBankInfo>(48);
		abld.seqs = new ArrayList<EngineSeqInfo>(128);
		abld.warcs = new ArrayList<EngineWaveArcInfo>(8);
		abld.date_modified = ZonedDateTime.now();
		return abld;
	}
	
	public static AbldFile newInjectionABLD(String buildname, String baserom){
		AbldFile abld = new AbldFile();
		abld.date_created = ZonedDateTime.now();
		abld.name = buildname;
		abld.baserom = baserom;
		abld.banks = new ArrayList<EngineBankInfo>(48);
		abld.seqs = new ArrayList<EngineSeqInfo>(128);
		abld.warcs = new ArrayList<EngineWaveArcInfo>(2);
		
		EngineWaveArcInfo warc = new EngineWaveArcInfo(64);
		abld.warcs.add(warc);
		abld.date_modified = ZonedDateTime.now();
		return abld;
	}
	
	public static AbldFile fromROM(ZeqerRom rom, int[] vseq, int[] vbnk, VersionWaveTable vwav) throws IOException{
		//Will need to load the code tables as well as the zeqer version tables.
		//Zeqer version tables SHOULD be generated when ROM is first imported.
		
		if(rom == null) return null;
		if(vseq == null || vbnk == null || vwav == null) return null;
		AbldFile abld = new AbldFile();
		abld.date_created = ZonedDateTime.now();
		abld.oot_mode = rom.getRomInfo().isZ5();
		abld.name = rom.getRomInfo().getZeqerID();
		abld.tv_type = rom.getRomInfo().getTVType();
		
		//Banks
		BankInfoEntry[] code_banks = rom.loadBankEntries();
		abld.banks = new ArrayList<EngineBankInfo>(vbnk.length);
		for(int i = 0; i < vbnk.length; i++){
			EngineBankInfo info = new EngineBankInfo();
			abld.banks.add(info);
			info.setBankUID(vbnk[i]);
			BankInfoEntry entry = code_banks[i];
			info.setCachePolicy(entry.getCachePolicy());
			info.setMedium(entry.getMedium());
			info.setWArc1(entry.getPrimaryWaveArcIndex());
			info.setWArc2(entry.getSecondaryWaveArcIndex());
		}
		
		//Seqs
		SeqInfoEntry[] code_seqs = rom.loadSeqEntries();
		BankMapEntry[] code_bs = rom.loadSeqBankMap();
		abld.seqs = new ArrayList<EngineSeqInfo>(vseq.length);
		for(int i = 0; i < vseq.length; i++){
			EngineSeqInfo info = new EngineSeqInfo();
			abld.seqs.add(info);
			info.setSeqUID(vseq[i]);
			SeqInfoEntry entry = code_seqs[i];
			BankMapEntry bmap = code_bs[i];
			info.setMedium(entry.getMedium());
			info.setCachePolicy(entry.getCachePolicy());
			int bcount = bmap.getBankCount();
			if(bcount > 0){
				int[] bankmap = new int[bcount];
				for(int j = 0; j < bcount; j++) bankmap[j] = vbnk[bmap.getBank(j)];
				info.setBanks(bankmap);
			}
		}
		
		//WArcs
		WaveArcInfoEntry[] code_warcs = rom.loadWaveArcEntries();
		int warc_count = vwav.getWArcCount();
		abld.warcs = new ArrayList<EngineWaveArcInfo>(warc_count);
		for(int i = 0; i < warc_count; i++){
			EngineWaveArcInfo info = null;
			if(vwav.isWArcReference(i)){
				int refid = code_warcs[i].getOffset();
				info = new EngineWaveArcInfo(1);
				if(refid >= 0 && refid < warc_count){
					info.setRefIndex(code_warcs[i].getOffset());
				}
			}
			else{
				int scount = vwav.getWArcSampleCount(i);
				info = new EngineWaveArcInfo(scount);
				for(int j = 0; j < scount; j++){
					int[] res = vwav.getSampleInfo(i, j);
					info.addSample(res[0]);
				}
			}
			info.setName("uwar" + String.format("%02d", i));
			info.setEnumString(RecompFiles.genEnumStrFromName(info.getName()));
			info.setMedium(code_warcs[i].getMedium());
			info.setCachePolicy(code_warcs[i].getCachePolicy());
			abld.warcs.add(info);
		}
		
		if(rom.getRomInfo().isZ5()){
			abld.warcClean_Z5();
		}
		else if(rom.getRomInfo().isZ6()){
			abld.warcClean_Z6();
		}
		
		abld.date_modified = ZonedDateTime.now();
		return abld;
	}
	
	private static String getDateTimeString(){
		String str = ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
		str = str.replace(":", "");
		str = str.replace("-", "");
		str = str.replace("+", "");
		return str;
	}
	
	/*----- Getters -----*/
	
	public boolean isSysBuild(){return this.sys_build;}
	public boolean isZ5Compatible(){return this.oot_mode;}
	public String getBuildName(){return name;}
	public String getBaseRomID(){return baserom;}
	public int getTVType(){return tv_type;}
	public ZonedDateTime getTimeCreated(){return date_created;}
	public ZonedDateTime getTimeModified(){return date_modified;}
	
	public boolean isInjectionBuild(){
		return baserom != null;
	}
	
	public int getBankCount(){
		if(banks == null) return 0;
		return banks.size();
	}
	
	public int getSeqCount(){
		if(seqs == null) return 0;
		return seqs.size();
	}
	
	public int getWaveArcCount(){
		if(warcs == null) return 0;
		return warcs.size();
	}
	
	public int getTotalSampleCount(){
		if(warcs == null) return 0;
		int samples = 0;
		for(EngineWaveArcInfo warc : warcs){
			if(warc.getRefIndex() >= 0) continue;
			samples += warc.countSamples();
		}
		return samples;
	}
	
	public List<EngineBankInfo> getBanks(){
		List<EngineBankInfo> copy = new ArrayList<EngineBankInfo>(banks.size());
		copy.addAll(banks);
		return copy;
	}
	
	public List<EngineWaveArcInfo> getWaveArcs(){
		List<EngineWaveArcInfo> copy = new ArrayList<EngineWaveArcInfo>(warcs.size());
		copy.addAll(warcs);
		return copy;
	}
	
	public List<EngineSeqInfo> getSeqs(){
		List<EngineSeqInfo> copy = new ArrayList<EngineSeqInfo>(seqs.size());
		copy.addAll(seqs);
		return copy;
	}
	
	/*----- Setters -----*/
	
	public void flagSysBuild(boolean b){sys_build = b;}
	public void setBuildName(String val){name = val;}
	
	public void timestamp(){
		date_modified = ZonedDateTime.now();
	}
	
	/*----- Parse -----*/
	
	public static AbldFile readABLD(FileBuffer data) throws UnsupportedFileTypeException{
		if(data == null) return null;
		
		//Check magic tag...
		long mpos = data.findString(0L, 4L, MAGIC);
		if(mpos != 0L) throw new UnsupportedFileTypeException("AbldFile.readABLD || Magic number not found!");
		
		data.setCurrentPosition(4L);
		int flags = Short.toUnsignedInt(data.nextShort());
		short version = data.nextShort(); //Version.
		
		AbldFile abld = new AbldFile();
		abld.date_created = ZonedDateTime.now();
		if((flags & 0x8000) != 0) abld.oot_mode = true;
		else abld.oot_mode = false;
		abld.tv_type = (flags >> 12) & 0x3;
		
		if(version >= 2){
			abld.date_created = ZonedDateTime.ofInstant(Instant.ofEpochSecond(data.nextLong()), ZoneId.systemDefault());
			abld.date_modified = ZonedDateTime.ofInstant(Instant.ofEpochSecond(data.nextLong()), ZoneId.systemDefault());
		}
		else{
			abld.date_modified = ZonedDateTime.now();
		}
		
		SerializedString ss = data.readVariableLengthString("UTF8", data.getCurrentPosition(), BinFieldSize.WORD, 2);
		data.skipBytes(ss.getSizeOnDisk());
		abld.name = ss.getString();
		
		//Check for injection
		boolean isInject = false;
		if(version >= 2){
			if((flags & 0x0001) != 0){
				isInject = true;
			}
		}
		
		if(isInject){
			ss = data.readVariableLengthString(data.getCurrentPosition(), BinFieldSize.WORD, 2);
			data.skipBytes(ss.getSizeOnDisk());
			abld.baserom = ss.getString();
		}
		
		//Seqs
		int scount = data.nextShort();
		if(scount > 0){
			abld.seqs = new ArrayList<EngineSeqInfo>(scount);
			for(int i = 0; i < scount; i++){
				EngineSeqInfo info = new EngineSeqInfo();
				if(isInject) info.setSubSlot(data.nextInt());
				
				info.setSeqUID(data.nextInt());
				info.setMedium(data.nextByte());
				info.setCachePolicy(data.nextByte());
				int bcount = data.nextShort();
				if(bcount > 0){
					int[] sbanks = new int[bcount];
					for(int j = 0; j < bcount; j++){
						sbanks[j] = data.nextInt();
					}
					info.setBanks(sbanks);
				}
				abld.seqs.add(info);
			}
		}
		
		//Banks
		int bcount = data.nextShort();
		if(bcount > 0){
			abld.banks = new ArrayList<EngineBankInfo>(bcount);
			for(int i = 0; i < bcount; i++){
				EngineBankInfo info = new EngineBankInfo();
				if(isInject) info.setSubSlot(data.nextInt());
				info.setBankUID(data.nextInt());
				info.setMedium(data.nextByte());
				info.setCachePolicy(data.nextByte());
				info.setWArc1(data.nextByte());
				info.setWArc2(data.nextByte());
				abld.banks.add(info);
			}
		}
		
		//Warcs
		if(!isInject){
			int acount = data.nextShort();
			if(acount > 0){
				abld.warcs = new ArrayList<EngineWaveArcInfo>(acount);
				for(int i = 0; i < acount; i++){
					int wcount = data.nextShort();
					EngineWaveArcInfo info = null;
					if((wcount & 0x8000) != 0){
						//Reference
						info = new EngineWaveArcInfo(1);
						info.setRefIndex(wcount & 0x7fff);
						//System.err.println("Reference found! Ref index: " + info.getRefIndex());
						wcount = 0;
					}
					else{
						info = new EngineWaveArcInfo(wcount);
					}
					ss = data.readVariableLengthString("UTF8", data.getCurrentPosition(), BinFieldSize.WORD, 2);
					data.skipBytes(ss.getSizeOnDisk());
					info.setName(ss.getString());
					ss = data.readVariableLengthString("UTF8", data.getCurrentPosition(), BinFieldSize.WORD, 2);
					data.skipBytes(ss.getSizeOnDisk());
					info.setEnumString(ss.getString());
					info.setMedium(data.nextByte());
					info.setCachePolicy(data.nextByte());
					for(int j = 0; j < wcount; j++){
						info.addSample(data.nextInt());
					}
					abld.warcs.add(info);
				}
			}
		}
		else{
			int smplcount = data.nextShort();
			abld.warcs = new ArrayList<EngineWaveArcInfo>(2);
			EngineWaveArcInfo info = new EngineWaveArcInfo(smplcount+1);
			for(int i = 0; i < smplcount; i++){
				info.addSample(data.nextInt());
			}
		}
		
		return abld;
	}
	
	/*----- Serialize -----*/
	
	public boolean serializeTo(FileBuffer buffer){
		boolean inject = isInjectionBuild();
		
		//Header
		int alloc = 32;
		if(name == null){
			name = "abld_" + getDateTimeString();
		}
		alloc += name.length() + 3;
		if(inject){
			alloc += baserom.length() + 3;
		}
		
		FileBuffer hdr = new FileBuffer(alloc, true);
		hdr.printASCIIToFile(MAGIC);
		int flags = 0;
		if(oot_mode) flags |= 0x8000;
		flags |= (tv_type & 0x3) << 12;
		if(inject) flags |= 0x1;
		hdr.addToFile((short)flags);
		hdr.addToFile(VERSION);
		
		hdr.addToFile(date_created.toEpochSecond());
		hdr.addToFile(date_modified.toEpochSecond());
		
		hdr.addVariableLengthString("UTF8", name, BinFieldSize.WORD, 2);
		if(inject){
			hdr.addVariableLengthString(baserom, BinFieldSize.WORD, 2);
		}
		buffer.addToFile(hdr);
		
		//Seqs
		int scount = seqs.size();
		FileBuffer ct = new FileBuffer(2, true);
		ct.addToFile((short)scount);
		buffer.addToFile(ct);
		if(scount > 0){
			for(EngineSeqInfo seq : seqs){
				buffer.addToFile(seq.serializeMe());
			}
		}
		
		//Banks
		int bcount = banks.size();
		ct = new FileBuffer(2, true);
		ct.addToFile((short)bcount);
		buffer.addToFile(ct);
		if(bcount > 0){
			for(EngineBankInfo bnk : banks){
				buffer.addToFile(bnk.serializeMe());
			}
		}
		
		//Warcs
		if(!inject){
			int acount = warcs.size();
			ct = new FileBuffer(2, true);
			ct.addToFile((short)acount);
			buffer.addToFile(ct);
			if(acount > 0){
				for(EngineWaveArcInfo warc : warcs){
					buffer.addToFile(warc.serializeMe());
				}
			}
		}
		else{
			if(!warcs.isEmpty()){
				EngineWaveArcInfo warc = warcs.get(0);
				List<Integer> slist = warc.getSamples();
				int wcount = slist.size();
				FileBuffer temp = new FileBuffer(2 + (wcount << 2), true);
				temp.addToFile((short)wcount);
				for(Integer w : slist) temp.addToFile(w);
			}
			else{
				ct = new FileBuffer(2, true);
				ct.addToFile((short)0);
				buffer.addToFile(ct);
			}
		}
		
		return true;
	}
	
	public boolean serializeTo(String filepath) throws IOException{
		int slotcount = 4;
		slotcount += seqs.size();
		slotcount += banks.size();
		slotcount += warcs.size();
		FileBuffer buff = new MultiFileBuffer(slotcount);
		if(!this.serializeTo(buff)) return false;
		buff.writeFile(filepath);
		return true;
	}
	
	/*----- Misc -----*/
	
	protected void warcClean_Z5(){
		//Names WARCs and notes samples for bank 4
		int acount = warcs.size();
		if(acount != WARC_NAMES_Z5.length) return;
		
		int i = 0;
		for(EngineWaveArcInfo ainfo : this.warcs){
			ainfo.setName(WARC_NAMES_Z5[i][0]);
			ainfo.setEnumString(WARC_NAMES_Z5[i][1]);
			if(i == 4){
				ainfo.updateCapacity(Z5_WARC4_SAMPLES.length);
				for(int j = 0; j < Z5_WARC4_SAMPLES.length; j++){
					ainfo.addSample(Z5_WARC4_SAMPLES[j]);
				}
			}
			i++;
		}
		
	}
	
	protected void warcClean_Z6(){
		//Just names WARCs
		int acount = warcs.size();
		if(acount != WARC_NAMES_Z6.length) return;
		
		int i = 0;
		for(EngineWaveArcInfo ainfo : this.warcs){
			ainfo.setName(WARC_NAMES_Z6[i][0]);
			ainfo.setEnumString(WARC_NAMES_Z6[i][1]);
		}
	}
	
	/*----- Export -----*/
	
}
