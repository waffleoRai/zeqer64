package waffleoRai_zeqer64.GUI.filters;

import waffleoRai_Utils.VoidCallbackMethod;

import java.awt.GridBagLayout;
import javax.swing.JPanel;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

public class TextFilterPanel<T> extends FilterPanel<T> {

	private static final long serialVersionUID = 314914614571166913L;
	
	public static final int MIN_WIDTH = 270;
	public static final int MIN_HEIGHT = 65;
	
	public static final int MAX_FLAGS = 64;
	
	/*----- Instance Variables -----*/
	
	private String[] flag_names;
	private JCheckBox[] flag_boxes;
	
	private JPanel pnlFlags;
	private GridBagLayout pnlFlagsLayout;
	
	private int cb_per_row = 1;
	private int cb_rows = 0;
	private int pix_per_cbcol = 80;
	
	private JTextField txtSearch;
	private JButton btnSearch;
	
	private ZeqerFilter<T> search_filter;
	
	/*----- Init -----*/
	
	public TextFilterPanel(int flag_fields){
		if(flag_fields > 0){
			if(flag_fields > MAX_FLAGS) flag_fields = MAX_FLAGS;
			flag_names = new String[flag_fields];
			flag_boxes = new JCheckBox[flag_fields];
		}
		cb_per_row = MIN_WIDTH/pix_per_cbcol;
		action_callbacks = new LinkedList<VoidCallbackMethod>();
		initGUI();
	}
	
