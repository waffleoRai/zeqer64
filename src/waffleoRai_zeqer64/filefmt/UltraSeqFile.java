package waffleoRai_zeqer64.filefmt;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import waffleoRai_SeqSound.n64al.NUSALSeq;
import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_Utils.BinFieldSize;
import waffleoRai_Utils.BufferReference;
import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.SerializedString;
import waffleoRai_zeqer64.ZeqerSeq;
import waffleoRai_zeqer64.ZeqerSeq.IOAlias;
import waffleoRai_zeqer64.ZeqerSeq.Label;
import waffleoRai_zeqer64.ZeqerSeq.Module;
import waffleoRai_zeqer64.filefmt.ZeqerSeqTable.SeqTableEntry;

public class UltraSeqFile {
	
	//TODO Make sure cmds are labeled in NUSALSeq (Could also put in ZeqerSeq)
	
	/*----- Constants -----*/
	
	public static final String MAGIC = "USEQ";
	public static final byte MAJOR_VERSION = 1;
	public static final byte MINOR_VERSION = 1;
	
	private static final int HEADER_SIZE = 16;

	/*----- Instance Variables -----*/
	
	private String path = null;
	private int data_len = -1;
	private Map<String, int[]> chunks;
	
	private int meta_flags = 0;
	
	private FileBuffer raw_file = null;
	
	/*----- Initialization -----*/
	
	private UltraSeqFile(){
		chunks = new HashMap<String, int[]>();
	}
	
	/*----- Getters -----*/
	
	public String getFilePath(){return path;}
	public int getDataLen(){return data_len;}
	
	/*----- Setters -----*/
	
	public void clearStoredFileData(){raw_file = null;}
	
	public void dispose(){
		raw_file = null;
		chunks.clear();
	}
	
	/*----- Reading -----*/
	
	public FileBuffer loadChunk(String key) throws IOException{
		FileBuffer chunk_data = null;
		int[] loc = chunks.get(key);
		if(loc != null){
			if(raw_file != null){
				chunk_data = raw_file.createReadOnlyCopy(loc[0], loc[1]);
			}
			else{
				chunk_data = FileBuffer.createBuffer(path, loc[0], loc[1], true);
			}
		}
		return chunk_data;
	}
	
	private static void readMETA(BufferReference chunk_data, ZeqerSeq dest, UltraSeqFile file){
		int flags = 0;
		
		SeqTableEntry t_entry = dest.getTableEntry();
		chunk_data.nextInt(); //Skip UID
		flags = Short.toUnsignedInt(chunk_data.nextShort());
		dest.setOoTCompatible((flags & 0x2) != 0);
		dest.setMoreThanFourLayers((flags & 0x4) != 0);
		dest.setSFXSeqFlag((flags & 0x100) != 0);
		file.meta_flags = flags;
		dest.setMaxVoiceLoad((int)chunk_data.nextByte());
		chunk_data.nextByte();
		
		if(t_entry != null){
			t_entry.setMedium(chunk_data.nextByte());	
			t_entry.setCache(chunk_data.nextByte());
		}
		else chunk_data.nextShort();
		
		//Skip bank stuff.
	}
	
	private static void readDATA(FileBuffer chunk_data, ZeqerSeq dest) throws IOException{
		chunk_data.setCurrentPosition(0L);
		int raw_size = chunk_data.nextInt();
		FileBuffer rawdat = chunk_data.createCopy(4L, 4L + raw_size);
		try{
			NUSALSeq seq = NUSALSeq.readNUSALSeq(rawdat);
			dest.setSequence(seq);
			rawdat.dispose();
		}
		catch(Exception ex){
			dest.setRawData(rawdat);
		}
	}
	
	private static void readLMOD(FileBuffer chunk_data, ZeqerSeq dest){
		long npos = 0L;
		chunk_data.setCurrentPosition(0L);
		int modcount = chunk_data.nextInt();
		dest.allocModuleList(modcount);
		for(int i = 0; i < modcount; i++){
			npos = Integer.toUnsignedLong(chunk_data.nextInt()) - 8L;
			SerializedString ss = chunk_data.readVariableLengthString("UTF8", npos, BinFieldSize.WORD, 2);
			Module m = new Module(ss.getString());
			dest.addModule(m);
		}
	}
	
