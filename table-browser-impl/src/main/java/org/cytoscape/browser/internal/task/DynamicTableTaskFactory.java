package org.cytoscape.browser.internal.task;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.DynamicTaskFactoryProvisioner;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

/**
 * This task factory wraps a {@link TableTaskFactory} in a simpler {@link TaskFactory}, because the one
 * provided by {@link DynamicTaskFactoryProvisioner} does not expose the method
 * {@link TableTaskFactory#isApplicable(CyTable)}, which we need here if we want to dynamically
 * show/hide buttons created from a {@link TableTaskFactory}.
 */
public class DynamicTableTaskFactory implements TaskFactory {

	protected final TableTaskFactory factory;
	protected final CyServiceRegistrar serviceRegistrar;

	public DynamicTableTaskFactory(TableTaskFactory factory, CyServiceRegistrar serviceRegistrar) {
		this.factory = factory;
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return factory.createTaskIterator(getCurrentTable());
	}
	
	@Override
	public boolean isReady() {
		return factory.isReady(getCurrentTable());
	}
	
	public boolean isReady(CyTable table) {
		return factory.isReady(table);
	}
	
	/**
	 * Just calls {@link TableTaskFactory#isApplicable(CyTable)} on the wrapped factory and pass the current table.
	 */
	public boolean isApplicable() {
		return factory.isApplicable(getCurrentTable());
	}
	
	public boolean isApplicable(CyTable table) {
		return factory.isApplicable(table);
	}
	
	protected CyTable getCurrentTable() {
		return serviceRegistrar.getService(CyApplicationManager.class).getCurrentTable();
	}
}
