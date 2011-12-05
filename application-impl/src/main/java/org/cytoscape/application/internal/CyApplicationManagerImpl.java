/*
 File: CyApplicationManagerImpl.java

 Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.application.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentRenderingEngineEvent;
import org.cytoscape.application.events.SetSelectedNetworkViewsEvent;
import org.cytoscape.application.events.SetSelectedNetworksEvent;

import org.cytoscape.event.CyEventHelper;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.presentation.RenderingEngine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * An implementation of CyApplicationManager.
 */
public class CyApplicationManagerImpl implements CyApplicationManager,
                                                 NetworkAboutToBeDestroyedListener,
                                                 NetworkViewAboutToBeDestroyedListener {
	private static final Logger logger = LoggerFactory.getLogger(CyApplicationManagerImpl.class);
	private final CyEventHelper cyEventHelper;
	private final CyNetworkManager networkManager;
	private final CyNetworkViewManager networkViewManager;
	private final List<CyNetworkView> selectedNetworkViews;
	private final List<CyNetwork> selectedNetworks;

	// Trackers for current network object
	private CyNetwork currentNetwork;
	private CyNetworkView currentNetworkView;
	private RenderingEngine<CyNetwork> currentRenderer;
	private CyTable currentTable;

	public CyApplicationManagerImpl(final CyEventHelper cyEventHelper,
	                                final CyNetworkManager networkManager,
	                                final CyNetworkViewManager networkViewManager) {
		this.cyEventHelper = cyEventHelper;
		this.networkManager = networkManager;
		this.networkViewManager = networkViewManager;

		selectedNetworkViews = new LinkedList<CyNetworkView>();
		selectedNetworks = new LinkedList<CyNetwork>();

		currentNetwork = null;
		currentNetworkView = null;
		this.currentRenderer = null;
	}

	public void handleEvent(final NetworkAboutToBeDestroyedEvent event) {
		final CyNetwork toBeDestroyed = event.getNetwork();
		boolean changed = false;

		synchronized (this) {
			if (toBeDestroyed == currentNetwork) {
				changed = true;
				currentNetwork = null;
				currentNetworkView = null;

				final Set<CyNetworkView> networkViews = networkViewManager.getNetworkViewSet();

				for (final CyNetworkView view : networkViews) {
					if (view.getModel() != toBeDestroyed) {
						currentNetworkView = view;
						currentNetwork = view.getModel();

						break;
					}
				}

				if (currentNetwork == null) {
					final Set<CyNetwork> networks = networkManager.getNetworkSet();

					for (final CyNetwork network : networks) {
						if (network != toBeDestroyed) {
							currentNetwork = network;

							break;
						}
					}
				}
			}
		}

		if (changed) {
			cyEventHelper.fireEvent(new SetCurrentNetworkViewEvent(this, currentNetworkView));
		}
	}

	public void handleEvent(final NetworkViewAboutToBeDestroyedEvent event) {
		final CyNetworkView toBeDestroyed = event.getNetworkView();
		boolean changed = false;

		synchronized (this) {
			if (toBeDestroyed == currentNetworkView) {
				changed = true;
				currentNetworkView = null;

				final Set<CyNetworkView> networkViews = networkViewManager.getNetworkViewSet();

				for (final CyNetworkView view : networkViews) {
					if (view != toBeDestroyed) {
						currentNetworkView = view;
						currentNetwork = view.getModel();

						break;
					}
				}

				if (currentNetwork == null) {
					final Set<CyNetwork> networks = networkManager.getNetworkSet();

					for (final CyNetwork network : networks) {
						if (network != toBeDestroyed.getModel()) {
							currentNetwork = network;

							break;
						}
					}
				}
			}
		}

		if (changed) {
			cyEventHelper.fireEvent(new SetCurrentNetworkViewEvent(this, currentNetworkView));
		}
	}

	public synchronized CyNetwork getCurrentNetwork() {
		return currentNetwork;
	}

	public void setCurrentNetwork(final CyNetwork network) {
		final long networkId = network.getSUID();
		synchronized (this) {
			if (!networkManager.networkExists(networkId))
				throw new IllegalArgumentException("Network is not registered in this ApplicationManager: ID = "
				                                   + networkId);

			logger.info("Set current network called.  Current network ID = " + networkId);
			currentNetwork = network; 
			currentNetworkView = networkViewManager.getNetworkView(networkId);

			// reset selected networks
			selectedNetworks.clear();
			selectedNetworks.add(currentNetwork);
		}

		logger.debug("Current network is set.  Firing SetCurrentNetworkEvent: Network ID = "
		             + networkId);
		cyEventHelper.fireEvent(new SetCurrentNetworkEvent(this, currentNetwork));
	}

	public synchronized CyNetworkView getCurrentNetworkView() {
		return currentNetworkView;
	}

	public void setCurrentNetworkView(final CyNetworkView view) {
		if (view == null) {
			logger.warn("View was null - not setting current network view.");
			return;
		}

		synchronized (this) {
			if (!networkManager.networkExists(view.getModel().getSUID()))
				throw new IllegalArgumentException("network is not recognized by this ApplicationManager");

			logger.debug("Set current network view called: View ID = " + view.getSUID());

			setCurrentNetwork(view.getModel());

			// reset selected network views
			selectedNetworkViews.clear();
			selectedNetworkViews.add(currentNetworkView);
		}

		logger.debug("Current network view is set.  Firing SetCurrentNetworkViewEvent: View ID = "
		             + view.getSUID());
		cyEventHelper.fireEvent(new SetCurrentNetworkViewEvent(this, currentNetworkView));
	}

	public synchronized List<CyNetworkView> getSelectedNetworkViews() {
		return new ArrayList<CyNetworkView>(selectedNetworkViews);
	}

	public void setSelectedNetworkViews(final List<CyNetworkView> networkViews) {
		if (networkViews == null)
			return;

		synchronized (this) {
			selectedNetworkViews.clear();
			selectedNetworkViews.addAll(networkViews);

			CyNetworkView cv = getCurrentNetworkView();

			if (!selectedNetworkViews.contains(cv)) {
				selectedNetworkViews.add(cv);
			}
		}

		cyEventHelper.fireEvent(new SetSelectedNetworkViewsEvent(this,
		                                                         new ArrayList<CyNetworkView>(selectedNetworkViews)));
	}

	public synchronized List<CyNetwork> getSelectedNetworks() {
		return new ArrayList<CyNetwork>(selectedNetworks);
	}

	public void setSelectedNetworks(final List<CyNetwork> networks) {
		if (networks == null)
			return;

		synchronized (this) {
			selectedNetworks.clear();
			selectedNetworks.addAll(networks);

			CyNetwork cn = currentNetwork;

			if (!selectedNetworks.contains(cn))
				selectedNetworks.add(cn);
		}

		cyEventHelper.fireEvent(new SetSelectedNetworksEvent(this,
		                                                     new ArrayList<CyNetwork>(selectedNetworks)));
	}

	public RenderingEngine<CyNetwork> getCurrentRenderingEngine() {
		return currentRenderer;
	}

	public void setCurrentRenderingEngine(RenderingEngine<CyNetwork> engine) {
		this.currentRenderer = engine;

		cyEventHelper.fireEvent(new SetCurrentRenderingEngineEvent(this, this.currentRenderer));
	}

	@Override
	public CyTable getCurrentTable() {
		return currentTable;
	}

	@Override
	public void setCurrentTable(CyTable table) {
		currentTable = table;
	}
}
