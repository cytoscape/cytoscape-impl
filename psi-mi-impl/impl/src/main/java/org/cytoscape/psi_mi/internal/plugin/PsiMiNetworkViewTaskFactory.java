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

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.psi_mi.internal.plugin.PsiMiCyFileFilter.PSIMIVersion;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskIterator;

public class PsiMiNetworkViewTaskFactory extends AbstractInputStreamTaskFactory {
	
	private final CyApplicationManager cyApplicationManager;
	private final CyNetworkViewFactory networkViewFactory;
	private final CyNetworkFactory networkFactory;
	private final CyLayoutAlgorithmManager layouts;
	private final CyNetworkManager cyNetworkManager;
	private final CyRootNetworkManager cyRootNetworkManager;
	
	private final PSIMIVersion version;

	public PsiMiNetworkViewTaskFactory(
			final PSIMIVersion version,
			final CyFileFilter filter,
			final CyApplicationManager cyApplicationManager,
			final CyNetworkFactory networkFactory, 
			final CyNetworkViewFactory networkViewFactory,
			final CyLayoutAlgorithmManager layouts,
			final CyNetworkManager cyNetworkManager,
			final CyRootNetworkManager cyRootNetworkManager
		) {
		super(filter);
		this.cyApplicationManager = cyApplicationManager;
		this.networkFactory = networkFactory;
		this.networkViewFactory = networkViewFactory;
		this.layouts = layouts;
		this.version = version;
		this.cyNetworkManager= cyNetworkManager;
		this.cyRootNetworkManager = cyRootNetworkManager;
	}
	
	@Override
	public TaskIterator createTaskIterator(InputStream inputStream, String inputName) {
		// Usually 3 tasks: load, visualize, and layout.
		
		if(version == PSIMIVersion.PSIMI25)
			return new TaskIterator(3, new PSIMI25XMLNetworkViewReader(inputStream, cyApplicationManager, networkFactory, networkViewFactory, layouts, cyNetworkManager, cyRootNetworkManager));
		else
			return new TaskIterator(3, new PSIMI10XMLNetworkViewReader(inputStream, cyApplicationManager, networkFactory, networkViewFactory, layouts, cyNetworkManager, cyRootNetworkManager));
	}
}
