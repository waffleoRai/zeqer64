package waffleoRai_zeqer64.GUI.seqDisplay;

import java.util.List;

import javax.swing.JPanel;

import waffleoRai_SeqSound.n64al.NUSALSeqCommand;
import waffleoRai_SeqSound.n64al.NUSALSeqCommandMap;

public class DisplayTrack {
	//This is NOT a component as it draws two different panels SEPARATELY
	
	/*----- Constants -----*/
	
	public static final double ROW_WEIGHT_EVENTS = 3.0;
	public static final double ROW_WEIGHT_LEVEL = 1.0;
	public static final double ROW_WEIGHT_VOX = 2.0;
	
	public static final int DEFO_HEIGHT_EVENTPNL = 60;
	public static final int DEFO_HEIGHT_LEVELPNL = 30;
	public static final int DEFO_HEIGHT_VOXPNL = 40;
	
	//Vertical zooms
	public static final int[] XOFFS_VZOOM = {15, 20, 50};
	public static final int[] HEIGHTS_EVENTPNL = {30, 65, 100};
	public static final int[] HEIGHTS_LEVELPNL = {10, 30, 60};
	public static final int[] HEIGHTS_VOXPNL = {20, 50, 75};
	public static final int HEIGHT_BUFFER = 6;
	public static final int MAX_VZOOM_LVL = DisplayTrack.HEIGHTS_EVENTPNL.length-1;
	
	//Horizontal zooms
	public static final int NUS_AL_TPQN = 48;
	public static final double[] TPP_ZOOM_VALS = {0.125, 0.25, 0.5, 1.0,
												  2.0, 3.0, 4.0, 6.0,
												  8.0, 12.0, 16.0, 24.0};
	public static final int[] MARK_IVALS_MINOR = {1, 6, 6,12,12,24, 24, 48, 48, 96,192,192};
	public static final int[] MARK_IVALS_MAJOR = {6,24,24,48,96,96,192,192,192,192,384,384};
	
	//Interval of major ticks marked with number
	public static final int[] MARK_IVALS_NUM = {1,1,1,1,1,1,1,1,2,2,2,3}; 
	
	public static final double TPP_MAX_RES = 0.125; //8 pixels per tick
	public static final double TPP_MIN_RES = 24;
	
	/*----- Instance Variables -----*/
	
	private TrackColorScheme color_primary;
	private TrackColorScheme color_secondary;
	private TrackColorScheme color_tertiary;
	
	private int min_height = 0;
	
	private int zoom_lvl = 6;
	private int vzoom_lvl = 1;
	
	private boolean isInactive;
	
	private TrackLabelPanel pnlLabel;
	private TimelineDisplayPanel pnlTimeline;
	
	/*----- Initialization -----*/
	
	public DisplayTrack(TrackColorScheme color, NUSALSeqCommandMap commands){
		if(color == null) color = TrackColorScheme.GREY;
		color_primary = color;
		color_secondary = color;
		color_tertiary = color;
		
		//Generate the sub-panels, add commands...
		pnlLabel = new TrackLabelPanel();
		pnlLabel.setPrimaryColorScheme(color_primary);
		pnlLabel.setSecondaryColorScheme(color_secondary);
		pnlLabel.setTrackName("(New Track)");
		
		pnlTimeline = new TimelineDisplayPanel(color_primary);
		pnlTimeline.setSecondaryColorScheme(color_secondary);
		if(commands != null) pnlTimeline.addEvents(commands);
		
		updateHeight();
	}
	
	/*----- Getters -----*/
	
	public JPanel getTimelinePanel(){return pnlTimeline;}
	public JPanel getLabelPanel(){return pnlLabel;}
	public boolean isInactive(){return isInactive;}
	public TrackColorScheme getPrimaryColor(){return color_primary;}
	public TrackColorScheme getSecondaryColor(){return color_secondary;}
	public TrackColorScheme getTertiaryColor(){return color_tertiary;}
	public int getXBuffer(){return pnlTimeline.getLeftBuffer();}
	public String getTrackName(){return pnlLabel.getTrackName();}
	
	public int getLevelPanelCount(){return pnlTimeline.getLevelPanelCount();}
	public int getVoiceTrackCount(){return pnlTimeline.getVoiceTrackCount();}
	public int getTrackHeight(){return min_height;}
	
	/*----- Setters -----*/
	
	private void updateHeight(){
		min_height = HEIGHTS_EVENTPNL[vzoom_lvl];
		int lct = pnlTimeline.getLevelPanelCount();
		int vct = pnlTimeline.getVoiceTrackCount();
		min_height += HEIGHTS_LEVELPNL[vzoom_lvl] * lct;
		min_height += HEIGHTS_VOXPNL[vzoom_lvl] * vct;
		min_height += HEIGHT_BUFFER;
		
		setXBuffer(XOFFS_VZOOM[vzoom_lvl]);
	}
	
	public void setInactive(boolean b){
		isInactive = b;
		if(b){
			//Grey out
			pnlLabel.setGrey();
			pnlTimeline.setGrey();
		}
		else{
			//Restore colors
			pnlLabel.setPrimaryColorScheme(color_primary);
			pnlLabel.setSecondaryColorScheme(color_secondary);
			pnlTimeline.setPrimaryColorScheme(color_primary);
			pnlTimeline.setSecondaryColorScheme(color_secondary);
			pnlTimeline.setTertiaryColorScheme(color_tertiary);
		}
	}
	
