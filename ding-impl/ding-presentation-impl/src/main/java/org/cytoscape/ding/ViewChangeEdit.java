package org.cytoscape.ding;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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


import org.cytoscape.ding.impl.ViewState;
import org.cytoscape.work.undo.UndoSupport;
import org.cytoscape.work.undo.AbstractCyEdit;


/**
 * A Ding specific undoable edit.
 */
public class ViewChangeEdit extends AbstractCyEdit {
	private final static long serialVersionUID = 1202416511789433L;

	private ViewState origState;
	private ViewState newState;

	private GraphView m_view;

	private SavedObjs m_savedObjs;

	private UndoSupport m_undo;

	public enum SavedObjs { ALL, SELECTED, NODES, EDGES, SELECTED_NODES, SELECTED_EDGES }

	public ViewChangeEdit(GraphView view,String label,UndoSupport undo) {
		this(view, SavedObjs.ALL, label, undo);
	}

	public ViewChangeEdit(GraphView view, SavedObjs saveObjs, String label, UndoSupport undo) {
		super(label);
		m_view = view;
		m_savedObjs = saveObjs;
		m_undo = undo;

		saveOldPositions();
	}

	protected void saveOldPositions() {
		origState = new ViewState(m_view, m_savedObjs);
	}

	protected void saveNewPositions() {
		newState = new ViewState(m_view, m_savedObjs);
	}

	public void post() {
		saveNewPositions();
		if ( !origState.equals(newState) )
			m_undo.postEdit( this );
	}

	/**
	 * Applies the new state to the view after it has been undone.
	 */
	public void redo() {
		newState.apply();
	}

	/**
	 * Applies the original state to the view.
	 */
	public void undo() {
		origState.apply();
	}
}
