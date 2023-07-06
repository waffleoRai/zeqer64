package waffleoRai_zeqer64.filefmt;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import waffleoRai_Utils.FileBuffer;

public class UltraFile{
	
	public static final int HEADER_SIZE = 16;
	
	/*--- Instance Variables ---*/
	
	private String file_magic = null;
	private int ver_major = 0;
	private int ver_minor = 0;
	
	private Map<String, FileBuffer> chunks;
	private List<String> chunk_order;
	
	/*--- Init ---*/
	
	private UltraFile(){
		chunks = new HashMap<String, FileBuffer>();
		chunk_order = new ArrayList<String>();
	}
	
	/*--- Getters ---*/
	
	public String getFileMagic(){return file_magic;}
	public int getMajorVersion(){return ver_major;}
	public int getMinorVersion(){return ver_minor;}
	
	public boolean hasChunk(String chunk_id){
		return chunks.containsKey(chunk_id);
	}
	
	public FileBuffer getChunkData(String chunk_id){
		return chunks.get(chunk_id);
	}
	
	/*--- Setters ---*/
	
	public int addOrReplaceChunk(String chunk_id, FileBuffer data, int pos){
		if(data == null || chunk_id == null) return -1;
		if(chunks.containsKey(chunk_id)){
			FileBuffer old = chunks.remove(chunk_id);
			try{old.dispose();}
			catch(Exception ex){ex.printStackTrace(); return -1;}
			chunks.put(chunk_id, data);
		}
		else{
			chunks.put(chunk_id, data);
			if(pos >= 0){
				chunk_order.add(pos, chunk_id);
			}
		}
		return chunk_order.indexOf(chunk_id);
	}
	
	public void close(){
		chunk_order.clear();
		for(FileBuffer chunk : chunks.values()){
			try{chunk.dispose();}
			catch(IOException ex){ex.printStackTrace();}
		}
		chunks.clear();
	}
	
	/*--- Read ---*/
	
	public static UltraFile openUltraFile(String path) throws IOException{
		FileBuffer buffer = FileBuffer.createBuffer(path, true);
		return openUltraFile(buffer);
	}
	
	public static UltraFile openUltraFile(FileBuffer data) throws IOException{
		data.setEndian(true);
		UltraFile file = new UltraFile();
		
		file.file_magic = data.getASCII_string(0L, 4);
		data.setCurrentPosition(4L);
		
		int bom = Short.toUnsignedInt(data.nextShort());
		if(bom == 0xFFFE) data.setEndian(false);
		
		file.ver_major = data.nextByte(); //Major version
		file.ver_minor = data.nextByte(); //Minor version
		data.nextInt(); //Total file size
		data.nextShort(); //Header size
		int ccount = Short.toUnsignedInt(data.nextShort());
		
		String cmag = null;
		int csz = 0;
		for(int i = 0; i < ccount; i++){
			long pos = data.getCurrentPosition();
			cmag = data.getASCII_string(pos, 4);
			data.skipBytes(4L);
			csz = data.nextInt();
			
			FileBuffer chunkdat = data.createCopy(pos + 8, pos + csz + 8);
			file.chunks.put(cmag, chunkdat);
			file.chunk_order.add(cmag);
			
			data.skipBytes(csz);
		}
		
		return file;
	}
	
	/*--- Write ---*/
	
	public void writeTo(String path) throws IOException{
		//Calculate full file size (for header)
		int ccount = chunk_order.size();
		long fsize = HEADER_SIZE;
		
		for(String cid : chunk_order){
			FileBuffer dat = chunks.get(cid);
			fsize += (dat.getFileSize() + 8 + 0x3) & ~0x3;
		}
		
		FileBuffer header = new FileBuffer(HEADER_SIZE, true);
		header.printASCIIToFile(file_magic);
		header.addToFile((short)0xfeff);
		header.addToFile((byte)ver_major);
		header.addToFile((byte)ver_minor);
		header.addToFile(fsize);
		header.addToFile((short)HEADER_SIZE);
		header.addToFile((short)ccount);
		
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path));
		
		for(String cid : chunk_order){
			FileBuffer dat = chunks.get(cid);
			long trgsize = (dat.getFileSize() + 0x3) & ~0x3;
			int pad = (int)(trgsize - dat.getFileSize());
			
			FileBuffer chdr = new FileBuffer(8, true);
			chdr.printASCIIToFile(cid);
			chdr.addToFile((int)trgsize);
			
			chdr.writeToStream(bos);
			dat.writeToStream(bos);
			for(int i = 0; i < pad; i++){
				bos.write(0);
			}
		}
		
		bos.close();
	}

}
