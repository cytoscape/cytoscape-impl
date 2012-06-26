package org.cytoscape.app.internal;


import java.util.Properties;
import org.cytoscape.app.AbstractCyApp;
import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.app.swing.CySwingAppAdapter;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyVersion;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedEvent;
import org.cytoscape.io.datasource.DataSourceManager;
import org.cytoscape.equations.AbstractFunction;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.group.data.CyGroupAggregationManager;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.io.read.CyPropertyReaderManager;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.io.read.CyTableReaderManager;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.io.webservice.NetworkImportWebServiceClient;
import org.cytoscape.io.webservice.client.AbstractWebServiceClient;
import org.cytoscape.io.webservice.events.DataImportFinishedEvent;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyNetworkViewWriterManager;
import org.cytoscape.io.write.CyPropertyWriterManager;
import org.cytoscape.io.write.CySessionWriterManager;
import org.cytoscape.io.write.PresentationWriterManager;
import org.cytoscape.io.write.CyTableWriterManager;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.AboutToRemoveEdgesEvent;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
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
import org.cytoscape.task.write.ExportSelectedTableTaskFactory;
import org.cytoscape.task.write.ExportTableTaskFactory;
import org.cytoscape.task.write.ExportNetworkImageTaskFactory;
import org.cytoscape.task.write.ExportNetworkViewTaskFactory;
import org.cytoscape.task.write.ExportVizmapTaskFactory;
import org.cytoscape.task.write.SaveSessionAsTaskFactory;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.events.AboutToRemoveEdgeViewsListener;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.events.RenderingEngineAboutToBeRemovedEvent;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.events.VisualStyleAboutToBeRemovedEvent;
import org.cytoscape.view.vizmap.gui.editor.AbstractVisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.event.LexiconStateChangedEvent;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.swing.AbstractGUITunableHandler;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.PanelTaskManager;
import org.cytoscape.work.undo.UndoSupport;
import org.cytoscape.work.util.BoundedDouble;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.work.TaskFactory;



/**
 * An implementation of CyAppAdapter
 */
public class CyAppAdapterImpl implements CySwingAppAdapter {

	//
	// Since there are a lot of fields, keep them
	// in alphabetical order to maintain sanity.
	// Always make the field name same as the class
	// name, but with a lower case first letter.
	//
	// NOTE that grep and sort are very useful!
	//
	private final CyApplicationManager cyApplicationManager;
	private final CyEventHelper cyEventHelper;
	private final CyGroupAggregationManager cyGroupAggregationManager;
	private final CyGroupFactory cyGroupFactory;
	private final CyGroupManager cyGroupManager;
	private final CyLayoutAlgorithmManager cyLayoutAlgorithmManager;
	private final CyNetworkFactory cyNetworkFactory;
	private final CyNetworkManager cyNetworkManager;
	private final CyNetworkViewFactory cyNetworkViewFactory;
	private final CyNetworkViewManager cyNetworkViewManager;
	private final CyNetworkReaderManager cyNetworkViewReaderManager;
	private final CyNetworkViewWriterManager cyNetworkViewWriterManager;
	private final CyProperty<Properties> cyProperty;
	private final CyPropertyReaderManager cyPropertyReaderManager;
	private final CyPropertyWriterManager cyPropertyWriterManager;
	private final CyRootNetworkManager cyRootNetworkFactory;
	private final CyServiceRegistrar cyServiceRegistrar;
	private final CySessionManager cySessionManager;
	private final CySessionReaderManager cySessionReaderManager;
	private final CyVersion cyVersion;
	private final CySessionWriterManager cySessionWriterManager;
	private final CySwingApplication cySwingApplication;
	private final CyTableFactory cyTableFactory;
	private final CyTableManager cyTableManager;
	private final CyTableReaderManager cyTableReaderManager;
	private final CyTableWriterManager cyTableWriterManager;
	private final DialogTaskManager dialogTaskManager;
	private final PanelTaskManager panelTaskManager;
	private final PresentationWriterManager presentationWriterManager;
	private final RenderingEngineManager renderingEngineManager;
	private final TaskManager taskManager;
	private final UndoSupport undoSupport;
	private final VisualMappingManager visualMappingManager;
	private final VisualStyleFactory visualStyleFactory;
	private final DataSourceManager dataSourceManager;

