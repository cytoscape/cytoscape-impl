package org.cytoscape.task.internal.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.cytoscape.filter.TransformerContainer;
import org.cytoscape.filter.model.NamedTransformer;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;
import org.cytoscape.work.json.JSONResult;

import com.google.gson.Gson;

public class ListFiltersTask extends AbstractTask implements ObservableTask {

	@ContainsTunables
	public ContainerTunable containerTunable = new ContainerTunable();
	
	private List<String> filterNames;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public ListFiltersTask(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		TransformerContainer<CyNetwork,CyIdentifiable> container = containerTunable.getContainer(serviceRegistrar);
		if(container == null) {
			taskMonitor.showMessage(Level.ERROR, "container type not found: '" + containerTunable.getValue() + "'");
			return;
		}
		
		filterNames = container.getNamedTransformers().stream().map(NamedTransformer::getName).collect(Collectors.toList()); 
	}


	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(String.class.equals(type)) {
			return type.cast(filterNames.stream().collect(Collectors.joining(",")));
		} else if(List.class.equals(type) || Collection.class.equals(type)) {
			return type.cast(filterNames);
		} else if(JSONResult.class.equals(type)) {
			JSONResult res = () -> new Gson().toJson(filterNames);
			return type.cast(res);
		}
		return null;
	}

	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(String.class, List.class, Collection.class, JSONResult.class);
	}
	
}
