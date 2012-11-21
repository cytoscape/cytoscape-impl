/*
 File: NewNetworkSelectedNodesEdgesTaskFactory.java

 Copyright (c) 2006, 2010-2011, The Cytoscape Consortium (www.cytoscape.org)

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.task.internal.creation;


import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.task.create.NewNetworkSelectedNodesAndEdgesTaskFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;


public class NewNetworkSelectedNodesEdgesTaskFactoryImpl extends AbstractNetworkTaskFactory implements NewNetworkSelectedNodesAndEdgesTaskFactory{
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

	public NewNetworkSelectedNodesEdgesTaskFactoryImpl(final UndoSupport undoSupport,
	                                               final CyRootNetworkManager crnf,
	                                               final CyNetworkViewFactory cnvf,
	                                               final CyNetworkManager netmgr,
	                                               final CyNetworkViewManager networkViewManager,
	                                               final CyNetworkNaming naming,
	                                               final VisualMappingManager vmm,
	                                               final CyApplicationManager appManager,
	                                               final CyEventHelper eventHelper,
	                                               final CyGroupManager groupMgr,
	                                               final RenderingEngineManager renderingEngineMgr)
	{
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
	}

	public TaskIterator createTaskIterator(CyNetwork network) {
		return new TaskIterator(3,
			new NewNetworkSelectedNodesEdgesTask(undoSupport, network, crnf, cnvf,
			                                     netmgr, networkViewManager, naming, vmm,
			                                     appManager, eventHelper, groupMgr, renderingEngineMgr));
	}
}
