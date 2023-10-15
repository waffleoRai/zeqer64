package waffleoRai_zeqer64.GUI.abldEdit;

import waffleoRai_zeqer64.engine.EngineSeqInfo;
import waffleoRai_zeqer64.filefmt.AbldFile;
import waffleoRai_zeqer64.filefmt.ZeqerSeqTable.SeqTableEntry;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;

import java.awt.Component;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JFrame;

import waffleoRai_Utils.VoidCallbackMethod;

public class AbldSeqEditPanel extends AbldSlotEditPanel<SeqSlotEntry> {

	private static final long serialVersionUID = -923663792419337845L;
	
	/*----- Instance Variables -----*/
	
	private JFrame parent;
	private ZeqerCoreInterface core;
	
	private ArrayList<AbldSeqSlotPanel> slotPanels;
	
	private AbldFile refBuild;
	
	/*----- Init -----*/

	public AbldSeqEditPanel(JFrame parentFrame, ZeqerCoreInterface coreIFace, boolean readonly){
		parent = parentFrame;
		core = coreIFace;
		super.readOnly = readonly;
	}
	
	/*----- Getters -----*/
	
	public JFrame getParentFrame(){return parent;}
	
	public boolean isRefLoaded(){
		return refBuild != null;
	}
	
	/*----- Setters -----*/
	
	public void loadRef(AbldFile ref){
		refBuild = ref;
		if(ref != null){
			List<EngineSeqInfo> seqs = ref.getSeqs();
			List<SeqSlotEntry> elist = new LinkedList<SeqSlotEntry>();
			int i = 0;
			for(EngineSeqInfo seq : seqs){
				SeqSlotEntry e = info2Entry(seq, i++);
				if(e != null) elist.add(e);
			}
			super.setRefList(elist);
		}
		else{
			super.setRefList(null);
		}
	}
	
	public void clearRef(){
		refBuild = null;
		super.setRefList(null);
	}
	
	public void loadBuild(AbldFile abld){
		if(slotPanels != null) slotPanels.clear();
		
		if(abld != null){
			super.isInjection = abld.isInjectionBuild();
			List<EngineSeqInfo> seqs = abld.getSeqs();
			if(seqs == null){
				setMyList(null);
				return;
			}
			
			List<SeqSlotEntry> elist;
			if(isInjection){
				if(refList == null || refList.isEmpty()){
					setMyList(null);
					return;
				}

				elist = new ArrayList<SeqSlotEntry>(refList.size());
				if(slotPanels == null) slotPanels = new ArrayList<AbldSeqSlotPanel>(refList.size());
				else slotPanels.ensureCapacity(refList.size());
				
				elist.addAll(refList);
				for(EngineSeqInfo seq : seqs){
					int i = seq.getSubSlot();
					SeqSlotEntry e = info2Entry(seq, i);
					if(e != null) elist.set(i, e);
				}
			}
			else{
				if(slotPanels == null) slotPanels = new ArrayList<AbldSeqSlotPanel>(seqs.size());
				else slotPanels.ensureCapacity(seqs.size());
				
				elist = new LinkedList<SeqSlotEntry>();
				
				int i = 0;
				for(EngineSeqInfo seq : seqs){
					SeqSlotEntry e = info2Entry(seq, i++);
					if(e != null) elist.add(e);
				}
				super.setMyList(elist);
			}
		}
		else{
			super.isInjection = false;
			super.setMyList(null);
		}
	}
	
	/*----- Internal -----*/
	
	protected void onMyListSet(){
		if(slotPanels != null) slotPanels.clear();
	}
		
	private SeqSlotEntry info2Entry(EngineSeqInfo info, int i){
		SeqSlotEntry entry = new SeqSlotEntry();
		entry.index = i;
		if(info != null && core != null){
			int uid = info.getSeqUID();
			if((uid != 0) && (uid != -1)){
				SeqTableEntry meta = core.getSeqInfo(uid);
				entry.entry = meta;
				entry.cacheOverride = info.getCachePolicy();
				entry.medOverride = info.getMedium();
			}
		}
		return entry;
	}
	
	protected SeqSlotEntry newItem(int index){
		SeqSlotEntry entry = new SeqSlotEntry();
		entry.index = index;
		return entry;
	}
	
	protected Component getRefListable(SeqSlotEntry listItem){
		AbldSeqSlotPanel pnl = new AbldSeqSlotPanel(core, true);
		pnl.contract();
		if(listItem != null){
			pnl.setSlotId(listItem.index);
			pnl.setSeq(listItem.entry);
			pnl.setSizeUpdateCallback(new VoidCallbackMethod(){
				public void doMethod() {
					pnlListRef.repaint();
				}});
			
			if(listItem.cacheOverride >= 0) pnl.setCacheOverride(listItem.cacheOverride);
			if(listItem.medOverride >= 0) pnl.setMediumOverride(listItem.medOverride);
		}
		return pnl;
	}
	
	protected Component getMyListable(SeqSlotEntry listItem){
		if(listItem == null) return null;
		
		//See if already there...
		if(slotPanels != null){
			if(listItem.index >= 0 && listItem.index < slotPanels.size()){
				return slotPanels.get(listItem.index);
			}
		}
		
		AbldSeqSlotPanel pnl = new AbldSeqSlotPanel(core, readOnly);
		pnl.contract();
		pnl.setSlotId(listItem.index);
		pnl.setSeq(listItem.entry);
		pnl.setSizeUpdateCallback(new VoidCallbackMethod(){
			public void doMethod() {
				pnlListMine.repaint();
			}});
		
		if(listItem.cacheOverride >= 0) pnl.setCacheOverride(listItem.cacheOverride);
		if(listItem.medOverride >= 0) pnl.setMediumOverride(listItem.medOverride);
		
		//Should probably save links to these components when generated so can retrieve data from them on save...
		if(slotPanels == null) slotPanels = new ArrayList<AbldSeqSlotPanel>(16);
		slotPanels.add(pnl);
		return pnl;
	}
	
	/*----- Callbacks -----*/
	
	protected void btnMinusCallback(){
		if(readOnly || isInjection){
			return;
		}
		
		//Also need to remove from slotPanels
		if(slotPanels != null && !slotPanels.isEmpty()){
			slotPanels.remove(slotPanels.size() - 1);
		}
		
		//TODO Add "are you sure" dialog before calling super?
		super.btnMinusCallback();
	}

	
}
