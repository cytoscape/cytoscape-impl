package org.cytoscape.view.vizmap.gui.internal.view;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_X_LOCATION;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_Y_LOCATION;
import static org.cytoscape.view.vizmap.gui.internal.ApplicationFacade.CURRENT_NETWORK_VIEW_CHANGED;
import static org.cytoscape.view.vizmap.gui.internal.ApplicationFacade.CURRENT_VISUAL_STYLE_CHANGED;
import static org.cytoscape.view.vizmap.gui.internal.ApplicationFacade.VISUAL_STYLE_SET_CHANGED;
import static org.cytoscape.view.vizmap.gui.internal.ApplicationFacade.VISUAL_STYLE_UPDATED;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.CyAction;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineFactory;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.DefaultVisualizableVisualProperty;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.gui.editor.EditorManager;
import org.cytoscape.view.vizmap.gui.event.LexiconStateChangedEvent;
import org.cytoscape.view.vizmap.gui.event.LexiconStateChangedListener;
import org.cytoscape.view.vizmap.gui.internal.model.LockedValueState;
import org.cytoscape.view.vizmap.gui.internal.model.VizMapperProxy;
import org.cytoscape.view.vizmap.gui.internal.task.GenerateValuesTaskFactory;
import org.cytoscape.view.vizmap.gui.internal.util.ServicesUtil;
import org.cytoscape.view.vizmap.gui.internal.view.VizMapperMainPanel.VisualStyleDropDownButton;
import org.cytoscape.view.vizmap.gui.util.DiscreteMappingGenerator;
import org.cytoscape.view.vizmap.gui.util.PropertySheetUtil;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.puremvc.java.multicore.interfaces.INotification;
import org.puremvc.java.multicore.patterns.mediator.Mediator;

import com.l2fprod.common.propertysheet.PropertySheetPanel;

@SuppressWarnings("unchecked")
public class VizMapperMediator extends Mediator implements LexiconStateChangedListener, RowsSetListener {

	public static final String NAME = "VizMapperMediator";
	
	private static final Class<? extends CyIdentifiable>[] SHEET_TYPES = 
			new Class[] { CyNode.class, CyEdge.class, CyNetwork.class };
	
	private static final String METADATA_MENU_KEY = "menu";
	private static final String METADATA_TITLE_KEY = "title";

	private static final String MAIN_MENU = "main";
	private static final String CONTEXT_MENU = "context";
	
	private VizMapperProxy proxy;
	private boolean ignoreVisualStyleSelectedEvents;
	private VisualPropertySheetItem<?> curVpSheetItem;
	private CyNetworkView previewNetView;
	
	private final ServicesUtil servicesUtil;
	private final VizMapperMainPanel vizMapperMainPanel;
	private final EditorManager editorManager;
	private final VizMapPropertyBuilder vizMapPropertyBuilder;

	// ==[ CONSTRUCTORS ]===============================================================================================
	
	public VizMapperMediator(final VizMapperMainPanel vizMapperMainPanel,
							 final ServicesUtil servicesUtil,
							 final EditorManager editorManager,
							 final VizMapPropertyBuilder vizMapPropertyBuilder) {
		super(NAME, null);
		
		if (vizMapperMainPanel == null)
			throw new IllegalArgumentException("'vizMapperMainPanel' must not be null");
		if (servicesUtil == null)
			throw new IllegalArgumentException("'servicesUtil' must not be null");
		if (editorManager == null)
			throw new IllegalArgumentException("'editorManager' must not be null");
		if (vizMapPropertyBuilder == null)
			throw new IllegalArgumentException("'vizMapPropertyBuilder' must not be null");
		
		this.vizMapperMainPanel = vizMapperMainPanel;
		this.servicesUtil = servicesUtil;
		this.editorManager = editorManager;
		this.vizMapPropertyBuilder = vizMapPropertyBuilder;
		
		setViewComponent(vizMapperMainPanel);
	}

	// ==[ PUBLIC METHODS ]=============================================================================================
	
