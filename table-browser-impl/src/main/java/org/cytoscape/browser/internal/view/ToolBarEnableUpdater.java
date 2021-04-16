package org.cytoscape.browser.internal.view;

import static org.cytoscape.browser.internal.util.ViewUtil.invokeOnEDT;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.events.SetCurrentTableEvent;
import org.cytoscape.application.events.SetCurrentTableListener;
import org.cytoscape.browser.internal.action.TaskFactoryTunableAction;
import org.cytoscape.browser.internal.task.DynamicTableTaskFactory;
import org.cytoscape.browser.internal.view.AbstractTableBrowser.TableToolBar;
import org.cytoscape.event.DebounceTimer;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.NetworkDestroyedEvent;
import org.cytoscape.model.events.NetworkDestroyedListener;
import org.cytoscape.model.events.RowsCreatedEvent;
import org.cytoscape.model.events.RowsCreatedListener;
import org.cytoscape.model.events.RowsDeletedEvent;
import org.cytoscape.model.events.RowsDeletedListener;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.model.events.TableAddedEvent;
import org.cytoscape.model.events.TableAddedListener;
import org.cytoscape.model.events.TableDeletedEvent;
import org.cytoscape.model.events.TableDeletedListener;
import org.cytoscape.model.events.TablePrivacyChangedEvent;
import org.cytoscape.model.events.TablePrivacyChangedListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeLoadedEvent;
import org.cytoscape.session.events.SessionAboutToBeLoadedListener;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.session.events.SessionSaveCancelledEvent;
import org.cytoscape.session.events.SessionSaveCancelledListener;
import org.cytoscape.session.events.SessionSavedEvent;
import org.cytoscape.session.events.SessionSavedListener;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.model.events.NetworkViewDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewDestroyedListener;
import org.cytoscape.view.model.events.TableViewChangedEvent;
import org.cytoscape.view.model.events.TableViewChangedListener;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
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

/**
 * A utility class that listens for various events and then updates the enable
 * state for the toolbar icons. Menus do this check every time that a menu is
 * selected, but since toolbars are always visible, we need to listen for the
 * actual events. This is less than ideal.
 */
public class ToolBarEnableUpdater implements SessionAboutToBeLoadedListener, SessionLoadedListener,
		SessionAboutToBeSavedListener, SessionSavedListener, SessionSaveCancelledListener, NetworkAddedListener,
		NetworkDestroyedListener, NetworkViewAddedListener, NetworkViewDestroyedListener, SetCurrentNetworkListener,
		SetCurrentNetworkViewListener, SetCurrentTableListener, TableAddedListener, TableDeletedListener,
		TablePrivacyChangedListener, ColumnCreatedListener, ColumnDeletedListener, RowsCreatedListener,
		RowsDeletedListener, RowsSetListener, TableViewChangedListener {

	private final DebounceTimer debounceTimer = new DebounceTimer(100);
	
	private boolean loadingSession;
	
	private final Set<TableToolBar> toolbars = new HashSet<>();
	private final CyServiceRegistrar serviceRegistrar;

	public ToolBarEnableUpdater(Collection<TableToolBar> toolbars, CyServiceRegistrar serviceRegistrar) {
		this.toolbars.addAll(toolbars);
		this.serviceRegistrar = serviceRegistrar;
	}
	
	@Override
	public void handleEvent(SessionAboutToBeLoadedEvent e) {
		updateToolbar();
		loadingSession = true;
	}
	
	@Override
	public void handleEvent(SessionLoadedEvent e) {
		loadingSession = false;
		updateToolbar();
	}
	
	@Override
	public void handleEvent(SessionAboutToBeSavedEvent e) {
		updateToolbar();
	}
	
	@Override
	public void handleEvent(SessionSavedEvent e) {
		updateToolbar();
	}
	
	@Override
	public void handleEvent(SessionSaveCancelledEvent e) {
		updateToolbar();
	}

	@Override
	public void handleEvent(SetCurrentNetworkEvent e) {
		if (!loadingSession)
			updateToolbar();
	}

	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		if (!loadingSession)
			updateToolbar();
	}

	@Override
	public void handleEvent(NetworkAddedEvent e) {
		if (!loadingSession)
			updateToolbar();
	}

	@Override
	public void handleEvent(NetworkViewAddedEvent e) {
		if (!loadingSession)
			updateToolbar();
	}

	@Override
	public void handleEvent(NetworkDestroyedEvent e) {
		if (!loadingSession)
			updateToolbar();
	}

	@Override
	public void handleEvent(NetworkViewDestroyedEvent e) {
		if (!loadingSession)
			updateToolbar();
	}
	
	@Override
	public void handleEvent(SetCurrentTableEvent e) {
		if (!loadingSession)
			updateToolbar();
	}
	
	@Override
	public void handleEvent(ColumnDeletedEvent e) {
		if (!loadingSession && e.getSource().equals(getCurrentTable()))
			updateToolbar();
	}

	@Override
	public void handleEvent(ColumnCreatedEvent e) {
		if (!loadingSession && e.getSource().equals(getCurrentTable()))
			updateToolbar();
	}

	@Override
	public void handleEvent(TableAddedEvent e) {
		if (!loadingSession && e.getTable().equals(getCurrentTable()))
			updateToolbar();
	}

	@Override
	public void handleEvent(TableDeletedEvent e) {
		if (!loadingSession)
			updateToolbar();
	}
	
	@Override
	public void handleEvent(TablePrivacyChangedEvent e) {
		if (!loadingSession && e.getSource().equals(getCurrentTable()))
			updateToolbar();
	}

	@Override
	public void handleEvent(RowsCreatedEvent e) {
		if (!loadingSession && e.getSource().equals(getCurrentTable()))
			updateToolbar();
	}

	@Override
	public void handleEvent(RowsDeletedEvent e) {
		if (!loadingSession && e.getSource().equals(getCurrentTable()))
			updateToolbar();
	}

	@Override
	public void handleEvent(RowsSetEvent e) {
		if (!loadingSession && e.getSource().equals(getCurrentTable()))
			updateToolbar();
	}

	@Override
	public void handleEvent(TableViewChangedEvent<?> e) {
		if (!loadingSession && e.getSource().getModel().equals(getCurrentTable()))
			updateToolbar();
	}

	private void updateToolbar() {
		debounceTimer.debounce(() -> {
			invokeOnEDT(() -> {
				for (var tb : toolbars) {
					if (!tb.isShowing())
						continue;
					
					var table = tb.getCurrentTable();
					
					for (var action : tb.getAllToolBarActions()) {
						// Check whether we should also show or hide the component, based on the current CyTable
						if (action instanceof TaskFactoryTunableAction) {
							var tf = ((TaskFactoryTunableAction) action).getTaskFactory();
							
							if (tf instanceof DynamicTableTaskFactory) {
								var comp = tb.getComponent(action);
								
								var ready = ((DynamicTableTaskFactory) tf).isReady(table);
								comp.setEnabled(ready);
								
								var applicable = ((DynamicTableTaskFactory) tf).isApplicable(table);
								comp.setVisible(applicable);
							}
						} else {
							// This should enable or disable the toolbar button
							action.updateEnableState();
						}
					}
				}
			});
		});
	}
	
	private CyTable getCurrentTable() {
		return serviceRegistrar.getService(CyApplicationManager.class).getCurrentTable();
	}
}
