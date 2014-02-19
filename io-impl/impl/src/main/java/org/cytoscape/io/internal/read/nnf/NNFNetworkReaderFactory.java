package org.cytoscape.io.internal.read.nnf;

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

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.read.AbstractNetworkReaderFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskIterator;

public class NNFNetworkReaderFactory extends AbstractNetworkReaderFactory {

	private final CyLayoutAlgorithmManager layouts;
	private final CyNetworkManager cyNetworkManagerServiceRef;
	private final CyRootNetworkManager cyRootNetworkFactory;

	public NNFNetworkReaderFactory(CyFileFilter filter, CyLayoutAlgorithmManager layouts,
			CyNetworkViewFactory cyNetworkViewFactory, CyNetworkFactory cyNetworkFactory,
			 CyNetworkManager cyNetworkManagerServiceRef,CyRootNetworkManager cyRootNetworkFactory) {
		super(filter, cyNetworkViewFactory, cyNetworkFactory);
		this.layouts = layouts;
		this.cyNetworkManagerServiceRef = cyNetworkManagerServiceRef;
		this.cyRootNetworkFactory = cyRootNetworkFactory;
	}

	public TaskIterator createTaskIterator(InputStream inputStream, String inputName) {
		return new TaskIterator(new NNFNetworkReader(inputStream, layouts, cyNetworkViewFactory, cyNetworkFactory,
				 cyNetworkManagerServiceRef, cyRootNetworkFactory));
	}
}
