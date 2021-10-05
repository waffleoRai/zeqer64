package waffleoRai_zeqer64.GUI.seqDisplay;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.LinkedList;

import waffleoRai_SeqSound.n64al.NUSALSeqCmdType;
import waffleoRai_SeqSound.n64al.NUSALSeqCommand;
import waffleoRai_SeqSound.n64al.cmd.NUSALSeqNoteCommand;
import waffleoRai_SeqSound.n64al.cmd.NUSALSeqWaitCommand;

class VoiceEventPanel extends AbstractEventPanel{

	/*----- Constants -----*/
	
	private static final long serialVersionUID = 4655888026926213107L;
	
	public static final int SHADEBY_PITCH = 0;
	public static final int SHADEBY_VELOCITY = 1;
	public static final int SHADEBY_GATE = 2;
	
	private static final int ROW_TOP = 0;
	private static final int ROW_MIDDLE = 1; //Size is always minidim + 2. Other two rows are each half of remaining height.
	private static final int ROW_BOTTOM = 2;
	
	public static final int WAIT_NODE_ALPHA = 127;
	
	/*----- Instance Variables -----*/
	
	private TrackColorScheme color_sub;
	
	private int shadeby;
	private boolean number_octave; //Whether or not to include octave number in labels
	
	//For redrawing, so only resize in needed direction
	private int lastw = 0;
	private int lasth = 0;
	
	//private boolean dirty;
	private LinkedList<VoiceEventNode> nodelist;
	
	/*----- Init -----*/
	
	public VoiceEventPanel(){
		this(TrackColorScheme.GREY);
	}
	
	public VoiceEventPanel(TrackColorScheme primary_color){
		super(primary_color);
		color_sub = super.getColorScheme();
		shadeby = SHADEBY_PITCH;
		number_octave = true;
		nodelist = new LinkedList<VoiceEventNode>();
		this.setLayout(null);
		//dirty = false;
		super.setExpectedHeight(DisplayTrack.HEIGHTS_VOXPNL[1]);
	}
	
	/*----- Inner Classes -----*/
	
	private static class VoiceEventNode implements Comparable<VoiceEventNode>{
		
		public CommandDisplayNode viewNode;
		public int row;
		public int ticks = -1;
		
		public VoiceEventNode(CommandDisplayNode node){
			viewNode = node;
		}

		public boolean equals(Object o){
			return this == o;
		}
		
		public int compareTo(VoiceEventNode o) {
			if(o == null) return 1;
			if(this.viewNode == null){
				if(o.viewNode == null) return 0;
				else return -1;
			}
			
			if(o.viewNode == null) return 1;
			
			return this.viewNode.getTick() - o.viewNode.getTick();
		}
		
	}
	
	/*----- Getters -----*/
	
	/*----- Setters -----*/
	
	protected void onEventsUpdate(){
		updateNodes();
	}
	
	protected void updateNodeColor(ZeqerColorScheme clr){
		//dirty = true; //Will update on paint.
		updateNodes();
	}
	
	protected void putEvent(int tick, NUSALSeqCommand cmd){
		//Look for different command types.
		CommandDisplayNode dnode = new CommandDisplayNode(cmd, tick);
		if(cmd instanceof NUSALSeqNoteCommand){
			dnode.setShapeType(CommandDisplayNode.SHAPE_EXT_RECTANGLE);
		}
		else if(cmd instanceof NUSALSeqWaitCommand){
			dnode.setShapeType(CommandDisplayNode.SHAPE_EXT_RECTANGLE);
			dnode.setMainAlpha(WAIT_NODE_ALPHA);
		}
		else{
			dnode.setShapeType(CommandDisplayNode.SHAPE_DIAMOND);
			dnode.setMini(true);
		}
		VoiceEventNode node = new VoiceEventNode(dnode);
		nodelist.add(node);
		add(node.viewNode);
		//dirty = true;
	}
	
	public void setSecondaryColorScheme(TrackColorScheme color){
		if(color == null){
			color = super.getColorScheme();
		}
		color_sub = color;
		//dirty = true;
		updateNodes();
	}
	
	public void setNoteColorLevelDeterminer(int det){
		if(det < SHADEBY_PITCH) det = SHADEBY_PITCH;
		if(det > SHADEBY_GATE) det = SHADEBY_GATE;
		shadeby = det;
		updateNodes();
	}
	
	public void setDisplayOctaveNumbers(boolean b){
		number_octave = b;
		updateNodes();
	}
	
	public void forceRedraw(){
		updateNodes();
		positionNodes();
	} //Like if the key is changed so note names can be updated
	
	public void clearEvents(){
		//dirty = true;
		nodelist.clear();
		removeAll();
	}
	
	/*----- Utils -----*/
	
