package org.cytoscape.ding.impl.editor;

import static javax.swing.GroupLayout.Alignment.LEADING;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_BEND;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_SELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_BACKGROUND_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_FILL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_COLOR;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_FONT_SIZE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_SELECTED_PAINT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_SHAPE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_WIDTH;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.impl.BendImpl;
import org.cytoscape.ding.impl.DEdgeView;
import org.cytoscape.ding.impl.InnerCanvas;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.Bend;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

public class EdgeBendValueEditor implements ValueEditor<Bend> {

	private static final Dimension DEF_PANEL_SIZE = new Dimension(600, 400);
	
	private JDialog dialog;
	private JPanel innerPanel;
	private CyNetworkView dummyView;
	private View<CyEdge> edgeView;

	private final CyNetworkViewFactory cyNetworkViewFactory;
	private final RenderingEngineFactory<CyNetwork> presentationFactory;
	private final CyServiceRegistrar serviceRegistrar;

	private boolean initialized;
	private boolean editCancelled;
	private boolean bendRemoved;

	public EdgeBendValueEditor(
			final CyNetworkViewFactory cyNetworkViewFactory,
			final RenderingEngineFactory<CyNetwork> presentationFactory,
			final CyServiceRegistrar serviceRegistrar
	) {
		if (cyNetworkViewFactory == null)
			throw new NullPointerException("CyNetworkViewFactory is null.");
		if (presentationFactory == null)
			throw new NullPointerException("RenderingEngineFactory is null.");
		
		this.cyNetworkViewFactory = cyNetworkViewFactory;
		this.presentationFactory = presentationFactory;
		this.serviceRegistrar = serviceRegistrar;
	}

