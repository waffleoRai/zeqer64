package waffleoRai_zeqer64.bankImport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_Sound.nintendo.Z64WaveInfo;
import waffleoRai_Sound.wav.WAVFmtSpecPCM;
import waffleoRai_Sound.wav.WAVFormat;
import waffleoRai_Sound.wav.WAVFormatSpecific;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_soundbank.DLSFile;
import waffleoRai_soundbank.Region;
import waffleoRai_soundbank.dls.DLSArticulator;
import waffleoRai_soundbank.dls.DLSArticulator.DLSConBlock;
import waffleoRai_soundbank.dls.DLSArticulators;
import waffleoRai_soundbank.dls.DLSCommon;
import waffleoRai_soundbank.dls.DLSInstrument;
import waffleoRai_soundbank.dls.DLSRegion;
import waffleoRai_soundbank.dls.DLSSample;
import waffleoRai_soundbank.dls.DLSSampleLoop;
import waffleoRai_soundbank.dls.DLSWaveLink;
import waffleoRai_soundbank.nintendo.z64.Z64Bank;
import waffleoRai_soundbank.nintendo.z64.Z64Drum;
import waffleoRai_soundbank.nintendo.z64.Z64Envelope;
import waffleoRai_soundbank.nintendo.z64.Z64Instrument;
import waffleoRai_zeqer64.ErrorCode;
import waffleoRai_zeqer64.ZeqerBank;
import waffleoRai_zeqer64.bankImport.BankImportInfo.PresetInfo;
import waffleoRai_zeqer64.bankImport.BankImportInfo.SampleInfo;
import waffleoRai_zeqer64.bankImport.BankImportInfo.SubBank;
import waffleoRai_zeqer64.filefmt.bank.ZeqerBankIO;
import waffleoRai_zeqer64.filefmt.wave.ZeqerWaveIO;
import waffleoRai_zeqer64.filefmt.wave.ZeqerWaveTable;
import waffleoRai_zeqer64.filefmt.wave.ZeqerWaveIO.SampleImportOptions;
import waffleoRai_zeqer64.filefmt.wave.ZeqerWaveIO.SampleImportResult;
import waffleoRai_zeqer64.filefmt.wave.WaveTableEntry;
import waffleoRai_zeqer64.filefmt.bank.BankTableEntry;
import waffleoRai_zeqer64.presets.ZeqerDrumPreset;
import waffleoRai_zeqer64.presets.ZeqerInstPreset;
import waffleoRai_zeqer64.presets.ZeqerPercPreset;
import waffleoRai_zeqer64.presets.ZeqerPercRegion;

public class DLSImportHandler extends BankImporter{
	
	public static final int[] COMPAT_DST = {DLSArticulators.CONN_DST_EG1_ATTACKTIME,
			DLSArticulators.CONN_DST_EG1_DECAYTIME, DLSArticulators.CONN_DST_EG1_RELEASETIME,
			DLSArticulators.CONN_DST_EG1_SUSTAINLEVEL, DLSArticulators.CONN_DST_EG1_DELAYTIME,
			DLSArticulators.CONN_DST_EG1_HOLDTIME, DLSArticulators.CONN_DST_EG1_SHUTDOWNTIME};
	
	private static boolean articulatorIncompatible(DLSArticulator art){
		//Looks like pretty much anything that isn't envelope will be
		//	incompatible
		if(art == null) return false;
		List<DLSConBlock> blocks = art.getBlocks();
		if(blocks == null || blocks.isEmpty()) return false;
		for(DLSConBlock block : blocks){
			boolean dstokay = false;
			if(block.usSource != DLSArticulators.CONN_SRC_NONE) return true;
			if(block.usControl != DLSArticulators.CONN_SRC_NONE) return true;
			if(block.usTransform != DLSArticulators.CONN_TRN_NONE) return true;
			for(int dst : COMPAT_DST){
				if(block.usDestination == dst){
					dstokay = true;
					break;
				}
			}
			if(!dstokay) return true;
		}
		
		return false;
	}
	
