package org.cytoscape.cpath2.internal;

/*
 * #%L
 * Cytoscape CPath2 Impl (cpath2-impl)
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

import org.cytoscape.cpath2.internal.schemas.search_response.SearchResponseType;
import org.cytoscape.cpath2.internal.util.NullTaskMonitor;
import org.cytoscape.cpath2.internal.web_service.CPathWebService;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

public class CPathSearchTask implements Task {

	private final CPathWebService client;
	private final String query;
	private final int taxonomyId;

	public CPathSearchTask(String query, CPathWebService client, int taxonomyId) {
		this.query = query;
		this.client = client;
		this.taxonomyId = taxonomyId;
	}
	
    /**
     * NCBI Taxonomy ID Filter.
     */
    public static final String NCBI_TAXONOMY_ID_FILTER = "ncbi_taxonomy_id_filter";

    @Override
	public void run(TaskMonitor taskMonitor) throws Exception {
        SearchResponseType response = client.searchPhysicalEntities(query, taxonomyId,
                new NullTaskMonitor());
        Integer totalNumHits = response.getTotalNumHits().intValue();
	}

	@Override
	public void cancel() {
	}
}
