package org.cytoscape.browser.internal;

import static org.cytoscape.browser.internal.view.AbstractTableBrowser.ICON_FONT_SIZE;
import static org.cytoscape.browser.internal.view.AbstractTableBrowser.ICON_HEIGHT;
import static org.cytoscape.browser.internal.view.AbstractTableBrowser.ICON_WIDTH;
import static org.cytoscape.util.swing.IconManager.ICON_COG;
import static org.cytoscape.util.swing.IconManager.ICON_TRASH_O;
import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_AFTER;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;
import static org.cytoscape.work.ServiceProperties.INSERT_TOOLBAR_SEPARATOR_BEFORE;
import static org.cytoscape.work.ServiceProperties.IN_EDGE_TABLE_TOOL_BAR;
import static org.cytoscape.work.ServiceProperties.IN_NETWORK_TABLE_TOOL_BAR;
import static org.cytoscape.work.ServiceProperties.IN_NODE_TABLE_TOOL_BAR;
import static org.cytoscape.work.ServiceProperties.IN_UNASSIGNED_TABLE_TOOL_BAR;
import static org.cytoscape.work.ServiceProperties.LARGE_ICON_ID;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.SMALL_ICON_ID;
import static org.cytoscape.work.ServiceProperties.TITLE;
import static org.cytoscape.work.ServiceProperties.TOOLTIP;
import static org.cytoscape.work.ServiceProperties.TOOL_BAR_GRAVITY;

import java.util.Arrays;
import java.util.Properties;

import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.events.SetCurrentTableListener;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.TableToolBarComponent;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedListener;
import org.cytoscape.browser.internal.action.CreateColumnAction;
import org.cytoscape.browser.internal.action.DeleteColumnsAction;
import org.cytoscape.browser.internal.action.ShowColumnsAction;
import org.cytoscape.browser.internal.action.TableOptionsAction;
import org.cytoscape.browser.internal.task.ClearAllErrorsTaskFactory;
import org.cytoscape.browser.internal.task.DeleteTableTaskFactoryImpl;
import org.cytoscape.browser.internal.task.HideColumnTaskFactory;
import org.cytoscape.browser.internal.task.SetColumnFormatTaskFactory;
import org.cytoscape.browser.internal.task.ToggleTextWrapTaskFactory;
import org.cytoscape.browser.internal.util.IconUtil;
import org.cytoscape.browser.internal.view.DefaultTableBrowser;
import org.cytoscape.browser.internal.view.GlobalTableBrowser;
import org.cytoscape.browser.internal.view.TableBrowserMediator;
import org.cytoscape.browser.internal.view.TableBrowserStyleMediator;
import org.cytoscape.browser.internal.view.ToolBarEnableUpdater;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.events.TableAboutToBeDeletedListener;
import org.cytoscape.model.events.TableAddedListener;
import org.cytoscape.model.events.TablePrivacyChangedListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.task.TableColumnTaskFactory;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.model.events.TableViewAddedListener;
import org.cytoscape.view.vizmap.events.VisualStyleChangedListener;
import org.cytoscape.view.vizmap.events.table.ColumnVisualStyleSetListener;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;

