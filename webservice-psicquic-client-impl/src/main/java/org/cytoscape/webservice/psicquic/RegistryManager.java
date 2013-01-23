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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Simple REST client for PSICQUIC registry service.
 * 
 */
public class RegistryManager {

	private static final Logger logger = LoggerFactory.getLogger(RegistryManager.class);

	// Tag definitions
	private static final String REST_URL = "restUrl";
	private static final String TAG = "tag";
	private static final String COUNT = "count";
	private static final String IS_ACTIVE = "active";

	private static final String DEF_SERVICE_URL = "http://www.ebi.ac.uk/Tools/webservices/psicquic/registry/registry";

	// Defines action command
	public enum STATE {
		ACTIVE, INACTIVE, STATUS;
	}

	private final String serviceURLString;

	private final Map<String, String> activeServiceMap;
	private final Map<String, String> inactiveServiceMap;
	private final Map<String, String> source2NameMap;
	private final SortedSet<String> allServiceNames;

	private final Map<String, Boolean> statusMap;
	private final Map<String, String> urlMap;
	private final Map<String, List<String>> tagMap;
	private final Map<String, Long> countMap;

	private boolean isInitialized;
	
	/**
	 * Constructor to use default registry location.
	 */
	public RegistryManager() {
		this(DEF_SERVICE_URL);
	}

	/**
	 * Use custom registry location.
	 * 
	 * @param regLocaiton
	 *            URL of the registry
	 */
	public RegistryManager(String regLocaiton) {
		if (regLocaiton == null || regLocaiton.trim().length() == 0)
			serviceURLString = DEF_SERVICE_URL;
		else
			serviceURLString = regLocaiton;

		activeServiceMap = new HashMap<String, String>();
		inactiveServiceMap = new HashMap<String, String>();
		source2NameMap = new HashMap<String, String>();
		statusMap = new HashMap<String, Boolean>();
		urlMap = new HashMap<String, String>();
		tagMap = new HashMap<String, List<String>>();
		countMap = new HashMap<String, Long>();
		allServiceNames = new TreeSet<String>();
	}
	
	private void ensureInitialized() {
		synchronized (this) {
			if (isInitialized) {
				return;
			}

			ExecutorService executor = Executors.newCachedThreadPool();
			executor.execute(new Runnable() {
				@Override
				public void run() {
					invoke();
					allServiceNames.addAll(activeServiceMap.keySet());
					allServiceNames.addAll(inactiveServiceMap.keySet());
				}
			});
			try {
				executor.shutdown();
				executor.awaitTermination(60, TimeUnit.SECONDS);
				isInitialized = true;
			} catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
		}
	}
	
	public void refresh() {
		activeServiceMap.clear();
		inactiveServiceMap.clear();
		source2NameMap.clear();
		statusMap.clear();
		urlMap.clear();
		tagMap.clear();
		countMap.clear();

		invoke();

		allServiceNames.clear();
		allServiceNames.addAll(activeServiceMap.keySet());
		allServiceNames.addAll(inactiveServiceMap.keySet());
	}

	public Map<String, String> getActiveServices() {
		ensureInitialized();
		return activeServiceMap;
	}

	public Map<String, String> getInactiveServices() {
		ensureInitialized();
		return inactiveServiceMap;
	}

	public Map<String, List<String>> getTagMap() {
		ensureInitialized();
		return this.tagMap;
	}

	public Map<String, Long> getCountMap() {
		ensureInitialized();
		return this.countMap;
	}

	public SortedSet<String> getAllServiceNames() {
		ensureInitialized();
		return this.allServiceNames;
	}

	public Map<String, String> getSource2NameMap() {
		ensureInitialized();
		// Lazy instantiation.
		if (source2NameMap.size() == 0) {
			for (String name : activeServiceMap.keySet())
				source2NameMap.put(activeServiceMap.get(name), name);
			for (String name : inactiveServiceMap.keySet())
				source2NameMap.put(inactiveServiceMap.get(name), name);
		}

		return source2NameMap;
	}

