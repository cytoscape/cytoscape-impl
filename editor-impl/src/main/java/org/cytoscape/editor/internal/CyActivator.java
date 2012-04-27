

package org.cytoscape.editor.internal;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.NetworkViewLocationTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.osgi.framework.BundleContext;

import static org.cytoscape.work.ServiceProperties.*;

public class CyActivator extends AbstractCyActivator {
	
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		CyApplicationManager cyApplicationManagerServiceRef = getService(bc, CyApplicationManager.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc, CyNetworkManager.class);
		CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc, CyNetworkViewManager.class);
		CyEventHelper cyEventHelperServiceRef = getService(bc, CyEventHelper.class);
		VisualMappingManager visualMappingManagerServiceRef = getService(bc, VisualMappingManager.class);
		CyGroupManager cyGroupManagerServiceRef = getService(bc, CyGroupManager.class);
		CreateNetworkViewTaskFactory createNetworkViewTaskFactoryServiceRef = getService(bc, CreateNetworkViewTaskFactory.class);

		SIFInterpreterTaskFactory sifInterpreterTaskFactory = new SIFInterpreterTaskFactory();
		NetworkViewLocationTaskFactory networkViewLocationTaskFactory = new AddNodeTaskFactory(cyEventHelperServiceRef, visualMappingManagerServiceRef);
		NodeViewTaskFactory addNestedNetworkTaskFactory = new AddNestedNetworkTaskFactory(cyNetworkManagerServiceRef, visualMappingManagerServiceRef, cyGroupManagerServiceRef);
		NodeViewTaskFactory deleteNestedNetworkTaskFactory = new DeleteNestedNetworkTaskFactory(cyNetworkManagerServiceRef, visualMappingManagerServiceRef, cyGroupManagerServiceRef);
		NodeViewTaskFactory goToNestedNetworkTaskFactory = new GoToNestedNetworkTaskFactory(cyNetworkManagerServiceRef, cyNetworkViewManagerServiceRef, cyApplicationManagerServiceRef, createNetworkViewTaskFactoryServiceRef);
			
		Properties sifInterpreterTaskFactoryProps = new Properties();
		sifInterpreterTaskFactoryProps.setProperty(ENABLE_FOR, "networkAndView");
		// Setting preferredAction to OPEN registers this service for double clicks on
		// the network canvas, something we don't want right now for this task!
		//sifInterpreterTaskFactoryProps.setProperty("preferredAction","OPEN");
		sifInterpreterTaskFactoryProps.setProperty(PREFERRED_MENU, "Tools");
		sifInterpreterTaskFactoryProps.setProperty(MENU_GRAVITY, "5.0f");
		sifInterpreterTaskFactoryProps.setProperty(TITLE, "SIF Interpreter");
		registerService(bc, sifInterpreterTaskFactory, NetworkViewTaskFactory.class, sifInterpreterTaskFactoryProps);

		Properties networkViewLocationTaskFactoryProps = new Properties();
		networkViewLocationTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		networkViewLocationTaskFactoryProps.setProperty(TITLE, "Add Node");
		registerService(bc, networkViewLocationTaskFactory, NetworkViewLocationTaskFactory.class, networkViewLocationTaskFactoryProps);

		Properties addNestedNetworkProps = new Properties();
		addNestedNetworkProps.setProperty(PREFERRED_ACTION, "NEW");
		addNestedNetworkProps.setProperty(PREFERRED_MENU, "Nested Network");
		addNestedNetworkProps.setProperty(TITLE, "Add Nested Network");
		registerService(bc, addNestedNetworkTaskFactory, NodeViewTaskFactory.class, addNestedNetworkProps);

		Properties deleteNestedNetworkProps = new Properties();
		deleteNestedNetworkProps.setProperty(PREFERRED_ACTION, "NEW");
		deleteNestedNetworkProps.setProperty(PREFERRED_MENU, "Nested Network");
		deleteNestedNetworkProps.setProperty(TITLE, "Delete Nested Network");
		registerService(bc, deleteNestedNetworkTaskFactory, NodeViewTaskFactory.class, deleteNestedNetworkProps);
		
		Properties goToNestedNetworkProps = new Properties();
		goToNestedNetworkProps.setProperty(PREFERRED_ACTION, "NEW");
		goToNestedNetworkProps.setProperty(PREFERRED_MENU, "Nested Network");
		goToNestedNetworkProps.setProperty(TITLE, "Go to Nested Network");
		registerService(bc, goToNestedNetworkTaskFactory, NodeViewTaskFactory.class, goToNestedNetworkProps);
	}
}
