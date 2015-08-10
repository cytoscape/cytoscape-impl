package org.cytoscape.task.internal.layout;

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

import java.util.Collection;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.task.AbstractNetworkViewCollectionTaskFactory;
import org.cytoscape.task.visualize.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class ApplyPreferredLayoutTaskFactoryImpl extends AbstractNetworkViewCollectionTaskFactory implements
		ApplyPreferredLayoutTaskFactory, TaskFactory {

	private final CyLayoutAlgorithmManager layouts;
	private final CyNetworkViewManager viewMgr;
	private final CyApplicationManager appMgr;

	public ApplyPreferredLayoutTaskFactoryImpl(final CyApplicationManager appMgr, final CyNetworkViewManager viewMgr,
	                                           final CyLayoutAlgorithmManager layouts) {
		this.layouts = layouts;
		this.appMgr = appMgr;
		this.viewMgr = viewMgr;
	}

	@Override
	public TaskIterator createTaskIterator(final Collection<CyNetworkView> networkViews) {
		return new TaskIterator(2, new ApplyPreferredLayoutTask(networkViews, layouts));
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(2, new ApplyPreferredLayoutTask(appMgr, viewMgr, layouts));
	}

	@Override
	public boolean isReady() {
		return true;
	}
}
