package org.cytoscape.browser.internal.task;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.work.Togglable;

public class DynamicTogglableTableTaskFactory extends DynamicTableTaskFactory implements Togglable {

	public DynamicTogglableTableTaskFactory(TableTaskFactory factory, CyServiceRegistrar serviceRegistrar) {
		super(factory, serviceRegistrar);
	}

	@Override
	public boolean isOn() {
		return factory.isOn(getCurrentTable());
	}
}
