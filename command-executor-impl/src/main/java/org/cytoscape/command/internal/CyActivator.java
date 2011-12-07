



package org.cytoscape.command.internal;

import org.cytoscape.application.CyApplicationManager;

import org.cytoscape.command.internal.CommandExecutorImpl;
import org.cytoscape.command.internal.CommandExecutorTaskFactory;

import org.cytoscape.work.TaskFactory;

import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TaskFactory;

import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		CyApplicationManager cyApplicationManagerServiceRef = getService(bc,CyApplicationManager.class);
		
		CommandExecutorImpl commandExecutorImpl = new CommandExecutorImpl(cyApplicationManagerServiceRef);
		CommandExecutorTaskFactory commandExecutorTaskFactory = new CommandExecutorTaskFactory(commandExecutorImpl);
		
		
		Properties commandExecutorTaskFactoryProps = new Properties();
		commandExecutorTaskFactoryProps.setProperty("preferredMenu","Apps");
		commandExecutorTaskFactoryProps.setProperty("title","Load Command File");
		registerService(bc,commandExecutorTaskFactory,TaskFactory.class, commandExecutorTaskFactoryProps);

		registerServiceListener(bc,commandExecutorImpl,"addTaskFactory","removeTaskFactory",TaskFactory.class);
		registerServiceListener(bc,commandExecutorImpl,"addNetworkTaskFactory","removeNetworkTaskFactory",NetworkTaskFactory.class);


	}
}

