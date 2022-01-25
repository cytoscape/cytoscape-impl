package org.cytoscape.search.internal.index;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;

public class IndexCreateTask implements Callable<IndexCreateTask.Result> {

	private final Path indexPath;
	private final CyNetwork network;
	
	public IndexCreateTask(Path indexPath, CyNetwork network) {
		this.indexPath = indexPath;
		this.network = network;
	}

	@Override
	public Result call() {
		try {
			System.out.println("Indexing network : " + indexPath);
			Directory dir = FSDirectory.open(indexPath);
			
			Analyzer analyzer = new CaseInsensitiveWhitespaceAnalyzer();
			IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
			iwc.setOpenMode(OpenMode.CREATE);
			IndexWriter writer = new IndexWriter(dir, iwc);
			
			indexNetwork(writer);
			
			writer.close();
			
			System.out.println("Indexing network complete");
			return Result.succeeded();
		} catch (IOException e) {
			return Result.failed(e);
		}
	}
	
	
	private void indexNetwork(IndexWriter writer) throws IOException {
		List<CyNode> nodeList = network.getNodeList();
		List<CyEdge> edgeList = network.getEdgeList();
		
		for(CyNode cyNode : nodeList) {
//			System.out.println("adding node: " + cyNode.getSUID());
			Document document = createDocument(network, cyNode, SearchManager.NODE_TYPE);
			writer.addDocument(document);
		}
	
		for(CyEdge cyEdge : edgeList) {
//			System.out.println("adding edge: " + cyEdge.getSUID());
			Document document = createDocument(network, cyEdge, SearchManager.EDGE_TYPE);
			writer.addDocument(document);
		}
	}
	

	private static Document createDocument(CyNetwork network, CyIdentifiable graphObject, String graphObjectType) {
		Document doc = new Document();
		String identifier = Long.toString(graphObject.getSUID());
		
		doc.add(new StringField(SearchManager.INDEX_FIELD, identifier, Field.Store.YES));
		doc.add(new StringField(SearchManager.TYPE_FIELD, graphObjectType, Field.Store.YES));
		
		CyRow cyRow = network.getRow(graphObject);
		CyTable cyDataTable = cyRow.getTable();
		Set<String> attributeNames = CyTableUtil.getColumnNames(cyDataTable);

		for(String attrName : attributeNames) {
			if(attrName == null)
				continue;
			String attrIndexingName = attrName.toLowerCase();

			indexField(doc, network, cyDataTable, attrIndexingName, graphObject);
		}
		return doc;
	}
	
	
	private static void indexField(Document doc, CyNetwork network, CyTable cyDataTable, String attrName, CyIdentifiable graphObject) {
		CyColumn column = cyDataTable.getColumn(attrName);
		Class<?> valueType = column.getType();
		
		if(valueType == String.class) {
			String attrValue = network.getRow(graphObject).get(attrName, String.class);
			if(attrValue != null) {
				doc.add(new TextField(attrName, attrValue, Field.Store.NO));
			}				
		} else if(valueType == Integer.class) {
			Integer attrValue = network.getRow(graphObject).get(attrName, Integer.class);
			if(attrValue != null) {
				doc.add(new IntPoint(attrName, attrValue));
			}
		} else if(valueType == Long.class) {
			Long attrValue = network.getRow(graphObject).get(attrName, Long.class);
			if(attrValue != null) {
				doc.add(new LongPoint(attrName, attrValue));
			}
		} else if(valueType == Double.class) {	
			Double attrValue = network.getRow(graphObject).get(attrName, Double.class);
			if(attrValue != null) {
				doc.add(new DoublePoint(attrName, attrValue));
			}
		} else if(valueType == Boolean.class) {
			Boolean attrValue = network.getRow(graphObject).get(attrName, Boolean.class);
			if(attrValue != null) {
				doc.add(new StringField(attrName, String.valueOf(attrValue), Field.Store.NO));
			}
		} else if(valueType == List.class) {
			List<?> attrValueList = network.getRow(graphObject).get(attrName, List.class);
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
	
	
	
	public static record Result(boolean sucesss, Throwable error) {
		public static Result succeeded() {
			return new Result(true, null);
		}
		public static Result failed(Throwable error) {
			return new Result(false, error);
		}
	}
	
}
