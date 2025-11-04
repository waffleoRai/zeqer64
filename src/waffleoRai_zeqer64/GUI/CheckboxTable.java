package waffleoRai_zeqer64.GUI;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.JScrollPane;
import java.awt.GridBagConstraints;
import javax.swing.border.BevelBorder;

public class CheckboxTable extends JPanel{
	
	//TODO Force column widths to match.
	//TODO Word wrap header text
	
	/*----- Constants -----*/

	private static final long serialVersionUID = 7858375560535984388L;
	
	public static final int COLTYPE_STRING = 0;
	public static final int COLTYPE_CHECKBOX = 1;
	
	/*----- Inner Classes -----*/
	
	private class Row{
		public boolean rowEnabled = true;
		
		public boolean[] cellEnabled;
		public JComponent[] cells;
		
		public int rowIndex = -1;
		
		public Row(){
			int colCount = colTypes.length;
			cellEnabled = new boolean[colCount];
			Arrays.fill(cellEnabled, true);
			
			cells = new JComponent[colCount];
			for(int c = 0; c < colCount; c++){
				if(colTypes[c] == COLTYPE_CHECKBOX){
					JCheckBox cb = new JCheckBox("");
					cb.setEnabled(true);
					cb.setSelected(false);
					cb.setToolTipText(colNames[c]);
					cells[c] = cb;
					
					final int cc = c;
					
					cb.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent e) {
							if(cbCallbacks[cc] != null){
								cbCallbacks[cc].onAction(rowIndex, cb);
							}
						}
					});
				}
				else{
					//String
					JLabel lbl = new JLabel(colNames[c]);
					cells[c] = lbl;
				}
			}
		}
		
		public void updateEnabling(){
			if(cells == null) return;
			for(int c = 0; c < cells.length; c++){
				if(cells[c] != null){
					cells[c].setEnabled(rowEnabled && cellEnabled[c] && colEnabled[c]);
					cells[c].repaint();
				}
			}
		}
		
		public void disableAllComp(){
			if(cells == null) return;
			for(int c = 0; c < cells.length; c++){
				if(cells[c] != null){
					cells[c].setEnabled(false);
					cells[c].repaint();
				}
			}
		}
		
		public void repaintAllComp(){
			if(cells == null) return;
			for(int c = 0; c < cells.length; c++){
				if(cells[c] != null){
					cells[c].repaint();
				}
			}
		}
		
		public void updateColumnName(String name, int index){
			if(cells == null) return;
			if(index >= cells.length) return;
			JComponent c = cells[index];
			if(c == null) return;
			
			if(c instanceof JCheckBox){
				c.setToolTipText(name);
				c.repaint();
			}
		}
		
		public void addToPanel(JPanel panel){
			if(cells == null) return;
			int colCount = cells.length;
			
			for(int i = 0; i < colCount; i++){
				if(cells[i] == null) continue;
				
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx = i;
				gbc.gridy = rowIndex;
				
				switch(colTypes[i]){
				case COLTYPE_STRING:
					gbc.anchor = GridBagConstraints.WEST;
					gbc.fill = GridBagConstraints.BOTH;
					break;
				case COLTYPE_CHECKBOX:
					gbc.anchor = GridBagConstraints.CENTER;
					gbc.fill = GridBagConstraints.NONE;
					break;
				}
				
				panel.add(cells[i], gbc);
			}
		}
		
	}
	
	public static interface CheckCallback{
		public void onAction(int row, JCheckBox cb);
	}
	
	/*----- Instance Variables -----*/
	
	private int[] colTypes;
	private String[] colNames;
	private int[] colWidths;
	private boolean[] colEnabled;
	private CheckCallback[] cbCallbacks; //For each column.
	
	private Row[] contents;
	
	private GridBagLayout tableLayout;
	private JScrollPane spScroll;
	private JPanel pnlTable;
	
	private JCheckBox[] hdrCheckboxes;
	private JLabel[] hdrLabels;
	
	/*----- Init -----*/
	
	public CheckboxTable(int[] columns){
		if(columns == null){
			columns = new int[]{COLTYPE_STRING};
		}
		
		colTypes = columns;
		colNames = new String[colTypes.length];
		colWidths = new int[colTypes.length];
		colEnabled = new boolean[colTypes.length];
		cbCallbacks = new CheckCallback[colTypes.length];
		
		Arrays.fill(colEnabled, true);
		
		for(int i = 0; i < colTypes.length; i++){
			colNames[i] = "Column " + i;
		}
		
		initGUI();
	}
	
	private void initGUI(){
		int colCount = colTypes.length;
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		
		gridBagLayout.columnWidths = new int[colCount + 1];
		gridBagLayout.columnWeights = defaultWeightsArray(colCount);
		setLayout(gridBagLayout);
		
		//Generate header labels and checkboxes...
		hdrLabels = new JLabel[colTypes.length];
		hdrCheckboxes = new JCheckBox[colTypes.length];
		for(int c = 0; c < colTypes.length; c++){
			hdrLabels[c] = new JLabel(colNames[c]);
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			gbc.anchor = GridBagConstraints.CENTER;
			gbc.gridx = c;
			gbc.gridy = 0;
			add(hdrLabels[c], gbc);
			
			if(colTypes[c] == COLTYPE_CHECKBOX){
				hdrCheckboxes[c] = new JCheckBox("");
				hdrCheckboxes[c].setToolTipText(colNames[c]);
				hdrCheckboxes[c].setSelected(false);
				hdrCheckboxes[c].setEnabled(true);
				
				final int cc = c;
				hdrCheckboxes[c].addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						onHeaderCheckboxSelect(cc);
					}
				});
				
				gbc = new GridBagConstraints();
				gbc.fill = GridBagConstraints.NONE;
				gbc.anchor = GridBagConstraints.CENTER;
				gbc.gridx = c;
				gbc.gridy = 1;
				add(hdrCheckboxes[c], gbc);
			}
		}
		
		spScroll = new JScrollPane();
		spScroll.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spScroll = new GridBagConstraints();
		gbc_spScroll.fill = GridBagConstraints.BOTH;
		gbc_spScroll.gridx = 0;
		gbc_spScroll.gridy = 2;
		gbc_spScroll.gridwidth = colTypes.length;
		add(spScroll, gbc_spScroll);
		
		pnlTable = new JPanel();
		tableLayout = new GridBagLayout();
		tableLayout.columnWidths = new int[colCount + 1];
		tableLayout.columnWeights = defaultWeightsArray(colCount);
		
		pnlTable.setLayout(tableLayout);
		
		spScroll.setViewportView(pnlTable);
		
		updateEnabling();
	}
	
	/*----- Getters -----*/
	
	public int getAllocatedRowCount(){
		if(contents == null) return 0;
		return contents.length;
	}
	
	public String getTextCellContents(int row, int col){
		if(contents == null) return null;
		if(col < 0 || col >= colTypes.length) return null;
		if(row < 0 || row >= contents.length) return null;
		if(colTypes[col] != COLTYPE_STRING) return null;
		
		Row rr = contents[row];
		if(rr == null || rr.cells == null) return null;
		if(rr.cells[col] instanceof JLabel){
			JLabel lbl = (JLabel)rr.cells[col];
			return lbl.getText();
		}
		
		return null;
	}
	
	public boolean getCheckboxCellContents(int row, int col){
		if(contents == null) return false;
		if(col < 0 || col >= colTypes.length) return false;
		if(row < 0 || row >= contents.length) return false;
		if(colTypes[col] != COLTYPE_CHECKBOX) return false;
		
		Row rr = contents[row];
		if(rr == null || rr.cells == null) return false;
		if(rr.cells[col] instanceof JCheckBox){
			JCheckBox cb = (JCheckBox)rr.cells[col];
			return cb.isSelected();
		}
		
		return false;
	}
	
	public boolean rowEnabled(int row){
		if(contents == null) return false;
		if(row < 0 || row >= contents.length) return false;
		
		Row rr = contents[row];
		if(rr == null) return false;
		return rr.rowEnabled;
	}
	
	public boolean cellEnabled(int row, int col) {
		if(contents == null) return false;
		if(col < 0 || col >= colTypes.length) return false;
		if(row < 0 || row >= contents.length) return false;
		if(colTypes[col] != COLTYPE_CHECKBOX) return false;
		
		Row rr = contents[row];
		if(rr == null || rr.cells == null) return false;
		if(rr.cells[col] instanceof JCheckBox){
			//JCheckBox cb = (JCheckBox)rr.cells[col];
			//return cb.isEnabled();
			return rr.cellEnabled[col];
		}
		
		return false;
	}
	
	/*----- Setters -----*/
	
	public void setColumnName(int index, String name){
		if(index < 0 || index >= colNames.length) return;
		colNames[index] = name;
		
		hdrLabels[index].setText(name);
		hdrLabels[index].repaint();
		
		//Column checkbox tooltips
		if(colTypes[index] == COLTYPE_CHECKBOX){
			if(hdrCheckboxes[index] != null){
				hdrCheckboxes[index].setToolTipText(name);
				hdrCheckboxes[index].repaint();
			}
			
			if(contents != null){
				for(int r = 0; r < contents.length; r++){
					if(contents[r] != null){
						contents[r].updateColumnName(name, index);
					}
				}
			}
		}
		
	}
	
	public void setColumnCheckboxCallback(int index, CheckCallback func){
		if(index < 0 || index >= colNames.length) return;
		cbCallbacks[index] = func;
	}
	
	public void setTextCellContents(int row, int col, String value){
		if(contents == null) return;
		if(col < 0 || col >= colTypes.length) return;
		if(row < 0 || row >= contents.length) return;
		if(colTypes[col] != COLTYPE_STRING) return;
		
		Row rr = contents[row];
		if(rr == null || rr.cells == null) return;
		if(rr.cells[col] instanceof JLabel){
			JLabel lbl = (JLabel)rr.cells[col];
			lbl.setText(value);
			lbl.repaint();
		}
	}
	
	public void setCheckboxCellContents(int row, int col, boolean value){
		if(contents == null) return;
		if(col < 0 || col >= colTypes.length) return;
		if(row < 0 || row >= contents.length) return;
		if(colTypes[col] != COLTYPE_CHECKBOX) return;
		
		Row rr = contents[row];
		if(rr == null || rr.cells == null) return;
		if(rr.cells[col] instanceof JCheckBox){
			JCheckBox cb = (JCheckBox)rr.cells[col];
			cb.setSelected(value);
			cb.repaint();
		}
	}
	
	/*----- Row Management -----*/
	
	public void allocateRows(int rows){
		//Erases all existing rows
		pnlTable.removeAll();
		if(rows > 0){
			contents = new Row[rows];
			for(int i = 0; i < rows; i++){
				contents[i] = new Row();
				contents[i].rowIndex = i;
			}
			
			tableLayout.rowHeights = new int[rows + 1];
			tableLayout.rowWeights = defaultWeightsArray(rows);
			
			for(int i = 0; i < rows; i++){
				contents[i].addToPanel(pnlTable);
			}
		}
		else{
			//No rows
			contents = null;
		}
		
		repaintAll();
	}
	
	public void setRowEnabled(int row, boolean b){
		if(contents == null) return;
		if(row < 0 || row >= contents.length) return;
		
		contents[row].rowEnabled = b;
		contents[row].updateEnabling();
	}
	
	/*----- Column Management -----*/
	
	public void setColumnEnabled(int col, boolean b){
		if(colTypes == null) return;
		if(col < 0 || col >= colTypes.length);
		colEnabled[col] = b;
		updateEnabling();
	}
	
	/*----- GUI Update -----*/
	
	private double[] defaultWeightsArray(int size){
		if(size < 0) return null;
		double[] darr = new double[size+1];
		Arrays.fill(darr, 1.0);
		darr[size] = Double.MIN_VALUE;
		return darr;
	}
	
	public void setCellEnabled(int row, int col, boolean b){
		if(contents == null) return;
		if(row < 0 || row >= contents.length) return;
		if(col < 0 || col >= colTypes.length);
		
		Row rr = contents[row];
		if(rr == null) return;
		rr.cellEnabled[col] = b;
		rr.updateEnabling();
	}
	
	public void updateEnabling(){
		int colCount = colTypes.length;
		
		//Header part...
		for(int i = 0; i < colCount; i++){
			if(hdrCheckboxes[i] != null){
				hdrCheckboxes[i].setEnabled(colEnabled[i]);
				hdrCheckboxes[i].repaint();
			}
		}
		
		//Rows...
		if(contents == null) return;
		int rowCount = contents.length;
		for(int r = 0; r < rowCount; r++){
			Row rr = contents[r];
			if(rr != null) rr.updateEnabling();
		}
	}
	
	public void disableAll(){
		int colCount = colTypes.length;
		
		//Header part...
		for(int i = 0; i < colCount; i++){
			if(hdrCheckboxes[i] != null){
				hdrCheckboxes[i].setEnabled(false);
				hdrCheckboxes[i].repaint();
			}
		}
		
		//Rows...
		if(contents == null) return;
		int rowCount = contents.length;
		for(int r = 0; r < rowCount; r++){
			Row rr = contents[r];
			if(rr != null) rr.disableAllComp();
		}
	}
	
	public void repaintAll(){
		int colCount = colTypes.length;
		
		//Header part...
		for(int i = 0; i < colCount; i++){
			if(hdrCheckboxes[i] != null){
				hdrCheckboxes[i].repaint();
			}
			if(hdrLabels[i] != null){
				hdrLabels[i].repaint();
			}
		}
		
		//Rows...
		if(contents == null) return;
		int rowCount = contents.length;
		for(int r = 0; r < rowCount; r++){
			Row rr = contents[r];
			if(rr != null) rr.repaintAllComp();
		}
	}
	
	/*----- Callbacks -----*/
	
	private void onHeaderCheckboxSelect(int col){
		if(col < 0 || col >= colTypes.length) return;
		if(contents == null) return;
		
		JCheckBox cb = hdrCheckboxes[col];
		if(cb == null) return;
		
		boolean b = cb.isSelected();
		int rowCount = contents.length;
		for(int r = 0; r < rowCount; r++){
			Row rr = contents[r];
			if(rr == null) continue;
			if(rr.cells == null) continue;
			if(rr.cells[col] == null) continue;
			if(rr.cells[col] instanceof JCheckBox){
				cb = (JCheckBox)rr.cells[col];
				if(cb.isEnabled() && rr.cellEnabled[col]) {
					cb.setSelected(b);
					cb.repaint();
				}
			}
		}
	}

}
