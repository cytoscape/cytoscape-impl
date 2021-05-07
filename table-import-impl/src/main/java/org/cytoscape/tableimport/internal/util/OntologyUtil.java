package org.cytoscape.tableimport.internal.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.cytoscape.application.CyUserLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class OntologyUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	private static final String NCBI_TAXON_SERVER = "http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?id=";
	private static final String TAXON_RESOURCE_FILE = "/cytoscape/resources/tax_report.txt";
	private static final String TAXON_FILE = "tax_report.txt";


	public String getSpecies(final BufferedReader taxRd, final BufferedReader gaRd) throws IOException {
		String sp = null;
		String curLine = null;

		while ((sp == null) && (null != (curLine = gaRd.readLine()))) {
			curLine.trim();

			// Skip comment
			if (curLine.startsWith("!")) {
				// do nothing
				// logger.info("Comment: " + curLine);
			} else {
				StringTokenizer st = new StringTokenizer(curLine, "\t");

				while ((sp == null) && (st.hasMoreTokens())) {
					String curToken = st.nextToken();

					if (curToken.startsWith("taxon") || curToken.startsWith("Taxon")) {
						st = new StringTokenizer(curToken, ":");
						st.nextToken();
						curToken = st.nextToken();
						st = new StringTokenizer(curToken, "|");
						curToken = st.nextToken();
						// logger.info("Taxon ID found: " + curToken);
						sp = curToken;
						sp = taxIdToName(sp, taxRd);
					}
				}
			}
		}

		return sp;
	}

	
	public String taxIdToName(String taxId, final BufferedReader taxRd) throws IOException {
		String name = null;
		String curLine = null;

		taxRd.readLine();

		while (null != (curLine = taxRd.readLine())) {
			curLine.trim();

			StringTokenizer st = new StringTokenizer(curLine, "|");
			String[] oneEntry = new String[st.countTokens()];
			int counter = 0;

			while (st.hasMoreTokens()) {
				String curToken = st.nextToken().trim();
				oneEntry[counter] = curToken;
				counter++;

				if (curToken.equals(taxId)) {
					name = oneEntry[1];

					return name;
				}
			}
		}

		return name;
	}


	public Map<String, String> getTaxonMap(File taxonFile) throws IOException {
		
		final Map<String, String> taxonMap = new HashMap<String, String>();
		
		String name = null;
		String curLine = null;

		if (taxonFile.canRead() == true) {
			BufferedReader taxonFileRd = null;

			try {
				taxonFileRd = new BufferedReader(new FileReader(taxonFile));
				taxonFileRd.readLine();

				while (null != (curLine = taxonFileRd.readLine())) {
					curLine.trim();

					StringTokenizer st = new StringTokenizer(curLine, "|");
					String[] oneEntry = new String[st.countTokens()];
					int counter = 0;

					while (st.hasMoreTokens()) {
						String curToken = st.nextToken().trim();
						oneEntry[counter] = curToken;
						counter++;
						name = oneEntry[1];
						taxonMap.put(curToken, name);
					}
				}
			} finally {
				if (taxonFileRd != null)
					taxonFileRd.close();
			}
		}

		return taxonMap;
	}

	
	public Map<String, String> getTaxonMap(BufferedReader taxonFileReader) throws IOException {
		final Map<String, String> taxonMap = new HashMap<String, String>();

		String curLine = null;

		taxonFileReader.readLine();

		while ((curLine = taxonFileReader.readLine()) != null) {
			String[] parts = curLine.split("\\|");

			logger.info("####ID = " + parts[3].trim() + ", Name = " + parts[1].trim());
			taxonMap.put(parts[3].trim(), parts[1].trim());
		}

		return taxonMap;
	}


	private String getTaxonFromNCBI(String id) throws MalformedURLException {
		String txName = null;
		URL taxonURL = null;
		BufferedReader htmlPageReader = null;
		String curLine = null;

		String targetId = id + "&lvl=0";

		taxonURL = new URL(NCBI_TAXON_SERVER + targetId);

		try {
			// htmlPageReader = new BufferedReader(new
			// InputStreamReader(taxonURL.openStream()));
			// Use URLUtil to get the InputStream since we might be using a
			// proxy server
			// and because pages may be cached:
			try {
				htmlPageReader = new BufferedReader(new InputStreamReader(URLUtil.getBasicInputStream(taxonURL), Charset.forName("UTF-8").newDecoder()));

				while ((txName == null) && ((curLine = htmlPageReader.readLine()) != null)) {
					curLine.trim();

					// logger.info("HTML:" + curLine);
					if (curLine.startsWith("<title>Taxonomy")) {
						logger.info("HTML:" + curLine);

						StringTokenizer st = new StringTokenizer(curLine, "(");
						st.nextToken();
						curLine = st.nextToken();
						st = new StringTokenizer(curLine, ")");
						txName = st.nextToken().trim();
						logger.info("Fetch result: NCBI code " + id + " is " + txName);
					}
				}
			} finally {
				if (htmlPageReader != null) {
					htmlPageReader.close();
				}
			}
		} catch (IOException e) {
			logger.error("Unable to get taxonomy " + id + " from NCBI: " + e.getMessage());
		}

		return txName;
	}
}
