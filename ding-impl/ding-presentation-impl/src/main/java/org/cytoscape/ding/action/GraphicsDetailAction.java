package org.cytoscape.ding.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.ding.ShowGraphicsDetailsTaskFactory;
import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.DingGraphLODAll;
import org.cytoscape.ding.impl.DingRenderer;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.swing.DialogTaskManager;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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
public class GraphicsDetailAction extends AbstractCyAction {

	private static String GraphicsDetails = "Graphics Details";

	protected static String SHOW = "Show";
	protected static String HIDE = "Hide";

	private ShowGraphicsDetailsTaskFactory taskFactory;
	private final CyServiceRegistrar serviceRegistrar;

	public GraphicsDetailAction(
			float gravity,
			String preferredMenu,
			ShowGraphicsDetailsTaskFactory taskFactory,
			CyServiceRegistrar serviceRegistrar
	) {
		super(
				SHOW + " " + GraphicsDetails,
				serviceRegistrar.getService(CyApplicationManager.class),
				"networkAndView",
				serviceRegistrar.getService(CyNetworkViewManager.class)
		);

		this.taskFactory = taskFactory;
		this.serviceRegistrar = serviceRegistrar;
		
		if (preferredMenu != null) {
			setPreferredMenu(preferredMenu);
			setAcceleratorKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_D,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + InputEvent.SHIFT_MASK));
		}
		
		setMenuGravity(gravity);
	}
	
	/**
	 * Toggles the Show/Hide state.
	 * @param ev Triggering event - not used.
	 */
	@Override
	public void actionPerformed(ActionEvent ev) {
		CyNetworkView view = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetworkView();
		
		if (taskFactory.isReady(view))
			serviceRegistrar.getService(DialogTaskManager.class).execute(taskFactory.createTaskIterator(view));
	}

	/**
	 * This dynamically sets the title of the menu based on the state of the graphics detail.
	 */
	@Override
	public void menuSelected(MenuEvent me) {
		updateEnableState();
	}
	
	@Override
	public void updateEnableState() {
		CyNetworkView view = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetworkView();
		if (isDetailShown(view))
			putValue(Action.NAME, HIDE + " " + GraphicsDetails);
		else
			putValue(Action.NAME, SHOW + " " + GraphicsDetails);
		
		setName(getValue(Action.NAME).toString());
		setEnabled(taskFactory.isReady(view));
	}

	private boolean isDetailShown(CyNetworkView view) {
		DRenderingEngine renderingEngine = serviceRegistrar.getService(DingRenderer.class).getRenderingEngine(view);
		if (renderingEngine != null)
			return renderingEngine.getGraphLOD() instanceof DingGraphLODAll;
		return false;
	}
}
