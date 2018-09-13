package org.cytoscape.ding.impl.cyannotator.tasks;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.AnnotationTree;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

public class GroupAnnotationsTaskFactory implements NetworkViewTaskFactory {
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView) {
		return new TaskIterator(new GroupAnnotationsTask(networkView));

	}

	@Override
	public boolean isReady(CyNetworkView networkView) {
		CyAnnotator cyAnnotator = ((DGraphView)networkView).getCyAnnotator();
		return AnnotationTree.hasSameParent(cyAnnotator.getAnnotationSelection().getSelectedAnnotations());
	}
}
