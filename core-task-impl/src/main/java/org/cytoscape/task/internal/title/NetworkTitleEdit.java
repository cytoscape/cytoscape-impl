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
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.work.undo.AbstractCyEdit;


/** An undoable edit that will undo and redo renaming of a network. */ 
final class NetworkTitleEdit extends AbstractCyEdit {
	private final CyNetwork network;
	private String previousTitle;

	NetworkTitleEdit(final CyNetwork network, final String previousTitle) {
		super("Rename Title");

		this.network = network;
		this.previousTitle = previousTitle;
	}

	public void redo() {
		final String savedTitle = network.getRow(network).get(CyNetwork.NAME, String.class);
		network.getRow(network).set(CyNetwork.NAME, previousTitle);
		previousTitle = savedTitle;
	}

	public void undo() {
		final String savedTitle = network.getRow(network).get(CyNetwork.NAME, String.class);
		network.getRow(network).set(CyNetwork.NAME, previousTitle);
		previousTitle = savedTitle;
	}
}
