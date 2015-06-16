package org.cytoscape.tableimport.internal.reader;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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

//import cytoscape.data.readers.AbstractGraphReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Network text table reader. This implements GraphReader just like other network file readers.
 */
public class NetworkTableReader extends AbstractGraphReader implements TextTableReader {
	
	protected static final String COMMENT_CHAR = "!";
	
	protected final NetworkTableMappingParameters mapping;
	protected final NetworkLineParser parser;
	protected final List<Long> nodeList;
	protected final List<Long> edgeList;
	protected final int startLineNumber;
	protected final String commentChar;
	protected final InputStream is;

	protected CyNetwork network;
	
	private static final Logger logger = LoggerFactory.getLogger(NetworkTableReader.class);

	public NetworkTableReader(final String networkName,
							  final InputStream is,
	                          final NetworkTableMappingParameters mapping,
	                          final Map<Object, CyNode> nMap,
	                          final CyRootNetwork rootNetwork,
	                          final CyServiceRegistrar serviceRegistrar) {
		super(networkName, serviceRegistrar);
		
		this.is = is;
		this.mapping = mapping;
		this.startLineNumber = mapping.getStartLineNumber();
		this.nodeList = new ArrayList<Long>();
		this.edgeList = new ArrayList<Long>();
		this.commentChar = mapping.getCommentChar();
		
		parser = new NetworkLineParser(nodeList, edgeList, mapping, nMap, rootNetwork);
	}

	@Override
	public List<String> getColumnNames() {
		return Arrays.asList(mapping.getAttributeNames());
	}

	@Override
	public void readTable(final CyTable table) throws IOException {
		String line;

		network.getRow(network).set("name", this.getNetworkName());
		parser.setNetwork(network);

		// TODO: it would be really useful to be able to try to recover from a UTF-8 decoder failure
		// by resetting the stream and attempting to start over with ISO-8859-1.  Unfortunately, the
		// way our reader code is structured, we can't easily do this.
		
		try {
			BufferedReader bufRd = null;

			try {
				bufRd = new BufferedReader(new InputStreamReader(is,Charset.forName("UTF-8").newDecoder()));
				/*
				 * Read & extract one line at a time. The line can be Tab delimited,
				 */
				int lineCount = 0;
				int skipped = 0;

				while ((line = bufRd.readLine()) != null) {
					/*
					 * Ignore Empty & Comment lines.
					 */
					if ((commentChar != null) && (commentChar.trim().length() != 0)
						&& line.startsWith(commentChar)) {
						skipped++;
					} else if ((line.trim().length() > 0) && ((startLineNumber + skipped) <= lineCount)) {
						String[] parts = line.split(mapping.getDelimiterRegEx());
						try {
							parser.parseEntry(parts);
						} catch (Exception ex) {
							logger.warn("Couldn't parse row: " + lineCount, ex);
						}
					}

					lineCount++;
				}
			} catch (MalformedInputException mie) {
				throw new IOException("Unable to import network: illegal character encoding in input");
			} finally {
				if (bufRd != null)
					bufRd.close();
			}
		}
		finally {
			if (is != null) {
				is.close();
			}
		}
	}

	@Override
	public void read() throws IOException {
		readTable(null);
	}

	@Override
	public String getReport() {
		final StringBuffer sb = new StringBuffer();
		sb.append(network.getNodeCount() + " nodes and " + network.getEdgeCount() + " edges are loaded.\n");
		sb.append("New network name is " + super.getNetworkName() + "\n\n");

		return sb.toString();		
	}
	
	@Override
	public void setNetwork(CyNetwork network){
		this.network = network;
	}
	
	@Override
	public MappingParameter getMappingParameter(){
		return mapping;
	}
}
