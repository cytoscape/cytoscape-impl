package org.cytoscape.ding.customgraphics;

import java.net.URL;
import java.util.Collection;
import java.util.Properties;
import java.util.SortedSet;

public interface CustomGraphicsManager {
	
	void addCustomGraphics(CyCustomGraphics cg, URL source);
	
	Collection<CyCustomGraphics> getAllCustomGraphics();
	
	CyCustomGraphics getCustomGraphicsByID(Long id);
	CyCustomGraphics getCustomGraphicsBySourceURL(URL source);
	
	SortedSet<Long> getIDSet();
	
	Properties getMetadata();
	
	boolean isUsedInCurrentSession(final CyCustomGraphics graphics);
	void setUsedInCurrentSession(final CyCustomGraphics graphics, final Boolean isUsed);
	
	void removeAllCustomGraphics();
	void removeCustomGraphics(Long id);
		
}
