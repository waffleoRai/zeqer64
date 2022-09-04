package waffleoRai_zeqer64.GUI;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

public abstract class FilterListPanel<T> extends JPanel{

	private static final long serialVersionUID = 8733538999687623796L;
	
	/*----- Instance Variables -----*/
	
	/*----- Init -----*/
	
	public FilterListPanel() {
	}
	
	private void initGUI(){
		
	}
	
	/*----- Getters -----*/
	
	/*----- Setters -----*/
	
	/*----- Drawing -----*/
	
	/*----- Actions -----*/
	
	/*----- Text Boxes -----*/
	
	public void showWarning(String text){
		JOptionPane.showMessageDialog(this, text, "Warning", JOptionPane.WARNING_MESSAGE);
	}
	
	public void showError(String text){
		JOptionPane.showMessageDialog(this, text, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	public void showInfo(String text){
		JOptionPane.showMessageDialog(this, text, "Notice", JOptionPane.INFORMATION_MESSAGE);
	}

}
