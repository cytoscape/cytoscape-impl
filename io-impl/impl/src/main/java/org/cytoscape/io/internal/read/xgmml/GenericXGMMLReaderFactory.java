package org.cytoscape.io.internal.read.xgmml;

import java.io.InputStream;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.read.AbstractNetworkReaderFactory;
import org.cytoscape.io.internal.read.xgmml.handler.ReadDataManager;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskIterator;

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
 * This factory creates readers which can handle standard XGMML files.
 */
public class GenericXGMMLReaderFactory extends AbstractNetworkReaderFactory {

	private final XGMMLParser parser;
	private final ReadDataManager readDataMgr;
	private final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr;
	
	public GenericXGMMLReaderFactory(final CyFileFilter filter,
									 final ReadDataManager readDataMgr,
									 final XGMMLParser parser,
									 final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr,
									 final CyServiceRegistrar serviceRegistrar) {
		super(filter, serviceRegistrar);
		this.readDataMgr = readDataMgr;
		this.parser = parser;
		this.unrecognizedVisualPropertyMgr = unrecognizedVisualPropertyMgr;
	}

	@Override
	public TaskIterator createTaskIterator(InputStream inputStream, String inputName) {
		final CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
		final CyNetworkFactory netFactory = serviceRegistrar.getService(CyNetworkFactory.class);
		final CyNetworkManager netManager = serviceRegistrar.getService(CyNetworkManager.class);
		final CyRootNetworkManager rootNetManager = serviceRegistrar.getService(CyRootNetworkManager.class);
		
		return new TaskIterator(new GenericXGMMLReader(inputStream, readDataMgr, parser, unrecognizedVisualPropertyMgr,
				applicationManager, netFactory, netManager, rootNetManager, serviceRegistrar));
	}
}
