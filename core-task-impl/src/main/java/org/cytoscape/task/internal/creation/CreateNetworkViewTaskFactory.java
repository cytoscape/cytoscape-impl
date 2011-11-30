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


import org.cytoscape.event.CyEventHelper;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;


public class CreateNetworkViewTaskFactory extends AbstractNetworkTaskFactory {
	private final UndoSupport undoSupport;
	private final CyNetworkViewManager networkViewManager;
	private final CyNetworkViewFactory viewFactory;
	private final CyLayoutAlgorithmManager layouts;
	private final CyEventHelper eventHelper;

	public CreateNetworkViewTaskFactory(final UndoSupport undoSupport,
	                             final CyNetworkViewFactory viewFactory,
	                             final CyNetworkViewManager networkViewManager,
	                             final CyLayoutAlgorithmManager layouts,
	                             final CyEventHelper eventHelper)
	{
		this.undoSupport        = undoSupport;
		this.viewFactory        = viewFactory;
		this.networkViewManager = networkViewManager;
		this.layouts            = layouts;
		this.eventHelper        = eventHelper;
	}

	public TaskIterator createTaskIterator() {
		return new TaskIterator(2, new CreateNetworkViewTask(undoSupport, network, viewFactory,
		                                                     networkViewManager, layouts,
		                                                     eventHelper));
	}
}
