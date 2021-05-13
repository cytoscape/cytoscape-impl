package org.cytoscape.io.internal.read.session;

import static org.cytoscape.io.internal.util.session.SessionUtil.APPS_FOLDER;
import static org.cytoscape.io.internal.util.session.SessionUtil.CYTABLE_STATE_FILE;
import static org.cytoscape.io.internal.util.session.SessionUtil.NETWORKS_FOLDER;
import static org.cytoscape.io.internal.util.session.SessionUtil.NETWORK_VIEWS_FOLDER;
import static org.cytoscape.io.internal.util.session.SessionUtil.PROPERTIES_FOLDER;
import static org.cytoscape.io.internal.util.session.SessionUtil.TABLE_EXT;
import static org.cytoscape.io.internal.util.session.SessionUtil.VERSION_EXT;
import static org.cytoscape.io.internal.util.session.SessionUtil.VIZMAP_XML_FILE;
import static org.cytoscape.io.internal.util.session.SessionUtil.XGMML_EXT;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.equations.EquationUtil;
import org.cytoscape.io.internal.read.datatable.CSVCyReaderFactory;
import org.cytoscape.io.internal.read.datatable.CyTablesXMLReader;
import org.cytoscape.io.internal.read.session.CyTableMetadataImpl.CyTableMetadataBuilder;
import org.cytoscape.io.internal.read.xgmml.SessionXGMMLNetworkViewReader;
import org.cytoscape.io.internal.util.GroupUtil;
import org.cytoscape.io.internal.util.ReadCache;
import org.cytoscape.io.internal.util.SUIDUpdater;
import org.cytoscape.io.internal.util.cytables.model.CyTables;
import org.cytoscape.io.internal.util.cytables.model.TableView;
import org.cytoscape.io.internal.util.cytables.model.VirtualColumn;
import org.cytoscape.io.internal.util.session.SessionUtil;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.io.read.CyPropertyReaderManager;
import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableMetadata;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.table.CyColumnViewMetadata;
import org.cytoscape.view.model.table.CyRowViewMetadata;
import org.cytoscape.view.model.table.CyTableViewMetadata;
import org.cytoscape.work.TaskMonitor;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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
 * Session reader implementation that handles the Cytoscape 3 session format.
 * 
 * @see org.cytoscape.io.internal.read.session.Cy2SessionReaderImpl
 * @see org.cytoscape.io.internal.write.session.SessionWriterImpl
 */
public class Cy3SessionReaderImpl extends AbstractSessionReader {
	
	private static final String TEMP_DIR = "java.io.tmpdir";

	public static final Pattern NETWORK_PATTERN = Pattern.compile(".*/"+NETWORKS_FOLDER+"(([^/]+)[.]xgmml)");
	public static final Pattern NETWORK_NAME_PATTERN = Pattern.compile("(\\d+)(-(.+))?");
	public static final Pattern NETWORK_VIEW_PATTERN = Pattern.compile(".*/"+NETWORK_VIEWS_FOLDER+"(([^/]+)[.]xgmml)");
	public static final Pattern NETWORK_VIEW_NAME_PATTERN = Pattern.compile("(\\d+)-(\\d+)(-(.+))?"); // netId_viewId_title
	public static final Pattern NETWORK_TABLE_PATTERN = Pattern.compile(".*/(([^/]+)/([^/]+)-([^/]+)-([^/]+)[.]cytable)");
	public static final Pattern GLOBAL_TABLE_PATTERN = Pattern.compile(".*/(global/(\\d+)-([^/]+)[.]cytable)");
	public static final Pattern PROPERTIES_PATTERN = Pattern.compile(".*/"+PROPERTIES_FOLDER+"?(([^/]+)[.](props|properties))");
	
	private static final String THUMBNAIL_FILE = "session_thumbnail.png";
	
	private final Map<Long/*network_suid*/, CyNetwork> networkLookup = new LinkedHashMap<>();
	private final Map<Long/*old_network_id*/, Set<CyTableMetadataBuilder>> networkTableMap = new HashMap<>();

