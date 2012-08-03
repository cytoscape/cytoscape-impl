/*
 Copyright (c) 2006, 2010-2011, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.io.internal.read.nnf;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.internal.read.AbstractNetworkReader;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Reader for graphs in the interactions file format. Given the filename,
 * provides the graph and attributes objects constructed from the file.
 */
public class NNFNetworkReader extends AbstractNetworkReader {
	private static final Logger logger = LoggerFactory.getLogger(NNFNetworkReader.class);

	private final CyLayoutAlgorithmManager layouts;	
	// Optional comments start with this character and extend to the end of line.
	private static final char COMMENT_CHAR = '#';	
	private final NNFParser parser;
	private TaskMonitor parentTaskMonitor;

	public NNFNetworkReader(InputStream is, CyLayoutAlgorithmManager layouts,
			CyNetworkViewFactory cyNetworkViewFactory, CyNetworkFactory cyNetworkFactory,
			CyNetworkManager cyNetworkManagerServiceRef) {
		super(is, cyNetworkViewFactory, cyNetworkFactory);
		this.layouts = layouts;
		this.parser = new NNFParser(cyNetworkManagerServiceRef, cyNetworkFactory);
	}

	@Override
	public void run(TaskMonitor tm) throws IOException {
		try {
			readInput(tm);
		} finally {
			if (inputStream != null) {
				inputStream.close();
				inputStream = null;
			}
		}
	}

	private void readInput(TaskMonitor tm) throws IOException {
		this.parentTaskMonitor = tm;
		tm.setProgress(0.0);

		// Create buffered reader from given InputStream
		final BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));

		String line;
		try {
			for (int lineNumber = 1; (line = in.readLine()) != null; ++lineNumber) {
				line = processComment(line);
				if (line.length() == 0) {
					continue;
				}
				if (!parser.parse(line)) {
					throw new IOException("Malformed line in NNF file: " + lineNumber + " \"" + line + "\"");
				}
			}
		} finally {
			in.close();
		}
		
		if (parser.getNetworks().size() == 0) {
			throw new IOException("Input NNF file is empty!");
		}

		this.cyNetworks = new CyNetwork[parser.getNetworks().size()]; 
		
		Iterator<CyNetwork> it = parser.getNetworks().iterator();
		
		int i=0;
		while(it.hasNext()){
			this.cyNetworks[i++] = it.next();
		}
		
		tm.setProgress(1.0);

		logger.debug("NNF file loaded!");
	}

	
	private String processComment(String line) {
		final int hashPos = line.indexOf(COMMENT_CHAR);
		if (hashPos != -1) {
			line = line.substring(0, hashPos);
		}
		return line.trim();
	}


	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork network) {
		final CyNetworkView view = cyNetworkViewFactory.createNetworkView(network);

		final CyLayoutAlgorithm layout = layouts.getDefaultLayout();
		TaskIterator itr = layout.createTaskIterator(view, layout.getDefaultLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS, "");
		Task nextTask = itr.next();
		try {
			nextTask.run(parentTaskMonitor);
		} catch (Exception e) {
			throw new RuntimeException("Could not finish layout", e);
		}

		return view;
	}
}
