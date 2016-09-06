package org.cytoscape.ding;

import org.cytoscape.ding.impl.ViewState;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.undo.UndoSupport;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

/**
 * A Ding specific undoable edit.
 */
public class ViewChangeEdit extends AbstractCyEdit {

	private ViewState origState;
	private ViewState newState;
	private final GraphView graphView;
	private final SavedObjs savedObjs;

	private final CyServiceRegistrar serviceRegistrar;

	public static enum SavedObjs { ALL, SELECTED, NODES, EDGES, SELECTED_NODES, SELECTED_EDGES }

	public ViewChangeEdit(GraphView view, String label, CyServiceRegistrar serviceRegistrar) {
		this(view, SavedObjs.ALL, label, serviceRegistrar);
	}

	public ViewChangeEdit(GraphView graphView, SavedObjs saveObjs, String label, CyServiceRegistrar serviceRegistrar) {
		super(label);
		this.graphView = graphView;
		this.savedObjs = saveObjs;
		this.serviceRegistrar = serviceRegistrar;

		saveOldPositions();
	}

	protected void saveOldPositions() {
		origState = new ViewState(graphView, savedObjs);
	}

	protected void saveNewPositions() {
		newState = new ViewState(graphView, savedObjs);
	}

	public void post() {
		saveNewPositions();
		
		if (!origState.equals(newState))
			serviceRegistrar.getService(UndoSupport.class).postEdit(this);
	}

	/**
	 * Applies the new state to the view after it has been undone.
	 */
	@Override
	public void redo() {
		newState.apply();
	}

	/**
	 * Applies the original state to the view.
	 */
	@Override
	public void undo() {
		origState.apply();
	}
}
