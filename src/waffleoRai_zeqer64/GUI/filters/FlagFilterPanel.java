package waffleoRai_zeqer64.GUI.filters;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import waffleoRai_Utils.VoidCallbackMethod;

public class FlagFilterPanel<T> extends FilterPanel<T>{

	private static final long serialVersionUID = -5843999963372418255L;
	
	public static final int MIN_WIDTH = 270;
	public static final int MIN_HEIGHT = 60;
	
	public static final int MIN_COL_WIDTH = 40;

	/*----- Inner Classes -----*/
	
	private static class FlagNode<T>{
		public LabelSwitch label;
		public ZeqerFilter<T> filter;
	}
	
	/*----- Instance Variables -----*/
	
	private String group_name;
	
	private JLabel lblOr;
	private JLabel lblAnd;
	private boolean and_mode = false;
	
	private Font fnt_andor_on;
	private Font fnt_andor_off;
	private Color clr_andor_on = Color.black;
	private Color clr_andor_off = Color.black;
	
	private Color clr_sw_on = Color.green;
	private Color clr_sw_off = Color.black;
	private Color clr_sw_not = Color.red;
	
	private JPanel pnlFlags;
	private GridBagLayout layoutFlags;
	private int flag_cols = 1;
	private int flag_rows = 1;
	private int flag_col_width = MIN_COL_WIDTH;
	
	private List<FlagNode<T>> flags;
	private boolean flags_dirty = false;

	/*----- Init -----*/
	
 	public FlagFilterPanel(){
 		this("");
	}
 	
 	public FlagFilterPanel(String name){
 		action_callbacks = new LinkedList<VoidCallbackMethod>();
		initGUI();
	}
	
