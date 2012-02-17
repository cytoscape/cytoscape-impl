
/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/

package org.cytoscape.internal;

import static org.cytoscape.application.swing.CytoPanelName.EAST;
import static org.cytoscape.application.swing.CytoPanelName.SOUTH;
import static org.cytoscape.application.swing.CytoPanelName.SOUTH_WEST;
import static org.cytoscape.application.swing.CytoPanelName.WEST;
import static org.cytoscape.internal.view.CyDesktopManager.Arrange.CASCADE;
import static org.cytoscape.internal.view.CyDesktopManager.Arrange.GRID;
import static org.cytoscape.internal.view.CyDesktopManager.Arrange.HORIZONTAL;
import static org.cytoscape.internal.view.CyDesktopManager.Arrange.VERTICAL;

import java.util.Properties;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyShutdown;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.ToolBarComponent;
import org.cytoscape.datasource.DataSourceManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.internal.actions.BookmarkAction;
import org.cytoscape.internal.actions.CytoPanelAction;
import org.cytoscape.internal.actions.ExitAction;
import org.cytoscape.internal.actions.PreferenceAction;
import org.cytoscape.internal.actions.PrintAction;
import org.cytoscape.internal.actions.RecentSessionManager;
import org.cytoscape.internal.actions.WelcomeScreenAction;
import org.cytoscape.internal.dialogs.BookmarkDialogFactoryImpl;
import org.cytoscape.internal.dialogs.PreferencesDialogFactoryImpl;
import org.cytoscape.internal.io.SessionStateIO;
import org.cytoscape.internal.layout.ui.LayoutMenuPopulator;
import org.cytoscape.internal.layout.ui.SettingsAction;
import org.cytoscape.internal.select.RowViewTracker;
import org.cytoscape.internal.select.RowsSetViewUpdater;
import org.cytoscape.internal.select.SelectEdgeViewUpdater;
import org.cytoscape.internal.select.SelectNodeViewUpdater;
import org.cytoscape.internal.shutdown.ConfigDirPropertyWriter;
import org.cytoscape.internal.undo.RedoAction;
import org.cytoscape.internal.undo.UndoAction;
import org.cytoscape.internal.util.undo.UndoMonitor;
import org.cytoscape.internal.view.BirdsEyeViewHandler;
import org.cytoscape.internal.view.CyHelpBrokerImpl;
import org.cytoscape.internal.view.CytoscapeDesktop;
import org.cytoscape.internal.view.CytoscapeMenuBar;
import org.cytoscape.internal.view.CytoscapeMenuPopulator;
import org.cytoscape.internal.view.CytoscapeMenus;
import org.cytoscape.internal.view.CytoscapeToolBar;
import org.cytoscape.internal.view.NetworkPanel;
import org.cytoscape.internal.view.NetworkViewManager;
import org.cytoscape.internal.view.ToolBarEnableUpdater;
import org.cytoscape.internal.view.help.ArrangeTaskFactory;
import org.cytoscape.internal.view.help.HelpAboutTaskFactory;
import org.cytoscape.internal.view.help.HelpContactHelpDeskTaskFactory;
import org.cytoscape.internal.view.help.HelpContentsTaskFactory;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.io.write.CyPropertyWriterManager;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.NetworkDestroyedListener;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CyNetworkNaming;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.task.NetworkCollectionTaskFactory;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewCollectionTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.task.creation.ImportNetworksTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.NetworkViewDestroyedListener;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.PanelTaskManager;
import org.cytoscape.work.swing.SubmenuTaskManager;
import org.cytoscape.work.swing.undo.SwingUndoSupport;
import org.osgi.framework.BundleContext;

/**
 *
 */
public class CyActivator extends AbstractCyActivator {
	/**
	 * Creates a new CyActivator object.
	 */
	public CyActivator() {
		super();
	}

