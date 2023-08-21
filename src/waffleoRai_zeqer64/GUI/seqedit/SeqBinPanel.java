package waffleoRai_zeqer64.GUI.seqedit;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JScrollPane;
import java.awt.GridBagConstraints;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import waffleoRai_SeqSound.n64al.NUSALSeq;
import waffleoRai_SeqSound.n64al.NUSALSeqCommand;
import waffleoRai_SeqSound.n64al.NUSALSeqCommandSource;
import waffleoRai_SeqSound.n64al.cmd.NUSALSeqCommandChunk;
import waffleoRai_SeqSound.n64al.cmd.NUSALSeqReader;
import waffleoRai_Utils.FileBuffer;

import java.awt.Insets;
import java.util.List;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.ListSelectionModel;

public class SeqBinPanel extends JPanel{

	private static final long serialVersionUID = -3115545520002780182L;
	
	public static final int MIN_COL_WIDTH_OFFSET = 20;
	public static final int MIN_COL_WIDTH_BYTE = 5;
	
	private static final byte CELL_EMPTY = 0;
	private static final byte CELL_SEQ1 = 1;
	private static final byte CELL_SEQ2 = 2;
	private static final byte CELL_CH_A1 = 3;
	private static final byte CELL_CH_A2 = 4;
	private static final byte CELL_CH_B1 = 5;
	private static final byte CELL_CH_B2 = 6;
	private static final byte CELL_LYR_A1 = 7;
	private static final byte CELL_LYR_A2 = 8;
	private static final byte CELL_LYR_B1 = 9;
	private static final byte CELL_LYR_B2 = 10;
	
	private static final Color RED1 = new Color(224, 83, 83);
	private static final Color RED2 = new Color(247, 151, 151);
	
	private static final Color ORANGE1 = new Color(230, 188, 76);
	private static final Color ORANGE2 = new Color(255, 223, 138);
	private static final Color YELLOW1 = new Color(232, 228, 102);
	private static final Color YELLOW2 = new Color(255, 251, 138);
	
	private static final Color BLUE1 = new Color(136, 139, 227);
	private static final Color BLUE2 = new Color(199, 201, 255);
	private static final Color PURPLE1 = new Color(237, 135, 224);
	private static final Color PURPLE2 = new Color(255, 196, 248);
	
	private static final Color DARK_GREY = new Color(20, 20, 20);
	
	private static final Color[] CLUT = {Color.WHITE, RED1, RED2,
						ORANGE1, ORANGE2, YELLOW1, YELLOW2,
						BLUE1, BLUE2, PURPLE1, PURPLE2};
	
	private static final Font FONT_BASE = new Font("Courier New", Font.PLAIN, 11);
	private static final Font FONT_BOLD = new Font("Courier New", Font.BOLD, 11);
	
	/*----- Instance Variables -----*/
	
	private JScrollPane scrollPane;
	private JTable table;
	
	private	byte[] cellColors;
	private byte[] data;
	//private String[][] cellContents;
	
	//Command loader state
	private boolean flipSeq;
	private boolean flipChMajor;
	private boolean flipChMinor;
	private boolean flipLyMajor;
	private boolean flipLyMinor;
	
	private int lastCh = -1;
	private int lastLyCh = -1;
	private int lastLy = -1;
	
	/*----- Init -----*/
	
	public SeqBinPanel(){
		initGUI();
	}
	
