package org.cytoscape.task.internal.vizmap;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.task.layout.ApplyVisualStyleTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.util.ListSingleSelection;

public class ApplyVisualStyleTaskFactoryimpl extends AbstractNetworkViewTaskFactory implements ApplyVisualStyleTaskFactory {

	private final VisualMappingManager vmm;
	
	private final TunableSetter tunableSetter; 

	public ApplyVisualStyleTaskFactoryimpl(final VisualMappingManager vmm, TunableSetter tunableSetter) {
		this.vmm = vmm;
		this.tunableSetter = tunableSetter;
	}
	
	@Override
	public TaskIterator createTaskIterator(final CyNetworkView networkView) {
		return new TaskIterator(new ApplyVisualStyleTask(networkView, vmm));
	}

	@Override
	public TaskIterator createTaskIterator(final CyNetworkView networkView, VisualStyle style) {
		final Map<String, Object> m = new HashMap<String, Object>();

		ListSingleSelection<VisualStyle> styles = new ListSingleSelection<VisualStyle>(style);
		styles.setSelectedValue(style);

		m.put("styles", styles);

		return tunableSetter.createTaskIterator(this.createTaskIterator(networkView), m);
	}

}
