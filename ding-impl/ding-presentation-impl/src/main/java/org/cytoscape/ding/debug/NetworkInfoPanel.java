package org.cytoscape.ding.debug;

import java.awt.BorderLayout;
import java.util.function.Supplier;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.canvas.NetworkTransform;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;
import org.cytoscape.util.swing.BasicCollapsiblePanel;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class NetworkInfoPanel extends BasicCollapsiblePanel {

	private final Supplier<DRenderingEngine> reSupplier;
	
	private JLabel networkNameLabel;
	private JLabel edgeCountLabel;
	private JLabel transformViewLabel;
	private JLabel transformCntrLabel;
	private JLabel transformBoundsLabel;
	private JLabel transformZoomLabel;
	private JLabel selectedNodeLabel;
	
	
	public NetworkInfoPanel(Supplier<DRenderingEngine> reSupplier) {
		super("Network View and Transform");
		this.reSupplier = reSupplier;
		createContents();
	}
	
	private void createContents() {
		networkNameLabel = new JLabel();
		transformViewLabel = new JLabel();
		transformCntrLabel = new JLabel();
		transformBoundsLabel = new JLabel();
		transformZoomLabel = new JLabel();
		edgeCountLabel = new JLabel();
		selectedNodeLabel = new JLabel();
		clear();
		
		JButton edgeButton = new JButton("Count Edges");
		edgeButton.addActionListener(e -> countEdges());
		
		LookAndFeelUtil.makeSmall(edgeCountLabel, selectedNodeLabel, edgeButton);
		LookAndFeelUtil.makeSmall(transformViewLabel, transformCntrLabel, transformBoundsLabel, transformZoomLabel);
		
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(networkNameLabel)
			.addComponent(transformViewLabel)
			.addComponent(transformCntrLabel)
			.addComponent(transformBoundsLabel)
			.addComponent(transformZoomLabel)
			.addComponent(selectedNodeLabel)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(edgeButton)
				.addComponent(edgeCountLabel)
			)
		);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(networkNameLabel)
			.addComponent(transformViewLabel)
			.addComponent(transformCntrLabel)
			.addComponent(transformBoundsLabel)
			.addComponent(transformZoomLabel)
			.addComponent(selectedNodeLabel)
			.addGroup(layout.createSequentialGroup()
				.addComponent(edgeButton)
				.addComponent(edgeCountLabel)
			)
		);
		
		JPanel content = getContentPane();
		content.setLayout(new BorderLayout());
		content.add(BorderLayout.WEST, panel);
	}
	
	public void clear() {
		networkNameLabel.setText("-none-");
		transformViewLabel.setText("");
		transformCntrLabel.setText("");
		transformBoundsLabel.setText("");
		transformZoomLabel.setText("");
		edgeCountLabel.setText("");
		selectedNodeLabel.setText("Selected Node - none");
	}
	
	public void updateTransform(NetworkTransform t) {
		var b = t.getNetworkVisibleAreaNodeCoords();
		transformViewLabel.setText(String.format("Viewport - w:%d h:%d", t.getWidth(), t.getHeight()));
		transformCntrLabel.setText(String.format("Center - x:%.2f, y:%.2f", t.getCenterX(), t.getCenterY()));
		transformBoundsLabel.setText(String.format("Bounds - xMin:%.2f, yMin:%.2f, xMax:%.2f, yMax:%.2f", b.getMinX(), b.getMinY(), b.getMaxX(), b.getMaxY()));
		transformZoomLabel.setText(String.format("Zoom - %.4f", t.getScaleFactor()));
	}
	
	public void setNetworkName(String name) {
		networkNameLabel.setText(name == null ? "-none-" : name);
	}
	
	private void countEdges() {
		String text = "-";
		var re = reSupplier.get();
		if(re != null)
			text = "" + RenderDetailFlags.countEdges(re);
		edgeCountLabel.setText(text);
	}
	
	public void setSelectedNodesInfo(int nodeCount) {
		if(nodeCount == 0)
			selectedNodeLabel.setText("Selected Node - none");
		else
			selectedNodeLabel.setText("Selected Node - " + nodeCount + " nodes selected");
	}
	
	public void setSingleNode(double x, double y, double w, double h) {
		selectedNodeLabel.setText("Selected Node - " + String.format("(x:%.2f, y:%.2f, w:%.2f, h:%.2f)", x, y, w, h));
	}
	
}
