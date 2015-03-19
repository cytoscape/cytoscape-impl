package org.cytoscape.group.internal.view;

/*
 * #%L
 * Cytoscape Group View Impl (group-view-impl)
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
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.internal.CyGroupManagerImpl;
import org.cytoscape.group.internal.data.CyGroupSettingsImpl;
import org.cytoscape.model.CyNode;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.ViewChangedEvent;
import org.cytoscape.view.model.events.ViewChangedListener;
import org.cytoscape.view.model.events.ViewChangeRecord;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;

/**
 * Handle the view portion of group collapse/expand
 */
public class NodeChangeListener implements ViewChangedListener, SessionLoadedListener
{
	private final CyGroupManagerImpl cyGroupManager;
	private final CyEventHelper cyEventHelper;
	private final CyGroupSettingsImpl cyGroupSettings;
	private VisualMappingManager cyStyleManager = null;

	// This is a CyNode to make it faster to reject events we don't
	// care about
	Map<CyNetworkView, Set<CyNode>> groupMap;

	Map<CyNetworkView, Set<CyNode>> nodeMap;
	Map<CyNetworkView, Map<CyNode, CyGroup>> node2GroupMap;

	// Ignore changes flag. We set this when we need move
	// nodes ourselves
	boolean ignoreChanges = false;

	public NodeChangeListener(final CyGroupManagerImpl groupManager,
	                          final CyEventHelper eventHelper,
	                          final CyGroupSettingsImpl cyGroupSettings) {
		this.cyGroupManager = groupManager;
		this.cyEventHelper = eventHelper;
		this.cyGroupSettings = cyGroupSettings;
		groupMap = new HashMap<>();
		nodeMap = new HashMap<>();
		node2GroupMap = new HashMap<>();
	}

	public void handleEvent(ViewChangedEvent<?> e) {
		if (ignoreChanges) {
			// System.out.println("Ignoring changes");
			return;
		}

		CyNetworkView networkView = e.getSource();
		if (!groupMap.containsKey(networkView))
			return;

		Set<CyNode> groupNodes = groupMap.get(networkView);
		Set<CyNode> nodes = nodeMap.get(networkView);

		Collection<?> payload = e.getPayloadCollection();
		for (ViewChangeRecord vcr: (Collection<ViewChangeRecord>)payload) {
			// Only care about nodes
			if (!(vcr.getView().getModel() instanceof CyNode))
				continue;

			View<CyNode> nodeView = vcr.getView();

			VisualProperty<?> property =  vcr.getVisualProperty();
			if (property.equals(BasicVisualLexicon.NODE_X_LOCATION) ||
			    property.equals(BasicVisualLexicon.NODE_Y_LOCATION) ||
					property.equals(BasicVisualLexicon.NODE_WIDTH) ||
					property.equals(BasicVisualLexicon.NODE_HEIGHT)) {

				CyNode node = nodeView.getModel();
				// System.out.println("Got geometry change for "+node);
				if (groupNodes.contains(node)) {
					// System.out.println("It's a group: "+node);
					try {
					updateGroupLocation(networkView, nodeView);
					} catch (Exception ee) { ee.printStackTrace(); }
				}

				if (nodes.contains(node)) {
					// System.out.println("It's a node: "+node);
					try {
					updateNodeLocation(networkView, nodeView);
					} catch (Exception ee) { ee.printStackTrace(); }
				}
			}
		}
		cyEventHelper.flushPayloadEvents(); // Do we need to update the view?
	}

	public void handleEvent(SessionLoadedEvent e) {
		groupMap = new HashMap<>();
		nodeMap = new HashMap<>();
		node2GroupMap = new HashMap<>();
	}

	public void addGroup(CyGroup group, CyNetworkView networkView) {
		if (!groupMap.containsKey(networkView))
			groupMap.put(networkView, new HashSet<CyNode>());

		groupMap.get(networkView).add(group.getGroupNode());

		if (!nodeMap.containsKey(networkView))
			nodeMap.put(networkView, new HashSet<CyNode>());

		if (!node2GroupMap.containsKey(networkView))
			node2GroupMap.put(networkView, new HashMap<CyNode, CyGroup>());

		Map<CyNode, CyGroup> node2Group = node2GroupMap.get(networkView);

		for (CyNode node: group.getNodeList()) {
			// If we're already associated with a group, don't add ourselves to
			// another one
			if (!node2Group.containsKey(node)) {
				nodeMap.get(networkView).add(node);
				node2Group.put(node, group);
			}
		}
	}

