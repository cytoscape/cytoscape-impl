/*
  File: UndoAction.java

  Copyright (c) 2006, 2011, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.task.internal.undo;


import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.undo.CannotUndoException;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.session.CyApplicationManager;
import org.cytoscape.work.undo.UndoSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An action that calls undo for the most recent edit in the
 * undoable edit stack.
 */
public class UndoAction extends AbstractCyAction {
	private final static long serialVersionUID = 1202339875212525L;

	private final static Logger logger = LoggerFactory.getLogger(UndoAction.class);

	private UndoSupport undo;

	/**
	 * Constructs the action.
	 */
	public UndoAction(UndoSupport undo,CyApplicationManager appMgr) {
		super("Undo",appMgr);
		setAcceleratorKeyStroke(
			KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit()
				.getMenuShortcutKeyMask()));
		setPreferredMenu("Edit");
		setEnabled(true);
		setMenuGravity(1.0f);
		this.undo = undo;
	}

	/**
	 * Tries to run undo() on the top edit of the edit stack.
	 * @param e The action event that triggers this method call.
	 */
	public void actionPerformed(ActionEvent e) {
		try {
			if ( undo.getUndoManager().canUndo() )
				undo.getUndoManager().undo();
		} catch (CannotUndoException ex) {
			logger.warn("Unable to undo: " + ex);
			ex.printStackTrace();
		}
	}

	/**
	 * Called when the menu that contains this action is clicked on.
	 * @param e The menu event that triggers this method call.
	 */
	public void menuSelected(MenuEvent e) {
		if (undo.getUndoManager().canUndo()) {
			setEnabled(true);
			putValue(Action.NAME, undo.getUndoManager().getUndoPresentationName());
		} else {
			setEnabled(false);
			putValue(Action.NAME, "Undo");
		}
	}
}
