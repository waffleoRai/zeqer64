package waffleoRai_zeqer64.GUI.filters;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import waffleoRai_Utils.VoidCallbackMethod;

public class TagLabel extends JLabel{

	private static final long serialVersionUID = -6989317816027090312L;
	
	/*----- Instance Variables -----*/
	
	private Color std_color = Color.black;
	private Color not_color = Color.red;
	
	private boolean not_mode = false;
	
	private List<VoidCallbackMethod> lc_callbacks;
	private List<VoidCallbackMethod> rc_callbacks;
	
	/*----- Init -----*/
	
	public TagLabel(String init_str){
		super(init_str);
		rc_callbacks = new LinkedList<VoidCallbackMethod>();
		lc_callbacks = new LinkedList<VoidCallbackMethod>();
		initGUI();
	}
	
	private void initGUI(){
		setBorder(BorderFactory.createEtchedBorder());
		this.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				int btn = e.getButton();
				if(btn == MouseEvent.BUTTON1){
					onLeftClick();
				}
				else{
					onRightClick();
				}
			}
		});
		
		updateForeground();
	}
	
	/*----- Getters -----*/
	
	public boolean inNotMode(){return not_mode;}
	
	/*----- Setters -----*/
	
	public void setBaseTextColor(Color c){
		if(c == null) return;
		std_color = c;
		updateForeground();
	}
	
	public void setNotModeColor(Color c){
		if(c == null) return;
		not_color = c;
		updateForeground();
	}
	
	public void setNotMode(boolean b){
		not_mode = b;
		updateForeground();
	}
	
	public void addRightClickCallback(VoidCallbackMethod func){
		rc_callbacks.add(func);
	}
	
	public void clearRightClickCallbacks(){
		rc_callbacks.clear();
	}
	
	public void addLeftClickCallback(VoidCallbackMethod func){
		lc_callbacks.add(func);
	}
	
	public void clearLeftClickCallbacks(){
		lc_callbacks.clear();
	}
	
	/*----- Draw -----*/
	
	private void updateForeground(){
		if(not_mode){
			super.setForeground(not_color);
		}
		else{
			super.setForeground(std_color);
		}
		repaint();
	}
	
	/*----- Actions -----*/
	
	public void onLeftClick(){
		if(!isEnabled()) return;
		setNotMode(!not_mode);
		for(VoidCallbackMethod func : lc_callbacks){
			func.doMethod();
		}
	}
	
	public void onRightClick(){
		if(!isEnabled()) return;
		for(VoidCallbackMethod func : rc_callbacks){
			func.doMethod();
		}
	}

}
