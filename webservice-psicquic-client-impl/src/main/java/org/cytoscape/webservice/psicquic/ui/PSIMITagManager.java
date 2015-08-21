package org.cytoscape.webservice.psicquic.ui;

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
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.cytoscape.webservice.psicquic.mapper.CyNetworkBuilder;
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
		final URL tableURL = CyNetworkBuilder.class.getClassLoader().getResource("psimi_terms.txt");
		final BufferedReader reader = new BufferedReader(new InputStreamReader(tableURL.openStream(), Charset.forName("UTF-8").newDecoder()));

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
