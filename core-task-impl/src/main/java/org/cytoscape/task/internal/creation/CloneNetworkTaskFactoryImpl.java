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
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.task.create.CloneNetworkTaskFactory;
import org.cytoscape.task.internal.utils.SessionUtils;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;

public class CloneNetworkTaskFactoryImpl extends AbstractNetworkTaskFactory implements CloneNetworkTaskFactory {
    private final CyNetworkManager networkMgr;
    private final CyNetworkViewManager networkViewMgr;
    private final VisualMappingManager vmm;
    private final CyNetworkFactory netFactory;
    private final CyNetworkViewFactory netViewFactory;
    private final CyNetworkNaming naming;
    private final CyApplicationManager appMgr;
    private final CyNetworkTableManager netTableMgr;
    private final CyRootNetworkManager rootNetMgr;
    private final CyGroupManager groupMgr;
	private final CyGroupFactory groupFactory;
	private final RenderingEngineManager renderingEngineMgr;
	private final CyNetworkViewFactory nullNetworkViewFactory;
	private final SessionUtils sessionUtils;

    public CloneNetworkTaskFactoryImpl(final CyNetworkManager networkMgr,
    								   final CyNetworkViewManager networkViewMgr,
    								   final VisualMappingManager vmm,
    								   final CyNetworkFactory netFactory,
    								   final CyNetworkViewFactory netViewFactory,
    								   final CyNetworkNaming naming,
    								   final CyApplicationManager appMgr,
    								   final CyNetworkTableManager netTableMgr,
    								   final CyRootNetworkManager rootNetMgr,
    								   final CyGroupManager groupMgr,
    								   final CyGroupFactory groupFactory,
    								   final RenderingEngineManager renderingEngineMgr,
    								   final CyNetworkViewFactory nullNetworkViewFactory,
    								   final SessionUtils sessionUtils) {
    	this.networkMgr = networkMgr;
		this.networkViewMgr = networkViewMgr;
		this.vmm = vmm;
		this.netFactory = netFactory;
		this.netViewFactory = netViewFactory;
		this.naming = naming;
		this.appMgr = appMgr;
		this.netTableMgr = netTableMgr;
		this.rootNetMgr = rootNetMgr;
		this.groupMgr = groupMgr;
		this.groupFactory = groupFactory;
		this.renderingEngineMgr = renderingEngineMgr;
		this.nullNetworkViewFactory = nullNetworkViewFactory;
		this.sessionUtils = sessionUtils;
    }

    @Override
    public TaskIterator createTaskIterator(CyNetwork network) {
    	return new TaskIterator(2,new CloneNetworkTask(network, networkMgr, networkViewMgr, vmm, netFactory, 
    			netViewFactory, naming, appMgr, netTableMgr, rootNetMgr, groupMgr, groupFactory, renderingEngineMgr, nullNetworkViewFactory));
    }
    
    @Override
	public boolean isReady(CyNetwork network) {
		return super.isReady(network) && sessionUtils.isSessionReady();
	}
}
