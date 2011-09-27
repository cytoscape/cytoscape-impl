



package org.cytoscape.editor.internal;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.task.creation.NewEmptyNetworkViewFactory;
import org.cytoscape.model.CyNetworkManager;

import org.cytoscape.editor.internal.DropNetworkViewTaskFactoryImpl;
import org.cytoscape.editor.internal.DropNodeViewTaskFactoryImpl;
import org.cytoscape.editor.internal.CurrentNetworkViewListener;
import org.cytoscape.editor.internal.gui.EditorCytoPanelComponent;
import org.cytoscape.editor.internal.EditorPanelSelectedListener;
import org.cytoscape.editor.internal.SIFInterpreterTaskFactory;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.dnd.DropNetworkViewTaskFactory;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedListener;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.dnd.DropNodeViewTaskFactory;


import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		CySwingApplication cySwingApplicationServiceRef = getService(bc,CySwingApplication.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class);
		NewEmptyNetworkViewFactory newEmptyNetworkViewFactoryServiceRef = getService(bc,NewEmptyNetworkViewFactory.class);
		CyEventHelper cyEventHelperServiceRef = getService(bc,CyEventHelper.class);
		
		VisualMappingManager vmm = getService(bc,VisualMappingManager.class);
		SIFInterpreterTaskFactory sifInterpreterTaskFactory = new SIFInterpreterTaskFactory();
		DropNetworkViewTaskFactoryImpl dropNetworkViewTaskFactory = new DropNetworkViewTaskFactoryImpl(cyEventHelperServiceRef, vmm);
		DropNodeViewTaskFactoryImpl dropNodeViewTaskFactory = new DropNodeViewTaskFactoryImpl(cyNetworkManagerServiceRef);
		EditorCytoPanelComponent editorCytoPanelComponent = new EditorCytoPanelComponent(cySwingApplicationServiceRef);
		CurrentNetworkViewListener currentNetworkViewListener = new CurrentNetworkViewListener(cySwingApplicationServiceRef,editorCytoPanelComponent);
		EditorPanelSelectedListener editorPanelSelectedListener = new EditorPanelSelectedListener(cySwingApplicationServiceRef,editorCytoPanelComponent,cyNetworkManagerServiceRef,newEmptyNetworkViewFactoryServiceRef);
		
		registerService(bc,editorCytoPanelComponent,CytoPanelComponent.class, new Properties());

		Properties sifInterpreterTaskFactoryProps = new Properties();
		sifInterpreterTaskFactoryProps.setProperty("enableFor","networkAndView");
		sifInterpreterTaskFactoryProps.setProperty("preferredAction","OPEN");
		sifInterpreterTaskFactoryProps.setProperty("preferredMenu","Plugins");
		sifInterpreterTaskFactoryProps.setProperty("menuGravity","5.0f");
		sifInterpreterTaskFactoryProps.setProperty("title","SIF Interpreter");
		registerService(bc,sifInterpreterTaskFactory,NetworkViewTaskFactory.class, sifInterpreterTaskFactoryProps);

		Properties dropNetworkViewTaskFactoryProps = new Properties();
		dropNetworkViewTaskFactoryProps.setProperty("preferredAction","Node");
		dropNetworkViewTaskFactoryProps.setProperty("title","Create Node");
		registerService(bc,dropNetworkViewTaskFactory,DropNetworkViewTaskFactory.class, dropNetworkViewTaskFactoryProps);

		Properties dropNodeViewTaskFactoryProps = new Properties();
		dropNodeViewTaskFactoryProps.setProperty("preferredAction","Network");
		dropNodeViewTaskFactoryProps.setProperty("title","Create Nested Network");
		registerService(bc,dropNodeViewTaskFactory,DropNodeViewTaskFactory.class, dropNodeViewTaskFactoryProps);
		registerService(bc,currentNetworkViewListener,SetCurrentNetworkViewListener.class, new Properties());
		registerService(bc,editorPanelSelectedListener,CytoPanelComponentSelectedListener.class, new Properties());

	}
}

