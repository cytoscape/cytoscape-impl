/*
 File: CreateNetworkViewTask.java

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


import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.task.AbstractNetworkTask;
import org.cytoscape.task.internal.layout.ApplyPreferredLayoutTask;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CreateNetworkViewTask extends AbstractNetworkTask {
	
	private static final Logger logger = LoggerFactory.getLogger(CreateNetworkViewTask.class);
	
	private final UndoSupport undoSupport;
	private final CyNetworkViewManager networkViewManager;
	private final CyNetworkViewFactory viewFactory;
	private final CyLayoutAlgorithmManager layouts;
	private final CyEventHelper eventHelper;

	public CreateNetworkViewTask(final UndoSupport undoSupport, final CyNetwork networkModel,
	                             final CyNetworkViewFactory viewFactory,
	                             final CyNetworkViewManager networkViewManager,
	                             final CyLayoutAlgorithmManager layouts,
	                             final CyEventHelper eventHelper)
	{
		super(networkModel);

		this.undoSupport        = undoSupport;
		this.viewFactory        = viewFactory;
		this.networkViewManager = networkViewManager;
		this.layouts            = layouts;
		this.eventHelper        = eventHelper;
	}

	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.0);
		final long start = System.currentTimeMillis();

		taskMonitor.setStatusMessage("Creating network view...");

		try {
			// By calling this task, actual view will be created even if it's a
			// large network.
			taskMonitor.setProgress(0.4);
			final CyNetworkView view = viewFactory.createNetworkView(network, false);
			taskMonitor.setProgress(0.8);
			networkViewManager.addNetworkView(view);
			
			// Apply layout only when it is necessary.
			if (layouts != null)
				insertTasksAfterCurrentTask(new ApplyPreferredLayoutTask(view, layouts));

		} catch (Exception e) {
			throw new Exception("Could not create network view for network: "
				+ network.getCyRow().get(CyTableEntry.NAME, String.class), e);
		}

		if (undoSupport != null)
			undoSupport.postEdit(
				new CreateNetworkViewEdit(eventHelper, network, viewFactory,
				                          networkViewManager));
		
		taskMonitor.setProgress(1.0);
		taskMonitor.setStatusMessage("Network view successfully create for:  "
				+ network.getCyRow().get(CyTableEntry.NAME, String.class));
		
		logger.info("Network view creation finished in " + (System.currentTimeMillis() -start) + " msec.");
	}
}
