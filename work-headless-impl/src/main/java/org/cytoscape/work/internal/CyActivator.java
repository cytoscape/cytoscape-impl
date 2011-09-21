
package org.cytoscape.work.internal;

import org.cytoscape.cmdline.launcher.CommandLineProvider;

import org.cytoscape.work.internal.UndoSupportImpl;
import org.cytoscape.work.internal.task.HeadlessTaskManager;
import org.cytoscape.work.internal.tunables.CLTunableInterceptor;
import org.cytoscape.work.UndoSupport;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TunableInterceptor;
import org.osgi.framework.BundleContext;
import org.cytoscape.service.util.AbstractCyActivator;
import java.util.Properties;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		CommandLineProvider commandLineProviderServiceRef = getService(bc,CommandLineProvider.class);
		
		UndoSupportImpl undoSupport = new UndoSupportImpl();
		HeadlessTaskManager headlessTaskManager = new HeadlessTaskManager(tunableInterceptor);
		CLTunableInterceptor tunableInterceptor = new CLTunableInterceptor(commandLineProviderServiceRef);
		
		registerService(bc,headlessTaskManager,TaskManager.class, new Properties());
		registerService(bc,tunableInterceptor,TunableInterceptor.class, new Properties());
		registerService(bc,undoSupport,UndoSupport.class, new Properties());
	}
}

