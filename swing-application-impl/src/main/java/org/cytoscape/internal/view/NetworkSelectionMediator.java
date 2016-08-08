package org.cytoscape.internal.view;

import static org.cytoscape.internal.util.Util.equalSets;
import static org.cytoscape.internal.util.Util.getNetworkViews;
import static org.cytoscape.internal.util.Util.getNetworks;
import static org.cytoscape.internal.util.Util.same;
import static org.cytoscape.internal.util.ViewUtil.invokeOnEDT;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.events.SetSelectedNetworkViewsEvent;
import org.cytoscape.application.events.SetSelectedNetworkViewsListener;
import org.cytoscape.application.events.SetSelectedNetworksEvent;
import org.cytoscape.application.events.SetSelectedNetworksListener;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeLoadedEvent;
import org.cytoscape.session.events.SessionAboutToBeLoadedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

/**
 * This class acts as an intermediary between the CyNetwork/CyNetworkView selection events
 * and the selection of network/view entries in the UI, so they are kept in sync in a way
 * that makes sense to the end user.
 */
public class NetworkSelectionMediator
		implements SetSelectedNetworksListener, SetSelectedNetworkViewsListener, SetCurrentNetworkListener,
		SetCurrentNetworkViewListener, SessionAboutToBeLoadedListener, SessionLoadedListener {

	private boolean loadingSession;
	
	private final NetPanelPropertyChangeListener netPanelPropChangeListener;
	private final ViewPanelPropertyChangeListener viewPanelPropChangeListener;
	private final GridPanelPropertyChangeListener gridPanelPropChangeListener;
	
	private final NetworkMainPanel netMainPanel;
	private final NetworkViewMainPanel viewMainPanel;
	private final CyServiceRegistrar serviceRegistrar;
	
	private final Object lock = new Object();

	public NetworkSelectionMediator(final NetworkMainPanel netMainPanel, final NetworkViewMainPanel viewMainPanel,
			final CyServiceRegistrar serviceRegistrar) {
		this.netMainPanel = netMainPanel;
		this.viewMainPanel = viewMainPanel;
		this.serviceRegistrar = serviceRegistrar;
		
		netPanelPropChangeListener = new NetPanelPropertyChangeListener();
		viewPanelPropChangeListener = new ViewPanelPropertyChangeListener();
		gridPanelPropChangeListener = new GridPanelPropertyChangeListener();
		
		addPropertyChangeListeners();
	}

	@Override
	public void handleEvent(final SessionAboutToBeLoadedEvent e) {
		loadingSession = true;
	}
	
	@Override
	public void handleEvent(final SessionLoadedEvent e) {
		loadingSession = false;
	}
	
	@Override
	public void handleEvent(final SetCurrentNetworkEvent e) {
		final CyNetwork network = e.getNetwork();
		
		synchronized (lock) {
			if (same(network, netMainPanel.getCurrentNetwork())) // Nothing has changed!
				return;
		}
		
		invokeOnEDT(() -> {
			// Don't sync if the current network has changed again between the moment
			// the SetCurrentNetworkEvent was received and this lambda is executed on the EDT.
			if (same(network, serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork()))
				syncFrom(network);
		});
	}

	@Override
	public void handleEvent(final SetCurrentNetworkViewEvent e) {
		if (loadingSession)
			return;
		
		final CyNetworkView view = e.getNetworkView();
		
		synchronized (lock) {
			if (same(view, viewMainPanel.getCurrentNetworkView())) // Nothing has changed!
				return;
		}
		
		invokeOnEDT(() -> {
			// Don't sync if the current view has changed again between the moment
			// the SetCurrentNetworkViewEvent was received and this lambda is executed on the EDT.
			if (same(view, serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetworkView()))
				syncFrom(view);
		});
	}
	
	@Override
	public void handleEvent(final SetSelectedNetworksEvent e) {
		if (loadingSession)
			return;
		
		final List<CyNetwork> networks = e.getNetworks();
		
		synchronized (lock) {
			if (equalSets(networks, netMainPanel.getSelectedNetworks(false))) // Nothing has changed!
				return;
		}
		
		invokeOnEDT(() -> {
			// Don't sync if the list has changed again between the moment
			// the event was received and this lambda is executed on the EDT.
			if (equalSets(networks, serviceRegistrar.getService(CyApplicationManager.class).getSelectedNetworks()))
				syncFromSelectedNetworks(networks);
		});
	}
	
	@Override
	public void handleEvent(final SetSelectedNetworkViewsEvent e) {
		if (loadingSession)
			return;
		
		final List<CyNetworkView> views = e.getNetworkViews();
		
		synchronized (lock) {
			if (equalSets(views, viewMainPanel.getSelectedNetworkViews())) // Nothing has changed!
				return;
		}
		
		invokeOnEDT(() -> {
			// Don't sync if the list has changed again between the moment
			// the event was received and this lambda is executed on the EDT.
			if (equalSets(views, serviceRegistrar.getService(CyApplicationManager.class).getSelectedNetworkViews()))
				syncFromSelectedViews(views);
		});
	}
	
	private void syncFrom(final CyNetwork currentNet) {
		CyNetworkView currentView = viewMainPanel.getCurrentNetworkView();
		Collection<CyNetworkView> selectedViews = viewMainPanel.getSelectedNetworkViews();
		Collection<CyNetwork> selectedNets = netMainPanel.getSelectedNetworks(false);
		
		if (currentNet != null) {
			final CyNetworkViewManager viewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
			final Collection<CyNetworkView> views = viewMgr.getNetworkViews(currentNet);
			
			// If the new current network is not selected, reset the selection and select the current one only
			if (!selectedNets.contains(currentNet))
				selectedNets = Collections.singleton(currentNet);
			
			// Set new current network view, unless the current view's model is already the new current network
			if (currentView == null || !currentView.getModel().equals(currentNet))
				currentView = views.isEmpty() ? null : views.iterator().next();
			
			if (currentView == null)
				selectedViews = Collections.emptySet();
			else if (!selectedViews.contains(currentView))
				selectedViews = Collections.singleton(currentView);
		} else {
			currentView = null;
			selectedNets = Collections.emptySet();
			selectedViews = Collections.emptySet();
		}
		
		// First update the UI
		removePropertyChangeListeners();
		
		try {
			netMainPanel.setCurrentNetwork(currentNet);
			netMainPanel.setSelectedNetworks(selectedNets);
			viewMainPanel.setSelectedNetworkViews(selectedViews);
			viewMainPanel.setCurrentNetworkView(currentView);
			
			maybeShowNullView(currentView, currentNet);
		} finally {
			addPropertyChangeListeners();
		}
		
		// Then update the related Cytoscape states
		updateApplicationManager(currentNet, currentView, selectedNets, selectedViews);
	}
	
	private void syncFrom(final CyNetworkView currentView) {
		CyNetwork currentNet = netMainPanel.getCurrentNetwork();
		Collection<CyNetworkView> selectedViews = viewMainPanel.getSelectedNetworkViews();
		Collection<CyNetwork> selectedNets = netMainPanel.getSelectedNetworks(false);
			
		// Synchronize the UI first
		if (currentView != null) {
			currentNet = currentView.getModel();

			if (!selectedViews.contains(currentView)) {
				if (selectedNets.contains(currentView.getModel())) {
					selectedViews.add(currentView);
				} else {
					selectedViews = Collections.singleton(currentView);
					selectedNets = Collections.singleton(currentNet);
				}
			}
		} else {
			if (currentNet != null) {
				final CyNetworkViewManager viewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
			
				for (CyNetwork net : selectedNets) {
					if (viewMgr.viewExists(net)) {
						currentNet = null;
						selectedNets = Collections.emptySet();
						break;
					}
				}
			}
			
			selectedViews = Collections.emptySet();
		}
		
		// First update the UI
		removePropertyChangeListeners();
		
		try {
			viewMainPanel.setCurrentNetworkView(currentView);
			viewMainPanel.setSelectedNetworkViews(selectedViews);
			netMainPanel.setCurrentNetwork(currentNet);
			netMainPanel.setSelectedNetworks(selectedNets);
			
			maybeShowNullView(currentView, currentNet);
		} finally {
			addPropertyChangeListeners();
		}
		
		// Then update the related Cytoscape states
		updateApplicationManager(currentNet, currentView, selectedNets, selectedViews);
	}
	
	private void syncFromSelectedNetworks(final Collection<CyNetwork> selectedNets) {
		CyNetworkView currentView = viewMainPanel.getCurrentNetworkView();
		CyNetwork currentNet = netMainPanel.getCurrentNetwork();
		Collection<CyNetworkView> selectedViews = getNetworkViews(selectedNets, serviceRegistrar);
		
		if (selectedNets.isEmpty()) {
			currentNet = null;
			currentView = null;
		} else {
			if (currentNet == null || !selectedNets.contains(currentNet))
				currentNet = selectedNets.iterator().next();
			
			if (currentView == null || !currentView.getModel().equals(currentNet)) {
				final CyNetworkViewManager viewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
				final Collection<CyNetworkView> views = viewMgr.getNetworkViews(currentNet);
				
				currentView = views == null || views.isEmpty() ? null : views.iterator().next();
			}
		}
			
		// Synchronize the UI first
		removePropertyChangeListeners();
		
		try {
			netMainPanel.setSelectedNetworks(selectedNets);
			netMainPanel.setCurrentNetwork(currentNet);
			
			if (currentView != null)
				viewMainPanel.setCurrentNetworkView(currentView);
			else
				viewMainPanel.showNullView(currentNet);
			
			viewMainPanel.setSelectedNetworkViews(selectedViews);
		} finally {
			addPropertyChangeListeners();
		}
		
		// Then update the related Cytoscape states
		updateApplicationManager(currentNet, currentView, selectedNets, selectedViews);
	}
	
	private void syncFromSelectedViews(final Collection<CyNetworkView> selectedViews) {
		CyNetworkView currentView = viewMainPanel.getCurrentNetworkView();
		CyNetwork currentNet = netMainPanel.getCurrentNetwork();
		Collection<CyNetwork> selectedNets = getNetworks(selectedViews);
		
		// Synchronize the UI first
		removePropertyChangeListeners();
		
		try {
			if (selectedViews.isEmpty())
				currentView = null;
			else if (!selectedViews.contains(currentView))
				currentView = selectedViews.iterator().next();
			
			if (currentView == null) {
				final CyNetworkViewManager viewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
				
				if (currentNet != null && viewMgr.viewExists(currentNet))
					currentNet = null;
			} else {
				currentNet = currentView.getModel();
			}
			
			// Synchronize the UI first
			viewMainPanel.setSelectedNetworkViews(selectedViews);
			viewMainPanel.setCurrentNetworkView(currentView);
			netMainPanel.setCurrentNetwork(currentNet);
			netMainPanel.setSelectedNetworks(selectedNets);
			
			maybeShowNullView(currentView, currentNet);
		} finally {
			addPropertyChangeListeners();
		}
		
		// Then update the related Cytoscape states
		updateApplicationManager(currentNet, currentView, selectedNets, selectedViews);
	}
	
	private void addPropertyChangeListeners() {
		removePropertyChangeListeners(); // Just to guarantee we don't add the listeners more than once
		
		for (String propName : netPanelPropChangeListener.PROP_NAMES)
			netMainPanel.addPropertyChangeListener(propName, netPanelPropChangeListener);
		
		for (String propName : viewPanelPropChangeListener.PROP_NAMES)
			viewMainPanel.addPropertyChangeListener(propName, viewPanelPropChangeListener);
		
		for (String propName : gridPanelPropChangeListener.PROP_NAMES)
			viewMainPanel.getNetworkViewGrid().addPropertyChangeListener(propName, gridPanelPropChangeListener);
	}
	
	private void removePropertyChangeListeners() {
		for (String propName : netPanelPropChangeListener.PROP_NAMES)
			netMainPanel.removePropertyChangeListener(propName, netPanelPropChangeListener);
		
		for (String propName : viewPanelPropChangeListener.PROP_NAMES)
			viewMainPanel.removePropertyChangeListener(propName, viewPanelPropChangeListener);
		
		for (String propName : gridPanelPropChangeListener.PROP_NAMES)
			viewMainPanel.getNetworkViewGrid().removePropertyChangeListener(propName, gridPanelPropChangeListener);
	}
	
	private void maybeShowNullView(final CyNetworkView view, final CyNetwork network) {
		if (view == null)
			viewMainPanel.showNullView(network);
	}
	
	private void updateApplicationManager(final CyNetwork currentNetwork, final CyNetworkView currentView,
			final Collection<CyNetwork> selectedNetworks, final Collection<CyNetworkView> selectedViews) {
			final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
		appMgr.setSelectedNetworks(new ArrayList<>(selectedNetworks));
		appMgr.setCurrentNetwork(currentNetwork);
		appMgr.setSelectedNetworkViews(new ArrayList<>(selectedViews));
		appMgr.setCurrentNetworkView(currentView);
	}
	
	private class NetPanelPropertyChangeListener implements PropertyChangeListener {

		final String[] PROP_NAMES = new String[] { "currentNetwork", "selectedSubNetworks" };
		
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (Arrays.asList(PROP_NAMES).contains(e.getPropertyName())) {
				if (e.getPropertyName().equals("currentNetwork"))
					handleCurrentNetworkChange(e);
				else if (e.getPropertyName().equals("selectedSubNetworks"))
					handleSelectedSubNetworksChange(e);
			}
		}
		
		private void handleCurrentNetworkChange(PropertyChangeEvent e) {
			if (loadingSession)
				return;
			
			if (e.getNewValue() == null || e.getNewValue() instanceof CyRootNetwork)
				viewMainPanel.showNullView((CyNetwork) e.getNewValue());
			
			final CyNetwork network = e.getNewValue() instanceof CySubNetwork ? (CyNetwork) e.getNewValue() : null;
			final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
			
			synchronized (lock) {
				final CyNetwork currentNet = appMgr.getCurrentNetwork();
				
				if (same(network, currentNet))
					return;
			}
			
			syncFrom(network);
		}

		@SuppressWarnings("unchecked")
		private void handleSelectedSubNetworksChange(PropertyChangeEvent e) {
			if (loadingSession)
				return;
			
			final Collection<CyNetwork> selectedNets = (Collection<CyNetwork>) e.getNewValue();
			
			synchronized (lock) {
				final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
				
				if (equalSets(selectedNets, appMgr.getSelectedNetworks()))
					return;
			}
			
			syncFromSelectedNetworks(selectedNets);
		}
	}
	
	private class ViewPanelPropertyChangeListener implements PropertyChangeListener {
		
		final String[] PROP_NAMES = new String[] { "selectedNetworkViews" };
		
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals("selectedNetworkViews"))
				handleSelectedViewsChange(e);
		}
		
		@SuppressWarnings("unchecked")
		private void handleSelectedViewsChange(PropertyChangeEvent e) {
			if (loadingSession)
				return;
			
			final Collection<CyNetworkView> selectedViews = (Collection<CyNetworkView>) e.getNewValue();
			
			synchronized (lock) {
				final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
				
				if (equalSets(selectedViews, appMgr.getSelectedNetworkViews()))
					return;
			}
			
			syncFromSelectedViews(selectedViews);
		}
	}
	
	private class GridPanelPropertyChangeListener implements PropertyChangeListener {
		
		final String[] PROP_NAMES = new String[] { "currentNetworkView" };
		
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals("currentNetworkView"))
				handleCurrentViewChange(e);
		}
		
		private void handleCurrentViewChange(PropertyChangeEvent e) {
			final CyNetworkView view = (CyNetworkView) e.getNewValue();
			final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
			
			synchronized (lock) {
				final CyNetworkView currentView = appMgr.getCurrentNetworkView();
				
				if (same(view, currentView))
					return;
			}
			
			syncFrom(view);
		}
	}
}
