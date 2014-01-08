package org.cytoscape.view.vizmap.gui.internal.view;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;
import static org.cytoscape.view.vizmap.gui.internal.util.NotificationNames.CURRENT_NETWORK_VIEW_CHANGED;
import static org.cytoscape.view.vizmap.gui.internal.util.NotificationNames.CURRENT_VISUAL_STYLE_CHANGED;
import static org.cytoscape.view.vizmap.gui.internal.util.NotificationNames.VISUAL_STYLE_NAME_CHANGED;
import static org.cytoscape.view.vizmap.gui.internal.util.NotificationNames.VISUAL_STYLE_SET_CHANGED;
import static org.cytoscape.view.vizmap.gui.internal.util.NotificationNames.VISUAL_STYLE_UPDATED;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuListener;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.ColumnNameChangedEvent;
import org.cytoscape.model.events.ColumnNameChangedListener;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.UpdateNetworkPresentationEvent;
import org.cytoscape.view.model.events.UpdateNetworkPresentationListener;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.DefaultVisualizableVisualProperty;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.events.VisualMappingFunctionChangedEvent;
import org.cytoscape.view.vizmap.events.VisualMappingFunctionChangedListener;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.event.LexiconStateChangedEvent;
import org.cytoscape.view.vizmap.gui.event.LexiconStateChangedListener;
import org.cytoscape.view.vizmap.gui.internal.VizMapperProperty;
import org.cytoscape.view.vizmap.gui.internal.action.GenerateDiscreteValuesAction;
import org.cytoscape.view.vizmap.gui.internal.model.AttributeSetProxy;
import org.cytoscape.view.vizmap.gui.internal.model.LockedValueState;
import org.cytoscape.view.vizmap.gui.internal.model.LockedValuesVO;
import org.cytoscape.view.vizmap.gui.internal.model.MappingFunctionFactoryProxy;
import org.cytoscape.view.vizmap.gui.internal.model.PropsProxy;
import org.cytoscape.view.vizmap.gui.internal.model.VizMapperProxy;
import org.cytoscape.view.vizmap.gui.internal.theme.ThemeManager;
import org.cytoscape.view.vizmap.gui.internal.util.NotificationNames;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.view.VisualPropertySheetItem.MessageType;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperMainPanel.VisualStyleDropDownButton;
import org.cytoscape.view.vizmap.gui.util.DiscreteMappingGenerator;
import org.cytoscape.view.vizmap.gui.util.PropertySheetUtil;
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

