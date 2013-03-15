package org.cytoscape.editor.internal;

/*
 * #%L
 * Cytoscape Editor Impl (editor-impl)
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

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.undo.AbstractCyEdit;

import java.util.List;

public class RenameNodeEdit extends AbstractCyEdit {
	private final CyRow nodeRow;
	private final String oldName;
	private final String newName;

	public RenameNodeEdit(CyNetwork net, CyNode node, String oldName, String newName) { 
		super("Rename Node");
		this.nodeRow = net.getRow(node);
		this.oldName = oldName;
		this.newName = newName;
	}

	public void redo() {
		setName(newName);
	}

	public void undo() {
		setName(oldName);
	}

	private void setName(String name) {
		nodeRow.set(CyNetwork.NAME, name);
		nodeRow.set(CyRootNetwork.SHARED_NAME, name);
	}
}
