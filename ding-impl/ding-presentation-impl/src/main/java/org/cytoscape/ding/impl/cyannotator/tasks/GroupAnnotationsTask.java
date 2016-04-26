package org.cytoscape.ding.impl.cyannotator.tasks;

import java.util.ArrayList;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.annotations.DingAnnotation;
import org.cytoscape.ding.impl.cyannotator.annotations.GroupAnnotationImpl;
import org.cytoscape.task.AbstractNetworkViewTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;

public class GroupAnnotationsTask extends AbstractNetworkViewTask {

	public GroupAnnotationsTask(CyNetworkView view) {
		super(view);
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		if (view instanceof DGraphView) {
			DGraphView dView = (DGraphView) view;
			CyAnnotator cyAnnotator = dView.getCyAnnotator();
			GroupAnnotationImpl group = new GroupAnnotationImpl(cyAnnotator, dView, null);
			group.addComponent(null); // Need to add this first so we can update
										// things appropriately
			cyAnnotator.addAnnotation(group);

			// Now, add all of the children
			for (DingAnnotation child : new ArrayList<>(cyAnnotator.getSelectedAnnotations())) {
				group.addMember(child);
				child.setSelected(false);
			}

			// Finally, set ourselves to be the selected component
			group.setSelected(true);
			group.update();
		}
	}
}