@SuppressWarnings({"unchecked", "serial"})
public class VizMapperMediator extends Mediator implements LexiconStateChangedListener, RowsSetListener, 
														   ColumnCreatedListener, ColumnDeletedListener,
														   ColumnNameChangedListener, UpdateNetworkPresentationListener,
														   VisualMappingFunctionChangedListener {

	public static final String NAME = "VizMapperMediator";
	
	private static final Class<? extends CyIdentifiable>[] SHEET_TYPES = 
			new Class[] { CyNode.class, CyEdge.class, CyNetwork.class };
	
	private static final String METADATA_MENU_KEY = "menu";
	private static final String METADATA_TITLE_KEY = "title";

	private static final String MAIN_MENU = "main";
	private static final String CONTEXT_MENU = "context";
	
	private VizMapperProxy vmProxy;
	private AttributeSetProxy attrProxy;
	private MappingFunctionFactoryProxy mappingFactoryProxy;
	private PropsProxy propsProxy;
	
	private boolean ignoreVisualStyleSelectedEvents;
	
	private VisualPropertySheetItem<?> curVpSheetItem;
	private VizMapperProperty<?, ?, ?> curVizMapperProperty;
	private CyNetworkView previewNetView;
	
	private final ServicesUtil servicesUtil;
	private final VizMapperMainPanel vizMapperMainPanel;
	private final VizMapPropertyBuilder vizMapPropertyBuilder;
	private final ThemeManager themeMgr;
	
	private final Map<DiscreteMappingGenerator<?>, JMenuItem> mappingGenerators;
	private final Map<TaskFactory, JMenuItem> taskFactories;
	
	/** IDs of property sheet items that were set visible/invisible by the user */
	private final Map<String, Boolean> userProps;
	
	private final Map<Class<? extends CyIdentifiable>, Set<String>> defVisibleProps;

	private static final Logger logger = LoggerFactory.getLogger(VizMapperMediator.class);

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public VizMapperMediator(final VizMapperMainPanel vizMapperMainPanel,
							 final ServicesUtil servicesUtil,
							 final VizMapPropertyBuilder vizMapPropertyBuilder,
							 final ThemeManager themeMgr) {
		super(NAME, vizMapperMainPanel);
		
		if (vizMapperMainPanel == null)
			throw new IllegalArgumentException("'vizMapperMainPanel' must not be null");
		if (servicesUtil == null)
			throw new IllegalArgumentException("'servicesUtil' must not be null");
		if (vizMapPropertyBuilder == null)
			throw new IllegalArgumentException("'vizMapPropertyBuilder' must not be null");
		if (themeMgr == null)
			throw new IllegalArgumentException("'themeMgr' must not be null");
		
		this.vizMapperMainPanel = vizMapperMainPanel;
		this.servicesUtil = servicesUtil;
		this.vizMapPropertyBuilder = vizMapPropertyBuilder;
		this.themeMgr = themeMgr;
		
		mappingGenerators = new HashMap<DiscreteMappingGenerator<?>, JMenuItem>();
		taskFactories = new HashMap<TaskFactory, JMenuItem>();
		userProps = new HashMap<String, Boolean>();
		defVisibleProps = new HashMap<Class<? extends CyIdentifiable>, Set<String>>();
		
		setViewComponent(vizMapperMainPanel);
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public final void onRegister() {
		vmProxy = (VizMapperProxy) getFacade().retrieveProxy(VizMapperProxy.NAME);
		attrProxy = (AttributeSetProxy) getFacade().retrieveProxy(AttributeSetProxy.NAME);
		mappingFactoryProxy = (MappingFunctionFactoryProxy) getFacade().retrieveProxy(MappingFunctionFactoryProxy.NAME);
		propsProxy = (PropsProxy) getFacade().retrieveProxy(PropsProxy.NAME);
		
		initDefaultProps();
		initView();
		super.onRegister();
	}
	
	@Override
	public String[] listNotificationInterests() {
		return new String[]{ VISUAL_STYLE_SET_CHANGED,
							 CURRENT_VISUAL_STYLE_CHANGED,
							 VISUAL_STYLE_UPDATED,
							 CURRENT_NETWORK_VIEW_CHANGED,
							 VISUAL_STYLE_NAME_CHANGED };
	}
	
	@Override
	public void handleNotification(final INotification notification) {
		final String id = notification.getName();
		final Object body = notification.getBody();
		
		if (id.equals(VISUAL_STYLE_SET_CHANGED)) {
			updateVisualStyleList((SortedSet<VisualStyle>) body);
		} else if (id.equals(CURRENT_VISUAL_STYLE_CHANGED)) {
			final Thread thread = new Thread() {
				// Create a new Thread to prevent invokeAndWait being called from the EventDispatchThread
				public void run() {
					try {
						SwingUtilities.invokeAndWait(new Runnable() {
							@Override
							public void run() {
								ignoreVisualStyleSelectedEvents = true;
								selectCurrentVisualStyle((VisualStyle) body);
								ignoreVisualStyleSelectedEvents = false;
							}
						});
						
						updateVisualPropertySheets((VisualStyle) body);
					} catch (InterruptedException e) {
						logger.error("Error selecting current Visual Style", e);
					} catch (InvocationTargetException e) {
						logger.error("Error selecting current Visual Style", e);
					}
			     }
			};
			thread.start();
		} else if (id.equals(VISUAL_STYLE_UPDATED) && body != null && body.equals(vmProxy.getCurrentVisualStyle())) {
			updateVisualPropertySheets((VisualStyle) body);
		} else if (id.equals(CURRENT_NETWORK_VIEW_CHANGED)) {
			final CyNetworkView view = (CyNetworkView) body;
			
			// Ignore it, if the selected style is not the current one,
			// because it should change the selection style first and then recreate all the items, anyway.
			if (view == null || vmProxy.getVisualStyle(view).equals(vizMapperMainPanel.getSelectedVisualStyle())) {
				updateLockedValues((CyNetworkView) body);
				
				if (body instanceof CyNetworkView) {
					updateMappings(CyNode.class, view.getModel().getDefaultNodeTable());
					updateMappings(CyEdge.class, view.getModel().getDefaultEdgeTable());
				}
				
				updateItemsStatus();
			}
		} else if (id.equals(VISUAL_STYLE_NAME_CHANGED)) {
			vizMapperMainPanel.getStylesBtn().repaint();
		}
	}

	@Override
	public void handleEvent(final LexiconStateChangedEvent e) {
		// Update Network Views
		final VisualStyle curStyle = vmProxy.getCurrentVisualStyle();
		final Set<CyNetworkView> views = vmProxy.getNetworkViewsWithStyle(curStyle);
		
		for (final CyNetworkView view : views) {
			curStyle.apply(view);
			view.updateView();
		}
		
		// Update VP Sheet Items
		invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				updateItemsStatus();
			}
		});
	}
	
	@Override
	public void handleEvent(final RowsSetEvent e) {
		final CyTable tbl = e.getSource();
		
		// Update bypass buttons--check selected nodes and edges of the current view
		final CyNetworkView curNetView = vmProxy.getCurrentNetworkView();
		
		if (curNetView != null && !e.getColumnRecords(CyNetwork.SELECTED).isEmpty()) {
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
				final Collection<RowSetRecord> payloadCollection = e.getPayloadCollection();
				
				for (final VisualPropertySheetItem<?> item : vpSheet.getItems()) {
					final VisualMappingFunction<?, ?> mapping = item.getModel().getVisualMappingFunction();
					
					if (mapping != null) {
						for (final RowSetRecord record : payloadCollection) {
							if (mapping.getMappingColumnName().equalsIgnoreCase(record.getColumn())) {
								invokeOnEDT(new Runnable() {
									@Override
									public void run() {
										item.updateMapping();
									}
								});
								
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
	public void handleEvent(final VisualMappingFunctionChangedEvent e) {
		final VisualMappingFunction<?, ?> vm = e.getSource();
		final VisualProperty<?> vp = vm.getVisualProperty();
		final VisualStyle curStyle = vmProxy.getCurrentVisualStyle();
		
		// If the source mapping belongs to the current visual style, update the correspondent property sheet item
		if (vm.equals(curStyle.getVisualMappingFunction(vp))) {
			final VisualPropertySheet vpSheet = vizMapperMainPanel.getVisualPropertySheet(vp.getTargetDataType());
			
			if (vpSheet != null) {
				final VisualPropertySheetItem<?> vpSheetItem = vpSheet.getItem(vp);
				
				if (vpSheetItem != null) {
					invokeOnEDT(new Runnable() {
						@Override
						public void run() {
							vpSheetItem.updateMapping();
						}
					});
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
	public synchronized void onCyActionRegistered(final CyAction action, final Map<?, ?> properties) {
		final Object serviceType = properties.get("service.type");
		
		if (serviceType != null && serviceType.toString().equals("vizmapUI.contextMenu")) {
			Object title = properties.get(METADATA_TITLE_KEY);
			
			if (title == null)
				title = action.getName();
				
			if (title == null) {
				logger.error("Cannot create VizMapper context menu item for: " + action + 
						"; \"" + METADATA_TITLE_KEY +  "\" metadata is missing from properties: " + properties);
				return;
			}

			final JMenuItem menuItem = new JMenuItem(action);
			menuItem.setText(title.toString());
			vizMapperMainPanel.getEditSubMenu().add(menuItem);
			vizMapperMainPanel.getContextMenu().addPopupMenuListener(action);
		}
	}

	/**
	 * Custom listener for removing unregistered VizMapper CyActions from the main menu.
	 */
	public synchronized void onCyActionUnregistered(final CyAction action, final Map<?, ?> properties) {
		final Component[] arr = vizMapperMainPanel.getEditSubMenu().getComponents();
		final List<Component> cList = arr != null ? Arrays.asList(arr) : new ArrayList<Component>();

		for (final Component c : cList) {
			if (c instanceof JMenuItem && ((JMenuItem)c).getAction().equals(action)) {
				vizMapperMainPanel.getEditSubMenu().remove(c);
				vizMapperMainPanel.getContextMenu().removePopupMenuListener(action);
			}
		}
	}
	
	/**
	 * Create menu items for related registered Task Factories.
	 */
	public void onTaskFactoryRegistered(final TaskFactory taskFactory, final Map<?, ?> properties) {
		// first filter the service...
		final Object serviceType = properties.get("service.type");
		
		if (serviceType == null || !(serviceType instanceof String)
				|| !((String) serviceType).equals("vizmapUI.taskFactory"))
			return;

		final Object menuDef = properties.get(METADATA_MENU_KEY);
		
		if (menuDef == null) {
			logger.error("Cannot create VizMapper context menu item for: " + taskFactory + 
					"; \"" + METADATA_MENU_KEY +  "\" metadata is missing from properties: " + properties);
			return;
		}

		// This is a menu item for Main Command Button.
		final Object title = properties.get(METADATA_TITLE_KEY);
		
		if (title == null) {
			logger.error("Cannot create VizMapper context menu item for: " + taskFactory + 
					"; \"" + METADATA_TITLE_KEY +  "\" metadata is missing from properties: " + properties);
			return;
		}

		final String menuKey = menuDef.toString();
		
		if (menuKey.equals(MAIN_MENU) || menuKey.equals(CONTEXT_MENU)) {
			// Add new menu to the pull-down
			final HashMap<String, String> config = new HashMap<String, String>();
			config.put(ServiceProperties.TITLE, title.toString());
			
			final AbstractCyAction action = new AbstractCyAction(config, taskFactory) {
				@Override
				public void actionPerformed(final ActionEvent e) {
					servicesUtil.get(DialogTaskManager.class).execute(taskFactory.createTaskIterator());
				}
			};
			final JMenuItem menuItem = new JMenuItem(action);
			
			if (menuKey.equals(MAIN_MENU)) {
				vizMapperMainPanel.getMainMenu().add(menuItem);
				vizMapperMainPanel.getMainMenu().addPopupMenuListener(action);
			} else if (menuKey.equals(CONTEXT_MENU)) {
				vizMapperMainPanel.getEditSubMenu().add(menuItem);
				vizMapperMainPanel.getContextMenu().addPopupMenuListener(action);
			}
			
			taskFactories.put(taskFactory, menuItem);
		}
	}

	public void onTaskFactoryUnregistered(final TaskFactory taskFactory, final Map<?, ?> properties) {
		final JMenuItem menuItem = taskFactories.remove(taskFactory);
		
		if (menuItem != null) {
			vizMapperMainPanel.getMainMenu().remove(menuItem);
			vizMapperMainPanel.getEditSubMenu().remove(menuItem);
			
			if (menuItem.getAction() instanceof PopupMenuListener) {
				vizMapperMainPanel.getMainMenu().removePopupMenuListener((PopupMenuListener)menuItem.getAction());
				vizMapperMainPanel.getContextMenu().removePopupMenuListener((PopupMenuListener)menuItem.getAction());
			}
		}
	}

	public void onMappingGeneratorRegistered(final DiscreteMappingGenerator<?> generator, final Map<?, ?> properties) {
		final Object serviceType = properties.get(METADATA_MENU_KEY);
		
		if (serviceType == null) {
			logger.error("Cannot create VizMapper context menu item for: " + generator + 
					"; \"" + METADATA_MENU_KEY +  "\" metadata is missing from properties: " + properties);
			return;
		}

		// This is a menu item for Main Command Button.
		final Object title = properties.get(METADATA_TITLE_KEY);
		
		if (title == null) {
			logger.error("Cannot create VizMapper context menu item for: " + generator + 
					"; \"" + METADATA_TITLE_KEY +  "\" metadata is missing from properties: " + properties);
			return;
		}
		
		// Add new menu to the pull-down
		final GenerateDiscreteValuesAction action = new GenerateDiscreteValuesAction(title.toString(), generator,
				servicesUtil);
		vizMapperMainPanel.getContextMenu().addPopupMenuListener(action);
		
		final JMenuItem menuItem = new JMenuItem(action);
		vizMapperMainPanel.getMapValueGeneratorsSubMenu().add(menuItem);
		mappingGenerators.put(generator, menuItem);
	}

	public void onMappingGeneratorUnregistered(final DiscreteMappingGenerator<?> generator, final Map<?, ?> properties) {
		final JMenuItem menuItem = mappingGenerators.remove(generator);
		
		if (menuItem != null) {
			vizMapperMainPanel.getMapValueGeneratorsSubMenu().remove(menuItem);
			
			if (menuItem.getAction() instanceof PopupMenuListener)
				vizMapperMainPanel.getContextMenu().removePopupMenuListener((PopupMenuListener)menuItem.getAction());
		}
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================

	private void initDefaultProps() {
		defVisibleProps.put(CyNode.class, propsProxy.getDefaultVisualProperties(CyNode.class));
		defVisibleProps.put(CyEdge.class, propsProxy.getDefaultVisualProperties(CyEdge.class));
		defVisibleProps.put(CyNetwork.class, propsProxy.getDefaultVisualProperties(CyNetwork.class));
	}
	
	private void initView() {
		createPreviewNetworkView();
		servicesUtil.registerAllServices(vizMapperMainPanel, new Properties());
		addViewListeners();
	}
	
	private void createPreviewNetworkView() {
		// Create dummy view first
		final CyNetwork net = servicesUtil.get(CyNetworkFactory.class)
				.createNetworkWithPrivateTables(SavePolicy.DO_NOT_SAVE);
		final CyNode source = net.addNode();
		final CyNode target = net.addNode();

		net.getRow(source).set(CyNetwork.NAME, "Source");
		net.getRow(target).set(CyNetwork.NAME, "Target");

		final CyEdge edge = net.addEdge(source, target, true);
		net.getRow(edge).set(CyNetwork.NAME, "Source (interaction) Target");

		net.getRow(net).set(CyNetwork.NAME, "Default Appearance");
		final CyNetworkView view = servicesUtil.get(CyNetworkViewFactory.class).createNetworkView(net);

		// Set node locations
		view.getNodeView(source).setVisualProperty(NODE_X_LOCATION, 0d);
		view.getNodeView(source).setVisualProperty(NODE_Y_LOCATION, 0d);
		view.getNodeView(target).setVisualProperty(NODE_X_LOCATION, 150d);
		view.getNodeView(target).setVisualProperty(NODE_Y_LOCATION, 20d);
		
		previewNetView = view;
	}
	
	private void addViewListeners() {
		// Switching the current Visual Style
		final VisualStyleDropDownButton stylesBtn = vizMapperMainPanel.getStylesBtn();
		stylesBtn.addPropertyChangeListener("selectedItem", new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent e) {
				onSelectedVisualStyleChanged(e);
			}
		});
	}
	
	private void addViewListeners(final VisualPropertySheet vpSheet) {
		for (final VisualPropertySheetItem<?> vpSheetItem : vpSheet.getItems())
			addViewListeners(vpSheet, vpSheetItem);
	}

	private void addViewListeners(final VisualPropertySheet vpSheet, final VisualPropertySheetItem<?> vpSheetItem) {
		if (vpSheetItem.getModel().getVisualPropertyDependency() == null) {
			// It's a regular VisualProperty Editor...
			// Default value button clicked
			vpSheetItem.getDefaultBtn().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					openDefaultValueEditor(e, vpSheetItem);
				}
			});
			
			// Bypass popup menu items
			if (vpSheetItem.getModel().isLockedValueAllowed()) {
				final JPopupMenu bypassMenu = new JPopupMenu();
				
				bypassMenu.add(new JMenuItem(new AbstractAction("Set Bypass...") {
					@Override
					public void actionPerformed(final ActionEvent e) {
						openLockedValueEditor(e, vpSheetItem);
					}
				}));
				bypassMenu.add(new JMenuItem(new AbstractAction("Remove Bypass") {
					@Override
					public void actionPerformed(final ActionEvent e) {
						removeLockedValue(e, vpSheetItem);
					}
				}));
				
				vpSheetItem.getBypassBtn().addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						final LockedValueState state = vpSheetItem.getModel().getLockedValueState();
						final JButton btn = vpSheetItem.getBypassBtn();
						
						if (state == LockedValueState.ENABLED_NOT_SET) {
							// There is only one option to execute, so do it now, rather than showing the popup menu
							openLockedValueEditor(e, vpSheetItem);
						} else {
							bypassMenu.show(btn, 0, btn.getHeight());
							bypassMenu.requestFocusInWindow();
						}
					}
				});
			}
			
			// Right-click
			final ContextMenuMouseListener cmMouseListener = new ContextMenuMouseListener(vpSheet, vpSheetItem);
			vpSheetItem.addMouseListener(cmMouseListener);
			
			if (vpSheetItem.getModel().isVisualMappingAllowed()) {
				vpSheetItem.getPropSheetPnl().getTable().addMouseListener(cmMouseListener);
				
				vpSheetItem.getRemoveMappingBtn().addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						removeVisualMapping(vpSheetItem);
					}
				});
				
				vpSheetItem.getPropSheetTbl().addPropertyChangeListener("editingVizMapperProperty", new PropertyChangeListener() {
					@Override
					public void propertyChange(final PropertyChangeEvent e) {
						curVpSheetItem = vpSheetItem; // Save the current editor (the one the user is interacting with)
						curVizMapperProperty = (VizMapperProperty<?, ?, ?>) e.getNewValue();
						
						final VizMapperProperty<String, VisualMappingFunctionFactory, VisualMappingFunction<?, ?>> mappingTypeProperty = 
								vizMapPropertyBuilder.getMappingTypeProperty(vpSheetItem.getPropSheetPnl());
						final VisualMappingFunctionFactory factory = (VisualMappingFunctionFactory) mappingTypeProperty.getValue();
						attrProxy.setCurrentMappingType(factory != null ? factory.getMappingFunctionType() : null);
						
						final VizMapperProperty<VisualProperty<?>, String, VisualMappingFunctionFactory> columnProp = 
								vizMapPropertyBuilder.getColumnProperty(vpSheetItem.getPropSheetPnl());
						final Object columnValue = columnProp.getValue();
						mappingFactoryProxy.setCurrentColumnName(columnValue != null ? columnValue.toString() : null);
						mappingFactoryProxy.setCurrentTargetDataType(vpSheet.getModel().getTargetDataType());
					}
				});
			}
		} else {
			// It's a Dependency Editor...
			vpSheetItem.getDependencyCkb().addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(final ItemEvent e) {
					onDependencySelectionChanged(e, vpSheetItem);
				}
			});
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

	private void updateVisualStyleList(final SortedSet<VisualStyle> styles) {
		attrProxy.setCurrentMappingType(null);
		mappingFactoryProxy.setCurrentColumnName(null);
		final RenderingEngineFactory<CyNetwork> engineFactory = vmProxy.getCurrentRenderingEngineFactory();
		
		invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				ignoreVisualStyleSelectedEvents = true;
				vizMapperMainPanel.updateVisualStyles(styles, previewNetView, engineFactory);
				final VisualStyle vs = vmProxy.getCurrentVisualStyle();
				selectCurrentVisualStyle(vs);
				updateVisualPropertySheets(vs);
				ignoreVisualStyleSelectedEvents = false;
			}
		});
	}
	
	private void selectCurrentVisualStyle(final VisualStyle vs) {
		invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				final VisualStyle selectedVs = vizMapperMainPanel.getSelectedVisualStyle();
				
				if (vs != null && !vs.equals(selectedVs))
					vizMapperMainPanel.setSelectedVisualStyle(vs);
			}
		});
	}
	
	@SuppressWarnings("rawtypes")
	private void updateVisualPropertySheets(final VisualStyle vs) {
		if (vs == null)
			return;
		
		final VisualPropertySheet curNetSheet = vizMapperMainPanel.getVisualPropertySheet(CyNetwork.class);
		final VisualPropertySheetModel curModel = curNetSheet != null ? curNetSheet.getModel() : null;
		final VisualStyle curStyle = curModel != null ? curModel.getVisualStyle() : null;
		
		boolean rebuild = !vs.equals(curStyle); // If a different style, rebuild all property sheets

		if (!rebuild) {
			// Also check if dependencies have changed
			final Map<String, VisualPropertyDependency<?>> map = new HashMap<String, VisualPropertyDependency<?>>();
			final Set<VisualPropertyDependency<?>> dependencies = vs.getAllVisualPropertyDependencies();
			
			for (final VisualPropertyDependency<?> dep : dependencies) {
				final Class<? extends CyIdentifiable> type = dep.getParentVisualProperty().getTargetDataType();
				final VisualPropertySheet sheet = vizMapperMainPanel.getVisualPropertySheet(type);
				
				if (sheet.getItem(dep) == null) {
					// There's a new dependency!
					rebuild = true;
					break;
				}
				
				map.put(dep.getIdString(), dep);
			}
			
			if (!rebuild) {
				final Set<VisualPropertySheet> vpSheets = vizMapperMainPanel.getVisualPropertySheets();
				
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
			createVisualPropertySheets();
		} else {
			// Just update the current Visual Property sheets
			final Set<VisualPropertySheet> vpSheets = vizMapperMainPanel.getVisualPropertySheets();
			
			for (final VisualPropertySheet sheet : vpSheets) {
				for (final VisualPropertySheetItem<?> item : sheet.getItems()) {
					// Update values
					final VisualPropertySheetItemModel model = item.getModel();
					model.update(vizMapperMainPanel.getRenderingEngine());
					
					if (model.getVisualPropertyDependency() != null)
						item.update();
				}
			}
		}
	}
	
	private void createVisualPropertySheets() {
		final Set<VisualPropertySheet> vpSheets = vizMapperMainPanel.getVisualPropertySheets();
		final Map<Class<? extends CyIdentifiable>, Set<String>> visibleProps = 
				new HashMap<Class<? extends CyIdentifiable>, Set<String>>();
		
		if (vpSheets.isEmpty()) {
			// First time
			visibleProps.putAll(defVisibleProps);
		} else {
			for (final VisualPropertySheet sheet : vpSheets) {
				for (VisualPropertySheetItem<?> item : sheet.getItems()) {
					// Keep the same items visible after updating the sheets
					if (item.isVisible()) {
						Set<String> set = visibleProps.get(item.getModel().getTargetDataType());
						
						if (set == null)
							visibleProps.put(item.getModel().getTargetDataType(), set = new HashSet<String>());
						
						set.add(item.getModel().getId());
					}
				}
			}
		}
		
		final VisualStyle style = vmProxy.getCurrentVisualStyle();
		final VisualLexicon lexicon = vmProxy.getCurrentVisualLexicon();
		
		invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				final VisualPropertySheet selVpSheet = getSelectedVisualPropertySheet();
				final Class<? extends CyIdentifiable> selectedTargetDataType = selVpSheet != null ?
						selVpSheet.getModel().getTargetDataType() : null;
				
				for (final Class<? extends CyIdentifiable> type : SHEET_TYPES) {
					// Create Visual Property Sheet
					final VisualPropertySheetModel model = new VisualPropertySheetModel(type, style, lexicon);
					final VisualPropertySheet vpSheet = new VisualPropertySheet(model, themeMgr);
					vizMapperMainPanel.addVisualPropertySheet(vpSheet);
					
					// Create Visual Property Sheet Items
					final Set<VisualPropertySheetItem<?>> vpSheetItems = 
							createVisualPropertySheetItems(vpSheet.getModel().getTargetDataType());
					vpSheet.setItems(vpSheetItems);
					
					// Add event listeners to the new components
					addViewListeners(vpSheet);
					
					// Add another menu item to the Properties menu
					vpSheet.getVpsMenu().add(new JSeparator());
					
					final JMenuItem mi = new JMenuItem("Make Default");
					mi.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(final ActionEvent e) {
							saveDefaultVisibleItems(vpSheet);
						}
					});
					vpSheet.getVpsMenu().add(mi);
				}
				
				updateVisibleItems();
				updateItemsStatus();
				
				// Update panel's width
				int minWidth = 200;
				
				for (final VisualPropertySheet vpSheet : vizMapperMainPanel.getVisualPropertySheets()) {
					minWidth = Math.max(minWidth, vpSheet.getMinimumSize().width);
				}
				
				vizMapperMainPanel.setPreferredSize(
						new Dimension(vizMapperMainPanel.getPropertiesPn().getMinimumSize().width + 20,
									  vizMapperMainPanel.getPreferredSize().height));
				
				// Select the same sheet that was selected before
				final VisualPropertySheet vpSheet = vizMapperMainPanel.getVisualPropertySheet(selectedTargetDataType);
				vizMapperMainPanel.setSelectedVisualPropertySheet(vpSheet);
			}
		});
	}
	
	@SuppressWarnings("rawtypes")
	private Set<VisualPropertySheetItem<?>> createVisualPropertySheetItems(final Class<? extends CyIdentifiable> type) {
		final Set<VisualPropertySheetItem<?>> items = new HashSet<VisualPropertySheetItem<?>>();
		
		final VisualLexicon lexicon = vmProxy.getCurrentVisualLexicon();
		final VisualStyle style = vmProxy.getCurrentVisualStyle();
		
		if (lexicon == null || style == null)
			return items;
		
		final Collection<VisualProperty<?>> vpList = lexicon.getAllDescendants(BasicVisualLexicon.NETWORK);
		final CyNetworkView curNetView = vmProxy.getCurrentNetworkView();
		final Set<View<CyNode>> selectedNodeViews = vmProxy.getSelectedNodeViews(curNetView);
		final Set<View<CyEdge>> selectedEdgeViews = vmProxy.getSelectedEdgeViews(curNetView);
		final Set<View<CyNetwork>> selectedNetViews = curNetView != null ?
				Collections.singleton((View<CyNetwork>) curNetView) : Collections.EMPTY_SET;
		final RenderingEngine<CyNetwork> engine = vizMapperMainPanel.getRenderingEngine();
		
		for (final VisualProperty<?> vp : vpList) {
			if (vp.getTargetDataType() != type)
				continue;
			
			if (PropertySheetUtil.isCompatible(vp) && !(vp instanceof DefaultVisualizableVisualProperty)) {
				// Create model
				final VisualPropertySheetItemModel<?> model = new VisualPropertySheetItemModel(vp, style, engine,
						lexicon);
				
				final Set values;
				
				if (vp.getTargetDataType() == CyNode.class) {
					values = getDistinctLockedValues(vp, selectedNodeViews);
					updateVpInfoLockedState(model, values, selectedNodeViews);
				} else if (vp.getTargetDataType() == CyEdge.class) {
					values = getDistinctLockedValues(vp, selectedEdgeViews);
					updateVpInfoLockedState(model, values, selectedEdgeViews);
				} else {
					values = getDistinctLockedValues(vp, selectedNetViews);
					updateVpInfoLockedState(model, values, selectedNetViews);
				}
				
				// Create View
				final VisualPropertySheetItem<?> sheetItem = new VisualPropertySheetItem(model, vizMapPropertyBuilder,
						themeMgr);
				items.add(sheetItem);
				
				// Add listeners to item and model:
				if (model.isVisualMappingAllowed()) {
					sheetItem.getPropSheetPnl().addPropertySheetChangeListener(new PropertyChangeListener() {
						@Override
						public void propertyChange(final PropertyChangeEvent e) {
							if (e.getPropertyName().equals("value") && e.getSource() instanceof VizMapperProperty)
								updateMappingStatus(sheetItem);
						}
					});
				}
				
				// Set the updated values to the visual style
				model.addPropertyChangeListener("defaultValue", new PropertyChangeListener() {
					@Override
					public void propertyChange(final PropertyChangeEvent e) {
						final VisualStyle vs = model.getVisualStyle();
						vs.setDefaultValue((VisualProperty)vp, e.getNewValue());
					}
				});
				model.addPropertyChangeListener("visualMappingFunction", new PropertyChangeListener() {
					@Override
					public void propertyChange(final PropertyChangeEvent e) {
						final VisualStyle vs = model.getVisualStyle();
						
						if (e.getNewValue() == null && vs.getVisualMappingFunction(vp) != null)
							vs.removeVisualMappingFunction(vp);
						else if (e.getNewValue() != null && !e.getNewValue().equals(vs.getVisualMappingFunction(vp)))
							vs.addVisualMappingFunction((VisualMappingFunction<?, ?>)e.getNewValue());
						
						updateMappingStatus(sheetItem);
					}
				});
			}
		}
		
		// Add dependencies
		final Set<VisualPropertyDependency<?>> dependencies = style.getAllVisualPropertyDependencies();
		
		for (final VisualPropertyDependency<?> dep : dependencies) {
			if (dep.getParentVisualProperty().getTargetDataType() != type)
				continue;
			
			final VisualPropertySheetItemModel<?> model = new VisualPropertySheetItemModel(dep, style, engine, lexicon);
			final VisualPropertySheetItem<?> sheetItem = new VisualPropertySheetItem(model, vizMapPropertyBuilder,
					themeMgr);
			items.add(sheetItem);
		}
		
		return items;
	}
	
	private void updateItemsStatus() {
		// Children of enabled dependencies must be disabled
		final Set<VisualProperty<?>> disabled = new HashSet<VisualProperty<?>>();
		final Map<VisualProperty<?>, String> messages = new HashMap<VisualProperty<?>, String>();
		final VisualStyle style = vmProxy.getCurrentVisualStyle();
		
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
		
		for (final VisualPropertySheet vpSheet : vizMapperMainPanel.getVisualPropertySheets()) {
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
	
	private void updateVisibleItems() {
		for (final VisualPropertySheet vpSheet : vizMapperMainPanel.getVisualPropertySheets()) {
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
	
	private void saveDefaultVisibleItems(final VisualPropertySheet vpSheet) {
		final Set<String> idSet = new HashSet<String>();
		
		for (final VisualPropertySheetItem<?> item : vpSheet.getItems()) {
			if (item.isVisible())
				idSet.add(item.getModel().getId());
		}
		
		propsProxy.setDefaultVisualProperties(vpSheet.getModel().getTargetDataType(), idSet);
	}
	
	private void updateMappings(final Class<? extends CyIdentifiable> targetDataType, final CyTable table) {
		if (table != null) {
			final VisualPropertySheet vpSheet = vizMapperMainPanel.getVisualPropertySheet(targetDataType);
			
			if (vpSheet != null) {
				final Collection<CyColumn> columns = table.getColumns();
				final HashMap<String, Class<?>> colTypes = new HashMap<String, Class<?>>();
				
				for (final CyColumn col : columns)
					colTypes.put(col.getName().toLowerCase(), col.getType());
					
				for (final VisualPropertySheetItem<?> item : vpSheet.getItems()) {
					final VisualMappingFunction<?, ?> mapping = item.getModel().getVisualMappingFunction();
					
					// Passthrough mappings don't need to be updated
					if (mapping instanceof DiscreteMapping || mapping instanceof ContinuousMapping) {
						final Class<?> colType = colTypes.get(mapping.getMappingColumnName().toLowerCase());
						
						if (colType != null && mapping.getMappingColumnType().isAssignableFrom(colType)) {
							invokeOnEDT(new Runnable() {
								@Override
								public void run() {
									item.updateMapping();
								}
							});
						}
					}
				}
			}
		}
	}
	
	private void updateMappingStatus(final VisualPropertySheetItem<?> item) {
		if (!item.isEnabled())
			return;
		
		final CyNetwork net = vmProxy.getCurrentNetwork();
		final Class<? extends CyIdentifiable> targetDataType = item.getModel().getTargetDataType();
		
		if (net != null && targetDataType != CyNetwork.class) {
			final CyTable netTable = targetDataType == CyNode.class ?
					net.getDefaultNodeTable() : net.getDefaultEdgeTable();
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
			
			invokeOnEDT(new Runnable() {
				@Override
				public void run() {
					item.setMessage(finalMsg, finalMsgType);
				}
			});
		}
	}
	
	private void updateLockedValues(final CyNetworkView currentView) {
		if (currentView != null) {
			updateLockedValues(Collections.singleton((View<CyNetwork>)currentView), CyNetwork.class);
			updateLockedValues(vmProxy.getSelectedNodeViews(currentView), CyNode.class);
			updateLockedValues(vmProxy.getSelectedEdgeViews(currentView), CyEdge.class);
		} else {
			updateLockedValues(Collections.EMPTY_SET, CyNetwork.class);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private <S extends CyIdentifiable> void updateLockedValues(final Set<View<S>> selectedViews,
															   final Class<S> targetDataType) {
		final Set<VisualPropertySheet> vpSheets = vizMapperMainPanel.getVisualPropertySheets();
		
		for (VisualPropertySheet sheet : vpSheets) {
			final Set<VisualPropertySheetItem<?>> vpItems = sheet.getItems();
			
			for (final VisualPropertySheetItem<?> item : vpItems) {
				final VisualPropertySheetItemModel<?> model = item.getModel();
				
				if (model.getTargetDataType() != targetDataType)
					continue;
				
				final Set values = getDistinctLockedValues(model.getVisualProperty(), selectedViews);
				
				if (targetDataType == CyNode.class) {
					updateVpInfoLockedState(model, values, selectedViews);
				} else if (targetDataType == CyEdge.class) {
					updateVpInfoLockedState(model, values, selectedViews);
				} else {
					updateVpInfoLockedState(model, values, selectedViews);
				}
			}
		}
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
	
	private <T, S extends CyIdentifiable> Set<T> getDistinctLockedValues(final VisualProperty<T> vp,
			 final Set<View<S>> views) {
		final Set<T> values = new HashSet<T>();

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
	
	@SuppressWarnings("rawtypes")
	private void openDefaultValueEditor(final ActionEvent evt, final VisualPropertySheetItem vpSheetItem) {
		final VisualPropertySheetItemModel model = vpSheetItem.getModel();
		final VisualProperty vp = model.getVisualProperty();

		final VisualStyle style = vmProxy.getCurrentVisualStyle();
		final Object oldValue = style.getDefaultValue(vp);
		Object val = null;
		
		try {
			final EditorManager editorMgr = servicesUtil.get(EditorManager.class);
			val = editorMgr.showVisualPropertyValueEditor(vizMapperMainPanel, vp, oldValue);
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
			newValue = editorMgr.showVisualPropertyValueEditor(vizMapperMainPanel, vp, curValue);
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
	
	@SuppressWarnings("rawtypes")
	private void onDependencySelectionChanged(final ItemEvent e, final VisualPropertySheetItem<?> vpSheetItem) {
		final boolean selected = e.getStateChange() == ItemEvent.SELECTED;
		final VisualPropertyDependency<?> dep = vpSheetItem.getModel().getVisualPropertyDependency();
		dep.setDependency(selected);
		
		final CyEventHelper evtHelper = servicesUtil.get(CyEventHelper.class);
		final Set<VisualProperty<?>> visualProperties = dep.getVisualProperties();
		final Set parent = Collections.singleton(dep.getParentVisualProperty());
		
 		if (selected)
 			evtHelper.fireEvent(new LexiconStateChangedEvent(this, parent, visualProperties));
		else
			evtHelper.fireEvent(new LexiconStateChangedEvent(this, visualProperties, parent));
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
	
	/**
	 * Utility method that invokes the code in Runnable.run on the AWT Event Dispatch Thread.
	 * @param runnable
	 */
	private void invokeOnEDT(final Runnable runnable) {
		if (SwingUtilities.isEventDispatchThread())
			runnable.run();
		else
			SwingUtilities.invokeLater(runnable);
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
			
			final JPopupMenu contextMenu = vizMapperMainPanel.getContextMenu();
			
			invokeOnEDT(new Runnable() {
				@Override
				public void run() {
					// Network properties don't have visual mappings
					final JMenu mapValueGeneratorsMenu = vizMapperMainPanel.getMapValueGeneratorsSubMenu();
					final Class<? extends CyIdentifiable> targetDataType = vpSheet.getModel().getTargetDataType();
					mapValueGeneratorsMenu.setVisible(targetDataType != CyNetwork.class);
					
					// Show context menu
					final Component parent = (Component) e.getSource();
					contextMenu.show(parent, e.getX(), e.getY());
				}
			});
		}
	}
}
