package de.mpg.mpi_inf.bioinf.netanalyzer.task;

import java.util.Collection;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkCollectionTaskFactory;
import org.cytoscape.task.analyze.AnalyzeNetworkCollectionTaskFactory;
import org.cytoscape.work.TaskIterator;

public class AnalyzeNetworkByNetworkAnalyzerTaskFactory extends AbstractNetworkCollectionTaskFactory implements AnalyzeNetworkCollectionTaskFactory {

	@Override
	public TaskIterator createTaskIterator(Collection<CyNetwork> networks) {
		return new TaskIterator(new AnalyzeNetworkTask(networks));
	}

}
