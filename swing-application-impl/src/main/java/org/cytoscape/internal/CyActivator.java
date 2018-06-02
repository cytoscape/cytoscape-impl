package org.cytoscape.internal;

import static org.cytoscape.application.swing.ActionEnableSupport.ENABLE_FOR_NETWORK_AND_VIEW;
import static org.cytoscape.application.swing.CyNetworkViewDesktopMgr.ArrangeType.*;
import static org.cytoscape.internal.util.ViewUtil.invokeOnEDTAndWait;
import static org.cytoscape.work.ServiceProperties.*;

import java.awt.Color;
import java.awt.Font;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
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
import org.cytoscape.internal.actions.BookmarkAction;
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
import org.cytoscape.internal.actions.FullScreenMacAction;
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
import org.cytoscape.internal.util.ViewUtil;
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
import org.cytoscape.internal.view.MacFullScreenEnabler;
import org.cytoscape.internal.view.NetworkMainPanel;
import org.cytoscape.internal.view.NetworkMediator;
import org.cytoscape.internal.view.NetworkSearchBar;
import org.cytoscape.internal.view.NetworkSearchMediator;
import org.cytoscape.internal.view.NetworkSelectionMediator;
import org.cytoscape.internal.view.NetworkViewMainPanel;
import org.cytoscape.internal.view.NetworkViewMediator;
import org.cytoscape.internal.view.ToolBarEnableUpdater;
import org.cytoscape.internal.view.help.ArrangeTaskFactory;
import org.cytoscape.internal.view.help.HelpAboutTaskFactory;
import org.cytoscape.internal.view.help.HelpContactHelpDeskTaskFactory;
import org.cytoscape.internal.view.help.HelpReportABugTaskFactory;
import org.cytoscape.internal.view.help.HelpUserManualTaskFactory;
import org.cytoscape.model.events.NetworkDestroyedListener;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.task.NetworkCollectionTaskFactory;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.task.NetworkViewCollectionTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.task.RootNetworkCollectionTaskFactory;
import org.cytoscape.task.TableTaskFactory;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.NetworkViewDestroyedListener;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.GUITunableHandlerFactory;
import org.cytoscape.work.swing.SimpleGUITunableHandlerFactory;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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
	
	private static final String CONTEXT_MENU_FILTER = "(" + ServiceProperties.IN_NETWORK_PANEL_CONTEXT_MENU + "=true)";

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
	private HelpContactHelpDeskTaskFactory helpContactHelpDeskTaskFactory;
	private HelpReportABugTaskFactory helpReportABugTaskFactory;
	private HelpAboutTaskFactory helpAboutTaskFactory;
	
	private CyDesktopManager cyDesktopManager;
	
	private ArrangeTaskFactory arrangeGridTaskFactory;
	private ArrangeTaskFactory arrangeCascadeTaskFactory;
	private ArrangeTaskFactory arrangeHorizontalTaskFactory;
	private ArrangeTaskFactory arrangeVerticalTaskFactory;
	
