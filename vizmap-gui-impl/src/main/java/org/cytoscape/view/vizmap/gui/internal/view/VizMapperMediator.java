package org.cytoscape.view.vizmap.gui.internal.view;

import static org.cytoscape.view.vizmap.gui.internal.util.NotificationNames.*;
import static org.cytoscape.view.vizmap.gui.internal.view.util.ViewUtil.invokeOnEDT;
import static org.cytoscape.view.vizmap.gui.internal.view.util.ViewUtil.invokeOnEDTAndWait;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.function.BiConsumer;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.event.DebounceTimer;
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
import org.cytoscape.view.presentation.property.DefaultVisualizableVisualProperty;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;
import org.cytoscape.view.vizmap.TableVisualMappingManager;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.events.VisualMappingFunctionChangedEvent;
import org.cytoscape.view.vizmap.events.VisualMappingFunctionChangedListener;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.event.LexiconStateChangedEvent;
import org.cytoscape.view.vizmap.gui.event.LexiconStateChangedListener;
import org.cytoscape.view.vizmap.gui.internal.ColumnSpec;
import org.cytoscape.view.vizmap.gui.internal.GraphObjectType;
import org.cytoscape.view.vizmap.gui.internal.VizMapperProperty;
import org.cytoscape.view.vizmap.gui.internal.action.GenerateDiscreteValuesAction;
import org.cytoscape.view.vizmap.gui.internal.model.AttributeSetProxy;
import org.cytoscape.view.vizmap.gui.internal.model.LockedValueState;
import org.cytoscape.view.vizmap.gui.internal.model.LockedValuesVO;
import org.cytoscape.view.vizmap.gui.internal.model.MappingFunctionFactoryProxy;
import org.cytoscape.view.vizmap.gui.internal.model.PropsProxy;
import org.cytoscape.view.vizmap.gui.internal.model.VizMapperProxy;
import org.cytoscape.view.vizmap.gui.internal.util.NotificationNames;
import org.cytoscape.view.vizmap.gui.internal.util.ServicePropertiesUtil;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.view.ColumnStylePicker.Action;
import org.cytoscape.view.vizmap.gui.internal.view.VisualPropertySheetItem.MessageType;
import org.cytoscape.view.vizmap.gui.internal.view.editor.mappingeditor.ContinuousMappingEditorPanel;
import org.cytoscape.view.vizmap.gui.internal.view.editor.mappingeditor.EditorValueRangeTracer;
import org.cytoscape.view.vizmap.gui.util.DiscreteMappingGenerator;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.undo.UndoSupport;
import org.puremvc.java.multicore.interfaces.INotification;
import org.puremvc.java.multicore.patterns.mediator.Mediator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape VizMap GUI Impl (vizmap-gui-impl)
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