	private static void readLABL(FileBuffer chunk_data, ZeqerSeq dest){
		long rpos = 0L;
		chunk_data.setCurrentPosition(0L);
		int lblcount = chunk_data.nextInt();
		for(int i = 0; i < lblcount; i++){
			rpos = Integer.toUnsignedLong(chunk_data.nextInt()) - 8L;
			
			int ctxt = (int)chunk_data.getByte(rpos++);
			int modidx = (int)chunk_data.getByte(rpos++);
			int datsz = (int)chunk_data.shortFromFile(rpos); rpos+=2;
			int lbloff = (int)chunk_data.shortFromFile(rpos); rpos+=2;
			SerializedString lblname = chunk_data.readVariableLengthString("UTF8", rpos, BinFieldSize.WORD, 2);
			Label lbl = new Label(lblname.getString());
			lbl.setContext(ctxt);
			lbl.setDataSize(datsz);
			lbl.setPosition(lbloff);
			
			if(modidx >= 0){
				Module mod = dest.getModule(modidx);
				mod.addLabel(lbl);
				lbl.setModule(mod);
			}
			else{
				lbl.setModule(null);
				dest.addCommonLabel(lbl);
			}
		}
	}
	
	private static void readIOAL(FileBuffer chunk_data, ZeqerSeq dest){
		int acount = chunk_data.nextInt();
		for(int i = 0; i < acount; i++){
			int ch_idx = (int)chunk_data.nextByte();
			int io_idx = (int)chunk_data.nextByte();
			int md_idx = (int)chunk_data.nextByte();
			chunk_data.nextByte();
			SerializedString ss = chunk_data.readVariableLengthString("UTF8", chunk_data.getCurrentPosition(), BinFieldSize.WORD, 2);
			String name = ss.getString();
			chunk_data.skipBytes(ss.getSizeOnDisk());
			IOAlias alias = new IOAlias(name, ch_idx, io_idx);
			if(md_idx >= 0){
				Module mod = dest.getModule(md_idx);
				mod.addIOAlias(alias);
			}
			else dest.addCommonAlias(alias);
		}
	}
	
	public static UltraSeqFile openUSEQ(String filepath) throws IOException, UnsupportedFileTypeException{
		FileBuffer data = FileBuffer.createBuffer(filepath, true);
		UltraSeqFile useq = openUSEQ(data);
		useq.path = filepath;
		return useq;
	}
	
	public static UltraSeqFile openUSEQ(FileBuffer data) throws UnsupportedFileTypeException{
		UltraSeqFile useq = new UltraSeqFile();
		
		long pos = data.findString(0L, 0x10L, MAGIC);
		if(pos != 0L) throw new UnsupportedFileTypeException("UltraSeqFile.openUSEQ || Magic number not recognized.");
		data.setCurrentPosition(4L);
		
		int bom = Short.toUnsignedInt(data.nextShort());
		if(bom == 0xFFFE) data.setEndian(false);
		
		//Skip version for now
		data.nextByte(); //Major version
		data.nextByte(); //Minor version
		data.nextInt(); //Total file size
		data.nextShort(); //Header size
		int ccount = Short.toUnsignedInt(data.nextShort());
		
		String cmag = null;
		int csz = 0;
		for(int i = 0; i < ccount; i++){
			pos = data.getCurrentPosition();
			cmag = data.getASCII_string(pos, 4);
			data.skipBytes(4L);
			csz = data.nextInt();
			useq.chunks.put(cmag, new int[]{(int)data.getCurrentPosition(), csz});
			data.skipBytes(csz);
		}
		
		useq.raw_file = data;
		return useq;
	}
	
	public static ZeqerSeq readUSEQ(String filepath) throws IOException, UnsupportedFileTypeException{
		UltraSeqFile useq = openUSEQ(filepath);
		return readUSEQ(useq);
	}
	
	public static ZeqerSeq readUSEQ(FileBuffer data) throws UnsupportedFileTypeException, IOException{
		UltraSeqFile useq = openUSEQ(data);
		return readUSEQ(useq);
	}
	
	public static ZeqerSeq readUSEQ(UltraSeqFile useq) throws IOException{
		return readUSEQ(useq, null);
	}
	
	public static ZeqerSeq readUSEQ(UltraSeqFile useq, SeqTableEntry tblEntry) throws IOException{
		ZeqerSeq zseq = new ZeqerSeq(tblEntry);
		
		//Read DATA block (since it's minimum required)
		FileBuffer chunk_data = useq.loadChunk("DATA");
		if(chunk_data == null) return null;
		readDATA(chunk_data, zseq);
		useq.data_len = chunk_data.intFromFile(0L);
		chunk_data.dispose();
		
		//Find META block
		chunk_data = useq.loadChunk("META");
		if(chunk_data != null){
			readMETA(chunk_data.getReferenceAt(0L), zseq, useq);
			chunk_data.dispose();
		}
		
		//Do modules, if applicable.
		if((useq.meta_flags & 0x08) != 0){
			chunk_data = useq.loadChunk("LMOD");
			if(chunk_data != null){
				readLMOD(chunk_data, zseq);
				chunk_data.dispose();
			}
		}
		
		//IOAlias
		if((useq.meta_flags & 0x10) != 0){
			chunk_data = useq.loadChunk("IOAL");
			if(chunk_data != null){
				readIOAL(chunk_data, zseq);
				chunk_data.dispose();
			}
		}
		
		//Labels
		chunk_data = useq.loadChunk("LABL");
		if(chunk_data != null){
			readLABL(chunk_data, zseq);
			chunk_data.dispose();
		}
		
		return zseq;
	}
	