//	private Cy3PreferencesDialogFactory cy3preferencesDialogFactory;
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
//	private Cy3PreferenceAction cy3preferenceAction;
	private BookmarkAction bookmarkAction;
	private SettingsAction settingsAction;
	
	private CytoPanelAction cytoPanelWestAction;
	private CytoPanelAction cytoPanelSouthAction;
	private CytoPanelAction cytoPanelEastAction;
	private CytoPanelAction cytoPanelSouthWestAction;
	private CytoPanelAction cytoPanelCommandAction;

	private StarterPanelAction starterPanelAction;
	private DetachedViewToolBarAction detachedViewToolBarAction;
	private CloseWindowAction closeWindowAction;
	private CreateNetworkViewsAction createNetworkViewsAction;
	private DestroyNetworkViewsAction destroyNetworkViewsAction;
	private DestroyNetworksAction destroyNetworksAction;
	private ExportNetworkAction exportNetworkAction;
	private ExportImageAction exportImageAction;
	
	@Override
	public void start(final BundleContext bc) throws Exception {
		final CyServiceRegistrar serviceRegistrar = getService(bc, CyServiceRegistrar.class);
		
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
//		registerService(bc, cy3preferenceAction, CyAction.class);
		registerService(bc, bookmarkAction, CyAction.class);
		registerService(bc, settingsAction, CyAction.class);
		registerService(bc, settingsAction, SetCurrentNetworkViewListener.class);
		registerService(bc, cytoPanelWestAction, CyAction.class);
		registerService(bc, cytoPanelSouthAction, CyAction.class);
		registerService(bc, cytoPanelEastAction, CyAction.class);
		registerService(bc, cytoPanelSouthWestAction, CyAction.class);
		registerService(bc, cytoPanelCommandAction, CyAction.class);
		registerService(bc, starterPanelAction, CyAction.class);
		registerService(bc, detachedViewToolBarAction, CyAction.class);
		registerService(bc, closeWindowAction, CyAction.class);
		
		registerService(bc, cyDesktopManager, CyNetworkViewDesktopMgr.class);
		
		registerService(bc, bookmarkDialogFactory, SessionLoadedListener.class);

		{
			Properties props = new Properties();
			props.setProperty(PREFERRED_MENU, "Help");
			props.setProperty(TITLE, "User Manual");
			props.setProperty(MENU_GRAVITY, "1.0");
			props.setProperty(TOOLTIP, "Show User Manual");
			registerService(bc, helpUserManualTaskFactory, TaskFactory.class, props);
		}
		{
			Properties props = new Properties();
			props.setProperty(PREFERRED_MENU, "Help");
			props.setProperty(MENU_GRAVITY, "7.0");
			props.setProperty(TITLE, "Contact Help Desk...");
			registerService(bc, helpContactHelpDeskTaskFactory, TaskFactory.class, props);
		}
		{
			Properties props = new Properties();
			props.setProperty(PREFERRED_MENU, "Help");
			props.setProperty(TITLE, "Report a Bug...");
			props.setProperty(MENU_GRAVITY, "8.0");
			registerService(bc, helpReportABugTaskFactory, TaskFactory.class, props);
		}
		{
			Properties props = new Properties();
			props.setProperty(ServiceProperties.ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
			props.setProperty(ACCELERATOR, "cmd g");
			props.setProperty(PREFERRED_MENU, "View.Arrange Network Windows[8]");
			props.setProperty(TITLE, "Grid");
			props.setProperty(MENU_GRAVITY, "1.0");
			registerService(bc, arrangeGridTaskFactory, TaskFactory.class, props);
		}
		{
			Properties props = new Properties();
			props.setProperty(ServiceProperties.ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
			props.setProperty(PREFERRED_MENU, "View.Arrange Network Windows[8]");
			props.setProperty(TITLE, "Cascade");
			props.setProperty(MENU_GRAVITY, "2.0");
			registerService(bc, arrangeCascadeTaskFactory, TaskFactory.class, props);
		}
		{
			Properties props = new Properties();
			props.setProperty(ServiceProperties.ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
			props.setProperty(PREFERRED_MENU, "View.Arrange Network Windows[8]");
			props.setProperty(TITLE, "Vertical Stack");
			props.setProperty(MENU_GRAVITY, "3.0");
			registerService(bc, arrangeHorizontalTaskFactory, TaskFactory.class, props);
		}
		{
			Properties props = new Properties();
			props.setProperty(ServiceProperties.ENABLE_FOR, ENABLE_FOR_NETWORK_AND_VIEW);
			props.setProperty(PREFERRED_MENU, "View.Arrange Network Windows[8]");
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
		registerAllServices(bc, netMainPanel);
		registerAllServices(bc, commandToolPanel);
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

		registerServiceListener(bc, cytoscapeDesktop::addAction, cytoscapeDesktop::removeAction, CyAction.class);
		registerServiceListener(bc, preferenceAction::addCyProperty, preferenceAction::removeCyProperty, CyProperty.class);
//		registerServiceListener(bc, cy3preferenceAction::addCyProperty, cy3preferenceAction::removeCyProperty, CyProperty.class);
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
		
		if (LookAndFeelUtil.isMac()) {
			try {
				new MacCyActivator().start(bc);
			} catch (Exception e) {
				logger.error("Cannot start MacCyActivator", e);
			}
		} else {
			Properties props = new Properties();
			props.setProperty(PREFERRED_MENU, "Help");
			props.setProperty(TITLE, "About");
			props.setProperty(MENU_GRAVITY,"10.0");
			registerService(bc, helpAboutTaskFactory, TaskFactory.class, props);
			
			registerService(bc, exitAction, CyAction.class);
		}
	
		// Full screen actions.  This is platform dependent
		FullScreenAction fullScreenAction = null;
		
		if (LookAndFeelUtil.isMac() && MacFullScreenEnabler.supportsNativeFullScreenMode())
			fullScreenAction = new FullScreenMacAction(cytoscapeDesktop);
		else
			fullScreenAction = new FullScreenAction(cytoscapeDesktop);
		
		registerService(bc, fullScreenAction, CyAction.class);
	}

	private void initComponents(final BundleContext bc, final CyServiceRegistrar serviceRegistrar) {
		CyApplicationManager applicationManager = getService(bc, CyApplicationManager.class);
		CyNetworkViewManager netViewManager = getService(bc, CyNetworkViewManager.class);
		
		final RootNetworkManager rootNetManager = new RootNetworkManager();
		
		final CytoscapeMenuBar cytoscapeMenuBar = new CytoscapeMenuBar(serviceRegistrar);
		final CytoscapeToolBar cytoscapeToolBar = new CytoscapeToolBar(serviceRegistrar);
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

		final SessionIO sessionIO = new SessionIO();
		sessionHandler = new SessionHandler(cytoscapeDesktop, netViewMediator, sessionIO, netMainPanel, serviceRegistrar);
		
		layoutMenuPopulator = new LayoutMenuPopulator(cytoscapeMenuBar, serviceRegistrar);
		cytoscapeMenuPopulator = new CytoscapeMenuPopulator(cytoscapeDesktop, rootNetManager, serviceRegistrar);

		layoutSettingsManager = new LayoutSettingsManager(serviceRegistrar);
		
		helpUserManualTaskFactory = new HelpUserManualTaskFactory(serviceRegistrar);
		helpContactHelpDeskTaskFactory = new HelpContactHelpDeskTaskFactory(serviceRegistrar);
		helpReportABugTaskFactory = new HelpReportABugTaskFactory(serviceRegistrar);
		helpAboutTaskFactory = new HelpAboutTaskFactory(serviceRegistrar);
		
		cyDesktopManager = new CyDesktopManager(netViewMediator);
		
		arrangeGridTaskFactory = new ArrangeTaskFactory(GRID, cyDesktopManager, netViewMediator);
		arrangeCascadeTaskFactory = new ArrangeTaskFactory(CASCADE, cyDesktopManager, netViewMediator);
		arrangeHorizontalTaskFactory = new ArrangeTaskFactory(HORIZONTAL, cyDesktopManager, netViewMediator);
		arrangeVerticalTaskFactory = new ArrangeTaskFactory(VERTICAL, cyDesktopManager, netViewMediator);
		
		preferencesDialogFactory = new PreferencesDialogFactory(serviceRegistrar);
//		cy3preferencesDialogFactory = new Cy3PreferencesDialogFactory(serviceRegistrar);
		bookmarkDialogFactory = new BookmarkDialogFactory(serviceRegistrar);
		
		undoMonitor = new UndoMonitor(serviceRegistrar);
		rowViewTracker = new RowViewTracker();
		selecteEdgeViewUpdater = new SelectEdgeViewUpdater(rowViewTracker);
		selecteNodeViewUpdater = new SelectNodeViewUpdater(rowViewTracker);
		columnPresentationManager = new CyColumnPresentationManagerImpl();
		
		rowsSetViewUpdater = new RowsSetViewUpdater(rowViewTracker, netViewMediator, serviceRegistrar);

		recentSessionManager = new RecentSessionManager(serviceRegistrar);
		netSelectionMediator = new NetworkSelectionMediator(netMainPanel, netViewMainPanel, rootNetManager, serviceRegistrar);
		
		///// CyActions ////
		undoAction = new UndoAction(serviceRegistrar);
		redoAction = new RedoAction(serviceRegistrar);
		
		printAction = new PrintAction(applicationManager, netViewManager, serviceRegistrar);
		exitAction = new ExitAction(serviceRegistrar);
//		cy3preferenceAction = new Cy3PreferenceAction(cytoscapeDesktop, cy3preferencesDialogFactory);
		preferenceAction = new PreferenceAction(cytoscapeDesktop, preferencesDialogFactory);
		bookmarkAction = new BookmarkAction(cytoscapeDesktop, bookmarkDialogFactory);
		settingsAction = new SettingsAction(layoutSettingsManager, serviceRegistrar);
		
		cytoPanelWestAction = new CytoPanelAction(CytoPanelNameInternal.WEST, true, cytoscapeDesktop, 1.0f);
		cytoPanelSouthAction = new CytoPanelAction(CytoPanelNameInternal.SOUTH, true, cytoscapeDesktop, 1.1f);
		cytoPanelEastAction = new CytoPanelAction(CytoPanelNameInternal.EAST, false, cytoscapeDesktop, 1.2f);
		cytoPanelSouthWestAction = new CytoPanelAction(CytoPanelNameInternal.SOUTH_WEST, false, cytoscapeDesktop, 1.3f);
		cytoPanelCommandAction = new CytoPanelAction(CytoPanelNameInternal.BOTTOM, false, cytoscapeDesktop, 1.35f);
		
		starterPanelAction = new StarterPanelAction(1.4f, cytoscapeDesktop);
		detachedViewToolBarAction = new DetachedViewToolBarAction(1.5f, netViewMediator);
		
		closeWindowAction = new CloseWindowAction(1.99f, netViewMediator);
		createNetworkViewsAction = new CreateNetworkViewsAction(3.0f, serviceRegistrar);
		destroyNetworkViewsAction = new DestroyNetworkViewsAction(3.1f, serviceRegistrar);
		destroyNetworksAction = new DestroyNetworksAction(3.2f, netMainPanel, serviceRegistrar);
		exportNetworkAction = new ExportNetworkAction(1000.1f, serviceRegistrar);
		exportImageAction = new ExportImageAction(1000.2f, serviceRegistrar);
	}
	
	private void setLookAndFeel(final BundleContext bc) {
		// Set Look and Feel
		final Properties props = getCy3Property(bc).getProperties();
		String lookAndFeel = props.getProperty("lookAndFeel");
		
		if (lookAndFeel == null) {
			if (LookAndFeelUtil.isMac() || LookAndFeelUtil.isWindows())
				lookAndFeel = UIManager.getSystemLookAndFeelClassName();
			else // Use Nimbus on *nix systems
				// lookAndFeel = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
				lookAndFeel = "javax.swing.plaf.nimbus.NimbusLookAndFeel";
		}
			
		try {
			logger.debug("Setting look and feel to: " + lookAndFeel);
			UIManager.setLookAndFeel(lookAndFeel);
		} catch (Exception e) {
			logger.error("Unexpected error", e);
		}
		
		if (LookAndFeelUtil.isAquaLAF()) {
			final boolean useScreenMenuBar;
			String useScreenMenuBarVal = props.getProperty("useScreenMenuBar");
			
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
				
				final Color selBgColor = UIManager.getColor("TextField.selectionBackground");
				
				if (selBgColor != null)
					UIManager.put("Focus.color", new Color(selBgColor.getRGB()));
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
				
				Color nimbusColor = UIManager.getColor("nimbusFocus");
				
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
	
	@SuppressWarnings("unchecked")
	private CyProperty<Properties> getCy3Property(final BundleContext bc) {
		return getService(bc, CyProperty.class, "(cyPropertyName=cytoscape3.props)");
	}
	
	private class ViewComparator implements Comparator<CyNetworkView> {

		private final NetworkMainPanel netMainPanel;
		private final Collator collator;
		
		ViewComparator(final NetworkMainPanel netMainPanel) {
			this.netMainPanel = netMainPanel;
			collator = Collator.getInstance(Locale.getDefault());
		}
		
		@Override
		public int compare(final CyNetworkView v1, final CyNetworkView v2) {
			// Sort by view title, but group them by collection (root-network) and subnetwork
			final Integer idx1 = netMainPanel.indexOf(v1.getModel());
			final Integer idx2 = netMainPanel.indexOf(v2.getModel());
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
