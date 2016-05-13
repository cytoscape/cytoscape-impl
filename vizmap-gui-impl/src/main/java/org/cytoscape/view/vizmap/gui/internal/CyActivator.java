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

import org.cytoscape.application.swing.CyAction;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.values.BendFactory;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingCellRendererFactory;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyValueEditor;
import org.cytoscape.view.vizmap.gui.internal.action.EditSelectedDiscreteValuesAction;
import org.cytoscape.view.vizmap.gui.internal.action.RemoveSelectedDiscreteValuesAction;
import org.cytoscape.view.vizmap.gui.internal.controller.StartupCommand;
import org.cytoscape.view.vizmap.gui.internal.event.VizMapEventHandlerManagerImpl;
import org.cytoscape.view.vizmap.gui.internal.model.AttributeSetProxy;
import org.cytoscape.view.vizmap.gui.internal.model.MappingFunctionFactoryProxy;
import org.cytoscape.view.vizmap.gui.internal.model.PropsProxy;
import org.cytoscape.view.vizmap.gui.internal.model.VizMapperProxy;
import org.cytoscape.view.vizmap.gui.internal.task.ClearAllBendsForThisEdgeTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.task.CopyVisualStyleTask;
import org.cytoscape.view.vizmap.gui.internal.task.CopyVisualStyleTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.task.CreateLegendTask;
import org.cytoscape.view.vizmap.gui.internal.task.CreateLegendTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.task.CreateNewVisualStyleTask;
import org.cytoscape.view.vizmap.gui.internal.task.CreateNewVisualStyleTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.task.MakeVisualStylesDefaultTask;
import org.cytoscape.view.vizmap.gui.internal.task.MakeVisualStylesDefaultTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.task.RemoveVisualMappingsTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.task.RemoveVisualStyleTask;
import org.cytoscape.view.vizmap.gui.internal.task.RemoveVisualStyleTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.task.RenameVisualStyleTask;
import org.cytoscape.view.vizmap.gui.internal.task.RenameVisualStyleTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.util.ServicePropertiesUtil;
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
import org.cytoscape.view.vizmap.gui.internal.view.editor.valueeditor.FontValueEditor;
import org.cytoscape.view.vizmap.gui.internal.view.editor.valueeditor.NumericValueEditor;
import org.cytoscape.view.vizmap.gui.internal.view.editor.valueeditor.StringValueEditor;
import org.cytoscape.view.vizmap.gui.util.DiscreteMappingGenerator;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;


public class CyActivator extends AbstractCyActivator {

	@Override
	public void start(final BundleContext bc) {
		final CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);
		final ServicesUtil servicesUtil = new ServicesUtil(serviceRegistrar, ApplicationFacade.NAME);
		
		final AttributeSetProxy attributeSetProxy = new AttributeSetProxy(servicesUtil);
		final MappingFunctionFactoryProxy mappingFactoryProxy = new MappingFunctionFactoryProxy(servicesUtil);
		
		final ContinuousMappingCellRendererFactory continuousMappingCellRendererFactory = getService(bc, ContinuousMappingCellRendererFactory.class);
		
		final EditorManagerImpl editorManager = new EditorManagerImpl(attributeSetProxy, mappingFactoryProxy, continuousMappingCellRendererFactory, servicesUtil);
		// These listeners must be registered before the ValueEditors and VisualPropertyEditors:
		registerServiceListener(bc, editorManager, "addValueEditor", "removeValueEditor", ValueEditor.class);
		registerServiceListener(bc, editorManager, "addVisualPropertyValueEditor", "removeVisualPropertyValueEditor", VisualPropertyValueEditor.class);
		registerServiceListener(bc, editorManager, "addVisualPropertyEditor", "removeVisualPropertyEditor", VisualPropertyEditor.class);
		registerServiceListener(bc, editorManager, "addRenderingEngineFactory", "removeRenderingEngineFactory", RenderingEngineFactory.class);
		registerAllServices(bc, editorManager, new Properties());
		
