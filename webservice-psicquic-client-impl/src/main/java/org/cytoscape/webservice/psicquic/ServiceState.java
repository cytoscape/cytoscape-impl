package org.cytoscape.webservice.psicquic;

/*
 * #%L
 * Cytoscape PSIQUIC Web Service Impl (webservice-psicquic-client-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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
