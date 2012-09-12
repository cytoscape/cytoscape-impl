package org.cytoscape.ding.customgraphics;

import java.net.URL;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;

import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;

public interface CustomGraphicsManager {

	void addCustomGraphicsFactory(CyCustomGraphicsFactory factory);
	void removeCustomGraphicsFactory(CyCustomGraphicsFactory factory);
	CyCustomGraphicsFactory getCustomGraphicsFactory(Class<? extends CyCustomGraphics> cls);
	CyCustomGraphicsFactory getCustomGraphicsFactory(String className);
	Collection<CyCustomGraphicsFactory> getAllCustomGraphicsFactories();
	
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
	
	/**
	 * Provides id available ID;
	 * @return 
	 */
	Long getNextAvailableID();
		
}
