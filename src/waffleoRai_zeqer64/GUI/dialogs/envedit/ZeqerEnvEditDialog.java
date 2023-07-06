package waffleoRai_zeqer64.GUI.dialogs.envedit;

import javax.swing.JDialog;

import java.awt.GridBagLayout;
import javax.swing.JPanel;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import waffleoRai_GUITools.ComponentGroup;
import waffleoRai_GUITools.GUITools;
import waffleoRai_Sound.nintendo.Z64Sound;
import waffleoRai_soundbank.nintendo.z64.Z64Envelope;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

//Dialog for editing envelopes
public class ZeqerEnvEditDialog extends JDialog{
	
	private static final long serialVersionUID = 3730417777811683896L;

	public static final int DEFO_WIDTH = 425;
	public static final int DEFO_HEIGHT = 340;
	
	/*----- Inner Classes -----*/
	
	private static class EnvEventNode{
		public short command;
		public short parameter;
		
		public EnvEventNode next;
		public EnvEventNode prev;
		
		public boolean flag = false;
		
		public String toString(){
			switch(command){
			case Z64Sound.ENVCMD__ADSR_DISABLE:
				return "DISABLE";
			case Z64Sound.ENVCMD__ADSR_HANG:
				return "HANG";
			case Z64Sound.ENVCMD__ADSR_RESTART:
				return "RESTART";
			case Z64Sound.ENVCMD__ADSR_GOTO:
				//TODO Resolve links properly?
				return "GOTO [0x" + String.format("%04x", parameter) + "]";
			default:
				return "DELTA " + command + " -> " + String.format("0x%04x", parameter);
			}
		}
	}
	
	private static class PresetNode{
		public Z64Envelope data;
		public String name;
		
		public String toString(){
			if(name != null) return name;
			return super.toString();
		}
	}
	
	/*----- Instance Variables -----*/
	
	private Frame parent;
	
	//For enabling
	private ComponentGroup globalEnable;
	private JButton btnUp;
	private JButton btnDown;
	private JButton btnDelete;
	
	private JComboBox<PresetNode> cmbxPreset;
	private JList<EnvEventNode> lstEvents;
	
	//Backing
	private List<PresetNode> storedPresets;
	private EnvEventNode storedEventsHead;
	
	private Z64Envelope out_env = null; //Rendered on okay/exit
	
	private ZeqerCoreInterface core;
	
	/*----- Init -----*/
	
	public ZeqerEnvEditDialog(Frame parent_frame, ZeqerCoreInterface corelink){
		super(parent_frame, true);
		core = corelink;
		parent = parent_frame;
		globalEnable = new ComponentGroup();
		initGUI();
		loadPresetsFromCore();
	}
	
