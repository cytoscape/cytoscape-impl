package org.cytoscape.io.datasource.internal;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;

import org.cytoscape.io.DataCategory;
import org.cytoscape.io.datasource.DataSource;
import org.cytoscape.io.datasource.DataSourceManager;

public final class DataSourceManagerImpl implements DataSourceManager {
	
	
	private final Map<DataCategory, Map<String, DataSource>> dataSourceMap;
	
	DataSourceManagerImpl() {
		this.dataSourceMap = new HashMap<DataCategory, Map<String, DataSource>>();
	}
	
	/**
	 * Will be used by OSGi services
	 * @param datasource
	 */
	public void addDataSource(final DataSource datasource, Map metadata) {
		if(datasource == null)
			return;

		if (dataSourceMap.containsKey(datasource.getDataCategory())){
			Map<String, DataSource> map = dataSourceMap.get(datasource.getDataCategory());
			map.put(datasource.getName(), datasource);
		}
		else {// this DataCategory does not exist yet
			Map<String, DataSource> map = new HashMap<String, DataSource>();
			map.put(datasource.getName(), datasource);
			
			this.dataSourceMap.put(datasource.getDataCategory(), map);
		}		
	}
	
	
	public void removeDataSource(final DataSource datasource, Map metadata) {
		if(datasource == null)
			return;

		Map<String, DataSource> map = this.dataSourceMap.get(datasource.getDataCategory());
		if (map == null){
			return;
		}
		map.remove(datasource.getName());
	}
	

	@Override
	public Collection<DataSource> getDataSources(DataCategory category) {
		return this.dataSourceMap.get(category).values();		
	}

	
	@Override
	public Collection<DataSource> getDataSources(String providerName) {
		
		final Set<DataSource> sources = new HashSet<DataSource>();
		
		Iterator<DataCategory> it = this.dataSourceMap.keySet().iterator();
		Map<String, DataSource> map;
		while (it.hasNext()){
			map = this.dataSourceMap.get(it.next());
			Iterator<DataSource> it_ds = map.values().iterator();
			while (it_ds.hasNext()){
				DataSource ds = it_ds.next(); 
				if(ds.getProvider().equals(providerName))
					sources.add(ds);				
			}
		}		
		
		return sources;
	}

	@Override
	public Collection<DataSource> getAllDataSources() {
		
		final Set<DataSource> sources = new HashSet<DataSource>();
		
		Iterator<DataCategory> it = this.dataSourceMap.keySet().iterator();
		while (it.hasNext()){
			sources.addAll(this.dataSourceMap.get(it.next()).values());
		}
		
		return sources;		
	}

	
	@Override
	public boolean deleteDataSource(DataSource pDataSource){
		Map<String, DataSource> map = this.dataSourceMap.get(pDataSource.getDataCategory());
		if (map == null || map.get(pDataSource.getName()) == null){
			return false;
		}
		map.remove(pDataSource.getName());
		
		return true;
	}

	
	@Override
	public void saveDataSource(DataSource pDataSource){
		Map<String, DataSource> map = this.dataSourceMap.get(pDataSource.getDataCategory());
		if (map == null){
			map = new HashMap<String, DataSource>();
			this.dataSourceMap.put(pDataSource.getDataCategory(), map);
		}
		map.put(pDataSource.getName(), pDataSource);
	}

	
	@Override
	public boolean containsDataSource(DataSource pDataSource){
		Collection<DataSource> dataSourcesSet = this.getDataSources(pDataSource.getDataCategory());
		
		if (dataSourcesSet == null || dataSourcesSet.size() == 0){
			return false;
		}
		
		Iterator<DataSource> it = dataSourcesSet.iterator();
		while (it.hasNext()){
			DataSource ds = it.next();
			if (ds.getName().equalsIgnoreCase(pDataSource.getName())){
				return true;
			}
		}
		
		return false;
	}
}
