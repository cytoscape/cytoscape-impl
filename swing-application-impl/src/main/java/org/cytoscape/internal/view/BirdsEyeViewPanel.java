package org.cytoscape.internal.view;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.UIManager;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;

@SuppressWarnings("serial")
public class BirdsEyeViewPanel extends JPanel {
	
	private JPanel presentationPanel;
	
	private RenderingEngine<CyNetwork> engine;
	private final CyNetworkView networkView;
	
	private CyServiceRegistrar serviceRegistrar;

	public BirdsEyeViewPanel(final CyNetworkView networkView, final CyServiceRegistrar serviceRegistrar) {
		this.networkView = networkView;
		this.serviceRegistrar = serviceRegistrar;
		
		init();
	}

	public RenderingEngine<CyNetwork> getEngine() {
		return engine;
	}
	
	public final void update() {
		final Dimension currentPanelSize = getSize();
		getPresentationPanel().setSize(currentPanelSize);
		getPresentationPanel().setPreferredSize(currentPanelSize);
		
		if (engine == null) {
			final CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
			final NetworkViewRenderer renderer = applicationManager.getNetworkViewRenderer(networkView.getRendererId());
			final RenderingEngineFactory<CyNetwork> bevFactory =
					renderer.getRenderingEngineFactory(NetworkViewRenderer.BIRDS_EYE_CONTEXT);
			engine = bevFactory.createRenderingEngine(getPresentationPanel(), networkView);
		}
		
		repaint();
	}
	
	public void dispose() {
		if (engine != null)
			engine.dispose();
		
		getPresentationPanel().removeAll();
		repaint();
	}
	
	private void init() {
		setBackground(UIManager.getColor("Table.background"));
		
		setLayout(new BorderLayout());
		add(getPresentationPanel(), BorderLayout.CENTER);
	}
	
	protected JPanel getPresentationPanel() {
		if (presentationPanel == null) {
			presentationPanel = new JPanel();
		}
		
		return presentationPanel;
	}
}