	private void initGUI(){
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(5, 5, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		add(scrollPane, gbc_scrollPane);
		
		table = new JTable();
		scrollPane.setViewportView(table);
		
		initTable();
	}
	
	private void initTable(){
		table.setModel(new MyModel());
		table.setFont(FONT_BASE);
		table.setFillsViewportHeight(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		table.setRowSelectionAllowed(false);
		//table.setColumnSelectionAllowed(false);
		table.setCellSelectionEnabled(true);
		table.setDefaultRenderer(String.class, new MyRenderer());
		updateTableColumnWidths();
	}
	
	private void allocTable(int binsize){
		binsize = (binsize + 0xf) & ~0xf;
		cellColors = new byte[binsize];
		data = new byte[binsize];
		updateTableColumnWidths();
	}
	
	/*----- Getters -----*/
	
	/*----- Setters -----*/
	
	public void loadSeq(NUSALSeq seq){
		if(seq != null){
			NUSALSeqCommandSource cmdsrc = seq.getAllCommands();
			if(cmdsrc != null){
				List<NUSALSeqCommand> cmdlist = cmdsrc.getOrderedCommands();
				if(cmdlist != null){
					//Initialize data structures
					int binsize = (int)(seq.getSerializedData().getFileSize()); //Don't like this, need to add a size estimate method...
					allocTable(binsize);
					
					//Load commands
					for(NUSALSeqCommand cmd : cmdlist){
						addCommand(cmd);
					}
				}
			}
		}

		table.repaint();
		scrollPane.repaint();
	}
	
	public void loadRawSeq(FileBuffer rawdat){
		if(rawdat != null){
			int binsize = (int)(rawdat.getFileSize());
			allocTable(binsize);
			
			//Preload data strings
			for(int i = 0; i < binsize; i++) data[i] = rawdat.getByte(i);
			
			//Try a partial parse...
			NUSALSeqReader seqreader = new NUSALSeqReader(rawdat);
			try {seqreader.preParse(true, binsize, binsize);} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			List<NUSALSeqCommand> cmdlist = seqreader.getOrderedCommands();
			if(cmdlist != null){
				for(NUSALSeqCommand cmd : cmdlist) addCommand(cmd);
			}
		}

		table.repaint();
		scrollPane.repaint();
	}
	
	public boolean jumpTo(int addr, boolean triggerCallbacks){
		if(data == null) return false;
		if(addr < 0) return false;
		if(addr >= data.length) return false;
		
		int row = addr >>> 4;
		int col = (addr & 0xf) + 1;
		
		table.changeSelection(row, col, false, false);
		
		if(triggerCallbacks) fireCallbacks();
		table.repaint();
		scrollPane.repaint();
		return true;
	}
	
	/*----- Internal -----*/
	
	private void updateTableColumnWidths(){
		TableColumnModel tcm = table.getColumnModel();
		tcm.getColumn(0).setMinWidth(MIN_COL_WIDTH_OFFSET);
		tcm.getColumn(0).setPreferredWidth(MIN_COL_WIDTH_OFFSET);
		for(int i = 1; i <= 16; i++){
			tcm.getColumn(i).setMinWidth(MIN_COL_WIDTH_BYTE);
			tcm.getColumn(i).setPreferredWidth(MIN_COL_WIDTH_BYTE);
		}
	}
	
	private void addCommand(NUSALSeqCommand cmd){
		if(cmd instanceof NUSALSeqCommandChunk){
			NUSALSeqCommandChunk ccmd = (NUSALSeqCommandChunk)cmd;
			List<NUSALSeqCommand> children = ccmd.getCommands();
			for(NUSALSeqCommand child : children) addCommand(child);
		}
		else{
			int addr = cmd.getAddress();
			byte[] ser = cmd.serializeMe();
			if(ser == null) return;
			byte cellClr = CELL_EMPTY;
			
			//Figure out cell color
			if(cmd.seqUsed()){
				//Seq
				if(flipSeq) cellClr = CELL_SEQ2;
				else cellClr = CELL_SEQ1;
				flipSeq = !flipSeq;
			}
			else{
				int[] firstused = cmd.getFirstUsed();
				if(firstused[1] < 0){
					//Channel command
					if((lastCh >= 0) && (firstused[0] != lastCh)){
						flipChMajor = !flipChMajor;
					}
					
					if(flipChMajor){
						if(flipChMinor) cellClr = CELL_CH_B2;
						else cellClr = CELL_CH_B1;
					}
					else{
						if(flipChMinor) cellClr = CELL_CH_A2;
						else cellClr = CELL_CH_A1;
					}
					
					flipChMinor = !flipChMinor;
					lastCh = firstused[0];
				}
				else{
					//Layer
					if(((lastLyCh >= 0) && (firstused[0] != lastLyCh)) ||
							((lastLy >= 0) && (firstused[1] != lastLy))){
						flipLyMajor = !flipLyMajor;
					}
					
					if(flipLyMajor){
						if(flipLyMinor) cellClr = CELL_LYR_B2;
						else cellClr = CELL_LYR_B1;
					}
					else{
						if(flipLyMinor) cellClr = CELL_LYR_A2;
						else cellClr = CELL_LYR_A1;
					}
					
					flipLyMinor = !flipLyMinor;
					lastLyCh = firstused[0];
					lastLy = firstused[1];
				}
			}
			
			//Set panel data
			int sz = ser.length;
			for(int i = 0; i < sz; i++){
				cellColors[addr + i] = cellClr;
				data[addr + i] = ser[i];
				//int r = (addr + i) >>> 4;
				//int l = ((addr + i) & 0xF) + 1;
				//cellContents[r][l] = String.format("%02x", ser[i]);
			}
		}
	}
	
	/*----- Drawing -----*/
	
	private class MyModel extends AbstractTableModel{

		private static final long serialVersionUID = -6232541421411773537L;
		
		private String[] colNames;
		
		public MyModel(){
			colNames = getColHeaders();
		}
		
		public int getRowCount() {
			if(data == null) return 0;
			return (data.length + 0xf) >>> 4;
		}

		public int getColumnCount() {
			return 17;
		}
		
		public String getColumnName(int column){
			return colNames[column];
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if(data == null) return null;
			if(rowIndex < 0 || columnIndex < 0) return null;
			if(columnIndex > 16) return null;
			
			int maxRow = ((data.length + 0xf) & ~0xf);
			if(rowIndex > maxRow) return null;
			
			int off = rowIndex << 4;
			if(columnIndex == 0){
				return String.format("%04x", off);
			}
			
			off += (columnIndex - 1) & 0xf;
			if(off >= data.length) return "00";
			
			return String.format("%02x", Byte.toUnsignedInt(data[off]));
		}
		
		public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

		public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }
		
	}
	
	private class MyRenderer extends DefaultTableCellRenderer{
		private static final long serialVersionUID = -7176516673265856875L;

		public Component getTableCellRendererComponent(
				JTable table, Object value, boolean isSelected,
	            boolean hasFocus, int row, int column) {
			
			Component c = super.getTableCellRendererComponent(
	                table, value, isSelected, hasFocus, row, column);
			
			//First column ("row labels") should be bold with greyish bkg
			c.setForeground(Color.BLACK);
			if(column > 0){
				if(hasFocus || isSelected){
					c.setBackground(DARK_GREY);
					c.setForeground(Color.WHITE);
				}
				else{
					int addr = row << 4;
					addr |= (column - 1);
					if(cellColors != null){
						if((addr >= 0) && (addr < cellColors.length)){
							byte clri = cellColors[addr];
							if(clri >= 0){
								c.setBackground(CLUT[clri]);
							}
							else c.setBackground(Color.WHITE);
						}
						else c.setBackground(Color.WHITE);
					}
					else c.setBackground(Color.WHITE);
				}
			}
			else{
				c.setFont(FONT_BOLD);
				c.setBackground(Color.LIGHT_GRAY);
			}
			
			return c;
		}
	}
	
	private String[] getColHeaders(){
		String[] cols = new String[17];
		cols[0] = "POS";
		for(int i = 0; i < 10; i++){
			cols[i+1] = Integer.toString(i);
		}
		for(int i = 0; i < 6; i++){
			cols[i+11] = "" + (char)('a' + i);
		}
		return cols;
	}
	
	public void disable(){
		table.setEnabled(false);
		scrollPane.setEnabled(false);
		table.repaint();
		scrollPane.repaint();
	}
	
	public void enable(){
		table.setEnabled(true);
		scrollPane.setEnabled(true);
		table.repaint();
		scrollPane.repaint();
	}
	
	/*----- Callbacks -----*/
	
	private void fireCallbacks(){
		//TODO
	}
	
}
