package waffleoRai_zeqer64.GUI;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class ScrollableCompListPanel extends JPanel{

	/*----- Constants -----*/
	
	private static final long serialVersionUID = 4128932399117713236L;
	
	private static final int WIDTH_ADD = 20;
	private static final int HEIGHT_ADD = 10;
	private static final int COMP_PAD_VERT = 2;
	
	/*----- Instance Variables -----*/
	
	private JScrollPane parentScroller;
	private Component[] contents;
	private int contentsInUse = 0;
	
	private int ypad = COMP_PAD_VERT;
	private int padL = 0;
	private int padR = 0;
	
	/*----- Init -----*/

	public ScrollableCompListPanel(JScrollPane parent){
		parentScroller = parent;
		resetLayout(0);
	}
	
	/*----- Getters -----*/
	
	public int getRowSpacing(){return ypad;}
	public int getLeftPadding(){return padL;}
	public int getRightPadding(){return padR;}
	
	public int getRowCapacity(){return (contents != null) ? contents.length:0;}
	public int getRowsUsed(){return contentsInUse;}
	
	public JScrollPane getParentScrollPane(){return parentScroller;}
	public int getContentCount(){return (contents != null)?contents.length:0;}
	
	/*----- Setters -----*/
	
	private void updateSize(){
		int w = 0;
		int h = 0;
		
		if(contents != null){
			for(int i = 0; i < contents.length; i++){
				if(contents[i] != null){
					Dimension dim = contents[i].getPreferredSize();
					int cw = dim.width + padL + padR;
					if(cw > w) w = cw;
					h += dim.height;
				}
				if(i > 0 && i < (contents.length - 1)) h += ypad;
			}
		}
		
		this.setMinimumSize(new Dimension(w, h));
		this.setPreferredSize(new Dimension(w, h));
		if(parentScroller != null){
			parentScroller.setMinimumSize(new Dimension(w+WIDTH_ADD, h+HEIGHT_ADD));
			parentScroller.setPreferredSize(new Dimension(w+WIDTH_ADD, h+HEIGHT_ADD));	
		}
	}
	
	private void resetLayout(int rows){
		GridBagLayout gridBagLayout = new GridBagLayout();
		
		if(rows < 0) rows = 0;
		int alloc = rows + 1;
		
		//All zero
		gridBagLayout.columnWidths = new int[]{0,0}; //1 column
		gridBagLayout.rowHeights = new int[alloc];
		
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[alloc];
		
		//Keep these separate just in case we want something different for filled rows.
		gridBagLayout.rowWeights[alloc - 1] = Double.MIN_VALUE;
		for(int i = 0; i < rows; i++) gridBagLayout.rowWeights[i] = Double.MIN_VALUE;
		
		setLayout(gridBagLayout);
		contentsInUse = 0;
	}
	
	private void loadToRow(Component c, int row){
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.gridx = 0;
		gbc.gridy = row;
	
		gbc.insets = new Insets(0,padL,0,padR);
		if(row > 0){
			gbc.insets.top = ypad;
		}
		
		this.add(c, gbc);
		
		int amt = row+1;
		if(amt > contentsInUse) contentsInUse = amt;
	}
	
	public void reallocateRows(int rowCount){
		//Performs a reallocation and re-layout, but KEEPS current component instances.
		if(rowCount == 0){
			setContents(null);
			return;
		}
		
		if(contents != null){
			if(contents.length == rowCount) return;
			
			Component[] old = contents;
			contents = new Component[rowCount];
			for(int i = 0; i < old.length; i++){
				if(i >= rowCount) break;
				contents[i] = old[i];
			}
		}
		else{
			contents = new Component[rowCount];
		}
		
		removeAll();
		resetLayout(rowCount);
		for(int i = 0; i < contents.length; i++){
			if(contents[i] != null){
				loadToRow(contents[i], i);
			}
		}
		updateSize();
		repaint();
	}
	
	public void removeComponentsFromEnd(int count){
		if(contents == null) return;
		int newCount = contents.length - count;
		if(newCount < 0) newCount = 0;
		
		this.removeAll();
		contentsInUse = 0;
		if(newCount > 0){
			for(int i = 0; i < contents.length; i++) {
				if(i >= newCount) contents[i] = null;
				else{
					if(contents[i] != null) loadToRow(contents[i], i);
				}
			}
		}
		else{
			if(contents != null){
				for(int i = 0; i < contents.length; i++) contents[i] = null;
			}
		}
		
		updateSize();
		repaint();
	}
	
	public void appendContents(Component[] comp){
		if(comp == null) return;
		
		//Check if allocated enough
		int newlen = contentsInUse + comp.length;
		if(contents == null || contents.length < newlen){
			reallocateRows(newlen);
		}
		
		int oldlen = contentsInUse;
		for(int i = 0; i < comp.length; i++){
			int r = i+oldlen;
			contents[r] = comp[i];
			loadToRow(contents[r], r);
		}
		
		updateSize();
		repaint();
	}
	
	public void clearContents(){
		setContents(null);
	}
	
	public void setContents(Component[] compList){
		this.removeAll();
		if(compList != null){
			resetLayout(compList.length);
			
			//Copy.
			contents = new Component[compList.length];
			for(int i = 0; i < compList.length; i++){
				contents[i] = compList[i];
				loadToRow(contents[i], i);
			}
		}
		else{
			contents = null;
			resetLayout(0);
		}
		
		updateSize();
		repaint();
	}
	
	public void setRowSpacing(int pix){
		ypad = pix;
	}
	
	public void setLeftPadding(int pix){
		padL = (pix > 0)?pix:0;
	}
	
	public void setRightPadding(int pix){
		padR = (pix > 0)?pix:0;
	}
	
	/*----- Draw -----*/
	
	public void repaint(){
		super.repaint();
		if(parentScroller != null) parentScroller.repaint();
	}
	
}
