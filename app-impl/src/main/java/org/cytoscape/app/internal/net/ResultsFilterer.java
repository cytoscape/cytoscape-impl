package org.cytoscape.app.internal.net;

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

import java.util.HashSet;
import java.util.Set;

/**
 * This class is capable of filtering a set of {@link WebApp} objects to create a subset
 * containing only elements that match a given search.
 */
public class ResultsFilterer {
	
	/**
	 * Given a set of {@link WebApp} objects, return a subset where app in the subset
	 * contains the given substring, such as in its name or description.
	 * @param text The substring used to perform matching, ignoring case.
	 * @param webApps The set of {@link WebApp} objects to find matches with.
	 * @return A subset of the given set of {@link WebApp} objects containing only
	 * apps that contain the given substring, such as in its name or description.
	 */
	public Set<WebApp> findMatches(String text, Set<WebApp> webApps) {
		Set<WebApp> result = new HashSet<WebApp>();
		
		for (WebApp webApp : webApps) {
			if (matches(text, webApp)) {
				result.add(webApp);
			}
		}
		
		return result;
	}
	
	// Return true if the app matches the filter text
	
	/**
	 * Check if a given app contains the given substring in one of its fields such as
	 * name and description. The check is case-insensitive.
	 * @param text The substring used to find matches with
	 * @param webApp the {@link WebApp} object used to check for a substring
	 */
	public boolean matches(String text, WebApp webApp) {
		String lowerCaseText = text.toLowerCase();
		
		if (webApp.getFullName().toLowerCase().indexOf(lowerCaseText) != -1) {
			return true;
		} else if (webApp.getDescription().toLowerCase().indexOf(lowerCaseText) != -1) {
			return true;
		} 
		
		return false;
	}
}
