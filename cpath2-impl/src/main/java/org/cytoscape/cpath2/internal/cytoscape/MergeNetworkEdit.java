package org.cytoscape.cpath2.internal.cytoscape;

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

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;

import org.cytoscape.cpath2.internal.CPath2Factory;
import org.cytoscape.cpath2.internal.util.AttributeUtil;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.undo.AbstractCyEdit;

/**
 * An undoable edit used by MergeNetworkTask
 * to provide undo/redo support.  Code based on cytoscape.editor.AddNodeEdit
 */
public class MergeNetworkEdit extends AbstractCyEdit {

    /**
     * ref to CyNetwork that we are modifying
     */
    private CyNetwork cyNetwork;

    /**
     * ref to map: node is key, value is node position
     */
    private Map<CyNode, Point2D.Double> cyNodes;

    /**
     * ref to edge set
     */
    private Collection<CyEdge> cyEdges;

	private final CPath2Factory factory;

    /**
     * Constructor.
     *
     * @param cyNetwork CyNetwork
     * @param cyNodes   Set<CyNode>
     * @param cyEdges   Set<CyEdge>
     */
    public MergeNetworkEdit(CyNetwork cyNetwork, Collection<CyNode> cyNodes, Collection<CyEdge> cyEdges, CPath2Factory factory) {
        super("Merge Network");
        this.factory = factory;

        // check args
        if (cyNetwork == null || cyNodes == null || cyEdges == null)
            throw new IllegalArgumentException("network, nodes, or edges is null");

        // init args
        this.cyNetwork = cyNetwork;
        this.cyEdges = cyEdges;

        this.cyNodes = new WeakHashMap<CyNode, Point2D.Double>();
        final Collection<CyNetworkView> views = factory.getCyNetworkViewManager().getNetworkViews(cyNetwork);
        CyNetworkView view = null;
		if(views.size() != 0)
			view = views.iterator().next();
		
        if (view != null) {
            for (CyNode cyNode : cyNodes) {
                View<CyNode> nv = view.getNodeView(cyNode);
                double x = nv.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION).doubleValue();
                double y = nv.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION).doubleValue();
                Point2D.Double point = new Point2D.Double(x, y);
                this.cyNodes.put(cyNode, point);
            }
        }
    }

    /**
     * Method to undo this network merge
     */
    public void undo() {

		cyNetwork.removeNodes(cyNodes.keySet());
		cyNetwork.removeEdges(cyEdges);
    }

    /**
     * Method to redo this network merge
     */
    public void redo() {

        // get ref to view
    		final Collection<CyNetworkView> views = factory.getCyNetworkViewManager().getNetworkViews(cyNetwork);
    		CyNetworkView view = null;
    		if(views.size() != 0)
    			view = views.iterator().next();
    		
        if (view != null) {

            // iterate through nodes and restore each one (also set proper position)
            for (CyNode cyNode : cyNodes.keySet()) {
            	CyNode node = cyNetwork.addNode();
            	AttributeUtil.copyAttributes(cyNetwork, cyNode, cyNetwork, node);
            	
                Point2D.Double point = cyNodes.get(cyNode);
                View<CyNode> nv = view.getNodeView(node);
                nv.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, point.getX());
                nv.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, point.getY());
            }

            // interate through edges and restore each one...
            for (CyEdge cyEdge : cyEdges) {
            	// TODO: need to restore edges...
            }

            // do we perform layout here ?
        }

        // fire Cytoscape.NETWORK_MODIFIED
//        Cytoscape.firePropertyChange(Cytoscape.NETWORK_MODIFIED, null, cyNetwork);
	}
}
