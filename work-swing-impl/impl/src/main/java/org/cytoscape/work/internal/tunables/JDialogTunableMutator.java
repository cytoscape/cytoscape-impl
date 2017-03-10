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

import java.awt.Component;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.cytoscape.work.internal.tunables.utils.SimplePanel;
import org.cytoscape.work.internal.tunables.utils.TunableDialog;
import org.cytoscape.work.swing.GUITunableHandler;
import org.cytoscape.work.swing.RequestsUIHelper;
import org.cytoscape.work.swing.TunableUIHelper;

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
public class JDialogTunableMutator extends JPanelTunableMutator implements TunableUIHelper {

	/** Provides an initialised logger. */
	private Logger logger = LoggerFactory.getLogger(JDialogTunableMutator.class);

	private Window parent;

	private Dialog.ModalityType modality = Dialog.DEFAULT_MODALITY_TYPE;
	private	TunableDialog dialogWindow;
	private JPanel lastPanel = null;

	/**
	 * Constructor.
	 */
	public JDialogTunableMutator() {
		super();
	}

	/**
	 * Used configure the TunableMutator so that it builds its
	 * configuration object in the correct location. For instance,
	 * a GUI based TunableMutator might call this method with a
	 * JPanel, indicating that the TunableMutator should build its
	 * configuration within that JPanel.  This method may be a 
	 * no-op depending on the type of configuration.
	 * @param o The context object in which the configuration will be built.
	 * @param resetContext It tells whether the context map variables need to be cleared
	 */
	public void setConfigurationContext(Object win, boolean resetContext) {
		if (win == null) {
			if (resetContext) {
				handlerMap.clear();
				titleProviderMap.clear();
			}
			
			return;
		}

		if (win instanceof Window)
			parent = (Window) win;
		else
			throw new IllegalArgumentException("Dialog configuration context must be a Window, but it's a: "
					+ win.getClass());
	}

	@Override
	public JPanel buildConfiguration(Object objectWithTunables) {
		return super.buildConfiguration(objectWithTunables, parent);
	}

	@Override
	public boolean validateAndWriteBack(Object objectWithTunables) {
		JPanel panel = buildConfiguration(objectWithTunables, parent);
		lastPanel = panel;
		return validateAndWriteBack(panel, objectWithTunables);
	}

	public boolean validateAndWriteBack(JPanel panel, Object objectWithTunables) {
		// no tunables found, everything OK for task to proceed
		if (panel == null) {
			return true;
		} else if (panel == HANDLER_CANCEL_PANEL) {
			// found the special case of the file handle cancel panel,
			// which means we should quit now
			return false;
		} else {
			/*
			if (objectWithTunables instanceof RequestsUIHelper) {
				dialogWindow = (Window)panel.getTopLevelAncestor();
				((RequestsUIHelper)objectWithTunables).setUIHelper(this);
			}
			*/

			// If the tunables panel has no visible controls, it means the user can't do anything with it,
			// so just validate it and continue; no need to show the dialog.
			if (panel instanceof SimplePanel && !((SimplePanel)panel).hasVisibleControls(panel))
				return super.validateAndWriteBack(objectWithTunables);
			
			return displayGUI(panel, objectWithTunables);
		}
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
	private boolean displayGUI(JPanel optionPanel, Object objectWithTunables) {
		TunableDialog tunableDialog;
		boolean valid = false;
		String userInput;

		do {
			tunableDialog = new TunableDialog(parent, optionPanel);
			dialogWindow = tunableDialog;
			tunableDialog.setLocationRelativeTo(parent);

			if (objectWithTunables instanceof RequestsUIHelper) {
				((RequestsUIHelper)objectWithTunables).setUIHelper(this);
			}

			tunableDialog.setTitle(getTitle(objectWithTunables));
			tunableDialog.setModalityType(modality);
			tunableDialog.setVisible(true);

			userInput = tunableDialog.getUserInput();

			optionPanel = tunableDialog.getOptionPanel();

			if (userInput.equalsIgnoreCase("OK"))
				valid = super.validateAndWriteBack(objectWithTunables);
		} while (userInput.equalsIgnoreCase("OK") && !valid);

		return userInput.equalsIgnoreCase("OK") ? valid : false;
	}

	@Override
	public Window getParent() {
		return dialogWindow;
	}

	@Override
	public void setModality(Dialog.ModalityType modality) {
		this.modality = modality;
	}

	@Override
	public void update(Object objectWithTunables) {
		if (!handlerMap.containsKey(objectWithTunables)) {
			return;
		}

		if (handlers != null && handlers.size() > 0) {
			for (GUITunableHandler handler: handlers)
				handler.update();
		}
	}

	@Override
	public void refresh(Object objectWithTunables) {
		if (!handlerMap.containsKey(objectWithTunables)) {
			return;
		}
		handlerMap.remove(objectWithTunables);
		titleProviderMap.remove(objectWithTunables);

		// Rebuild the configuration;
		JPanel panel = buildConfiguration(objectWithTunables, parent);

		// validateAndWriteBack(panel, objectWithTunables);
		dialogWindow.updateOptionPanel(panel);
		update(objectWithTunables);
	}
}
