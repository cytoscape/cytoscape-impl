package org.cytoscape.task.internal.zoom;

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


import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_SCALE_FACTOR;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.undo.AbstractCyEdit;


/** An undoable edit that will undo and redo the zooming of a network view. */
final class ZoomEdit extends AbstractCyEdit {
	private final CyNetworkView view;
	private final double factor;

	ZoomEdit(final CyNetworkView view, final double factor) {
		super(factor < 0.0 ? "Zoom Out" : "Zoom In");

		this.view   = view;
		this.factor = factor;
	}

	public void redo() {
		;

		final double currentScaleFactor = view.getVisualProperty(NETWORK_SCALE_FACTOR);
		view.setVisualProperty(NETWORK_SCALE_FACTOR, currentScaleFactor * factor);
		view.updateView();
	}

	public void undo() {
		;

		final double currentScaleFactor = view.getVisualProperty(NETWORK_SCALE_FACTOR);
		view.setVisualProperty(NETWORK_SCALE_FACTOR, currentScaleFactor / factor);
		view.updateView();
	}
}
