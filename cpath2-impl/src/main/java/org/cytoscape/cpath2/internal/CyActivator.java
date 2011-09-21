package org.cytoscape.cpath2.internal;

import org.cytoscape.work.TaskManager;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.biopax.MapBioPaxToCytoscapeFactory;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.biopax.BioPaxContainer;
import org.cytoscape.work.undo.UndoSupport;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.biopax.NetworkListener;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;

import org.cytoscape.cpath2.internal.web_service.CytoscapeCPathWebService;
import org.cytoscape.cpath2.internal.cytoscape.BinarySifVisualStyleUtil;
import org.cytoscape.cpath2.internal.CPath2Factory;



import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		CySwingApplication cySwingApplicationRef = getService(bc,CySwingApplication.class);
		TaskManager taskManagerRef = getService(bc,TaskManager.class);
		OpenBrowser openBrowserRef = getService(bc,OpenBrowser.class);
		CyNetworkManager cyNetworkManagerRef = getService(bc,CyNetworkManager.class);
		CyApplicationManager cyApplicationManagerRef = getService(bc,CyApplicationManager.class);
		CyNetworkViewManager cyNetworkViewManagerRef = getService(bc,CyNetworkViewManager.class);
		CyNetworkReaderManager cyNetworkViewReaderManagerRef = getService(bc,CyNetworkReaderManager.class);
		CyNetworkNaming cyNetworkNamingRef = getService(bc,CyNetworkNaming.class);
		CyNetworkFactory cyNetworkFactoryRef = getService(bc,CyNetworkFactory.class);
		CyLayoutAlgorithmManager cyLayoutsRef = getService(bc,CyLayoutAlgorithmManager.class);
		UndoSupport undoSupportRef = getService(bc,UndoSupport.class);
		BioPaxContainer bioPaxContainerRef = getService(bc,BioPaxContainer.class);
		MapBioPaxToCytoscapeFactory mapBioPaxToCytoscapeFactoryRef = getService(bc,MapBioPaxToCytoscapeFactory.class);
		NetworkListener networkListenerRef = getService(bc,NetworkListener.class);
		VisualMappingManager visualMappingManagerRef = getService(bc,VisualMappingManager.class);
		VisualStyleFactory visualStyleFactoryRef = getService(bc,VisualStyleFactory.class);
		VisualMappingFunctionFactory discreteMappingFactoryRef = getService(bc,VisualMappingFunctionFactory.class,"(mapping.type=discrete)");
		VisualMappingFunctionFactory passthroughMappingFactoryRef = getService(bc,VisualMappingFunctionFactory.class,"(mapping.type=passthrough)");
		
		BinarySifVisualStyleUtil binarySifVisualStyleUtil = new BinarySifVisualStyleUtil(visualStyleFactoryRef,visualMappingManagerRef,discreteMappingFactoryRef,passthroughMappingFactoryRef);
		CPath2Factory cPath2Factory = new CPath2Factory(cySwingApplicationRef,taskManagerRef,openBrowserRef,cyNetworkManagerRef,cyApplicationManagerRef,cyNetworkViewManagerRef,cyNetworkViewReaderManagerRef,cyNetworkNamingRef,cyNetworkFactoryRef,cyLayoutsRef,undoSupportRef,bioPaxContainerRef,mapBioPaxToCytoscapeFactoryRef,networkListenerRef,binarySifVisualStyleUtil,visualMappingManagerRef);
		CytoscapeCPathWebService cPathWebService = new CytoscapeCPathWebService(cPath2Factory);
		
		registerAllServices(bc,cPathWebService, new Properties());
	}
}

