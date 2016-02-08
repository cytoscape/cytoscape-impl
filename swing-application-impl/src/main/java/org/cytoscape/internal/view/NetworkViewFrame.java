package org.cytoscape.internal.view;

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
	
	private final CyServiceRegistrar serviceRegistrar;

	public NetworkViewFrame(final NetworkViewContainer vc, final GraphicsConfiguration gc,
			final CyServiceRegistrar serviceRegistrar) {
		super(ViewUtil.getTitle(vc.getNetworkView()), gc);
		
		setName("Frame." + vc.getName());
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		this.networkViewContainer = vc;
		this.serviceRegistrar = serviceRegistrar;
		
		final JRootPane rp = vc.getRootPane();
		vc.setDetached(true);
		vc.setComparing(false);
		setRootPane(rp);
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
	public String toString() {
		return "NetworkViewFrame: " + getNetworkView();
	}
}
