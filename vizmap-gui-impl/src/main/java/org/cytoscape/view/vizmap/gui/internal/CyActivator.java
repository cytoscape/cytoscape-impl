package org.cytoscape.view.vizmap.gui.internal;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingCellRendererFactory;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.controller.ImportDefaultVisualStylesCommand;
import org.cytoscape.view.vizmap.gui.internal.controller.LoadVisualStylesCommand;
import org.cytoscape.view.vizmap.gui.internal.controller.StartupCommand;
import org.cytoscape.view.vizmap.gui.internal.event.VizMapEventHandlerManagerImpl;
import org.cytoscape.view.vizmap.gui.internal.model.VizMapperProxy;
import org.cytoscape.view.vizmap.gui.internal.task.ClearBendTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.task.CopyVisualStyleTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.task.CreateLegendTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.task.CreateNewVisualStyleTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.task.DeleteVisualStyleTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.task.RenameVisualStyleTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.theme.IconManager;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.FitLabelMappingGenerator;
import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.NumberSeriesMappingGenerator;
import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.RainbowColorMappingGenerator;
import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.RainbowOscColorMappingGenerator;
import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.RandomColorMappingGenerator;
import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.RandomNumberMappingGenerator;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapPropertyBuilder;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperMainPanel;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperMediator;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperMenuMediator;
import org.cytoscape.view.vizmap.gui.internal.view.editor.BooleanVisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.view.editor.ColorVisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.view.editor.EditorManagerImpl;
import org.cytoscape.view.vizmap.gui.internal.view.editor.FontVisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.view.editor.NumberVisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.view.editor.StringVisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.view.editor.propertyeditor.CyColorPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.view.editor.propertyeditor.CyComboBoxPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.view.editor.propertyeditor.CyFontPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.view.editor.valueeditor.BooleanValueEditor;
import org.cytoscape.view.vizmap.gui.internal.view.editor.valueeditor.CyColorChooser;
import org.cytoscape.view.vizmap.gui.internal.view.editor.valueeditor.FontEditor;
import org.cytoscape.view.vizmap.gui.internal.view.editor.valueeditor.NumericValueEditor;
import org.cytoscape.view.vizmap.gui.internal.view.editor.valueeditor.StringValueEditor;
import org.cytoscape.view.vizmap.gui.util.DiscreteMappingGenerator;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;


public class CyActivator extends AbstractCyActivator {
	

	public void start(final BundleContext bc) {

		VisualStyleFactory visualStyleFactoryServiceRef = getService(bc,VisualStyleFactory.class);
		VisualMappingManager visualMappingManagerServiceRef = getService(bc,VisualMappingManager.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class);
		CyApplicationManager cyApplicationManagerServiceRef = getService(bc,CyApplicationManager.class);
		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc,CyNetworkFactory.class);
		CyNetworkViewFactory graphViewFactoryServiceRef = getService(bc,CyNetworkViewFactory.class);
		CyEventHelper cyEventHelperServiceRef = getService(bc,CyEventHelper.class);
		CyServiceRegistrar cyServiceRegistrarServiceRef = getService(bc,CyServiceRegistrar.class);
		CyNetworkTableManager cyNetworkTableManagerServiceRef = getService(bc,CyNetworkTableManager.class);
		
		final ServicesUtil servicesUtil = new ServicesUtil(cyServiceRegistrarServiceRef);
		
