package waffleoRai_zeqer64.GUI;

import javax.swing.JOptionPane;

import waffleoRai_Utils.VoidCallbackMethod;
import waffleoRai_zeqer64.GUI.filters.FilterPanel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class FilterListPanel<T> extends FilterPanel<T>{

	private static final long serialVersionUID = 8733538999687623796L;
	
	/*----- Instance Variables -----*/
	
	private GridBagLayout layout;
	
	private List<FilterPanel<T>> filters;
	
	/*----- Init -----*/
	
	public FilterListPanel() {
		filters = new LinkedList<FilterPanel<T>>();
		initGUI();
	}
	
	private void initGUI(){
		layout = new GridBagLayout();
		layout.columnWidths = new int[]{0};
		layout.rowHeights = new int[]{0};
		layout.columnWeights = new double[]{Double.MIN_VALUE};
		layout.rowWeights = new double[]{Double.MIN_VALUE};
		setLayout(layout);
	}
	
	/*----- Getters -----*/
	
	public boolean itemPassesFilters(T item){
		for(FilterPanel<T> filter : filters){
			if(!filter.itemPassesFilters(item)) return false;
		}
		return true;
	}
	
	public List<T> filterItems(Collection<T> items){
		List<T> list1 = new LinkedList<T>();
		List<T> list2 = new LinkedList<T>();
		boolean uselist = false; //False = 1->2
		list1.addAll(items);
		
		List<T> inlist = list1;
		List<T> outlist = list2;
		for(FilterPanel<T> filter : filters){
			for(T item : inlist){
				if(filter.itemPassesFilters(item)){
					outlist.add(item);
				}
			}
			inlist.clear();
			uselist = !uselist;
			if(!uselist){
				inlist = list1;
				outlist = list2;
			}
			else{
				inlist = list2;
				outlist = list1;
			}
		}
		
		return outlist;
	}
	
	/*----- Setters -----*/
	
	public void addPanel(FilterPanel<T> pnl){
		filters.add(pnl);
		pnl.addRefilterCallback(new VoidCallbackMethod(){
			public void doMethod() {
				//The FilterListPanel is a FilterPanel, so this calls the
				//	FilterListPanel's own callbacks
				triggerRefilterCallbacks();
			}
		});
		updateGrid();
	}
	
	public void clearPanels(){
		filters.clear();
		updateGrid();
	}

	
	/*----- Drawing -----*/
	
	private void updateFlagsLayout(){
		int rows = filters.size();
		layout.rowHeights = new int[rows];
		layout.rowWeights = new double[rows];
		
		for(int i = 0; i < rows; i++) layout.rowWeights[i] = Double.MIN_VALUE;
	}
	
	private void updateGrid(){
		removeAll();
		updateFlagsLayout();
		int r = 0;
		for(FilterPanel<T> pnl : filters){
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.BOTH;
			gbc.gridx = 0;
			gbc.gridy = r;
			gbc.gridheight = 1;
			gbc.gridwidth = 1;
			gbc.insets = new Insets(2,2,2,2);
			add(pnl,gbc);
			r++;
		}
		repaint();
	}
	
	public void disableAll(){
		for(FilterPanel<T> pnl : filters) pnl.disableAll();
	}
	
	public void enableAll(){
		for(FilterPanel<T> pnl : filters) pnl.enableAll();
	}
	
	/*----- Actions -----*/
	
	/*----- Text Boxes -----*/
	
	public void showWarning(String text){
		JOptionPane.showMessageDialog(this, text, "Warning", JOptionPane.WARNING_MESSAGE);
	}
	
	public void showError(String text){
		JOptionPane.showMessageDialog(this, text, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	public void showInfo(String text){
		JOptionPane.showMessageDialog(this, text, "Notice", JOptionPane.INFORMATION_MESSAGE);
	}

}
