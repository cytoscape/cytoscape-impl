package org.cytoscape.task.internal.filter;

import org.cytoscape.application.CyApplicationManager;
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
	
	@Tunable(description="If true the filter will be applied to the current network immediately after it is created. Default is true.")
	public boolean apply = true;
	
	@ContainsTunables
	public TransformerJsonTunable jsonTunable = new TransformerJsonTunable();
	
	@ContainsTunables
	public ContainerTunable containerTunable = new ContainerTunable();
	
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public CreateFilterTask(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	public CreateFilterTask(CyServiceRegistrar serviceRegistrar, String name, boolean apply) {
		this(serviceRegistrar);
		this.name = name;
		this.apply = apply;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if(name == null || name.isEmpty()) {
			taskMonitor.showMessage(Level.ERROR, "name is missing");
			return;
		}
		
		TransformerContainer<CyNetwork,CyIdentifiable> container = containerTunable.getContainer(serviceRegistrar);
		if(container == null) {
			taskMonitor.showMessage(Level.ERROR, "container type not found: '" + containerTunable.getValue() + "'");
			return;
		}
		
		// If a filter with the name already exists overwrite it.
		// The default behaviour of addNamedTransformer() is to add a number to the end of the name.
		// That's bad for commands because subsequent commands can't refer to the filter by the expected name.
		NamedTransformer<CyNetwork,CyIdentifiable> existingTransformer = container.getNamedTransformer(name);
		if(existingTransformer != null) {
			container.removeNamedTransformer(name);
		}
		
		CyTransformerReader transformerReader = serviceRegistrar.getService(CyTransformerReader.class);
		
		NamedTransformer<CyNetwork,CyIdentifiable> transformer = jsonTunable.getTransformer(name, transformerReader);
		if(transformer == null) {
			taskMonitor.showMessage(Level.ERROR, "Error parsing Filter JSON");
			throw new Exception("Error parsing Filter JSON");
		}
		
		boolean valid = TransformerJsonTunable.validate(transformer, taskMonitor);
		if(!valid) {
			throw new Exception("Transformer is not valid: " + transformer.getName());
		}

		container.addNamedTransformer(transformer);
		
		if(apply) {
			CyNetwork network = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork();
			if(network != null) {
				int[] result = SelectFilterTask.applyFilter(serviceRegistrar, network, transformer, SelectTunable.Action.SELECT);
				
				taskMonitor.showMessage(Level.INFO, SelectFilterTask.getResultMessage(result));
			}
		}
	}

}
