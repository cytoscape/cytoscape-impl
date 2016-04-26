package org.cytoscape.app.internal.manager;

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.cytoscape.app.internal.manager.App.AppStatus;

// This class represents a node in the app dependency graph, where each node corresponds to an app.
public class AppDependencyNode {
	
	private Set<AppDependencyNode> dependencies = new HashSet<>();
	private Set<AppDependencyNode> parents = new HashSet<>();
	
	private String currentAppName;
	private String currentAppVersion;
	
	
	public AppDependencyNode(String appName, String appVersion) {
		this.currentAppName = appName;
		this.currentAppVersion = appVersion;
	}
	
	public String getNodeAppName() {
		return currentAppName;
	}
	
	public String getNodeAppVersion() {
		return currentAppVersion;
	}
	
	public Set<AppDependencyNode> getDependencies() { return dependencies; }
	public Set<AppDependencyNode> getParents() { return parents; }
	
	
	public void addDependency(AppDependencyNode dependency) {
		dependencies.add(dependency);
	}
	
	public void addParent(AppDependencyNode parent) {
		parents.add(parent);
	}
	
	public List<String> getMissingDependencies(AppManager appManager) {
		
		// Hash current apps into slots
		Map<String, App> currentApps = new HashMap<>();
		
		for (App app : appManager.getApps()) {
			currentApps.put(app.getAppName() + " " + app.getVersion(), app);
		}
		
		Queue<AppDependencyNode> dependenciesToCheck = new LinkedList<>();
		
		for (AppDependencyNode node : dependencies) {
			dependenciesToCheck.add(node);	
		}
		
		List<String> missingDependencies = new LinkedList<>();
		
		// To check if a dependency is missing, check if it was hashed into the hash table from
		// a few lines above
		while (!dependenciesToCheck.isEmpty()) {
			AppDependencyNode current = dependenciesToCheck.remove();
			
			if (current.getDependencies().size() != 0) {
				for (AppDependencyNode node : current.getDependencies()) {
					dependenciesToCheck.add(node);
				}
			} else {
				// Leaf
				String keyName = current.getNodeAppName() + " " + current.getNodeAppVersion();
				
				if (currentApps.get(keyName) == null
						|| currentApps.get(keyName).getStatus() != AppStatus.INSTALLED) {

					missingDependencies.add(keyName);
				}
			}
		}
		
		return missingDependencies;
	}
}
