package org.cytoscape.io.internal.read.sif;

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
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskIterator;

public class SIFNetworkReaderFactory extends AbstractNetworkReaderFactory {

	private final CyLayoutAlgorithmManager layouts;

	private final CyNetworkManager cyNetworkManager;;
	private final CyRootNetworkManager cyRootNetworkManager;
	
	public SIFNetworkReaderFactory(CyFileFilter filter, CyLayoutAlgorithmManager layouts,
			CyNetworkViewFactory cyNetworkViewFactory, CyNetworkFactory cyNetworkFactory,
			final CyNetworkManager cyNetworkManager, CyRootNetworkManager cyRootNetworkManager,
			CyApplicationManager cyApplicationManager) {
		super(filter, cyNetworkViewFactory, cyNetworkFactory);
		this.layouts = layouts;
		this.cyNetworkManager = cyNetworkManager;
		this.cyRootNetworkManager = cyRootNetworkManager;
	}

	public TaskIterator createTaskIterator(InputStream inputStream, String inputName) {
		return new TaskIterator(new SIFNetworkReader(inputStream, layouts, cyNetworkViewFactory, cyNetworkFactory,
				this.cyNetworkManager, this.cyRootNetworkManager));
	}
}
