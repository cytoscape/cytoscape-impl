package org.cytoscape.view.vizmap.gui.internal.action;

import java.awt.event.ActionEvent;

import javax.swing.event.PopupMenuEvent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.task.visualize.ApplyVisualStyleTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.work.swing.DialogTaskManager;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2020 The Cytoscape Consortium
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
public class ApplyVisualStyleAction extends AbstractCyAction {
	
	public static final String NAME = "Apply Style...";
	
	private final ServicesUtil servicesUtil;
	
	public ApplyVisualStyleAction(ServicesUtil servicesUtil) {
		super(NAME);
		this.servicesUtil = servicesUtil;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		var views = servicesUtil.get(CyApplicationManager.class).getSelectedNetworkViews();

		if (!views.isEmpty()) {
			var factory = servicesUtil.get(ApplyVisualStyleTaskFactory.class);
			var taskManager = servicesUtil.get(DialogTaskManager.class);

			taskManager.execute(factory.createTaskIterator(views));
		}
	}
	
	@Override
	public void updateEnableState() {
		var views = servicesUtil.get(CyApplicationManager.class).getSelectedNetworkViews();
		var factory = servicesUtil.get(ApplyVisualStyleTaskFactory.class);
		setEnabled(factory.isReady(views));
	}
	
	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		updateEnableState();
	}
}
