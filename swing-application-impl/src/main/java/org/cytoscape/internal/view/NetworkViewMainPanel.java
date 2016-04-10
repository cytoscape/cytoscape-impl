package org.cytoscape.internal.view;

import static org.cytoscape.internal.util.ViewUtil.createUniqueKey;
import static org.cytoscape.internal.util.ViewUtil.getTitle;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
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
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.internal.view.GridViewToggleModel.Mode;
import org.cytoscape.internal.view.NetworkViewGrid.ThumbnailPanel;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.create.CreateNetworkViewTaskFactory;
import org.cytoscape.task.destroy.DestroyNetworkViewTaskFactory;
import org.cytoscape.util.swing.LookAndFeelUtil;
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

@SuppressWarnings("serial")
public class NetworkViewMainPanel extends JPanel {

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
	
	private final Set<CyNetworkView> dirtyThumbnails;
	
	private NetworkViewFrame currentViewFrame;
	
	private final MousePressedAWTEventListener mousePressedAWTEventListener;
	
	private final CytoscapeMenus cyMenus;
	private final Comparator<CyNetworkView> viewComparator;
	private final CyServiceRegistrar serviceRegistrar;

	public NetworkViewMainPanel(
			final GridViewToggleModel gridViewToggleModel,
			final CytoscapeMenus cyMenus,
			final Comparator<CyNetworkView> viewComparator,
			final CyServiceRegistrar serviceRegistrar
	) {
		this.gridViewToggleModel = gridViewToggleModel;
		this.cyMenus = cyMenus;
		this.viewComparator = viewComparator;
		this.serviceRegistrar = serviceRegistrar;
		
		allViewContainers = new HashMap<>();
		viewCards = new LinkedHashMap<>();
		viewFrames = new HashMap<>();
		comparisonPanels = new HashMap<>();
		dirtyThumbnails = new HashSet<>();
		
		mousePressedAWTEventListener = new MousePressedAWTEventListener();
		
		cardLayout = new CardLayout();
		networkViewGrid = createNetworkViewGrid();
		nullViewPanel = new NullNetworkViewPanel(gridViewToggleModel, serviceRegistrar);
		
		init();
	}

	public RenderingEngine<CyNetwork> addNetworkView(final CyNetworkView view,
			final RenderingEngineFactory<CyNetwork> engineFactory, boolean showView) {
		if (isRendered(view))
			return null;
		
		final GraphicsConfiguration gc = currentViewFrame != null ? currentViewFrame.getGraphicsConfiguration() : null;
		
		final NetworkViewContainer vc = new NetworkViewContainer(view, view.equals(getCurrentNetworkView()),
				engineFactory, gridViewToggleModel, serviceRegistrar);
		
		vc.getDetachViewButton().addActionListener((ActionEvent e) -> {
			detachNetworkView(view);
		});
		vc.getReattachViewButton().addActionListener((ActionEvent e) -> {
			reattachNetworkView(view);
		});
		vc.getViewTitleTextField().addActionListener((ActionEvent e) -> {
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
				Toolkit.getDefaultToolkit().addAWTEventListener(mousePressedAWTEventListener,
						MouseEvent.MOUSE_EVENT_MASK);
			}
			@Override
			public void focusGained(FocusEvent e) {
				Toolkit.getDefaultToolkit().removeAWTEventListener(mousePressedAWTEventListener);
			}
		});
		
		allViewContainers.put(view, vc);
		viewCards.put(vc.getName(), vc);
		networkViewGrid.addItem(vc.getRenderingEngine());
		getContentPane().add(vc, vc.getName());
		
		setDirtyThumbnail(view);
		
		if (showView) {
			if (isGridMode())
				updateGrid();
			else
				showViewContainer(vc.getName());
			
			// If the latest focused view was in a detached frame,
			// detach the new one as well and put it in the same monitor
			if (gc != null)
				detachNetworkView(view, gc);
		}
		
