package org.cytoscape.command.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class DynamicTaskFactoryProvisioner {

	private CyApplicationManager applicationManager;

	public DynamicTaskFactoryProvisioner(CyApplicationManager applicationManager) {
		this.applicationManager = applicationManager;
	}

	public  TaskFactory createFor(final NetworkTaskFactory factory) {
		return new TaskFactory() {
			@Override
			public TaskIterator createTaskIterator() {
				return factory.createTaskIterator(applicationManager.getCurrentNetwork());
			}
			
			@Override
			public boolean isReady() {
				return factory.isReady(applicationManager.getCurrentNetwork());
			}
		};
	}

	public  TaskFactory createFor(final NetworkViewTaskFactory factory) {
		return new TaskFactory() {
			@Override
			public TaskIterator createTaskIterator() {
				return factory.createTaskIterator(applicationManager.getCurrentNetworkView());
			}
			
			@Override
			public boolean isReady() {
				return factory.isReady(applicationManager.getCurrentNetworkView());
			}
		};
	}

	public  TaskFactory createFor(final TableTaskFactory factory) {
		return new TaskFactory() {
			public TaskIterator createTaskIterator() {
				return factory.createTaskIterator(applicationManager.getCurrentTable());
			}
			
			@Override
			public boolean isReady() {
				return factory.isReady(applicationManager.getCurrentTable());
			}
		};
	}
}
