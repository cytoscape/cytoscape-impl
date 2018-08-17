package org.cytoscape.ding;

import static org.cytoscape.work.ServiceProperties.ACCELERATOR;
import static org.cytoscape.work.ServiceProperties.ID;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_AFTER;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;
import static org.cytoscape.work.ServiceProperties.IN_CONTEXT_MENU;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.IN_NETWORK_PANEL_CONTEXT_MENU;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.NETWORK_ADD_MENU;
import static org.cytoscape.work.ServiceProperties.NETWORK_DELETE_MENU;
import static org.cytoscape.work.ServiceProperties.NETWORK_EDIT_MENU;
import static org.cytoscape.work.ServiceProperties.NETWORK_GROUP_MENU;
import static org.cytoscape.work.ServiceProperties.NODE_ADD_MENU;
import static org.cytoscape.work.ServiceProperties.PREFERRED_ACTION;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CyEdgeViewContextMenuFactory;
import org.cytoscape.application.swing.CyNetworkViewContextMenuFactory;
import org.cytoscape.application.swing.CyNodeViewContextMenuFactory;
import org.cytoscape.ding.action.GraphicsDetailAction;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.customgraphics.CustomGraphicsTranslator;
import org.cytoscape.ding.customgraphics.CyCustomGraphics2Manager;
import org.cytoscape.ding.customgraphics.CyCustomGraphics2ManagerImpl;
import org.cytoscape.ding.customgraphics.bitmap.URLImageCustomGraphicsFactory;
import org.cytoscape.ding.customgraphics.vector.GradientOvalFactory;
import org.cytoscape.ding.customgraphics.vector.GradientRoundRectangleFactory;
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
import org.cytoscape.ding.impl.DingRenderer;
import org.cytoscape.ding.impl.DingRenderingEngineFactory;
import org.cytoscape.ding.impl.DingThumbnailRenderingEngineFactory;
import org.cytoscape.ding.impl.DingViewModelFactory;
import org.cytoscape.ding.impl.DingVisualStyleRenderingEngineFactory;
import org.cytoscape.ding.impl.HandleFactoryImpl;
import org.cytoscape.ding.impl.NVLTFActionSupport;
import org.cytoscape.ding.impl.ViewTaskFactoryListener;
// Annotation creation
import org.cytoscape.ding.impl.cyannotator.AnnotationFactoryManager;
import org.cytoscape.ding.impl.cyannotator.AnnotationManagerImpl;
import org.cytoscape.ding.impl.cyannotator.create.ArrowAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.BoundedTextAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.GroupAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.ImageAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.ShapeAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.TextAnnotationFactory;
// Annotation edits and changes
import org.cytoscape.ding.impl.cyannotator.tasks.AddAnnotationTaskFactory;
import org.cytoscape.ding.impl.cyannotator.tasks.AddArrowTaskFactory;
import org.cytoscape.ding.impl.cyannotator.tasks.EditAnnotationTaskFactory;
import org.cytoscape.ding.impl.cyannotator.tasks.GroupAnnotationsTaskFactory;
import org.cytoscape.ding.impl.cyannotator.tasks.RemoveAnnotationTaskFactory;
import org.cytoscape.ding.impl.cyannotator.tasks.ReorderSelectedAnnotationsTaskFactory;
import org.cytoscape.ding.impl.cyannotator.tasks.UngroupAnnotationsTaskFactory;
import org.cytoscape.ding.impl.cyannotator.ui.AnnotationMainPanel;
import org.cytoscape.ding.impl.cyannotator.ui.AnnotationMediator;
import org.cytoscape.ding.impl.editor.CustomGraphicsVisualPropertyEditor;
import org.cytoscape.ding.impl.editor.CyCustomGraphicsValueEditor;
import org.cytoscape.ding.impl.editor.EdgeBendEditor;
import org.cytoscape.ding.impl.editor.EdgeBendValueEditor;
import org.cytoscape.ding.impl.editor.ObjectPositionEditor;
import org.cytoscape.ding.internal.charts.bar.BarChartFactory;
import org.cytoscape.ding.internal.charts.box.BoxChartFactory;
import org.cytoscape.ding.internal.charts.heatmap.HeatMapChartFactory;
import org.cytoscape.ding.internal.charts.line.LineChartFactory;
import org.cytoscape.ding.internal.charts.pie.PieChartFactory;
import org.cytoscape.ding.internal.charts.ring.RingChartFactory;
import org.cytoscape.ding.internal.gradients.linear.LinearGradientFactory;
import org.cytoscape.ding.internal.gradients.radial.RadialGradientFactory;
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
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2Factory;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.presentation.property.values.HandleFactory;
import org.cytoscape.view.vizmap.VisualPropertyDependencyFactory;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingCellRendererFactory;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyEditor;
import org.cytoscape.view.vizmap.mappings.ValueTranslator;
import org.osgi.framework.BundleContext;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

