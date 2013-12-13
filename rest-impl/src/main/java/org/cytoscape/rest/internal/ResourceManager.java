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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.rest.internal.RESTResource;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceManager {

	private static final Logger logger = LoggerFactory.getLogger(ResourceManager.class);

	// TODO: pick this up from the command line
	public static final String BASE_URI = "http://localhost:";
	public static final String BASE_PATH = "/cytoscape/";
	
	private final Set<Object> resourceSet;
	
	private ResourceConfig config;
	private HttpServer server;
	
	private final String uri;
	
	public ResourceManager(final String restPortNumber) {
		// Create our ResourceConfig and initialize it with our internal resources
		this.resourceSet = new HashSet<Object>();
		this.uri= BASE_URI + restPortNumber + BASE_PATH;
	}

	/**
	 * Create and start a new instance of grizzly http server
	 * exposing the Jersey application at BASE_URI
	 * 
	 * @param uri
	 * @param resourceConfig
	 * @return
	 */
	final void startServer() {
		if(server!= null && server.isStarted()) {
			stopServer();
		}
		// Create our ResourceConfig and initialize it with our internal resources
		server = GrizzlyHttpServerFactory.createHttpServer(URI.create(uri), config);
		final String message = String.format("Jersey app started with WADL available at " + "%sapplication.wadl\n", uri);
		logger.info(message);
		System.out.println(message);
	}

	final void stopServer() {
		if(server != null && server.isStarted()) {
			server.stop();
		}
	}
	
	/**
	 * Register exported RESTResource (OSGi services)
	 * 
	 * @param resource
	 * @param props
	 */
	public void addResource(final RESTResource resource, Map<String, String> props) {
		logger.info("Adding new REST resource (API): " + resource.toString());
		this.resourceSet.add(resource);
		logger.info("Restarting API server...");
		stopServer();
		this.config = new ResourceConfig();
		this.config.registerInstances(resourceSet);
		
		startServer();
	}

	public void removeResource(final RESTResource resource, Map<String, String> props) {
		logger.info("Removing REST resource: " + resource.toString());

		// TODO: is this possible?
	}
}
