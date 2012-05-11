package org.cytoscape.cmdline.headless.internal;

import org.cytoscape.application.CyShutdown;
import org.cytoscape.application.CyVersion;
import org.cytoscape.cmdline.CommandLineArgs;
//import org.cytoscape.command.internal.CommandExecutorTaskFactory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TunableSetter;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		
		TaskFactory cmdExec = getService(bc, TaskFactory.class, "(title=Run Commands...)");
		
		CommandLineArgs args = getService(bc,CommandLineArgs.class);
		
		CyVersion cyVersion = getService(bc,CyVersion.class);
		CyShutdown cyShutdown = getService(bc,CyShutdown.class); 
		StreamUtil streamUtil = getService(bc,StreamUtil.class); /*
		OpenSessionTaskFactory loadSession = getService(bc, OpenSessionTaskFactory.class);
		LoadNetworkFileTaskFactory networkFileLoader = getService(bc, LoadNetworkFileTaskFactory.class);
		LoadNetworkURLTaskFactory networkURLLoader = getService(bc, LoadNetworkURLTaskFactory.class);
		LoadVizmapFileTaskFactory visualStylesLoader = getService(bc, LoadVizmapFileTaskFactory.class);
		*/TaskManager <?,?> taskManager = getService(bc, TaskManager.class); /*

		CyProperty<Properties> props = (CyProperty<Properties>)getService(bc, CyProperty.class, "(cyPropertyName=cytoscape3.props)");

		StartupConfig sc = new StartupConfig(props.getProperties(),streamUtil, loadSession, networkFileLoader, networkURLLoader, visualStylesLoader, taskManager);

		*/
		//CMDExecTaskFactory cmdTF = new CMDExecTaskFactory((CommandExecutorTaskFactory)cmdExec);
		
		TunableSetter setter = getService(bc,TunableSetter.class);
		
		StartupConfig sc = new StartupConfig(streamUtil, cmdExec, taskManager, setter);
		Parser p = new Parser(args.getArgs(), cyShutdown, cyVersion, sc,null);
		sc.start();
		
		//Terminate the program
		cyShutdown.exit(0);
	}
}
