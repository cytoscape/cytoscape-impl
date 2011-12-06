package org.cytoscape.webservice.psicquic;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class ServiceState {
	
	private final RegistryManager registryManager;
	
	private final SortedSet<String> services;
	private final Map<String, Integer> lastCounts;
	
	private final Map<String, String> uri2name;
	
	
	public ServiceState(final RegistryManager registryManager) {
		this.registryManager = registryManager;
		
		services = new TreeSet<String>(registryManager.getActiveServices().keySet());
		services.addAll(registryManager.getInactiveServices().keySet());
		
		uri2name = new HashMap<String, String>();
		
		final Set<String> active = registryManager.getActiveServices().keySet();
		for(String serviceName: active)
			uri2name.put(registryManager.getActiveServices().get(serviceName), serviceName);
		
		lastCounts = new HashMap<String, Integer>();	
	}

	
	public Collection<String> getServiceNames() {
		return services;
	}
	
	public int getRecentResultCount(final String serviceName) {
		Integer count = lastCounts.get(serviceName);
		
		if(count != null)
			return count;
		else
			return 0;
	}
	
	public void setRecentResultCount(final String serviceName, Integer count) {
		lastCounts.put(serviceName, count);
	}
	
	public String getName(String uriString) {
		return this.uri2name.get(uriString);
	}
}
