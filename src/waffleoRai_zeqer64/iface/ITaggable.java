package waffleoRai_zeqer64.iface;

import java.util.List;

public interface ITaggable {

	public boolean hasTag(String tag);
	public List<String> getAllTags();
	
	public void addTag(String tag);
	public void clearTags();
	
}
