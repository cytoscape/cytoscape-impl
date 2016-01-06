package org.cytoscape.internal.view;

import static org.cytoscape.internal.util.ViewUtil.invokeOnEDT;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.events.SetSelectedNetworkViewsEvent;
import org.cytoscape.application.events.SetSelectedNetworkViewsListener;
import org.cytoscape.application.events.SetSelectedNetworksEvent;
import org.cytoscape.application.events.SetSelectedNetworksListener;
import org.cytoscape.application.swing.CyHelpBroker;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.ColumnNameChangedEvent;
import org.cytoscape.model.events.ColumnNameChangedListener;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeLoadedEvent;
import org.cytoscape.session.events.SessionAboutToBeLoadedListener;
import org.cytoscape.session.events.SessionLoadCancelledEvent;
import org.cytoscape.session.events.SessionLoadCancelledListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.model.events.UpdateNetworkPresentationEvent;
import org.cytoscape.view.model.events.UpdateNetworkPresentationListener;
import org.cytoscape.view.model.events.ViewChangeRecord;
import org.cytoscape.view.model.events.ViewChangedEvent;
import org.cytoscape.view.model.events.ViewChangedListener;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;
import org.cytoscape.view.presentation.property.values.MappableVisualPropertyValue;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.events.SetCurrentVisualStyleEvent;
import org.cytoscape.view.vizmap.events.SetCurrentVisualStyleListener;
import org.cytoscape.view.vizmap.events.VisualStyleChangedEvent;
import org.cytoscape.view.vizmap.events.VisualStyleChangedListener;
import org.cytoscape.view.vizmap.events.VisualStyleSetEvent;
import org.cytoscape.view.vizmap.events.VisualStyleSetListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Managing views (presentations) in current session.
 * 
 */
