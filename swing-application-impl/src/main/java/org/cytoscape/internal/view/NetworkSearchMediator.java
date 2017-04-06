package org.cytoscape.internal.view;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.internal.view.NetworkSearchPanel.NetworkSearchTaskFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.swing.DialogTaskManager;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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

public class NetworkSearchMediator {

	private final Map<String, NetworkSearchTaskFactory> taskFactories = new HashMap<>();
	
	private final NetworkSearchPanel networkSearchPanel;
	private final CyServiceRegistrar serviceRegistrar;
	
	public NetworkSearchMediator(NetworkSearchPanel networkSearchPanel, CyServiceRegistrar serviceRegistrar) {
		this.networkSearchPanel = networkSearchPanel;
		this.serviceRegistrar = serviceRegistrar;
		
		networkSearchPanel.getSearchTextField().addActionListener(evt -> {
			runSearch();
		});
		networkSearchPanel.getSearchButton().addActionListener(evt -> {
			runSearch();
		});
	}

	public NetworkSearchPanel getNetworkSearchPanel() {
		return networkSearchPanel;
	}
	
	public void addNetworkSearchTaskFactory(NetworkSearchTaskFactory factory, Map<?, ?> properties) {
		if (factory.getId() != null && !factory.getId().trim().isEmpty()) {
			taskFactories.put(factory.getId(), factory);
			updateSearchPanel();
		}
	}
	
	public void removeNetworkSearchTaskFactory(NetworkSearchTaskFactory factory, Map<?, ?> properties) {
		if (factory.getId() != null && taskFactories.remove(factory.getId(), factory))
			updateSearchPanel();
	}
	
	private void updateSearchPanel() {
		networkSearchPanel.update(taskFactories.values());
	}
	
	private void runSearch() {
		NetworkSearchTaskFactory tf = networkSearchPanel.getSelectedProvider();
		
		if (tf != null && tf.isReady())
			serviceRegistrar.getService(DialogTaskManager.class).execute(tf.createTaskIterator());
	}
}
