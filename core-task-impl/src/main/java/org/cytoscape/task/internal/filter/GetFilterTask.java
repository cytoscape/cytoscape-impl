package org.cytoscape.task.internal.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.cytoscape.filter.TransformerContainer;
import org.cytoscape.filter.model.NamedTransformer;
import org.cytoscape.io.write.CyTransformerWriter;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskMonitor.Level;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

public class GetFilterTask extends AbstractTask implements ObservableTask {
 
	@Tunable
	public String name = null;
	
	@ContainsTunables
	public ContainerTunable containerTunable = new ContainerTunable();
	
	private String jsonResult = null;
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public GetFilterTask(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) throws IOException {
		if(name == null) {
			taskMonitor.showMessage(Level.ERROR, "name must be provided");
			return;
		}
		
		TransformerContainer<CyNetwork,CyIdentifiable> container = containerTunable.getContainer(serviceRegistrar);
		if(container == null) {
			taskMonitor.showMessage(Level.ERROR, "container type not found: '" + containerTunable.getValue() + "'");
			return;
		}
		
		NamedTransformer<CyNetwork, CyIdentifiable> namedTransformer = container.getNamedTransformer(name);
		if(namedTransformer == null) {
			taskMonitor.showMessage(Level.ERROR, "transformer not found: '" + name + "'");
			return;
		}
		
		CyTransformerWriter transformerWriter = serviceRegistrar.getService(CyTransformerWriter.class);
		
		try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			transformerWriter.write(out, namedTransformer);
			jsonResult = out.toString();
		}
	}


	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(String.class.equals(type)) {
			return type.cast(jsonResult);
		} else if(JSONResult.class.equals(type)) {
			JSONResult res = () -> jsonResult;
			return type.cast(res);
		}
		return null;
	}
	
	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(String.class, JSONResult.class);
	}

}
