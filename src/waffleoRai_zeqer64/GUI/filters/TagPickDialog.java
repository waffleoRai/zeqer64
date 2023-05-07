package waffleoRai_zeqer64.GUI.filters;

import java.awt.Dimension;
import java.awt.Frame;
import java.util.Collection;
import java.util.Set;

import javax.swing.JDialog;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.border.BevelBorder;

import waffleoRai_GUITools.InExListPair;

public class TagPickDialog extends JDialog{

	private static final long serialVersionUID = -7601157932567692594L;
	
	public static final int MIN_WIDTH = 450;
	public static final int MIN_HEIGHT = 300;
	
	public static final int SELECTION_NONE = 0;
	public static final int SELECTION_CANCEL = 1;
	public static final int SELECTION_OKAY = 2;
	
	/*----- Instance Variables -----*/
	
	private Frame parent;
	private InExListPair<String> listPair;
	
	private int selection = SELECTION_NONE;
	
	/*----- Init -----*/
	
	public TagPickDialog(Frame parent_frame, boolean modal){
		super(parent_frame, modal);
		parent = parent_frame;
		setMinimumSize(new Dimension(MIN_WIDTH,MIN_HEIGHT));
		setPreferredSize(new Dimension(MIN_WIDTH,MIN_HEIGHT));
		setLocationRelativeTo(parent);
		initGUI();
	}
	
