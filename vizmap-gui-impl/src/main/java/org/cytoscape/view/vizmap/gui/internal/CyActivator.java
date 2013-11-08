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

import org.cytoscape.application.swing.CyAction;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.RenderingEngineFactory;
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
import org.cytoscape.view.vizmap.gui.internal.task.*;
import org.cytoscape.view.vizmap.gui.internal.theme.ThemeManager;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.*;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapPropertyBuilder;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperMainPanel;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperMediator;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperMenuMediator;
import org.cytoscape.view.vizmap.gui.internal.view.editor.*;
import org.cytoscape.view.vizmap.gui.internal.view.editor.propertyeditor.CyColorPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.view.editor.propertyeditor.CyComboBoxPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.view.editor.propertyeditor.CyFontPropertyEditor;
import org.cytoscape.view.vizmap.gui.internal.view.editor.valueeditor.BooleanValueEditor;
import org.cytoscape.view.vizmap.gui.internal.view.editor.valueeditor.CyColorChooser;
import org.cytoscape.view.vizmap.gui.internal.view.editor.valueeditor.NumericValueEditor;
import org.cytoscape.view.vizmap.gui.internal.view.editor.valueeditor.StringValueEditor;
import org.cytoscape.view.vizmap.gui.util.DiscreteMappingGenerator;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;

