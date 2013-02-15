package org.cytoscape.view.vizmap.gui.internal;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.DefaultViewPanel;

/**
 * Container to embed the default presentation.
 * 
 */
public class DefaultViewPanelImpl extends JPanel implements DefaultViewPanel {

	private final static long serialVersionUID = 1202339876691085L;

	private static final Dimension MIN_SIZE = new Dimension(250, 150);

	// Space around view.
	private static final int PADDING = 20;

	// Dummy network and its view
	private RenderingEngine<CyNetwork> renderingEngine;
	private final VisualMappingManager vmm;

	// For padding.
	private JPanel innerPanel;

	private final CyNetwork dummyNet;

	private CyNode source;

	private CyNode target;

	private final CyApplicationManager applicationManager;

	/**
	 * Creates a new DefaultViewPanel object.
	 * 
	 * @param cyNetworkFactory
	 *            DOCUMENT ME!
	 * @param cyNetworkViewFactory
	 *            DOCUMENT ME!
	 */
	public DefaultViewPanelImpl(final CyNetworkFactory cyNetworkFactory,
			final CyApplicationManager applicationManager, final VisualMappingManager vmm) {
		// Validate
		if (cyNetworkFactory == null)
			throw new NullPointerException("CyNetworkFactory is null.");

		if (applicationManager == null)
			throw new NullPointerException("CyApplicationManager is null.");

		this.vmm = vmm;
		this.applicationManager = applicationManager;
		
		dummyNet = createNetwork(cyNetworkFactory);
		
		this.setPreferredSize(MIN_SIZE);
		this.setBorder(new LineBorder(Color.DARK_GRAY, 2));
		this.setLayout(new BorderLayout());
		
		innerPanel = createInnerPanel();
	}
	
	CyNetwork createNetwork(CyNetworkFactory cyNetworkFactory) {
		CyNetwork dummyNet = cyNetworkFactory.createNetworkWithPrivateTables(SavePolicy.DO_NOT_SAVE);
		source = dummyNet.addNode();
		target = dummyNet.addNode();

		dummyNet.getRow(source).set(CyNetwork.NAME, "Source");
		dummyNet.getRow(target).set(CyNetwork.NAME, "Target");

		final CyEdge edge = dummyNet.addEdge(source, target, true);
		dummyNet.getRow(edge).set(CyNetwork.NAME, "Source (interaction) Target");

		dummyNet.getRow(dummyNet).set(CyNetwork.NAME, "Default Appearance");
		return dummyNet;
	}
	
	JPanel createInnerPanel() {
		removeAll();
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));

		this.add(panel, BorderLayout.CENTER);

		// Create dummy view.
		NetworkViewRenderer renderer = applicationManager.getCurrentNetworkViewRenderer();
		CyNetworkViewFactory cyNetworkViewFactory = renderer.getNetworkViewFactory();
		RenderingEngineFactory<CyNetwork> presentationFactory = renderer.getRenderingEngineFactory(NetworkViewRenderer.VISUAL_STYLE_PREVIEW_CONTEXT);
		
		final CyNetworkView dummyview = cyNetworkViewFactory.createNetworkView(dummyNet);

		// Set node locations
		dummyview.getNodeView(source).setVisualProperty(NODE_X_LOCATION, 0d);
		dummyview.getNodeView(source).setVisualProperty(NODE_Y_LOCATION, 0d);
		dummyview.getNodeView(target).setVisualProperty(NODE_X_LOCATION, 150d);
		dummyview.getNodeView(target).setVisualProperty(NODE_Y_LOCATION, 20d);

		final VisualStyle currentStyle = vmm.getCurrentVisualStyle();
		currentStyle.apply(dummyview);

		panel.setBackground((Color) currentStyle.getDefaultValue(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT));
		// Render it in this panel
		renderingEngine = presentationFactory.createRenderingEngine(panel, dummyview);

		// Register it to the manager.
		// renderingEngineManager.addRenderingEngine(renderingEngine);
		dummyview.fitContent();

		// Remove unnecessary mouse listeners.
		final int compCount = panel.getComponentCount();
		for (int i = 0; i < compCount; i++) {
			final Component comp = panel.getComponent(i);
			final MouseListener[] listeners = comp.getMouseListeners();
			for (MouseListener ml : listeners)
				comp.removeMouseListener(ml);
		}
		return panel;
	}

	void updateView(final VisualStyle vs) {
		final CyNetworkView viewModel = (CyNetworkView) renderingEngine.getViewModel();
		vs.apply(viewModel);
		this.innerPanel.setBackground((Color) vs.getDefaultValue(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT));
		// This is necessary to adjust the size of default image.
		viewModel.fitContent();
	}

	void updateView() {
		updateView(vmm.getCurrentVisualStyle());
	}

	@Override
	public RenderingEngine<CyNetwork> getRenderingEngine() {
		return renderingEngine;
	}
}
