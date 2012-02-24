package org.cytoscape.app.internal;

import org.cytoscape.app.internal.CyAppAdapterImpl;
import org.cytoscape.app.internal.AppLoaderTaskFactory;
import org.cytoscape.app.internal.StartupMostlyFinished;
import org.cytoscape.app.internal.action.AppManagerAction;
import org.cytoscape.application.CyVersion;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.io.read.CyTableReaderManager;
import org.cytoscape.io.read.CyPropertyReaderManager;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.datasource.DataSourceManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.write.PresentationWriterManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.work.TaskManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.io.write.CyPropertyWriterManager;
import org.cytoscape.io.write.CySessionWriterManager;
import org.cytoscape.io.write.CyNetworkViewWriterManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.undo.UndoSupport;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.work.swing.SubmenuTaskManager;
import org.cytoscape.work.swing.PanelTaskManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.io.read.CyNetworkReaderManager;


import org.cytoscape.application.swing.CyAction;
import org.cytoscape.work.TaskFactory;


import org.osgi.framework.BundleContext;

import org.cytoscape.service.util.AbstractCyActivator;

import java.util.Properties;



public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {

		CyApplicationManager cyApplicationManagerRef = getService(bc,CyApplicationManager.class);
		CyEventHelper cyEventHelperRef = getService(bc,CyEventHelper.class);
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
		PanelTaskManager panelTaskManagerRef = getService(bc,PanelTaskManager.class);
		DialogTaskManager dialogTaskManagerRef = getService(bc,DialogTaskManager.class);
		SubmenuTaskManager submenuTaskManagerRef = getService(bc,SubmenuTaskManager.class);
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
		
		CyAppAdapterImpl cyAppAdapter = new CyAppAdapterImpl(cyApplicationManagerRef,cyEventHelperRef,cyLayoutAlgorithmManagerRef,cyNetworkFactoryRef,cyNetworkManagerRef,cyNetworkViewFactoryRef,cyNetworkViewManagerRef,cyNetworkViewReaderManagerRef,cyNetworkViewWriterManagerRef,cyPropertyRef,cyPropertyReaderManagerRef,cyPropertyWriterManagerRef,cyRootNetworkFactoryRef,cyServiceRegistrarRef,cySessionManagerRef,cySessionReaderManagerRef,cySessionWriterManagerRef,cySwingApplicationRef,cyTableFactoryRef,cyTableManagerRef,cyTableReaderManagerRef,cytoscapeVersionService, dialogTaskManagerRef,panelTaskManagerRef,submenuTaskManagerRef,presentationWriterManagerRef,renderingEngineManagerRef,taskManagerRef,undoSupportRef, vmfFactoryC, vmfFactoryD, vmfFactoryP, visualMappingManagerRef,visualStyleFactoryRef, dataSourceManager);
		AppLoaderTaskFactory appLoaderTaskFactory = new AppLoaderTaskFactory(cyAppAdapter);
		AppManagerAction appManagerAction = new AppManagerAction(
				cySwingApplicationRef,
				cytoscapeVersionService,
				bookmarkServiceRef,
				bookmarksUtilServiceRef,
				dialogTaskManagerRef,
				cyPropertyRef,
				cyAppAdapter,
				appLoaderTaskFactory,
				cyApplicationConfigurationServiceRef);
		StartupMostlyFinished startupMostlyFinished = new StartupMostlyFinished(cyEventHelperRef);
		
		registerService(bc,appLoaderTaskFactory,TaskFactory.class, new Properties());
		registerAllServices(bc,appManagerAction, new Properties());
	}
}

