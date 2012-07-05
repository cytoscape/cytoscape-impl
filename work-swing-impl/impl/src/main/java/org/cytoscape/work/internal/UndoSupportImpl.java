/*
  File: UndoSupportImpl.java

  Copyright (c) 2007, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.work.internal;

import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEditSupport;
import javax.swing.undo.AbstractUndoableEdit;

import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.swing.undo.SwingUndoSupport;


public class UndoSupportImpl implements SwingUndoSupport {

	private UndoManager m_undoManager;
	private UndoableEditSupport m_undoSupport; 

	public UndoSupportImpl() {
		m_undoManager = new UndoManager();
		m_undoSupport = new UndoableEditSupport();
		m_undoSupport.addUndoableEditListener( m_undoManager );
	}

	/**
	 * Returns the UndoManager. To preserve encapsulation and to prevent
	 * the wrong hands for mucking with your edit stack, don't make this
	 * public.  Rather, extend the class and use this method with the 
	 * class and package to set up actions, etc..
	 * @return the UndoManager used for managing the edits.
	 */
	public UndoManager getUndoManager() {
		return m_undoManager;
	}

	/**
	 * Use this method to get the UndoableEditSupport which you should use
	 * to post edits: Undo.getUndoableEditSupport().postEdit(yourEdit).
	 * @return the UndoableEditSupport used for posting edits. 
	 */
	public UndoableEditSupport getUndoableEditSupport() {
		return m_undoSupport;
	}

	/**
	 * Posts the edit to the UndoableEditSupport instance.
	 */
	public void postEdit(AbstractCyEdit edit) {
		if ( edit != null )
			m_undoSupport.postEdit( new SwingEditWrapper(edit) );	
	}
	
	@Override
	public void reset() {
		m_undoManager.discardAllEdits();
	}

	/**
	 * Creates a Swing UndoableEdit by wrapping a Cytoscape AbstractCyEdit.
	 */
	private class SwingEditWrapper extends AbstractUndoableEdit {

		private final AbstractCyEdit edit;

		SwingEditWrapper(AbstractCyEdit edit) {
			super();
			this.edit = edit;
		}
	
		public String getPresentationName() {
			return edit.getPresentationName();
		}

		public String getUndoPresentationName() {
			return edit.getUndoPresentationName();
		}

		public String getRedoPresentationName() {
			return edit.getRedoPresentationName();
		}
		
		public void undo() {
			super.undo();
			edit.undo();
		}

		public void redo() {
			super.redo();
			edit.redo();
		}
	}
}

