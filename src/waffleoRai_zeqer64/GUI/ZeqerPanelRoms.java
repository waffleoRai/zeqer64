package waffleoRai_zeqer64.GUI;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import waffleoRai_zeqer64.ZeqerCoreInterface;
import waffleoRai_zeqer64.ZeqerRom;
import waffleoRai_zeqer64.GUI.dialogs.progress.IndefProgressDialog;
import waffleoRai_zeqer64.filefmt.NusRomInfo;

import javax.swing.border.BevelBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import waffleoRai_Containers.nintendo.nus.N64ROMImage;
import waffleoRai_GUITools.WriterPanel;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

public class ZeqerPanelRoms extends JPanel{

	private static final long serialVersionUID = 1687456758126872944L;
	
	public static final String ROMPNL_LAST_IMPORT_PATH = "ZROMPNL_LASTIMPORT";
	public static final String ROMPNL_LAST_XML_PATH = "ZROMPNL_LASTXML";

	/*----- Instance Variables -----*/
	
	private ZeqerCoreInterface core;
		
	private JFrame parent;
	private WriterPanel pnlInfo;
	private JList<GuiRomNode> lstRoms;
	
	private List<ZeqerRom> roms;
	
	/*----- Init -----*/

	public ZeqerPanelRoms(ZeqerCoreInterface corelink, JFrame parent_frame){
		core = corelink;
		parent = parent_frame;
		roms = new LinkedList<ZeqerRom>();
		initGUI();
		refreshRomList();
	}
	
