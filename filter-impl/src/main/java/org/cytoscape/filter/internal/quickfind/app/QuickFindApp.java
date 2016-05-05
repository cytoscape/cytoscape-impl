package org.cytoscape.filter.internal.quickfind.app;

/*
 * #%L
 * Cytoscape Filters Impl (filter-impl)
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


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.filter.internal.filters.util.SelectUtil;
import org.cytoscape.filter.internal.filters.util.VisualPropertyUtil;
import org.cytoscape.filter.internal.prefuse.data.query.NumberRangeModel;
import org.cytoscape.filter.internal.quickfind.util.QuickFind;
import org.cytoscape.filter.internal.quickfind.util.QuickFindListener;
import org.cytoscape.filter.internal.quickfind.util.TaskMonitorBase;
import org.cytoscape.filter.internal.widgets.autocomplete.index.GenericIndex;
import org.cytoscape.filter.internal.widgets.autocomplete.index.Hit;
import org.cytoscape.filter.internal.widgets.autocomplete.index.NumberIndex;
import org.cytoscape.filter.internal.widgets.autocomplete.view.TextIndexComboBox;
import org.cytoscape.filter.internal.widgets.slider.JRangeSliderExtended;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.events.AddedEdgesEvent;
import org.cytoscape.model.events.AddedEdgesListener;
import org.cytoscape.model.events.AddedNodesEvent;
import org.cytoscape.model.events.AddedNodesListener;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.RemovedEdgesEvent;
import org.cytoscape.model.events.RemovedEdgesListener;
import org.cytoscape.model.events.RemovedNodesEvent;
import org.cytoscape.model.events.RemovedNodesListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;



/**
 * Quick Find App.
 *
 * @author Ethan Cerami.
 */
