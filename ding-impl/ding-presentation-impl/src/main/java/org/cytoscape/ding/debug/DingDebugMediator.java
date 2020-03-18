package org.cytoscape.ding.debug;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Properties;

import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.DingRenderer;
import org.cytoscape.ding.impl.TransformChangeListener;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.events.SelectedNodesAndEdgesEvent;
import org.cytoscape.model.events.SelectedNodesAndEdgesListener;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.PropertyUpdatedEvent;
import org.cytoscape.property.PropertyUpdatedListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

public class DingDebugMediator implements DebugProgressMonitorCallback, TransformChangeListener, 
	SetCurrentNetworkViewListener, SelectedNodesAndEdgesListener, PropertyUpdatedListener {

	private final CyServiceRegistrar registrar;
	private final CyProperty<Properties> cyProps;
	private final DingDebugPanel debugPanel;
	
	private DRenderingEngine currentRE;
	
	
	private final LinkedList<DebugRootProgressMonitor> frameList = new LinkedList<>();
	
	
	
	@SuppressWarnings("unchecked")
	public DingDebugMediator(CyServiceRegistrar registrar) {
		this.registrar = registrar;
		this.debugPanel = new DingDebugPanel(registrar);
		this.cyProps = registrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)");
		registrar.registerService(debugPanel, CytoPanelComponent.class, new Properties());
	}
	
	public static boolean showDebugPanel(CyServiceRegistrar registrar) {
		@SuppressWarnings("unchecked")
		CyProperty<Properties> cyProp = registrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)");
		return cyProp != null && "true".equalsIgnoreCase(cyProp.getProperties().getProperty("showDebugPanel"));
	}
	
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		debugPanel.clear();
		
		if(currentRE != null) {
			currentRE.setDebugProgressMonitorFactory(null);
			currentRE.removeTransformChangeListener(this);
		}
		
		CyNetworkView netView = e.getNetworkView();
		if(netView == null) {
			currentRE = null; // avoid memory leak
		} else {
			DingRenderer dingRenderer = registrar.getService(DingRenderer.class);
			currentRE = dingRenderer.getRenderingEngine(netView);
			if(currentRE != null) {
				currentRE.setDebugProgressMonitorFactory(new DebugProgressMonitorFactory(this));
				currentRE.addTransformChangeListener(this);
			}
		}
		debugPanel.setRenderingEngine(currentRE);
	}


	@Override
	public void handleEvent(SelectedNodesAndEdgesEvent event) {
		Collection<CyNode> nodes = event.getSelectedNodes();
		int nodeCount = nodes.size();
		
		if(nodeCount == 1) {
			CyNode node = nodes.iterator().next();
			View<CyNode> nodeView = currentRE.getViewModel().getNodeView(node);
			if(nodeView != null) {
				var x = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
				var y = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
				var w = nodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH);
				var h = nodeView.getVisualProperty(BasicVisualLexicon.NODE_HEIGHT);
				debugPanel.getNetworkInfoPanel().setSingleNode(x, y, w, h);
				return;
			}
		} 
		
		debugPanel.getNetworkInfoPanel().setSelectedNodesInfo(nodeCount);
	}
	
	@Override
	public void handleEvent(PropertyUpdatedEvent e) {
		if(Objects.equals(e.getSource(), cyProps)) {
			debugPanel.getSettingsPanel().update();
		}
	}
	

	@Override
	public void transformChanged() {
		debugPanel.getNetworkInfoPanel().updateTransform(currentRE.getTransform());
	}

	@Override
	public void addFrame(DebugRootFrameInfo frame) {
		debugPanel.getFrameListPanel().addFrame(frame);
		debugPanel.getFrameRatePanel().addFrame(frame);
	}
	
}
