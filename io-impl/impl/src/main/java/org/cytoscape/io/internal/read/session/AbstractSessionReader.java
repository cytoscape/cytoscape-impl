/*
 Copyright (c) 2006, 2011, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.io.internal.read.session;

import static org.cytoscape.io.internal.util.session.SessionUtil.ENTRY_SUID_COLUMN;
import static org.cytoscape.io.internal.util.session.SessionUtil.ID_MAPPING_TABLE;
import static org.cytoscape.io.internal.util.session.SessionUtil.INDEX_COLUMN;
import static org.cytoscape.io.internal.util.session.SessionUtil.NETWORK_POINTERS_TABLE;
import static org.cytoscape.io.internal.util.session.SessionUtil.NODE_SUID_COLUMN;
import static org.cytoscape.io.internal.util.session.SessionUtil.ORIGINAL_ID_COLUMN;
import static org.cytoscape.io.internal.util.session.SessionUtil.ORIGINAL_NETWORK_ID_COLUMN;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.cytoscape.io.internal.read.MarkSupportedInputStream;
import org.cytoscape.io.internal.util.session.SessionUtil;
import org.cytoscape.io.read.CySessionReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyTableMetadata;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.session.Cysession;
import org.cytoscape.session.CySession;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSessionReader extends AbstractTask implements CySessionReader {

	protected final Logger logger;
	
	protected InputStream sourceInputStream;
	protected final CyTableManager tableManager;
	protected final CyTableFactory tablefacory;
	
	protected TaskMonitor tm;
	protected DummyTaskMonitor taskMonitor;
	
	protected Cysession cysession;
	protected Bookmarks bookmarks;
	protected Properties cytoscapeProps;
	protected final Set<CyNetwork> networks = new HashSet<CyNetwork>();
	protected final Set<CyNetworkView> networkViews = new HashSet<CyNetworkView>();
	protected final Set<VisualStyle> visualStyles = new HashSet<VisualStyle>();
	protected final Map<CyNetworkView, String> visualStyleMap = new HashMap<CyNetworkView, String>();
	protected final Set<CyTableMetadata> tableMetadata = new HashSet<CyTableMetadata>();
	protected final Map<String, List<File>> pluginFileListMap = new HashMap<String, List<File>>();
	
	private CyTable idMappingTable;
	private CyTable networkPointersTable;

	private boolean inputStreamRead;
	
	public AbstractSessionReader(final InputStream sourceInputStream,
								 final CyTableManager tableManager,
								 final CyTableFactory tablefacory) {
		if (sourceInputStream == null)
			throw new NullPointerException("input stream is null!");
		// So it can be read multiple times:
		this.sourceInputStream = new ReusableInputStream(sourceInputStream);

		if (tableManager == null)
			throw new NullPointerException("table manager is null!");
		this.tableManager = tableManager;
		
		if (tablefacory == null)
			throw new NullPointerException("table factory is null!");
		this.tablefacory = tablefacory;
		
		this.logger = LoggerFactory.getLogger(this.getClass());
	}

	/**
	 * Read a session file.
	 */
	@Override
	public void run(TaskMonitor tm) throws Exception {
		this.tm = tm;
		
		try {
			init();
			readSessionFile();
			complete();
		} finally {
			cleanUp();
		}
	}

	@Override
	public CySession getCySession() {
		CySession ret = new CySession.Builder().networks(networks).networkViews(networkViews)
				.viewVisualStyleMap(visualStyleMap).cytoscapeProperties(cytoscapeProps).visualStyles(visualStyles)
				.bookmarks(bookmarks).cysession(cysession).pluginFileListMap(pluginFileListMap).tables(tableMetadata)
				.build();
	
		return ret;
	}
	
	/**
	 * Every action that needs to happen before reading the cys file should be executed here.
	 */
	protected void init() throws Exception {
		inputStreamRead = false;
		
		SessionUtil.setReadingSessionFile(true);
		createTempTables();
		
		logger.debug("Reading CYS file...");
		taskMonitor = new DummyTaskMonitor();
		tm.setProgress(0.0);
		tm.setTitle("Read Session File");
		tm.setStatusMessage("Preparing...");
	}

	/**
	 * Hook method for actions that need to be executed after reading the entries of the cys file.
	 */
	protected void complete() throws Exception {
		tm.setProgress(0.9);
		tm.setTitle("Process network pointers");
		tm.setStatusMessage("Processing network pointers...");
		processNetworkPointers();
		
		tm.setProgress(1.0);
	}

	/**
	 * Use this methods to dispose temporary resources.
	 */
	protected void cleanUp() {
		try {
			((ReusableInputStream) sourceInputStream).reallyClose();
		} catch (Exception e) {
			logger.error("Error closing source input stream.", e);
		}
		
		deleteTempTables();
		SessionUtil.setReadingSessionFile(false);
	}
	
	protected void processNetworkPointers() {
		if (networkPointersTable != null) {
			final List<CyRow> rows = networkPointersTable.getAllRows();
			
			if (rows.size() == 0)
				return;
			
			// Create lookup maps
			final Map<Long, CyNetwork> netMap = new HashMap<Long, CyNetwork>();
			final Map<Long, CyNode> nodeMap = new HashMap<Long, CyNode>();
			
			for (CyNetwork net : networks) {
				netMap.put(net.getSUID(), net);
				final List<CyNode> nodes = net.getNodeList();
				
				for (CyNode n : nodes)
					nodeMap.put(n.getSUID(), n);
			}
			
			// Iterate the rows and recreate the network pointers
			for (CyRow r : rows) {
				final Long nodeId = r.get(NODE_SUID_COLUMN, Long.class);
				final String oldNetId = r.get(ORIGINAL_NETWORK_ID_COLUMN, String.class);
				final Long netId = getNewId(oldNetId);
				CyNetwork network = netMap.get(netId);
				
				if (network != null) {
					CyNode node = nodeMap.get(nodeId);
					
					if (node != null)
						node.setNetworkPointer(network);
					else
						logger.error("Cannot recreate network pointer for network " + netId + ": Cannot find node "
								+ nodeId);
				} else {
					logger.error("Cannot recreate network pointer: Cannot find network " + oldNetId);
				}
			}
		}
	}
	
	/**
	 * Extract Zip entries from the cys file.
	 * @throws Exception
	 */
	protected void readSessionFile() throws Exception {
		if (!sourceInputStream.markSupported()) {
			throw new RuntimeException("Mark/Reset not supported!");
		}
		
		if (inputStreamRead)
			sourceInputStream.reset();
		
		// So it can be read again if necessary.
		sourceInputStream.mark(Integer.MAX_VALUE);
		inputStreamRead = true;
		
		ZipInputStream zis = new ZipInputStream(sourceInputStream);
		int count = 0;
	
		try {
			ZipEntry zen = null;
	
			// Extract cysession.xml and the other files, except the XGMML ones:
			while ((zen = zis.getNextEntry()) != null) {
				this.tm.setStatusMessage("Extracting zip entry #" + ++count);
				
				String entryName = zen.getName();
				InputStream is = new MarkSupportedInputStream(zis);
	
				try {
					this.handleEntry(is, entryName);
				} catch (Exception e) {
					logger.error("Failed reading session entry: " + entryName, e);
				} finally {
					if (is != null) {
						try {
							is.close();
						} catch (final Exception ex) {
							logger.error("Unable to close ZIP entry's input stream.", ex);
						}
						is = null;
					}
				}
	
				zis.closeEntry();
			}
		} finally {
			if (zis != null)
				zis.close();
			zis = null;
		}
	}
	
	/**
	 * The implementation of this method should handle the passed zip entry.
	 * @param is
	 * @param entryName
	 * @throws Exception
	 */
	abstract void handleEntry(InputStream is, String entryName) throws Exception;
	
	protected InputStream findEntry(String regex) throws IOException {
		InputStream is = null;
		ZipInputStream zis = null;

		try {
			sourceInputStream.reset();
			zis = new ZipInputStream(sourceInputStream);
			ZipEntry zen = null;
			boolean found = false;

			while (!found && (zen = zis.getNextEntry()) != null) {
				String name = zen.getName();
				is = new MarkSupportedInputStream(zis);

				if (name.matches(regex)) {
					found = true;
				}
				
				zis.closeEntry();
			}
		} finally {
			if (zis != null) {
				try {
					zis.close();
				} catch (final Exception ex) {
					logger.error("Unable to close zip input stream.", ex);
				}
				zis = null;
			}
		}
		
		return is;
	}
	
	protected String getOldId(Long suid) {
		Collection<CyRow> rows = idMappingTable.getMatchingRows(ENTRY_SUID_COLUMN, suid);
		
		if (rows != null) {
			for (CyRow r : rows) {
				return r.get(ORIGINAL_ID_COLUMN, String.class);
			}
		}
		
		return null;
	}
	
	protected Long getNewId(String oldId) {
		CyRow r = idMappingTable.getRow(oldId);		
		return r != null ? r.get(ENTRY_SUID_COLUMN, Long.class) : null;
	}
	
	protected void createTempTables() {
		createIdMappingTable();
		createNetworkPointersTable();
	}
	
	protected void deleteTempTables() {
		deleteIdMappingTable();
		deleteNetworkPointersTable();
	}

	private void createIdMappingTable() {
		deleteIdMappingTable();
		
		// The ORIGINAL_ID_COLUMN has to be a String, because 2.x XGMML edges uses the label as ID!
		idMappingTable = tablefacory.createTable(ID_MAPPING_TABLE, ORIGINAL_ID_COLUMN, String.class, false, true);
		idMappingTable.createColumn(ENTRY_SUID_COLUMN, Long.class, true);
		idMappingTable.createColumn(INDEX_COLUMN, Integer.class, true);
		
		tableManager.addTable(idMappingTable);
		SessionUtil.setIdMappingTableSUID(idMappingTable.getSUID());
	}
	
	private void createNetworkPointersTable() {
		deleteNetworkPointersTable();
		
		networkPointersTable = tablefacory.createTable(NETWORK_POINTERS_TABLE, NODE_SUID_COLUMN,
				Long.class, false, true);
		networkPointersTable.createColumn(ORIGINAL_NETWORK_ID_COLUMN, String.class, true);
		
		tableManager.addTable(networkPointersTable);
		SessionUtil.setNetworkPointersTableSUID(networkPointersTable.getSUID());
	}
	
	private void deleteIdMappingTable() {
		if (idMappingTable != null && tableManager.getTable(idMappingTable.getSUID()) != null) {
			tableManager.deleteTable(idMappingTable.getSUID());
			idMappingTable = null;
			SessionUtil.setIdMappingTableSUID(null);
		}
	}
	
	private void deleteNetworkPointersTable() {
		if (networkPointersTable != null && tableManager.getTable(networkPointersTable.getSUID()) != null) {
			tableManager.deleteTable(networkPointersTable.getSUID());
			networkPointersTable = null;
			SessionUtil.setNetworkPointersTableSUID(null);
		}
	}
	
	/**
	 *  We need this class to avoid the progress-bar showing back-forth  when extract zipEntries.
	 */
	protected class DummyTaskMonitor implements TaskMonitor {
		public void setTitle(String title) {
		}

		public void setProgress(double progress) {
		}

		public void setStatusMessage(String statusMessage) {
		}
	}
	
	private class ReusableInputStream extends InputStream {
	    private InputStream stream;

	    public ReusableInputStream(InputStream stream) {
	        this.stream = stream;
	    }

	    @Override
	    public int read() throws IOException {
	        return stream.read();
	    }

	    @Override
	    public void close() {
	        // ignore
	    }
	    
	    @Override
		public int available() throws IOException {
			return stream.available();
		}

		@Override
		public synchronized void mark(int readlimit) {
			stream.mark(readlimit);
		}

		@Override
		public boolean markSupported() {
			return stream.markSupported();
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return stream.read(b, off, len);
		}

		@Override
		public int read(byte[] b) throws IOException {
			return stream.read(b);
		}

		@Override
		public synchronized void reset() throws IOException {
			stream.reset();
		}

		@Override
		public long skip(long n) throws IOException {
			return stream.skip(n);
		}

		public void reallyClose() throws IOException {
	        stream.close();
	    }
	}
}
