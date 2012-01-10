/*
 Copyright (c) 2006,2010 The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package org.cytoscape.io.internal.write.session;

import static org.cytoscape.io.internal.util.session.SessionUtil.APPS_FOLDER;
import static org.cytoscape.io.internal.util.session.SessionUtil.BOOKMARKS_FILE;
import static org.cytoscape.io.internal.util.session.SessionUtil.CYSESSION_FILE;
import static org.cytoscape.io.internal.util.session.SessionUtil.CYSESSION_VERSION;
import static org.cytoscape.io.internal.util.session.SessionUtil.CYS_VERSION;
import static org.cytoscape.io.internal.util.session.SessionUtil.CYTABLE_METADATA_FILE;
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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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
import org.cytoscape.io.internal.util.session.SessionUtil;
import org.cytoscape.io.internal.util.session.VirtualColumnSerializer;
import org.cytoscape.io.internal.write.xgmml.XGMMLWriter;
import org.cytoscape.io.write.CyNetworkViewWriterManager;
import org.cytoscape.io.write.CyPropertyWriterManager;
import org.cytoscape.io.write.CyTableWriterManager;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.VizmapWriterManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTable.SavePolicy;
import org.cytoscape.model.CyTableMetadata;
import org.cytoscape.model.VirtualColumnInfo;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.property.session.Cysession;
import org.cytoscape.session.CySession;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Write session states into files.<br>
 * Basic functions of this class are:<br>
 * <ul>
 * <li> 1. Create network files</li>
 * <li> 2. Create session state file</li>
 * <li> 3. Get properties file locations</li>
 * <li> 4. Zip them into one session file "*.cys" </li>
 * </ul>
 *
 * @version 1.0
 * @since 2.3
 * @see cytoscape.data.readers.XGMMLReader
 * @author kono
 *
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
	private final CyNetworkViewWriterManager networkViewWriterMgr;
	private final CyPropertyWriterManager propertyWriterMgr;
	private final CyTableWriterManager tableWriterMgr;
	private final VizmapWriterManager vizmapWriterMgr;
	private final CyRootNetworkManager rootNetworkManager;
	private final CyFileFilter xgmmlFilter;
	private final CyFileFilter bookmarksFilter;
	private final CyFileFilter cysessionFilter;
	private final CyFileFilter propertiesFilter;
	private final CyFileFilter tableFilter;
	private final CyFileFilter vizmapFilter;
	private Map<Long, String> tableFilenamesBySUID;

	public SessionWriterImpl(final OutputStream outputStream, 
	                         final CySession session, 
	                         final CyNetworkViewWriterManager networkViewWriterMgr,
	                         final CyRootNetworkManager rootNetworkManager,
	                         final CyPropertyWriterManager propertyWriterMgr,
	                         final CyTableWriterManager tableWriterMgr,
	                         final VizmapWriterManager vizmapWriterMgr,
	                         final CyFileFilter xgmmlFilter, 
	                         final CyFileFilter bookmarksFilter,
	                         final CyFileFilter cysessionFilter,
	                         final CyFileFilter propertiesFilter,
	                         final CyFileFilter tableFilter,
	                         final CyFileFilter vizmapFilter) {
		this.outputStream = outputStream;
		this.session = session;
		this.networkViewWriterMgr = networkViewWriterMgr;
		this.rootNetworkManager = rootNetworkManager;
		this.propertyWriterMgr = propertyWriterMgr;
		this.tableWriterMgr = tableWriterMgr;
		this.vizmapWriterMgr = vizmapWriterMgr;
		this.xgmmlFilter = xgmmlFilter;
		this.bookmarksFilter = bookmarksFilter;
		this.cysessionFilter = cysessionFilter;
		this.propertiesFilter = propertiesFilter;
		this.tableFilter = tableFilter;
		this.vizmapFilter = vizmapFilter;

		// For now, session ID is time and date
		final DateFormat df = new SimpleDateFormat("yyyy_MM_dd-HH_mm");
		String now = df.format(new Date());

		cysessionDocId = "CytoscapeSession-" + now;
		sessionDir = cysessionDocId + "/"; 
	}

	/**
	 * Write current session to a local .cys file.
	 *
	 * @throws Exception
	 */
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;
		taskMonitor.setProgress(0.0);
		zos = new ZipOutputStream(outputStream); 

		taskMonitor.setStatusMessage("Zip networks...");
		zipVersion();
		zipNetworks();
		zipNetworkViews();
		
		taskMonitor.setProgress(0.1);
		taskMonitor.setStatusMessage("Zip tables...");
		zipTables();
		taskMonitor.setProgress(0.2);
		taskMonitor.setStatusMessage("Zip virtual columns...");
		zipVirtualColumns();
		taskMonitor.setProgress(0.3);
		taskMonitor.setStatusMessage("Zip session info...");
		zipCySession();
		taskMonitor.setProgress(0.4);
		taskMonitor.setStatusMessage("Zip Vizmap...");
		zipVizmap();
		taskMonitor.setProgress(0.5);
		taskMonitor.setStatusMessage("Zip Cytoscape properties...");
		zipProperties();
		taskMonitor.setProgress(0.6);
		taskMonitor.setStatusMessage("Zip bookmarks...");
		zipBookmarks();
		taskMonitor.setProgress(0.7);
		taskMonitor.setStatusMessage("Zip File list...");
		zipFileListMap();
		taskMonitor.setProgress(0.8);
		zos.close();
		taskMonitor.setStatusMessage("Done!");
		taskMonitor.setProgress(1.0);
	}

	/**
	 * Writes the version file, which has no content. The file name itself gives the CYS version.
	 */
	private void zipVersion() throws Exception {
		zos.putNextEntry(new ZipEntry(sessionDir + CYS_VERSION + VERSION_EXT));
		zos.closeEntry();
	}
	
	private void zipVirtualColumns() throws IOException {
		zos.putNextEntry(new ZipEntry(sessionDir + TABLES_FOLDER + CYTABLE_METADATA_FILE));
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(zos, "UTF-8"));
		
		try {
			for (CyTableMetadata metadata : session.getTables()) {
				CyTable table = metadata.getTable();
				String targetTable = tableFilenamesBySUID.get(table.getSUID());
				
				if (targetTable == null) {
					continue;
				}
				
				for (CyColumn column : table.getColumns()) {
					VirtualColumnInfo info = column.getVirtualColumnInfo();
					
					if (!info.isVirtual()) {
						continue;
					}
					
					String sourceTable = tableFilenamesBySUID.get(info.getSourceTable().getSUID());
					
					if (sourceTable == null) {
						logger.warn("Cannot serialize virtual column \"" + column.getName() + "\" of \"" + targetTable
								+ "\" because the source table is null.");
						continue;
					}
					
					VirtualColumnSerializer serializer = new VirtualColumnSerializer(
						column.getName(),
						sourceTable,
						targetTable,
						info.getSourceColumn(),
						info.getSourceJoinKey(),
						info.getTargetJoinKey(),
						info.isImmutable());
					serializer.serialize(writer);
				}
			}
		} finally {
			writer.flush();
			zos.closeEntry();
		}
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
		for (CyProperty<Properties> cyProps : session.getProperties()) {
			String fileName = cyProps.getName() + PROPERTIES_EXT;
			Properties props = cyProps.getProperties(); 
			
			zos.putNextEntry(new ZipEntry(sessionDir + PROPERTIES_FOLDER + fileName) );
	
			CyWriter propertiesWriter = propertyWriterMgr.getWriter(props, propertiesFilter, zos);
			propertiesWriter.run(taskMonitor);
	
			zos.closeEntry();
			propertiesWriter = null;
		}
	}

	/**
	 * Writes the bookmarks.xml file to the session zip.
	 */
	private void zipBookmarks() throws Exception {
		zos.putNextEntry(new ZipEntry(sessionDir + PROPERTIES_FOLDER +BOOKMARKS_FILE) );

		CyWriter bookmarksWriter = propertyWriterMgr.getWriter(session.getBookmarks(), bookmarksFilter, zos);
		bookmarksWriter.run(taskMonitor);

		zos.closeEntry();
		bookmarksWriter = null;
	}

	/**
	 * Writes network files to the session zip. 
	 * @throws Exception
	 */
	private void zipNetworks() throws Exception {
		final Set<CyNetwork> networks = session.getNetworks();
		final Set<CyRootNetwork> rootNetworks = new HashSet<CyRootNetwork>();

		// Zip only root-networks, because sub-networks should be automatically saved with them.
		for (CyNetwork n : networks) {
			CyRootNetwork rn = rootNetworkManager.getRootNetwork(n);
			rootNetworks.add(rn);
		}
		
		for (CyRootNetwork rn : rootNetworks) {
			String xgmmlFile = SessionUtil.getXGMMLFilename(rn);
			zos.putNextEntry(new ZipEntry(sessionDir + NETWORKS_FOLDER + xgmmlFile) );
			
			CyWriter writer = networkViewWriterMgr.getWriter(rn, xgmmlFilter, zos);
			
			// Write the XGMML file *without* graphics attributes--let the Vizmap handle those
			if (writer instanceof XGMMLWriter) {
				((XGMMLWriter) writer).setSessionFormat(true);
			}
			
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

		for (CyNetworkView view : netViews) {
			String xgmmlFile = SessionUtil.getXGMMLFilename(view);
			zos.putNextEntry(new ZipEntry(sessionDir + NETWORK_VIEWS_FOLDER + xgmmlFile) );
			
			CyWriter writer = networkViewWriterMgr.getWriter(view, xgmmlFilter, zos);
			
			// Write the XGMML file for the CYS file
			if (writer instanceof XGMMLWriter) {
				// TODO: there should be a better way of doing that without having to cast the writer. 
				((XGMMLWriter) writer).setSessionFormat(true);
				
				final String visualStyleName = session.getViewVisualStyleMap().get(view);
				
				if (visualStyleName != null)
					((XGMMLWriter) writer).setVisualStyleName(visualStyleName);
			}
			
			writer.run(taskMonitor);
	
			zos.closeEntry();
			writer = null;
		}
	}

	/**
	 * Create cysession.xml file.
	 *
	 * @throws Exception
	 */
	private void zipCySession() throws Exception {
		Cysession cysess = session.getCysession();
		cysess.setId(cysessionDocId);
		cysess.setDocumentVersion(CYSESSION_VERSION);
		
		zos.putNextEntry(new ZipEntry(sessionDir + CYSESSION_FILE) );

		CyWriter cysessionWriter = propertyWriterMgr.getWriter(cysess, cysessionFilter, zos);
		cysessionWriter.run(taskMonitor);

		zos.closeEntry();
		cysessionWriter = null;
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
		tableFilenamesBySUID = new HashMap<Long, String>();
		Set<CyNetwork> networks = session.getNetworks();
		Set<CyNetwork> allNetworks = new HashSet<CyNetwork>();
		
		for (CyNetwork n : networks) {
			allNetworks.add(n);
			// Don't forget the root networks!
			CyRootNetwork rn = rootNetworkManager.getRootNetwork(n);
			allNetworks.add(rn);
		}
		
		Set<CyTableMetadata> tableData = session.getTables();
		
		for (CyTableMetadata metadata : tableData) {
			CyTable table = metadata.getTable();
			
			if (table.getSavePolicy() == SavePolicy.DO_NOT_SAVE) {
				continue;
			}

			String tableTitle = SessionUtil.escape(table.getTitle());
			String fileName;
			CyNetwork network = metadata.getNetwork();
			
			if (network == null) {
				fileName = String.format("global/%d-%s.cytable", table.getSUID(), tableTitle);
			} else {
				if (!allNetworks.contains(network)) {
					continue;
				}
				
				fileName = SessionUtil.getNetworkTableFilename(network, metadata);
			}
			
			tableFilenamesBySUID.put(table.getSUID(), fileName);
			zos.putNextEntry(new ZipEntry(sessionDir + TABLES_FOLDER + fileName));
			
			try {
				CyWriter writer = tableWriterMgr.getWriter(table, tableFilter, zos);
				writer.run(taskMonitor);
			} finally {
				zos.closeEntry();
			}
		}
	}
}