	@Override
	public void start(BundleContext bc) throws Exception {
		
		ImportNetworksTaskFactory importNetworkTF = getService(bc, ImportNetworksTaskFactory.class, "(id=loadNetworkURLTaskFactory)");
		TaskFactory importNetworkFileTF = getService(bc, TaskFactory.class, "(id=loadNetworkFileTaskFactory)");
		NetworkTaskFactory createNetworkViewTaskFactory = getService(bc, NetworkTaskFactory.class, "(id=createNetworkViewTaskFactory)");
		TaskFactory openSessionTaskFactory = getService(bc, TaskFactory.class, "(id=openSessionTaskFactory)");
		
		DataSourceManager dsManagerServiceRef = getService(bc, DataSourceManager.class);
		
		
		CyShutdown cytoscapeShutdownServiceRef = getService(bc, CyShutdown.class);
		CyApplicationConfiguration cyApplicationConfigurationServiceRef = getService(bc,
		                                                                             CyApplicationConfiguration.class);
		StreamUtil streamUtilServiceRef = getService(bc, StreamUtil.class);
		RecentlyOpenedTracker recentlyOpenedTrackerServiceRef = getService(bc,
		                                                                   RecentlyOpenedTracker.class);
		CyProperty cytoscapePropertiesServiceRef = getService(bc, CyProperty.class,
		                                                      "(cyPropertyName=cytoscape3.props)");
		CyApplicationManager cyApplicationManagerServiceRef = getService(bc,
		                                                                 CyApplicationManager.class);
		CySessionManager cySessionManagerServiceRef = getService(bc, CySessionManager.class);
		CySessionReaderManager sessionReaderManagerServiceRef = getService(bc,
		                                                                   CySessionReaderManager.class);
		CyPropertyWriterManager propertyWriterManagerRef = getService(bc,
		                                                              CyPropertyWriterManager.class);
		CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc,
		                                                                 CyNetworkViewManager.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc, CyNetworkManager.class);
		CyNetworkNaming cyNetworkNamingServiceRef = getService(bc, CyNetworkNaming.class);
		DialogTaskManager dialogTaskManagerServiceRef = getService(bc, DialogTaskManager.class);
		PanelTaskManager panelTaskManagerServiceRef = getService(bc, PanelTaskManager.class);
		SubmenuTaskManager submenuTaskManagerServiceRef = getService(bc, SubmenuTaskManager.class);
		RenderingEngineFactory dingRenderingEngineFactoryServiceRef = getService(bc,
		                                                                         RenderingEngineFactory.class,
		                                                                         "(id=ding)");
		RenderingEngineFactory dingNavigationPresentationFactoryServiceRef = getService(bc,
		                                                                                RenderingEngineFactory.class,
		                                                                                "(id=dingNavigation)");
		CyProperty bookmarkServiceRef = getService(bc, CyProperty.class,
		                                           "(cyPropertyName=bookmarks)");
		BookmarksUtil bookmarksUtilServiceRef = getService(bc, BookmarksUtil.class);
		CyNetworkFactory cyNetworkFactoryServiceRef = getService(bc, CyNetworkFactory.class);
		CyNetworkViewFactory cyNetworkViewFactoryServiceRef = getService(bc,
		                                                                 CyNetworkViewFactory.class);
		CyLayoutAlgorithmManager cyLayoutsServiceRef = getService(bc, CyLayoutAlgorithmManager.class);
		SwingUndoSupport undoSupportServiceRef = getService(bc, SwingUndoSupport.class);
		CyEventHelper cyEventHelperServiceRef = getService(bc, CyEventHelper.class);
		CyTableManager cyTableManagerServiceRef = getService(bc, CyTableManager.class);
		CyServiceRegistrar cyServiceRegistrarServiceRef = getService(bc, CyServiceRegistrar.class);
		OpenBrowser openBrowserServiceRef = getService(bc, OpenBrowser.class);
		
		VisualMappingManager visualMappingManagerServiceRef  = getService(bc, VisualMappingManager.class);

