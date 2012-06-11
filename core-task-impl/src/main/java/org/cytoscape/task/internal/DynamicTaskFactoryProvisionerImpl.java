package org.cytoscape.task.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.task.DynamicTaskFactoryProvisioner;
import org.cytoscape.task.NetworkCollectionTaskFactory;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewCollectionTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class DynamicTaskFactoryProvisionerImpl implements DynamicTaskFactoryProvisioner{
	

	private final CyApplicationManager applicationManager;

	public DynamicTaskFactoryProvisionerImpl(CyApplicationManager applicationManager) {
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

	public  TaskFactory createFor(final NetworkCollectionTaskFactory factory) {
		return new TaskFactory() {
			@Override
			public TaskIterator createTaskIterator() {
				return factory.createTaskIterator(applicationManager.getSelectedNetworks());
			}
			
			@Override
			public boolean isReady() {
				return factory.isReady(applicationManager.getSelectedNetworks());
			}
		};
	}

	public  TaskFactory createFor(final NetworkViewCollectionTaskFactory factory) {
		return new TaskFactory() {
			@Override
			public TaskIterator createTaskIterator() {
				return factory.createTaskIterator(applicationManager.getSelectedNetworkViews());
			}
			
			@Override
			public boolean isReady() {
				return factory.isReady(applicationManager.getSelectedNetworkViews());
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
