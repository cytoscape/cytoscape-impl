package org.cytoscape.internal.view;

import static org.cytoscape.internal.view.util.ViewUtil.createUniqueKey;
import static org.cytoscape.internal.view.util.ViewUtil.getTitle;
import static org.cytoscape.internal.view.util.ViewUtil.isScreenMenuBar;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.internal.view.GridViewToggleModel.Mode;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.task.destroy.DestroyNetworkViewTaskFactory;
import org.cytoscape.task.write.ExportNetworkImageTaskFactory;
import org.cytoscape.task.write.ExportNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.swing.DialogTaskManager;

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

@SuppressWarnings("serial")
public class NetworkViewMainPanel extends JPanel {

	public static String NAME = "__NETWORK_VIEW_MAIN_PANEL__";
	private static final String VIEW_THRESHOLD = "viewThreshold";
	private static final int DEF_VIEW_THRESHOLD = 3000;
	
	private JPanel contentPane;
	private final CardLayout cardLayout;
	private final NetworkViewGrid networkViewGrid;
	private final GridViewToggleModel gridViewToggleModel;
	
	/** Attached View Containers */
	private final Map<CyNetworkView, NetworkViewContainer> allViewContainers;
	/** Attached View Containers */
	private final Map<String, NetworkViewContainer> viewCards;
	/** Detached View Frames */
	private final Map<String, NetworkViewFrame> viewFrames;
	private final Map<String, NetworkViewComparisonPanel> comparisonPanels;
	private final NullNetworkViewPanel nullViewPanel;
	
	private final MousePressedAWTEventListener mousePressedAWTEventListener;
	
	private final CytoscapeMenus cyMenus;
	private final Comparator<CyNetworkView> viewComparator;
	private final CyServiceRegistrar serviceRegistrar;

	public NetworkViewMainPanel(
			GridViewToggleModel gridViewToggleModel,
			CytoscapeMenus cyMenus,
			Comparator<CyNetworkView> viewComparator,
			CyServiceRegistrar serviceRegistrar
	) {
		this.gridViewToggleModel = gridViewToggleModel;
		this.cyMenus = cyMenus;
		this.viewComparator = viewComparator;
		this.serviceRegistrar = serviceRegistrar;
		
		allViewContainers = new HashMap<>();
		viewCards = new LinkedHashMap<>();
		viewFrames = new HashMap<>();
		comparisonPanels = new HashMap<>();
		
		mousePressedAWTEventListener = new MousePressedAWTEventListener();
		
		cardLayout = new CardLayout();
		networkViewGrid = createNetworkViewGrid();
		nullViewPanel = new NullNetworkViewPanel(gridViewToggleModel, serviceRegistrar);
		
		setName(NAME);
		
		init();
	}

