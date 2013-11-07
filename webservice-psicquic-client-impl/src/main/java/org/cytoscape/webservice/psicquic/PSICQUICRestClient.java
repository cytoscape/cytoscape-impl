package org.cytoscape.webservice.psicquic;

/*
 * #%L
 * Cytoscape PSIQUIC Web Service Impl (webservice-psicquic-client-impl)
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.webservice.psicquic.mapper.CyNetworkBuilder;
import org.cytoscape.webservice.psicquic.simpleclient.PSICQUICSimpleClient;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import uk.ac.ebi.enfin.mi.cluster.InteractionCluster;

/**
 * Light-weight REST client based on SimpleClient by EBI team.
 * 
 */
public final class PSICQUICRestClient {

	private static final Logger logger = LoggerFactory.getLogger(PSICQUICRestClient.class);

	// Static list of ID. The oreder is important!
	private static final String MAPPING_NAMES = "uniprotkb,chebi,ddbj/embl/genbank,ensembl,irefindex";

	public enum SearchMode {
		MIQL("Search by Query (MIQL)"), INTERACTOR("Search by gene/protein ID list"), SPECIES("Search by species");

		private final String name;

		private SearchMode(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}

	public static final Long ERROR_SEARCH_FAILED = -1l;
	public static final Long ERROR_TIMEOUT = -2l;
	public static final Long ERROR_CANCEL = -3l;

	// Timeout for search. TODO: Make public as property.
	private static final long SEARCH_TIMEOUT_MSEC = 1200;

	// Timeout for import. TODO: Make public as property.
	private static final long IMPORT_TIMEOUT = 1000;

	private final CyNetworkFactory factory;
	private final RegistryManager regManager;
	private final CyNetworkBuilder builder;

	private static volatile boolean canceled = false;

	public PSICQUICRestClient(final CyNetworkFactory factory, final RegistryManager regManager,
			final CyNetworkBuilder builder) {
		this.factory = factory;
		this.regManager = regManager;
		this.builder = builder;
	}

	/**
	 * Create one merged network from multiple data sources. This merge will be
	 * done by MiCluster.
	 * 
	 * TODO: Merge should be replaced by advanced network merge.
	 * 
	 * @param query
	 * @param targetServices
	 * @param mode
	 * @param tm
	 * @return
	 * @throws IOException
	 */
	public CyNetwork importMergedNetwork(final String query, final Collection<String> targetServices,
			final SearchMode mode, final TaskMonitor tm) throws IOException {

		final InteractionCluster importedCluster = importMerged(query, targetServices, mode, tm);
		final CyNetwork network = builder.buildNetwork(importedCluster);

		tm.setProgress(1.0d);
		return network;
	}

	/**
	 * Create collection of networks from the returned MITAB data.
	 * 
	 * @param query
	 * @param targetServices
	 * @param mode
	 * @param tm
	 * @return
	 * @throws IOException
	 */
	public Collection<CyNetwork> importNetworks(final String query, final Collection<String> targetServices,
			final SearchMode mode, final TaskMonitor tm) throws IOException {

		final Map<String, CyNetwork> result = importNetworksParallel(query, targetServices, mode, tm);
		final Set<CyNetwork> networks = new HashSet<CyNetwork>(result.values());
		tm.setProgress(1.0d);
		return networks;
	}

	public InteractionCluster importNeighbours(final String query, final Collection<String> targetServices,
			final SearchMode mode, final TaskMonitor tm) throws IOException {
		return importMerged(query, targetServices, mode, tm);
	}

	private final InteractionCluster importMerged(final String query, final Collection<String> targetServices,
			final SearchMode mode, final TaskMonitor tm) {

		final Map<String, Collection<BinaryInteraction>> result = importNetwork(query, targetServices, mode, tm);
		final Collection<Collection<BinaryInteraction>> binaryInteractions = result.values();
		final List<BinaryInteraction> allInteractions = new ArrayList<BinaryInteraction>();
		for (Collection<BinaryInteraction> interactions : binaryInteractions)
			allInteractions.addAll(interactions);

		tm.setStatusMessage("Merging results...");
		InteractionCluster iC = new InteractionCluster();
		iC.setBinaryInteractionIterator(allInteractions.iterator());
		iC.setMappingIdDbNames(MAPPING_NAMES);
		iC.runService();

		return iC;
	}

