package org.cytoscape.cmdline.headless.internal;

/*
 * #%L
 * Cytoscape Headless Command Line Parser Impl (headless-cmdline-parser-impl)
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


import org.cytoscape.application.CyShutdown;
import org.cytoscape.application.CyVersion;
import org.cytoscape.cmdline.CommandLineArgs;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.SynchronousTaskManager;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		
		final CommandLineArgs args = getService(bc,CommandLineArgs.class);
		final CyVersion cyVersion = getService(bc,CyVersion.class);
		final CyShutdown cyShutdown = getService(bc,CyShutdown.class); 
		final AvailableCommands availableCommands = getService(bc,AvailableCommands.class); 
		final CommandExecutorTaskFactory cmdExec = getService(bc,CommandExecutorTaskFactory.class); 
		final SynchronousTaskManager taskManager = getService(bc, SynchronousTaskManager.class); 

		new Thread( new Runnable() {
			public void run() {
				StartupConfig sc = new StartupConfig(cmdExec, taskManager);
				Parser p = new Parser(args.getArgs(), cyShutdown, cyVersion, sc, availableCommands);
				sc.start();
		try { 
				Thread.sleep(200);
		} catch ( InterruptedException ie) { ie.printStackTrace(); }
				cyShutdown.exit(0);
			}
		}).start();
	}
}
