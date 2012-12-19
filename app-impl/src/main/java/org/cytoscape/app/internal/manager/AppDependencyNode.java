package org.cytoscape.app.internal.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class AppDependencyNode {
	
	private Set<AppDependencyNode> dependencies = new HashSet<AppDependencyNode>();
	private Set<AppDependencyNode> parents = new HashSet<AppDependencyNode>();
	
	private String currentAppName;
	private String currentAppVersion;
	
	private App currentApp;
	
	public AppDependencyNode(String appName, String appVersion) {
		this.currentAppName = appName;
		this.currentAppVersion = appVersion;
	}
	
	public void setCurrentApp(App app) {
		this.currentApp = app;
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
//			currentApps.put(app.getAppName() + " " + app.get)
		}
		
		Queue<AppDependencyNode> dependenciesToCheck = new LinkedList<AppDependencyNode>();
		
		for (AppDependencyNode node : dependencies) {
			dependenciesToCheck.add(node);	
		}
		
		while (!dependenciesToCheck.isEmpty()) {
			AppDependencyNode current = dependenciesToCheck.remove();
			
			if (current.getDependencies().size() != 0) {
				for (AppDependencyNode node : current.getDependencies()) {
					dependenciesToCheck.add(node);
				}
			} else {
				// Leaf
//				AppManager appManager;
//				if (appManager.getApps())
			}
		}
		
		return null;
	}
}
