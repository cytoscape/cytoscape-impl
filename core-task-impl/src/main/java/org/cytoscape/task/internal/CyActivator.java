
package org.cytoscape.task.internal;

import static org.cytoscape.work.ServiceProperties.ACCELERATOR;
import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.ID;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.IN_NETWORK_PANEL_CONTEXT_MENU;
import static org.cytoscape.work.ServiceProperties.IN_TOOL_BAR;
import static org.cytoscape.work.ServiceProperties.LARGE_ICON_URL;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_ACTION;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;
import static org.cytoscape.work.ServiceProperties.TOOLTIP;
import static org.cytoscape.work.ServiceProperties.TOOL_BAR_GRAVITY;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
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
import org.cytoscape.model.events.NetworkAddedListener;
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
import org.cytoscape.task.create.CloneNetworkTaskFactory;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.task.create.NewEmptyNetworkViewFactory;
import org.cytoscape.task.create.NewNetworkSelectedNodesAndEdgesTaskFatory;
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
import org.cytoscape.task.edit.MapTableToNetworkTablesTaskFactory;
import org.cytoscape.task.edit.MapGlobalToLocalTableTaskFactory;
import org.cytoscape.task.edit.RenameColumnTaskFactory;
import org.cytoscape.task.edit.UnGroupNodesTaskFactory;
import org.cytoscape.task.edit.UnGroupTaskFactory;
import org.cytoscape.task.hide.HideSelectedEdgesTaskFactory;
import org.cytoscape.task.hide.HideSelectedNodesTaskFactory;
import org.cytoscape.task.hide.HideSelectedTaskFactory;
import org.cytoscape.task.hide.UnHideAllEdgesTaskFactory;
import org.cytoscape.task.hide.UnHideAllNodesTaskFactory;
import org.cytoscape.task.hide.UnHideAllTaskFactory;
import org.cytoscape.task.internal.creation.CloneNetworkTaskFactoryImpl;
import org.cytoscape.task.internal.creation.CreateNetworkViewTaskFactoryImpl;
import org.cytoscape.task.internal.creation.NewEmptyNetworkTaskFactoryImpl;
import org.cytoscape.task.internal.creation.NewNetworkSelectedNodesEdgesTaskFactoryImpl;
import org.cytoscape.task.internal.creation.NewNetworkSelectedNodesOnlyTaskFactoryImpl;
import org.cytoscape.task.internal.destruction.DestroyNetworkTaskFactoryImpl;
import org.cytoscape.task.internal.destruction.DestroyNetworkViewTaskFactoryImpl;
import org.cytoscape.task.internal.edit.ConnectSelectedNodesTaskFactoryImpl;
import org.cytoscape.task.internal.export.graphics.ExportNetworkImageTaskFactoryImpl;
import org.cytoscape.task.internal.export.network.ExportNetworkViewTaskFactoryImpl;
import org.cytoscape.task.internal.export.table.ExportSelectedTableTaskFactoryImpl;
import org.cytoscape.task.internal.export.table.ExportTableTaskFactoryImpl;
import org.cytoscape.task.internal.export.vizmap.ExportVizmapTaskFactoryImpl;
import org.cytoscape.task.internal.group.GroupNodeContextTaskFactoryImpl;
import org.cytoscape.task.internal.group.GroupNodesTaskFactoryImpl;
import org.cytoscape.task.internal.group.UnGroupNodesTaskFactoryImpl;
import org.cytoscape.task.internal.hide.HideSelectedEdgesTaskFactoryImpl;
import org.cytoscape.task.internal.hide.HideSelectedNodesTaskFactoryImpl;
import org.cytoscape.task.internal.hide.HideSelectedTaskFactoryImpl;
import org.cytoscape.task.internal.hide.UnHideAllEdgesTaskFactoryImpl;
import org.cytoscape.task.internal.hide.UnHideAllNodesTaskFactoryImpl;
import org.cytoscape.task.internal.hide.UnHideAllTaskFactoryImpl;
import org.cytoscape.task.internal.layout.ApplyPreferredLayoutTaskFactoryImpl;
import org.cytoscape.task.internal.loaddatatable.LoadAttributesFileTaskFactoryImpl;
import org.cytoscape.task.internal.loaddatatable.LoadAttributesURLTaskFactoryImpl;
import org.cytoscape.task.internal.loadnetwork.LoadNetworkFileTaskFactoryImpl;
import org.cytoscape.task.internal.loadnetwork.LoadNetworkURLTaskFactoryImpl;
import org.cytoscape.task.internal.loadvizmap.LoadVizmapFileTaskFactoryImpl;
import org.cytoscape.task.internal.networkobjects.DeleteSelectedNodesAndEdgesTaskFactoryImpl;
import org.cytoscape.task.internal.proxysettings.ProxySettingsTaskFactoryImpl;
import org.cytoscape.task.internal.select.DeselectAllEdgesTaskFactoryImpl;
import org.cytoscape.task.internal.select.DeselectAllNodesTaskFactoryImpl;
import org.cytoscape.task.internal.select.DeselectAllTaskFactoryImpl;
import org.cytoscape.task.internal.select.InvertSelectedEdgesTaskFactoryImpl;
import org.cytoscape.task.internal.select.InvertSelectedNodesTaskFactoryImpl;
import org.cytoscape.task.internal.select.SelectAdjacentEdgesTaskFactoryImpl;
import org.cytoscape.task.internal.select.SelectAllEdgesTaskFactoryImpl;
import org.cytoscape.task.internal.select.SelectAllNodesTaskFactoryImpl;
import org.cytoscape.task.internal.select.SelectAllTaskFactoryImpl;
import org.cytoscape.task.internal.select.SelectConnectedNodesTaskFactoryImpl;
import org.cytoscape.task.internal.select.SelectFirstNeighborsNodeViewTaskFactoryImpl;
import org.cytoscape.task.internal.select.SelectFirstNeighborsTaskFactoryImpl;
import org.cytoscape.task.internal.select.SelectFromFileListTaskFactoryImpl;
import org.cytoscape.task.internal.session.NewSessionTaskFactoryImpl;
import org.cytoscape.task.internal.session.OpenSessionTaskFactoryImpl;
import org.cytoscape.task.internal.session.SaveSessionAsTaskFactoryImpl;
import org.cytoscape.task.internal.session.SaveSessionTaskFactoryImpl;
import org.cytoscape.task.internal.table.CopyValueToEntireColumnTaskFactoryImpl;
import org.cytoscape.task.internal.table.DeleteColumnTaskFactoryImpl;
import org.cytoscape.task.internal.table.DeleteTableTaskFactoryImpl;
import org.cytoscape.task.internal.table.MapTableToNetworkTablesTaskFactoryImpl;
import org.cytoscape.task.internal.table.MapGlobalToLocalTableTaskFactoryImpl;
import org.cytoscape.task.internal.table.RenameColumnTaskFactoryImpl;
import org.cytoscape.task.internal.table.UpdateAddedNetworkAttributes;
import org.cytoscape.task.internal.title.EditNetworkTitleTaskFactoryImpl;
import org.cytoscape.task.internal.vizmap.ApplyVisualStyleTaskFactoryimpl;
import org.cytoscape.task.internal.zoom.FitContentTaskFactory;
import org.cytoscape.task.internal.zoom.FitSelectedTaskFactory;
import org.cytoscape.task.internal.zoom.ZoomInTaskFactory;
import org.cytoscape.task.internal.zoom.ZoomOutTaskFactory;
import org.cytoscape.task.read.LoadNetworkFileTaskFactory;
import org.cytoscape.task.read.LoadNetworkURLTaskFactory;
import org.cytoscape.task.read.LoadTableFileTaskFactory;
import org.cytoscape.task.read.LoadTableURLTaskFactory;
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
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;


public class CyActivator extends AbstractCyActivator {
	
	public CyActivator() {
		super();
	}


