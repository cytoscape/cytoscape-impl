package org.cytoscape.view.vizmap.gui.internal;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.event.MenuEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.view.vizmap.gui.util.PropertySheetUtil;

public final class SetViewModeAction extends AbstractCyAction {
	
	private static final long serialVersionUID = -832910068413870738L;

	// Local property changed event.
	public static final String VIEW_MODE_CHANGED = "VIEW_MODE_CHANGED";

	private final static String BASIC = "Show All Visual Properties";
	private final static String ALL = "Hide Advanced Visual Properties";
	
	public SetViewModeAction() {
		super(BASIC);

		PropertySheetUtil.setMode(false);

		setPreferredMenu("View");
		setMenuGravity(25.0f);
	}

	/**
	 * Toggles the Show/Hide state.
	 * 
	 * @param ev
	 *            Triggering event - not used.
	 */
	public void actionPerformed(ActionEvent ev) {
		firePropertyChange(VIEW_MODE_CHANGED, null, null);
	}

	@Override
	public void menuSelected(MenuEvent me) {
		if (PropertySheetUtil.isAdvancedMode()) {
			putValue(Action.NAME, ALL);
			PropertySheetUtil.setMode(false);
		} else {
			putValue(Action.NAME, BASIC);
			PropertySheetUtil.setMode(true);
		}
	}
}
