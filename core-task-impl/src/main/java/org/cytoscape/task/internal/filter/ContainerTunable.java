package org.cytoscape.task.internal.filter;

import org.cytoscape.filter.TransformerContainer;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

public class ContainerTunable {

	@Tunable
	public ListSingleSelection<String> container = new ListSingleSelection<>("filter", "chain");
	
	@SuppressWarnings("unchecked")
	public TransformerContainer<CyNetwork,CyIdentifiable> getContainer(CyServiceRegistrar serviceRegistrar) {
		String containerType = container.getSelectedValue();
		if(containerType == null)
			containerType = "filter";
		
		return serviceRegistrar.getService(TransformerContainer.class, "(container.type=" + containerType + ")");
	}
	
	public String getValue() {
		return container.getSelectedValue();
	}
}
