package waffleoRai_zeqer64;

import java.util.Arrays;

import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_Utils.BufferReference;
import waffleoRai_Utils.FileBuffer;

public class SoundTables {
	
	public static final int MEDIUM_RAM = Z64Sound.MEDIUM_RAM;
	public static final int MEDIUM_UNK = Z64Sound.MEDIUM_UNK;
	public static final int MEDIUM_CART = Z64Sound.MEDIUM_CART;
	public static final int MEDIUM_DISK_DRIVE = Z64Sound.MEDIUM_DISK_DRIVE;
	
	public static final int CACHE__PERMANENT = Z64Sound.CACHE_PERMANENT;
	public static final int CACHE__PERSISTENT = Z64Sound.CACHE_PERSISTENT;
	public static final int CACHE__TEMPORARY = Z64Sound.CACHE_TEMPORARY;
	public static final int CACHE__ANY = Z64Sound.CACHE_ANY;
	public static final int CACHE__ANY_NO_SYNC_LOAD = Z64Sound.CACHE_ANYNOSYNCLOAD;

	public static class BankInfoEntry{
		private int offset = -1;
		private int size = 0;
		private int medium = MEDIUM_CART;
		private int cachePolicy = CACHE__TEMPORARY;
		private int warc1 = -1;
		private int warc2 = -1;
		private int inst_count = 0;
		private int perc_count = 0;
		private int sfx_count = 0;
		
		public static BankInfoEntry readEntry(BufferReference ptr){
			BankInfoEntry entry = new BankInfoEntry();
			entry.offset = ptr.nextInt();
			entry.size = ptr.nextInt();
			entry.medium = (int)ptr.nextByte();
			entry.cachePolicy = (int)ptr.nextByte();
			entry.warc1 = (int)ptr.nextByte();
			entry.warc2 = (int)ptr.nextByte();
			entry.inst_count = (int)ptr.nextByte();
			entry.perc_count = (int)ptr.nextByte();
			entry.sfx_count = (int)ptr.nextShort();
			return entry;
		}
		
		public int getOffset(){return offset;}
		public int getSize(){return size;}
		public int getMedium(){return medium;}
		public int getCachePolicy(){return cachePolicy;}
		public int getPrimaryWaveArcIndex(){return warc1;}
		public int getSecondaryWaveArcIndex(){return warc2;}
		public int getInstrumentCount(){return inst_count;}
		public int getPercussionCount(){return perc_count;}
		public int getSFXCount(){return sfx_count;}
		
		public void setOffset(int val){offset = val;}
		public void setSize(int val){size = val;}
		public void setMedium(int val){medium = val;}
		public void setCachePolicy(int val){cachePolicy = val;}
		public void setPrimaryWaveArcIndex(int val){warc1 = val;}
		public void setSecondaryWaveArcIndex(int val){warc2 = val;}
		public void setInstrumentCount(int val){inst_count = val;}
		public void setPercussionCount(int val){perc_count = val;}
		public void setSFXCount(int val){sfx_count = val;}
		
		public void serializeTo(FileBuffer buffer){
			buffer.addToFile(offset);
			buffer.addToFile(size);
			buffer.addToFile((byte)medium);
			buffer.addToFile((byte)cachePolicy);
			buffer.addToFile((byte)warc1);
			buffer.addToFile((byte)warc2);
			buffer.addToFile((byte)inst_count);
			buffer.addToFile((byte)perc_count);
			buffer.addToFile((short)sfx_count);
		}
		
	}
	
	public static class BankMapEntry{
		private int[] banklist;
		
		public BankMapEntry(int size){
			banklist = new int[size];
			Arrays.fill(banklist, -1);
		}
		
		public int getBank(int idx){
			if(idx < 0) return -1;
			if(idx >= banklist.length) return -1;
			return banklist[idx];
		}
				
		public int getBankCount(){
			return banklist.length;
		}
		
		public void setBank(int idx, int val){
			if(idx < 0) return;
			if(idx >= banklist.length) return;
			banklist[idx] = val;
		}
		
	}
	
	public static class SeqInfoEntry{
		private int offset = -1;
		private int size = 0;
		private int medium = MEDIUM_CART;
		private int cachePolicy = CACHE__TEMPORARY;
		
		public static SeqInfoEntry readEntry(BufferReference ptr){
			SeqInfoEntry entry = new SeqInfoEntry();
			entry.offset = ptr.nextInt();
			entry.size = ptr.nextInt();
			entry.medium = (int)ptr.nextByte();
			entry.cachePolicy = (int)ptr.nextByte();
			ptr.add(6);
			return entry;
		}
		
		public int getOffset(){return offset;}
		public int getSize(){return size;}
		public int getMedium(){return medium;}
		public int getCachePolicy(){return cachePolicy;}
		
		public void setOffset(int val){offset = val;}
		public void setSize(int val){size = val;}
		public void setMedium(int val){medium = val;}
		public void setCachePolicy(int val){cachePolicy = val;}
		
		public void serializeTo(FileBuffer buffer){
			buffer.addToFile(offset);
			buffer.addToFile(size);
			buffer.addToFile((byte)medium);
			buffer.addToFile((byte)cachePolicy);
			buffer.addToFile((short)0);
			buffer.addToFile(0);
		}
	}
	
	public static class WaveArcInfoEntry{
		private int offset = -1;
		private int size = 0;
		private int medium = MEDIUM_CART;
		private int cachePolicy = CACHE__ANY_NO_SYNC_LOAD;
		
		public static WaveArcInfoEntry readEntry(BufferReference ptr){
			WaveArcInfoEntry entry = new WaveArcInfoEntry();
			entry.offset = ptr.nextInt();
			entry.size = ptr.nextInt();
			entry.medium = (int)ptr.nextByte();
			entry.cachePolicy = (int)ptr.nextByte();
			ptr.add(6);
			return entry;
		}
		
		public int getOffset(){return offset;}
		public int getSize(){return size;}
		public int getMedium(){return medium;}
		public int getCachePolicy(){return cachePolicy;}
		
		public void setOffset(int val){offset = val;}
		public void setSize(int val){size = val;}
		public void setMedium(int val){medium = val;}
		public void setCachePolicy(int val){cachePolicy = val;}
		
		public void serializeTo(FileBuffer buffer){
			//In my script I didn't see the need to add a 64-bit ver? I guess the struct is fixed size?
			buffer.addToFile(offset);
			buffer.addToFile(size);
			buffer.addToFile((byte)medium);
			buffer.addToFile((byte)cachePolicy);
			buffer.addToFile((short)0);
			buffer.addToFile(0);
		}
	}
	
}