		UndoAction undoAction = new UndoAction(undoSupportServiceRef);
		RedoAction redoAction = new RedoAction(undoSupportServiceRef);
		ConfigDirPropertyWriter configDirPropertyWriter = new ConfigDirPropertyWriter(dialogTaskManagerServiceRef,
		                                                                              propertyWriterManagerRef,
		                                                                              cyApplicationConfigurationServiceRef);
		CyHelpBrokerImpl cyHelpBroker = new CyHelpBrokerImpl();
		PreferencesDialogFactoryImpl preferencesDialogFactory = new PreferencesDialogFactoryImpl(cyEventHelperServiceRef);
		BookmarkDialogFactoryImpl bookmarkDialogFactory = new BookmarkDialogFactoryImpl(bookmarkServiceRef,
		                                                                                bookmarksUtilServiceRef);
		
		registerService(bc, bookmarkDialogFactory, SessionLoadedListener.class, new Properties());
		
		CytoscapeMenuBar cytoscapeMenuBar = new CytoscapeMenuBar();
		CytoscapeToolBar cytoscapeToolBar = new CytoscapeToolBar();
		CytoscapeMenus cytoscapeMenus = new CytoscapeMenus(cytoscapeMenuBar, cytoscapeToolBar);
		ToolBarEnableUpdater toolBarEnableUpdater = new ToolBarEnableUpdater(cytoscapeToolBar);
		NetworkViewManager networkViewManager = new NetworkViewManager(cyApplicationManagerServiceRef,
		                                                               cyNetworkViewManagerServiceRef,
		                                                               cytoscapePropertiesServiceRef,
		                                                               cyHelpBroker);
		BirdsEyeViewHandler birdsEyeViewHandler = new BirdsEyeViewHandler(cyApplicationManagerServiceRef,
		                                                                  networkViewManager,
		                                                                  dingNavigationPresentationFactoryServiceRef);
		NetworkPanel networkPanel = new NetworkPanel(cyApplicationManagerServiceRef,
		                                             cyNetworkManagerServiceRef,
		                                             cyNetworkViewManagerServiceRef,
		                                             birdsEyeViewHandler, dialogTaskManagerServiceRef);
		CytoscapeDesktop cytoscapeDesktop = new CytoscapeDesktop(cytoscapeMenus,
		                                                         networkViewManager, networkPanel,
		                                                         cytoscapeShutdownServiceRef,
		                                                         cyEventHelperServiceRef,
		                                                         cyServiceRegistrarServiceRef,
		                                                         dialogTaskManagerServiceRef);
		SynchronousTaskManager<?> synchronousTaskManagerServiceRef = getService(bc, SynchronousTaskManager.class);
		TaskFactory saveTaskFactoryServiceRef = getService(bc, TaskFactory.class, "(task.id=saveSession)");
		SessionStateIO sessStateIO = new SessionStateIO();
		SessionHandler sessionHandler = new SessionHandler(cytoscapeDesktop,
														   cyNetworkManagerServiceRef,
														   cyApplicationManagerServiceRef,
														   networkViewManager,
														   synchronousTaskManagerServiceRef,
														   saveTaskFactoryServiceRef,
														   sessStateIO);
		PrintAction printAction = new PrintAction(cyApplicationManagerServiceRef, cytoscapePropertiesServiceRef);
		ExitAction exitAction = new ExitAction( cytoscapeShutdownServiceRef);
		PreferenceAction preferenceAction = new PreferenceAction(cytoscapeDesktop,
		                                                         preferencesDialogFactory,
		                                                         bookmarksUtilServiceRef);
		BookmarkAction bookmarkAction = new BookmarkAction(cytoscapeDesktop, bookmarkDialogFactory);
		LayoutMenuPopulator layoutMenuPopulator = new LayoutMenuPopulator(cytoscapeDesktop,
		                                                                  cyApplicationManagerServiceRef,
		                                                                  submenuTaskManagerServiceRef,
		                                                                  undoSupportServiceRef,
		                                                                  cyEventHelperServiceRef);
		CytoscapeMenuPopulator cytoscapeMenuPopulator = new CytoscapeMenuPopulator(cytoscapeDesktop,
		                                                                           dialogTaskManagerServiceRef,
		                                                                           panelTaskManagerServiceRef,
		                                                                           cyApplicationManagerServiceRef,
		                                                                           cyServiceRegistrarServiceRef);
		SettingsAction settingsAction = new SettingsAction(cyLayoutsServiceRef, cytoscapeDesktop,
		                                                   cyApplicationManagerServiceRef,
		                                                   panelTaskManagerServiceRef, cytoscapePropertiesServiceRef);
		HelpContentsTaskFactory helpContentsTaskFactory = new HelpContentsTaskFactory(cyHelpBroker,
		                                                                              cytoscapeDesktop);
		HelpContactHelpDeskTaskFactory helpContactHelpDeskTaskFactory = new HelpContactHelpDeskTaskFactory(openBrowserServiceRef);
		HelpAboutTaskFactory helpAboutTaskFactory = new HelpAboutTaskFactory();
		ArrangeTaskFactory arrangeGridTaskFactory = new ArrangeTaskFactory((CytoscapeDesktop)cytoscapeDesktop, GRID);
		ArrangeTaskFactory arrangeCascadeTaskFactory = new ArrangeTaskFactory((CytoscapeDesktop)cytoscapeDesktop,
		                                                                      CASCADE);
		ArrangeTaskFactory arrangeHorizontalTaskFactory = new ArrangeTaskFactory((CytoscapeDesktop)cytoscapeDesktop,
		                                                                         HORIZONTAL);
		ArrangeTaskFactory arrangeVerticalTaskFactory = new ArrangeTaskFactory((CytoscapeDesktop)cytoscapeDesktop,
		                                                                       VERTICAL);
		CytoPanelAction cytoPanelWestAction = new CytoPanelAction(WEST, true, cytoscapeDesktop, 1.0f);
		CytoPanelAction cytoPanelSouthAction = new CytoPanelAction(SOUTH, true, cytoscapeDesktop, 1.1f);
		CytoPanelAction cytoPanelEastAction = new CytoPanelAction(EAST, false, cytoscapeDesktop, 1.2f);
		CytoPanelAction cytoPanelSouthWestAction = new CytoPanelAction(SOUTH_WEST, false, cytoscapeDesktop, 1.3f);

