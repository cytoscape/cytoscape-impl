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

import static org.cytoscape.application.swing.ActionEnableSupport.ENABLE_FOR_NETWORK;
import static org.cytoscape.application.swing.ActionEnableSupport.ENABLE_FOR_NETWORK_AND_VIEW;
import static org.cytoscape.application.swing.ActionEnableSupport.ENABLE_FOR_SELECTED_EDGES;
import static org.cytoscape.application.swing.ActionEnableSupport.ENABLE_FOR_SELECTED_NODES;
import static org.cytoscape.application.swing.ActionEnableSupport.ENABLE_FOR_SELECTED_NODES_OR_EDGES;
import static org.cytoscape.work.ServiceProperties.ACCELERATOR;
import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.ID;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_AFTER;
import static org.cytoscape.work.ServiceProperties.IN_CONTEXT_MENU;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.IN_NETWORK_PANEL_CONTEXT_MENU;
import static org.cytoscape.work.ServiceProperties.IN_TOOL_BAR;
import static org.cytoscape.work.ServiceProperties.LARGE_ICON_URL;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.NETWORK_GROUP_MENU;
import static org.cytoscape.work.ServiceProperties.NETWORK_SELECT_MENU;
import static org.cytoscape.work.ServiceProperties.NODE_ADD_MENU;
import static org.cytoscape.work.ServiceProperties.NODE_GROUP_MENU;
import static org.cytoscape.work.ServiceProperties.NODE_SELECT_MENU;
import static org.cytoscape.work.ServiceProperties.PREFERRED_ACTION;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;
import static org.cytoscape.work.ServiceProperties.TOOLTIP;
import static org.cytoscape.work.ServiceProperties.TOOL_BAR_GRAVITY;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.io.write.CyNetworkViewWriterManager;
import org.cytoscape.io.write.CySessionWriterFactory;
import org.cytoscape.io.write.CySessionWriterManager;
import org.cytoscape.io.write.CyTableWriterManager;
import org.cytoscape.io.write.PresentationWriterManager;
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
import org.cytoscape.service.util.CyServiceRegistrar;
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
import org.cytoscape.task.edit.ImportDataTableTaskFactory;
import org.cytoscape.task.edit.MapGlobalToLocalTableTaskFactory;
import org.cytoscape.task.edit.MapTableToNetworkTablesTaskFactory;
import org.cytoscape.task.edit.MergeTablesTaskFactory;
import org.cytoscape.task.edit.RenameColumnTaskFactory;
import org.cytoscape.task.edit.UnGroupNodesTaskFactory;
import org.cytoscape.task.edit.UnGroupTaskFactory;
import org.cytoscape.task.hide.HideSelectedEdgesTaskFactory;
import org.cytoscape.task.hide.HideSelectedNodesTaskFactory;
import org.cytoscape.task.hide.HideSelectedTaskFactory;
import org.cytoscape.task.hide.HideUnselectedEdgesTaskFactory;
import org.cytoscape.task.hide.HideUnselectedNodesTaskFactory;
import org.cytoscape.task.hide.HideUnselectedTaskFactory;
import org.cytoscape.task.hide.UnHideAllEdgesTaskFactory;
import org.cytoscape.task.hide.UnHideAllNodesTaskFactory;
import org.cytoscape.task.hide.UnHideAllTaskFactory;
import org.cytoscape.task.internal.creation.CloneNetworkTaskFactoryImpl;
import org.cytoscape.task.internal.creation.CreateNetworkViewTaskFactoryImpl;
import org.cytoscape.task.internal.creation.NewEmptyNetworkTaskFactoryImpl;
import org.cytoscape.task.internal.creation.NewNetworkCommandTaskFactory;
import org.cytoscape.task.internal.creation.NewNetworkSelectedNodesEdgesTaskFactoryImpl;
import org.cytoscape.task.internal.creation.NewNetworkSelectedNodesOnlyTaskFactoryImpl;
import org.cytoscape.task.internal.destruction.DestroyNetworkTaskFactoryImpl;
import org.cytoscape.task.internal.destruction.DestroyNetworkViewTaskFactoryImpl;
import org.cytoscape.task.internal.edit.ConnectSelectedNodesTaskFactoryImpl;
import org.cytoscape.task.internal.export.graphics.ExportNetworkImageTaskFactoryImpl;
import org.cytoscape.task.internal.export.network.ExportNetworkTaskFactoryImpl;
import org.cytoscape.task.internal.export.network.ExportNetworkViewTaskFactoryImpl;
import org.cytoscape.task.internal.export.table.ExportNoGuiSelectedTableTaskFactoryImpl;
import org.cytoscape.task.internal.export.table.ExportSelectedTableTaskFactoryImpl;
import org.cytoscape.task.internal.export.table.ExportTableTaskFactoryImpl;
import org.cytoscape.task.internal.export.vizmap.ExportVizmapTaskFactoryImpl;
import org.cytoscape.task.internal.export.web.ExportAsWebArchiveTaskFactory;
import org.cytoscape.task.internal.group.AddToGroupTaskFactory;
import org.cytoscape.task.internal.group.GroupNodeContextTaskFactoryImpl;
import org.cytoscape.task.internal.group.GroupNodesTaskFactoryImpl;
import org.cytoscape.task.internal.group.ListGroupsTaskFactory;
import org.cytoscape.task.internal.group.RemoveFromGroupTaskFactory;
import org.cytoscape.task.internal.group.RenameGroupTaskFactory;
import org.cytoscape.task.internal.group.UnGroupNodesTaskFactoryImpl;
import org.cytoscape.task.internal.hide.HideCommandTaskFactory;
import org.cytoscape.task.internal.hide.HideSelectedEdgesTaskFactoryImpl;
import org.cytoscape.task.internal.hide.HideSelectedNodesTaskFactoryImpl;
import org.cytoscape.task.internal.hide.HideSelectedTaskFactoryImpl;
import org.cytoscape.task.internal.hide.HideUnselectedEdgesTaskFactoryImpl;
import org.cytoscape.task.internal.hide.HideUnselectedNodesTaskFactoryImpl;
import org.cytoscape.task.internal.hide.HideUnselectedTaskFactoryImpl;
import org.cytoscape.task.internal.hide.UnHideAllEdgesTaskFactoryImpl;
import org.cytoscape.task.internal.hide.UnHideAllNodesTaskFactoryImpl;
import org.cytoscape.task.internal.hide.UnHideAllTaskFactoryImpl;
import org.cytoscape.task.internal.hide.UnHideCommandTaskFactory;
import org.cytoscape.task.internal.layout.ApplyPreferredLayoutTaskFactoryImpl;
import org.cytoscape.task.internal.layout.GetPreferredLayoutTaskFactory;
import org.cytoscape.task.internal.layout.SetPreferredLayoutTaskFactory;
import org.cytoscape.task.internal.loaddatatable.LoadTableFileTaskFactoryImpl;
import org.cytoscape.task.internal.loaddatatable.LoadTableURLTaskFactoryImpl;
import org.cytoscape.task.internal.loadnetwork.LoadNetworkFileTaskFactoryImpl;
import org.cytoscape.task.internal.loadnetwork.LoadNetworkURLTaskFactoryImpl;
import org.cytoscape.task.internal.loadvizmap.LoadVizmapFileTaskFactoryImpl;
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
import org.cytoscape.task.internal.table.ImportTableDataTaskFactoryImpl;
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
import org.cytoscape.task.internal.view.GetCurrentNetworkViewTaskFactory;
import org.cytoscape.task.internal.view.ListNetworkViewsTaskFactory;
import org.cytoscape.task.internal.view.SetCurrentNetworkViewTaskFactory;
import org.cytoscape.task.internal.view.UpdateNetworkViewTaskFactory;
import org.cytoscape.task.internal.vizmap.ApplyVisualStyleTaskFactoryimpl;
import org.cytoscape.task.internal.vizmap.ClearAllEdgeBendsFactory;
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
import org.cytoscape.task.write.ExportNetworkTaskFactory;
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
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;


public class CyActivator extends AbstractCyActivator {
	
