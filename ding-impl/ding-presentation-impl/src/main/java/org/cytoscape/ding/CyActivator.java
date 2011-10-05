package org.cytoscape.ding;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.dependency.CustomGraphicsSizeDependency;
import org.cytoscape.ding.dependency.EdgePaintToArrowHeadPaintDependency;
import org.cytoscape.ding.impl.AddEdgeNodeViewTaskFactoryImpl;
import org.cytoscape.ding.impl.DVisualLexicon;
import org.cytoscape.ding.impl.DingNavigationRenderingEngineFactory;
import org.cytoscape.ding.impl.DingRenderingEngineFactory;
import org.cytoscape.ding.impl.DingViewModelFactory;
import org.cytoscape.ding.impl.ViewTaskFactoryListener;
import org.cytoscape.ding.impl.editor.ObjectPositionEditor;
import org.cytoscape.dnd.DropNetworkViewTaskFactory;
import org.cytoscape.dnd.DropNodeViewTaskFactory;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkFactory;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.spacial.SpacialIndex2DFactory;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.events.UpdateNetworkPresentationEventListener;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;
import org.cytoscape.ding.action.GraphicsDetailAction;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		CyApplicationManager applicationManagerManagerServiceRef = getService(bc,CyApplicationManager.class);
		CustomGraphicsManager customGraphicsManagerServiceRef = getService(bc,CustomGraphicsManager.class);
		RenderingEngineManager renderingEngineManagerServiceRef = getService(bc,RenderingEngineManager.class);
		CyRootNetworkFactory cyRootNetworkFactoryServiceRef = getService(bc,CyRootNetworkFactory.class);
		UndoSupport undoSupportServiceRef = getService(bc,UndoSupport.class);
		CyTableFactory cyDataTableFactoryServiceRef = getService(bc,CyTableFactory.class);
		SpacialIndex2DFactory spacialIndex2DFactoryServiceRef = getService(bc,SpacialIndex2DFactory.class);
		TaskManager taskManagerServiceRef = getService(bc,TaskManager.class);
		CyServiceRegistrar cyServiceRegistrarRef = getService(bc,CyServiceRegistrar.class);
		CyTableManager cyTableManagerServiceRef = getService(bc,CyTableManager.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class);
		CyEventHelper cyEventHelperServiceRef = getService(bc,CyEventHelper.class);
		CyProperty cyPropertyServiceRef = getService(bc,CyProperty.class,"(cyPropertyName=cytoscape3.props)");
		CyNetworkTableManager cyNetworkTableManagerServiceRef = getService(bc,CyNetworkTableManager.class);
		
		DVisualLexicon dVisualLexicon = new DVisualLexicon(customGraphicsManagerServiceRef);
		
		ViewTaskFactoryListener vtfListener = new ViewTaskFactoryListener();
		
		DingRenderingEngineFactory dingRenderingEngineFactory = new DingRenderingEngineFactory(cyDataTableFactoryServiceRef,cyRootNetworkFactoryServiceRef,undoSupportServiceRef,spacialIndex2DFactoryServiceRef,dVisualLexicon,taskManagerServiceRef,cyServiceRegistrarRef,cyNetworkTableManagerServiceRef,cyEventHelperServiceRef,renderingEngineManagerServiceRef, vtfListener);
		DingNavigationRenderingEngineFactory dingNavigationRenderingEngineFactory = new DingNavigationRenderingEngineFactory(dVisualLexicon,renderingEngineManagerServiceRef,applicationManagerManagerServiceRef);
		AddEdgeNodeViewTaskFactoryImpl addEdgeNodeViewTaskFactory = new AddEdgeNodeViewTaskFactoryImpl(cyNetworkManagerServiceRef);
		ObjectPositionValueEditor objectPositionValueEditor = new ObjectPositionValueEditor();
		ObjectPositionEditor objectPositionEditor = new ObjectPositionEditor(objectPositionValueEditor);
		EdgePaintToArrowHeadPaintDependency edgeColor2arrowColorDependency = new EdgePaintToArrowHeadPaintDependency();
		CustomGraphicsSizeDependency nodeCustomGraphicsSizeDependency = new CustomGraphicsSizeDependency();
		DingViewModelFactory dingNetworkViewFactory = new DingViewModelFactory(cyDataTableFactoryServiceRef,cyRootNetworkFactoryServiceRef,undoSupportServiceRef,spacialIndex2DFactoryServiceRef,dVisualLexicon,taskManagerServiceRef,cyServiceRegistrarRef,cyNetworkTableManagerServiceRef,cyEventHelperServiceRef, vtfListener);
		
		Properties dingRenderingEngineFactoryProps = new Properties();
		dingRenderingEngineFactoryProps.setProperty("serviceType","presentationFactory");
		dingRenderingEngineFactoryProps.setProperty("id","ding");
		registerService(bc,dingRenderingEngineFactory,RenderingEngineFactory.class, dingRenderingEngineFactoryProps);
		registerService(bc,dingRenderingEngineFactory,UpdateNetworkPresentationEventListener.class, dingRenderingEngineFactoryProps);

		Properties dingNavigationRenderingEngineFactoryProps = new Properties();
		dingNavigationRenderingEngineFactoryProps.setProperty("serviceType","presentationFactory");
		dingNavigationRenderingEngineFactoryProps.setProperty("id","dingNavigation");
		registerService(bc,dingNavigationRenderingEngineFactory,RenderingEngineFactory.class, dingNavigationRenderingEngineFactoryProps);
		registerService(bc,dingNavigationRenderingEngineFactory,UpdateNetworkPresentationEventListener.class, dingNavigationRenderingEngineFactoryProps);

		Properties addEdgeNodeViewTaskFactoryProps = new Properties();
		addEdgeNodeViewTaskFactoryProps.setProperty("preferredAction","Edge");
		addEdgeNodeViewTaskFactoryProps.setProperty("title","Create Edge");
		registerService(bc,addEdgeNodeViewTaskFactory,DropNodeViewTaskFactory.class, addEdgeNodeViewTaskFactoryProps);

		Properties dVisualLexiconProps = new Properties();
		dVisualLexiconProps.setProperty("serviceType","visualLexicon");
		dVisualLexiconProps.setProperty("id","ding");
		registerService(bc,dVisualLexicon,VisualLexicon.class, dVisualLexiconProps);
		registerAllServices(bc,objectPositionValueEditor, new Properties());
		registerAllServices(bc,objectPositionEditor, new Properties());
		registerAllServices(bc,edgeColor2arrowColorDependency, new Properties());
		registerAllServices(bc,nodeCustomGraphicsSizeDependency, new Properties());

		Properties dingNetworkViewFactoryServiceProps = new Properties();
		dingNetworkViewFactoryServiceProps.setProperty("service.type","factory");
		registerService(bc,dingNetworkViewFactory,CyNetworkViewFactory.class, dingNetworkViewFactoryServiceProps);

		registerServiceListener(bc,vtfListener,"addNodeViewTaskFactory","removeNodeViewTaskFactory",NodeViewTaskFactory.class);
		registerServiceListener(bc,vtfListener,"addEdgeViewTaskFactory","removeEdgeViewTaskFactory",EdgeViewTaskFactory.class);
		registerServiceListener(bc,vtfListener,"addNetworkViewTaskFactory","removeNetworkViewTaskFactory",NetworkViewTaskFactory.class);
		registerServiceListener(bc,vtfListener,"addDropNodeViewTaskFactory","removeDropNodeViewTaskFactory",DropNodeViewTaskFactory.class);
		registerServiceListener(bc,vtfListener,"addDropNetworkViewTaskFactory","removeDropNetworkViewTaskFactory",DropNetworkViewTaskFactory.class);

		GraphicsDetailAction graphicsDetailAction = new GraphicsDetailAction(applicationManagerManagerServiceRef, taskManagerServiceRef,
				 cyPropertyServiceRef);
		registerAllServices(bc,graphicsDetailAction, new Properties());
	}
	
}

