/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.io.webservice.biomart.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 
 * TODO: use JAX-RS reference impl.
 */
public class BiomartRestClient {

	private static final Logger logger = LoggerFactory.getLogger(BiomartRestClient.class);

	private static final int CONNECTION_TIMEOUT = 3000;
	private static final int READ_TIMEOUT = 5000;

	private String baseURL;
	private static final String RESOURCE = "/settings/filterconversion.txt";
	private static final String TAXONOMY_TABLE = "/settings/tax_report.txt";

	private Map<String, Map<String, String>> databases = null;

	// Key is datasource, value is database name.
	private Map<String, String> datasourceMap = new HashMap<String, String>();
	private Map<String, Map<String, String>> filterConversionMap;
	private Map<String, String> taxonomyTable;

	private static final int BUFFER_SIZE = 81920;

	/**
	 * Creates a new BiomartStub object from given URL.
	 * 
	 * @param baseURL
	 *            DOCUMENT ME!
	 * @throws IOException
	 */
	public BiomartRestClient(final String baseURL) {

		if (baseURL == null)
			throw new NullPointerException("Biomart base URL is missing.");

		this.baseURL = baseURL + "?";

		try {
		loadConversionFile();
		} catch (IOException ioe) {
			throw new RuntimeException("Couldn't initialize BiomartRestClient",ioe);
		}

		logger.debug("Biomart REST client initialized.");
	}

