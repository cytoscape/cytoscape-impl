package org.cytoscape.rest.internal;

/*
 * #%L
 * Cytoscape REST Impl (rest-impl)
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

import java.util.Properties;

import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.property.CyProperty;
import org.cytoscape.rest.RESTResource;
import org.cytoscape.rest.internal.resources.NamespacesResource;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.SynchronousTaskManager;
import org.ops4j.pax.logging.spi.PaxAppender;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

	private static final String REST_PORT_PROP = "restPort";

	public CyActivator() {
		super();
	}

	@Override
	public void start(BundleContext bc) {

		// Import required services
		final CyServiceRegistrar cyServiceRegistrar = getService(bc, CyServiceRegistrar.class);
		final AvailableCommands available = getService(bc, AvailableCommands.class);
		final CommandExecutorTaskFactory ceTaskFactory = getService(bc, CommandExecutorTaskFactory.class);
		final SynchronousTaskManager<?> taskManager = getService(bc, SynchronousTaskManager.class);

		// Get any command line arguments. The "-R" is ours
		@SuppressWarnings("unchecked")
		final CyProperty<Properties> commandLineProps = 
			getService(bc, CyProperty.class, "(cyPropertyName=commandline.props)");

		final Properties p = commandLineProps.getProperties();
		String restPortNumber = null;
		if (p.getProperty(REST_PORT_PROP) != null)
			restPortNumber = p.getProperty(REST_PORT_PROP);

		// Port not specified.
		if (restPortNumber == null) {
			return;
		}

		// At some point, we may want to load resources by registering a Resource listener
		final ResourceManager resourceManager = new ResourceManager(restPortNumber);
		registerServiceListener(bc, resourceManager, "addResource", "removeResource", RESTResource.class);

		// Register the name spaces resource handler. This resource handles all of the commands
		final NamespacesResource nsResource = new NamespacesResource(cyServiceRegistrar, available, taskManager, ceTaskFactory);
		registerService(bc, nsResource, RESTResource.class, new Properties());

		// Register the NamespacesResource as a listener for the Task Monitor messages
		final Properties props = new Properties();
		props.setProperty("org.ops4j.pax.logging.appender.name", "TaskMonitorShowMessagesAppender");
		registerService(bc, nsResource, PaxAppender.class, props);
		
		// Server starts automatically when new resource has been registered.
		
	}
}