	private final SUIDUpdater suidUpdater;
	private final CyNetworkReaderManager networkReaderMgr;
	private final CyPropertyReaderManager propertyReaderMgr;
	private final VizmapReaderManager vizmapReaderMgr;
	private final CSVCyReaderFactory csvCyReaderFactory;

	protected final Map<String, CyTable> filenameTableMap;
	private Map<CyTableMetadataBuilder, String> builderFilenameMap;

	protected final List<VirtualColumn> virtualColumns;
	private boolean networksExtracted;


	public Cy3SessionReaderImpl(
			InputStream sourceInputStream, 
			ReadCache cache, 
			GroupUtil groupUtil,
			SUIDUpdater suidUpdater, 
			CyNetworkReaderManager networkReaderMgr, 
			CyPropertyReaderManager propertyReaderMgr,
			VizmapReaderManager vizmapReaderMgr, 
			CSVCyReaderFactory csvCyReaderFactory,
			CyServiceRegistrar serviceRegistrar
	) {
		super(sourceInputStream, cache, groupUtil, serviceRegistrar);

		if (suidUpdater == null) throw new NullPointerException("SUID updater is null.");
		this.suidUpdater = suidUpdater;
		
		if (networkReaderMgr == null) throw new NullPointerException("network reader manager is null.");
		this.networkReaderMgr = networkReaderMgr;
		
		if (propertyReaderMgr == null) throw new NullPointerException("property reader manager is null.");
		this.propertyReaderMgr = propertyReaderMgr;

		if (vizmapReaderMgr == null) throw new NullPointerException("vizmap reader manager is null.");
		this.vizmapReaderMgr = vizmapReaderMgr;
		
		if (csvCyReaderFactory == null) throw new NullPointerException("table reader manager is null.");
		this.csvCyReaderFactory = csvCyReaderFactory;

		virtualColumns = new LinkedList<>();
		filenameTableMap = new HashMap<>();
		builderFilenameMap = new HashMap<>();
	}
	
	@Override
	protected void init(TaskMonitor tm) throws Exception {
		super.init(tm);
		suidUpdater.init();
	}
	
	@Override
	protected void handleEntry(InputStream is, String entryName) throws Exception {
		if (!networksExtracted) {
			// First pass..
			if (entryName.contains("/" + APPS_FOLDER)) {
				extractAppEntry(is, entryName);
			} else if (entryName.endsWith(VIZMAP_XML_FILE)) {
				extractVizmap(is, entryName);
			} else if (entryName.contains("/" + PROPERTIES_FOLDER)) {
				extractProperties(is, entryName);
			} else if (entryName.endsWith(XGMML_EXT)) {
				// Ignore network view files for now...
				Matcher matcher = NETWORK_PATTERN.matcher(entryName);
				if (matcher.matches()) {
					extractNetworks(is, entryName);
				}
			} else if (entryName.endsWith(TABLE_EXT)) {
				extractTable(is, entryName);
			} else if (entryName.endsWith(CYTABLE_STATE_FILE)) {
				extractCyTableSessionState(is, entryName);
			} else if (!entryName.endsWith(VERSION_EXT) && !entryName.endsWith("/" + THUMBNAIL_FILE)) {
				logger.warn("Unknown entry found in session zip file!\n" + entryName);
			}
		} else {
			// Second pass..
			if (!entryName.contains("/" + APPS_FOLDER) && entryName.endsWith(XGMML_EXT)) {
				// Now the network views can be extracted!
				var matcher = NETWORK_VIEW_PATTERN.matcher(entryName);
				
				if (matcher.matches()) {
					extractNetworkView(is, entryName);
				}
			} else if (entryName.endsWith(CYTABLE_STATE_FILE)) {
				extractTableViews(is, entryName);
			}
		}
	}

