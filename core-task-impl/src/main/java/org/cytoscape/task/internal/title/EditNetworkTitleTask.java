package org.cytoscape.task.internal.title;

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
	public String name;

	@Tunable(description = "Network to rename", context="nogui")
	public CyNetwork sourceNetwork = null;

	public EditNetworkTitleTask(final UndoSupport undoSupport, final CyNetwork net, CyNetworkManager cyNetworkManagerServiceRef,
			CyNetworkNaming cyNetworkNamingServiceRef) {
		super(net);
		this.undoSupport = undoSupport;
		this.cyNetworkManagerServiceRef = cyNetworkManagerServiceRef;
		this.cyNetworkNamingServiceRef = cyNetworkNamingServiceRef;
		name = network.getRow(network).get(CyNetwork.NAME, String.class);		
	}

	@Override
	public ValidationState getValidationState(final Appendable errMsg) {
		name = name.trim();
		
		// Check if the network tile already existed
		boolean titleAlreayExisted = false;
		
		String newTitle = this.cyNetworkNamingServiceRef.getSuggestedNetworkTitle(name);
		if (!newTitle.equalsIgnoreCase(name)){
			titleAlreayExisted= true;
		}
				
		if (titleAlreayExisted){
			// Inform user duplicated network title!
			try {
				errMsg.append("Duplicated network name.");	
			}
			catch (Exception e){
				System.out.println("Warning: Duplicated network name.");
			}
			return ValidationState.INVALID;			
		}

		
		return ValidationState.OK;		
	}
	
	
	public void run(TaskMonitor e) {
		e.setProgress(0.0);
		if (sourceNetwork == null)
			sourceNetwork = network;
		final String oldTitle = network.getRow(sourceNetwork).get(CyNetwork.NAME, String.class);
		e.setProgress(0.3);
		network.getRow(sourceNetwork).set(CyNetwork.NAME, name);
		e.setProgress(0.6);
		undoSupport.postEdit(
			new NetworkTitleEdit(sourceNetwork, oldTitle));
		
		e.setProgress(1.0);
	}
}
