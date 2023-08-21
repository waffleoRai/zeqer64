package waffleoRai_zeqer64.engine;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_soundbank.nintendo.z64.Z64Bank;
import waffleoRai_zeqer64.AudiotableMap;
import waffleoRai_zeqer64.SoundTables.BankInfoEntry;
import waffleoRai_zeqer64.SoundTables.BankMapEntry;
import waffleoRai_zeqer64.SoundTables.SeqInfoEntry;
import waffleoRai_zeqer64.SoundTables.WaveArcInfoEntry;
import waffleoRai_zeqer64.ZeqerCore;

public class EngineTables {

	public static class AudiotableReturn{
		public AudiotableMap<Z64WaveInfo> id_offset_map;
		public FileBuffer code_table;
		public WaveArcInfoEntry[] ctbl;
	}
	
	public static class AudiobankReturn{
		public FileBuffer code_table;
		public Map<Integer, Integer> id_idx_map;
		public BankInfoEntry[] ctbl;
	}
	
	public static class AudioseqReturn{
		public FileBuffer code_table;
		public FileBuffer code_sbmap;
		public SeqInfoEntry[] ctbl;
		public BankMapEntry[] sbtbl;
	}
	
	public static AudiotableReturn buildAudiotable(List<EngineWaveArcInfo> arcs, OutputStream out) throws IOException{
		return buildAudiotable(arcs, out, true);
	}
	
	public static AudiotableReturn buildAudiotable(List<EngineWaveArcInfo> arcs, OutputStream out, boolean big_endian) throws IOException{
		//Output stream may be null.
		if(arcs == null || arcs.isEmpty()) return null;
		int acount = arcs.size();
		AudiotableReturn ret = new AudiotableReturn();
		ret.id_offset_map = new AudiotableMap<Z64WaveInfo>(acount);
		
		ret.code_table = new FileBuffer((acount + 1) << 4, big_endian);
		ret.ctbl = new WaveArcInfoEntry[acount];
		
		int i = 0; int global_pos = 0; int local_pos = 0;
		for(EngineWaveArcInfo ainfo : arcs){
			local_pos = 0;
			WaveArcInfoEntry entry = new WaveArcInfoEntry();
			ret.ctbl[i] = entry;
			entry.setMedium(ainfo.getMedium());
			entry.setCachePolicy(ainfo.getCachePolicy());
			
			if(ainfo.isReference()){
				entry.setOffset(ainfo.getRefIndex());
				entry.setSize(0);
				ret.id_offset_map.copyMappings(ainfo.getRefIndex(), i);
			}
			else{
				entry.setOffset(global_pos);
				List<Integer> samples = ainfo.getSamples();
				Map<Integer, Z64WaveInfo> offmap = ret.id_offset_map.getMap(i);
				for(Integer sid : samples){
					int wsize = 0; int dsize = 0;
					FileBuffer wdat = ZeqerCore.getActiveCore().loadWaveData(sid);
					if(wdat == null){
						System.err.println("EngineTables.buildAudiotable || ERROR: Sample with UID 0x" + String.format("%08x", sid) + " not found!");
						return null;
					}
					dsize = (int)wdat.getFileSize();
					wsize = (dsize + 0xf) & ~0xf;
					
					Z64WaveInfo winfo = ZeqerCore.getActiveCore().getWaveInfo(sid);
					if(winfo == null){
						System.err.println("EngineTables.buildAudiotable || ERROR: Sample info with UID 0x" + String.format("%08x", sid) + " not found!");
						return null;
					}
					winfo = winfo.copy();
					winfo.setWaveOffset(local_pos);
					offmap.put(sid, winfo);
					local_pos += wsize;
					
					if(out != null){
						wdat.writeToStream(out);
						int pad = wsize - dsize;
						for(int j = 0; j < pad; j++) out.write(0);
					}
				}
				entry.setSize(local_pos);
			}
			global_pos += local_pos;
			i++;
		}
		
		//Serialize code table
		ret.code_table.addToFile((short)acount);
		for(int j = 0; j < 14; j++) ret.code_table.addToFile(FileBuffer.ZERO_BYTE);
		for(int j = 0; j < acount; j++){
			ret.ctbl[j].serializeTo(ret.code_table);
		}
		
		return ret;
	}
	
