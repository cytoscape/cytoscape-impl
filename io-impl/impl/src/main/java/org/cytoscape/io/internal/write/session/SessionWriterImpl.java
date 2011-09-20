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
import org.cytoscape.property.session.Cysession;
import org.cytoscape.session.CySession;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

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

	// Enumerate types (node & edge)
	public static final int NODE = 1;
	public static final int EDGE = 2;

	// Name of CySession file.
	private static final String CYSESSION_FILE_NAME = "cysession.xml";
	private static final String VIZMAP_FILE = "session_vizmap.xml";
	private static final String CYPROP_FILE = "session_cytoscape.props";
	private static final String BOOKMARKS_FILE = "session_bookmarks.xml";
	
	// Document versions
	private static final String CYSESSION_VERSION = "3.0";

	// Extension for the xgmml file
	private static final String XGMML_EXT = ".xgmml";

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
		
		zos = new ZipOutputStream(outputStream); 

		for (CyNetworkView netView : session.getNetworkViews())
			zipNetwork(netView);
		zipTables();
		zipVirtualColumns();
		zipCySession();
		zipVizmap();
		zipCytoscapeProps();
		zipBookmarks();
		zipFileListMap();

		zos.close();
	}

	private void zipVirtualColumns() throws IOException {
		zos.putNextEntry(new ZipEntry(sessionDir + SessionUtil.CYTABLE_METADATA_FILE));
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(zos, "UTF-8"));
		try {
			for (CyTableMetadata metadata : session.getTables()) {
				CyTable table = metadata.getCyTable();
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
	private void zipCytoscapeProps() throws Exception {
		zos.putNextEntry(new ZipEntry(sessionDir + CYPROP_FILE) );

		CyWriter propertiesWriter = propertyWriterMgr.getWriter(session.getCytoscapeProperties(), propertiesFilter, zos);
		propertiesWriter.run(taskMonitor);

		zos.closeEntry();
		propertiesWriter = null;
	}

	/**
	 * Writes the bookmarks.xml file to the session zip.
	 */
	private void zipBookmarks() throws Exception {
		zos.putNextEntry(new ZipEntry(sessionDir + BOOKMARKS_FILE) );

		CyWriter bookmarksWriter = propertyWriterMgr.getWriter(session.getBookmarks(), bookmarksFilter, zos);
		bookmarksWriter.run(taskMonitor);

		zos.closeEntry();
		bookmarksWriter = null;
	}

	/**
	 * Writes a network file to the session zip. 
	 * @throws Exception
	 */
	private void zipNetwork(final CyNetworkView view) throws Exception {
		final CyNetwork network = view.getModel();

		String xgmmlFile = SessionUtil.getNetworkFileName(network) + XGMML_EXT;
		zos.putNextEntry(new ZipEntry(sessionDir + xgmmlFile) );
		
		CyWriter writer = networkViewWriterMgr.getWriter(view, xgmmlFilter, zos);
		
		// Write the XGMML file *without* our graphics attributes--let the Vizmap handle those
		if (writer instanceof XGMMLWriter) {
			((XGMMLWriter) writer).setSessionFormat(true);
		}
		
		writer.run(taskMonitor);

		zos.closeEntry();
		writer = null;
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
		
		zos.putNextEntry(new ZipEntry(sessionDir + CYSESSION_FILE_NAME) );

		CyWriter cysessionWriter = propertyWriterMgr.getWriter(cysess, cysessionFilter, zos);
		cysessionWriter.run(taskMonitor);

		zos.closeEntry();
		cysessionWriter = null;
	}

	/**
	 * Writes any files from plugins to the session file.
	 *
	 * @throws IOException
	 */
	private void zipFileListMap() throws IOException {

		// fire an event to tell plugins we're ready to save!
		Map<String, List<File>> pluginFileMap = session.getPluginFileListMap(); 

		// now write any files to the zip files
		if ((pluginFileMap != null) && (pluginFileMap.size() > 0)) {
			byte[] buf = new byte[5000];
			Set<String> pluginSet = pluginFileMap.keySet();
		
			for (String pluginName : pluginSet) {
				List<File> theFileList = (List<File>) pluginFileMap.get(pluginName);
		
				if ((theFileList == null) || (theFileList.size() == 0))
					continue;
	
				for (File theFile : theFileList) {
					if ((theFile == null) || (!theFile.exists()))
						continue;
	
					zos.putNextEntry(new ZipEntry( sessionDir + "plugins/" + pluginName + 
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
		Set<CyNetworkView> views = session.getNetworkViews();
		Set<CyNetwork> includedNetworks = new HashSet<CyNetwork>();
		
		for (CyNetworkView view : views) {
			CyNetwork network = view.getModel();
			includedNetworks.add(network);
		}
		
		Set<CyTableMetadata> tableData = session.getTables();
		
		for (CyTableMetadata metadata : tableData) {
			CyTable table = metadata.getCyTable();
			
			if (table.getSavePolicy() == SavePolicy.DO_NOT_SAVE) {
				continue;
			}

			String tableTitle = SessionUtil.escape(table.getTitle());
			String fileName;
			CyNetwork network = metadata.getCyNetwork();
			
			if (network == null) {
				fileName = String.format("global/%d-%s.cytable", table.getSUID(), tableTitle);
			} else {
				if (!includedNetworks.contains(network)) {
					continue;
				}
				fileName = SessionUtil.getNetworkTableFilename(network, metadata);
			}
			
			tableFilenamesBySUID.put(table.getSUID(), fileName);
			zos.putNextEntry(new ZipEntry(sessionDir + fileName));
			
			try {
				CyWriter writer = tableWriterMgr.getWriter(table, tableFilter, zos);
				writer.run(taskMonitor);
			} finally {
				zos.closeEntry();
			}
		}
	}
}
