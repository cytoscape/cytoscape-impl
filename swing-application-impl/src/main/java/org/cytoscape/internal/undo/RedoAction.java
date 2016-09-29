package org.cytoscape.internal.undo;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.undo.CannotUndoException;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.swing.undo.SwingUndoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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
 * An action that calls redo for the most recent edit in the undoable edit stack.  
 */
@SuppressWarnings("serial")
public class RedoAction extends AbstractCyAction {

	private static final Logger logger = LoggerFactory.getLogger(RedoAction.class);
	
	private final CyServiceRegistrar serviceRegistrar;

	public RedoAction(final CyServiceRegistrar serviceRegistrar) {
		super("Redo");
		this.serviceRegistrar = serviceRegistrar;
		
		setAcceleratorKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		setPreferredMenu("Edit");
		setEnabled(true);
		setMenuGravity(1.1f);
	}

	/**
	 * Tries to run redo() on the top edit of the edit stack. 
	 * @param e The action event that triggers this method call.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			final SwingUndoSupport undo = serviceRegistrar.getService(SwingUndoSupport.class);
			
			if (undo.getUndoManager().canRedo())
				undo.getUndoManager().redo();
		} catch (CannotUndoException ex) {
			logger.warn("Unable to redo: " + ex);
			ex.printStackTrace();
		}
	}

	/**
	 * Called when the menu that contains this action is clicked on. 
	 * @param e The menu event that triggers this method call.
	 */
	@Override
	public void menuSelected(MenuEvent e) {
		final SwingUndoSupport undo = serviceRegistrar.getService(SwingUndoSupport.class);
		
		if (undo.getUndoManager().canRedo()) {
			setEnabled(true);
			putValue(Action.NAME, undo.getUndoManager().getRedoPresentationName());
		} else {
			setEnabled(false);
			putValue(Action.NAME, "Redo");
		}
	}
}