public class CyActivator extends AbstractCyActivator {
	
	/** View Context Menu - Reorder Right-Clicked Annotation */
	private static final String REORDER_ANNOTATION_MENU = NETWORK_EDIT_MENU + ".Reorder Annotation[2.2]";
	
	private CustomGraphicsManager cgManager;
	private CyCustomGraphics2Manager cg2Manager;
	private CustomGraphicsBrowser cgBrowser;
	
	@Override
	public void start(BundleContext bc) {
		final CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);
		
		startSpacial(bc, serviceRegistrar); 
		startCustomGraphicsMgr(bc, serviceRegistrar);
		startCharts(bc, serviceRegistrar);
		startGradients(bc, serviceRegistrar);
		startPresentationImpl(bc, serviceRegistrar);
	}

	private void startPresentationImpl(final BundleContext bc, final CyServiceRegistrar serviceRegistrar) {
		DVisualLexicon dVisualLexicon = new DVisualLexicon(cgManager);

		NVLTFActionSupport nvltfActionSupport = new NVLTFActionSupport(serviceRegistrar);
		ViewTaskFactoryListener vtfListener = new ViewTaskFactoryListener(nvltfActionSupport);

		AnnotationFactoryManager annotationFactoryManager = new AnnotationFactoryManager();
		AnnotationManager annotationManager = new AnnotationManagerImpl(serviceRegistrar);

		DingGraphLOD dingGraphLOD = new DingGraphLOD(serviceRegistrar);
		registerService(bc, dingGraphLOD, PropertyUpdatedListener.class);
		
		DingGraphLODAll dingGraphLODAll = new DingGraphLODAll();
		
		HandleFactory handleFactory = new HandleFactoryImpl();
		registerService(bc, handleFactory, HandleFactory.class);
		
		DingRenderingEngineFactory dingRenderingEngineFactory =
				new DingRenderingEngineFactory(dVisualLexicon, vtfListener, annotationFactoryManager, dingGraphLOD,
						handleFactory, serviceRegistrar);
		DingNavigationRenderingEngineFactory dingNavigationRenderingEngineFactory =
				new DingNavigationRenderingEngineFactory(serviceRegistrar, dVisualLexicon);
		DingRenderingEngineFactory dingVisualStyleRenderingEngineFactory =
				new DingVisualStyleRenderingEngineFactory(dVisualLexicon, vtfListener, annotationFactoryManager,
						dingGraphLOD, handleFactory, serviceRegistrar);
		DingThumbnailRenderingEngineFactory dingThumbnailRenderingEngineFactory =
				new DingThumbnailRenderingEngineFactory(dVisualLexicon, serviceRegistrar);
		
		AddEdgeNodeViewTaskFactoryImpl addEdgeNodeViewTaskFactory = new AddEdgeNodeViewTaskFactoryImpl(serviceRegistrar);

		ContinuousMappingCellRendererFactory continuousMappingCellRendererFactory = getService(bc, ContinuousMappingCellRendererFactory.class);

		// Object Position Editor
		ObjectPositionValueEditor objectPositionValueEditor = new ObjectPositionValueEditor();
		ObjectPositionEditor objectPositionEditor =
				new ObjectPositionEditor(objectPositionValueEditor, continuousMappingCellRendererFactory, serviceRegistrar);

		DingViewModelFactory dingNetworkViewFactory = new DingViewModelFactory(dVisualLexicon, vtfListener,
				annotationFactoryManager, dingGraphLOD, handleFactory, serviceRegistrar);

		DingRenderer renderer = DingRenderer.getInstance();
		renderer.registerNetworkViewFactory(dingNetworkViewFactory);
		renderer.registerRenderingEngineFactory(NetworkViewRenderer.DEFAULT_CONTEXT, dingRenderingEngineFactory);
		renderer.registerRenderingEngineFactory(NetworkViewRenderer.BIRDS_EYE_CONTEXT, dingNavigationRenderingEngineFactory);
		renderer.registerRenderingEngineFactory(NetworkViewRenderer.VISUAL_STYLE_PREVIEW_CONTEXT, dingVisualStyleRenderingEngineFactory);
		renderer.registerRenderingEngineFactory(NetworkViewRenderer.THUMBNAIL_CONTEXT, dingThumbnailRenderingEngineFactory);
		registerService(bc, renderer, NetworkViewRenderer.class);
		
		// Edge Bend editor
		EdgeBendValueEditor edgeBendValueEditor =
				new EdgeBendValueEditor(dingNetworkViewFactory, dingRenderingEngineFactory, serviceRegistrar);
		EdgeBendEditor edgeBendEditor =
				new EdgeBendEditor(edgeBendValueEditor, continuousMappingCellRendererFactory, serviceRegistrar);

		
		Properties dingRenderingEngineFactoryProps = new Properties();
		dingRenderingEngineFactoryProps.setProperty(ID, "ding");
		registerAllServices(bc, dingRenderingEngineFactory, dingRenderingEngineFactoryProps);

		Properties dingNavigationRenderingEngineFactoryProps = new Properties();
		dingNavigationRenderingEngineFactoryProps.setProperty(ID, "dingNavigation");
		registerAllServices(bc, dingNavigationRenderingEngineFactory, dingNavigationRenderingEngineFactoryProps);

		Properties addEdgeNodeViewTaskFactoryProps = new Properties();
		addEdgeNodeViewTaskFactoryProps.setProperty(PREFERRED_ACTION, "Edge");
		addEdgeNodeViewTaskFactoryProps.setProperty(PREFERRED_MENU, NODE_ADD_MENU);
		addEdgeNodeViewTaskFactoryProps.setProperty(TITLE, "Edge");
		addEdgeNodeViewTaskFactoryProps.setProperty(MENU_GRAVITY, "0.1");
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

		registerAllServices(bc, edgeBendValueEditor);
		registerService(bc, edgeBendEditor, VisualPropertyEditor.class);

		Properties dingNetworkViewFactoryServiceProps = new Properties();
		registerService(bc, dingNetworkViewFactory, CyNetworkViewFactory.class, dingNetworkViewFactoryServiceProps);

		// Annotation Manager
		registerServiceListener(bc, annotationFactoryManager::addAnnotationFactory, annotationFactoryManager::removeAnnotationFactory, AnnotationFactory.class);
		registerService(bc, annotationManager, AnnotationManager.class);

		// Annotations UI
		AnnotationMainPanel annotationMainPanel = new AnnotationMainPanel(serviceRegistrar);
		registerAllServices(bc, annotationMainPanel);
		
		AnnotationMediator annotationMediator = new AnnotationMediator(annotationMainPanel, serviceRegistrar);
		registerServiceListener(bc, annotationMediator::addAnnotationFactory, annotationMediator::removeAnnotationFactory, AnnotationFactory.class);
		registerAllServices(bc, annotationMediator);
		
		// Annotation Factories (the order they are registered is the order they appear in the UI)
		AnnotationFactory<?> textAnnotationFactory = new TextAnnotationFactory(serviceRegistrar);
		Properties textFactory = new Properties();
		textFactory.setProperty("type","TextAnnotation.class");
		registerService(bc, textAnnotationFactory, AnnotationFactory.class, textFactory);
		
		AnnotationFactory<?> boundedAnnotationFactory = new BoundedTextAnnotationFactory(serviceRegistrar);
		Properties boundedFactory = new Properties();
		boundedFactory.setProperty("type","BoundedTextAnnotation.class");
		registerService(bc, boundedAnnotationFactory, AnnotationFactory.class, boundedFactory);
		
		AnnotationFactory<?> shapeAnnotationFactory = new ShapeAnnotationFactory(serviceRegistrar);
		Properties shapeFactory = new Properties();
		shapeFactory.setProperty("type","ShapeAnnotation.class");
		registerService(bc, shapeAnnotationFactory, AnnotationFactory.class, shapeFactory);
		
		AnnotationFactory<?> imageAnnotationFactory = new ImageAnnotationFactory(serviceRegistrar);
		Properties imageFactory = new Properties();
		imageFactory.setProperty("type","ImageAnnotation.class");
		registerService(bc, imageAnnotationFactory, AnnotationFactory.class, imageFactory);
		
		AnnotationFactory<?> arrowAnnotationFactory = new ArrowAnnotationFactory(serviceRegistrar);
		Properties arrowFactory = new Properties();
		arrowFactory.setProperty("type","ArrowAnnotation.class");
		registerService(bc, arrowAnnotationFactory, AnnotationFactory.class, arrowFactory);
		
		AnnotationFactory<?> groupAnnotationFactory = new GroupAnnotationFactory(serviceRegistrar);
		Properties groupFactory = new Properties();
		groupFactory.setProperty("type","GroupAnnotation.class");
		registerService(bc, groupAnnotationFactory, AnnotationFactory.class, groupFactory);
		
		// Annotation Task Factories
		AddArrowTaskFactory addArrowTaskFactory = new AddArrowTaskFactory(arrowAnnotationFactory);
		Properties addArrowTaskFactoryProps = new Properties();
		addArrowTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		addArrowTaskFactoryProps.setProperty(PREFERRED_MENU, NETWORK_ADD_MENU);
		addArrowTaskFactoryProps.setProperty(MENU_GRAVITY, "1.2");
		addArrowTaskFactoryProps.setProperty(TITLE, "Arrow Annotation");
		registerService(bc, addArrowTaskFactory, NetworkViewLocationTaskFactory.class, addArrowTaskFactoryProps);

		AddAnnotationTaskFactory addImageTaskFactory = new AddAnnotationTaskFactory(imageAnnotationFactory);
		Properties addImageTaskFactoryProps = new Properties();
		addImageTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		addImageTaskFactoryProps.setProperty(PREFERRED_MENU, NETWORK_ADD_MENU);
		addImageTaskFactoryProps.setProperty(MENU_GRAVITY, "1.3");
		addImageTaskFactoryProps.setProperty(TITLE, "Image Annotation");
		registerService(bc, addImageTaskFactory, NetworkViewLocationTaskFactory.class, addImageTaskFactoryProps);

		AddAnnotationTaskFactory addShapeTaskFactory = new AddAnnotationTaskFactory(shapeAnnotationFactory);
		Properties addShapeTaskFactoryProps = new Properties();
		addShapeTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		addShapeTaskFactoryProps.setProperty(PREFERRED_MENU, NETWORK_ADD_MENU);
		addShapeTaskFactoryProps.setProperty(MENU_GRAVITY, "1.4");
		addShapeTaskFactoryProps.setProperty(TITLE, "Shape Annotation");
		registerService(bc, addShapeTaskFactory, NetworkViewLocationTaskFactory.class, addShapeTaskFactoryProps);

		AddAnnotationTaskFactory addTextTaskFactory = new AddAnnotationTaskFactory(textAnnotationFactory);
		Properties addTextTaskFactoryProps = new Properties();
		addTextTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		addTextTaskFactoryProps.setProperty(MENU_GRAVITY, "1.5");
		addTextTaskFactoryProps.setProperty(PREFERRED_MENU, NETWORK_ADD_MENU);
		addTextTaskFactoryProps.setProperty(TITLE, "Text Annotation");
		registerService(bc, addTextTaskFactory, NetworkViewLocationTaskFactory.class, addTextTaskFactoryProps);

		AddAnnotationTaskFactory addBoundedTextTaskFactory =  new AddAnnotationTaskFactory(boundedAnnotationFactory);
		Properties addBoundedTextTaskFactoryProps = new Properties();
		addBoundedTextTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		addBoundedTextTaskFactoryProps.setProperty(MENU_GRAVITY, "1.6");
		addBoundedTextTaskFactoryProps.setProperty(PREFERRED_MENU, NETWORK_ADD_MENU);
		addBoundedTextTaskFactoryProps.setProperty(TITLE, "Bounded Text Annotation");
		registerService(bc, addBoundedTextTaskFactory, NetworkViewLocationTaskFactory.class, 
		                addBoundedTextTaskFactoryProps);

		// Annotation edit
		EditAnnotationTaskFactory editAnnotationTaskFactory = new EditAnnotationTaskFactory();
		Properties editAnnotationTaskFactoryProps = new Properties();
		editAnnotationTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		editAnnotationTaskFactoryProps.setProperty(MENU_GRAVITY, "2.0");
		editAnnotationTaskFactoryProps.setProperty(PREFERRED_MENU, NETWORK_EDIT_MENU);
		editAnnotationTaskFactoryProps.setProperty(TITLE, "Modify Annotation");
		registerService(bc, editAnnotationTaskFactory, NetworkViewLocationTaskFactory.class, 
		                editAnnotationTaskFactoryProps);

		/*
		MoveAnnotationTaskFactory moveAnnotationTaskFactory = new MoveAnnotationTaskFactory();
		Properties moveAnnotationTaskFactoryProps = new Properties();
		moveAnnotationTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		moveAnnotationTaskFactoryProps.setProperty(MENU_GRAVITY, "2.1");
		moveAnnotationTaskFactoryProps.setProperty(PREFERRED_MENU, NETWORK_EDIT_MENU);
		moveAnnotationTaskFactoryProps.setProperty(TITLE, "Move Annotation");
		registerService(bc, moveAnnotationTaskFactory, NetworkViewLocationTaskFactory.class, 
		                moveAnnotationTaskFactoryProps);
		*/

		// Reorder Selected Annotations - Edit Menu
		{
			ReorderSelectedAnnotationsTaskFactory factory = new ReorderSelectedAnnotationsTaskFactory(Integer.MIN_VALUE);
			Properties props = new Properties();
			props.setProperty(PREFERRED_MENU, NETWORK_EDIT_MENU);
			props.setProperty(TITLE, "Bring Annotations to Front");
			props.setProperty(ACCELERATOR, "shift cmd CLOSE_BRACKET");
			props.setProperty(MENU_GRAVITY, "6.1");
			props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
		}
		{
			ReorderSelectedAnnotationsTaskFactory factory = new ReorderSelectedAnnotationsTaskFactory(-1);
			Properties props = new Properties();
			props.setProperty(PREFERRED_MENU, NETWORK_EDIT_MENU);
			props.setProperty(TITLE, "Bring Annotations Forward");
			props.setProperty(ACCELERATOR, "cmd CLOSE_BRACKET");
			props.setProperty(MENU_GRAVITY, "6.2");
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
		}
		{
			ReorderSelectedAnnotationsTaskFactory factory = new ReorderSelectedAnnotationsTaskFactory(1);
			Properties props = new Properties();
			props.setProperty(PREFERRED_MENU, NETWORK_EDIT_MENU);
			props.setProperty(TITLE, "Send Annotations Backward");
			props.setProperty(ACCELERATOR, "cmd OPEN_BRACKET");
			props.setProperty(MENU_GRAVITY, "6.3");
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
		}
		{
			ReorderSelectedAnnotationsTaskFactory factory = new ReorderSelectedAnnotationsTaskFactory(Integer.MAX_VALUE);
			Properties props = new Properties();
			props.setProperty(PREFERRED_MENU, NETWORK_EDIT_MENU);
			props.setProperty(TITLE, "Send Annotations to Back");
			props.setProperty(ACCELERATOR, "shift cmd OPEN_BRACKET");
			props.setProperty(MENU_GRAVITY, "6.4");
			props.setProperty(INSERT_SEPARATOR_AFTER, "true");
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
		}
		{
			ReorderSelectedAnnotationsTaskFactory factory = new ReorderSelectedAnnotationsTaskFactory(Annotation.FOREGROUND);
			Properties props = new Properties();
			props.setProperty(PREFERRED_MENU, NETWORK_EDIT_MENU);
			props.setProperty(MENU_GRAVITY, "6.5");
			props.setProperty(TITLE, "Pull Annotations to Foreground Layer");
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
		}
		{
			ReorderSelectedAnnotationsTaskFactory factory = new ReorderSelectedAnnotationsTaskFactory(Annotation.BACKGROUND);
			Properties props = new Properties();
			props.setProperty(PREFERRED_MENU, NETWORK_EDIT_MENU);
			props.setProperty(TITLE, "Push Annotations to Background Layer");
			props.setProperty(MENU_GRAVITY, "6.6");
			props.setProperty(INSERT_SEPARATOR_AFTER, "true");
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
		}
		
		/*
		ResizeAnnotationTaskFactory resizeAnnotationTaskFactory = new ResizeAnnotationTaskFactory();
		Properties resizeAnnotationTaskFactoryProps = new Properties();
		resizeAnnotationTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		resizeAnnotationTaskFactoryProps.setProperty(MENU_GRAVITY, "2.3");
		resizeAnnotationTaskFactoryProps.setProperty(PREFERRED_MENU, NETWORK_EDIT_MENU);
		resizeAnnotationTaskFactoryProps.setProperty(TITLE, "Resize Annotation");
		registerService(bc, resizeAnnotationTaskFactory, NetworkViewLocationTaskFactory.class, 
		                resizeAnnotationTaskFactoryProps);
		*/

		// Annotation delete
		RemoveAnnotationTaskFactory removeAnnotationTaskFactory = new RemoveAnnotationTaskFactory();
		Properties removeAnnotationTaskFactoryProps = new Properties();
		removeAnnotationTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		removeAnnotationTaskFactoryProps.setProperty(MENU_GRAVITY, "1.1");
		removeAnnotationTaskFactoryProps.setProperty(PREFERRED_MENU, NETWORK_DELETE_MENU);
		removeAnnotationTaskFactoryProps.setProperty(TITLE, "Annotation");
		registerService(bc, removeAnnotationTaskFactory, NetworkViewLocationTaskFactory.class, 
		                removeAnnotationTaskFactoryProps);

		/*
		// Annotation select
		SelectAnnotationTaskFactory selectAnnotationTaskFactory = new SelectAnnotationTaskFactory();
		Properties selectAnnotationTaskFactoryProps = new Properties();
		selectAnnotationTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		selectAnnotationTaskFactoryProps.setProperty(MENU_GRAVITY, "1.1");
		selectAnnotationTaskFactoryProps.setProperty(PREFERRED_MENU, NETWORK_SELECT_MENU);
		selectAnnotationTaskFactoryProps.setProperty(TITLE, "Select/Unselect Annotation");
		registerService(bc, selectAnnotationTaskFactory, NetworkViewLocationTaskFactory.class, 
		                selectAnnotationTaskFactoryProps);
		*/

		// Annotation group
		GroupAnnotationsTaskFactory groupAnnotationTaskFactory = new GroupAnnotationsTaskFactory();
		Properties groupAnnotationTaskFactoryProps = new Properties();
		groupAnnotationTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		groupAnnotationTaskFactoryProps.setProperty(MENU_GRAVITY, "100");
		groupAnnotationTaskFactoryProps.setProperty(INSERT_SEPARATOR_BEFORE, "true");
		groupAnnotationTaskFactoryProps.setProperty(IN_MENU_BAR, "false");
		groupAnnotationTaskFactoryProps.setProperty(PREFERRED_MENU, NETWORK_GROUP_MENU);
		groupAnnotationTaskFactoryProps.setProperty(TITLE, "Group Annotations");
		registerService(bc, groupAnnotationTaskFactory, NetworkViewTaskFactory.class, 
		                groupAnnotationTaskFactoryProps);

		// Annotation ungroup
		UngroupAnnotationsTaskFactory ungroupAnnotationTaskFactory = new UngroupAnnotationsTaskFactory();
		Properties ungroupAnnotationTaskFactoryProps = new Properties();
		ungroupAnnotationTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		ungroupAnnotationTaskFactoryProps.setProperty(MENU_GRAVITY, "100");
		ungroupAnnotationTaskFactoryProps.setProperty(INSERT_SEPARATOR_BEFORE, "true");
		ungroupAnnotationTaskFactoryProps.setProperty(PREFERRED_MENU, NETWORK_GROUP_MENU);
		ungroupAnnotationTaskFactoryProps.setProperty(TITLE, "Ungroup Annotations");
		registerService(bc, ungroupAnnotationTaskFactory, NetworkViewLocationTaskFactory.class, 
		                ungroupAnnotationTaskFactoryProps);

		// Set mouse drag selection modes
		SelectModeAction selectNodesOnlyAction = new SelectModeAction(SelectModeAction.NODES, 0.5f, serviceRegistrar);
		registerAllServices(bc, selectNodesOnlyAction);
		
		SelectModeAction selectEdgesOnlyAction = new SelectModeAction(SelectModeAction.EDGES, 0.6f, serviceRegistrar);
		registerAllServices(bc, selectEdgesOnlyAction);
		
		SelectModeAction selectAnnotationsOnlyAction = new SelectModeAction(SelectModeAction.ANNOTATIONS, 0.7f, serviceRegistrar);
		registerAllServices(bc, selectAnnotationsOnlyAction);
		
		SelectModeAction selectNodesEdgesAction = new SelectModeAction(SelectModeAction.NODES_EDGES, 0.8f, serviceRegistrar);
		registerAllServices(bc, selectNodesEdgesAction);

		SelectModeAction selectAllAction = new SelectModeAction(SelectModeAction.ALL, 0.9f, serviceRegistrar);
		registerAllServices(bc, selectAllAction);
		
		{
			// Toggle Graphics Details
			ShowGraphicsDetailsTaskFactory factory = new ShowGraphicsDetailsTaskFactory(dingGraphLOD, dingGraphLODAll);
			Properties props = new Properties();
			props.setProperty(ID, "showGraphicsDetailsTaskFactory");
			registerService(bc, factory, NetworkViewTaskFactory.class, props); // Used at least by cyREST
			
			// Main menu
			GraphicsDetailAction mainMenuAction = new GraphicsDetailAction(5.0f, "View", factory, serviceRegistrar);
			registerAllServices(bc, mainMenuAction);
			
			// Network tab's context menu
			GraphicsDetailAction networkMenuAction = new GraphicsDetailAction(11.0f, null, factory, serviceRegistrar);
			props = new Properties();
			props.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU, "true");
			registerAllServices(bc, networkMenuAction, props);
		}

		final String vtfFilter = String.format("(| (!(%s=*)) (%s=true))", IN_CONTEXT_MENU, IN_CONTEXT_MENU); // if IN_CONTEXT_MENU is not specified, default to true
		registerServiceListener(bc, vtfListener::addNodeViewTaskFactory, vtfListener::removeNodeViewTaskFactory, NodeViewTaskFactory.class, vtfFilter);
		registerServiceListener(bc, vtfListener::addEdgeViewTaskFactory, vtfListener::removeEdgeViewTaskFactory, EdgeViewTaskFactory.class, vtfFilter);
		registerServiceListener(bc, vtfListener::addNetworkViewTaskFactory, vtfListener::removeNetworkViewTaskFactory, NetworkViewTaskFactory.class, vtfFilter);
		registerServiceListener(bc, vtfListener::addNetworkViewLocationTaskFactory, vtfListener::removeNetworkViewLocationTaskFactory, NetworkViewLocationTaskFactory.class);
		registerServiceListener(bc, vtfListener::addCyEdgeViewContextMenuFactory, vtfListener::removeCyEdgeViewContextMenuFactory, CyEdgeViewContextMenuFactory.class);
		registerServiceListener(bc, vtfListener::addCyNodeViewContextMenuFactory, vtfListener::removeCyNodeViewContextMenuFactory, CyNodeViewContextMenuFactory.class);
		registerServiceListener(bc, vtfListener::addCyNetworkViewContextMenuFactory, vtfListener::removeCyNetworkViewContextMenuFactory, CyNetworkViewContextMenuFactory.class);

		registerServiceListener(bc, annotationFactoryManager::addAnnotationFactory, annotationFactoryManager::removeAnnotationFactory, AnnotationFactory.class);

		BendFactory bendFactory = new BendFactoryImpl();
		registerService(bc, bendFactory, BendFactory.class);

		// Register the factory
		dVisualLexicon.addBendFactory(bendFactory, new HashMap<Object, Object>());
		
		// Translators for Passthrough
		final CustomGraphicsTranslator cgTranslator = new CustomGraphicsTranslator(cgManager, cg2Manager);
		registerService(bc, cgTranslator, ValueTranslator.class);

		// Factories for Visual Property Dependency
		final NodeSizeDependencyFactory nodeSizeDependencyFactory = new NodeSizeDependencyFactory(dVisualLexicon);
		registerService(bc, nodeSizeDependencyFactory, VisualPropertyDependencyFactory.class);

		final EdgeColorDependencyFactory edgeColorDependencyFactory = new EdgeColorDependencyFactory(dVisualLexicon);
		registerService(bc, edgeColorDependencyFactory, VisualPropertyDependencyFactory.class);

		final CustomGraphicsSizeDependencyFactory cgSizeDependencyFactory = new CustomGraphicsSizeDependencyFactory(
				dVisualLexicon);
		registerService(bc, cgSizeDependencyFactory, VisualPropertyDependencyFactory.class);

		// Custom Graphics Editors
		final CyCustomGraphicsValueEditor cgValueEditor = new CyCustomGraphicsValueEditor(cgManager, cg2Manager,
				cgBrowser, serviceRegistrar);
		registerAllServices(bc, cgValueEditor);

		final CustomGraphicsVisualPropertyEditor cgVisualPropertyEditor = new CustomGraphicsVisualPropertyEditor(
				CyCustomGraphics.class, cgValueEditor, continuousMappingCellRendererFactory, serviceRegistrar);
		registerService(bc, cgVisualPropertyEditor, VisualPropertyEditor.class);
	}

	private void startCustomGraphicsMgr(final BundleContext bc, final CyServiceRegistrar serviceRegistrar) {
		cgManager = new CustomGraphicsManagerImpl(getdefaultImageURLs(bc), serviceRegistrar);
		registerAllServices(bc, cgManager);

		cgBrowser = new CustomGraphicsBrowser(cgManager);
		registerAllServices(bc, cgBrowser);

		CustomGraphicsManagerAction cgManagerAction = new CustomGraphicsManagerAction(cgManager, cgBrowser,
				serviceRegistrar);

		registerService(bc, cgManagerAction, CyAction.class);

		// Create and register our built-in factories.
		// TODO:  When the CustomGraphicsFactory service stuff is set up, just
		// register these as services
		URLImageCustomGraphicsFactory imageFactory = new URLImageCustomGraphicsFactory(cgManager);
		cgManager.addCustomGraphicsFactory(imageFactory, new Properties());

		GradientOvalFactory ovalFactory = new GradientOvalFactory(cgManager);
		cgManager.addCustomGraphicsFactory(ovalFactory, new Properties());

		GradientRoundRectangleFactory rectangleFactory = 
		     new GradientRoundRectangleFactory(cgManager);
		cgManager.addCustomGraphicsFactory(rectangleFactory, new Properties());

		// Register this service listener so that app writers can provide their own CustomGraphics factories
		registerServiceListener(bc, cgManager::addCustomGraphicsFactory, cgManager::removeCustomGraphicsFactory,
				CyCustomGraphicsFactory.class);
		
		// Register this service listener so that app writers can provide their own CyCustomGraphics2 factories
		cg2Manager = CyCustomGraphics2ManagerImpl.getInstance();
		registerAllServices(bc, cg2Manager);
		registerServiceListener(bc, ((CyCustomGraphics2ManagerImpl)cg2Manager)::addFactory, 
				((CyCustomGraphics2ManagerImpl)cg2Manager)::removeFactory, CyCustomGraphics2Factory.class);
	}
	
	private void startCharts(final BundleContext bc, final CyServiceRegistrar serviceRegistrar) {
		// Register Chart Factories
		final Properties factoryProps = new Properties();
		factoryProps.setProperty(CyCustomGraphics2Factory.GROUP, CyCustomGraphics2Manager.GROUP_CHARTS);
		{
			final BarChartFactory factory = new BarChartFactory(serviceRegistrar);
			registerService(bc, factory, CyCustomGraphics2Factory.class, factoryProps);
		}
		{
			final BoxChartFactory factory = new BoxChartFactory(serviceRegistrar);
			registerService(bc, factory, CyCustomGraphics2Factory.class, factoryProps);
		}
		{
			final PieChartFactory factory = new PieChartFactory(serviceRegistrar);
			registerService(bc, factory, CyCustomGraphics2Factory.class, factoryProps);
		}
		{
			final RingChartFactory factory = new RingChartFactory(serviceRegistrar);
			registerService(bc, factory, CyCustomGraphics2Factory.class, factoryProps);
		}
		{
			final LineChartFactory factory = new LineChartFactory(serviceRegistrar);
			registerService(bc, factory, CyCustomGraphics2Factory.class, factoryProps);
		}
		{
			final HeatMapChartFactory factory = new HeatMapChartFactory(serviceRegistrar);
			registerService(bc, factory, CyCustomGraphics2Factory.class, factoryProps);
		}
	}
	
	private void startGradients(final BundleContext bc, final CyServiceRegistrar serviceRegistrar) {
		// Register Gradient Factories
		final Properties factoryProps = new Properties();
		factoryProps.setProperty(CyCustomGraphics2Factory.GROUP, CyCustomGraphics2Manager.GROUP_GRADIENTS);
		{
			final LinearGradientFactory factory = new LinearGradientFactory();
			registerService(bc, factory, CyCustomGraphics2Factory.class, factoryProps);
		}
		{
			final RadialGradientFactory factory = new RadialGradientFactory();
			registerService(bc, factory, CyCustomGraphics2Factory.class, factoryProps);
		}
	}
	
	/**
	 * Get list of default images from resource.
	 */
	private Set<URL> getdefaultImageURLs(final BundleContext bc) {
		Enumeration<URL> e = bc.getBundle().findEntries("images/sampleCustomGraphics", "*.png", true);
		final Set<URL> defaultImageUrls = new HashSet<>();
		
		while (e.hasMoreElements())
			defaultImageUrls.add(e.nextElement());
		
		return defaultImageUrls;
	}

	private void startSpacial(final BundleContext bc, final CyServiceRegistrar serviceRegistrar) {
		RTreeFactory rtreeFactory = new RTreeFactory();
		registerService(bc, rtreeFactory, SpacialIndex2DFactory.class);
	}
}
