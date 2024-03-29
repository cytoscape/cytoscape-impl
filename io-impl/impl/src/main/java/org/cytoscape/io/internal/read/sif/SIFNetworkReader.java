package org.cytoscape.io.internal.read.sif;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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

/**
 * Reader for graphs in the interactions file format. Given the filename,
 * provides the graph and attributes objects constructed from the file.
 */
public class SIFNetworkReader extends AbstractCyNetworkReader {
	
	private static final String TAB = "\t";
	private String delimiter = " "; // single space

	private final StringBuilder edgeNameBuilder = new StringBuilder();
	
	private TaskMonitor parentTaskMonitor;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	public SIFNetworkReader(final InputStream is, final CyServiceRegistrar serviceRegistrar) {
		super(
				is, 
				serviceRegistrar.getService(CyApplicationManager.class), 
				serviceRegistrar.getService(CyNetworkFactory.class), 
				serviceRegistrar.getService(CyNetworkManager.class),
				serviceRegistrar.getService(CyRootNetworkManager.class)
		);
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor tm) throws IOException {
		tm.setTitle("Read SIF File");
		
		try {
			readInput(tm);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
					inputStream = null;
				} catch (Exception e) {
					logger.warn("Cannot close SIF input stream", e);
				}
			}
		}
	}

	private void readInput(TaskMonitor tm) throws IOException {
		tm.setProgress(0.0);
		tm.setStatusMessage("Preparing to read...");
		
		this.parentTaskMonitor = tm;

		/*
		 * Whitespace (space or tab) is used to delimit the names in the simple interaction file format.
		 * However, in some cases spaces are desired in a node name or edge type.
		 * The standard is that, if the file contains any tab characters, then tabs are used to delimit
		 * the fields and spaces are considered part of the name.
		 * If the file contains no tabs, then any spaces are delimiters that separate names
		 * (and names cannot contain spaces).
		 */
		
		final CharsetDecoder charset = Charset.forName("UTF-8").newDecoder();
		final BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, charset), 128 * 1024);
		String line = null;
		
		CyRootNetwork root = getRootNetwork();
		final CySubNetwork newNetwork;
		
		if (root != null)
			newNetwork = root.addSubNetwork();
		else // Need to create new network with new root.
			newNetwork = (CySubNetwork) cyNetworkFactory.createNetwork();

		// First Pass: try to read the file until we find a TAB character
		// (NOTE: We're assuming that BufferedReader always supports the mark() operation!)
		tm.setProgress(0.1);
		tm.setStatusMessage("Analyzing SIF file...");
		
		List<String> allLines = new ArrayList<>();
		
		while ((line = br.readLine()) != null) {
			if (cancelled) {
				br.close();
				return;
			}
			
			if (!TAB.equals(delimiter) && line.contains(TAB))
				delimiter = TAB;
			
			if (!line.trim().isEmpty())
				allLines.add(line);
		}
		
		// Second Pass: create nodes and interactions
		tm.setProgress(0.2);
		tm.setStatusMessage("Processing the interactions...");
		int numInteractionsRead = 0;
		Map<Object, CyNode> nMap = getNodeMap();
		int total = allLines.size();
		
		for (int i = 0; i < total; i++) {
			if (cancelled) {
				// Cancel called. Clean up the garbage.
				nMap.clear();
				br.close();
				return;
			}
			
			line = allLines.set(i, null); // Making sure this list entry is ready to be garbage collected!

			try {
				final Interaction itr = new Interaction(line, delimiter);
				createEdge(itr, newNetwork, nMap);
			} catch (Exception e) {
				// Simply ignore invalid lines.
				logger.warn("Invalid SIF line: " + line, e);
				continue;
			}

			if ((++numInteractionsRead % 1000) == 0)
				tm.setStatusMessage("Processed " + numInteractionsRead + " interactions so far.");
		}

		br.close();
		tm.setStatusMessage("Processed " + numInteractionsRead + " interactions in total.");

		nMap.clear();
		nMap = null;

		this.networks = new CyNetwork[] { newNetwork };
		tm.setProgress(1.0);
	}
	
	private void createEdge(final Interaction itr, final CySubNetwork subNetwork, final Map<Object, CyNode> nMap) {
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
		final CyNetworkView view = getNetworkViewFactory().createNetworkView(network);

		CyLayoutAlgorithmManager layoutMgr = serviceRegistrar.getService(CyLayoutAlgorithmManager.class);
		CyLayoutAlgorithm layout = layoutMgr.getDefaultLayout();
		String attribute = layoutMgr.getLayoutAttribute(layout, view);
		
		TaskIterator itr = layout.createTaskIterator(view, layout.getDefaultLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS, attribute);
		Task nextTask = itr.next();
		
		try {
			nextTask.run(parentTaskMonitor);
		} catch (Exception e) {
			throw new RuntimeException("Could not finish layout", e);
		}

		return view;
	}
}
