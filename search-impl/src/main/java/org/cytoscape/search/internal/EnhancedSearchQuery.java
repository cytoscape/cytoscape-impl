/*
 Copyright (c) 2006, 2007, 2011, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.search.internal;


import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JOptionPane;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.util.Version;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.search.internal.util.AttributeFields;
import org.cytoscape.search.internal.util.CustomMultiFieldQueryParser;
import org.cytoscape.search.internal.util.EnhancedSearchUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EnhancedSearchQuery {
	
	private static final Logger logger = LoggerFactory.getLogger(EnhancedSearchQuery.class);
	
	private final RAMDirectory idx;
	private final CyNetwork network;
	private IdentifiersCollector hitCollector = null;
	private Searcher searcher = null;

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
	private void search(final String queryString, final AttributeFields attFields)
		throws IOException
	{
		// Build a Query object.
		// CustomMultiFieldQueryParser is used to support range queries on numerical attribute fields.
		final CustomMultiFieldQueryParser queryParser =
			new CustomMultiFieldQueryParser(attFields, new StandardAnalyzer(Version.LUCENE_30));

		try {
			// Execute query
			Query query = queryParser.parse(queryString);
			hitCollector = new IdentifiersCollector(searcher);
			searcher.search(query, hitCollector);		    
		} catch (final ParseException pe) {
			// Parse exceptions occur when colon appear in the query in an
			// unexpected location, e.g. when attribute or value are
			// missing in the query. In such case, the hitCollector
			// variable will be null.
			JOptionPane.showMessageDialog(null, pe.getMessage(),
						      "Invalid query!",
						      JOptionPane.ERROR_MESSAGE);
		} catch (final Exception e) {
			// Other types of exception may occur
			JOptionPane.showMessageDialog(null, e.getMessage(),
						      "Query execution error!",
						      JOptionPane.ERROR_MESSAGE);
		}			
	}

	// hitCollector object may be null if this method is called before
	// executeQuery
	public int getNodeHitCount() {
		if (hitCollector != null) {
			return hitCollector.getNodeHitCount();
		} else {
			return 0;
		}
	}
	public int getEdgeHitCount() {
		if (hitCollector != null) {
			return hitCollector.getEdgeHitCount();
		} else {
			return 0;
		}
	}

	// hitCollector object may be null if this method is called before
	// ExecuteQuery
	public ArrayList<String> getNodeHits() {
		if (hitCollector != null) {
			return hitCollector.getNodeHits();
		} else {
			return null;
		}
	}
	public ArrayList<String> getEdgeHits() {
		if (hitCollector != null) {
			return hitCollector.getEdgeHits();
		} else {
			return null;
		}
	}

}


class IdentifiersCollector extends Collector {

	private Searcher searcher;

	public ArrayList<String> nodeHitsIdentifiers = new ArrayList<String>();
	public ArrayList<String> edgeHitsIdentifiers = new ArrayList<String>();

	public IdentifiersCollector(Searcher searcher) {
		this.searcher = searcher;
	}
	
	public int getNodeHitCount() {
		return nodeHitsIdentifiers.size();
	}
	public int getEdgeHitCount() {
		return edgeHitsIdentifiers.size();
	}

	public ArrayList<String> getNodeHits() {
		return nodeHitsIdentifiers;
	}
	public ArrayList<String> getEdgeHits() {
		return edgeHitsIdentifiers;
	}

	public boolean acceptsDocsOutOfOrder() {
		return true;
	}

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

	public void setNextReader(IndexReader reader, int docBase) {
	}

	public void setScorer(Scorer scorer) {
	}

}
