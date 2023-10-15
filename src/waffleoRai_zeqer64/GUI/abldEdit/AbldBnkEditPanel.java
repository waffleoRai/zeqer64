package waffleoRai_zeqer64.GUI.abldEdit;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import waffleoRai_Utils.VoidCallbackMethod;
import waffleoRai_zeqer64.engine.EngineBankInfo;
import waffleoRai_zeqer64.engine.EngineWaveArcInfo;
import waffleoRai_zeqer64.filefmt.AbldFile;
import waffleoRai_zeqer64.iface.ZeqerCoreInterface;

public class AbldBnkEditPanel extends AbldSlotEditPanel<BnkSlotEntry>{

	private static final long serialVersionUID = 3396839236952349070L;
	
	/*----- Instance Variables -----*/
	
	private JFrame parent;
	private ZeqerCoreInterface core;
	
	private boolean refLoaded = false;
	private ArrayList<AbldBnkSlotPanel> slotPanels;
	private ArrayList<String> warcNames;
	
	/*----- Init -----*/

	public AbldBnkEditPanel(JFrame parentFrame, ZeqerCoreInterface coreIFace, boolean readonly){
		parent = parentFrame;
		core = coreIFace;
		super.readOnly = readonly;
		warcNames = new ArrayList<String>(8);
	}
	
	/*----- Getters -----*/
	
	public JFrame getParentFrame(){return parent;}
	public boolean isRefLoaded(){return refLoaded;}

	/*----- Setters -----*/
	
	public void loadWarcNames(ArrayList<String> names){
		warcNames.clear();
		if(names != null) warcNames.addAll(names);
	}
	
	public void loadRef(AbldFile ref){
		if(ref == null){
			clearRef();
			return;
		}
		
		List<EngineBankInfo> blist = ref.getBanks();
		if(blist == null){
			clearRef();
			return;
		}
		
		int i = 0;
		List<BnkSlotEntry> entries = new ArrayList<BnkSlotEntry>(blist.size()+1);
		for(EngineBankInfo info : blist){
			entries.add(bankInfoToEntry(info, i));
			i++;
		}
		super.setRefList(entries);
		
		refLoaded = true;
		reenable();
	}
	
	public void clearRef(){
		refLoaded = false;
		super.setRefList(null);
		reenable();
	}
	
	public void loadBuild(AbldFile abld){
		if(abld == null){
			clearBuild();
			return;
		}
		
		//Wave Arc Names
		warcNames.clear();
		List<EngineWaveArcInfo> warcs = abld.getWaveArcs();
		if(warcs != null && !warcs.isEmpty()){
			warcNames.ensureCapacity(warcs.size());
			for(EngineWaveArcInfo warc : warcs){
				warcNames.add(warc.getName());
			}
		}
		
		//Fonts
		super.isInjection = abld.isInjectionBuild();
		List<EngineBankInfo> blist = abld.getBanks();
		if(blist == null){
			clearBuild();
			return;
		}
		
		int i = 0;
		if(abld.isInjectionBuild()){
			if(super.refList == null || super.refList.isEmpty()){
				clearBuild();
				return;
			}
			
			if(slotPanels == null){
				slotPanels = new ArrayList<AbldBnkSlotPanel>(refList.size()+1);
			}
			else slotPanels.ensureCapacity(refList.size());
			
			List<BnkSlotEntry> entries = new ArrayList<BnkSlotEntry>(refList.size()+1);
			entries.addAll(refList);
			for(EngineBankInfo info : blist){
				entries.set(info.getSubSlot(), bankInfoToEntry(info, info.getSubSlot()));
			}
		}
		else{
			if(slotPanels == null){
				slotPanels = new ArrayList<AbldBnkSlotPanel>(blist.size());
			}
			else slotPanels.ensureCapacity(blist.size());
			
			List<BnkSlotEntry> entries = new ArrayList<BnkSlotEntry>(blist.size()+1);
			for(EngineBankInfo info : blist){
				entries.add(bankInfoToEntry(info, i));
				i++;
			}
			super.setMyList(entries);
		}
		
		reenable();
	}
	
	public void clearBuild(){
		super.setMyList(null);
		reenable();
	}
	
	/*----- Internal -----*/
	
	protected void onMyListSet(){
		if(slotPanels != null) slotPanels.clear();
	}
	
	protected BnkSlotEntry bankInfoToEntry(EngineBankInfo info, int index){
		BnkSlotEntry entry = new BnkSlotEntry();
		entry.index = index;
		entry.cacheOverride = info.getCachePolicy();
		entry.medOverride = info.getMedium();
		entry.warcIndex = info.getWArc1();
		
		if(core != null){
			entry.entry = core.getBankInfo(info.getBankUID());
		}
		
		return entry;
	}
	
	protected Component getRefListable(BnkSlotEntry listItem) {
		AbldBnkSlotPanel pnl = new AbldBnkSlotPanel(core, readOnly);
		pnl.loadWaveArcNames(warcNames);
		if(listItem != null){
			pnl.setSlotId(listItem.index);
			pnl.setBank(listItem.entry);
			pnl.setCacheOverride(listItem.cacheOverride);
			pnl.setMediumOverride(listItem.medOverride);
			pnl.setWaveArcIndex(listItem.warcIndex);
			pnl.setSizeUpdateCallback(new VoidCallbackMethod(){
				public void doMethod() {
					pnlListRef.repaint();
				}});
		}
		return pnl;
	}

	protected Component getMyListable(BnkSlotEntry listItem) {
		AbldBnkSlotPanel pnl = new AbldBnkSlotPanel(core, readOnly);
		pnl.loadWaveArcNames(warcNames);
		if(listItem != null){
			pnl.setSlotId(listItem.index);
			pnl.setBank(listItem.entry);
			pnl.setCacheOverride(listItem.cacheOverride);
			pnl.setMediumOverride(listItem.medOverride);
			pnl.setWaveArcIndex(listItem.warcIndex);
			pnl.setSizeUpdateCallback(new VoidCallbackMethod(){
				public void doMethod() {
					pnlListMine.repaint();
				}});
			slotPanels.add(pnl);
		}
		return pnl;
	}

	protected BnkSlotEntry newItem(int index) {
		BnkSlotEntry entry = new BnkSlotEntry();
		entry.index = index;
		return entry;
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
