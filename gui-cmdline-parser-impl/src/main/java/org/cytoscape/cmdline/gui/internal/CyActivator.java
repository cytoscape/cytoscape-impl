package org.cytoscape.cmdline.gui.internal;

import org.cytoscape.cmdline.CommandLineArgs;
import org.cytoscape.property.CyProperty;
import org.cytoscape.application.CyVersion;
import org.cytoscape.application.CyShutdown;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.read.LoadNetworkFileTaskFactory;
import org.cytoscape.task.read.LoadNetworkURLTaskFactory;
import org.cytoscape.task.read.LoadTableFileTaskFactory;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.work.TaskManager;

import java.util.Properties; 

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
		LoadTableFileTaskFactory attributesFileLoader = getService(bc, LoadTableFileTaskFactory.class);
		LoadNetworkURLTaskFactory networkURLLoader = getService(bc, LoadNetworkURLTaskFactory.class);
		LoadVizmapFileTaskFactory visualStylesLoader = getService(bc, LoadVizmapFileTaskFactory.class);
		TaskManager <?,?> taskManager = getService(bc, TaskManager.class);
		CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);
		
		CyProperty<Properties> props = (CyProperty<Properties>)getService(bc, CyProperty.class, "(cyPropertyName=cytoscape3.props)");

		StartupConfig sc = new StartupConfig(props.getProperties(),streamUtil,attributesFileLoader, loadSession, networkFileLoader, networkURLLoader, visualStylesLoader, taskManager, registrar);


		Parser p = new Parser(args.getArgs(), cyShutdown, cyVersion, sc,props.getProperties());
		sc.start();
	}
}
