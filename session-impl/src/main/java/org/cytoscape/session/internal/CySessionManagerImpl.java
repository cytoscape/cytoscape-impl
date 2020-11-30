package org.cytoscape.session.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.TableViewRenderer;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyTableMetadata;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySession;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionSavedEvent;
import org.cytoscape.session.events.SessionSavedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.table.CyColumnViewMetadata;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.model.table.CyTableViewFactory;
import org.cytoscape.view.model.table.CyTableViewManager;
import org.cytoscape.view.model.table.CyTableViewMetadata;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;
import org.cytoscape.view.vizmap.TableVisualMappingManager;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.undo.UndoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Session Impl (session-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2018 The Cytoscape Consortium
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
 * Default implementation of {@link org.cytoscape.session.CySessionManager}.
 */
public class CySessionManagerImpl implements CySessionManager, SessionSavedListener {

	private String currentFileName;
	private final Map<String, CyProperty<?>> sessionProperties;
	private CyProperty<Bookmarks> bookmarks;
	private boolean disposed;
	
	private final CyServiceRegistrar serviceRegistrar;
	private final Object lock = new Object();

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	public CySessionManagerImpl(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		this.sessionProperties = new HashMap<>();
	}

	@Override
	public CySession getCurrentSession() {
		// Apps who want to save anything to a session will have to listen for this event
		// and will then be responsible for adding files through SessionAboutToBeSavedEvent.addAppFiles(..)
		SessionAboutToBeSavedEvent savingEvent = new SessionAboutToBeSavedEvent(this);
		CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		eventHelper.fireEvent(savingEvent);

		Set<CyNetwork> networks = getSerializableNetworks();
		
		CyNetworkViewManager nvMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
		Set<CyNetworkView> netViews = nvMgr.getNetworkViewSet();

		// Visual Styles Map
		Map<CyNetworkView, String> stylesMap = new HashMap<>();
		VisualMappingManager vmMgr = serviceRegistrar.getService(VisualMappingManager.class);

		if (netViews != null) {
			for (CyNetworkView nv : netViews) {
				VisualStyle style = vmMgr.getVisualStyle(nv);
				if (style != null)
					stylesMap.put(nv, style.getTitle());
			}
		}
		
		// Table styles
		TableVisualMappingManager tblVmMgr = serviceRegistrar.getService(TableVisualMappingManager.class);
		CyTableViewManager tvMgr = serviceRegistrar.getService(CyTableViewManager.class);
		
		
		Set<CyTableView> tableViews = tvMgr.getTableViewSet();
		Set<CyTableViewMetadata> tableViewMetadatas = new HashSet<>();
		
		
		for(CyTableView tableView : tableViews) {
			tableViewMetadatas.add(getTableViewMetadata(tableView));
		}
		

		Map<String, List<File>> appMap = savingEvent.getAppFileListMap();
		Set<CyTableMetadata> metadata = createTablesMetadata(networks);
		Set<VisualStyle> styles = vmMgr.getAllVisualStyles();
		Set<CyProperty<?>> props = getAllProperties();
		Set<VisualStyle> tableStyles = tblVmMgr.getAllVisualStyles();
		
		
		// Build the session
		CySession sess = new CySession.Builder()
				.properties(props)
				.appFileListMap(appMap)
				.tables(metadata)
				.networks(networks)
				.networkViews(netViews)
				.tableViews(tableViewMetadatas)
				.visualStyles(styles)
				.tableStyles(tableStyles)
				.viewVisualStyleMap(stylesMap)
				.build();

		return sess;
	}
	

	private CyTableViewMetadata getTableViewMetadata(CyTableView tableView) {
		var networkTableManager = serviceRegistrar.getService(CyNetworkTableManager.class);
		var tableMappingManager = serviceRegistrar.getService(TableVisualMappingManager.class);
		
		VisualLexicon lexicon = getVisualLexicon(tableView);
		CyTable table = tableView.getModel();
		
		// The table browser is passed facade tables not the actual tables, so we need to dig down and get the real SUID.
		// MKTODO Should probably add an interface CyTableFacade and use instanceof to test for it.
		Long actualSuid = table.getPrimaryKey().getTable().getSUID();
		
		String namespace = networkTableManager.getTableNamespace(table);
		String rendererID = tableView.getRendererId();
		
		List<CyColumnViewMetadata> columnMetadataList = new ArrayList<>();
		
		for(View<CyColumn> colView : tableView.getColumnViews()) {
			Map<String,String> bypassValues = new HashMap<>();
			
			for(VisualProperty vp : lexicon.getAllVisualProperties()) {
				if(colView.isDirectlyLocked(vp)) {
					Object value = colView.getVisualProperty(vp);
					
					// Have to serialize the VP values early in the session manager because they get restored here too :)
					String valueString = null;
					try {
						valueString = vp.toSerializableString(value);
					} catch(ClassCastException e) { }
					
					if(value != null) {
						bypassValues.put(vp.getIdString(), valueString);
					}
				}
			}
			
			String styleName = null;
			VisualStyle style = tableMappingManager.getVisualStyle(colView);
			if(style != null) {
				styleName = style.getTitle();
			}
			
			var colName = colView.getModel().getName();
			var colMetadata = new CyColumnViewMetadata(colName, styleName, bypassValues);
			columnMetadataList.add(colMetadata);
		}
		
		return new CyTableViewMetadata(actualSuid, namespace, rendererID, columnMetadataList);
	}
	
	
	private VisualLexicon getVisualLexicon(CyTableView tableView) {
		var renderingEngineManager = serviceRegistrar.getService(RenderingEngineManager.class);
		var renderingEngines = renderingEngineManager.getRenderingEngines(tableView);
		for(var re : renderingEngines) {
			if(Objects.equals(tableView.getRendererId(), re.getRendererId())) {
				return re.getVisualLexicon();
			}
		}
		return null;
	}
	
	
	@SuppressWarnings("unchecked")
	private static Class<? extends CyIdentifiable>[] TYPES = new Class[] { CyNetwork.class, CyNode.class, CyEdge.class };
	
	private Set<CyNetwork> getSerializableNetworks() {
		final Set<CyNetwork> serializableNetworks = new HashSet<>();
		
		final CyNetworkTableManager netTblMgr = serviceRegistrar.getService(CyNetworkTableManager.class);
		final Set<CyNetwork> allNetworks = netTblMgr.getNetworkSet();
		
		for (final CyNetwork net : allNetworks) {
			if (net.getSavePolicy() == SavePolicy.SESSION_FILE)
				serializableNetworks.add(net);
		}
		
		return serializableNetworks;
	}
	
	private Set<CyTableMetadata> createTablesMetadata(final Set<CyNetwork> networks) {
		final Set<CyTableMetadata> result = new HashSet<>();
		
		result.addAll(createNetworkTablesMetadata(networks));
		result.addAll(createGlobalTablesMetadata(serviceRegistrar.getService(CyTableManager.class).getGlobalTables()));
		
		return result;
	}

	private Set<CyTableMetadata> createNetworkTablesMetadata(final Set<CyNetwork> networks) {
		final Set<CyTableMetadata> result = new HashSet<>();
		final CyNetworkTableManager netTblMgr = serviceRegistrar.getService(CyNetworkTableManager.class);
		
		// Create the metadata object for each network table
		for (final CyNetwork network : networks) {
			for (final Class<? extends CyIdentifiable> type : TYPES) {
				final Map<String, CyTable> tableMap = netTblMgr.getTables(network, type);
				
				for (final Entry<String, CyTable> entry : tableMap.entrySet()) {
					final CyTable tbl = entry.getValue();
					
					if (tbl.getSavePolicy() == SavePolicy.SESSION_FILE) {
						final String namespace = entry.getKey();
						final CyTableMetadata metadata = new CyTableMetadataImpl.CyTableMetadataBuilder()
								.setCyTable(tbl).setNamespace(namespace).setType(type).setNetwork(network)
								.build();
						
						result.add(metadata);
					}
				}
			}
		}
		
		return result;
	}
	
	private Collection<? extends CyTableMetadata> createGlobalTablesMetadata(final Set<CyTable> tables) {
		final Set<CyTableMetadata> result = new HashSet<CyTableMetadata>();
		
		for (final CyTable tbl : tables) {
			if (tbl.getSavePolicy() == SavePolicy.SESSION_FILE)
				result.add(new CyTableMetadataImpl.CyTableMetadataBuilder().setCyTable(tbl));
		}
		
		return result;
	}

	@Override
	public void setCurrentSession(CySession sess, final String fileName) {
		// DO NOT save a reference to the sess parameter
		// the session is a large object and that would cause a memory leak
		
		// Always remove the current session first
		if (!disposed)
			disposeCurrentSession();

		if (sess == null) {
			logger.debug("Creating empty session...");
			
			final VisualMappingManager vmMgr = serviceRegistrar.getService(VisualMappingManager.class);
			final Set<VisualStyle> styles = vmMgr.getAllVisualStyles();
			final Set<CyProperty<?>> props = getAllProperties();

			sess = new CySession.Builder().properties(props).visualStyles(styles).build();
		} else {
			logger.debug("Restoring the session...");

			// Save the selected networks first, so the selection state can be restored later.
			final List<CyNetwork> selectedNetworks = new ArrayList<>();
			final Set<CyNetwork> networks = sess.getNetworks();

			for (CyNetwork n : networks) {
				final Boolean selected = n.getDefaultNetworkTable().getRow(n.getSUID())
						.get(CyNetwork.SELECTED, Boolean.class);
				
				if (Boolean.TRUE.equals(selected))
					selectedNetworks.add(n);
			}
			
			restoreProperties(sess);
			restoreNetworks(sess);

			restoreTables(sess);
			restoreNetworkViews(sess, selectedNetworks);
			restoreNetworkSelection(sess, selectedNetworks);
			restoreVisualStyles(sess);
			restoreTableViews(sess);
			restoreCurrentVisualStyle();
		}

		currentFileName = fileName;
		disposed = false;

		final CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		eventHelper.fireEvent(new SessionLoadedEvent(this, sess, getCurrentSessionFileName()));
	}

	/**
	 * Update current session session object when session is saved.
	 */
	@Override
	public void handleEvent(SessionSavedEvent e) {
		if (currentFileName != e.getSavedFileName())
			currentFileName = e.getSavedFileName();
	}
	
	@Override
	public String getCurrentSessionFileName() {
		return currentFileName;
	}

	@SuppressWarnings("unchecked")
	public void addCyProperty(final CyProperty<?> newCyProperty, final Map<String, String> properties) {
		CyProperty.SavePolicy sp = newCyProperty.getSavePolicy();

		synchronized (lock ) {
			if (sp == CyProperty.SavePolicy.SESSION_FILE || sp == CyProperty.SavePolicy.SESSION_FILE_AND_CONFIG_DIR) {
				if (Bookmarks.class.isAssignableFrom(newCyProperty.getPropertyType()))
					bookmarks = (CyProperty<Bookmarks>) newCyProperty;
				else
					sessionProperties.put(newCyProperty.getName(), newCyProperty);
			}
		}
	}

	public void removeCyProperty(final CyProperty<?> oldCyProperty, final Map<String, String> properties) {
		CyProperty.SavePolicy sp = oldCyProperty.getSavePolicy();

		synchronized (lock) {
			if (sp == CyProperty.SavePolicy.SESSION_FILE || sp == CyProperty.SavePolicy.SESSION_FILE_AND_CONFIG_DIR) {
				if (Bookmarks.class.isAssignableFrom(oldCyProperty.getPropertyType()))
					bookmarks = null;
				else
					sessionProperties.remove(oldCyProperty.getName());
			}
		}
	}

	private Set<CyProperty<?>> getAllProperties() {
		final Set<CyProperty<?>> set;
		
		synchronized (lock) {
			set = new HashSet<>(sessionProperties.values());
		}
		
		if (bookmarks != null)
			set.add(bookmarks);

		return set;
	}

	private void restoreProperties(final CySession sess) {
		for (CyProperty<?> cyProps : sess.getProperties()) {
			final CyProperty<?> oldCyProps = sessionProperties.get(cyProps.getName());
			
			// Do we already have a CyProperty with the same name and SESSION_FILE_AND_CONFIG_DIR policy?
			if (oldCyProps != null && oldCyProps.getSavePolicy() == CyProperty.SavePolicy.SESSION_FILE_AND_CONFIG_DIR) {
				if (oldCyProps.getPropertyType() == Properties.class && cyProps.getPropertyType() == Properties.class) {
					// Simply overwrite the existing properties with the new ones from the session...
					final Properties oldProps = (Properties) oldCyProps.getProperties();
					final Properties newProps = (Properties) cyProps.getProperties();
					
					for (final String key : newProps.stringPropertyNames()) {
						final String newValue = newProps.getProperty(key);
						oldProps.setProperty(key, newValue);
					}
					
					continue; // This new CyProperty does not need to be registered!
				} else {
					// The whole CyProperty object will have to be replaced...
					
					// But we need to keep the original SESSION_FILE_AND_CONFIG_DIR policy.
					// Since there is no CyProperty.setProperty() method, we have to create a new CyProperty
					cyProps = new SimpleCyProperty<>(cyProps.getName(), cyProps.getProperties(), cyProps.getPropertyType(),
							CyProperty.SavePolicy.SESSION_FILE_AND_CONFIG_DIR);
					
					// The new CyProperty will replace this one, which has to be unregistered first
					serviceRegistrar.unregisterAllServices(oldCyProps);
				}
			}
			
			final Properties serviceProps = new Properties();
			serviceProps.setProperty("cyPropertyName", cyProps.getName());
			serviceRegistrar.registerAllServices(cyProps, serviceProps);
		}
	}
	
	private void restoreNetworks(final CySession sess) {
		logger.debug("Restoring networks...");
		Set<CyNetwork> networks = sess.getNetworks();
		final CyNetworkManager netMgr = serviceRegistrar.getService(CyNetworkManager.class);

		for (CyNetwork n : networks) {
			netMgr.addNetwork(n, false);
		}
	}
	
	private void restoreNetworkViews(final CySession sess, List<CyNetwork> selectedNetworks) {
		logger.debug("Restoring network views...");
		Set<CyNetworkView> netViews = sess.getNetworkViews();
		List<CyNetworkView> selectedViews = new ArrayList<>();
		
		for (CyNetworkView nv : netViews) {
			CyNetwork network = nv.getModel();
			
			if (selectedNetworks.contains(network)) {
				selectedViews.add(nv);
			}
		}
		
		Map<CyNetworkView, Map<VisualProperty<?>, Object>> viewVPMap = new HashMap<>();
		
		if (netViews != null) {
			final CyNetworkViewManager nvMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
			
			for (CyNetworkView nv : netViews) {
				if (nv != null) {
					// Save the original values of these visual properties,
					// because we will have to set them again after the views are rendered
					Map<VisualProperty<?>, Object> vpMap = new HashMap<>();
					viewVPMap.put(nv, vpMap);
					vpMap.put(BasicVisualLexicon.NETWORK_HEIGHT, nv.getVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT));
					vpMap.put(BasicVisualLexicon.NETWORK_WIDTH, nv.getVisualProperty(BasicVisualLexicon.NETWORK_WIDTH));
					
					nvMgr.addNetworkView(nv, false);
				}
			}
		}

		// Let's guarantee the network views are rendered
		final CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		eventHelper.flushPayloadEvents();
		
		// Set the saved visual properties again, because the renderer may have overwritten the original values
		for (Entry<CyNetworkView, Map<VisualProperty<?>, Object>> entry1 : viewVPMap.entrySet()) {
			CyNetworkView nv = entry1.getKey();
			
			for (Entry<VisualProperty<?>, Object> entry2 : entry1.getValue().entrySet())
				nv.setVisualProperty(entry2.getKey(), entry2.getValue());
		}
		
		final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
		
		if (!selectedViews.isEmpty())
			appMgr.setCurrentNetworkView(selectedViews.get(0));
		
		appMgr.setSelectedNetworkViews(selectedViews);
	}
	
	
	private void restoreTableViews(CySession sess) {
		var appManager = serviceRegistrar.getService(CyApplicationManager.class);
		var tableViewManager = serviceRegistrar.getService(CyTableViewManager.class);
		var networkTableManager = serviceRegistrar.getService(CyNetworkTableManager.class);
		var tableMappingManager = serviceRegistrar.getService(TableVisualMappingManager.class);
		
		Set<VisualStyle> styles = sess.getTableStyles();
		Map<String, VisualStyle> stylesMap = new HashMap<>();
		if (styles != null) {
			for (VisualStyle vs : styles) {
				stylesMap.put(vs.getTitle(), vs);
			}
		}
		
		Set<CyTableViewMetadata> tableViews = sess.getTableViews();
		for(var tableViewMetadata : tableViews) {
			TableViewRenderer renderer = appManager.getTableViewRenderer(tableViewMetadata.getRendererID());
			if(renderer != null) {
				CyTableMetadata tableMetadata = tableViewMetadata.getUnderlyingTable();
				if(tableMetadata != null) {
					CyTable table = tableMetadata.getTable();
					
					// Check if the table view was created for a facade table.
					if(CyNetwork.LOCAL_ATTRS.equals(tableMetadata.getNamespace()) && CyNetwork.DEFAULT_ATTRS.equals(tableViewMetadata.getNamespace())) {
						// get the facade table
						table = networkTableManager.getTable(tableMetadata.getNetwork(), ((Class<? extends CyIdentifiable>)tableMetadata.getType()), CyNetwork.DEFAULT_ATTRS);
					}
					
					CyTableViewFactory tableViewFactory = renderer.getTableViewFactory();
					CyTableView tableView = tableViewFactory.createTableView(table);
					
					VisualLexicon lexicon = renderer.getRenderingEngineFactory(TableViewRenderer.DEFAULT_CONTEXT).getVisualLexicon();
					restoreTableViewBypasses(tableView, tableViewMetadata, lexicon);
					
					tableViewManager.setTableView(tableView);
					
					
					for(var colViewMeta : tableViewMetadata.getColumnViews()) {
						String styleName = colViewMeta.getStyleName();
						if(styleName != null) {
							VisualStyle vs = stylesMap.get(styleName);
							if (vs != null) {
								var colView = tableView.getColumnView(colViewMeta.getName());
								if(colView != null) {
									tableMappingManager.setVisualStyle(colView, vs);
									vs.apply(colView);
								}
							}
						}
					}
				}
			}
		}
	}
	

	private void restoreTableViewBypasses(CyTableView tableView, CyTableViewMetadata tableViewMetadata, VisualLexicon lexicon) {
		// Restore column view visual property bypass values. Other values come from the style.
		for(CyColumnViewMetadata colViewMeta : tableViewMetadata.getColumnViews()) {
			View<CyColumn> colView = tableView.getColumnView(colViewMeta.getName());
			
			for(var entry : colViewMeta.getBypassValues().entrySet()) {
				String vpId = entry.getKey();
				String vpStr = entry.getValue();
				
				VisualProperty<?> vp = lexicon.lookup(CyColumn.class, vpId);
				if(vp != null) {
					Object parsedValue = vp.parseSerializableString(vpStr);
					if(parsedValue != null) {
						colView.setLockedValue(vp, parsedValue);
					}
				}
			}
		}
	}
	
	private void restoreTables(final CySession sess) {
		final Set<CyTable> allTables = new HashSet<>();
		
		// Register all tables sent through the CySession, if not already registered
		for (final CyTableMetadata metadata : sess.getTables()) {
			allTables.add(metadata.getTable());
		}
		
		// There may be other network tables in the CyNetworkTableManager that were not serialized in the session file
		// (e.g. Table Facades), so it's necessary to add them to CyTableManager as well
		final CyNetworkTableManager netTblMgr = serviceRegistrar.getService(CyNetworkTableManager.class);
		final CyRootNetworkManager rootNetMgr = serviceRegistrar.getService(CyRootNetworkManager.class);
		
		for (final CyNetwork net : sess.getNetworks()) {
			allTables.addAll(netTblMgr.getTables(net, CyNetwork.class).values());
			allTables.addAll(netTblMgr.getTables(net, CyNode.class).values());
			allTables.addAll(netTblMgr.getTables(net, CyEdge.class).values());
			
			if (!(net instanceof CyRootNetwork)) {
				final CyRootNetwork root = rootNetMgr.getRootNetwork(net);
				allTables.addAll(netTblMgr.getTables(root, CyNetwork.class).values());
				allTables.addAll(netTblMgr.getTables(root, CyNode.class).values());
				allTables.addAll(netTblMgr.getTables(root, CyEdge.class).values());
			}
		}
		
		// Register all tables sent through the CySession, if not already registered
		final CyTableManager tblMgr = serviceRegistrar.getService(CyTableManager.class);
		
		for (final CyTable tbl : allTables) {
			if (tblMgr.getTable(tbl.getSUID()) == null)
				tblMgr.addTable(tbl);
		}
	}
	
	private void restoreVisualStyles(final CySession sess) {
		logger.debug("Restoring visual styles...");
		// Register visual styles
		final VisualMappingManager vmMgr = serviceRegistrar.getService(VisualMappingManager.class);
		final VisualStyle defStyle = vmMgr.getDefaultVisualStyle();
		final String DEFAULT_STYLE_NAME = defStyle.getTitle();
		
		final Set<VisualStyle> styles = sess.getVisualStyles();
		final Map<String, VisualStyle> stylesMap = new HashMap<>();
		
		final RenderingEngineManager engineManager = serviceRegistrar.getService(RenderingEngineManager.class);
		final VisualLexicon lexicon = engineManager.getDefaultVisualLexicon();
		
		if (styles != null) {
			for (VisualStyle vs : styles) {
				if (vs.getTitle().equals(DEFAULT_STYLE_NAME)) {
					// Update the current default style, because it can't be replaced or removed
					updateVisualStyle(vs, defStyle, lexicon);
					vs = defStyle;
				}
				
				stylesMap.put(vs.getTitle(), vs);
				
				if (!vs.equals(defStyle))
					vmMgr.addVisualStyle(vs);
			}
		}
		
		// Set visual styles to network views
		final Map<CyNetworkView, String> viewStyleMap = sess.getViewVisualStyleMap();
		
		if (viewStyleMap != null) {
			for (Entry<CyNetworkView, String> entry : viewStyleMap.entrySet()) {
				final CyNetworkView netView = entry.getKey();
				final String stName = entry.getValue();
				VisualStyle vs = stylesMap.get(stName);

				if (vs == null)
					vs = defStyle;
				
				if (vs != null) {
					vmMgr.setVisualStyle(vs, netView);
					vs.apply(netView);
				}
			}
		}
	}
	
	
	/**
	 * @param source the Visual Style that will provide the new properties and values.
	 * @param target the Visual Style that will be updated.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void updateVisualStyle(final VisualStyle source, final VisualStyle target, VisualLexicon lexicon) {
		// First clean up the target
		final HashSet<VisualMappingFunction<?, ?>> mapingSet = new HashSet<>(target.getAllVisualMappingFunctions());
		
		for (final VisualMappingFunction<?, ?> mapping : mapingSet)
			target.removeVisualMappingFunction(mapping.getVisualProperty());
		
		final Set<VisualPropertyDependency<?>> depList = 
				new HashSet<VisualPropertyDependency<?>>(target.getAllVisualPropertyDependencies());
		
		for (final VisualPropertyDependency<?> dep : depList)
			target.removeVisualPropertyDependency(dep);
		
		// Copy the default visual properties, mappings and dependencies from source to target
		final Set<VisualProperty<?>> properties = lexicon.getAllVisualProperties();
		
		for (final VisualProperty vp : properties) {
			if (!vp.equals(BasicVisualLexicon.NETWORK)
					&& !vp.equals(BasicVisualLexicon.NODE)
					&& !vp.equals(BasicVisualLexicon.EDGE)
					&& !vp.equals(BasicTableVisualLexicon.TABLE)
					&& !vp.equals(BasicTableVisualLexicon.COLUMN)
					&& !vp.equals(BasicTableVisualLexicon.TABLE))
				target.setDefaultValue(vp, source.getDefaultValue(vp));
		}
		
		for (final VisualPropertyDependency<?> dep : source.getAllVisualPropertyDependencies())
			target.addVisualPropertyDependency(dep);
		
		for (final VisualMappingFunction<?, ?> mapping : source.getAllVisualMappingFunctions())
			target.addVisualMappingFunction(mapping);
	}

	private void restoreNetworkSelection(final CySession sess, final List<CyNetwork> selectedNets) {
		// If the current view/network was not set, set the first selected network as current
		if (!selectedNets.isEmpty()) {
			final CyNetwork cn = selectedNets.get(0);
			final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
			appMgr.setCurrentNetwork(cn);
			
			// Also set the current view, if there is one
			final CyNetworkViewManager nvMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
			final Collection<CyNetworkView> cnViews = nvMgr.getNetworkViews(cn);
			final CyNetworkView cv = cnViews.isEmpty() ? null : cnViews.iterator().next();
			appMgr.setCurrentNetworkView(cv);
		
			// The selected networks must be set after setting the current one!
			appMgr.setSelectedNetworks(selectedNets);
		}
	}

	private void restoreCurrentVisualStyle() {
		// Make sure the current visual style is the one applied to the current network view
		final CyEventHelper eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		eventHelper.flushPayloadEvents();
		
		final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
		final CyNetworkView cv = appMgr.getCurrentNetworkView();
		
		if (cv != null) {
			final VisualMappingManager vmMgr = serviceRegistrar.getService(VisualMappingManager.class);
			final VisualStyle style = vmMgr.getVisualStyle(cv);
			
			if (style != null && !style.equals(vmMgr.getCurrentVisualStyle()))
				vmMgr.setCurrentVisualStyle(style);
		}
	}

	@Override
	public void disposeCurrentSession() {
		logger.debug("Disposing current session...");
		
		// Destroy network views
		CyNetworkViewManager nvMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
		for (CyNetworkView nv : nvMgr.getNetworkViewSet())
			nvMgr.destroyNetworkView(nv);
		
		nvMgr.reset();
		
		// Destroy table views
		CyTableViewManager tvMgr = serviceRegistrar.getService(CyTableViewManager.class);
		for(CyTableView tv : tvMgr.getTableViewSet()) {
			tvMgr.destroyTableView(tv);
		}
		
		tvMgr.reset();
		
		// Destroy networks
		final CyNetworkManager netMgr = serviceRegistrar.getService(CyNetworkManager.class);
		final Set<CyNetwork> networks = netMgr.getNetworkSet();
		
		for (final CyNetwork n : networks)
			netMgr.destroyNetwork(n);
		
		netMgr.reset();

		// Destroy styles
		logger.debug("Removing current visual styles...");
		final VisualMappingManager vmMgr = serviceRegistrar.getService(VisualMappingManager.class);
		final VisualStyle defaultStyle = vmMgr.getDefaultVisualStyle();
		final List<VisualStyle> allStyles = new ArrayList<>(vmMgr.getAllVisualStyles());

		for (VisualStyle vs : allStyles) {
			if (!vs.equals(defaultStyle))
				vmMgr.removeVisualStyle(vs);
		}

		// Destroy tables
		final CyTableManager tblMgr = serviceRegistrar.getService(CyTableManager.class);
		tblMgr.reset();
		
		// Reset groups
		serviceRegistrar.getService(CyGroupManager.class).reset();
		
		// Unregister session properties
		final Set<CyProperty<?>> cyPropsClone = getAllProperties();
		
		for (CyProperty<?> cyProps : cyPropsClone) {
			if (cyProps.getSavePolicy().equals(CyProperty.SavePolicy.SESSION_FILE)) {
				serviceRegistrar.unregisterAllServices(cyProps);
				sessionProperties.remove(cyProps.getName());
			}
		}
		
		// Clear undo stack
		serviceRegistrar.getService(UndoSupport.class).reset();
		
		// Reset current table and rendering engine
		final CyApplicationManager appMgr = serviceRegistrar.getService(CyApplicationManager.class);
		appMgr.reset();
		
		currentFileName = null;
		disposed = true;
	}
}
