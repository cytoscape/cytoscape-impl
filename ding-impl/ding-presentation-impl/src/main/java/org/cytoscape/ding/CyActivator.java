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
import org.cytoscape.dnd.GraphicalEntity;
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
import org.cytoscape.ding.impl.cyannotator.tasks.BasicGraphicalEntity;
import org.cytoscape.ding.impl.cyannotator.tasks.DropAnnotationTaskFactory;
import org.cytoscape.ding.impl.cyannotator.create.AnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.ImageAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.ArrowAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.BoundedAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.ShapeAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.TextAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.AnnotationFactoryManager;

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

		AnnotationFactoryManager annotationFactoryManager = new AnnotationFactoryManager();
		
		DingRenderingEngineFactory dingRenderingEngineFactory = new DingRenderingEngineFactory(cyDataTableFactoryServiceRef,cyRootNetworkFactoryServiceRef,undoSupportServiceRef,spacialIndex2DFactoryServiceRef,dVisualLexicon,taskManagerServiceRef,cyServiceRegistrarRef,cyNetworkTableManagerServiceRef,cyEventHelperServiceRef,renderingEngineManagerServiceRef, vtfListener,annotationFactoryManager);
		DingNavigationRenderingEngineFactory dingNavigationRenderingEngineFactory = new DingNavigationRenderingEngineFactory(dVisualLexicon,renderingEngineManagerServiceRef,applicationManagerManagerServiceRef);
		AddEdgeNodeViewTaskFactoryImpl addEdgeNodeViewTaskFactory = new AddEdgeNodeViewTaskFactoryImpl(cyNetworkManagerServiceRef);
		ObjectPositionValueEditor objectPositionValueEditor = new ObjectPositionValueEditor();
		ObjectPositionEditor objectPositionEditor = new ObjectPositionEditor(objectPositionValueEditor);
		EdgePaintToArrowHeadPaintDependency edgeColor2arrowColorDependency = new EdgePaintToArrowHeadPaintDependency();
		CustomGraphicsSizeDependency nodeCustomGraphicsSizeDependency = new CustomGraphicsSizeDependency();
		DingViewModelFactory dingNetworkViewFactory = new DingViewModelFactory(cyDataTableFactoryServiceRef,cyRootNetworkFactoryServiceRef,undoSupportServiceRef,spacialIndex2DFactoryServiceRef,dVisualLexicon,taskManagerServiceRef,cyServiceRegistrarRef,cyNetworkTableManagerServiceRef,cyEventHelperServiceRef, vtfListener,annotationFactoryManager);

		BasicGraphicalEntity imageGraphicalEntity = new BasicGraphicalEntity("Image","Image Attr","Image Value", "An Image annotation", "/images/imageIcon.png");
//		BasicGraphicalEntity arrowGraphicalEntity = new BasicGraphicalEntity("Arrow","Arrow Attr","Arrow Value", "An Arrow annotation", "/images/arrowIcon.png");
//		BasicGraphicalEntity boundedGraphicalEntity = new BasicGraphicalEntity("Bounded","Bounded Attr","Bounded Value", "A Bounded annotation", "/images/boundedIcon.png");
		BasicGraphicalEntity shapeGraphicalEntity = new BasicGraphicalEntity("Shape","Shape Attr","Shape Value", "A Shape annotation", "/images/shapeIcon.png");
		BasicGraphicalEntity textGraphicalEntity = new BasicGraphicalEntity("Text","Text Attr","Text Value", "A Text annotation", "/images/textIcon.png");

		AnnotationFactory imageAnnotationFactory = new ImageAnnotationFactory(customGraphicsManagerServiceRef);
//		AnnotationFactory arrowAnnotationFactory = new ArrowAnnotationFactory();
//		AnnotationFactory boundedAnnotationFactory = new BoundedAnnotationFactory();
		AnnotationFactory shapeAnnotationFactory = new ShapeAnnotationFactory();
		AnnotationFactory textAnnotationFactory = new TextAnnotationFactory();

		DropAnnotationTaskFactory dropImageTaskFactory = new DropAnnotationTaskFactory(imageGraphicalEntity,imageAnnotationFactory);
