package org.cytoscape.ding;

import static org.cytoscape.work.ServiceProperties.*;

import java.net.URL;
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
import org.cytoscape.ding.customgraphics.image.BitmapCustomGraphicsFactory;
import org.cytoscape.ding.customgraphics.image.SVGCustomGraphicsFactory;
import org.cytoscape.ding.customgraphics.vector.GradientOvalFactory;
import org.cytoscape.ding.customgraphics.vector.GradientRoundRectangleFactory;
import org.cytoscape.ding.customgraphicsmgr.internal.CustomGraphicsManagerImpl;
import org.cytoscape.ding.customgraphicsmgr.internal.action.CustomGraphicsManagerAction;
import org.cytoscape.ding.customgraphicsmgr.internal.ui.CustomGraphicsBrowser;
import org.cytoscape.ding.debug.DingDebugMediator;
import org.cytoscape.ding.dependency.CustomGraphicsSizeDependencyFactory;
import org.cytoscape.ding.dependency.EdgeColorDependencyFactory;
import org.cytoscape.ding.dependency.NodeSizeDependencyFactory;
import org.cytoscape.ding.impl.AddEdgeNodeViewTaskFactoryImpl;
import org.cytoscape.ding.impl.BendFactoryImpl;
import org.cytoscape.ding.impl.DingGraphLOD;
import org.cytoscape.ding.impl.DingNetworkViewFactory;
import org.cytoscape.ding.impl.DingRenderer;
import org.cytoscape.ding.impl.HandleFactoryImpl;
import org.cytoscape.ding.impl.NVLTFActionSupport;
import org.cytoscape.ding.impl.ViewTaskFactoryListener;
import org.cytoscape.ding.impl.canvas.ThumbnailFactoryImpl;
import org.cytoscape.ding.impl.cyannotator.AnnotationClipboard;
// Annotation creation
import org.cytoscape.ding.impl.cyannotator.AnnotationFactoryManager;
import org.cytoscape.ding.impl.cyannotator.AnnotationManagerImpl;
import org.cytoscape.ding.impl.cyannotator.AnnotationTree.Shift;
import org.cytoscape.ding.impl.cyannotator.create.ArrowAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.BoundedTextAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.GroupAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.ImageAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.ShapeAnnotationFactory;
import org.cytoscape.ding.impl.cyannotator.create.TextAnnotationFactory;
// Annotation edits and changes
import org.cytoscape.ding.impl.cyannotator.tasks.AddAnnotationTaskFactory;
import org.cytoscape.ding.impl.cyannotator.tasks.CopyAnnotationStyleTaskFactory;
import org.cytoscape.ding.impl.cyannotator.tasks.DuplicateAnnotationsTaskFactory;
import org.cytoscape.ding.impl.cyannotator.tasks.EditAnnotationTaskFactory;
import org.cytoscape.ding.impl.cyannotator.tasks.GroupAnnotationsTaskFactory;
import org.cytoscape.ding.impl.cyannotator.tasks.ListAnnotationsTaskFactory;
import org.cytoscape.ding.impl.cyannotator.tasks.PasteAnnotationStyleTaskFactory;
import org.cytoscape.ding.impl.cyannotator.tasks.RemoveAnnotationTaskFactory;
import org.cytoscape.ding.impl.cyannotator.tasks.RemoveSelectedAnnotationsTaskFactory;
import org.cytoscape.ding.impl.cyannotator.tasks.ReorderSelectedAnnotationsTaskFactory;
import org.cytoscape.ding.impl.cyannotator.tasks.UngroupAnnotationsTaskFactory;
import org.cytoscape.ding.impl.cyannotator.tasks.UpdateAnnotationTaskFactory;
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
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.NetworkViewLocationTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkViewFactoryProvider;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.ThumbnailFactory;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.BoundedTextAnnotation;
import org.cytoscape.view.presentation.annotations.GroupAnnotation;
import org.cytoscape.view.presentation.annotations.ImageAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
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
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2020 The Cytoscape Consortium
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
	
	private CustomGraphicsManager cgManager;
	private CyCustomGraphics2Manager cg2Manager;
	private CustomGraphicsBrowser cgBrowser;
	
	@Override
	public void start(BundleContext bc) {
		var serviceRegistrar = getService(bc, CyServiceRegistrar.class);

		startCustomGraphicsMgr(bc, serviceRegistrar);
		startCharts(bc, serviceRegistrar);
		startGradients(bc, serviceRegistrar);
		startPresentationImpl(bc, serviceRegistrar);

		if (DingDebugMediator.showDebugPanel(serviceRegistrar)) {
			var debugMediator = new DingDebugMediator(serviceRegistrar);
			registerAllServices(bc, debugMediator);
		}
	}

	private void startPresentationImpl(BundleContext bc, CyServiceRegistrar serviceRegistrar) {
		var dVisualLexicon = new DVisualLexicon(cgManager);

		var nvltfActionSupport = new NVLTFActionSupport(serviceRegistrar);
		var vtfListener = new ViewTaskFactoryListener(nvltfActionSupport);
		registerService(bc, vtfListener, ViewTaskFactoryListener.class);

		var annotationFactoryManager = new AnnotationFactoryManager();
		var annotationManager = new AnnotationManagerImpl(serviceRegistrar);

		var dingGraphLOD = new DingGraphLOD(serviceRegistrar);
		registerService(bc, dingGraphLOD, PropertyUpdatedListener.class);
		
		var handleFactory = new HandleFactoryImpl();
		registerService(bc, handleFactory, HandleFactory.class);

		var continuousMappingCellRendererFactory = getService(bc, ContinuousMappingCellRendererFactory.class);

		// Object Position Editor
		var objectPositionValueEditor = new ObjectPositionValueEditor();
		var objectPositionEditor =
				new ObjectPositionEditor(objectPositionValueEditor, continuousMappingCellRendererFactory, serviceRegistrar);

		var netViewFactoryProvider = getService(bc, CyNetworkViewFactoryProvider.class);
		var netViewManager = getService(bc, CyNetworkViewManager.class);
		var renderingEngineManager = getService(bc, RenderingEngineManager.class);
		var viewFactoryConfig = DingNetworkViewFactory.getNetworkViewConfig(netViewFactoryProvider, dVisualLexicon);
		var netViewFactory = netViewFactoryProvider.createNetworkViewFactory(dVisualLexicon, DingRenderer.ID, viewFactoryConfig);
		
		var dingNetViewFactory = new DingNetworkViewFactory(netViewFactory, dVisualLexicon, annotationFactoryManager, dingGraphLOD, handleFactory, serviceRegistrar);
		registerService(bc, dingNetViewFactory, NetworkViewAboutToBeDestroyedListener.class);
		
		var renderer = new DingRenderer(dingNetViewFactory, dVisualLexicon, serviceRegistrar);
		registerService(bc, renderer, NetworkViewRenderer.class);
		registerService(bc, renderer, DingRenderer.class);
		
		var dingRenderingEngineFactory = renderer.getRenderingEngineFactory(DingRenderer.DEFAULT_CONTEXT);
		{
			var props = new Properties();
			props.setProperty(ID, "ding");
			registerAllServices(bc, dingRenderingEngineFactory, props);
		}
		
//		Properties dingNavigationRenderingEngineFactoryProps = new Properties();
//		dingNavigationRenderingEngineFactoryProps.setProperty(ID, "dingNavigation");
//		registerAllServices(bc, dingNavigationRenderingEngineFactory, dingNavigationRenderingEngineFactoryProps);
		{
			var factory = new AddEdgeNodeViewTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(PREFERRED_ACTION, "Edge");
			props.setProperty(PREFERRED_MENU, NODE_ADD_MENU);
			props.setProperty(TITLE, "Edge");
			props.setProperty(MENU_GRAVITY, "0.1");
			registerService(bc, factory, NodeViewTaskFactory.class, props);
		}
		{
			var props = new Properties();
			props.setProperty(ID, "ding");
			registerService(bc, dVisualLexicon, VisualLexicon.class, props);
		}
		{
			var props = new Properties();
			props.setProperty(ID, "objectPositionValueEditor");
			registerService(bc, objectPositionValueEditor, ValueEditor.class, props);
		}
		{
			var props = new Properties();
			props.setProperty(ID, "objectPositionEditor");
			registerService(bc, objectPositionEditor, VisualPropertyEditor.class, props);
		}
		
		// Edge Bend editor
		var edgeBendValueEditor = new EdgeBendValueEditor(dingNetViewFactory, dingRenderingEngineFactory, serviceRegistrar);
		registerAllServices(bc, edgeBendValueEditor);
		
		var edgeBendEditor = new EdgeBendEditor(edgeBendValueEditor, continuousMappingCellRendererFactory, serviceRegistrar);
		registerService(bc, edgeBendEditor, VisualPropertyEditor.class);

		// Annotation Manager
		registerServiceListener(bc, annotationFactoryManager::addAnnotationFactory, annotationFactoryManager::removeAnnotationFactory, AnnotationFactory.class);
		registerService(bc, annotationManager, AnnotationManager.class);

		// Annotations UI
		var annotationMediator = new AnnotationMediator(serviceRegistrar);
		registerServiceListener(bc, annotationMediator::addAnnotationFactory, annotationMediator::removeAnnotationFactory, AnnotationFactory.class);
		registerAllServices(bc, annotationMediator);
		
		// Annotation Factories (the order they are registered is the order they appear in the UI)
		var textAnnotationFactory = new TextAnnotationFactory(serviceRegistrar);
		{
			var props = new Properties();
			props.setProperty("type", "TextAnnotation.class");
			registerService(bc, textAnnotationFactory, AnnotationFactory.class, props);
		}
		var boundedAnnotationFactory = new BoundedTextAnnotationFactory(serviceRegistrar);
		{
			var props = new Properties();
			props.setProperty("type", "BoundedTextAnnotation.class");
			registerService(bc, boundedAnnotationFactory, AnnotationFactory.class, props);
		}
		var shapeAnnotationFactory = new ShapeAnnotationFactory(serviceRegistrar);
		{
			var props = new Properties();
			props.setProperty("type", "ShapeAnnotation.class");
			registerService(bc, shapeAnnotationFactory, AnnotationFactory.class, props);
		}
		var imageAnnotationFactory = new ImageAnnotationFactory(cgBrowser, serviceRegistrar);
		{
			var props = new Properties();
			props.setProperty("type", "ImageAnnotation.class");
			registerService(bc, imageAnnotationFactory, AnnotationFactory.class, props);
		}
		var arrowAnnotationFactory = new ArrowAnnotationFactory(serviceRegistrar);
		{
			var props = new Properties();
			props.setProperty("type", "ArrowAnnotation.class");
			registerService(bc, arrowAnnotationFactory, AnnotationFactory.class, props);
		}
		{
			var factory = new GroupAnnotationFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty("type", "GroupAnnotation.class");
			registerService(bc, factory, AnnotationFactory.class, props);
		}
		
		// Annotation Task Factories
		{
			var factory = new AddAnnotationTaskFactory(arrowAnnotationFactory, renderer, annotationMediator);
			var props = new Properties();
			props.setProperty(ID, "addAnnotationTaskFactory_" + arrowAnnotationFactory.getId());
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(PREFERRED_MENU, NETWORK_ADD_MENU);
			props.setProperty(MENU_GRAVITY, "1.2");
			props.setProperty(TITLE, "Arrow Annotation...");
			props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
			registerService(bc, factory, NetworkViewLocationTaskFactory.class, props);
		}
		{
			var factory = new AddAnnotationTaskFactory(imageAnnotationFactory, renderer, annotationMediator);
			var props = new Properties();
			props.setProperty(ID, "addAnnotationTaskFactory_" + imageAnnotationFactory.getId());
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(PREFERRED_MENU, NETWORK_ADD_MENU);
			props.setProperty(MENU_GRAVITY, "1.3");
			props.setProperty(TITLE, "Image Annotation...");
			registerService(bc, factory, NetworkViewLocationTaskFactory.class, props);
		}
		{
			var factory = new AddAnnotationTaskFactory(shapeAnnotationFactory, renderer, annotationMediator);
			var props = new Properties();
			props.setProperty(ID, "addAnnotationTaskFactory_" + shapeAnnotationFactory.getId());
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(PREFERRED_MENU, NETWORK_ADD_MENU);
			props.setProperty(MENU_GRAVITY, "1.4");
			props.setProperty(TITLE, "Shape Annotation...");
			registerService(bc, factory, NetworkViewLocationTaskFactory.class, props);
		}
		{
			var factory = new AddAnnotationTaskFactory(textAnnotationFactory, renderer, annotationMediator);
			var props = new Properties();
			props.setProperty(ID, "addAnnotationTaskFactory_" + textAnnotationFactory.getId());
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(MENU_GRAVITY, "1.5");
			props.setProperty(PREFERRED_MENU, NETWORK_ADD_MENU);
			props.setProperty(TITLE, "Text Annotation...");
			registerService(bc, factory, NetworkViewLocationTaskFactory.class, props);
		}
		{
			var factory = new AddAnnotationTaskFactory(boundedAnnotationFactory, renderer, annotationMediator);
			var props = new Properties();
			props.setProperty(ID, "addAnnotationTaskFactory_" + boundedAnnotationFactory.getId());
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(MENU_GRAVITY, "1.6");
			props.setProperty(PREFERRED_MENU, NETWORK_ADD_MENU);
			props.setProperty(TITLE, "Bounded Text Annotation...");
			registerService(bc, factory, NetworkViewLocationTaskFactory.class, props);
		}
		
		// Annotation edit
		{
			var factory = new EditAnnotationTaskFactory(renderer, annotationMediator);
			var props = new Properties();
			props.setProperty(ID, "editAnnotationTaskFactory");
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(MENU_GRAVITY, "6.0");
			props.setProperty(PREFERRED_MENU, NETWORK_EDIT_MENU);
			props.setProperty(TITLE, "Modify Annotation...");
			props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
			registerService(bc, factory, NetworkViewLocationTaskFactory.class, props);
		}
		// Annotation (right-clicked one) delete (context-menu only)
		{
			var factory = new RemoveAnnotationTaskFactory(renderer);
			var props = new Properties();
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(MENU_GRAVITY, "1.1");
			props.setProperty(PREFERRED_MENU, NETWORK_DELETE_MENU);
			props.setProperty(TITLE, "Annotation");
			registerService(bc, factory, NetworkViewLocationTaskFactory.class, props);
		}
		// Delete Selected Annotations
		{
			var factory = new RemoveSelectedAnnotationsTaskFactory(renderer, serviceRegistrar);
			var props = new Properties();
			props.setProperty(ID, "removeSelectedAnnotationsTaskFactory");
			props.setProperty(MENU_GRAVITY, "5.1");
			props.setProperty(PREFERRED_MENU, "Edit");
			props.setProperty(TITLE, "Remove Selected Annotations");
			props.setProperty(IN_CONTEXT_MENU, "false");
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
		}
		// Annotation duplicate
		{
			var factory = new DuplicateAnnotationsTaskFactory(renderer, annotationFactoryManager);
			var props = new Properties();
			props.setProperty(ID, "duplicateAnnotationsTaskFactory");
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(MENU_GRAVITY, "6.1");
			props.setProperty(PREFERRED_MENU, NETWORK_EDIT_MENU);
			props.setProperty(TITLE, "Duplicate Selected Annotations");
			props.setProperty(ACCELERATOR, "cmd D");
			props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
		}
		// Copy/Paste Annotation Style
		var annotationClipboard = new AnnotationClipboard();
		{
			var factory = new CopyAnnotationStyleTaskFactory(renderer, annotationClipboard);
			var props = new Properties();
			props.setProperty(ID, "copyAnnotationStyleTaskFactory");
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(MENU_GRAVITY, "6.11");
			props.setProperty(PREFERRED_MENU, NETWORK_EDIT_MENU);
			props.setProperty(TITLE, "Copy Annotation Style");
			props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
			registerService(bc, factory, NetworkViewLocationTaskFactory.class, props);
		}
		{
			var factory = new PasteAnnotationStyleTaskFactory(renderer, annotationClipboard);
			var props = new Properties();
			props.setProperty(ID, "pasteAnnotationStyleTaskFactory");
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(MENU_GRAVITY, "6.12");
			props.setProperty(PREFERRED_MENU, NETWORK_EDIT_MENU);
			props.setProperty(TITLE, "Paste Annotation Style");
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
		}
		// Reorder Selected Annotations - Edit Menu
		{
			var factory = new ReorderSelectedAnnotationsTaskFactory(renderer, Shift.TO_FRONT);
			var props = new Properties();
			props.setProperty(PREFERRED_MENU, NETWORK_EDIT_MENU);
			props.setProperty(TITLE, "Bring Annotations to Front");
			props.setProperty(ACCELERATOR, "shift cmd CLOSE_BRACKET");
			props.setProperty(MENU_GRAVITY, "6.2");
			props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
		}
		{
			var factory = new ReorderSelectedAnnotationsTaskFactory(renderer, Shift.UP_ONE);
			var props = new Properties();
			props.setProperty(PREFERRED_MENU, NETWORK_EDIT_MENU);
			props.setProperty(TITLE, "Bring Annotations Forward");
			props.setProperty(ACCELERATOR, "cmd CLOSE_BRACKET");
			props.setProperty(MENU_GRAVITY, "6.3");
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
		}
		{
			var factory = new ReorderSelectedAnnotationsTaskFactory(renderer, Shift.DOWN_ONE);
			var props = new Properties();
			props.setProperty(PREFERRED_MENU, NETWORK_EDIT_MENU);
			props.setProperty(TITLE, "Send Annotations Backward");
			props.setProperty(ACCELERATOR, "cmd OPEN_BRACKET");
			props.setProperty(MENU_GRAVITY, "6.4");
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
		}
		{
			var factory = new ReorderSelectedAnnotationsTaskFactory(renderer, Shift.TO_BACK);
			var props = new Properties();
			props.setProperty(PREFERRED_MENU, NETWORK_EDIT_MENU);
			props.setProperty(TITLE, "Send Annotations to Back");
			props.setProperty(ACCELERATOR, "shift cmd OPEN_BRACKET");
			props.setProperty(MENU_GRAVITY, "6.5");
			props.setProperty(INSERT_SEPARATOR_AFTER, "true");
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
		}
		{
			var factory = new ReorderSelectedAnnotationsTaskFactory(renderer, Annotation.FOREGROUND);
			var props = new Properties();
			props.setProperty(PREFERRED_MENU, NETWORK_EDIT_MENU);
			props.setProperty(MENU_GRAVITY, "6.6");
			props.setProperty(TITLE, "Pull Annotations to Foreground Layer");
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
		}
		{
			var factory = new ReorderSelectedAnnotationsTaskFactory(renderer, Annotation.BACKGROUND);
			var props = new Properties();
			props.setProperty(PREFERRED_MENU, NETWORK_EDIT_MENU);
			props.setProperty(TITLE, "Push Annotations to Background Layer");
			props.setProperty(MENU_GRAVITY, "6.7");
			props.setProperty(INSERT_SEPARATOR_AFTER, "true");
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
		}
		
		// Annotation group
		{
			var factory = new GroupAnnotationsTaskFactory(renderer);
			var props = new Properties();
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(MENU_GRAVITY, "100");
			props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
			props.setProperty(IN_MENU_BAR, "false");
			props.setProperty(PREFERRED_MENU, NETWORK_GROUP_MENU);
			props.setProperty(TITLE, "Group Annotations");
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
		}
		{
			// Annotation ungroup
			var factory = new UngroupAnnotationsTaskFactory(renderer);
			var props = new Properties();
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(MENU_GRAVITY, "100");
			props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
			props.setProperty(PREFERRED_MENU, NETWORK_GROUP_MENU);
			props.setProperty(TITLE, "Ungroup Annotations");
			registerService(bc, factory, NetworkViewLocationTaskFactory.class, props);
		}

    // -------------------------- Annotation Commands ----------------------------- //
    {
			// Annotation list
			var factory = new ListAnnotationsTaskFactory(annotationManager, netViewManager);
			var props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "annotation");
			props.setProperty(COMMAND, "list");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_DESCRIPTION, "List all current annotations");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "List all annotations, or annotations for the specified view, if provided.");
			props.setProperty(COMMAND_EXAMPLE_JSON, "[{\"canvas\":\"foreground\",\"name\":\"Text\","+
                                             "\"type\":\"org.cytoscape.view.presentation.annotations.TextAnnotation\","+
                                             "\"uuid\":\"e643934f-cf94-4ab8-affe-b86675034bee\""+
                                             "\"x\":\"2807.0\",\"y\":\"1268.0\",\"z\":\"0\"}]");

			registerService(bc, factory, TaskFactory.class, props);
		}

    	{
			// Annotation add shape
			var factory = new AddAnnotationTaskFactory(annotationManager, shapeAnnotationFactory);
			var props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "annotation");
			props.setProperty(COMMAND, "add shape");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_DESCRIPTION, "Add a shape annotation");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Adds a shape annotation to a view.  The view must be specified.");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{"+
                                              "\"edgeThickness\": \"4.0\","+
                                              "\"canvas\": \"foreground\","+
                                              "\"fillOpacity\": \"100.0\","+
                                              "\"rotation\": \"0.0\","+
                                              "\"type\": \"org.cytoscape.view.presentation.annotations.ShapeAnnotation\","+
                                              "\"uuid\": \"177c3b25-a138-4734-99f4-94316dd555c7\","+
                                              "\"fillColor\": \"#9999FF\","+
                                              "\"shapeType\": \"RECTANGLE\","+
                                              "\"edgeColor\": \"#000000\","+
                                              "\"edgeOpacity\": \"100.0\","+
                                              "\"name\": \"Shape 1\","+
                                              "\"x\": \"2735.0\","+
                                              "\"width\": \"152.0\","+
                                              "\"y\": \"1221.0761988896875\","+
                                              "\"z\": \"2\","+
                                              "\"height\": \"171.0\""+
                                              "}");

			registerService(bc, factory, TaskFactory.class, props);
		}
    
	    {
	    	var thumbnailFactory = new ThumbnailFactoryImpl();
	    	var props = new Properties();
			props.setProperty(ID, "ding");
	    	registerService(bc, thumbnailFactory, ThumbnailFactory.class, props);
	    }

    	{
			// Annotation add text
			var factory = new AddAnnotationTaskFactory(annotationManager, textAnnotationFactory);
			var props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "annotation");
			props.setProperty(COMMAND, "add text");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_DESCRIPTION, "Adds a text annotation"); 
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Adds a text annotation to a view.  The view must be specified. ");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{"+
                                              "\"canvas\": \"foreground\","+
                                              "\"color\": \"#000000\","+
                                              "\"rotation\": \"0.0\","+
                                              "\"type\": \"org.cytoscape.view.presentation.annotations.TextAnnotation\","+
                                              "\"fontStyle\": \"plain\","+
                                              "\"uuid\": \"e643934f-cf94-4ab8-affe-b86675034bee\","+
                                              "\"fontFamily\": \"Abyssinica SIL\","+
                                              "\"name\": \"Text\","+
                                              "\"x\": \"2807.0\","+
                                              "\"y\": \"1268.0\","+
                                              "\"z\": \"0\","+
                                              "\"fontSize\": \"74\","+
                                              "\"text\": \"Text\""+
                                              "}");

			registerService(bc, factory, TaskFactory.class, props);
		}

    {
			// Annotation add bounded text
			var factory = new AddAnnotationTaskFactory(annotationManager, boundedAnnotationFactory);
			var props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "annotation");
			props.setProperty(COMMAND, "add bounded text");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_DESCRIPTION, "Add a bounded text annotation");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Adds a bounded text annotation to a view.  The view must be specified.");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{"+
                                              "\"edgeThickness\": \"1.0\","+
                                              "\"canvas\": \"foreground\","+
                                              "\"fillOpacity\": \"50.0\","+
                                              "\"color\": \"#000000\","+
                                              "\"rotation\": \"0.0\","+
                                              "\"type\": \"org.cytoscape.view.presentation.annotations.BoundedTextAnnotation\","+
                                              "\"fontStyle\": \"0\","+
                                              "\"uuid\": \"8f5d3f59-d023-4522-bfb5-164794b233cf\","+
                                              "\"fillColor\": \"#FF66FF\","+
                                              "\"shapeType\": \"RECTANGLE\","+
                                              "\"edgeColor\": \"#000000\","+
                                              "\"fontFamily\": \"Abyssinica SIL\","+
                                              "\"edgeOpacity\": \"100.0\","+
                                              "\"name\": \"Text\","+
                                              "\"x\": \"2930.0\","+
                                              "\"width\": \"163.0\","+
                                              "\"y\": \"1176.0\","+
                                              "\"z\": \"1\","+
                                              "\"fontSize\": \"72\","+
                                              "\"text\": \"Text\","+
                                              "\"height\": \"104.0\""+
                                              "}");

			registerService(bc, factory, TaskFactory.class, props);
		}

    {
			// Annotation add image
			var factory = new AddAnnotationTaskFactory(annotationManager, imageAnnotationFactory);
			var props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "annotation");
			props.setProperty(COMMAND, "add image");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_DESCRIPTION, "Adds an image annotation."); 
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Adds an image annotation to the specified view.  The view must be specified.");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{"+
                                              "\"edgeThickness\": \"0.0\","+
                                              "\"canvas\": \"foreground\","+
                                              "\"fillOpacity\": \"100.0\","+
                                              "\"rotation\": \"0.0\","+
                                              "\"type\": \"org.cytoscape.view.presentation.annotations.ImageAnnotation\","+
                                              "\"uuid\": \"0218f867-008e-48ed-800c-a2fc8863af33\","+
                                              "\"URL\": \"file:/home/scooter/Bliss1.jpg\","+
                                              "\"shapeType\": \"RECTANGLE\","+
                                              "\"edgeColor\": \"#000000\","+
                                              "\"brightness\": \"0\","+
                                              "\"edgeOpacity\": \"100.0\","+
                                              "\"contrast\": \"0\","+
                                              "\"name\": \"Bliss1.jpg\","+
                                              "\"x\": \"2705.0\","+
                                              "\"width\": \"492.0\","+
                                              "\"y\": \"1422.0\","+
                                              "\"z\": \"1\","+
                                              "\"opacity\": \"1.0\","+
                                              "\"height\": \"369.0\""+
                                              "}");

			registerService(bc, factory, TaskFactory.class, props);
		}

    {
			// Annotation remove
			var factory = new RemoveAnnotationTaskFactory(annotationManager, netViewManager);
			var props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "annotation");
			props.setProperty(COMMAND, "delete");
			props.setProperty(COMMAND_SUPPORTS_JSON, "false");
			props.setProperty(COMMAND_DESCRIPTION, "Deletes an annotation from a view.");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Deletes an annotation from a view.");

			registerService(bc, factory, TaskFactory.class, props);
		}

    {
			// Annotation update shape
			var factory = new UpdateAnnotationTaskFactory(ShapeAnnotation.class, annotationManager, netViewManager);
			var props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "annotation");
			props.setProperty(COMMAND, "update shape");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_DESCRIPTION, "Updates a shape annotation.");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Updates a shape annotation, changing the given properties.");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{"+
                                              "\"edgeThickness\": \"4.0\","+
                                              "\"canvas\": \"foreground\","+
                                              "\"fillOpacity\": \"100.0\","+
                                              "\"rotation\": \"0.0\","+
                                              "\"type\": \"org.cytoscape.view.presentation.annotations.ShapeAnnotation\","+
                                              "\"uuid\": \"177c3b25-a138-4734-99f4-94316dd555c7\","+
                                              "\"fillColor\": \"#9999FF\","+
                                              "\"shapeType\": \"RECTANGLE\","+
                                              "\"edgeColor\": \"#000000\","+
                                              "\"edgeOpacity\": \"100.0\","+
                                              "\"name\": \"Shape 1\","+
                                              "\"x\": \"2735.0\","+
                                              "\"width\": \"152.0\","+
                                              "\"y\": \"1221.0761988896875\","+
                                              "\"z\": \"2\","+
                                              "\"height\": \"171.0\""+
                                              "}");


			registerService(bc, factory, TaskFactory.class, props);
		}

    {
			// Annotation update text
			var factory = new UpdateAnnotationTaskFactory(TextAnnotation.class, annotationManager, netViewManager);
			var props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "annotation");
			props.setProperty(COMMAND, "update text");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_DESCRIPTION, "Updates a text annotation.");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Updates a text annotation, changing the given properties.");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{"+
                                              "\"canvas\": \"foreground\","+
                                              "\"color\": \"#000000\","+
                                              "\"rotation\": \"0.0\","+
                                              "\"type\": \"org.cytoscape.view.presentation.annotations.TextAnnotation\","+
                                              "\"fontStyle\": \"plain\","+
                                              "\"uuid\": \"e643934f-cf94-4ab8-affe-b86675034bee\","+
                                              "\"fontFamily\": \"Abyssinica SIL\","+
                                              "\"name\": \"Text\","+
                                              "\"x\": \"2807.0\","+
                                              "\"y\": \"1268.0\","+
                                              "\"z\": \"0\","+
                                              "\"fontSize\": \"74\","+
                                              "\"text\": \"Text\""+
                                              "}");

			registerService(bc, factory, TaskFactory.class, props);
		}

    {
			// Annotation update bounded text
			var factory = new UpdateAnnotationTaskFactory(BoundedTextAnnotation.class, annotationManager, netViewManager);
			var props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "annotation");
			props.setProperty(COMMAND, "update bounded text");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_DESCRIPTION, "Updates a bounded text annotation.");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Updates a bounded text annotation, changing the given properties.");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{"+
                                              "\"edgeThickness\": \"1.0\","+
                                              "\"canvas\": \"foreground\","+
                                              "\"fillOpacity\": \"50.0\","+
                                              "\"color\": \"#000000\","+
                                              "\"rotation\": \"0.0\","+
                                              "\"type\": \"org.cytoscape.view.presentation.annotations.BoundedTextAnnotation\","+
                                              "\"fontStyle\": \"0\","+
                                              "\"uuid\": \"8f5d3f59-d023-4522-bfb5-164794b233cf\","+
                                              "\"fillColor\": \"#FF66FF\","+
                                              "\"shapeType\": \"RECTANGLE\","+
                                              "\"edgeColor\": \"#000000\","+
                                              "\"fontFamily\": \"Abyssinica SIL\","+
                                              "\"edgeOpacity\": \"100.0\","+
                                              "\"name\": \"Text\","+
                                              "\"x\": \"2930.0\","+
                                              "\"width\": \"163.0\","+
                                              "\"y\": \"1176.0\","+
                                              "\"z\": \"1\","+
                                              "\"fontSize\": \"72\","+
                                              "\"text\": \"Text\","+
                                              "\"height\": \"104.0\""+
                                              "}");

			registerService(bc, factory, TaskFactory.class, props);
		}

    {
			// Annotation update image
			var factory = new UpdateAnnotationTaskFactory(ImageAnnotation.class, annotationManager, netViewManager);
			var props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "annotation");
			props.setProperty(COMMAND, "update image");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_DESCRIPTION, "Updates an image annotation.");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Updates an image annotation, changing the given properties.");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{"+
                                              "\"edgeThickness\": \"0.0\","+
                                              "\"canvas\": \"foreground\","+
                                              "\"fillOpacity\": \"100.0\","+
                                              "\"rotation\": \"0.0\","+
                                              "\"type\": \"org.cytoscape.view.presentation.annotations.ImageAnnotation\","+
                                              "\"uuid\": \"0218f867-008e-48ed-800c-a2fc8863af33\","+
                                              "\"URL\": \"file:/home/scooter/Bliss1.jpg\","+
                                              "\"shapeType\": \"RECTANGLE\","+
                                              "\"edgeColor\": \"#000000\","+
                                              "\"brightness\": \"0\","+
                                              "\"edgeOpacity\": \"100.0\","+
                                              "\"contrast\": \"0\","+
                                              "\"name\": \"Bliss1.jpg\","+
                                              "\"x\": \"2705.0\","+
                                              "\"width\": \"492.0\","+
                                              "\"y\": \"1422.0\","+
                                              "\"z\": \"1\","+
                                              "\"opacity\": \"1.0\","+
                                              "\"height\": \"369.0\""+
                                              "}");

			registerService(bc, factory, TaskFactory.class, props);
		}

    {
			// Annotation update image
			var factory = new UpdateAnnotationTaskFactory(GroupAnnotation.class, annotationManager, netViewManager);
			var props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "annotation");
			props.setProperty(COMMAND, "update group");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_DESCRIPTION, "Updates a group annotation.");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Updates a group annotation, changing the given properties.");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{"+
                                              "\"canvas\": \"foreground\","+
                                              "\"rotation\": \"0.0\","+
                                              "\"name\": \"Group 1\","+
                                              "\"x\": \"2705.0\","+
                                              "\"y\": \"1176.0\","+
                                              "\"z\": \"0\","+
                                              "\"type\": \"org.cytoscape.view.presentation.annotations.GroupAnnotation\","+
                                              "\"uuid\": \"1486946b-f68b-4569-b0ab-80c545348932\","+
                                              "\"memberUUIDs\": \"e643934f-cf94-4ab8-affe-b86675034bee,0218f867-008e-48ed-800c-a2fc8863af33,177c3b25-a138-4734-99f4-94316dd555c7,8f5d3f59-d023-4522-bfb5-164794b233cf\""+
                                              "}"); 
			registerService(bc, factory, TaskFactory.class, props);
    }

    {
			var factory = new GroupAnnotationsTaskFactory(annotationManager, renderingEngineManager, netViewManager);
			var props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "annotation");
			props.setProperty(COMMAND, "group");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_DESCRIPTION, "Combines a list of annotations into a group annotation.");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Combines a list of annotations into a group annotation.");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{"+
                                              "\"canvas\": \"foreground\","+
                                              "\"rotation\": \"0.0\","+
                                              "\"name\": \"Group 1\","+
                                              "\"x\": \"2705.0\","+
                                              "\"y\": \"1176.0\","+
                                              "\"z\": \"0\","+
                                              "\"type\": \"org.cytoscape.view.presentation.annotations.GroupAnnotation\","+
                                              "\"uuid\": \"1486946b-f68b-4569-b0ab-80c545348932\","+
                                              "\"memberUUIDs\": \"e643934f-cf94-4ab8-affe-b86675034bee,0218f867-008e-48ed-800c-a2fc8863af33,177c3b25-a138-4734-99f4-94316dd555c7,8f5d3f59-d023-4522-bfb5-164794b233cf\""+
                                              "}"); 
			registerService(bc, factory, TaskFactory.class, props);
		}

    {
			var factory = new UngroupAnnotationsTaskFactory(annotationManager, renderingEngineManager, netViewManager);
			var props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "annotation");
			props.setProperty(COMMAND, "ungroup");
			props.setProperty(COMMAND_SUPPORTS_JSON, "false");
			props.setProperty(COMMAND_DESCRIPTION, ""); // FIXME
			props.setProperty(COMMAND_LONG_DESCRIPTION, ""); // FIXME
			registerService(bc, factory, TaskFactory.class, props);
		}

		// Set mouse drag selection modes
		var selectNodesOnlyAction = new SelectModeAction(SelectModeAction.NODES, 0.5f, serviceRegistrar);
		registerAllServices(bc, selectNodesOnlyAction);
		
		var selectEdgesOnlyAction = new SelectModeAction(SelectModeAction.EDGES, 0.6f, serviceRegistrar);
		registerAllServices(bc, selectEdgesOnlyAction);
		
		var selectAnnotationsOnlyAction = new SelectModeAction(SelectModeAction.ANNOTATIONS, 0.7f, serviceRegistrar);
		registerAllServices(bc, selectAnnotationsOnlyAction);
		
		var selectNodesEdgesAction = new SelectModeAction(SelectModeAction.NODES_EDGES, 0.8f, serviceRegistrar);
		registerAllServices(bc, selectNodesEdgesAction);

		var selectNodeLabelsAction = new SelectModeAction(SelectModeAction.NODE_LABELS, 0.85f, serviceRegistrar);
		registerAllServices(bc, selectNodeLabelsAction);
		
		SelectModeAction selectAllAction = new SelectModeAction(SelectModeAction.ALL, 0.9f, serviceRegistrar);
		registerAllServices(bc, selectAllAction);
		
		{
			// Toggle Graphics Details
			var factory = new ShowGraphicsDetailsTaskFactory();
			var props = new Properties();
			props.setProperty(ID, "showGraphicsDetailsTaskFactory");
			registerService(bc, factory, NetworkViewTaskFactory.class, props); // Used at least by cyREST
			
			// Main menu
			var mainMenuAction = new GraphicsDetailAction(5.0f, "View", factory, serviceRegistrar);
			registerAllServices(bc, mainMenuAction);
		}

		var vtfFilter = String.format("(| (!(%s=*)) (%s=true))", IN_CONTEXT_MENU, IN_CONTEXT_MENU); // if IN_CONTEXT_MENU is not specified, default to true
		registerServiceListener(bc, vtfListener::addNodeViewTaskFactory, vtfListener::removeNodeViewTaskFactory, NodeViewTaskFactory.class, vtfFilter);
		registerServiceListener(bc, vtfListener::addEdgeViewTaskFactory, vtfListener::removeEdgeViewTaskFactory, EdgeViewTaskFactory.class, vtfFilter);
		registerServiceListener(bc, vtfListener::addNetworkViewTaskFactory, vtfListener::removeNetworkViewTaskFactory, NetworkViewTaskFactory.class, vtfFilter);
		registerServiceListener(bc, vtfListener::addNetworkViewLocationTaskFactory, vtfListener::removeNetworkViewLocationTaskFactory, NetworkViewLocationTaskFactory.class);
		registerServiceListener(bc, vtfListener::addCyEdgeViewContextMenuFactory, vtfListener::removeCyEdgeViewContextMenuFactory, CyEdgeViewContextMenuFactory.class);
		registerServiceListener(bc, vtfListener::addCyNodeViewContextMenuFactory, vtfListener::removeCyNodeViewContextMenuFactory, CyNodeViewContextMenuFactory.class);
		registerServiceListener(bc, vtfListener::addCyNetworkViewContextMenuFactory, vtfListener::removeCyNetworkViewContextMenuFactory, CyNetworkViewContextMenuFactory.class);

		registerServiceListener(bc, annotationFactoryManager::addAnnotationFactory, annotationFactoryManager::removeAnnotationFactory, AnnotationFactory.class);

		var bendFactory = new BendFactoryImpl();
		registerService(bc, bendFactory, BendFactory.class);

		// Register the factory
		dVisualLexicon.addBendFactory(bendFactory, new HashMap<Object, Object>());
		
		// Translators for Passthrough
		var cgTranslator = new CustomGraphicsTranslator(cgManager, cg2Manager);
		registerService(bc, cgTranslator, ValueTranslator.class);

		// Factories for Visual Property Dependency
		var nodeSizeDependencyFactory = new NodeSizeDependencyFactory(dVisualLexicon);
		registerService(bc, nodeSizeDependencyFactory, VisualPropertyDependencyFactory.class);

		var edgeColorDependencyFactory = new EdgeColorDependencyFactory(dVisualLexicon);
		registerService(bc, edgeColorDependencyFactory, VisualPropertyDependencyFactory.class);

		var cgSizeDependencyFactory = new CustomGraphicsSizeDependencyFactory(dVisualLexicon);
		registerService(bc, cgSizeDependencyFactory, VisualPropertyDependencyFactory.class);

		// Custom Graphics Editors
		var cgValueEditor = new CyCustomGraphicsValueEditor(cgBrowser, serviceRegistrar);
		registerAllServices(bc, cgValueEditor);

		var cgVisualPropertyEditor = new CustomGraphicsVisualPropertyEditor(CyCustomGraphics.class, cgValueEditor,
				continuousMappingCellRendererFactory, serviceRegistrar);
		registerService(bc, cgVisualPropertyEditor, VisualPropertyEditor.class);
	}

	private void startCustomGraphicsMgr(BundleContext bc, CyServiceRegistrar serviceRegistrar) {
		cgManager = new CustomGraphicsManagerImpl(getdefaultImageURLs(bc), serviceRegistrar);
		registerAllServices(bc, cgManager);

		cgBrowser = new CustomGraphicsBrowser(cgManager);
		registerAllServices(bc, cgBrowser);

		var cgManagerAction = new CustomGraphicsManagerAction(cgManager, cgBrowser, serviceRegistrar);
		registerService(bc, cgManagerAction, CyAction.class);

		// Create and register our built-in factories.
		// TODO:  When the CustomGraphicsFactory service stuff is set up, just register these as services
		{
			var bitmapFactory = new BitmapCustomGraphicsFactory(cgManager, serviceRegistrar);
			var props = new Properties();
			props.setProperty(CustomGraphicsManager.SUPPORTED_CLASS_ID, BitmapCustomGraphicsFactory.SUPPORTED_CLASS_ID);
			cgManager.addCustomGraphicsFactory(bitmapFactory, props);
		}
		{
			var vectorFactory = new SVGCustomGraphicsFactory(cgManager, serviceRegistrar);
			var props = new Properties();
			props.setProperty(CustomGraphicsManager.SUPPORTED_CLASS_ID, SVGCustomGraphicsFactory.SUPPORTED_CLASS_ID);
			cgManager.addCustomGraphicsFactory(vectorFactory, props);
		}

		var ovalFactory = new GradientOvalFactory(cgManager);
		cgManager.addCustomGraphicsFactory(ovalFactory, new Properties());

		var rectangleFactory = new GradientRoundRectangleFactory(cgManager);
		cgManager.addCustomGraphicsFactory(rectangleFactory, new Properties());

		// Register this service listener so that app writers can provide their own CustomGraphics factories
		registerServiceListener(bc, cgManager::addCustomGraphicsFactory, cgManager::removeCustomGraphicsFactory,
				CyCustomGraphicsFactory.class);
		
		// Register this service listener so that app writers can provide their own CyCustomGraphics2 factories
		cg2Manager = CyCustomGraphics2ManagerImpl.getInstance();
		registerAllServices(bc, cg2Manager);
		registerServiceListener(bc, ((CyCustomGraphics2ManagerImpl) cg2Manager)::addFactory, 
				((CyCustomGraphics2ManagerImpl) cg2Manager)::removeFactory, CyCustomGraphics2Factory.class);
	}
	
	private void startCharts(BundleContext bc, CyServiceRegistrar serviceRegistrar) {
		// Register Chart Factories
		var props = new Properties();
		props.setProperty(CyCustomGraphics2Factory.GROUP, CyCustomGraphics2Manager.GROUP_CHARTS);
		{
			var factory = new BarChartFactory(serviceRegistrar);
			registerService(bc, factory, CyCustomGraphics2Factory.class, props);
		}
		{
			var factory = new BoxChartFactory(serviceRegistrar);
			registerService(bc, factory, CyCustomGraphics2Factory.class, props);
		}
		{
			var factory = new PieChartFactory(serviceRegistrar);
			registerService(bc, factory, CyCustomGraphics2Factory.class, props);
		}
		{
			var factory = new RingChartFactory(serviceRegistrar);
			registerService(bc, factory, CyCustomGraphics2Factory.class, props);
		}
		{
			var factory = new LineChartFactory(serviceRegistrar);
			registerService(bc, factory, CyCustomGraphics2Factory.class, props);
		}
		{
			var factory = new HeatMapChartFactory(serviceRegistrar);
			registerService(bc, factory, CyCustomGraphics2Factory.class, props);
		}
	}
	
	private void startGradients(BundleContext bc, CyServiceRegistrar serviceRegistrar) {
		// Register Gradient Factories
		var props = new Properties();
		props.setProperty(CyCustomGraphics2Factory.GROUP, CyCustomGraphics2Manager.GROUP_GRADIENTS);
		{
			var factory = new LinearGradientFactory();
			registerService(bc, factory, CyCustomGraphics2Factory.class, props);
		}
		{
			var factory = new RadialGradientFactory();
			registerService(bc, factory, CyCustomGraphics2Factory.class, props);
		}
	}
	
	/**
	 * Get list of default images from resource.
	 */
	private Set<URL> getdefaultImageURLs(BundleContext bc) {
		var e = bc.getBundle().findEntries("images/sampleCustomGraphics", "*.png", true);
		var defaultImageUrls = new HashSet<URL>();
		
		while (e.hasMoreElements())
			defaultImageUrls.add(e.nextElement());
		
		return defaultImageUrls;
	}
}
