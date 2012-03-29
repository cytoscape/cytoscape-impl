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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentRenderingEngineEvent;
import org.cytoscape.application.events.SetCurrentRenderingEngineListener;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.events.NetworkViewDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewDestroyedListener;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles the creation of the BirdsEyeView navigation object and
 * handles the events which change view seen.
 */
public class BirdsEyeViewHandler implements SetCurrentRenderingEngineListener,
		NetworkViewDestroyedListener {

	private static final Logger logger = LoggerFactory.getLogger(BirdsEyeViewHandler.class);

	private static final Dimension DEF_PANEL_SIZE = new Dimension(280, 280);
	private static final Color DEF_BACKGROUND_COLOR = Color.WHITE;

	// BEV is just a special implementation of RenderingEngine.
	private final RenderingEngineFactory<CyNetwork> bevFactory;

	private FrameListener frameListener = new FrameListener();

	private final NetworkViewManager networkViewManager;
	private final RenderingEngineManager renderingEngineManager;

	private Container bevPanel;

	private RenderingEngine<CyNetwork> engine;
	private final CyApplicationManager appManager;
	
	private final Map<CyNetworkView, JPanel> presentationMap;

	/**
	 * Updates Bird's Eye View
	 * 
	 * @param appManager
	 * @param viewmgr
	 * @param defaultFactory
	 * @param renderingEngineManager
	 */
	public BirdsEyeViewHandler(final CyApplicationManager appManager,
			final NetworkViewManager viewmgr,
			final RenderingEngineFactory<CyNetwork> defaultFactory, final RenderingEngineManager renderingEngineManager) {

		this.appManager = appManager;
		this.networkViewManager = viewmgr;
		this.renderingEngineManager = renderingEngineManager;
		
		presentationMap = new HashMap<CyNetworkView, JPanel>();

		this.bevPanel = new JPanel();
		this.bevPanel.setPreferredSize(DEF_PANEL_SIZE);
		this.bevPanel.setSize(DEF_PANEL_SIZE);
		this.bevPanel.setBackground(DEF_BACKGROUND_COLOR);

		this.bevFactory = defaultFactory;

		final JDesktopPane desktopPane = viewmgr.getDesktopPane();
		desktopPane.addComponentListener(new DesktopListener());
	}

	private void setFocus() {
		final JDesktopPane desktopPane = networkViewManager.getDesktopPane();
		if (desktopPane == null)
			return;

		final JInternalFrame frame = desktopPane.getSelectedFrame();
		if (frame == null)
			return;

		boolean hasListener = false;
		ComponentListener[] listeners = frame.getComponentListeners();
		for (int i = 0; i < listeners.length; i++)
			if (listeners[i] == frameListener)
				hasListener = true;

		if (!hasListener)
			frame.addComponentListener(frameListener);
	}

	/**
	 * Returns a birds eye view component.
	 * 
	 * @return The component that contains the birds eye view.
	 */
	Component getBirdsEyeView() {
		return bevPanel;
	}

	/**
	 * Repaint a JInternalFrame whenever it is moved.
	 */
	class FrameListener extends ComponentAdapter {
		public void componentMoved(ComponentEvent e) {
			bevPanel.repaint();
		}
	}

	/**
	 * Repaint the JDesktopPane whenever its size has changed.
	 */
	class DesktopListener extends ComponentAdapter {
		public void componentResized(ComponentEvent e) {
			bevPanel.repaint();
		}
	}

	@Override
	public void handleEvent(final SetCurrentRenderingEngineEvent e) {
		final RenderingEngine<CyNetwork> newEngine = e.getRenderingEngine();
		final CyNetworkView newViewModel = (CyNetworkView) newEngine.getViewModel();
		
		// Remove it from the manager object.
		if(engine != null)
			renderingEngineManager.removeRenderingEngine(engine);
		
		engine = newEngine;

		JPanel presentationPanel = presentationMap.get(newViewModel);
		
		if(presentationPanel == null) {
			presentationPanel = new JPanel();
			final RenderingEngine<CyNetwork> bevEngine = bevFactory.createRenderingEngine(presentationPanel, newViewModel);
			presentationMap.put((CyNetworkView) newViewModel, presentationPanel);
		}
		
		bevPanel.removeAll();
		final Dimension currentPanelSize = bevPanel.getSize();
		bevPanel.setLayout(new BorderLayout());
		presentationPanel.setSize(currentPanelSize);
		presentationPanel.setPreferredSize(currentPanelSize);
		
		bevPanel.add(presentationPanel, BorderLayout.CENTER);
		setFocus();
		
		presentationPanel.repaint();		
		bevPanel.repaint();
		
		//System.out.println("#10 of rendering engine ===> " + renderingEngineManager.getAllRenderingEngines().size());
	}

	@Override
	public void handleEvent(NetworkViewDestroyedEvent e) {
		// Cleanup the visualization container
		if (appManager.getCurrentNetworkView() == null) {
			bevPanel.removeAll();
			bevPanel.repaint();
		}
	}
}
