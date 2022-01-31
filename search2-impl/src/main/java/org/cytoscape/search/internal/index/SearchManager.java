package org.cytoscape.search.internal.index;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.NetworkDestroyedEvent;
import org.cytoscape.model.events.NetworkDestroyedListener;

public class SearchManager implements NetworkAddedListener, NetworkDestroyedListener {

	public static final String INDEX_FIELD = "ESP_INDEX";
	public static final String TYPE_FIELD = "ESP_TYPE";
	public static final String NODE_TYPE = "node";
	public static final String EDGE_TYPE = "edge";
	
	private final Path baseDir;
	private final ExecutorService executorService;
	
	
	public SearchManager(Path baseDir) {
		this.baseDir = Objects.requireNonNull(baseDir);
		
		this.executorService = Executors.newSingleThreadExecutor(r -> {
			Thread thread = Executors.defaultThreadFactory().newThread(r);
			thread.setName("search2-" + thread.getName());
			return thread;
		});
	}
	
	public Path getIndexPath(CyNetwork network) {
		return baseDir.resolve("index_" + network.getSUID());
	}
	
	public Directory getDirectory(CyNetwork network) throws IOException {
		Path path = getIndexPath(network);
		Directory directory = FSDirectory.open(path);
		return directory;
	}
	
	
	@Override
	public void handleEvent(NetworkAddedEvent e) {
		System.out.println("SearchManager.handleEvent(NetworkAddedEvent)");
		
		CyNetwork network = e.getNetwork();
		Path indexPath = getIndexPath(network);
		
		executorService.submit(new IndexCreateTask(indexPath, network));
	}
	
	
	@Override
	public void handleEvent(NetworkDestroyedEvent e) {
		System.out.println("SearchManager.handleEvent(NetworkDestroyedEvent)");
		
		
	}

	

}