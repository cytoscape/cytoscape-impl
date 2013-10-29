package org.cytoscape.task.internal;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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

import static org.cytoscape.work.ServiceProperties.ACCELERATOR;
import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.ID;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_AFTER;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.IN_NETWORK_PANEL_CONTEXT_MENU;
import static org.cytoscape.work.ServiceProperties.IN_CONTEXT_MENU;
import static org.cytoscape.work.ServiceProperties.IN_TOOL_BAR;
import static org.cytoscape.work.ServiceProperties.LARGE_ICON_URL;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.NETWORK_GROUP_MENU;
import static org.cytoscape.work.ServiceProperties.NETWORK_SELECT_MENU;
import static org.cytoscape.work.ServiceProperties.NODE_ADD_MENU;
import static org.cytoscape.work.ServiceProperties.NODE_EDIT_MENU;
import static org.cytoscape.work.ServiceProperties.NODE_GROUP_MENU;
import static org.cytoscape.work.ServiceProperties.NODE_SELECT_MENU;
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
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableFactory;
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
import org.cytoscape.task.create.CloneNetworkTaskFactory;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.task.create.NewEmptyNetworkViewFactory;
import org.cytoscape.task.internal.creation.NewNetworkCommandTaskFactory;
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
import org.cytoscape.task.edit.ImportDataTableTaskFactory;
import org.cytoscape.task.edit.MergeDataTableTaskFactory;
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
import org.cytoscape.task.internal.group.AddToGroupTaskFactory;
import org.cytoscape.task.internal.group.ListGroupsTaskFactory;
import org.cytoscape.task.internal.group.RemoveFromGroupTaskFactory;
import org.cytoscape.task.internal.group.RenameGroupTaskFactory;
import org.cytoscape.task.internal.group.GroupNodeContextTaskFactoryImpl;
import org.cytoscape.task.internal.group.GroupNodesTaskFactoryImpl;
import org.cytoscape.task.internal.group.UnGroupNodesTaskFactoryImpl;
import org.cytoscape.task.internal.hide.HideSelectedEdgesTaskFactoryImpl;
import org.cytoscape.task.internal.hide.HideSelectedNodesTaskFactoryImpl;
import org.cytoscape.task.internal.hide.HideSelectedTaskFactoryImpl;
import org.cytoscape.task.internal.hide.HideTaskFactory;
import org.cytoscape.task.internal.hide.UnHideAllEdgesTaskFactoryImpl;
import org.cytoscape.task.internal.hide.UnHideAllNodesTaskFactoryImpl;
import org.cytoscape.task.internal.hide.UnHideAllTaskFactoryImpl;
import org.cytoscape.task.internal.hide.UnHideTaskFactory;
import org.cytoscape.task.internal.layout.ApplyPreferredLayoutTaskFactoryImpl;
import org.cytoscape.task.internal.layout.GetPreferredLayoutTaskFactory;
import org.cytoscape.task.internal.layout.SetPreferredLayoutTaskFactory;
import org.cytoscape.task.internal.loaddatatable.LoadAttributesFileTaskFactoryImpl;
import org.cytoscape.task.internal.loaddatatable.LoadAttributesURLTaskFactoryImpl;
import org.cytoscape.task.internal.loaddatatable.ImportAttributesFileTaskFactoryImpl;
import org.cytoscape.task.internal.loaddatatable.ImportAttributesURLTaskFactoryImpl;
import org.cytoscape.task.internal.loadnetwork.LoadNetworkFileTaskFactoryImpl;
import org.cytoscape.task.internal.loadnetwork.LoadNetworkURLTaskFactoryImpl;
import org.cytoscape.task.internal.loadvizmap.LoadVizmapFileTaskFactoryImpl;
import org.cytoscape.task.internal.networkobjects.AddTaskFactory;
import org.cytoscape.task.internal.networkobjects.AddEdgeTaskFactory;
import org.cytoscape.task.internal.networkobjects.AddNodeTaskFactory;
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
import org.cytoscape.task.internal.table.DeleteColumnTaskFactoryImpl;
import org.cytoscape.task.internal.table.DeleteColumnCommandTaskFactory;
import org.cytoscape.task.internal.table.DeleteRowTaskFactory;
import org.cytoscape.task.internal.table.DeleteTableTaskFactoryImpl;
import org.cytoscape.task.internal.table.DestroyTableTaskFactory;
import org.cytoscape.task.internal.table.GetColumnTaskFactory;
import org.cytoscape.task.internal.table.GetNetworkAttributeTaskFactory;
import org.cytoscape.task.internal.table.GetRowTaskFactory;
import org.cytoscape.task.internal.table.GetValueTaskFactory;
import org.cytoscape.task.internal.table.ListNetworkAttributesTaskFactory;
import org.cytoscape.task.internal.table.ListColumnsTaskFactory;
import org.cytoscape.task.internal.table.ListRowsTaskFactory;
import org.cytoscape.task.internal.table.ListTablesTaskFactory;
import org.cytoscape.task.internal.table.MapGlobalToLocalTableTaskFactoryImpl;
import org.cytoscape.task.internal.table.MapTableToNetworkTablesTaskFactoryImpl;
import org.cytoscape.task.internal.table.RenameColumnTaskFactoryImpl;
import org.cytoscape.task.internal.table.ImportDataTableTaskFactoryImpl;
import org.cytoscape.task.internal.table.MergeDataTableTaskFactoryImpl;
import org.cytoscape.task.internal.table.SetNetworkAttributeTaskFactory;
import org.cytoscape.task.internal.table.SetTableTitleTaskFactory;
import org.cytoscape.task.internal.table.SetValuesTaskFactory;
import org.cytoscape.task.internal.title.EditNetworkTitleTaskFactoryImpl;
import org.cytoscape.task.internal.view.GetCurrentNetworkViewTaskFactory;
import org.cytoscape.task.internal.view.ListNetworkViewsTaskFactory;
import org.cytoscape.task.internal.view.SetCurrentNetworkViewTaskFactory;
import org.cytoscape.task.internal.view.UpdateNetworkViewTaskFactory;
import org.cytoscape.task.internal.vizmap.ApplyVisualStyleTaskFactoryimpl;
import org.cytoscape.task.internal.vizmap.ClearEdgeBendTaskFactory;
import org.cytoscape.task.internal.zoom.FitContentTaskFactory;
import org.cytoscape.task.internal.zoom.FitSelectedTaskFactory;
import org.cytoscape.task.internal.zoom.ZoomInTaskFactory;
import org.cytoscape.task.internal.zoom.ZoomOutTaskFactory;
import org.cytoscape.task.read.LoadNetworkFileTaskFactory;
import org.cytoscape.task.read.LoadNetworkURLTaskFactory;
import org.cytoscape.task.read.LoadTableFileTaskFactory;
import org.cytoscape.task.read.ImportTableURLTaskFactory;
import org.cytoscape.task.read.ImportTableFileTaskFactory;
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
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.SynchronousTaskManager;
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
		CyTableFactory cyTableFactoryServiceRef = getService(bc,CyTableFactory.class);
		CyTableManager cyTableManagerServiceRef = getService(bc,CyTableManager.class);
		CyLayoutAlgorithmManager cyLayoutsServiceRef = getService(bc,CyLayoutAlgorithmManager.class);
		CyTableWriterManager cyTableWriterManagerRef = getService(bc,CyTableWriterManager.class);
		SynchronousTaskManager<?> synchronousTaskManagerServiceRef = getService(bc,SynchronousTaskManager.class);
		TunableSetter tunableSetterServiceRef = getService(bc,TunableSetter.class);
		CyRootNetworkManager rootNetworkManagerServiceRef = getService(bc, CyRootNetworkManager.class);
		CyNetworkTableManager cyNetworkTableManagerServiceRef = getService(bc, CyNetworkTableManager.class);
		RenderingEngineManager renderingEngineManagerServiceRef = getService(bc, RenderingEngineManager.class);
		CyNetworkViewFactory nullNetworkViewFactory = getService(bc, CyNetworkViewFactory.class, "(id=NullCyNetworkViewFactory)");
		
		CyGroupManager cyGroupManager = getService(bc, CyGroupManager.class);
		CyGroupFactory cyGroupFactory = getService(bc, CyGroupFactory.class);
		
		LoadVizmapFileTaskFactoryImpl loadVizmapFileTaskFactory = new LoadVizmapFileTaskFactoryImpl(vizmapReaderManagerServiceRef,visualMappingManagerServiceRef,synchronousTaskManagerServiceRef, tunableSetterServiceRef);

		LoadNetworkFileTaskFactoryImpl loadNetworkFileTaskFactory = new LoadNetworkFileTaskFactoryImpl(cyNetworkReaderManagerServiceRef,cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef,cyPropertyServiceRef,cyNetworkNamingServiceRef, tunableSetterServiceRef, visualMappingManagerServiceRef, nullNetworkViewFactory);
		LoadNetworkURLTaskFactoryImpl loadNetworkURLTaskFactory = new LoadNetworkURLTaskFactoryImpl(cyNetworkReaderManagerServiceRef,cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef,cyPropertyServiceRef,cyNetworkNamingServiceRef,streamUtilRef, synchronousTaskManagerServiceRef, tunableSetterServiceRef, visualMappingManagerServiceRef, nullNetworkViewFactory);

		DeleteSelectedNodesAndEdgesTaskFactoryImpl deleteSelectedNodesAndEdgesTaskFactory = new DeleteSelectedNodesAndEdgesTaskFactoryImpl(cyApplicationManagerServiceRef, undoSupportServiceRef,cyNetworkViewManagerServiceRef,visualMappingManagerServiceRef,cyEventHelperRef);
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
		NewEmptyNetworkTaskFactoryImpl newEmptyNetworkTaskFactory = new NewEmptyNetworkTaskFactoryImpl(cyNetworkFactoryServiceRef,cyNetworkViewFactoryServiceRef,cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef,cyNetworkNamingServiceRef,synchronousTaskManagerServiceRef,visualMappingManagerServiceRef, cyRootNetworkFactoryServiceRef, cyApplicationManagerServiceRef);
		CloneNetworkTaskFactoryImpl cloneNetworkTaskFactory = new CloneNetworkTaskFactoryImpl(cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef,visualMappingManagerServiceRef,cyNetworkFactoryServiceRef,cyNetworkViewFactoryServiceRef,cyNetworkNamingServiceRef,cyApplicationManagerServiceRef,cyNetworkTableManagerServiceRef,rootNetworkManagerServiceRef,cyGroupManager,cyGroupFactory,renderingEngineManagerServiceRef, nullNetworkViewFactory );
		NewNetworkSelectedNodesEdgesTaskFactoryImpl newNetworkSelectedNodesEdgesTaskFactory = new NewNetworkSelectedNodesEdgesTaskFactoryImpl(undoSupportServiceRef,cyRootNetworkFactoryServiceRef,cyNetworkViewFactoryServiceRef,cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef,cyNetworkNamingServiceRef,visualMappingManagerServiceRef,cyApplicationManagerServiceRef,cyEventHelperRef,cyGroupManager,renderingEngineManagerServiceRef);
		NewNetworkCommandTaskFactory newNetworkCommandTaskFactory = new NewNetworkCommandTaskFactory(undoSupportServiceRef,cyRootNetworkFactoryServiceRef,cyNetworkViewFactoryServiceRef,cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef,cyNetworkNamingServiceRef,visualMappingManagerServiceRef,cyApplicationManagerServiceRef,cyEventHelperRef,cyGroupManager,renderingEngineManagerServiceRef);
		NewNetworkSelectedNodesOnlyTaskFactoryImpl newNetworkSelectedNodesOnlyTaskFactory = new NewNetworkSelectedNodesOnlyTaskFactoryImpl(undoSupportServiceRef,cyRootNetworkFactoryServiceRef,cyNetworkViewFactoryServiceRef,cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef,cyNetworkNamingServiceRef,visualMappingManagerServiceRef,cyApplicationManagerServiceRef,cyEventHelperRef,cyGroupManager,renderingEngineManagerServiceRef);
		DestroyNetworkTaskFactoryImpl destroyNetworkTaskFactory = new DestroyNetworkTaskFactoryImpl(cyNetworkManagerServiceRef);
		DestroyNetworkViewTaskFactoryImpl destroyNetworkViewTaskFactory = new DestroyNetworkViewTaskFactoryImpl(cyNetworkViewManagerServiceRef);
		ZoomInTaskFactory zoomInTaskFactory = new ZoomInTaskFactory(undoSupportServiceRef, cyApplicationManagerServiceRef);
		ZoomOutTaskFactory zoomOutTaskFactory = new ZoomOutTaskFactory(undoSupportServiceRef, cyApplicationManagerServiceRef);
		FitSelectedTaskFactory fitSelectedTaskFactory = new FitSelectedTaskFactory(undoSupportServiceRef, cyApplicationManagerServiceRef);
		FitContentTaskFactory fitContentTaskFactory = new FitContentTaskFactory(undoSupportServiceRef, cyApplicationManagerServiceRef);
		NewSessionTaskFactoryImpl newSessionTaskFactory = new NewSessionTaskFactoryImpl(cySessionManagerServiceRef, tunableSetterServiceRef);
		OpenSessionCommandTaskFactory openSessionCommandTaskFactory = new OpenSessionCommandTaskFactory(cySessionManagerServiceRef,sessionReaderManagerServiceRef,cyApplicationManagerServiceRef,cyNetworkManagerServiceRef,cyTableManagerServiceRef,cyNetworkTableManagerServiceRef,cyGroupManager,recentlyOpenedTrackerServiceRef);
		OpenSessionTaskFactoryImpl openSessionTaskFactory = new OpenSessionTaskFactoryImpl(cySessionManagerServiceRef,sessionReaderManagerServiceRef,cyApplicationManagerServiceRef,cyNetworkManagerServiceRef,cyTableManagerServiceRef,cyNetworkTableManagerServiceRef,cyGroupManager,recentlyOpenedTrackerServiceRef,tunableSetterServiceRef);
		SaveSessionTaskFactoryImpl saveSessionTaskFactory = new SaveSessionTaskFactoryImpl( sessionWriterManagerServiceRef, cySessionManagerServiceRef, recentlyOpenedTrackerServiceRef, cyEventHelperRef);
		SaveSessionAsTaskFactoryImpl saveSessionAsTaskFactory = new SaveSessionAsTaskFactoryImpl( sessionWriterManagerServiceRef, cySessionManagerServiceRef, recentlyOpenedTrackerServiceRef, cyEventHelperRef, tunableSetterServiceRef);
		ProxySettingsTaskFactoryImpl proxySettingsTaskFactory = new ProxySettingsTaskFactoryImpl(cyPropertyServiceRef, streamUtilRef);
		EditNetworkTitleTaskFactoryImpl editNetworkTitleTaskFactory = new EditNetworkTitleTaskFactoryImpl(undoSupportServiceRef, cyNetworkManagerServiceRef, cyNetworkNamingServiceRef, tunableSetterServiceRef);
		CreateNetworkViewTaskFactoryImpl createNetworkViewTaskFactory = new CreateNetworkViewTaskFactoryImpl(undoSupportServiceRef,cyNetworkViewFactoryServiceRef,cyNetworkViewManagerServiceRef,cyLayoutsServiceRef,cyEventHelperRef,visualMappingManagerServiceRef,renderingEngineManagerServiceRef,cyApplicationManagerServiceRef);
		ExportNetworkImageTaskFactoryImpl exportNetworkImageTaskFactory = new ExportNetworkImageTaskFactoryImpl(viewWriterManagerServiceRef,cyApplicationManagerServiceRef);
		ExportNetworkViewTaskFactoryImpl exportNetworkViewTaskFactory = new ExportNetworkViewTaskFactoryImpl(networkViewWriterManagerServiceRef, tunableSetterServiceRef);
		ExportSelectedTableTaskFactoryImpl exportCurrentTableTaskFactory = new ExportSelectedTableTaskFactoryImpl(cyTableWriterManagerRef, cyTableManagerServiceRef, cyNetworkManagerServiceRef);
		ApplyPreferredLayoutTaskFactoryImpl applyPreferredLayoutTaskFactory = new ApplyPreferredLayoutTaskFactoryImpl(cyApplicationManagerServiceRef, cyNetworkViewManagerServiceRef, cyLayoutsServiceRef,cyPropertyServiceRef);
		DeleteColumnTaskFactoryImpl deleteColumnTaskFactory = new DeleteColumnTaskFactoryImpl(undoSupportServiceRef);
		RenameColumnTaskFactoryImpl renameColumnTaskFactory = new RenameColumnTaskFactoryImpl(undoSupportServiceRef, tunableSetterServiceRef);
		
		CopyValueToColumnTaskFactoryImpl copyValueToEntireColumnTaskFactory = new CopyValueToColumnTaskFactoryImpl(undoSupportServiceRef, false, "Apply to entire column");
		CopyValueToColumnTaskFactoryImpl copyValueToSelectedNodesTaskFactory = new CopyValueToColumnTaskFactoryImpl(undoSupportServiceRef, true, "Apply to selected nodes");
		CopyValueToColumnTaskFactoryImpl copyValueToSelectedEdgesTaskFactory = new CopyValueToColumnTaskFactoryImpl(undoSupportServiceRef, true, "Apply to selected edges");

		DeleteTableTaskFactoryImpl deleteTableTaskFactory = new DeleteTableTaskFactoryImpl(cyTableManagerServiceRef);
		ExportVizmapTaskFactoryImpl exportVizmapTaskFactory = new ExportVizmapTaskFactoryImpl(vizmapWriterManagerServiceRef,visualMappingManagerServiceRef, tunableSetterServiceRef);
		ConnectSelectedNodesTaskFactoryImpl connectSelectedNodesTaskFactory = new ConnectSelectedNodesTaskFactoryImpl(undoSupportServiceRef, cyEventHelperRef, visualMappingManagerServiceRef, cyNetworkViewManagerServiceRef);
		MapGlobalToLocalTableTaskFactoryImpl mapGlobal = new MapGlobalToLocalTableTaskFactoryImpl(cyTableManagerServiceRef, cyNetworkManagerServiceRef, tunableSetterServiceRef);
		
		DynamicTaskFactoryProvisionerImpl dynamicTaskFactoryProvisionerImpl = new DynamicTaskFactoryProvisionerImpl(cyApplicationManagerServiceRef);
		registerAllServices(bc, dynamicTaskFactoryProvisionerImpl, new Properties());

		LoadAttributesFileTaskFactoryImpl loadAttrsFileTaskFactory = new LoadAttributesFileTaskFactoryImpl(cyDataTableReaderManagerServiceRef, tunableSetterServiceRef,cyNetworkManagerServiceRef, cyTableManagerServiceRef, rootNetworkManagerServiceRef );
		LoadAttributesURLTaskFactoryImpl loadAttrsURLTaskFactory = new LoadAttributesURLTaskFactoryImpl(cyDataTableReaderManagerServiceRef, tunableSetterServiceRef, cyNetworkManagerServiceRef, cyTableManagerServiceRef, rootNetworkManagerServiceRef);
		ImportAttributesFileTaskFactoryImpl importAttrsFileTaskFactory = new ImportAttributesFileTaskFactoryImpl(cyDataTableReaderManagerServiceRef, tunableSetterServiceRef,cyNetworkManagerServiceRef, cyTableManagerServiceRef, rootNetworkManagerServiceRef );
		ImportAttributesURLTaskFactoryImpl importAttrsURLTaskFactory = new ImportAttributesURLTaskFactoryImpl(cyDataTableReaderManagerServiceRef, tunableSetterServiceRef, cyNetworkManagerServiceRef, cyTableManagerServiceRef, rootNetworkManagerServiceRef);
		MergeDataTableTaskFactoryImpl mergeTableTaskFactory = new MergeDataTableTaskFactoryImpl( cyTableManagerServiceRef,cyNetworkManagerServiceRef,tunableSetterServiceRef, rootNetworkManagerServiceRef );
		// Apply Visual Style Task
		ApplyVisualStyleTaskFactoryimpl applyVisualStyleTaskFactory = new ApplyVisualStyleTaskFactoryimpl(visualMappingManagerServiceRef);
		Properties applyVisualStyleProps = new Properties();
		applyVisualStyleProps.setProperty(ID,"applyVisualStyleTaskFactory");
		applyVisualStyleProps.setProperty(TITLE, "Apply Visual Style");
		applyVisualStyleProps.setProperty(COMMAND,"apply");
		applyVisualStyleProps.setProperty(COMMAND_NAMESPACE,"vizmap");
		applyVisualStyleProps.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU,"true");
		applyVisualStyleProps.setProperty(ENABLE_FOR,"networkAndView");
		
		registerService(bc, applyVisualStyleTaskFactory, NetworkViewCollectionTaskFactory.class, applyVisualStyleProps);
		registerService(bc, applyVisualStyleTaskFactory, ApplyVisualStyleTaskFactory.class, applyVisualStyleProps);
		
		// Clear edge bends
		ClearEdgeBendTaskFactory clearEdgeBendTaskFactory = new ClearEdgeBendTaskFactory();
		Properties clearEdgeBendProps = new Properties();
		clearEdgeBendProps.setProperty(ID, "clearEdgeBendTaskFactory");
		clearEdgeBendProps.setProperty(TITLE, "Clear Edge Bends");
		clearEdgeBendProps.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU, "true");
		clearEdgeBendProps.setProperty(ENABLE_FOR, "networkAndView");
		clearEdgeBendProps.setProperty(PREFERRED_MENU,"Layout");
		clearEdgeBendProps.setProperty(MENU_GRAVITY,"0.1");

		registerService(bc, clearEdgeBendTaskFactory, NetworkViewCollectionTaskFactory.class, clearEdgeBendProps);
		
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
		//loadNetworkFileTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		//loadNetworkFileTaskFactoryProps.setProperty(COMMAND,"load file");
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
		//loadNetworkURLTaskFactoryProps.setProperty(COMMAND,"load url");
		//loadNetworkURLTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc, loadNetworkURLTaskFactory, TaskFactory.class, loadNetworkURLTaskFactoryProps);
		registerService(bc, loadNetworkURLTaskFactory, LoadNetworkURLTaskFactory.class, loadNetworkURLTaskFactoryProps);

		Properties loadVizmapFileTaskFactoryProps = new Properties();
		loadVizmapFileTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Import");
		loadVizmapFileTaskFactoryProps.setProperty(MENU_GRAVITY,"3.0");
		loadVizmapFileTaskFactoryProps.setProperty(TITLE,"Vizmap File...");
		loadVizmapFileTaskFactoryProps.setProperty(COMMAND,"load file");
		loadVizmapFileTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"vizmap");
		registerService(bc,loadVizmapFileTaskFactory,TaskFactory.class, loadVizmapFileTaskFactoryProps);
		registerService(bc,loadVizmapFileTaskFactory,LoadVizmapFileTaskFactory.class, new Properties());

		Properties importAttrsFileTaskFactoryProps = new Properties();
		importAttrsFileTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Import.Table");
		importAttrsFileTaskFactoryProps.setProperty(MENU_GRAVITY,"1.1");
		importAttrsFileTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"3.2");
		importAttrsFileTaskFactoryProps.setProperty(TITLE,"File...");
		importAttrsFileTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/table_file_import.png").toString());
		importAttrsFileTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		importAttrsFileTaskFactoryProps.setProperty(TOOLTIP,"Import Table From File");
		importAttrsFileTaskFactoryProps.setProperty(COMMAND,"load file");
		importAttrsFileTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"table");
		importAttrsFileTaskFactoryProps.setProperty(ENABLE_FOR,"network");
		registerService(bc,importAttrsFileTaskFactory,TaskFactory.class, importAttrsFileTaskFactoryProps);
		registerService(bc,importAttrsFileTaskFactory,ImportTableFileTaskFactory.class, importAttrsFileTaskFactoryProps);


		Properties importAttrsURLTaskFactoryProps = new Properties();
		importAttrsURLTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Import.Table");
		importAttrsURLTaskFactoryProps.setProperty(MENU_GRAVITY,"1.2");
		importAttrsURLTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"3.3");
		importAttrsURLTaskFactoryProps.setProperty(TITLE,"URL...");
		importAttrsURLTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/table_url_import.png").toString());
		importAttrsURLTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		importAttrsURLTaskFactoryProps.setProperty(TOOLTIP,"Import Table From URL");
		//importAttrsURLTaskFactoryProps.setProperty(COMMAND,"load url");
		//importAttrsURLTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"table");
		importAttrsURLTaskFactoryProps.setProperty(ENABLE_FOR,"network");
		registerService(bc,importAttrsURLTaskFactory,TaskFactory.class, importAttrsURLTaskFactoryProps);
		registerService(bc,importAttrsURLTaskFactory,ImportTableURLTaskFactory.class, importAttrsURLTaskFactoryProps);

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
		deleteSelectedNodesAndEdgesTaskFactoryProps.setProperty(COMMAND,"delete");
		deleteSelectedNodesAndEdgesTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,deleteSelectedNodesAndEdgesTaskFactory,NetworkTaskFactory.class, deleteSelectedNodesAndEdgesTaskFactoryProps);
		registerService(bc,deleteSelectedNodesAndEdgesTaskFactory,DeleteSelectedNodesAndEdgesTaskFactory.class, deleteSelectedNodesAndEdgesTaskFactoryProps);

		Properties selectAllTaskFactoryProps = new Properties();
		selectAllTaskFactoryProps.setProperty(PREFERRED_MENU,"Select");
		selectAllTaskFactoryProps.setProperty(ACCELERATOR,"cmd alt a");
		selectAllTaskFactoryProps.setProperty(ENABLE_FOR,"network");
		selectAllTaskFactoryProps.setProperty(TITLE,"Select all nodes and edges");
		selectAllTaskFactoryProps.setProperty(MENU_GRAVITY,"5.0");
		selectAllTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		// selectAllTaskFactoryProps.setProperty(COMMAND,"select all");
		// selectAllTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,selectAllTaskFactory,NetworkTaskFactory.class, selectAllTaskFactoryProps);
		registerService(bc,selectAllTaskFactory,SelectAllTaskFactory.class, selectAllTaskFactoryProps);

		Properties selectAllViewTaskFactoryProps = new Properties();
		selectAllViewTaskFactoryProps.setProperty(PREFERRED_MENU, NETWORK_SELECT_MENU);
		selectAllViewTaskFactoryProps.setProperty(ENABLE_FOR,"networkAndView");
		selectAllViewTaskFactoryProps.setProperty(TITLE,"All nodes and edges");
		selectAllViewTaskFactoryProps.setProperty(MENU_GRAVITY,"1.1");
		selectAllViewTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		selectAllViewTaskFactoryProps.setProperty(IN_MENU_BAR,"false");
		registerService(bc,selectAllTaskFactory,NetworkViewTaskFactory.class, selectAllViewTaskFactoryProps);

		Properties selectAllEdgesTaskFactoryProps = new Properties();
		selectAllEdgesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Edges");
		selectAllEdgesTaskFactoryProps.setProperty(ACCELERATOR,"cmd alt a");
		selectAllEdgesTaskFactoryProps.setProperty(ENABLE_FOR,"network");
		selectAllEdgesTaskFactoryProps.setProperty(TITLE,"Select all edges");
		selectAllEdgesTaskFactoryProps.setProperty(MENU_GRAVITY,"4.0");
		registerService(bc,selectAllEdgesTaskFactory,NetworkTaskFactory.class, selectAllEdgesTaskFactoryProps);
		registerService(bc,selectAllEdgesTaskFactory,SelectAllEdgesTaskFactory.class, selectAllEdgesTaskFactoryProps);

		Properties selectAllNodesTaskFactoryProps = new Properties();
		selectAllNodesTaskFactoryProps.setProperty(ENABLE_FOR,"network");
		selectAllNodesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Nodes");
		selectAllNodesTaskFactoryProps.setProperty(MENU_GRAVITY,"4.0");
		selectAllNodesTaskFactoryProps.setProperty(ACCELERATOR,"cmd a");
		selectAllNodesTaskFactoryProps.setProperty(TITLE,"Select all nodes");
		registerService(bc,selectAllNodesTaskFactory,NetworkTaskFactory.class, selectAllNodesTaskFactoryProps);
		registerService(bc,selectAllNodesTaskFactory,SelectAllNodesTaskFactory.class, selectAllNodesTaskFactoryProps);

		Properties selectAdjacentEdgesTaskFactoryProps = new Properties();
		selectAdjacentEdgesTaskFactoryProps.setProperty(ENABLE_FOR,"network");
		selectAdjacentEdgesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Edges");
		selectAdjacentEdgesTaskFactoryProps.setProperty(MENU_GRAVITY,"6.0");
		selectAdjacentEdgesTaskFactoryProps.setProperty(ACCELERATOR,"alt e");
		selectAdjacentEdgesTaskFactoryProps.setProperty(TITLE,"Select adjacent edges");
		// selectAdjacentEdgesTaskFactoryProps.setProperty(COMMAND,"select adjacent");
		// selectAdjacentEdgesTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"edge");
		registerService(bc,selectAdjacentEdgesTaskFactory,NetworkTaskFactory.class, selectAdjacentEdgesTaskFactoryProps);
		registerService(bc,selectAdjacentEdgesTaskFactory,SelectAdjacentEdgesTaskFactory.class, selectAdjacentEdgesTaskFactoryProps);

		Properties selectConnectedNodesTaskFactoryProps = new Properties();
		selectConnectedNodesTaskFactoryProps.setProperty(ENABLE_FOR,"network");
		selectConnectedNodesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Nodes");
		selectConnectedNodesTaskFactoryProps.setProperty(MENU_GRAVITY,"7.0");
		selectConnectedNodesTaskFactoryProps.setProperty(ACCELERATOR,"cmd 7");
		selectConnectedNodesTaskFactoryProps.setProperty(TITLE,"Nodes connected by selected edges");
		// selectConnectedNodesTaskFactoryProps.setProperty(COMMAND,"select by connected edges");
		// selectConnectedNodesTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"node");
		registerService(bc,selectConnectedNodesTaskFactory,NetworkTaskFactory.class, selectConnectedNodesTaskFactoryProps);
		registerService(bc,selectConnectedNodesTaskFactory,SelectConnectedNodesTaskFactory.class, selectConnectedNodesTaskFactoryProps);

		Properties selectFirstNeighborsTaskFactoryProps = new Properties();
		selectFirstNeighborsTaskFactoryProps.setProperty(ENABLE_FOR,"selectedNodesOrEdges");
		selectFirstNeighborsTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Nodes.First Neighbors of Selected Nodes");
		selectFirstNeighborsTaskFactoryProps.setProperty(MENU_GRAVITY,"6.0");
		selectFirstNeighborsTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"9.15");
		selectFirstNeighborsTaskFactoryProps.setProperty(ACCELERATOR,"cmd 6");
		selectFirstNeighborsTaskFactoryProps.setProperty(TITLE,"Undirected");
		selectFirstNeighborsTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/select_firstneighbors.png").toString());
		selectFirstNeighborsTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		selectFirstNeighborsTaskFactoryProps.setProperty(TOOLTIP,"First Neighbors of Selected Nodes (Undirected)");
		// selectFirstNeighborsTaskFactoryProps.setProperty(COMMAND,"select first neighbors undirected");
		// selectFirstNeighborsTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"node");
		registerService(bc,selectFirstNeighborsTaskFactory,NetworkTaskFactory.class, selectFirstNeighborsTaskFactoryProps);
		registerService(bc,selectFirstNeighborsTaskFactory,SelectFirstNeighborsTaskFactory.class, selectFirstNeighborsTaskFactoryProps);

		Properties selectFirstNeighborsTaskFactoryInEdgeProps = new Properties();
		selectFirstNeighborsTaskFactoryInEdgeProps.setProperty(ENABLE_FOR,"network");
		selectFirstNeighborsTaskFactoryInEdgeProps.setProperty(PREFERRED_MENU,"Select.Nodes.First Neighbors of Selected Nodes");
		selectFirstNeighborsTaskFactoryInEdgeProps.setProperty(MENU_GRAVITY,"6.1");
		selectFirstNeighborsTaskFactoryInEdgeProps.setProperty(TITLE,"Directed: Incoming");
		selectFirstNeighborsTaskFactoryInEdgeProps.setProperty(TOOLTIP,"First Neighbors of Selected Nodes (Directed: Incoming)");
		// selectFirstNeighborsTaskFactoryInEdgeProps.setProperty(COMMAND,"select first neighbors incoming");
		// selectFirstNeighborsTaskFactoryInEdgeProps.setProperty(COMMAND_NAMESPACE,"node");
		registerService(bc,selectFirstNeighborsTaskFactoryInEdge,NetworkTaskFactory.class, selectFirstNeighborsTaskFactoryInEdgeProps);
		registerService(bc,selectFirstNeighborsTaskFactoryInEdge,SelectFirstNeighborsTaskFactory.class, selectFirstNeighborsTaskFactoryInEdgeProps);

		Properties selectFirstNeighborsTaskFactoryOutEdgeProps = new Properties();
		selectFirstNeighborsTaskFactoryOutEdgeProps.setProperty(ENABLE_FOR,"network");
		selectFirstNeighborsTaskFactoryOutEdgeProps.setProperty(PREFERRED_MENU,"Select.Nodes.First Neighbors of Selected Nodes");
		selectFirstNeighborsTaskFactoryOutEdgeProps.setProperty(MENU_GRAVITY,"6.2");
		selectFirstNeighborsTaskFactoryOutEdgeProps.setProperty(TITLE,"Directed: Outgoing");
		selectFirstNeighborsTaskFactoryOutEdgeProps.setProperty(TOOLTIP,"First Neighbors of Selected Nodes (Directed: Outgoing)");
		// selectFirstNeighborsTaskFactoryOutEdgeProps.setProperty(COMMAND,"select first neighbors outgoing");
		// selectFirstNeighborsTaskFactoryOutEdgeProps.setProperty(COMMAND_NAMESPACE,"node");
		registerService(bc,selectFirstNeighborsTaskFactoryOutEdge,NetworkTaskFactory.class, selectFirstNeighborsTaskFactoryOutEdgeProps);
		registerService(bc,selectFirstNeighborsTaskFactoryOutEdge,SelectFirstNeighborsTaskFactory.class, selectFirstNeighborsTaskFactoryOutEdgeProps);		

		Properties deselectAllTaskFactoryProps = new Properties();
		deselectAllTaskFactoryProps.setProperty(ENABLE_FOR,"network");
		deselectAllTaskFactoryProps.setProperty(PREFERRED_MENU,"Select");
		deselectAllTaskFactoryProps.setProperty(MENU_GRAVITY,"5.1");
		deselectAllTaskFactoryProps.setProperty(ACCELERATOR,"cmd shift alt a");
		deselectAllTaskFactoryProps.setProperty(TITLE,"Deselect all nodes and edges");
		// deselectAllTaskFactoryProps.setProperty(COMMAND,"deselect all");
		// deselectAllTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,deselectAllTaskFactory,NetworkTaskFactory.class, deselectAllTaskFactoryProps);
		registerService(bc,deselectAllTaskFactory,DeselectAllTaskFactory.class, deselectAllTaskFactoryProps);

		Properties deselectAllEdgesTaskFactoryProps = new Properties();
		deselectAllEdgesTaskFactoryProps.setProperty(ENABLE_FOR,"network");
		deselectAllEdgesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Edges");
		deselectAllEdgesTaskFactoryProps.setProperty(MENU_GRAVITY,"5.0");
		deselectAllEdgesTaskFactoryProps.setProperty(ACCELERATOR,"alt shift a");
		deselectAllEdgesTaskFactoryProps.setProperty(TITLE,"Deselect all edges");
		registerService(bc,deselectAllEdgesTaskFactory,NetworkTaskFactory.class, deselectAllEdgesTaskFactoryProps);
		registerService(bc,deselectAllEdgesTaskFactory,DeselectAllEdgesTaskFactory.class, deselectAllEdgesTaskFactoryProps);

		Properties deselectAllNodesTaskFactoryProps = new Properties();
		deselectAllNodesTaskFactoryProps.setProperty(ENABLE_FOR,"network");
		deselectAllNodesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Nodes");
		deselectAllNodesTaskFactoryProps.setProperty(MENU_GRAVITY,"5.0");
		deselectAllNodesTaskFactoryProps.setProperty(ACCELERATOR,"cmd shift a");
		deselectAllNodesTaskFactoryProps.setProperty(TITLE,"Deselect all nodes");
		registerService(bc,deselectAllNodesTaskFactory,NetworkTaskFactory.class, deselectAllNodesTaskFactoryProps);
		registerService(bc,deselectAllNodesTaskFactory,DeselectAllNodesTaskFactory.class, deselectAllNodesTaskFactoryProps);

		Properties invertSelectedEdgesTaskFactoryProps = new Properties();
		invertSelectedEdgesTaskFactoryProps.setProperty(ENABLE_FOR,"network");
		invertSelectedEdgesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Edges");
		invertSelectedEdgesTaskFactoryProps.setProperty(MENU_GRAVITY,"1.0");
		invertSelectedEdgesTaskFactoryProps.setProperty(ACCELERATOR,"alt i");
		invertSelectedEdgesTaskFactoryProps.setProperty(TITLE,"Invert edge selection");
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
		invertSelectedNodesTaskFactoryProps.setProperty(IN_TOOL_BAR,"false");
		invertSelectedNodesTaskFactoryProps.setProperty(TOOLTIP,"Invert Node Selection");
		registerService(bc,invertSelectedNodesTaskFactory,NetworkTaskFactory.class, invertSelectedNodesTaskFactoryProps);
		registerService(bc,invertSelectedNodesTaskFactory,InvertSelectedNodesTaskFactory.class, invertSelectedNodesTaskFactoryProps);

		Properties selectFromFileListTaskFactoryProps = new Properties();
		selectFromFileListTaskFactoryProps.setProperty(ENABLE_FOR,"network");
		selectFromFileListTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Nodes");
		selectFromFileListTaskFactoryProps.setProperty(MENU_GRAVITY,"8.0");
		selectFromFileListTaskFactoryProps.setProperty(ACCELERATOR,"cmd i");
		selectFromFileListTaskFactoryProps.setProperty(TITLE,"From ID List file...");
		selectFromFileListTaskFactoryProps.setProperty(COMMAND,"select from file");
		selectFromFileListTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"node");
		registerService(bc,selectFromFileListTaskFactory,NetworkTaskFactory.class, selectFromFileListTaskFactoryProps);
		registerService(bc,selectFromFileListTaskFactory,SelectFromFileListTaskFactory.class, selectFromFileListTaskFactoryProps);

		Properties selectFirstNeighborsNodeViewTaskFactoryProps = new Properties();
		selectFirstNeighborsNodeViewTaskFactoryProps.setProperty(PREFERRED_MENU,NODE_SELECT_MENU);
		selectFirstNeighborsNodeViewTaskFactoryProps.setProperty(MENU_GRAVITY,"1.0");
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
		// hideSelectedTaskFactoryProps.setProperty(COMMAND,"hide selected");
		// hideSelectedTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"view");
		registerService(bc,hideSelectedTaskFactory,NetworkViewTaskFactory.class, hideSelectedTaskFactoryProps);
		registerService(bc,hideSelectedTaskFactory,HideSelectedTaskFactory.class, hideSelectedTaskFactoryProps);

		Properties hideSelectedNodesTaskFactoryProps = new Properties();
		hideSelectedNodesTaskFactoryProps.setProperty(ENABLE_FOR,"selectedNodes");
		hideSelectedNodesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Nodes");
		hideSelectedNodesTaskFactoryProps.setProperty(MENU_GRAVITY,"2.0");
		hideSelectedNodesTaskFactoryProps.setProperty(TITLE,"Hide selected nodes");
		// hideSelectedNodesTaskFactoryProps.setProperty(COMMAND,"hide selected nodes");
		// hideSelectedNodesTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"view");
		registerService(bc,hideSelectedNodesTaskFactory,NetworkViewTaskFactory.class, hideSelectedNodesTaskFactoryProps);
		registerService(bc,hideSelectedNodesTaskFactory,HideSelectedNodesTaskFactory.class, hideSelectedNodesTaskFactoryProps);

		Properties hideSelectedEdgesTaskFactoryProps = new Properties();
		hideSelectedEdgesTaskFactoryProps.setProperty(ENABLE_FOR,"selectedEdges");
		hideSelectedEdgesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Edges");
		hideSelectedEdgesTaskFactoryProps.setProperty(MENU_GRAVITY,"2.0");
		hideSelectedEdgesTaskFactoryProps.setProperty(TITLE,"Hide selected edges");
		// hideSelectedEdgesTaskFactoryProps.setProperty(COMMAND,"hide selected edges");
		// hideSelectedEdgesTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"view");
		registerService(bc,hideSelectedEdgesTaskFactory,NetworkViewTaskFactory.class, hideSelectedEdgesTaskFactoryProps);
		registerService(bc,hideSelectedEdgesTaskFactory,HideSelectedEdgesTaskFactory.class, hideSelectedEdgesTaskFactoryProps);

		Properties unHideAllTaskFactoryProps = new Properties();
		unHideAllTaskFactoryProps.setProperty(ENABLE_FOR,"networkAndView");
		unHideAllTaskFactoryProps.setProperty(PREFERRED_MENU,"Select");
		unHideAllTaskFactoryProps.setProperty(MENU_GRAVITY,"3.0");
		unHideAllTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"9.6");
		unHideAllTaskFactoryProps.setProperty(TITLE,"Show all nodes and edges");
		// unHideAllTaskFactoryProps.setProperty(COMMAND,"show all");
		// unHideAllTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"view");
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
		// unHideAllNodesTaskFactoryProps.setProperty(COMMAND,"show all nodes");
		// unHideAllNodesTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"view");
		registerService(bc,unHideAllNodesTaskFactory,NetworkViewTaskFactory.class, unHideAllNodesTaskFactoryProps);
		registerService(bc,unHideAllNodesTaskFactory,UnHideAllNodesTaskFactory.class, unHideAllNodesTaskFactoryProps);

		Properties unHideAllEdgesTaskFactoryProps = new Properties();
		unHideAllEdgesTaskFactoryProps.setProperty(ENABLE_FOR,"networkAndView");
		unHideAllEdgesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Edges");
		unHideAllEdgesTaskFactoryProps.setProperty(MENU_GRAVITY,"3.0");
		unHideAllEdgesTaskFactoryProps.setProperty(TITLE,"Show all edges");
		// unHideAllEdgesTaskFactoryProps.setProperty(COMMAND,"show all edges");
		// unHideAllEdgesTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"view");
		registerService(bc,unHideAllEdgesTaskFactory,NetworkViewTaskFactory.class, unHideAllEdgesTaskFactoryProps);
		registerService(bc,unHideAllEdgesTaskFactory,UnHideAllEdgesTaskFactory.class, unHideAllEdgesTaskFactoryProps);


		Properties newEmptyNetworkTaskFactoryProps = new Properties();
		newEmptyNetworkTaskFactoryProps.setProperty(PREFERRED_MENU,"File.New.Network");
		newEmptyNetworkTaskFactoryProps.setProperty(MENU_GRAVITY,"4.0");
		newEmptyNetworkTaskFactoryProps.setProperty(TITLE,"Empty Network");
		newEmptyNetworkTaskFactoryProps.setProperty(COMMAND,"create empty");
		newEmptyNetworkTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,newEmptyNetworkTaskFactory,TaskFactory.class, newEmptyNetworkTaskFactoryProps);
		registerService(bc,newEmptyNetworkTaskFactory,NewEmptyNetworkViewFactory.class, newEmptyNetworkTaskFactoryProps);

		Properties newNetworkSelectedNodesEdgesTaskFactoryProps = new Properties();
		newNetworkSelectedNodesEdgesTaskFactoryProps.setProperty(ENABLE_FOR,"selectedNodesOrEdges");
		newNetworkSelectedNodesEdgesTaskFactoryProps.setProperty(PREFERRED_MENU,"File.New.Network");
		newNetworkSelectedNodesEdgesTaskFactoryProps.setProperty(MENU_GRAVITY,"2.0");
		newNetworkSelectedNodesEdgesTaskFactoryProps.setProperty(ACCELERATOR,"cmd shift n");
		newNetworkSelectedNodesEdgesTaskFactoryProps.setProperty(TITLE,"From selected nodes, selected edges");
		// newNetworkSelectedNodesEdgesTaskFactoryProps.setProperty(COMMAND,"create from selected nodes and edges");
		// newNetworkSelectedNodesEdgesTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,newNetworkSelectedNodesEdgesTaskFactory,NetworkTaskFactory.class, newNetworkSelectedNodesEdgesTaskFactoryProps);
		registerService(bc,newNetworkSelectedNodesEdgesTaskFactory,NewNetworkSelectedNodesAndEdgesTaskFactory.class, newNetworkSelectedNodesEdgesTaskFactoryProps);

		Properties newNetworkSelectedNodesOnlyTaskFactoryProps = new Properties();
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty(PREFERRED_MENU,"File.New.Network");
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/new_fromselected.png").toString());
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty(ACCELERATOR,"cmd n");
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty(ENABLE_FOR,"selectedNodes");
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty(TITLE,"From selected nodes, all edges");
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"9.1");
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty(MENU_GRAVITY,"1.0");
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty(TOOLTIP,"New Network From Selection");
		// newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty(COMMAND,"create from selected nodes and all edges");
		// newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,newNetworkSelectedNodesOnlyTaskFactory,NetworkTaskFactory.class, newNetworkSelectedNodesOnlyTaskFactoryProps);
		registerService(bc,newNetworkSelectedNodesOnlyTaskFactory,NewNetworkSelectedNodesOnlyTaskFactory.class, newNetworkSelectedNodesOnlyTaskFactoryProps);

		Properties newNetworkCommandTaskFactoryProps = new Properties();
		newNetworkCommandTaskFactoryProps.setProperty(COMMAND,"create");
		newNetworkCommandTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,newNetworkCommandTaskFactory,NetworkTaskFactory.class, newNetworkCommandTaskFactoryProps);

		Properties cloneNetworkTaskFactoryProps = new Properties();
		cloneNetworkTaskFactoryProps.setProperty(ENABLE_FOR,"network");
		cloneNetworkTaskFactoryProps.setProperty(PREFERRED_MENU,"File.New.Network");
		cloneNetworkTaskFactoryProps.setProperty(MENU_GRAVITY,"3.0");
		cloneNetworkTaskFactoryProps.setProperty(TITLE,"Clone Current Network");
		cloneNetworkTaskFactoryProps.setProperty(COMMAND,"clone");
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
		//destroyNetworkTaskFactoryProps.setProperty(COMMAND,"destroy");
		//destroyNetworkTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,destroyNetworkTaskFactory,NetworkCollectionTaskFactory.class, destroyNetworkTaskFactoryProps);
		registerService(bc,destroyNetworkTaskFactory,DestroyNetworkTaskFactory.class, destroyNetworkTaskFactoryProps);
		Properties destroyNetworkTaskFactoryProps2 = new Properties();
		destroyNetworkTaskFactoryProps2.setProperty(ENABLE_FOR,"network");
		destroyNetworkTaskFactoryProps2.setProperty(COMMAND,"destroy");
		destroyNetworkTaskFactoryProps2.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,destroyNetworkTaskFactory,TaskFactory.class, destroyNetworkTaskFactoryProps2);


		Properties destroyNetworkViewTaskFactoryProps = new Properties();
		destroyNetworkViewTaskFactoryProps.setProperty(PREFERRED_MENU,"Edit");
		destroyNetworkViewTaskFactoryProps.setProperty(ACCELERATOR,"cmd w");
		destroyNetworkViewTaskFactoryProps.setProperty(ENABLE_FOR,"networkAndView");
		destroyNetworkViewTaskFactoryProps.setProperty(TITLE,"Destroy View");
		destroyNetworkViewTaskFactoryProps.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU,"true");
		destroyNetworkViewTaskFactoryProps.setProperty(MENU_GRAVITY,"3.1");
		destroyNetworkViewTaskFactoryProps.setProperty(COMMAND,"destroy");
		destroyNetworkViewTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"view");
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
//		zoomInTaskFactoryProps.setProperty(COMMAND,"zoom in");
//		zoomInTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"view");
		registerService(bc,zoomInTaskFactory,NetworkTaskFactory.class, zoomInTaskFactoryProps);

		Properties zoomOutTaskFactoryProps = new Properties();
		zoomOutTaskFactoryProps.setProperty(ACCELERATOR,"cmd minus");
		zoomOutTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/stock_zoom-out.png").toString());
		zoomOutTaskFactoryProps.setProperty(ENABLE_FOR,"networkAndView");
		zoomOutTaskFactoryProps.setProperty(TITLE,"Zoom Out");
		zoomOutTaskFactoryProps.setProperty(TOOLTIP,"Zoom Out");
		zoomOutTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"5.2");
		zoomOutTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