		UndoMonitor undoMonitor = new UndoMonitor(undoSupportServiceRef,
		                                          cytoscapePropertiesServiceRef);
		RowViewTracker rowViewTracker = new RowViewTracker();
		SelectEdgeViewUpdater selecteEdgeViewUpdater = new SelectEdgeViewUpdater(rowViewTracker);
		SelectNodeViewUpdater selecteNodeViewUpdater = new SelectNodeViewUpdater(rowViewTracker);
		
		RowsSetViewUpdater rowsSetViewUpdater = new RowsSetViewUpdater(cyApplicationManagerServiceRef, 
				cyNetworkViewManagerServiceRef, visualMappingManagerServiceRef, rowViewTracker);
		
		
		RecentSessionManager recentSessionManager = new RecentSessionManager(recentlyOpenedTrackerServiceRef,
		                                                                     cyServiceRegistrarServiceRef,
		                                                                     cySessionManagerServiceRef,
		                                                                     sessionReaderManagerServiceRef,
		                                                                     cyApplicationManagerServiceRef);
		
		// Show Welcome Screen
		final WelcomeScreenAction welcomeScreenAction = new WelcomeScreenAction(bc,cytoscapeDesktop, openBrowserServiceRef, recentlyOpenedTrackerServiceRef, openSessionTaskFactory, submenuTaskManagerServiceRef, importNetworkFileTF, importNetworkTF, createNetworkViewTaskFactory, cyApplicationConfigurationServiceRef, dsManagerServiceRef, cytoscapePropertiesServiceRef);
		registerAllServices(bc, welcomeScreenAction, new Properties());

		registerService(bc, undoAction, CyAction.class, new Properties());
		registerService(bc, redoAction, CyAction.class, new Properties());
		registerService(bc, printAction, CyAction.class, new Properties());
		registerService(bc, preferenceAction, CyAction.class, new Properties());
		registerService(bc, bookmarkAction, CyAction.class, new Properties());
		registerService(bc, settingsAction, CyAction.class, new Properties());
		registerService(bc, cytoPanelWestAction, CyAction.class, new Properties());
		registerService(bc, cytoPanelSouthAction, CyAction.class, new Properties());
		registerService(bc, cytoPanelEastAction, CyAction.class, new Properties());
		registerService(bc, cytoPanelSouthWestAction, CyAction.class, new Properties());

