package org.cytoscape.datasource.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.io.DataCategory;
import org.cytoscape.io.datasource.DataSource;
import org.cytoscape.io.datasource.DataSourceManager;

public final class DataSourceManagerImpl implements DataSourceManager {
	
	
	private final Map<String, DataSource> dataSourceMap;
	
	DataSourceManagerImpl() {
		this.dataSourceMap = new HashMap<String, DataSource>();
	}
	
	/**
	 * Will be used by OSGi services
	 * @param datasource
	 */
	public void addDataSource(final DataSource datasource, Map metadata) {
		if(datasource == null)
			return;

		dataSourceMap.put(datasource.getName(), datasource);
	}
	
	public void removeDataSource(final DataSource datasource, Map metadata) {
		if(datasource == null)
			return;

		dataSourceMap.remove(datasource.getName());
	}
	

	@Override
	public Collection<DataSource> getDataSources(DataCategory category) {
		final Set<DataSource> sources = new HashSet<DataSource>();
		for(DataSource source: dataSourceMap.values()) {
			if(source.getDataCategory() == category)
				sources.add(source);
		}
		
		return sources;
	}

	@Override
	public Collection<DataSource> getDataSources(String providerName) {
		final Set<DataSource> sources = new HashSet<DataSource>();
		for(DataSource source: dataSourceMap.values()) {
			if(source.getProvider().equals(providerName))
				sources.add(source);
		}
		
		return sources;
	}

	@Override
	public Collection<DataSource> getAllDataSources() {
		return dataSourceMap.values();
	}

}
