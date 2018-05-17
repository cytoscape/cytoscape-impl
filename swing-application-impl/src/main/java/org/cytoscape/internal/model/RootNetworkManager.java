package org.cytoscape.internal.model;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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
 * This manager provides {@link CyRootNetwork} instances that are currently selected to other
 * classes in this module. Cytoscape does not keep a global reference to the current or selected root-networks 
 * (see {@link CyApplicationManager}), since a {@link CyRootNetwork} is just a "meta-network"
 * (or a "Collection" of sub-networks), and is not visualized by end users.
 */
public class RootNetworkManager {

	private final Collection<CyRootNetwork> selectedRootNetworks = new LinkedHashSet<>();
	
	public Collection<CyRootNetwork> getSelectedRootNetworks() {
		return new LinkedHashSet<>(selectedRootNetworks);
	}
	
	public void setSelectedRootNetworks(Collection<CyRootNetwork> rootNetworks) {
		selectedRootNetworks.clear();
		
		if (rootNetworks != null && !rootNetworks.isEmpty())
			selectedRootNetworks.addAll(rootNetworks);
	}
}
