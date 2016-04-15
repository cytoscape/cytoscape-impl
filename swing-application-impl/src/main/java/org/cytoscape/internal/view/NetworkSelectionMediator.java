package org.cytoscape.internal.view;

import static org.cytoscape.internal.util.ViewUtil.invokeOnEDT;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.CyStartEvent;
import org.cytoscape.application.events.CyStartListener;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.events.SetSelectedNetworkViewsEvent;
import org.cytoscape.application.events.SetSelectedNetworkViewsListener;
import org.cytoscape.application.events.SetSelectedNetworksEvent;
import org.cytoscape.application.events.SetSelectedNetworksListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.internal.util.Util;
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
public class NetworkSelectionMediator implements SetSelectedNetworksListener, SetSelectedNetworkViewsListener,
		SetCurrentNetworkListener, SetCurrentNetworkViewListener, SessionAboutToBeLoadedListener, SessionLoadedListener,
		CyStartListener {

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
	public void handleEvent(final CyStartEvent e) {
		final JFrame cyFrame = serviceRegistrar.getService(CySwingApplication.class).getJFrame();
		
		cyFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {
				// Set the visible View card as current when the main Cytoscape window gains focus again,
				// if necessary (usually when there are detached view frames)
				final NetworkViewContainer vc = viewMainPanel.getCurrentViewContainer();
				setCurrent(vc);
			}
		});
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
		synchronized (lock) {
			final CyNetwork network = e.getNetwork();
			final CyNetwork currentNet = netMainPanel.getCurrentNetwork();
			
			if ((currentNet == null && network == null) || (currentNet != null && currentNet.equals(network)))
				return;
		}
		
		invokeOnEDT(() -> {
			syncFrom(serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork());
		});
	}

	@Override
	public void handleEvent(final SetCurrentNetworkViewEvent e) {
		if (loadingSession)
			return;
		
		synchronized (lock) {
			final CyNetworkView view = e.getNetworkView();
			final CyNetworkView currentView = viewMainPanel.getCurrentNetworkView();
			
			if ((currentView == null && view == null) || (currentView != null && currentView.equals(view)))
				return;
		}
		
		invokeOnEDT(() -> {
			final CyNetworkView view = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetworkView();
			Collection<CyNetworkView> selectedViews = viewMainPanel.getSelectedNetworkViews();
			
			// Synchronize the UI first
			if (view != null) {
				if (!selectedViews.contains(view))
					selectedViews = Collections.singleton(view);
			} else {
				selectedViews = Collections.emptySet();
			}
						
			viewMainPanel.setCurrentNetworkView(view);
			viewMainPanel.setSelectedNetworkViews(selectedViews);
		});
	}
	
	@Override
	public void handleEvent(final SetSelectedNetworksEvent e) {
		if (loadingSession)
			return;
		
		synchronized (lock) {
			if (Util.equalSets(e.getNetworks(), netMainPanel.getSelectedNetworks(false)))
				return;
		}
		
		invokeOnEDT(() -> {
			final List<CyNetwork> selectedNets = serviceRegistrar.getService(CyApplicationManager.class)
					.getSelectedNetworks();
			syncFromSelectedNetworks(selectedNets);
		});
	}
	
	@Override
	public void handleEvent(final SetSelectedNetworkViewsEvent e) {
		if (loadingSession)
			return;
		
		synchronized (lock) {
			if (Util.equalSets(e.getNetworkViews(), viewMainPanel.getSelectedNetworkViews()))
				return;
		}
		
		invokeOnEDT(() -> {
			final List<CyNetworkView> selectedViews = serviceRegistrar.getService(CyApplicationManager.class)
					.getSelectedNetworkViews();
			syncFromSelectedViews(selectedViews);
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
	
	private void syncFromSelectedNetworks(final Collection<CyNetwork> selectedNets) {
		CyNetworkView currentView = viewMainPanel.getCurrentNetworkView();
		CyNetwork currentNet = netMainPanel.getCurrentNetwork();
		Collection<CyNetworkView> selectedViews = Util.getNetworkViews(selectedNets, serviceRegistrar);
		
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
		Collection<CyNetwork> selectedNets = Util.getNetworks(selectedViews);
		
		// Synchronize the UI first
		removePropertyChangeListeners();
		
		try {
			if (selectedViews.isEmpty()) {
				currentView = null;
				currentNet = null;
			} else if (!selectedViews.contains(currentView)) {
				currentView = selectedViews.iterator().next();
			}
			
			if (currentView != null)
				currentNet = currentView.getModel();
			
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
	
	private void setCurrent(final NetworkViewContainer vc) {
		final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
		
		if (vc == null || vc.getNetworkView().equals(appMgr.getCurrentNetworkView()))
			return;
		
		new Thread(() -> {
			appMgr.setCurrentNetworkView(vc.getNetworkView());
		}).start();
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
		new Thread(() -> {
			final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
			appMgr.setSelectedNetworks(new ArrayList<>(selectedNetworks));
			appMgr.setCurrentNetwork(currentNetwork);
			appMgr.setSelectedNetworkViews(new ArrayList<>(selectedViews));
			appMgr.setCurrentNetworkView(currentView);
		}).start();
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
				
				if ((network == null && currentNet == null) || (network != null && network.equals(currentNet)))
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
				// Then update the related Cytoscape states
				final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
				
				if (Util.equalSets(selectedNets, appMgr.getSelectedNetworks()))
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
				
				if (Util.equalSets(selectedViews, appMgr.getSelectedNetworkViews()))
					return;
			}
			
			syncFromSelectedViews(selectedViews);
		}
	}
	
	private class GridPanelPropertyChangeListener implements PropertyChangeListener {
		
		final String[] PROP_NAMES = new String[] { "currentNetworkView" };
		
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals("currentNetworkView")) {
				removePropertyChangeListeners();
				
				try {
					handleCurrentViewChange(e);
				} finally {
					addPropertyChangeListeners();
				}
			}
		}
		
		private void handleCurrentViewChange(PropertyChangeEvent e) {
			final CyNetworkView view = (CyNetworkView) e.getNewValue();
			final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
			
			synchronized (lock) {
				final CyNetworkView currentView = appMgr.getCurrentNetworkView();
				
				if ((view == null && currentView == null) || (view != null && view.equals(currentView)))
					return;
			}
			
			CyNetwork currentNet = netMainPanel.getCurrentNetwork();
			Collection<CyNetworkView> selectedViews = viewMainPanel.getSelectedNetworkViews();
			Collection<CyNetwork> selectedNets = netMainPanel.getSelectedNetworks(false);
				
			// Synchronize the UI first
			if (view != null) {
				currentNet = view.getModel();

				if (!selectedViews.contains(view)) {
					selectedViews = Collections.singleton(view);
					selectedNets = Collections.singleton(currentNet);
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
			
			// Synchronize the UI first
			viewMainPanel.setSelectedNetworkViews(selectedViews);
			netMainPanel.setCurrentNetwork(currentNet);
			netMainPanel.setSelectedNetworks(selectedNets);
			
			maybeShowNullView(view, currentNet);
			
			// Then update the related Cytoscape states
			updateApplicationManager(currentNet, view, selectedNets, selectedViews);
		}
	}
}
