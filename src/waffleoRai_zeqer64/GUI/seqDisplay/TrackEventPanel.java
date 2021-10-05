package waffleoRai_zeqer64.GUI.seqDisplay;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import waffleoRai_SeqSound.n64al.NUSALSeqCommand;

class TrackEventPanel extends AbstractEventPanel{

	//min/preferred size should be recalculated whenever:
	//	- Ticks per pixel (zoom level) is set or
	//	- A new command is added that changes the track length
	
	/*----- Constants -----*/
	
	private static final long serialVersionUID = 7333078946589654233L;

	/*----- Instance Variables -----*/
	
	private Map<Integer, List<CommandDisplayNode>> nodes;
	
	private boolean allow_node_resize = false; //Height resize behavior
	private int node_dim = CommandDisplayNode.DEFO_DIM;
	private int max_per_tick = 3; //Max nodes visible on a single tick (if nodes are set to resize)
	
	private int last_height = 0;
	private LinkedList<NodeDrawGroup> precluster; //If null, then recluster
	
	/*----- Initialization -----*/
	
	public TrackEventPanel(){
		nodes = new TreeMap<Integer, List<CommandDisplayNode>>();
		super.setExpectedHeight(DisplayTrack.HEIGHTS_EVENTPNL[1]);
		initGUI();
	}
	
	private void initGUI(){
		setColorScheme(TrackColorScheme.GREY);
		//super.setLeftBuffer((CommandDisplayNode.DEFO_DIM >>> 1) + 2);
		super.setLayout(null);
	}
	
	/*----- Inner Classes -----*/
	
	private static class NodeDrawGroup{
		
		private int xoff;
		private int nodedim;
		private boolean drawmini;
		private LinkedList<CommandDisplayNode> nodes;
		
		public NodeDrawGroup(){
			xoff = 0;
			nodes = new LinkedList<CommandDisplayNode>();
		}
		
	}
	
	/*----- Getters -----*/
	
	/*----- Setters -----*/
	
	protected void putEvent(int tick, NUSALSeqCommand cmd) {
		if(tick < 0) return;
		//precluster = null;
		List<CommandDisplayNode> list = nodes.get(tick);
		if(list == null){
			list = new LinkedList<CommandDisplayNode>();
			nodes.put(tick, list);
		}
		
		CommandDisplayNode cnode = new CommandDisplayNode(cmd, tick);
		cnode.setColorScheme(getColorScheme().getNodeColorScheme());
		list.add(cnode);
	}
	
	public void setResizeBehavior_fixedRows(int rows){
		precluster = null;
		allow_node_resize = true;
		max_per_tick = rows;
	}
	
	public void setResizeBehavior_fixedNodeSize(int pix){
		precluster = null;
		allow_node_resize = false;
		node_dim = pix;
	}
	
	public void clearEvents(){
		precluster = null;
		last_height = -1;
		nodes.clear();
	}
	
	/*----- Util -----*/
	
	protected void onEventsUpdate(){
		precluster = null;
		last_height = -1;
	}
	
	protected void updateNodeColor(ZeqerColorScheme clr) {
		precluster = null;
		if(nodes == null || nodes.isEmpty()) return;
		//System.err.println("Updating node color to: " + clr.toString());
		for(List<CommandDisplayNode> elist : nodes.values()){
			for(CommandDisplayNode e : elist) e.setColorScheme(clr);
		}
	}
	
	protected void recalculateMinimumLeftBuffer(Graphics2D g){
		Rectangle cb = g.getClipBounds();
		if(allow_node_resize){
			//Fixed row count
			int ppr = cb.height/max_per_tick;
			if(ppr <= CommandDisplayNode.DEFO_DIM_MINI){
				//Mini
				setLeftBuffer(CommandDisplayNode.DEFO_DIM_MINI >>> 1);
			}
			else setLeftBuffer((ppr >>> 1) + 2);
		}
		else{
			//Fixed node size. Draw as many rows as can fit.
			if(cb.height <= CommandDisplayNode.DEFO_DIM_MINI){
				setLeftBuffer(CommandDisplayNode.DEFO_DIM_MINI >>> 1);
			}
			else{
				setLeftBuffer((node_dim >>> 1) + 2);
			}
		}
	}
	
