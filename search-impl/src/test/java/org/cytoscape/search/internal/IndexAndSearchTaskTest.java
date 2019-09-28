package org.cytoscape.search.internal;

/*
 * #%L
 * Cytoscape Search Impl (search-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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

import static org.junit.Assert.*;
import org.junit.Test;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.lucene.store.RAMDirectory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;

public class IndexAndSearchTaskTest {

	@Test
	public void testConstructorWithNullNetwork() {
		try {
			IndexAndSearchTask task = new IndexAndSearchTask(null,
					mock(EnhancedSearch.class), "query", mock(ViewUpdator.class), mock(NodeAndEdgeSelector.class),
					mock(EnhancedSearchQueryFactory.class),
					mock(EnhancedSearchIndexFactory.class));
			fail("Expected NullPointerException");
		} catch(NullPointerException npe) {
			assertEquals("CyNetwork is null", npe.getMessage());
		}
	}
	
	@Test
	public void testRunWhereTaskIsCancelledImmediately() {
		
		IndexAndSearchTask task = new IndexAndSearchTask(mock(CyNetwork.class),
				mock(EnhancedSearch.class), "query", mock(ViewUpdator.class), mock(NodeAndEdgeSelector.class),
				mock(EnhancedSearchQueryFactory.class),
				mock(EnhancedSearchIndexFactory.class));
		task.cancel();
		TaskMonitor tMonitor = mock(TaskMonitor.class);
		task.run(tMonitor);
		verify(tMonitor).setTitle("Searching the network");
		
	}
	
	@Test
	public void testRunWhereQueryExceedsMaxCharLength() {
		char[] array = new char[IndexAndSearchTask.MAX_QUERY_LEN + 1];
		Arrays.fill(array, 'x');
		IndexAndSearchTask task = new IndexAndSearchTask(mock(CyNetwork.class),
				mock(EnhancedSearch.class), new String(array), mock(ViewUpdator.class), mock(NodeAndEdgeSelector.class),
				mock(EnhancedSearchQueryFactory.class),
				mock(EnhancedSearchIndexFactory.class));
		TaskMonitor tMonitor = mock(TaskMonitor.class);
		task.run(tMonitor);
		SearchResults sr = task.getResults(SearchResults.class);
		assertTrue(sr.isError());
		assertEquals("At " + Integer.toString(array.length) + " characters query string is too large",
				sr.getMessage());
	}
	
	@Test
	public void testRunWhereQueryFails() throws Exception {
		CyNetwork network = mock(CyNetwork.class);
		EnhancedSearch es = mock(EnhancedSearch.class);
		when(es.getNetworkIndexStatus(network)).thenReturn(EnhancedSearch.Status.INDEX_SET);
		RAMDirectory rd = new RAMDirectory();
		when(es.getNetworkIndex(network)).thenReturn(rd);
		EnhancedSearchQueryFactory mockQueryFac = mock(EnhancedSearchQueryFactory.class);
		EnhancedSearchQuery mockQuery = mock(EnhancedSearchQuery.class);
		when(mockQuery.call()).thenReturn(SearchResults.fatalError("some error"));
		when(mockQueryFac.getEnhancedSearchQuery(network, rd, "query")).thenReturn(mockQuery);
		IndexAndSearchTask task = new IndexAndSearchTask(network, es, "query", mock(ViewUpdator.class), mock(NodeAndEdgeSelector.class),
				mockQueryFac,
				mock(EnhancedSearchIndexFactory.class));
		TaskMonitor tMonitor = mock(TaskMonitor.class);

		task.run(tMonitor);

		SearchResults sr = task.getResults(SearchResults.class);
		assertEquals(SearchResults.Status.ERROR_FATAL, sr.getStatus());
		assertEquals("some error", sr.getMessage());

	}
	
	@Test
	public void testRunWithBuildIndexWhereIndexingFails() throws Exception {
		CyNetwork network = mock(CyNetwork.class);
		EnhancedSearch es = mock(EnhancedSearch.class);
		when(es.getNetworkIndexStatus(network)).thenReturn(null);
		TaskMonitor tMonitor = mock(TaskMonitor.class);
		EnhancedSearchIndexFactory mockIndexFac = mock(EnhancedSearchIndexFactory.class);
		EnhancedSearchIndex mockIndex = mock(EnhancedSearchIndex.class);
		when(mockIndex.call()).thenReturn(null);
		when(mockIndexFac.getEnhancedSearchIndex(network, tMonitor)).thenReturn(mockIndex);
		
		EnhancedSearchQueryFactory mockQueryFac = mock(EnhancedSearchQueryFactory.class);
		
		IndexAndSearchTask task = new IndexAndSearchTask(network, es, "query", mock(ViewUpdator.class), mock(NodeAndEdgeSelector.class),
				mockQueryFac,
				mockIndexFac);
		
		task.run(tMonitor);

		SearchResults sr = task.getResults(SearchResults.class);
		assertTrue(sr.isError());
		assertEquals("Error building index", sr.getMessage());
	}
	
	@Test
	public void testRunBuildIndexWhereQuerySucceeds() throws Exception {
		CyNetwork network = mock(CyNetwork.class);
		EnhancedSearch es = mock(EnhancedSearch.class);
		when(es.getNetworkIndexStatus(network)).thenReturn(null);
		RAMDirectory rd = new RAMDirectory();
		EnhancedSearchPlugin.attributeChanged = true;
		TaskMonitor tMonitor = mock(TaskMonitor.class);
		EnhancedSearchIndexFactory mockIndexFac = mock(EnhancedSearchIndexFactory.class);
		EnhancedSearchIndex mockIndex = mock(EnhancedSearchIndex.class);
		when(mockIndex.call()).thenReturn(rd);
		when(mockIndexFac.getEnhancedSearchIndex(network, tMonitor)).thenReturn(mockIndex);
		
		EnhancedSearchQueryFactory mockQueryFac = mock(EnhancedSearchQueryFactory.class);
		EnhancedSearchQuery mockQuery = mock(EnhancedSearchQuery.class);
		when(mockQuery.call()).thenReturn(SearchResults.results(new ArrayList<String>(), new ArrayList<String>()));
		when(mockQueryFac.getEnhancedSearchQuery(network, rd, "query")).thenReturn(mockQuery);
		IndexAndSearchTask task = new IndexAndSearchTask(network, es, "query", mock(ViewUpdator.class), mock(NodeAndEdgeSelector.class),
				mockQueryFac,
				mockIndexFac);
		
		task.run(tMonitor);
		
		SearchResults sr = task.getResults(SearchResults.class);
		assertFalse(sr.isError());
		assertEquals("Selected 0 nodes and 0 edges", sr.getMessage());
		assertFalse(EnhancedSearchPlugin.attributeChanged);
	}
}
