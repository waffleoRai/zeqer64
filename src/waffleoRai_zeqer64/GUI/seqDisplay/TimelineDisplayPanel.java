package waffleoRai_zeqer64.GUI.seqDisplay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import waffleoRai_SeqSound.n64al.NUSALSeqCmdType;
import waffleoRai_SeqSound.n64al.NUSALSeqCommand;
import waffleoRai_SeqSound.n64al.NUSALSeqCommandMap;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

class TimelineDisplayPanel extends JPanel{

	/*----- Constants -----*/
	
	private static final long serialVersionUID = 8597162803097330257L;
	
	/*----- Instance Variables -----*/
	
	private NUSALSeqCommandMap cmd_source; //Channel commands ONLY!! Voice commands held only by voice panels!
	
	private GridBagLayout layout;
	
	private TrackEventPanel pnlEvent;
	private ArrayList<LevelTimePane> pnlLevels;
	private ArrayList<VoiceEventPanel> pnlVoices;
	
	private Map<LevelPanelType, LevelTimePane> lvlpnlMap;
	
	private TrackColorScheme color_primary;
	private TrackColorScheme color_secondary;
	private TrackColorScheme color_tertiary;
	
	private int h_event = DisplayTrack.HEIGHTS_EVENTPNL[1];
	private int h_level = DisplayTrack.HEIGHTS_LEVELPNL[1];
	private int h_voice = DisplayTrack.HEIGHTS_VOXPNL[1];
	
	private double resolution_tpp = AbstractEventPanel.DEFAULT_TPP;
	private int minor_interval = AbstractEventPanel.DEFAULT_MINOR_IVAL;
	private int major_interval = AbstractEventPanel.DEFAULT_MAJOR_IVAL;
	private int x_offset = DisplayTrack.XOFFS_VZOOM[1]; //Number of pixels left of zero tick
	private int tick_len = 0;
	
	//private boolean recalc_xbuff = true;
	//private boolean repaint = true;
	
	/*----- Init -----*/
	
	public TimelineDisplayPanel(){
		this(TrackColorScheme.GREY);
	}
	
	public TimelineDisplayPanel(TrackColorScheme color){
		pnlLevels = new ArrayList<LevelTimePane>(4);
		pnlVoices = new ArrayList<VoiceEventPanel>(4);
		lvlpnlMap = new HashMap<LevelPanelType, LevelTimePane>();
		color_primary = color;
		if(color_primary == null) color_primary = TrackColorScheme.GREY;
		color_secondary = color_primary;
		cmd_source = new NUSALSeqCommandMap();
		
		initGUI();
	}
	
	private void initGUI(){
		layout = new GridBagLayout();
		layout.columnWidths = new int[]{0, 0};
		layout.rowHeights = new int[]{0};
		layout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		layout.rowWeights = new double[]{Double.MIN_VALUE};
		setLayout(layout);
		
		pnlEvent = new TrackEventPanel();
		pnlEvent.setColorScheme(color_primary);
		addComponentToRow(pnlEvent, 0);
	}
	
	private void addComponentToRow(Component c, int row){
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = 0;
		gbc.gridy = row;
		add(c, gbc);
	}
	
	private void regrid(){
		//Clear
		removeAll();
		
		//Setup rows...
		int lcount = pnlLevels.size();
		int vcount = pnlVoices.size();
		int rcount = 1 + lcount + vcount + 1;
		int[] rheights = new int[rcount];
		double[] rweights = new double[rcount];
		//rheights[rcount-1] = 0;
		rweights[rcount-1] = Double.MIN_VALUE;
		
		//Main event panel...
		int r = 0;
		rheights[r++] = h_event;
		//rweights[r++] = DisplayTrack.ROW_WEIGHT_EVENTS; 
		
		//Level panels...
		for(int i = 0; i < lcount; i++){
			//rweights[r++] = DisplayTrack.ROW_WEIGHT_LEVEL;
			rheights[r++] = h_level;
		}
		
		//Voice panels...
		for(int i = 0; i < vcount; i++){
			//rweights[r++] = DisplayTrack.ROW_WEIGHT_VOX;
			rheights[r++] = h_voice;
		}
		
		layout.rowHeights = rheights;
		layout.rowWeights = rweights;
		
		//Add components
		r = 0;
		addComponentToRow(pnlEvent, r++);
		for(int i = 0; i < lcount; i++){
			addComponentToRow(pnlLevels.get(i), r++);
		}
		
		for(int i = 0; i < vcount; i++){
			addComponentToRow(pnlVoices.get(i), r++);
		}
		updateComponentSizes();
	}
	
	/*private void forceComponentSize(Component c, int width, int height){
		c.setMinimumSize(new Dimension(width, height));
		c.setPreferredSize(new Dimension(width, height));
	}*/
	
	private void updateComponentSizes(){
		int w = x_offset;
		w += ticksToPixels(tick_len);
		layout.columnWidths[0] = w;

		//forceComponentSize(pnlEvent, w, h_event); 
		pnlEvent.setExpectedHeight(h_event);
		for(LevelTimePane pnl : pnlLevels) {
			//forceComponentSize(pnl, w, h_level); 
			pnl.setExpectedHeight(h_level);
		}
		for(VoiceEventPanel pnl : pnlVoices) {
			//forceComponentSize(pnl, w, h_voice); 
			pnl.setExpectedHeight(h_voice);
		}
	}
	
