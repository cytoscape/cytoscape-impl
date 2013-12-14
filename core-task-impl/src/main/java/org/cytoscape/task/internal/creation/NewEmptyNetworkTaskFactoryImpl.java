package org.cytoscape.task.internal.creation;

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
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.create.NewEmptyNetworkViewFactory;
import org.cytoscape.task.internal.utils.SessionUtils;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;

public class NewEmptyNetworkTaskFactoryImpl extends AbstractTaskFactory implements NewEmptyNetworkViewFactory {
	private final CyNetworkFactory cnf;
	private final CyNetworkViewFactory cnvf;
	private final CyNetworkManager netMgr;
	private final CyNetworkViewManager networkViewMgr;
	private final CyNetworkNaming namingUtil;
	private final SynchronousTaskManager<?> syncTaskMgr;
	private final VisualMappingManager vmm;
	private final CyRootNetworkManager cyRootNetworkManager;
	private final CyApplicationManager cyApplicationManager;
	private final SessionUtils sessionUtils;
	
	public NewEmptyNetworkTaskFactoryImpl(final CyNetworkFactory cnf, final CyNetworkViewFactory cnvf, 
			final CyNetworkManager netMgr, final CyNetworkViewManager networkViewManager, 
			final CyNetworkNaming namingUtil, final SynchronousTaskManager<?> syncTaskMgr,
			final VisualMappingManager vmm, final CyRootNetworkManager cyRootNetworkManager,
			final CyApplicationManager cyApplicationManager, final SessionUtils sessionUtils) {
		this.cnf = cnf;
		this.cnvf = cnvf;
		this.netMgr = netMgr;
		this.networkViewMgr = networkViewManager;
		this.namingUtil = namingUtil;
		this.syncTaskMgr = syncTaskMgr;
		this.vmm = vmm;
		this.cyRootNetworkManager = cyRootNetworkManager;
		this.cyApplicationManager = cyApplicationManager;
		this.sessionUtils = sessionUtils;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(createTask());
	} 

	private NewEmptyNetworkTask createTask() {
		return new NewEmptyNetworkTask(cnf, cnvf, netMgr, networkViewMgr, namingUtil, vmm, cyRootNetworkManager, cyApplicationManager);
	}
	
	@Override
	public CyNetworkView createNewEmptyNetworkView() {
		// no tunables, so no need to set the execution context
		NewEmptyNetworkTask task = createTask();
		syncTaskMgr.execute(new TaskIterator(task));	
		return task.getView(); 
	}
	
	@Override
	public boolean isReady() {
		return sessionUtils.isSessionReady();
	}
}
