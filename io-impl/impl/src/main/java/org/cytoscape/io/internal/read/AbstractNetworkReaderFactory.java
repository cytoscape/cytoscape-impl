package org.cytoscape.io.internal.read;

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

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;

public abstract class AbstractNetworkReaderFactory extends AbstractInputStreamTaskFactory {

	protected final CyApplicationManager cyApplicationManager;
	protected final CyNetworkFactory cyNetworkFactory;
	protected final CyNetworkManager cyNetworkManager;
	protected final CyRootNetworkManager cyRootNetworkManager;

	public AbstractNetworkReaderFactory(final CyFileFilter filter,
										final CyApplicationManager cyApplicationManager,
										final CyNetworkFactory cyNetworkFactory,
										final CyNetworkManager cyNetworkManager,
										final CyRootNetworkManager cyRootNetworkManager) {
		super(filter);
		this.cyApplicationManager = cyApplicationManager;
		this.cyNetworkFactory = cyNetworkFactory;
		this.cyNetworkManager = cyNetworkManager;
		this.cyRootNetworkManager = cyRootNetworkManager;
	}
}
