package org.cytoscape.io.internal.read.xgmml;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.io.InputStream;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.internal.read.xgmml.handler.ReadDataManager;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.work.TaskMonitor;

/**
 * This reader handles XGMML files that serialize CyNetworkViews in Cy3 session files.
 */
public class SessionXGMMLNetworkViewReader extends GenericXGMMLReader {

	private boolean settingLockedVisualProperties;
	
	public SessionXGMMLNetworkViewReader(final InputStream inputStream,
										 final CyNetworkFactory cyNetworkFactory,
										 final RenderingEngineManager renderingEngineMgr,
										 final ReadDataManager readDataMgr,
										 final XGMMLParser parser,
										 final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr,
										 final CyNetworkManager cyNetworkManager, 
										 final CyRootNetworkManager cyRootNetworkManager,
										 final CyApplicationManager cyApplicationManager) {
		super(inputStream, cyNetworkFactory, renderingEngineMgr, readDataMgr, parser, unrecognizedVisualPropertyMgr,
				cyNetworkManager, cyRootNetworkManager, cyApplicationManager);
	}

	public String getVisualStyleName() {
		return readDataMgr.getVisualStyleName();
	}
	
	@Override
	protected void init(final TaskMonitor tm) {
		super.init(tm);
		readDataMgr.setViewFormat(true);
	}
	
	@Override
	protected void setNetworkViewProperties(final CyNetworkView netView) {
		// Direct visual properties
		Map<String, String> atts = readDataMgr.getViewGraphicsAttributes(readDataMgr.getNetworkId(), false);
		this.settingLockedVisualProperties = false;
		setVisualProperties(netView, netView, atts);
		
		// Locked visual properties
		atts = readDataMgr.getViewGraphicsAttributes(readDataMgr.getNetworkId(), true);
		this.settingLockedVisualProperties = true;
		setVisualProperties(netView, netView, atts);
		
		// Cache the view with its former SUID
		readDataMgr.getCache().cache(readDataMgr.getNetworkViewId(), netView);
	}

	@Override
	protected void setNodeViewProperties(final CyNetworkView netView, final View<CyNode> nodeView) {
		final CyNode node = nodeView.getModel();
		
		// When parsing view-format XGMML, the manager does not have the network model
		// to create a map by CyNode objects, so the graphics mapping is indexed by the old element id.
		final Object oldId = readDataMgr.getCache().getOldId(node.getSUID());
		
		// Direct visual properties
		Map<String, String> atts = readDataMgr.getViewGraphicsAttributes(oldId, false);
		this.settingLockedVisualProperties = false;
		setVisualProperties(netView, nodeView, atts);
		
		// Locked visual properties
		atts = readDataMgr.getViewGraphicsAttributes(oldId, true);
		this.settingLockedVisualProperties = true;
		setVisualProperties(netView, nodeView, atts);
	}

	@Override
	protected void setEdgeViewProperties(final CyNetworkView netView, final View<CyEdge> edgeView) {
		final CyEdge edge = edgeView.getModel();
		
		// When parsing view-format XGMML, the manager does not have the network model
		// to create a map by CyEdge objects, so the graphics mapping is indexed by the old element id.
		final Object oldId = readDataMgr.getCache().getOldId(edge.getSUID());
		
		// Direct visual properties
		Map<String, String> atts = readDataMgr.getViewGraphicsAttributes(oldId, false);
		this.settingLockedVisualProperties = false;
		setVisualProperties(netView, edgeView, atts);
		
		// Locked visual properties
		atts = readDataMgr.getViewGraphicsAttributes(oldId, true);
		this.settingLockedVisualProperties = true;
		setVisualProperties(netView, edgeView, atts);
	}

	@Override
	protected boolean isLockedVisualProperty(final CyIdentifiable element, String attName) {
		return this.settingLockedVisualProperties;
	}
}
