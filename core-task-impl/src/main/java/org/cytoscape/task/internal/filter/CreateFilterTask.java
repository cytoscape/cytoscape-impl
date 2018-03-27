package org.cytoscape.task.internal.filter;

import org.cytoscape.filter.TransformerContainer;
import org.cytoscape.filter.model.NamedTransformer;
import org.cytoscape.io.read.CyTransformerReader;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;
import org.cytoscape.work.Tunable;

public class CreateFilterTask extends AbstractTask {

	@Tunable
	public String name;
	
	@ContainsTunables
	public TransformerJsonTunable jsonTunable = new TransformerJsonTunable();
	
	@ContainsTunables
	public ContainerTunable containerTunable = new ContainerTunable();
	
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public CreateFilterTask(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		if(name == null || name.isEmpty()) {
			taskMonitor.showMessage(Level.ERROR, "name is missing");
			return;
		}
		
		CyTransformerReader transformerReader = serviceRegistrar.getService(CyTransformerReader.class);
		
		NamedTransformer<CyNetwork,CyIdentifiable> transformer = jsonTunable.getTransformer(name, transformerReader);
		if(transformer == null) {
			taskMonitor.showMessage(Level.ERROR, "Error parsing JSON");
			return;
		}
		
		boolean valid = TransformerJsonTunable.validate(transformer, taskMonitor);
		if(!valid) {
			return;
		}
		
		TransformerContainer<CyNetwork,CyIdentifiable> container = containerTunable.getContainer(serviceRegistrar);
		if(container == null) {
			taskMonitor.showMessage(Level.ERROR, "container type not found: '" + containerTunable.getValue() + "'");
			return;
		}
			
		container.addNamedTransformer(transformer);
	}

}
