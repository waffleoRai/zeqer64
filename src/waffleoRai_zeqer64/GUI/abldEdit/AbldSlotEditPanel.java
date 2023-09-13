package waffleoRai_zeqer64.GUI.abldEdit;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.WRPanel;
import waffleoRai_zeqer64.GUI.ScrollableCompListPanel;

public abstract class AbldSlotEditPanel<T> extends WRPanel {
	
	private static final long serialVersionUID = -7007771127648593588L;
	
	/*----- Instance Variables -----*/
	
	protected ArrayList<T> refList;
	protected ArrayList<T> myList;
	
	protected boolean isInjection = false;
	protected boolean readOnly = false;

	protected ComponentGroup cgRef;
	protected ComponentGroup cgMine;
	protected ComponentGroup cgRefEditable;
	protected ComponentGroup cgMineEditable;
	protected ComponentGroup cgRefList;
	protected ComponentGroup cgMyList;
	
	protected JLabel lblRefSlots;
	protected JLabel lblMySlots;
	
	protected ScrollableCompListPanel pnlListRef;
	protected ScrollableCompListPanel pnlListMine;
	
	/*----- Init -----*/

	protected AbldSlotEditPanel(){
		
		cgRef = globalEnable.newChild();
		cgMine = globalEnable.newChild();
		cgRefEditable = cgRef.newChild();
		cgMineEditable = cgMine.newChild();
		cgRefList = cgRef.newChild();
		cgMyList = cgMine.newChild();
		
		initGUI();
	}
	