	private void initGUI(){
		fnt_andor_on = new Font("Tahoma", Font.BOLD, 12);
		fnt_andor_off = new Font("Tahoma", Font.PLAIN, 12);
		
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JPanel pnlOp = new JPanel();
		GridBagConstraints gbc_pnlOp = new GridBagConstraints();
		gbc_pnlOp.insets = new Insets(0, 0, 5, 0);
		gbc_pnlOp.fill = GridBagConstraints.BOTH;
		gbc_pnlOp.gridx = 0;
		gbc_pnlOp.gridy = 0;
		add(pnlOp, gbc_pnlOp);
		GridBagLayout gbl_pnlOp = new GridBagLayout();
		gbl_pnlOp.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_pnlOp.rowHeights = new int[]{0, 0};
		gbl_pnlOp.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlOp.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlOp.setLayout(gbl_pnlOp);
		
		lblAnd = new JLabel("AND");
		lblAnd.setFont(fnt_andor_off);
		GridBagConstraints gbc_lblAnd = new GridBagConstraints();
		gbc_lblAnd.insets = new Insets(5, 5, 5, 5);
		gbc_lblAnd.gridx = 2;
		gbc_lblAnd.gridy = 0;
		pnlOp.add(lblAnd, gbc_lblAnd);
		lblAnd.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				onClickAND();
			}
		});
		
		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.fill = GridBagConstraints.VERTICAL;
		gbc_separator.insets = new Insets(0, 0, 0, 5);
		gbc_separator.gridx = 3;
		gbc_separator.gridy = 0;
		pnlOp.add(separator, gbc_separator);
		
		lblOr = new JLabel("OR");
		lblOr.setFont(fnt_andor_on);
		GridBagConstraints gbc_lblOr = new GridBagConstraints();
		gbc_lblOr.insets = new Insets(5, 5, 5, 15);
		gbc_lblOr.gridx = 4;
		gbc_lblOr.gridy = 0;
		pnlOp.add(lblOr, gbc_lblOr);
		lblOr.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				onClickOR();
			}
		});
		
		pnlFlags = new JPanel();
		GridBagConstraints gbc_pnlTags = new GridBagConstraints();
		gbc_pnlTags.fill = GridBagConstraints.BOTH;
		gbc_pnlTags.gridx = 0;
		gbc_pnlTags.gridy = 1;
		add(pnlFlags, gbc_pnlTags);
		layoutFlags = new GridBagLayout();
		layoutFlags.columnWidths = new int[]{0};
		layoutFlags.rowHeights = new int[]{0};
		layoutFlags.columnWeights = new double[]{Double.MIN_VALUE};
		layoutFlags.rowWeights = new double[]{Double.MIN_VALUE};
		pnlFlags.setLayout(layoutFlags);
	}
	
	/*----- Getters -----*/
	
	public boolean itemPassesFilters(T item) {
		boolean pass = false;
		for(FlagNode<T> node : flags){
			if(node.label.getSwitchState() == LabelSwitch.SWITCHSTATE_OFF) continue;
			pass = node.filter.itemPasses(item);
			if(node.label.getSwitchState() == LabelSwitch.SWITCHSTATE_ON_NOT){
				pass = !pass;
			}
			if(pass){
				if(!and_mode) return true;
			}
			else{
				if(and_mode) return false;
			}
		}
		
		return true;
	}
	
	public String getName(){return group_name;}
	
	/*----- Setters -----*/
	
	public void setTextColor_AndOrSwitchOn(Color c){
		if(c == null) return;
		clr_andor_on = c;
		updateANDORSwitches();
	}
	
	public void setTextColor_AndOrSwitchOff(Color c){
		if(c == null) return;
		clr_andor_off = c;
		updateANDORSwitches();
	}
	
	public void setTextColor_SwitchesOn(Color c){
		if(c == null) return;
		clr_sw_on = c;
		updateLabelsInPlace();
	}
	
	public void setTextColor_SwitchesOff(Color c){
		if(c == null) return;
		clr_sw_off = c;
		updateLabelsInPlace();
	}
	
	public void setTextColor_SwitchesNot(Color c){
		if(c == null) return;
		clr_sw_not = c;
		updateLabelsInPlace();
	}
	
	public void addSwitch(ZeqerFilter<T> condition, String text, boolean enable_not){
		FlagNode<T> node = new FlagNode<T>();
		node.filter = condition;
		int stype = LabelSwitch.TYPE_TWO_PHASE;
		if(enable_not) stype = LabelSwitch.TYPE_THREE_PHASE;
		node.label = new LabelSwitch(text, stype);
		flags.add(node);
		
		node.label.addCallback(new VoidCallbackMethod(){
			public void doMethod() {
				triggerRefilterCallbacks();
			}
		});
		
		flags_dirty = true;
		repaint();
	}
	
	public void clearSwitches(){
		flags.clear();
		flags_dirty = true;
		repaint();
	}
	
	/*----- Draw -----*/
	
	private void updateLabelsInPlace(){
		for(FlagNode<T> node : flags){
			node.label.setOnColor(clr_sw_on);
			node.label.setOffColor(clr_sw_off);
			node.label.setNotColor(clr_sw_not);
			node.label.repaint();
		}
	}
	
	private void updateANDORSwitches(){
		if(and_mode){
			lblOr.setFont(fnt_andor_off);
			lblOr.setForeground(clr_andor_off);
			lblAnd.setFont(fnt_andor_on);
			lblAnd.setForeground(clr_andor_on);
		}
		else{
			lblOr.setFont(fnt_andor_on);
			lblOr.setForeground(clr_andor_on);
			lblAnd.setFont(fnt_andor_off);
			lblAnd.setForeground(clr_andor_off);
		}
		lblOr.repaint();
		lblAnd.repaint();
	}
	
	private void addToFlagGrid(LabelSwitch lbl, int row, int col){
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(2, 2, 2, 2);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = col;
		gbc.gridy = row;
		pnlFlags.add(lbl, gbc);
	}
	
	private void updateFlagsLayout(){
		layoutFlags.columnWidths = new int[flag_cols];
		layoutFlags.rowHeights = new int[flag_rows];
		layoutFlags.columnWeights = new double[flag_cols];
		layoutFlags.rowWeights = new double[flag_rows];
		
		for(int i = 0; i < flag_cols; i++) layoutFlags.columnWeights[i] = Double.MIN_VALUE;
		for(int i = 0; i < flag_rows; i++) layoutFlags.rowWeights[i] = Double.MIN_VALUE;
	}
	
	private boolean recalculateFlagsGrid(int comp_width){
		//Returns whether or not need to remove and rearrange labels
		//First, determine rows and columns needed...
		boolean ret = flags_dirty;
		flags_dirty = false;
		if(flags.isEmpty()){
			flag_rows = 1;
			flag_cols = 1;
			return ret;
		}
		
		//First determine col width...
		flag_col_width = MIN_COL_WIDTH;
		for(FlagNode<T> node : flags){
			//TODO I do not know if this method will work for this purpose.
			int w = node.label.getWidth();
			if(w > flag_col_width){
				flag_col_width = w;
			}
		}
		
		int flag_count = flags.size();
		int newcols = comp_width/flag_col_width;
		if(newcols < 1) newcols = 1;
		int newrows = flag_count/newcols;
		if(flag_count % newcols != 0) newrows++;
		if(newrows < 1) newrows = 1;
		
		ret = ret || (flag_cols != newcols);
		ret = ret || (flag_rows != newrows);
		
		flag_cols = newcols;
		flag_rows = newrows;
		
		return ret;
	}
	
	private void updateFlagLabels(int comp_width){
		if(recalculateFlagsGrid(comp_width)){
			pnlFlags.removeAll();
			updateFlagsLayout();
			int r = 0;
			int l = 0;
			for(FlagNode<T> node : flags){
				addToFlagGrid(node.label, r, l);
				if(++l >= flag_cols){
					l = 0;
					r++;
				}
			}
		}
	}
	
	public void paintComponent(Graphics g){
		if(!flags.isEmpty()){
			Rectangle cb = g.getClipBounds();
			updateFlagLabels(cb.width);
		}
		
		super.paintComponent(g);
	}
	
	public void disableAll(){
		setEnabled(false);
		lblOr.setEnabled(false);
		lblAnd.setEnabled(false);
		for(FlagNode<T> node : flags) node.label.setEnabled(false);
	}
	
	public void enableAll(){
		setEnabled(true);
		lblOr.setEnabled(true);
		lblAnd.setEnabled(true);
		for(FlagNode<T> node : flags) node.label.setEnabled(true);
	}
	
	/*----- Actions -----*/
	
	private void onClickAND(){
		if(!this.isEnabled()) return;
		and_mode = true;
		updateANDORSwitches();
	}
	
	private void onClickOR(){
		if(!this.isEnabled()) return;
		and_mode = false;
		updateANDORSwitches();
	}
	
}
