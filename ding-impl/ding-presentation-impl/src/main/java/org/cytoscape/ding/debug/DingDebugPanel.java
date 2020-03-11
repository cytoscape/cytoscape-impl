package org.cytoscape.ding.debug;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;

@SuppressWarnings("serial")
public class DingDebugPanel extends JPanel implements CytoPanelComponent {

	private DRenderingEngine re;
	
	private NetworkInfoPanel networkInfoPanel;
	private IndividualFramesPanel framesPanel;
	
	
	public DingDebugPanel() {
		networkInfoPanel = new NetworkInfoPanel(() -> re);
		networkInfoPanel.setCollapsed(false);
		framesPanel = new IndividualFramesPanel();
		framesPanel.setCollapsed(false);
		
		JPanel panel = new JPanel() {
			@Override
			public java.awt.Dimension getPreferredSize() {
			    int h = super.getPreferredSize().height;
			    int w = getParent().getSize().width;
			    return new java.awt.Dimension(w, h);
			}
		};
		
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(networkInfoPanel)
			.addComponent(framesPanel)
		);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(networkInfoPanel)
			.addComponent(framesPanel)
		);

		JScrollPane scrollPane = new JScrollPane(panel, 
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
	            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		setLayout(new BorderLayout());
		add(BorderLayout.CENTER, scrollPane);
	}
	
	
	public NetworkInfoPanel getNetworkInfoPanel() {
		return networkInfoPanel;
	}
	
	
	public void clear() {
		networkInfoPanel.clear();
		framesPanel.clear();
	}
	
	public void setRenderingEngine(DRenderingEngine re) {
		this.re = re;
		if(re != null) {
			CyNetworkView netView = re.getViewModel();
			CyNetwork model = netView.getModel();
			String name = model.getRow(model).get(CyNetwork.NAME, String.class);
			networkInfoPanel.setNetworkName(name);
		} else {
			networkInfoPanel.setNetworkName(null);
		}
	}
	
	
	public void addFrame(DebugFrameType type, boolean cancelled, int nodeCount, int edgeCountEstimate, long time) {
		framesPanel.addFrame(type, cancelled, nodeCount, edgeCountEstimate, time);
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
