package waffleoRai_zeqer64.GUI.filters;

public abstract class ZeqerFilter<T> {

	public boolean itemPasses(T item){return true;}
	public boolean itemPasses(T item, String txt, long flags){return true;}
}
