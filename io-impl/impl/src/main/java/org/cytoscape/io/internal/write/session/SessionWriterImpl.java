package org.cytoscape.io.internal.write.session;

import static org.cytoscape.io.internal.util.session.SessionUtil.*;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;

import org.cytoscape.application.CyApplicationManager;
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
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySession;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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
 * Write session states into files and zip them into one session file "*.cys".
 *
 * @see org.cytoscape.io.internal.read.session.Cy2SessionReaderImpl
 * @see org.cytoscape.io.internal.read.session.Cy3SessionReaderImpl
 */
public class SessionWriterImpl extends AbstractTask implements CyWriter {

	private static final Logger logger = LoggerFactory.getLogger("org.cytoscape.application.userlog");
	
	private static final String VIZMAP_FILE = "session_vizmap.xml";
	private static final String VIZMAP_TABLE_FILE = "session_vizmap_tables.xml";
	private static final String THUMBNAIL_FILE = "session_thumbnail.png";
	
	private static final int THUMBNAIL_WIDTH = 96;
	private static final int THUMBNAIL_HEIGHT = 96;
	
	private final String cysessionDocId;
	private final String sessionDir;
	private ZipOutputStream zos; 
	private TaskMonitor taskMonitor;

	private final OutputStream outputStream;
	private final CySession session;
	private final CyPropertyWriterManager propertyWriterMgr;
	private final CyTableWriterManager tableWriterMgr;
	private final VizmapWriterManager vizmapWriterMgr;
	private final CyNetworkViewWriterFactory networkViewWriterFactory;
	private final CyFileFilter bookmarksFilter;
	private final CyFileFilter propertiesFilter;
	private final CyFileFilter tableFilter;
	private final CyFileFilter vizmapFilter;
	private final GroupUtil groupUtils;
	private Map<Long, String> tableFilenamesBySUID;
	private Map<String, VisualStyle> tableVisualStylesByName;

	private final CyServiceRegistrar serviceRegistrar;

	public SessionWriterImpl(final OutputStream outputStream, 
	                         final CySession session, 
	                         final CyPropertyWriterManager propertyWriterMgr,
	                         final CyTableWriterManager tableWriterMgr,
	                         final VizmapWriterManager vizmapWriterMgr,
	                         final CyNetworkViewWriterFactory networkViewWriterFactory,
	                         final CyFileFilter bookmarksFilter,
	                         final CyFileFilter propertiesFilter,
	                         final CyFileFilter tableFilter,
	                         final CyFileFilter vizmapFilter,
	                         final GroupUtil groupUtils,
	                         final CyServiceRegistrar serviceRegistrar) {
		this.outputStream = outputStream;
		this.session = session;
		this.propertyWriterMgr = propertyWriterMgr;
		this.tableWriterMgr = tableWriterMgr;
		this.vizmapWriterMgr = vizmapWriterMgr;
		this.networkViewWriterFactory = networkViewWriterFactory;
		this.bookmarksFilter = bookmarksFilter;
		this.propertiesFilter = propertiesFilter;
		this.tableFilter = tableFilter;
		this.vizmapFilter = vizmapFilter;
		this.groupUtils = groupUtils;
		this.serviceRegistrar = serviceRegistrar;

		// For now, session ID is time and date
		final DateFormat df = new SimpleDateFormat("yyyy_MM_dd-HH_mm");
		String now = df.format(new Date());

		cysessionDocId = "CytoscapeSession-" + now;
		sessionDir = cysessionDocId + "/";
		
		tableVisualStylesByName = new HashMap<>();
		
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
		zipSessionThumbnail();
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
		Set<VisualStyle> networkStyles = session.getVisualStyles();
		Set<VisualStyle> tableStyles = session.getTableStyles();

		zos.putNextEntry(new ZipEntry(sessionDir + VIZMAP_FILE));

		CyWriter vizmapWriter = vizmapWriterMgr.getWriter(networkStyles, tableStyles, vizmapFilter, zos);
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
			
			String filename = null;
			CyFileFilter filter = null;
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
		final CyRootNetworkManager rootNetworkManager = serviceRegistrar.getService(CyRootNetworkManager.class);
		final Set<CyNetwork> networks = session.getNetworks();
		final Set<CyRootNetwork> rootNetworks = new HashSet<>();

		// Zip only root-networks, because sub-networks should be automatically saved with them.
		for (final CyNetwork n : networks) {
			final CyRootNetwork rn = rootNetworkManager.getRootNetwork(n);
			
			if (rn.getSavePolicy() == SavePolicy.SESSION_FILE)
				rootNetworks.add(rn);
		}
		
		for (CyRootNetwork rn : rootNetworks) {
			if (cancelled) return;
			
			String xgmmlFile = SessionUtil.getXGMMLFilename(rn);
			if (xgmmlFile.contains("_ERROR")) throw new Exception("Simulating exception...");
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
					int numRead = 0;
			        while ((numRead = fileIS.read(buf)) > -1)
		            	zos.write(buf, 0, numRead);
					fileIS.close();

					zos.closeEntry();
				}
			}
		}
	}

	private void zipTables() throws Exception {
		tableFilenamesBySUID = new HashMap<>();
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
			CyTablesXMLWriter writer = new CyTablesXMLWriter(session.getTables(), session.getTableVisualStyleMap(), tableFilenamesBySUID, zos);
			writer.run(taskMonitor);
		} finally {
			zos.closeEntry();
		}
	}
	
	private void zipSessionThumbnail() {
		CyNetworkView view = serviceRegistrar.getService(CyApplicationManager.class).getCurrentNetworkView();
		
		if (view == null)
			view = session.getNetworkViews().isEmpty() ? null : session.getNetworkViews().iterator().next();
		
		if (view != null) {
			Collection<RenderingEngine<?>> engines =
				serviceRegistrar.getService(RenderingEngineManager.class).getRenderingEngines(view);

			Image img = null;
			
			for (RenderingEngine<?> re : engines) {
				try {
					img = re.createImage(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT);
					break;
				} catch (Throwable t) {
					// Better just log the error than prevent the session from being saved
					// only because a third party renderer throws an exception,
					// even if it never creates/saves a thumbnail.
					logger.error("Cannot create session thumbnail", t);
				}
			}
				
			if (img != null) {
				try {
					final RenderedImage ri;
					
					if (img instanceof RenderedImage) {
						ri = (RenderedImage) img;
					} else {
						ri = new BufferedImage(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, BufferedImage.TYPE_INT_ARGB);
						Graphics g = ((BufferedImage) ri).getGraphics();
					    g.drawImage(img, 0, 0, null);
					    g.dispose();
					}
					
					zos.putNextEntry(new ZipEntry(sessionDir + THUMBNAIL_FILE));
					ImageIO.write(ri, "png", zos);
					zos.closeEntry();
				} catch (Exception e) {
					// Just log the error. Don't let it prevent the session from being saved!
					logger.error("Cannot save session thumbnail", e);
				}
			}
		}
	}

	private void prepareGroups() {
		groupUtils.prepareGroupsForSerialization(session.getNetworks());
	}
}