	public static BankImportInfo initRead(DLSFile dls){
		if(dls == null) return null;
		
		//int bcount = dls.getBankCount();
		int scount = dls.getSampleCount();
		
		BankImportInfo info = new BankImportInfo(scount);
		
		//Allocate banks
		List<Integer> bankIds = dls.getAllBankIDs();
		List<Integer> templist = bankIds;
		bankIds = new ArrayList<Integer>(templist.size()+1);
		
		//Clean up bank IDs to get all into 15 bit space
		for(Integer id : templist) {
			if(((id & 0x80000000) != 0)) {
				//Set bit 7 which should be unused in MIDI LSB
				id |= 0x80;
				id &= 0xffff;
			}
			info.newBank(id);
			bankIds.add(id);
		}

		
		//Samples (ptbl order)
		DLSSample[] slist = dls.getOrderedSamples();
		if(slist != null){
			int i = 0;
			for(DLSSample sample : slist){
				SampleInfo sinfo = info.getSample(i);
				sinfo.id = i;
				sinfo.importSample = true;
				sinfo.name = sample.getNameTag().trim();
				if(sinfo.name == null){
					sinfo.name = "Sample " + i;
				}
				
				//Warning flags...
				if(sample.getWaveFormatInfo().getSamplesPerSecond() < 
						BankImportInfo.MIN_SAMPLE_RATE_NOWARN){
					sinfo.warningFlags |= BankImportInfo.SAMPLE_WARNING_LOWSR;
				}
				
				i++;
			}	
		}
		
		//Instruments
		List<DLSInstrument> ilist = dls.getAllInstruments();
		for(DLSInstrument inst : ilist){
			int bankId = inst.getBankId();
			if(inst.bankHasDrumFlag()) bankId |= 0x80;
			int pidx = inst.getInstrumentIndex();
			
			SubBank bnk = info.getBank(bankId);
			PresetInfo preset = bnk.instruments[pidx];
			
			preset.emptySlot = false;
			preset.importToFont = true;
			preset.savePreset = true;
			preset.name = inst.getNameTag().trim();
			if(preset.name == null){
				preset.name = String.format("Preset %04x-%03d", bankId, pidx);
			}
			
			//Slot/Percussion
			preset.percInSrc = inst.bankHasDrumFlag();
			if(pidx < 126) {
				if(preset.percInSrc) preset.warningFlags |= BankImportInfo.PRESET_WARNING_PERC_AS_INST;
			}
			
			//Flags
			if(inst.getRegionCount() > 3){
				preset.warningFlags |= BankImportInfo.PRESET_WARNING_REGCOUNT;
			}
			//Articulators?
			List<DLSArticulator> globalArt = inst.getAllGlobalArticulators();
			if(!globalArt.isEmpty()){
				for(DLSArticulator art : globalArt){
					if(articulatorIncompatible(art)){
						preset.warningFlags |= BankImportInfo.PRESET_WARNING_INCOMP_MODS;
						break;
					}
				}
			}
			
			List<DLSRegion> regions = inst.getAllRegions();
			for(DLSRegion r : regions){
				if(r.getArticulatorCount() > 0){
					preset.warningFlags |= BankImportInfo.PRESET_WARNING_INCOMP_MODS;
					break;
				}
			}
		}
		
		//Do any 126/127 reassignments
		bankIds = info.getAllBankIds();
		for(Integer bankId : bankIds) {
			SubBank bnk = info.getBank(bankId);
			if(bnk == null) continue;
			if(bnk.instruments == null) continue;
			int icount = bnk.instruments.length;
			PresetInfo i126 = null;
			PresetInfo i127 = null;
			if(icount > 126) {
				if((bnk.instruments[126] != null) && !bnk.instruments[126].emptySlot) { 
					i126 = bnk.instruments[126];
				}
			}
			if(icount > 127) {
				if((bnk.instruments[127] != null) && !bnk.instruments[127].emptySlot) { 
					i127 = bnk.instruments[127];
				}
			}
			if(icount > 126) icount = 126;
			for(int i = (icount - 1); i >= 0; i--) {
				if((bnk.instruments[i] == null) || bnk.instruments[i].emptySlot) {
					if((i127 != null) && (i127.movedToIndex < 0)) {
						i127.movedToIndex = i;
					}
					else if((i126 != null) && (i126.movedToIndex < 0)) {
						i126.movedToIndex = i;
					}
					else break;
				}
			}
			if((i126 != null) && (i126.movedToIndex < 0)) {
				i126.warningFlags |= BankImportInfo.PRESET_WARNING_BAD_SLOT;
			}
			if((i127 != null) && (i127.movedToIndex < 0)) {
				i127.warningFlags |= BankImportInfo.PRESET_WARNING_BAD_SLOT;
			}
		}
		
		return info;
	}

