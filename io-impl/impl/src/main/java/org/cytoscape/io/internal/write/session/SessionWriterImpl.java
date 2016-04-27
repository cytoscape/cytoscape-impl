package org.cytoscape.io.internal.write.session;

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
import static org.cytoscape.io.internal.util.session.SessionUtil.BOOKMARKS_FILE;
import static org.cytoscape.io.internal.util.session.SessionUtil.CYS_VERSION;
import static org.cytoscape.io.internal.util.session.SessionUtil.CYTABLE_STATE_FILE;
import static org.cytoscape.io.internal.util.session.SessionUtil.NETWORKS_FOLDER;
import static org.cytoscape.io.internal.util.session.SessionUtil.NETWORK_VIEWS_FOLDER;
import static org.cytoscape.io.internal.util.session.SessionUtil.PROPERTIES_EXT;
import static org.cytoscape.io.internal.util.session.SessionUtil.PROPERTIES_FOLDER;
import static org.cytoscape.io.internal.util.session.SessionUtil.TABLES_FOLDER;
import static org.cytoscape.io.internal.util.session.SessionUtil.VERSION_EXT;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.internal.util.GroupUtil;
import org.cytoscape.io.internal.util.session.SessionUtil;
import org.cytoscape.io.internal.write.datatable.CyTablesXMLWriter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyPropertyWriterManager;
import org.cytoscape.io.write.CyTableWriterManager;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.VizmapWriterManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableMetadata;
import org.cytoscape.model.SavePolicy;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.session.CySession;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Write session states into files and zip them into one session file "*.cys".
 *
 * @see org.cytoscape.io.internal.read.session.Cy2SessionReaderImpl
 * @see org.cytoscape.io.internal.read.session.Cy3SessionReaderImpl
 */
public class SessionWriterImpl extends AbstractTask implements CyWriter {

	private static final Logger logger = LoggerFactory.getLogger(SessionWriterImpl.class);
	
	// Name of CySession file.
	private static final String VIZMAP_FILE = "session_vizmap.xml";
	
	private final String cysessionDocId;
	private final String sessionDir;
	private ZipOutputStream zos; 
	private TaskMonitor taskMonitor;

	private final OutputStream outputStream;
	private final CySession session;
	private final CyPropertyWriterManager propertyWriterMgr;
	private final CyTableWriterManager tableWriterMgr;
	private final VizmapWriterManager vizmapWriterMgr;
	private final CyRootNetworkManager rootNetworkManager;
	private final CyNetworkViewWriterFactory networkViewWriterFactory;
	private final CyFileFilter bookmarksFilter;
	private final CyFileFilter propertiesFilter;
	private final CyFileFilter tableFilter;
	private final CyFileFilter vizmapFilter;
	private final GroupUtil groupUtils;
	private Map<Long, String> tableFilenamesBySUID;
	private Map<String, VisualStyle> tableVisualStylesByName;

	public SessionWriterImpl(final OutputStream outputStream, 
	                         final CySession session, 
	                         final CyRootNetworkManager rootNetworkMgr,
	                         final CyPropertyWriterManager propertyWriterMgr,
	                         final CyTableWriterManager tableWriterMgr,
	                         final VizmapWriterManager vizmapWriterMgr,
	                         final CyNetworkViewWriterFactory networkViewWriterFactory,
	                         final CyFileFilter bookmarksFilter,
	                         final CyFileFilter propertiesFilter,
	                         final CyFileFilter tableFilter,
	                         final CyFileFilter vizmapFilter,
	                         final GroupUtil groupUtils) {
		this.outputStream = outputStream;
		this.session = session;
		this.rootNetworkManager = rootNetworkMgr;
		this.propertyWriterMgr = propertyWriterMgr;
		this.tableWriterMgr = tableWriterMgr;
		this.vizmapWriterMgr = vizmapWriterMgr;
		this.networkViewWriterFactory = networkViewWriterFactory;
		this.bookmarksFilter = bookmarksFilter;
		this.propertiesFilter = propertiesFilter;
		this.tableFilter = tableFilter;
		this.vizmapFilter = vizmapFilter;
		this.groupUtils = groupUtils;

		// For now, session ID is time and date
		final DateFormat df = new SimpleDateFormat("yyyy_MM_dd-HH_mm");
		String now = df.format(new Date());

		cysessionDocId = "CytoscapeSession-" + now;
		sessionDir = cysessionDocId + "/";
		
		tableVisualStylesByName = new HashMap<String, VisualStyle>();
		
		for (final VisualStyle vs : session.getVisualStyles())
			tableVisualStylesByName.put(vs.getTitle(), vs);
	}

