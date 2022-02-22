package org.cytoscape.search.internal.index;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.search.internal.progress.ProgressMonitor;

public class TableIndexer {
	
	
	public static void indexTable(IndexWriter writer, CyTable table, TableType type, ProgressMonitor pm) throws IOException {
		var dpm = pm.toDiscrete(table.getRowCount());
		
		CyColumn keyCol = table.getPrimaryKey();
		String keyName = keyCol.getName();
		Class<?> keyType = keyCol.getType();
		
		for(CyRow row : table.getAllRows()) {
			var key = row.get(keyName, keyType);
			Document document = createDocument(table, key, type);
			writer.addDocument(document);
			dpm.increment();
		}
		
		dpm.done();
	}
	
	
	public static void updateRows(IndexWriter writer, CyTable table, Set<? extends Object> keys, TableType type, ProgressMonitor pm) throws IOException {
		var dpm = pm.toDiscrete(table.getRowCount());

		for(var key : keys) {
			var term = new Term(SearchManager.INDEX_FIELD, String.valueOf(key));
			var doc = createDocument(table, key, type);
			
			writer.updateDocument(term, doc);
			
			dpm.increment();
		}
		
		dpm.done();
	}
	
	
	private static Document createDocument(CyTable table, Object key, TableType type) {
		Document doc = new Document();
		String identifier = String.valueOf(key);
		
		doc.add(new StringField(SearchManager.INDEX_FIELD, identifier, Field.Store.YES));
		doc.add(new StringField(SearchManager.TYPE_FIELD, type.name(), Field.Store.YES));
		
		Set<String> attributeNames = CyTableUtil.getColumnNames(table);

		for(String attrName : attributeNames) {
			if(attrName == null)
				continue;
			if(Objects.equals(CyNetwork.SELECTED, attrName))
				continue;
			
			String attrIndexingName = attrName.toLowerCase();
			indexField(doc, table, attrIndexingName, key);
		}
		return doc;
	}
	
	
	private static void indexField(Document doc, CyTable table, String attrName, Object key) {
		CyColumn column = table.getColumn(attrName);
		Class<?> valueType = column.getType();
		CyRow row = table.getRow(key);
		
		if(valueType == String.class) {
			String attrValue = row.get(attrName, String.class);
			if(attrValue != null) {
				doc.add(new TextField(attrName, attrValue, Field.Store.YES));  // MKTODO, make this a NO
			}				
		} else if(valueType == Integer.class) {
			Integer attrValue = row.get(attrName, Integer.class);
			if(attrValue != null) {
				doc.add(new IntPoint(attrName, attrValue));
			}
		} else if(valueType == Long.class) {
			Long attrValue = row.get(attrName, Long.class);
			if(attrValue != null) {
				doc.add(new LongPoint(attrName, attrValue));
			}
		} else if(valueType == Double.class) {	
			Double attrValue = row.get(attrName, Double.class);
			if(attrValue != null) {
				doc.add(new DoublePoint(attrName, attrValue));
			}
		} else if(valueType == Boolean.class) {
			Boolean attrValue = row.get(attrName, Boolean.class);
			if(attrValue != null) {
				doc.add(new StringField(attrName, String.valueOf(attrValue), Field.Store.NO));
			}
		} else if(valueType == List.class) {
			List<?> attrValueList = row.get(attrName, List.class);
			if(attrValueList != null) {
				Class<?> listElementType = column.getListElementType();
				for(Object attrValue : attrValueList) {
					if(attrValue != null) {
						if(listElementType == String.class) {
							doc.add(new TextField(attrName, (String)attrValue, Field.Store.NO));
						} else if (listElementType == Integer.class) {
							doc.add(new IntPoint(attrName, (Integer)attrValue));
						} else if (listElementType == Long.class) {
							doc.add(new LongPoint(attrName, (Long)attrValue));
						} else if (listElementType == Double.class) {	
							doc.add(new DoublePoint(attrName, (Double)attrValue));
						} else if (listElementType == Boolean.class) {
							doc.add(new StringField(attrName, String.valueOf(attrValue), Field.Store.NO));
						}
					}
				}
			}
		}
	}


}
