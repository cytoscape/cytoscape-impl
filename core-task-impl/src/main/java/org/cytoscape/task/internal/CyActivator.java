package org.cytoscape.task.internal;

import static org.cytoscape.application.swing.ActionEnableSupport.ENABLE_FOR_NETWORK;
import static org.cytoscape.application.swing.ActionEnableSupport.ENABLE_FOR_NETWORK_AND_VIEW;
import static org.cytoscape.application.swing.ActionEnableSupport.ENABLE_FOR_SELECTED_EDGES;
import static org.cytoscape.application.swing.ActionEnableSupport.ENABLE_FOR_SELECTED_NODES;
import static org.cytoscape.application.swing.ActionEnableSupport.ENABLE_FOR_SELECTED_NODES_OR_EDGES;
import static org.cytoscape.application.swing.ActionEnableSupport.ENABLE_FOR_SINGLE_NETWORK;
import static org.cytoscape.task.internal.utils.IconUtil.APPLY_LAYOUT;
import static org.cytoscape.task.internal.utils.IconUtil.C1;
import static org.cytoscape.task.internal.utils.IconUtil.COLORS_2A;
import static org.cytoscape.task.internal.utils.IconUtil.COLORS_2B;
import static org.cytoscape.task.internal.utils.IconUtil.COLORS_3;
import static org.cytoscape.task.internal.utils.IconUtil.FIRST_NEIGHBORS;
import static org.cytoscape.task.internal.utils.IconUtil.LAYERED_HELP;
import static org.cytoscape.task.internal.utils.IconUtil.LAYERED_HIDE_SELECTED;
import static org.cytoscape.task.internal.utils.IconUtil.LAYERED_IMPORT_NET;
import static org.cytoscape.task.internal.utils.IconUtil.LAYERED_OPEN_FILE;
import static org.cytoscape.task.internal.utils.IconUtil.LAYERED_SAVE;
import static org.cytoscape.task.internal.utils.IconUtil.LAYERED_SHOW_ALL;
import static org.cytoscape.task.internal.utils.IconUtil.LAYERED_ZOOM_FIT;
import static org.cytoscape.task.internal.utils.IconUtil.LAYERED_ZOOM_IN;
import static org.cytoscape.task.internal.utils.IconUtil.LAYERED_ZOOM_OUT;
import static org.cytoscape.task.internal.utils.IconUtil.LAYERED_ZOOM_SEL;
import static org.cytoscape.work.ServiceProperties.ACCELERATOR;
import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_EXAMPLE_JSON;
import static org.cytoscape.work.ServiceProperties.COMMAND_LONG_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.COMMAND_SUPPORTS_JSON;
import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.ID;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_AFTER;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;
import static org.cytoscape.work.ServiceProperties.INSERT_TOOLBAR_SEPARATOR_AFTER;
import static org.cytoscape.work.ServiceProperties.IN_CONTEXT_MENU;
import static org.cytoscape.work.ServiceProperties.IN_EDGE_TABLE_TOOL_BAR;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.IN_NETWORK_PANEL_CONTEXT_MENU;
import static org.cytoscape.work.ServiceProperties.IN_NETWORK_TABLE_TOOL_BAR;
import static org.cytoscape.work.ServiceProperties.IN_NODE_TABLE_TOOL_BAR;
import static org.cytoscape.work.ServiceProperties.IN_TOOL_BAR;
import static org.cytoscape.work.ServiceProperties.IN_UNASSIGNED_TABLE_TOOL_BAR;
import static org.cytoscape.work.ServiceProperties.LARGE_ICON_ID;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.NETWORK_GROUP_MENU;
import static org.cytoscape.work.ServiceProperties.NETWORK_SELECT_MENU;
import static org.cytoscape.work.ServiceProperties.NODE_ADD_MENU;
import static org.cytoscape.work.ServiceProperties.NODE_GROUP_MENU;
import static org.cytoscape.work.ServiceProperties.NODE_SELECT_MENU;
import static org.cytoscape.work.ServiceProperties.PREFERRED_ACTION;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.SMALL_ICON_ID;
import static org.cytoscape.work.ServiceProperties.TITLE;
import static org.cytoscape.work.ServiceProperties.TOOLTIP;
import static org.cytoscape.work.ServiceProperties.TOOLTIP_IMAGE;
import static org.cytoscape.work.ServiceProperties.TOOLTIP_LONG_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.TOOL_BAR_GRAVITY;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.stream.Collectors;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.io.write.CyNetworkViewWriterManager;
import org.cytoscape.io.write.CySessionWriterFactory;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkCollectionTaskFactory;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewCollectionTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.task.TableCellTaskFactory;
import org.cytoscape.task.TableColumnTaskFactory;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.task.create.CloneNetworkTaskFactory;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.task.create.NewEmptyNetworkViewFactory;
import org.cytoscape.task.create.NewNetworkSelectedNodesAndEdgesTaskFactory;
import org.cytoscape.task.create.NewNetworkSelectedNodesOnlyTaskFactory;
import org.cytoscape.task.create.NewSessionTaskFactory;
import org.cytoscape.task.destroy.DeleteColumnTaskFactory;
import org.cytoscape.task.destroy.DeleteSelectedNodesAndEdgesTaskFactory;
import org.cytoscape.task.destroy.DeleteTableTaskFactory;
import org.cytoscape.task.destroy.DestroyNetworkTaskFactory;
import org.cytoscape.task.destroy.DestroyNetworkViewTaskFactory;
import org.cytoscape.task.edit.CollapseGroupTaskFactory;
import org.cytoscape.task.edit.ConnectSelectedNodesTaskFactory;
import org.cytoscape.task.edit.EditNetworkTitleTaskFactory;
import org.cytoscape.task.edit.ExpandGroupTaskFactory;
import org.cytoscape.task.edit.GroupNodesTaskFactory;
import org.cytoscape.task.edit.MapGlobalToLocalTableTaskFactory;
import org.cytoscape.task.edit.MapTableToNetworkTablesTaskFactory;
import org.cytoscape.task.edit.MergeTablesTaskFactory;
import org.cytoscape.task.edit.RenameColumnTaskFactory;
import org.cytoscape.task.edit.UnGroupNodesTaskFactory;
import org.cytoscape.task.edit.UnGroupTaskFactory;
import org.cytoscape.task.hide.HideSelectedEdgesTaskFactory;
import org.cytoscape.task.hide.HideSelectedNodesTaskFactory;
import org.cytoscape.task.hide.HideSelectedTaskFactory;
import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.task.hide.HideUnselectedEdgesTaskFactory;
import org.cytoscape.task.hide.HideUnselectedNodesTaskFactory;
import org.cytoscape.task.hide.HideUnselectedTaskFactory;
import org.cytoscape.task.hide.UnHideAllEdgesTaskFactory;
import org.cytoscape.task.hide.UnHideAllNodesTaskFactory;
import org.cytoscape.task.hide.UnHideAllTaskFactory;
import org.cytoscape.task.hide.UnHideSelectedEdgesTaskFactory;
import org.cytoscape.task.hide.UnHideSelectedNodesTaskFactory;
import org.cytoscape.task.hide.UnHideTaskFactory;
import org.cytoscape.task.internal.edit.ConnectSelectedNodesTaskFactoryImpl;
import org.cytoscape.task.internal.export.graphics.ExportNetworkImageTaskFactoryImpl;
import org.cytoscape.task.internal.export.network.ExportNetworkTaskFactoryImpl;
import org.cytoscape.task.internal.export.network.ExportNetworkViewTaskFactoryImpl;
import org.cytoscape.task.internal.export.network.ExportSelectedNetworkTaskFactoryImpl;
import org.cytoscape.task.internal.export.network.GenerateNetworkViewsTask;
import org.cytoscape.task.internal.export.network.LoadMultipleNetworkFilesTaskFactoryImpl;
import org.cytoscape.task.internal.export.network.LoadNetworkFileTaskFactoryImpl;
import org.cytoscape.task.internal.export.network.LoadNetworkURLTaskFactoryImpl;
import org.cytoscape.task.internal.export.table.ExportNoGuiSelectedTableTaskFactoryImpl;
import org.cytoscape.task.internal.export.table.ExportSelectedTableTaskFactoryImpl;
import org.cytoscape.task.internal.export.table.ExportTableTaskFactoryImpl;
import org.cytoscape.task.internal.export.web.ExportAsWebArchiveTaskFactory;
import org.cytoscape.task.internal.filter.ApplyFilterTaskFactory;
import org.cytoscape.task.internal.filter.CreateFilterTaskFactory;
import org.cytoscape.task.internal.filter.DeleteFilterTaskFactory;
import org.cytoscape.task.internal.filter.GetFilterTaskFactory;
import org.cytoscape.task.internal.filter.ListFiltersTaskFactory;
import org.cytoscape.task.internal.filter.RenameFilterTaskFactory;
import org.cytoscape.task.internal.filter.SelectFilterTaskFactory;
import org.cytoscape.task.internal.group.AddToGroupTaskFactory;
import org.cytoscape.task.internal.group.GetGroupTask;
import org.cytoscape.task.internal.group.GetGroupTaskFactory;
import org.cytoscape.task.internal.group.GroupNodeContextTaskFactoryImpl;
import org.cytoscape.task.internal.group.GroupNodesTaskFactoryImpl;
import org.cytoscape.task.internal.group.ListGroupsTaskFactory;
import org.cytoscape.task.internal.group.RemoveFromGroupTaskFactory;
import org.cytoscape.task.internal.group.RenameGroupTaskFactory;
import org.cytoscape.task.internal.group.UnGroupNodesTaskFactoryImpl;
import org.cytoscape.task.internal.help.HelpTaskFactory;
import org.cytoscape.task.internal.hide.HideCommandTaskFactory;
import org.cytoscape.task.internal.hide.HideSelectedEdgesTaskFactoryImpl;
import org.cytoscape.task.internal.hide.HideSelectedNodesTaskFactoryImpl;
import org.cytoscape.task.internal.hide.HideSelectedTaskFactoryImpl;
import org.cytoscape.task.internal.hide.HideTaskFactoryImpl;
import org.cytoscape.task.internal.hide.HideUnselectedEdgesTaskFactoryImpl;
import org.cytoscape.task.internal.hide.HideUnselectedNodesTaskFactoryImpl;
import org.cytoscape.task.internal.hide.HideUnselectedTaskFactoryImpl;
import org.cytoscape.task.internal.hide.UnHideAllEdgesTaskFactoryImpl;
import org.cytoscape.task.internal.hide.UnHideAllNodesTaskFactoryImpl;
import org.cytoscape.task.internal.hide.UnHideAllTaskFactoryImpl;
import org.cytoscape.task.internal.hide.UnHideCommandTaskFactory;
import org.cytoscape.task.internal.hide.UnHideSelectedEdgesTaskFactoryImpl;
import org.cytoscape.task.internal.hide.UnHideSelectedNodesTaskFactoryImpl;
import org.cytoscape.task.internal.hide.UnHideTaskFactoryImpl;
import org.cytoscape.task.internal.layout.ApplyPreferredLayoutTaskFactoryImpl;
import org.cytoscape.task.internal.layout.GetPreferredLayoutTaskFactory;
import org.cytoscape.task.internal.layout.SetPreferredLayoutTaskFactory;
import org.cytoscape.task.internal.network.CloneNetworkTaskFactoryImpl;
import org.cytoscape.task.internal.network.DestroyNetworkTaskFactoryImpl;
import org.cytoscape.task.internal.network.NewEmptyNetworkTaskFactoryImpl;
import org.cytoscape.task.internal.network.NewNetworkCommandTaskFactory;
import org.cytoscape.task.internal.network.NewNetworkSelectedNodesEdgesTaskFactoryImpl;
import org.cytoscape.task.internal.network.NewNetworkSelectedNodesOnlyTaskFactoryImpl;
import org.cytoscape.task.internal.networkobjects.AddEdgeTaskFactory;
import org.cytoscape.task.internal.networkobjects.AddNodeTaskFactory;
import org.cytoscape.task.internal.networkobjects.AddTaskFactory;
import org.cytoscape.task.internal.networkobjects.DeleteSelectedNodesAndEdgesTaskFactoryImpl;
import org.cytoscape.task.internal.networkobjects.GetEdgeTaskFactory;
import org.cytoscape.task.internal.networkobjects.GetNetworkTaskFactory;
import org.cytoscape.task.internal.networkobjects.GetNodeTaskFactory;
import org.cytoscape.task.internal.networkobjects.GetPropertiesTaskFactory;
import org.cytoscape.task.internal.networkobjects.ListEdgesTaskFactory;
import org.cytoscape.task.internal.networkobjects.ListNetworksTaskFactory;
import org.cytoscape.task.internal.networkobjects.ListNodesTaskFactory;
import org.cytoscape.task.internal.networkobjects.ListPropertiesTaskFactory;
import org.cytoscape.task.internal.networkobjects.RenameEdgeTaskFactory;
import org.cytoscape.task.internal.networkobjects.RenameNodeTaskFactory;
import org.cytoscape.task.internal.networkobjects.SetCurrentNetworkTaskFactory;
import org.cytoscape.task.internal.networkobjects.SetPropertiesTaskFactory;
import org.cytoscape.task.internal.proxysettings.ProxySettingsTaskFactoryImpl;
import org.cytoscape.task.internal.select.DeselectAllEdgesTaskFactoryImpl;
import org.cytoscape.task.internal.select.DeselectAllNodesTaskFactoryImpl;
import org.cytoscape.task.internal.select.DeselectAllTaskFactoryImpl;
import org.cytoscape.task.internal.select.DeselectTaskFactory;
import org.cytoscape.task.internal.select.InvertSelectedEdgesTaskFactoryImpl;
import org.cytoscape.task.internal.select.InvertSelectedNodesTaskFactoryImpl;
import org.cytoscape.task.internal.select.SelectAdjacentEdgesTaskFactoryImpl;
import org.cytoscape.task.internal.select.SelectAllEdgesTaskFactoryImpl;
import org.cytoscape.task.internal.select.SelectAllNodesTaskFactoryImpl;
import org.cytoscape.task.internal.select.SelectAllTaskFactoryImpl;
import org.cytoscape.task.internal.select.SelectConnectedNodesTaskFactoryImpl;
import org.cytoscape.task.internal.select.SelectEdgesBetweenSelectedNodesTaskFactoryImpl;
import org.cytoscape.task.internal.select.SelectFirstNeighborsNodeViewTaskFactoryImpl;
import org.cytoscape.task.internal.select.SelectFirstNeighborsTaskFactoryImpl;
import org.cytoscape.task.internal.select.SelectFromFileListTaskFactoryImpl;
import org.cytoscape.task.internal.select.SelectTaskFactory;
import org.cytoscape.task.internal.session.NewSessionTaskFactoryImpl;
import org.cytoscape.task.internal.session.OpenSessionCommandTaskFactory;
import org.cytoscape.task.internal.session.OpenSessionTaskFactoryImpl;
import org.cytoscape.task.internal.session.SaveSessionAsTaskFactoryImpl;
import org.cytoscape.task.internal.session.SaveSessionTaskFactoryImpl;
import org.cytoscape.task.internal.table.AddRowTaskFactory;
import org.cytoscape.task.internal.table.CopyValueToColumnTaskFactoryImpl;
import org.cytoscape.task.internal.table.CreateColumnTaskFactory;
import org.cytoscape.task.internal.table.CreateNetworkAttributeTaskFactory;
import org.cytoscape.task.internal.table.CreateTableTaskFactory;
import org.cytoscape.task.internal.table.DeleteColumnCommandTaskFactory;
import org.cytoscape.task.internal.table.DeleteColumnTaskFactoryImpl;
import org.cytoscape.task.internal.table.DeleteRowTaskFactory;
import org.cytoscape.task.internal.table.DeleteTableTaskFactoryImpl;
import org.cytoscape.task.internal.table.DestroyTableTaskFactory;
import org.cytoscape.task.internal.table.GetColumnTaskFactory;
import org.cytoscape.task.internal.table.GetNetworkAttributeTaskFactory;
import org.cytoscape.task.internal.table.GetRowTaskFactory;
import org.cytoscape.task.internal.table.GetValueTaskFactory;
import org.cytoscape.task.internal.table.ListColumnsTaskFactory;
import org.cytoscape.task.internal.table.ListNetworkAttributesTaskFactory;
import org.cytoscape.task.internal.table.ListRowsTaskFactory;
import org.cytoscape.task.internal.table.ListTablesTaskFactory;
import org.cytoscape.task.internal.table.MapGlobalToLocalTableTaskFactoryImpl;
import org.cytoscape.task.internal.table.MapTableToNetworkTablesTaskFactoryImpl;
import org.cytoscape.task.internal.table.MergeTablesTaskFactoryImpl;
import org.cytoscape.task.internal.table.RenameColumnTaskFactoryImpl;
import org.cytoscape.task.internal.table.SetNetworkAttributeTaskFactory;
import org.cytoscape.task.internal.table.SetTableTitleTaskFactory;
import org.cytoscape.task.internal.table.SetValuesTaskFactory;
import org.cytoscape.task.internal.title.EditNetworkTitleTaskFactoryImpl;
import org.cytoscape.task.internal.utils.CoreImplDocumentationConstants;
import org.cytoscape.task.internal.utils.IconUtil;
import org.cytoscape.task.internal.view.CreateNetworkViewTaskFactoryImpl;
import org.cytoscape.task.internal.view.DestroyNetworkViewTaskFactoryImpl;
import org.cytoscape.task.internal.view.GetCurrentNetworkViewTaskFactory;
import org.cytoscape.task.internal.view.ListNetworkViewsTaskFactory;
import org.cytoscape.task.internal.view.SetCurrentNetworkViewTaskFactory;
import org.cytoscape.task.internal.view.UpdateNetworkViewTaskFactory;
import org.cytoscape.task.internal.vizmap.ApplyVisualStyleTaskFactoryimpl;
import org.cytoscape.task.internal.vizmap.ClearAllEdgeBendsFactory;
import org.cytoscape.task.internal.vizmap.ExportVizmapTaskFactoryImpl;
import org.cytoscape.task.internal.vizmap.LoadVizmapFileTaskFactoryImpl;
import org.cytoscape.task.internal.zoom.FitContentTaskFactory;
import org.cytoscape.task.internal.zoom.FitSelectedTaskFactory;
import org.cytoscape.task.internal.zoom.ZoomInTaskFactory;
import org.cytoscape.task.internal.zoom.ZoomOutTaskFactory;
import org.cytoscape.task.read.LoadMultipleNetworkFilesTaskFactory;
import org.cytoscape.task.read.LoadNetworkFileTaskFactory;
import org.cytoscape.task.read.LoadNetworkURLTaskFactory;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.task.select.DeselectAllEdgesTaskFactory;
import org.cytoscape.task.select.DeselectAllNodesTaskFactory;
import org.cytoscape.task.select.DeselectAllTaskFactory;
import org.cytoscape.task.select.InvertSelectedEdgesTaskFactory;
import org.cytoscape.task.select.InvertSelectedNodesTaskFactory;
import org.cytoscape.task.select.SelectAdjacentEdgesTaskFactory;
import org.cytoscape.task.select.SelectAllEdgesTaskFactory;
import org.cytoscape.task.select.SelectAllNodesTaskFactory;
import org.cytoscape.task.select.SelectAllTaskFactory;
import org.cytoscape.task.select.SelectConnectedNodesTaskFactory;
import org.cytoscape.task.select.SelectEdgesBetweenSelectedNodesTaskFactory;
import org.cytoscape.task.select.SelectFirstNeighborsNodeViewTaskFactory;
import org.cytoscape.task.select.SelectFirstNeighborsTaskFactory;
import org.cytoscape.task.select.SelectFromFileListTaskFactory;
import org.cytoscape.task.visualize.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.task.visualize.ApplyVisualStyleTaskFactory;
import org.cytoscape.task.write.ExportNetworkImageTaskFactory;
import org.cytoscape.task.write.ExportNetworkTaskFactory;
import org.cytoscape.task.write.ExportNetworkViewTaskFactory;
import org.cytoscape.task.write.ExportSelectedNetworkTaskFactory;
import org.cytoscape.task.write.ExportSelectedTableTaskFactory;
import org.cytoscape.task.write.ExportTableTaskFactory;
import org.cytoscape.task.write.ExportVizmapTaskFactory;
import org.cytoscape.task.write.SaveSessionAsTaskFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TunableSetter;
import org.osgi.framework.BundleContext;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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
	private static float LARGE_ICON_FONT_SIZE = 32.0f;
	private static int LARGE_ICON_SIZE = 32;

	private static float SMALL_ICON_FONT_SIZE = 14.0f;
	private static int SMALL_ICON_SIZE = 16;

	private Font iconFont;

	private CyServiceRegistrar serviceRegistrar;
	private CyNetworkViewWriterManager netViewWriterManager;
	private CyNetworkManager netManager;
	private CyApplicationManager applicationManager;
	private CyTableManager tableManager;
	private CyLayoutAlgorithmManager layoutAlgManager;
	private TunableSetter tunableSetter;
	private IconManager iconManager;

	@Override
	public void start(BundleContext bc) {
		serviceRegistrar = getService(bc, CyServiceRegistrar.class);
		netViewWriterManager = getService(bc, CyNetworkViewWriterManager.class);
		netManager = getService(bc, CyNetworkManager.class);
		applicationManager = getService(bc, CyApplicationManager.class);
		tableManager = getService(bc, CyTableManager.class);
		layoutAlgManager = getService(bc, CyLayoutAlgorithmManager.class);
		tunableSetter = getService(bc, TunableSetter.class);
		iconManager = getService(bc, IconManager.class);

		var groupManager = getService(bc, CyGroupManager.class);
		var groupFactory = getService(bc, CyGroupFactory.class);

		iconFont = iconManager.getIconFont("cytoscape-3", LARGE_ICON_FONT_SIZE);

		{
			var factory = new DynamicTaskFactoryProvisionerImpl(serviceRegistrar);
			registerAllServices(bc, factory);
		}

		createPreferencesTaskFactories(bc);
		createFilterTaskFactories(bc);
		createTableTaskFactories(bc);
		createNetworkTaskFactories(bc, groupManager, groupFactory);
		createViewTaskFactories(bc);
		createVizmapTaskFactories(bc);
		createSessionTaskFactories(bc);
		createGroupTaskFactories(bc, groupManager, groupFactory);
		createNodeEdgeTaskFactories(bc);
		createLayoutTaskFactories(bc);
		createHelpTaskFactories(bc);
	}

	private void createPreferencesTaskFactories(BundleContext bc) {
		{
			var factory = new ProxySettingsTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(PREFERRED_MENU, "Edit.Preferences");
			props.setProperty(MENU_GRAVITY, "3.0");
			props.setProperty(TITLE, "Proxy Settings...");
			registerService(bc, factory, TaskFactory.class, props);
		}
	}

	private void createFilterTaskFactories(BundleContext bc) {
		String createLongDescription;

		try {
			InputStream in = getClass().getResourceAsStream("create_filter_long_description.md");
			createLongDescription = new BufferedReader(new InputStreamReader(in)).lines().collect(Collectors.joining("\n"));
		} catch (Exception e) {
			createLongDescription = "Create a filter by suppling a name and a JSON filter expression.";
		}

		// export and import commands are in filter2-impl
		{
			var props = new Properties();
			props.setProperty(COMMAND, "create");
			props.setProperty(COMMAND_NAMESPACE, "filter");
			props.setProperty(COMMAND_DESCRIPTION, "Create a filter.");
			props.setProperty(COMMAND_LONG_DESCRIPTION, createLongDescription);
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{ }");
			registerService(bc, new CreateFilterTaskFactory(serviceRegistrar), TaskFactory.class, props);
		}
		{
			var props = new Properties();
			props.setProperty(COMMAND, "select");
			props.setProperty(COMMAND_NAMESPACE, "filter");
			props.setProperty(COMMAND_DESCRIPTION, "Select nodes and edges using a JSON filter expression.");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "See the documentation for 'filter create' for details on the accepted JSON format.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{ }");
			registerService(bc, new SelectFilterTaskFactory(serviceRegistrar), TaskFactory.class, props);
		}
		{
			var props = new Properties();
			props.setProperty(COMMAND, "apply");
			props.setProperty(COMMAND_NAMESPACE, "filter");
			props.setProperty(COMMAND_DESCRIPTION, "Select nodes and edges by running a filter.");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Run an existing filter by supplying the filter name.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{ }");
			registerService(bc, new ApplyFilterTaskFactory(serviceRegistrar), TaskFactory.class, props);
		}
		{
			var props = new Properties();
			props.setProperty(COMMAND, "delete");
			props.setProperty(COMMAND_NAMESPACE, "filter");
			props.setProperty(COMMAND_DESCRIPTION, "Delete a filter.");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Delete an existing filter by supplying the filter name.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{ }");
			registerService(bc, new DeleteFilterTaskFactory(serviceRegistrar), TaskFactory.class, props);
		}
		{
			var props = new Properties();
			props.setProperty(COMMAND, "rename");
			props.setProperty(COMMAND_NAMESPACE, "filter");
			props.setProperty(COMMAND_DESCRIPTION, "Rename a filter.");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Rename an existing filter by supplying the filter name and a new name.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{ }");
			registerService(bc, new RenameFilterTaskFactory(serviceRegistrar), TaskFactory.class, props);
		}
		{
			var props = new Properties();
			props.setProperty(COMMAND, "list");
			props.setProperty(COMMAND_NAMESPACE, "filter");
			props.setProperty(COMMAND_DESCRIPTION, "List filters.");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Returns a list of current filter names.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{ }");
			registerService(bc, new ListFiltersTaskFactory(serviceRegistrar), TaskFactory.class, props);
		}
		{
			var props = new Properties();
			props.setProperty(COMMAND, "get");
			props.setProperty(COMMAND_NAMESPACE, "filter");
			props.setProperty(COMMAND_DESCRIPTION, "Returns the JSON representation of a filter.");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Returns the JSON representation of a filter.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{ }");
			registerService(bc, new GetFilterTaskFactory(serviceRegistrar), TaskFactory.class, props);
		}
	}

	private void createLayoutTaskFactories(BundleContext bc) {
		{
			var factory = new ApplyPreferredLayoutTaskFactoryImpl(serviceRegistrar);

			var icon = new TextIcon(APPLY_LAYOUT, iconFont, C1, LARGE_ICON_SIZE, LARGE_ICON_SIZE);
			var iconId = "cy::APPLY_LAYOUT";
			iconManager.addIcon(iconId, icon);

			var props = new Properties();
			props.setProperty(PREFERRED_MENU, "Layout");
			props.setProperty(ACCELERATOR, "fn5");
			props.setProperty(LARGE_ICON_ID, iconId);
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
			props.setProperty(TITLE, "Apply Preferred Layout");
			props.setProperty(TOOL_BAR_GRAVITY, "7.0");
			props.setProperty(IN_TOOL_BAR, "true");
			props.setProperty(MENU_GRAVITY, "5.0");
			props.setProperty(TOOLTIP, "Apply Preferred Layout");
			props.setProperty(TOOLTIP_LONG_DESCRIPTION, "Applies the preferred layout to the selected views.");
			props.setProperty(TOOLTIP_IMAGE, getClass().getResource("/images/tooltips/apply-preferred-layout.gif").toString());
			registerService(bc, factory, NetworkViewCollectionTaskFactory.class, props);
			registerService(bc, factory, ApplyPreferredLayoutTaskFactory.class, props);

			// For commands
			props = new Properties();
			props.setProperty(COMMAND, "apply preferred");
			props.setProperty(COMMAND_NAMESPACE, "layout");
			props.setProperty(COMMAND_DESCRIPTION, "Execute the preferred layout on a network");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
					"Executes the current preferred layout. "
					+ "Default is ```grid```.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{ }");
			registerService(bc, factory, TaskFactory.class, props);
		}
		// ---------- COMMANDS ----------
		// NAMESPACE: layout
		{
			var factory = new GetPreferredLayoutTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "get preferred");
			props.setProperty(COMMAND_NAMESPACE, "layout");
			props.setProperty(COMMAND_DESCRIPTION, "Return the current preferred layout");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
					"Returns the name of the current preferred layout or empty string if not set. "
					+ "Default is ```grid```.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "\"grid\"");

			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new SetPreferredLayoutTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "set preferred");
			props.setProperty(COMMAND_NAMESPACE, "layout");
			props.setProperty(COMMAND_DESCRIPTION, "Set the preferred layout");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
					"Sets the preferred layout. Takes a specific name as defined in the API "
					+ "Default is ```grid```.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{ }");
			registerService(bc, factory, TaskFactory.class, props);
		}
	}

	private void createViewTaskFactories(BundleContext bc) {
		{
			var factory = new ExportNetworkViewTaskFactoryImpl(netViewWriterManager, applicationManager, tunableSetter);
			var props = new Properties();
			props.setProperty(ID, "exportNetworkViewTaskFactory");
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
			registerService(bc, factory, ExportNetworkViewTaskFactory.class, props);
		}
		{
			var factory = new CreateNetworkViewTaskFactoryImpl(netManager, layoutAlgManager, applicationManager,
					serviceRegistrar);
			// UI
			var props = new Properties();
			props.setProperty(ID, "createNetworkViewTaskFactory");
			// No ENABLE_FOR because that is handled by the isReady() methdod of the task factory.
			props.setProperty(PREFERRED_MENU, "Edit");
			props.setProperty(TITLE, "Create Views");
			props.setProperty(MENU_GRAVITY, "3.0");
			registerService(bc, factory, NetworkCollectionTaskFactory.class, props);
			registerService(bc, factory, CreateNetworkViewTaskFactory.class, props);

			// Commands
			props = new Properties();
			props.setProperty(ID, "createNetworkViewTaskFactory");
			props.setProperty(COMMAND, "create");
			props.setProperty(COMMAND_NAMESPACE, "view");
			props.setProperty(COMMAND_DESCRIPTION, "Create a new view for a network");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
					"Creates a new view for the passed network and returns the SUID of the new view and the original network. "
					+ "If no networks are specified, it creates a view for the current network, if there is one.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"network\":101,\"view\":400}");
			registerService(bc, factory, TaskFactory.class, props);

			registerServiceListener(bc, factory::addNetworkViewRenderer, factory::removeNetworkViewRenderer, NetworkViewRenderer.class);
		}
		{
			var factory = new DestroyNetworkViewTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(PREFERRED_MENU, "Edit");
			props.setProperty(TITLE, "Destroy Views");
			props.setProperty(MENU_GRAVITY, "3.1");
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
			props.setProperty(COMMAND, "destroy");
			props.setProperty(COMMAND_NAMESPACE, "view");
			props.setProperty(COMMAND_DESCRIPTION, "Destroy the selected network views");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
					"Destroys all selected network views and returns their SUIDs. If no views are selected, this command does nothing.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"views\":[ 343, 521, 770 ]}");
			registerService(bc, factory, NetworkViewCollectionTaskFactory.class, props);
			registerService(bc, factory, DestroyNetworkViewTaskFactory.class, props);
		}
		{
			var factory = new ExportNetworkImageTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(PREFERRED_MENU, "File.Export[24.8]");
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
			props.setProperty(MENU_GRAVITY, "3");
			props.setProperty(TITLE, "Network to Image...");
			props.setProperty(IN_CONTEXT_MENU, "false");
			props.setProperty(TOOLTIP, "Export Network Image to File");

			registerService(bc, factory, NetworkViewTaskFactory.class, props);
			registerService(bc, factory, ExportNetworkImageTaskFactory.class, props);

			// view export command
			props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
			props.setProperty(COMMAND, "export");
			props.setProperty(COMMAND_NAMESPACE, "view");
			props.setProperty(COMMAND_DESCRIPTION, "Export the current view to a graphics file");
			props.setProperty(COMMAND_LONG_DESCRIPTION, """
					*** This command is DEPRECATED. Use the 'view export png', 'view export jpg' and 'view export pdf' commands instead. *** 
					Exports the current view to a graphics file and returns the path to the saved file.
					PNG and JPEG formats have options for scaling, while other formats only have the option 'exportTextAsFont'.
				    To speed up image export for large networks use the option 'highDetail=false'.
					For the PDF format, exporting text as font does not work for two-byte characters such as Chinese or Japanese.
					To avoid corrupted texts in the exported PDF, please set false to 'exportTextAsFont'
					when exporting networks including those non-English characters. """);
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{ \"file\": \"/Users/johndoe/Documents/MyNetwork.pdf\" }");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			// New in 3.2.0: Export to HTML5 archive
			var factory = new ExportAsWebArchiveTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(PREFERRED_MENU, "File.Export[24.8]");
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
			props.setProperty(MENU_GRAVITY, "4");
			props.setProperty(TITLE, "Network to Web Page...");
			registerAllServices(bc, factory, props);
			registerServiceListener(bc, factory::registerFactory, factory::unregisterFactory, CySessionWriterFactory.class);
		}
		{
			var factory = new ZoomInTaskFactory(serviceRegistrar);

			var icon = new TextIcon(LAYERED_ZOOM_IN, iconFont, COLORS_3, LARGE_ICON_SIZE, LARGE_ICON_SIZE, 1);
			var iconId = "cy::LAYERED_ZOOM_IN";
			iconManager.addIcon(iconId, icon);

			var props = new Properties();
			props.setProperty(PREFERRED_MENU, "View");
			props.setProperty(TITLE, "Zoom In");
			props.setProperty(MENU_GRAVITY, "6.3");
			props.setProperty(ACCELERATOR, "cmd equals");
			props.setProperty(LARGE_ICON_ID, iconId);
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
			props.setProperty(TOOLTIP, "Zoom In");
			props.setProperty(TOOLTIP_LONG_DESCRIPTION, "Zooms in the current view.");
			props.setProperty(TOOLTIP_IMAGE, getClass().getResource("/images/tooltips/zoom-in.gif").toString());
			props.setProperty(TOOL_BAR_GRAVITY, "5.1");
			props.setProperty(IN_TOOL_BAR, "true");
			// props.setProperty(COMMAND, "zoom in");
			// props.setProperty(COMMAND_NAMESPACE, "view");
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
		}
		{
			var factory = new ZoomOutTaskFactory(serviceRegistrar);

			var icon = new TextIcon(LAYERED_ZOOM_OUT, iconFont, COLORS_3, LARGE_ICON_SIZE, LARGE_ICON_SIZE, 1);
			var iconId = "cy::LAYERED_ZOOM_OUT";
			iconManager.addIcon(iconId, icon);

			var props = new Properties();
			props.setProperty(PREFERRED_MENU, "View");
			props.setProperty(TITLE, "Zoom Out");
			props.setProperty(TOOLTIP, "Zoom Out");
			props.setProperty(TOOLTIP_LONG_DESCRIPTION, "Zooms out the current view.");
			props.setProperty(TOOLTIP_IMAGE, getClass().getResource("/images/tooltips/zoom-out.gif").toString());
			props.setProperty(MENU_GRAVITY, "6.4");
			props.setProperty(INSERT_SEPARATOR_AFTER, "true");
			props.setProperty(ACCELERATOR, "cmd minus");
			props.setProperty(LARGE_ICON_ID, iconId);
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
			props.setProperty(TOOL_BAR_GRAVITY, "5.2");
			props.setProperty(IN_TOOL_BAR, "true");
			// props.setProperty(COMMAND, "zoom out");
			// props.setProperty(COMMAND_NAMESPACE, "view");
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
		}
		{
			var factory = new FitSelectedTaskFactory(serviceRegistrar);

			var icon = new TextIcon(LAYERED_ZOOM_SEL, iconFont, COLORS_3, LARGE_ICON_SIZE, LARGE_ICON_SIZE, 1);
			var iconId = "cy::LAYERED_ZOOM_SELECTED";
			iconManager.addIcon(iconId, icon);

			var props = new Properties();
			props.setProperty(PREFERRED_MENU, "View");
			props.setProperty(TITLE, "Fit Selected");
			props.setProperty(TOOLTIP, "Fit Selected");
			props.setProperty(TOOLTIP_LONG_DESCRIPTION, "Changes the current view's zoom and viewport so the selected nodes and edges fit into the displayed view area.");
			props.setProperty(TOOLTIP_IMAGE, getClass().getResource("/images/tooltips/fit-selected.gif").toString());
			props.setProperty(MENU_GRAVITY, "6.2");
			props.setProperty(ACCELERATOR, "cmd 9");
			props.setProperty(LARGE_ICON_ID, iconId);
			props.setProperty(ENABLE_FOR, ENABLE_FOR_SELECTED_NODES_OR_EDGES);
			props.setProperty(TOOL_BAR_GRAVITY, "5.4");
			props.setProperty(IN_TOOL_BAR, "true");
			registerService(bc, factory, NetworkViewTaskFactory.class, props);

			props = new Properties();
			props.setProperty(COMMAND, "fit selected");
			props.setProperty(COMMAND_NAMESPACE, "view");
			props.setProperty(COMMAND_DESCRIPTION, "Fit the selected nodes and edges into the view");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Changes the current view's zoom and viewport so the selected nodes and edges fit into the view area.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{ }");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new FitContentTaskFactory(serviceRegistrar);

			var icon = new TextIcon(LAYERED_ZOOM_FIT, iconFont, COLORS_3, LARGE_ICON_SIZE, LARGE_ICON_SIZE, 1);
			var iconId = "cy::LAYERED_ZOOM_FIT";
			iconManager.addIcon(iconId, icon);

			var props = new Properties();
			props.setProperty(PREFERRED_MENU, "View");
			props.setProperty(TITLE, "Fit Content");
			props.setProperty(TOOLTIP, "Fit Content");
			props.setProperty(TOOLTIP_LONG_DESCRIPTION, "Zooms out the current view in order to display all of its elements.");
			props.setProperty(TOOLTIP_IMAGE, getClass().getResource("/images/tooltips/fit-content.gif").toString());
			props.setProperty(MENU_GRAVITY, "6.1");
			props.setProperty(ACCELERATOR, "cmd 0");
			props.setProperty(LARGE_ICON_ID, iconId);
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
			props.setProperty(TOOL_BAR_GRAVITY, "5.3");
			props.setProperty(IN_TOOL_BAR, "true");
			registerService(bc, factory, NetworkViewTaskFactory.class, props);

			props = new Properties();
			props.setProperty(COMMAND, "fit content");
			props.setProperty(COMMAND_NAMESPACE, "view");
			props.setProperty(COMMAND_DESCRIPTION, "Fit all of the nodes and edges into the view");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Zooms out the current view in order to display all of its elements.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{ }");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new GetCurrentNetworkViewTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "get current");
			props.setProperty(COMMAND_NAMESPACE, "view");
			props.setProperty(COMMAND_DESCRIPTION, "Get the current view");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Returns the current view or null if there is none.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"view\": 136}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new ListNetworkViewsTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "list");
			props.setProperty(COMMAND_NAMESPACE, "view");
			props.setProperty(COMMAND_DESCRIPTION, "List views");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
					"Returns a list with the passed network's views or an empty list if there are no views. "
					+ "If a network is not specified, it assumes the current network.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"views\":[ 90, 136 ]}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new SetCurrentNetworkViewTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "set current");
			props.setProperty(COMMAND_NAMESPACE, "view");
			props.setProperty(COMMAND_DESCRIPTION, "Set the current view");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Sets the current view, which can also be null.  Note "+
					"that this command takes both ```view``` and ```network``` "+
					"as arguments.  If both are provided, the ```view``` "+
					"argument takes precedence.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{ }");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new UpdateNetworkViewTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "update");
			props.setProperty(COMMAND_NAMESPACE, "view");
			props.setProperty(COMMAND_DESCRIPTION, "Update (repaint) a view");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Updates and repaints all views of the specified network.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{ }");
			registerService(bc, factory, TaskFactory.class, props);
		}
	}

	private void createNodeEdgeTaskFactories(BundleContext bc) {
		// SELECTION
		{
			var factory = new DeleteSelectedNodesAndEdgesTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(PREFERRED_MENU, "Edit");
			props.setProperty(ENABLE_FOR, ENABLE_FOR_SELECTED_NODES_OR_EDGES);
			props.setProperty(TITLE, "Remove Selected Nodes and Edges");
			props.setProperty(MENU_GRAVITY, "5.0");
			props.setProperty(ACCELERATOR, "DELETE");
			props.setProperty(COMMAND, "delete");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND_DESCRIPTION, "Delete nodes or edges from a network");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
					"Deletes nodes and edges provided by the arguments, or if no "+
					"nodes or edges are provides, the selected nodes and edges.  "+
					"When deleting nodes, adjacent edges are also deleted.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"nodes\":[101,102,103], \"edges\":[201,202]}");
			registerService(bc, factory, NetworkTaskFactory.class, props);
			registerService(bc, factory, DeleteSelectedNodesAndEdgesTaskFactory.class, props);
		}
		{
			var factory = new SelectAllEdgesTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(PREFERRED_MENU, "Select.Edges[3]");
			props.setProperty(ACCELERATOR, "alt e");
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK);
			props.setProperty(TITLE, "Select All Edges");
			props.setProperty(MENU_GRAVITY, "4");
			registerService(bc, factory, NetworkTaskFactory.class, props);
			registerService(bc, factory, SelectAllEdgesTaskFactory.class, props);
		}
		{
			var factory = new SelectAllNodesTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK);
			props.setProperty(PREFERRED_MENU, "Select.Nodes[2]");
			props.setProperty(MENU_GRAVITY, "4");
			props.setProperty(ACCELERATOR, "alt n");
			props.setProperty(TITLE, "Select All Nodes");
			registerService(bc, factory, NetworkTaskFactory.class, props);
			registerService(bc, factory, SelectAllNodesTaskFactory.class, props);
		}
		{
			var factory = new SelectAdjacentEdgesTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK);
			props.setProperty(PREFERRED_MENU, "Select.Edges[3]");
			props.setProperty(MENU_GRAVITY, "6");
			props.setProperty(ACCELERATOR, "cmd alt e");
			props.setProperty(TITLE, "Select Adjacent Edges");
			// props.setProperty(COMMAND, "select adjacent");
			// props.setProperty(COMMAND_NAMESPACE, "edge");
			registerService(bc, factory, NetworkTaskFactory.class, props);
			registerService(bc,factory,SelectAdjacentEdgesTaskFactory.class, props);
		}
		{
			var factory = new SelectConnectedNodesTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK);
			props.setProperty(PREFERRED_MENU, "Select.Nodes[2]");
			props.setProperty(MENU_GRAVITY, "7");
			props.setProperty(ACCELERATOR, "cmd alt n");
			props.setProperty(TITLE, "Nodes Connected by Selected Edges");
			// props.setProperty(COMMAND, "select by connected edges");
			// props.setProperty(COMMAND_NAMESPACE, "node");
			registerService(bc, factory, NetworkTaskFactory.class, props);
			registerService(bc, factory, SelectConnectedNodesTaskFactory.class, props);
		}
		{
			var factory = new SelectEdgesBetweenSelectedNodesTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK);
			props.setProperty(PREFERRED_MENU, "Select.Edges[3]");
			props.setProperty(MENU_GRAVITY, "5");
			props.setProperty(ACCELERATOR, "cmd alt b");
			props.setProperty(TITLE, "Edges Between Selected Nodes");
			// props.setProperty(COMMAND, "select by connected edges");
			// props.setProperty(COMMAND_NAMESPACE, "node");
			registerService(bc, factory, NetworkTaskFactory.class, props);
			registerService(bc, factory, SelectEdgesBetweenSelectedNodesTaskFactory.class, props);
		}
		{
			var factory = new SelectFirstNeighborsTaskFactoryImpl(CyEdge.Type.ANY, serviceRegistrar);

			var icon = new TextIcon(FIRST_NEIGHBORS, iconFont, C1, LARGE_ICON_SIZE, LARGE_ICON_SIZE);
			var iconId = "cy::FIRST_NEIGHBORS";
			iconManager.addIcon(iconId, icon);

			var props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_SELECTED_NODES_OR_EDGES);
			props.setProperty(PREFERRED_MENU, "Select.Nodes[2].First Neighbors of Selected Nodes[0.5]");
			props.setProperty(MENU_GRAVITY, "6");
			props.setProperty(TOOL_BAR_GRAVITY, "9.15");
			props.setProperty(ACCELERATOR, "cmd 6");
			props.setProperty(TITLE, "Undirected");
			props.setProperty(LARGE_ICON_ID, iconId);
			props.setProperty(IN_TOOL_BAR, "true");
			props.setProperty(TOOLTIP, "First Neighbors of Selected Nodes (Undirected)");
			props.setProperty(TOOLTIP_LONG_DESCRIPTION, "Selects the first neighbors of the current network's selected nodes (undirected).");
			props.setProperty(TOOLTIP_IMAGE, getClass().getResource("/images/tooltips/first-neighbors.gif").toString());
			// props.setProperty(C	OMMAND, "select first neighbors undirected");
			// props.setProperty(COMMAND_NAMESPACE, "node");
			registerService(bc, factory, NetworkTaskFactory.class, props);
			registerService(bc, factory, SelectFirstNeighborsTaskFactory.class, props);
		}
		{
			// IN Edge
			var factory = new SelectFirstNeighborsTaskFactoryImpl(CyEdge.Type.INCOMING, serviceRegistrar);
			var props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK);
			props.setProperty(PREFERRED_MENU, "Select.Nodes[2].First Neighbors of Selected Nodes[0.5]");