	//
	// The following fields are not actually used, but are here
	// to trick BND into importing the packages that contain
	// these objects.  This will pull the package into the classloader,
	// thus making it available to any app.  All packages in the
	// Cytoscape API should have at least one object in this class!!!
	//
	private NetworkTaskFactory networkTaskFactory;
	private SessionAboutToBeSavedEvent sessionAboutToBeSavedEvent;
	private VisualStyleAboutToBeRemovedEvent visualStyleAboutToBeRemovedEvent;
	private AboutToRemoveEdgeViewsListener aboutToRemoveEdgeViewsListener;
	private AboutToRemoveEdgesEvent aboutToRemoveEdgesEvent;
	private RenderingEngineAboutToBeRemovedEvent renderingEngineAboutToBeRemovedEvent;
	private VisualLexicon abstractVisualLexicon;
	private SimpleCyProperty basicCyProperty;
	private BookmarksUtil bookmarksUtil;
	private NewEmptyNetworkViewFactory newEmptyNetworkViewFactory;
	private Task task;
	private BoundedDouble boundedDouble;
	private CyAction cyAction;
	private CytoPanelComponentSelectedEvent cytoPanelComponentSelectedEvent;
	private CyFileFilter cyFileFilter;
	private CyNetworkReader cyNetworkViewReader;
	private StreamUtil streamUtil;
	private CyNetworkViewWriterFactory cyNetworkViewWriterFactory;
	private AbstractGUITunableHandler abstractGUITunableHandler;
	private AbstractLayoutAlgorithm abstractLayout;
	private AbstractFunction abstractFunction;
	private AbstractVisualPropertyEditor abstractVisualPropertyEditor;
	private LexiconStateChangedEvent lexiconStateChangedEvent;
	private NetworkImportWebServiceClient networkImportWebServiceClient;
	private AbstractWebServiceClient abstractWebServiceClient;
	private DataImportFinishedEvent dataImportFinishedEvent;
	private AbstractCyApp cyApp;
	private BasicVisualLexicon basicVisualLexicon; 
	private final VisualMappingFunctionFactory visualMappingFunctionContinuousFactory;
	private final VisualMappingFunctionFactory visualMappingFunctionDiscreteFactory;
	private final VisualMappingFunctionFactory visualMappingFunctionPassthroughFactory;

/// from core-task api
	private LoadVizmapFileTaskFactory loadVizmapFileTaskFactory;
	private LoadNetworkFileTaskFactory loadNetworkFileTaskFactory;
	private LoadNetworkURLTaskFactory loadNetworkURLTaskFactory;
	private DeleteSelectedNodesAndEdgesTaskFactory deleteSelectedNodesAndEdgesTaskFactory;
	private SelectAllTaskFactory selectAllTaskFactory;

	private SelectAllEdgesTaskFactory selectAllEdgesTaskFactory;
	private SelectAllNodesTaskFactory selectAllNodesTaskFactory;
	private SelectAdjacentEdgesTaskFactory selectAdjacentEdgesTaskFactory;
	private SelectConnectedNodesTaskFactory selectConnectedNodesTaskFactory;
	
	private SelectFirstNeighborsTaskFactory selectFirstNeighborsTaskFactory;
	private SelectFirstNeighborsTaskFactory selectFirstNeighborsTaskFactoryInEdge;
	private SelectFirstNeighborsTaskFactory selectFirstNeighborsTaskFactoryOutEdge;
	
	private DeselectAllTaskFactory deselectAllTaskFactory;
	private DeselectAllEdgesTaskFactory deselectAllEdgesTaskFactory;
	private DeselectAllNodesTaskFactory deselectAllNodesTaskFactory;
	private InvertSelectedEdgesTaskFactory invertSelectedEdgesTaskFactory;
	private InvertSelectedNodesTaskFactory invertSelectedNodesTaskFactory;
	private SelectFromFileListTaskFactory selectFromFileListTaskFactory;
	
	private SelectFirstNeighborsNodeViewTaskFactory selectFirstNeighborsNodeViewTaskFactory;
	
	private HideSelectedTaskFactory hideSelectedTaskFactory;
	private HideSelectedNodesTaskFactory hideSelectedNodesTaskFactory;
	private HideSelectedEdgesTaskFactory hideSelectedEdgesTaskFactory;
	private UnHideAllTaskFactory unHideAllTaskFactory;
	private UnHideAllNodesTaskFactory unHideAllNodesTaskFactory;
	private UnHideAllEdgesTaskFactory unHideAllEdgesTaskFactory;

	private NewEmptyNetworkViewFactory newEmptyNetworkTaskFactory;

	private CloneNetworkTaskFactory cloneNetworkTaskFactory;
	private NewNetworkSelectedNodesAndEdgesTaskFatory newNetworkSelectedNodesEdgesTaskFactory;
	private NewNetworkSelectedNodesOnlyTaskFactory newNetworkSelectedNodesOnlyTaskFactory;
	private DestroyNetworkTaskFactory destroyNetworkTaskFactory;
	private DestroyNetworkViewTaskFactory destroyNetworkViewTaskFactory;

