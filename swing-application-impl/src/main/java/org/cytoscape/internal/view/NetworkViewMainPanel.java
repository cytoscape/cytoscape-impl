package org.cytoscape.internal.view;

import static org.cytoscape.internal.util.ViewUtil.createUniqueKey;
import static org.cytoscape.internal.util.ViewUtil.getTitle;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.internal.view.NetworkViewComparisonPanel.ViewPanel;
import org.cytoscape.internal.view.NetworkViewGrid.ThumbnailPanel;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.destroy.DestroyNetworkViewTaskFactory;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.swing.DialogTaskManager;

@SuppressWarnings("serial")
public class NetworkViewMainPanel extends JPanel {

	private JPanel contentPane;
	private final CardLayout cardLayout;
	private final NetworkViewGrid networkViewGrid;
	
	private final Map<String, NetworkViewContainer> viewContainers;
	private final Map<String, NetworkViewFrame> viewFrames;
	private final Map<String, NetworkViewComparisonPanel> comparisonPanels;
	
	private final Set<CyNetworkView> dirtyThumbnails;
	
	private NetworkViewFrame currentViewFrame;
	
	private final MouseEventRedispatcher mouseEventRedispatcher;
	
	private final CytoscapeMenus cyMenus;
	private final CyServiceRegistrar serviceRegistrar;

	public NetworkViewMainPanel(final CytoscapeMenus cyMenus, final CyServiceRegistrar serviceRegistrar) {
		this.cyMenus = cyMenus;
		this.serviceRegistrar = serviceRegistrar;
		
		viewContainers = new LinkedHashMap<>();
		viewFrames = new HashMap<>();
		comparisonPanels = new HashMap<>();
		dirtyThumbnails = new HashSet<>();
		
		mouseEventRedispatcher = new MouseEventRedispatcher();
		
		cardLayout = new CardLayout();
		networkViewGrid = createNetworkViewGrid();
		
		init();
	}

