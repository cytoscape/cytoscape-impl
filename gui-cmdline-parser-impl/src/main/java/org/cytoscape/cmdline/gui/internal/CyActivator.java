package org.cytoscape.cmdline.gui.internal;

import org.cytoscape.cmdline.CommandLineArgs;
import org.cytoscape.property.CyProperty;
import org.cytoscape.application.CyVersion;
import org.cytoscape.application.CyShutdown;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.loadnetwork.NetworkFileLoader;
import org.cytoscape.task.loadnetwork.NetworkURLLoader;
import org.cytoscape.task.loadvizmap.LoadVisualStyles;
import org.cytoscape.task.session.LoadSession;
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
		LoadSession loadSession = getService(bc, LoadSession.class);
		NetworkFileLoader networkFileLoader = getService(bc, NetworkFileLoader.class);
		NetworkURLLoader networkURLLoader = getService(bc, NetworkURLLoader.class);
		LoadVisualStyles visualStylesLoader = getService(bc, LoadVisualStyles.class);
		TaskManager <?,?> taskManager = getService(bc, TaskManager.class);

		CyProperty<Properties> props = (CyProperty<Properties>)getService(bc, CyProperty.class, "(cyPropertyName=cytoscape3.props)");

		StartupConfig sc = new StartupConfig(props.getProperties(),streamUtil, loadSession, networkFileLoader, networkURLLoader, visualStylesLoader, taskManager);


		Parser p = new Parser(args.getArgs(), cyShutdown, cyVersion, sc,props.getProperties());
		sc.start();
	}
}
