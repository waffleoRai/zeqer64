package waffleoRai_zeqer64.extract;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import waffleoRai_Utils.FileBuffer;
import waffleoRai_Utils.FileBuffer.UnsupportedFileTypeException;
import waffleoRai_Utils.FileUtils;
import waffleoRai_soundbank.nintendo.Z64Bank;
import waffleoRai_soundbank.nintendo.Z64Bank.WaveInfoBlock;
import waffleoRai_zeqer64.ZeqerCore;
import waffleoRai_zeqer64.ZeqerRom;
import waffleoRai_zeqer64.filefmt.NusRomInfo;
import waffleoRai_zeqer64.filefmt.ZeqerBankTable;
import waffleoRai_zeqer64.filefmt.ZeqerBankTable.BankTableEntry;

public class BankExtractor {
	
	/*----- Constants -----*/
	
	private static final char SEP = File.separatorChar;
	
	/*----- Instance Variables -----*/
	
	private boolean write_perm; //Determines if sys tables are updated
	
	private String dir_base; //Expecting %PROGRAM_DIR%\bnk\zbnks
	private ZeqerRom z_rom;
	
	private ZeqerBankTable table;
	
	/*----- Init -----*/
	
	public BankExtractor(ZeqerRom rom) throws IOException{
		this(rom, ZeqerCore.getProgramDirectory() + SEP + ZeqerCore.DIRNAME_BANK + SEP + ZeqerCore.DIRNAME_ZBANK);
	}
	
	public BankExtractor(ZeqerRom rom, String base_dir) throws IOException{
		this(rom, base_dir, null);
	}
	
	public BankExtractor(ZeqerRom rom, String base_dir, ZeqerBankTable tbl) throws IOException{
		if(rom == null) throw new IllegalArgumentException("ROM is required for extraction.");
		
		dir_base = base_dir;
		table = tbl;
		z_rom = rom;
		
		if(table == null){
			String path = getTablePath();
			if(FileBuffer.fileExists(path)){
				//Load
				try {
					table = ZeqerBankTable.readTable(FileBuffer.createBuffer(path, true));
				}
				catch (UnsupportedFileTypeException e) {
					e.printStackTrace();
					throw new IOException();
				}
			}
			else{
				//New
				table = ZeqerBankTable.createTable();
			}
		}
	}
	
	/*----- Paths -----*/
	
	public String getTablePath(){
		return dir_base + SEP + ZeqerCore.FN_SYSBANK;
	}
	
	public String getIDTablePath(){
		return dir_base + SEP + "bnk_" + z_rom.getRomInfo().getZeqerID() + ".bin";
	}
	
	/*----- Setters -----*/
	
	public void setSysPermission(boolean b){write_perm = b;}
	
	/*----- Extraction -----*/

	public boolean extractBanks(Map<Integer, Integer> wavloc_map) throws IOException{
		if(wavloc_map == null) return false;
		if(z_rom == null) return false;
		boolean good = true;
		
		NusRomInfo rominfo = z_rom.getRomInfo();
		if(rominfo == null) return false;
		FileBuffer code = z_rom.loadCode(); code.setEndian(true);
		FileBuffer audiobank = z_rom.loadAudiobank(); audiobank.setEndian(true);
		
		//Read bank table
		code.setCurrentPosition(rominfo.getCodeOffset_bnktable());
		int bcount = Short.toUnsignedInt(code.nextShort());
		int[] uid_tbl = new int[bcount];
		code.skipBytes(14);
		for(int i = 0; i < bcount; i++){	
			boolean skip = false;
			long stoff = Integer.toUnsignedLong(code.nextInt());
			long size = Integer.toUnsignedLong(code.nextInt());
			byte[] ub = new byte[3];
			ub[0] = code.nextByte();
			ub[1] = code.nextByte();
			int widx = Byte.toUnsignedInt(code.nextByte());
			ub[2] = code.nextByte();
			int i0 = Byte.toUnsignedInt(code.nextByte());
			int i1 = Byte.toUnsignedInt(code.nextByte());
			int i2 = Short.toUnsignedInt(code.nextShort());
			
			//This needs to be NEW copy so can modify!
			FileBuffer bdat = audiobank.createCopy(stoff, stoff+size);
			//System.err.println("DEBUG -- Bank Data Size: 0x" + Long.toHexString(bdat.getFileSize()));
			Z64Bank bank = Z64Bank.readBank(bdat, i0, i1, i2);
			int wmask = widx << 24;
			List<WaveInfoBlock> wblocks = bank.getAllWaveInfoBlocks();
			for(WaveInfoBlock wb : wblocks){
				//Base is the second word in the record. That's what we read and update.
				int addr = wb.getBankOffset() + 4;
				int woff = wb.getOffset();
				woff |= wmask;
				if(!wavloc_map.containsKey(woff)){
					System.err.println("BankExtractor.extractBanks || Failed to ID sound at audiotable 0x" + Integer.toHexString(woff));
					skip = true;
					break;
				}
				//System.err.println("DEBUG -- Wave Block addr: 0x" + Integer.toHexString(addr));
				int wid = wavloc_map.get(woff);
				bdat.replaceInt(wid, addr);
			}
			if(skip) continue;
			
			//Otherwise, continue processing bank.
			//Now we can hash it.
			byte[] hash = FileUtils.getMD5Sum(bdat.getBytes());
			String md5str = FileUtils.bytes2str(hash).toLowerCase();
			BankTableEntry entry = table.matchSequenceMD5(md5str);
			if(entry == null){
				//New (if write perm)
				if(write_perm){
					entry = table.newEntry(hash);
					entry.setInstCounts(i0, i1, i2);
					//entry.setUnknownWord(unkw);
					for(int j = 0; j < 3; j++) entry.setUnkByte(j, ub[j]);
					entry.setWarcIndex(widx);
					
					String dpath = dir_base + SEP + entry.getDataFileName();
					if(!FileBuffer.fileExists(dpath)){
						//Copy Data
						bdat.writeFile(dpath);
					}
					uid_tbl[i] = entry.getUID();
				}
			}
			else{
				String dpath = dir_base + SEP + entry.getDataFileName();
				if(!FileBuffer.fileExists(dpath)){
					//Copy Data
					bdat.writeFile(dpath);
				}
				uid_tbl[i] = entry.getUID();
			}
		}
		
		//Write
		String idtblpath = getIDTablePath();
		FileBuffer outbuff = new FileBuffer(bcount << 2, true);
		for(int i = 0; i < bcount; i++){
			outbuff.addToFile(uid_tbl[i]);
		}
		outbuff.writeFile(idtblpath);
		
		if(write_perm){
			table.writeTo(getTablePath());
		}
		
		return good;
	}
	
}
