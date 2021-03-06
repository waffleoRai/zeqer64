package waffleoRai_zeqer64.filefmt;

import java.io.IOException;
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
	
	public static final short VERSION = 1;
	
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
	private String name;
	
	/*----- Init -----*/
	
	private AbldFile(){}
	
	public static AbldFile newABLD(){
		return newABLD("abld_" + getDateTimeString());
	}
	
	public static AbldFile newABLD(String buildname){
		AbldFile abld = new AbldFile();
		//abld.name = "abld_" + getDateTimeString();
		abld.name = buildname;
		abld.banks = new ArrayList<EngineBankInfo>(48);
		abld.seqs = new ArrayList<EngineSeqInfo>(128);
		abld.warcs = new ArrayList<EngineWaveArcInfo>(8);
		return abld;
	}
	
	public static AbldFile fromROM(ZeqerRom rom, int[] vseq, int[] vbnk, int[][][] vwav) throws IOException{
		//Will need to load the code tables as well as the zeqer version tables.
		//Zeqer version tables SHOULD be generated when ROM is first imported.
		
		if(rom == null) return null;
		if(vseq == null || vbnk == null || vwav == null) return null;
		AbldFile abld = new AbldFile();
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
		abld.warcs = new ArrayList<EngineWaveArcInfo>(vwav.length);
		for(int i = 0; i < vwav.length; i++){
			int[][] wavtbl = vwav[i];
			EngineWaveArcInfo info = null;
			if(wavtbl == null){
				int refid = code_warcs[i].getOffset();
				info = new EngineWaveArcInfo(1);
				if(refid >= 0 && refid < vwav.length){
					info.setRefIndex(code_warcs[i].getOffset());
				}
			}
			else{
				info = new EngineWaveArcInfo(wavtbl.length);
				for(int j = 0; j < wavtbl.length; j++){
					info.addSample(wavtbl[j][0]);
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
	
	/*----- Parse -----*/
	
	public static AbldFile readABLD(FileBuffer data) throws UnsupportedFileTypeException{
		if(data == null) return null;
		
		//Check magic tag...
		long mpos = data.findString(0L, 4L, MAGIC);
		if(mpos != 0L) throw new UnsupportedFileTypeException("AbldFile.readABLD || Magic number not found!");
		
		data.setCurrentPosition(4L);
		int flags = Short.toUnsignedInt(data.nextShort());
		data.nextShort(); //Version. Will use when version is updated.
		
		AbldFile abld = new AbldFile();
		if((flags & 0x8000) != 0) abld.oot_mode = true;
		else abld.oot_mode = false;
		abld.tv_type = (flags >> 12) & 0x3;
		SerializedString ss = data.readVariableLengthString("UTF8", data.getCurrentPosition(), BinFieldSize.WORD, 2);
		data.skipBytes(ss.getSizeOnDisk());
		abld.name = ss.getString();
		
		//Seqs
		int scount = data.nextShort();
		if(scount > 0){
			abld.seqs = new ArrayList<EngineSeqInfo>(scount);
			for(int i = 0; i < scount; i++){
				EngineSeqInfo info = new EngineSeqInfo();
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
				info.setBankUID(data.nextInt());
				info.setMedium(data.nextByte());
				info.setCachePolicy(data.nextByte());
				info.setWArc1(data.nextByte());
				info.setWArc2(data.nextByte());
				abld.banks.add(info);
			}
		}
		
		//Warcs
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
		
		return abld;
	}
	
	/*----- Serialize -----*/
	
	public boolean serializeTo(FileBuffer buffer){
		//Header
		int alloc = 8;
		if(name == null){
			name = "abld_" + getDateTimeString();
		}
		alloc += name.length() + 3;
		
		FileBuffer hdr = new FileBuffer(alloc, true);
		hdr.printASCIIToFile(MAGIC);
		int flags = 0;
		if(oot_mode) flags |= 0x8000;
		flags |= (tv_type & 0x3) << 12;
		hdr.addToFile((short)flags);
		hdr.addToFile(VERSION);
		hdr.addVariableLengthString("UTF8", name, BinFieldSize.WORD, 2);
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
		int acount = warcs.size();
		ct = new FileBuffer(2, true);
		ct.addToFile((short)acount);
		buffer.addToFile(ct);
		if(acount > 0){
			for(EngineWaveArcInfo warc : warcs){
				buffer.addToFile(warc.serializeMe());
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
