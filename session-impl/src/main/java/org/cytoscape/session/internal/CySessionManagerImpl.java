package org.cytoscape.session.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.cytoscape.model.CyRow;
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
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.table.CyColumnViewMetadata;
import org.cytoscape.view.model.table.CyRowViewMetadata;
import org.cytoscape.view.model.table.CyTableView;
import org.cytoscape.view.model.table.CyTableViewManager;
import org.cytoscape.view.model.table.CyTableViewMetadata;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;
import org.cytoscape.view.vizmap.TableVisualMappingManager;
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

/**
 * Default implementation of {@link org.cytoscape.session.CySessionManager}.
 */
public class CySessionManagerImpl implements CySessionManager, SessionSavedListener {

	@SuppressWarnings("unchecked")
	private static Class<? extends CyIdentifiable>[] TYPES = new Class[] { CyNetwork.class, CyNode.class, CyEdge.class };
	
	private String currentFileName;
	private final Map<String, CyProperty<?>> sessionProperties;
	private CyProperty<Bookmarks> bookmarks;
	private boolean disposed;
	
	private final CyServiceRegistrar serviceRegistrar;
	private final Object lock = new Object();

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	public CySessionManagerImpl(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		this.sessionProperties = new HashMap<>();
	}

	@Override
	public CySession getCurrentSession() {
		// Apps who want to save anything to a session will have to listen for this event
		// and will then be responsible for adding files through SessionAboutToBeSavedEvent.addAppFiles(..)
		var savingEvent = new SessionAboutToBeSavedEvent(this);
		var eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		eventHelper.fireEvent(savingEvent);

		var networks = getSerializableNetworks();
		
		var nvMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
		var netViews = nvMgr.getNetworkViewSet();

		// Visual Styles Map
		var stylesMap = new HashMap<CyNetworkView, String>();
		var vmMgr = serviceRegistrar.getService(VisualMappingManager.class);

		if (netViews != null) {
			for (var nv : netViews) {
				var style = vmMgr.getVisualStyle(nv);
				
				if (style != null)
					stylesMap.put(nv, style.getTitle());
			}
		}
		
		// Table styles
		var tblVmMgr = serviceRegistrar.getService(TableVisualMappingManager.class);
		var tvMgr = serviceRegistrar.getService(CyTableViewManager.class);
		
		var tableViews = tvMgr.getTableViewSet();
		var tableViewMetadatas = new HashSet<CyTableViewMetadata>();
		
		for (var tableView : tableViews)
			tableViewMetadatas.add(getTableViewMetadata(tableView));

		var appMap = savingEvent.getAppFileListMap();
		var metadata = createTablesMetadata(networks);
		var styles = vmMgr.getAllVisualStyles();
		var props = getAllProperties();
		var tableStyles = tblVmMgr.getAllVisualStyles();
		var styleAssociations = tblVmMgr.getAllStyleAssociations();
		
		// Build the session
		var sess = new CySession.Builder()
				.properties(props)
				.appFileListMap(appMap)
				.tables(metadata)
				.networks(networks)
				.networkViews(netViews)
				.tableViews(tableViewMetadatas)
				.visualStyles(styles)
				.tableStyles(tableStyles)
				.columnStyleAssociations(styleAssociations)
				.viewVisualStyleMap(stylesMap)
				.build();

		return sess;
	}
	
