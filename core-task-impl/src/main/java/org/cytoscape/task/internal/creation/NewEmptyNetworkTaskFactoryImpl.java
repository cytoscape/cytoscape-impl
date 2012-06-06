/*
 File: NewEmptyNetworkTaskFactory.java

 Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

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

import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.create.NewEmptyNetworkViewFactory;
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

	private NewEmptyNetworkTask task;

	public NewEmptyNetworkTaskFactoryImpl(final CyNetworkFactory cnf, final CyNetworkViewFactory cnvf, 
			final CyNetworkManager netMgr, final CyNetworkViewManager networkViewManager, 
			final CyNetworkNaming namingUtil, final SynchronousTaskManager<?> syncTaskMgr,
			final VisualMappingManager vmm) {
		this.cnf = cnf;
		this.cnvf = cnvf;
		this.netMgr = netMgr;
		this.networkViewMgr = networkViewManager;
		this.namingUtil = namingUtil;
		this.syncTaskMgr = syncTaskMgr;
		this.vmm = vmm;
	}

	public TaskIterator createTaskIterator() {
		task = new NewEmptyNetworkTask(cnf, cnvf, netMgr, networkViewMgr, namingUtil, vmm);
		return new TaskIterator(task);
	} 

	public CyNetworkView createNewEmptyNetworkView() {
		// no tunables, so no need to set the execution context
		syncTaskMgr.execute(createTaskIterator());	
		return task.getView(); 
	}
}
