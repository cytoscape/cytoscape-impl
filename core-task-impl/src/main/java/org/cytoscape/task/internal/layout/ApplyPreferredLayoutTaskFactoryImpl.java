package org.cytoscape.task.internal.layout;

import java.util.Collection;
import java.util.Properties;

import org.cytoscape.property.CyProperty;
import org.cytoscape.task.AbstractNetworkViewCollectionTaskFactory;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.task.visualize.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

public class ApplyPreferredLayoutTaskFactoryImpl extends AbstractNetworkViewCollectionTaskFactory implements
		ApplyPreferredLayoutTaskFactory {

	private final CyLayoutAlgorithmManager layouts;
	private final Properties props;

	public ApplyPreferredLayoutTaskFactoryImpl(final CyLayoutAlgorithmManager layouts, final CyProperty<Properties> p) {
		this.layouts = layouts;
		this.props = p.getProperties();
	}


	@Override
	public TaskIterator createTaskIterator(final Collection<CyNetworkView> networkViews) {
		return new TaskIterator(2, new ApplyPreferredLayoutTask(networkViews, layouts, props));
	}
}