		if (isMac()) {
			new MacCyActivator().start(bc);
		} else {
			registerService(bc, exitAction, CyAction.class, new Properties());
		}

		Properties helpContentsTaskFactoryProps = new Properties();
		helpContentsTaskFactoryProps.setProperty("preferredMenu", "Help");
		helpContentsTaskFactoryProps.setProperty("largeIconURL", getClass().getResource("/images/ximian/stock_help.png").toString());
		helpContentsTaskFactoryProps.setProperty("title", "Contents...");
		helpContentsTaskFactoryProps.setProperty("tooltip", "Show Help Contents...");
		helpContentsTaskFactoryProps.setProperty("toolBarGravity", "20.0f");
		helpContentsTaskFactoryProps.setProperty("inToolBar", "true");
		registerService(bc, helpContentsTaskFactory, TaskFactory.class, helpContentsTaskFactoryProps);

		Properties helpContactHelpDeskTaskFactoryProps = new Properties();
		helpContactHelpDeskTaskFactoryProps.setProperty("preferredMenu", "Help");
		helpContactHelpDeskTaskFactoryProps.setProperty("title", "Contact Help Desk...");
		registerService(bc, helpContactHelpDeskTaskFactory, TaskFactory.class,
		                helpContactHelpDeskTaskFactoryProps);

		Properties helpAboutTaskFactoryProps = new Properties();
		helpAboutTaskFactoryProps.setProperty("preferredMenu", "Help");
		helpAboutTaskFactoryProps.setProperty("title", "About...");
		registerService(bc, helpAboutTaskFactory, TaskFactory.class, helpAboutTaskFactoryProps);

		Properties arrangeGridTaskFactoryProps = new Properties();
		arrangeGridTaskFactoryProps.setProperty("enableFor", "networkAndView");
		arrangeGridTaskFactoryProps.setProperty("preferredMenu", "View.Arrange Network Windows[110]");
		arrangeGridTaskFactoryProps.setProperty("title", "Grid");
		registerService(bc, arrangeGridTaskFactory, TaskFactory.class, arrangeGridTaskFactoryProps);

		Properties arrangeCascadeTaskFactoryProps = new Properties();
		arrangeCascadeTaskFactoryProps.setProperty("enableFor", "networkAndView");
		arrangeCascadeTaskFactoryProps.setProperty("preferredMenu",
		                                           "View.Arrange Network Windows[110]");
		arrangeCascadeTaskFactoryProps.setProperty("title", "Cascade");
		registerService(bc, arrangeCascadeTaskFactory, TaskFactory.class,
		                arrangeCascadeTaskFactoryProps);

		Properties arrangeHorizontalTaskFactoryProps = new Properties();
		arrangeHorizontalTaskFactoryProps.setProperty("enableFor", "networkAndView");
		arrangeHorizontalTaskFactoryProps.setProperty("preferredMenu",
		                                              "View.Arrange Network Windows[110]");
		arrangeHorizontalTaskFactoryProps.setProperty("title", "Horizontal");
		registerService(bc, arrangeHorizontalTaskFactory, TaskFactory.class,
		                arrangeHorizontalTaskFactoryProps);

