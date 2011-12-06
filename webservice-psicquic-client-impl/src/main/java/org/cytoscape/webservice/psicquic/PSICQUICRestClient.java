package org.cytoscape.webservice.psicquic;

import java.io.InputStream;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.webservice.psicquic.mapper.CyNetworkBuilder;
import org.cytoscape.webservice.psicquic.simpleclient.PSICQUICSimpleClient;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Light-weight REST client based on SimpleClient by EBI team.
 * 
 */
public class PSICQUICRestClient {
	private static final Logger logger = LoggerFactory.getLogger(PSICQUICRestClient.class);

	public enum SearchMode {
		MIQL("MIQL Mode"), INTERACTOR("Search by Interactor Names");

		private final String name;

		private SearchMode(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}

	private static final Long ERROR_CODE = -1l;

	// Timeout for search
	private static final long SEARCH_TIMEOUT = 20000;
	private static final long IMPORT_TIMEOUT = 1200000;

	private final CyNetworkFactory factory;
	private final CyNetworkViewFactory viewFactory;
	private final CyNetworkViewManager viewManager;

	public PSICQUICRestClient(final CyNetworkFactory factory, final CyNetworkViewFactory viewFactory,
			final CyNetworkViewManager viewManager) {
		this.factory = factory;
		this.viewFactory = viewFactory;
		this.viewManager = viewManager;
	}

	public Map<String, CyNetwork> importNetwork(final String query, final Collection<String> targetServices,
			final SearchMode mode, final TaskMonitor tm) throws InterruptedException {
		final Map<String, CyNetwork> resultMap = new ConcurrentHashMap<String, CyNetwork>();
		final ExecutorService exe = Executors.newCachedThreadPool();
		final long startTime = System.currentTimeMillis();

		// Submit the query for each active service
		final List<ImportNetworkTask> tasks = new ArrayList<ImportNetworkTask>();
		for (final String serviceURL : targetServices)
			tasks.add(new ImportNetworkTask(serviceURL, query, mode));

		final List<Future<CyNetwork>> futures = exe.invokeAll(tasks, IMPORT_TIMEOUT, TimeUnit.MILLISECONDS);
		logger.debug("Task submitted!");

		double completed = 0.0d;
		final double increment = 1.0d / (double) futures.size();
		final Iterator<ImportNetworkTask> taskItr = tasks.iterator();
		for (final Future<CyNetwork> future : futures) {
			final ImportNetworkTask task = taskItr.next();
			final String source = task.getURL();
			try {
				final CyNetwork network = future.get();
				//createView(network);
				resultMap.put(source, network);
				logger.debug(source + " : Got response = " + resultMap.get(source));
			} catch (ExecutionException e) {
				logger.warn("Error occured in network import from: " + source, e);
				continue;
			} catch (CancellationException ce) {
				logger.warn("Import operation timeout for " + source, ce);
				continue;
			}
			completed += increment;
			tm.setProgress(completed);
		}
		long endTime = System.currentTimeMillis();
		double sec = (endTime - startTime) / (1000.0);
		logger.info("PSICQUIC import finished in " + sec + " sec.");
		
		tm.setProgress(1.0d);

		return resultMap;
	}

	private void createView(final CyNetwork network) {
		final CyNetworkView view = viewFactory.createNetworkView(network);
		viewManager.addNetworkView(view);
		//view.fitContent();
	}

	public Map<String, Long> search(final String query, final Collection<String> targetServices, final SearchMode mode,
			final TaskMonitor tm) throws InterruptedException {
		final Map<String, Long> resultMap = new ConcurrentHashMap<String, Long>();

		final ExecutorService exe = Executors.newCachedThreadPool();
		final long startTime = System.currentTimeMillis();

		// Submit the query for each active service
		final List<SearchTask> tasks = new ArrayList<SearchTask>();
		for (final String serviceURL : targetServices)
			tasks.add(new SearchTask(serviceURL, query, mode));

		final List<Future<Long>> futures = exe.invokeAll(tasks, SEARCH_TIMEOUT, TimeUnit.MILLISECONDS);
		logger.debug("Task submitted!");

		final Iterator<SearchTask> taskItr = tasks.iterator();

		double completed = 0.0d;
		final double increment = 1.0d / (double) futures.size();
		for (final Future<Long> future : futures) {
			final SearchTask task = taskItr.next();
			final String source = task.getURL();
			try {
				resultMap.put(source, future.get());
				logger.debug(source + " : Got response = " + resultMap.get(source));
			} catch (ExecutionException e) {
				logger.warn("Error occured in search: " + source, e);
				resultMap.put(source, ERROR_CODE);
				continue;
			} catch (CancellationException ce) {
				logger.warn("Operation timeout for " + source, ce);
				continue;
			}
			completed += increment;
			tm.setProgress(completed);
		}
		long endTime = System.currentTimeMillis();
		double sec = (endTime - startTime) / (1000.0);
		logger.info("PSICQUIC DB search finished in " + sec + " sec.");

		tm.setProgress(1.0d);
		
		return resultMap;
	}

	/**
	 * Search each data source and return aggregated result.
	 * 
	 */
	private final class SearchTask implements Callable<Long> {
		private final String serviceURL;
		private final String query;
		private final SearchMode mode;

		private SearchTask(final String serviceURL, final String query, final SearchMode mode) {
			this.serviceURL = serviceURL;
			this.query = query;
			this.mode = mode;
		}

		public Long call() throws Exception {
			final PSICQUICSimpleClient simpleClient = new PSICQUICSimpleClient(serviceURL);
			if (mode == SearchMode.INTERACTOR)
				return simpleClient.countByInteractor(query);
			else
				return simpleClient.countByQuery(query);
		}

		String getURL() {
			return serviceURL;
		}
	}

	private final class ImportNetworkTask implements Callable<CyNetwork> {
		private final String serviceURL;
		private final String query;
		private final SearchMode mode;

		private ImportNetworkTask(final String serviceURL, final String query, final SearchMode mode) {
			this.serviceURL = serviceURL;
			this.query = query;
			this.mode = mode;
		}

		public CyNetwork call() throws Exception {
			final PSICQUICSimpleClient simpleClient = new PSICQUICSimpleClient(serviceURL);
			InputStream is = null;
			if (mode == SearchMode.INTERACTOR)
				is = simpleClient.getByInteraction(query);
			else if (mode == SearchMode.MIQL)
				is = simpleClient.getByQuery(query);

			final CyNetworkBuilder networkBuilder = new CyNetworkBuilder(factory);
			final CyNetwork network = networkBuilder.buildNetwork(is);
			is.close();
			is = null;

			return network;
		}

		String getURL() {
			return serviceURL;
		}
	}

}
