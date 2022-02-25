package org.cytoscape.search.internal.index;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiBits;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.util.Bits;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.event.DebounceTimer;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.ColumnNameChangedEvent;
import org.cytoscape.model.events.ColumnNameChangedListener;
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
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.search.internal.progress.CompositeProgressMonitor;
import org.cytoscape.search.internal.progress.ProgressMonitor;
import org.cytoscape.search.internal.progress.ProgressViewer;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchManager implements 
	TableAddedListener, TableAboutToBeDeletedListener, 
	ColumnDeletedListener, ColumnNameChangedListener, ColumnCreatedListener,
	RowsSetListener, RowsCreatedListener, RowsDeletedListener {

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	public static final String INDEX_FIELD = "CY_SEARCH2_INDEX";
	public static final String TYPE_FIELD  = "CY_SEARCH2_TYPE";
	
	
	private final CyServiceRegistrar registrar;
	
	private final Path baseDir;
	private final ExecutorService executorService;
	
	private final List<ProgressViewer> progressViewers = new ArrayList<>();
	
	private final Map<Long,Index> tableIndexMap = new ConcurrentHashMap<>();
	private final DebounceTimer columnChangeDebounceTimer = new DebounceTimer();
	
	
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
	
	
	public void addProgressViewer(ProgressViewer viewer) {
		this.progressViewers.add(viewer);
	}
	
	private Path getIndexPath(CyIdentifiable ele) {
		return baseDir.resolve("index_" + ele.getSUID());
	}
	
	public QueryParser getQueryParser(CyTable table) {
		var index = tableIndexMap.get(table.getSUID());
		if(index != null) {
			return index.getQueryParser(table);
		}
		return null;
	}
	
	public IndexReader getIndexReader(CyTable table) throws IOException {
		return tableIndexMap.get(table.getSUID()).getIndexReader();
	}
	
	private ProgressMonitor getProgressMonitor(CyTable table, String suffix) {
		if(progressViewers.isEmpty())
			return ProgressMonitor.nullMonitor();
		
		String name = (suffix == null) 
			? table.getTitle() 
			: table.getTitle() + " (" + suffix + ")";
		
		if(progressViewers.size() == 1)
			return progressViewers.get(0).addProgress(name);
		
		var monitors = progressViewers.stream().map(pv -> pv.addProgress(name)).collect(toList());
		return new CompositeProgressMonitor(monitors);
	}
	
	
	private TableType getTableType(CyTable table) {
		var networkTableManager = registrar.getService(CyNetworkTableManager.class);
		var network = networkTableManager.getNetworkForTable(table);
		
		if(network == null) {
			// Don't index unassigned public tables yet... 
//			if(table.isPublic()) {
//				return TableType.UNASSIGNED;
//			}
			return null;
		}
		if(network instanceof CyRootNetwork) {
			return null;
		}
		if(network.getDefaultNodeTable().equals(table)) {
			return TableType.NODE;
		}
		if(network.getDefaultEdgeTable().equals(table)) {
			return TableType.EDGE;
		}
		return null;
	}
	
	private boolean isIndexable(CyTable table) {
		return getTableType(table) != null;
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
		var pm = getProgressMonitor(table, null);
		Path path = getIndexPath(table);
		Index index = new Index(table.getSUID(), type, path);
		
		return executorService.submit(() -> {
			Long suid = table.getSUID();
			if(tableIndexMap.containsKey(suid)) // This shouldn't happen, just being defensive
				return;
			
			try {
				TableIndexer.indexTable(index, table, pm);
			} catch(IOException e) {
				logger.error("Error indexing table: " + suid, e); // TODO handle exception
			} finally {
				pm.done();
			}
			
			tableIndexMap.put(suid, index);
		});
	}
	
	
	@Override
	public void handleEvent(TableAboutToBeDeletedEvent e) {
		var table = e.getTable();
		removeTable(table);
	}
	
	public Future<?> removeTable(CyTable table) {
		Long suid = table.getSUID();
		return executorService.submit(() -> {
			Index index = tableIndexMap.remove(suid);
			if(index != null) {
				try {
					var writer = index.getWriter();
					writer.deleteAll();
					writer.commit();
					writer.close();
				} catch (IOException e) {
					logger.error("Error deleting table index: " + suid, e); // TODO handle exception
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
		if(!isIndexable(table))
			return;
		
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
		
		updateRows(table, keys);
	}
	
	@Override
	public void handleEvent(RowsDeletedEvent e) {
		CyTable table = e.getSource();
		if(!isIndexable(table))
			return;
		
		var keys = e.getKeys();
		updateRows(table, keys);
	}
	
	@Override
	public void handleEvent(RowsCreatedEvent e) {
		CyTable table = e.getSource();
		if(!isIndexable(table))
			return;
		
		var keys = e.getPayloadCollection();
		updateRows(table, keys);
	}
	
	public Future<?> updateRows(CyTable table, Collection<? extends Object> keys) {
		Long suid = table.getSUID();
		var pm = getProgressMonitor(table, "update rows");
		
		return executorService.submit(() -> {
			try {
				Index index = tableIndexMap.get(suid);
				if(index != null) {
					TableIndexer.updateRows(index, table, keys, pm);
				}
			} catch(IOException e) {
				logger.error("Error indexing table: " + suid, e); // TODO handle exception
			} finally {
				pm.done();
			}
		});
	}
	
	
	@Override
	public void handleEvent(ColumnCreatedEvent e) {
//		var table = e.getSource();
//		if(isIndexable(table)) {
//			columnChangeDebounceTimer.debounce(() -> reindexTable(table));
//		}
	}
	
	@Override
	public void handleEvent(ColumnDeletedEvent e) {
		var table = e.getSource();
		if(isIndexable(table)) {
			columnChangeDebounceTimer.debounce(() -> reindexTable(table));
		}
	}

	@Override
	public void handleEvent(ColumnNameChangedEvent e) {
		var table = e.getSource();
		if(isIndexable(table)) {
			columnChangeDebounceTimer.debounce(() -> reindexTable(table));
		}
	}
	
	public Future<?> reindexTable(CyTable table) {
		Long suid = table.getSUID();
		var pm = getProgressMonitor(table, "reindex table");
		
		return executorService.submit(() -> {
			try {
				Index index = tableIndexMap.get(suid);
				if(index != null) {
					var subPms = pm.split(1, 10);
					
					var writer = index.getWriter();
					writer.deleteAll();
					subPms[0].done();
					
					TableIndexer.indexTable(index, table, subPms[1]);
					writer.commit();
					subPms[1].done();
				}
			} catch (IOException e) {
				logger.error("Error indexing table: " + suid, e);
			} finally {
				pm.done();
			}
		});
	}
	
	
	
	/**
	 * For debugging purposes.
	 */
	protected int getDocumentCount(CyTable table) throws IOException {
		Index index = tableIndexMap.get(table.getSUID());
		try(var indexReader = index.getIndexReader()) {
			return indexReader.numDocs();
		}
	}
	
	/**
	 * For debugging purposes.
	 */
	protected void printIndex(CyTable table) throws IOException {
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
