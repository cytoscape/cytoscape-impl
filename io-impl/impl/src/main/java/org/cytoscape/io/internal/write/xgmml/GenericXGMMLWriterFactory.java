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
import org.cytoscape.io.internal.util.GroupUtil;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.io.internal.write.AbstractCyWriterFactory;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;

public class GenericXGMMLWriterFactory extends AbstractCyWriterFactory implements CyNetworkViewWriterFactory {

	protected final RenderingEngineManager renderingEngineMgr;
	protected final UnrecognizedVisualPropertyManager unrecognizedVisualPropMgr;
	protected final CyNetworkManager netMgr;
	protected final CyRootNetworkManager rootNetMgr;
	protected final VisualMappingManager vmMgr;
	protected final GroupUtil groupUtil;

	public GenericXGMMLWriterFactory(final CyFileFilter filter,
									 final RenderingEngineManager renderingEngineMgr,
									 final UnrecognizedVisualPropertyManager unrecognizedVisualPropMgr,
									 final CyNetworkManager netMgr,
									 final CyRootNetworkManager rootNetMgr,
									 final VisualMappingManager vmMgr,
									 final GroupUtil groupUtil) {
		super(filter);
		this.renderingEngineMgr = renderingEngineMgr;
		this.unrecognizedVisualPropMgr = unrecognizedVisualPropMgr;
		this.netMgr = netMgr;
		this.rootNetMgr = rootNetMgr;
		this.vmMgr = vmMgr;
		this.groupUtil = groupUtil;
	}

	@Override
    public CyWriter createWriter(OutputStream os, CyNetworkView view) {
		return new GenericXGMMLWriter(os, renderingEngineMgr, view, unrecognizedVisualPropMgr, netMgr, rootNetMgr,
				vmMgr, groupUtil);
    }

	@Override
    public CyWriter createWriter(OutputStream os, CyNetwork network) {
		return new GenericXGMMLWriter(os, renderingEngineMgr, network, unrecognizedVisualPropMgr, netMgr, rootNetMgr,
				groupUtil);
    }
}
