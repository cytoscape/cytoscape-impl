package org.cytoscape.io.internal.write;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.write.CyWriterFactory;
import org.cytoscape.io.write.CyWriterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AbstractWriterManager<T extends CyWriterFactory>  implements CyWriterManager<T> {
	protected final DataCategory category; 
	protected final Map<CyFileFilter,T> factories;
	private static final Logger logger = LoggerFactory.getLogger( AbstractWriterManager.class ); 

	public AbstractWriterManager(DataCategory category) {
		this.category = category;
		factories = new HashMap<CyFileFilter,T>();
	}

	public List<CyFileFilter> getAvailableWriterFilters() {
		return new ArrayList<CyFileFilter>( factories.keySet() );
	}
	
	@SuppressWarnings("unchecked")
	public void addCyWriterFactory(T factory, Map props) {
		if ( factory != null && factory.getFileFilter().getDataCategory() == category ) {
			logger.info("adding IO taskFactory ");
			factories.put(factory.getFileFilter(), factory);
		} else
			logger.warn("Specified factory is null or has wrong DataCategory (" + category + ")");
	}

	@SuppressWarnings("unchecked")
	public void removeCyWriterFactory(T factory, Map props) {
		factories.remove(factory.getFileFilter());
	}

	public T getMatchingFactory(CyFileFilter filter) {
		for (T factory : factories.values()) {
			CyFileFilter cff = factory.getFileFilter();
			if ( filter.equals(cff) ) {
				logger.debug("found factory for file filter: " + filter.toString());
				return factory;
			}
		}
		logger.warn("Couldn't find matching factory for filter: " + filter.toString());

		return null;
	}
}
