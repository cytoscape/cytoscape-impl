package org.cytoscape.io.internal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

// TODO: this is a temporary solution, until we find a better way of saving/restoring groups
public class GroupUtil {

	// 3.x group attributes
	private final String EXTERNAL_EDGE_ATTRIBUTE="__externalEdges.SUID";
	private final String GROUP_COLLAPSED_ATTRIBUTE="__groupCollapsed";
	// 2.x group attributes
	private final String GROUP_STATE_ATTRIBUTE="__groupState";
	private final String GROUP_ISLOCAL_ATTRIBUTE="__groupIsLocal";
	private final String GROUP_NODEX_ATTRIBUTE="__metanodeHintX";
	private final String GROUP_NODEY_ATTRIBUTE="__metanodeHintY";
	private final String X_LOCATION_ATTR="__xLocation";
	private final String Y_LOCATION_ATTR="__yLocation";
	private final String X_OFFSET_ATTR="__xOffset";
	private final String Y_OFFSET_ATTR="__yOffset";
	
	private final CyGroupManager groupMgr;
	private final CyGroupFactory groupFactory;
	
	public GroupUtil(final CyGroupManager groupMgr, final CyGroupFactory groupFactory) {
		assert groupMgr != null;
		assert groupFactory != null;
		
		this.groupMgr = groupMgr;
		this.groupFactory = groupFactory;
	}

	public void prepareGroupsForSerialization(final Set<CyNetwork> networks) {
		if (networks == null)
			return;
		
		for (CyNetwork net: networks) {
			if (!(net instanceof CySubNetwork))
				continue;
			
			// Get the root network
			CyRootNetwork rootNetwork = ((CySubNetwork)net).getRootNetwork();
			// Get all of our groups
			Set<CyGroup> groupSet = groupMgr.getGroupSet(net);

			// For each group, save the list of external edges
			for (CyGroup group: groupSet) {
				// Get the group node
				CyNode groupNode = group.getGroupNode();

				// Get the list of external edges
				Set<CyEdge> externalEdges = group.getExternalEdgeList();

				// Save the SUIDs for each edge
				List<Long> externalEdgeSUIDs = new ArrayList<Long>();
				for (CyEdge edge: externalEdges) {
					externalEdgeSUIDs.add(edge.getSUID());
				}

				// We need to use the root network to get the row because the group might be expanded
				CyRow groupNodeRow = rootNetwork.getRow(groupNode, CyNetwork.HIDDEN_ATTRS);

				// Make sure our column exists
				CyTable hiddenNodeTable = groupNodeRow.getTable();
				if (hiddenNodeTable.getColumn(EXTERNAL_EDGE_ATTRIBUTE) == null) {
					hiddenNodeTable.createListColumn(EXTERNAL_EDGE_ATTRIBUTE, Long.class, false);
				}

				groupNodeRow.set(EXTERNAL_EDGE_ATTRIBUTE, externalEdgeSUIDs);
			}
		}
	}
	
	/**
	 * Make sure all network pointers are already set.
	 * @param networks
	 */
	public void createGroups(final Set<CyNetwork> networks, final Set<CyNetworkView> viewSet) {
		if (networks == null) return;
		
		for (final CyNetwork net : networks) {
			if (net instanceof CySubNetwork)
				createGroups((CySubNetwork) net, viewSet);
		}
	}

