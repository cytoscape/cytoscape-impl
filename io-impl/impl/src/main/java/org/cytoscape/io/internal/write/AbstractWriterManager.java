package org.cytoscape.io.internal.write;


import java.io.OutputStream;
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


public class AbstractWriterManager<T extends CyWriterFactory>  implements CyWriterManager {
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
		if ( factory != null && factory.getCyFileFilter().getDataCategory() == category ) {
			logger.info("adding IO taskFactory ");
			factories.put(factory.getCyFileFilter(), factory);
		} else
			logger.warn("Specified factory is null or has wrong DataCategory (" + category + ")");
	}

	@SuppressWarnings("unchecked")
	public void removeCyWriterFactory(T factory, Map props) {
		factories.remove(factory.getCyFileFilter());
	}

	public T getMatchingFactory(CyFileFilter filter, OutputStream stream) {
		for (T factory : factories.values()) {
			CyFileFilter cff = factory.getCyFileFilter();
			if ( filter.equals(cff) ) {
				logger.debug("found factory for file filter: " + filter.toString());
				factory.setOutputStream(stream);
				return factory;
			}
		}
		logger.warn("Couldn't find matching factory for filter: " + filter.toString());

		return null;
	}
}
