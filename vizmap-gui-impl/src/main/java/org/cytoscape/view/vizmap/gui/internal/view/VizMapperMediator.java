package org.cytoscape.view.vizmap.gui.internal.view;

import static org.cytoscape.view.vizmap.gui.internal.util.NotificationNames.CURRENT_NETWORK_VIEW_CHANGED;
import static org.cytoscape.view.vizmap.gui.internal.util.NotificationNames.CURRENT_VISUAL_STYLE_CHANGED;
import static org.cytoscape.view.vizmap.gui.internal.util.NotificationNames.VISUAL_STYLE_ADDED;
import static org.cytoscape.view.vizmap.gui.internal.util.NotificationNames.VISUAL_STYLE_NAME_CHANGED;
import static org.cytoscape.view.vizmap.gui.internal.util.NotificationNames.VISUAL_STYLE_REMOVED;
import static org.cytoscape.view.vizmap.gui.internal.util.NotificationNames.VISUAL_STYLE_SET_CHANGED;
import static org.cytoscape.view.vizmap.gui.internal.util.NotificationNames.VISUAL_STYLE_UPDATED;
import static org.cytoscape.view.vizmap.gui.internal.view.util.ViewUtil.invokeOnEDT;
import static org.cytoscape.view.vizmap.gui.internal.view.util.ViewUtil.invokeOnEDTAndWait;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;

import javax.swing.JMenuItem;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.ColumnNameChangedEvent;
import org.cytoscape.model.events.ColumnNameChangedListener;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.UpdateNetworkPresentationEvent;
import org.cytoscape.view.model.events.UpdateNetworkPresentationListener;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.event.LexiconStateChangedEvent;
import org.cytoscape.view.vizmap.gui.event.LexiconStateChangedListener;
import org.cytoscape.view.vizmap.gui.internal.VizMapperProperty;
import org.cytoscape.view.vizmap.gui.internal.util.ServicePropertiesUtil;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.view.VisualPropertySheetItem.MessageType;
import org.cytoscape.view.vizmap.gui.internal.view.editor.mappingeditor.ContinuousMappingEditorPanel;
import org.cytoscape.view.vizmap.gui.internal.view.editor.mappingeditor.EditorValueRangeTracer;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.undo.UndoSupport;
import org.puremvc.java.multicore.interfaces.INotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