		Properties arrangeVerticalTaskFactoryProps = new Properties();
		arrangeVerticalTaskFactoryProps.setProperty("enableFor", "networkAndView");
		arrangeVerticalTaskFactoryProps.setProperty("preferredMenu",
		                                            "View.Arrange Network Windows[110]");
		arrangeVerticalTaskFactoryProps.setProperty("title", "Vertical");
		registerService(bc, arrangeVerticalTaskFactory, TaskFactory.class,
		                arrangeVerticalTaskFactoryProps);
		registerAllServices(bc, cytoscapeDesktop, new Properties());
		registerAllServices(bc, networkPanel, new Properties());
		registerAllServices(bc, networkViewManager, new Properties());
		registerAllServices(bc, birdsEyeViewHandler, new Properties());
		registerService(bc, undoMonitor, SetCurrentNetworkViewListener.class, new Properties());
		registerService(bc, undoMonitor, NetworkDestroyedListener.class, new Properties());
		registerService(bc, undoMonitor, NetworkViewDestroyedListener.class, new Properties());
		registerAllServices(bc, rowViewTracker, new Properties());
		registerAllServices(bc, selecteEdgeViewUpdater, new Properties());
		registerAllServices(bc, selecteNodeViewUpdater, new Properties());

		registerAllServices(bc, rowsSetViewUpdater, new Properties());
		
		registerAllServices(bc, sessionHandler, new Properties());
		registerAllServices(bc, toolBarEnableUpdater, new Properties());
		registerService(bc, configDirPropertyWriter, CyShutdownListener.class, new Properties());
		registerAllServices(bc, recentSessionManager, new Properties());

		registerServiceListener(bc, cytoscapeDesktop, "addAction", "removeAction", CyAction.class);
		registerServiceListener(bc, preferenceAction, "addCyProperty", "removeCyProperty",
		                        CyProperty.class);
		registerServiceListener(bc, cytoscapeDesktop, "addCytoPanelComponent",
		                        "removeCytoPanelComponent", CytoPanelComponent.class);
		registerServiceListener(bc, cytoscapeDesktop, "addToolBarComponent",
		                        "removeToolBarComponent", ToolBarComponent.class);
		registerServiceListener(bc, cytoscapeMenuPopulator, "addTaskFactory", "removeTaskFactory",
		                        TaskFactory.class);
		registerServiceListener(bc, cytoscapeMenuPopulator, "addNetworkTaskFactory",
		                        "removeNetworkTaskFactory", NetworkTaskFactory.class);
		registerServiceListener(bc, cytoscapeMenuPopulator, "addNetworkViewTaskFactory",
		                        "removeNetworkViewTaskFactory", NetworkViewTaskFactory.class);
		registerServiceListener(bc, cytoscapeMenuPopulator, "addNetworkCollectionTaskFactory",
		                        "removeNetworkCollectionTaskFactory",
		                        NetworkCollectionTaskFactory.class);
		registerServiceListener(bc, cytoscapeMenuPopulator, "addNetworkViewCollectionTaskFactory",
		                        "removeNetworkViewCollectionTaskFactory",
		                        NetworkViewCollectionTaskFactory.class);
		registerServiceListener(bc, cytoscapeMenuPopulator, "addTableTaskFactory",
		                        "removeTableTaskFactory", TableTaskFactory.class);
		registerServiceListener(bc, networkViewManager, "addPresentationFactory",
		                        "removePresentationFactory", RenderingEngineFactory.class);
		registerServiceListener(bc, networkPanel, "addNetworkViewTaskFactory",
		                        "removeNetworkViewTaskFactory", NetworkViewTaskFactory.class, "(scope=limited)");
		registerServiceListener(bc, networkPanel, "addNetworkTaskFactory",
		                        "removeNetworkTaskFactory", NetworkTaskFactory.class, "(scope=limited)");
		registerServiceListener(bc, networkPanel, "addNetworkViewCollectionTaskFactory",
		                        "removeNetworkViewCollectionTaskFactory",
		                        NetworkViewCollectionTaskFactory.class, "(scope=limited)");
		registerServiceListener(bc, networkPanel, "addNetworkCollectionTaskFactory",
		                        "removeNetworkCollectionTaskFactory",
		                        NetworkCollectionTaskFactory.class, "(scope=limited)");
		registerServiceListener(bc, configDirPropertyWriter, "addCyProperty", "removeCyProperty",
		                        CyProperty.class);
		registerServiceListener(bc, layoutMenuPopulator, "addLayout", "removeLayout",
		                        CyLayoutAlgorithm.class);
	}

	private boolean isMac() {
		return System.getProperty("os.name").startsWith("Mac OS X");
	}
}