//		DropAnnotationTaskFactory dropArrowTaskFactory = new DropAnnotationTaskFactory(arrowGraphicalEntity,arrowAnnotationFactory);
//		DropAnnotationTaskFactory dropBoundedTaskFactory = new DropAnnotationTaskFactory(boundedGraphicalEntity,boundedAnnotationFactory);
		DropAnnotationTaskFactory dropShapeTaskFactory = new DropAnnotationTaskFactory(shapeGraphicalEntity,shapeAnnotationFactory);
		DropAnnotationTaskFactory dropTextTaskFactory = new DropAnnotationTaskFactory(textGraphicalEntity,textAnnotationFactory);
		
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


		registerService(bc,imageGraphicalEntity,GraphicalEntity.class,new Properties());
		registerService(bc,shapeGraphicalEntity,GraphicalEntity.class,new Properties());
//		registerService(bc,arrowGraphicalEntity,GraphicalEntity.class,new Properties());
		registerService(bc,textGraphicalEntity,GraphicalEntity.class,new Properties());
//		registerService(bc,boundedGraphicalEntity,GraphicalEntity.class,new Properties());

		Properties dropImageTaskFactoryProps = new Properties();
		dropImageTaskFactoryProps.setProperty("preferredAction","Image");
		dropImageTaskFactoryProps.setProperty("title","Drop Image");
		registerService(bc,dropImageTaskFactory,DropNetworkViewTaskFactory.class,dropImageTaskFactoryProps);

//		Properties dropArrowTaskFactoryProps = new Properties();
//		dropArrowTaskFactoryProps.setProperty("preferredAction","Arrow");
//		dropArrowTaskFactoryProps.setProperty("title","Drop Arrow");
//		registerService(bc,dropArrowTaskFactory,DropNetworkViewTaskFactory.class,dropArrowTaskFactoryProps);

//		Properties dropBoundedTaskFactoryProps = new Properties();
//		dropBoundedTaskFactoryProps.setProperty("preferredAction","Bounded");
//		dropBoundedTaskFactoryProps.setProperty("title","Drop Bounded");
//		registerService(bc,dropBoundedTaskFactory,DropNetworkViewTaskFactory.class,dropBoundedTaskFactoryProps);

		Properties dropShapeTaskFactoryProps = new Properties();
		dropShapeTaskFactoryProps.setProperty("preferredAction","Shape");
		dropShapeTaskFactoryProps.setProperty("title","Drop Shape");
		registerService(bc,dropShapeTaskFactory,DropNetworkViewTaskFactory.class,dropShapeTaskFactoryProps);

		Properties dropTextTaskFactoryProps = new Properties();
		dropTextTaskFactoryProps.setProperty("preferredAction","Text");
		dropTextTaskFactoryProps.setProperty("title","Drop Text");
		registerService(bc,dropTextTaskFactory,DropNetworkViewTaskFactory.class,dropTextTaskFactoryProps);

		registerServiceListener(bc,vtfListener,"addNodeViewTaskFactory","removeNodeViewTaskFactory",NodeViewTaskFactory.class);
		registerServiceListener(bc,vtfListener,"addEdgeViewTaskFactory","removeEdgeViewTaskFactory",EdgeViewTaskFactory.class);
		registerServiceListener(bc,vtfListener,"addNetworkViewTaskFactory","removeNetworkViewTaskFactory",NetworkViewTaskFactory.class);
		registerServiceListener(bc,vtfListener,"addDropNodeViewTaskFactory","removeDropNodeViewTaskFactory",DropNodeViewTaskFactory.class);
		registerServiceListener(bc,vtfListener,"addDropNetworkViewTaskFactory","removeDropNetworkViewTaskFactory",DropNetworkViewTaskFactory.class);

		registerServiceListener(bc,annotationFactoryManager,"addAnnotationFactory","removeAnnotationFactory",AnnotationFactory.class);

		GraphicsDetailAction graphicsDetailAction = new GraphicsDetailAction(applicationManagerManagerServiceRef, taskManagerServiceRef,
				 cyPropertyServiceRef);
		registerAllServices(bc,graphicsDetailAction, new Properties());
	}
	
}

