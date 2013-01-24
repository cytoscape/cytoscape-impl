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

import org.cytoscape.app.internal.net.UpdateManager;

/**
 * An event used by the {@link UpdateManager} to notify UpdatesChangedListeners that an update
 * has become available or installed,  and that the listeners (such as UI components) should 
 * update their data to reflect the change.
 */
public final class UpdatesChangedEvent {

	/** The {@link UpdateManager} that created the event. */
	private UpdateManager source;

	/**
	 * Create a new UpdatesChangedEvent.
	 * @param source The {@link UpdateManager} creating the event.
	 */
	public UpdatesChangedEvent(UpdateManager source) {
		this.source = source;
	}
	
	/**
	 * Find the source of the event.
	 * @return The {@link UpdateManager} that triggered the event.
	 */
	public UpdateManager getSource() {
		return source;
	}
}
