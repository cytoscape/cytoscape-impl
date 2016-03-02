package org.cytoscape.internal.view;

import static org.cytoscape.internal.util.ViewUtil.invokeOnEDT;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeLoadedEvent;
import org.cytoscape.session.events.SessionAboutToBeLoadedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;

/**
 * This class acts as an intermediary between the CyNetwork/CyNetworkView selection events
 * and the selection of network/view entries in the UI, so they are kept in sync in a way
 * that makes sense to the end user.
 */
public class NetworkSelectionMediator implements SetSelectedNetworksListener, SetSelectedNetworkViewsListener,
		SetCurrentNetworkListener, SessionAboutToBeLoadedListener, SessionLoadedListener, CyStartListener {

	private boolean loadingSession;
	private boolean ignoreNetworkSelectionEvents;
	private boolean ignoreNetworkViewSelectionEvents;
	
	private final NetworkMainPanel netMainPanel;
	private final NetworkViewMainPanel netViewMainPanel;
	private final CyServiceRegistrar serviceRegistrar;

	public NetworkSelectionMediator(final NetworkMainPanel netMainPanel, final NetworkViewMainPanel netViewMainPanel,
			final CyServiceRegistrar serviceRegistrar) {
		this.netMainPanel = netMainPanel;
		this.netViewMainPanel = netViewMainPanel;
		this.serviceRegistrar = serviceRegistrar;
		
		netMainPanel.addPropertyChangeListener("selectedSubNetworks", new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent e) {
				if (ignoreNetworkSelectionEvents)
					return;
				
				new Thread() {
					@Override
					@SuppressWarnings("unchecked")
					public void run() {
						final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
						final Collection<CyNetwork> selectedNetworks = (Collection<CyNetwork>) e.getNewValue();
						
						// If no selected networks, set null to current network first,
						// or the current one will be selected again by the application!
						if (selectedNetworks == null || selectedNetworks.isEmpty())
							appMgr.setCurrentNetwork(null);
						
						// Ask Cytoscape to set these networks as selected
						appMgr.setSelectedNetworks(new ArrayList<>(selectedNetworks));
						
						// Also ask Cytoscape to select all views of the selected networks
						final List<CyNetworkView> selectedViews = new ArrayList<>();
						final CyNetworkViewManager netViewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
						
						for (CyNetwork net : selectedNetworks)
							selectedViews.addAll(netViewMgr.getNetworkViews(net));
						
						if (selectedViews.isEmpty())
							appMgr.setCurrentNetworkView(null);
						
						appMgr.setSelectedNetworkViews(selectedViews);
					}
				}.start();
			}
		});
		
		netViewMainPanel.addPropertyChangeListener("selectedNetworkViews", new PropertyChangeListener() {
			@Override
			@SuppressWarnings("unchecked")
			public void propertyChange(PropertyChangeEvent e) {
				if (ignoreNetworkViewSelectionEvents)
					return;
				
				final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
				appMgr.setSelectedNetworkViews((List<CyNetworkView>) e.getNewValue());
			}
		});
		
		final NetworkViewGrid networkViewGrid = netViewMainPanel.getNetworkViewGrid();
		
		networkViewGrid.addPropertyChangeListener("currentNetworkView", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
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
		netMainPanel.getRootNetworkListPanel().update();
		
		if (e.getNetwork() != null) {
			netMainPanel.scrollTo(e.getNetwork());
			final SubNetworkPanel subNetPanel = netMainPanel.getSubNetworkPanel(e.getNetwork());
			
			if (subNetPanel != null)
				subNetPanel.requestFocus();
		}
	}
	
	@Override
	public void handleEvent(final SetSelectedNetworksEvent e) {
		if (loadingSession)
			return;
		
		invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				ignoreNetworkSelectionEvents = true;
				
				try {
					netMainPanel.setSelectedNetworks(e.getNetworks());
				} finally {
					ignoreNetworkSelectionEvents = false;
				}
			}
		});
	}
	
	@Override
	public void handleEvent(final SetSelectedNetworkViewsEvent e) {
		invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				ignoreNetworkViewSelectionEvents = true;
				
				try {
					netViewMainPanel.setSelectedNetworkViews(e.getNetworkViews());
				} finally {
					ignoreNetworkViewSelectionEvents = false;
				}
			}
		});
	}
}