public class QuickFindApp implements QuickFindListener, AddedEdgesListener,
					AddedNodesListener, RemovedEdgesListener,
					RemovedNodesListener, NetworkAddedListener,
					NetworkAboutToBeDestroyedListener,
					NetworkViewAboutToBeDestroyedListener
{
	private static final int REINDEX_THRESHOLD = 1000;
	
	private final QuickFind quickFind;
	
	private final CyApplicationManager applicationManager;
	private final CyNetworkManager networkManager;
	private static final int NODE_SIZE_MULTIPLER = 10;

	
	public QuickFindApp(final QuickFind quickFind, final CyApplicationManager applicationManager, final CyNetworkManager networkManager) {
		this.applicationManager = applicationManager;
		this.networkManager = networkManager;
		this.quickFind = quickFind;
		
		initListeners();
	}

	/**
	 * Initializes All Cytoscape Listeners.
	 */
	private void initListeners() {
		quickFind.addQuickFindListener(this);
	}


	@Override
	public void handleEvent(NetworkViewAboutToBeDestroyedEvent e) {
		final CyNetworkView networkView = e.getNetworkView();
		final CyNetwork cyNetwork = networkView.getModel();
		quickFind.removeNetwork(cyNetwork);
	}

	/**
	 * Event:  Network Added to Index.
	 *
	 * @param network CyNetwork Object.
	 */
	@Override
	public void networkAddedToIndex(CyNetwork network) {
		//  No-op
	}

	/**
	 * Event:  Network Removed from Index.
	 *
	 * @param network CyNetwork Object.
	 */
	@Override
	public void networkRemovedfromIndex(CyNetwork network) {
		//  No-op
	}

	@Override
	public void indexingStarted(CyNetwork cyNetwork, int indexType, String controllingAttribute)
	{
		// No-op
	}

	
	@Override
	public void indexingEnded() {
		// No-op
	}

	/**
	 * Indicates that the user has selected a text item within the QuickFind
	 * Search Box.
	 *
	 * @param network the current CyNetwork.
	 * @param hit     hit value chosen by the user.
	 */
	public void onUserSelection(final CyNetwork network, Hit hit) {
		SelectUtil.unselectAllNodes(network);
		SelectUtil.unselectAllEdges(network);

		//  Assemble Hit Objects
		final Object[] graphObjects = hit.getAssociatedObjects();
		final List list = new ArrayList();

		for (int i = 0; i < graphObjects.length; i++) {
			list.add(graphObjects[i]);
		}

		//  Fit Selected Content
		SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					final CyNetwork cyNetwork = applicationManager.getCurrentNetwork();
					CyNetworkView networkView = applicationManager.getCurrentNetworkView();
					GenericIndex index = quickFind.getIndex(cyNetwork);

					if (index.getIndexType() == QuickFind.INDEX_NODES) {
						SelectUtil.setSelectedNodeState(network, list, true);
						final CyNetworkView currentView = applicationManager.getCurrentNetworkView();
						
						if (currentView != null)
							currentView.fitSelected();
					} else {
						SelectUtil.setSelectedEdgeState(network, list, true);

						List<CyNode> nodeList = new ArrayList<>();

						for (int i = 0; i < list.size(); i++) {
							CyEdge edge = (CyEdge) list.get(i);
							CyNode sourceNode = edge.getSource();
							CyNode targetNode = edge.getTarget();
							nodeList.add(sourceNode);
							nodeList.add(targetNode);
						}

						SelectUtil.setSelectedNodeState(network, nodeList, true);
						networkView.fitSelected();
					}

					//  If only one node is selected, auto-adjust zoom factor
					//  so that node does not take up whole screen.
					if (graphObjects.length == 1) {
						if (graphObjects[0] instanceof CyNode) {
							CyNode node = (CyNode) graphObjects[0];

							//  Obtain dimensions of current InnerCanvas
							RenderingEngine<CyNetwork> engine = applicationManager.getCurrentRenderingEngine();
							VisualLexicon lexicon = engine.getVisualLexicon();
							
							Double networkWidth = VisualPropertyUtil.get(lexicon, networkView, "NETWORK_WIDTH", BasicVisualLexicon.NETWORK, Double.class);
							Double networkHeight = VisualPropertyUtil.get(lexicon, networkView, "NETWORK_HEIGHT", BasicVisualLexicon.NETWORK, Double.class);
							
							View<CyNode> nodeView = networkView.getNodeView(node);

							Double nodeWidth = VisualPropertyUtil.get(lexicon, nodeView, "NODE_WIDTH", BasicVisualLexicon.NODE, Double.class);
							Double nodeHeight = VisualPropertyUtil.get(lexicon, nodeView, "NODE_HEIGHT", BasicVisualLexicon.NODE, Double.class);
							double width = nodeWidth * NODE_SIZE_MULTIPLER;
							double height = nodeHeight * NODE_SIZE_MULTIPLER;
							double scaleFactor = Math.min(networkWidth / width,
							                              (networkHeight / height));
							
							// TODO: How do we set the zoom in the new API?
							//networkView.setZoom(scaleFactor);
						}
					}

					networkView.updateView();
				}

			});
	}

	/**
	 * Indicates that the user has selected a number range within the QuickFind
	 * Range selector.
	 *
	 * @param network   the current CyNetwork.
	 * @param lowValue  the low value of the range.
	 * @param highValue the high value of the range.
	 */
	public void onUserRangeSelection(CyNetwork network, Number lowValue, Number highValue) {
		try {
			GenericIndex index = quickFind.getIndex(network);
			NumberIndex numberIndex = (NumberIndex) index;
			final List rangeList = numberIndex.getRange(lowValue, highValue);

			if (index.getIndexType() == QuickFind.INDEX_NODES) {
				selectNodes(network, rangeList);
			} else {
				selectEdges(network, rangeList);
			}
		} catch (IllegalArgumentException exc) {
		}
	}

	private void selectNodes(final CyNetwork cyNetwork, List<CyNode> rangeList) {
		//  First, do we have any edges selected?  If so, unselect them all
		List<CyEdge> selectedEdgeSet = SelectUtil.getSelectedEdges(cyNetwork);

		if (selectedEdgeSet.size() > 0) {
			SelectUtil.setSelectedEdgeState(cyNetwork, selectedEdgeSet, false);
		}

		//  Then, determine the current set of selected nodes
		List<CyNode> selectedNodeSet = SelectUtil.getSelectedNodes(cyNetwork);

		//  Then, figure out which new nodes to select
		//  This is the set operation:  R - S
		final Set<CyNode> toBeSelected = new HashSet<>(rangeList);
		toBeSelected.removeAll(selectedNodeSet);

		//  Then, figure out which current nodes to unselect
		//  This is the set operation:  S - R
		final Set<CyNode> toBeUnselected = new HashSet<>(selectedNodeSet);
		toBeUnselected.removeAll(rangeList);

		SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					SelectUtil.setSelectedNodeState(cyNetwork, toBeSelected, true);
					SelectUtil.setSelectedNodeState(cyNetwork, toBeUnselected, false);
					final CyNetworkView currentView = applicationManager.getCurrentNetworkView();
					
					if (currentView != null)
						currentView.updateView();
				}
			});
	}

	private void selectEdges(final CyNetwork cyNetwork, List<CyEdge> rangeList) {
		//  First, do we have any nodes selected?  If so, unselect them all
		List<CyNode> selectedNodeSet = SelectUtil.getSelectedNodes(cyNetwork);

		if (selectedNodeSet.size() > 0) {
			SelectUtil.setSelectedNodeState(cyNetwork, selectedNodeSet, false);
		}

		//  Then, determine the current set of selected edge
		List<CyEdge> selectedEdgeSet = SelectUtil.getSelectedEdges(cyNetwork);

		//  Then, figure out which new nodes to select
		//  This is the set operation:  R - S
		final Set<CyEdge> toBeSelected = new HashSet<>(rangeList);
		toBeSelected.removeAll(selectedEdgeSet);

		//  Then, figure out which current nodes to unselect
		//  This is the set operation:  S - R
		final Set<CyEdge> toBeUnselected = new HashSet<>(selectedEdgeSet);
		toBeUnselected.removeAll(rangeList);

		SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					SelectUtil.setSelectedEdgeState(cyNetwork, toBeSelected, true);
					SelectUtil.setSelectedEdgeState(cyNetwork, toBeUnselected, false);
					final CyNetworkView currentView = applicationManager.getCurrentNetworkView();
					
					if (currentView != null)
						currentView.updateView();
				}
			});
	}

	@Override
	public void handleEvent(NetworkAboutToBeDestroyedEvent e) {
		quickFind.removeNetwork(e.getNetwork());
	}

	@Override
	public void handleEvent(NetworkAddedEvent e) {
		handleNetworkModified(e.getNetwork());
	}

	@Override
	public void handleEvent(RemovedNodesEvent e) {
		handleNetworkModified(e.getSource());
	}

	@Override
	public void handleEvent(RemovedEdgesEvent e) {
		handleNetworkModified(e.getSource());
	}

	@Override
	public void handleEvent(AddedNodesEvent e) {
		handleNetworkModified(e.getSource());
	}

	@Override
	public void handleEvent(AddedEdgesEvent e) {
		handleNetworkModified(e.getSource());
	}

	private void handleNetworkModified(final CyNetwork cyNetwork) {
		if (!networkManager.networkExists(cyNetwork.getSUID()))
			return;
		
		if (cyNetwork.getNodeCount() > 0) {
			// this network may not have been added to quick find - 
			// this can happen if an empty network was added
			if (quickFind.getIndex(cyNetwork) == null) {
				//  Run Indexer in separate background daemon thread.
				Thread thread = new Thread() {
						public void run() {
							quickFind.addNetwork(cyNetwork, new TaskMonitorBase());
						}
					};
				thread.start();
			}
			//  Only re-index if network has fewer than REINDEX_THRESHOLD nodes
			//  I put this in to prevent quick find from auto re-indexing
			//  very large networks.  
			else if (cyNetwork.getNodeCount() < QuickFindApp.REINDEX_THRESHOLD) {
				//  Run Indexer in separate background daemon thread.
				Thread thread = new Thread() {
						public void run() {
							GenericIndex index = quickFind.getIndex(cyNetwork);
							quickFind.reindexNetwork(cyNetwork, index.getIndexType(),
										 index.getControllingAttribute(), new TaskMonitorBase());
						}
					};
				thread.start();
			}
		}
	}
}


