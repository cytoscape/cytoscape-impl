package org.cytoscape.internal;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
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

import static org.cytoscape.application.swing.CytoPanelName.*;
import static org.cytoscape.application.swing.CyNetworkViewDesktopMgr.ArrangeType.*;
import static org.cytoscape.work.ServiceProperties.*;

import java.util.Properties;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyShutdown;
import org.cytoscape.application.CyVersion;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CyNetworkViewDesktopMgr;
import org.cytoscape.application.swing.CyHelpBroker;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.ToolBarComponent;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.internal.actions.BookmarkAction;
import org.cytoscape.internal.actions.CytoPanelAction;
import org.cytoscape.internal.actions.ExitAction;
import org.cytoscape.internal.actions.FullScreenAction;
import org.cytoscape.internal.actions.FullScreenMacAction;
import org.cytoscape.internal.actions.PreferenceAction;
import org.cytoscape.internal.actions.PrintAction;
import org.cytoscape.internal.actions.RecentSessionManager;
import org.cytoscape.internal.dialogs.BookmarkDialogFactoryImpl;
import org.cytoscape.internal.dialogs.PreferencesDialogFactoryImpl;
import org.cytoscape.internal.io.SessionIO;
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
import org.cytoscape.internal.view.CyDesktopManager;
import org.cytoscape.internal.view.CytoscapeMenuBar;
import org.cytoscape.internal.view.CytoscapeMenuPopulator;
import org.cytoscape.internal.view.CytoscapeMenus;
import org.cytoscape.internal.view.CytoscapeToolBar;
import org.cytoscape.internal.view.IconManagerImpl;
import org.cytoscape.internal.view.MacFullScreenEnabler;
import org.cytoscape.internal.view.NetworkPanel;
import org.cytoscape.internal.view.NetworkViewManager;
import org.cytoscape.internal.view.ToolBarEnableUpdater;
import org.cytoscape.internal.view.help.ArrangeTaskFactory;
import org.cytoscape.internal.view.help.HelpAboutTaskFactory;
import org.cytoscape.internal.view.help.HelpContactHelpDeskTaskFactory;
import org.cytoscape.internal.view.help.HelpContentsTaskFactory;
import org.cytoscape.internal.view.help.HelpReportABugTaskFactory;
import org.cytoscape.io.datasource.DataSourceManager;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.NetworkDestroyedListener;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.task.DynamicTaskFactoryProvisioner;
import org.cytoscape.task.NetworkCollectionTaskFactory;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewCollectionTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.task.edit.EditNetworkTitleTaskFactory;
import org.cytoscape.task.write.SaveSessionAsTaskFactory;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.NetworkViewDestroyedListener;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.PanelTaskManager;
import org.cytoscape.work.swing.TaskStatusPanelFactory;
import org.cytoscape.work.swing.undo.SwingUndoSupport;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class CyActivator extends AbstractCyActivator {
	private static final String CONTEXT_MENU_FILTER = "(" + ServiceProperties.IN_NETWORK_PANEL_CONTEXT_MENU + "=true)";

	/**
	 * Creates a new CyActivator object.
	 */
	public CyActivator() {
		super();
	}

	@Override
	public void start(BundleContext bc) throws Exception {
		setLookAndFeel();
		
		RenderingEngineManager renderingEngineManagerServiceRef = getService(bc, RenderingEngineManager.class);
		CyShutdown cytoscapeShutdownServiceRef = getService(bc, CyShutdown.class);
		CyApplicationConfiguration cyApplicationConfigurationServiceRef = getService(bc, CyApplicationConfiguration.class);
		RecentlyOpenedTracker recentlyOpenedTrackerServiceRef = getService(bc, RecentlyOpenedTracker.class);
		CyProperty cytoscapePropertiesServiceRef = getService(bc, CyProperty.class, "(cyPropertyName=cytoscape3.props)");
		CyVersion cyVersionServiceRef = getService(bc, CyVersion.class);
		CyApplicationManager cyApplicationManagerServiceRef = getService(bc, CyApplicationManager.class);
		CySessionManager cySessionManagerServiceRef = getService(bc, CySessionManager.class);
		CySessionReaderManager sessionReaderManagerServiceRef = getService(bc, CySessionReaderManager.class);
		CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc, CyNetworkViewManager.class);
		CyNetworkManager cyNetworkManagerServiceRef = getService(bc, CyNetworkManager.class);
		CyTableManager cyTableManagerServiceRef = getService(bc, CyTableManager.class);
		CyNetworkTableManager cyNetworkTableManagerServiceRef = getService(bc, CyNetworkTableManager.class);
		CyGroupManager cyGroupManagerServiceRef = getService(bc, CyGroupManager.class);
		DialogTaskManager dialogTaskManagerServiceRef = getService(bc, DialogTaskManager.class);
		PanelTaskManager panelTaskManagerServiceRef = getService(bc, PanelTaskManager.class);
		TaskStatusPanelFactory taskStatusPanelFactoryRef = getService(bc, TaskStatusPanelFactory.class);

		RenderingEngineFactory dingNavigationPresentationFactoryServiceRef = getService(bc,
		                                                                                RenderingEngineFactory.class,
		                                                                                "(id=dingNavigation)");
		CyProperty bookmarkServiceRef = getService(bc, CyProperty.class, "(cyPropertyName=bookmarks)");
		BookmarksUtil bookmarksUtilServiceRef = getService(bc, BookmarksUtil.class);
		
		CyLayoutAlgorithmManager cyLayoutsServiceRef = getService(bc, CyLayoutAlgorithmManager.class);
		SwingUndoSupport undoSupportServiceRef = getService(bc, SwingUndoSupport.class);
		CyEventHelper cyEventHelperServiceRef = getService(bc, CyEventHelper.class);
		CyServiceRegistrar cyServiceRegistrarServiceRef = getService(bc, CyServiceRegistrar.class);
		OpenBrowser openBrowserServiceRef = getService(bc, OpenBrowser.class);
		
		VisualMappingManager visualMappingManagerServiceRef  = getService(bc, VisualMappingManager.class);
		FileUtil fileUtilServiceRef = getService(bc, FileUtil.class);

		DynamicTaskFactoryProvisioner dynamicTaskFactoryProvisionerServiceRef = getService(bc, 
		                                                                                   DynamicTaskFactoryProvisioner.class);
		
		DataSourceManager dsManagerServiceRef = getService(bc, DataSourceManager.class);
		
		EditNetworkTitleTaskFactory editNetworkTitleTFServiceRef  = getService(bc, EditNetworkTitleTaskFactory.class);
		
		//////////////		
		UndoAction undoAction = new UndoAction(undoSupportServiceRef);
		RedoAction redoAction = new RedoAction(undoSupportServiceRef);
		ConfigDirPropertyWriter configDirPropertyWriter = new ConfigDirPropertyWriter(dialogTaskManagerServiceRef,
		                                                                              cyApplicationConfigurationServiceRef);
		CyHelpBrokerImpl cyHelpBroker = new CyHelpBrokerImpl();
		PreferencesDialogFactoryImpl preferencesDialogFactory = new PreferencesDialogFactoryImpl(cyEventHelperServiceRef);
		BookmarkDialogFactoryImpl bookmarkDialogFactory = new BookmarkDialogFactoryImpl(/*bookmarkServiceRef,
                              bookmarksUtilServiceRef,*/ dsManagerServiceRef);
		
		registerService(bc, bookmarkDialogFactory, SessionLoadedListener.class, new Properties());
		
		CytoscapeMenuBar cytoscapeMenuBar = new CytoscapeMenuBar();
		CytoscapeToolBar cytoscapeToolBar = new CytoscapeToolBar();
		CytoscapeMenus cytoscapeMenus = new CytoscapeMenus(cytoscapeMenuBar, cytoscapeToolBar);

		ToolBarEnableUpdater toolBarEnableUpdater = new ToolBarEnableUpdater(cytoscapeToolBar);

		NetworkViewManager networkViewManager = new NetworkViewManager(cyApplicationManagerServiceRef,
		                                                               cyNetworkViewManagerServiceRef, 
		                                                               renderingEngineManagerServiceRef,
		                                                               cytoscapePropertiesServiceRef,
		                                                               cyHelpBroker,
		                                                               visualMappingManagerServiceRef,
		                                                               cyNetworkTableManagerServiceRef);

		BirdsEyeViewHandler birdsEyeViewHandler = new BirdsEyeViewHandler(cyApplicationManagerServiceRef,
		                                                                  cyNetworkViewManagerServiceRef);

		NetworkPanel networkPanel = new NetworkPanel(cyApplicationManagerServiceRef,
		                                             cyNetworkManagerServiceRef,
		                                             cyNetworkViewManagerServiceRef,
		                                             birdsEyeViewHandler,
		                                             dialogTaskManagerServiceRef,
		                                             dynamicTaskFactoryProvisionerServiceRef,
		                                             editNetworkTitleTFServiceRef);

		final IconManagerImpl iconManager = new IconManagerImpl();
		
		CytoscapeDesktop cytoscapeDesktop = new CytoscapeDesktop(cytoscapeMenus,
		                                                         networkViewManager,
		                                                         cytoscapeShutdownServiceRef,
		                                                         cyEventHelperServiceRef,
		                                                         cyServiceRegistrarServiceRef,
		                                                         dialogTaskManagerServiceRef,
		                                                         taskStatusPanelFactoryRef,
		                                                         iconManager);

		CyDesktopManager cyDesktopManager = new CyDesktopManager(cytoscapeDesktop, networkViewManager);

		SynchronousTaskManager<?> synchronousTaskManagerServiceRef = getService(bc, SynchronousTaskManager.class);

		SaveSessionAsTaskFactory saveTaskFactoryServiceRef = getService(bc, SaveSessionAsTaskFactory.class);

		SessionIO sessionIO = new SessionIO();

		SessionHandler sessionHandler = new SessionHandler(cytoscapeDesktop,
														   cyNetworkManagerServiceRef,
														   networkViewManager,
														   synchronousTaskManagerServiceRef,
														   saveTaskFactoryServiceRef,
														   sessionIO,
														   cySessionManagerServiceRef,
														   fileUtilServiceRef,
														   networkPanel);

		PrintAction printAction = new PrintAction(cyApplicationManagerServiceRef, 
		                                          cyNetworkViewManagerServiceRef, 
		                                          cytoscapePropertiesServiceRef);

		ExitAction exitAction = new ExitAction( cytoscapeShutdownServiceRef);

		PreferenceAction preferenceAction = new PreferenceAction(cytoscapeDesktop,
		                                                         preferencesDialogFactory,
		                                                         bookmarksUtilServiceRef);

		BookmarkAction bookmarkAction = new BookmarkAction(cytoscapeDesktop, bookmarkDialogFactory);

		LayoutMenuPopulator layoutMenuPopulator = new LayoutMenuPopulator(cytoscapeMenuBar,
		                                                                  cyApplicationManagerServiceRef, 
		                                                                  dialogTaskManagerServiceRef);

		CytoscapeMenuPopulator cytoscapeMenuPopulator = new CytoscapeMenuPopulator(cytoscapeDesktop,
		                                                                           dialogTaskManagerServiceRef,
		                                                                           panelTaskManagerServiceRef,
		                                                                           cyApplicationManagerServiceRef, 
		                                                                           cyNetworkViewManagerServiceRef,
		                                                                           cyServiceRegistrarServiceRef,
		                                                                           dynamicTaskFactoryProvisionerServiceRef);

		SettingsAction settingsAction = new SettingsAction(cyLayoutsServiceRef, cytoscapeDesktop,
		                                                   cyApplicationManagerServiceRef, 
		                                                   cyNetworkViewManagerServiceRef,
		                                                   panelTaskManagerServiceRef,
		                                                   cytoscapePropertiesServiceRef, 
		                                                   dynamicTaskFactoryProvisionerServiceRef);

		HelpContentsTaskFactory helpContentsTaskFactory = new HelpContentsTaskFactory(cyHelpBroker,
		                                                                              cytoscapeDesktop);
		HelpContactHelpDeskTaskFactory helpContactHelpDeskTaskFactory = new HelpContactHelpDeskTaskFactory(openBrowserServiceRef);
		HelpReportABugTaskFactory helpReportABugTaskFactory = new HelpReportABugTaskFactory(openBrowserServiceRef, cyVersionServiceRef);
		HelpAboutTaskFactory helpAboutTaskFactory = new HelpAboutTaskFactory(cyVersionServiceRef, cytoscapeDesktop);
		ArrangeTaskFactory arrangeGridTaskFactory = new ArrangeTaskFactory(cyDesktopManager, GRID);
		ArrangeTaskFactory arrangeCascadeTaskFactory = new ArrangeTaskFactory(cyDesktopManager,
		                                                                      CASCADE);
		ArrangeTaskFactory arrangeHorizontalTaskFactory = new ArrangeTaskFactory(cyDesktopManager,
		                                                                         HORIZONTAL);
		ArrangeTaskFactory arrangeVerticalTaskFactory = new ArrangeTaskFactory(cyDesktopManager,
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
		                                                               cyNetworkViewManagerServiceRef, 
		                                                               visualMappingManagerServiceRef, rowViewTracker, networkViewManager);
		
		RecentSessionManager recentSessionManager = new RecentSessionManager(recentlyOpenedTrackerServiceRef,
		                                                                     cyServiceRegistrarServiceRef,
		                                                                     cySessionManagerServiceRef,
		                                                                     sessionReaderManagerServiceRef,
		                                                                     cyApplicationManagerServiceRef,
		                                                                     cyNetworkManagerServiceRef,
		                                                                     cyTableManagerServiceRef,
		                                                                     cyNetworkTableManagerServiceRef,
		                                                                     cyGroupManagerServiceRef,
		                                                                     cyEventHelperServiceRef);
		
		registerService(bc, cyHelpBroker, CyHelpBroker.class, new Properties());
		registerService(bc, undoAction, CyAction.class, new Properties());
		registerService(bc, redoAction, CyAction.class, new Properties());
		registerService(bc, printAction, CyAction.class, new Properties());
		registerService(bc, preferenceAction, CyAction.class, new Properties());
		registerService(bc, bookmarkAction, CyAction.class, new Properties());
		registerService(bc, settingsAction, CyAction.class, new Properties());
		registerService(bc, settingsAction, SetCurrentNetworkViewListener.class, new Properties());
		registerService(bc, cytoPanelWestAction, CyAction.class, new Properties());
		registerService(bc, cytoPanelSouthAction, CyAction.class, new Properties());
		registerService(bc, cytoPanelEastAction, CyAction.class, new Properties());
		registerService(bc, cytoPanelSouthWestAction, CyAction.class, new Properties());
		registerService(bc, cyDesktopManager, CyNetworkViewDesktopMgr.class, new Properties());

		Properties helpContentsTaskFactoryProps = new Properties();
		helpContentsTaskFactoryProps.setProperty(PREFERRED_MENU, "Help");
		helpContentsTaskFactoryProps.setProperty(LARGE_ICON_URL, getClass().getResource("/images/ximian/stock_help.png").toString());
		helpContentsTaskFactoryProps.setProperty(TITLE, "Contents...");
		helpContentsTaskFactoryProps.setProperty(MENU_GRAVITY,"1.0");
		helpContentsTaskFactoryProps.setProperty(TOOLTIP, "Show Help Contents...");
		helpContentsTaskFactoryProps.setProperty(TOOL_BAR_GRAVITY, "20.0f");
		helpContentsTaskFactoryProps.setProperty(IN_TOOL_BAR, "true");
		registerService(bc, helpContentsTaskFactory, TaskFactory.class, helpContentsTaskFactoryProps);

		Properties helpContactHelpDeskTaskFactoryProps = new Properties();
		helpContactHelpDeskTaskFactoryProps.setProperty(PREFERRED_MENU, "Help");
		helpContactHelpDeskTaskFactoryProps.setProperty(MENU_GRAVITY,"7.0");	
		helpContactHelpDeskTaskFactoryProps.setProperty(TITLE, "Contact Help Desk...");
		registerService(bc, helpContactHelpDeskTaskFactory, TaskFactory.class,
		                helpContactHelpDeskTaskFactoryProps);

		Properties helpReportABugTaskFactoryProps = new Properties();
		helpReportABugTaskFactoryProps.setProperty(PREFERRED_MENU, "Help");
		helpReportABugTaskFactoryProps.setProperty(TITLE, "Report a Bug...");
		helpReportABugTaskFactoryProps.setProperty(MENU_GRAVITY,"8.0");
		registerService(bc, helpReportABugTaskFactory, TaskFactory.class,
		                helpReportABugTaskFactoryProps);

		
		Properties arrangeGridTaskFactoryProps = new Properties();
		arrangeGridTaskFactoryProps.setProperty(ServiceProperties.ENABLE_FOR, "networkAndView");
		arrangeGridTaskFactoryProps.setProperty(ACCELERATOR,"cmd g");
		arrangeGridTaskFactoryProps.setProperty(PREFERRED_MENU, "View.Arrange Network Windows[8]");
		arrangeGridTaskFactoryProps.setProperty(TITLE, "Grid");
		arrangeGridTaskFactoryProps.setProperty(MENU_GRAVITY, "1.0");
		registerService(bc, arrangeGridTaskFactory, TaskFactory.class, arrangeGridTaskFactoryProps);

		Properties arrangeCascadeTaskFactoryProps = new Properties();
		arrangeCascadeTaskFactoryProps.setProperty(ServiceProperties.ENABLE_FOR, "networkAndView");
		arrangeCascadeTaskFactoryProps.setProperty(PREFERRED_MENU,
		                                           "View.Arrange Network Windows[8]");
		arrangeCascadeTaskFactoryProps.setProperty(TITLE, "Cascade");
		arrangeCascadeTaskFactoryProps.setProperty(MENU_GRAVITY, "2.0");
		registerService(bc, arrangeCascadeTaskFactory, TaskFactory.class,
		                arrangeCascadeTaskFactoryProps);

		Properties arrangeHorizontalTaskFactoryProps = new Properties();
		arrangeHorizontalTaskFactoryProps.setProperty(ServiceProperties.ENABLE_FOR, "networkAndView");
		arrangeHorizontalTaskFactoryProps.setProperty(PREFERRED_MENU,
		                                              "View.Arrange Network Windows[8]");
		arrangeHorizontalTaskFactoryProps.setProperty(TITLE, "Horizontal");
		arrangeHorizontalTaskFactoryProps.setProperty(MENU_GRAVITY, "3.0");
		registerService(bc, arrangeHorizontalTaskFactory, TaskFactory.class,
		                arrangeHorizontalTaskFactoryProps);

		Properties arrangeVerticalTaskFactoryProps = new Properties();
		arrangeVerticalTaskFactoryProps.setProperty(ServiceProperties.ENABLE_FOR, "networkAndView");
		arrangeVerticalTaskFactoryProps.setProperty(PREFERRED_MENU,
		                                            "View.Arrange Network Windows[8]");
		arrangeVerticalTaskFactoryProps.setProperty(TITLE, "Vertical");
		arrangeVerticalTaskFactoryProps.setProperty(MENU_GRAVITY, "4.0");
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
		registerServiceListener(bc, settingsAction, "addLayout", "removeLayout", CyLayoutAlgorithm.class);
		
		// For Network Panel context menu
		registerServiceListener(bc, networkPanel, "addNetworkViewTaskFactory",
		                        "removeNetworkViewTaskFactory", NetworkViewTaskFactory.class, CONTEXT_MENU_FILTER);
		registerServiceListener(bc, networkPanel, "addNetworkTaskFactory",
		                        "removeNetworkTaskFactory", NetworkTaskFactory.class, CONTEXT_MENU_FILTER);
		registerServiceListener(bc, networkPanel, "addNetworkViewCollectionTaskFactory",
		                        "removeNetworkViewCollectionTaskFactory",
		                        NetworkViewCollectionTaskFactory.class, CONTEXT_MENU_FILTER);
		registerServiceListener(bc, networkPanel, "addNetworkCollectionTaskFactory",
		                        "removeNetworkCollectionTaskFactory",
		                        NetworkCollectionTaskFactory.class, CONTEXT_MENU_FILTER);
		
		registerServiceListener(bc, configDirPropertyWriter, "addCyProperty", "removeCyProperty",
		                        CyProperty.class);
		registerServiceListener(bc, layoutMenuPopulator, "addLayout", "removeLayout",
		                        CyLayoutAlgorithm.class);

		if (isMac()) {
			new MacCyActivator().start(bc);
		} else {
			Properties helpAboutTaskFactoryProps = new Properties();
			helpAboutTaskFactoryProps.setProperty(PREFERRED_MENU, "Help");
			helpAboutTaskFactoryProps.setProperty(TITLE, "About...");
			helpAboutTaskFactoryProps.setProperty(MENU_GRAVITY,"10.0");

			registerService(bc, helpAboutTaskFactory, TaskFactory.class, helpAboutTaskFactoryProps);
			
			registerService(bc, exitAction, CyAction.class, new Properties());
		}

		// Full screen actions.  This is platform dependent
		FullScreenAction fullScreenAction = null;
		if(isMac()) {
			if (MacFullScreenEnabler.supportsNativeFullScreenMode()) {
				fullScreenAction = new FullScreenMacAction(cytoscapeDesktop);
			} else {
				fullScreenAction = new FullScreenAction(cytoscapeDesktop);
			}
		} else {
			fullScreenAction = new FullScreenAction(cytoscapeDesktop);
		}
		registerService(bc, fullScreenAction, CyAction.class, new Properties());
		
	}

	private void setLookAndFeel() {
		Logger logger = LoggerFactory.getLogger(getClass());
		String lookAndFeel;
		// update look and feel
		if (System.getProperty("os.name").startsWith("Mac OS X") ||
		    System.getProperty("os.name").startsWith("Windows"))
			lookAndFeel = UIManager.getSystemLookAndFeelClassName();
		else {
			// Use Nimbus on Unix systems
			lookAndFeel = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
		}
		try {
			logger.debug("setting look and feel to: " + lookAndFeel);
			UIManager.setLookAndFeel(lookAndFeel);
		} catch (ClassNotFoundException e) {
			logger.error("Unexpected error", e);
		} catch (InstantiationException e) {
			logger.error("Unexpected error", e);
		} catch (IllegalAccessException e) {
			logger.error("Unexpected error", e);
		} catch (UnsupportedLookAndFeelException e) {
			logger.error("Unexpected error", e);
		}
	}

	private boolean isMac() {
		return System.getProperty("os.name").startsWith("Mac OS X");
	}
}