	public void start(BundleContext bc) {

		CyEventHelper cyEventHelperRef = getService(bc,CyEventHelper.class);
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
		StreamUtil streamUtilRef = getService(bc,StreamUtil.class);
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
		CyLayoutAlgorithmManager cyLayoutsServiceRef = getService(bc,CyLayoutAlgorithmManager.class);
		CyTableWriterManager cyTableWriterManagerRef = getService(bc,CyTableWriterManager.class);
		SynchronousTaskManager<?> synchronousTaskManagerServiceRef = getService(bc,SynchronousTaskManager.class);
		TunableSetter tunableSetterServiceRef = getService(bc,TunableSetter.class);
		
		LoadAttributesFileTaskFactoryImpl loadAttrsFileTaskFactory = new LoadAttributesFileTaskFactoryImpl(cyDataTableReaderManagerServiceRef, tunableSetterServiceRef,cyNetworkManagerServiceRef, cyTableManagerServiceRef);
		LoadAttributesURLTaskFactoryImpl loadAttrsURLTaskFactory = new LoadAttributesURLTaskFactoryImpl(cyDataTableReaderManagerServiceRef, tunableSetterServiceRef, cyNetworkManagerServiceRef, cyTableManagerServiceRef);
		
		
		CyGroupManager cyGroupManager = getService(bc, CyGroupManager.class);
		CyGroupFactory cyGroupFactory = getService(bc, CyGroupFactory.class);
		
		LoadVizmapFileTaskFactoryImpl loadVizmapFileTaskFactory = new LoadVizmapFileTaskFactoryImpl(vizmapReaderManagerServiceRef,visualMappingManagerServiceRef,synchronousTaskManagerServiceRef, tunableSetterServiceRef);

		LoadNetworkFileTaskFactoryImpl loadNetworkFileTaskFactory = new LoadNetworkFileTaskFactoryImpl(cyNetworkReaderManagerServiceRef,cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef,cyPropertyServiceRef,cyNetworkNamingServiceRef, tunableSetterServiceRef);
		LoadNetworkURLTaskFactoryImpl loadNetworkURLTaskFactory = new LoadNetworkURLTaskFactoryImpl(cyNetworkReaderManagerServiceRef,cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef,cyPropertyServiceRef,cyNetworkNamingServiceRef,streamUtilRef, synchronousTaskManagerServiceRef, tunableSetterServiceRef);

		DeleteSelectedNodesAndEdgesTaskFactoryImpl deleteSelectedNodesAndEdgesTaskFactory = new DeleteSelectedNodesAndEdgesTaskFactoryImpl(undoSupportServiceRef,cyNetworkViewManagerServiceRef,visualMappingManagerServiceRef,cyEventHelperRef);
		SelectAllTaskFactoryImpl selectAllTaskFactory = new SelectAllTaskFactoryImpl(undoSupportServiceRef,cyNetworkViewManagerServiceRef,cyEventHelperRef);
		SelectAllEdgesTaskFactoryImpl selectAllEdgesTaskFactory = new SelectAllEdgesTaskFactoryImpl(undoSupportServiceRef,cyNetworkViewManagerServiceRef,cyEventHelperRef);
		SelectAllNodesTaskFactoryImpl selectAllNodesTaskFactory = new SelectAllNodesTaskFactoryImpl(undoSupportServiceRef,cyNetworkViewManagerServiceRef,cyEventHelperRef);
		SelectAdjacentEdgesTaskFactoryImpl selectAdjacentEdgesTaskFactory = new SelectAdjacentEdgesTaskFactoryImpl(undoSupportServiceRef,cyNetworkViewManagerServiceRef,cyEventHelperRef);
		SelectConnectedNodesTaskFactoryImpl selectConnectedNodesTaskFactory = new SelectConnectedNodesTaskFactoryImpl(undoSupportServiceRef,cyNetworkViewManagerServiceRef,cyEventHelperRef);
		
		SelectFirstNeighborsTaskFactoryImpl selectFirstNeighborsTaskFactory = new SelectFirstNeighborsTaskFactoryImpl(undoSupportServiceRef,cyNetworkViewManagerServiceRef,cyEventHelperRef, CyEdge.Type.ANY);
		SelectFirstNeighborsTaskFactoryImpl selectFirstNeighborsTaskFactoryInEdge = new SelectFirstNeighborsTaskFactoryImpl(undoSupportServiceRef,cyNetworkViewManagerServiceRef,cyEventHelperRef, CyEdge.Type.INCOMING);
		SelectFirstNeighborsTaskFactoryImpl selectFirstNeighborsTaskFactoryOutEdge = new SelectFirstNeighborsTaskFactoryImpl(undoSupportServiceRef,cyNetworkViewManagerServiceRef,cyEventHelperRef, CyEdge.Type.OUTGOING);
		
		
		DeselectAllTaskFactoryImpl deselectAllTaskFactory = new DeselectAllTaskFactoryImpl(undoSupportServiceRef,cyNetworkViewManagerServiceRef,cyEventHelperRef);
		DeselectAllEdgesTaskFactoryImpl deselectAllEdgesTaskFactory = new DeselectAllEdgesTaskFactoryImpl(undoSupportServiceRef,cyNetworkViewManagerServiceRef,cyEventHelperRef);
		DeselectAllNodesTaskFactoryImpl deselectAllNodesTaskFactory = new DeselectAllNodesTaskFactoryImpl(undoSupportServiceRef,cyNetworkViewManagerServiceRef,cyEventHelperRef);
		InvertSelectedEdgesTaskFactoryImpl invertSelectedEdgesTaskFactory = new InvertSelectedEdgesTaskFactoryImpl(undoSupportServiceRef,cyNetworkViewManagerServiceRef,cyEventHelperRef);
		InvertSelectedNodesTaskFactoryImpl invertSelectedNodesTaskFactory = new InvertSelectedNodesTaskFactoryImpl(undoSupportServiceRef,cyNetworkViewManagerServiceRef,cyEventHelperRef);
		SelectFromFileListTaskFactoryImpl selectFromFileListTaskFactory = new SelectFromFileListTaskFactoryImpl(undoSupportServiceRef,cyNetworkViewManagerServiceRef,cyEventHelperRef, tunableSetterServiceRef);
		
		SelectFirstNeighborsNodeViewTaskFactoryImpl selectFirstNeighborsNodeViewTaskFactory = new SelectFirstNeighborsNodeViewTaskFactoryImpl(CyEdge.Type.ANY);
		
		HideSelectedTaskFactoryImpl hideSelectedTaskFactory = new HideSelectedTaskFactoryImpl(undoSupportServiceRef,cyEventHelperRef,visualMappingManagerServiceRef);
		HideSelectedNodesTaskFactoryImpl hideSelectedNodesTaskFactory = new HideSelectedNodesTaskFactoryImpl(undoSupportServiceRef,cyEventHelperRef,visualMappingManagerServiceRef);
		HideSelectedEdgesTaskFactoryImpl hideSelectedEdgesTaskFactory = new HideSelectedEdgesTaskFactoryImpl(undoSupportServiceRef,cyEventHelperRef,visualMappingManagerServiceRef);
		UnHideAllTaskFactoryImpl unHideAllTaskFactory = new UnHideAllTaskFactoryImpl(undoSupportServiceRef,cyEventHelperRef,visualMappingManagerServiceRef);
		UnHideAllNodesTaskFactoryImpl unHideAllNodesTaskFactory = new UnHideAllNodesTaskFactoryImpl(undoSupportServiceRef,cyEventHelperRef,visualMappingManagerServiceRef);
		UnHideAllEdgesTaskFactoryImpl unHideAllEdgesTaskFactory = new UnHideAllEdgesTaskFactoryImpl(undoSupportServiceRef,cyEventHelperRef,visualMappingManagerServiceRef);
		NewEmptyNetworkTaskFactoryImpl newEmptyNetworkTaskFactory = new NewEmptyNetworkTaskFactoryImpl(cyNetworkFactoryServiceRef,cyNetworkViewFactoryServiceRef,cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef,cyNetworkNamingServiceRef,synchronousTaskManagerServiceRef,cyApplicationManagerServiceRef);
		CloneNetworkTaskFactoryImpl cloneNetworkTaskFactory = new CloneNetworkTaskFactoryImpl(cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef,visualMappingManagerServiceRef,cyNetworkFactoryServiceRef,cyNetworkViewFactoryServiceRef,cyNetworkNamingServiceRef,cyApplicationManagerServiceRef);
		NewNetworkSelectedNodesEdgesTaskFactoryImpl newNetworkSelectedNodesEdgesTaskFactory = new NewNetworkSelectedNodesEdgesTaskFactoryImpl(undoSupportServiceRef,cyRootNetworkFactoryServiceRef,cyNetworkViewFactoryServiceRef,cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef,cyNetworkNamingServiceRef,visualMappingManagerServiceRef,cyApplicationManagerServiceRef,cyEventHelperRef);
		NewNetworkSelectedNodesOnlyTaskFactoryImpl newNetworkSelectedNodesOnlyTaskFactory = new NewNetworkSelectedNodesOnlyTaskFactoryImpl(undoSupportServiceRef,cyRootNetworkFactoryServiceRef,cyNetworkViewFactoryServiceRef,cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef,cyNetworkNamingServiceRef,visualMappingManagerServiceRef,cyApplicationManagerServiceRef,cyEventHelperRef);
		DestroyNetworkTaskFactoryImpl destroyNetworkTaskFactory = new DestroyNetworkTaskFactoryImpl(cyNetworkManagerServiceRef);
		DestroyNetworkViewTaskFactoryImpl destroyNetworkViewTaskFactory = new DestroyNetworkViewTaskFactoryImpl(cyNetworkViewManagerServiceRef);
		ZoomInTaskFactory zoomInTaskFactory = new ZoomInTaskFactory(undoSupportServiceRef);
		ZoomOutTaskFactory zoomOutTaskFactory = new ZoomOutTaskFactory(undoSupportServiceRef);
		FitSelectedTaskFactory fitSelectedTaskFactory = new FitSelectedTaskFactory(undoSupportServiceRef);
		FitContentTaskFactory fitContentTaskFactory = new FitContentTaskFactory(undoSupportServiceRef);
		NewSessionTaskFactoryImpl newSessionTaskFactory = new NewSessionTaskFactoryImpl(cySessionManagerServiceRef, tunableSetterServiceRef);
		OpenSessionTaskFactoryImpl openSessionTaskFactory = new OpenSessionTaskFactoryImpl(cySessionManagerServiceRef,sessionReaderManagerServiceRef,cyApplicationManagerServiceRef,recentlyOpenedTrackerServiceRef, synchronousTaskManagerServiceRef, tunableSetterServiceRef);
		SaveSessionTaskFactoryImpl saveSessionTaskFactory = new SaveSessionTaskFactoryImpl( sessionWriterManagerServiceRef, cySessionManagerServiceRef, recentlyOpenedTrackerServiceRef, cyEventHelperRef);
		SaveSessionAsTaskFactoryImpl saveSessionAsTaskFactory = new SaveSessionAsTaskFactoryImpl( sessionWriterManagerServiceRef, cySessionManagerServiceRef, recentlyOpenedTrackerServiceRef, cyEventHelperRef, tunableSetterServiceRef);
		ProxySettingsTaskFactoryImpl proxySettingsTaskFactory = new ProxySettingsTaskFactoryImpl(cyPropertyServiceRef, streamUtilRef);
		EditNetworkTitleTaskFactoryImpl editNetworkTitleTaskFactory = new EditNetworkTitleTaskFactoryImpl(undoSupportServiceRef, cyNetworkManagerServiceRef, cyNetworkNamingServiceRef, tunableSetterServiceRef);
		CreateNetworkViewTaskFactoryImpl createNetworkViewTaskFactory = new CreateNetworkViewTaskFactoryImpl(undoSupportServiceRef,cyNetworkViewFactoryServiceRef,cyNetworkViewManagerServiceRef,cyLayoutsServiceRef,cyEventHelperRef);
		ExportNetworkImageTaskFactoryImpl exportNetworkImageTaskFactory = new ExportNetworkImageTaskFactoryImpl(viewWriterManagerServiceRef,cyApplicationManagerServiceRef);
		ExportNetworkViewTaskFactoryImpl exportNetworkViewTaskFactory = new ExportNetworkViewTaskFactoryImpl(networkViewWriterManagerServiceRef, tunableSetterServiceRef);
		ExportSelectedTableTaskFactoryImpl exportCurrentTableTaskFactory = new ExportSelectedTableTaskFactoryImpl(cyTableWriterManagerRef, cyTableManagerServiceRef, cyNetworkManagerServiceRef);
		ApplyPreferredLayoutTaskFactoryImpl applyPreferredLayoutTaskFactory = new ApplyPreferredLayoutTaskFactoryImpl(cyLayoutsServiceRef,cyPropertyServiceRef);
		DeleteColumnTaskFactoryImpl deleteColumnTaskFactory = new DeleteColumnTaskFactoryImpl(undoSupportServiceRef);
		RenameColumnTaskFactoryImpl renameColumnTaskFactory = new RenameColumnTaskFactoryImpl(undoSupportServiceRef, tunableSetterServiceRef);
		CopyValueToEntireColumnTaskFactoryImpl copyValueToEntireColumnTaskFactory = new CopyValueToEntireColumnTaskFactoryImpl(undoSupportServiceRef);
		DeleteTableTaskFactoryImpl deleteTableTaskFactory = new DeleteTableTaskFactoryImpl(cyTableManagerServiceRef);
		ExportVizmapTaskFactoryImpl exportVizmapTaskFactory = new ExportVizmapTaskFactoryImpl(vizmapWriterManagerServiceRef,visualMappingManagerServiceRef, tunableSetterServiceRef);

		ConnectSelectedNodesTaskFactoryImpl connectSelectedNodesTaskFactory = new ConnectSelectedNodesTaskFactoryImpl(undoSupportServiceRef,cyEventHelperRef);
		
		MapGlobalToLocalTableTaskFactoryImpl mapGlobal = new MapGlobalToLocalTableTaskFactoryImpl(cyTableManagerServiceRef, cyNetworkManagerServiceRef, tunableSetterServiceRef);
		
		
		// Apply Visual Style Task
		ApplyVisualStyleTaskFactoryimpl applyVisualStyleTaskFactory = new ApplyVisualStyleTaskFactoryimpl(visualMappingManagerServiceRef);
		Properties applyVisualStyleProps = new Properties();
		applyVisualStyleProps.setProperty(ID,"applyVisualStyleTaskFactory");
		applyVisualStyleProps.setProperty(TITLE, "Apply Visual Style");
		applyVisualStyleProps.setProperty(COMMAND,"apply-visualstyle");
		applyVisualStyleProps.setProperty(COMMAND_NAMESPACE,"vizmap");
		applyVisualStyleProps.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU,"true");
		applyVisualStyleProps.setProperty(ENABLE_FOR,"networkAndView");
		