	private Map<String, CyNetwork> importNetworksParallel(String query, Collection<String> targetServices,
			SearchMode mode, TaskMonitor tm) {

		final Map<String, CyNetwork> result = new ConcurrentHashMap<String, CyNetwork>();
		canceled = false;

		tm.setTitle("Loading network data from Remote PSICQUIC Services");

		Map<String, CyNetwork> resultMap = new ConcurrentHashMap<String, CyNetwork>();
		final ExecutorService exe = Executors.newCachedThreadPool();
		final CompletionService<CyNetwork> completionService = new ExecutorCompletionService<CyNetwork>(exe);

		final long startTime = System.currentTimeMillis();
		double completed = 0.0d;
		final double increment = 1.0d / (double) targetServices.size();

		final SortedSet<String> sourceSet = new TreeSet<String>();
		final SortedSet<String> nameSet = new TreeSet<String>();
		final Set<ImportNetworkTask> taskSet = new HashSet<ImportNetworkTask>();
		for (final String serviceURL : targetServices) {
			final String networkTitle = regManager.getSource2NameMap().get(serviceURL);
			nameSet.add(networkTitle);
			final ImportNetworkTask task = new ImportNetworkTask(networkTitle, serviceURL, query, mode, factory);
			completionService.submit(task);
			taskSet.add(task);
			sourceSet.add(serviceURL);
		}

		int i = 0;
		for (final String service : targetServices) {
			// Cancel operation
			if (canceled) {
				logger.warn("Interrupted by user: network import task");
				tm.setTitle("Import Canceled");
				tm.setStatusMessage("Canceled: Partial result will be returned.");
				try {
					exe.shutdownNow();
					long endTime = System.currentTimeMillis();
					double sec = (endTime - startTime) / (1000.0);
					logger.info("PSICUQIC Import terminated by user in " + sec + " sec.");
				} catch (Exception ex) {
					logger.warn("Operation timeout", ex);
					return null;
				} finally {
					resultMap.clear();
					taskSet.clear();
					sourceSet.clear();
				}
				return result;
			}

			Future<CyNetwork> future = null;
			try {
				future = completionService.take();
				final CyNetwork ret = future.get();
				String sourceName = null;
				if (ret != null) {
					sourceName = ret.getRow(ret).get("source", String.class);
					result.put(sourceName, ret);
				}

				// Update status message
				completed = completed + increment;
				tm.setProgress(completed);
				nameSet.remove(sourceName);

				final StringBuilder builder = new StringBuilder();
				for(final String name: nameSet) {
					builder.append("<li>" + name + "</li>");
				}
				
				tm.setStatusMessage("<html><body style=\"line-height:150%\">" + 
						(i + 1) + " / " + targetServices.size() + " tasks finished.<br><br>"
						+ "Waiting results from the following databases:<br><ul>" + builder.toString() + "</ul></body></html>");

			} catch (InterruptedException ie) {
				for (final ImportNetworkTask t : taskSet)
					t.cancel();

				taskSet.clear();

				List<Runnable> tasks = exe.shutdownNow();
				logger.warn("Interrupted: network import.  Remaining = " + tasks.size(), ie);
				resultMap.clear();
				resultMap = null;
				return null;
			} catch (ExecutionException e) {
				logger.warn("Error occured in network import", e);
				continue;
			}

			i++;
		}

		try {
			exe.shutdown();
			exe.awaitTermination(IMPORT_TIMEOUT, TimeUnit.SECONDS);

			long endTime = System.currentTimeMillis();
			double sec = (endTime - startTime) / (1000.0);
			logger.info("PSICUQIC Import Finished in " + sec + " sec.");
		} catch (Exception ex) {
			logger.warn("Import operation timeout", ex);
			return null;
		} finally {
			taskSet.clear();
			sourceSet.clear();
		}

		return result;
	}