		final MappingFunctionFactoryManagerImpl mappingFunctionFactoryManager = new MappingFunctionFactoryManagerImpl();
		registerServiceListener(bc, mappingFunctionFactoryManager, "addFactory", "removeFactory", VisualMappingFunctionFactory.class);
		registerAllServices(bc, mappingFunctionFactoryManager, new Properties());
		
		final NumericValueEditor<Double> doubleValueEditor = new NumericValueEditor<Double>(Double.class);
		final NumericValueEditor<Integer> integerValueEditor = new NumericValueEditor<Integer>(Integer.class);
		final NumericValueEditor<Float> floatValueEditor = new NumericValueEditor<Float>(Float.class);
		final StringValueEditor stringValueEditor = new StringValueEditor();
		final BooleanValueEditor booleanValueEditor = new BooleanValueEditor();
		final FontValueEditor fontValueEditor = new FontValueEditor(servicesUtil);
		
		final CyColorChooser colorChooser = new CyColorChooser();
		final CyColorPropertyEditor cyColorPropertyEditor = new CyColorPropertyEditor(colorChooser, servicesUtil);
		final CyFontPropertyEditor cyFontPropertyEditor = new CyFontPropertyEditor();
		
		final ColorVisualPropertyEditor colorPropertyEditor = new ColorVisualPropertyEditor(Paint.class, editorManager, cyColorPropertyEditor, continuousMappingCellRendererFactory);
		final NumberVisualPropertyEditor<Double> doublePropertyEditor = new NumberVisualPropertyEditor<Double>(Double.class, continuousMappingCellRendererFactory);
		final NumberVisualPropertyEditor<Integer> integerPropertyEditor = new NumberVisualPropertyEditor<Integer>(Integer.class, continuousMappingCellRendererFactory);
		final NumberVisualPropertyEditor<Float> floatPropertyEditor = new NumberVisualPropertyEditor<Float>(Float.class, continuousMappingCellRendererFactory);
		
		final FontVisualPropertyEditor fontVisualPropertyEditor = new FontVisualPropertyEditor(Font.class, cyFontPropertyEditor, continuousMappingCellRendererFactory);
		final StringVisualPropertyEditor stringPropertyEditor = new StringVisualPropertyEditor(continuousMappingCellRendererFactory);
		final CyComboBoxPropertyEditor booleanEditor = new CyComboBoxPropertyEditor();
		booleanEditor.setAvailableValues(new Boolean[] {true, false});
		final BooleanVisualPropertyEditor booleanVisualPropertyEditor = new BooleanVisualPropertyEditor(booleanEditor, continuousMappingCellRendererFactory);
		
		// Context menu for edge bend
		final BendFactory bf = getService(bc, BendFactory.class);

		final Properties clearAllBendsForThisEdgeProps = new Properties();
		clearAllBendsForThisEdgeProps.put(ServiceProperties.PREFERRED_MENU, ServiceProperties.EDGE_EDIT_MENU);
		clearAllBendsForThisEdgeProps.put(ServiceProperties.TITLE, "Clear All Bends For This Edge");
		clearAllBendsForThisEdgeProps.put(ServiceProperties.MENU_GRAVITY, "5.0");
		clearAllBendsForThisEdgeProps.put(ServiceProperties.INSERT_SEPARATOR_BEFORE, "true");
		final ClearAllBendsForThisEdgeTaskFactory clearAllBendsForThisEdgeTaskFactory = new ClearAllBendsForThisEdgeTaskFactory(bf, servicesUtil);
		registerService(bc, clearAllBendsForThisEdgeTaskFactory, EdgeViewTaskFactory.class, clearAllBendsForThisEdgeProps);

