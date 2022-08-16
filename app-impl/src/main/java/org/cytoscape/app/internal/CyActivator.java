package org.cytoscape.app.internal;

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_EXAMPLE_JSON;
import static org.cytoscape.work.ServiceProperties.COMMAND_LONG_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.COMMAND_SUPPORTS_JSON;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.IN_TOOL_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.app.internal.action.UpdateNotificationAction;
import org.cytoscape.app.event.AppsFinishedStartingEvent;
import org.cytoscape.app.event.AppsFinishedStartingListener;
import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.App.AppStatus;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.net.UpdateManager;
import org.cytoscape.app.internal.net.WebQuerier;
import org.cytoscape.app.internal.task.AppStoreTaskFactory;
import org.cytoscape.app.internal.task.AppManagerTaskFactory;
import org.cytoscape.app.internal.task.ManagerInstallAppsFromFileTaskFactory;
import org.cytoscape.app.internal.task.DisableTaskFactory;
import org.cytoscape.app.internal.task.EnableTaskFactory;
import org.cytoscape.app.internal.task.InformationTaskFactory;
import org.cytoscape.app.internal.task.InstallTaskFactory;
import org.cytoscape.app.internal.task.ListAppsTaskFactory;
import org.cytoscape.app.internal.task.ListAvailableTaskFactory;
import org.cytoscape.app.internal.task.ListUpdatesTaskFactory;
import org.cytoscape.app.internal.task.StatusTaskFactory;
import org.cytoscape.app.internal.task.UninstallTaskFactory;
import org.cytoscape.app.internal.task.UpdateTaskFactory;
import org.cytoscape.app.internal.tunable.AppConflictHandlerFactory;
import org.cytoscape.app.internal.ui.AppManagerMediator;
import org.cytoscape.app.internal.ui.downloadsites.DownloadSitesManager;
import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyVersion;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.command.AvailableCommands;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.data.CyGroupAggregationManager;
import org.cytoscape.io.datasource.DataSourceManager;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.io.read.CyPropertyReaderManager;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.io.read.CyTableReaderManager;
import org.cytoscape.io.write.CyNetworkViewWriterManager;
import org.cytoscape.io.write.CyPropertyWriterManager;
import org.cytoscape.io.write.CySessionWriterManager;
import org.cytoscape.io.write.CyTableWriterManager;
import org.cytoscape.io.write.PresentationWriterManager;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySessionManager;
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
import org.cytoscape.task.edit.RenameColumnTaskFactory;
import org.cytoscape.task.edit.UnGroupNodesTaskFactory;
import org.cytoscape.task.edit.UnGroupTaskFactory;
import org.cytoscape.task.hide.HideSelectedEdgesTaskFactory;
import org.cytoscape.task.hide.HideSelectedNodesTaskFactory;
import org.cytoscape.task.hide.HideSelectedTaskFactory;
import org.cytoscape.task.hide.UnHideAllEdgesTaskFactory;
import org.cytoscape.task.hide.UnHideAllNodesTaskFactory;
import org.cytoscape.task.hide.UnHideAllTaskFactory;
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
import org.cytoscape.task.select.SelectFirstNeighborsNodeViewTaskFactory;
import org.cytoscape.task.select.SelectFirstNeighborsTaskFactory;
import org.cytoscape.task.select.SelectFromFileListTaskFactory;
import org.cytoscape.task.visualize.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.task.visualize.ApplyVisualStyleTaskFactory;
import org.cytoscape.task.write.ExportNetworkImageTaskFactory;
import org.cytoscape.task.write.ExportNetworkViewTaskFactory;
import org.cytoscape.task.write.ExportSelectedTableTaskFactory;
import org.cytoscape.task.write.ExportTableTaskFactory;
import org.cytoscape.task.write.ExportVizmapTaskFactory;
import org.cytoscape.task.write.SaveSessionAsTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.properties.TunablePropertySerializerFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.GUITunableHandlerFactory;
import org.cytoscape.work.swing.PanelTaskManager;
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.service.startlevel.StartLevel;

