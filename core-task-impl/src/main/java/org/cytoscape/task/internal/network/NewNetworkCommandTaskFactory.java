package org.cytoscape.task.internal.network;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.task.create.NewNetworkSelectedNodesAndEdgesTaskFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;

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

public class NewNetworkCommandTaskFactory extends AbstractNetworkTaskFactory 
                                          implements NewNetworkSelectedNodesAndEdgesTaskFactory {
	private final UndoSupport undoSupport;
	private final CyNetworkManager netmgr;
	private final CyNetworkViewManager networkViewManager;
	private final CyRootNetworkManager crnf;
	private final CyNetworkViewFactory cnvf;
	private final CyNetworkNaming naming;
	private final VisualMappingManager vmm;
	private final CyApplicationManager appManager;
	private final CyEventHelper eventHelper;
	private final CyGroupManager groupMgr;
	private final RenderingEngineManager renderingEngineMgr;
	private final CyServiceRegistrar serviceRegistrar;

	public NewNetworkCommandTaskFactory(final UndoSupport undoSupport,
	                                    final CyRootNetworkManager crnf,
	                                    final CyNetworkViewFactory cnvf,
	                                    final CyNetworkManager netmgr,
	                                    final CyNetworkViewManager networkViewManager,
	                                    final CyNetworkNaming naming,
	                                    final VisualMappingManager vmm,
	                                    final CyApplicationManager appManager,
	                                    final CyEventHelper eventHelper,
	                                    final CyGroupManager groupMgr,
	                                    final RenderingEngineManager renderingEngineMgr,
	                                    final CyServiceRegistrar serviceRegistrar) {
		this.undoSupport        = undoSupport;
		this.netmgr             = netmgr;
		this.networkViewManager = networkViewManager;
		this.crnf               = crnf;
		this.cnvf               = cnvf;
		this.naming             = naming;
		this.vmm                = vmm;
		this.appManager         = appManager;
		this.eventHelper        = eventHelper;
		this.groupMgr           = groupMgr;
		this.renderingEngineMgr = renderingEngineMgr;
		this.serviceRegistrar   = serviceRegistrar;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(3,
			new NewNetworkCommandTask(undoSupport, crnf, cnvf,
			                          netmgr, networkViewManager, naming, vmm,
			                          appManager, eventHelper, groupMgr, renderingEngineMgr, serviceRegistrar));
	}

	@Override
	public TaskIterator createTaskIterator(CyNetwork net) {
		return new TaskIterator(3,
			new NewNetworkCommandTask(undoSupport, crnf, cnvf,
			                          netmgr, networkViewManager, naming, vmm,
			                          appManager, eventHelper, groupMgr, renderingEngineMgr, serviceRegistrar));
	}
}
