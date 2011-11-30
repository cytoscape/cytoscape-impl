/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.view.vizmap.gui.internal;

import static org.cytoscape.view.presentation.property.MinimalVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.MinimalVisualLexicon.NODE_Y_LOCATION;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.MinimalVisualLexicon;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.DefaultViewPanel;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;

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
	private final RenderingEngine<CyNetwork> renderingEngine;
	private final SelectedVisualStyleManager selectedManager;

	// For padding.
	private final JPanel innerPanel;

	/**
	 * Creates a new DefaultViewPanel object.
	 * 
	 * @param cyNetworkFactory
	 *            DOCUMENT ME!
	 * @param cyNetworkViewFactory
	 *            DOCUMENT ME!
	 */
	public DefaultViewPanelImpl(final CyNetworkFactory cyNetworkFactory,
			final CyNetworkViewFactory cyNetworkViewFactory,
			final RenderingEngineFactory<CyNetwork> presentationFactory,
			final SelectedVisualStyleManager selectedManager) {

		this.innerPanel = new JPanel();
		this.innerPanel.setBorder(new EmptyBorder(PADDING, PADDING, PADDING, PADDING));

		this.setPreferredSize(MIN_SIZE);
		this.setBorder(new LineBorder(Color.DARK_GRAY, 2));
		this.setLayout(new BorderLayout());
		this.add(innerPanel, BorderLayout.CENTER);

		// Validate
		if (cyNetworkFactory == null)
			throw new NullPointerException("CyNetworkFactory is null.");

		if (cyNetworkViewFactory == null)
			throw new NullPointerException("CyNetworkViewFactory is null.");

		if (presentationFactory == null)
			throw new NullPointerException("RenderingEngineFactory is null.");

		if (selectedManager == null)
			throw new NullPointerException(
					"SelectedVisualStyleManager is null.");

		this.selectedManager = selectedManager;

		// Create dummy view.
		final CyNetwork dummyNet = cyNetworkFactory.createNetworkWithPrivateTables();

		final CyNode source = dummyNet.addNode();
		final CyNode target = dummyNet.addNode();

		source.getCyRow().set(CyTableEntry.NAME, "Source");
		target.getCyRow().set(CyTableEntry.NAME, "Target");

		final CyEdge edge = dummyNet.addEdge(source, target, true);
		edge.getCyRow().set(CyTableEntry.NAME, "Source (interaction) Target");

		dummyNet.getCyRow().set(CyTableEntry.NAME, "Default Appearance");
		final CyNetworkView dummyview = cyNetworkViewFactory
				.getNetworkView(dummyNet);

		// Set node locations
		dummyview.getNodeView(source).setVisualProperty(NODE_X_LOCATION, 0d);
		dummyview.getNodeView(source).setVisualProperty(NODE_Y_LOCATION, 0d);
		dummyview.getNodeView(target).setVisualProperty(NODE_X_LOCATION, 150d);
		dummyview.getNodeView(target).setVisualProperty(NODE_Y_LOCATION, 20d);

		final VisualStyle currentStyle = selectedManager.getCurrentVisualStyle();
		currentStyle.apply(dummyview);

		this.innerPanel.setBackground((Color) currentStyle.getDefaultValue(MinimalVisualLexicon.NETWORK_BACKGROUND_PAINT));
		// Render it in this panel
		renderingEngine = presentationFactory
				.getInstance(innerPanel, dummyview);
		dummyview.fitContent();
	}

	
	void updateView(final VisualStyle vs) {
		final CyNetworkView viewModel = (CyNetworkView) renderingEngine.getViewModel();
		vs.apply(viewModel);
		this.innerPanel.setBackground((Color) vs.getDefaultValue(MinimalVisualLexicon.NETWORK_BACKGROUND_PAINT));
		// This is necessary to adjust the size of default image.
		viewModel.fitContent();
	}
	
	void updateView() {
		updateView(selectedManager.getCurrentVisualStyle());
	}

	@Override
	public RenderingEngine<CyNetwork> getRenderingEngine() {
		return renderingEngine;
	}
}