	private NewSessionTaskFactory newSessionTaskFactory;
	private OpenSessionTaskFactory openSessionTaskFactory;
	private SaveSessionAsTaskFactory saveSessionAsTaskFactory;
	private EditNetworkTitleTaskFactory editNetworkTitleTaskFactory;
	private CreateNetworkViewTaskFactory createNetworkViewTaskFactory;
	private ExportNetworkImageTaskFactory exportNetworkImageTaskFactory;
	private ExportNetworkViewTaskFactory exportNetworkViewTaskFactory;
	private ExportSelectedTableTaskFactory exportSelectedTableTaskFactory;
	private ExportTableTaskFactory exportTableTaskFactory;
	private ApplyPreferredLayoutTaskFactory applyPreferredLayoutTaskFactory;
	private DeleteColumnTaskFactory deleteColumnTaskFactory;
	private RenameColumnTaskFactory renameColumnTaskFactory;
	private DeleteTableTaskFactory deleteTableTaskFactory;
	private ExportVizmapTaskFactory exportVizmapTaskFactory;
	
	private ConnectSelectedNodesTaskFactory connectSelectedNodesTaskFactory;
	private MapGlobalToLocalTableTaskFactory mapGlobal;
	private ApplyVisualStyleTaskFactory applyVisualStyleTaskFactory;
	private MapTableToNetworkTablesTaskFactory mapNetworkAttrTaskFactory;

	private GroupNodesTaskFactory groupNodesTaskFactory;
	private UnGroupTaskFactory unGroupTaskFactory;
	private CollapseGroupTaskFactory collapseGroupTaskFactory;
	private ExpandGroupTaskFactory expandGroupTaskFactory;
	private UnGroupNodesTaskFactory unGroupNodesTaskFactory;

	private LoadTableFileTaskFactory loadAttributesFileTaskFactory;
	private LoadTableURLTaskFactory loadAttributesURLTaskFactory;
	
