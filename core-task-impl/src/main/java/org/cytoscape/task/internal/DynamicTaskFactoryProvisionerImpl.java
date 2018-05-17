package org.cytoscape.task.internal;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.DynamicTaskFactoryProvisioner;
import org.cytoscape.task.NetworkCollectionTaskFactory;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewCollectionTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

public class DynamicTaskFactoryProvisionerImpl implements DynamicTaskFactoryProvisioner {

	private final CyServiceRegistrar serviceRegistrar;

	public DynamicTaskFactoryProvisionerImpl(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public TaskFactory createFor(final NetworkTaskFactory factory) {
		return new TaskFactory() {
			@Override
			public TaskIterator createTaskIterator() {
				return factory.createTaskIterator(getApplicationManager().getCurrentNetwork());
			}
			
			@Override
			public boolean isReady() {
				return factory.isReady(getApplicationManager().getCurrentNetwork());
			}
		};
	}

	@Override
	public  TaskFactory createFor(final NetworkViewTaskFactory factory) {
		return new TaskFactory() {
			@Override
			public TaskIterator createTaskIterator() {
				return factory.createTaskIterator(getApplicationManager().getCurrentNetworkView());
			}
			
			@Override
			public boolean isReady() {
				return factory.isReady(getApplicationManager().getCurrentNetworkView());
			}
		};
	}

	@Override
	public  TaskFactory createFor(final NetworkCollectionTaskFactory factory) {
		return new TaskFactory() {
			@Override
			public TaskIterator createTaskIterator() {
				return factory.createTaskIterator(getApplicationManager().getSelectedNetworks());
			}
			
			@Override
			public boolean isReady() {
				return factory.isReady(getApplicationManager().getSelectedNetworks());
			}
		};
	}

	@Override
	public  TaskFactory createFor(final NetworkViewCollectionTaskFactory factory) {
		return new TaskFactory() {
			@Override
			public TaskIterator createTaskIterator() {
				return factory.createTaskIterator(getApplicationManager().getSelectedNetworkViews());
			}
			
			@Override
			public boolean isReady() {
				return factory.isReady(getApplicationManager().getSelectedNetworkViews());
			}
		};
	}

	@Override
	public  TaskFactory createFor(final TableTaskFactory factory) {
		return new TaskFactory() {
			public TaskIterator createTaskIterator() {
				return factory.createTaskIterator(getApplicationManager().getCurrentTable());
			}
			
			@Override
			public boolean isReady() {
				return factory.isReady(getApplicationManager().getCurrentTable());
			}
		};
	}
	
	private CyApplicationManager getApplicationManager() {
		return serviceRegistrar.getService(CyApplicationManager.class);
	}
}
