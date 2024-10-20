package org.cytoscape.view.table.internal;

import static org.cytoscape.work.ServiceProperties.*;

import java.awt.Font;
import java.util.Properties;

import org.cytoscape.application.TableViewRenderer;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.TableCellTaskFactory;
import org.cytoscape.task.TableColumnTaskFactory;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.model.table.CyTableViewFactory;
import org.cytoscape.view.model.table.CyTableViewFactoryProvider;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.table.internal.equation.EquationEditorDialogFactory;
import org.cytoscape.view.table.internal.equation.EquationEditorTaskFactory;
import org.cytoscape.view.table.internal.impl.PopupMenuHelper;
import org.cytoscape.work.ServiceProperties;
import org.osgi.framework.BundleContext;

/*
 * #%L
 * Cytoscape Table Presentation Impl (table-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2010 - 2021 The Cytoscape Consortium
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

	public static final String FUNCTION_ICON_LARGE_ID = "cy::FN_BUILDER";
	public static final String FUNCTION_ICON_SMALL_ID = "cy::FN_BUILDER_SMALL";
	
	@Override
	public void start(BundleContext bc) {
		var registrar = getService(bc, CyServiceRegistrar.class);
		var iconManager = getService(bc, IconManager.class);
		
		var popupMenuHelper = new PopupMenuHelper(registrar);
		registerServiceListener(bc, popupMenuHelper::addTableColumnTaskFactory, popupMenuHelper::removeTableColumnTaskFactory, TableColumnTaskFactory.class);
		registerServiceListener(bc, popupMenuHelper::addTableCellTaskFactory, popupMenuHelper::removeTableCellTaskFactory, TableCellTaskFactory.class);
		
		var lexicon = new BrowserTableVisualLexicon();
		
		var tableViewFactoryFactory = getService(bc, CyTableViewFactoryProvider.class);
		var tableViewFactory = tableViewFactoryFactory.createTableViewFactory(lexicon, TableViewRendererImpl.ID);
		
		var renderer = new TableViewRendererImpl(registrar, tableViewFactory, lexicon, popupMenuHelper);
		registerService(bc, renderer, TableViewRenderer.class);
		registerService(bc, tableViewFactory, CyTableViewFactory.class); // register the default CyTableViewFactory
		
		{
			// Need to register the RenderingEngineFactory itself because the RenderingEngineManager is listening for this service.
			var factory = renderer.getRenderingEngineFactory(TableViewRenderer.DEFAULT_CONTEXT);
			var props = new Properties();
			props.setProperty(ServiceProperties.ID, TableViewRendererImpl.ID);
			registerService(bc, factory, RenderingEngineFactory.class, props);
		}
		
		// Equations
		{
			var factory = new EquationEditorDialogFactory(registrar);
			registerService(bc, factory, EquationEditorDialogFactory.class);
		}
		
		// Function builder toolbar button
		{
			Font iconFont = null;
			try {
				iconFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/fonts/jsMath-cmti10.ttf"));
			} catch (Exception e) {
				throw new RuntimeException("Error loading font", e);
			}
			
			var iconLarge = new TextIcon("f(x)", iconFont.deriveFont(18.0f), 32, 31);
			iconManager.addIcon(FUNCTION_ICON_LARGE_ID, iconLarge);
			
			var iconSmall = new TextIcon("f(x)", iconFont.deriveFont(10.0f), 16, 16);
			iconManager.addIcon(FUNCTION_ICON_SMALL_ID, iconSmall);
			
			var factory = new EquationEditorTaskFactory(registrar);
			var props = new Properties();
			props.setProperty("task", "equationEditor");
			props.setProperty(LARGE_ICON_ID, FUNCTION_ICON_LARGE_ID);
			props.setProperty(TOOLTIP, "Function Builder...");
			props.setProperty(IN_NODE_TABLE_TOOL_BAR, "true");
			props.setProperty(IN_EDGE_TABLE_TOOL_BAR, "true");
			props.setProperty(IN_NETWORK_TABLE_TOOL_BAR, "true");
			props.setProperty(IN_UNASSIGNED_TABLE_TOOL_BAR, "true");
			props.setProperty(TOOL_BAR_GRAVITY, "0.005");
			props.setProperty(INSERT_TOOLBAR_SEPARATOR_BEFORE, "true");
			registerService(bc, factory, TableTaskFactory.class, props);
		}
	}
}