	private void initGUI(){
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JPanel pnlRef = new JPanel();
		GridBagConstraints gbc_pnlRef = new GridBagConstraints();
		gbc_pnlRef.insets = new Insets(0, 0, 0, 5);
		gbc_pnlRef.fill = GridBagConstraints.BOTH;
		gbc_pnlRef.gridx = 0;
		gbc_pnlRef.gridy = 0;
		add(pnlRef, gbc_pnlRef);
		GridBagLayout gbl_pnlRef = new GridBagLayout();
		gbl_pnlRef.columnWidths = new int[]{0, 0};
		gbl_pnlRef.rowHeights = new int[]{0, 0, 0};
		gbl_pnlRef.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlRef.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		pnlRef.setLayout(gbl_pnlRef);
		
		JPanel pnlRefBtns = new JPanel();
		GridBagConstraints gbc_pnlRefBtns = new GridBagConstraints();
		gbc_pnlRefBtns.insets = new Insets(5, 5, 5, 5);
		gbc_pnlRefBtns.fill = GridBagConstraints.BOTH;
		gbc_pnlRefBtns.gridx = 0;
		gbc_pnlRefBtns.gridy = 0;
		pnlRef.add(pnlRefBtns, gbc_pnlRefBtns);
		GridBagLayout gbl_pnlRefBtns = new GridBagLayout();
		gbl_pnlRefBtns.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_pnlRefBtns.rowHeights = new int[]{0, 0};
		gbl_pnlRefBtns.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlRefBtns.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlRefBtns.setLayout(gbl_pnlRefBtns);
		
		JLabel lblSlots_1 = new JLabel("Slots:");
		GridBagConstraints gbc_lblSlots_1 = new GridBagConstraints();
		gbc_lblSlots_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblSlots_1.gridx = 0;
		gbc_lblSlots_1.gridy = 0;
		pnlRefBtns.add(lblSlots_1, gbc_lblSlots_1);
		
		lblRefSlots = new JLabel("0");
		GridBagConstraints gbc_lblRefSlots = new GridBagConstraints();
		gbc_lblRefSlots.insets = new Insets(0, 0, 0, 5);
		gbc_lblRefSlots.gridx = 1;
		gbc_lblRefSlots.gridy = 0;
		pnlRefBtns.add(lblRefSlots, gbc_lblRefSlots);
		
		/*JButton btnSet = new JButton("Set");
		GridBagConstraints gbc_btnSet = new GridBagConstraints();
		gbc_btnSet.insets = new Insets(0, 0, 0, 5);
		gbc_btnSet.gridx = 3;
		gbc_btnSet.gridy = 0;
		pnlRefBtns.add(btnSet, gbc_btnSet);
		cgRefEditable.addComponent("btnSet", btnSet);
		btnSet.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnRefSetCallback();
			}
		});*/
		
		/*JButton btnClear = new JButton("Clear");
		GridBagConstraints gbc_btnClear = new GridBagConstraints();
		gbc_btnClear.gridx = 4;
		gbc_btnClear.gridy = 0;
		pnlRefBtns.add(btnClear, gbc_btnClear);
		cgRefEditable.addComponent("btnClear", btnClear);
		btnClear.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnRefClearCallback();
			}
		});*/
		
		JScrollPane spRef = new JScrollPane();
		spRef.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spRef = new GridBagConstraints();
		gbc_spRef.insets = new Insets(5, 5, 5, 5);
		gbc_spRef.fill = GridBagConstraints.BOTH;
		gbc_spRef.gridx = 0;
		gbc_spRef.gridy = 1;
		pnlRef.add(spRef, gbc_spRef);
		cgRef.addComponent("spRef", spRef);
		
		pnlListRef = new ScrollableCompListPanel(spRef);
		spRef.setViewportView(pnlListRef);
		
		JSeparator separator = new JSeparator();
		separator.setOrientation(SwingConstants.VERTICAL);
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.fill = GridBagConstraints.VERTICAL;
		gbc_separator.insets = new Insets(0, 0, 0, 5);
		gbc_separator.gridx = 1;
		gbc_separator.gridy = 0;
		add(separator, gbc_separator);
		
		JPanel pnlEdit = new JPanel();
		GridBagConstraints gbc_pnlEdit = new GridBagConstraints();
		gbc_pnlEdit.fill = GridBagConstraints.BOTH;
		gbc_pnlEdit.gridx = 2;
		gbc_pnlEdit.gridy = 0;
		add(pnlEdit, gbc_pnlEdit);
		GridBagLayout gbl_pnlEdit = new GridBagLayout();
		gbl_pnlEdit.columnWidths = new int[]{0, 0};
		gbl_pnlEdit.rowHeights = new int[]{0, 0, 0};
		gbl_pnlEdit.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlEdit.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		pnlEdit.setLayout(gbl_pnlEdit);
		
		JPanel pnlMyBtns = new JPanel();
		GridBagConstraints gbc_pnlMyBtns = new GridBagConstraints();
		gbc_pnlMyBtns.insets = new Insets(5, 0, 5, 5);
		gbc_pnlMyBtns.fill = GridBagConstraints.BOTH;
		gbc_pnlMyBtns.gridx = 0;
		gbc_pnlMyBtns.gridy = 0;
		pnlEdit.add(pnlMyBtns, gbc_pnlMyBtns);
		GridBagLayout gbl_pnlMyBtns = new GridBagLayout();
		gbl_pnlMyBtns.columnWidths = new int[]{0, 0, 0, 0, 0, 0};
		gbl_pnlMyBtns.rowHeights = new int[]{0, 0};
		gbl_pnlMyBtns.columnWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlMyBtns.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlMyBtns.setLayout(gbl_pnlMyBtns);
		
		JLabel lblSlots = new JLabel("Slots:");
		GridBagConstraints gbc_lblSlots = new GridBagConstraints();
		gbc_lblSlots.insets = new Insets(0, 5, 0, 5);
		gbc_lblSlots.gridx = 0;
		gbc_lblSlots.gridy = 0;
		pnlMyBtns.add(lblSlots, gbc_lblSlots);
		
		lblMySlots = new JLabel("0");
		GridBagConstraints gbc_lblMySlots = new GridBagConstraints();
		gbc_lblMySlots.insets = new Insets(0, 0, 0, 5);
		gbc_lblMySlots.gridx = 1;
		gbc_lblMySlots.gridy = 0;
		pnlMyBtns.add(lblMySlots, gbc_lblMySlots);
		
		JButton btnPlus = new JButton("+");
		GridBagConstraints gbc_btnPlus = new GridBagConstraints();
		gbc_btnPlus.insets = new Insets(0, 0, 0, 5);
		gbc_btnPlus.gridx = 3;
		gbc_btnPlus.gridy = 0;
		pnlMyBtns.add(btnPlus, gbc_btnPlus);
		cgMineEditable.addComponent("btnPlus", btnPlus);
		btnPlus.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnPlusCallback();
			}
		});
		
		JButton btnMinus = new JButton("-");
		GridBagConstraints gbc_btnMinus = new GridBagConstraints();
		gbc_btnMinus.gridx = 4;
		gbc_btnMinus.gridy = 0;
		pnlMyBtns.add(btnMinus, gbc_btnMinus);
		cgMineEditable.addComponent("btnMinus", btnMinus);
		btnMinus.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				btnMinusCallback();
			}
		});
		
		JScrollPane spMyBuild = new JScrollPane();
		spMyBuild.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spMyBuild = new GridBagConstraints();
		gbc_spMyBuild.insets = new Insets(5, 5, 5, 5);
		gbc_spMyBuild.fill = GridBagConstraints.BOTH;
		gbc_spMyBuild.gridx = 0;
		gbc_spMyBuild.gridy = 1;
		pnlEdit.add(spMyBuild, gbc_spMyBuild);
		cgMine.addComponent("spMyBuild", spMyBuild);
		
		pnlListMine = new ScrollableCompListPanel(spMyBuild);
		spMyBuild.setViewportView(pnlListMine);
	}
	
	/*----- Getters -----*/
	
	public abstract boolean isRefLoaded();
	
	/*----- Setters -----*/
	
	public void setRefList(List<T> list){
		if(refList != null){
			refList.clear();
		}
		
		if(list != null){
			int size = list.size();
			if(refList == null){
				refList = new ArrayList<T>(size+1);
			}
			else{
				refList.ensureCapacity(size+1);
			}
			refList.addAll(list);
		}
		else{
			refList = null;
		}
		
		//Update Panel...
		cgRefList.clear();
		if(refList != null && !refList.isEmpty()){
			Component[] carr = new Component[refList.size()];
			
			int i = 0;
			for(T node : refList){
				carr[i] = getRefListable(node);
				if(carr[i] != null) cgRefList.addComponent("item_" + Integer.toString(i), carr[i]);
				i++;
			}
			
			pnlListRef.setContents(carr);
			lblRefSlots.setText(Integer.toString(carr.length));
		}
		else{
			lblRefSlots.setText("0");
			pnlListRef.setContents(null);
		}
		
		lblRefSlots.repaint();
		cgRefList.repaint();
		pnlListRef.repaint();
	}
	
	public void setMyList(List<T> list){
		if(myList != null){
			myList.clear();
		}
		
		if(list != null){
			int size = list.size();
			if(myList == null){
				myList = new ArrayList<T>(size+1);
			}
			else{
				myList.ensureCapacity(size+1);
			}
			myList.addAll(list);
		}
		else{
			myList = null;
		}
		
		//Update Panel...
		cgMyList.clear();
		if(myList != null && !myList.isEmpty()){
			Component[] carr = new Component[myList.size()];
			
			int i = 0;
			for(T node : myList){
				carr[i] = getMyListable(node);
				if(carr[i] != null) cgMyList.addComponent("item_" + Integer.toString(i), carr[i]);
				i++;
			}
			
			pnlListMine.setContents(carr);
			lblMySlots.setText(Integer.toString(carr.length));
		}
		else{
			lblMySlots.setText("0");
			pnlListMine.setContents(null);
		}
		
		lblMySlots.repaint();
		cgMyList.repaint();
		pnlListMine.repaint();
	}
	
	/*----- Internal -----*/
	
	protected abstract Component getRefListable(T listItem);
	protected abstract Component getMyListable(T listItem);
	protected abstract T newItem(int index);
	
	/*----- Draw -----*/
	
	public void reenable(){
		globalEnable.setEnabling(true);
		if(isInjection){
			cgRefEditable.setEnabling(false);
		}
		else{
			cgRefEditable.setEnabling(isRefLoaded() && !readOnly);
		}
		
		if(readOnly){
			cgMineEditable.setEnabling(false);
		}
		globalEnable.repaint();
	}
	
	/*----- Callbacks -----*/
	
	//protected abstract void btnRefSetCallback();
	//protected abstract void btnRefClearCallback();
	protected void btnPlusCallback(){
		if(readOnly || isInjection){
			return;
		}
		
		int cap = pnlListMine.getRowCapacity();
		int size = pnlListMine.getRowsUsed();
		if(cap <= size){
			//Reallocate with 4 more
			pnlListMine.reallocateRows(size + 4);
		}
		
		//Add.
		if(myList == null) myList = new ArrayList<T>(16);
		T item = newItem(myList.size());
		myList.add(item);
		
		Component c = getMyListable(item);
		pnlListMine.appendContents(new Component[]{c});
	}
	
	protected void btnMinusCallback(){
		if(readOnly || isInjection){
			return;
		}
		
		if(myList != null && !myList.isEmpty()){
			myList.remove(myList.size()-1);
		}
		pnlListMine.removeComponentsFromEnd(1);
	}

}
