package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.application.swing.CytoPanelName.EAST;
import static org.cytoscape.application.swing.CytoPanelName.SOUTH;
import static org.cytoscape.application.swing.CytoPanelName.SOUTH_WEST;
import static org.cytoscape.application.swing.CytoPanelName.WEST;
import static org.cytoscape.application.swing.CytoPanelState.DOCK;
import static org.cytoscape.application.swing.CytoPanelState.HIDE;
import static org.cytoscape.internal.util.ViewUtil.invokeOnEDT;
import static org.cytoscape.internal.util.ViewUtil.invokeOnEDTAndWait;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

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

import org.cytoscape.app.event.AppsFinishedStartingEvent;
import org.cytoscape.app.event.AppsFinishedStartingListener;
import org.cytoscape.application.CyShutdown;
import org.cytoscape.application.events.CyStartEvent;
import org.cytoscape.application.events.CyStartListener;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.application.swing.ToolBarComponent;
import org.cytoscape.application.swing.events.CytoPanelStateChangedEvent;
import org.cytoscape.application.swing.events.CytoPanelStateChangedListener;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.TableAddedEvent;
import org.cytoscape.model.events.TableAddedListener;
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
 * Copyright (C) 2006 - 2017 The Cytoscape Consortium
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
public class CytoscapeDesktop extends JFrame implements CySwingApplication, CyStartListener,
		AppsFinishedStartingListener, SessionLoadedListener, SessionSavedListener, SetCurrentNetworkListener,
		SetCurrentNetworkViewListener, TableAddedListener, CytoPanelStateChangedListener {

	private static final String TITLE_PREFIX_STRING ="Session: ";
	private static final String NEW_SESSION_NAME ="New Session";
	
	static final Dimension DEF_DESKTOP_SIZE = new Dimension(1300, 850);
	private static final int DEF_DIVIDER_LOATION = 540;
	
	private static final String SMALL_ICON = "/images/logo.png";
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger("org.cytoscape.application.userlog");
	
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

	private BiModalJSplitPane masterPane;
	private BiModalJSplitPane rightPane;
	private BiModalJSplitPane topRightPane;
	
	private CytoPanelImpl westPanel;
	private CytoPanelImpl eastPanel;
	private CytoPanelImpl southPanel;
	private CytoPanelImpl southWestPanel; 

	// Status Bar TODO: Move this to log-swing to avoid cyclic dependency.
	private JPanel mainPanel;
	private JPanel centerPanel;
	private JToolBar statusToolBar;
	private StarterPanel starterPanel;
	private StatusBarPanelFactory taskStatusPanelFactory;
	private StatusBarPanelFactory jobStatusPanelFactory;
	
	/** Holds frames that contain floating CytoPanels */
	private final  Map<CytoPanel, JFrame> floatingFrames = new HashMap<>();
	private boolean ignoreFloatingFrameCloseEvents;
	
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
					
					if (LookAndFeelUtil.isAquaLAF())
						cyMenus.setMenuBarVisible(true);
					
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
		setContentPane(getMainPanel());
		
		pack();
		setSize(DEF_DESKTOP_SIZE);
		
		// Move it to the center
		setLocationRelativeTo(null);

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
				.addPreferredGap(ComponentPlacement.RELATED)
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
		
		getMainPanel().add(statusPanel, BorderLayout.SOUTH);

		return statusToolBar;
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

	private CytoPanelImpl getCytoPanelInternal(final CytoPanelName compassDirection) {
		switch (compassDirection) {
			case SOUTH:
				return getSouthPanel();
			case EAST:
				return getEastPanel();
			case WEST:
				return getWestPanel();
			case SOUTH_WEST:
				return getSouthWestPanel();
		}

		throw new IllegalArgumentException(
				"Illegal Argument:  " + compassDirection + ".  Must be one of:  {SOUTH,EAST,WEST,SOUTH_WEST}.");
	}

	public void addCytoPanelComponent(CytoPanelComponent cp, Map<?, ?> props) {
		invokeOnEDTAndWait(() -> {
			CytoPanelImpl impl = getCytoPanelInternal(cp.getCytoPanelName());
			impl.add(cp);
		});
	}

	public void removeCytoPanelComponent(CytoPanelComponent cp, Map<?, ?> props) {
		invokeOnEDTAndWait(() -> {
			CytoPanelImpl impl = getCytoPanelInternal(cp.getCytoPanelName());
			impl.remove(cp);
		});
	}

	@Override
	public JToolBar getStatusToolBar() {
		return statusToolBar;
	}

	public void addToolBarComponent(ToolBarComponent tp, Map<?, ?> props) {
		invokeOnEDTAndWait(() -> {
			((CytoscapeToolBar) cyMenus.getJToolBar()).addToolBarComponent(tp);
		});
	}

	public void removeToolBarComponent(ToolBarComponent tp, Map<?, ?> props) {
		invokeOnEDTAndWait(() -> {
			((CytoscapeToolBar) cyMenus.getJToolBar()).removeToolBarComponent(tp);
		});
	}
	
	@Override
	public void handleEvent(CyStartEvent e) {
		invokeOnEDT(() -> {
			setVisible(true);
			toFront();
		});
	}
	
	@Override
	public void handleEvent(AppsFinishedStartingEvent e) {
		invokeOnEDT(() -> {
			// Only show Starter Panel the first time if the initial session is empty
			// (for instance, Cystoscape can start up with a session file specified through the terminal)
			final CyNetworkManager netManager = serviceRegistrar.getService(CyNetworkManager.class);
			final CyTableManager tableManager = serviceRegistrar.getService(CyTableManager.class);
			
			if (netManager.getNetworkSet().isEmpty() && tableManager.getAllTables(false).isEmpty())
				showStarterPanel();
		});
	}
	
	@Override
	public void handleEvent(SessionLoadedEvent e) {
		// Update window title
		String sessionName = e.getLoadedFileName();
		
		if (sessionName == null)
			sessionName = NEW_SESSION_NAME;
		
		final String title = TITLE_PREFIX_STRING + sessionName;
		
		invokeOnEDT(() -> {
			setTitle(title);
			hideStarterPanel();
		});
	}
	
	@Override
	public void handleEvent(SessionSavedEvent e) {
		// Update window title
		final String sessionName = e.getSavedFileName();
		invokeOnEDT(() -> setTitle(TITLE_PREFIX_STRING + sessionName));
	}
	
	@Override
	public void handleEvent(SetCurrentNetworkEvent e) {
		invokeOnEDT(() -> hideStarterPanel());
	}
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		invokeOnEDT(() -> hideStarterPanel());
	}
	
	@Override
	public void handleEvent(TableAddedEvent e) {
		CyTable table = e.getTable();
		
		// It does not make sense to hide the Starter Panel when the table is private, because the user will not see it.
		// Also apps can create private tables during initialization, which would hide the Starter Panel by accident.
		if (table != null && table.isPublic())
			invokeOnEDT(() -> hideStarterPanel());
	}
	
	@Override
	public void handleEvent(CytoPanelStateChangedEvent e) {
		if (e.getCytoPanel() instanceof CytoPanelImpl == false)
			return;
		
		CytoPanelImpl cytoPanel = (CytoPanelImpl) e.getCytoPanel();
		CytoPanelState state = e.getNewState();

		switch (state) {
			case HIDE:
				hideCytoPanel(cytoPanel);
				break;
			case FLOAT:
				floatCytoPanel(cytoPanel);
				break;
			case DOCK:
				dockCytoPanel(cytoPanel);
				break;
		}
	}
	
	public void showStarterPanel() {
		getStarterPanel().update();
		((CardLayout) getCenterPanel().getLayout()).show(getCenterPanel(), StarterPanel.NAME);
	}
	
	public void hideStarterPanel() {
		if (isStarterPanelVisible())
			((CardLayout) getCenterPanel().getLayout()).show(getCenterPanel(), NetworkViewMainPanel.NAME);
	}
	
	public boolean isStarterPanelVisible() {
		return getStarterPanel().isVisible();
	}
	
	private void floatCytoPanel(CytoPanelImpl cytoPanel) {
		// show ourselves
		showCytoPanel(cytoPanel);

		if (!isFloating(cytoPanel)) {
			BiModalJSplitPane splitPane = getSplitPaneOf(cytoPanel);
			
			if (splitPane != null)
				splitPane.removeCytoPanel(cytoPanel);
			
			// New window to place this CytoPanel
			JFrame frame = new JFrame(cytoPanel.getTitle(), getGraphicsConfiguration());
			floatingFrames.put(cytoPanel, frame);
			
			// add listener to handle when window is closed
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					if (!ignoreFloatingFrameCloseEvents)
						cytoPanel.setState(DOCK);
				}
			});

			if (cytoPanel.getThisComponent().getSize() != null) {
				frame.getContentPane().setPreferredSize(cytoPanel.getThisComponent().getSize());
				frame.getContentPane().setSize(cytoPanel.getThisComponent().getSize());
			}
			
			//  Add CytoPanel to the New External Window
			frame.getContentPane().add(cytoPanel.getThisComponent(), BorderLayout.CENTER);
			frame.pack();
			
			// Show it
			setLocationOfFloatingFrame(frame, cytoPanel);
			frame.setVisible(true);

			if (splitPane != null)
				splitPane.update();
		}
	}
	
	private void dockCytoPanel(CytoPanelImpl cytoPanel) {
		// Show the panel first
		showCytoPanel(cytoPanel);

		BiModalJSplitPane splitPane = getSplitPaneOf(cytoPanel);
		
		if (isFloating(cytoPanel)) {
			// Remove cytopanel from external view
			JFrame frame = floatingFrames.remove(cytoPanel);
			
			if (frame != null) {
				frame.remove(cytoPanel.getThisComponent());
				ignoreFloatingFrameCloseEvents = true;
				
				try {
					frame.dispose();
				} finally {
					ignoreFloatingFrameCloseEvents = false;
				}
			}

			if (splitPane != null)
				splitPane.addCytoPanel(cytoPanel);
		}

		if (splitPane != null)
			splitPane.update();
	}
	
	private void showCytoPanel(CytoPanelImpl cytoPanel) {
		cytoPanel.getThisComponent().setVisible(true);
	}
	
	private void hideCytoPanel(CytoPanelImpl cytoPanel) {
		if (isFloating(cytoPanel))
			dockCytoPanel(cytoPanel);

		cytoPanel.getThisComponent().setVisible(false);
		
		BiModalJSplitPane splitPane = getSplitPaneOf(cytoPanel);
		
		if (splitPane != null)
			splitPane.update();
	}
	
	private boolean isFloating(CytoPanelImpl cytoPanel) {
		JFrame frame = floatingFrames.get(cytoPanel);
		
		return frame != null && frame == SwingUtilities.getWindowAncestor(cytoPanel.getThisComponent());
	}
	
	private void setLocationOfFloatingFrame(JFrame frame, CytoPanelImpl cytoPanel) {
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screenDimension = tk.getScreenSize();

		// Get Absolute Location and Bounds, relative to Screen
		Rectangle bounds = getBounds();
		bounds.setLocation(getLocationOnScreen());

		Point p = CytoPanelUtil.getLocationOfExternalWindow(screenDimension, bounds, frame.getSize(),
				cytoPanel.getCytoPanelName(), false);
		frame.setLocation(p);
		frame.setVisible(true);
	}
	
	private BiModalJSplitPane getSplitPaneOf(CytoPanelImpl cytoPanel) {
		switch (cytoPanel.getCytoPanelName()) {
			case SOUTH:
				return getRightPane();
			case EAST:
				return getTopRightPane();
			case WEST:
				return getMasterPane();
			case SOUTH_WEST:
				return (BiModalJSplitPane) getWestPanel().getThisComponent();
			default:
				return null;
		}
	}
	
	private JPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			mainPanel.add(cyMenus.getJToolBar(), BorderLayout.NORTH);
			mainPanel.add(getMasterPane(), BorderLayout.CENTER);
		}
		
		return mainPanel;
	}
	
	private BiModalJSplitPane getMasterPane() {
		if (masterPane == null) {
			masterPane = new BiModalJSplitPane(WEST, JSplitPane.HORIZONTAL_SPLIT, getWestPanel().getThisComponent(),
					getRightPane());
			masterPane.setDividerLocation(400);
		}

		return masterPane;
	}

	private BiModalJSplitPane getRightPane() {
		if (rightPane == null) {
			rightPane = new BiModalJSplitPane(SOUTH, JSplitPane.VERTICAL_SPLIT, getTopRightPane(),
					getSouthPanel().getThisComponent());
			rightPane.setDividerLocation(DEF_DIVIDER_LOATION);
			rightPane.setResizeWeight(1.0);
		}

		return rightPane;
	}

	private BiModalJSplitPane getTopRightPane() {
		if (topRightPane == null) {
			topRightPane = new BiModalJSplitPane(EAST, JSplitPane.HORIZONTAL_SPLIT, getCenterPanel(),
					getEastPanel().getThisComponent());
			topRightPane.setResizeWeight(1.0);
		}

		return topRightPane;
	}
	
	private JPanel getCenterPanel() {
		if (centerPanel == null) {
			centerPanel = new JPanel();
			centerPanel.setLayout(new CardLayout());
			
			// Null check is just for unit tests, because it's very hard to mock up the UI
			if (netViewMediator.getNetworkViewMainPanel() != null)
				centerPanel.add(netViewMediator.getNetworkViewMainPanel(), NetworkViewMainPanel.NAME);
			
			centerPanel.add(getStarterPanel(), StarterPanel.NAME);
		}
		
		return centerPanel;
	}
	
	private StarterPanel getStarterPanel() {
		if (starterPanel == null) {
			starterPanel = new StarterPanel(serviceRegistrar);
			starterPanel.getCloseButton().addActionListener(e -> hideStarterPanel());
		}

		return starterPanel;
	}

	private CytoPanelImpl getWestPanel() {
		if (westPanel == null) {
			westPanel = new CytoPanelImpl(WEST, JTabbedPane.TOP, DOCK, getSouthWestPanel(), JSplitPane.VERTICAL_SPLIT,
					1.0, serviceRegistrar);
		}

		return westPanel;
	}

	private CytoPanelImpl getEastPanel() {
		if (eastPanel == null) {
			eastPanel = new CytoPanelImpl(EAST, JTabbedPane.TOP, HIDE, serviceRegistrar);
		}

		return eastPanel;
	}

	private CytoPanelImpl getSouthPanel() {
		if (southPanel == null) {
			southPanel = new CytoPanelImpl(SOUTH, JTabbedPane.BOTTOM, DOCK, serviceRegistrar);
		}

		return southPanel;
	}

	private CytoPanelImpl getSouthWestPanel() {
		if (southWestPanel == null) {
			southWestPanel = new CytoPanelImpl(SOUTH_WEST, JTabbedPane.TOP, HIDE, serviceRegistrar);
		}
		
		return southWestPanel;
	}
}
