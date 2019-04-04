package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.application.swing.CytoPanelState.DOCK;
import static org.cytoscape.application.swing.CytoPanelState.HIDE;
import static org.cytoscape.internal.util.ViewUtil.invokeOnEDT;
import static org.cytoscape.internal.util.ViewUtil.invokeOnEDTAndWait;
import static org.cytoscape.internal.util.ViewUtil.isScreenMenuBar;
import static org.cytoscape.internal.util.ViewUtil.styleToolBarButton;
import static org.cytoscape.internal.view.CytoPanelNameInternal.BOTTOM;
import static org.cytoscape.internal.view.CytoPanelNameInternal.EAST;
import static org.cytoscape.internal.view.CytoPanelNameInternal.SOUTH;
import static org.cytoscape.internal.view.CytoPanelNameInternal.SOUTH_WEST;
import static org.cytoscape.internal.view.CytoPanelNameInternal.WEST;
import static org.cytoscape.util.swing.IconManager.ICON_WINDOW_RESTORE;
import static org.cytoscape.util.swing.LookAndFeelUtil.isNimbusLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.isWinLAF;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.cytoscape.app.event.AppsFinishedStartingEvent;
import org.cytoscape.app.event.AppsFinishedStartingListener;
import org.cytoscape.application.CyApplicationManager;
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
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.application.swing.ToolBarComponent;
import org.cytoscape.application.swing.events.CytoPanelStateChangedEvent;
import org.cytoscape.application.swing.events.CytoPanelStateChangedListener;
import org.cytoscape.internal.command.CommandToolPanel;
import org.cytoscape.model.CyNetwork;
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
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.StatusBarPanelFactory;

/*
 * #%L
 * Cytoscape Swing Application Impl (swing-application-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2019 The Cytoscape Consortium
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
 * The CytoscapeDesktop is the central Window for working with Cytoscape.<br><br>
 * Layout:<br><br>
 * <b>1. MASTER Pane:</b>
 * <pre>
 *  ___________________________
 * |         TOP Pane          |
 * |___________________________|
 * |        Automation         |
 * |___________________________|
 * </pre>
 * <b>2. TOP Pane:</b>
 * <pre>
 *  ___________________________
 * | LEFT Pane |  RIGHT Pane   |
 * |___________|_______________|
 * </pre>
 * <b>3. LEFT Pane:</b>
 * <pre>
 *  ___________
 * |    NW     |
 * |___________|
 * |    SW     |
 * |___________|
 * </pre>
 * <b>4. RIGHT Pane:</b>
 * <pre>
 *  ________________
 * | TOP-RIGHT Pane |
 * |________________|
 * |       S        |
 * |________________|
 * </pre>
 * <b>5. TOP-RIGHT Pane:</b>
 * <pre>
 *  ________________
 * |   C    |   E   |
 * |________|_______|
 * </pre>
 */
