package org.cytoscape.work.internal.task;

/*
 * #%L
 * org.cytoscape.work-headless-impl
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


import javax.swing.undo.*;

import org.cytoscape.work.swing.undo.SwingUndoSupport;
import org.cytoscape.work.undo.AbstractCyEdit;

public class UndoSupportImpl implements SwingUndoSupport {

	public UndoSupportImpl() {
	}

	/**
	 * Returns the UndoManager. To preserve encapsulation and to prevent
	 * the wrong hands for mucking with your edit stack, don't make this
	 * public.  Rather, extend the class and use this method with the 
	 * class and package to set up actions, etc..
	 * @return the UndoManager used for managing the edits.
	 */
	public UndoManager getUndoManager() {
		return null;
	}

	/**
	 * Use this method to get the UndoableEditSupport which you should use
	 * to post edits: Undo.getUndoableEditSupport().postEdit(yourEdit).
	 * @return the UndoableEditSupport used for posting edits. 
	 */
	public UndoableEditSupport getUndoableEditSupport() {
		return null;
	}
	
	@Override
	public void reset() {
	}

	@Override
	public void postEdit(AbstractCyEdit edit) {
		// TODO Auto-generated method stub
		
	}
}

