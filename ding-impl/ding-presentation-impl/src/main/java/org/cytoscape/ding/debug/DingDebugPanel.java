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
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;

@SuppressWarnings("serial")
public class DingDebugPanel extends JPanel implements CytoPanelComponent {

	private DRenderingEngine re;
	
	private NetworkInfoPanel networkInfoPanel;
	private SettingsPanel settingsPanel;
	private FrameRatePanel frameRatePanel;
	private FrameListPanel frameListPanel;
	private ThumbnailPanel thumbnailPanel;
	
	
	public DingDebugPanel(CyServiceRegistrar registrar) {
		networkInfoPanel = new NetworkInfoPanel(() -> re);
		networkInfoPanel.setCollapsed(false);
		
		settingsPanel = new SettingsPanel(registrar);
		settingsPanel.setCollapsed(true);
		
		frameRatePanel = new FrameRatePanel();
		frameRatePanel.setCollapsed(false);
		
		frameListPanel = new FrameListPanel();
		frameListPanel.setCollapsed(true);
		
		thumbnailPanel = new ThumbnailPanel(registrar);
		thumbnailPanel.setCollapsed(true);
		
		JPanel panel = new JPanel();
		
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(networkInfoPanel)
			.addComponent(settingsPanel)
			.addComponent(frameRatePanel)
			.addComponent(frameListPanel)
			.addComponent(thumbnailPanel)
			.addGap(0, 500, Short.MAX_VALUE)
		);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(networkInfoPanel)
			.addComponent(settingsPanel)
			.addComponent(frameRatePanel)
			.addComponent(frameListPanel)
			.addComponent(thumbnailPanel)
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
	
	public SettingsPanel getSettingsPanel() {
		return settingsPanel;
	}
	
	public FrameListPanel getFrameListPanel() {
		return frameListPanel;
	}
	
	public FrameRatePanel getFrameRatePanel() {
		return frameRatePanel;
	}
	
	public ThumbnailPanel getThumbnailPanel() {
		return thumbnailPanel;
	}
	
	
	public void clear() {
		networkInfoPanel.clear();
		frameListPanel.clear();
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
