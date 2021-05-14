package org.cytoscape.task.internal.session;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;

import org.apache.commons.io.FilenameUtils;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.session.events.SessionAboutToBeLoadedEvent;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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
 * Call the session reader and read everything in the zip archive.<br>
 * setAcceleratorCombo(java.awt.event.KeyEvent.VK_O, ActionEvent.CTRL_MASK);
 */
public class OpenSessionCommandTask extends AbstractOpenSessionTask {

	public static final String TEMP_FILE_EXT = ".tmpCYS";
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	@ProvidesTitle
	public String getTitle() {
		return "Open Session";
	}

	@Tunable(
			description = "Session file to load:",
			longDescription = "The path to the session file (.cys) to be loaded.",
			exampleStringValue = "/Users/johndoe/Downloads/MySession.cys",
			params = "fileCategory=session;input=true",
			context = "nogui"
	)
	public File file;
	
	@Tunable(
			description = "URL from which to load the session file:",
			longDescription = "A URL that provides a session file.",
			exampleStringValue = "/Users/johndoe/Downloads/MySession.cys",
			params = "fileCategory=session;input=true",
			context = "nogui"
	)
	public String url;

	public OpenSessionCommandTask(CyServiceRegistrar serviceRegistrar) {
		super(serviceRegistrar);
	}
	
	private File tmpFile;

	@Override
	public void run(TaskMonitor tm) throws Exception {
		var eventHelper = serviceRegistrar.getService(CyEventHelper.class);
		var sessionManager = serviceRegistrar.getService(CySessionManager.class);
		
		try {
			try {
				tm.setTitle("Open Session");
				tm.setStatusMessage("Opening Session File.\n\nIt may take a while.\nPlease wait...");
				tm.setProgress(0.0);
				
				if (url != null)
					url = url.trim();
		
				if (file == null && (url == null || url.isEmpty()))
					throw new NullPointerException("No file or URL specified.");
				
				var readerMgr = serviceRegistrar.getService(CySessionReaderManager.class);
				
				if (url != null && file == null && !url.toLowerCase().startsWith("file://")) {
					// Download it as a temp file in order to prevent errors when trying to read parts of the zip file
					// over a network connection or when calling InputStream.markSupported()
					// -- see: https://cytoscape.atlassian.net/browse/CYTOSCAPE-12556
					FileOutputStream tmpStream = null;
					
					try (var channel = Channels.newChannel(new URL(url).openStream())) {
						tmpFile = File.createTempFile(url, TEMP_FILE_EXT);
						tmpFile.deleteOnExit();
						
						tmpStream = new FileOutputStream(tmpFile);
						tmpStream.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
						
						file = tmpFile;
					} catch (Exception e) {
						logger.error("Cannot create temp file for remote session file.", e);
					} finally {
						if (tmpStream != null) {
							try {
								tmpStream.close();
							} catch (Exception e) {
								// Ignore...
							}
						}
					}
				}
				
				if (file != null)
					reader = readerMgr.getReader(file.toURI(), file.getName());
				else
					reader = readerMgr.getReader(new URI(url), url);
				
				if (reader == null)
					throw new NullPointerException("Failed to find appropriate reader for file: " + file);
				
				// Let everybody know the current session will be destroyed
				eventHelper.fireEvent(new SessionAboutToBeLoadedEvent(this));
				tm.setProgress(0.1);
				
				// Dispose the current session before loading the new one
				serviceRegistrar.getService(CySessionManager.class).disposeCurrentSession();
				tm.setProgress(0.2);
				
				// Now we can read the new session
				if (!cancelled)
					reader.run(tm);
				
				tm.setProgress(0.8);
			} catch (Exception e) {
				disposeCancelledSession(e, sessionManager);
				throw e;
			}
			
			if (cancelled)
				disposeCancelledSession(null, sessionManager);
			else
				changeCurrentSession(sessionManager, tm);
		} finally {
			// plug big memory leak
			reader = null;
			
			// Delete the tmp file now, otherwise the user could inadvertently save the session
			// to the temp directory later
			if (tmpFile != null) {
				try {
					tmpFile.delete();
				} catch (Exception e) {
					logger.error("Cannot delete temp cys file.", e);
				}
				
				tmpFile = file = null;
			}
		}
	}
		
	private void changeCurrentSession(CySessionManager sessionManager, TaskMonitor tm) throws Exception {
		var newSession = reader.getSession();
		
		if (newSession == null)
			throw new NullPointerException("Session could not be read for: " + (file != null ? file : url));
		
		var fileName = file != null ? file.getAbsolutePath() : FilenameUtils.getName(new URL(url).getPath());
		sessionManager.setCurrentSession(newSession, fileName);
		
		// Set Current network: this is necessary to update GUI
		var appManager = serviceRegistrar.getService(CyApplicationManager.class);
		var currentEngine = appManager.getCurrentRenderingEngine();
		
		if (currentEngine != null)
			appManager.setCurrentRenderingEngine(currentEngine);
		
		tm.setProgress(1.0);
		tm.setStatusMessage("Session file " + fileName + " successfully loaded.");
		
		// Add this session file URL as the most recent file
		if (file != null)
			serviceRegistrar.getService(RecentlyOpenedTracker.class).add(file.toURI().toURL());
	}
}
