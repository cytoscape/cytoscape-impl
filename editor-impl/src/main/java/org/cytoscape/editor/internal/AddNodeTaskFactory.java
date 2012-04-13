package org.cytoscape.editor.internal;

//import java.awt.datatransfer.Transferable;
import java.awt.geom.Point2D;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.task.AbstractNetworkViewLocationTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;

public class AddNodeTaskFactory extends AbstractNetworkViewLocationTaskFactory{

	private final CyEventHelper eh;
	private final VisualMappingManager vmm;
	
	public AddNodeTaskFactory(CyEventHelper eh, VisualMappingManager vmm) {
		this.eh = eh;
		this.vmm = vmm;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView,
			Point2D javaPt, Point2D xformPt) {
		// TODO Auto-generated method stub
		return new TaskIterator(new AddNodeTask(vmm, networkView, xformPt, eh));
	}
}
