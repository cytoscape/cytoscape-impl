package org.cytoscape.scripting.internal;

import static org.cytoscape.work.ServiceProperties.*;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.service.util.AbstractCyActivator;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		
		final CommandExecutorTaskFactory commandExecutorTaskFactoryServiceRef = getService(bc, CommandExecutorTaskFactory.class);
		
		// This object should be injected to all scripts to access manager objects from scripts.
		final CyAppAdapter appAdapter = getService(bc, CyAppAdapter.class);
		
		final ExecuteScriptTaskFactory executeScriptTaskFactory = new ExecuteScriptTaskFactory(appAdapter, commandExecutorTaskFactoryServiceRef);

		final Properties executeScriptTaskFactoryProps = new Properties();
		executeScriptTaskFactoryProps.setProperty(ID, "executeScriptTaskFactory");
		executeScriptTaskFactoryProps.setProperty(PREFERRED_MENU,"File");
		executeScriptTaskFactoryProps.setProperty(MENU_GRAVITY,"6.1f");
		executeScriptTaskFactoryProps.setProperty(TITLE,"Run...");
		executeScriptTaskFactoryProps.setProperty("inToolBar", "false");

		registerAllServices(bc, executeScriptTaskFactory, executeScriptTaskFactoryProps);

	}
}
