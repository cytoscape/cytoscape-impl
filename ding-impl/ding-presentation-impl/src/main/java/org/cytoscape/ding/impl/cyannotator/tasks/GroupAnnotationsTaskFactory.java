package org.cytoscape.ding.impl.cyannotator.tasks;

import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.DingRenderer;
import org.cytoscape.ding.impl.cyannotator.AnnotationTree;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

public class GroupAnnotationsTaskFactory implements NetworkViewTaskFactory {
	
	private final DingRenderer dingRenderer;
	
	public GroupAnnotationsTaskFactory(DingRenderer dingRenderer) {
		this.dingRenderer = dingRenderer;
	}
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView) {
		DRenderingEngine re = dingRenderer.getRenderingEngine(networkView);
		if(re == null)
			return null;
		return new TaskIterator(new GroupAnnotationsTask(re));

	}

	@Override
	public boolean isReady(CyNetworkView networkView) {
		DRenderingEngine re = dingRenderer.getRenderingEngine(networkView);
		if(re == null)
			return false;
		CyAnnotator cyAnnotator = re.getCyAnnotator();
		return AnnotationTree.hasSameParent(cyAnnotator.getAnnotationSelection().getSelectedAnnotations());
	}
}
