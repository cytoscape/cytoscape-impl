package org.cytoscape.group.session.restore.shim.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.session.CySession;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionEventsListener implements SessionLoadedListener, SessionAboutToBeSavedListener {

	private final CyGroupFactory groupFactory;
	private final CyGroupManager groupMgr;
	private final CyNetworkManager netMgr;
	private final CyRootNetworkManager rootNetMgr;
	private final String EXTERNAL_EDGE_ATTRIBUTE="__externalEdges";
	
	private static final Logger logger = LoggerFactory.getLogger(SessionEventsListener.class);
	
	public SessionEventsListener(final CyGroupFactory groupFactory,
								 final CyGroupManager groupMgr,
								 final CyNetworkManager netMgr,
								 final CyRootNetworkManager rootNetMgr) {
		this.groupFactory = groupFactory;
		this.groupMgr = groupMgr;
		this.netMgr = netMgr;
		this.rootNetMgr = rootNetMgr;
	}

	@Override
	public void handleEvent(SessionLoadedEvent e) {
		final CySession sess = e.getLoadedSession();

		if (sess != null) {
			final Set<CyNetwork> networks = sess.getNetworks();

			for (final CyNetwork net : networks) {
				recreateGroups(net, sess);
			}
		}
	}
	
	private void recreateGroups(final CyNetwork net, final CySession sess) {
		// Look for possible meta-nodes by inspecting the groups metadata in the network's hidden table
		final CyRootNetwork rootNet = rootNetMgr.getRootNetwork(net);
		final List<CyNode> nodes = net.getNodeList();

		// Iterate each node and check if they have network pointers
		for (final CyNode n : nodes) {
			final CyNetwork netPointer = n.getNetworkPointer();
			if (netPointer == null) continue;
			
			// Retrieve the internal nodes and edges
			final CyRow dnRow = net.getRow(n, CyNetwork.DEFAULT_ATTRS);
			final CyRow hnRow = net.getRow(n, CyNetwork.HIDDEN_ATTRS);
			final CyRow rnRow = rootNet.getRow(n, CyNetwork.HIDDEN_ATTRS);
			if (!dnRow.isSet("__groupState") && !hnRow.isSet("__groupState")
			    && !rnRow.isSet("__groupCollapsed"))
				return;

			boolean collapsed = false;

			if (hnRow.isSet("__groupState")) {
				Integer grState = hnRow.get("__groupState", Integer.class); // 2.x metadata
				if (grState.intValue() == 2) collapsed = true;
			} else if (dnRow.isSet("__groupState")) {
				Integer grState = dnRow.get("__groupState", Integer.class); // 2.x metadata
				if (grState.intValue() == 2) collapsed = true;
			} 
			
			// Create the group
			final CyGroup group = groupFactory.createGroup(net, n, true);

			// Add in the missing external edges if we're collapsed
			// CyRow groupNodeRow = net.getRow(n, CyNetwork.HIDDEN_ATTRS);
			if (rnRow.isSet(EXTERNAL_EDGE_ATTRIBUTE)) {
				Class<?> listType = rnRow.getTable().getColumn(EXTERNAL_EDGE_ATTRIBUTE).getListElementType();
				List<?> externalIDs = rnRow.getList(EXTERNAL_EDGE_ATTRIBUTE, listType);
				List<CyEdge> externalEdges = new ArrayList<CyEdge>();
				for (Object oldId: externalIDs) {
					CyEdge newEdge = null;
					
					if (oldId instanceof Long) // Cy3 old edge IDs are SUIDs
						newEdge = sess.getObject((Long)oldId, CyEdge.class);
					else // Cy2 uses edge labels as IDs
						newEdge = sess.getObject(oldId.toString(), CyEdge.class);
						
					if (newEdge != null)
						externalEdges.add(newEdge);
				}
				group.addEdges(externalEdges);
			}
			
			// TODO: restore group's settings
			// final boolean nestedNetVisible = dnRow.get("nested_network_is_visible", Boolean.class, Boolean.FALSE);
//			groupSettings.setUseNestedNetworks(nestedNetVisible, group);
			
			// So the group node is removed from the network when it is expanded:
			// TODO: Shouldn't the group factory automatically remove the provided group node, since the initial state
			// should be the same as expanded?
			/*
			group.collapse(net);

			if (!collapsed) {
				// Expanded...
				group.expand(net);
			}
			*/
		}
	}

	public void handleEvent(SessionAboutToBeSavedEvent e) {
		// Get all of our networks
		Set<CyNetwork> networkSet = netMgr.getNetworkSet();
		for (CyNetwork net: networkSet) {
			// Get the root network
			CyRootNetwork rootNetwork = ((CySubNetwork)net).getRootNetwork();
			// Get all of our groups
			Set<CyGroup>groupSet = groupMgr.getGroupSet(net);

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
}
