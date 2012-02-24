package org.cytoscape.app.internal;


import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyVersion;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedEvent;
import org.cytoscape.datasource.DataSourceManager;
import org.cytoscape.dnd.DropNetworkViewTaskFactory;
import org.cytoscape.equations.AbstractFunction;
import org.cytoscape.event.CyEventHelper;
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
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.AboutToRemoveEdgesEvent;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.app.AbstractCyApp;
import org.cytoscape.app.CyAppAdapter;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.creation.NewEmptyNetworkViewFactory;
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.events.AboutToRemoveEdgeViewsListener;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.events.RenderingEngineAboutToBeRemovedEvent;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.events.VisualStyleAboutToBeRemovedEvent;
import org.cytoscape.view.vizmap.gui.AbstractVisualPropertyDependency;
import org.cytoscape.view.vizmap.gui.editor.AbstractVisualPropertyEditor;
import org.cytoscape.view.vizmap.gui.event.LexiconStateChangedEvent;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.swing.AbstractGUITunableHandler;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.PanelTaskManager;
import org.cytoscape.work.swing.SubmenuTaskManager;
import org.cytoscape.work.undo.UndoSupport;
import org.cytoscape.work.util.BoundedDouble;


/**
 * An implementation of CyAppAdapter
 */
public class CyAppAdapterImpl implements CyAppAdapter {

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
//	private final CyTableWriterManager cyTableWriterManager;
	private final DialogTaskManager dialogTaskManager;
	private final PanelTaskManager panelTaskManager;
	private final SubmenuTaskManager submenuTaskManager;
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
	private SessionAboutToBeSavedEvent sessionAboutToBeSavedEvent;
	private VisualStyleAboutToBeRemovedEvent visualStyleAboutToBeRemovedEvent;
	private AboutToRemoveEdgeViewsListener aboutToRemoveEdgeViewsListener;
	private AboutToRemoveEdgesEvent aboutToRemoveEdgesEvent;
	private RenderingEngineAboutToBeRemovedEvent renderingEngineAboutToBeRemovedEvent;
	private VisualLexicon abstractVisualLexicon;
	private SimpleCyProperty basicCyProperty;
	private BookmarksUtil bookmarksUtil;
	private NetworkTaskFactory networkTaskFactory;
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
	private AbstractVisualPropertyDependency abstractVisualPropertyDependency;
	private AbstractVisualPropertyEditor abstractVisualPropertyEditor;
	private LexiconStateChangedEvent lexiconStateChangedEvent;
	private NetworkImportWebServiceClient networkImportWebServiceClient;
	private AbstractWebServiceClient abstractWebServiceClient;
	private DataImportFinishedEvent dataImportFinishedEvent;
	private DropNetworkViewTaskFactory dropNetworkViewTaskFactory;
	private AbstractCyApp cyApp;
	private final VisualMappingFunctionFactory visualMappingFunctionContinuousFactory;
	private final VisualMappingFunctionFactory visualMappingFunctionDiscreteFactory;
	private final VisualMappingFunctionFactory visualMappingFunctionPassthroughFactory;

	//
	// Since this is implementation code, there shouldn't be a
	// a problem adding new arguments as needed.  Therefore, to
	// maintain sanity, keep the arguments in alphabetical order.
	//
	CyAppAdapterImpl( final CyApplicationManager cyApplicationManager,
	                     final CyEventHelper cyEventHelper,
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
	                     final CyVersion cyVersion,
//	                     final CyTableWriterManager cyTableWriterManager,
	                     final DialogTaskManager dialogTaskManager,
	                     final PanelTaskManager panelTaskManager,
	                     final SubmenuTaskManager submenuTaskManager,
	                     final PresentationWriterManager presentationWriterManager,
	                     final RenderingEngineManager renderingEngineManager,
	                     final TaskManager taskManager,
	                     final UndoSupport undoSupport,
	                     final VisualMappingFunctionFactory visualMappingFunctionContinuousFactory,
	                     final VisualMappingFunctionFactory visualMappingFunctionDiscreteFactory,
	                     final VisualMappingFunctionFactory visualMappingFunctionPassthroughFactory,
	                     final VisualMappingManager visualMappingManager,
	                     final VisualStyleFactory visualStyleFactory,
	                     final DataSourceManager dataSourceManager
					    )
	{
		this.cyApplicationManager = cyApplicationManager;
		this.cyEventHelper = cyEventHelper;
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
		this.cyVersion = cyVersion;
//		this.cyTableWriterManager = cyTableWriterManager;
		this.dialogTaskManager = dialogTaskManager;
		this.panelTaskManager = panelTaskManager;
		this.submenuTaskManager = submenuTaskManager;
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
	}

	//
	// May as well keep the methods alphabetical too!
	// 
	public CyApplicationManager getCyApplicationManager() { return cyApplicationManager; }
	public CyEventHelper getCyEventHelper() { return cyEventHelper; } 
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
	public CyRootNetworkManager getCyRootNetworkFactory() { return cyRootNetworkFactory; } 
	public CyServiceRegistrar getCyServiceRegistrar() { return cyServiceRegistrar; }
	public CySessionManager getCySessionManager() { return cySessionManager; } 
	public CySessionReaderManager getCySessionReaderManager() { return cySessionReaderManager; }
	public CySessionWriterManager getCySessionWriterManager() { return cySessionWriterManager; }
	public CySwingApplication getCySwingApplication() { return cySwingApplication; }
	public CyTableFactory getCyTableFactory() { return cyTableFactory; } 
	public CyTableManager getCyTableManager() { return cyTableManager; }
	public CyTableReaderManager getCyTableReaderManager() { return cyTableReaderManager; }
	public CyVersion getCyVersion() { return cyVersion; }
//	public CyTableWriterManager getCyTableWriterManager() { return cyTableWriterManager; }
	public DialogTaskManager getDialogTaskManager() { return dialogTaskManager; }
	public PanelTaskManager getPanelTaskManager() { return panelTaskManager; }
	public SubmenuTaskManager getSubmenuTaskManager() { return submenuTaskManager; }
	public PresentationWriterManager getPresentationWriterManager() { return presentationWriterManager; }
	public RenderingEngineManager getRenderingEngineManager() { return renderingEngineManager; }
	public TaskManager getTaskManager() { return taskManager; }
	public UndoSupport getUndoSupport() { return undoSupport; }
	@Override public VisualMappingFunctionFactory getVisualMappingFunctionContinuousFactory() { return visualMappingFunctionContinuousFactory; }
	@Override public VisualMappingFunctionFactory getVisualMappingFunctionDiscreteFactory() { return visualMappingFunctionDiscreteFactory; }
	@Override public VisualMappingFunctionFactory getVisualMappingFunctionPassthroughFactory() { return visualMappingFunctionPassthroughFactory; }
	public VisualMappingManager getVisualMappingManager() { return visualMappingManager; }
	public VisualStyleFactory getVisualStyleFactory() { return visualStyleFactory; }
	@Override public DataSourceManager getDataSourceManager() { return dataSourceManager; }
}