	@Override
	public void start(BundleContext bc) {
		CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);
		CyEventHelper cyEventHelperRef = getService(bc,CyEventHelper.class);
		RecentlyOpenedTracker recentlyOpenedTrackerServiceRef = getService(bc,RecentlyOpenedTracker.class);
		CyNetworkNaming cyNetworkNamingServiceRef = getService(bc,CyNetworkNaming.class);
		UndoSupport undoSupportServiceRef = getService(bc,UndoSupport.class);
		CyNetworkViewFactory cyNetworkViewFactoryServiceRef = getService(bc,CyNetworkViewFactory.class);
		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc,CyNetworkFactory.class);
		CyRootNetworkManager cyRootNetworkFactoryServiceRef = getService(bc,CyRootNetworkManager.class);
		CyNetworkReaderManager cyNetworkReaderManagerServiceRef = getService(bc,CyNetworkReaderManager.class);
		VizmapReaderManager vizmapReaderManagerServiceRef = getService(bc,VizmapReaderManager.class);
		VisualMappingManager visualMappingManagerServiceRef = getService(bc,VisualMappingManager.class);
		StreamUtil streamUtilRef = getService(bc,StreamUtil.class);
		PresentationWriterManager viewWriterManagerServiceRef = getService(bc,PresentationWriterManager.class);
		CyNetworkViewWriterManager networkViewWriterManagerServiceRef = getService(bc,CyNetworkViewWriterManager.class);
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

		LoadNetworkFileTaskFactoryImpl loadNetworkFileTaskFactory = new LoadNetworkFileTaskFactoryImpl(cyNetworkReaderManagerServiceRef,cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef,cyPropertyServiceRef,cyNetworkNamingServiceRef, visualMappingManagerServiceRef, nullNetworkViewFactory);
		LoadNetworkURLTaskFactoryImpl loadNetworkURLTaskFactory = new LoadNetworkURLTaskFactoryImpl(cyNetworkReaderManagerServiceRef,cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef,cyPropertyServiceRef,cyNetworkNamingServiceRef,streamUtilRef, visualMappingManagerServiceRef, nullNetworkViewFactory);

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
		
		SelectFirstNeighborsNodeViewTaskFactoryImpl selectFirstNeighborsNodeViewTaskFactory = new SelectFirstNeighborsNodeViewTaskFactoryImpl(CyEdge.Type.ANY,cyEventHelperRef);
		
		NewEmptyNetworkTaskFactoryImpl newEmptyNetworkTaskFactory = new NewEmptyNetworkTaskFactoryImpl(cyNetworkFactoryServiceRef,cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef,cyNetworkNamingServiceRef,synchronousTaskManagerServiceRef,visualMappingManagerServiceRef, cyRootNetworkFactoryServiceRef, cyApplicationManagerServiceRef);
		CloneNetworkTaskFactoryImpl cloneNetworkTaskFactory = new CloneNetworkTaskFactoryImpl(cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef,visualMappingManagerServiceRef,cyNetworkFactoryServiceRef,cyNetworkViewFactoryServiceRef,cyNetworkNamingServiceRef,cyApplicationManagerServiceRef,cyNetworkTableManagerServiceRef,rootNetworkManagerServiceRef,cyGroupManager,cyGroupFactory,renderingEngineManagerServiceRef, nullNetworkViewFactory);
		NewNetworkSelectedNodesEdgesTaskFactoryImpl newNetworkSelectedNodesEdgesTaskFactory = new NewNetworkSelectedNodesEdgesTaskFactoryImpl(undoSupportServiceRef,cyRootNetworkFactoryServiceRef,cyNetworkViewFactoryServiceRef,cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef,cyNetworkNamingServiceRef,visualMappingManagerServiceRef,cyApplicationManagerServiceRef,cyEventHelperRef,cyGroupManager,renderingEngineManagerServiceRef);
		NewNetworkCommandTaskFactory newNetworkCommandTaskFactory = new NewNetworkCommandTaskFactory(undoSupportServiceRef,cyRootNetworkFactoryServiceRef,cyNetworkViewFactoryServiceRef,cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef,cyNetworkNamingServiceRef,visualMappingManagerServiceRef,cyApplicationManagerServiceRef,cyEventHelperRef,cyGroupManager,renderingEngineManagerServiceRef);
		NewNetworkSelectedNodesOnlyTaskFactoryImpl newNetworkSelectedNodesOnlyTaskFactory = new NewNetworkSelectedNodesOnlyTaskFactoryImpl(undoSupportServiceRef,cyRootNetworkFactoryServiceRef,cyNetworkViewFactoryServiceRef,cyNetworkManagerServiceRef,cyNetworkViewManagerServiceRef,cyNetworkNamingServiceRef,visualMappingManagerServiceRef,cyApplicationManagerServiceRef,cyEventHelperRef,cyGroupManager,renderingEngineManagerServiceRef);
		DestroyNetworkTaskFactoryImpl destroyNetworkTaskFactory = new DestroyNetworkTaskFactoryImpl(cyNetworkManagerServiceRef);
		DestroyNetworkViewTaskFactoryImpl destroyNetworkViewTaskFactory = new DestroyNetworkViewTaskFactoryImpl(cyNetworkViewManagerServiceRef);
		ZoomInTaskFactory zoomInTaskFactory = new ZoomInTaskFactory(undoSupportServiceRef, cyApplicationManagerServiceRef);
		ZoomOutTaskFactory zoomOutTaskFactory = new ZoomOutTaskFactory(undoSupportServiceRef, cyApplicationManagerServiceRef);
		FitSelectedTaskFactory fitSelectedTaskFactory = new FitSelectedTaskFactory(undoSupportServiceRef, cyApplicationManagerServiceRef);
		FitContentTaskFactory fitContentTaskFactory = new FitContentTaskFactory(undoSupportServiceRef, cyApplicationManagerServiceRef);
		NewSessionTaskFactoryImpl newSessionTaskFactory = new NewSessionTaskFactoryImpl(cySessionManagerServiceRef, tunableSetterServiceRef, cyEventHelperRef);
		OpenSessionCommandTaskFactory openSessionCommandTaskFactory = new OpenSessionCommandTaskFactory(cySessionManagerServiceRef,sessionReaderManagerServiceRef,cyApplicationManagerServiceRef,cyNetworkManagerServiceRef,cyTableManagerServiceRef,cyNetworkTableManagerServiceRef,cyGroupManager,recentlyOpenedTrackerServiceRef,cyEventHelperRef);
		OpenSessionTaskFactoryImpl openSessionTaskFactory = new OpenSessionTaskFactoryImpl(cySessionManagerServiceRef,sessionReaderManagerServiceRef,cyApplicationManagerServiceRef,cyNetworkManagerServiceRef,cyTableManagerServiceRef,cyNetworkTableManagerServiceRef,cyGroupManager,recentlyOpenedTrackerServiceRef,tunableSetterServiceRef,cyEventHelperRef);
		SaveSessionTaskFactoryImpl saveSessionTaskFactory = new SaveSessionTaskFactoryImpl( sessionWriterManagerServiceRef, cySessionManagerServiceRef, recentlyOpenedTrackerServiceRef, cyEventHelperRef);
		SaveSessionAsTaskFactoryImpl saveSessionAsTaskFactory = new SaveSessionAsTaskFactoryImpl( sessionWriterManagerServiceRef, cySessionManagerServiceRef, recentlyOpenedTrackerServiceRef, cyEventHelperRef, tunableSetterServiceRef);
		ProxySettingsTaskFactoryImpl proxySettingsTaskFactory = new ProxySettingsTaskFactoryImpl(cyPropertyServiceRef, streamUtilRef);
		EditNetworkTitleTaskFactoryImpl editNetworkTitleTaskFactory = new EditNetworkTitleTaskFactoryImpl(undoSupportServiceRef, cyNetworkManagerServiceRef, cyNetworkNamingServiceRef, tunableSetterServiceRef);
		CreateNetworkViewTaskFactoryImpl createNetworkViewTaskFactory = new CreateNetworkViewTaskFactoryImpl(undoSupportServiceRef,cyNetworkViewManagerServiceRef,cyLayoutsServiceRef,cyEventHelperRef,visualMappingManagerServiceRef,renderingEngineManagerServiceRef,cyApplicationManagerServiceRef);
		ExportNetworkImageTaskFactoryImpl exportNetworkImageTaskFactory = new ExportNetworkImageTaskFactoryImpl(viewWriterManagerServiceRef,cyApplicationManagerServiceRef);
		ExportNetworkTaskFactoryImpl exportNetworkTaskFactory = new ExportNetworkTaskFactoryImpl(networkViewWriterManagerServiceRef, tunableSetterServiceRef);
		ExportNetworkViewTaskFactoryImpl exportNetworkViewTaskFactory = new ExportNetworkViewTaskFactoryImpl(networkViewWriterManagerServiceRef, tunableSetterServiceRef);
		ExportSelectedTableTaskFactoryImpl exportCurrentTableTaskFactory = new ExportSelectedTableTaskFactoryImpl(cyTableWriterManagerRef, cyTableManagerServiceRef, cyNetworkManagerServiceRef);
		ExportNoGuiSelectedTableTaskFactoryImpl exportNoGuiCurrentTableTaskFactory = new ExportNoGuiSelectedTableTaskFactoryImpl(cyTableWriterManagerRef, cyTableManagerServiceRef, cyNetworkManagerServiceRef);
		ApplyPreferredLayoutTaskFactoryImpl applyPreferredLayoutTaskFactory = new ApplyPreferredLayoutTaskFactoryImpl(cyApplicationManagerServiceRef, cyNetworkViewManagerServiceRef, cyLayoutsServiceRef);
		DeleteColumnTaskFactoryImpl deleteColumnTaskFactory = new DeleteColumnTaskFactoryImpl(undoSupportServiceRef);
		RenameColumnTaskFactoryImpl renameColumnTaskFactory = new RenameColumnTaskFactoryImpl(undoSupportServiceRef, tunableSetterServiceRef);
		
		CopyValueToColumnTaskFactoryImpl copyValueToEntireColumnTaskFactory = new CopyValueToColumnTaskFactoryImpl(undoSupportServiceRef, false, "Apply to entire column");
		CopyValueToColumnTaskFactoryImpl copyValueToSelectedNodesTaskFactory = new CopyValueToColumnTaskFactoryImpl(undoSupportServiceRef, true, "Apply to selected nodes");
		CopyValueToColumnTaskFactoryImpl copyValueToSelectedEdgesTaskFactory = new CopyValueToColumnTaskFactoryImpl(undoSupportServiceRef, true, "Apply to selected edges");

		DeleteTableTaskFactoryImpl deleteTableTaskFactory = new DeleteTableTaskFactoryImpl(cyTableManagerServiceRef);
		ExportVizmapTaskFactoryImpl exportVizmapTaskFactory = new ExportVizmapTaskFactoryImpl(serviceRegistrar);
		ConnectSelectedNodesTaskFactoryImpl connectSelectedNodesTaskFactory = new ConnectSelectedNodesTaskFactoryImpl(undoSupportServiceRef, cyEventHelperRef, visualMappingManagerServiceRef, cyNetworkViewManagerServiceRef);
		MapGlobalToLocalTableTaskFactoryImpl mapGlobal = new MapGlobalToLocalTableTaskFactoryImpl(cyTableManagerServiceRef, cyNetworkManagerServiceRef, tunableSetterServiceRef);
		
		DynamicTaskFactoryProvisionerImpl dynamicTaskFactoryProvisionerImpl = new DynamicTaskFactoryProvisionerImpl(cyApplicationManagerServiceRef);
		registerAllServices(bc, dynamicTaskFactoryProvisionerImpl, new Properties());

		LoadTableFileTaskFactoryImpl loadTableFileTaskFactory = new LoadTableFileTaskFactoryImpl(serviceRegistrar);
		LoadTableURLTaskFactoryImpl loadTableURLTaskFactory = new LoadTableURLTaskFactoryImpl(serviceRegistrar);
		MergeTablesTaskFactoryImpl mergeTablesTaskFactory = new MergeTablesTaskFactoryImpl(cyTableManagerServiceRef,cyNetworkManagerServiceRef,tunableSetterServiceRef, rootNetworkManagerServiceRef);
		// Apply Style Task
		ApplyVisualStyleTaskFactoryimpl applyVisualStyleTaskFactory = new ApplyVisualStyleTaskFactoryimpl(visualMappingManagerServiceRef);
		Properties applyVisualStyleProps = new Properties();
		applyVisualStyleProps.setProperty(ID,"applyVisualStyleTaskFactory");
		applyVisualStyleProps.setProperty(TITLE, "Apply Style...");
		applyVisualStyleProps.setProperty(COMMAND,"apply");
		applyVisualStyleProps.setProperty(COMMAND_NAMESPACE,"vizmap");
		applyVisualStyleProps.setProperty(COMMAND_DESCRIPTION,"Apply the style");
		applyVisualStyleProps.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU,"true");
		applyVisualStyleProps.setProperty(ENABLE_FOR,ENABLE_FOR_NETWORK_AND_VIEW);
		
		registerService(bc, applyVisualStyleTaskFactory, NetworkViewCollectionTaskFactory.class, applyVisualStyleProps);
		registerService(bc, applyVisualStyleTaskFactory, ApplyVisualStyleTaskFactory.class, applyVisualStyleProps);
		
		// Clear edge bends
		ClearAllEdgeBendsFactory clearAllEdgeBendsFactory = new ClearAllEdgeBendsFactory(visualMappingManagerServiceRef);
		Properties clearAllEdgeBendsProps = new Properties();
		clearAllEdgeBendsProps.setProperty(ID, "clearAllEdgeBendsFactory");
		clearAllEdgeBendsProps.setProperty(TITLE, "Clear All Edge Bends");
		clearAllEdgeBendsProps.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU, "true");
		clearAllEdgeBendsProps.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
		clearAllEdgeBendsProps.setProperty(PREFERRED_MENU, "Layout");
		clearAllEdgeBendsProps.setProperty(MENU_GRAVITY, "0.1");

		registerService(bc, clearAllEdgeBendsFactory, NetworkViewCollectionTaskFactory.class, clearAllEdgeBendsProps);
		
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
		loadNetworkFileTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Import.Network[1.0]");
		loadNetworkFileTaskFactoryProps.setProperty(ACCELERATOR,"cmd l");
		loadNetworkFileTaskFactoryProps.setProperty(TITLE,"File...");
		loadNetworkFileTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		loadNetworkFileTaskFactoryProps.setProperty(COMMAND,"load file");
		loadNetworkFileTaskFactoryProps.setProperty(COMMAND_DESCRIPTION,"Load a network file (e.g. XGMML)");
		loadNetworkFileTaskFactoryProps.setProperty(MENU_GRAVITY,"1.0");
		loadNetworkFileTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"2.0");
		loadNetworkFileTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/import-net-32.png").toString());
		loadNetworkFileTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		loadNetworkFileTaskFactoryProps.setProperty(TOOLTIP,"Import Network From File");
		registerService(bc, loadNetworkFileTaskFactory, TaskFactory.class, loadNetworkFileTaskFactoryProps);
		registerService(bc, loadNetworkFileTaskFactory, LoadNetworkFileTaskFactory.class, loadNetworkFileTaskFactoryProps);

		Properties loadNetworkURLTaskFactoryProps = new Properties();
		loadNetworkURLTaskFactoryProps.setProperty(ID,"loadNetworkURLTaskFactory");
		loadNetworkURLTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Import.Network[1.0]");
		loadNetworkURLTaskFactoryProps.setProperty(ACCELERATOR,"cmd shift l");
		loadNetworkURLTaskFactoryProps.setProperty(MENU_GRAVITY,"2.0");
//		loadNetworkURLTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"2.1");
		loadNetworkURLTaskFactoryProps.setProperty(TITLE,"URL...");
//		loadNetworkURLTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/import-net-url-32.png").toString());
//		loadNetworkURLTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		loadNetworkURLTaskFactoryProps.setProperty(TOOLTIP,"Import Network From URL");
		loadNetworkURLTaskFactoryProps.setProperty(COMMAND,"load url");
		loadNetworkURLTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		loadNetworkURLTaskFactoryProps.setProperty(COMMAND_DESCRIPTION,"Load a network file (e.g. XGMML) from a url");
		registerService(bc, loadNetworkURLTaskFactory, TaskFactory.class, loadNetworkURLTaskFactoryProps);
		registerService(bc, loadNetworkURLTaskFactory, LoadNetworkURLTaskFactory.class, loadNetworkURLTaskFactoryProps);

		Properties loadVizmapFileTaskFactoryProps = new Properties();
		loadVizmapFileTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Import");
		loadVizmapFileTaskFactoryProps.setProperty(MENU_GRAVITY,"3.0");
		loadVizmapFileTaskFactoryProps.setProperty(TITLE,"Styles...");
		loadVizmapFileTaskFactoryProps.setProperty(COMMAND,"load file");
		loadVizmapFileTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"vizmap");
		loadVizmapFileTaskFactoryProps.setProperty(COMMAND_DESCRIPTION,"Load styles from a file");
		registerService(bc,loadVizmapFileTaskFactory,TaskFactory.class, loadVizmapFileTaskFactoryProps);
		registerService(bc,loadVizmapFileTaskFactory,LoadVizmapFileTaskFactory.class, new Properties());

		Properties importAttrsFileTaskFactoryProps = new Properties();
		importAttrsFileTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Import.Table[2.0]");
		importAttrsFileTaskFactoryProps.setProperty(MENU_GRAVITY,"1.0");
		importAttrsFileTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"2.2");
		importAttrsFileTaskFactoryProps.setProperty(TITLE,"File...");
		importAttrsFileTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/import-table-32.png").toString());
		importAttrsFileTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		importAttrsFileTaskFactoryProps.setProperty(TOOLTIP,"Import Table From File");
		//importAttrsFileTaskFactoryProps.setProperty(COMMAND,"load file");
		//importAttrsFileTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"table");
		//importAttrsFileTaskFactoryProps.setProperty(ENABLE_FOR,ENABLE_FOR_NETWORK);
		registerService(bc,loadTableFileTaskFactory,TaskFactory.class, importAttrsFileTaskFactoryProps);
		registerService(bc,loadTableFileTaskFactory,LoadTableFileTaskFactory.class, importAttrsFileTaskFactoryProps);


		Properties importAttrsURLTaskFactoryProps = new Properties();
		importAttrsURLTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Import.Table[2.0]");
		importAttrsURLTaskFactoryProps.setProperty(MENU_GRAVITY,"2.0");
