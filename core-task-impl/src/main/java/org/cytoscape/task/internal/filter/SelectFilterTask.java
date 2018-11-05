package org.cytoscape.task.internal.filter;

import java.io.IOException;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.filter.TransformerManager;
import org.cytoscape.filter.model.NamedTransformer;
import org.cytoscape.io.read.CyTransformerReader;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.internal.select.SelectUtils;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;
import org.cytoscape.work.Tunable;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2012 - 2018 The Cytoscape Consortium
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

public class SelectFilterTask extends AbstractTask {

	@Tunable(description="Network", context="nogui", longDescription=StringToModel.CY_NETWORK_LONG_DESCRIPTION, exampleStringValue=StringToModel.CY_NETWORK_EXAMPLE_STRING)
	public CyNetwork network = null;
	
	@ContainsTunables
	public TransformerJsonTunable json = new TransformerJsonTunable();
	
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public SelectFilterTask(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public void run(TaskMonitor tm) throws IOException {
		if (network == null)
			network = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork();

		CyTransformerReader transformerReader = serviceRegistrar.getService(CyTransformerReader.class);

		NamedTransformer<CyNetwork, CyIdentifiable> transformer = json.getTransformer("ApplyFilterTask",
				transformerReader);
		
		if (transformer == null) {
			tm.showMessage(Level.ERROR, "Error parsing JSON");
			return;
		}

		boolean valid = TransformerJsonTunable.validate(transformer, tm);
		
		if (!valid)
			return;

		applyFilter(serviceRegistrar, network, transformer);
	}
	
	
	public static void applyFilter(CyServiceRegistrar serviceRegistrar, CyNetwork network,
			NamedTransformer<CyNetwork, CyIdentifiable> transformer) {
		SelectUtils selectUtils = new SelectUtils(serviceRegistrar);

		// De-select all nodes and edges.
		// Do this before running the filter because selection handlers can run in
		// parallel with the filter.
		selectUtils.setSelectedNodes(network, network.getNodeList(), false);
		selectUtils.setSelectedEdges(network, network.getEdgeList(), false);

		Sink sink = new Sink();
		serviceRegistrar.getService(TransformerManager.class).execute(network, transformer.getTransformers(), sink);

		selectUtils.setSelectedNodes(network, sink.getNodes(), true);
	}
}
