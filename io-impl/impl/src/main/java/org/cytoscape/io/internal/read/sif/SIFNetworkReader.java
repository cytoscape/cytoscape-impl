package org.cytoscape.io.internal.read.sif;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
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
public class SIFNetworkReader extends AbstractCyNetworkReader {
	private static final Logger logger = LoggerFactory.getLogger(SIFNetworkReader.class);

	private static final String TAB = "\t";
	private String delimiter = " "; // single space

	private final CyLayoutAlgorithmManager layouts;
	private final StringBuilder edgeNameBuilder = new StringBuilder();
	
	private TaskMonitor parentTaskMonitor;

	private Map<String, CyNode> nMap;
	
	public SIFNetworkReader(InputStream is, final CyLayoutAlgorithmManager layouts,
			final CyNetworkViewFactory cyNetworkViewFactory, final CyNetworkFactory cyNetworkFactory,
			final CyNetworkManager cyNetworkManager, final CyRootNetworkManager cyRootNetworkManager) {
		super(is, cyNetworkViewFactory, cyNetworkFactory, cyNetworkManager, cyRootNetworkManager);
		this.layouts = layouts;
		
		this.nMap = new HashMap<String, CyNode>(10000);
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
			new BufferedReader(new InputStreamReader(inputStream, Charset.forName("UTF-8").newDecoder()), 128*1024);

		CySubNetwork subNetwork = null;
		final CyRootNetwork rootNetwork = getRootNetwork();
		
		if(rootNetwork != null) {
			subNetwork = rootNetwork.addSubNetwork();
		} else {
			// Need to create new network with new root.
			subNetwork = (CySubNetwork) cyNetworkFactory.createNetwork();
		}
		
		Map<Object, CyNode> nMap = getNodeMap() ;
		
		tm.setProgress(0.1);
		
		final String firstLine = br.readLine();
		if (firstLine.contains(TAB))
			delimiter = TAB;
		createEdge(new Interaction(firstLine.trim(), delimiter), subNetwork);

		tm.setProgress(0.15);
		tm.setStatusMessage("Processing the interactions...");
		int numInteractionsRead = 0;
		while ((line = br.readLine()) != null) {
			if (cancelled) {
				// Cancel called. Clean up the garbage.
				nMap.clear();
				nMap = null;
				subNetwork = null;
				br.close();
				return;
			}

			if (line.trim().length() <= 0)
				continue;

			try {
				final Interaction itr = new Interaction(line, delimiter);
				createEdge(itr, subNetwork);
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

		this.networks = new CyNetwork[] {subNetwork};
		tm.setProgress(1.0);
		logger.debug("SIF file loaded: ID = " + subNetwork.getSUID());
	}
	
	
	private void createEdge(final Interaction itr, final CySubNetwork subNetwork) {
		CyNode sourceNode = nMap.get(itr.getSource());
		if (sourceNode == null) {
			sourceNode = subNetwork.addNode();
			subNetwork.getRow(sourceNode).set(CyNetwork.NAME, itr.getSource());
			nMap.put(itr.getSource(), subNetwork.getRootNetwork().getNode(sourceNode.getSUID()));
		}

		for (final String target : itr.getTargets()) {
			CyNode targetNode = nMap.get(target);
			if (targetNode == null) {
				targetNode = subNetwork.addNode();
				subNetwork.getRow(targetNode).set(CyNetwork.NAME, target);
				nMap.put(target, subNetwork.getRootNetwork().getNode(targetNode.getSUID()));
			}
			
			// Add the sourceNode and targetNode to subNetwork
			if (!subNetwork.containsNode(sourceNode)){
				subNetwork.addNode(sourceNode);				
			}
			if (!subNetwork.containsNode(targetNode)){
				subNetwork.addNode(targetNode);				
			}
			
			final CyEdge edge = subNetwork.addEdge(sourceNode, targetNode, true);
			subNetwork.getRow(edge).set(CyNetwork.NAME, getEdgeName(itr,target));
			subNetwork.getRow(edge).set(CyEdge.INTERACTION, itr.getType());
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
