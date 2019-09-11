package org.cytoscape.ding.debug;

import java.awt.Component;

import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.DingRenderer;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;

@SuppressWarnings("serial")
public class DingDebugPanel extends JPanel implements CytoPanelComponent, DebugCallback, SetCurrentNetworkViewListener {

	private final CyServiceRegistrar registrar;
	
	private DRenderingEngine re;
	
	private JLabel networkNameLabel;
	private FramePanel fastPanel;
	private FramePanel slowPanel;
	private FramePanel fastBirdPanel;
	private FramePanel slowBirdPanel;
	
	
	public DingDebugPanel(CyServiceRegistrar registrar) {
		this.registrar = registrar;
		createContents();
	}

	private void createContents() {
		networkNameLabel = new JLabel("Network Name");
		
		fastPanel = new FramePanel("Main Fast (on EDT)");
		slowPanel = new FramePanel("Main Slow (Async)");
		fastBirdPanel = new FramePanel("Birds-Eye-View Fast (on EDT)");
		slowBirdPanel = new FramePanel("Birds-Eye-View Slow (Async)");
		
		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(e -> clear());
		
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(networkNameLabel)
			.addGroup(layout.createParallelGroup()
				.addComponent(fastPanel)
				.addComponent(slowPanel)
			)
			.addGroup(layout.createParallelGroup()
				.addComponent(fastBirdPanel)
				.addComponent(slowBirdPanel)
			)
			.addComponent(clearButton)
		);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(networkNameLabel)
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
			.addComponent(clearButton)
		);
	}
	
	private void clear() {
		fastPanel.clear();
		slowPanel.clear();
		fastBirdPanel.clear();
		slowBirdPanel.clear();
	}
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		clear();
		if(re != null)
			re.setDebugCallback(null);
		
		CyNetworkView netView = e.getNetworkView();
		if(netView != null) {
			CyNetwork model = netView.getModel();
			String name = model.getRow(model).get(CyNetwork.NAME, String.class);
			networkNameLabel.setText(name);
			
			DingRenderer dingRenderer = registrar.getService(DingRenderer.class);
			re = dingRenderer.getRenderingEngine(netView);
			if(re != null)
				re.setDebugCallback(this);
		}
	}
	
	
	private FramePanel getPanel(DebugFrameType type) {
		switch(type) {
			case MAIN_ANNOTAITONS:
			case MAIN_FAST: return fastPanel;
			case MAIN_SLOW: return slowPanel;
			case BEV_FAST:  return fastBirdPanel;
			case BEV_SLOW:  return slowBirdPanel;
		}
		return null;
	}
	
	@Override
	public void addFrameTime(DebugFrameType type, boolean cancelled, long time) {
		String message;
		if(type == DebugFrameType.MAIN_ANNOTAITONS)
			message = "(annotations) " + time;
		else if(cancelled)
			message = "(cancelled) " + time;
		else
			message = "" + time;
		
		getPanel(type).addMessage(message);
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