	private void positionNodes(){
		//super.removeAll();
		if(nodelist == null || nodelist.isEmpty()) return;
		final int minidim = CommandDisplayNode.DEFO_DIM_MINI; //Just to shorten typing
		
		int height = super.getExpectedHeight();
		int ew = super.getExpectedWidth();
		boolean do_height = (height != lasth);
		boolean do_width = (ew != lastw);
		if(do_height) lasth = height;
		if(do_width) lastw = ew;
		
		//Calculate row locations
		//Need to have at least 5 pixels for both top and bottom rows, otherwise only middle is drawn
		int[][] rowdims = new int[3][2]; //First col is y coord, second is height
		if(do_height){
			rowdims[1][1] = minidim;
			int min_height = minidim + 10;
			if(height < min_height){
				rowdims[0] = new int[]{-1,-1};
				rowdims[2] = new int[]{-1,-1};
						
				int yc = height/2;
				int off = minidim >>> 1;
				rowdims[1][0] = yc - off;
			}
			else{
				int yc = height/2;
				int minioff = minidim >>> 1;
				rowdims[0][0] = 1;
				rowdims[0][1] = rowdims[1][0] = yc - minioff;
				rowdims[0][1]--;
				rowdims[2][1] = rowdims[0][1];
				rowdims[2][0] = yc + minioff;
			}	
		}
		
		//Now draw(add) events...
		int xoff = super.getLeftBufferSize();
		for(VoiceEventNode node : nodelist){
			int x = 0, y = 0, h = 0, w = 0;
			Rectangle bounds = node.viewNode.getBounds();
			if(do_height){
				y = rowdims[node.row][0];
				if(y < 0) continue;
				h = rowdims[node.row][1];
			}
			else{
				y = bounds.y;
				h = bounds.height;
			}
			
			if(do_width){
				x = ticksToPixels(node.viewNode.getTick()) + xoff;
				if(node.ticks > 0){
					w = ticksToPixels(node.ticks);
					
					w -= 1; //Gap
					//x++;
				}
				else{
					w = minidim;
					x -= w>>>1;
				}
			}
			else{
				x = bounds.x;
				w = bounds.width;
			}
	
			node.viewNode.setBounds(x,y,w,h);
			//add(node.viewNode);
		}
	}
	
	private void updateNodes(){
		if(nodelist == null) return;
		Collections.sort(nodelist);
		
		/*
		 * Need to update:
		 * 	- Color Scheme
		 * 	- Row
		 *	- Set color level based on event value (might also be notes only)
		 * 	- (If note) - Icon & MIDI value
		 * 	- (If note) Update tooltip text
		 */
		
		int transpose = 0;
		int lasttime = 0;
		boolean insub = false; //In subroutine call? True between branch commands and END or return commands
		
		NoteNameIconFactory.setDenoteOctaveStatic(number_octave);
		for(VoiceEventNode node : nodelist){
			NUSALSeqCommand cmd = node.viewNode.getCommand();
			if(cmd instanceof NUSALSeqNoteCommand){
				NUSALSeqNoteCommand notecmd = (NUSALSeqNoteCommand)cmd;
				notecmd.updateMidiValue(transpose);
				//BufferedImage noteico = NoteNameIconFactory.getNoteAsImage(notecmd.getMidiNote());
				String notestr = NoteNameIconFactory.getNoteAsString(notecmd.getMidiNote(), false);
				//String notestr = "DEBUG";  Placeholder to check loading speed
				//node.viewNode.setIconImage(noteico);
				node.viewNode.setIconDrawer(new NoteNameIconDrawer(notecmd.getMidiNote()));
				
				StringBuilder sb = new StringBuilder(512);
				sb.append(cmd.toString());
				sb.append(" [MIDI " + notecmd.getMidiNote());
				sb.append(" (" + notestr + ")] ");
				sb.append(" (@0x" + Integer.toHexString(cmd.getAddress()));
				sb.append(", tick " + node.viewNode.getTick() + ")");
				node.viewNode.setToolTipText(sb.toString());
				
				if(insub){
					node.viewNode.setColorScheme(color_sub.getNodeColorScheme());
					node.row = ROW_BOTTOM;
				}
				else{
					node.viewNode.setColorScheme(getColorScheme().getNodeColorScheme());
					node.row = ROW_TOP;
				}
				
				//Shade
				switch(shadeby){
				case SHADEBY_PITCH:
					node.viewNode.setColorLevel((int)notecmd.getMidiNote());
					break;
				case SHADEBY_VELOCITY:
					node.viewNode.setColorLevel((int)notecmd.getVelocity());
					break;
				case SHADEBY_GATE:
					node.viewNode.setColorLevel((int)((notecmd.getGate()+128)/2));
					break;
				}
				
				if(node.ticks < 0){
					//Update
					if(cmd.getCommand() == NUSALSeqCmdType.PLAY_NOTE_NVG){
						node.ticks = lasttime;
					}
					else{
						node.ticks = cmd.getSizeInTicks();
						lasttime = node.ticks;
					}
				}
				
			}
			else if(cmd instanceof NUSALSeqWaitCommand){
				//RESTs also must be on note track, not middle track.
				//Color determined by whether insub
				if(insub){
					node.viewNode.setColorScheme(color_sub.getNodeColorScheme());
					node.row = ROW_BOTTOM;
				}
				else{
					node.viewNode.setColorScheme(getColorScheme().getNodeColorScheme());
					node.row = ROW_TOP;
				}
				
				if(node.ticks < 0){
					//Update
					node.ticks = cmd.getSizeInTicks();
				}
			}
			else{
				//Look for: TRANSPOSE, any kind of branch, END_READ, and RETURN
				node.row = ROW_MIDDLE;
				if(insub){
					node.viewNode.setColorScheme(color_sub.getNodeColorScheme());
				}
				else{
					node.viewNode.setColorScheme(getColorScheme().getNodeColorScheme());
				}
				//See if need to row switch
				if(cmd.isBranch()){
					insub = true;
				}
				else{
					switch(cmd.getCommand()){
					case END_READ:
					case RETURN:
						insub = false;
						break;
					case TRANSPOSE:
						transpose = cmd.getParam(0);
						break;
					default:
						break;
					}
				}
			}
		}
		//dirty = false;
	}
	
	/*----- Paint -----*/
	
	protected void onResizeTrigger(){
		//updateNodes();
		positionNodes();
	}
	
	protected void recalculateMinimumLeftBuffer(Graphics2D g){
		setLeftBuffer(CommandDisplayNode.DEFO_DIM_MINI >>> 1);
	}
	
	protected void onRepaint(Graphics2D g){
		//super.removeAll();
		//if(dirty) updateNodes();
	}
	
	protected void drawEventMarkers(Graphics2D g){}

}
