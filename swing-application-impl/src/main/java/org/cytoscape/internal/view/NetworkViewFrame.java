package org.cytoscape.internal.view;

import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;

import javax.swing.JFrame;
import javax.swing.JRootPane;

import org.cytoscape.internal.util.ViewUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;

@SuppressWarnings("serial")
public class NetworkViewFrame extends JFrame {

	private final NetworkViewContainer networkViewContainer;
	private final JRootPane containerRootPane;
	
	private final CyServiceRegistrar serviceRegistrar;

	public NetworkViewFrame(final NetworkViewContainer vc, final GraphicsConfiguration gc,
			final CyServiceRegistrar serviceRegistrar) {
		super(ViewUtil.getTitle(vc.getNetworkView()), gc);
		
		setName("Frame." + vc.getName());
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		this.networkViewContainer = vc;
		this.serviceRegistrar = serviceRegistrar;
		
		containerRootPane = vc.getRootPane();
		
		getContentPane().add(containerRootPane, BorderLayout.CENTER);
		vc.update();
	}
	
	protected NetworkViewContainer getNetworkViewContainer() {
		return networkViewContainer;
	}
	
	protected RenderingEngine<CyNetwork> getRenderingEngine() {
		return networkViewContainer.getRenderingEngine();
	}
	
	protected CyNetworkView getNetworkView() {
		return networkViewContainer.getNetworkView();
	}
	
	@Override
	public void dispose() {
		// To prevent error
		// "IllegalArgumentException: adding a container to a container on a different GraphicsDevice"
		// when using multiple monitors
		getContentPane().removeAll();
		remove(getRootPane());
		
		super.dispose();
		
		networkViewContainer.setRootPane(containerRootPane);
	}
	
	@Override
	public String toString() {
		return "NetworkViewFrame: " + getNetworkView();
	}
}
