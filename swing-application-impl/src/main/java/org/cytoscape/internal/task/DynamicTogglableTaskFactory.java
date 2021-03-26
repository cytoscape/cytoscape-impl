package org.cytoscape.internal.task;

import org.cytoscape.internal.model.RootNetworkManager;
import org.cytoscape.task.RootNetworkCollectionTaskFactory;
import org.cytoscape.work.Togglable;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
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

public class DynamicTogglableTaskFactory extends DynamicTaskFactory implements Togglable {
	
	public DynamicTogglableTaskFactory(RootNetworkCollectionTaskFactory factory, RootNetworkManager rootNetManager) {
		super(factory, rootNetManager);
	}
	
	@Override
	public boolean isOn() {
		if (factory instanceof RootNetworkCollectionTaskFactory)
			return ((RootNetworkCollectionTaskFactory) factory).isOn(rootNetManager.getSelectedRootNetworks());
		
		return false;
	}
}