//		zoomOutTaskFactoryProps.setProperty(COMMAND,"zoom out");
//		zoomOutTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"view");
		registerService(bc,zoomOutTaskFactory,NetworkTaskFactory.class, zoomOutTaskFactoryProps);

		Properties fitSelectedTaskFactoryProps = new Properties();
		fitSelectedTaskFactoryProps.setProperty(ACCELERATOR,"cmd shift f");
		fitSelectedTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/stock_zoom-object.png").toString());
		fitSelectedTaskFactoryProps.setProperty(ENABLE_FOR,"networkAndView");
		fitSelectedTaskFactoryProps.setProperty(TITLE,"Fit Selected");
		fitSelectedTaskFactoryProps.setProperty(TOOLTIP,"Zoom selected region");
		fitSelectedTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"5.4");
		fitSelectedTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		fitSelectedTaskFactoryProps.setProperty(COMMAND,"fit selected");
		fitSelectedTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"view");
		registerService(bc,fitSelectedTaskFactory,NetworkTaskFactory.class, fitSelectedTaskFactoryProps);

		Properties fitContentTaskFactoryProps = new Properties();
		fitContentTaskFactoryProps.setProperty(ACCELERATOR,"cmd f");
		fitContentTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/stock_zoom-1.png").toString());
		fitContentTaskFactoryProps.setProperty(ENABLE_FOR,"networkAndView");
		fitContentTaskFactoryProps.setProperty(TITLE,"Fit Content");
		fitContentTaskFactoryProps.setProperty(TOOLTIP,"Zoom out to display all of current Network");
		fitContentTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"5.3");
		fitContentTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		fitContentTaskFactoryProps.setProperty(COMMAND,"fit content");
		fitContentTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"view");
		registerService(bc,fitContentTaskFactory,NetworkTaskFactory.class, fitContentTaskFactoryProps);

		Properties editNetworkTitleTaskFactoryProps = new Properties();
		editNetworkTitleTaskFactoryProps.setProperty(ENABLE_FOR,"singleNetwork");
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
		// No ENABLE_FOR because that is handled by the isReady() methdod of the task factory.
		createNetworkViewTaskFactoryProps.setProperty(PREFERRED_MENU,"Edit");
		createNetworkViewTaskFactoryProps.setProperty(MENU_GRAVITY,"3.0");
		createNetworkViewTaskFactoryProps.setProperty(TITLE,"Create View");
		createNetworkViewTaskFactoryProps.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU,"true");
		createNetworkViewTaskFactoryProps.setProperty(COMMAND,"create");
		createNetworkViewTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"view");
		registerService(bc,createNetworkViewTaskFactory,NetworkCollectionTaskFactory.class, createNetworkViewTaskFactoryProps);
		registerService(bc,createNetworkViewTaskFactory,CreateNetworkViewTaskFactory.class, createNetworkViewTaskFactoryProps);
		// For commands
		registerService(bc,createNetworkViewTaskFactory,TaskFactory.class, createNetworkViewTaskFactoryProps);

		Properties exportNetworkImageTaskFactoryProps = new Properties();
		exportNetworkImageTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Export");
		exportNetworkImageTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/img_file_export.png").toString());
		exportNetworkImageTaskFactoryProps.setProperty(ENABLE_FOR,"networkAndView");
		exportNetworkImageTaskFactoryProps.setProperty(MENU_GRAVITY,"1.2");
		exportNetworkImageTaskFactoryProps.setProperty(TITLE,"Network View as Graphics...");
		exportNetworkImageTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"3.7");
		exportNetworkImageTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		exportNetworkImageTaskFactoryProps.setProperty(IN_CONTEXT_MENU,"false");
		exportNetworkImageTaskFactoryProps.setProperty(TOOLTIP,"Export Network Image to File");
		exportNetworkImageTaskFactoryProps.setProperty(COMMAND,"export");
		exportNetworkImageTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"view");
		registerService(bc,exportNetworkImageTaskFactory,NetworkViewTaskFactory.class, exportNetworkImageTaskFactoryProps);
		registerService(bc,exportNetworkImageTaskFactory,ExportNetworkImageTaskFactory.class, exportNetworkImageTaskFactoryProps);
		

		Properties exportNetworkViewTaskFactoryProps = new Properties();
		exportNetworkViewTaskFactoryProps.setProperty(ENABLE_FOR,"networkAndView");
		exportNetworkViewTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Export");
		exportNetworkViewTaskFactoryProps.setProperty(MENU_GRAVITY,"1.1");
		exportNetworkViewTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"3.5");
		exportNetworkViewTaskFactoryProps.setProperty(TITLE,"Network...");
		exportNetworkViewTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/net_file_export.png").toString());
		exportNetworkViewTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		exportNetworkViewTaskFactoryProps.setProperty(IN_CONTEXT_MENU,"false");
		exportNetworkViewTaskFactoryProps.setProperty(TOOLTIP,"Export Network to File");
		exportNetworkViewTaskFactoryProps.setProperty(COMMAND,"export");
		exportNetworkViewTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,exportNetworkViewTaskFactory,NetworkViewTaskFactory.class, exportNetworkViewTaskFactoryProps);
		registerService(bc,exportNetworkViewTaskFactory,ExportNetworkViewTaskFactory.class, exportNetworkViewTaskFactoryProps);

		Properties exportCurrentTableTaskFactoryProps = new Properties();
		exportCurrentTableTaskFactoryProps.setProperty(ENABLE_FOR,"table");
		exportCurrentTableTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Export");
		exportCurrentTableTaskFactoryProps.setProperty(MENU_GRAVITY,"1.3");
		exportCurrentTableTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"3.6");
		exportCurrentTableTaskFactoryProps.setProperty(TITLE,"Table...");
		exportCurrentTableTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/table_file_export.png").toString());
		exportCurrentTableTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		exportCurrentTableTaskFactoryProps.setProperty(TOOLTIP,"Export Table to File");
		exportCurrentTableTaskFactoryProps.setProperty(COMMAND,"export");
		exportCurrentTableTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"table");
		registerService(bc,exportCurrentTableTaskFactory, TaskFactory.class, exportCurrentTableTaskFactoryProps);
		registerService(bc,exportCurrentTableTaskFactory,ExportSelectedTableTaskFactory.class, exportCurrentTableTaskFactoryProps);

		Properties exportVizmapTaskFactoryProps = new Properties();
		exportVizmapTaskFactoryProps.setProperty(ENABLE_FOR,"vizmap");
		exportVizmapTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Export");
		exportVizmapTaskFactoryProps.setProperty(MENU_GRAVITY,"1.4");
		exportVizmapTaskFactoryProps.setProperty(TITLE,"Vizmap...");
		exportVizmapTaskFactoryProps.setProperty(COMMAND,"export");
		exportVizmapTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"vizmap");
		registerService(bc,exportVizmapTaskFactory,TaskFactory.class, exportVizmapTaskFactoryProps);
		registerService(bc,exportVizmapTaskFactory,ExportVizmapTaskFactory.class, exportVizmapTaskFactoryProps);
		
		Properties loadDataFileTaskFactoryProps = new Properties();
		loadDataFileTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Load Data Table");
		loadDataFileTaskFactoryProps.setProperty(MENU_GRAVITY,"1.1");
		loadDataFileTaskFactoryProps.setProperty(TITLE,"File...");
		//loadDataFileTaskFactoryProps.setProperty(ServiceProperties.INSERT_SEPARATOR_BEFORE, "true");
		loadDataFileTaskFactoryProps.setProperty(TOOLTIP,"Load Data Table From File");
		//loadDataFileTaskFactoryProps.setProperty(COMMAND,"load file");
		//loadDataFileTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"table");
		registerService(bc,loadAttrsFileTaskFactory,TaskFactory.class, loadDataFileTaskFactoryProps);
		registerService(bc,loadAttrsFileTaskFactory,LoadTableFileTaskFactory.class, loadDataFileTaskFactoryProps);


		Properties loadDataURLTaskFactoryProps = new Properties();
		loadDataURLTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Load Data Table");
		loadDataURLTaskFactoryProps.setProperty(MENU_GRAVITY,"1.2");
		loadDataURLTaskFactoryProps.setProperty(TITLE,"URL...");
		loadDataURLTaskFactoryProps.setProperty(TOOLTIP,"Load Data Table From URL");
		//loadDataURLTaskFactoryProps.setProperty(COMMAND,"load url");
		//loadDataURLTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"table");
		registerService(bc,loadAttrsURLTaskFactory,TaskFactory.class, loadDataURLTaskFactoryProps);
		registerService(bc,loadAttrsURLTaskFactory,LoadTableURLTaskFactory.class, loadDataURLTaskFactoryProps);
		
		Properties MergeGlobalTaskFactoryProps = new Properties();
		MergeGlobalTaskFactoryProps.setProperty(ENABLE_FOR,"network");
		MergeGlobalTaskFactoryProps.setProperty(PREFERRED_MENU,"File");
		MergeGlobalTaskFactoryProps.setProperty(TITLE,"Merge Data Table...");
		//MergeGlobalTaskFactoryProps.setProperty(ServiceProperties.INSERT_SEPARATOR_AFTER, "true");
		//MergeGlobalTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"1.1");
		MergeGlobalTaskFactoryProps.setProperty(MENU_GRAVITY,"5.4");
		MergeGlobalTaskFactoryProps.setProperty(TOOLTIP,"Merge Data Table");
		MergeGlobalTaskFactoryProps.setProperty(COMMAND,"merge");
		MergeGlobalTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"table");
		registerService(bc,mergeTableTaskFactory,TaskFactory.class, MergeGlobalTaskFactoryProps);
		registerService(bc,mergeTableTaskFactory,MergeDataTableTaskFactory.class, MergeGlobalTaskFactoryProps);


		Properties newSessionTaskFactoryProps = new Properties();
		newSessionTaskFactoryProps.setProperty(PREFERRED_MENU,"File.New");
		newSessionTaskFactoryProps.setProperty(MENU_GRAVITY,"1.1");
		newSessionTaskFactoryProps.setProperty(TITLE,"Session");
		newSessionTaskFactoryProps.setProperty(COMMAND,"new");
		newSessionTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"session");
		registerService(bc,newSessionTaskFactory,TaskFactory.class, newSessionTaskFactoryProps);
		registerService(bc,newSessionTaskFactory,NewSessionTaskFactory.class, newSessionTaskFactoryProps);

		Properties openSessionTaskFactoryProps = new Properties();
		openSessionTaskFactoryProps.setProperty(ID,"openSessionTaskFactory");
		openSessionTaskFactoryProps.setProperty(PREFERRED_MENU,"File");
		openSessionTaskFactoryProps.setProperty(ACCELERATOR,"cmd o");
		openSessionTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/open_session.png").toString());
		openSessionTaskFactoryProps.setProperty(TITLE,"Open...");
		openSessionTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"1.0");
		openSessionTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		openSessionTaskFactoryProps.setProperty(MENU_GRAVITY,"1.0");
		openSessionTaskFactoryProps.setProperty(TOOLTIP,"Open Session");
		registerService(bc,openSessionTaskFactory,OpenSessionTaskFactory.class, openSessionTaskFactoryProps);
		registerService(bc,openSessionTaskFactory,TaskFactory.class, openSessionTaskFactoryProps);

		// We can't use the "normal" OpenSessionTaskFactory for commands
		// because it inserts the task with the file tunable in it, so the
		// Command processor never sees it, so we need a special OpenSessionTaskFactory
		// for commands
		// openSessionTaskFactoryProps.setProperty(COMMAND,"open");
		// openSessionTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"session");
		// registerService(bc,openSessionTaskFactory,TaskFactory.class, openSessionTaskFactoryProps);

		Properties openSessionCommandTaskFactoryProps = new Properties();
		openSessionCommandTaskFactoryProps.setProperty(COMMAND,"open");
		openSessionCommandTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"session");
		registerService(bc,openSessionCommandTaskFactory,TaskFactory.class, openSessionCommandTaskFactoryProps);

		Properties saveSessionTaskFactoryProps = new Properties();
		saveSessionTaskFactoryProps.setProperty(PREFERRED_MENU,"File");
		saveSessionTaskFactoryProps.setProperty(ACCELERATOR,"cmd s");
		saveSessionTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/stock_save.png").toString());
		saveSessionTaskFactoryProps.setProperty(TITLE,"Save");
		saveSessionTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"1.1");
		saveSessionTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		saveSessionTaskFactoryProps.setProperty(MENU_GRAVITY,"3.0");
		saveSessionTaskFactoryProps.setProperty(TOOLTIP,"Save Session");
		saveSessionTaskFactoryProps.setProperty(COMMAND,"save");
		saveSessionTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"session");
		registerService(bc,saveSessionTaskFactory,TaskFactory.class, saveSessionTaskFactoryProps);

		Properties saveSessionAsTaskFactoryProps = new Properties();
		saveSessionAsTaskFactoryProps.setProperty(PREFERRED_MENU,"File");
		saveSessionAsTaskFactoryProps.setProperty(ACCELERATOR,"cmd shift s");
		saveSessionAsTaskFactoryProps.setProperty(MENU_GRAVITY,"3.1");
		saveSessionAsTaskFactoryProps.setProperty(TITLE,"Save As...");
		saveSessionAsTaskFactoryProps.setProperty(COMMAND,"save as");
		saveSessionAsTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"session");
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
		registerService(bc,applyPreferredLayoutTaskFactory,NetworkViewCollectionTaskFactory.class, applyPreferredLayoutTaskFactoryProps);
		registerService(bc,applyPreferredLayoutTaskFactory,ApplyPreferredLayoutTaskFactory.class, applyPreferredLayoutTaskFactoryProps);

		// For commands
		Properties applyPreferredLayoutTaskFactoryProps2 = new Properties();
		applyPreferredLayoutTaskFactoryProps2.setProperty(COMMAND,"apply preferred");
		applyPreferredLayoutTaskFactoryProps2.setProperty(COMMAND_NAMESPACE,"layout");
		registerService(bc,applyPreferredLayoutTaskFactory,TaskFactory.class, applyPreferredLayoutTaskFactoryProps2);

		Properties deleteColumnTaskFactoryProps = new Properties();
		deleteColumnTaskFactoryProps.setProperty(TITLE,"Delete column");
		deleteColumnTaskFactoryProps.setProperty(COMMAND,"delete column");
		deleteColumnTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"table");
		registerService(bc,deleteColumnTaskFactory,TableColumnTaskFactory.class, deleteColumnTaskFactoryProps);
		registerService(bc,deleteColumnTaskFactory,DeleteColumnTaskFactory.class, deleteColumnTaskFactoryProps);

		Properties renameColumnTaskFactoryProps = new Properties();
		renameColumnTaskFactoryProps.setProperty(TITLE,"Rename column");
		renameColumnTaskFactoryProps.setProperty(COMMAND,"rename column");
		renameColumnTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"table");
		registerService(bc,renameColumnTaskFactory,TableColumnTaskFactory.class, renameColumnTaskFactoryProps);
		registerService(bc,renameColumnTaskFactory,RenameColumnTaskFactory.class, renameColumnTaskFactoryProps);

		Properties copyValueToEntireColumnTaskFactoryProps = new Properties();
		copyValueToEntireColumnTaskFactoryProps.setProperty(TITLE,copyValueToEntireColumnTaskFactory.getTaskFactoryName());
		copyValueToEntireColumnTaskFactoryProps.setProperty("tableTypes", "node,edge,network,unassigned");
		registerService(bc,copyValueToEntireColumnTaskFactory,TableCellTaskFactory.class, copyValueToEntireColumnTaskFactoryProps);

		Properties copyValueToSelectedNodesTaskFactoryProps = new Properties();
		copyValueToSelectedNodesTaskFactoryProps.setProperty(TITLE,copyValueToSelectedNodesTaskFactory.getTaskFactoryName());
		copyValueToSelectedNodesTaskFactoryProps.setProperty("tableTypes", "node");
		registerService(bc,copyValueToSelectedNodesTaskFactory,TableCellTaskFactory.class, copyValueToSelectedNodesTaskFactoryProps);
		
		Properties copyValueToSelectedEdgesTaskFactoryProps = new Properties();
		copyValueToSelectedEdgesTaskFactoryProps.setProperty(TITLE,copyValueToSelectedEdgesTaskFactory.getTaskFactoryName());
		copyValueToSelectedEdgesTaskFactoryProps.setProperty("tableTypes", "edge");
		registerService(bc,copyValueToSelectedEdgesTaskFactory,TableCellTaskFactory.class, copyValueToSelectedEdgesTaskFactoryProps);
		
		
		registerService(bc,deleteTableTaskFactory,TableTaskFactory.class, new Properties());
		registerService(bc,deleteTableTaskFactory,DeleteTableTaskFactory.class, new Properties());
		
		// Register as 3 types of service.
		Properties connectSelectedNodesTaskFactoryProps = new Properties();
		connectSelectedNodesTaskFactoryProps.setProperty(IN_MENU_BAR,"false");
		connectSelectedNodesTaskFactoryProps.setProperty(IN_TOOL_BAR,"false");
		connectSelectedNodesTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		connectSelectedNodesTaskFactoryProps.setProperty(PREFERRED_MENU, NODE_ADD_MENU);
		connectSelectedNodesTaskFactoryProps.setProperty(MENU_GRAVITY, "0.2");
		connectSelectedNodesTaskFactoryProps.setProperty(TITLE, "Edges Connecting Selected Nodes");
		connectSelectedNodesTaskFactoryProps.setProperty(COMMAND, "connect selected nodes");
		connectSelectedNodesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