	//
	// Since this is implementation code, there shouldn't be a
	// a problem adding new arguments as needed.  Therefore, to
	// maintain sanity, keep the arguments in alphabetical order.
	//
	CyAppAdapterImpl( final CyApplicationManager cyApplicationManager,
	                     final CyEventHelper cyEventHelper,
	                     final CyGroupAggregationManager cyGroupAggregationManager,
	                     final CyGroupFactory cyGroupFactory,
	                     final CyGroupManager cyGroupManager,
	                     final CyLayoutAlgorithmManager cyLayoutAlgorithmManager,
	                     final CyNetworkFactory cyNetworkFactory,
	                     final CyNetworkManager cyNetworkManager,
	                     final CyNetworkViewFactory cyNetworkViewFactory,
	                     final CyNetworkViewManager cyNetworkViewManager,
	                     final CyNetworkReaderManager cyNetworkViewReaderManager,
	                     final CyNetworkViewWriterManager cyNetworkViewWriterManager,
	                     final CyProperty<Properties> cyProperty,
	                     final CyPropertyReaderManager cyPropertyReaderManager,
	                     final CyPropertyWriterManager cyPropertyWriterManager,
	                     final CyRootNetworkManager cyRootNetworkFactory,
	                     final CyServiceRegistrar cyServiceRegistrar,
	                     final CySessionManager cySessionManager,
	                     final CySessionReaderManager cySessionReaderManager,
	                     final CySessionWriterManager cySessionWriterManager,
	                     final CySwingApplication cySwingApplication,
	                     final CyTableFactory cyTableFactory,
	                     final CyTableManager cyTableManager,
	                     final CyTableReaderManager cyTableReaderManager,
	                     final CyTableWriterManager cyTableWriterManager,
	                     final CyVersion cyVersion,
	                     final DialogTaskManager dialogTaskManager,
	                     final PanelTaskManager panelTaskManager,
	                     final PresentationWriterManager presentationWriterManager,
	                     final RenderingEngineManager renderingEngineManager,
	                     final TaskManager taskManager,
	                     final UndoSupport undoSupport,
	                     final VisualMappingFunctionFactory visualMappingFunctionContinuousFactory,
	                     final VisualMappingFunctionFactory visualMappingFunctionDiscreteFactory,
	                     final VisualMappingFunctionFactory visualMappingFunctionPassthroughFactory,
	                     final VisualMappingManager visualMappingManager,
	                     final VisualStyleFactory visualStyleFactory,
	                     final DataSourceManager dataSourceManager,
	                     
	                     // new from core-task-api
	                     
	                 	final LoadVizmapFileTaskFactory loadVizmapFileTaskFactory,
	                	final LoadNetworkFileTaskFactory loadNetworkFileTaskFactory,
	                	final LoadNetworkURLTaskFactory loadNetworkURLTaskFactory,
	                	final DeleteSelectedNodesAndEdgesTaskFactory deleteSelectedNodesAndEdgesTaskFactory,
	                	final SelectAllTaskFactory selectAllTaskFactory,

	                	final SelectAllEdgesTaskFactory selectAllEdgesTaskFactory,
	                	final SelectAllNodesTaskFactory selectAllNodesTaskFactory,
	                	final SelectAdjacentEdgesTaskFactory selectAdjacentEdgesTaskFactory,
	                	final SelectConnectedNodesTaskFactory selectConnectedNodesTaskFactory,
	                	
	                	final SelectFirstNeighborsTaskFactory selectFirstNeighborsTaskFactory,
	                	final SelectFirstNeighborsTaskFactory selectFirstNeighborsTaskFactoryInEdge,
	                	final SelectFirstNeighborsTaskFactory selectFirstNeighborsTaskFactoryOutEdge,
	                	
	                	final DeselectAllTaskFactory deselectAllTaskFactory,
	                	final DeselectAllEdgesTaskFactory deselectAllEdgesTaskFactory,
	                	final DeselectAllNodesTaskFactory deselectAllNodesTaskFactory,
	                	final InvertSelectedEdgesTaskFactory invertSelectedEdgesTaskFactory,
	                	final InvertSelectedNodesTaskFactory invertSelectedNodesTaskFactory,
	                	final SelectFromFileListTaskFactory selectFromFileListTaskFactory,
	                	
	                	final SelectFirstNeighborsNodeViewTaskFactory selectFirstNeighborsNodeViewTaskFactory,
	                	
	                	final HideSelectedTaskFactory hideSelectedTaskFactory,
	                	final HideSelectedNodesTaskFactory hideSelectedNodesTaskFactory,
	                	final HideSelectedEdgesTaskFactory hideSelectedEdgesTaskFactory,
	                	final UnHideAllTaskFactory unHideAllTaskFactory,
	                	final UnHideAllNodesTaskFactory unHideAllNodesTaskFactory,
	                	final UnHideAllEdgesTaskFactory unHideAllEdgesTaskFactory,

	                	final NewEmptyNetworkViewFactory newEmptyNetworkTaskFactory,

	                	final CloneNetworkTaskFactory cloneNetworkTaskFactory,
	                	final NewNetworkSelectedNodesAndEdgesTaskFatory newNetworkSelectedNodesEdgesTaskFactory,
	                	final NewNetworkSelectedNodesOnlyTaskFactory newNetworkSelectedNodesOnlyTaskFactory,
	                	final DestroyNetworkTaskFactory destroyNetworkTaskFactory,
	                	final DestroyNetworkViewTaskFactory destroyNetworkViewTaskFactory,
	                	final NewSessionTaskFactory newSessionTaskFactory,
	                	final OpenSessionTaskFactory openSessionTaskFactory,
	                	final SaveSessionAsTaskFactory saveSessionAsTaskFactory,
	                	final EditNetworkTitleTaskFactory editNetworkTitleTaskFactory,
	                	final CreateNetworkViewTaskFactory createNetworkViewTaskFactory,
	                	final ExportNetworkImageTaskFactory exportNetworkImageTaskFactory,
	                	final ExportNetworkViewTaskFactory exportNetworkViewTaskFactory,
	                	final ExportSelectedTableTaskFactory exportSelectedTableTaskFactory,
	                	final ExportTableTaskFactory exportTableTaskFactory,
	                	final ApplyPreferredLayoutTaskFactory applyPreferredLayoutTaskFactory,
	                	final DeleteColumnTaskFactory deleteColumnTaskFactory,
	                	final RenameColumnTaskFactory renameColumnTaskFactory,
	                	final DeleteTableTaskFactory deleteTableTaskFactory,
	                	final ExportVizmapTaskFactory exportVizmapTaskFactory,

	                	final ConnectSelectedNodesTaskFactory connectSelectedNodesTaskFactory,
	                	
	                	final MapGlobalToLocalTableTaskFactory mapGlobal,
	                	
	                	final ApplyVisualStyleTaskFactory applyVisualStyleTaskFactory,
	                	final MapTableToNetworkTablesTaskFactory mapNetworkAttrTaskFactory,
	                	
	                	final GroupNodesTaskFactory groupNodesTaskFactory,
	                	final UnGroupTaskFactory unGroupTaskFactory,
	                	final CollapseGroupTaskFactory collapseGroupTaskFactory,
	                	final ExpandGroupTaskFactory expandGroupTaskFactory,	
	                	final UnGroupNodesTaskFactory unGroupNodesTaskFactory
					    )
	{		
		this.cyApplicationManager = cyApplicationManager;
		this.cyEventHelper = cyEventHelper;
		this.cyGroupAggregationManager = cyGroupAggregationManager;
		this.cyGroupFactory = cyGroupFactory;
		this.cyGroupManager = cyGroupManager;
		this.cyLayoutAlgorithmManager = cyLayoutAlgorithmManager;
		this.cyNetworkFactory = cyNetworkFactory;
		this.cyNetworkManager = cyNetworkManager;
		this.cyNetworkViewFactory = cyNetworkViewFactory;
		this.cyNetworkViewManager = cyNetworkViewManager;
		this.cyNetworkViewReaderManager = cyNetworkViewReaderManager;
		this.cyNetworkViewWriterManager = cyNetworkViewWriterManager;
		this.cyProperty = cyProperty;
		this.cyPropertyReaderManager = cyPropertyReaderManager;
		this.cyPropertyWriterManager = cyPropertyWriterManager;
		this.cyRootNetworkFactory = cyRootNetworkFactory;
		this.cyServiceRegistrar = cyServiceRegistrar;
		this.cySessionManager = cySessionManager;
		this.cySessionReaderManager = cySessionReaderManager;
		this.cySessionWriterManager = cySessionWriterManager;
		this.cySwingApplication = cySwingApplication;
		this.cyTableFactory = cyTableFactory;
		this.cyTableManager = cyTableManager;
		this.cyTableReaderManager = cyTableReaderManager;
		this.cyTableWriterManager = cyTableWriterManager;
		this.cyVersion = cyVersion;
		this.dialogTaskManager = dialogTaskManager;
		this.panelTaskManager = panelTaskManager;
		this.presentationWriterManager = presentationWriterManager;
		this.renderingEngineManager = renderingEngineManager;
		this.taskManager = taskManager;
		this.undoSupport = undoSupport;
		this.visualMappingFunctionContinuousFactory = visualMappingFunctionContinuousFactory;
		this.visualMappingFunctionDiscreteFactory = visualMappingFunctionDiscreteFactory;
		this.visualMappingFunctionPassthroughFactory = visualMappingFunctionPassthroughFactory;
		this.visualMappingManager = visualMappingManager;
		this.visualStyleFactory = visualStyleFactory;
		this.dataSourceManager = dataSourceManager;
		
		//
		this.loadVizmapFileTaskFactory = loadVizmapFileTaskFactory;
		this.loadNetworkFileTaskFactory = loadNetworkFileTaskFactory;
		this.loadNetworkURLTaskFactory = loadNetworkURLTaskFactory;
		this.deleteSelectedNodesAndEdgesTaskFactory = deleteSelectedNodesAndEdgesTaskFactory;
		this.selectAllTaskFactory = selectAllTaskFactory;
		this.selectAllEdgesTaskFactory = selectAllEdgesTaskFactory;
		this.selectAllNodesTaskFactory = selectAllNodesTaskFactory;
		this.selectAdjacentEdgesTaskFactory = selectAdjacentEdgesTaskFactory;
		this.selectConnectedNodesTaskFactory = selectConnectedNodesTaskFactory;
		this.selectFirstNeighborsTaskFactory = selectFirstNeighborsTaskFactory;
		this.selectFirstNeighborsTaskFactoryInEdge = selectFirstNeighborsTaskFactoryInEdge;
		this.selectFirstNeighborsTaskFactoryOutEdge = selectFirstNeighborsTaskFactoryOutEdge;
		this.deselectAllTaskFactory = deselectAllTaskFactory;
		this.deselectAllEdgesTaskFactory = deselectAllEdgesTaskFactory;
		this.deselectAllNodesTaskFactory = deselectAllNodesTaskFactory;
		this.invertSelectedEdgesTaskFactory = invertSelectedEdgesTaskFactory;
		this.invertSelectedNodesTaskFactory = invertSelectedNodesTaskFactory;
		this.selectFromFileListTaskFactory = selectFromFileListTaskFactory;
		this.selectFirstNeighborsNodeViewTaskFactory = selectFirstNeighborsNodeViewTaskFactory;
		this.hideSelectedTaskFactory = hideSelectedTaskFactory;
		this.hideSelectedNodesTaskFactory = hideSelectedNodesTaskFactory;
		this.hideSelectedEdgesTaskFactory = hideSelectedEdgesTaskFactory;
		this.unHideAllTaskFactory = unHideAllTaskFactory;
		this.unHideAllNodesTaskFactory = unHideAllNodesTaskFactory;
		this.unHideAllEdgesTaskFactory = unHideAllEdgesTaskFactory;
		this.newEmptyNetworkTaskFactory = newEmptyNetworkTaskFactory;
		this.cloneNetworkTaskFactory = cloneNetworkTaskFactory;
		this.newNetworkSelectedNodesEdgesTaskFactory = newNetworkSelectedNodesEdgesTaskFactory;
		this.newNetworkSelectedNodesOnlyTaskFactory = newNetworkSelectedNodesOnlyTaskFactory;
		this.destroyNetworkTaskFactory = destroyNetworkTaskFactory;
		this.destroyNetworkViewTaskFactory = destroyNetworkViewTaskFactory;
		this.newSessionTaskFactory = newSessionTaskFactory;
		this.openSessionTaskFactory = openSessionTaskFactory;
		this.saveSessionAsTaskFactory = saveSessionAsTaskFactory;
		this.editNetworkTitleTaskFactory = editNetworkTitleTaskFactory;
		this.createNetworkViewTaskFactory = createNetworkViewTaskFactory;
		this.exportNetworkImageTaskFactory = exportNetworkImageTaskFactory;
		this.exportNetworkViewTaskFactory = exportNetworkViewTaskFactory;
		this.exportSelectedTableTaskFactory = exportSelectedTableTaskFactory;
		this.exportTableTaskFactory = exportTableTaskFactory;
		this.applyPreferredLayoutTaskFactory = applyPreferredLayoutTaskFactory;
		this.deleteColumnTaskFactory = deleteColumnTaskFactory;
		this.renameColumnTaskFactory = renameColumnTaskFactory;
		this.deleteTableTaskFactory = deleteTableTaskFactory;
		this.exportVizmapTaskFactory = exportVizmapTaskFactory;
		this.connectSelectedNodesTaskFactory = connectSelectedNodesTaskFactory;
		this.mapGlobal = mapGlobal;
		this.applyVisualStyleTaskFactory = applyVisualStyleTaskFactory;
		this.mapNetworkAttrTaskFactory = mapNetworkAttrTaskFactory;
		
		this.groupNodesTaskFactory = groupNodesTaskFactory;
		this.unGroupNodesTaskFactory = unGroupNodesTaskFactory;
		this.collapseGroupTaskFactory = collapseGroupTaskFactory;
		this.expandGroupTaskFactory = expandGroupTaskFactory;
		this.unGroupNodesTaskFactory = unGroupNodesTaskFactory;

	}

