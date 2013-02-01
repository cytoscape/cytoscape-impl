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

/**
 * A listener used to detect when an app is added, removed, or changed. This is useful for UI components
 * to update their data when the list of apps has changed.
 */
public interface AppsChangedListener {
	
	/**
	 * Notifies the listener that apps have been updated, or added/removed from the list of available apps.
	 * @param event The {@link AppsChangedEvent} containing information about the app change event.
	 */
	public void appsChanged(AppsChangedEvent event);
}