@SuppressWarnings("serial")
public class CytoscapeDesktop extends JFrame implements CySwingApplication, CyStartListener,
		AppsFinishedStartingListener, SessionLoadedListener, SessionSavedListener, SetCurrentNetworkListener,
		SetCurrentNetworkViewListener, TableAddedListener, CytoPanelStateChangedListener {

	private static final String TITLE_PREFIX_STRING = "Session: ";
	private static final String NEW_SESSION_NAME = "New Session";

	static final Dimension DEF_DESKTOP_SIZE = new Dimension(1300, 850);
	private static final int DEF_DIVIDER_LOATION = 540;

	private static final String SMALL_ICON = "/images/logo.png";
	
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

	private TrimBar westTrimBar;
	private TrimBar eastTrimBar;
	private ComponentPopup popup;
	private final ButtonGroup trimButtonGroup;
	
	private int lastPopupIndex = -1;
	private long lastPopupTime;
	private ButtonModel lastTrimButtonModel;
	private boolean isAdjusting;
	
	private BiModalJSplitPane masterPane;
	private BiModalJSplitPane topPane;
	private BiModalJSplitPane leftPane;
	private BiModalJSplitPane rightPane;
	private BiModalJSplitPane topRightPane;
	
	private CytoPanelImpl northWestPanel;
	private CytoPanelImpl southWestPanel;
	private CytoPanelImpl eastPanel;
	private CytoPanelImpl southPanel;
	private CytoPanelImpl automationPanel;

	// Status Bar TODO: Move this to log-swing to avoid cyclic dependency.
	private JPanel mainPanel;
	private JPanel centerPanel;
	private JToolBar statusToolBar;
	private StarterPanel starterPanel;
	private StatusBarPanelFactory taskStatusPanelFactory;
	private StatusBarPanelFactory jobStatusPanelFactory;
	
	// These Control Panel components must respect this order
	private List<String> controlComponentsOrder = Arrays.asList(
			"org.cytoscape.Network",
			"org.cytoscape.Style",
			"org.cytoscape.Filter",
			"org.cytoscape.Annotation"
	);
	
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
		
		// Modified ButtonGroup that allows a selected toggle button to be deselected
		// when it's clicked again
		trimButtonGroup = new ButtonGroup() {
			@Override
			public void setSelected(ButtonModel m, boolean b) {
				if (isAdjusting)
					return;
				if (m == lastTrimButtonModel) {
					isAdjusting = true;
					clearSelection();
					isAdjusting = false;
				} else {
					super.setSelected(m, b);
				}
				lastTrimButtonModel = getSelection();
			}
		};
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {
				// This is necessary because the same menu bar can be used by other frames
				final JMenuBar menuBar = cyMenus.getJMenuBar();
				final Window window = SwingUtilities.getWindowAncestor(menuBar);
				
				if (!CytoscapeDesktop.this.equals(window)) {
					if (window instanceof JFrame && !isScreenMenuBar()) {
						// Do this first, or the user could see the menu disappearing from the out-of-focus windows
						final JMenuBar dummyMenuBar = cyMenus.createDummyMenuBar();
						((JFrame) window).setJMenuBar(dummyMenuBar);
						dummyMenuBar.updateUI();
						window.repaint();
					}
					
					if (isScreenMenuBar())
						cyMenus.setMenuBarVisible(true);
					
					setJMenuBar(menuBar);
					menuBar.updateUI();
				}
				
				taskManager.setExecutionContext(CytoscapeDesktop.this);
				
				// Also hide the TrimBar popup
				disposeComponentPopup();
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

		setContentPane(getMainPanel());
		
		// Prepare to show the desktop...
		CytoPanelImpl[] allCytoPanels = new CytoPanelImpl[] { getNorthWestPanel(), getSouthWestPanel(), getEastPanel(),
				getSouthPanel(), getAutomationPanel() };
		
		for (CytoPanelImpl cp : allCytoPanels) {
			if (cp.getState() == HIDE)
				minimizeCytoPanel(cp);
		}
		
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
		final MemStatusPanel memStatusPanel = new MemStatusPanel(serviceRegistrar);
		
		final JToolBar statusToolBar = new JToolBar();
		statusToolBar.setFloatable(false);
		statusToolBar.setBorder(BorderFactory.createEmptyBorder());
		
		if (isNimbusLAF()) {
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
				.addGap(isWinLAF() ? 5 : 0)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, true)
						.addComponent(jobStatusPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(taskStatusPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(statusToolBar, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(memStatusPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				)
				.addGap(isWinLAF() ? 5 : 0)
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
		return getCytoPanelInternal(CytoPanelNameInternal.valueOf(compassDirection));
	}
	
	public CytoPanel getCytoPanel(final CytoPanelNameInternal compassDirection) {
		return getCytoPanelInternal(compassDirection);
	}

	private CytoPanelImpl getCytoPanelInternal(final CytoPanelNameInternal compassDirection) {
		switch (compassDirection) {
			case BOTTOM:
				return getAutomationPanel();
			case SOUTH:
				return getSouthPanel();
			case EAST:
				return getEastPanel();
			case WEST:
				return getNorthWestPanel();
			case SOUTH_WEST:
				return getSouthWestPanel();
		}

		throw new IllegalArgumentException(
				"Illegal Argument:  " + compassDirection + ".  Must be one of:  {SOUTH,EAST,WEST,SOUTH_WEST}.");
	}

	public void addCytoPanelComponent(CytoPanelComponent cp, Map<?, ?> props) {
		invokeOnEDTAndWait(() -> {
			final CytoPanelImpl impl;
			
			if (cp instanceof CommandToolPanel)
				impl = getCytoPanelInternal(BOTTOM);
			else
				impl = getCytoPanelInternal(CytoPanelNameInternal.valueOf(cp.getCytoPanelName()));

			int index = getInsertIndex(cp, impl);
			impl.insert(cp, index);
			
			if (impl.getState() == HIDE) {
				TrimBar.TrimStack ts = getTrimStackOf(impl);
				
				if (ts != null)
					ts.update();
			}
		});
	}

	public void removeCytoPanelComponent(CytoPanelComponent cp, Map<?, ?> props) {
		invokeOnEDTAndWait(() -> {
			final CytoPanelImpl impl;
			
			if (cp instanceof CommandToolPanel)
				impl = getCytoPanelInternal(BOTTOM);
			else
				impl = getCytoPanelInternal(CytoPanelNameInternal.valueOf(cp.getCytoPanelName()));
			
			impl.remove(cp);
			
			if (impl.getState() == HIDE) {
				TrimBar.TrimStack ts = getTrimStackOf(impl);
				
				if (ts != null)
					ts.update();
			}
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
		invokeOnEDT(() -> {
			if (e.getNetwork() != null && !isCommandDocGenNetwork(e.getNetwork()))
				hideStarterPanel();
		});
	}
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		invokeOnEDT(() -> {
			if (e.getNetworkView() != null && !isCommandDocGenNetwork(e.getNetworkView().getModel()))
				hideStarterPanel();
		});
	}
	
	private boolean isCommandDocGenNetwork(CyNetwork network) {
		if (network != null) {
			String name = network.getRow(network).get(CyNetwork.NAME, String.class);
			//Note: DO NOT CHANGE THE NETWORK NAME FROM "cy:command_documentation_generation", it is necessary for a workaround.
			return name != null && name.equals("cy:command_documentation_generation");
		}
		return false;
	}
	
	@Override
	public void handleEvent(TableAddedEvent e) {
		CyTable table = e.getTable();
		
		// It does not make sense to hide the Starter Panel when the table is private, because the user will not see it.
		// Also apps can create private tables during initialization, which would hide the Starter Panel by accident.
		if (table != null && table.isPublic()) {
			CyNetwork network = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetwork();
			if (network != null && !isCommandDocGenNetwork(network))
				invokeOnEDT(() -> hideStarterPanel());
		}
	}
	
	@Override
	public void handleEvent(CytoPanelStateChangedEvent e) {
		if (e.getCytoPanel() instanceof CytoPanelImpl == false)
			return;
		
		trimButtonGroup.clearSelection();
		
		CytoPanelImpl cytoPanel = (CytoPanelImpl) e.getCytoPanel();
		CytoPanelState state = e.getNewState();

		switch (state) {
			case HIDE:
				minimizeCytoPanel(cytoPanel);
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
	
	private int getInsertIndex(CytoPanelComponent cp, CytoPanelImpl impl) {
		int index = -1;
		int total = impl.getCytoPanelComponentCount();
		
		if (impl.getCytoPanelName() == CytoPanelName.WEST && cp instanceof CytoPanelComponent2) {
			String id = ((CytoPanelComponent2) cp).getIdentifier();
			index = controlComponentsOrder.indexOf(id);
			
			// If any of the next components have been inserted already, add the new one before that one
			if (index >= 0 && controlComponentsOrder.size() > index + 1) {
				for (int i = index + 1; i < controlComponentsOrder.size(); i++) {
					String nextId = controlComponentsOrder.get(i);
					int nextIndex = impl.indexOfComponent(nextId);
					
					if (nextIndex < 0)
						continue;
					
					if (index >= nextIndex) {
						index = nextIndex;
						break;
					}
				}
			}
		}
		
		if (index < 0 || index > total)
			index = total;
		
		return index;
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
		} else if (isMinimized(cytoPanel)) {
			getTrimBarOf(cytoPanel).removeStack(cytoPanel);
		}

		if (splitPane != null)
			splitPane.update();
	}
	
	private void minimizeCytoPanel(CytoPanelImpl cytoPanel) {
		hideCytoPanel(cytoPanel);
		
		TrimBar trimBar = getTrimBarOf(cytoPanel);
		trimBar.addStack(cytoPanel);
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
	
	private boolean isMinimized(CytoPanelImpl cytoPanel) {
		return getTrimBarOf(cytoPanel).contains(cytoPanel);
	}
	
	private void setLocationOfFloatingFrame(JFrame frame, CytoPanelImpl cytoPanel) {
		Toolkit tk = Toolkit.getDefaultToolkit();
		Dimension screenDimension = tk.getScreenSize();

		// Get Absolute Location and Bounds, relative to Screen
		Rectangle bounds = getBounds();
		bounds.setLocation(getLocationOnScreen());

		Point p = CytoPanelUtil.getLocationOfExternalWindow(screenDimension, bounds, frame.getSize(),
				cytoPanel.getCytoPanelNameInternal(), false);
		frame.setLocation(p);
		frame.setVisible(true);
	}
	
	private BiModalJSplitPane getSplitPaneOf(CytoPanelImpl cytoPanel) {
		switch (cytoPanel.getCytoPanelNameInternal()) {
			case BOTTOM:
				return getMasterPane();
			case SOUTH:
				return getRightPane();
			case EAST:
				return getTopRightPane();
			case WEST:
				return getLeftPane();
			case SOUTH_WEST:
				return getLeftPane();
			default:
				return null;
		}
	}
	
	private TrimBar getTrimBarOf(CytoPanelImpl cytoPanel) {
		switch (cytoPanel.getCytoPanelNameInternal()) {
			case WEST:
			case SOUTH_WEST:
				return getWestTrimBar();
			case EAST:
			case SOUTH:
			case BOTTOM:
			default:
				return getEastTrimBar();
		}
	}
	
	private TrimBar.TrimStack getTrimStackOf(CytoPanelImpl cytoPanel) {
		TrimBar trimBar = getTrimBarOf(cytoPanel);
		
		return trimBar != null ? trimBar.getStack(cytoPanel) : null;
	}
	
	private JPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			mainPanel.add(cyMenus.getJToolBar(), BorderLayout.NORTH);
			mainPanel.add(getMasterPane(), BorderLayout.CENTER);
			mainPanel.add(getWestTrimBar(), BorderLayout.WEST);
			mainPanel.add(getEastTrimBar(), BorderLayout.EAST);
		}
		
		return mainPanel;
	}
	
	private TrimBar getTrimBar(int compassDirection) {
		if (compassDirection == SwingConstants.WEST) return getWestTrimBar();
		if (compassDirection == SwingConstants.EAST) return getEastTrimBar();
		return null;
	}
	
	private TrimBar getWestTrimBar() {
		if (westTrimBar == null) {
			westTrimBar = new TrimBar(SwingConstants.WEST);
			westTrimBar.setVisible(false);
		}
		
		return westTrimBar;
	}
	
	public TrimBar getEastTrimBar() {
		if (eastTrimBar == null) {
			eastTrimBar = new TrimBar(SwingConstants.EAST);
			eastTrimBar.setVisible(false);
		}
		
		return eastTrimBar;
	}
	
	private BiModalJSplitPane getMasterPane() {
		if (masterPane == null) {
			masterPane = new BiModalJSplitPane(
					JSplitPane.VERTICAL_SPLIT,
					getTopPane(),
					getAutomationPanel().getThisComponent()
			);
			masterPane.setDividerLocation(600);
		}
		return masterPane;
	}

	private BiModalJSplitPane getTopPane() {
		if (topPane == null) {
			topPane = new BiModalJSplitPane(
					JSplitPane.HORIZONTAL_SPLIT,
					getLeftPane(),
					getRightPane()
			);
			topPane.setDividerLocation(400);
		}

		return topPane;
	}
	
	private BiModalJSplitPane getLeftPane() {
		if (leftPane == null) {
			leftPane = new BiModalJSplitPane(
					JSplitPane.VERTICAL_SPLIT,
					getNorthWestPanel().getThisComponent(),
					getSouthWestPanel().getThisComponent()
			);
			leftPane.setResizeWeight(1.0);
		}
		
		return leftPane;
	}

	private BiModalJSplitPane getRightPane() {
		if (rightPane == null) {
			rightPane = new BiModalJSplitPane(
					JSplitPane.VERTICAL_SPLIT,
					getTopRightPane(),
					getSouthPanel().getThisComponent()
			);
			rightPane.setDividerLocation(DEF_DIVIDER_LOATION);
			rightPane.setResizeWeight(1.0);
		}

		return rightPane;
	}

	private BiModalJSplitPane getTopRightPane() {
		if (topRightPane == null) {
			topRightPane = new BiModalJSplitPane(
					JSplitPane.HORIZONTAL_SPLIT,
					getCenterPanel(),
					getEastPanel().getThisComponent()
			);
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

	private CytoPanelImpl getNorthWestPanel() {
		if (northWestPanel == null) {
			northWestPanel = new CytoPanelImpl(WEST, 0, JTabbedPane.TOP, DOCK, serviceRegistrar);
		}

		return northWestPanel;
	}
	
	private CytoPanelImpl getSouthWestPanel() {
		if (southWestPanel == null) {
			southWestPanel = new CytoPanelImpl(SOUTH_WEST, 1, JTabbedPane.TOP, HIDE, serviceRegistrar);
		}
		
		return southWestPanel;
	}

	private CytoPanelImpl getEastPanel() {
		if (eastPanel == null) {
			eastPanel = new CytoPanelImpl(EAST, 2, JTabbedPane.TOP, HIDE, serviceRegistrar);
		}

		return eastPanel;
	}

	private CytoPanelImpl getSouthPanel() {
		if (southPanel == null) {
			southPanel = new CytoPanelImpl(SOUTH, 3, JTabbedPane.BOTTOM, DOCK, serviceRegistrar);
		}

		return southPanel;
	}

	private CytoPanelImpl getAutomationPanel() {
		if (automationPanel == null) {
			automationPanel = new CytoPanelImpl(BOTTOM, 4, JTabbedPane.RIGHT, HIDE, serviceRegistrar);
		}
		
		return automationPanel;
	}
	
	private void disposeComponentPopup() {
		if (popup != null) {
			popup.dispose();
			popup = null;
		}
	}
	
	private class TrimBar extends JPanel {
		
		private final Set<TrimStack> stacks;
		private final int compassDirection;
		
		public TrimBar(int compassDirection) {
			this.compassDirection = compassDirection;
			
			stacks = new TreeSet<>((t1, t2) -> {
				return t1.getCytoPanel().getTrimBarIndex() - t2.getCytoPanel().getTrimBarIndex();
			});
			
			if (compassDirection == SwingConstants.WEST)
				setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIManager.getColor("Separator.foreground")));
			else if (compassDirection == SwingConstants.EAST)
				setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, UIManager.getColor("Separator.foreground")));
			
			BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
			setLayout(layout);
		}
		
		TrimStack addStack(CytoPanelImpl cytoPanel) {
			TrimStack ts = new TrimStack(cytoPanel);
			stacks.remove(ts);
			stacks.add(ts);
			update();
			
			return ts;
		}
		
		void removeStack(CytoPanel cytoPanel) {
			Iterator<TrimStack> iter = stacks.iterator();
			
			while (iter.hasNext()) {
				if (iter.next().getCytoPanel().equals(cytoPanel)) {
					iter.remove();
					update();
					break;
				}
			}
		}
		
		TrimStack getStack(CytoPanelImpl cytoPanel) {
			for (TrimStack ts : stacks) {
				if (ts.getCytoPanel().equals(cytoPanel))
					return ts;
			}
			
			return null;
		}
		
		void update() {
			removeAll();
			
			for (TrimStack ts : stacks)
				add(ts);
			
			add(Box.createVerticalGlue());
			
			setVisible(!isEmpty());
			
			if (isVisible())
				updateUI();
		}

		boolean contains(CytoPanelImpl cytoPanel) {
			for (TrimStack ts : stacks) {
				if (ts.getCytoPanel().equals(cytoPanel))
					return true;
			}
			
			return false;
		}
		
		boolean isEmpty() {
			return stacks.isEmpty();
		}
		
		private class TrimStack extends JPanel {
			
			private static final String TOOL_TIP_RESTORE = "Restore";
			private static final int BTN_HPAD = 7;
			private static final int BTN_VPAD = 5;
			
			private JButton restoreButton;
			private final CytoPanelImpl cytoPanel;
			private final List<JToggleButton> trimButtons = new ArrayList<>();
			
			TrimStack(CytoPanelImpl cytoPanel) {
				this.cytoPanel = cytoPanel;
				
				setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createEmptyBorder(1, 1, 2, 1),
						BorderFactory.createCompoundBorder(
								BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground"), 1),
								BorderFactory.createEmptyBorder(1, 1, 1, 1)
						)
				));
				
				IconManager iconManager = serviceRegistrar.getService(IconManager.class);
				
				restoreButton = new JButton(ICON_WINDOW_RESTORE);
				restoreButton.setToolTipText(TOOL_TIP_RESTORE + " " + cytoPanel.getTitle());
				styleToolBarButton(restoreButton, iconManager.getIconFont(10), BTN_HPAD, BTN_VPAD);
				restoreButton.setAlignmentX(CENTER_ALIGNMENT);
				restoreButton.addActionListener(evt -> cytoPanel.setState(DOCK));
				
				BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
				setLayout(layout);
				
				update();
			}
			
			CytoPanelImpl getCytoPanel() {
				return cytoPanel;
			}
			
			JToggleButton getButton(int index) {
				return index >= 0 && index < trimButtons.size() ? trimButtons.get(index) : null;
			}
			
			void update() {
				removeAll();
				
				add(restoreButton);
				add(Box.createVerticalStrut(10));
				addComponents();
				add(Box.createVerticalStrut(5));
			}
			
			private void addComponents() {
				trimButtons.clear();
				JTabbedPane tabbedPane = cytoPanel.getTabbedPane();
				
				for (int i = 0; i < tabbedPane.getTabCount(); i++) {
					Component c = tabbedPane.getComponentAt(i);
					String title = tabbedPane.getTitleAt(i);
					Icon icon = tabbedPane.getIconAt(i);
					
					if ((title == null || title.trim().isEmpty()) && icon == null)
						continue;
					
					Icon buttonIcon = icon;
					
					if (buttonIcon == null)
						buttonIcon = new TextIcon(
								"" + title.charAt(0),
								UIManager.getFont("Button.font").deriveFont(Font.BOLD),
								CytoPanelUtil.BUTTON_SIZE,
								CytoPanelUtil.BUTTON_SIZE
						);
					else if (buttonIcon instanceof ImageIcon
							&& (buttonIcon.getIconWidth() > CytoPanelUtil.BUTTON_SIZE
							|| buttonIcon.getIconHeight() > CytoPanelUtil.BUTTON_SIZE))
						buttonIcon = IconManager.resizeIcon(buttonIcon, CytoPanelUtil.BUTTON_SIZE);

					final int index = i;
					
					JToggleButton btn = new JToggleButton(buttonIcon);
					btn.setToolTipText(title);
					btn.setAlignmentX(CENTER_ALIGNMENT);
					btn.addItemListener(evt -> {
						if (isAdjusting)
							return;
						if (evt.getStateChange() == ItemEvent.SELECTED)
							showComponentPopup(c, title, icon, index);
						else
							disposeComponentPopup();
					});
					
					styleToolBarButton(btn, null, BTN_HPAD, BTN_VPAD);
					
					// Make the button squared
					if (btn.getWidth() != btn.getHeight()) {
						int s = Math.max(btn.getWidth(), btn.getHeight());
						btn.setMinimumSize(new Dimension(s, s));
						btn.setMaximumSize(new Dimension(s, s));
						btn.setPreferredSize(new Dimension(s, s));
						btn.setSize(s, s);
					}
					
					trimButtons.add(btn);
					
					trimButtonGroup.add(btn);
					add(btn);
				}
			}
			
			private void showComponentPopup(Component c, String title, Icon icon, int index) {
				disposeComponentPopup(); // Always make sure the previous popup has been disposed
				
				if (index < 0) // Should not happen!
					return;
				
				// So clicking the same button again can actually dispose the popup,
				// otherwise it would dispose and then show the popup again
				if (index == lastPopupIndex && System.currentTimeMillis() - lastPopupTime < 100)
					return;
				
				lastPopupIndex = -1;
				lastPopupTime = 0;
				
				popup = new ComponentPopup(c, title, icon, index, cytoPanel);
				
				popup.addWindowFocusListener(new WindowFocusListener() {
					@Override
					public void windowLostFocus(WindowEvent evt) {
						if (evt.getOppositeWindow() == CytoscapeDesktop.this && popup != null) {
							lastPopupIndex = index;
							lastPopupTime = System.currentTimeMillis();
							
							isAdjusting = true;
							trimButtonGroup.clearSelection();
							isAdjusting = false;
							
							// Reset lastTrimButtonModel when clicking "background" and no trim button clicked
							Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
							boolean overTrimButton = false;
							
							for (JToggleButton btn : trimButtons) {
								Point buttonLoc = btn.getLocationOnScreen();
								mouseLoc.x -= buttonLoc.x;
								mouseLoc.y -= buttonLoc.y;
								
								if (btn.contains(mouseLoc)) {
									overTrimButton = true;
									break;
								}
							}
							
							if (!overTrimButton)
								lastTrimButtonModel = null;
							
							disposeComponentPopup();
						}
					}
					@Override
					public void windowGainedFocus(WindowEvent evt) {
						// Just ignore...
					}
				});
				
				// Adjust button selection
				JToggleButton btn = getButton(index);
				
				if (btn != null && !btn.isSelected()) {
					isAdjusting = true;
					btn.setSelected(true);
					isAdjusting = false;
				}
				
				// Show it -- get Absolute Location and Bounds, relative to Screen
				Rectangle bounds = TrimBar.this.getBounds();
				bounds.setLocation(TrimBar.this.getLocationOnScreen());
				Point p = bounds.getLocation();
				int offset = 2;
				
				p.y += offset;
				
				if (compassDirection == SwingConstants.WEST)
					p.x += (TrimBar.this.getWidth() + offset);
				else if (compassDirection == SwingConstants.EAST)
					p.x -= (popup.getSize().width + offset);
				
				popup.setLocation(p);
				popup.setVisible(true);
				c.setVisible(true);
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 11;
				result = prime * result + getOuterType().hashCode();
				result = prime * result + ((cytoPanel == null) ? 0 : cytoPanel.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (!(obj instanceof TrimStack))
					return false;
				TrimStack other = (TrimStack) obj;
				if (!getOuterType().equals(other.getOuterType()))
					return false;
				if (cytoPanel == null) {
					if (other.cytoPanel != null)
						return false;
				} else if (!cytoPanel.equals(other.cytoPanel)) {
					return false;
				}
				return true;
			}

			private TrimBar getOuterType() {
				return TrimBar.this;
			}
		}
	}
	
	private class ComponentPopup extends JDialog {
		
		private final Component comp;
		private final String title;
		private final Icon icon;
		private final int index;
		private final CytoPanelImpl cytoPanel;

		ComponentPopup(Component comp, String title, Icon icon, int index, CytoPanelImpl cytoPanel) {
			super(CytoscapeDesktop.this);
			this.comp = comp;
			this.title = title != null ? title.trim() : null;
			this.icon = icon;
			this.index = index;
			this.cytoPanel = cytoPanel;
			
			setUndecorated(true);
			getRootPane().setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createLineBorder(UIManager.getColor("Label.disabledForeground"), 1),
					BorderFactory.createEmptyBorder(4, 4, 4, 4)
			));
			
			JLabel titleLabel = new JLabel(title);
			titleLabel.setBorder(BorderFactory.createEmptyBorder(4, 4, 8, 4));
			
			if (title == null || title.isEmpty())
				titleLabel.setIcon(icon);
			
			cytoPanel.getTabbedPane().remove(comp);
			
			getContentPane().add(titleLabel, BorderLayout.NORTH);
			getContentPane().add(comp, BorderLayout.CENTER);
			pack();
		}
		
		int getIndex() {
			return index;
		}
		
		CytoPanelImpl getCytoPanel() {
			return cytoPanel;
		}
		
		Component getCytoPanelComponent() {
			return comp;
		}
		
		@Override
		public void dispose() {
			getContentPane().removeAll();
			cytoPanel.insert(comp, title, icon, index, false);
			
			super.dispose();
		}
	}
}
