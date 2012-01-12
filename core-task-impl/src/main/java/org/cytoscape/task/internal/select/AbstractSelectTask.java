/*
 File: AbstractSelectTask.java

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
package org.cytoscape.task.internal.select;  


import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkTask;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;


public abstract class AbstractSelectTask extends AbstractNetworkTask {
	protected final CyNetworkViewManager networkViewManager;
	protected final SelectUtils selectUtils;
	protected final CyEventHelper eventHelper;

	public AbstractSelectTask(final CyNetwork net, final CyNetworkViewManager networkViewManager, final CyEventHelper eventHelper) {
		super(net);
		this.networkViewManager = networkViewManager;
		this.selectUtils = new SelectUtils();
		this.eventHelper = eventHelper;
	}

	protected final void updateView() {
		// This is necessary, otherwise, this does not update presentation!
		eventHelper.flushPayloadEvents();
		
		final CyNetworkView view = networkViewManager.getNetworkView(network);
		if (view != null)
			view.updateView();
	}
}
