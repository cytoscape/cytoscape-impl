



package org.cytoscape.editor.internal;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.task.creation.NewEmptyNetworkViewFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;

import org.cytoscape.editor.internal.SIFInterpreterTaskFactory;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedListener;
import org.cytoscape.task.NetworkViewLocationTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;


import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;
import javax.swing.ImageIcon;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		CySwingApplication cySwingApplicationServiceRef = getService(bc,CySwingApplication.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class);
		CyRootNetworkManager cyRootNetworkManagerServiceRef = getService(bc,CyRootNetworkManager.class);
		NewEmptyNetworkViewFactory newEmptyNetworkViewFactoryServiceRef = getService(bc,NewEmptyNetworkViewFactory.class);
		CyEventHelper cyEventHelperServiceRef = getService(bc,CyEventHelper.class);
		VisualMappingManager vmm = getService(bc,VisualMappingManager.class);


		SIFInterpreterTaskFactory sifInterpreterTaskFactory = new SIFInterpreterTaskFactory();
		NetworkViewLocationTaskFactory networkViewLocationTaskFactory = new AddNodeTaskFactory(cyEventHelperServiceRef, vmm, cyRootNetworkManagerServiceRef);
		NodeViewTaskFactory addNestedNetworkTaskFactory = new AddNestedNetworkTaskFactory(cyNetworkManagerServiceRef);
			
		Properties sifInterpreterTaskFactoryProps = new Properties();
		sifInterpreterTaskFactoryProps.setProperty("enableFor","networkAndView");
		// Setting preferredAction to OPEN registers this service for double clicks on
		// the network canvas, something we don't want right now for this task!
		//sifInterpreterTaskFactoryProps.setProperty("preferredAction","OPEN");
		sifInterpreterTaskFactoryProps.setProperty("preferredMenu","Tools");
		sifInterpreterTaskFactoryProps.setProperty("menuGravity","5.0f");
		sifInterpreterTaskFactoryProps.setProperty("title","SIF Interpreter");
		registerService(bc,sifInterpreterTaskFactory,NetworkViewTaskFactory.class, sifInterpreterTaskFactoryProps);

		Properties dropNetworkViewTaskFactoryProps = new Properties();
		dropNetworkViewTaskFactoryProps.setProperty("preferredAction","Node");
		dropNetworkViewTaskFactoryProps.setProperty("title","Add Node");
		registerService(bc,networkViewLocationTaskFactory,NetworkViewLocationTaskFactory.class, dropNetworkViewTaskFactoryProps);
		
		Properties dropNodeViewTaskFactoryProps = new Properties();
		dropNodeViewTaskFactoryProps.setProperty("preferredAction","Network");
		dropNodeViewTaskFactoryProps.setProperty("title","Add Nested Network");
		registerService(bc,addNestedNetworkTaskFactory,NodeViewTaskFactory.class, dropNodeViewTaskFactoryProps);
	}
}

