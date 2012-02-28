
package org.cytoscape.task.internal;

import java.util.Properties;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.datasource.DataSourceManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.io.read.CyTableReaderManager;
import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.io.write.CyNetworkViewWriterManager;
import org.cytoscape.io.write.CySessionWriterManager;
import org.cytoscape.io.write.CyTableWriterManager;
import org.cytoscape.io.write.PresentationWriterManager;
import org.cytoscape.io.write.VizmapWriterManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.task.NetworkCollectionTaskFactory;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewCollectionTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.task.TableCellTaskFactory;
import org.cytoscape.task.TableColumnTaskFactory;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.task.creation.LoadVisualStyles;
import org.cytoscape.task.creation.NewEmptyNetworkViewFactory;
import org.cytoscape.task.internal.creation.CloneNetworkTaskFactory;
import org.cytoscape.task.internal.creation.CreateNetworkViewTaskFactory;
import org.cytoscape.task.internal.creation.NewEmptyNetworkTaskFactory;
import org.cytoscape.task.internal.creation.NewNetworkSelectedNodesEdgesTaskFactory;
import org.cytoscape.task.internal.creation.NewNetworkSelectedNodesOnlyTaskFactory;
import org.cytoscape.task.internal.destruction.DestroyNetworkTaskFactory;
import org.cytoscape.task.internal.destruction.DestroyNetworkViewTaskFactory;
import org.cytoscape.task.internal.edit.ConnectSelectedNodesTaskFactory;
import org.cytoscape.task.internal.export.graphics.ExportNetworkImageTaskFactory;
import org.cytoscape.task.internal.export.network.ExportNetworkViewTaskFactory;
import org.cytoscape.task.internal.export.table.ExportCurrentTableTaskFactory;
import org.cytoscape.task.internal.export.vizmap.ExportVizmapTaskFactory;
import org.cytoscape.task.internal.group.GroupNodeContextTaskFactory;
import org.cytoscape.task.internal.group.GroupNodesTaskFactory;
import org.cytoscape.task.internal.hide.HideSelectedEdgesTaskFactory;
import org.cytoscape.task.internal.hide.HideSelectedNodesTaskFactory;
import org.cytoscape.task.internal.hide.HideSelectedTaskFactory;
import org.cytoscape.task.internal.hide.UnHideAllEdgesTaskFactory;
import org.cytoscape.task.internal.hide.UnHideAllNodesTaskFactory;
import org.cytoscape.task.internal.hide.UnHideAllTaskFactory;
import org.cytoscape.task.internal.layout.ApplyPreferredLayoutTaskFactory;
import org.cytoscape.task.internal.loaddatatable.LoadAttributesFileTaskFactoryImpl;
import org.cytoscape.task.internal.loaddatatable.LoadAttributesURLTaskFactoryImpl;
import org.cytoscape.task.internal.loadnetwork.LoadNetworkFileTaskFactoryImpl;
import org.cytoscape.task.internal.loadnetwork.LoadNetworkURLTaskFactoryImpl;
import org.cytoscape.task.internal.loadvizmap.LoadVizmapFileTaskFactoryImpl;
import org.cytoscape.task.internal.networkobjects.DeleteSelectedNodesAndEdgesTaskFactory;
import org.cytoscape.task.internal.proxysettings.ProxySettingsTaskFactory;
import org.cytoscape.task.internal.quickstart.ImportTaskUtil;
import org.cytoscape.task.internal.quickstart.datasource.BioGridPreprocessor;
import org.cytoscape.task.internal.quickstart.datasource.InteractionFilePreprocessor;
import org.cytoscape.task.internal.quickstart.subnetworkbuilder.SubnetworkBuilderUtil;
import org.cytoscape.task.internal.select.DeselectAllEdgesTaskFactory;
import org.cytoscape.task.internal.select.DeselectAllNodesTaskFactory;
import org.cytoscape.task.internal.select.DeselectAllTaskFactory;
import org.cytoscape.task.internal.select.InvertSelectedEdgesTaskFactory;
import org.cytoscape.task.internal.select.InvertSelectedNodesTaskFactory;
import org.cytoscape.task.internal.select.SelectAdjacentEdgesTaskFactory;
import org.cytoscape.task.internal.select.SelectAllEdgesTaskFactory;
import org.cytoscape.task.internal.select.SelectAllNodesTaskFactory;
import org.cytoscape.task.internal.select.SelectAllTaskFactory;
import org.cytoscape.task.internal.select.SelectConnectedNodesTaskFactory;
import org.cytoscape.task.internal.select.SelectFirstNeighborsNodeViewTaskFactory;
import org.cytoscape.task.internal.select.SelectFirstNeighborsTaskFactory;
import org.cytoscape.task.internal.select.SelectFromFileListTaskFactory;
import org.cytoscape.task.internal.session.NewSessionTaskFactory;
import org.cytoscape.task.internal.session.OpenSessionTaskFactory;
import org.cytoscape.task.internal.session.SaveSessionAsTaskFactory;
import org.cytoscape.task.internal.session.SaveSessionTaskFactory;
import org.cytoscape.task.internal.setcurrent.SetCurrentNetworkTaskFactoryImpl;
import org.cytoscape.task.internal.table.CopyValueToEntireColumnTaskFactory;
import org.cytoscape.task.internal.table.DeleteColumnTaskFactory;
import org.cytoscape.task.internal.table.DeleteTableTaskFactory;
import org.cytoscape.task.internal.table.RenameColumnTaskFactory;
import org.cytoscape.task.internal.title.EditNetworkTitleTaskFactory;
import org.cytoscape.task.internal.zoom.FitContentTaskFactory;
import org.cytoscape.task.internal.zoom.FitSelectedTaskFactory;
import org.cytoscape.task.internal.zoom.ZoomInTaskFactory;
import org.cytoscape.task.internal.zoom.ZoomOutTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;


public class CyActivator extends AbstractCyActivator {
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		DataSourceManager dataSourceManagerServiceRef = getService(bc,DataSourceManager.class);
		