		registerService(bc, applyVisualStyleTaskFactory, NetworkViewCollectionTaskFactory.class, applyVisualStyleProps);
		registerService(bc, applyVisualStyleTaskFactory, ApplyVisualStyleTaskFactory.class, applyVisualStyleProps);
		
		
		Properties mapGlobalProps = new Properties();
	/*	mapGlobalProps.setProperty(ID,"mapGlobalToLocalTableTaskFactory");
		mapGlobalProps.setProperty(PREFERRED_MENU,"Tools");
		mapGlobalProps.setProperty(ACCELERATOR,"cmd m");
		mapGlobalProps.setProperty(TITLE, "Map Table to Attributes");
		mapGlobalProps.setProperty(MENU_GRAVITY,"1.0");
		mapGlobalProps.setProperty(TOOL_BAR_GRAVITY,"3.0");
		mapGlobalProps.setProperty(IN_TOOL_BAR,"false");
		mapGlobalProps.setProperty(COMMAND,"map-global-to-local");
		mapGlobalProps.setProperty(COMMAND_NAMESPACE,"table");
	*/	registerService(bc, mapGlobal, TableTaskFactory.class, mapGlobalProps);
		registerService(bc, mapGlobal, MapGlobalToLocalTableTaskFactory.class, mapGlobalProps);
	