//			props.setProperty(PREFERRED_MENU, "Select");
			props.setProperty(MENU_GRAVITY, "0.11");
//			props.setProperty(TITLE, "FirstIncomingNeighbors");
			props.setProperty(TITLE, "Directed: Incoming");
			props.setProperty(TOOLTIP, "First Neighbors of Selected Nodes (Directed: Incoming)");
			// props.setProperty(COMMAND, "select first neighbors incoming");
			// props.setProperty(COMMAND_NAMESPACE, "node");
			registerService(bc, factory, NetworkTaskFactory.class, props);
			registerService(bc, factory, SelectFirstNeighborsTaskFactory.class, props);
		}
		{
			// OUT Edge
			var factory = new SelectFirstNeighborsTaskFactoryImpl(CyEdge.Type.OUTGOING, serviceRegistrar);
			var props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK);
			props.setProperty(PREFERRED_MENU, "Select.Nodes[2].First Neighbors of Selected Nodes[0.5]");
//			props.setProperty(PREFERRED_MENU, "Select");
			props.setProperty(MENU_GRAVITY, "6.2");
			props.setProperty(TITLE, "Directed: Outgoing");
			props.setProperty(TOOLTIP, "First Neighbors of Selected Nodes (Directed: Outgoing)");
			// props.setProperty(COMMAND, "select first neighbors outgoing");
			// props.setProperty(COMMAND_NAMESPACE, "node");
			registerService(bc, factory, NetworkTaskFactory.class, props);
			registerService(bc, factory, SelectFirstNeighborsTaskFactory.class, props);
		}
		{
			var factory = new DeselectAllEdgesTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK);
			props.setProperty(PREFERRED_MENU, "Select.Edges[3]");
			props.setProperty(MENU_GRAVITY, "5");
			props.setProperty(ACCELERATOR, "alt shift e");
			props.setProperty(TITLE, "Deselect All Edges");
			registerService(bc, factory, NetworkTaskFactory.class, props);
			registerService(bc, factory, DeselectAllEdgesTaskFactory.class, props);
		}
		{
			var factory = new DeselectAllNodesTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK);
			props.setProperty(PREFERRED_MENU, "Select.Nodes[2]");
			props.setProperty(MENU_GRAVITY, "5.0");
			props.setProperty(ACCELERATOR, "alt shift n");
			props.setProperty(TITLE, "Deselect All Nodes");
			registerService(bc, factory, NetworkTaskFactory.class, props);
			registerService(bc, factory, DeselectAllNodesTaskFactory.class, props);
		}
		{
			var factory = new InvertSelectedEdgesTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK);
			props.setProperty(PREFERRED_MENU, "Select.Edges[3]");
			props.setProperty(MENU_GRAVITY, "1.0");
			props.setProperty(ACCELERATOR, "alt i");
			props.setProperty(TITLE, "Invert Edge Selection");
			registerService(bc, factory, NetworkTaskFactory.class, props);
			registerService(bc, factory, InvertSelectedEdgesTaskFactory.class, props);
		}
		{
			var factory = new InvertSelectedNodesTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_SELECTED_NODES);
			props.setProperty(PREFERRED_MENU, "Select.Nodes[2]");
			props.setProperty(MENU_GRAVITY, "1.0");
			props.setProperty(TOOL_BAR_GRAVITY, "9.2");
			props.setProperty(ACCELERATOR, "cmd i");
			props.setProperty(TITLE, "Invert Node Selection");
			props.setProperty(IN_TOOL_BAR, "false");
			props.setProperty(TOOLTIP, "Invert Node Selection");
			registerService(bc, factory, NetworkTaskFactory.class, props);
			registerService(bc, factory, InvertSelectedNodesTaskFactory.class, props);
		}
		{
			var factory = new SelectFromFileListTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK);
			props.setProperty(PREFERRED_MENU, "Select.Nodes[2]");
			props.setProperty(MENU_GRAVITY, "8");
			props.setProperty(ACCELERATOR, "cmd alt i");			// this was same as Invert command
			props.setProperty(TITLE, "From ID List File...");
			props.setProperty(COMMAND_NAMESPACE, "node");
			props.setProperty(COMMAND, "select from file");
			props.setProperty(COMMAND_DESCRIPTION, "Select nodes from a file");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Selects nodes in the current network based on node names provided by a file.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "true");
			registerService(bc, factory, NetworkTaskFactory.class, props);
			registerService(bc, factory, SelectFromFileListTaskFactory.class, props);
		}
		{
			var factory = new SelectFirstNeighborsNodeViewTaskFactoryImpl(CyEdge.Type.ANY, serviceRegistrar);
			var props = new Properties();
			props.setProperty(PREFERRED_MENU, NODE_SELECT_MENU);
			props.setProperty(MENU_GRAVITY, "1.0");
			props.setProperty(TITLE, "Select First Neighbors (Undirected)");
			registerService(bc, factory, NodeViewTaskFactory.class, props);
			registerService(bc, factory, SelectFirstNeighborsNodeViewTaskFactory.class, props);
		}
		{
			var factory = new SelectAllTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(PREFERRED_MENU, "Select");
			props.setProperty(ACCELERATOR, "cmd a");
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK);
			props.setProperty(TITLE, "Select All");
			props.setProperty(TOOLTIP, "Select all nodes, edges and annotations");
			props.setProperty(MENU_GRAVITY, "5.2");
			props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
			props.setProperty(PREFERRED_ACTION, "NEW");
			registerService(bc, factory, NetworkTaskFactory.class, props);
			registerService(bc, factory, SelectAllTaskFactory.class, props);

			props = new Properties();
			props.setProperty(PREFERRED_MENU, NETWORK_SELECT_MENU);
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
			props.setProperty(TITLE, "All Nodes and Edges");
			props.setProperty(MENU_GRAVITY, "1.1");
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(IN_MENU_BAR, "false");
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
		}
		{
			var factory = new DeselectAllTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK);
			props.setProperty(PREFERRED_MENU, "Select");
			props.setProperty(MENU_GRAVITY, "5.2");
			props.setProperty(ACCELERATOR, "cmd shift a");
			props.setProperty(TITLE, "Deselect All");
			props.setProperty(TOOLTIP, "Deselect all nodes, edges and annotations");
			// props.setProperty(COMMAND, "deselect all");
			// props.setProperty(COMMAND_NAMESPACE, "network");
			registerService(bc, factory, NetworkTaskFactory.class, props);
			registerService(bc, factory, DeselectAllTaskFactory.class, props);
		}
		// SHOW / HIDE
		{
			var factory = new UnHideAllTaskFactoryImpl(serviceRegistrar);

			var icon = new TextIcon(LAYERED_SHOW_ALL, iconFont, COLORS_2A, LARGE_ICON_SIZE, LARGE_ICON_SIZE, 1);
			var iconId = "cy::SHOW_ALL";
			iconManager.addIcon(iconId, icon);

			var props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
			props.setProperty(PREFERRED_MENU, "Select");
			props.setProperty(MENU_GRAVITY, "4.10");
			props.setProperty(TOOL_BAR_GRAVITY, "9.6");
			props.setProperty(TITLE, factory.getDescription());
			props.setProperty(LARGE_ICON_ID, iconId);
			props.setProperty(IN_TOOL_BAR, "true");
			props.setProperty(TOOLTIP, factory.getDescription());
			props.setProperty(TOOLTIP_LONG_DESCRIPTION, "Unhides the current view's hidden nodes and edges.");
			props.setProperty(TOOLTIP_IMAGE, getClass().getResource("/images/tooltips/show-hide.gif").toString());
			props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
			registerService(bc, factory, UnHideAllTaskFactory.class, props);
		}
		{
			var factory = new HideSelectedTaskFactoryImpl(serviceRegistrar);

			var icon = new TextIcon(LAYERED_HIDE_SELECTED, iconFont, COLORS_3, LARGE_ICON_SIZE, LARGE_ICON_SIZE, 1);
			var iconId = "cy::HIDE_SELECTED";
			iconManager.addIcon(iconId, icon);

			var props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_SELECTED_NODES_OR_EDGES);
			props.setProperty(PREFERRED_MENU, "Select");
			props.setProperty(MENU_GRAVITY, "4.11");
			props.setProperty(TOOL_BAR_GRAVITY, "9.5");
			props.setProperty(TITLE, factory.getDescription());
			props.setProperty(LARGE_ICON_ID, iconId);
			props.setProperty(IN_TOOL_BAR, "true");
			props.setProperty(TOOLTIP, factory.getDescription());
			props.setProperty(TOOLTIP_LONG_DESCRIPTION, "Hides the current view's selected nodes and edges.");
			props.setProperty(TOOLTIP_IMAGE, getClass().getResource("/images/tooltips/show-hide.gif").toString());
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
			registerService(bc, factory, HideSelectedTaskFactory.class, props);
		}
		{
			var factory = new HideUnselectedTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(PREFERRED_MENU, "Select");
			props.setProperty(MENU_GRAVITY, "4.2");
			props.setProperty(TITLE, factory.getDescription());
			props.setProperty(TOOLTIP, factory.getDescription());
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
			registerService(bc, factory, HideUnselectedTaskFactory.class, props);
		}
		{
			var factory = new HideSelectedNodesTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_SELECTED_NODES);
			props.setProperty(PREFERRED_MENU, "Select.Nodes[2]");
			props.setProperty(MENU_GRAVITY, "2");
			props.setProperty(TITLE, factory.getDescription());
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
			registerService(bc, factory, HideSelectedNodesTaskFactory.class, props);
		}
		{
			var factory = new HideUnselectedNodesTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(PREFERRED_MENU, "Select.Nodes[2]");
			props.setProperty(MENU_GRAVITY, "2.1");
			props.setProperty(TITLE, factory.getDescription());
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
			registerService(bc, factory, HideUnselectedNodesTaskFactory.class, props);
		}
		{
			var factory = new HideSelectedEdgesTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_SELECTED_EDGES);
			props.setProperty(PREFERRED_MENU, "Select.Edges[3]");
			props.setProperty(MENU_GRAVITY, "2");
			props.setProperty(TITLE, factory.getDescription());
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
			registerService(bc, factory, HideSelectedEdgesTaskFactory.class, props);
		}
		{
			var factory = new HideUnselectedEdgesTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(PREFERRED_MENU, "Select.Edges[3]");
			props.setProperty(MENU_GRAVITY, "2.1");
			props.setProperty(TITLE, factory.getDescription());
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
			registerService(bc, factory, HideUnselectedEdgesTaskFactory.class, props);
		}
		{
			var factory = new UnHideAllNodesTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
			props.setProperty(PREFERRED_MENU, "Select.Nodes[2]");
			props.setProperty(MENU_GRAVITY, "3.0");
			props.setProperty(TITLE, factory.getDescription());
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
			registerService(bc, factory, UnHideAllNodesTaskFactory.class, props);
		}
		{
			var factory = new UnHideAllEdgesTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
			props.setProperty(PREFERRED_MENU, "Select.Edges[3]");
			props.setProperty(MENU_GRAVITY, "3");
			props.setProperty(TITLE, factory.getDescription());
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
			registerService(bc, factory, UnHideAllEdgesTaskFactory.class, props);
		}
		{
			var factory = new UnHideSelectedNodesTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
			props.setProperty(PREFERRED_MENU, "Select.Nodes[2]");
			props.setProperty(MENU_GRAVITY, "3.0");
			props.setProperty(TITLE, factory.getDescription());
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
			registerService(bc, factory, UnHideSelectedNodesTaskFactory.class, props);
		}
		{
			var factory = new UnHideSelectedEdgesTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
			props.setProperty(PREFERRED_MENU, "Select.Edges[3]");
			props.setProperty(MENU_GRAVITY, "3");
			props.setProperty(TITLE, factory.getDescription());
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
			registerService(bc, factory, UnHideSelectedEdgesTaskFactory.class, props);
		}
		{
			var factory = new HideTaskFactoryImpl(serviceRegistrar);
			registerService(bc, factory, HideTaskFactory.class);
		}
		{
			var factory = new UnHideTaskFactoryImpl(serviceRegistrar);
			registerService(bc, factory, UnHideTaskFactory.class);
		}
		// ---------- COMMANDS ----------
		// NAMESPACE: network
		{
			var factory = new AddTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "add");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND_DESCRIPTION,
					"Add nodes and edges to a network (they must be in the current collection)");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
					"Adds nodes and edges to an existing network.  The nodes and edges to be added "+
					"must already exist in the network collection.  This command is most often used "+
					"to populate a subnetwork with selected nodes and edges from a parent network.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"nodes\":[101,102,103],\"edges\":[201,202,203]}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new AddEdgeTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "add edge");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND_DESCRIPTION, "Add an edge between two nodes");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
				"Add a new edge between two existing nodes in a network.  The names of the "+
				"nodes must be specified and much match the value in the 'name' column "+
				"for each node");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"edge\":101}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new AddNodeTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "add node");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND_DESCRIPTION, "Add a new node to a network");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
				"Add a new node to an existing network.  The name of the "+
				"node must be provided.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"node\":101}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new SelectTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "select");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND_DESCRIPTION, "Select nodes or edges in a network");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
				"Select nodes and/or edges in a network.  This command provides options to invert the selection, "+
				"add first neighbors, add adjacent edges of selected nodes, and add adjacent nodes of selected edges");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"nodes\": [101,122,495], \"edges\": [201,202,203]}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new DeselectTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "deselect");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND_DESCRIPTION, "Deselect nodes or edges in a network");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
				"Deselect nodes and/or edges in a network.  A list of nodes and/or edges may be provided and "+
				"those nodes and edges will be deselected.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"nodes\": [101,122,495], \"edges\": [201,202,203]}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new HideCommandTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "hide");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND_DESCRIPTION, "Hide nodes or edges in a network");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
				"Hide nodes and/or edges in a network.  A list of nodes and/or edges may be provided and "+
				"those nodes and edges will be hidden in the view associated with the provided network."+
				"Note that the network '''must''' have a view.  The SUIDs of the hidden nodes and/or edges "+
				"are returned.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"nodes\": [101,122,495], \"edges\": [201,202,203]}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new UnHideCommandTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "show");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND_DESCRIPTION, "Show hidden nodes and edges");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
				"Show nodes and/or edges in a network.  A list of nodes and/or edges may be provided and "+
				"those nodes and edges will be unhidden in the view associated with the provided network."+
				"Note that the network '''must''' have a view.  The SUIDs of the unhidden nodes and/or edges "+
				"are returned.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"nodes\": [101,122,495], \"edges\": [201,202,203]}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		// NAMESPACE: node
		{
			var factory = new CreateNetworkAttributeTaskFactory(CyNode.class, serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "create attribute");
			props.setProperty(COMMAND_NAMESPACE, "node");
			props.setProperty(COMMAND_DESCRIPTION, "Create a new column for nodes");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Creates a new node column.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, CreateNetworkAttributeTaskFactory.COMMAND_EXAMPLE_JSON);

			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new GetNodeTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "get");
			props.setProperty(COMMAND_NAMESPACE, "node");
			props.setProperty(COMMAND_DESCRIPTION, "Get a node from a network");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
					"Returns the SUID of a node that matches the passed parameters. If multiple nodes are found, only one will be returned, and a warning will be printed.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"node\":101}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new GetNetworkAttributeTaskFactory(CyNode.class, serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "get attribute");
			props.setProperty(COMMAND_NAMESPACE, "node");
			props.setProperty(COMMAND_DESCRIPTION, "Get values from the node table");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
					"Returns the attributes for the nodes passed as parameters.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, GetNetworkAttributeTaskFactory.COMMAND_EXAMPLE_JSON);
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new GetPropertiesTaskFactory(CyNode.class, serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "get properties");
			props.setProperty(COMMAND_NAMESPACE, "node");
			props.setProperty(COMMAND_DESCRIPTION, "Get visual properties for a node");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
					"Returns the visual properties for the nodes that match the passed parameters.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "[ {\"SUID\": 92,\"visualProperties\": [{\"visualProperty\": \"NODE_PAINT\",\"value\": \"#808080\"},{\"visualProperty\": \"NODE_VISIBLE\",\"value\": true}]}]");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new ListNodesTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "list");
			props.setProperty(COMMAND_NAMESPACE, "node");
			props.setProperty(COMMAND_DESCRIPTION, "List all of the nodes in a network");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Returns a list of the node SUIDs associated with the passed network parameter.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"nodes\": [101,102]}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new ListNetworkAttributesTaskFactory(CyNode.class, serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "list attributes");
			props.setProperty(COMMAND_NAMESPACE, "node");
			props.setProperty(COMMAND_DESCRIPTION, "List all of the columns for nodes");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Returns a list of column names assocated with nodes.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "[\"SUID\",\"name\"]");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new ListPropertiesTaskFactory(CyNode.class, serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "list properties");
			props.setProperty(COMMAND_NAMESPACE, "node");
			props.setProperty(COMMAND_DESCRIPTION, "List all of the visual properties for nodes");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Returns a list of visual properties available for nodes.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "[\"Paint\",\"Visible\"]");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new RenameNodeTaskFactory();
			var props = new Properties();
			props.setProperty(COMMAND, "rename");
			props.setProperty(COMMAND_NAMESPACE, "node");
			props.setProperty(COMMAND_DESCRIPTION, "Rename a node");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Sets the value of the name column for the passed node.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, CoreImplDocumentationConstants.RENAME_EXAMPLE_JSON);
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new SetNetworkAttributeTaskFactory(CyNode.class, serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "set attribute");
			props.setProperty(COMMAND_NAMESPACE, "node");
			props.setProperty(COMMAND_DESCRIPTION, "Change node table values for a node or set of nodes");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Sets the value of a specified column for the passed node or set of nodes.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, SetNetworkAttributeTaskFactory.COMMAND_EXAMPLE_JSON);
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new SetPropertiesTaskFactory(CyNode.class, serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "set properties");
			props.setProperty(COMMAND_NAMESPACE, "node");
			props.setProperty(COMMAND_DESCRIPTION, "Set node visual properties");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Sets the value of a specified property for the passed node or set of nodes.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, SetPropertiesTaskFactory.COMMAND_EXAMPLE_JSON);
			registerService(bc, factory, TaskFactory.class, props);
		}
		// NAMESPACE: edge
		{
			var factory = new CreateNetworkAttributeTaskFactory(CyEdge.class, serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "create attribute");
			props.setProperty(COMMAND_NAMESPACE, "edge");
			props.setProperty(COMMAND_DESCRIPTION, "Create a new column for edges");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Creates a new edge column.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, CreateNetworkAttributeTaskFactory.COMMAND_EXAMPLE_JSON);
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new GetEdgeTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "get");
			props.setProperty(COMMAND_NAMESPACE, "edge");
			props.setProperty(COMMAND_DESCRIPTION, "Get an edge");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
					"Returns the SUID of an edge that matches the passed parameters. If multiple edges are found, only one will be returned, and a warning will be reported in the Cytoscape Task History dialog.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"edge\": 101}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new GetNetworkAttributeTaskFactory(CyEdge.class, serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "get attribute");
			props.setProperty(COMMAND_NAMESPACE, "edge");
			props.setProperty(COMMAND_DESCRIPTION, "Get the values from a column in a set of edges");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Returns the attributes for the edges passed as parameters.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, GetNetworkAttributeTaskFactory.COMMAND_EXAMPLE_JSON);

			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new GetPropertiesTaskFactory(CyEdge.class, serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "get properties");
			props.setProperty(COMMAND_NAMESPACE, "edge");
			props.setProperty(COMMAND_DESCRIPTION, "Get the visual properties for edges");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
					"Returns the visual properties for the edges that match the passed parameters.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "[ {\"SUID\": 92,\"visualProperties\": [{\"visualProperty\": \"EDGE_PAINT\",\"value\": \"#808080\"},{\"visualProperty\": \"EDGE_VISIBLE\",\"value\": true}]}]");

			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new ListEdgesTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "list");
			props.setProperty(COMMAND_NAMESPACE, "edge");
			props.setProperty(COMMAND_DESCRIPTION, "List edges");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Returns a list of the edge SUIDs associated with the passed network parameter.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"edges\": [101,102]}");

			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new ListNetworkAttributesTaskFactory(CyEdge.class, serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "list attributes");
			props.setProperty(COMMAND_NAMESPACE, "edge");
			props.setProperty(COMMAND_DESCRIPTION, "List all of the columns for edges");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Returns a list of column names assocated with edges.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "[\"SUID\",\"name\"]");

			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new ListPropertiesTaskFactory(CyEdge.class, serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "list properties");
			props.setProperty(COMMAND_NAMESPACE, "edge");
			props.setProperty(COMMAND_DESCRIPTION, "List all of the visual properties for edges");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Returns a list of visual properties available for edges.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "[\"Paint\",\"Visible\"]");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new RenameEdgeTaskFactory();
			var props = new Properties();
			props.setProperty(COMMAND, "rename");
			props.setProperty(COMMAND_NAMESPACE, "edge");
			props.setProperty(COMMAND_DESCRIPTION, "Rename an edge");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Sets the value of the name column for the passed edge.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, CoreImplDocumentationConstants.RENAME_EXAMPLE_JSON);
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new SetNetworkAttributeTaskFactory(CyEdge.class, serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "set attribute");
			props.setProperty(COMMAND_NAMESPACE, "edge");
			props.setProperty(COMMAND_DESCRIPTION, "Change edge table values for an edge or set of edges");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Sets the value of a specified column for the passed edge or set of edges.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, SetNetworkAttributeTaskFactory.COMMAND_EXAMPLE_JSON);
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new SetPropertiesTaskFactory(CyEdge.class, serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "set properties");
			props.setProperty(COMMAND_NAMESPACE, "edge");
			props.setProperty(COMMAND_DESCRIPTION, "Change visual properties for a set of edges");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Sets the value of a specified property for the passed edge or set of edges.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, SetPropertiesTaskFactory.COMMAND_EXAMPLE_JSON);
			registerService(bc, factory, TaskFactory.class, props);
		}
	}

	private void createGroupTaskFactories(BundleContext bc, CyGroupManager groupManager, CyGroupFactory groupFactory) {
		{
			var factory = new GroupNodesTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(PREFERRED_MENU, NETWORK_GROUP_MENU);
			props.setProperty(TITLE, "Group Selected Nodes");
			props.setProperty(TOOLTIP, "Group Selected Nodes Together");
			props.setProperty(IN_TOOL_BAR, "false");
			props.setProperty(MENU_GRAVITY, "0.0");
			props.setProperty(IN_MENU_BAR, "false");
			props.setProperty(PREFERRED_ACTION, "NEW");
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
			registerService(bc, factory, GroupNodesTaskFactory.class, props);

			// For commands
			props = new Properties();
			props.setProperty(COMMAND, "create");
			props.setProperty(COMMAND_NAMESPACE, "group");
			props.setProperty(COMMAND_DESCRIPTION, "Create a new group of nodes");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Create a group from the specified nodes.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"group\":123}");
			registerService(bc, factory, TaskFactory.class, props);

			// Add Group Selected Nodes to the nodes context also
			props = new Properties();
			props.setProperty(PREFERRED_MENU, NODE_GROUP_MENU);
			props.setProperty(MENU_GRAVITY, "0.0");
			props.setProperty(TITLE, "Group Selected Nodes");
			props.setProperty(TOOLTIP, "Group Selected Nodes Together");
			props.setProperty(IN_TOOL_BAR, "false");
			props.setProperty(IN_MENU_BAR, "false");
			props.setProperty(PREFERRED_ACTION, "NEW");
			registerService(bc, factory, NodeViewTaskFactory.class, props);
		}
		{
			var factory = new GetGroupTaskFactory(applicationManager, groupManager, serviceRegistrar);
			// For commands
			var props = new Properties();
			props.setProperty(COMMAND, "get");
			props.setProperty(COMMAND_NAMESPACE, "group");
			props.setProperty(COMMAND_DESCRIPTION, "Get a particular group");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Get a group by providing a network and the group node identifier");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, GetGroupTask.EXAMPLE_JSON);
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			// UNGROUP
			var factory = new UnGroupNodesTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(PREFERRED_MENU, NETWORK_GROUP_MENU);
			props.setProperty(TITLE, "Ungroup Selected Nodes");
			props.setProperty(TOOLTIP, "Ungroup Selected Nodes");
			props.setProperty(IN_TOOL_BAR, "false");
			props.setProperty(IN_MENU_BAR, "false");
			props.setProperty(MENU_GRAVITY, "1.0");
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(COMMAND_DESCRIPTION, "Removes the selected group nodes and replaces them with the members of the groups. ");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
			registerService(bc, factory, UnGroupTaskFactory.class, props);

			props = new Properties();
			props.setProperty(COMMAND, "ungroup");
			props.setProperty(COMMAND_NAMESPACE, "group");
			props.setProperty(COMMAND_DESCRIPTION, "Ungroup a set of previously grouped nodes");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Ungroups one or more groups, expanding them if "+
			                                            "they are collapsed and removing the group nodes.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"groups\": [123,124]}");
			registerService(bc, factory, TaskFactory.class, props);

			// Add Ungroup Selected Nodes to the nodes context also
			props = new Properties();
			props.setProperty(PREFERRED_MENU, NODE_GROUP_MENU);
			props.setProperty(MENU_GRAVITY, "1.0");
			props.setProperty(INSERT_SEPARATOR_AFTER, "true");
			props.setProperty(TITLE, "Ungroup Selected Nodes");
			props.setProperty(TOOLTIP, "Ungroup Selected Nodes");
			props.setProperty(IN_TOOL_BAR, "false");
			props.setProperty(IN_MENU_BAR, "false");
			props.setProperty(PREFERRED_ACTION, "NEW");
			registerService(bc, factory, NodeViewTaskFactory.class, props);
			registerService(bc, factory, UnGroupNodesTaskFactory.class, props);
		}
		{
			// COLLAPSE
			var factory = new GroupNodeContextTaskFactoryImpl(groupManager, true, serviceRegistrar);
			var props = new Properties();
			props.setProperty(PREFERRED_MENU, NODE_GROUP_MENU);
			props.setProperty(TITLE, "Collapse Group(s)");
			props.setProperty(TOOLTIP, "Collapse Grouped Nodes");
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(MENU_GRAVITY, "2.0");
			registerService(bc, factory, NodeViewTaskFactory.class, props);
			registerService(bc, factory, CollapseGroupTaskFactory.class, props);

			props = new Properties();
			props.setProperty(COMMAND, "collapse");
			props.setProperty(COMMAND_NAMESPACE, "group");
			props.setProperty(COMMAND_DESCRIPTION, "Collapse groups");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Replaces the representation of all of the nodes and edges in a group with a single node");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"groups\": [123,124]}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			// EXPAND
			var factory = new GroupNodeContextTaskFactoryImpl(groupManager, false, serviceRegistrar);
			var props = new Properties();
			props.setProperty(PREFERRED_MENU, NODE_GROUP_MENU);
			props.setProperty(TITLE, "Expand Group(s)");
			props.setProperty(TOOLTIP, "Expand Group(s)");
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(MENU_GRAVITY, "3.0");
			registerService(bc, factory, NodeViewTaskFactory.class, props);
			registerService(bc, factory, ExpandGroupTaskFactory.class, props);

			props = new Properties();
			props.setProperty(COMMAND, "expand");
			props.setProperty(COMMAND_NAMESPACE, "group");  // TODO right namespace?
			props.setProperty(COMMAND_DESCRIPTION, "Expand collapsed groups");  // TODO right namespace?
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Replaces the group node with member nodes for a set of groups");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"groups\": [123,124]}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new AddToGroupTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "add");
			props.setProperty(COMMAND_NAMESPACE, "group");
			props.setProperty(COMMAND_DESCRIPTION, "Add nodes or edges to a group");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{}");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Adds the specified nodes and edges to the specified group");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new ListGroupsTaskFactory(applicationManager, groupManager,serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "list");
			props.setProperty(COMMAND_NAMESPACE, "group");
			props.setProperty(COMMAND_DESCRIPTION, "List all of the groups in a network");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Lists the SUIDs of all of the groups in a network");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"groups\": [123,124,126]}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new RemoveFromGroupTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "remove");
			props.setProperty(COMMAND_NAMESPACE, "group");
			props.setProperty(COMMAND_DESCRIPTION, "Remove nodes or edges from a group");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Remove the selected nodes and edges from their current group");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new RenameGroupTaskFactory(applicationManager, groupManager, serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "rename");
			props.setProperty(COMMAND_NAMESPACE, "group");
			props.setProperty(COMMAND_DESCRIPTION, "Rename a group");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Changes the name of the selected group or groups");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{}");
			registerService(bc, factory, TaskFactory.class, props);
		}

		// TODO: add to group...
		// TODO: remove from group...
	}

	private void createTableTaskFactories(BundleContext bc) {
		{
			var factory = new ExportSelectedTableTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(ENABLE_FOR, "table");
			props.setProperty(PREFERRED_MENU, "File.Export[24.8]");
			props.setProperty(MENU_GRAVITY, "5");
			props.setProperty(TITLE, "Table to File...");
			props.setProperty(INSERT_SEPARATOR_BEFORE, "true");

			props.setProperty(TOOLTIP, "Save a table to the file system");
			registerService(bc, factory, TaskFactory.class, props);
			registerService(bc, factory, ExportSelectedTableTaskFactory.class, props);
		}
		{
			var factory = new ExportTableTaskFactoryImpl(serviceRegistrar);

			var icon = new TextIcon(IconUtil.FILE_EXPORT, iconFont.deriveFont(22.0f), 32, 31);
			var iconId = "cy::Table:EXPORT_TABLE";
			iconManager.addIcon(iconId, icon);

			var props = new Properties();
			props.setProperty(ENABLE_FOR, "table");
			props.setProperty(IN_NODE_TABLE_TOOL_BAR, "true");
			props.setProperty(IN_EDGE_TABLE_TOOL_BAR, "true");
			props.setProperty(IN_NETWORK_TABLE_TOOL_BAR, "true");
			props.setProperty(IN_UNASSIGNED_TABLE_TOOL_BAR, "true");
			props.setProperty(TOOL_BAR_GRAVITY, "0.007");
			props.setProperty(LARGE_ICON_ID, iconId);
			props.setProperty(TOOLTIP, "Export Table to File...");
			props.setProperty(INSERT_TOOLBAR_SEPARATOR_AFTER, "true");
			registerService(bc, factory, TableTaskFactory.class, props);
			registerService(bc, factory, ExportTableTaskFactory.class);
		}
		{
			var factory = new MergeTablesTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(ENABLE_FOR, "table");
			props.setProperty(PREFERRED_MENU, "Tools.Merge[2.0]");
			props.setProperty(TITLE, "Tables...");
			// props.setProperty(ServiceProperties.INSERT_SEPARATOR_AFTER, "true");
			// props.setProperty(TOOL_BAR_GRAVITY, "1.1");
			props.setProperty(MENU_GRAVITY, "8");
			props.setProperty(TOOLTIP, "Merge Tables");
			props.setProperty(COMMAND, "merge");
			props.setProperty(COMMAND_NAMESPACE, "table");
			props.setProperty(COMMAND_DESCRIPTION, "Merge tables together");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Merge tables together joining around a designated key column.  Depending on the arguments, might merge into multiple local tables.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"tables\":[101,102]}");
			registerService(bc, factory, TaskFactory.class, props);
			registerService(bc, factory, MergeTablesTaskFactory.class, props);
		}
		{
			var factory = new MapGlobalToLocalTableTaskFactoryImpl(tableManager, netManager, tunableSetter);
			var props = new Properties();
			// props.setProperty(ID, "mapGlobalToLocalTableTaskFactory");
			// props.setProperty(PREFERRED_MENU, "Tools");
			// props.setProperty(ACCELERATOR, "cmd m");
			// props.setProperty(TITLE, "Map Table to Attributes");
			// props.setProperty(MENU_GRAVITY, "1.0");
			// props.setProperty(TOOL_BAR_GRAVITY, "3.0");
			// props.setProperty(IN_TOOL_BAR, "false");
			// props.setProperty(COMMAND, "map-global-to-local");
			// props.setProperty(COMMAND_NAMESPACE, "table");
			registerService(bc, factory, TableTaskFactory.class, props);
			registerService(bc, factory, MapGlobalToLocalTableTaskFactory.class, props);
		}
		{
			var factory = new MapTableToNetworkTablesTaskFactoryImpl(serviceRegistrar);
			registerService(bc, factory, MapTableToNetworkTablesTaskFactory.class);
		}
		{
			var factory = new DeleteTableTaskFactoryImpl(serviceRegistrar);
			registerService(bc, factory, TableTaskFactory.class);
			registerService(bc, factory, DeleteTableTaskFactory.class);
		}
		{
			var factory = new CopyValueToColumnTaskFactoryImpl(false, "Apply to entire column", serviceRegistrar);
			var props = new Properties();
			props.setProperty(TITLE, factory.getTaskFactoryName());
			props.setProperty("tableTypes", "node,edge,network,unassigned");
			registerService(bc, factory, TableCellTaskFactory.class, props);
		}
		{
			var factory = new CopyValueToColumnTaskFactoryImpl(true, "Apply to selected nodes", serviceRegistrar);
			var props = new Properties();
			props.setProperty(TITLE, factory.getTaskFactoryName());
			props.setProperty("tableTypes", "node");
			registerService(bc, factory, TableCellTaskFactory.class, props);
		}
		{
			var factory = new CopyValueToColumnTaskFactoryImpl(true, "Apply to selected edges", serviceRegistrar);
			var props = new Properties();
			props.setProperty(TITLE, factory.getTaskFactoryName());
			props.setProperty("tableTypes", "edge");
			registerService(bc, factory, TableCellTaskFactory.class, props);
		}
		// ---------- COMMANDS ----------
		// NAMESPACE: table
		{
			var factory = new ExportNoGuiSelectedTableTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "export");
			props.setProperty(COMMAND_NAMESPACE, "table");
			props.setProperty(COMMAND_DESCRIPTION, "Export a table to a file");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Creates a file with name <FILE> and writes the table there.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"file\": \"myfile.csv\"}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new CreateTableTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "create table");
			props.setProperty(COMMAND_NAMESPACE, "table");
			props.setProperty(COMMAND_DESCRIPTION, "Create a new table");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Adds a new table to the network.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"table\":101}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new DestroyTableTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "destroy");
			props.setProperty(COMMAND_NAMESPACE, "table");
			props.setProperty(COMMAND_DESCRIPTION, "Destroy (delete) an entire table");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Removes the specified table from the network. ");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"table\": 101}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new DeleteColumnCommandTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "delete column");
			props.setProperty(COMMAND_NAMESPACE, "table");
			props.setProperty(COMMAND_DESCRIPTION, "Delete a column from a table");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Remove a column from a table, specified by its name.  Returns the name of the column removed.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"table\":101,\"column\":\"defunct\"}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var icon = new TextIcon(IconManager.ICON_TRASH_O, iconManager.getIconFont(SMALL_ICON_FONT_SIZE), SMALL_ICON_SIZE, SMALL_ICON_SIZE);
			var iconId = "cy::DELETE_COLUMN_SMALL";
			iconManager.addIcon(iconId, icon);

			var factory = new DeleteColumnTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(TITLE, "Delete Column");
			props.setProperty(MENU_GRAVITY, "1.3");
			props.setProperty(SMALL_ICON_ID, iconId);
			props.setProperty(INSERT_SEPARATOR_AFTER, "true");
			registerService(bc, factory, TableColumnTaskFactory.class, props);
			registerService(bc, factory, DeleteColumnTaskFactory.class, props);
		}
		{
			var icon = new TextIcon(IconManager.ICON_EDIT, iconManager.getIconFont(SMALL_ICON_FONT_SIZE), SMALL_ICON_SIZE, SMALL_ICON_SIZE);
			var iconId = "cy::RENAME_COLUMN_SMALL";
			iconManager.addIcon(iconId, icon);

			var factory = new RenameColumnTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(TITLE, "Rename Column...");
			props.setProperty(MENU_GRAVITY, "1.2");
			props.setProperty(SMALL_ICON_ID, iconId);
			props.setProperty(COMMAND, "rename column");
			props.setProperty(COMMAND_NAMESPACE, "table");
			props.setProperty(COMMAND_DESCRIPTION, "Rename a column in a table");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Changes the name of a specified column in the table.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"table\":101,\"column\":\"New Column\"}");
			registerService(bc, factory, TableColumnTaskFactory.class, props);
			registerService(bc, factory, RenameColumnTaskFactory.class, props);
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new AddRowTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "add row");
			props.setProperty(COMMAND_NAMESPACE, "table");
			props.setProperty(COMMAND_DESCRIPTION, "Add a new row to a table");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Appends an additional row of empty cells to the current table");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"table\":101,\"row\":\"row key\"}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new CreateColumnTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "create column");
			props.setProperty(COMMAND_NAMESPACE, "table");
			props.setProperty(COMMAND_DESCRIPTION, "Create a new column in a table");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Appends an additional column of attribute values to the current table");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"table\":101, \"column\": \"uncertainty\"}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new DeleteRowTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "delete row");
			props.setProperty(COMMAND_NAMESPACE, "table");
			props.setProperty(COMMAND_DESCRIPTION, "Delete a row from a table");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
			                  "Deletes a row from a table."+
			                  "Requires the table name or SUID and the row key.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"table\":101,\"key\":\"62\"}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new GetColumnTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "get column");
			props.setProperty(COMMAND_NAMESPACE, "table");
			props.setProperty(COMMAND_DESCRIPTION, "Get the information about a table column");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Get the information about a table column.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"name\": \"New name\", \"type\": \"String\", "+
			                                        "\"immutable\": false, \"primaryKey\": false, \"values\":[\"EGFR\",\"BRCA1\",\"BRCA2\"]}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new GetRowTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "get row");
			props.setProperty(COMMAND_NAMESPACE, "table");
			props.setProperty(COMMAND_DESCRIPTION, "Return all values in a table row");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Returns the values in each column of a row of a table.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"table\":101,\"SUID\":101,\"name\":\"mynode\"}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new GetValueTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "get value");
			props.setProperty(COMMAND_NAMESPACE, "table");
			props.setProperty(COMMAND_DESCRIPTION, "Return a single value from a table");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Returns the value from a cell as specified by row and column ids");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"table\":123, \"row\":\"123\", \"column\":\"degree\", \"value\":1}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new ListColumnsTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "list columns");
			props.setProperty(COMMAND_NAMESPACE, "table");
			props.setProperty(COMMAND_DESCRIPTION, "List all of the columns in a table");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Returns the list of columns in the table");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "[\"name\",\"degree\"]");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new ListRowsTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "list rows");
			props.setProperty(COMMAND_NAMESPACE, "table");
			props.setProperty(COMMAND_DESCRIPTION, "List all of the rows in a table");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Returns the list of primary keys for each of the rows in the specified table");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"table\": 123, \"rows\":[\"101\",\"102\",\"103\"]}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new ListTablesTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "list");
			props.setProperty(COMMAND_NAMESPACE, "table");
			props.setProperty(COMMAND_DESCRIPTION, "List all of the registered tables");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Returns a list of the table SUIDs associated with the passed network parameter.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"tables\":[101,102,104]}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new SetTableTitleTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "set title");
			props.setProperty(COMMAND_NAMESPACE, "table");
			props.setProperty(COMMAND_DESCRIPTION, "Set the title of a table");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Changes the visible identifier of a single table");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"table\":101, \"title\": \"My Title\"}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new SetValuesTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "set values");
			props.setProperty(COMMAND_NAMESPACE, "table");
			props.setProperty(COMMAND_DESCRIPTION, "Set values in a table");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Set all the values in the specified list of rows with a single value");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"table\":101, \"rows\":[\"key1\",\"key1\",\"key1\"]}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		// NAMESPACE: network
		{
			var factory = new CreateNetworkAttributeTaskFactory(CyNetwork.class, serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "create attribute");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND_DESCRIPTION, "Create a new column in the network table");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Creates a new network column.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, CreateNetworkAttributeTaskFactory.COMMAND_EXAMPLE_JSON);
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new GetNetworkAttributeTaskFactory(CyNetwork.class, serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "get attribute");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND_DESCRIPTION, "Get the value from a column for a network");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Returns the attributes for the network passed as parameter.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, GetNetworkAttributeTaskFactory.COMMAND_EXAMPLE_JSON);
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new ListNetworkAttributesTaskFactory(CyNetwork.class, serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "list attributes");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND_DESCRIPTION, "List all of the columns for networks");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Returns a list of column names assocated with a network.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "[\"SUID\",\"shared name\",\"name\",\"selected\",\"__Annotations\",\"publication\"]");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new SetNetworkAttributeTaskFactory(CyNetwork.class, serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "set attribute");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND_DESCRIPTION, "Set a value in the network table");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Sets the value of a specified column for the passed network.");
			props.setProperty(COMMAND_EXAMPLE_JSON, SetNetworkAttributeTaskFactory.COMMAND_EXAMPLE_JSON);
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			registerService(bc, factory, TaskFactory.class, props);
		}
	}

	private void createNetworkTaskFactories(BundleContext bc, CyGroupManager groupManager, CyGroupFactory groupFactory) {
		{
			var factory = new NewEmptyNetworkTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(PREFERRED_MENU, "File.New Network[16]");
			props.setProperty(MENU_GRAVITY, "4.0");
			props.setProperty(TITLE, "Empty");
			props.setProperty(COMMAND, "create empty");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND_DESCRIPTION, "Create an empty network");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
					"Create a new, empty network. The new network may be created as part "+
					"of an existing network collection or a new network collection.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"network\":101}");
			registerService(bc, factory, TaskFactory.class, props);
			registerService(bc, factory, NewEmptyNetworkViewFactory.class, props);
			registerServiceListener(bc, factory::addNetworkViewRenderer, factory::removeNetworkViewRenderer, NetworkViewRenderer.class);
		}
		{
			var factory = new CloneNetworkTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK);
			props.setProperty(PREFERRED_MENU, "File.New Network[16]");
			props.setProperty(MENU_GRAVITY, "3.0");
			props.setProperty(TITLE, "Clone Current Network");