	public static SampleImportResult importSample(DLSSample sample, SampleImportOptions options){
		if(sample == null || options == null) return null;
		SampleImportResult res = new SampleImportResult();
		
		WAVFormat fmt = sample.getWaveFormatInfo();
		if(fmt.getCodecID() != WAVFormat.WAV_CODEC_PCM){
			res.error = ZeqerWaveIO.ERROR_CODE_INVALID_CODEC;
			return res;
		}
		
		WAVFormatSpecific fmtspec = fmt.getCodecSpecificInfo();
		if(!(fmtspec instanceof WAVFmtSpecPCM)){
			res.error = ZeqerWaveIO.ERROR_CODE_INVALID_CODEC;
			return res;
		}
		WAVFmtSpecPCM pcmspec = (WAVFmtSpecPCM)fmtspec;
		
		options.srcSampleRate = fmt.getSamplesPerSecond();
		options.bitDepth = pcmspec.getBitsPerSample();
		
		byte[] rawdata = sample.getData();
		int[] samples = null;
		
		if(options.bitDepth == 8){
			//Rescale to 16.
			int framecount = rawdata.length;
			samples = new int[framecount];
			for(int i = 0; i < framecount; i++){
				int b = Byte.toUnsignedInt(rawdata[i]);
				b -= 0x80;
				double d = (double)b / 128.0;
				samples[i] = (int)Math.round(d * 65535.0);
			}
			
			options.bitDepth = 16;
		}
		else{
			FileBuffer buff = FileBuffer.wrap(rawdata);
			buff.setEndian(false);
			
			int framecount = (int)(buff.getFileSize() / (options.bitDepth >>> 3));
			samples = new int[framecount];
			
			buff.setCurrentPosition(0L);
			for(int i = 0; i < framecount; i++){
				switch(options.bitDepth){
				case 16:
					samples[i] = (int)buff.nextShort();
					break;
				case 24:
					samples[i] = buff.nextShortish();
					break;
				case 32:
					//Assumed float...
					double raw = Float.intBitsToFloat(buff.nextInt());
					samples[i] = (int)Math.round(raw * Integer.MAX_VALUE);
					break;
				}
			}
		}
		
		//Apply gain
		long clamp_max = (1L << (options.bitDepth - 1)) - 1;
		long clamp_min = -(1L << (options.bitDepth - 1));
		double gainRatio = DLSCommon.dlsGainToVolRatio(sample.getGain());
		for(int i = 0; i < samples.length; i++){
			double scaled = (double)samples[i] * gainRatio;
			long rounded = Math.round(scaled);
			if(rounded > clamp_max) rounded = clamp_max;
			if(rounded < clamp_min) rounded = clamp_min;
			samples[i] = (int)rounded;
		}
		
		//Handle any multichannel...
		int chcount = fmt.getChannelCount();
		if(chcount > 1){
			switch(options.multiChannelBehavior){
			case ZeqerWaveIO.MULTICHAN_IMPORT_SUM:
				options.channel = -1;
				break;
			case ZeqerWaveIO.MULTICHAN_IMPORT_LEFTONLY:
				options.channel = 0;
				break;
			case ZeqerWaveIO.MULTICHAN_IMPORT_RIGHTONLY:
				options.channel = 1;
				break;
			case ZeqerWaveIO.MULTICHAN_IMPORT_SPECIFY:
				break;
			}
		}
		else options.channel = 0;
		
		if(chcount > 1){
			int frames = samples.length / chcount;
			int[] temp = new int[frames];
			if(options.channel < 0){
				//Sum
				int pos = 0;
				for(int f = 0; f < frames; f++){
					double val = 0.0;
					for(int j = 0; j < chcount; j++){
						val += (double)samples[pos++] / (double)chcount;
					}
					temp[f] = (int)Math.round(val);
				}
			}
			else{
				//One channel
				int pos = 0;
				for(int f = 0; f < frames; f++){
					temp[f] = samples[pos + options.channel];
					pos += chcount;
				}
			}
			samples = temp;
		}
		
		//Loop points
		if(options.loopStart < 0){
			//No manual override
			//Pull from sample info
			DLSSampleLoop loop = sample.getLoop();
			if(loop != null){
				options.loopCount = -1;
				options.loopStart = loop.getLoopStart();
				options.loopEnd = options.loopStart + loop.getLoopLength();
			}
			else{
				//Oneshot
				options.loopCount = 0;
				options.loopStart = 0;
				options.loopEnd = samples.length;
			}
		}
		
		ZeqerWaveIO.importPCMAudio(samples, options, res);
		
		return res;
	}
	
	private static Z64WaveInfo resolveWaveLink(DLSRegion reg, WaveTableEntry[] sampleTable){
		if(reg == null || sampleTable == null) return null;
		DLSWaveLink wlnk = reg.getWaveLink();
		if(wlnk != null){
			int index = wlnk.getTableIndex();
			if(index < 0 || index >= sampleTable.length) return null;
			WaveTableEntry e = sampleTable[index];
			if(e == null) return null;
			return e.getWaveInfo();
		}
		return null;
	}
	
	private static float resolveTuning(DLSRegion reg){
		if(reg == null) return 1.0f;
		return Z64Sound.calculateTuning(Z64Sound.MIDDLE_C_S8, 
				reg.getUnityKey(), reg.getFineTuneCents());
	}
	
