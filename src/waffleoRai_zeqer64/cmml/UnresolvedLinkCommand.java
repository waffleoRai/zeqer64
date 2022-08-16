package waffleoRai_zeqer64.cmml;

import waffleoRai_SeqSound.n64al.NUSALSeqCmdType;
import waffleoRai_SeqSound.n64al.NUSALSeqCommand;

public class UnresolvedLinkCommand extends NUSALSeqCommand{

	public UnresolvedLinkCommand() {
		super(NUSALSeqCmdType.UNRESOLVED_LINK, (byte) 0x00);
	}

}
