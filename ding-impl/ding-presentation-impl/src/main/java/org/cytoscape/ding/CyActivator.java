package org.cytoscape.ding;

import static org.cytoscape.work.ServiceProperties.*;

import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CyEdgeViewContextMenuFactory;
import org.cytoscape.application.swing.CyNodeViewContextMenuFactory;
import org.cytoscape.ding.action.GraphicsDetailAction;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.customgraphicsmgr.internal.CustomGraphicsManagerImpl;
import org.cytoscape.ding.customgraphicsmgr.internal.action.CustomGraphicsManagerAction;
import org.cytoscape.ding.customgraphicsmgr.internal.ui.CustomGraphicsBrowser;
import org.cytoscape.ding.dependency.CustomGraphicsSizeDependencyFactory;
import org.cytoscape.ding.dependency.EdgeColorDependencyFactory;
import org.cytoscape.ding.dependency.NodeSizeDependencyFactory;
import org.cytoscape.ding.impl.AddEdgeNodeViewTaskFactoryImpl;
import org.cytoscape.ding.impl.BendFactoryImpl;
import org.cytoscape.ding.impl.DingGraphLOD;
import org.cytoscape.ding.impl.DingGraphLODAll;
import org.cytoscape.ding.impl.DingNavigationRenderingEngineFactory;
import org.cytoscape.ding.impl.DingRenderingEngineFactory;
import org.cytoscape.ding.impl.DingViewModelFactory;
import org.cytoscape.ding.impl.HandleFactoryImpl;
import org.cytoscape.ding.impl.ViewTaskFactoryListener;
import org.cytoscape.ding.impl.cyannotator.create.AnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.AnnotationFactoryManager;
import org.cytoscape.ding.impl.cyannotator.create.ImageAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.ShapeAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.TextAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.tasks.AddAnnotationTaskFactory;
import org.cytoscape.ding.impl.editor.EdgeBendEditor;
import org.cytoscape.ding.impl.editor.EdgeBendValueEditor;
import org.cytoscape.ding.impl.editor.ObjectPositionEditor;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.PropertyUpdatedListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.spacial.SpacialIndex2DFactory;
import org.cytoscape.spacial.internal.rtree.RTreeFactory;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.NetworkViewLocationTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualPropertyDependencyFactory;
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

	@Override
	public void start(BundleContext bc) {
		startSpacial(bc); 
		startCustomGraphicsMgr(bc);
		startPresentationImpl(bc);
	}


	private void startPresentationImpl(BundleContext bc) {

		CyServiceRegistrar cyServiceRegistrarServiceRef = getService(bc, CyServiceRegistrar.class);
		CyApplicationManager applicationManagerManagerServiceRef = getService(bc, CyApplicationManager.class);
		CustomGraphicsManager customGraphicsManagerServiceRef = getService(bc, CustomGraphicsManager.class);
		RenderingEngineManager renderingEngineManagerServiceRef = getService(bc, RenderingEngineManager.class);
		CyRootNetworkManager cyRootNetworkFactoryServiceRef = getService(bc, CyRootNetworkManager.class);
		UndoSupport undoSupportServiceRef = getService(bc, UndoSupport.class);
		CyTableFactory cyDataTableFactoryServiceRef = getService(bc, CyTableFactory.class);
		SpacialIndex2DFactory spacialIndex2DFactoryServiceRef = getService(bc, SpacialIndex2DFactory.class);
		DialogTaskManager dialogTaskManager = getService(bc, DialogTaskManager.class);
		SubmenuTaskManager submenuTaskManager = getService(bc, SubmenuTaskManager.class);
		CyServiceRegistrar cyServiceRegistrarRef = getService(bc, CyServiceRegistrar.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc, CyNetworkManager.class);
		CyEventHelper cyEventHelperServiceRef = getService(bc, CyEventHelper.class);
		CyProperty cyPropertyServiceRef = getService(bc, CyProperty.class, "(cyPropertyName=cytoscape3.props)");
		CyNetworkTableManager cyNetworkTableManagerServiceRef = getService(bc, CyNetworkTableManager.class);
		CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc, CyNetworkViewManager.class);

		CyNetworkFactory cyNetworkFactory = getService(bc, CyNetworkFactory.class);

		DVisualLexicon dVisualLexicon = new DVisualLexicon(customGraphicsManagerServiceRef);

		ViewTaskFactoryListener vtfListener = new ViewTaskFactoryListener();

		AnnotationFactoryManager annotationFactoryManager = new AnnotationFactoryManager();


		DingGraphLOD dingGraphLOD = new DingGraphLOD(cyPropertyServiceRef, applicationManagerManagerServiceRef);
		registerService(bc, dingGraphLOD, PropertyUpdatedListener.class, new Properties());
		
		DingGraphLODAll dingGraphLODAll = new DingGraphLODAll();
		
		DingRenderingEngineFactory dingRenderingEngineFactory = new DingRenderingEngineFactory(
				cyDataTableFactoryServiceRef, cyRootNetworkFactoryServiceRef, undoSupportServiceRef,
				spacialIndex2DFactoryServiceRef, dVisualLexicon, dialogTaskManager, submenuTaskManager,
				cyServiceRegistrarRef, cyNetworkTableManagerServiceRef, cyEventHelperServiceRef,
				renderingEngineManagerServiceRef, vtfListener, annotationFactoryManager, dingGraphLOD);
		DingNavigationRenderingEngineFactory dingNavigationRenderingEngineFactory = new DingNavigationRenderingEngineFactory(
				cyServiceRegistrarServiceRef, dVisualLexicon, renderingEngineManagerServiceRef,
				applicationManagerManagerServiceRef);
		AddEdgeNodeViewTaskFactoryImpl addEdgeNodeViewTaskFactory = new AddEdgeNodeViewTaskFactoryImpl(
				cyNetworkManagerServiceRef);

		// Object Position Editor
		ObjectPositionValueEditor objectPositionValueEditor = new ObjectPositionValueEditor();
		ObjectPositionEditor objectPositionEditor = new ObjectPositionEditor(objectPositionValueEditor);

		DingViewModelFactory dingNetworkViewFactory = new DingViewModelFactory(cyDataTableFactoryServiceRef,
				cyRootNetworkFactoryServiceRef, undoSupportServiceRef, spacialIndex2DFactoryServiceRef, dVisualLexicon,
				dialogTaskManager, submenuTaskManager, cyServiceRegistrarRef, cyNetworkTableManagerServiceRef,
				cyEventHelperServiceRef, vtfListener, annotationFactoryManager, dingGraphLOD);

		// Edge Bend editor
		EdgeBendValueEditor edgeBendValueEditor = new EdgeBendValueEditor(cyNetworkFactory, dingNetworkViewFactory,
				dingRenderingEngineFactory);
		EdgeBendEditor edgeBendEditor = new EdgeBendEditor(edgeBendValueEditor);

		AnnotationFactory imageAnnotationFactory = new ImageAnnotationFactory(customGraphicsManagerServiceRef);
		AnnotationFactory shapeAnnotationFactory = new ShapeAnnotationFactory();
		AnnotationFactory textAnnotationFactory = new TextAnnotationFactory();

		AddAnnotationTaskFactory addImageTaskFactory = new AddAnnotationTaskFactory(imageAnnotationFactory);
		AddAnnotationTaskFactory addShapeTaskFactory = new AddAnnotationTaskFactory(shapeAnnotationFactory);
		AddAnnotationTaskFactory addTextTaskFactory = new AddAnnotationTaskFactory(textAnnotationFactory);

		
		Properties dingRenderingEngineFactoryProps = new Properties();
		dingRenderingEngineFactoryProps.setProperty(ID, "ding");
		registerAllServices(bc, dingRenderingEngineFactory, dingRenderingEngineFactoryProps);

		Properties dingNavigationRenderingEngineFactoryProps = new Properties();
		dingNavigationRenderingEngineFactoryProps.setProperty(ID, "dingNavigation");
		registerAllServices(bc, dingNavigationRenderingEngineFactory, dingNavigationRenderingEngineFactoryProps);

		Properties addEdgeNodeViewTaskFactoryProps = new Properties();
		addEdgeNodeViewTaskFactoryProps.setProperty(PREFERRED_ACTION, "Edge");
		addEdgeNodeViewTaskFactoryProps.setProperty(TITLE, "Add Edge");
		registerService(bc, addEdgeNodeViewTaskFactory, NodeViewTaskFactory.class, addEdgeNodeViewTaskFactoryProps);

		Properties dVisualLexiconProps = new Properties();
		dVisualLexiconProps.setProperty(ID, "ding");
		registerService(bc, dVisualLexicon, VisualLexicon.class, dVisualLexiconProps);

		final Properties positionEditorProp = new Properties();
		positionEditorProp.setProperty(ID, "objectPositionValueEditor");
		registerService(bc, objectPositionValueEditor, ValueEditor.class, positionEditorProp);

		final Properties objectPositionEditorProp = new Properties();
		objectPositionEditorProp.setProperty(ID, "objectPositionEditor");
		registerService(bc, objectPositionEditor, VisualPropertyEditor.class, objectPositionEditorProp);

		registerAllServices(bc, edgeBendValueEditor, new Properties());
		registerService(bc, edgeBendEditor, VisualPropertyEditor.class, new Properties());

		Properties dingNetworkViewFactoryServiceProps = new Properties();
		registerService(bc, dingNetworkViewFactory, CyNetworkViewFactory.class, dingNetworkViewFactoryServiceProps);

		Properties addImageTaskFactoryProps = new Properties();
		addImageTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		addImageTaskFactoryProps.setProperty(TITLE, "Add Image");
		registerService(bc, addImageTaskFactory, NetworkViewLocationTaskFactory.class, addImageTaskFactoryProps);

		Properties addShapeTaskFactoryProps = new Properties();
		addShapeTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		addShapeTaskFactoryProps.setProperty(TITLE, "Add Shape");
		registerService(bc, addShapeTaskFactory, NetworkViewLocationTaskFactory.class, addShapeTaskFactoryProps);

		Properties addTextTaskFactoryProps = new Properties();
		addTextTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		addTextTaskFactoryProps.setProperty(TITLE, "Add Text");
		registerService(bc, addTextTaskFactory, NetworkViewLocationTaskFactory.class, addTextTaskFactoryProps);

		registerServiceListener(bc, vtfListener, "addNodeViewTaskFactory", "removeNodeViewTaskFactory",
				NodeViewTaskFactory.class);
		registerServiceListener(bc, vtfListener, "addEdgeViewTaskFactory", "removeEdgeViewTaskFactory",
				EdgeViewTaskFactory.class);
		registerServiceListener(bc, vtfListener, "addNetworkViewTaskFactory", "removeNetworkViewTaskFactory",
				NetworkViewTaskFactory.class);
		registerServiceListener(bc, vtfListener, "addNetworkViewLocationTaskFactory",
				"removeNetworkViewLocationTaskFactory", NetworkViewLocationTaskFactory.class);
		registerServiceListener(bc, vtfListener, "addCyEdgeViewContextMenuFactory",
				"removeCyEdgeViewContextMenuFactory", CyEdgeViewContextMenuFactory.class);
		registerServiceListener(bc, vtfListener, "addCyNodeViewContextMenuFactory",
				"removeCyNodeViewContextMenuFactory", CyNodeViewContextMenuFactory.class);

		registerServiceListener(bc, annotationFactoryManager, "addAnnotationFactory", "removeAnnotationFactory",
				AnnotationFactory.class);

		GraphicsDetailAction graphicsDetailAction = new GraphicsDetailAction(applicationManagerManagerServiceRef,
				cyNetworkViewManagerServiceRef, dialogTaskManager, cyPropertyServiceRef, dingGraphLOD, dingGraphLODAll);
		registerAllServices(bc, graphicsDetailAction, new Properties());

		HandleFactory handleFactory = new HandleFactoryImpl();
		registerService(bc, handleFactory, HandleFactory.class, new Properties());

		BendFactory bendFactory = new BendFactoryImpl();
		registerService(bc, bendFactory, BendFactory.class, new Properties());

		// Register the factory
		dVisualLexicon.addBendFactory(bendFactory, new HashMap());

		
		// Factories for Visual Property Dependency
		final NodeSizeDependencyFactory nodeSizeDependencyFactory = new NodeSizeDependencyFactory(dVisualLexicon);
		registerService(bc, nodeSizeDependencyFactory, VisualPropertyDependencyFactory.class, new Properties());

		final EdgeColorDependencyFactory edgeColorDependencyFactory = new EdgeColorDependencyFactory(dVisualLexicon);
		registerService(bc, edgeColorDependencyFactory, VisualPropertyDependencyFactory.class, new Properties());

		final CustomGraphicsSizeDependencyFactory customGraphicsSizeDependencyFactory = new CustomGraphicsSizeDependencyFactory(dVisualLexicon);
		registerService(bc, customGraphicsSizeDependencyFactory, VisualPropertyDependencyFactory.class, new Properties());
	}

	private void startCustomGraphicsMgr(BundleContext bc) {

		DialogTaskManager dialogTaskManagerServiceRef = getService(bc, DialogTaskManager.class);
		CyProperty coreCyPropertyServiceRef = getService(bc, CyProperty.class, "(cyPropertyName=cytoscape3.props)");
		CyApplicationManager cyApplicationManagerServiceRef = getService(bc, CyApplicationManager.class);
		CyApplicationConfiguration cyApplicationConfigurationServiceRef = getService(bc,
				CyApplicationConfiguration.class);
		CyEventHelper eventHelperServiceRef = getService(bc, CyEventHelper.class);
		VisualMappingManager vmmServiceRef = getService(bc, VisualMappingManager.class);

		CustomGraphicsManagerImpl customGraphicsManager = new CustomGraphicsManagerImpl(coreCyPropertyServiceRef,
				dialogTaskManagerServiceRef, cyApplicationConfigurationServiceRef, eventHelperServiceRef,
				vmmServiceRef, cyApplicationManagerServiceRef, getdefaultImageURLs(bc));
		CustomGraphicsBrowser browser = new CustomGraphicsBrowser(customGraphicsManager);
		registerAllServices(bc, browser, new Properties());

		CustomGraphicsManagerAction customGraphicsManagerAction = new CustomGraphicsManagerAction(
				customGraphicsManager, cyApplicationManagerServiceRef, browser);

		registerAllServices(bc, customGraphicsManager, new Properties());
		registerService(bc, customGraphicsManagerAction, CyAction.class, new Properties());
	}
	
	/**
	 * Get list of default images from resource.
	 * 
	 * @param bc
	 * @return Set of default image files in the bundle
	 */
	private Set<URL> getdefaultImageURLs(BundleContext bc) {
		Enumeration<URL> e = bc.getBundle().findEntries("images/sampleCustomGraphics", "*.png", true);
		final Set<URL> defaultImageUrls = new HashSet<URL>();
		while (e.hasMoreElements())
			defaultImageUrls.add(e.nextElement());
		
		return defaultImageUrls;
	}

	private void startSpacial(BundleContext bc) {
		RTreeFactory rtreeFactory = new RTreeFactory();
		registerService(bc,rtreeFactory,SpacialIndex2DFactory.class, new Properties());
	}
}