	private static int scoreRegionDistance(Region a, Region b){
		int min_a = a.getMinKeyInt();
		int max_a = a.getMaxKeyInt();
		int min_b = b.getMinKeyInt();
		int max_b = b.getMaxKeyInt();
		
		if(max_a <= min_b) return min_b - max_a; //No overlap. B is higher.
		if(max_b <= min_a) return min_a - max_b; //No overlap. A is higher.
		
		int olo = Math.max(min_a, min_b);
		int ohi = Math.min(max_a, max_b);
		
		return olo - ohi;
	}
	
	private static int convertEnvelope(List<DLSArticulator> artlist, Z64Envelope env){
		if(artlist == null) return -1;
		if(env == null) return -1;
		
		//All values in millis
		int delay = 0;
		int attack = 0;
		int hold = 0;
		int decay = 0;
		int release = 0;
		int shutdown = 0;
	
		double suslvl = 1.0;
		
		for(DLSArticulator art : artlist){
			List<DLSConBlock> blocks = art.getBlocks();
			for(DLSConBlock block : blocks){
				if(block.usSource != DLSArticulators.CONN_SRC_NONE) continue;
				if(block.usControl != DLSArticulators.CONN_SRC_NONE) continue;
				if(block.usTransform != DLSArticulators.CONN_TRN_NONE) continue;
				switch(block.usDestination){
				case DLSArticulators.CONN_DST_EG1_DELAYTIME:
					delay = DLSCommon.absTimeToMillis(block.lScale);
					break;
				case DLSArticulators.CONN_DST_EG1_ATTACKTIME:
					attack = DLSCommon.absTimeToMillis(block.lScale);
					break;
				case DLSArticulators.CONN_DST_EG1_HOLDTIME:
					hold = DLSCommon.absTimeToMillis(block.lScale);
					break;
				case DLSArticulators.CONN_DST_EG1_DECAYTIME:
					decay = DLSCommon.absTimeToMillis(block.lScale);
					break;
				case DLSArticulators.CONN_DST_EG1_RELEASETIME:
					release = DLSCommon.absTimeToMillis(block.lScale);
					break;
				case DLSArticulators.CONN_DST_EG1_SHUTDOWNTIME:
					shutdown = DLSCommon.absTimeToMillis(block.lScale);
					break;
				case DLSArticulators.CONN_DST_EG1_SUSTAINLEVEL:
					suslvl = (double)block.lScale / 100.0;
					break;
				}
			}
		}
		
		env.clearEvents();
		int delta = 0;
		if(shutdown > 0){
			//Delay
			int timeToShutdown = Z64Sound.envelopeMillisToDelta(shutdown);
			if(timeToShutdown > 0 && delay > 0){
				delta = Z64Sound.envelopeMillisToDelta(delay);
				if(timeToShutdown < delta){
					env.addEvent((short)timeToShutdown, (short)0);
				}
				else env.addEvent((short)delta, (short)0);
				timeToShutdown -= delta;
			}
			
			//Attack
			if(timeToShutdown > 0){
				if(attack > 0){
					delta = Z64Sound.envelopeMillisToDelta(attack);
				}
				else delta = 1;
				if(timeToShutdown < delta){
					double timeRatio = (double)timeToShutdown / (double)delta;
					int trglvl = (int)Math.round(32700.0 * timeRatio);
					env.addEvent((short)timeToShutdown, (short)trglvl);
				}
				else env.addEvent((short)delta, (short)32700);
				timeToShutdown -= delta;
			}
			
			//Hold
			if(timeToShutdown > 0 && hold > 0){
				delta = Z64Sound.envelopeMillisToDelta(hold);
				if(timeToShutdown < delta){
					env.addEvent((short)timeToShutdown, (short)32700);
				}
				else env.addEvent((short)delta, (short)32700);
				timeToShutdown -= delta;
			}
			
			//Decay/sustain
			if(timeToShutdown > 0 && suslvl < 1.0){
				if(decay > 0){
					delta = Z64Sound.envelopeMillisToDelta(decay);
				}
				else delta = 1;
				
				int sus64 = (int)Math.round(suslvl * 32700.0);
				if(timeToShutdown < delta){
					double timeRatio = (double)timeToShutdown / (double)delta;
					sus64 = (int)Math.round((double)sus64 * timeRatio);
					env.addEvent((short)timeToShutdown, (short)sus64);
				}
				else env.addEvent((short)delta, (short)sus64);
				timeToShutdown -= delta;
			}
			
			//Shutdown
			if(timeToShutdown > 0){
				env.addEvent((short)timeToShutdown, (short)0);
			}
			else env.addEvent((short)1, (short)0);
		}
		else{
			if(delay > 0){
				delta = Z64Sound.envelopeMillisToDelta(delay);
				env.addEvent((short)delta, (short)0);
			}
			
			if(attack > 0){
				delta = Z64Sound.envelopeMillisToDelta(attack);
				env.addEvent((short)delta, (short)32700);
			}
			else{
				env.addEvent((short)1, (short)32700);
			}
			
			if(hold > 0){
				delta = Z64Sound.envelopeMillisToDelta(hold);
				env.addEvent((short)delta, (short)32700);
			}
			
			if(suslvl < 1.0){
				if(decay > 0){
					delta = Z64Sound.envelopeMillisToDelta(decay);
				}
				else delta = 1;
				int sus64 = (int)Math.round(suslvl * 32700.0);
				env.addEvent((short)delta, (short)sus64);
			}
		}
		
		env.addEvent((short)Z64Sound.ENVCMD__ADSR_HANG, (short)0);
		env.addEvent((short)Z64Sound.ENVCMD__ADSR_DISABLE, (short)0);
		
		//TODO Is this last 0,0 event needed? Padding?

		int rel64 = 0; 
		if(release > 0){
			//Will use NTSC for estimates. Sorry, PAL.
			rel64 = Z64Sound.releaseMillisToValue(release);
		}
		
		return rel64; //Returns release (Z64 scale)
	}
	