		// Register ValueEditors and VisualPropertyEditors
		// -------------------------------------------------------------------------------------------------------------
		registerAllServices(bc, attributeSetProxy, new Properties());
		registerAllServices(bc, editorManager.getNodeEditor(), new Properties());
		registerAllServices(bc, editorManager.getEdgeEditor(), new Properties());
		registerAllServices(bc, editorManager.getNetworkEditor(), new Properties());
		registerAllServices(bc, colorChooser, new Properties());
		registerAllServices(bc, doubleValueEditor, new Properties());
		registerAllServices(bc, integerValueEditor, new Properties());
		registerAllServices(bc, floatValueEditor, new Properties());
		registerAllServices(bc, stringValueEditor, new Properties());
		registerAllServices(bc, booleanValueEditor, new Properties());
		registerAllServices(bc, fontValueEditor, new Properties());
		
		registerAllServices(bc, colorPropertyEditor, new Properties());
		registerAllServices(bc, doublePropertyEditor, new Properties());
		registerAllServices(bc, floatPropertyEditor, new Properties());
		registerAllServices(bc, integerPropertyEditor, new Properties());
		
		registerAllServices(bc, fontVisualPropertyEditor, new Properties());
		registerAllServices(bc, stringPropertyEditor, new Properties());
		registerAllServices(bc, booleanVisualPropertyEditor, new Properties());
		
		// Tasks
		// -------------------------------------------------------------------------------------------------------------
		final CreateNewVisualStyleTaskFactory createNewVisualStyleTaskFactory = new CreateNewVisualStyleTaskFactory(servicesUtil);
		final Properties createNewVisualStyleTaskFactoryProps = new Properties();
		createNewVisualStyleTaskFactoryProps.setProperty(ServicePropertiesUtil.SERVICE_TYPE, "vizmapUI");
		createNewVisualStyleTaskFactoryProps.setProperty(ServicePropertiesUtil.TITLE, CreateNewVisualStyleTask.TITLE + "...");
		createNewVisualStyleTaskFactoryProps.setProperty(ServicePropertiesUtil.MENU_ID, ServicePropertiesUtil.MAIN_MENU);
		createNewVisualStyleTaskFactoryProps.setProperty(ServicePropertiesUtil.GRAVITY, "1.0");
		registerAllServices(bc,createNewVisualStyleTaskFactory, createNewVisualStyleTaskFactoryProps);

		final CopyVisualStyleTaskFactory copyVisualStyleTaskFactory = new CopyVisualStyleTaskFactory(servicesUtil);
		final Properties copyVisualStyleTaskFactoryProps = new Properties();
		copyVisualStyleTaskFactoryProps.setProperty(ServicePropertiesUtil.SERVICE_TYPE, "vizmapUI");
		copyVisualStyleTaskFactoryProps.setProperty(ServicePropertiesUtil.TITLE, CopyVisualStyleTask.TITLE + "...");
		copyVisualStyleTaskFactoryProps.setProperty(ServicePropertiesUtil.MENU_ID, ServicePropertiesUtil.MAIN_MENU);
		copyVisualStyleTaskFactoryProps.setProperty(ServicePropertiesUtil.GRAVITY, "2.0");
		registerAllServices(bc, copyVisualStyleTaskFactory, copyVisualStyleTaskFactoryProps);
		
		final RenameVisualStyleTaskFactory renameVisualStyleTaskFactory = new RenameVisualStyleTaskFactory(servicesUtil);
		final Properties renameVisualStyleTaskFactoryProps = new Properties();
		renameVisualStyleTaskFactoryProps.setProperty(ServicePropertiesUtil.SERVICE_TYPE, "vizmapUI");
		renameVisualStyleTaskFactoryProps.setProperty(ServicePropertiesUtil.TITLE, RenameVisualStyleTask.TITLE + "...");
		renameVisualStyleTaskFactoryProps.setProperty(ServicePropertiesUtil.MENU_ID, ServicePropertiesUtil.MAIN_MENU);
		renameVisualStyleTaskFactoryProps.setProperty(ServicePropertiesUtil.GRAVITY, "3.0");
		registerAllServices(bc, renameVisualStyleTaskFactory, renameVisualStyleTaskFactoryProps);
		
