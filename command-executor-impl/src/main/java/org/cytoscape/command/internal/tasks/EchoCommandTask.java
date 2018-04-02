package org.cytoscape.command.internal.tasks;

import java.util.Arrays;
import java.util.List;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

public class EchoCommandTask extends AbstractTask implements ObservableTask {

	@Tunable
	public String message = "";

	@Override
	public void run(TaskMonitor taskMonitor) {
	}

	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(JSONResult.class.equals(type)) {
			return type.cast((JSONResult)() -> "[ \"" + message + "\" ]");
		}
		if(String.class.equals(type)) {
			return type.cast(message);
		}
		return null;
	}

	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(JSONResult.class, String.class);
	}

}