	@SuppressWarnings("serial")
	private void init(final Component parent) {
		final Window owner = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
		dialog = new JDialog(owner, ModalityType.APPLICATION_MODAL);
		dialog.setTitle("Edge Bend Editor");
		dialog.setResizable(false);
		
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				editCancelled = true;
			}
		});
		dialog.setPreferredSize(DEF_PANEL_SIZE);

		final String osName = System.getProperty("os.name").toLowerCase();
		String newHandleAction = "Ctrl-Alt-click";
		
		if (osName.contains("windows"))
			newHandleAction = "Alt-click";
		else if (osName.contains("mac"))
			newHandleAction = "Option-click";
		
		final JLabel infoLabel = new JLabel(
				"<html><b>1. <i>" + newHandleAction + "</i></b> the edge to add a new handle.<br />" +
				"<b>2. </b>Drag handles to bend (select the edge first).</html>"
		);
		infoLabel.setFont(infoLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
		
		innerPanel = new JPanel();
		innerPanel.setBackground(UIManager.getColor("Table.background"));
		innerPanel.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")));

		final JButton okButton = new JButton(new AbstractAction("OK") {
			@Override
			public void actionPerformed(ActionEvent e) {
				editCancelled = false;
				dialog.dispose();
			}
		});
		
		final JButton cancelButton = new JButton(new AbstractAction("Cancel") {
			@Override
			public void actionPerformed(ActionEvent e) {
				editCancelled = true;
				dialog.dispose();
			}
		});
		
		final JButton removeBendButton = new JButton(new AbstractAction("Remove Bend") {
			@Override
			public void actionPerformed(ActionEvent e) {
				bendRemoved = true;
				editCancelled = false;
				dialog.dispose();
			}
		});
		
		final JPanel buttonPanel = LookAndFeelUtil.createOkCancelPanel(okButton, cancelButton, removeBendButton);
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(dialog.getRootPane(), okButton.getAction(), cancelButton.getAction());
		dialog.getRootPane().setDefaultButton(okButton);
		
		final GroupLayout layout = new GroupLayout(dialog.getContentPane());
		dialog.getContentPane().setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(LEADING, true)
				.addComponent(infoLabel)
				.addComponent(innerPanel)
				.addComponent(buttonPanel)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(infoLabel)
				.addComponent(innerPanel)
				.addComponent(buttonPanel)
		);
	}
	
	private void updateUI(Bend startBend) {
		innerPanel.removeAll();
		
		final Color NODE_COLOR = UIManager.getColor("Label.disabledForeground");
		final Color EDGE_COLOR = UIManager.getColor("Label.foreground");
		final Color SELECTION_COLOR = UIManager.getColor("Focus.color");
		final Color BACKGROUND_COLOR = UIManager.getColor("Table.background");
		
		// Create very simple dummy view.
		final CyNetworkFactory networkFactory = serviceRegistrar.getService(CyNetworkFactory.class);
		final CyNetwork dummyNet = networkFactory.createNetworkWithPrivateTables(SavePolicy.DO_NOT_SAVE);
		final CyNode source = dummyNet.addNode();
		final CyNode target = dummyNet.addNode();
		final CyEdge edge = dummyNet.addEdge(source, target, true);
		
		// TODO Unfortunately, handles cannot be clicked when the edge is selected like this
		//dummyNet.getRow(edge, CyNetwork.DEFAULT_ATTRS).set(CyNetwork.SELECTED, true);
		
		// Create View
		dummyView = cyNetworkViewFactory.createNetworkView(dummyNet);

		// Set appearances of the view
		final View<CyNode> sourceView = dummyView.getNodeView(source);
		final View<CyNode> targetView = dummyView.getNodeView(target);
		edgeView = dummyView.getEdgeView(edge);
		
		dummyView.setVisualProperty(NETWORK_BACKGROUND_PAINT, BACKGROUND_COLOR);
		
		sourceView.setVisualProperty(NODE_FILL_COLOR, NODE_COLOR);
		targetView.setVisualProperty(NODE_FILL_COLOR, NODE_COLOR);
		sourceView.setVisualProperty(NODE_LABEL_COLOR, BACKGROUND_COLOR);
		targetView.setVisualProperty(NODE_LABEL_COLOR, BACKGROUND_COLOR);
		sourceView.setVisualProperty(NODE_SELECTED_PAINT, SELECTION_COLOR);
		targetView.setVisualProperty(NODE_SELECTED_PAINT, SELECTION_COLOR);
		sourceView.setVisualProperty(NODE_LABEL_FONT_SIZE, 16);
		targetView.setVisualProperty(NODE_LABEL_FONT_SIZE, 16);
		sourceView.setVisualProperty(NODE_LABEL, "S");
		targetView.setVisualProperty(NODE_LABEL, "T");
		
		sourceView.setVisualProperty(NODE_SHAPE, NodeShapeVisualProperty.ELLIPSE);
		targetView.setVisualProperty(NODE_SHAPE, NodeShapeVisualProperty.ELLIPSE);
		
		sourceView.setVisualProperty(NODE_WIDTH, 40d);
		sourceView.setVisualProperty(NODE_HEIGHT, 40d);
		sourceView.setVisualProperty(NODE_BORDER_PAINT, NODE_COLOR);
		targetView.setVisualProperty(NODE_WIDTH, 40d);
		targetView.setVisualProperty(NODE_HEIGHT, 40d);
		targetView.setVisualProperty(NODE_BORDER_PAINT, NODE_COLOR);
		
		edgeView.setVisualProperty(EDGE_SELECTED_PAINT, SELECTION_COLOR);
		edgeView.setVisualProperty(EDGE_STROKE_SELECTED_PAINT, SELECTION_COLOR);
		edgeView.setVisualProperty(EDGE_STROKE_UNSELECTED_PAINT, EDGE_COLOR);
		edgeView.setVisualProperty(EDGE_WIDTH, 4d);
		edgeView.setVisualProperty(EDGE_TARGET_ARROW_SHAPE, ArrowShapeVisualProperty.ARROW);
		edgeView.setVisualProperty(DVisualLexicon.EDGE_TARGET_ARROW_UNSELECTED_PAINT, EDGE_COLOR);
		edgeView.setVisualProperty(DVisualLexicon.EDGE_CURVED, true);
		
		if (startBend == null || startBend.equals(EDGE_BEND.getDefault()))
			startBend = new BendImpl();
		
		edgeView.setVisualProperty(EDGE_BEND, startBend);
		
		sourceView.setVisualProperty(NODE_X_LOCATION, 0d);
		sourceView.setVisualProperty(NODE_Y_LOCATION, 100d);
		targetView.setVisualProperty(NODE_X_LOCATION, 400d);
		targetView.setVisualProperty(NODE_Y_LOCATION, 120d);

		// Render it in this panel.  It is not necessary to register this engine to manager.
		presentationFactory.createRenderingEngine(innerPanel, dummyView);
		
		final InnerCanvas innerCanvas = (InnerCanvas) innerPanel.getComponent(0);
		innerCanvas.disablePopupMenu();
	}

	@Override
	public <S extends Bend> Bend showEditor(final Component parent, S initialValue) {
		if (!initialized) {
			init(parent);
			initialized = true;
		}
		
		editCancelled = false;
		bendRemoved = false;
		updateUI(initialValue);
		
		EditMode.setMode(true);
		
		if (parent != null)
			dialog.setLocationRelativeTo(parent);
		else
			dialog.setLocationByPlatform(true);

		dialog.pack();
		dummyView.fitContent();
		dialog.setVisible(true);
		
		EditMode.setMode(false);
		
		if (bendRemoved)
			return EDGE_BEND.getDefault();
		
		if (!editCancelled && edgeView instanceof DEdgeView)
			return ((DEdgeView)edgeView).getBend();
		
		return null;
	}

	@Override
	public Class<Bend> getValueType() {
		return Bend.class;
	}
}
