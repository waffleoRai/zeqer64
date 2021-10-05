package waffleoRai_zeqer64.GUI.seqDisplay;

import javax.swing.JPanel;

import waffleoRai_SeqSound.n64al.NUSALSeq;
import waffleoRai_SeqSound.n64al.NUSALSeqCommand;
import waffleoRai_SeqSound.n64al.NUSALSeqCommandSource;

import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collections;
import java.util.List;

import javax.swing.JScrollPane;
import java.awt.GridBagConstraints;
import javax.swing.JTable;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.Dimension;
import java.awt.Font;

class CommandTablePanel extends JPanel{

	private static final long serialVersionUID = 4834634749810889915L;
	
	private JTable table;

	public CommandTablePanel(){
		initGUI();
		load(null);
	}
	
	private void initGUI(){
		this.setMinimumSize(new Dimension(0,0));
		this.setPreferredSize(new Dimension(150,0));
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setMinimumSize(new Dimension(0,0));
		scrollPane.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		gbc_scrollPane.insets = new Insets(5,5,5,5);
		add(scrollPane, gbc_scrollPane);
		
		table = new JTable();
		table.setMinimumSize(new Dimension(0,0));
		table.setFont(new Font("Courier New", Font.PLAIN, 10));
		table.setFillsViewportHeight(true);
		scrollPane.setViewportView(table);
	}
	
	public void load(NUSALSeq seq){
		String[] columns = new String[]{"Address","Command","Serialized"};
		
		if(seq == null){
			DefaultTableModel mdl = new DefaultTableModel(new String[1][3], columns);
			table.setModel(mdl);
		}
		else{
			NUSALSeqCommandSource cmdmap = seq.getAllCommands();
			if(cmdmap == null){
				DefaultTableModel mdl = new DefaultTableModel(new String[1][3], columns);
				table.setModel(mdl);
			}
			else{
				List<Integer> addrlist = cmdmap.getAllAddresses();
				Collections.sort(addrlist);
				int rows = addrlist.size();
				
				String[][] data = new String[rows][3];
				int r = 0;
				for(Integer addr : addrlist){
					data[r][0] = String.format("%04x", addr);
					NUSALSeqCommand cmd = cmdmap.getCommandAt(addr);
					if(cmd == null){
						data[r][1] = data[r][2] = "<null>";
					}
					else{
						data[r][1] = cmd.toString();
						byte[] ser = cmd.serializeMe();
						if(ser == null) data[r][2] = "<error>";
						else{
							data[r][2] = "";
							for(int i = 0; i < ser.length; i++){
								data[r][2] += String.format("%02x ", ser[i]);
							}
						}
					}
					r++;
				}
				
				DefaultTableModel mdl = new DefaultTableModel(data, columns);
				table.setModel(mdl);
			}
		}
		
		repaint();
	}
	
}