/*
 * #%L
 * Cytoscape App Impl (app-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2021 The Cytoscape Consortium
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
	public void start(BundleContext bc) {
		CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);
		CyApplicationConfiguration applicationConfig = getService(bc, CyApplicationConfiguration.class);
		CyApplicationManager applicationManager = getService(bc, CyApplicationManager.class);
		CyEventHelper eventHelper = getService(bc, CyEventHelper.class);
		CyGroupAggregationManager groupAggregationManager = getService(bc, CyGroupAggregationManager.class);
		CyGroupFactory groupFactory = getService(bc, CyGroupFactory.class);
		CyGroupManager groupManager = getService(bc, CyGroupManager.class);
		CyLayoutAlgorithmManager layoutAlgorithmManager = getService(bc, CyLayoutAlgorithmManager.class);
		CyNetworkFactory networkFactory = getService(bc, CyNetworkFactory.class);
		CyNetworkManager networkManager = getService(bc, CyNetworkManager.class);
		CyNetworkViewFactory networkViewFactory = getService(bc, CyNetworkViewFactory.class);
		CyNetworkViewManager networkViewManager = getService(bc, CyNetworkViewManager.class);
		CyNetworkReaderManager networkViewReaderManager = getService(bc, CyNetworkReaderManager.class);
		CyNetworkViewWriterManager networkViewWriterManager = getService(bc, CyNetworkViewWriterManager.class);
		CyProperty<Properties> cyProperty = getService(bc, CyProperty.class, "(cyPropertyName=cytoscape3.props)");
		CyPropertyReaderManager cyPropertyReaderManager = getService(bc, CyPropertyReaderManager.class);
		CyPropertyWriterManager cyPropertyWriterManager = getService(bc, CyPropertyWriterManager.class);
		CyRootNetworkManager rootNetworkFactory = getService(bc, CyRootNetworkManager.class);
		CySessionManager sessionManager = getService(bc, CySessionManager.class);
		CySessionReaderManager sessionReaderManager = getService(bc, CySessionReaderManager.class);
		CySessionWriterManager sessionWriterManager = getService(bc, CySessionWriterManager.class);
		CySwingApplication swingApplication = getService(bc, CySwingApplication.class);
		CyTableFactory tableFactory = getService(bc, CyTableFactory.class);
		CyTableManager tableManager = getService(bc, CyTableManager.class);
		CyTableReaderManager tableReaderManager = getService(bc, CyTableReaderManager.class);
		CyTableWriterManager tableWriterManager = getService(bc, CyTableWriterManager.class);
		PanelTaskManager panelTaskManager = getService(bc, PanelTaskManager.class);
		DialogTaskManager dialogTaskManager = getService(bc, DialogTaskManager.class);
		PresentationWriterManager presentationWriterManager = getService(bc, PresentationWriterManager.class);
		RenderingEngineManager renderingEngineManager = getService(bc, RenderingEngineManager.class);
		TaskManager<?, ?> taskManager = getService(bc, TaskManager.class);
		UndoSupport undoSupport = getService(bc, UndoSupport.class);
		TunablePropertySerializerFactory tunablePropSerializerFactory = getService(bc, TunablePropertySerializerFactory.class);
		VisualMappingManager visualMappingManager = getService(bc, VisualMappingManager.class);
		VisualStyleFactory visualStyleFactory = getService(bc, VisualStyleFactory.class);
		CyVersion cyVersion = getService(bc, CyVersion.class);

		VisualMappingFunctionFactory vmfFactoryC = getService(bc, VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
		VisualMappingFunctionFactory vmfFactoryD = getService(bc, VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
		VisualMappingFunctionFactory vmfFactoryP = getService(bc ,VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");

		DataSourceManager dataSourceManager = getService(bc, DataSourceManager.class);

		// Start of core-task services

		LoadVizmapFileTaskFactory loadVizmapFileTaskFactory = getService(bc, LoadVizmapFileTaskFactory.class);
		LoadNetworkFileTaskFactory loadNetworkFileTaskFactory = getService(bc, LoadNetworkFileTaskFactory.class);
		LoadNetworkURLTaskFactory loadNetworkURLTaskFactory = getService(bc, LoadNetworkURLTaskFactory.class);
		DeleteSelectedNodesAndEdgesTaskFactory deleteSelectedNodesAndEdgesTaskFactory = getService(bc, DeleteSelectedNodesAndEdgesTaskFactory.class);
		SelectAllTaskFactory selectAllTaskFactory = getService(bc, SelectAllTaskFactory.class);

		SelectAllEdgesTaskFactory selectAllEdgesTaskFactory = getService(bc, SelectAllEdgesTaskFactory.class);
		SelectAllNodesTaskFactory selectAllNodesTaskFactory = getService(bc, SelectAllNodesTaskFactory.class);
		SelectAdjacentEdgesTaskFactory selectAdjacentEdgesTaskFactory = getService(bc, SelectAdjacentEdgesTaskFactory.class);
		SelectConnectedNodesTaskFactory selectConnectedNodesTaskFactory = getService(bc, SelectConnectedNodesTaskFactory.class);

		SelectFirstNeighborsTaskFactory selectFirstNeighborsTaskFactory = getService(bc, SelectFirstNeighborsTaskFactory.class, "(title=Undirected)");
		SelectFirstNeighborsTaskFactory selectFirstNeighborsTaskFactoryInEdge = getService(bc, SelectFirstNeighborsTaskFactory.class, "(title=Directed: Incoming)");
		SelectFirstNeighborsTaskFactory selectFirstNeighborsTaskFactoryOutEdge = getService(bc, SelectFirstNeighborsTaskFactory.class, "(title=Directed: Outgoing)");

		DeselectAllTaskFactory deselectAllTaskFactory = getService(bc, DeselectAllTaskFactory.class);
		DeselectAllEdgesTaskFactory deselectAllEdgesTaskFactory = getService(bc, DeselectAllEdgesTaskFactory.class);
		DeselectAllNodesTaskFactory deselectAllNodesTaskFactory = getService(bc, DeselectAllNodesTaskFactory.class);
		InvertSelectedEdgesTaskFactory invertSelectedEdgesTaskFactory = getService(bc, InvertSelectedEdgesTaskFactory.class);
		InvertSelectedNodesTaskFactory invertSelectedNodesTaskFactory = getService(bc, InvertSelectedNodesTaskFactory.class);
		SelectFromFileListTaskFactory selectFromFileListTaskFactory = getService(bc, SelectFromFileListTaskFactory.class);

		SelectFirstNeighborsNodeViewTaskFactory selectFirstNeighborsNodeViewTaskFactory = getService(bc, SelectFirstNeighborsNodeViewTaskFactory.class);

		HideSelectedTaskFactory hideSelectedTaskFactory = getService(bc, HideSelectedTaskFactory.class);
		HideSelectedNodesTaskFactory hideSelectedNodesTaskFactory = getService(bc, HideSelectedNodesTaskFactory.class);
		HideSelectedEdgesTaskFactory hideSelectedEdgesTaskFactory = getService(bc, HideSelectedEdgesTaskFactory.class);
		UnHideAllTaskFactory unHideAllTaskFactory = getService(bc, UnHideAllTaskFactory.class);
		UnHideAllNodesTaskFactory unHideAllNodesTaskFactory = getService(bc, UnHideAllNodesTaskFactory.class);
		UnHideAllEdgesTaskFactory unHideAllEdgesTaskFactory = getService(bc, UnHideAllEdgesTaskFactory.class);

		NewEmptyNetworkViewFactory newEmptyNetworkTaskFactory = getService(bc, NewEmptyNetworkViewFactory.class);

		CloneNetworkTaskFactory cloneNetworkTaskFactory = getService(bc, CloneNetworkTaskFactory.class);
		NewNetworkSelectedNodesAndEdgesTaskFactory newNetworkSelectedNodesEdgesTaskFactory = getService(bc, NewNetworkSelectedNodesAndEdgesTaskFactory.class);
		NewNetworkSelectedNodesOnlyTaskFactory newNetworkSelectedNodesOnlyTaskFactory = getService(bc, NewNetworkSelectedNodesOnlyTaskFactory.class);
		DestroyNetworkTaskFactory destroyNetworkTaskFactory = getService(bc, DestroyNetworkTaskFactory.class);
		DestroyNetworkViewTaskFactory destroyNetworkViewTaskFactory = getService(bc, DestroyNetworkViewTaskFactory.class);

		NewSessionTaskFactory newSessionTaskFactory = getService(bc, NewSessionTaskFactory.class);
		OpenSessionTaskFactory openSessionTaskFactory = getService(bc, OpenSessionTaskFactory.class);
		SaveSessionAsTaskFactory saveSessionAsTaskFactory = getService(bc, SaveSessionAsTaskFactory.class);
		EditNetworkTitleTaskFactory editNetworkTitleTaskFactory = getService(bc, EditNetworkTitleTaskFactory.class);
		CreateNetworkViewTaskFactory createNetworkViewTaskFactory = getService(bc, CreateNetworkViewTaskFactory.class);
		ExportNetworkImageTaskFactory exportNetworkImageTaskFactory = getService(bc, ExportNetworkImageTaskFactory.class);
		ExportNetworkViewTaskFactory exportNetworkViewTaskFactory = getService(bc, ExportNetworkViewTaskFactory.class);
		ExportSelectedTableTaskFactory exportSelectedTableTaskFactory = getService(bc, ExportSelectedTableTaskFactory.class);
		ExportTableTaskFactory exportTableTaskFactory = getService(bc, ExportTableTaskFactory.class);
		ApplyPreferredLayoutTaskFactory applyPreferredLayoutTaskFactory = getService(bc, ApplyPreferredLayoutTaskFactory.class);
		DeleteColumnTaskFactory deleteColumnTaskFactory = getService(bc, DeleteColumnTaskFactory.class);
		RenameColumnTaskFactory renameColumnTaskFactory = getService(bc, RenameColumnTaskFactory.class);
		DeleteTableTaskFactory deleteTableTaskFactory = getService(bc, DeleteTableTaskFactory.class);
		ExportVizmapTaskFactory exportVizmapTaskFactory = getService(bc, ExportVizmapTaskFactory.class);

		ConnectSelectedNodesTaskFactory connectSelectedNodesTaskFactory = getService(bc, ConnectSelectedNodesTaskFactory.class);
		MapGlobalToLocalTableTaskFactory mapGlobal = getService(bc, MapGlobalToLocalTableTaskFactory.class);
		ApplyVisualStyleTaskFactory applyVisualStyleTaskFactory = getService(bc, ApplyVisualStyleTaskFactory.class);
		MapTableToNetworkTablesTaskFactory mapNetworkAttrTaskFactory = getService(bc, MapTableToNetworkTablesTaskFactory.class);

		GroupNodesTaskFactory groupNodesTaskFactory = getService(bc, GroupNodesTaskFactory.class);
		UnGroupTaskFactory unGroupTaskFactory = getService(bc, UnGroupTaskFactory.class);
		CollapseGroupTaskFactory collapseGroupTaskFactory = getService(bc, CollapseGroupTaskFactory.class);
		ExpandGroupTaskFactory expandGroupTaskFactory = getService(bc, ExpandGroupTaskFactory.class);
		UnGroupNodesTaskFactory unGroupNodesTaskFactory = getService(bc, UnGroupNodesTaskFactory.class);

		// Command execution services
		CommandExecutorTaskFactory cyCommandExecutorTaskFactory = getService(bc, CommandExecutorTaskFactory.class);
		AvailableCommands availableCommands = getService(bc, AvailableCommands.class);

		CySwingAppAdapter cyAppAdapter = new CyAppAdapterImpl(
				 applicationConfig,
				 applicationManager,
                 eventHelper,
                 groupAggregationManager,
                 groupFactory,
                 groupManager,
                 layoutAlgorithmManager,
                 networkFactory,
                 networkManager,
                 networkViewFactory,
                 networkViewManager,
                 networkViewReaderManager,
                 networkViewWriterManager,
                 cyProperty,
                 cyPropertyReaderManager,
                 cyPropertyWriterManager,
                 rootNetworkFactory,
                 serviceRegistrar,
                 sessionManager,
                 sessionReaderManager,
                 sessionWriterManager,
                 swingApplication,
                 tableFactory,
                 tableManager,
                 tableReaderManager,
                 tableWriterManager,
                 cyVersion,
                 dialogTaskManager,
                 panelTaskManager,
                 presentationWriterManager,
                 renderingEngineManager,
                 taskManager,
                 undoSupport,
                 tunablePropSerializerFactory,
                 vmfFactoryC,
                 vmfFactoryD,
                 vmfFactoryP,
                 visualMappingManager,
                 visualStyleFactory,
                 dataSourceManager,
                 // from core-task-api
                 loadVizmapFileTaskFactory,
                 loadNetworkFileTaskFactory,
                 loadNetworkURLTaskFactory,
                 deleteSelectedNodesAndEdgesTaskFactory,
                 selectAllTaskFactory,
                 selectAllEdgesTaskFactory,
                 selectAllNodesTaskFactory,
                 selectAdjacentEdgesTaskFactory,
                 selectConnectedNodesTaskFactory,
                 selectFirstNeighborsTaskFactory,
                 selectFirstNeighborsTaskFactoryInEdge,
                 selectFirstNeighborsTaskFactoryOutEdge,
                 deselectAllTaskFactory,
                 deselectAllEdgesTaskFactory,
                 deselectAllNodesTaskFactory,
                 invertSelectedEdgesTaskFactory,
                 invertSelectedNodesTaskFactory,
                 selectFromFileListTaskFactory,
                 selectFirstNeighborsNodeViewTaskFactory,
                 hideSelectedTaskFactory,
                 hideSelectedNodesTaskFactory,
                 hideSelectedEdgesTaskFactory,
                 unHideAllTaskFactory,
                 unHideAllNodesTaskFactory,
                 unHideAllEdgesTaskFactory,
                 newEmptyNetworkTaskFactory,
                 cloneNetworkTaskFactory,
                 newNetworkSelectedNodesEdgesTaskFactory,
                 newNetworkSelectedNodesOnlyTaskFactory,
                 destroyNetworkTaskFactory,
                 destroyNetworkViewTaskFactory,
                 newSessionTaskFactory,
                 openSessionTaskFactory,
                 saveSessionAsTaskFactory,
                 editNetworkTitleTaskFactory,
                 createNetworkViewTaskFactory,
                 exportNetworkImageTaskFactory,
                 exportNetworkViewTaskFactory,
                 exportSelectedTableTaskFactory,
                 exportTableTaskFactory,
                 applyPreferredLayoutTaskFactory,
                 deleteColumnTaskFactory,
                 renameColumnTaskFactory,
                 deleteTableTaskFactory,
                 exportVizmapTaskFactory,
                 connectSelectedNodesTaskFactory,
                 mapGlobal,
                 applyVisualStyleTaskFactory,
                 mapNetworkAttrTaskFactory,
                 groupNodesTaskFactory,
                 unGroupTaskFactory,
                 collapseGroupTaskFactory,
                 expandGroupTaskFactory,
                 unGroupNodesTaskFactory,
                 cyCommandExecutorTaskFactory,
                 availableCommands
        );

		registerService(bc, cyAppAdapter, CyAppAdapter.class);
		registerService(bc, cyAppAdapter, CySwingAppAdapter.class);

		WebQuerier webQuerier = new WebQuerier(serviceRegistrar);
		registerService(bc, webQuerier, WebQuerier.class);

		StartLevel startLevel = getService(bc, StartLevel.class);

		// Instantiate new manager
		final AppManager appManager = new AppManager(cyAppAdapter, applicationConfig, cyVersion, eventHelper,
				webQuerier, startLevel, bc);
		registerService(bc, appManager, AppManager.class);
		bc.addFrameworkListener(appManager);

		final DownloadSitesManager downloadSitesManager = new DownloadSitesManager(cyProperty);

		final UpdateManager updateManager = new UpdateManager(appManager, downloadSitesManager);
		registerService(bc, updateManager, AppsFinishedStartingListener.class);

		AppManagerMediator appManagerMediator = new AppManagerMediator(appManager, downloadSitesManager, updateManager, serviceRegistrar);
		registerService(bc, appManagerMediator, CyShutdownListener.class);

		final AppConflictHandlerFactory appConflictHandlerFactory = new AppConflictHandlerFactory();
		registerService(bc,appConflictHandlerFactory,GUITunableHandlerFactory.class);
		
		{
				UpdateNotificationAction action = new UpdateNotificationAction(appManager, updateManager,
						appManagerMediator, serviceRegistrar);
				registerService(bc, action, CyAction.class);
			}

		// Task Factories
		{
			AppStoreTaskFactory factory = new AppStoreTaskFactory(appManager, serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "apps");
			props.setProperty(COMMAND, "open appstore");
			props.setProperty(COMMAND_DESCRIPTION, "Open the app store page for an app");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Open the app store page for an app.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			AppManagerTaskFactory factory = new AppManagerTaskFactory(appManager, serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(PREFERRED_MENU, "Apps");
			props.setProperty(TITLE, "App Manager");
			props.setProperty(MENU_GRAVITY, "1.0");
			props.setProperty(IN_MENU_BAR, "true");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			ManagerInstallAppsFromFileTaskFactory factory = new ManagerInstallAppsFromFileTaskFactory(appManager, taskManager, serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(PREFERRED_MENU, "Apps");
			props.setProperty(TITLE, "Install Apps From File");
			props.setProperty(MENU_GRAVITY, "1.1");
			props.setProperty(IN_MENU_BAR, "true");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			DisableTaskFactory factory = new DisableTaskFactory(appManager);
			Properties props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "apps");
			props.setProperty(COMMAND, "disable");
			props.setProperty(COMMAND_DESCRIPTION, "Disable an app");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Disable a currently installed app.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"appName\": \"appname\"}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			EnableTaskFactory factory = new EnableTaskFactory(appManager);
			Properties props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "apps");
			props.setProperty(COMMAND, "enable");
			props.setProperty(COMMAND_DESCRIPTION, "Enable a disabled app");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Enable a currently disabled app.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"appName\": \"appname\"}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			InformationTaskFactory factory = new InformationTaskFactory(appManager);
			Properties props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "apps");
			props.setProperty(COMMAND, "information");
			props.setProperty(COMMAND_DESCRIPTION, "Get app information");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Get information about an app.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON,
					"{\"appName\": \"appname\""+
                    ", \"description\": \"App description\""+
                    ", \"version\": \"1.2.2\"}"
			);
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			InstallTaskFactory factory = new InstallTaskFactory(appManager);
			Properties props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "apps");
			props.setProperty(COMMAND, "install");
			props.setProperty(COMMAND_DESCRIPTION, "Install an app");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Install an app given an app name or file.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			ListAvailableTaskFactory factory = new ListAvailableTaskFactory(appManager);
			Properties props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "apps");
			props.setProperty(COMMAND, "list available");
			props.setProperty(COMMAND_DESCRIPTION, "List the available apps");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Return a list of the available apps in the app store");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON,
				    "[{\"appName\":\"name\", "+
				    "\"description\":\"descriptions\", "+
				    "\"details\":\"app details\"}]"
			);
			registerService(bc, factory, TaskFactory.class, props);
		}

		final String DESCRIPTION_WARNING = "\nThe `description` field will be null if the App store has not been accessed from Cytoscape.";

		{
			ListAppsTaskFactory factory = new ListAppsTaskFactory(appManager, AppStatus.DISABLED);
			Properties props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "apps");
			props.setProperty(COMMAND, "list disabled");
			props.setProperty(COMMAND_DESCRIPTION, "List the disabled apps");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
					"Return a list of the disabled apps in the current installation." + DESCRIPTION_WARNING);
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON,
				    "[{ \"appName\": \"appname\","+
				    "\"version\": \"1.1.0\","+
				    "\"description\": \"descriptions\","+
				    "\"status\": \"Disabled\"}]"
			);
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			ListAppsTaskFactory factory = new ListAppsTaskFactory(appManager, AppStatus.INSTALLED);
			Properties props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "apps");
			props.setProperty(COMMAND, "list installed");
			props.setProperty(COMMAND_DESCRIPTION, "List the installed apps");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
					"Return a list of the installed apps in the current installation." + DESCRIPTION_WARNING);
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON,
				    "[{\"appName\": \"appname\","+
				    "\"version\": \"1.1.0\","+
				    "\"description\": \"descriptions\","+
				    "\"status\": \"Installed\"}]"
			);
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			ListAppsTaskFactory factory = new ListAppsTaskFactory(appManager, AppStatus.UNINSTALLED);
			Properties props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "apps");
			props.setProperty(COMMAND, "list uninstalled");
			props.setProperty(COMMAND_DESCRIPTION, "List the uninstalled apps");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
					"Return a list of the uninstalled apps in the current installation." + DESCRIPTION_WARNING);
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON,
				    "[{ \"appName\": \"appname\","+
				    "\"version\": \"1.1.0\","+
				    "\"description\": \"descriptions\","+
				    "\"status\": \"Uninstalled\"}]"
			);
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			ListUpdatesTaskFactory factory = new ListUpdatesTaskFactory(appManager);
			Properties props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "apps");
			props.setProperty(COMMAND, "list updates");
			props.setProperty(COMMAND_DESCRIPTION, "List the apps available for updates");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
					"Return a list of the apps that have updates in the app store.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON,
				    "[{ \"appName\": \"appname\","+
				    "\"version\": \"1.1.10\","+
				    "\"information\": \"app information\"}]"
			);
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			StatusTaskFactory factory = new StatusTaskFactory(appManager);
			Properties props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "apps");
			props.setProperty(COMMAND, "status");
			props.setProperty(COMMAND_DESCRIPTION, "Get the status of an app");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Get the status of an app.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"appName\": \"appname\", \"status\": \"Installed\"}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			UninstallTaskFactory factory = new UninstallTaskFactory(appManager);
			Properties props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "apps");
			props.setProperty(COMMAND, "uninstall");
			props.setProperty(COMMAND_DESCRIPTION, "Uninstall an app");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Uninstall a currently installed app.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{\"appName\": \"appname\"}");
			registerService(bc, factory, TaskFactory.class, props);
		}
		{
			UpdateTaskFactory factory = new UpdateTaskFactory(appManager, updateManager);
			Properties props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "apps");
			props.setProperty(COMMAND, "update");
			props.setProperty(COMMAND_DESCRIPTION, "Update an app or all apps");
			props.setProperty(COMMAND_LONG_DESCRIPTION, "Update an app or all apps.");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{}");
			registerService(bc, factory, TaskFactory.class, props);
		}
	}

	private class YFilesChecker implements BundleListener, AppsFinishedStartingListener {

		private static final String YFILES_TAG = "yfiles";

		private final AppManager manager;
		private final CyServiceRegistrar serviceRegistrar;
		private final String[] yFilesLayouts = new String[] {
				"Circular Layout", "Hierarchic Layout", "Hierarchic Layout Selected Nodes",
				"Organic Layout", "Orthogonal Layout", "Radial Layout", "Tree Layout",
				"Orthogonal Edge Router", "Organic Edge Router"
		};
		private final HashMap<String, CyAction> actionMap = new HashMap<>();

		YFilesChecker(AppManager manager, CyServiceRegistrar serviceRegistrar, OpenBrowser openBrowser) {
			this.manager = manager;
			this.serviceRegistrar = serviceRegistrar;
		}

		@Override
		public void handleEvent(AppsFinishedStartingEvent event) {
			final Set<App> installed = manager.getInstalledApps();
			final Set<App> filtered = installed.stream()
					.filter(app -> app.getAppName().toLowerCase().contains(YFILES_TAG)).collect(Collectors.toSet());

			if (filtered.isEmpty()) {
				for (CyAction action : actionMap.values()) {
					serviceRegistrar.registerService(action, CyAction.class, new Properties());
				}
			}
		}

		@Override
		public void bundleChanged(BundleEvent bundleEvent) {
			final String bundleName = bundleEvent.getBundle().getSymbolicName();

			if (bundleName.toLowerCase().contains(YFILES_TAG) && bundleEvent.getType() == BundleEvent.STARTED) {
				for (CyAction action : actionMap.values()) {
					serviceRegistrar.unregisterAllServices(action);
				}
			}
		}
	}
}
