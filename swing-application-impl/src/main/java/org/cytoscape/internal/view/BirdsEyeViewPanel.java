package org.cytoscape.internal.view;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.UIManager;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;

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


@SuppressWarnings("serial")
public class BirdsEyeViewPanel extends JPanel {
	
	private JPanel presentationPanel;
	
	private RenderingEngine<CyNetwork> engine;
	private final CyNetworkView networkView;
	
	private CyServiceRegistrar serviceRegistrar;

	public BirdsEyeViewPanel(final CyNetworkView networkView, final CyServiceRegistrar serviceRegistrar) {
		this.networkView = networkView;
		this.serviceRegistrar = serviceRegistrar;
		
		init();
	}

	public RenderingEngine<CyNetwork> getEngine() {
		return engine;
	}
	
	public final void update() {
		final Dimension currentPanelSize = getSize();
		getPresentationPanel().setSize(currentPanelSize);
		getPresentationPanel().setPreferredSize(currentPanelSize);
		
		if (engine == null) {
			final CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
			final NetworkViewRenderer renderer = applicationManager.getNetworkViewRenderer(networkView.getRendererId());
			final RenderingEngineFactory<CyNetwork> bevFactory =
					renderer.getRenderingEngineFactory(NetworkViewRenderer.BIRDS_EYE_CONTEXT);
			engine = bevFactory.createRenderingEngine(getPresentationPanel(), networkView);
		}
		
		repaint();
	}
	
	public void dispose() {
		if (engine != null)
			engine.dispose();
		
		getPresentationPanel().removeAll();
		repaint();
	}
	
	private void init() {
		setBackground(UIManager.getColor("Table.background"));
		
		setLayout(new BorderLayout());
		add(getPresentationPanel(), BorderLayout.CENTER);
	}
	
	protected JPanel getPresentationPanel() {
		if (presentationPanel == null) {
			presentationPanel = new JPanel();
		}
		
		return presentationPanel;
	}
}
