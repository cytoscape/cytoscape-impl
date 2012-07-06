package org.cytoscape.task.internal.vizmap;

import java.util.Collection;

import org.cytoscape.task.AbstractNetworkViewCollectionTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

public class ClearEdgeBendTaskFactory extends AbstractNetworkViewCollectionTaskFactory {

	@Override
	public TaskIterator createTaskIterator(Collection<CyNetworkView> networkViews) {
		return new TaskIterator(new ClearEdgeBendTask(networkViews));
	}

}
