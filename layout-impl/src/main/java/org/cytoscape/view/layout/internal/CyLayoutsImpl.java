package org.cytoscape.view.layout.internal;

/*
 * #%L
 * Cytoscape Layout Impl (layout-impl)
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
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.TaskFactory;

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;

import org.cytoscape.view.layout.internal.task.LayoutTaskFactoryWrapper;

/**
 * CyLayoutsImpl is a singleton class that is used to register all available
 * layout algorithms.  
 */
public class CyLayoutsImpl implements CyLayoutAlgorithmManager {

	private final Map<String, CyLayoutAlgorithm> layoutMap;
	private final Map<String, TaskFactory> serviceMap;
	private final CyProperty<Properties> cyProps;
	private final CyServiceRegistrar serviceRegistrar;
	private CyApplicationManager appManager;
	private CyNetworkViewManager viewManager;

	public CyLayoutsImpl(CyServiceRegistrar serviceRegistrar, final CyProperty<Properties> p, CyLayoutAlgorithm defaultLayout) {
		this.cyProps = p;
		this.serviceRegistrar = serviceRegistrar;
		layoutMap = new ConcurrentHashMap<String,CyLayoutAlgorithm>();
		serviceMap = new ConcurrentHashMap<String,TaskFactory>();

		// Get some services that we'll need.  
		// NOTE: This creates a loader-order dependency for application-impl.  We
		// could work around that by handing the serviceRegistrar ref down to
		// the wrapper task and having it take the responsibility for getting
		// the service, but that means we'll need to get the service every time
		// we do a layout.  Probably not what we want.  Not clear what the
		// right trade-off is here....
		if (serviceRegistrar != null) {
			appManager = serviceRegistrar.getService(CyApplicationManager.class);
			viewManager = serviceRegistrar.getService(CyNetworkViewManager.class);
		}

		addLayout(defaultLayout, new HashMap());
	}

	/**
	 * Add a layout to the layout manager's list.  If menu is "null"
	 * it will be assigned to the "none" menu, which is not displayed.
	 * This can be used to register layouts that are to be used for
	 * specific algorithmic purposes, but not, in general, supposed
	 * to be for direct user use.
	 *
	 * @param layout The layout to be added
	 * @param menu The menu that this should appear under
	 */
	public void addLayout(CyLayoutAlgorithm layout, Map props) {
		if ( layout != null ) {
			layoutMap.put(layout.getName(),layout);

			if (serviceRegistrar != null) {
				Properties layoutProps = new Properties();
				layoutProps.setProperty(COMMAND, layout.getName());
				layoutProps.setProperty(COMMAND_NAMESPACE, "layout");
				TaskFactory service = new LayoutTaskFactoryWrapper(appManager, viewManager, layout);
				// Register the service as a TaskFactory for commands
				serviceRegistrar.registerService(service, TaskFactory.class, layoutProps);
				serviceMap.put(layout.getName(), service);
			}
		}
	}

	/**
	 * Remove a layout from the layout maanger's list.
	 *
	 * @param layout The layout to remove
	 */
	public void removeLayout(CyLayoutAlgorithm layout, Map props) {
		if ( layout != null ) {
			layoutMap.remove(layout.getName());
			if (serviceRegistrar != null && serviceMap.containsKey(layout.getName())) {
				TaskFactory service = serviceMap.get(layout.getName());
				serviceRegistrar.unregisterService(service,TaskFactory.class);
				serviceMap.remove(layout.getName());
			}
		}
	}

	/**
	 * Get the layout named "name".  If "name" does
	 * not exist, this will return null
	 *
	 * @param name String representing the name of the layout
	 * @return the layout of that name or null if it is not reigstered
	 */
	@Override
	public CyLayoutAlgorithm getLayout(String name) {
		if (name != null)
			return layoutMap.get(name);
		return null;
	}

	/**
	 * Get all of the available layouts.
	 *
	 * @return a Collection of all the available layouts
	 */
	@Override
	public Collection<CyLayoutAlgorithm> getAllLayouts() {
		return layoutMap.values();
	}

	/**
	 * Get the default layout.  This is either the grid layout or a layout
	 * chosen by the user via the setting of the "layout.default" property.
	 *
	 * @return CyLayoutAlgorithm to use as the default layout algorithm
	 */
	@Override
	public CyLayoutAlgorithm getDefaultLayout() {
		// See if the user has set the layout.default property	
		String defaultLayout = cyProps.getProperties().getProperty(CyLayoutAlgorithmManager.DEFAULT_LAYOUT_PROPERTY_NAME);
		if (defaultLayout == null || layoutMap.containsKey(defaultLayout) == false)
			defaultLayout = CyLayoutAlgorithmManager.DEFAULT_LAYOUT_NAME; 

		return layoutMap.get(defaultLayout);
	}
}
