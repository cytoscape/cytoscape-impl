package org.cytoscape.ding.action;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.ding.ShowGraphicsDetailsTaskFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.swing.DialogTaskManager;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

	private Long lastViewSuid;
	private long lastTime = -1;
	
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
	public void actionPerformed(ActionEvent evt) {
		var view = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetworkView();
		var currentTime = System.currentTimeMillis();
		
		if (taskFactory.isReady(view)) {
			var source = evt.getSource();
			
			// There is a bug in Java 9+ where the ActionListener is called twice when using accelerator key
			// on JCheckBoxMenuItem: https://bugs.openjdk.java.net/browse/JDK-8208712
			// 
			// Because of that, the second call reverts the "Graphics Details" to the original boolean value right away.
			// The workaround here is to first check if we are on macOS+Aqua and the source is a JCheckBoxMenuItem.
			// If so, we also check if the last call for the same View happened after a very short time threshold,
			// to make sure it's really another user action and not the second "buggy" call.
			// See: https://cytoscape.atlassian.net/browse/CYTOSCAPE-12637
			if (LookAndFeelUtil.isAquaLAF() && source instanceof JCheckBoxMenuItem
					&& view.getSUID().equals(lastViewSuid) && currentTime - lastTime < 32)
				return;
			
			if (LookAndFeelUtil.isAquaLAF() && source instanceof JCheckBoxMenuItem) {
				lastTime = currentTime;
				lastViewSuid = view.getSUID();
			}
			
			serviceRegistrar.getService(DialogTaskManager.class).execute(taskFactory.createTaskIterator(view));
		}
	}

	@Override
	public void menuSelected(MenuEvent me) {
		updateEnableState();
		var view = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetworkView();
		
		if (view != null && isEnabled())
			putValue(SELECTED_KEY, Boolean.TRUE.equals(view.getVisualProperty(DVisualLexicon.NETWORK_FORCE_HIGH_DETAIL)));
		else
			putValue(SELECTED_KEY, false);
	}
	
	@Override
	public void updateEnableState() {
		var view = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetworkView();
		setEnabled(taskFactory.isReady(view));
	}
}