	//
	// May as well keep the methods alphabetical too!
	// 
	public CyApplicationManager getCyApplicationManager() { return cyApplicationManager; }
	public CyEventHelper getCyEventHelper() { return cyEventHelper; } 
	@Override public CyGroupAggregationManager getCyGroupAggregationManager() { return cyGroupAggregationManager; } 
	@Override public CyGroupFactory getCyGroupFactory() { return cyGroupFactory; } 
	@Override public CyGroupManager getCyGroupManager() { return cyGroupManager; } 
	public CyLayoutAlgorithmManager getCyLayoutAlgorithmManager() { return cyLayoutAlgorithmManager; } 
	public CyNetworkFactory getCyNetworkFactory() { return cyNetworkFactory; }
	public CyNetworkManager getCyNetworkManager() { return cyNetworkManager; } 
	public CyNetworkViewFactory getCyNetworkViewFactory() { return cyNetworkViewFactory; }
	public CyNetworkViewManager getCyNetworkViewManager() { return cyNetworkViewManager; }
	public CyNetworkReaderManager getCyNetworkViewReaderManager() { return cyNetworkViewReaderManager; }
	public CyNetworkViewWriterManager getCyNetworkViewWriterManager() { return cyNetworkViewWriterManager; }
	public CyProperty<Properties> getCoreProperties() { return cyProperty; }
	public CyPropertyReaderManager getCyPropertyReaderManager() { return cyPropertyReaderManager; }
	public CyPropertyWriterManager getCyPropertyWriterManager() { return cyPropertyWriterManager; }
	public CyRootNetworkManager getCyRootNetworkManager() { return cyRootNetworkFactory; } 
	public CyServiceRegistrar getCyServiceRegistrar() { return cyServiceRegistrar; }
	public CySessionManager getCySessionManager() { return cySessionManager; } 
	public CySessionReaderManager getCySessionReaderManager() { return cySessionReaderManager; }
	public CySessionWriterManager getCySessionWriterManager() { return cySessionWriterManager; }
	public CySwingApplication getCySwingApplication() { return cySwingApplication; }
	public CyTableFactory getCyTableFactory() { return cyTableFactory; } 
	public CyTableManager getCyTableManager() { return cyTableManager; }
	public CyTableReaderManager getCyTableReaderManager() { return cyTableReaderManager; }
	public CyTableWriterManager getCyTableWriterManager() { return cyTableWriterManager; }
	public CyVersion getCyVersion() { return cyVersion; }
	public DialogTaskManager getDialogTaskManager() { return dialogTaskManager; }
	public PanelTaskManager getPanelTaskManager() { return panelTaskManager; }
	public PresentationWriterManager getPresentationWriterManager() { return presentationWriterManager; }
	public RenderingEngineManager getRenderingEngineManager() { return renderingEngineManager; }
	public StreamUtil getStreamUtil(){ return streamUtil;}
	public TaskManager getTaskManager() { return taskManager; }
	public UndoSupport getUndoSupport() { return undoSupport; }
	@Override public VisualMappingFunctionFactory getVisualMappingFunctionContinuousFactory() { return visualMappingFunctionContinuousFactory; }
	@Override public VisualMappingFunctionFactory getVisualMappingFunctionDiscreteFactory() { return visualMappingFunctionDiscreteFactory; }
	@Override public VisualMappingFunctionFactory getVisualMappingFunctionPassthroughFactory() { return visualMappingFunctionPassthroughFactory; }
	public VisualMappingManager getVisualMappingManager() { return visualMappingManager; }
	public VisualStyleFactory getVisualStyleFactory() { return visualStyleFactory; }
	@Override public DataSourceManager getDataSourceManager() { return dataSourceManager; }
	
