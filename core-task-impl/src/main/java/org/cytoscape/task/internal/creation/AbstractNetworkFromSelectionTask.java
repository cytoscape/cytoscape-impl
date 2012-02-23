/*
 File: AbstractNetworkFromSelectionTask.java

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
package org.cytoscape.task.internal.creation;


import java.util.Collection;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;


abstract class AbstractNetworkFromSelectionTask extends AbstractCreationTask {
	private final UndoSupport undoSupport;
	protected final CyRootNetworkManager rootNetworkManager;
	protected final CyNetworkViewFactory viewFactory;
	protected final VisualMappingManager vmm;
	protected final CyNetworkNaming cyNetworkNaming;
	protected final CyApplicationManager appManager;
	private final CyEventHelper eventHelper;

	public AbstractNetworkFromSelectionTask(final UndoSupport undoSupport,
	                                        final CyNetwork parentNetwork,
	                                        final CyRootNetworkManager rootNetworkManager,
	                                        final CyNetworkViewFactory viewFactory,
	                                        final CyNetworkManager netmgr,
	                                        final CyNetworkViewManager networkViewManager,
	                                        final CyNetworkNaming cyNetworkNaming,
	                                        final VisualMappingManager vmm,
	                                        final CyApplicationManager appManager,
	                                        final CyEventHelper eventHelper)
	{
		super(parentNetwork, netmgr, networkViewManager);

		this.undoSupport        = undoSupport;
		this.rootNetworkManager = rootNetworkManager;
		this.viewFactory        = viewFactory;
		this.cyNetworkNaming    = cyNetworkNaming;
		this.vmm                = vmm;
		this.appManager         = appManager;
		this.eventHelper        = eventHelper;
	}

	abstract Collection<CyEdge> getEdges(CyNetwork netx, List<CyNode> nodes);

	@Override
	public void run(TaskMonitor tm) {
		if (parentNetwork == null)
			throw new NullPointerException("Source network is null.");
		tm.setProgress(0.0);

		final CyNetworkView sourceView = networkViewManager.getNetworkView(parentNetwork);
		tm.setProgress(0.1);

		// Get the selected nodes, but only create network if nodes are actually
		// selected.
		final List<CyNode> selectedNodes = CyTableUtil.getNodesInState(parentNetwork, CyNetwork.SELECTED, true);
		tm.setProgress(0.2);

		if (selectedNodes.size() <= 0)
			throw new IllegalArgumentException("No nodes are selected!");

		// create subnetwork and add selected nodes and appropriate edges
		final CySubNetwork newNet = rootNetworkManager.getRootNetwork(parentNetwork).addSubNetwork();
		tm.setProgress(0.3);

		for (final CyNode node : selectedNodes)
			newNet.addNode(node);

		tm.setProgress(0.4);
		for (final CyEdge edge : getEdges(parentNetwork, selectedNodes))
			newNet.addEdge(edge);
		tm.setProgress(0.5);

		newNet.getRow(newNet).set(CyTableEntry.NAME, cyNetworkNaming.getSuggestedSubnetworkTitle(parentNetwork));

		networkManager.addNetwork(newNet);
		tm.setProgress(0.6);

		// Set the new network as current network
		appManager.setCurrentNetwork(newNet);
		
		// No view is available for parent network.
		if (sourceView == null) {
			// inserted first, happens second
			final Task setCurrentNetworkView = new SetCurrentNetworkViewTask(newNet); 
			insertTasksAfterCurrentTask(setCurrentNetworkView);
			
			// inserted second, happens first
			final Task createViewTask = new CreateNetworkViewTask(undoSupport, newNet, viewFactory,
			                                                      networkViewManager, null, eventHelper);
			insertTasksAfterCurrentTask(createViewTask);
			return;
		}
		tm.setProgress(0.7);

		// create new view
		final CyNetworkView newView = viewFactory.createNetworkView(newNet);

//		networkViewManager.addNetworkView(newView);
		tm.setProgress(0.8);

		// copy node location only.
		for (View<CyNode> newNodeView : newView.getNodeViews()) {
			View<CyNode> origNodeView = sourceView.getNodeView(parentNetwork.getNode(newNodeView.getModel().getIndex()));
			newNodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION,
					origNodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION));
			newNodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION,
					origNodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION));

			// FIXME
			// // Set lock (if necessary)
			// for ( VisualProperty<?> vp : vpSet ) {
			// if (origNodeView.isValueLocked(vp) )
			// newNodeView.setLockedValue(vp,
			// origNodeView.getVisualProperty(vp));
			// }
		}
		tm.setProgress(0.9);

		final VisualStyle style = vmm.getVisualStyle(sourceView);
		vmm.setVisualStyle(style, newView);
		style.apply(newView);
		newView.fitContent();
		
		networkViewManager.addNetworkView(newView);
		appManager.setCurrentNetworkView(newView);
		tm.setProgress(1.0);
	}

	private class SetCurrentNetworkViewTask extends AbstractTask {
		CyNetwork net;
		SetCurrentNetworkViewTask(CyNetwork net) {
			this.net = net;
		}
		public void run(TaskMonitor tm) {
			tm.setProgress(0.0);
			final CyNetworkView view = networkViewManager.getNetworkView(net);			
			tm.setProgress(0.5);
			if ( view != null )
				appManager.setCurrentNetworkView( view );
			tm.setProgress(1.0);
		}
	}
}
