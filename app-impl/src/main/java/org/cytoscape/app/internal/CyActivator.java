package org.cytoscape.app.internal;

/*
 * #%L
 * Cytoscape App Impl (app-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
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

import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.app.internal.action.AppManagerAction;
import org.cytoscape.app.internal.action.CitationsAction;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.app.internal.manager.StartupMonitor;
import org.cytoscape.app.internal.net.WebQuerier;
import org.cytoscape.app.internal.net.server.*;
import org.cytoscape.app.internal.tunable.AppInstallationConflictHandlerFactory;
import org.cytoscape.app.internal.ui.downloadsites.DownloadSitesManager;
import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyVersion;
import org.cytoscape.application.events.CyStartEvent;
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
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.io.write.*;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.task.create.*;
import org.cytoscape.task.destroy.*;
import org.cytoscape.task.edit.*;
import org.cytoscape.task.hide.*;
import org.cytoscape.task.read.LoadNetworkFileTaskFactory;
import org.cytoscape.task.read.LoadNetworkURLTaskFactory;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.task.select.*;
import org.cytoscape.task.visualize.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.task.visualize.ApplyVisualStyleTaskFactory;
import org.cytoscape.task.write.*;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.GUITunableHandlerFactory;
import org.cytoscape.work.swing.PanelTaskManager;
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.startlevel.StartLevel;

import java.net.URL;
import java.util.Map.Entry;
import java.util.Properties;

public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		CyApplicationConfiguration cyApplicationConfigurationRef = getService(bc, CyApplicationConfiguration.class);
		CyApplicationManager cyApplicationManagerRef = getService(bc,CyApplicationManager.class);
		CyEventHelper cyEventHelperRef = getService(bc,CyEventHelper.class);
		CyGroupAggregationManager cyGroupAggregationManagerRef = getService(bc,CyGroupAggregationManager.class);
		CyGroupFactory cyGroupFactoryRef = getService(bc,CyGroupFactory.class);
		CyGroupManager cyGroupManagerRef = getService(bc,CyGroupManager.class);
		CyLayoutAlgorithmManager cyLayoutAlgorithmManagerRef = getService(bc,CyLayoutAlgorithmManager.class);
		CyNetworkFactory cyNetworkFactoryRef = getService(bc,CyNetworkFactory.class);
		CyNetworkManager cyNetworkManagerRef = getService(bc,CyNetworkManager.class);
		CyNetworkViewFactory cyNetworkViewFactoryRef = getService(bc,CyNetworkViewFactory.class);
		CyNetworkViewManager cyNetworkViewManagerRef = getService(bc,CyNetworkViewManager.class);
		CyNetworkReaderManager cyNetworkViewReaderManagerRef = getService(bc,CyNetworkReaderManager.class);
		CyNetworkViewWriterManager cyNetworkViewWriterManagerRef = getService(bc,CyNetworkViewWriterManager.class);
		CyProperty<Properties> cyPropertyRef = getService(bc,CyProperty.class,"(cyPropertyName=cytoscape3.props)");
		CyPropertyReaderManager cyPropertyReaderManagerRef = getService(bc,CyPropertyReaderManager.class);
		CyPropertyWriterManager cyPropertyWriterManagerRef = getService(bc,CyPropertyWriterManager.class);
		CyRootNetworkManager cyRootNetworkFactoryRef = getService(bc,CyRootNetworkManager.class);
		CyServiceRegistrar cyServiceRegistrarRef = getService(bc,CyServiceRegistrar.class);
		CySessionManager cySessionManagerRef = getService(bc,CySessionManager.class);
		CySessionReaderManager cySessionReaderManagerRef = getService(bc,CySessionReaderManager.class);
		CySessionWriterManager cySessionWriterManagerRef = getService(bc,CySessionWriterManager.class);
		CySwingApplication cySwingApplicationRef = getService(bc,CySwingApplication.class);
		CyTableFactory cyTableFactoryRef = getService(bc,CyTableFactory.class);
		CyTableManager cyTableManagerRef = getService(bc,CyTableManager.class);
		CyTableReaderManager cyTableReaderManagerRef = getService(bc,CyTableReaderManager.class);
		CyTableWriterManager cyTableWriterManagerRef = getService(bc,CyTableWriterManager.class);
		PanelTaskManager panelTaskManagerRef = getService(bc,PanelTaskManager.class);
		DialogTaskManager dialogTaskManagerRef = getService(bc,DialogTaskManager.class);
		PresentationWriterManager presentationWriterManagerRef = getService(bc,PresentationWriterManager.class);
		RenderingEngineManager renderingEngineManagerRef = getService(bc,RenderingEngineManager.class);
		TaskManager taskManagerRef = getService(bc,TaskManager.class);
		UndoSupport undoSupportRef = getService(bc,UndoSupport.class);
		VisualMappingManager visualMappingManagerRef = getService(bc,VisualMappingManager.class);
		VisualStyleFactory visualStyleFactoryRef = getService(bc,VisualStyleFactory.class);
		CyVersion cytoscapeVersionService = getService(bc,CyVersion.class);
		CyProperty<Bookmarks> bookmarkServiceRef = getService(bc,CyProperty.class,"(cyPropertyName=bookmarks)");
		BookmarksUtil bookmarksUtilServiceRef = getService(bc,BookmarksUtil.class);
		CyApplicationConfiguration cyApplicationConfigurationServiceRef = getService(bc,CyApplicationConfiguration.class);
		
		VisualMappingFunctionFactory vmfFactoryC = getService(bc,VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
		VisualMappingFunctionFactory vmfFactoryD = getService(bc,VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
		VisualMappingFunctionFactory vmfFactoryP = getService(bc,VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");

		DataSourceManager dataSourceManager = getService(bc, DataSourceManager.class);
		
		// Start of core-task services
		
		 LoadVizmapFileTaskFactory loadVizmapFileTaskFactory = getService(bc,LoadVizmapFileTaskFactory.class);
		 LoadNetworkFileTaskFactory loadNetworkFileTaskFactory = getService(bc,LoadNetworkFileTaskFactory.class);
		 LoadNetworkURLTaskFactory loadNetworkURLTaskFactory = getService(bc,LoadNetworkURLTaskFactory.class);
		 DeleteSelectedNodesAndEdgesTaskFactory deleteSelectedNodesAndEdgesTaskFactory = getService(bc,DeleteSelectedNodesAndEdgesTaskFactory.class);
		 SelectAllTaskFactory selectAllTaskFactory = getService(bc,SelectAllTaskFactory.class);

		 SelectAllEdgesTaskFactory selectAllEdgesTaskFactory = getService(bc,SelectAllEdgesTaskFactory.class);
		 SelectAllNodesTaskFactory selectAllNodesTaskFactory = getService(bc,SelectAllNodesTaskFactory.class);
		 SelectAdjacentEdgesTaskFactory selectAdjacentEdgesTaskFactory = getService(bc,SelectAdjacentEdgesTaskFactory.class);
		 SelectConnectedNodesTaskFactory selectConnectedNodesTaskFactory = getService(bc,SelectConnectedNodesTaskFactory.class);
		
		 SelectFirstNeighborsTaskFactory selectFirstNeighborsTaskFactory = getService(bc,SelectFirstNeighborsTaskFactory.class,"(title=Undirected)");
		 SelectFirstNeighborsTaskFactory selectFirstNeighborsTaskFactoryInEdge = getService(bc,SelectFirstNeighborsTaskFactory.class,"(title=Directed: Incoming)");
		 SelectFirstNeighborsTaskFactory selectFirstNeighborsTaskFactoryOutEdge = getService(bc,SelectFirstNeighborsTaskFactory.class,"(title=Directed: Outgoing)");
		
		 DeselectAllTaskFactory deselectAllTaskFactory = getService(bc,DeselectAllTaskFactory.class);
		 DeselectAllEdgesTaskFactory deselectAllEdgesTaskFactory = getService(bc,DeselectAllEdgesTaskFactory.class);
		 DeselectAllNodesTaskFactory deselectAllNodesTaskFactory = getService(bc,DeselectAllNodesTaskFactory.class);
		 InvertSelectedEdgesTaskFactory invertSelectedEdgesTaskFactory = getService(bc,InvertSelectedEdgesTaskFactory.class);
		 InvertSelectedNodesTaskFactory invertSelectedNodesTaskFactory = getService(bc,InvertSelectedNodesTaskFactory.class);
		 SelectFromFileListTaskFactory selectFromFileListTaskFactory = getService(bc,SelectFromFileListTaskFactory.class);
		
		 SelectFirstNeighborsNodeViewTaskFactory selectFirstNeighborsNodeViewTaskFactory = getService(bc,SelectFirstNeighborsNodeViewTaskFactory.class);
		
		 HideSelectedTaskFactory hideSelectedTaskFactory = getService(bc,HideSelectedTaskFactory.class);
		 HideSelectedNodesTaskFactory hideSelectedNodesTaskFactory = getService(bc,HideSelectedNodesTaskFactory.class);
		 HideSelectedEdgesTaskFactory hideSelectedEdgesTaskFactory = getService(bc,HideSelectedEdgesTaskFactory.class);
		 UnHideAllTaskFactory unHideAllTaskFactory = getService(bc,UnHideAllTaskFactory.class);
		 UnHideAllNodesTaskFactory unHideAllNodesTaskFactory = getService(bc,UnHideAllNodesTaskFactory.class);
		 UnHideAllEdgesTaskFactory unHideAllEdgesTaskFactory = getService(bc,UnHideAllEdgesTaskFactory.class);

		 NewEmptyNetworkViewFactory newEmptyNetworkTaskFactory = getService(bc,NewEmptyNetworkViewFactory.class);

		 CloneNetworkTaskFactory cloneNetworkTaskFactory = getService(bc,CloneNetworkTaskFactory.class);
		 NewNetworkSelectedNodesAndEdgesTaskFactory newNetworkSelectedNodesEdgesTaskFactory = getService(bc,NewNetworkSelectedNodesAndEdgesTaskFactory.class);
		 NewNetworkSelectedNodesOnlyTaskFactory newNetworkSelectedNodesOnlyTaskFactory = getService(bc,NewNetworkSelectedNodesOnlyTaskFactory.class);
		 DestroyNetworkTaskFactory destroyNetworkTaskFactory = getService(bc,DestroyNetworkTaskFactory.class);
		 DestroyNetworkViewTaskFactory destroyNetworkViewTaskFactory = getService(bc,DestroyNetworkViewTaskFactory.class);

		 NewSessionTaskFactory newSessionTaskFactory = getService(bc,NewSessionTaskFactory.class);
		 OpenSessionTaskFactory openSessionTaskFactory = getService(bc,OpenSessionTaskFactory.class);
		 SaveSessionAsTaskFactory saveSessionAsTaskFactory = getService(bc,SaveSessionAsTaskFactory.class);
		 EditNetworkTitleTaskFactory editNetworkTitleTaskFactory = getService(bc,EditNetworkTitleTaskFactory.class);
		 CreateNetworkViewTaskFactory createNetworkViewTaskFactory = getService(bc,CreateNetworkViewTaskFactory.class);
		 ExportNetworkImageTaskFactory exportNetworkImageTaskFactory = getService(bc,ExportNetworkImageTaskFactory.class);
		 ExportNetworkViewTaskFactory exportNetworkViewTaskFactory = getService(bc,ExportNetworkViewTaskFactory.class);
		 ExportSelectedTableTaskFactory exportSelectedTableTaskFactory = getService(bc,ExportSelectedTableTaskFactory.class);
		 ExportTableTaskFactory exportTableTaskFactory = getService(bc,ExportTableTaskFactory.class);
		 ApplyPreferredLayoutTaskFactory applyPreferredLayoutTaskFactory = getService(bc,ApplyPreferredLayoutTaskFactory.class);
		 DeleteColumnTaskFactory deleteColumnTaskFactory = getService(bc,DeleteColumnTaskFactory.class);
		 RenameColumnTaskFactory renameColumnTaskFactory = getService(bc,RenameColumnTaskFactory.class);
		 DeleteTableTaskFactory deleteTableTaskFactory = getService(bc,DeleteTableTaskFactory.class);
		 ExportVizmapTaskFactory exportVizmapTaskFactory = getService(bc,ExportVizmapTaskFactory.class);
		
		 ConnectSelectedNodesTaskFactory connectSelectedNodesTaskFactory = getService(bc,ConnectSelectedNodesTaskFactory.class);
		 MapGlobalToLocalTableTaskFactory mapGlobal = getService(bc,MapGlobalToLocalTableTaskFactory.class);
		 ApplyVisualStyleTaskFactory applyVisualStyleTaskFactory = getService(bc,ApplyVisualStyleTaskFactory.class);
		 MapTableToNetworkTablesTaskFactory mapNetworkAttrTaskFactory = getService(bc,MapTableToNetworkTablesTaskFactory.class);

    	 GroupNodesTaskFactory groupNodesTaskFactory = getService(bc,GroupNodesTaskFactory.class);
    	 UnGroupTaskFactory unGroupTaskFactory= getService(bc,UnGroupTaskFactory.class);
    	 CollapseGroupTaskFactory collapseGroupTaskFactory= getService(bc,CollapseGroupTaskFactory.class);
    	 ExpandGroupTaskFactory expandGroupTaskFactory= getService(bc,ExpandGroupTaskFactory.class);
    	 UnGroupNodesTaskFactory unGroupNodesTaskFactory= getService(bc,UnGroupNodesTaskFactory.class);

		// End of core-task services


		// Command execution services
		CommandExecutorTaskFactory cyCommandExecutorTaskFactory = getService(bc,CommandExecutorTaskFactory.class);
		AvailableCommands availableCommands = getService(bc,AvailableCommands.class);
		 
    	 StreamUtil streamUtilServiceRef = getService(bc, StreamUtil.class);
    	 FileUtil fileUtilServiceRef = getService(bc, FileUtil.class);
    	 OpenBrowser openBrowser = getService(bc, OpenBrowser.class);

    	 CySwingAppAdapter cyAppAdapter = new CyAppAdapterImpl(
				 cyApplicationConfigurationRef,
				 cyApplicationManagerRef,
                 cyEventHelperRef,
                 cyGroupAggregationManagerRef, 
                 cyGroupFactoryRef, 
                 cyGroupManagerRef,
                 cyLayoutAlgorithmManagerRef,
                 cyNetworkFactoryRef,
                 cyNetworkManagerRef,
                 cyNetworkViewFactoryRef,
                 cyNetworkViewManagerRef,
                 cyNetworkViewReaderManagerRef,
                 cyNetworkViewWriterManagerRef,
                 cyPropertyRef,
                 cyPropertyReaderManagerRef,
                 cyPropertyWriterManagerRef,
                 cyRootNetworkFactoryRef,
                 cyServiceRegistrarRef,
                 cySessionManagerRef,
                 cySessionReaderManagerRef,
                 cySessionWriterManagerRef,
                 cySwingApplicationRef,
                 cyTableFactoryRef,
                 cyTableManagerRef,
                 cyTableReaderManagerRef,
                 cyTableWriterManagerRef,
                 cytoscapeVersionService, 
                 dialogTaskManagerRef,
                 panelTaskManagerRef,
                 presentationWriterManagerRef,
                 renderingEngineManagerRef,
                 taskManagerRef,
                 undoSupportRef, 
                 vmfFactoryC, 
                 vmfFactoryD, 
                 vmfFactoryP, 
                 visualMappingManagerRef,
                 visualStyleFactoryRef, 
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
		
		registerService(bc,cyAppAdapter,CyAppAdapter.class, new Properties());
		registerService(bc,cyAppAdapter,CySwingAppAdapter.class, new Properties());
		
		WebQuerier webQuerier = new WebQuerier(streamUtilServiceRef, cytoscapeVersionService);
		registerService(bc, webQuerier, WebQuerier.class, new Properties());
		
		Properties properties = System.getProperties();

		for (Entry<Object, Object> entry : properties.entrySet()) {
			//System.out.println("Entry: " + entry.getKey() + ", value: " + entry.getValue());
		}
		
		StartLevel startLevel = getService(bc, StartLevel.class);
		PackageAdmin packageAdmin = getService(bc, PackageAdmin.class);
		StartupMonitor startupMonitor = new StartupMonitor(bc, packageAdmin, cyEventHelperRef);
		bc.addBundleListener(startupMonitor);

		URL welcomeJarUrl = bc.getBundle().getResource("welcome-screen-jar/welcome-impl-default.jar");

		// Instantiate new manager
		final AppManager appManager = new AppManager(cyAppAdapter, 
				cyApplicationConfigurationServiceRef, webQuerier, startLevel, startupMonitor, welcomeJarUrl);
		registerService(bc, appManager, AppManager.class, new Properties());
		bc.addFrameworkListener(appManager);
		
		final DownloadSitesManager downloadSitesManager = new DownloadSitesManager(cyPropertyRef);
		
		final AppInstallationConflictHandlerFactory appInstallationConflictHandlerFactory = new AppInstallationConflictHandlerFactory();
		registerService(bc,appInstallationConflictHandlerFactory,GUITunableHandlerFactory.class, new Properties());
		
		// AbstractCyAction implementation for updated app manager
		AppManagerAction appManagerAction = new AppManagerAction(
				appManager, downloadSitesManager,
				cySwingApplicationRef, fileUtilServiceRef,
				dialogTaskManagerRef, cyServiceRegistrarRef);
		registerService(bc, appManagerAction, CyAction.class, new Properties());

		// Show citations dialog
		final CitationsAction citationsAction = new CitationsAction(webQuerier, appManager, dialogTaskManagerRef, cySwingApplicationRef, openBrowser);
		registerService(bc, citationsAction, CyAction.class, new Properties());
		
		// Start local server that reports app installation status to the app store when requested,
		// also able to install an app when told by the app store
        final AppGetResponder appGetResponder = new AppGetResponder(appManager, cytoscapeVersionService);
        final CyHttpd httpd = (new CyHttpdFactoryImpl()).createHttpd(new LocalhostServerSocketFactory(2607));
        httpd.addBeforeResponse(new ScreenOriginsBeforeResponse(WebQuerier.DEFAULT_APP_STORE_URL));
        httpd.addBeforeResponse(new OriginOptionsBeforeResponse("x-csrftoken"));
        httpd.addAfterResponse(new AddAllowOriginHeader());
        httpd.addResponder(appGetResponder.new StatusResponder());
        httpd.addResponder(appGetResponder.new InstallResponder());
        httpd.start();

        Object o = cyPropertyRef;
//		cyPropertyRef.getProperties().put("testkey1", "testval1");
//		cyPropertyRef.getProperties().setProperty("testkey2", "testval2");
		
		// Fire event "start up mostly finished". This seems to close the Cytoscape splash screen and show the actual UI.
		cyEventHelperRef.fireEvent(new CyStartEvent(this));
		
		/*
		for (DownloadSite site : WebQuerier.DEFAULT_DOWNLOAD_SITES) {
			System.out.println(site.getSiteName());
		}
		*/
	}
}

