package waffleoRai_zeqer64.GUI.seqDisplay;

import java.awt.Dimension;

import javax.swing.JFrame;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JPanel;
import java.awt.Insets;

import waffleoRai_SeqSound.n64al.NUSALSeq;
import waffleoRai_SeqSound.n64al.NUSALSeqChannel;
import waffleoRai_SeqSound.n64al.NUSALSeqCommandMap;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class SeqDisplayForm extends JFrame{

	/*----- Constants -----*/
	
	private static final long serialVersionUID = -5924941739239218627L;
	
	public static final int MIN_WIDTH = 640;
	public static final int MIN_HEIGHT = 480;

	/*----- Instance Variables -----*/
	
	private TrackDisplayPanel pnlTracks;
	private CommandTablePanel pnlCommands;
	
	private TrackColorScheme color_seq = TrackColorScheme.RED;
	private TrackColorScheme color_ch = TrackColorScheme.YELLOW;
	private TrackColorScheme color_disabled = TrackColorScheme.GREY;
	private TrackColorScheme color_vox = TrackColorScheme.BLUE;
	private TrackColorScheme color_voxsub = TrackColorScheme.PURPLE;
	
	private NUSALSeq sequence;
	
	/*----- Initialization -----*/
	
	public SeqDisplayForm(){
		//TODO
		init();
		
	}
	
	private void init(){
		setTitle("(test)");
		setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0, 0};
		gridBagLayout.columnWeights = new double[]{4.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 4.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JPanel pnlInfo = new JPanel();
		pnlInfo.setLayout(null);
		GridBagConstraints gbc_pnlInfo = new GridBagConstraints();
		gbc_pnlInfo.insets = new Insets(0, 0, 5, 5);
		gbc_pnlInfo.fill = GridBagConstraints.BOTH;
		gbc_pnlInfo.gridx = 0;
		gbc_pnlInfo.gridy = 0;
		getContentPane().add(pnlInfo, gbc_pnlInfo);
		
		JLabel lblNewLabel = new JLabel("New label");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(10, 11, 167, 14);
		pnlInfo.add(lblNewLabel);
		
		pnlCommands = new CommandTablePanel();
		GridBagConstraints gbc_spCmdTable = new GridBagConstraints();
		gbc_spCmdTable.gridheight = 2;
		gbc_spCmdTable.fill = GridBagConstraints.BOTH;
		gbc_spCmdTable.gridx = 1;
		gbc_spCmdTable.gridy = 0;
		//gbc_spCmdTable.weightx = 0.2;
		getContentPane().add(pnlCommands, gbc_spCmdTable);
		
		pnlTracks = new TrackDisplayPanel(17);
		GridBagConstraints gbc_spTracks = new GridBagConstraints();
		gbc_spTracks.insets = new Insets(0, 0, 0, 5);
		gbc_spTracks.fill = GridBagConstraints.BOTH;
		gbc_spTracks.gridx = 0;
		gbc_spTracks.gridy = 1;
		getContentPane().add(pnlTracks, gbc_spTracks);
		

	}
	
	public void render(){
		this.pack();
		this.setVisible(true);
	}
	
	/*----- Getters -----*/
	
	/*----- Setters -----*/
	
	/*----- Sequence Load/Sync -----*/
	
	private void loadSeqTrack(){
		NUSALSeqCommandMap seqcmds = sequence.getCommandTickMap();
		DisplayTrack track = pnlTracks.addTrack("Sequence", color_seq, seqcmds);
		track.addLevelPanel(LevelPanelType.TEMPO);
		track.addLevelPanel(LevelPanelType.VOLUME);
	}
	
	private void loadChannel(int ch){
		if(!sequence.channelEnabled(ch)){
			pnlTracks.addTrack("Channel " + ch, color_disabled, null);
		}
		else{
			NUSALSeqChannel channel = sequence.getChannel(ch);
			DisplayTrack track = pnlTracks.addTrack("Channel " + ch, color_ch, channel.getCommandTickMap());
			track.addLevelPanel(LevelPanelType.VOLUME);
			track.addLevelPanel(LevelPanelType.PAN);
			track.addLevelPanel(LevelPanelType.PITCH_BEND);
			//Determine how many voices are valid...
			int vcount = 4;
			for(int i = 3; i >= 0; i--){
				NUSALSeqCommandMap map = channel.getVoiceCommandTickMap(i);
				if(map == null || map.isEmpty()) vcount--;
				else break;
			}
			track.allocVoiceTracks(vcount);
			track.setSecondaryColorScheme(color_vox);
			track.setTertiaryColorScheme(color_voxsub);
			for(int i = 0; i < vcount; i++){
				System.err.println("Loading voice track: " + ch + "-" + i);
				track.loadVoiceTrack(i, channel.getVoiceCommandTickMap(i));
			}
		}
	}
	
	private void updateToMatchSeq(){
		if(sequence == null){
			pnlTracks.clearTracks();
			pnlCommands.load(null);
			return;
		}
		pnlCommands.load(sequence);
		pnlTracks.clearTracks();
		loadSeqTrack();
		for(int i = 0; i < 16; i++){
			loadChannel(i);
		}
		
		pnlTracks.setMinimumTicks((int)sequence.getLengthInTicks());
	}
	
	public void loadSequence(NUSALSeq seq){
		sequence = seq;
		updateToMatchSeq();
		repaint();
	}
	
	/*----- GUI Controls -----*/
	
	/*----- Painting -----*/
	
	
	
}
