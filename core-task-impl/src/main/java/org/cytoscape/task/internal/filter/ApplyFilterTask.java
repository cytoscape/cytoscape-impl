package org.cytoscape.task.internal.filter;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.command.StringToModel;
import org.cytoscape.filter.TransformerContainer;
import org.cytoscape.filter.model.NamedTransformer;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;
import org.cytoscape.work.Tunable;

public class ApplyFilterTask extends AbstractTask {

	@Tunable(description="Network", context="nogui", longDescription=StringToModel.CY_NETWORK_LONG_DESCRIPTION, exampleStringValue=StringToModel.CY_NETWORK_EXAMPLE_STRING)
	public CyNetwork network = null;
	
	@Tunable
	public String name;
	
	@ContainsTunables
	public ContainerTunable containerTunable = new ContainerTunable();
	
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public ApplyFilterTask(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if(name == null || name.isEmpty()) {
			taskMonitor.showMessage(Level.ERROR, "name is missing");
			return;
		}

		if(network == null)
			network = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork();
		
		TransformerContainer<CyNetwork,CyIdentifiable> container = containerTunable.getContainer(serviceRegistrar);
		if(container == null) {
			taskMonitor.showMessage(Level.ERROR, "container type not found: '" + containerTunable.getValue() + "'");
			return;
		}
		
		NamedTransformer<CyNetwork,CyIdentifiable> transformer = container.getNamedTransformer(name);
		
		if(transformer == null) {
			taskMonitor.showMessage(Level.WARN, "filter '" + name + "' not found");
			return;
		}
		
		SelectFilterTask.applyFilter(serviceRegistrar, network, transformer);
	}

}
