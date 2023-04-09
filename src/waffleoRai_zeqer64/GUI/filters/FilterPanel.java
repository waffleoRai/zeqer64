package waffleoRai_zeqer64.GUI.filters;

import java.awt.Cursor;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

import waffleoRai_Utils.VoidCallbackMethod;

public abstract class FilterPanel<T> extends JPanel {

	private static final long serialVersionUID = -869531323964748568L;
	
	protected List<VoidCallbackMethod> action_callbacks;
	
	protected FilterPanel(){
		action_callbacks = new LinkedList<VoidCallbackMethod>();
	}

	public abstract boolean itemPassesFilters(T item);
	
	public void addRefilterCallback(VoidCallbackMethod func){
		action_callbacks.add(func);
	}
	
	public void clearRefilterCallbacks(){
		action_callbacks.clear();
	}
	
	protected void triggerRefilterCallbacks(){
		for(VoidCallbackMethod func : action_callbacks) func.doMethod();
	}
	
	public abstract void disableAll();
	public abstract void enableAll();
	
	public void setWait(){
		disableAll();
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	public void unsetWait(){
		enableAll();
		setCursor(null);
	}
	
}
