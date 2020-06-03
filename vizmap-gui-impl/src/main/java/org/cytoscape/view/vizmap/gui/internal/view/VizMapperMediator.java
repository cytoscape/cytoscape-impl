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
import org.cytoscape.view.vizmap.VisualPropertyDependency;
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
	
	private String curRendererId;
	
	private final VizMapperMainPanel vizMapperMainPanel;
	
	private final Map<TaskFactory, JMenuItem> taskFactories;
	private final Map<CyAction, JMenuItem> actions;
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public VizMapperMediator(final VizMapperMainPanel vizMapperMainPanel,
							 final ServicesUtil servicesUtil,
							 final VizMapPropertyBuilder vizMapPropertyBuilder) {
		super(NAME, vizMapperMainPanel, servicesUtil, vizMapPropertyBuilder, SHEET_TYPES);
		
		this.vizMapperMainPanel = vizMapperMainPanel;
		
		taskFactories = new HashMap<>();
		actions = new HashMap<>();
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
	protected VisualLexicon getVisualLexicon() {
		return vmProxy.getCurrentVisualLexicon();
	}
	
	@Override
	protected VisualStyle getVisualStyle() {
		return vmProxy.getCurrentVisualStyle();
	}
	
	@Override
	protected boolean isSupported(VisualProperty<?>	vp) {
		return vmProxy.isSupported(vp);
	}
	
	@Override
	protected boolean isSupported(VisualPropertyDependency<?> dep) {
		return vmProxy.isSupported(dep);
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

	
	private void initView() {
		servicesUtil.registerAllServices(vizMapperMainPanel, new Properties());
		addViewListeners();
	}
	
	private void addViewListeners() {
		// Switching the current Visual Style
		var stylesBtn = vizMapperMainPanel.getStylesBtn();
		stylesBtn.addPropertyChangeListener("selectedStyle", evt -> onSelectedVisualStyleChanged(evt));
	}
	

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
	
}
