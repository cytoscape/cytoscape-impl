package org.cytoscape.io.internal.read.session;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cytoscape.io.internal.read.datatable.CSVCyReaderFactory;
import org.cytoscape.io.internal.read.datatable.CyTablesXMLReader;
import org.cytoscape.io.internal.read.session.CyTableMetadataImpl.CyTableMetadataBuilder;
import org.cytoscape.io.internal.read.xgmml.SessionXGMMLNetworkViewReader;
import org.cytoscape.io.internal.util.GroupUtil;
import org.cytoscape.io.internal.util.ReadCache;
import org.cytoscape.io.internal.util.SUIDUpdater;
import org.cytoscape.io.internal.util.cytables.model.VirtualColumn;
import org.cytoscape.io.internal.util.session.SessionUtil;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.io.read.CyNetworkReaderManager;
import org.cytoscape.io.read.CyPropertyReader;
import org.cytoscape.io.read.CyPropertyReaderManager;
import org.cytoscape.io.read.CyTableReader;
import org.cytoscape.io.read.VizmapReader;
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
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.SimpleCyProperty;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;

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
	
	private final Map<Long/*network_suid*/, CyNetwork> networkLookup = new LinkedHashMap<Long, CyNetwork>();
	private final Map<Long/*old_network_id*/, Set<CyTableMetadataBuilder>> networkTableMap = new HashMap<Long, Set<CyTableMetadataBuilder>>();

	private final SUIDUpdater suidUpdater;
	private final CyNetworkReaderManager networkReaderMgr;
	private final CyPropertyReaderManager propertyReaderMgr;
	private final VizmapReaderManager vizmapReaderMgr;
	private final CSVCyReaderFactory csvCyReaderFactory;
	private final CyNetworkTableManager networkTableMgr;
	private final CyRootNetworkManager rootNetworkMgr;

	private Map<String, CyTable> filenameTableMap;
	private Map<CyTableMetadataBuilder, String> builderFilenameMap;

	private List<VirtualColumn> virtualColumns;
	private boolean networksExtracted;


	public Cy3SessionReaderImpl(final InputStream sourceInputStream,
							    final ReadCache cache,
							    final GroupUtil groupUtil,
							    final SUIDUpdater suidUpdater,
							    final CyNetworkReaderManager networkReaderMgr,
							    final CyPropertyReaderManager propertyReaderMgr,
							    final VizmapReaderManager vizmapReaderMgr,
							    final CSVCyReaderFactory csvCyReaderFactory,
							    final CyNetworkTableManager networkTableMgr,
							    final CyRootNetworkManager rootNetworkMgr) {
		super(sourceInputStream, cache, groupUtil);

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

		if (networkTableMgr == null) throw new NullPointerException("network table manager is null.");
		this.networkTableMgr = networkTableMgr;
		
		if (rootNetworkMgr == null) throw new NullPointerException("root network manager is null.");
		this.rootNetworkMgr = rootNetworkMgr;

		filenameTableMap = new HashMap<String, CyTable>();
		builderFilenameMap = new HashMap<CyTableMetadataBuilder, String>();
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
			} else if (!entryName.endsWith(VERSION_EXT)) {
				logger.warn("Unknown entry found in session zip file!\n" + entryName);
			}
		} else {
			// Second pass..
			if (!entryName.contains("/" + APPS_FOLDER) && entryName.endsWith(XGMML_EXT)) {
				// Now the network views can be extracted!
				Matcher matcher = NETWORK_VIEW_PATTERN.matcher(entryName);
				
				if (matcher.matches()) {
					extractNetworkView(is, entryName);
				}
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
		
		tm.setProgress(0.6);
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
		CyTablesXMLReader reader = new CyTablesXMLReader(is);
		try {
			reader.run(taskMonitor);
			virtualColumns = reader.getCyTables().getVirtualColumns().getVirtualColumn();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	private void extractTable(InputStream stream, String entryName) throws Exception {
		CyTableReader reader = (CyTableReader) csvCyReaderFactory.createTaskIterator(stream, entryName).next();
		reader.run(taskMonitor);

		// Assume one table per entry
		CyTable table = reader.getTables()[0];
		Matcher matcher = NETWORK_TABLE_PATTERN.matcher(entryName);
		
		if (matcher.matches()) {
			String networkName = SessionUtil.unescape(matcher.group(2));
			Long oldNetId = getOldNetworkId(networkName);
			
			if (oldNetId == null) {
				throw new NullPointerException("Cannot extract table. Network SUID is null for entry: " + entryName);
			}
			
			String namespace = SessionUtil.unescape(matcher.group(3));
			Class<?> type = Class.forName(SessionUtil.unescape(matcher.group(4)));
			String title = SessionUtil.unescape(matcher.group(5));
			table.setTitle(title);
			CyTableMetadataBuilder builder = new CyTableMetadataBuilder().setCyTable(table).setNamespace(namespace)
					.setType(type);
			Set<CyTableMetadataBuilder> builders = networkTableMap.get(oldNetId);
			
			if (builders == null) {
				builders = new HashSet<CyTableMetadataBuilder>();
				networkTableMap.put(oldNetId, builders);
			}
			
			builders.add(builder);
			
			String filename = matcher.group(1);
			filenameTableMap.put(filename, table);
			builderFilenameMap.put(builder, filename);
			
			return;
		}

		matcher = GLOBAL_TABLE_PATTERN.matcher(entryName);
		
		if (matcher.matches()) {
			String title = SessionUtil.unescape(matcher.group(3));
			table.setTitle(title);
			CyTableMetadataBuilder builder = new CyTableMetadataBuilder().setCyTable(table).setNetwork(null);
			tableMetadata.add(builder.build());
			
			String filename = matcher.group(1);
			filenameTableMap.put(filename, table);
			builderFilenameMap.put(builder, filename);
			
			// Look for SUID-type columns--only global tables now
			suidUpdater.addTable(table);
		}
	}

	private void extractNetworks(InputStream is, String entryName) throws Exception {
		CyNetworkReader reader = networkReaderMgr.getReader(is, entryName);
		reader.run(taskMonitor);
		CyNetwork[] netArray = reader.getNetworks();
		
		for (CyNetwork net : netArray) {
			// Add its root-network to the lookup map first
			CyRootNetwork rootNet = rootNetworkMgr.getRootNetwork(net);
			
			if (!networkLookup.containsKey(rootNet.getSUID()));
				networkLookup.put(rootNet.getSUID(), rootNet);
			
			networkLookup.put(net.getSUID(), net);
			networks.add(net); // Note: do NOT add the root-network to this set!
		}
	}
	
	private void extractNetworkView(InputStream is, String entryName) throws Exception {
		// Get the token which identifies the network
		Matcher matcher = NETWORK_VIEW_PATTERN.matcher(entryName);
		Long oldNetId = null;
		
		if (matcher.matches()) {
			String netViewToken = matcher.group(2);
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
			final CyNetwork network = cache.getNetwork(oldNetId);
			
			if (network != null) {
				// Create the view
				final CyNetworkReader reader = networkReaderMgr.getReader(is, entryName);
				reader.run(taskMonitor);
				
				final CyNetworkView view = reader.buildCyNetworkView(network);
				networkViews.add(view);
				
				// Get its visual style name
				if (reader instanceof SessionXGMMLNetworkViewReader) {
					final String vsName = ((SessionXGMMLNetworkViewReader) reader).getVisualStyleName();
					
					if (vsName != null && !vsName.isEmpty())
						this.visualStyleMap.put(view, vsName);
				}
			}
		} else {
			logger.error("The network view will cannot be recreated. The network view entry is invalid: " + entryName);
		}
	}

	private void extractAppEntry(InputStream is, String entryName) {
		final String[] items = entryName.split("/");

		if (items.length < 3) {
			// It's a directory name, not a file name
			return;
		}

		String appName = items[2];
		String fileName = items[items.length - 1];

		final String tmpDir = System.getProperty(TEMP_DIR);
		final File file = new File(tmpDir, fileName);

		try {
			file.deleteOnExit();
		} catch (Exception e) {
			logger.warn("This temporary app file may not be deleted on exit: " + file.getAbsolutePath(), e);
		}
		
		try {
			// Write input stream into temp file (Use binary streams to support images/movies/etc.)
			final BufferedInputStream bin = new BufferedInputStream(is);
			final BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(file));
			final byte buf[] = new byte[256];
			
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
			appFileListMap.put(appName, new ArrayList<File>());

		List<File> fileList = appFileListMap.get(appName);
		fileList.add(file);
	}

	private void extractVizmap(InputStream is, String entryName) throws Exception {
		VizmapReader reader = vizmapReaderMgr.getReader(is, entryName);
		reader.run(taskMonitor);
		visualStyles.addAll(reader.getVisualStyles());
	}

	private void extractProperties(InputStream is, String entryName) throws Exception {
		CyPropertyReader reader = propertyReaderMgr.getReader(is, entryName);
		
		if (reader == null)
			return;
		
		reader.run(taskMonitor);
		
		CyProperty<?> cyProps = null;
		Object obj = reader.getProperty();
		
		if (obj instanceof Properties) {
			Properties props = (Properties) obj;
			Matcher matcher = PROPERTIES_PATTERN.matcher(entryName);
			
			if (matcher.matches()) {
				String propsName = matcher.group(2);
				
				if (propsName != null) {
					cyProps = new SimpleCyProperty<Properties>(propsName, props, Properties.class,
							CyProperty.SavePolicy.SESSION_FILE);
					
				}
			}
		} else if (obj instanceof Bookmarks) {
			cyProps = new SimpleCyProperty<Bookmarks>("bookmarks", (Bookmarks)obj, Bookmarks.class,
					CyProperty.SavePolicy.SESSION_FILE);
		} else {
			// TODO: get name and create the CyProperty for unknown types
			logger.error("Cannot extract CyProperty name from: " + entryName);
		}
		
		if (cyProps != null)
			properties.add(cyProps);
	}
	
	private void restoreVirtualColumns() {
		if (virtualColumns == null) {
			return;
		}
		
		for (VirtualColumn columnData : virtualColumns) {
			CyTable targetTable = filenameTableMap.get(columnData.getTargetTable());
			
			if (targetTable.getColumn(columnData.getName()) == null) {
				CyTable sourceTable = filenameTableMap.get(columnData.getSourceTable());
				
				try {
					targetTable.addVirtualColumn(columnData.getName(),
												 columnData.getSourceColumn(),
												 sourceTable,
												 columnData.getTargetJoinKey(),
												 columnData.isImmutable());
				} catch (Exception e) {
					logger.error("Error restoring virtual column \"" + columnData.getName() + "\" in table \"" + 
							targetTable + "\"(" + columnData.getTargetTable() + ")--source table: \"" + sourceTable + 
							"\"(" + columnData.getSourceTable() + ")", e);
				}
			}
		}
	}

	private final void mergeNetworkTables() throws UnsupportedEncodingException {
		for (final Entry<Long, Set<CyTableMetadataBuilder>> entry : networkTableMap.entrySet()) {
			final Object oldId = entry.getKey();
			final Set<CyTableMetadataBuilder> builders = entry.getValue();
			final CyNetwork network = cache.getNetwork(oldId);

			if (network == null) {
				logger.error("Cannot merge network tables: Cannot find network " + oldId);
				continue;
			}

			for (final CyTableMetadataBuilder builder : builders) {
				if (cancelled) return;
				
				builder.setNetwork(network);
				mergeNetworkTable(network, builder);
				CyTableMetadata metadata = builder.build();
				tableMetadata.add(metadata);
				
				// Update filename<->table maps
				final String filename = builderFilenameMap.get(builder);
				filenameTableMap.put(filename, metadata.getTable());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private final void mergeNetworkTable(CyNetwork network, CyTableMetadataBuilder builder) {
		final Class<? extends CyIdentifiable> type = (Class<? extends CyIdentifiable>) builder.getType();
		final String namespace = builder.getNamespace();
		final CyTable src = builder.getTable();
		final CyTable tgt = networkTableMgr.getTable(network, type, namespace);
		
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
	
	private void mergeTables(final CyTable source, final CyTable target, final Class<? extends CyIdentifiable> type) {
		CyColumn sourceKey = source.getPrimaryKey();
		CyColumn targetKey = target.getPrimaryKey();
		String keyName = sourceKey.getName();

		// Make sure keys match
		if (keyName.equals(targetKey.getName())) {
			// Merge columns first, because even if the source table has no rows to merge,
			// the columns have to be restored
			mergeColumns(keyName, source, target);
			
			for (CyRow sourceRow : source.getAllRows()) {
				Long key = sourceRow.get(keyName, Long.class);
				CyIdentifiable entry = cache.getObjectById(key, type);
				Long mappedKey = entry != null ? entry.getSUID() : null;
				
				if (mappedKey == null)
					mappedKey = key;

				CyRow targetRow = target.getRow(mappedKey);
				mergeRow(keyName, sourceRow, targetRow);
			}
		}
	}

	private void mergeColumns(final String keyName, final CyTable source, final CyTable target) {
		for (CyColumn column : source.getColumns()) {
			String columnName = column.getName();

			if (columnName.equals(keyName))
				continue;

			if (target.getColumn(columnName) == null) {
				Class<?> type = column.getType();
				boolean immutable = column.isImmutable();
	
				if (type.equals(List.class)) {
					Class<?> elementType = column.getListElementType();
					target.createListColumn(columnName, elementType, immutable);
				} else {
					target.createColumn(columnName, type, immutable);
				}
			}
		}
	}

	private void mergeRow(String keyName, CyRow sourceRow, CyRow targetRow) {
		for (CyColumn column : sourceRow.getTable().getColumns()) {
			String columnName = column.getName();

			if (columnName.equals(keyName))
				continue;

			Class<?> type = column.getType();

			if (type.equals(List.class)) {
				Class<?> elementType = column.getListElementType();
				List<?> list = sourceRow.getList(columnName, elementType);
				targetRow.set(columnName, list);
			} else {
				Object value = sourceRow.get(columnName, type);
				targetRow.set(columnName, value);
			}
		}
	}
	
	private void updateSUIDColumns() {
		suidUpdater.updateSUIDColumns();
	}
	
	/**
	 * @param networkToken
	 * @return
	 */
	private Long getOldNetworkId(final String networkToken) {
		Long id = null;
		Matcher matcher = NETWORK_NAME_PATTERN.matcher(networkToken);
		
		if (matcher.matches()) {
			String s = matcher.group(1);
			
			try {
				id = Long.valueOf(s);
			} catch (NumberFormatException nfe) {
				logger.error("Cannot extract network SUID from: " + networkToken);
			}
		}
		
		return id;
	}
}
