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

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.read.AbstractNetworkReaderFactory;
import org.cytoscape.io.internal.read.xgmml.handler.ReadDataManager;
import org.cytoscape.io.internal.util.UnrecognizedVisualPropertyManager;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.work.TaskIterator;

public class SessionXGMMLNetworkViewReaderFactory extends AbstractNetworkReaderFactory {

	private final RenderingEngineManager renderingEngineMgr;
	private final XGMMLParser parser;
	private final ReadDataManager readDataMgr;
	private final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr;

	private final CyNetworkManager cyNetworkManager;;
	private final CyRootNetworkManager cyRootNetworkManager;
	private final CyApplicationManager cyApplicationManager;
	
	public SessionXGMMLNetworkViewReaderFactory(final CyFileFilter filter,
												final CyNetworkViewFactory cyNetworkViewFactory,
												final CyNetworkFactory cyNetworkFactory,
												final RenderingEngineManager renderingEngineMgr,
												final ReadDataManager readDataMgr,
												final XGMMLParser parser,
												final UnrecognizedVisualPropertyManager unrecognizedVisualPropertyMgr,
												final CyNetworkManager cyNetworkManager,
												final CyRootNetworkManager cyRootNetworkManager,
												final CyApplicationManager cyApplicationManager) {
		super(filter, cyNetworkViewFactory, cyNetworkFactory);
		this.renderingEngineMgr = renderingEngineMgr;
		this.readDataMgr = readDataMgr;
		this.parser = parser;
		this.unrecognizedVisualPropertyMgr = unrecognizedVisualPropertyMgr;
		this.cyNetworkManager = cyNetworkManager;
		this.cyRootNetworkManager = cyRootNetworkManager;
		this.cyApplicationManager = cyApplicationManager;
	}

	@Override
	public TaskIterator createTaskIterator(InputStream inputStream, String inputName) {
		return new TaskIterator(new SessionXGMMLNetworkViewReader(inputStream, cyNetworkViewFactory, cyNetworkFactory,
				renderingEngineMgr, readDataMgr, parser, unrecognizedVisualPropertyMgr, cyNetworkManager, cyRootNetworkManager, this.cyApplicationManager));
	}
}