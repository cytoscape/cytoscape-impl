package org.cytoscape.app.internal.net;

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
