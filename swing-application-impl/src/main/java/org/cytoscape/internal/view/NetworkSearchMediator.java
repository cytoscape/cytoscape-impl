package org.cytoscape.internal.view;

import static org.cytoscape.internal.util.ViewUtil.invokeOnEDT;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.cytoscape.application.swing.search.AbstractNetworkSearchTaskFactory;
import org.cytoscape.application.swing.search.NetworkSearchTaskFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.PanelTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private final Map<NetworkSearchTaskFactory, JComponent> optionsComponents = new HashMap<>();
	
	private final NetworkSearchPanel networkSearchPanel;
	private final CyServiceRegistrar serviceRegistrar;
	
	private final Object lock = new Object();
	
	private static Logger logger = LoggerFactory.getLogger(NetworkSearchMediator.class);
	
	public NetworkSearchMediator(NetworkSearchPanel networkSearchPanel, CyServiceRegistrar serviceRegistrar) {
		this.networkSearchPanel = networkSearchPanel;
		this.serviceRegistrar = serviceRegistrar;
		
		addListeners();
	}

	public NetworkSearchPanel getNetworkSearchPanel() {
		return networkSearchPanel;
	}
	
	public void addNetworkSearchTaskFactory(NetworkSearchTaskFactory factory, Map<?, ?> properties) {
		if (factory.getId() != null && !factory.getId().trim().isEmpty()) {
			try {
				synchronized (lock) {
					if (factory != null) {
						JComponent comp = factory.getOptionsComponent();
						
						if (comp == null) {
							PanelTaskManager taskManager = serviceRegistrar.getService(PanelTaskManager.class);
							comp = taskManager.getConfiguration(factory, factory);
						}
						
						if (comp != null)
							optionsComponents.put(factory, comp);
					}
				
					taskFactories.put(factory.getId(), factory);
				}
				
				updateSearchPanel();
			} catch (Exception e) {
				logger.error("Cannot install Network Search Provider: " + factory, e);
			}
		}
	}
	
	public void removeNetworkSearchTaskFactory(NetworkSearchTaskFactory factory, Map<?, ?> properties) {
		boolean removed = false;
		
		synchronized (lock) {
			optionsComponents.remove(factory);
			
			if (factory.getId() != null)
				taskFactories.remove(factory.getId(), factory);
		}
		
		if (removed)
			updateSearchPanel();
	}
	
	/**
	 * Add listeners to UI components.
	 */
	private void addListeners() {
		networkSearchPanel.addPropertyChangeListener("selectedProvider", evt -> {
			NetworkSearchTaskFactory tf = (NetworkSearchTaskFactory) evt.getNewValue();
			
			if (tf != null) {
				updateSelectedProvider();
				updateOptionsButton(optionsComponents.get(tf));
			}
			
			networkSearchPanel.updateProvidersButton();
			networkSearchPanel.updateSearchEnabled();
		});
		networkSearchPanel.getSearchTextField().getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateSelectedProvider();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateSelectedProvider();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				// Nothing to do here...
			}
		});
		networkSearchPanel.getSearchTextField().addActionListener(evt -> {
			runSearch();
		});
		networkSearchPanel.getOptionsButton().addActionListener(evt -> {
			if (networkSearchPanel.getSelectedProvider() != null)
				networkSearchPanel.showOptionsPopup(optionsComponents.get(networkSearchPanel.getSelectedProvider()));
		});
		networkSearchPanel.getSearchButton().addActionListener(evt -> {
			runSearch();
		});
	}
	
	private void updateSelectedProvider() {
		NetworkSearchTaskFactory tf = networkSearchPanel.getSelectedProvider();
		
		if (tf instanceof AbstractNetworkSearchTaskFactory) {
			// TODO only if the TaskFactory did not provide its own query component!
			((AbstractNetworkSearchTaskFactory) tf).setQuery(networkSearchPanel.getSearchTextField().getText().trim());
			
			invokeOnEDT(() -> {
				networkSearchPanel.updateSearchButton();
			});
		}
	}
	
	private void updateSearchPanel() {
		invokeOnEDT(() -> {
			networkSearchPanel.update(new HashSet<>(taskFactories.values()));
		});
	}
	
	void updateOptionsButton(JComponent optionsComponent) {
		invokeOnEDT(() -> {
			networkSearchPanel.getOptionsButton().setVisible(optionsComponent != null);
		});
	}
	
	private void runSearch() {
		NetworkSearchTaskFactory tf = networkSearchPanel.getSelectedProvider();
		
		if (tf != null && tf.isReady()) {
			DialogTaskManager taskManager = serviceRegistrar.getService(DialogTaskManager.class);
			taskManager.execute(tf.createTaskIterator());
		}
	}
}
