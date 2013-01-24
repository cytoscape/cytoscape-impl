package org.cytoscape.internal.view;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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

import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.NetworkDestroyedEvent;
import org.cytoscape.model.events.NetworkDestroyedListener;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.model.events.NetworkViewDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewDestroyedListener;

import javax.swing.SwingUtilities;

/**
 * A utility class that listens for various events and then updates the enable
 * state for the toolbar icons. Menus do this check every time that a menu is
 * selected, but since toolbars are always visible, we need to listen for the
 * actual events. This is less than ideal.
 */
public class ToolBarEnableUpdater implements NetworkAddedListener, NetworkDestroyedListener,
		NetworkViewAddedListener, NetworkViewDestroyedListener, SetCurrentNetworkListener,
		SetCurrentNetworkViewListener, RowsSetListener {

	private final CytoscapeToolBar toolbar;

	public ToolBarEnableUpdater(final CytoscapeToolBar toolbar) {
		this.toolbar = toolbar;
	}

	@Override
	public void handleEvent(SetCurrentNetworkEvent e) {
		updateToolbar();
	}

	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		updateToolbar();
	}

	@Override
	public void handleEvent(NetworkAddedEvent e) {
		updateToolbar();
	}

	@Override
	public void handleEvent(NetworkViewAddedEvent e) {
		updateToolbar();
	}

	@Override
	public void handleEvent(NetworkDestroyedEvent e) {
		updateToolbar();
	}

	@Override
	public void handleEvent(NetworkViewDestroyedEvent e) {
		updateToolbar();
	}

	/**
	 * This is mainly for listening to node/edge selection events.
	 */
	@Override
	public void handleEvent(RowsSetEvent e) {
		updateToolbar();
	}

	private void updateToolbar() {
		SwingUtilities.invokeLater( new Runnable() {
			public void run() {
				for (final CyAction action : toolbar.getAllToolBarActions())
					action.updateEnableState();
			}
		});
	}

}