@SuppressWarnings({"unchecked", "serial"})
public class VizMapperMediator extends Mediator implements LexiconStateChangedListener, RowsSetListener, 
														   ColumnCreatedListener, ColumnDeletedListener,
														   ColumnNameChangedListener, UpdateNetworkPresentationListener,
														   VisualMappingFunctionChangedListener {

	public static final String NAME = "VizMapperMediator";
	
	static final List<Class<? extends CyIdentifiable>> NETWORK_SHEET_TYPES = List.of(CyNode.class, CyEdge.class, CyNetwork.class);
	static final List<Class<? extends CyIdentifiable>> TABLE_SHEET_TYPES   = List.of(CyColumn.class);
	
	private VizMapperProxy vmProxy;
	private AttributeSetProxy attrProxy;
	private MappingFunctionFactoryProxy mappingFactoryProxy;
	private PropsProxy propsProxy;
	
	private boolean ignoreVisualStyleSelectedEvents;
	
	private VisualPropertySheetItem<?> curVpSheetItem;
	private VizMapperProperty<?, ?, ?> curVizMapperProperty;
	
	// Remember what column was selected.
	private ColumnSpec selectedColumn;
	private BiConsumer<ColumnSpec,ColumnStylePicker.Action> columnChangeListener;
	
	// MKTODO Current network renderer
	private String curRendererId;
	
	private final ServicesUtil servicesUtil;
	private final VizMapperMainPanel vizMapperMainPanel;
	private final VizMapPropertyBuilder vizMapPropertyBuilder;
	
	private final Map<String, GenerateDiscreteValuesAction> mappingGenerators;
	private final Map<TaskFactory, JMenuItem> taskFactories;
	private final Map<CyAction, JMenuItem> actions;
	
	/** IDs of property sheet items that were set visible/invisible by the user */
	private final Map<String, Boolean> userProps;
	
	private final Map<Class<? extends CyIdentifiable>, Set<String>> defVisibleProps;
	
	private final DebounceTimer debounceTimer = new DebounceTimer(240);
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public VizMapperMediator(
			VizMapperMainPanel vizMapperMainPanel,
			ServicesUtil servicesUtil,
			VizMapPropertyBuilder vizMapPropertyBuilder
	) {
		super(NAME, vizMapperMainPanel);
		
		this.vizMapperMainPanel = Objects.requireNonNull(vizMapperMainPanel, "'vizMapperMainPanel' must not be null");
		this.servicesUtil = Objects.requireNonNull(servicesUtil, "'servicesUtil' must not be null");
		this.vizMapPropertyBuilder = Objects.requireNonNull(vizMapPropertyBuilder, "'vizMapPropertyBuilder' must not be null");
		
		mappingGenerators = new TreeMap<>(Collator.getInstance(Locale.getDefault())::compare);
		
		taskFactories = new HashMap<>();
		actions = new HashMap<>();
		userProps = new HashMap<>();
		defVisibleProps = new HashMap<>();
		
		setViewComponent(vizMapperMainPanel);
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public final void onRegister() {
		vmProxy = (VizMapperProxy) getFacade().retrieveProxy(VizMapperProxy.NAME);
		attrProxy = (AttributeSetProxy) getFacade().retrieveProxy(AttributeSetProxy.NAME);
		mappingFactoryProxy = (MappingFunctionFactoryProxy) getFacade().retrieveProxy(MappingFunctionFactoryProxy.NAME);
		propsProxy = (PropsProxy) getFacade().retrieveProxy(PropsProxy.NAME);
		
		updateDefaultProps();
		initView();
		super.onRegister();
	}
	
	@Override
	public String[] listNotificationInterests() {
		return new String[]{ VISUAL_STYLE_SET_CHANGED,
							 VISUAL_STYLE_ADDED,
							 VISUAL_STYLE_REMOVED,
							 CURRENT_VISUAL_STYLE_CHANGED,
							 VISUAL_STYLE_UPDATED,
							 CURRENT_NETWORK_VIEW_CHANGED,
							 VISUAL_STYLE_NAME_CHANGED};
	}
	
	@Override
	public void handleNotification(INotification notification) {
		var id = notification.getName();
		var body = notification.getBody();

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
				invokeOnEDT(() -> {
					var vs = (VisualStyle) body;
					updateAllVisualPropertySheets(vs, false);
				});
				break;
			case VISUAL_STYLE_UPDATED:
				if (body != null) {
					var style = (VisualStyle) body;
					VisualStyle currNetStyle = vmProxy.getCurrentNetworkVisualStyle();
					
					if (style.equals(currNetStyle)) {
						updateAllVisualPropertySheets(style, false);
					} else {
						// Check if its a column visual style associated with the current network style.
						var tableVMM = servicesUtil.get(TableVisualMappingManager.class);
						Set<VisualStyle> netStyles = tableVMM.getAssociatedNetworkVisualStyles(style);
						if(netStyles.contains(currNetStyle)) {
							updateVisualPropertySheets(style, TABLE_SHEET_TYPES, false, false);
						}
						break;
					}
					
					invokeOnEDT(() -> vizMapperMainPanel.getStylesPanelProvider().update(style));
				}
				break;
			case CURRENT_NETWORK_VIEW_CHANGED:
				var view = (CyNetworkView) body;
				var newRendererId = view != null ? view.getRendererId() : null;
				
				if (view != null && newRendererId != null && !newRendererId.equals(curRendererId)) {
					updateAllVisualPropertySheets(vmProxy.getNetworkVisualStyle(view), false);
					curRendererId = newRendererId;
				} else if (view == null || vmProxy.getNetworkVisualStyle(view).equals(vizMapperMainPanel.getSelectedVisualStyle())) {
					// Ignore it, if the selected style is not the current one,
					// because it should change the selection style first and then recreate all the items, anyway.
					updateLockedValues(view);
					
					if (body instanceof CyNetworkView) {
						updateMappings(CyNode.class, view.getModel().getDefaultNodeTable());
						updateMappings(CyEdge.class, view.getModel().getDefaultEdgeTable());
					}
					
					updateItemsStatus();
				}
				break;
			case VISUAL_STYLE_NAME_CHANGED:
				invokeOnEDT(() -> {
					vizMapperMainPanel.getStylesBtn().update();
					vizMapperMainPanel.getStylesPanelProvider().update((VisualStyle) body);
				});
				break;
		}
	}
	
	private VisualProperty<?> getRootVP(Class<? extends CyIdentifiable> type) {
		if (NETWORK_SHEET_TYPES.contains(type))
			return BasicVisualLexicon.NETWORK;
		else
			return BasicTableVisualLexicon.CELL;
	}
	
	public CyTable getCurrentMappingTable() {
		var tableType = getSelectedVisualPropertySheet().getModel().getTableType();
		return getCurrentMappingTable(tableType);
	}
	
	public CyTable getCurrentMappingTable(GraphObjectType type) {
		var curNet = vmProxy.getCurrentNetwork();
		if (type.type() == CyNode.class) {
			return curNet == null ? null : curNet.getDefaultNodeTable();
		} else if (type.type() == CyEdge.class) {
			return curNet == null ? null : curNet.getDefaultEdgeTable();
		} else if (type.type() == CyNetwork.class) {
			return curNet == null ? null : curNet.getDefaultNetworkTable();
		}
		return null;
	}
	

	// MKTODO possibly get rid of this method
	@Override
	public void handleEvent(LexiconStateChangedEvent e) {
		// Update Network Views
		var curStyle = vmProxy.getCurrentNetworkVisualStyle();
		var views = vmProxy.getNetworkViewsWithStyle(curStyle);
		
		for (var view : views) { // TODO This should be done by NetworkViewMediator only, if possible
			curStyle.apply(view);
			view.updateView();
		}
		
		// Update VP Sheet Items
		invokeOnEDT(() -> updateItemsStatus());
	}
	
	@Override
	public void handleEvent(RowsSetEvent e) {
		var tbl = e.getSource();
		
		// Update bypass buttons--check selected nodes and edges of the current view
		var curNetView = vmProxy.getCurrentNetworkView();
		
		if (curNetView != null && e.containsColumn(CyNetwork.SELECTED)) {
			var curNet = curNetView.getModel();
			
			// We have to get all selected elements again
			if (tbl.equals(curNet.getDefaultEdgeTable()))
				updateLockedValues(vmProxy.getSelectedEdgeViews(curNetView), CyEdge.class);
			else if (tbl.equals(curNet.getDefaultNodeTable()))
				updateLockedValues(vmProxy.getSelectedNodeViews(curNetView), CyNode.class);
			else if (tbl.equals(curNet.getDefaultNetworkTable()))
				updateLockedValues(Collections.singleton((View<CyNetwork>)curNetView), CyNetwork.class);
		}
		
		// Also update mappings
		var curNet = vmProxy.getCurrentNetwork();
		
		if (curNet != null) {
			var vpSheets = new ArrayList<VisualPropertySheet>(2);
			
			if (tbl.equals(curNet.getDefaultEdgeTable())) {
				vpSheets.add(vizMapperMainPanel.getVisualPropertySheet(CyEdge.class));
				vpSheets.add(vizMapperMainPanel.getVisualPropertySheet(CyColumn.class));
			} else if (tbl.equals(curNet.getDefaultNodeTable())) {
				vpSheets.add(vizMapperMainPanel.getVisualPropertySheet(CyNode.class));
				vpSheets.add(vizMapperMainPanel.getVisualPropertySheet(CyColumn.class));
			} else if (tbl.equals(curNet.getDefaultNetworkTable())) {
				vpSheets.add(vizMapperMainPanel.getVisualPropertySheet(CyNetwork.class));
				vpSheets.add(vizMapperMainPanel.getVisualPropertySheet(CyColumn.class)); // Not really needed by why not.
			}
			
			if (!vpSheets.isEmpty()) {
				var columns = e.getColumns();
				
				for (var vpSheet : vpSheets) {
					if (vpSheet == null) continue; // Shouldn't happen
					
					for (var item : vpSheet.getItems()) {
						var mapping = item.getModel().getVisualMappingFunction();
						
						if (mapping != null) {
							for (var columnName : columns) {
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
	}
	
	@Override
	public void handleEvent(ColumnDeletedEvent e) {
		var table = e.getSource();
		onColumnChangedUpdateMappings(e.getColumnName(), table);
	}

	@Override
	public void handleEvent(ColumnCreatedEvent e) {
		var table = e.getSource();
		onColumnChangedUpdateMappings(e.getColumnName(), table);
	}
	
	@Override
	public void handleEvent(ColumnNameChangedEvent e) {
		var table = e.getSource();
		onColumnChangedUpdateMappings(e.getOldColumnName(), table);
		onColumnChangedUpdateMappings(e.getNewColumnName(), table);
	}

	@Override
	public void handleEvent(UpdateNetworkPresentationEvent e) {
		var view = e.getSource();
		
		if (view.equals(vmProxy.getCurrentNetworkView()))
			updateLockedValues(view);
	}
	
	@Override
	public void handleEvent(VisualMappingFunctionChangedEvent e) {
		var vm = e.getSource();
		var vp = vm.getVisualProperty();
		var curStyleSet = vmProxy.getCurrentVisualStyleSet();
		
		// If the source mapping belongs to the current visual style, update the correspondent property sheet item
		if (curStyleSet != null) {
			for(VisualStyle style : curStyleSet.getAllStyles()) {
				if(vm.equals(style.getVisualMappingFunction(vp))) {
					var vpSheet = vizMapperMainPanel.getVisualPropertySheet(vp.getTargetDataType());
					if (vpSheet != null) {
						var vpSheetItem = vpSheet.getItem(vp);
						if (vpSheetItem != null) {
							invokeOnEDT(() -> vpSheetItem.updateMapping());
						}
					}
				}
			}
		}
	}

	public VisualPropertySheetItem<?> getCurrentVisualPropertySheetItem() {
		return curVpSheetItem;
	}
	
	public VisualPropertySheet getSelectedVisualPropertySheet() {
		return vizMapperMainPanel.getSelectedVisualPropertySheet();
	}
	
	public VizMapperProperty<?, ?, ?> getCurrentVizMapperProperty() {
		return curVizMapperProperty;
	}
	
	/**
	 * Custom listener for adding registered VizMapper CyActions to the main menu.
	 */
	public synchronized void onCyActionRegistered(CyAction action, Map<?, ?> properties) {
		var serviceType = ServicePropertiesUtil.getServiceType(properties);
		
		if (serviceType != null && serviceType.startsWith("vizmapUI")) {
			invokeOnEDT(() -> {
				var menuItem = createMenuItem(action, properties);
				if (menuItem != null)
					actions.put(action, menuItem);
			});
		}
	}

	/**
	 * Custom listener for removing unregistered VizMapper CyActions from the main and context menus.
	 */
	public synchronized void onCyActionUnregistered(CyAction action, Map<?,?> properties) {
		var menuItem = actions.remove(action);
		
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
	public void onTaskFactoryRegistered(TaskFactory taskFactory, Map<?,?> properties) {
		// First filter the service...
		var serviceType = ServicePropertiesUtil.getServiceType(properties);
		
		if (serviceType == null || !serviceType.toString().startsWith("vizmapUI"))
			return;

		var title = ServicePropertiesUtil.getTitle(properties);
		
		if (title == null) {
			logger.error("Cannot create VizMapper menu item for: " + taskFactory + 
					"; \"" + ServiceProperties.TITLE +  "\" metadata is missing from properties: " + properties);
			return;
		}

		// Add new menu to the pull-down
		var config = new HashMap<String, String>();
		config.put(ServiceProperties.TITLE, title.toString());
		
		var action = new AbstractCyAction(config, taskFactory) {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(() -> {
					servicesUtil.get(DialogTaskManager.class).execute(taskFactory.createTaskIterator());
				}).start();
			}
		};
		
		invokeOnEDT(() -> {
			var menuItem = createMenuItem(action, properties);
			
			if (menuItem != null)
				taskFactories.put(taskFactory, menuItem);
		});
	}

	public void onTaskFactoryUnregistered(TaskFactory taskFactory, Map<?, ?> properties) {
		var menuItem = taskFactories.remove(taskFactory);
		
		if (menuItem != null) {
			invokeOnEDT(() -> {
				vizMapperMainPanel.removeOption(menuItem);
				vizMapperMainPanel.removeContextMenuItem(menuItem);
			});
		}
	}

	public void onMappingGeneratorRegistered(DiscreteMappingGenerator<?> generator, Map<?, ?> properties) {
		var serviceType = ServicePropertiesUtil.getServiceType(properties);
		
		if (serviceType == null) {
			logger.error("Cannot create VizMapper context menu item for: " + generator + 
					"; \"" + ServicePropertiesUtil.SERVICE_TYPE +  "\" metadata is missing from properties: " + properties);
			return;
		}

		// This is a menu item for Main Command Button.
		var title = ServicePropertiesUtil.getTitle(properties);;
		
		if (title == null) {
			logger.error("Cannot create VizMapper context menu item for: " + generator + 
					"; \"" + ServiceProperties.TITLE +  "\" metadata is missing from properties: " + properties);
			return;
		}
		
		// Add new menu to the pull-down
		var action = new GenerateDiscreteValuesAction(title.toString(), generator, servicesUtil);
		vizMapperMainPanel.getContextMenu().addPopupMenuListener(action);
		
		// Concatenate the data type with the title when setting the map key, so the generators
		// can be sorted first by data type and then by title.
		mappingGenerators.put(generator.getDataType().getSimpleName() + "::" + title.toString(), action);
	}

	public void onMappingGeneratorUnregistered(DiscreteMappingGenerator<?> generator, Map<?, ?> properties) {
		var iter = mappingGenerators.entrySet().iterator();
		
		while (iter.hasNext()) {
			var entry = iter.next();
			var action = entry.getValue();
			
			if (action.getGenerator().equals(generator)) {
				vizMapperMainPanel.getContextMenu().removePopupMenuListener(action);
				iter.remove();
				break;
			}
		}
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================

	private void updateDefaultProps() {
		defVisibleProps.clear();
		defVisibleProps.put(CyNode.class,    propsProxy.getDefaultVisualProperties(CyNode.class));
		defVisibleProps.put(CyEdge.class,    propsProxy.getDefaultVisualProperties(CyEdge.class));
		defVisibleProps.put(CyNetwork.class, propsProxy.getDefaultVisualProperties(CyNetwork.class));
		defVisibleProps.put(CyColumn.class,  propsProxy.getDefaultVisualProperties(CyColumn.class));
	}
	
	private void initView() {
		servicesUtil.registerAllServices(vizMapperMainPanel, new Properties());
		addViewListeners();
	}
	
	private void addViewListeners() {
		// Switching the current Visual Style
		var stylesBtn = vizMapperMainPanel.getStylesBtn();
		stylesBtn.addPropertyChangeListener("selectedStyle", evt -> onSelectedVisualStyleChanged(evt));
		
		columnChangeListener = this::onSelectedColumnChanged;
		vizMapperMainPanel.getColumnStylePnl().addColumnSelectionListener(columnChangeListener);
//		vizMapperMainPanel.getColumnStylePnl().getAddButton(columnChangeListener);
	}
	
	private void addViewListeners(VisualPropertySheet vpSheet) {
		for (var vpSheetItem : vpSheet.getItems())
			addViewListeners(vpSheet, vpSheetItem);
	}

	private void addViewListeners(VisualPropertySheet vpSheet, VisualPropertySheetItem<?> vpSheetItem) {
		if (vpSheetItem.getModel().getVisualPropertyDependency() == null) {
			// It's a regular VisualProperty Editor...
			
			// Default value button clicked
			vpSheetItem.getDefaultBtn().addActionListener(evt -> openDefaultValueEditor(evt, vpSheetItem));
			
			// Default value button right-clicked
			vpSheetItem.getDefaultBtn().addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					maybeShowContextMenu(e);
				}
				@Override
				public void mouseReleased(MouseEvent e) {
					maybeShowContextMenu(e);
				}
				private void maybeShowContextMenu(MouseEvent e) {
					if (e.isPopupTrigger()) {
						var contextMenu = new JPopupMenu();
						contextMenu.add(new JMenuItem(new AbstractAction("Reset Default Value") {
							@Override
							public void actionPerformed(ActionEvent e) {
								vpSheetItem.getModel().resetDefaultValue();
							}
						}));
						showContextMenu(contextMenu, e);
					}
				}
			});
			
			// Bypass button clicked
			if (vpSheetItem.getModel().isLockedValueAllowed()) {
				// Create context menu
				var bypassMenu = new JPopupMenu();
				final JMenuItem removeBypassMenuItem;
				
				bypassMenu.add(new JMenuItem(new AbstractAction("Set Bypass...") {
					@Override
					public void actionPerformed(ActionEvent e) {
						openLockedValueEditor(e, vpSheetItem);
					}
				}));
				bypassMenu.add(removeBypassMenuItem = new JMenuItem(new AbstractAction("Remove Bypass") {
					@Override
					public void actionPerformed(ActionEvent e) {
						removeLockedValue(e, vpSheetItem);
					}
				}));
				
				// Right-clicked
				vpSheetItem.getBypassBtn().addMouseListener(new MouseAdapter() {
					@Override
					public void mousePressed(MouseEvent e) {
						maybeShowContextMenu(e);
					}
					@Override
					public void mouseReleased(MouseEvent e) {
						maybeShowContextMenu(e);
					}
					private void maybeShowContextMenu(MouseEvent e) {
						if (vpSheetItem.getBypassBtn().isEnabled() && e.isPopupTrigger()) {
							var state = vpSheetItem.getModel().getLockedValueState();
							removeBypassMenuItem.setEnabled(state != LockedValueState.ENABLED_NOT_SET);
							showContextMenu(bypassMenu, e);
						}
					}
				});
				
				// Left-clicked
				vpSheetItem.getBypassBtn().addActionListener(evt -> {
					var state = vpSheetItem.getModel().getLockedValueState();
					var btn = vpSheetItem.getBypassBtn();
					
					if (state == LockedValueState.ENABLED_NOT_SET) {
						// There is only one option to execute, so do it now, rather than showing the popup menu
						openLockedValueEditor(evt, vpSheetItem);
					} else {
						bypassMenu.show(btn, 0, btn.getHeight());
						bypassMenu.requestFocusInWindow();
					}
				});
			}
			
			// Right-click
			var cmMouseListener = new ContextMenuMouseListener(vpSheet, vpSheetItem);
			vpSheetItem.addMouseListener(cmMouseListener);
			
			if (vpSheetItem.getModel().isVisualMappingAllowed()) {
				vpSheetItem.getPropSheetPnl().getTable().addMouseListener(cmMouseListener);
				vpSheetItem.getRemoveMappingBtn().addActionListener(evt -> {
					curVpSheetItem = vpSheetItem;
					removeVisualMapping(vpSheetItem);
				});
				vpSheetItem.getPropSheetTbl().addPropertyChangeListener("editingVizMapperProperty", evt -> {
					curVpSheetItem = vpSheetItem; // Save the current editor (the one the user is interacting with)
					curVizMapperProperty = (VizMapperProperty<?, ?, ?>) evt.getNewValue();
					
					var mappingTypeProperty = vizMapPropertyBuilder.getMappingTypeProperty(vpSheetItem.getPropSheetPnl());
					var factory = (VisualMappingFunctionFactory) mappingTypeProperty.getValue();
					attrProxy.setCurrentMappingType(factory != null ? factory.getMappingFunctionType() : null);

					var columnProp = vizMapPropertyBuilder.getColumnProperty(vpSheetItem.getPropSheetPnl());
					var columnValue = columnProp.getValue();
					mappingFactoryProxy.setCurrentColumnName(columnValue != null ? columnValue.toString() : null);
					mappingFactoryProxy.setCurrentTargetDataType(vpSheet.getModel().getTableType());
				});
			}
		} else {
			// It's a Dependency Editor...
			vpSheetItem.getDependencyCkb().addItemListener(evt -> onDependencySelectionChanged(evt, vpSheetItem));
		}
		
		// Save sheet items that were explicitly shown/hidden by the user,
		// so his preferences can be respected when the current style changes
		vpSheetItem.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				userProps.put(vpSheetItem.getModel().getId(), Boolean.TRUE);
			}
			@Override
			public void componentHidden(ComponentEvent e) {
				userProps.put(vpSheetItem.getModel().getId(), Boolean.FALSE);
			}
		});
	}

	protected void removeVisualMapping(VisualPropertySheetItem<?> vpSheetItem) {
		var vm = vpSheetItem.getModel().getVisualMappingFunction();
		if (vm != null)
			sendNotification(NotificationNames.REMOVE_VISUAL_MAPPINGS, Collections.singleton(vm));
	}

	private void updateVisualStyleList(SortedSet<VisualStyle> styles, boolean resetDefaultVisibleItems) {
		attrProxy.setCurrentMappingType(null);
		mappingFactoryProxy.setCurrentColumnName(null);
		
		debounceTimer.debounce(() -> {
			var vs = vmProxy.getCurrentNetworkVisualStyle();
//			var table = vmProxy.getCurrentTable();
			
			invokeOnEDT(() -> {
				ignoreVisualStyleSelectedEvents = true;
				vizMapperMainPanel.updateVisualStyles(styles, vs);
				selectCurrentVisualStyle(vs);
				ignoreVisualStyleSelectedEvents = false;
				updateAllVisualPropertySheets(vs, resetDefaultVisibleItems);
				
//				if (table != null)
//					updateTableVisualPropertySheets(table, resetDefaultVisibleItems, false);
			});
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
	
	private void updateAllVisualPropertySheets(VisualStyle netVS, boolean resetDefaultVisibleItems) {
		boolean rebuild = shouldRebuildVisualPropertySheets(netVS);
	
		var tableVMM = servicesUtil.get(TableVisualMappingManager.class);
		
		VisualStyle colVS;
		if(selectedColumn == null) {
			colVS = tableVMM.getDefaultVisualStyle();
		} else {
			var tableType = selectedColumn.tableType().type();
			colVS = tableVMM.getAssociatedColumnVisualStyle(netVS, tableType, selectedColumn.columnName());
		}
		
		var nodeColStyles = tableVMM.getAssociatedColumnVisualStyles(netVS, CyNode.class);
		var edgeColStyles = tableVMM.getAssociatedColumnVisualStyles(netVS, CyEdge.class);
		
		List<ColumnSpec> columns = new ArrayList<>();
		for(var entry : nodeColStyles.entrySet()) {
			columns.add(new ColumnSpec(GraphObjectType.node(), entry.getKey()));
		}
		for(var entry : edgeColStyles.entrySet()) {
			columns.add(new ColumnSpec(GraphObjectType.edge(), entry.getKey()));
		}
		
		columns.sort(Comparator.naturalOrder());
		
		updateVisualPropertySheets(netVS, NETWORK_SHEET_TYPES, resetDefaultVisibleItems, rebuild);
		updateVisualPropertySheets(colVS, TABLE_SHEET_TYPES, resetDefaultVisibleItems, true);
		
		vizMapperMainPanel.getColumnStylePnl().removeColumnSelectionListener(columnChangeListener);
		vizMapperMainPanel.updateColumns(columns, selectedColumn);
		vizMapperMainPanel.getColumnStylePnl().addColumnSelectionListener(columnChangeListener);
	}
	
	
	private void onSelectedColumnChanged(ColumnSpec col, ColumnStylePicker.Action action) {
		if(col == null || action == null)
			return;
		
		ContinuousMappingEditorPanel.setTracer(new EditorValueRangeTracer(servicesUtil)); // MKTODO why?
		
		var netVS = vmProxy.getCurrentNetworkVisualStyle();
		var tableVMM = servicesUtil.get(TableVisualMappingManager.class);
		var tableType = col.tableType().type();
		
		selectedColumn = col;
		
		if(action == Action.CREATE) {
			VisualStyle colVS = tableVMM.getAssociatedColumnVisualStyle(netVS, tableType, col.columnName());
			if(colVS == null) {
				var visualStyleFactory = servicesUtil.get(VisualStyleFactory.class);
				colVS = visualStyleFactory.createVisualStyle(col.columnName());
				tableVMM.setAssociatedVisualStyle(netVS, tableType, col.columnName(), colVS);
				updateAllVisualPropertySheets(netVS, false);
			}
		} else if(action == Action.DELETE) {
			tableVMM.setAssociatedVisualStyle(netVS, tableType, col.columnName(), null);
			selectedColumn = null;
			updateAllVisualPropertySheets(netVS, false);
		} else { // UPDATE
			VisualStyle colVS = tableVMM.getAssociatedColumnVisualStyle(netVS, tableType, col.columnName());
			updateVisualPropertySheets(colVS, TABLE_SHEET_TYPES, false, true);
		}
	}
	
	
	private void updateVisualPropertySheets(
			VisualStyle vs,
			List<Class<? extends CyIdentifiable>> sheetTypes,
			boolean resetDefaultVisibleItems,
			boolean rebuild
	) {
		if (vs == null)
			return;

		if (!rebuild) {
			// Also check if dependencies have changed
			var map = new HashMap<String, VisualPropertyDependency<?>>();
			var dependencies = vs.getAllVisualPropertyDependencies();
			
			for (var dep : dependencies) {
				var type = dep.getParentVisualProperty().getTargetDataType();
				var sheet = vizMapperMainPanel.getVisualPropertySheet(type);
				
				if (sheet == null) continue; // Shouldn't happen

				if (sheet.getItem(dep) == null) {
					// There's a new dependency!
					rebuild = true;
					break;
				}
				
				map.put(dep.getIdString(), dep);
			}
			
			if (!rebuild) {
				var vpSheets = vizMapperMainPanel.getVisualPropertySheets();
				
				for (var sheet : vpSheets) {
					for (var item : sheet.getItems()) {
						var dep = item.getModel().getVisualPropertyDependency();
						
						if (dep != null && !map.containsKey(dep.getIdString())) {
							// This dependency has been removed from the Visual Style!
							rebuild = true;
							break;
						}
					}
				}
			}
		}
		
		if (rebuild) {
			createVisualPropertySheets(vs, sheetTypes, resetDefaultVisibleItems);
		} else {
			// Just update the current Visual Property sheets
			var vpSheets = vizMapperMainPanel.getVisualPropertySheets();
			
			for (var sheet : vpSheets) {
				for (var item : sheet.getItems()) {
					// Update values
					var model = item.getModel();
					model.update(vmProxy.getRenderingEngine(model.getLexiconType()));
					
					if (model.getVisualPropertyDependency() != null)
						item.update();
					
					// Also make sure items with mappings are visible
					if (model.getVisualMappingFunction() != null)
						item.setVisible(true);
				}
			}
			
			if (resetDefaultVisibleItems)
				updateVisibleItems(resetDefaultVisibleItems);
		}
	}
	
	private boolean shouldRebuildVisualPropertySheets(VisualStyle vs) {
		var curNetSheet = vizMapperMainPanel.getVisualPropertySheet(CyNetwork.class);
		var curModel = curNetSheet != null ? curNetSheet.getModel() : null;
		var curStyle = curModel != null ? curModel.getVisualStyle() : null;
		var curNetView = vmProxy.getCurrentNetworkView();
		var newRendererId = curNetView != null ? curNetView.getRendererId() : "";
		
		// If a different style or renderer, rebuild all property sheets
		boolean rebuild = !vs.equals(curStyle) || !newRendererId.equals(curRendererId);
		
		if (curNetView != null)
			curRendererId = curNetView.getRendererId();
		
		return rebuild;
	}
	
//	private boolean shouldRebuildTableVisualPropertySheets(VisualStyle vs) {
//		var col = vizMapperMainPanel.getColumnStylePnl().getSelectedColumn();
//		var curStyle = vmProxy.getVisualStyle(col);
//		
//		return vs != null && !vs.equals(curStyle);
//	}
	
	private void createVisualPropertySheets(VisualStyle style, List<Class<? extends CyIdentifiable>> sheetTypes, boolean resetDefaultVisibleItems) {
		invokeOnEDT(() -> {
			var selVpSheet = getSelectedVisualPropertySheet();
			var selectedTargetDataType = selVpSheet != null ? selVpSheet.getModel().getLexiconType() : null;
			
			for (var lexiconType : sheetTypes) {
				// Create Visual Property Sheet
				var re = vmProxy.getRenderingEngine(lexiconType);
				
				VisualPropertySheet vpSheet;
				Set<VisualPropertySheetItem<?>> vpSheetItems;
				
				if (re == null || (sheetTypes == TABLE_SHEET_TYPES && selectedColumn == null)) {
					var model = new VisualPropertySheetModel(lexiconType, null, style, null);
					vpSheet = new VisualPropertySheet(model, servicesUtil);
					vizMapperMainPanel.addVisualPropertySheet(vpSheet);
					
					vpSheetItems = Collections.emptySet();
					vpSheet.setItems(vpSheetItems);
					
				} else {
					var lexicon = re.getVisualLexicon();
					GraphObjectType tableType;
					if(lexiconType == CyColumn.class) {
						if(selectedColumn == null) // this shouldn't happen, being defensive
							tableType = GraphObjectType.node();
						else
							tableType = selectedColumn.tableType();
					} else {
						tableType = GraphObjectType.of(lexiconType);
					}
					
					var model = new VisualPropertySheetModel(lexiconType, tableType, style, lexicon);
					vpSheet = new VisualPropertySheet(model, servicesUtil);
					vizMapperMainPanel.addVisualPropertySheet(vpSheet);
					
					vpSheetItems = createVisualPropertySheetItems(lexiconType, tableType, lexicon, style);
					vpSheet.setItems(vpSheetItems);
				}
				
				// Add event listeners to the new components
				addViewListeners(vpSheet);
				
				// Add more menu items to the Properties menu
				if (vpSheetItems.size() > 1) {
					vpSheet.getVpsMenu().add(new JSeparator());
					
					{
						var mi = new JMenuItem("Show Default");
						mi.addActionListener(evt -> showDefaultItems(vpSheet));
						vpSheet.getVpsMenu().add(mi);
					}
					{
						var mi = new JMenuItem("Show All");
						mi.addActionListener(evt -> setVisibleItems(vpSheet, true));
						vpSheet.getVpsMenu().add(mi);
					}
					{
						var mi = new JMenuItem("Hide All");
						mi.addActionListener(evt -> setVisibleItems(vpSheet, false));
						vpSheet.getVpsMenu().add(mi);
					}
				}
				
				vpSheet.getVpsMenu().add(new JSeparator());
				
				var mi = new JMenuItem("Make Default");
				mi.addActionListener(evt -> saveDefaultVisibleItems(vpSheet));
				vpSheet.getVpsMenu().add(mi);
			}
			
			updateVisibleItems(resetDefaultVisibleItems);
			updateItemsStatus();
			
			// Update panel's width
			int minWidth = 200;
			
			for (var vpSheet : vizMapperMainPanel.getVisualPropertySheets()) {
				minWidth = Math.max(minWidth, vpSheet.getMinimumSize().width);
			}
			
			vizMapperMainPanel.setPreferredSize(
					new Dimension(vizMapperMainPanel.getPropertiesPnl().getComponent().getMinimumSize().width + 20,
								  vizMapperMainPanel.getPreferredSize().height));
			
			// Select the same sheet that was selected before
			var vpSheet = vizMapperMainPanel.getVisualPropertySheet(selectedTargetDataType);
			vizMapperMainPanel.setSelectedVisualPropertySheet(vpSheet);
		});
	}
	
	@SuppressWarnings("rawtypes")
	private Set<VisualPropertySheetItem<?>> createVisualPropertySheetItems(Class<? extends CyIdentifiable> lexiconType, GraphObjectType tableType, VisualLexicon lexicon, VisualStyle style) {
		var items = new HashSet<VisualPropertySheetItem<?>>();
		
		if (lexicon == null || style == null)
			return items;
		
		var vpList = lexicon.getAllDescendants(getRootVP(lexiconType));
		var engine = vmProxy.getRenderingEngine(lexiconType);
		
		for (var vp : vpList) {
			if (vp.getTargetDataType() != lexiconType || vp instanceof DefaultVisualizableVisualProperty)
				continue;
			if (!VizMapperProxy.isSupported(lexicon, vp))
				continue;
			
			// Create model
			var model = new VisualPropertySheetItemModel(vp, tableType, lexiconType, style, engine, lexicon);
			final Set values;
			
			if (vp.getTargetDataType() == CyNode.class) {
				var curNetView = vmProxy.getCurrentNetworkView();
				var selectedNodeViews = vmProxy.getSelectedNodeViews(curNetView);
				values = getDistinctLockedValues(vp, selectedNodeViews);
				updateVpInfoLockedState(model, values, selectedNodeViews);
			} else if (vp.getTargetDataType() == CyEdge.class) {
				var curNetView = vmProxy.getCurrentNetworkView();
				var selectedEdgeViews = vmProxy.getSelectedEdgeViews(curNetView);
				values = getDistinctLockedValues(vp, selectedEdgeViews);
				updateVpInfoLockedState(model, values, selectedEdgeViews);
			} else if (vp.getTargetDataType() == CyNetwork.class) {
				var curNetView = vmProxy.getCurrentNetworkView();
				var selectedNetViews = curNetView != null ? Collections.singleton((View<CyNetwork>)curNetView) : Collections.EMPTY_SET;
				values = getDistinctLockedValues(vp, selectedNetViews);
				updateVpInfoLockedState(model, values, selectedNetViews);
			}
			
			// Create View
			var sheetItem = new VisualPropertySheetItem(model, vizMapPropertyBuilder, servicesUtil);
			items.add(sheetItem);
			
			// Add listeners to item and model:
			if (model.isVisualMappingAllowed()) {
				sheetItem.getPropSheetPnl().addPropertySheetChangeListener(evt -> {
					if (evt.getPropertyName().equals("value") && evt.getSource() instanceof VizMapperProperty)
						updateMappingStatus(sheetItem);
				});
			}
			
			// Set the updated values to the visual style
			model.addPropertyChangeListener("defaultValue", evt -> {
				var vs = model.getVisualStyle();
				vs.setDefaultValue((VisualProperty)vp, evt.getNewValue());
			});
			model.addPropertyChangeListener("visualMappingFunction", evt -> {
				var vs = model.getVisualStyle();
				
				if (evt.getNewValue() == null && vs.getVisualMappingFunction(vp) != null)
					vs.removeVisualMappingFunction(vp);
				else if (evt.getNewValue() != null && !evt.getNewValue().equals(vs.getVisualMappingFunction(vp)))
					vs.addVisualMappingFunction((VisualMappingFunction<?, ?>)evt.getNewValue());
				
				updateMappingStatus(sheetItem);
			});
		}
		
		// Add dependencies
		var dependencies = style.getAllVisualPropertyDependencies();
		
		for (var dep : dependencies) {
			if (dep.getParentVisualProperty().getTargetDataType() != lexiconType)
				continue;
			if (!VizMapperProxy.isSupported(lexicon, dep))
				continue;
			
			var model = new VisualPropertySheetItemModel(dep, style, tableType, lexiconType, engine, lexicon);
			var sheetItem = new VisualPropertySheetItem(model, vizMapPropertyBuilder, servicesUtil);
			items.add(sheetItem);
		}
		
		return items;
	}
	
	private void updateItemsStatus() {
		// Children of enabled dependencies must be disabled
		var disabled = new HashSet<VisualProperty<?>>();
		var messages = new HashMap<VisualProperty<?>, String>();
		
		// MKTODO what about the current column style?
		var style = vmProxy.getCurrentNetworkVisualStyle();
		
		var infoMsgTemplate = "<html>To enable this visual property,<br><b>%s</b> the dependency <i><b>%s</b></i></html>";
		
		for (var dep : style.getAllVisualPropertyDependencies()) {
			var parent = dep.getParentVisualProperty();
			var properties = dep.getVisualProperties();
			
			if (dep.isDependencyEnabled()) {
				disabled.addAll(properties);
				
				for (var vp : properties)
					messages.put(vp, String.format(infoMsgTemplate, "uncheck", dep.getDisplayName()));
			} else {
				disabled.add(parent);
				messages.put(parent, String.format(infoMsgTemplate, "check", dep.getDisplayName()));
			}
		}
		
		for (var vpSheet : vizMapperMainPanel.getVisualPropertySheets()) {
			var vpSheetItems = vpSheet.getItems();
			
			for (var item : vpSheetItems) {
				// First check if this property item must be disabled and show an INFO message
				String msg = null;
				MessageType msgType = null;
				
				if (msgType == null && item.getModel().getVisualPropertyDependency() == null) {
					item.setEnabled(!disabled.contains(item.getModel().getVisualProperty()));
					msg = messages.get(item.getModel().getVisualProperty());
					msgType = item.isEnabled() ? null : MessageType.INFO;
				}
				
				item.setMessage(msg, msgType);
				
				// If item is enabled, check whether or not the mapping is valid for the current network
				updateMappingStatus(item);
			}
		}
	}
	
	private void updateVisibleItems(boolean reset) {
		if (reset)
			userProps.clear();
		
		for (var vpSheet : vizMapperMainPanel.getVisualPropertySheets()) {
			for (var item : vpSheet.getItems()) {
				// Items that are set visible by the user should still be visible when the current style changes.
				// Items hidden by the user will not be shown again when the current style changes,
				// unless it has a visual mapping:
				var set = defVisibleProps.get(item.getModel().getTargetDataType());
				var vpId = item.getModel().getId();
				
				// Start with the default properties,
				// but keep the ones previously hidden by the user invisible...
				boolean b = set != null && set.contains(vpId) && !Boolean.FALSE.equals(userProps.get(vpId));
				// ...but always show properties that have a mapping
				b = b || item.getModel().getVisualMappingFunction() != null;
				// ...or that were set visible by the user
				b = b || Boolean.TRUE.equals(userProps.get(vpId));
				
				item.setVisible(b);
			}
		}
	}
	
	private void setVisibleItems(VisualPropertySheet vpSheet, boolean visible) {
		userProps.clear();
		
		for (var item : vpSheet.getItems())
			item.setVisible(visible);
	}
	
	private void showDefaultItems(VisualPropertySheet vpSheet) {
		userProps.clear();
		
		for (var item : vpSheet.getItems()) {
			var set = defVisibleProps.get(item.getModel().getTargetDataType());
			var vpId = item.getModel().getId();
			
			// Start with the default properties, of course
			boolean b = set != null && set.contains(vpId);
			// ...but still show properties that have a mapping
			b = b || item.getModel().getVisualMappingFunction() != null;
			
			item.setVisible(b);
		}
	}
	
	private void saveDefaultVisibleItems(VisualPropertySheet vpSheet) {
		var idSet = new HashSet<String>();
		
		for (var item : vpSheet.getItems()) {
			if (item.isVisible())
				idSet.add(item.getModel().getId());
		}
		
		propsProxy.setDefaultVisualProperties(vpSheet.getModel().getLexiconType(), idSet);
		updateDefaultProps();
	}
	
	private void updateMappings(Class<? extends CyIdentifiable> targetDataType, CyTable table) {
		if (table != null) {
			var vpSheet = vizMapperMainPanel.getVisualPropertySheet(targetDataType);
			
			if (vpSheet != null) {
				var columns = table.getColumns();
				var colTypes = new HashMap<String, Class<?>>();
				
				for (var col : columns)
					colTypes.put(col.getName().toLowerCase(), col.getType());
					
				for (var item : vpSheet.getItems()) {
					var mapping = item.getModel().getVisualMappingFunction();
					
					// Passthrough mappings don't need to be updated
					if (mapping instanceof DiscreteMapping || mapping instanceof ContinuousMapping) {
						var colType = colTypes.get(mapping.getMappingColumnName().toLowerCase());
						
						if (colType != null && mapping.getMappingColumnType().isAssignableFrom(colType))
							invokeOnEDT(() -> item.updateMapping());
					}
				}
			}
		}
	}
	
	private void updateMappingStatus(VisualPropertySheetItem<?> item) {
		if (!item.isEnabled())
			return;
		
		var lexiconType = item.getModel().getLexiconType();
		var tableType = item.getModel().getTableType();
		
		if (lexiconType != CyNetwork.class) {
			String msg = null;
			MessageType msgType = null;
			var table = getCurrentMappingTable(tableType);
			
			if (table != null) {
				var columnProp = vizMapPropertyBuilder.getColumnProperty(item.getPropSheetPnl());
				var colName = (columnProp != null && columnProp.getValue() != null) ?
						columnProp.getValue().toString() : null;
				
				if (colName != null) {
					var mapping = item.getModel().getVisualMappingFunction();
					var mapColType = mapping != null ? mapping.getMappingColumnType() : null;
					var column = table.getColumn(colName);
					var colType = column != null ? column.getType() : null;
					
					// Ignore "List" type
					if (mapColType == List.class)
						mapColType = String.class;
					if (colType == List.class)
						colType = String.class;
					
					if (column == null || (mapColType != null && !mapColType.isAssignableFrom(colType))) {
						var tableName = table != null ? tableType.type().getSimpleName().replace("Cy", "") : null;
						msg = "<html>Visual Mapping cannot be applied to current network:<br>" + tableName +
								" table does not have column <b>\"" + colName + "\"</b>" +
								(mapColType != null ? " (" + mapColType.getSimpleName() + ")" : "") + "</html>";
						msgType = MessageType.WARNING;
					}
				}
			}
			
			var finalMsg = msg;
			var finalMsgType = msgType;
			
			invokeOnEDT(() -> item.setMessage(finalMsg, finalMsgType));
		}
	}
	
	private void updateLockedValues(CyNetworkView currentView) {
		if (currentView != null) {
			updateLockedValues(Collections.singleton((View<CyNetwork>) currentView), CyNetwork.class);
			updateLockedValues(vmProxy.getSelectedNodeViews(currentView), CyNode.class);
			updateLockedValues(vmProxy.getSelectedEdgeViews(currentView), CyEdge.class);
		} else {
			updateLockedValues(Collections.EMPTY_SET, CyNetwork.class);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private <S extends CyIdentifiable> void updateLockedValues(Set<View<S>> selectedViews, Class<S> targetDataType) {
		invokeOnEDT(() -> {
			var vpSheets = vizMapperMainPanel.getVisualPropertySheets();
			
			for (var sheet : vpSheets) {
				var vpItems = sheet.getItems();
				
				for (var item : vpItems) {
					var model = item.getModel();
					
					if (model.getTargetDataType() != targetDataType)
						continue;
					
					Set values = getDistinctLockedValues(model.getVisualProperty(), selectedViews);
					updateVpInfoLockedState(model, values, selectedViews);
				}
			}
		});
	}
	
	private <T, S extends CyIdentifiable> void updateVpInfoLockedState(VisualPropertySheetItemModel<T> model,
			   Set<T> lockedValues, Set<View<S>> selectedViews) {
		T value = null;
		var state = LockedValueState.DISABLED;

		if (lockedValues.size() == 1) {
			value = lockedValues.iterator().next();
			state = value == null ? LockedValueState.ENABLED_NOT_SET : LockedValueState.ENABLED_UNIQUE_VALUE;
		} else if (lockedValues.size() > 1) {
			state = LockedValueState.ENABLED_MULTIPLE_VALUES;
		}

		model.setLockedValue(value);
		model.setLockedValueState(state);
	}
	
	private <T, S extends CyIdentifiable> Set<T> getDistinctLockedValues(VisualProperty<T> vp, Set<View<S>> views) {
		var values = new HashSet<T>();

		for (var view : views) {
			if (view != null) {
				if (view.isValueLocked(vp))
					values.add(view.getVisualProperty(vp));
				else
					values.add(null); // To indicate that there is least one view without a locked value

				if (values.size() > 1) // For our current purposes, two values is the max we need
					break;
			}
		}

		return values;
	}
	
	private JMenuItem createMenuItem(CyAction action, Map<?, ?> properties) {
		var title = ServicePropertiesUtil.getTitle(properties);
		
		if (title == null)
			title = action.getName();
		
		if (title == null) {
			logger.error("Cannot create VizMapper menu item for: " + action + 
					"; \"" + ServicePropertiesUtil.TITLE +  "\" metadata is missing from properties: " + properties);
			return null;
		}
		
		var menuItem = new JMenuItem(action);
		menuItem.setText(title);
		
		double gravity = ServicePropertiesUtil.getGravity(properties);
		boolean insertSeparatorBefore = ServicePropertiesUtil.getInsertSeparatorBefore(properties);
		boolean insertSeparatorAfter = ServicePropertiesUtil.getInsertSeparatorAfter(properties);
		var menuId = ServicePropertiesUtil.getString(properties, ServicePropertiesUtil.MENU_ID, "");
		
		if (menuId.equals(ServicePropertiesUtil.CONTEXT_MENU))
			vizMapperMainPanel.addContextMenuItem(menuItem, gravity, insertSeparatorBefore, insertSeparatorAfter);
		else if(menuId.equals(ServicePropertiesUtil.TABLE_MAIN_MENU))
			vizMapperMainPanel.addTableOption(menuItem, gravity, insertSeparatorBefore, insertSeparatorAfter);
		else
			vizMapperMainPanel.addOption(menuItem, gravity, insertSeparatorBefore, insertSeparatorAfter);
		
		return menuItem;
	}
	
	@SuppressWarnings("rawtypes")
	private void openDefaultValueEditor(ActionEvent evt, VisualPropertySheetItem vpSheetItem) {
		var model = vpSheetItem.getModel();
		VisualProperty vp = model.getVisualProperty();

		var style = vpSheetItem.getModel().getVisualStyle();
		Object oldValue = style.getDefaultValue(vp);
		Object val = null;
		
		try {
			var editorMgr = servicesUtil.get(EditorManager.class);
			val = editorMgr.showVisualPropertyValueEditor(vizMapperMainPanel, vp, oldValue);
		} catch (Exception ex) {
			logger.error("Error opening Visual Property values editor for: " + vp, ex);
		}

		Object newValue = val;
		
		if (newValue != null && !newValue.equals(oldValue)) {
			style.setDefaultValue(vp, newValue);
			
			// Undo support
			var undo = servicesUtil.get(UndoSupport.class);
			undo.postEdit(new AbstractCyEdit("Set Default Value") {
				@Override
				public void undo() {
					style.setDefaultValue(vp, oldValue);
				}
				@Override
				public void redo() {
					style.setDefaultValue(vp, newValue);
				}
			});
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void openLockedValueEditor(ActionEvent evt, VisualPropertySheetItem vpSheetItem) {
		var model = vpSheetItem.getModel();
		var vp = model.getVisualProperty();
		
		Object curValue = model.getLockedValue();
		Object newValue = null;
		
		try {
			var editorMgr = servicesUtil.get(EditorManager.class);
			newValue = editorMgr.showVisualPropertyValueEditor(vizMapperMainPanel, vp, curValue);
		} catch (Exception ex) {
//			logger.error("Error opening Visual Property values editor for: " + vp, ex);
		}
		
		if (newValue != null && !newValue.equals(curValue)) {
			var vo = new LockedValuesVO((Map)Collections.singletonMap(vp, newValue));
			sendNotification(NotificationNames.SET_LOCKED_VALUES, vo);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private void removeLockedValue(ActionEvent e, VisualPropertySheetItem<?> vpSheetItem) {
		var visualProperty = vpSheetItem.getModel().getVisualProperty();
		var vo = new LockedValuesVO((Set)Collections.singleton(visualProperty));
		sendNotification(NotificationNames.REMOVE_LOCKED_VALUES, vo);
	}

	private void onSelectedVisualStyleChanged(PropertyChangeEvent e) {
		if (ignoreVisualStyleSelectedEvents)
			return;
		
		var newStyle = (VisualStyle) e.getNewValue();
		var oldStyle = vmProxy.getCurrentNetworkVisualStyle();
		
		if (!Objects.equals(newStyle, oldStyle)) {
			new Thread(() -> {
				// Update proxy
				vmProxy.setCurrentNetworkVisualStyle(newStyle);
				
				// Undo support
				var undo = servicesUtil.get(UndoSupport.class);
				undo.postEdit(new AbstractCyEdit("Set Current Style") {
					@Override
					public void undo() {
						vmProxy.setCurrentNetworkVisualStyle(oldStyle);
					}
					@Override
					public void redo() {
						vmProxy.setCurrentNetworkVisualStyle(newStyle);
					}
				});
			}).start();
		}
	}
	
	private void onDependencySelectionChanged(ItemEvent e, VisualPropertySheetItem<?> vpSheetItem) {
		boolean selected = e.getStateChange() == ItemEvent.SELECTED;
		var dep = vpSheetItem.getModel().getVisualPropertyDependency();
		dep.setDependency(selected);
		
		// Update VP Sheet Items
		invokeOnEDT(() -> updateItemsStatus());
	}
	
	private void onColumnChangedUpdateMappings(String colName, CyTable tbl) {
		var curNet = vmProxy.getCurrentNetwork();
		if (curNet == null)
			return;
		
		var vpSheets = new ArrayList<VisualPropertySheet>(2);
		
		if (tbl.equals(curNet.getDefaultEdgeTable())) {
			vpSheets.add(vizMapperMainPanel.getVisualPropertySheet(CyEdge.class));
			vpSheets.add(vizMapperMainPanel.getVisualPropertySheet(CyColumn.class));
		} else if (tbl.equals(curNet.getDefaultNodeTable())) {
			vpSheets.add(vizMapperMainPanel.getVisualPropertySheet(CyNode.class));
			vpSheets.add(vizMapperMainPanel.getVisualPropertySheet(CyColumn.class));
		} else if (tbl.equals(curNet.getDefaultNetworkTable())) {
			vpSheets.add(vizMapperMainPanel.getVisualPropertySheet(CyNetwork.class));
			vpSheets.add(vizMapperMainPanel.getVisualPropertySheet(CyColumn.class));
		}
		
		if (vpSheets != null && !vpSheets.isEmpty()) {
			for (var vpSheet : vpSheets) {
				// Update mapping status of this sheet's properties, if necessary
				if (vpSheet == null)
					continue;
				
				for (var item : vpSheet.getItems()) {
					var mapping = item.getModel().getVisualMappingFunction();
					if (mapping != null && mapping.getMappingColumnName().equalsIgnoreCase(colName))
						updateMappingStatus(item);
				}
			}
		}
	}
	
	private void showContextMenu(JPopupMenu contextMenu, MouseEvent e) {
		invokeOnEDT(() -> {
			var parent = (Component) e.getSource();
			contextMenu.show(parent, e.getX(), e.getY());
		});
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class ContextMenuMouseListener extends MouseAdapter {
		
		private VisualPropertySheet vpSheet;
		private VisualPropertySheetItem<?> vpSheetItem;
		
		ContextMenuMouseListener(VisualPropertySheet vpSheet, VisualPropertySheetItem<?> vpSheetItem) {
			this.vpSheet = vpSheet;
			this.vpSheetItem = vpSheetItem;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			maybeShowContextMenu(e, vpSheet, vpSheetItem);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowContextMenu(e, vpSheet, vpSheetItem);
		}
		
		@SuppressWarnings("rawtypes")
		private void maybeShowContextMenu(MouseEvent e, VisualPropertySheet vpSheet,
				VisualPropertySheetItem<?> vpSheetItem) {
			if (!e.isPopupTrigger())
				return;
			
			// Select the right-clicked sheet item, if not selected yet
			if (!vpSheetItem.isSelected())
				vpSheet.setSelectedItems((Set) (Collections.singleton(vpSheetItem)));
			
			var contextMenu = vizMapperMainPanel.getContextMenu();
			
			invokeOnEDT(() -> {
				// Network properties don't have visual mappings
				var mapValueGeneratorsMenu = vizMapperMainPanel.getMapValueGeneratorsSubMenu();
				var targetDataType = vpSheet.getModel().getLexiconType();
				mapValueGeneratorsMenu.setVisible(targetDataType != CyNetwork.class);
				
				if (mapValueGeneratorsMenu.isVisible()) {
					// Add all mapping generators again, to keep a consistent order
					mapValueGeneratorsMenu.removeAll();
					Class<?> dataType = null; // will store the previous generator's data type
					
					for (var entry : mappingGenerators.entrySet()) {
						if (dataType != null && dataType != entry.getValue().getGenerator().getDataType())
							mapValueGeneratorsMenu.add(new JSeparator());
						
						mapValueGeneratorsMenu.add(entry.getValue());
						dataType = entry.getValue().getGenerator().getDataType();
					}
				}
				
				showContextMenu(contextMenu, e);
			});
		}
	}
}
