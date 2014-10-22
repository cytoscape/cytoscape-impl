package org.cytoscape.view.layout.internal.task;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.util.NodeList;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

public class LayoutWrapperTask extends AbstractTask {
	private final CyApplicationManager appMgr;
	private final CyNetworkViewManager viewMgr;
	List<CyNode> nodes = null;
	private final CyLayoutAlgorithm algorithm;
	private static final String UNWEIGHTED = "(none)";

	@Tunable(description="Network to layout", context="nogui")
	public CyNetwork network = null;

	public NodeList nodeList = new NodeList(null);
	@Tunable(description="Nodes to layout", context="nogui")
	public NodeList getnodeList() {
		if (network == null)
			network = appMgr.getCurrentNetwork();
		nodeList.setNetwork(network);
		return nodeList;
	}
	public void setnodeList(NodeList setValue) {}

	// If this layout algorithm supports edge attributes, pick that
	// up here
	ListSingleSelection<String> possibleEdgeAttributes = null;
	@Tunable(description="Edge attribute to use to weight layout", context="nogui")
	public ListSingleSelection<String> getEdgeAttribute() {
		// Make sure we know the network
		if (network == null)
			network = appMgr.getCurrentNetwork();
		List<String> attrs = getSupportedEdgeAttributes();
		if (attrs == null || attrs.size() == 0) {
			possibleEdgeAttributes = null;
		} else {
			possibleEdgeAttributes = new ListSingleSelection<String>(attrs);
		}
		return possibleEdgeAttributes;
	}
	public void setEdgeAttribute(ListSingleSelection<String> setValue) {}

	// If this layout algorithm supports node attributes, pick that
	// up here
	ListSingleSelection<String> possibleNodeAttributes = null;
	@Tunable(description="Node attribute to use to weight layout", context="nogui")
	public ListSingleSelection<String> getNodeAttribute() {
		// Make sure we know the network
		if (network == null)
			network = appMgr.getCurrentNetwork();
		List<String> attrs = getSupportedNodeAttributes();
		if (attrs == null || attrs.size() == 0) {
			possibleNodeAttributes = null;
		} else {
			possibleNodeAttributes = new ListSingleSelection<String>(attrs);
		}
		return possibleNodeAttributes;
	}
	public void setNodeAttribute(ListSingleSelection<String> setValue) {}

	@ContainsTunables
	public Object layoutContext;

	public LayoutWrapperTask(CyApplicationManager appMgr, CyNetworkViewManager viewMgr, CyLayoutAlgorithm alg) {
		this.appMgr = appMgr;
		this.viewMgr = viewMgr;
		this.algorithm = alg;
		layoutContext = alg.getDefaultLayoutContext();
	}

	@Override
	public void run(final TaskMonitor taskMonitor) {
		if (network == null)
			network = appMgr.getCurrentNetwork();
		Collection<CyNetworkView> views = viewMgr.getNetworkViews(network);

		nodes = nodeList.getValue();

		for (CyNetworkView view: views) {
			Set<View<CyNode>> nodeViews = new HashSet<View<CyNode>>();
			if (nodes == null || nodes.size() == 0) {
				nodeViews = CyLayoutAlgorithm.ALL_NODE_VIEWS;
			}	else {
				for (CyNode node: nodes) 
					nodeViews.add(view.getNodeView(node));
			} 
	
			insertTasksAfterCurrentTask(algorithm.createTaskIterator(view, layoutContext, nodeViews, getLayoutAttribute()));
		}

	}

	private List<String> getSupportedEdgeAttributes() {
		Set<Class<?>> supportedEdgeTypes = algorithm.getSupportedEdgeAttributeTypes();
		if (supportedEdgeTypes == null || supportedEdgeTypes.size() == 0) return null;
		return getSupportedAttributes(supportedEdgeTypes, network.getDefaultEdgeTable());
	}

	private List<String> getSupportedNodeAttributes() {
		Set<Class<?>> supportedNodeTypes = algorithm.getSupportedNodeAttributeTypes();
		if (supportedNodeTypes == null || supportedNodeTypes.size() == 0) return null;
		return getSupportedAttributes(supportedNodeTypes, network.getDefaultNodeTable());
	}

	private List<String> getSupportedAttributes(Set<Class<?>> types, CyTable table) {
		List<String> attributes = new ArrayList<String>();
		attributes.add(UNWEIGHTED);
		for (CyColumn column: table.getColumns()) {
			for (Class<?> type: types) {
				if (column.getType().equals(type)) {
					attributes.add(column.getName());
					break;
				}
			}
		}
		if (attributes.size() > 1)
			return attributes;
		return null;
	}

	private String getLayoutAttribute() {
		if (possibleEdgeAttributes != null && !possibleEdgeAttributes.getSelectedValue().equals(UNWEIGHTED))
			return possibleEdgeAttributes.getSelectedValue();
		if (possibleNodeAttributes != null && !possibleNodeAttributes.getSelectedValue().equals(UNWEIGHTED))
			return possibleNodeAttributes.getSelectedValue();
		return null;
	}

}