		Properties loadNetworkFileTaskFactoryProps = new Properties();
		loadNetworkFileTaskFactoryProps.setProperty(ID,"loadNetworkFileTaskFactory");
		loadNetworkFileTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Import.Network");
		loadNetworkFileTaskFactoryProps.setProperty(ACCELERATOR,"cmd l");
		loadNetworkFileTaskFactoryProps.setProperty(TITLE,"File...");
		loadNetworkFileTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		loadNetworkFileTaskFactoryProps.setProperty(COMMAND,"load-file");
		loadNetworkFileTaskFactoryProps.setProperty(MENU_GRAVITY,"1.0");
		loadNetworkFileTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"3.0");
		loadNetworkFileTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/net_file_import.png").toString());
		loadNetworkFileTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		loadNetworkFileTaskFactoryProps.setProperty(TOOLTIP,"Import Network From File");
		registerService(bc, loadNetworkFileTaskFactory, TaskFactory.class, loadNetworkFileTaskFactoryProps);
		registerService(bc, loadNetworkFileTaskFactory, LoadNetworkFileTaskFactory.class, loadNetworkFileTaskFactoryProps);

		Properties loadNetworkURLTaskFactoryProps = new Properties();
		loadNetworkURLTaskFactoryProps.setProperty(ID,"loadNetworkURLTaskFactory");
		loadNetworkURLTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Import.Network");
		loadNetworkURLTaskFactoryProps.setProperty(ACCELERATOR,"cmd shift l");
		loadNetworkURLTaskFactoryProps.setProperty(MENU_GRAVITY,"2.0");
		loadNetworkURLTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"3.1");
		loadNetworkURLTaskFactoryProps.setProperty(TITLE,"URL...");
		loadNetworkURLTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/net_url_import.png").toString());
		loadNetworkURLTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		loadNetworkURLTaskFactoryProps.setProperty(TOOLTIP,"Import Network From URL");
		loadNetworkURLTaskFactoryProps.setProperty(COMMAND,"load-url");
		loadNetworkURLTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc, loadNetworkURLTaskFactory, TaskFactory.class, loadNetworkURLTaskFactoryProps);
		registerService(bc, loadNetworkURLTaskFactory, LoadNetworkURLTaskFactory.class, loadNetworkURLTaskFactoryProps);

		Properties loadVizmapFileTaskFactoryProps = new Properties();
		loadVizmapFileTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Import");
		loadVizmapFileTaskFactoryProps.setProperty(MENU_GRAVITY,"3.0");
		loadVizmapFileTaskFactoryProps.setProperty(TITLE,"Vizmap File...");
		loadVizmapFileTaskFactoryProps.setProperty(COMMAND,"load-file");
		loadVizmapFileTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"vizmap");
		registerService(bc,loadVizmapFileTaskFactory,TaskFactory.class, loadVizmapFileTaskFactoryProps);
		registerService(bc,loadVizmapFileTaskFactory,LoadVizmapFileTaskFactory.class, new Properties());

		Properties loadAttrsFileTaskFactoryProps = new Properties();
		loadAttrsFileTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Import.Table");
		loadAttrsFileTaskFactoryProps.setProperty(MENU_GRAVITY,"1.1");
		loadAttrsFileTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"3.2");
		loadAttrsFileTaskFactoryProps.setProperty(TITLE,"File...");
		loadAttrsFileTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/table_file_import.png").toString());
		loadAttrsFileTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		loadAttrsFileTaskFactoryProps.setProperty(TOOLTIP,"Import Table From File");
		loadAttrsFileTaskFactoryProps.setProperty(COMMAND,"load-file");
		loadAttrsFileTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"table");
		registerService(bc,loadAttrsFileTaskFactory,TaskFactory.class, loadAttrsFileTaskFactoryProps);
		registerService(bc,loadAttrsFileTaskFactory,LoadTableFileTaskFactory.class, loadAttrsFileTaskFactoryProps);


		Properties loadAttrsURLTaskFactoryProps = new Properties();
		loadAttrsURLTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Import.Table");
		loadAttrsURLTaskFactoryProps.setProperty(MENU_GRAVITY,"1.2");
		loadAttrsURLTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"3.3");
		loadAttrsURLTaskFactoryProps.setProperty(TITLE,"URL...");
		loadAttrsURLTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/table_url_import.png").toString());
		loadAttrsURLTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		loadAttrsURLTaskFactoryProps.setProperty(TOOLTIP,"Import Table From URL");
		loadAttrsURLTaskFactoryProps.setProperty(COMMAND,"load-url");
		loadAttrsURLTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"table");
		registerService(bc,loadAttrsURLTaskFactory,TaskFactory.class, loadAttrsURLTaskFactoryProps);
		registerService(bc,loadAttrsURLTaskFactory,LoadTableURLTaskFactory.class, loadAttrsURLTaskFactoryProps);

		Properties proxySettingsTaskFactoryProps = new Properties();
		proxySettingsTaskFactoryProps.setProperty(PREFERRED_MENU,"Edit.Preferences");
		proxySettingsTaskFactoryProps.setProperty(MENU_GRAVITY,"1.0");
		proxySettingsTaskFactoryProps.setProperty(TITLE,"Proxy Settings...");
		registerService(bc,proxySettingsTaskFactory,TaskFactory.class, proxySettingsTaskFactoryProps);

		Properties deleteSelectedNodesAndEdgesTaskFactoryProps = new Properties();
		deleteSelectedNodesAndEdgesTaskFactoryProps.setProperty(PREFERRED_MENU,"Edit");
		deleteSelectedNodesAndEdgesTaskFactoryProps.setProperty(ENABLE_FOR,"selectedNodesOrEdges");
		deleteSelectedNodesAndEdgesTaskFactoryProps.setProperty(TITLE,"Delete Selected Nodes and Edges...");
		deleteSelectedNodesAndEdgesTaskFactoryProps.setProperty(MENU_GRAVITY,"5.0");
		deleteSelectedNodesAndEdgesTaskFactoryProps.setProperty(ACCELERATOR,"DELETE");
		deleteSelectedNodesAndEdgesTaskFactoryProps.setProperty(COMMAND,"delete-selected-nodes-and-edges");
		deleteSelectedNodesAndEdgesTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,deleteSelectedNodesAndEdgesTaskFactory,NetworkTaskFactory.class, deleteSelectedNodesAndEdgesTaskFactoryProps);
		registerService(bc,deleteSelectedNodesAndEdgesTaskFactory,DeleteSelectedNodesAndEdgesTaskFactory.class, deleteSelectedNodesAndEdgesTaskFactoryProps);

		Properties selectAllTaskFactoryProps = new Properties();
		selectAllTaskFactoryProps.setProperty(PREFERRED_MENU,"Select");
		selectAllTaskFactoryProps.setProperty(ACCELERATOR,"cmd alt a");
		selectAllTaskFactoryProps.setProperty(ENABLE_FOR,"network");
		selectAllTaskFactoryProps.setProperty(TITLE,"Select all nodes and edges");
		selectAllTaskFactoryProps.setProperty(MENU_GRAVITY,"5.0");
		selectAllTaskFactoryProps.setProperty(COMMAND,"select-all-nodes-and-edges");
		selectAllTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,selectAllTaskFactory,NetworkTaskFactory.class, selectAllTaskFactoryProps);
		registerService(bc,selectAllTaskFactory,SelectAllTaskFactory.class, selectAllTaskFactoryProps);

		Properties selectAllEdgesTaskFactoryProps = new Properties();
		selectAllEdgesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Edges");
		selectAllEdgesTaskFactoryProps.setProperty(ACCELERATOR,"cmd alt a");
		selectAllEdgesTaskFactoryProps.setProperty(ENABLE_FOR,"network");
		selectAllEdgesTaskFactoryProps.setProperty(TITLE,"Select all edges");
		selectAllEdgesTaskFactoryProps.setProperty(MENU_GRAVITY,"4.0");
		selectAllEdgesTaskFactoryProps.setProperty(COMMAND,"select-all-edges");
		selectAllEdgesTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,selectAllEdgesTaskFactory,NetworkTaskFactory.class, selectAllEdgesTaskFactoryProps);
		registerService(bc,selectAllEdgesTaskFactory,SelectAllEdgesTaskFactory.class, selectAllEdgesTaskFactoryProps);

		Properties selectAllNodesTaskFactoryProps = new Properties();
		selectAllNodesTaskFactoryProps.setProperty(ENABLE_FOR,"network");
		selectAllNodesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Nodes");
		selectAllNodesTaskFactoryProps.setProperty(MENU_GRAVITY,"4.0");
		selectAllNodesTaskFactoryProps.setProperty(ACCELERATOR,"cmd a");
		selectAllNodesTaskFactoryProps.setProperty(TITLE,"Select all nodes");
		selectAllNodesTaskFactoryProps.setProperty(COMMAND,"select-all-nodes");
		selectAllNodesTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,selectAllNodesTaskFactory,NetworkTaskFactory.class, selectAllNodesTaskFactoryProps);
		registerService(bc,selectAllNodesTaskFactory,SelectAllNodesTaskFactory.class, selectAllNodesTaskFactoryProps);

		Properties selectAdjacentEdgesTaskFactoryProps = new Properties();
		selectAdjacentEdgesTaskFactoryProps.setProperty(ENABLE_FOR,"network");
		selectAdjacentEdgesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Edges");
		selectAdjacentEdgesTaskFactoryProps.setProperty(MENU_GRAVITY,"6.0");
		selectAdjacentEdgesTaskFactoryProps.setProperty(ACCELERATOR,"alt e");
		selectAdjacentEdgesTaskFactoryProps.setProperty(TITLE,"Select adjacent edges");
		selectAdjacentEdgesTaskFactoryProps.setProperty(COMMAND,"select-adjacent-edges");
		selectAdjacentEdgesTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,selectAdjacentEdgesTaskFactory,NetworkTaskFactory.class, selectAdjacentEdgesTaskFactoryProps);
		registerService(bc,selectAdjacentEdgesTaskFactory,SelectAdjacentEdgesTaskFactory.class, selectAdjacentEdgesTaskFactoryProps);

		Properties selectConnectedNodesTaskFactoryProps = new Properties();
		selectConnectedNodesTaskFactoryProps.setProperty(ENABLE_FOR,"network");
		selectConnectedNodesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Nodes");
		selectConnectedNodesTaskFactoryProps.setProperty(MENU_GRAVITY,"7.0");
		selectConnectedNodesTaskFactoryProps.setProperty(ACCELERATOR,"cmd 7");
		selectConnectedNodesTaskFactoryProps.setProperty(TITLE,"Nodes connected by selected edges");
		selectConnectedNodesTaskFactoryProps.setProperty(COMMAND,"select-nodes-connected-by-selected-edges");
		selectConnectedNodesTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,selectConnectedNodesTaskFactory,NetworkTaskFactory.class, selectConnectedNodesTaskFactoryProps);
		registerService(bc,selectConnectedNodesTaskFactory,SelectConnectedNodesTaskFactory.class, selectConnectedNodesTaskFactoryProps);

		Properties selectFirstNeighborsTaskFactoryProps = new Properties();
		selectFirstNeighborsTaskFactoryProps.setProperty(ENABLE_FOR,"selectedNodesOrEdges");
		selectFirstNeighborsTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Nodes.First Neighbors of Selected Nodes");
		selectFirstNeighborsTaskFactoryProps.setProperty(MENU_GRAVITY,"6.0");
		selectFirstNeighborsTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"9.1");
		selectFirstNeighborsTaskFactoryProps.setProperty(ACCELERATOR,"cmd 6");
		selectFirstNeighborsTaskFactoryProps.setProperty(TITLE,"Undirected");
		selectFirstNeighborsTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/select_firstneighbors.png").toString());
		selectFirstNeighborsTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		selectFirstNeighborsTaskFactoryProps.setProperty(TOOLTIP,"First Neighbors of Selected Nodes (Undirected)");
		selectFirstNeighborsTaskFactoryProps.setProperty(COMMAND,"select-first-neighbors-of-selected-nodes-undirected");
		selectFirstNeighborsTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,selectFirstNeighborsTaskFactory,NetworkTaskFactory.class, selectFirstNeighborsTaskFactoryProps);
		registerService(bc,selectFirstNeighborsTaskFactory,SelectFirstNeighborsTaskFactory.class, selectFirstNeighborsTaskFactoryProps);
		Properties selectFirstNeighborsTaskFactoryInEdgeProps = new Properties();
		selectFirstNeighborsTaskFactoryInEdgeProps.setProperty(ENABLE_FOR,"network");
		selectFirstNeighborsTaskFactoryInEdgeProps.setProperty(PREFERRED_MENU,"Select.Nodes.First Neighbors of Selected Nodes");
		selectFirstNeighborsTaskFactoryInEdgeProps.setProperty(MENU_GRAVITY,"6.1");
		selectFirstNeighborsTaskFactoryInEdgeProps.setProperty(TITLE,"Directed: Incoming");
		selectFirstNeighborsTaskFactoryInEdgeProps.setProperty(TOOLTIP,"First Neighbors of Selected Nodes (Directed: Incoming)");
		selectFirstNeighborsTaskFactoryInEdgeProps.setProperty(COMMAND,"select-first-neighbors-of-selected-nodes-incoming");
		selectFirstNeighborsTaskFactoryInEdgeProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,selectFirstNeighborsTaskFactoryInEdge,NetworkTaskFactory.class, selectFirstNeighborsTaskFactoryInEdgeProps);
		registerService(bc,selectFirstNeighborsTaskFactoryInEdge,SelectFirstNeighborsTaskFactory.class, selectFirstNeighborsTaskFactoryInEdgeProps);
		Properties selectFirstNeighborsTaskFactoryOutEdgeProps = new Properties();
		selectFirstNeighborsTaskFactoryOutEdgeProps.setProperty(ENABLE_FOR,"network");
		selectFirstNeighborsTaskFactoryOutEdgeProps.setProperty(PREFERRED_MENU,"Select.Nodes.First Neighbors of Selected Nodes");
		selectFirstNeighborsTaskFactoryOutEdgeProps.setProperty(MENU_GRAVITY,"6.2");
		selectFirstNeighborsTaskFactoryOutEdgeProps.setProperty(TITLE,"Directed: Outgoing");
		selectFirstNeighborsTaskFactoryOutEdgeProps.setProperty(TOOLTIP,"First Neighbors of Selected Nodes (Directed: Outgoing)");
		selectFirstNeighborsTaskFactoryOutEdgeProps.setProperty(COMMAND,"select-first-neighbors-of-selected-nodes-outgoing");
		selectFirstNeighborsTaskFactoryOutEdgeProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,selectFirstNeighborsTaskFactoryOutEdge,NetworkTaskFactory.class, selectFirstNeighborsTaskFactoryOutEdgeProps);
		registerService(bc,selectFirstNeighborsTaskFactoryOutEdge,SelectFirstNeighborsTaskFactory.class, selectFirstNeighborsTaskFactoryOutEdgeProps);		

		Properties deselectAllTaskFactoryProps = new Properties();
		deselectAllTaskFactoryProps.setProperty(ENABLE_FOR,"network");
		deselectAllTaskFactoryProps.setProperty(PREFERRED_MENU,"Select");
		deselectAllTaskFactoryProps.setProperty(MENU_GRAVITY,"5.1");
		deselectAllTaskFactoryProps.setProperty(ACCELERATOR,"cmd shift alt a");
		deselectAllTaskFactoryProps.setProperty(TITLE,"Deselect all nodes and edges");
		deselectAllTaskFactoryProps.setProperty(COMMAND,"deselect-all");
		deselectAllTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,deselectAllTaskFactory,NetworkTaskFactory.class, deselectAllTaskFactoryProps);
		registerService(bc,deselectAllTaskFactory,DeselectAllTaskFactory.class, deselectAllTaskFactoryProps);

		Properties deselectAllEdgesTaskFactoryProps = new Properties();
		deselectAllEdgesTaskFactoryProps.setProperty(ENABLE_FOR,"network");
		deselectAllEdgesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Edges");
		deselectAllEdgesTaskFactoryProps.setProperty(MENU_GRAVITY,"5.0");
		deselectAllEdgesTaskFactoryProps.setProperty(ACCELERATOR,"alt shift a");
		deselectAllEdgesTaskFactoryProps.setProperty(TITLE,"Deselect all edges");
		deselectAllEdgesTaskFactoryProps.setProperty(COMMAND,"deselect-all-edges");
		deselectAllEdgesTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,deselectAllEdgesTaskFactory,NetworkTaskFactory.class, deselectAllEdgesTaskFactoryProps);
		registerService(bc,deselectAllEdgesTaskFactory,DeselectAllEdgesTaskFactory.class, deselectAllEdgesTaskFactoryProps);

		Properties deselectAllNodesTaskFactoryProps = new Properties();
		deselectAllNodesTaskFactoryProps.setProperty(ENABLE_FOR,"network");
		deselectAllNodesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Nodes");
		deselectAllNodesTaskFactoryProps.setProperty(MENU_GRAVITY,"5.0");
		deselectAllNodesTaskFactoryProps.setProperty(ACCELERATOR,"cmd shift a");
		deselectAllNodesTaskFactoryProps.setProperty(TITLE,"Deselect all nodes");
		deselectAllNodesTaskFactoryProps.setProperty(COMMAND,"deselect-all-nodes");
		deselectAllNodesTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,deselectAllNodesTaskFactory,NetworkTaskFactory.class, deselectAllNodesTaskFactoryProps);
		registerService(bc,deselectAllNodesTaskFactory,DeselectAllNodesTaskFactory.class, deselectAllNodesTaskFactoryProps);

		Properties invertSelectedEdgesTaskFactoryProps = new Properties();
		invertSelectedEdgesTaskFactoryProps.setProperty(ENABLE_FOR,"network");
		invertSelectedEdgesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Edges");
		invertSelectedEdgesTaskFactoryProps.setProperty(MENU_GRAVITY,"1.0");
		invertSelectedEdgesTaskFactoryProps.setProperty(ACCELERATOR,"alt i");
		invertSelectedEdgesTaskFactoryProps.setProperty(TITLE,"Invert edge selection");
		invertSelectedEdgesTaskFactoryProps.setProperty(COMMAND,"invert-selected-edges");
		invertSelectedEdgesTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,invertSelectedEdgesTaskFactory,NetworkTaskFactory.class, invertSelectedEdgesTaskFactoryProps);
		registerService(bc,invertSelectedEdgesTaskFactory,InvertSelectedEdgesTaskFactory.class, invertSelectedEdgesTaskFactoryProps);

		Properties invertSelectedNodesTaskFactoryProps = new Properties();
		invertSelectedNodesTaskFactoryProps.setProperty(ENABLE_FOR,"selectedNodes");
		invertSelectedNodesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Nodes");
		invertSelectedNodesTaskFactoryProps.setProperty(MENU_GRAVITY,"1.0");
		invertSelectedNodesTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"9.2");
		invertSelectedNodesTaskFactoryProps.setProperty(ACCELERATOR,"cmd i");
		invertSelectedNodesTaskFactoryProps.setProperty(TITLE,"Invert node selection");
		invertSelectedNodesTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/invert_selection.png").toString());
		invertSelectedNodesTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		invertSelectedNodesTaskFactoryProps.setProperty(TOOLTIP,"Invert Node Selection");
		invertSelectedNodesTaskFactoryProps.setProperty(COMMAND,"invert-selected-nodes");
		invertSelectedNodesTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,invertSelectedNodesTaskFactory,NetworkTaskFactory.class, invertSelectedNodesTaskFactoryProps);
		registerService(bc,invertSelectedNodesTaskFactory,InvertSelectedNodesTaskFactory.class, invertSelectedNodesTaskFactoryProps);

		Properties selectFromFileListTaskFactoryProps = new Properties();
		selectFromFileListTaskFactoryProps.setProperty(ENABLE_FOR,"network");
		selectFromFileListTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Nodes");
		selectFromFileListTaskFactoryProps.setProperty(MENU_GRAVITY,"8.0");
		selectFromFileListTaskFactoryProps.setProperty(ACCELERATOR,"cmd i");
		selectFromFileListTaskFactoryProps.setProperty(TITLE,"From ID List file...");
		selectFromFileListTaskFactoryProps.setProperty(COMMAND,"select-nodes-for-id-list");
		selectFromFileListTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,selectFromFileListTaskFactory,NetworkTaskFactory.class, selectFromFileListTaskFactoryProps);
		registerService(bc,selectFromFileListTaskFactory,SelectFromFileListTaskFactory.class, selectFromFileListTaskFactoryProps);

		Properties selectFirstNeighborsNodeViewTaskFactoryProps = new Properties();
		selectFirstNeighborsNodeViewTaskFactoryProps.setProperty(TITLE,"Select First Neighbors (Undirected)");
		registerService(bc,selectFirstNeighborsNodeViewTaskFactory,NodeViewTaskFactory.class, selectFirstNeighborsNodeViewTaskFactoryProps);
		registerService(bc,selectFirstNeighborsNodeViewTaskFactory,SelectFirstNeighborsNodeViewTaskFactory.class, selectFirstNeighborsNodeViewTaskFactoryProps);

		Properties hideSelectedTaskFactoryProps = new Properties();
		hideSelectedTaskFactoryProps.setProperty(ENABLE_FOR,"selectedNodesOrEdges");
		hideSelectedTaskFactoryProps.setProperty(PREFERRED_MENU,"Select");
		hideSelectedTaskFactoryProps.setProperty(MENU_GRAVITY,"3.1");
		hideSelectedTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"9.5");
		hideSelectedTaskFactoryProps.setProperty(TITLE,"Hide selected nodes and edges");
		hideSelectedTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/hide_selected.png").toString());
		hideSelectedTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		hideSelectedTaskFactoryProps.setProperty(TOOLTIP,"Hide Selected Nodes and Edges");
		hideSelectedTaskFactoryProps.setProperty(COMMAND,"hide-selected-nodes-and-edges");
		hideSelectedTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network-view");
		registerService(bc,hideSelectedTaskFactory,NetworkViewTaskFactory.class, hideSelectedTaskFactoryProps);
		registerService(bc,hideSelectedTaskFactory,HideSelectedTaskFactory.class, hideSelectedTaskFactoryProps);

		Properties hideSelectedNodesTaskFactoryProps = new Properties();
		hideSelectedNodesTaskFactoryProps.setProperty(ENABLE_FOR,"selectedNodes");
		hideSelectedNodesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Nodes");
		hideSelectedNodesTaskFactoryProps.setProperty(MENU_GRAVITY,"2.0");
		hideSelectedNodesTaskFactoryProps.setProperty(TITLE,"Hide selected nodes");
		hideSelectedNodesTaskFactoryProps.setProperty(COMMAND,"hide-selected-nodes");
		hideSelectedNodesTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network-view");
		registerService(bc,hideSelectedNodesTaskFactory,NetworkViewTaskFactory.class, hideSelectedNodesTaskFactoryProps);
		registerService(bc,hideSelectedNodesTaskFactory,HideSelectedNodesTaskFactory.class, hideSelectedNodesTaskFactoryProps);

		Properties hideSelectedEdgesTaskFactoryProps = new Properties();
		hideSelectedEdgesTaskFactoryProps.setProperty(ENABLE_FOR,"selectedEdges");
		hideSelectedEdgesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Edges");
		hideSelectedEdgesTaskFactoryProps.setProperty(MENU_GRAVITY,"2.0");
		hideSelectedEdgesTaskFactoryProps.setProperty(TITLE,"Hide selected edges");
		hideSelectedEdgesTaskFactoryProps.setProperty(COMMAND,"hide-selected-edges");
		hideSelectedEdgesTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network-view");
		registerService(bc,hideSelectedEdgesTaskFactory,NetworkViewTaskFactory.class, hideSelectedEdgesTaskFactoryProps);
		registerService(bc,hideSelectedEdgesTaskFactory,HideSelectedEdgesTaskFactory.class, hideSelectedEdgesTaskFactoryProps);

		Properties unHideAllTaskFactoryProps = new Properties();
		unHideAllTaskFactoryProps.setProperty(ENABLE_FOR,"networkAndView");
		unHideAllTaskFactoryProps.setProperty(PREFERRED_MENU,"Select");
		unHideAllTaskFactoryProps.setProperty(MENU_GRAVITY,"3.0");
		unHideAllTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"9.6");
		unHideAllTaskFactoryProps.setProperty(TITLE,"Show all nodes and edges");
		unHideAllTaskFactoryProps.setProperty(COMMAND,"show-all-nodes-and-edges");
		unHideAllTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network-view");
		unHideAllTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/unhide_all.png").toString());
		unHideAllTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		unHideAllTaskFactoryProps.setProperty(TOOLTIP,"Show All Nodes and Edges");
		registerService(bc,unHideAllTaskFactory,NetworkViewTaskFactory.class, unHideAllTaskFactoryProps);
		registerService(bc,unHideAllTaskFactory,UnHideAllTaskFactory.class, unHideAllTaskFactoryProps);

		Properties unHideAllNodesTaskFactoryProps = new Properties();
		unHideAllNodesTaskFactoryProps.setProperty(ENABLE_FOR,"networkAndView");
		unHideAllNodesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Nodes");
		unHideAllNodesTaskFactoryProps.setProperty(MENU_GRAVITY,"3.0");
		unHideAllNodesTaskFactoryProps.setProperty(TITLE,"Show all nodes");
		unHideAllNodesTaskFactoryProps.setProperty(COMMAND,"show-all-nodes");
		unHideAllNodesTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network-view");
		registerService(bc,unHideAllNodesTaskFactory,NetworkViewTaskFactory.class, unHideAllNodesTaskFactoryProps);
		registerService(bc,unHideAllNodesTaskFactory,UnHideAllNodesTaskFactory.class, unHideAllNodesTaskFactoryProps);

		Properties unHideAllEdgesTaskFactoryProps = new Properties();
		unHideAllEdgesTaskFactoryProps.setProperty(ENABLE_FOR,"networkAndView");
		unHideAllEdgesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Edges");
		unHideAllEdgesTaskFactoryProps.setProperty(MENU_GRAVITY,"3.0");
		unHideAllEdgesTaskFactoryProps.setProperty(TITLE,"Show all edges");
		unHideAllEdgesTaskFactoryProps.setProperty(COMMAND,"show-all-edges");
		unHideAllEdgesTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network-view");
		registerService(bc,unHideAllEdgesTaskFactory,NetworkViewTaskFactory.class, unHideAllEdgesTaskFactoryProps);
		registerService(bc,unHideAllEdgesTaskFactory,UnHideAllEdgesTaskFactory.class, unHideAllEdgesTaskFactoryProps);


		Properties newEmptyNetworkTaskFactoryProps = new Properties();
		newEmptyNetworkTaskFactoryProps.setProperty(PREFERRED_MENU,"File.New.Network");
		newEmptyNetworkTaskFactoryProps.setProperty(MENU_GRAVITY,"4.0");
		newEmptyNetworkTaskFactoryProps.setProperty(TITLE,"Empty Network");
		newEmptyNetworkTaskFactoryProps.setProperty(COMMAND,"new-empty-network");
		newEmptyNetworkTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,newEmptyNetworkTaskFactory,TaskFactory.class, newEmptyNetworkTaskFactoryProps);
		registerService(bc,newEmptyNetworkTaskFactory,NewEmptyNetworkViewFactory.class, newEmptyNetworkTaskFactoryProps);

		Properties newNetworkSelectedNodesEdgesTaskFactoryProps = new Properties();
		newNetworkSelectedNodesEdgesTaskFactoryProps.setProperty(ENABLE_FOR,"selectedNodesOrEdges");
		newNetworkSelectedNodesEdgesTaskFactoryProps.setProperty(PREFERRED_MENU,"File.New.Network");
		newNetworkSelectedNodesEdgesTaskFactoryProps.setProperty(MENU_GRAVITY,"2.0");
		newNetworkSelectedNodesEdgesTaskFactoryProps.setProperty(ACCELERATOR,"cmd shift n");
		newNetworkSelectedNodesEdgesTaskFactoryProps.setProperty(TITLE,"From selected nodes, selected edges");
		newNetworkSelectedNodesEdgesTaskFactoryProps.setProperty(COMMAND,"new-network-from-selected-nodes-and-selected-edges");
		newNetworkSelectedNodesEdgesTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,newNetworkSelectedNodesEdgesTaskFactory,NetworkTaskFactory.class, newNetworkSelectedNodesEdgesTaskFactoryProps);
		registerService(bc,newNetworkSelectedNodesEdgesTaskFactory,NewNetworkSelectedNodesAndEdgesTaskFatory.class, newNetworkSelectedNodesEdgesTaskFactoryProps);

		Properties newNetworkSelectedNodesOnlyTaskFactoryProps = new Properties();
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty(PREFERRED_MENU,"File.New.Network");
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/new_fromselected.png").toString());
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty(ACCELERATOR,"cmd n");
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty(ENABLE_FOR,"selectedNodesOrEdges");
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty(TITLE,"From selected nodes, all edges");
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"9.1");
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty(MENU_GRAVITY,"1.0");
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty(TOOLTIP,"New Network From Selection");
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty(COMMAND,"new-network-selected-nodes-and-all-edges");
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,newNetworkSelectedNodesOnlyTaskFactory,NetworkTaskFactory.class, newNetworkSelectedNodesOnlyTaskFactoryProps);
		registerService(bc,newNetworkSelectedNodesOnlyTaskFactory,NewNetworkSelectedNodesOnlyTaskFactory.class, newNetworkSelectedNodesOnlyTaskFactoryProps);

		Properties cloneNetworkTaskFactoryProps = new Properties();
		cloneNetworkTaskFactoryProps.setProperty(ENABLE_FOR,"network");
		cloneNetworkTaskFactoryProps.setProperty(PREFERRED_MENU,"File.New.Network");
		cloneNetworkTaskFactoryProps.setProperty(MENU_GRAVITY,"3.0");
		cloneNetworkTaskFactoryProps.setProperty(TITLE,"Clone Current Network");
		cloneNetworkTaskFactoryProps.setProperty(COMMAND,"clone-current-network");
		cloneNetworkTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,cloneNetworkTaskFactory,NetworkTaskFactory.class, cloneNetworkTaskFactoryProps);
		registerService(bc,cloneNetworkTaskFactory,CloneNetworkTaskFactory.class, cloneNetworkTaskFactoryProps);

		Properties destroyNetworkTaskFactoryProps = new Properties();
		destroyNetworkTaskFactoryProps.setProperty(PREFERRED_MENU,"Edit");
		destroyNetworkTaskFactoryProps.setProperty(ACCELERATOR,"cmd shift w");
		destroyNetworkTaskFactoryProps.setProperty(ENABLE_FOR,"network");
		destroyNetworkTaskFactoryProps.setProperty(TITLE,"Destroy Network");
		destroyNetworkTaskFactoryProps.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU,"true");
		destroyNetworkTaskFactoryProps.setProperty(MENU_GRAVITY,"3.2");
		destroyNetworkTaskFactoryProps.setProperty(COMMAND,"destroy-network");
		destroyNetworkTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,destroyNetworkTaskFactory,NetworkCollectionTaskFactory.class, destroyNetworkTaskFactoryProps);
		registerService(bc,destroyNetworkTaskFactory,DestroyNetworkTaskFactory.class, destroyNetworkTaskFactoryProps);


		Properties destroyNetworkViewTaskFactoryProps = new Properties();
		destroyNetworkViewTaskFactoryProps.setProperty(PREFERRED_MENU,"Edit");
		destroyNetworkViewTaskFactoryProps.setProperty(ACCELERATOR,"cmd w");
		destroyNetworkViewTaskFactoryProps.setProperty(ENABLE_FOR,"networkAndView");
		destroyNetworkViewTaskFactoryProps.setProperty(TITLE,"Destroy View");
		destroyNetworkViewTaskFactoryProps.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU,"true");
		destroyNetworkViewTaskFactoryProps.setProperty(MENU_GRAVITY,"3.1");
		destroyNetworkViewTaskFactoryProps.setProperty(COMMAND,"destroy-view");
		destroyNetworkViewTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network-view");
		registerService(bc,destroyNetworkViewTaskFactory,NetworkViewCollectionTaskFactory.class, destroyNetworkViewTaskFactoryProps);
		registerService(bc,destroyNetworkViewTaskFactory,DestroyNetworkViewTaskFactory.class, destroyNetworkViewTaskFactoryProps);

		Properties zoomInTaskFactoryProps = new Properties();
		zoomInTaskFactoryProps.setProperty(ACCELERATOR,"cmd equals");
		zoomInTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/stock_zoom-in.png").toString());
		zoomInTaskFactoryProps.setProperty(ENABLE_FOR,"networkAndView");
		zoomInTaskFactoryProps.setProperty(TITLE,"Zoom In");
		zoomInTaskFactoryProps.setProperty(TOOLTIP,"Zoom In");
		zoomInTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"5.1");
		zoomInTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		zoomInTaskFactoryProps.setProperty(COMMAND,"zoom-in");
		zoomInTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network-view");
		registerService(bc,zoomInTaskFactory,NetworkViewTaskFactory.class, zoomInTaskFactoryProps);

		Properties zoomOutTaskFactoryProps = new Properties();
		zoomOutTaskFactoryProps.setProperty(ACCELERATOR,"cmd minus");
		zoomOutTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/stock_zoom-out.png").toString());
		zoomOutTaskFactoryProps.setProperty(ENABLE_FOR,"networkAndView");
		zoomOutTaskFactoryProps.setProperty(TITLE,"Zoom Out");
		zoomOutTaskFactoryProps.setProperty(TOOLTIP,"Zoom Out");
		zoomOutTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"5.2");
		zoomOutTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		zoomOutTaskFactoryProps.setProperty(COMMAND,"zoom-out");
		zoomOutTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network-view");
		registerService(bc,zoomOutTaskFactory,NetworkViewTaskFactory.class, zoomOutTaskFactoryProps);

		Properties fitSelectedTaskFactoryProps = new Properties();
		fitSelectedTaskFactoryProps.setProperty(ACCELERATOR,"cmd shift f");
		fitSelectedTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/stock_zoom-object.png").toString());
		fitSelectedTaskFactoryProps.setProperty(ENABLE_FOR,"networkAndView");
		fitSelectedTaskFactoryProps.setProperty(TITLE,"Fit Selected");
		fitSelectedTaskFactoryProps.setProperty(TOOLTIP,"Zoom selected region");
		fitSelectedTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"5.4");
		fitSelectedTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		fitSelectedTaskFactoryProps.setProperty(COMMAND,"fit-selected");
		fitSelectedTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network-view");
		registerService(bc,fitSelectedTaskFactory,NetworkViewTaskFactory.class, fitSelectedTaskFactoryProps);

		Properties fitContentTaskFactoryProps = new Properties();
		fitContentTaskFactoryProps.setProperty(ACCELERATOR,"cmd f");
		fitContentTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/stock_zoom-1.png").toString());
		fitContentTaskFactoryProps.setProperty(ENABLE_FOR,"networkAndView");
		fitContentTaskFactoryProps.setProperty(TITLE,"Fit Content");
		fitContentTaskFactoryProps.setProperty(TOOLTIP,"Zoom out to display all of current Network");
		fitContentTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"5.3");
		fitContentTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		fitContentTaskFactoryProps.setProperty(COMMAND,"fit-content");
		fitContentTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network-view");
		registerService(bc,fitContentTaskFactory,NetworkViewTaskFactory.class, fitContentTaskFactoryProps);

		Properties editNetworkTitleTaskFactoryProps = new Properties();
		editNetworkTitleTaskFactoryProps.setProperty(ENABLE_FOR,"network");
		editNetworkTitleTaskFactoryProps.setProperty(PREFERRED_MENU,"Edit");
		editNetworkTitleTaskFactoryProps.setProperty(MENU_GRAVITY,"5.5");
		editNetworkTitleTaskFactoryProps.setProperty(TITLE,"Rename Network...");
		editNetworkTitleTaskFactoryProps.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU,"true");
		editNetworkTitleTaskFactoryProps.setProperty(COMMAND,"rename");
		editNetworkTitleTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,editNetworkTitleTaskFactory,NetworkTaskFactory.class, editNetworkTitleTaskFactoryProps);
		registerService(bc,editNetworkTitleTaskFactory,EditNetworkTitleTaskFactory.class, editNetworkTitleTaskFactoryProps);


		Properties createNetworkViewTaskFactoryProps = new Properties();
		createNetworkViewTaskFactoryProps.setProperty(ID,"createNetworkViewTaskFactory");
		createNetworkViewTaskFactoryProps.setProperty(ENABLE_FOR,"networkWithoutView");
		createNetworkViewTaskFactoryProps.setProperty(PREFERRED_MENU,"Edit");
		createNetworkViewTaskFactoryProps.setProperty(MENU_GRAVITY,"3.0");
		createNetworkViewTaskFactoryProps.setProperty(TITLE,"Create View");
		createNetworkViewTaskFactoryProps.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU,"true");
		createNetworkViewTaskFactoryProps.setProperty(COMMAND,"create-view");
		createNetworkViewTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,createNetworkViewTaskFactory,NetworkCollectionTaskFactory.class, createNetworkViewTaskFactoryProps);
		registerService(bc,createNetworkViewTaskFactory,CreateNetworkViewTaskFactory.class, createNetworkViewTaskFactoryProps);

		Properties exportNetworkImageTaskFactoryProps = new Properties();
		exportNetworkImageTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Export.Network View");
		exportNetworkImageTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/img_file_export.png").toString());
		exportNetworkImageTaskFactoryProps.setProperty(ENABLE_FOR,"networkAndView");
		exportNetworkImageTaskFactoryProps.setProperty(TITLE,"Graphics...");
		exportNetworkImageTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"3.7");
		exportNetworkImageTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		exportNetworkImageTaskFactoryProps.setProperty(TOOLTIP,"Export Network Image to File");
		exportNetworkImageTaskFactoryProps.setProperty(COMMAND,"export-network-image");
		exportNetworkImageTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network-view");
		registerService(bc,exportNetworkImageTaskFactory,NetworkViewTaskFactory.class, exportNetworkImageTaskFactoryProps);
		registerService(bc,exportNetworkImageTaskFactory,ExportNetworkImageTaskFactory.class, exportNetworkImageTaskFactoryProps);
		

		Properties exportNetworkViewTaskFactoryProps = new Properties();
		exportNetworkViewTaskFactoryProps.setProperty(ENABLE_FOR,"networkAndView");
		exportNetworkViewTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Export.Network View");
		exportNetworkViewTaskFactoryProps.setProperty(MENU_GRAVITY,"5.1");
		exportNetworkViewTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"3.5");
		exportNetworkViewTaskFactoryProps.setProperty(TITLE,"File...");
		exportNetworkViewTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/net_file_export.png").toString());
		exportNetworkViewTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		exportNetworkViewTaskFactoryProps.setProperty(TOOLTIP,"Export Network to File");
		exportNetworkViewTaskFactoryProps.setProperty(COMMAND,"export-network");
		exportNetworkViewTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,exportNetworkViewTaskFactory,NetworkViewTaskFactory.class, exportNetworkViewTaskFactoryProps);
		registerService(bc,exportNetworkViewTaskFactory,ExportNetworkViewTaskFactory.class, exportNetworkViewTaskFactoryProps);

		Properties exportCurrentTableTaskFactoryProps = new Properties();
		exportCurrentTableTaskFactoryProps.setProperty(ENABLE_FOR,"table");
		exportCurrentTableTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Export.Table");
		exportCurrentTableTaskFactoryProps.setProperty(MENU_GRAVITY,"1.2");
		exportCurrentTableTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"3.6");
		exportCurrentTableTaskFactoryProps.setProperty(TITLE,"File...");
		exportCurrentTableTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/table_file_export.png").toString());
		exportCurrentTableTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		exportCurrentTableTaskFactoryProps.setProperty(TOOLTIP,"Export Table to File");
		exportCurrentTableTaskFactoryProps.setProperty(COMMAND,"export-table");
		exportCurrentTableTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"table");
		registerService(bc,exportCurrentTableTaskFactory, TaskFactory.class, exportCurrentTableTaskFactoryProps);
		registerService(bc,exportCurrentTableTaskFactory,ExportSelectedTableTaskFactory.class, exportCurrentTableTaskFactoryProps);

		Properties exportVizmapTaskFactoryProps = new Properties();
		exportVizmapTaskFactoryProps.setProperty(ENABLE_FOR,"vizmap");
		exportVizmapTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Export.Vizmap");
		exportVizmapTaskFactoryProps.setProperty(MENU_GRAVITY,"1.1");
		exportVizmapTaskFactoryProps.setProperty(TITLE,"File...");
		exportVizmapTaskFactoryProps.setProperty(COMMAND,"export-vizmap");
		exportVizmapTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"vizmap");
		registerService(bc,exportVizmapTaskFactory,TaskFactory.class, exportVizmapTaskFactoryProps);
		registerService(bc,exportVizmapTaskFactory,ExportVizmapTaskFactory.class, exportVizmapTaskFactoryProps);


		Properties newSessionTaskFactoryProps = new Properties();
		newSessionTaskFactoryProps.setProperty(PREFERRED_MENU,"File.New");
		newSessionTaskFactoryProps.setProperty(MENU_GRAVITY,"1.1");
		newSessionTaskFactoryProps.setProperty(TITLE,"Session");
		newSessionTaskFactoryProps.setProperty(COMMAND,"new-session");
		newSessionTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"cytoscape");
		registerService(bc,newSessionTaskFactory,TaskFactory.class, newSessionTaskFactoryProps);
		registerService(bc,newSessionTaskFactory,NewSessionTaskFactory.class, newSessionTaskFactoryProps);

		Properties openSessionTaskFactoryProps = new Properties();
		openSessionTaskFactoryProps.setProperty(ID,"openSessionTaskFactory");
		openSessionTaskFactoryProps.setProperty(PREFERRED_MENU,"File");
		openSessionTaskFactoryProps.setProperty(ACCELERATOR,"cmd o");
		openSessionTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/open_session.png").toString());
		openSessionTaskFactoryProps.setProperty(TITLE,"Open");
		openSessionTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"1.0");
		openSessionTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		openSessionTaskFactoryProps.setProperty(MENU_GRAVITY,"1.0");
		openSessionTaskFactoryProps.setProperty(TOOLTIP,"Open Session");
		openSessionTaskFactoryProps.setProperty(COMMAND,"open-session");
		openSessionTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"cytoscape");
		registerService(bc,openSessionTaskFactory,TaskFactory.class, openSessionTaskFactoryProps);
		registerService(bc,openSessionTaskFactory,OpenSessionTaskFactory.class, openSessionTaskFactoryProps);

		Properties saveSessionTaskFactoryProps = new Properties();
		saveSessionTaskFactoryProps.setProperty(PREFERRED_MENU,"File");
		saveSessionTaskFactoryProps.setProperty(ACCELERATOR,"cmd s");
		saveSessionTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/stock_save.png").toString());
		saveSessionTaskFactoryProps.setProperty(TITLE,"Save");
		saveSessionTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"1.1");
		saveSessionTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		saveSessionTaskFactoryProps.setProperty(MENU_GRAVITY,"3.0");
		saveSessionTaskFactoryProps.setProperty(TOOLTIP,"Save Session");
		saveSessionTaskFactoryProps.setProperty(COMMAND,"save-session");
		saveSessionTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"cytoscape");
		registerService(bc,saveSessionTaskFactory,TaskFactory.class, saveSessionTaskFactoryProps);

		Properties saveSessionAsTaskFactoryProps = new Properties();
		saveSessionAsTaskFactoryProps.setProperty(PREFERRED_MENU,"File");
		saveSessionAsTaskFactoryProps.setProperty(ACCELERATOR,"cmd shift s");
		saveSessionAsTaskFactoryProps.setProperty(MENU_GRAVITY,"3.1");
		saveSessionAsTaskFactoryProps.setProperty(TITLE,"Save As");
		saveSessionAsTaskFactoryProps.setProperty(COMMAND,"save-session-as");
		saveSessionAsTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"cytoscape");
		registerService(bc,saveSessionAsTaskFactory,TaskFactory.class, saveSessionAsTaskFactoryProps);
		registerService(bc,saveSessionAsTaskFactory,SaveSessionAsTaskFactory.class, saveSessionAsTaskFactoryProps);


		Properties applyPreferredLayoutTaskFactoryProps = new Properties();
		applyPreferredLayoutTaskFactoryProps.setProperty(PREFERRED_MENU,"Layout");
		applyPreferredLayoutTaskFactoryProps.setProperty(ACCELERATOR,"fn5");
		applyPreferredLayoutTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/apply_layout.png").toString());
		applyPreferredLayoutTaskFactoryProps.setProperty(ENABLE_FOR,"networkAndView");
		applyPreferredLayoutTaskFactoryProps.setProperty(TITLE,"Apply Preferred Layout");
		applyPreferredLayoutTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"7.0");
		applyPreferredLayoutTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		applyPreferredLayoutTaskFactoryProps.setProperty(MENU_GRAVITY,"5.0");
		applyPreferredLayoutTaskFactoryProps.setProperty(TOOLTIP,"Apply Preferred Layout");
		applyPreferredLayoutTaskFactoryProps.setProperty(COMMAND,"apply-preferred-layout");
		applyPreferredLayoutTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network-view");
		registerService(bc,applyPreferredLayoutTaskFactory,NetworkViewCollectionTaskFactory.class, applyPreferredLayoutTaskFactoryProps);
		registerService(bc,applyPreferredLayoutTaskFactory,ApplyPreferredLayoutTaskFactory.class, applyPreferredLayoutTaskFactoryProps);

		Properties deleteColumnTaskFactoryProps = new Properties();
		deleteColumnTaskFactoryProps.setProperty(TITLE,"Delete column");
		deleteColumnTaskFactoryProps.setProperty(COMMAND,"delete-column");
		deleteColumnTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"table");
		registerService(bc,deleteColumnTaskFactory,TableColumnTaskFactory.class, deleteColumnTaskFactoryProps);
		registerService(bc,deleteColumnTaskFactory,DeleteColumnTaskFactory.class, deleteColumnTaskFactoryProps);

		Properties renameColumnTaskFactoryProps = new Properties();
		renameColumnTaskFactoryProps.setProperty(TITLE,"Rename column");
		renameColumnTaskFactoryProps.setProperty(COMMAND,"rename-column");
		renameColumnTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"table");
		registerService(bc,renameColumnTaskFactory,TableColumnTaskFactory.class, renameColumnTaskFactoryProps);
		registerService(bc,renameColumnTaskFactory,RenameColumnTaskFactory.class, renameColumnTaskFactoryProps);

		Properties copyValueToEntireColumnTaskFactoryProps = new Properties();
		copyValueToEntireColumnTaskFactoryProps.setProperty(TITLE,"Copy to entire column");
		registerService(bc,copyValueToEntireColumnTaskFactory,TableCellTaskFactory.class, copyValueToEntireColumnTaskFactoryProps);
		registerService(bc,deleteTableTaskFactory,TableTaskFactory.class, new Properties());
		registerService(bc,deleteTableTaskFactory,DeleteTableTaskFactory.class, new Properties());

		Properties connectSelectedNodesTaskFactoryProps = new Properties();
		connectSelectedNodesTaskFactoryProps.setProperty(PREFERRED_MENU,"Edit");
		connectSelectedNodesTaskFactoryProps.setProperty(ENABLE_FOR,"network");
		connectSelectedNodesTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"2.5");
		connectSelectedNodesTaskFactoryProps.setProperty(TITLE,"Connect Selected Nodes");
		connectSelectedNodesTaskFactoryProps.setProperty(COMMAND,"connect-selected-nodes");
		connectSelectedNodesTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,connectSelectedNodesTaskFactory,NetworkTaskFactory.class, connectSelectedNodesTaskFactoryProps);
		registerService(bc,connectSelectedNodesTaskFactory,ConnectSelectedNodesTaskFactory.class, connectSelectedNodesTaskFactoryProps);

		GroupNodesTaskFactoryImpl groupNodesTaskFactory = 
			new GroupNodesTaskFactoryImpl(cyGroupManager, cyGroupFactory);
		Properties groupNodesTaskFactoryProps = new Properties();
		groupNodesTaskFactoryProps.setProperty(PREFERRED_MENU,"Groups");
		groupNodesTaskFactoryProps.setProperty(TITLE,"Group Selected Nodes");
		groupNodesTaskFactoryProps.setProperty(TOOLTIP,"Group Selected Nodes Together");
		groupNodesTaskFactoryProps.setProperty(IN_TOOL_BAR,"false");
		groupNodesTaskFactoryProps.setProperty(IN_MENU_BAR,"false");
		groupNodesTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		groupNodesTaskFactoryProps.setProperty(COMMAND, "group-selected-nodes");
		groupNodesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network-view");
		registerService(bc,groupNodesTaskFactory,NetworkViewTaskFactory.class, groupNodesTaskFactoryProps);
		registerService(bc,groupNodesTaskFactory,GroupNodesTaskFactory.class, groupNodesTaskFactoryProps);

		UnGroupNodesTaskFactoryImpl unGroupTaskFactory = 
			new UnGroupNodesTaskFactoryImpl(cyGroupManager);
		Properties unGroupNodesTaskFactoryProps = new Properties();
		unGroupNodesTaskFactoryProps.setProperty(PREFERRED_MENU,"Groups");
		unGroupNodesTaskFactoryProps.setProperty(TITLE,"Ungroup Selected Nodes");
		unGroupNodesTaskFactoryProps.setProperty(TOOLTIP,"Ungroup Selected Nodes");
		unGroupNodesTaskFactoryProps.setProperty(IN_TOOL_BAR,"false");
		unGroupNodesTaskFactoryProps.setProperty(IN_MENU_BAR,"false");
		unGroupNodesTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		unGroupNodesTaskFactoryProps.setProperty(COMMAND, "ungroup-selected-nodes");
		groupNodesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network-view");
		registerService(bc,unGroupTaskFactory,NetworkViewTaskFactory.class, unGroupNodesTaskFactoryProps);
		registerService(bc,unGroupTaskFactory,UnGroupTaskFactory.class, unGroupNodesTaskFactoryProps);

		GroupNodeContextTaskFactoryImpl collapseGroupTaskFactory = 
			new GroupNodeContextTaskFactoryImpl(cyGroupManager, true);
		Properties collapseGroupTaskFactoryProps = new Properties();
		collapseGroupTaskFactoryProps.setProperty(PREFERRED_MENU,"Groups");
		collapseGroupTaskFactoryProps.setProperty(TITLE,"Collapse Group");
		collapseGroupTaskFactoryProps.setProperty(TOOLTIP,"Collapse Grouped Nodes");
		collapseGroupTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		collapseGroupTaskFactoryProps.setProperty(COMMAND, "collapse-grouped-nodes");
		collapseGroupTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network-view"); // TODO right namespace?
		registerService(bc,collapseGroupTaskFactory,NodeViewTaskFactory.class, collapseGroupTaskFactoryProps);
		registerService(bc,collapseGroupTaskFactory,CollapseGroupTaskFactory.class, collapseGroupTaskFactoryProps);

		GroupNodeContextTaskFactoryImpl expandGroupTaskFactory = 
			new GroupNodeContextTaskFactoryImpl(cyGroupManager, false);
		Properties expandGroupTaskFactoryProps = new Properties();
		expandGroupTaskFactoryProps.setProperty(PREFERRED_MENU,"Groups");
		expandGroupTaskFactoryProps.setProperty(TITLE,"Expand Group");
		expandGroupTaskFactoryProps.setProperty(TOOLTIP,"Expand Group");
		expandGroupTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		expandGroupTaskFactoryProps.setProperty(COMMAND, "expand-group");
		expandGroupTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network-view"); // TODO right namespace
		registerService(bc,expandGroupTaskFactory,NodeViewTaskFactory.class, expandGroupTaskFactoryProps);
		registerService(bc,expandGroupTaskFactory,ExpandGroupTaskFactory.class, expandGroupTaskFactoryProps);

		UnGroupNodesTaskFactoryImpl unGroupNodesTaskFactory = 
			new UnGroupNodesTaskFactoryImpl(cyGroupManager);
		Properties unGroupTaskFactoryProps = new Properties();
		unGroupTaskFactoryProps.setProperty(PREFERRED_MENU,"Groups");
		unGroupTaskFactoryProps.setProperty(TITLE,"Ungroup Nodes");
		unGroupTaskFactoryProps.setProperty(TOOLTIP,"Ungroup Nodes");
		unGroupTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		unGroupTaskFactoryProps.setProperty(COMMAND, "ungroup");
		unGroupTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network-view"); // TODO right namespace
		registerService(bc,unGroupNodesTaskFactory,NodeViewTaskFactory.class, unGroupTaskFactoryProps);
		registerService(bc,unGroupNodesTaskFactory,UnGroupNodesTaskFactory.class, unGroupTaskFactoryProps);
		
		MapTableToNetworkTablesTaskFactoryImpl mapNetworkToTables = new MapTableToNetworkTablesTaskFactoryImpl(cyNetworkManagerServiceRef, tunableSetterServiceRef);
		Properties mapNetworkToTablesProps = new Properties();
		registerService(bc, mapNetworkToTables, MapTableToNetworkTablesTaskFactory.class, mapNetworkToTablesProps);
		
		
		ExportTableTaskFactoryImpl exportTableTaskFactory = new ExportTableTaskFactoryImpl(cyTableWriterManagerRef,tunableSetterServiceRef);
		Properties exportTableTaskFactoryProps = new Properties();
		registerService(bc,exportTableTaskFactory,ExportTableTaskFactory.class,exportTableTaskFactoryProps);
		
		UpdateAddedNetworkAttributes updateAddedNetworkAttributes = new UpdateAddedNetworkAttributes(mapGlobal, synchronousTaskManagerServiceRef);
		registerService(bc, updateAddedNetworkAttributes, NetworkAddedListener.class, new Properties());

	}
}
