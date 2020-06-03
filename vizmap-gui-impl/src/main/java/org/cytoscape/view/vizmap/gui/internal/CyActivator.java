package org.cytoscape.view.vizmap.gui.internal;

import static org.cytoscape.application.swing.ActionEnableSupport.ENABLE_FOR_NETWORK_AND_VIEW;
import static org.cytoscape.work.ServiceProperties.EDGE_EDIT_MENU;
import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;
import static org.cytoscape.work.ServiceProperties.IN_NETWORK_PANEL_CONTEXT_MENU;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.util.Properties;

import org.cytoscape.application.swing.CyAction;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.TableColumnTaskFactory;
import org.cytoscape.util.color.BrewerType;
import org.cytoscape.util.color.Palette;
import org.cytoscape.util.color.PaletteProvider;
import org.cytoscape.util.color.PaletteProviderManager;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.gui.editor.ContinuousMappingCellRendererFactory;
import org.cytoscape.view.vizmap.gui.editor.ValueEditor;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.editor.VisualPropertyValueEditor;
import org.cytoscape.view.vizmap.gui.internal.action.ApplyVisualStyleAction;
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
import org.cytoscape.view.vizmap.gui.internal.task.RemoveVisualStylesTask;
import org.cytoscape.view.vizmap.gui.internal.task.RemoveVisualStylesTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.task.RenameVisualStyleTask;
import org.cytoscape.view.vizmap.gui.internal.task.RenameVisualStyleTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.util.ServicePropertiesUtil;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.FitLabelMappingGenerator;
import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.NumberSeriesMappingGenerator;
import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.PaletteMappingWrapper;
// import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.RainbowOscColorMappingGenerator;
// import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.RandomColorMappingGenerator;
import org.cytoscape.view.vizmap.gui.internal.util.mapgenerator.RandomNumberMappingGenerator;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapPropertyBuilder;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperMainPanel;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperMediator;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperMenuMediator;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperTableDialog;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperTableMediator;
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
import org.cytoscape.view.vizmap.gui.internal.view.table.TableStyleDialogTaskFactory;
import org.cytoscape.view.vizmap.gui.util.DiscreteMappingGenerator;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

	@Override
	public void start(final BundleContext bc) {
		var serviceRegistrar = getService(bc, CyServiceRegistrar.class);
		var servicesUtil = new ServicesUtil(serviceRegistrar, ApplicationFacade.NAME);
		
		var attributeSetProxy = new AttributeSetProxy(servicesUtil);
		var mappingFactoryProxy = new MappingFunctionFactoryProxy(servicesUtil);

		var paletteProviderManager = getService(bc, PaletteProviderManager.class);
		generateColorMappingGenerators(serviceRegistrar, paletteProviderManager);
		
		var editorManager = new EditorManagerImpl(attributeSetProxy, mappingFactoryProxy, servicesUtil);
		// These listeners must be registered before the ValueEditors and VisualPropertyEditors:
		registerServiceListener(bc, editorManager::addValueEditor, editorManager::removeValueEditor, ValueEditor.class);
		registerServiceListener(bc, editorManager::addVisualPropertyValueEditor, editorManager::removeVisualPropertyValueEditor, VisualPropertyValueEditor.class);
		registerServiceListener(bc, editorManager::addVisualPropertyEditor, editorManager::removeVisualPropertyEditor, VisualPropertyEditor.class);
		registerServiceListener(bc, editorManager::addRenderingEngineFactory, editorManager::removeRenderingEngineFactory, RenderingEngineFactory.class);
		registerAllServices(bc, editorManager);
		
		var mappingFunctionFactoryManager = new MappingFunctionFactoryManagerImpl();
		registerServiceListener(bc, mappingFunctionFactoryManager::addFactory, mappingFunctionFactoryManager::removeFactory, VisualMappingFunctionFactory.class);
		registerAllServices(bc, mappingFunctionFactoryManager);
		
		var doubleValueEditor = new NumericValueEditor<Double>(Double.class);
		var integerValueEditor = new NumericValueEditor<Integer>(Integer.class);
		var floatValueEditor = new NumericValueEditor<Float>(Float.class);
		var stringValueEditor = new StringValueEditor();
		var booleanValueEditor = new BooleanValueEditor();
		var fontValueEditor = new FontValueEditor(servicesUtil);
		
		var colorChooser = new CyColorChooser();
		var cyColorPropertyEditor = new CyColorPropertyEditor(colorChooser, servicesUtil);
		var cyFontPropertyEditor = new CyFontPropertyEditor();
		
		var cmCellRendererFactory = getService(bc, ContinuousMappingCellRendererFactory.class);
		
		var colorPropertyEditor = new ColorVisualPropertyEditor(Paint.class, editorManager, cyColorPropertyEditor, cmCellRendererFactory);
		var doublePropertyEditor = new NumberVisualPropertyEditor<Double>(Double.class, cmCellRendererFactory);
		var integerPropertyEditor = new NumberVisualPropertyEditor<Integer>(Integer.class, cmCellRendererFactory);
		var floatPropertyEditor = new NumberVisualPropertyEditor<Float>(Float.class, cmCellRendererFactory);
		
		var fontVisualPropertyEditor = new FontVisualPropertyEditor(Font.class, cyFontPropertyEditor, cmCellRendererFactory);
		var stringPropertyEditor = new StringVisualPropertyEditor(cmCellRendererFactory, servicesUtil);
		var booleanEditor = new CyComboBoxPropertyEditor();
		booleanEditor.setAvailableValues(new Boolean[] {true, false});
		var booleanVisualPropertyEditor = new BooleanVisualPropertyEditor(booleanEditor, cmCellRendererFactory);
		
		// Context menu for edge bend
		{
			var factory = new ClearAllBendsForThisEdgeTaskFactory(servicesUtil);
			var props = new Properties();
			props.put(PREFERRED_MENU, EDGE_EDIT_MENU);
			props.put(TITLE, "Clear All Bends For This Edge");
			props.put(MENU_GRAVITY, "5.0");
			props.put(INSERT_SEPARATOR_BEFORE, "true");
			registerService(bc, factory, EdgeViewTaskFactory.class, props);
		}

		// Register ValueEditors and VisualPropertyEditors
		// -------------------------------------------------------------------------------------------------------------
		registerAllServices(bc, attributeSetProxy);
		registerAllServices(bc, editorManager.getNodeEditor());
		registerAllServices(bc, editorManager.getEdgeEditor());
		registerAllServices(bc, editorManager.getNetworkEditor());
		registerAllServices(bc, colorChooser);
		registerAllServices(bc, doubleValueEditor);
		registerAllServices(bc, integerValueEditor);
		registerAllServices(bc, floatValueEditor);
		registerAllServices(bc, stringValueEditor);
		registerAllServices(bc, booleanValueEditor);
		registerAllServices(bc, fontValueEditor);
		
		registerAllServices(bc, colorPropertyEditor);
		registerAllServices(bc, doublePropertyEditor);
		registerAllServices(bc, floatPropertyEditor);
		registerAllServices(bc, integerPropertyEditor);
		
		registerAllServices(bc, fontVisualPropertyEditor);
		registerAllServices(bc, stringPropertyEditor);
		registerAllServices(bc, booleanVisualPropertyEditor);
		
		// Tasks
		// -------------------------------------------------------------------------------------------------------------
		{
			var factory = new CreateNewVisualStyleTaskFactory(servicesUtil);
			var props = new Properties();
			props.setProperty(ServicePropertiesUtil.SERVICE_TYPE, "vizmapUI");
			props.setProperty(ServicePropertiesUtil.TITLE, CreateNewVisualStyleTask.TITLE + "...");
			props.setProperty(ServicePropertiesUtil.MENU_ID, ServicePropertiesUtil.MAIN_MENU);
			props.setProperty(ServicePropertiesUtil.GRAVITY, "1.0");
			registerAllServices(bc, factory, props);
		}
		{
			var factory = new CopyVisualStyleTaskFactory(servicesUtil);
			var props = new Properties();
			props.setProperty(ServicePropertiesUtil.SERVICE_TYPE, "vizmapUI");
			props.setProperty(ServicePropertiesUtil.TITLE, CopyVisualStyleTask.TITLE + "...");
			props.setProperty(ServicePropertiesUtil.MENU_ID, ServicePropertiesUtil.MAIN_MENU);
			props.setProperty(ServicePropertiesUtil.GRAVITY, "2.0");
			registerAllServices(bc, factory, props);
		}
		{
			var factory = new RenameVisualStyleTaskFactory(servicesUtil);
			var props = new Properties();
			props.setProperty(ServicePropertiesUtil.SERVICE_TYPE, "vizmapUI");
			props.setProperty(ServicePropertiesUtil.TITLE, RenameVisualStyleTask.TITLE + "...");
			props.setProperty(ServicePropertiesUtil.MENU_ID, ServicePropertiesUtil.MAIN_MENU);
			props.setProperty(ServicePropertiesUtil.GRAVITY, "3.0");
			registerAllServices(bc, factory, props);
		}
		{
			var factory = new RemoveVisualStylesTaskFactory(servicesUtil);
			var props = new Properties();
			props.setProperty(ServicePropertiesUtil.SERVICE_TYPE, "vizmapUI");
			props.setProperty(ServicePropertiesUtil.TITLE, RemoveVisualStylesTask.TITLE);
			props.setProperty(ServicePropertiesUtil.MENU_ID, ServicePropertiesUtil.MAIN_MENU);
			props.setProperty(ServicePropertiesUtil.GRAVITY, "4.0");
			registerAllServices(bc, factory, props);
		}
		{
			var factory = new MakeVisualStylesDefaultTaskFactory(servicesUtil);
			var props = new Properties();
			props.setProperty(ServicePropertiesUtil.SERVICE_TYPE, "vizmapUI");
			props.setProperty(ServicePropertiesUtil.TITLE, MakeVisualStylesDefaultTask.TITLE);
			props.setProperty(ServicePropertiesUtil.MENU_ID, ServicePropertiesUtil.MAIN_MENU);
			props.setProperty(ServicePropertiesUtil.GRAVITY, "5.0");
			props.setProperty(ServicePropertiesUtil.INSERT_SEPARATOR_BEFORE, "true");
			registerAllServices(bc, factory, props);
		}
		{
			var factory = new CreateLegendTaskFactory(servicesUtil);
			var props = new Properties();
			props.setProperty(ServicePropertiesUtil.SERVICE_TYPE, "vizmapUI");
			props.setProperty(ServicePropertiesUtil.TITLE, CreateLegendTask.TITLE + "...");
			props.setProperty(ServicePropertiesUtil.MENU_ID, ServicePropertiesUtil.MAIN_MENU);
			props.setProperty(ServicePropertiesUtil.GRAVITY, "6.0");
			props.setProperty(ServicePropertiesUtil.INSERT_SEPARATOR_BEFORE, "true");
			registerAllServices(bc, factory, props);
		}

		// Visual Styles Panel Context Menu
		// -------------------------------------------------------------------------------------------------------------
		// Edit sub-menu
		{
			var editAction = new EditSelectedDiscreteValuesAction(servicesUtil, editorManager);
			var props = new Properties();
			props.setProperty(ServicePropertiesUtil.SERVICE_TYPE, "vizmapUI");
			props.setProperty(ServicePropertiesUtil.TITLE, EditSelectedDiscreteValuesAction.NAME);
			props.setProperty(ServicePropertiesUtil.MENU_ID, "context");
			props.setProperty(ServicePropertiesUtil.GRAVITY, "1.0");
			registerService(bc, editAction, CyAction.class, props);
		}
		{
			var removeAction = new RemoveSelectedDiscreteValuesAction(servicesUtil);
			var props = new Properties();
			props.setProperty(ServicePropertiesUtil.SERVICE_TYPE, "vizmapUI");
			props.setProperty(ServicePropertiesUtil.TITLE, RemoveSelectedDiscreteValuesAction.NAME);
			props.setProperty(ServicePropertiesUtil.MENU_ID, "context");
			props.setProperty(ServicePropertiesUtil.GRAVITY, "2.0");
			registerService(bc, removeAction, CyAction.class, props);
		}
		{
			var factory = new RemoveVisualMappingsTaskFactory(servicesUtil);
			var props = new Properties();
			props.setProperty(ServicePropertiesUtil.SERVICE_TYPE, "vizmapUI");
			props.setProperty(ServicePropertiesUtil.TITLE, "Remove Mappings from Selected Visual Properties");
			props.setProperty(ServicePropertiesUtil.MENU_ID, "context");
			props.setProperty(ServicePropertiesUtil.GRAVITY, "3.0");
			props.setProperty(ServicePropertiesUtil.INSERT_SEPARATOR_BEFORE, "true");
			registerAllServices(bc, factory, props);
		}
		
		// Discrete value generators:
		{
			var seriesGenerator = new NumberSeriesMappingGenerator<Number>(Number.class);
			var props = new Properties();
			props.setProperty(ServicePropertiesUtil.SERVICE_TYPE, "vizmapUI.contextMenu");
			props.setProperty(ServicePropertiesUtil.TITLE, "Number Series");
			props.setProperty(ServicePropertiesUtil.MENU_ID, "context");
			registerService(bc, seriesGenerator, DiscreteMappingGenerator.class, props);
		}
		{
			final RandomNumberMappingGenerator randomNumberGenerator = new RandomNumberMappingGenerator();
			var props = new Properties();
			props.setProperty(ServicePropertiesUtil.SERVICE_TYPE, "vizmapUI.contextMenu");
			props.setProperty(ServicePropertiesUtil.TITLE, "Random Numbers");
			props.setProperty(ServicePropertiesUtil.MENU_ID, "context");
			registerService(bc, randomNumberGenerator, DiscreteMappingGenerator.class, props);
		}
		{
			var fitLabelMappingGenerator = new FitLabelMappingGenerator<Double>(Double.class, servicesUtil);
			var props = new Properties();
			props.setProperty(ServicePropertiesUtil.SERVICE_TYPE, "vizmapUI.contextMenu");
			props.setProperty(ServicePropertiesUtil.TITLE, "Fit label width (only works with 'name' column to node size or width)");
			props.setProperty(ServicePropertiesUtil.MENU_ID, "context");
			registerService(bc, fitLabelMappingGenerator, DiscreteMappingGenerator.class, props);
		}
		
		// Network Panel Context Menu
		// -------------------------------------------------------------------------------------------------------------
		{
			var action = new ApplyVisualStyleAction(6.999f, servicesUtil);
			var props = new Properties();
			props.setProperty(TITLE, ApplyVisualStyleAction.NAME);
			props.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU, "true");
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
			registerService(bc, action, CyAction.class, props);
		}
		
		// Create the main GUI component
		// -------------------------------------------------------------------------------------------------------------
		var vizMapperMainPanel = new VizMapperMainPanel(servicesUtil);
		var vizMapperTableDialog = new VizMapperTableDialog(servicesUtil);
		
		// Start the PureMVC components
		// -------------------------------------------------------------------------------------------------------------
		var vizMapperProxy = new VizMapperProxy(servicesUtil);
		var propsProxy = new PropsProxy(servicesUtil);
		var vizMapPropertyBuilder = new VizMapPropertyBuilder(editorManager, mappingFunctionFactoryManager, servicesUtil);
		
		var vizMapperMediator = new VizMapperMediator(vizMapperMainPanel, servicesUtil, vizMapPropertyBuilder);
		var vizMapperMenuMediator = new VizMapperMenuMediator(vizMapperMainPanel, servicesUtil);
		var vizMapperTableMediator = new VizMapperTableMediator(vizMapperTableDialog, servicesUtil, vizMapPropertyBuilder);
		
		var startupCommand = new StartupCommand(vizMapperProxy,
												attributeSetProxy,
												mappingFactoryProxy,
												propsProxy,
												vizMapperMediator,
												vizMapperMenuMediator,
												vizMapperTableMediator,
												servicesUtil);
		
		// Table Panel Context Menu
		// -------------------------------------------------------------------------------------------------------------
				
		{
			var factory = new TableStyleDialogTaskFactory(vizMapperTableMediator, serviceRegistrar);
			var props = new Properties();
			props.setProperty(TITLE, "Open Table Style Dialog");
			registerService(bc, factory, TableColumnTaskFactory.class, props);
		}
		
		// Register Services
		// -------------------------------------------------------------------------------------------------------------
		
		registerAllServices(bc, vizMapperProxy);
		registerAllServices(bc, mappingFactoryProxy);
		registerAllServices(bc, propsProxy);
		
		registerAllServices(bc, vizMapperMediator);
		
		registerServiceListener(bc, vizMapperMediator::onCyActionRegistered, vizMapperMediator::onCyActionUnregistered, CyAction.class);
		registerServiceListener(bc, vizMapperMediator::onTaskFactoryRegistered, vizMapperMediator::onTaskFactoryUnregistered, TaskFactory.class);
		registerServiceListener(bc, vizMapperMediator::onMappingGeneratorRegistered, vizMapperMediator::onMappingGeneratorUnregistered, DiscreteMappingGenerator.class);
		
		registerServiceListener(bc, vizMapperMenuMediator::onRenderingEngineFactoryRegistered, vizMapperMenuMediator::onRenderingEngineFactoryUnregistered, RenderingEngineFactory.class);
		
		var vizMapEventHandlerManager = new VizMapEventHandlerManagerImpl(editorManager, attributeSetProxy,
				servicesUtil, vizMapPropertyBuilder, vizMapperMediator);
		registerServiceListener(bc, vizMapEventHandlerManager::registerPCL, vizMapEventHandlerManager::unregisterPCL, RenderingEngineFactory.class);
		
		// Startup the framework
		new ApplicationFacade(startupCommand).startup();
	}

	public void generateColorMappingGenerators(CyServiceRegistrar registrar, PaletteProviderManager paletteProviderMgr) {
		for (PaletteProvider provider : paletteProviderMgr.getPaletteProviders(BrewerType.QUALITATIVE, false)) {
			for (var paletteName : provider.listPaletteNames(BrewerType.QUALITATIVE, false)) {
				Palette palette = provider.getPalette(paletteName);
				registerNewGenerator(registrar, palette.toString(), palette);
			}
		}
	}

	private void registerNewGenerator(CyServiceRegistrar registrar, String name, Palette palette) {
		DiscreteMappingGenerator<Color> generator = new PaletteMappingWrapper(name, palette);
		var props = new Properties();
		props.setProperty(ServicePropertiesUtil.SERVICE_TYPE, "vizmapUI");
		props.setProperty(ServicePropertiesUtil.TITLE, name);
		props.setProperty(ServicePropertiesUtil.MENU_ID, "context");
		registrar.registerService(generator, DiscreteMappingGenerator.class, props);
	}
}
