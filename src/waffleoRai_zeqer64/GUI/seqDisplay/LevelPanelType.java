package waffleoRai_zeqer64.GUI.seqDisplay;

import java.awt.image.BufferedImage;

enum LevelPanelType {

	VOLUME,
	PAN,
	TEMPO,
	PITCH_BEND;
	
	public static BufferedImage getIconFor(LevelPanelType type){
		switch(type){
		case PAN:
			return IconFactory.getMappedIcon("pan");
		case PITCH_BEND:
			return IconFactory.getMappedIcon("pitchbend");
		case TEMPO:
			return IconFactory.getMappedIcon("tempo");
		case VOLUME:
			return IconFactory.getMappedIcon("volume");
		default:
			break;
		}
		return null;
	}
	
}
