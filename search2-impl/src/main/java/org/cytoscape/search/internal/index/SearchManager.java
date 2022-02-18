package org.cytoscape.search.internal.index;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.cytoscape.application.CyUserLog;
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
import org.cytoscape.search.internal.progress.DiscreteProgressMonitor;
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
	
	private final Map<Long,IndexWriter> tableIndexMap = new ConcurrentHashMap<>();
	
	
	public SearchManager(CyServiceRegistrar registrar, Path baseDir) {
		this.registrar = registrar;
		this.baseDir = Objects.requireNonNull(baseDir);
		
		// TODO this can be a thread pool, IndexWriters are thread safe
		this.executorService = Executors.newCachedThreadPool(r -> {
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
	
	private IndexWriterConfig getIndexWriterConfig(OpenMode openMode) {
		Analyzer analyzer = new CaseInsensitiveWhitespaceAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		iwc.setOpenMode(openMode);
		return iwc;
	}
	
	public QueryParser getQueryParser(CyNetwork network) {
		Analyzer analyser = new CaseInsensitiveWhitespaceAnalyzer();
		AttributeFields fields = new AttributeFields(network);
		QueryParser parser = new MultiFieldQueryParser(fields.getFields(), analyser);
		return parser;
	}
	
	public IndexWriter createIndexWriter(CyTable table, ProgressMonitor pm) throws IOException {
		DiscreteProgressMonitor dpm = pm.toDiscrete(3);
		
		Path indexPath = getIndexPath(table);
		dpm.increment();
		
		Directory dir = FSDirectory.open(indexPath);
		dpm.increment();
		
		IndexWriterConfig iwc = getIndexWriterConfig(OpenMode.CREATE);
		IndexWriter writer = new IndexWriter(dir, iwc);
		dpm.done();
		
		return writer;
	}
	
	public IndexReader getIndexReader(CyTable table) throws IOException {
		IndexWriter writer = tableIndexMap.get(table.getSUID());
		if(writer == null)
			return null;
		Directory directory = writer.getDirectory();
		IndexReader reader = DirectoryReader.open(directory);
		return reader;
	}
	
	
	private ProgressMonitor getProgressMonitor(CyTable table) {
		if(searchBox == null)
			return ProgressMonitor.nullMonitor();
		
		Long suid = table.getSUID();
		String name = table.getTitle(); // MKTODO Use the network title if its a network table.
		
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
		var objectType = getTableType(table);
		if(objectType == null)
			return;  // Don't index network tables that are not shown in the table viewer.
		
		addTable(table, objectType);
	}
	
	public Future<?> addTable(CyTable table, TableType type) {
		System.out.println("SearchManager.addTable " + table.getTitle());
		var pm = getProgressMonitor(table);
		
		return executorService.submit(() -> {
			Long suid = table.getSUID();
			if(tableIndexMap.containsKey(suid)) // This shouldn't happen, just being defensive
				return;
			
			logger.info("Indexing table: " + suid);
			try {
				var subPms = pm.split(1, 10);
				
				IndexWriter writer = createIndexWriter(table, subPms[0]);
				TableIndexer.indexTable(table, writer, type, subPms[1]);
				
				writer.close();
				
				tableIndexMap.put(suid, writer);
			} catch(IOException e) {
				logger.error("Error indexing table: " + suid, e); // TODO handle exception
			} finally {
				pm.done();
			}
			
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
			IndexWriter indexWriter = tableIndexMap.remove(suid);
			if(indexWriter != null) {
				logger.info("deleting network index: " + suid);
				try {
					indexWriter.deleteAll();
					indexWriter.commit();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	
	@Override
	public void handleEvent(ColumnCreatedEvent e) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void handleEvent(ColumnDeletedEvent e) {
		// TODO Auto-generated method stub
	}


	@Override
	public void handleEvent(RowsSetEvent e) {
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


}
