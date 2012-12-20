package org.cytoscape.app.internal.manager;

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
	
	private Set<AppDependencyNode> dependencies = new HashSet<AppDependencyNode>();
	private Set<AppDependencyNode> parents = new HashSet<AppDependencyNode>();
	
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
		Map<String, App> currentApps = new HashMap<String, App>();
		
		for (App app : appManager.getApps()) {
			currentApps.put(app.getAppName() + " " + app.getVersion(), app);
		}
		
		Queue<AppDependencyNode> dependenciesToCheck = new LinkedList<AppDependencyNode>();
		
		for (AppDependencyNode node : dependencies) {
			dependenciesToCheck.add(node);	
		}
		
		List<String> missingDependencies = new LinkedList<String>();
		
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