		final RemoveVisualStyleTaskFactory removeVisualStyleTaskFactory = new RemoveVisualStyleTaskFactory(servicesUtil);
		final Properties removeVisualStyleTaskFactoryProps = new Properties();
		removeVisualStyleTaskFactoryProps.setProperty(ServicePropertiesUtil.SERVICE_TYPE, "vizmapUI");
		removeVisualStyleTaskFactoryProps.setProperty(ServicePropertiesUtil.TITLE, RemoveVisualStyleTask.TITLE);
		removeVisualStyleTaskFactoryProps.setProperty(ServicePropertiesUtil.MENU_ID, ServicePropertiesUtil.MAIN_MENU);
		removeVisualStyleTaskFactoryProps.setProperty(ServicePropertiesUtil.GRAVITY, "4.0");
		registerAllServices(bc,removeVisualStyleTaskFactory, removeVisualStyleTaskFactoryProps);

		final MakeVisualStylesDefaultTaskFactory makeVisualStylesDefaultTaskFactory = new MakeVisualStylesDefaultTaskFactory(servicesUtil);
		final Properties makeVisualStylesDefaultTaskFactoryProps = new Properties();
		makeVisualStylesDefaultTaskFactoryProps.setProperty(ServicePropertiesUtil.SERVICE_TYPE, "vizmapUI");
		makeVisualStylesDefaultTaskFactoryProps.setProperty(ServicePropertiesUtil.TITLE, MakeVisualStylesDefaultTask.TITLE);
		makeVisualStylesDefaultTaskFactoryProps.setProperty(ServicePropertiesUtil.MENU_ID, ServicePropertiesUtil.MAIN_MENU);
		makeVisualStylesDefaultTaskFactoryProps.setProperty(ServicePropertiesUtil.GRAVITY, "5.0");
		makeVisualStylesDefaultTaskFactoryProps.setProperty(ServicePropertiesUtil.INSERT_SEPARATOR_BEFORE, "true");
		registerAllServices(bc, makeVisualStylesDefaultTaskFactory, makeVisualStylesDefaultTaskFactoryProps);

		final CreateLegendTaskFactory createLegendTaskFactory = new CreateLegendTaskFactory(servicesUtil);
		final Properties createLegendTaskFactoryProps = new Properties();
		createLegendTaskFactoryProps.setProperty(ServicePropertiesUtil.SERVICE_TYPE, "vizmapUI");
		createLegendTaskFactoryProps.setProperty(ServicePropertiesUtil.TITLE, CreateLegendTask.TITLE + "...");
		createLegendTaskFactoryProps.setProperty(ServicePropertiesUtil.MENU_ID, ServicePropertiesUtil.MAIN_MENU);
		createLegendTaskFactoryProps.setProperty(ServicePropertiesUtil.GRAVITY, "6.0");
		createLegendTaskFactoryProps.setProperty(ServicePropertiesUtil.INSERT_SEPARATOR_BEFORE, "true");
		registerAllServices(bc, createLegendTaskFactory, createLegendTaskFactoryProps);

		// Visual Styles Panel Context Menu
		// -------------------------------------------------------------------------------------------------------------
		// Edit sub-menu
		final EditSelectedDiscreteValuesAction editAction = new EditSelectedDiscreteValuesAction(servicesUtil, editorManager);
		final Properties editSelectedProps = new Properties();
		editSelectedProps.setProperty(ServicePropertiesUtil.SERVICE_TYPE, "vizmapUI");
		editSelectedProps.setProperty(ServicePropertiesUtil.TITLE, EditSelectedDiscreteValuesAction.NAME);
		editSelectedProps.setProperty(ServicePropertiesUtil.MENU_ID, "context");
		editSelectedProps.setProperty(ServicePropertiesUtil.GRAVITY, "1.0");
		registerService(bc, editAction, CyAction.class, editSelectedProps);
		
		final RemoveSelectedDiscreteValuesAction removeAction = new RemoveSelectedDiscreteValuesAction(servicesUtil);
		final Properties removeSelectedProps = new Properties();
		removeSelectedProps.setProperty(ServicePropertiesUtil.SERVICE_TYPE, "vizmapUI");
		removeSelectedProps.setProperty(ServicePropertiesUtil.TITLE, RemoveSelectedDiscreteValuesAction.NAME);
		removeSelectedProps.setProperty(ServicePropertiesUtil.MENU_ID, "context");
		removeSelectedProps.setProperty(ServicePropertiesUtil.GRAVITY, "2.0");
		registerService(bc, removeAction, CyAction.class, removeSelectedProps);
		