	public RenderingEngine<CyNetwork> addNetworkView(final CyNetworkView view,
			final RenderingEngineFactory<CyNetwork> engineFactory, boolean showView) {
		if (isRendered(view))
			return null;
		
		final GraphicsConfiguration gc = currentViewFrame != null ? currentViewFrame.getGraphicsConfiguration() : null;
		
		final NetworkViewContainer vc = new NetworkViewContainer(view, engineFactory, serviceRegistrar);
		vc.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				setCurrentNetworkView(view);
				setSelectedNetworkViews(Collections.singletonList(view));
			}
		});
		vc.getGridModeButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showGrid();
				networkViewGrid.requestFocusInWindow();
			}
		});
		vc.getDetachViewButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				detachNetworkView(view);
			}
		});
		vc.getReattachViewButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				reattachNetworkView(view);
			}
		});
		vc.getViewTitleTextField().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				changeCurrentViewTitle(vc);
				vc.requestFocusInWindow();
			}
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
				addMouseEventRedispatcher(mouseEventRedispatcher, vc.getGlassPane());
			}
			@Override
			public void focusGained(FocusEvent e) {
				removeMouseEventRedispatcher(mouseEventRedispatcher, vc.getGlassPane());
			}
		});
		
		addMouseEventRedispatcher(mouseEventRedispatcher, vc.getGlassPane());
		
		viewContainers.put(vc.getName(), vc);
		networkViewGrid.addItem(vc.getRenderingEngine());
		getContentPane().add(vc, vc.getName());
		
		if (showView) {
			// Always show attached container first, even if it will be detached later
			// so the thumbnail can be generated
			showViewContainer(vc.getName());
			setDirtyThumbnail(view);
			
			// If the latest focused view was in a detached frame,
			// detach the new one as well and put it in the same monitor
			if (gc != null)
				detachNetworkView(view, gc);
		} else {
			showGrid();
		}
		
		return vc.getRenderingEngine();
	}
	
	public boolean isRendered(final CyNetworkView view) {
		final String name = createUniqueKey(view);
		return viewContainers.containsKey(name) || viewFrames.containsKey(name);
	}

	public void remove(final CyNetworkView view) {
		if (view == null)
			return;
		
		dirtyThumbnails.remove(view);
		
		RenderingEngine<CyNetwork> re = null;
		final int total = getContentPane().getComponentCount();
		
		for (int i = 0; i < total; i++) {
			final Component c = getContentPane().getComponent(i);
			
			if (c instanceof NetworkViewContainer) {
				final NetworkViewContainer vc = (NetworkViewContainer) c;
				
				if (vc.getNetworkView().equals(view)) {
					cardLayout.removeLayoutComponent(c);
					viewContainers.remove(vc.getName());
					re = vc.getRenderingEngine();
					
					vc.dispose();
					
					break;
				}
			} else if (c instanceof NetworkViewComparisonPanel) {
				// TODO
			}
		}
		
		final NetworkViewFrame frame = viewFrames.remove(createUniqueKey(view));
		
		if (frame != null) {
			re = frame.getRenderingEngine();
			
			frame.getRootPane().getLayeredPane().removeAll();
			frame.getRootPane().getContentPane().removeAll();
			frame.dispose();
			
			for (ComponentListener l : frame.getComponentListeners())
				frame.removeComponentListener(l);
			
			for (WindowListener l : frame.getWindowListeners())
				frame.removeWindowListener(l);
		}
		
		if (re != null) {
			networkViewGrid.removeItems(Collections.singleton(re));
			showGrid();
		}
	}
	
	public void setSelectedNetworkViews(final List<CyNetworkView> networkViews) {
		final Set<ThumbnailPanel> selectedItems = new HashSet<>();
		
		for (ThumbnailPanel tp : networkViewGrid.getItems()) {
			if (networkViews.contains(tp.getNetworkView()))
				selectedItems.add(tp);
		}
			
		networkViewGrid.setSelectedItems(selectedItems);
	}
	
	public List<CyNetworkView> getSelectedNetworkViews() {
		return getNetworkViews(networkViewGrid.getSelectedItems());
	}

	public CyNetworkView getCurrentNetworkView() {
		return networkViewGrid.getCurrentNetworkView();
	}
	
	public void setCurrentNetworkView(final CyNetworkView view) {
		networkViewGrid.setCurrentNetworkView(view);
		
		if (view == null) {
			showGrid();
		} else {
			if (isGridMode()) {
				if (networkViewGrid.getCurrentItem() != null)
					networkViewGrid.scrollRectToVisible(networkViewGrid.getCurrentItem().getBounds());
			} else {
				showViewContainer(createUniqueKey(view));
			}
		}
	}
	
	public void showGrid() {
		if (!isGridMode()) {
			cardLayout.show(getContentPane(), networkViewGrid.getName());
			networkViewGrid.update(networkViewGrid.getThumbnailSlider().getValue()); // TODO remove it when already updating after view changes
			networkViewGrid.getReattachAllViewsButton().setEnabled(!viewFrames.isEmpty());
			
			final HashSet<CyNetworkView> dirtySet = new HashSet<>(dirtyThumbnails);
			
			for (CyNetworkView view : dirtySet)
				updateThumbnail(view);
		}
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
		
		final NetworkViewContainer vc = getNetworkViewContainer(view);
		
		if (vc == null)
			return null;
		
		final String name = vc.getName();
		
		cardLayout.removeLayoutComponent(vc);
		viewContainers.remove(name);
		
		final NetworkViewFrame frame = new NetworkViewFrame(vc, gc, serviceRegistrar);
		vc.setDetached(true);
		vc.setComparing(false);
		
		viewFrames.put(name, frame);
		
		if (!LookAndFeelUtil.isAquaLAF())
			frame.setJMenuBar(cyMenus.createDummyMenuBar());
		
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {
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
						setCurrentNetworkView(frame.getNetworkView());
						setSelectedNetworkViews(Collections.singletonList(frame.getNetworkView()));
					}
				});
			}
		});
		frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				view.setVisualProperty(BasicVisualLexicon.NETWORK_WIDTH, (double)frame.getContentPane().getWidth());
				view.setVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT, (double)frame.getContentPane().getHeight());
				view.updateView();
			};
		});
		
		int w = view.getVisualProperty(BasicVisualLexicon.NETWORK_WIDTH).intValue();
		int h = view.getVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT).intValue();
		final boolean resizable = !view.isValueLocked(BasicVisualLexicon.NETWORK_WIDTH) &&
				!view.isValueLocked(BasicVisualLexicon.NETWORK_HEIGHT);
		
		if (w > 0 && h > 0)
			frame.getContentPane().setPreferredSize(new Dimension(w, h));
		
		getNetworkViewGrid().setDetached(vc.getNetworkView(), true);
		showGrid();
		
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
			viewContainers.put(vc.getName(), vc);
			getNetworkViewGrid().setDetached(vc.getNetworkView(), false);
			showViewContainer(vc.getName());
		}
	}
	
	public void updateThumbnail(final CyNetworkView view) {
		networkViewGrid.updateThumbnail(view);
		dirtyThumbnails.remove(view);
	}
	
	public void updateThumbnailPanel(final CyNetworkView view, final boolean redraw) {
		// If the Grid is not visible, just flag this view as dirty.
		// IMPORTANT: If we update the grid thumbnail before the actual view canvas is updated,
		//            Ding flags itself as not dirty and thinks it does not need
		//            to redraw the main view anymore.
		if (isGridMode()) {
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
		} else if (!isGridMode()) {
			final NetworkViewContainer vc = getNetworkViewContainer(view);
			
			if (vc != null && vc.equals(getCurrentViewContainer()))
				vc.update();
			
			final NetworkViewComparisonPanel cp = getComparisonPanel(view);
			
			if (cp != null)
				cp.update();
		}
		
		updateThumbnailPanel(view, false);
	}
	
	public boolean isEmpty() {
		return viewFrames.isEmpty() && viewContainers.isEmpty();
	}
	
	public NetworkViewGrid getNetworkViewGrid() {
		return networkViewGrid;
	}
	
	public Set<NetworkViewFrame> getAllNetworkViewFrames() {
		return new HashSet<>(viewFrames.values());
	}
	
	/**
	 * @param view
	 * @return The current NetworkViewContainer
	 */
	public NetworkViewContainer showViewContainer(final CyNetworkView view) {
		return view != null ? showViewContainer(createUniqueKey(view)) : null;
	}
	
	/**
	 * @param name
	 * @return The current NetworkViewContainer
	 */
	private NetworkViewContainer showViewContainer(final String name) {
		NetworkViewContainer viewContainer = null;
		
		if (name != null) {
			viewContainer = viewContainers.get(name);
			
			if (viewContainer != null) {
				cardLayout.show(getContentPane(), name);
				viewContainer.update();
				currentViewFrame = null;
			} else {
				for (NetworkViewComparisonPanel cp : comparisonPanels.values()) {
					if (name.equals(cp.getName())
							|| name.equals(cp.getContainer1().getName())
							|| name.equals(cp.getContainer2().getName())) {
						cardLayout.show(getContentPane(), cp.getName());
						cp.update();
						currentViewFrame = null;
						viewContainer = cp.getViewPanel1().isCurrent() ? cp.getContainer1() : cp.getContainer2();
						break;
					}
				}
			}
		} else {
			showGrid();
		}
		
		return viewContainer;
	}

	private void showViewFrame(final NetworkViewFrame frame) {
		frame.setVisible(true);
		frame.toFront();
		showGrid();
	}
	
	private void showComparisonPanel(final int orientation, final CyNetworkView view1, final CyNetworkView view2) {
		final CyNetworkView currentView = getCurrentNetworkView();
		final String key = NetworkViewComparisonPanel.createUniqueKey(view1, view2);
		NetworkViewComparisonPanel cp = comparisonPanels.get(key);
		
		if (cp == null) {
			// End previous comparison panels that have one of the new selected views first
			cp = getComparisonPanel(view1);
			
			if (cp != null)
				endComparison(cp);
			
			cp = getComparisonPanel(view2);
			
			if (cp != null)
				endComparison(cp);
			
			// Then check if any of the views are detached
			final NetworkViewFrame frame1 = getNetworkViewFrame(view1);
			
			if (frame1 != null)
				reattachNetworkView(view1);
			
			final NetworkViewFrame frame2 = getNetworkViewFrame(view2);
			
			if (frame2 != null)
				reattachNetworkView(view2);
			
			final NetworkViewContainer vc1 = getNetworkViewContainer(view1);
			final NetworkViewContainer vc2 = getNetworkViewContainer(view2);
			
			cardLayout.removeLayoutComponent(vc1);
			viewContainers.remove(vc1.getName());
			cardLayout.removeLayoutComponent(vc2);
			viewContainers.remove(vc2.getName());
			
			// Now we can create the comparison panel
			cp = new NetworkViewComparisonPanel(orientation, vc1, vc2, currentView, serviceRegistrar);
			
			cp.getGridModeButton().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final Component currentCard = getCurrentCard();
					
					if (currentCard instanceof NetworkViewComparisonPanel) {
						endComparison((NetworkViewComparisonPanel) currentCard);
						networkViewGrid.requestFocusInWindow();
					}
				}
			});
			
			cp.getDetachComparedViewsButton().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					final Component currentCard = getCurrentCard();
					
					if (currentCard instanceof NetworkViewComparisonPanel) {
						final NetworkViewComparisonPanel cp = (NetworkViewComparisonPanel) currentCard;
						final CyNetworkView view1 = cp.getContainer1().getNetworkView();
						final CyNetworkView view2 = cp.getContainer2().getNetworkView();
						
						// End comparison first
						endComparison(cp);
						
						// Then detach the views
						detachNetworkView(view1);
						detachNetworkView(view2);
					}
				}
			});
			
			cp.addPropertyChangeListener("currentNetworkView", new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					final CyNetworkView newCurrentView = (CyNetworkView) evt.getNewValue();
					setCurrentNetworkView(newCurrentView);
				}
			});
			
			getContentPane().add(cp, cp.getName());
			comparisonPanels.put(cp.getName(), cp);
		}
		
		if (cp != null)
			showViewContainer(cp.getName());
	}
	
	private void endComparison(final NetworkViewComparisonPanel cp) {
		if (cp != null) {
			final NetworkViewContainer vc1 = cp.getContainer1();
			final NetworkViewContainer vc2 = cp.getContainer2();
			
			cardLayout.removeLayoutComponent(cp);
			comparisonPanels.remove(cp.getName());
			cp.dispose(); // Don't forget to call this method!
			
			getContentPane().add(vc1, vc1.getName());
			viewContainers.put(vc2.getName(), vc2);
			getContentPane().add(vc2, vc2.getName());
			viewContainers.put(vc1.getName(), vc1);
			
			showGrid();
		}
	}
	
	private NetworkViewComparisonPanel getComparisonPanel(final CyNetworkView view) {
		for (NetworkViewComparisonPanel cp : comparisonPanels.values()) {
			if (cp.getContainer1().getNetworkView().equals(view)
					|| cp.getContainer2().getNetworkView().equals(view))
				return cp;
		}
		
		return null;
	}
	
	public boolean isGridMode() {
		return getCurrentCard() == networkViewGrid;
	}
	
	private NetworkViewContainer getCurrentViewContainer() {
		final Component c = getCurrentCard();

		return c instanceof NetworkViewContainer ? (NetworkViewContainer) c : null;
	}
	
	private Component getCurrentCard() {
		Component current = null;
		
		for (Component comp : getContentPane().getComponents()) {
			if (comp.isVisible())
				current = comp;
		}
		
		return current;
	}
	
	private NetworkViewGrid createNetworkViewGrid() {
		final NetworkViewGrid nvg = new NetworkViewGrid(serviceRegistrar);
		
		nvg.getViewModeButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final CyNetworkView currentView = getCurrentNetworkView();
				NetworkViewContainer viewContainer = null;
				
				if (currentView != null) {
					viewContainer = showViewContainer(currentView);
				} else {
					final List<ThumbnailPanel> selectedItems = networkViewGrid.getSelectedItems();
					
					if (!selectedItems.isEmpty())
						viewContainer = showViewContainer(selectedItems.get(0).getNetworkView());
					else if (!networkViewGrid.isEmpty())
						viewContainer = showViewContainer(networkViewGrid.firstItem().getNetworkView());
				}
				
				if (viewContainer != null)
					viewContainer.getContentPane().requestFocusInWindow();
			}
		});
		
		nvg.getComparisonModeButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final List<CyNetworkView> selectedViews = getSelectedNetworkViews();

				if (selectedViews.size() == 2)
					showComparisonPanel(NetworkViewComparisonPanel.HORIZONTAL, selectedViews.get(0),
							selectedViews.get(1));
			}
		});
		
		nvg.getDetachSelectedViewsButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final List<ThumbnailPanel> selectedItems = networkViewGrid.getSelectedItems();

				if (selectedItems != null) {
					// Get the current view first
					final CyNetworkView currentView = getCurrentNetworkView();

					// Detach the views
					for (ThumbnailPanel tp : selectedItems) {
						if (getNetworkViewContainer(tp.getNetworkView()) != null)
							detachNetworkView(tp.getNetworkView());
					}

					// Set the original current view by bringing its frame to front, if it is detached
					final NetworkViewFrame frame = getNetworkViewFrame(currentView);

					if (frame != null)
						frame.toFront();
				}
			}
		});
		
		nvg.getReattachAllViewsButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final Collection<NetworkViewFrame> allFrames = new ArrayList<>(viewFrames.values());

				for (NetworkViewFrame f : allFrames)
					reattachNetworkView(f.getNetworkView());
			}
		});
		
		nvg.getDestroySelectedViewsButton().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				final List<CyNetworkView> selectedViews = getSelectedNetworkViews();
				
				if (selectedViews != null && !selectedViews.isEmpty()) {
					final DialogTaskManager taskMgr = serviceRegistrar.getService(DialogTaskManager.class);
					final DestroyNetworkViewTaskFactory taskFactory =
							serviceRegistrar.getService(DestroyNetworkViewTaskFactory.class);
					taskMgr.execute(taskFactory.createTaskIterator(selectedViews));
				}
			}
		});
		
		return nvg;
	}
	
	private void init() {
		setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, UIManager.getColor("Separator.foreground")));
		
		setLayout(new BorderLayout());
		add(getContentPane(), BorderLayout.CENTER);
		
		// Add Listeners
		networkViewGrid.addPropertyChangeListener("thumbnailPanels", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				networkViewGrid.updateToolBar();
				networkViewGrid.getReattachAllViewsButton().setEnabled(!viewFrames.isEmpty()); // TODO Should not be done here
				
				for (ThumbnailPanel tp : networkViewGrid.getItems()) {
					tp.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseClicked(MouseEvent e) {
							if (e.getClickCount() == 2) {
								// Double-Click: set this one as current and show attached view or view frame
								final NetworkViewFrame frame = getNetworkViewFrame(tp.getNetworkView());
									
								if (frame != null) {
									showViewFrame(frame);
								} else {
									final NetworkViewContainer vc = showViewContainer(tp.getNetworkView());
									
									if (vc != null)
										vc.getContentPane().requestFocusInWindow();
								}
							}
						}
					});
					tp.addComponentListener(new ComponentAdapter() {
						@Override
						public void componentResized(ComponentEvent e) {
							updateThumbnail(tp.getNetworkView());
						};
					});
				}
			}
		});
		
		networkViewGrid.addPropertyChangeListener("selectedItems", new PropertyChangeListener() {
			@Override
			@SuppressWarnings("unchecked")
			public void propertyChange(PropertyChangeEvent e) {
				networkViewGrid.updateToolBar();
				networkViewGrid.getReattachAllViewsButton().setEnabled(!viewFrames.isEmpty()); // TODO
				
				firePropertyChange("selectedNetworkViews",
						getNetworkViews((Collection<ThumbnailPanel>) e.getOldValue()),
						getNetworkViews((Collection<ThumbnailPanel>) e.getNewValue()));
			}
		});
		
		// Update
		showGrid();
	}
	
	private JPanel getContentPane() {
		if (contentPane == null) {
			contentPane = new JPanel();
			contentPane.setLayout(cardLayout);
			// Add the first panel in the card layout
			contentPane.add(networkViewGrid, networkViewGrid.getName());
		}
		
		return contentPane;
	}

	private NetworkViewContainer getNetworkViewContainer(final CyNetworkView view) {
		return view != null ? viewContainers.get(createUniqueKey(view)) : null;
	}
	
	private NetworkViewFrame getNetworkViewFrame(final CyNetworkView view) {
		return view != null ? viewFrames.get(createUniqueKey(view)) : null;
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
	
	private static List<CyNetworkView> getNetworkViews(final Collection<ThumbnailPanel> thumbnailPanels) {
		final List<CyNetworkView> views = new ArrayList<>();
		
		for (ThumbnailPanel tp : thumbnailPanels)
			views.add(tp.getNetworkView());
		
		return views;
	}
	
	private static void addMouseEventRedispatcher(final MouseEventRedispatcher redispatcher, final Component source) {
		source.addMouseListener(redispatcher);
		source.addMouseMotionListener(redispatcher);
		source.addMouseWheelListener(redispatcher);
	}
	
	private static void removeMouseEventRedispatcher(final MouseEventRedispatcher redispatcher, final Component source) {
		source.removeMouseListener(redispatcher);
		source.removeMouseMotionListener(redispatcher);
		source.removeMouseWheelListener(redispatcher);
	}
	
	private class MouseEventRedispatcher implements MouseListener, MouseMotionListener, MouseWheelListener {
		
		// MouseListener
		@Override
		public void mouseReleased(MouseEvent e) {
			redispatchMouseEvent(e, e.getComponent());
		}

		@Override
		public void mousePressed(MouseEvent e) {
			redispatchMouseEvent(e, e.getComponent());
			final Container target = getTarget(e.getComponent());

			if (target != null)
				target.requestFocusInWindow();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			redispatchMouseEvent(e, e.getComponent());
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			redispatchMouseEvent(e, e.getComponent());
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			redispatchMouseEvent(e, e.getComponent());
		}

		// MouseMotionListener
		@Override
		public void mouseMoved(MouseEvent e) {
			redispatchMouseEvent(e, e.getComponent());
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			redispatchMouseEvent(e, e.getComponent());
		}

		// MouseWheelListener
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			redispatchMouseEvent(e, e.getComponent());
		}
		
		private void redispatchMouseEvent(final MouseEvent e, final Component source) {
			final Container target = getTarget(source);
			
			if (target == null)
				return;
			
			final Point glassPanePoint = e.getPoint();
			Point containerPoint = SwingUtilities.convertPoint(source, glassPanePoint, target);

			if (containerPoint.y >= 0) {
				// The mouse event is probably over the content pane, so find out which component it's over
				final Component comp = SwingUtilities.getDeepestComponentAt(target, containerPoint.x, containerPoint.y);
				
				if (comp != null) {
					// Forward events over the check box.
					final Point componentPoint = SwingUtilities.convertPoint(source, glassPanePoint, comp);
					final MouseEvent newMouseEvent;
					
					if (e instanceof MouseWheelEvent) {
						final MouseWheelEvent we = ((MouseWheelEvent) e);
						
						newMouseEvent = new MouseWheelEvent(comp, e.getID(), e.getWhen(), e.getModifiers(),
								componentPoint.x, componentPoint.y, e.getClickCount(), e.isPopupTrigger(),
								we.getScrollType(), we.getScrollAmount(), we.getWheelRotation());
					} else {
						newMouseEvent = new MouseEvent(comp, e.getID(), e.getWhen(), e.getModifiers(),
								componentPoint.x, componentPoint.y, e.getClickCount(), e.isPopupTrigger());
					}
					
					comp.dispatchEvent(newMouseEvent);
				}
			}
		}
		
		private Container getTarget(final Component source) {
			final NetworkViewContainer vc = getParentContainer(source, NetworkViewContainer.class);
			
			if (vc != null) // View Mode (docked View)
				return vc.getContentPane();
			
			final ViewPanel vp = getParentContainer(source, ViewPanel.class); // Compare mode
			
			if (vp != null)
				return vp.getNetworkViewContainer().getContentPane();
			
			final NetworkViewFrame vf = getParentContainer(source, NetworkViewFrame.class); // Detached view
			
			if (vf != null)
				return vf.getContainerRootPane().getContentPane();
			
			return null;
		}
		
		@SuppressWarnings("unchecked")
		private <T extends Container> T getParentContainer(Component c, Class<T> type) {
			Container parent = c.getParent();
			
			while (parent != null) {
				if (parent.getClass() == type)
					return (T) parent;
				
				parent = parent.getParent();
			}
			
			return null;
		}
	}
}
