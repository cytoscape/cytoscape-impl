package org.cytoscape.cmdline.gui.internal;

/*
 * #%L
 * Cytoscape GUI Command Line Parser Impl (gui-cmdline-parser-impl)
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

import org.cytoscape.application.CyShutdown;
import org.cytoscape.application.CyVersion;
import org.cytoscape.cmdline.CommandLineArgs;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.LoadNetworkFileTaskFactory;
import org.cytoscape.task.read.LoadNetworkURLTaskFactory;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.work.TaskManager;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		CommandLineArgs args = getService(bc,CommandLineArgs.class);
		CyVersion cyVersion = getService(bc,CyVersion.class);
		CyShutdown cyShutdown = getService(bc,CyShutdown.class);
		StreamUtil streamUtil = getService(bc,StreamUtil.class);
		OpenSessionTaskFactory loadSession = getService(bc, OpenSessionTaskFactory.class);
		LoadNetworkFileTaskFactory networkFileLoader = getService(bc, LoadNetworkFileTaskFactory.class);
		LoadNetworkURLTaskFactory networkURLLoader = getService(bc, LoadNetworkURLTaskFactory.class);
		LoadVizmapFileTaskFactory visualStylesLoader = getService(bc, LoadVizmapFileTaskFactory.class);
		TaskManager <?,?> taskManager = getService(bc, TaskManager.class);
		CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);
		
		CyProperty<Properties> props = (CyProperty<Properties>)getService(bc, CyProperty.class, "(cyPropertyName=cytoscape3.props)");

		StartupConfig sc = new StartupConfig(props.getProperties(),streamUtil, loadSession, networkFileLoader, networkURLLoader, visualStylesLoader, taskManager, registrar);


		Parser p = new Parser(args.getArgs(), cyShutdown, cyVersion, sc,props.getProperties());
		sc.start();
	}
}