	public static AudiobankReturn buildAudiobank(List<EngineBankInfo> banks, AudiotableMap<Z64WaveInfo> id_offset_map, OutputStream out) throws IOException{
		return buildAudiobank(banks, id_offset_map, out, true, false);
	}
	
	public static AudiobankReturn buildAudiobank(List<EngineBankInfo> banks, AudiotableMap<Z64WaveInfo> id_offset_map, OutputStream out, boolean big_endian, boolean x64) throws IOException{
		//don't forget to account for reference entries!
		if(banks == null || banks.isEmpty()) return null;
		if(id_offset_map == null) return null;
		int bcount = banks.size();
		AudiobankReturn ret = new AudiobankReturn();
		ret.id_idx_map = new HashMap<Integer, Integer>();
		
		ret.code_table = new FileBuffer((bcount + 1) << 4, big_endian);
		ret.ctbl = new BankInfoEntry[bcount];
		
		int ser_op = 0;
		if(!big_endian) ser_op |= Z64Bank.SEROP_LITTLE_ENDIAN;
		if(x64) ser_op |= Z64Bank.SEROP_64BIT;
		
		int i = 0; int global_pos = 0;
		for(EngineBankInfo binfo : banks){
			int buid = binfo.getBankUID();
			Integer rmap = ret.id_idx_map.get(buid);
			BankInfoEntry entry = new BankInfoEntry();
			ret.ctbl[i] = entry;
			entry.setMedium(binfo.getMedium());
			entry.setCachePolicy(binfo.getCachePolicy());
			entry.setPrimaryWaveArcIndex(binfo.getWArc1());
			entry.setSecondaryWaveArcIndex(binfo.getWArc2());
			
			if(rmap != null){
				//Reference
				entry.setOffset(rmap);
				entry.setSize(0);
				entry.setInstrumentCount(ret.ctbl[rmap].getInstrumentCount());
				entry.setPercussionCount(ret.ctbl[rmap].getPercussionCount());
				entry.setSFXCount(ret.ctbl[rmap].getSFXCount());
			}
			else{
				//New bank
				entry.setOffset(global_pos);
				ret.id_idx_map.put(buid, i);
				
				//Load
				//System.err.println("Looking at bank " + i + String.format(" (%08x)", buid));
				Z64Bank mybank = ZeqerCore.getActiveCore().loadBankData(buid);
				if(mybank == null){
					System.err.println("EngineTables.buildAudiobank || ERROR: Bank with UID 0x" + String.format("%08x", buid) + " not found!");
					return null;
				}
				int icount = mybank.getEffectiveInstCount();
				int pcount = mybank.getEffectivePercCount();
				int xcount = mybank.getEffectiveSFXCount();
				entry.setInstrumentCount(icount);
				entry.setPercussionCount(pcount);
				entry.setSFXCount(xcount);
				//mybank.printMeTo(new OutputStreamWriter(System.err));
				
				//Update wave offsets
				/*
				 * TODO This wave mapping scheme needs to be updated.
				 * If samples occur in multiple banks, the offset fields
				 * need to be updated to reflect offset in the bank AT HAND.
				 * Since there is generally 1 Z64WaveInfo instance active in the 
				 * core's wave manager at a time, I need a function that will 
				 * instead tell the build to go through a samplebank's waves
				 */
				
				//mybank.updateWaves(id_offset_map.getMap(binfo.getWArc1()));
				//mybank.setSamplesOrderedByUID(false);
				//mybank.printMeTo(new OutputStreamWriter(System.err));
				
				//Write output
				FileBuffer buff = mybank.serializeMe(ser_op);
				int dsize = (int)buff.getFileSize();
				int tsize = (dsize + 0xf) & ~0xf;
				if(out != null){
					buff.writeToStream(out);
					int pad = tsize - dsize;
					for(int j = 0; j < pad; j++) out.write(0);
				}
				
				//Update size
				entry.setSize(tsize);
				global_pos += tsize;
			}
			i++;
		}
		
		//Serialize code table
		ret.code_table.addToFile((short)bcount);
		for(int j = 0; j < 14; j++) ret.code_table.addToFile(FileBuffer.ZERO_BYTE);
		for(int j = 0; j < bcount; j++){
			ret.ctbl[j].serializeTo(ret.code_table);
		}
		
		return ret;
	}
	