	/**
	 * Write current session to a local .cys file.
	 *
	 * @throws Exception
	 */
	@Override
	public void run(TaskMonitor tm) throws Exception {
		this.taskMonitor = tm;
		
		try {
			init(tm);
			write(tm);
		} finally {
			try {
				if (zos != null)
					zos.close();
			} catch (Exception e) {
				logger.error("Error closing zip output stream", e);
			}
		}
		
		complete(tm);
	}

	private void init(TaskMonitor tm) throws Exception {
		tm.setProgress(0.0);
		tm.setTitle("Writing Session File");
		tm.setStatusMessage("Preparing...");
		
		zos = new ZipOutputStream(outputStream);
		prepareGroups(); // Groups require specific metadata
	}
	
	private void write(TaskMonitor tm) throws Exception {
		zipVersion();
		tm.setProgress(0.1);
		
		if (cancelled) return;
		
		tm.setStatusMessage("Saving networks...");
		zipNetworks();
		tm.setProgress(0.3);
		
		if (cancelled) return;
		
		tm.setStatusMessage("Saving network views...");
		zipNetworkViews();
		tm.setProgress(0.4);
		
		if (cancelled) return;
		
		tm.setStatusMessage("Saving tables...");
		zipTables();
		tm.setProgress(0.5);
		
		if (cancelled) return;
		
		tm.setStatusMessage("Saving table properties...");
		zipTableProperties();
		tm.setProgress(0.6);
		
		if (cancelled) return;
		
		tm.setStatusMessage("Saving visual styles...");
		zipVizmap();
		tm.setProgress(0.7);
		
		if (cancelled) return;
		
		tm.setStatusMessage("Saving Cytoscape properties...");
		zipProperties();
		tm.setProgress(0.8);
		
		if (cancelled) return;
		
		zipFileListMap();
		tm.setProgress(0.9);
	}
	
	private void complete(TaskMonitor tm) {
		if (cancelled) return;
		
		// Tell the groupUtils that we're done
		groupUtils.groupsSerialized(session.getNetworks(), session.getNetworkViews());
		
		tm.setStatusMessage("Done.");
		tm.setProgress(1.0);
	}
	
	/**
	 * Writes the version file, which has no content. The file name itself gives the CYS version.
	 */
	private void zipVersion() throws Exception {
		zos.putNextEntry(new ZipEntry(sessionDir + CYS_VERSION + VERSION_EXT));
		zos.closeEntry();
	}
	
	/**
	 * Writes the vizmap.props file to the session zip.
	 */
	private void zipVizmap() throws Exception {
		Set<VisualStyle> styles = session.getVisualStyles();

		zos.putNextEntry(new ZipEntry(sessionDir + VIZMAP_FILE) );

		CyWriter vizmapWriter = vizmapWriterMgr.getWriter(styles, vizmapFilter, zos);
		vizmapWriter.run(taskMonitor);

		zos.closeEntry();
		vizmapWriter = null;
	}

	/**
	 * Writes the cytoscape.props file to the session zip.
	 */
	private void zipProperties() throws Exception {
		for (CyProperty<?> cyProps : session.getProperties()) {
			if (cancelled) return;
			
			String filename;
			CyFileFilter filter;
			Class<?> type = cyProps.getPropertyType();
			
			if (Bookmarks.class.isAssignableFrom(type)) {
				filename = BOOKMARKS_FILE;
				filter = bookmarksFilter;
			} else if (Properties.class.isAssignableFrom(type)) {
				filename = cyProps.getName() + PROPERTIES_EXT;
				filter = propertiesFilter;
			} else {
				//filename = cyProps.getName();
				// TODO: how to get the file extension and the file filter for unknown CyProperty types?
				logger.error("Cannot save CyProperty \"" + cyProps.getName() + "\": type \"" + type + "\" is not supported");
				continue;
			}
			
			zos.putNextEntry(new ZipEntry(sessionDir + PROPERTIES_FOLDER + filename));
			
			CyWriter propertiesWriter = propertyWriterMgr.getWriter(cyProps.getProperties(), filter, zos);
			propertiesWriter.run(taskMonitor);
	
			zos.closeEntry();
			propertiesWriter = null;
		}
	}