	private CyTableViewMetadata getTableViewMetadata(CyTableView tableView) {
		var networkTableManager = serviceRegistrar.getService(CyNetworkTableManager.class);
		var tableMappingManager = serviceRegistrar.getService(TableVisualMappingManager.class);
		
		var lexicon = getVisualLexicon(tableView);
		var table = tableView.getModel();
		
		// The table browser is passed facade tables not the actual tables, so we need to dig down and get the real SUID.
		// MKTODO Should probably add an interface CyTableFacade and use instanceof to test for it.
		var actualSuid = table.getPrimaryKey().getTable().getSUID();
		
		var namespace = networkTableManager.getTableNamespace(table);
		var rendererID = tableView.getRendererId();
		
		var tableBypasses = new HashMap<String, String>();
		
		for (var vp : lexicon.getAllDescendants(BasicTableVisualLexicon.TABLE)) {
			if (tableView.isDirectlyLocked(vp))
				addBypassValue(tableBypasses, vp, tableView.getVisualProperty(vp));
		}
		
		var columnMetadataList = new ArrayList<CyColumnViewMetadata>();
		
		for (var colView : tableView.getColumnViews()) {
			var colBypasses = new HashMap<String, String>();
			
			for (var vp : lexicon.getAllDescendants(BasicTableVisualLexicon.COLUMN)) {
				if (colView.isDirectlyLocked(vp)) {
					// Have to serialize the VP values early in the session manager because they get restored here too :)
					addBypassValue(colBypasses, vp, colView.getVisualProperty(vp));
				}
			}
			
			var style = tableMappingManager.getVisualStyle(colView);
			String styleName = null;
			
			if (style != null)
				styleName = style.getTitle();
			
			var colName = colView.getModel().getName();
			var colMetadata = new CyColumnViewMetadata(colName, styleName, colBypasses);
			columnMetadataList.add(colMetadata);
		}
		
		var keyCol = table.getPrimaryKey();
		var rowMetadataList = new ArrayList<CyRowViewMetadata>();
		
		for (var rowView : tableView.getRowViews()) {
			var rowBypasses = new HashMap<String, String>();
			
			for (var vp : lexicon.getAllDescendants(BasicTableVisualLexicon.ROW)) {
				if (rowView.isDirectlyLocked(vp))
					addBypassValue(rowBypasses, vp, rowView.getVisualProperty(vp));
			}
			
			// MKTODO what if the key is a List ??? Take a look at CSVCyWriter.writeValues()
			var keyValue = rowView.getModel().get(keyCol.getName(), keyCol.getType());
			var rowMetadata = new CyRowViewMetadata(keyValue, rowBypasses);
			rowMetadataList.add(rowMetadata);
		}
		
		return new CyTableViewMetadata(actualSuid, namespace, rendererID, tableBypasses, 
				columnMetadataList, rowMetadataList, keyCol.getType(), keyCol.getListElementType());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void addBypassValue(Map<String,String> bypassValues, VisualProperty vp, Object value) {
		// Have to serialize the VP values early in the session manager because they get restored here too :)
		String valueString = null;
		
		try {
			valueString = vp.toSerializableString(value);
		} catch (ClassCastException e) {
			// Ignore...
		}
		
		if (value != null)
			bypassValues.put(vp.getIdString(), valueString);
	}
	
	private VisualLexicon getVisualLexicon(CyTableView tableView) {
		var renderingEngineManager = serviceRegistrar.getService(RenderingEngineManager.class);
		var renderingEngines = renderingEngineManager.getRenderingEngines(tableView);
		
		for (var re : renderingEngines) {
			if (Objects.equals(tableView.getRendererId(), re.getRendererId()))
				return re.getVisualLexicon();
		}
		
		logger.error("VisualLexicon not found for tableView while saving session: " + tableView.getModel().getTitle());
		return renderingEngineManager.getDefaultTableVisualLexicon();
	}
	
	private Set<CyNetwork> getSerializableNetworks() {
		var serializableNetworks = new HashSet<CyNetwork>();
		
		var netTblMgr = serviceRegistrar.getService(CyNetworkTableManager.class);
		var allNetworks = netTblMgr.getNetworkSet();
		
		for (var net : allNetworks) {
			if (net.getSavePolicy() == SavePolicy.SESSION_FILE)
				serializableNetworks.add(net);
		}
		
		return serializableNetworks;
	}
	
	private Set<CyTableMetadata> createTablesMetadata(Set<CyNetwork> networks) {
		var result = new HashSet<CyTableMetadata>();
		
		result.addAll(createNetworkTablesMetadata(networks));
		result.addAll(createGlobalTablesMetadata(serviceRegistrar.getService(CyTableManager.class).getGlobalTables()));
		
		return result;
	}

	private Set<CyTableMetadata> createNetworkTablesMetadata(Set<CyNetwork> networks) {
		var result = new HashSet<CyTableMetadata>();
		var netTblMgr = serviceRegistrar.getService(CyNetworkTableManager.class);
		
		// Create the metadata object for each network table
		for (var network : networks) {
			for (var type : TYPES) {
				var tableMap = netTblMgr.getTables(network, type);
				
				for (var entry : tableMap.entrySet()) {
					var tbl = entry.getValue();
					
					if (tbl.getSavePolicy() == SavePolicy.SESSION_FILE) {
						var namespace = entry.getKey();
						var metadata = new CyTableMetadataImpl.CyTableMetadataBuilder()
								.setCyTable(tbl).setNamespace(namespace).setType(type).setNetwork(network)
								.build();
						
						result.add(metadata);
					}
				}
			}
		}
		
		return result;
	}
	
	private Collection<? extends CyTableMetadata> createGlobalTablesMetadata(Set<CyTable> tables) {
		var result = new HashSet<CyTableMetadata>();
		
		for (var tbl : tables) {
			if (tbl.getSavePolicy() == SavePolicy.SESSION_FILE)
				result.add(new CyTableMetadataImpl.CyTableMetadataBuilder().setCyTable(tbl));
		}
		
		return result;
	}

	@Override
	public void setCurrentSession(CySession sess, String fileName) {
		// DO NOT save a reference to the sess parameter
		// the session is a large object and that would cause a memory leak
		
		// Always remove the current session first
		if (!disposed)
			disposeCurrentSession();

		if (sess == null) {
			logger.debug("Creating empty session...");
			
			var vmMgr = serviceRegistrar.getService(VisualMappingManager.class);
			var styles = vmMgr.getAllVisualStyles();
			var props = getAllProperties();

			sess = new CySession.Builder().properties(props).visualStyles(styles).build();
		} else {
			logger.debug("Restoring the session...");

			// Save the selected networks first, so the selection state can be restored later.
			var selectedNetworks = new ArrayList<CyNetwork>();
			var networks = sess.getNetworks();

			for (var n : networks) {
				var selected = n.getDefaultNetworkTable().getRow(n.getSUID()).get(CyNetwork.SELECTED, Boolean.class);
				
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
			restoreColumnStyleAssociations(sess);
			restoreCurrentVisualStyle();
		}

		currentFileName = fileName;
		disposed = false;

		var eventHelper = serviceRegistrar.getService(CyEventHelper.class);
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
	public void addCyProperty(CyProperty<?> newCyProperty, Map<String, String> properties) {
		var sp = newCyProperty.getSavePolicy();

		synchronized (lock ) {
			if (sp == CyProperty.SavePolicy.SESSION_FILE || sp == CyProperty.SavePolicy.SESSION_FILE_AND_CONFIG_DIR) {
				if (Bookmarks.class.isAssignableFrom(newCyProperty.getPropertyType()))
					bookmarks = (CyProperty<Bookmarks>) newCyProperty;
				else
					sessionProperties.put(newCyProperty.getName(), newCyProperty);
			}
		}
	}

	public void removeCyProperty(CyProperty<?> oldCyProperty, Map<String, String> properties) {
		var sp = oldCyProperty.getSavePolicy();

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

	private void restoreProperties(CySession sess) {
		for (var cyProps : sess.getProperties()) {
			var oldCyProps = sessionProperties.get(cyProps.getName());
			
			// Do we already have a CyProperty with the same name and SESSION_FILE_AND_CONFIG_DIR policy?
			if (oldCyProps != null && oldCyProps.getSavePolicy() == CyProperty.SavePolicy.SESSION_FILE_AND_CONFIG_DIR) {
				if (oldCyProps.getPropertyType() == Properties.class && cyProps.getPropertyType() == Properties.class) {
					// Simply overwrite the existing properties with the new ones from the session...
					var oldProps = (Properties) oldCyProps.getProperties();
					var newProps = (Properties) cyProps.getProperties();
					
					for (var key : newProps.stringPropertyNames()) {
						var newValue = newProps.getProperty(key);
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
			
			var serviceProps = new Properties();
			serviceProps.setProperty("cyPropertyName", cyProps.getName());
			serviceRegistrar.registerAllServices(cyProps, serviceProps);
		}
	}
	
	private void restoreNetworks(CySession sess) {
		logger.debug("Restoring networks...");
		var networks = sess.getNetworks();
		var netMgr = serviceRegistrar.getService(CyNetworkManager.class);

		for (var n : networks) {
			netMgr.addNetwork(n, false);
		}
	}
	
	private void restoreNetworkViews(CySession sess, List<CyNetwork> selectedNetworks) {
		logger.debug("Restoring network views...");
		var netViews = sess.getNetworkViews();
		var selectedViews = new ArrayList<CyNetworkView>();
		
		for (var nv : netViews) {
			var network = nv.getModel();
			
			if (selectedNetworks.contains(network))
				selectedViews.add(nv);
		}
		
		var viewVPMap = new HashMap<CyNetworkView, Map<VisualProperty<?>, Object>>();
		
		if (netViews != null) {
			var nvMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
			
			for (var nv : netViews) {
				if (nv != null) {
					// Save the original values of these visual properties,
					// because we will have to set them again after the views are rendered
					var vpMap = new HashMap<VisualProperty<?>, Object>();
					viewVPMap.put(nv, vpMap);
					vpMap.put(BasicVisualLexicon.NETWORK_HEIGHT, nv.getVisualProperty(BasicVisualLexicon.NETWORK_HEIGHT));
					vpMap.put(BasicVisualLexicon.NETWORK_WIDTH, nv.getVisualProperty(BasicVisualLexicon.NETWORK_WIDTH));
					
					nvMgr.addNetworkView(nv, false);
				}
			}
		}

		// Let's guarantee the network views are rendered
		var eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		eventHelper.flushPayloadEvents();
		
		// Set the saved visual properties again, because the renderer may have overwritten the original values
		for (var entry1 : viewVPMap.entrySet()) {
			var nv = entry1.getKey();
			
			for (var entry2 : entry1.getValue().entrySet())
				nv.setVisualProperty(entry2.getKey(), entry2.getValue());
		}
		
		var appMgr = serviceRegistrar.getService(CyApplicationManager.class);
		
		if (!selectedViews.isEmpty())
			appMgr.setCurrentNetworkView(selectedViews.get(0));
		
		appMgr.setSelectedNetworkViews(selectedViews);
	}
	
	@SuppressWarnings("unchecked")
	private void restoreTableViews(CySession sess) {
		var appManager = serviceRegistrar.getService(CyApplicationManager.class);
		var tableViewManager = serviceRegistrar.getService(CyTableViewManager.class);
		var networkTableManager = serviceRegistrar.getService(CyNetworkTableManager.class);
		var tableMappingManager = serviceRegistrar.getService(TableVisualMappingManager.class);
		
		var styles = sess.getTableStyles();
		var stylesMap = new HashMap<String, VisualStyle>();
		
		if (styles != null) {
			for (var vs : styles)
				stylesMap.put(vs.getTitle(), vs);
		}
		
		var tableViews = sess.getTableViews();
		
		for (var tableViewMetadata : tableViews) {
			var renderer = appManager.getTableViewRenderer(tableViewMetadata.getRendererID());
			
			if (renderer != null) {
				var tableMetadata = tableViewMetadata.getUnderlyingTable();
				
				if (tableMetadata != null) {
					var table = tableMetadata.getTable();
					
					// Check if the table view was created for a facade table.
					if (CyNetwork.LOCAL_ATTRS.equals(tableMetadata.getNamespace()) && CyNetwork.DEFAULT_ATTRS.equals(tableViewMetadata.getNamespace())) {
						// get the facade table
						table = networkTableManager.getTable(tableMetadata.getNetwork(),
								((Class<? extends CyIdentifiable>) tableMetadata.getType()), CyNetwork.DEFAULT_ATTRS);
					}
					
					var tableViewFactory = renderer.getTableViewFactory();
					var tableView = tableViewFactory.createTableView(table);
					
					var lexicon = renderer.getRenderingEngineFactory(TableViewRenderer.DEFAULT_CONTEXT).getVisualLexicon();
					restoreTableViewBypasses(sess, tableView, tableViewMetadata, lexicon);
					
					tableViewManager.setTableView(tableView);
					
					for (var colViewMeta : tableViewMetadata.getColumnViews()) {
						var styleName = colViewMeta.getStyleName();
						
						if (styleName != null) {
							var vs = stylesMap.get(styleName);
							
							if (vs != null) {
								var colView = tableView.getColumnView(colViewMeta.getName());
								
								if (colView != null) {
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
	
	private void restoreColumnStyleAssociations(CySession sess) {
		var tableVMM = serviceRegistrar.getService(TableVisualMappingManager.class);
		
		for(var association : sess.getColumnStyleAssociations()) {
			var netStyle = association.networkVisualStyle();
			var tableType = association.tableType();
			var colName = association.colName();
			var columnVisualStyle = association.columnVisualStyle();
			
			tableVMM.setAssociatedVisualStyle(netStyle, tableType, colName, columnVisualStyle);
		}
	}

	private void restoreTableViewBypasses(CySession sess, CyTableView tableView, CyTableViewMetadata tableViewMetadata, VisualLexicon lexicon) {
		for (var entry : tableViewMetadata.getBypassValues().entrySet()) {
			var vpId = entry.getKey();
			var vpStr = entry.getValue();
			
			var vp = lexicon.lookup(CyTable.class, vpId);
			
			if (vp != null) {
				var parsedValue = vp.parseSerializableString(vpStr);
				
				if (parsedValue != null)
					tableView.setLockedValue(vp, parsedValue);
			}
		}
		
		// Restore column view visual property bypass values. Other values come from the style.
		for (var colViewMeta : tableViewMetadata.getColumnViews()) {
			var colView = tableView.getColumnView(colViewMeta.getName());
			
			if (colView == null)
				continue;
			
			for (var entry : colViewMeta.getBypassValues().entrySet()) {
				var vpId = entry.getKey();
				var vpStr = entry.getValue();
				
				var vp = lexicon.lookup(CyColumn.class, vpId);
				
				if (vp != null) {
					var parsedValue = vp.parseSerializableString(vpStr);
					
					if (parsedValue != null)
						colView.setLockedValue(vp, parsedValue);
				}
			}
		}
		
		for (var rowViewMeta : tableViewMetadata.getRowViews()) {
			var row = tableView.getModel().getRow(rowViewMeta.getKeyValue());
			var rowView = tableView.getRowView(row);
			
			if (rowView == null)
				continue;
			
			for (var entry : rowViewMeta.getBypassValues().entrySet()) {
				var vpId = entry.getKey();
				var vpStr = entry.getValue();
				
				var vp = lexicon.lookup(CyRow.class, vpId);
				
				if (vp != null) {
					var parsedValue = vp.parseSerializableString(vpStr);
					
					if (parsedValue != null)
						rowView.setLockedValue(vp, parsedValue);
				}
			}
		}
	}
	
	private void restoreTables(CySession sess) {
		var allTables = new HashSet<CyTable>();
		
		// Register all tables sent through the CySession, if not already registered
		for (var metadata : sess.getTables())
			allTables.add(metadata.getTable());
		
		// There may be other network tables in the CyNetworkTableManager that were not serialized in the session file
		// (e.g. Table Facades), so it's necessary to add them to CyTableManager as well
		var netTblMgr = serviceRegistrar.getService(CyNetworkTableManager.class);
		var rootNetMgr = serviceRegistrar.getService(CyRootNetworkManager.class);
		
		for (var net : sess.getNetworks()) {
			allTables.addAll(netTblMgr.getTables(net, CyNetwork.class).values());
			allTables.addAll(netTblMgr.getTables(net, CyNode.class).values());
			allTables.addAll(netTblMgr.getTables(net, CyEdge.class).values());
			
			if (!(net instanceof CyRootNetwork)) {
				var root = rootNetMgr.getRootNetwork(net);
				allTables.addAll(netTblMgr.getTables(root, CyNetwork.class).values());
				allTables.addAll(netTblMgr.getTables(root, CyNode.class).values());
				allTables.addAll(netTblMgr.getTables(root, CyEdge.class).values());
			}
		}
		
		// Register all tables sent through the CySession, if not already registered
		var tblMgr = serviceRegistrar.getService(CyTableManager.class);
		
		for (var tbl : allTables) {
			if (tblMgr.getTable(tbl.getSUID()) == null)
				tblMgr.addTable(tbl);
		}
	}
	
	private void restoreVisualStyles(CySession sess) {
		logger.debug("Restoring visual styles...");
		// Register visual styles
		var vmMgr = serviceRegistrar.getService(VisualMappingManager.class);
		var defStyle = vmMgr.getDefaultVisualStyle();
		var DEFAULT_STYLE_NAME = defStyle.getTitle();
		
		var styles = sess.getVisualStyles();
		var stylesMap = new HashMap<String, VisualStyle>();
		
		var engineManager = serviceRegistrar.getService(RenderingEngineManager.class);
		var lexicon = engineManager.getDefaultVisualLexicon();
		
		if (styles != null) {
			for (var vs : styles) {
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
		var viewStyleMap = sess.getViewVisualStyleMap();
		
		if (viewStyleMap != null) {
			for (var entry : viewStyleMap.entrySet()) {
				var netView = entry.getKey();
				var stName = entry.getValue();
				var vs = stylesMap.get(stName);

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
	private static void updateVisualStyle(VisualStyle source, VisualStyle target, VisualLexicon lexicon) {
		// First clean up the target
		var mapingSet = new HashSet<>(target.getAllVisualMappingFunctions());
		
		for (var mapping : mapingSet)
			target.removeVisualMappingFunction(mapping.getVisualProperty());
		
		var depList = new HashSet<VisualPropertyDependency<?>>(target.getAllVisualPropertyDependencies());
		
		for (var dep : depList)
			target.removeVisualPropertyDependency(dep);
		
		// Copy the default visual properties, mappings and dependencies from source to target
		var properties = lexicon.getAllVisualProperties();
		
		for (VisualProperty vp : properties) {
			if (!vp.equals(BasicVisualLexicon.NETWORK)
					&& !vp.equals(BasicVisualLexicon.NODE)
					&& !vp.equals(BasicVisualLexicon.EDGE)
					&& !vp.equals(BasicTableVisualLexicon.TABLE)
					&& !vp.equals(BasicTableVisualLexicon.COLUMN)
					&& !vp.equals(BasicTableVisualLexicon.TABLE))
				target.setDefaultValue(vp, source.getDefaultValue(vp));
		}
		
		for (var dep : source.getAllVisualPropertyDependencies())
			target.addVisualPropertyDependency(dep);
		
		for (var mapping : source.getAllVisualMappingFunctions())
			target.addVisualMappingFunction(mapping);
	}

	private void restoreNetworkSelection(CySession sess, List<CyNetwork> selectedNets) {
		// If the current view/network was not set, set the first selected network as current
		if (!selectedNets.isEmpty()) {
			var cn = selectedNets.get(0);
			var appMgr = serviceRegistrar.getService(CyApplicationManager.class);
			appMgr.setCurrentNetwork(cn);
			
			// Also set the current view, if there is one
			var nvMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
			var cnViews = nvMgr.getNetworkViews(cn);
			var cv = cnViews.isEmpty() ? null : cnViews.iterator().next();
			appMgr.setCurrentNetworkView(cv);
		
			// The selected networks must be set after setting the current one!
			appMgr.setSelectedNetworks(selectedNets);
		}
	}

	private void restoreCurrentVisualStyle() {
		// Make sure the current visual style is the one applied to the current network view
		var eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		eventHelper.flushPayloadEvents();
		
		var appMgr = serviceRegistrar.getService(CyApplicationManager.class);
		var cv = appMgr.getCurrentNetworkView();
		
		if (cv != null) {
			var vmMgr = serviceRegistrar.getService(VisualMappingManager.class);
			var style = vmMgr.getVisualStyle(cv);
			
			if (style != null && !style.equals(vmMgr.getCurrentVisualStyle()))
				vmMgr.setCurrentVisualStyle(style);
		}
	}

	@Override
	public void disposeCurrentSession() {
		logger.debug("Disposing current session...");
		
		// Destroy network views
		var nvMgr = serviceRegistrar.getService(CyNetworkViewManager.class);
		
		for (var nv : nvMgr.getNetworkViewSet())
			nvMgr.destroyNetworkView(nv);
		
		nvMgr.reset();
		
		// Destroy table views
		var tvMgr = serviceRegistrar.getService(CyTableViewManager.class);
		
		for (var tv : tvMgr.getTableViewSet())
			tvMgr.destroyTableView(tv);
		
		tvMgr.reset();
		
		// Destroy networks
		var netMgr = serviceRegistrar.getService(CyNetworkManager.class);
		var networks = netMgr.getNetworkSet();
		
		for (var n : networks) {
			try {
				if (netMgr.networkExists(n.getSUID())) // Check whether the network still exists in the Network Manager
					netMgr.destroyNetwork(n);
			} catch (IllegalArgumentException e) {
				// The manager throws this exception if the network is not registered (or not anymore),
				// so it is probably safe to catch it here and just log it, instead of interrupting the whole action.
				logger.warn("Error when trying to destroy network: " + n, e);
			}
		}
		
		netMgr.reset();

		// Destroy table styles. Must be done before destroying network styles because style associations refer to them.
		var tvmMgr = serviceRegistrar.getService(TableVisualMappingManager.class);

		for (var colView : tvmMgr.getAllVisualStylesMap().keySet()) {
			tvmMgr.setVisualStyle(colView, null);
		}
		for (var association : tvmMgr.getAllStyleAssociations()) {
			var netStyle = association.networkVisualStyle();
			var colName = association.colName();
			var tableType = association.tableType();
			tvmMgr.setAssociatedVisualStyle(netStyle, tableType, colName, null);
		}
				
		// Destroy network styles
		logger.debug("Removing current visual styles...");
		var vmMgr = serviceRegistrar.getService(VisualMappingManager.class);
		var defaultStyle = vmMgr.getDefaultVisualStyle();
		var allStyles = new ArrayList<>(vmMgr.getAllVisualStyles());

		for (var vs : allStyles) {
			if (!vs.equals(defaultStyle))
				vmMgr.removeVisualStyle(vs);
		}
		
		// Destroy tables
		var tblMgr = serviceRegistrar.getService(CyTableManager.class);
		tblMgr.reset();
		
		// Reset groups
		serviceRegistrar.getService(CyGroupManager.class).reset();
		
		// Unregister session properties
		var cyPropsClone = getAllProperties();
		
		for (var cyProps : cyPropsClone) {
			if (cyProps.getSavePolicy().equals(CyProperty.SavePolicy.SESSION_FILE)) {
				serviceRegistrar.unregisterAllServices(cyProps);
				sessionProperties.remove(cyProps.getName());
			}
		}
		
		// Clear undo stack
		serviceRegistrar.getService(UndoSupport.class).reset();
		
		// Reset current table and rendering engine
		var appMgr = serviceRegistrar.getService(CyApplicationManager.class);
		appMgr.reset();
		
		currentFileName = null;
		disposed = true;
	}
}
