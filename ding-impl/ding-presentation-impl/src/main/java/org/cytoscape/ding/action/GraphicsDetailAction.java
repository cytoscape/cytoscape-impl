package org.cytoscape.ding.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.ShowGraphicsDetailsTaskFactory;
import org.cytoscape.ding.impl.DGraphView;
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

	private ShowGraphicsDetailsTaskFactory taskFactory;
	private final CyServiceRegistrar serviceRegistrar;

	public GraphicsDetailAction(
			float gravity,
			String preferredMenu,
			ShowGraphicsDetailsTaskFactory taskFactory,
			CyServiceRegistrar serviceRegistrar
	) {
		super(
				"Always Show Graphics Details",
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
		useCheckBoxMenuItem = true;
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

	@Override
	public void menuSelected(MenuEvent me) {
		updateEnableState();
		JCheckBoxMenuItem item = getThisItem();

		if (item != null) {
			CyNetworkView view = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetworkView();
			
			if (view instanceof DGraphView && isEnabled())
				item.setSelected(view.getVisualProperty(DVisualLexicon.NETWORK_FORCE_HIGH_DETAIL));
			else
				item.setSelected(false);
		}
	}
	
	@Override
	public void updateEnableState() {
		CyNetworkView view = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetworkView();
		setEnabled(taskFactory.isReady(view));
	}

	private JCheckBoxMenuItem getThisItem() {
		JMenu menu = serviceRegistrar.getService(CySwingApplication.class).getJMenu(preferredMenu);
		
		for (int i = 0; i < menu.getItemCount(); i++) {
			JMenuItem item = menu.getItem(i);
			
			if (item instanceof JCheckBoxMenuItem && item.getText().equals(getName()))
				return (JCheckBoxMenuItem) item;
		}
		
		return null;
	}
}
