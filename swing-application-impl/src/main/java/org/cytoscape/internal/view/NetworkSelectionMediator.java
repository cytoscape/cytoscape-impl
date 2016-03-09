package org.cytoscape.internal.view;

import static org.cytoscape.internal.util.ViewUtil.invokeOnEDT;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

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
	private boolean ignoreSetSelectedNetworksEvent;
	private boolean ignoreSetSelectedViewsEvent;
	
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
				
				if (vc != null && !vc.getNetworkView().equals(viewMainPanel.getCurrentNetworkView())) {
					final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
					appMgr.setCurrentNetworkView(vc.getNetworkView());
				}
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
		invokeOnEDT(() -> {
			netMainPanel.setCurrentNetwork(e.getNetwork());
		});
	}

	@Override
	public void handleEvent(final SetCurrentNetworkViewEvent e) {
		if (loadingSession)
			return;
		
		final CyNetworkView view = e.getNetworkView();
		
		invokeOnEDT(() -> {
			viewMainPanel.setCurrentNetworkView(view);
		});
	}
	
	@Override
	public void handleEvent(final SetSelectedNetworksEvent e) {
		if (loadingSession || ignoreSetSelectedNetworksEvent)
			return;
		
		synchronized (lock) {
			if (Util.equalSets(e.getNetworks(), netMainPanel.getSelectedNetworks(false)))
				return;
		}
		
		invokeOnEDT(() -> {
			netMainPanel.setSelectedNetworks(e.getNetworks());
		});
	}
	
	@Override
	public void handleEvent(final SetSelectedNetworkViewsEvent e) {
		if (loadingSession || ignoreSetSelectedViewsEvent)
			return;
		
		synchronized (lock) {
			if (Util.equalSets(e.getNetworkViews(), viewMainPanel.getSelectedNetworkViews()))
				return;
		}
		
		invokeOnEDT(() -> {
			viewMainPanel.setSelectedNetworkViews(e.getNetworkViews());
		});
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
	
	private class NetPanelPropertyChangeListener implements PropertyChangeListener {

		final String[] PROP_NAMES = new String[] { "currentNetwork", "selectedSubNetworks" };
		
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (Arrays.asList(PROP_NAMES).contains(e.getPropertyName())) {
				removePropertyChangeListeners();
				
				try {
					if (e.getPropertyName().equals("currentNetwork"))
						handleCurrentNetworkChange(e);
					else if (e.getPropertyName().equals("selectedSubNetworks"))
						handleSelectedSubNetworksChange(e);
				} finally {
					addPropertyChangeListeners();
				}
			}
		}
		
		private void handleCurrentNetworkChange(PropertyChangeEvent e) {
			if (loadingSession)
				return;
			
			final CyNetwork network = e.getNewValue() instanceof CySubNetwork ? (CyNetwork) e.getNewValue() : null;
			
			// Synchronize the UI first
			if (network == null)
				viewMainPanel.setCurrentNetworkView(null);
			
			synchronized (lock) {
				final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
				final CyNetwork currentNet = appMgr.getCurrentNetwork();
				
				// Then update the related Cytoscape states
				if ((network == null && currentNet == null) || (network != null && network.equals(currentNet)))
					return;
				
				new Thread(() -> {
					appMgr.setCurrentNetwork(network);
					
					if (network == null)
						appMgr.setCurrentNetworkView(null);
				}).start();
			}
		}
		
		@SuppressWarnings("unchecked")
		private void handleSelectedSubNetworksChange(PropertyChangeEvent e) {
			if (loadingSession)
				return;
			
			final Collection<CyNetwork> selectedNetworks = (Collection<CyNetwork>) e.getNewValue();
			final Collection<CyNetworkView> selectedViews = Util.getNetworkViews(selectedNetworks,
					serviceRegistrar);
			
			// Synchronize the UI first
			viewMainPanel.setSelectedNetworkViews(new ArrayList<>(selectedViews));
			
			synchronized (lock) {
				// Then update the related Cytoscape states
				final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
				final boolean setSelectedViews = !Util.equalSets(selectedViews, appMgr.getSelectedNetworkViews());
				final boolean setSelectedNetworks = !Util.equalSets(selectedNetworks, appMgr.getSelectedNetworks());
				
				if (!setSelectedNetworks && !setSelectedViews)
					return;
				
				new Thread(() -> {
					ignoreSetSelectedNetworksEvent = true;
					ignoreSetSelectedViewsEvent = true;
					
					try {
						if (setSelectedNetworks) {
							// If no selected networks, set null to current network first,
							// or the current one will be selected again by the application!
							if (selectedNetworks == null || selectedNetworks.isEmpty()) {
								appMgr.setCurrentNetwork(null);
								appMgr.setCurrentNetworkView(null);
							}
							
							// Ask Cytoscape to set these networks as selected
							appMgr.setSelectedNetworks(new ArrayList<>(selectedNetworks));
						}
					
						// Also ask Cytoscape to select all views of the selected networks
						if (setSelectedViews) {
							if (selectedViews.isEmpty())
								appMgr.setCurrentNetworkView(null);
							
							appMgr.setSelectedNetworkViews(new ArrayList<>(selectedViews));
						}
					} finally {
						ignoreSetSelectedNetworksEvent = false;
						ignoreSetSelectedViewsEvent = false;
					}
				}).start();
			}
		}
	}
	
	private class ViewPanelPropertyChangeListener implements PropertyChangeListener {
		
		final String[] PROP_NAMES = new String[] { "selectedNetworkViews" };
		
		@Override
		public void propertyChange(PropertyChangeEvent e) {
			if (e.getPropertyName().equals("selectedNetworkViews")) {
				removePropertyChangeListeners();
				
				try {
					handleSelectedViewsChange(e);
				} finally {
					addPropertyChangeListeners();
				}
			}
		}
		
		@SuppressWarnings("unchecked")
		private void handleSelectedViewsChange(PropertyChangeEvent e) {
			if (loadingSession)
				return;
			
			synchronized (lock) {
				final Collection<CyNetworkView> selectedViews = (Collection<CyNetworkView>) e.getNewValue();
				final Collection<CyNetwork> selectedNetworks = Util.getNetworks(selectedViews);
				
				// Synchronize the UI first
				netMainPanel.setSelectedNetworks(new ArrayList<>(selectedNetworks));
				
				// Then update the related Cytoscape states
				final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
				final boolean setSelectedViews = !Util.equalSets(selectedViews, appMgr.getSelectedNetworkViews());
				final boolean setSelectedNetworks = !Util.equalSets(selectedNetworks, appMgr.getSelectedNetworks());
				
				if (!setSelectedViews && !setSelectedNetworks)
					return;
				
				new Thread(() -> {
					ignoreSetSelectedViewsEvent = true;
					ignoreSetSelectedNetworksEvent = true;
					
					try {
						// Ask Cytoscape to set these views as selected
						if (setSelectedViews) {
							if (selectedViews.isEmpty()) {
								appMgr.setCurrentNetworkView(null);
								appMgr.setCurrentNetwork(null);
							}
							
							appMgr.setSelectedNetworkViews(new ArrayList<>(selectedViews));
						}
						
						// Also ask Cytoscape to select all networks that have the selected views
						if (setSelectedNetworks)
							appMgr.setSelectedNetworks(new ArrayList<>(selectedNetworks));
					} finally {
						ignoreSetSelectedViewsEvent = false;
						ignoreSetSelectedNetworksEvent = false;
					}
				}).start();
			}
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
			synchronized (lock) {
				final CyNetworkView view = (CyNetworkView) e.getNewValue();
				
				final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
				final CyNetworkView currentView = appMgr.getCurrentNetworkView();
				
				// Synchronize the UI first
				if (view != null)
					netMainPanel.setCurrentNetwork(view.getModel());
				
				// Then update the related Cytoscape states
				if ((view == null && currentView == null) || (view != null && view.equals(currentView)))
					return;
				
				new Thread(() -> {
					final CyNetworkViewManager netViewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
					
					if (view != null) {
						if (netViewMgr.getNetworkViewSet().contains(view)) {
							if (!view.equals(appMgr.getCurrentNetworkView()))
								appMgr.setCurrentNetworkView(view);
						}
					} else {
						appMgr.setCurrentNetworkView(view);
					}
				}).start();
			}
		}
	}
}