	/*----- Writing -----*/
	
	private static FileBuffer serializeLABL(ZeqerSeq seq, Module[] modlist){
		//Collect all Labels.
		List<Label> list = new LinkedList<Label>();
		list.addAll(seq.getCommonLabels());
		if(modlist != null){
			for(Module m : modlist){
				list.addAll(m.getAllLabels());
			}
		}
		if(list.isEmpty()) return null;
		
		//Serialize
		int count = list.size();
		int alloc = 12 + (count << 2);
		for(Label lbl : list){
			alloc += 8 + lbl.getName().length() + 1;
		}
		
		FileBuffer LABL = new FileBuffer(alloc, true);
		LABL.printASCIIToFile("LABL");
		LABL.addToFile(0); //Chunk size.
		LABL.addToFile(count);
		for(int i = 0; i < count; i++) LABL.addToFile(0);
		
		int cpos = (int)LABL.getFileSize();
		int tpos = 12;
		for(Label lbl : list){
			LABL.addToFile((byte)lbl.getContext());
			Module lmod = lbl.getModule();
			if(lmod == null) LABL.addToFile((byte)-1);
			else LABL.addToFile((byte)lmod.getIndex());
			LABL.addToFile((short)lbl.getDataSize());
			LABL.addToFile((short)lbl.getPosition());
			LABL.addVariableLengthString("UTF8", lbl.getName(), BinFieldSize.WORD, 2);
			
			LABL.replaceInt(cpos, tpos);
			cpos = (int)LABL.getFileSize();
			tpos+=4;
		}
		while((LABL.getFileSize() & 0x3L) != 0L) LABL.addToFile(FileBuffer.ZERO_BYTE);
		
		LABL.replaceInt((int)LABL.getFileSize()-8, 4L);
		return LABL;
	}
	
	private static FileBuffer serializeIOAL(ZeqerSeq seq, Module[] modlist){
		//Collect
		List<IOAlias> list = new LinkedList<IOAlias>();
		list.addAll(seq.getCommonAliases());
		if(modlist != null){
			for(Module m : modlist){
				list.addAll(m.getAllIOAliases());
			}
		}
		if(list.isEmpty()) return null;
		
		//Serialize
		int count = list.size();
		int alloc = 12;
		for(IOAlias ioa : list){
			alloc += 4 + ioa.getName().length() + 3;
		}
		
		FileBuffer IOAL = new FileBuffer(alloc, true);
		IOAL.printASCIIToFile("IOAL");
		IOAL.addToFile(0);
		IOAL.addToFile(count);
		for(IOAlias ioa : list){
			IOAL.addToFile((byte)ioa.getChannel());
			IOAL.addToFile((byte)ioa.getSlot());
			Module mod = ioa.getModule();
			if(mod == null) IOAL.addToFile((byte)-1);
			else IOAL.addToFile((byte)mod.getIndex());
			IOAL.addToFile(FileBuffer.ZERO_BYTE);
			IOAL.addVariableLengthString("UTF8", ioa.getName(), BinFieldSize.WORD, 2);
		}
		while((IOAL.getFileSize() & 0x3L) != 0L) IOAL.addToFile(FileBuffer.ZERO_BYTE);
		
		IOAL.replaceInt((int)IOAL.getFileSize()-8, 4L);
		
		return IOAL;
	}
	