	public void createGroups(final CySubNetwork net, final Set<CyNetworkView> viewSet) {
		// Look for possible meta-nodes by inspecting the groups metadata in the network's hidden table
		final CyRootNetwork rootNet = net.getRootNetwork();
		final List<CyNode> nodes = net.getNodeList();

		// Iterate each node and check if they have network pointers
		for (final CyNode n : nodes) {
			final CyNetwork netPointer = n.getNetworkPointer();
			if (netPointer == null) continue;
			
			// Retrieve the internal nodes and edges
			final CyRow dnRow = net.getRow(n, CyNetwork.DEFAULT_ATTRS);
			final CyRow hnRow = net.getRow(n, CyNetwork.HIDDEN_ATTRS);
			final CyRow rnRow = rootNet.getRow(n, CyNetwork.HIDDEN_ATTRS);
			if (!dnRow.isSet(GROUP_STATE_ATTRIBUTE) && !hnRow.isSet(GROUP_STATE_ATTRIBUTE)
			    && !rnRow.isSet(GROUP_COLLAPSED_ATTRIBUTE))
				return;

			boolean collapsed = false;
			boolean cy2group = false;

			if (hnRow.isSet(GROUP_STATE_ATTRIBUTE)) {
				Integer grState = hnRow.get(GROUP_STATE_ATTRIBUTE, Integer.class); // 2.x metadata
				cy2group = true;
				if (grState.intValue() == 2) collapsed = true;
			} else if (dnRow.isSet(GROUP_STATE_ATTRIBUTE)) {
				Integer grState = dnRow.get(GROUP_STATE_ATTRIBUTE, Integer.class); // 2.x metadata
				cy2group = true;
				if (grState.intValue() == 2) collapsed = true;
			} 

			// Check to make sure the column exists
			if (rnRow.getTable().getColumn(GROUP_COLLAPSED_ATTRIBUTE) == null) {
				rnRow.getTable().createListColumn(GROUP_COLLAPSED_ATTRIBUTE, String.class, false);
			}

			if (!rnRow.isSet(GROUP_COLLAPSED_ATTRIBUTE)) {
				// This is a 2.x group.  We need to recreate the 3.x structure
				List<String> collapsedList = rnRow.getList(GROUP_COLLAPSED_ATTRIBUTE, String.class);
				if (collapsedList == null) collapsedList = new ArrayList<String>();

				String netName = net.getDefaultNetworkTable().getRow(net.getSUID()).get(CyNetwork.NAME, String.class);
				
				if (netName == null)
					netName = "(null)";

				collapsedList.add(netName+":"+collapsed);
				rnRow.set(GROUP_COLLAPSED_ATTRIBUTE, collapsedList);
			}
			
			// Create the group
			final CyGroup group = groupFactory.createGroup(net, n, true);

			// Add in the missing external edges if we're collapsed
			// CyRow groupNodeRow = net.getRow(n, CyNetwork.HIDDEN_ATTRS);
			if (rnRow.isSet(EXTERNAL_EDGE_ATTRIBUTE)) {
				List<Long> externalIDs = rnRow.getList(EXTERNAL_EDGE_ATTRIBUTE, Long.class);
				List<CyEdge> externalEdges = new ArrayList<CyEdge>();
				
				for (Long suid: externalIDs) {
					CyEdge newEdge = rootNet.getEdge(suid);
					if (newEdge != null)
						externalEdges.add(newEdge);
				}
				
				group.addEdges(externalEdges);
			}
			
			/*
			// Disable nested network
			final boolean nestedNetVisible = dnRow.get("nested_network_is_visible", Boolean.class, Boolean.FALSE);
			if (dnRow.getTable().getColumn("nested_network_is_visible") == null) {
				dnRow.getTable().createColumn("nested_network_is_visible", Boolean.class, false);
			}
			dnRow.set("nested_network_is_visible", Boolean.FALSE);
			*/
			
			// TODO: restore group's settings
			// Some special handling for cy2 groups.  We need to restore the X,Y coordinates of the nodes
			// if we're collapsed.
			if (cy2group) {
				// Copy all of the tables for the group
				copyTables(netPointer, net, n);
// TODO add groups to a cached map: xgmmlreader has to access all groups and update their node locations when creating new views
				// If the group is collapsed, we need to provide the location information so we can expand it
				if (viewSet != null) {
					if (collapsed) {
						for (CyNetworkView view : viewSet) {
							if (view.getModel().equals(net))
								updateGroupNodeLocation(view, n);
						}
					}
				}
			}
		}

		// TODO: If this is a 2.x group, clean up
		if (net.getDefaultNodeTable().getColumn(GROUP_STATE_ATTRIBUTE) != null)
			net.getDefaultNodeTable().deleteColumn(GROUP_STATE_ATTRIBUTE);
		if (net.getDefaultNodeTable().getColumn(GROUP_ISLOCAL_ATTRIBUTE) != null) 
			net.getDefaultNodeTable().deleteColumn(GROUP_ISLOCAL_ATTRIBUTE);
	}
	
