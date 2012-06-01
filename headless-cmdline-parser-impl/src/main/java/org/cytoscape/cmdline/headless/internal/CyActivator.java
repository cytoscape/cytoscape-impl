package org.cytoscape.cmdline.headless.internal;

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
