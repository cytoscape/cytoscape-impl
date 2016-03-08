package org.cytoscape.internal.view;

import static org.cytoscape.internal.util.ViewUtil.invokeOnEDT;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CyHelpBroker;
import org.cytoscape.internal.util.ViewUtil;
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
import org.cytoscape.property.CyProperty;
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
 * This class mediates the communication between the Network View UI and the rest of Cytoscape.
 */
public class NetworkViewMediator
		implements NetworkViewAddedListener, NetworkViewAboutToBeDestroyedListener, SetCurrentNetworkViewListener,
		RowsSetListener, VisualStyleChangedListener, SetCurrentVisualStyleListener, UpdateNetworkPresentationListener,
		VisualStyleSetListener, SessionAboutToBeLoadedListener, SessionLoadCancelledListener, SessionLoadedListener,
		ColumnDeletedListener, ColumnNameChangedListener, ViewChangedListener {

	private static final String SHOW_VIEW_TOOLBARS_KEY = "showDetachedViewToolBars";
	
	private static final Logger logger = LoggerFactory.getLogger(NetworkViewMediator.class);

	@Deprecated
	private final JDesktopPane desktopPane;
	
	private final NetworkViewMainPanel networkViewMainPanel;

	// Key is MODEL ID
	private final Map<CyNetworkView, RenderingEngine<CyNetwork>> presentationMap;
	private final Set<CyNetworkView> viewUpdateRequired;

	/** columnIdentifier -> { valueInfo -> [views] }*/
	private final Map<CyColumnIdentifier, Map<MappedVisualPropertyValueInfo, Set<View<?>>>> mappedValuesMap;
	
	private volatile boolean loadingSession;

	private final CyServiceRegistrar serviceRegistrar;
	
	public NetworkViewMediator(
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
				
				final CyNetworkViewManager netViewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
				
				if (targetView != null) {
					if (netViewMgr.getNetworkViewSet().contains(targetView)) {
						if (viewUpdateRequired.contains(targetView)) {
							viewUpdateRequired.remove(targetView);
							
							final VisualMappingManager vmm = serviceRegistrar.getService(VisualMappingManager.class);
							final VisualStyle style = vmm.getVisualStyle(targetView);
							style.apply(targetView);
							targetView.updateView();
						}
					}
				}
			}
		});
		
		networkViewGrid.addPropertyChangeListener("networkViews", new PropertyChangeListener() {
			@Override
			@SuppressWarnings("unchecked")
			public void propertyChange(PropertyChangeEvent e) {
				if (loadingSession)
					return;
				
				final Collection<CyNetworkView> oldSet = (Collection<CyNetworkView>) e.getOldValue();
				
				if (oldSet != null && !oldSet.isEmpty()) {
					final Collection<CyNetworkView> newSet = (Collection<CyNetworkView>) e.getNewValue();
					final Collection<CyNetworkView> deletedSet = new HashSet<>(oldSet);
					
					if (newSet != null)
						deletedSet.removeAll(newSet);
					
					final CyNetworkViewManager netViewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
					
					for (CyNetworkView view : deletedSet)
						netViewMgr.destroyNetworkView(view);
					
					if (!deletedSet.isEmpty())
						getNetworkViewMainPanel().getNetworkViewGrid()
								.update(getNetworkViewMainPanel().getNetworkViewGrid().getThumbnailSlider().getValue());
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
	
	public Set<NetworkViewFrame> getAllNetworkViewFrames() {
		return getNetworkViewMainPanel().getAllNetworkViewFrames();
	}
	
	public NetworkViewFrame getNetworkViewFrame(final CyNetworkView view) {
		return getNetworkViewMainPanel().getNetworkViewFrame(view);
	}
	
	public void reattachNetworkView(final CyNetworkView view) {
		getNetworkViewMainPanel().reattachNetworkView(view);
	}
	
	public boolean isViewToolBarsVisible() {
		return ViewUtil.getViewProperty(SHOW_VIEW_TOOLBARS_KEY, "true", serviceRegistrar).equalsIgnoreCase("true");
	}
	
	public void setViewToolBarsVisible(final boolean b) {
		for (NetworkViewFrame frame : getAllNetworkViewFrames())
			frame.setToolBarVisible(b);
		
		final Properties props = (Properties) 
				serviceRegistrar.getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)").getProperties();
		props.setProperty(SHOW_VIEW_TOOLBARS_KEY, "" + b);
	}

	// // Event Handlers ////
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent e) {
		if (loadingSession)
			return;
		
		final CyNetworkView view = e.getNetworkView();
		
		invokeOnEDT(() -> {
			final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
			final RenderingEngine<CyNetwork> currentEngine = appMgr.getCurrentRenderingEngine();
			
			new Thread(() -> {
				// Set current RenderingEngine
				if (view != null) {
					final CyNetworkViewManager netViewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
					
					if (netViewMgr.getNetworkViewSet().contains(view)) {
						if (currentEngine == null || currentEngine.getViewModel() != view)
							appMgr.setCurrentRenderingEngine(presentationMap.get(view));
					}
				} else if (view == null && currentEngine != null) {
					appMgr.setCurrentRenderingEngine(null);
				}
			}).start();
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
		final CyNetworkView netView = e.getSource();
		
		invokeOnEDT(() -> {
			getNetworkViewMainPanel().update(netView);
		});
	}
	
	@Override
	public void handleEvent(final ViewChangedEvent<?> e) {
		final CyNetworkView netView = e.getSource();
		
		// Ask the Views Panel to update the thumbnail for the affected network view
		invokeOnEDT(() -> {
			// If the Grid is not visible, just flag this view as dirty.
			if (getNetworkViewMainPanel().isGridMode()) {
				getNetworkViewMainPanel().updateThumbnail(netView);
			} else {
				getNetworkViewMainPanel().update(netView);
				getNetworkViewMainPanel().setDirtyThumbnail(netView);
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
		
		final CyNetworkView view = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetworkView();
		
		if (view != null) {
			final CyNetworkViewManager netViewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
			
			if (netViewMgr.getNetworkViewSet().contains(view)) {
				final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
				final RenderingEngine<CyNetwork> currentEngine = appMgr.getCurrentRenderingEngine();
				
				if (currentEngine == null || currentEngine.getViewModel() != view)
					appMgr.setCurrentRenderingEngine(presentationMap.get(view));
			}
		}
		
		invokeOnEDT(() -> {
			getNetworkViewMainPanel().setCurrentNetworkView(view);
			
			// Always show the current view in the View Mode when opening older session files (up to version 3.3)
			if (view != null)
				getNetworkViewMainPanel().showViewContainer(view);
			else
				getNetworkViewMainPanel().getNetworkViewGrid()
						.update(getNetworkViewMainPanel().getNetworkViewGrid().getThumbnailSlider().getValue());
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
		final CyNetworkTableManager netTblMgr = serviceRegistrar.getService(CyNetworkTableManager.class);
		final CyNetwork net = netTblMgr.getNetworkForTable(tbl);
		final CyNetworkViewManager netViewMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
		
		// Is this column from a network table?
		// And if there is no related view, nothing needs to be done
		if (net != null && netViewMgr.viewExists(net)) {
			// Update Network View Title
			final Collection<RowSetRecord> nameRecords = e.getColumnRecords(CyNetwork.NAME);
			
			if (!nameRecords.isEmpty())
				updateNetworkViewTitle(nameRecords, tbl);
			
			if (tbl.equals(net.getDefaultNodeTable()) || tbl.equals(net.getDefaultEdgeTable())) {
				final Collection<CyNetworkView> networkViews = netViewMgr.getNetworkViews(net);
				final Set<CyNetworkView> viewsToUpdate = new HashSet<>();
				
				// Update node/edge selection info
				final Collection<RowSetRecord> selectedRecords = e.getColumnRecords(CyNetwork.SELECTED);
				
				if (!selectedRecords.isEmpty())
					viewsToUpdate.addAll(netViewMgr.getNetworkViews(net));
				
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
	}

	private final void removeView(final CyNetworkView view) {
		invokeOnEDT(() -> {
			try {
				getNetworkViewMainPanel().remove(view);
				viewUpdateRequired.remove(view);
				final RenderingEngine<CyNetwork> removed = presentationMap.remove(view);
				
				if (removed != null) {
					new Thread(() -> {
						serviceRegistrar.getService(RenderingEngineManager.class).removeRenderingEngine(removed);
					}).start();
				}
			} catch (Exception e) {
				logger.error("Unable to destroy Network View", e);
			}
		});
	}

	/**
	 * Create a visualization container and add presentation to it.
	 */
	private final void render(final CyNetworkView view) {
		invokeOnEDT(() -> {
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
			
			new Thread(() -> {
				serviceRegistrar.getService(RenderingEngineManager.class).addRenderingEngine(renderingEngine);
			}).start();
		});
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
							if (!view.isValueLocked(BasicVisualLexicon.NETWORK_TITLE)) {
								invokeOnEDT(() -> {
									getNetworkViewMainPanel().update(view);
								});
							}

							return; // assuming just one row is set.
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
		
		if (getNetworkViewMainPanel().isGridMode() || view.equals(getNetworkViewMainPanel().getCurrentNetworkView())) {
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
