package org.cytoscape.psi_mi.internal.plugin;

/*
 * #%L
 * Cytoscape PSI-MI Impl (psi-mi-impl)
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
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskIterator;

public class PsiMiTabReaderFactory extends AbstractInputStreamTaskFactory {

	private final CyApplicationManager cyApplicationManager;
	private final CyNetworkViewFactory cyNetworkViewFactory;
	private final CyNetworkFactory cyNetworkFactory;
	private final CyLayoutAlgorithmManager layouts;
	private final CyNetworkManager cyNetworkManager;
	private final CyRootNetworkManager cyRootNetworkManager;
	
	private final CyProperty<Properties> prop;
	
	public PsiMiTabReaderFactory(
			final CyFileFilter filter,
			final CyApplicationManager cyApplicationManager,
			final CyNetworkViewFactory cyNetworkViewFactory,
			final CyNetworkFactory cyNetworkFactory,
			final CyLayoutAlgorithmManager layouts,
			final CyProperty<Properties> prop,
			final CyNetworkManager cyNetworkManager,
			final CyRootNetworkManager cyRootNetworkManager
		) {
		super(filter);
		this.cyApplicationManager = cyApplicationManager;
		this.cyNetworkFactory = cyNetworkFactory;
		this.cyNetworkViewFactory = cyNetworkViewFactory;
		this.layouts = layouts;
		this.prop = prop;
		this.cyNetworkManager = cyNetworkManager;
		this.cyRootNetworkManager = cyRootNetworkManager;
	}

	@Override
	public TaskIterator createTaskIterator(InputStream inputStream, String inputName) {
		return new TaskIterator(new PsiMiTabReader(inputStream, cyApplicationManager,
				cyNetworkViewFactory, cyNetworkFactory, layouts, prop, cyNetworkManager, cyRootNetworkManager));
	}
}
