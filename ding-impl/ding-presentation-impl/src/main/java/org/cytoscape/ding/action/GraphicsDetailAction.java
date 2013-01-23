package org.cytoscape.ding.action;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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


import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Properties;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.DingGraphLOD;
import org.cytoscape.ding.impl.DingGraphLODAll;
import org.cytoscape.graph.render.stateful.GraphLOD;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.property.CyProperty;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.work.TaskManager;

public class GraphicsDetailAction extends AbstractCyAction {

	private final static long serialVersionUID = 1202323129387651L;

	private static String GraphicsDetails = "Graphics Details";

	protected static String SHOW = "Show";
	protected static String HIDE = "Hide";

	private final CyApplicationManager applicationManager;

	private final DingGraphLOD dingGraphLOD;
	private final DingGraphLODAll dingGraphLODAll;

	public GraphicsDetailAction(final CyApplicationManager applicationManager,
			final CyNetworkViewManager networkViewManager, DingGraphLOD dingGraphLOD, DingGraphLODAll dingGraphLODAll) {
		super(SHOW + " " + GraphicsDetails, applicationManager, "networkAndView", networkViewManager);

		setPreferredMenu("View");
		setMenuGravity(5.0f);
		setAcceleratorKeyStroke(KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit()
				.getMenuShortcutKeyMask() + InputEvent.SHIFT_MASK));
		this.applicationManager = applicationManager;
		this.dingGraphLOD = dingGraphLOD;
		this.dingGraphLODAll = dingGraphLODAll;
	}

	/**
	 * Toggles the Show/Hide state.
	 * 
	 * @param ev
	 *            Triggering event - not used.
	 */
	public void actionPerformed(ActionEvent ev) {

		final RenderingEngine<CyNetwork> engine = applicationManager.getCurrentRenderingEngine();

		if (engine instanceof DGraphView == false)
			return;

		final GraphLOD lod = ((DGraphView) engine).getGraphLOD();

		if (lod instanceof DingGraphLODAll) {
			((DGraphView) engine).setGraphLOD(dingGraphLOD);
		} else {
			((DGraphView) engine).setGraphLOD(dingGraphLODAll);

		}
		((CyNetworkView) engine.getViewModel()).updateView();
	}

	/**
	 * This dynamically sets the title of the menu based on the state of the
	 * graphics detail.
	 */
	public void menuSelected(MenuEvent me) {

		if (isDetailShown()) {
			putValue(Action.NAME, HIDE + " " + GraphicsDetails);
		} else {
			putValue(Action.NAME, SHOW + " " + GraphicsDetails);
		}
	}

	public boolean isDetailShown() {

		final RenderingEngine<CyNetwork> engine = applicationManager.getCurrentRenderingEngine();

		if (engine instanceof DGraphView == false)
			return false;

		final GraphLOD lod = ((DGraphView) engine).getGraphLOD();

		if (lod instanceof DingGraphLODAll)
			return true;
		else
			return false;
	}
}
