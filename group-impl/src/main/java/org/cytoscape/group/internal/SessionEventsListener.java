package org.cytoscape.group.internal;

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
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.session.CySession;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionEventsListener implements NetworkAddedListener, SessionLoadedListener,
		SessionAboutToBeSavedListener {

	private final CyGroupFactory groupFactory;
	private final CyGroupManager groupMgr;
	private final CyNetworkManager netMgr;
	private final CyRootNetworkManager rootNetMgr;
	
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
	public void handleEvent(NetworkAddedEvent e) {
		// TODO: handle imported XGMML
	}
	
	@Override
	public void handleEvent(SessionAboutToBeSavedEvent e) {
		// TODO: anything to be done here?
	}

	@Override
	public void handleEvent(SessionLoadedEvent e) {
		final CySession sess = e.getLoadedSession();

		if (sess != null) {
			final Set<CyNetwork> networks = sess.getNetworks();
			
			for (final CyNetwork net : networks) {
				recreateGroups(net);
			}
		}
	}
	
	private void recreateGroups(final CyNetwork net) {
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
			final Integer grState = hnRow.get("__groupState", Integer.class); // 2.x metadata
			// TODO: also check 3.0 metadata
			
			if (grState == null) continue; // It's not a group!
			
			// TODO: copy attributes and visual properties from the old network pointer to the group network later
			// TODO: delete the network pointer
			// TODO: add groupFactory.createGroup(net, netPointer) method to avoid all this?
			
			// Create the group
			final List<CyNode> internalNodes = netPointer.getNodeList();
			final List<CyEdge> internalEdges = netPointer.getEdgeList();
			final CyGroup group = groupFactory.createGroup(net, n, internalNodes, internalEdges, true);
			
			// TODO: restore group's settings
			final boolean nestedNetVisible = dnRow.get("nested_network_is_visible", Boolean.class, Boolean.FALSE);
//			groupSettings.setUseNestedNetworks(nestedNetVisible, group);
			
			// So the group node is removed from the network when it is expanded:
			// TODO: Shouldn't the group factory automatically remove the provided group node, since the initial state
			// should be the same as expanded?
			group.collapse(net);

			if (grState == 1) {
				// Expanded...
				group.expand(net);
			}
		}
	}
}
