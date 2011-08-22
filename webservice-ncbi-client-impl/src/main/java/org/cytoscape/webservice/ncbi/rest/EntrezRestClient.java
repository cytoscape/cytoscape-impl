package org.cytoscape.webservice.ncbi.rest;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.webservice.ncbi.ImportTableTask;
import org.cytoscape.webservice.ncbi.ui.AnnotationCategory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;


public class EntrezRestClient {
	private static final Logger logger = LoggerFactory.getLogger(EntrezRestClient.class);
	private static final String BASE_URL = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/";
	public static final String FETCH_URL = BASE_URL + "efetch.fcgi?db=gene&retmode=xml&id=";
	private static final String SEARCH_URL = BASE_URL + "esearch.fcgi?db=gene&retmax=100000&term=";
	private final String regex = "\\s+";
	private static final String ID = "Id";

	private final CyTableFactory tableFactory;
	private final CyNetworkFactory networkFactory;
	private final CyTableManager tableManager;

	public EntrezRestClient(final CyNetworkFactory networkFactory,
				final CyTableFactory tableFactory, final CyTableManager tableManager)
	{
		this.networkFactory = networkFactory;
		this.tableFactory   = tableFactory;
		this.tableManager   = tableManager;
	}

	public Set<String> search(final String queryString) throws IOException, ParserConfigurationException, SAXException {
		final URL url = createURL(SEARCH_URL, queryString);

		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder builder = factory.newDocumentBuilder();
		InputStream is = url.openStream();

		final Document result = builder.parse(is);

		final Set<String> idSet = new HashSet<String>();
		final NodeList ids = result.getElementsByTagName(ID);
		final int dataSize = ids.getLength();

		for (int i = 0; i < dataSize; i++) {
			Node id = ids.item(i);
			idSet.add(id.getTextContent());
		}

		is.close();
		is = null;

		return idSet;
	}

	public CyNetwork importNetwork(final Set<String> idList) {

		long startTime = System.currentTimeMillis();

		final ExecutorService executer = Executors.newFixedThreadPool(4);

		logger.debug("Executor initialized.");
		final CyNetwork newNetwork = networkFactory.getInstance();


		final ConcurrentMap<String, CyNode> nodeName2CyNodeMap = new ConcurrentHashMap<String, CyNode>();

		int group = 0;
		int buketNum = 10;
		String[] box = new String[buketNum];


		for (String entrezID : idList) {
			box[group] = entrezID;
			group++;

			if (group == buketNum) {
				executer.submit(new EntryProcessor<String>(new ImportNetworkTask<String>(box, newNetwork, nodeName2CyNodeMap)));
				group = 0;
				box = new String[buketNum];
			}
		}

		String[] newbox = new String[group];

		for (int i = 0; i < group; i++)
			newbox[i] = box[i];

		executer.submit(new EntryProcessor<String>(new ImportNetworkTask<String>(box, newNetwork, nodeName2CyNodeMap)));

		try {
			executer.shutdown();
			executer.awaitTermination(1000, TimeUnit.SECONDS);

			long endTime = System.currentTimeMillis();
			double sec = (endTime - startTime) / (1000.0);
			System.out.println("Finished in " + sec + " sec.");

//			if ((canceled != null) && canceled) {
//				canceled = null;
//
//				return null;
//			}
		} catch( Exception ex) {
			ex.printStackTrace();
		}

		return newNetwork;
	}



	public CyTable importDataTable(final Set<String> idList, final Set<AnnotationCategory> category) {
		if(idList == null || idList.size() == 0)
			throw new IllegalArgumentException("ID list is null.");

		long startTime = System.currentTimeMillis();
		final ExecutorService executer = Executors.newFixedThreadPool(4);

		logger.debug("Table Import Executor initialized.");
		final Date currentDate = new Date();
		final CyTable table = tableFactory.createTable("NCBI Global Table: " + currentDate.toString(), CyTableEntry.NAME, String.class, true, true);

		int group = 0;
		int buketNum = 10;
		String[] box = new String[buketNum];


		for (String entrezID : idList) {
			box[group] = entrezID;
			group++;

			if (group == buketNum) {
				executer.submit(new ImportTableTask(box, category, table));
				group = 0;
				box = new String[buketNum];
			}
		}

		String[] newbox = new String[group];

		for (int i = 0; i < group; i++)
			newbox[i] = box[i];

		executer.submit(new ImportTableTask(box, category, table));

		try {
			executer.shutdown();
			executer.awaitTermination(1000, TimeUnit.SECONDS);

			long endTime = System.currentTimeMillis();
			double sec = (endTime - startTime) / (1000.0);
			System.out.println("Table Import Finished in " + sec + " sec.");

//			if ((canceled != null) && canceled) {
//				canceled = null;
//
//				return null;
//			}
		} catch( Exception ex) {
			ex.printStackTrace();
		}

		tableManager.addTable(table);

		return table;
	}

	private URL createURL(final String base, final String queryString) throws IOException {
		final String[] parts = queryString.split(regex);
		final StringBuilder builder = new StringBuilder();

		if (parts.length != 0) {
			for (String dTerm : parts) {
				final String trimed = dTerm.trim();
				builder.append(trimed + "+");
			}
		}

		String urlString = builder.toString();
		urlString = urlString.substring(0, urlString.length() - 1);
		final URL url = new URL(base + urlString);
		logger.debug("Query URL = " + url.toString());
		return url;
	}
}
