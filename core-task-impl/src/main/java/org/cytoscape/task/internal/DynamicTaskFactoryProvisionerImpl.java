package org.cytoscape.task.internal;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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
