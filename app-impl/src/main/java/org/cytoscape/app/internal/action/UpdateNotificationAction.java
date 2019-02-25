package org.cytoscape.app.internal.action;

import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.UIManager;

import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.net.UpdateManager;
import org.cytoscape.app.internal.ui.AppManagerMediator;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;

/*
 * #%L
 * Cytoscape App Impl (app-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2019 The Cytoscape Consortium
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

@SuppressWarnings("serial")
public class UpdateNotificationAction extends AbstractCyAction {

	private static float ICON_FONT_SIZE = 24f;
	private static int ICON_SIZE = 32;
	
	private final TextIcon icon;
	
	private final UpdateManager updateManager;
	private final AppManagerMediator appManagerMediator;

	public UpdateNotificationAction(
			AppManager appManager,
			UpdateManager updateManager,
			AppManagerMediator appManagerMediator,
			CyServiceRegistrar serviceRegistrar
	) {
		super("App Updates");
		this.updateManager = updateManager;
		this.appManagerMediator = appManagerMediator;
		
		Font iconFont = serviceRegistrar.getService(IconManager.class).getIconFont(ICON_FONT_SIZE);
		icon = new TextIcon(IconManager.ICON_BELL, iconFont, UIManager.getColor("CyColor.primary(0)"),
				ICON_SIZE, ICON_SIZE);
		
		putValue(LARGE_ICON_KEY, icon);
		putValue(SHORT_DESCRIPTION, "App Updates");
		setIsInMenuBar(false);
		setIsInToolBar(true);
		setToolbarGravity(Float.MAX_VALUE);
		
		appManager.addAppListener(evt -> updateEnableState(true));
		updateManager.addUpdatesChangedListener(evt -> updateEnableState(false));
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		appManagerMediator.showAppManager(true, evt);
	}

	@Override
	public void updateEnableState() {System.out.println(">>> updateEnableState()...");
		final int total = updateManager.getUpdateCount();
		setEnabled(total > 0);
		
		final String text;
		
		if (total > 0)
			text = total + " update" + (total > 1 ? "s" : "") + " available!";
		else
			text = "All your apps are up-to-date.";
			
		putValue(LONG_DESCRIPTION, text);
	}
	
	public void updateEnableState(boolean checkForUpdates) {
		if (checkForUpdates)
			updateManager.checkForUpdates();
		
		updateEnableState();
	}
}
