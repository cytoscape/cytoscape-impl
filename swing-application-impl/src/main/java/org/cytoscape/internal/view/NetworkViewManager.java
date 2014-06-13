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

import java.awt.Dimension;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CyHelpBroker;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.property.CyProperty;
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
import org.cytoscape.view.model.events.LockedValueSetRecord;
import org.cytoscape.view.model.events.LockedValuesSetEvent;
import org.cytoscape.view.model.events.LockedValuesSetListener;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.model.events.UpdateNetworkPresentationEvent;
import org.cytoscape.view.model.events.UpdateNetworkPresentationListener;
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
public class NetworkViewManager extends InternalFrameAdapter implements NetworkViewAddedListener,
		NetworkViewAboutToBeDestroyedListener, SetCurrentNetworkViewListener, SetCurrentNetworkListener,
		RowsSetListener, VisualStyleChangedListener, SetCurrentVisualStyleListener, UpdateNetworkPresentationListener,
		VisualStyleSetListener, SessionAboutToBeLoadedListener, SessionLoadCancelledListener, SessionLoadedListener,
		ColumnDeletedListener, LockedValuesSetListener {

	private static final Logger logger = LoggerFactory.getLogger(NetworkViewManager.class);

	// TODO Where should we store these property constants?
	private static final String VIEW_THRESHOLD = "viewThreshold";
	private static final int DEF_VIEW_THRESHOLD = 10000;

	private static final int MINIMUM_WIN_WIDTH = 200;
	private static final int MINIMUM_WIN_HEIGHT = 200;

	private final JDesktopPane desktopPane;

	// Key is MODEL ID
	private final Map<CyNetworkView, JInternalFrame> presentationContainerMap;
	private final Map<CyNetworkView, RenderingEngine<CyNetwork>> presentationMap;
	private final Set<CyNetworkView> viewUpdateRequired;

	private final Map<JInternalFrame, CyNetworkView> iFrameMap;
	private final Map<JInternalFrame, InternalFrameListener> frameListeners;
	private final Properties props;
	
	/** columnIdentifier -> { valueInfo -> [views] }*/
	private final Map<CyColumnIdentifier, Map<MappedVisualPropertyValueInfo, Set<View<? extends CyIdentifiable>>>> mappedValuesMap;
	
	private volatile boolean loadingSession;

	private final CyNetworkViewManager netViewMgr;
	private final CyApplicationManager appMgr;
	private final RenderingEngineManager renderingEngineMgr;
	private final VisualMappingManager vmm;
	private final CyNetworkTableManager netTblMgr;
	private final CyColumnIdentifierFactory colIdfFactory;
	
	public NetworkViewManager(final CyApplicationManager appMgr,
							  final CyNetworkViewManager netViewMgr,
							  final RenderingEngineManager renderingEngineManager,
							  final CyProperty<Properties> cyProps,
							  final CyHelpBroker help,
							  final VisualMappingManager vmm,
							  final CyNetworkTableManager netTblMgr,
							  final CyColumnIdentifierFactory colIdfFactory) {
		if (appMgr == null)
			throw new NullPointerException("CyApplicationManager is null.");
		if (netViewMgr == null)
			throw new NullPointerException("CyNetworkViewManager is null.");
		if (netTblMgr == null)
			throw new NullPointerException("CyNetworkTableManager is null.");
		if (colIdfFactory == null)
			throw new NullPointerException("CyColumnIdentifierFactory is null.");
		
		this.renderingEngineMgr = renderingEngineManager;

		this.netViewMgr = netViewMgr;
		this.appMgr = appMgr;
		this.props = cyProps.getProperties();
		this.vmm = vmm;
		this.netTblMgr = netTblMgr;
		this.colIdfFactory = colIdfFactory;

		this.desktopPane = new JDesktopPane();

		// add Help hooks
		help.getHelpBroker().enableHelp(desktopPane, "network-view-manager", null);

		presentationContainerMap = new WeakHashMap<CyNetworkView, JInternalFrame>();
		presentationMap = new WeakHashMap<CyNetworkView, RenderingEngine<CyNetwork>>();
		iFrameMap = new WeakHashMap<JInternalFrame, CyNetworkView>();
		frameListeners = new HashMap<JInternalFrame, InternalFrameListener>();
		viewUpdateRequired = new HashSet<CyNetworkView>();
		mappedValuesMap = new HashMap<CyColumnIdentifier, Map<MappedVisualPropertyValueInfo, Set<View<? extends CyIdentifiable>>>>();
	}

	/**
	 * Desktop for JInternalFrames which contains actual network presentations.
	 */
	public JDesktopPane getDesktopPane() {
		return desktopPane;
	}

	/**
	 * Given a CyNetworkView, returns the internal frame.
	 * 
	 * @param view
	 *            CyNetworkView
	 * @return JInternalFrame
	 * @throws IllegalArgumentException
	 */
	public JInternalFrame getInternalFrame(CyNetworkView view) throws IllegalArgumentException {
		if (view == null) {
			throw new IllegalArgumentException("NetworkViewManager.getInternalFrame(), argument is null");
		}

		return presentationContainerMap.get(view);
	}
	
	public CyNetworkView getNetworkView(JInternalFrame frame) throws IllegalArgumentException {
		if (frame == null) {
			throw new IllegalArgumentException("NetworkViewManager.getNetworkView(), argument is null");
		}
		
		return iFrameMap.get(frame);
	}

	/**
	 * View switched
	 */
	@Override
	public void internalFrameActivated(InternalFrameEvent e) {
		final JInternalFrame frame = e.getInternalFrame();
		
		if (frame.isClosed())
			return;
		
		final CyNetworkView targetView = iFrameMap.get(frame);
		
		if (targetView != null) {
			final RenderingEngine<CyNetwork> currentEngine = appMgr.getCurrentRenderingEngine();
			
			if (netViewMgr.getNetworkViewSet().contains(targetView)) {
				if (!targetView.equals(appMgr.getCurrentNetworkView()))
					appMgr.setCurrentNetworkView(targetView);
	
				if (currentEngine == null || currentEngine.getViewModel() != targetView)
					appMgr.setCurrentRenderingEngine(presentationMap.get(targetView));
				
				if (viewUpdateRequired.contains(targetView)) {
					viewUpdateRequired.remove(targetView);
					final VisualStyle style = vmm.getVisualStyle(targetView);
					style.apply(targetView);
					targetView.updateView();
				}
			}
		}
	}

	/**
	 * Fire Events when a Managed Network View gets the Focus.
	 */
	@Override
	public void internalFrameOpened(InternalFrameEvent e) {
		internalFrameActivated(e);
	}

	// // Event Handlers ////
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		final CyNetworkView view = e.getNetworkView();
		// Do not use invokeLater() here. It cause all kinds of threading problem.
		setFocus(view);
	}

	@Override
	public void handleEvent(SetCurrentNetworkEvent e) {
		final CyNetwork net = e.getNetwork();
		CyNetworkView view = null;
		
		if (net != null) {
			final Collection<CyNetworkView> views = netViewMgr.getNetworkViews(net);
			
			if (!views.isEmpty())
				view = views.iterator().next();
		}
		
		// Do not use invokeLater() here. It cause all kinds of threading problem.
		setFocus(view);
	}

	@Override
	public void handleEvent(NetworkViewAboutToBeDestroyedEvent nvde) {
		logger.info("Network view destroyed: " + nvde.getNetworkView());
		final CyNetworkView view = nvde.getNetworkView();
		
		removeView(view);
	}

	/**
	 * Adding new network view model to this manager. Then, render presentation.
	 */
	@Override
	public void handleEvent(final NetworkViewAddedEvent nvae) {
		logger.debug("\n\n\nView Manager got Network view added event.  Adding view to manager: NetworkViewManager: View ID = "
				+ nvae.getNetworkView().getSUID() + "\n\n\n");

		final String viewThresholdString = props.getProperty(VIEW_THRESHOLD);
		int viewThreshold;
		
		try {
			viewThreshold = Integer.parseInt(viewThresholdString);
		} catch (Exception e) {
			viewThreshold = DEF_VIEW_THRESHOLD;
			logger.warn("Could not parse view threshold property.  Use default value: " + DEF_VIEW_THRESHOLD);
		}

		final CyNetworkView networkView = nvae.getNetworkView();
		final CyNetwork model = networkView.getModel();
		final int graphObjectCount = model.getNodeCount() + model.getEdgeCount();
		
		// Render only when graph size is smaller than threshold.
		if (graphObjectCount > viewThreshold) {
			logger.info("Network is too big to visualize.  This may take very long time to render. " +
					"(Current View Threshold = " + viewThreshold + ")");
			// TODO: Should we cancel visualization?
		}
		
		render(networkView);
	}

	private final void removeView(final CyNetworkView view) {
		try {
			JInternalFrame frame = presentationContainerMap.get(view);
			
			if (frame != null) {
				RenderingEngine<CyNetwork> removed = presentationMap.remove(view);
				logger.debug("Removing rendering engine: " + removed);
				
				viewUpdateRequired.remove(frame);
				iFrameMap.remove(frame);
				
				disposeFrame(frame);
				
				renderingEngineMgr.removeRenderingEngine(removed);
			}
		} catch (Exception e) {
			logger.error("Network View unable to be killed", e);
		}

		synchronized (presentationContainerMap) {
			presentationContainerMap.remove(view);
		}
		
		logger.debug("Network View Model removed.");
	}

	private void disposeFrame(final JInternalFrame frame) throws PropertyVetoException {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					try {
						disposeFrame(frame);
					} catch (PropertyVetoException e) {
						logger.error("Network View unable to be killed", e);
					}
				}
			});
			return;
		}
		frame.getRootPane().getLayeredPane().removeAll();
		frame.getRootPane().getContentPane().removeAll();
		frame.setClosed(true);
		
		frame.removeInternalFrameListener(this);
		InternalFrameListener frameListener = frameListeners.remove(frame);
		if (frameListener != null)
			frame.removeInternalFrameListener(frameListener);
		
		frame.dispose();
	}

	/**
	 * Create a visualization container and add presentation to it.
	 */
	private final void render(final CyNetworkView view) {
		// If already registered in this manager, do not render.
		if (presentationContainerMap.containsKey(view))
			return;

		// Create a new InternalFrame and put the CyNetworkView Component into it
		final String title = getTitle(view);
		final JInternalFrame iframe = new JInternalFrame(title, true, true, true, true);
		
		// This is for force move title bar to the desktop if it's out of range.
		iframe.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				final Point originalPoint = iframe.getLocation();
				if (originalPoint.y < 0)
					iframe.setLocation(originalPoint.x, 0);
			}
		});

		final InternalFrameAdapter frameListener = new InternalFrameAdapter() {
			public void internalFrameClosing(InternalFrameEvent e) {
				if (netViewMgr.getNetworkViewSet().contains(view))
					netViewMgr.destroyNetworkView(view);

				// See bug #1178 (item #3)
				KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
			}
		};
		
		iframe.addInternalFrameListener(frameListener);
		frameListeners.put(iframe, frameListener);
		
		desktopPane.add(iframe);
		
		synchronized (presentationContainerMap) {
			presentationContainerMap.put(view, iframe);
		}
		
		iFrameMap.put(iframe, view);

		final long start = System.currentTimeMillis();
		logger.debug("Rendering start: view model = " + view.getSUID());
		NetworkViewRenderer renderer = appMgr.getCurrentNetworkViewRenderer();
		RenderingEngineFactory<CyNetwork> engineFactory = renderer.getRenderingEngineFactory(NetworkViewRenderer.DEFAULT_CONTEXT);
		final RenderingEngine<CyNetwork> renderingEngine = engineFactory.createRenderingEngine(iframe, view);
		renderingEngineMgr.addRenderingEngine(renderingEngine);
		
		logger.debug("Rendering finished in " + (System.currentTimeMillis() - start) + " m sec.");
		presentationMap.put(view, renderingEngine);

		iframe.pack();

		// create cascade iframe
		int x = 0;
		int y = 0;
		JInternalFrame refFrame = null;
		JInternalFrame[] allFrames = desktopPane.getAllFrames();

		// frame Location
		if (allFrames.length > 1)
			refFrame = allFrames[0];

		if (refFrame != null) {
			x = refFrame.getLocation().x + 20;
			y = refFrame.getLocation().y + 20;
		}

		if (x > (desktopPane.getWidth() - MINIMUM_WIN_WIDTH))
			x = desktopPane.getWidth() - MINIMUM_WIN_WIDTH;
		if (y > (desktopPane.getHeight() - MINIMUM_WIN_HEIGHT))
			y = desktopPane.getHeight() - MINIMUM_WIN_HEIGHT;
		if (x < 0)
			x = 0;
		if (y < 0)
			y = 0;

		iframe.setLocation(x, y);

		// maximize the frame if the specified property is set

		final String max = props.getProperty("maximizeViewOnCreate");

		if ((max != null) && Boolean.parseBoolean(max)) {
			try {
				iframe.setMaximum(true);
			} catch (PropertyVetoException pve) {
				logger.warn("Could not maximize frame.", pve);
			}
		} else {
			int w = view.getVisualProperty(BasicVisualLexicon.NETWORK_WIDTH).intValue();
			int h = view.getVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT).intValue();
			boolean resizable = !view.isValueLocked(BasicVisualLexicon.NETWORK_WIDTH) &&
					!view.isValueLocked(BasicVisualLexicon.NETWORK_HEIGHT);
			updateNetworkFrameSize(view, w, h, resizable);
		}

		// Display it and add listeners
		iframe.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				view.setVisualProperty(BasicVisualLexicon.NETWORK_WIDTH, (double)iframe.getContentPane().getWidth());
				view.setVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT, (double)iframe.getContentPane().getHeight());
			}
		});
		iframe.addInternalFrameListener(this);
		iframe.setVisible(true);
	}
	
	private String getTitle(final CyNetworkView view) {
		String title = view.getVisualProperty(BasicVisualLexicon.NETWORK_TITLE);
		
		if (title == null || title.trim().isEmpty())
			title = view.getModel().getRow(view.getModel()).get(CyNetwork.NAME, String.class);
		
		return title;
	}
	
	private void updateNetworkFrameSize(final CyNetworkView view, int width, int height, boolean resizable) {
		final JInternalFrame frame = presentationContainerMap.get(view);

		if (frame == null)
			return;

		if (width > 0 && height > 0) {
			if(width != frame.getContentPane().getWidth() && 
				height != frame.getContentPane().getHeight()) {
				frame.getContentPane().setPreferredSize(new Dimension(width, height));
				frame.pack();
			}
		}
		
		frame.setResizable(resizable);
		frame.setMaximizable(resizable);
	}
	
	private void updateNetworkFrameTitle(final CyNetworkView view, final String title) {
		if (title != null && !title.trim().isEmpty()) {
			final JInternalFrame frame = presentationContainerMap.get(view);
	
			if (frame != null)
				frame.setTitle(title);
		}
	}
	
	private void setFocus(final CyNetworkView targetView) {
		final CyNetworkView curView = getSelectedNetworkView();
		
		if ((curView == null && targetView == null) || (curView != null && curView.equals(targetView))) {
			logger.debug("Same as current focus.  No need to update focus view model: " + targetView);
			return;
		}

		// Reset focus on frames
		for (JInternalFrame f : presentationContainerMap.values()) {
			try {
				f.setSelected(false);
			} catch (PropertyVetoException pve) {
				logger.error("Couldn't reset focus for internal frames.", pve);
			}
		}

		// Set focus
		if (targetView != null) {
			final JInternalFrame curr = presentationContainerMap.get(targetView);
			
			if (curr != null) {
				try {
					logger.debug("Selecting JInternalFrame of: " + targetView);
	
					curr.setIcon(false);
					curr.show();
					// fires internalFrameActivated
					curr.setSelected(true);
	
				} catch (Exception ex) {
					logger.error("Could not update focus: ", ex);
				}
			} else {
				logger.debug("Frame was not found. Need to create new frame for presentation.");
			}
		}
	}

	@Override
	public void handleEvent(final ColumnDeletedEvent e) {
		if (loadingSession || iFrameMap.isEmpty())
			return;
		
		// Is this column from a network table?
		final CyTable tbl = e.getSource();
		final CyNetwork net = netTblMgr.getNetworkForTable(tbl);
		
		// And if there is no related view, nothing needs to be done
		if ( net != null && netViewMgr.viewExists(net) && 
				(tbl.equals(net.getDefaultNodeTable()) || tbl.equals(net.getDefaultEdgeTable())) ) {
			final Collection<CyNetworkView> networkViews = netViewMgr.getNetworkViews(net);
			final boolean lockedValuesApplyed = reapplyLockedValues(e.getColumnName(), networkViews);
			
			final Set<VisualStyle> styles = findStylesWithMappedColumn(e.getColumnName());
			final Set<CyNetworkView> viewsToUpdate = findNetworkViewsWithStyles(styles);
			
			if (lockedValuesApplyed)
				viewsToUpdate.addAll(networkViews);
			
			for (final CyNetworkView view : viewsToUpdate)
				updateView(view, null);
		}
	}
	
	@Override
	public void handleEvent(final RowsSetEvent e) {
		final CyTable tbl = e.getSource();
		
		// Update Network View Title
		final Collection<RowSetRecord> nameRecords = e.getColumnRecords(CyNetwork.NAME);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				updateNetworkViewTitle(nameRecords, tbl);
			}
		});
		
		if (loadingSession || iFrameMap.isEmpty())
			return;
		
		final CyNetwork net = netTblMgr.getNetworkForTable(tbl);
		
		// Is this column from a network table?
		// And if there is no related view, nothing needs to be done
		if ( net != null && netViewMgr.viewExists(net) && 
				(tbl.equals(net.getDefaultNodeTable()) || tbl.equals(net.getDefaultEdgeTable())) ) {
			// Reapply locked values that map to changed columns
			for (final RowSetRecord record : e.getPayloadCollection()) {
				final String columnName = record.getColumn();
				
				final Collection<CyNetworkView> networkViews = netViewMgr.getNetworkViews(net);
				final boolean lockedValuesApplyed = reapplyLockedValues(columnName, networkViews);
				
				if (lockedValuesApplyed) {
					for (final CyNetworkView view : networkViews)
						updateView(view, null);
				}
			}
		}
	}
	
	private final void updateNetworkViewTitle(final Collection<RowSetRecord> records, final CyTable source) {
		for (final RowSetRecord record : records) {
			if (CyNetwork.NAME.equals(record.getColumn())) {
				// assume payload collection is for same column
				synchronized (iFrameMap) {
					for (final JInternalFrame targetIF : iFrameMap.keySet()) {
						final CyNetworkView view = iFrameMap.get(targetIF);
						final CyNetwork net = view.getModel();

						if (net.getDefaultNetworkTable() == source) {
							final String title = record.getRow().get(CyNetwork.NAME, String.class);
							// We should guarantee this visual property is up to date
							view.setVisualProperty(BasicVisualLexicon.NETWORK_TITLE, title);

							// Do not update the title with the new network name
							// if this visual property is locked
							if (!view.isValueLocked(BasicVisualLexicon.NETWORK_TITLE))
								targetIF.setTitle(title);

							return; // assuming just one row is set.
						}
					}
				}
			}
		}
	}
	
	private JInternalFrame getSelectedFrame() {
		synchronized (presentationContainerMap) {
			for (JInternalFrame f : presentationContainerMap.values()) {
				if (f.isSelected())
					return f;
			}
		}
		
		return null;
	}
	
	private CyNetworkView getSelectedNetworkView() {
		final JInternalFrame selectedFrame = getSelectedFrame();
		
		return iFrameMap.get(selectedFrame);
	}
	
	public void setUpdateFlag(final CyNetworkView view) {
		this.viewUpdateRequired.add(view);
	}

	@Override
	public void handleEvent(final VisualStyleChangedEvent e) {
		if (loadingSession)
			return;
		
		if (e.getSource() != null && !iFrameMap.isEmpty()) {
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
			final CyNetworkView curView = getSelectedNetworkView();
			
			if (curView != null)
				vmm.setVisualStyle(style, curView);
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
		
		final String title = getTitle(view);
		updateNetworkFrameTitle(view, title);
		
		final int w = view.getVisualProperty(BasicVisualLexicon.NETWORK_WIDTH).intValue();
		final int h = view.getVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT).intValue();
		final boolean resizable = !view.isValueLocked(BasicVisualLexicon.NETWORK_WIDTH) &&
				!view.isValueLocked(BasicVisualLexicon.NETWORK_HEIGHT);
		
		updateNetworkFrameSize(view, w, h, resizable);
	}
	
	@Override
	public void handleEvent(final LockedValuesSetEvent e) {
		final CyNetworkView netView = e.getSource();
		
		// Look for MappableVisualPropertyValue objects, so they can be saved for future reference
		for (final LockedValueSetRecord record : e.getPayloadCollection()) {
			final View<? extends CyIdentifiable> view = record.getView();
			final Object value = record.getValue();
			
			if (value instanceof MappableVisualPropertyValue) {
				final Set<CyColumnIdentifier> columnIds = ((MappableVisualPropertyValue)value).getMappedColumnNames();
				
				if (columnIds == null)
					continue;
				
				final VisualProperty<?> vp = record.getVisualProperty();
				
				for (final CyColumnIdentifier colId : columnIds) {
					Map<MappedVisualPropertyValueInfo, Set<View<? extends CyIdentifiable>>> mvpInfoMap =
							mappedValuesMap.get(colId);
					
					if (mvpInfoMap == null)
						mappedValuesMap.put(colId,
								mvpInfoMap = new HashMap<MappedVisualPropertyValueInfo, Set<View<? extends CyIdentifiable>>>());
					
					final MappedVisualPropertyValueInfo mvpInfo =
							new MappedVisualPropertyValueInfo((MappableVisualPropertyValue)value, vp, netView);
					Set<View<? extends CyIdentifiable>> viewSet = mvpInfoMap.get(mvpInfo);
					
					if (viewSet == null)
						mvpInfoMap.put(mvpInfo, viewSet = new HashSet<View<? extends CyIdentifiable>>());
					
					viewSet.add(view);
				}
			}
		}
		
		// Do NOT update the view is session is being loaded
		if (!loadingSession) {
			updateView(netView, null);
		}
	}

	@Override
	public void handleEvent(final SessionAboutToBeLoadedEvent e) {
		loadingSession = true;
	}
	
	@Override
	public void handleEvent(final SessionLoadCancelledEvent e) {
		loadingSession = false;
	}
	
	@Override
	public void handleEvent(final SessionLoadedEvent e) {
		loadingSession = false;
	}
	
	private Set<VisualStyle> findStylesWithMappedColumn(final String columnName) {
		final Set<VisualStyle> styles = new HashSet<VisualStyle>();
		final RenderingEngine<CyNetwork> renderer = appMgr.getCurrentRenderingEngine();
		
		if (columnName != null && renderer != null) {
			final Set<VisualProperty<?>> properties = renderer.getVisualLexicon().getAllVisualProperties();
			
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
		final Set<CyNetworkView> networkViews = netViewMgr.getNetworkViewSet();
		
		for (final CyNetworkView view: networkViews) {
			if (styles.contains(vmm.getVisualStyle(view)))
				result.add(view);
		}
		
		return result;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean reapplyLockedValues(final String columnName, final Collection<CyNetworkView> networkViews) {
		boolean result = false;
		final CyColumnIdentifier colId = colIdfFactory.createColumnIdentifier(columnName);
		final Map<MappedVisualPropertyValueInfo, Set<View<? extends CyIdentifiable>>> mvpInfoMap =
				mappedValuesMap.get(colId);
		
		if (mvpInfoMap != null) {
			for (final MappedVisualPropertyValueInfo mvpInfo : mvpInfoMap.keySet()) {
				if (networkViews == null || !networkViews.contains(mvpInfo.getNetworkView()))
					continue;
				
				final MappableVisualPropertyValue value = mvpInfo.getValue();
				final VisualProperty vp = mvpInfo.getVisualProperty();
				final Set<View<? extends CyIdentifiable>> viewSet = mvpInfoMap.get(mvpInfo);
				
				for (final View<? extends CyIdentifiable> view : viewSet) {
					if (view.isDirectlyLocked(vp) && value.equals(view.getVisualProperty(vp))) {
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
		
		if (view.equals(appMgr.getCurrentNetworkView())) {
			if (vs == null)
				vs = vmm.getVisualStyle(view);
			
			vs.apply(view);
			view.updateView();
		} else {
			this.viewUpdateRequired.add(view);
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
