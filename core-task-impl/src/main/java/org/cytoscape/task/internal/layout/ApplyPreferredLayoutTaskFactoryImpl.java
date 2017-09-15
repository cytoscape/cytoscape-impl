package org.cytoscape.task.internal.layout;

import java.util.Collection;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewCollectionTaskFactory;
import org.cytoscape.task.visualize.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

public class ApplyPreferredLayoutTaskFactoryImpl extends AbstractNetworkViewCollectionTaskFactory implements
		ApplyPreferredLayoutTaskFactory, TaskFactory {

	private final CyServiceRegistrar serviceRegistrar;

	public ApplyPreferredLayoutTaskFactoryImpl(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public TaskIterator createTaskIterator(final Collection<CyNetworkView> networkViews) {
		return new TaskIterator(2, new ApplyPreferredLayoutTask(networkViews, serviceRegistrar));
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(2, new ApplyPreferredLayoutTask(serviceRegistrar));
	}

	@Override
	public boolean isReady() {
		return true;
	}
}