	@Override
	protected void complete(TaskMonitor tm) throws Exception {
		tm.setProgress(0.4);
		tm.setTitle("Set network tables");
		tm.setStatusMessage("Setting network tables...");
		mergeNetworkTables();
		
		tm.setProgress(0.5);
		tm.setTitle("Restore virtual columns");
		tm.setStatusMessage("Restoring virtual columns...");
		restoreVirtualColumns();
		
		tm.setProgress(0.55);
		tm.setTitle("Restore equations");
		tm.setStatusMessage("Restoring equations...");
		restoreEquations();
		
		tm.setProgress(0.6);
		tm.setTitle("Update network columns");
		tm.setStatusMessage("Moving column \"" + CY2_PARENT_NETWORK_COLUMN + "\"...");
		moveParentNetworkColumn();
		
		tm.setProgress(0.65);
		tm.setTitle("Extract network views");
		tm.setStatusMessage("Extracting network views...");
		// Read the session file again, this time to extract the network views
		networksExtracted = true;
		readSessionFile(tm);
		
		tm.setProgress(0.8);
		tm.setTitle("Update SUID columns");
		tm.setStatusMessage("Updating SUID columns...");
		updateSUIDColumns();
		
		super.complete(tm);
	}

	@Override
	protected void createObjectMap() {
		objectMap.put(CyNetwork.class, cache.getNetworkByIdMap());
		objectMap.put(CyNetworkView.class, cache.getNetworkViewByIdMap());
		objectMap.put(CyNode.class, cache.getNodeByIdMap());
		objectMap.put(CyEdge.class, cache.getEdgeByIdMap());
	}
	
