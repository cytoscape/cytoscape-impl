package org.cytoscape.internal;

import static org.cytoscape.application.swing.ActionEnableSupport.ENABLE_FOR_NETWORK_AND_VIEW;
import static org.cytoscape.application.swing.CyNetworkViewDesktopMgr.ArrangeType.CASCADE;
import static org.cytoscape.application.swing.CyNetworkViewDesktopMgr.ArrangeType.GRID;
import static org.cytoscape.application.swing.CyNetworkViewDesktopMgr.ArrangeType.HORIZONTAL;
import static org.cytoscape.application.swing.CyNetworkViewDesktopMgr.ArrangeType.VERTICAL;
import static org.cytoscape.internal.util.IconUtil.COLORS_3;
import static org.cytoscape.internal.util.IconUtil.CY_FONT_NAME;
import static org.cytoscape.internal.util.IconUtil.LAYERED_NEW_FROM_SELECTED;
import static org.cytoscape.internal.view.util.ViewUtil.invokeOnEDTAndWait;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.isMac;
import static org.cytoscape.util.swing.LookAndFeelUtil.isNimbusLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.isWinLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.isWindows;
import static org.cytoscape.work.ServiceProperties.ACCELERATOR;
import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_EXAMPLE_JSON;
import static org.cytoscape.work.ServiceProperties.COMMAND_LONG_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.COMMAND_SUPPORTS_JSON;
import static org.cytoscape.work.ServiceProperties.IN_NETWORK_PANEL_CONTEXT_MENU;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;
import static org.cytoscape.work.ServiceProperties.TOOLTIP;