		final RemoveVisualMappingsTaskFactory removeVisualMappingsTaskFactory = new RemoveVisualMappingsTaskFactory(servicesUtil);
		final Properties removeVisualMappingTaskFactoryProps = new Properties();
		removeVisualMappingTaskFactoryProps.setProperty(ServicePropertiesUtil.SERVICE_TYPE, "vizmapUI");
		removeVisualMappingTaskFactoryProps.setProperty(ServicePropertiesUtil.TITLE, "Remove Mappings from Selected Visual Properties");
		removeVisualMappingTaskFactoryProps.setProperty(ServicePropertiesUtil.MENU_ID, "context");
		removeVisualMappingTaskFactoryProps.setProperty(ServicePropertiesUtil.GRAVITY, "3.0");
		removeVisualMappingTaskFactoryProps.setProperty(ServicePropertiesUtil.INSERT_SEPARATOR_BEFORE, "true");
		registerAllServices(bc, removeVisualMappingsTaskFactory, removeVisualMappingTaskFactoryProps);
		
		// Discrete value generators:
		final RainbowColorMappingGenerator rainbowGenerator = new RainbowColorMappingGenerator(Color.class);
		final Properties rainbowGeneratorProps = new Properties();
		rainbowGeneratorProps.setProperty(ServicePropertiesUtil.SERVICE_TYPE, "vizmapUI");
		rainbowGeneratorProps.setProperty(ServicePropertiesUtil.TITLE, "Rainbow");
		rainbowGeneratorProps.setProperty(ServicePropertiesUtil.MENU_ID, "context");
		registerService(bc, rainbowGenerator, DiscreteMappingGenerator.class, rainbowGeneratorProps);

		final RainbowOscColorMappingGenerator rainbowOscGenerator = new RainbowOscColorMappingGenerator(Color.class);
		final Properties rainbowOscGeneratorProps = new Properties();
		rainbowOscGeneratorProps.setProperty(ServicePropertiesUtil.SERVICE_TYPE, "vizmapUI.contextMenu");
		rainbowOscGeneratorProps.setProperty(ServicePropertiesUtil.TITLE, "Rainbow OSC");
		rainbowOscGeneratorProps.setProperty(ServicePropertiesUtil.MENU_ID, "context");
		registerService(bc, rainbowOscGenerator, DiscreteMappingGenerator.class, rainbowOscGeneratorProps);

		final RandomColorMappingGenerator randomColorGenerator = new RandomColorMappingGenerator(Color.class);
		final Properties randomColorGeneratorProps = new Properties();
		randomColorGeneratorProps.setProperty(ServicePropertiesUtil.SERVICE_TYPE, "vizmapUI.contextMenu");
		randomColorGeneratorProps.setProperty(ServicePropertiesUtil.TITLE, "Random Color");
		randomColorGeneratorProps.setProperty(ServicePropertiesUtil.MENU_ID, "context");
		registerService(bc, randomColorGenerator, DiscreteMappingGenerator.class, randomColorGeneratorProps);
		
		final NumberSeriesMappingGenerator<Number> seriesGenerator = new NumberSeriesMappingGenerator<Number>(Number.class);
		final Properties numberSeriesGeneratorProps = new Properties();
		numberSeriesGeneratorProps.setProperty(ServicePropertiesUtil.SERVICE_TYPE, "vizmapUI.contextMenu");
		numberSeriesGeneratorProps.setProperty(ServicePropertiesUtil.TITLE, "Number Series");
		numberSeriesGeneratorProps.setProperty(ServicePropertiesUtil.MENU_ID, "context");
		registerService(bc, seriesGenerator, DiscreteMappingGenerator.class, numberSeriesGeneratorProps);
		