//			props.setProperty(TITLE, "From All Nodes, All Edges");
			props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
			props.setProperty(COMMAND, "clone");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND_DESCRIPTION, "Make a copy of the current network");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
					"Create a new network by cloning an existing network. The new network will "+
					"be created as part of a new network collection.  The SUID of the new network "+
					"and view (if one is created) are returned.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"network\":101,\"view\":400}");
			registerService(bc, factory, NetworkTaskFactory.class, props);
			registerService(bc, factory, CloneNetworkTaskFactory.class, props);
		}
		{
			var factory = new NewNetworkSelectedNodesOnlyTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(PREFERRED_MENU, "File.New Network[16]");
			props.setProperty(ACCELERATOR, "cmd n");
			props.setProperty(ENABLE_FOR, ENABLE_FOR_SELECTED_NODES);
			props.setProperty(TITLE, "From Selected Nodes, All Edges");
			props.setProperty(MENU_GRAVITY, "1.0");
			registerService(bc, factory, NetworkTaskFactory.class, props);
			registerService(bc, factory, NewNetworkSelectedNodesOnlyTaskFactory.class, props);
		}
		{
			var factory = new NewNetworkSelectedNodesEdgesTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_SELECTED_NODES_OR_EDGES);
			props.setProperty(PREFERRED_MENU, "File.New Network[16]");
			props.setProperty(MENU_GRAVITY, "2.0");
			props.setProperty(ACCELERATOR, "cmd shift n");
			props.setProperty(TITLE, "From Selected Nodes, Selected Edges");
			registerService(bc, factory, NetworkTaskFactory.class, props);
			registerService(bc, factory, NewNetworkSelectedNodesAndEdgesTaskFactory.class, props);
		}
		{
			var factory = new DestroyNetworkTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(PREFERRED_MENU, "Edit");
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK);
			props.setProperty(TITLE, "Destroy Networks");
			props.setProperty(MENU_GRAVITY, "3.2");
			// props.setProperty(COMMAND, "destroy");
			// props.setProperty(COMMAND_NAMESPACE, "network");
			registerService(bc, factory, NetworkCollectionTaskFactory.class, props);
			registerService(bc, factory, DestroyNetworkTaskFactory.class, props);
			var props2 = new Properties();
			props2.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK);
			props2.setProperty(COMMAND, "destroy");
			props2.setProperty(COMMAND_NAMESPACE, "network");
			props2.setProperty(COMMAND_DESCRIPTION, "Destroy (delete) a network");
			props2.setProperty(COMMAND_LONG_DESCRIPTION, "Destroy (delete) a network. The SUID of the destroyed network is returned.");
			props2.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props2.setProperty(COMMAND_EXAMPLE_JSON, "{\"network\":101}");
			registerService(bc, factory, TaskFactory.class, props2);
		}
		{
			var factory = new LoadNetworkFileTaskFactoryImpl(serviceRegistrar);

			var icon = new TextIcon(LAYERED_IMPORT_NET, iconFont, COLORS_2B, LARGE_ICON_SIZE, LARGE_ICON_SIZE);
			var iconId = "cy::IMPORT_NET";
			iconManager.addIcon(iconId, icon);

			var props = new Properties();
			props.setProperty(ID, "loadNetworkFileTaskFactory");
			props.setProperty(PREFERRED_MENU, "File.Import[23.0]");
//			props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
			props.setProperty(ACCELERATOR, "cmd l");
			props.setProperty(TITLE, "Network from File...");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND, "load file");
			props.setProperty(COMMAND_DESCRIPTION, "Load a network file (e.g. XGMML)");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
					"Load a new network from a network file type "+
					"(e.g. ``SIF``, ``XGMML``, etc.).  Use ``network import file`` "+
					"to load networks from Excel or csv files.  This command will create a "+
					"new network collection if no current network collection is selected, otherwise "+
					"it will add the network to the current collection.	The SUIDs of the new networks "+
					"and views are returned.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, GenerateNetworkViewsTask.JSON_EXAMPLE);
			props.setProperty(MENU_GRAVITY, "0.1");
			props.setProperty(TOOL_BAR_GRAVITY, "2.0");
			props.setProperty(LARGE_ICON_ID, iconId);
			props.setProperty(IN_TOOL_BAR, "true");
			props.setProperty(TOOLTIP, "Import Network from File System");
			props.setProperty(TOOLTIP_LONG_DESCRIPTION,
					"Opens a network from a file and adds it to the current session.");
			registerService(bc, factory, TaskFactory.class, props);
			registerService(bc, factory, LoadNetworkFileTaskFactory.class, props);
		}
		{
			var factory = new LoadMultipleNetworkFilesTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			// props.setProperty(ID, "loadMultipleNetworkFilesTaskFactory");
			// props.setProperty(COMMAND_NAMESPACE, "network");
			// props.setProperty(COMMAND, "load file");
			// props.setProperty(COMMAND_DESCRIPTION, "Load a network file (e.g. XGMML)");
			registerService(bc, factory, TaskFactory.class, props);
			registerService(bc, factory, LoadMultipleNetworkFilesTaskFactory.class, props);
		}
		{
			var factory = new LoadNetworkURLTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(ID, "loadNetworkURLTaskFactory");
			props.setProperty(PREFERRED_MENU, "File.Import[23.0]");
			props.setProperty(ACCELERATOR, "cmd shift l");
			props.setProperty(MENU_GRAVITY, "0.2");
			props.setProperty(TITLE, "Network from URL...");
			props.setProperty(TOOLTIP, "Open a network from a remote Internet location");
			props.setProperty(COMMAND, "load url");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND_DESCRIPTION, "Load a network file (e.g. XGMML) from a url");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
					"Load a new network from a URL that points to a network file type "+
					"(e.g. ``SIF``, ``XGMML``, etc.).  Use ``network import url`` "+
					"to load networks from Excel or csv files.  This command will create a "+
					"new network collection if no current network collection is selected, otherwise "+
					"it will add the network to the current collection.	The SUIDs of the new networks "+
					"and views are returned.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, GenerateNetworkViewsTask.JSON_EXAMPLE);
			registerService(bc, factory, TaskFactory.class, props);
			registerService(bc, factory, LoadNetworkURLTaskFactory.class, props);
		}
		// Rename Network (Command & Network Context-Menu)
		{
			var factory = new EditNetworkTitleTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU, "true");
			props.setProperty(IN_CONTEXT_MENU, "false");
			props.setProperty(ENABLE_FOR, ENABLE_FOR_SINGLE_NETWORK);
			props.setProperty(MENU_GRAVITY, "5.5");
			props.setProperty(TITLE, "Rename Network...");
			props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
			props.setProperty(COMMAND, "rename");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND_DESCRIPTION, "Rename a network");
			props.setProperty(COMMAND_LONG_DESCRIPTION,  "Rename an existing network.  The SUID of the network is returned");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"network\":101, \"title\":\"My title\"}");
			registerService(bc, factory, EditNetworkTitleTaskFactory.class, props);
			registerService(bc, factory, NetworkTaskFactory.class, props);
		}
		{
			var factory = new ExportSelectedNetworkTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK);
			props.setProperty(PREFERRED_MENU, "File.Export[24.8]");
			props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
			props.setProperty(MENU_GRAVITY, "2.99");
			props.setProperty(TITLE, "Network to File...");
			props.setProperty(IN_CONTEXT_MENU, "false");
			props.setProperty(TOOLTIP, "Save this network to the file system.");
			registerService(bc, factory, TaskFactory.class, props);
			registerService(bc, factory, ExportSelectedNetworkTaskFactory.class, props);
		}
		{
			var factory = new ExportNetworkTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(ID, "exportNetworkTaskFactory");
			registerService(bc, factory, NetworkTaskFactory.class, props);
			registerService(bc, factory, ExportNetworkTaskFactory.class, props);

			// Now register the command version
			props = new Properties();
			props.setProperty(COMMAND, "export");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND_DESCRIPTION, "Export a network to a file");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Export a network to a network file (e.g. ``XGMML``, ``SIF``, etc.)");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"file\":\"/tmp/foo.sif\"}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			// Register as 3 types of service.
			var factory = new ConnectSelectedNodesTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(IN_MENU_BAR, "false");
			props.setProperty(IN_TOOL_BAR, "false");
			props.setProperty(PREFERRED_ACTION, "NEW");
			props.setProperty(PREFERRED_MENU, NODE_ADD_MENU);
			props.setProperty(MENU_GRAVITY, "0.2");
			props.setProperty(TITLE, "Edges Connecting Selected Nodes");
			registerService(bc, factory, NodeViewTaskFactory.class, props);
			registerService(bc, factory, ConnectSelectedNodesTaskFactory.class, props);
			var props2 = new Properties();
			props2.setProperty(COMMAND, "connect nodes");
			props2.setProperty(COMMAND_NAMESPACE, "network");
			props2.setProperty(COMMAND_DESCRIPTION, "Create new edges that connect a list of nodes");
			props2.setProperty(COMMAND_LONG_DESCRIPTION, "Create new edges that connect a list of nodes");
			props2.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props2.setProperty(COMMAND_EXAMPLE_JSON, "{\"edges\": [102,103]}");
			registerService(bc, factory, NetworkTaskFactory.class, props2);
		}
		// ---------- COMMANDS ----------
		// NAMESPACE: network
		{
			var factory = new NewNetworkCommandTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "create");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND_DESCRIPTION, "Create a new network");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
					"Create a new network from a list of nodes and edges in an existing source network. "+
					"The SUID of the network and view are returned.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"network\":102,\"view\":500}");
			registerService(bc, factory, NetworkTaskFactory.class, props);
		}
		{
			var factory = new GetNetworkTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "get");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND_DESCRIPTION, "Return a network");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
					"Return a network from the name, SUID, "+
					"or other identifier.  If the name or SUID "+
					"doesn't exist, the current network is returned.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON,
					"{\"shared name\": \"my network\", "+
					"\"SUID\": 80, \"name\":\"my network\"}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new ListNetworksTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "list");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND_DESCRIPTION, "List all of the available networks");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "List all of the networks in the current session.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"networks\": [102,103]}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new SetCurrentNetworkTaskFactory(applicationManager);
			var props = new Properties();
			props.setProperty(COMMAND, "set current");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND_DESCRIPTION, "Set the current network");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Sets the current network, which can also be null.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new ListPropertiesTaskFactory(CyNetwork.class, serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "list properties");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND_DESCRIPTION, "List all of the visual properties for networks");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "List all of the visual properties for networks");
			props.setProperty(COMMAND_EXAMPLE_JSON, "[\"Background Paint\",\"Node Selection\",\"Edge Selection\"]");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new GetPropertiesTaskFactory(CyNetwork.class, serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "get properties");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND_DESCRIPTION, "Get the visual property value for a network");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
					"Returns the visual properties for the network that matches the passed parameters.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "[ {\"SUID\": 92,\"visualProperties\": [{\"visualProperty\": \"NETWORK_BACKGROUND_PAINT\",\"value\": \"#808080\"},{\"visualProperty\": \"NETWORK_TITLE\",\"value\": \"my network\"}]}]");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new SetPropertiesTaskFactory(CyNetwork.class, serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "set properties");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND_DESCRIPTION, "Set network visual properties");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Sets the value of a specified property for the passed network.");
			props.setProperty(COMMAND_EXAMPLE_JSON, SetPropertiesTaskFactory.COMMAND_EXAMPLE_JSON);
			registerService(bc, factory, TaskFactory.class, props);
		}
	}

	private void createSessionTaskFactories(BundleContext bc) {
		{
			var factory = new NewSessionTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(PREFERRED_MENU, "File");
			props.setProperty(MENU_GRAVITY, "1.8");
			props.setProperty(INSERT_SEPARATOR_AFTER, "true");

			props.setProperty(TITLE, "Close Session");
			props.setProperty(COMMAND, "new");
			props.setProperty(COMMAND_NAMESPACE, "session");
			props.setProperty(COMMAND_DESCRIPTION, "Create a new, empty session");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Destroys the current session and creates a new, empty one.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{ }");
			registerService(bc, factory, TaskFactory.class, props);
			registerService(bc, factory, NewSessionTaskFactory.class, props);
		}
		{
			var factory = new OpenSessionTaskFactoryImpl(serviceRegistrar);

			var icon = new TextIcon(LAYERED_OPEN_FILE, iconFont, COLORS_2B, LARGE_ICON_SIZE, LARGE_ICON_SIZE);
			var iconId = "cy::OPEN_FILE";
			iconManager.addIcon(iconId, icon);

			var props = new Properties();
			props.setProperty(ID, "openSessionTaskFactory");
			props.setProperty(PREFERRED_MENU, "File");
			props.setProperty(ACCELERATOR, "cmd o");
			props.setProperty(LARGE_ICON_ID, iconId);
			props.setProperty(TITLE, "Open Session...");
			props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
			props.setProperty(TOOL_BAR_GRAVITY, "1.0");
			props.setProperty(IN_TOOL_BAR, "true");
			props.setProperty(MENU_GRAVITY, "0.5");
			props.setProperty(TOOLTIP, "Open Session");
			props.setProperty(TOOLTIP_LONG_DESCRIPTION, "Opens a session file (.cys) from the file system.");
			registerService(bc, factory, OpenSessionTaskFactory.class, props);
			registerService(bc, factory, TaskFactory.class, props);

		}
		{
			// We can't use the "normal" OpenSessionTaskFactory for commands
			// because it inserts the task with the file tunable in it, so the Command processor never sees it.
			// We need a special OpenSessionTaskFactory for commands
			var factory = new OpenSessionCommandTaskFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(COMMAND, "open");
			props.setProperty(COMMAND_NAMESPACE, "session");
			props.setProperty(COMMAND_DESCRIPTION, "Open a session from a file");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Opens a session from a local file or URL.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new SaveSessionTaskFactoryImpl(serviceRegistrar);

			var icon = new TextIcon(LAYERED_SAVE, iconFont, COLORS_3, LARGE_ICON_SIZE, LARGE_ICON_SIZE, 1, 2);
			var iconId = "cy::SAVE";
			iconManager.addIcon(iconId, icon);

			var props = new Properties();
			props.setProperty(PREFERRED_MENU, "File");
			props.setProperty(ACCELERATOR, "cmd s");
			props.setProperty(LARGE_ICON_ID, iconId);
			props.setProperty(TITLE, "Save Session");
			props.setProperty(TOOL_BAR_GRAVITY, "1.1");
			props.setProperty(IN_TOOL_BAR, "true");
			props.setProperty(MENU_GRAVITY, "1.5");
			props.setProperty(TOOLTIP, "Save Session");
			props.setProperty(TOOLTIP_LONG_DESCRIPTION, "Saves the session to a file.");
			props.setProperty(COMMAND, "save");
			props.setProperty(COMMAND_NAMESPACE, "session");
			props.setProperty(COMMAND_DESCRIPTION, "Save the session");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
					"Saves the current session to an existing file, which will be replaced."
					+ " If this is a new session that has not been saved yet, use 'save as' instead.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{ }");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			var factory = new SaveSessionAsTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(PREFERRED_MENU, "File");
			props.setProperty(ACCELERATOR, "cmd shift s");
			props.setProperty(MENU_GRAVITY, "1.7");
			props.setProperty(TITLE, "Save Session As...");
			props.setProperty(COMMAND, "save as");
			props.setProperty(COMMAND_NAMESPACE, "session");
			props.setProperty(COMMAND_DESCRIPTION, "Save the session to a file");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Saves the current session as a new file.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{ }");
			registerService(bc, factory, TaskFactory.class, props);
			registerService(bc, factory, SaveSessionAsTaskFactory.class, props);
		}
	}

	private void createVizmapTaskFactories(BundleContext bc) {
		{
			var factory = new ApplyVisualStyleTaskFactoryimpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(ID, "applyVisualStyleTaskFactory");
			props.setProperty(COMMAND, "apply");
			props.setProperty(COMMAND_NAMESPACE, "vizmap");
			props.setProperty(COMMAND_DESCRIPTION, "Apply a style");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Applies the specified style to the selected views and returns the SUIDs of the affected views.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"views\": [ 322, 420 ]}");
			registerService(bc, factory, NetworkViewCollectionTaskFactory.class, props);
			registerService(bc, factory, ApplyVisualStyleTaskFactory.class, props);
		}
		{
			var factory = new ExportVizmapTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(ENABLE_FOR, "vizmap");
			props.setProperty(PREFERRED_MENU, "File.Export[24.8]");
			props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
			props.setProperty(MENU_GRAVITY, "8");
			props.setProperty(TITLE, "Styles to File...");
			props.setProperty(COMMAND, "export");
			props.setProperty(COMMAND_NAMESPACE, "vizmap");
			props.setProperty(COMMAND_DESCRIPTION, "Export styles to a file");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Exports the specified styles to a Cytoscape vizmap (XML) or a Cytoscape.js (JSON) file and returns the path to the saved file.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{ \"file\": \"/Users/johndoe/Downloads/MyStyles.json\" }");
			registerService(bc, factory, TaskFactory.class, props);
			registerService(bc, factory, ExportVizmapTaskFactory.class, props);
		}
		{
			var factory = new LoadVizmapFileTaskFactoryImpl(serviceRegistrar);
			var props = new Properties();
			props.setProperty(PREFERRED_MENU, "File.Import[23.0]");
			props.setProperty(MENU_GRAVITY, "20.0");
			props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
			props.setProperty(TITLE, "Styles from File...");
			props.setProperty(COMMAND, "load file");
			props.setProperty(COMMAND_NAMESPACE, "vizmap");
			props.setProperty(COMMAND_DESCRIPTION, "Load styles from a file");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Loads styles from a vizmap (XML or properties) file and returns the names of the loaded styles.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "[ \"My Style 1\", \"My Style 2\" ]");
			registerService(bc, factory, TaskFactory.class, props);
			registerService(bc, factory, LoadVizmapFileTaskFactory.class, new Properties());
		}
		{
			// Clear edge bends - Main menu
			var factory = new ClearAllEdgeBendsFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(ID, "clearAllEdgeBendsFactory");
			props.setProperty(TITLE, "Clear All Edge Bends");
			props.setProperty(PREFERRED_MENU, "Layout");
			props.setProperty(MENU_GRAVITY, "0.1");
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
			registerService(bc, factory, NetworkViewCollectionTaskFactory.class, props);
		}
		{
			// Clear edge bends - Network context menu
			var factory = new ClearAllEdgeBendsFactory(serviceRegistrar);
			var props = new Properties();
			props.setProperty(TITLE, "Clear All Edge Bends");
			props.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU, "true");
			props.setProperty(MENU_GRAVITY, "6.0");
			props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
			registerService(bc, factory, NetworkViewCollectionTaskFactory.class, props);
		}
	}

	private void createHelpTaskFactories(BundleContext bc) {
		{
			var factory = new HelpTaskFactory(serviceRegistrar);

			var icon = new TextIcon(LAYERED_HELP, iconFont, COLORS_2A, LARGE_ICON_SIZE, LARGE_ICON_SIZE, 1);
			var iconId = "cy::HELP";
			iconManager.addIcon(iconId, icon);

			var props = new Properties();
			props.setProperty(ACCELERATOR, "cmd ?");
			props.setProperty(LARGE_ICON_ID, iconId);
			props.setProperty(TITLE, "Help");
			props.setProperty(TOOLTIP, "Help");
			props.setProperty(TOOLTIP_LONG_DESCRIPTION, "Opens a web page that contains context sensitive help.");
			props.setProperty(TOOL_BAR_GRAVITY, "" + Float.MAX_VALUE);
			props.setProperty(IN_TOOL_BAR, "true");
			registerService(bc, factory, TaskFactory.class, props);
		}
	}
}