import java.awt.Color;
import java.awt.Dimension;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CyColumnPresentation;
import org.cytoscape.application.swing.CyColumnPresentationManager;
import org.cytoscape.application.swing.CyHelpBroker;
import org.cytoscape.application.swing.CyNetworkViewDesktopMgr;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.ToolBarComponent;
import org.cytoscape.application.swing.search.NetworkSearchTaskFactory;
import org.cytoscape.internal.actions.AboutAction;
import org.cytoscape.internal.actions.BookmarkAction;
import org.cytoscape.internal.actions.CheckForUpdatesAction;
import org.cytoscape.internal.actions.CloseWindowAction;
import org.cytoscape.internal.actions.CreateNetworkViewsAction;
import org.cytoscape.internal.actions.CytoPanelAction;
import org.cytoscape.internal.actions.DestroyNetworkViewsAction;
import org.cytoscape.internal.actions.DestroyNetworksAction;
import org.cytoscape.internal.actions.DetachedViewToolBarAction;
import org.cytoscape.internal.actions.ExitAction;
import org.cytoscape.internal.actions.ExportImageAction;
import org.cytoscape.internal.actions.ExportNetworkAction;
import org.cytoscape.internal.actions.FullScreenAction;
import org.cytoscape.internal.actions.NewNetworkFromSelectionAction;
import org.cytoscape.internal.actions.PreferenceAction;
import org.cytoscape.internal.actions.PrintAction;
import org.cytoscape.internal.actions.RecentSessionManager;
import org.cytoscape.internal.actions.StarterPanelAction;
import org.cytoscape.internal.command.CommandToolPanel;
import org.cytoscape.internal.command.PauseCommandTaskFactory;
import org.cytoscape.internal.dialogs.BookmarkDialogFactory;
import org.cytoscape.internal.dialogs.PreferencesDialogFactory;
import org.cytoscape.internal.io.SessionIO;
import org.cytoscape.internal.layout.ui.LayoutMenuPopulator;
import org.cytoscape.internal.layout.ui.LayoutSettingsManager;
import org.cytoscape.internal.layout.ui.SettingsAction;
import org.cytoscape.internal.model.RootNetworkManager;
import org.cytoscape.internal.select.RowViewTracker;
import org.cytoscape.internal.select.RowsSetViewUpdater;
import org.cytoscape.internal.select.SelectEdgeViewUpdater;
import org.cytoscape.internal.select.SelectNodeViewUpdater;
import org.cytoscape.internal.shutdown.ConfigDirPropertyWriter;
import org.cytoscape.internal.tunable.CyPropertyConfirmation;
import org.cytoscape.internal.tunable.CyPropertyConfirmationHandler;
import org.cytoscape.internal.undo.RedoAction;
import org.cytoscape.internal.undo.UndoAction;
import org.cytoscape.internal.util.HSLColor;
import org.cytoscape.internal.util.undo.UndoMonitor;
import org.cytoscape.internal.view.CyColumnPresentationManagerImpl;
import org.cytoscape.internal.view.CyDesktopManager;
import org.cytoscape.internal.view.CyHelpBrokerImpl;
import org.cytoscape.internal.view.CytoPanelNameInternal;
import org.cytoscape.internal.view.CytoscapeDesktop;
import org.cytoscape.internal.view.CytoscapeMenuBar;
import org.cytoscape.internal.view.CytoscapeMenuPopulator;
import org.cytoscape.internal.view.CytoscapeMenus;
import org.cytoscape.internal.view.CytoscapeToolBar;
import org.cytoscape.internal.view.GridViewToggleModel;
import org.cytoscape.internal.view.NetworkMainPanel;
import org.cytoscape.internal.view.NetworkMediator;
import org.cytoscape.internal.view.NetworkSearchBar;
import org.cytoscape.internal.view.NetworkSearchMediator;
import org.cytoscape.internal.view.NetworkSelectionMediator;
import org.cytoscape.internal.view.NetworkViewMainPanel;
import org.cytoscape.internal.view.NetworkViewMediator;
import org.cytoscape.internal.view.ToolBarEnableUpdater;
import org.cytoscape.internal.view.help.ArrangeTaskFactory;
import org.cytoscape.internal.view.help.HelpContactHelpDeskTaskFactory;
import org.cytoscape.internal.view.help.HelpReportABugTaskFactory;
import org.cytoscape.internal.view.help.HelpTourTaskFactory;
import org.cytoscape.internal.view.help.HelpTutorialsTaskFactory;
import org.cytoscape.internal.view.help.HelpUserManualTaskFactory;
import org.cytoscape.internal.view.help.HelpVideosTaskFactory;
import org.cytoscape.internal.view.util.ViewUtil;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkDestroyedListener;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeLoadedListener;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.task.NetworkCollectionTaskFactory;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewCollectionTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.RootNetworkCollectionTaskFactory;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.NetworkViewDestroyedListener;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.GUITunableHandlerFactory;
import org.cytoscape.work.swing.SimpleGUITunableHandlerFactory;
import org.jdesktop.swingx.color.ColorUtil;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class CyActivator extends AbstractCyActivator {
	
	private static int LARGE_ICON_SIZE = 32;
	
	private static final String CONTEXT_MENU_FILTER = "(" + ServiceProperties.IN_NETWORK_PANEL_CONTEXT_MENU + "=true)";
	
	private static final String HELP_MENU = "Help";
	private static final String ARRANGE_VIEWS_MENU = "View.Arrange Detached Views[8]";
	
	private static Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	private CytoscapeMenus cytoscapeMenus;
	private ToolBarEnableUpdater toolBarEnableUpdater;
	
	private NetworkSearchBar netSearchBar;
	private NetworkSearchMediator netSearchMediator;
	private NetworkMainPanel netMainPanel;
	private NetworkMediator netMediator;
	private ViewComparator viewComparator;
	private GridViewToggleModel gridViewToggleModel;
	private NetworkViewMainPanel netViewMainPanel;
	private NetworkViewMediator netViewMediator;
	private CytoscapeDesktop cytoscapeDesktop;
	private CommandToolPanel commandToolPanel;
	
	private SessionHandler sessionHandler;
	
	private LayoutMenuPopulator layoutMenuPopulator;
	private CytoscapeMenuPopulator cytoscapeMenuPopulator;
	private LayoutSettingsManager layoutSettingsManager;
	
	private HelpUserManualTaskFactory helpUserManualTaskFactory;
	private HelpTourTaskFactory helpTourTaskFactory;
	private HelpTutorialsTaskFactory helpTutorialsTaskFactory;
	private HelpVideosTaskFactory helpVideosTaskFactory;
	private HelpContactHelpDeskTaskFactory helpContactHelpDeskTaskFactory;
	private HelpReportABugTaskFactory helpReportABugTaskFactory;
	
	private CyDesktopManager cyDesktopManager;
	
	private ArrangeTaskFactory arrangeGridTaskFactory;
	private ArrangeTaskFactory arrangeCascadeTaskFactory;
	private ArrangeTaskFactory arrangeHorizontalTaskFactory;
	private ArrangeTaskFactory arrangeVerticalTaskFactory;
	
	private PreferencesDialogFactory preferencesDialogFactory;
	private BookmarkDialogFactory bookmarkDialogFactory;
	
	private UndoMonitor undoMonitor;
	private RowViewTracker rowViewTracker;
	private SelectEdgeViewUpdater selecteEdgeViewUpdater;
	private SelectNodeViewUpdater selecteNodeViewUpdater;
	private CyColumnPresentationManagerImpl columnPresentationManager;
	
	private RowsSetViewUpdater rowsSetViewUpdater;
	
	private RecentSessionManager recentSessionManager;
	private NetworkSelectionMediator netSelectionMediator;
	
	///// CyActions ////
	private UndoAction undoAction;
	private RedoAction redoAction;
	
	private PrintAction printAction;
	private ExitAction exitAction;
	private PreferenceAction preferenceAction;
	private BookmarkAction bookmarkAction;
	private SettingsAction settingsAction;
	
	private CytoPanelAction cytoPanelWestAction;
	private CytoPanelAction cytoPanelSouthAction;
	private CytoPanelAction cytoPanelEastAction;
	private CytoPanelAction cytoPanelSouthWestAction;
	private CytoPanelAction cytoPanelCommandAction;

	private NewNetworkFromSelectionAction newNetworkFromSelectionAction;
	
	private StarterPanelAction starterPanelActionMenu;
	private StarterPanelAction starterPanelActionToolBar;
	private DetachedViewToolBarAction detachedViewToolBarAction;
	private CloseWindowAction closeWindowAction;
	private CreateNetworkViewsAction createNetworkViewsAction;
	private DestroyNetworkViewsAction destroyNetworkViewsAction;
	private DestroyNetworksAction destroyNetworksAction;
	private ExportNetworkAction exportNetworkAction;
	private ExportImageAction exportImageAction;

	// Show Welcome Screen
	private CheckForUpdatesAction welcomeScreenAction;


	@Override
	public void start(BundleContext bc) throws Exception {
		var serviceRegistrar = getService(bc, CyServiceRegistrar.class);
		
		invokeOnEDTAndWait(() -> {
			setLookAndFeel(bc);
		}, logger);
		
		//////////////
		ConfigDirPropertyWriter configDirPropertyWriter = new ConfigDirPropertyWriter(serviceRegistrar);
		registerService(bc, configDirPropertyWriter, CyShutdownListener.class);
		
		CyHelpBrokerImpl cyHelpBroker = new CyHelpBrokerImpl();
		registerService(bc, cyHelpBroker, CyHelpBroker.class);
		registerServiceListener(bc, configDirPropertyWriter::addCyProperty, configDirPropertyWriter::removeCyProperty, CyProperty.class);
		
		SimpleGUITunableHandlerFactory<CyPropertyConfirmationHandler> cyPropConfirmHandlerFactory =
				new SimpleGUITunableHandlerFactory<>(CyPropertyConfirmationHandler.class, CyPropertyConfirmation.class);
		registerService(bc, cyPropConfirmHandlerFactory, GUITunableHandlerFactory.class);
		
		invokeOnEDTAndWait(() -> {
			initComponents(bc, serviceRegistrar);
		});
		
		registerService(bc, undoAction, CyAction.class);
		registerService(bc, redoAction, CyAction.class);
		registerService(bc, printAction, CyAction.class);
		registerService(bc, preferenceAction, CyAction.class);
		registerService(bc, bookmarkAction, CyAction.class);
		registerService(bc, settingsAction, CyAction.class);
		registerService(bc, settingsAction, SetCurrentNetworkViewListener.class);
		registerService(bc, cytoPanelWestAction, CyAction.class);
		registerService(bc, cytoPanelSouthAction, CyAction.class);
		registerService(bc, cytoPanelEastAction, CyAction.class);
		registerService(bc, cytoPanelSouthWestAction, CyAction.class);
		registerService(bc, cytoPanelCommandAction, CyAction.class);
		registerService(bc, starterPanelActionMenu, CyAction.class);
		registerService(bc, starterPanelActionToolBar, CyAction.class);
		registerService(bc, detachedViewToolBarAction, CyAction.class);
		registerService(bc, closeWindowAction, CyAction.class);
		registerService(bc, newNetworkFromSelectionAction, CyAction.class);
		
		registerService(bc, cyDesktopManager, CyNetworkViewDesktopMgr.class);
		
		registerService(bc, bookmarkDialogFactory, SessionLoadedListener.class);

		{
			Properties props = new Properties();
			props.setProperty(PREFERRED_MENU, HELP_MENU);
			props.setProperty(TITLE, "User Manual");
			props.setProperty(MENU_GRAVITY, "1.0");
			props.setProperty(TOOLTIP, "Show User Manual");
			registerService(bc, helpUserManualTaskFactory, TaskFactory.class, props);
		}
		{
			Properties props = new Properties();
			props.setProperty(PREFERRED_MENU, HELP_MENU);
			props.setProperty(TITLE, "Video Demos");
			props.setProperty(MENU_GRAVITY, "1.1");
			props.setProperty(TOOLTIP, "Show Demo Videos");
			registerService(bc, helpVideosTaskFactory, TaskFactory.class, props);
		}
		{
			Properties props = new Properties();
			props.setProperty(PREFERRED_MENU, HELP_MENU);
			props.setProperty(TITLE, "Tour");
			props.setProperty(MENU_GRAVITY, "1.2");
			registerService(bc, helpTourTaskFactory, TaskFactory.class, props);
		}
		{
			Properties props = new Properties();
			props.setProperty(PREFERRED_MENU, HELP_MENU);
			props.setProperty(TITLE, "Tutorials");
			props.setProperty(MENU_GRAVITY, "1.5");
			props.setProperty(TOOLTIP, "Show Tutorials");
			registerService(bc, helpTutorialsTaskFactory, TaskFactory.class, props);
		}
		{
			Properties props = new Properties();
			props.setProperty(PREFERRED_MENU, HELP_MENU);
			props.setProperty(MENU_GRAVITY, "7.0");
			props.setProperty(TITLE, "Links for Help");
			registerService(bc, helpContactHelpDeskTaskFactory, TaskFactory.class, props);
		}
		{
			Properties props = new Properties();
			props.setProperty(PREFERRED_MENU, HELP_MENU);
			props.setProperty(TITLE, "Report a Bug");
			props.setProperty(MENU_GRAVITY, "8.0");
			registerService(bc, helpReportABugTaskFactory, TaskFactory.class, props);
		}
		{
			Properties props = new Properties();
			props.setProperty(ServiceProperties.ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
			props.setProperty(ACCELERATOR, "cmd g");
			props.setProperty(PREFERRED_MENU, ARRANGE_VIEWS_MENU);
			props.setProperty(TITLE, "Grid");
			props.setProperty(MENU_GRAVITY, "1.0");
			registerService(bc, arrangeGridTaskFactory, TaskFactory.class, props);
		}
		{
			Properties props = new Properties();
			props.setProperty(ServiceProperties.ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
			props.setProperty(PREFERRED_MENU, ARRANGE_VIEWS_MENU);
			props.setProperty(TITLE, "Cascade");
			props.setProperty(MENU_GRAVITY, "2.0");
			registerService(bc, arrangeCascadeTaskFactory, TaskFactory.class, props);
		}
		{
			Properties props = new Properties();
			props.setProperty(ServiceProperties.ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
			props.setProperty(PREFERRED_MENU, ARRANGE_VIEWS_MENU);
			props.setProperty(TITLE, "Vertical Stack");
			props.setProperty(MENU_GRAVITY, "3.0");
			registerService(bc, arrangeHorizontalTaskFactory, TaskFactory.class, props);
		}
		{
			Properties props = new Properties();
			props.setProperty(ServiceProperties.ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
			props.setProperty(PREFERRED_MENU, ARRANGE_VIEWS_MENU);
			props.setProperty(TITLE, "Side by Side");
			props.setProperty(MENU_GRAVITY, "4.0");
			registerService(bc, arrangeVerticalTaskFactory, TaskFactory.class, props);
		}
		{
			Properties props = new Properties();
			props.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU, "true");
			registerAllServices(bc, createNetworkViewsAction, props);
		}
		{
			Properties props = new Properties();
			props.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU, "true");
			registerAllServices(bc, destroyNetworkViewsAction, props);
		}
		{
			Properties props = new Properties();
			props.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU, "true");
			registerAllServices(bc, destroyNetworksAction, props);
		}
		{
			Properties props = new Properties();
			props.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU, "true");
			registerAllServices(bc, exportNetworkAction, props);
		}
		{
			Properties props = new Properties();
			props.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU, "true");
			registerAllServices(bc, exportImageAction, props);
		}
		{
			TaskFactory pauseCommand = new PauseCommandTaskFactory(cytoscapeDesktop);
			Properties props = new Properties();
			props.setProperty(COMMAND_NAMESPACE, "command");
			props.setProperty(COMMAND_DESCRIPTION, "Display a message and pause until the user continues.");
			props.setProperty(COMMAND, "pause");
			props.setProperty(COMMAND_LONG_DESCRIPTION,
	                            "The **pause** command displays a dialog with the text provided in the *message* argument "+
	                            "and waits for the user to click **OK**");
			props.setProperty(COMMAND_SUPPORTS_JSON, "true");
			props.setProperty(COMMAND_EXAMPLE_JSON, "{}");
			registerService(bc, pauseCommand, TaskFactory.class, props);
		}
		
		registerAllServices(bc, cytoscapeDesktop);
		registerAllServices(bc, netMediator);
		registerAllServices(bc, netViewMediator);
		registerService(bc, undoMonitor, SetCurrentNetworkViewListener.class);
		registerService(bc, undoMonitor, NetworkDestroyedListener.class);
		registerService(bc, undoMonitor, NetworkViewDestroyedListener.class);
		registerAllServices(bc, rowViewTracker);
		registerAllServices(bc, selecteEdgeViewUpdater);
		registerAllServices(bc, selecteNodeViewUpdater);
		registerService(bc, columnPresentationManager, CyColumnPresentationManager.class);

		registerAllServices(bc, rowsSetViewUpdater);
		
		registerAllServices(bc, sessionHandler);
		registerAllServices(bc, toolBarEnableUpdater);
		registerAllServices(bc, recentSessionManager);
		registerAllServices(bc, netSelectionMediator);
		registerAllServices(bc, netSearchMediator);

		// From old Welcome Screen
		registerAllServices(bc, welcomeScreenAction, new Properties());

		registerServiceListener(bc, cytoscapeDesktop::addAction, cytoscapeDesktop::removeAction, CyAction.class);
		registerServiceListener(bc, preferenceAction::addCyProperty, preferenceAction::removeCyProperty, CyProperty.class);
		registerServiceListener(bc, cytoscapeDesktop::addCytoPanelComponent, cytoscapeDesktop::removeCytoPanelComponent, CytoPanelComponent.class);
		registerServiceListener(bc, cytoscapeDesktop::addToolBarComponent, cytoscapeDesktop::removeToolBarComponent, ToolBarComponent.class);
		registerServiceListener(bc, cytoscapeMenuPopulator::addTaskFactory, cytoscapeMenuPopulator::removeTaskFactory, TaskFactory.class);
		registerServiceListener(bc, cytoscapeMenuPopulator::addNetworkTaskFactory, cytoscapeMenuPopulator::removeNetworkTaskFactory, NetworkTaskFactory.class);
		registerServiceListener(bc, cytoscapeMenuPopulator::addNetworkViewTaskFactory, cytoscapeMenuPopulator::removeNetworkViewTaskFactory, NetworkViewTaskFactory.class);
		registerServiceListener(bc, cytoscapeMenuPopulator::addRootNetworkCollectionTaskFactory, cytoscapeMenuPopulator::removeRootNetworkCollectionTaskFactory, RootNetworkCollectionTaskFactory.class);
		registerServiceListener(bc, cytoscapeMenuPopulator::addNetworkCollectionTaskFactory, cytoscapeMenuPopulator::removeNetworkCollectionTaskFactory, NetworkCollectionTaskFactory.class);
		registerServiceListener(bc, cytoscapeMenuPopulator::addNetworkViewCollectionTaskFactory, cytoscapeMenuPopulator::removeNetworkViewCollectionTaskFactory, NetworkViewCollectionTaskFactory.class);
		registerServiceListener(bc, cytoscapeMenuPopulator::addTableTaskFactory, cytoscapeMenuPopulator::removeTableTaskFactory, TableTaskFactory.class);
		registerServiceListener(bc, layoutSettingsManager::addLayout, layoutSettingsManager::removeLayout, CyLayoutAlgorithm.class);
		registerServiceListener(bc, settingsAction::addLayout, settingsAction::removeLayout, CyLayoutAlgorithm.class);
		registerServiceListener(bc, columnPresentationManager::addPresentation, columnPresentationManager::removePresentation, CyColumnPresentation.class);
		
		// For Network Panel context menu
		registerServiceListener(bc, netMediator::addNetworkViewTaskFactory, netMediator::removeNetworkViewTaskFactory, NetworkViewTaskFactory.class, CONTEXT_MENU_FILTER);
		registerServiceListener(bc, netMediator::addNetworkTaskFactory, netMediator::removeNetworkTaskFactory, NetworkTaskFactory.class, CONTEXT_MENU_FILTER);
		registerServiceListener(bc, netMediator::addRootNetworkCollectionTaskFactory, netMediator::removeRootNetworkCollectionTaskFactory, RootNetworkCollectionTaskFactory.class, CONTEXT_MENU_FILTER);
		registerServiceListener(bc, netMediator::addNetworkViewCollectionTaskFactory, netMediator::removeNetworkViewCollectionTaskFactory, NetworkViewCollectionTaskFactory.class, CONTEXT_MENU_FILTER);
		registerServiceListener(bc, netMediator::addNetworkCollectionTaskFactory, netMediator::removeNetworkCollectionTaskFactory, NetworkCollectionTaskFactory.class, CONTEXT_MENU_FILTER);
		registerServiceListener(bc, netMediator::addCyAction, netMediator::removeCyAction, CyAction.class, CONTEXT_MENU_FILTER);
		registerServiceListener(bc, netSearchMediator::addNetworkSearchTaskFactory, netSearchMediator::removeNetworkSearchTaskFactory, NetworkSearchTaskFactory.class);
		registerServiceListener(bc, layoutMenuPopulator::addLayout, layoutMenuPopulator::removeLayout, CyLayoutAlgorithm.class);
		
		// Only register CytoPanelComponents after corresponding OSGI listeners for CytoscapeDesktop
		// are registered, otherwise the CytoPanelComponent order is messed up ("Network" must be the first tab!)
		registerAllServices(bc, netMainPanel);
		registerAllServices(bc, commandToolPanel);
		
		if (isMac()) {
			try {
				new MacCyActivator().start(bc);
			} catch (Exception e) {
				logger.error("Cannot start MacCyActivator", e);
			}
		} else {
			AboutAction aboutAction = new AboutAction(HELP_MENU, serviceRegistrar);
			registerService(bc, aboutAction, CyAction.class);
			
			registerService(bc, exitAction, CyAction.class);
		}
	
		// Full screen actions. This is platform dependent and is no longer supported on macOS + Java 11.
		if (!isMac()) {
			FullScreenAction fullScreenAction = new FullScreenAction(cytoscapeDesktop);
			registerService(bc, fullScreenAction, CyAction.class);
		}
	}

	private void initComponents(BundleContext bc, CyServiceRegistrar serviceRegistrar) {
		var applicationManager = getService(bc, CyApplicationManager.class);
		var netViewManager = getService(bc, CyNetworkViewManager.class);
		var iconManager = serviceRegistrar.getService(IconManager.class);
		
		var rootNetManager = new RootNetworkManager(serviceRegistrar);
		registerService(bc, rootNetManager, NetworkAboutToBeDestroyedListener.class);
		registerService(bc, rootNetManager, SessionAboutToBeLoadedListener.class);
		
		var cytoscapeMenuBar = new CytoscapeMenuBar(serviceRegistrar);
		var cytoscapeToolBar = new CytoscapeToolBar(serviceRegistrar);
		cytoscapeMenus = new CytoscapeMenus(cytoscapeMenuBar, cytoscapeToolBar);
		toolBarEnableUpdater = new ToolBarEnableUpdater(cytoscapeToolBar, serviceRegistrar);
		
		netSearchBar = new NetworkSearchBar(serviceRegistrar);
		netSearchMediator = new NetworkSearchMediator(netSearchBar, serviceRegistrar);
		
		netMainPanel = new NetworkMainPanel(netSearchBar, serviceRegistrar);
		netMediator = new NetworkMediator(netMainPanel, rootNetManager, serviceRegistrar);
		commandToolPanel = new CommandToolPanel(serviceRegistrar);
		
		viewComparator = new ViewComparator(netMainPanel);
		gridViewToggleModel = new GridViewToggleModel(GridViewToggleModel.Mode.VIEW);
		netViewMainPanel = new NetworkViewMainPanel(gridViewToggleModel, cytoscapeMenus, viewComparator, serviceRegistrar);
		netViewMediator = new NetworkViewMediator(netViewMainPanel, netMediator, gridViewToggleModel, serviceRegistrar);
		
		cytoscapeDesktop = new CytoscapeDesktop(cytoscapeMenus, netViewMediator, serviceRegistrar);

		var sessionIO = new SessionIO();
		sessionHandler = new SessionHandler(cytoscapeDesktop, netViewMediator, sessionIO, netMainPanel, serviceRegistrar);
		
		layoutMenuPopulator = new LayoutMenuPopulator(cytoscapeMenuBar, serviceRegistrar);
		cytoscapeMenuPopulator = new CytoscapeMenuPopulator(cytoscapeDesktop, rootNetManager, serviceRegistrar);

		layoutSettingsManager = new LayoutSettingsManager(serviceRegistrar);
		
		helpUserManualTaskFactory = new HelpUserManualTaskFactory(serviceRegistrar);
		helpTutorialsTaskFactory = new HelpTutorialsTaskFactory(serviceRegistrar);
		helpVideosTaskFactory = new HelpVideosTaskFactory(serviceRegistrar);
		helpContactHelpDeskTaskFactory = new HelpContactHelpDeskTaskFactory(serviceRegistrar);
		helpReportABugTaskFactory = new HelpReportABugTaskFactory(serviceRegistrar);
		helpTourTaskFactory = new HelpTourTaskFactory(serviceRegistrar);
		
		cyDesktopManager = new CyDesktopManager(netViewMediator);
		
		arrangeGridTaskFactory = new ArrangeTaskFactory(GRID, cyDesktopManager, netViewMediator);
		arrangeCascadeTaskFactory = new ArrangeTaskFactory(CASCADE, cyDesktopManager, netViewMediator);
		arrangeHorizontalTaskFactory = new ArrangeTaskFactory(HORIZONTAL, cyDesktopManager, netViewMediator);
		arrangeVerticalTaskFactory = new ArrangeTaskFactory(VERTICAL, cyDesktopManager, netViewMediator);
		
		preferencesDialogFactory = new PreferencesDialogFactory(serviceRegistrar);
		bookmarkDialogFactory = new BookmarkDialogFactory(serviceRegistrar);
		
		undoMonitor = new UndoMonitor(serviceRegistrar);
		rowViewTracker = new RowViewTracker();
		selecteEdgeViewUpdater = new SelectEdgeViewUpdater(rowViewTracker);
		selecteNodeViewUpdater = new SelectNodeViewUpdater(rowViewTracker);
		columnPresentationManager = new CyColumnPresentationManagerImpl(serviceRegistrar);
		
		rowsSetViewUpdater = new RowsSetViewUpdater(rowViewTracker, netViewMediator, serviceRegistrar);

		recentSessionManager = new RecentSessionManager(serviceRegistrar);
		netSelectionMediator = new NetworkSelectionMediator(netMainPanel, netViewMainPanel, rootNetManager, serviceRegistrar);
		
		///// CyActions ////
		undoAction = new UndoAction(serviceRegistrar);
		redoAction = new RedoAction(serviceRegistrar);
		
		printAction = new PrintAction(applicationManager, netViewManager, serviceRegistrar);
		exitAction = new ExitAction(serviceRegistrar);
		preferenceAction = new PreferenceAction(cytoscapeDesktop, preferencesDialogFactory);
		bookmarkAction = new BookmarkAction(cytoscapeDesktop, bookmarkDialogFactory);
		settingsAction = new SettingsAction(layoutSettingsManager, serviceRegistrar);
		
		cytoPanelWestAction = new CytoPanelAction(CytoPanelNameInternal.WEST, cytoscapeDesktop, 1.0f);
		cytoPanelSouthAction = new CytoPanelAction(CytoPanelNameInternal.SOUTH, cytoscapeDesktop, 1.1f);
		cytoPanelEastAction = new CytoPanelAction(CytoPanelNameInternal.EAST, cytoscapeDesktop, 1.2f);
		cytoPanelSouthWestAction = new CytoPanelAction(CytoPanelNameInternal.SOUTH_WEST, cytoscapeDesktop, 1.3f);
		cytoPanelCommandAction = new CytoPanelAction(CytoPanelNameInternal.BOTTOM, cytoscapeDesktop, 1.35f);

		{
			var iconFont = iconManager.getIconFont(CY_FONT_NAME, 32.0f);
			var icon = new TextIcon(LAYERED_NEW_FROM_SELECTED, iconFont, COLORS_3, LARGE_ICON_SIZE, LARGE_ICON_SIZE, 1);
			
			newNetworkFromSelectionAction = new NewNetworkFromSelectionAction(10.1f, icon, serviceRegistrar);
		}
		{
			var icon = new TextIcon(IconManager.ICON_HOME, iconManager.getIconFont(28.0f),
					UIManager.getColor("CyColor.complement(+1)"), LARGE_ICON_SIZE, LARGE_ICON_SIZE);
			
			starterPanelActionToolBar = new StarterPanelAction(110000000000000.0f, icon, cytoscapeDesktop);
			starterPanelActionMenu = new StarterPanelAction(1.4f, cytoscapeDesktop);
		}
		
		detachedViewToolBarAction = new DetachedViewToolBarAction(1.5f, netViewMediator);
		
		closeWindowAction = new CloseWindowAction(1.99f, netViewMediator);
		createNetworkViewsAction = new CreateNetworkViewsAction(3.0f, serviceRegistrar);
		destroyNetworkViewsAction = new DestroyNetworkViewsAction(3.1f, serviceRegistrar);
		destroyNetworksAction = new DestroyNetworksAction(3.2f, netMainPanel, serviceRegistrar);
		exportNetworkAction = new ExportNetworkAction(1000.1f, serviceRegistrar);
		exportImageAction = new ExportImageAction(1000.2f, serviceRegistrar);

		// Updater moved from old Welcome Screen
		welcomeScreenAction = new CheckForUpdatesAction(serviceRegistrar);
	}
	
	private void setLookAndFeel(BundleContext bc) {
		var iconManager = getService(bc, IconManager.class);
		
		// Set Look and Feel
		var props = getCy3Property(bc).getProperties();
		var lookAndFeel = props.getProperty("lookAndFeel");
		
		if (lookAndFeel == null) {
			if (isMac() || isWindows())
				lookAndFeel = UIManager.getSystemLookAndFeelClassName();
			else // Use Nimbus on *nix systems
				lookAndFeel = "javax.swing.plaf.nimbus.NimbusLookAndFeel";
		}
			
		try {
			logger.debug("Setting look and feel to: " + lookAndFeel);
			UIManager.setLookAndFeel(lookAndFeel);
		} catch (Exception e) {
			logger.error("Unexpected error", e);
		}
		
		if (isAquaLAF()) {
			final boolean useScreenMenuBar;
			var useScreenMenuBarVal = props.getProperty("useScreenMenuBar");
			
			if ("true".equalsIgnoreCase(useScreenMenuBarVal)) {
				useScreenMenuBar = true;
			} else if ("false".equalsIgnoreCase(useScreenMenuBarVal)) {
				useScreenMenuBar = false;
			} else {
				// Use the regular window menu bar if it's High Sierra and the system is set to any other
				// language other than English, to work around a bug in High Sierra where the screen menu
				// disappears.
				// See http://code.cytoscape.org/redmine/issues/3967 and
				//     https://github.com/arduino/Arduino/issues/6548
				String language = Locale.getDefault().getLanguage();
				String version = System.getProperty("os.version");
				boolean highSierra = version.startsWith("10.13");
				boolean english = language.equals(new Locale("en").getLanguage());
				useScreenMenuBar = !(highSierra && !english);
			}
			
			System.setProperty("apple.laf.useScreenMenuBar", "" + useScreenMenuBar);
		}
		
		try {
			if (UIManager.getFont("Label.font") == null)
				UIManager.put("Label.font", new JLabel().getFont());
			
			var tsb = UIManager.getColor("Table.selectionBackground");
			if (tsb == null) tsb = UIManager.getColor("Tree.selectionBackground");
			if (tsb == null) tsb = UIManager.getColor("Table[Enabled+Selected].textBackground");
			
			if (tsb != null) {
				HSLColor hsl = new HSLColor(tsb);
				tsb = hsl.adjustLuminance(isAquaLAF() ? 94.0f : 90.0f);
			}
			
			var tableSelectionBg = tsb != null && !tsb.equals(Color.WHITE) ? tsb : new Color(222, 234, 252);
			
			UIManager.put("Table.focusCellBackground", UIManager.getColor("Tree.selectionBackground"));
			UIManager.put("Table.focusCellForeground", UIManager.getColor("Tree.selectionForeground"));
			UIManager.put("Table.selectionBackground", tableSelectionBg);
			UIManager.put("Table.selectionForeground", UIManager.getColor("Table.foreground"));
			
			Color originalDisabledFg = null;
			
			if (isNimbusLAF()) {
				originalDisabledFg = UIManager.getColor("nimbusDisabledText");
				
				// Because Nimbus colors are usually of class javax.swing.plaf.nimbus.DerivedColor
				if (originalDisabledFg != null)
					originalDisabledFg = new Color(originalDisabledFg.getRGB());
			} else {
				originalDisabledFg = UIManager.getColor(isGtkLAF() ? "Button.disabledForeground" : "Label.disabledForeground");
				
				if (originalDisabledFg == null)
					originalDisabledFg = UIManager.getColor("TextField.inactiveForeground");
			}
			
			// The default disabled color is usually too dark, so let's make it look more like the native one
			final Color disabledFg;
			
			if (originalDisabledFg == null)
				disabledFg = originalDisabledFg = UIManager.getColor("Panel.background").darker();
			else if (isWinLAF() || isAquaLAF())
				disabledFg = ColorUtil.setBrightness(originalDisabledFg, 0.75f);
			else
				disabledFg = originalDisabledFg;
			
			UIManager.put("Label.disabledForeground", disabledFg);
			UIManager.put("Button.disabledForeground", disabledFg);
			UIManager.put("Button.disabledText", disabledFg);
			UIManager.put("ToggleButton.disabledForeground", disabledFg);
			UIManager.put("ToggleButton.disabledText", disabledFg);
			UIManager.put("CheckBox.disabledForeground", disabledFg);
			UIManager.put("Radio.disabledForeground", disabledFg);
			UIManager.put("Menu.disabledForeground", disabledFg);
			UIManager.put("MenuItem.disabledForeground", disabledFg);
			UIManager.put("CheckBoxMenuItem.disabledForeground", disabledFg);
			UIManager.put("RadioButtonMenuItem.disabledForeground", disabledFg);
			UIManager.put("Table.disabledForeground", disabledFg);
			UIManager.put("Table.disabledText", disabledFg);
			UIManager.put("TableHeader.disabledForeground", disabledFg);
			UIManager.put("TextField.inactiveForeground", disabledFg);
			UIManager.put("TextField.disabledForeground", disabledFg);
			UIManager.put("FormattedTextField.inactiveForeground", disabledFg);
			UIManager.put("FormattedTextField.disabledForeground", disabledFg);
			UIManager.put("PasswordField.disabledBackground", disabledFg);
			UIManager.put("TextArea.inactiveForeground", disabledFg);
			UIManager.put("TextArea.disabledForeground", disabledFg);
			UIManager.put("List.disabledForeground", disabledFg);
			UIManager.put("ComboBox.disabledForeground", disabledFg);
			
			UIManager.put("Tree.hash", new Color(255, 255, 255, 0)); // Hide tree lines properly
			
			// Hide the separator line and increase the gap
			UIManager.put("ToolBar.separatorSize", new Dimension(1, 20));
			UIManager.put("ToolBarSeparatorUI", "javax.swing.plaf.basic.BasicToolBarSeparatorUI");
			
			// Cytoscape
			UIManager.put("Label.infoForeground", ColorUtil.setBrightness(UIManager.getColor("Label.foreground"), 0.48f));
			
			var tableFont = UIManager.getFont("Label.font").deriveFont(11.0f);
			
			if (isAquaLAF()) {
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
				UIManager.put("TableHeader.background", new Color(240, 240, 240));
				UIManager.put("Table.alternateRowColor", new Color(244, 245, 245, 0));
				UIManager.put("Table.showGrid", false);
				UIManager.put("Table.gridColor", new Color(216, 216, 216, 0)); // starts with a 100% transparency, or Swing will not respect a false "Table.showGrid"
				UIManager.put("Table.font", tableFont);
				UIManager.put("Tree.font", tableFont);
			} else if (isWinLAF()) {
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
				UIManager.put("Table.alternateRowColor", new Color(245, 245, 245, 0));
				UIManager.put("Viewport.background", UIManager.getColor("Table.background"));
				UIManager.put("ScrollPane.background", UIManager.getColor("Table.background"));
				UIManager.put("Separator.foreground", new Color(208, 208, 208));
				
				var selBgColor = UIManager.getColor("TextField.selectionBackground");
				
				if (selBgColor != null)
					UIManager.put("Focus.color", new Color(selBgColor.getRGB()));
			} else if (isNimbusLAF()) {
				// Nimbus (usually Linux)
				// Translating Nimbus default colors to more standard UIManager keys
				// in order to make it easier for Cytoscape to reuse the LAF colors.
				// Also fixes inconsistent colors since the latest Java 8 version.
				// (http://docs.oracle.com/javase/tutorial/uiswing/lookandfeel/_nimbusDefaults.html)
				UIManager.put("nimbusLightBackground", Color.WHITE);
				UIManager.put("nimbusDisabledText", new Color(142, 143, 145));
				
				// Make all table rows white, like the other LAFs
				UIManager.put("Table.background", Color.WHITE);
				UIManager.put("Table.foreground", new Color(UIManager.getColor("Table.foreground").getRGB()));
				UIManager.put("Table.alternateRowColor", new Color(242, 242, 242, 0));
				UIManager.put("Table:\"Table.cellRenderer\".background", Color.WHITE);
				UIManager.put("Table.showGrid", true);
				UIManager.put("Table.gridColor", UIManager.getColor("Separator.background"));
				UIManager.put("Table.font", tableFont);
				
				UIManager.put("Viewport.background", Color.WHITE);
				UIManager.put("ScrollPane.background", Color.WHITE);
				UIManager.put("Tree.font", tableFont);
				UIManager.put("Separator.foreground", UIManager.getColor("nimbusBorder"));
				
				var nimbusColor = UIManager.getColor("nimbusFocus");
				
				if (nimbusColor == null)
					nimbusColor = new Color(115, 164, 209);
				
				UIManager.put("Focus.color", new Color(nimbusColor.getRGB()));
				
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
			} else if (isGtkLAF()) {
				// GTK (usually Linux):
				UIManager.put(
						"TableHeader.cellBorder", 
						BorderFactory.createCompoundBorder(
								BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Separator.foreground")),
								BorderFactory.createEmptyBorder(2, 4, 2, 4)
						)
				);
				UIManager.put("TableHeader.background", UIManager.getColor("Table.background"));
				UIManager.put("Table.alternateRowColor", new Color(245, 245, 245, 0));
			}
			
			// Tree icons -- we need a right-to-left arrow!
			if (UIManager.getIcon("Tree.rightToLeftCollapsedIcon") == null && (isWinLAF() || isGtkLAF())) {
				// These custom icons did not work well with original JTrees on Nimbus (they were misaligned)
				// and Aqua usually does not need this.
				var treeIcon = UIManager.getIcon("Tree.collapsedIcon");
				int w = treeIcon != null ? Math.max(12, treeIcon.getIconWidth()) : 16;
				int h = treeIcon != null ? Math.max(12, treeIcon.getIconHeight()) : 16;
				var font = iconManager.getIconFont(10f);
				var c = UIManager.getColor("Label.infoForeground");
				UIManager.put("Tree.expandedIcon", new TextIcon(IconManager.ICON_CHEVRON_DOWN, font, c, w, h));
				UIManager.put("Tree.collapsedIcon", new TextIcon(IconManager.ICON_CHEVRON_RIGHT, font, c, w, h));
				UIManager.put("Tree.rightToLeftCollapsedIcon", new TextIcon(IconManager.ICON_CHEVRON_LEFT, font, c, w, h));
			}
			
			// ScrollPane
			UIManager.put("ScrollPane.border", BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")));
			
			// SplitPane
			UIManager.put("SplitPaneUI", "javax.swing.plaf.basic.BasicSplitPaneUI");
			UIManager.put("SplitPane.dividerSize", ViewUtil.DIVIDER_SIZE);
			UIManager.put("SplitPane.foreground", UIManager.getColor("Separator.foreground"));
			UIManager.put("SplitPane.background", UIManager.getColor("Separator.foreground"));
			UIManager.put("SplitPaneDivider.border", BorderFactory.createEmptyBorder());
			
			// Created for Cytoscape ------------------------------------------------------------------------
			// ToggleButton
			UIManager.put("CyToggleButton.background", UIManager.getColor("Button.background"));
			
			if (isNimbusLAF()) {
				UIManager.put("CyToggleButton.foreground", new Color(UIManager.getColor("Button.foreground").getRGB()));
				UIManager.put("CyToggleButton[Selected].background", new Color(UIManager.getColor("nimbusBlueGrey").getRGB()));
				UIManager.put("CyToggleButton[Selected].foreground", new Color(UIManager.getColor("ToggleButton.foreground").getRGB()));
				UIManager.put("CyToggleButton[Selected].borderColor", new Color(UIManager.getColor("nimbusBorder").getRGB()));
			} else {
				UIManager.put("CyToggleButton.foreground", ColorUtil.setBrightness(UIManager.getColor("Button.foreground"), 0.25f));
				UIManager.put("CyToggleButton[Selected].background", ColorUtil.setBrightness(UIManager.getColor("Button.background"), 0.8f));
				UIManager.put("CyToggleButton[Selected].foreground", UIManager.getColor("Button.foreground"));
				UIManager.put("CyToggleButton[Selected].borderColor", ColorUtil.setBrightness(UIManager.getColor("Button.foreground"), 0.68f));
			}
		} catch (Exception e) {
			logger.error("Unexpected error", e);
		}
		
		// Cytoscape Palette (http://paletton.com/#uid=70F2h0krdtngVCclWv9tYnSyeiT) -------------------------
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
		
		// A JPopupMenu that is completely contained inside the frame bounds is called lightweight and
		// lives in the Popup layer of the layered pane, which means popups created when a CytoPanel is in
		// the UNDOCK state may be covered by the undocked CytoPanel component. That happens because the
		// CytoPanel component is rendered on the Glass Pane, which resides on top of the Popup layer.
		// To avoid this issue, let's just disable lightweight popup menus
		// (NOTE: This must be done before showing the application frame).
		// See: https://www.pushing-pixels.org/category/swing/page/72
		JPopupMenu.setDefaultLightWeightPopupEnabled(false);
	}
	
	@SuppressWarnings("unchecked")
	private CyProperty<Properties> getCy3Property(BundleContext bc) {
		return getService(bc, CyProperty.class, "(cyPropertyName=cytoscape3.props)");
	}
	
	public static boolean isGtkLAF() {
		return UIManager.getLookAndFeel() != null && "GTK".equals(UIManager.getLookAndFeel().getID());
	}
	
	private class ViewComparator implements Comparator<CyNetworkView> {

		private final NetworkMainPanel netMainPanel;
		private final Collator collator;
		
		ViewComparator(NetworkMainPanel netMainPanel) {
			this.netMainPanel = netMainPanel;
			collator = Collator.getInstance(Locale.getDefault());
		}
		
		@Override
		public int compare(CyNetworkView v1, CyNetworkView v2) {
			// Sort by view title, but group them by collection (root-network) and subnetwork
			Integer idx1 = netMainPanel.indexOf(v1.getModel());
			Integer idx2 = netMainPanel.indexOf(v2.getModel());
			int value = idx1.compareTo(idx2);
			
			if (value == 0) {
				// Views from the same network? Sort alphabetically...
				value = collator.compare(ViewUtil.getTitle(v1), ViewUtil.getTitle(v2));
			
				// Same title? Just use their SUIDs then...
				if (value == 0)
					value = v1.getSUID().compareTo(v2.getSUID());
			}
			
			return value;
		}
	}
}