		VisualMappingFunctionFactory continousMappingFactory = getService(bc, VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
		
		AttributeSetManager attributeSetManager = new AttributeSetManager(cyNetworkTableManagerServiceRef);
		ContinuousMappingCellRendererFactory continuousMappingCellRendererFactory = getService(bc,ContinuousMappingCellRendererFactory.class);
		EditorManagerImpl editorManager = new EditorManagerImpl(cyApplicationManagerServiceRef,attributeSetManager,visualMappingManagerServiceRef,cyNetworkTableManagerServiceRef, cyNetworkManagerServiceRef, continousMappingFactory, continuousMappingCellRendererFactory, cyServiceRegistrarServiceRef);
		MappingFunctionFactoryManagerImpl mappingFunctionFactoryManager = new MappingFunctionFactoryManagerImpl(editorManager);
		
		CyColorChooser colorEditor = new CyColorChooser();
		CyColorPropertyEditor cyColorPropertyEditor = new CyColorPropertyEditor(colorEditor);
		
		FontEditor fontEditor = new FontEditor();
		CyFontPropertyEditor fontPropertyEditor = new CyFontPropertyEditor(fontEditor);
		
		NumericValueEditor<Double> doubleValueEditor = new NumericValueEditor<Double>(Double.class);
		NumericValueEditor<Integer> integerValueEditor = new NumericValueEditor<Integer>(Integer.class);
		NumericValueEditor<Float> floatValueEditor = new NumericValueEditor<Float>(Float.class);
		StringValueEditor stringValueEditor = new StringValueEditor();
		BooleanValueEditor booleanValueEditor = new BooleanValueEditor();
// TODO		
		ColorVisualPropertyEditor colorPropertyEditor = new ColorVisualPropertyEditor(Paint.class,cyNetworkTableManagerServiceRef,cyApplicationManagerServiceRef,editorManager,visualMappingManagerServiceRef,cyColorPropertyEditor,continuousMappingCellRendererFactory);
		NumberVisualPropertyEditor<?> doublePropertyEditor = new NumberVisualPropertyEditor(Double.class,cyNetworkTableManagerServiceRef,cyApplicationManagerServiceRef,editorManager,visualMappingManagerServiceRef, continuousMappingCellRendererFactory);
		NumberVisualPropertyEditor<?> integerPropertyEditor = new NumberVisualPropertyEditor(Integer.class,cyNetworkTableManagerServiceRef,cyApplicationManagerServiceRef,editorManager,visualMappingManagerServiceRef, continuousMappingCellRendererFactory);
		NumberVisualPropertyEditor<?> floatPropertyEditor = new NumberVisualPropertyEditor(Float.class,cyNetworkTableManagerServiceRef,cyApplicationManagerServiceRef,editorManager,visualMappingManagerServiceRef, continuousMappingCellRendererFactory);
		
		FontVisualPropertyEditor fontVisualPropertyEditor = new FontVisualPropertyEditor(Font.class, fontPropertyEditor, continuousMappingCellRendererFactory);
		StringVisualPropertyEditor stringPropertyEditor = new StringVisualPropertyEditor(continuousMappingCellRendererFactory);
		final CyComboBoxPropertyEditor booleanEditor = new CyComboBoxPropertyEditor();
		booleanEditor.setAvailableValues(new Boolean[] {true, false});
		BooleanVisualPropertyEditor booleanVisualPropertyEditor = new BooleanVisualPropertyEditor(booleanEditor, continuousMappingCellRendererFactory);

		CreateNewVisualStyleTaskFactory createNewVisualStyleTaskFactory = new CreateNewVisualStyleTaskFactory(visualStyleFactoryServiceRef,visualMappingManagerServiceRef);
		DeleteVisualStyleTaskFactory removeVisualStyleTaskFactory = new DeleteVisualStyleTaskFactory(visualMappingManagerServiceRef);
		
		VizMapPropertyBuilder vizMapPropertyBuilder = new VizMapPropertyBuilder(cyApplicationManagerServiceRef, editorManager);
		
		RenameVisualStyleTaskFactory renameVisualStyleTaskFactory = new RenameVisualStyleTaskFactory(visualMappingManagerServiceRef);
		CopyVisualStyleTaskFactory copyVisualStyleTaskFactory = new CopyVisualStyleTaskFactory(visualMappingManagerServiceRef,visualStyleFactoryServiceRef);
		CreateLegendTaskFactory createLegendTaskFactory = new CreateLegendTaskFactory(cyApplicationManagerServiceRef, visualMappingManagerServiceRef, continousMappingFactory);
// TODO
//		DeleteMappingFunctionTaskFactory deleteMappingFunctionTaskFactory = new DeleteMappingFunctionTaskFactory(propertySheetPanel,visualMappingManagerServiceRef);
		
		RainbowColorMappingGenerator rainbowGenerator = new RainbowColorMappingGenerator(Color.class);
		RainbowOscColorMappingGenerator rainbowOscGenerator = new RainbowOscColorMappingGenerator(Color.class);
		RandomColorMappingGenerator randomColorGenerator = new RandomColorMappingGenerator(Color.class);
		NumberSeriesMappingGenerator<Number> seriesGenerator = new NumberSeriesMappingGenerator<Number>(Number.class);
		RandomNumberMappingGenerator randomNumberGenerator = new RandomNumberMappingGenerator();
		FitLabelMappingGenerator<Number> fitLabelMappingGenerator = new FitLabelMappingGenerator<Number>(Number.class, cyApplicationManagerServiceRef, visualMappingManagerServiceRef);
		
		// Context menu for edge bend
		BendFactory bf = getService(bc, BendFactory.class);
		
		final Properties clearBendProp = new Properties();
		clearBendProp.put(ServiceProperties.PREFERRED_MENU, ServiceProperties.EDGE_EDIT_MENU);
		clearBendProp.put(ServiceProperties.TITLE, "Clear Edge Bends");
		clearBendProp.put(ServiceProperties.MENU_GRAVITY, "5.0");
		clearBendProp.put(ServiceProperties.INSERT_SEPARATOR_BEFORE, "true");
		final ClearBendTaskFactory clearBendTaskFactory = new ClearBendTaskFactory(visualMappingManagerServiceRef, bf);
		registerService(bc, clearBendTaskFactory, EdgeViewTaskFactory.class, clearBendProp);
		
		registerAllServices(bc, attributeSetManager, new Properties());
//		registerAllServices(bc, defViewEditor, new Properties());
		registerAllServices(bc, editorManager.getNodeEditor(), new Properties());
		registerAllServices(bc, editorManager.getEdgeEditor(), new Properties());
		registerAllServices(bc, editorManager.getNetworkEditor(), new Properties());
		registerAllServices(bc, colorEditor, new Properties());
		registerAllServices(bc, fontEditor, new Properties());
		registerAllServices(bc, doubleValueEditor, new Properties());
		registerAllServices(bc, integerValueEditor, new Properties());
		registerAllServices(bc, floatValueEditor, new Properties());
		registerAllServices(bc, stringValueEditor, new Properties());
		registerAllServices(bc, booleanValueEditor, new Properties());
		
		registerAllServices(bc, colorPropertyEditor, new Properties());
		registerAllServices(bc, doublePropertyEditor, new Properties());
		registerAllServices(bc, floatPropertyEditor, new Properties());
		registerAllServices(bc, integerPropertyEditor, new Properties());
		
		registerAllServices(bc, fontVisualPropertyEditor, new Properties());
		registerAllServices(bc, stringPropertyEditor, new Properties());
		registerAllServices(bc, booleanVisualPropertyEditor, new Properties());
		
		registerAllServices(bc,editorManager, new Properties());

		Properties createNewVisualStyleTaskFactoryProps = new Properties();
		createNewVisualStyleTaskFactoryProps.setProperty("service.type","vizmapUI.taskFactory");
		createNewVisualStyleTaskFactoryProps.setProperty("title","Create New Visual Style");
		createNewVisualStyleTaskFactoryProps.setProperty("menu","main");
		registerAllServices(bc,createNewVisualStyleTaskFactory, createNewVisualStyleTaskFactoryProps);

		Properties removeVisualStyleTaskFactoryProps = new Properties();
		removeVisualStyleTaskFactoryProps.setProperty("service.type","vizmapUI.taskFactory");
		removeVisualStyleTaskFactoryProps.setProperty("title","Remove Visual Style");
		removeVisualStyleTaskFactoryProps.setProperty("menu","main");
		registerAllServices(bc,removeVisualStyleTaskFactory, removeVisualStyleTaskFactoryProps);

		Properties renameVisualStyleTaskFactoryProps = new Properties();
		renameVisualStyleTaskFactoryProps.setProperty("service.type","vizmapUI.taskFactory");
		renameVisualStyleTaskFactoryProps.setProperty("title","Rename Visual Style");
		renameVisualStyleTaskFactoryProps.setProperty("menu","main");
		registerAllServices(bc,renameVisualStyleTaskFactory, renameVisualStyleTaskFactoryProps);

		Properties copyVisualStyleTaskFactoryProps = new Properties();
		copyVisualStyleTaskFactoryProps.setProperty("service.type","vizmapUI.taskFactory");
		copyVisualStyleTaskFactoryProps.setProperty("title","Copy Visual Style");
		copyVisualStyleTaskFactoryProps.setProperty("menu","main");
		registerAllServices(bc,copyVisualStyleTaskFactory, copyVisualStyleTaskFactoryProps);

		Properties createLegendTaskFactoryProps = new Properties();
		createLegendTaskFactoryProps.setProperty("service.type","vizmapUI.taskFactory");
		createLegendTaskFactoryProps.setProperty("title","Create Legend");
		createLegendTaskFactoryProps.setProperty("menu","main");
		registerAllServices(bc,createLegendTaskFactory, createLegendTaskFactoryProps);

// TODO
//		Properties deleteMappingFunctionTaskFactoryProps = new Properties();
//		deleteMappingFunctionTaskFactoryProps.setProperty("service.type","vizmapUI.taskFactory");
//		deleteMappingFunctionTaskFactoryProps.setProperty("title","Delete Selected Mapping");
//		deleteMappingFunctionTaskFactoryProps.setProperty("menu","context");
//		registerAllServices(bc,deleteMappingFunctionTaskFactory, deleteMappingFunctionTaskFactoryProps);

		Properties rainbowGeneratorProps = new Properties();
		rainbowGeneratorProps.setProperty("service.type","vizmapUI.contextMenu");
		rainbowGeneratorProps.setProperty("title","Rainbow");
		rainbowGeneratorProps.setProperty("menu","context");
		registerService(bc,rainbowGenerator, DiscreteMappingGenerator.class, rainbowGeneratorProps);

		Properties rainbowOscGeneratorProps = new Properties();
		rainbowOscGeneratorProps.setProperty("service.type","vizmapUI.contextMenu");
		rainbowOscGeneratorProps.setProperty("title","Rainbow OSC");
		rainbowOscGeneratorProps.setProperty("menu","context");
		registerService(bc,rainbowOscGenerator, DiscreteMappingGenerator.class, rainbowOscGeneratorProps);

		Properties randomColorGeneratorProps = new Properties();
		randomColorGeneratorProps.setProperty("service.type","vizmapUI.contextMenu");
		randomColorGeneratorProps.setProperty("title","Random Color");
		randomColorGeneratorProps.setProperty("menu","context");
		registerService(bc,randomColorGenerator, DiscreteMappingGenerator.class, randomColorGeneratorProps);
		
		Properties numberSeriesGeneratorProps = new Properties();
		numberSeriesGeneratorProps.setProperty("service.type","vizmapUI.contextMenu");
		numberSeriesGeneratorProps.setProperty("title","Number Series");
		numberSeriesGeneratorProps.setProperty("menu","context");
		registerService(bc,seriesGenerator, DiscreteMappingGenerator.class, numberSeriesGeneratorProps);
		
		Properties randomNumberGeneratorProps = new Properties();
		randomNumberGeneratorProps.setProperty("service.type","vizmapUI.contextMenu");
		randomNumberGeneratorProps.setProperty("title","Random Numbers");
		randomNumberGeneratorProps.setProperty("menu","context");
		registerService(bc, randomNumberGenerator, DiscreteMappingGenerator.class, randomNumberGeneratorProps);
		
		Properties fitLabelGeneratorProps = new Properties();
		fitLabelGeneratorProps.setProperty("service.type","vizmapUI.contextMenu");
		fitLabelGeneratorProps.setProperty("title","Fit label width (Only works with NAME column to width)");
		fitLabelGeneratorProps.setProperty("menu","context");
		registerService(bc, fitLabelMappingGenerator, DiscreteMappingGenerator.class, fitLabelGeneratorProps);
// TODO				
//		EditSelectedCellAction editAction = new EditSelectedCellAction(editorManager, cyApplicationManagerServiceRef, propertySheetPanel, visualMappingManagerServiceRef);
//		Properties editSelectedProps = new Properties();
//		editSelectedProps.setProperty("service.type","vizmapUI.contextMenu");
//		editSelectedProps.setProperty("title","Edit Selected");
//		editSelectedProps.setProperty("menu","context");
//		registerService(bc, editAction, CyAction.class, editSelectedProps);

//		BypassManager bypassManager = new BypassManager(cyServiceRegistrarServiceRef,editorManager, visualMappingManagerServiceRef);
//		registerServiceListener(bc, bypassManager,"addBypass","removeBypass", RenderingEngineFactory.class);
		
		// Create the main GUI component
		final IconManager iconManager = new IconManager();
		final VizMapperMainPanel vizMapperMainPanel = new VizMapperMainPanel(iconManager);
		
		registerServiceListener(bc, mappingFunctionFactoryManager, "addFactory", "removeFactory", VisualMappingFunctionFactory.class);
		registerServiceListener(bc, editorManager, "addValueEditor", "removeValueEditor", ValueEditor.class);
		registerServiceListener(bc, editorManager, "addVisualPropertyEditor", "removeVisualPropertyEditor", VisualPropertyEditor.class);

		registerServiceListener(bc, editorManager, "addRenderingEngineFactory", "removeRenderingEngineFactory", RenderingEngineFactory.class);
		
		// Start the PureMVC components
		// -------------------------------------------------------------------------------------------------------------
		final VizMapperProxy vizMapperProxy = new VizMapperProxy(servicesUtil);
		
		final VizMapperMediator vizMapperMediator = new VizMapperMediator(vizMapperMainPanel,
																		  servicesUtil,
																		  editorManager,
																		  vizMapPropertyBuilder);
		final VizMapperMenuMediator vizMapperMenuMediator = new VizMapperMenuMediator(vizMapperMainPanel, servicesUtil);
		
		final ImportDefaultVisualStylesCommand importDefaultVisualStylesCommand = new ImportDefaultVisualStylesCommand(servicesUtil);
		final LoadVisualStylesCommand loadVisualStylesCommand = new LoadVisualStylesCommand(servicesUtil);
		final StartupCommand startupCommand = new StartupCommand(vizMapperProxy,
																 vizMapperMediator,
																 vizMapperMenuMediator,
																 importDefaultVisualStylesCommand,
																 loadVisualStylesCommand);
		
		registerAllServices(bc, vizMapperProxy, new Properties());
		registerAllServices(bc, vizMapperMediator, new Properties());
		
		registerServiceListener(bc, vizMapperMediator, "onCyActionRegistered", "onCyActionUnregistered", CyAction.class);
		registerServiceListener(bc, vizMapperMediator, "onTaskFactoryRegistered", "onTaskFactoryUnregistered", TaskFactory.class);
		registerServiceListener(bc, vizMapperMediator, "onMappingGeneratorRegistered", "onMappingGeneratorUnregistered", DiscreteMappingGenerator.class);
		
		registerServiceListener(bc, vizMapperMenuMediator, "onRenderingEngineFactoryRegistered", "onRenderingEngineFactoryUnregistered", RenderingEngineFactory.class);
		
		final VizMapEventHandlerManagerImpl vizMapEventHandlerManager = new VizMapEventHandlerManagerImpl(editorManager,
				attributeSetManager, servicesUtil, vizMapPropertyBuilder, vizMapperMediator);
		registerServiceListener(bc, vizMapEventHandlerManager, "registerPCL", "unregisterPCL", RenderingEngineFactory.class);
		
		new ApplicationFacade(startupCommand).startup();
	}
}

