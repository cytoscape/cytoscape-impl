package org.cytoscape.webservice.internal.task;

/*
 * #%L
 * Cytoscape Webservice Impl (webservice-impl)
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

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.webservice.internal.ui.WebServiceImportDialog;

/**
 * Display Network Import GUI.
 * 
 */
public class ShowImportDialogAction extends AbstractCyAction {

	private static final long serialVersionUID = -36712860667900147L;


	private WebServiceImportDialog<?> dialog;
	private final Window parent;

	public ShowImportDialogAction(final CySwingApplication app,
			final WebServiceImportDialog<?> dialog, final String menuLocation, final String menuLabel,
			final KeyStroke shortcut) {
		super(menuLabel);

		if (dialog == null)
			throw new NullPointerException("Dialog is null.");

		if (menuLocation == null)
			throw new NullPointerException("Menu Location is null.");
		
		if (menuLabel == null)
			throw new NullPointerException("Menu Label is null.");

		setPreferredMenu(menuLocation);
		setMenuGravity(3.0f);

		if(shortcut != null)
			setAcceleratorKeyStroke(shortcut);

		this.parent = app.getJFrame();
		this.dialog = dialog;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		dialog.prepareForDisplay();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
	}
}
