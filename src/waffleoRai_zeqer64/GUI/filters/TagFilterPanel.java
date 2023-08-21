package waffleoRai_zeqer64.GUI.filters;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import waffleoRai_Utils.VoidCallbackMethod;

import java.awt.Font;
import java.awt.Frame;

import javax.swing.JButton;

public class TagFilterPanel<T> extends FilterPanel<T>{

	private static final long serialVersionUID = 6237017886522922609L;
	
	public static final int MIN_WIDTH = 270;
	public static final int MIN_HEIGHT = 75;
	
	/*----- Inner Classes -----*/
	
	private static class TagNode<T>{
		public TagLabel tag;
		public ZeqerFilter<T> filter;
		
		public TagNode<T> prev;
		public TagNode<T> next;
	}
	
	/*----- Instance Variables -----*/
	
	private Frame parent;
	private ZeqerFilter<T> pool_filter;
	private Set<String> pool;
	
	private TagNode<T> head_tag = null;
	private TagNode<T> tail_tag = null;
	private boolean and_mode = false; //False = OR, true = AND
	private String title;
	
	private JPanel pnlTags;
	private JLabel lblOr;
	private JLabel lblAnd;
	
	private Font fnt_andor_on;
	private Font fnt_andor_off;
	
	private Color clr_andor_on = Color.black;
	private Color clr_andor_off = Color.black;
	
	private Color tag_std = Color.black;
	private Color tag_not = Color.red;
	private JPanel panel;
	private JButton btnAdd;
	
	/*----- Init -----*/
	
	public TagFilterPanel(Frame parent_frame){
		this(parent_frame, null);
	}
	
	public TagFilterPanel(Frame parent_frame, String title_text){
		title = title_text;
		parent = parent_frame;
		action_callbacks = new LinkedList<VoidCallbackMethod>();
		initGUI();
	}
	
