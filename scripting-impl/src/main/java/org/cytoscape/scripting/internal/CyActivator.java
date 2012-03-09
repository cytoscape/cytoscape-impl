package org.cytoscape.scripting.internal;

import java.util.Properties;

import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.service.util.AbstractCyActivator;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		CyAppAdapter appAdapter = getService(bc,CyAppAdapter.class);
		
		ExecuteScriptTaskFactory executeScriptTaskFactory = new ExecuteScriptTaskFactory(appAdapter);
		
		Properties executeScriptTaskFactoryProps = new Properties();
		executeScriptTaskFactoryProps.setProperty("id","executeScriptTaskFactory");
		executeScriptTaskFactoryProps.setProperty("preferredMenu","Tools");
		executeScriptTaskFactoryProps.setProperty("title", "Run script...");
		executeScriptTaskFactoryProps.setProperty("menuGravity","2.0");
		executeScriptTaskFactoryProps.setProperty("toolBarGravity","3.0");
		executeScriptTaskFactoryProps.setProperty("inToolBar","false");
		
		registerAllServices(bc, executeScriptTaskFactory, executeScriptTaskFactoryProps);		
		
	}
}
