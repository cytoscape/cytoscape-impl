package org.cytoscape.internal.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;

import org.cytoscape.internal.util.ViewUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;

@SuppressWarnings("serial")
public class NetworkViewContainer extends JComponent implements RootPaneContainer {
	
	private final CyNetworkView networkView;
	private final RenderingEngineFactory<CyNetwork> engineFactory;
	private final RenderingEngine<CyNetwork> renderingEngine;

	protected final JRootPane rootPane;
	
	private final CyServiceRegistrar serviceRegistrar;

	public NetworkViewContainer(final CyNetworkView networkView, final RenderingEngineFactory<CyNetwork> engineFactory,
			final CyServiceRegistrar serviceRegistrar) {
		this.networkView = networkView;
		this.engineFactory = engineFactory;
		this.serviceRegistrar = serviceRegistrar;
		
		setName(ViewUtil.createUniqueKey(networkView));
		rootPane = new JRootPane();
		
		init();
		
		renderingEngine = engineFactory.createRenderingEngine(this, networkView);
	}
	
	@Override
	public JRootPane getRootPane() {
        return rootPane;
    }
	
	@Override
	public Container getContentPane() {
        return getRootPane().getContentPane();
    }
	
	@Override
	public void setContentPane(final Container c) {
		Container oldValue = getContentPane();
        getRootPane().setContentPane(c);
        firePropertyChange("contentPane", oldValue, c);
	}
	
	@Override
	public JLayeredPane getLayeredPane() {
		return getRootPane().getLayeredPane();
	}
	
	@Override
    public void setLayeredPane(JLayeredPane layered) {
        final JLayeredPane oldValue = getLayeredPane();
        getRootPane().setLayeredPane(layered);
        firePropertyChange("layeredPane", oldValue, layered);
    }
	
	@Override
	public Component getGlassPane() {
		return getRootPane().getGlassPane();
	}
	
	@Override
    public void setGlassPane(Component glass) {
        Component oldValue = getGlassPane();
        getRootPane().setGlassPane(glass);
        firePropertyChange("glassPane", oldValue, glass);
    }
	
	protected RenderingEngine<CyNetwork> getRenderingEngine() {
		return renderingEngine;
	}
	
	private void init() {
		setLayout(new BorderLayout());
		add(getRootPane(), BorderLayout.CENTER);
	}
	
	protected CyNetworkView getNetworkView() {
		return networkView;
	}
}
