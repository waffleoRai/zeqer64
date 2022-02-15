package waffleoRai_zeqer64;

import java.util.Arrays;

import waffleoRai_Utils.BufferReference;

public class SoundTables {
	
	public static final int MEDIUM_RAM = 0;
	public static final int MEDIUM_UNK = 1;
	public static final int MEDIUM_CART = 2;
	public static final int MEDIUM_DISK_DRIVE = 3;
	
	public static final int CACHE__PERMANENT = 0;
	public static final int CACHE__PERSISTENT = 1;
	public static final int CACHE__TEMPORARY = 2;
	public static final int CACHE__ANY = 3;
	public static final int CACHE__ANY_NO_SYNC_LOAD = 4;

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
	}
	
}
