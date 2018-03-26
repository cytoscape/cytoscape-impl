package org.cytoscape.task.internal.filter;

import org.cytoscape.filter.TransformerContainer;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;
import org.cytoscape.work.Tunable;

public class DeleteFilterTask extends AbstractTask {

	@Tunable
	public String name;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public DeleteFilterTask(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if(name == null || name.isEmpty()) {
			taskMonitor.showMessage(Level.ERROR, "name is missing");
			return;
		}
		
		@SuppressWarnings("unchecked")
		TransformerContainer<CyNetwork,CyIdentifiable> container = serviceRegistrar.getService(TransformerContainer.class, "(panel.type=filter)");
		
		boolean removed = container.removeNamedTransformer(name);
		if(removed)
			taskMonitor.showMessage(Level.INFO, "filter '" + name + "' removed");
		else
			taskMonitor.showMessage(Level.WARN, "filter '" + name + "' not found");
	}

}