	private void loadConversionFile() throws IOException {
		filterConversionMap = new HashMap<String, Map<String, String>>();

		InputStreamReader inFile;

		inFile = new InputStreamReader(this.getClass().getResource(RESOURCE)
				.openStream());

		BufferedReader inBuffer = new BufferedReader(inFile);

		String line;
		String trimed;
		String oldName = null;
		Map<String, String> oneEntry = new HashMap<String, String>();

		String[] dbparts;

		while ((line = inBuffer.readLine()) != null) {
			trimed = line.trim();
			dbparts = trimed.split("\\t");

			if (dbparts[0].equals(oldName) == false) {
				oneEntry = new HashMap<String, String>();
				oldName = dbparts[0];
				filterConversionMap.put(oldName, oneEntry);
			}

			oneEntry.put(dbparts[1], dbparts[2]);
		}

		inFile.close();
		inBuffer.close();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param dbName
	 *            DOCUMENT ME!
	 * @param filterID
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public String toAttributeName(String dbName, String filterID) {
		if (filterConversionMap.get(dbName) == null) {
			return null;
		} else {
			return filterConversionMap.get(dbName).get(filterID);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param baseURL
	 *            DOCUMENT ME!
	 */
	public void setBaseURL(String baseURL) {
		if (baseURL == null)
			throw new NullPointerException("URL string is null.");

		this.baseURL = baseURL + "?";
	}

	public String getBaseURL() {
		return this.baseURL;
	}

	/**
	 * Get the registry information from the base URL.
	 * 
	 * @return Map of registry information. Key value is "name" field.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * 
	 * @throws Exception
	 *             DOCUMENT ME!
	 */
	public Map<String, Map<String, String>> getRegistry() throws IOException,
			ParserConfigurationException, SAXException {
		// If already loaded, just return it.
		if (databases != null)
			return databases;

		// Initialize database map.
		databases = new HashMap<String, Map<String, String>>();

		// Prepare URL for the registry status
		final String reg = "type=registry";
		final URL targetURL = new URL(baseURL + reg);

		// Get the result as XML document.
		final DocumentBuilderFactory factory = DocumentBuilderFactory
				.newInstance();
		final DocumentBuilder builder = factory.newDocumentBuilder();

		// TODO: use Proxy is available
		// InputStream is = URLUtil.getBasicInputStream(targetURL);
		InputStream is = targetURL.openStream();

		final Document registry = builder.parse(is);

		// Extract each datasource
		NodeList locations = registry.getElementsByTagName("MartURLLocation");
		int locSize = locations.getLength();
		NamedNodeMap attrList;
		int attrLen;
		String dbID;

		for (int i = 0; i < locSize; i++) {

			attrList = locations.item(i).getAttributes();
			attrLen = attrList.getLength();

			// First, get the key value
			dbID = attrList.getNamedItem("name").getNodeValue();
			Map<String, String> entry = new HashMap<String, String>();

			for (int j = 0; j < attrLen; j++) {
				entry.put(attrList.item(j).getNodeName(), attrList.item(j)
						.getNodeValue());
			}

			databases.put(dbID, entry);
		}

		is.close();
		is = null;

		return databases;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param martName
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws Exception
	 *             DOCUMENT ME!
	 */
	public Map<String, String> getAvailableDatasets(final String martName)
			throws IOException {
		try {
			getRegistry();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}

		final Map<String, String> datasources = new HashMap<String, String>();

		Map<String, String> detail = databases.get(martName);

		String urlStr = "http://" + detail.get("host") + ":"
				+ detail.get("port") + detail.get("path")
				+ "?type=datasets&mart=" + detail.get("name");

		// System.out.println("Connection start:  DB name = " + martName +
		// ", Target URL = " + urlStr + "\n");

		URL url = new URL(urlStr);
		// TODO: use Proxy is available
		// final URLConnection connection = URLUtil.getURLConnection(url);
		final URLConnection connection = url.openConnection();

		connection.setReadTimeout(READ_TIMEOUT);
		connection.setConnectTimeout(CONNECTION_TIMEOUT);

		InputStream is = null;
		try {
			is = connection.getInputStream();
		} catch (Exception e) {
			// Could not create connection.
			is.close();
			throw new IOException("Could not create connection.");
		} finally {
			is.close();
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String s;

		String[] parts;

		while ((s = reader.readLine()) != null) {
			parts = s.split("\\t");

			if ((parts.length > 4) && parts[3].equals("1")) {
				datasources.put(parts[1], parts[2]);
				datasourceMap.put(parts[1], martName);
			}
		}

		is.close();
		reader.close();
		reader = null;
		is = null;

		return datasources;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param datasetName
	 *            DOCUMENT ME!
	 * @param getAll
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws IOException
	 *             DOCUMENT ME!
	 */
	public Map<String, String> getFilters(String datasetName, boolean getAll)
			throws IOException {
		Map<String, String> filters = new HashMap<String, String>();

		String martName = datasourceMap.get(datasetName);
		Map<String, String> detail = databases.get(martName);

		String urlStr = "http://" + detail.get("host") + ":"
				+ detail.get("port") + detail.get("path") + "?virtualschema="
				+ detail.get("serverVirtualSchema") + "&type=filters&dataset="
				+ datasetName;

		// System.out.println("Dataset name = " + datasetName +
		// ", Target URL = " + urlStr + "\n");
		URL url = new URL(urlStr);

		// TODO: Use Proxy if available.
		// InputStream is = URLUtil.getBasicInputStream(url);
		InputStream is = url.openStream();

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String s;

		String[] parts;

		while ((s = reader.readLine()) != null) {
			parts = s.split("\\t");

			if (parts.length <= 1)
				continue;

			if ((parts[1].contains("ID(s)") || parts[1].contains("Accession(s)") || parts[1].contains("IDs"))
					&& (parts[0].startsWith("with_") == false)
					&& (parts[0].endsWith("-2") == false)
					|| parts.length > 6
					&& parts[5].equals("id_list")) {
				filters.put(parts[1], parts[0]);
//				System.out.println("### Filter Entry = " + parts[1] + " = "
//						+ parts[0]);
			}

		}

		is.close();
		reader.close();
		reader = null;
		is = null;

		return filters;
	}

	
	public Map<String, String[]> getAttributes(String datasetName)
			throws IOException {
		Map<String, String[]> attributes = new HashMap<String, String[]>();

		String martName = datasourceMap.get(datasetName);
		Map<String, String> detail = databases.get(martName);

		String urlStr = "http://" + detail.get("host") + ":"
				+ detail.get("port") + detail.get("path") + "?virtualschema="
				+ detail.get("serverVirtualSchema")
				+ "&type=attributes&dataset=" + datasetName;

		//System.out.println("Attr Import: Dataset name = " + datasetName + ", Target URL = " + urlStr + "\n");
		URL url = new URL(urlStr);

		// TODO: use proxy
		// InputStream is = URLUtil.getBasicInputStream(url);
		InputStream is = url.openStream();

		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String s;

		String[] attrInfo;

		String[] parts;

		while ((s = reader.readLine()) != null) {
			parts = s.split("\\t");
			attrInfo = new String[3];
			
			if (parts.length == 0)
				continue;

			if (parts.length == 4) {
				// Display name of this attribute.
				attrInfo[0] = parts[1];
				attrInfo[1] = parts[2];
				attrInfo[2] = parts[3];
			} else if (parts.length > 1) {
				attrInfo[0] = parts[1];
			}

			attributes.put(parts[0], attrInfo);
		}

		is.close();
		reader.close();
		reader = null;
		is = null;

		return attributes;
	}

	/**
	 * Send the XML query to Biomart, and get the result as table.
	 * 
	 * @param xmlQuery
	 *            DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 * 
	 * @throws Exception
	 *             DOCUMENT ME!
	 */
	public BufferedReader sendQuery(String xmlQuery) throws IOException {

		//System.out.println("Generated Query:\n\n" + xmlQuery);

		URL url = new URL(baseURL);

		// TODO: use proxy
		// URLConnection uc = URLUtil.getURLConnection(url);
		URLConnection uc = url.openConnection();

		uc.setDoOutput(true);
		uc.setRequestProperty("User-Agent", "Java URLConnection");

		OutputStream os = uc.getOutputStream();

		final String postStr = "query=" + xmlQuery;
		PrintStream ps = new PrintStream(os);

		// Post the data
		ps.print(postStr);
		os.close();
		ps.close();
		ps = null;
		os = null;

		return new BufferedReader(new InputStreamReader(uc.getInputStream()),
				BUFFER_SIZE);
		// String line;
		// line = reader.readLine();
		//
		// String[] parts = line.split("\\t");
		// final List<String[]> result = new ArrayList<String[]>();
		// result.add(parts);

		// while ((line = reader.readLine()) != null) {
		//
		// System.out.println("Result ==> " + line);
		//
		// parts = line.split("\\t");
		// result.add(parts);
		// }
		//
		// is.close();
		// reader.close();
		// reader = null;

		// return result;
	}

	private String taxID2datasource(String ncbiTaxID) throws IOException {
		if (taxonomyTable == null) {
			final InputStreamReader inFile;
			inFile = new InputStreamReader(this.getClass()
					.getResource(TAXONOMY_TABLE).openStream());

			BufferedReader inBuffer = new BufferedReader(inFile);

			String line;
			String trimed;
			taxonomyTable = new HashMap<String, String>();

			String[] dbparts;

			String name1 = null;
			String name2 = null;
			String[] spName;
			while ((line = inBuffer.readLine()) != null) {
				trimed = line.trim();
				dbparts = trimed.split("\\t");
				spName = dbparts[0].split(" ");
				name1 = spName[0].substring(0, 1);
				name2 = spName[1];
				taxonomyTable.put(dbparts[1], (name1 + name2).toLowerCase());
			}

			inFile.close();
			inBuffer.close();
		}

		if (taxonomyTable.get(ncbiTaxID) == null)
			return null;
		else
			return taxonomyTable.get(ncbiTaxID) + "_gene_ensembl";
	}

	/**
	 * Method to return all GO annotations for the given NCBI taxonomy ID.
	 * 
	 * @param ncbiTaxID
	 * @return Annotation as a text table.
	 * @throws IOException
	 */
	public List<String[]> getAllGOAnnotations(final String ncbiTaxID)
			throws IOException {
		List<String[]> res = new ArrayList<String[]>();

		final String dbName = taxID2datasource(ncbiTaxID);

		if (dbName == null)
			return res;

		Dataset dataset;
		Attribute[] attrs;
		Filter[] filters;

		dataset = new Dataset(dbName);
		attrs = new Attribute[3];
		attrs[0] = new Attribute("ensembl_gene_id");
		attrs[1] = new Attribute("go");
		attrs[2] = new Attribute("evidence_code");

		filters = new Filter[1];
		filters[0] = new Filter("with_go", null);

		String query2 = XMLQueryBuilder.getQueryString(dataset, attrs, filters);

		BufferedReader reader = sendQuery(query2);

		String line;
		line = reader.readLine();

		String[] parts = line.split("\\t");

		res.add(parts);

		while ((line = reader.readLine()) != null) {
			parts = line.split("\\t");
			res.add(parts);
		}

		reader.close();
		reader = null;

		return res;
	}

	public List<String[]> getAllAliases(final String ncbiTaxID)
			throws IOException {
		List<String[]> res = new ArrayList<String[]>();

		final String dbName = taxID2datasource(ncbiTaxID);

		return res;
	}
}