public class NetworkViewManager implements NetworkViewAddedListener,
		NetworkViewAboutToBeDestroyedListener, SetCurrentNetworkViewListener, SetCurrentNetworkListener,
		SetSelectedNetworkViewsListener, SetSelectedNetworksListener, RowsSetListener,
		VisualStyleChangedListener, SetCurrentVisualStyleListener, UpdateNetworkPresentationListener,
		VisualStyleSetListener, SessionAboutToBeLoadedListener, SessionLoadCancelledListener, SessionLoadedListener,
		ColumnDeletedListener, ColumnNameChangedListener, ViewChangedListener {

	private static final Logger logger = LoggerFactory.getLogger(NetworkViewManager.class);

	@Deprecated
	private final JDesktopPane desktopPane;
	
	private final NetworkViewMainPanel networkViewMainPanel;

	// Key is MODEL ID
	private final Map<CyNetworkView, RenderingEngine<CyNetwork>> presentationMap;
	private final Set<CyNetworkView> viewUpdateRequired;

	/** columnIdentifier -> { valueInfo -> [views] }*/
	private final Map<CyColumnIdentifier, Map<MappedVisualPropertyValueInfo, Set<View<?>>>> mappedValuesMap;
	
	private volatile boolean loadingSession;
	private volatile boolean ignoreSeletedNetworkViewsEvents;

	private Object lock = new Object();
	
	private final CyServiceRegistrar serviceRegistrar;
	
	public NetworkViewManager(
			final NetworkViewMainPanel networkViewMainPanel,
			final CyHelpBroker help,
			final CyServiceRegistrar serviceRegistrar
	) {
		this.serviceRegistrar = serviceRegistrar;
		this.desktopPane = new JDesktopPane();
		this.networkViewMainPanel = networkViewMainPanel;

		// add Help hooks
		help.getHelpBroker().enableHelp(desktopPane, "network-view-manager", null);

		presentationMap = new WeakHashMap<>();
		viewUpdateRequired = new HashSet<>();
		mappedValuesMap = new HashMap<>();
		
		final NetworkViewGrid networkViewGrid = networkViewMainPanel.getNetworkViewGrid();
		
		networkViewGrid.addPropertyChangeListener("currentNetworkView", new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				final CyNetworkView targetView = (CyNetworkView) e.getNewValue();
				
				final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
				final CyNetworkViewManager netViewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
				final RenderingEngine<CyNetwork> currentEngine = appMgr.getCurrentRenderingEngine();
				
				if (targetView != null) {
					if (netViewMgr.getNetworkViewSet().contains(targetView)) {
						if (!targetView.equals(appMgr.getCurrentNetworkView()))
							appMgr.setCurrentNetworkView(targetView);
			
						if (currentEngine == null || currentEngine.getViewModel() != targetView)
							appMgr.setCurrentRenderingEngine(presentationMap.get(targetView));
						
						if (viewUpdateRequired.contains(targetView)) {
							viewUpdateRequired.remove(targetView);
							
							final VisualMappingManager vmm = serviceRegistrar.getService(VisualMappingManager.class);
							final VisualStyle style = vmm.getVisualStyle(targetView);
							style.apply(targetView);
							targetView.updateView();
						}
					}
				} else {
					if (appMgr.getCurrentNetworkView() != null)
						appMgr.setCurrentNetworkView(targetView);
					
					if (currentEngine != null)
						appMgr.setCurrentRenderingEngine(null);
				}
			}
		});
		
		networkViewMainPanel.addPropertyChangeListener("selectedNetworkViews", new PropertyChangeListener() {
			@Override
			@SuppressWarnings("unchecked")
			public void propertyChange(PropertyChangeEvent e) {
				if (!ignoreSeletedNetworkViewsEvents) {
					final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
					appMgr.setSelectedNetworkViews((List<CyNetworkView>) e.getNewValue());
				}
			}
		});
		
		networkViewGrid.addPropertyChangeListener("networkViews", new PropertyChangeListener() {
			@Override
			@SuppressWarnings("unchecked")
			public void propertyChange(PropertyChangeEvent e) {
				final Collection<CyNetworkView> newSet = (Collection<CyNetworkView>) e.getNewValue();
				
				final CyNetworkViewManager netViewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
				final Set<CyNetworkView> currentSet = netViewMgr.getNetworkViewSet();
				
				for (CyNetworkView view : currentSet) {
					if (!newSet.contains(view))
						netViewMgr.destroyNetworkView(view);
				}
			}
		});
	}

	public NetworkViewMainPanel getNetworkViewMainPanel() {
		return networkViewMainPanel;
	}
	
	/**
	 * Desktop for JInternalFrames which contains actual network presentations.
	 */
	@Deprecated
	public JDesktopPane getDesktopPane() {
		return desktopPane;
	}

	public JInternalFrame getInternalFrame(CyNetworkView view) throws IllegalArgumentException {
		return null; // TODO Fix for backwards compatibility
	}
	
	public CyNetworkView getNetworkView(JInternalFrame frame) throws IllegalArgumentException {
		return null; // TODO Fix for backwards compatibility
	}

	// // Event Handlers ////
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		final CyNetworkView view = e.getNetworkView();
		
		invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				onCurrentNetworkViewChanged(view);
			}
		});
	}

	@Override
	public void handleEvent(SetCurrentNetworkEvent e) {
		final CyNetwork net = e.getNetwork();
		CyNetworkView view = null;
		
		if (net != null) {
			final CyNetworkViewManager netViewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
			final Collection<CyNetworkView> views = netViewMgr.getNetworkViews(net);
			
			if (!views.isEmpty())
				view = views.iterator().next();
		}
		
		final CyNetworkView curView = view;
		
		invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				onCurrentNetworkViewChanged(curView);
			}
		});
	}

	@Override
	public void handleEvent(NetworkViewAboutToBeDestroyedEvent nvde) {
		final CyNetworkView view = nvde.getNetworkView();
		removeView(view);
	}

	/**
	 * Adding new network view model to this manager. Then, render presentation.
	 */
	@Override
	public void handleEvent(final NetworkViewAddedEvent nvae) {
		final CyNetworkView networkView = nvae.getNetworkView();
		render(networkView);
	}
	
	@Override
	public void handleEvent(final SetSelectedNetworksEvent e) {
		// Select all views of the selected networks
		final List<CyNetworkView> selectedViews = new ArrayList<>();
		final CyNetworkViewManager netViewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
		final List<CyNetwork> networks = e.getNetworks();
		
		for (CyNetwork net : networks)
			selectedViews.addAll(netViewMgr.getNetworkViews(net));
		
		invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				try {
					getNetworkViewMainPanel().setSelectedNetworkViews(selectedViews);
				} finally {
				}
			}
		});
	}

	@Override
	public void handleEvent(final SetSelectedNetworkViewsEvent e) {
		invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				ignoreSeletedNetworkViewsEvents = true;
				
				try {
					getNetworkViewMainPanel().setSelectedNetworkViews(e.getNetworkViews());
				} finally {
					ignoreSeletedNetworkViewsEvents = false;
				}
			}
		});
	}
	
	@Override
	public void handleEvent(final VisualStyleChangedEvent e) {
		if (loadingSession)
			return;
		
		if (e.getSource() != null && !getNetworkViewMainPanel().isEmpty()) {
			final Set<CyNetworkView> viewsSet = findNetworkViewsWithStyles(Collections.singleton(e.getSource()));
			
			for (final CyNetworkView view : viewsSet)
				updateView(view, null);
		}
	}

	@Override
	public void handleEvent(final SetCurrentVisualStyleEvent e) {
		if (loadingSession)
			return;
		
		final VisualStyle style = e.getVisualStyle();
		
		if (style != null) {
			final CyNetworkView curView = getNetworkViewMainPanel().getCurrentNetworkView();
			
			if (curView != null) {
				final VisualMappingManager vmm = serviceRegistrar.getService(VisualMappingManager.class);
				vmm.setVisualStyle(style, curView);
			}
		}
	}
	
	@Override
	public void handleEvent(final VisualStyleSetEvent e) {
		if (loadingSession)
			return;
		
		final CyNetworkView view = e.getNetworkView();
		updateView(view, null);
	}
	
	@Override
	public void handleEvent(final UpdateNetworkPresentationEvent e) {
		final CyNetworkView view = e.getSource();
		getNetworkViewMainPanel().update(view);
	}
	
	@Override
	public void handleEvent(final ViewChangedEvent<?> e) {
		final CyNetworkView netView = e.getSource();
		
		// Ask the Views Panel to update the thumbnail for the affected network view
		invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				getNetworkViewMainPanel().updateThumbnail(netView);
			}
		});
		
		// Look for MappableVisualPropertyValue objects, so they can be saved for future reference
		for (final ViewChangeRecord<?> record : e.getPayloadCollection()) {
			if (!record.isLockedValue())
				continue;
			
			final View<?> view = record.getView();
			final Object value = record.getValue();
			
			if (value instanceof MappableVisualPropertyValue) {
				final Set<CyColumnIdentifier> columnIds = ((MappableVisualPropertyValue)value).getMappedColumns();
				
				if (columnIds == null)
					continue;
				
				final VisualProperty<?> vp = record.getVisualProperty();
				
				for (final CyColumnIdentifier colId : columnIds) {
					Map<MappedVisualPropertyValueInfo, Set<View<?>>> mvpInfoMap = mappedValuesMap.get(colId);
					
					if (mvpInfoMap == null)
						mappedValuesMap.put(colId, mvpInfoMap = new HashMap<>());
					
					final MappedVisualPropertyValueInfo mvpInfo =
							new MappedVisualPropertyValueInfo((MappableVisualPropertyValue)value, vp, netView);
					Set<View<?>> viewSet = mvpInfoMap.get(mvpInfo);
					
					if (viewSet == null)
						mvpInfoMap.put(mvpInfo, viewSet = new HashSet<View<?>>());
					
					viewSet.add(view);
				}
			}
		}
	}

	@Override
	public void handleEvent(final SessionAboutToBeLoadedEvent e) {
		loadingSession = true;
	}
	
	@Override
	public void handleEvent(final SessionLoadCancelledEvent e) {
		loadingSession = false;
		// TODO Destroy rendered views
	}
	
	@Override
	public void handleEvent(final SessionLoadedEvent e) {
		loadingSession = false;
		
		invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				getNetworkViewMainPanel().setCurrentNetworkView(
						serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetworkView());
			}
		});
	}
	
	@Override
	public void handleEvent(final ColumnDeletedEvent e) {
		onColumnChanged(e.getSource(), e.getColumnName());
	}
	
	@Override
	public void handleEvent(final ColumnNameChangedEvent e) {
		onColumnChanged(e.getSource(), e.getOldColumnName());
		onColumnChanged(e.getSource(), e.getNewColumnName());
	}
	
	@Override
	public void handleEvent(final RowsSetEvent e) {
		if (loadingSession || getNetworkViewMainPanel().isEmpty())
			return;
		
		final CyTable tbl = e.getSource();
		
		// Update Network View Title
		final Collection<RowSetRecord> nameRecords = e.getColumnRecords(CyNetwork.NAME);
		
		invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				updateNetworkViewTitle(nameRecords, tbl);
			}
		});
		
		final CyNetworkTableManager netTblMgr = serviceRegistrar.getService(CyNetworkTableManager.class);
		final CyNetwork net = netTblMgr.getNetworkForTable(tbl);
		
		final CyNetworkViewManager netViewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
		
		// Is this column from a network table?
		// And if there is no related view, nothing needs to be done
		if ( net != null && netViewMgr.viewExists(net) && 
				(tbl.equals(net.getDefaultNodeTable()) || tbl.equals(net.getDefaultEdgeTable())) ) {
			final Collection<CyNetworkView> networkViews = netViewMgr.getNetworkViews(net);
			final Set<CyNetworkView> viewsToUpdate = new HashSet<>();
			
			for (final RowSetRecord record : e.getPayloadCollection()) {
				final String columnName = record.getColumn();
				
				// Reapply locked values that map to changed columns
				final boolean lockedValuesApplyed = reapplyLockedValues(columnName, networkViews);
				
				if (lockedValuesApplyed)
					viewsToUpdate.addAll(networkViews);
				
				// Find views that had their styles affected by the RowsSetEvent
				final Set<VisualStyle> styles = findStylesWithMappedColumn(columnName);
				viewsToUpdate.addAll(findNetworkViewsWithStyles(styles));
			}
			
			// Update views
			for (final CyNetworkView view : viewsToUpdate)
				updateView(view, null);
		}
	}

	private final void removeView(final CyNetworkView view) {
		invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				try {
					getNetworkViewMainPanel().remove(view);
					viewUpdateRequired.remove(view);
					final RenderingEngine<CyNetwork> removed = presentationMap.remove(view);
					
					if (removed != null) {
						new Thread() {
							@Override
							public void run() {
								serviceRegistrar.getService(RenderingEngineManager.class)
										.removeRenderingEngine(removed);
							}
						}.start();
					}
				} catch (Exception e) {
					logger.error("Unable to destroy Network View", e);
				}
			}
		});
	}

	/**
	 * Create a visualization container and add presentation to it.
	 */
	private final void render(final CyNetworkView view) {
		invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				// If already registered in this manager, do not render.
				if (getNetworkViewMainPanel().isRendered(view))
					return;

				NetworkViewRenderer renderer = null;
				final String rendererId = view.getRendererId();
				final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
				
				if (rendererId != null)
					renderer = appMgr.getNetworkViewRenderer(rendererId);
				
				if (renderer == null)
					renderer = appMgr.getDefaultNetworkViewRenderer();

				final RenderingEngineFactory<CyNetwork> engineFactory = renderer
						.getRenderingEngineFactory(NetworkViewRenderer.DEFAULT_CONTEXT);
				
				final RenderingEngine<CyNetwork> renderingEngine =
						getNetworkViewMainPanel().addNetworkView(view, engineFactory, !loadingSession);
				presentationMap.put(view, renderingEngine);
				
				new Thread() {
					@Override
					public void run() {
						serviceRegistrar.getService(RenderingEngineManager.class).addRenderingEngine(renderingEngine);
					}
				}.start();
			}
		});
	}

