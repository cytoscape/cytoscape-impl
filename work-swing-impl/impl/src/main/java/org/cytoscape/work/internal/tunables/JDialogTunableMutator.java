package org.cytoscape.work.internal.tunables;

/*
 * #%L
 * Cytoscape Work Swing Impl (work-swing-impl)
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

import java.awt.Dialog;
import java.awt.Window;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interceptor of <code>Tunable</code> that will be applied on
 * <code>GUITunableHandlers</code>.
 * 
 * <p>
 * 
 * <pre>
 * To set the new value to the original objects contained in the <code>GUITunableHandlers</code>:
 * <ul>
 *   <li>Creates the parent container for the GUI, or use the one that is specified </li>
 *   <li>Creates a GUI with swing components for each intercepted <code>Tunable</code> </li>
 *   <li>
 *     Displays the GUI to the user, following the layout construction rule specified in the <code>Tunable</code>
 *     annotations, and the dependencies to enable or not the graphic components
 *   </li>
 *   <li>
 *     Applies the new <i>value,item,string,state...</i> to the object contained in the <code>GUITunableHandler</code>,
 *     if the modifications have been validated by the user.
 *   </li>
 * </ul>
 * </pre>
 * 
 * </p>
 * 
 * @author pasteur
 */
public class JDialogTunableMutator extends JPanelTunableMutator {

	/** Provides an initialised logger. */
	private Logger logger = LoggerFactory.getLogger(JDialogTunableMutator.class);

	private Window parent = null;

	/**
	 * Constructor.
	 */
	public JDialogTunableMutator() {
		super();
	}

	/** {@inheritDoc} */
	public void setConfigurationContext(Object win) {
		if (win == null)
			return;

		if (win instanceof Window)
			parent = (Window) win;
		else
			throw new IllegalArgumentException("Dialog configuration context must be a Window, but it's a: "
					+ win.getClass());
	}

	/** {@inheritDoc} */
	public boolean validateAndWriteBack(Object objectWithTunables) {
		final JPanel panel = buildConfiguration(objectWithTunables, parent);

		// no tunables found, everything OK for task to proceed
		if (panel == null)
			return true;

		// found the special case of the file handle cancel panel,
		// which means we should quit now
		else if (panel == HANDLER_CANCEL_PANEL)
			return false;

		else
			return displayGUI(panel, objectWithTunables);
	}

	/**
	 * This implements the final action in execUI() and executes the UI.
	 * 
	 * @param optionPanel
	 *            the panel containing the various UI elements corresponding to
	 *            individual tunables
	 * @param objectWithTunables
	 *            represents the objects annotated with tunables
	 */
	private boolean displayGUI(final JPanel optionPanel, Object objectWithTunables) {
		TunableDialog tunableDialog;
		boolean result = false;
		String userInput;

		do {
			tunableDialog = new TunableDialog(parent, optionPanel);
			tunableDialog.setLocationRelativeTo(parent);

			tunableDialog.setTitle(getTitle(objectWithTunables));
			tunableDialog.setModalityType(Dialog.DEFAULT_MODALITY_TYPE);
			tunableDialog.setVisible(true);

			userInput = tunableDialog.getUserInput();
			if (userInput.equalsIgnoreCase("OK")) {
				result = super.validateAndWriteBack(objectWithTunables);
			}
		} while (userInput.equalsIgnoreCase("OK") == true && result == false);

		if (userInput.equalsIgnoreCase("OK")) {
			return result;
		} else {
			return false;
		}
	}
}