	public static AudioseqReturn buildAudioseq(List<EngineSeqInfo> seqs, Map<Integer, Integer> bank_idx_map, OutputStream out) throws IOException{
		return buildAudioseq(seqs, bank_idx_map, out);
	}
	
	public static AudioseqReturn buildAudioseq(List<EngineSeqInfo> seqs, Map<Integer, Integer> bank_idx_map, OutputStream out, boolean big_endian) throws IOException{
		//Output may be null.
		if(seqs == null || seqs.isEmpty()) return null;
		if(bank_idx_map == null) return null;
		int scount = seqs.size();
		AudioseqReturn ret = new AudioseqReturn();
		Map<Integer, Integer> sid_idx_map = new HashMap<Integer, Integer>();
		
		ret.code_table = new FileBuffer((scount + 1) << 4, big_endian);
		int sballoc = 16;
		ret.ctbl = new SeqInfoEntry[scount];
		ret.sbtbl = new BankMapEntry[scount];
		
		int i = 0; int global_pos = 0;
		for(EngineSeqInfo sinfo : seqs){
			int sid = sinfo.getSeqUID();
			Integer rmap = sid_idx_map.get(sid);
			
			SeqInfoEntry entry = new SeqInfoEntry();
			ret.ctbl[i] = entry;
			entry.setMedium(sinfo.getMedium());
			entry.setCachePolicy(sinfo.getCachePolicy());
			
			List<Integer> sbanks = sinfo.getBankList();
			int sbcount = sbanks.size();
			BankMapEntry sbentry = new BankMapEntry(sbcount);
			ret.sbtbl[i] = sbentry;
			int j = 0;
			for(Integer b : sbanks){
				Integer bmap = bank_idx_map.get(b);
				if(bmap != null) sbentry.setBank(j, bmap);
				else sbentry.setBank(j, 0);
				j++;
			}
			sballoc += sbcount + 3;
			
			if(rmap != null){
				//Reference
				entry.setOffset(rmap);
				entry.setSize(0);
			}
			else{
				entry.setOffset(global_pos);
				sid_idx_map.put(sid, i);
				
				FileBuffer seqdat = ZeqerCore.getActiveCore().loadSeqData(sid);
				if(seqdat == null){
					System.err.println("EngineTables.buildAudioseq || ERROR: Seq with UID 0x" + String.format("%08x", sid) + " not found!");
					return null;
				}
				
				int dsize = (int)seqdat.getFileSize();
				int tsize = (dsize + 0xf) & ~0xf;
				if(out != null){
					seqdat.writeToStream(out);
					int pad = tsize - dsize;
					for(int k = 0; k < pad; k++) out.write(0);
				}
				
				entry.setSize(tsize);
				global_pos += tsize;
			}
			i++;
		}
		
		//Code tables
		ret.code_table.addToFile((short)scount);
		for(int j = 0; j < 14; j++) ret.code_table.addToFile(FileBuffer.ZERO_BYTE);
		for(int j = 0; j < scount; j++){
			ret.ctbl[j].serializeTo(ret.code_table);
		}
		
		ret.code_sbmap = new FileBuffer(sballoc, big_endian);
		int mpos = scount << 1;
		for(i = 0; i < scount; i++){
			ret.code_sbmap.addToFile((short)mpos);
			mpos += ret.sbtbl[i].getBankCount() + 1;
		}
		for(i = 0; i < scount; i++){
			int sbcount = ret.sbtbl[i].getBankCount();
			ret.code_sbmap.addToFile((byte)sbcount);
			for(int j = 0; j < sbcount; j++){
				ret.code_sbmap.addToFile((byte)ret.sbtbl[i].getBank(j));
			}
		}
		while(ret.code_sbmap.getFileSize() % 16 != 0) ret.code_sbmap.addToFile(FileBuffer.ZERO_BYTE);

		return ret;
	}
	
}