//		importAttrsURLTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"2.3");
		importAttrsURLTaskFactoryProps.setProperty(TITLE,"URL...");
//		importAttrsURLTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/import-table-url-32.png").toString());
//		importAttrsURLTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		importAttrsURLTaskFactoryProps.setProperty(TOOLTIP,"Import Table From URL");
		//importAttrsURLTaskFactoryProps.setProperty(COMMAND,"load url");
		//importAttrsURLTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"table");
		//importAttrsURLTaskFactoryProps.setProperty(ENABLE_FOR,ENABLE_FOR_NETWORK);
		registerService(bc,loadTableURLTaskFactory,TaskFactory.class, importAttrsURLTaskFactoryProps);
		registerService(bc,loadTableURLTaskFactory,LoadTableURLTaskFactory.class, importAttrsURLTaskFactoryProps);

		Properties proxySettingsTaskFactoryProps = new Properties();
		proxySettingsTaskFactoryProps.setProperty(PREFERRED_MENU,"Edit.Preferences");
		proxySettingsTaskFactoryProps.setProperty(MENU_GRAVITY,"3.0");
		proxySettingsTaskFactoryProps.setProperty(TITLE,"Proxy Settings...");
		registerService(bc,proxySettingsTaskFactory,TaskFactory.class, proxySettingsTaskFactoryProps);

		{
			DeleteSelectedNodesAndEdgesTaskFactoryImpl factory = new DeleteSelectedNodesAndEdgesTaskFactoryImpl(serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(PREFERRED_MENU, "Edit");
			props.setProperty(ENABLE_FOR, ENABLE_FOR_SELECTED_NODES_OR_EDGES);
			props.setProperty(TITLE, "Delete Selected Nodes and Edges");
			props.setProperty(MENU_GRAVITY, "5.0");
			props.setProperty(ACCELERATOR, "DELETE");
			props.setProperty(COMMAND, "delete");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND_DESCRIPTION, "Delete nodes or edges from a network");
			registerService(bc, factory, NetworkTaskFactory.class, props);
			registerService(bc, factory, DeleteSelectedNodesAndEdgesTaskFactory.class, props);
		}

		Properties selectAllTaskFactoryProps = new Properties();
		selectAllTaskFactoryProps.setProperty(PREFERRED_MENU,"Select");
		selectAllTaskFactoryProps.setProperty(ACCELERATOR,"cmd alt a");
		selectAllTaskFactoryProps.setProperty(ENABLE_FOR,ENABLE_FOR_NETWORK);
		selectAllTaskFactoryProps.setProperty(TITLE,"Select all nodes and edges");
		selectAllTaskFactoryProps.setProperty(MENU_GRAVITY,"5.0");
		selectAllTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		// selectAllTaskFactoryProps.setProperty(COMMAND,"select all");
		// selectAllTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,selectAllTaskFactory,NetworkTaskFactory.class, selectAllTaskFactoryProps);
		registerService(bc,selectAllTaskFactory,SelectAllTaskFactory.class, selectAllTaskFactoryProps);

		Properties selectAllViewTaskFactoryProps = new Properties();
		selectAllViewTaskFactoryProps.setProperty(PREFERRED_MENU, NETWORK_SELECT_MENU);
		selectAllViewTaskFactoryProps.setProperty(ENABLE_FOR,ENABLE_FOR_NETWORK_AND_VIEW);
		selectAllViewTaskFactoryProps.setProperty(TITLE,"All nodes and edges");
		selectAllViewTaskFactoryProps.setProperty(MENU_GRAVITY,"1.1");
		selectAllViewTaskFactoryProps.setProperty(PREFERRED_ACTION, "NEW");
		selectAllViewTaskFactoryProps.setProperty(IN_MENU_BAR,"false");
		registerService(bc,selectAllTaskFactory,NetworkViewTaskFactory.class, selectAllViewTaskFactoryProps);

		Properties selectAllEdgesTaskFactoryProps = new Properties();
		selectAllEdgesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Edges");
		selectAllEdgesTaskFactoryProps.setProperty(ACCELERATOR,"cmd alt a");
		selectAllEdgesTaskFactoryProps.setProperty(ENABLE_FOR,ENABLE_FOR_NETWORK);
		selectAllEdgesTaskFactoryProps.setProperty(TITLE,"Select all edges");
		selectAllEdgesTaskFactoryProps.setProperty(MENU_GRAVITY,"4.0");
		registerService(bc,selectAllEdgesTaskFactory,NetworkTaskFactory.class, selectAllEdgesTaskFactoryProps);
		registerService(bc,selectAllEdgesTaskFactory,SelectAllEdgesTaskFactory.class, selectAllEdgesTaskFactoryProps);

		Properties selectAllNodesTaskFactoryProps = new Properties();
		selectAllNodesTaskFactoryProps.setProperty(ENABLE_FOR,ENABLE_FOR_NETWORK);
		selectAllNodesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Nodes");
		selectAllNodesTaskFactoryProps.setProperty(MENU_GRAVITY,"4.0");
		selectAllNodesTaskFactoryProps.setProperty(ACCELERATOR,"cmd a");
		selectAllNodesTaskFactoryProps.setProperty(TITLE,"Select all nodes");
		registerService(bc,selectAllNodesTaskFactory,NetworkTaskFactory.class, selectAllNodesTaskFactoryProps);
		registerService(bc,selectAllNodesTaskFactory,SelectAllNodesTaskFactory.class, selectAllNodesTaskFactoryProps);

		Properties selectAdjacentEdgesTaskFactoryProps = new Properties();
		selectAdjacentEdgesTaskFactoryProps.setProperty(ENABLE_FOR,ENABLE_FOR_NETWORK);
		selectAdjacentEdgesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Edges");
		selectAdjacentEdgesTaskFactoryProps.setProperty(MENU_GRAVITY,"6.0");
		selectAdjacentEdgesTaskFactoryProps.setProperty(ACCELERATOR,"alt e");
		selectAdjacentEdgesTaskFactoryProps.setProperty(TITLE,"Select adjacent edges");
		// selectAdjacentEdgesTaskFactoryProps.setProperty(COMMAND,"select adjacent");
		// selectAdjacentEdgesTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"edge");
		registerService(bc,selectAdjacentEdgesTaskFactory,NetworkTaskFactory.class, selectAdjacentEdgesTaskFactoryProps);
		registerService(bc,selectAdjacentEdgesTaskFactory,SelectAdjacentEdgesTaskFactory.class, selectAdjacentEdgesTaskFactoryProps);

		Properties selectConnectedNodesTaskFactoryProps = new Properties();
		selectConnectedNodesTaskFactoryProps.setProperty(ENABLE_FOR,ENABLE_FOR_NETWORK);
		selectConnectedNodesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Nodes");
		selectConnectedNodesTaskFactoryProps.setProperty(MENU_GRAVITY,"7.0");
		selectConnectedNodesTaskFactoryProps.setProperty(ACCELERATOR,"cmd 7");
		selectConnectedNodesTaskFactoryProps.setProperty(TITLE,"Nodes connected by selected edges");
		// selectConnectedNodesTaskFactoryProps.setProperty(COMMAND,"select by connected edges");
		// selectConnectedNodesTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"node");
		registerService(bc,selectConnectedNodesTaskFactory,NetworkTaskFactory.class, selectConnectedNodesTaskFactoryProps);
		registerService(bc,selectConnectedNodesTaskFactory,SelectConnectedNodesTaskFactory.class, selectConnectedNodesTaskFactoryProps);

		Properties selectFirstNeighborsTaskFactoryProps = new Properties();
		selectFirstNeighborsTaskFactoryProps.setProperty(ENABLE_FOR,ENABLE_FOR_SELECTED_NODES_OR_EDGES);
		selectFirstNeighborsTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Nodes.First Neighbors of Selected Nodes");
		selectFirstNeighborsTaskFactoryProps.setProperty(MENU_GRAVITY,"6.0");
		selectFirstNeighborsTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"9.15");
		selectFirstNeighborsTaskFactoryProps.setProperty(ACCELERATOR,"cmd 6");
		selectFirstNeighborsTaskFactoryProps.setProperty(TITLE,"Undirected");
		selectFirstNeighborsTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/first-neighbors-32.png").toString());
		selectFirstNeighborsTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		selectFirstNeighborsTaskFactoryProps.setProperty(TOOLTIP,"First Neighbors of Selected Nodes (Undirected)");
		// selectFirstNeighborsTaskFactoryProps.setProperty(COMMAND,"select first neighbors undirected");
		// selectFirstNeighborsTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"node");
		registerService(bc,selectFirstNeighborsTaskFactory,NetworkTaskFactory.class, selectFirstNeighborsTaskFactoryProps);
		registerService(bc,selectFirstNeighborsTaskFactory,SelectFirstNeighborsTaskFactory.class, selectFirstNeighborsTaskFactoryProps);

		Properties selectFirstNeighborsTaskFactoryInEdgeProps = new Properties();
		selectFirstNeighborsTaskFactoryInEdgeProps.setProperty(ENABLE_FOR,ENABLE_FOR_NETWORK);
		selectFirstNeighborsTaskFactoryInEdgeProps.setProperty(PREFERRED_MENU,"Select.Nodes.First Neighbors of Selected Nodes");
		selectFirstNeighborsTaskFactoryInEdgeProps.setProperty(MENU_GRAVITY,"6.1");
		selectFirstNeighborsTaskFactoryInEdgeProps.setProperty(TITLE,"Directed: Incoming");
		selectFirstNeighborsTaskFactoryInEdgeProps.setProperty(TOOLTIP,"First Neighbors of Selected Nodes (Directed: Incoming)");
		// selectFirstNeighborsTaskFactoryInEdgeProps.setProperty(COMMAND,"select first neighbors incoming");
		// selectFirstNeighborsTaskFactoryInEdgeProps.setProperty(COMMAND_NAMESPACE,"node");
		registerService(bc,selectFirstNeighborsTaskFactoryInEdge,NetworkTaskFactory.class, selectFirstNeighborsTaskFactoryInEdgeProps);
		registerService(bc,selectFirstNeighborsTaskFactoryInEdge,SelectFirstNeighborsTaskFactory.class, selectFirstNeighborsTaskFactoryInEdgeProps);

		Properties selectFirstNeighborsTaskFactoryOutEdgeProps = new Properties();
		selectFirstNeighborsTaskFactoryOutEdgeProps.setProperty(ENABLE_FOR,ENABLE_FOR_NETWORK);
		selectFirstNeighborsTaskFactoryOutEdgeProps.setProperty(PREFERRED_MENU,"Select.Nodes.First Neighbors of Selected Nodes");
		selectFirstNeighborsTaskFactoryOutEdgeProps.setProperty(MENU_GRAVITY,"6.2");
		selectFirstNeighborsTaskFactoryOutEdgeProps.setProperty(TITLE,"Directed: Outgoing");
		selectFirstNeighborsTaskFactoryOutEdgeProps.setProperty(TOOLTIP,"First Neighbors of Selected Nodes (Directed: Outgoing)");
		// selectFirstNeighborsTaskFactoryOutEdgeProps.setProperty(COMMAND,"select first neighbors outgoing");
		// selectFirstNeighborsTaskFactoryOutEdgeProps.setProperty(COMMAND_NAMESPACE,"node");
		registerService(bc,selectFirstNeighborsTaskFactoryOutEdge,NetworkTaskFactory.class, selectFirstNeighborsTaskFactoryOutEdgeProps);
		registerService(bc,selectFirstNeighborsTaskFactoryOutEdge,SelectFirstNeighborsTaskFactory.class, selectFirstNeighborsTaskFactoryOutEdgeProps);		

		Properties deselectAllTaskFactoryProps = new Properties();
		deselectAllTaskFactoryProps.setProperty(ENABLE_FOR,ENABLE_FOR_NETWORK);
		deselectAllTaskFactoryProps.setProperty(PREFERRED_MENU,"Select");
		deselectAllTaskFactoryProps.setProperty(MENU_GRAVITY,"5.1");
		deselectAllTaskFactoryProps.setProperty(ACCELERATOR,"cmd shift alt a");
		deselectAllTaskFactoryProps.setProperty(TITLE,"Deselect all nodes and edges");
		// deselectAllTaskFactoryProps.setProperty(COMMAND,"deselect all");
		// deselectAllTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,deselectAllTaskFactory,NetworkTaskFactory.class, deselectAllTaskFactoryProps);
		registerService(bc,deselectAllTaskFactory,DeselectAllTaskFactory.class, deselectAllTaskFactoryProps);

		Properties deselectAllEdgesTaskFactoryProps = new Properties();
		deselectAllEdgesTaskFactoryProps.setProperty(ENABLE_FOR,ENABLE_FOR_NETWORK);
		deselectAllEdgesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Edges");
		deselectAllEdgesTaskFactoryProps.setProperty(MENU_GRAVITY,"5.0");
		deselectAllEdgesTaskFactoryProps.setProperty(ACCELERATOR,"alt shift a");
		deselectAllEdgesTaskFactoryProps.setProperty(TITLE,"Deselect all edges");
		registerService(bc,deselectAllEdgesTaskFactory,NetworkTaskFactory.class, deselectAllEdgesTaskFactoryProps);
		registerService(bc,deselectAllEdgesTaskFactory,DeselectAllEdgesTaskFactory.class, deselectAllEdgesTaskFactoryProps);

		Properties deselectAllNodesTaskFactoryProps = new Properties();
		deselectAllNodesTaskFactoryProps.setProperty(ENABLE_FOR,ENABLE_FOR_NETWORK);
		deselectAllNodesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Nodes");
		deselectAllNodesTaskFactoryProps.setProperty(MENU_GRAVITY,"5.0");
		deselectAllNodesTaskFactoryProps.setProperty(ACCELERATOR,"cmd shift a");
		deselectAllNodesTaskFactoryProps.setProperty(TITLE,"Deselect all nodes");
		registerService(bc,deselectAllNodesTaskFactory,NetworkTaskFactory.class, deselectAllNodesTaskFactoryProps);
		registerService(bc,deselectAllNodesTaskFactory,DeselectAllNodesTaskFactory.class, deselectAllNodesTaskFactoryProps);

		Properties invertSelectedEdgesTaskFactoryProps = new Properties();
		invertSelectedEdgesTaskFactoryProps.setProperty(ENABLE_FOR,ENABLE_FOR_NETWORK);
		invertSelectedEdgesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Edges");
		invertSelectedEdgesTaskFactoryProps.setProperty(MENU_GRAVITY,"1.0");
		invertSelectedEdgesTaskFactoryProps.setProperty(ACCELERATOR,"alt i");
		invertSelectedEdgesTaskFactoryProps.setProperty(TITLE,"Invert edge selection");
		registerService(bc,invertSelectedEdgesTaskFactory,NetworkTaskFactory.class, invertSelectedEdgesTaskFactoryProps);
		registerService(bc,invertSelectedEdgesTaskFactory,InvertSelectedEdgesTaskFactory.class, invertSelectedEdgesTaskFactoryProps);

		Properties invertSelectedNodesTaskFactoryProps = new Properties();
		invertSelectedNodesTaskFactoryProps.setProperty(ENABLE_FOR,ENABLE_FOR_SELECTED_NODES);
		invertSelectedNodesTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Nodes");
		invertSelectedNodesTaskFactoryProps.setProperty(MENU_GRAVITY,"1.0");
		invertSelectedNodesTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"9.2");
		invertSelectedNodesTaskFactoryProps.setProperty(ACCELERATOR,"cmd i");
		invertSelectedNodesTaskFactoryProps.setProperty(TITLE,"Invert node selection");
		invertSelectedNodesTaskFactoryProps.setProperty(IN_TOOL_BAR,"false");
		invertSelectedNodesTaskFactoryProps.setProperty(TOOLTIP,"Invert Node Selection");
		registerService(bc,invertSelectedNodesTaskFactory,NetworkTaskFactory.class, invertSelectedNodesTaskFactoryProps);
		registerService(bc,invertSelectedNodesTaskFactory,InvertSelectedNodesTaskFactory.class, invertSelectedNodesTaskFactoryProps);

		Properties selectFromFileListTaskFactoryProps = new Properties();
		selectFromFileListTaskFactoryProps.setProperty(ENABLE_FOR,ENABLE_FOR_NETWORK);
		selectFromFileListTaskFactoryProps.setProperty(PREFERRED_MENU,"Select.Nodes");
		selectFromFileListTaskFactoryProps.setProperty(MENU_GRAVITY,"8.0");
		selectFromFileListTaskFactoryProps.setProperty(ACCELERATOR,"cmd i");
		selectFromFileListTaskFactoryProps.setProperty(TITLE,"From ID List file...");
		selectFromFileListTaskFactoryProps.setProperty(COMMAND,"select from file");
		selectFromFileListTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"node");
		selectFromFileListTaskFactoryProps.setProperty(COMMAND_DESCRIPTION,"Select nodes from a file");
		registerService(bc,selectFromFileListTaskFactory,NetworkTaskFactory.class, selectFromFileListTaskFactoryProps);
		registerService(bc,selectFromFileListTaskFactory,SelectFromFileListTaskFactory.class, selectFromFileListTaskFactoryProps);

		Properties selectFirstNeighborsNodeViewTaskFactoryProps = new Properties();
		selectFirstNeighborsNodeViewTaskFactoryProps.setProperty(PREFERRED_MENU,NODE_SELECT_MENU);
		selectFirstNeighborsNodeViewTaskFactoryProps.setProperty(MENU_GRAVITY,"1.0");
		selectFirstNeighborsNodeViewTaskFactoryProps.setProperty(TITLE,"Select First Neighbors (Undirected)");
		registerService(bc,selectFirstNeighborsNodeViewTaskFactory,NodeViewTaskFactory.class, selectFirstNeighborsNodeViewTaskFactoryProps);
		registerService(bc,selectFirstNeighborsNodeViewTaskFactory,SelectFirstNeighborsNodeViewTaskFactory.class, selectFirstNeighborsNodeViewTaskFactoryProps);

		{
			UnHideAllTaskFactoryImpl factory = new UnHideAllTaskFactoryImpl(serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
			props.setProperty(PREFERRED_MENU, "Select");
			props.setProperty(MENU_GRAVITY, "3.0");
			props.setProperty(TOOL_BAR_GRAVITY, "9.6");
			props.setProperty(TITLE, factory.getDescription());
			props.setProperty(LARGE_ICON_URL, getClass().getResource("/images/icons/show-all-32.png").toString());
			props.setProperty(IN_TOOL_BAR, "true");
			props.setProperty(TOOLTIP, factory.getDescription());
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
			registerService(bc, factory, UnHideAllTaskFactory.class, props);
		}
		{
			HideSelectedTaskFactoryImpl factory = new HideSelectedTaskFactoryImpl(serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_SELECTED_NODES_OR_EDGES);
			props.setProperty(PREFERRED_MENU, "Select");
			props.setProperty(MENU_GRAVITY, "3.1");
			props.setProperty(TOOL_BAR_GRAVITY, "9.5");
			props.setProperty(TITLE, factory.getDescription());
			props.setProperty(LARGE_ICON_URL, getClass().getResource("/images/icons/hide-selected-32.png").toString());
			props.setProperty(IN_TOOL_BAR, "true");
			props.setProperty(TOOLTIP, factory.getDescription());
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
			registerService(bc, factory, HideSelectedTaskFactory.class, props);
		}
		{
			HideUnselectedTaskFactoryImpl factory = new HideUnselectedTaskFactoryImpl(serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(PREFERRED_MENU, "Select");
			props.setProperty(MENU_GRAVITY, "3.2");
			props.setProperty(TITLE, factory.getDescription());
			props.setProperty(TOOLTIP, factory.getDescription());
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
			registerService(bc, factory, HideUnselectedTaskFactory.class, props);
		}
		{
			HideSelectedNodesTaskFactoryImpl factory = new HideSelectedNodesTaskFactoryImpl(serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_SELECTED_NODES);
			props.setProperty(PREFERRED_MENU, "Select.Nodes");
			props.setProperty(MENU_GRAVITY, "2.0");
			props.setProperty(TITLE, factory.getDescription());
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
			registerService(bc, factory, HideSelectedNodesTaskFactory.class, props);
		}
		{
			HideUnselectedNodesTaskFactoryImpl factory = new HideUnselectedNodesTaskFactoryImpl(serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(PREFERRED_MENU, "Select.Nodes");
			props.setProperty(MENU_GRAVITY, "2.1");
			props.setProperty(TITLE, factory.getDescription());
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
			registerService(bc, factory, HideUnselectedNodesTaskFactory.class, props);
		}
		{
			HideSelectedEdgesTaskFactoryImpl factory = new HideSelectedEdgesTaskFactoryImpl(serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_SELECTED_EDGES);
			props.setProperty(PREFERRED_MENU, "Select.Edges");
			props.setProperty(MENU_GRAVITY, "2.0");
			props.setProperty(TITLE, factory.getDescription());
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
			registerService(bc, factory, HideSelectedEdgesTaskFactory.class, props);
		}
		{
			HideUnselectedEdgesTaskFactoryImpl factory = new HideUnselectedEdgesTaskFactoryImpl(serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(PREFERRED_MENU, "Select.Edges");
			props.setProperty(MENU_GRAVITY, "2.1");
			props.setProperty(TITLE, factory.getDescription());
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
			registerService(bc, factory, HideUnselectedEdgesTaskFactory.class, props);
		}
		{
			UnHideAllNodesTaskFactoryImpl factory = new UnHideAllNodesTaskFactoryImpl(serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
			props.setProperty(PREFERRED_MENU, "Select.Nodes");
			props.setProperty(MENU_GRAVITY, "3.0");
			props.setProperty(TITLE, factory.getDescription());
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
			registerService(bc, factory, UnHideAllNodesTaskFactory.class, props);
		}
		{
			UnHideAllEdgesTaskFactoryImpl factory = new UnHideAllEdgesTaskFactoryImpl(serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
			props.setProperty(PREFERRED_MENU, "Select.Edges");
			props.setProperty(MENU_GRAVITY, "3.0");
			props.setProperty(TITLE, factory.getDescription());
			registerService(bc, factory, NetworkViewTaskFactory.class, props);
			registerService(bc, factory, UnHideAllEdgesTaskFactory.class, props);
		}

		Properties newEmptyNetworkTaskFactoryProps = new Properties();
		newEmptyNetworkTaskFactoryProps.setProperty(PREFERRED_MENU,"File.New.Network");
		newEmptyNetworkTaskFactoryProps.setProperty(MENU_GRAVITY,"4.0");
		newEmptyNetworkTaskFactoryProps.setProperty(TITLE,"Empty Network");
		newEmptyNetworkTaskFactoryProps.setProperty(COMMAND,"create empty");
		newEmptyNetworkTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		newEmptyNetworkTaskFactoryProps.setProperty(COMMAND_DESCRIPTION,"Create an empty network");
		registerService(bc,newEmptyNetworkTaskFactory,TaskFactory.class, newEmptyNetworkTaskFactoryProps);
		registerService(bc,newEmptyNetworkTaskFactory,NewEmptyNetworkViewFactory.class, newEmptyNetworkTaskFactoryProps);
		registerServiceListener(bc, newEmptyNetworkTaskFactory, "addNetworkViewRenderer", "removeNetworkViewRenderer", NetworkViewRenderer.class);

		Properties newNetworkSelectedNodesEdgesTaskFactoryProps = new Properties();
		newNetworkSelectedNodesEdgesTaskFactoryProps.setProperty(ENABLE_FOR,ENABLE_FOR_SELECTED_NODES_OR_EDGES);
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
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/new-from-selected-32.png").toString());
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty(ACCELERATOR,"cmd n");
		newNetworkSelectedNodesOnlyTaskFactoryProps.setProperty(ENABLE_FOR,ENABLE_FOR_SELECTED_NODES);
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
		newNetworkCommandTaskFactoryProps.setProperty(COMMAND_DESCRIPTION,"Create a new network");
		registerService(bc,newNetworkCommandTaskFactory,NetworkTaskFactory.class, newNetworkCommandTaskFactoryProps);

		Properties cloneNetworkTaskFactoryProps = new Properties();
		cloneNetworkTaskFactoryProps.setProperty(ENABLE_FOR,ENABLE_FOR_NETWORK);
		cloneNetworkTaskFactoryProps.setProperty(PREFERRED_MENU,"File.New.Network");
		cloneNetworkTaskFactoryProps.setProperty(MENU_GRAVITY,"3.0");
		cloneNetworkTaskFactoryProps.setProperty(TITLE,"Clone Current Network");
		cloneNetworkTaskFactoryProps.setProperty(COMMAND,"clone");
		cloneNetworkTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		cloneNetworkTaskFactoryProps.setProperty(COMMAND_DESCRIPTION,"Make a copy of the current network");
		registerService(bc,cloneNetworkTaskFactory,NetworkTaskFactory.class, cloneNetworkTaskFactoryProps);
		registerService(bc,cloneNetworkTaskFactory,CloneNetworkTaskFactory.class, cloneNetworkTaskFactoryProps);

		Properties destroyNetworkTaskFactoryProps = new Properties();
		destroyNetworkTaskFactoryProps.setProperty(PREFERRED_MENU,"Edit");
		destroyNetworkTaskFactoryProps.setProperty(ACCELERATOR,"cmd shift w");
		destroyNetworkTaskFactoryProps.setProperty(ENABLE_FOR,ENABLE_FOR_NETWORK);
		destroyNetworkTaskFactoryProps.setProperty(TITLE,"Destroy Network");
		destroyNetworkTaskFactoryProps.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU,"true");
		destroyNetworkTaskFactoryProps.setProperty(MENU_GRAVITY,"3.2");
		//destroyNetworkTaskFactoryProps.setProperty(COMMAND,"destroy");
		//destroyNetworkTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		registerService(bc,destroyNetworkTaskFactory,NetworkCollectionTaskFactory.class, destroyNetworkTaskFactoryProps);
		registerService(bc,destroyNetworkTaskFactory,DestroyNetworkTaskFactory.class, destroyNetworkTaskFactoryProps);
		Properties destroyNetworkTaskFactoryProps2 = new Properties();
		destroyNetworkTaskFactoryProps2.setProperty(ENABLE_FOR,ENABLE_FOR_NETWORK);
		destroyNetworkTaskFactoryProps2.setProperty(COMMAND,"destroy");
		destroyNetworkTaskFactoryProps2.setProperty(COMMAND_NAMESPACE,"network");
		destroyNetworkTaskFactoryProps2.setProperty(COMMAND_DESCRIPTION,"Destroy (delete) a network");
		registerService(bc,destroyNetworkTaskFactory,TaskFactory.class, destroyNetworkTaskFactoryProps2);


		Properties destroyNetworkViewTaskFactoryProps = new Properties();
		destroyNetworkViewTaskFactoryProps.setProperty(PREFERRED_MENU,"Edit");
		destroyNetworkViewTaskFactoryProps.setProperty(ACCELERATOR,"cmd w");
		destroyNetworkViewTaskFactoryProps.setProperty(ENABLE_FOR,ENABLE_FOR_NETWORK_AND_VIEW);
		destroyNetworkViewTaskFactoryProps.setProperty(TITLE,"Destroy Views");
		destroyNetworkViewTaskFactoryProps.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU,"true");
		destroyNetworkViewTaskFactoryProps.setProperty(MENU_GRAVITY,"3.1");
		destroyNetworkViewTaskFactoryProps.setProperty(COMMAND,"destroy");
		destroyNetworkViewTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"view");
		destroyNetworkViewTaskFactoryProps.setProperty(COMMAND_DESCRIPTION,"Destroy (delete) a network view");
		registerService(bc,destroyNetworkViewTaskFactory,NetworkViewCollectionTaskFactory.class, destroyNetworkViewTaskFactoryProps);
		registerService(bc,destroyNetworkViewTaskFactory,DestroyNetworkViewTaskFactory.class, destroyNetworkViewTaskFactoryProps);

		Properties zoomInTaskFactoryProps = new Properties();
		zoomInTaskFactoryProps.setProperty(ACCELERATOR,"cmd equals");
		zoomInTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/zoom-in-32.png").toString());
		zoomInTaskFactoryProps.setProperty(ENABLE_FOR,ENABLE_FOR_NETWORK_AND_VIEW);
		zoomInTaskFactoryProps.setProperty(TITLE,"Zoom In");
		zoomInTaskFactoryProps.setProperty(TOOLTIP,"Zoom In");
		zoomInTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"5.1");
		zoomInTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
//		zoomInTaskFactoryProps.setProperty(COMMAND,"zoom in");
//		zoomInTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"view");
		registerService(bc,zoomInTaskFactory,NetworkTaskFactory.class, zoomInTaskFactoryProps);

		Properties zoomOutTaskFactoryProps = new Properties();
		zoomOutTaskFactoryProps.setProperty(ACCELERATOR,"cmd minus");
		zoomOutTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/zoom-out-32.png").toString());
		zoomOutTaskFactoryProps.setProperty(ENABLE_FOR,ENABLE_FOR_NETWORK_AND_VIEW);
		zoomOutTaskFactoryProps.setProperty(TITLE,"Zoom Out");
		zoomOutTaskFactoryProps.setProperty(TOOLTIP,"Zoom Out");
		zoomOutTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"5.2");
		zoomOutTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
//		zoomOutTaskFactoryProps.setProperty(COMMAND,"zoom out");
//		zoomOutTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"view");
		registerService(bc,zoomOutTaskFactory,NetworkTaskFactory.class, zoomOutTaskFactoryProps);

		Properties fitSelectedTaskFactoryProps = new Properties();
		fitSelectedTaskFactoryProps.setProperty(ACCELERATOR,"cmd shift f");
		fitSelectedTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/zoom-selected-32.png").toString());
		fitSelectedTaskFactoryProps.setProperty(ENABLE_FOR,ENABLE_FOR_SELECTED_NODES_OR_EDGES);
		fitSelectedTaskFactoryProps.setProperty(TITLE,"Fit Selected");
		fitSelectedTaskFactoryProps.setProperty(TOOLTIP,"Zoom selected region");
		fitSelectedTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"5.4");
		fitSelectedTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		fitSelectedTaskFactoryProps.setProperty(COMMAND,"fit selected");
		fitSelectedTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"view");
		fitSelectedTaskFactoryProps.setProperty(COMMAND_DESCRIPTION,"Fit the selected nodes and edges into the view");
		registerService(bc,fitSelectedTaskFactory,NetworkTaskFactory.class, fitSelectedTaskFactoryProps);

		Properties fitContentTaskFactoryProps = new Properties();
		fitContentTaskFactoryProps.setProperty(ACCELERATOR,"cmd f");
		fitContentTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/zoom-fit-32.png").toString());
		fitContentTaskFactoryProps.setProperty(ENABLE_FOR,ENABLE_FOR_NETWORK_AND_VIEW);
		fitContentTaskFactoryProps.setProperty(TITLE,"Fit Content");
		fitContentTaskFactoryProps.setProperty(TOOLTIP,"Zoom out to display all of current Network");
		fitContentTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"5.3");
		fitContentTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		fitContentTaskFactoryProps.setProperty(COMMAND,"fit content");
		fitContentTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"view");
		fitContentTaskFactoryProps.setProperty(COMMAND_DESCRIPTION,"Fit all of the nodes and edges into the view");
		registerService(bc,fitContentTaskFactory,NetworkTaskFactory.class, fitContentTaskFactoryProps);

		Properties editNetworkTitleTaskFactoryProps = new Properties();
		editNetworkTitleTaskFactoryProps.setProperty(ENABLE_FOR,"singleNetwork");
		editNetworkTitleTaskFactoryProps.setProperty(PREFERRED_MENU,"Edit");
		editNetworkTitleTaskFactoryProps.setProperty(MENU_GRAVITY,"5.5");
		editNetworkTitleTaskFactoryProps.setProperty(TITLE,"Rename Network...");
		editNetworkTitleTaskFactoryProps.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU,"true");
		editNetworkTitleTaskFactoryProps.setProperty(COMMAND,"rename");
		editNetworkTitleTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		editNetworkTitleTaskFactoryProps.setProperty(COMMAND_DESCRIPTION,"Rename a network");
		registerService(bc,editNetworkTitleTaskFactory,NetworkTaskFactory.class, editNetworkTitleTaskFactoryProps);
		registerService(bc,editNetworkTitleTaskFactory,EditNetworkTitleTaskFactory.class, editNetworkTitleTaskFactoryProps);


		Properties createNetworkViewTaskFactoryProps = new Properties();
		createNetworkViewTaskFactoryProps.setProperty(ID,"createNetworkViewTaskFactory");
		// No ENABLE_FOR because that is handled by the isReady() methdod of the task factory.
		createNetworkViewTaskFactoryProps.setProperty(PREFERRED_MENU,"Edit");
		createNetworkViewTaskFactoryProps.setProperty(MENU_GRAVITY,"3.0");
		createNetworkViewTaskFactoryProps.setProperty(TITLE,"Create View");
		createNetworkViewTaskFactoryProps.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU,"true");
		registerService(bc,createNetworkViewTaskFactory,NetworkCollectionTaskFactory.class, createNetworkViewTaskFactoryProps);
		registerService(bc,createNetworkViewTaskFactory,CreateNetworkViewTaskFactory.class, createNetworkViewTaskFactoryProps);
		registerServiceListener(bc, createNetworkViewTaskFactory, "addNetworkViewRenderer", "removeNetworkViewRenderer", NetworkViewRenderer.class);

		// For commands
		Properties createNetworkViewCommandProps = new Properties();
		createNetworkViewCommandProps.setProperty(COMMAND,"create");
		createNetworkViewCommandProps.setProperty(COMMAND_NAMESPACE,"view");
		createNetworkViewCommandProps.setProperty(COMMAND_DESCRIPTION,"Create a new view for a network");
		registerService(bc,createNetworkViewTaskFactory,TaskFactory.class, createNetworkViewCommandProps);

		Properties exportNetworkTaskFactoryProps = new Properties();
		exportNetworkTaskFactoryProps.setProperty(ENABLE_FOR,ENABLE_FOR_NETWORK);
		exportNetworkTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Export");
		exportNetworkTaskFactoryProps.setProperty(MENU_GRAVITY,"1.0");
		exportNetworkTaskFactoryProps.setProperty(TITLE,"Network...");
		exportNetworkTaskFactoryProps.setProperty(IN_TOOL_BAR,"false");
		exportNetworkTaskFactoryProps.setProperty(IN_CONTEXT_MENU,"false");
		exportNetworkTaskFactoryProps.setProperty(TOOLTIP,"Export Network to File");
		registerService(bc,exportNetworkTaskFactory,NetworkTaskFactory.class, exportNetworkTaskFactoryProps);
		registerService(bc,exportNetworkTaskFactory,ExportNetworkTaskFactory.class, exportNetworkTaskFactoryProps);

		Properties exportNetworkViewTaskFactoryProps = new Properties();
		exportNetworkViewTaskFactoryProps.setProperty(ENABLE_FOR,ENABLE_FOR_NETWORK_AND_VIEW);
		exportNetworkViewTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Export");
		exportNetworkViewTaskFactoryProps.setProperty(MENU_GRAVITY,"1.1");
		exportNetworkViewTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"3.0");
		exportNetworkViewTaskFactoryProps.setProperty(TITLE,"Network and View ...");
		exportNetworkViewTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/export-net-32.png").toString());
		exportNetworkViewTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		exportNetworkViewTaskFactoryProps.setProperty(IN_CONTEXT_MENU,"false");
		exportNetworkViewTaskFactoryProps.setProperty(TOOLTIP,"Export Network and View to File");
		exportNetworkViewTaskFactoryProps.setProperty(COMMAND,"export");
		exportNetworkViewTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"network");
		exportNetworkViewTaskFactoryProps.setProperty(COMMAND_DESCRIPTION,"Export a network and its view to a file");
		registerService(bc,exportNetworkViewTaskFactory,NetworkViewTaskFactory.class, exportNetworkViewTaskFactoryProps);
		registerService(bc,exportNetworkViewTaskFactory,ExportNetworkViewTaskFactory.class, exportNetworkViewTaskFactoryProps);

		Properties exportNetworkImageTaskFactoryProps = new Properties();
		exportNetworkImageTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Export");
		exportNetworkImageTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/export-img-32.png").toString());
		exportNetworkImageTaskFactoryProps.setProperty(ENABLE_FOR,ENABLE_FOR_NETWORK_AND_VIEW);
		exportNetworkImageTaskFactoryProps.setProperty(MENU_GRAVITY,"1.2");
		exportNetworkImageTaskFactoryProps.setProperty(TITLE,"Network View as Graphics...");
		exportNetworkImageTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"3.2");
		exportNetworkImageTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		exportNetworkImageTaskFactoryProps.setProperty(IN_CONTEXT_MENU,"false");
		exportNetworkImageTaskFactoryProps.setProperty(TOOLTIP,"Export Network Image to File");
		exportNetworkImageTaskFactoryProps.setProperty(COMMAND,"export");
		exportNetworkImageTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"view");
		exportNetworkImageTaskFactoryProps.setProperty(COMMAND_DESCRIPTION,"Export a view to a graphics file");
		registerService(bc,exportNetworkImageTaskFactory,NetworkViewTaskFactory.class, exportNetworkImageTaskFactoryProps);
		registerService(bc,exportNetworkImageTaskFactory,ExportNetworkImageTaskFactory.class, exportNetworkImageTaskFactoryProps);
		
		Properties exportCurrentTableTaskFactoryProps = new Properties();
		exportCurrentTableTaskFactoryProps.setProperty(ENABLE_FOR,"table");
		exportCurrentTableTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Export");
		exportCurrentTableTaskFactoryProps.setProperty(MENU_GRAVITY,"1.3");
		exportCurrentTableTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"3.1");
		exportCurrentTableTaskFactoryProps.setProperty(TITLE,"Table...");
		exportCurrentTableTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/export-table-32.png").toString());
		exportCurrentTableTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		exportCurrentTableTaskFactoryProps.setProperty(TOOLTIP,"Export Table to File");
		registerService(bc,exportCurrentTableTaskFactory, TaskFactory.class, exportCurrentTableTaskFactoryProps);
		registerService(bc,exportCurrentTableTaskFactory,ExportSelectedTableTaskFactory.class, exportCurrentTableTaskFactoryProps);
		
		Properties exportNoGuiCurrentTableTaskFactoryProps = new Properties();
		exportNoGuiCurrentTableTaskFactoryProps.setProperty(COMMAND,"export");
		exportNoGuiCurrentTableTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"table");
		exportNoGuiCurrentTableTaskFactoryProps.setProperty(COMMAND_DESCRIPTION,"Export a table to a file");
		registerService(bc,exportNoGuiCurrentTableTaskFactory, TaskFactory.class, exportNoGuiCurrentTableTaskFactoryProps);


		Properties exportVizmapTaskFactoryProps = new Properties();
		exportVizmapTaskFactoryProps.setProperty(ENABLE_FOR,"vizmap");
		exportVizmapTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Export");
		exportVizmapTaskFactoryProps.setProperty(MENU_GRAVITY,"1.4");
		exportVizmapTaskFactoryProps.setProperty(TITLE,"Styles...");
		exportVizmapTaskFactoryProps.setProperty(COMMAND,"export");
		exportVizmapTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"vizmap");
		exportVizmapTaskFactoryProps.setProperty(COMMAND_DESCRIPTION,"Export styles to a file");
		registerService(bc,exportVizmapTaskFactory,TaskFactory.class, exportVizmapTaskFactoryProps);
		registerService(bc,exportVizmapTaskFactory,ExportVizmapTaskFactory.class, exportVizmapTaskFactoryProps);
		
		Properties mergeTablesTaskFactoryProps = new Properties();
		mergeTablesTaskFactoryProps.setProperty(ENABLE_FOR,"table");
		mergeTablesTaskFactoryProps.setProperty(PREFERRED_MENU,"Tools.Merge[2.0]");
		mergeTablesTaskFactoryProps.setProperty(TITLE,"Tables...");
		//MergeGlobalTaskFactoryProps.setProperty(ServiceProperties.INSERT_SEPARATOR_AFTER, "true");
		//MergeGlobalTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"1.1");
		mergeTablesTaskFactoryProps.setProperty(MENU_GRAVITY,"5.4");
		mergeTablesTaskFactoryProps.setProperty(TOOLTIP,"Merge Tables");
		mergeTablesTaskFactoryProps.setProperty(COMMAND,"merge");
		mergeTablesTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"table");
		mergeTablesTaskFactoryProps.setProperty(COMMAND_DESCRIPTION,"Merge tables together");
		registerService(bc,mergeTablesTaskFactory,TaskFactory.class, mergeTablesTaskFactoryProps);
		registerService(bc,mergeTablesTaskFactory,MergeTablesTaskFactory.class, mergeTablesTaskFactoryProps);


		Properties newSessionTaskFactoryProps = new Properties();
		newSessionTaskFactoryProps.setProperty(PREFERRED_MENU,"File.New");
		newSessionTaskFactoryProps.setProperty(MENU_GRAVITY,"1.1");
		newSessionTaskFactoryProps.setProperty(TITLE,"Session");
		newSessionTaskFactoryProps.setProperty(COMMAND,"new");
		newSessionTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"session");
		newSessionTaskFactoryProps.setProperty(COMMAND_DESCRIPTION,"Create a new, empty session");
		registerService(bc,newSessionTaskFactory,TaskFactory.class, newSessionTaskFactoryProps);
		registerService(bc,newSessionTaskFactory,NewSessionTaskFactory.class, newSessionTaskFactoryProps);

		Properties openSessionTaskFactoryProps = new Properties();
		openSessionTaskFactoryProps.setProperty(ID,"openSessionTaskFactory");
		openSessionTaskFactoryProps.setProperty(PREFERRED_MENU,"File");
		openSessionTaskFactoryProps.setProperty(ACCELERATOR,"cmd o");
		openSessionTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/open-file-32.png").toString());
		openSessionTaskFactoryProps.setProperty(TITLE,"Open...");
		openSessionTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"1.0");
		openSessionTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		openSessionTaskFactoryProps.setProperty(MENU_GRAVITY,"1.2");
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
		openSessionCommandTaskFactoryProps.setProperty(COMMAND_DESCRIPTION,"Open a session from a file");
		registerService(bc,openSessionCommandTaskFactory,TaskFactory.class, openSessionCommandTaskFactoryProps);

		Properties saveSessionTaskFactoryProps = new Properties();
		saveSessionTaskFactoryProps.setProperty(PREFERRED_MENU,"File");
		saveSessionTaskFactoryProps.setProperty(ACCELERATOR,"cmd s");
		saveSessionTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/save-32.png").toString());
		saveSessionTaskFactoryProps.setProperty(TITLE,"Save");
		saveSessionTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY,"1.1");
		saveSessionTaskFactoryProps.setProperty(IN_TOOL_BAR,"true");
		saveSessionTaskFactoryProps.setProperty(MENU_GRAVITY,"3.0");
		saveSessionTaskFactoryProps.setProperty(TOOLTIP,"Save Session");
		saveSessionTaskFactoryProps.setProperty(COMMAND,"save");
		saveSessionTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"session");
		saveSessionTaskFactoryProps.setProperty(COMMAND_DESCRIPTION,"Save the session");
		registerService(bc,saveSessionTaskFactory,TaskFactory.class, saveSessionTaskFactoryProps);

		Properties saveSessionAsTaskFactoryProps = new Properties();
		saveSessionAsTaskFactoryProps.setProperty(PREFERRED_MENU,"File");
		saveSessionAsTaskFactoryProps.setProperty(ACCELERATOR,"cmd shift s");
		saveSessionAsTaskFactoryProps.setProperty(MENU_GRAVITY,"3.1");
		saveSessionAsTaskFactoryProps.setProperty(TITLE,"Save As...");
		saveSessionAsTaskFactoryProps.setProperty(COMMAND,"save as");
		saveSessionAsTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"session");
		saveSessionAsTaskFactoryProps.setProperty(COMMAND_DESCRIPTION,"Save the session to a file");
		registerService(bc,saveSessionAsTaskFactory,TaskFactory.class, saveSessionAsTaskFactoryProps);
		registerService(bc,saveSessionAsTaskFactory,SaveSessionAsTaskFactory.class, saveSessionAsTaskFactoryProps);

		Properties applyPreferredLayoutTaskFactoryProps = new Properties();
		applyPreferredLayoutTaskFactoryProps.setProperty(PREFERRED_MENU,"Layout");
		applyPreferredLayoutTaskFactoryProps.setProperty(ACCELERATOR,"fn5");
		applyPreferredLayoutTaskFactoryProps.setProperty(LARGE_ICON_URL,getClass().getResource("/images/icons/apply-layout-32.png").toString());
		applyPreferredLayoutTaskFactoryProps.setProperty(ENABLE_FOR,ENABLE_FOR_NETWORK_AND_VIEW);
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
		applyPreferredLayoutTaskFactoryProps2.setProperty(COMMAND_DESCRIPTION,"Execute the preferred layout on a network");
		registerService(bc,applyPreferredLayoutTaskFactory,TaskFactory.class, applyPreferredLayoutTaskFactoryProps2);

		Properties deleteColumnTaskFactoryProps = new Properties();
		deleteColumnTaskFactoryProps.setProperty(TITLE,"Delete column");
		deleteColumnTaskFactoryProps.setProperty(COMMAND,"delete column");
		deleteColumnTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"table");
		deleteColumnTaskFactoryProps.setProperty(COMMAND_DESCRIPTION,"Delete a column from a table");
		registerService(bc,deleteColumnTaskFactory,TableColumnTaskFactory.class, deleteColumnTaskFactoryProps);
		registerService(bc,deleteColumnTaskFactory,DeleteColumnTaskFactory.class, deleteColumnTaskFactoryProps);

		Properties renameColumnTaskFactoryProps = new Properties();
		renameColumnTaskFactoryProps.setProperty(TITLE,"Rename column");
		renameColumnTaskFactoryProps.setProperty(COMMAND,"rename column");
		renameColumnTaskFactoryProps.setProperty(COMMAND_NAMESPACE,"table");
		renameColumnTaskFactoryProps.setProperty(COMMAND_DESCRIPTION,"Rename a column in a table");
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
		connectSelectedNodesTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Create new edges that connect the selected nodes");
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
		groupNodesTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Create a new group of nodes");
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
		unGroupNodesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "group");
		unGroupNodesTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Ungroup a set of previously grouped nodes");
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
			new GroupNodeContextTaskFactoryImpl(cyApplicationManagerServiceRef, cyGroupManager, true);
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
		collapseGroupTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Collapse a group"); // TODO right namespace?
		registerService(bc,collapseGroupTaskFactory,TaskFactory.class, collapseGroupTaskFactoryProps);

		GroupNodeContextTaskFactoryImpl expandGroupTaskFactory = 
			new GroupNodeContextTaskFactoryImpl(cyApplicationManagerServiceRef, cyGroupManager, false);
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
		expandGroupTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Expand a collapsed group"); // TODO right namespace

		registerService(bc,expandGroupTaskFactory,TaskFactory.class, expandGroupTaskFactoryProps);

		// TODO: add to group...

		// TODO: remove from group...

		MapTableToNetworkTablesTaskFactoryImpl mapNetworkToTables = new MapTableToNetworkTablesTaskFactoryImpl(cyNetworkManagerServiceRef, tunableSetterServiceRef, rootNetworkManagerServiceRef);
		Properties mapNetworkToTablesProps = new Properties();
		registerService(bc, mapNetworkToTables, MapTableToNetworkTablesTaskFactory.class, mapNetworkToTablesProps);
		
		ImportTableDataTaskFactoryImpl importTableTaskFactory = new ImportTableDataTaskFactoryImpl(serviceRegistrar);
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
		createEdgeAttributeTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Create a new column for edges");
		registerService(bc,createEdgeAttributeTaskFactory,TaskFactory.class,createEdgeAttributeTaskFactoryProps);

		GetEdgeTaskFactory getEdgeTaskFactory = new GetEdgeTaskFactory(cyApplicationManagerServiceRef);
		Properties getEdgeTaskFactoryProps = new Properties();
		getEdgeTaskFactoryProps.setProperty(COMMAND, "get");
		getEdgeTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "edge");
		getEdgeTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Get an edge based on its name");
		registerService(bc,getEdgeTaskFactory,TaskFactory.class,getEdgeTaskFactoryProps);

		GetNetworkAttributeTaskFactory getEdgeAttributeTaskFactory = 
			new GetNetworkAttributeTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef, CyEdge.class);
		Properties getEdgeAttributeTaskFactoryProps = new Properties();
		getEdgeAttributeTaskFactoryProps.setProperty(COMMAND, "get attribute");
		getEdgeAttributeTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "edge");
		getEdgeAttributeTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Get the values from a column in a set of edges");
		registerService(bc,getEdgeAttributeTaskFactory,TaskFactory.class,getEdgeAttributeTaskFactoryProps);

		GetPropertiesTaskFactory getEdgePropertiesTaskFactory = 
			new GetPropertiesTaskFactory(cyApplicationManagerServiceRef, CyEdge.class, 
			                             cyNetworkViewManagerServiceRef, renderingEngineManagerServiceRef);
		Properties getEdgePropertiesTaskFactoryProps = new Properties();
		getEdgePropertiesTaskFactoryProps.setProperty(COMMAND, "get properties");
		getEdgePropertiesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "edge");
		getEdgePropertiesTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Get the visual properties for edges");
		registerService(bc,getEdgePropertiesTaskFactory,TaskFactory.class,getEdgePropertiesTaskFactoryProps);

		ListEdgesTaskFactory listEdges = new ListEdgesTaskFactory(cyApplicationManagerServiceRef);
		Properties listEdgesTaskFactoryProps = new Properties();
		listEdgesTaskFactoryProps.setProperty(COMMAND, "list");
		listEdgesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "edge");
		listEdgesTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "List edges");
		registerService(bc,listEdges,TaskFactory.class,listEdgesTaskFactoryProps);

		ListNetworkAttributesTaskFactory listEdgeAttributesTaskFactory = 
			new ListNetworkAttributesTaskFactory(cyApplicationManagerServiceRef, 
		                                cyTableManagerServiceRef, CyEdge.class);
		Properties listEdgeAttributesTaskFactoryProps = new Properties();
		listEdgeAttributesTaskFactoryProps.setProperty(COMMAND, "list attributes");
		listEdgeAttributesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "edge");
		listEdgeAttributesTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "List all of the columns for edges");
		registerService(bc,listEdgeAttributesTaskFactory,TaskFactory.class,listEdgeAttributesTaskFactoryProps);

		ListPropertiesTaskFactory listEdgeProperties = 
			new ListPropertiesTaskFactory(cyApplicationManagerServiceRef,
		                                CyEdge.class, cyNetworkViewManagerServiceRef,
		                                renderingEngineManagerServiceRef);
		Properties listEdgePropertiesTaskFactoryProps = new Properties();
		listEdgePropertiesTaskFactoryProps.setProperty(COMMAND, "list properties");
		listEdgePropertiesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "edge");
		listEdgePropertiesTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "List all of the visual properties for edges");
		registerService(bc,listEdgeProperties,TaskFactory.class,listEdgePropertiesTaskFactoryProps);

		RenameEdgeTaskFactory renameEdge = new RenameEdgeTaskFactory();
		Properties renameEdgeTaskFactoryProps = new Properties();
		renameEdgeTaskFactoryProps.setProperty(COMMAND, "rename");
		renameEdgeTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "edge");
		renameEdgeTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Rename an edge");
		registerService(bc,renameEdge,TaskFactory.class,renameEdgeTaskFactoryProps);

		SetNetworkAttributeTaskFactory setEdgeAttributeTaskFactory = 
			new SetNetworkAttributeTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef, CyEdge.class);
		Properties setEdgeAttributeTaskFactoryProps = new Properties();
		setEdgeAttributeTaskFactoryProps.setProperty(COMMAND, "set attribute");
		setEdgeAttributeTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "edge");
		setEdgeAttributeTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Change edge table values for an edge or set of edges");
		registerService(bc,setEdgeAttributeTaskFactory,TaskFactory.class,setEdgeAttributeTaskFactoryProps);

		SetPropertiesTaskFactory setEdgePropertiesTaskFactory = 
			new SetPropertiesTaskFactory(cyApplicationManagerServiceRef, CyEdge.class, 
		                               cyNetworkViewManagerServiceRef, renderingEngineManagerServiceRef);
		Properties setEdgePropertiesTaskFactoryProps = new Properties();
		setEdgePropertiesTaskFactoryProps.setProperty(COMMAND, "set properties");
		setEdgePropertiesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "edge");
		setEdgePropertiesTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Change visual properties for a set of edges");
		registerService(bc,setEdgePropertiesTaskFactory,TaskFactory.class,setEdgePropertiesTaskFactoryProps);

		// NAMESPACE: group
		{
			AddToGroupTaskFactory factory = new AddToGroupTaskFactory(serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(COMMAND, "add");
			props.setProperty(COMMAND_NAMESPACE, "group");
			props.setProperty(COMMAND_DESCRIPTION, "Add nodes or edges to a group");
			registerService(bc, factory, TaskFactory.class, props);
		}

		ListGroupsTaskFactory listGroupsTaskFactory = 
			new ListGroupsTaskFactory(cyApplicationManagerServiceRef, cyGroupManager);
		Properties listGroupsTFProps = new Properties();
		listGroupsTFProps.setProperty(COMMAND, "list");
		listGroupsTFProps.setProperty(COMMAND_NAMESPACE, "group");
		listGroupsTFProps.setProperty(COMMAND_DESCRIPTION, "List all of the groups in a network");
		registerService(bc,listGroupsTaskFactory,TaskFactory.class,listGroupsTFProps);

		{
			RemoveFromGroupTaskFactory factory = new RemoveFromGroupTaskFactory(serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(COMMAND, "remove");
			props.setProperty(COMMAND_NAMESPACE, "group");
			props.setProperty(COMMAND_DESCRIPTION, "Remove nodes or edges from a group");
			registerService(bc, factory, TaskFactory.class, props);
		}

		RenameGroupTaskFactory renameGroupTaskFactory = 
			new RenameGroupTaskFactory(cyApplicationManagerServiceRef, cyGroupManager);
		Properties renameGroupTFProps = new Properties();
		renameGroupTFProps.setProperty(COMMAND, "rename");
		renameGroupTFProps.setProperty(COMMAND_NAMESPACE, "group");
		renameGroupTFProps.setProperty(COMMAND_DESCRIPTION, "Rename a group");
		registerService(bc,renameGroupTaskFactory,TaskFactory.class,renameGroupTFProps);

		// NAMESPACE: layout
		GetPreferredLayoutTaskFactory getPreferredLayoutTaskFactory = 
			new GetPreferredLayoutTaskFactory(cyLayoutsServiceRef);
		Properties getPreferredTFProps = new Properties();
		getPreferredTFProps.setProperty(COMMAND, "get preferred");
		getPreferredTFProps.setProperty(COMMAND_NAMESPACE, "layout");
		getPreferredTFProps.setProperty(COMMAND_DESCRIPTION, "Return the current preferred layout");
		registerService(bc,getPreferredLayoutTaskFactory,TaskFactory.class,getPreferredTFProps);

		SetPreferredLayoutTaskFactory setPreferredLayoutTaskFactory =
				new SetPreferredLayoutTaskFactory(cyLayoutsServiceRef);
		Properties setPreferredTFProps = new Properties();
		setPreferredTFProps.setProperty(COMMAND, "set preferred");
		setPreferredTFProps.setProperty(COMMAND_NAMESPACE, "layout");
		setPreferredTFProps.setProperty(COMMAND_DESCRIPTION, "Set the preferred layout");
		registerService(bc,setPreferredLayoutTaskFactory,TaskFactory.class,setPreferredTFProps);


		// NAMESPACE: network
		AddTaskFactory addTaskFactory = new AddTaskFactory();
		Properties addTaskFactoryProps = new Properties();
		addTaskFactoryProps.setProperty(COMMAND, "add");
		addTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		addTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Add nodes and edges to a network (they must be in the current collection)");
		registerService(bc,addTaskFactory,TaskFactory.class,addTaskFactoryProps);

		AddEdgeTaskFactory addEdgeTaskFactory = new AddEdgeTaskFactory(visualMappingManagerServiceRef,
		                                                               cyNetworkViewManagerServiceRef, cyEventHelperRef);
		Properties addEdgeTaskFactoryProps = new Properties();
		addEdgeTaskFactoryProps.setProperty(COMMAND, "add edge");
		addEdgeTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		addEdgeTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Add an edge between two nodes");
		registerService(bc,addEdgeTaskFactory,TaskFactory.class,addEdgeTaskFactoryProps);

		AddNodeTaskFactory addNodeTaskFactory = new AddNodeTaskFactory(visualMappingManagerServiceRef,
		                                                               cyNetworkViewManagerServiceRef, cyEventHelperRef);
		Properties addNodeTaskFactoryProps = new Properties();
		addNodeTaskFactoryProps.setProperty(COMMAND, "add node");
		addNodeTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		addNodeTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Add a new node to a network");
		registerService(bc,addNodeTaskFactory,TaskFactory.class,addNodeTaskFactoryProps);

		CreateNetworkAttributeTaskFactory createNetworkAttributeTaskFactory = 
			new CreateNetworkAttributeTaskFactory(cyApplicationManagerServiceRef, 
		                                        cyTableManagerServiceRef, CyNetwork.class);
		Properties createNetworkAttributeTaskFactoryProps = new Properties();
		createNetworkAttributeTaskFactoryProps.setProperty(COMMAND, "create attribute");
		createNetworkAttributeTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		createNetworkAttributeTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Create a new column in the network table");
		registerService(bc,createNetworkAttributeTaskFactory,TaskFactory.class,createNetworkAttributeTaskFactoryProps);

		DeselectTaskFactory deselectTaskFactory = new DeselectTaskFactory(cyNetworkViewManagerServiceRef, cyEventHelperRef);
		Properties deselectTaskFactoryProps = new Properties();
		deselectTaskFactoryProps.setProperty(COMMAND, "deselect");
		deselectTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		deselectTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Deselect nodes or edges in a network");
		registerService(bc,deselectTaskFactory,TaskFactory.class,deselectTaskFactoryProps);

		GetNetworkTaskFactory getNetwork = new GetNetworkTaskFactory(cyApplicationManagerServiceRef);
		Properties getNetworkTaskFactoryProps = new Properties();
		getNetworkTaskFactoryProps.setProperty(COMMAND, "get");
		getNetworkTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		getNetworkTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Return a network");
		registerService(bc,getNetwork,TaskFactory.class,getNetworkTaskFactoryProps);

		GetNetworkAttributeTaskFactory getNetworkAttributeTaskFactory = 
			new GetNetworkAttributeTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef, CyNetwork.class);
		Properties getNetworkAttributeTaskFactoryProps = new Properties();
		getNetworkAttributeTaskFactoryProps.setProperty(COMMAND, "get attribute");
		getNetworkAttributeTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		getNetworkAttributeTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Get the value from a column for a network");
		registerService(bc,getNetworkAttributeTaskFactory,TaskFactory.class,getNetworkAttributeTaskFactoryProps);

		GetPropertiesTaskFactory getNetworkPropertiesTaskFactory = 
			new GetPropertiesTaskFactory(cyApplicationManagerServiceRef, CyNetwork.class,
		                               cyNetworkViewManagerServiceRef, renderingEngineManagerServiceRef);
		Properties getNetworkPropertiesTaskFactoryProps = new Properties();
		getNetworkPropertiesTaskFactoryProps.setProperty(COMMAND, "get properties");
		getNetworkPropertiesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		getNetworkPropertiesTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Get the visual property value for a network");
		registerService(bc,getNetworkPropertiesTaskFactory,TaskFactory.class,getNetworkPropertiesTaskFactoryProps);

		{
			HideCommandTaskFactory factory = new HideCommandTaskFactory(serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(COMMAND, "hide");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND_DESCRIPTION, "Hide nodes or edges in a network");
			registerService(bc, factory, TaskFactory.class, props);
		}

		ListNetworksTaskFactory listNetworks = new ListNetworksTaskFactory(cyNetworkManagerServiceRef);
		Properties listNetworksTaskFactoryProps = new Properties();
		listNetworksTaskFactoryProps.setProperty(COMMAND, "list");
		listNetworksTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		listNetworksTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "List all of the available networks");
		registerService(bc,listNetworks,TaskFactory.class,listNetworksTaskFactoryProps);

		ListNetworkAttributesTaskFactory listNetworkAttributesTaskFactory = 
			new ListNetworkAttributesTaskFactory(cyApplicationManagerServiceRef, 
		                                cyTableManagerServiceRef, CyNetwork.class);
		Properties listNetworkAttributesTaskFactoryProps = new Properties();
		listNetworkAttributesTaskFactoryProps.setProperty(COMMAND, "list attributes");
		listNetworkAttributesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		listNetworkAttributesTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "List all of the columns for networks");
		registerService(bc,listNetworkAttributesTaskFactory,TaskFactory.class,listNetworkAttributesTaskFactoryProps);

		ListPropertiesTaskFactory listNetworkProperties = 
			new ListPropertiesTaskFactory(cyApplicationManagerServiceRef,
		                                CyNetwork.class, cyNetworkViewManagerServiceRef,
		                                renderingEngineManagerServiceRef);
		Properties listNetworkPropertiesTaskFactoryProps = new Properties();
		listNetworkPropertiesTaskFactoryProps.setProperty(COMMAND, "list properties");
		listNetworkPropertiesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		listNetworkPropertiesTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "List all of the network visual properties");
		registerService(bc,listNetworkProperties,TaskFactory.class,listNetworkPropertiesTaskFactoryProps);

		SelectTaskFactory selectTaskFactory = new SelectTaskFactory(cyApplicationManagerServiceRef,
		                                                            cyNetworkViewManagerServiceRef, cyEventHelperRef); Properties selectTaskFactoryProps = new Properties();
		selectTaskFactoryProps.setProperty(COMMAND, "select");
		selectTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		selectTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Select nodes or edges in a network");
		registerService(bc,selectTaskFactory,TaskFactory.class,selectTaskFactoryProps);

		SetNetworkAttributeTaskFactory setNetworkAttributeTaskFactory = 
			new SetNetworkAttributeTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef, CyNetwork.class);
		Properties setNetworkAttributeTaskFactoryProps = new Properties();
		setNetworkAttributeTaskFactoryProps.setProperty(COMMAND, "set attribute");
		setNetworkAttributeTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		setNetworkAttributeTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Set a value in the network table");
		registerService(bc,setNetworkAttributeTaskFactory,TaskFactory.class,setNetworkAttributeTaskFactoryProps);

		SetCurrentNetworkTaskFactory setCurrentNetwork = new SetCurrentNetworkTaskFactory(cyApplicationManagerServiceRef);
		Properties setCurrentNetworkTaskFactoryProps = new Properties();
		setCurrentNetworkTaskFactoryProps.setProperty(COMMAND, "set current");
		setCurrentNetworkTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		setCurrentNetworkTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Set the current network");
		registerService(bc,setCurrentNetwork,TaskFactory.class,setCurrentNetworkTaskFactoryProps);

		SetPropertiesTaskFactory setNetworkPropertiesTaskFactory = 
			new SetPropertiesTaskFactory(cyApplicationManagerServiceRef, CyNetwork.class,
		                               cyNetworkViewManagerServiceRef, renderingEngineManagerServiceRef);
		Properties setNetworkPropertiesTaskFactoryProps = new Properties();
		setNetworkPropertiesTaskFactoryProps.setProperty(COMMAND, "set properties");
		setNetworkPropertiesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "network");
		setNetworkPropertiesTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Set network visual properties");
		registerService(bc,setNetworkPropertiesTaskFactory,TaskFactory.class,setNetworkPropertiesTaskFactoryProps);

		{
			UnHideCommandTaskFactory factory = new UnHideCommandTaskFactory(serviceRegistrar);
			Properties props = new Properties();
			props.setProperty(COMMAND, "show");
			props.setProperty(COMMAND_NAMESPACE, "network");
			props.setProperty(COMMAND_DESCRIPTION, "Show hidden nodes and edges");
			registerService(bc, factory, TaskFactory.class, props);
		}

		// NAMESPACE: node
		CreateNetworkAttributeTaskFactory createNodeAttributeTaskFactory = 
			new CreateNetworkAttributeTaskFactory(cyApplicationManagerServiceRef, 
		                                        cyTableManagerServiceRef, CyNode.class);
		Properties createNodeAttributeTaskFactoryProps = new Properties();
		createNodeAttributeTaskFactoryProps.setProperty(COMMAND, "create attribute");
		createNodeAttributeTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "node");
		createNodeAttributeTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Create a new column for nodes");
		registerService(bc,createNodeAttributeTaskFactory,TaskFactory.class,createNodeAttributeTaskFactoryProps);

		GetNodeTaskFactory getNodeTaskFactory = new GetNodeTaskFactory(cyApplicationManagerServiceRef);
		Properties getNodeTaskFactoryProps = new Properties();
		getNodeTaskFactoryProps.setProperty(COMMAND, "get");
		getNodeTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "node");
		getNodeTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Get a node from a network");
		registerService(bc,getNodeTaskFactory,TaskFactory.class,getNodeTaskFactoryProps);

		GetNetworkAttributeTaskFactory getNodeAttributeTaskFactory = 
			new GetNetworkAttributeTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef, CyNode.class);
		Properties getNodeAttributeTaskFactoryProps = new Properties();
		getNodeAttributeTaskFactoryProps.setProperty(COMMAND, "get attribute");
		getNodeAttributeTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "node");
		getNodeAttributeTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Get values from the node table");
		registerService(bc,getNodeAttributeTaskFactory,TaskFactory.class,getNodeAttributeTaskFactoryProps);

		GetPropertiesTaskFactory getNodePropertiesTaskFactory = 
			new GetPropertiesTaskFactory(cyApplicationManagerServiceRef, CyNode.class,
		                               cyNetworkViewManagerServiceRef, renderingEngineManagerServiceRef);
		Properties getNodePropertiesTaskFactoryProps = new Properties();
		getNodePropertiesTaskFactoryProps.setProperty(COMMAND, "get properties");
		getNodePropertiesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "node");
		getNodePropertiesTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Get visual properties for a node");
		registerService(bc,getNodePropertiesTaskFactory,TaskFactory.class,getNodePropertiesTaskFactoryProps);


		ListNodesTaskFactory listNodes = new ListNodesTaskFactory(cyApplicationManagerServiceRef);
		Properties listNodesTaskFactoryProps = new Properties();
		listNodesTaskFactoryProps.setProperty(COMMAND, "list");
		listNodesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "node");
		listNodesTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "List all of the nodes in a network");
		registerService(bc,listNodes,TaskFactory.class,listNodesTaskFactoryProps);

		ListNetworkAttributesTaskFactory listNodeAttributesTaskFactory = 
			new ListNetworkAttributesTaskFactory(cyApplicationManagerServiceRef, 
		                                cyTableManagerServiceRef, CyNode.class);
		Properties listNodeAttributesTaskFactoryProps = new Properties();
		listNodeAttributesTaskFactoryProps.setProperty(COMMAND, "list attributes");
		listNodeAttributesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "node");
		listNodeAttributesTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "List node columns");
		registerService(bc,listNodeAttributesTaskFactory,TaskFactory.class,listNodeAttributesTaskFactoryProps);

		ListPropertiesTaskFactory listNodeProperties = 
			new ListPropertiesTaskFactory(cyApplicationManagerServiceRef,
		                                CyNode.class, cyNetworkViewManagerServiceRef,
		                                renderingEngineManagerServiceRef);
		Properties listNodePropertiesTaskFactoryProps = new Properties();
		listNodePropertiesTaskFactoryProps.setProperty(COMMAND, "list properties");
		listNodePropertiesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "node");
		listNodePropertiesTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "List node visual properties");
		registerService(bc,listNodeProperties,TaskFactory.class,listNodePropertiesTaskFactoryProps);

		RenameNodeTaskFactory renameNode = new RenameNodeTaskFactory();
		Properties renameNodeTaskFactoryProps = new Properties();
		renameNodeTaskFactoryProps.setProperty(COMMAND, "rename");
		renameNodeTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "node");
		renameNodeTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Rename a node");
		registerService(bc,renameNode,TaskFactory.class,renameNodeTaskFactoryProps);

		SetNetworkAttributeTaskFactory setNodeAttributeTaskFactory = 
			new SetNetworkAttributeTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef, CyNode.class);
		Properties setNodeAttributeTaskFactoryProps = new Properties();
		setNodeAttributeTaskFactoryProps.setProperty(COMMAND, "set attribute");
		setNodeAttributeTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "node");
		setNodeAttributeTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, 
		                                             "Change node table values for a node or set of nodes");
		registerService(bc,setNodeAttributeTaskFactory,TaskFactory.class,setNodeAttributeTaskFactoryProps);

		SetPropertiesTaskFactory setNodePropertiesTaskFactory = 
			new SetPropertiesTaskFactory(cyApplicationManagerServiceRef, CyNode.class,
		                               cyNetworkViewManagerServiceRef, renderingEngineManagerServiceRef);
		Properties setNodePropertiesTaskFactoryProps = new Properties();
		setNodePropertiesTaskFactoryProps.setProperty(COMMAND, "set properties");
		setNodePropertiesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "node");
		setNodePropertiesTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Set node visual properties");
		registerService(bc,setNodePropertiesTaskFactory,TaskFactory.class,setNodePropertiesTaskFactoryProps);

		// NAMESPACE: table
		AddRowTaskFactory addRowTaskFactory = 
			new AddRowTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef); 
		Properties addRowTaskFactoryProps = new Properties();
		addRowTaskFactoryProps.setProperty(COMMAND, "add row");
		addRowTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "table");
		addRowTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Add a new row to a table");
		registerService(bc,addRowTaskFactory,TaskFactory.class,addRowTaskFactoryProps);

		CreateColumnTaskFactory createColumnTaskFactory = 
			new CreateColumnTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef); 
		Properties createColumnTaskFactoryProps = new Properties();
		createColumnTaskFactoryProps.setProperty(COMMAND, "create column");
		createColumnTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "table");
		createColumnTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Create a new column in a table");
		registerService(bc,createColumnTaskFactory,TaskFactory.class,createColumnTaskFactoryProps);

		CreateTableTaskFactory createTableTaskFactory = 
			new CreateTableTaskFactory(cyApplicationManagerServiceRef, 
			                           cyTableFactoryServiceRef, cyTableManagerServiceRef); 
		Properties createTableTaskFactoryProps = new Properties();
		createTableTaskFactoryProps.setProperty(COMMAND, "create table");
		createTableTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "table");
		createTableTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Create a new table");
		registerService(bc,createTableTaskFactory,TaskFactory.class,createTableTaskFactoryProps);

		DeleteColumnCommandTaskFactory deleteColumnCommandTaskFactory = 
			new DeleteColumnCommandTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef); 
		Properties deleteColumnCommandTaskFactoryProps = new Properties();
		deleteColumnCommandTaskFactoryProps.setProperty(COMMAND, "delete column");
		deleteColumnCommandTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "table");
		deleteColumnCommandTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Delete a column from a table");
		registerService(bc,deleteColumnCommandTaskFactory,TaskFactory.class,deleteColumnCommandTaskFactoryProps);

		DeleteRowTaskFactory deleteRowTaskFactory = 
			new DeleteRowTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef); 
		Properties deleteRowTaskFactoryProps = new Properties();
		deleteRowTaskFactoryProps.setProperty(COMMAND, "delete row");
		deleteRowTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "table");
		deleteRowTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Delete a row from a table");
		registerService(bc,deleteRowTaskFactory,TaskFactory.class,deleteRowTaskFactoryProps);

		DestroyTableTaskFactory destroyTableTaskFactory = 
			new DestroyTableTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef); 
		Properties destroyTableTaskFactoryProps = new Properties();
		destroyTableTaskFactoryProps.setProperty(COMMAND, "destroy");
		destroyTableTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "table");
		destroyTableTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Destroy (delete) an entire table");
		registerService(bc,destroyTableTaskFactory,TaskFactory.class,destroyTableTaskFactoryProps);

		GetColumnTaskFactory getColumnTaskFactory = 
			new GetColumnTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef); 
		Properties getColumnTaskFactoryProps = new Properties();
		getColumnTaskFactoryProps.setProperty(COMMAND, "get column");
		getColumnTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "table");
		getColumnTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Get the information about a table column");
		registerService(bc,getColumnTaskFactory,TaskFactory.class,getColumnTaskFactoryProps);

		GetRowTaskFactory getRowTaskFactory = 
			new GetRowTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef); 
		Properties getRowTaskFactoryProps = new Properties();
		getRowTaskFactoryProps.setProperty(COMMAND, "get row");
		getRowTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "table");
		getRowTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Return all values in a table row");
		registerService(bc,getRowTaskFactory,TaskFactory.class,getRowTaskFactoryProps);

		GetValueTaskFactory getValueTaskFactory = 
			new GetValueTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef); 
		Properties getValueTaskFactoryProps = new Properties();
		getValueTaskFactoryProps.setProperty(COMMAND, "get value");
		getValueTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "table");
		getValueTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Return a single value from a table");
		registerService(bc,getValueTaskFactory,TaskFactory.class,getValueTaskFactoryProps);

		ListColumnsTaskFactory listColumnsTaskFactory = 
			new ListColumnsTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef, 
			                          cyNetworkTableManagerServiceRef);
		Properties listColumnsTaskFactoryProps = new Properties();
		listColumnsTaskFactoryProps.setProperty(COMMAND, "list columns");
		listColumnsTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "table");
		listColumnsTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "List all of the columns in a table");
		registerService(bc,listColumnsTaskFactory,TaskFactory.class,listColumnsTaskFactoryProps);

		ListRowsTaskFactory listRowsTaskFactory = 
			new ListRowsTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef);
		Properties listRowsTaskFactoryProps = new Properties();
		listRowsTaskFactoryProps.setProperty(COMMAND, "list rows");
		listRowsTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "table");
		listRowsTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "List all of the rows in a table");
		registerService(bc,listRowsTaskFactory,TaskFactory.class,listRowsTaskFactoryProps);

		ListTablesTaskFactory listTablesTaskFactory = 
			new ListTablesTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef, 
			                          cyNetworkTableManagerServiceRef);
		Properties listTablesTaskFactoryProps = new Properties();
		listTablesTaskFactoryProps.setProperty(COMMAND, "list");
		listTablesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "table");
		listTablesTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "List all of the registered tables");
		registerService(bc,listTablesTaskFactory,TaskFactory.class,listTablesTaskFactoryProps);

		SetTableTitleTaskFactory setTableTitleTaskFactory = 
			new SetTableTitleTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef);
		Properties setTableTitleTaskFactoryProps = new Properties();
		setTableTitleTaskFactoryProps.setProperty(COMMAND, "set title");
		setTableTitleTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "table");
		setTableTitleTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Set the title of a table");
		registerService(bc,setTableTitleTaskFactory,TaskFactory.class,setTableTitleTaskFactoryProps);

		SetValuesTaskFactory setValuesTaskFactory = 
			new SetValuesTaskFactory(cyApplicationManagerServiceRef, cyTableManagerServiceRef);
		Properties setValuesTaskFactoryProps = new Properties();
		setValuesTaskFactoryProps.setProperty(COMMAND, "set values");
		setValuesTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "table");
		setValuesTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Set values in a table");
		registerService(bc,setValuesTaskFactory,TaskFactory.class,setValuesTaskFactoryProps);

		// NAMESPACE: view
		GetCurrentNetworkViewTaskFactory getCurrentView = 
			new GetCurrentNetworkViewTaskFactory(cyApplicationManagerServiceRef);
		Properties getCurrentViewTaskFactoryProps = new Properties();
		getCurrentViewTaskFactoryProps.setProperty(COMMAND, "get current");
		getCurrentViewTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "view");
		getCurrentViewTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Get the current view");
		registerService(bc,getCurrentView,TaskFactory.class,getCurrentViewTaskFactoryProps);

		ListNetworkViewsTaskFactory listNetworkViews = 
			new ListNetworkViewsTaskFactory(cyApplicationManagerServiceRef, cyNetworkViewManagerServiceRef);
		Properties listNetworkViewsTaskFactoryProps = new Properties();
		listNetworkViewsTaskFactoryProps.setProperty(COMMAND, "list");
		listNetworkViewsTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "view");
		listNetworkViewsTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "List all views");
		registerService(bc,listNetworkViews,TaskFactory.class,listNetworkViewsTaskFactoryProps);

		SetCurrentNetworkViewTaskFactory setCurrentView = 
			new SetCurrentNetworkViewTaskFactory(cyApplicationManagerServiceRef, 
			                                     cyNetworkViewManagerServiceRef);
		Properties setCurrentViewTaskFactoryProps = new Properties();
		setCurrentViewTaskFactoryProps.setProperty(COMMAND, "set current");
		setCurrentViewTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "view");
		setCurrentViewTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Set the current view");
		registerService(bc,setCurrentView,TaskFactory.class,setCurrentViewTaskFactoryProps);

		UpdateNetworkViewTaskFactory updateView = 
			new UpdateNetworkViewTaskFactory(cyApplicationManagerServiceRef, cyNetworkViewManagerServiceRef);
		Properties updateViewTaskFactoryProps = new Properties();
		updateViewTaskFactoryProps.setProperty(COMMAND, "update");
		updateViewTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "view");
		updateViewTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Update (repaint) a view");
		registerService(bc,updateView,TaskFactory.class,updateViewTaskFactoryProps);

		// New in 3.2.0: Export to HTML5 archive
		ExportAsWebArchiveTaskFactory exportAsWebArchiveTaskFactory = new ExportAsWebArchiveTaskFactory(cyNetworkManagerServiceRef);
		Properties exportAsWebArchiveTaskFactoryProps = new Properties();
		exportAsWebArchiveTaskFactoryProps.setProperty(PREFERRED_MENU,"File.Export");
		exportAsWebArchiveTaskFactoryProps.setProperty(ENABLE_FOR,ENABLE_FOR_NETWORK_AND_VIEW);
		exportAsWebArchiveTaskFactoryProps.setProperty(MENU_GRAVITY,"1.25");
		exportAsWebArchiveTaskFactoryProps.setProperty(TITLE,"Network View(s) as Web Page...");
		registerAllServices(bc, exportAsWebArchiveTaskFactory, exportAsWebArchiveTaskFactoryProps);
		registerServiceListener(bc, exportAsWebArchiveTaskFactory, "registerFactory", "unregisterFactory", CySessionWriterFactory.class);
	}
}
