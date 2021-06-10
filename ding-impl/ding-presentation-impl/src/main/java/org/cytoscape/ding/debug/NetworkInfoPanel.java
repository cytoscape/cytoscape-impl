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
	private JLabel selectedNodeLabel;
	
	private NetworkViewportPanel viewportPanel;
	
	
	public NetworkInfoPanel(Supplier<DRenderingEngine> reSupplier) {
		super("Network View and Transform");
		this.reSupplier = reSupplier;
		createContents();
	}
	
	private void createContents() {
		networkNameLabel = new JLabel();
		transformViewLabel = new JLabel();
		
		viewportPanel = new NetworkViewportPanel();
		edgeCountLabel = new JLabel();
		selectedNodeLabel = new JLabel();
		clear();
		
		JButton countButton = new JButton("Count Visible Nodes/Edges");
		countButton.addActionListener(e -> countNodesEdges());
		
		LookAndFeelUtil.makeSmall(edgeCountLabel, selectedNodeLabel, countButton);
		LookAndFeelUtil.makeSmall(transformViewLabel);
		
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(networkNameLabel)
			.addComponent(viewportPanel)
			.addComponent(transformViewLabel)
			.addComponent(selectedNodeLabel)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(countButton)
				.addComponent(edgeCountLabel)
			)
		);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(networkNameLabel)
			.addComponent(viewportPanel, Alignment.CENTER)
			.addComponent(transformViewLabel)
			.addComponent(selectedNodeLabel)
			.addGroup(layout.createSequentialGroup()
				.addComponent(countButton)
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
		viewportPanel.clear();
		edgeCountLabel.setText("");
		selectedNodeLabel.setText("Selected Node - none");
	}
	
	public void updateTransform(NetworkTransform t) {
		transformViewLabel.setText(String.format("Viewport - w:%d h:%d", t.getWidth(), t.getHeight()));
		viewportPanel.updateTransform(t);
	}
	
	public void setNetworkName(String name) {
		networkNameLabel.setText(name == null ? "-none-" : name);
	}
	
	private void countNodesEdges() {
		String text = "-";
		var re = reSupplier.get();
		if(re != null) {
			int[] counts = RenderDetailFlags.countNodesEdges(re);
			int nodeCount = counts[0];
			int edgeCount = counts[1];
			text = "nodes: " + nodeCount + ", edges: " + edgeCount;
		}
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
