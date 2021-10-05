package waffleoRai_zeqer64.GUI.seqDisplay;

import java.util.ArrayList;
import java.util.List;

import waffleoRai_SeqSound.n64al.NUSALSeqCmdType;
import waffleoRai_SeqSound.n64al.NUSALSeqCommand;
import waffleoRai_SeqSound.n64al.cmd.NUSALSeqGenericCommand;

public class MultiCmdDisplayNode extends CommandDisplayNode{

	private static final long serialVersionUID = -7530000924505954150L;
	
	private ArrayList<NUSALSeqCommand> commands;
	//private int last_rendered_size = -1;
	
	public MultiCmdDisplayNode(int tick) {
		super(new NUSALSeqGenericCommand(NUSALSeqCmdType.MULTI_EVENT_CHUNK), tick);
		commands = new ArrayList<NUSALSeqCommand>();
		
		super.setShapeType(CommandDisplayNode.SHAPE_CIRCLE);
		updateNumberIcon();	
		updateTooltip();
	}
	
	public void addCommand(NUSALSeqCommand cmd){
		commands.add(cmd);
		updateTooltip();
		updateNumberIcon();
	}
	
	public void clearCommands(){
		commands.clear();
		updateTooltip();
		updateNumberIcon();
	}
	
	protected void updateTooltip(){
		if(commands.isEmpty()){
			super.setToolTipText("Empty Multi-node");
			return;
		}
		NUSALSeqCommand cmd0 = commands.get(0);
		StringBuilder sb = new StringBuilder(512);
		sb.append(commands.size());
		sb.append(" commands @ 0x");
		sb.append(Integer.toHexString(cmd0.getAddress()));
		sb.append(", tick ");
		sb.append(super.getTick());
		super.setToolTipText(sb.toString());
	}
	
	protected void updateNumberIcon(){
		int ccount = commands.size();
		super.setIconDrawer(new NumberIconDrawer(ccount));
	}
	
	public List<NUSALSeqCommand> getCommands(){
		ArrayList<NUSALSeqCommand> copy = new ArrayList<NUSALSeqCommand>(commands.size()+1);
		copy.addAll(commands);
		return copy;
	}
	
}
