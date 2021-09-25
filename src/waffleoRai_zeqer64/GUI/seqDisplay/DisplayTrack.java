package waffleoRai_zeqer64.GUI.seqDisplay;

public class DisplayTrack {

	private TrackColorScheme colorScheme;
	private TrackEventPanel event_pnl;
	
	public DisplayTrack(TrackColorScheme color){
		colorScheme = color;
	}
	
	public TrackEventPanel getEventPanel(){return event_pnl;}
	
}
