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

import org.cytoscape.tableimport.internal.util.CytoscapeServices;
import org.cytoscape.tableimport.internal.util.URLUtil;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.URL;
import java.nio.charset.Charset;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;


/**
 * Network text table reader. This implemets GraphReader just like other network
 * file readers.<br>
 *
 * @since Cytoscape 2.4
 * @version 0.8
 * @author Keiichiro Ono
 *
 */
public class NetworkTableReader extends AbstractGraphReader implements TextTableReader {
	protected static final String COMMENT_CHAR = "!";
	protected final NetworkTableMappingParameters nmp;
	//protected final URL sourceURL;
	protected final NetworkLineParser parser;
	protected final List<Long> nodeList;
	protected final List<Long> edgeList;
	protected final int startLineNumber;
	protected final String commentChar;
	protected final InputStream is;

	protected CyNetwork network;
//	private Map<Object, CyNode> nMap;
	
	private static final Logger logger = LoggerFactory.getLogger(NetworkTableReader.class);

	/**
	 * Creates a new NetworkTableReader object.
	 *
	 * @param networkName  DOCUMENT ME!
	 * @param sourceURL  DOCUMENT ME!
	 * @param nmp  DOCUMENT ME!
	 * @param startLineNumber  DOCUMENT ME!
	 * @param commentChar  DOCUMENT ME!
	 */
	public NetworkTableReader(final String networkName, final InputStream is,
	                          final NetworkTableMappingParameters nmp,
	                          final Map<Object, CyNode> nMap, final CyRootNetwork rootNetwork) 
	{
		super(networkName);
		//this.sourceURL = sourceURL;
		this.is = is;
		this.nmp = nmp;
		this.startLineNumber = nmp.getStartLineNumber();
		this.nodeList = new ArrayList<Long>();
		this.edgeList = new ArrayList<Long>();
		this.commentChar = nmp.getCommentChar();
		
		parser = new NetworkLineParser(nodeList, edgeList, nmp, nMap, rootNetwork);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public List getColumnNames() {
		List<String> colNames = new ArrayList<String>();

		for (String name : nmp.getAttributeNames()) {
			colNames.add(name);
		}

		return colNames;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @throws IOException DOCUMENT ME!
	 */
	public void readTable(CyTable table) throws IOException {
		//InputStream is = null;
		String line;

		network.getRow(network).set("name", this.getNetworkName());
		parser.setNetwork(network);
		
		try {
			BufferedReader bufRd = null;

			//is = URLUtil.getInputStream(sourceURL);
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
						String[] parts = line.split(nmp.getDelimiterRegEx());
						try {
							parser.parseEntry(parts);
						} catch (Exception ex) {
							logger.warn("Couldn't parse row: " + lineCount, ex);
						}
					}

					lineCount++;
				}
			}
			finally {
				if (bufRd != null) {
					bufRd.close();
				}
			}
		}
		finally {
			if (is != null) {
				is.close();
			}
		}
	}


	/**
	 *  DOCUMENT ME!
	 *
	 * @throws IOException DOCUMENT ME!
	 */
	//@Override
	public void read() throws IOException {
		readTable(null);
	}


	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String getReport() {

		final StringBuffer sb = new StringBuffer();
		sb.append(network.getNodeCount() + " nodes and " + network.getEdgeCount() + " edges are loaded.\n");
		sb.append("New network name is " + super.getNetworkName() + "\n\n");

		return sb.toString();		
	}
	
	public void setNetwork(CyNetwork network){
		this.network = network;
	}
	
	public MappingParameter getMappingParameter(){
		return nmp;
	}
}
