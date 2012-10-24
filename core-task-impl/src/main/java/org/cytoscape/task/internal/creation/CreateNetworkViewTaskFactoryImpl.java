/*
 File: CreateNetworkViewTaskFactory.java

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


import java.util.Collection;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkCollectionTaskFactory;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;


public class CreateNetworkViewTaskFactoryImpl extends AbstractNetworkCollectionTaskFactory implements CreateNetworkViewTaskFactory {
	
	private final UndoSupport undoSupport;
	private final CyNetworkViewManager netViewMgr;
	private final CyNetworkViewFactory viewFactory;
	private final CyLayoutAlgorithmManager layoutMgr;
	private final CyEventHelper eventHelper;
	private final VisualMappingManager vmm;
	private final RenderingEngineManager renderingEngineMgr;

	public CreateNetworkViewTaskFactoryImpl(final UndoSupport undoSupport,
											final CyNetworkViewFactory viewFactory,
											final CyNetworkViewManager netViewMgr,
											final CyLayoutAlgorithmManager layoutMgr,
											final CyEventHelper eventHelper,
											final VisualMappingManager vmm,
											final RenderingEngineManager renderingEngineMgr) {
		this.undoSupport        = undoSupport;
		this.viewFactory        = viewFactory;
		this.netViewMgr         = netViewMgr;
		this.layoutMgr          = layoutMgr;
		this.eventHelper        = eventHelper;
		this.vmm                = vmm;
		this.renderingEngineMgr = renderingEngineMgr;
	}


	@Override
	public TaskIterator createTaskIterator(final Collection<CyNetwork> networks) {
		// Create visualization + layout (optional)
		if (layoutMgr == null)
			return new TaskIterator(1, new CreateNetworkViewTask(undoSupport, networks, viewFactory, netViewMgr,
					layoutMgr, eventHelper, vmm, renderingEngineMgr));
		else
			return new TaskIterator(2, new CreateNetworkViewTask(undoSupport, networks, viewFactory, netViewMgr,
					layoutMgr, eventHelper, vmm, renderingEngineMgr));
	}

	@Override
	public boolean isReady(final Collection<CyNetwork> networks) {
        for (CyNetwork n : networks) 
            if (!netViewMgr.getNetworkViews(n).isEmpty()) 
                return false;
        
        return true;
	}

}
