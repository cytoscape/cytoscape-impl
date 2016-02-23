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
		
		// To prevent error this error when using multiple monitors:
		// "IllegalArgumentException: adding a container to a container on a different GraphicsDevice".
		vc.setRootPane(new JRootPane());
		
		getContentPane().add(containerRootPane, BorderLayout.CENTER);
		update();
	}
	
	protected NetworkViewContainer getNetworkViewContainer() {
		return networkViewContainer;
	}
	
	/**
	 * Use this method to get the original NetworkViewContainer's JRootPane
	 * instead of {@link #getNetworkViewContainer()#getRootPane()}.
	 * @return The JRootPane that contains the rendered view.
	 */
	protected JRootPane getContainerRootPane() {
		return containerRootPane;
	}
	
	protected RenderingEngine<CyNetwork> getRenderingEngine() {
		return networkViewContainer.getRenderingEngine();
	}
	
	protected CyNetworkView getNetworkView() {
		return networkViewContainer.getNetworkView();
	}
	
	public void update() {
		getNetworkViewContainer().update();
	}
	
	@Override
	public void dispose() {
		// To prevent error this error when using multiple monitors:
		// "IllegalArgumentException: adding a container to a container on a different GraphicsDevice".
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
