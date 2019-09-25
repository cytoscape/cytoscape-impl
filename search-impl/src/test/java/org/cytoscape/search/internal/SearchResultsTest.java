package org.cytoscape.search.internal;

/*
 * #%L
 * Cytoscape Layout API (layout-api)
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

import java.util.ArrayList;
import static org.junit.Assert.*;
import org.junit.Test;

public class SearchResultsTest {
	
	@Test
	public void testSyntaxErrorNoArgs() {
		SearchResults sr = SearchResults.syntaxError();
		assertEquals(sr.getStatus(), SearchResults.Status.ERROR_SYNTAX);
		assertTrue(sr.isError());
		assertEquals("Cannot execute search query", sr.getMessage());
		assertEquals(0, sr.getEdgeHitCount());
		assertEquals(0, sr.getNodeHitCount());
		assertEquals(0, sr.getNodeHits().size());
		assertEquals(0, sr.getEdgeHits().size());
	}
	
	@Test
	public void testSyntaxErrorWithErrorMessage() {
		SearchResults sr = SearchResults.syntaxError("some error");
		assertEquals(sr.getStatus(), SearchResults.Status.ERROR_SYNTAX);
		assertTrue(sr.isError());
		assertEquals("some error", sr.getMessage());
		assertEquals(0, sr.getEdgeHitCount());
		assertEquals(0, sr.getNodeHitCount());
		assertEquals(0, sr.getNodeHits().size());
		assertEquals(0, sr.getEdgeHits().size());
	}
	
	@Test
	public void testFatalErrorNoArgs() {
		SearchResults sr = SearchResults.fatalError();
		assertEquals(sr.getStatus(), SearchResults.Status.ERROR_FATAL);
		assertTrue(sr.isError());
		assertEquals("Query execution error", sr.getMessage());
		assertEquals(0, sr.getEdgeHitCount());
		assertEquals(0, sr.getNodeHitCount());
		assertEquals(0, sr.getNodeHits().size());
		assertEquals(0, sr.getEdgeHits().size());
	}
	
	@Test
	public void testFatalErrorWithErrorMessage() {
		SearchResults sr = SearchResults.fatalError("some error");
		assertEquals(sr.getStatus(), SearchResults.Status.ERROR_FATAL);
		assertTrue(sr.isError());
		assertEquals("some error", sr.getMessage());
		assertEquals(0, sr.getEdgeHitCount());
		assertEquals(0, sr.getNodeHitCount());
		assertEquals(0, sr.getNodeHits().size());
		assertEquals(0, sr.getEdgeHits().size());
	}
	
	@Test
	public void testResultsNullArgs() {
		SearchResults sr = SearchResults.results(null, null);
		assertEquals(sr.getStatus(), SearchResults.Status.SUCCESS);
		assertFalse(sr.isError());
		assertEquals("Selected 0 nodes and 0 edges", sr.getMessage());
		assertEquals(0, sr.getEdgeHitCount());
		assertEquals(0, sr.getNodeHitCount());
		assertEquals(0, sr.getNodeHits().size());
		assertEquals(0, sr.getEdgeHits().size());
	}
	
	@Test
	public void testResultsOneNodeOneEdge() {
		ArrayList<String> nodes = new ArrayList<String>();
		nodes.add("node1");
		ArrayList<String> edges = new ArrayList<String>();
		edges.add("edge1");
		
		SearchResults sr = SearchResults.results(nodes, edges);
		assertEquals(sr.getStatus(), SearchResults.Status.SUCCESS);
		assertFalse(sr.isError());
		assertEquals("Selected 1 node and 1 edge", sr.getMessage());
		assertEquals(1, sr.getEdgeHitCount());
		assertEquals(1, sr.getNodeHitCount());
		assertEquals(1, sr.getNodeHits().size());
		assertEquals(1, sr.getEdgeHits().size());
	}
	
	@Test
	public void testResultsOneNodeTwoEdges() {
		ArrayList<String> nodes = new ArrayList<String>();
		nodes.add("node1");
		ArrayList<String> edges = new ArrayList<String>();
		edges.add("edge1");
		edges.add("edge2");
		
		SearchResults sr = SearchResults.results(nodes, edges);
		assertEquals(sr.getStatus(), SearchResults.Status.SUCCESS);
		assertFalse(sr.isError());
		assertEquals("Selected 1 node and 2 edges", sr.getMessage());
		assertEquals(2, sr.getEdgeHitCount());
		assertEquals(1, sr.getNodeHitCount());
		assertEquals(1, sr.getNodeHits().size());
		assertEquals(2, sr.getEdgeHits().size());
	}
	
	@Test
	public void testResultsTwoNodesOneEdge() {
		ArrayList<String> nodes = new ArrayList<String>();
		nodes.add("node1");
		nodes.add("node2");
		ArrayList<String> edges = new ArrayList<String>();
		edges.add("edge1");

		SearchResults sr = SearchResults.results(nodes, edges);
		assertEquals(sr.getStatus(), SearchResults.Status.SUCCESS);
		assertFalse(sr.isError());
		assertEquals("Selected 2 nodes and 1 edge", sr.getMessage());
		assertEquals(1, sr.getEdgeHitCount());
		assertEquals(2, sr.getNodeHitCount());
		assertEquals(2, sr.getNodeHits().size());
		assertEquals(1, sr.getEdgeHits().size());
	}
}
