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

import java.net.URI;
import java.util.Properties;

import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.property.CyProperty;
import org.cytoscape.rest.RESTResource;
import org.cytoscape.rest.internal.resources.NamespacesResource;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.SynchronousTaskManager;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.ops4j.pax.logging.spi.PaxAppender;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

	// TODO: pick this up from the command line
	public static final String BASE_URI = "http://localhost:";
	public static final String BASE_PATH = "/cytoscape/";
	private HttpServer httpServer = null;

	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		CyServiceRegistrar cyServiceRegistrar = getService(bc, CyServiceRegistrar.class);

		AvailableCommands available = getService(bc, AvailableCommands.class);
		CommandExecutorTaskFactory ceTaskFactory = getService(bc, CommandExecutorTaskFactory.class);
		SynchronousTaskManager taskManager = getService(bc, SynchronousTaskManager.class);

		// Get any command line arguments. The "-R" is ours
		CyProperty<Properties> commandLineProps = getService(bc, CyProperty.class, "(cyPropertyName=commandline.props)");
		Properties p = commandLineProps.getProperties();
		String restPort = null;
		if (p.getProperty("restPort") != null)
			restPort = p.getProperty("restPort");

		// Oops, they don't want a rest server
		if (restPort == null)
			return;

		// Create our ResourceConfig and initialize it with our internal
		// resources
		ResourceConfig resourceConfig = new ResourceConfig();

		// At some point, we may want to load resources by registering a
		// Resource listener
		ResourceManager resourceManager = new ResourceManager(resourceConfig);
		registerServiceListener(bc, resourceManager, "addResource", "removeResource", RESTResource.class);

		// Register the namespaces resource handler. This resource handles
		// all of the commands
		NamespacesResource nsResource = new NamespacesResource(cyServiceRegistrar, available, taskManager,
				ceTaskFactory);
		registerService(bc, nsResource, RESTResource.class, new Properties());

		// Register the NamespacesResource as a listener for the Task Monitor
		// messages
		Properties props = new Properties();
		props.setProperty("org.ops4j.pax.logging.appender.name", "TaskMonitorShowMessagesAppender");
		registerService(bc, nsResource, PaxAppender.class, props);

		// Register network resource handler

		// Register table resource handler
		startServer(BASE_URI + restPort + BASE_PATH, resourceConfig);
		
		System.out.println("SING  = " + resourceConfig.getSingletons());

	}

	HttpServer startServer(String uri, ResourceConfig resourceConfig) {
		// create and start a new instance of grizzly http server
		// exposing the Jersey application at BASE_URI
		HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(uri), resourceConfig);
		System.out.println(String.format("Jersey app started with WADL available at " + "%sapplication.wadl\n", uri));
		return httpServer;
	}

	/*
	 * public void stop(BundleContext bc) { httpServer.stop(); }
	 */
}