	private void initGUI(){
		fnt_andor_on = new Font("Tahoma", Font.BOLD, 12);
		fnt_andor_off = new Font("Tahoma", Font.PLAIN, 12);
		
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 50, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
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
		
		if(title != null){
			JLabel lblttl = new JLabel(title);
			lblttl.setFont(new Font("Tahoma", Font.BOLD, 13));
			GridBagConstraints gbc_lblt = new GridBagConstraints();
			gbc_lblt.insets = new Insets(5, 5, 5, 5);
			gbc_lblt.gridx = 0;
			gbc_lblt.gridy = 0;
			pnlOp.add(lblttl, gbc_lblt);
		}
		
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
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_pnlTags = new GridBagConstraints();
		gbc_pnlTags.insets = new Insets(0, 0, 5, 0);
		gbc_pnlTags.fill = GridBagConstraints.BOTH;
		gbc_pnlTags.gridx = 0;
		gbc_pnlTags.gridy = 1;
		add(scrollPane, gbc_pnlTags);
		
		pnlTags = new JPanel();
		scrollPane.setViewportView(pnlTags);
		pnlTags.setBackground(Color.white);
		pnlTags.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
		
		panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 2;
		add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		btnAdd = new JButton("Add...");
		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.insets = new Insets(5, 5, 5, 5);
		gbc_btnAdd.gridx = 1;
		gbc_btnAdd.gridy = 0;
		panel.add(btnAdd, gbc_btnAdd);
		btnAdd.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				onButtonAdd();
			}
		});
		btnAdd.setEnabled(false);
	}
	
	/*----- Getters -----*/

	public boolean itemPassesFilters(T item){
		if(head_tag == null) return true;
		
		boolean pass = false;
		TagNode<T> node = head_tag;
		while(node != null){
			if(node.filter != null){
				pass = node.filter.itemPasses(item, node.tag.getText());
				if(node.tag.inNotMode()) pass = !pass;
				if(pass){
					//If it's in OR mode, one pass is enough.
					if(!and_mode) return true;
				}
				else{
					if(and_mode) return false;
				}
			}
			node = node.next;
		}
		if(!and_mode) return false;
		return true;
	}
	
	public Set<String> getAllTagStrings(){
		Set<String> set = new TreeSet<String>();
		TagNode<T> node = head_tag;
		while(node != null){
			set.add(node.tag.getText());
			node = node.next;
		}
		return set;
	}
	
	public String getTitle(){return this.title;}
	
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
	
	public void setTextColor_TagsNormal(Color c){
		if(c == null) return;
		tag_std = c;
		redrawTags();
	}
	
	public void setTextColor_TagsNotMode(Color c){
		if(c == null) return;
		tag_not = c;
		redrawTags();
	}
	
	public void addTag(ZeqerFilter<T> condition, String text){
		TagNode<T> tagnode = new TagNode<T>();
		tagnode.filter = condition;
		tagnode.tag = new TagLabel(text);
		if(head_tag != null){
			if(tail_tag != null){
				tagnode.prev = tail_tag;
				tail_tag.next = tagnode;
				tail_tag = tagnode;
			}
			else{
				//Find manually. This should not happen, but it is here just in case.
				tail_tag = head_tag;
				while(tail_tag.next != null){
					tail_tag = tail_tag.next;
				}
				tagnode.prev = tail_tag;
				tail_tag.next = tagnode;
				tail_tag = tagnode;
			}
		}
		else{
			head_tag = tagnode;
			tail_tag = tagnode;
		}
		
		//Add listener...
		tagnode.tag.addRightClickCallback(new VoidCallbackMethod(){
			public void doMethod() {
				onRightClickTag(tagnode);
			}
		});
		tagnode.tag.addLeftClickCallback(new VoidCallbackMethod(){
			public void doMethod() {
				triggerRefilterCallbacks();
			}
		});
		
		updateTags();
	}
	
	public void clearTags(){
		head_tag = null;
		tail_tag = null;
		updateTags();
	}
	
	public void addTagPool(Collection<String> tags, ZeqerFilter<T> condition){
		if(tags == null){
			pool.clear();
			btnAdd.setEnabled(false);
			return;
		}
		pool = new TreeSet<String>();
		pool.addAll(tags);
		pool_filter = condition;
		if(!tags.isEmpty()) btnAdd.setEnabled(true);
		else btnAdd.setEnabled(false);
		clearTags();
	}
	
	public void clearFilterSelections(){
		clearTags();
	}
	
	/*----- Draw -----*/
	
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
	
	private void updateTags(){
		//Readds them to the panel, then calls the redraw
		pnlTags.removeAll();
		TagNode<T> node = head_tag;
		while(node != null){
			pnlTags.add(node.tag);
			node = node.next;
		}
		pnlTags.validate();
		redrawTags();
		triggerRefilterCallbacks();
	}
	
	private void redrawTags(){
		//Just redraws them in place.
		TagNode<T> node = head_tag;
		while(node != null){
			node.tag.setBaseTextColor(tag_std);
			node.tag.setNotModeColor(tag_not);
			node.tag.repaint();
			node = node.next;
		}
		//pnlTags.repaint();
		//repaint();
	}
	
	public void disableAll(){
		btnAdd.setEnabled(false);
		lblOr.setEnabled(false);
		lblAnd.setEnabled(false);
		btnAdd.repaint();
		lblOr.repaint();
		lblAnd.repaint();
		
		TagNode<T> node = head_tag;
		while(node != null){
			node.tag.setEnabled(false);
			node.tag.repaint();
			node = node.next;
		}
	}
	
	public void enableAll(){
		btnAdd.setEnabled(pool != null && !pool.isEmpty());
		lblOr.setEnabled(true);
		lblAnd.setEnabled(true);
		btnAdd.repaint();
		lblOr.repaint();
		lblAnd.repaint();
		
		TagNode<T> node = head_tag;
		while(node != null){
			node.tag.setEnabled(true);
			node.tag.repaint();
			node = node.next;
		}
	}
	
	/*----- Actions -----*/
	
	private void onRightClickTag(TagNode<T> node){
		//Removes it.
		if(node.prev != null){
			node.prev.next = node.next;
		}
		if(node.next != null){
			node.next.prev = node.prev;
		}
		
		if(tail_tag == node) tail_tag = node.prev;
		if(head_tag == node) head_tag = node.next;
		
		updateTags();
		//triggerRefilterCallbacks();
	}
	
	private void onClickAND(){
		and_mode = true;
		updateANDORSwitches();
	}
	
	private void onClickOR(){
		and_mode = false;
		updateANDORSwitches();
	}

	private void onButtonAdd(){
		if(pool == null || pool.isEmpty()) return;
		TagPickDialog dialog = new TagPickDialog(parent, true);
		//Add tag pool...
		dialog.setTagPool(pool);
		Set<String> incl = getAllTagStrings();
		dialog.addIncluded(incl);
		
		dialog.pack();
		dialog.setVisible(true);
		
		if(dialog.getSelection() == TagPickDialog.SELECTION_OKAY){
			head_tag = null;
			tail_tag = null;
			Collection<String> incl_tags = dialog.getIncluded();
			for(String tag: incl_tags){
				addTag(pool_filter, tag);
			}
		}
		updateTags();
	}
	
}
