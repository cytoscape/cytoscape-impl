

package org.cytoscape.editor.internal;

import java.util.Properties;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.NetworkViewLocationTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.gui.SelectedVisualStyleManager;
import org.osgi.framework.BundleContext;


public class CyActivator extends AbstractCyActivator {
	
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class);
		CyEventHelper cyEventHelperServiceRef = getService(bc,CyEventHelper.class);
		VisualMappingManager vmm = getService(bc,VisualMappingManager.class);
		SelectedVisualStyleManager selectedVisualStyleManagerServiceRef = getService(bc,SelectedVisualStyleManager.class);

		SIFInterpreterTaskFactory sifInterpreterTaskFactory = new SIFInterpreterTaskFactory();
		NetworkViewLocationTaskFactory networkViewLocationTaskFactory = new AddNodeTaskFactory(cyEventHelperServiceRef, vmm);
		NodeViewTaskFactory addNestedNetworkTaskFactory = new AddNestedNetworkTaskFactory(cyNetworkManagerServiceRef, selectedVisualStyleManagerServiceRef);
		NodeViewTaskFactory deleteNestedNetworkTaskFactory = new DeleteNestedNetworkTaskFactory(cyNetworkManagerServiceRef, selectedVisualStyleManagerServiceRef);
			
		Properties sifInterpreterTaskFactoryProps = new Properties();
		sifInterpreterTaskFactoryProps.setProperty("enableFor", "networkAndView");
		// Setting preferredAction to OPEN registers this service for double clicks on
		// the network canvas, something we don't want right now for this task!
		//sifInterpreterTaskFactoryProps.setProperty("preferredAction","OPEN");
		sifInterpreterTaskFactoryProps.setProperty("preferredMenu", "Tools");
		sifInterpreterTaskFactoryProps.setProperty("menuGravity", "5.0f");
		sifInterpreterTaskFactoryProps.setProperty("title", "SIF Interpreter");
		registerService(bc, sifInterpreterTaskFactory, NetworkViewTaskFactory.class, sifInterpreterTaskFactoryProps);

		Properties networkViewLocationTaskFactoryProps = new Properties();
		networkViewLocationTaskFactoryProps.setProperty("preferredAction", "NEW");
		networkViewLocationTaskFactoryProps.setProperty("title", "Add Node");
		registerService(bc, networkViewLocationTaskFactory, NetworkViewLocationTaskFactory.class,
				networkViewLocationTaskFactoryProps);

		Properties addNestedNetworkProps = new Properties();
		addNestedNetworkProps.setProperty("preferredAction", "NEW");
		addNestedNetworkProps.setProperty("preferredMenu", "Nested Network");
		addNestedNetworkProps.setProperty("title", "Add Nested Network");
		registerService(bc, addNestedNetworkTaskFactory, NodeViewTaskFactory.class, addNestedNetworkProps);

		Properties deleteNestedNetworkProps = new Properties();
		deleteNestedNetworkProps.setProperty("preferredAction", "NEW");
		deleteNestedNetworkProps.setProperty("preferredMenu", "Nested Network");
		deleteNestedNetworkProps.setProperty("title", "Delete Nested Network");
		registerService(bc, deleteNestedNetworkTaskFactory, NodeViewTaskFactory.class, deleteNestedNetworkProps);
	}
}