	/*----- Getters -----*/
	
	public TrackEventPanel getEventPane(){return pnlEvent;}
	public int getLevelPanelCount(){return pnlLevels.size();}
	public int getVoiceTrackCount(){return pnlVoices.size();}
	public TrackColorScheme getPrimaryColorScheme(){return color_primary;}
	public TrackColorScheme getSecondaryColorScheme(){return color_secondary;}
	public TrackColorScheme getTertiaryColorScheme(){return color_tertiary;}
	
	public boolean hasLevelPaneOfType(LevelPanelType type){return lvlpnlMap.containsKey(type);}
	
	public int getLeftBuffer(){return x_offset;}
	
	/*----- Setters -----*/
	
	/*protected void setRecalcXBuffer(boolean b){
		recalc_xbuff = b;
		
		//Set components to match.
		pnlEvent.setRecalcXBuffer(b);
		for(VoiceEventPanel vpnl : pnlVoices) vpnl.setRecalcXBuffer(b);
	}
	
	protected void setRepaint(boolean b){
		repaint = b;
		
		//Set components to match.
		pnlEvent.setRepaint(b);
		for(VoiceEventPanel vpnl : pnlVoices) vpnl.setRepaint(b);
	}*/
	
	public void setEventPanelHeight(int pix){h_event = pix; regrid();}
	public void setLevelPanelHeight(int pix){h_level = pix; regrid();}
	public void setVoicePanelHeight(int pix){h_voice = pix; regrid();}
	
	public void setPrimaryColorScheme(TrackColorScheme color){
		if(color == null) color = TrackColorScheme.GREY;
		color_primary = color;
		pnlEvent.setColorScheme(color_primary);
		for(LevelTimePane pnl : pnlLevels) pnl.setColorScheme(color_primary.getNodeColorScheme());
	}
	
	public void setSecondaryColorScheme(TrackColorScheme color){
		if(color == null) color = TrackColorScheme.GREY;
		color_secondary = color;
		for(VoiceEventPanel pnl : pnlVoices) pnl.setColorScheme(color_secondary);
	}
	
	public void setTertiaryColorScheme(TrackColorScheme color){
		color_tertiary = color;
		if(color == null) color = color_secondary;
		for(VoiceEventPanel pnl : pnlVoices) pnl.setSecondaryColorScheme(color);
	}
	
	public void setGrey(){
		setPrimaryColorScheme(TrackColorScheme.GREY);
		setSecondaryColorScheme(TrackColorScheme.GREY);
		setTertiaryColorScheme(TrackColorScheme.GREY);
	}
	
	public void setLengthInTicks(int ticks){
		tick_len = Math.max(ticks, 0);
		updateComponentSizes();
	}
	
	public void setLeftBuffer(int val){
		x_offset = val;
		pnlEvent.setLeftBuffer(val);
		for(LevelTimePane pnl : pnlLevels) pnl.setLeftBufferSize(val);
		for(VoiceEventPanel pnl : pnlVoices) pnl.setLeftBuffer(val);
		updateComponentSizes();
	}
	
	public void setTickMarkIntervals(int major, int minor){
		pnlEvent.setTickMarkIntervals(minor, major);
		for(VoiceEventPanel pnl : pnlVoices) pnl.setTickMarkIntervals(minor, major);
		minor_interval = minor;
		major_interval = major;
	}
	
	public void setResolution(double ticks_per_pixel){
		pnlEvent.setTicksPerPixel(ticks_per_pixel);
		for(LevelTimePane pnl : pnlLevels) pnl.setTicksPerPixel(ticks_per_pixel);
		for(VoiceEventPanel pnl : pnlVoices) pnl.setTicksPerPixel(ticks_per_pixel);
		resolution_tpp = ticks_per_pixel;
		updateComponentSizes();
	}
	
	public void addLevelPanel(LevelPanelType type){
		//Don't forget to see if there already is one for this type.
		if(type == null) return;
		if(lvlpnlMap.get(type) != null) return; //Redundant, they display the same info.
		
		//Create new
		LevelTimePane lvlpnl = new LevelTimePane(color_primary.getNodeColorScheme(), type);
		lvlpnl.setTicksPerPixel(resolution_tpp);
		
		//Add to instance variable collections
		pnlLevels.add(lvlpnl);
		lvlpnlMap.put(type, lvlpnl);
		lvlpnl.setLeftBufferSize(x_offset);
		
		//And populate with existing events!
		List<Integer> tcoords = cmd_source.getTimeCoordinates();
		for(Integer t : tcoords){
			List<NUSALSeqCommand> group = cmd_source.getCommandsAt(t);
			if(group != null){
				for(NUSALSeqCommand cmd : group){
					if(commandIsOfLevelType(cmd, type)){
						int val = cmd.getParam(0);
						lvlpnl.addPoint(t, val);
					}
				}
			}
		}
		
		//Update layout
		regrid();
	}
	