/*
 * #%L
 * Cytoscape Table Browser Impl (table-browser-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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
	
	private static final String TOOLBAR_FILTER =
			"(|(" + IN_NODE_TABLE_TOOL_BAR + "=true)"
			+ "(" + IN_EDGE_TABLE_TOOL_BAR + "=true)"
			+ "(" + IN_NETWORK_TABLE_TOOL_BAR + "=true)"
			+ "(" + IN_UNASSIGNED_TABLE_TOOL_BAR + "=true))";
	
	private static float SMALL_ICON_FONT_SIZE = 14.0f;
	private static int SMALL_ICON_SIZE = 16;
	
	@Override
	public void start(BundleContext bc) {
		var serviceRegistrar = getService(bc, CyServiceRegistrar.class);
		var iconManager = getService(bc, IconManager.class);
		
		var nodeTableBrowser = new DefaultTableBrowser("Node Table", CyNode.class, serviceRegistrar);
		var edgeTableBrowser = new DefaultTableBrowser("Edge Table", CyEdge.class, serviceRegistrar);
		var networkTableBrowser = new DefaultTableBrowser("Network Table", CyNetwork.class, serviceRegistrar);
		var globalTableBrowser = new GlobalTableBrowser("Unassigned Tables", serviceRegistrar);
		
		registerAllServices(bc, nodeTableBrowser);
		registerAllServices(bc, edgeTableBrowser);
		registerAllServices(bc, networkTableBrowser);

		registerService(bc, globalTableBrowser, SessionLoadedListener.class);
		registerService(bc, globalTableBrowser, SessionAboutToBeSavedListener.class);
		registerService(bc, globalTableBrowser, TableAboutToBeDeletedListener.class);
		registerService(bc, globalTableBrowser, TablePrivacyChangedListener.class);
		registerService(bc, globalTableBrowser, TableViewAddedListener.class);

		{
			var factory = new ClearAllErrorsTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(TITLE, "Clear All Errors");
			props.setProperty(MENU_GRAVITY, "100.1");
			props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
			props.setProperty(INSERT_SEPARATOR_AFTER, "true");
			registerService(bc, factory, TableColumnTaskFactory.class, props);
		}
		{
			var factory = new SetColumnFormatTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(TITLE, "Format Column...");
			props.setProperty(MENU_GRAVITY, "2.1");
			props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
			props.setProperty(INSERT_SEPARATOR_AFTER, "true");
			registerService(bc, factory, TableColumnTaskFactory.class, props);
		}
		
		var mediator = new TableBrowserMediator(nodeTableBrowser, edgeTableBrowser, networkTableBrowser, globalTableBrowser, serviceRegistrar);
		registerService(bc, mediator, SetCurrentNetworkListener.class);
		registerService(bc, mediator, SetCurrentTableListener.class);
		registerService(bc, mediator, TableAddedListener.class);
		registerService(bc, mediator, CytoPanelComponentSelectedListener.class);
		
		var toolBarEnableUpdater = new ToolBarEnableUpdater(
				Arrays.asList(
						nodeTableBrowser.getToolBar(),
						edgeTableBrowser.getToolBar(),
						networkTableBrowser.getToolBar(),
						globalTableBrowser.getToolBar()
				),
				serviceRegistrar
		);
		registerAllServices(bc, toolBarEnableUpdater);
		
		{
			var iconFont = iconManager.getIconFont(SMALL_ICON_FONT_SIZE);
			var icon = new TextIcon(IconManager.ICON_EYE_SLASH, iconFont, SMALL_ICON_SIZE, SMALL_ICON_SIZE);
			var iconId = "cy::Table::HIDE_COLUMN_SMALL";
			iconManager.addIcon(iconId, icon);
			
			var factory = new HideColumnTaskFactory(mediator);
			var props = new Properties();
			props.setProperty(TITLE, "Hide Column");
			props.setProperty(MENU_GRAVITY, "1.1");
			props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
			props.setProperty(SMALL_ICON_ID, iconId);
			registerService(bc, factory, TableColumnTaskFactory.class, props);
		}
		{
			var factory = new ToggleTextWrapTaskFactory(mediator);
			var props = new Properties();
			props.setProperty(TITLE, "Wrap Text");
			props.setProperty(MENU_GRAVITY, "2.2");
			props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
			props.setProperty(INSERT_SEPARATOR_AFTER, "true");
			registerService(bc, factory, TableColumnTaskFactory.class, props);
		}
		
		var styleMediator = new TableBrowserStyleMediator(serviceRegistrar);
		registerService(bc, styleMediator, VisualStyleChangedListener.class);
		registerService(bc, styleMediator, ColumnVisualStyleSetListener.class);
		
		registerServiceListener(bc, mediator::addAction, mediator::removeAction, CyAction.class);
		registerServiceListener(bc, mediator::addTaskFactory, mediator::removeTaskFactory, TaskFactory.class, TOOLBAR_FILTER);
		registerServiceListener(bc, mediator::addTableTaskFactory, mediator::removeTableTaskFactory, TableTaskFactory.class, TOOLBAR_FILTER);
		registerServiceListener(bc, mediator::addTableToolBarComponent, mediator::removeTableToolBarComponent, TableToolBarComponent.class);
		
		// Toolbar actions and task factories
		{
			var iconFont = iconManager.getIconFont(ICON_FONT_SIZE * 4 / 5);
			var icon = new TextIcon(ICON_COG, iconFont, ICON_WIDTH, ICON_HEIGHT);

			var action = new TableOptionsAction(icon, 0.001f, mediator);
			registerService(bc, action, CyAction.class);
		}
		{
			var iconFont = iconManager.getIconFont(IconUtil.CY_FONT_NAME, ICON_FONT_SIZE);
			var icon = new TextIcon(IconUtil.COLUMN_SHOW, iconFont, ICON_WIDTH, ICON_HEIGHT);

			var action = new ShowColumnsAction(icon, 0.002f, mediator, serviceRegistrar);
			registerService(bc, action, CyAction.class);
		}
		{
			var iconFont = iconManager.getIconFont(IconUtil.CY_FONT_NAME, ICON_FONT_SIZE);
			var icon = new TextIcon(IconUtil.COLUMN_ADD, iconFont, ICON_WIDTH, ICON_HEIGHT);
			
			var action = new CreateColumnAction(icon, 0.003f, mediator, serviceRegistrar);
			registerService(bc, action, CyAction.class);
		}
		{
			var iconFont = iconManager.getIconFont(IconUtil.CY_FONT_NAME, ICON_FONT_SIZE);
			var icon = new TextIcon(IconUtil.COLUMN_REMOVE, iconFont, ICON_WIDTH, ICON_HEIGHT);
			
			var action = new DeleteColumnsAction(icon, 0.004f, mediator);
			registerService(bc, action, CyAction.class);
		}
		{
			var iconFont = iconManager.getIconFont(ICON_FONT_SIZE);
			var icon = new TextIcon(ICON_TRASH_O, iconFont, ICON_WIDTH, ICON_HEIGHT);
			var iconId = "cy::Table::DELETE_TABLE";
			iconManager.addIcon(iconId, icon);
			
			var props = new Properties();
			props.setProperty(ENABLE_FOR, "table");
			props.setProperty(TOOLTIP, "Delete Table...");
			props.setProperty(LARGE_ICON_ID, iconId);
			props.setProperty(TOOL_BAR_GRAVITY, "" + Integer.MAX_VALUE);
			props.setProperty(IN_NODE_TABLE_TOOL_BAR, "true");
			props.setProperty(IN_EDGE_TABLE_TOOL_BAR, "true");
			props.setProperty(IN_NETWORK_TABLE_TOOL_BAR, "true");
			props.setProperty(IN_UNASSIGNED_TABLE_TOOL_BAR, "true");
			props.setProperty(INSERT_TOOLBAR_SEPARATOR_BEFORE, "true");
			
			var factory = new DeleteTableTaskFactoryImpl(serviceRegistrar);
			registerService(bc, factory, TableTaskFactory.class, props);
		}
	}
}