	private Map<String, Collection<BinaryInteraction>> importNetwork(final String query,
			final Collection<String> targetServices, final SearchMode mode, final TaskMonitor tm) {
		final Map<String, Collection<BinaryInteraction>> result = new HashMap<String, Collection<BinaryInteraction>>();

		canceled = false;

		tm.setTitle("Loading network data from Remote PSICQUIC Services");

		Map<String, CyNetwork> resultMap = new ConcurrentHashMap<String, CyNetwork>();
		final ExecutorService exe = Executors.newCachedThreadPool();
		final CompletionService<Collection<BinaryInteraction>> completionService = new ExecutorCompletionService<Collection<BinaryInteraction>>(
				exe);

		final long startTime = System.currentTimeMillis();

		// Submit the query for each active service
		double completed = 0.0d;
		final double increment = 1.0d / (double) targetServices.size();

		final SortedSet<String> sourceSet = new TreeSet<String>();
		final SortedSet<String> nameSet = new TreeSet<String>();
		final Set<ImportNetworkAsMitabTask> taskSet = new HashSet<ImportNetworkAsMitabTask>();
		for (final String serviceURL : targetServices) {
			nameSet.add(regManager.getSource2NameMap().get(serviceURL));
			final ImportNetworkAsMitabTask task = new ImportNetworkAsMitabTask(serviceURL, query, mode);
			completionService.submit(task);
			taskSet.add(task);
			sourceSet.add(serviceURL);
		}

		int i = 0;
		for (final String service : targetServices) {
			if (canceled) {
				logger.warn("Interrupted by user: network import task");
				exe.shutdownNow();
				resultMap.clear();
				resultMap = null;

				return null;
			}

			Future<Collection<BinaryInteraction>> future = null;
			try {
				future = completionService.take();
				final Collection<BinaryInteraction> ret = future.get();
				if (ret != null) {
					result.put(service, ret);
					// binaryInteractions.addAll(ret);
				}

				completed = completed + increment;
				tm.setProgress(completed);
				nameSet.remove(regManager.getSource2NameMap().get(service));
				// final StringBuilder sBuilder = new StringBuilder();
				// for (String sourceStr : sourceSet) {
				// sBuilder.append(regManager.getSource2NameMap().get(sourceStr)
				// + " ");
				// }

				tm.setStatusMessage((i + 1) + " / " + targetServices.size() + " tasks finished.\n"
						+ "Still waiting responses from the following databases:\n\n" + nameSet.toString());

			} catch (InterruptedException ie) {
				for (final ImportNetworkAsMitabTask t : taskSet)
					t.cancel();

				taskSet.clear();

				List<Runnable> tasks = exe.shutdownNow();
				logger.warn("Interrupted: network import.  Remaining = " + tasks.size(), ie);
				resultMap.clear();
				resultMap = null;
				return null;
			} catch (ExecutionException e) {
				logger.warn("Error occured in network import", e);
				continue;
			}

			i++;
		}

		try {
			exe.shutdown();
			exe.awaitTermination(IMPORT_TIMEOUT, TimeUnit.SECONDS);

			long endTime = System.currentTimeMillis();
			double sec = (endTime - startTime) / (1000.0);
			logger.info("PSICUQIC Import Finished in " + sec + " sec.");
		} catch (Exception ex) {
			logger.warn("Import operation timeout", ex);
			return null;
		} finally {
			taskSet.clear();
			sourceSet.clear();
		}

		return result;
	}

	/**
	 * Search databases in parallel.
	 * 
	 * @param query
	 * @param targetServices
	 * @param mode
	 * @param tm
	 * @return
	 */
	public Map<String, Long> search(final String query, final Collection<String> targetServices, final SearchMode mode,
			final TaskMonitor tm) {

		canceled = false;

		Map<String, Long> resultMap = new ConcurrentHashMap<String, Long>();

		final ExecutorService exe = Executors.newCachedThreadPool();
		final CompletionService<SearchResult> completionService = new ExecutorCompletionService<SearchResult>(exe);

		final long startTime = System.currentTimeMillis();

		// Submit the query for each active service
		for (final String serviceURL : targetServices)
			completionService.submit(new SearchTask(serviceURL, query, mode));

		double completed = 0.0d;
		final double increment = 1.0d / (double) targetServices.size();
		Future<SearchResult> future = null;
		
		int compCount = 0;
		final int total = targetServices.size();
		
		final Set<String> remaining = new HashSet<String>(targetServices);
		
		try {
			while((future = completionService.poll(SEARCH_TIMEOUT_MSEC, TimeUnit.MILLISECONDS)) != null){
				if (canceled) {
					logger.warn("Search canceled by user.");
					tm.setTitle("Search Canceled");
					for(final String timeout: remaining) {
						resultMap.put(timeout, ERROR_SEARCH_FAILED);
					}
					return resultMap;
				}

				try {
					final SearchResult result = future.get();
					resultMap.put(result.getUrl(), result.getRecordCount());
					completed += increment;
					tm.setProgress(completed);
					compCount++;
					tm.setStatusMessage("Completed: " + compCount + "/" + total);
					remaining.remove(result.getUrl());
				} catch (ExecutionException e) {
					logger.warn("Error occured in search: ", e);
					continue;
				} catch (InterruptedException e) {
					logger.warn("Search canceled: ", e);
					continue;
				}
			}
		} catch (InterruptedException e) {
			exe.shutdownNow();
			logger.warn("Interrupted", e);
			for(final String timeout: remaining) {
				resultMap.put(timeout, ERROR_SEARCH_FAILED);
			}
			return resultMap;
		}
		tm.setProgress(1.0d);

		long endTime = System.currentTimeMillis();
		double sec = (endTime - startTime) / (1000.0);
		logger.info("PSICQUIC DB search finished in " + sec + " sec.");
		
		for(final String timeout: remaining) {
			resultMap.put(timeout, ERROR_TIMEOUT);
		}

		exe.shutdown();
		return resultMap;
	}

