package org.cytoscape.internal.view;

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

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Dictionary;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.cytoscape.application.CyShutdown;
import org.cytoscape.application.events.CyStartEvent;
import org.cytoscape.application.events.CyStartListener;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.application.swing.ToolBarComponent;
import org.cytoscape.application.swing.events.CytoPanelStateChangedListener;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.session.events.SessionSavedEvent;
import org.cytoscape.session.events.SessionSavedListener;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.TaskStatusPanelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The CytoscapeDesktop is the central Window for working with Cytoscape
 */
public class CytoscapeDesktop extends JFrame implements CySwingApplication, CyStartListener, SessionLoadedListener, SessionSavedListener {

	private final static long serialVersionUID = 1202339866271348L;
	
	private static final String TITLE_PREFIX_STRING ="Session: ";
	private static final String NEW_SESSION_NAME ="New Session";
	
	static final Dimension DEF_DESKTOP_SIZE = new Dimension(1300, 850);
	private static final int DEF_DIVIDER_LOATION = 450;
	
	private static final String SMALL_ICON = "/images/logo.png";
	private static final int DIVIDER_SIZE = 4;
	
	private static final Logger logger = LoggerFactory.getLogger(CytoscapeDesktop.class);
	
	/**
	 * The CyMenus object provides access to the all of the menus and toolbars
	 * that will be needed.
	 */
	protected CytoscapeMenus cyMenus;

	/**
	 * The NetworkViewManager can support three types of interfaces.
	 * Tabbed/InternalFrame/ExternalFrame
	 */
	protected NetworkViewManager networkViewManager;

	//
	// CytoPanel Variables
	//
	private CytoPanelImp cytoPanelWest;
	private CytoPanelImp cytoPanelEast;
	private CytoPanelImp cytoPanelSouth;
	private CytoPanelImp cytoPanelSouthWest; 

	// Status Bar TODO: Move this to log-swing to avoid cyclic dependency.
	private JPanel main_panel;
	private final CyShutdown shutdown; 
	private final CyEventHelper cyEventHelper;
	private final CyServiceRegistrar registrar;
	private final IconManager iconManager;
	private final JToolBar statusToolBar;

