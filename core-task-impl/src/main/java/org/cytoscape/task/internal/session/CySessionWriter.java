package org.cytoscape.task.internal.session;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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


import org.cytoscape.session.CySession;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CySessionWriterManager;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.AbstractTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;


/**
 * A utility Task implementation that writes a {@link org.cytoscape.session.CySession} to a file.
 * @CyAPI.Final.Class
 */
public final class CySessionWriter extends AbstractTask implements CyWriter {
	private final CySession session; 
	private final CySessionWriterManager writerMgr; 
	private File outputFile; 

	Logger logger = LoggerFactory.getLogger(CySessionWriter.class);
	
	/**
	 * Constructs this CySessionWriter.
	 * @param writerMgr The {@link org.cytoscape.io.write.CySessionWriterManager} contains single expected
	 * {@link org.cytoscape.io.write.CySessionWriterFactory} to use to write the file.
	 * @param session The {@link org.cytoscape.session.CySession} to be written out. 
	 * @param outputFile The file the {@link org.cytoscape.session.CySession} should be written to.
 	 */
	public CySessionWriter(CySessionWriterManager writerMgr, CySession session, File outputFile) {
		if (writerMgr == null)
			throw new NullPointerException("Writer Manager is null");
		this.writerMgr = writerMgr;

		if (session == null)
			throw new NullPointerException("Session Manager is null");
		this.session = session;

		if (outputFile == null)
			throw new NullPointerException("Output File is null");
		this.outputFile = outputFile;
	}

	/**
	 * The method that will actually write the specified session to the specified
	 * file.
	 * @param tm The {@link org.cytoscape.work.TaskMonitor} provided by the TaskManager execution environment.
	 */
	public final void run(TaskMonitor tm) throws Exception {
		final List<CyFileFilter> filters = writerMgr.getAvailableWriterFilters();
		
		if (filters == null || filters.size() < 1)
			throw new NullPointerException("No Session file filters found");
		if (filters.size() > 1)
			throw new IllegalArgumentException("Found too many session filters.");

		if (!outputFile.getName().endsWith(".cys")) {
			outputFile = new File(outputFile.getPath() + ".cys");
			logger.warn("File name is changed to " + outputFile.getName());
		}

		// Write to a temporary file first, to prevent the original file from being damaged, in case there is any error
		final String filename = outputFile.getName() + ".tmp";
		final File tmpFile = new File(System.getProperty("java.io.tmpdir"), filename);
		tmpFile.deleteOnExit();
		
		final CyWriter writer = writerMgr.getWriter(session, filters.get(0), tmpFile);
		
		if (writer == null)
			throw new NullPointerException("No CyWriter found for specified file type.");

		// If the main task is successfully executed, this task will move the temp file to the actual output path
		final ReplaceFileTask replaceFileTask = new ReplaceFileTask(tmpFile, outputFile);
		
		insertTasksAfterCurrentTask(writer, replaceFileTask);
	}

	static boolean HasFileExtension(final String pathName) {
		final int lastDotPos = pathName.lastIndexOf('.');
		final int lastSlashPos = pathName.lastIndexOf(File.separatorChar);
		return lastSlashPos < lastDotPos; // Yes, this also works if one or both of lastSlashPos and lastDotPos are -1!
	}
	
	private class ReplaceFileTask extends AbstractTask {
		private final File source;
		private final File target;
		
		public ReplaceFileTask(final File source, final File target) {
			this.source = source;
			this.target = target;
		}

		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			if (target.exists()) {
				target.delete();
			}
			boolean success = source.renameTo(target);
			
			if (success) {
				try {
					source.delete();
				} catch (Exception e) {
					logger.warn("Cannot delete temp file: " + source.getAbsolutePath(), e);
				}
			} else {
				throw new RuntimeException("Session not saved: Cannot copy temporary file to " + 
						target.getAbsolutePath());
			}
		}
	}
}
