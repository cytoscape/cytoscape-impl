



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
import org.cytoscape.editor.internal.gui.ShapePalette;
import org.cytoscape.editor.internal.gui.BasicCytoShapeEntity;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedListener;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.dnd.DropNodeViewTaskFactory;
import org.cytoscape.dnd.DropNetworkViewTaskFactory;
import org.cytoscape.dnd.GraphicalEntity;


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
		NewEmptyNetworkViewFactory newEmptyNetworkViewFactoryServiceRef = getService(bc,NewEmptyNetworkViewFactory.class);
		CyEventHelper cyEventHelperServiceRef = getService(bc,CyEventHelper.class);
		
		VisualMappingManager vmm = getService(bc,VisualMappingManager.class);


		ImageIcon nodeIcon = new ImageIcon(getClass().getResource("/images/node.png"));
		ImageIcon edgeIcon = new ImageIcon(getClass().getResource("/images/edge.png"));
		ImageIcon netIcon = new ImageIcon(getClass().getResource("/images/network.png"));

		BasicCytoShapeEntity nodeEntity = new BasicCytoShapeEntity(cySwingApplicationServiceRef, 
		                                                           "NODE_TYPE","unknown", nodeIcon, "Node", 
		                                                           "<html>To add a node to a network,<br>" +
		                                                           "drag and drop a shape<br>" +
		                                                           "from the palette onto the canvas<br>" +
		                                                           "OR<br>" +
		                                                           "simply CTRL-click on the canvas.</html>");

		BasicCytoShapeEntity edgeEntity = new BasicCytoShapeEntity(cySwingApplicationServiceRef, 
		                                                           "EDGE_TYPE","unknown", edgeIcon, "Edge",
		                                                           "<html>To connect two nodes with an edge<br>" +
		                                                           "drag and drop the arrow onto a node<br>" +
		                                                           "on the canvas, then move the cursor<br>" +
		                                                           "over a second node and click the mouse.<br>" +
		                                                           "OR<br>" +
		                                                           "CTRL-click on the first node and then<br>" +
		                                                           "click on the second node. </html>");

		BasicCytoShapeEntity netEntity = new BasicCytoShapeEntity(cySwingApplicationServiceRef, 
		                                                          "NETWORK_TYPE","unknown", netIcon, "Network",
		                                                          "<html>To create a nested network<br>" +
		                                                          "drag and drop the network onto a node<br>" +
		                                                          "to assign a nested network,<br>" +
		                                                          "or on the canvas to create a new node and<br>" +
		                                                          "assign a nested network. </html>");


		ShapePalette shapePalette = new ShapePalette(cySwingApplicationServiceRef);

		SIFInterpreterTaskFactory sifInterpreterTaskFactory = new SIFInterpreterTaskFactory();
		DropNetworkViewTaskFactoryImpl dropNetworkViewTaskFactory = new DropNetworkViewTaskFactoryImpl(cyEventHelperServiceRef, vmm);
		DropNodeViewTaskFactoryImpl dropNodeViewTaskFactory = new DropNodeViewTaskFactoryImpl(cyNetworkManagerServiceRef);
		EditorCytoPanelComponent editorCytoPanelComponent = new EditorCytoPanelComponent(shapePalette);
		CurrentNetworkViewListener currentNetworkViewListener = new CurrentNetworkViewListener(cySwingApplicationServiceRef,editorCytoPanelComponent);
		EditorPanelSelectedListener editorPanelSelectedListener = new EditorPanelSelectedListener(cySwingApplicationServiceRef,editorCytoPanelComponent,cyNetworkManagerServiceRef,newEmptyNetworkViewFactoryServiceRef);
		
		registerService(bc,editorCytoPanelComponent,CytoPanelComponent.class, new Properties());

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
		dropNetworkViewTaskFactoryProps.setProperty("title","Create Node");
		registerService(bc,dropNetworkViewTaskFactory,DropNetworkViewTaskFactory.class, dropNetworkViewTaskFactoryProps);

		Properties dropNodeViewTaskFactoryProps = new Properties();
		dropNodeViewTaskFactoryProps.setProperty("preferredAction","Network");
		dropNodeViewTaskFactoryProps.setProperty("title","Create Nested Network");
		registerService(bc,dropNodeViewTaskFactory,DropNodeViewTaskFactory.class, dropNodeViewTaskFactoryProps);
		registerService(bc,currentNetworkViewListener,SetCurrentNetworkViewListener.class, new Properties());
		registerService(bc,editorPanelSelectedListener,CytoPanelComponentSelectedListener.class, new Properties());

		Properties nodeEntityProps = new Properties();
		nodeEntityProps.setProperty("editorGravity","1.0");
		registerService(bc,nodeEntity,GraphicalEntity.class, nodeEntityProps);

		Properties edgeEntityProps = new Properties();
		edgeEntityProps.setProperty("editorGravity","2.0");
		registerService(bc,edgeEntity,GraphicalEntity.class, edgeEntityProps);

		Properties netEntityProps = new Properties();
		netEntityProps.setProperty("editorGravity","3.0");
		registerService(bc,netEntity,GraphicalEntity.class, netEntityProps);

		registerServiceListener(bc,shapePalette,"addGraphicalEntity","removeGraphicalEntity",GraphicalEntity.class);

	}
}

