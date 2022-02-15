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
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.search.internal.search.AttributeFields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchManager implements NetworkAddedListener, NetworkAboutToBeDestroyedListener {

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	public static final String INDEX_FIELD = "ESP_INDEX";
	public static final String TYPE_FIELD = "ESP_TYPE";
	public static final String NODE_TYPE = "node";
	public static final String EDGE_TYPE = "edge";
	
	private final Path baseDir;
	private final ExecutorService executorService;
	
	private final Map<Long,IndexWriter> networkIndexMap = new ConcurrentHashMap<>();
	
	
	public SearchManager(Path baseDir) {
		this.baseDir = Objects.requireNonNull(baseDir);
		
		// TODO this can be a thread pool, IndexWriters are thread safe
		this.executorService = Executors.newCachedThreadPool(r -> {
			Thread thread = Executors.defaultThreadFactory().newThread(r);
			thread.setName("search2-" + thread.getName());
			return thread;
		});
	}
	
	private Path getIndexPath(CyNetwork network) {
		return baseDir.resolve("index_" + network.getSUID());
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
	
	public IndexWriter createIndexWriter(CyNetwork network) throws IOException {
		Path indexPath = getIndexPath(network);
		Directory dir = FSDirectory.open(indexPath);
		IndexWriterConfig iwc = getIndexWriterConfig(OpenMode.CREATE);
		IndexWriter writer = new IndexWriter(dir, iwc);
		return writer;
	}
	
	public IndexReader getIndexReader(CyNetwork network) throws IOException {
		IndexWriter writer = networkIndexMap.get(network.getSUID());
		if(writer == null)
			return null;
		Directory directory = writer.getDirectory();
		IndexReader reader = DirectoryReader.open(directory);
		return reader;
	}
	
	
	public Future<?> addNetwork(CyNetwork network) {
		return executorService.submit(() -> {
			Long suid = network.getSUID();
			if(networkIndexMap.containsKey(suid)) // This shouldn't happen, just being defensive
				return;
			logger.info("indexing network: " + network.getSUID());
			try {
				IndexWriter writer = createIndexWriter(network);
				NetworkIndexer.indexNetwork(network, writer);
				writer.close();
				networkIndexMap.put(suid, writer);
			} catch(IOException e) {
				// TODO handle exception
				logger.error("error indexing network: " + suid, e);
			}
			logger.info("indexing network complete: " + network.getSUID());
		});
	}
	
	
	public Future<?> removeNetwork(CyNetwork network) {
		return executorService.submit(() -> {
			Long suid = network.getSUID();
			IndexWriter indexWriter = networkIndexMap.remove(suid);
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
	public void handleEvent(NetworkAddedEvent event) {
		addNetwork(event.getNetwork());
	}
	
	@Override
	public void handleEvent(NetworkAboutToBeDestroyedEvent event) {
		removeNetwork(event.getNetwork());
	}


}