@SuppressWarnings({"unchecked", "serial"})
public class VizMapperMediator extends AbstractVizMapperMediator implements LexiconStateChangedListener, RowsSetListener, 
														   ColumnCreatedListener, ColumnDeletedListener,
														   ColumnNameChangedListener, UpdateNetworkPresentationListener {

	public static final String NAME = "VizMapperMediator";
	
	private static final Class<? extends CyIdentifiable>[] SHEET_TYPES = 
			new Class[] { CyNode.class, CyEdge.class, CyNetwork.class };
	
	private boolean ignoreVisualStyleSelectedEvents;
	
//	private VisualPropertySheetItem<?> curVpSheetItem;
//	private VizMapperProperty<?, ?, ?> curVizMapperProperty;
	private String curRendererId;
	
	private final VizMapperMainPanel vizMapperMainPanel;
	
//	private final Map<String, GenerateDiscreteValuesAction> mappingGenerators;
	private final Map<TaskFactory, JMenuItem> taskFactories;
	private final Map<CyAction, JMenuItem> actions;
	
	/** IDs of property sheet items that were set visible/invisible by the user */
//	private final Map<String, Boolean> userProps;
//	private final Map<Class<? extends CyIdentifiable>, Set<String>> defVisibleProps;
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public VizMapperMediator(final VizMapperMainPanel vizMapperMainPanel,
							 final ServicesUtil servicesUtil,
							 final VizMapPropertyBuilder vizMapPropertyBuilder) {
		super(NAME, vizMapperMainPanel, servicesUtil, vizMapPropertyBuilder, SHEET_TYPES);
		
		this.vizMapperMainPanel = vizMapperMainPanel;
		
//		final Collator collator = Collator.getInstance(Locale.getDefault());
//		mappingGenerators = new TreeMap<>((s1, s2) -> {
//			return collator.compare(s1, s2);
//		});
		
		taskFactories = new HashMap<>();
		actions = new HashMap<>();
//		userProps = new HashMap<>();
//		defVisibleProps = new HashMap<>();
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public final void onRegister() {
		super.onRegister();
		initView();
	}
	
	@Override
	public String[] listNotificationInterests() {
		return new String[]{ VISUAL_STYLE_SET_CHANGED,
							 VISUAL_STYLE_ADDED,
							 VISUAL_STYLE_REMOVED,
							 CURRENT_VISUAL_STYLE_CHANGED,
							 VISUAL_STYLE_UPDATED,
							 CURRENT_NETWORK_VIEW_CHANGED,
							 VISUAL_STYLE_NAME_CHANGED };
	}
	
	@Override
	public void handleNotification(final INotification notification) {
		final String id = notification.getName();
		final Object body = notification.getBody();
		
		switch(id) {
			case VISUAL_STYLE_SET_CHANGED:
				updateVisualStyleList((SortedSet<VisualStyle>) body, true);
				break;
			case VISUAL_STYLE_ADDED:
			case VISUAL_STYLE_REMOVED:
				updateVisualStyleList(vmProxy.getVisualStyles(), false);
				break;
			case CURRENT_VISUAL_STYLE_CHANGED:
				invokeOnEDTAndWait(() -> {
					ignoreVisualStyleSelectedEvents = true;
					try {
						selectCurrentVisualStyle((VisualStyle) body);
					} finally {
						ignoreVisualStyleSelectedEvents = false;
					}
				});
				invokeOnEDT(() -> updateVisualPropertySheets((VisualStyle) body, false));
				break;
			case VISUAL_STYLE_UPDATED:
				if(body != null && body.equals(vmProxy.getCurrentVisualStyle())) {
					updateVisualPropertySheets((VisualStyle) body, false);
				}
				break;
			case CURRENT_NETWORK_VIEW_CHANGED:
				final CyNetworkView view = (CyNetworkView) body;
				final String newRendererId = view != null ? view.getRendererId() : null;
				
				if (view != null && newRendererId != null && !newRendererId.equals(curRendererId)) {
					updateVisualPropertySheets(vmProxy.getVisualStyle(view), false);
					curRendererId = newRendererId;
				} else if (view == null || vmProxy.getVisualStyle(view).equals(vizMapperMainPanel.getSelectedVisualStyle())) {
					// Ignore it, if the selected style is not the current one,
					// because it should change the selection style first and then recreate all the items, anyway.
					updateLockedValues((CyNetworkView) body);
					
					if (body instanceof CyNetworkView) {
						updateMappings(CyNode.class, view.getModel().getDefaultNodeTable());
						updateMappings(CyEdge.class, view.getModel().getDefaultEdgeTable());
					}
					
					updateItemsStatus();
				}
				break;
			case VISUAL_STYLE_NAME_CHANGED:
				vizMapperMainPanel.getStylesBtn().update();
				break;
		}
	}

	@Override
	protected Collection<VisualProperty<?>> getVisualPropertyList(VisualLexicon lexicon) {
		return lexicon.getAllDescendants(BasicVisualLexicon.NETWORK);
	}
	
	@Override
	protected Set<View<? extends CyIdentifiable>> getSelectedViews(Class<?> type) {
		final CyNetworkView curNetView = vmProxy.getCurrentNetworkView();
		if(CyNode.class.equals(type)) {
			return new HashSet<>(vmProxy.getSelectedNodeViews(curNetView));
		} else if(CyEdge.class.equals(type)) {
			return new HashSet<>(vmProxy.getSelectedEdgeViews(curNetView));
		} else {
			return curNetView != null ? Collections.singleton((View<CyNetwork>)curNetView) : Collections.EMPTY_SET;
		}
	}
	
	@Override
	public void handleEvent(final LexiconStateChangedEvent e) {
		// Update Network Views
		final VisualStyle curStyle = vmProxy.getCurrentVisualStyle();
		final Set<CyNetworkView> views = vmProxy.getNetworkViewsWithStyle(curStyle);
		
		for (final CyNetworkView view : views) { // TODO This should be done by NetworkViewMediator only, if possible
			curStyle.apply(view);
			view.updateView();
		}
		
		// Update VP Sheet Items
		invokeOnEDT(() -> updateItemsStatus());
	}
	
	@Override
	public void handleEvent(final RowsSetEvent e) {
		final CyTable tbl = e.getSource();
		
		// Update bypass buttons--check selected nodes and edges of the current view
		final CyNetworkView curNetView = vmProxy.getCurrentNetworkView();
		
		if (curNetView != null && e.containsColumn(CyNetwork.SELECTED)) {
			final CyNetwork curNet = curNetView.getModel();
			
			// We have to get all selected elements again
			if (tbl.equals(curNet.getDefaultEdgeTable()))
				updateLockedValues(vmProxy.getSelectedEdgeViews(curNetView), CyEdge.class);
			else if (tbl.equals(curNet.getDefaultNodeTable()))
				updateLockedValues(vmProxy.getSelectedNodeViews(curNetView), CyNode.class);
			else if (tbl.equals(curNet.getDefaultNetworkTable()))
				updateLockedValues(Collections.singleton((View<CyNetwork>)curNetView), CyNetwork.class);
		}
		
		// Also update mappings
		final CyNetwork curNet = vmProxy.getCurrentNetwork();
		
		if (curNet != null) {
			VisualPropertySheet vpSheet = null;
			
			if (tbl.equals(curNet.getDefaultEdgeTable()))
				vpSheet = vizMapperMainPanel.getVisualPropertySheet(CyEdge.class);
			else if (tbl.equals(curNet.getDefaultNodeTable()))
				vpSheet = vizMapperMainPanel.getVisualPropertySheet(CyNode.class);
			else if (tbl.equals(curNet.getDefaultNetworkTable()))
				vpSheet = vizMapperMainPanel.getVisualPropertySheet(CyNetwork.class);
			
			if (vpSheet != null) {
				final Set<String> columns = e.getColumns();
				
				for (final VisualPropertySheetItem<?> item : vpSheet.getItems()) {
					final VisualMappingFunction<?, ?> mapping = item.getModel().getVisualMappingFunction();
					
					if (mapping != null) {
						for (String columnName : columns) {
							if (mapping.getMappingColumnName().equalsIgnoreCase(columnName)) {
								invokeOnEDT(() -> item.updateMapping());
								break;
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	public void handleEvent(final ColumnDeletedEvent e) {
		onColumnChanged(e.getColumnName(), e.getSource());
	}

	@Override
	public void handleEvent(final ColumnCreatedEvent e) {
		onColumnChanged(e.getColumnName(), e.getSource());
	}
	
	@Override
	public void handleEvent(final ColumnNameChangedEvent e) {
		onColumnChanged(e.getOldColumnName(), e.getSource());
		onColumnChanged(e.getNewColumnName(), e.getSource());
	}

	@Override
	public void handleEvent(final UpdateNetworkPresentationEvent e) {
		final CyNetworkView view = e.getSource();
		
		if (view.equals(vmProxy.getCurrentNetworkView()))
			updateLockedValues(view);
	}
	
	
	@Override
	protected void updateMappingStatus(final VisualPropertySheetItem<?> item) {
		if (!item.isEnabled())
			return;
		
		final CyNetwork net = vmProxy.getCurrentNetwork();
		final Class<? extends CyIdentifiable> targetDataType = item.getModel().getTargetDataType();
		
		if (net != null && targetDataType != CyNetwork.class) {
			final CyTable netTable = targetDataType == CyNode.class ? net.getDefaultNodeTable() : net.getDefaultEdgeTable();
			String msg = null;
			MessageType msgType = null;
			
			if (netTable != null) {
				final VizMapperProperty<VisualProperty<?>, String, VisualMappingFunctionFactory> columnProp =
						vizMapPropertyBuilder.getColumnProperty(item.getPropSheetPnl());
				final String colName = (columnProp != null && columnProp.getValue() != null) ?
						columnProp.getValue().toString() : null;
				
				if (colName != null) {
					final VisualMappingFunction<?, ?> mapping = item.getModel().getVisualMappingFunction();
					Class<?> mapColType = mapping != null ? mapping.getMappingColumnType() : null;
					final CyColumn column = netTable.getColumn(colName);
					Class<?> colType = column != null ? column.getType() : null;
					
					// Ignore "List" type
					if (mapColType == List.class)
						mapColType = String.class;
					if (colType == List.class)
						colType = String.class;
					
					if (column == null || (mapColType != null && !mapColType.isAssignableFrom(colType))) {
						String tableName = netTable != null ? targetDataType.getSimpleName().replace("Cy", "") : null;
						msg = "<html>Visual Mapping cannot be applied to current network:<br>" + tableName +
								" table does not have column <b>\"" + colName + "\"</b>" +
								(mapColType != null ? " (" + mapColType.getSimpleName() + ")" : "") + "</html>";
						msgType = MessageType.WARNING;
					}
				}
			}
			
			final String finalMsg = msg;
			final MessageType finalMsgType = msgType;
			
			invokeOnEDT(() -> item.setMessage(finalMsg, finalMsgType));
		}
	}
	
	protected void updateLockedValues(final CyNetworkView currentView) {
		if (currentView != null) {
			updateLockedValues(Collections.singleton((View<CyNetwork>)currentView), CyNetwork.class);
			updateLockedValues(vmProxy.getSelectedNodeViews(currentView), CyNode.class);
			updateLockedValues(vmProxy.getSelectedEdgeViews(currentView), CyEdge.class);
		} else {
			updateLockedValues(Collections.EMPTY_SET, CyNetwork.class);
		}
	}
	
//	@Override
//	public void handleEvent(final VisualMappingFunctionChangedEvent e) {
//		final VisualMappingFunction<?, ?> vm = e.getSource();
//		final VisualProperty<?> vp = vm.getVisualProperty();
//		final VisualStyle curStyle = vmProxy.getCurrentVisualStyle();
//		
//		// If the source mapping belongs to the current visual style, update the correspondent property sheet item
//		if (vm.equals(curStyle.getVisualMappingFunction(vp))) {
//			final VisualPropertySheet vpSheet = vizMapperMainPanel.getVisualPropertySheet(vp.getTargetDataType());
//			
//			if (vpSheet != null) {
//				final VisualPropertySheetItem<?> vpSheetItem = vpSheet.getItem(vp);
//				
//				if (vpSheetItem != null)
//					invokeOnEDT(() -> vpSheetItem.updateMapping());
//			}
//		}
//	}
//	
//	public VisualPropertySheetItem<?> getCurrentVisualPropertySheetItem() {
//		return curVpSheetItem;
//	}
//	
//	public VisualPropertySheet getSelectedVisualPropertySheet() {
//		return vizMapperMainPanel.getSelectedVisualPropertySheet();
//	}
//	
//	public VizMapperProperty<?, ?, ?> getCurrentVizMapperProperty() {
//		return curVizMapperProperty;
//	}
	
	/**
	 * Custom listener for adding registered VizMapper CyActions to the main menu.
	 */
	public synchronized void onCyActionRegistered(final CyAction action, final Map<?, ?> properties) {
		final String serviceType = ServicePropertiesUtil.getServiceType(properties);
		
		if (serviceType != null && serviceType.toString().startsWith("vizmapUI")) {
			invokeOnEDT(() -> {
				final JMenuItem menuItem = createMenuItem(action, properties);
				
				if (menuItem != null)
					actions.put(action, menuItem);
			});
		}
	}

	/**
	 * Custom listener for removing unregistered VizMapper CyActions from the main and context menus.
	 */
	public synchronized void onCyActionUnregistered(final CyAction action, final Map<?, ?> properties) {
		final JMenuItem menuItem = actions.remove(action);
		
		if (menuItem != null) {
			invokeOnEDT(() -> {
				vizMapperMainPanel.removeOption(menuItem);
				vizMapperMainPanel.removeContextMenuItem(menuItem);
			});
		}
	}
	
	/**
	 * Create menu items for related registered Task Factories.
	 */
	public void onTaskFactoryRegistered(final TaskFactory taskFactory, final Map<?, ?> properties) {
		// First filter the service...
		final String serviceType = ServicePropertiesUtil.getServiceType(properties);
		
		if (serviceType == null || !serviceType.toString().startsWith("vizmapUI"))
			return;

		final String title = ServicePropertiesUtil.getTitle(properties);
		
		if (title == null) {
			logger.error("Cannot create VizMapper menu item for: " + taskFactory + 
					"; \"" + ServiceProperties.TITLE +  "\" metadata is missing from properties: " + properties);
			return;
		}

		// Add new menu to the pull-down
		final HashMap<String, String> config = new HashMap<>();
		config.put(ServiceProperties.TITLE, title.toString());
		
		final AbstractCyAction action = new AbstractCyAction(config, taskFactory) {
			@Override
			public void actionPerformed(final ActionEvent e) {
				new Thread(() -> {
					servicesUtil.get(DialogTaskManager.class).execute(taskFactory.createTaskIterator());
				}).start();
			}
		};
		
		invokeOnEDT(() -> {
			final JMenuItem menuItem = createMenuItem(action, properties);
			
			if (menuItem != null)
				taskFactories.put(taskFactory, menuItem);
		});
	}

	public void onTaskFactoryUnregistered(final TaskFactory taskFactory, final Map<?, ?> properties) {
		final JMenuItem menuItem = taskFactories.remove(taskFactory);
		
		if (menuItem != null) {
			invokeOnEDT(() -> {
				vizMapperMainPanel.removeOption(menuItem);
				vizMapperMainPanel.removeContextMenuItem(menuItem);
			});
		}
	}

//	public void onMappingGeneratorRegistered(final DiscreteMappingGenerator<?> generator, final Map<?, ?> properties) {
//		final String serviceType = ServicePropertiesUtil.getServiceType(properties);
//		
//		if (serviceType == null) {
//			logger.error("Cannot create VizMapper context menu item for: " + generator + 
//					"; \"" + ServicePropertiesUtil.SERVICE_TYPE +  "\" metadata is missing from properties: " + properties);
//			return;
//		}
//
//		// This is a menu item for Main Command Button.
//		final String title = ServicePropertiesUtil.getTitle(properties);;
//		
//		if (title == null) {
//			logger.error("Cannot create VizMapper context menu item for: " + generator + 
//					"; \"" + ServiceProperties.TITLE +  "\" metadata is missing from properties: " + properties);
//			return;
//		}
//		
//		// Add new menu to the pull-down
//		final GenerateDiscreteValuesAction action = new GenerateDiscreteValuesAction(title.toString(), generator, servicesUtil);
//		vizMapperMainPanel.getContextMenu().addPopupMenuListener(action);
//		
//		// Concatenate the data type with the title when setting the map key, so the generators
//		// can be sorted first by data type and then by title.
//		mappingGenerators.put(generator.getDataType().getSimpleName() + "::" + title.toString(), action);
//	}
//
//	public void onMappingGeneratorUnregistered(final DiscreteMappingGenerator<?> generator, final Map<?, ?> properties) {
//		final Iterator<Entry<String, GenerateDiscreteValuesAction>> iter = mappingGenerators.entrySet().iterator();
//		
//		while (iter.hasNext()) {
//			final Entry<String, GenerateDiscreteValuesAction> entry = iter.next();
//			final GenerateDiscreteValuesAction action = entry.getValue();
//			
//			if (action.getGenerator().equals(generator)) {
//				vizMapperMainPanel.getContextMenu().removePopupMenuListener(action);
//				iter.remove();
//				break;
//			}
//		}
//	}
	
	// ==[ PRIVATE METHODS ]============================================================================================

//	private void updateDefaultProps() {
//		defVisibleProps.clear();
//		defVisibleProps.put(CyNode.class, propsProxy.getDefaultVisualProperties(CyNode.class));
//		defVisibleProps.put(CyEdge.class, propsProxy.getDefaultVisualProperties(CyEdge.class));
//		defVisibleProps.put(CyNetwork.class, propsProxy.getDefaultVisualProperties(CyNetwork.class));
//	}
	
	private void initView() {
		servicesUtil.registerAllServices(vizMapperMainPanel, new Properties());
		addViewListeners();
	}
	
	private void addViewListeners() {
		// Switching the current Visual Style
		var stylesBtn = vizMapperMainPanel.getStylesBtn();
		stylesBtn.addPropertyChangeListener("selectedStyle", evt -> onSelectedVisualStyleChanged(evt));
	}
	
//	private void addViewListeners(final VisualPropertySheet vpSheet) {
//		for (var vpSheetItem : vpSheet.getItems())
//			addViewListeners(vpSheet, vpSheetItem);
//	}
//
//	private void addViewListeners(final VisualPropertySheet vpSheet, final VisualPropertySheetItem<?> vpSheetItem) {
//		if (vpSheetItem.getModel().getVisualPropertyDependency() == null) {
//			// It's a regular VisualProperty Editor...
//			
//			// Default value button clicked
//			vpSheetItem.getDefaultBtn().addActionListener(evt -> openDefaultValueEditor(evt, vpSheetItem));
//			
//			// Default value button right-clicked
//			vpSheetItem.getDefaultBtn().addMouseListener(new MouseAdapter() {
//				@Override
//				public void mousePressed(final MouseEvent e) {
//					maybeShowContextMenu(e);
//				}
//				@Override
//				public void mouseReleased(final MouseEvent e) {
//					maybeShowContextMenu(e);
//				}
//				private void maybeShowContextMenu(final MouseEvent e) {
//					if (e.isPopupTrigger()) {
//						final JPopupMenu contextMenu = new JPopupMenu();
//						contextMenu.add(new JMenuItem(new AbstractAction("Reset Default Value") {
//							@Override
//							public void actionPerformed(final ActionEvent e) {
//								vpSheetItem.getModel().resetDefaultValue();
//							}
//						}));
//						showContextMenu(contextMenu, e);
//					}
//				}
//			});
//			
//			// Bypass button clicked
//			if (vpSheetItem.getModel().isLockedValueAllowed()) {
//				// Create context menu
//				final JPopupMenu bypassMenu = new JPopupMenu();
//				final JMenuItem removeBypassMenuItem;
//				
//				bypassMenu.add(new JMenuItem(new AbstractAction("Set Bypass...") {
//					@Override
//					public void actionPerformed(final ActionEvent e) {
//						openLockedValueEditor(e, vpSheetItem);
//					}
//				}));
//				bypassMenu.add(removeBypassMenuItem = new JMenuItem(new AbstractAction("Remove Bypass") {
//					@Override
//					public void actionPerformed(final ActionEvent e) {
//						removeLockedValue(e, vpSheetItem);
//					}
//				}));
//				
//				// Right-clicked
//				vpSheetItem.getBypassBtn().addMouseListener(new MouseAdapter() {
//					@Override
//					public void mousePressed(final MouseEvent e) {
//						maybeShowContextMenu(e);
//					}
//					@Override
//					public void mouseReleased(final MouseEvent e) {
//						maybeShowContextMenu(e);
//					}
//					private void maybeShowContextMenu(final MouseEvent e) {
//						if (vpSheetItem.getBypassBtn().isEnabled() && e.isPopupTrigger()) {
//							final LockedValueState state = vpSheetItem.getModel().getLockedValueState();
//							removeBypassMenuItem.setEnabled(state != LockedValueState.ENABLED_NOT_SET);
//							showContextMenu(bypassMenu, e);
//						}
//					}
//				});
//				
//				// Left-clicked
//				vpSheetItem.getBypassBtn().addActionListener(evt -> {
//					final LockedValueState state = vpSheetItem.getModel().getLockedValueState();
//					final JButton btn = vpSheetItem.getBypassBtn();
//					
//					if (state == LockedValueState.ENABLED_NOT_SET) {
//						// There is only one option to execute, so do it now, rather than showing the popup menu
//						openLockedValueEditor(evt, vpSheetItem);
//					} else {
//						bypassMenu.show(btn, 0, btn.getHeight());
//						bypassMenu.requestFocusInWindow();
//					}
//				});
//			}
//			
//			// Right-click
//			final ContextMenuMouseListener cmMouseListener = new ContextMenuMouseListener(vpSheet, vpSheetItem);
//			vpSheetItem.addMouseListener(cmMouseListener);
//			
//			if (vpSheetItem.getModel().isVisualMappingAllowed()) {
//				vpSheetItem.getPropSheetPnl().getTable().addMouseListener(cmMouseListener);
//				vpSheetItem.getRemoveMappingBtn().addActionListener(evt -> removeVisualMapping(vpSheetItem));
//				vpSheetItem.getPropSheetTbl().addPropertyChangeListener("editingVizMapperProperty", evt -> {
//					curVpSheetItem = vpSheetItem; // Save the current editor (the one the user is interacting with)
//					curVizMapperProperty = (VizMapperProperty<?, ?, ?>) evt.getNewValue();
//					
//					final VizMapperProperty<String, VisualMappingFunctionFactory, VisualMappingFunction<?, ?>> mappingTypeProperty = 
//							vizMapPropertyBuilder.getMappingTypeProperty(vpSheetItem.getPropSheetPnl());
//					final VisualMappingFunctionFactory factory = (VisualMappingFunctionFactory) mappingTypeProperty.getValue();
//					attrProxy.setCurrentMappingType(factory != null ? factory.getMappingFunctionType() : null);
//					
//					final VizMapperProperty<VisualProperty<?>, String, VisualMappingFunctionFactory> columnProp = 
//							vizMapPropertyBuilder.getColumnProperty(vpSheetItem.getPropSheetPnl());
//					final Object columnValue = columnProp.getValue();
//					mappingFactoryProxy.setCurrentColumnName(columnValue != null ? columnValue.toString() : null);
//					mappingFactoryProxy.setCurrentTargetDataType(vpSheet.getModel().getTargetDataType());
//				});
//			}
//		} else {
//			// It's a Dependency Editor...
//			vpSheetItem.getDependencyCkb().addItemListener(evt -> onDependencySelectionChanged(evt, vpSheetItem));
//		}
//		
//		// Save sheet items that were explicitly shown/hidden by the user,
//		// so his preferences can be respected when the current style changes
//		vpSheetItem.addComponentListener(new ComponentAdapter() {
//			@Override
//			public void componentShown(final ComponentEvent e) {
//				userProps.put(vpSheetItem.getModel().getId(), Boolean.TRUE);
//			}
//			@Override
//			public void componentHidden(final ComponentEvent e) {
//				userProps.put(vpSheetItem.getModel().getId(), Boolean.FALSE);
//			}
//		});
//	}
//
//	protected void removeVisualMapping(final VisualPropertySheetItem<?> vpSheetItem) {
//		final VisualMappingFunction<?, ?> vm = vpSheetItem.getModel().getVisualMappingFunction();
//		
//		if (vm != null)
//			sendNotification(NotificationNames.REMOVE_VISUAL_MAPPINGS, Collections.singleton(vm));
//	}

	private void updateVisualStyleList(final SortedSet<VisualStyle> styles, final boolean resetDefaultVisibleItems) {
		attrProxy.setCurrentMappingType(null);
		mappingFactoryProxy.setCurrentColumnName(null);
		
		invokeOnEDT(() -> {
			ignoreVisualStyleSelectedEvents = true;
			final VisualStyle vs = vmProxy.getCurrentVisualStyle();
			vizMapperMainPanel.updateVisualStyles(styles, vs);
			selectCurrentVisualStyle(vs);
			updateVisualPropertySheets(vs, resetDefaultVisibleItems);
			ignoreVisualStyleSelectedEvents = false;
		});
	}
	
	private void selectCurrentVisualStyle(VisualStyle vs) {
		invokeOnEDT(() -> {
			var selectedVs = vizMapperMainPanel.getSelectedVisualStyle();

			// Switching styles.  Need to reset the range tracer
			ContinuousMappingEditorPanel.setTracer(new EditorValueRangeTracer(servicesUtil));
			
			if (vs != null && !vs.equals(selectedVs))
				vizMapperMainPanel.setSelectedVisualStyle(vs);
		});
	}
	
	
	
	private boolean shouldRebuildVisualPropertySheets(final VisualStyle vs) {
		final VisualPropertySheet curNetSheet = vizMapperMainPanel.getVisualPropertySheet(CyNetwork.class);
		final VisualPropertySheetModel curModel = curNetSheet != null ? curNetSheet.getModel() : null;
		final VisualStyle curStyle = curModel != null ? curModel.getVisualStyle() : null;
		final CyNetworkView curNetView = vmProxy.getCurrentNetworkView();
		final String newRendererId = curNetView != null ? curNetView.getRendererId() : "";
		
		// If a different style or renderer, rebuild all property sheets
		boolean rebuild = !vs.equals(curStyle) || !newRendererId.equals(curRendererId);
		
		if (curNetView != null)
			curRendererId = curNetView.getRendererId();
		
		return rebuild;
	}
	
	protected void updateVisualPropertySheets(final VisualStyle vs, final boolean resetDefaultVisibleItems) {
		boolean rebuild = shouldRebuildVisualPropertySheets(vs);
		super.updateVisualPropertySheets(vs, resetDefaultVisibleItems, rebuild);
	}
	
//	@SuppressWarnings("rawtypes")
//	private void updateVisualPropertySheets(final VisualStyle vs, final boolean resetDefaultVisibleItems) {
//		if (vs == null)
//			return;
//		
//		final VisualPropertySheet curNetSheet = vizMapperMainPanel.getVisualPropertySheet(CyNetwork.class);
//		final VisualPropertySheetModel curModel = curNetSheet != null ? curNetSheet.getModel() : null;
//		final VisualStyle curStyle = curModel != null ? curModel.getVisualStyle() : null;
//		final CyNetworkView curNetView = vmProxy.getCurrentNetworkView();
//		final String newRendererId = curNetView != null ? curNetView.getRendererId() : "";
//		
//		// If a different style or renderer, rebuild all property sheets
//		boolean rebuild = !vs.equals(curStyle) || !newRendererId.equals(curRendererId);
//		
//		if (curNetView != null)
//			curRendererId = curNetView.getRendererId();
//
//		if (!rebuild) {
//			// Also check if dependencies have changed
//			final Map<String, VisualPropertyDependency<?>> map = new HashMap<>();
//			final Set<VisualPropertyDependency<?>> dependencies = vs.getAllVisualPropertyDependencies();
//			
//			for (final VisualPropertyDependency<?> dep : dependencies) {
//				final Class<? extends CyIdentifiable> type = dep.getParentVisualProperty().getTargetDataType();
//				final VisualPropertySheet sheet = vizMapperMainPanel.getVisualPropertySheet(type);
//				
//				if (sheet.getItem(dep) == null) {
//					// There's a new dependency!
//					rebuild = true;
//					break;
//				}
//				
//				map.put(dep.getIdString(), dep);
//			}
//			
//			if (!rebuild) {
//				final Set<VisualPropertySheet> vpSheets = vizMapperMainPanel.getVisualPropertySheets();
//				
//				for (final VisualPropertySheet sheet : vpSheets) {
//					for (final VisualPropertySheetItem<?> item : sheet.getItems()) {
//						final VisualPropertyDependency<?> dep = item.getModel().getVisualPropertyDependency();
//						
//						if (dep != null && !map.containsKey(dep.getIdString())) {
//							// This dependency has been removed from the Visual Style!
//							rebuild = true;
//							break;
//						}
//					}
//				}
//			}
//		}
//		
//		if (rebuild) {
//			createVisualPropertySheets(resetDefaultVisibleItems);
//		} else {
//			// Just update the current Visual Property sheets
//			final Set<VisualPropertySheet> vpSheets = vizMapperMainPanel.getVisualPropertySheets();
//			
//			for (final VisualPropertySheet sheet : vpSheets) {
//				for (final VisualPropertySheetItem<?> item : sheet.getItems()) {
//					// Update values
//					final VisualPropertySheetItemModel model = item.getModel();
//					model.update(vizMapperMainPanel.getRenderingEngine());
//					
//					if (model.getVisualPropertyDependency() != null)
//						item.update();
//					
//					// Also make sure items with mappings are visible
//					if (model.getVisualMappingFunction() != null)
//						item.setVisible(true);
//				}
//			}
//			
//			if (resetDefaultVisibleItems)
//				updateVisibleItems(resetDefaultVisibleItems);
//		}
//	}
//	
//	private void createVisualPropertySheets(final boolean resetDefaultVisibleItems) {
//		final VisualStyle style = vmProxy.getCurrentVisualStyle();
//		final VisualLexicon lexicon = vmProxy.getCurrentVisualLexicon();
//		
//		invokeOnEDT(() -> {
//			final VisualPropertySheet selVpSheet = getSelectedVisualPropertySheet();
//			final Class<? extends CyIdentifiable> selectedTargetDataType = selVpSheet != null ?
//					selVpSheet.getModel().getTargetDataType() : null;
//			
//			for (final Class<? extends CyIdentifiable> type : SHEET_TYPES) {
//				// Create Visual Property Sheet
//				final VisualPropertySheetModel model = new VisualPropertySheetModel(type, style, lexicon);
//				final VisualPropertySheet vpSheet = new VisualPropertySheet(model, servicesUtil);
//				vizMapperMainPanel.addVisualPropertySheet(vpSheet);
//				
//				// Create Visual Property Sheet Items
//				final Set<VisualPropertySheetItem<?>> vpSheetItems = 
//						createVisualPropertySheetItems(vpSheet.getModel().getTargetDataType(), lexicon, style);
//				vpSheet.setItems(vpSheetItems);
//				
//				// Add event listeners to the new components
//				addViewListeners(vpSheet);
//				
//				// Add more menu items to the Properties menu
//				if (vpSheetItems.size() > 1) {
//					vpSheet.getVpsMenu().add(new JSeparator());
//					
//					{
//						final JMenuItem mi = new JMenuItem("Show Default");
//						mi.addActionListener(evt -> showDefaultItems(vpSheet));
//						vpSheet.getVpsMenu().add(mi);
//					}
//					{
//						final JMenuItem mi = new JMenuItem("Show All");
//						mi.addActionListener(evt -> setVisibleItems(vpSheet, true));
//						vpSheet.getVpsMenu().add(mi);
//					}
//					{
//						final JMenuItem mi = new JMenuItem("Hide All");
//						mi.addActionListener(evt -> setVisibleItems(vpSheet, false));
//						vpSheet.getVpsMenu().add(mi);
//					}
//				}
//				
//				vpSheet.getVpsMenu().add(new JSeparator());
//				
//				final JMenuItem mi = new JMenuItem("Make Default");
//				mi.addActionListener(evt -> saveDefaultVisibleItems(vpSheet));
//				vpSheet.getVpsMenu().add(mi);
//			}
//			
//			updateVisibleItems(resetDefaultVisibleItems);
//			updateItemsStatus();
//			
//			// Update panel's width
//			int minWidth = 200;
//			
//			for (final VisualPropertySheet vpSheet : vizMapperMainPanel.getVisualPropertySheets()) {
//				minWidth = Math.max(minWidth, vpSheet.getMinimumSize().width);
//			}
//			
//			vizMapperMainPanel.setPreferredSize(
//					new Dimension(vizMapperMainPanel.getPropertiesPnl().getComponent().getMinimumSize().width + 20,
//								  vizMapperMainPanel.getPreferredSize().height));
//			
//			// Select the same sheet that was selected before
//			final VisualPropertySheet vpSheet = vizMapperMainPanel.getVisualPropertySheet(selectedTargetDataType);
//			vizMapperMainPanel.setSelectedVisualPropertySheet(vpSheet);
//		});
//	}
//	
//	@SuppressWarnings("rawtypes")
//	private Set<VisualPropertySheetItem<?>> createVisualPropertySheetItems(final Class<? extends CyIdentifiable> type,
//			final VisualLexicon lexicon, final VisualStyle style) {
//		final Set<VisualPropertySheetItem<?>> items = new HashSet<>();
//		
//		if (lexicon == null || style == null)
//			return items;
//		
//		final Collection<VisualProperty<?>> vpList = lexicon.getAllDescendants(BasicVisualLexicon.NETWORK);
//		final CyNetworkView curNetView = vmProxy.getCurrentNetworkView();
//		final Set<View<CyNode>> selectedNodeViews = vmProxy.getSelectedNodeViews(curNetView);
//		final Set<View<CyEdge>> selectedEdgeViews = vmProxy.getSelectedEdgeViews(curNetView);
//		final Set<View<CyNetwork>> selectedNetViews = curNetView != null ?
//				Collections.singleton((View<CyNetwork>) curNetView) : Collections.EMPTY_SET;
//		final RenderingEngine<CyNetwork> engine = vizMapperMainPanel.getRenderingEngine();
//		
//		for (final VisualProperty<?> vp : vpList) {
//			if (vp.getTargetDataType() != type || vp instanceof DefaultVisualizableVisualProperty)
//				continue;
//			if (!vmProxy.isSupported(vp))
//				continue;
//			
//			// Create model
//			final VisualPropertySheetItemModel<?> model = new VisualPropertySheetItemModel(vp, style, engine, lexicon);
//			final Set values;
//			
//			if (vp.getTargetDataType() == CyNode.class) {
//				values = getDistinctLockedValues(vp, selectedNodeViews);
//				updateVpInfoLockedState(model, values, selectedNodeViews);
//			} else if (vp.getTargetDataType() == CyEdge.class) {
//				values = getDistinctLockedValues(vp, selectedEdgeViews);
//				updateVpInfoLockedState(model, values, selectedEdgeViews);
//			} else {
//				values = getDistinctLockedValues(vp, selectedNetViews);
//				updateVpInfoLockedState(model, values, selectedNetViews);
//			}
//			
//			// Create View
//			final VisualPropertySheetItem<?> sheetItem = new VisualPropertySheetItem(model, vizMapPropertyBuilder,
//					servicesUtil);
//			items.add(sheetItem);
//			
//			// Add listeners to item and model:
//			if (model.isVisualMappingAllowed()) {
//				sheetItem.getPropSheetPnl().addPropertySheetChangeListener(evt -> {
//					if (evt.getPropertyName().equals("value") && evt.getSource() instanceof VizMapperProperty)
//						updateMappingStatus(sheetItem);
//				});
//			}
//			
//			// Set the updated values to the visual style
//			model.addPropertyChangeListener("defaultValue", evt -> {
//				final VisualStyle vs = model.getVisualStyle();
//				vs.setDefaultValue((VisualProperty)vp, evt.getNewValue());
//			});
//			model.addPropertyChangeListener("visualMappingFunction", evt -> {
//				final VisualStyle vs = model.getVisualStyle();
//				
//				if (evt.getNewValue() == null && vs.getVisualMappingFunction(vp) != null)
//					vs.removeVisualMappingFunction(vp);
//				else if (evt.getNewValue() != null && !evt.getNewValue().equals(vs.getVisualMappingFunction(vp)))
//					vs.addVisualMappingFunction((VisualMappingFunction<?, ?>)evt.getNewValue());
//				
//				updateMappingStatus(sheetItem);
//			});
//		}
//		
//		// Add dependencies
//		final Set<VisualPropertyDependency<?>> dependencies = style.getAllVisualPropertyDependencies();
//		
//		for (final VisualPropertyDependency<?> dep : dependencies) {
//			if (dep.getParentVisualProperty().getTargetDataType() != type)
//				continue;
//			if (!vmProxy.isSupported(dep))
//				continue;
//			
//			final VisualPropertySheetItemModel<?> model = new VisualPropertySheetItemModel(dep, style, engine, lexicon);
//			final VisualPropertySheetItem<?> sheetItem = new VisualPropertySheetItem(model, vizMapPropertyBuilder,
//					servicesUtil);
//			items.add(sheetItem);
//		}
//		
//		return items;
//	}
//	
//	private void updateItemsStatus() {
//		// Children of enabled dependencies must be disabled
//		final Set<VisualProperty<?>> disabled = new HashSet<>();
//		final Map<VisualProperty<?>, String> messages = new HashMap<>();
//		final VisualStyle style = vmProxy.getCurrentVisualStyle();
//		
//		final String infoMsgTemplate = 
//				"<html>To enable this visual property,<br><b>%s</b> the dependency <i><b>%s</b></i></html>";
//		
//		for (final VisualPropertyDependency<?> dep : style.getAllVisualPropertyDependencies()) {
//			final VisualProperty<?> parent = dep.getParentVisualProperty();
//			final Set<VisualProperty<?>> properties = dep.getVisualProperties();
//			
//			if (dep.isDependencyEnabled()) {
//				disabled.addAll(properties);
//				
//				for (final VisualProperty<?> vp : properties)
//					messages.put(vp, String.format(infoMsgTemplate, "uncheck", dep.getDisplayName()));
//			} else {
//				disabled.add(parent);
//				messages.put(parent, String.format(infoMsgTemplate, "check", dep.getDisplayName()));
//			}
//		}
//		
//		for (final VisualPropertySheet vpSheet : vizMapperMainPanel.getVisualPropertySheets()) {
//			final Set<VisualPropertySheetItem<?>> vpSheetItems = vpSheet.getItems();
//			
//			for (final VisualPropertySheetItem<?> item : vpSheetItems) {
//				// First check if this property item must be disabled and show an INFO message
//				String msg = null;
//				MessageType msgType = null;
//				
//				if (msgType == null && item.getModel().getVisualPropertyDependency() == null) {
//					item.setEnabled(!disabled.contains(item.getModel().getVisualProperty()));
//					msg = messages.get(item.getModel().getVisualProperty());
//					msgType = item.isEnabled() ? null : MessageType.INFO;
//				}
//				
//				item.setMessage(msg, msgType);
//				
//				// If item is enabled, check whether or not the mapping is valid for the current network
//				updateMappingStatus(item);
//			}
//		}
//	}
//	
//	private void updateVisibleItems(final boolean reset) {
//		if (reset)
//			userProps.clear();
//		
//		for (final VisualPropertySheet vpSheet : vizMapperMainPanel.getVisualPropertySheets()) {
//			for (final VisualPropertySheetItem<?> item : vpSheet.getItems()) {
//				// Items that are set visible by the user should still be visible when the current style changes.
//				// Items hidden by the user will not be shown again when the current style changes,
//				// unless it has a visual mapping:
//				final Set<String> set = defVisibleProps.get(item.getModel().getTargetDataType());
//				final String vpId = item.getModel().getId();
//				
//				// Start with the default properties,
//				// but keep the ones previously hidden by the user invisible...
//				boolean b = set != null && set.contains(vpId) && !Boolean.FALSE.equals(userProps.get(vpId));
//				// ...but always show properties that have a mapping
//				b = b || item.getModel().getVisualMappingFunction() != null;
//				// ...or that were set visible by the user
//				b = b || Boolean.TRUE.equals(userProps.get(vpId));
//				
//				item.setVisible(b);
//			}
//		}
//	}
//	
//	private void setVisibleItems(VisualPropertySheet vpSheet, boolean visible) {
//		userProps.clear();
//		
//		for (final VisualPropertySheetItem<?> item : vpSheet.getItems())
//			item.setVisible(visible);
//	}
//	
//	private void showDefaultItems(VisualPropertySheet vpSheet) {
//		userProps.clear();
//		
//		for (final VisualPropertySheetItem<?> item : vpSheet.getItems()) {
//			final Set<String> set = defVisibleProps.get(item.getModel().getTargetDataType());
//			final String vpId = item.getModel().getId();
//			
//			// Start with the default properties, of course
//			boolean b = set != null && set.contains(vpId);
//			// ...but still show properties that have a mapping
//			b = b || item.getModel().getVisualMappingFunction() != null;
//			
//			item.setVisible(b);
//		}
//	}
//	
//	private void saveDefaultVisibleItems(final VisualPropertySheet vpSheet) {
//		final Set<String> idSet = new HashSet<>();
//		
//		for (final VisualPropertySheetItem<?> item : vpSheet.getItems()) {
//			if (item.isVisible())
//				idSet.add(item.getModel().getId());
//		}
//		
//		propsProxy.setDefaultVisualProperties(vpSheet.getModel().getTargetDataType(), idSet);
//		updateDefaultProps();
//	}
//	
//	private void updateMappings(final Class<? extends CyIdentifiable> targetDataType, final CyTable table) {
//		if (table != null) {
//			final VisualPropertySheet vpSheet = vizMapperMainPanel.getVisualPropertySheet(targetDataType);
//			
//			if (vpSheet != null) {
//				final Collection<CyColumn> columns = table.getColumns();
//				final HashMap<String, Class<?>> colTypes = new HashMap<>();
//				
//				for (final CyColumn col : columns)
//					colTypes.put(col.getName().toLowerCase(), col.getType());
//					
//				for (final VisualPropertySheetItem<?> item : vpSheet.getItems()) {
//					final VisualMappingFunction<?, ?> mapping = item.getModel().getVisualMappingFunction();
//					
//					// Passthrough mappings don't need to be updated
//					if (mapping instanceof DiscreteMapping || mapping instanceof ContinuousMapping) {
//						final Class<?> colType = colTypes.get(mapping.getMappingColumnName().toLowerCase());
//						
//						if (colType != null && mapping.getMappingColumnType().isAssignableFrom(colType))
//							invokeOnEDT(() -> item.updateMapping());
//					}
//				}
//			}
//		}
//	}
//	
//	private void updateMappingStatus(final VisualPropertySheetItem<?> item) {
//		if (!item.isEnabled())
//			return;
//		
//		final CyNetwork net = vmProxy.getCurrentNetwork();
//		final Class<? extends CyIdentifiable> targetDataType = item.getModel().getTargetDataType();
//		
//		if (net != null && targetDataType != CyNetwork.class) {
//			final CyTable netTable = targetDataType == CyNode.class ?
//					net.getDefaultNodeTable() : net.getDefaultEdgeTable();
//			String msg = null;
//			MessageType msgType = null;
//			
//			if (netTable != null) {
//				final VizMapperProperty<VisualProperty<?>, String, VisualMappingFunctionFactory> columnProp =
//						vizMapPropertyBuilder.getColumnProperty(item.getPropSheetPnl());
//				final String colName = (columnProp != null && columnProp.getValue() != null) ?
//						columnProp.getValue().toString() : null;
//				
//				if (colName != null) {
//					final VisualMappingFunction<?, ?> mapping = item.getModel().getVisualMappingFunction();
//					Class<?> mapColType = mapping != null ? mapping.getMappingColumnType() : null;
//					final CyColumn column = netTable.getColumn(colName);
//					Class<?> colType = column != null ? column.getType() : null;
//					
//					// Ignore "List" type
//					if (mapColType == List.class)
//						mapColType = String.class;
//					if (colType == List.class)
//						colType = String.class;
//					
//					if (column == null || (mapColType != null && !mapColType.isAssignableFrom(colType))) {
//						String tableName = netTable != null ? targetDataType.getSimpleName().replace("Cy", "") : null;
//						msg = "<html>Visual Mapping cannot be applied to current network:<br>" + tableName +
//								" table does not have column <b>\"" + colName + "\"</b>" +
//								(mapColType != null ? " (" + mapColType.getSimpleName() + ")" : "") + "</html>";
//						msgType = MessageType.WARNING;
//					}
//				}
//			}
//			
//			final String finalMsg = msg;
//			final MessageType finalMsgType = msgType;
//			
//			invokeOnEDT(() -> item.setMessage(finalMsg, finalMsgType));
//		}
//	}
//	
//	private void updateLockedValues(final CyNetworkView currentView) {
//		if (currentView != null) {
//			updateLockedValues(Collections.singleton((View<CyNetwork>)currentView), CyNetwork.class);
//			updateLockedValues(vmProxy.getSelectedNodeViews(currentView), CyNode.class);
//			updateLockedValues(vmProxy.getSelectedEdgeViews(currentView), CyEdge.class);
//		} else {
//			updateLockedValues(Collections.EMPTY_SET, CyNetwork.class);
//		}
//	}
//	
//	@SuppressWarnings("rawtypes")
//	private <S extends CyIdentifiable> void updateLockedValues(Set<View<S>> selectedViews, Class<S> targetDataType) {
//		invokeOnEDT(() -> {
//			final Set<VisualPropertySheet> vpSheets = vizMapperMainPanel.getVisualPropertySheets();
//			
//			for (VisualPropertySheet sheet : vpSheets) {
//				final Set<VisualPropertySheetItem<?>> vpItems = sheet.getItems();
//				
//				for (final VisualPropertySheetItem<?> item : vpItems) {
//					final VisualPropertySheetItemModel<?> model = item.getModel();
//					
//					if (model.getTargetDataType() != targetDataType)
//						continue;
//					
//					final Set values = getDistinctLockedValues(model.getVisualProperty(), selectedViews);
//					updateVpInfoLockedState(model, values, selectedViews);
//				}
//			}
//		});
//	}
//	
//	private <T, S extends CyIdentifiable> void updateVpInfoLockedState(final VisualPropertySheetItemModel<T> model,
//			   final Set<T> lockedValues, final Set<View<S>> selectedViews) {
//		T value = null;
//		LockedValueState state = LockedValueState.DISABLED;
//
//		if (lockedValues.size() == 1) {
//			value = lockedValues.iterator().next();
//			state = value == null ? LockedValueState.ENABLED_NOT_SET : LockedValueState.ENABLED_UNIQUE_VALUE;
//		} else if (lockedValues.size() > 1) {
//			state = LockedValueState.ENABLED_MULTIPLE_VALUES;
//		}
//
//		model.setLockedValue(value);
//		model.setLockedValueState(state);
//	}
//	
//	private <T, S extends CyIdentifiable> Set<T> getDistinctLockedValues(final VisualProperty<T> vp,
//			 final Set<View<S>> views) {
//		final Set<T> values = new HashSet<>();
//
//		for (final View<S> view : views) {
//			if (view != null) {
//				if (view.isValueLocked(vp))
//					values.add(view.getVisualProperty(vp));
//				else
//					values.add(null); // To indicate that there is least one view without a locked value
//
//				if (values.size() > 1) // For our current purposes, two values is the max we need
//					break;
//			}
//		}
//
//		return values;
//	}
	
	private JMenuItem createMenuItem(final CyAction action, final Map<?, ?> properties) {
		String title = ServicePropertiesUtil.getTitle(properties);
		
		if (title == null)
			title = action.getName();
			
		if (title == null) {
			logger.error("Cannot create VizMapper menu item for: " + action + 
					"; \"" + ServicePropertiesUtil.TITLE +  "\" metadata is missing from properties: " + properties);
			return null;
		}
		
		final JMenuItem menuItem = new JMenuItem(action);
		menuItem.setText(title);
		
		final double gravity = ServicePropertiesUtil.getGravity(properties);
		final boolean insertSeparatorBefore = ServicePropertiesUtil.getInsertSeparatorBefore(properties);
		final boolean insertSeparatorAfter = ServicePropertiesUtil.getInsertSeparatorAfter(properties);
		final String menuId = ServicePropertiesUtil.getString(properties, ServicePropertiesUtil.MENU_ID, "");
		
		if (menuId.equals(ServicePropertiesUtil.CONTEXT_MENU))
			vizMapperMainPanel.addContextMenuItem(menuItem, gravity, insertSeparatorBefore, insertSeparatorAfter);
		else
			vizMapperMainPanel.addOption(menuItem, gravity, insertSeparatorBefore, insertSeparatorAfter);
		
		return menuItem;
	} 
//	
//	@SuppressWarnings("rawtypes")
//	private void openDefaultValueEditor(final ActionEvent evt, final VisualPropertySheetItem vpSheetItem) {
//		final VisualPropertySheetItemModel model = vpSheetItem.getModel();
//		final VisualProperty vp = model.getVisualProperty();
//
//		final VisualStyle style = vmProxy.getCurrentVisualStyle();
//		final Object oldValue = style.getDefaultValue(vp);
//		Object val = null;
//		
//		try {
//			final EditorManager editorMgr = servicesUtil.get(EditorManager.class);
//			val = editorMgr.showVisualPropertyValueEditor(vizMapperMainPanel, vp, oldValue);
//		} catch (final Exception ex) {
//			logger.error("Error opening Visual Property values editor for: " + vp, ex);
//		}
//
//		final Object newValue = val;
//		
//		if (newValue != null && !newValue.equals(oldValue)) {
//			style.setDefaultValue(vp, newValue);
//			
//			// Undo support
//			final UndoSupport undo = servicesUtil.get(UndoSupport.class);
//			undo.postEdit(new AbstractCyEdit("Set Default Value") {
//				@Override
//				public void undo() {
//					style.setDefaultValue(vp, oldValue);
//				}
//				@Override
//				public void redo() {
//					style.setDefaultValue(vp, newValue);
//				}
//			});
//		}
//	}
//	
//	@SuppressWarnings("rawtypes")
//	private void openLockedValueEditor(final ActionEvent evt, final VisualPropertySheetItem vpSheetItem) {
//		final VisualPropertySheetItemModel model = vpSheetItem.getModel();
//		final VisualProperty vp = model.getVisualProperty();
//		
//		final Object curValue = model.getLockedValue();
//		Object newValue = null;
//		
//		try {
//			final EditorManager editorMgr = servicesUtil.get(EditorManager.class);
//			newValue = editorMgr.showVisualPropertyValueEditor(vizMapperMainPanel, vp, curValue);
//		} catch (Exception ex) {
////			logger.error("Error opening Visual Property values editor for: " + vp, ex);
//		}
//		
//		if (newValue != null && !newValue.equals(curValue)) {
//			final LockedValuesVO vo = new LockedValuesVO((Map)Collections.singletonMap(vp, newValue));
//			sendNotification(NotificationNames.SET_LOCKED_VALUES, vo);
//		}
//	}
//	
//	@SuppressWarnings("rawtypes")
//	private void removeLockedValue(final ActionEvent e, final VisualPropertySheetItem<?> vpSheetItem) {
//		final VisualProperty<?> visualProperty = vpSheetItem.getModel().getVisualProperty();
//		final LockedValuesVO vo = new LockedValuesVO((Set)Collections.singleton(visualProperty));
//		sendNotification(NotificationNames.REMOVE_LOCKED_VALUES, vo);
//	}

	private void onSelectedVisualStyleChanged(final PropertyChangeEvent e) {
		final VisualStyle newStyle = (VisualStyle) e.getNewValue();
		final VisualStyle oldStyle = vmProxy.getCurrentVisualStyle();
		
		if (!ignoreVisualStyleSelectedEvents && newStyle != null && !newStyle.equals(oldStyle)) {
			// Update proxy
			vmProxy.setCurrentVisualStyle(newStyle);
			
			// Undo support
			final UndoSupport undo = servicesUtil.get(UndoSupport.class);
			undo.postEdit(new AbstractCyEdit("Set Current Style") {
				@Override
				public void undo() {
					vmProxy.setCurrentVisualStyle(oldStyle);
				}
				@Override
				public void redo() {
					vmProxy.setCurrentVisualStyle(newStyle);
				}
			});
		}
	}
	
//	private void onDependencySelectionChanged(final ItemEvent e, final VisualPropertySheetItem<?> vpSheetItem) {
//		final boolean selected = e.getStateChange() == ItemEvent.SELECTED;
//		final VisualPropertyDependency<?> dep = vpSheetItem.getModel().getVisualPropertyDependency();
//		dep.setDependency(selected);
//		
//		// Update VP Sheet Items
//		invokeOnEDT(() -> updateItemsStatus());
//	}
	
	private void onColumnChanged(final String colName, final CyTable tbl) {
		final CyNetwork curNet = vmProxy.getCurrentNetwork();
		if (curNet == null) return;
		VisualPropertySheet vpSheet = null;
		
		if (tbl.equals(curNet.getDefaultEdgeTable()))
			vpSheet = vizMapperMainPanel.getVisualPropertySheet(CyEdge.class);
		else if (tbl.equals(curNet.getDefaultNodeTable()))
			vpSheet = vizMapperMainPanel.getVisualPropertySheet(CyNode.class);
		else if (tbl.equals(curNet.getDefaultNetworkTable()))
			vpSheet = vizMapperMainPanel.getVisualPropertySheet(CyNetwork.class);
		
		if (vpSheet != null) {
			// Update mapping status of this sheet's properties, if necessary
			for (final VisualPropertySheetItem<?> item : vpSheet.getItems()) {
				final VisualMappingFunction<?, ?> mapping = item.getModel().getVisualMappingFunction();
				
				if (mapping != null && mapping.getMappingColumnName().equalsIgnoreCase(colName))
					updateMappingStatus(item);
			}
		}
	}
	
//	private void showContextMenu(final JPopupMenu contextMenu, final MouseEvent e) {
//		invokeOnEDT(() -> {
//			final Component parent = (Component) e.getSource();
//			contextMenu.show(parent, e.getX(), e.getY());
//		});
//	}
//	
//	// ==[ CLASSES ]====================================================================================================
//	
//	private class ContextMenuMouseListener extends MouseAdapter {
//		
//		private VisualPropertySheet vpSheet;
//		private VisualPropertySheetItem<?> vpSheetItem;
//		
//		ContextMenuMouseListener(final VisualPropertySheet vpSheet,
//				final VisualPropertySheetItem<?> vpSheetItem) {
//			this.vpSheet = vpSheet;
//			this.vpSheetItem = vpSheetItem;
//		}
//
//		@Override
//		public void mousePressed(final MouseEvent e) {
//			maybeShowContextMenu(e, vpSheet, vpSheetItem);
//		}
//
//		@Override
//		public void mouseReleased(final MouseEvent e) {
//			maybeShowContextMenu(e, vpSheet, vpSheetItem);
//		}
//		
//		@SuppressWarnings("rawtypes")
//		private void maybeShowContextMenu(final MouseEvent e, final VisualPropertySheet vpSheet, 
//				final VisualPropertySheetItem<?> vpSheetItem) {
//			if (!e.isPopupTrigger())
//				return;
//			
//			// Select the right-clicked sheet item, if not selected yet
//			if (!vpSheetItem.isSelected())
//				vpSheet.setSelectedItems((Set) (Collections.singleton(vpSheetItem)));
//			
//			final JPopupMenu contextMenu = vizMapperMainPanel.getContextMenu();
//			
//			invokeOnEDT(() -> {
//				// Network properties don't have visual mappings
//				final JMenu mapValueGeneratorsMenu = vizMapperMainPanel.getMapValueGeneratorsSubMenu();
//				final Class<? extends CyIdentifiable> targetDataType = vpSheet.getModel().getTargetDataType();
//				mapValueGeneratorsMenu.setVisible(targetDataType != CyNetwork.class);
//				
//				if (mapValueGeneratorsMenu.isVisible()) {
//					// Add all mapping generators again, to keep a consistent order
//					mapValueGeneratorsMenu.removeAll();
//					Class<?> dataType = null; // will store the previous generator's data type
//					
//					for (final Entry<String, GenerateDiscreteValuesAction> entry : mappingGenerators.entrySet()) {
//						if (dataType != null && dataType != entry.getValue().getGenerator().getDataType())
//							mapValueGeneratorsMenu.add(new JSeparator());
//						
//						mapValueGeneratorsMenu.add(entry.getValue());
//						dataType = entry.getValue().getGenerator().getDataType();
//					}
//				}
//				
//				showContextMenu(contextMenu, e);
//			});
//		}
//	}
}
