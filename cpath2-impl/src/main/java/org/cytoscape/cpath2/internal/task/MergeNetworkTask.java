package org.cytoscape.cpath2.internal.task;

/*
 * #%L
 * Cytoscape CPath2 Impl (cpath2-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2007 - 2013
 *   Memorial Sloan-Kettering Cancer Center
 *   The Cytoscape Consortium
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

// imports

import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.cpath2.internal.CPath2Factory;
import org.cytoscape.cpath2.internal.util.AttributeUtil;
import org.cytoscape.cpath2.internal.util.SelectUtil;
import org.cytoscape.cpath2.internal.web_service.CPathProperties;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

/**
 * Task to merge a network.
 *
 * @author Benjamin Gross
 */
public class MergeNetworkTask implements Task {

    /**
     * ref to cpathInstanceURL
     */
    private URL cpathInstanceURL;

    /**
     * ref to cyNetwork
     */
    private CyNetwork mergedNetwork;

	private final CPath2Factory factory;

    /**
     * Constructor.
     *
     * @param cpathURL URL
     * @param cyNetwork         CyNetwork
     */
    public MergeNetworkTask(URL cpathURL, CyNetwork cyNetwork, CPath2Factory factory) {
    	this.factory = factory;
    	
        // init member vars
        this.cpathInstanceURL = cpathURL;
        this.mergedNetwork = cyNetwork;
    }

    @Override
    public void cancel() {
    }
    

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        // read the network from cpath instance
        taskMonitor.setProgress(0);
        taskMonitor.setStatusMessage("Reading in Network Data from "
                + CPathProperties.getInstance().getCPathServerName()  + "...");
        
        CyNetworkReaderManager manager = factory.getCyNetworkViewReaderManager();
        CyNetworkReader reader = manager.getReader(cpathInstanceURL.toURI(), cpathInstanceURL.getFile());
        reader.run(taskMonitor);
        
        CyNetwork network = reader.getNetworks()[0];

        // unselect all nodes / edges
        SelectUtil.unselectAllNodes(mergedNetwork);
        SelectUtil.unselectAllEdges(mergedNetwork);

        // refs to capture new nodes and edgets
        Map<String, CyNode> newNodes = new HashMap<String, CyNode>();
        Set<CyEdge> newEdges = new HashSet<CyEdge>();

        // add new nodes and edges to existing network
        // TODO tbd: worry about networks that exceed # node/edge threshold
        for (CyNode node : network.getNodeList()) {
            CyNode mergedNode = mergedNetwork.addNode();
            AttributeUtil.copyAttributes(network, node, mergedNetwork, mergedNode);
            String name = network.getRow(mergedNode).get(CyNetwork.NAME, String.class);
            newNodes.put(name, mergedNode);
        }
        for (CyEdge edge : network.getEdgeList()) {
        	String sourceName = network.getRow(edge.getSource()).get(CyNetwork.NAME, String.class);
        	String targetName = network.getRow(edge.getTarget()).get(CyNetwork.NAME, String.class);
        	CyNode source = newNodes.get(sourceName);
        	CyNode target = newNodes.get(targetName);
            CyEdge mergedEdge = mergedNetwork.addEdge(source, target, true);
            AttributeUtil.copyAttributes(network, edge, mergedNetwork, mergedEdge);
            newEdges.add(mergedEdge);
        }

        // execute any post processing -
        // in this case, biopax style is applied, network attributes set, etc
//        reader.doPostProcessing(mergedNetwork);

        // select nodes / edges
        Collection<CyNode> nodes = newNodes.values();
        SelectUtil.setSelectedNodeState(mergedNetwork,nodes, true);
        SelectUtil.setSelectedEdgeState(mergedNetwork,newEdges, true);

        // setup undo
        UndoSupport undo = factory.getUndoSupport();
        undo.postEdit(factory.createMergeNetworkEdit(mergedNetwork, nodes, newEdges));

        // fire Cytoscape.NETWORK_MODIFIED - should be removed when undo support is back in
//        Cytoscape.firePropertyChange(Cytoscape.NETWORK_MODIFIED, null, mergedNetwork);

        // update the task monitor
        taskMonitor.setStatusMessage(getMergeStatus(mergedNetwork, nodes.size(), newEdges.size()));
        taskMonitor.setProgress(1.0);
    }

    /**
     * Constructs merge status string.
     * (based on cytoscape.action.LoadNetworkFromUrlTask.informUserOfGraphStats)
     *
     * @param cyNetwork CyNetwork
     * @param nodeCount int
     * @param edgeCount int
     * @return String
     */
    private String getMergeStatus(CyNetwork cyNetwork, int nodeCount, int edgeCount) {

        NumberFormat formatter = new DecimalFormat("#,###,###");
        StringBuffer sb = new StringBuffer();

        // construct status string
        sb.append("Succesfully merged network from:  ");
        sb.append(cpathInstanceURL.toString() + ".\n");
        sb.append(formatter.format(nodeCount) + " nodes and " + formatter.format(edgeCount) + " edges have been merged.");
        sb.append("  The merged network contains a total of " + formatter.format(cyNetwork.getNodeCount()));
        sb.append(" nodes and " + formatter.format(cyNetwork.getEdgeCount()) + " edges.");

        // outta here
        return sb.toString();
	}
}
