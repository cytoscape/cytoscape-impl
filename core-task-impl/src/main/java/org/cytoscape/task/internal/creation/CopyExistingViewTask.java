
/*
 File: CopyExistingViewTask.java

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
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableEntry;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.undo.UndoSupport;

import java.util.Map;

/**
 * A utility task that copies the node positions and visual style to a new
 * network view from an existing network view.
 */
class CopyExistingViewTask extends AbstractTask {

	private final CyNetworkView newView;
	private final CyNetworkView sourceView;
	private final VisualMappingManager vmm; 
	private final Map<CyNode,CyNode> new2sourceMap; 

	CopyExistingViewTask(final VisualMappingManager vmm, 
	                     final CyNetworkView newView, 
	                     final CyNetworkView sourceView, 
	                     final Map<CyNode,CyNode> new2sourceMap // may be null!
						 ) {
		super();
		this.newView = newView;
		this.sourceView = sourceView;
		this.vmm = vmm;
		this.new2sourceMap = new2sourceMap;
	}

	@Override
	public void run(TaskMonitor tm) {

		if (sourceView == null)
			throw new NullPointerException("source network view is null.");
		if (newView == null)
			throw new NullPointerException("new network view is null.");

		tm.setProgress(0.0);

		CyNetwork sourceNet = sourceView.getModel();

		// copy node location only.
		for (View<CyNode> newNodeView : newView.getNodeViews()) {
			
			View<CyNode> origNodeView = getOrigNodeView( newNodeView ); 
			if ( origNodeView == null )
				continue;

			newNodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION,
					origNodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION));
			newNodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION,
					origNodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION));

			// FIXME
			// // Set lock (if necessary)
			// for ( VisualProperty<?> vp : vpSet ) {
			// if (origNodeView.isValueLocked(vp) )
			// newNodeView.setLockedValue(vp,
			// origNodeView.getVisualProperty(vp));
			// }
		}
		tm.setProgress(0.9);

		final VisualStyle style = vmm.getVisualStyle(sourceView);
		vmm.setVisualStyle(style, newView);
		style.apply(newView);
		newView.fitContent();
		
		tm.setProgress(1.0);
	}

	// may return null if nodes don't somehow line up!
	private View<CyNode> getOrigNodeView(View<CyNode> newNodeView) {
		if ( new2sourceMap != null ) {
			CyNode origNode = new2sourceMap.get(newNodeView.getModel());
			if (origNode == null) 
				return null;
			return sourceView.getNodeView(origNode);
		} else {
			return sourceView.getNodeView(newNodeView.getModel());
		}
	}
}
