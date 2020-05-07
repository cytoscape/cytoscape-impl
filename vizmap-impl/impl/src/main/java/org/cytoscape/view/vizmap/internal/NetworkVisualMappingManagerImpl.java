package org.cytoscape.view.vizmap.internal;

import java.awt.Color;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.events.SetCurrentVisualStyleEvent;
import org.cytoscape.view.vizmap.events.VisualStyleAboutToBeRemovedEvent;
import org.cytoscape.view.vizmap.events.VisualStyleAddedEvent;
import org.cytoscape.view.vizmap.events.VisualStyleSetEvent;

/*
 * #%L
 * Cytoscape VizMap Impl (vizmap-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

public class NetworkVisualMappingManagerImpl extends AbstractVisualMappingManager<CyNetworkView> implements VisualMappingManager, SetCurrentNetworkViewListener {

	// Default Style
	private static final Color NETWORK_COLOR = Color.WHITE;
	private static final Color NODE_COLOR = new Color(0x4F, 0x94, 0xCD);
	private static final Color NODE_LABEL_COLOR = Color.BLACK;
	private static final Color EDGE_COLOR = new Color(50, 50, 50);
	private static final Double EDGE_WIDTH = 2d;
	private static final Double NODE_WIDTH = 35d;
	private static final Double NODE_HEIGHT = 35d;
	private static final Color EDGE_LABEL_COLOR = Color.BLACK;
	
	private final CyServiceRegistrar serviceRegistrar;
	
		
	public NetworkVisualMappingManagerImpl(VisualStyleFactory factory, CyServiceRegistrar serviceRegistrar) {
		super(factory, serviceRegistrar);
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	protected VisualStyle buildGlobalDefaultStyle(final VisualStyleFactory factory) {
		VisualStyle defStyle = factory.createVisualStyle(DEFAULT_STYLE_NAME);
		defStyle.setDefaultValue(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT, NETWORK_COLOR);
		defStyle.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, NODE_COLOR);
		defStyle.setDefaultValue(BasicVisualLexicon.NODE_LABEL_COLOR, NODE_LABEL_COLOR);
		defStyle.setDefaultValue(BasicVisualLexicon.NODE_WIDTH, NODE_WIDTH);
		defStyle.setDefaultValue(BasicVisualLexicon.NODE_HEIGHT, NODE_HEIGHT);
		defStyle.setDefaultValue(BasicVisualLexicon.EDGE_WIDTH, EDGE_WIDTH);
		defStyle.setDefaultValue(BasicVisualLexicon.EDGE_PAINT, EDGE_COLOR);
		defStyle.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_COLOR, EDGE_LABEL_COLOR);
		return defStyle;
	}

	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		CyNetworkView view = e.getNetworkView();
		if (view == null)
			return;
		VisualStyle newStyle = this.getVisualStyle(view);
		if (newStyle != null)
			setCurrentVisualStyle(newStyle);
	}

	@Override
	protected CyNetworkView getCurrentView() {
		final CyApplicationManager appManager = serviceRegistrar.getService(CyApplicationManager.class);
		return appManager.getCurrentNetworkView();
	}

	@Override
	protected void fireChangeEvent(VisualStyle vs, CyNetworkView view) {
		CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		eventHelper.fireEvent(new VisualStyleSetEvent(this, vs, view));
	}

	@Override
	protected void fireAddEvent(VisualStyle vs) {
		CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		eventHelper.fireEvent(new VisualStyleAddedEvent(this, vs));
	}

	@Override
	protected void fireRemoveEvent(VisualStyle vs) {
		CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		eventHelper.fireEvent(new VisualStyleAboutToBeRemovedEvent(this, vs));
	}

	@Override
	protected void fireSetCurrentEvent(VisualStyle vs) {
		CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		eventHelper.fireEvent(new SetCurrentVisualStyleEvent(this, vs));
	}
}