	public void setPrimaryColorScheme(TrackColorScheme color){
		color_primary = color;
		if(color_primary == null) color_primary = TrackColorScheme.GREY;
		pnlLabel.setPrimaryColorScheme(color_primary);
		pnlTimeline.setPrimaryColorScheme(color_primary);
	}
	
	public void setSecondaryColorScheme(TrackColorScheme color){
		color_secondary = color;
		if(color_secondary == null) color_secondary = color_primary;
		pnlLabel.setSecondaryColorScheme(color_secondary);
		pnlTimeline.setSecondaryColorScheme(color_secondary);
	}
	
	public void setTertiaryColorScheme(TrackColorScheme color){
		color_tertiary = color;
		if(color_tertiary == null) color_tertiary = color_secondary;
		pnlTimeline.setTertiaryColorScheme(color_tertiary);
	}
	
	public void addLevelPanel(LevelPanelType type){
		//Check if already has it
		if(type == null) return;
		if(pnlTimeline.hasLevelPaneOfType(type)) return;
		
		pnlLabel.addLevelPaneType(type);
		pnlTimeline.addLevelPanel(type);
		updateHeight();
	}
	
	public void clearLevelPanels(){
		pnlLabel.clearLevelPaneLabels();
		pnlTimeline.removeLevelPanels();
		updateHeight();
	}
	
	public void allocVoiceTracks(int vcount){
		vcount = Math.min(vcount, 4);
		vcount = Math.max(0, vcount);
		clearVoiceTracks();
		if(vcount == 0) return;
		pnlLabel.setVoiceCount(vcount);
		pnlTimeline.allocateVoicePanels(vcount);
		updateHeight();
	}
	
	public boolean loadVoiceTrack(int idx, NUSALSeqCommandMap commands){
		if(commands == null) return false;
		if(idx < 0) return false;
		int vcount = pnlTimeline.getVoiceTrackCount();
		if(idx >= vcount) return false;
		
		/*List<Integer> times = commands.getTimeCoordinates();
		if(times == null) return false;
		for(Integer t : times){
			List<NUSALSeqCommand> list = commands.getCommandsAt(t);
			if(list == null) continue;
			for(NUSALSeqCommand cmd : list){
				pnlTimeline.addVoiceEvent(idx, t, cmd);
			}
		}*/
		int loaded = pnlTimeline.addVoiceEvents(idx, commands);
		System.err.println("Events loaded: " + loaded);
		
		return true;
	}
	
	public void clearVoiceTracks(){
		pnlLabel.setVoiceCount(0);
		pnlTimeline.allocateVoicePanels(0);
	}
	
	/*public void setTimelineRepaintMode(boolean b){
		if(b){
			//Allow full repaint
			pnlTimeline.setRecalcXBuffer(false);
			pnlTimeline.setRepaint(true);
		}
		else{
			//Allow only x offset calculation
			pnlTimeline.setRecalcXBuffer(true);
			pnlTimeline.setRepaint(false);
		}
	}*/
	
	public void setLengthInTicks(int ticks){
		pnlTimeline.setLengthInTicks(ticks);
	}
	
	public void setTrackName(String name){pnlLabel.setTrackName(name);}
	
	public void setXBuffer(int pix){
		pnlTimeline.setLeftBuffer(pix);
	}
	
	public void dispose(){
		pnlLabel.clearLevelPaneLabels();
		pnlTimeline.dispose();
	}
	
	/*----- Zoom Level Handling -----*/
	
	public boolean zoomIn(){
		if(zoom_lvl <= 0) return false;
		zoom_lvl--;
		
		pnlTimeline.setResolution(TPP_ZOOM_VALS[zoom_lvl]);
		pnlTimeline.setTickMarkIntervals(MARK_IVALS_MAJOR[zoom_lvl], MARK_IVALS_MINOR[zoom_lvl]);
		
		return true;
	}
	
	public boolean zoomOut(){
		if(zoom_lvl >= TPP_ZOOM_VALS.length) return false;
		zoom_lvl++;
		
		pnlTimeline.setResolution(TPP_ZOOM_VALS[zoom_lvl]);
		pnlTimeline.setTickMarkIntervals(MARK_IVALS_MAJOR[zoom_lvl], MARK_IVALS_MINOR[zoom_lvl]);
		
		return true;
	}
	
	public void zoomInVertical(){
		if(vzoom_lvl >= MAX_VZOOM_LVL){vzoom_lvl = MAX_VZOOM_LVL; return;}
		vzoom_lvl++;
		pnlLabel.zoomInVertical();
		pnlTimeline.setEventPanelHeight(HEIGHTS_EVENTPNL[vzoom_lvl]);
		pnlTimeline.setLevelPanelHeight(HEIGHTS_LEVELPNL[vzoom_lvl]);
		pnlTimeline.setVoicePanelHeight(HEIGHTS_VOXPNL[vzoom_lvl]);
	}
	
	public void zoomOutVertical(){
		if(vzoom_lvl <= 0){vzoom_lvl = 0; return;}
		vzoom_lvl--;
		pnlLabel.zoomOutVertical();
		pnlTimeline.setEventPanelHeight(HEIGHTS_EVENTPNL[vzoom_lvl]);
		pnlTimeline.setLevelPanelHeight(HEIGHTS_LEVELPNL[vzoom_lvl]);
		pnlTimeline.setVoicePanelHeight(HEIGHTS_VOXPNL[vzoom_lvl]);
	}
}