	private void initGUI(){
		setTitle("Select Tags");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel pnlLeft = new JPanel();
		GridBagConstraints gbc_pnlLeft = new GridBagConstraints();
		gbc_pnlLeft.insets = new Insets(0, 0, 5, 5);
		gbc_pnlLeft.fill = GridBagConstraints.BOTH;
		gbc_pnlLeft.gridx = 0;
		gbc_pnlLeft.gridy = 0;
		getContentPane().add(pnlLeft, gbc_pnlLeft);
		GridBagLayout gbl_pnlLeft = new GridBagLayout();
		gbl_pnlLeft.columnWidths = new int[]{0, 0};
		gbl_pnlLeft.rowHeights = new int[]{15, 0, 15, 0};
		gbl_pnlLeft.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlLeft.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		pnlLeft.setLayout(gbl_pnlLeft);
		
		JScrollPane spLeft = new JScrollPane();
		spLeft.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spLeft = new GridBagConstraints();
		gbc_spLeft.insets = new Insets(5, 5, 5, 5);
		gbc_spLeft.fill = GridBagConstraints.BOTH;
		gbc_spLeft.gridx = 0;
		gbc_spLeft.gridy = 1;
		pnlLeft.add(spLeft, gbc_spLeft);
		
		JList<String> lstLeft = new JList<String>();
		spLeft.setViewportView(lstLeft);
		
		JPanel pnlMid = new JPanel();
		GridBagConstraints gbc_pnlMid = new GridBagConstraints();
		gbc_pnlMid.insets = new Insets(0, 0, 5, 5);
		gbc_pnlMid.fill = GridBagConstraints.BOTH;
		gbc_pnlMid.gridx = 1;
		gbc_pnlMid.gridy = 0;
		getContentPane().add(pnlMid, gbc_pnlMid);
		GridBagLayout gbl_pnlMid = new GridBagLayout();
		gbl_pnlMid.columnWidths = new int[]{0, 0};
		gbl_pnlMid.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_pnlMid.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_pnlMid.rowWeights = new double[]{1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		pnlMid.setLayout(gbl_pnlMid);
		
		JButton btnArrowR = new JButton(">");
		GridBagConstraints gbc_btnArrowR = new GridBagConstraints();
		gbc_btnArrowR.insets = new Insets(0, 0, 5, 0);
		gbc_btnArrowR.gridx = 0;
		gbc_btnArrowR.gridy = 1;
		pnlMid.add(btnArrowR, gbc_btnArrowR);
		btnArrowR.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onButton_ArrowRight();
			}
		});
		
		JButton btnArrowL = new JButton("<");
		GridBagConstraints gbc_btnArrowL = new GridBagConstraints();
		gbc_btnArrowL.insets = new Insets(0, 0, 5, 0);
		gbc_btnArrowL.gridx = 0;
		gbc_btnArrowL.gridy = 2;
		pnlMid.add(btnArrowL, gbc_btnArrowL);
		btnArrowL.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onButton_ArrowLeft();
			}
		});
		
		JPanel pnlRight = new JPanel();
		GridBagConstraints gbc_pnlRight = new GridBagConstraints();
		gbc_pnlRight.insets = new Insets(0, 0, 5, 0);
		gbc_pnlRight.fill = GridBagConstraints.BOTH;
		gbc_pnlRight.gridx = 2;
		gbc_pnlRight.gridy = 0;
		getContentPane().add(pnlRight, gbc_pnlRight);
		GridBagLayout gbl_pnlRight = new GridBagLayout();
		gbl_pnlRight.columnWidths = new int[]{0, 0};
		gbl_pnlRight.rowHeights = new int[]{15, 0, 15, 0};
		gbl_pnlRight.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlRight.rowWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		pnlRight.setLayout(gbl_pnlRight);
		
		JScrollPane spRight = new JScrollPane();
		spRight.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spRight = new GridBagConstraints();
		gbc_spRight.insets = new Insets(5, 5, 5, 5);
		gbc_spRight.fill = GridBagConstraints.BOTH;
		gbc_spRight.gridx = 0;
		gbc_spRight.gridy = 1;
		pnlRight.add(spRight, gbc_spRight);
		
		JList<String> lstRight = new JList<String>();
		spRight.setViewportView(lstRight);
		
		JPanel pnlButtons = new JPanel();
		GridBagConstraints gbc_pnlButtons = new GridBagConstraints();
		gbc_pnlButtons.gridwidth = 3;
		gbc_pnlButtons.fill = GridBagConstraints.BOTH;
		gbc_pnlButtons.gridx = 0;
		gbc_pnlButtons.gridy = 1;
		getContentPane().add(pnlButtons, gbc_pnlButtons);
		GridBagLayout gbl_pnlButtons = new GridBagLayout();
		gbl_pnlButtons.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_pnlButtons.rowHeights = new int[]{0, 0};
		gbl_pnlButtons.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_pnlButtons.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlButtons.setLayout(gbl_pnlButtons);
		
		JButton btnCancel = new JButton("Cancel");
		GridBagConstraints gbc_btnCancel = new GridBagConstraints();
		gbc_btnCancel.insets = new Insets(5, 5, 5, 2);
		gbc_btnCancel.gridx = 2;
		gbc_btnCancel.gridy = 0;
		pnlButtons.add(btnCancel, gbc_btnCancel);
		btnCancel.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onButton_Cancel();
			}
		});
		
		JButton btnOk = new JButton("OK");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		GridBagConstraints gbc_btnOk = new GridBagConstraints();
		gbc_btnOk.insets = new Insets(5, 2, 5, 5);
		gbc_btnOk.gridx = 3;
		gbc_btnOk.gridy = 0;
		pnlButtons.add(btnOk, gbc_btnOk);
		btnOk.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onButton_Okay();
			}
		});
		
		listPair = new InExListPair<String>(lstLeft, lstRight, null, null);
	}
	
	/*----- Getters -----*/
	
	public Frame getParent(){return parent;}
	
	public int getSelection(){return selection;}
	
	public Collection<String> getIncluded(){
		listPair.updateSourceLists();
		return listPair.getSourceIncludeList();
	}
	
	/*----- Setters -----*/
	
	public void addIncluded(Set<String> included){
		listPair.getSourceIncludeList().addAll(included);
		listPair.updateGraphicLists();
	}
	
	public void setTagPool(Set<String> tags){
		listPair.getSourceIncludeList().clear();
		Collection<String> src = listPair.getSourceList();
		src.clear();
		src.addAll(tags);
		listPair.updateGraphicLists();
	}
	
	/*----- Draw -----*/
	
	/*----- Actions -----*/

	private void onButton_ArrowLeft(){
		listPair.excludeSelected();
	}
	
	private void onButton_ArrowRight(){
		listPair.includeSelected();
	}
	
	private void onButton_Okay(){
		listPair.updateSourceLists();
		selection = TagPickDialog.SELECTION_OKAY;
		setVisible(false);
	}
	
	private void onButton_Cancel(){
		selection = TagPickDialog.SELECTION_CANCEL;
		setVisible(false);
	}
	
}
