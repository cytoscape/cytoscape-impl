package org.cytoscape.app.internal.event;

/*
 * #%L
 * Cytoscape App Impl (app-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
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

import org.cytoscape.app.internal.manager.AppManager;

/**
 * An event used by the {@link AppManager} to notify AppsChangedListeners that an app has 
 * been added, removed, or changed and that the listeners (such as UI components) should 
 * update their data to reflect the change.
 */
public final class AppsChangedEvent {

	/** The {@link AppManager} that created the event. */
	private AppManager source;

	/**
	 * Create a new AppsChangedEvent.
	 * @param source The {@link AppManager} creating the event.
	 */
	public AppsChangedEvent(AppManager source) {
		this.source = source;
	}
	
	/**
	 * Find the source of the event.
	 * @return The {@link AppManager} that triggered the event.
	 */
	public AppManager getSource() {
		return source;
	}
}
