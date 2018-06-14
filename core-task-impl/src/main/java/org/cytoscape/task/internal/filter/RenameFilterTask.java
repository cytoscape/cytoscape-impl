package org.cytoscape.task.internal.filter;

import org.cytoscape.filter.TransformerContainer;
import org.cytoscape.filter.TransformerManager;
import org.cytoscape.filter.model.NamedTransformer;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;
import org.cytoscape.work.Tunable;

public class RenameFilterTask extends AbstractTask {

	@Tunable
	public String name;
	
	@Tunable
	public String newName;
	
	@ContainsTunables
	public ContainerTunable containerTunable = new ContainerTunable();
	
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public RenameFilterTask(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if(name == null || name.isEmpty()) {
			taskMonitor.showMessage(Level.ERROR, "name is missing");
			return;
		}
		if(newName == null || newName.isEmpty()) {
			taskMonitor.showMessage(Level.ERROR, "newName is missing");
			return;
		}
		
		TransformerContainer<CyNetwork,CyIdentifiable> container = containerTunable.getContainer(serviceRegistrar);
		if(container == null) {
			taskMonitor.showMessage(Level.ERROR, "container type not found: '" + containerTunable.getValue() + "'");
			return;
		}
		
		NamedTransformer<CyNetwork,CyIdentifiable> transformer = container.getNamedTransformer(name);
		if(transformer == null) {
			taskMonitor.showMessage(Level.ERROR, "filter with name '" + name + "' not found");
			return;
		}
		
		TransformerManager transformerManager = serviceRegistrar.getService(TransformerManager.class);
		NamedTransformer<CyNetwork,CyIdentifiable> newTransformer = transformerManager.createNamedTransformer(newName, transformer.getTransformers());
		
		container.removeNamedTransformer(name);
		container.addNamedTransformer(newTransformer);
	}

}