	public RenderingEngine<CyNetwork> addNetworkView(
			CyNetworkView view,
			RenderingEngineFactory<CyNetwork> engineFactory,
			RenderingEngineFactory<CyNetwork> thumbnailFactory
	) {
		if (isRendered(view))
			return null;
		
		var vc = new NetworkViewContainer(view, view.equals(getCurrentNetworkView()), engineFactory, thumbnailFactory,
				gridViewToggleModel, serviceRegistrar);
		
		vc.getDetachViewButton().addActionListener(evt -> {
			detachNetworkView(view);
		});
		vc.getReattachViewButton().addActionListener(evt -> {
			reattachNetworkView(view);
		});
		vc.getExportButton().addActionListener(evt -> {
			showExportPopup(vc.getExportButton(), view);
		});
		vc.getViewTitleTextField().addActionListener(evt -> {
			changeCurrentViewTitle(vc);
			vc.requestFocusInWindow();
		});
		vc.getViewTitleTextField().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					cancelViewTitleChange(vc);
			}
		});
		vc.getViewTitleTextField().addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				changeCurrentViewTitle(vc);
				vc.requestFocusInWindow();
				Toolkit.getDefaultToolkit().addAWTEventListener(mousePressedAWTEventListener, MouseEvent.MOUSE_EVENT_MASK);
			}
			@Override
			public void focusGained(FocusEvent e) {
				Toolkit.getDefaultToolkit().removeAWTEventListener(mousePressedAWTEventListener);
			}
		});
		
		allViewContainers.put(view, vc);
		viewCards.put(vc.getName(), vc);
		networkViewGrid.addItem(vc.getRenderingEngine(), vc.getThumbnailEngineFactory());
		getContentPane().add(vc, vc.getName());
		
		if (isGridMode())
			updateGrid();
		
		// Hide the Birds-Eye-View for performance reasons if the network is large.
		int numElements = view.getModel().getNodeCount() + view.getModel().getEdgeCount();
		if(numElements > getViewThreshold()) {
			vc.hideBirdsEyePanel();
		}
		
		return vc.getRenderingEngine();
	}
	
	private int getViewThreshold() {
		var props = (Properties) serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)")
				.getProperties();
		var vts = props.getProperty(VIEW_THRESHOLD);
		int threshold;
		
		try {
			threshold = Integer.parseInt(vts);
		} catch (Exception e) {
			threshold = DEF_VIEW_THRESHOLD;
		}

		return threshold;
	}
	
	public boolean isRendered(CyNetworkView view) {
		return allViewContainers.containsKey(view);
	}

	public void remove(CyNetworkView view) {
		if (view == null)
			return;
		
		allViewContainers.remove(view);
		
		var components = getContentPane().getComponents();
		
		if (components != null) {
			for (var c : components) {
				if (c instanceof NetworkViewContainer) {
					var vc = (NetworkViewContainer) c;
					
					if (vc.getNetworkView().equals(view)) {
						networkViewGrid.removeItems(Collections.singleton(vc.getRenderingEngine()));
						removeCard(vc);
						vc.dispose();
					}
				} else if (c instanceof NetworkViewComparisonPanel) {
					var vc = ((NetworkViewComparisonPanel) c).getContainer(view);
					
					if (vc != null) {
						networkViewGrid.removeItems(Collections.singleton(vc.getRenderingEngine()));
						((NetworkViewComparisonPanel) c).removeView(view);
						
						// Show regular view container if only one remains
						if (((NetworkViewComparisonPanel) c).viewCount() < 2)
							endComparison(((NetworkViewComparisonPanel) c));
					}
				} else if (c instanceof NullNetworkViewPanel) {
					if (view.equals(((NullNetworkViewPanel) c).getNetworkView()));
						nullViewPanel.update((CyNetwork) null);
				}
			}
		}
		
		var frame = viewFrames.remove(createUniqueKey(view));
		
		if (frame != null) {
			networkViewGrid.removeItems(Collections.singleton(frame.getRenderingEngine()));
			
			frame.getRootPane().getLayeredPane().removeAll();
			frame.getRootPane().getContentPane().removeAll();
			frame.dispose();
			
			for (var l : frame.getComponentListeners())
				frame.removeComponentListener(l);
			
			for (var l : frame.getWindowListeners())
				frame.removeWindowListener(l);
		}
		
		if (isGridMode())
			updateGrid();
	}
	
	public boolean hasViews(CyNetwork network) {
		for (var view : allViewContainers.keySet()) {
			if (view.getModel().equals(network))
				return true;
		}
		
		return false;
	}
	
	public void setSelectedNetworkViews(Collection<CyNetworkView> networkViews) {
		networkViewGrid.setSelectedNetworkViews(networkViews);
	}
	
	public List<CyNetworkView> getSelectedNetworkViews() {
		return networkViewGrid.getSelectedNetworkViews();
	}

	public CyNetworkView getCurrentNetworkView() {
		return networkViewGrid.getCurrentNetworkView();
	}
	
	public void setCurrentNetworkView(CyNetworkView view) {
		boolean currentViewChanged = networkViewGrid.setCurrentNetworkView(view);
		
		if (currentViewChanged) {
			if (view != null) {
				if (isGridMode()) {
					if (isGridVisible())
						updateGrid();
					else
						showGrid(true);
				} else {
					showViewContainer(createUniqueKey(view));
				}
				
				if (isGridVisible())
					networkViewGrid.scrollToCurrentItem();
				
				var vc = getNetworkViewContainer(view);
				
				if (vc != null) {
					var window = SwingUtilities.getWindowAncestor(vc);
					
					if (window != null && !window.isActive())
						window.toFront();
				}
			} else {
				showNullView(null);
			}
		}
	}
	
	public void showNullView(CyNetwork network) {
		if (isGridMode()) {
			if (isGridVisible())
				updateGrid();
			else
				showGrid(true);
		} else {
			showNullViewContainer(network);
		}
	}
	
	public void showGrid() {
		showGrid(false);
	}
	
	public void showGrid(boolean scrollToCurrentItem) {
		if (!isGridMode()) {
			gridViewToggleModel.setMode(Mode.GRID);
			return;
		}
		
		if (!isGridVisible()) {
			cardLayout.show(getContentPane(), networkViewGrid.getName());
			updateGrid();
		}
		
		if (scrollToCurrentItem)
			networkViewGrid.scrollToCurrentItem();
	}

	public void updateGrid() {
		networkViewGrid.update(networkViewGrid.getThumbnailSlider().getValue()); // TODO remove it when already updating after view changes
	}
	
	public void detachNetworkViews(Collection<CyNetworkView> views) {
		var currentView = getCurrentNetworkView(); // Get the current view first
		
		// Then detach the views
		for (var v : views)
			detachNetworkView(v);
		
		// Set the original current view by bringing its frame to front, if it is detached
		var frame = getNetworkViewFrame(currentView);

		if (frame != null)
			frame.toFront();
	}
	
	public NetworkViewFrame detachNetworkView(CyNetworkView view) {
		if (view == null)
			return null;
		
		var gc = serviceRegistrar.getService(CySwingApplication.class).getJFrame().getGraphicsConfiguration();
		
		return detachNetworkView(view, gc);
	}
	
	public NetworkViewFrame detachNetworkView(CyNetworkView view, GraphicsConfiguration gc) {
		if (view == null)
			return null;
		
		var vc = getNetworkViewCard(view);
		
		if (vc == null)
			return null;
		
		// Show grid first to prevent changing the current view
		getNetworkViewGrid().setDetached(vc.getNetworkView(), true);
		
		// Remove the container from the card layout
		removeCard(vc);
		
		if (!isGridMode())
			showNullViewContainer(view);
		
		if (viewFrames.get(vc.getName()) != null)
			return viewFrames.get(vc.getName());
		
		var width = view.getVisualProperty(BasicVisualLexicon.NETWORK_WIDTH);
		var height = view.getVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT);
		
		if (width == null || width <= 0)
			view.setVisualProperty(BasicVisualLexicon.NETWORK_WIDTH, 600.0);
		if (height == null || height <= 0)
			view.setVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT, 500.0);
		
		// Create and show the frame
		var frame = new NetworkViewFrame(vc, gc, cyMenus.createViewFrameToolBar(), serviceRegistrar);
		vc.setDetached(true);
		vc.setComparing(false);
		
		viewFrames.put(vc.getName(), frame);
		
		if (!isScreenMenuBar())
			frame.setJMenuBar(cyMenus.createDummyMenuBar());
		
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {
				// So Tunable dialogs open in the same monitor of the current frame
				serviceRegistrar.getService(DialogTaskManager.class).setExecutionContext(frame);
				
				// This is necessary because the same menu bar is used by other frames, including CytoscapeDesktop
				var menuBar = cyMenus.getJMenuBar();
				var window = SwingUtilities.getWindowAncestor(menuBar);

				if (!frame.equals(window)) {
					if (window instanceof JFrame && !isScreenMenuBar()) {
						// Do this first, or the user could see the menu disappearing from the out-of-focus windows
						var dummyMenuBar = cyMenus.createDummyMenuBar();
						((JFrame) window).setJMenuBar(dummyMenuBar);
						dummyMenuBar.updateUI();
						window.repaint();
					}

					frame.setJMenuBar(menuBar);
				}
				
				if (isScreenMenuBar() && menuBar.equals(frame.getJMenuBar()))
					cyMenus.setMenuBarVisible(true);
				
				// Don't forget to update the UI, or it can cause some issues,
				// such as the menus being duplicated on Mac/Aqua when activating detached frames
				// (see http://code.cytoscape.org/redmine/issues/3582)
				menuBar.updateUI();
			}
			@Override
			public void windowDeactivated(WindowEvent e) {
				// Workaround that removes the menus from detached views frames on Mac/Aqua
				// to prevent users from selecting them when a modal dialog is open from the detached view.
				// The problem is that the menus are not automatically disabled on Mac/Aqua when that happens,
				// as it should, though it works fine when the modal dialog is open from the main Cytoscape frame.
				if (isScreenMenuBar() && cyMenus.getJMenuBar().equals(frame.getJMenuBar()))
					cyMenus.setMenuBarVisible(false);
			}
			@Override
			public void windowClosed(WindowEvent e) {
				reattachNetworkView(view);
			}
			@Override
			public void windowOpened(WindowEvent e) {
				// Add another window listener so subsequent Window Activated events trigger
				// a current view change action.
				// It has to be done this way (after the Open event), otherwise it can cause infinite loops
				// when detaching more than one view at the same time.
				frame.addWindowListener(new WindowAdapter() {
					@Override
					public void windowActivated(WindowEvent e) {
						setSelectedNetworkViews(Collections.singletonList(frame.getNetworkView()));
						setCurrentNetworkView(frame.getNetworkView());
					}
				});
			}
		});
		
		int w = view.getVisualProperty(BasicVisualLexicon.NETWORK_WIDTH).intValue();
		int h = view.getVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT).intValue();
		boolean resizable = !view.isValueLocked(BasicVisualLexicon.NETWORK_WIDTH) &&
				!view.isValueLocked(BasicVisualLexicon.NETWORK_HEIGHT);
		
		if (w > 0 && h > 0)
			frame.getNetworkViewContainer().getVisualizationContainer().setPreferredSize(new Dimension(w, h));
		
		frame.pack();
		frame.setResizable(resizable);
		frame.setVisible(true);
		
		return frame;
	}

	public void reattachNetworkView(CyNetworkView view) {
		var frame = getNetworkViewFrame(view);
		
		if (frame != null) {
			var vc = frame.getNetworkViewContainer();
			
			frame.setJMenuBar(null);
			frame.dispose();
			viewFrames.remove(vc.getName());
			
			vc.setDetached(false);
			vc.setComparing(false);
			getContentPane().add(vc, vc.getName());
			viewCards.put(vc.getName(), vc);
			getNetworkViewGrid().setDetached(view, false);
			
			if (!isGridMode() && view.equals(getCurrentNetworkView()))
				showViewContainer(vc.getName());
		}
	}
	
	public void updateThumbnailPanel(CyNetworkView view, boolean redraw) {
		// If the Grid is not visible, just flag this view as dirty.
		if (isGridVisible()) {
			var tp = networkViewGrid.getItem(view);
			
			if (tp != null)
				tp.update(redraw);
		}
	}
	
	public void update(CyNetworkView view, boolean updateSelectionInfo, boolean updateHiddenInfo) {
		var frame = getNetworkViewFrame(view);
		
		if (frame != null) {
			// Frame Title
			frame.setTitle(getTitle(view));
			
			// Frame Size
			int w = view.getVisualProperty(BasicVisualLexicon.NETWORK_WIDTH).intValue();
			int h = view.getVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT).intValue();
			boolean resizable = !view.isValueLocked(BasicVisualLexicon.NETWORK_WIDTH) &&
					!view.isValueLocked(BasicVisualLexicon.NETWORK_HEIGHT);
			
			if (w > 0 && h > 0) {
				if (w != frame.getContentPane().getWidth() && 
					h != frame.getContentPane().getHeight()) {
					frame.getContentPane().setPreferredSize(new Dimension(w, h));
					frame.pack();
				}
			}
			
			frame.setResizable(resizable);
			frame.update();
			frame.invalidate();
		} else if (!isGridVisible()) {
			var vc = getNetworkViewCard(view);
			
			if (vc != null && vc.equals(getCurrentViewContainer()))
				vc.update(updateSelectionInfo, updateHiddenInfo);
			
			var cp = getComparisonPanel(view);
			
			if (cp != null)
				cp.update();
		}
		
		updateThumbnailPanel(view, false);
	}
	
	public void updateSelectionInfo(CyNetworkView view) {
		var vc = getNetworkViewContainer(view);
		
		if (vc != null)
			vc.updateSelectionInfo();
	}
	
	public boolean isEmpty() {
		return allViewContainers.isEmpty();
	}
	
	public NetworkViewGrid getNetworkViewGrid() {
		return networkViewGrid;
	}
	
	public Set<NetworkViewFrame> getAllNetworkViewFrames() {
		return new HashSet<>(viewFrames.values());
	}
	
	public Set<NetworkViewContainer> getAllNetworkViewContainers() {
		return new HashSet<>(allViewContainers.values());
	}
	
	public void showNullViewContainer(CyNetwork net) {
		nullViewPanel.update(net instanceof CySubNetwork ? (CySubNetwork) net : null);
		showNullViewContainer();
	}
	
	public void showNullViewContainer(CyNetworkView view) {
		nullViewPanel.update(view);
		showNullViewContainer();
	}
	
	public NetworkViewContainer showViewContainer(CyNetworkView view) {
		return view != null ? showViewContainer(createUniqueKey(view)) : null;
	}
	
	public void dispose() {
		networkViewGrid.dispose();
		removeAll();
	}
	
	private NetworkViewContainer showViewContainer(String key) {
		NetworkViewContainer viewContainer = null;
		
		if (key != null) {
			viewContainer = viewCards.get(key);
			
			if (isGridMode())
				gridViewToggleModel.setMode(Mode.VIEW);
			
			if (viewContainer != null) {
				cardLayout.show(getContentPane(), key);
				viewContainer.update(true, true);
			} else {
				NetworkViewComparisonPanel foundCompPanel = null;
				
				for (var cp : comparisonPanels.values()) {
					if (key.equals(cp.getName())) {
						foundCompPanel = cp;
					} else {
						for (var vc : cp.getAllContainers()) {
							if (key.equals(vc.getName())) {
								foundCompPanel = cp;
								break;
							}
						}
					}
				}
				
				if (foundCompPanel != null) {
					cardLayout.show(getContentPane(), foundCompPanel.getName());
					foundCompPanel.update();
					viewContainer = foundCompPanel.getCurrentContainer();
				} else {
					var frame = getNetworkViewFrame(key);
					
					if (frame != null && !isGridMode())
						showNullViewContainer(frame.getNetworkView());
				}
			}
		} else {
			showGrid();
		}
		
		return viewContainer;
	}
	
	private void showNullViewContainer() {
		cardLayout.show(getContentPane(), nullViewPanel.getName());
	}

	protected void showViewFrame(NetworkViewFrame frame) {
		frame.setVisible(true);
		frame.toFront();
	}
	
	protected void showComparisonPanel(Set<CyNetworkView> views) {
		var currentView = getCurrentNetworkView();
		var key = NetworkViewComparisonPanel.createUniqueKey(views);
		var cp = comparisonPanels.get(key);
		
		if (cp == null) {
			// End previous comparison panels that have one of the new selected views first
			for (var v : views) {
				cp = getComparisonPanel(v);
				
				if (cp != null)
					endComparison(cp);
			}
			
			var containersToCompare = new LinkedHashSet<NetworkViewContainer>();
			
			// Then check if any of the views are detached
			for (var v : views) {
				var frame = getNetworkViewFrame(v);
			
				if (frame != null)
					reattachNetworkView(v);
				
				var vc = getNetworkViewCard(v);
				
				if (vc != null) {
					removeCard(vc);
					containersToCompare.add(vc);
				}
			}
			
			// Now we can create the comparison panel
			cp = new NetworkViewComparisonPanel(gridViewToggleModel, containersToCompare, currentView, serviceRegistrar);
			
			cp.getDetachComparedViewsButton().addActionListener(evt -> {
				var currentCard = getCurrentCard();
				
				if (currentCard instanceof NetworkViewComparisonPanel) {
					var ncp = (NetworkViewComparisonPanel) currentCard;
					var viewSet = ncp.getAllNetworkViews();
					
					// End comparison first
					endComparison(ncp);
					// Then detach the views
					detachNetworkViews(viewSet);
				}
			});
			
			cp.addPropertyChangeListener("currentNetworkView", evt -> {
				var newCurrentView = (CyNetworkView) evt.getNewValue();
				setCurrentNetworkView(newCurrentView);
			});
			
			getContentPane().add(cp, cp.getName());
			comparisonPanels.put(cp.getName(), cp);
		}
		
		if (cp != null) {
			gridViewToggleModel.setMode(Mode.VIEW);
			showViewContainer(cp.getName());
		}
	}
	
	protected void endComparison(NetworkViewComparisonPanel cp) {
		if (cp != null) {
			removeCard(cp);
			cp.dispose(); // Don't forget to call this method!
			
			for (var vc : cp.getAllContainers()) {
				getContentPane().add(vc, vc.getName());
				viewCards.put(vc.getName(), vc);
			}
		}
	}
	
	private NetworkViewComparisonPanel getComparisonPanel(final CyNetworkView view) {
		for (NetworkViewComparisonPanel cp : comparisonPanels.values()) {
			if (cp.contains(view))
				return cp;
		}
		
		return null;
	}
	
	private void removeCard(final JComponent comp) {
		if (comp == null)
			return;
		
		if (comp instanceof NetworkViewContainer)
			viewCards.remove(((NetworkViewContainer) comp).getName());
		else if (comp instanceof NetworkViewComparisonPanel)
			comparisonPanels.remove(((NetworkViewComparisonPanel) comp).getName());
		
		cardLayout.removeLayoutComponent(comp);
		getContentPane().remove(comp);
	}
	
	protected boolean isGridMode() {
		return gridViewToggleModel.getMode() == Mode.GRID;
	}
	
	protected boolean isGridVisible() {
		return getCurrentCard() == networkViewGrid;
	}
	
	/**
	 * @return The current attached View container
	 */
	protected NetworkViewContainer getCurrentViewContainer() {
		var c = getCurrentCard();

		return c instanceof NetworkViewContainer ? (NetworkViewContainer) c : null;
	}
	
	protected Component getCurrentCard() {
		Component current = null;
		
		for (var comp : getContentPane().getComponents()) {
			if (comp.isVisible())
				current = comp;
		}
		
		return current;
	}
	
	private NetworkViewGrid createNetworkViewGrid() {
		var nvg = new NetworkViewGrid(gridViewToggleModel, viewComparator, serviceRegistrar);
		
		nvg.getDetachSelectedViewsButton().addActionListener(evt -> {
			var selectedItems = networkViewGrid.getSelectedItems();

			if (selectedItems != null) {
				// Get the current view first
				var currentView = getCurrentNetworkView();

				// Detach the views
				for (var tp : selectedItems) {
					if (getNetworkViewCard(tp.getNetworkView()) != null)
						detachNetworkView(tp.getNetworkView());
				}

				// Set the original current view by bringing its frame to front, if it is detached
				var frame = getNetworkViewFrame(currentView);

				if (frame != null)
					frame.toFront();
			}
		});
		
		nvg.getReattachAllViewsButton().addActionListener(evt -> {
			var allFrames = new ArrayList<>(viewFrames.values());

			for (var f : allFrames)
				reattachNetworkView(f.getNetworkView());
		});
		
		nvg.getDestroySelectedViewsButton().addActionListener(evt -> {
			var selectedViews = getSelectedNetworkViews();
			
			if (selectedViews != null && !selectedViews.isEmpty()) {
				var taskMgr = serviceRegistrar.getService(DialogTaskManager.class);
				var taskFactory = serviceRegistrar.getService(DestroyNetworkViewTaskFactory.class);
				taskMgr.execute(taskFactory.createTaskIterator(selectedViews));
			}
		});
		
		return nvg;
	}
	
	private void init() {
		setLayout(new BorderLayout());
		add(getContentPane(), BorderLayout.CENTER);
		
		// Add Listeners
		nullViewPanel.getCreateViewButton().addActionListener(evt -> {
			if (nullViewPanel.getNetwork() instanceof CySubNetwork) {
				var factory = serviceRegistrar.getService(CreateNetworkViewTaskFactory.class);
				var taskManager = serviceRegistrar.getService(DialogTaskManager.class);
				taskManager.execute(factory.createTaskIterator(Collections.singleton(nullViewPanel.getNetwork())));
			}
		});
		nullViewPanel.getInfoIconLabel().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!e.isPopupTrigger() && nullViewPanel.getNetworkView() != null) {
					var frame = getNetworkViewFrame(nullViewPanel.getNetworkView());
					
					if (frame != null)
						showViewFrame(frame);
				}
			}
		});
		nullViewPanel.getReattachViewButton().addActionListener(evt -> {
			if (nullViewPanel.getNetworkView() != null)
				reattachNetworkView(nullViewPanel.getNetworkView());
		});
		
		networkViewGrid.addPropertyChangeListener("selectedNetworkViews", evt -> {
			// Just fire the same event
			firePropertyChange("selectedNetworkViews", evt.getOldValue(), evt.getNewValue());
		});
		networkViewGrid.addPropertyChangeListener("currentNetworkView", evt -> {
			var curView = (CyNetworkView) evt.getNewValue();
			
			for (NetworkViewContainer vc : getAllNetworkViewContainers())
				vc.setCurrent(vc.getNetworkView().equals(curView));
		});
		
		Toolkit.getDefaultToolkit().addAWTEventListener(mousePressedAWTEventListener, MouseEvent.MOUSE_EVENT_MASK);
		
		// Update
		updateGrid();
	}
	
	private JPanel getContentPane() {
		if (contentPane == null) {
			contentPane = new JPanel();
			contentPane.setLayout(cardLayout);
			
			contentPane.add(nullViewPanel, nullViewPanel.getName());
			contentPane.add(networkViewGrid, networkViewGrid.getName());
		}
		
		return contentPane;
	}

	protected NetworkViewContainer getNetworkViewContainer(CyNetworkView view) {
		return view != null ? allViewContainers.get(view) : null;
	}
	
	protected NetworkViewContainer getNetworkViewCard(CyNetworkView view) {
		return view != null ? viewCards.get(createUniqueKey(view)) : null;
	}
	
	protected NetworkViewFrame getNetworkViewFrame(CyNetworkView view) {
		return view != null ? getNetworkViewFrame(createUniqueKey(view)) : null;
	}
	
	protected NetworkViewFrame getNetworkViewFrame(String key) {
		return key != null ? viewFrames.get(key) : null;
	}
	
	private void changeCurrentViewTitle(NetworkViewContainer vc) {
		var text = vc.getViewTitleTextField().getText();
		
		if (text != null) {
			text = text.trim();
			
			// TODO Make sure it's unique
			if (!text.isEmpty()) {
				vc.getViewTitleLabel().setText(text);
				
				// TODO This will fire a ViewChangedEvent - Just let the NetworkViewManager ask this panel to update itself instead?
				var view = vc.getNetworkView();
				view.setVisualProperty(BasicVisualLexicon.NETWORK_TITLE, text);
				
				updateThumbnailPanel(view, false);
			}
		}
		
		vc.getViewTitleTextField().setText(null);
		vc.getViewTitleTextField().setVisible(false);
		vc.getViewTitleLabel().setVisible(true);
		vc.getToolBar().updateUI();
	}
	
	private void cancelViewTitleChange(NetworkViewContainer vc) {
		vc.getViewTitleTextField().setText(null);
		vc.getViewTitleTextField().setVisible(false);
		vc.getViewTitleLabel().setVisible(true);
	}
	
	private void showExportPopup(JComponent source, CyNetworkView view) {
		var taskMgr = serviceRegistrar.getService(DialogTaskManager.class);

		var popupMenu = new JPopupMenu();
		{
			var mi = new JMenuItem("Export as Network...");
			mi.addActionListener(evt -> {
				var factory = serviceRegistrar.getService(ExportNetworkViewTaskFactory.class);
				taskMgr.execute(factory.createTaskIterator(view));
			});
			popupMenu.add(mi);
		}
		{
			var mi = new JMenuItem("Export as Image...");
			mi.addActionListener(evt -> {
				var factory = serviceRegistrar.getService(ExportNetworkImageTaskFactory.class);
				taskMgr.execute(factory.createTaskIterator(view));
			});
			popupMenu.add(mi);
		}

		popupMenu.show(source, 0, source.getHeight());
	}
	
	/**
	 * This listener is used to pass mouse events through the glass pane to the network view.
	 */
	private class MousePressedAWTEventListener implements AWTEventListener {
		
        @Override
        public void eventDispatched(AWTEvent event) {
            if (event.getID() == MouseEvent.MOUSE_PRESSED && event instanceof MouseEvent) {
				var me = (MouseEvent) event;
				var window = SwingUtilities.windowForComponent(me.getComponent());
				
				if (!(window instanceof NetworkViewFrame || window instanceof CySwingApplication))
					return;
				
				if (window instanceof CySwingApplication) {
					// Get all CytoPanels and check if they are undocked.
					// If so, and the mouse event is inside the CytoPanel's bounds, ignore the event.
					// If we don't do this here and just call requestFocusInWindow() on the view component,
					// that would steal the focus from widgets on the CytoPanel, such as when editing a Node Table cell,
					// for instance.
					// See bug: https://cytoscape.atlassian.net/browse/CYTOSCAPE-12661
					for (var cpName : CytoPanelName.values()) {
						var cp = ((CySwingApplication) window).getCytoPanel(cpName);
						
						if (cp instanceof CytoPanelImpl == false || cp.getThisComponent() == null)
							continue;
						
						// We only check UNDOCKED CytoPanels, because thet's when they can be rendered
						// on the glass pane over the network view area
						if (((CytoPanelImpl) cp).getStateInternal() == CytoPanelStateInternal.UNDOCK) {
							var pt = new Point(SwingUtilities.convertPoint(me.getComponent(), me.getPoint(), cp.getThisComponent()));
							
							if (cp.getThisComponent().contains(pt))
								return; // Ignore the mouse event because it was meant for an undocked CytoPanel
						}
					}
				}
				
				// Detect if a new view container received the mouse pressed event.
				// If so, it must request focus.
                var targets = new HashSet<Component>();
                
                // Find the view container to be verified
                if (window instanceof NetworkViewFrame) {
                	targets.add(((NetworkViewFrame) window).getContainerRootPane().getContentPane());
                } else {
                	var currentCard = getCurrentCard();
                	
                	if (currentCard instanceof NetworkViewContainer) {
                		var vc = (NetworkViewContainer) currentCard;
                		targets.add(vc.getContentPane());
                	} else if (currentCard instanceof NetworkViewComparisonPanel) {
                		// Get the view component which is not in focus
                		var cp = (NetworkViewComparisonPanel) currentCard;
                		var currentContainer = cp.getCurrentContainer();
                		
                		for (var vc : cp.getAllContainers()) {
                			if (vc != currentContainer)
                				targets.add(vc.getContentPane());
                		}
                	}
                }
                
                for (var c : targets) {
                	me = SwingUtilities.convertMouseEvent(me.getComponent(), me, c);
                	
                	// Received the mouse event and got here?
                	// So it should get focus now, otherwise keyboard events may not work on the view.
                	if (c.getBounds().contains(me.getPoint()))
                		c.requestFocusInWindow();
                }
            }
        }
    }
}
