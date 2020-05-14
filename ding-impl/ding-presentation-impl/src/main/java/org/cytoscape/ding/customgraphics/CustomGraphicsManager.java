package org.cytoscape.ding.customgraphics;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;

import org.cytoscape.ding.customgraphics.image.MissingImageCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2020 The Cytoscape Consortium
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

@SuppressWarnings("rawtypes")
public interface CustomGraphicsManager {
	
	/**
	 * Use this as a property key to override the default behavior of using the factory's supported class name
	 * (see {@link CyCustomGraphicsFactory#getSupportedClass()}) as the key when registering a 
	 * {@link CyCustomGraphicsFactory} through {@link #addCustomGraphicsFactory(CyCustomGraphicsFactory, Map)}.
	 * The property value must also be a String.
	 */
	static final String SUPPORTED_CLASS_ID = "SUPPORTED_CLASS_ID";

	void addCustomGraphicsFactory(CyCustomGraphicsFactory factory, Map<?, ?> props);
	void removeCustomGraphicsFactory(CyCustomGraphicsFactory factory, Map<?, ?> props);
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
	
	boolean isUsedInCurrentSession(CyCustomGraphics graphics);
	void setUsedInCurrentSession(CyCustomGraphics graphics, Boolean isUsed);
	
	void removeAllCustomGraphics();
	void removeCustomGraphics(Long id);
	
	/**
	 * Provides id available ID;
	 * @return 
	 */
	Long getNextAvailableID();
	
	void addMissingImageCustomGraphics(MissingImageCustomGraphics<?> cg);
	Collection<MissingImageCustomGraphics<?>> reloadMissingImageCustomGraphics();
}
