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
package org.cytoscape.io.internal.read.sif;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
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
import org.cytoscape.model.subnetwork.CyRootNetworkManager;

/**
 * Reader for graphs in the interactions file format. Given the filename,
 * provides the graph and attributes objects constructed from the file.
 */
public class SIFNetworkReader extends AbstractNetworkReader {
	private static final Logger logger = LoggerFactory.getLogger(SIFNetworkReader.class);

	private static final String TAB = "\t";
	private String delimiter = " "; // single space

	private final CyEventHelper eventHelper;
	private final CyLayoutAlgorithmManager layouts;
	private final StringBuilder edgeNameBuilder = new StringBuilder();
	
	private TaskMonitor parentTaskMonitor;
	
	public SIFNetworkReader(InputStream is, CyLayoutAlgorithmManager layouts,
			CyNetworkViewFactory cyNetworkViewFactory, CyNetworkFactory cyNetworkFactory,
			final CyEventHelper eventHelper, CyNetworkManager cyNetworkManager, CyRootNetworkManager cyRootNetworkManager) {
		super(is, cyNetworkViewFactory, cyNetworkFactory, cyNetworkManager, cyRootNetworkManager);
		this.layouts = layouts;
		this.eventHelper = eventHelper;
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

		String line;
		final BufferedReader br =
			new BufferedReader(new InputStreamReader(inputStream), 128*1024);
		Map<String, CyNode> nMap = new HashMap<String, CyNode>(10000);

		String networkCollectionName =  networkCollection.getSelectedValue().toString();

		CyNetwork network;
		if (networkCollectionName.equalsIgnoreCase(CRERATE_NEW_COLLECTION_STRING)){
			network = cyNetworkFactory.createNetwork();
		}
		else {
			// Create a subNetwork of a give collection
			network = this.name2RootMap.get(networkCollectionName).addSubNetwork();
		}
		
		final CyTable nodeTable = network.getDefaultNodeTable();
		final CyTable edgeTable = network.getDefaultEdgeTable();

		tm.setProgress(0.1);
		
		// Generate bundled event to avoid too many events problem.

		final String firstLine = br.readLine();
		if (firstLine.contains(TAB))
			delimiter = TAB;
		createEdge(new Interaction(firstLine.trim(), delimiter), network, nMap);

		tm.setProgress(0.15);
		tm.setStatusMessage("Processing the interactions...");
		int numInteractionsRead = 0;
		while ((line = br.readLine()) != null) {
			if (cancelled) {
				// Cancel called. Clean up the garbage.
				nMap.clear();
				nMap = null;
				network = null;
				br.close();
				return;
			}

			if (line.trim().length() <= 0)
				continue;

			try {
				final Interaction itr = new Interaction(line, delimiter);
				createEdge(itr, network, nMap);
			} catch (Exception e) {
				// Simply ignore invalid lines.
				continue;
			}

			if ( (++numInteractionsRead % 1000) == 0 )
				tm.setStatusMessage("Processed " + numInteractionsRead + " interactions so far.");
		}

		br.close();
		tm.setStatusMessage("Processed " + numInteractionsRead + " interactions in total.");

		nMap.clear();
		nMap = null;

		this.cyNetworks = new CyNetwork[] {network};
		
		tm.setProgress(1.0);

		logger.debug("SIF file loaded: ID = " + network.getSUID());
	}

	private void createEdge(final Interaction itr, final CyNetwork network, final Map<String, CyNode> nMap) {
		CyNode sourceNode = nMap.get(itr.getSource());
		if (sourceNode == null) {
			sourceNode = network.addNode();
			network.getRow(sourceNode).set(CyNetwork.NAME, itr.getSource());
			nMap.put(itr.getSource(), sourceNode);
		}

		for (final String target : itr.getTargets()) {
			CyNode targetNode = nMap.get(target);
			if (targetNode == null) {
				targetNode = network.addNode();
				network.getRow(targetNode).set(CyNetwork.NAME, target);
				nMap.put(target, targetNode);
			}
			final CyEdge edge = network.addEdge(sourceNode, targetNode, true);
			network.getRow(edge).set(CyNetwork.NAME, getEdgeName(itr,target));
			network.getRow(edge).set(CyEdge.INTERACTION, itr.getType());
		}
	}

	private String getEdgeName(Interaction itr, String target) {
		edgeNameBuilder.delete(0,edgeNameBuilder.length());
		edgeNameBuilder.append(itr.getSource());
		edgeNameBuilder.append(" (");
		edgeNameBuilder.append(itr.getType());
		edgeNameBuilder.append(") ");
		edgeNameBuilder.append(target);
		return edgeNameBuilder.toString();
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