	////////////////// core-task services //////////////////////
	
	@Override public ApplyVisualStyleTaskFactory get_ApplyVisualStyleTaskFactory(){ return this.applyVisualStyleTaskFactory;}
	@Override public MapGlobalToLocalTableTaskFactory get_MapGlobalToLocalTableTaskFactory(){ return this.mapGlobal; }
	@Override public LoadNetworkFileTaskFactory get_LoadNetworkFileTaskFactory(){ return this.loadNetworkFileTaskFactory; }
	@Override public LoadNetworkURLTaskFactory get_LoadNetworkURLTaskFactory(){ return this.loadNetworkURLTaskFactory;	}
	@Override public LoadVizmapFileTaskFactory get_LoadVizmapFileTaskFactory(){ return this.loadVizmapFileTaskFactory; }
	@Override public LoadTableFileTaskFactory get_LoadTableFileTaskFactory(){ return this.loadAttributesFileTaskFactory; }
	@Override public LoadTableURLTaskFactory get_LoadTableURLTaskFactory(){ return this.loadAttributesURLTaskFactory; }
	@Override public DeleteSelectedNodesAndEdgesTaskFactory get_DeleteSelectedNodesAndEdgesTaskFactory(){ return this.deleteSelectedNodesAndEdgesTaskFactory; }
	@Override public SelectAllTaskFactory get_SelectAllTaskFactory(){ return this.selectAllTaskFactory;	}
	@Override public SelectAllEdgesTaskFactory get_SelectAllEdgesTaskFactory(){ return this.selectAllEdgesTaskFactory;	}
	@Override public SelectAllNodesTaskFactory get_SelectAllNodesTaskFactory(){	return this.selectAllNodesTaskFactory; }
	@Override public SelectAdjacentEdgesTaskFactory get_SelectAdjacentEdgesTaskFactory(){ return this.selectAdjacentEdgesTaskFactory;}
	@Override public SelectConnectedNodesTaskFactory get_SelectConnectedNodesTaskFactory(){ return this.selectConnectedNodesTaskFactory; }
	@Override public SelectFirstNeighborsTaskFactory get_SelectFirstNeighborsTaskFactory(){ return this.selectFirstNeighborsTaskFactory; }
	@Override public SelectFirstNeighborsTaskFactory get_SelectFirstNeighborsTaskFactoryInEdge(){ return this.selectFirstNeighborsTaskFactoryInEdge; }
	@Override public SelectFirstNeighborsTaskFactory get_SelectFirstNeighborsTaskFactoryOutEdge(){ return this.selectFirstNeighborsTaskFactoryOutEdge;}
	@Override public DeselectAllTaskFactory get_DeselectAllTaskFactory(){ return this.deselectAllTaskFactory; }
	@Override public DeselectAllEdgesTaskFactory get_DeselectAllEdgesTaskFactory(){ return this.deselectAllEdgesTaskFactory; }
	@Override public DeselectAllNodesTaskFactory get_DeselectAllNodesTaskFactory(){ return this.deselectAllNodesTaskFactory; }
	@Override public InvertSelectedEdgesTaskFactory get_InvertSelectedEdgesTaskFactory(){ return this.invertSelectedEdgesTaskFactory; }
	@Override public InvertSelectedNodesTaskFactory get_InvertSelectedNodesTaskFactory(){ return this.invertSelectedNodesTaskFactory;}
	@Override public SelectFromFileListTaskFactory get_SelectFromFileListTaskFactory(){ return this.selectFromFileListTaskFactory; }
	@Override public SelectFirstNeighborsNodeViewTaskFactory get_SelectFirstNeighborsNodeViewTaskFactory(){ return this.selectFirstNeighborsNodeViewTaskFactory; }
	@Override public HideSelectedTaskFactory get_HideSelectedTaskFactory(){ return this.hideSelectedTaskFactory; }
	@Override public HideSelectedNodesTaskFactory get_HideSelectedNodesTaskFactory(){ return this.hideSelectedNodesTaskFactory;	}
	@Override public HideSelectedEdgesTaskFactory get_HideSelectedEdgesTaskFactory(){ return this.hideSelectedEdgesTaskFactory; }
	@Override public UnHideAllTaskFactory get_UnHideAllTaskFactory(){ return this.unHideAllTaskFactory; }
	@Override public UnHideAllNodesTaskFactory get_UnHideAllNodesTaskFactory(){ return this.unHideAllNodesTaskFactory;}
	@Override public UnHideAllEdgesTaskFactory get_UnHideAllEdgesTaskFactory(){ return this.unHideAllEdgesTaskFactory;	}
	@Override public NewEmptyNetworkViewFactory get_NewEmptyNetworkViewFactory(){ return this.newEmptyNetworkViewFactory; }
	@Override public NewNetworkSelectedNodesAndEdgesTaskFatory get_NewNetworkSelectedNodesAndEdgesTaskFatory(){ return this.newNetworkSelectedNodesEdgesTaskFactory; }
	@Override public NewNetworkSelectedNodesOnlyTaskFactory get_NewNetworkSelectedNodesOnlyTaskFactory(){ return this.newNetworkSelectedNodesOnlyTaskFactory; }
	@Override public CloneNetworkTaskFactory get_CloneNetworkTaskFactory(){ return this.cloneNetworkTaskFactory; }
	@Override public DestroyNetworkTaskFactory get_DestroyNetworkTaskFactory(){ return this.destroyNetworkTaskFactory;	}
	@Override public DestroyNetworkViewTaskFactory get_DestroyNetworkViewTaskFactory(){ return this.destroyNetworkViewTaskFactory; }
	@Override public EditNetworkTitleTaskFactory get_EditNetworkTitleTaskFactory(){	return this.editNetworkTitleTaskFactory; }
	@Override public CreateNetworkViewTaskFactory get_CreateNetworkViewTaskFactory(){ return this.createNetworkViewTaskFactory;	}
	@Override public ExportNetworkImageTaskFactory get_ExportNetworkImageTaskFactory(){	return this.exportNetworkImageTaskFactory;}
	@Override public ExportNetworkViewTaskFactory get_ExportNetworkViewTaskFactory(){ return this.exportNetworkViewTaskFactory;	}
	@Override public ExportSelectedTableTaskFactory get_ExportSelectedTableTaskFactory(){	return this.exportSelectedTableTaskFactory; }
	@Override public ExportTableTaskFactory get_ExportTableTaskFactory(){	return this.exportTableTaskFactory; }
	@Override public ExportVizmapTaskFactory get_ExportVizmapTaskFactory(){	return this.exportVizmapTaskFactory; }
	@Override public NewSessionTaskFactory get_NewSessionTaskFactory(){	return this.newSessionTaskFactory;}
	@Override public OpenSessionTaskFactory get_OpenSessionTaskFactory(){ return this.openSessionTaskFactory;}
	@Override public SaveSessionAsTaskFactory get_SaveSessionAsTaskFactory(){ return this.saveSessionAsTaskFactory;	}
	@Override public ApplyPreferredLayoutTaskFactory get_ApplyPreferredLayoutTaskFactory(){	return this.applyPreferredLayoutTaskFactory; }
	@Override public DeleteColumnTaskFactory get_DeleteColumnTaskFactory(){ return this.deleteColumnTaskFactory;}
	@Override public RenameColumnTaskFactory get_RenameColumnTaskFactory(){	return this.renameColumnTaskFactory;}
	@Override public DeleteTableTaskFactory get_DeleteTableTaskFactory(){ return this.deleteTableTaskFactory;}
	@Override public ConnectSelectedNodesTaskFactory get_ConnectSelectedNodesTaskFactory(){	return this.connectSelectedNodesTaskFactory; }
	@Override public GroupNodesTaskFactory get_GroupNodesTaskFactory(){return this.groupNodesTaskFactory;}
	@Override public UnGroupTaskFactory get_UnGroupTaskFactory(){return this.unGroupTaskFactory;}
	@Override public CollapseGroupTaskFactory get_CollapseGroupTaskFactory(){return this.collapseGroupTaskFactory;}
	@Override public ExpandGroupTaskFactory get_ExpandGroupTaskFactory(){return this.expandGroupTaskFactory;}
	@Override public UnGroupNodesTaskFactory get_UnGroupNodesTaskFactory(){return this.unGroupNodesTaskFactory;}
	@Override public MapTableToNetworkTablesTaskFactory get_MapTableToNetworkTablesTaskFactory(){	return this.mapNetworkAttrTaskFactory;}
}
