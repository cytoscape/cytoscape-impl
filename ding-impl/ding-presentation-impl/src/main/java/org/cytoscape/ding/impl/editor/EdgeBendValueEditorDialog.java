package org.cytoscape.ding.impl.editor;

import static javax.swing.GroupLayout.Alignment.LEADING;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.*;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;

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
import org.cytoscape.ding.impl.DingNetworkViewFactory;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.Bend;

public class EdgeBendValueEditorDialog {

	private static final Dimension DEF_PANEL_SIZE = new Dimension(600, 400);
	
	private JDialog dialog;
	private SimpleRootPaneContainer innerPanel;
	private CyNetworkView dummyView;
	private View<CyEdge> edgeView;

	private final DingNetworkViewFactory cyNetworkViewFactory;
	private final RenderingEngineFactory<CyNetwork> presentationFactory;
	private final CyServiceRegistrar serviceRegistrar;

	private boolean editCancelled;
	private boolean bendRemoved;

	public EdgeBendValueEditorDialog(
			final DingNetworkViewFactory cyNetworkViewFactory,
			final RenderingEngineFactory<CyNetwork> presentationFactory,
			final CyServiceRegistrar serviceRegistrar
	) {
		this.cyNetworkViewFactory = Objects.requireNonNull(cyNetworkViewFactory, "CyNetworkViewFactory is null.");
		this.presentationFactory  = Objects.requireNonNull(presentationFactory, "RenderingEngineFactory is null.");
		this.serviceRegistrar = serviceRegistrar;
	}

	@SuppressWarnings("serial")
	private void init(final Component parent) {
		final Window owner = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
		dialog = new JDialog(owner, ModalityType.APPLICATION_MODAL);
		dialog.setTitle("Edge Bend Editor");
		dialog.setResizable(false);
		
		dialog.addWindowListener(new WindowAdapter() {
			@Override public void windowClosing(WindowEvent e) {
				editCancelled = true;
			}
		});
		dialog.setPreferredSize(DEF_PANEL_SIZE);

		String newHandleAction = getHandleActionLabel();
		final JLabel infoLabel = new JLabel(
				"<html><b>1. <i>" + newHandleAction + "</i></b> the edge to add a new handle.<br />" +
				"<b>2. </b>Drag handles to bend (select the edge first).</html>"
		);
		infoLabel.setFont(infoLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
		
		innerPanel = new SimpleRootPaneContainer();
		innerPanel.setBackground(UIManager.getColor("Table.background"));
		innerPanel.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")));

		final JButton okButton = new JButton(new AbstractAction("OK") {
			@Override public void actionPerformed(ActionEvent e) {
				editCancelled = false;
				dialog.dispose();
			}
		});
		
		final JButton cancelButton = new JButton(new AbstractAction("Cancel") {
			@Override public void actionPerformed(ActionEvent e) {
				editCancelled = true;
				dialog.dispose();
			}
		});
		
		final JButton removeBendButton = new JButton(new AbstractAction("Remove Bend") {
			@Override public void actionPerformed(ActionEvent e) {
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
	
	private String getHandleActionLabel() {
		final String osName = System.getProperty("os.name").toLowerCase();
		if (osName.contains("windows"))
			return "Alt-click";
		else if (osName.contains("mac"))
			return "Option-click";
		else
			return "Ctrl-Alt-click";
	}
	
	private void updateUI(Bend startBend) {
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
	}
	
	
	public boolean isEditCancelled() {
		return editCancelled;
	}

	public Bend showDialog(Component parent, Bend initialValue) {
		init(parent);
		
		editCancelled = false;
		bendRemoved = false;
		updateUI(initialValue);
		
		if (parent != null)
			dialog.setLocationRelativeTo(parent);
		else
			dialog.setLocationByPlatform(true);

		dialog.pack();
		dummyView.fitContent();
		dialog.setVisible(true);
		
		cyNetworkViewFactory.removeRenderingEngine(dummyView);
		
		// very important to dispose the network view to prevent memory leaks
		dummyView.dispose();
		
		dialog.dispose();
		
		if (bendRemoved)
			return EDGE_BEND.getDefault();
		
		if (!editCancelled)
			return edgeView.getVisualProperty(EDGE_BEND);
		
		return null;
	}

}
