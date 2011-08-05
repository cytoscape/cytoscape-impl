package org.cytoscape.view.vizmap.gui.internal;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.session.events.SetCurrentRenderingEngineEvent;
import org.cytoscape.session.events.SetCurrentRenderingEngineListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.cytoscape.view.vizmap.gui.event.SelectedVisualStyleSwitchedEvent;
import org.cytoscape.view.vizmap.gui.event.SelectedVisualStyleSwitchedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SelectedVisualStyleManagerImpl implements
		SelectedVisualStyleManager, SelectedVisualStyleSwitchedListener, SetCurrentRenderingEngineListener {
	
	private static final Logger logger = LoggerFactory.getLogger(SelectedVisualStyleManagerImpl.class);
	
	private final VisualMappingManager vmm;
	
	private VisualStyle selectedStyle;
	
	protected final VisualStyle defaultVS;
	
	public SelectedVisualStyleManagerImpl(final VisualMappingManager vmm) {
		if(vmm == null)
			throw new NullPointerException("Visual Mapping Manager is missing.");
		this.vmm = vmm;
		
		this.defaultVS = vmm.getDefaultVisualStyle();
		this.selectedStyle = this.defaultVS;
	}

	public VisualStyle getDefaultVisualStyle() {
		return defaultVS;
	}
	

	@Override
	public void handleEvent(SelectedVisualStyleSwitchedEvent e) {
		final VisualStyle style = e.getNewVisualStyle();
		if(style == null)
			throw new NullPointerException("Tried to set selected Visual Style to null.");
		
		this.selectedStyle = style;
		logger.debug("========= Selected Style Switched to " + selectedStyle.getTitle());
	}

	@Override
	public VisualStyle getCurrentVisualStyle() {
		return selectedStyle;
	}

	@Override
	public VisualStyle getDefaultStyle() {
		return defaultVS;
	}

	@Override
	public void handleEvent(SetCurrentRenderingEngineEvent e) {
		logger.debug("Presentation switched: " + e.getRenderingEngine());
		final RenderingEngine<CyNetwork> engine = e.getRenderingEngine();
		final VisualStyle targetStyle = vmm.getVisualStyle((CyNetworkView) engine.getViewModel());
		logger.debug("New Style ========= " + targetStyle.getTitle());
		if(targetStyle != this.selectedStyle) {
			selectedStyle = targetStyle;
			logger.debug("Presentation switch ========= Selected Style Switched to " + selectedStyle.getTitle());
		}
	}
}
