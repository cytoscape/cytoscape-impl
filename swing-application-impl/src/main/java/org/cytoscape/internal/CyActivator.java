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

import static org.cytoscape.application.swing.CyNetworkViewDesktopMgr.ArrangeType.CASCADE;
import static org.cytoscape.application.swing.CyNetworkViewDesktopMgr.ArrangeType.GRID;
import static org.cytoscape.application.swing.CyNetworkViewDesktopMgr.ArrangeType.HORIZONTAL;
import static org.cytoscape.application.swing.CyNetworkViewDesktopMgr.ArrangeType.VERTICAL;
import static org.cytoscape.application.swing.CytoPanelName.EAST;
import static org.cytoscape.application.swing.CytoPanelName.SOUTH;
import static org.cytoscape.application.swing.CytoPanelName.SOUTH_WEST;
import static org.cytoscape.application.swing.CytoPanelName.WEST;
import static org.cytoscape.work.ServiceProperties.ACCELERATOR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;
import static org.cytoscape.work.ServiceProperties.TOOLTIP;

import java.awt.Color;
import java.awt.Font;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyShutdown;
import org.cytoscape.application.CyVersion;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CyHelpBroker;
import org.cytoscape.application.swing.CyNetworkViewDesktopMgr;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.ToolBarComponent;
import org.cytoscape.event.CyEventHelper;
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
import org.cytoscape.internal.layout.ui.LayoutSettingsManager;
import org.cytoscape.internal.layout.ui.SettingsAction;
import org.cytoscape.internal.select.RowViewTracker;
import org.cytoscape.internal.select.RowsSetViewUpdater;
import org.cytoscape.internal.select.SelectEdgeViewUpdater;
import org.cytoscape.internal.select.SelectNodeViewUpdater;
import org.cytoscape.internal.shutdown.ConfigDirPropertyWriter;
import org.cytoscape.internal.undo.RedoAction;
import org.cytoscape.internal.undo.UndoAction;
import org.cytoscape.internal.util.HSLColor;
import org.cytoscape.internal.util.undo.UndoMonitor;
import org.cytoscape.internal.view.BirdsEyeViewHandler;
import org.cytoscape.internal.view.CyDesktopManager;
import org.cytoscape.internal.view.CyHelpBrokerImpl;
import org.cytoscape.internal.view.CytoscapeDesktop;
import org.cytoscape.internal.view.CytoscapeMenuBar;
import org.cytoscape.internal.view.CytoscapeMenuPopulator;
import org.cytoscape.internal.view.CytoscapeMenus;
import org.cytoscape.internal.view.CytoscapeToolBar;
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
import org.cytoscape.model.events.NetworkDestroyedListener;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.task.DynamicTaskFactoryProvisioner;
import org.cytoscape.task.NetworkCollectionTaskFactory;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewCollectionTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.NetworkViewDestroyedListener;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.properties.TunablePropertySerializerFactory;
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

	@Override
	@SuppressWarnings("unchecked")
	public void start(BundleContext bc) throws Exception {
		CyProperty<Properties> cytoscapePropertiesServiceRef = getService(bc, CyProperty.class, "(cyPropertyName=cytoscape3.props)");
		
		setLookAndFeel(cytoscapePropertiesServiceRef.getProperties());
		
		CyShutdown cytoscapeShutdownServiceRef = getService(bc, CyShutdown.class);
		CyApplicationConfiguration cyApplicationConfigurationServiceRef = getService(bc, CyApplicationConfiguration.class);
		CyVersion cyVersionServiceRef = getService(bc, CyVersion.class);
		CyApplicationManager cyApplicationManagerServiceRef = getService(bc, CyApplicationManager.class);
		CyNetworkViewManager cyNetworkViewManagerServiceRef = getService(bc, CyNetworkViewManager.class);
		DialogTaskManager dialogTaskManagerServiceRef = getService(bc, DialogTaskManager.class);
		PanelTaskManager panelTaskManagerServiceRef = getService(bc, PanelTaskManager.class);
		TaskStatusPanelFactory taskStatusPanelFactoryRef = getService(bc, TaskStatusPanelFactory.class);
		CyColumnIdentifierFactory cyColumnIdentifierFactory = getService(bc, CyColumnIdentifierFactory.class);
		IconManager iconManagerServiceRef = getService(bc, IconManager.class);
		BookmarksUtil bookmarksUtilServiceRef = getService(bc, BookmarksUtil.class);
		CyLayoutAlgorithmManager cyLayoutsServiceRef = getService(bc, CyLayoutAlgorithmManager.class);
		SwingUndoSupport undoSupportServiceRef = getService(bc, SwingUndoSupport.class);
		CyEventHelper cyEventHelperServiceRef = getService(bc, CyEventHelper.class);
		CyServiceRegistrar cyServiceRegistrarServiceRef = getService(bc, CyServiceRegistrar.class);
		OpenBrowser openBrowserServiceRef = getService(bc, OpenBrowser.class);
		VisualMappingManager visualMappingManagerServiceRef  = getService(bc, VisualMappingManager.class);
		DynamicTaskFactoryProvisioner dynamicTaskFactoryProvisionerServiceRef = getService(bc, DynamicTaskFactoryProvisioner.class);
		DataSourceManager dsManagerServiceRef = getService(bc, DataSourceManager.class);
		TunablePropertySerializerFactory tunablePropertySerializerFactoryRef =  getService(bc, TunablePropertySerializerFactory.class);
		
		//////////////		
		UndoAction undoAction = new UndoAction(undoSupportServiceRef);
		RedoAction redoAction = new RedoAction(undoSupportServiceRef);
		ConfigDirPropertyWriter configDirPropertyWriter = new ConfigDirPropertyWriter(dialogTaskManagerServiceRef,
		                                                                              cyApplicationConfigurationServiceRef);
		CyHelpBrokerImpl cyHelpBroker = new CyHelpBrokerImpl();
		PreferencesDialogFactoryImpl preferencesDialogFactory = new PreferencesDialogFactoryImpl(cyEventHelperServiceRef);
		BookmarkDialogFactoryImpl bookmarkDialogFactory = new BookmarkDialogFactoryImpl(dsManagerServiceRef);
		
		registerService(bc, bookmarkDialogFactory, SessionLoadedListener.class, new Properties());
		
		CytoscapeMenuBar cytoscapeMenuBar = new CytoscapeMenuBar();
		CytoscapeToolBar cytoscapeToolBar = new CytoscapeToolBar();
		CytoscapeMenus cytoscapeMenus = new CytoscapeMenus(cytoscapeMenuBar, cytoscapeToolBar);

		ToolBarEnableUpdater toolBarEnableUpdater =
				new ToolBarEnableUpdater(cytoscapeToolBar, cyServiceRegistrarServiceRef);

		NetworkViewManager networkViewManager = new NetworkViewManager(cyHelpBroker, cyServiceRegistrarServiceRef);

		BirdsEyeViewHandler birdsEyeViewHandler = new BirdsEyeViewHandler(cyApplicationManagerServiceRef,
		                                                                  cyNetworkViewManagerServiceRef);

		NetworkPanel networkPanel = new NetworkPanel(birdsEyeViewHandler, cyServiceRegistrarServiceRef);

		CytoscapeDesktop cytoscapeDesktop = new CytoscapeDesktop(cytoscapeMenus,
		                                                         networkViewManager,
		                                                         cytoscapeShutdownServiceRef,
		                                                         cyEventHelperServiceRef,
		                                                         cyServiceRegistrarServiceRef,
		                                                         dialogTaskManagerServiceRef,
		                                                         taskStatusPanelFactoryRef,
		                                                         iconManagerServiceRef);

		CyDesktopManager cyDesktopManager = new CyDesktopManager(cytoscapeDesktop, networkViewManager);

		SessionIO sessionIO = new SessionIO();

		SessionHandler sessionHandler = new SessionHandler(cytoscapeDesktop, networkViewManager, sessionIO,
				networkPanel, cyServiceRegistrarServiceRef);

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

		LayoutSettingsManager layoutSettingsManager = new LayoutSettingsManager(cyServiceRegistrarServiceRef, tunablePropertySerializerFactoryRef);
		
		SettingsAction settingsAction = new SettingsAction(cyLayoutsServiceRef, cytoscapeDesktop,
		                                                   cyApplicationManagerServiceRef, 
		                                                   layoutSettingsManager,
		                                                   cyNetworkViewManagerServiceRef,
		                                                   panelTaskManagerServiceRef,
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
		                                                               visualMappingManagerServiceRef,
		                                                               rowViewTracker,
		                                                               networkViewManager,
		                                                               cyColumnIdentifierFactory);
		
		RecentSessionManager recentSessionManager = new RecentSessionManager(cyServiceRegistrarServiceRef);
		
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
		helpContentsTaskFactoryProps.setProperty(TITLE, "Contents...");
		helpContentsTaskFactoryProps.setProperty(MENU_GRAVITY,"1.0");
		helpContentsTaskFactoryProps.setProperty(TOOLTIP, "Show Help Contents...");
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
		registerServiceListener(bc, layoutSettingsManager, "addLayout", "removeLayout", CyLayoutAlgorithm.class);
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

		if (LookAndFeelUtil.isMac()) {
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
		
		if (LookAndFeelUtil.isMac()) {
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

	private void setLookAndFeel(final Properties props) {
		Logger logger = LoggerFactory.getLogger(getClass());
		
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					@Override
					public void run() {
						setLookAndFeel(props);
					}
				});
				return;
			} catch (InterruptedException e) {
				logger.error("Unexpected error", e);
			} catch (InvocationTargetException e) {
				logger.error("Unexpected error", e);
			}
		}
		
		// Update look and feel
		String lookAndFeel = props.getProperty("lookAndFeel");
		
		if (lookAndFeel == null) {
			if (LookAndFeelUtil.isMac() || LookAndFeelUtil.isWindows())
				lookAndFeel = UIManager.getSystemLookAndFeelClassName();
			else // Use Nimbus on *nix systems
				lookAndFeel = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
		}
		
		try {
			logger.debug("Setting look and feel to: " + lookAndFeel);
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
		
		try {
			if (UIManager.getFont("Label.font") == null)
				UIManager.put("Label.font", new JLabel().getFont());
			
			Color tsb = UIManager.getColor("Table.selectionBackground");
			if (tsb == null) tsb = UIManager.getColor("Tree.selectionBackground");
			if (tsb == null) tsb = UIManager.getColor("Table[Enabled+Selected].textBackground");
			
			if (tsb != null) {
				HSLColor hsl = new HSLColor(tsb);
				tsb = hsl.adjustLuminance(LookAndFeelUtil.isAquaLAF() ? 94.0f : 90.0f);
			}
			
			final Color TABLE_SELECTION_BG = tsb != null && !tsb.equals(Color.WHITE) ? tsb : new Color(222, 234, 252);
			
			UIManager.put("Table.focusCellBackground", UIManager.getColor("Tree.selectionBackground"));
			UIManager.put("Table.focusCellForeground", UIManager.getColor("Tree.selectionForeground"));
			UIManager.put("Table.selectionBackground", TABLE_SELECTION_BG);
			UIManager.put("Table.selectionForeground", UIManager.getColor("Table.foreground"));
			
			final Font TABLE_FONT = UIManager.getFont("Label.font").deriveFont(11.0f);
			
			if (LookAndFeelUtil.isAquaLAF()) {
				// Mac OS X + Aqua:
				UIManager.put(
						"TableHeader.cellBorder",
						BorderFactory.createCompoundBorder(
								BorderFactory.createMatteBorder(0, 0, 1, 0, UIManager.getColor("Separator.foreground")),
								BorderFactory.createCompoundBorder(
										BorderFactory.createEmptyBorder(2, 0, 2, 0),
										BorderFactory.createCompoundBorder(
												BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Separator.foreground")),
												BorderFactory.createEmptyBorder(0, 4, 0, 4)
										)
								)
						)
				);
				UIManager.put("TableHeader.background", new Color(244, 244, 244));
				UIManager.put("Table.gridColor", UIManager.getColor("Table.background"));
				UIManager.put("Table.font", TABLE_FONT);
				UIManager.put("Tree.font", TABLE_FONT);
			} else if (LookAndFeelUtil.isWindows()) {
				// Windows:
				UIManager.put(
						"TableHeader.cellBorder", 
						BorderFactory.createCompoundBorder(
								BorderFactory.createEmptyBorder(2, 0, 6, 0),
								BorderFactory.createCompoundBorder(
										BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(223, 223, 223)),
										BorderFactory.createEmptyBorder(2, 6, 2, 6)
								)
						)
				);
				UIManager.put("TableHeader.background", UIManager.getColor("Table.background"));
				UIManager.put("Table.gridColor", UIManager.getColor("Table.background"));
				UIManager.put("Separator.foreground", new Color(208, 208, 208));
				UIManager.put("Focus.color", UIManager.getColor("TextField.selectionBackground"));
			} else if (LookAndFeelUtil.isNimbusLAF()) {
				// Nimbus (usually Linux)
				// Translating Nimbus default colors to more standard UIManager keys
				// in order to make it easier for Cytoscape to reuse the LAF colors.
				// Also fixes inconsistent colors since the latest Java 8 version.
				// (http://docs.oracle.com/javase/tutorial/uiswing/lookandfeel/_nimbusDefaults.html)
				UIManager.put("nimbusLightBackground", Color.WHITE);
				UIManager.put("nimbusDisabledText", new Color(142, 143, 145));
				
				// Make all table rows white, like the other LAFs
				UIManager.put("Table.background", Color.WHITE);
				UIManager.put("Table.alternateRowColor", Color.WHITE);
				UIManager.put("Table:\"Table.cellRenderer\".background", Color.WHITE);
				
				UIManager.put("Table.showGrid", true);
				UIManager.put("Table.gridColor", new Color(242, 242, 242));
				UIManager.put("Table.disabledText", UIManager.getColor("nimbusDisabledText"));
				UIManager.put("Table.disabledForeground", UIManager.getColor("nimbusDisabledText"));
				UIManager.put("Table.font", TABLE_FONT);
				UIManager.put("Tree.font", TABLE_FONT);
				
				UIManager.put("Separator.foreground", UIManager.getColor("nimbusBorder"));
				UIManager.put("TextField.inactiveForeground", UIManager.getColor("nimbusDisabledText"));
				UIManager.put("TextField.disabledForeground", UIManager.getColor("nimbusDisabledText"));
				UIManager.put("Label.disabledForeground", UIManager.getColor("nimbusDisabledText"));
				UIManager.put("Button.disabledForeground", UIManager.getColor("nimbusDisabledText"));
				UIManager.put("Button.disabledText", UIManager.getColor("nimbusDisabledText"));
				UIManager.put("Focus.color", UIManager.getColor("nimbusFocus"));
				UIManager.put("TextField.selectionBackground", UIManager.getColor("nimbusSelectionBackground"));
				UIManager.put(
						"TableHeader.cellBorder", 
						BorderFactory.createCompoundBorder(
								BorderFactory.createMatteBorder(0, 0, 1, 1, UIManager.getColor("nimbusBorder")),
								BorderFactory.createEmptyBorder(2, 4, 2, 4)
						)
				);
				
				// JList has ugly inconsistent selection colors in the latest Java 8 version
				UIManager.getLookAndFeelDefaults().put("List[Selected].textBackground", new Color(57, 105, 138));
				UIManager.getLookAndFeelDefaults().put("List[Selected].textForeground", Color.WHITE);
			} else if (UIManager.getLookAndFeel() != null && "GTK".equals(UIManager.getLookAndFeel().getID())) {
				// GTK (usually Linux):
				UIManager.put(
						"TableHeader.cellBorder", 
						BorderFactory.createCompoundBorder(
								BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Separator.foreground")),
								BorderFactory.createEmptyBorder(2, 4, 2, 4)
						)
				);
				UIManager.put("TableHeader.background", UIManager.getColor("Table.background"));
			}
		} catch (Exception e) {
			logger.error("Unexpected error", e);
		}
		
		// Cytoscape Palette (http://paletton.com/#uid=70F2h0krdtngVCclWv9tYnSyeiT)
		UIManager.put("CyColor.primary(-2)", new Color(150, 83, 0));    // Darkest
		UIManager.put("CyColor.primary(-1)", new Color(190, 110, 12));  // Darker
		UIManager.put("CyColor.primary(0)",  new Color(234, 145, 35));  // Base color
		UIManager.put("CyColor.primary",     new Color(234, 145, 35));  // Just an alias for (0)
		UIManager.put("CyColor.primary(+1)", new Color(248, 172, 78));  // Brighter
		UIManager.put("CyColor.primary(+2)", new Color(255, 194, 120)); // Brightest

		UIManager.put("CyColor.secondary1(-2)", new Color(0, 110, 37));
		UIManager.put("CyColor.secondary1(-1)", new Color(9, 139, 53));
		UIManager.put("CyColor.secondary1(0)",  new Color(26, 171, 75));
		UIManager.put("CyColor.secondary1",     new Color(26, 171, 75));
		UIManager.put("CyColor.secondary1(+1)", new Color(57, 182, 99));
		UIManager.put("CyColor.secondary1(+2)", new Color(95, 202, 131));

		UIManager.put("CyColor.secondary2(-2)", new Color(150, 23, 0));
		UIManager.put("CyColor.secondary2(-1)", new Color(190, 40, 12));
		UIManager.put("CyColor.secondary2(0)",  new Color(234, 66, 35));
		UIManager.put("CyColor.secondary2",     new Color(234, 66, 35));
		UIManager.put("CyColor.secondary2(+1)", new Color(248, 105, 78));
		UIManager.put("CyColor.secondary2(+2)", new Color(255, 141, 120));

		UIManager.put("CyColor.complement(-2)", new Color(5, 62, 96));
		UIManager.put("CyColor.complement(-1)", new Color(14, 81, 121));
		UIManager.put("CyColor.complement(0)",  new Color(29, 105, 149));
		UIManager.put("CyColor.complement",     new Color(29, 105, 149));
		UIManager.put("CyColor.complement(+1)", new Color(56, 120, 158));
		UIManager.put("CyColor.complement(+2)", new Color(92, 149, 183));
	}
}