	public void removeGroup(CyGroup group, CyNetworkView networkView) {
		if (groupMap.containsKey(networkView)) {
			Set<CyNode> groups = groupMap.get(networkView);
			groups.remove(group.getGroupNode());
			if (groups.size() == 0)
				groupMap.remove(networkView);
		}

		Map<CyNode, CyGroup> node2Group = null;
		if (node2GroupMap.containsKey(networkView))
			node2Group = node2GroupMap.get(networkView);

		if (node2Group != null)
			node2Group.remove(group.getGroupNode());

		if (nodeMap.containsKey(networkView)) {
			Set<CyNode> nodes = nodeMap.get(networkView);
			for (CyNode node: group.getNodeList()) {
				nodes.remove(node);
				if (node2Group != null)
					node2Group.remove(node);
			}

			if (nodes.size() == 0)
				nodeMap.remove(networkView);
		}
	}

	private void updateGroupLocation(CyNetworkView networkView, View<CyNode> nodeView) {
		double groupX = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
		double groupY = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
		CyGroup group = cyGroupManager.getGroup(nodeView.getModel(), networkView.getModel());

		//System.out.println("Updating group "+group+" location");
		Dimension lastPosition = ViewUtils.getLocation(networkView.getModel(), group);
		double xOffset = lastPosition.getWidth() - groupX;
		double yOffset = lastPosition.getHeight() - groupY;

		// System.out.println("Group node offset = "+xOffset+","+yOffset);
		List<View<CyNode>> groupNodeList = new ArrayList<>();

		boolean lastIgnoreChanges = ignoreChanges;
		ignoreChanges = true;
		// OK, move all of our nodes
		for (CyNode node: group.getNodeList()) {
			View<CyNode> nv = networkView.getNodeView(node);
			if (nv == null) continue;

			double x = nv.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
			double y = nv.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
			// System.out.println("Node location = "+x+","+y);
			Dimension d = new Dimension();
			// System.out.println("Moving node to "+(x-xOffset)+","+(y-yOffset));
			d.setSize(x-xOffset, y-yOffset);
			ViewUtils.moveNode(networkView, node, d);
			if (cyGroupManager.isGroup(node, networkView.getModel()))
				groupNodeList.add(nv);
		}
		cyEventHelper.flushPayloadEvents();
		ignoreChanges = lastIgnoreChanges;

		ViewUtils.updateGroupLocation(networkView.getModel(), group, groupX, groupY);
		if (groupNodeList.size() > 0) {
			for (View<CyNode> nv: groupNodeList) {
				updateGroupLocation(networkView, nv);
			}
		}
		cyEventHelper.flushPayloadEvents();
	}

	private void updateNodeLocation(CyNetworkView networkView, View<CyNode> nodeView) {
		//System.out.println("Updating node "+nodeView.getModel()+" location");
		// double groupX = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
		// double groupY = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
		if (nodeView == null)
			return;

		CyNode node = nodeView.getModel();

		// System.out.println("UpdateNodeLocation for: "+node);
		CyGroup group = node2GroupMap.get(networkView).get(node);
		if (group == null) {
			// System.out.println("Lost group: "+node);
			group = getGroupForNode(node, networkView);
			if (group == null)
				return;
			else
				node2GroupMap.get(networkView).put(node, group);
		}

		if (cyStyleManager == null)
			cyStyleManager = cyGroupManager.getService(VisualMappingManager.class);

		boolean lastIgnoreChanges = ignoreChanges;
		ignoreChanges = true;
		ViewUtils.styleCompoundNode(group, networkView, cyGroupManager, cyStyleManager, 
		                            cyGroupSettings.getGroupViewType(group));
		cyEventHelper.flushPayloadEvents();
		ignoreChanges = lastIgnoreChanges;

		// OK, a little trickery here.  If our group is itself a node in another group, we
		// won't restyle appropriately because we were ignoring changes.  Deal with it here,
		// but only if our group is also a node in another group
		List<CyGroup> groupList = cyGroupManager.getGroupsForNode(group.getGroupNode());
		if (groupList != null || groupList.size() > 0) {
			// updateNodeLocation(networkView, networkView.getNodeView(group.getGroupNode()));
			CyGroup g = getGroupForNode(group.getGroupNode(), networkView);
			if (g != null) {
				ViewUtils.styleCompoundNode(g, networkView, cyGroupManager, cyStyleManager, 
		                                cyGroupSettings.getGroupViewType(group));
				cyEventHelper.flushPayloadEvents();
			}
		}
	}

	private CyGroup getGroupForNode(CyNode node, CyNetworkView networkView) {
		CyGroup group = null;
		List<CyGroup> groupList = cyGroupManager.getGroupsForNode(node);
		if (groupList == null || groupList.size() == 0)
			return null; // Shouldn't happen!

		Set<CyNode> groupNodes = groupMap.get(networkView);
		for (CyGroup g: groupList) {
			if (groupNodes.contains(g.getGroupNode())) {
				group = g;
			}
		}
		return group;
	}
}
