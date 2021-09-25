package waffleoRai_zeqer64.GUI.seqDisplay;

import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import waffleoRai_SeqSound.n64al.NUSALSeqCmdType;
import waffleoRai_SeqSound.n64al.NUSALSeqCommand;
import waffleoRai_SeqSound.n64al.cmd.NUSALSeqGenericCommand;

public class MultiCmdDisplayNode extends CommandDisplayNode{

	private static final long serialVersionUID = -7530000924505954150L;
	
	private ArrayList<NUSALSeqCommand> commands;
	private int last_rendered_size = -1;
	
	public MultiCmdDisplayNode(int tick) {
		super(new NUSALSeqGenericCommand(NUSALSeqCmdType.MULTI_EVENT_CHUNK), tick);
		commands = new ArrayList<NUSALSeqCommand>();
		
		super.setShapeType(CommandDisplayNode.SHAPE_CIRCLE);
	}
	
	public void addCommand(NUSALSeqCommand cmd){
		commands.add(cmd);
	}
	
	public void clearCommands(){
		commands.clear();
	}
	
	public List<NUSALSeqCommand> getCommands(){
		ArrayList<NUSALSeqCommand> copy = new ArrayList<NUSALSeqCommand>(commands.size()+1);
		copy.addAll(commands);
		return copy;
	}

	public void paintComponent(Graphics g){
		int ccount = commands.size();
		if(ccount != last_rendered_size){
			Image nicon = NumberIconFactory.generate(ccount);
			last_rendered_size = ccount;
			super.setIconImage(nicon);
		}
		super.paintComponent(g);
	}
	
}
