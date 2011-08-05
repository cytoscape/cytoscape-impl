package org.cytoscape.io.webservice.biomart.task;

import java.util.List;
import java.util.Map;

public class LoadRepositoryResult {
	
	private final Map<String, String> datasourceMap;
	private final List<String> dsList;

	public LoadRepositoryResult(final Map<String, String> datasourceMap, final List<String> dsList) {
		this.datasourceMap = datasourceMap;
		this.dsList = dsList;
	}
	
	public Map<String, String> getDatasourceMap() {
		return this.datasourceMap;
	}

	public List<String> getSortedDataSourceList() {
		return this.dsList;
	}
}
