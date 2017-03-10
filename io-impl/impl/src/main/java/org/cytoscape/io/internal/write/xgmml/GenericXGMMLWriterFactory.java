package org.cytoscape.io.internal.write.xgmml;

import java.io.OutputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.util.GroupUtil;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.io.internal.write.AbstractCyWriterFactory;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
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

public class GenericXGMMLWriterFactory extends AbstractCyWriterFactory implements CyNetworkViewWriterFactory {

	protected final UnrecognizedVisualPropertyManager unrecognizedVisualPropMgr;
	protected final GroupUtil groupUtil;
	protected final CyServiceRegistrar serviceRegistrar;

	public GenericXGMMLWriterFactory(final CyFileFilter filter,
									 final UnrecognizedVisualPropertyManager unrecognizedVisualPropMgr,
									 final GroupUtil groupUtil,
									 final CyServiceRegistrar serviceRegistrar) {
		super(filter);
		this.unrecognizedVisualPropMgr = unrecognizedVisualPropMgr;
		this.groupUtil = groupUtil;
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
    public CyWriter createWriter(OutputStream os, CyNetworkView view) {
		return new GenericXGMMLWriter(os, view, unrecognizedVisualPropMgr, groupUtil, serviceRegistrar);
    }

	@Override
    public CyWriter createWriter(OutputStream os, CyNetwork network) {
		return new GenericXGMMLWriter(os, network, unrecognizedVisualPropMgr, groupUtil, serviceRegistrar);
    }
}
