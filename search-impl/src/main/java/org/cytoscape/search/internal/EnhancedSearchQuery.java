package org.cytoscape.search.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.RAMDirectory;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.search.internal.util.AttributeFields;
import org.cytoscape.search.internal.util.CaseInsensitiveWhitespaceAnalyzer;
import org.cytoscape.search.internal.util.CustomMultiFieldQueryParser;
import org.cytoscape.search.internal.util.EnhancedSearchUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Search Impl (search-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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

public class EnhancedSearchQuery {
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	private final RAMDirectory idx;
	private final CyNetwork network;
	private IdentifiersCollector hitCollector;
	private Searcher searcher;

	public EnhancedSearchQuery(CyNetwork network, RAMDirectory index) {
		this.network = network;
		this.idx = index;
	}

	public void executeQuery(String queryString) {
		try {
			logger.debug("Query start for: " + queryString);
			
			// Define attribute fields in which the search is to be carried on
			AttributeFields attFields = new AttributeFields(network);

			// Build an IndexSearcher using the in-memory index
			searcher = new IndexSearcher(idx);
			queryString = EnhancedSearchUtils.queryToLowerCase(queryString);
			
			search(queryString, attFields);
			
			searcher.close();

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	/**
	 * Searches for the given query string. By default (without specifying
	 * attributeName), search is carried out on all attribute fields. This
	 * functionality is enabled with the use of MultiFieldQueryParser.
	 */
	private void search(final String queryString, final AttributeFields attFields) throws IOException {
		// Build a Query object.
		// CustomMultiFieldQueryParser is used to support range queries on numerical attribute fields.
		QueryParser queryParser = new CustomMultiFieldQueryParser(attFields, new CaseInsensitiveWhitespaceAnalyzer());

		try {
			// Execute query
			Query query = queryParser.parse(queryString);
			hitCollector = new IdentifiersCollector(searcher);
			searcher.search(query, hitCollector);		    
		} catch (final ParseException pe) {
			// Parse exceptions occur when colon appear in the query in an
			// unexpected location, e.g. when attribute or value are
			// missing in the query. In such case, the hitCollector variable will be null.
			SwingUtilities.invokeLater(() ->
				JOptionPane.showMessageDialog(null, pe.getMessage(), "Invalid query.", JOptionPane.ERROR_MESSAGE)
			);
		} catch (final Exception e) {
			// Other types of exception may occur
			SwingUtilities.invokeLater(() ->
				JOptionPane.showMessageDialog(null, e.getMessage(), "Query execution error.", JOptionPane.ERROR_MESSAGE)
			);
		}			
	}

	// hitCollector object may be null if this method is called before
	// executeQuery
	public int getNodeHitCount() {
		return hitCollector == null ? 0 : hitCollector.getNodeHitCount();
	}
	public int getEdgeHitCount() {
		return hitCollector == null ? 0 : hitCollector.getEdgeHitCount();
	}

	// hitCollector object may be null if this method is called before ExecuteQuery
	public List<String> getNodeHits() {
		return hitCollector == null ? null : hitCollector.getNodeHits();
	}
	
	public List<String> getEdgeHits() {
		return hitCollector == null ? null : hitCollector.getEdgeHits();
	}
}

class IdentifiersCollector extends Collector {

	private Searcher searcher;

	public List<String> nodeHitsIdentifiers = new ArrayList<>();
	public List<String> edgeHitsIdentifiers = new ArrayList<>();

	public IdentifiersCollector(Searcher searcher) {
		this.searcher = searcher;
	}
	
	public int getNodeHitCount() {
		return nodeHitsIdentifiers.size();
	}
	public int getEdgeHitCount() {
		return edgeHitsIdentifiers.size();
	}

	public List<String> getNodeHits() {
		return nodeHitsIdentifiers;
	}
	public List<String> getEdgeHits() {
		return edgeHitsIdentifiers;
	}

	@Override
	public boolean acceptsDocsOutOfOrder() {
		return true;
	}

	@Override
	public void collect(int id) {		
		try {
			Document doc = searcher.doc(id);
			String currID = doc.get(EnhancedSearch.INDEX_FIELD);
			String currType = doc.get(EnhancedSearch.TYPE_FIELD);
			
			if (currType.equalsIgnoreCase(EnhancedSearch.NODE_TYPE)) {
				nodeHitsIdentifiers.add(currID);
			} else if (currType.equalsIgnoreCase(EnhancedSearch.EDGE_TYPE)) {
				edgeHitsIdentifiers.add(currID);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	@Override
	public void setNextReader(IndexReader reader, int docBase) {
	}

	@Override
	public void setScorer(Scorer scorer) {
	}
}
