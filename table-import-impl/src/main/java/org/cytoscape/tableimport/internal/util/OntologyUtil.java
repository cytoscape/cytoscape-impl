package org.cytoscape.tableimport.internal.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OntologyUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(OntologyUtil.class);

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
				htmlPageReader = new BufferedReader(new InputStreamReader(URLUtil.getBasicInputStream(taxonURL)));

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
