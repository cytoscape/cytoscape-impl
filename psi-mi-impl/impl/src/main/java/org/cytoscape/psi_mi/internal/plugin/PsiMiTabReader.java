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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.property.CyProperty;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class PsiMiTabReader extends AbstractCyNetworkReader {
	
	private final CyLayoutAlgorithmManager layouts;
	private final PsiMiTabParser parser;
	private TaskMonitor parentTaskMonitor;
	private final CyProperty<Properties> prop;
	
	public PsiMiTabReader(
			final InputStream is,
			final CyApplicationManager applicationManager,
			final CyNetworkViewFactory networkViewFactory,
			final CyNetworkFactory networkFactory,
			final CyLayoutAlgorithmManager layouts,
			final CyProperty<Properties> prop, 
			final CyNetworkManager networkManager,
			final CyRootNetworkManager rootNetworkManager
	) {
		super(is, applicationManager, networkFactory, networkManager, rootNetworkManager);
		this.layouts = layouts;
		this.prop = prop;
		parser = new PsiMiTabParser(is);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.parentTaskMonitor = taskMonitor;
		
		CyRootNetwork rootNetwork = getRootNetwork();
		final CySubNetwork newNetwork;
		
		if (rootNetwork != null)
			newNetwork = rootNetwork.addSubNetwork();
		else // Need to create new network with new root.
			newNetwork = (CySubNetwork) cyNetworkFactory.createNetwork();
		
		parser.setNodeMap(getNodeMap());
		
		try {
			taskMonitor.setTitle("Loading PSIMI-TAB File");
			taskMonitor.setStatusMessage("Loading PSI-MI-TAB25 file.");
			taskMonitor.setProgress(0.01);

			parser.parse(newNetwork, taskMonitor);
			networks = new CyNetwork[] { newNetwork };

			taskMonitor.setProgress(1.0);
		} finally {
			if (inputStream != null) {
				inputStream.close();
				inputStream = null;
			}
		}
	}

	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork network) {
		if (cancelled) {
			if(network != null) {
				network.dispose();
				network = null;
			}
			throw new RuntimeException("Network loading canceled by user.");
		}
		
		final CyNetworkView view = getNetworkViewFactory().createNetworkView(network);

		String pref = CyLayoutAlgorithmManager.DEFAULT_LAYOUT_NAME;
		if (prop != null)
			pref = prop.getProperties().getProperty("preferredLayoutAlgorithm", pref);

		final CyLayoutAlgorithm layout = layouts.getLayout(pref);
		// Force to run this task here to avoid concurrency problem.
		TaskIterator itr = layout.createTaskIterator(view, layout.getDefaultLayoutContext(),
				CyLayoutAlgorithm.ALL_NODE_VIEWS, "");
		Task nextTask = itr.next();
		
		try {
			nextTask.run(parentTaskMonitor);
		} catch (Exception e) {
			throw new RuntimeException("Could not finish layout", e);
		}

		return view;
	}

	@Override
	public void cancel() {
		super.cancel();
		parser.cancel();
	}
}
