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
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.psi_mi.internal.data_mapper.PSIMI25EntryMapper;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.model.EntrySet;

public class PSIMI25XMLNetworkViewReader extends AbstractCyNetworkReader {
	
	private static final Logger logger = LoggerFactory.getLogger(PSIMI25XMLNetworkViewReader.class);
	
	private CyLayoutAlgorithmManager layouts;
	private TaskMonitor parentTaskMonitor;
	private PSIMI25EntryMapper mapper;
	
	public PSIMI25XMLNetworkViewReader(
			final InputStream inputStream,
			final CyApplicationManager applicationManager,
			final CyNetworkFactory networkFactory,
			final CyNetworkViewFactory networkViewFactory, // TODO Remove? Does it work now if this factory is "forced"?
			final CyLayoutAlgorithmManager layouts,
			final CyNetworkManager networkManager,
			final CyRootNetworkManager rootNetworkManager
	) {
		super(inputStream, applicationManager, networkFactory, networkManager, rootNetworkManager);
		this.layouts = layouts;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		parentTaskMonitor = taskMonitor;
		final long start = System.currentTimeMillis();
		
		taskMonitor.setProgress(0.01);
		taskMonitor.setTitle("Loading PSI-MI 2.5.x XML File ");
		taskMonitor.setStatusMessage("Loading data file in PSI-MI 2.5 XML format.");

		final PsimiXmlReader reader = new PsimiXmlReader();
		final EntrySet result = reader.read(inputStream);		
		taskMonitor.setProgress(0.4);
		taskMonitor.setStatusMessage("Data Loaded.  Mapping Data to Network...");

		if (cancelled) {
			inputStream.close();
			return;
		}
		
		CyRootNetwork root = getRootNetwork();
		final CySubNetwork newNetwork;
		
		if (root != null)
			newNetwork = root.addSubNetwork();
		else // Need to create new network with new root.
			newNetwork = (CySubNetwork) cyNetworkFactory.createNetwork();
		
		mapper = new PSIMI25EntryMapper(newNetwork, result);
		mapper.map();
		
		networks = new CyNetwork[] { newNetwork };
		
		taskMonitor.setProgress(1.0d);
		logger.info("PSI-MI XML Data Import finihsed in " + (System.currentTimeMillis() - start) + " msec.");
	}

	@Override
	public CyNetworkView buildCyNetworkView(final CyNetwork network) {
		final CyNetworkView view = getNetworkViewFactory().createNetworkView(network);
		final CyLayoutAlgorithm layout = layouts.getDefaultLayout();
		TaskIterator itr = layout.createTaskIterator(view, layout.getDefaultLayoutContext(),
				CyLayoutAlgorithm.ALL_NODE_VIEWS, "");
		Task nextTask = itr.next();
		
		try {
			nextTask.run(parentTaskMonitor);
		} catch (Exception e) {
			throw new RuntimeException("Could not finish layout", e);
		}

		parentTaskMonitor.setProgress(1.0d);
		
		return view;		
	}
	
	@Override
	public void cancel() {
		super.cancel();
		mapper.cancel();
	}
}
