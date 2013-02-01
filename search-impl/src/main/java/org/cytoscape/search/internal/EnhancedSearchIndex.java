package org.cytoscape.search.internal;

/*
 * #%L
 * Cytoscape Search Impl (search-impl)
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



import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.search.internal.util.EnhancedSearchUtils;
import org.cytoscape.work.TaskMonitor;


public class EnhancedSearchIndex {
	RAMDirectory idx;
	final TaskMonitor taskMonitor;
	// Index the given network
	public EnhancedSearchIndex(final CyNetwork network, final TaskMonitor taskMonitor) {
		if(network == null)
			throw new NullPointerException("Network is null.");
		this.taskMonitor = taskMonitor;
		// Construct a RAMDirectory to hold the in-memory representation of the index.		
		idx = new RAMDirectory();
		BuildIndex(idx, network);
	}

	private void BuildIndex(RAMDirectory idx, CyNetwork network) {

		try {
			// Make a writer to create the index
			IndexWriter writer = new IndexWriter(idx, new StandardAnalyzer(Version.LUCENE_30), 
					IndexWriter.MaxFieldLength.UNLIMITED);

			// Add a document for each graph object - node and edge
			List<CyNode> nodeList = network.getNodeList();
			
			this.taskMonitor.setProgress(0.1);
			for (CyNode cyNode : nodeList) {
				writer.addDocument(createDocument(network, cyNode, EnhancedSearch.NODE_TYPE, cyNode.getSUID()));
			}
			this.taskMonitor.setProgress(0.6);
		
			List<CyEdge> edgeList = network.getEdgeList();
			for (CyEdge cyEdge : edgeList) {
				writer.addDocument(createDocument(network, cyEdge, EnhancedSearch.EDGE_TYPE, cyEdge.getSUID()));
			}

			// Optimize and close the writer to finish building the index
			writer.optimize();
			writer.close();

		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		this.taskMonitor.setProgress(0.95);

	}

	/**
	 * Make a Document object with an un-indexed identifier field and indexed
	 * attribute fields
	 */
	private static Document createDocument(CyNetwork network, CyIdentifiable graphObject, String graphObjectType, long index) {
		Document doc = new Document();
		String identifier = Long.toString(index);
		
		doc.add(new Field(EnhancedSearch.INDEX_FIELD, identifier, Field.Store.YES, Field.Index.ANALYZED));
		doc.add(new Field(EnhancedSearch.TYPE_FIELD, graphObjectType, Field.Store.YES, Field.Index.ANALYZED));
		
		CyRow cyRow = network.getRow(graphObject);
		CyTable cyDataTable = cyRow.getTable();
		Set<String> attributeNames = CyTableUtil.getColumnNames(cyDataTable);

		for (final String attrName : attributeNames) {
			// Handle whitespace characters and case in attribute names
			String attrIndexingName = EnhancedSearchUtils.replaceWhitespace(attrName);
			attrIndexingName = attrIndexingName.toLowerCase();

			// Determine type
			Class<?> valueType = cyDataTable.getColumn(attrName).getType();
			
			if (valueType == String.class) {
				String attrValue = network.getRow(graphObject).get(attrName, String.class);
				if (attrValue == null){
					continue;
				}				
				doc.add(new Field(attrIndexingName, attrValue, Field.Store.YES, Field.Index.ANALYZED));
			} else if (valueType == Integer.class) {
				if (network.getRow(graphObject).get(attrName, Integer.class) == null){
					continue;
				}

				int attrValue = network.getRow(graphObject).get(attrName, Integer.class);

				NumericField field = new NumericField(attrIndexingName);
				field.setIntValue(attrValue);
				doc.add(field);
			} else if (valueType == Double.class) {	
				if (network.getRow(graphObject).get(attrName, Double.class) == null){
					continue;
				}
				
				double attrValue = network.getRow(graphObject).get(attrName, Double.class);
				
				NumericField field = new NumericField(attrIndexingName);
				field.setDoubleValue(attrValue);
				doc.add(field);
			} else if (valueType == Boolean.class) {
				if (network.getRow(graphObject).get(attrName, Boolean.class) != null){
					String attrValue = network.getRow(graphObject).get(attrName, Boolean.class).toString();
					doc.add(new Field(attrIndexingName, attrValue, Field.Store.YES, Field.Index.ANALYZED));					
				}
			} else if (valueType == List.class) {
				List attrValueList = network.getRow(graphObject).get(attrName, List.class);
				if (attrValueList != null) {
					for (int j = 0; j < attrValueList.size(); j++) {
						String attrValue = attrValueList.get(j).toString();
						doc.add(new Field(attrIndexingName, attrValue,
								  Field.Store.YES,
								  Field.Index.ANALYZED));
					}
				}
			}
		}

		return doc;
	}

	public RAMDirectory getIndex() {
		return idx;
	}
}
