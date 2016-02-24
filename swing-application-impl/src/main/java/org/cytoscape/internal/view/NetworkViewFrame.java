package org.cytoscape.internal.view;

import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.JToolBar;

import org.cytoscape.internal.util.ViewUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;

@SuppressWarnings("serial")
public class NetworkViewFrame extends JFrame {

	private final JToolBar toolBar;
	private final NetworkViewContainer networkViewContainer;
	private final JRootPane containerRootPane;
	
	private final CyServiceRegistrar serviceRegistrar;

	public NetworkViewFrame(final NetworkViewContainer vc, final GraphicsConfiguration gc,
			final JToolBar toolBar, final CyServiceRegistrar serviceRegistrar) {
		super(ViewUtil.getTitle(vc.getNetworkView()), gc);
		
		setName("Frame." + vc.getName());
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		this.toolBar = toolBar;
		this.networkViewContainer = vc;
		this.serviceRegistrar = serviceRegistrar;
		containerRootPane = vc.getRootPane();
		
		// To prevent error this error when using multiple monitors:
		// "IllegalArgumentException: adding a container to a container on a different GraphicsDevice".
		vc.setRootPane(new JRootPane());
		
		getContentPane().add(containerRootPane, BorderLayout.CENTER);
		
		if (toolBar != null) {
			toolBar.setFloatable(false);
			
			final Properties props = (Properties) 
					serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)").getProperties();
			setToolBarVisible(props.getProperty("showDetachedViewToolBars", "true").equalsIgnoreCase("true"));
			
			getContentPane().add(toolBar, BorderLayout.NORTH);
		}
		
		update();
	}
	
	public JToolBar getToolBar() {
		return toolBar;
	}
	
	public void setToolBarVisible(final boolean b) {
		getToolBar().setVisible(b);
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