	/*----- Drawing -----*/
	
	protected void regrid(){
		super.removeAll();
		int exheight = super.getExpectedHeight();
		if(last_height != exheight) precluster = null;
		if(precluster == null) precluster = clusterNodes(exheight);
		last_height = exheight;
		
		for(NodeDrawGroup group : precluster){
			int y = 1;
			for(CommandDisplayNode node : group.nodes){
				int yend = y+group.nodedim;
				if(yend > exheight) break; //Don't bother drawing nodes that go outside bounds.
				
				node.setMini(group.drawmini);
				node.setBounds(group.xoff, y, group.nodedim, group.nodedim);
				add(node);
				
				y+=group.nodedim+1;
			}
		}
	}
	
	protected void onResizeTrigger(){
		precluster = null;
		last_height = 0;
		regrid();
	}
	
	private LinkedList<NodeDrawGroup> clusterNodes(int clip_height){

		//Allocate output and sort event coordinates
		LinkedList<NodeDrawGroup> out = new LinkedList<NodeDrawGroup>();
		LinkedList<Integer> tickcoords = new LinkedList<Integer>();
		tickcoords.addAll(nodes.keySet());
		Collections.sort(tickcoords);
		
		//Determine row count & node dimension...
		int rows = max_per_tick;
		int dim = node_dim;
		boolean mini = false;
		final int minidim = CommandDisplayNode.DEFO_DIM_MINI; //To save space
		if(!allow_node_resize){
			//Need row count
			dim += 2; //Buffer
			rows = clip_height/(dim+1);
			if(rows < 1){
				mini = true;
				dim = minidim + 2;
				rows = Math.max(1, clip_height/(dim+1));
			}
		}
		else{
			//Need node dim
			dim = clip_height/rows;
			if(dim <= minidim){
				mini = true;
				dim = minidim+2;
				rows = Math.min(clip_height/(dim+1), max_per_tick);
			}
		}
		int halfdim = dim >>> 1;
		int tdim = pixelsToTicks(dim);
		
		while(!tickcoords.isEmpty()){
			int grouploc = tickcoords.pop();
			NodeDrawGroup group = new NodeDrawGroup();
			group.xoff = super.getLeftBufferSize() + ticksToPixels(grouploc) - halfdim;
			group.nodedim = dim;
			group.drawmini = mini;
			
			int added = 0;
			//Add events from this tick.
			List<CommandDisplayNode> egroup = nodes.get(grouploc);
			MultiCmdDisplayNode multi = null;
			for(CommandDisplayNode n : egroup){
				if(added < rows-1){
					group.nodes.add(n);
					//System.err.println("DEBUG Node added: " + n.getCommand().toString());
				}
				else{
					if(multi == null){
						multi = new MultiCmdDisplayNode(grouploc);
						multi.setColorScheme(getColorScheme().getNodeColorScheme());
						group.nodes.add(multi);
					}
					multi.addCommand(n.getCommand());
				}
				added++;
			}
			
			//Determine how many ticks will be clustered into this group
			int end_tick = tdim + grouploc;
			
			//Pull ticks and add events from that tick until outside cluster range
			while(!tickcoords.isEmpty() && tickcoords.peek() < end_tick){
				int mytick = tickcoords.pop();
				egroup = nodes.get(mytick);
				for(CommandDisplayNode n : egroup){
					if(added < rows-1){
						group.nodes.add(n);
						//System.err.println("DEBUG Node added: " + n.getCommand().toString());
					}
					else{
						if(multi == null){
							multi = new MultiCmdDisplayNode(mytick);
							multi.setColorScheme(getColorScheme().getNodeColorScheme());
							group.nodes.add(multi);
						}
						multi.addCommand(n.getCommand());
					}
					added++;
				}
			}
			
			//Add group to output list
			out.add(group);
		}
		
		return out;
	}

	protected void onRepaint(Graphics2D g) {
		/*super.removeAll();
		//Rectangle cb = g.getClipBounds();
		int exheight = super.getExpectedHeight();
		
		//Recluster, if needed
		if(last_height != exheight) precluster = null;
		if(precluster == null) precluster = clusterNodes(exheight);
		last_height = exheight;*/
	}

	protected void drawEventMarkers(Graphics2D g) {}

}
