package org.cytoscape.internal.model;

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
 * This manager provides {@link CyRootNetwork} instances that are currently selected (or right-clicked) to other
 * classes in this module. Cytoscape does not keep a global reference to the current root-network 
 * (see {@link CyApplicationManager}), since a {@link CyRootNetwork} is just a "meta-network"
 * (or a "Collection" of sub-networks), and is not visualized by end users.
 */
public class RootNetworkManager {

	private CyRootNetwork currentRootNetwork;
	
	public CyRootNetwork getCurrentRootNetwork() {
		return currentRootNetwork;
	}
	
	public void setCurrentRootNetwork(CyRootNetwork currentRootNetwork) {
		this.currentRootNetwork = currentRootNetwork;
	}
}