import java.awt.*;
import java.util.Properties;


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
		
		final ThemeManager themeManager = new ThemeManager();
		
		final CyColorChooser colorChooser = new CyColorChooser();
		final CyColorPropertyEditor cyColorPropertyEditor = new CyColorPropertyEditor(colorChooser, themeManager);
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
		createNewVisualStyleTaskFactoryProps.setProperty("service.type", "vizmapUI.taskFactory");
		createNewVisualStyleTaskFactoryProps.setProperty("title", "Create New Visual Style");
		createNewVisualStyleTaskFactoryProps.setProperty("menu", "main");
		registerAllServices(bc,createNewVisualStyleTaskFactory, createNewVisualStyleTaskFactoryProps);

		final RemoveVisualStyleTaskFactory removeVisualStyleTaskFactory = new RemoveVisualStyleTaskFactory(servicesUtil);
		final Properties removeVisualStyleTaskFactoryProps = new Properties();
		removeVisualStyleTaskFactoryProps.setProperty("service.type", "vizmapUI.taskFactory");
		removeVisualStyleTaskFactoryProps.setProperty("title", "Remove Visual Style");
		removeVisualStyleTaskFactoryProps.setProperty("menu", "main");
		registerAllServices(bc,removeVisualStyleTaskFactory, removeVisualStyleTaskFactoryProps);

		final RenameVisualStyleTaskFactory renameVisualStyleTaskFactory = new RenameVisualStyleTaskFactory(servicesUtil);
		final Properties renameVisualStyleTaskFactoryProps = new Properties();
		renameVisualStyleTaskFactoryProps.setProperty("service.type", "vizmapUI.taskFactory");
		renameVisualStyleTaskFactoryProps.setProperty("title", "Rename Visual Style");
		renameVisualStyleTaskFactoryProps.setProperty("menu", "main");
		registerAllServices(bc, renameVisualStyleTaskFactory, renameVisualStyleTaskFactoryProps);

		final CopyVisualStyleTaskFactory copyVisualStyleTaskFactory = new CopyVisualStyleTaskFactory(servicesUtil);
		final Properties copyVisualStyleTaskFactoryProps = new Properties();
		copyVisualStyleTaskFactoryProps.setProperty("service.type", "vizmapUI.taskFactory");
		copyVisualStyleTaskFactoryProps.setProperty("title", "Copy Visual Style");
		copyVisualStyleTaskFactoryProps.setProperty("menu", "main");
		registerAllServices(bc, copyVisualStyleTaskFactory, copyVisualStyleTaskFactoryProps);

		final CreateLegendTaskFactory createLegendTaskFactory = new CreateLegendTaskFactory(servicesUtil);
		final Properties createLegendTaskFactoryProps = new Properties();
		createLegendTaskFactoryProps.setProperty("service.type", "vizmapUI.taskFactory");
		createLegendTaskFactoryProps.setProperty("title", "Create Legend");
		createLegendTaskFactoryProps.setProperty("menu", "main");
		registerAllServices(bc, createLegendTaskFactory, createLegendTaskFactoryProps);

		// Visual Styles Panel Context Menu
		// -------------------------------------------------------------------------------------------------------------
		final RemoveVisualMappingsTaskFactory removeVisualMappingsTaskFactory = new RemoveVisualMappingsTaskFactory(servicesUtil);
		final Properties removeVisualMappingTaskFactoryProps = new Properties();
		removeVisualMappingTaskFactoryProps.setProperty("service.type", "vizmapUI.taskFactory");
		removeVisualMappingTaskFactoryProps.setProperty("title", "Remove Mappings from Selected Visual Properties");
		removeVisualMappingTaskFactoryProps.setProperty("menu", "context");
		registerAllServices(bc, removeVisualMappingsTaskFactory, removeVisualMappingTaskFactoryProps);
		
		final EditSelectedDiscreteValuesAction editAction = new EditSelectedDiscreteValuesAction(servicesUtil, editorManager);
		final Properties editSelectedProps = new Properties();
		editSelectedProps.setProperty("service.type", "vizmapUI.contextMenu");
		editSelectedProps.setProperty("title", EditSelectedDiscreteValuesAction.NAME);
		editSelectedProps.setProperty("menu", "context");
		registerService(bc, editAction, CyAction.class, editSelectedProps);
		
		final RemoveSelectedDiscreteValuesAction removeAction = new RemoveSelectedDiscreteValuesAction(servicesUtil);
		final Properties removeSelectedProps = new Properties();
		removeSelectedProps.setProperty("service.type", "vizmapUI.contextMenu");
		removeSelectedProps.setProperty("title", RemoveSelectedDiscreteValuesAction.NAME);
		removeSelectedProps.setProperty("menu", "context");
		registerService(bc, removeAction, CyAction.class, removeSelectedProps);
		
		// Discrete value generators:
		final RainbowColorMappingGenerator rainbowGenerator = new RainbowColorMappingGenerator(Color.class);
		final Properties rainbowGeneratorProps = new Properties();
		rainbowGeneratorProps.setProperty("service.type", "vizmapUI.contextMenu");
		rainbowGeneratorProps.setProperty("title", "Rainbow");
		rainbowGeneratorProps.setProperty("menu", "context");
		registerService(bc, rainbowGenerator, DiscreteMappingGenerator.class, rainbowGeneratorProps);

		final RainbowOscColorMappingGenerator rainbowOscGenerator = new RainbowOscColorMappingGenerator(Color.class);
		final Properties rainbowOscGeneratorProps = new Properties();
		rainbowOscGeneratorProps.setProperty("service.type", "vizmapUI.contextMenu");
		rainbowOscGeneratorProps.setProperty("title", "Rainbow OSC");
		rainbowOscGeneratorProps.setProperty("menu", "context");
		registerService(bc, rainbowOscGenerator, DiscreteMappingGenerator.class, rainbowOscGeneratorProps);

		final RandomColorMappingGenerator randomColorGenerator = new RandomColorMappingGenerator(Color.class);
		final Properties randomColorGeneratorProps = new Properties();
		randomColorGeneratorProps.setProperty("service.type", "vizmapUI.contextMenu");
		randomColorGeneratorProps.setProperty("title", "Random Color");
		randomColorGeneratorProps.setProperty("menu", "context");
		registerService(bc, randomColorGenerator, DiscreteMappingGenerator.class, randomColorGeneratorProps);
		
		final NumberSeriesMappingGenerator<Number> seriesGenerator = new NumberSeriesMappingGenerator<Number>(Number.class);
		final Properties numberSeriesGeneratorProps = new Properties();
		numberSeriesGeneratorProps.setProperty("service.type", "vizmapUI.contextMenu");
		numberSeriesGeneratorProps.setProperty("title", "Number Series");
		numberSeriesGeneratorProps.setProperty("menu", "context");
		registerService(bc, seriesGenerator, DiscreteMappingGenerator.class, numberSeriesGeneratorProps);
		
		final RandomNumberMappingGenerator randomNumberGenerator = new RandomNumberMappingGenerator();
		final Properties randomNumberGeneratorProps = new Properties();
		randomNumberGeneratorProps.setProperty("service.type", "vizmapUI.contextMenu");
		randomNumberGeneratorProps.setProperty("title", "Random Numbers");
		randomNumberGeneratorProps.setProperty("menu", "context");
		registerService(bc, randomNumberGenerator, DiscreteMappingGenerator.class, randomNumberGeneratorProps);
		
		final FitLabelMappingGenerator<Number> fitLabelMappingGenerator = new FitLabelMappingGenerator<Number>(Number.class, servicesUtil);
		final Properties fitLabelGeneratorProps = new Properties();
		fitLabelGeneratorProps.setProperty("service.type", "vizmapUI.contextMenu");
		fitLabelGeneratorProps.setProperty("title", "Fit label width (Only works with NAME column to width)");
		fitLabelGeneratorProps.setProperty("menu", "context");
		registerService(bc, fitLabelMappingGenerator, DiscreteMappingGenerator.class, fitLabelGeneratorProps);
		
		// Create the main GUI component
		// -------------------------------------------------------------------------------------------------------------
		final VizMapperMainPanel vizMapperMainPanel = new VizMapperMainPanel(themeManager);
		
		// Start the PureMVC components
		// -------------------------------------------------------------------------------------------------------------
		final VizMapperProxy vizMapperProxy = new VizMapperProxy(servicesUtil);
		final PropsProxy propsProxy = new PropsProxy(servicesUtil);
		
		final VizMapPropertyBuilder vizMapPropertyBuilder = new VizMapPropertyBuilder(editorManager, mappingFunctionFactoryManager, servicesUtil);
		
		final VizMapperMediator vizMapperMediator = new VizMapperMediator(vizMapperMainPanel,
																		  servicesUtil,
																		  vizMapPropertyBuilder,
																		  themeManager);
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