	@Override
	public final void onRegister() {
		proxy = (VizMapperProxy) getFacade().retrieveProxy(VizMapperProxy.NAME);
		createPreviewNetworkView();
		initView();
		super.onRegister();
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

	@Override
	public String[] listNotificationInterests() {
		return new String[]{ VISUAL_STYLE_SET_CHANGED,
							 CURRENT_VISUAL_STYLE_CHANGED,
							 VISUAL_STYLE_UPDATED,
							 CURRENT_NETWORK_VIEW_CHANGED };
	}
	
	@Override
	public void handleNotification(final INotification notification) {
		final String id = notification.getName();
		final Object body = notification.getBody();
		
		if (id.equals(VISUAL_STYLE_SET_CHANGED)) {
			updateVisualStyleList((SortedSet<VisualStyle>) body);
		} else if (id.equals(CURRENT_VISUAL_STYLE_CHANGED)) {
			selectCurrentVisualStyle((VisualStyle) body);
			updateVisualPropertySheets((VisualStyle) body);
		} else if (id.equals(VISUAL_STYLE_UPDATED) && body == proxy.getCurrentVisualStyle()) {
			updateVisualPropertySheets((VisualStyle) body);
		} else if (id.equals(CURRENT_NETWORK_VIEW_CHANGED)) {
			final CyNetworkView nv = (CyNetworkView) body;
			
			if (nv != null) {
				updateLockedValues(Collections.singleton((View<CyNetwork>)nv), CyNetwork.class);
				updateLockedValues(proxy.getSelectedNodeViews(nv), CyNode.class);
				updateLockedValues(proxy.getSelectedEdgeViews(nv), CyEdge.class);
			} else {
				updateLockedValues(Collections.EMPTY_SET, CyNetwork.class);
			}
		}
	}

	@Override
	public void handleEvent(final LexiconStateChangedEvent e) {
		final Set<VisualProperty<?>> enabled = e.getEnabled();
		final Set<VisualProperty<?>> disabled = e.getDisabled();
		// TODO: update VP Sheets, removing disabled VPs and adding enabled ones if necessary
		
		// Update Network Views
		final VisualStyle curStyle = proxy.getCurrentVisualStyle();
		final Set<CyNetworkView> views = proxy.getNetworkViewsWithStyle(curStyle);
		
		for (final CyNetworkView view : views) {
			curStyle.apply(view);
			view.updateView();
		}
	}
	
	@Override
	public void handleEvent(final RowsSetEvent e) {
		// TODO also update after bypass is set
		// TODO what about when deleting elements?
		// Check selected nodes and edges of the current view
		final CyNetworkView curNetView = proxy.getCurrentNetworkView();
		
		if (curNetView == null || e.getColumnRecords(CyNetwork.SELECTED).isEmpty())
			return;
		
		final CyNetwork curNet = curNetView.getModel();
		final CyTable defNetTbl = curNet.getDefaultNetworkTable();
		final CyTable defNodeTbl = curNet.getDefaultNodeTable();
		final CyTable defEdgeTbl = curNet.getDefaultEdgeTable();
		final CyTable tbl = e.getSource();
		
		// We have to get all selected elements again
		if (tbl.equals(defEdgeTbl))
			updateLockedValues(proxy.getSelectedEdgeViews(curNetView), CyEdge.class);
		else if (tbl.equals(defNodeTbl))
			updateLockedValues(proxy.getSelectedNodeViews(curNetView), CyNode.class);
		else if (tbl.equals(defNetTbl))
			updateLockedValues(Collections.singleton((View<CyNetwork>)curNetView), CyNetwork.class);
	}
	
	public VisualProperty<?> getCurrentVisualProperty() {// TODO delete?
		return curVpSheetItem != null ? curVpSheetItem.getModel().getVisualProperty() : null;
	}
	
	public VisualPropertySheetItem<?> getCurrentVisualPropertySheetItem() {// TODO delete?
		return curVpSheetItem;
	}
	
	public PropertySheetPanel getCurrentPropertySheetPanel() {// TODO delete?
		return curVpSheetItem != null ? curVpSheetItem.getPropSheetPnl() : null;
	}
	
	/**
	 * Custom listener for adding registered VizMapper CyActions to the main menu.
	 */
	public synchronized void onCyActionRegistered(final CyAction action, final Map<?, ?> properties) {
		final Object serviceType = properties.get("service.type");
		
		if (serviceType != null && serviceType.toString().equals("vizmapUI.contextMenu")) {
			final Object title = properties.get("title");
			
			if (title == null)
				throw new NullPointerException("Title is missing for a menu item");

			final JMenuItem menuItem = new JMenuItem(title.toString());
			menuItem.addActionListener(action);
			vizMapperMainPanel.getEditSubMenu().add(menuItem);
		}
	}

	/**
	 * Custom listener for removing unregistered VizMapper CyActions from the main menu.
	 */
	public synchronized void onCyActionUnregistered(final CyAction action, final Map<?, ?> properties) {
		final Component[] arr = vizMapperMainPanel.getEditSubMenu().getComponents();
		final List<Component> cList = arr != null ? Arrays.asList(arr) : new ArrayList<Component>();

		for (final Component c : cList) {
			if (c instanceof JMenuItem && ((JMenuItem)c).getAction().equals(action))
				vizMapperMainPanel.getEditSubMenu().remove(c);
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
		
		if (menuDef == null)
			throw new NullPointerException("Menu metadata is missing.");

		// This is a menu item for Main Command Button.
		final Object title = properties.get(METADATA_TITLE_KEY);
		
		if (title == null)
			throw new NullPointerException("Title metadata is missing.");

		// Add new menu to the pull-down
		final JMenuItem menuItem = new JMenuItem(title.toString());
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				servicesUtil.get(DialogTaskManager.class).execute(taskFactory.createTaskIterator());
			}
		});