		OpenBrowser openBrowserServiceRef = getService(bc,OpenBrowser.class);
		CyEventHelper cyEventHelperRef = getService(bc,CyEventHelper.class);
		CyApplicationConfiguration cyApplicationConfigurationServiceRef = getService(bc,CyApplicationConfiguration.class);
		RecentlyOpenedTracker recentlyOpenedTrackerServiceRef = getService(bc,RecentlyOpenedTracker.class);
		CyNetworkNaming cyNetworkNamingServiceRef = getService(bc,CyNetworkNaming.class);
		UndoSupport undoSupportServiceRef = getService(bc,UndoSupport.class);
		CyNetworkViewFactory cyNetworkViewFactoryServiceRef = getService(bc,CyNetworkViewFactory.class);
		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc,CyNetworkFactory.class);
		CyRootNetworkManager cyRootNetworkFactoryServiceRef = getService(bc,CyRootNetworkManager.class);
		CyNetworkReaderManager cyNetworkReaderManagerServiceRef = getService(bc,CyNetworkReaderManager.class);
		CyTableReaderManager cyDataTableReaderManagerServiceRef = getService(bc,CyTableReaderManager.class);
		VizmapReaderManager vizmapReaderManagerServiceRef = getService(bc,VizmapReaderManager.class);
		VisualMappingManager visualMappingManagerServiceRef = getService(bc,VisualMappingManager.class);
		VisualStyleFactory visualStyleFactoryServiceRef = getService(bc,VisualStyleFactory.class);
		StreamUtil streamUtilRef = getService(bc,StreamUtil.class);
		TaskManager<?,?> taskManagerServiceRef = getService(bc,TaskManager.class);
		PresentationWriterManager viewWriterManagerServiceRef = getService(bc,PresentationWriterManager.class);
		CyNetworkViewWriterManager networkViewWriterManagerServiceRef = getService(bc,CyNetworkViewWriterManager.class);
		VizmapWriterManager vizmapWriterManagerServiceRef = getService(bc,VizmapWriterManager.class);
		CySessionWriterManager sessionWriterManagerServiceRef = getService(bc,CySessionWriterManager.class);
		CySessionReaderManager sessionReaderManagerServiceRef = getService(bc,CySessionReaderManager.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc,CyNetworkManager.class);
		CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc,CyNetworkViewManager.class);
		CyApplicationManager cyApplicationManagerServiceRef = getService(bc,CyApplicationManager.class);
		CySessionManager cySessionManagerServiceRef = getService(bc,CySessionManager.class);
		CyProperty cyPropertyServiceRef = getService(bc,CyProperty.class,"(cyPropertyName=cytoscape3.props)");
		CyTableManager cyTableManagerServiceRef = getService(bc,CyTableManager.class);
		RenderingEngineManager renderingEngineManagerServiceRef = getService(bc,RenderingEngineManager.class);
		CyLayoutAlgorithmManager cyLayoutsServiceRef = getService(bc,CyLayoutAlgorithmManager.class);
		CyTableWriterManager cyTableWriterManagerRef = getService(bc,CyTableWriterManager.class);
		SynchronousTaskManager<?> synchronousTaskManagerServiceRef = getService(bc,SynchronousTaskManager.class);
		
		LoadAttributesFileTaskFactoryImpl loadAttrsFileTaskFactory = new LoadAttributesFileTaskFactoryImpl(cyDataTableReaderManagerServiceRef);
		LoadAttributesURLTaskFactoryImpl loadAttrsURLTaskFactory = new LoadAttributesURLTaskFactoryImpl(cyDataTableReaderManagerServiceRef);

		CyGroupManager cyGroupManager = getService(bc, CyGroupManager.class);
		CyGroupFactory cyGroupFactory = getService(bc, CyGroupFactory.class);
		
		LoadVizmapFileTaskFactoryImpl loadVizmapFileTaskFactory = new LoadVizmapFileTaskFactoryImpl(vizmapReaderManagerServiceRef,visualMappingManagerServiceRef,synchronousTaskManagerServiceRef);

		LoadNetworkFileTaskFactoryImpl loadNetworkFileTaskFactory = new LoadNetworkFileTaskFactoryImpl(cyNetworkReaderManagerServiceRef,cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef,cyPropertyServiceRef,cyNetworkNamingServiceRef);
		LoadNetworkURLTaskFactoryImpl loadNetworkURLTaskFactory = new LoadNetworkURLTaskFactoryImpl(cyNetworkReaderManagerServiceRef,cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef,cyPropertyServiceRef,cyNetworkNamingServiceRef,streamUtilRef, synchronousTaskManagerServiceRef);

		SetCurrentNetworkTaskFactoryImpl setCurrentNetworkTaskFactory = new SetCurrentNetworkTaskFactoryImpl(cyApplicationManagerServiceRef,cyNetworkManagerServiceRef);
		DeleteSelectedNodesAndEdgesTaskFactory deleteSelectedNodesAndEdgesTaskFactory = new DeleteSelectedNodesAndEdgesTaskFactory(undoSupportServiceRef,cyApplicationManagerServiceRef,cyNetworkViewManagerServiceRef,visualMappingManagerServiceRef,cyEventHelperRef);
		SelectAllTaskFactory selectAllTaskFactory = new SelectAllTaskFactory(undoSupportServiceRef,cyNetworkViewManagerServiceRef,cyEventHelperRef);
		SelectAllEdgesTaskFactory selectAllEdgesTaskFactory = new SelectAllEdgesTaskFactory(undoSupportServiceRef,cyNetworkViewManagerServiceRef,cyEventHelperRef);
		SelectAllNodesTaskFactory selectAllNodesTaskFactory = new SelectAllNodesTaskFactory(undoSupportServiceRef,cyNetworkViewManagerServiceRef,cyEventHelperRef);
		SelectAdjacentEdgesTaskFactory selectAdjacentEdgesTaskFactory = new SelectAdjacentEdgesTaskFactory(undoSupportServiceRef,cyNetworkViewManagerServiceRef,cyEventHelperRef);
		SelectConnectedNodesTaskFactory selectConnectedNodesTaskFactory = new SelectConnectedNodesTaskFactory(undoSupportServiceRef,cyNetworkViewManagerServiceRef,cyEventHelperRef);
		
		SelectFirstNeighborsTaskFactory selectFirstNeighborsTaskFactory = new SelectFirstNeighborsTaskFactory(undoSupportServiceRef,cyNetworkViewManagerServiceRef,cyEventHelperRef, CyEdge.Type.ANY);
		SelectFirstNeighborsTaskFactory selectFirstNeighborsTaskFactoryInEdge = new SelectFirstNeighborsTaskFactory(undoSupportServiceRef,cyNetworkViewManagerServiceRef,cyEventHelperRef, CyEdge.Type.INCOMING);
		SelectFirstNeighborsTaskFactory selectFirstNeighborsTaskFactoryOutEdge = new SelectFirstNeighborsTaskFactory(undoSupportServiceRef,cyNetworkViewManagerServiceRef,cyEventHelperRef, CyEdge.Type.OUTGOING);
		
		
		DeselectAllTaskFactory deselectAllTaskFactory = new DeselectAllTaskFactory(undoSupportServiceRef,cyNetworkViewManagerServiceRef,cyEventHelperRef);
		DeselectAllEdgesTaskFactory deselectAllEdgesTaskFactory = new DeselectAllEdgesTaskFactory(undoSupportServiceRef,cyNetworkViewManagerServiceRef,cyEventHelperRef);
		DeselectAllNodesTaskFactory deselectAllNodesTaskFactory = new DeselectAllNodesTaskFactory(undoSupportServiceRef,cyNetworkViewManagerServiceRef,cyEventHelperRef);
		InvertSelectedEdgesTaskFactory invertSelectedEdgesTaskFactory = new InvertSelectedEdgesTaskFactory(undoSupportServiceRef,cyNetworkViewManagerServiceRef,cyEventHelperRef);
		InvertSelectedNodesTaskFactory invertSelectedNodesTaskFactory = new InvertSelectedNodesTaskFactory(undoSupportServiceRef,cyNetworkViewManagerServiceRef,cyEventHelperRef);
		SelectFromFileListTaskFactory selectFromFileListTaskFactory = new SelectFromFileListTaskFactory(undoSupportServiceRef,cyNetworkViewManagerServiceRef,cyEventHelperRef);
		
		SelectFirstNeighborsNodeViewTaskFactory selectFirstNeighborsNodeViewTaskFactory = new SelectFirstNeighborsNodeViewTaskFactory(CyEdge.Type.ANY);
		
		HideSelectedTaskFactory hideSelectedTaskFactory = new HideSelectedTaskFactory(undoSupportServiceRef,cyEventHelperRef);
		HideSelectedNodesTaskFactory hideSelectedNodesTaskFactory = new HideSelectedNodesTaskFactory(undoSupportServiceRef,cyEventHelperRef);
		HideSelectedEdgesTaskFactory hideSelectedEdgesTaskFactory = new HideSelectedEdgesTaskFactory(undoSupportServiceRef,cyEventHelperRef);
		UnHideAllTaskFactory unHideAllTaskFactory = new UnHideAllTaskFactory(undoSupportServiceRef,cyEventHelperRef);
		UnHideAllNodesTaskFactory unHideAllNodesTaskFactory = new UnHideAllNodesTaskFactory(undoSupportServiceRef,cyEventHelperRef);
		UnHideAllEdgesTaskFactory unHideAllEdgesTaskFactory = new UnHideAllEdgesTaskFactory(undoSupportServiceRef,cyEventHelperRef);
		NewEmptyNetworkTaskFactory newEmptyNetworkTaskFactory = new NewEmptyNetworkTaskFactory(cyNetworkFactoryServiceRef,cyNetworkViewFactoryServiceRef,cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef,cyNetworkNamingServiceRef,synchronousTaskManagerServiceRef,cyApplicationManagerServiceRef);
		CloneNetworkTaskFactory cloneNetworkTaskFactory = new CloneNetworkTaskFactory(cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef,visualMappingManagerServiceRef,cyNetworkFactoryServiceRef,cyNetworkViewFactoryServiceRef,cyNetworkNamingServiceRef,cyApplicationManagerServiceRef);
		NewNetworkSelectedNodesEdgesTaskFactory newNetworkSelectedNodesEdgesTaskFactory = new NewNetworkSelectedNodesEdgesTaskFactory(undoSupportServiceRef,cyRootNetworkFactoryServiceRef,cyNetworkViewFactoryServiceRef,cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef,cyNetworkNamingServiceRef,visualMappingManagerServiceRef,cyApplicationManagerServiceRef,cyEventHelperRef);
		NewNetworkSelectedNodesOnlyTaskFactory newNetworkSelectedNodesOnlyTaskFactory = new NewNetworkSelectedNodesOnlyTaskFactory(undoSupportServiceRef,cyRootNetworkFactoryServiceRef,cyNetworkViewFactoryServiceRef,cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef,cyNetworkNamingServiceRef,visualMappingManagerServiceRef,cyApplicationManagerServiceRef,cyEventHelperRef);
		DestroyNetworkTaskFactory destroyNetworkTaskFactory = new DestroyNetworkTaskFactory(cyNetworkManagerServiceRef);
		DestroyNetworkViewTaskFactory destroyNetworkViewTaskFactory = new DestroyNetworkViewTaskFactory(cyNetworkViewManagerServiceRef);
		ZoomInTaskFactory zoomInTaskFactory = new ZoomInTaskFactory(undoSupportServiceRef);
		ZoomOutTaskFactory zoomOutTaskFactory = new ZoomOutTaskFactory(undoSupportServiceRef);
		FitSelectedTaskFactory fitSelectedTaskFactory = new FitSelectedTaskFactory(undoSupportServiceRef);
		FitContentTaskFactory fitContentTaskFactory = new FitContentTaskFactory(undoSupportServiceRef);
		NewSessionTaskFactory newSessionTaskFactory = new NewSessionTaskFactory(cySessionManagerServiceRef);
		OpenSessionTaskFactory openSessionTaskFactory = new OpenSessionTaskFactory(cySessionManagerServiceRef,sessionReaderManagerServiceRef,cyApplicationManagerServiceRef,recentlyOpenedTrackerServiceRef, synchronousTaskManagerServiceRef);
		SaveSessionTaskFactory saveSessionTaskFactory = new SaveSessionTaskFactory( sessionWriterManagerServiceRef, cySessionManagerServiceRef, recentlyOpenedTrackerServiceRef, cyEventHelperRef);
		SaveSessionAsTaskFactory saveSessionAsTaskFactory = new SaveSessionAsTaskFactory( sessionWriterManagerServiceRef, cySessionManagerServiceRef, recentlyOpenedTrackerServiceRef, cyEventHelperRef);
		ProxySettingsTaskFactory proxySettingsTaskFactory = new ProxySettingsTaskFactory(cyPropertyServiceRef, streamUtilRef);
		EditNetworkTitleTaskFactory editNetworkTitleTaskFactory = new EditNetworkTitleTaskFactory(undoSupportServiceRef);
		CreateNetworkViewTaskFactory createNetworkViewTaskFactory = new CreateNetworkViewTaskFactory(undoSupportServiceRef,cyNetworkViewFactoryServiceRef,cyNetworkViewManagerServiceRef,cyLayoutsServiceRef,cyEventHelperRef);
		ExportNetworkImageTaskFactory exportNetworkImageTaskFactory = new ExportNetworkImageTaskFactory(viewWriterManagerServiceRef,cyApplicationManagerServiceRef);
		ExportNetworkViewTaskFactory exportNetworkViewTaskFactory = new ExportNetworkViewTaskFactory(networkViewWriterManagerServiceRef);
		ExportCurrentTableTaskFactory exportCurrentTableTaskFactory = new ExportCurrentTableTaskFactory(cyTableWriterManagerRef, cyTableManagerServiceRef, cyNetworkManagerServiceRef);
		ApplyPreferredLayoutTaskFactory applyPreferredLayoutTaskFactory = new ApplyPreferredLayoutTaskFactory(undoSupportServiceRef,cyEventHelperRef,cyLayoutsServiceRef,cyPropertyServiceRef);
		DeleteColumnTaskFactory deleteColumnTaskFactory = new DeleteColumnTaskFactory(undoSupportServiceRef);
		RenameColumnTaskFactory renameColumnTaskFactory = new RenameColumnTaskFactory(undoSupportServiceRef);
		CopyValueToEntireColumnTaskFactory copyValueToEntireColumnTaskFactory = new CopyValueToEntireColumnTaskFactory(undoSupportServiceRef);
		DeleteTableTaskFactory deleteTableTaskFactory = new DeleteTableTaskFactory(cyTableManagerServiceRef);
		ExportVizmapTaskFactory exportVizmapTaskFactory = new ExportVizmapTaskFactory(vizmapWriterManagerServiceRef,visualMappingManagerServiceRef);
		SubnetworkBuilderUtil subnetworkBuilderUtil = new SubnetworkBuilderUtil(cyNetworkReaderManagerServiceRef,cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef,cyPropertyServiceRef,cyNetworkNamingServiceRef,streamUtilRef,cyEventHelperRef,cyApplicationManagerServiceRef,cyRootNetworkFactoryServiceRef,cyNetworkViewFactoryServiceRef,visualMappingManagerServiceRef,visualStyleFactoryServiceRef,cyLayoutsServiceRef,undoSupportServiceRef);
		ImportTaskUtil importTaskUtil = new ImportTaskUtil(cyNetworkReaderManagerServiceRef,cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef,cyPropertyServiceRef,cyNetworkNamingServiceRef,streamUtilRef,cyDataTableReaderManagerServiceRef,cyApplicationManagerServiceRef);

		BioGridPreprocessor bioGridPreprocessor = new BioGridPreprocessor(cyPropertyServiceRef,cyApplicationConfigurationServiceRef);
		ConnectSelectedNodesTaskFactory connectSelectedNodesTaskFactory = new ConnectSelectedNodesTaskFactory(undoSupportServiceRef,cyApplicationManagerServiceRef,cyEventHelperRef);

		GroupNodesTaskFactory groupNodesTaskFactory = new GroupNodesTaskFactory(cyGroupManager, cyGroupFactory);
		GroupNodeContextTaskFactory collapseGroupTaskFactory = new GroupNodeContextTaskFactory(cyGroupManager, true);
		GroupNodeContextTaskFactory expandGroupTaskFactory = new GroupNodeContextTaskFactory(cyGroupManager, false);
		
		
		Properties loadNetworkFileTaskFactoryProps = new Properties();
		loadNetworkFileTaskFactoryProps.setProperty("id","loadNetworkFileTaskFactory");
		loadNetworkFileTaskFactoryProps.setProperty("preferredMenu","File.Import.Network");
		loadNetworkFileTaskFactoryProps.setProperty("accelerator","cmd l");
		loadNetworkFileTaskFactoryProps.setProperty("title","File...");
		loadNetworkFileTaskFactoryProps.setProperty("commandNamespace","network");
		loadNetworkFileTaskFactoryProps.setProperty("command","load");
		loadNetworkFileTaskFactoryProps.setProperty("menuGravity","1.0");
		loadNetworkFileTaskFactoryProps.setProperty("toolBarGravity","3.0");
		loadNetworkFileTaskFactoryProps.setProperty("largeIconURL",getClass().getResource("/images/icons/net_file_import.png").toString());
		loadNetworkFileTaskFactoryProps.setProperty("inToolBar","true");
		loadNetworkFileTaskFactoryProps.setProperty("tooltip","Import Network From File");
		registerAllServices(bc,loadNetworkFileTaskFactory, loadNetworkFileTaskFactoryProps);

		Properties loadNetworkURLTaskFactoryProps = new Properties();
		loadNetworkURLTaskFactoryProps.setProperty("id","loadNetworkURLTaskFactory");
		loadNetworkURLTaskFactoryProps.setProperty("preferredMenu","File.Import.Network");
		loadNetworkURLTaskFactoryProps.setProperty("accelerator","cmd shift l");
		loadNetworkURLTaskFactoryProps.setProperty("menuGravity","2.0");
		loadNetworkURLTaskFactoryProps.setProperty("toolBarGravity","3.1");
		loadNetworkURLTaskFactoryProps.setProperty("title","URL...");
		loadNetworkURLTaskFactoryProps.setProperty("largeIconURL",getClass().getResource("/images/icons/net_url_import.png").toString());
		loadNetworkURLTaskFactoryProps.setProperty("inToolBar","true");
		loadNetworkURLTaskFactoryProps.setProperty("tooltip","Import Network From URL");
		registerAllServices(bc,loadNetworkURLTaskFactory, loadNetworkURLTaskFactoryProps);

		Properties loadVizmapFileTaskFactoryProps = new Properties();
		loadVizmapFileTaskFactoryProps.setProperty("preferredMenu","File.Import");
		loadVizmapFileTaskFactoryProps.setProperty("menuGravity","3.0");
		loadVizmapFileTaskFactoryProps.setProperty("title","Vizmap File...");
		registerService(bc,loadVizmapFileTaskFactory,TaskFactory.class, loadVizmapFileTaskFactoryProps);
		registerService(bc,loadVizmapFileTaskFactory,LoadVisualStyles.class, new Properties());

		Properties loadAttrsFileTaskFactoryProps = new Properties();
		loadAttrsFileTaskFactoryProps.setProperty("preferredMenu","File.Import.Table");
		loadAttrsFileTaskFactoryProps.setProperty("menuGravity","1.1");
		loadAttrsFileTaskFactoryProps.setProperty("toolBarGravity","3.2");
		loadAttrsFileTaskFactoryProps.setProperty("title","File...");
		loadAttrsFileTaskFactoryProps.setProperty("largeIconURL",getClass().getResource("/images/icons/table_file_import.png").toString());
		loadAttrsFileTaskFactoryProps.setProperty("inToolBar","true");
		loadAttrsFileTaskFactoryProps.setProperty("tooltip","Import Table From File");
		registerService(bc,loadAttrsFileTaskFactory,TaskFactory.class, loadAttrsFileTaskFactoryProps);

		Properties loadAttrsURLTaskFactoryProps = new Properties();
		loadAttrsURLTaskFactoryProps.setProperty("preferredMenu","File.Import.Table");
		loadAttrsURLTaskFactoryProps.setProperty("menuGravity","1.2");
		loadAttrsURLTaskFactoryProps.setProperty("toolBarGravity","3.3");
		loadAttrsURLTaskFactoryProps.setProperty("title","URL...");
		loadAttrsURLTaskFactoryProps.setProperty("largeIconURL",getClass().getResource("/images/icons/table_url_import.png").toString());
		loadAttrsURLTaskFactoryProps.setProperty("inToolBar","true");
		loadAttrsURLTaskFactoryProps.setProperty("tooltip","Import Table From URL");
		registerService(bc,loadAttrsURLTaskFactory,TaskFactory.class, loadAttrsURLTaskFactoryProps);

		Properties proxySettingsTaskFactoryProps = new Properties();
		proxySettingsTaskFactoryProps.setProperty("preferredMenu","Edit.Preferences");
		proxySettingsTaskFactoryProps.setProperty("menuGravity","1.0");
		proxySettingsTaskFactoryProps.setProperty("title","Proxy Settings...");
		registerService(bc,proxySettingsTaskFactory,TaskFactory.class, proxySettingsTaskFactoryProps);

		Properties deleteSelectedNodesAndEdgesTaskFactoryProps = new Properties();
		deleteSelectedNodesAndEdgesTaskFactoryProps.setProperty("preferredMenu","Edit");
		deleteSelectedNodesAndEdgesTaskFactoryProps.setProperty("enableFor","selectedNodesOrEdges");
		deleteSelectedNodesAndEdgesTaskFactoryProps.setProperty("title","Delete Selected Nodes and Edges...");
		deleteSelectedNodesAndEdgesTaskFactoryProps.setProperty("command","delete selected nodes and edges");
		deleteSelectedNodesAndEdgesTaskFactoryProps.setProperty("commandNamespace","network");
		deleteSelectedNodesAndEdgesTaskFactoryProps.setProperty("menuGravity","5.0");
		deleteSelectedNodesAndEdgesTaskFactoryProps.setProperty("accelerator","DELETE");
		
		registerService(bc,deleteSelectedNodesAndEdgesTaskFactory,TaskFactory.class, deleteSelectedNodesAndEdgesTaskFactoryProps);

		Properties selectAllTaskFactoryProps = new Properties();
		selectAllTaskFactoryProps.setProperty("preferredMenu","Select");
		selectAllTaskFactoryProps.setProperty("accelerator","cmd alt a");
		selectAllTaskFactoryProps.setProperty("enableFor","network");
		selectAllTaskFactoryProps.setProperty("title","Select all nodes and edges");
		selectAllTaskFactoryProps.setProperty("command","select all nodes");
		selectAllTaskFactoryProps.setProperty("commandNamespace","network");
		selectAllTaskFactoryProps.setProperty("menuGravity","5.0");
		registerService(bc,selectAllTaskFactory,NetworkTaskFactory.class, selectAllTaskFactoryProps);

		Properties selectAllEdgesTaskFactoryProps = new Properties();
		selectAllEdgesTaskFactoryProps.setProperty("preferredMenu","Select.Edges");
		selectAllEdgesTaskFactoryProps.setProperty("accelerator","cmd alt a");
		selectAllEdgesTaskFactoryProps.setProperty("enableFor","network");
		selectAllEdgesTaskFactoryProps.setProperty("title","Select all edges");
		selectAllEdgesTaskFactoryProps.setProperty("command","select all edges");
		selectAllEdgesTaskFactoryProps.setProperty("commandNamespace","network");
		selectAllEdgesTaskFactoryProps.setProperty("menuGravity","4.0");
		registerService(bc,selectAllEdgesTaskFactory,NetworkTaskFactory.class, selectAllEdgesTaskFactoryProps);

		Properties selectAllNodesTaskFactoryProps = new Properties();
		selectAllNodesTaskFactoryProps.setProperty("enableFor","network");
		selectAllNodesTaskFactoryProps.setProperty("preferredMenu","Select.Nodes");
		selectAllNodesTaskFactoryProps.setProperty("menuGravity","4.0");
		selectAllNodesTaskFactoryProps.setProperty("accelerator","cmd a");
		selectAllNodesTaskFactoryProps.setProperty("title","Select all nodes");
		registerService(bc,selectAllNodesTaskFactory,NetworkTaskFactory.class, selectAllNodesTaskFactoryProps);

		Properties selectAdjacentEdgesTaskFactoryProps = new Properties();
		selectAdjacentEdgesTaskFactoryProps.setProperty("enableFor","network");
		selectAdjacentEdgesTaskFactoryProps.setProperty("preferredMenu","Select.Edges");
		selectAdjacentEdgesTaskFactoryProps.setProperty("menuGravity","6.0");
		selectAdjacentEdgesTaskFactoryProps.setProperty("accelerator","alt e");
		selectAdjacentEdgesTaskFactoryProps.setProperty("title","Select adjacent edges");
		registerService(bc,selectAdjacentEdgesTaskFactory,NetworkTaskFactory.class, selectAdjacentEdgesTaskFactoryProps);

		Properties selectConnectedNodesTaskFactoryProps = new Properties();
		selectConnectedNodesTaskFactoryProps.setProperty("enableFor","network");
		selectConnectedNodesTaskFactoryProps.setProperty("preferredMenu","Select.Nodes");
		selectConnectedNodesTaskFactoryProps.setProperty("menuGravity","7.0");
		selectConnectedNodesTaskFactoryProps.setProperty("accelerator","cmd 7");
		selectConnectedNodesTaskFactoryProps.setProperty("title","Nodes connected by selected edges");
		registerService(bc,selectConnectedNodesTaskFactory,NetworkTaskFactory.class, selectConnectedNodesTaskFactoryProps);

		Properties selectFirstNeighborsTaskFactoryProps = new Properties();
		selectFirstNeighborsTaskFactoryProps.setProperty("enableFor","selectedNodesOrEdges");
		selectFirstNeighborsTaskFactoryProps.setProperty("preferredMenu","Select.Nodes.First Neighbors of Selected Nodes");
		selectFirstNeighborsTaskFactoryProps.setProperty("menuGravity","6.0");
		selectFirstNeighborsTaskFactoryProps.setProperty("toolBarGravity","9.1");
		selectFirstNeighborsTaskFactoryProps.setProperty("accelerator","cmd 6");
		selectFirstNeighborsTaskFactoryProps.setProperty("title","Undirected");
		selectFirstNeighborsTaskFactoryProps.setProperty("largeIconURL",getClass().getResource("/images/icons/select_firstneighbors.png").toString());
		selectFirstNeighborsTaskFactoryProps.setProperty("inToolBar","true");
		selectFirstNeighborsTaskFactoryProps.setProperty("tooltip","First Neighbors of Selected Nodes (Undirected)");
		registerService(bc,selectFirstNeighborsTaskFactory,NetworkTaskFactory.class, selectFirstNeighborsTaskFactoryProps);
		Properties selectFirstNeighborsTaskFactoryInEdgeProps = new Properties();
		selectFirstNeighborsTaskFactoryInEdgeProps.setProperty("enableFor","network");
		selectFirstNeighborsTaskFactoryInEdgeProps.setProperty("preferredMenu","Select.Nodes.First Neighbors of Selected Nodes");
		selectFirstNeighborsTaskFactoryInEdgeProps.setProperty("menuGravity","6.1");
		selectFirstNeighborsTaskFactoryInEdgeProps.setProperty("title","Directed: Incoming");
		selectFirstNeighborsTaskFactoryInEdgeProps.setProperty("tooltip","First Neighbors of Selected Nodes (Directed: Incoming)");
		registerService(bc,selectFirstNeighborsTaskFactoryInEdge,NetworkTaskFactory.class, selectFirstNeighborsTaskFactoryInEdgeProps);
		Properties selectFirstNeighborsTaskFactoryOutEdgeProps = new Properties();
		selectFirstNeighborsTaskFactoryOutEdgeProps.setProperty("enableFor","network");
		selectFirstNeighborsTaskFactoryOutEdgeProps.setProperty("preferredMenu","Select.Nodes.First Neighbors of Selected Nodes");
		selectFirstNeighborsTaskFactoryOutEdgeProps.setProperty("menuGravity","6.2");
		selectFirstNeighborsTaskFactoryOutEdgeProps.setProperty("title","Directed: Outgoing");
		selectFirstNeighborsTaskFactoryOutEdgeProps.setProperty("tooltip","First Neighbors of Selected Nodes (Directed: Outgoing)");
		registerService(bc,selectFirstNeighborsTaskFactoryOutEdge,NetworkTaskFactory.class, selectFirstNeighborsTaskFactoryOutEdgeProps);
		

		Properties deselectAllTaskFactoryProps = new Properties();
		deselectAllTaskFactoryProps.setProperty("enableFor","network");
		deselectAllTaskFactoryProps.setProperty("preferredMenu","Select");
		deselectAllTaskFactoryProps.setProperty("menuGravity","5.1");
		deselectAllTaskFactoryProps.setProperty("accelerator","cmd shift alt a");
		deselectAllTaskFactoryProps.setProperty("title","Deselect all nodes and edges");
		registerService(bc,deselectAllTaskFactory,NetworkTaskFactory.class, deselectAllTaskFactoryProps);

		Properties deselectAllEdgesTaskFactoryProps = new Properties();
		deselectAllEdgesTaskFactoryProps.setProperty("enableFor","network");
		deselectAllEdgesTaskFactoryProps.setProperty("preferredMenu","Select.Edges");
		deselectAllEdgesTaskFactoryProps.setProperty("menuGravity","5.0");
		deselectAllEdgesTaskFactoryProps.setProperty("accelerator","alt shift a");
		deselectAllEdgesTaskFactoryProps.setProperty("title","Deselect all edges");
		registerService(bc,deselectAllEdgesTaskFactory,NetworkTaskFactory.class, deselectAllEdgesTaskFactoryProps);

		Properties deselectAllNodesTaskFactoryProps = new Properties();
		deselectAllNodesTaskFactoryProps.setProperty("enableFor","network");
		deselectAllNodesTaskFactoryProps.setProperty("preferredMenu","Select.Nodes");
		deselectAllNodesTaskFactoryProps.setProperty("menuGravity","5.0");
		deselectAllNodesTaskFactoryProps.setProperty("accelerator","cmd shift a");
		deselectAllNodesTaskFactoryProps.setProperty("title","Deselect all nodes");
		registerService(bc,deselectAllNodesTaskFactory,NetworkTaskFactory.class, deselectAllNodesTaskFactoryProps);

		Properties invertSelectedEdgesTaskFactoryProps = new Properties();
		invertSelectedEdgesTaskFactoryProps.setProperty("enableFor","network");
		invertSelectedEdgesTaskFactoryProps.setProperty("preferredMenu","Select.Edges");
		invertSelectedEdgesTaskFactoryProps.setProperty("menuGravity","1.0");
		invertSelectedEdgesTaskFactoryProps.setProperty("accelerator","alt i");
		invertSelectedEdgesTaskFactoryProps.setProperty("title","Invert edge selection");
		registerService(bc,invertSelectedEdgesTaskFactory,NetworkTaskFactory.class, invertSelectedEdgesTaskFactoryProps);

		Properties invertSelectedNodesTaskFactoryProps = new Properties();
		invertSelectedNodesTaskFactoryProps.setProperty("enableFor","selectedNodes");
		invertSelectedNodesTaskFactoryProps.setProperty("preferredMenu","Select.Nodes");
		invertSelectedNodesTaskFactoryProps.setProperty("menuGravity","1.0");
		invertSelectedNodesTaskFactoryProps.setProperty("toolBarGravity","9.2");
		invertSelectedNodesTaskFactoryProps.setProperty("accelerator","cmd i");
		invertSelectedNodesTaskFactoryProps.setProperty("title","Invert node selection");
		invertSelectedNodesTaskFactoryProps.setProperty("largeIconURL",getClass().getResource("/images/icons/invert_selection.png").toString());
		invertSelectedNodesTaskFactoryProps.setProperty("inToolBar","true");
		invertSelectedNodesTaskFactoryProps.setProperty("tooltip","Invert Node Selection");
		registerService(bc,invertSelectedNodesTaskFactory,NetworkTaskFactory.class, invertSelectedNodesTaskFactoryProps);

		Properties selectFromFileListTaskFactoryProps = new Properties();
		selectFromFileListTaskFactoryProps.setProperty("enableFor","network");
		selectFromFileListTaskFactoryProps.setProperty("preferredMenu","Select.Nodes");
		selectFromFileListTaskFactoryProps.setProperty("menuGravity","8.0");
		selectFromFileListTaskFactoryProps.setProperty("accelerator","cmd i");
		selectFromFileListTaskFactoryProps.setProperty("title","From ID List file...");
		registerService(bc,selectFromFileListTaskFactory,NetworkTaskFactory.class, selectFromFileListTaskFactoryProps);

		Properties selectFirstNeighborsNodeViewTaskFactoryProps = new Properties();
		selectFirstNeighborsNodeViewTaskFactoryProps.setProperty("title","Select First Neighbors (Undirected)");
		registerService(bc,selectFirstNeighborsNodeViewTaskFactory,NodeViewTaskFactory.class, selectFirstNeighborsNodeViewTaskFactoryProps);

		Properties hideSelectedTaskFactoryProps = new Properties();
		hideSelectedTaskFactoryProps.setProperty("enableFor","selectedNodesOrEdges");
		hideSelectedTaskFactoryProps.setProperty("preferredMenu","Select");
		hideSelectedTaskFactoryProps.setProperty("menuGravity","3.1");
		hideSelectedTaskFactoryProps.setProperty("toolBarGravity","9.5");
		hideSelectedTaskFactoryProps.setProperty("title","Hide selected nodes and edges");
		hideSelectedTaskFactoryProps.setProperty("largeIconURL",getClass().getResource("/images/icons/hide_selected.png").toString());
		hideSelectedTaskFactoryProps.setProperty("inToolBar","true");
		hideSelectedTaskFactoryProps.setProperty("tooltip","Hide Selected Nodes and Edges");
		registerService(bc,hideSelectedTaskFactory,NetworkViewTaskFactory.class, hideSelectedTaskFactoryProps);

		Properties hideSelectedNodesTaskFactoryProps = new Properties();
		hideSelectedNodesTaskFactoryProps.setProperty("enableFor","selectedNodes");
		hideSelectedNodesTaskFactoryProps.setProperty("preferredMenu","Select.Nodes");
		hideSelectedNodesTaskFactoryProps.setProperty("menuGravity","2.0");
		hideSelectedNodesTaskFactoryProps.setProperty("title","Hide selected nodes");
		registerService(bc,hideSelectedNodesTaskFactory,NetworkViewTaskFactory.class, hideSelectedNodesTaskFactoryProps);

		Properties hideSelectedEdgesTaskFactoryProps = new Properties();
		hideSelectedEdgesTaskFactoryProps.setProperty("enableFor","selectedEdges");
		hideSelectedEdgesTaskFactoryProps.setProperty("preferredMenu","Select.Edges");
		hideSelectedEdgesTaskFactoryProps.setProperty("menuGravity","2.0");
		hideSelectedEdgesTaskFactoryProps.setProperty("title","Hide selected edges");
		registerService(bc,hideSelectedEdgesTaskFactory,NetworkViewTaskFactory.class, hideSelectedEdgesTaskFactoryProps);

		Properties unHideAllTaskFactoryProps = new Properties();
		unHideAllTaskFactoryProps.setProperty("enableFor","networkAndView");
		unHideAllTaskFactoryProps.setProperty("preferredMenu","Select");
		unHideAllTaskFactoryProps.setProperty("menuGravity","3.0");
		unHideAllTaskFactoryProps.setProperty("toolBarGravity","9.6");
		unHideAllTaskFactoryProps.setProperty("title","Show all nodes and edges");
		unHideAllTaskFactoryProps.setProperty("largeIconURL",getClass().getResource("/images/icons/unhide_all.png").toString());
		unHideAllTaskFactoryProps.setProperty("inToolBar","true");
		unHideAllTaskFactoryProps.setProperty("tooltip","Show All Nodes and Edges");
		registerService(bc,unHideAllTaskFactory,NetworkViewTaskFactory.class, unHideAllTaskFactoryProps);

		Properties unHideAllNodesTaskFactoryProps = new Properties();
		unHideAllNodesTaskFactoryProps.setProperty("enableFor","networkAndView");
		unHideAllNodesTaskFactoryProps.setProperty("preferredMenu","Select.Nodes");
		unHideAllNodesTaskFactoryProps.setProperty("menuGravity","3.0");
		unHideAllNodesTaskFactoryProps.setProperty("title","Show all nodes");
		registerService(bc,unHideAllNodesTaskFactory,NetworkViewTaskFactory.class, unHideAllNodesTaskFactoryProps);

		Properties unHideAllEdgesTaskFactoryProps = new Properties();
		unHideAllEdgesTaskFactoryProps.setProperty("enableFor","networkAndView");
		unHideAllEdgesTaskFactoryProps.setProperty("preferredMenu","Select.Edges");
		unHideAllEdgesTaskFactoryProps.setProperty("menuGravity","3.0");
		unHideAllEdgesTaskFactoryProps.setProperty("title","Show all edges");
		registerService(bc,unHideAllEdgesTaskFactory,NetworkViewTaskFactory.class, unHideAllEdgesTaskFactoryProps);

		Properties newEmptyNetworkTaskFactoryProps = new Properties();
		newEmptyNetworkTaskFactoryProps.setProperty("preferredMenu","File.New.Network");
		newEmptyNetworkTaskFactoryProps.setProperty("menuGravity","4.0");
		newEmptyNetworkTaskFactoryProps.setProperty("title","Empty Network");
		registerService(bc,newEmptyNetworkTaskFactory,TaskFactory.class, newEmptyNetworkTaskFactoryProps);
		registerService(bc,newEmptyNetworkTaskFactory,NewEmptyNetworkViewFactory.class, newEmptyNetworkTaskFactoryProps);

		Properties newNetworkSelectedNodesEdgesTaskFactoryProps = new Properties();
		newNetworkSelectedNodesEdgesTaskFactoryProps.setProperty("enableFor","selectedNodesOrEdges");
		newNetworkSelectedNodesEdgesTaskFactoryProps.setProperty("preferredMenu","File.New.Network");
		newNetworkSelectedNodesEdgesTaskFactoryProps.setProperty("menuGravity","2.0");
		newNetworkSelectedNodesEdgesTaskFactoryProps.setProperty("accelerator","cmd shift n");
		newNetworkSelectedNodesEdgesTaskFactoryProps.setProperty("title","From selected nodes, selected edges");
		registerService(bc,newNetworkSelectedNodesEdgesTaskFactory,NetworkTaskFactory.class, newNetworkSelectedNodesEdgesTaskFactoryProps);

		Properties newNetworkSelectedNodesOnlyTaskFactoryProps = new Properties();
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty("preferredMenu","File.New.Network");
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty("largeIconURL",getClass().getResource("/images/icons/new_fromselected.png").toString());
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty("accelerator","cmd n");
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty("enableFor","selectedNodesOrEdges");
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty("title","From selected nodes, all edges");
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty("toolBarGravity","9.1");
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty("inToolBar","true");
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty("menuGravity","1.0");
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty("tooltip","New Network From Selection");
		registerService(bc,newNetworkSelectedNodesOnlyTaskFactory,NetworkTaskFactory.class, newNetworkSelectedNodesOnlyTaskFactoryProps);

		Properties cloneNetworkTaskFactoryProps = new Properties();
		cloneNetworkTaskFactoryProps.setProperty("enableFor","network");
		cloneNetworkTaskFactoryProps.setProperty("preferredMenu","File.New.Network");
		cloneNetworkTaskFactoryProps.setProperty("menuGravity","3.0");
		cloneNetworkTaskFactoryProps.setProperty("title","Clone Current Network");
		registerService(bc,cloneNetworkTaskFactory,NetworkTaskFactory.class, cloneNetworkTaskFactoryProps);

		Properties destroyNetworkTaskFactoryProps = new Properties();
		destroyNetworkTaskFactoryProps.setProperty("preferredMenu","Edit");
		destroyNetworkTaskFactoryProps.setProperty("accelerator","cmd shift w");
		destroyNetworkTaskFactoryProps.setProperty("enableFor","network");
		destroyNetworkTaskFactoryProps.setProperty("title","Destroy Network");
		destroyNetworkTaskFactoryProps.setProperty("scope","limited");
		destroyNetworkTaskFactoryProps.setProperty("menuGravity","3.2");
		registerService(bc,destroyNetworkTaskFactory,NetworkCollectionTaskFactory.class, destroyNetworkTaskFactoryProps);

		Properties destroyNetworkViewTaskFactoryProps = new Properties();
		destroyNetworkViewTaskFactoryProps.setProperty("preferredMenu","Edit");
		destroyNetworkViewTaskFactoryProps.setProperty("accelerator","cmd w");
		destroyNetworkViewTaskFactoryProps.setProperty("enableFor","networkAndView");
		destroyNetworkViewTaskFactoryProps.setProperty("title","Destroy View");
		destroyNetworkViewTaskFactoryProps.setProperty("scope","limited");
		destroyNetworkViewTaskFactoryProps.setProperty("menuGravity","3.1");
		registerService(bc,destroyNetworkViewTaskFactory,NetworkViewCollectionTaskFactory.class, destroyNetworkViewTaskFactoryProps);

		Properties zoomInTaskFactoryProps = new Properties();
		zoomInTaskFactoryProps.setProperty("accelerator","cmd equals");
		zoomInTaskFactoryProps.setProperty("largeIconURL",getClass().getResource("/images/icons/stock_zoom-in.png").toString());
		zoomInTaskFactoryProps.setProperty("enableFor","networkAndView");
		zoomInTaskFactoryProps.setProperty("title","Zoom In");
		zoomInTaskFactoryProps.setProperty("tooltip","Zoom In");
		zoomInTaskFactoryProps.setProperty("toolBarGravity","5.1");
		zoomInTaskFactoryProps.setProperty("inToolBar","true");
		registerService(bc,zoomInTaskFactory,NetworkViewTaskFactory.class, zoomInTaskFactoryProps);

		Properties zoomOutTaskFactoryProps = new Properties();
		zoomOutTaskFactoryProps.setProperty("accelerator","cmd minus");
		zoomOutTaskFactoryProps.setProperty("largeIconURL",getClass().getResource("/images/icons/stock_zoom-out.png").toString());
		zoomOutTaskFactoryProps.setProperty("enableFor","networkAndView");
		zoomOutTaskFactoryProps.setProperty("title","Zoom Out");
		zoomOutTaskFactoryProps.setProperty("tooltip","Zoom Out");
		zoomOutTaskFactoryProps.setProperty("toolBarGravity","5.2");
		zoomOutTaskFactoryProps.setProperty("inToolBar","true");
		registerService(bc,zoomOutTaskFactory,NetworkViewTaskFactory.class, zoomOutTaskFactoryProps);

		Properties fitSelectedTaskFactoryProps = new Properties();
		fitSelectedTaskFactoryProps.setProperty("accelerator","cmd shift f");
		fitSelectedTaskFactoryProps.setProperty("largeIconURL",getClass().getResource("/images/icons/stock_zoom-object.png").toString());
		fitSelectedTaskFactoryProps.setProperty("enableFor","networkAndView");
		fitSelectedTaskFactoryProps.setProperty("title","Fit Selected");
		fitSelectedTaskFactoryProps.setProperty("tooltip","Zoom selected region");
		fitSelectedTaskFactoryProps.setProperty("toolBarGravity","5.4");
		fitSelectedTaskFactoryProps.setProperty("inToolBar","true");
		registerService(bc,fitSelectedTaskFactory,NetworkViewTaskFactory.class, fitSelectedTaskFactoryProps);

		Properties fitContentTaskFactoryProps = new Properties();
		fitContentTaskFactoryProps.setProperty("accelerator","cmd f");
		fitContentTaskFactoryProps.setProperty("largeIconURL",getClass().getResource("/images/icons/stock_zoom-1.png").toString());
		fitContentTaskFactoryProps.setProperty("enableFor","networkAndView");
		fitContentTaskFactoryProps.setProperty("title","Fit Content");
		fitContentTaskFactoryProps.setProperty("tooltip","Zoom out to display all of current Network");
		fitContentTaskFactoryProps.setProperty("toolBarGravity","5.3");
		fitContentTaskFactoryProps.setProperty("inToolBar","true");
		registerService(bc,fitContentTaskFactory,NetworkViewTaskFactory.class, fitContentTaskFactoryProps);

		Properties editNetworkTitleTaskFactoryProps = new Properties();
		editNetworkTitleTaskFactoryProps.setProperty("enableFor","network");
		editNetworkTitleTaskFactoryProps.setProperty("preferredMenu","Edit");
		editNetworkTitleTaskFactoryProps.setProperty("scope","limited");
		editNetworkTitleTaskFactoryProps.setProperty("menuGravity","5.5");
		editNetworkTitleTaskFactoryProps.setProperty("title","Rename Network...");
		registerService(bc,editNetworkTitleTaskFactory,NetworkTaskFactory.class, editNetworkTitleTaskFactoryProps);

		Properties createNetworkViewTaskFactoryProps = new Properties();
		createNetworkViewTaskFactoryProps.setProperty("id","createNetworkViewTaskFactory");
		createNetworkViewTaskFactoryProps.setProperty("enableFor","networkWithoutView");
		createNetworkViewTaskFactoryProps.setProperty("preferredMenu","Edit");
		createNetworkViewTaskFactoryProps.setProperty("scope","limited");
		createNetworkViewTaskFactoryProps.setProperty("menuGravity","3.0");
		createNetworkViewTaskFactoryProps.setProperty("title","Create View");
		registerService(bc,createNetworkViewTaskFactory,NetworkTaskFactory.class, createNetworkViewTaskFactoryProps);

		Properties exportNetworkImageTaskFactoryProps = new Properties();
		exportNetworkImageTaskFactoryProps.setProperty("preferredMenu","File.Export.Network View");
		exportNetworkImageTaskFactoryProps.setProperty("largeIconURL",getClass().getResource("/images/icons/img_file_export.png").toString());
		exportNetworkImageTaskFactoryProps.setProperty("enableFor","networkAndView");
		exportNetworkImageTaskFactoryProps.setProperty("title","Graphics...");
		exportNetworkImageTaskFactoryProps.setProperty("toolBarGravity","3.7");
		exportNetworkImageTaskFactoryProps.setProperty("inToolBar","true");
		exportNetworkImageTaskFactoryProps.setProperty("tooltip","Export Network Image to File");
		registerService(bc,exportNetworkImageTaskFactory,NetworkViewTaskFactory.class, exportNetworkImageTaskFactoryProps);

		Properties exportNetworkViewTaskFactoryProps = new Properties();
		exportNetworkViewTaskFactoryProps.setProperty("enableFor","networkAndView");
		exportNetworkViewTaskFactoryProps.setProperty("preferredMenu","File.Export.Network View");
		exportNetworkViewTaskFactoryProps.setProperty("menuGravity","5.1");
		exportNetworkViewTaskFactoryProps.setProperty("toolBarGravity","3.5");
		exportNetworkViewTaskFactoryProps.setProperty("title","File...");
		exportNetworkViewTaskFactoryProps.setProperty("largeIconURL",getClass().getResource("/images/icons/net_file_export.png").toString());
		exportNetworkViewTaskFactoryProps.setProperty("inToolBar","true");
		exportNetworkViewTaskFactoryProps.setProperty("tooltip","Export Network to File");
		registerService(bc,exportNetworkViewTaskFactory,NetworkViewTaskFactory.class, exportNetworkViewTaskFactoryProps);

		Properties exportCurrentTableTaskFactoryProps = new Properties();
		exportCurrentTableTaskFactoryProps.setProperty("enableFor","table");
		exportCurrentTableTaskFactoryProps.setProperty("preferredMenu","File.Export.Table");
		exportCurrentTableTaskFactoryProps.setProperty("menuGravity","1.2");
		exportCurrentTableTaskFactoryProps.setProperty("toolBarGravity","3.6");
		exportCurrentTableTaskFactoryProps.setProperty("title","File...");
		exportCurrentTableTaskFactoryProps.setProperty("largeIconURL",getClass().getResource("/images/icons/table_file_export.png").toString());
		exportCurrentTableTaskFactoryProps.setProperty("inToolBar","true");
		exportCurrentTableTaskFactoryProps.setProperty("tooltip","Export Table to File");
		registerService(bc,exportCurrentTableTaskFactory,TableTaskFactory.class, exportCurrentTableTaskFactoryProps);

		Properties exportVizmapTaskFactoryProps = new Properties();
		exportVizmapTaskFactoryProps.setProperty("enableFor","vizmap");
		exportVizmapTaskFactoryProps.setProperty("preferredMenu","File.Export.Vizmap");
		exportVizmapTaskFactoryProps.setProperty("menuGravity","1.1");
		exportVizmapTaskFactoryProps.setProperty("title","File...");
		registerService(bc,exportVizmapTaskFactory,TaskFactory.class, exportVizmapTaskFactoryProps);

		Properties newSessionTaskFactoryProps = new Properties();
		newSessionTaskFactoryProps.setProperty("preferredMenu","File.New");
		newSessionTaskFactoryProps.setProperty("menuGravity","1.1");
		newSessionTaskFactoryProps.setProperty("title","Session");
		registerService(bc,newSessionTaskFactory,TaskFactory.class, newSessionTaskFactoryProps);

		Properties openSessionTaskFactoryProps = new Properties();
		openSessionTaskFactoryProps.setProperty("id","openSessionTaskFactory");
		openSessionTaskFactoryProps.setProperty("preferredMenu","File");
		openSessionTaskFactoryProps.setProperty("accelerator","cmd o");
		openSessionTaskFactoryProps.setProperty("largeIconURL",getClass().getResource("/images/icons/open_session.png").toString());
		openSessionTaskFactoryProps.setProperty("title","Open");
		openSessionTaskFactoryProps.setProperty("toolBarGravity","1.0");
		openSessionTaskFactoryProps.setProperty("inToolBar","true");
		openSessionTaskFactoryProps.setProperty("menuGravity","1.0");
		openSessionTaskFactoryProps.setProperty("tooltip","Open Session");
		registerService(bc,openSessionTaskFactory,TaskFactory.class, openSessionTaskFactoryProps);

		Properties saveSessionTaskFactoryProps = new Properties();
		saveSessionTaskFactoryProps.setProperty("preferredMenu","File");
		saveSessionTaskFactoryProps.setProperty("accelerator","cmd s");
		saveSessionTaskFactoryProps.setProperty("largeIconURL",getClass().getResource("/images/icons/stock_save.png").toString());
		saveSessionTaskFactoryProps.setProperty("title","Save");
		saveSessionTaskFactoryProps.setProperty("toolBarGravity","1.1");
		saveSessionTaskFactoryProps.setProperty("inToolBar","true");
		saveSessionTaskFactoryProps.setProperty("menuGravity","3.0");
		saveSessionTaskFactoryProps.setProperty("tooltip","Save Session");
		saveSessionTaskFactoryProps.setProperty("task.id","saveSession"); // TODO: Find a better way of making anonymous TaskFactories available to other bundles
		registerService(bc,saveSessionTaskFactory,TaskFactory.class, saveSessionTaskFactoryProps);

		Properties saveSessionAsTaskFactoryProps = new Properties();
		saveSessionAsTaskFactoryProps.setProperty("preferredMenu","File");
		saveSessionAsTaskFactoryProps.setProperty("accelerator","cmd shift s");
		saveSessionAsTaskFactoryProps.setProperty("menuGravity","3.1");
		saveSessionAsTaskFactoryProps.setProperty("title","Save As");
		registerService(bc,saveSessionAsTaskFactory,TaskFactory.class, saveSessionAsTaskFactoryProps);

		Properties applyPreferredLayoutTaskFactoryProps = new Properties();
		applyPreferredLayoutTaskFactoryProps.setProperty("preferredMenu","Layout");
		applyPreferredLayoutTaskFactoryProps.setProperty("accelerator","fn5");
		applyPreferredLayoutTaskFactoryProps.setProperty("largeIconURL",getClass().getResource("/images/icons/apply_layout.png").toString());
		applyPreferredLayoutTaskFactoryProps.setProperty("enableFor","networkAndView");
		applyPreferredLayoutTaskFactoryProps.setProperty("title","Apply Preferred Layout");
		applyPreferredLayoutTaskFactoryProps.setProperty("toolBarGravity","7.0");
		applyPreferredLayoutTaskFactoryProps.setProperty("inToolBar","true");
		applyPreferredLayoutTaskFactoryProps.setProperty("menuGravity","5.0");
		applyPreferredLayoutTaskFactoryProps.setProperty("tooltip","Apply Preferred Layout");
		registerService(bc,applyPreferredLayoutTaskFactory,NetworkViewTaskFactory.class, applyPreferredLayoutTaskFactoryProps);

		Properties deleteColumnTaskFactoryProps = new Properties();
		deleteColumnTaskFactoryProps.setProperty("title","Delete column");
		registerService(bc,deleteColumnTaskFactory,TableColumnTaskFactory.class, deleteColumnTaskFactoryProps);

		Properties renameColumnTaskFactoryProps = new Properties();
		renameColumnTaskFactoryProps.setProperty("title","Rename column");
		registerService(bc,renameColumnTaskFactory,TableColumnTaskFactory.class, renameColumnTaskFactoryProps);

		Properties copyValueToEntireColumnTaskFactoryProps = new Properties();
		copyValueToEntireColumnTaskFactoryProps.setProperty("title","Copy to entire column");
		registerService(bc,copyValueToEntireColumnTaskFactory,TableCellTaskFactory.class, copyValueToEntireColumnTaskFactoryProps);
		registerService(bc,deleteTableTaskFactory,TableTaskFactory.class, new Properties());

		registerAllServices(bc,bioGridPreprocessor, new Properties());

		Properties connectSelectedNodesTaskFactoryProps = new Properties();
		connectSelectedNodesTaskFactoryProps.setProperty("preferredMenu","Edit");
		connectSelectedNodesTaskFactoryProps.setProperty("enableFor","network");
		connectSelectedNodesTaskFactoryProps.setProperty("toolBarGravity","2.5");
		connectSelectedNodesTaskFactoryProps.setProperty("title","Connect Selected Nodes");
		registerService(bc,connectSelectedNodesTaskFactory,TaskFactory.class, connectSelectedNodesTaskFactoryProps);

		registerServiceListener(bc,importTaskUtil,"addProcessor","removeProcessor",InteractionFilePreprocessor.class);
		registerServiceListener(bc,subnetworkBuilderUtil,"addProcessor","removeProcessor",InteractionFilePreprocessor.class);
		registerServiceListener(bc,subnetworkBuilderUtil,"addFactory","removeFactory",VisualMappingFunctionFactory.class);


		Properties groupNodesTaskFactoryProps = new Properties();
		groupNodesTaskFactoryProps.setProperty("title","Group Nodes");
		groupNodesTaskFactoryProps.setProperty("tooltip","Group Selected Nodes Together");
		groupNodesTaskFactoryProps.setProperty("preferredAction", "NEW");
		registerService(bc,groupNodesTaskFactory,NetworkViewTaskFactory.class, groupNodesTaskFactoryProps);

		Properties collapseGroupTaskFactoryProps = new Properties();
		collapseGroupTaskFactoryProps.setProperty("title","Collapse Group");
		collapseGroupTaskFactoryProps.setProperty("tooltip","Collapse Grouped Nodes");
		collapseGroupTaskFactoryProps.setProperty("preferredAction", "NEW");
		registerService(bc,collapseGroupTaskFactory,NodeViewTaskFactory.class, collapseGroupTaskFactoryProps);

		Properties expandGroupTaskFactoryProps = new Properties();
		expandGroupTaskFactoryProps.setProperty("title","Expand Group");
		expandGroupTaskFactoryProps.setProperty("tooltip","Expand Group");
		expandGroupTaskFactoryProps.setProperty("preferredAction", "NEW");
		registerService(bc,expandGroupTaskFactory,NodeViewTaskFactory.class, expandGroupTaskFactoryProps);

		//ShowWelcomeScreenTask ws = new ShowWelcomeScreenTask();
		//registerAllServices(bc, ws, new Properties());
		
	}
}

