package org.cytoscape.ding;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyEdgeViewContextMenuFactory;
import org.cytoscape.application.swing.CyNodeViewContextMenuFactory;
import org.cytoscape.ding.action.GraphicsDetailAction;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.dependency.CustomGraphicsSizeDependency;
import org.cytoscape.ding.dependency.EdgePaintToArrowHeadPaintDependency;
import org.cytoscape.ding.impl.AddEdgeNodeViewTaskFactoryImpl;
import org.cytoscape.ding.impl.DingNavigationRenderingEngineFactory;
import org.cytoscape.ding.impl.DingRenderingEngineFactory;
import org.cytoscape.ding.impl.DingViewModelFactory;
import org.cytoscape.ding.impl.ViewTaskFactoryListener;
import org.cytoscape.ding.impl.cyannotator.create.AnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.AnnotationFactoryManager;
import org.cytoscape.ding.impl.cyannotator.create.ImageAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.ShapeAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.TextAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.tasks.AddAnnotationTaskFactory;
import org.cytoscape.ding.impl.editor.EdgeBendEditor;
import org.cytoscape.ding.impl.editor.EdgeBendPropertyEditor;
import org.cytoscape.ding.impl.editor.EdgeBendValueEditor;
import org.cytoscape.ding.impl.editor.ObjectPositionEditor;
import org.cytoscape.ding.impl.HandleFactoryImpl;
import org.cytoscape.ding.impl.BendFactoryImpl;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.spacial.SpacialIndex2DFactory;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.NetworkViewLocationTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.events.UpdateNetworkPresentationEventListener;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyEditor;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.SubmenuTaskManager;
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		CyApplicationManager applicationManagerManagerServiceRef = getService(bc,CyApplicationManager.class);
		CustomGraphicsManager customGraphicsManagerServiceRef = getService(bc,CustomGraphicsManager.class);
		RenderingEngineManager renderingEngineManagerServiceRef = getService(bc,RenderingEngineManager.class);
		CyRootNetworkManager cyRootNetworkFactoryServiceRef = getService(bc,CyRootNetworkManager.class);
		UndoSupport undoSupportServiceRef = getService(bc,UndoSupport.class);
		CyTableFactory cyDataTableFactoryServiceRef = getService(bc,CyTableFactory.class);
		SpacialIndex2DFactory spacialIndex2DFactoryServiceRef = getService(bc,SpacialIndex2DFactory.class);
		DialogTaskManager dialogTaskManager = getService(bc,DialogTaskManager.class);
		SubmenuTaskManager submenuTaskManager = getService(bc,SubmenuTaskManager.class);
		CyServiceRegistrar cyServiceRegistrarRef = getService(bc,CyServiceRegistrar.class);
		CyTableManager cyTableManagerServiceRef = getService(bc,CyTableManager.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class);
		CyEventHelper cyEventHelperServiceRef = getService(bc,CyEventHelper.class);
		CyProperty cyPropertyServiceRef = getService(bc,CyProperty.class,"(cyPropertyName=cytoscape3.props)");
		CyNetworkTableManager cyNetworkTableManagerServiceRef = getService(bc,CyNetworkTableManager.class);
		
		CyNetworkFactory cyNetworkFactory = getService(bc,CyNetworkFactory.class);
		
		DVisualLexicon dVisualLexicon = new DVisualLexicon(customGraphicsManagerServiceRef);
		
		ViewTaskFactoryListener vtfListener = new ViewTaskFactoryListener();

		AnnotationFactoryManager annotationFactoryManager = new AnnotationFactoryManager();
		
		DingRenderingEngineFactory dingRenderingEngineFactory = new DingRenderingEngineFactory(cyDataTableFactoryServiceRef,cyRootNetworkFactoryServiceRef,undoSupportServiceRef,spacialIndex2DFactoryServiceRef,dVisualLexicon,dialogTaskManager,submenuTaskManager,cyServiceRegistrarRef,cyNetworkTableManagerServiceRef,cyEventHelperServiceRef,renderingEngineManagerServiceRef, vtfListener,annotationFactoryManager);
		DingNavigationRenderingEngineFactory dingNavigationRenderingEngineFactory = new DingNavigationRenderingEngineFactory(dVisualLexicon,renderingEngineManagerServiceRef,applicationManagerManagerServiceRef);
		AddEdgeNodeViewTaskFactoryImpl addEdgeNodeViewTaskFactory = new AddEdgeNodeViewTaskFactoryImpl(cyNetworkManagerServiceRef);
		
		// Object Position Editor
		ObjectPositionValueEditor objectPositionValueEditor = new ObjectPositionValueEditor();
		ObjectPositionEditor objectPositionEditor = new ObjectPositionEditor(objectPositionValueEditor);
		
		EdgePaintToArrowHeadPaintDependency edgeColor2arrowColorDependency = new EdgePaintToArrowHeadPaintDependency();
		CustomGraphicsSizeDependency nodeCustomGraphicsSizeDependency = new CustomGraphicsSizeDependency();
		DingViewModelFactory dingNetworkViewFactory = new DingViewModelFactory(cyDataTableFactoryServiceRef,cyRootNetworkFactoryServiceRef,undoSupportServiceRef,spacialIndex2DFactoryServiceRef,dVisualLexicon,dialogTaskManager,submenuTaskManager,cyServiceRegistrarRef,cyNetworkTableManagerServiceRef,cyEventHelperServiceRef, vtfListener,annotationFactoryManager);

		// Edge Bend editor
		EdgeBendValueEditor edgeBendValueEditor = new EdgeBendValueEditor(cyNetworkFactory, dingNetworkViewFactory,dingRenderingEngineFactory);
		EdgeBendEditor edgeBendEditor = new EdgeBendEditor(edgeBendValueEditor);
		
		AnnotationFactory imageAnnotationFactory = new ImageAnnotationFactory(customGraphicsManagerServiceRef);
		AnnotationFactory shapeAnnotationFactory = new ShapeAnnotationFactory();
		AnnotationFactory textAnnotationFactory = new TextAnnotationFactory();

		AddAnnotationTaskFactory addImageTaskFactory = new AddAnnotationTaskFactory(imageAnnotationFactory);
		AddAnnotationTaskFactory addShapeTaskFactory = new AddAnnotationTaskFactory(shapeAnnotationFactory);
		AddAnnotationTaskFactory addTextTaskFactory = new AddAnnotationTaskFactory(textAnnotationFactory);
		
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
		addEdgeNodeViewTaskFactoryProps.setProperty("title","Add Edge");
		registerService(bc,addEdgeNodeViewTaskFactory,NodeViewTaskFactory.class, addEdgeNodeViewTaskFactoryProps);

		Properties dVisualLexiconProps = new Properties();
		dVisualLexiconProps.setProperty("serviceType","visualLexicon");
		dVisualLexiconProps.setProperty("id","ding");
		registerService(bc,dVisualLexicon,VisualLexicon.class, dVisualLexiconProps);
		
		final Properties positionEditorProp = new Properties();
		positionEditorProp.setProperty("id","objectPositionValueEditor");
		registerService(bc,objectPositionValueEditor, ValueEditor.class, positionEditorProp);
		
		final Properties objectPositionEditorProp = new Properties();
		objectPositionEditorProp.setProperty("id","objectPositionEditor");
		registerService(bc,objectPositionEditor, VisualPropertyEditor.class, objectPositionEditorProp);
		
		registerAllServices(bc, edgeBendValueEditor, new Properties());
		registerService(bc, edgeBendEditor, VisualPropertyEditor.class, new Properties());
		
		registerAllServices(bc,edgeColor2arrowColorDependency, new Properties());
		registerAllServices(bc,nodeCustomGraphicsSizeDependency, new Properties());

		Properties dingNetworkViewFactoryServiceProps = new Properties();
		dingNetworkViewFactoryServiceProps.setProperty("service.type","factory");
		registerService(bc,dingNetworkViewFactory,CyNetworkViewFactory.class, dingNetworkViewFactoryServiceProps);
		
		Properties addImageTaskFactoryProps = new Properties();
		addImageTaskFactoryProps.setProperty("preferredAction","NEW");
		addImageTaskFactoryProps.setProperty("title","Add Image");
		registerService(bc,addImageTaskFactory,NetworkViewLocationTaskFactory.class,addImageTaskFactoryProps);

		Properties addShapeTaskFactoryProps = new Properties();
		addShapeTaskFactoryProps.setProperty("preferredAction","NEW");
		addShapeTaskFactoryProps.setProperty("title","Add Shape");
		registerService(bc,addShapeTaskFactory,NetworkViewLocationTaskFactory.class,addShapeTaskFactoryProps);

		Properties addTextTaskFactoryProps = new Properties();
		addTextTaskFactoryProps.setProperty("preferredAction","NEW");
		addTextTaskFactoryProps.setProperty("title","Add Text");
		registerService(bc,addTextTaskFactory,NetworkViewLocationTaskFactory.class,addTextTaskFactoryProps);

		registerServiceListener(bc,vtfListener,"addNodeViewTaskFactory","removeNodeViewTaskFactory",NodeViewTaskFactory.class);
		registerServiceListener(bc,vtfListener,"addEdgeViewTaskFactory","removeEdgeViewTaskFactory",EdgeViewTaskFactory.class);
		registerServiceListener(bc,vtfListener,"addNetworkViewTaskFactory","removeNetworkViewTaskFactory",NetworkViewTaskFactory.class);
		registerServiceListener(bc,vtfListener,"addNetworkViewLocationTaskFactory","removeNetworkViewLocationTaskFactory",NetworkViewLocationTaskFactory.class);
		registerServiceListener(bc,vtfListener,"addCyEdgeViewContextMenuFactory","removeCyEdgeViewContextMenuFactory",CyEdgeViewContextMenuFactory.class);
		registerServiceListener(bc,vtfListener,"addCyNodeViewContextMenuFactory","removeCyNodeViewContextMenuFactory",CyNodeViewContextMenuFactory.class);
		

		registerServiceListener(bc,annotationFactoryManager,"addAnnotationFactory","removeAnnotationFactory",AnnotationFactory.class);

		GraphicsDetailAction graphicsDetailAction = new GraphicsDetailAction(applicationManagerManagerServiceRef, dialogTaskManager,
				 cyPropertyServiceRef);
		registerAllServices(bc,graphicsDetailAction, new Properties());
	
		
		HandleFactory handleFactory = new HandleFactoryImpl();
		registerService(bc,handleFactory,HandleFactory.class,new Properties());

		BendFactory bendFactory = new BendFactoryImpl();
		registerService(bc,bendFactory,BendFactory.class,new Properties());

	}
}