	private void invoke() {
		final String command = "?action=" + STATE.STATUS.name() + "&format=xml";
		try {
			callRegistry(command);
		} catch (IOException e) {
			logger.error("Could not initialize PSICQUIC registory manager.");
		}
		setMap();

		logger.info("Found " + activeServiceMap.size() + " active PSICQUIC services.");
	}

	private void callRegistry(final String command) throws IOException {
		final URL url = new URL(serviceURLString + command);
		final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setDoInput(true);
		connection.setDoOutput(true);

		connection.setRequestProperty("accept", "text/xml");
		connection.connect();

		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		final StringBuilder builder = new StringBuilder();

		String next;
		while ((next = reader.readLine()) != null)
			builder.append(next);

		reader.close();
		reader = null;

		try {
			parse(builder.toString());
		} catch (ParserConfigurationException e) {
			throw new IOException("Could not parse result from registry.");
		} catch (XPathException e) {
			throw new IOException("Could not parse result from registry.");
		} catch (SAXException e) {
			throw new IOException("Could not parse result from registry.");
		}
	}

	private void parse(String result) throws ParserConfigurationException, IOException, XPathException, SAXException {
		final DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder docbuilder = dbfactory.newDocumentBuilder();
		final Document doc = docbuilder.parse(new ByteArrayInputStream(result.getBytes("UTF-8")));

		final XPathFactory xpf = XPathFactory.newInstance();
		final XPath xp = xpf.newXPath();

		// Extract all service entries.
		final NodeList list = (NodeList) xp.evaluate("//service", doc, XPathConstants.NODESET);

		String regName = null;
		for (int i = 0; i < list.getLength(); i++) {
			regName = list.item(i).getFirstChild().getFirstChild().getNodeValue();
			logger.debug("Service Provider " + i + ": " + regName);
			walk(list.item(i), regName);
		}

	}

	private void walk(Node item, String serviceName) {
		String tag = null;

		for (Node n = item.getFirstChild(); n != null; n = n.getNextSibling()) {
			tag = item.getNodeName();
			if (tag.equals(REST_URL))
				urlMap.put(serviceName, item.getFirstChild().getNodeValue());
			else if (tag.equals(IS_ACTIVE))
				statusMap.put(serviceName, Boolean.parseBoolean(item.getFirstChild().getNodeValue()));
			else if (tag.equals(TAG)) {
				List<String> tagList = tagMap.get(serviceName);
				if (tagList == null)
					tagList = new ArrayList<String>();
				tagList.add(item.getFirstChild().getNodeValue());
				tagMap.put(serviceName, tagList);
			} else if (tag.equals(COUNT)) {
				String entry = item.getFirstChild().getNodeValue();
				Long numberOfRecoreds = 0l;
				try {
					numberOfRecoreds = Long.parseLong(entry);
				} catch (Exception ex) {
					logger.warn("Could not get count", ex);
				}
				countMap.put(serviceName, numberOfRecoreds);
			}

			walk(n, serviceName);
		}
	}

	private void setMap() {
		for (final String serviceName : statusMap.keySet()) {
			if (statusMap.get(serviceName) == true)
				activeServiceMap.put(serviceName, urlMap.get(serviceName));
			else
				inactiveServiceMap.put(serviceName, urlMap.get(serviceName));
		}
	}

	public boolean isActive(final String serviceName) {
		ensureInitialized();
		if (serviceName == null)
			throw new NullPointerException("Service Name is null.");

		if (this.inactiveServiceMap.keySet().contains(serviceName))
			return false;
		else
			return true;
	}

	public String getRestURL(final String serviceName) {
		ensureInitialized();
		if (serviceName == null)
			throw new NullPointerException("Service Name is null.");

		if (allServiceNames.contains(serviceName) == false) {
			throw new IllegalArgumentException("Could not find the service in the registry: " + serviceName);
		}

		String serviceURLString = activeServiceMap.get(serviceName);
		if (serviceURLString == null)
			serviceURLString = inactiveServiceMap.get(serviceName);

		if (serviceURLString == null)
			throw new IllegalStateException("Could not find REST service URL for " + serviceName);

		return serviceURLString;
	}
}
