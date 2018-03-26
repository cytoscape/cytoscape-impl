package org.cytoscape.task.internal.filter;

import java.io.IOException;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.event.CyEventHelper;
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

public class ApplyFilterTask extends AbstractTask {

	@Tunable(description="Network", context="nogui", longDescription=StringToModel.CY_NETWORK_LONG_DESCRIPTION, exampleStringValue=StringToModel.CY_NETWORK_EXAMPLE_STRING)
	public CyNetwork network = null;
	
	@ContainsTunables
	public TransformerJsonTunable json = new TransformerJsonTunable();
	
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public ApplyFilterTask(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws IOException {
		if(network == null)
			network = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork();
		
		CyTransformerReader transformerReader = serviceRegistrar.getService(CyTransformerReader.class);
		
		NamedTransformer<CyNetwork,CyIdentifiable> transformer = json.getTransformer("ApplyFilterTask", transformerReader);
		if(transformer == null) {
			taskMonitor.showMessage(Level.ERROR, "Error parsing JSON");
			return;
		}
		
		applyFilter(serviceRegistrar, network, transformer);
	}
	
	
	public static void applyFilter(CyServiceRegistrar serviceRegistrar, CyNetwork network, NamedTransformer<CyNetwork,CyIdentifiable> transformer) {
		CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		TransformerManager transformerManager = serviceRegistrar.getService(TransformerManager.class);
		
		SelectUtils selectUtils = new SelectUtils(eventHelper);
		
		// De-select all nodes and edges. 
		// Do this before running the filter because selection handlers can run in parallel with the filter.
		selectUtils.setSelectedNodes(network, network.getNodeList(), false);
		selectUtils.setSelectedEdges(network, network.getEdgeList(), false);
		
		Sink sink = new Sink();
		transformerManager.execute(network, transformer.getTransformers(), sink);
		
		selectUtils.setSelectedNodes(network, sink.getNodes(), true);
	}

}