	private static Z64Instrument dlsInst2Z64(DLSInstrument inst, WaveTableEntry[] sampleTable, String iname){
		if(inst == null) return null;
		Z64Instrument zinst = new Z64Instrument();
		zinst.setName(iname);
		zinst.setIDRandom();
		
		int rcount = inst.getRegionCount();
		List<DLSRegion> reglist = inst.getAllRegions();
		if(rcount == 1){
			//Assign to middle
			DLSRegion reg = reglist.get(0);
			zinst.setSampleMiddle(resolveWaveLink(reg, sampleTable));
			zinst.setTuningMiddle(resolveTuning(reg));
		}
		else if(rcount == 2){
			//Middle and low
			DLSRegion[] regs = new DLSRegion[2];
			regs = reglist.toArray(regs);
			Arrays.sort(regs);
			
			zinst.setSampleMiddle(resolveWaveLink(regs[1], sampleTable));
			zinst.setTuningMiddle(resolveTuning(regs[1]));
			zinst.setSampleLow(resolveWaveLink(regs[0], sampleTable));
			zinst.setTuningLow(resolveTuning(regs[0]));
			zinst.setLowRangeTop(regs[0].getMaxKey());
		}
		else if(rcount == 3){
			//Assign all three
			DLSRegion[] regs = new DLSRegion[3];
			regs = reglist.toArray(regs);
			Arrays.sort(regs);

			zinst.setSampleMiddle(resolveWaveLink(regs[1], sampleTable));
			zinst.setTuningMiddle(resolveTuning(regs[1]));
			zinst.setSampleLow(resolveWaveLink(regs[0], sampleTable));
			zinst.setTuningLow(resolveTuning(regs[0]));
			zinst.setSampleHigh(resolveWaveLink(regs[2], sampleTable));
			zinst.setTuningHigh(resolveTuning(regs[2]));
			zinst.setLowRangeTop(regs[0].getMaxKey());
			zinst.setHighRangeBottom(regs[2].getMinKey());
		}
		else if(rcount > 3){
			//First, need to condense into three regions at most.
			//Find three biggest.
			DLSRegion[] regs = new DLSRegion[3];
			int n = 0;
			for(DLSRegion reg : reglist){
				if(n == 0){
					regs[n++] = reg;
					continue;
				}
				boolean inserted = false;
				int rsize = reg.getMaxKeyInt() - reg.getMinKeyInt();
				for(int ii = 0; ii < n; ii++){
					int osize = regs[ii].getMaxKeyInt() - regs[ii].getMinKeyInt();
					if(rsize > osize){
						//Insert.
						for(int jj = n-1; jj == ii; jj--){
							if(jj < regs.length - 1){
								regs[jj+1] = regs[jj];
							}
						}
						regs[ii] = reg;
						if(n < 3) n++;
						inserted = true;
						break;
					}
				}
				if(n < 3 && !inserted) regs[n++] = reg;
			}
			
			//Merge other regions into the three biggest.
			for(DLSRegion reg : reglist){
				int ci = -1;
				int cscore = Integer.MAX_VALUE;
				for(int i = 0; i < 3; i++){
					int score = scoreRegionDistance(reg, regs[i]);
					if(score < cscore){
						cscore = score;
						ci = i;
					}
				}
				if(ci >= 0){
					//Merge
					int newMin = Math.min(reg.getMinKeyInt(), regs[ci].getMinKeyInt());
					int newMax = Math.max(reg.getMaxKeyInt(), regs[ci].getMaxKeyInt());
					regs[ci].setMinKey((byte)newMin);
					regs[ci].setMaxKey((byte)newMax);
				}
			}
			Arrays.sort(regs);

			zinst.setSampleMiddle(resolveWaveLink(regs[1], sampleTable));
			zinst.setTuningMiddle(resolveTuning(regs[1]));
			zinst.setSampleLow(resolveWaveLink(regs[0], sampleTable));
			zinst.setTuningLow(resolveTuning(regs[0]));
			zinst.setSampleHigh(resolveWaveLink(regs[2], sampleTable));
			zinst.setTuningHigh(resolveTuning(regs[2]));
			zinst.setLowRangeTop(regs[0].getMaxKey());
			zinst.setHighRangeBottom(regs[2].getMinKey());
		}
		else return null;
		
		//Use global articulators to estimate envelope
		List<DLSArticulator> artlist = inst.getAllGlobalArticulators();
		if(artlist != null && !artlist.isEmpty()){
			int rel = convertEnvelope(artlist, zinst.getEnvelope());
			if(rel >= 0){
				zinst.setDecay((byte)rel);
			}
			else {
				zinst.setEnvelope(Z64Envelope.newDefaultEnvelope());
				zinst.setDecay((byte)0);
			}
		}
		else{
			//Otherwise, leave default envelope.
			zinst.setDecay((byte)0);
		}
		
		return zinst;
	}
	
