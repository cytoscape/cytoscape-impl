package org.cytoscape.ding.impl.editor;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.impl.BendImpl;
import org.cytoscape.ding.impl.DEdgeView;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;
import org.cytoscape.ding.impl.InnerCanvas;

public class EdgeBendValueEditor extends JDialog implements ValueEditor<Bend> {

	private static final long serialVersionUID = 9145223127932839836L;

	private static final Dimension DEF_PANEL_SIZE = new Dimension(600, 350);
	
	private static final Color NODE_COLOR = Color.gray;
	private static final Color EDGE_COLOR = Color.BLACK;
	private static final Color BACKGROUND_COLOR = Color.white;
	
	private DEdgeView edgeView;

	private final CyNetworkFactory cyNetworkFactory;
	private final CyNetworkViewFactory cyNetworkViewFactory;
	private final RenderingEngineFactory<CyNetwork> presentationFactory;

	private boolean editCancelled = false;

	public EdgeBendValueEditor(final CyNetworkFactory cyNetworkFactory,
			final CyNetworkViewFactory cyNetworkViewFactory, final RenderingEngineFactory<CyNetwork> presentationFactory) {
		super();
		
		// Null check
		if (cyNetworkFactory == null)
			throw new NullPointerException("CyNetworkFactory is null.");
		if (cyNetworkViewFactory == null)
			throw new NullPointerException("CyNetworkViewFactory is null.");
		if (presentationFactory == null)
			throw new NullPointerException("RenderingEngineFactory is null.");
		
		this.cyNetworkFactory = cyNetworkFactory;
		this.cyNetworkViewFactory = cyNetworkViewFactory;
		this.presentationFactory = presentationFactory;
		
		
		this.setModal(true);
	}

	private void initUI(final CyNetworkFactory cyNetworkFactory,
			final CyNetworkViewFactory cyNetworkViewFactory, final RenderingEngineFactory<CyNetwork> presentationFactory) {
		
		this.getContentPane().removeAll();
		
		setTitle("Edge Bend Editor");

		// Create Dummy View for this editor
		JPanel innerPanel = new JPanel();
		final String osName = System.getProperty("os.name").toLowerCase();
		if(osName.contains("windows"))
			innerPanel.setBorder(new TitledBorder("Alt-Click to add new Edge Handle / Drag Handles to adjust Bend"));
		else if(osName.contains("mac"))
			innerPanel.setBorder(new TitledBorder("Option-click to add new Edge Handle / Drag Handles to adjust Bend"));
		else
			innerPanel.setBorder(new TitledBorder("Ctrl-Alt-Click to add new Edge Handle / Drag Handles to adjust Bend"));


		setPreferredSize(DEF_PANEL_SIZE);
		setLayout(new BorderLayout());
		add(innerPanel, BorderLayout.CENTER);

		// Create very simple dummy view.
		final CyNetwork dummyNet = cyNetworkFactory.createNetworkWithPrivateTables(SavePolicy.DO_NOT_SAVE);
		final CyNode source = dummyNet.addNode();
		final CyNode target = dummyNet.addNode();
		final CyEdge edge = dummyNet.addEdge(source, target, true);

		// Create View
		final CyNetworkView dummyview = cyNetworkViewFactory.createNetworkView(dummyNet);

		// Set appearances of the view
		final View<CyNode> sourceView = dummyview.getNodeView(source);
		final View<CyNode> targetView = dummyview.getNodeView(target);
		edgeView = (DEdgeView) dummyview.getEdgeView(edge);
		
		sourceView.setVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR, NODE_COLOR);
		targetView.setVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR, NODE_COLOR);
		sourceView.setVisualProperty(BasicVisualLexicon.NODE_LABEL_COLOR, Color.WHITE);
		targetView.setVisualProperty(BasicVisualLexicon.NODE_LABEL_COLOR, Color.WHITE);
		sourceView.setVisualProperty(BasicVisualLexicon.NODE_LABEL_FONT_SIZE, 16);
		targetView.setVisualProperty(BasicVisualLexicon.NODE_LABEL_FONT_SIZE, 16);
		sourceView.setVisualProperty(BasicVisualLexicon.NODE_LABEL, "S");
		targetView.setVisualProperty(BasicVisualLexicon.NODE_LABEL, "T");
		
		sourceView.setVisualProperty(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ELLIPSE);
		targetView.setVisualProperty(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ELLIPSE);
		
		sourceView.setVisualProperty(BasicVisualLexicon.NODE_WIDTH, 40d);
		sourceView.setVisualProperty(BasicVisualLexicon.NODE_HEIGHT, 40d);
		targetView.setVisualProperty(BasicVisualLexicon.NODE_WIDTH, 40d);
		targetView.setVisualProperty(BasicVisualLexicon.NODE_HEIGHT, 40d);
		
		edgeView.setVisualProperty(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, EDGE_COLOR);
		edgeView.setVisualProperty(BasicVisualLexicon.EDGE_WIDTH, 4d);
		edgeView.setVisualProperty(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE, ArrowShapeVisualProperty.ARROW);
		edgeView.setVisualProperty(DVisualLexicon.EDGE_TARGET_ARROW_UNSELECTED_PAINT, EDGE_COLOR);
		edgeView.setVisualProperty(DVisualLexicon.EDGE_CURVED, true);
		
		final Bend newBend = new BendImpl();
		edgeView.setVisualProperty(DVisualLexicon.EDGE_BEND, newBend);
		
		
		dummyview.getNodeView(source).setVisualProperty(NODE_X_LOCATION, 0d);
		dummyview.getNodeView(source).setVisualProperty(NODE_Y_LOCATION, 100d);
		dummyview.getNodeView(target).setVisualProperty(NODE_X_LOCATION, 400d);
		dummyview.getNodeView(target).setVisualProperty(NODE_Y_LOCATION, 120d);

		innerPanel.setBackground(BACKGROUND_COLOR);
		// Render it in this panel.  It is not necessary to register this engine to manager.
		final RenderingEngine<CyNetwork> renderingEngine = presentationFactory.createRenderingEngine(innerPanel, dummyview);
		dummyview.fitContent();
		
		InnerCanvas innerCanvas = (InnerCanvas)innerPanel.getComponent(0);
		innerCanvas.disablePopupMenu();
				
		final JPanel buttonPanel = new JPanel();
		final BoxLayout buttonLayout = new BoxLayout(buttonPanel, BoxLayout.X_AXIS);
		buttonPanel.setLayout(buttonLayout);
		final JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		final JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editCancelled = true;
				dispose();
			}
		});

		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		this.add(buttonPanel, BorderLayout.SOUTH);
		
		pack();
	}

	@Override
	public <S extends Bend> Bend showEditor(Component parent, S initialValue) {
		editCancelled = false;
		initUI(cyNetworkFactory, cyNetworkViewFactory, presentationFactory);
		EditMode.setMode(true);
		this.setLocationRelativeTo(parent);
		this.setVisible(true);
		EditMode.setMode(false);
		return editCancelled ? null : edgeView.getBend();
	}

	@Override
	public Class<Bend> getValueType() {
		return Bend.class;
	}
}