		final RandomNumberMappingGenerator randomNumberGenerator = new RandomNumberMappingGenerator();
		final Properties randomNumberGeneratorProps = new Properties();
		randomNumberGeneratorProps.setProperty(ServicePropertiesUtil.SERVICE_TYPE, "vizmapUI.contextMenu");
		randomNumberGeneratorProps.setProperty(ServicePropertiesUtil.TITLE, "Random Numbers");
		randomNumberGeneratorProps.setProperty(ServicePropertiesUtil.MENU_ID, "context");
		registerService(bc, randomNumberGenerator, DiscreteMappingGenerator.class, randomNumberGeneratorProps);
		
		final FitLabelMappingGenerator<Double> fitLabelMappingGenerator = new FitLabelMappingGenerator<Double>(Double.class, servicesUtil);
		final Properties fitLabelGeneratorProps = new Properties();
		fitLabelGeneratorProps.setProperty(ServicePropertiesUtil.SERVICE_TYPE, "vizmapUI.contextMenu");
		fitLabelGeneratorProps.setProperty(ServicePropertiesUtil.TITLE, "Fit label width (only works with 'name' column to node size or width)");
		fitLabelGeneratorProps.setProperty(ServicePropertiesUtil.MENU_ID, "context");
		registerService(bc, fitLabelMappingGenerator, DiscreteMappingGenerator.class, fitLabelGeneratorProps);
		
		// Create the main GUI component
		// -------------------------------------------------------------------------------------------------------------
		final VizMapperMainPanel vizMapperMainPanel = new VizMapperMainPanel(servicesUtil);
		
		// Start the PureMVC components
		// -------------------------------------------------------------------------------------------------------------
		final VizMapperProxy vizMapperProxy = new VizMapperProxy(servicesUtil);
		final PropsProxy propsProxy = new PropsProxy(servicesUtil);
		
		final VizMapPropertyBuilder vizMapPropertyBuilder = new VizMapPropertyBuilder(editorManager, mappingFunctionFactoryManager, servicesUtil);
		
		final VizMapperMediator vizMapperMediator = new VizMapperMediator(vizMapperMainPanel,
																		  servicesUtil,
																		  vizMapPropertyBuilder);
		final VizMapperMenuMediator vizMapperMenuMediator = new VizMapperMenuMediator(vizMapperMainPanel, servicesUtil);
		
		final StartupCommand startupCommand = new StartupCommand(vizMapperProxy,
																 attributeSetProxy,
																 mappingFactoryProxy,
																 propsProxy,
																 vizMapperMediator,
																 vizMapperMenuMediator,
																 servicesUtil);
		
		registerAllServices(bc, vizMapperProxy, new Properties());
		registerAllServices(bc, mappingFactoryProxy, new Properties());
		registerAllServices(bc, propsProxy, new Properties());
		
		registerAllServices(bc, vizMapperMediator, new Properties());
		
		registerServiceListener(bc, vizMapperMediator, "onCyActionRegistered", "onCyActionUnregistered", CyAction.class);
		registerServiceListener(bc, vizMapperMediator, "onTaskFactoryRegistered", "onTaskFactoryUnregistered", TaskFactory.class);
		registerServiceListener(bc, vizMapperMediator, "onMappingGeneratorRegistered", "onMappingGeneratorUnregistered", DiscreteMappingGenerator.class);
		
		registerServiceListener(bc, vizMapperMenuMediator, "onRenderingEngineFactoryRegistered", "onRenderingEngineFactoryUnregistered", RenderingEngineFactory.class);
		
		final VizMapEventHandlerManagerImpl vizMapEventHandlerManager = new VizMapEventHandlerManagerImpl(editorManager,
				attributeSetProxy, servicesUtil, vizMapPropertyBuilder, vizMapperMediator);
		registerServiceListener(bc, vizMapEventHandlerManager, "registerPCL", "unregisterPCL", RenderingEngineFactory.class);
		
		// Startup the framework
		new ApplicationFacade(startupCommand).startup();
	}
}