/**
 * Listens for Final Selection from User.
 *
 * @author Ethan Cerami.
 */
final class UserSelectionListener implements ActionListener {
	
	private final QuickFind quickFind;
	
	private final TextIndexComboBox comboBox;
	private final CyApplicationManager applicationManager;

	/**
	 * Constructor.
	 *
	 * @param comboBox TextIndexComboBox.
	 */
	public UserSelectionListener(final QuickFind quickFind, TextIndexComboBox comboBox, CyApplicationManager applicationManager) {
		this.comboBox = comboBox;
		this.applicationManager = applicationManager;
		this.quickFind = quickFind;
	}

	/**
	 * User has made final selection.
	 *
	 * @param e ActionEvent Object.
	 */
	public void actionPerformed(ActionEvent e) {
		//  Get Current Network
		final CyNetwork currentNetwork = applicationManager.getCurrentNetwork();

		//  Get Current User Selection
		Object o = comboBox.getSelectedItem();

		if ((o != null) && o instanceof Hit) {
			Hit hit = (Hit) comboBox.getSelectedItem();
			quickFind.selectHit(currentNetwork, hit);
		}
	}
}


/**
 * Action to select a range of nodes.
 *
 * @author Ethan Cerami.
 */
class RangeSelectionListener implements ChangeListener {
	
	private final QuickFind quickFind;
	private final JRangeSliderExtended slider;
	private final CyApplicationManager applicationManager;

	/**
	 * Constructor.
	 *
	 * @param slider JRangeSliderExtended Object.
	 */
	public RangeSelectionListener(final QuickFind quickFind, JRangeSliderExtended slider, CyApplicationManager applicationManager) {
		this.slider = slider;
		this.applicationManager = applicationManager;
		this.quickFind = quickFind;
	}

	/**
	 * State Change Event.
	 *
	 * @param e ChangeEvent Object.
	 */
	public void stateChanged(ChangeEvent e) {
		final CyNetwork cyNetwork = applicationManager.getCurrentNetwork();
		GenericIndex index = quickFind.getIndex(cyNetwork);
		NumberRangeModel model = (NumberRangeModel) slider.getModel();

		if (slider.isVisible()) {
			if (index instanceof NumberIndex) {
				Number lowValue = (Number) model.getLowValue();
				Number highValue = (Number) model.getHighValue();
				quickFind.selectRange(cyNetwork, lowValue, highValue);
			}
		}
	}
	
	
}
