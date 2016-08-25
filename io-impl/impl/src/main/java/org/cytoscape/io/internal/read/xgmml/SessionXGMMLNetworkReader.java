package org.cytoscape.io.internal.read.xgmml;

import java.io.InputStream;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.internal.read.xgmml.handler.ReadDataManager;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskMonitor;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

/**
 * This reader handles XGMML files from Cy2 sessions and CyNetwork serialization files from Cy3 sessions.
 */
public class SessionXGMMLNetworkReader extends GenericXGMMLReader {
	
	protected static final String CY_NAMESPACE = "http://www.cytoscape.org";

	private CyRootNetwork parent;
	
	public SessionXGMMLNetworkReader(final InputStream inputStream,
									 final ReadDataManager readDataMgr,
									 final XGMMLParser parser,
									 final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr,
									 final CyServiceRegistrar serviceRegistrar) {
		super(
				inputStream,
				readDataMgr,
				parser,
				unrecognizedVisualPropertyMgr,
				serviceRegistrar.getService(CyApplicationManager.class),
				serviceRegistrar.getService(CyNetworkFactory.class),
				null, // The CyNetworkManager has to be null when reading XGMML from session files!
				serviceRegistrar.getService(CyRootNetworkManager.class),
				serviceRegistrar
		);
	}

	public void setParent(final CyNetwork n) {
		this.parent = n != null ? serviceRegistrar.getService(CyRootNetworkManager.class).getRootNetwork(n) : null;
	}
	
	@Override
	protected void init(final TaskMonitor tm) {
		super.init(tm);
		readDataMgr.setViewFormat(false);
		readDataMgr.setParentNetwork(parent);
	}
	
	@Override
	protected void setNetworkViewProperties(final CyNetworkView netView) {
		// Only for Cytocape 2.x files. In Cy3 the visual properties are saved in the view xgmml file.
		if (readDataMgr.getDocumentVersion() < 3.0)
			super.setNetworkViewProperties(netView);
	}

	@Override
	protected void setNodeViewProperties(final CyNetworkView netView, final View<CyNode> nodeView) {
		if (readDataMgr.getDocumentVersion() < 3.0)
			super.setNodeViewProperties(netView, nodeView);
	}

	@Override
	protected void setEdgeViewProperties(final CyNetworkView netView, final View<CyEdge> edgeView) {
		if (readDataMgr.getDocumentVersion() < 3.0)
			super.setEdgeViewProperties(netView, edgeView);
	}
}
