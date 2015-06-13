package org.cytoscape.tableimport.internal;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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

import static org.cytoscape.work.TunableValidator.ValidationState.OK;

import java.io.InputStream;

import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.tableimport.internal.ui.ImportType;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TunableValidator;

public class CombineReaderAndMappingTask extends AbstractTask implements CyNetworkReader, TunableValidator {

	private TaskMonitor taskMonitor;
	private final CyServiceRegistrar serviceRegistrar;
	
	@ProvidesTitle
	public String getTitle() {
		return ImportType.NETWORK_IMPORT.getTitle();
	}	

	@ContainsTunables
	public NetworkCollectionHelper networkCollectionHelperTask;
	
	@ContainsTunables
	public ImportNetworkTableReaderTask importTask;

	public CombineReaderAndMappingTask(final InputStream is, final String fileType, final String inputName,
			final CyServiceRegistrar serviceRegistrar) {
		this.importTask = new ImportNetworkTableReaderTask(is, fileType, inputName, serviceRegistrar);
		this.networkCollectionHelperTask = new NetworkCollectionHelper(serviceRegistrar);
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public ValidationState getValidationState(Appendable errMsg) {
		if (importTask instanceof TunableValidator) {
			ValidationState readVS = ((TunableValidator)importTask).getValidationState(errMsg);

			if (readVS != OK)
				return readVS;
		}
		
		return OK;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;
		this.networkCollectionHelperTask.run(taskMonitor);
		this.importTask.setNodeMap(networkCollectionHelperTask.getNodeMap());
		this.importTask.setRootNetwork(networkCollectionHelperTask.getRootNetwork());
		this.importTask.setNetworkViewFactory(networkCollectionHelperTask.getNetworkViewFactory());
		this.importTask.run(taskMonitor);
	}
	
	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork network) {
		final CyNetworkView view = networkCollectionHelperTask.getNetworkViewFactory().createNetworkView(network);
		final CyLayoutAlgorithm layout = serviceRegistrar.getService(CyLayoutAlgorithmManager.class).getDefaultLayout();
		TaskIterator itr = layout.createTaskIterator(view, layout.getDefaultLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS,"");
		Task nextTask = itr.next();
		
		try {
			nextTask.run(taskMonitor);
		} catch (Exception e) {
			throw new RuntimeException("Could not finish layout", e);
		}

		taskMonitor.setProgress(1.0d);
		return view;	
	}

	@Override
	public CyNetwork[] getNetworks() {
		return this.importTask.getNetworks();
	}
}