//	@SuppressWarnings("unchecked")
//	private void renderAsInternalFrame(final String title, final CyNetworkView view,
//			final RenderingEngineFactory<CyNetwork> engineFactory) {
//		final JInternalFrame iframe = new JInternalFrame(title, true, true, true, true);
//		
//		// This is to work around a bug with Mac JInternalFrame L&F that causes large borders (#3352)
//		if (LookAndFeelUtil.isAquaLAF()) {
//			iframe.putClientProperty("JInternalFrame.frameType", "normal");
//			iframe.getRootPane().setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, new Color(128, 128, 128, 128)));
//		}
//		
//		// This is for force move title bar to the desktop if it's out of range.
//		iframe.addMouseListener(new MouseAdapter() {
//			@Override
//			public void mouseReleased(MouseEvent e) {
//				final Point originalPoint = iframe.getLocation();
//				if (originalPoint.y < 0)
//					iframe.setLocation(originalPoint.x, 0);
//			}
//		});
//
//		final InternalFrameAdapter frameListener = new InternalFrameAdapter() {
//			@Override
//			public void internalFrameClosing(InternalFrameEvent e) {
//				final CyNetworkViewManager netViewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
//				
//				if (netViewMgr.getNetworkViewSet().contains(view))
//					netViewMgr.destroyNetworkView(view);
//
				// TODO