	private static Z64Drum dlsRegion2Drum(DLSRegion reg, WaveTableEntry[] sampleTable){
		if(reg == null) return null;
		if(sampleTable == null) return null;
		
		Z64Drum drum = new Z64Drum();
		drum.setPoolIDRandom();
		
		drum.setSample(resolveWaveLink(reg, sampleTable));
		drum.setRootKey(reg.getUnityKey());
		drum.setFineTune(reg.getFineTuneCents());
		drum.setDecay((byte)0);
		drum.setPan((byte)0x40);
		
		//Move region-specific envelope translation up here.
		//Can also check for region-specific pan and use that...
		if(reg.getArticulatorCount() > 0){
			List<DLSArticulator> artlist = reg.getAllArticulators();
			int rel = convertEnvelope(artlist, drum.getEnvelope());
			if(rel < 0){
				rel = 0;
				drum.setEnvelope(Z64Envelope.newDefaultEnvelope());
			}
			drum.setDecay((byte)rel);
			
			for(DLSArticulator art : artlist){
				List<DLSConBlock> blocks = art.getBlocks();
				for(DLSConBlock block : blocks){
					if(block.usSource != DLSArticulators.CONN_SRC_NONE) continue;
					if(block.usControl != DLSArticulators.CONN_SRC_NONE) continue;
					if(block.usTransform != DLSArticulators.CONN_TRN_NONE) continue;
					if(block.usDestination == DLSArticulators.CONN_DST_PAN){
						drum.setPan((byte)DLSCommon.dlsPanToStdPan(block.lScale));
					}
				}
			}
		}
		
		return drum;
	}
	
	private static ZeqerPercPreset dlsInst2Perc64(DLSInstrument inst, WaveTableEntry[] sampleTable, String iname){
		if(inst == null) return null;
		if(sampleTable == null) return null;
		
		//Default envelope
		int deforel = 0;
		Z64Envelope defoenv = Z64Envelope.newDefaultEnvelope();
		List<DLSArticulator> artlist = inst.getAllGlobalArticulators();
		if(artlist != null && !artlist.isEmpty()){
			deforel = convertEnvelope(artlist, defoenv);
			if(deforel < 0){
				deforel = 0;
				defoenv = Z64Envelope.newDefaultEnvelope();
			}
		}
		
		ZeqerPercPreset preset = new ZeqerPercPreset(inst.hashCode());
		preset.setName(iname);

		List<DLSRegion> regions = inst.getAllRegions();
		for(DLSRegion reg : regions){
			Z64Drum drum = dlsRegion2Drum(reg, sampleTable);
			if(drum == null) continue;
			drum.setName("Drum " + reg.getMinKeyInt());
			
			//Set default envelope if no region envelope
			if(reg.getArticulatorCount() < 1){
				drum.setEnvelope(defoenv.copy());
				drum.setDecay((byte)deforel);
			}
			
			//Add to perc set
			int minkey = reg.getMinKeyInt();
			int maxkey = reg.getMaxKeyInt();
			for(int i = minkey; i <= maxkey; i++){
				preset.setDrumToSlot(i, drum);
			}
		}
		preset.consolidateRegions();
		
		return preset;
	}
	
	private static void setError(ErrorCode error, int value){
		if(error == null) return;
		error.value = value;
	}
	
	/*----- Importer Instance -----*/
	
	private DLSFile dls; //Hold in between init and finish so don't have to reparse.
	
	public DLSImportHandler(String filePath){
		super.path = filePath;
		super.sampleOps = new SampleImportOptions();
	}
	
