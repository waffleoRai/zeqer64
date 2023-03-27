package waffleoRai_zeqer64.GUI.dialogs;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JDialog;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import javax.swing.JScrollPane;
import javax.swing.ListModel;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.border.BevelBorder;

import waffleoRai_GUITools.ComponentGroup;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JOptionPane;

//Little dialog for managing tags attached to a seq, inst, sample etc.
public class ZeqerTagEditDialog extends JDialog{

	private static final long serialVersionUID = 977196948862671577L;
	
	public static final int DEFO_WIDTH = 370;
	public static final int DEFO_HEIGHT = 280;
	
	/*----- Instance Variables -----*/
	
	private ComponentGroup globalEnable;
	
	private boolean exitSelection = false;
	private JList<String> lstTags;
	
	/*----- Init -----*/
	
	public ZeqerTagEditDialog(Frame parent){
		super(parent, true);
		globalEnable = new ComponentGroup();
		initGUI();
	}
	
	private void initGUI(){
		setMinimumSize(new Dimension(DEFO_WIDTH, DEFO_HEIGHT));
		setPreferredSize(new Dimension(DEFO_WIDTH, DEFO_HEIGHT));
		
		setTitle("Edit Tags");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JScrollPane spTags = new JScrollPane();
		spTags.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spTags = new GridBagConstraints();
		gbc_spTags.insets = new Insets(10, 10, 10, 10);
		gbc_spTags.fill = GridBagConstraints.BOTH;
		gbc_spTags.gridx = 0;
		gbc_spTags.gridy = 0;
		getContentPane().add(spTags, gbc_spTags);
		globalEnable.addComponent("spTags", spTags);
		
		lstTags = new JList<String>();
		spTags.setViewportView(lstTags);
		globalEnable.addComponent("lstTags", lstTags);
		
		JPanel pnlButtons = new JPanel();
		GridBagConstraints gbc_pnlButtons = new GridBagConstraints();
		gbc_pnlButtons.fill = GridBagConstraints.BOTH;
		gbc_pnlButtons.gridx = 0;
		gbc_pnlButtons.gridy = 1;
		getContentPane().add(pnlButtons, gbc_pnlButtons);
		GridBagLayout gbl_pnlButtons = new GridBagLayout();
		gbl_pnlButtons.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_pnlButtons.rowHeights = new int[]{0, 0};
		gbl_pnlButtons.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlButtons.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlButtons.setLayout(gbl_pnlButtons);
		
		JButton btnAdd = new JButton("Add...");
		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.insets = new Insets(5, 5, 5, 5);
		gbc_btnAdd.gridx = 0;
		gbc_btnAdd.gridy = 0;
		pnlButtons.add(btnAdd, gbc_btnAdd);
		globalEnable.addComponent("btnAdd", btnAdd);
		btnAdd.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {btnAddCallback();}
		});
		
		JButton btnRemove = new JButton("Remove");
		GridBagConstraints gbc_btnRemove = new GridBagConstraints();
		gbc_btnRemove.insets = new Insets(5, 5, 5, 5);
		gbc_btnRemove.gridx = 1;
		gbc_btnRemove.gridy = 0;
		pnlButtons.add(btnRemove, gbc_btnRemove);
		globalEnable.addComponent("btnRemove", btnRemove);
		btnRemove.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {btnRemoveCallback();}
		});
		
		JButton btnSave = new JButton("Save");
		GridBagConstraints gbc_btnSave = new GridBagConstraints();
		gbc_btnSave.insets = new Insets(5, 5, 5, 5);
		gbc_btnSave.gridx = 3;
		gbc_btnSave.gridy = 0;
		pnlButtons.add(btnSave, gbc_btnSave);
		globalEnable.addComponent("btnSave", btnSave);
		btnSave.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {btnSaveCallback();}
		});
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(5, 5, 5, 5);
		gbc_btnCancel.gridx = 4;
		gbc_btnCancel.gridy = 0;
		pnlButtons.add(btnCancel, gbc_btnCancel);
		globalEnable.addComponent("btnCancel", btnCancel);
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {btnCancelCallback();}
		});
	}
	
	/*----- Getters -----*/
	
	public boolean getExitSelection(){return exitSelection;}
	
	public List<String> getTags(){
		String[] tags_arr = getAllTags();
		if(tags_arr == null) return new LinkedList<String>();
		
		List<String> tags = new ArrayList<String>(tags_arr.length);
		for(int i = 0; i < tags_arr.length; i++) tags.add(tags_arr[i]);
		
		return tags;
	}
	
	private String[] getAllTags(){
		ListModel<String> mdl = lstTags.getModel();
		int count = mdl.getSize();
		if(count > 0){
			String[] items = new String[count];
			for(int i = 0; i < count; i++){
				items[i] = mdl.getElementAt(i);
			}
		}
		return null;
	}
	
	/*----- Setters -----*/
	
	public void loadTags(List<String> tags){
		globalEnable.setEnabling(false);
		globalEnable.repaint();
		
		DefaultListModel<String> model = new DefaultListModel<String>();
		for(String tag : tags) model.addElement(tag);
		
		lstTags.setModel(model);
		globalEnable.setEnabling(true);
		globalEnable.repaint();
	}
	
	/*----- GUI Management -----*/

	public void closeMe(){
		setVisible(false);
		dispose();
	}
	
	/*----- Callbacks -----*/
	
	private void btnSaveCallback(){
		exitSelection = true;
		closeMe();
	}
	
	private void btnCancelCallback(){
		exitSelection = false;
		closeMe();
	}
	
	private void btnAddCallback(){
		String val = JOptionPane.showInputDialog(this, 
				"Enter new tag string:", "Add Tag", JOptionPane.QUESTION_MESSAGE);
		if(val == null) return;
		if(val.isEmpty()) return;
		
		globalEnable.setEnabling(false);
		globalEnable.repaint();
		
		String[] alltags = getAllTags();
		DefaultListModel<String> model = new DefaultListModel<String>();
		for(int i = 0; i < alltags.length; i++){
			model.addElement(alltags[i]);
		}
		model.addElement(val);
		
		lstTags.setModel(model);
		globalEnable.setEnabling(true);
		globalEnable.repaint();
		
	}
	
	private void btnRemoveCallback(){
		if(lstTags.isSelectionEmpty()) return;
		globalEnable.setEnabling(false);
		globalEnable.repaint();
		
		String[] alltags = getAllTags();
		int[] selidxs = lstTags.getSelectedIndices();
		DefaultListModel<String> model = new DefaultListModel<String>();
		for(int i = 0; i < alltags.length; i++){
			boolean keep = true;
			for(int j = 0; j < selidxs.length; j++){
				if(selidxs[j] == i){
					keep = false;
					break;
				}
			}
			if(keep) model.addElement(alltags[i]);
		}
		
		lstTags.setModel(model);
		globalEnable.setEnabling(true);
		globalEnable.repaint();
	}
	
}
