package org.cytoscape.internal.view;

import static org.cytoscape.application.swing.search.NetworkSearchTaskFactory.QUERY_PROPERTY;
import static org.cytoscape.internal.util.ViewUtil.invokeOnEDT;
import static org.cytoscape.internal.util.ViewUtil.makeSmall;
import static org.cytoscape.internal.util.ViewUtil.recursiveDo;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.cytoscape.app.event.AppsFinishedStartingEvent;
import org.cytoscape.app.event.AppsFinishedStartingListener;
import org.cytoscape.application.swing.search.AbstractNetworkSearchTaskFactory;
import org.cytoscape.application.swing.search.NetworkSearchTaskFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskObserver;
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

public class NetworkSearchMediator implements AppsFinishedStartingListener {

	private final Map<String, NetworkSearchTaskFactory> taskFactories = new HashMap<>();
	private final Map<NetworkSearchTaskFactory, JComponent> optionsComponents = new HashMap<>();
	private final Map<NetworkSearchTaskFactory, JComponent> queryComponents = new HashMap<>();
	
	private final NetworkSearchBar networkSearchBar;
	private final CyServiceRegistrar serviceRegistrar;
	
	private boolean appsFinishedStarting;
	
	private final Object lock = new Object();
	
	private static Logger logger = LoggerFactory.getLogger("org.cytoscape.application.userlog");
	
	public NetworkSearchMediator(NetworkSearchBar networkSearchBar, CyServiceRegistrar serviceRegistrar) {
		this.networkSearchBar = networkSearchBar;
		this.serviceRegistrar = serviceRegistrar;
		
		addListeners();
	}

	public NetworkSearchBar getNetworkSearchBar() {
		return networkSearchBar;
	}
	
	public void addNetworkSearchTaskFactory(NetworkSearchTaskFactory factory, Map<?, ?> properties) {
		if (factory.getId() != null && !factory.getId().trim().isEmpty()) {
			try {
				synchronized (lock) {
					if (factory != null) {
						JComponent qc = factory.getQueryComponent();
						
						if (qc != null) {
							queryComponents.put(factory, qc);
							qc.addPropertyChangeListener(QUERY_PROPERTY, new QueryChangeListener(factory));
						}
						
						JComponent oc = factory.getOptionsComponent();
						
						if (oc == null) {
							PanelTaskManager taskManager = serviceRegistrar.getService(PanelTaskManager.class);
							oc = taskManager.getConfiguration(factory, factory);
							
							if (oc != null) {
								// Make the components smaller and the background white
								recursiveDo(oc, c -> makeSmall(c));
								recursiveDo(oc, c -> {
									if (c instanceof JPanel)
										((JPanel) c).setBackground(UIManager.getColor("Table.background"));
								});
							}
						} else {
							oc.addPropertyChangeListener(QUERY_PROPERTY, new QueryChangeListener(factory));
							// TODO How do we detect changes to tunable fields???
						}
						
						if (oc != null)
							optionsComponents.put(factory, oc);
					}
				
					taskFactories.put(factory.getId(), factory);
				}
				
				// Save this flag to a local scope variable first, because it may change before the
				// EDT below starts, which would cause the selection of the incorrect default provider
				final boolean appsFinishedStarting = this.appsFinishedStarting;
				
				invokeOnEDT(() -> {
					updateSearchPanel();
					
					// Also select the new provider,
					// so the user knows it has been installed correctly and is now available
					if (factory != null && appsFinishedStarting)
						networkSearchBar.setSelectedProvider(factory);
				});
			} catch (Exception e) {
				logger.error("Cannot install Network Search Provider: " + factory, e);
			}
		}
	}
	
	public void removeNetworkSearchTaskFactory(NetworkSearchTaskFactory factory, Map<?, ?> properties) {
		boolean removed = false;
		
		synchronized (lock) {
			queryComponents.remove(factory);
			optionsComponents.remove(factory);
			
			if (factory.getId() != null)
				removed = taskFactories.remove(factory.getId(), factory);
		}
		
		if (removed)
			updateSearchPanel();
	}
	
	@Override
	public void handleEvent(AppsFinishedStartingEvent evt) {
		appsFinishedStarting = true;
	}
	
	/**
	 * Add listeners to UI components.
	 */
	private void addListeners() {
		networkSearchBar.addPropertyChangeListener("selectedProvider", evt -> {
			NetworkSearchTaskFactory tf = (NetworkSearchTaskFactory) evt.getNewValue();
			
			if (tf != null)
				updateSelectedProvider(tf);
			
			updateSelectedSearchComponent(tf);
			
			networkSearchBar.updateProvidersButton();
			networkSearchBar.updateSearchEnabled();
		});
		networkSearchBar.getSearchTextField().getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateSelectedProvider(networkSearchBar.getSelectedProvider());
				networkSearchBar.updateSearchButton();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateSelectedProvider(networkSearchBar.getSelectedProvider());
				networkSearchBar.updateSearchButton();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				// Nothing to do here...
			}
		});
		networkSearchBar.getSearchTextField().addActionListener(evt -> {
			runSearch();
		});
		networkSearchBar.getOptionsButton().addActionListener(evt -> {
			if (networkSearchBar.getSelectedProvider() != null)
				networkSearchBar.showOptionsDialog(optionsComponents.get(networkSearchBar.getSelectedProvider()));
		});
		networkSearchBar.getSearchButton().addActionListener(evt -> {
			runSearch();
		});
	}
	
	private void updateSelectedProvider(NetworkSearchTaskFactory factory) {
		JComponent queryComp = queryComponents.get(factory);
		
		// Only if the TaskFactory did not provide its own query component!
		if (factory instanceof AbstractNetworkSearchTaskFactory && queryComp == null)
			((AbstractNetworkSearchTaskFactory) factory).setQuery(
					networkSearchBar.getSearchTextField().getText().trim());
	}
	
	private void updateSelectedSearchComponent(NetworkSearchTaskFactory factory) {
		invokeOnEDT(() -> {
			JComponent queryComp = queryComponents.get(factory);
			JComponent optionsComp = optionsComponents.get(factory);
			
			networkSearchBar.updateSelectedSearchComponent(queryComp);
			networkSearchBar.getOptionsButton().setVisible(optionsComp != null);
			networkSearchBar.updateSearchButton();
		});
	}
	
	private void updateSearchPanel() {
		invokeOnEDT(() -> {
			networkSearchBar.update(new HashSet<>(taskFactories.values()));
		});
	}
	
	private void runSearch() {
		NetworkSearchTaskFactory tf = networkSearchBar.getSelectedProvider();
		
		if (tf != null && tf.isReady()) {
			DialogTaskManager taskManager = serviceRegistrar.getService(DialogTaskManager.class);
			TaskObserver taskObserver = tf.getTaskObserver();
			
			if (taskObserver != null)
				taskManager.execute(tf.createTaskIterator(), taskObserver);
			else
				taskManager.execute(tf.createTaskIterator());
		}
	}
	
	private class QueryChangeListener implements PropertyChangeListener {

		private NetworkSearchTaskFactory factory;
		
		public QueryChangeListener(NetworkSearchTaskFactory factory) {
			this.factory = factory;
		}
		
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if (factory.equals(networkSearchBar.getSelectedProvider()))
				invokeOnEDT(() -> networkSearchBar.updateSearchButton());
		}
	}
}
