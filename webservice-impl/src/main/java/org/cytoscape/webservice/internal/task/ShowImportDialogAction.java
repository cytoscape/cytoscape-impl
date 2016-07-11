package org.cytoscape.webservice.internal.task;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.webservice.internal.ui.WebServiceImportDialog;

/*
 * #%L
 * Cytoscape Webservice Impl (webservice-impl)
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
 * Display Network Import GUI.
 */
@SuppressWarnings("serial")
public class ShowImportDialogAction extends AbstractCyAction {

	private WebServiceImportDialog<?> dialog;
	private final Window parent;

	public ShowImportDialogAction(
			final WebServiceImportDialog<?> dialog,
			final String menuLocation,
			final float menuGravity,
			final String menuLabel,
			final KeyStroke shortcut,
			final CyServiceRegistrar serviceRegistrar
	) {
		super(menuLabel);

		if (dialog == null)
			throw new NullPointerException("Dialog is null.");
		if (menuLocation == null)
			throw new NullPointerException("Menu Location is null.");
		if (menuLabel == null)
			throw new NullPointerException("Menu Label is null.");

		setPreferredMenu(menuLocation);
		setMenuGravity(menuGravity);

		if (shortcut != null)
			setAcceleratorKeyStroke(shortcut);
		
		this.parent = serviceRegistrar.getService(CySwingApplication.class).getJFrame();
		this.dialog = dialog;
	}
	
	/**
	 * Use this constructor to also make the action available as a tool bar button.
	 */
	public ShowImportDialogAction(
			final WebServiceImportDialog<?> dialog,
			final String menuLocation,
			final float menuGravity,
			final String menuLabel,
			final KeyStroke shortcut,
			final float toolbarGravity,
			final URL iconUrl,
			final String toolTip,
			final CyServiceRegistrar serviceRegistrar
	) {
		this(dialog, menuLocation, menuGravity, menuLabel, shortcut, serviceRegistrar);
		
		setPreferredMenu(menuLocation);
		setMenuGravity(menuGravity);
		
		if (shortcut != null)
			setAcceleratorKeyStroke(shortcut);
		
		inToolBar = true;
		setToolbarGravity(toolbarGravity);
		putValue(SHORT_DESCRIPTION, toolTip);
		
		if (iconUrl != null)
			putValue(LARGE_ICON_KEY, new ImageIcon(iconUrl));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		dialog.prepareForDisplay();
		dialog.setLocationRelativeTo(parent);
		dialog.setVisible(true);
	}
}
