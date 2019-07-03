package org.cytoscape.internal.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.cytoscape.internal.view.CytoPanelNameInternal.BOTTOM;
import static org.cytoscape.internal.view.CytoPanelNameInternal.EAST;
import static org.cytoscape.internal.view.CytoPanelNameInternal.SOUTH;
import static org.cytoscape.internal.view.CytoPanelNameInternal.SOUTH_WEST;
import static org.cytoscape.internal.view.CytoPanelNameInternal.WEST;
import static org.cytoscape.internal.view.CytoPanelStateInternal.DOCK;
import static org.cytoscape.internal.view.CytoPanelStateInternal.FLOAT;
import static org.cytoscape.internal.view.CytoPanelStateInternal.HIDE;
import static org.cytoscape.internal.view.CytoPanelStateInternal.MINIMIZE;
import static org.cytoscape.internal.view.CytoPanelStateInternal.UNDOCK;
import static org.cytoscape.internal.view.util.ViewUtil.invokeOnEDT;
import static org.cytoscape.internal.view.util.ViewUtil.invokeOnEDTAndWait;
import static org.cytoscape.internal.view.util.ViewUtil.isScreenMenuBar;
import static org.cytoscape.util.swing.LookAndFeelUtil.getSmallFontSize;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.isNimbusLAF;
import static org.cytoscape.util.swing.LookAndFeelUtil.isWinLAF;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;

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
import org.cytoscape.application.swing.ToolBarComponent;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedEvent;
import org.cytoscape.application.swing.events.CytoPanelComponentSelectedListener;
import org.cytoscape.internal.actions.CytoPanelAction;
import org.cytoscape.internal.command.CommandToolPanel;
import org.cytoscape.internal.util.CoalesceTimer;
import org.cytoscape.internal.view.util.ToggleableButtonGroup;
import org.cytoscape.internal.view.util.VerticalButtonUI;
import org.cytoscape.internal.view.util.ViewUtil;
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
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.util.swing.TextIcon;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.swing.StatusBarPanelFactory;
import org.jdesktop.swingx.border.DropShadowBorder;

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
public class CytoscapeDesktop extends JFrame
		implements CySwingApplication, CyStartListener, AppsFinishedStartingListener, SessionLoadedListener,
		SessionSavedListener, SetCurrentNetworkListener, SetCurrentNetworkViewListener, TableAddedListener,
		CytoPanelComponentSelectedListener {

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

	private SideBar westSideBar;
	private SideBar eastSideBar;
	private SideBar southSideBar;
	private ComponentPopup popup;
	/** This button group is used with buttons from all CytoPanels in state UNDOCK or MINIMIZE. */
	private final ToggleableButtonGroup undockedButtonGroup;
	private boolean isAdjusting;
	private boolean updatingTrimStack;
	
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
	private JPanel bottomPanel;
	private JToolBar statusToolBar;
	private StarterPanel starterPanel;
	private StatusBarPanelFactory taskStatusPanelFactory;
	private StatusBarPanelFactory jobStatusPanelFactory;
	
	private final GlassPaneMouseListener glassPaneMouseListener = new GlassPaneMouseListener();
	
	/** User preferred sizes for each cytopanel popup, to be set when the user manually resizes a popup. */
	private final Map<CytoPanelNameInternal, Dimension> undockPreferredSizes = new HashMap<>();
	private final Map<CytoPanelNameInternal, Dimension> dockPreferredSizes = new HashMap<>();
	
	private final CoalesceTimer resizeEventTimer = new CoalesceTimer(200, 1);
	
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
	
	private boolean appsFinishedStarting;
	
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

		if (MacFullScreenEnabler.supportsNativeFullScreenMode())
			MacFullScreenEnabler.setEnabled(this, true);

		// Don't automatically close window. Let shutdown.exit(returnVal) handle this
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		setJMenuBar(cyMenus.getJMenuBar());
		
		((JComponent) getGlassPane()).setLayout(null);
		
		undockedButtonGroup = new ToggleableButtonGroup(true) {
			@Override
			public void setSelected(ButtonModel model, boolean selected) {
				if (isAdjusting || selected == isSelected(model))
					return;
				
				isAdjusting = true;
				onSidebarButtonSelected((SidebarToggleButton) getButton(model), selected);
				super.setSelected(model, selected);
				isAdjusting = false;
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
			}
			@Override
			public void windowClosing(WindowEvent we) {
				final CyShutdown cyShutdown = serviceRegistrar.getService(CyShutdown.class);
				cyShutdown.exit(0);
			}
		});

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent evt) {
				// We need to do this later in the cycle to make sure everything is loaded
				if (jobStatusPanelFactory == null || taskStatusPanelFactory == null) {
					jobStatusPanelFactory = serviceRegistrar.getService(StatusBarPanelFactory.class, "(type=JobStatus)");
					taskStatusPanelFactory = serviceRegistrar.getService(StatusBarPanelFactory.class, "(type=TaskStatus)");
					statusToolBar = setupStatusPanel(jobStatusPanelFactory, taskStatusPanelFactory);
				}
			}
			@Override
			public void componentResized(ComponentEvent evt) {
				if (isVisible())
					resizeEventTimer.coalesce(() -> {
						invokeOnEDT(() -> {
							// Update the sidebars
							for (CytoPanelImpl cp : getAllCytoPanels()) {
								SideBar.TrimStack ts = getTrimStackOf(cp);
								
								if (ts != null && cp.getStateInternal() != HIDE)
									ts.update(); // TODO Probably too expensive to update everything here!!!
							}
						});
					});
			}
		});

		setContentPane(getMainPanel());
		
		// Prepare to show the desktop...
		for (CytoPanelImpl cp : getAllCytoPanels()) {
			handleStateInternalChanged(cp, cp.getStateInternal());
			addListeners(cp);
		}
		
		pack();
		setSize(DEF_DESKTOP_SIZE);
		
		// Move it to the center
		setLocationRelativeTo(null);

		// ...but don't actually show it!!!!
		// Once the system has fully started the JFrame will be set to 
		// visible by the StartupMostlyFinished class, found elsewhere.
	}

	private List<CytoPanelImpl> getAllCytoPanels() {
		return Arrays.asList(getNorthWestPanel(), getSouthWestPanel(), getEastPanel(), getSouthPanel(),
				getAutomationPanel());
	}
	
	private void addListeners(CytoPanelImpl cp) {
		cp.addPropertyChangeListener("stateInternal", evt -> {
			if (appsFinishedStarting && evt.getOldValue() == DOCK)
				saveDockedSize(cp);
			
			handleStateInternalChanged(cp, (CytoPanelStateInternal) evt.getNewValue());
		});
		
		cp.getFloatButton().addActionListener(evt -> cp.setStateInternal(FLOAT));
		cp.getDockButton().addActionListener(evt -> cp.setStateInternal(DOCK));
		cp.getUndockButton().addActionListener(evt -> cp.setStateInternal(UNDOCK));
		cp.getMinimizeButton().addActionListener(evt -> cp.setStateInternal(MINIMIZE));
		cp.getTitlePanel().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent evt) {
				if (evt.getClickCount() == 2)
					toggleMaximizeCytoPanel(cp);
			}
		});
	}

	private void handleStateInternalChanged(CytoPanelImpl cp, CytoPanelStateInternal state) {
		switch (state) {
			case HIDE:
				invokeOnEDT(() -> removeCytoPanel(cp));
				break;
			case MINIMIZE:
				invokeOnEDT(() -> minimizeCytoPanel(cp));
				break;
			case DOCK:
				invokeOnEDT(() -> dockCytoPanel(cp));
				break;
			case UNDOCK:
				invokeOnEDT(() -> undockCytoPanel(cp));
				break;
			case FLOAT:
				invokeOnEDT(() -> floatCytoPanel(cp));
				break;
		}
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
		
		getBottomPanel().add(statusPanel, BorderLayout.SOUTH);

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

	public void addCytoPanelComponent(CytoPanelComponent cpc, Map<?, ?> props) {
		invokeOnEDTAndWait(() -> {
			final CytoPanelImpl cytoPanel;
			
			if (cpc instanceof CommandToolPanel)
				cytoPanel = getCytoPanelInternal(BOTTOM);
			else
				cytoPanel = getCytoPanelInternal(CytoPanelNameInternal.valueOf(cpc.getCytoPanelName()));

			int index = getInsertIndex(cpc, cytoPanel);
			boolean inserted = cytoPanel.insert(cpc, index);
			getTrimStackOf(cytoPanel).update();
			
			// Automatically show the cytopanel again when it was empty and the first component is added
			if (cytoPanel.getCytoPanelComponentCount() == 1) {
				if (cytoPanel.getStateInternal() == DOCK) {
					BiModalJSplitPane splitPane = getSplitPaneOf(cytoPanel);
				
					if (splitPane != null)
						splitPane.update();
				
					getTrimStackOf(cytoPanel).update();
					getSideBarOf(cytoPanel).update();
					
					if (appsFinishedStarting && inserted)
						restoreDockedSize(cytoPanel);
				} else if (cytoPanel.getStateInternal() == FLOAT) {
					floatCytoPanel(cytoPanel);
				}
			}
		});
	}

	public void removeCytoPanelComponent(CytoPanelComponent cpc, Map<?, ?> props) {
		invokeOnEDTAndWait(() -> {
			final CytoPanelImpl cytoPanel;
			
			if (cpc instanceof CommandToolPanel)
				cytoPanel = getCytoPanelInternal(BOTTOM);
			else
				cytoPanel = getCytoPanelInternal(CytoPanelNameInternal.valueOf(cpc.getCytoPanelName()));
			
			boolean removed = cytoPanel.remove(cpc);
			
			if (removed && cytoPanel.getCytoPanelComponentCount() == 0) {
				if (cytoPanel.getStateInternal() == DOCK)
					saveDockedSize(cytoPanel);
				
				hideCytoPanel(cytoPanel);
			}
			
			getTrimStackOf(cytoPanel).update();
			getSideBarOf(cytoPanel).update();
			
			// Automatically hide the cytopanel when the last component is removed
			if (cytoPanel.getCytoPanelComponentCount() == 0 && cytoPanel.getStateInternal() == DOCK) {
				BiModalJSplitPane splitPane = getSplitPaneOf(cytoPanel);
				
				if (splitPane != null)
					splitPane.update();
				
				getSideBarOf(cytoPanel).update();
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
			
			// Update the Sidebar Stacks, as they need the whole desktop to be showing
			// in order to calculate their sizes correctly
			for (CytoPanelImpl cp : getAllCytoPanels()) {
				SideBar.TrimStack ts = getTrimStackOf(cp);
				
				if (ts != null && cp.getStateInternal() != HIDE)
					ts.update();
			}
			
			toFront();
		});
	}
	
	@Override
	public void handleEvent(AppsFinishedStartingEvent e) {
		invokeOnEDT(() -> {
			// Only show Starter Panel the first time if the initial session is empty
			// (for instance, Cytoscape can start up with a session file specified through the terminal)
			final CyNetworkManager netManager = serviceRegistrar.getService(CyNetworkManager.class);
			final CyTableManager tableManager = serviceRegistrar.getService(CyTableManager.class);
			
			if (netManager.getNetworkSet().isEmpty() && tableManager.getAllTables(false).isEmpty()) {
				showStarterPanel();
				
				if (getCenterPanel().getHeight() < getStarterPanel().getPreferredSize().height) {
					getRightPane().setDividerLocation(getStarterPanel().getPreferredSize().height + getRightPane().getDividerSize() / 2);
					getRightPane().resetToPreferredSizes();
				}
			}
			
			appsFinishedStarting = true;
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
	public void handleEvent(CytoPanelComponentSelectedEvent evt) {
		CytoPanel cp = evt.getCytoPanel();
		
		if (cp instanceof CytoPanelImpl && evt.getSelectedIndex() >= 0) {
			SideBar.TrimStack ts = getTrimStackOf((CytoPanelImpl) cp);
			
			if (ts != null) {
				JToggleButton btn = ts.getButton(evt.getSelectedIndex());
				CytoPanelStateInternal state = ((CytoPanelImpl) cp).getStateInternal();
				
				if (btn != null && !btn.isSelected() && state != HIDE) {
					if (undockedButtonGroup.contains(btn))
						undockedButtonGroup.setSelected(btn, true);
					else
						ts.getButtonGroup().setSelected(btn, true);
				}
			}
		}
	}
	
	public void showStarterPanel() {
		invokeOnEDT(() -> {
			getCenterPanel().add(getStarterPanel(), StarterPanel.NAME);
			getStarterPanel().update();
			((CardLayout) getCenterPanel().getLayout()).show(getCenterPanel(), StarterPanel.NAME);
			
			if (appsFinishedStarting) {
				if (getCenterPanel().getHeight() < getStarterPanel().getMinimumSize().height) {
					getRightPane().setDividerLocation(getStarterPanel().getMinimumSize().height + getRightPane().getDividerSize() / 2);
					getRightPane().resetToPreferredSizes();
				}
			}
			
			// Minimize any undocked cytopanel, because it could hide the Starter panel
			if (popup != null && popup.getCytoPanel() != null)
				popup.getCytoPanel().setStateInternal(MINIMIZE);
		});
	}
	
	public void hideStarterPanel() {
		invokeOnEDT(() -> {
			if (isStarterPanelVisible()) {
				getCenterPanel().remove(getStarterPanel());
				((CardLayout) getCenterPanel().getLayout()).show(getCenterPanel(), NetworkViewMainPanel.NAME);
			}
		});
	}
	
	public boolean isStarterPanelVisible() {
		return getStarterPanel().isVisible();
	}
	
	public boolean isShowSideBarLabels() {
		return Boolean.parseBoolean(ViewUtil.getViewProperty("sideBar.showLabels", "true", serviceRegistrar));
	}
	
	public void setShowSideBarLabels(boolean show) {
		ViewUtil.setViewProperty("sideBar.showLabels", "" + show, serviceRegistrar);
			
		for (CytoPanelImpl cp : getAllCytoPanels()) {
			SideBar.TrimStack ts = getTrimStackOf(cp);
			
			if (ts != null)
				ts.update();
		}
	}
	
	/**
	 * Only invoke this method when the CytoPanel is removed by a user action,
	 * because this also updates a CyProperty.
	 */
	private void removeCytoPanel(CytoPanelImpl cytoPanel) {
		cytoPanel.setStateInternal(HIDE);
		getTrimStackOf(cytoPanel).update();
		getSideBarOf(cytoPanel).update();
		
		if (isPopupShowingFor(cytoPanel))
			disposeComponentPopup();
		else if (getSplitPaneOf(cytoPanel) != null)
			getSplitPaneOf(cytoPanel).update();
	}

	private void showCytoPanel(CytoPanelImpl cytoPanel) {
		if (cytoPanel.getCytoPanelComponentCount() == 0)
			return;
		
		CytoPanelStateInternal state = cytoPanel.getStateInternal();
		
		if (state == HIDE)
			cytoPanel.setStateInternal(DOCK);
		
		getTrimStackOf(cytoPanel).update();
		getSideBarOf(cytoPanel).update();
	}
	
	private void floatCytoPanel(CytoPanelImpl cytoPanel) {
		if (isPopupShowingFor(cytoPanel)) {
			disposeComponentPopup();
			addToCytoPanelButtonGroup(cytoPanel);
		}
		
		showCytoPanel(cytoPanel);

		if (!isFloating(cytoPanel)) {
			BiModalJSplitPane splitPane = getSplitPaneOf(cytoPanel);
			
			if (splitPane != null)
				splitPane.removeCytoPanel(cytoPanel);
			
			// New window to place this CytoPanel
			JFrame frame = new JFrame(cytoPanel.getTitle(), getGraphicsConfiguration());
			floatingFrames.put(cytoPanel, frame);
			
			// Add listener to handle when window is closed
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					if (!ignoreFloatingFrameCloseEvents)
						cytoPanel.setStateInternal(DOCK);
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
		if (isPopupShowingFor(cytoPanel))
			disposeComponentPopup();
		
		// Make sure the button selection is correct
		addToCytoPanelButtonGroup(cytoPanel); // Make sure it belongs to the correct button group
		
		SideBar.TrimStack ts = getTrimStackOf(cytoPanel);
		JToggleButton selBtn = ts != null ? ts.getButton(cytoPanel.getSelectedIndex()) : null;
		
		if (selBtn != null)
			selBtn.setSelected(true);
		
		if (isFloating(cytoPanel))
			disposeFloatingCytoPanel(cytoPanel);

		showCytoPanel(cytoPanel);
		BiModalJSplitPane splitPane = getSplitPaneOf(cytoPanel);
		
		if (splitPane != null) {
			splitPane.addCytoPanel(cytoPanel);
			splitPane.update();
			
			if (appsFinishedStarting)
				restoreDockedSize(cytoPanel);
		}
	}

	private void undockCytoPanel(CytoPanelImpl cytoPanel) {
		if (isFloating(cytoPanel))
			disposeFloatingCytoPanel(cytoPanel);
		
		cytoPanel.getThisComponent().setVisible(true);
		
		if (popup == null) {
			popup = new ComponentPopup(cytoPanel);
			Toolkit.getDefaultToolkit().addAWTEventListener(glassPaneMouseListener, AWTEvent.MOUSE_EVENT_MASK);
		} else {
			CytoPanelImpl oldCytoPanel = popup.getCytoPanel();
			popup.setCytoPanel(cytoPanel);
			
			if (oldCytoPanel != null && !oldCytoPanel.equals(cytoPanel) && oldCytoPanel.getStateInternal() != MINIMIZE)
				oldCytoPanel.setStateInternal(MINIMIZE);
		}
		
		BiModalJSplitPane splitPane = getSplitPaneOf(cytoPanel);
		
		if (splitPane != null)
			splitPane.update();
		
		SideBar.TrimStack ts = getTrimStackOf(cytoPanel);
		ts.update();
		SideBar bar = getSideBarOf(cytoPanel);
		bar.update();
		
		if (!bar.isShowing())
			return;
		
		// Show it
		updateComponentPopupBounds();
		((JComponent) getGlassPane()).add(popup);
		
		if (!getGlassPane().isVisible())
			getGlassPane().setVisible(true);
		
		if (!popup.isVisible()) // To avoid flickering
			popup.setVisible(true);
		
		SidebarToggleButton btn = ts.getButton(cytoPanel.getSelectedIndex());
		
		if (btn != null && !btn.isSelected()) {
			btn.setSelected(true);
			ts.update();
		}
	}
	
	private void hideCytoPanel(CytoPanelImpl cytoPanel) {
		if (isFloating(cytoPanel)) {
			disposeFloatingCytoPanel(cytoPanel);
		} else if (isPopupShowingFor(cytoPanel)) {
			disposeComponentPopup();
			
			AbstractButton btn = undockedButtonGroup.getSelectedButton();
			
			if (btn != null)
				btn.setSelected(false);
			
			isAdjusting = true;
			undockedButtonGroup.clearSelection();
			isAdjusting = false;
		} else {
			BiModalJSplitPane splitPane = getSplitPaneOf(cytoPanel);
			
			if (splitPane != null)
				splitPane.update();
		}
	}
	
	private void minimizeCytoPanel(CytoPanelImpl cytoPanel) {
		hideCytoPanel(cytoPanel);
		
		if (!isPopupShowingFor(cytoPanel)) {
			// Adjust button selection
			SideBar.TrimStack ts = getTrimStackOf(cytoPanel);
			ts.getButtonGroup().clearSelection();
			
			addToUndockedButtonGroup(cytoPanel);
		}
	}
	
	private void disposeFloatingCytoPanel(CytoPanelImpl cytoPanel) {
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
	}

	private boolean isFloating(CytoPanelImpl cytoPanel) {
		JFrame frame = floatingFrames.get(cytoPanel);
		
		return frame != null && frame == SwingUtilities.getWindowAncestor(cytoPanel.getThisComponent());
	}
	
	private boolean isPopupShowingFor(CytoPanelImpl cytoPanel) {
		return popup != null && popup.getCytoPanel().equals(cytoPanel);
	}
	
	private void toggleMaximizeCytoPanel(CytoPanelImpl cytoPanel) {
		// For now, it can be maximized only when unpinned
		if (isPopupShowingFor(cytoPanel)) {
			cytoPanel.setMaximized(!cytoPanel.isMaximized());
			updateComponentPopupBounds();
		}
	}
	
	private void addToUndockedButtonGroup(CytoPanelImpl cytoPanel) {
		List<SidebarToggleButton> buttons = getSidebarButtons(cytoPanel);
		SideBar.TrimStack ts = getTrimStackOf(cytoPanel);
		
		if (ts != null)
			ts.getButtonGroup().remove(buttons);
		
		undockedButtonGroup.add(buttons);
	}
	
	private void addToCytoPanelButtonGroup(CytoPanelImpl cytoPanel) {
		List<SidebarToggleButton> buttons = getSidebarButtons(cytoPanel);
		undockedButtonGroup.remove(buttons);
		
		SideBar.TrimStack ts = getTrimStackOf(cytoPanel);
		
		if (ts != null)
			ts.getButtonGroup().add(buttons);
	}
	
	private List<SidebarToggleButton> getSidebarButtons(CytoPanelImpl cytoPanel) {
		SideBar.TrimStack ts = getTrimStackOf(cytoPanel);
		
		return ts != null ? ts.getAllButtons() : Collections.emptyList();
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
	
	private int getInsertIndex(CytoPanelComponent cpc, CytoPanelImpl cytoPanel) {
		int index = -1;
		int total = cytoPanel.getCytoPanelComponentCount();
		
		if (cytoPanel.getCytoPanelName() == CytoPanelName.WEST && cpc instanceof CytoPanelComponent2) {
			String id = ((CytoPanelComponent2) cpc).getIdentifier();
			index = controlComponentsOrder.indexOf(id);
			
			// If any of the next components have been inserted already, add the new one before that one
			if (index >= 0 && controlComponentsOrder.size() > index + 1) {
				for (int i = index + 1; i < controlComponentsOrder.size(); i++) {
					String nextId = controlComponentsOrder.get(i);
					int nextIndex = cytoPanel.indexOfComponent(nextId);
					
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
	
	private void onSidebarButtonSelected(SidebarToggleButton btn, boolean selected) {
		if (updatingTrimStack)
			return;
		
		CytoPanelImpl cytoPanel = btn.getCytoPanel();
		CytoPanelStateInternal state = cytoPanel.getStateInternal();
		
		if (state == HIDE)
			return;
		
		if (selected) {
			cytoPanel.setSelectedIndex(btn.getIndex());
			
			if (state == MINIMIZE) {
				cytoPanel.setStateInternal(UNDOCK);
			} else if (state == FLOAT) {
				JFrame frame = floatingFrames.get(cytoPanel);
				
				if (frame != null)
					frame.toFront();
			}
		} else {
			if (state == UNDOCK)
				cytoPanel.setStateInternal(MINIMIZE);
			else if (state == MINIMIZE) // TODO is this necessary???
				disposeComponentPopup();
		}
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
	
	private SideBar getSideBarOf(CytoPanelImpl cytoPanel) {
		switch (cytoPanel.getCytoPanelNameInternal()) {
			case SOUTH:
			case BOTTOM:
				return getSouthSideBar();
			case WEST:
			case SOUTH_WEST:
				return getWestSideBar();
			case EAST:
			default:
				return getEastSideBar();
		}
	}
	
	private SideBar.TrimStack getTrimStackOf(CytoPanelImpl cytoPanel) {
		SideBar bar = getSideBarOf(cytoPanel);
		
		return bar != null ? bar.getStack(cytoPanel) : null;
	}
	
	private JPanel getMainPanel() {
		if (mainPanel == null) {
			mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			mainPanel.add(cyMenus.getJToolBar(), BorderLayout.NORTH);
			mainPanel.add(getMasterPane(), BorderLayout.CENTER);
			mainPanel.add(getWestSideBar(), BorderLayout.WEST);
			mainPanel.add(getEastSideBar(), BorderLayout.EAST);
			mainPanel.add(getBottomPanel(), BorderLayout.SOUTH);
		}
		
		return mainPanel;
	}
	
	private SideBar getWestSideBar() {
		if (westSideBar == null) {
			westSideBar = new SideBar(SwingConstants.WEST, getNorthWestPanel(), getSouthWestPanel());
			westSideBar.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentHidden(ComponentEvent e) {
					if (southSideBar != null && southSideBar.isVisible())
						southSideBar.update(); // Just to update the south sidebar left padding
				}
				@Override
				public void componentShown(ComponentEvent e) {
					if (southSideBar != null && southSideBar.isVisible())
						southSideBar.update(); // Just to update the south sidebar left padding
				}
				@Override
				public void componentResized(ComponentEvent e) {
					if (southSideBar != null && southSideBar.isVisible())
						southSideBar.update(); // Just to update the south sidebar left padding
				}
			});
		}
		
		return westSideBar;
	}
	
	private SideBar getEastSideBar() {
		if (eastSideBar == null) {
			eastSideBar = new SideBar(SwingConstants.EAST, getEastPanel());
			eastSideBar.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentHidden(ComponentEvent e) {
					if (southSideBar != null && southSideBar.isVisible())
						southSideBar.update(); // Just to update the south sidebar right padding
				}
				@Override
				public void componentShown(ComponentEvent e) {
					if (southSideBar != null && southSideBar.isVisible())
						southSideBar.update(); // Just to update the south sidebar right padding
				}
				@Override
				public void componentResized(ComponentEvent e) {
					if (southSideBar != null && southSideBar.isVisible())
						southSideBar.update(); // Just to update the south sidebar left padding
				}
			});
		}
		
		return eastSideBar;
	}
	
	private SideBar getSouthSideBar() {
		if (southSideBar == null) {
			southSideBar = new SideBar(SwingConstants.SOUTH, getAutomationPanel(), getSouthPanel());
		}
		
		return southSideBar;
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
			topPane.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent evt) {
					updateComponentPopupBounds();
				}
			});
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
		}
		
		return centerPanel;
	}
	
	private JPanel getBottomPanel() {
		if (bottomPanel == null) {
			bottomPanel = new JPanel(new BorderLayout());
			bottomPanel.add(getSouthSideBar());
		}
		
		return bottomPanel;
	}
	
	public StarterPanel getStarterPanel() {
		if (starterPanel == null) {
			starterPanel = new StarterPanel(serviceRegistrar);
			starterPanel.getCloseButton().addActionListener(e -> hideStarterPanel());
		}

		return starterPanel;
	}

	private CytoPanelImpl getNorthWestPanel() {
		if (northWestPanel == null) {
			northWestPanel = new CytoPanelImpl(WEST, 0, DOCK, serviceRegistrar);
		}

		return northWestPanel;
	}
	
	private CytoPanelImpl getSouthWestPanel() {
		if (southWestPanel == null) {
			southWestPanel = new CytoPanelImpl(SOUTH_WEST, 1, MINIMIZE, serviceRegistrar);
		}
		
		return southWestPanel;
	}

	private CytoPanelImpl getEastPanel() {
		if (eastPanel == null) {
			eastPanel = new CytoPanelImpl(EAST, 2, DOCK, serviceRegistrar);
		}

		return eastPanel;
	}

	private CytoPanelImpl getSouthPanel() {
		if (southPanel == null) {
			southPanel = new CytoPanelImpl(SOUTH, 3, DOCK, serviceRegistrar);
		}

		return southPanel;
	}

	private CytoPanelImpl getAutomationPanel() {
		if (automationPanel == null) {
			automationPanel = new CytoPanelImpl(BOTTOM, 4, HIDE, serviceRegistrar);
		}
		
		return automationPanel;
	}
	
	private void saveDockedSize(CytoPanelImpl cytoPanel) {
		invokeOnEDT(() -> {
			if (cytoPanel != null)
				dockPreferredSizes.put(cytoPanel.getCytoPanelNameInternal(), cytoPanel.getThisComponent().getSize());
		});
	}
	
	private Dimension getDockedSize(CytoPanelImpl cytoPanel) {
		Dimension dim = dockPreferredSizes.get(cytoPanel.getCytoPanelNameInternal());
		
		return dim != null ? dim : cytoPanel.getThisComponent().getPreferredSize();
	}
	
	private void restoreDockedSize(CytoPanelImpl cytoPanel) {
		Dimension dim = getDockedSize(cytoPanel);
		
		if (dim != null) {
			if (cytoPanel.getCytoPanelNameInternal() == EAST) {
				int w = getTopRightPane().getWidth() - dim.width - ViewUtil.DIVIDER_SIZE;

				if (w >= 0)
					getCenterPanel().setPreferredSize(new Dimension(w, getCenterPanel().getPreferredSize().height));
			} else if (cytoPanel.getCytoPanelNameInternal() == SOUTH) {
				int h = getRightPane().getHeight() - dim.height - ViewUtil.DIVIDER_SIZE;
				
				if (h >= 0)
					getTopRightPane().setPreferredSize(new Dimension(getTopRightPane().getPreferredSize().width, h));
			}
			
			cytoPanel.getThisComponent().setPreferredSize(dim);
			BiModalJSplitPane splitPane = getSplitPaneOf(cytoPanel);
			
			if (splitPane != null)
				splitPane.resetToPreferredSizes();
		}
	}
	
	private void updateComponentPopupBounds() {
		if (popup == null)
			return;
		
		CytoPanelImpl cytoPanel = popup.getCytoPanel();
		
		if (cytoPanel == null)
			return;
		
		SideBar bar = getSideBarOf(cytoPanel);
		CytoPanelNameInternal name = cytoPanel.getCytoPanelNameInternal();
		
		try {
			Dimension maxDim = popup.getMaximumSize();
			Dimension newDim = cytoPanel.isMaximized() ? maxDim : undockPreferredSizes.get(name);
			Dimension dim = newDim != null ? newDim : cytoPanel.getThisComponent().getPreferredSize();
			
			if (newDim == null) {
				if (name == SOUTH) {
					dim.width = (int) (maxDim.width * 0.75f);
					dim.height = (int) (maxDim.height * 0.5f);
				} else if (name == BOTTOM) {
					dim.width = (int) (maxDim.width * 0.5f);
					dim.height = (int) (maxDim.height * 0.75f);
				} else {
					if (dim.width <= 0)
						dim.width = 200;
					if (dim.height <= 0)
						dim.height = maxDim.height;
				}
			}
			
			// Max size
			dim.height = Math.min(dim.height, maxDim.height);
			// Min size
			dim.width = Math.max(dim.width, ComponentPopup.MIN_SIZE);
			dim.height = Math.max(dim.height, ComponentPopup.MIN_SIZE);
			
			Point p = bar.compassDirection == SwingConstants.SOUTH ? getBottomPanel().getLocation() : bar.getLocation();
			p = SwingUtilities.convertPoint(getMainPanel(), p, getGlassPane());
			
			if (bar.compassDirection == SwingConstants.WEST) {
				p.x += bar.getWidth();
				
				if (name == SOUTH_WEST) 
					p.y += (bar.getHeight() - dim.height);
			} else if (bar.compassDirection == SwingConstants.EAST) {
				p.x -= dim.width;
			} else if (bar.compassDirection == SwingConstants.SOUTH) {
				if (name == SOUTH) {
					p.x += (bar.getWidth() - dim.width);
					
					if (getEastSideBar().isShowing())
						p.x -= getEastSideBar().getPreferredSize().width;
				} else { // BOTTOM
					p.x += getWestSideBar().getPreferredSize().width;
				}
				
				p.y -= dim.height;
			}
			
			popup.setBounds(p.x, p.y, dim.width, dim.height);
			cytoPanel.getThisComponent().revalidate();
		} catch (Exception e) {
			// Just ignore...
		}
	}
	
	private void disposeComponentPopup() {
		if (popup != null) {
			Toolkit.getDefaultToolkit().removeAWTEventListener(glassPaneMouseListener);
			
			((JComponent) getGlassPane()).remove(popup);
			getGlassPane().setVisible(false);
			
			popup.dispose();
			popup = null;
		}
	}
	
	private class SideBar extends JPanel {
		
		private static final int BORDER_WIDTH = 1;
		
		private final Map<CytoPanelImpl, TrimStack> stacks = new LinkedHashMap<>();
		private final int compassDirection;
		
		public SideBar(int compassDirection, CytoPanelImpl... cytoPanels) {
			this.compassDirection = compassDirection;
			
			final Color borderColor = UIManager.getColor("Separator.foreground");
			
			if (compassDirection == SwingConstants.WEST)
				setBorder(BorderFactory.createMatteBorder(0, 0, 0, BORDER_WIDTH, borderColor));
			else if (compassDirection == SwingConstants.EAST)
				setBorder(BorderFactory.createMatteBorder(0, BORDER_WIDTH, 0, 0, borderColor));
			else if (compassDirection == SwingConstants.SOUTH)
				setBorder(BorderFactory.createMatteBorder(BORDER_WIDTH, 0, 0, 0, borderColor));
			
			BoxLayout layout = new BoxLayout(this,
					compassDirection == SwingConstants.SOUTH ? BoxLayout.X_AXIS : BoxLayout.Y_AXIS);
			setLayout(layout);
			
			// Create Trim Stacks
			for (CytoPanelImpl cp : cytoPanels) {
				TrimStack ts = new TrimStack(cp);
				stacks.put(cp, ts);
			}
			
			addMouseListener(new ContextMenuMouseListener());
			update();
		}
		
		TrimStack getStack(CytoPanelImpl cytoPanel) {
			return stacks.get(cytoPanel);
		}
		
		void update() {
			removeAll();
			
			if (compassDirection == SwingConstants.SOUTH && westSideBar != null && westSideBar.isVisible()
					&& westSideBar.getWidth() > 0)
				add(Box.createHorizontalStrut(westSideBar.getWidth() - SideBar.BORDER_WIDTH));
			
			boolean visible = false;
			int i = 0;
			
			for (TrimStack ts : stacks.values()) {
				if (ts.isVisible())
					visible = true;
				
				add(ts);
				
				if (i == 0)
					add(compassDirection == SwingConstants.SOUTH ? Box.createHorizontalGlue()
							: Box.createVerticalGlue());

				i++;
			}
			
			if (compassDirection == SwingConstants.SOUTH && eastSideBar != null && eastSideBar.isVisible()
					&& eastSideBar.getWidth() > 0)
				add(Box.createHorizontalStrut(eastSideBar.getWidth() - SideBar.BORDER_WIDTH));
			
			if (visible != isVisible())
				setVisible(visible);
			
			if (isVisible())
				updateUI();
		}

		private class TrimStack extends JPanel {
			
			private static final int BTN_HPAD = 8;
			private static final int BTN_VPAD = 4;
			
			private final CytoPanelImpl cytoPanel;
			private final int orientation;
			private final List<SidebarToggleButton> buttons = new ArrayList<>();
			/** Used when the CytoPanel state is DOCK or FLOAT. */
			private final ToggleableButtonGroup buttonGroup;
			
			TrimStack(CytoPanelImpl cytoPanel) {
				this.cytoPanel = cytoPanel;
				this.orientation = compassDirection == SwingConstants.SOUTH ? SwingConstants.HORIZONTAL
						: SwingConstants.VERTICAL;
				
				BoxLayout layout = new BoxLayout(this,
						orientation == SwingConstants.VERTICAL ? BoxLayout.X_AXIS : BoxLayout.Y_AXIS);
				setLayout(layout);
				
				addMouseListener(new ContextMenuMouseListener(cytoPanel));
				
				buttonGroup = new ToggleableButtonGroup() {
					@Override
					public void setSelected(ButtonModel model, boolean selected) {
						if (isAdjusting || selected == isSelected(model))
							return;
						
						isAdjusting = true;
						onSidebarButtonSelected((SidebarToggleButton) getButton(model), selected);
						super.setSelected(model, selected);
						isAdjusting = false;
					}
				};
				
				update();
			}
			
			SidebarToggleButton getButton(int index) {
				return index >= 0 && index < buttons.size() ? buttons.get(index) : null;
			}
			
			List<SidebarToggleButton> getAllButtons() {
				return new ArrayList<>(buttons);
			}
			
			ToggleableButtonGroup getButtonGroup() {
				return buttonGroup;
			}
			
			void update() {
				updatingTrimStack = true;
				
				try {
					if (cytoPanel.getStateInternal() == MINIMIZE || cytoPanel.getStateInternal() == UNDOCK)
						buttonGroup.remove(buttons);
					else
						undockedButtonGroup.remove(buttons);
					
					if (cytoPanel.getStateInternal() == HIDE) {
						setVisible(false);
						return;
					}
					
					// Remove all buttons
					removeAll();
					Map<CytoPanelComponent, SidebarToggleButton> oldButtons = buttons.stream()
							.collect(Collectors.toMap(SidebarToggleButton::getCytoPanelComponent, Function.identity()));
					buttons.clear();
					
					// Create new buttons
					JPanel panel = null;
					final boolean showLabels = isShowSideBarLabels();
					int totalEdge = 0; // Total width or height of added buttons
					List<CytoPanelComponent> cpComponents = cytoPanel.getCytoPanelComponents();
					int i = 0;
					
					for (CytoPanelComponent cpc : cpComponents) {
						final int index = i++;
						// First check if we already have a button for this CytoPanelComponent
						SidebarToggleButton btn = oldButtons.remove(cpc);
						
						if (btn == null) {
							// This means this CytoPanelComponent has been added recently
							// and it does not have a button yet, so let's create one
							String title = cpc.getTitle();
							Icon icon = cpc.getIcon();
							
							if ((title == null || title.trim().isEmpty()) && icon == null)
								continue;
							
							Icon buttonIcon = icon;
							
							if (buttonIcon == null) {
								buttonIcon = ViewUtil.createDefaultIcon(title, CytoPanelUtil.BUTTON_SIZE,
										serviceRegistrar.getService(IconManager.class));
							} else if (buttonIcon instanceof ImageIcon) {
								if (showLabels && buttonIcon.getIconHeight() > CytoPanelUtil.BUTTON_SIZE)
									buttonIcon = ViewUtil.resizeIcon(buttonIcon, CytoPanelUtil.BUTTON_SIZE);
								else if (!showLabels && (buttonIcon.getIconHeight() > CytoPanelUtil.BUTTON_SIZE
										|| buttonIcon.getIconWidth() > CytoPanelUtil.BUTTON_SIZE))
									buttonIcon = IconManager.resizeIcon(buttonIcon, CytoPanelUtil.BUTTON_SIZE);
							}
		
							final SidebarToggleButton newButton = btn = new SidebarToggleButton(cytoPanel, index);
							btn.setIcon(buttonIcon);
							btn.setToolTipText(title);
							btn.setAlignmentX(CENTER_ALIGNMENT);
							
							btn.addItemListener(evt -> ViewUtil.updateToolBarStyle(newButton, false));
							btn.addMouseListener(new ContextMenuMouseListener(cytoPanel));
						}
						
						buttons.add(btn);
						
						// Always reset the text, ButtonUI and button's size
						btn.setText(showLabels ? cpc.getTitle() : null);
						
						if (showLabels && orientation == SwingConstants.VERTICAL)
							btn.setUI(new VerticalButtonUI(compassDirection == SwingConstants.EAST));
						else
							btn.setUI(new BasicButtonUI());
						
						btn.setPreferredSize(null);
						Dimension d = btn.getPreferredSize();
						
						if (orientation == SwingConstants.VERTICAL)
							d = new Dimension(d.width + 2 * BTN_VPAD, d.height + 2 * BTN_HPAD);
						else
							d = new Dimension(d.width + 2 * BTN_HPAD, d.height + 2 * BTN_VPAD);
						
						btn.setPreferredSize(d);
						btn.setMinimumSize(d);
						btn.setMaximumSize(d);
						btn.setSize(d);
						ViewUtil.updateToolBarStyle(btn, false);
						
						// The remaining old buttons are buttons from CytoPanelComponents that have been removed
						if (!oldButtons.isEmpty()) {
							buttonGroup.remove(oldButtons.values());
							undockedButtonGroup.remove(oldButtons.values());
						}
						
						// Add all buttons to correct button group again
						if (cytoPanel.getStateInternal() == MINIMIZE || cytoPanel.getStateInternal() == UNDOCK) {
							undockedButtonGroup.add(btn);
						} else {
							buttonGroup.add(btn);
							
							// Restore button selection
							JToggleButton selBtn = getButton(cytoPanel.getSelectedIndex());
							
							if (selBtn != null)
								buttonGroup.setSelected(selBtn, true);
						}
						
						int buttonEdge = orientation == SwingConstants.VERTICAL ? d.height : d.width;
						
						if (panel != null && SideBar.this.getSize() != null) {
							int barEdge = orientation == SwingConstants.VERTICAL ?
									SideBar.this.getSize().height : SideBar.this.getSize().width;
							
							for (TrimStack ts : stacks.values()) {
								if (ts != this && ts.getSize() != null)
									barEdge -= orientation == SwingConstants.VERTICAL ?
											ts.getSize().height : ts.getSize().width;
							}
									
							if (totalEdge + buttonEdge > barEdge)
								panel = null;
						}
						
						if (panel == null) {
							panel = new JPanel();
							panel.setAlignmentX(cytoPanel.getCytoPanelName() == CytoPanelName.SOUTH ?
									RIGHT_ALIGNMENT : LEFT_ALIGNMENT);
							panel.setAlignmentY(TOP_ALIGNMENT);
							
							BoxLayout layout = new BoxLayout(panel,
									orientation == SwingConstants.VERTICAL ? BoxLayout.Y_AXIS : BoxLayout.X_AXIS);
							panel.setLayout(layout);
							
							// When adding more columns/rows of buttons, the first buttons
							// should be closer to the CytoPanel, which is why the WEST SideBar
							// adds new columns to the left
							if (compassDirection == SwingConstants.WEST)
								add(panel, 0);
							else
								add(panel);
							
							totalEdge = 0;
						}
						
						totalEdge += buttonEdge;
						panel.add(btn);
					}
					
					setVisible(!buttons.isEmpty());
					
					if (isVisible()) {
						if (!showLabels)
							LookAndFeelUtil.equalizeSize(buttons.toArray(new JComponent[buttons.size()]));
						
						// Align columns/rows of buttons accordingly
						if (compassDirection == SwingConstants.WEST)
							add(Box.createHorizontalGlue(), 0);
						else if (compassDirection == SwingConstants.SOUTH)
							add(Box.createVerticalGlue());
						else
							add(Box.createHorizontalGlue());
						
						updateUI();
					}
				} finally {
					updatingTrimStack = false;
				}
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

			private SideBar getOuterType() {
				return SideBar.this;
			}
		}
		
		private class ContextMenuMouseListener extends MouseAdapter {
			
			private final CytoPanelImpl cytoPanel;

			public ContextMenuMouseListener() {
				this(null);
			}
			
			public ContextMenuMouseListener(CytoPanelImpl cytoPanel) {
				this.cytoPanel = cytoPanel;
			}

			@Override
			public void mousePressed(MouseEvent evt) {
				maybeShowContextMenu(evt);
			}

			@Override
			public void mouseReleased(final MouseEvent evt) {
				maybeShowContextMenu(evt);
			}
			
			private void maybeShowContextMenu(MouseEvent evt) {
				if (!evt.isPopupTrigger())
					return;
				
				invokeOnEDT(() -> {
					JPopupMenu menu = new JPopupMenu();
					
					if (cytoPanel != null) {
						String title = cytoPanel.getTitle();
						
						menu.add(createMenuItemFromButton(cytoPanel.getFloatButton()));
						menu.add(createMenuItemFromButton(cytoPanel.getDockButton()));
						menu.add(createMenuItemFromButton(cytoPanel.getUndockButton()));
						menu.add(createMenuItemFromButton(cytoPanel.getMinimizeButton()));
						menu.addSeparator();
						{
							JMenuItem mi = new JMenuItem(CytoPanelImpl.TEXT_HIDE + " " + title);
							mi.addActionListener(e -> removeCytoPanel(cytoPanel));
							mi.setEnabled(cytoPanel.getStateInternal() != HIDE);
							menu.add(mi);
						}
					} else {
						for (CytoPanelImpl cp : getAllCytoPanels()) {
							CytoPanelStateInternal state = cp.getStateInternal();
							CytoPanelAction action = new CytoPanelAction(cp.getCytoPanelNameInternal(),
									CytoscapeDesktop.this, 0.0f, serviceRegistrar);
							
							JMenuItem mi = new JCheckBoxMenuItem(action);
							mi.setSelected(state != HIDE);
							menu.add(mi);
							action.updateEnableState();
						}
					}
					
					menu.addSeparator();
					{
						JMenuItem mi = new JCheckBoxMenuItem("Show Labels");
						mi.addActionListener(e -> setShowSideBarLabels(mi.isSelected()));
						mi.setSelected(isShowSideBarLabels());
						menu.add(mi);
					}
					
					Component parent = (Component) evt.getSource();
					menu.show(parent, evt.getX(), evt.getY());
				});
			}
		}

		public JMenuItem createMenuItemFromButton(JButton btn) {
			int iconSize = 16;
			
			JMenuItem mi = new JMenuItem(btn.getToolTipText());
			mi.setIcon(new TextIcon(btn.getText(), btn.getFont(), iconSize, iconSize));
			mi.addActionListener(e -> btn.doClick());
			mi.setEnabled(btn.isVisible() && btn.isEnabled());
			
			return mi;
		}
	}
	
	private class SidebarToggleButton extends JToggleButton {
		
		private final CytoPanelImpl cytoPanel;
		private final CytoPanelComponent cytoPanelComponent;
		private final int index;

		public SidebarToggleButton(CytoPanelImpl cytoPanel, int index) {
			this.cytoPanel = cytoPanel;
			this.cytoPanelComponent = cytoPanel.getCytoPanelComponentAt(index);
			this.index = index;
			
			setFont(getFont().deriveFont(getSmallFontSize()));
			setFocusPainted(false);
			setFocusable(false);
			setBorder(BorderFactory.createEmptyBorder());
			setContentAreaFilled(false);
			setOpaque(true);
		}
		
		CytoPanelImpl getCytoPanel() {
			return cytoPanel;
		}
		
		CytoPanelComponent getCytoPanelComponent() {
			return cytoPanelComponent;
		}
		
		public int getIndex() {
			return index;
		}
		
		@Override
		public String toString() {
			return index +  ": '" + getText() + "'";
		}
	}
	
	private class ComponentPopup extends JRootPane {
		
		private static final int IN_BORDER_WIDTH = 1;
		private static final int OUT_BORDER_WIDTH = ViewUtil.DIVIDER_SIZE;
		private static final int BORDER_WIDTH = IN_BORDER_WIDTH + OUT_BORDER_WIDTH;
		private static final int MIN_SIZE = 100 + BORDER_WIDTH;
		
		// Border widths: Top, Left, Bottom. Right
		private int tb, lb, bb, rb;
		
		private int[] locations = {
				SwingConstants.NORTH,
				SwingConstants.SOUTH,
				SwingConstants.WEST,
				SwingConstants.EAST,
				SwingConstants.NORTH_WEST,
				SwingConstants.NORTH_EAST,
				SwingConstants.SOUTH_WEST,
				SwingConstants.SOUTH_EAST
		};

		private int[] cursors = {
				Cursor.N_RESIZE_CURSOR,
				Cursor.S_RESIZE_CURSOR,
				Cursor.W_RESIZE_CURSOR,
				Cursor.E_RESIZE_CURSOR,
				Cursor.NW_RESIZE_CURSOR,
				Cursor.NE_RESIZE_CURSOR,
				Cursor.SW_RESIZE_CURSOR,
				Cursor.SE_RESIZE_CURSOR
		};

		private CytoPanelImpl cytoPanel;
		
		private Point dragPoint;
		private int cursor = Cursor.DEFAULT_CURSOR;

		ComponentPopup(CytoPanelImpl cytoPanel) {
			this.cytoPanel = cytoPanel;
			
			if (isNimbusLAF())
				setOpaque(false); // So the drag-border is also transparent
			
			addListeners();
			update();
		}
		
		@Override
		public Dimension getMaximumSize() {
			Dimension dim = getTopPane().getSize();
			// Adds the shadow width to max width/height
			// so the alignment with the other components feels natural
			dim.width += OUT_BORDER_WIDTH;
			dim.height += OUT_BORDER_WIDTH;
			
			return dim;
		}
		
		void dispose() {
			setVisible(false);
			getContentPane().removeAll();
			cytoPanel = null;
		}
		
		void update() {
			getContentPane().removeAll();
			updateBorder();
			Component c = cytoPanel != null ? cytoPanel.getThisComponent() : null;
			
			if (c != null) {
				getContentPane().add(c, BorderLayout.CENTER);
				c.revalidate();
				c.repaint();
			}
		}
		
		void updateBorder() {
			tb = lb = bb = rb = 0;
			CytoPanelNameInternal name = cytoPanel.getCytoPanelNameInternal();
			
			switch (name) {
				case WEST:       rb = bb = BORDER_WIDTH; break;
				case SOUTH_WEST: rb = tb = BORDER_WIDTH; break;
				case EAST:       lb = bb = BORDER_WIDTH; break;
				case SOUTH:      lb = tb = BORDER_WIDTH; break;
				case BOTTOM:     rb = tb = BORDER_WIDTH; break;
			}
			
			final Border outBorder;
			
			if (isWinLAF() || isAquaLAF()) {
				// This shadow border does not work on Nimbus and causes NullPointerExceptions!
				DropShadowBorder shadow = new DropShadowBorder();
		        shadow.setShadowColor(UIManager.getColor("Label.foreground"));
		        shadow.setShadowOpacity(0.1f);
		        shadow.setShadowSize(OUT_BORDER_WIDTH);
		        shadow.setShowLeftShadow(lb > 0);
		        shadow.setShowRightShadow(rb > 0);
		        shadow.setShowBottomShadow(bb > 0);
		        shadow.setShowTopShadow(tb > 0);
		        outBorder = shadow;
			} else {
				// Add a transparent border to make the drag-resize action easier
				// and to keep the border width consistent no matter the look-and-feel.
				outBorder = BorderFactory.createMatteBorder(
						Math.max(0, tb - IN_BORDER_WIDTH),
						Math.max(0, lb - IN_BORDER_WIDTH),
						Math.max(0, bb - IN_BORDER_WIDTH),
						Math.max(0, rb - IN_BORDER_WIDTH),
						new Color(0 , 0, 0, 0)
				);
			}
			
			getRootPane().setBorder(BorderFactory.createCompoundBorder(
					outBorder,
					BorderFactory.createMatteBorder(
							Math.max(0, tb - OUT_BORDER_WIDTH),
							Math.max(0, lb - OUT_BORDER_WIDTH),
							Math.max(0, bb - OUT_BORDER_WIDTH),
							Math.max(0, rb - OUT_BORDER_WIDTH),
							UIManager.getColor("Separator.foreground"))
			));
		}
		
		void setCytoPanel(CytoPanelImpl cytoPanel) {
			if (this.cytoPanel != cytoPanel) {
				this.cytoPanel = cytoPanel;
				update();
			}
		}

		CytoPanelImpl getCytoPanel() {
			return cytoPanel;
		}
		
		private void addListeners() {
			getRootPane().addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent evt) {
		            cursor = getCursor(evt);
		            dragPoint = evt.getPoint();
		            requestFocus();
		            repaint();
				}
				@Override
				public void mouseReleased(MouseEvent evt) {
					cursor = Cursor.DEFAULT_CURSOR;
					dragPoint = null;
					setCursor(Cursor.getDefaultCursor());
					revalidate();
				}
				@Override
				public void mouseExited(MouseEvent evt) {
					setCursor(Cursor.getDefaultCursor());
				}
			});
			getRootPane().addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseMoved(MouseEvent evt) {
					setCursor(Cursor.getPredefinedCursor(getCursor(evt)));
				}
				@Override
				public void mouseDragged(MouseEvent evt) {
					if (dragPoint != null) {
						setCursor(Cursor.getPredefinedCursor(cursor));
						
						int x = getX();
						int y = getY();
						int w = getWidth();
						int h = getHeight();

						int dx = evt.getX() - dragPoint.x;
						int dy = evt.getY() - dragPoint.y;
						
						switch (cursor) {
							case Cursor.N_RESIZE_CURSOR:
								y += dy;
								h -= dy;
								break;
	
							case Cursor.S_RESIZE_CURSOR:
								dragPoint = evt.getPoint();
								h += dy;
								break;
	
							case Cursor.W_RESIZE_CURSOR:
								x += dx;
								w -= dx;
								break;
	
							case Cursor.E_RESIZE_CURSOR:
								w += dx;
								dragPoint = evt.getPoint();
								break;
	
							case Cursor.NW_RESIZE_CURSOR:
								x += dx;
								y += dy;
								w -= dx;
								h -= dy;
								break;
	
							case Cursor.NE_RESIZE_CURSOR:
								y += dy;
								w += dx;
								h -= dy;
								dragPoint = new Point(evt.getX(), dragPoint.y);
								break;
	
							case Cursor.SW_RESIZE_CURSOR:
								x += dx;
								w -= dx;
								h += dy;
								dragPoint = new Point(dragPoint.x, evt.getY());
								break;
	
							case Cursor.SE_RESIZE_CURSOR:
								w += dx;
								h += dy;
								dragPoint = evt.getPoint();
								break;
						}

						final int maxWidth = getTopPane().getWidth() + OUT_BORDER_WIDTH;
						final int maxHeight = getTopPane().getHeight() + OUT_BORDER_WIDTH;
						
						w = Math.max(w, MIN_SIZE);
						h = Math.max(h, MIN_SIZE);
						w = Math.min(w, maxWidth);
						h = Math.min(h, maxHeight);
						
						Point p = getMasterPane().getLocation();
						p = SwingUtilities.convertPoint(getMainPanel(), p, CytoscapeDesktop.this.getGlassPane());
						final int minX = p.x;
						final int minY = p.y;
						final int maxX = minX + getTopPane().getWidth();
						final int maxY = minY + getTopPane().getHeight();
						
						switch (cursor) {
							case Cursor.N_RESIZE_CURSOR:
							case Cursor.NE_RESIZE_CURSOR:
								y = Math.max(y, minY);
								y = Math.min(y, maxY - h);
								break;
	
							case Cursor.W_RESIZE_CURSOR:
							case Cursor.SW_RESIZE_CURSOR:
								x = Math.max(x, minX);
								x = Math.min(x, maxX - w);
								break;
	
							case Cursor.NW_RESIZE_CURSOR:
								x = Math.max(x, minX);
								x = Math.min(x, maxX - w);
								y = Math.max(y, minY);
								y = Math.min(y, maxY - h);
								break;
						}
						
						switch (cursor) {
							case Cursor.S_RESIZE_CURSOR:
								dragPoint = evt.getPoint();
								break;
	
							case Cursor.E_RESIZE_CURSOR:
								dragPoint = evt.getPoint();
								break;
	
							case Cursor.NE_RESIZE_CURSOR:
								dragPoint = new Point(evt.getX(), dragPoint.y);
								break;
	
							case Cursor.SW_RESIZE_CURSOR:
								dragPoint = new Point(dragPoint.x, evt.getY());
								break;
	
							case Cursor.SE_RESIZE_CURSOR:
								dragPoint = evt.getPoint();
								break;
						}
						
						setBounds(x, y, w, h);
						cytoPanel.setMaximized(false);
						
						if (getParent() != null)
							getParent().revalidate();
						
						if (cytoPanel != null)
							undockPreferredSizes.put(cytoPanel.getCytoPanelNameInternal(), new Dimension(w, h));
					}
				}
			});
		}
		
		private Rectangle getRectangle(int x, int y, int w, int h, int location) {
			CytoPanelNameInternal name = cytoPanel != null ? cytoPanel.getCytoPanelNameInternal() : null;
			
			switch (location) {
				case SwingConstants.NORTH:
					if (name == BOTTOM || name == SOUTH || name == SOUTH_WEST)
						return new Rectangle(x + lb, y, w - rb, BORDER_WIDTH);
					
					break;
				case SwingConstants.SOUTH:
					if (name == WEST || name == EAST)
						return new Rectangle(x + lb, y + h - bb, w - BORDER_WIDTH, BORDER_WIDTH);
					
					break;
				case SwingConstants.WEST:
					if (name == EAST || name == SOUTH)
						return new Rectangle(x, y + tb, BORDER_WIDTH, h - BORDER_WIDTH);
					
					break;
				case SwingConstants.EAST:
					if (name == WEST || name == SOUTH_WEST || name == BOTTOM)
						return new Rectangle(x + w - rb, y + tb, BORDER_WIDTH, h - BORDER_WIDTH);
					
					break;
				case SwingConstants.NORTH_WEST:
					if (name == SOUTH)
						return new Rectangle(x, y, BORDER_WIDTH, BORDER_WIDTH);
					
					break;
				case SwingConstants.NORTH_EAST:
					if (name == SOUTH_WEST || name == BOTTOM)
						return new Rectangle(x + w - rb, y, BORDER_WIDTH, BORDER_WIDTH);
					
					break;
				case SwingConstants.SOUTH_WEST:
					if (name == EAST)
						return new Rectangle(x, y + h - bb, BORDER_WIDTH, BORDER_WIDTH);
					
					break;
				case SwingConstants.SOUTH_EAST:
					if (name == WEST)
						return new Rectangle(x + w - rb, y + h - bb, BORDER_WIDTH, BORDER_WIDTH);
			}
			
			return null;
		}
		
		private int getCursor(MouseEvent evt) {
			Component c = evt.getComponent();
			int w = c.getWidth();
			int h = c.getHeight();

			for (int i = 0; i < locations.length; i++) {
				Rectangle rect = getRectangle(0, 0, w, h, locations[i]);

				if (rect != null && rect.contains(evt.getPoint()))
					return cursors[i];
			}

			return Cursor.DEFAULT_CURSOR;
		}
	}
	
	private class GlassPaneMouseListener implements AWTEventListener {
		
		@Override
		public void eventDispatched(AWTEvent ae) {
			if (popup == null || !popup.isShowing())
				return;
			if (ae instanceof MouseEvent == false || ae.getID() != MouseEvent.MOUSE_PRESSED)
				return;
			
			// Over a Window other than the CytoscapeDesktop?
			KeyboardFocusManager keyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
			Window window = keyboardFocusManager.getActiveWindow();
			
			if (window instanceof CytoscapeDesktop == false)
				return;
			
			// Over a JPopupMenu?
			MenuSelectionManager msm = MenuSelectionManager.defaultManager();
		    MenuElement[] path = msm.getSelectedPath();
		    
		    for (MenuElement me : path) {
		        if (me instanceof JPopupMenu)
		            return;
		    }
			
			// Over the cytopanel popup?
		    Point mouseLoc = MouseInfo.getPointerInfo().getLocation();
		    
			if (mouseLoc.x >= popup.getLocationOnScreen().x
					&& mouseLoc.x <= popup.getLocationOnScreen().x + popup.getWidth()
					&& mouseLoc.y >= popup.getLocationOnScreen().y
					&& mouseLoc.y <= popup.getLocationOnScreen().y + popup.getHeight())
				return; // Mouse pressed on the popup, nothing to do here

			// Over a sidebar button?
			boolean overButton = false;
			
			PANELS_LOOP:
			for (CytoPanelImpl cp : getAllCytoPanels()) {
				if (cp.getStateInternal() != MINIMIZE && cp.getStateInternal() != UNDOCK)
					continue;
				
				SideBar.TrimStack ts = getTrimStackOf(cp);
				
				if (!ts.isShowing())
					continue;
				
				for (SidebarToggleButton btn : ts.getAllButtons()) {
					if (!btn.isShowing())
						continue;
	
					if (mouseLoc.x >= btn.getLocationOnScreen().x
							&& mouseLoc.x <= btn.getLocationOnScreen().x + btn.getWidth()
							&& mouseLoc.y >= btn.getLocationOnScreen().y
							&& mouseLoc.y <= btn.getLocationOnScreen().y + btn.getHeight()) {
						overButton = true;
						break PANELS_LOOP;
					}
				}
			}

			// No need to dispose when over a sidebar button for a hidden panel,
			// since the button click will do it
			if (!overButton && popup != null) {
				if (popup.getCytoPanel() != null && popup.getCytoPanel().getStateInternal() != MINIMIZE) {
					popup.getCytoPanel().setStateInternal(MINIMIZE);
				} else {
					// This should not happen, but just in case we need to force-dispose the popup...
					disposeComponentPopup();
					AbstractButton btn = undockedButtonGroup.getSelectedButton();
					
					if (btn != null)
						btn.setSelected(false);
					
					isAdjusting = true;
					undockedButtonGroup.clearSelection();
					isAdjusting = false;
				}
			}
		}
	}
}
