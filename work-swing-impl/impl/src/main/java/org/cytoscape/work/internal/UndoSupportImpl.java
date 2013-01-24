package org.cytoscape.work.internal;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2007 - 2013 The Cytoscape Consortium
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

