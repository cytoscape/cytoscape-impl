/*
 File: BirdsEyeViewHandler.java

 Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

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
package org.cytoscape.internal.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentRenderingEngineEvent;
import org.cytoscape.application.events.SetCurrentRenderingEngineListener;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.NetworkViewDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewDestroyedListener;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles the creation of the BirdsEyeView navigation object and
 * handles the events which change view seen.
 */
public class BirdsEyeViewHandler implements SetCurrentRenderingEngineListener, NetworkViewDestroyedListener {

	private static final Logger logger = LoggerFactory.getLogger(BirdsEyeViewHandler.class);

	private static final Dimension DEF_PANEL_SIZE = new Dimension(300, 300);
	private static final Color DEF_BACKGROUND_COLOR = Color.WHITE;

	// BEV is just a special implementation of RenderingEngine.
	private final RenderingEngineFactory<CyNetwork> bevFactory;
	private final JPanel bevPanel;
	private final Map<CyNetworkView, RenderingEngine<?>> viewToEngineMap;
	private final CyApplicationManager appManager;
	private final CyNetworkViewManager netViewManager;
	private final Map<CyNetworkView, JPanel> presentationMap;

	/**
	 * Updates Bird's Eye View
	 * 
	 * @param appManager
	 * @param defaultFactory
	 */
	public BirdsEyeViewHandler(final CyApplicationManager appManager,
							   final RenderingEngineFactory<CyNetwork> defaultFactory,
							   final CyNetworkViewManager netViewManager) {

		this.appManager = appManager;
		this.netViewManager = netViewManager;
		this.viewToEngineMap = new WeakHashMap<CyNetworkView, RenderingEngine<?>>();
		this.presentationMap = new WeakHashMap<CyNetworkView, JPanel>();

		this.bevPanel = new JPanel();
		this.bevPanel.setLayout(new BorderLayout());
		this.bevPanel.setPreferredSize(DEF_PANEL_SIZE);
		this.bevPanel.setSize(DEF_PANEL_SIZE);
		this.bevPanel.setBackground(DEF_BACKGROUND_COLOR);

		this.bevFactory = defaultFactory;
	}

	/**
	 * Returns a birds eye view component.
	 * 
	 * @return The component that contains the birds eye view.
	 */
	final Component getBirdsEyeView() {
		return bevPanel;
	}

	@Override
	public void handleEvent(final SetCurrentRenderingEngineEvent e) {
		final RenderingEngine<CyNetwork> newEngine = e.getRenderingEngine();
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateBEV(newEngine);
			}
		});
	}

	@Override
	public void handleEvent(final NetworkViewDestroyedEvent e) {
		final CyNetworkViewManager manager = e.getSource();
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				removeView(manager);
			}
		});
	}
	
	private final void updateBEV(final RenderingEngine<CyNetwork> newEngine) {
		JPanel presentationPanel = null;
		
		if (newEngine != null) {
			final CyNetworkView newView = (CyNetworkView) newEngine.getViewModel();
			
			if (netViewManager.getNetworkViewSet().contains(newView)) {
				presentationPanel = presentationMap.get(newView);
		
				if (presentationPanel == null) {
					logger.debug("Creating new BEV for: " + newView);
					presentationPanel = new JPanel();
					viewToEngineMap.put(newView, bevFactory.createRenderingEngine(presentationPanel, newView));
					presentationMap.put((CyNetworkView) newView, presentationPanel);
				}
				
				final Dimension currentPanelSize = bevPanel.getSize();
				presentationPanel.setSize(currentPanelSize);
				presentationPanel.setPreferredSize(currentPanelSize);
			}
		}

		bevPanel.removeAll();
		
		if (presentationPanel != null) {
			bevPanel.add(presentationPanel, BorderLayout.CENTER);
			bevPanel.revalidate(); // without this, the BEV might not be updated right away
		}
		
		bevPanel.repaint();
	}

	private final void removeView(final CyNetworkViewManager manager) {
		Set<CyNetworkView> toBeRemoved = new HashSet<CyNetworkView>();
		
		for (CyNetworkView view : presentationMap.keySet()) {
			if (manager.getNetworkViewSet().contains(view) == false)
				toBeRemoved.add(view);
		}

		for (CyNetworkView view : toBeRemoved) {
			presentationMap.remove(view);
			RenderingEngine<?> engine = viewToEngineMap.remove(view);
			if (engine != null)
				engine.dispose();
		}

		toBeRemoved.clear();
		toBeRemoved = null;

		// Cleanup the visualization container
		if (appManager.getCurrentNetworkView() == null) {
			bevPanel.removeAll();
			bevPanel.repaint();
		}
	}
}
