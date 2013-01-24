package org.cytoscape.io.internal.write.xgmml;

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

import java.io.OutputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;

public class SessionXGMMLWriterFactory extends GenericXGMMLWriterFactory {

	public SessionXGMMLWriterFactory(final CyFileFilter filter,
									 final RenderingEngineManager renderingEngineMgr,
									 final UnrecognizedVisualPropertyManager unrecognizedVisualPropMgr,
									 final CyNetworkManager netMgr,
									 final CyRootNetworkManager rootNetMgr,
									 final VisualMappingManager vmMgr) {
		super(filter, renderingEngineMgr, unrecognizedVisualPropMgr, netMgr, rootNetMgr, vmMgr, null);
	}

	@Override
	public CyWriter createWriter(OutputStream os, CyNetworkView view) {
		return new SessionXGMMLNetworkViewWriter(os, renderingEngineMgr, view, unrecognizedVisualPropMgr, netMgr,
				rootNetMgr, vmMgr);
	}

	@Override
	public CyWriter createWriter(OutputStream os, CyNetwork network) {
		return new SessionXGMMLNetworkWriter(os, renderingEngineMgr, network, unrecognizedVisualPropMgr, netMgr,
				rootNetMgr);
	}
}