	private static FileBuffer serializeMETA(ZeqerSeq seq, boolean use_lmod, boolean use_ioal){
		SeqTableEntry tentry = seq.getTableEntry();
		int alloc = 20;
		if(tentry != null){
			alloc += tentry.getBankCount() << 2;	
		}
		
		int flags = 0;
		if(seq.isOoTCompatible()) flags |= 0x02;
		if(seq.canUseMoreThanFourLayers()) flags |= 0x04;
		if(use_lmod) flags |= 0x08;
		if(use_ioal) flags |= 0x10;
		if(seq.isSFXSeq()) flags |= 0x100;
		
		FileBuffer META = new FileBuffer(alloc, true);
		META.printASCIIToFile("META");
		META.addToFile(0);
		
		if(tentry != null){
			META.addToFile(tentry.getUID());
			META.addToFile((short)flags);
			META.addToFile((byte)seq.getMaxVoiceLoad());
			META.addToFile(FileBuffer.ZERO_BYTE);
			META.addToFile((byte)tentry.getMedium());
			META.addToFile((byte)tentry.getCache());
			
			List<Integer> banks = tentry.getLinkedBankUIDs();
			META.addToFile((byte)banks.size());
			for(Integer buid : banks) META.addToFile(buid);
		}
		else{
			META.addToFile(0); //GUID
			META.addToFile((short)flags);
			META.addToFile((byte)seq.getMaxVoiceLoad());
			META.addToFile(FileBuffer.ZERO_BYTE);
			META.addToFile((byte)Z64Sound.MEDIUM_CART);
			META.addToFile((byte)Z64Sound.CACHE_TEMPORARY);
			META.addToFile((byte)0); //No banks listed here...
		}
		
		while((META.getFileSize() & 0x3L) != 0L) META.addToFile(FileBuffer.ZERO_BYTE);
		META.replaceInt((int)META.getFileSize()-8, 4L);
		return META;
	}
	
	public static boolean writeUSEQ(ZeqerSeq seq, String path) throws IOException{
		NUSALSeq nseq = seq.getSequence();
		if(nseq == null) return false;
		return writeUSEQ(seq, path, nseq.getSerializedData());
	}
	
	public static boolean writeUSEQ(ZeqerSeq seq, String path, FileBuffer seqdata) throws IOException{
		if(seq == null) return false;
		int alloc = 0;
		long total_size = 0L;
		int ccount = 0;
		
		//First detangle labels, aliases, and modules...
		Module[] modlist = seq.getModules();
		FileBuffer LMOD = null;
		if(modlist != null){
			if(modlist.length > 0){
				alloc = 12 + (modlist.length << 2);
				for(Module m : modlist) alloc += m.getName().length() + 3;
			}
			LMOD = new FileBuffer(alloc, true);
			LMOD.printASCIIToFile("LMOD");
			LMOD.addToFile(0); //Chunk size
			LMOD.addToFile(modlist.length);
			for(int i = 0; i < modlist.length; i++) LMOD.addToFile(0); //Pos
			
			int cpos = (int)LMOD.getFileSize(); long tpos = 12;
			for(Module m : modlist){
				LMOD.replaceInt(cpos, tpos);
				LMOD.addVariableLengthString("UTF8", m.getName(), BinFieldSize.WORD, 2);
				cpos = (int)LMOD.getFileSize();
				tpos += 4;
			}
			while((LMOD.getFileSize() & 0x3L) != 0L) LMOD.addToFile(FileBuffer.ZERO_BYTE);
			
			LMOD.replaceInt((int)LMOD.getFileSize()-8, 4L);
			ccount++;
			total_size += LMOD.getFileSize();
		}
		
		FileBuffer LABL = serializeLABL(seq, modlist);
		if(LABL != null){
			ccount++;
			total_size += LABL.getFileSize();
		}
		
		FileBuffer IOAL = serializeIOAL(seq, modlist);
		if(IOAL != null){
			ccount++;
			total_size += IOAL.getFileSize();
		}
		
		//DATA
		if(seqdata == null){
			NUSALSeq nseq = seq.getSequence();
			if(nseq == null) return false;
			seqdata = nseq.getSerializedData();
		}
		FileBuffer DATA_header = new FileBuffer(12, true);
		int data_pad = (int)seqdata.getFileSize() % 4;
		DATA_header.printASCIIToFile("DATA");
		DATA_header.addToFile(4 + data_pad + (int)seqdata.getFileSize());
		DATA_header.addToFile((int)seqdata.getFileSize());
		ccount++;
		total_size += DATA_header.getFileSize() + data_pad + seqdata.getFileSize();
		
		//META
		FileBuffer META = serializeMETA(seq, LABL != null, IOAL != null);
		ccount++;
		total_size = META.getFileSize();
		
		//Header
		FileBuffer header = new FileBuffer(HEADER_SIZE, true);
		header.printASCIIToFile(MAGIC);
		header.addToFile((short)0xFEFF);
		header.addToFile(MAJOR_VERSION);
		header.addToFile(MINOR_VERSION);
		header.addToFile((int)total_size + HEADER_SIZE);
		header.addToFile((short)HEADER_SIZE);
		header.addToFile((short)ccount);
		
		//Write!
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path));
		header.writeToStream(bos);
		META.writeToStream(bos);
		DATA_header.writeToStream(bos);
		seqdata.writeToStream(bos);
		for(int i = 0; i < data_pad; i++) bos.write(0);
		if(LMOD != null) LMOD.writeToStream(bos);
		if(LABL != null) LABL.writeToStream(bos);
		if(IOAL != null) IOAL.writeToStream(bos);
		bos.close();
		
		return true;
	}
	
}
