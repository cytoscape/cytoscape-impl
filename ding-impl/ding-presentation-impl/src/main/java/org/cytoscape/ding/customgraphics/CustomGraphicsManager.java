package org.cytoscape.ding.customgraphics;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;

import org.cytoscape.ding.customgraphics.bitmap.MissingImageCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;

public interface CustomGraphicsManager {

	void addCustomGraphicsFactory(CyCustomGraphicsFactory factory, Map props);
	void removeCustomGraphicsFactory(CyCustomGraphicsFactory factory, Map props);
	CyCustomGraphicsFactory getCustomGraphicsFactory(Class<? extends CyCustomGraphics> cls);
	CyCustomGraphicsFactory getCustomGraphicsFactory(String className);
	Collection<CyCustomGraphicsFactory> getAllCustomGraphicsFactories();
	
	void addCustomGraphics(CyCustomGraphics cg, URL source);
	
	Collection<CyCustomGraphics> getAllCustomGraphics();
	Collection<CyCustomGraphics> getAllCustomGraphics(boolean sorted);
	Collection<CyCustomGraphics> getAllPersistantCustomGraphics();
	
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
	
	void addMissingImageCustomGraphics(MissingImageCustomGraphics cg);
	Collection<MissingImageCustomGraphics> reloadMissingImageCustomGraphics();
}
