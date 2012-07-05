/*
  File: EditNetworkTitleTask.java

  Copyright (c) 2006, 2011, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.task.internal.title;


import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.AbstractNetworkTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.undo.UndoSupport;
import java.util.Iterator;
import org.cytoscape.work.TunableValidator;


public class EditNetworkTitleTask extends AbstractNetworkTask implements TunableValidator {
	private final UndoSupport undoSupport;
	private final CyNetworkManager cyNetworkManagerServiceRef;
	private final CyNetworkNaming cyNetworkNamingServiceRef;

	
	@ProvidesTitle
	public String getTitle() {
		return "Rename Network";
	}
	
	@Tunable(description = "New title")
	public String title;

	public EditNetworkTitleTask(final UndoSupport undoSupport, final CyNetwork net, CyNetworkManager cyNetworkManagerServiceRef,
			CyNetworkNaming cyNetworkNamingServiceRef) {
		super(net);
		this.undoSupport = undoSupport;
		this.cyNetworkManagerServiceRef = cyNetworkManagerServiceRef;
		this.cyNetworkNamingServiceRef = cyNetworkNamingServiceRef;
		title = network.getRow(network).get(CyNetwork.NAME, String.class);		
	}

	@Override
	public ValidationState getValidationState(final Appendable errMsg) {
		title = title.trim();
		
		// Check if the network tile already existed
		boolean titleAlreayExisted = false;
		
		String newTitle = this.cyNetworkNamingServiceRef.getSuggestedNetworkTitle(title);
		if (!newTitle.equalsIgnoreCase(title)){
			titleAlreayExisted= true;
		}
				
		if (titleAlreayExisted){
			// Inform user duplicated network title!
			try {
				errMsg.append("Duplicated network title.");	
			}
			catch (Exception e){
				System.out.println("Warning: Duplicated network title.");
			}
			return ValidationState.INVALID;			
		}

		
		return ValidationState.OK;		
	}
	
	
	public void run(TaskMonitor e) {
		e.setProgress(0.0);
		final String oldTitle = network.getRow(network).get(CyNetwork.NAME, String.class);
		e.setProgress(0.3);
		network.getRow(network).set(CyNetwork.NAME, title);
		e.setProgress(0.6);
		undoSupport.postEdit(
			new NetworkTitleEdit(network, oldTitle));
		
		e.setProgress(1.0);
	}
}
