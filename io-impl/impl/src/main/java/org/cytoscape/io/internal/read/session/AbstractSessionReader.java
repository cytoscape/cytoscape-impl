package org.cytoscape.io.internal.read.session;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.cytoscape.group.CyGroup;
import org.cytoscape.io.internal.read.MarkSupportedInputStream;
import org.cytoscape.io.internal.util.GroupUtil;
import org.cytoscape.io.internal.util.ReadCache;
import org.cytoscape.io.internal.util.session.SessionUtil;
import org.cytoscape.io.read.CySessionReader;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableMetadata;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySession;
import org.cytoscape.view.model.CyNetworkView;
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

public abstract class AbstractSessionReader extends AbstractTask implements CySessionReader {

	/** Column used to store the parent network SUID that comes from v2.8 files--was used until v3.3. */
	public static final String CY2_PARENT_NETWORK_COLUMN = "Cy2 Parent Network.SUID";
	/**
	 * Column used to store the parent network SUID in versions 3.4+.
	 * It replaces {@link AbstractSessionReader#CY2_PARENT_NETWORK_COLUMN}
	 */
	public static final String CY3_PARENT_NETWORK_COLUMN = "__parentNetwork.SUID";
	
	protected final Logger logger;
	
	protected InputStream sourceInputStream;
	protected final ReadCache cache;
	protected final GroupUtil groupUtil;
	protected final CyServiceRegistrar serviceRegistrar;
	
	protected DummyTaskMonitor taskMonitor;
	
	protected Set<CyProperty<?>> properties = new HashSet<>();
	protected final Set<CyNetwork> networks = new LinkedHashSet<>();
	protected final Set<CyNetworkView> networkViews = new LinkedHashSet<>();
	protected final Set<VisualStyle> visualStyles = new HashSet<>();
	protected final Map<CyNetworkView, String> visualStyleMap = new WeakHashMap<>();
	protected final Set<CyTableMetadata> tableMetadata = new HashSet<>();
	protected final Map<String, List<File>> appFileListMap = new HashMap<>();
	protected final Map<Class<? extends CyIdentifiable>, Map<Object, ? extends CyIdentifiable>> objectMap = new HashMap<>();
	
	private boolean inputStreamRead;

	public AbstractSessionReader(final InputStream sourceInputStream,
								 final ReadCache cache,
								 final GroupUtil groupUtil,
								 final CyServiceRegistrar serviceRegistrar) {
		assert sourceInputStream != null;
		assert cache != null;
		assert groupUtil != null;
		assert serviceRegistrar != null;
		
		this.sourceInputStream = new ReusableInputStream(sourceInputStream); // So it can be read multiple times
		this.cache = cache;
		this.groupUtil = groupUtil;
		this.serviceRegistrar = serviceRegistrar;
		
		this.logger = LoggerFactory.getLogger(this.getClass());
	}

	/**
	 * Read a session file.
	 */
	@Override
	public void run(TaskMonitor tm) throws Exception {
		try {
			init(tm);
			readSessionFile(tm);
			complete(tm);
		} finally {
			cleanUp(tm);
		}
	}

	@Override
	public CySession getSession() {
		CySession ret = new CySession.Builder().networks(networks).networkViews(networkViews)
				.viewVisualStyleMap(visualStyleMap).properties(properties).visualStyles(visualStyles)
				.appFileListMap(appFileListMap).tables(tableMetadata).objectMap(objectMap)
				.build();
	
		return ret;
	}

	/**
	 * Every action that needs to happen before reading the cys file should be executed here.
	 */
	protected void init(TaskMonitor tm) throws Exception {
		inputStreamRead = false;
		
		SessionUtil.setReadingSessionFile(true);
		cache.init();
		
		logger.debug("Reading CYS file...");
		taskMonitor = new DummyTaskMonitor();
		tm.setProgress(0.0);
		tm.setTitle("Read Session File");
		tm.setStatusMessage("Preparing...");
	}

	/**
	 * Hook method for actions that need to be executed after reading the entries of the cys file.
	 */
	protected void complete(TaskMonitor tm) throws Exception {
		if (cancelled) return;
		
		tm.setProgress(0.9);
		tm.setTitle("Process network pointers");
		tm.setStatusMessage("Processing network pointers...");
		processNetworkPointers();
		
		if (cancelled) return;
		
		tm.setProgress(0.95);
		tm.setTitle("Finalize");
		tm.setStatusMessage("Finalizing...");
		createObjectMap();
		createGroups();
		
		tm.setProgress(1.0);
	}
	
	/**
	 * Use this methods to dispose temporary resources.
	 * This method must always be invoked, even if this task is cancelled.
	 */
	protected void cleanUp(TaskMonitor tm) {
		try {
			((ReusableInputStream) sourceInputStream).reallyClose();
		} catch (Exception e) {
			logger.error("Error closing source input stream.", e);
		}
		
		if (cancelled) {
			// Destroy groups
			final Set<CyGroup> groups = groupUtil.getGroups(cache.getNetworks());
			groupUtil.destroyGroups(groups);
			
			// Dispose CyNetworkViews and CyNetworks
			for (final CyNetworkView view : networkViews)
				view.dispose();
			
			final CyRootNetworkManager rootNetworkManager = serviceRegistrar.getService(CyRootNetworkManager.class);
			final Set<CyRootNetwork> rootNetworks = new HashSet<>();
			
			// Get all networks from the ReadCache, because it also contains unregistered networks
			// such as group networks.
			for (final CyNetwork net : cache.getNetworks()) {
				net.dispose();
				rootNetworks.add(rootNetworkManager.getRootNetwork(net));
			}
			
			for (final CyRootNetwork rootNet : rootNetworks)
				rootNet.dispose();
			
			networkViews.clear();
			networks.clear();
		}
		
		cache.dispose();
		SessionUtil.setReadingSessionFile(false);
	}
	
	protected void processNetworkPointers() {
		cache.createNetworkPointers();
	}
	
	/**
	 * Extract Zip entries from the cys file.
	 * @throws Exception
	 */
	protected void readSessionFile(TaskMonitor tm) throws Exception {
		if (!sourceInputStream.markSupported())
			throw new RuntimeException("Mark/Reset not supported.");
		
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
			while ((zen = zis.getNextEntry()) != null && !cancelled) {
				tm.setStatusMessage("Extracting zip entry #" + ++count);
				
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
	
	protected InputStream findEntry(String entry) throws IOException {
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

				if (name.equals(entry)) {
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
	
	abstract void createObjectMap();

	protected void createGroups() {
		groupUtil.createGroups(networks, networkViews);
	}
	
	/**
	 *  We need this class to avoid the progress-bar showing back-forth  when extract zipEntries.
	 */
	protected class DummyTaskMonitor implements TaskMonitor {
		@Override
		public void setTitle(String title) {
		}
		@Override
		public void setProgress(double progress) {
		}
		@Override
		public void setStatusMessage(String statusMessage) {
		}
		@Override
        public void showMessage(TaskMonitor.Level level, String message) {
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
