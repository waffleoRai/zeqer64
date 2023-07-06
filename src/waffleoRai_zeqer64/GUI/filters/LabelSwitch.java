package waffleoRai_zeqer64.GUI.filters;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import waffleoRai_Utils.VoidCallbackMethod;

public class LabelSwitch extends JLabel{

	private static final long serialVersionUID = 4132004972213402510L;
	
	public static final int TYPE_TWO_PHASE = 0;
	public static final int TYPE_THREE_PHASE = 1;
	
	public static final int SWITCHSTATE_OFF = 0;
	public static final int SWITCHSTATE_ON = 1;
	public static final int SWITCHSTATE_ON_NOT = 2;

	/*----- Instance Variables -----*/
	
	private int switch_type = TYPE_TWO_PHASE;
	private int current_state = SWITCHSTATE_OFF;
	
	private Color color_off = Color.black;
	private Color color_on = Color.white;
	private Color color_not = Color.red;
	
	private List<VoidCallbackMethod> callbacks;
	
	/*----- Init -----*/
	
	public LabelSwitch(String init_str){
		super(init_str);
		initgui();
	}
	
	public LabelSwitch(String init_str, int type){
		super(init_str);
		switch_type = type;
		initgui();
	}
	
	private void initgui(){
		callbacks = new LinkedList<VoidCallbackMethod>();
		
		setBorder(BorderFactory.createEtchedBorder());
		setHorizontalAlignment(SwingConstants.CENTER);
		setVerticalAlignment(SwingConstants.CENTER);
		
		this.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e) {
				onMouseClick(e.getButton());
			}
		});
	}
	
	/*----- Getters -----*/
	
	public int getSwitchState(){return current_state;}
	
	public int estimateWidth(Graphics g){
		Font f = getFont();
		String s = getText();
		if(s == null) s = "";
		if(g == null){
			return ((f.getSize() + 2) * this.getText().length()) + 10;
		}
		
		return g.getFontMetrics(f).stringWidth(s) + 6;
	}
	
	/*----- Setters -----*/
	
	public void setOnColor(Color c){
		if(c == null) return;
		color_on = c;
		updateForeground();
	}
	
	public void setOffColor(Color c){
		if(c == null) return;
		color_off = c;
		updateForeground();
	}
	
	public void setNotColor(Color c){
		if(c == null) return;
		color_not = c;
		updateForeground();
	}
	
	public void setSwitchState(int state){
		current_state = state;
		updateForeground();
	}
	
	public void addCallback(VoidCallbackMethod func){
		callbacks.add(func);
	}
	
	public void clearCallbacks(){
		callbacks.clear();
	}
	
	/*----- Draw -----*/
	
	private void updateForeground(){
		switch(current_state){
		case SWITCHSTATE_OFF:
			super.setForeground(color_off);
			break;
		case SWITCHSTATE_ON:
			super.setForeground(color_on);
			break;
		case SWITCHSTATE_ON_NOT:
			super.setForeground(color_not);
			break;
		}
		repaint();
	}
	
	/*----- Actions -----*/
	
	private void onMouseClick(int btn){
		if(!isEnabled()) return;
		switch(current_state){
		case SWITCHSTATE_OFF:
			if(switch_type != TYPE_THREE_PHASE || btn == MouseEvent.BUTTON1){
				current_state = SWITCHSTATE_ON;
				super.setForeground(color_on);
			}
			else{
				current_state = SWITCHSTATE_ON_NOT;
				super.setForeground(color_not);
			}
			break;
		case SWITCHSTATE_ON:
			if(switch_type == TYPE_THREE_PHASE){
				if(btn == MouseEvent.BUTTON1){
					current_state = SWITCHSTATE_OFF;
					super.setForeground(color_off);
				}
				else{
					current_state = SWITCHSTATE_ON_NOT;
					super.setForeground(color_not);
				}
			}
			else{
				current_state = SWITCHSTATE_OFF;
				super.setForeground(color_off);
			}
			break;
		case SWITCHSTATE_ON_NOT:
			if(btn == MouseEvent.BUTTON1){
				current_state = SWITCHSTATE_ON;
				super.setForeground(color_on);
			}
			else{
				current_state = SWITCHSTATE_OFF;
				super.setForeground(color_off);
			}
			break;
		}
		for(VoidCallbackMethod func : callbacks) func.doMethod();
		repaint();
	}
	
}