	private static final class SearchResult {
		private final String url;
		private final Long recordCount;

		public SearchResult(final String url, final Long recordCount) {
			this.recordCount = recordCount;
			this.url = url;
		}

		public final Long getRecordCount() {
			return this.recordCount;
		}

		public final String getUrl() {
			return this.url;
		}
	}

	/**
	 * Search each data source and return aggregated result.
	 * 
	 */
	private static final class SearchTask implements Callable<SearchResult> {
		private final String serviceURL;
		private final String query;
		private final SearchMode mode;

		private SearchTask(final String serviceURL, final String query, final SearchMode mode) {
			this.serviceURL = serviceURL;
			this.query = query;
			this.mode = mode;
		}

		public SearchResult call() throws Exception {
			final PSICQUICSimpleClient simpleClient = new PSICQUICSimpleClient(serviceURL);
			final SearchResult result;
			if (mode == SearchMode.INTERACTOR) {
				result = new SearchResult(serviceURL, simpleClient.countByInteractor(query));
			} else {
				result = new SearchResult(serviceURL, simpleClient.countByQuery(query));
			}
			return result;
		}
	}

	private static final class ImportNetworkAsMitabTask implements Callable<Collection<BinaryInteraction>> {
		private final String serviceURL;
		private final String query;
		private final SearchMode mode;

		private ImportNetworkAsMitabTask(final String serviceURL, final String query, final SearchMode mode) {
			this.serviceURL = serviceURL;
			this.query = query;
			this.mode = mode;
		}

		@Override
		public Collection<BinaryInteraction> call() throws Exception {

			String encodedStr = URLEncoder.encode(query, "UTF-8");
			encodedStr = encodedStr.replaceAll("\\+", "%20");

			URL queryURL = null;
			if (mode == SearchMode.INTERACTOR) {
				// Query is list of interactors.
				queryURL = new URL(serviceURL + "interactor/" + encodedStr);
			} else if (mode == SearchMode.MIQL) {
				queryURL = new URL(serviceURL + "query/" + encodedStr);
			}

			if (queryURL == null)
				throw new IllegalArgumentException("Could not create query URL.");

			logger.info("Query URL: " + queryURL);

			final PsimiTabReader mitabReader = new PsimiTabReader(false);
			return mitabReader.read(queryURL);
		}

		public void cancel() {
			canceled = true;
		}
	}

	private static final class ImportNetworkTask implements Callable<CyNetwork> {

		private final String serviceURL;
		private final String query;
		private final SearchMode mode;
		private final CyNetworkBuilder builder;
		private final String networkTitle;

		// The network created from the result.
		private CyNetwork network = null;

		private ImportNetworkTask(final String networkTitle, final String serviceURL, final String query,
				final SearchMode mode, final CyNetworkFactory factory) {
			this.serviceURL = serviceURL;
			this.query = query;
			this.mode = mode;
			this.networkTitle = networkTitle;
			this.builder = new CyNetworkBuilder(factory);
		}

		@Override
		public CyNetwork call() throws Exception {
			String encodedStr = URLEncoder.encode(query, "UTF-8");
			encodedStr = encodedStr.replaceAll("\\+", "%20");

			URL queryURL = null;
			String queryString = "";
			if (mode == SearchMode.INTERACTOR) {
				queryString = serviceURL + "interactor/" + encodedStr + "?format=tab27";
			} else if (mode == SearchMode.MIQL) {
				queryString = serviceURL + "query/" + encodedStr + "?format=tab27";
			}
			queryURL = new URL(queryString);

			InputStream strm = null;
			try {
				strm = queryURL.openStream();
			} catch (Exception ex) {
				logger.warn("MITAB 2.7 is not supported by: " + networkTitle);
				queryString = queryString.split("\\?")[0];
				queryURL = new URL(queryString);
				strm = queryURL.openStream();
			}

			final BufferedReader reader = new BufferedReader(new InputStreamReader(strm));
			CyNetwork network = builder.buildNetwork(reader, networkTitle);
			return network;
		}

		public void cancel() {
			canceled = true;
			network.getRow(network).set(CyNetwork.NAME, networkTitle);
		}
	}

	public void cancel() {
		canceled = true;
	}
}