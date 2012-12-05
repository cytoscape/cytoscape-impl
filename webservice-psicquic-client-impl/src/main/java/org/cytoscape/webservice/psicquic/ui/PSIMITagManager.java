package org.cytoscape.webservice.psicquic.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.cytoscape.webservice.psicquic.mapper.MergedNetworkBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PSIMITagManager {
	
	private static final Logger logger = LoggerFactory.getLogger(PSIMITagManager.class);
	
	private static final Pattern SPLIT_PTTR = Pattern.compile("\t");
	
	private final Map<String, String> tag2name;
	
	public PSIMITagManager() {
		this.tag2name = new HashMap<String, String>();
		try {
			createMap();
		} catch (IOException e) {
			logger.warn("Could not create tag map.", e);
		}
		
		if(tag2name.size() == 0)
			logger.warn("Tag map is empty.");
	}

	private final void createMap() throws IOException {
		final URL tableURL = MergedNetworkBuilder.class.getClassLoader().getResource("psimi_terms.txt");
		final BufferedReader reader = new BufferedReader(new InputStreamReader(tableURL.openStream()));

		String line;
		while ((line = reader.readLine()) != null) {
			final String[] result = SPLIT_PTTR.split(line);
			if(result != null && result.length == 2) {
				tag2name.put(result[0], result[1]);
			}
		}
		reader.close();
	}
	
	public String toName(final String tag) {
		return tag2name.get(tag);
	}

}
