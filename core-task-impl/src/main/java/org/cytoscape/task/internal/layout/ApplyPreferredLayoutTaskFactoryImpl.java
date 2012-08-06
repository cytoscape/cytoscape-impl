package org.cytoscape.task.internal.layout;

import java.util.Collection;
import java.util.Properties;
import java.util.Map;
import java.util.HashMap;

import org.cytoscape.property.CyProperty;
import org.cytoscape.task.AbstractNetworkViewCollectionTaskFactory;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.task.visualize.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.Event;

public class ApplyPreferredLayoutTaskFactoryImpl extends AbstractNetworkViewCollectionTaskFactory implements
		ApplyPreferredLayoutTaskFactory {

	private final CyLayoutAlgorithmManager layouts;
	private final Properties props;
    private final EventAdmin eventAdmin;

	public ApplyPreferredLayoutTaskFactoryImpl(final CyLayoutAlgorithmManager layouts, final CyProperty<Properties> p) {
        this(layouts, p, null);
    }

	public ApplyPreferredLayoutTaskFactoryImpl(final CyLayoutAlgorithmManager layouts, final CyProperty<Properties> p, final EventAdmin eventAdmin) {
		this.layouts = layouts;
		this.props = p.getProperties();
        this.eventAdmin = eventAdmin;
	}


	@Override
	public TaskIterator createTaskIterator(final Collection<CyNetworkView> networkViews) {
        if (eventAdmin != null) {
            final Map<String,String> eventProps = new HashMap<String,String>();
            eventProps.put("action", "apply preferred layout invoked");
            final Event event = new Event("org/cytoscape/gettingstarted", eventProps);
            eventAdmin.postEvent(event);
        }
		return new TaskIterator(2, new ApplyPreferredLayoutTask(networkViews, layouts, props));
	}
}
