



package org.cytoscape.event.internal;


import org.cytoscape.event.internal.CyEventHelperImpl;
import org.cytoscape.event.internal.CyListenerAdapter;
import org.cytoscape.event.CyEventHelper;
import org.osgi.framework.BundleContext;
import org.cytoscape.service.util.AbstractCyActivator;
import java.util.Properties;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		CyListenerAdapter cyListenerAdapter = new CyListenerAdapter(bc);
		CyEventHelperImpl cyEventHelper = new CyEventHelperImpl(cyListenerAdapter);
		
		registerService(bc,cyEventHelper,CyEventHelper.class, new Properties());
	}
}

