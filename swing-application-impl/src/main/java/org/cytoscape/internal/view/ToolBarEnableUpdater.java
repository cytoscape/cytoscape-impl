/*
 Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.internal.view;

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
				for (CyAction action : toolbar.getAllToolBarActions())
					action.updateEnableState();
			}
		});
	}

}
