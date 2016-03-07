package org.cytoscape.internal.view;

import static org.cytoscape.internal.util.ViewUtil.invokeOnEDT;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JFrame;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.CyStartEvent;
import org.cytoscape.application.events.CyStartListener;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.events.SetSelectedNetworkViewsEvent;
import org.cytoscape.application.events.SetSelectedNetworkViewsListener;
import org.cytoscape.application.events.SetSelectedNetworksEvent;
import org.cytoscape.application.events.SetSelectedNetworksListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.internal.util.Util;
import org.cytoscape.model.CyNetwork;
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
		SetCurrentNetworkListener, SessionAboutToBeLoadedListener, SessionLoadedListener, CyStartListener {

	private boolean loadingSession;
	private boolean ignoreSetSelectedNetworksEvent;
	private boolean ignoreSetSelectedViewsEvent;
	
	private final NetworkMainPanel netMainPanel;
	private final NetworkViewMainPanel netViewMainPanel;
	private final CyServiceRegistrar serviceRegistrar;
	
	private final Object lock = new Object();

	public NetworkSelectionMediator(final NetworkMainPanel netMainPanel, final NetworkViewMainPanel netViewMainPanel,
			final CyServiceRegistrar serviceRegistrar) {
		this.netMainPanel = netMainPanel;
		this.netViewMainPanel = netViewMainPanel;
		this.serviceRegistrar = serviceRegistrar;
		
		netMainPanel.addPropertyChangeListener("selectedSubNetworks", new PropertyChangeListener() {
			@Override
			@SuppressWarnings("unchecked")
			public void propertyChange(final PropertyChangeEvent e) {
				if (loadingSession)
					return;
				
				synchronized (lock) {
					final Collection<CyNetwork> selectedNetworks = (Collection<CyNetwork>) e.getNewValue();
					final Collection<CyNetworkView> selectedViews = Util.getNetworkViews(selectedNetworks,
							serviceRegistrar);

					final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
					final boolean setSelectedViews = !Util.equalSets(selectedViews, appMgr.getSelectedNetworkViews());
					final boolean setSelectedNetworks = !Util.equalSets(selectedNetworks, appMgr.getSelectedNetworks());
					
					if (!setSelectedNetworks && !setSelectedViews)
						return;
					
					new Thread(() -> {
						ignoreSetSelectedNetworksEvent = true;
						
						try {
							if (setSelectedNetworks) {
								// If no selected networks, set null to current network first,
								// or the current one will be selected again by the application!
								if (selectedNetworks == null || selectedNetworks.isEmpty())
									appMgr.setCurrentNetwork(null);
								
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
						}
					}).start();
				}
			}
		});
		
		netViewMainPanel.addPropertyChangeListener("selectedNetworkViews", new PropertyChangeListener() {
			@Override
			@SuppressWarnings("unchecked")
			public void propertyChange(PropertyChangeEvent e) {
				if (loadingSession)
					return;
				
				synchronized (lock) {
					final Collection<CyNetworkView> selectedViews = (Collection<CyNetworkView>) e.getNewValue();
					final Collection<CyNetwork> selectedNetworks = Util.getNetworks(selectedViews);
					
					final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
					final boolean setSelectedViews = !Util.equalSets(selectedViews, appMgr.getSelectedNetworkViews());
					final boolean setSelectedNetworks = !Util.equalSets(selectedNetworks, appMgr.getSelectedNetworks());
					
					if (!setSelectedViews && !setSelectedNetworks)
						return;
					
					new Thread(() -> {
						ignoreSetSelectedViewsEvent = true;
						
						try {
							// Ask Cytoscape to set these views as selected
							if (setSelectedViews) {
								if (selectedViews.isEmpty())
									appMgr.setCurrentNetworkView(null);
								
								appMgr.setSelectedNetworkViews(new ArrayList<>(selectedViews));
							}
							
							// Also ask Cytoscape to select all networks that have the selected views
							if (setSelectedNetworks)
								appMgr.setSelectedNetworks(new ArrayList<>(selectedNetworks));
						} finally {
							ignoreSetSelectedViewsEvent = false;
						}
					}).start();
				}
			}
		});
		
		final NetworkViewGrid networkViewGrid = netViewMainPanel.getNetworkViewGrid();
		
		networkViewGrid.addPropertyChangeListener("currentNetworkView", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				synchronized (lock) {
					new Thread(() -> {
						final CyNetworkView targetView = (CyNetworkView) e.getNewValue();
						
						final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
						final CyNetworkViewManager netViewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
						
						if (targetView != null) {
							if (netViewMgr.getNetworkViewSet().contains(targetView)) {
								if (!targetView.equals(appMgr.getCurrentNetworkView()))
									appMgr.setCurrentNetworkView(targetView);
							}
						} else {
							if (appMgr.getCurrentNetworkView() != null)
								appMgr.setCurrentNetworkView(targetView);
						}
					}).start();
				}
			}
		});
	}

	@Override
	public void handleEvent(final CyStartEvent e) {
		final JFrame cyFrame = serviceRegistrar.getService(CySwingApplication.class).getJFrame();
		
		cyFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {
				// Set the visible View card as current when the main Cytoscape window gains focus again,
				// if necessary (usually when there are detached view frames)
				final NetworkViewContainer vc = netViewMainPanel.getCurrentViewContainer();
				
				if (vc != null && !vc.getNetworkView().equals(netViewMainPanel.getCurrentNetworkView())) {
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
			netMainPanel.getRootNetworkListPanel().update();
			
			if (e.getNetwork() != null) {
				netMainPanel.scrollTo(e.getNetwork());
				final SubNetworkPanel subNetPanel = netMainPanel.getSubNetworkPanel(e.getNetwork());
				
				if (subNetPanel != null)
					subNetPanel.requestFocus();
			}
		});
	}
	
	@Override
	public void handleEvent(final SetSelectedNetworksEvent e) {
		if (loadingSession || ignoreSetSelectedNetworksEvent)
			return;
		
		if (Util.equalSets(e.getNetworks(), netMainPanel.getSelectedNetworks(false)))
			return;
		
		invokeOnEDT(() -> {
			netMainPanel.setSelectedNetworks(e.getNetworks());
		});
	}
	
	@Override
	public void handleEvent(final SetSelectedNetworkViewsEvent e) {
		if (loadingSession || ignoreSetSelectedViewsEvent)
			return;
		
		if (Util.equalSets(e.getNetworkViews(), netViewMainPanel.getSelectedNetworkViews()))
			return;
		
		invokeOnEDT(() -> {
			netViewMainPanel.setSelectedNetworkViews(e.getNetworkViews());
		});
	}
}