	private void initGUI(){
		int pref_height = preferredHeight(MIN_WIDTH);

		setMinimumSize(new Dimension(MIN_WIDTH, pref_height));
		setPreferredSize(new Dimension(MIN_WIDTH, pref_height));
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JPanel pnlText = new JPanel();
		GridBagConstraints gbc_pnlText = new GridBagConstraints();
		gbc_pnlText.insets = new Insets(0, 0, 5, 0);
		gbc_pnlText.fill = GridBagConstraints.BOTH;
		gbc_pnlText.gridx = 0;
		gbc_pnlText.gridy = 1;
		add(pnlText, gbc_pnlText);
		GridBagLayout gbl_pnlText = new GridBagLayout();
		gbl_pnlText.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlText.rowHeights = new int[]{0, 0};
		gbl_pnlText.columnWeights = new double[]{1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlText.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlText.setLayout(gbl_pnlText);
		
		txtSearch = new JTextField();
		GridBagConstraints gbc_txtSearch = new GridBagConstraints();
		gbc_txtSearch.insets = new Insets(5, 5, 5, 5);
		gbc_txtSearch.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtSearch.gridx = 0;
		gbc_txtSearch.gridy = 0;
		pnlText.add(txtSearch, gbc_txtSearch);
		txtSearch.setColumns(10);
		
		btnSearch = new JButton("Search");
		GridBagConstraints gbc_btnSearch = new GridBagConstraints();
		gbc_btnSearch.insets = new Insets(5, 5, 5, 0);
		gbc_btnSearch.gridx = 1;
		gbc_btnSearch.gridy = 0;
		pnlText.add(btnSearch, gbc_btnSearch);
		btnSearch.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				triggerRefilterCallbacks();
			}
		});
		
		JButton btnClear = new JButton("Clear");
		GridBagConstraints gbc_btnClear = new GridBagConstraints();
		gbc_btnClear.insets = new Insets(5, 5, 5, 5);
		gbc_btnClear.gridx = 2;
		gbc_btnClear.gridy = 0;
		pnlText.add(btnClear, gbc_btnClear);
		btnClear.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onButton_Clear();
			}
		});
		
		pnlFlags = new JPanel();
		GridBagConstraints gbc_pnlFlags = new GridBagConstraints();
		gbc_pnlFlags.insets = new Insets(0, 0, 5, 0);
		gbc_pnlFlags.fill = GridBagConstraints.BOTH;
		gbc_pnlFlags.gridx = 0;
		gbc_pnlFlags.gridy = 2;
		add(pnlFlags, gbc_pnlFlags);
		pnlFlagsLayout = new GridBagLayout();
		pnlFlagsLayout.columnWidths = new int[]{0};
		pnlFlagsLayout.rowHeights = new int[]{0};
		pnlFlagsLayout.columnWeights = new double[]{Double.MIN_VALUE};
		pnlFlagsLayout.rowWeights = new double[]{Double.MIN_VALUE};
		pnlFlags.setLayout(pnlFlagsLayout);
		
		initCheckboxes();
	}
	
	private void initCheckboxes(){
		if(flag_names == null) return;
		for(int i = 0; i < flag_names.length; i++){
			flag_names[i] = "flag_" + i;
			flag_boxes[i] = new JCheckBox(flag_names[i]);
			flag_boxes[i].addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					triggerRefilterCallbacks();
				}
			});
		}
		updateCheckboxes(MIN_WIDTH);
	}
	
	/*----- Getters -----*/
	
	public boolean itemPassesFilters(T item){
		if(search_filter == null) return true;
		long flag_field = 0L;
		if(flag_boxes != null){
			long mask = 1L;
			for(int i = 0; i < flag_boxes.length; i++){
				if(flag_boxes[i].isSelected()){
					flag_field |= mask;
				}
				mask <<= 1;
			}
		}
		return search_filter.itemPasses(item, txtSearch.getText(), flag_field);
	}
	
	public String getText(){return txtSearch.getText();}
	
	public boolean flagSet(int index){
		if(flag_boxes == null) return false;
		if(index < 0 || index >= flag_boxes.length) return false;
		return flag_boxes[index].isSelected();
	}
	
	/*----- Setters -----*/
	
	public void setPixelsPerCheckboxColumn(int pix){
		if(pix >= 10) pix_per_cbcol = pix;
	}

	public void setSearchFilter(ZeqerFilter<T> filter){
		search_filter = filter;
	}
	
	public void clearFilterSelections(){
		txtSearch.setText("");
		txtSearch.repaint();
	}
	
	/*----- Draw -----*/
	
	private void updateFlagPanelLayout(){
		pnlFlags.removeAll();
		pnlFlagsLayout.columnWidths = new int[cb_per_row];
		pnlFlagsLayout.rowHeights = new int[cb_rows];
		pnlFlagsLayout.columnWeights = new double[cb_per_row];
		pnlFlagsLayout.rowWeights = new double[cb_rows];
		
		for(int i = 0; i < cb_per_row; i++){
			pnlFlagsLayout.columnWidths[i] = 0;
			pnlFlagsLayout.columnWeights[i] = Double.MIN_VALUE;
		}
		
		for(int i = 0; i < cb_rows; i++){
			pnlFlagsLayout.rowHeights[i] = 0;
			pnlFlagsLayout.rowWeights[i] = Double.MIN_VALUE;
		}
	}
	
	private void updateCheckboxPosition(JCheckBox cb, int row, int col){
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = col;
		gbc.gridy = row;
		pnlFlags.add(cb,gbc);
	}
	
	private void updateCheckboxes(int pnlWidth){
		if(flag_names == null) return;
		preferredHeight(pnlWidth); //Updates the sizes...
		
		updateFlagPanelLayout();
		int i = 0;
		for(int r = 0; r < cb_rows; r++){
			for(int l = 0; l < cb_per_row; l++){
				if(i >= flag_names.length) break;
				updateCheckboxPosition(flag_boxes[i++], r, l);
			}
		}
	}
	
	private int preferredHeight(int width){
		int pref_height = MIN_HEIGHT;
		if(flag_names != null){
			//Rectangle cb = this.getGraphics().getClipBounds();
			cb_per_row = width/pix_per_cbcol;
			cb_rows = flag_names.length/cb_per_row;
			if(flag_names.length % cb_per_row != 0) cb_rows++;
			pref_height += 15 *(cb_rows);
		}
		return pref_height;
	}
	
	public void paintComponent(Graphics g){
		//Makes sure to rearrange checkboxes before update.
		if(flag_names != null){
			Rectangle cb = g.getClipBounds();
			updateCheckboxes(cb.width);
		}
		
		super.paintComponent(g);
	}
	
	public void disableAll(){
		txtSearch.setEnabled(false);
		btnSearch.setEnabled(false);
		if(flag_boxes != null){
			for(JCheckBox cb : flag_boxes){
				cb.setEnabled(false);
			}
		}
	}
	
	public void enableAll(){
		txtSearch.setEnabled(true);
		btnSearch.setEnabled(true);
		if(flag_boxes != null){
			for(JCheckBox cb : flag_boxes){
				cb.setEnabled(true);
			}
		}
	}
	
	/*----- Actions -----*/
	
	private void onButton_Clear(){
		txtSearch.setText("");
		txtSearch.repaint();
		triggerRefilterCallbacks();
	}
	
}