	public void removeLevelPanels(){pnlLevels.clear(); lvlpnlMap.clear(); regrid();}
	
	public void allocateVoicePanels(int vox_count){
		vox_count = Math.min(vox_count, 4);
		vox_count = Math.max(vox_count, 0);
		pnlVoices.clear();
		
		for(int i = 0; i < vox_count; i++){
			VoiceEventPanel vpnl = new VoiceEventPanel(color_secondary);
			if(color_tertiary == null) vpnl.setSecondaryColorScheme(color_secondary);
			else vpnl.setSecondaryColorScheme(color_tertiary);
			vpnl.setTickMarkIntervals(minor_interval, major_interval);
			vpnl.setTicksPerPixel(resolution_tpp);
			vpnl.setLeftBuffer(x_offset);
			pnlVoices.add(vpnl);
			//vpnl.setRecalcXBuffer(recalc_xbuff);
			//vpnl.setRepaint(repaint);
		}
		
		//Update layout
		regrid();
	}
	
	public void addEvent(int tick, NUSALSeqCommand cmd){
		//Also put in level panels if appl.
		cmd_source.addCommand(tick, cmd);
		pnlEvent.addEvent(tick, cmd);
		
		for(LevelTimePane pnl : pnlLevels){
			if(commandIsOfLevelType(cmd, pnl.getLevelValueType())){
				pnl.addPoint(tick, cmd.getParam(0));
			}
		}
	}
	
	public void addEvents(NUSALSeqCommandMap events){
		List<Integer> times = events.getTimeCoordinates();
		for(Integer t : times){
			List<NUSALSeqCommand> list = events.getCommandsAt(t);
			for(NUSALSeqCommand cmd : list){
				addEvent(t, cmd);
			}
		}
	}
	
	public void addVoiceEvent(int vox, int tick, NUSALSeqCommand cmd){
		if(vox < 0 || vox >= pnlVoices.size()) return;
		VoiceEventPanel vpnl = pnlVoices.get(vox);
		vpnl.addEvent(tick, cmd);
	}
	
	public int addVoiceEvents(int vox, NUSALSeqCommandMap cmds){
		if(vox < 0 || vox >= pnlVoices.size()) return 0;
		VoiceEventPanel vpnl = pnlVoices.get(vox);
		return vpnl.addEvents(cmds);
	}
	
	public void clearTrackEvents(){
		//Don't forget to remove from level panels!
		cmd_source.clearCommands();
		pnlEvent.clearEvents();
		for(LevelTimePane pnl : pnlLevels) pnl.clearPoints();
	}
	
	public void clearVoiceEvents(int vox){
		if(vox < 0 || vox >= pnlVoices.size()) return;
		VoiceEventPanel vpnl = pnlVoices.get(vox);
		vpnl.clearEvents();
	}
	
	public void dispose(){
		cmd_source.clearCommands();
		pnlEvent.clearEvents();
		for(LevelTimePane pnl : pnlLevels) pnl.clearPoints();
		for(VoiceEventPanel pnl : pnlVoices)pnl.clearEvents();
		pnlLevels.clear();
		pnlVoices.clear();
		regrid();
	}
	
	/*----- Util -----*/
	
	private static boolean commandIsOfLevelType(NUSALSeqCommand cmd, LevelPanelType type){
		if(cmd == null || type == null) return false;
		switch(type){
		case PAN:
			if(cmd.getCommand() == NUSALSeqCmdType.CH_PAN) return true;
			break;
		case PITCH_BEND:
			if(cmd.getCommand() == NUSALSeqCmdType.CH_PITCHBEND) return true;
			break;
		case TEMPO:
			if(cmd.getCommand() == NUSALSeqCmdType.SET_TEMPO) return true;
			break;
		case VOLUME:
			if(cmd.getCommand() == NUSALSeqCmdType.CH_VOLUME) return true;
			if(cmd.getCommand() == NUSALSeqCmdType.MASTER_VOLUME) return true;
			break;
		}
		return false;
	}
	
	public int ticksToPixels(int ticks){
		int pix = (int)Math.round((double)ticks/resolution_tpp);
		return pix;
	}
	
	/*----- Paint -----*/
	
	/*public void paintComponent(Graphics g){
		if(recalc_xbuff){
			super.paintComponent(g); //I think this should draw panels with current behavior?
			
			//Get left offset from all panes.
			x_offset = pnlEvent.getLeftBufferSize();
			for(VoiceEventPanel vpnl : pnlVoices){
				int xoff = vpnl.getLeftBufferSize();
				if(xoff > x_offset) x_offset = xoff;
			}
		}
		if(repaint){
			//Set x offset for component panes and paint again.
			pnlEvent.setLeftBuffer(x_offset);
			for(LevelTimePane pnl : pnlLevels) pnl.setLeftBufferSize(x_offset);
			for(VoiceEventPanel vpnl : pnlVoices) vpnl.setLeftBuffer(x_offset);
			
			super.paintComponent(g);
		}
	}*/
	
}
