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


import java.util.HashMap;
import java.util.Map;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.task.edit.EditNetworkTitleTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.undo.UndoSupport;


public class EditNetworkTitleTaskFactoryImpl extends AbstractNetworkTaskFactory implements EditNetworkTitleTaskFactory{
	private final UndoSupport undoSupport;
	private final CyNetworkManager cyNetworkManagerServiceRef;
	private final CyNetworkNaming cyNetworkNamingServiceRef;
	
	private final TunableSetter tunableSetter;

	public EditNetworkTitleTaskFactoryImpl(final UndoSupport undoSupport, CyNetworkManager cyNetworkManagerServiceRef,
			CyNetworkNaming cyNetworkNamingServiceRef, TunableSetter tunableSetter) {
		this.undoSupport = undoSupport;
		this.cyNetworkManagerServiceRef = cyNetworkManagerServiceRef;
		this.cyNetworkNamingServiceRef = cyNetworkNamingServiceRef;
		this.tunableSetter = tunableSetter;
	}

	public TaskIterator createTaskIterator(CyNetwork network) {
		return new TaskIterator(new EditNetworkTitleTask(undoSupport, network, this.cyNetworkManagerServiceRef, this.cyNetworkNamingServiceRef));
	}

	@Override
	public TaskIterator createTaskIterator(CyNetwork network, String title) {
		final Map<String, Object> m = new HashMap<>();
		m.put("title", title);

		return tunableSetter.createTaskIterator(this.createTaskIterator(network), m); 
	} 
}