//		registerService(bc, connectSelectedNodesTaskFactory, NetworkTaskFactory.class,
//				connectSelectedNodesTaskFactoryProps);
		registerService(bc, connectSelectedNodesTaskFactory, NodeViewTaskFactory.class,
				connectSelectedNodesTaskFactoryProps);
		registerService(bc, connectSelectedNodesTaskFactory, ConnectSelectedNodesTaskFactory.class,
				connectSelectedNodesTaskFactoryProps);

		GroupNodesTaskFactoryImpl groupNodesTaskFactory = 
			new GroupNodesTaskFactoryImpl(cyApplicationManagerServiceRef, cyGroupManager, 
			                              cyGroupFactory, undoSupportServiceRef);
		Properties groupNodesTaskFactoryProps = new Properties();
		groupNodesTaskFactoryProps.setProperty(PREFERRED_MENU,NETWORK_GROUP_MENU);
		groupNodesTaskFactoryProps.setProperty(TITLE,"Group Selected Nodes");
		groupNodesTaskFactoryProps.setProperty(TOOLTIP,"Group Selected Nodes Together");
		groupNodesTaskFactoryProps.setProperty(IN_TOOL_BAR,"false");
		groupNodesTaskFactoryProps.setProperty(MENU_GRAVITY,"0.0");
		groupNodesTaskFactoryProps.setProperty(IN_MENU_BAR,"false");
		groupNodesTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		registerService(bc,groupNodesTaskFactory,NetworkViewTaskFactory.class, groupNodesTaskFactoryProps);
		registerService(bc,groupNodesTaskFactory,GroupNodesTaskFactory.class, groupNodesTaskFactoryProps);
		// For commands
		groupNodesTaskFactoryProps = new Properties();
		groupNodesTaskFactoryProps.setProperty(COMMAND, "create");
		groupNodesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "group");
		registerService(bc,groupNodesTaskFactory,TaskFactory.class, groupNodesTaskFactoryProps);

		// Add Group Selected Nodes to the nodes context also
		Properties groupNodeViewTaskFactoryProps = new Properties();
		groupNodeViewTaskFactoryProps.setProperty(PREFERRED_MENU,NODE_GROUP_MENU);
		groupNodeViewTaskFactoryProps.setProperty(MENU_GRAVITY, "0.0");
		groupNodeViewTaskFactoryProps.setProperty(TITLE,"Group Selected Nodes");
		groupNodeViewTaskFactoryProps.setProperty(TOOLTIP,"Group Selected Nodes Together");
		groupNodeViewTaskFactoryProps.setProperty(IN_TOOL_BAR,"false");
		groupNodeViewTaskFactoryProps.setProperty(IN_MENU_BAR,"false");
		groupNodeViewTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		registerService(bc,groupNodesTaskFactory,NodeViewTaskFactory.class, groupNodeViewTaskFactoryProps);

		UnGroupNodesTaskFactoryImpl unGroupTaskFactory = 
			new UnGroupNodesTaskFactoryImpl(cyApplicationManagerServiceRef, cyGroupManager, 
			                                cyGroupFactory, undoSupportServiceRef);
		Properties unGroupNodesTaskFactoryProps = new Properties();
		unGroupNodesTaskFactoryProps.setProperty(PREFERRED_MENU,NETWORK_GROUP_MENU);
		unGroupNodesTaskFactoryProps.setProperty(TITLE,"Ungroup Selected Nodes");
		unGroupNodesTaskFactoryProps.setProperty(TOOLTIP,"Ungroup Selected Nodes");
		unGroupNodesTaskFactoryProps.setProperty(IN_TOOL_BAR,"false");
		unGroupNodesTaskFactoryProps.setProperty(IN_MENU_BAR,"false");
		unGroupNodesTaskFactoryProps.setProperty(MENU_GRAVITY,"1.0");
		unGroupNodesTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		registerService(bc,unGroupTaskFactory,NetworkViewTaskFactory.class, unGroupNodesTaskFactoryProps);
		registerService(bc,unGroupTaskFactory,UnGroupTaskFactory.class, unGroupNodesTaskFactoryProps);

		unGroupNodesTaskFactoryProps = new Properties();
		unGroupNodesTaskFactoryProps.setProperty(COMMAND, "ungroup");
		groupNodesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "group");
		registerService(bc,unGroupTaskFactory,TaskFactory.class, unGroupNodesTaskFactoryProps);

		// Add Ungroup Selected Nodes to the nodes context also
		Properties unGroupNodeViewTaskFactoryProps = new Properties();
		unGroupNodeViewTaskFactoryProps.setProperty(PREFERRED_MENU,NODE_GROUP_MENU);
		unGroupNodeViewTaskFactoryProps.setProperty(MENU_GRAVITY, "1.0");
		unGroupNodeViewTaskFactoryProps.setProperty(INSERT_SEPARATOR_AFTER, "true");
		unGroupNodeViewTaskFactoryProps.setProperty(TITLE,"Ungroup Selected Nodes");
		unGroupNodeViewTaskFactoryProps.setProperty(TOOLTIP,"Ungroup Selected Nodes");
		unGroupNodeViewTaskFactoryProps.setProperty(IN_TOOL_BAR,"false");
		unGroupNodeViewTaskFactoryProps.setProperty(IN_MENU_BAR,"false");
		unGroupNodeViewTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		registerService(bc,unGroupTaskFactory,NodeViewTaskFactory.class, unGroupNodeViewTaskFactoryProps);
		registerService(bc,unGroupTaskFactory,UnGroupNodesTaskFactory.class, unGroupNodeViewTaskFactoryProps);

		GroupNodeContextTaskFactoryImpl collapseGroupTaskFactory = 
			new GroupNodeContextTaskFactoryImpl(cyApplicationManagerServiceRef, 
			                                    cyNetworkViewManagerServiceRef, cyGroupManager, true);
		Properties collapseGroupTaskFactoryProps = new Properties();
		collapseGroupTaskFactoryProps.setProperty(PREFERRED_MENU,NODE_GROUP_MENU);
		collapseGroupTaskFactoryProps.setProperty(TITLE,"Collapse Group(s)");
		collapseGroupTaskFactoryProps.setProperty(TOOLTIP,"Collapse Grouped Nodes");
		collapseGroupTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		collapseGroupTaskFactoryProps.setProperty(MENU_GRAVITY, "2.0");
		registerService(bc,collapseGroupTaskFactory,NodeViewTaskFactory.class, collapseGroupTaskFactoryProps);
		registerService(bc,collapseGroupTaskFactory,CollapseGroupTaskFactory.class, collapseGroupTaskFactoryProps);
		collapseGroupTaskFactoryProps = new Properties();
		collapseGroupTaskFactoryProps.setProperty(COMMAND, "collapse");
		collapseGroupTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "group"); // TODO right namespace?
		registerService(bc,collapseGroupTaskFactory,TaskFactory.class, collapseGroupTaskFactoryProps);

		GroupNodeContextTaskFactoryImpl expandGroupTaskFactory = 
			new GroupNodeContextTaskFactoryImpl(cyApplicationManagerServiceRef, 
			                                    cyNetworkViewManagerServiceRef, cyGroupManager, false);
		Properties expandGroupTaskFactoryProps = new Properties();
		expandGroupTaskFactoryProps.setProperty(PREFERRED_MENU,NODE_GROUP_MENU);
		expandGroupTaskFactoryProps.setProperty(TITLE,"Expand Group(s)");
		expandGroupTaskFactoryProps.setProperty(TOOLTIP,"Expand Group");
		expandGroupTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		expandGroupTaskFactoryProps.setProperty(MENU_GRAVITY, "3.0");
		registerService(bc,expandGroupTaskFactory,NodeViewTaskFactory.class, expandGroupTaskFactoryProps);
		registerService(bc,expandGroupTaskFactory,ExpandGroupTaskFactory.class, expandGroupTaskFactoryProps);
		expandGroupTaskFactoryProps = new Properties();
		expandGroupTaskFactoryProps.setProperty(COMMAND, "expand");
		expandGroupTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "group"); // TODO right namespace

		registerService(bc,expandGroupTaskFactory,TaskFactory.class, expandGroupTaskFactoryProps);

		// TODO: add to group...

		// TODO: remove from group...

		MapTableToNetworkTablesTaskFactoryImpl mapNetworkToTables = new MapTableToNetworkTablesTaskFactoryImpl(cyNetworkManagerServiceRef, tunableSetterServiceRef, rootNetworkManagerServiceRef);
		Properties mapNetworkToTablesProps = new Properties();
		registerService(bc, mapNetworkToTables, MapTableToNetworkTablesTaskFactory.class, mapNetworkToTablesProps);
		
		ImportDataTableTaskFactoryImpl importTableTaskFactory = new ImportDataTableTaskFactoryImpl(cyNetworkManagerServiceRef,tunableSetterServiceRef,rootNetworkManagerServiceRef);
		Properties importTablesProps = new Properties();
		registerService(bc, importTableTaskFactory, ImportDataTableTaskFactory.class, importTablesProps);
		
		ExportTableTaskFactoryImpl exportTableTaskFactory = new ExportTableTaskFactoryImpl(cyTableWriterManagerRef,tunableSetterServiceRef);
		Properties exportTableTaskFactoryProps = new Properties();
		registerService(bc,exportTableTaskFactory,ExportTableTaskFactory.class,exportTableTaskFactoryProps);
		

		// These are task factories that are only available to the command line

		// NAMESPACE: edge
		CreateNetworkAttributeTaskFactory createEdgeAttributeTaskFactory = 
			new CreateNetworkAttributeTaskFactory(cyApplicationManagerServiceRef, 
		                                        cyTableManagerServiceRef, CyEdge.class);
		Properties createEdgeAttributeTaskFactoryProps = new Properties();
		createEdgeAttributeTaskFactoryProps.setProperty(COMMAND, "create attribute");
		createEdgeAttributeTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "edge");
		registerService(bc,createEdgeAttributeTaskFactory,TaskFactory.class,createEdgeAttributeTaskFactoryProps);

		GetEdgeTaskFactory getEdgeTaskFactory = new GetEdgeTaskFactory(cyApplicationManagerServiceRef);
		Properties getEdgeTaskFactoryProps = new Properties();
		getEdgeTaskFactoryProps.setProperty(COMMAND, "get");
		getEdgeTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "edge");
		registerService(bc,getEdgeTaskFactory,TaskFactory.class,getEdgeTaskFactoryProps);

		GetNetworkAttributeTaskFactory getEdgeAttributeTaskFactory = 
			new GetNetworkAttributeTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef, CyEdge.class);
		Properties getEdgeAttributeTaskFactoryProps = new Properties();
		getEdgeAttributeTaskFactoryProps.setProperty(COMMAND, "get attribute");
		getEdgeAttributeTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "edge");
		registerService(bc,getEdgeAttributeTaskFactory,TaskFactory.class,getEdgeAttributeTaskFactoryProps);

		GetPropertiesTaskFactory getEdgePropertiesTaskFactory = 
			new GetPropertiesTaskFactory(cyApplicationManagerServiceRef, CyEdge.class, 
			                             cyNetworkViewManagerServiceRef, renderingEngineManagerServiceRef);
		Properties getEdgePropertiesTaskFactoryProps = new Properties();
		getEdgePropertiesTaskFactoryProps.setProperty(COMMAND, "get properties");
		getEdgePropertiesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "edge");
		registerService(bc,getEdgePropertiesTaskFactory,TaskFactory.class,getEdgePropertiesTaskFactoryProps);

		ListEdgesTaskFactory listEdges = new ListEdgesTaskFactory(cyApplicationManagerServiceRef);
		Properties listEdgesTaskFactoryProps = new Properties();
		listEdgesTaskFactoryProps.setProperty(COMMAND, "list");
		listEdgesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "edge");
		registerService(bc,listEdges,TaskFactory.class,listEdgesTaskFactoryProps);

		ListNetworkAttributesTaskFactory listEdgeAttributesTaskFactory = 
			new ListNetworkAttributesTaskFactory(cyApplicationManagerServiceRef, 
		                                cyTableManagerServiceRef, CyEdge.class);
		Properties listEdgeAttributesTaskFactoryProps = new Properties();
		listEdgeAttributesTaskFactoryProps.setProperty(COMMAND, "list attributes");
		listEdgeAttributesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "edge");
		registerService(bc,listEdgeAttributesTaskFactory,TaskFactory.class,listEdgeAttributesTaskFactoryProps);

		ListPropertiesTaskFactory listEdgeProperties = 
			new ListPropertiesTaskFactory(cyApplicationManagerServiceRef,
		                                CyEdge.class, cyNetworkViewManagerServiceRef,
		                                renderingEngineManagerServiceRef);
		Properties listEdgePropertiesTaskFactoryProps = new Properties();
		listEdgePropertiesTaskFactoryProps.setProperty(COMMAND, "list properties");
		listEdgePropertiesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "edge");
		registerService(bc,listEdgeProperties,TaskFactory.class,listEdgePropertiesTaskFactoryProps);

		RenameEdgeTaskFactory renameEdge = new RenameEdgeTaskFactory();
		Properties renameEdgeTaskFactoryProps = new Properties();
		renameEdgeTaskFactoryProps.setProperty(COMMAND, "rename");
		renameEdgeTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "edge");
		registerService(bc,renameEdge,TaskFactory.class,renameEdgeTaskFactoryProps);

		SetNetworkAttributeTaskFactory setEdgeAttributeTaskFactory = 
			new SetNetworkAttributeTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef, CyEdge.class);
		Properties setEdgeAttributeTaskFactoryProps = new Properties();
		setEdgeAttributeTaskFactoryProps.setProperty(COMMAND, "set attribute");
		setEdgeAttributeTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "edge");
		registerService(bc,setEdgeAttributeTaskFactory,TaskFactory.class,setEdgeAttributeTaskFactoryProps);

		SetPropertiesTaskFactory setEdgePropertiesTaskFactory = 
			new SetPropertiesTaskFactory(cyApplicationManagerServiceRef, CyEdge.class, 
		                               cyNetworkViewManagerServiceRef, renderingEngineManagerServiceRef);
		Properties setEdgePropertiesTaskFactoryProps = new Properties();
		setEdgePropertiesTaskFactoryProps.setProperty(COMMAND, "set properties");
		setEdgePropertiesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "edge");
		registerService(bc,setEdgePropertiesTaskFactory,TaskFactory.class,setEdgePropertiesTaskFactoryProps);

		// NAMESPACE: group
		AddToGroupTaskFactory addToGroupTaskFactory = 
			new AddToGroupTaskFactory(cyApplicationManagerServiceRef, cyGroupManager);
		Properties addToGroupTFProps = new Properties();
		addToGroupTFProps.setProperty(COMMAND, "add");
		addToGroupTFProps.setProperty(COMMAND_NAMESPACE, "group");
		registerService(bc,addToGroupTaskFactory,TaskFactory.class,addToGroupTFProps);

		ListGroupsTaskFactory listGroupsTaskFactory = 
			new ListGroupsTaskFactory(cyApplicationManagerServiceRef, cyGroupManager);
		Properties listGroupsTFProps = new Properties();
		listGroupsTFProps.setProperty(COMMAND, "list");
		listGroupsTFProps.setProperty(COMMAND_NAMESPACE, "group");
		registerService(bc,listGroupsTaskFactory,TaskFactory.class,listGroupsTFProps);

		RemoveFromGroupTaskFactory removeFromGroupTaskFactory = 
			new RemoveFromGroupTaskFactory(cyApplicationManagerServiceRef, cyGroupManager);
		Properties removeFromGroupTFProps = new Properties();
		removeFromGroupTFProps.setProperty(COMMAND, "remove");
		removeFromGroupTFProps.setProperty(COMMAND_NAMESPACE, "group");
		registerService(bc,removeFromGroupTaskFactory,TaskFactory.class,removeFromGroupTFProps);

		RenameGroupTaskFactory renameGroupTaskFactory = 
			new RenameGroupTaskFactory(cyApplicationManagerServiceRef, cyGroupManager);
		Properties renameGroupTFProps = new Properties();
		renameGroupTFProps.setProperty(COMMAND, "rename");
		renameGroupTFProps.setProperty(COMMAND_NAMESPACE, "group");
		registerService(bc,renameGroupTaskFactory,TaskFactory.class,renameGroupTFProps);

		// NAMESPACE: layout
		GetPreferredLayoutTaskFactory getPreferredLayoutTaskFactory = 
			new GetPreferredLayoutTaskFactory(cyLayoutsServiceRef,cyPropertyServiceRef);
		Properties getPreferredTFProps = new Properties();
		getPreferredTFProps.setProperty(COMMAND, "get preferred");
		getPreferredTFProps.setProperty(COMMAND_NAMESPACE, "layout");
		registerService(bc,getPreferredLayoutTaskFactory,TaskFactory.class,getPreferredTFProps);

		SetPreferredLayoutTaskFactory setPreferredLayoutTaskFactory = 
			new SetPreferredLayoutTaskFactory(cyLayoutsServiceRef,cyPropertyServiceRef);
		Properties setPreferredTFProps = new Properties();
		setPreferredTFProps.setProperty(COMMAND, "set preferred");
		setPreferredTFProps.setProperty(COMMAND_NAMESPACE, "layout");
		registerService(bc,setPreferredLayoutTaskFactory,TaskFactory.class,setPreferredTFProps);


		// NAMESPACE: network
		AddTaskFactory addTaskFactory = new AddTaskFactory();
		Properties addTaskFactoryProps = new Properties();
		addTaskFactoryProps.setProperty(COMMAND, "add");
		addTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		registerService(bc,addTaskFactory,TaskFactory.class,addTaskFactoryProps);

		AddEdgeTaskFactory addEdgeTaskFactory = new AddEdgeTaskFactory();
		Properties addEdgeTaskFactoryProps = new Properties();
		addEdgeTaskFactoryProps.setProperty(COMMAND, "add edge");
		addEdgeTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		registerService(bc,addEdgeTaskFactory,TaskFactory.class,addEdgeTaskFactoryProps);

		AddNodeTaskFactory addNodeTaskFactory = new AddNodeTaskFactory();
		Properties addNodeTaskFactoryProps = new Properties();
		addNodeTaskFactoryProps.setProperty(COMMAND, "add node");
		addNodeTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		registerService(bc,addNodeTaskFactory,TaskFactory.class,addNodeTaskFactoryProps);

		CreateNetworkAttributeTaskFactory createNetworkAttributeTaskFactory = 
			new CreateNetworkAttributeTaskFactory(cyApplicationManagerServiceRef, 
		                                        cyTableManagerServiceRef, CyNetwork.class);
		Properties createNetworkAttributeTaskFactoryProps = new Properties();
		createNetworkAttributeTaskFactoryProps.setProperty(COMMAND, "create attribute");
		createNetworkAttributeTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		registerService(bc,createNetworkAttributeTaskFactory,TaskFactory.class,createNetworkAttributeTaskFactoryProps);

		DeselectTaskFactory deselectTaskFactory = new DeselectTaskFactory(cyNetworkViewManagerServiceRef, cyEventHelperRef);
		Properties deselectTaskFactoryProps = new Properties();
		deselectTaskFactoryProps.setProperty(COMMAND, "deselect");
		deselectTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		registerService(bc,deselectTaskFactory,TaskFactory.class,deselectTaskFactoryProps);

		GetNetworkTaskFactory getNetwork = new GetNetworkTaskFactory(cyApplicationManagerServiceRef);
		Properties getNetworkTaskFactoryProps = new Properties();
		getNetworkTaskFactoryProps.setProperty(COMMAND, "get");
		getNetworkTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		registerService(bc,getNetwork,TaskFactory.class,getNetworkTaskFactoryProps);

		GetNetworkAttributeTaskFactory getNetworkAttributeTaskFactory = 
			new GetNetworkAttributeTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef, CyNetwork.class);
		Properties getNetworkAttributeTaskFactoryProps = new Properties();
		getNetworkAttributeTaskFactoryProps.setProperty(COMMAND, "get attribute");
		getNetworkAttributeTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		registerService(bc,getNetworkAttributeTaskFactory,TaskFactory.class,getNetworkAttributeTaskFactoryProps);

		GetPropertiesTaskFactory getNetworkPropertiesTaskFactory = 
			new GetPropertiesTaskFactory(cyApplicationManagerServiceRef, CyNetwork.class,
		                               cyNetworkViewManagerServiceRef, renderingEngineManagerServiceRef);
		Properties getNetworkPropertiesTaskFactoryProps = new Properties();
		getNetworkPropertiesTaskFactoryProps.setProperty(COMMAND, "get properties");
		getNetworkPropertiesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		registerService(bc,getNetworkPropertiesTaskFactory,TaskFactory.class,getNetworkPropertiesTaskFactoryProps);

		HideTaskFactory hideTaskFactory = new HideTaskFactory(cyApplicationManagerServiceRef, cyNetworkViewManagerServiceRef, 
		                                                      visualMappingManagerServiceRef);
		Properties hideTaskFactoryProps = new Properties();
		hideTaskFactoryProps.setProperty(COMMAND, "hide");
		hideTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		registerService(bc,hideTaskFactory,TaskFactory.class,hideTaskFactoryProps);

		ListNetworksTaskFactory listNetworks = new ListNetworksTaskFactory(cyNetworkManagerServiceRef);
		Properties listNetworksTaskFactoryProps = new Properties();
		listNetworksTaskFactoryProps.setProperty(COMMAND, "list");
		listNetworksTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		registerService(bc,listNetworks,TaskFactory.class,listNetworksTaskFactoryProps);

		ListNetworkAttributesTaskFactory listNetworkAttributesTaskFactory = 
			new ListNetworkAttributesTaskFactory(cyApplicationManagerServiceRef, 
		                                cyTableManagerServiceRef, CyNetwork.class);
		Properties listNetworkAttributesTaskFactoryProps = new Properties();
		listNetworkAttributesTaskFactoryProps.setProperty(COMMAND, "list attributes");
		listNetworkAttributesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		registerService(bc,listNetworkAttributesTaskFactory,TaskFactory.class,listNetworkAttributesTaskFactoryProps);

		ListPropertiesTaskFactory listNetworkProperties = 
			new ListPropertiesTaskFactory(cyApplicationManagerServiceRef,
		                                CyNetwork.class, cyNetworkViewManagerServiceRef,
		                                renderingEngineManagerServiceRef);
		Properties listNetworkPropertiesTaskFactoryProps = new Properties();
		listNetworkPropertiesTaskFactoryProps.setProperty(COMMAND, "list properties");
		listNetworkPropertiesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		registerService(bc,listNetworkProperties,TaskFactory.class,listNetworkPropertiesTaskFactoryProps);

		SelectTaskFactory selectTaskFactory = new SelectTaskFactory(cyApplicationManagerServiceRef,
		                                                            cyNetworkViewManagerServiceRef, cyEventHelperRef); Properties selectTaskFactoryProps = new Properties();
		selectTaskFactoryProps.setProperty(COMMAND, "select");
		selectTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		registerService(bc,selectTaskFactory,TaskFactory.class,selectTaskFactoryProps);

		SetNetworkAttributeTaskFactory setNetworkAttributeTaskFactory = 
			new SetNetworkAttributeTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef, CyNetwork.class);
		Properties setNetworkAttributeTaskFactoryProps = new Properties();
		setNetworkAttributeTaskFactoryProps.setProperty(COMMAND, "set attribute");
		setNetworkAttributeTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		registerService(bc,setNetworkAttributeTaskFactory,TaskFactory.class,setNetworkAttributeTaskFactoryProps);

		SetCurrentNetworkTaskFactory setCurrentNetwork = new SetCurrentNetworkTaskFactory(cyApplicationManagerServiceRef);
		Properties setCurrentNetworkTaskFactoryProps = new Properties();
		setCurrentNetworkTaskFactoryProps.setProperty(COMMAND, "set current");
		setCurrentNetworkTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		registerService(bc,setCurrentNetwork,TaskFactory.class,setCurrentNetworkTaskFactoryProps);

		SetPropertiesTaskFactory setNetworkPropertiesTaskFactory = 
			new SetPropertiesTaskFactory(cyApplicationManagerServiceRef, CyNetwork.class,
		                               cyNetworkViewManagerServiceRef, renderingEngineManagerServiceRef);
		Properties setNetworkPropertiesTaskFactoryProps = new Properties();
		setNetworkPropertiesTaskFactoryProps.setProperty(COMMAND, "set properties");
		setNetworkPropertiesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		registerService(bc,setNetworkPropertiesTaskFactory,TaskFactory.class,setNetworkPropertiesTaskFactoryProps);

	
		UnHideTaskFactory unHideTaskFactory = new UnHideTaskFactory(cyApplicationManagerServiceRef, cyNetworkViewManagerServiceRef, 
		                                                            visualMappingManagerServiceRef);
		Properties unHideTaskFactoryProps = new Properties();
		unHideTaskFactoryProps.setProperty(COMMAND, "show");
		unHideTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		registerService(bc,unHideTaskFactory,TaskFactory.class,unHideTaskFactoryProps);


		// NAMESPACE: node
		CreateNetworkAttributeTaskFactory createNodeAttributeTaskFactory = 
			new CreateNetworkAttributeTaskFactory(cyApplicationManagerServiceRef, 
		                                        cyTableManagerServiceRef, CyNode.class);
		Properties createNodeAttributeTaskFactoryProps = new Properties();
		createNodeAttributeTaskFactoryProps.setProperty(COMMAND, "create attribute");
		createNodeAttributeTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "node");
		registerService(bc,createNodeAttributeTaskFactory,TaskFactory.class,createNodeAttributeTaskFactoryProps);

		GetNodeTaskFactory getNodeTaskFactory = new GetNodeTaskFactory(cyApplicationManagerServiceRef);
		Properties getNodeTaskFactoryProps = new Properties();
		getNodeTaskFactoryProps.setProperty(COMMAND, "get");
		getNodeTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "node");
		registerService(bc,getNodeTaskFactory,TaskFactory.class,getNodeTaskFactoryProps);

		GetNetworkAttributeTaskFactory getNodeAttributeTaskFactory = 
			new GetNetworkAttributeTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef, CyNode.class);
		Properties getNodeAttributeTaskFactoryProps = new Properties();
		getNodeAttributeTaskFactoryProps.setProperty(COMMAND, "get attribute");
		getNodeAttributeTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "node");
		registerService(bc,getNodeAttributeTaskFactory,TaskFactory.class,getNodeAttributeTaskFactoryProps);

		GetPropertiesTaskFactory getNodePropertiesTaskFactory = 
			new GetPropertiesTaskFactory(cyApplicationManagerServiceRef, CyNode.class,
		                               cyNetworkViewManagerServiceRef, renderingEngineManagerServiceRef);
		Properties getNodePropertiesTaskFactoryProps = new Properties();
		getNodePropertiesTaskFactoryProps.setProperty(COMMAND, "get properties");
		getNodePropertiesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "node");
		registerService(bc,getNodePropertiesTaskFactory,TaskFactory.class,getNodePropertiesTaskFactoryProps);


		ListNodesTaskFactory listNodes = new ListNodesTaskFactory(cyApplicationManagerServiceRef);
		Properties listNodesTaskFactoryProps = new Properties();
		listNodesTaskFactoryProps.setProperty(COMMAND, "list");
		listNodesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "node");
		registerService(bc,listNodes,TaskFactory.class,listNodesTaskFactoryProps);

		ListNetworkAttributesTaskFactory listNodeAttributesTaskFactory = 
			new ListNetworkAttributesTaskFactory(cyApplicationManagerServiceRef, 
		                                cyTableManagerServiceRef, CyNode.class);
		Properties listNodeAttributesTaskFactoryProps = new Properties();
		listNodeAttributesTaskFactoryProps.setProperty(COMMAND, "list attributes");
		listNodeAttributesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "node");
		registerService(bc,listNodeAttributesTaskFactory,TaskFactory.class,listNodeAttributesTaskFactoryProps);

		ListPropertiesTaskFactory listNodeProperties = 
			new ListPropertiesTaskFactory(cyApplicationManagerServiceRef,
		                                CyNode.class, cyNetworkViewManagerServiceRef,
		                                renderingEngineManagerServiceRef);
		Properties listNodePropertiesTaskFactoryProps = new Properties();
		listNodePropertiesTaskFactoryProps.setProperty(COMMAND, "list properties");
		listNodePropertiesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "node");
		registerService(bc,listNodeProperties,TaskFactory.class,listNodePropertiesTaskFactoryProps);

		RenameNodeTaskFactory renameNode = new RenameNodeTaskFactory();
		Properties renameNodeTaskFactoryProps = new Properties();
		renameNodeTaskFactoryProps.setProperty(COMMAND, "rename");
		renameNodeTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "node");
		registerService(bc,renameNode,TaskFactory.class,renameNodeTaskFactoryProps);

		SetNetworkAttributeTaskFactory setNodeAttributeTaskFactory = 
			new SetNetworkAttributeTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef, CyNode.class);
		Properties setNodeAttributeTaskFactoryProps = new Properties();
		setNodeAttributeTaskFactoryProps.setProperty(COMMAND, "set attribute");
		setNodeAttributeTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "node");
		registerService(bc,setNodeAttributeTaskFactory,TaskFactory.class,setNodeAttributeTaskFactoryProps);

		SetPropertiesTaskFactory setNodePropertiesTaskFactory = 
			new SetPropertiesTaskFactory(cyApplicationManagerServiceRef, CyNode.class,
		                               cyNetworkViewManagerServiceRef, renderingEngineManagerServiceRef);
		Properties setNodePropertiesTaskFactoryProps = new Properties();
		setNodePropertiesTaskFactoryProps.setProperty(COMMAND, "set properties");
		setNodePropertiesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "node");
		registerService(bc,setNodePropertiesTaskFactory,TaskFactory.class,setNodePropertiesTaskFactoryProps);

		// NAMESPACE: table
		AddRowTaskFactory addRowTaskFactory = 
			new AddRowTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef); 
		Properties addRowTaskFactoryProps = new Properties();
		addRowTaskFactoryProps.setProperty(COMMAND, "add row");
		addRowTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "table");
		registerService(bc,addRowTaskFactory,TaskFactory.class,addRowTaskFactoryProps);

		CreateColumnTaskFactory createColumnTaskFactory = 
			new CreateColumnTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef); 
		Properties createColumnTaskFactoryProps = new Properties();
		createColumnTaskFactoryProps.setProperty(COMMAND, "create column");
		createColumnTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "table");
		registerService(bc,createColumnTaskFactory,TaskFactory.class,createColumnTaskFactoryProps);

		CreateTableTaskFactory createTableTaskFactory = 
			new CreateTableTaskFactory(cyApplicationManagerServiceRef, 
			                           cyTableFactoryServiceRef, cyTableManagerServiceRef); 
		Properties createTableTaskFactoryProps = new Properties();
		createTableTaskFactoryProps.setProperty(COMMAND, "create table");
		createTableTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "table");
		registerService(bc,createTableTaskFactory,TaskFactory.class,createTableTaskFactoryProps);

		DeleteColumnCommandTaskFactory deleteColumnCommandTaskFactory = 
			new DeleteColumnCommandTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef); 
		Properties deleteColumnCommandTaskFactoryProps = new Properties();
		deleteColumnCommandTaskFactoryProps.setProperty(COMMAND, "delete column");
		deleteColumnCommandTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "table");
		registerService(bc,deleteColumnCommandTaskFactory,TaskFactory.class,deleteColumnCommandTaskFactoryProps);

		DeleteRowTaskFactory deleteRowTaskFactory = 
			new DeleteRowTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef); 
		Properties deleteRowTaskFactoryProps = new Properties();
		deleteRowTaskFactoryProps.setProperty(COMMAND, "delete row");
		deleteRowTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "table");
		registerService(bc,deleteRowTaskFactory,TaskFactory.class,deleteRowTaskFactoryProps);

		DestroyTableTaskFactory destroyTableTaskFactory = 
			new DestroyTableTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef); 
		Properties destroyTableTaskFactoryProps = new Properties();
		destroyTableTaskFactoryProps.setProperty(COMMAND, "destroy");
		destroyTableTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "table");
		registerService(bc,destroyTableTaskFactory,TaskFactory.class,destroyTableTaskFactoryProps);

		GetColumnTaskFactory getColumnTaskFactory = 
			new GetColumnTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef); 
		Properties getColumnTaskFactoryProps = new Properties();
		getColumnTaskFactoryProps.setProperty(COMMAND, "get column");
		getColumnTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "table");
		registerService(bc,getColumnTaskFactory,TaskFactory.class,getColumnTaskFactoryProps);

		GetRowTaskFactory getRowTaskFactory = 
			new GetRowTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef); 
		Properties getRowTaskFactoryProps = new Properties();
		getRowTaskFactoryProps.setProperty(COMMAND, "get row");
		getRowTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "table");
		registerService(bc,getRowTaskFactory,TaskFactory.class,getRowTaskFactoryProps);

		GetValueTaskFactory getValueTaskFactory = 
			new GetValueTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef); 
		Properties getValueTaskFactoryProps = new Properties();
		getValueTaskFactoryProps.setProperty(COMMAND, "get value");
		getValueTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "table");
		registerService(bc,getValueTaskFactory,TaskFactory.class,getValueTaskFactoryProps);

		ListColumnsTaskFactory listColumnsTaskFactory = 
			new ListColumnsTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef, 
			                          cyNetworkTableManagerServiceRef);
		Properties listColumnsTaskFactoryProps = new Properties();
		listColumnsTaskFactoryProps.setProperty(COMMAND, "list columns");
		listColumnsTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "table");
		registerService(bc,listColumnsTaskFactory,TaskFactory.class,listColumnsTaskFactoryProps);

		ListRowsTaskFactory listRowsTaskFactory = 
			new ListRowsTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef);
		Properties listRowsTaskFactoryProps = new Properties();
		listRowsTaskFactoryProps.setProperty(COMMAND, "list rows");
		listRowsTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "table");
		registerService(bc,listRowsTaskFactory,TaskFactory.class,listRowsTaskFactoryProps);

		ListTablesTaskFactory listTablesTaskFactory = 
			new ListTablesTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef, 
			                          cyNetworkTableManagerServiceRef);
		Properties listTablesTaskFactoryProps = new Properties();
		listTablesTaskFactoryProps.setProperty(COMMAND, "list");
		listTablesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "table");
		registerService(bc,listTablesTaskFactory,TaskFactory.class,listTablesTaskFactoryProps);

		SetTableTitleTaskFactory setTableTitleTaskFactory = 
			new SetTableTitleTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef);
		Properties setTableTitleTaskFactoryProps = new Properties();
		setTableTitleTaskFactoryProps.setProperty(COMMAND, "set title");
		setTableTitleTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "table");
		registerService(bc,setTableTitleTaskFactory,TaskFactory.class,setTableTitleTaskFactoryProps);

		SetValuesTaskFactory setValuesTaskFactory = 
			new SetValuesTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef);
		Properties setValuesTaskFactoryProps = new Properties();
		setValuesTaskFactoryProps.setProperty(COMMAND, "set values");
		setValuesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "table");
		registerService(bc,setValuesTaskFactory,TaskFactory.class,setValuesTaskFactoryProps);

		// NAMESPACE: view
		GetCurrentNetworkViewTaskFactory getCurrentView = 
			new GetCurrentNetworkViewTaskFactory(cyApplicationManagerServiceRef);
		Properties getCurrentViewTaskFactoryProps = new Properties();
		getCurrentViewTaskFactoryProps.setProperty(COMMAND, "get current");
		getCurrentViewTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "view");
		registerService(bc,getCurrentView,TaskFactory.class,getCurrentViewTaskFactoryProps);

		ListNetworkViewsTaskFactory listNetworkViews = 
			new ListNetworkViewsTaskFactory(cyApplicationManagerServiceRef, cyNetworkViewManagerServiceRef);
		Properties listNetworkViewsTaskFactoryProps = new Properties();
		listNetworkViewsTaskFactoryProps.setProperty(COMMAND, "list");
		listNetworkViewsTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "view");
		registerService(bc,listNetworkViews,TaskFactory.class,listNetworkViewsTaskFactoryProps);

		SetCurrentNetworkViewTaskFactory setCurrentView = 
			new SetCurrentNetworkViewTaskFactory(cyApplicationManagerServiceRef, 
			                                     cyNetworkViewManagerServiceRef);
		Properties setCurrentViewTaskFactoryProps = new Properties();
		setCurrentViewTaskFactoryProps.setProperty(COMMAND, "set current");
		setCurrentViewTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "view");
		registerService(bc,setCurrentView,TaskFactory.class,setCurrentViewTaskFactoryProps);

		UpdateNetworkViewTaskFactory updateView = 
			new UpdateNetworkViewTaskFactory(cyApplicationManagerServiceRef, cyNetworkViewManagerServiceRef);
		Properties updateViewTaskFactoryProps = new Properties();
		updateViewTaskFactoryProps.setProperty(COMMAND, "update");
		updateViewTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "view");
		registerService(bc,updateView,TaskFactory.class,updateViewTaskFactoryProps);

	}
}