	/**
	 * Writes network files to the session zip. 
	 * @throws Exception
	 */
	private void zipNetworks() throws Exception {
		final Set<CyNetwork> networks = session.getNetworks();
		final Set<CyRootNetwork> rootNetworks = new HashSet<CyRootNetwork>();

		// Zip only root-networks, because sub-networks should be automatically saved with them.
		for (final CyNetwork n : networks) {
			final CyRootNetwork rn = rootNetworkManager.getRootNetwork(n);
			
			if (rn.getSavePolicy() == SavePolicy.SESSION_FILE)
				rootNetworks.add(rn);
		}
		
		for (CyRootNetwork rn : rootNetworks) {
			if (cancelled) return;
			
			String xgmmlFile = SessionUtil.getXGMMLFilename(rn);if (xgmmlFile.contains("_ERROR")) throw new Exception("Simulating exception...");
			zos.putNextEntry(new ZipEntry(sessionDir + NETWORKS_FOLDER + xgmmlFile) );
			
			CyWriter writer = networkViewWriterFactory.createWriter(zos, rn);
			writer.run(taskMonitor);
	
			zos.closeEntry();
			writer = null;
		}
	}
	
	/**
	 * Writes network view files to the session zip. 
	 * @throws Exception
	 */
	private void zipNetworkViews() throws Exception {
		final Set<CyNetworkView> netViews = session.getNetworkViews();

		for (final CyNetworkView view : netViews) {
			if (cancelled) return;
			
			String xgmmlFile = SessionUtil.getXGMMLFilename(view);
			zos.putNextEntry(new ZipEntry(sessionDir + NETWORK_VIEWS_FOLDER + xgmmlFile) );
			
			CyWriter writer = networkViewWriterFactory.createWriter(zos, view);
			writer.run(taskMonitor);
	
			zos.closeEntry();
			writer = null;
		}
	}

	/**
	 * Writes any files from apps to the session file.
	 *
	 * @throws IOException
	 */
	private void zipFileListMap() throws IOException {
		// fire an event to tell apps we're ready to save!
		Map<String, List<File>> appFileMap = session.getAppFileListMap(); 

		// now write any files to the zip files
		if ((appFileMap != null) && (appFileMap.size() > 0)) {
			byte[] buf = new byte[5000];
			Set<String> appSet = appFileMap.keySet();
		
			for (String appName : appSet) {
				List<File> theFileList = (List<File>) appFileMap.get(appName);
		
				if ((theFileList == null) || (theFileList.size() == 0))
					continue;
	
				for (File theFile : theFileList) {
					if (cancelled) return;
					
					if ((theFile == null) || (!theFile.exists()))
						continue;
	
					zos.putNextEntry(new ZipEntry( sessionDir + APPS_FOLDER + appName + 
					                               "/" + theFile.getName() ) );

					// copy the file contents to the zip output stream
					FileInputStream fileIS = new FileInputStream(theFile);
					int numRead;
			        while ((numRead = fileIS.read(buf)) > -1)
		            	zos.write(buf, 0, numRead);
					fileIS.close();

					zos.closeEntry();
				}
			}
		}
	}

	private void zipTables() throws Exception {
		tableFilenamesBySUID = new HashMap<Long, String>();
		Set<CyTableMetadata> tableData = session.getTables();
		
		for (CyTableMetadata metadata : tableData) {
			if (cancelled) return;
			
			CyTable table = metadata.getTable();
			
			if (table.getSavePolicy() != SavePolicy.SESSION_FILE)
				continue;

			String tableTitle = SessionUtil.escape(table.getTitle());
			String filename;
			CyNetwork network = metadata.getNetwork();
			
			if (network == null) {
				filename = String.format("global/%d-%s.cytable", table.getSUID(), tableTitle);
			} else {
				filename = SessionUtil.getNetworkTableFilename(network, metadata);
			}
			
			tableFilenamesBySUID.put(table.getSUID(), filename);
			zos.putNextEntry(new ZipEntry(sessionDir + TABLES_FOLDER + filename));
			
			try {
				CyWriter writer = tableWriterMgr.getWriter(table, tableFilter, zos);
				writer.run(taskMonitor);
			} finally {
				zos.closeEntry();
			}
		}
	}
	
	private void zipTableProperties() throws Exception {
		zos.putNextEntry(new ZipEntry(sessionDir + TABLES_FOLDER + CYTABLE_STATE_FILE));
		try {
			CyTablesXMLWriter writer = new CyTablesXMLWriter(session.getTables(), tableFilenamesBySUID, zos);
			writer.run(taskMonitor);
		} finally {
			zos.closeEntry();
		}
	}
	
	private void prepareGroups() {
		groupUtils.prepareGroupsForSerialization(session.getNetworks());
	}
}
