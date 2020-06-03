package org.cytoscape.view.vizmap.gui.internal.view;

import static org.cytoscape.view.vizmap.gui.internal.view.util.ViewUtil.invokeOnEDT;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.DefaultVisualizableVisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.events.VisualMappingFunctionChangedEvent;
import org.cytoscape.view.vizmap.events.VisualMappingFunctionChangedListener;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
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
import org.cytoscape.view.vizmap.gui.internal.view.VisualPropertySheetItem.MessageType;
import org.cytoscape.view.vizmap.gui.util.DiscreteMappingGenerator;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.undo.AbstractCyEdit;
import org.cytoscape.work.undo.UndoSupport;
import org.puremvc.java.multicore.patterns.mediator.Mediator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractVizMapperMediator extends Mediator implements VisualMappingFunctionChangedListener {

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	protected final VizMapPropertyBuilder vizMapPropertyBuilder;
	protected final ServicesUtil servicesUtil;
	
	private final VisualPropertySheetContainer viewComponent;
	
	private final Map<String, Boolean> userProps;
	private final Map<Class<? extends CyIdentifiable>, Set<String>> defVisibleProps;
	
	private final Map<String, GenerateDiscreteValuesAction> mappingGenerators;
	
	private VisualPropertySheetItem<?> curVpSheetItem;
	private VizMapperProperty<?, ?, ?> curVizMapperProperty;
	
	protected VizMapperProxy vmProxy;
	protected AttributeSetProxy attrProxy;
	protected MappingFunctionFactoryProxy mappingFactoryProxy;
	protected PropsProxy propsProxy;
	
	private final Class<? extends CyIdentifiable>[] sheetTypes;
	
	public AbstractVizMapperMediator(
			String mediatorName, 
			VisualPropertySheetContainer viewComponent, 
			ServicesUtil servicesUtil, 
			VizMapPropertyBuilder vizMapPropertyBuilder,
			Class<? extends CyIdentifiable>[] sheetTypes
	) {
		super(mediatorName, viewComponent);
		this.servicesUtil = Objects.requireNonNull(servicesUtil, "'servicesUtil' must not be null");
		this.vizMapPropertyBuilder = Objects.requireNonNull(vizMapPropertyBuilder, "'vizMapPropertyBuilder' must not be null");
		this.viewComponent = Objects.requireNonNull(viewComponent, "'viewComponent' must not be null");
		
		this.sheetTypes = sheetTypes;
		
		userProps = new HashMap<>();
		defVisibleProps = new HashMap<>();
		
		final Collator collator = Collator.getInstance(Locale.getDefault());
		mappingGenerators = new TreeMap<>((s1, s2) -> {
			return collator.compare(s1, s2);
		});
	}
	
	
	abstract protected void updateMappingStatus(final VisualPropertySheetItem<?> item);
	
	abstract protected Collection<VisualProperty<?>> getVisualPropertyList(VisualLexicon lexicon);
	
	abstract protected Set<View<? extends CyIdentifiable>> getSelectedViews(Class<?> type);
	
	abstract protected VisualLexicon getVisualLexicon();
	
	abstract protected VisualStyle getVisualStyle();
	
	abstract protected boolean isSupported(VisualProperty<?> vp);
	
	abstract protected boolean isSupported(VisualPropertyDependency<?> dep);
	
	
	
	@Override
	public void onRegister() {
		vmProxy = (VizMapperProxy) getFacade().retrieveProxy(VizMapperProxy.NAME);
		attrProxy = (AttributeSetProxy) getFacade().retrieveProxy(AttributeSetProxy.NAME);
		mappingFactoryProxy = (MappingFunctionFactoryProxy) getFacade().retrieveProxy(MappingFunctionFactoryProxy.NAME);
		propsProxy = (PropsProxy) getFacade().retrieveProxy(PropsProxy.NAME);
		updateDefaultProps();
	}
	
	@Override
	public void handleEvent(final VisualMappingFunctionChangedEvent e) {
		final VisualMappingFunction<?, ?> vm = e.getSource();
		final VisualProperty<?> vp = vm.getVisualProperty();
		final VisualStyle curStyle = getVisualStyle();
		
		// If the source mapping belongs to the current visual style, update the correspondent property sheet item
		if (vm.equals(curStyle.getVisualMappingFunction(vp))) {
			final VisualPropertySheet vpSheet = viewComponent.getVisualPropertySheet(vp.getTargetDataType());
			
			if (vpSheet != null) {
				final VisualPropertySheetItem<?> vpSheetItem = vpSheet.getItem(vp);
				
				if (vpSheetItem != null)
					invokeOnEDT(() -> vpSheetItem.updateMapping());
			}
		}
	}
	
	
	public void onMappingGeneratorRegistered(final DiscreteMappingGenerator<?> generator, final Map<?, ?> properties) {
		final String serviceType = ServicePropertiesUtil.getServiceType(properties);
		
		if (serviceType == null) {
			logger.error("Cannot create VizMapper context menu item for: " + generator + 
					"; \"" + ServicePropertiesUtil.SERVICE_TYPE +  "\" metadata is missing from properties: " + properties);
			return;
		}

		// This is a menu item for Main Command Button.
		final String title = ServicePropertiesUtil.getTitle(properties);;
		
		if (title == null) {
			logger.error("Cannot create VizMapper context menu item for: " + generator + 
					"; \"" + ServiceProperties.TITLE +  "\" metadata is missing from properties: " + properties);
			return;
		}
		
		// Add new menu to the pull-down
		final GenerateDiscreteValuesAction action = new GenerateDiscreteValuesAction(title.toString(), generator, servicesUtil);
		viewComponent.getContextMenu().addPopupMenuListener(action);
		
		// Concatenate the data type with the title when setting the map key, so the generators
		// can be sorted first by data type and then by title.
		mappingGenerators.put(generator.getDataType().getSimpleName() + "::" + title.toString(), action);
	}

	public void onMappingGeneratorUnregistered(final DiscreteMappingGenerator<?> generator, final Map<?, ?> properties) {
		final Iterator<Entry<String, GenerateDiscreteValuesAction>> iter = mappingGenerators.entrySet().iterator();
		
		while (iter.hasNext()) {
			final Entry<String, GenerateDiscreteValuesAction> entry = iter.next();
			final GenerateDiscreteValuesAction action = entry.getValue();
			
			if (action.getGenerator().equals(generator)) {
				viewComponent.getContextMenu().removePopupMenuListener(action);
				iter.remove();
				break;
			}
		}
	}
	
	
	public VisualPropertySheetItem<?> getCurrentVisualPropertySheetItem() {
		return curVpSheetItem;
	}
	
	public VizMapperProperty<?, ?, ?> getCurrentVizMapperProperty() {
		return curVizMapperProperty;
	}
	
	public VisualPropertySheet getSelectedVisualPropertySheet() {
		return viewComponent.getSelectedVisualPropertySheet();
	}
	
	private void updateDefaultProps() {
		defVisibleProps.clear();
		for(Class<? extends CyIdentifiable> type : sheetTypes) {
			defVisibleProps.put(type, propsProxy.getDefaultVisualProperties(type));
		}
	}
	
	@SuppressWarnings("rawtypes")
	protected void updateVisualPropertySheets(final VisualStyle vs, final boolean resetDefaultVisibleItems, boolean rebuild) {
		if (vs == null)
			return;
		
		if (!rebuild) {
			// Also check if dependencies have changed
			final Map<String, VisualPropertyDependency<?>> map = new HashMap<>();
			final Set<VisualPropertyDependency<?>> dependencies = vs.getAllVisualPropertyDependencies();
			
			for (final VisualPropertyDependency<?> dep : dependencies) {
				final Class<? extends CyIdentifiable> type = dep.getParentVisualProperty().getTargetDataType();
				final VisualPropertySheet sheet = viewComponent.getVisualPropertySheet(type);
				
				if (sheet.getItem(dep) == null) {
					// There's a new dependency!
					rebuild = true;
					break;
				}
				
				map.put(dep.getIdString(), dep);
			}
			
			if (!rebuild) {
				final Set<VisualPropertySheet> vpSheets = viewComponent.getVisualPropertySheets();
				
				for (final VisualPropertySheet sheet : vpSheets) {
					for (final VisualPropertySheetItem<?> item : sheet.getItems()) {
						final VisualPropertyDependency<?> dep = item.getModel().getVisualPropertyDependency();
						
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
			createVisualPropertySheets(resetDefaultVisibleItems);
		} else {
			// Just update the current Visual Property sheets
			final Set<VisualPropertySheet> vpSheets = viewComponent.getVisualPropertySheets();
			
			for (final VisualPropertySheet sheet : vpSheets) {
				for (final VisualPropertySheetItem<?> item : sheet.getItems()) {
					// Update values
					final VisualPropertySheetItemModel model = item.getModel();
					// MKTODO need to parameterize the model class
					model.update(viewComponent.getRenderingEngine());
					
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
	
	private void createVisualPropertySheets(final boolean resetDefaultVisibleItems) {
		final VisualStyle style = getVisualStyle();
		final VisualLexicon lexicon = getVisualLexicon();
		
		invokeOnEDT(() -> {
			final VisualPropertySheet selVpSheet = getSelectedVisualPropertySheet();
			final Class<? extends CyIdentifiable> selectedTargetDataType = selVpSheet != null ?
					selVpSheet.getModel().getTargetDataType() : null;
			
			for (final Class<? extends CyIdentifiable> type : sheetTypes) {
				// Create Visual Property Sheet
				final VisualPropertySheetModel model = new VisualPropertySheetModel(type, style, lexicon);
				final VisualPropertySheet vpSheet = new VisualPropertySheet(model, servicesUtil);
				viewComponent.addVisualPropertySheet(vpSheet);
				
				// Create Visual Property Sheet Items
				final Set<VisualPropertySheetItem<?>> vpSheetItems = 
						createVisualPropertySheetItems(vpSheet.getModel().getTargetDataType(), lexicon, style);
				vpSheet.setItems(vpSheetItems);
				
				// Add event listeners to the new components
				addViewListeners(vpSheet);
				
				// Add more menu items to the Properties menu
				if (vpSheetItems.size() > 1) {
					vpSheet.getVpsMenu().add(new JSeparator());
					
					{
						final JMenuItem mi = new JMenuItem("Show Default");
						mi.addActionListener(evt -> showDefaultItems(vpSheet));
						vpSheet.getVpsMenu().add(mi);
					}
					{
						final JMenuItem mi = new JMenuItem("Show All");
						mi.addActionListener(evt -> setVisibleItems(vpSheet, true));
						vpSheet.getVpsMenu().add(mi);
					}
					{
						final JMenuItem mi = new JMenuItem("Hide All");
						mi.addActionListener(evt -> setVisibleItems(vpSheet, false));
						vpSheet.getVpsMenu().add(mi);
					}
				}
				
				vpSheet.getVpsMenu().add(new JSeparator());
				
				final JMenuItem mi = new JMenuItem("Make Default");
				mi.addActionListener(evt -> saveDefaultVisibleItems(vpSheet));
				vpSheet.getVpsMenu().add(mi);
			}
			
			updateVisibleItems(resetDefaultVisibleItems);
			updateItemsStatus();
			
			// Update panel's width
			int minWidth = 200;
			
			for (final VisualPropertySheet vpSheet : viewComponent.getVisualPropertySheets()) {
				minWidth = Math.max(minWidth, vpSheet.getMinimumSize().width);
			}
			
			// MKTODO
//			vizMapperMainPanel.setPreferredSize(
//					new Dimension(vizMapperMainPanel.getPropertiesPnl().getComponent().getMinimumSize().width + 20,
//								  vizMapperMainPanel.getPreferredSize().height));
			
			// Select the same sheet that was selected before
			final VisualPropertySheet vpSheet = viewComponent.getVisualPropertySheet(selectedTargetDataType);
			viewComponent.setSelectedVisualPropertySheet(vpSheet);
		});
	}
	
	
	@SuppressWarnings("rawtypes")
	private Set<VisualPropertySheetItem<?>> createVisualPropertySheetItems(final Class<? extends CyIdentifiable> type,
			final VisualLexicon lexicon, final VisualStyle style) {
		final Set<VisualPropertySheetItem<?>> items = new HashSet<>();
		
		if (lexicon == null || style == null)
			return items;
		
		final Collection<VisualProperty<?>> vpList = getVisualPropertyList(lexicon);
		
//		final CyNetworkView curNetView = vmProxy.getCurrentNetworkView();
//		final Set<View<CyNode>> selectedNodeViews = vmProxy.getSelectedNodeViews(curNetView);
//		final Set<View<CyEdge>> selectedEdgeViews = vmProxy.getSelectedEdgeViews(curNetView);
//		final Set<View<CyNetwork>> selectedNetViews = curNetView != null ?
//				Collections.singleton((View<CyNetwork>) curNetView) : Collections.EMPTY_SET;
		final RenderingEngine<?> engine = viewComponent.getRenderingEngine();
		
		Map<Class<?>,Set> selectedViewsCache = new HashMap<>();
		
		for (final VisualProperty<?> vp : vpList) {
			if (vp.getTargetDataType() != type || vp instanceof DefaultVisualizableVisualProperty)
				continue;
			if (!isSupported(vp))
				continue;
			
			// Create model
			final VisualPropertySheetItemModel<?> model = new VisualPropertySheetItemModel(vp, style, engine, lexicon);
			
			Set selectedViews = selectedViewsCache.computeIfAbsent(vp.getTargetDataType(), this::getSelectedViews);
			Set values = getDistinctLockedValues(vp, selectedViews);
			updateVpInfoLockedState(model, values, selectedViews);
//			
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
			
			// Create View
			final VisualPropertySheetItem<?> sheetItem = new VisualPropertySheetItem(model, vizMapPropertyBuilder, servicesUtil);
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
				final VisualStyle vs = model.getVisualStyle();
				vs.setDefaultValue((VisualProperty)vp, evt.getNewValue());
			});
			model.addPropertyChangeListener("visualMappingFunction", evt -> {
				final VisualStyle vs = model.getVisualStyle();
				
				if (evt.getNewValue() == null && vs.getVisualMappingFunction(vp) != null)
					vs.removeVisualMappingFunction(vp);
				else if (evt.getNewValue() != null && !evt.getNewValue().equals(vs.getVisualMappingFunction(vp)))
					vs.addVisualMappingFunction((VisualMappingFunction<?, ?>)evt.getNewValue());
				
				updateMappingStatus(sheetItem);
			});
		}
		
		// Add dependencies
		final Set<VisualPropertyDependency<?>> dependencies = style.getAllVisualPropertyDependencies();
		
		for (final VisualPropertyDependency<?> dep : dependencies) {
			if (dep.getParentVisualProperty().getTargetDataType() != type)
				continue;
			if (!isSupported(dep))
				continue;
			
			final VisualPropertySheetItemModel<?> model = new VisualPropertySheetItemModel(dep, style, engine, lexicon);
			final VisualPropertySheetItem<?> sheetItem = new VisualPropertySheetItem(model, vizMapPropertyBuilder, servicesUtil);
			items.add(sheetItem);
		}
		
		return items;
	}
	
	
	protected void updateItemsStatus() {
		// Children of enabled dependencies must be disabled
		final Set<VisualProperty<?>> disabled = new HashSet<>();
		final Map<VisualProperty<?>, String> messages = new HashMap<>();
		final VisualStyle style = getVisualStyle();
		
		final String infoMsgTemplate = 
				"<html>To enable this visual property,<br><b>%s</b> the dependency <i><b>%s</b></i></html>";
		
		for (final VisualPropertyDependency<?> dep : style.getAllVisualPropertyDependencies()) {
			final VisualProperty<?> parent = dep.getParentVisualProperty();
			final Set<VisualProperty<?>> properties = dep.getVisualProperties();
			
			if (dep.isDependencyEnabled()) {
				disabled.addAll(properties);
				
				for (final VisualProperty<?> vp : properties)
					messages.put(vp, String.format(infoMsgTemplate, "uncheck", dep.getDisplayName()));
			} else {
				disabled.add(parent);
				messages.put(parent, String.format(infoMsgTemplate, "check", dep.getDisplayName()));
			}
		}
		
		for (final VisualPropertySheet vpSheet : viewComponent.getVisualPropertySheets()) {
			final Set<VisualPropertySheetItem<?>> vpSheetItems = vpSheet.getItems();
			
			for (final VisualPropertySheetItem<?> item : vpSheetItems) {
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
	
	
	private void updateVisibleItems(final boolean reset) {
		if (reset)
			userProps.clear();
		
		for (final VisualPropertySheet vpSheet : viewComponent.getVisualPropertySheets()) {
			for (final VisualPropertySheetItem<?> item : vpSheet.getItems()) {
				// Items that are set visible by the user should still be visible when the current style changes.
				// Items hidden by the user will not be shown again when the current style changes,
				// unless it has a visual mapping:
				final Set<String> set = defVisibleProps.get(item.getModel().getTargetDataType());
				final String vpId = item.getModel().getId();
				
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
		
		for (final VisualPropertySheetItem<?> item : vpSheet.getItems())
			item.setVisible(visible);
	}
	
	private void showDefaultItems(VisualPropertySheet vpSheet) {
		userProps.clear();
		
		for (final VisualPropertySheetItem<?> item : vpSheet.getItems()) {
			final Set<String> set = defVisibleProps.get(item.getModel().getTargetDataType());
			final String vpId = item.getModel().getId();
			
			// Start with the default properties, of course
			boolean b = set != null && set.contains(vpId);
			// ...but still show properties that have a mapping
			b = b || item.getModel().getVisualMappingFunction() != null;
			
			item.setVisible(b);
		}
	}
	
	
	private void saveDefaultVisibleItems(final VisualPropertySheet vpSheet) {
		final Set<String> idSet = new HashSet<>();
		
		for (final VisualPropertySheetItem<?> item : vpSheet.getItems()) {
			if (item.isVisible())
				idSet.add(item.getModel().getId());
		}
		
		propsProxy.setDefaultVisualProperties(vpSheet.getModel().getTargetDataType(), idSet);
		updateDefaultProps();
	}
	
	protected void updateMappings(final Class<? extends CyIdentifiable> targetDataType, final CyTable table) {
		if (table != null) {
			final VisualPropertySheet vpSheet = viewComponent.getVisualPropertySheet(targetDataType);
			
			if (vpSheet != null) {
				final Collection<CyColumn> columns = table.getColumns();
				final HashMap<String, Class<?>> colTypes = new HashMap<>();
				
				for (final CyColumn col : columns)
					colTypes.put(col.getName().toLowerCase(), col.getType());
					
				for (final VisualPropertySheetItem<?> item : vpSheet.getItems()) {
					final VisualMappingFunction<?, ?> mapping = item.getModel().getVisualMappingFunction();
					
					// Passthrough mappings don't need to be updated
					if (mapping instanceof DiscreteMapping || mapping instanceof ContinuousMapping) {
						final Class<?> colType = colTypes.get(mapping.getMappingColumnName().toLowerCase());
						
						if (colType != null && mapping.getMappingColumnType().isAssignableFrom(colType))
							invokeOnEDT(() -> item.updateMapping());
					}
				}
			}
		}
	}
	
	
//	protected void updateMappingStatus(final VisualPropertySheetItem<?> item) {
//		if (!item.isEnabled())
//			return;
//		
//		final CyNetwork net = vmProxy.getCurrentNetwork();
//		final Class<? extends CyIdentifiable> targetDataType = item.getModel().getTargetDataType();
//		
//		if (net != null && targetDataType != CyNetwork.class) {
//			final CyTable netTable = targetDataType == CyNode.class ? net.getDefaultNodeTable() : net.getDefaultEdgeTable();
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
	
	
//	protected void updateLockedValues(final CyNetworkView currentView) {
//		if (currentView != null) {
//			updateLockedValues(Collections.singleton((View<CyNetwork>)currentView), CyNetwork.class);
//			updateLockedValues(vmProxy.getSelectedNodeViews(currentView), CyNode.class);
//			updateLockedValues(vmProxy.getSelectedEdgeViews(currentView), CyEdge.class);
//		} else {
//			updateLockedValues(Collections.EMPTY_SET, CyNetwork.class);
//		}
//	}
	
	@SuppressWarnings("rawtypes")
	public <S extends CyIdentifiable> void updateLockedValues(Set<View<S>> selectedViews, Class<S> targetDataType) {
		invokeOnEDT(() -> {
			final Set<VisualPropertySheet> vpSheets = viewComponent.getVisualPropertySheets();
			
			for (VisualPropertySheet sheet : vpSheets) {
				final Set<VisualPropertySheetItem<?>> vpItems = sheet.getItems();
				
				for (final VisualPropertySheetItem<?> item : vpItems) {
					final VisualPropertySheetItemModel<?> model = item.getModel();
					
					if (model.getTargetDataType() != targetDataType)
						continue;
					
					final Set values = getDistinctLockedValues(model.getVisualProperty(), selectedViews);
					updateVpInfoLockedState(model, values, selectedViews);
				}
			}
		});
	}
	
	private <T, S extends CyIdentifiable> void updateVpInfoLockedState(final VisualPropertySheetItemModel<T> model,
			   final Set<T> lockedValues, final Set<View<S>> selectedViews) {
		T value = null;
		LockedValueState state = LockedValueState.DISABLED;

		if (lockedValues.size() == 1) {
			value = lockedValues.iterator().next();
			state = value == null ? LockedValueState.ENABLED_NOT_SET : LockedValueState.ENABLED_UNIQUE_VALUE;
		} else if (lockedValues.size() > 1) {
			state = LockedValueState.ENABLED_MULTIPLE_VALUES;
		}

		model.setLockedValue(value);
		model.setLockedValueState(state);
	}
	
	private <T, S extends CyIdentifiable> Set<T> getDistinctLockedValues(final VisualProperty<T> vp, final Set<View<S>> views) {
		final Set<T> values = new HashSet<>();

		for (final View<S> view : views) {
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
	
	private void addViewListeners(final VisualPropertySheet vpSheet) {
		for (var vpSheetItem : vpSheet.getItems())
			addViewListeners(vpSheet, vpSheetItem);
	}

	private void addViewListeners(final VisualPropertySheet vpSheet, final VisualPropertySheetItem<?> vpSheetItem) {
		if (vpSheetItem.getModel().getVisualPropertyDependency() == null) {
			// It's a regular VisualProperty Editor...
			
			// Default value button clicked
			vpSheetItem.getDefaultBtn().addActionListener(evt -> openDefaultValueEditor(evt, vpSheetItem));
			
			// Default value button right-clicked
			vpSheetItem.getDefaultBtn().addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(final MouseEvent e) {
					maybeShowContextMenu(e);
				}
				@Override
				public void mouseReleased(final MouseEvent e) {
					maybeShowContextMenu(e);
				}
				private void maybeShowContextMenu(final MouseEvent e) {
					if (e.isPopupTrigger()) {
						final JPopupMenu contextMenu = new JPopupMenu();
						contextMenu.add(new JMenuItem(new AbstractAction("Reset Default Value") {
							@Override
							public void actionPerformed(final ActionEvent e) {
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
				final JPopupMenu bypassMenu = new JPopupMenu();
				final JMenuItem removeBypassMenuItem;
				
				bypassMenu.add(new JMenuItem(new AbstractAction("Set Bypass...") {
					@Override
					public void actionPerformed(final ActionEvent e) {
						openLockedValueEditor(e, vpSheetItem);
					}
				}));
				bypassMenu.add(removeBypassMenuItem = new JMenuItem(new AbstractAction("Remove Bypass") {
					@Override
					public void actionPerformed(final ActionEvent e) {
						removeLockedValue(e, vpSheetItem);
					}
				}));
				
				// Right-clicked
				vpSheetItem.getBypassBtn().addMouseListener(new MouseAdapter() {
					@Override
					public void mousePressed(final MouseEvent e) {
						maybeShowContextMenu(e);
					}
					@Override
					public void mouseReleased(final MouseEvent e) {
						maybeShowContextMenu(e);
					}
					private void maybeShowContextMenu(final MouseEvent e) {
						if (vpSheetItem.getBypassBtn().isEnabled() && e.isPopupTrigger()) {
							final LockedValueState state = vpSheetItem.getModel().getLockedValueState();
							removeBypassMenuItem.setEnabled(state != LockedValueState.ENABLED_NOT_SET);
							showContextMenu(bypassMenu, e);
						}
					}
				});
				
				// Left-clicked
				vpSheetItem.getBypassBtn().addActionListener(evt -> {
					final LockedValueState state = vpSheetItem.getModel().getLockedValueState();
					final JButton btn = vpSheetItem.getBypassBtn();
					
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
			final ContextMenuMouseListener cmMouseListener = new ContextMenuMouseListener(vpSheet, vpSheetItem);
			vpSheetItem.addMouseListener(cmMouseListener);
			
			if (vpSheetItem.getModel().isVisualMappingAllowed()) {
				vpSheetItem.getPropSheetPnl().getTable().addMouseListener(cmMouseListener);
				vpSheetItem.getRemoveMappingBtn().addActionListener(evt -> removeVisualMapping(vpSheetItem));
				vpSheetItem.getPropSheetTbl().addPropertyChangeListener("editingVizMapperProperty", evt -> {
					curVpSheetItem = vpSheetItem; // Save the current editor (the one the user is interacting with)
					curVizMapperProperty = (VizMapperProperty<?, ?, ?>) evt.getNewValue();
					
					final VizMapperProperty<String, VisualMappingFunctionFactory, VisualMappingFunction<?, ?>> mappingTypeProperty = 
							vizMapPropertyBuilder.getMappingTypeProperty(vpSheetItem.getPropSheetPnl());
					final VisualMappingFunctionFactory factory = (VisualMappingFunctionFactory) mappingTypeProperty.getValue();
					attrProxy.setCurrentMappingType(factory != null ? factory.getMappingFunctionType() : null);
					
					final VizMapperProperty<VisualProperty<?>, String, VisualMappingFunctionFactory> columnProp = 
							vizMapPropertyBuilder.getColumnProperty(vpSheetItem.getPropSheetPnl());
					final Object columnValue = columnProp.getValue();
					mappingFactoryProxy.setCurrentColumnName(columnValue != null ? columnValue.toString() : null);
					mappingFactoryProxy.setCurrentTargetDataType(vpSheet.getModel().getTargetDataType());
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
			public void componentShown(final ComponentEvent e) {
				userProps.put(vpSheetItem.getModel().getId(), Boolean.TRUE);
			}
			@Override
			public void componentHidden(final ComponentEvent e) {
				userProps.put(vpSheetItem.getModel().getId(), Boolean.FALSE);
			}
		});
	}

	protected void removeVisualMapping(final VisualPropertySheetItem<?> vpSheetItem) {
		final VisualMappingFunction<?, ?> vm = vpSheetItem.getModel().getVisualMappingFunction();
		
		if (vm != null)
			sendNotification(NotificationNames.REMOVE_VISUAL_MAPPINGS, Collections.singleton(vm));
	}
	
	
	@SuppressWarnings("rawtypes")
	private void openDefaultValueEditor(final ActionEvent evt, final VisualPropertySheetItem vpSheetItem) {
		final VisualPropertySheetItemModel model = vpSheetItem.getModel();
		final VisualProperty vp = model.getVisualProperty();

		final VisualStyle style = getVisualStyle();
		final Object oldValue = style.getDefaultValue(vp);
		Object val = null;
		
		try {
			final EditorManager editorMgr = servicesUtil.get(EditorManager.class);
			val = editorMgr.showVisualPropertyValueEditor(viewComponent.getComponent(), vp, oldValue);
		} catch (final Exception ex) {
			logger.error("Error opening Visual Property values editor for: " + vp, ex);
		}

		final Object newValue = val;
		
		if (newValue != null && !newValue.equals(oldValue)) {
			style.setDefaultValue(vp, newValue);
			
			// Undo support
			final UndoSupport undo = servicesUtil.get(UndoSupport.class);
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
	private void openLockedValueEditor(final ActionEvent evt, final VisualPropertySheetItem vpSheetItem) {
		final VisualPropertySheetItemModel model = vpSheetItem.getModel();
		final VisualProperty vp = model.getVisualProperty();
		
		final Object curValue = model.getLockedValue();
		Object newValue = null;
		
		try {
			final EditorManager editorMgr = servicesUtil.get(EditorManager.class);
			newValue = editorMgr.showVisualPropertyValueEditor(viewComponent.getComponent(), vp, curValue);
		} catch (Exception ex) {
//			logger.error("Error opening Visual Property values editor for: " + vp, ex);
		}
		
		if (newValue != null && !newValue.equals(curValue)) {
			final LockedValuesVO vo = new LockedValuesVO((Map)Collections.singletonMap(vp, newValue));
			sendNotification(NotificationNames.SET_LOCKED_VALUES, vo);
		}
	}
	
	
	@SuppressWarnings("rawtypes")
	private void removeLockedValue(final ActionEvent e, final VisualPropertySheetItem<?> vpSheetItem) {
		final VisualProperty<?> visualProperty = vpSheetItem.getModel().getVisualProperty();
		final LockedValuesVO vo = new LockedValuesVO((Set)Collections.singleton(visualProperty));
		sendNotification(NotificationNames.REMOVE_LOCKED_VALUES, vo);
	}
	
	private void onDependencySelectionChanged(final ItemEvent e, final VisualPropertySheetItem<?> vpSheetItem) {
		final boolean selected = e.getStateChange() == ItemEvent.SELECTED;
		final VisualPropertyDependency<?> dep = vpSheetItem.getModel().getVisualPropertyDependency();
		dep.setDependency(selected);
		
		// Update VP Sheet Items
		invokeOnEDT(() -> updateItemsStatus());
	}
	
	private void showContextMenu(final JPopupMenu contextMenu, final MouseEvent e) {
		invokeOnEDT(() -> {
			final Component parent = (Component) e.getSource();
			contextMenu.show(parent, e.getX(), e.getY());
		});
	}
	
	// ==[ CLASSES ]====================================================================================================
	
	private class ContextMenuMouseListener extends MouseAdapter {
		
		private VisualPropertySheet vpSheet;
		private VisualPropertySheetItem<?> vpSheetItem;
		
		ContextMenuMouseListener(final VisualPropertySheet vpSheet,
				final VisualPropertySheetItem<?> vpSheetItem) {
			this.vpSheet = vpSheet;
			this.vpSheetItem = vpSheetItem;
		}

		@Override
		public void mousePressed(final MouseEvent e) {
			maybeShowContextMenu(e, vpSheet, vpSheetItem);
		}

		@Override
		public void mouseReleased(final MouseEvent e) {
			maybeShowContextMenu(e, vpSheet, vpSheetItem);
		}
		
		@SuppressWarnings("rawtypes")
		private void maybeShowContextMenu(final MouseEvent e, final VisualPropertySheet vpSheet, 
				final VisualPropertySheetItem<?> vpSheetItem) {
			if (!e.isPopupTrigger())
				return;
			
			// Select the right-clicked sheet item, if not selected yet
			if (!vpSheetItem.isSelected())
				vpSheet.setSelectedItems((Set) (Collections.singleton(vpSheetItem)));
			
			final JPopupMenu contextMenu = viewComponent.getContextMenu();
			
			invokeOnEDT(() -> {
				// Network properties don't have visual mappings
				final JMenu mapValueGeneratorsMenu = viewComponent.getMapValueGeneratorsSubMenu();
				final Class<? extends CyIdentifiable> targetDataType = vpSheet.getModel().getTargetDataType();
				
				// MKTODO is it ok to directly reference CyNetwork here
				mapValueGeneratorsMenu.setVisible(targetDataType != CyNetwork.class);
				
				if (mapValueGeneratorsMenu.isVisible()) {
					// Add all mapping generators again, to keep a consistent order
					mapValueGeneratorsMenu.removeAll();
					Class<?> dataType = null; // will store the previous generator's data type
					
					for (final Entry<String, GenerateDiscreteValuesAction> entry : mappingGenerators.entrySet()) {
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
