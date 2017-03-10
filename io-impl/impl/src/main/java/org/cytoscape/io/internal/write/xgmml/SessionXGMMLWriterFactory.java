package org.cytoscape.io.internal.write.xgmml;

import java.io.OutputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;

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

public class SessionXGMMLWriterFactory extends GenericXGMMLWriterFactory {

	public SessionXGMMLWriterFactory(final CyFileFilter filter,
									 final UnrecognizedVisualPropertyManager unrecognizedVisualPropMgr,
									 final CyServiceRegistrar serviceRegistrar) {
		super(filter, unrecognizedVisualPropMgr, null, serviceRegistrar);
	}

	@Override
	public CyWriter createWriter(OutputStream os, CyNetworkView view) {
		return new SessionXGMMLNetworkViewWriter(os, view, unrecognizedVisualPropMgr, serviceRegistrar);
	}

	@Override
	public CyWriter createWriter(OutputStream os, CyNetwork network) {
		return new SessionXGMMLNetworkWriter(os, network, unrecognizedVisualPropMgr, serviceRegistrar);
	}
}
