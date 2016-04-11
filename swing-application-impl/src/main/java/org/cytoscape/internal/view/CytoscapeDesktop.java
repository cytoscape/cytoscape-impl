package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
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
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.session.events.SessionSavedEvent;
import org.cytoscape.session.events.SessionSavedListener;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.StatusBarPanelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

/**
 * The CytoscapeDesktop is the central Window for working with Cytoscape.
 */
@SuppressWarnings("serial")
public class CytoscapeDesktop extends JFrame
		implements CySwingApplication, CyStartListener, SessionLoadedListener, SessionSavedListener {

	private static final String TITLE_PREFIX_STRING ="Session: ";
	private static final String NEW_SESSION_NAME ="New Session";
	
	static final Dimension DEF_DESKTOP_SIZE = new Dimension(1300, 850);
	private static final int DEF_DIVIDER_LOATION = 450;
	
	private static final String SMALL_ICON = "/images/logo.png";
	private static final int DIVIDER_SIZE = 4;
	
	@SuppressWarnings("unused")
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
	protected NetworkViewMediator netViewMediator;

	//
	// CytoPanel Variables
	//
	private CytoPanelImp cytoPanelWest;
	private CytoPanelImp cytoPanelEast;
	private CytoPanelImp cytoPanelSouth;
	private CytoPanelImp cytoPanelSouthWest; 

	// Status Bar TODO: Move this to log-swing to avoid cyclic dependency.
	private JPanel mainPanel;
	private JToolBar statusToolBar;
	private StatusBarPanelFactory taskStatusPanelFactory;
	private StatusBarPanelFactory jobStatusPanelFactory;
	
	private final CyServiceRegistrar serviceRegistrar;

	public CytoscapeDesktop(
			final CytoscapeMenus cyMenus,
			final NetworkViewMediator netViewMediator,
			final CyServiceRegistrar serviceRegistrar
	) {
		super(TITLE_PREFIX_STRING + NEW_SESSION_NAME);

		this.cyMenus = cyMenus;
		this.netViewMediator = netViewMediator;
		this.serviceRegistrar = serviceRegistrar;
		
		final DialogTaskManager taskManager = serviceRegistrar.getService(DialogTaskManager.class);
		taskManager.setExecutionContext(this);

		setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource(SMALL_ICON)));

		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		// create the CytoscapeDesktop
		final BiModalJSplitPane masterPane = setupCytoPanels(netViewMediator);

		mainPanel.add(masterPane, BorderLayout.CENTER);
		mainPanel.add(cyMenus.getJToolBar(), BorderLayout.NORTH);

		// statusToolBar = setupStatusPanel(jobStatusPanelFactory, taskStatusPanelFactory);

		if (MacFullScreenEnabler.supportsNativeFullScreenMode())
			MacFullScreenEnabler.setEnabled(this, true);

		//don't automatically close window. Let shutdown.exit(returnVal) handle this
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		setJMenuBar(cyMenus.getJMenuBar());
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {
				// This is necessary because the same menu bar can be used by other frames
				final JMenuBar menuBar = cyMenus.getJMenuBar();
				final Window window = SwingUtilities.getWindowAncestor(menuBar);
				
				if (!CytoscapeDesktop.this.equals(window)) {
					if (window instanceof JFrame && !LookAndFeelUtil.isAquaLAF()) {
						// Do this first, or the user could see the menu disappearing from the out-of-focus windows
						final JMenuBar dummyMenuBar = cyMenus.createDummyMenuBar();
						((JFrame) window).setJMenuBar(dummyMenuBar);
						dummyMenuBar.updateUI();
						window.repaint();
					}
					
					setJMenuBar(menuBar);
					menuBar.updateUI();
				}
				
				taskManager.setExecutionContext(CytoscapeDesktop.this);
			}
			@Override
			public void windowClosing(WindowEvent we) {
				final CyShutdown cyShutdown = serviceRegistrar.getService(CyShutdown.class);
				cyShutdown.exit(0);
			}
		});

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentHidden(ComponentEvent e) { }

			@Override
			public void componentShown(ComponentEvent e) {
				// We need to do this later in the cycle to make sure everything is loaded
				if (jobStatusPanelFactory == null || taskStatusPanelFactory == null) {
					jobStatusPanelFactory = serviceRegistrar.getService(StatusBarPanelFactory.class, "(type=JobStatus)");
					taskStatusPanelFactory = serviceRegistrar.getService(StatusBarPanelFactory.class, "(type=TaskStatus)");
					statusToolBar = setupStatusPanel(jobStatusPanelFactory, taskStatusPanelFactory);
				}
			}
		});

		// Prepare to show the desktop...
		setContentPane(mainPanel);
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

	private JToolBar setupStatusPanel(StatusBarPanelFactory jobStatusPanelFactory,
	                                  StatusBarPanelFactory taskStatusPanelFactory) {
		final JPanel taskStatusPanel = taskStatusPanelFactory.createTaskStatusPanel();
		final JPanel jobStatusPanel = jobStatusPanelFactory.createTaskStatusPanel();
		final JToolBar statusToolBar = new JToolBar();
		final MemStatusPanel memStatusPanel = new MemStatusPanel();
		
		if (LookAndFeelUtil.isNimbusLAF()) {
			jobStatusPanel.setOpaque(false);
			taskStatusPanel.setOpaque(false);
			statusToolBar.setOpaque(false);
			memStatusPanel.setOpaque(false);
		}

		final JPanel statusPanel = new JPanel();
		statusPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIManager.getColor("Separator.foreground")));
		
		final GroupLayout layout = new GroupLayout(statusPanel);
		statusPanel.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);

		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addContainerGap()
				.addComponent(jobStatusPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(taskStatusPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(statusToolBar, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(memStatusPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addContainerGap()
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGap(LookAndFeelUtil.isWinLAF() ? 5 : 0)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(jobStatusPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(taskStatusPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(statusToolBar, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(memStatusPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGap(LookAndFeelUtil.isWinLAF() ? 5 : 0)
		);
		
		mainPanel.add(statusPanel, BorderLayout.SOUTH);
		
		return statusToolBar;
	}

	/**
	 * Create the CytoPanels UI.
	 */
	private BiModalJSplitPane setupCytoPanels(NetworkViewMediator netViewMediator) {
		// bimodals that our Cytopanels Live within
		final BiModalJSplitPane topRightPane = createTopRightPane(netViewMediator);
		final BiModalJSplitPane rightPane = createRightPane(topRightPane);
		final BiModalJSplitPane masterPane = createMasterPane(rightPane);
		createBottomLeft();

		return masterPane;
	}

	private BiModalJSplitPane createTopRightPane(NetworkViewMediator netViewMediator) {
		// create cytopanel with tabs along the top
		cytoPanelEast = new CytoPanelImp(CytoPanelName.EAST, JTabbedPane.TOP, CytoPanelState.HIDE, this,
				serviceRegistrar);

		// determine proper network view manager component
		final JPanel networkViewPanel = netViewMediator.getNetworkViewMainPanel();

		// create the split pane - we show this on startup
		BiModalJSplitPane splitPane = new BiModalJSplitPane(this, JSplitPane.HORIZONTAL_SPLIT,
		                                                    BiModalJSplitPane.MODE_HIDE_SPLIT,
		                                                    networkViewPanel, cytoPanelEast);

		// set the cytopanelcontainer
		cytoPanelEast.setCytoPanelContainer(splitPane);

		// set the resize weight - left component gets extra space
		splitPane.setResizeWeight(1.0);

		// outta here
		return splitPane;
	}

	private BiModalJSplitPane createRightPane(BiModalJSplitPane topRightPane) {
		// create cytopanel with tabs along the bottom
		cytoPanelSouth = new CytoPanelImp(CytoPanelName.SOUTH, JTabbedPane.BOTTOM, CytoPanelState.DOCK, this,
				serviceRegistrar);

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
		cytoPanelSouthWest = new CytoPanelImp(CytoPanelName.SOUTH_WEST, JTabbedPane.TOP, CytoPanelState.HIDE, this,
				serviceRegistrar);

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
		serviceRegistrar.registerService(t,CytoPanelStateChangedListener.class,new Properties());
	}

	private BiModalJSplitPane createMasterPane(BiModalJSplitPane rightPane) {
		// create cytopanel with tabs along the top
		cytoPanelWest = new CytoPanelImp(CytoPanelName.WEST, JTabbedPane.TOP, CytoPanelState.DOCK, this,
				serviceRegistrar);

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

	public void addAction(CyAction action, Map<?, ?> props) {
		cyMenus.addAction(action, props);
	}

	@Override
	public void addAction(CyAction action) {
		cyMenus.addAction(action, new HashMap<>());
	}

	public void removeAction(CyAction action, Map<?, ?> props) {
		cyMenus.removeAction(action);
	}

	@Override
	public void removeAction(CyAction action) {
		cyMenus.removeAction(action);
	}

	@Override
	public JMenu getJMenu(String name) {
		return cyMenus.getJMenu(name);
	}

	@Override
	public JMenuBar getJMenuBar() {
		return cyMenus.getJMenuBar();
	}

	@Override
	public JToolBar getJToolBar() {
		return cyMenus.getJToolBar();
	}

	@Override
	public JFrame getJFrame() {
		return this;
	}

	@Override
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
