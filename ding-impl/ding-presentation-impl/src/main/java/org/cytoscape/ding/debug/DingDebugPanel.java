package org.cytoscape.ding.debug;

import java.awt.Component;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.canvas.NetworkTransform;
import org.cytoscape.ding.internal.util.ViewUtil;
import org.cytoscape.graph.render.stateful.RenderDetailFlags;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;

@SuppressWarnings("serial")
public class DingDebugPanel extends JPanel implements CytoPanelComponent {

	private DRenderingEngine re;
	
	private JLabel networkNameLabel;
	private JLabel edgeCountLabel;
	
	private JLabel transformViewLabel;
	private JLabel transformCntrLabel;
	private JLabel transformZoomLabel;
	private JLabel selectedNodeLabel;
	
	private JCheckBox logCheckbox;
	
	private FramePanel fastPanel;
	private FramePanel slowPanel;
	private FramePanel fastBirdPanel;
	private FramePanel slowBirdPanel;
	
	private Map<DebugFrameType,Integer> frameCount = new EnumMap<>(DebugFrameType.class);
	
	
	public DingDebugPanel() {
		networkNameLabel = new JLabel("Network Name");
		transformViewLabel = new JLabel("");
		transformCntrLabel = new JLabel("");
		transformZoomLabel = new JLabel("");
		edgeCountLabel = new JLabel("");
		LookAndFeelUtil.makeSmall(transformViewLabel, transformCntrLabel, transformZoomLabel);
		
		fastPanel = new FramePanel("Main Fast (on EDT)");
		slowPanel = new FramePanel("Main Slow (Async)");
		fastBirdPanel = new FramePanel("Birds-Eye-View Fast (on EDT)");
		slowBirdPanel = new FramePanel("Birds-Eye-View Slow (Async)");
		
		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(e -> clear());
		JButton edgeButton = new JButton("Count Edges");
		edgeButton.addActionListener(e -> countEdges());
		
		selectedNodeLabel = new JLabel("Selected Node - none");
		
		logCheckbox = new JCheckBox("log to console");
		
		LookAndFeelUtil.makeSmall(edgeButton, edgeCountLabel, logCheckbox, selectedNodeLabel);
		
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(networkNameLabel)
			.addComponent(transformViewLabel)
			.addComponent(transformCntrLabel)
			.addComponent(transformZoomLabel)
			.addComponent(selectedNodeLabel)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(edgeButton)
				.addComponent(edgeCountLabel)
			)
			.addGroup(layout.createParallelGroup()
				.addComponent(fastPanel)
				.addComponent(slowPanel)
			)
			.addGroup(layout.createParallelGroup()
				.addComponent(fastBirdPanel)
				.addComponent(slowBirdPanel)
			)
			.addGroup(layout.createParallelGroup(Alignment.BASELINE)
				.addComponent(clearButton)
				.addComponent(logCheckbox)
			)
		);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(networkNameLabel)
			.addComponent(transformViewLabel)
			.addComponent(transformCntrLabel)
			.addComponent(transformZoomLabel)
			.addComponent(selectedNodeLabel)
			.addGroup(layout.createSequentialGroup()
				.addComponent(edgeButton)
				.addComponent(edgeCountLabel)
			)
			.addGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
					.addComponent(fastPanel)
					.addComponent(fastBirdPanel)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(slowPanel)
					.addComponent(slowBirdPanel)
				)
			)
			.addGroup(layout.createSequentialGroup()
				.addComponent(clearButton)
				.addGap(0, Short.MAX_VALUE, Short.MAX_VALUE)
				.addComponent(logCheckbox)
			)
		);
	}
	
	public void clear() {
		fastPanel.clear();
		slowPanel.clear();
		fastBirdPanel.clear();
		slowBirdPanel.clear();
		frameCount.clear();
	}
	
	private void countEdges() {
		String text = "-";
		if(re != null)
			text = "" + RenderDetailFlags.countEdges(re);
		edgeCountLabel.setText(text);
	}
	
	public void updateTransform(NetworkTransform t) {
		transformViewLabel.setText(String.format("Viewport - w:%d h:%d",           t.getWidth(), t.getHeight()));
		transformCntrLabel.setText(String.format("Network Center - x:%.4f y:%.4f", t.getCenterX(), t.getCenterY()));
		transformZoomLabel.setText(String.format("Zoom - %.4f",                    t.getScaleFactor()));
	}
	
	public void setRenderingEngine(DRenderingEngine re) {
		this.re = re;
		if(re != null) {
			CyNetworkView netView = re.getViewModel();
			CyNetwork model = netView.getModel();
			String name = model.getRow(model).get(CyNetwork.NAME, String.class);
			networkNameLabel.setText(name);
		} else {
			networkNameLabel.setText(" -- none -- ");
		}
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
	
	
	private FramePanel getPanel(DebugFrameType type) {
		switch(type) {
			case MAIN_ANNOTAITONS:
			case MAIN_EDGES:
			case MAIN_FAST: return fastPanel;
			case MAIN_SLOW: return slowPanel;
			case BEV_FAST:  return fastBirdPanel;
			case BEV_SLOW:  return slowBirdPanel;
		}
		return null;
	}
	
	public void start(DebugFrameType type) {
		int frameNumber = frameCount.merge(type, 0, (x,y) -> x + 1);
		if(logCheckbox.isSelected()) { 
			System.out.println(type + " " + frameNumber + " start");
		}
	}
	
	public void done(DebugFrameType type, boolean cancelled, int nodeCount, int edgeCountEstimate, long time) {
		DebugEntry entry = new DebugEntry(time, cancelled, type, nodeCount, edgeCountEstimate);
		int frameNumber = frameCount.getOrDefault(type, 0);
		if(logCheckbox.isSelected()) { 
			System.out.println(type + " " + frameNumber + " done (" + time + ")");
		}
		ViewUtil.invokeOnEDT(() -> {
			getPanel(type).addEntry(entry);
		});
	}
	
	
	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	@Override
	public String getTitle() {
		return "Ding Debug";
	}

	@Override
	public Icon getIcon() {
		return null;
	}
}