	private void initGUI(){
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		JPanel pnlLeft = new JPanel();
		GridBagConstraints gbc_pnlLeft = new GridBagConstraints();
		gbc_pnlLeft.insets = new Insets(5, 5, 5, 5);
		gbc_pnlLeft.fill = GridBagConstraints.BOTH;
		gbc_pnlLeft.gridx = 0;
		gbc_pnlLeft.gridy = 0;
		add(pnlLeft, gbc_pnlLeft);
		GridBagLayout gbl_pnlLeft = new GridBagLayout();
		gbl_pnlLeft.columnWidths = new int[]{0, 0};
		gbl_pnlLeft.rowHeights = new int[]{0, 0, 0};
		gbl_pnlLeft.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_pnlLeft.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		pnlLeft.setLayout(gbl_pnlLeft);
		
		JScrollPane spLeft = new JScrollPane();
		spLeft.setViewportBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		GridBagConstraints gbc_spLeft = new GridBagConstraints();
		gbc_spLeft.insets = new Insets(0, 0, 5, 0);
		gbc_spLeft.fill = GridBagConstraints.BOTH;
		gbc_spLeft.gridx = 0;
		gbc_spLeft.gridy = 0;
		pnlLeft.add(spLeft, gbc_spLeft);
		
		lstRoms = new JList<GuiRomNode>();
		spLeft.setViewportView(lstRoms);
		lstRoms.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e) {
				onSelectListItem();
			}
		});
		lstRoms.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){
				if(e.getButton() == MouseEvent.BUTTON2 || e.getButton() == MouseEvent.BUTTON3){
					onRightclickList(e.getX(), e.getY());
				}
			}
		});
		
		JPanel pnlBtns = new JPanel();
		GridBagConstraints gbc_pnlBtns = new GridBagConstraints();
		gbc_pnlBtns.fill = GridBagConstraints.BOTH;
		gbc_pnlBtns.gridx = 0;
		gbc_pnlBtns.gridy = 1;
		pnlLeft.add(pnlBtns, gbc_pnlBtns);
		GridBagLayout gbl_pnlBtns = new GridBagLayout();
		gbl_pnlBtns.columnWidths = new int[]{0, 0, 0, 0};
		gbl_pnlBtns.rowHeights = new int[]{0, 0};
		gbl_pnlBtns.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gbl_pnlBtns.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		pnlBtns.setLayout(gbl_pnlBtns);
		
		JButton btnAdd = new JButton("Import...");
		GridBagConstraints gbc_btnAdd = new GridBagConstraints();
		gbc_btnAdd.insets = new Insets(5, 5, 5, 5);
		gbc_btnAdd.gridx = 2;
		gbc_btnAdd.gridy = 0;
		pnlBtns.add(btnAdd, gbc_btnAdd);
		btnAdd.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				onClickAddButton();
			}
		});
		
		pnlInfo = new WriterPanel();
		GridBagConstraints gbc_spRight = new GridBagConstraints();
		gbc_spRight.fill = GridBagConstraints.BOTH;
		gbc_spRight.gridx = 1;
		gbc_spRight.gridy = 0;
		add(pnlInfo, gbc_spRight);
	}
	
	/*----- Getters -----*/
	
	public ZeqerRom getSelectedRom(){
		GuiRomNode node = lstRoms.getSelectedValue();
		if(node == null) return null;
		return node.getRom();
	}
	
	public List<ZeqerRom> getSelectedRoms(){
		LinkedList<ZeqerRom> romlist = new LinkedList<ZeqerRom>();
		List<GuiRomNode> selected = lstRoms.getSelectedValuesList();
		if(selected == null) return romlist;
		for(GuiRomNode node : selected){
			romlist.add(node.getRom());
		}
		return romlist;
	}
	
	/*----- Setters -----*/
	
	/*----- Draw -----*/
	
	public void writeInfoToRightpane(ZeqerRom rom){
		pnlInfo.clear();
		try{
			Writer pnlWriter = pnlInfo.getWriter();
			if(rom == null){
				pnlWriter.write("<Please select one ROM>");
			}
			else{
				NusRomInfo rominfo = rom.getRomInfo();
				N64ROMImage romhead = rom.getRomHead();
				pnlWriter.write(rominfo.getROMName()); pnlWriter.write("\n");
				pnlWriter.write("Gamecode: " + romhead.getGamecode() + "\n");
				pnlWriter.write("Internal Name: " + romhead.getName() + "\n");
				
				pnlWriter.write("Version: ");
				switch(rominfo.getGameVersionEnum()){
				case ZeqerRom.GAME_OCARINA_V1_0: pnlWriter.write("Ocarina of Time V1.0"); break;
				case ZeqerRom.GAME_OCARINA_V1_1: pnlWriter.write("Ocarina of Time V1.1"); break;
				case ZeqerRom.GAME_OCARINA_V1_2: pnlWriter.write("Ocarina of Time V1.2"); break;
				case ZeqerRom.GAME_OCARINA_GC_V1_1: pnlWriter.write("Ocarina of Time (Gamecube - V1.1 Based)"); break;
				case ZeqerRom.GAME_OCARINA_GC_V1_2: pnlWriter.write("Ocarina of Time (Gamecube - V1.2 Based)"); break;
				case ZeqerRom.GAME_OCARINA_V0_9: pnlWriter.write("Ocarina of Time Prerelease Build"); break;
				case ZeqerRom.GAME_OCARINA_MQ_V1_1: pnlWriter.write("Ocarina of Time Master Quest (V1.1 Based)"); break;
				case ZeqerRom.GAME_OCARINA_MQ_V1_2: pnlWriter.write("Ocarina of Time Master Quest (V1.2 Based)"); break;
				case ZeqerRom.GAME_OCARINA_MQDBG_V1_1: pnlWriter.write("Ocarina of Time Master Quest (V1.1 Based) Debug"); break;
				case ZeqerRom.GAME_OCARINA_GZ: pnlWriter.write("Ocarina of Time Practice ROM (gz)"); break;
				case ZeqerRom.GAME_OCARINA_MOD: pnlWriter.write("Ocarina of Time Modded ROM (General)"); break;
				case ZeqerRom.GAME_OCARINA_MQ_GZ: pnlWriter.write("Ocarina of Time Master Quest Practice ROM (gz)"); break;
				case ZeqerRom.GAME_OCARINA_MQ_MOD: pnlWriter.write("Ocarina of Time Master Quest Modded ROM (General)"); break;
				case ZeqerRom.GAME_MAJORA_V1_0_J: pnlWriter.write("Majora's Mask (Japan Release)"); break;
				case ZeqerRom.GAME_MAJORA_V1_0_I: pnlWriter.write("Majora's Mask (International)"); break;
				case ZeqerRom.GAME_MAJORA_GC: pnlWriter.write("Majora's Mask (Gamecube)"); break;
				case ZeqerRom.GAME_MAJORA_KZ: pnlWriter.write("Majora's Mask Practice ROM (kz)"); break;
				case ZeqerRom.GAME_MAJORA_MOD: pnlWriter.write("Majora's Mask Modded ROM (General)"); break;
				default: pnlWriter.write("(Unknown)"); break;
				}
				pnlWriter.write("\n");
				
				pnlWriter.write("Video Output: ");
				switch(rominfo.getTVType()){
				case ZeqerRom.TV_TYPE__NTSC: pnlWriter.write("NTSC"); break;
				case ZeqerRom.TV_TYPE__PAL: pnlWriter.write("PAL"); break;
				case ZeqerRom.TV_TYPE__MPAL: pnlWriter.write("MPAL"); break;
				default: pnlWriter.write("(Unknown)"); break;
				}
				pnlWriter.write("\n");
				
				pnlWriter.write("Region: ");
				char regchar = romhead.getGamecode().charAt(3);
				switch(regchar){
				case 'E': pnlWriter.write("North America"); break;
				case 'J': pnlWriter.write("Japan"); break;
				case 'P': pnlWriter.write("Europe/PAL"); break;
				default: pnlWriter.write("\'" + regchar + "\'"); break;
				}
				pnlWriter.write("\n");
				
				pnlWriter.write("Zeqer ID: " + rominfo.getZeqerID() + "\n");
				pnlWriter.write("Standard MD5: " + rominfo.getMD5String() + "\n");
				
				pnlWriter.write("CRC1: " + String.format("%08x", romhead.getCRC1()) + "\n");
				pnlWriter.write("CRC2: " + String.format("%08x", romhead.getCRC2()) + "\n");
				pnlWriter.write("dmadata Offset: " + Long.toHexString(rominfo.getDMADataOffset()) + "\n");
				pnlWriter.write("Path: " + rom.getRomPath() + "\n");
			}
			pnlWriter.close();
		}
		catch(Exception ex){
			ex.printStackTrace();
			showError("ROM info could not be rendered");
		}
		pnlInfo.repaint();
	}
	
	public void refreshRomList(){
		roms.clear();
		if(core != null){
			roms.addAll(core.getImportedRoms());
			ArrayList<GuiRomNode> nodelist = new ArrayList<GuiRomNode>(roms.size()+1);
			for(ZeqerRom rom : roms){
				nodelist.add(new GuiRomNode(rom));
			}
			Collections.sort(nodelist);
			
			//Now add to Jlist
			DefaultListModel<GuiRomNode> mdl = new DefaultListModel<GuiRomNode>();
			for(GuiRomNode node : nodelist) mdl.addElement(node);
			lstRoms.setModel(mdl);
		}
		lstRoms.clearSelection();
		lstRoms.repaint();
		writeInfoToRightpane(null);
	}
	
	/*----- Actions -----*/
	
	public void onRightclickList(int x, int y){
		JPopupMenu menu = new JPopupMenu();
		
		JMenuItem menuitem = new JMenuItem("Remove");
		menu.add(menuitem);
		menuitem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				onClickMenuSelection_Delete();
			}
		});
		
		menu.show(lstRoms, x, y);
	}
	
	public void onClickMenuSelection_Delete(){
		//This allows multiple deletions.
		if(core == null) return;
		if(!core.removeImportedRoms(getSelectedRoms())){
			showError("ROM removal failed. See stderr for details.");
		}
		refreshRomList();
	}
	
	public void onSelectListItem(){
		//Update info panel
		writeInfoToRightpane(getSelectedRom());
	}
	
	public void onClickAddButton(){
		if(core == null) return;
		
		int op = JOptionPane.showOptionDialog(this, "Would you like to manually provide an xml specification?", "Manual Import", 
				JOptionPane.YES_NO_CANCEL_OPTION, 
				JOptionPane.QUESTION_MESSAGE, null, null, null);
		
		if(op == JOptionPane.CANCEL_OPTION) return;
		
		JFileChooser fc;
		String xmlpath = null;
		if(op == JOptionPane.YES_OPTION){
			fc = new JFileChooser(core.getSetting(ROMPNL_LAST_XML_PATH));
			fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			fc.addChoosableFileFilter(new FileFilter(){
				public boolean accept(File f) {
					String fpath = f.getAbsolutePath();
					if(fpath.endsWith(".xml")) return true;
					return false;
				}
				public String getDescription() {
					return "eXtensible Markup Language Document (.xml)";
				}
			});	
			
			op = fc.showOpenDialog(this);
			if(op == JFileChooser.APPROVE_OPTION){
				xmlpath = fc.getSelectedFile().getAbsolutePath();
				core.setSetting(ROMPNL_LAST_XML_PATH, xmlpath);
			}
			else if(op == JFileChooser.CANCEL_OPTION){
				return;
			}
		}

		//Deal with the path selection.
		fc = new JFileChooser(core.getSetting(ROMPNL_LAST_IMPORT_PATH));
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.addChoosableFileFilter(new FileFilter(){
			public boolean accept(File f) {
				String fpath = f.getAbsolutePath();
				if(fpath.endsWith(".z64")) return true;
				if(fpath.endsWith(".n64")) return true;
				return false;
			}
			public String getDescription() {
				return "Nintendo 64 Cartridge ROM Image (.z64, .n64)";
			}
		});
		
		fc.addChoosableFileFilter(new FileFilter(){
			public boolean accept(File f) {
				String fpath = f.getAbsolutePath();
				if(fpath.endsWith(".gcm")) return true;
				if(fpath.endsWith(".iso")) return true;
				return false;
			}
			public String getDescription() {
				return "Nintendo Gamecube Disc Image (.gcm, .iso)";
			}
		});
		op = fc.showOpenDialog(this);
		if(op != JFileChooser.APPROVE_OPTION) return;
		
		String rompath = fc.getSelectedFile().getAbsolutePath();
		core.setSetting(ROMPNL_LAST_IMPORT_PATH, rompath);
		String xmlpath_final = xmlpath;
		
		//Now do import in background.
		IndefProgressDialog dialog = new IndefProgressDialog(parent, "ROM Import");
		dialog.setPrimaryString("Importing N64 ROM");
		dialog.setSecondaryString("Reading " + rompath);
		SwingWorker<Void, Void> task = new SwingWorker<Void, Void>(){
			protected Void doInBackground() throws Exception{
				try{
					String result = null;
					if(xmlpath_final != null){
						result = core.importRom(rompath, xmlpath_final, dialog);
						if(result != null){
							showError(result);
						}
					}
					else{
						result = core.importRom(rompath, dialog);
						if(result != null){
							showError(result);
						}
					}
				}
				catch(Exception ex){
					ex.printStackTrace();
					showError("Unknown Error: Import failed!");
				}
				return null;
			}
			
			public void done(){
				dialog.closeMe();
			}
		};
		
		task.execute();
		dialog.render();
	}
	
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