		if (menuDef.toString().equals(MAIN_MENU))
			vizMapperMainPanel.getMainMenu().add(menuItem);
		else if (menuDef.toString().equals(CONTEXT_MENU))
			vizMapperMainPanel.getEditSubMenu().add(menuItem);
	}

	public void onTaskFactoryUnregistered(final TaskFactory taskFactory, final Map<?, ?> properties) {
		// TODO
	}

	public void onMappingGeneratorRegistered(final DiscreteMappingGenerator<?> generator, final Map<?, ?> properties) {
		final Object serviceType = properties.get(METADATA_MENU_KEY);
		
		if (serviceType == null)
			throw new NullPointerException("Service Type metadata is null.  This value is required.");

		// This is a menu item for Main Command Button.
		final Object title = properties.get(METADATA_TITLE_KEY);
		
		if (title == null)
			throw new NullPointerException("Title metadata is missing.");
		
		// Create mapping generator task factory
		final GenerateValuesTaskFactory taskFactory = new GenerateValuesTaskFactory(generator, vizMapperMainPanel, 
				servicesUtil);

		// Add new menu to the pull-down
		final JMenuItem menuItem = new JMenuItem(title.toString());
		// menuItem.setIcon(iconManager.getIcon(iconId));
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				servicesUtil.get(DialogTaskManager.class).execute(taskFactory.createTaskIterator());
			}
		});

		vizMapperMainPanel.getMapValueGeneratorsSubMenu().add(menuItem);
	}

	public void onMappingGeneratorUnregistered(final DiscreteMappingGenerator<?> generator, final Map<?, ?> properties) {
		// TODO
	}
	
	// ==[ PRIVATE METHODS ]============================================================================================

	private void initView() {
		servicesUtil.registerAllServices(vizMapperMainPanel);
		addViewListeners();
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
	
	@SuppressWarnings("serial")
	private void addViewListeners(final VisualPropertySheet vpSheet) {
		for (final VisualPropertySheetItem<?> vpSheetItem : vpSheet.getItems()) {
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
							final DropDownMenuButton btn = vpSheetItem.getBypassBtn();
							
							if (state == LockedValueState.ENABLED_NOT_SET) {
								// There is only one option to execute, so do it now, rather than showing the popup menu
								openLockedValueEditor(e, vpSheetItem);
								// Make sure the drop-down menu is removed, so it's not displayed
								// after this action is executed
								btn.setPopupMenu(null);
							} else {
								bypassMenu.show(btn, 0, btn.getHeight());
								bypassMenu.requestFocusInWindow();
							}
						}
					});
				}
				
				// Right-click
				vpSheetItem.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(final MouseEvent e) {
						if (SwingUtilities.isRightMouseButton(e))
							handleContextMenuEvent(e, vpSheet, vpSheetItem);
					}
				});
				vpSheetItem.getPropSheetPnl().getTable().addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(final MouseEvent e) {
						if (SwingUtilities.isRightMouseButton(e))
							handleContextMenuEvent(e, vpSheet, vpSheetItem);
					}
				});
				
				// Save the current editor (the one the user is interacting with)
				vpSheetItem.getPropSheetTbl().addFocusListener(new FocusAdapter() {
					@Override
					public void focusGained(final FocusEvent e) {
						curVpSheetItem = vpSheetItem;
					}
				});
			} else {
				// It's a Dependency Editor...
				vpSheetItem.getDependencyCkb().addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(final ItemEvent e) {
						onDependencySelectionChanged(e, vpSheetItem);
					}
				});
			}
		}
	}

	private void updateVisualStyleList(final SortedSet<VisualStyle> styles) {
		final RenderingEngineFactory<CyNetwork> engineFactory = proxy.getCurrentRenderingEngineFactory();
		
		invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				ignoreVisualStyleSelectedEvents = true;
				vizMapperMainPanel.updateVisualStyles(styles, previewNetView, engineFactory);
				final VisualStyle vs = proxy.getCurrentVisualStyle();
				selectCurrentVisualStyle(vs);
				updateVisualPropertySheets(vs);
				ignoreVisualStyleSelectedEvents = false;
			}
		});
	}
	
	private void selectCurrentVisualStyle(final VisualStyle vs) {
		final VisualStyleDropDownButton btn = vizMapperMainPanel.getStylesBtn();
		
		invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				final VisualStyle selectedVs = btn.getSelectedItem();
				
				if (vs != selectedVs)
					btn.setSelectedItem(vs);
			}
		});
	}
	
	private void updateVisualPropertySheets(final VisualStyle vs) {
		final VisualPropertySheet curNetSheet = vizMapperMainPanel.getVisualPropertySheet(CyNetwork.class);
		final VisualPropertySheetModel curModel = curNetSheet != null ? curNetSheet.getModel() : null;
		final VisualStyle curStyle = curModel != null ? curModel.getVisualStyle() : null;
		final Set<String> visibleProps = new HashSet<String>();
		
		if (vs != null && !vs.equals(curStyle)) {
			// TODO group by target data type, because the VP id is not guaranteed to be unique among different types
			final Set<VisualPropertySheet> vpSheets = vizMapperMainPanel.getVisualPropertySheets();
			
			if (vpSheets.isEmpty()) {
				// First time
				// TODO: load default visual properties from app file
				// #######################
				visibleProps.add("NODE_BORDER_PAINT");
				visibleProps.add("NODE_BORDER_WIDTH");
				visibleProps.add("NODE_FILL_COLOR");
				visibleProps.add("NODE_LABEL");
				visibleProps.add("NODE_LABEL_COLOR");
				visibleProps.add("NODE_LABEL_FONT_SIZE");
				visibleProps.add("NODE_SHAPE");
				visibleProps.add("NODE_SIZE");
				visibleProps.add("NODE_WIDTH");
				visibleProps.add("NODE_HEIGHT");
				visibleProps.add("NODE_TRANSPARENCY");
				visibleProps.add("nodeSizeLocked");
				visibleProps.add("EDGE_LABEL");
				visibleProps.add("EDGE_LABEL_COLOR");
				visibleProps.add("EDGE_LABEL_FONT_SIZE");
				visibleProps.add("EDGE_LINE_TYPE");
				visibleProps.add("EDGE_PAINT");
				visibleProps.add("EDGE_SOURCE_ARROW_SHAPE");
				visibleProps.add("EDGE_SOURCE_ARROW_UNSELECTED_PAINT");
				visibleProps.add("EDGE_TARGET_ARROW_UNSELECTED_PAINT");
				visibleProps.add("EDGE_TARGET_ARROW_SHAPE");
				visibleProps.add("EDGE_STROKE_UNSELECTED_PAINT");
				visibleProps.add("EDGE_TRANSPARENCY");
				visibleProps.add("EDGE_WIDTH");
				visibleProps.add("arrowColorMatchesEdge");
				visibleProps.add("NETWORK_BACKGROUND_PAINT");
				visibleProps.add("NETWORK_TITLE");
				visibleProps.add("NETWORK_NODE_SELECTION");
				visibleProps.add("NETWORK_EDGE_SELECTION");
				// #######################
			} else {
				// Keep the same items visible after updating the sheets
				for (final VisualPropertySheet sheet : vpSheets) {
					for (VisualPropertySheetItem<?> item : sheet.getItems()) {
						if (item.isVisible())
							visibleProps.add(item.getModel().getId());
					}
				}
			}
		}
		
		invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				if (vs != null && vs.equals(curStyle)) {
					// If same style, just update the current Visual Property sheets
					final RenderingEngine<CyNetwork> engine = vizMapperMainPanel.getRenderingEngine();
					
					for (final VisualPropertySheet vpSheet : vizMapperMainPanel.getVisualPropertySheets()) {
						for (final VisualPropertySheetItem<?> item : vpSheet.getItems()) {
							item.getModel().setRenderingEngine(engine);
							item.update();
						}
					}
				} else {
					final VisualPropertySheet curVpSheet = vizMapperMainPanel.getCurrentVisualPropertySheet();
					final Class<? extends CyIdentifiable> curTargetDataType = curVpSheet != null ?
							curVpSheet.getModel().getTargetDataType() : null;
					
					// If a different style, rebuild all property sheets
					createVisualPropertySheets();
					
					
					for (final VisualPropertySheet vpSheet : vizMapperMainPanel.getVisualPropertySheets()) {
						// Create new Visual Property Sheet Items
						final Set<VisualPropertySheetItem<?>> vpSheetItems = 
								createVisualPropertySheetItems(vpSheet.getModel().getTargetDataType());
						
						for (final VisualPropertySheetItem<?> item : vpSheetItems)
							item.setVisible(visibleProps.contains(item.getModel().getId()));
						
						vpSheet.setItems(vpSheetItems);
						
						// Add event listeners to the new components
						addViewListeners(vpSheet);
					}
					
					invokeOnEDT(new Runnable() {
						@Override
						public void run() {
							// Select the same sheet that was selected before
							final VisualPropertySheet vpSheet = vizMapperMainPanel.getVisualPropertySheet(curTargetDataType);
							vizMapperMainPanel.setCurrentVisualPropertySheet(vpSheet);
						}
					});
				}
			}
		});
	}
	
	private void createVisualPropertySheets() {
		final VisualStyle style = proxy.getCurrentVisualStyle();
		final VisualLexicon lexicon = proxy.getCurrentVisualLexicon();
		
		invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				for (final Class<? extends CyIdentifiable> type : SHEET_TYPES) {
					final VisualPropertySheetModel model = new VisualPropertySheetModel(type, style, lexicon);
					final VisualPropertySheet vpSheet = new VisualPropertySheet(model);
					vizMapperMainPanel.addVisualPropertySheet(vpSheet);
				}
			}
		});
	}
	
	@SuppressWarnings("rawtypes")
	private Set<VisualPropertySheetItem<?>> createVisualPropertySheetItems(final Class<? extends CyIdentifiable> type) {
		final Set<VisualPropertySheetItem<?>> items = new HashSet<VisualPropertySheetItem<?>>();
		
		final VisualLexicon lexicon = proxy.getCurrentVisualLexicon();
		final VisualStyle style = proxy.getCurrentVisualStyle();
		
		if (lexicon == null || style == null)
			return items;
		
		final Collection<VisualProperty<?>> vpList = lexicon.getAllDescendants(BasicVisualLexicon.NETWORK);
		final CyNetworkView curNetView = proxy.getCurrentNetworkView();
		final Set<View<CyNode>> selectedNodeViews = proxy.getSelectedNodeViews(curNetView);
		final Set<View<CyEdge>> selectedEdgeViews = proxy.getSelectedEdgeViews(curNetView);
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
				final VisualPropertySheetItem<?> sheetItem = new VisualPropertySheetItem(model, editorManager,
						vizMapPropertyBuilder);
				items.add(sheetItem);
			}
		}
		
		// Add dependencies
		final Set<VisualPropertyDependency<?>> dependencies = style.getAllVisualPropertyDependencies();
		
		for (final VisualPropertyDependency<?> dep : dependencies) {
			if (dep.getParentVisualProperty().getTargetDataType() != type)
				continue;
			
			final VisualPropertySheetItemModel<?> model = new VisualPropertySheetItemModel(dep, style, engine, lexicon);
			final VisualPropertySheetItem<?> sheetItem = new VisualPropertySheetItem(model, editorManager,
					vizMapPropertyBuilder);
			items.add(sheetItem);
		}
		
		return items;
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

		final VisualStyle curStyle = proxy.getCurrentVisualStyle();
		final Object defaultVal = curStyle.getDefaultValue(vp);
		Object newValue = null;
		
		try {
			final EditorManager editorMgr = servicesUtil.get(EditorManager.class);
			newValue = editorMgr.showVisualPropertyValueEditor(vizMapperMainPanel, vp, defaultVal);
		} catch (Exception ex) {
//			logger.error("Error opening Visual Property values editor for: " + vp, ex);
		}

		if (newValue != null && !newValue.equals(defaultVal)) {
			curStyle.setDefaultValue(vp, newValue);
			vpSheetItem.repaint();
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
		
		if (newValue == null || newValue.equals(curValue))
			return;
		
		// TODO: move to an asynchronous task
		final CyNetworkView curNetView = proxy.getCurrentNetworkView();
		
		if (curNetView == null)
			return;
		
		final Class<? extends CyIdentifiable> targetDataType = model.getVisualProperty().getTargetDataType();
		final Set<View<?>> selectedViews = new HashSet<View<?>>();
		
		if (targetDataType == CyNode.class)
			selectedViews.addAll(proxy.getSelectedNodeViews(curNetView));
		else if (targetDataType == CyEdge.class)
			selectedViews.addAll(proxy.getSelectedEdgeViews(curNetView));
		else
			selectedViews.add(curNetView);
		
		// Clear or set the new locked value to all selected elements
		for (final View<?> view : selectedViews) {
			view.setLockedValue(vp, newValue);
		}
		
		model.setLockedValue(newValue);
		model.setLockedValueState(newValue == null ? 
				LockedValueState.ENABLED_NOT_SET : LockedValueState.ENABLED_UNIQUE_VALUE);
		
		curNetView.updateView();
	}
	
	private void removeLockedValue(final ActionEvent e, final VisualPropertySheetItem<?> vpSheetItem) {
		// TODO: move to an asynchronous task
		final CyNetworkView curNetView = proxy.getCurrentNetworkView();
		final VisualPropertySheetItemModel<?> model = vpSheetItem.getModel();
		final Class<? extends CyIdentifiable> targetDataType = model.getVisualProperty().getTargetDataType();
		final Set<View<?>> selectedViews = new HashSet<View<?>>();
		
		if (targetDataType == CyNode.class)
			selectedViews.addAll(proxy.getSelectedNodeViews(curNetView));
		else if (targetDataType == CyEdge.class)
			selectedViews.addAll(proxy.getSelectedEdgeViews(curNetView));
		else
			selectedViews.add(curNetView);
		
		// Clear or set the new locked value to all selected elements
		for (final View<?> view : selectedViews) {
			view.clearValueLock(model.getVisualProperty());
		}
		
		model.setLockedValue(null);
		model.setLockedValueState(LockedValueState.ENABLED_NOT_SET);
		
		curNetView.updateView();
	}

	private void onSelectedVisualStyleChanged(final PropertyChangeEvent e) {
		final VisualStyle vs = (VisualStyle) e.getNewValue();
		
		if (!ignoreVisualStyleSelectedEvents && vs != proxy.getCurrentVisualStyle()) {
			// Update proxy
			final Thread t = new Thread() {
				@Override
				public void run() {
					proxy.setCurrentVisualStyle(vs);
				};
			};
			t.start();
		}
	}
	
	private void onDependencySelectionChanged(final ItemEvent e, final VisualPropertySheetItem<?> vpSheetItem) {
		final boolean selected = e.getStateChange() == ItemEvent.SELECTED;
		final VisualPropertyDependency<?> dep = vpSheetItem.getModel().getVisualPropertyDependency();
		dep.setDependency(selected);
		
		final CyEventHelper evtHelper = servicesUtil.get(CyEventHelper.class);
		final Set<VisualProperty<?>> visualProperties = dep.getVisualProperties();
		
 		if (selected)
 			evtHelper.fireEvent(new LexiconStateChangedEvent(this, null, visualProperties));
		else
			evtHelper.fireEvent(new LexiconStateChangedEvent(this, visualProperties, null));
	}
	
	private void handleContextMenuEvent(final MouseEvent e, final VisualPropertySheet vpSheet, 
			final VisualPropertySheetItem<?> vpSheetItem) {
		if (!vpSheetItem.isSelected())
			vpSheet.setSelectedItems((Set) (Collections.singleton(vpSheetItem)));
		
		final JPopupMenu contextMenu = vizMapperMainPanel.getContextMenu();
		
		invokeOnEDT(new Runnable() {
			@Override
			public void run() {
				final Component parent = (Component) e.getSource();
				contextMenu.show(parent, e.getX(), e.getY());
			}
		});
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
}
