package org.cytoscape.search.internal.index;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiBits;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.util.Bits;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.RowsCreatedEvent;
import org.cytoscape.model.events.RowsCreatedListener;
import org.cytoscape.model.events.RowsDeletedEvent;
import org.cytoscape.model.events.RowsDeletedListener;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.model.events.TableAboutToBeDeletedEvent;
import org.cytoscape.model.events.TableAboutToBeDeletedListener;
import org.cytoscape.model.events.TableAddedEvent;
import org.cytoscape.model.events.TableAddedListener;
import org.cytoscape.search.internal.progress.ProgressMonitor;
import org.cytoscape.search.internal.search.AttributeFields;
import org.cytoscape.search.internal.ui.SearchBox;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchManager implements 
	TableAddedListener, TableAboutToBeDeletedListener,
	ColumnCreatedListener, ColumnDeletedListener, 
	RowsSetListener, RowsCreatedListener, RowsDeletedListener {

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	public static final String INDEX_FIELD = "CY_SEARCH2_INDEX";
	public static final String TYPE_FIELD  = "CY_SEARCH2_TYPE";
	
	
	private final CyServiceRegistrar registrar;
	
	private final Path baseDir;
	private final ExecutorService executorService;
	
	private SearchBox searchBox;
	
	private final Map<Long,Index> tableIndexMap = new ConcurrentHashMap<>();
	
	
	public SearchManager(CyServiceRegistrar registrar, Path baseDir) {
		this.registrar = registrar;
		this.baseDir = Objects.requireNonNull(baseDir);
		
		// TODO this can be a thread pool, IndexWriters are thread safe
		// TODO Test if using multiple threads for indexing actually speeds anything up.
		// Indexing might be IO bound so using multiple threads might not actually be worth the added complexity.
		// Use a single thread for now.
		this.executorService = Executors.newSingleThreadExecutor(r -> {
			Thread thread = Executors.defaultThreadFactory().newThread(r);
			thread.setName("search2-" + thread.getName());
			return thread;
		});
	}
	
	
	public void setSearchBox(SearchBox searchBox) {
		this.searchBox = searchBox;
	}
	
	private Path getIndexPath(CyIdentifiable ele) {
		return baseDir.resolve("index_" + ele.getSUID());
	}
	
	public QueryParser getQueryParser(CyNetwork network) {
		Analyzer analyser = new CaseInsensitiveWhitespaceAnalyzer();
		AttributeFields fields = new AttributeFields(network);
		QueryParser parser = new MultiFieldQueryParser(fields.getFields(), analyser);
		return parser;
	}
	
	public IndexReader getIndexReader(CyTable table) throws IOException {
		return tableIndexMap.get(table.getSUID()).getIndexReader();
	}
	
	private ProgressMonitor getProgressMonitor(CyTable table, boolean update) {
		if(searchBox == null)
			return ProgressMonitor.nullMonitor();
		
		Long suid = table.getSUID();
		String name = table.getTitle(); // MKTODO Use the network title if its a network table.
		if(update)
			name = name + " (update)";
		
		return searchBox.getProgressPopup().addProgress(suid, name);
	}
	
	
	private TableType getTableType(CyTable table) {
		var networkTableManager = registrar.getService(CyNetworkTableManager.class);
		var network = networkTableManager.getNetworkForTable(table);
		
		if(network == null) {
			// Don't index unassigned public tables yet... 
//			if(table.isPublic()) {
//				return TableType.UNASSIGNED;
//			}
		} else if(network.getDefaultNodeTable().equals(table)) {
			return TableType.NODE;
		} else if(network.getDefaultEdgeTable().equals(table)) {
			return TableType.EDGE;
		}
		
		return null;
	}
	
	
	@Override
	public void handleEvent(TableAddedEvent e) {
		var table = e.getTable();
		var type = getTableType(table);
		if(type == null)
			return;  // Don't index network tables that are not shown in the table viewer.
		
		addTable(table, type);
	}
	
	public Future<?> addTable(CyTable table, TableType type) {
		System.out.println("SearchManager.addTable '" + table.getTitle() + "'");
		var pm = getProgressMonitor(table, false);
		
		Path path = getIndexPath(table);
		Index index = new Index(table.getSUID(), path);
		
		return executorService.submit(() -> {
			Long suid = table.getSUID();
			if(tableIndexMap.containsKey(suid)) // This shouldn't happen, just being defensive
				return;
			
			logger.info("Indexing table: " + suid);
			try {
				var writer = index.getWriter();
				TableIndexer.indexTable(writer, table, type, pm);
				writer.commit();
			} catch(IOException e) {
				logger.error("Error indexing table: " + suid, e); // TODO handle exception
			} finally {
				pm.done();
			}
			
			tableIndexMap.put(suid, index);
			logger.info("Indexing table complete: " + suid);
		});
	}
	
	
	@Override
	public void handleEvent(TableAboutToBeDeletedEvent e) {
		System.out.println("SearchManager.handleEvent(TableAboutToBeDeletedEvent) " + e.getTable().getTitle());
		var table = e.getTable();
		removeTable(table);
	}
	
	public Future<?> removeTable(CyTable table) {
		// MKTODO what happens if the indexer is still running???
		Long suid = table.getSUID();
		return executorService.submit(() -> {
			Index index = tableIndexMap.remove(suid);
			if(index != null) {
				logger.info("deleting network index: " + suid);
				try {
					var writer = index.getWriter();
					writer.deleteAll();
					writer.commit();
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	
	@Override
	public void handleEvent(RowsSetEvent e) {
		var cols = e.getColumns();
		if(cols.size() == 1 && cols.contains(CyNetwork.SELECTED))
			return;
		
		CyTable table = e.getSource();
		
		var type = getTableType(table);
		if(type == null)
			return;  // Don't index network tables that are not shown in the table viewer.
		
		CyColumn keyCol = table.getPrimaryKey();
		String keyName = keyCol.getName();
		Class<?> keyType = keyCol.getType();
		
		Set<Object> keys = new HashSet<>();
		
		for(var rowSetRecord : e.getPayloadCollection()) {
			var column = rowSetRecord.getColumn();
			if(!CyNetwork.SELECTED.equals(column)) {
				var key = rowSetRecord.getRow().get(keyName, keyType);
				keys.add(key);
			}
		}
		
		updateRows(table, keys, type);
	}
	
	public Future<?> updateRows(CyTable table, Set<? extends Object> keys, TableType type) {
		Long suid = table.getSUID();
		var pm = getProgressMonitor(table, true);
		
		return executorService.submit(() -> {
			Index index = tableIndexMap.get(suid);
			if(index != null) {
				try {
					var writer = index.getWriter();
					TableIndexer.updateRows(writer, table, keys, type, pm);
					writer.commit();
				} catch(IOException e) {
					logger.error("Error indexing table: " + suid, e); // TODO handle exception
				} finally {
					pm.done();
				}
			}
		});
	}
	
	
	
	@Override
	public void handleEvent(ColumnCreatedEvent e) {
		CyTable table = e.getSource();
		String colName = e.getColumnName();
	}
	
//	public Future<?> addColumn(CyTable table, String colName) {
//		Long suid = table.getSUID();
//		return executorService.submit(() -> {
//			IndexWriter indexWriter = tableIndexMap.get(suid);
//			if(indexWriter != null) {
//				
//			}
//		});
//	}
	
	
	
	@Override
	public void handleEvent(ColumnDeletedEvent e) {
		// TODO Auto-generated method stub
	}


	

	@Override
	public void handleEvent(RowsDeletedEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void handleEvent(RowsCreatedEvent e) {
		// TODO Auto-generated method stub
	}


	
	public void printIndex(CyTable table) throws IOException {
		Index index = tableIndexMap.get(table.getSUID());
		try(var indexReader = index.getIndexReader()) {
			System.out.println("All Documents in Lucene Index");
			Bits liveDocs = MultiBits.getLiveDocs(indexReader);
			for (int i = 0; i < indexReader.maxDoc(); i++) {
				if (liveDocs != null && !liveDocs.get(i))
					continue;

				Document doc = indexReader.document(i);
				System.out.print(doc.get(INDEX_FIELD) + " - ");
				boolean first = true;
				for(var field : doc.getFields()) {
					if(!first)
						System.out.print(", ");
					System.out.print(field.name() + ":" + doc.get(field.name()));
					first = false;
				}
				System.out.println();
			}

			System.out.println();
		}
	}
	
	
}
