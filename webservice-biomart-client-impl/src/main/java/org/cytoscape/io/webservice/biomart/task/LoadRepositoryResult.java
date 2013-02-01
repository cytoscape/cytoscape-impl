package org.cytoscape.io.webservice.biomart.task;

/*
 * #%L
 * Cytoscape Biomart Webservice Impl (webservice-biomart-client-impl)
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
