package org.cytoscape.tableimport.internal.task;

import static org.cytoscape.work.TunableValidator.ValidationState.OK;

import java.io.InputStream;
import java.util.Collections;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.tableimport.internal.util.ImportType;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.util.ListSingleSelection;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

public class CombineReaderAndMappingTask extends AbstractCyNetworkReader implements TunableValidator {

	@ProvidesTitle
	public String getTitle() {
		return ImportType.NETWORK_IMPORT.getTitle();
	}	

	@ContainsTunables
	public ImportNetworkTableReaderTask importTask;
	
	private final NetworkCollectionHelper collectionHelper;
	
	private TaskMonitor taskMonitor;
	private final CyServiceRegistrar serviceRegistrar;

	public CombineReaderAndMappingTask(final InputStream is, final String fileType, final String inputName,
			final CyServiceRegistrar serviceRegistrar) {
		super(
				is,
				serviceRegistrar.getService(CyApplicationManager.class),
				serviceRegistrar.getService(CyNetworkFactory.class),
				serviceRegistrar.getService(CyNetworkManager.class),
				serviceRegistrar.getService(CyRootNetworkManager.class)
		);
		this.serviceRegistrar = serviceRegistrar;
		importTask = new ImportNetworkTableReaderTask(is, fileType, inputName, serviceRegistrar);
		collectionHelper = new NetworkCollectionHelper(serviceRegistrar);
	}
	
	@Override
	public ValidationState getValidationState(Appendable errMsg) {
		if (importTask instanceof TunableValidator) {
			ValidationState readVS = ((TunableValidator) importTask).getValidationState(errMsg);

			if (readVS != OK)
				return readVS;
		}
		
		return OK;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;
		
		// Pass the tunable values from this task to the helper task
		final String rootNetName = getRootNetworkList().getSelectedValue();
		final String targetColumn = getTargetColumnList().getSelectedValue();
		final NetworkViewRenderer renderer = getNetworkViewRendererList().getSelectedValue();
		
		if (rootNetName != null) {
			ListSingleSelection<String> ls = new ListSingleSelection<>(Collections.singletonList(rootNetName));
			ls.setSelectedValue(rootNetName);
			collectionHelper.setRootNetworkList(ls);
		} else {
			collectionHelper.setRootNetworkList(new ListSingleSelection<>(Collections.emptyList()));
			collectionHelper.setTargetColumnList(new ListSingleSelection<>(Collections.emptyList()));
		}
		
		if (targetColumn != null) {
			ListSingleSelection<String> ls = new ListSingleSelection<>(Collections.singletonList(targetColumn));
			ls.setSelectedValue(targetColumn);
			collectionHelper.setTargetColumnList(ls);
		}
		
		if (renderer != null) {
			ListSingleSelection<NetworkViewRenderer> ls =
					new ListSingleSelection<>(Collections.singletonList(renderer));
			ls.setSelectedValue(renderer);
			collectionHelper.setNetworkViewRendererList(ls);
		}
		
		// Run the helper task
		collectionHelper.run(taskMonitor);
		
		// Run the Import task
		importTask.setNodeMap(collectionHelper.getNodeMap());
		importTask.setRootNetwork(collectionHelper.getRootNetwork());
		importTask.setNetworkViewFactory(collectionHelper.getNetworkViewFactory());
		importTask.run(taskMonitor);
	}
	
	@Override
	public CyNetworkView buildCyNetworkView(final CyNetwork network) {
		final CyNetworkView view = collectionHelper.getNetworkViewFactory().createNetworkView(network);
		final CyLayoutAlgorithm layout = serviceRegistrar.getService(CyLayoutAlgorithmManager.class).getDefaultLayout();
		TaskIterator itr = layout.createTaskIterator(view, layout.getDefaultLayoutContext(),
				CyLayoutAlgorithm.ALL_NODE_VIEWS, "");
		Task nextTask = itr.next();
		
		try {
			nextTask.run(taskMonitor);
		} catch (Exception e) {
			throw new RuntimeException("Could not finish layout", e);
		}

		taskMonitor.setProgress(1.0);
		
		return view;	
	}

	@Override
	public CyNetwork[] getNetworks() {
		return importTask.getNetworks();
	}
}
