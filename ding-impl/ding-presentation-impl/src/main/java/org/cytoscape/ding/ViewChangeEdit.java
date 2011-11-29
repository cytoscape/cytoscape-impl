
/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

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

package org.cytoscape.ding;


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

	public static enum SavedObjs { ALL, SELECTED, NODES, EDGES, SELECTED_NODES, SELECTED_EDGES }

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