//				// See bug #1178 (item #3)
//				KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
//			}
//		};
//		
//		iframe.addInternalFrameListener(frameListener);
//		frameListeners.put(iframe, frameListener);
//		
//		desktopPane.add(iframe);
//		
//		synchronized (presentationContainerMap) {
//			presentationContainerMap.put(view, iframe);
//		}
//		
//		iFrameMap.put(iframe, view);
//		
//		final RenderingEngine<CyNetwork> renderingEngine = engineFactory.createRenderingEngine(iframe, view);
//		serviceRegistrar.getService(RenderingEngineManager.class).addRenderingEngine(renderingEngine);
//		
//		presentationMap.put(view, renderingEngine);
//		iframe.pack();
//
//		// create cascade iframe
//		int x = 0;
//		int y = 0;
//		JInternalFrame refFrame = null;
//		JInternalFrame[] allFrames = desktopPane.getAllFrames();
//
//		// frame Location
//		if (allFrames.length > 1)
//			refFrame = allFrames[0];
//
//		if (refFrame != null) {
//			x = refFrame.getLocation().x + 20;
//			y = refFrame.getLocation().y + 20;
//		}
//
//		if (x > (desktopPane.getWidth() - MINIMUM_WIN_WIDTH))
//			x = desktopPane.getWidth() - MINIMUM_WIN_WIDTH;
//		if (y > (desktopPane.getHeight() - MINIMUM_WIN_HEIGHT))
//			y = desktopPane.getHeight() - MINIMUM_WIN_HEIGHT;
//		if (x < 0)
//			x = 0;
//		if (y < 0)
//			y = 0;
//
//		iframe.setLocation(x, y);
//
//		// maximize the frame if the specified property is set
//		final CyProperty<Properties> cyProp = serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)");
//		final String max = cyProp.getProperties().getProperty("maximizeViewOnCreate");
//
//		if ((max != null) && Boolean.parseBoolean(max)) {
//			try {
//				iframe.setMaximum(true);
//			} catch (PropertyVetoException pve) {
//				logger.warn("Could not maximize frame.", pve);
//			}
//		} else {
//			int w = view.getVisualProperty(BasicVisualLexicon.NETWORK_WIDTH).intValue();
//			int h = view.getVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT).intValue();
//			boolean resizable = !view.isValueLocked(BasicVisualLexicon.NETWORK_WIDTH) &&
//					!view.isValueLocked(BasicVisualLexicon.NETWORK_HEIGHT);
//			updateNetworkFrameSize(view, w, h, resizable);
//		}
//
//		// Display it and add listeners
//		iframe.addComponentListener(new ComponentAdapter() {
//			@Override
//			public void componentResized(ComponentEvent e) {
//				view.setVisualProperty(BasicVisualLexicon.NETWORK_WIDTH, (double)iframe.getContentPane().getWidth());
//				view.setVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT, (double)iframe.getContentPane().getHeight());
//			}
//		});
//		
////		iframe.addInternalFrameListener(this);
//		iframe.setVisible(true);
//	}

	private void onCurrentNetworkViewChanged(final CyNetworkView view) {
		if (loadingSession)
			return;
		
		final CyNetworkView curView = getNetworkViewMainPanel().getCurrentNetworkView();
		
		// Same as current focus; no need to update view
		if ((curView == null && view == null) || (curView != null && curView.equals(view)))
			return;
		
		getNetworkViewMainPanel().setCurrentNetworkView(view);
	}

	private void onColumnChanged(final CyTable tbl, final String columnName) {
		if (loadingSession || getNetworkViewMainPanel().isEmpty())
			return;
		
		final CyNetworkTableManager netTblMgr = serviceRegistrar.getService(CyNetworkTableManager.class);
		final CyNetwork net = netTblMgr.getNetworkForTable(tbl);
		
		final CyNetworkViewManager netViewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
		
		// And if there is no related view, nothing needs to be done
		if ( net != null && netViewMgr.viewExists(net) && 
				(tbl.equals(net.getDefaultNodeTable()) || tbl.equals(net.getDefaultEdgeTable())) ) {
			// Reapply locked values that map to changed columns
			final Collection<CyNetworkView> networkViews = netViewMgr.getNetworkViews(net);
			final boolean lockedValuesApplyed = reapplyLockedValues(columnName, networkViews);
			
			// Find views that had their styles affected by the RowsSetEvent
			final Set<VisualStyle> styles = findStylesWithMappedColumn(columnName);
			final Set<CyNetworkView> viewsToUpdate = findNetworkViewsWithStyles(styles);
			
			if (lockedValuesApplyed)
				viewsToUpdate.addAll(networkViews);
			
			// Update views
			for (final CyNetworkView view : viewsToUpdate)
				updateView(view, null);
		}
	}
	
	private final void updateNetworkViewTitle(final Collection<RowSetRecord> records, final CyTable source) {
		for (final RowSetRecord record : records) {
			if (CyNetwork.NAME.equals(record.getColumn())) { // TODO test again
				final CyNetworkViewManager netViewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
				
				// Assume payload collection is for same column
				synchronized (lock) {
					for (CyNetworkView view : netViewMgr.getNetworkViewSet()) {
						final CyNetwork net = view.getModel();

						if (net.getDefaultNetworkTable() == source) {
							final String name = record.getRow().get(CyNetwork.NAME, String.class);
							final String title = view.getVisualProperty(BasicVisualLexicon.NETWORK_TITLE);
							
							// TODO: Only update the view's title if the current title and the network name are in sync,
							// because users can change the Network View title at any time
							if (name != null && title == null || title.trim().isEmpty()) {
								view.setVisualProperty(BasicVisualLexicon.NETWORK_TITLE, name);
	
								// Does not need to update the rendered title with the new network name
								// if this visual property is locked
								if (!view.isValueLocked(BasicVisualLexicon.NETWORK_TITLE))
									getNetworkViewMainPanel().update(view);
	
								return; // assuming just one row is set.
							}
						}
					}
				}
			}
		}
	}
	
	public void setUpdateFlag(final CyNetworkView view) {
		viewUpdateRequired.add(view);
	}

	private Set<VisualStyle> findStylesWithMappedColumn(final String columnName) {
		final Set<VisualStyle> styles = new HashSet<VisualStyle>();
		final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
		final RenderingEngine<CyNetwork> renderer = appMgr.getCurrentRenderingEngine();
		
		if (columnName != null && renderer != null) {
			final Set<VisualProperty<?>> properties = renderer.getVisualLexicon().getAllVisualProperties();
			final VisualMappingManager vmm = serviceRegistrar.getService(VisualMappingManager.class);
			
			for (final VisualStyle vs : vmm.getAllVisualStyles()) {
				for (final VisualProperty<?> vp : properties) {
					// Check VisualMappingFunction
					final VisualMappingFunction<?, ?> fn = vs.getVisualMappingFunction(vp);
					
					if (fn != null && fn.getMappingColumnName().equalsIgnoreCase(columnName)) {
						styles.add(vs);
						break;
					}
					
					// Check MappableVisualPropertyValue
					final Object defValue = vs.getDefaultValue(vp);
					
					if (defValue instanceof MappableVisualPropertyValue) {
						((MappableVisualPropertyValue) defValue).update();
						styles.add(vs);
						break;
					}
				}
			}
		}
		
		return styles;
	}
	
	private Set<CyNetworkView> findNetworkViewsWithStyles(final Set<VisualStyle> styles) {
		final Set<CyNetworkView> result = new HashSet<CyNetworkView>();
		
		if (styles == null || styles.isEmpty())
			return result;
		
		// First, check current view.  If necessary, apply it.
		final CyNetworkViewManager netViewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
		final Set<CyNetworkView> networkViews = netViewMgr.getNetworkViewSet();
		
		final VisualMappingManager vmm = serviceRegistrar.getService(VisualMappingManager.class);
		
		for (final CyNetworkView view: networkViews) {
			if (styles.contains(vmm.getVisualStyle(view)))
				result.add(view);
		}
		
		return result;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean reapplyLockedValues(final String columnName, final Collection<CyNetworkView> networkViews) {
		boolean result = false;
		
		final CyColumnIdentifierFactory colIdfFactory = serviceRegistrar.getService(CyColumnIdentifierFactory.class);
		final CyColumnIdentifier colId = colIdfFactory.createColumnIdentifier(columnName);
		final Map<MappedVisualPropertyValueInfo, Set<View<?>>> mvpInfoMap = mappedValuesMap.get(colId);
		
		if (mvpInfoMap != null) {
			for (final MappedVisualPropertyValueInfo mvpInfo : mvpInfoMap.keySet()) {
				if (networkViews == null || !networkViews.contains(mvpInfo.getNetworkView()))
					continue;
				
				final MappableVisualPropertyValue value = mvpInfo.getValue();
				final VisualProperty vp = mvpInfo.getVisualProperty();
				final Set<View<?>> viewSet = mvpInfoMap.get(mvpInfo);
				
				for (final View<?> view : viewSet) {
					if (view.isDirectlyLocked(vp) && value.equals(view.getVisualProperty(vp))) {
						value.update();
						view.setLockedValue(vp, value);
						result = true;
					}
				}
			}
		}
		
		return result;
	}
	
	private void updateView(final CyNetworkView view, VisualStyle vs) {
		if (view == null)
			return;
		
		final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
		
		if (view.equals(appMgr.getCurrentNetworkView())) {
			if (vs == null) {
				final VisualMappingManager vmm = serviceRegistrar.getService(VisualMappingManager.class);
				vs = vmm.getVisualStyle(view);
			}
			
			vs.apply(view);
			view.updateView();
		} else {
			viewUpdateRequired.add(view);
		}
	}
	
	private static class MappedVisualPropertyValueInfo {
		
		private final MappableVisualPropertyValue value;
		private final VisualProperty<?> visualProperty;
		private final CyNetworkView networkView;
		
		MappedVisualPropertyValueInfo(final MappableVisualPropertyValue value,
									  final VisualProperty<?> visualProperty,
									  final CyNetworkView networkView) {
			this.value = value;
			this.visualProperty = visualProperty;
			this.networkView = networkView;
		}
		
		MappableVisualPropertyValue getValue() {
			return value;
		}

		VisualProperty<?> getVisualProperty() {
			return visualProperty;
		}
		
		CyNetworkView getNetworkView() {
			return networkView;
		}

		@Override
		public int hashCode() {
			final int prime = 29;
			int result = 3;
			result = prime * result + ((networkView == null) ? 0 : networkView.hashCode());
			result = prime * result + ((value == null) ? 0 : value.hashCode());
			result = prime
					* result
					+ ((visualProperty == null) ? 0 : visualProperty.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MappedVisualPropertyValueInfo other = (MappedVisualPropertyValueInfo) obj;
			if (networkView == null) {
				if (other.networkView != null)
					return false;
			} else if (!networkView.equals(other.networkView))
				return false;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			if (visualProperty == null) {
				if (other.visualProperty != null)
					return false;
			} else if (!visualProperty.equals(other.visualProperty))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "{vp:" + visualProperty + ", value:" + value + ", networkView:" + networkView + "}";
		}
	}
}