	public void updateGroupNodes(final CyNetworkView view) {
		if (view == null) return;
		
		CyNetwork network = view.getModel();
		Set<CyGroup> groupSet = groupMgr.getGroupSet(network);
		
		for (CyGroup group : groupSet) {
			if (group.isCollapsed(network))
				updateGroupNodeLocation(view, group.getGroupNode());
		}
	}
	
	private void copyTables(CyNetwork netPointer, CyNetwork net, CyNode groupNode) {
		// Copy the tables from the subnetwork
		CyTable subTable = netPointer.getDefaultNodeTable();
		CyTable nodeTable = net.getDefaultNodeTable();

		CyTable hiddenSubTable = netPointer.getTable(CyNode.class, CyNetwork.HIDDEN_ATTRS);
		CyTable hiddenTable = net.getTable(CyNode.class, CyNetwork.HIDDEN_ATTRS);

		// The group code stores location information in the root table
		CyRootNetwork rootNetwork = ((CySubNetwork)net).getRootNetwork();
		CyTable hiddenRootTable = rootNetwork.getTable(CyNode.class, CyNetwork.HIDDEN_ATTRS);

		for (CyNode child: netPointer.getNodeList()) {
			CyRow row = netPointer.getRow(child);
			CyRow nodeRow = nodeTable.getRow(child.getSUID());
			
			for (CyColumn col: subTable.getColumns()) {
				String name = col.getName();
				if (nodeTable.getColumn(name) != null && 
				    !nodeTable.getColumn(name).getVirtualColumnInfo().isVirtual()) {
					nodeRow.set(col.getName(), row.get(name, col.getType()));
				}
			}

			CyRow hiddenRow = hiddenTable.getRow(child.getSUID());
			CyRow hiddenSubRow = hiddenSubTable.getRow(child.getSUID());
			CyRow hiddenRootRow = hiddenRootTable.getRow(child.getSUID());
			
			for (CyColumn col: hiddenSubTable.getColumns()) {
				String name = col.getName();
				
				// Look for child node location information
				if (name.equals(GROUP_NODEX_ATTRIBUTE)) {
					Double offset = hiddenSubRow.get(name, Double.class);
					createColumn(hiddenRootRow, X_OFFSET_ATTR, Double.class);
					hiddenRootRow.set(X_OFFSET_ATTR, offset);
				} else if (name.equals(GROUP_NODEY_ATTRIBUTE)) {
					Double offset = hiddenSubRow.get(name, Double.class);
					createColumn(hiddenRootRow, Y_OFFSET_ATTR, Double.class);
					hiddenRootRow.set(Y_OFFSET_ATTR, offset);
				} else if (hiddenTable.getColumn(name) != null && 
				           !hiddenTable.getColumn(name).getVirtualColumnInfo().isVirtual()) {
					hiddenSubRow.set(col.getName(), hiddenRow.get(name, col.getType()));
				}
			}
		}
	}
	
	private void updateGroupNodeLocation(final CyNetworkView view, final CyNode groupNode) {
		if (view == null) return;

		CyRootNetwork rootNetwork = ((CySubNetwork)view.getModel()).getRootNetwork();
		View<CyNode> nView = view.getNodeView(groupNode);
		double x = nView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
		double y = nView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
		
		// Save it
		// TODO these attributes will not work with multiple views per network!
		CyRow hRow = rootNetwork.getRow(groupNode, CyNetwork.HIDDEN_ATTRS);
		createColumn(hRow, X_LOCATION_ATTR, Double.class);
		createColumn(hRow, Y_LOCATION_ATTR, Double.class);
		hRow.set(X_LOCATION_ATTR, new Double(x));
		hRow.set(Y_LOCATION_ATTR, new Double(y));
	}
	
	private void createColumn(CyRow row, String column, Class<?> type) {
		CyTable table = row.getTable();
		
		if (table.getColumn(column) == null)
			table.createColumn(column, type, false);
	}
}