	private void extractCyTableSessionState(InputStream is, String entryName) throws IOException {
		var reader = new CyTablesXMLReader(is);
		
		try {
			reader.run(taskMonitor);
			var cyTables = reader.getCyTables();
			
			virtualColumns.addAll(cyTables.getVirtualColumns().getVirtualColumn());
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	private void extractTable(InputStream stream, String entryName) throws Exception {
		var reader = (CyTableReader) csvCyReaderFactory.createTaskIterator(stream, entryName).next();
		reader.run(taskMonitor);

		// Assume one table per entry
		var table = reader.getTables()[0];
		var matcher = NETWORK_TABLE_PATTERN.matcher(entryName);
		
		if (matcher.matches()) {
			var networkName = SessionUtil.unescape(matcher.group(2));
			var oldNetId = getOldNetworkId(networkName);
			
			if (oldNetId == null)
				throw new NullPointerException("Cannot extract table. Network SUID is null for entry: " + entryName);
			
			var namespace = SessionUtil.unescape(matcher.group(3));
			var type = Class.forName(SessionUtil.unescape(matcher.group(4)));
			var title = SessionUtil.unescape(matcher.group(5));
			table.setTitle(title);
			var builder = new CyTableMetadataBuilder().setCyTable(table).setNamespace(namespace).setType(type);
			var builders = networkTableMap.get(oldNetId);
			
			if (builders == null) {
				builders = new HashSet<>();
				networkTableMap.put(oldNetId, builders);
			}
			
			builders.add(builder);
			
			var filename = matcher.group(1);
			filenameTableMap.put(filename, table);
			builderFilenameMap.put(builder, filename);
			
			return;
		}

		matcher = GLOBAL_TABLE_PATTERN.matcher(entryName);
		
		if (matcher.matches()) {
			var title = SessionUtil.unescape(matcher.group(3));
			table.setTitle(title);
			var builder = new CyTableMetadataBuilder().setCyTable(table).setNetwork(null);
			tableMetadata.add(builder.build());
			
			var filename = matcher.group(1);
			filenameTableMap.put(filename, table);
			builderFilenameMap.put(builder, filename);

			// Add the SUID for this table
			var tableSUID = Long.parseLong(matcher.group(2));
			suidUpdater.addSUIDMapping(tableSUID, table.getSUID());
			
			// Look for SUID-type columns--only global tables now
			suidUpdater.addTable(table);
		}
	}

	private void extractNetworks(InputStream is, String entryName) throws Exception {
		var reader = networkReaderMgr.getReader(is, entryName);
		reader.run(taskMonitor);
		
		var rootNetworkManager = serviceRegistrar.getService(CyRootNetworkManager.class);
		var netArray = reader.getNetworks();
		
		for (var net : netArray) {
			// Add its root-network to the lookup map first
			var rootNet = rootNetworkManager.getRootNetwork(net);
			
			if (!networkLookup.containsKey(rootNet.getSUID()));
				networkLookup.put(rootNet.getSUID(), rootNet);
			
			networkLookup.put(net.getSUID(), net);
			networks.add(net); // Note: do NOT add the root-network to this set!
		}
	}
	
	private void extractNetworkView(InputStream is, String entryName) throws Exception {
		// Get the token which identifies the network
		var matcher = NETWORK_VIEW_PATTERN.matcher(entryName);
		Long oldNetId = null;
		
		if (matcher.matches()) {
			var netViewToken = matcher.group(2);
			matcher = NETWORK_VIEW_NAME_PATTERN.matcher(netViewToken);
			
			if (matcher.matches()) {
				try {
					oldNetId = Long.valueOf(matcher.group(1));
				} catch (NumberFormatException nfe) {
					logger.error("Cannot extract network view SUID from: " + netViewToken);
				}
			}
		}
		
		if (oldNetId != null) {
			var network = cache.getNetwork(oldNetId);
			
			if (network != null && !cancelled) {
				// Create the view
				var reader = networkReaderMgr.getReader(is, entryName);
				reader.run(taskMonitor);
				
				var view = reader.buildCyNetworkView(network);
				networkViews.add(view);
				
				// Get its visual style name
				if (reader instanceof SessionXGMMLNetworkViewReader) {
					var vsName = ((SessionXGMMLNetworkViewReader) reader).getVisualStyleName();
					
					if (vsName != null && !vsName.isEmpty())
						visualStyleMap.put(view, vsName);
				}
			}
		} else {
			logger.error("The network view will cannot be recreated. The network view entry is invalid: " + entryName);
		}
	}
	
	private void extractTableViews(InputStream is, String entryName) throws Exception {
		var reader = new CyTablesXMLReader(is);
		
		final CyTables xmlTables;
		
		try {
			reader.run(taskMonitor);
			xmlTables = reader.getCyTables();
		} catch (Exception e) {
			throw new IOException(e);
		}
		
		if (xmlTables.getTableViews() != null) {
			var xmlTableViews = xmlTables.getTableViews().getTableView();
			
			for (var xmlTableView : xmlTableViews) {
				var table = lookupTable(xmlTableView);
				var keyCol = table.getTable().getPrimaryKey();
				
				var rendererId = xmlTableView.getRendererId();
				var namespace  = xmlTableView.getTableNamespace();
				
				var columnViews = new ArrayList<CyColumnViewMetadata>();
				
				for (var xmlColView : xmlTableView.getColumnView()) {
					var styleTitle = xmlColView.getStyleTitle();
					var colName = xmlColView.getColumnName();
					
					var colBypasses = new HashMap<String, String>();
					
					for (var xmlBypass : xmlColView.getBypassValue()) {
						colBypasses.put(xmlBypass.getName(), xmlBypass.getValue());
					}
					
					columnViews.add(new CyColumnViewMetadata(colName, styleTitle, colBypasses));
				}
				
				boolean isSuid = primaryKeyIsSUID(table.getTable());
				var rowViews = new ArrayList<CyRowViewMetadata>();
				
				for (var xmlRowView : xmlTableView.getRowView()) {
					var keyVal = deserializeKey(xmlRowView.getKey(), keyCol);
					
					if (isSuid)
						keyVal = suidUpdater.getNewSUID((Long)keyVal);
					
					var rowBypasses = new HashMap<String, String>();
					
					for (var xmlBypass : xmlRowView.getBypassValue()) {
						rowBypasses.put(xmlBypass.getName(), xmlBypass.getValue());
					}
					
					rowViews.add(new CyRowViewMetadata(keyVal, rowBypasses));
				}
				
				var tableBypasses = new HashMap<String, String>();
				
				for (var xmlBypass : xmlTableView.getBypassValue()) {
					tableBypasses.put(xmlBypass.getName(), xmlBypass.getValue());
				}
				
				var tableViewMetadata = new CyTableViewMetadata(-1, namespace, rendererId, tableBypasses, 
						columnViews, rowViews, keyCol.getType(), keyCol.getListElementType());
				tableViewMetadata.setUnderlyingTable(table);
				
				tableViews.add(tableViewMetadata);
			}
		}
	}
	
	private static boolean primaryKeyIsSUID(CyTable table) {
		var pk = table.getPrimaryKey();
		return pk.getName().equals(CyIdentifiable.SUID) && pk.getType().equals(Long.class);
	}
	
	/**
	 * See CyTablesXMLWriter.serializeKey(...)
	 */
	private static Object deserializeKey(String key, CyColumn primaryKeyColumn) {
		var type = primaryKeyColumn.getType();
		
		if (type.equals(List.class)) {
			var listElementType = primaryKeyColumn.getListElementType();
			var list = new ArrayList<Object>();
			var values = key.split("|");
			
			for (var item : values) {
				list.add(deserializeNonListValue(item, listElementType));
			}
			
			if (list.size() == 1 && list.get(0) == null) 
				return null;
			
			return list;
		} else {
			return deserializeNonListValue(key, type);
		}
	}
	
	private static Object deserializeNonListValue(String value, Class<?> type) {
		if (type.equals(String.class)) {
			return value;
		} else if(value.isEmpty()) {
			return null;
		} else {
			try {
				if (type.equals(Long.class)) {
					return Long.valueOf(value);
				} else if (type.equals(Boolean.class)) {
					return Boolean.valueOf(value);
				} else if (type.equals(Double.class)) {
					return Double.valueOf(value);
				} else if (type.equals(Integer.class)) {
					return Integer.valueOf(value); 
				}
			} catch (Exception e) { }
		}
		return null;
	}
	
	private CyTableMetadata lookupTable(TableView xmlTableView) {
		var table = filenameTableMap.get(xmlTableView.getTable());
		
		if (table == null)
			return null;
		
		for (var tableMetadata : tableMetadata) {
			if (tableMetadata.getTable().equals(table))
				return tableMetadata;
		}
		
		return null;
	}
	
	private void extractAppEntry(InputStream is, String entryName) {
		var items = entryName.split("/");

		if (items.length < 3) {
			// It's a directory name, not a file name
			return;
		}

		var appName = items[2];
		var fileName = items[items.length - 1];

		var tmpDir = System.getProperty(TEMP_DIR);
		var file = new File(tmpDir, fileName);

		try {
			file.deleteOnExit();
		} catch (Exception e) {
			logger.warn("This temporary app file may not be deleted on exit: " + file.getAbsolutePath(), e);
		}
		
		try {
			// Write input stream into temp file (Use binary streams to support images/movies/etc.)
			var bin = new BufferedInputStream(is);
			var output = new BufferedOutputStream(new FileOutputStream(file));
			var buf = new byte[256];
			
			int len;
			while ((len = bin.read(buf)) != -1 && !cancelled)
				output.write(buf, 0, len);
			
			output.flush();
			output.close();
			bin.close();
		} catch (IOException e) {
			logger.error("Error: read from zip: " + entryName, e);
			return;
		}
		
		if (cancelled) return;

		// Put the file into appFileListMap
		if (!appFileListMap.containsKey(appName))
			appFileListMap.put(appName, new ArrayList<>());

		var fileList = appFileListMap.get(appName);
		fileList.add(file);
	}

	private void extractVizmap(InputStream is, String entryName) throws Exception {
		var reader = vizmapReaderMgr.getReader(is, entryName);
		reader.run(taskMonitor);
		
		networkStyles.addAll(reader.getVisualStyles());
		
		var tableVisualStyles = reader.getTableVisualStyles();
		
		if (tableVisualStyles != null)
			tableStyles.addAll(tableVisualStyles);
	}

	private void extractProperties(InputStream is, String entryName) throws Exception {
		var reader = propertyReaderMgr.getReader(is, entryName);
		
		if (reader == null)
			return;
		
		reader.run(taskMonitor);
		
		CyProperty<?> cyProps = null;
		var obj = reader.getProperty();
		
		if (obj instanceof Properties) {
			var props = (Properties) obj;
			var matcher = PROPERTIES_PATTERN.matcher(entryName);
			
			if (matcher.matches()) {
				var propsName = matcher.group(2);
				
				if (propsName != null)
					cyProps = new SimpleCyProperty<>(propsName, props, Properties.class,
							CyProperty.SavePolicy.SESSION_FILE);
			}
		} else if (obj instanceof Bookmarks) {
			cyProps = new SimpleCyProperty<>("bookmarks", (Bookmarks) obj, Bookmarks.class,
					CyProperty.SavePolicy.SESSION_FILE);
		} else {
			// TODO: get name and create the CyProperty for unknown types
			logger.error("Cannot extract CyProperty name from: " + entryName);
		}
		
		if (cyProps != null)
			properties.add(cyProps);
	}
	
	protected void restoreVirtualColumns() throws Exception {
		if (virtualColumns == null)
			return;
		
		Queue<VirtualColumn> queue = new LinkedList<>();
		queue.addAll(virtualColumns);
		
		// Will be used to prevent infinite loops if there are circular references or missing table/columns
		VirtualColumn markedColumn = null; // First column marked as depending on a missing column
		int lastSize = queue.size();
		
		while (!queue.isEmpty()) {
			if (cancelled) return;
			
			var vcData = queue.poll();
			var tgtTable = filenameTableMap.get(vcData.getTargetTable());
			var colName = vcData.getName();
			
			if (tgtTable.getColumn(colName) == null) {
				var srcTable = filenameTableMap.get(vcData.getSourceTable());
				var srcColName = vcData.getSourceColumn();
				var tgtJoinKey = vcData.getTargetJoinKey();
				
				if (srcTable.getColumn(srcColName) != null && tgtTable.getColumn(tgtJoinKey) != null) {
					try {
						tgtTable.addVirtualColumn(colName, srcColName, srcTable, tgtJoinKey, vcData.isImmutable());
						markedColumn = null; // Reset it!
					} catch (Exception e) {
						throw new Exception("Error restoring virtual column \"" + colName + "\" in table \"" + 
								tgtTable + "\"(" + vcData.getTargetTable() + ")--source table: \"" + srcTable + 
								"\"(" + vcData.getSourceTable() + ")", e);
					}
				} else {
					queue.add(vcData);
					
					if (markedColumn == null) {
						// Mark this element and save the queue's size
						markedColumn = vcData;
						lastSize = queue.size();
					} else if (vcData == markedColumn && queue.size() == lastSize) {
						// The iteration reached the same marked column again and the queue's size hasn't decreased,
						// which means that the remaining elements in the queue cannot be resolved
						var msg = new StringBuilder(
								"Cannot restore the following virtual columns because of missing or circular dependencies: ");
						var prefix = "";
						
						for (var vc : queue) {
							msg.append(prefix + vc.getTargetTable() + "." + vc.getName());
							prefix = ", ";
						}
						
						throw new Exception(msg.toString());
					}
				}
			}
		}
	}

	private final void mergeNetworkTables() throws UnsupportedEncodingException {
		var networkTableManager = serviceRegistrar.getService(CyNetworkTableManager.class);
		
		for (var entry : networkTableMap.entrySet()) {
			var oldId = entry.getKey();
			var builders = entry.getValue();
			var network = cache.getNetwork(oldId);

			if (network == null) {
				logger.error("Cannot merge network tables: Cannot find network " + oldId);
				continue;
			}

			for (var builder : builders) {
				if (cancelled) return;
				
				builder.setNetwork(network);
				mergeNetworkTable(network, builder, networkTableManager);
				
				var metadata = builder.build();
				tableMetadata.add(metadata);
				
				// Update filename<->table maps
				var filename = builderFilenameMap.get(builder);
				filenameTableMap.put(filename, metadata.getTable());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private final void mergeNetworkTable(CyNetwork network, CyTableMetadataBuilder builder,
			CyNetworkTableManager networkTableMgr) {
		var type = (Class<? extends CyIdentifiable>) builder.getType();
		var namespace = builder.getNamespace();
		var src = builder.getTable();
		var tgt = networkTableMgr.getTable(network, type, namespace);
		
		if (tgt == null) {
			// Just use the source table
			networkTableMgr.setTable(network, type, namespace, src);
			builder.setCyTable(src);
			
			suidUpdater.addTable(src);
		} else {
			mergeTables(src, tgt, type);
			builder.setCyTable(tgt);
			
			suidUpdater.addTable(tgt);
		}
	}
	
	private void mergeTables(CyTable source, CyTable target, Class<? extends CyIdentifiable> type) {
		var sourceKey = source.getPrimaryKey();
		var targetKey = target.getPrimaryKey();
		var keyName = sourceKey.getName();

		// Make sure keys match
		if (keyName.equals(targetKey.getName())) {
			// Merge columns first, because even if the source table has no rows to merge,
			// the columns have to be restored
			mergeColumns(keyName, source, target);
			
			for (var sourceRow : source.getAllRows()) {
				if (cancelled) return;
				
				var key = sourceRow.get(keyName, Long.class);
				var entry = cache.getObjectById(key, type);
				var mappedKey = entry != null ? entry.getSUID() : null;
				
				if (mappedKey == null)
					mappedKey = key;

				var targetRow = target.getRow(mappedKey);
				mergeRow(keyName, sourceRow, targetRow);
			}
		}
	}

	private void mergeColumns(String keyName, CyTable source, CyTable target) {
		for (var column : source.getColumns()) {
			var columnName = column.getName();

			if (columnName.equals(keyName))
				continue;

			if (target.getColumn(columnName) == null) {
				var type = column.getType();
				boolean immutable = column.isImmutable();
	
				if (type.equals(List.class)) {
					var elementType = column.getListElementType();
					target.createListColumn(columnName, elementType, immutable);
				} else {
					target.createColumn(columnName, type, immutable);
				}
			}
		}
	}

	private void mergeRow(String keyName, CyRow sourceRow, CyRow targetRow) {
		for (var column : sourceRow.getTable().getColumns()) {
			if (cancelled) return;
			
			var columnName = column.getName();

			if (columnName.equals(keyName))
				continue;

			var value = sourceRow.getRaw(columnName);
			targetRow.set(columnName, value);
		}
	}
	
	private void updateSUIDColumns() {
		suidUpdater.updateSUIDColumns();
	}
	
	/**
	 * @param networkToken
	 * @return
	 */
	private Long getOldNetworkId(String networkToken) {
		Long id = null;
		var matcher = NETWORK_NAME_PATTERN.matcher(networkToken);
		
		if (matcher.matches()) {
			var s = matcher.group(1);
			
			try {
				id = Long.valueOf(s);
			} catch (NumberFormatException nfe) {
				logger.error("Cannot extract network SUID from: " + networkToken);
			}
		}
		
		return id;
	}
	
	private void restoreEquations() {
		var compiler = serviceRegistrar.getService(EquationCompiler.class);
		
		for (var network : networkLookup.values()) {
			EquationUtil.refreshEquations(network.getDefaultNetworkTable(), compiler);
			EquationUtil.refreshEquations(network.getDefaultNodeTable(), compiler);
			EquationUtil.refreshEquations(network.getDefaultEdgeTable(), compiler);
		}
	}

	private void moveParentNetworkColumn() {
		for (var net : networks) {
			try {
				var tbl = net.getRow(net, CyNetwork.LOCAL_ATTRS).getTable();
				
				// Remove this old column from the local table (used until v3.3)
				// and create a new one with the same value in the hidden table
				if (tbl.getColumn(CY2_PARENT_NETWORK_COLUMN) != null) {
					var row = tbl.getRow(net.getSUID());
					var parentSUID = row.get(CY2_PARENT_NETWORK_COLUMN, Long.class);
					
					var hRow = net.getRow(net, CyNetwork.HIDDEN_ATTRS);
					var hTbl = hRow.getTable();
					
					if (hTbl.getColumn(CY3_PARENT_NETWORK_COLUMN) == null)
						hTbl.createColumn(CY3_PARENT_NETWORK_COLUMN, Long.class, false);
					
					if (parentSUID != null)
						hRow.set(CY3_PARENT_NETWORK_COLUMN, parentSUID);
					
					tbl.deleteColumn(CY2_PARENT_NETWORK_COLUMN);
				}
			} catch (Exception e) {
				logger.error("Unexpected error while moving column \"" + CY2_PARENT_NETWORK_COLUMN + "\"", e);
			}
		}
	}
}