	/**
	 * Creates a new CytoscapeDesktop object.
	 */
	public CytoscapeDesktop(final CytoscapeMenus cyMenus,
							final NetworkViewManager networkViewManager,
							final CyShutdown shut,
							final CyEventHelper eh,
							final CyServiceRegistrar registrar,
							final DialogTaskManager taskManager,
							final TaskStatusPanelFactory taskStatusPanelFactory,
							final IconManager iconManager) {
		super(TITLE_PREFIX_STRING + NEW_SESSION_NAME);

		this.cyMenus = cyMenus;
		this.networkViewManager = networkViewManager;
		this.shutdown = shut;
		this.cyEventHelper = eh;
		this.registrar = registrar;
		this.iconManager = iconManager;
		
		taskManager.setExecutionContext(this);

		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource(SMALL_ICON)));

		main_panel = new JPanel();
		main_panel.setLayout(new BorderLayout());

		// create the CytoscapeDesktop
		final BiModalJSplitPane masterPane = setupCytoPanels(networkViewManager);

		main_panel.add(masterPane, BorderLayout.CENTER);
		main_panel.add(cyMenus.getJToolBar(), BorderLayout.NORTH);

		statusToolBar = setupStatusPanel(taskStatusPanelFactory);

		setJMenuBar(cyMenus.getJMenuBar());

		if(MacFullScreenEnabler.supportsNativeFullScreenMode()) {
			MacFullScreenEnabler.setEnabled(this, true);
		}

		//don't automatically close window. Let shutdown.exit(returnVal)
		//handle this
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent we) {
					shutdown.exit(0);
				}
			});

		// Prepare to show the desktop...
		setContentPane(main_panel);
		pack();
		setSize(DEF_DESKTOP_SIZE);
		
		// Defines default divider location for the network panel to JDesktop
		masterPane.setDividerLocation(400);
		
		// Move it to the center
		this.setLocationRelativeTo(null);

		// ...but don't actually show it!!!!
		// Once the system has fully started the JFrame will be set to 
		// visible by the StartupMostlyFinished class, found elsewhere.
	}

	private JToolBar setupStatusPanel(TaskStatusPanelFactory taskStatusPanelFactory) {
		final JPanel statusPanel = new JPanel(new GridBagLayout());
		statusPanel.setOpaque(false);
		final GridBagConstraints c = new GridBagConstraints();
		final JToolBar statusToolBar = new JToolBar();
		statusToolBar.setOpaque(false);

		final JPanel taskStatusPanel = taskStatusPanelFactory.createTaskStatusPanel();

		c.gridx = 0;		c.gridy = 0;
		c.gridwidth = 1;	c.gridheight = 1;
		c.weightx = 1.0;	c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		statusPanel.add(taskStatusPanel, c);

		c.gridx++;
		c.weightx = 0.0;	c.weighty = 0.0;
		c.fill = GridBagConstraints.NONE;
		statusPanel.add(statusToolBar, c);

		c.gridx++;
		c.anchor = GridBagConstraints.EAST;
		statusPanel.add(new MemStatusPanel(), c);

		main_panel.add(statusPanel, BorderLayout.SOUTH);
		return statusToolBar;
	}

	/**
	 * Create the CytoPanels UI.
	 *
	 * @param networkPanel
	 *            to load on left side of right bimodal.
	 * @param networkViewManager
	 *            to load on left side (CytoPanel West).
	 * @return BiModalJSplitPane Object.
	 */
	private BiModalJSplitPane setupCytoPanels(NetworkViewManager networkViewManager) {
		// bimodals that our Cytopanels Live within
		final BiModalJSplitPane topRightPane = createTopRightPane(networkViewManager);
		final BiModalJSplitPane rightPane = createRightPane(topRightPane);
		final BiModalJSplitPane masterPane = createMasterPane(rightPane);
		createBottomLeft();

		return masterPane;
	}

	/**
	 * Creates the TopRight Pane.
	 *
	 * @param networkViewManager
	 *            to load on left side of top right bimodal.
	 * @return BiModalJSplitPane Object.
	 */
	private BiModalJSplitPane createTopRightPane(NetworkViewManager networkViewManager) {
		// create cytopanel with tabs along the top
		cytoPanelEast = new CytoPanelImp(CytoPanelName.EAST, JTabbedPane.TOP, CytoPanelState.HIDE, cyEventHelper, this,
				iconManager);

		// determine proper network view manager component
		Component networkViewComp = (Component) networkViewManager.getDesktopPane();

		// create the split pane - we show this on startup
		BiModalJSplitPane splitPane = new BiModalJSplitPane(this, JSplitPane.HORIZONTAL_SPLIT,
		                                                    BiModalJSplitPane.MODE_HIDE_SPLIT,
		                                                    networkViewComp, cytoPanelEast);

		// set the cytopanelcontainer
		cytoPanelEast.setCytoPanelContainer(splitPane);

		// set the resize weight - left component gets extra space
		splitPane.setResizeWeight(1.0);

		// outta here
		return splitPane;
	}

	/**
	 * Creates the Right Panel.
	 *
	 * @param topRightPane
	 *            TopRightPane Object.
	 * @return BiModalJSplitPane Object
	 */
	private BiModalJSplitPane createRightPane(BiModalJSplitPane topRightPane) {
		// create cytopanel with tabs along the bottom
		cytoPanelSouth = new CytoPanelImp(CytoPanelName.SOUTH, JTabbedPane.BOTTOM,
		                                  CytoPanelState.DOCK, cyEventHelper, this, iconManager);

		// create the split pane - hidden by default
		BiModalJSplitPane splitPane = new BiModalJSplitPane(this, JSplitPane.VERTICAL_SPLIT,
		                                                    BiModalJSplitPane.MODE_SHOW_SPLIT,
		                                                    topRightPane, cytoPanelSouth);

		// set the cytopanel container
		cytoPanelSouth.setCytoPanelContainer(splitPane);

		splitPane.setDividerSize(DIVIDER_SIZE);
		splitPane.setDividerLocation(DEF_DIVIDER_LOATION);

		// set resize weight - top component gets all the extra space.
		splitPane.setResizeWeight(1.0);

		// outta here
		return splitPane;
	}

	private void createBottomLeft() {

		// create cytopanel with tabs along the top for manual layout
		cytoPanelSouthWest = new CytoPanelImp(CytoPanelName.SOUTH_WEST, JTabbedPane.TOP,
						      CytoPanelState.HIDE, cyEventHelper, this, iconManager);

        final BiModalJSplitPane split = new BiModalJSplitPane(this, JSplitPane.VERTICAL_SPLIT,
                                      BiModalJSplitPane.MODE_HIDE_SPLIT, new JPanel(),
                                      cytoPanelSouthWest);
        split.setResizeWeight(0);
        cytoPanelSouthWest.setCytoPanelContainer(split);
        cytoPanelSouthWest.setMinimumSize(new Dimension(180, 330));
        cytoPanelSouthWest.setMaximumSize(new Dimension(180, 330));
        cytoPanelSouthWest.setPreferredSize(new Dimension(180, 330));

        split.setDividerSize(DIVIDER_SIZE);

		ToolCytoPanelListener t = new ToolCytoPanelListener( split, cytoPanelWest, 
		                                                     cytoPanelSouthWest );
		registrar.registerService(t,CytoPanelStateChangedListener.class,new Properties());
	}

	/**
	 * Creates the Master Split Pane.
	 *
	 * @param networkPanel
	 *            to load on left side of CytoPanel (cytoPanelWest).
	 * @param rightPane
	 *            BiModalJSplitPane Object.
	 * @return BiModalJSplitPane Object.
	 */
	private BiModalJSplitPane createMasterPane(BiModalJSplitPane rightPane) {
		// create cytopanel with tabs along the top
		cytoPanelWest = new CytoPanelImp(CytoPanelName.WEST, JTabbedPane.TOP, CytoPanelState.DOCK, cyEventHelper, this,
				iconManager);

		// create the split pane - displayed by default
		BiModalJSplitPane splitPane = new BiModalJSplitPane(this, JSplitPane.HORIZONTAL_SPLIT,
		                                                    BiModalJSplitPane.MODE_SHOW_SPLIT,
		                                                    cytoPanelWest, rightPane);

		splitPane.setDividerSize(DIVIDER_SIZE);

		// set the cytopanel container
		cytoPanelWest.setCytoPanelContainer(splitPane);

		// outta here
		return splitPane;
	}

	NetworkViewManager getNetworkViewManager() {
		return networkViewManager;
	}

	public void addAction(CyAction action, Dictionary<?, ?> props) {
		cyMenus.addAction(action);
	}

	public void addAction(CyAction action) {
		cyMenus.addAction(action);
	}

	public void removeAction(CyAction action, Dictionary<?, ?> props) {
		cyMenus.removeAction(action);
	}

	public void removeAction(CyAction action) {
		cyMenus.removeAction(action);
	}

	public JMenu getJMenu(String name) {
		return cyMenus.getJMenu(name);
	}

	public JMenuBar getJMenuBar() {
		return cyMenus.getJMenuBar();
	}

	public JToolBar getJToolBar() {
		return cyMenus.getJToolBar();
	}

	public JFrame getJFrame() {
		return this;
	}

	public CytoPanel getCytoPanel(final CytoPanelName compassDirection) {
		return getCytoPanelInternal(compassDirection);
	}

	private CytoPanelImp getCytoPanelInternal(final CytoPanelName compassDirection) {
		// return appropriate cytoPanel based on compass direction
		switch (compassDirection) {
		case SOUTH:
			return cytoPanelSouth;
		case EAST:
			return cytoPanelEast;
		case WEST:
			return cytoPanelWest;
		case SOUTH_WEST:
			return cytoPanelSouthWest;
		}

		// houston we have a problem
		throw new IllegalArgumentException("Illegal Argument:  " + compassDirection
		                                   + ".  Must be one of:  {SOUTH,EAST,WEST,SOUTH_WEST}.");
	}

	public void addCytoPanelComponent(CytoPanelComponent cp, Dictionary<?, ?> props) {
		CytoPanelImp impl = getCytoPanelInternal(cp.getCytoPanelName());
		impl.add(cp);
	}

	public void removeCytoPanelComponent(CytoPanelComponent cp, Dictionary<?, ?> props) {
		CytoPanelImp impl = getCytoPanelInternal(cp.getCytoPanelName());
		impl.remove(cp);
	}

	@Override
	public JToolBar getStatusToolBar() {
		return statusToolBar;
	}

	public void addToolBarComponent(ToolBarComponent tp, Dictionary<?, ?> props) {
		((CytoscapeToolBar)cyMenus.getJToolBar()).addToolBarComponent(tp);
	}

	public void removeToolBarComponent(ToolBarComponent tp, Dictionary<?, ?> props) {
		((CytoscapeToolBar)cyMenus.getJToolBar()).removeToolBarComponent(tp);		
	}
	
	// handle CytoscapeStartEvent
	@Override
	public void handleEvent(CyStartEvent e) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setVisible(true);
				toFront();
			}
		});
	}

	@Override
	public void handleEvent(SessionLoadedEvent e) {
		// Update window title
		String sessionName = e.getLoadedFileName();
		if (sessionName == null)
			sessionName = NEW_SESSION_NAME;
		final String title = TITLE_PREFIX_STRING + sessionName;
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setTitle(title);
			}
		});
	}

	@Override
	public void handleEvent(SessionSavedEvent e) {
		// Update window title
		final String sessionName = e.getSavedFileName();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setTitle(TITLE_PREFIX_STRING + sessionName);
			}
		});
	}
}