	private void initGUI(){
		setMinimumSize(new Dimension(DEFO_WIDTH, DEFO_HEIGHT));
		setPreferredSize(new Dimension(DEFO_WIDTH, DEFO_HEIGHT));
		
		setTitle("Edit Envelope");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel pnlPresets = new JPanel();
		GridBagConstraints gbc_pnlPresets = new GridBagConstraints();
		gbc_pnlPresets.gridwidth = 2;
		gbc_pnlPresets.insets = new Insets(5, 5, 5, 5);
		gbc_pnlPresets.fill = GridBagConstraints.BOTH;
		gbc_pnlPresets.gridx = 0;
		gbc_pnlPresets.gridy = 0;
		getContentPane().add(pnlPresets, gbc_pnlPresets);
		GridBagLayout gbl_pnlPresets = new GridBagLayout();
		gbl_pnlPresets.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_pnlPresets.rowHeights = new int[]{0, 0};
		gbl_pnlPresets.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlPresets.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlPresets.setLayout(gbl_pnlPresets);
		
		JLabel lblPreset = new JLabel("Preset:");
		GridBagConstraints gbc_lblPreset = new GridBagConstraints();
		gbc_lblPreset.insets = new Insets(0, 5, 0, 5);
		gbc_lblPreset.anchor = GridBagConstraints.EAST;
		gbc_lblPreset.gridx = 0;
		gbc_lblPreset.gridy = 0;
		pnlPresets.add(lblPreset, gbc_lblPreset);
		
		cmbxPreset = new JComboBox<PresetNode>();
		GridBagConstraints gbc_cmbxPreset = new GridBagConstraints();
		gbc_cmbxPreset.insets = new Insets(0, 0, 0, 5);
		gbc_cmbxPreset.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbxPreset.gridx = 1;
		gbc_cmbxPreset.gridy = 0;
		pnlPresets.add(cmbxPreset, gbc_cmbxPreset);
		cmbxPreset.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {cmbxSelectCallback();}});
		globalEnable.addComponent("cmbxPreset", cmbxPreset);
		
		JButton btnSavePreset = new JButton("Save Preset...");
		GridBagConstraints gbc_btnSavePreset = new GridBagConstraints();
		gbc_btnSavePreset.gridx = 3;
		gbc_btnSavePreset.gridy = 0;
		pnlPresets.add(btnSavePreset, gbc_btnSavePreset);
		globalEnable.addComponent("btnSavePreset", btnSavePreset);
		btnSavePreset.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {btnSavePresetCallback();}});
		
		JScrollPane spList = new JScrollPane();
		spList.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spList = new GridBagConstraints();
		gbc_spList.insets = new Insets(5, 5, 5, 5);
		gbc_spList.fill = GridBagConstraints.BOTH;
		gbc_spList.gridx = 0;
		gbc_spList.gridy = 1;
		getContentPane().add(spList, gbc_spList);
		globalEnable.addComponent("spList", spList);
		
		lstEvents = new JList<EnvEventNode>();
		spList.setViewportView(lstEvents);
		globalEnable.addComponent("lstEvents", lstEvents);
		lstEvents.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e) {
				listItemSelectionCallback();
			}});
		
		JPanel pnlListCtrl = new JPanel();
		GridBagConstraints gbc_pnlListCtrl = new GridBagConstraints();
		gbc_pnlListCtrl.insets = new Insets(0, 5, 5, 5);
		gbc_pnlListCtrl.fill = GridBagConstraints.BOTH;
		gbc_pnlListCtrl.gridx = 1;
		gbc_pnlListCtrl.gridy = 1;
		getContentPane().add(pnlListCtrl, gbc_pnlListCtrl);
		GridBagLayout gbl_pnlListCtrl = new GridBagLayout();
		gbl_pnlListCtrl.columnWidths = new int[]{0, 0, 0};
		gbl_pnlListCtrl.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_pnlListCtrl.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_pnlListCtrl.rowWeights = new double[]{1.0, 0.0, 0.0, 20.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		pnlListCtrl.setLayout(gbl_pnlListCtrl);
		
		btnUp = new JButton("Move Up");
		GridBagConstraints gbc_btnUp = new GridBagConstraints();
		gbc_btnUp.fill = GridBagConstraints.BOTH;
		gbc_btnUp.insets = new Insets(0, 0, 5, 5);
		gbc_btnUp.gridx = 0;
		gbc_btnUp.gridy = 1;
		pnlListCtrl.add(btnUp, gbc_btnUp);
		btnUp.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {btnUpCallback();}});
		globalEnable.addComponent("btnUp", btnUp);
		
		btnDown = new JButton("Move Down");
		GridBagConstraints gbc_btnDown = new GridBagConstraints();
		gbc_btnDown.fill = GridBagConstraints.BOTH;
		gbc_btnDown.insets = new Insets(0, 0, 5, 5);
		gbc_btnDown.gridx = 0;
		gbc_btnDown.gridy = 2;
		pnlListCtrl.add(btnDown, gbc_btnDown);
		btnDown.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {btnDownCallback();}});
		globalEnable.addComponent("btnDown", btnDown);
		
		JButton btnInsert = new JButton("Insert Event");
		GridBagConstraints gbc_btnInsert = new GridBagConstraints();
		gbc_btnInsert.fill = GridBagConstraints.BOTH;
		gbc_btnInsert.insets = new Insets(0, 0, 5, 5);
		gbc_btnInsert.gridx = 0;
		gbc_btnInsert.gridy = 4;
		pnlListCtrl.add(btnInsert, gbc_btnInsert);
		btnInsert.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {btnInsertCallback();}});
		globalEnable.addComponent("btnInsert", btnInsert);
		
		btnDelete = new JButton("Delete Event");
		GridBagConstraints gbc_btnDelete = new GridBagConstraints();
		gbc_btnDelete.insets = new Insets(0, 0, 5, 5);
		gbc_btnDelete.fill = GridBagConstraints.BOTH;
		gbc_btnDelete.gridx = 0;
		gbc_btnDelete.gridy = 5;
		pnlListCtrl.add(btnDelete, gbc_btnDelete);
		btnDelete.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {btnDeleteCallback();}});
		globalEnable.addComponent("btnDelete", btnDelete);
		
		JPanel pnlOkay = new JPanel();
		GridBagConstraints gbc_pnlOkay = new GridBagConstraints();
		gbc_pnlOkay.insets = new Insets(5, 5, 5, 5);
		gbc_pnlOkay.gridwidth = 2;
		gbc_pnlOkay.fill = GridBagConstraints.BOTH;
		gbc_pnlOkay.gridx = 0;
		gbc_pnlOkay.gridy = 2;
		getContentPane().add(pnlOkay, gbc_pnlOkay);
		GridBagLayout gbl_pnlOkay = new GridBagLayout();
		gbl_pnlOkay.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_pnlOkay.rowHeights = new int[]{0, 0};
		gbl_pnlOkay.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlOkay.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlOkay.setLayout(gbl_pnlOkay);
		
		JButton btnSimple = new JButton("From AHDS...");
		GridBagConstraints gbc_btnSimple = new GridBagConstraints();
		gbc_btnSimple.insets = new Insets(0, 5, 0, 5);
		gbc_btnSimple.gridx = 0;
		gbc_btnSimple.gridy = 0;
		pnlOkay.add(btnSimple, gbc_btnSimple);
		btnSimple.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {btnSimpleEnvCallback();}});
		globalEnable.addComponent("btnSimple", btnSimple);
		
		JButton btnOkay = new JButton("Okay");
		GridBagConstraints gbc_btnOkay = new GridBagConstraints();
		gbc_btnOkay.insets = new Insets(0, 0, 0, 5);
		gbc_btnOkay.gridx = 2;
		gbc_btnOkay.gridy = 0;
		pnlOkay.add(btnOkay, gbc_btnOkay);
		btnOkay.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {btnOkayCallback();}});
		globalEnable.addComponent("btnOkay", btnOkay);
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.gridx = 3;
		gbc_btnCancel.gridy = 0;
		pnlOkay.add(btnCancel, gbc_btnCancel);
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {btnCancelCallback();}});
		globalEnable.addComponent("btnCancel", btnCancel);
	}
	
	private void loadPresetsFromCore(){
		if(core != null){
			addPresets(core.getAllEnvelopePresets());
		}
	}
	
	/*----- Getters -----*/
	
	public Z64Envelope getOutputEnvelope(){return out_env;}
	
	public Map<String, Z64Envelope> getStoredPresets(){
		Map<String, Z64Envelope> pmap = new HashMap<String, Z64Envelope>();
		for(PresetNode pnode : storedPresets){
			pmap.put(pnode.name, pnode.data);
		}
		return pmap;
	}
	
	/*----- Setters -----*/
	
	public void addPresets(Map<String, Z64Envelope> presets){
		if(storedPresets == null){
			storedPresets = new LinkedList<PresetNode>();
		}
		List<String> keys = new ArrayList<String>(presets.size()+1);
		keys.addAll(presets.keySet());
		Collections.sort(keys);
		for(String k : keys){
			PresetNode node = new PresetNode();
			node.data = presets.get(k);
			node.name = k;
			storedPresets.add(node);
		}
		updatePresetCmbx();
	}
	
	public void loadEnvelope(Z64Envelope env){
		storedEventsHead = null;
		if(env != null) {
			List<short[]> eventlist = env.getEvents();
			EnvEventNode prev = null;
			for(short[] e : eventlist){
				EnvEventNode enode = new EnvEventNode();
				enode.command = e[0];
				enode.parameter = e[1];
				if(prev != null) prev.next = enode;
				else storedEventsHead = enode;
				enode.prev = prev;
				prev = enode;
			}
		}
		updateEventList();
	}
	
	/*----- GUI Management -----*/
	
	private void updatePresetCmbx(){
		DefaultComboBoxModel<PresetNode> model = new DefaultComboBoxModel<PresetNode>();
		if(storedPresets != null){
			for(PresetNode pnode : storedPresets){
				model.addElement(pnode);
			}
			cmbxPreset.setEnabled(!storedPresets.isEmpty());
		}
		else cmbxPreset.setEnabled(false);
		cmbxPreset.setModel(model);
		cmbxPreset.repaint();
	}
	
	private void updateEventList(){
		DefaultListModel<EnvEventNode> model = new DefaultListModel<EnvEventNode>();
		if(storedEventsHead != null){
			EnvEventNode node = storedEventsHead;
			while(node != null){
				model.addElement(node);
				node.flag = false;
				node = node.next;
			}
		}
		lstEvents.setModel(model);
		lstEvents.repaint();
	}
	
	private void disableGlobal(){
		globalEnable.setEnabling(false);
		globalEnable.repaint();
	}
	
	private void enableGlobal(){
		globalEnable.setEnabling(true);
		if(storedPresets == null || storedPresets.isEmpty()) cmbxPreset.setEnabled(false);
		if(lstEvents.isSelectionEmpty()){
			btnUp.setEnabled(false);
			btnDown.setEnabled(false);
			btnDelete.setEnabled(false);
		}
		globalEnable.repaint();
	}
	
	public void setWait(){
		disableGlobal();
		super.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	public void unsetWait(){
		enableGlobal();
		super.setCursor(null);
	}
	
	public void closeMe(){
		this.setVisible(false);
		this.dispose();
	}
	
	public void showMe(Component c){
		if(c != null) setLocationRelativeTo(c);
		else{
			if(parent != null) setLocationRelativeTo(parent);
			else{
				setLocation(GUITools.getScreenCenteringCoordinates(this));
			}
		}
		
		pack();
		setVisible(true);
	}
	
	/*----- Envelope -----*/
	
	private boolean renderEnvelope(){
		//If empty, leave null.
		if(storedEventsHead == null){
			out_env = null;
			return false;
		}
		
		//Copy events
		//Check for terminator, add HANG if none
		out_env = new Z64Envelope();
		EnvEventNode enode = storedEventsHead;
		EnvEventNode last = null;
		while(enode != null){
			out_env.addEvent(enode.command, enode.parameter);
			last = enode;
			enode = enode.next;
		}
		
		if(!out_env.hasTerminal()){
			enode = new EnvEventNode();
			enode.command = (short)Z64Sound.ENVCMD__ADSR_HANG;
			enode.parameter = 0;
			last.next = enode;
			enode.prev = last;
			updateEventList();
			
			out_env.addEvent(enode.command, enode.parameter);
		}
		
		return true;
	}
	
	/*----- Callbacks -----*/
	
	private void listItemSelectionCallback(){
		//Set enabling of buttons for list ordering
		btnUp.setEnabled(!lstEvents.isSelectionEmpty());
		btnDown.setEnabled(!lstEvents.isSelectionEmpty());
		btnDelete.setEnabled(!lstEvents.isSelectionEmpty());
		btnUp.repaint();
		btnDown.repaint();
		btnDelete.repaint();
	}
	
	private void btnOkayCallback(){
		if(!renderEnvelope()){
			JOptionPane.showMessageDialog(this, "Envelope render failed!", "Envelope Invalid", JOptionPane.ERROR_MESSAGE);
			return;
		}
		closeMe();
	}
	
	private void btnCancelCallback(){
		out_env = null;
		closeMe();
	}
	
	private void btnUpCallback(){
		List<EnvEventNode> selected = lstEvents.getSelectedValuesList();
		if(selected == null || selected.isEmpty()) return;
		
		for(EnvEventNode snode : selected){
			EnvEventNode pnode = snode.prev;
			if(pnode != null){
				//Swap if not selected
				if(!pnode.flag){
					pnode.next = snode.next;
					snode.next = pnode;
					snode.prev = pnode.prev;
					pnode.prev = snode;
					if(snode.prev == null) storedEventsHead = snode;	
				}
			}
			else{
				//Presumably already head
				storedEventsHead = snode;
			}
			snode.flag = true;
		}
		
		updateEventList();
	}
	
	private void btnDownCallback(){
		List<EnvEventNode> selected = lstEvents.getSelectedValuesList();
		if(selected == null || selected.isEmpty()) return;
		Collections.reverse(selected);
		
		for(EnvEventNode snode : selected){
			EnvEventNode nnode = snode.next;
			if(nnode != null){
				//Swap.
				//If swapped out of head position, make partner the head
				if(!nnode.flag){
					if(snode.prev == null) storedEventsHead = nnode;
					snode.next = nnode.next;
					nnode.prev = snode.prev;
					snode.prev = nnode;
					nnode.next = snode;
				}
			}
			//If null, then already tail.
		}
		
		updateEventList();
	}
	
	private void btnInsertCallback(){
		//Inserts after the last selection
		//Event dialog
		setWait();
		EnvEventEditDialog dialog = new EnvEventEditDialog(parent);
		dialog.showMe(this);
		
		short[] e = dialog.getEvent();
		if(e == null){
			//Was a cancel. Do nothing.
			unsetWait();
			return;
		}
		
		//Look for last.
		EnvEventNode pnode = null;
		List<EnvEventNode> selected = lstEvents.getSelectedValuesList();
		if(selected != null && !selected.isEmpty()){
			pnode = selected.get(selected.size()-1);
		}
		else{
			//If none selected, just add to end
			if(storedEventsHead != null){
				EnvEventNode node = storedEventsHead;
				while(node != null) {
					pnode = node;
					node = node.next;
				}
			}
		}
		
		EnvEventNode newnode = new EnvEventNode();
		newnode.command = e[0];
		newnode.parameter = e[1];
		newnode.prev = pnode;
		if(pnode != null){
			newnode.next = pnode.next;
			if(newnode.next != null) newnode.next.prev = newnode;
			pnode.next = newnode;
		}
		else{
			//Should only occur if no events so far.
			storedEventsHead = newnode;
		}
		
		updateEventList();
		unsetWait();
	}
	
	private void btnDeleteCallback(){
		List<EnvEventNode> selected = lstEvents.getSelectedValuesList();
		if(selected == null || selected.isEmpty()) return;
		Collections.reverse(selected);
		
		for(EnvEventNode snode : selected){
			if(snode.prev != null){
				snode.prev.next = snode.next;
			}
			else{
				storedEventsHead = snode.next;
			}
			
			if(snode.next != null){
				snode.next.prev = snode.prev;
			}
		}
		
		updateEventList();
	}
	
	private void btnSimpleEnvCallback(){
		setWait();
		EnvEditAHDSSimpleDialog dialog = new EnvEditAHDSSimpleDialog(parent);
		dialog.showMe(this);
		
		Z64Envelope env = dialog.getEnvelope();
		if(env != null){
			loadEnvelope(env);
		}
		unsetWait();
	}
	
	private void btnSavePresetCallback(){
		setWait();
		
		//See if envelope is valid... If empty, no point.
		if(storedEventsHead == null){
			JOptionPane.showMessageDialog(this, 
					"Cannot save an empty envelope as a preset!", 
					"Preset Empty", JOptionPane.ERROR_MESSAGE);
			unsetWait();
			return;
		}
		
		
		boolean namevalid = false;
		String pname = null;
		while(!namevalid){
			namevalid = true;
			pname = JOptionPane.showInputDialog(this, "Enter preset name:", 
					"Save Preset", JOptionPane.PLAIN_MESSAGE);
			
			if(pname != null && !pname.isEmpty()){
				for(PresetNode pnode : storedPresets){
					if(pname.equals(pnode.name)){
						JOptionPane.showMessageDialog(this, 
								"This name is already in use for another preset.\n"
								+ "Please choose a different name.", "Name In Use", JOptionPane.ERROR_MESSAGE);
						namevalid = false;
						break;
					}
				}
				if(namevalid){
					this.renderEnvelope();
					PresetNode newnode = new PresetNode();
					newnode.data = out_env;
					if(core != null){
						core.addEnvelopePreset(pname, out_env);
					}
					out_env = null;
					newnode.name = pname;
					storedPresets.add(newnode);
					updatePresetCmbx();
				}
			}
			else{
				//Taken as a cancel
				unsetWait();
				return;
			}
		}
		
		unsetWait();
	}
	
	private void cmbxSelectCallback(){
		//Have a confirmation dialog first
		//It's annoying, but better than accidentally erasing ur shit
		
		int seli = cmbxPreset.getSelectedIndex();
		if(seli < 0) return;
		
		setWait();
		PresetNode sel = cmbxPreset.getItemAt(seli);
		if(sel != null){
			int answer = JOptionPane.showConfirmDialog(this, 
					"Do you want to load the \"" + sel.name + "\" preset?", 
					"Load Preset", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(answer == JOptionPane.YES_OPTION){
				loadEnvelope(sel.data);
			}
		}
		unsetWait();
	}
	
}
