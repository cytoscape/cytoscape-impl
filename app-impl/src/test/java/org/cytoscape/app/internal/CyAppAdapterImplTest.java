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
import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyVersion;
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
import org.cytoscape.io.write.*;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
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
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.properties.TunablePropertySerializerFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.PanelTaskManager;
import org.cytoscape.work.undo.UndoSupport;
import org.junit.Before;

import java.util.Properties;

import static org.mockito.Mockito.mock;

//import org.cytoscape.app.CyAppAdapterTest;

public class CyAppAdapterImplTest /*extends CyAppAdapterTest*/ {

	@Before
	public void setUp() {
		CyAppAdapter adapter = new CyAppAdapterImpl(
		    mock(CyApplicationConfiguration.class),
			mock(CyApplicationManager.class),
			mock(CyEventHelper.class),
			mock(CyGroupAggregationManager.class),
			mock(CyGroupFactory.class),
			mock(CyGroupManager.class),
			mock(CyLayoutAlgorithmManager.class),
			mock(CyNetworkFactory.class),
			mock(CyNetworkManager.class),
			mock(CyNetworkViewFactory.class),
			mock(CyNetworkViewManager.class),
			mock(CyNetworkReaderManager.class),
			mock(CyNetworkViewWriterManager.class),
			(CyProperty<Properties>)mock(CyProperty.class),
			mock(CyPropertyReaderManager.class),
			mock(CyPropertyWriterManager.class),
			mock(CyRootNetworkManager.class),
			mock(CyServiceRegistrar.class),
			mock(CySessionManager.class),
			mock(CySessionReaderManager.class),
			mock(CySessionWriterManager.class),
			mock(CySwingApplication.class),
			mock(CyTableFactory.class),
			mock(CyTableManager.class),
			mock(CyTableReaderManager.class),
			mock(CyTableWriterManager.class),
			mock(CyVersion.class),
			mock(DialogTaskManager.class),
			mock(PanelTaskManager.class),
			mock(PresentationWriterManager.class),
			mock(RenderingEngineManager.class),
			mock(TaskManager.class),
			mock(UndoSupport.class),
			mock(TunablePropertySerializerFactory.class),
			mock(VisualMappingFunctionFactory.class),
			mock(VisualMappingFunctionFactory.class),
			mock(VisualMappingFunctionFactory.class),
			mock(VisualMappingManager.class),
			mock(VisualStyleFactory.class),
			mock(DataSourceManager.class),
			
			// core-task services
			mock(LoadVizmapFileTaskFactory.class),
			mock(LoadNetworkFileTaskFactory.class),
			mock(LoadNetworkURLTaskFactory.class),
			mock(DeleteSelectedNodesAndEdgesTaskFactory.class),
			mock(SelectAllTaskFactory.class),
			mock(SelectAllEdgesTaskFactory.class),
			mock(SelectAllNodesTaskFactory.class),
			mock(SelectAdjacentEdgesTaskFactory.class),
			mock(SelectConnectedNodesTaskFactory.class),
			mock(SelectFirstNeighborsTaskFactory.class),
			mock(SelectFirstNeighborsTaskFactory.class),
			mock(SelectFirstNeighborsTaskFactory.class),
			mock(DeselectAllTaskFactory.class),
			mock(DeselectAllEdgesTaskFactory.class),
			mock(DeselectAllNodesTaskFactory.class),
			mock(InvertSelectedEdgesTaskFactory.class),
			mock(InvertSelectedNodesTaskFactory.class),
			mock(SelectFromFileListTaskFactory.class),
			mock(SelectFirstNeighborsNodeViewTaskFactory.class),
			mock(HideSelectedTaskFactory.class),
			mock(HideSelectedNodesTaskFactory.class),
			mock(HideSelectedEdgesTaskFactory.class),
			mock(UnHideAllTaskFactory.class),
			mock(UnHideAllNodesTaskFactory.class),
			mock(UnHideAllEdgesTaskFactory.class),
			mock(NewEmptyNetworkViewFactory.class),
			mock(CloneNetworkTaskFactory.class),
			mock(NewNetworkSelectedNodesAndEdgesTaskFactory.class),
			mock(NewNetworkSelectedNodesOnlyTaskFactory.class),
			mock(DestroyNetworkTaskFactory.class),
			mock(DestroyNetworkViewTaskFactory.class),
			mock(NewSessionTaskFactory.class),
			mock(OpenSessionTaskFactory.class),
			mock(SaveSessionAsTaskFactory.class),
			mock(EditNetworkTitleTaskFactory.class),
			mock(CreateNetworkViewTaskFactory.class),
			mock(ExportNetworkImageTaskFactory.class),
			mock(ExportNetworkViewTaskFactory.class),
			mock(ExportSelectedTableTaskFactory.class),
			mock(ExportTableTaskFactory.class),
			mock(ApplyPreferredLayoutTaskFactory.class),
			mock(DeleteColumnTaskFactory.class),
			mock(RenameColumnTaskFactory.class),
			mock(DeleteTableTaskFactory.class),
			mock(ExportVizmapTaskFactory.class),
			mock(ConnectSelectedNodesTaskFactory.class),
			mock(MapGlobalToLocalTableTaskFactory.class),
			mock(ApplyVisualStyleTaskFactory.class),
			mock(MapTableToNetworkTablesTaskFactory.class),
			mock(GroupNodesTaskFactory.class),
			mock(UnGroupTaskFactory.class),
			mock(CollapseGroupTaskFactory.class),
			mock(ExpandGroupTaskFactory.class),
			mock(UnGroupNodesTaskFactory.class),
			mock(CommandExecutorTaskFactory.class),
			mock(AvailableCommands.class)
		    );
	}	
}