	public WaveTableEntry importSampleToCore(DLSSample sample, ErrorCode error){
		if(core == null) return null;
		
		SampleImportResult res = importSample(sample, sampleOps);
		if(res == null) return null;
		
		String samplename = sample.getNameTag();
		if(samplename == null || samplename.isEmpty()){
			samplename = String.format("Sample %08x", res.info.getUID());
		}
		res.info.setName(samplename);
		
		if(res.error != ZeqerWaveIO.ERROR_CODE_NONE) return null;
		
		//Add to core.
		WaveTableEntry e = core.addUserWaveSample(res.info, res.data, error);
		if(e == null){return null;}
		
		//Additional flags for the entry.
		if(sampleOps.flagActor) e.setFlags(ZeqerWaveTable.FLAG_ISACTOR);
		if(sampleOps.flagEnv) e.setFlags(ZeqerWaveTable.FLAG_ISENV);
		if(sampleOps.flagMusic) e.setFlags(ZeqerWaveTable.FLAG_ISMUSIC);
		if(sampleOps.flagSFX) e.setFlags(ZeqerWaveTable.FLAG_ISSFX);
		if(sampleOps.flagVox) e.setFlags(ZeqerWaveTable.FLAG_ISVOX);
		
		//Additional user tags
		if(sampleTags != null){
			for(String tag : sampleTags) e.addTag(tag);
		}
		
		return e;
	}
	
	public boolean init(){
		try{
			dls = DLSFile.readDLS(path);
			super.info = initRead(dls);
		}
		catch(Exception ex){
			ex.printStackTrace();
			return false;
		}
		
		return (super.info != null);
	}
	
