package org.cytoscape.internal.undo;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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


import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.undo.CannotUndoException;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.work.swing.undo.SwingUndoSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An action that calls undo for the most recent edit in the
 * undoable edit stack.
 */
public class UndoAction extends AbstractCyAction {
	private final static long serialVersionUID = 1202339875212525L;

	private final static Logger logger = LoggerFactory.getLogger(UndoAction.class);

	private SwingUndoSupport undo;

	/**
	 * Constructs the action.
	 */
	public UndoAction(SwingUndoSupport undo) {
		super("Undo");
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