		return vc.getRenderingEngine();
	}
	
	public boolean isRendered(final CyNetworkView view) {
		return allViewContainers.containsKey(view);
	}

	public void remove(final CyNetworkView view) {
		if (view == null)
			return;
		
		allViewContainers.remove(view);
		dirtyThumbnails.remove(view);
		
		final Component[] components = getContentPane().getComponents();
		
		if (components != null) {
			for (Component c : components) {
				if (c instanceof NetworkViewContainer) {
					final NetworkViewContainer vc = (NetworkViewContainer) c;
					
					if (vc.getNetworkView().equals(view)) {
						networkViewGrid.removeItems(Collections.singleton(vc.getRenderingEngine()));
						removeCard(vc);
						vc.dispose();
					}
				} else if (c instanceof NetworkViewComparisonPanel) {
					final NetworkViewContainer vc = ((NetworkViewComparisonPanel) c).getContainer(view);
					
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
		
		final NetworkViewFrame frame = viewFrames.remove(createUniqueKey(view));
		
		if (frame != null) {
			networkViewGrid.removeItems(Collections.singleton(frame.getRenderingEngine()));
			
			frame.getRootPane().getLayeredPane().removeAll();
			frame.getRootPane().getContentPane().removeAll();
			frame.dispose();
			
			for (ComponentListener l : frame.getComponentListeners())
				frame.removeComponentListener(l);
			
			for (WindowListener l : frame.getWindowListeners())
				frame.removeWindowListener(l);
		}
		
		if (isGridMode())
			updateGrid();
	}
	
	public boolean hasViews(final CyNetwork network) {
		for (CyNetworkView view : allViewContainers.keySet()) {
			if (view.getModel().equals(network))
				return true;
		}
		
		return false;
	}
	
	public void setSelectedNetworkViews(final Collection<CyNetworkView> networkViews) {
		networkViewGrid.setSelectedNetworkViews(networkViews);
	}
	
	public List<CyNetworkView> getSelectedNetworkViews() {
		return networkViewGrid.getSelectedNetworkViews();
	}

	public CyNetworkView getCurrentNetworkView() {
		return networkViewGrid.getCurrentNetworkView();
	}
	
	public void setCurrentNetworkView(final CyNetworkView view) {
		final boolean currentViewChanged = networkViewGrid.setCurrentNetworkView(view);
		
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
			}
		}
	}
	
	public void showNullView(final CyNetwork network) {
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
	
	public void showGrid(final boolean scrollToCurrentItem) {
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
		final HashSet<CyNetworkView> dirtySet = new HashSet<>(dirtyThumbnails);
		
		for (CyNetworkView view : dirtySet)
			updateThumbnail(view, true);
	}
	
	public void detachNetworkViews(final Collection<CyNetworkView> views) {
		final CyNetworkView currentView = getCurrentNetworkView(); // Get the current view first
		
		// Then detach the views
		for (CyNetworkView v : views)
			detachNetworkView(v);
		
		// Set the original current view by bringing its frame to front, if it is detached
		final NetworkViewFrame frame = getNetworkViewFrame(currentView);

		if (frame != null)
			frame.toFront();
	}
	
	public NetworkViewFrame detachNetworkView(final CyNetworkView view) {
		if (view == null)
			return null;
		
		final GraphicsConfiguration gc = serviceRegistrar.getService(CySwingApplication.class).getJFrame()
				.getGraphicsConfiguration();
		
		return detachNetworkView(view, gc);
	}
	
	public NetworkViewFrame detachNetworkView(final CyNetworkView view, final GraphicsConfiguration gc) {
		if (view == null)
			return null;
		
		final NetworkViewContainer vc = getNetworkViewCard(view);
		
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
		
		// Create and show the frame
		final NetworkViewFrame frame = new NetworkViewFrame(vc, gc, cyMenus.createViewFrameToolBar(), serviceRegistrar);
		vc.setDetached(true);
		vc.setComparing(false);
		
		viewFrames.put(vc.getName(), frame);
		
		if (!LookAndFeelUtil.isAquaLAF())
			frame.setJMenuBar(cyMenus.createDummyMenuBar());
		
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {
				// So Tunable dialogs open in the same monitor of the current frame
				serviceRegistrar.getService(DialogTaskManager.class).setExecutionContext(frame);
				
				currentViewFrame = frame;
				
				// This is necessary because the same menu bar is used by other frames, including CytoscapeDesktop
				final JMenuBar menuBar = cyMenus.getJMenuBar();
				final Window window = SwingUtilities.getWindowAncestor(menuBar);

				if (!frame.equals(window)) {
					if (window instanceof JFrame && !LookAndFeelUtil.isAquaLAF()) {
						// Do this first, or the user could see the menu disappearing from the out-of-focus windows
						final JMenuBar dummyMenuBar = cyMenus.createDummyMenuBar();
						((JFrame) window).setJMenuBar(dummyMenuBar);
						dummyMenuBar.updateUI();
						window.repaint();
					}

					frame.setJMenuBar(menuBar);
					menuBar.updateUI();
				}
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
		final boolean resizable = !view.isValueLocked(BasicVisualLexicon.NETWORK_WIDTH) &&
				!view.isValueLocked(BasicVisualLexicon.NETWORK_HEIGHT);
		
		if (w > 0 && h > 0)
			frame.getContentPane().setPreferredSize(new Dimension(w, h));
		
		frame.pack();
		frame.setResizable(resizable);
		frame.setVisible(true);
		
		return frame;
	}

	public void reattachNetworkView(final CyNetworkView view) {
		final NetworkViewFrame frame = getNetworkViewFrame(view);
		
		if (frame != null) {
			final NetworkViewContainer vc = frame.getNetworkViewContainer();
			
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
	
	public void updateThumbnail(final CyNetworkView view, boolean forceRedraw) {
		networkViewGrid.updateThumbnail(view, forceRedraw);
		dirtyThumbnails.remove(view);
	}
	
	public void updateThumbnailPanel(final CyNetworkView view, final boolean redraw) {
		// If the Grid is not visible, just flag this view as dirty.
		if (isGridVisible()) {
			final ThumbnailPanel tp = networkViewGrid.getItem(view);
			
			if (tp != null)
				tp.update(redraw);
			
			if (redraw)
				dirtyThumbnails.remove(view);
		} else {
			setDirtyThumbnail(view);
		}
	}
	
	public void setDirtyThumbnail(final CyNetworkView view) {
		dirtyThumbnails.add(view);
	}
	
	public void update(final CyNetworkView view) {
		final NetworkViewFrame frame = getNetworkViewFrame(view);
		
		if (frame != null) {
			// Frame Title
			frame.setTitle(getTitle(view));
			
			// Frame Size
			final int w = view.getVisualProperty(BasicVisualLexicon.NETWORK_WIDTH).intValue();
			final int h = view.getVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT).intValue();
			final boolean resizable = !view.isValueLocked(BasicVisualLexicon.NETWORK_WIDTH) &&
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
			final NetworkViewContainer vc = getNetworkViewCard(view);
			
			if (vc != null && vc.equals(getCurrentViewContainer()))
				vc.update();
			
			final NetworkViewComparisonPanel cp = getComparisonPanel(view);
			
			if (cp != null)
				cp.update();
		}
		
		updateThumbnailPanel(view, false);
	}
	
	public void updateSelectionInfo(final CyNetworkView view) {
		final NetworkViewContainer vc = getNetworkViewContainer(view);
		
		if (vc != null)
			vc.updateInfoPanel();
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
	
	public void showNullViewContainer(final CyNetwork net) {
		nullViewPanel.update(net instanceof CySubNetwork ? (CySubNetwork) net : null);
		showNullViewContainer();
	}
	
	public void showNullViewContainer(final CyNetworkView view) {
		nullViewPanel.update(view);
		showNullViewContainer();
	}
	
	public NetworkViewContainer showViewContainer(final CyNetworkView view) {
		return view != null ? showViewContainer(createUniqueKey(view)) : null;
	}
	
	private NetworkViewContainer showViewContainer(final String key) {
		NetworkViewContainer viewContainer = null;
		
		if (key != null) {
			viewContainer = viewCards.get(key);
			
			if (isGridMode())
				gridViewToggleModel.setMode(Mode.VIEW);
			
			if (viewContainer != null) {
				cardLayout.show(getContentPane(), key);
				viewContainer.update();
				currentViewFrame = null;
			} else {
				NetworkViewComparisonPanel foundCompPanel = null;
				
				for (NetworkViewComparisonPanel cp : comparisonPanels.values()) {
					if (key.equals(cp.getName())) {
						foundCompPanel = cp;
					} else {
						for (NetworkViewContainer vc : cp.getAllContainers()) {
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
					currentViewFrame = null;
					viewContainer = foundCompPanel.getCurrentContainer();
				} else {
					final NetworkViewFrame frame = getNetworkViewFrame(key);
					
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

	protected void showViewFrame(final NetworkViewFrame frame) {
		frame.setVisible(true);
		frame.toFront();
	}
	
	protected void showComparisonPanel(final Set<CyNetworkView> views) {
		final CyNetworkView currentView = getCurrentNetworkView();
		final String key = NetworkViewComparisonPanel.createUniqueKey(views);
		NetworkViewComparisonPanel cp = comparisonPanels.get(key);
		
		if (cp == null) {
			// End previous comparison panels that have one of the new selected views first
			for (CyNetworkView v : views) {
				cp = getComparisonPanel(v);
				
				if (cp != null)
					endComparison(cp);
			}
			
			final Set<NetworkViewContainer> containersToCompare = new LinkedHashSet<>();
			
			// Then check if any of the views are detached
			for (CyNetworkView v : views) {
				final NetworkViewFrame frame = getNetworkViewFrame(v);
			
				if (frame != null)
					reattachNetworkView(v);
				
				final NetworkViewContainer vc = getNetworkViewCard(v);
				
				if (vc != null) {
					removeCard(vc);
					containersToCompare.add(vc);
				}
			}
			
			// Now we can create the comparison panel
			cp = new NetworkViewComparisonPanel(gridViewToggleModel, containersToCompare, currentView, serviceRegistrar);
			
			cp.getDetachComparedViewsButton().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final Component currentCard = getCurrentCard();
					
					if (currentCard instanceof NetworkViewComparisonPanel) {
						final NetworkViewComparisonPanel cp = (NetworkViewComparisonPanel) currentCard;
						final Set<CyNetworkView>views = cp.getAllNetworkViews();
						
						// End comparison first
						endComparison(cp);
						// Then detach the views
						detachNetworkViews(views);
					}
				}
			});
			
			cp.addPropertyChangeListener("currentNetworkView", (PropertyChangeEvent evt) -> {
				final CyNetworkView newCurrentView = (CyNetworkView) evt.getNewValue();
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
	
	protected void endComparison(final NetworkViewComparisonPanel cp) {
		if (cp != null) {
			removeCard(cp);
			cp.dispose(); // Don't forget to call this method!
			
			for (NetworkViewContainer vc : cp.getAllContainers()) {
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
		final Component c = getCurrentCard();

		return c instanceof NetworkViewContainer ? (NetworkViewContainer) c : null;
	}
	
	protected Component getCurrentCard() {
		Component current = null;
		
		for (Component comp : getContentPane().getComponents()) {
			if (comp.isVisible())
				current = comp;
		}
		
		return current;
	}
	
	private NetworkViewGrid createNetworkViewGrid() {
		final NetworkViewGrid nvg = new NetworkViewGrid(gridViewToggleModel, viewComparator, serviceRegistrar);
		
		nvg.getDetachSelectedViewsButton().addActionListener((ActionEvent e) -> {
			final List<ThumbnailPanel> selectedItems = networkViewGrid.getSelectedItems();

			if (selectedItems != null) {
				// Get the current view first
				final CyNetworkView currentView = getCurrentNetworkView();

				// Detach the views
				for (ThumbnailPanel tp : selectedItems) {
					if (getNetworkViewCard(tp.getNetworkView()) != null)
						detachNetworkView(tp.getNetworkView());
				}

				// Set the original current view by bringing its frame to front, if it is detached
				final NetworkViewFrame frame = getNetworkViewFrame(currentView);

				if (frame != null)
					frame.toFront();
			}
		});
		
		nvg.getReattachAllViewsButton().addActionListener((ActionEvent e) -> {
			final Collection<NetworkViewFrame> allFrames = new ArrayList<>(viewFrames.values());

			for (NetworkViewFrame f : allFrames)
				reattachNetworkView(f.getNetworkView());
		});
		
		nvg.getDestroySelectedViewsButton().addActionListener((ActionEvent e) -> {
			final List<CyNetworkView> selectedViews = getSelectedNetworkViews();
			
			if (selectedViews != null && !selectedViews.isEmpty()) {
				final DialogTaskManager taskMgr = serviceRegistrar.getService(DialogTaskManager.class);
				final DestroyNetworkViewTaskFactory taskFactory =
						serviceRegistrar.getService(DestroyNetworkViewTaskFactory.class);
				taskMgr.execute(taskFactory.createTaskIterator(selectedViews));
			}
		});
		
		return nvg;
	}
	
	private void init() {
		setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, UIManager.getColor("Separator.foreground")));
		
		setLayout(new BorderLayout());
		add(getContentPane(), BorderLayout.CENTER);
		
		// Add Listeners
		nullViewPanel.getCreateViewButton().addActionListener((ActionEvent e) -> {
			if (nullViewPanel.getNetwork() instanceof CySubNetwork) {
				final CreateNetworkViewTaskFactory factory =
						serviceRegistrar.getService(CreateNetworkViewTaskFactory.class);
				final DialogTaskManager taskManager = serviceRegistrar.getService(DialogTaskManager.class);
				taskManager.execute(factory.createTaskIterator(Collections.singleton(nullViewPanel.getNetwork())));
			}
		});
		nullViewPanel.getInfoIconLabel().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!e.isPopupTrigger() && nullViewPanel.getNetworkView() != null) {
					final NetworkViewFrame frame = getNetworkViewFrame(nullViewPanel.getNetworkView());
					
					if (frame != null)
						showViewFrame(frame);
				}
			}
		});
		nullViewPanel.getReattachViewButton().addActionListener((ActionEvent e) -> {
			if (nullViewPanel.getNetworkView() != null)
				reattachNetworkView(nullViewPanel.getNetworkView());
		});
		
		networkViewGrid.addPropertyChangeListener("thumbnailPanels", (PropertyChangeEvent e) -> {
			networkViewGrid.updateToolBar();
		});
		networkViewGrid.addPropertyChangeListener("selectedNetworkViews", (PropertyChangeEvent e) -> {
			// Just fire the same event
			firePropertyChange("selectedNetworkViews", e.getOldValue(), e.getNewValue());
		});
		networkViewGrid.addPropertyChangeListener("selectedItems", (PropertyChangeEvent e) -> {
			networkViewGrid.updateToolBar();
		});
		networkViewGrid.addPropertyChangeListener("currentNetworkView", (PropertyChangeEvent e) -> {
			final CyNetworkView curView = (CyNetworkView) e.getNewValue();
			
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

	protected NetworkViewContainer getNetworkViewContainer(final CyNetworkView view) {
		return view != null ? allViewContainers.get(view) : null;
	}
	
	protected NetworkViewContainer getNetworkViewCard(final CyNetworkView view) {
		return view != null ? viewCards.get(createUniqueKey(view)) : null;
	}
	
	protected NetworkViewFrame getNetworkViewFrame(final CyNetworkView view) {
		return getNetworkViewFrame(createUniqueKey(view));
	}
	
	protected NetworkViewFrame getNetworkViewFrame(final String key) {
		return key != null ? viewFrames.get(key) : null;
	}
	
	private void changeCurrentViewTitle(final NetworkViewContainer vc) {
		String text = vc.getViewTitleTextField().getText();
		
		if (text != null) {
			text = text.trim();
			
			// TODO Make sure it's unique
			if (!text.isEmpty()) {
				vc.getViewTitleLabel().setText(text);
				
				// TODO This will fire a ViewChangedEvent - Just let the NetworkViewManager ask this panel to update itself instead?
				final CyNetworkView view = vc.getNetworkView();
				view.setVisualProperty(BasicVisualLexicon.NETWORK_TITLE, text);
				
				updateThumbnailPanel(view, false);
			}
		}
		
		vc.getViewTitleTextField().setText(null);
		vc.getViewTitleTextField().setVisible(false);
		vc.getViewTitleLabel().setVisible(true);
		vc.getToolBar().updateUI();
	}
	
	private void cancelViewTitleChange(final NetworkViewContainer vc) {
		vc.getViewTitleTextField().setText(null);
		vc.getViewTitleTextField().setVisible(false);
		vc.getViewTitleLabel().setVisible(true);
	}
	
	private class MousePressedAWTEventListener implements AWTEventListener {
		
        @Override
        public void eventDispatched(AWTEvent event) {
            if (event.getID() == MouseEvent.MOUSE_PRESSED && event instanceof MouseEvent) {
				final KeyboardFocusManager keyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
				final Window window = keyboardFocusManager.getActiveWindow();
				
				if (!(window instanceof NetworkViewFrame || window instanceof CytoscapeDesktop))
					return;
				
				// Detect if a new view container received the mouse pressed event.
				// If so, it must request focus.
				MouseEvent me = (MouseEvent) event;
                final Set<Component> targets = new HashSet<>();
                
                // Find the view container to be verified
                if (window instanceof NetworkViewFrame) {
                	targets.add(((NetworkViewFrame) window).getContainerRootPane().getContentPane());
                } else {
                	final Component currentCard = getCurrentCard();
                	
                	if (currentCard instanceof NetworkViewContainer) {
                		final NetworkViewContainer vc = (NetworkViewContainer) currentCard;
                		targets.add(vc.getContentPane());
                	} else if (currentCard instanceof NetworkViewComparisonPanel) {
                		// Get the view component which is not in focus
                		final NetworkViewComparisonPanel cp = (NetworkViewComparisonPanel) currentCard;
                		final NetworkViewContainer currentContainer = cp.getCurrentContainer();
                		
                		for (NetworkViewContainer vc : cp.getAllContainers()) {
                			if (vc != currentContainer)
                				targets.add(vc.getContentPane());
                		}
                	}
                }
                
                for (Component c : targets) {
                	me = SwingUtilities.convertMouseEvent(me.getComponent(), me, c);
                	
                	// Received the mouse event? So it should get focus now.
                	if (c.getBounds().contains(me.getPoint()))
                		c.requestFocusInWindow();
                }
            }
        }
    }
}