	public boolean importToCore(){
		ErrorCode error = super.bankImportError;
		setError(error, ZeqerBankIO.ERR_NONE);
		if(dls == null){
			setError(error, ZeqerBankIO.ERR_NULL_INPUT_DATA);
			return false;
		}
		if(info == null){
			setError(error, ZeqerBankIO.ERR_NULL_IMPORT_PARAMS);
			return false;
		}
		if(core == null){
			setError(error, ZeqerBankIO.ERR_NULL_CORE_LINK);
			return false;
		}
		
		Set<Integer> inclSamples = new HashSet<Integer>();
		//int bcount = info.getBankCount();
		
		//Map instruments by Bank/Preset ID
		Map<Integer, DLSInstrument> instmap = new HashMap<Integer, DLSInstrument>();
		List<DLSInstrument> instlist = dls.getAllInstruments();
		for(DLSInstrument inst : instlist){
			int bid = inst.getBankId();
			if(inst.bankHasDrumFlag()) bid |= 0x80;
			int id = bid << 16;
			id |= inst.getInstrumentIndex();
			instmap.put(id, inst);
		}
		
		//Figure out which samples need to be imported
		List<Integer> bankids = info.getAllBankIds();
		for(Integer bid : bankids){
			SubBank bnk = info.getBank(bid);
			if(bnk.instruments == null) continue;
			for(int j = 0; j < bnk.instruments.length; j++){
				PresetInfo pinfo = bnk.instruments[j];
				if(pinfo == null) continue;
				if(pinfo.emptySlot) continue;
				if(j > 125) {
					if(pinfo.movedToIndex < 0) continue;
				}
				
				//TODO Update to include proper perc/ slots 126/127 check
				if((bnk.importAsFont && pinfo.importToFont) || pinfo.savePreset){
					int iid = (bid << 16) | j;
					DLSInstrument inst = instmap.get(iid);
					if(inst == null) continue;
					List<DLSRegion> reglist = inst.getAllRegions();
					for(DLSRegion reg : reglist){
						DLSWaveLink wavl = reg.getWaveLink();
						if(wavl != null){
							inclSamples.add(wavl.getTableIndex());
						}
					}
				}
			}
		}
		
		//Import samples
		int scount = info.getSampleCount();
		WaveTableEntry[] waveEntries = new WaveTableEntry[scount];
		if(scount > 0){
			DLSSample[] samples = dls.getOrderedSamples();
			for(int i = 0; i < scount; i++){
				SampleInfo sinfo = info.getSample(i);
				if(sinfo == null) continue;
				if(sinfo.importSample || inclSamples.contains(i)){
					//Import this sample.
					waveEntries[i] = importSampleToCore(samples[i], error);
					if(waveEntries[i] == null){
						setError(error, ZeqerBankIO.ERR_SAMPLE_IMPORT_FAILED);
						return false;
					}
				}
			}
		}
		
		//Import fonts/presets
		for(Integer bid : bankids){
			SubBank bnk = info.getBank(bid);
			if(bnk.instruments == null) continue;
			Z64Instrument[] zinsts = new Z64Instrument[128];
			ZeqerPercPreset perc = null;
			if(bnk.importAsFont){
				for(int j = 0; j < bnk.instruments.length; j++){
					if(bnk.instruments[j] == null) continue;
					
					PresetInfo pinfo = bnk.instruments[j];
					DLSInstrument dlsinst = instmap.get((bid << 16) | j);
					if(dlsinst == null) continue;
					if(pinfo.emptySlot) continue;
					if(!pinfo.importToFont) continue;
					zinsts[j] = DLSImportHandler.dlsInst2Z64(dlsinst, waveEntries, pinfo.name);
				}

				//Perc
				if(bnk.importPercToFont && (bnk.percInst >= 0)){
					DLSInstrument dlsinst = instmap.get((bid << 16) | bnk.percInst);
					PresetInfo pinfo = bnk.instruments[bnk.percInst];
					if(dlsinst != null && pinfo != null){
						perc = DLSImportHandler.dlsInst2Perc64(dlsinst, waveEntries, pinfo.name);
					}
				}
				
				//Build Font
				Z64Bank bankdata = new Z64Bank(0);
				for(int j = 0; j < 126; j++){
					if(zinsts[j] != null){
						bankdata.setInstrument(j, zinsts[j]);
					}
				}
				if(perc != null){
					int rcount = perc.getRegionCount();
					for(int r = 0; r < rcount; r++){
						ZeqerPercRegion pr = perc.getRegion(r);
						bankdata.setDrum(pr.getDrumData(), pr.getMinSlot(), pr.getMaxSlot());
					}
				}
				
				//Add font to core
				ZeqerBank zbnk = core.addUserBank(bankdata);
				if(zbnk == null) {
					setError(error, ZeqerBankIO.ERR_BANK_IMPORT_FAILED);
					return false;
				}
				
				//Add metadata.
				BankTableEntry meta = zbnk.getTableEntry();
				meta.setCachePolicy(bnk.cache);
				meta.setMedium(bnk.medium);
				meta.setEnumString(bnk.enumLabel);
				meta.setName(bnk.name);
			}
			
			//Presets
			if(bnk.percInst >= 0){
				if(perc == null){
					//Try to create preset.
					DLSInstrument dlsinst = instmap.get((bid << 16) | bnk.percInst);
					PresetInfo pinfo = bnk.instruments[bnk.percInst];
					if(dlsinst != null && pinfo != null){
						perc = DLSImportHandler.dlsInst2Perc64(dlsinst, waveEntries, pinfo.name);
					}
				}
				if(perc != null){
					if(bnk.saveDrums){
						int rcount = perc.getRegionCount();
						for(int r = 0; r < rcount; r++){
							ZeqerPercRegion pr = perc.getRegion(r);
							if(pr != null){
								Z64Drum drum = pr.getDrumData();
								if(drum != null){
									ZeqerDrumPreset dp = new ZeqerDrumPreset(drum);
									dp.setEnumLabel(bnk.enumLabel + String.format("_DRUM%02d", r));
									if(presetTags != null){
										for(String tag : presetTags) dp.addTag(tag);
									}
									if(!core.addUserPreset(dp)){
										setError(error, ZeqerBankIO.ERR_PRESET_IMPORT_FAILED);
										return false;
									}
								}
							}
						}
					}
					if(bnk.saveDrumset){
						perc.setEnumLabel(bnk.enumLabel + "_PERC");
						if(presetTags != null){
							for(String tag : presetTags) perc.addTag(tag);
						}
						if(!core.addUserPreset(perc)){
							setError(error, ZeqerBankIO.ERR_PRESET_IMPORT_FAILED);
							return false;
						}
					}	
				}
			}
			
			//Instruments
			for(int j = 0; j < bnk.instruments.length; j++){
				if(bnk.instruments[j] == null) continue;
				
				PresetInfo pinfo = bnk.instruments[j];
				if(pinfo.emptySlot) continue;
				if(!pinfo.savePreset) continue;
				if(zinsts[j] == null){
					DLSInstrument dlsinst = instmap.get((bid << 16) | j);
					if(dlsinst == null) continue;
					zinsts[j] = DLSImportHandler.dlsInst2Z64(dlsinst, waveEntries, pinfo.name);
				}
				if(zinsts[j] == null) continue;
				
				ZeqerInstPreset ipre = new ZeqerInstPreset();
				ipre.loadData(zinsts[j]);
				
				ipre.setEnumLabel(bnk.enumLabel + String.format("_INST%03d", j));
				if(presetTags != null){
					for(String tag : presetTags) ipre.addTag(tag);
				}
				if(!core.addUserPreset(ipre)){
					setError(error, ZeqerBankIO.ERR_PRESET_IMPORT_FAILED);
					return false;
				}
			}
		}
		
		return true;
	}
	
}
